/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ---------------------------------------------
 *  2003/01/29  Samuel Kounev, Darmstadt  Created FulfillOrderMDB to process incoming
 *                                        messages in the FulfillOrderQueue. The MDB
 *                                        simply marks respective order as completed.
 *  2003/03/28  Samuel Kounev, Darmstadt  Moved CustomerSes EJB creation to ejbCreate so
 *                                        we only do it once when the MDB is created.
 *                                        Added code to remove CustomerSes EJB Object
 *                                        in ejbRemove.
 *  2003/04/17  Samuel Kounev, Darmstadt  Removed caching of customerSes EJB reference.
 *  2003/05/23  John Stecher, IBM         Updated the CustomerSes Home lookup to use narrow.
 *  2003/05/29  Samuel Kounev, Darmstadt  Added call to set OrderLine status.
 *  2003/06/18  John Stecher, IBM         Updated to make better use of BigDecimal.
 *  2003/06/28  John Stecher, IBM         Removed unnecessary imports.
 *  2003/01/08  John Stecher, IBM         Changed code to eliminate unused objects being
 *                                        passed into methods
 *  2006/02/01   Bernhard Riedhofer, SAP  Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.orders;

import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.corp.CustomerSes;
import org.spec.jappserver.corp.CustomerSesEJB;


@Stateless (name="FulfillOrderMDB_Ses")
@MessageDriven(activationConfig = {
   @ActivationConfigProperty(propertyName = "destinationType",
                             propertyValue = "javax.jms.Queue"),
   @ActivationConfigProperty(propertyName = "destination",
                             propertyValue = "jms/FulfillOrderQueue"),
   @ActivationConfigProperty(propertyName = "parallelConsumers",
                             propertyValue = "10"),
   @ActivationConfigProperty(propertyName="connectionFactoryName",
                             propertyValue="jms/QueueConnectionFactory")
})
public class FulfillOrderMDB
      implements MessageListener
{
   private static final long serialVersionUID = 1L;

   @PersistenceContext(unitName = "Orders")
   private EntityManager em;

//   @Resource
//   private MessageDrivenContext ctx;

   @Resource
   int debuglevel = 0;
   Object debugSemaphore = new Object();
   Debug debug = null;

   @EJB(
         name="org.spec.jappserver.corp.CustomerSes",
         beanInterface=CustomerSes.class,
         beanName="CustomerSes"
         )
   protected CustomerSes cSes;

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

   private Order findOrder(int id)
   {
     if (Config.isPessimisticLockingUsed) {
       Query query = em.createNamedQuery("getOrder").setParameter(1, id);
       Config.setFlushMode(query);
       Order order = (Order) query.getSingleResult();
       return order;
     }else{     
       Order order = em.getReference(Order.class, id);
       if (Config.isLockingUsed)
       {
         if (order != null)
         {
            em.lock(order, LockModeType.READ);
         }         
       }
       return order;
     }
   }
   
   public void onMessage (Message message)
   {
      if (debuglevel > 0) debug(3, "FulfillOrderMDB:onMessage");

      try
      {
         int orderId = message.getIntProperty("orderId");
         int orderLineId = message.getIntProperty("oLineId");
         Order order = findOrder(orderId);
         OrderLine orderLine = em.getReference(OrderLine.class, new OrderLinePK(
               orderLineId, orderId));
         orderLine.setOlineStatus(3);
         orderLine.setShipDate(Util.getCurrentDateRoundToDay());

         cSes.addInventory(order.getCustomerId(), orderLine
               .getItemId(), orderLine.getQuantity(), orderLine.getTotalValue());
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new EJBException(e);
      }
   }
}
