/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
* This source code is provided as is, without any express or implied warranty.
*
*  History:
*  Date        ID, Company             Description
*  ----------  ----------------        ----------------------------------------------
*  2002/03/22  ramesh, SUN Microsystem Created
*  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
*  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
*                                      SPECjAppServer2002 (EJB2.0).
*  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
*/

package org.spec.jappserver.orders.helper;


import java.math.BigDecimal;


/**
 * An object of this class is used to represent item, qty pairs
 * in line items of an order.
 */
public class ItemQuantity implements java.io.Serializable {

    /**
     * Constructor ItemQuantity
     *
     *
     * @param itemId
     * @param itemQuantity
     *
     */
    public ItemQuantity(String itemId, int itemQuantity, double totalValue) {
        this.itemId       = itemId;
        this.itemQuantity = itemQuantity;
        this.itemTotal    = new BigDecimal(totalValue);
    }

    public ItemQuantity(String itemId, int itemQuantity, BigDecimal itemTotal) {
        this.itemId       = itemId;
        this.itemQuantity = itemQuantity;
        this.itemTotal = itemTotal;
    }

    public String itemId;
    public int    itemQuantity;
    public BigDecimal itemTotal;
}

