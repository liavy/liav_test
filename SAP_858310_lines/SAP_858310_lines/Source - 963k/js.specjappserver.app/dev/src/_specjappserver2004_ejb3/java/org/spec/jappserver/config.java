package org.spec.jappserver;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * Class which holds constants for configuration of
 *  SAP specific variants of SPECj ported to EJB3.
 */
public class Config
{
   /* set to true if jms should be bypassed */
//   public static boolean bypassJMS = true;
   public static boolean bypassJMS = Boolean.getBoolean("bypassJMS");

   /* Flag if lock method should be used */
   public static boolean isLockingUsed = true;
    
   /* Flag if pessimistic locking is used.
    * Implemenation not finished for all beans for which according to run rules Repeatable read should be used.
    * If bypassJMS = true then isPessimisticLockingUsed should be true,
    * otherwise there will be too much OptimisticLockExceptions during order processing in FulfillOrderMDB.
    */
   public static boolean isPessimisticLockingUsed = false;
    
   /* Lock mode type if lock method is used */
   public static LockModeType lockMode = LockModeType.WRITE;
   
   /* controls if cache for items during browsing should be used */
   //public static boolean isItemsCacheUsed = false;
   public static boolean isItemsCacheUsed = Boolean.getBoolean("EnableItemCache");
   //true, if property -DEnableItemCache=true used, false if not specified

   /* determines size of e.g. caches like item cache */ 
   public static int txRate = 200;

   /* determines if entities should be preloaded by executing additional jpa statements -> reduces number of OptimisticLockExceptions??? */
   public static boolean isPreloadEntities = Boolean.getBoolean("isPreloadEntities");
   
   /* number of categories for large orders, must be equal to number of larger order agents of driver */
   public static int numCategories = Integer.getInteger("numCategories", 1).intValue();

   /* flag if sharability is checked or not */ 
   public static boolean isSharabilityChecked = false;

   /* flag if constructor expression should be used in ItemBrowserSes */ 
   public static boolean isConstrExprInItemBrowserSesUsed = false;
   
   private static final FlushModeType FLUSH_MODE = FlushModeType.AUTO;
   
   public static void setFlushMode(Query query)
   {
      if (FLUSH_MODE != FlushModeType.AUTO)
      {
         query.setFlushMode(FLUSH_MODE);
      }
   }

   public static void checkSharability(Object obj)
   {
      ShareabilityChecker.checkSharability(obj);
   }
}
