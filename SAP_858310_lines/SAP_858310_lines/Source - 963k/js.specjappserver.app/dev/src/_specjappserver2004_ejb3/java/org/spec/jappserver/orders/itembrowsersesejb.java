/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ------------------------------------------------------------------
 *  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/11/22  Tom Daly    , Sun         Add support for finding items by category, don't use findAll()
 *                                        of itemEnt, remove cache of ItemDataBeans, replace with a cache
 *                                        of ItemEnt references instead
 *  2004/02/16  Samuel Kounev, Darmstadt  Removed unneeded import statements.
 *  2006/01/18  Bernhard Riedhofer, SAP   Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.orders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.corp.CustomerSesEJB;
import org.spec.jappserver.orders.helper.ItemsDataBean;

/**
 * Bean implementation class for Enterprise Bean: ItemBrowserSes
 */
@Stateful (name = "ItemBrowserSes")
public class ItemBrowserSesEJB implements ItemBrowserSes, ItemBrowserSesLocal
{
   private static final long serialVersionUID = 1L;

   // used for item cache which is a workaround for read only beans in original SPECj 
   private static final HashMap<Integer, ArrayList<Item>> itemsPerCategoryCache =
      Config.isItemsCacheUsed ? new HashMap<Integer, ArrayList<Item>>((int) 1.3 * Config.txRate / 2) : null;
   
   @PersistenceContext(unitName = "Orders")
   protected EntityManager em;

   @Resource
   protected int debuglevel = 0;
   Object debugSemaphore = new String();
   Debug debug = null;

   private static final int ITEMS_PER_PAGE = 10;

   private transient ArrayList<Item> itemsList; // transient due to SolidRock failover scenario
   private int itemsListSize;
   private int currentMax, currentMin;
   private ArrayList<String> search;
   private int querySize;
   private transient HashMap<String, Item> itemsMap; // transient due to SolidRock failover scenario
   int category; // needed for executing the query again in case of SolidRock failover

   String getItemsQuery = Config.isConstrExprInItemBrowserSesUsed ? "getItemsByCategoryConstrExp" : "getItemsByCategory";
   
   private void debug(int level, String str)
   {
      synchronized (debugSemaphore)
      {
         if (debug == null)
         {
            if (debuglevel > 0)
            {
               debug = new DebugPrint(debuglevel, CustomerSesEJB.class);
            }
            else
            {
               debug = new Debug();
            }
         }
      }
      debug.println(level, str);
   }

   public ItemBrowserSesEJB ()
   {
      if (debuglevel > 0) debug(3, "ItemBrowserSesEJB () default constructor");
   }

   @Remove
   public void removeBean()
   {
      if (debuglevel > 0) debug(3, "ItemBrowserSesEJB.remove()");
   }

   /**
    * Gets a collection of ITEMS_PER_PAGE items
    * from the query they specified (all or specific vehicles)
    * and within the category specified.
    *
    * @param queryItems
    * @param category what category of items/vehicles to search on
    * @return collection of ITEMS_PER_PAGE items from the query they specified
    *         (all or specific vehicles) and within the category specified
    */
   public Collection<ItemsDataBean> getItems (ArrayList<String> queryItems, int category)
   {
      if (debuglevel > 0) debug(3, "getItems");

      // initialize the first time and in case of SolidRock failover
      this.category = category;
      initItemsList(category);

      ArrayList<ItemsDataBean> itemsDataBeans = new ArrayList<ItemsDataBean>();;
      if (queryItems == null)
      {
         // case of general query over all items
         for (int i = 0; (i < ITEMS_PER_PAGE) && (currentMax < itemsListSize); i++)
         {
            itemsDataBeans.add(itemsList.get(i).getDataBean());
         }
         currentMin = 0;
         currentMax = ITEMS_PER_PAGE;
         search = null;
         querySize = 0;
      }
      else
      {
         // case of specific query on items (vehicle1, vehicle2, etc.)
         search = queryItems;
         currentMin = 0;
         querySize = search.size();
         for (currentMax = 0; (currentMax < querySize)
               && (currentMax < ITEMS_PER_PAGE); currentMax++)
         {
            itemsDataBeans.add(itemsMap.get( ((String) search.get(currentMax)))
                  .getDataBean());
         }
      }

      return itemsDataBeans;
   }

