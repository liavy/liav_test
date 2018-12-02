/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/24  Samuel Kounev,          Conversion of BuyerSes to BuyerMDB.
 *              Darmstadt Univ.
 *  2003/03/28  Samuel Kounev,          Moved call-level initialization logic from
 *              Darmstadt Univ.         ejbCreate to onMessage. Unlike for Session
 *                                      beans, for MDBs ejbCreate is called just once
 *                                      when the bean is first created and added to a
 *                                      pool. Therefore ejbCreate should contain ONLY
 *                                      global (one-time) initialization logic such as
 *                                      JNDI reference lookups.
 *                                      Removed redundant closeConnection method.
 */

package org.spec.jappserver.supplier.buyermdb.ejb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.*;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.ComponentDemand;
import org.spec.jappserver.common.DataIntegrityException;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.supplier.helper.ComponentOrder;
import org.spec.jappserver.supplier.helper.ComponentQuantities;
import org.spec.jappserver.supplier.helper.NonSecureXmlCommand;
import org.spec.jappserver.supplier.helper.PartSpec;
import org.spec.jappserver.supplier.helper.SecureXmlCommand;
import org.spec.jappserver.supplier.helper.SendXmlCommand;
import org.spec.jappserver.supplier.poent.ejb.POEntHomeLocal;
import org.spec.jappserver.supplier.poent.ejb.POEntLocal;
import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntHomeLocal;
import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntLocal;
import org.spec.jappserver.supplier.supplierent.ejb.SupplierEntHomeLocal;
import org.spec.jappserver.supplier.supplierent.ejb.SupplierEntLocal;

public class BuyerMDB implements MessageDrivenBean, MessageListener, SessionBean {

    //private MessageDrivenContext         messageDrivenContext;
    private EJBContext ejbContext; 
    protected Debug                          debug;
    protected boolean                        debugging = false;

    private SComponentEntHomeLocal       componentHome;
    private POEntHomeLocal               poHome;
    private SupplierEntHomeLocal         supplierHome;
    private String                           servlet;
    private String                           servletHost;
    private int                              servletPort;
    private boolean                          isSecure;

    private Vector                           required;
    private int                              siteID;
    private int                                          woID;
    private Vector                           compIDs;
    private SupplierEntLocal        chosen = null;

//    static private AtomicLong counter = new AtomicLong();
//    static private AtomicInteger parallel = new AtomicInteger();
//    
//    static void inc() {
//        int p = parallel.incrementAndGet();
//        long c = counter.incrementAndGet();
//        if (c % 100 == 0 || c > 18) {
//            System.err.println("MDBcounter=" + p);
//        }
//    }
//
//    static void dec() {
//        parallel.decrementAndGet();
//    }
    
    public void onMessage(Message message) {
//        inc();
        try {
            required     = new Vector();
            compIDs     = new Vector();

            siteID = message.getIntProperty("siteID");
            woID = message.getIntProperty("woID");
//          long publishTime = message.getLongProperty("publishTime");
//          long receiveTime = System.currentTimeMillis();
            Vector componentsToPurchase = (Vector) ((ObjectMessage) message).getObject();
            ComponentDemand compDemand;
            for( int i=0; i < componentsToPurchase.size(); i++ ) {
                compDemand = (ComponentDemand) componentsToPurchase.elementAt(i);
                add(compDemand.getComponentId(), compDemand.getQty());
            }
            purchase();
        } catch( Exception e ) { // JMS onMessage should handle all exceptions
            if( debugging )
                debug.println(1, "Exception thrown in BuyerMDB:onMessage " + e);
            debug.printStackTrace(e);
            //messageDrivenContext.setRollbackOnly();
            ejbContext.setRollbackOnly();
        }
//        dec();
    }

    /**
     * add: add a component to the collection of components to be ordered.
     * First check if the component exists, and if there is an existing PO.
     * @param componentID - the Id of the component to be ordered.
     * @param quantityRequired - the qty that is required.
     *
     */
    public void add(String componentID, int quantityRequired) {
        try {
            SComponentEntLocal component =
            componentHome.findByPrimaryKey(componentID);

            // See if there is an outstanding order
            if( component.checkForPO(quantityRequired) == false ) {
                compIDs.addElement(componentID);
                required
                .addElement(new ComponentQuantities(component,
                                                    quantityRequired));
            } else {
                component.updateDemand(quantityRequired);
            }
        } catch( FinderException e ) {
            if( debugging )
                debug.println(1, "Finder Exception");
            throw new EJBException(e);
        }
    }

