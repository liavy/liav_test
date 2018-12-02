package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import com.sap.sl.util.jarsl.api.JarSLIF;


class TimeHandler extends FieldHandler
{
   TimeHandler(String name)
   {
      super(name, java.sql.Types.TIME);
   }

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     long l = jarsl.getLongData();
     if (l == -1)
       initialize(stmt,columnNumber);
     else
       stmt.setTime(columnNumber, createTime(l,false));
   }
   
  String readValue(JarSLIF jarsl) throws IOException
  {
    long l = jarsl.getLongData();
    if (l == -1)
      return "<NULL>";
      
    return new Long(l).toString()+" ("+createTime(l,false).toString()+")";
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value)
     throws SQLException
  {
    long l = new Long(value).longValue();
    if (l == -1)
      initialize(stmt,columnNumber);
    else
      stmt.setTime(columnNumber, createTime(l,false));
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

   long getValue(ResultSet rs, int columnNumber) throws SQLException
   {
      return rs.getTime(columnNumber).getTime();
   }

   void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
   {
     Time time = rs.getTime(columnNumber);
     if (time != null)
       jarsl.putLongData(createTime(time.getTime(),true).getTime());
     else
       jarsl.putLongData(-1);
   }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
  
  private Time createTime(long l, boolean normalize_it)
  {
    if (normalize_it)
      return new Time(DateTimeNormalizer.normalizeSqlTimeMillies((l)));
    else
      return new Time(l);
  }
}
