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
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.spec.jappserver.orders.helper.OrderDataBean;

/**
 * EJB3 equivalent for OrderEnt
 */
@Entity(name = "Order_") // "order" is a reserved (case insensitive) identifier in JPQL. It is not allowed as entity name.
@Table(name = "O_ORDERS")
@NamedQueries(value= {
      @NamedQuery(name = "getOrdersByCustomerIdOrderedByOrderId",
            query = "select o from Order_ o where o.customerId = ?1 order by o.orderId"),
      @NamedQuery(name = "getOpenOrdersByCustomerIdOrderedByOrderId",
            query = "select o from Order_ o where o.customerId = ?1 and (o.orderStatus = 1 or o.orderStatus = 2) order by o.orderId")})
public class Order
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   @TableGenerator(
         name="order",
         table="U_SEQUENCES",
         pkColumnName="S_ID",
         valueColumnName="S_NEXTNUM",
         pkColumnValue="order",
         initialValue=1000000000,
         allocationSize=10000)

   // persistent
   @Id @Column(name = "O_ID")
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "order")
   protected int orderId;

   @Basic @Column(name = "O_C_ID")
   protected int customerId;

   @Basic @Column(name = "O_OL_CNT")
   protected int orderLinesCount;

   @Basic @Column(name = "O_ENTRY_DATE")
   protected Timestamp entryDate;

   @Basic @Column(name = "O_SHIP_DATE")
   protected Date shipDate;

   @Basic @Column(name = "O_STATUS")
   protected int orderStatus;

   @Basic @Column(name = "O_TOTAL")
   protected BigDecimal total;

   @Basic @Column(name = "O_DISCOUNT")
   protected BigDecimal discount;

   // version column
   @Version @Column(name = "O_VERSION")
   protected int version;

   // relationship
   // solution with helper table
//   @OneToMany(cascade=CascadeType.ALL)
//   @JoinTable(
//         name = "O_ORDER_ORDER_LINE",
//         joinColumns = @JoinColumn(name="O_ID"),
//         inverseJoinColumns= {
//               @JoinColumn(name = "OL_ID", referencedColumnName = "OL_ID"),
//               @JoinColumn(name = "OL_O_ID", referencedColumnName = "OL_O_ID")})
//   protected List<OrderLine> orderLines;

//    solution without helper table:
    @OneToMany(cascade=CascadeType.ALL, mappedBy="order")
    protected List<OrderLine> orderLines;

   /**
    * Default constructor
    */
   public Order ()
   {
   }

   /**
    * This method returns to the user a structure filled with information about
    * this order.
    * @return OrderDataBean object
    */
   @SuppressWarnings("boxing")
public OrderDataBean getDataBean()
   {
      OrderDataBean bean = new OrderDataBean();
      bean.setCustomerID(getCustomerId());
      bean.setDiscount(getDiscount());
      bean.setEntryDate(getEntryDate());
      bean.setOrderID(getOrderId());
      bean.setOrderLineCount(getOrderLinesCount());
      bean.setOrderLines(getOrderLines());
      bean.setOrderStatus(getOrderStatus());
      bean.setShipDate(getShipDate());
      bean.setTotal(getTotal());
      return bean;
   }

   /**
    * Persistent field.
    *
    * @return customer id
    */
   public int getCustomerId ()
   {
      return customerId;
   }

   /**
    * Persistent field.
    *
    * @return discount value
    */
   public BigDecimal getDiscount ()
   {
      return discount;
   }

   /**
    * Persistent field.
    *
    * @return entry date
    */
   public Timestamp getEntryDate ()
   {
      return entryDate;
   }

   /**
    * Persistent field.
    *
    * @return order id
    */
   public int getOrderId ()
   {
      return orderId;
   }

   /**
    * Persistent field.
    *
    * @return order lines count
    */
   public int getOrderLinesCount ()
   {
      return orderLinesCount;
   }

   /**
    * Persistent field.
    *
    * @return order status
    */
   public int getOrderStatus ()
   {
      return orderStatus;
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
    * @return total value
    */
   public BigDecimal getTotal ()
   {
      return total;
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
    * @param i customer id
    */
   public void setCustomerId (int i)
   {
      customerId = i;
   }

   /**
    * Persistent field.
    *
    * @param decimal discount value
    */
   public void setDiscount (BigDecimal decimal)
   {
      discount = decimal;
   }

   /**
    * Persistent field.
    *
    * @param timestamp entry date
    */
   public void setEntryDate (Timestamp timestamp)
   {
      entryDate = timestamp;
   }

   /**
    * Persistent field.
    *
    * @param i order id
    */
   public void setOrderId (int i)
   {
      orderId = i;
   }

   /**
    * Persistent field.
    *
    * @param i order id
    */
   public void setOrderLinesCount (int i)
   {
      orderLinesCount = i;
   }

   /**
    * Persistent field.
    *
    * @param i order status
    */
   public void setOrderStatus (int i)
   {
      orderStatus = i;
   }

   /**
    * Persistent field.
    *
    * @param date shipping date
    */
   public void setShipDate (Date date)
   {
      shipDate = date;
   }

   /**
    * Persistent field.
    *
    * @param decimal total value
    */
   public void setTotal (BigDecimal decimal)
   {
      total = decimal;
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

   /**
    * Relationship.
    *
    * @return order lines
    */
   public List<OrderLine> getOrderLines ()
   {
      return orderLines;
   }

   /**
    * Relationship.
    *
    * @param ordLines order lines
    */
   public void setOrderLines (List<OrderLine> ordLines)
   {
      orderLines = ordLines;
   }

   /**
    * Persistent field.
    *
    * @param ordLines order lines
    */
   public void addOrderLine(OrderLine orderLine)
   {
      if (orderLines == null)
      {
         orderLines = new LinkedList<OrderLine>();
      }
      orderLines.add(orderLine);
   }
}
