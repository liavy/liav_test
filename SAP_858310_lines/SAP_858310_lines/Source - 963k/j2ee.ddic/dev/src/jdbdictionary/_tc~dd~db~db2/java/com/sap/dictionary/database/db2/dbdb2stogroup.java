package com.sap.dictionary.database.db2;

import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
/**
 * @author d022204
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class DbDb2Stogroup {
	private static Location loc = Logger.getLocation("db2.DbDb2Stogroup");
	private static Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	
	DbDb2Stogroup(DbFactory factory) {
	}
	private static boolean checkVcat = false;
	private static Hashtable connections = new Hashtable();
	public static String getStogroup(Connection con) {
		loc.entering("getStogroup");
		String stmtTxt = null;
		try {
			if (con == null)
				return "SYSDEFLT";
			if (connections.containsKey(con))
							return ((String) connections.get(con));
			String stogroup = null;
			// try to find stogroup with same name as schema
			// created by schema (<sapsidnn> )
			// this is the normal case
			String schema = DbDb2Environment.getSchema(con);
			stmtTxt =
				"SELECT NAME  FROM "
					+ " SYSIBM.SYSSTOGROUP "
					+ " WHERE CREATOR = ? "
					+ " AND NAME = ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement pstmt1 = NativeSQLAccess.prepareNativeStatement(con,stmtTxt);
			pstmt1.setString(1, schema);
			pstmt1.setString(2, schema.toUpperCase());
			ResultSet rs = pstmt1.executeQuery();

			if (rs.next()) {
				stogroup = rs.getString(1).trim();
			}
			rs.close();
			pstmt1.close();

			// if no stogroup found according to naming conventions
			// try to find any stogroup created by schema 
			if (stogroup == null) {
				stmtTxt =
					"SELECT NAME  FROM "
						+ " SYSIBM.SYSSTOGROUP "
						+ " WHERE CREATOR = ? "
						+ " ORDER BY NAME "
						+ DbDb2Environment.fetch_first_row
						+ DbDb2Environment.optimize_for_one_row
						+ DbDb2Environment.fetch_only_with_ur;
				pstmt1 = NativeSQLAccess.prepareNativeStatement(con,stmtTxt);
				pstmt1.setString(1, schema.toUpperCase());
				rs = pstmt1.executeQuery();
 
				if (rs.next()) {
					stogroup = rs.getString(1).trim();
				}
				rs.close();
				pstmt1.close();
			}

			// if no stogroup found created by schema 
			// try to find any stogroup 
			// according to naming conventions <sapsidxx>
			// created by different schema
			if ((stogroup == null) && (schema.length() >= 6)) {
				stmtTxt =
					"SELECT NAME  FROM "
						+ " SYSIBM.SYSSTOGROUP "
						+ " WHERE NAME LIKE ? "
						+ " ORDER BY NAME "
						+ DbDb2Environment.fetch_first_row
						+ DbDb2Environment.optimize_for_one_row
						+ DbDb2Environment.fetch_only_with_ur;
				pstmt1 = NativeSQLAccess.prepareNativeStatement(con,stmtTxt);
				pstmt1.setString(1, schema.toUpperCase().substring(0, 6) + "%");
				rs = pstmt1.executeQuery();

				if (rs.next()) {
					stogroup = rs.getString(1).trim();
				}
				rs.close();
				pstmt1.close();
			}

			// else try to find stogroup SAPJ 
			if (stogroup == null) {
				stmtTxt =
					"SELECT NAME  FROM "
						+ " SYSIBM.SYSSTOGROUP "
						+ " WHERE NAME  = ? "
						+ DbDb2Environment.fetch_first_row
						+ DbDb2Environment.optimize_for_one_row
						+ DbDb2Environment.fetch_only_with_ur;
				pstmt1 = NativeSQLAccess.prepareNativeStatement(con,stmtTxt);
				pstmt1.setString(1, "SAPJ");
				rs = pstmt1.executeQuery();

				if (rs.next()) {
					stogroup = rs.getString(1).trim();
				}
				rs.close();
				pstmt1.close();
			}

			if (checkVcat && (stogroup != null)) {
				stmtTxt =
					"SELECT B.NAME, A.VCATNAME  FROM "
						+ " SYSIBM.SYSSTOGROUP A, SYSIBM.SYSSTOGROUP B "
						+ " WHERE A.CREATOR = ? "
						+ " AND A.NAME  = ? "
						+ " AND A.VCATNAME  =  B.VCATNAME"
						+ " AND B.NAME  <>  A.NAME"
						+ DbDb2Environment.optimize_for_one_row
						+ DbDb2Environment.fetch_only_with_ur;
				pstmt1 = NativeSQLAccess.prepareNativeStatement(con,stmtTxt);
				pstmt1.setString(1, schema);
				pstmt1.setString(2, stogroup);
				rs = pstmt1.executeQuery();

				while (rs.next()) {
					Object[] arguments =
						{ stogroup, rs.getString(2), rs.getString(1)};
					cat.warningT(loc,
						"getStogroup: stogroup {0} has same high level qualifier {1} as {2}",
						arguments);
				}
				rs.close();
				pstmt1.close();
			}
			connections.put(con, stogroup);			
			loc.exiting();
			if (stogroup == null)
				return "SYSDEFLT"; 
			return stogroup;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex,stmtTxt)};
			cat.errorT(loc,"getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			return "SYSDEFLT"; // return default what else? 
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc,"getDatabaseNameViaDb: {0}", arguments);
			loc.exiting();
			return "SYSDEFLT"; // return default what else? 
		}
	}
}
