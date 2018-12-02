/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2006/01/17  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.orders;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.spec.jappserver.orders.helper.OrderLineDataBean;

/**
 * EJB3 equivalent for OrderEnt
 */
@Entity
@IdClass(OrderLinePK.class)
@Table(name = "O_ORDERLINE")
public class OrderLine
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id @Column(name = "OL_ID")
   protected int orderLineId;

// solution without helper table
//   @Id @Column(name = "OL_O_ID", updatable = false)
   @Id @Column(name = "OL_O_ID")
   protected int orderId;

   @Basic @Column(name = "OL_I_ID")
   protected String itemId;

   @Basic @Column(name = "OL_QTY")
   protected int quantity;

   @Basic @Column(name = "OL_STATUS")
   protected int olineStatus;

   @Basic @Column(name = "OL_SHIP_DATE")
   protected Date shipDate;

   @Basic @Column(name = "OL_TOTAL_VALUE")
   protected BigDecimal totalValue;

   @Basic @Column(name = "OL_MSRP")
   protected BigDecimal msrpAtPurchase;

   // version column
   @Version @Column(name = "OL_VERSION")
   protected int version;

   // relationship
   // solution without helper table
   @ManyToOne
   @JoinColumn(name="OL_O_ID", updatable = false, insertable = false)
   protected Order order;

   /**
    * Default constructor
    */
   public OrderLine ()
   {
   }

   /**
    * This method returns a data structure to the user containing information
    * about this specific part of an order
    * @return OrderLineDataBean object
    */
   public OrderLineDataBean getDataBean()
   {
      OrderLineDataBean bean = new OrderLineDataBean();
      bean.setID(getOrderLineId());
      bean.setItemID(getItemId());
      bean.setOrderID(getOrderId());
      bean.setQuantity(getQuantity());
      bean.setTotalValue(getTotalValue());
      bean.setShipDate(getShipDate());
      return bean;
   }

   /**
    * Persistent field.
    *
    * @return shipping date
    */
   public Date getShipDate ()
   {
      return shipDate;
   }

   /**
    * Persistent field.
    *
    * @return item identifier
    */
   public String getItemId ()
   {
      return itemId;
   }

   /**
    * Persistent field.
    *
    * @return manufacturer suggested retail prica
    */
   public BigDecimal getMsrpAtPurchase ()
   {
      return msrpAtPurchase;
   }

   /**
    * Persistent field.
    *
    * @return orderline status
    */
   public int getOlineStatus ()
   {
      return olineStatus;
   }

   /**
    * Persistent field.
    *
    * @return orderline identifier
    */
   public int getOrderLineId ()
   {
      return orderLineId;
   }

   /**
    * Persistent field.
    *
    * @return quantity value
    */
   public int getQuantity ()
   {
      return quantity;
   }

   /**
    * Persistent field.
    *
    * @return total value
    */
   public BigDecimal getTotalValue ()
   {
      return totalValue;
   }

   /**
    * Persistent field.
    *
    * @return order identifier
    */
   public int getOrderId ()
   {
      return orderId;
   }

   /**
    * Persistent field.
    * Version column.
    *
    * @return version
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * Persistent field.
    *
    * @param date shipping date
    */
   public void setShipDate (Date date)
   {
      this.shipDate = date;
   }

   /**
    * Persistent field.
    *
    * @param string item identifier
    */
   public void setItemId (String string)
   {
      itemId = string;
   }

   /**
    * Persistent field.
    *
    * @param decimal manufacturer suggested retail price
    */
   public void setMsrpAtPurchase (BigDecimal decimal)
   {
      msrpAtPurchase = decimal;
   }

   /**
    * Persistent field.
    *
    * @param i orderline status
    */
   public void setOlineStatus (int i)
   {
      olineStatus = i;
   }

   /**
    * Persistent field.
    *
    * @param i orderline identifier
    */
   public void setOrderLineId (int i)
   {
      orderLineId = i;
   }

   /**
    * Persistent field.
    *
    * @param i quantity value
    */
   public void setQuantity (int i)
   {
      quantity = i;
   }

   /**
    * Persistent field.
    *
    * @param decimal total value
    */
   public void setTotalValue (BigDecimal decimal)
   {
      totalValue = decimal;
   }

   /**
    * Persistent field.
    *
    * @param i order identifier
    */
   public void setOrderId (int i)
   {
      orderId = i;
   }

   /**
    * Persistent field.
    * Version column.
    *
    * @param int version
    */
   public void setVersion(int version)
   {
      this.version = version;
   }

// solution without helper table
   /**
    * Relationship.
    * @return order
    */
   public Order getOrder ()
   {
      return order;
   }

   /**
    * Relationship.
    * @param order order value
    */
   public void setOrder (Order ord)
   {
      order = ord;
   }
}
