package com.sap.sl.util.dbaccess.impl;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;


class BigIntHandler extends FieldHandler
{
   BigIntHandler(String name)
   {
      super(name, java.sql.Types.BIGINT);
   }
   
  BigIntHandler(String name, int type)
  {
    super(name, type);
  }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setLong(columnNumber, jarsl.getLongData());
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Long(jarsl.getLongData()).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     stmt.setLong(columnNumber, new Long(value).longValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   long getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getLong(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putLongData(rs.getLong(columnNumber));
   }   
  
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
