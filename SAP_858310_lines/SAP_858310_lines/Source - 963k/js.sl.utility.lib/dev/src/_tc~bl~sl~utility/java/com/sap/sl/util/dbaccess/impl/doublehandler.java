package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


class DoubleHandler extends FieldHandler
{
  static double nulldefault = -2.4680;
  
   DoubleHandler(String name)
   {
      super(name, java.sql.Types.DOUBLE);
   }
   
   DoubleHandler(String name, int type)
   {
     super(name, type);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     double value = new Double(jarsl.getDoubleData()).doubleValue();
     if (value == nulldefault)
       this.initialize(stmt,columnNumber);
     else  
       stmt.setDouble(columnNumber, value);
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    Double retval = new Double(jarsl.getDoubleData());
    if (retval.doubleValue() == nulldefault)
      return "<NULL>";
    
    return retval.toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    stmt.setDouble(columnNumber,new Double(value).doubleValue());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   double getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getDouble(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     double value = rs.getDouble(columnNumber);
     if (rs.wasNull())
       value = nulldefault;
     jarsl.putDoubleData(value);
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
