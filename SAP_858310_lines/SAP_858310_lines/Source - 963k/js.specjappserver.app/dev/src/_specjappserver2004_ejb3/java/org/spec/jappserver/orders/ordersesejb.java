/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2003/04/01  John Stecher, IBM         updated debugging
 *  2003/04/23  John Stecher, IBM         made newOrder return and ArrayList containing
 *                                        both the orderDataBean and the items HashMap to
 *                                        correctly make use of the session bean.
 *  2003/05/05  John Stecher, IBM         Made changes to catch noSuchObject exception on remove and display
 *                                        warning message.
 *  2003/05/06  John Stecher, IBM         Made changes to allow drive instead of application to determine
 *                                        if an order is to be deferred or added to inventory immediately
 *  2003/06/18  John Stecher, IBM         Updated to make better use of BigDecimal
 *  2003/06/18  John Stecher, IBM         Updated to implement changes suggested for best practices
 *  2003/06/23  John Stecher, IBM         Moved business logic for placing orders out to this session bean
 *                                        Credit checks now occur at this level and so do largeOrder placement
 *  2003/06/24  John Stecher, IBM         Added new getItemInfo method to take array of ids
 *  2003/07/22  Samuel Kounev, Darmstadt  Fixed bugs as reported in osgjava-5140 and osgjava-5154.
 *                                        Rewrote the newOrder method optimizing it for better performance.
 *  2003/08/28  John Stecher, IBM         Updated the session bean to store MSRP prices into the orderline.
 *  2003/08/28  John Stecher, IBM         Fixed bugs in computation of Discount on Order
 *  2003/09/04  John Stecher, IBM         Removed unnecessary calculations and fixed bug introduced in new order
 *                                        processing when changing the way the shopping cart is handled as it
 *                                        did not agree with the driver.
 *  2003/12/16  John Stecher, IBM         Added Atomicity Tests
 *  2003/01/08  John Stecher, IBM         Changed code to eliminate unused objects being passed into methods
 *  2004/01/25  John Stecher, IBM         Added code for cache consistancy test.
 *  2004/01/25  John Stecher, IBM         Updated cache consistency testing code
 *  2004/02/16  Samuel Kounev, Darmstadt  Removed unused import statements.
 *  2004/02/18  Samuel Kounev, Darmstadt  Moved method updateItemPrice to OrderAuditSes.
 *  2006/01/17  Bernhard Riedhofer, SAP   Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.orders;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.BigDecimalUtils;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.corp.CustomerSes;
import org.spec.jappserver.corp.CustomerSesEJB;
import org.spec.jappserver.orders.helper.InsufficientCreditException;
import org.spec.jappserver.orders.helper.ItemsDataBean;
import org.spec.jappserver.orders.helper.OrderDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;
import org.spec.jappserver.orders.helper.ShoppingCartDataBean;


import java.util.concurrent.LinkedBlockingQueue;
import javax.jms.MessageListener;
import org.spec.jappserver.async.AsyncExecutor;
import org.spec.jappserver.mfg.LargeOrderMDB;
import org.spec.jappserver.async.ObjectMessageImpl;
import org.spec.jappserver.async.TextMessageImpl;

/**
 * The OrderSessionBean is a wrapper for the Order and Orderline entity beans.
 * The session bean is what is accessed by the OrderEntry application. This
 * bean also implements the getCustStatus method to retrieve all orders
 * belonging to a particular customer.
 */
@Stateless (name="OrderSes")
public class OrderSesEJB implements OrderSes, OrderSesLocal
{
   @PersistenceContext(unitName = "Orders")
   protected EntityManager em;

   @Resource
   protected SessionContext ctx;

   @Resource
   protected int debuglevel = 0;
   Object debugSemaphore = new Object();
   Debug debug = null;

   @Resource (mappedName="jms/QueueConnectionFactory")
   protected QueueConnectionFactory queueConnectionFactory;
   
   @Resource (mappedName="jms/LargeOrderQueue")
   protected Queue largeOrderQueue;

   @EJB(
         name="org.spec.jappserver.corp.CustomerSes",
         beanInterface=CustomerSes.class,
         beanName="CustomerSes"
         )
   protected CustomerSes cSes;

