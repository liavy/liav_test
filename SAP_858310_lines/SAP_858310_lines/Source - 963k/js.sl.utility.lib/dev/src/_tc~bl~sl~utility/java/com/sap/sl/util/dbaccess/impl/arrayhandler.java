package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;

class ArrayHandler extends FieldHandler
{
   ArrayHandler(String name)
   {
      super(name, java.sql.Types.ARRAY);
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
   
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    stmt.setObject(columnNumber, value);
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
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   Object getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getArray(columnNumber).getArray();
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      jarsl.putObjectData(rs.getArray(columnNumber).getArray());
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
