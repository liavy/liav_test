package com.sap.sl.util.dbaccess.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

// import com.sap.sql.catalog.Table;
// import com.sap.sql.jdbc.common.CommonConnection;
// import com.sap.sql.services.OpenSQLServices;

/*
 * Created on 29.01.2004
 * @author d000706
 *
 * creates a table desciption (and returns an already existing one if possible
 */
class TableDescriptionFactory
{
  /**
   *  The TCSFactory instance (singleton)
   */
  static TableDescriptionFactory instance = null;
  private TableDescription currentTableDescription = null;

  private TableDescriptionFactory()
  {
  }
  
  public void reset()
  {
	currentTableDescription = null;
	return;
  }

  /**
   *  Gets the TableDescriptionFactory instance (singleton)
   *
   *@return    The TableDescriptionFactory instance
   */
  public final static synchronized TableDescriptionFactory getInstance()
  {
    if (instance == null) {
      instance = new TableDescriptionFactory();
    }
    return instance;
  }
  
  public TableDescription getTableDescription(Connection connection, String tablename) throws SQLException
  {
//    if (connection.getClass().getName().equals("com.sap.sql.jdbc.common.CommonConnectionImpl"))
//      return (getTableDescription2(connection,this.mystrip(tablename)));
 
    if (currentTableDescription == null || !(currentTableDescription.getName().equals(tablename)))
    {
      try
      {
      currentTableDescription = new TableDescription(tablename);
      
      // read the fields and store the found info into the table description
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM "+tablename);
      
      // there's an oracle bug, so that I have to call execute() before I can call getMetaData()
      // So I try it twice: first without execute(), and then (in case of failure) with preceeding execute()
      
      ResultSetMetaData rsmd = null;
			
      try
			{
				rsmd = ps.getMetaData();
			}
			catch (SQLException e)
			{
        ps.execute();  // if this is too slow, it can be optimzed with hints, e.g. on Oracle: SELECT /* FIRST_ROWS(1) */ * FROM mytable;
        rsmd = ps.getMetaData();
			}
      
      int numberOfColumns = rsmd.getColumnCount();
      
      if (numberOfColumns <= 0)
        throw new SQLException("old jdbc driver => problem in java.sql.ResultSetMetaData.getColumnCount()");

      String cname;
      int ctype;
      int clen;
      boolean cnullable;
      
      for (int i = 0; i < numberOfColumns; i++)
      {
        //these ugly methods start counting with 1 instead of 0 ... :-(
        cname = rsmd.getColumnName(i+1);
        ctype = rsmd.getColumnType(i+1);
        cnullable = (rsmd.isNullable(i+1) == ResultSetMetaData.columnNullable);
        
        switch (ctype)
        {
        case Types.BINARY:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
          clen = rsmd.getPrecision(i+1);
          break;
        case Types.BLOB:
        case Types.CLOB:
          clen = Integer.MAX_VALUE;
          break;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
          clen = 0;
          break;
        default:
          clen = rsmd.getScale(i+1);
          break;
        }
        
        currentTableDescription.addFieldDescription(new FieldDescription(cname,ctype,clen,cnullable));
      }
      
      ps.close();
      }
      catch (SQLException e)
      {
        currentTableDescription = null;
        throw e;
      }
    }
      
    return currentTableDescription;
  }
  
  /*
  private TableDescription getTableDescription2(Connection connection, String tablename) throws SQLException
  {
    Table td = OpenSQLServices.getCatalogReader((CommonConnection)connection).getTable(tablename);
    
    if (td == null)
    {
      currentTableDescription = null;
      throw new SQLException("table "+tablename+" doesn't exist in JDDI catalog");
    }
      
    if (currentTableDescription == null || !(currentTableDescription.getName().equals(tablename)))
    {
      try
      {
        currentTableDescription = new TableDescription(tablename);
      
        int numberOfColumns = td.getColumnCnt();
        
        if (numberOfColumns <= 0)
          throw new SQLException("old jdbc driver => problem in java.sql.ResultSetMetaData.getColumnCount()");
  
        for (int i = 0; i < numberOfColumns; i++)
        {
          //this ugly class starts counting with 1 instead of 0 ... :-(
          currentTableDescription.addFieldDescription(new FieldDescription(td.getColumn(i+1).getName(),
                                                                           td.getColumn(i+1).getJdbcType(),
                                                                           (int)td.getColumn(i+1).getSize()));
        }
      }
      catch (SQLException e)
      {
        currentTableDescription = null;
        throw e;
      }
    }
      
    return currentTableDescription;
  }
  
  private String mystrip(String name)
  {
    return name.substring(1,name.length()-1);
  }
  */
}
