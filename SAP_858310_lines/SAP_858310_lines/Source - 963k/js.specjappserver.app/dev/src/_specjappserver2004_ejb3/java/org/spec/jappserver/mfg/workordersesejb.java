/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Ajay Mittal, SUN        Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/06/19  John Stecher, IBM       Added method to return all Assemblies for WebBased testing
 *  2003/06/28  John Stecher, IBM       Removed unnecessary imports
 *  2005/12/22  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.ComponentDemand;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.Util;

import java.util.concurrent.LinkedBlockingQueue;
import javax.jms.MessageListener;
import org.spec.jappserver.async.AsyncExecutor;
import org.spec.jappserver.orders.FulfillOrderMDB;
import org.spec.jappserver.supplier.buyermdb.ejb.BuyerMDB;
import org.spec.jappserver.async.ObjectMessageImpl;
import org.spec.jappserver.async.TextMessageImpl;


/**
 * Bean implementation class for WorkOrderSes
 */
@WebService
@Stateless (name="WorkOrderSes")
public class WorkOrderSesEJB implements WorkOrderSes, WorkOrderSesLocal
{
   @PersistenceContext (unitName="Mfg")
   protected EntityManager em;


   @Resource (mappedName="jms/QueueConnectionFactory")
   protected QueueConnectionFactory queueConnectionFactory;

   @Resource (mappedName="jms/BuyerQueue")
   protected Queue buyerQueue;

   @Resource (mappedName="jms/FulfillOrderQueue")
   protected Queue fulfillOrderQueue;

   @Resource
   protected int debuglevel = 0;

   protected Object debugSemaphore = new Object();
   protected Debug debug = null;
   
   
   AsyncExecutor fulfillOrderExec ; 
   AsyncExecutor buyerExec ; 
   //static LinkedBlockingQueue<MessageListener>  fulfillOrderMDB_Q = new LinkedBlockingQueue<MessageListener>(10000); 
   //static LinkedBlockingQueue<MessageListener> buyerMDB_Q  = new LinkedBlockingQueue<MessageListener>(10000); 

   @EJB(
         name="org.spec.jappserver.orders.FulfillOrderMDB",
         beanInterface=MessageListener.class,
         beanName="FulfillOrderMDB_Ses"
         )
   protected MessageListener fulfillOrderMsgL; 
   
   @EJB(
         name="org.spec.jappserver.supplier.buyermdb.BuyerMDB",
         beanInterface=MessageListener.class,
         beanName="BuyerMDB_Ses"
         )
   protected MessageListener buyerMsgL; 
         
         

   private void debug(int level, String str)
   {
      synchronized (debugSemaphore)
      {
         if (debug == null)
         {
            if (debuglevel > 0)
            {
               debug = new DebugPrint(debuglevel, WorkOrderSesEJB.class);
            }
            else
            {
               debug = new Debug();
            }
         }
      }
      debug.println(level, str);
   }

   private void lockInventory(Inventory inventory)
   {      
      if (Config.isLockingUsed)
      {
         if (inventory != null)
         {
            em.lock(inventory, Config.lockMode);
         }
      }
   }

   private Inventory findInventory(int iid) {
      Inventory inventory = null;
//      if (Config.isPessimisticLockingUsed) { // TODO: implementation not finished yet
      if (false) {
         //query is overridden with native named query which executes "select ... for update"
         Query query = em.createNamedQuery("getInventory").setParameter(1, iid);
         Config.setFlushMode(query);
         inventory = (Inventory) query.getSingleResult();
      } else {
         inventory = em.getReference(Inventory.class, iid);
         lockInventory(inventory);
      }
      return inventory;
   }
   
   private WorkOrder findWorkOrder(int wid)
   {
      WorkOrder workOrder = null;
//    if (Config.isPessimisticLockingUsed) { // TODO: implementation not finished yet
      if (false) {
          //query is overridden with native named query which executes "select ... for update"
         Query query = em.createNamedQuery("getWorkOrder").setParameter(1, wid);
         Config.setFlushMode(query);
         workOrder = (WorkOrder) query.getSingleResult();
      } else {
         workOrder = em.getReference(WorkOrder.class, wid);
         if (Config.isLockingUsed) {
             if (workOrder != null) {
                 em.lock(workOrder, Config.lockMode);
             }
         }
      }
      return workOrder;
   }
   
