/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
 *  2003/04/24  John Stecher, IBM       Updated doPurchase to return a ArrayList because we
 *                                      are now using the session bean to return the ArrayList
 *                                      as a wrapper around the items, and the orderDataBean
 *  2003/05/01  John Stecher, IBM       Imported Russ Raymundo's mfg side of the benchmark for web base driving
 *  2003/05/06  John Stecher, IBM       Made changes to allow drive instead of application to determine
 *                                      if an order is to be deferred or added to inventory immediately
 *  2003/08/30  John Stecher, IBM       Updated for new sell functionality
 *  2003/11/25  Tom Daly    , Sun       Added category to getVehicleQuotes();
 *  2003/12/05  John Stecher, IBM       Added Atomicity Tests
 *  2003/01/08  John Stecher, IBM       Changed code to eliminate unused objects being passed into methods
 *  2004/01/25  John Stecher, IBM       Added code for cache consistancy test.
 */


package org.spec.jappserver.servlet.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.corp.helper.CustomerDataBean;
import org.spec.jappserver.orders.helper.InsufficientCreditException;
import org.spec.jappserver.orders.helper.ItemsDataBean;
import org.spec.jappserver.orders.helper.OrderDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;


/**
  * SpecServices interface specifies the business methods provided by the Spec online dealership application.
  * These business methods represent the features and operations that can be performed by dealerships 
  * such as login, logout, view the shopping cart, buy or sell a vehicle, etc.
  *
  */

public interface SpecServices {

	/**
	 * This test checks to see if the proper transaction atomicity levels are being upheld in transactions associated with the
	 * benchmark.  This test case drives placing an order for immediate insertion into the dealerships inventory.  An exception is
	 * raised after placing the order and while adding the inventory to the dealers inventory table.  This should cause the transaction
	 * changes to be removed from the database and all other items returned to how they existed before the transaction took place.
	 * This test case has three steps which are as follows to verify atomicity
	 * 
	 * 1.)  Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of 
	 * orders which have been placed for the dealer inside of the dealer domain.  These number are the initial metrics that the final
	 * test cases should line up with after rolling back the tran.
	 * 
	 * 2.)  Drives the above listed transction which causes a transaction rollback exception to occur
	 * 
	 * 3.)  Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of 
	 * orders which have been placed for the dealer inside of the dealer domain.  These number should equal those in step 1) for
	 * the test case to be sucessful and the transaction to have been atomic.
	 * 
	 * @return if the test passed or failed
	 * @throws Exception
	 */
	public boolean atomicityTestOne() throws Exception;
    
	/**
	 * This test transaction simply tests that the application server is working properly and that it is able to insert an order as in Atomicity
	 * test 1 but without causing the exception and have it show up in the database.
	 * 
	 * @return if the test passed or failed
	 * @throws Exception
	 */
	public boolean atomicityTestTwo() throws Exception;

	/**
	 * This test checks to see if the proper transaction atomicity levels are being upheld in transaction associated with the benchmark
	 * and specifically the messaging subsystem in this test case.  This test case drives placing a order which contains a large order and
	 * an item to be insert immediately into the dealerships inventory.  An exception is raised after placing the order and while adding the inventory to the dealers inventory 
	 * table.  This should cause the transaction changes to be removed from the database, messages removed from queue and all other items returned to how they existed 
	 * before the transaction took place.  This test case has three steps which are as follows to verify atomicity
	 *  
	 * 1.)  Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of 
	 * orders which have been placed for the dealer inside of the dealer domain.  Also the large order table is queried to check how 
	 * many large orders exist in the database before we drive the transaction.  These number are the initial metrics that the final
	 * test cases should line up with after rolling back the tran.
	 * 
	 * 2.)  Drives the above listed transction which causes a transaction rollback exception to occur
	 * 
	 * 3.)  Query database to check how many inventory items the dealership has, the dealerships account balance, and the number of 
	 * orders which have been placed for the dealer inside of the dealer domain.  Also query the large order table to check how many
	 * large orders there are in the table.  These number should equal those in step 1) for
	 * the test case to be sucessful and the transaction to have been atomic.
	 * 
	 * @return if the test passed or failed
	 * @throws Exception
	 */	
	public boolean atomicityTestThree() throws Exception;
	
	public ItemsDataBean getItemInfo(String itemID) throws Exception;
	
	public void updateItemPrice(String itemID, BigDecimal newPrice) throws Exception;

    /**
      * Get the collection of all open orders for a given account
      *
      * @param userID the customer account to retrieve orders for
      * @return Collection OrderDataBeans providing detailed order information
      */
    public Collection getOpenOrders(Integer userID) throws Exception;

