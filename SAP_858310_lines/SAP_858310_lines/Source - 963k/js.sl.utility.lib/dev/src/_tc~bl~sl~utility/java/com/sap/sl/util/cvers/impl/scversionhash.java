
package com.sap.sl.util.cvers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sap.sl.util.cvers.api.CVersCreateException;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  This class covers hash numbering for name & vendor of SCVersion tables
 * 
 *  NOTE: This file is currently not used !
 *  SC name and vendor are part of each DB table without being a key field.
 *  Table BC_SCNAMES is not yet used and therefore not created.
 *
 *@author     JM
 *@created    12.03.2005
 *@version    1.0
 */

public class SCVersionHash {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													SCVersionHash.class.getName());

	private final static String FINDBYREALKEYSTRING =
		"SELECT HASHNUMBER, VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME "+
		" FROM BC_COMPVERS WHERE COMPID = ? AND COMPONENTTYPE = ?" +
		" AND SUBSYSTEM = ?";

	private final static String INSERTSTRING =
		"INSERT INTO BC_COMPVERS " +
		"(COMPID, HASHNUMBER, COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME, " +
		"LOCATION, COUNTER, "+
		"SCVENDOR, SCNAME, SAPRELEASE, SERVICELEVEL, PATCHLEVEL, DELTAVERSION, "+
		"UPDATEVERSION, APPLYTIME, SCELEMENTTYPEID, SPELEMENTTYPEID, "+
		"SPNAME, SPVERSION, SERVERTYPE, SERVERNAME, CHANGENUMBER,PROJECTNAME)  VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private Integer hashindex = null;
	
	public SCVersionHash (Connection connection, String name, String vendor) throws CVersCreateException {
		
		hashindex = findHashIndex (connection, name, vendor);
		if (hashindex == null) {
			hashindex = insertHashIndex (connection, name, vendor);
		}
	}
	
	public String getIndex () {
		return String.valueOf(hashindex.intValue());
	}
	
	private Integer findHashIndex (Connection connection, String name, String vendor) {

		log.entering("findHashIndex");
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("SELECT HASHINDEX FROM BC_SCNAMES WHERE NAME = ? AND VENDOR = ?");			
			statement.setString(1, name);
			statement.setString(2, vendor);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				return null;
			}
			int result = resultSet.getInt (1);
			log.debug ("findHashIndex = "+result+" for "+name+" "+vendor);
			return new Integer (result);
		} catch (Exception se) {
			//$JL-EXC$
			return null;
		} finally {
			log.exiting("findHashIndex");
		}
	}

	private Integer findMaxHashIndex (Connection connection) {

		log.entering("findMaxHashIndex");
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("SELECT MAX(HASHINDEX) FROM BC_SCNAMES");			
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				return new Integer (0);
			}
			int result = resultSet.getInt (1);
			log.debug ("findMaxHashIndex = "+result);
			return new Integer (result);
		} catch (Exception se) {
//			$JL-EXC$
			return new Integer (0);
		} finally {
			log.exiting("findMaxHashIndex");
		}
	}

	private Integer insertHashIndex (Connection connection, String name, String vendor) 
               		 		throws CVersCreateException {

		log.entering("insertHashIndex");
		Integer hashindex = findMaxHashIndex (connection);
		PreparedStatement insertStatement = null;
		try {
			insertStatement = connection.prepareStatement ("INSERT INTO BC_SCNAMES (HASHINDEX,NAME,VENDOR) VALUES (?,?,?)");			
			hashindex = new Integer (hashindex.intValue()+1);
			insertStatement.clearParameters();
			insertStatement.setInt(1, hashindex.intValue());
			insertStatement.setString(2, name);
			insertStatement.setString(3, vendor);
			if (insertStatement.executeUpdate() != 1) {
				String msg = "insertHashIndex: can not insert "+hashindex.intValue()+" for "+name+" "+vendor;
				log.error(msg);
				throw new CVersCreateException(msg);
			}
			log.exiting("insertHashIndex");
			return hashindex;
		} catch(SQLException se) {
			String msg = "insertHashIndex: SQLException, can not insert "+hashindex.intValue()+" for "+name+" "+vendor + " "+se.getMessage();
			log.error(msg, se);
			throw new CVersCreateException(msg);
		}
	}
}
