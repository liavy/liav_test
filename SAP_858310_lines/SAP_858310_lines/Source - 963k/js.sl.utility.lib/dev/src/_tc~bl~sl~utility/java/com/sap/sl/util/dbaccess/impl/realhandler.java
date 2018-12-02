package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class RealHandler extends FieldHandler
{
   RealHandler(String name)
   {
      super(name, java.sql.Types.REAL);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setFloat(columnNumber, jarsl.getFloatData());
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Float(jarsl.getFloatData()).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     stmt.setFloat(columnNumber, new Float(value).floatValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   float getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getFloat(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putFloatData(rs.getFloat(columnNumber));
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
