package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.sap.sl.util.jarsl.api.JarSLIF;

// according to the "JDBC API Tutorial and Reference"  CHAR, VARCHAR
// and LONGVARCHAR fields can be treated as String

class NullHandler extends FieldHandler
{
  NullHandler(String name, int type)
  {
    super(name,type);
  }

  void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl) throws IOException, SQLException
  {
    this.initialize(stmt,columnNumber);
  }
  
  String readValue(JarSLIF jarsl) throws IOException
  {
    return "<NULL>";
  }
   
  void setValue(PreparedStatement stmt, int columnNumber, String value) throws SQLException
  {
    this.initialize(stmt,columnNumber);
  }
  
  void initialize(PreparedStatement stmt, int columnNumber)
        throws SQLException
  {
    switch (type)
    {
      /* 
      case Types.DECIMAL:
      case Types.NUMERIC:
        stmt.setBigDecimal(columnNumber,new BigDecimal("0"));
        break;
      */
      case Types.CLOB:
        stmt.setCharacterStream(columnNumber, new StringReader(""),"".length());
        break;
      default:
        stmt.setNull(columnNumber,this.type);
        break;
    }
  }

  String getValue(ResultSet rs, int columnNumber) throws SQLException
  {
    return null;
  }

  void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException
  {
    return;
  }
   
  void set_bereadyforoldfile()
  {
    bereadyforoldfile = true;
  }
}
