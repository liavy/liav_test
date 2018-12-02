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

package org.spec.jappserver.supplier.scomponentent.ejb;


//Import statements
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
 * This class implements the Component Entity bean
 * container managed.
 *
 * @author Damian Guy
 */
public abstract class SComponentCmp20EJB implements EntityBean {

    protected EntityContext entityContext;
    protected Debug         debug;
    protected boolean       debugging;

    /**
     * getID - get the id of this component.
     * @return String
     */
    public String getID() {
        return getCompID();
    }

    /**
     * checkForPO: check if there is an outstanding PO
     * for this component, and if the qtyOnOrder will
     * satisfy the qtyDemanded + the current qty required.
     *
     * @param qtyRequired
     * @return boolean - true if above condition satisified.
     */
    public boolean checkForPO(int qtyRequired) {

        if( (getQtyDemanded() + qtyRequired) <= getQtyOnOrder() ) {
            return true;
        }

        return false;
    }

    /**
     * updateDemand: update the qtyDemanded for a component.
     * @param qtyRequired - quantity to add to existing qtyDemanded.
     */
    public void updateDemand(int qtyRequired) {
        setQtyDemanded(getQtyDemanded() + qtyRequired);
    }

    /**
     * updateQuantities: update the qtyOnOrder and qtyDemanded fields.
     *
     * @param qtyOrdered
     * @param qtyDemanded - qty to add to qtyDemanded.
     */
    public void updateQuantities(int qtyOrdered, int qtyDemanded) {
        setQtyDemanded(getQtyDemanded() + qtyDemanded);
        setQtyOnOrder(getQtyOnOrder() + qtyOrdered);

    }

    /**
     * deliveredQuantity: used to update the qtyOnOrder and
     * qtyDemanded fields when an order has been delivered.
     */
    public void deliveredQuantity(int quantityDelivered) {

        int tmpDemanded = getQtyDemanded() - quantityDelivered;

        setQtyDemanded((tmpDemanded < 0)
                       ? 0
                       : tmpDemanded);
        setQtyOnOrder(getQtyOnOrder() - quantityDelivered);


    }

    /**
     * ejbCreate: Create new Component.
     *
     * @param compID
     * @param compName
     * @param compDesc
     * @param compUnit
     * @param compCost
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     * @return ComponentEnt
     * @exception CreateException - if the create fails.
     */
    public String ejbCreate(
                           String compID, String compName, String compDesc, String compUnit, double compCost, int qtyOnOrder, int qtyDemanded, int leadTime, int containerSize)
    throws CreateException {

        if( debugging )
            debug.println(3, "ejbCreate");

        setCompID(compID);
        setCompName(compName);
        setCompDesc(compDesc);
        setCompUnit(compUnit);
        setCompCost(compCost);
        setQtyOnOrder(qtyOnOrder);
        setQtyDemanded(qtyDemanded);
        setLeadTime(leadTime);
        setContainerSize(containerSize);

        return null;


    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param compID
     * @param compName
     * @param compDesc
     * @param compUnit
     * @param compCost
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     *
     */
    public void ejbPostCreate(String compID, String compName,
                              String compDesc, String compUnit,
                              double compCost, int qtyOnOrder,
                              int qtyDemanded, int leadTime,
                              int containerSize) {
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if( debugging )
            debug.println(3, "ejbActivate ");
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
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        try {
            Context context    = new InitialContext();
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
     * getCompID: get method for compID instance variable.
     * @return compID
     */
    public abstract String getCompID() ;

    /**
     * setCompID: get method for compID instance variable.
     * @param compID
     */
    public abstract void setCompID(String val) ;

    /**
     * getCompName: get method for compName instance variable.
     * @return compName
     */
    public abstract String getCompName() ;

    /**
     * setCompName: get method for compName instance variable.
     * @param compName
     */
    public abstract void setCompName(String val) ;

    /**
     * getCompDesc: get method for compDesc instance variable.
     * @return compDesc
     */
    public abstract String getCompDesc() ;

    /**
     * setCompDesc: get method for compDesc instance variable.
     * @param compDesc
     */
    public abstract void setCompDesc(String val) ;

    /**
     * getCompUnit: get method for compUnit instance variable.
     * @return compUnit
     */
    public abstract String getCompUnit() ;

    /**
     * setCompUnit: get method for compUnit instance variable.
     * @param compUnit
     */
    public abstract void setCompUnit(String val) ;

    /**
     * getCompCost: get method for compCost instance variable.
     * @return compCost
     */
    public abstract double getCompCost() ;

    /**
     * setCompCost: get method for compCost instance variable.
     * @param compCost
     */
    public abstract void setCompCost(double val) ;

    /**
     * getQtyOnOrder: get method for qtyOnOrder instance variable.
     * @return int - the quantity on order for this component.
     */
    public abstract int getQtyOnOrder() ;

    /**
     * setQtyOnOrder: get method for qtyOnOrder instance variable.
     * @param qtyOnOrder
     */
    public abstract void setQtyOnOrder(int val) ;

    /**
     * getQtyDemanded: get methof for qtyDemanded instance variable.
     * @return int - the quantity currently demanded for this component.
     */
    public abstract int getQtyDemanded() ;

    /**
     * setQtyDemanded: get methof for qtyDemanded instance variable.
     * @param qtyDemanded
     */
    public abstract void setQtyDemanded(int val) ;

    /**
     * getLeadTime: get the maximum allowable lead time
     * for this component.
     * @return int -the maximum lead time.
     */
    public abstract int getLeadTime() ;

    /**
     * setLeadTime: get the maximum allowable lead time
     * for this component.
     * @param
     */
    public abstract void setLeadTime(int val) ;

    /**
     * getContainerSize: get method for containerSize instance variable.
     * @return int - the size of the container (How many parts to order)
     */
    public abstract int getContainerSize() ;

    /**
     * setContainerSize: get method for containerSize instance variable.
     * @param
     */
    public abstract void setContainerSize(int val) ;

}

