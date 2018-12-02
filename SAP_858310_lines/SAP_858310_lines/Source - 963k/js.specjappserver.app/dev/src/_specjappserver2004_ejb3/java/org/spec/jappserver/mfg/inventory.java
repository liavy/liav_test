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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * EJB3 equivalent for InventoryEnt
 */
@Entity
@Table(name = "M_INVENTORY")
@NamedQueries(value= {
      @NamedQuery(name = "getInventory",
            query = "select i from Inventory i where i.partId = ?1"
                ),
      @NamedQuery(name = "preloadInventories",
            query = "select b.component.inventory from Assembly a join a.bOMs b where a = :ass"
                )
      })
public class Inventory
      implements Serializable
{
   private static final long serialVersionUID = 1L;

   // persistent
   private String partId;
   private int qty;
   private String location;
   private int accCode;
   private Date accDate;
   private int in_ordered;

   // version column
   private int version;

   /**
    * Default constructor
    */
   public Inventory ()
   {
   }

   /**
     * Constructs the Inventory object and stores the information into the DB.
     *
     * @param partId
     * @param qty Quantity
     * @param in_ordered
     * @param location Warehouse location
     * @param accCode Account Finance Code
     * @param accDate Date/time of last activity
     */
   public Inventory (String partId, int qty, int in_ordered, String location,
         int accCode, Date accDate)
   {
      setPartId(partId);
      setQty(qty);
      setIn_ordered(in_ordered);
      setLocation(location);
      setAccCode(accCode);
      setAccDate(accDate);
   }

   /**
    * Updates the quantity of inventory. Occurs when an item has been completed
    *
    * @param qty Number of items to be added.
    */
   public void add (int qty)
   {
      setQty(getQty() + qty);
   }

   /**
    * Deletes the specified quantity of inventory. Occurs when an item has been
    * ordered/shipped.
    *
    * @param qty Number of items to be removed.
    */
   public void take (int qty)
   {
      setQty(getQty() - qty);
   }

   /**
    * Returns the qty value of this object
    *
    * @return the number of items for this object
    */
   @Transient
   public int getOnHand ()
   {
      return getQty();
   }

   /**
    * Added by DG Get the number on ordered
    */
   @Transient
   public int getOrdered ()
   {
      return getIn_ordered();
   }

   /**
    * Added by DG Add to the number on order.
    */
   public void addOrdered (int qty)
   {
      setIn_ordered(getIn_ordered() + qty);
   }

   /**
    * Added by DG Subtract from the number ordered.
    */
   public void takeOrdered (int qty)
   {
      int tmp = getIn_ordered() - qty;
      setIn_ordered( (tmp < 0) ? 0 : tmp);
   }

   @Basic @Column(name = "IN_ACC_CODE")
   public int getAccCode ()
   {
      return accCode;
   }

   public void setAccCode (int accCode)
   {
      this.accCode = accCode;
   }

   @Basic @Column(name = "IN_ACT_DATE")
   public Date getAccDate ()
   {
      return accDate;
   }

   public void setAccDate (Date accDate)
   {
      this.accDate = accDate;
   }

   @Basic @Column(name = "IN_ORDERED")
   public int getIn_ordered ()
   {
      return in_ordered;
   }

   public void setIn_ordered (int in_ordered)
   {
      this.in_ordered = in_ordered;
   }

   @Basic @Column(name = "IN_LOCATION")
   public String getLocation ()
   {
      return location;
   }

   public void setLocation (String location)
   {
      this.location = location;
   }

   @Id @Column(name = "IN_P_ID")
   public String getPartId ()
   {
      return partId;
   }

   public void setPartId (String partId)
   {
      this.partId = partId;
   }

   @Basic @Column(name = "IN_QTY")
   public int getQty ()
   {
      return qty;
   }

   public void setQty (int qty)
   {
      this.qty = qty;
   }

   @Version @Column(name = "IN_VERSION")
   public int getVersion()
   {
      return version;
   }

   public void setVersion(int version)
   {
      this.version = version;
   }
}

