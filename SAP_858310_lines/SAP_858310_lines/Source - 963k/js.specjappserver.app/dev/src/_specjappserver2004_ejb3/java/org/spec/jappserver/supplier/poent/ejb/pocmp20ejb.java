/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN           Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2004/02/21  Samuel Kounev, Darmstadt  Removed throws EJBException clauses
 */

package org.spec.jappserver.supplier.poent.ejb;


//Import statements
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.DataIntegrityException;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.supplier.helper.ComponentOrder;
import org.spec.jappserver.supplier.helper.XmlTags;
import org.spec.jappserver.supplier.polineent.ejb.POLineEntHomeLocal;
import org.spec.jappserver.supplier.polineent.ejb.POLineEntLocal;
import org.spec.jappserver.supplier.polineent.ejb.POLineEntPK;
import org.spec.jappserver.util.sequenceses.ejb.SequenceSes;
import org.spec.jappserver.util.sequenceses.ejb.SequenceSesHome;


/**
 * This class implements the Purchase Order Entity Bean
 * Container Managed.
 *
 * @author Damian Guy
 */
public abstract class POCmp20EJB implements EntityBean {

    protected EntityContext   entityContext;
    protected Debug           debug;
    protected boolean         debugging;
    private String            poDTD;
    protected SequenceSesHome sequenceHome;
    protected POLineEntHomeLocal   poLineHome;
    protected HashMap         poLineCache;

    /**
     * poLineDeliverd - indicate that a POline has been delivered.
     * @param lineNumber - line number of delivered line.
     */
    public void poLineDelivered(int lineNumber) {
        try {
            POLineEntLocal pol =
            poLineHome.findByPrimaryKey(new POLineEntPK(lineNumber,
                                                        getPoNumber()));

            Calendar  cal = Calendar.getInstance();

            pol.setDeliveredDate(Util.getDateRoundToDay(cal.getTime().getTime()));
        } catch( FinderException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch( EJBException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * generateXml - generates the  XML for this Purchase Order.
     * @return String - containing XML.
     */
    public String generateXml() throws SPECjAppServerException {

        StringBuffer xml = new StringBuffer(XmlTags.XMLVERSION);

        xml.append(XmlTags.PODOC);
        xml.append("\"");
        xml.append(poDTD);
        xml.append("\">");
        xml.append(XmlTags.POSTART);
        xml.append(XmlTags.PONUMBERSTART);
        xml.append(getPoNumber());
        xml.append(XmlTags.PONUMBEREND);
        xml.append(XmlTags.SITESTART);
        xml.append(getPoSiteID());
        xml.append(XmlTags.SITEEND);

        try {
            Collection lines = this.getPoLines();

            if( poLineCache == null )
                poLineCache = new HashMap();

            for( Iterator lineIter = lines.iterator(); lineIter.hasNext(); ) {
                POLineEntLocal line = (POLineEntLocal) lineIter.next();
                poLineCache.put(line.getPrimaryKey(), line);
            }

            if( poLineCache.size() <= 0 )
                throw new DataIntegrityException("No PO lines found!");

            xml.append(XmlTags.NUMLINESSTART);
            xml.append(lines.size());
            xml.append(XmlTags.NUMLINESEND);

            Iterator lineIter = poLineCache.values().iterator();

            while( lineIter.hasNext() ) {
                POLineEntLocal line = (POLineEntLocal) lineIter.next();

                xml.append(line.createXml());
            }

            xml.append(XmlTags.POEND);

            return xml.toString();
        } catch( EJBException re ) {
            throw new EJBException(re);
        }
    }

    /**
     * ejbCreate: creates new Purchase Order + PO Lines.
     * @param suppID  -  the id of the supplier.
     * @param siteID  -  id of site that has ordered components.
     * @param orders - Array of Objects containing qty + pricing information for components
     *                     that are being ordered.
     * @return Integer - id of this PO.
     * @exception CreateException - if there is a create failure.
     */
    public Integer ejbCreate(
                            int suppID, int siteID, ComponentOrder[] orders)
    throws CreateException {

        if( debugging )
            debug.println(3, "ejbCreate with " + orders.length + "lines.");

        try {
            SequenceSes sequence = sequenceHome.create();

            setPoNumber(new Integer(sequence.nextKey("purchaseorder")));
        } catch( CreateException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch( FinderException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch( RemoteException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        setPoSuppID(suppID);
        setPoSiteID(siteID);


        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppID
     * @param siteID
     * @param orders
     *
     */
    public void ejbPostCreate(int suppID, int siteID,
                              ComponentOrder[] orders)
    throws CreateException {

        poLineCache = new HashMap(orders.length);

        try {
            Collection lines = this.getPoLines();
            for( int i = 0; i < orders.length; i++ ) {
                String id       = orders[i].id;
                int    qty      = orders[i].qty;
                double balance  = orders[i].balance;
                int    leadTime = orders[i].leadTime;

                POLineEntLocal poLine = poLineHome.create(i + 1, getPoNumber(),
                                                          id, qty, balance, leadTime,
                                                          "testing");

                lines.add(poLine);
                poLineCache.put(poLine.getPrimaryKey(), poLine);
            }
        } catch( CreateException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch( EJBException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        Context context = null;

        try {
            context = new InitialContext();
        } catch( Exception e ) {
            throw new EJBException(e);
        }

        try {
            int debugLevel =
            ((Integer) context.lookup("java:comp/env/debuglevel"))
            .intValue();

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

        this.entityContext = entityContext;

        try {
            poDTD = (String) context.lookup("java:comp/env/poDTD");

            if( debugging )
                debug.println(3, "found poDTD reference");

            sequenceHome =
            (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                                                                   context.lookup("java:comp/env/ejb/SequenceSes"),
                                                                   SequenceSesHome.class);

            if( debugging )
                debug.println(3, "found SequenceSesHome interface");

            poLineHome = (POLineEntHomeLocal) context.lookup("java:comp/env/ejb/POLineEntLocal");

            if( debugging )
                debug.println(3, "found POLineEntHomeLocal interface");
        } catch( NamingException ex ) {
            throw new EJBException(ex);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        if( debugging )
            debug.println(3, "ejbRemove ");
        poLineCache = null;
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if( debugging )
            debug.println(3, "ejbActivate ");
        poLineCache = null;
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if( debugging )
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if( debugging )
            debug.println(3, "ejbLoad ");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if( debugging )
            debug.println(3, "ejbStore ");
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {

        if( debugging )
            debug.println(3, "unsetEntityContext ");

        entityContext = null;
    }

    /**
     * Method getPoNumber
     *
     *
     * @return
     *
     */
    abstract public Integer getPoNumber();
    /**
     * Method setPoNumber
     *
     *
     * @param
     *
     */
    abstract public void setPoNumber(Integer val);

    /**
     * Method getPoSuppID
     *
     *
     * @return
     *
     */
    abstract public int getPoSuppID();
    /**
     * Method setPoSuppID
     *
     *
     * @param
     *
     */
    abstract public void setPoSuppID(int val);

    /**
     * Method getPoSiteID
     *
     *
     * @return
     *
     */
    abstract public int getPoSiteID();
    /**
     * Method setPoSiteID
     *
     *
     * @param
     *
     */
    abstract public void setPoSiteID(int val);


    // Access methods for relationship fields

    /**
     * Method getPolines
     *
     *
     * @return
     *
     */
    public abstract Collection getPoLines();
    /**
     * Method setPoLines
     *
     *
     * @param
     *
     */
    public abstract void setPoLines(Collection poLines);



}

