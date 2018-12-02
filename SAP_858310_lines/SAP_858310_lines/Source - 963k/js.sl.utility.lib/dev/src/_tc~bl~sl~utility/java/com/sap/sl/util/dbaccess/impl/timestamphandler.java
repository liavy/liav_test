package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import com.sap.sl.util.jarsl.api.JarSLIF;


class TimestampHandler extends FieldHandler
{ 
   TimestampHandler(String name)
   {
      super(name, java.sql.Types.TIMESTAMP);
   }

   // kept as long in data file

   void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
   throws IOException, SQLException
{
  long l = jarsl.getLongData();
  if (l == -1)
    initialize(stmt,columnNumber);
  else
  {
    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//    if (verbose)
//      logging.message("field "+name+": timestamp read from file = "+gettimestampdisplay(l));
    stmt.setTimestamp(columnNumber,createTimestamp(l),utcCalendar);
  }
}

String readValue(JarSLIF jarsl) throws IOException
{
 long l = jarsl.getLongData();
 if (l == -1)
   return "<NULL>";

 return new Long(l).toString()+" ("+createTimestamp(l).toString()+")";
}

void setValue(PreparedStatement stmt, int columnNumber, String value)
    throws SQLException
{
 long l = new Long(value).longValue();
 if (l == -1)
   initialize(stmt,columnNumber);
 else
 {
   Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
   stmt.setTimestamp(columnNumber,createTimestamp(l),utcCalendar);
 }
}
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    stmt.setNull(columnNumber,this.type);
  }

  long getValue(ResultSet rs, int columnNumber) throws SQLException
  {
    return rs.getTimestamp(columnNumber).getTime();
  }

  void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
       throws IOException, SQLException
  {
    Timestamp timestamp = rs.getTimestamp(columnNumber);
    if (timestamp != null)
    {
      jarsl.putLongData(timestamp.getTime());
    }
    else
      jarsl.putLongData(-1);
  }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
  
  private Timestamp createTimestamp(long l)
  {
    // switch to GMT calendar when calling setTimestamp()
    return new Timestamp(l);
  }

  private String gettimestampdisplay(long l)
  {
	return ""+l+" ("+new Timestamp(l)+" GMT)";
  }
}
