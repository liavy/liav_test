/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
* This source code is provided as is, without any express or implied warranty.
*
*  History:
*  Date        ID, Company             Description
*  ----------  ----------------        ----------------------------------------------
*  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
*  2003/02/13  Samuel Kounev,          added totalValue field 
                Darmstadt
*  2003/03/26  John Stecher, IBM       Implemented Big Decimal Changes
*  2003/04/20  John Stecher, IBM       moved class to helper package
*  2003/06/22  John Stecher, IBM       added new constructor to facilitate new Order creation
*  2003/08/27  John Stecher, IBM       Added new field and methods for MSRP
*/

package org.spec.jappserver.orders.helper;

import java.math.BigDecimal;

public class OrderLineDataBean implements java.io.Serializable {

    private java.sql.Date   shipDate;
    private int             quantity;
    private BigDecimal      totalValue;
    private BigDecimal		msrpAtPurchase;  
    private String          itemID;
    private Integer         orderID;
    private Integer         ID;
    private int			 	o_line_num;
    
    public OrderLineDataBean(){	
    }
    
    public OrderLineDataBean(int quant, BigDecimal value, String item_id, int ol_num, BigDecimal msrp){	
		quantity = quant;
		totalValue = value;
		itemID = item_id;
		o_line_num = ol_num;
		msrpAtPurchase = msrp;
    }
    
    /**
     * Returns the iD.
     * @return Integer
     */
    public Integer getID() {
        return ID;
    }

    /**
     * Returns the itemID.
     * @return String
     */
    public String getItemID() {
        return itemID;
    }

    /**
     * Returns the orderID.
     * @return Integer
     */
    public Integer getOrderID() {
        return orderID;
    }

    /**
     * Returns the quantity.
     * @return int
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the totalValue.
     * @return double
     */
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    /**
     * Returns the shipDate.
     * @return java.sql.Date
     */
    public java.sql.Date getShipDate() {
        return shipDate;
    }

    /**
     * Sets the iD.
     * @param iD The iD to set
     */
    public void setID(Integer iD) {
        ID = iD;
    }

    /**
     * Sets the itemID.
     * @param itemID The itemID to set
     */
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    /**
     * Sets the orderID.
     * @param orderID The orderID to set
     */
    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    /**
     * Sets the quantity.
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the order line total value.
     * @param totalValue - the total value to set
     */
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }


    /**
     * Sets the shipDate.
     * @param shipDate The shipDate to set
     */
    public void setShipDate(java.sql.Date shipDate) {
        this.shipDate = shipDate;
    }

	/**
	 * Returns the o_line_num.
	 * @return int
	 */
	public int getO_line_num() {
		return o_line_num;
	}

	/**
	 * @return BigDecimal
	 */
	public BigDecimal getMsrpAtPurchase() {
		return msrpAtPurchase;
	}

	/**
	 * @param decimal
	 */
	public void setMsrpAtPurchase(BigDecimal decimal) {
		msrpAtPurchase = decimal;
	}

}
