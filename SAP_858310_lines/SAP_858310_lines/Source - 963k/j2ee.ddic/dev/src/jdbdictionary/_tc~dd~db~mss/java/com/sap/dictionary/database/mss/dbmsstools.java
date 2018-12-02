package com.sap.dictionary.database.mss;

import java.sql.*;


import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTools;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.*;

/**
 * @author d000312
 *	
 */
public class DbMssTools extends DbTools {

	private static Location loc = Logger.getLocation("mss.DbMssTable");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	

	public DbMssTools(DbFactory factory) {
		super(factory);
	}

	/**
	* Renames a table on the database. If no exception is send, the table
	* could be renamed.
	* @param sourceName - current name of table
	* @param destinationName - new name of table
	* @exception JddException - The following error-situations should be
	*                  distinguished by the exception's ExType:
	*            ExType.NOT_ON_DB: Source-table does not exist on database
	*            ExType.EXISTS_ON_DB: Destination table already exists. 
	*            Every other error should be send with ExType.SQL_ERROR or
	*            ExType.OTHER.
	**/
	public void renameTable(String sourceName, String destinationName)
		throws JddException {

		loc.entering("renameTable");
		Connection con = getFactory().getConnection();

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			dstmt.execute(
				"exec sp_rename '"
					+ sourceName
					+ "', '"
					+ destinationName
					+ "'");
			dstmt.close();
		} catch (SQLException sqlex) {
			int errcode = sqlex.getErrorCode();
			ExType xt;

			Object[] arguments =
				{ sourceName, destinationName, sqlex.getMessage()};
			cat.errorT(loc, "renameTable({0},{1}) failed: {2}", arguments);
			loc.exiting();

			switch (errcode) {
				case 15225 :
					xt = ExType.NOT_ON_DB;
					break;
				case 15335 :
					xt = ExType.EXISTS_ON_DB;
					break;
				default :
					xt = ExType.SQL_ERROR;
					break;
			}

			throw new JddException(xt, sqlex.getMessage());
		} catch (Exception ex) {
			Object[] arguments =
				{ sourceName, destinationName, ex.getMessage()};
			cat.errorT(loc, "renameTable({0},{1}) failed: {2}", arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments = { sourceName, destinationName };
		cat.infoT(loc, "renameTable: renamed {0} to {1}", arguments);
		loc.exiting();

		return;

	}

        /**
         * Examines if a given table name is an alias.
         * This test is currently a bit fuzzy: since we're using in the shadow upgrade
         * views instead of 'real' aliases (synonymes) we're just checking if the object 
         * is a view. 
         * @param tableName - current name of table
         * @exception JddException - The following error-situations should be
         *                  distinguished by the exception's ExType:
         *            ExType.SQL_ERROR: Object with tableName could not be examined 
         **/
        public boolean isAlias(String tableName)
                             throws JddException {
  		boolean res = false;
		boolean doesntExist = true;

		Connection con = getFactory().getConnection();

		loc.entering("isAlias");

		/* alias: view or synonym */	
		String statement = "SELECT xtype from sysobjects where id = object_id(user + '.' + '" + tableName + "') and " +
                                   "xtype in ('U','V','SN')";
		
        	try {
  	    		PreparedStatement statementObject = 
  				 NativeSQLAccess.prepareNativeStatement(con, statement);	  
  	    		ResultSet result = statementObject.executeQuery();
  	    		if (result.next()) { 
				res = (result.getString(1).trim().equalsIgnoreCase("U") == false); 
				doesntExist = false; 
			} 
  	    		result.close(); 
  	    		statementObject.close();
  	  	} 
  	  	catch (SQLException ex) {
			Object[] arguments = { tableName, ex.getMessage() };
			cat.errorT(loc, "isAlias({0}) failed: {1}", arguments);
			loc.exiting();

  	  		JddException.log(ex,cat,Severity.ERROR,loc);
	  		throw new JddException(ExType.SQL_ERROR, ex.getMessage());
		}
                
                if (doesntExist == true) {
			Object[] arguments = { tableName };
			cat.infoT(loc, "isAlias({0}): there exists neither table, view nor synonym with that name");
                }

		loc.exiting();

		return res;   	  	
    	}

	public int getKindOfTableLikeDbObject(String name) 
                 throws JddException{
		String res = null;
		
		Connection con = getFactory().getConnection();

		loc.entering("isAlias");

		/* alias: view or synonym */	
		String statement = "SELECT xtype from sysobjects where id = object_id(user + '.' + '" + name + "') and " +
                                   "xtype in ('V','SN') ";
                

        	try {
  	    		PreparedStatement statementObject = 
  				 NativeSQLAccess.prepareNativeStatement(con, statement);	  
  	    		ResultSet result = statementObject.executeQuery();
  	    		if (result.next()) { 
				res = result.getString(1).trim();
			} 
  	    		result.close(); 
  	    		statementObject.close();
  	  	} 
  	  	catch (SQLException ex) {
			Object[] arguments = { name, ex.getMessage() };
			cat.errorT(loc, "isAlias({0}) failed: {1}", arguments);
			loc.exiting();

  	  		JddException.log(ex,cat,Severity.ERROR,loc);
	  		throw new JddException(ExType.SQL_ERROR, ex.getMessage());
		}
                
		loc.exiting();

		if (res == null)
			return DbTools.TABLE;
		if (res.equalsIgnoreCase("V"))
			return DbTools.VIEW;
		else
			return DbTools.ALIAS; 
  	}

	/**
	 * Retrieve the SQL Server release of the connected database
	 * @param con  current connection
	 * @return     SQL Server release (major)
	 * @throws JddException
	 */
	private long retrieveSQLServerRelease(Connection con) {
		long rel = 8;

		try {
			Statement schStmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet rs = schStmt.executeQuery("select (@@microsoftversion / 65536) / 256");
			rs.next();
			rel = rs.getInt(1);
			rs.close();
			schStmt.close();
		} catch (Exception ex) {
                        loc.entering("retrieveSQLServerRelease");
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "retrieveSQLServerRelease failed: {0}", arguments);
			loc.exiting();
		}
		
		return rel;
	}

}
