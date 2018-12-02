package com.sap.sl.util.dbaccess.impl;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class TinyIntHandler extends FieldHandler
{
   TinyIntHandler(String name)
   {
      super(name, java.sql.Types.TINYINT);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setByte(columnNumber, jarsl.getByteData());
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Byte(jarsl.getByteData()).toString();
  }
  
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     stmt.setByte(columnNumber, new Byte(value).byteValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }
  
   byte getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getByte(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putByteData(rs.getByte(columnNumber));
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
