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
 */

package org.spec.jappserver.supplier.polineent.ejb;


//Import statements
import java.util.Calendar;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.supplier.helper.XmlTags;
import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntHomeLocal;


/**
 * This class implements the Purchase Order Line entity bean.
 *
 * @author Damian Guy
 */
public abstract class POLineCmp20EJB implements EntityBean {

    private int             leadTime;
    protected Debug         debug;
    protected boolean       debugging;
    protected EntityContext entityContext;
    protected SComponentEntHomeLocal componentHome;

    /**
     * createXml: creates the XML for a Line item.
     * @return String - the generated XML.
     */
    public String createXml() {

        StringBuffer xml = new StringBuffer(XmlTags.POLINESTART);

        xml.append(XmlTags.LINENUMSTART);
        xml.append(getPoLineNumber());
        xml.append(XmlTags.LINENUMEND);
        xml.append(XmlTags.PARTIDSTART);
        xml.append(getPoLineID());
        xml.append(XmlTags.PARTIDEND);
        xml.append(XmlTags.QTYSTART);
        xml.append(getPoLineQty());
        xml.append(XmlTags.QTYEND);
        xml.append(XmlTags.BALANCESTART);
        xml.append(getPoLineBalance());
        xml.append(XmlTags.BALANCEEND);
        xml.append(XmlTags.LEADTIMESTART);
        xml.append(leadTime);
        xml.append(XmlTags.LEADTIMEEND);
        xml.append(XmlTags.POLINEEND);

        return xml.toString();
    }

    /**
     * setDeliveredDate: Sets the date that a Line item was delivered.
     * @param date  - the date that the delivery was made.
     */
    public void setDeliveredDate(java.sql.Date date) {
        setPoLineDelDate(date);
    }

    /**** ejbXxxxx methods ****/

    /**
     * ejbCreate - create a POLine
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     * @return POLineEntPK
     * @exception CreateException - if there is a create failure.
     */
    public POLineEntPK ejbCreate(
                                int poLineNumber, Integer poID, String pID, int qty, double balance, int leadTime, String message)
    throws CreateException {

        setPoLineNumber(poLineNumber);
        setPoLinePoID(poID);
        setPoLineID(pID);
        setPoLineQty(qty);
        setPoLineBalance(balance);


        Calendar cal        = Calendar.getInstance();
        int      hoursToADD = leadTime / 24;

        this.leadTime = leadTime;

        cal.add(Calendar.HOUR, hoursToADD);

        setPoLineDelDate(Util.getDateRoundToDay(cal.getTime().getTime()));
        setPoLineMsg(message);


        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     *
     */
    public void ejbPostCreate(int poLineNumber, Integer poID, String pID,
                              int qty, double balance, int leadTime,
                              String message) {
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        leadTime = Integer.MIN_VALUE;
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        // Recheck the leadTime only if the bean instance is just activated.
        if( leadTime == Integer.MIN_VALUE ) {
            try {
                leadTime = componentHome.findByPrimaryKey(getPoLineID())
                           .getLeadTime();

            } catch( ObjectNotFoundException e ) {
                throw new EJBException("Referenced item " + getPoLineID() +
                                       " not found");
            } catch( FinderException e ) {
                throw new EJBException(e);
            } catch( EJBException e ) {
                throw new EJBException(e);
            }
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        Context context = null;                        try {
            context    = new InitialContext();
            int     debugLevel =
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

        try {
            context       = (context == null)
                            ? new InitialContext()
                            : context;
            componentHome = (SComponentEntHomeLocal)
                            context.lookup("java:comp/env/ejb/SComponentLocal");
        } catch( NamingException e ) {
            throw new EJBException("Failure looking up home " + e);
        }
        this.entityContext = entityContext;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
    }


    /**
     * Method getPoLineNumber
     *
     *
     * @return
     *
     */
    public abstract int  getPoLineNumber() ;

    /**
     * Method setPoLineNumber
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineNumber(int poLineNumber) ;

    /**
     * Method getPoLinePoID
     *
     *
     * @return
     *
     */
    public abstract Integer  getPoLinePoID() ;
    /**
     * Method setPoLinePoID
     *
     *
     * @param
     *
     */
    public abstract void  setPoLinePoID(Integer poLinePOId) ;

    /**
     * Method getPoLineID
     *
     *
     * @return
     *
     */
    public abstract String  getPoLineID() ;
    /**
     * Method setPoLineID
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineID(String poLineId) ;

    /**
     * Method getPoLineQty
     *
     *
     * @return
     *
     */
    public abstract int  getPoLineQty() ;
    /**
     * Method setPoLineQty
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineQty(int poLineQty) ;

    /**
     * Method getPoLineBalance
     *
     *
     * @return
     *
     */
    public abstract double  getPoLineBalance() ;
    /**
     * Method setPoLineBalance
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineBalance(double poLineBalance) ;

    /**
     * Method getPoLineDelDate
     *
     *
     * @return
     *
     */
    public abstract java.sql.Date  getPoLineDelDate() ;
    /**
     * Method setPoLineDelDate
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineDelDate(java.sql.Date poLineDelDate) ;

    /**
     * Method getPoLineMsg
     *
     *
     * @return
     *
     */
    public abstract String  getPoLineMsg() ;
    /**
     * Method setPoLineMsg
     *
     *
     * @param
     *
     */
    public abstract void  setPoLineMsg(String poLineMsg) ;
}

