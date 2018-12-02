/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company              Description
 *  ----------  ----------------------   ----------------------------------------------
 *  2003/01/01  John Stecher, IBM        Created for SPECjAppServer2004
 *  2003/01/01  Samuel Kounev, Darmstadt added second addInventory() method for completed LOs
 *  2003/03/26  John Stecher, IBM        added Big Decimal support
 *  2003/04/01  John Stecher, IBM        updated debugging
 *  2003/05/05  John Stecher, IBM        Made changes to ensure that users are updated when car is sold out from
 *                                       under them with a warning message.
 *  2003/06/17  John Stecher, IBM        Update to make better use of BigDecimal
 *  2003/06/28  John Stecher, IBM        Removed unnecessary imports
 *  2003/08/30  John Stecher, IBM        Updated for new sell functionality
 *  2003/12/15  John Stecher, IBM        Changed for Atomicity Test
 *  2003/01/08  John Stecher, IBM        Changed code to eliminate unused objects being
 *                                       passed into methods
 *  2005/12/22  Bernhard Riedhofer, SAP  Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.corp;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.corp.helper.CustomerDataBean;
import org.spec.jappserver.corp.helper.CustomerInventoryDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;
import org.spec.jappserver.orders.helper.ShoppingCartDataBean;

/**
 * Bean implementation class for CustomerSes
 */
@Stateless (name = "CustomerSes")
public class CustomerSesEJB implements CustomerSes, CustomerSesLocal
{
   @PersistenceContext (unitName="Corp")
   protected EntityManager em;

   @Resource
   protected SessionContext ctx;

   @Resource
   protected int debuglevel = 0;
   protected Object debugSemaphore = new Object();
   protected Debug debug = null;

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

   public CustomerSesEJB ()
   {
      if (debuglevel > 0) debug(3, "CustomerSesEJB () default constructor");
   }

   private Customer findCustomer(int id)
   {
      Customer cust = em.getReference(Customer.class, id);
      if (Config.isLockingUsed)
      {
         if (cust != null)
         {
            em.lock(cust, Config.lockMode);
         }
      }
      return cust;
   }

   // validates a dealerships account exist given the userID
   public boolean validateCustomer (int userID)
   {
      if (debuglevel > 0) debug(3, "validateCustomer");

      Customer cust = em.find(Customer.class, userID);
      boolean found = cust != null;
      if (!found)
      {
         if (debuglevel > 0) debug(3, "User " + userID + " not found in database");
      }
      return found;
   }

   // returns a dealerships account infomation based off of the userID
   public CustomerDataBean getCustomerInfo (int userID)
   {
      if (debuglevel > 0) debug(3, "getCustomerInfo");

      Customer cust = em.find(Customer.class, userID);
      return cust.getDataBean();
   }

   // return the inventory of a specific dealership given userID
   public Set getInventory (int userID)
   {
      if (debuglevel > 0) debug(3, "getInventory");

      Customer cust = em.find(Customer.class, userID);;
      Set<CustomerInventory> custInv = cust.getCustomerInventory();

      TreeSet<CustomerInventoryDataBean> custInvDataBeans = new TreeSet<CustomerInventoryDataBean>();
      Iterator<CustomerInventory> itr = custInv.iterator();

      // Walk the CMR fully - Readonly
      while (itr.hasNext())
      {
         CustomerInventory ci = itr.next();
         custInvDataBeans.add(ci.getDataBean());
      }
      return custInvDataBeans;
   }

   public boolean sellInventory (int userID, int inventoryID,
         boolean isAtomicityTest)
   {
      if (debuglevel > 0) debug(3, "sellInventory");

      Customer cust = em.find(Customer.class, userID);
      CustomerInventory inventory = em.find(CustomerInventory.class,
            inventoryID);
      CustomerInventoryDataBean cidb = inventory.getDataBean();

      // Add back to the dealerships balance the value of the cars sold
      cust.setBalance(cust.getBalance().add(cidb.getTotalCost()).setScale(2,
            BigDecimal.ROUND_UP)); // no lock needed if getTotalCost()==0
      // cust.getCustomerInventory().remove(inventory); // additional select on DB!! but not necessary since outside of method nothing else is done
      // inventory.setCustomer(null);
      
      // Remove the vehicles from the inventory
      em.remove(inventory);
      // moved for EJB3 to this place in order to call
      // remove() after cibd.getTotalCost() is called

      if (isAtomicityTest)
      {
         ctx.setRollbackOnly();
      }
      return true;
   }

   // add shopping cart full of vehicles to the current dealership inventory
   // update all account figures.
   public void addInventory (int custID, ShoppingCart sc,
         BigDecimal orderCost, boolean atomicityTest)
         throws SPECjAppServerException
   {
      if (debuglevel > 0) debug(3, "addInventory");

      Customer cust = em.find(Customer.class, custID);

      // Add the contents of the cart to the dealerships inventory
      for (int i = 0; i < sc.getItemCount(); i++)
      {
         ShoppingCartDataBean scdb = sc.getItem(i);
         CustomerInventory custInv = new CustomerInventory(cust,
               scdb.getItemID(), scdb.getQuantity(), scdb.getTotalCost());
         cust.getCustomerInventory().add(custInv); // not needed?
         em.persist(custInv);
      }
      cust.setBalance(cust.getBalance().subtract(orderCost).setScale(2,
            BigDecimal.ROUND_UP));

      if (atomicityTest)
      {
         ctx.setRollbackOnly();
         throw new SPECjAppServerException();
      }
   }

   // adds completed large order to the current dealership inventory
   public void addInventory (int custId, String itemId, int quantity,
         BigDecimal totalValue)
   {
      if (debuglevel > 0) debug(3, "addInventory");

      Customer cust = em.find(Customer.class, custId);
      CustomerInventory custInv = new CustomerInventory(cust,
            itemId, quantity, totalValue);
      cust.getCustomerInventory().add(custInv); // not needed?
      em.persist(custInv);
   }

   public boolean checkCustomerCredit (int custID, BigDecimal orderCost)
   {
      if (debuglevel > 0) debug(3, "checkCustomerCredit");

      Customer cust = findCustomer(custID);
      return cust.hasSufficientCredit(orderCost);
   }
}