   private static final ConcurrentHashMap<String, Item> itemCache = new ConcurrentHashMap<String, Item>(
         (int)(1.3 * 100 * Config.txRate));
   
      
   AsyncExecutor largeOrderExec ; 
   //static LinkedBlockingQueue<MessageListener>  loMDB_Q = new LinkedBlockingQueue<MessageListener>(10000); 
    @EJB(
         name="org.spec.jappserver.mfg.LargeOrderMDB",
         beanInterface=MessageListener.class,
         beanName="LargeOrderMDB_Ses"
         )
   protected MessageListener largeOrderMsgL; 
   
   
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

   public OrderSesEJB ()
   {
      if (debuglevel > 0) debug(3, "OrderSesEJB () default constructor");
   }

   private void lockOrder(Order order)
   {
      if (Config.isLockingUsed)
      {
         if (order != null)
         {
            em.lock(order, Config.lockMode);
         }
      }
   }
   
   // predcondition: orders ordered by order id in order to avoid deadlocks
   private void lockOrders(List<Order> orders)
   {
      if (Config.isLockingUsed)
      {
         Iterator<Order> itr = orders.iterator();
         while (itr.hasNext())
         {
            lockOrder(itr.next());
         }
      }
   }
   
   private Order findOrder(int id)
   {
      Order order = em.getReference(Order.class, id);
      lockOrder(order);
      return order;
   }
   
  /**
    * Enter an order for a customer
    *
    * @param customerId - id of customer
    * @param sc - shopping cart
    * @param deferred - deferred order
    * @param atomicityTest - flag for atomicity test
    * @throws RemoteException
    * @throws CreateException
    * @throws SPECjAppServerException
    * @throws RemoveException
    * @throws SPECjAppServerException
    */
   public List<Object> newOrder (int customerId, ShoppingCart sc, boolean deferred,
         boolean atomicityTest)
         throws RemoteException, CreateException, RemoveException, SPECjAppServerException
         // for EJB3: check exceptions in throws
   {
      if (debuglevel > 0) debug(3, "newOrder");

//      CustomerSes cSes = null;
//      try
//      {
//         InitialContext ctx = new InitialContext();
//         cSes = (CustomerSes) ctx.lookup(CustomerSes.class
//               .getName());
//      }
//      catch (NamingException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
      if (! cSes.checkCustomerCredit(customerId, sc.getTotal()))
      {
         throw new InsufficientCreditException(
               "Not enough credit for customer " + customerId + " for amount "
                     + sc.getTotal());
      }

      // generate order id
      Order order = new Order();
      em.persist(order);
      int orderId = order.getOrderId();

      // add order lines to order and calculate discount
      BigDecimal priceBeforeDiscount = BigDecimalUtils.zeroBigDec;
      BigDecimal priceAfterDiscount = BigDecimalUtils.zeroBigDec;
      for (int i = 0; i < sc.getItemCount(); i++)
      {
         ShoppingCartDataBean scdb = sc.getItem(i);
         OrderLine orderLine = new OrderLine();

         orderLine.setOrderId(orderId);
         // solution without helper table
//       orderLine.setOrder(order);

         orderLine.setOrderLineId(i + 1);
         orderLine.setQuantity(scdb.getQuantity());
         orderLine.setItemId(scdb.getItemID());
         orderLine.setTotalValue(BigDecimalUtils.round(scdb.getTotalCost()));
         orderLine.setMsrpAtPurchase(BigDecimalUtils.round(scdb.getMSRP()));
         if (deferred || orderLine.getQuantity() > 20)
         {
            orderLine.setOlineStatus(1);
            orderLine.setShipDate(null);
         }
         else
         {
            orderLine.setOlineStatus(3);
            orderLine.setShipDate(Util.getCurrentDateRoundToDay());
         }
         order.addOrderLine(orderLine);

         priceBeforeDiscount = priceBeforeDiscount.add(scdb.getMSRP().multiply(
               new BigDecimal(scdb.getQuantity())));
         priceAfterDiscount = priceAfterDiscount.add(scdb.getTotalCost());
      }

      // set order properties
      order.setCustomerId(customerId);
      order.setEntryDate(new Timestamp(System.currentTimeMillis()));
      order.setTotal(BigDecimalUtils.round(priceAfterDiscount));
      BigDecimal discount = (BigDecimalUtils.oneBigDec
            .subtract( (priceAfterDiscount.divide(priceBeforeDiscount, 8,
                  BigDecimal.ROUND_DOWN)))
            .multiply(BigDecimalUtils.onehundredBigDec)).setScale(2,
            BigDecimal.ROUND_HALF_DOWN);
      order.setDiscount(BigDecimalUtils.round(discount));
      if (deferred)
      {
         order.setShipDate(null);
         order.setOrderStatus(1);
      }
      else
      {
         order.setShipDate(Util.getCurrentDateRoundToDay());
         order.setOrderStatus(3);
      }

      // do large orders
      if (!deferred)
      {
         int removed = 0;
         ArrayList<OrderLine> largeOrderLines = new ArrayList<OrderLine>();
         for (OrderLine orderLine : order.getOrderLines())
         {
            if (orderLine.getQuantity() > 20)
            {
               largeOrderLines.add(orderLine);
               removed++; // order line id starts with 1
               sc.removeItem(orderLine.getOrderLineId() - removed);
            }
         }
         if (largeOrderLines.size() != 0)
         {
            doLargeOrders(largeOrderLines, orderId);
         }

         cSes.addInventory(order.getCustomerId(), sc, priceAfterDiscount,
               atomicityTest);
      }

      // for atomicity tests
      if (debuglevel > 0)
      {
         debug(4, "Atomicity Test (1,2,3): Order Id: "
               + orderId);
         debug.println(4, "Atomicity Test 3: Order Line Id: "
               + order.getOrderLinesCount());
      }

      // prepare result parameter
      HashMap<String, ItemsDataBean> items = new HashMap<String, ItemsDataBean>();
      for (OrderLine orderLine : order.getOrderLines())
      {
         items.put(orderLine.getItemId(), null);
      }
      ArrayList<Object> returnWrapper = new ArrayList<Object>(2);
      returnWrapper.add(order.getDataBean());
      returnWrapper.add(getItemInfo(items));
      return returnWrapper;
   }

