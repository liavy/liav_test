package com.sap.sl.util.dbaccess.impl;


// =========================== binary fields ============================

class VarbinaryHandler extends BinaryHandler
{
   VarbinaryHandler(String name)
   {
      super(name);
      this.type = java.sql.Types.VARBINARY;
   }
}