   /**
    * Returns the next ITEMS_PER_PAGE items
    * in the query of items the user executed.
    *
    * @return collection of the next ITEMS_PER_PAGE items
    * in the query of items the user executed
    */
   public Collection<ItemsDataBean> browseForward ()
   {
      if (debuglevel > 0) debug(3, "browseForward");

      // initialize the first time and in case of SolidRock failover
      initItemsList(category);

      ArrayList<ItemsDataBean> itemsToReturn = new ArrayList<ItemsDataBean>();
      currentMin = currentMax;
      if (search == null)
      {
         // case of all item query
         for (; (currentMax < (currentMin + ITEMS_PER_PAGE))
               && (currentMax < itemsListSize); currentMax++)
         {
            itemsToReturn.add(itemsList.get(currentMax).getDataBean());
         }
      }
      else
      {
         // case of specific query
         for (; (currentMax < (currentMin + ITEMS_PER_PAGE))
               && (currentMax < querySize); currentMax++)
         {
            itemsToReturn.add(itemsMap.get( ((String) search.get(currentMax)))
                  .getDataBean());
         }
      }
      return itemsToReturn;
   }

   /**
    * Returns the previous ITEMS_PER_PAGE items
    * in the query of items the user executed.
    *
    * @return collection of the previous ITEMS_PER_PAGE items
    * in the query of items the user executed
    */
   public Collection<ItemsDataBean> browseReverse ()
   {
      if (debuglevel > 0) debug(3, "browseReverse");

      // initialize the first time and in case of SolidRock failover
      initItemsList(category);

      ArrayList<ItemsDataBean> itemsToReturn = new ArrayList<ItemsDataBean>();
      currentMin = currentMin - ITEMS_PER_PAGE;
      currentMax = currentMin;
      if (search == null)
      {
         // case of all item query
         for (; (currentMax < (currentMin + ITEMS_PER_PAGE))
               && (currentMax < itemsListSize); currentMax++)
         {
            itemsToReturn.add(itemsList.get(currentMax).getDataBean());
         }
      }
      else
      {
         // case of specific query
         for (; (currentMax < (currentMin + ITEMS_PER_PAGE))
               && (currentMax < querySize); currentMax++)
         {
            itemsToReturn.add(itemsMap.get( ((String) search.get(currentMax)))
                  .getDataBean());
         }
      }
      return itemsToReturn;
   }

   /**
    * Returns the total number of items that the user has in their query.
    *
    * @return total number of items that the user has in their query
    */
   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   public int getTotalItems ()
   {
      if (debuglevel > 0) debug(3, "getTotalItems");

      return search == null ? itemsListSize : querySize;
   }

   /**
    * Inits the item list into the stateful session bean for lightening quick
    * access after the first hit on purchase inventory page.
    *
    * @param category
    */
   private void initItemsList (int category)
   {
      if (debuglevel > 0) debug(3, "initItemsList");

      if (itemsList != null)
      {
         return;
      }

      // item cache as it might be implemented by ejb3 container
      ArrayList<Item> itemsOfCategory;
      if (Config.isItemsCacheUsed)
      {
         synchronized(itemsPerCategoryCache)
         {
            itemsOfCategory = itemsPerCategoryCache.get(category);
            if (itemsOfCategory == null)
            {
               itemsOfCategory = new ArrayList<Item>(250);
               itemsPerCategoryCache.put(category, itemsOfCategory);
            }
         }
         synchronized (itemsOfCategory)
         {
            if (itemsOfCategory.size() == 0)
            {
               Query query = em.createNamedQuery(getItemsQuery)
                     .setParameter(1, category);
               Config.setFlushMode(query);
               itemsOfCategory.addAll((ArrayList<Item>) query.getResultList());
            }
         }
      }
      else
      {
         Query query = em.createNamedQuery(getItemsQuery)
               .setParameter(1, category);
         Config.setFlushMode(query);
         itemsOfCategory = (ArrayList<Item>) query.getResultList();
      }
      
      itemsList = itemsOfCategory;
      itemsListSize = itemsOfCategory.size();
      itemsMap = new HashMap<String, Item>();
      for (Item item : itemsOfCategory)
      {
         itemsMap.put(item.getItemId(), item);
      }
   }

   /**
    * Returns the current max item the user is browsing (ie 20 if the user is browsing 11-20)
    * @return int
    */
   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   public int getCurrentMax ()
   {
      return currentMax;
   }

   /**
    * Returns the current min item the user is browsing (ie 11 if the user is browsing 11-20)
    * @return int
    */
   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   public int getCurrentMin ()
   {
      return currentMin;
   }
}
