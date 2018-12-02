/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/04/15  John Stecher, IBM         added debugging
 *  2003/04/20  John Stecher, IBM         optimized lookup of itemDataBeans
 *  2003/04/20  John Stecher, IBM         Explicitly remove session beans after use
 *  2003/04/24  John Stecher, IBM         Updated doPurchase to return a ArrayList because we
 *                                        are now using the session bean to return the ArrayList
 *                                        as a wrapper around the items, and the orderDataBean
 *  2003/04/29  John Stecher, IBM         Updated to use javax.rmi.PortableRemoteObject.narrow
 *  2003/05/01  John Stecher, IBM         Imported Russ Raymundo's mfg side of the benchmark for web base driving
 *  2003/05/05  John Stecher, IBM         Made changes to catch noSuchObject exception on remove and display
 *                                        warning message.
 *  2003/05/05  John Stecher, IBM         Made changes to ensure that users are updated when car is sold out from
 *                                        under them with a warning message.
 *  2003/05/06  John Stecher, IBM         Made changes to allow drive instead of application to determine
 *                                        if an order is to be deferred or added to inventory immediately
 *  2003/06/25  John Stecher, IBM         Made changes to this code in line with best practices
 *  2003/08/30  John Stecher, IBM         Updated for new sell functionality
 *  2003/10/27  John Stecher, IBM         Updated to store ItemBrowserSes handle in HTTP Session instead of bean
 *  2003/11/26  Tom Daly, Sun             Added support for category to getVehicleQuotes()
 *  2003/12/05  John Stecher, IBM         Added Atomicity Tests
 *  2004/01/08  John Stecher, IBM         Changed code to eliminate unused objects being passed into methods
 *  2004/01/15  John Stecher, IBM         Changes to make Atomicity Tests work in remote case (see osgjava-6324)
 *  2004/01/25  John Stecher, IBM         Added code for cache consistancy test.
 *  2004/02/18  Samuel Kounev, Darmstadt  Modified updateItemPrice to call OrderAuditSes instead of OrderSes.
 *  2004/02/27  Samuel Kounev, Darmstadt  Fixed a bug in atomicityTestTwo: call to doPurchase should catch
 *                                        SPECjAppServerException and not Exception.
 */

package org.spec.jappserver.servlet.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.corp.CustomerSes;
import org.spec.jappserver.corp.helper.CustomerDataBean;
import org.spec.jappserver.corp.helper.CustomerInventoryDataBean;
import org.spec.jappserver.mfg.LargeOrderSes;
import org.spec.jappserver.mfg.WorkOrderSes;
import org.spec.jappserver.mfg.helper.WorkOrderStateConstants;
import org.spec.jappserver.orders.ItemBrowserSes;
import org.spec.jappserver.orders.OrderSes;
import org.spec.jappserver.orders.helper.InsufficientCreditException;
import org.spec.jappserver.orders.helper.ItemsDataBean;
import org.spec.jappserver.orders.helper.OrderDataBean;
import org.spec.jappserver.orders.helper.OrderLineDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;
import org.spec.jappserver.orders.helper.ShoppingCartDataBean;

/**
 * The SpecAction class provides the generic client side access to each of the Spec
 * dealership operations. These include login, logout, view inventory, shopping cart, etc.
 * The SpecAction class handles generic client side processing for each operation type such
 * as input verification, etc.
 * The SpecAction class does not handle user interface processing and should be used by
 * a class that is UI specific. For example, SpecServletAction manages and builds a
 * a web interface to SpecJ, making calls to SpecAction methods to actually performance each operation.
 *
 */
public class SpecAction implements SpecServices {

