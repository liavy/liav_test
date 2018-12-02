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
*  2003/03/26  John Stecher, IBM       Implemented Big Decimal Changes
*  2003/04/20  John Stecher, IBM       moved class to helper package
*  2003/06/22  John Stecher, IBM       added new constructors
*  2003/06/28  John Stecher, IBM       Removed unnecessary imports
*/

package org.spec.jappserver.orders.helper;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.spec.jappserver.corp.helper.CustomerInventoryDataBean;
import org.spec.jappserver.orders.OrderLine;

public class OrderDataBean implements Serializable, Comparable<OrderDataBean> {

    private Integer     orderID;            /* orderID */
    private int         orderStatus;
    private java.sql.Date   shipDate;
    private java.sql.Timestamp  entryDate;
    private BigDecimal      total;
    private BigDecimal      discount;
    private int         orderLineCount;
    private int         customerID;
    private ArrayList       orderLines;

	public OrderDataBean(){
	}

	public OrderDataBean(BigDecimal order_total, BigDecimal total_discount, int o_line_count, int customerId, ArrayList o_lines){
		total = order_total;
		discount = total_discount;
		orderLineCount = o_line_count;
		customerID = customerId;
		orderLines = o_lines;
	}

    /**
     * Returns the customerID.
     * @return int
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Returns the discount.
     * @return double
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * Returns the entryDate.
     * @return java.sql.Timestamp
     */
    public java.sql.Timestamp getEntryDate() {
        return entryDate;
    }

    /**
     * Returns the orderID.
     * @return Integer
     */
    public Integer getOrderID() {
        return orderID;
    }

    /**
     * Returns the orderLineCount.
     * @return int
     */
    public int getOrderLineCount() {
        return orderLineCount;
    }

    /**
     * Returns the orderStatus.
     * @return int
     */
    public int getOrderStatus() {
        return orderStatus;
    }

    /**
     * Returns the shipDate.
     * @return java.sql.Date
     */
    public java.sql.Date getShipDate() {
        return shipDate;
    }

    /**
     * Returns the total.
     * @return double
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Sets the customerID.
     * @param customerID The customerID to set
     */
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Sets the discount.
     * @param discount The discount to set
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    /**
     * Sets the entryDate.
     * @param entryDate The entryDate to set
     */
    public void setEntryDate(java.sql.Timestamp entryDate) {
        this.entryDate = entryDate;
    }

    /**
     * Sets the orderID.
     * @param orderID The orderID to set
     */
    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    /**
     * Sets the orderLineCount.
     * @param orderLineCount The orderLineCount to set
     */
    public void setOrderLineCount(int orderLineCount) {
        this.orderLineCount = orderLineCount;
    }

    /**
     * Sets the orderStatus.
     * @param orderStatus The orderStatus to set
     */
    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * Sets the shipDate.
     * @param shipDate The shipDate to set
     */
    public void setShipDate(java.sql.Date shipDate) {
        this.shipDate = shipDate;
    }

    /**
     * Sets the total.
     * @param total The total to set
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /**
     * Returns the orderLines.
     * @return ArrayList
     */
    public ArrayList getOrderLines() {
        return orderLines;
    }

    /**
     * Sets the orderLines.
     * @param orderLines The orderLines to set
     */
    public void setOrderLines(Collection<OrderLine> ordLines) {
        orderLines = new ArrayList();
        if( ordLines == null ) return;
        for (OrderLine orderLine : ordLines)
        {
           orderLines.add(orderLine.getDataBean());
        }
    }

    @Override
    public boolean equals (Object o1) {
       OrderDataBean odb = (OrderDataBean) o1;
       if( orderID == odb.orderID ) return true;
       else return false;
   }

    
    @Override
    public int hashCode() {
        return System.identityHashCode(orderID);
    }

    public int compareTo (OrderDataBean o)
    {
       if( orderID < o.orderID ) {
           return -1;
       } else if( orderID == o.orderID ) {
           return 0;
       } else {
           return 1;
       }
    }
}

