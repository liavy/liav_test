
package com.sap.sl.util.cvers.impl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.sap.sl.util.logging.api.SlUtilLogger;

// import com.sap.sql.catalog.Table;
// import com.sap.sql.jdbc.common.CommonConnection;
// import com.sap.sql.services.OpenSQLServices;

/*
 * Created on 29.01.2004
 * @author d000706
 *
 * creates a table desciption (and returns an already existing one if possible
 */
public class TableDescriptionFactory
{
  /**
   *  The TCSFactory instance (singleton)
   */
  public static TableDescriptionFactory instance = null;

  private static final SlUtilLogger log = SlUtilLogger.getLogger(TableDescriptionFactory.class.getName());

  private TableDescriptionFactory()
  {
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
  
//  public TableDescription getTableDescriptionNew (Connection connection, String tablename) throws SQLException
//  {
//  	long started = System.currentTimeMillis();
//	try {
//		if (currentTableDescription == null || !(currentTableDescription.getName().equals(tablename)))
//		{
////			System.gc();
////			Runtime rt = Runtime.getRuntime();
////			long mem1 = Runtime.getRuntime().freeMemory();
////			System.out.println ("after  GC  free "+mem1+" max "+rt.maxMemory()+" total "+rt.totalMemory());
//		  	
//		log.debug ("currentTableDescription = "+currentTableDescription);
//		  currentTableDescription = new TableDescription(tablename);
//      
//		  DictionaryReader dr = OpenSQLServices.getDictionaryReader(connection);
//		  if (!dr.existsTable(tablename)) {
//			log.warning ("getTableDescription, table "+tablename+" does not exist.");
//			return null;
//		  }
//		  
////		  long mem2 = Runtime.getRuntime().freeMemory();
////		  System.out.println ("after load free "+mem2+" diff "+(mem1-mem2)+" max "+rt.maxMemory()+" total "+rt.totalMemory());
//		  	
//		  
//		  Table table = dr.getTable(tablename);
//		  int numberOfColumns = table.getColumnCnt();
//		  if (numberOfColumns <= 0)
//			throw new SQLException("no columns found for table "+tablename);
//
//		  String cname;
//		  int ctype;
//		  long clen;
//      
//		  for (int i = 0; i < numberOfColumns; i++)
//		  {
//			Column col = table.getColumn(i+1);
//			cname = col.getName();
//			ctype = col.getJdbcType();
//			clen = col.getSize();
//			currentTableDescription.addFieldDescription(new FieldDescription(cname,ctype,clen));
//		  }
//		} else {
//			log.debug ("getTableDescription for "+tablename+" already exists");
//		}
//		return currentTableDescription;
//	} finally {
//		long finished = System.currentTimeMillis();
//		log.debug ("getTableDescription for "+tablename+" took "+(finished - started)+" ms");
//	}
//  }
  
  public TableDescription getTableDescription(Connection connection, String tablename) throws SQLException {
 
 //		if (connection.getClass().getName().equals("com.sap.sql.jdbc.common.CommonConnectionImpl"))
//		  return (getTableDescription2(connection,this.mystrip(tablename)));
 		
	TableDescription currentTableDescription = null;
		  PreparedStatement ps = null;
		try
		{
//			System.gc();
//			Runtime rt = Runtime.getRuntime();
//			long mem1 = Runtime.getRuntime().freeMemory();
//			System.out.println ("after  GC  free "+mem1+" max "+rt.maxMemory()+" total "+rt.totalMemory());

		currentTableDescription = new TableDescription(tablename);
      
		// read the fields and store the found info into the table description
		ps = connection.prepareStatement("SELECT * FROM "+tablename);
      
		// there's an oracle bug, so that I have to call execute() before I can call getMetaData()
		// So I try it twice: first without execute(), and then (in case of failure) with preceeding execute()
      
		ResultSetMetaData rsmd = null;
			
		try
			  {
				  rsmd = ps.getMetaData();
			  }
			  catch (SQLException e)
			  {
//				  $JL-EXC$
				  rsmd = null;
				
				  // For Oracle version 9.2.0.7 there is no exception raised.
				  // Instead just a null is returned. For older versions an exception is raised.
				  // Oracle docu says either an exception or null is returned.
			  }
      
			  if (rsmd == null) {
				  ps.clearParameters();
				  ps = connection.prepareStatement("SELECT * FROM "+tablename);
				  ps.execute();  // if this is too slow, it can be optimzed with hints, e.g. on Oracle: SELECT /* FIRST_ROWS(1) */ * FROM mytable;
				  rsmd = ps.getMetaData();
			  }

			if (rsmd == null) {
			  log.warning ("getTableDescription, rsmd == null, table = "+tablename);
			  return null;
			}
			int numberOfColumns = rsmd.getColumnCount();
	      
			if (numberOfColumns <= 0)
			  throw new SQLException("old jdbc driver => problem in java.sql.ResultSetMetaData.getColumnCount()");
	
			String cname;
			int ctype;
			int clen;
	      
//			long mem2 = Runtime.getRuntime().freeMemory();
//			System.out.println ("after load free "+mem2+" diff "+(mem1-mem2)+" max "+rt.maxMemory()+" total "+rt.totalMemory());
		  	
			for (int i = 0; i < numberOfColumns; i++)
			{
			  //these ugly methods start counting with 1 instead of 0 ... :-(
			  cname = rsmd.getColumnName(i+1);
			  ctype = rsmd.getColumnType(i+1);
	        
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
	        
			  currentTableDescription.addFieldDescription(new FieldDescription(cname,ctype,clen));
			}
	      
			ps.close();
		}
		catch (Exception e)
		{
		  currentTableDescription = null;
		  log.info (e.getMessage());
		  throw new SQLException (e.getMessage());
		}
		catch (Throwable e)
		{
		  currentTableDescription = null;
		  log.info (e.getMessage());
		  throw new SQLException (e.getMessage());
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
      
	  return currentTableDescription;
	}
  
}
