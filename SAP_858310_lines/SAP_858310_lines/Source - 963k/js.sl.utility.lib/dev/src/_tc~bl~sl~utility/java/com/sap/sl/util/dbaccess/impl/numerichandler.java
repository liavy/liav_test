package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.sl.util.jarsl.api.JarSLIF;


class NumericHandler extends FieldHandler
{
   NumericHandler(String name, boolean nullable)
   {
     super(name, java.sql.Types.NUMERIC);
     this.nullable = nullable;
   }
   
  NumericHandler(String name, int type, boolean nullable)
  {
    super(name, type);
    this.nullable = nullable;
  }

   // values for this field are kept as strings inside the data files

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     int len = jarsl.getIntData();
     if (len <= 0)
       initialize(stmt,columnNumber);
     else
     {
       char[] buffer = new char[len];

       for (int i = 0; i < len; i++)
         buffer[i] = jarsl.getCharData();

       try
       {
         stmt.setBigDecimal(columnNumber, new BigDecimal(new String(buffer)));
       }
       catch (RuntimeException e)
       {
         System.out.println(new String(buffer).toString()+ "can't be converted to BigDecimal");
         stmt.setBigDecimal(columnNumber,new BigDecimal(0));
       }
     }
   }
   
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    int len = jarsl.getIntData();
    if (len < 0)
      return "<NULL>";

    char[] buffer = new char[len];

    for (int i = 0; i < len; i++)
      buffer[i] = jarsl.getCharData();

    return new BigDecimal(new String(buffer)).toString();
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    stmt.setBigDecimal(columnNumber,new BigDecimal(value));
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   String getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getBigDecimal(columnNumber).toString();
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     BigDecimal big = rs.getBigDecimal(columnNumber);
     if (big == null)
     {
       jarsl.putIntData(-1);
     }
     else
     {
       String s = big.toString();
       if (s.length() == 0)
         s = "0";
       jarsl.putIntData(s.length());
       jarsl.putCharsData(s);
     }
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
