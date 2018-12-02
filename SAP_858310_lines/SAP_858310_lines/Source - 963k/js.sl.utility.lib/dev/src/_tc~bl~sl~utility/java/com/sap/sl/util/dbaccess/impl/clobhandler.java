package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


// ============================= lob fields ==============================

class ClobHandler extends FieldHandler
{   
   ClobHandler(String name)
   {
      super(name, java.sql.Types.CLOB);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     int len = read_length(jarsl);
     
      if (len >= 0)
      {
        char[] buffer = new char[len];
        for (int i=0; i<len; ++i) {
          buffer[i]=jarsl.getCharData();
        }
        String sss = new String(buffer);
        stmt.setCharacterStream(columnNumber, new StringReader(sss),sss.length());
      }
      else
      {
        this.initialize(stmt,columnNumber);
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
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
	String newvalue = new CharHandler("dummy").decode(value);
    stmt.setCharacterStream(columnNumber, new StringReader(newvalue),newvalue.length());
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    // according to the observation of Dietmar Steinbichler, on Oracle setNull is not possible, see customer problem 1113299/2005
    // But probably SAP's openSQL offers a workaround for that ...
    if (ConnectionInfo.getInstance().isRunningOnOracle() &&
        ConnectionInfo.getInstance().isVendorConnection()  )
      this.setValue(stmt,columnNumber,"");
    else
      stmt.setNull(columnNumber,this.type);
  }

   // does not return data, but a Clob object containing a logical pointer
   // to the SQL CLOB data
   Clob getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getClob(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      Clob clob = rs.getClob(columnNumber);
      
      if (clob != null)
      {
        long len = clob.length();
        if ( len > Integer.MAX_VALUE )
          throw new IOException("Length " + len + " too large, field " + this.name);
        String buffer = clob.getSubString(1L, (int)len);
  
        jarsl.putIntData((int)clob.length());
        jarsl.putCharsData(buffer);
      }
      else
      {
        jarsl.putIntData(-1);
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
}
