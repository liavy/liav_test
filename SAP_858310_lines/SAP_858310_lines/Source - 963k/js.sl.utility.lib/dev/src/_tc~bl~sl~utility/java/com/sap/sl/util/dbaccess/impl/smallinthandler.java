package com.sap.sl.util.dbaccess.impl;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class SmallIntHandler extends FieldHandler
{
   SmallIntHandler(String name)
   {
      super(name, java.sql.Types.SMALLINT);
   }
   
   SmallIntHandler(String name, int type)
   {
     super(name, type);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setShort(columnNumber, jarsl.getShortData());
   }
   
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Short(jarsl.getShortData()).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     stmt.setShort(columnNumber, new Short(value).shortValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   short getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getShort(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putShortData(rs.getShort(columnNumber));
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
