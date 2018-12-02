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
 *  2003/05/23  John Stecher, IBM       Removed EJBExceptions
 */

package org.spec.jappserver.supplier.scomponentent.ejb;


import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;


/**
 * This is the public interface of the Component Entity bean.
 * It provides methods to update inventory and check for outstanding
 * purchase orders.
 * @author Damian Guy
 */
public interface SComponentEntLocal extends EJBLocalObject {

    /**
     * getID - get the id of this component.
     * @return String
     * @exception EJBException - if there is a system failure.
     */
    public String getID();

    /**
     * getQtyOnOrder: get method for qtyOnOrder instance variable.
     * @return int - the quantity on order for this component.
     * @exception EJBException - if there is a system failure.
     */
    public int getQtyOnOrder();

    /**
     * getQtyDemanded: get methof for qtyDemanded instance variable.
     * @return int - the quantity currently demanded for this component.
     * @exception EJBException - if there is a system failure.
     */
    public int getQtyDemanded();

    /**
     * getContainerSize: get method for containerSize instance variable.
     * @return int - the size of the container (How many parts to order).
     * @exception EJBException - if there is a system failure.
     */
    public int getContainerSize();

    /**
     * getLeadTime: get the maximum allowable lead time
     * for this component.
     * @return int -the maximum lead time.
     *
     * @throws 
     */
    public int getLeadTime();

    /**
     * checkForPO: check if there is an outstanding PO
     * for this component, and if the qtyOnOrder will
     * satisfy the qtyDemanded + the current qty required.
     *
     * @param qtyRequired
     * @return boolean - true if above condition satisified.
     * @exception EJBException - if there is a system failure.
     */
    public boolean checkForPO(int qtyRequired);

    /**
     * updateDemand: update the qtyDemanded for a component.
     * @param qtyRequired - quantity to add to existing qtyDemanded.
     * @exception EJBException - if there is a system failure.
     */
    public void updateDemand(int qtyRequired);

    /**
     * updateQuantities: update the qtyOnOrder and qtyDemanded fields.
     *
     * @param qtyOrdered
     * @param qtyDemanded - qty to add to qtyDemanded.
     * @exception EJBException - if there is a system failure.
     */
    public void updateQuantities(int qtyOrdered, int qtyDemanded)
   ;

    /**
     * deliveredQuantity: used to update the qtyOnOrder and
     * qtyDemanded fields when an order has been delivered.
     * @param quantity - quantity that was delivered.
     * @exception EJBException - if there is a system failure.
     */
    public void deliveredQuantity(int quantity);
}

