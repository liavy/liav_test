package com.sap.sl.util.dbaccess.impl;



class FloatHandler extends DoubleHandler
{
   FloatHandler(String name)
   {
      super(name);
      this.type = java.sql.Types.FLOAT;
   }
}
