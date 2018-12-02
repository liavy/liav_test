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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

import org.spec.jappserver.orders.helper.ItemsDataBean;

/**
 * EJB3 equivalent for ItemEnt
 */
@Entity
@Table(name = "O_ITEM")
@NamedQueries(value= {
      @NamedQuery(name = "getItemsByCategory",
            query = "select i from Item i where i.category = ?1",
            hints = { @QueryHint(name = "com.sap.jpa.cache.query-result", value = "cached")}
                ),
      @NamedQuery(name = "getItemsByCategoryConstrExp",
            query = "select new org.spec.jappserver.orders.Item(i.itemId, i.price, i.name, i.description, i.discount, i.category) from Item i where i.category = ?1",
            hints = { @QueryHint(name = "com.sap.jpa.cache.query-result", value = "cached")}
                )
      })
public class Item
      implements Serializable, Cloneable
{
   private static final long serialVersionUID = 1L;

   // persistent
   @Id @Column(name = "I_ID")
   protected String itemId;

   @Basic @Column(name = "I_PRICE")
   protected BigDecimal price;

   @Basic @Column(name = "I_NAME")
   protected String name;

   @Basic @Column(name = "I_DESC")
   protected String description;

   @Basic @Column(name = "I_DISCOUNT")
   protected BigDecimal discount;

   @Basic @Column(name = "I_CATEGORY")
   protected int category;

   /**
    * Default constructor
    */
   public Item ()
   {
   }

   /**
    * Constructor
    *
    * @param id Identifier
    * @param price Price
    * @param name Name
    * @param description Description
    * @param discount Discount
    * @param category Category
    */
   public Item (String id, BigDecimal price, String name, String description,
         BigDecimal discount, int category)
   {
      setItemId(id);
      setPrice(price.setScale(2, BigDecimal.ROUND_UP));
      setName(name);
      setDescription(description);
      setDiscount(discount.setScale(4, BigDecimal.ROUND_UP));
      setCategory(category);
   }

   /**
    * Returns back to the user a structure containing all the data from this
    * bean for read purposes
    *
    * @return ItemsDataBean
    * @see org.spec.jappserver.orders.helper.ItemsDataBean
    */
   public org.spec.jappserver.orders.helper.ItemsDataBean getDataBean ()
   {
      ItemsDataBean itemDataBean = new ItemsDataBean();
      itemDataBean.setDescription(getDescription());
      itemDataBean.setDiscount(getDiscount());
      itemDataBean.setName(getName());
      itemDataBean.setPrice(getPrice());
      itemDataBean.setId(getItemId());
      itemDataBean.setCategory(getCategory());
      return itemDataBean;
   }

   @Override
   public Object clone() {
      try {
          return super.clone();
      } catch (CloneNotSupportedException e) {
          throw new Error("This should not occur since we implement Cloneable");
      }
  }

   /**
    * Persistent field.
    *
    * @return category value
    */
   public int getCategory ()
   {
      return category;
   }

   /**
    * Persistent field.
    *
    * @return description value
    */
   public String getDescription ()
   {
      return description;
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
    * @return item identifier
    */
   public String getItemId ()
   {
      return itemId;
   }

   /**
    * Persistent field.
    *
    * @return name value
    */
   public String getName ()
   {
      return name;
   }

   /**
    * Persistent field.
    *
    * @return price value
    */
   public BigDecimal getPrice ()
   {
      return price;
   }

   /**
    * Persistent field.
    *
    * @param i category value
    */
   public void setCategory (int i)
   {
      category = i;
   }

   /**
    * Persistent field.
    *
    * @param string description value
    */
   public void setDescription (String string)
   {
      description = string;
   }

   /**
    * Persistent field.
    *
    * @param decimal dicsount value
    */
   public void setDiscount (BigDecimal decimal)
   {
      discount = decimal;
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
    * @param string name value
    */
   public void setName (String string)
   {
      name = string;
   }

   /**
    * Persistent field.
    *
    * @param decimal price value
    */
   public void setPrice (BigDecimal decimal)
   {
      price = decimal;
   }
}