    /**
     * purchase: choose the supplier that can supply all parts that
     * are needed within the required leadTime and for the cheapest price.
     *
     */
    public void purchase()
    throws FinderException, CreateException, SPECjAppServerException {

        if( required.size() == 0 ) {
            if( debugging )
                debug.println(2, "No parts to order");
            return;
        }

        Iterator suppliers;

        try {
            suppliers = supplierHome.findAll().iterator();
        } catch( FinderException fe ) {
            throw new EJBException(fe);
        }

        if( !suppliers.hasNext() ) {
            /** there are not any suppliers loaded into the DB **/
            throw new DataIntegrityException(
                                            "There are not any suppliers in Database");
        }

        /** For each supplier, see if they can deliver all parts within leadTime.
         *  If they can deliver parts, determine if they are the cheapest.
         *  Choose the cheapest as the supplier.
         */
        double     minPrice          = -0.5;
        PartSpec[] cheapestSuppliers = new PartSpec[required.size()];

        while( suppliers.hasNext() ) {
            double      totalPrice = 0.0;
            SupplierEntLocal supplier   =
            (SupplierEntLocal) suppliers.next();

            boolean     canSupply = true;
            int         i          = 0;

            while( /*canSupply &&*/i < required.size() ) {
                try {
                    SComponentEntLocal component =
                    ((ComponentQuantities) required.elementAt(i))
                    .component;
                    PartSpec      ps        =
                    supplier.getPartSpec((String) compIDs.elementAt(i));

                    if( ps.delDate > component.getLeadTime() ) {
                        canSupply = false;
                    } else {
                        double price = ps.calculatePrice();

                        totalPrice += price;

                        /** check if cheapest supplier for this part*/
                        PartSpec cheapest = cheapestSuppliers[i];

                        if( cheapest != null ) {
                            if( price < cheapest.calculatePrice() ) {
                                cheapestSuppliers[i] = ps;
                            }
                        } else {
                            cheapestSuppliers[i] = ps;
                        }
                    }
                } catch( FinderException fe ) {
                    canSupply = false;
                }

                i++;
            }

            if( canSupply && ((minPrice < 0) || (totalPrice < minPrice)) ) {
                minPrice = totalPrice;
                chosen   = supplier;
            }
        }

        if( chosen != null ) {
            createPO();
        } else {
            doIndividualPOs(cheapestSuppliers);
        }

        if( debugging )
            debug.println(3, "Order processing completed");
    }

    /**
     * doIndividualPOs: Creates and sends 1 PO per part. Added to take care
          * of the Case when a supplier cannot be found that can supply all parts
     * required.
     *
     * Damian.
     */
    private void doIndividualPOs(PartSpec[] cheapestSuppliers)
    throws CreateException, SPECjAppServerException {

        if( debugging )
            debug.println(3, "doIndividualPOs");

        for( int i = 0; i < cheapestSuppliers.length; i++ ) {
            try {
                ComponentOrder[] order = new ComponentOrder[1];

                order[0] = getOrderInfo(i, cheapestSuppliers[i]);

                POEntLocal purchOrd =
                poHome.create(cheapestSuppliers[i].suppID.intValue(),
                              siteID, order);

                sendPO(purchOrd);
            } catch( EJBException e ) {
                debug.printStackTrace(e);

                throw new EJBException(e);
            }
        }
    }

    /**
     * getOrderInfo: get information required to place an order for a component
     */
    private ComponentOrder getOrderInfo(int index, PartSpec ps) {

        ComponentQuantities componentQuant =
        (ComponentQuantities) required.elementAt(index);
        SComponentEntLocal       component      = componentQuant.component;
        String              compID        =
        (String) compIDs.elementAt(index);
        int                 qtyToOrder   = 0;

        if( componentQuant.quantity <= ps.qty ) {
            qtyToOrder = ps.qty;
        } else {
            qtyToOrder =
            (int) Math
            .ceil((double) componentQuant.quantity / (double) ps.qty)
            * ps.qty;
        }

        double balance = (qtyToOrder / ps.qty) * (1 - ps.disc) * ps.price;

        component.updateQuantities(qtyToOrder, componentQuant.quantity);

        return new ComponentOrder(compID, qtyToOrder,
                                  component.getLeadTime(), balance);
    }

