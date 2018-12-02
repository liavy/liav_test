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

package org.spec.jappserver.supplier.suppliercompent.ejb;


//Import statements
import javax.ejb.EJBLocalObject;


/**
 * This is the Remote Interface for the Supplier Componect entity bean.
 * This Bean enables lookup of parts that particular suppliers supply.
 *
 *
 * @author Damian Guy
 */
public interface SupplierCompEntLocal extends EJBLocalObject {

    /**
     * getPrice
     * @return double - price of component.
     */
    public double getPrice();

    /** getDiscount
     * @return double - discount percent.
     */
    public double getDiscount();

    /**
     * getDeliveryDate
     * @return int
     */
    public int getDeliveryDate();

    /**
     * getQuantity
     * @return int
     */
    public int getQuantity();

    /**
     * getDiscountedPrice
     * return double - cost of parts with discount applied.
     */
    public double getDiscountedPrice();
}

