package com.sap.sl.util.dbaccess.impl;



// =========================== binary fields ============================

class LongvarbinaryHandler extends BinaryHandler
{
   LongvarbinaryHandler(String name)
   {
      super(name);
      this.type = java.sql.Types.LONGVARBINARY;
   }
}