   /**
    * Method to schedule a work order.
    * @param assemblyId    Assembly Id
    * @param qty           Original Qty
    * @param dueDate       Date when order is due
    * @return Workorder id
    */
   @WebMethod
   public int scheduleWorkOrder (String assemblyId, int qty, java.util.Calendar dueDate)
   {
      if (debuglevel > 0) debug(3, "scheduleWorkOrder");

      java.sql.Date dueDateSQL = null;
      if (dueDate != null)
      {
    	  dueDateSQL = new java.sql.Date(dueDate.getTimeInMillis());
      }
      WorkOrder workOrder = new WorkOrder(qty, dueDateSQL);
      return schedule(assemblyId, workOrder);
   }

   /**
    * Method to schedule a work order.
    * @param salesId       Sales order id
    * @param oLineId       Order Line ID
    * @param assemblyId    Assembly Id
    * @param qty           Original Qty
    * @param dueDate       Date when order is due
    * @return Workorder id
    */
   @WebMethod
   @SuppressWarnings({"unchecked","boxing"})
   public int scheduleLargeWorkOrder (int salesId, int oLineId, String assemblyId,
         int qty, java.util.Calendar dueDate)
   {
      if (debuglevel > 0) debug(3, "scheduleWorkOrder");

      Query query = em.createNamedQuery("getLargeOrdersBySalesOrderIdAndOrderLineNumber")
            .setParameter(1, salesId)
            .setParameter(2, oLineId);
      Config.setFlushMode(query);
      List<LargeOrder> los = (List<LargeOrder>) query.getResultList();

      if (los.size() != 1)
      {
         throw new EJBException("Cannot find LargeOrder for Order: " + salesId
               + " Line: " + oLineId);
      }
      em.remove(los.get(0));

      WorkOrder workOrder = new WorkOrder(salesId, oLineId, qty, new java.sql.Date(dueDate.getTimeInMillis()));
      return schedule(assemblyId, workOrder);
    }

   /**
    * Update status of a workorder
    * @param wid WorkOrder ID
    */
   @WebMethod
   @SuppressWarnings("boxing")
   public void updateWorkOrder(int wid)
   {
      if (debuglevel > 0) debug(3, "updateWorkOrder");

      WorkOrder workOrder = findWorkOrder(wid);
      if (workOrder == null)
      {
         if (debuglevel > 0) debug(1, "Could not find bean for wid=" + wid);
         throw new EJBException("Could not find bean for id=  " + wid);
      }

      try
      {
         update(workOrder);
      }
      catch (IllegalStateException e)
      {
         if (debuglevel > 0) debug(1, "can not update ");
         e.printStackTrace();
         throw new EJBException("can not update " + e);
      }
   }

   /**
     * Complete work order.
     * Transfer completed portion to inventory.
     * @param  wid     WorkOrder ID
     * @return boolean false if failed to complete
     */
   @WebMethod
   @SuppressWarnings("boxing")
   public boolean completeWorkOrder(int wid)
   {
      if (debuglevel > 0) debug(3, "completeWorkOrder");

      WorkOrder workOrder = findWorkOrder(wid);
      if (workOrder == null)
      {
         if (debuglevel > 0) debug(1, "Could not find bean for wid=" + wid);
         return false;
      }

      try
      {
         finish(workOrder);
      }
      catch (IllegalStateException e)
      {
         e.printStackTrace();
         if (debuglevel > 0) debug(1, "Illegal State exception " + e);
         return false;
      }

      return true;
   }

   /**
    * Cancel work order Transfer completed portion to inventory.
    * Abort remaining work order.
    * @param  wid     WorkOrder ID
    * @return boolean false if failed to complete
    */
   @WebMethod(exclude = true)
   @SuppressWarnings("boxing")
   public boolean cancelWorkOrder (int wid)
   {
      if (debuglevel > 0) debug(3, "cancelWorkOrder");

      WorkOrder workOrder = findWorkOrder(wid);
      if (workOrder == null)
      {
         if (debuglevel > 0) debug(1, "Could not find bean for wid=" + wid);
         return false;
      }

      try
      {
         boolean bret = cancel(workOrder);
         em.remove(workOrder);
         return bret;
      }
      catch (IllegalStateException e)
      {
         e.printStackTrace();
         if (debuglevel > 0) debug(0, "Illegal State exception " + e);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         if (debuglevel > 0) debug(0, "Exception " + e);
      }
      return false;
   }