    public SpecAction() {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.init() ");
        }
    }

   /*
     * This atomicity test will attempt to drive a sell operation on a dealerships inventory,
     * the cars will be removed from the inventory, and then upon attempting to update the dealerships
     * finanical information an exception will be thrown.  The count of inventory items before and after
     * the transaction should remain the same for the transaction to be atomic.
     */
   public boolean atomicityTestThree() throws Exception{
      System.out.println("Starting Atomicity test 3");
      int custNum = 1;
      // Find a customer without bad credit to place an order with
      CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
      OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
      while(!cs.checkCustomerCredit(new Integer(custNum), new BigDecimal("2000000.00"))){
         custNum++;
      }
      Integer custID = new Integer(custNum);

      // Create shopping cart to use while placing order
      ShoppingCart sc = new ShoppingCart();
      ItemBrowserSes ibs = (ItemBrowserSes)SpecBeanFactory.createItemBrowserSes();
      Collection itemsColl = ibs.getItems(null, 0);
      Iterator itr = itemsColl.iterator();
      ItemsDataBean itemToAddToCart = (ItemsDataBean)itr.next();
      ShoppingCartDataBean scdb = new ShoppingCartDataBean();
      scdb.setCartID(custID);
      scdb.setDescription(itemToAddToCart.getDescription());
      scdb.setDiscount(itemToAddToCart.getDiscount());
      scdb.setItemID(itemToAddToCart.getId());
      scdb.setMSRP(itemToAddToCart.getPrice());
      scdb.setQuantity(25);
      scdb.setVehicle(itemToAddToCart.getName());
      sc.addItem(scdb);
      itemToAddToCart = (ItemsDataBean)itr.next();
      scdb = new ShoppingCartDataBean();
      scdb.setCartID(custID);
      scdb.setDescription(itemToAddToCart.getDescription());
      scdb.setDiscount(itemToAddToCart.getDiscount());
      scdb.setItemID(itemToAddToCart.getId());
      scdb.setMSRP(itemToAddToCart.getPrice());
      scdb.setQuantity(1);
      scdb.setVehicle(itemToAddToCart.getName());
      sc.addItem(scdb);

      // Retrieve out all information needed to compare before and after states
      Integer orderCountInit = os.getOrderCount(custID);
      Collection hColl = this.getHoldings(custID);
      int numOfHoldingsInit = hColl.size();
      BigDecimal balanceInit = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();
      int largeOrderSizeInit = this.findLargeOrders().size();

      // Drive the actual Order
      try{
         this.doPurchase(custID, sc, false, true);
      }catch(SPECjAppServerException e){
         e.printStackTrace();

      }

      // Retrieve out all information needed to compare before and after states
      Integer orderCountAfter = os.getOrderCount(custID);
      hColl = this.getHoldings(custID);
      int numOfHoldingsAfter = hColl.size();
      BigDecimal balanceAfter = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();
      int largeOrderSizeAfter = this.findLargeOrders().size();

      // Remove all session beans used
      SpecBeanFactory.destroyCustomerSes(cs);
      SpecBeanFactory.destroyItemBrowserSes(ibs);
      SpecBeanFactory.destroyOrderSes(os);
      System.out.println("Ending atomicity test 3");
      if((orderCountInit.intValue()==orderCountAfter.intValue())&&(0==balanceAfter.compareTo(balanceInit))&&(numOfHoldingsInit==numOfHoldingsAfter)&&(largeOrderSizeAfter==largeOrderSizeInit)){
         return true;
      }
      return false;
   }

   public boolean atomicityTestTwo() throws Exception{
      System.out.println("Starting atomicity test 2");
      int custNum = 1;
      // Find a customer without bad credit to place an order with
      CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
      OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
      while(!cs.checkCustomerCredit(new Integer(custNum), new BigDecimal("200000.00"))){
         custNum++;
      }
      Integer custID = new Integer(custNum);

      // Create shopping cart to use while placing order
      ShoppingCart sc = new ShoppingCart();
      ItemBrowserSes ibs = (ItemBrowserSes)SpecBeanFactory.createItemBrowserSes();
      Collection itemsColl = ibs.getItems(null, 0);
      Iterator itr = itemsColl.iterator();
      ItemsDataBean itemToAddToCart = (ItemsDataBean)itr.next();
      ShoppingCartDataBean scdb = new ShoppingCartDataBean();
      scdb.setCartID(custID);
      scdb.setDescription(itemToAddToCart.getDescription());
      scdb.setDiscount(itemToAddToCart.getDiscount());
      scdb.setItemID(itemToAddToCart.getId());
      scdb.setMSRP(itemToAddToCart.getPrice());
      scdb.setQuantity(1);
      scdb.setVehicle(itemToAddToCart.getName());
      sc.addItem(scdb);

      // Retrieve out all information needed to compare before and after states
      Integer orderCountInit = os.getOrderCount(custID);
      Collection hColl = this.getHoldings(custID);
      int numOfHoldingsInit = hColl.size();
      BigDecimal balanceInit = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();

      // Drive the actual Order
      try{
         this.doPurchase(custID, sc, false, false);
      }catch(SPECjAppServerException e){
         e.printStackTrace();
      }

      // Retrieve out all information needed to compare before and after states
      Integer orderCountAfter = os.getOrderCount(custID);
      hColl = this.getHoldings(custID);
      int numOfHoldingsAfter = hColl.size();
      BigDecimal balanceAfter = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();

      // Remove all session beans used
      SpecBeanFactory.destroyCustomerSes(cs);
      SpecBeanFactory.destroyItemBrowserSes(ibs);
      SpecBeanFactory.destroyOrderSes(os);
      System.out.println("Ending atomicity test 2");
      if((orderCountInit.intValue()<orderCountAfter.intValue())&&(-1==balanceAfter.compareTo(balanceInit))&&(numOfHoldingsInit<numOfHoldingsAfter)){
         return true;
      }
      return false;
   }

   public boolean atomicityTestOne() throws Exception{
      System.out.println("Starting atomicity test 1");
      int custNum = 1;
      // Find a customer without bad credit to place an order with
      CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
      OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
      while(!cs.checkCustomerCredit(new Integer(custNum), new BigDecimal("200000.00"))){
         custNum++;
      }
      Integer custID = new Integer(custNum);

      // Create shopping cart to use while placing order
      ShoppingCart sc = new ShoppingCart();
      ItemBrowserSes ibs = (ItemBrowserSes)SpecBeanFactory.createItemBrowserSes();
      Collection itemsColl = ibs.getItems(null, 0);
      Iterator itr = itemsColl.iterator();
      ItemsDataBean itemToAddToCart = (ItemsDataBean)itr.next();
      ShoppingCartDataBean scdb = new ShoppingCartDataBean();
      scdb.setCartID(custID);
      scdb.setDescription(itemToAddToCart.getDescription());
      scdb.setDiscount(itemToAddToCart.getDiscount());
      scdb.setItemID(itemToAddToCart.getId());
      scdb.setMSRP(itemToAddToCart.getPrice());
      scdb.setQuantity(1);
      scdb.setVehicle(itemToAddToCart.getName());
      sc.addItem(scdb);

      // Retrieve out all information needed to compare before and after states
      Integer orderCountInit = os.getOrderCount(custID);
      Collection hColl = this.getHoldings(custID);
      int numOfHoldingsInit = hColl.size();
      BigDecimal balanceInit = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();

      // Drive the actual Order
      try{
         this.doPurchase(custID, sc, false, true);
      }catch(SPECjAppServerException e){
         e.printStackTrace();
      }

      // Retrieve out all information needed to compare before and after states
      Integer orderCountAfter = os.getOrderCount(custID);
      hColl = this.getHoldings(custID);
      int numOfHoldingsAfter = hColl.size();
      BigDecimal balanceAfter = ((CustomerDataBean)cs.getCustomerInfo(custID)).getBalance();

      // Remove all session beans used
      SpecBeanFactory.destroyCustomerSes(cs);
      SpecBeanFactory.destroyItemBrowserSes(ibs);
      SpecBeanFactory.destroyOrderSes(os);
      System.out.println("Ending atomicity test 1");
      if((orderCountInit.intValue()==orderCountAfter.intValue())&&(0==balanceAfter.compareTo(balanceInit))&&(numOfHoldingsInit==numOfHoldingsAfter)){
         return true;
      }
      return false;
   }


    /**
     * Sell a vehicle and removed the vehicle and quantity for the given dealership.
     * Given a vehicle, remove the inventory, credit the dealerships account,
     * and remove the vehicles from the dealership's portfolio.
     *
     * @param userID the customer requesting the sell
     * @param holdingID the vehicle to be sold
     * @return boolean indicating if the sale occured without error
     */

    //public boolean sell(Integer userID, String holdingID, int quantity)
   public boolean sell(Integer userID, Integer holdingID, boolean isAtomicityTest)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.sell() ");
        }

        CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
      boolean sold = cs.sellInventory(userID, holdingID, isAtomicityTest);
        SpecBeanFactory.destroyCustomerSes(cs);
        return sold;
    }

    /**
     * Cancel an open order and remove the order from the dealers list of
     * orders to be processed in the future.
     * Given a orderID cancel the customers order.
     *
     * @param userID the customer requesting the sell
     * @return boolean indicating if cancelling the order occured without error
     */

    public boolean cancel(int orderID)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.cancel() ");
        }

        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        boolean cancelSucess = os.cancelOrder(orderID);
        SpecBeanFactory.destroyOrderSes(os);
        return cancelSucess;
    }

    /**
      * Get the collection of all open orders for a given account
      *
      * @param userID the customer account to retrieve orders for
      * @return Collection OrderDataBeans providing detailed order information
      */
    public Collection getOpenOrders(Integer userID)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getOpenOrders() ");
        }

        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        Collection openOrders = os.getOpenOrders(userID);
        SpecBeanFactory.destroyOrderSes(os);
        return openOrders;
    }

    /**
      * Gets a list of the current vehicles either specified in the search or
      * instead just the generic search of all vehicle.  Displays them 10 at a time
      *
      * @param vehciles - an arraylist containing all vehicles to search for in the manufacturers inventory
      * @param session - the http session that is used to hold a reference to the stateful session bean containing the inventory
      * @param browse - an indicator to the application on which direction the dealer want to browse the inventory
      * @param category - indicates category of vehicles to browse in or purchase from
      * @return the VehicleSearch - a wrapper object containing all the vehcile to display on the current page
      */

    public VehicleSearch getVehicleQuotes(java.util.ArrayList vehicles, HttpSession session,
                                          String browse, int category)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getVehicleQuotes() ");
        }
        // check if we have a reference to the SF bean

        ItemBrowserSes ibs = (ItemBrowserSes) session.getAttribute("ibs");
        if (ibs == null)
        {
           ibs =  (ItemBrowserSes) SpecBeanFactory.createItemBrowserSes();
           session.setAttribute("ibs", ibs);
        }
        VehicleSearch vs = new VehicleSearch();
        if( browse == null ) {
            vs.vehicles = ibs.getItems(vehicles, category);
        } else if( browse.equalsIgnoreCase("bkwd") ) {
            vs.vehicles = ibs.browseReverse();
        } else if( browse.equalsIgnoreCase("fwd") ) {
            vs.vehicles = ibs.browseForward();
        } else {
            vs.vehicles = ibs.getItems(vehicles, category);
        }
        vs.min = ibs.getCurrentMin();
        vs.max = ibs.getCurrentMax();
        vs.total = ibs.getTotalItems();
        return vs;
    }

    /**
      * Return the dealerships current inventory in stock given a userID
      * as a collection of DataBeans
      *
      * @param userID the customer requesting the portfolio
      * @return Collection of the users portfolio of stock holdings
      */

    public Collection getHoldings(Integer userID)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getHoldings() ");
        }
        CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
        Collection inventory = cs.getInventory((int)userID);
        SpecBeanFactory.destroyCustomerSes(cs);
        return inventory;
    }

    /**
      * Return the a hashmap containing all the items for sale by the manufacturor
      *
      * @param inventory - the inventory for which we wish to get the cooresponding items
      * @param orders - the orders for which we wish to get the corresponding items
      * @return Collection of the users portfolio of stock holdings
      */

    public HashMap getItemDataBeans(Collection inventory, Collection orders) throws Exception{
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getItemDataBeans<inventory>,<orders>() ");
        }
        HashMap items = new HashMap(inventory.size()+orders.size());
        Iterator itr = inventory.iterator();
        // Get all the vehicle information about the vehicles in inventory
        while( itr.hasNext() ) {
            CustomerInventoryDataBean cidb = (CustomerInventoryDataBean)itr.next();
            items.put(cidb.getVehicle(), null);
        }
        // Get all the vehicle information about the open orders
        itr = orders.iterator();
        while( itr.hasNext() ) {
            OrderDataBean odb = (OrderDataBean) itr.next();
            Iterator olItr = odb.getOrderLines().iterator();
            while( olItr.hasNext() ) {
                OrderLineDataBean oldb = (OrderLineDataBean) olItr.next();
                items.put(oldb.getItemID(), null);
            }
        }
        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        items = (HashMap)os.getItemInfo(items);
        SpecBeanFactory.destroyOrderSes(os);
        return items;
    }

    /**
      * Return the a hashmap containing items for sale by the manufacturor
      *
      * @param OrderDataBean from the dealership used to find the corresponding items
      * @return HashMap of the items
      */

    public HashMap getItemDataBeans(OrderDataBean odb) throws Exception{

        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getItemDataBeans<OrderDataBean>() ");
        }
        HashMap items = new HashMap();
        Iterator olItr = odb.getOrderLines().iterator();
        while( olItr.hasNext() ) {
            OrderLineDataBean oldb = (OrderLineDataBean) olItr.next();
            String vehicleID = oldb.getItemID();
            items.put(vehicleID, null);
        }
        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        items = (HashMap)os.getItemInfo(items);
        SpecBeanFactory.destroyOrderSes(os);
        return items;
    }

    /**
      * Return the a collection containing all the items for sale by the manufacturor
      *
      * @param inventory - the inventory for which we wish to get the cooresponding items
      * @return Collection of the users portfolio of stock holdings
      */

    public Collection getItemDataBeans(Collection inventory) throws Exception{
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getItemDataBeans<inventory>() ");
        }
        Iterator itr = inventory.iterator();
        HashMap items = new HashMap();
        while( itr.hasNext() ) {
            CustomerInventoryDataBean cidb = (CustomerInventoryDataBean)itr.next();
            items.put(cidb.getVehicle(), null);
        }
        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        items = (HashMap) os.getItemInfo(items);
        SpecBeanFactory.destroyOrderSes(os);
        return items.values();
    }

    public ItemsDataBean getItemInfo(String itemID) throws Exception{
      OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
      HashMap items = new HashMap();
      items.put(itemID, null);
      items = (HashMap) os.getItemInfo(items);
      SpecBeanFactory.destroyOrderSes(os);
       return (ItemsDataBean)items.get(itemID);
    }

   public void updateItemPrice(String itemID, BigDecimal newPrice) throws Exception {
//      OrderAuditSes oas = (OrderAuditSes) SpecBeanFactory.createOrderAuditSes();
//      oas.updateItemPrice(itemID, newPrice);
//      SpecBeanFactory.destroyOrderAuditSes(oas);
   }

    /**
     * Return an CustomerDataBean object for userID describing the dealerships account
     *
     * @param userID the dealership userID to lookup
     * @return dealerships CustomerDataBean
     */

    public CustomerDataBean getCustomerData(Integer userID)
    throws javax.ejb.FinderException, Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.getCustomerData() ");
        }
        CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
        CustomerDataBean cdb = cs.getCustomerInfo(userID);
        SpecBeanFactory.destroyCustomerSes(cs);
        return cdb;
    }


    /**
     * Purchase the items currently in the shopping cart
     *
     * @param userID the customer who wishes to purchase
     * @param sc the shopping cart that contains the items the dealership wishes to purchase
     * @return OrderDataBean showing the information about the placed order
     */

    public ArrayList doPurchase(Integer userID, ShoppingCart sc, boolean deferred, boolean atomicityTest)
    throws InsufficientCreditException, Exception{
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.doPurchase() ");
        }
        OrderSes os = (OrderSes)SpecBeanFactory.createOrderSes();
        ArrayList objWrapper = (ArrayList)os.newOrder(userID, sc, deferred, atomicityTest);
        SpecBeanFactory.destroyOrderSes(os);
        return objWrapper;
    }


    /**
     * Attempt to authenticate and login a user
     *
     * @param userID the customer to login
     * @return User account data in AccountDataBean
     */

    public boolean login(Integer userID)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.login() ");
        }
        CustomerSes cs = (CustomerSes)SpecBeanFactory.createCustomerSes();
        boolean isValid = cs.validateCustomer(userID);
        SpecBeanFactory.destroyCustomerSes(cs);
        return isValid;
    }

    /**
     * Logout the given user
     *
     * @param userID the customer to logout
     * @return the login status
     */

    public boolean logout(HttpSession session)
    throws Exception
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAction.logout() ");
        }
        ItemBrowserSes ibs = (ItemBrowserSes) session.getAttribute("ibs");
        if (ibs != null)
        {
            session.removeAttribute("ibs");
            ibs.removeBean();
//           ((EJBObject)ibs).remove();
           // Todo: throws NullPointerException
        }
        return true;
    }

    /**
     * Schedule A Work Order
     *
     * @param assemblyId    Assembly Id
     * @param qty           Original Qty
     * @param dueDate       Date when order is due
     * @return woID         Work Order ID
     */

    public Integer scheduleWorkOrder(String assemblyId, int qty, java.sql.Date dueDate)
    throws Exception
    {
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        java.util.Calendar c = new java.util.GregorianCalendar();
        if (dueDate == null){
            c = null;
        } else {
            c.setTime(dueDate);
        }        
        int woID = wos.scheduleWorkOrder(assemblyId, qty, c);
        SpecBeanFactory.destroyWorkOrderSes(wos);
        return woID;
    }

    /**
     * Schedule A Work Order
     *
     * @param salesId       Sales order id
     * @param oLineId       Order Line ID
     * @param assemblyId    Assembly Id
     * @param qty           Original Qty
     * @param dueDate       Date when order is due
     * @return woID         Work Order ID
     */

    public Integer scheduleWorkOrder(int salesId, int oLineId, String assemblyId, int qty, java.sql.Date dueDate)
    throws Exception
    {
        Integer woID;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.scheduleWorkOrder()");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        java.util.Calendar c = new java.util.GregorianCalendar();
        if (dueDate == null){
            c = null;
        } else {
            c.setTime(dueDate);
        }        
        woID = wos.scheduleLargeWorkOrder(salesId, oLineId, assemblyId, qty, c);
      SpecBeanFactory.destroyWorkOrderSes(wos);
        return woID;

    }

    /**
     * Get Work Order Status
     *
     * @param woID the work order to update
     * @return status of operation
     */

    public String getWorkOrderStatus(Integer woID)
    throws Exception
    {

        String status;
        int status_val;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.getWorkOrderStatus(" + woID + ")");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        status_val = wos.getWorkOrderStatus(woID);
      SpecBeanFactory.destroyWorkOrderSes(wos);
        return WorkOrderStateConstants.woStates[status_val];
    }


    /**
     * Cancel A Work Order
     *
     * @param woID the work order to cancel
     * @return status of operation
     */

    public boolean cancelWorkOrder(Integer woID)
    throws Exception
    {
        int status_val;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.cancelWorkOrder(" + woID + ")");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        status_val = wos.getWorkOrderStatus(woID);
      boolean cancelled = false;
        if( status_val == WorkOrderStateConstants.STAGE1 ) {
            cancelled = wos.cancelWorkOrder(woID);
        }
        SpecBeanFactory.destroyWorkOrderSes(wos);
        return cancelled;
    }

    /**
     * Complete A Work Order
     *
     * @param woID the work order to complete
     * @return status of operation
     */

    public boolean completeWorkOrder(Integer woID)
    throws Exception
    {
        int status_val;
        boolean completeOk;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.completeWorkOrder(" + woID + ")");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        status_val = wos.getWorkOrderStatus(woID);

        if( status_val == WorkOrderStateConstants.STAGE1 ) {
            wos.updateWorkOrder(woID);
            wos.updateWorkOrder(woID);
        } else if( status_val == WorkOrderStateConstants.STAGE2 ) {
            wos.updateWorkOrder(woID);
        } else if( status_val != WorkOrderStateConstants.STAGE3 ) {
           SpecBeanFactory.destroyWorkOrderSes(wos);
            return false;
        }
      boolean completed = wos.completeWorkOrder(woID);
      SpecBeanFactory.destroyWorkOrderSes(wos);
      return completed;
    }

    /**
     * Update A Work Order
     *
     * @param woID the work order to update
     * @return status of operation
     */

    public boolean updateWorkOrder(Integer woID)
    throws Exception
    {
        int status_val;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.updateWorkOrder(" + woID + ")");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        status_val = wos.getWorkOrderStatus(woID);

        if( !((status_val == WorkOrderStateConstants.STAGE1)
              || (status_val == WorkOrderStateConstants.STAGE2)) ) {
            SpecBeanFactory.destroyWorkOrderSes(wos);
            return false;
        }

        wos.updateWorkOrder(woID);
      SpecBeanFactory.destroyWorkOrderSes(wos);
        return true;
    }

    /**
     * Find All Large Order
     *
     * @return vector of large orders
     */

    public Vector findLargeOrders()
    throws Exception
    {
        int status_val;
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.findLargeOrder()");
        }
        LargeOrderSes los = (LargeOrderSes)SpecBeanFactory.createLargeOrderSes();
        Vector largeOrders = los.findLargeOrders();
        SpecBeanFactory.destroyLargeOrderSes(los);
        return largeOrders;
    }

    /**
     * Get All Assembly
     *
     * @return vector of Assembly IDs
     */

    public Vector getAssemblyIds()
    throws Exception
    {
        if( SpecConfig.debugging ) {
            System.out.println("SpecAction.getAssemblyIds()");
        }
        WorkOrderSes wos = (WorkOrderSes)SpecBeanFactory.createWorkOrderSes();
        Vector assemblies = wos.getAssemblyIds();
        SpecBeanFactory.destroyWorkOrderSes(wos);
        return assemblies;
    }
}
