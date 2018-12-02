/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company       Description
 *  ----------  ----------------  ----------------------------------------------
 *  2003/01/28  Samuel Kounev,    Created LargeOrderMDB to process incoming
 *              Darmstadt Univ.   messages in the LargeOrderQueue. The MDB simply
 *                                calls the LargeOrderEnt bean to place the order.
 *  2003/04/01  John Stecher      updated debugging
 *  2006/02/01  Bernhard Riedhofer, SAP  Modified for the EJB3 version of SPECjAppServer2004
 *
 */
package org.spec.jappserver.mfg;

import java.sql.Date;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.corp.CustomerSesEJB;

@Stateless (name="LargeOrderMDB_Ses")
@MessageDriven(activationConfig = {
   @ActivationConfigProperty(propertyName = "destinationType",
                             propertyValue = "javax.jms.Queue"),
   @ActivationConfigProperty(propertyName = "destination",
                             propertyValue = "jms/LargeOrderQueue"),
   @ActivationConfigProperty(propertyName = "parallelConsumers",
                             propertyValue = "10"),
   @ActivationConfigProperty(propertyName="connectionFactoryName",
                             propertyValue="jms/QueueConnectionFactory")
})
public class LargeOrderMDB implements MessageListener
{
   private static final long serialVersionUID = 1L;

   @PersistenceContext(unitName = "Mfg")
   private EntityManager em;

//   @Resource
//   private MessageDrivenContext ctx;

   @Resource
   int debuglevel = 0;
   Object debugSemaphore = new Object();
   Debug debug = null;

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

   public void onMessage (Message message)
   {
      if (debuglevel > 0) debug(3, "LargeOrderMDB:onMessage");

      try
      {
         int orderId = message.getIntProperty("orderId");
         int oLineId = message.getIntProperty("oLineId");
         String assemblyId = message.getStringProperty("assemblyId");
         short qty = message.getShortProperty("qty");
         Date dueDate = Util.getDateRoundToDay(message.getLongProperty("dueDate"));
         LargeOrder largeOrder = new LargeOrder(orderId, oLineId, assemblyId, qty, dueDate);
         em.persist(largeOrder);
         largeOrder.setCategory(largeOrder.getId() % Config.numCategories);

         // For Atomicity Test 3
         if (debuglevel > 0) debug.println(4, "Atomicity Test 3: OrderId " + largeOrder.getId()
               + " OrderLineId: " + largeOrder.getOrderLineNumber());
      }
      catch (JMSException e)
      {
         e.printStackTrace();
         throw new EJBException(
               "Failure during processing message in LargeOrderMDB, ", e);
      }
   }
}