   /**
    * Get completed Qty in the workorder
    * @param  wid WorkOrder ID
    * @return int status
    */
   @WebMethod(exclude = true)
   @SuppressWarnings("boxing")
   public int getWorkOrderCompletedQty(int wid)
   {
      if (debuglevel > 0) debug(3, "getWorkOrderCompletedQty");

      WorkOrder workOrder = findWorkOrder(wid);
      if (workOrder == null)
      {
         if (debuglevel > 0) debug(1, "Could not find bean for wid=" + wid);
         throw new EJBException("Could not find bean for id=  " + wid);
      }
      return(workOrder.getCompQty());
    }

   /**
    * Get status of a workorder
    * @param  wid WorkOrder ID
    * @return int status
    */
   @WebMethod(exclude = true)
   @SuppressWarnings("boxing")
   public int getWorkOrderStatus(int wid)
   {
      if (debuglevel > 0) debug(3, "getWorkOrderStatus");

      WorkOrder workOrder = findWorkOrder(wid);
      if (workOrder == null)
      {
         if (debuglevel > 0) debug(1, "Could not find bean for wid=" + wid);
         throw new EJBException("Could not find bean for id=  " + wid);
      }
      return(workOrder.getStatus());
   }

   /**
    * Get All Assembly
    * @return vector of Assembly IDs
    */
   @WebMethod(exclude = true)
   @SuppressWarnings("unchecked")
   public Vector<String> getAssemblyIds()
   {
      if (debuglevel > 0) debug(3, "WorkOrderSes.getAssemblyIds()");

      Query query = em.createNamedQuery("getAllAssemblies");
      Config.setFlushMode(query);
      List<Assembly> assemblies = (List<Assembly>) query.getResultList();

      // TODO: in case of inheritance delete "where purchased = 1"
      Vector<String> assemblyIds = new Vector<String>();;
      for (Assembly a : assemblies)
      {
         assemblyIds.add(a.getId());
      }
      return assemblyIds;
   }

   private int schedule(String assemblyId, WorkOrder workOrder)
   {
      Assembly assembly = em.getReference(Assembly.class, assemblyId);
      workOrder.setAssembly(assembly);

      if (debuglevel > 0) debug(3, "process");
      process(workOrder);
      if (debuglevel > 0) debug(3, "Stage 1 done");

      em.persist(workOrder);
      return workOrder.getId();
   }

