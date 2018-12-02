package com.sap.security.core.admin;

import com.sapmarkets.tpd.master.TradingPartnerInterface;

   /**
    *  Description of the Class
    *
    * @author  d027994
    * @created  26. Juni 2001
    */
   public class CompanyNameComparator implements java.util.Comparator
   {
      /**
       *  Description of the Method
       *
       * @param  o1 Description of Parameter
       * @param  o2 Description of Parameter
       * @return  Description of the Returned Value
       */
      public int compare(Object o1, Object o2)
      {
         int result = ((TradingPartnerInterface)o1).getDisplayName().compareToIgnoreCase(((TradingPartnerInterface)o2).getDisplayName());
         if (result == 0) result = -1;
         return result;
      }
      
   }
