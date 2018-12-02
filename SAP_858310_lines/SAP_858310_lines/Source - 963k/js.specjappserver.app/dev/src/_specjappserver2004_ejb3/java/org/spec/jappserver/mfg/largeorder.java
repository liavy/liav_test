/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC) All
 * rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 * History: Date ID, Company Description ---------- ----------------
 * ---------------------------------------------- 2005/12/22 Bernhard Riedhofer,
 * SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.spec.jappserver.mfg.helper.LargeOrderInfo;

/**
 * EJB3 equivalent for LargeOrderEnt
 */
@Entity
@Table(name = "M_LARGEORDER")
@NamedQueries(value = {
        @NamedQuery(name = "getAllLargeOrders", query = "select lo from LargeOrder lo"),
        @NamedQuery(name = "getLargeOrdersByCategory", query = "select lo from LargeOrder lo where lo.category = ?1"),
        @NamedQuery(name = "getLargeOrdersBySalesOrderIdAndOrderLineNumber", query = "select lo from LargeOrder lo where lo.salesOrderId = ?1 and lo.orderLineNumber = ?2") })
public class LargeOrder
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static int NUM_CATEGORIES = 1;

   @TableGenerator(
         name="largeorder",
         table="U_SEQUENCES",
         pkColumnName="S_ID",
         valueColumnName="S_NEXTNUM",
         pkColumnValue="largeorder",
         initialValue=1000000000,
         allocationSize=10000)

   // persistent
   private int id;
   private int salesOrderId;
   private int orderLineNumber;
   private String assemblyId;
   private int qty;
   private Date dueDate;
   private int category;

   /**
    * Default constructor
    */
   public LargeOrder ()
   {
   }

   /**
    * Constructs the LargeOrder object
    *
    * @param salesOrderId the id of sales order that caused this wo to be
    * created
    * @param orderLineNumber line (row) number in salesOrder identified by
    * salesOrderId
    * @param assemblyId assembly that is going to be manufactured
    * @param qty number of assemblies to be manufactured by this wo
    * @param dueDate date when this order is due
    */
   public LargeOrder (int salesOrderId, int orderLineNumber,
         String assemblyId, short qty, Date dueDate)
   {
      setSalesOrderId(salesOrderId);
      setOrderLineNumber(orderLineNumber);
      setAssemblyId(assemblyId);
      setQty(qty);
      setDueDate(dueDate);
      setCategory(0);
   }


//   @PostPersist
//   public void postCreate()
//   {
//      setCategory(getId() % NUM_CATEGORIES);
//      // TODO:
//   }

   @SuppressWarnings("boxing")
   @Transient
   public LargeOrderInfo getLargeOrderInfo ()
   {
      LargeOrderInfo loi = new LargeOrderInfo();
      loi.id = getId();
      loi.salesOrderId = getSalesOrderId();
      loi.orderLineNumber = getOrderLineNumber();
      loi.assemblyId = getAssemblyId();
      loi.qty = getQty();
      loi.dueDate = getDueDate();
      return loi;
   }

   @Transient
   public int getNumCategories ()
   {
      return NUM_CATEGORIES;
   }

   @Basic @Column(name = "LO_ASSEMBLY_ID")
   public String getAssemblyId ()
   {
      return assemblyId;
   }

   public void setAssemblyId (String str)
   {
      assemblyId = str;
   }

   @Basic @Column(name = "LO_CATEGORY")
   public int getCategory ()
   {
      return category;
   }

   public void setCategory (int i)
   {
      category = i;
   }

   @Basic @Column(name = "LO_DUE_DATE")
   public Date getDueDate ()
   {
      return dueDate;
   }

   public void setDueDate (Date date)
   {
      dueDate = date;
   }

   @Id @Column(name = "LO_ID")
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "largeorder")
   public int getId ()
   {
      return id;
   }

   public void setId (int loId)
   {
      id = loId;
   }

   @Basic @Column(name = "LO_OL_ID")
   public int getOrderLineNumber ()
   {
      return orderLineNumber;
   }

   public void setOrderLineNumber (int olNumber)
   {
      orderLineNumber = olNumber;
   }

   @Basic @Column(name = "LO_QTY")
   public int getQty ()
   {
      return qty;
   }

   public void setQty (int q)
   {
      qty = q;
   }

   @Basic @Column(name = "LO_O_ID")
   public int getSalesOrderId ()
   {
      return salesOrderId;
   }

   public void setSalesOrderId (int soId)
   {
      salesOrderId = soId;
   }
}
