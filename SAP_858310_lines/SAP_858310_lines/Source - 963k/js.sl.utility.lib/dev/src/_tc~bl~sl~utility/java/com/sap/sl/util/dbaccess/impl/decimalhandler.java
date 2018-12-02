package com.sap.sl.util.dbaccess.impl;


class DecimalHandler extends NumericHandler
{
   DecimalHandler(String name, boolean nullable)
   {
      super(name, nullable);
      type = java.sql.Types.DECIMAL;
   }
  
  DecimalHandler(String name, int type, boolean nullable)
  {
     super(name,type, nullable);
  }
}
