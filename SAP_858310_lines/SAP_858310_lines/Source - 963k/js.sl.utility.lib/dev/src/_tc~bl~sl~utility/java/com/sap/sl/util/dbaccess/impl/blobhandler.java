package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;

import com.sap.sl.util.jarsl.api.JarSLIF;


// ============================= lob fields ==============================

class BlobHandler extends FieldHandler
{
   BlobHandler(String name)
   {
      super(name, java.sql.Types.BLOB);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     int len = read_length(jarsl);
      
     if (len >= 0)
     {
      byte[] buffer = new byte[len];
      jarsl.getData(buffer);
     
      try
      {
        stmt.setBinaryStream(columnNumber, new ByteArrayInputStream(buffer), buffer.length);
      }
      catch (SQLException e)
      {
        if (len == 0)
          this.initialize(stmt,columnNumber);
        else
          throw e;
      }
     }
     else
     {
       this.initialize(stmt,columnNumber);
     }
   }
  
   String readValue(JarSLIF jarsl)
     throws IOException
   {
     int len = read_length(jarsl);
     if (len >=0)
     {
       byte[] buffer = new byte[len];
       jarsl.getData(buffer);
       return "<"+len+" Bytes>";
     }
     else
     {
       return "<null>";
     }
   }
  
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    // TODO implement this method
    stmt.setNull(columnNumber,this.type);
    throw new RuntimeException("BlobHandler.setValue() is not implemented");
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   Blob getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getBlob(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     Blob blob = rs.getBlob(columnNumber);
      
     if (blob != null)
     {
      long len = blob.length();
      if ( len > Integer.MAX_VALUE )
        throw new IOException("Length " + len + " too large, field " + this.name);
      byte[] buffer = blob.getBytes(1L,(int)len);
      jarsl.putIntData((int)len);
      jarsl.putData(buffer,0,buffer.length);
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
