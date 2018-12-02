package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;

// according to the "JDBC API Tutorial and Reference"  CHAR, VARCHAR
// and LONGVARCHAR fields can be treated as String

class CharHandler extends FieldHandler
{
   CharHandler(String name)
   {
      super(name, java.sql.Types.VARCHAR);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl) throws IOException, SQLException
   {
      int len = read_length(jarsl);

      if (len < 0)
        stmt.setNull(columnNumber,this.type);
      else
      {
        char[] buffer = new char[len];

        for (int i = 0; i < len; i++)
          buffer[i] = jarsl.getCharData();
          
        String newvalue = new String(buffer).trim();
        if (newvalue.length() == 0)
          newvalue = " ";

        stmt.setString(columnNumber,newvalue); // SAP's openSQL is not compatible to jdbc: empty CHAR fields are not allowed
      }
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    int len = read_length(jarsl);

    if (len < 0)
      return "<NULL>";
 
    char[] buffer = new char[len];

    for (int i = 0; i < len; i++)
      buffer[i] = jarsl.getCharData();

   return new String(buffer);
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value) throws SQLException
  {
     int len = value.length();

     if (len < 0)
       stmt.setNull(columnNumber,this.type);
     else
     {
       String newvalue = decode(value.trim());
       if (newvalue.length() == 0)
         newvalue = " ";
       stmt.setString(columnNumber,newvalue); // SAP's openSQL is not compatible to jdbc: empty CHAR fields are not allowed
     }
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   String getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getString(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      String s = rs.getString(columnNumber);
      if (s==null)
      {
        jarsl.putIntData(-1);
      }
      else
      {
        jarsl.putIntData(s.length());
        jarsl.putCharsData(s);
      }
   }
  private int read_length(JarSLIF jarsl) throws IOException
  {
    return FileLengthReader.getInstance().read_length(jarsl,bereadyforoldfile);
  }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
  
  public String decode (String input)
  {
	String result;
	
	try
	{
      result = URLDecoder.decode(input,"UTF-8");
	}
	catch (UnsupportedEncodingException e)
	{
      result = input;
	}
	catch (IllegalArgumentException e)
	{
      result = input;
	}
	
	return result;
  }
}
