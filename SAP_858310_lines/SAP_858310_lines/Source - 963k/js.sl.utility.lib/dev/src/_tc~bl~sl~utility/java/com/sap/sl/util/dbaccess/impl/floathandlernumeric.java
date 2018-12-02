package com.sap.sl.util.dbaccess.impl;


class FloatHandlerNumeric extends DoubleHandlerNumeric
{
  FloatHandlerNumeric(String name)
  {
    super(name);
    this.type = java.sql.Types.FLOAT;
  }
  
}