  /**
    * Cancel an existing customer order
    *
    * @param orderId - id of order being changed
    */
   @SuppressWarnings("boxing")
   public boolean cancelOrder (int orderId)
   {
      if (debuglevel > 0) debug(3, "cancelOrder");

      try
      {
         Order order = findOrder(orderId);
         em.remove(order);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      return true;
   }

   /**
    * Get number of orders placed by a specific customer.
    * Only used by atomicity tests.
    *
    * @param userId - id of cusomter
    * @return number of orders
    */
   @SuppressWarnings({"boxing","unchecked"})
   public int getOrderCount (int userId)
   {
      if (debuglevel > 0) debug(3, "getOrderCount");

      Query query = em.createNamedQuery("getOrdersByCustomerIdOrderedByOrderId")
            .setParameter(1, userId);
      Config.setFlushMode(query);
      List<Order> orders = query.getResultList();
      lockOrders(orders); // lock order due to possible side effects
      return orders.size();
   }

   /**
    * Get collection with information for the user
    * about all open orders still remaining, thus
    * allowing them to change and orders vehicle quantity.
    *
    * @param userId - id of cusomter
    * @return collection with order data beans
    */
   @SuppressWarnings({"boxing","unchecked"})
   public Collection getOpenOrders (int userId)
   {
      if (debuglevel > 0) debug(3, "getOpenOrders");

      Query query = em.createNamedQuery("getOpenOrdersByCustomerIdOrderedByOrderId")
            .setParameter(1, userId);
      Config.setFlushMode(query);
      List<Order> orders = (List<Order>) query.getResultList();
      lockOrders(orders);

      // Here we must search and make sure that we are only maintaining a
      // order history 10 deep
      if (orders.size() > 10)
      {
         // Trim Open orders set to 6. This is to minimize this method being
         // called, and thus taking CPU cylces
         List<Order> finalOrders = new ArrayList<Order>();
         Iterator<Order> itr = orders.iterator();
         while (itr.hasNext())
         {
            Order order = itr.next();
            if (finalOrders.size() < 6)
            {
               finalOrders.add(order);
            }
            else
            {
               // remove in order of order id in order to avoid deadlocks
               em.remove(order);
            }
         }
         orders = finalOrders;
      }
      
      Set<OrderDataBean> ordersSet = new TreeSet<OrderDataBean>();
      Iterator<Order> itr = orders.iterator();
      while (itr.hasNext())
      {
         ordersSet.add(itr.next().getDataBean());
      }
      return ordersSet;
   }

   /**
    * Creates large orders for large order lines.
    * A item quantity of > 20 is considered a large order,
    * since the largest qty for a regular order is 20.
    *
    * @param largeOrderLines - id of cusomter
    * @param orderID - order id of large orders
    */
   protected void doLargeOrders (ArrayList<OrderLine> largeOrderLines, int orderID)
   {
      if (debuglevel > 0) debug(3, "doLargeOrders");

      QueueConnection con = null;
      QueueSession ses = null;
      try
      {
         // TODO: replace by EJB3 injection
         /*InitialContext ctx = new InitialContext();
         largeOrderQueue = (Queue) ctx.lookup("java:comp/env/jms/LargeOrderQueue");
         queueConnectionFactory = (QueueConnectionFactory) ctx
               .lookup("java:comp/env/jms/QueueConnectionFactory");*/
               
         if(Config.bypassJMS){
         	if(largeOrderExec==null){         	  
            	  largeOrderExec = AsyncExecutor.createAsyncExecutor(largeOrderMsgL);             	  
         	}
         	
         	for (OrderLine orderLine : largeOrderLines){               
         	   TextMessage tmsg = new TextMessageImpl();
         	   tmsg.setIntProperty("orderId", orderID);
                   tmsg.setIntProperty("oLineId", orderLine.getOrderLineId());
                   tmsg.setStringProperty("assemblyId", orderLine.getItemId());
                   tmsg.setShortProperty("qty", (short) orderLine.getQuantity());
                   tmsg.setLongProperty("dueDate", System.currentTimeMillis());
                   tmsg.setLongProperty("publishTime", System.currentTimeMillis());
                   tmsg.setText("LargeOrder: orderID=" + orderID + ", oLineId="
                     + orderLine.getOrderLineId());
                     
                   /*
                   LargeOrderMDB loMDB = (LargeOrderMDB)loMDB_Q.poll(); 
                   if(loMDB==null){
                      loMDB = new LargeOrderMDB(); 
                   }
                   */
                   
                   largeOrderExec.deliverMessage(tmsg);   
               }//for   
         }else{
               con = queueConnectionFactory.createQueueConnection();
               ses = con.createQueueSession(true, QueueSession.AUTO_ACKNOWLEDGE);
               QueueSender queueSender = ses.createSender(largeOrderQueue);
               for (OrderLine orderLine : largeOrderLines)
               {
                  TextMessage message = ses.createTextMessage();
                  message.setIntProperty("orderId", orderID);
                  message.setIntProperty("oLineId", orderLine.getOrderLineId());
                  message.setStringProperty("assemblyId", orderLine.getItemId());
                  message.setShortProperty("qty", (short) orderLine.getQuantity());
                  message.setLongProperty("dueDate", System.currentTimeMillis());
                  message.setLongProperty("publishTime", System.currentTimeMillis());
                  message.setText("LargeOrder: orderID=" + orderID + ", oLineId="
                        + orderLine.getOrderLineId());
//                  long time = System.currentTimeMillis();
                  queueSender.send(message);
//                  (new Sender()).send("ResponseTime.LargeOrderQueue", System.currentTimeMillis() - time);
              }
        }
      }
      // TODO: replace by JMSException
      catch (Exception e)
      {
         e.printStackTrace();
         if (debuglevel > 0)
         {
            debug(1, "Exception in create large order for " + orderID);
            debug(1, e.getMessage());
         }
         throw new EJBException(
               "Failure to send message to the LargeOrderQueue, ", e);
      }
      finally
      {
         try
         {
            if (con != null) con.close();
            if (ses != null) ses.close();
         }
         catch (JMSException e)
         {
            e.printStackTrace();
            debug.printStackTrace(e);
         }
      }
   }

   private Item getItem(String itemId)
   {
      Item result;
      if (Config.isItemsCacheUsed)
      {
         result = itemCache.get(itemId);
         if (result == null)
         {
            result = em.find(Item.class, itemId);
            itemCache.put(itemId, result);
         }
      }
      else
      {
         result = em.find(Item.class, itemId);
      }
      return result;
   }
   
   public HashMap<String, ItemsDataBean> getItemInfo (HashMap<String, ItemsDataBean> itemIds)
   {
      if (debuglevel > 0) debug(3, "getItemInfo");

      for (String itemId : itemIds.keySet())
      {
         itemIds.put(itemId, getItem(itemId).getDataBean());
      }
      return itemIds;
   }
}
