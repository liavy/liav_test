package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.sap.sl.util.jarsl.api.JarSLIF;


class DateHandler extends FieldHandler
{
   DateHandler(String name)
   {
      super(name, java.sql.Types.DATE);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     long l = jarsl.getLongData();
     if (l == -1)
       initialize(stmt,columnNumber);
     else
     {
       try
       {
         stmt.setDate(columnNumber,createDate(l,false));
       }
       catch (SQLException e)
       {
         // be ready for old export which may ain unnormalized data
         stmt.setDate(columnNumber,createDate(l,true));
       }
     }
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    long l = jarsl.getLongData();
    if (l == -1)
      return "<NULL>";
      
    return new Long(l).toString()+" ("+createDate(l,false).toString()+")";
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
     long l = parseDate(value);
     if (l == -1)
       initialize(stmt,columnNumber);
     else
       stmt.setDate(columnNumber,createDate(l,false));
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   long getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getDate(columnNumber).getTime();
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     Date date = rs.getDate(columnNumber,Calendar.getInstance(TimeZone.getTimeZone("GMT")));
     if (date != null)
       jarsl.putLongData(DateTimeNormalizer.normalizeSqlDateMillies((date.getTime())));
     else
       jarsl.putLongData(-1);
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
  
  private Date createDate(long l, boolean normalize_it)
  {
    if (normalize_it)
      return new Date(DateTimeNormalizer.normalizeSqlDateMillies(l));
    else
      return new Date(l);
  }
  
  private long parseDate(String s)
  {
    long retval = -1;
    
    try
    {
      retval = new SimpleDateFormat("yyyy-MM-dd").parse(s).getTime();
    }
    catch (ParseException e)
    {
      retval = new Long(s).longValue();
    }

    return retval;
  }
}

