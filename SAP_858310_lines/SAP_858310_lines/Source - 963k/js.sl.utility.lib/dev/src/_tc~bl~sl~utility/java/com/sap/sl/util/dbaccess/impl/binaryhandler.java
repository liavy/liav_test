package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


// =========================== binary fields ============================

class BinaryHandler extends FieldHandler
{
   protected int maxlen = 0;
   
   BinaryHandler(String name)
   {
      super(name, java.sql.Types.BINARY);
   }
  
   BinaryHandler(String name, int maxlen)
   {
      super(name, java.sql.Types.BINARY);
      this.maxlen = maxlen;
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     int length = read_length(jarsl);

     if (length >= 0)
     {
       byte[] bytes;

       if (length <= maxlen && maxlen > 0)
       {
         bytes = new byte[maxlen];
         jarsl.getData(bytes,0,length);
       }
       else if (length <= maxlen || maxlen == 0)
       {
         bytes = new byte[length];
         jarsl.getData(bytes,0,length);
       }
       else
       {
         bytes = new byte[maxlen];
         byte[] throwaway = new byte[length-maxlen];
         jarsl.getData(bytes,0,maxlen);
         jarsl.getData(throwaway,0,length-maxlen);
       }

       stmt.setBytes(columnNumber, bytes);
     }
     else
     {
       this.initialize(stmt,columnNumber);
     }
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    int length = read_length(jarsl);
    if (length >= 0)
    {
      int readbytes;
      byte[] bytes = new byte[length];
  
      readbytes=jarsl.getData(bytes,0,length);
  
      String retval = "<"+length+" Bytes>";
      
      if (length > 0)
      {
        retval = retval+" (";
        for (int i = 0; i < length;i++)
        {
          if (Character.isLetterOrDigit((char)bytes[i]))
            retval = retval+(char)bytes[i];
          else
            retval = retval+"#";
          if (i == 50)
          {
            retval = retval+"...";
            break;
          }
        }
          
        retval = retval+")";
      }
      
      return retval;
    }
    else
      return "<null>";
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    // TODO implement this method
    stmt.setNull(columnNumber,this.type);
    throw new RuntimeException("BinaryHandler.setValue() is not implemented");
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   byte[] getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getBytes(columnNumber);
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
      byte[] bytes = rs.getBytes(columnNumber);
      if (bytes == null)
        jarsl.putIntData(-1);
      else
      {
        jarsl.putIntData(bytes.length);
        jarsl.putData(bytes,0,bytes.length);
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
