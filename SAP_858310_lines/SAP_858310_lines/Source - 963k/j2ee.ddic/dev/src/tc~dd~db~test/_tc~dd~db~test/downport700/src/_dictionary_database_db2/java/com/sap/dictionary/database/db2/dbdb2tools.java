package com.sap.dictionary.database.db2;

/**
 * @author d000269
 *
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sap.dictionary.database.dbs.DbTools;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JddException;

public class DbDb2Tools extends DbTools {

	private static Location loc = Logger.getLocation("db2.DbDb2Table");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	public DbDb2Tools(DbFactory factory) {

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

		try {

			Statement stmt =
				NativeSQLAccess.createNativeStatement(
					getFactory().getConnection());
			stmt.execute(
				"RENAME TABLE \""
					+ sourceName.toUpperCase()
					+ "\" TO \""
					+ destinationName.toUpperCase()
					+ "\"");
			stmt.close();
		} catch (SQLException sqlex) {

			int errcode = sqlex.getErrorCode();
			ExType xt;

			Object[] arguments =
				{ sourceName, destinationName, sqlex.getMessage()};
			cat.errorT(loc, "renameTable({0},{1}) failed: {2}", arguments);
			loc.exiting();

			switch (errcode) {

				case -204 :
					xt = ExType.NOT_ON_DB;
					break;
				case -601 :
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
	   * Checks what kind of tablelike database object corresponds to name. It is checked
	   * if we have an alias or a view on database with the given name. If this is the case the result 
	   * is delivered as DbTools.KindOfTableLikeDbObject. In all other cases (including object 
	   * is a table on database or object does not exist at all) the return value is null.
	   * @param name Name of object to check
	   * @return DbTools.KindOfTableLikeDbObject.VIEW, if object is a view on database,
	   *         DbTools.KindOfTableLikeDbObject.ALIAS, if object is an Alias on database,
	   *         null in all other cases
	   * @exception JddException is thrown if error occurs during analysis        
	  **/
	public int getKindOfTableLikeDbObject(
		String tableName)
		throws JddException {
		loc.entering(cat, "getKindOfTableLikeDbObject");

		String tabType = "Unknown";
		int tableObjType = DbTools.TABLE;
		Connection con = this.getFactory().getConnection();

		try {
			PreparedStatement pstmt =
				NativeSQLAccess.prepareNativeStatement(
					con,
					"SELECT TYPE FROM SYSIBM.SYSTABLES WHERE NAME = ? AND CREATOR = CURRENT SQLID");
			pstmt.setString(1, tableName.toUpperCase());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				tabType = rs.getString(1);

				if (tabType.equals("T")) {
					tabType = "Table";
				} else if (tabType.equals("X")) {
					tabType = "Auxiliary table";
				} else if (tabType.equals("M")) {
					tabType = "Materialized query table";
				} else if (tabType.equals("G")) {
					tabType = "Created global temporary table";	
				} else if (tabType.equals("A")) {
					tabType = "Alias";
					tableObjType = DbTools.ALIAS;
				} else if (tabType.equals("V")) {
					tabType = "View";
					tableObjType = DbTools.VIEW;
				}
			}
			rs.close();
			pstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { tableName, ex.getMessage()};
			cat.errorT(loc, "type check for table {0} failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		Object[] arguments = { tableName, tabType };
		cat.infoT(loc, "database object type for {0} is {1}", arguments);
		loc.exiting();

		return tableObjType;
	}
}