package com.sap.archtech.archconn.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JdbcUtils
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.mbeans.JdbcUtils");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Category");
  
  private JdbcUtils()
  {
  }
  
  public static Connection getConnection(DataSource dataSource) throws SQLException
  {
    Connection conn = dataSource.getConnection();
    conn.setAutoCommit(false);
    return conn;
  }
  
  public static void closeJdbcResources(Connection conn, PreparedStatement... preparedStatements)
  {
  	for(PreparedStatement stmt : preparedStatements)
  	{
  		if(stmt != null)
  		{
  			try
  			{
  				stmt.close();
  			}
  			catch(SQLException e)
  			{
  				cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
  			}
  		}
    }
    if(conn != null)
    {
      try
      {
        conn.close();
      }
      catch(SQLException e)
      {
        cat.logThrowableT(Severity.WARNING, loc, "Closing the database connection failed", e);
      }
    }
  }
  
  public static Date timestamp2Date(Timestamp timestamp)
  {
    if(timestamp != null)
    {
      return new Date(timestamp.getTime());
    }
    return null;
  }
}
