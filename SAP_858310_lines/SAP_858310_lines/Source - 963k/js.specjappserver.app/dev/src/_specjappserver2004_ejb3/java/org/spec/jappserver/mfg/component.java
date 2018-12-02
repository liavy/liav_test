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
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * EJB3 equivalent for ComponentEnt
 */
@Entity
@Table(name = "M_PARTS")
@NamedQuery(name = "preloadComponents", query = "select b.component from Assembly a join a.bOMs b where a = :ass")
public class Component
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   // persistent
   private String id;
   private String name;
   private String description;
   private String revision;
   private int planner;
   private int type;
   private int purchased;
   private int lomark;
   private int himark;

   // relationship
   private Inventory inventory;

   /**
    * Default constructor
    */
   public Component ()
   {
   }

   @Basic @Column(name = "P_DESC")
   public String getDescription ()
   {
      return description;
   }

   public void setDescription (String description)
   {
      this.description = description;
   }

   @Basic @Column(name = "P_HIMARK")
   public int getHimark ()
   {
      return himark;
   }

   public void setHimark (int himark)
   {
      this.himark = himark;
   }

   @Id @Column(name = "P_ID")
   public String getId ()
   {
      return id;
   }

   public void setId (String id)
   {
      this.id = id;
   }

   @Basic @Column(name = "P_LOMARK")
   public int getLomark ()
   {
      return lomark;
   }

   public void setLomark (int lomark)
   {
      this.lomark = lomark;
   }

   @Basic @Column(name = "P_NAME")
   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   @Basic @Column(name = "P_PLANNER")
   public int getPlanner ()
   {
      return planner;
   }

   public void setPlanner (int planner)
   {
      this.planner = planner;
   }

   @Basic @Column(name = "P_IND")
   public int getPurchased ()
   {
      return purchased;
   }

   public void setPurchased (int purchased)
   {
      this.purchased = purchased;
   }

   @Basic @Column(name = "P_REV")
   public String getRevision ()
   {
      return revision;
   }

   public void setRevision (String revision)
   {
      this.revision = revision;
   }

   @Basic @Column(name = "P_TYPE")
   public int getType ()
   {
      return type;
   }

   public void setType (int type)
   {
      this.type = type;
   }

   @OneToOne
   @JoinColumn(name="P_ID")
   public Inventory getInventory ()
   {
      return inventory;
   }

   public void setInventory (Inventory inventory)
   {
      this.inventory = inventory;
   }
}
