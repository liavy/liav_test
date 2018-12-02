package com.sap.sl.util.dbaccess.impl;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class IntegerHandler extends FieldHandler
{
   IntegerHandler(String name, boolean nullable)
   {
      super(name, java.sql.Types.INTEGER);
      this.nullable = nullable;
   }
   
  IntegerHandler(String name, int type, boolean nullable)
  {
    super(name, type);
    this.nullable = nullable;
   }
   
  IntegerHandler(String name, int type)
  {
    super(name, type);
  }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      stmt.setInt(columnNumber, jarsl.getIntData());
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    return new Integer(jarsl.getIntData()).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    stmt.setInt(columnNumber,new Integer(value).intValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   int getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getInt(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     int value = rs.getInt(columnNumber);
     
     if (nullable)
     {
       if (rs.wasNull())
         jarsl.putIntData(-1);
       else
       {
         String s = new Integer(value).toString();
         jarsl.putIntData(s.length());
         jarsl.putCharsData(s);
       }
     }
     else
       jarsl.putIntData(value);
   }

  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
