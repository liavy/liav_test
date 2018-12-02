package com.sap.sl.util.dbaccess.impl;

class DecimalHandlerChar extends DecimalHandler
{
  DecimalHandlerChar(String name, boolean nullable)
  {
    super(name,java.sql.Types.DECIMAL,nullable);
  }
}