    /**
      * Gets a list of the current vehicles either specified in the search or
      * instead just the generic search of all vehicle.  Displays them 10 at a time
      * 
      * @param vehciles - an arraylist containing all vehicles to search for in the manufacturers inventory
      * @param session - the http session that is used to hold a reference to the stateful session bean containing the inventory
      * @param browse - an indicator to the application on which direction the dealer want to browse the inventory
      * @return the VehicleSearch - a wrapper object containing all the vehcile to display on the current page
      */
    public VehicleSearch getVehicleQuotes(ArrayList vehicles, HttpSession session, String browse, int category) throws Exception;

    /**
      * Return the portfolio of stock holdings for the specified customer
      * as a collection of HoldingDataBeans
      *
      * @param userID the customer requesting the portfolio	 
      * @return Collection of the users portfolio of stock holdings
      */
    public Collection getHoldings(Integer userID) throws Exception;    

    /**
      * Return the a hashmap containing all the items for sale by the manufacturor
      *
      * @param inventory - the inventory for which we wish to get the cooresponding items
      * @param orders - the orders for which we wish to get the corresponding items	 
      * @return Collection of the users portfolio of stock holdings
      */
    public HashMap getItemDataBeans(Collection inventory, Collection orders) throws Exception;    

    /**
      * Return the a hashmap containing items for sale by the manufacturor
      *
      * @param OrderDataBean from the dealership used to find the corresponding items	 
      * @return HashMap of the items
      */
    public HashMap getItemDataBeans(OrderDataBean odb) throws Exception;    

    /**
      * Return the a collection containing all the items for sale by the manufacturor
      *
      * @param inventory - the inventory for which we wish to get the cooresponding items
      * @return Collection of the users portfolio of stock holdings
      */
    public Collection getItemDataBeans(Collection inventory) throws Exception;   

    /**
     * Cancel an open order and remove the order from the dealers list of
     * orders to be processed in the future.
     * Given a orderID cancel the customers order.
     *
     * @param userID the customer requesting the sell
     * @return boolean indicating if cancelling the order occured without error
     */
    public boolean cancel(int orderID) throws Exception;    

    /**
     * Sell a vehicle and removed the vehicle and quantity for the given dealership.
     * Given a vehicle, remove the inventory, credit the dealerships account,
     * and remove the vehicles from the dealership's portfolio.
     *
     * @param userID the customer requesting the sell
     * @param holdingID the vehicle to be sold
     * @return boolean indicating if the sale occured without error
     */
    public boolean sell(Integer userID, Integer holdingID, boolean isAtomicityTest) throws Exception;

    /**
     * Return an CustomerDataBean object for userID describing the dealerships account
     *
     * @param userID the dealership userID to lookup
     * @return dealerships CustomerDataBean
     */
    public CustomerDataBean getCustomerData(Integer userID) 
    throws javax.ejb.FinderException, Exception;     

    /**
     * Purchase the items currently in the shopping cart
     *
     * @param userID the customer who wishes to purchase
     * @param sc the shopping cart that contains the items the dealership wishes to purchase
     * @return ArrayList containing the orderDataBean and the items HashMap
     */
    public ArrayList doPurchase(Integer userID, ShoppingCart sc, boolean deferred, boolean atomicityTest) throws InsufficientCreditException, SPECjAppServerException, Exception;                                   

    /**
     * Attempt to authenticate and login a user
     *
     * @param userID the customer to login
     * @return User account data in AccountDataBean
     */
    public boolean login(Integer userID) throws Exception;                              

    /**
     * Logout the given user
     *
     * @param userID the customer to logout 
     * @return the login status
     */
    public boolean logout(HttpSession session) throws Exception; 

    /**
     * Schedule A Work Order
     *
     * @param assemblyId Assembly ID
     * @param qty Quantity
     * @param dueDate Due Date
     * @return status of operation
     */

    public Integer scheduleWorkOrder(String assemblyId, int qty, java.sql.Date dueDate) throws Exception;

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

    public Integer scheduleWorkOrder(int salesId, int oLineId, String assemblyId, int qty, java.sql.Date dueDate) throws Exception;

    /**
     * Get Work Order Status
     *
     * @param woID the work order to cancel
     * @return status of operation
     */

    public String getWorkOrderStatus(Integer woID) throws Exception;

    /**
     * Cancel A Work Order
     *
     * @param woID the work order to cancel
     * @return status of operation
     */

    public boolean cancelWorkOrder(Integer woID) throws Exception;

    /**
     * Complete A Work Order
     *
     * @param woID the work order to Complete
     * @return status of operation
     */

    public boolean completeWorkOrder(Integer woID) throws Exception;

    /**
     * Update A Work Order
     *
     * @param woID the work order to update
     * @return status of operation
     */

    public boolean updateWorkOrder(Integer woID) throws Exception;


    /**
    * Find All Large Order
    *
    * @return vector of large orders
     */

    public Vector findLargeOrders() throws Exception;

    /**
    * Get All Assembly
    *
    * @return vector of Assembly IDs
    */

    public Vector getAssemblyIds() throws Exception;
}   

