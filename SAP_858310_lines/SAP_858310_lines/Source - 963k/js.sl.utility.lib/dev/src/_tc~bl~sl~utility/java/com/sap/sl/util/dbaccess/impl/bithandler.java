package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;


// ============================= bit fields ==============================

class BitHandler extends FieldHandler
{
   BitHandler(String name)
   {
      super(name, java.sql.Types.BIT);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setBoolean(columnNumber, jarsl.getBooleanData());
   }
   
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Boolean(jarsl.getBooleanData()).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     stmt.setBoolean(columnNumber, new Boolean(value).booleanValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   boolean getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getBoolean(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putBooleanData(rs.getBoolean(columnNumber));
   }
  
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
