/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2005/12/22  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.spec.jappserver.mfg.helper.WorkOrderState;

/**
 * EJB3 equivalent for WorkOrderEnt
 */
@Entity
@Table(name = "M_WORKORDER")
@NamedQuery(name = "getWorkOrder", query = "select w from WorkOrder w where w.id = ?1")
public class WorkOrder
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   @TableGenerator(
         name="workorder",
         table="U_SEQUENCES",
         pkColumnName="S_ID",
         valueColumnName="S_NEXTNUM",
         pkColumnValue="workorder",
         initialValue=1000000000,
         allocationSize=10000)

   // persistent
   private int id;
   private int salesId;
   private int oLineId;
   private int status;
   private int origQty;
   private int compQty;
   private Date dueDate;
   private Timestamp startDate;

   // version column
   private int version;

   // relationship
   private Assembly assembly;

   protected WorkOrderState wos = null;

   /**
    * Default constructor
    */
   public WorkOrder()
   {
   }

   /**
    * Constructor
    *
    * @param salesId  sales order Id for a custom order
    * @param oLineId  order line id in sales order
    * @param assemblyId assembly id
    * @param origQty  original qty
    * @param dueDate  date when order is due
    */
   public WorkOrder (int salesId, int oLineId, int origQty, Date dueDate)
   {
      setSalesId(salesId);
      setOLineId(oLineId);
      setOrigQty(origQty);
      setDueDate(dueDate);
      wos = WorkOrderState.getInstance(WorkOrderState.OPEN);
      status = wos.getStatus();
      setStartDate(new Timestamp(new java.util.Date().getTime()));
      setCompQty(0);
   }

   /**
    * Constructor
    *
    * @param assemblyId assembly id
    * @param origQty  original qty
    * @param dueDate  date when order is due
    */
   public WorkOrder (int origQty, Date dueDate)
   {
      this(0, 0, origQty, dueDate);
   }

   private void initWOS() {
       if (wos == null) {
           ejbLoad();
       }
   }
   
//   @PostLoad
   public void ejbLoad()
   {
       wos = WorkOrderState.getInstance(getStatus());
   }

//   @PrePersist
   public void ejbStore()
   {
       initWOS();
       status = wos.getStatus();
   }

   public void process()
   {
      initWOS();
      wos = wos.process();
      status = wos.getStatus();
   }

   public void nextState ()
   {
      initWOS();
      wos = wos.nextState();
      status = wos.getStatus();
   }

   public void finish ()
   {
      initWOS();
      wos = wos.finish();
      status = wos.getStatus();
   }

   public void cancel ()
   {
      initWOS();
      wos = wos.cancel();
      status = wos.getStatus();
   }

   @Column(name = "WO_COMP_QTY")
   public int getCompQty ()
   {
      return compQty;
   }

   public void setCompQty (int compQty)
   {
      this.compQty = compQty;
   }

   @Basic @Column(name = "WO_DUE_DATE")
   public Date getDueDate ()
   {
      return dueDate;
   }

   public void setDueDate (Date dueDate)
   {
      this.dueDate = dueDate;
   }

   @Id @Column(name = "WO_NUMBER")
   @GeneratedValue(strategy=GenerationType.TABLE, generator="workorder")
   public int getId ()
   {
      return id;
   }

   public void setId (int id)
   {
      this.id = id;
   }

   @Basic @Column(name = "WO_OL_ID")
   public int getOLineId ()
   {
      return oLineId;
   }

   public void setOLineId (int lineId)
   {
      oLineId = lineId;
   }

   @Basic @Column(name = "WO_ORIG_QTY")
   public int getOrigQty ()
   {
      return origQty;
   }

   public void setOrigQty (int origQty)
   {
      this.origQty = origQty;
   }

   @Basic @Column(name = "WO_O_ID")
   public int getSalesId ()
   {
      return salesId;
   }

   public void setSalesId (int salesId)
   {
      this.salesId = salesId;
   }

   @Basic @Column(name = "WO_START_DATE")
   public Timestamp getStartDate ()
   {
      return startDate;
   }

   public void setStartDate (Timestamp startDate)
   {
      this.startDate = startDate;
   }

   @Basic @Column(name = "WO_STATUS")
   public int getStatus ()
   {
      return status;
   }

   public void setStatus (int status)
   {
      this.status = status;
      wos = null;
   }

   /**
    * Relationship.
    * @return assembly value
    */
   @ManyToOne
   @JoinColumn(name="WO_ASSEMBLY_ID")
   public Assembly getAssembly ()
   {
      return assembly;
   }

   /**
    * Relationship.
    * @param assembly assembly value
    */
   public void setAssembly (Assembly ass)
   {
      assembly = ass;
   }

   @Version @Column(name = "WO_VERSION")
   public int getVersion()
   {
      return version;
   }

   public void setVersion(int version)
   {
      this.version = version;
   }
}
