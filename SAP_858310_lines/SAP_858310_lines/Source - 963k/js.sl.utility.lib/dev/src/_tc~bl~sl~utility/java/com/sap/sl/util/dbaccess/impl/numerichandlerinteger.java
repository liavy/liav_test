package com.sap.sl.util.dbaccess.impl;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;


class NumericHandlerInteger extends NumericHandler
{
  NumericHandlerInteger(String name, boolean nullable)
  {
    super(name, java.sql.Types.NUMERIC, nullable);
  }
   
  NumericHandlerInteger(String name, int type, boolean nullable)
  {
    super(name, type, nullable);
  }

  void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
  {
    String s = new Integer(jarsl.getIntData()).toString();
    super.setValue(stmt,columnNumber,s);
  }
}
