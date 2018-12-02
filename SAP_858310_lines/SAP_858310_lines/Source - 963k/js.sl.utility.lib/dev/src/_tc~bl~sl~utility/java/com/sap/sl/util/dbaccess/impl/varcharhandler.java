package com.sap.sl.util.dbaccess.impl;


// according to the "JDBC API Tutorial and Reference"  CHAR, VARCHAR
// and LONGVARCHAR fields can be treated as String

class VarcharHandler extends CharHandler
{
   VarcharHandler(String name)
   {
      super(name);
      this.type = java.sql.Types.VARCHAR;
   }
}
