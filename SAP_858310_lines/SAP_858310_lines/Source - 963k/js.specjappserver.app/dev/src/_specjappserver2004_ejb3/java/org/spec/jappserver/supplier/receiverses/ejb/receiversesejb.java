/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/01/26  Samuel Kounev,          Made communication with the Mfg domain
 *              Darmstadt Univ.         asynchronous, i.e. call to ReceiveSes bean
 *                                      replaced with sending a message to ReceiveQueue. 
 *  2003/04/04  Samuel Kounev,          Changed deliverPO method to receive Vector of
 *              Darmstadt Univ.         DeliveryInfo objects. POs are now delivered
 *                                      using a single call to ReceiverSes and a single
 *                                      message to the Mfg domain to update inventory.
 *  2003/04/20  John Stecher	        Changed create in the QueueSession to pass in true. 
 *  2004/02/12  Samuel Kounev,          Rolled back penultimate change (dated 2003/04/04),  
 *              Darmstadt Univ.         since it might cause deadlocks when updating   
 *                                      InventoryEnt and SComponentEnt (osgjava-6527).
 */  

package org.spec.jappserver.supplier.receiverses.ejb;

//Import statements
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.DeliveryInfo;
import org.spec.jappserver.common.NotReadyException;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.supplier.poent.ejb.POEntHomeLocal;
import org.spec.jappserver.supplier.poent.ejb.POEntLocal;
import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntHomeLocal;
import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntLocal;

import java.util.concurrent.LinkedBlockingQueue;
import javax.jms.MessageListener;
import org.spec.jappserver.async.AsyncExecutor;
import org.spec.jappserver.mfg.ReceiveMDB;
import org.spec.jappserver.async.ObjectMessageImpl;
import org.spec.jappserver.async.TextMessageImpl;

/**
 * Implementation of the ReceiverSes Stateless Session Bean.
 *
 * @author Damian Guy
 * 
 */
public class ReceiverSesEJB implements SessionBean {

    private SessionContext    sessionContext;
    private POEntHomeLocal         poHome;
    private SComponentEntHomeLocal componentHome;
    private Debug             debug;
    protected boolean         debugging;

    private QueueConnectionFactory queueConnFactory = null;
    private Queue queue = null;
    
    AsyncExecutor receiveExec ; 
    //ReceiveMDB  receiveMDB; 
    //static LinkedBlockingQueue<MessageListener>  receiveMDB_Q = new LinkedBlockingQueue<MessageListener>(10000); 
    
    @EJB(
         name="org.spec.jappserver.mfg.ReceiveMDB",
         beanInterface=MessageListener.class,
         beanName="ReceiveMDB_Ses"
         )
   protected MessageListener receiveMsgL; 
    
    
    /**
     * deliverPO - deliver a Purchase Order Line.
     */
    public void deliverPO(DeliveryInfo delInfo) throws SPECjAppServerException {

        QueueConnection queueConn = null;
        QueueSession queueSess = null;

        if( debugging )
            debug.println(3, "deliverPO");

        try {        	
            if(Config.bypassJMS){
         	if(receiveExec==null){         	  
            	  receiveExec = AsyncExecutor.createAsyncExecutor(receiveMsgL);             	  
                }
                
                ObjectMessage msg = new ObjectMessageImpl(); 
            	msg.setLongProperty("publishTime", System.currentTimeMillis());
            	msg.setObject(delInfo);
            	
            	/*
            	ReceiveMDB receiveMDB = (ReceiveMDB)receiveMDB_Q.poll();
            	if(receiveMDB==null){
            		receiveMDB = new ReceiveMDB();
            	}
            	*/
            	
            	receiveExec.deliverMessage(msg); 
            	
            }else{	
                 // Send message to the Mfg domain to update inventory:
                 queueConn = queueConnFactory.createQueueConnection();
                 queueSess = queueConn.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
                 QueueSender queueSender = queueSess.createSender(queue);
                 ObjectMessage message = queueSess.createObjectMessage();
                 message.setLongProperty("publishTime", System.currentTimeMillis());
                 message.setObject(delInfo);
//                 long time = System.currentTimeMillis();
                 queueSender.send(message);
//                 (new Sender()).send("ResponseTime.ReceiverQueue", System.currentTimeMillis() - time);
            }
                                                                                      
            // Register POLine delivery:
            SComponentEntLocal comp = componentHome.findByPrimaryKey(delInfo.partID);
            comp.deliveredQuantity(delInfo.qty);

            // POs can get committed after the delivery attempts.
            // We allow delivery retries in such cases.
            // We use a separate try clause just to ensure this is only
            // allowed for POs and not for other finders.
            try {            
                POEntLocal po = poHome.findByPrimaryKey(new Integer(delInfo.poId));
                po.poLineDelivered(delInfo.lineNumber);
            } catch (ObjectNotFoundException e) {
                sessionContext.setRollbackOnly();
                throw new NotReadyException("PO " + delInfo.poId + " not found");
            }
            // Note that PO delivery is tracked on a per POLine basis since
            // in real life suppliers might fail to deliver all orderlines
            // at the same time.            
        } catch( javax.jms.JMSException e ) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch( FinderException e ) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            try {
                if( queueConn != null ) queueConn.close();
                if( queueSess != null ) queueSess.close();
            } catch( javax.jms.JMSException e ) {
                debug.printStackTrace(e);
            }
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

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
    }

    /**
     * Method setSessionContext
     *
     *
     * @param sessionContext
     *
     */
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
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
            context       = new InitialContext();
            componentHome =
            (SComponentEntHomeLocal)
            context.lookup("java:comp/env/ejb/SComponentLocal");
            poHome = (POEntHomeLocal)
                     context.lookup("java:comp/env/ejb/POEntLocal");
            queueConnFactory = (QueueConnectionFactory)
                               context.lookup("java:comp/env/jms/QueueConnectionFactory");
            queue = (Queue) context.lookup("java:comp/env/jms/ReceiveQueue");
        } catch( NamingException ne ) {
            throw new EJBException("Naming error: " + ne.getMessage());
        }
    }
}
