package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class ObjectHandler extends FieldHandler
{
   ObjectHandler(String name)
   {
      super(name, java.sql.Types.OTHER);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      try
      {
        stmt.setObject(columnNumber, jarsl.getObjectData());
      }
      catch(ClassNotFoundException e)
      {
        throw new IOException(e.toString());
      }
   }
   
   String readValue(JarSLIF jarsl) throws IOException
   {
     Object o;
     try
     {
       o = jarsl.getObjectData();
     }
     catch(ClassNotFoundException e)
     {
       return "???";
     }
     
     return o.toString();
   }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    stmt.setObject(columnNumber, value);
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   Object getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getObject(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putObjectData(rs.getObject(columnNumber));
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}