    /**
     * createPO: create the Purchase Order for required components.
     */
    private void createPO()
    throws FinderException, CreateException, SPECjAppServerException {

        ComponentOrder[] cq = new ComponentOrder[required.size()];

        if( debugging )
            debug.println(3, "createPO");

        try {
            for( int i = 0; i < cq.length; i++ ) {

                /*******
                 *              ComponentQuantities componentQuant = (ComponentQuantities)
             *                                  required.elementAt(i);

                        SComponentEntLocal component = componentQuant.component;

                        String compID = component.getID();

                        PartSpec ps = chosen.getPartSpec(compID);
                        int qtyToOrder = 0;

                        if(componentQuant.quantity <= ps.qty)
                                qtyToOrder = ps.qty;
                        else
                                qtyToOrder = (int) Math.ceil((double)componentQuant.quantity/
                                                        (double)ps.qty) * ps.qty;

                        double balance = (qtyToOrder / ps.qty) * (1 - ps.disc) * ps.price;
                        cq[i] = new ComponentOrder(compID,
                                        qtyToOrder, component.getLeadTime(), balance);
                 *
                 *
                 *******/
                String compID = (String) compIDs.elementAt(i);

                cq[i] = getOrderInfo(i, chosen.getPartSpec(compID));

                // component.updateQuantities(qtyToOrder, componentQuant.quantity);
            }

            POEntLocal purchOrd = poHome.create(chosen.getID(), siteID, cq);

            sendPO(purchOrd);
        } catch( EJBException re ) {
            throw new EJBException(re);
        }
    }

    /**
     * sendPO: send the purchase order to the supplier.
     * @param purchOrd - the purchase order.
     *
     */
    private void sendPO(POEntLocal purchOrd) throws SPECjAppServerException {

        if( debugging )
            debug.println(3, "sendPO");

        try {
            String         xml = purchOrd.generateXml();
            SendXmlCommand xmlComm;

            if( isSecure ) {
                xmlComm = new SecureXmlCommand(servletHost, xml, servlet,
                                               servletPort);
            } else {
                xmlComm = new NonSecureXmlCommand(servletHost, xml, servlet,
                                                  servletPort);
            }

            xmlComm.execute();
            if( debugging )
                debug.println(3, "PO has Been sent");
        } catch( IOException io ) {
            if( debugging )
                debug.println(
                             1, "IOException. Unable to delivery PurchaseOrder to Supplier");
            debug.printStackTrace(io);

            throw new EJBException(io);
        }
    }

    /**
     * Constructor BuyerMDB
     *
     *
     */
    public BuyerMDB() {
    }

    /**
     * Method setMessageDrivenContext
     *
     *
     * @param messageDrivenContext
     *
     */
    public void setMessageDrivenContext(MessageDrivenContext ejbContext){ //messageDrivenContext) {
        //this.messageDrivenContext = messageDrivenContext;
        this.ejbContext = ejbContext; 
        initEnv();
    }

    public void setSessionContext(SessionContext ejbContext){
    	this.ejbContext = ejbContext; 
        initEnv();
    }
    
    private void initEnv(){
        InitialContext context = null;

        try {
            context = new InitialContext();

            int debugLevel =
            ((Integer) context.lookup("java:comp/env/debuglevel")).intValue();

            if( debugLevel > 0 ) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch( NamingException ne ) {
            debug = new Debug();
        }

        try {
            context       = (context == null)
                            ? new InitialContext()
                            : context;
            componentHome =
            (SComponentEntHomeLocal) context.lookup("java:comp/env/ejb/SComponentLocal");
            poHome        =
            (POEntHomeLocal) context.lookup("java:comp/env/ejb/POEntLocal");
            supplierHome  =
            (SupplierEntHomeLocal) context.lookup("java:comp/env/ejb/SupplierLocal");
            servlet       = (String) context.lookup("java:comp/env/servlet");

            // A new env PREFIX is added which will include a "/" at the
            // start which should be removed. RFE 4491953
            if( servlet.startsWith("/") )
                servlet = servlet.substring(1);

            servletHost   =
            (String) context.lookup("java:comp/env/servletHost");
            servletPort   =
            ((Integer) context.lookup("java:comp/env/servletPort"))
            .intValue();
            isSecure      =
            ((Boolean) context.lookup("java:comp/env/secureHTTP"))
            .booleanValue();
        } catch( NamingException e ) {
            throw new EJBException("Failure looking up home " + e);
        }    
    }
    
    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {

    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {
}

  public void ejbPassivate(){
  }

  public void ejbActivate(){
  }
}
