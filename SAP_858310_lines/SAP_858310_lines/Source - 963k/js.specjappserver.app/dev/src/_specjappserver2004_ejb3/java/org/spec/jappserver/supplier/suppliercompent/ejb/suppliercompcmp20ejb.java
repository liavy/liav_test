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

package org.spec.jappserver.supplier.suppliercompent.ejb;


// Import statements
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;


/**
 * THis is the CMP version of the SupplierComp Entity Bean
 *
 *
 * @author Damian Guy
 */
public abstract class SupplierCompCmp20EJB implements EntityBean {

    protected EntityContext entityContext;
    protected Debug         debug;
    protected boolean       debugging;

    // SupplierCompEnt methods 

    /**
     * Method getPrice
     *
     *
     * @return
     *
     */
    public double getPrice() {
        return getSuppCompPrice();
    }

    /**
     * Method getDiscount
     *
     *
     * @return
     *
     */
    public double getDiscount() {
        return getSuppCompDiscount();
    }

    /**
     * Method getDeliveryDate
     *
     *
     * @return
     *
     */
    public int getDeliveryDate() {
        return getSuppCompDelDate();
    }

    /**
     * Method getQuantity
     *
     *
     * @return
     *
     */
    public int getQuantity() {
        return getSuppCompQty();
    }

    /**
     * Method getDiscountedPrice
     *
     *
     * @return
     *
     */
    public double getDiscountedPrice() {
        return(1 - getSuppCompDiscount()) * getSuppCompPrice();
    }

    // ejbXXXX methods

    /**
     * ejbCreate: Corresponds to create in the Home interface.
     * @param suppCompID - part number.
     * @param suppCompSuppID - supplier id.
     * @param suppCompPrice - price of supplied qty (suppCompQty).
     * @param suppCompQty - quantity that is supplied.
     * @param suppCompDiscount - discount the applies.
     * @param suppCompDelDate - probably should be lead time.
     * @return SuppCompEntPK - primary Key for this object (suppCompID + suppCompSuppID).
     * @exception CreateException - if there is a create failure.
     */
    public SuppCompEntPK ejbCreate(
                                  String suppCompID, int suppCompSuppID, double suppCompPrice, int suppCompQty, double suppCompDiscount, int suppCompDelDate)
    throws CreateException {

        if( debugging )
            debug.println(3, "ejbCreate");

        setSuppCompID(suppCompID);
        setSuppCompSuppID(suppCompSuppID);
        setSuppCompPrice(suppCompPrice);
        setSuppCompQty(suppCompQty);
        setSuppCompDiscount(suppCompDiscount);
        setSuppCompDelDate(suppCompDelDate);

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppCompID
     * @param suppCompSuppID
     * @param suppCompPrice
     * @param suppCompQty
     * @param suppCompDiscount
     * @param suppCompDelDate
     *
     */
    public void ejbPostCreate(String suppCompID, int suppCompSuppID,
                              double suppCompPrice, int suppCompQty,
                              double suppCompDiscount, int suppCompDelDate) {
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
            debug.println(3, "ejbRemove");
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if( debugging )
            debug.println(3, "ejbActivate");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if( debugging )
            debug.println(3, "ejbPassivate");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if( debugging )
            debug.println(3, "ejbLoad");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if( debugging )
            debug.println(3, "ejbStore");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param ec
     *
     */
    public void setEntityContext(EntityContext ec) {

        try {
            Context ic         = new InitialContext();
            int     debugLevel =
            ((Integer) ic.lookup("java:comp/env/debuglevel")).intValue();

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

        this.entityContext = ec;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        entityContext = null;
    }

    /**
     * Method getSuppCompID
     *
     *
     * @return
     *
     */
    public abstract String  getSuppCompID() ;
    /**
     * Method setSuppCompID
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompID(String suppCompID) ;

    /**
     * Method getSuppCompSuppID
     *
     *
     * @return
     *
     */
    public abstract int     getSuppCompSuppID() ;
    /**
     * Method setSuppCompSuppID
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompSuppID(int suppCompSuppID) ;

    /**
     * Method getSuppCompPrice
     *
     *
     * @return
     *
     */
    public abstract double  getSuppCompPrice() ;
    /**
     * Method setSuppCompPrice
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompPrice(double suppCompPrice) ;

    /**
     * Method getSuppCompQty
     *
     *
     * @return
     *
     */
    public abstract int     getSuppCompQty() ;
    /**
     * Method setSuppCompQty
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompQty(int suppCompQty) ;

    /**
     * Method getSuppCompDiscount
     *
     *
     * @return
     *
     */
    public abstract double  getSuppCompDiscount() ;
    /**
     * Method setSuppCompDiscount
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompDiscount(double suppCompDiscount) ;

    /**
     * Method getSuppCompDelDate
     *
     *
     * @return
     *
     */
    public abstract int     getSuppCompDelDate() ;
    /**
     * Method setSuppCompDelDate
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCompDelDate(int suppCompDelDate) ;

}

