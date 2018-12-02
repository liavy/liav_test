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

package org.spec.jappserver.supplier.supplierent.ejb;


//Import statements
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

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.supplier.helper.PartSpec;
import org.spec.jappserver.supplier.suppliercompent.ejb.SuppCompEntPK;
import org.spec.jappserver.supplier.suppliercompent.ejb.SupplierCompEntHomeLocal;
import org.spec.jappserver.supplier.suppliercompent.ejb.SupplierCompEntLocal;


/**
 * CMP version of the Supplier Entity Bean
 *
 *
 * @author Damian Guy
 */
public abstract class SupplierCmp20EJB implements EntityBean {

    protected EntityContext       entityContext;
    protected Debug               debug;
    protected boolean             debugging;
    protected SupplierCompEntHomeLocal suppCompEntHome;

    /**
     * getID - get the suppliers ID
     * @return int - id of supplier.
     */
    public int getID() {
        return getSuppID().intValue();
    }

    /**
     * getPartSpec - return information about a part that
     * supplier supplies.
     * @param pID - id of part to get Spec for
     * @return PartSpec
     * @exception EJBException
     * @exception FinderException
     */
    public PartSpec getPartSpec(String pID)
    throws FinderException {

        try {
            SupplierCompEntLocal suppComp =
            suppCompEntHome.findByPrimaryKey(new SuppCompEntPK(pID,
                                                               getSuppID().intValue()));

            double          price    = suppComp.getPrice();
            double          disc     = suppComp.getDiscount();
            int             delDate = suppComp.getDeliveryDate();
            int             qty      = suppComp.getQuantity();

            /** return new PartSpec(pID, price, qty, disc, delDate); **/
            return new PartSpec(getSuppID(), pID, price, qty, disc, delDate);
        } catch( EJBException re ) {
            throw new EJBException(re);
        }
    }

    /**
     * ejbCreate: create a new supplier.
     * @param suppID - id of supplier.
     * @param suppName - supplier name.
     * @param suppStreet1 - street line 1.
     * @param suppStreet2 - street line 2.
     * @param suppCity - city supplier is located.
     * @param suppState
     * @param suppCountry - country supplier is located.
     * @param suppZip - zip/postal code.
     * @param suppPhone - contact phone number.
     * @param suppContact - contact person.
     * @return SupplierEnt - newly created Supplier
     * @exception CreateException - if the create fails.
     */
    public Integer ejbCreate(int suppID, String suppName,
                             String suppStreet1, String suppStreet2, String suppCity, String suppState, String suppCountry, String suppZip, String suppPhone, String suppContact)
    throws CreateException {

        setSuppID(new Integer(suppID));
        setSuppName(suppName);
        setSuppStreet1(suppStreet1);
        setSuppStreet2(suppStreet2);
        setSuppState(suppState);
        setSuppCity(suppCity);
        setSuppCountry(suppCountry);
        setSuppZip(suppZip);
        setSuppPhone(suppPhone);
        setSuppContact(suppContact);

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppID
     * @param suppName
     * @param suppStreet1
     * @param suppStreet2
     * @param suppCity
     * @param suppState
     * @param suppCountry
     * @param suppZip
     * @param suppPhone
     * @param suppContact
     *
     */
    public void ejbPostCreate(int suppID, String suppName,
                              String suppStreet1, String suppStreet2,
                              String suppCity, String suppState,
                              String suppCountry, String suppZip,
                              String suppPhone, String suppContact) {
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
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        try {
            Iterator _enum =
            suppCompEntHome.findAllBySupplier(getSuppID().intValue()).iterator();

            while( _enum.hasNext() ) {
                SupplierCompEntLocal suppComp = (SupplierCompEntLocal) _enum.next();

                suppComp.remove();
            }


        } catch( FinderException fe ) {
            throw new EJBException(fe);
        } catch( EJBException re ) {
            throw new EJBException(re);
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
            context         = new InitialContext();
            suppCompEntHome =
            (SupplierCompEntHomeLocal) context.lookup("java:comp/env/ejb/SupplierCompEntLocal");
        } catch( NamingException ex ) {
            throw new EJBException(ex);
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
    }

    /**
     * Method getSuppID
     *
     *
     * @return
     *
     */
    public abstract Integer  getSuppID() ;
    /**
     * Method setSuppID
     *
     *
     * @param
     *
     */
    public abstract void  setSuppID(Integer suppID) ;

    /**
     * Method getSuppName
     *
     *
     * @return
     *
     */
    public abstract String  getSuppName() ;
    /**
     * Method setSuppName
     *
     *
     * @param
     *
     */
    public abstract void  setSuppName(String suppName) ;

    /**
     * Method getSuppStreet1
     *
     *
     * @return
     *
     */
    public abstract String  getSuppStreet1() ;
    /**
     * Method setSuppStreet1
     *
     *
     * @param
     *
     */
    public abstract void  setSuppStreet1(String suppStreet1) ;

    /**
     * Method getSuppStreet2
     *
     *
     * @return
     *
     */
    public abstract String  getSuppStreet2() ;
    /**
     * Method setSuppStreet2
     *
     *
     * @param
     *
     */
    public abstract void  setSuppStreet2(String suppStreet2) ;

    /**
     * Method getSuppCity
     *
     *
     * @return
     *
     */
    public abstract String  getSuppCity() ;
    /**
     * Method setSuppCity
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCity(String suppCity) ;

    /**
     * Method getSuppState
     *
     *
     * @return
     *
     */
    public abstract String  getSuppState() ;
    /**
     * Method setSuppState
     *
     *
     * @param
     *
     */
    public abstract void  setSuppState(String suppState) ;

    /**
     * Method getSuppCountry
     *
     *
     * @return
     *
     */
    public abstract String  getSuppCountry() ;
    /**
     * Method setSuppCountry
     *
     *
     * @param
     *
     */
    public abstract void  setSuppCountry(String suppCountry) ;

    /**
     * Method getSuppZip
     *
     *
     * @return
     *
     */
    public abstract String  getSuppZip() ;
    /**
     * Method setSuppZip
     *
     *
     * @param
     *
     */
    public abstract void  setSuppZip(String suppZip) ;

    /**
     * Method getSuppPhone
     *
     *
     * @return
     *
     */
    public abstract String  getSuppPhone() ;
    /**
     * Method setSuppPhone
     *
     *
     * @param
     *
     */
    public abstract void  setSuppPhone(String suppPhone) ;

    /**
     * Method getSuppContact
     *
     *
     * @return
     *
     */
    public abstract String  getSuppContact() ;
    /**
     * Method setSuppContact
     *
     *
     * @param
     *
     */
    public abstract void  setSuppContact(String suppContact) ;

}

