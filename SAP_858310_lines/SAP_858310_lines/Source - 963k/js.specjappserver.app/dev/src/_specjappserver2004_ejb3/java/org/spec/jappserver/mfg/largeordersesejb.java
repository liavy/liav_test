/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2002/03/22  Agnes Jacob, SUN          Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2002/08/29  Chris Beer, HP            Added try/catch block for NoSuchObjectException
 *                                        in findLargeOrders
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2003/02/..  Samuel Kounev, Darmstadt  Removed unnecessary method createLargeOrder.
 *                                        Orders are now created by the LargeOrderMDB.
 *  2003/06/28  John Stecher, IBM         Removed unnecessary imports
 *  2005/12/22  Bernhard Riedhofer, SAP   Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import java.util.List;
import java.util.Vector;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.spec.jappserver.Config;
import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.mfg.helper.LargeOrderInfo;

/**
 * Bean implementation class for LargeOrderSes
 */
@Stateless (name = "LargeOrderSes")
public class LargeOrderSesEJB implements LargeOrderSes, LargeOrderSesLocal
{
   @PersistenceContext (unitName="Mfg")
   protected EntityManager em;

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
               debug = new DebugPrint(debuglevel, LargeOrderSesEJB.class);
            }
            else
            {
               debug = new Debug();
            }
         }
      }
      debug.println(level, str);
   }

   public LargeOrderSesEJB ()
   {
      if (debuglevel > 0) debug(3, "LargeOrderSesEJB () default constructor");
   }

   /**
    * Find all LargeOrders on the Database
    * @return Vector of LargeOrderEntLocal found.
    */
   @SuppressWarnings("unchecked")
   public Vector<LargeOrderInfo> findLargeOrders()
   {
      if (debuglevel > 0) debug(3, "findLargeOrders");

      Query query = em.createNamedQuery("getAllLargeOrders");
      Config.setFlushMode(query);
      List<LargeOrder> los = query.getResultList();

      Vector<LargeOrderInfo> lov = new Vector<LargeOrderInfo>();
      for (LargeOrder lo : los)
      {
         lov.addElement(lo.getLargeOrderInfo());
         // TODO: ???
         //} catch (NoSuchObjectLocalException itemWasDeleted)  { }
      }
      return lov;
   }

    /**
     * Find LargeOrders on the Database based on categories
     * @return List of LargeOrderEntLocal found.
     */
    @SuppressWarnings({"unchecked","boxing"})
    public Vector<LargeOrderInfo> findLargeOrders(int category)
    {
       if (debuglevel > 0) debug(3, "findLargeOrders");
       Query query = em.createNamedQuery("getLargeOrdersByCategory")
             .setParameter(1, category);
       Config.setFlushMode(query);
       List<LargeOrder> los = (List<LargeOrder>) query.getResultList();

//       if (los.size() == 0)
//       {
//          throw new EJBException("Unable to find any orders with category "
//               + category);
//       }

       Vector<LargeOrderInfo> lov = new Vector<LargeOrderInfo>();
       for (LargeOrder lo : los)
       {
          lov.addElement(lo.getLargeOrderInfo());
          // TODO: ???
          //} catch (NoSuchObjectLocalException itemWasDeleted)  { }
       }
       return lov;
    }
}
