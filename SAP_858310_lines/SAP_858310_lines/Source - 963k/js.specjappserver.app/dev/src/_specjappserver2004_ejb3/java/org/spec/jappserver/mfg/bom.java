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
package org.spec.jappserver.mfg;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * EJB3 equivalent for BomEnt
 */
@Entity
@IdClass(BomPK.class)
@Table(name = "M_BOM")
public class Bom
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String assemblyId;
   private String componentId;
   private int lineNo;
   private int qty;
   private String engChange;
   private int opsNo;
   private String opsDesc;

   // relationship
   private Assembly assembly;
   private Component component;

   /**
    * Default constructor
    */
   public Bom ()
   {
   }

   @Id @Column(name = "B_ASSEMBLY_ID")
   public String getAssemblyId ()
   {
      return assemblyId;
   }

   public void setAssemblyId (String assemblyId)
   {
      this.assemblyId = assemblyId;
   }

   @Id @Column(name = "B_COMP_ID")
   public String getComponentId ()
   {
      return componentId;
   }

   public void setComponentId (String componentId)
   {
      this.componentId = componentId;
   }

   @Basic @Column(name = "B_ENG_CHANGE")
   public String getEngChange ()
   {
      return engChange;
   }

   public void setEngChange (String engChange)
   {
      this.engChange = engChange;
   }

   @Id @Column(name = "B_LINE_NO")
   public int getLineNo ()
   {
      return lineNo;
   }

   public void setLineNo (int lineNo)
   {
      this.lineNo = lineNo;
   }

   @Basic @Column(name = "B_OPS_DESC")
   public String getOpsDesc ()
   {
      return opsDesc;
   }

   public void setOpsDesc (String opsDesc)
   {
      this.opsDesc = opsDesc;
   }

   @Basic @Column(name = "B_OPS")
   public int getOpsNo ()
   {
      return opsNo;
   }

   public void setOpsNo (int opsNo)
   {
      this.opsNo = opsNo;
   }

   @Basic @Column(name = "B_QTY")
   public int getQty ()
   {
      return qty;
   }

   public void setQty (int qty)
   {
      this.qty = qty;
   }

   @ManyToOne
   @JoinColumn(name="B_ASSEMBLY_ID")
   public Assembly getAssembly ()
   {
      return assembly;
   }

   public void setAssembly (Assembly assembly)
   {
      this.assembly = assembly;
   }

   @ManyToOne
   @JoinColumn(name="B_COMP_ID")
   public Component getComponent ()
   {
      return component;
   }

   public void setComponent (Component component)
   {
      this.component = component;
   }
}