   private void process(WorkOrder workOrder)
   {
      if (debuglevel > 0) debug(3, "in process");

      if (Config.isPreloadEntities) {
          Assembly assembly = workOrder.getAssembly();

          Query query = em.createNamedQuery("preloadInventories");
          Config.setFlushMode(query);
          List<Inventory> inventories = query.setParameter("ass", assembly).getResultList();

          query = em.createNamedQuery("preloadComponents");
          Config.setFlushMode(query);
          List<Component> components = query.setParameter("ass", assembly).getResultList();
      }

      Set<Bom> bomSet = workOrder.getAssembly().getBOMs();

      // sort boms in order to avoid deadlock
      Bom[] bomArray = new Bom[bomSet.size()];
      for (Bom bom : bomSet)
      {
         bomArray[bom.getLineNo() - 1] = bom;
      }

      // From the boms list, take the number of components off the component
      // inventory.
      Vector<ComponentDemand> componentsToPurchase
            = new Vector<ComponentDemand>();
      for (Bom item : bomArray)
      {
         String cid = item.getComponentId();
         Component cItem = item.getComponent();
         // Based on qty from boms, multiply with requested qty (origQty)
         int qtyOff = item.getQty() * workOrder.getOrigQty();
         // Check if we need to order
         Inventory inv = cItem.getInventory();
         lockInventory(inv);
         int qtyRequired = 0;
         int numAvail = inv.getOnHand() + inv.getOrdered() - qtyOff;
         if (numAvail <= cItem.getLomark())
         {
            qtyRequired = cItem.getHimark() - numAvail;
         }
         if (qtyRequired > 0)
         {
            if (debuglevel > 0) debug(3, "Purchase needed, contacting supplier");
            cItem.getInventory().addOrdered(qtyRequired);
            componentsToPurchase.add(new ComponentDemand(cid, qtyRequired));
         }
         // Remove from inventory
         cItem.getInventory().take(qtyOff);
         if (debuglevel > 0) debug(3, "Obtain inventory");
      }

      if (! componentsToPurchase.isEmpty())
      {
         if (debuglevel > 0) debug(3, "Sending order");
         QueueConnection con = null;
         QueueSession ses = null;
         try
         {
            // TODO: replace by EJB3 injection
            /*InitialContext ctx = new InitialContext();
            Queue buyerQueue = (Queue) ctx.lookup("java:comp/env/jms/BuyerQueue");
            queueConnectionFactory = (QueueConnectionFactory) ctx
                  .lookup("java:comp/env/jms/QueueConnectionFactory");*/
                  

            if(Config.bypassJMS){      
            	if(buyerExec==null){         	  
            	     buyerExec = AsyncExecutor.createAsyncExecutor(buyerMsgL);             	  
         	}
         	
         	ObjectMessage objm = new ObjectMessageImpl();
         	objm.setIntProperty("siteID", 1);
                objm.setIntProperty("woID", workOrder.getId());
                objm.setLongProperty("publishTime", System.currentTimeMillis());
                objm.setObject(componentsToPurchase);                
                buyerExec.deliverMessage(objm);
            } else {
                con = queueConnectionFactory.createQueueConnection();
                ses = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
                QueueSender queueSender = ses.createSender(buyerQueue);
                ObjectMessage message = ses.createObjectMessage();
                message.setIntProperty("siteID", 1);
                message.setIntProperty("woID", workOrder.getId());
                message.setLongProperty("publishTime", System.currentTimeMillis());
                message.setObject(componentsToPurchase);
//                long time = System.currentTimeMillis();
                queueSender.send(message);
//                (new Sender()).send("ResponseTime.BuyerQueue", System.currentTimeMillis() - time);
           }
         }
         // TODO: replace by JMSException
         catch (Exception e)
         {
            e.printStackTrace();
            if (debuglevel > 0)
            {
               debug(1, "Exception in process order for " + workOrder.getId());
               debug(1, e.getMessage());
            }
            throw new EJBException(
                  "Failure to send message to the BuyerQueue, ", e);
         }
         finally
         {
            try
            {
               if(con != null) con.close();
               if(ses != null) ses.close();
            }
            catch(JMSException e)
            {
               e.printStackTrace();
               if (debuglevel > 0) debug.printStackTrace(e);
            }
         }
      }

      if (debuglevel > 0) debug(3, "Processed state");
      workOrder.process();
      if (debuglevel > 0) debug(3, "Obtained status");

      if (debuglevel > 0) debug(3, "Order sent");
   }

   /**
    *  Modifies the state of the workOrder to the nextState of process.
    */
   @WebMethod(exclude = true)
   public void update(WorkOrder workOrder)
   {
      workOrder.nextState();
      if (debuglevel > 0) debug(3, "update Status " + workOrder.getStatus());
   }

   /**
    *  When workOrder is finished, it will add the new object to
    *  inventory and modify the state of workOrder to finished.
    */
   @WebMethod(exclude = true)
   public void finish(WorkOrder workOrder)
   {
      if (debuglevel > 0) debug(3, "finish Status " + workOrder.getStatus());

      //If large order -> notify the customer domain
      if (workOrder.getSalesId() != 0)
      {
         fulfillOrder(workOrder);
      }

      workOrder.setCompQty(workOrder.getOrigQty());
      addInventory(workOrder.getAssembly(), workOrder.getCompQty());
      workOrder.finish();
   }

