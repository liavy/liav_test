/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company       Description
 *  ----------  ----------------  ----------------------------------------------
 *  2003/01/26  Samuel Kounev     Created based on ReceiveSes from SPECjAS2002.
 *              Darmstadt Univ.
 *  2003/04/04  Samuel Kounev,    Changed onMessage to receive an ObjectMessage
 *              Darmstadt Univ.   containing a Vector of DeliveryInfo objects.
 *                                When a PO is delivered, inventory is now
 *                                updated using a single message to the Mfg
 *                                domain.
 *  2004/02/12  Samuel Kounev,    Rolled back previous change, since it might
 *              Darmstadt Univ.   cause deadlocks when updating InventoryEnt and
 *                                SComponentEnt (see osgjava-6527).
 *  2006/02/01  Bernhard Riedhofer, SAP  Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.DeliveryInfo;
import org.spec.jappserver.common.Util;
import org.spec.jappserver.corp.CustomerSesEJB;

@Stateless (name="ReceiveMDB_Ses")
@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName = "destinationType",
                                propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination",
                                propertyValue = "jms/ReceiveQueue"),
      @ActivationConfigProperty(propertyName = "parallelConsumers",
                                propertyValue = "10"),
      @ActivationConfigProperty(propertyName="connectionFactoryName",
                                propertyValue="jms/QueueConnectionFactory")
})
public class ReceiveMDB implements MessageListener
{
   private static final long serialVersionUID = 1L;

   @PersistenceContext(unitName = "Mfg")
   private EntityManager em;

// TODO
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
   
   public void onMessage(Message message)
   {
      if (debuglevel > 0) debug(3, "ReceiveMDB:onMessage");

      try
      {
         DeliveryInfo delInfo = (DeliveryInfo) ((ObjectMessage) message)
               .getObject();
         int numComponents = delInfo.qty;

         Component comp = em.find(Component.class, delInfo.partID);
         Inventory inv = comp.getInventory();
         if (inv == null)
         {
            inv = new Inventory(comp.getId(), numComponents, 0, "location",
                  1234, Util.getCurrentDateRoundToDay());
            em.persist(inv);
            comp.setInventory(inv);
         }
         else
         {
            lockInventory(inv);
            inv.add(numComponents);
            inv.takeOrdered(numComponents);
         }
      }
      catch (JMSException e)
      {
         e.printStackTrace();
         throw new EJBException(
               "Failure during processing message in ReceiveMDB, ", e);
      }
   }
}
