package com.sap.sl.util.dbaccess.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.jarsl.api.JarSLIF;


abstract class FieldHandler
{
  protected String name;
  protected int    type;    // see java.sql.Types
  protected boolean bereadyforoldfile = false;
  protected boolean nullable = true;

  FieldHandler(String name, int type)
  {
    this.name = name;
    this.type = type;
  }

   abstract void setValue(PreparedStatement stmt, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException;
   abstract String readValue(JarSLIF jarsl)
      throws IOException;
   abstract void writeValue(ResultSet rs, int columnNumber, JarSLIF jarsl)
      throws IOException, SQLException;
   abstract void setValue(PreparedStatement stmt, int columnNumber, String value)
      throws SQLException;
   abstract void initialize(PreparedStatement stmt, int columnNumber)
      throws SQLException;
   abstract void set_bereadyforoldfile();

}  // class FieldHandler
