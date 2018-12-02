package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class DoubleHandlerNumeric extends DoubleHandler
{
  DoubleHandlerNumeric(String name)
  {
    super(name, java.sql.Types.DOUBLE);
  }
   
  DoubleHandlerNumeric(String name, int type)
  {
    super(name, type);
  }

  void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
  {
    int len = jarsl.getIntData();
    if (len < 0)
      super.initialize(stmt,columnNumber);
    else
    {
      char[] buffer = new char[len];
  
      for (int i = 0; i < len; i++)
        buffer[i] = jarsl.getCharData();
  
      double value = new BigDecimal(new String(buffer)).doubleValue();
      stmt.setDouble(columnNumber, value);
    }
  }
}