   /**
    *  Cancels the workOrder if possible. If it is unable to
    *  cancel the workOrder because it is further down the
    *  process then an IllegalStateException is caught.
    *  Adds inventory back.
    *  @return true/false if cancel was possible or not.
    */
   @WebMethod(exclude = true)
   public boolean cancel(WorkOrder workOrder)
   {
      if (debuglevel > 0) debug(3, "cancel Status " + workOrder.getStatus());

      try
      {
         workOrder.cancel();
      }
      catch (IllegalStateException e)
      {
         e.printStackTrace();
         return false;
      }

      Set<Bom> boms = workOrder.getAssembly().getBOMs();
      if (boms.isEmpty())
      {
          return false;
      }

      // From the boms list, add the number of components back to inv
      for (Bom item : boms)
      {
         Component cItem = item.getComponent();

         // Based on qty from boms, multiply with
         // requested qty (origQty) and add back to inv
         addInventory(cItem, item.getQty() * workOrder.getOrigQty());
         // TODO: sort according to pk of CItem
      }

      return true;
   }

   /**
    * Method to add components of this type to the inventory
    * @param numComponents number of components to be added
    */
   @WebMethod(exclude = true)
   public void addInventory (Component comp, int numComponents)
   {
      Inventory inventory = comp.getInventory();
      if (inventory == null)
      {
         inventory = new Inventory(comp.getId(), numComponents, 0,
               "location", 1234, Util.getCurrentDateRoundToDay());
         em.persist(inventory);
         comp.setInventory(inventory);
      }
      else
      {
         lockInventory(inventory);
         inventory.add(numComponents);
      }
   }

   /**
    * Method to add components of this type to the inventory
    * @param numComponents number of components to be added
    */
   // TODO: inheritance: addInventory(Assembly -> Component, ...);
   @WebMethod(exclude = true)
   public void addInventory (Assembly assembly, int numComponents)
   {
      Inventory inventory = assembly.getInventory();
      if (inventory == null)
      {
         inventory = new Inventory(assembly.getId(), numComponents, 0,
               "location", 1234, Util.getCurrentDateRoundToDay());
         em.persist(inventory);
         assembly.setInventory(inventory);
      }
      else
      {
         lockInventory(inventory);
         inventory.add(numComponents);
      }
   }

   /**
    * Sends a notification to the Customer domain to indicate that large order
    * has been fulfilled.
    */
   @WebMethod(exclude = true)
   public void fulfillOrder(WorkOrder workOrder)
   {
      QueueConnection con = null;
      QueueSession ses = null;
      try
      {
      	

      	 if(Config.bypassJMS){
                if(fulfillOrderExec==null){         	  
            	  fulfillOrderExec = AsyncExecutor.createAsyncExecutor(fulfillOrderMsgL);             	  
         	}

         	
         	TextMessage tmsg = new TextMessageImpl(); 
         	tmsg.setIntProperty("orderId", workOrder.getSalesId());
                tmsg.setIntProperty("oLineId", workOrder.getOLineId());
                tmsg.setLongProperty("publishTime", System.currentTimeMillis());
                tmsg
                     .setText("LargeOrderCompleted: orderID="
                           + workOrder.getSalesId() + ", oLineId="
                           + workOrder.getOLineId());
//               long time = System.currentTimeMillis();
                
                
                fulfillOrderExec.deliverMessage(tmsg); 	
         }else{
               con = queueConnectionFactory.createQueueConnection();
               ses = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
               QueueSender queueSender = ses.createSender(fulfillOrderQueue);
               TextMessage message = ses.createTextMessage();
               message.setIntProperty("orderId", workOrder.getSalesId());
               message.setIntProperty("oLineId", workOrder.getOLineId());
               message.setLongProperty("publishTime", System.currentTimeMillis());
               message
                     .setText("LargeOrderCompleted: orderID="
                           + workOrder.getSalesId() + ", oLineId="
                           + workOrder.getOLineId());
//               long time = System.currentTimeMillis();
               queueSender.send(message);
//               (new Sender()).send("ResponseTime.FulfillOrderQueue", System.currentTimeMillis() - time);
          }
      }
      // TODO: replace by JMSException
      catch (Exception e)
      {
         e.printStackTrace();
         if (debuglevel > 0)
         {
            debug(1, "Exception in fulfill  order for " + workOrder.getId());
            debug(1, e.getMessage());
         }
         throw new EJBException(
               "Failure to send message to the BuyerQueue, ", e);
      }
      finally
      {
         try
         {
            if(con != null) con.close();
            if(ses != null) ses.close();
         }
         catch(JMSException e)
         {
            e.printStackTrace();
            if (debuglevel > 0) debug.printStackTrace(e);
         }
      }
   }
}
