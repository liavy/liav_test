package com.sap.sl.util.dbaccess.impl;


class DecimalHandlerFloat extends DecimalHandlerDouble
{
  DecimalHandlerFloat(String name, boolean nullable)
  {
    super(name, nullable);
    this.type = java.sql.Types.DECIMAL;
  }
}
