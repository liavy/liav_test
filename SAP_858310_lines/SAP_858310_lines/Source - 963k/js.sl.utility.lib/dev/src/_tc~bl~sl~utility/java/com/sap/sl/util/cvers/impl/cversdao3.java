/*
 * updated from perforce CVersDao#5
 */
 
package com.sap.sl.util.cvers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.CVersCreateException;
import com.sap.sl.util.cvers.api.CVersFinderException;
import com.sap.sl.util.cvers.api.CVersSQLException;
import com.sap.sl.util.cvers.api.SyncResultIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  COMPVERS Database Access Object:
 *  The central class for reading and writing table COMPVERS from 6.30 SP11
 *
 *@author     JM
 *@created    18.01.2005
 *@version    1.0
 */

public class CVersDao3 implements CVersDaoIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CVersDao3.class.getName());

	private static String FN_COMPID = "COMPID";	// integer field
	private static int    FL_COMPID = 0;
	
	private static String FN_HASHNUMBER = "HASHNUMBER";	// integer field
	private static int    FL_HASHNUMBER = 0;
	
	private static String FN_TYPE = "TYPE";
	private static int    FL_TYPE = 10;
	
	private static String FN_SUBSYSTEM = "SUBSYSTEM";
	private static int    FL_SUBSYSTEM = 10;
	
	private static String FN_NAME = "NAME";
	private static int    FL_NAME = 256;
	
	private static String FN_VENDOR = "VENDOR";
	private static int    FL_VENDOR = 256;
	
	private static String FN_LOCATION = "LOCATION";
	private static int    FL_LOCATION = 20;
	
	private static String FN_COUNTER = "COUNTER";
	private static int    FL_COUNTER = 40;
	
	private static String FN_SCVENDOR = "SCVENDOR";
	private static int    FL_SCVENDOR = 256;
	
	private static String FN_SCNAME = "SCNAME";
	private static int    FL_SCNAME = 256;
	
	private static String FN_RELEASE = "RELEASE";
	private static int    FL_RELEASE = 10;
	
	private static String FN_SERVICELEVEL = "SERVICELEVEL";
	private static int    FL_SERVICELEVEL = 10;
	
	private static String FN_PATCHLEVEL = "PATCHLEVEL";
	private static int    FL_PATCHLEVEL = 10;
	
	private static String FN_UPDATEVERSION = "UPDATEVERSION";
	private static int    FL_UPDATEVERSION = 20;
	
	private static String FN_DELTAVERSION = "DELTAVERSION";
	private static int    FL_DELTAVERSION = 1;
	
	private static String FN_APPLYTIME = "APPLYTIME";
	private static int    FL_APPLYTIME = 20;
	
	private static String FN_SCELEMENTYPEID = "SCELEMENTYPEID";
	private static int    FL_SCELEMENTYPEID = 256;
	
	private static String FN_SPELEMENTYPEID = "SPELEMENTYPEID";
	private static int    FL_SPELEMENTYPEID = 256;
	
	private static String FN_SPNAME = "SPNAME";
	private static int    FL_SPNAME = 256;
	
	private static String FN_SPVERSION = "SPVERSION";
	private static int    FL_SPVERSION = 64;
	
	private static String FN_SERVERNAME = "SERVERNAME";
	private static int    FL_SERVERNAME = 256;
	
	private static String FN_CHANGENUMBER = "CHANGENUMBER";
	private static int    FL_CHANGENUMBER = 10;
	
	private static String FN_PROJECTNAME = "PROJECTNAME";
	private static int    FL_PROJECTNAME = 256;
	
	private static String FN_SERVERTYPE = "SERVERTYPE";
	private static int    FL_SERVERTYPE = 10;
	
	private final static String FINDBYREALKEYSTRING =
		"SELECT HASHNUMBER, VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME "+
		" FROM BC_COMPVERS WHERE COMPID = ? AND COMPONENTTYPE = ?" +
		" AND SUBSYSTEM = ?";
	private final static String UPDATESTRING =
		"UPDATE BC_COMPVERS SET VENDOR = ?, NAME = ?, " +
		"LOCATION = ?, COUNTER = ?, SCVENDOR = ?, "+
		"SCNAME = ?, SAPRELEASE = ?, SERVICELEVEL = ?, PATCHLEVEL = ?, DELTAVERSION = ?, "+
		"UPDATEVERSION = ?, APPLYTIME = ?, SCELEMENTTYPEID = ?, "+
		"SPELEMENTTYPEID = ?, SPNAME = ?, SPVERSION = ?, SERVERTYPE = ?, "+
		"SERVERNAME = ?, CHANGENUMBER = ?, PROJECTNAME = ? " +
		"WHERE COMPID = ? AND HASHNUMBER = ? AND COMPONENTTYPE = ? " +
		"AND SUBSYSTEM = ?"; 
	private final static String INSERTSTRING =
		"INSERT INTO BC_COMPVERS " +
		"(COMPID, HASHNUMBER, COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME, " +
		"LOCATION, COUNTER, "+
		"SCVENDOR, SCNAME, SAPRELEASE, SERVICELEVEL, PATCHLEVEL, DELTAVERSION, "+
		"UPDATEVERSION, APPLYTIME, SCELEMENTTYPEID, SPELEMENTTYPEID, "+
		"SPNAME, SPVERSION, SERVERTYPE, SERVERNAME, CHANGENUMBER,PROJECTNAME)  VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String FINDBYPKSTRING =
		"SELECT  VENDOR, NAME, LOCATION, COUNTER, SCVENDOR, SCNAME," +
		" SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, SERVERTYPE, " +
 		" SERVERNAME, CHANGENUMBER, PROJECTNAME"+
		" FROM BC_COMPVERS WHERE COMPID = ? AND HASHNUMBER = ? AND " +
		"COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	private final static String FINDALLSTRING =
		"SELECT COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME "+
		" FROM BC_COMPVERS";
	private final static String FINDBYCOMPTYPESTRING =
		"SELECT SUBSYSTEM, VENDOR, NAME, " +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME "+
		" FROM BC_COMPVERS WHERE COMPONENTTYPE = ?";
	private final static String FINDBYTYPEANDSUBSYSSTRING =
		"SELECT VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME "+
		" FROM BC_COMPVERS WHERE COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	private final static String REMOVESTRING =
		"DELETE FROM BC_COMPVERS WHERE COMPID = ?" +
		" AND HASHNUMBER = ? AND COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	
	private DataSource dataSource;

	public CVersDao3 (DataSource _dataSource) {
		this.dataSource = _dataSource;
	}
	
	public void writeSCHeader (ComponentElementIF cVersElement)
				throws CVersAccessException {

		error ("writeCVers0");
	}

	private String truncate (String text, String value, int maxlen) {
		
		if (value != null) {
			if (value.length() > maxlen) {
				log.error ("COMPVERS3: field "+text+" too small for value='"+value+"' len="+value.length()+" > "+maxlen);
				return value.substring(0, maxlen);
			}
		}
		return value;
	}
					
	public void writeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {
		log.entering("writeCVers3");
		CVersPK cVersPK;
		Connection connection = null;
		PreparedStatement updateStatement = null;
		PreparedStatement insertStatement = null;
		PreparedStatement findByRealKeyStatement = null;
		// Create connection and statements
		ComponentElementIF compElement = null;
		try {
			connection = dataSource.getConnection();
			findByRealKeyStatement = connection.prepareStatement(FINDBYREALKEYSTRING);
			updateStatement = connection.prepareStatement(UPDATESTRING);
			insertStatement = connection.prepareStatement(INSERTSTRING);

			// Let's go
			for (int i=0; i < cVersElements.length; i++) {
				// set the applytime
				ComponentFactoryIF compFactory = ComponentFactoryIF.getInstance();
				
				String servicelevel = truncate("SERVICELEVEL", cVersElements[i].getServiceLevel(), 10);
				String location = truncate("LOCATION", cVersElements[i].getLocation(), 40);
				
				if (servicelevel != null) {
					
					// check for special problem where exportSC wrote SP.timestamp
					int pos = servicelevel.indexOf('.');
					if (pos >= 0) {
						servicelevel = servicelevel.substring(0, pos);
					}
				}

				compElement = compFactory.createComponentElement(
								cVersElements[i].getVendor(),
								cVersElements[i].getName(),
								cVersElements[i].getComponentType(),
								cVersElements[i].getSubsystem(),
								location,
								cVersElements[i].getCounter(),
								cVersElements[i].getSCVendor(),
								cVersElements[i].getSCName(),
								cVersElements[i].getRelease(),
								servicelevel,
								cVersElements[i].getPatchLevel(),
								cVersElements[i].getDeltaVersion(),
								cVersElements[i].getUpdateVersion(),
								// time
								CVersTime.getTimeStamp(CVersTime.TS_SEC_FORMAT),
								cVersElements[i].getSCElementTypeID(),
								cVersElements[i].getSPElementTypeID(),
								cVersElements[i].getSPName(),
								cVersElements[i].getSPVersion(),
								cVersElements[i].getServerType(),
								cVersElements[i].getSourceServer (),
								cVersElements[i].getChangeNumber(),
								cVersElements[i].getProjectName()
								);

				log.debug ("writeCVers3: "+compElement.toString ());
				
				//First see if the object already exists
				cVersPK = findPKByRealKey(findByRealKeyStatement,
									  HashKey.defaultHashFunction(
									  	compElement.getVendor(),
									  	compElement.getName()),
									  compElement.getVendor(),
									  compElement.getName(),
									  compElement.getComponentType(),
									  compElement.getSubsystem());
				if (cVersPK == null) {
					//We have to create a new entry
					create(insertStatement, 0, compElement);
				} else {
					//We have to update the correct entry
					save(updateStatement, cVersPK.hashnumber, compElement);
				}
			}
		} catch (SQLException se) {
			String msg = "Error writing new entries into COMPVERS3: (newest tc/SL/utiljddschema deployed?) ";
			if (compElement != null) {
				msg = msg + compElement.toString ();
			} else {
				msg = msg + "null";
			};
			log.error (msg, se);
			throw new CVersSQLException(msg	+ se.toString());
		} finally {
			closeStatement(updateStatement);
			closeStatement(insertStatement);
			closeStatement(findByRealKeyStatement);
			closeConnection(connection);
			log.exiting("writeCVers3");
		}
	}

	protected void save(PreparedStatement updateStatement, int hashnumber,
				ComponentElementIF compElem) throws SQLException {
		this.save(updateStatement, hashnumber, 
					compElem.getVendor(), compElem.getName(),
					compElem.getComponentType(), compElem.getSubsystem(),
					compElem.getLocation(), compElem.getCounter(),
					compElem.getSCVendor(), compElem.getSCName(),
					compElem.getRelease(),
					compElem.getServiceLevel(), 
					compElem.getPatchLevel(),
					compElem.getDeltaVersion(),
					compElem.getUpdateVersion(), compElem.getApplyTime(),
					compElem.getSCElementTypeID(), compElem.getSPElementTypeID(),
					compElem.getSPName(), compElem.getSPVersion(),
					compElem.getServerType(),
					compElem.getSourceServer(),
					compElem.getChangeNumber(),
					compElem.getProjectName());
	}

	private void setStringValue (PreparedStatement updateStatement, String fieldname, int index, String value, int maxlen) throws SQLException {
		
		try {
			if (value != null) {
				if (value.length() > maxlen) {
					log.warning ("setStringValue pos="+index+" "+fieldname+" too small for value='"+value+"' len="+value.length()+" > "+maxlen+" --> value truncated");
					value = value.substring(0, maxlen);
				} else {
					log.debug ("setStringValue pos="+index+" "+fieldname+" to '"+value+"' len="+value.length()+" max="+maxlen);
				}
			} else {
				log.debug ("setStringValue pos="+index+" "+fieldname+" to '"+value+"' max="+maxlen);
			}
			updateStatement.setString(index, value);
		} catch (Exception exc) {
			log.error ("exc3, can not set string field "+index+" "+fieldname+" to '"+value+"'", exc);
			throw new SQLException (exc.getMessage());
		} catch (Throwable thr) {
			log.error ("thr3, can not set string field "+index+" "+fieldname+" to '"+value+"'", thr);
			throw new SQLException (thr.getMessage());
		}
	}
	
	private void setIntValue (PreparedStatement updateStatement, String fieldname, int index, int value) throws SQLException {
		
		try {
			log.debug ("setIntValue pos="+index+" "+fieldname+" to '"+value+"'");
			updateStatement.setInt (index, value);
		} catch (Exception exc) {
			log.error ("exc3, can not set int field "+index+" "+fieldname+" to '"+value+"'", exc);
			throw new SQLException (exc.getMessage());
		} catch (Throwable thr) {
			log.error ("thr3, can not set int field "+index+" "+fieldname+" to '"+value+"'", thr);
			throw new SQLException (thr.getMessage());
		}
	}
	
	protected void save(PreparedStatement updateStatement, int hashnumber,
					 String vendor, String name, String type, String subsystem,
					 String location, String counter, String scVendor,
  					 String scName, String release, String serviceLevel,
  					 String patchLevel,
  					 String deltaVersion, String updateVersion, String applyTime,
  					 String scElementTypeID, String spElementTypeID, String spName,
					 String spVersion,
					 String serverType,
					 String serverName,
					 String changeNumber,
					 String projectName) throws SQLException {
		log.entering("save3");
		// get internal DB key
		int compID = HashKey.defaultHashFunction(vendor, name);
		try {

			updateStatement.clearParameters();
			setStringValue (updateStatement, FN_VENDOR, 1, vendor, FL_VENDOR);
			setStringValue (updateStatement, FN_NAME, 2, name, FL_NAME);
			setStringValue (updateStatement, FN_LOCATION, 3, location, FL_LOCATION);
			setStringValue (updateStatement, FN_COUNTER, 4, counter, FL_COUNTER);
			setStringValue (updateStatement, FN_SCVENDOR, 5, scVendor, FL_SCVENDOR);
			setStringValue (updateStatement, FN_SCNAME, 6, scName, FL_SCNAME);
			setStringValue (updateStatement, FN_RELEASE, 7, release, FL_RELEASE);
			setStringValue (updateStatement, FN_SERVICELEVEL, 8, serviceLevel, FL_SERVICELEVEL);
			setStringValue (updateStatement, FN_PATCHLEVEL, 9, patchLevel, FL_PATCHLEVEL);
			setStringValue (updateStatement, FN_DELTAVERSION, 10, deltaVersion, FL_DELTAVERSION);
			setStringValue (updateStatement, FN_UPDATEVERSION, 11, updateVersion, FL_UPDATEVERSION);
			setStringValue (updateStatement, FN_APPLYTIME, 12, applyTime, FL_APPLYTIME);
			setStringValue (updateStatement, FN_SCELEMENTYPEID, 13, scElementTypeID, FL_SCELEMENTYPEID);
			setStringValue (updateStatement, FN_SPELEMENTYPEID, 14, spElementTypeID, FL_SPELEMENTYPEID);
			setStringValue (updateStatement, FN_SPNAME, 15, spName, FL_SPNAME);
			setStringValue (updateStatement, FN_SPVERSION, 16, spVersion, FL_SPVERSION);
			setStringValue (updateStatement, FN_SERVERTYPE, 17, serverType, FL_SERVERTYPE);
			setStringValue (updateStatement, FN_SERVERNAME, 18, serverName, FL_SERVERNAME);
			setStringValue (updateStatement, FN_CHANGENUMBER, 19, changeNumber, FL_CHANGENUMBER);
			setStringValue (updateStatement, FN_PROJECTNAME, 20, projectName, FL_PROJECTNAME);
			setIntValue (updateStatement, FN_COMPID, 21, compID);
			setIntValue (updateStatement, FN_HASHNUMBER, 22, hashnumber);
			setStringValue (updateStatement, FN_TYPE, 23, type, FL_TYPE);
			setStringValue (updateStatement, FN_SUBSYSTEM, 24, subsystem, FL_SUBSYSTEM);
			
			if (updateStatement.executeUpdate() < 1) {
				String msg = 
				        vendor +
				"," + name +
				"," + location +
				"," + counter +
				"," + scVendor +
				"," + scName +
				"," + release +
				"," + serviceLevel +
				"," + patchLevel +
				"," + deltaVersion +
				"," + updateVersion +
				"," + applyTime +
				"," + scElementTypeID +
				"," + spElementTypeID +
				"," + spName +
				"," + spVersion +
				"," + compID +
				"," + hashnumber +
				"," + type +
				"," + subsystem +
				"," + serverType  +
				"," + serverName +
				"," + projectName + 
				"," + changeNumber;
	      		log.error("Row does not exist: "+msg);
				throw new NoSuchElementException("Row does not exist");
			}
		} catch(SQLException se) {
			String msg = 
					vendor +
			"," + name +
			"," + location +
			"," + counter +
			"," + scVendor +
			"," + scName +
			"," + release +
			"," + serviceLevel +
			"," + patchLevel +
			"," + deltaVersion +
			"," + updateVersion +
			"," + applyTime +
			"," + scElementTypeID +
			"," + spElementTypeID +
			"," + spName +
			"," + spVersion +
			"," + compID +
			"," + hashnumber +
			"," + type +
			"," + subsystem +
			"," + serverType  +
			"," + serverName +
			"," + projectName + 
			"," + changeNumber;
	      	log.error("Error executing SQL UPDATE BC_COMPVERS... "+msg, se);
			throw se;
		}
		log.exiting("save");
	}

	public CVersPK create(PreparedStatement insertStatement, int hashnumber,
				ComponentElementIF compElem)
				throws SQLException, CVersAccessException {
		return this.create(insertStatement, hashnumber, 
							compElem.getVendor(), compElem.getName(),
							compElem.getComponentType(), compElem.getSubsystem(),
							compElem.getLocation(), compElem.getCounter(),
							compElem.getSCVendor(), compElem.getSCName(),
							compElem.getRelease(),
							compElem.getServiceLevel(), 
							compElem.getPatchLevel(),
							compElem.getDeltaVersion(),
							compElem.getUpdateVersion(), compElem.getApplyTime(),
							compElem.getSCElementTypeID(),
							compElem.getSPElementTypeID(),
							compElem.getSPName(), compElem.getSPVersion(),
							compElem.getServerType(),
							compElem.getSourceServer(),
							compElem.getChangeNumber(),
							compElem.getProjectName());
	}

	public CVersPK create(PreparedStatement insertStatement, int _hashnumber,
							String _vendor,
							String _name, String _componentType, String _subsystem,
							String _location, String _counter, String _scVendor,
  							String _scName, String _release, String _serviceLevel,
 							String _patchLevel, 
  							String _deltaVersion, String _updateVersion,
               				String _applyTime, String _scElementTypeID,
               				String _spElementTypeID, String _spName,
               				String _spVersion,
               				String servertype,
               				String servername,
               				String changenumber,
               				String projectname)
               		 		throws SQLException, CVersAccessException {

		log.entering("create");
		// get internal DB key
		int compID = HashKey.defaultHashFunction(_vendor, _name);

		try {
			insertStatement.clearParameters();
			this.setIntValue(insertStatement, FN_COMPID, 1, compID);
			this.setIntValue(insertStatement, FN_HASHNUMBER, 2, _hashnumber);
			setStringValue (insertStatement, FN_TYPE, 3, _componentType, FL_TYPE);
			setStringValue (insertStatement, FN_SUBSYSTEM, 4, _subsystem, FL_SUBSYSTEM);
			setStringValue (insertStatement, FN_VENDOR, 5, _vendor, FL_VENDOR);
			setStringValue (insertStatement, FN_NAME, 6, _name, FL_NAME);
			setStringValue (insertStatement, FN_LOCATION, 7, _location, FL_LOCATION);
			setStringValue (insertStatement, FN_COUNTER, 8, _counter, FL_COUNTER);
			setStringValue (insertStatement, FN_SCVENDOR, 9, _scVendor, FL_SCVENDOR); 
			setStringValue (insertStatement, FN_SCNAME, 10, _scName, FL_SCNAME);
			setStringValue (insertStatement, FN_RELEASE, 11, _release, FL_RELEASE);
			setStringValue (insertStatement, FN_SERVICELEVEL, 12, _serviceLevel, FL_SERVICELEVEL);
			setStringValue (insertStatement, FN_PATCHLEVEL, 13, _patchLevel, FL_PATCHLEVEL);
			setStringValue (insertStatement, FN_DELTAVERSION, 14, _deltaVersion, FL_DELTAVERSION);
			setStringValue (insertStatement, FN_UPDATEVERSION, 15, _updateVersion, FL_UPDATEVERSION);
			setStringValue (insertStatement, FN_APPLYTIME, 16, _applyTime, FL_APPLYTIME);
			setStringValue (insertStatement, FN_SCELEMENTYPEID, 17, _scElementTypeID, FL_SCELEMENTYPEID);
			setStringValue (insertStatement, FN_SPELEMENTYPEID, 18, _spElementTypeID, FL_SPELEMENTYPEID);
			setStringValue (insertStatement, FN_SPNAME, 19, _spName, FL_SPNAME);
			setStringValue (insertStatement, FN_SPVERSION, 20, _spVersion, FL_SPVERSION);
			setStringValue (insertStatement, FN_SERVERTYPE, 21, servertype, FL_SERVERTYPE);
			setStringValue (insertStatement, FN_SERVERNAME, 22, servername, FL_SERVERNAME);
			setStringValue (insertStatement, FN_CHANGENUMBER, 23, changenumber, FL_CHANGENUMBER);
			setStringValue (insertStatement, FN_PROJECTNAME, 24, projectname, FL_PROJECTNAME);
			
			if (insertStatement.executeUpdate() != 1) {
				String msg = compID + "," + _hashnumber + "," + _componentType + "," +
				  _subsystem + "," + _vendor + "," + _name + "," + 
				  _location + "," + _counter + "," + _scVendor + "," + _scName + "," + 
				  _release + "," + _serviceLevel + "," + _patchLevel + "," + _deltaVersion + "," +
				  _updateVersion + "," + _applyTime + "," + _scElementTypeID + "," +
				  _spElementTypeID + "," + _spName  + "," + _spVersion;
				log.error("Error adding row: "+msg);
				throw new CVersCreateException("Error adding row "+msg);
			}
			log.exiting("create");
			return new CVersPK(compID, _hashnumber, _componentType, _subsystem);
		} catch(SQLException se) {
			String msg = compID + "," + _hashnumber + "," + _componentType + "," +
			  _subsystem + "," + _vendor + "," + _name + "," + 
			  _location + "," + _counter + "," + _scVendor + "," + _scName + "," + 
			  _release + "," + _serviceLevel + "," + _patchLevel + "," + _deltaVersion + "," +
			  _updateVersion + "," + _applyTime + "," + _scElementTypeID + "," +
			  _spElementTypeID + "," + _spName  + "," + _spVersion;
			log.error("Error executing SQL INSERT INTO BC_COMPVERS... " +msg, se);
			throw se;
		}
	}

	public ComponentElementIF findByPrimaryKey(CVersPK _cVersPK)
			throws CVersAccessException {

		log.entering("findByPrimaryKey");
		Connection connection = null;
		PreparedStatement statement = null;
		ComponentElementIF compVers = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(FINDBYPKSTRING);			
			statement.setInt(1, _cVersPK.compID);
			statement.setInt(2, _cVersPK.hashnumber);
			statement.setString(3, _cVersPK.componentType);
			statement.setString(4, _cVersPK.subSystem);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				String msg = "COMPVERS - Primary key does not exist: "+_cVersPK.toString();
				log.error(msg);
				throw new CVersFinderException (msg);
			}
			ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
			compVers = factory.createComponentElement(
														resultSet.getString(1),
														resultSet.getString(2),
														_cVersPK.componentType,
														_cVersPK.subSystem,
														resultSet.getString(3),
														resultSet.getString(4),
														resultSet.getString(5),
														resultSet.getString(6),
														resultSet.getString(7),
														resultSet.getString(8),
														resultSet.getString(9),
														resultSet.getString(10),
														resultSet.getString(11),
														resultSet.getString(12),
														resultSet.getString(13),
														resultSet.getString(14),
														resultSet.getString(15),
														resultSet.getString(16),
														resultSet.getString(17),
														resultSet.getString(18),
														resultSet.getString(19),
														resultSet.getString(20)
														);
			return compVers;
		} catch(SQLException se) {
			String message = "Error executing SQL "+ FINDBYPKSTRING+_cVersPK.compID+
			_cVersPK.hashnumber+_cVersPK.componentType+
			_cVersPK.subSystem;
			if (compVers != null) {
				message = message + " " +compVers.toString();
			};
			log.warning (message, se);
			throw new CVersSQLException(message + se.toString());
		} finally {
			closeStatement(statement);
			closeConnection(connection);
			log.exiting("findByPrimaryKey");
		}
	}

	public Collection findAll() throws CVersAccessException {

		log.entering("findAll");
		Connection connection = null;
		PreparedStatement statement = null;
		ComponentElementIF compVers = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(FINDALLSTRING);
			ResultSet resultSet = statement.executeQuery();
			Vector res=new Vector();
			while(resultSet.next()) { 
				ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
				compVers = factory.createComponentElement(
											resultSet.getString(3),
											resultSet.getString(4),
											resultSet.getString(1),
											resultSet.getString(2),
											resultSet.getString(5),
											resultSet.getString(6),
											resultSet.getString(7),
											resultSet.getString(8),
											resultSet.getString(9),
											resultSet.getString(10),
											resultSet.getString(11),
											resultSet.getString(12),
											resultSet.getString(13),
											resultSet.getString(14),
											resultSet.getString(15),
											resultSet.getString(16),
											resultSet.getString(17),
											resultSet.getString(18),
											resultSet.getString(19),
											resultSet.getString(20),
											resultSet.getString(21),
											resultSet.getString(22));
				res.addElement(compVers);
			}
			return res;
		} catch(SQLException se) {
			String message = "Error executing SQL " + FINDALLSTRING;
			if (compVers != null) {
				message = message + " " + compVers.toString();
			} 
			log.warning (message, se);
			throw new CVersSQLException(message + se.toString());
		} finally {
			closeStatement(statement);
			closeConnection(connection);
			log.exiting("findAll");
		}
	}

	public CVersDBObject findByRealKey(PreparedStatement findByRealKeyStatement,
				int compID, String _vendor, String _name,
				String _componentType, String _subsystem)
				throws SQLException {

		log.entering("findByRealKey");
		CVersDBObject cVersDBObject = null;
		findByRealKeyStatement.clearParameters();
		findByRealKeyStatement.setInt(1, compID);
		findByRealKeyStatement.setString(2, _componentType);
		findByRealKeyStatement.setString(3, _subsystem);
		ResultSet resultSet = findByRealKeyStatement.executeQuery();
		Vector res=new Vector();
		while(resultSet.next()) {
			if (resultSet.getString(2).equalsIgnoreCase(_vendor) &&
				resultSet.getString(3).equalsIgnoreCase(_name)) {
					cVersDBObject = new CVersDBObject(
										compID, resultSet.getInt(1),
										_componentType, _subsystem,
										resultSet.getString(2),
										resultSet.getString(3),
										resultSet.getString(4),
										resultSet.getString(5),
										resultSet.getString(6),
										resultSet.getString(7),
										resultSet.getString(8),
										resultSet.getString(9),
										resultSet.getString(10),
										resultSet.getString(11),
										resultSet.getString(12),
										resultSet.getString(13),
										resultSet.getString(14),
										resultSet.getString(15),
										resultSet.getString(16),
										resultSet.getString(17),
										resultSet.getString(18),
										resultSet.getString(19),
										resultSet.getString(20),
										resultSet.getString(21));
			}
		}
		log.debug("Found the following real key: "+cVersDBObject);
		log.exiting("findByRealKey");
		return cVersDBObject;
	}

	public CVersPK findPKByRealKey(PreparedStatement findByRealKeyStatement,
				int compID, String _vendor, String _name,
				String _componentType, String _subsystem)
				throws SQLException {
		log.entering("findByRealKey");
		CVersDBObject cVersDBObject = findByRealKey(findByRealKeyStatement, compID,
										_vendor, _name,	_componentType, _subsystem);
		if (cVersDBObject == null) {
			log.exiting("findByRealKey");
			return null;
		} else {
			log.exiting("findByRealKey");
			return new CVersPK(cVersDBObject.getCompID(),
							   cVersDBObject.getHashnumber(),
							   cVersDBObject.getComponentType(),
							   cVersDBObject.getSubsystem());
		}
	}

	public ComponentElementIF findByRealKey(String _vendor, String _name,
				String _componentType, String _subsystem)
				throws CVersAccessException {
		log.entering("findByRealKey");
		Connection connection = null;
		PreparedStatement statement = null;
		CVersDBObject cVersDBObject;
		// get internal DB key
		int compID = HashKey.defaultHashFunction(_vendor, _name);
		ComponentElementIF compVers = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(FINDBYREALKEYSTRING);
			cVersDBObject = findByRealKey(statement, compID,
								  _vendor, _name, _componentType, _subsystem);
			if (cVersDBObject == null) {
				return null;
			};
			ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
			compVers = factory.createComponentElement(
											cVersDBObject.getVendor(),
											cVersDBObject.getName(),
											cVersDBObject.getComponentType(),
											cVersDBObject.getSubsystem(),
											cVersDBObject.getLocation(),
											cVersDBObject.getCounter(),
											cVersDBObject.getSCVendor(),
											cVersDBObject.getSCName(),
											cVersDBObject.getRelease(),
											cVersDBObject.getServiceLevel(),
											cVersDBObject.getPatchLevel(),
											cVersDBObject.getDeltaVersion(),
											cVersDBObject.getUpdateVersion(),
											cVersDBObject.getApplyTime(),
											cVersDBObject.getSCElementTypeID(),
											cVersDBObject.getSPElementTypeID(),
											cVersDBObject.getSPName(),
											cVersDBObject.getSPVersion(),
											cVersDBObject.getServerType(),
											cVersDBObject.getServerName(),
											cVersDBObject.getChangeNumber(),
											cVersDBObject.getProjectName()
											);			
			return compVers;
		} catch(SQLException se) {
			String message = "Error executing SQL " + FINDBYREALKEYSTRING;
			if (compVers != null) {
				message = message + " " + compVers.toString();
			}
			log.error(message, se);
			throw new CVersSQLException(message + se.toString());
		} finally {
			closeStatement(statement);
			closeConnection(connection);
			log.exiting("findByRealKey");
		}
	}

	public Collection findByCompType(String _componentType)
				throws CVersAccessException {
		log.entering("findByCompType");
		Connection connection = null;
		PreparedStatement statement = null;
		ComponentElementIF compVers = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(FINDBYCOMPTYPESTRING);
			statement.setString(1, _componentType);
			ResultSet resultSet = statement.executeQuery();
			Vector res=new Vector();
			while(resultSet.next()) {
				ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
				compVers = factory.createComponentElement(
											resultSet.getString(2),
											resultSet.getString(3),
											_componentType,
											resultSet.getString(1),
											resultSet.getString(4),
											resultSet.getString(5),
											resultSet.getString(6),
											resultSet.getString(7),
											resultSet.getString(8),
											resultSet.getString(9),
											resultSet.getString(10),
											resultSet.getString(11),
											resultSet.getString(12),
											resultSet.getString(13),
											resultSet.getString(14),
											resultSet.getString(15),
											resultSet.getString(16),
											resultSet.getString(17),
											resultSet.getString(18),
											resultSet.getString(19),
											resultSet.getString(20),
											resultSet.getString(21));

				res.addElement(compVers);
			}
			return res;
		} catch(SQLException se) {
			String message = "Error executing SQL " + FINDBYCOMPTYPESTRING;
			log.warning (message);
			throw new CVersSQLException(message + se.toString(), se);
		} finally {
			closeStatement(statement);
			closeConnection(connection);
			log.exiting("findByCompType");
		}
	}

	public Collection findByTypeAndSubsys(String _componentType, String _subsystem)
			throws CVersAccessException {
		log.entering("findByTypeAndSubsys");
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(FINDBYTYPEANDSUBSYSSTRING);
			statement.setString(1, _componentType);
			statement.setString(2, _subsystem);
			ResultSet resultSet = statement.executeQuery();
			Vector res=new Vector();  
			while(resultSet.next()) {
				ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
				ComponentElementIF compVers = factory.createComponentElement(
											resultSet.getString(1),
											resultSet.getString(2),
											_componentType,
											_subsystem,
											resultSet.getString(3),
											resultSet.getString(4),
											resultSet.getString(5),
											resultSet.getString(6),
											resultSet.getString(7),
											resultSet.getString(8),
											resultSet.getString(9),
											resultSet.getString(10),
											resultSet.getString(11),
											resultSet.getString(12),
											resultSet.getString(13),
											resultSet.getString(14),
											resultSet.getString(15),
											resultSet.getString(16),
											resultSet.getString(17),
											resultSet.getString(18),
											resultSet.getString(19),
											resultSet.getString(20));

				res.addElement(compVers);
			}
			return res;
		} catch(SQLException se) {
			log.error("Error executing SQL " + FINDBYTYPEANDSUBSYSSTRING, se);
			throw new CVersSQLException("Error executing SQL "+
						FINDBYTYPEANDSUBSYSSTRING +	se.toString());
		} finally {
			closeStatement(statement);
			closeConnection(connection);
			log.exiting("findByTypeAndSubsys");
		}
	}

	public void removeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {
		log.entering("removeCVers");
		CVersPK cVersPK;
		// Get connection and statement
		Connection connection = null;
		PreparedStatement findByRealKeyStatement = null;
		PreparedStatement removeStatement = null;
		// Create connection and statements
		try {
			connection = dataSource.getConnection();
			findByRealKeyStatement = connection.prepareStatement(FINDBYREALKEYSTRING);
			removeStatement = connection.prepareStatement(REMOVESTRING);
			for (int i=0; i < cVersElements.length; i++) {
				try {
					//First see if the object already exists
					cVersPK = findPKByRealKey(findByRealKeyStatement,
								HashKey.defaultHashFunction(
									cVersElements[i].getVendor(),
									cVersElements[i].getName()),							
								cVersElements[i].getVendor(),
								cVersElements[i].getName(),
								cVersElements[i].getComponentType(),
								cVersElements[i].getSubsystem());
					//If so, then we will remove the entry
					if (cVersPK != null) {
						log.debug("object will be removed: " +
									cVersPK.compID + "," +
									cVersPK.hashnumber + "," +
									cVersPK.componentType + "," +
									cVersPK.subSystem + "," +
									cVersElements[i].getVendor() + "," +
									cVersElements[i].getName());
						remove(removeStatement, cVersPK);
					} else {
						log.debug("object does not exists in CVERS: " +
									cVersElements[i].getVendor() + "," +
									cVersElements[i].getName() + "," +
									cVersElements[i].getComponentType() + "," +
									cVersElements[i].getSubsystem());
						//this is not a serious error: we go on processing the array
					}
				} catch(SQLException se) {
					log.error("error during removal of entry: " +
								cVersElements[i].getVendor() +
								cVersElements[i].getName() +
								cVersElements[i].getComponentType() +
								cVersElements[i].getSubsystem(),
								se);
					throw new CVersAccessException("error during removal of entry: " +
								cVersElements[i].getVendor() +
								cVersElements[i].getName() +
								cVersElements[i].getComponentType() +
								cVersElements[i].getSubsystem() +
								se.toString());
				}
			}
		} catch (SQLException se) {
			log.error("Error removing entries from COMPVERS: ", se);
			throw new CVersSQLException("Error removing entries from COMPVERS: "
						+ se.toString());
		} finally {
			closeStatement(findByRealKeyStatement);
			closeConnection(connection);
			log.exiting("removeCVers");
		}
	}

	public void remove(PreparedStatement statement, CVersPK cVersPK)
			throws SQLException {
		log.entering("remove");
		try {
			statement.clearParameters();
			statement.setInt(1, cVersPK.compID);
			statement.setInt(2, cVersPK.hashnumber);
			statement.setString(3, cVersPK.componentType);
			statement.setString(4, cVersPK.subSystem);
			if (statement.executeUpdate() < 1) {
				log.error("Row can't be deleted since it does not exist: " +
							"compID: " + cVersPK.compID +
							", hashnumber: " + cVersPK.hashnumber +
							", componenttype: " + cVersPK.componentType +
							", subsystem: " + cVersPK.subSystem);
			}
		} catch(SQLException se) {
			throw se;
		}
		log.debug("object removed!");
		log.exiting("remove");
	}

	// close
	private void closeConnection(Connection _connection) {
		log.entering("closeConnection");
		try {
			if (_connection != null) {
				_connection.close();
			}
		} catch(SQLException se) {
			log.warning ("connection close failed", se);
		}
		log.exiting("closeConnection");
	}

	private void closeStatement (Statement _statement) {

		log.entering("closeStatement");
		try {
			if (_statement != null) {
				_statement.close();
			}
		} catch(SQLException se) {
			log.warning ("statement close failed", se);
		}
		log.exiting("closeStatement");
	}

	public void error (String name) throws CVersAccessException {

		String message = name + " not supported by DB schema 3";
		log.warning (message);
		throw new CVersAccessException (message);
	}

	public SCVersionIF readSCVersion (String compName, String compVendor, String location, String counter, String provider, String rel, String spl, String pl) throws CVersAccessException {
		
		error ("readSCVersion");
		return null;
	}

	public void syncCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {

		error ("syncCVers");
	}

	public void syncFinished ()	throws CVersAccessException {

		error ("syncFinished");
	}

	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) throws CVersAccessException {
		error ("syncCVersUpdate");
		return null;
	}
	
	public Vector execSQL (String query){

		try {
			error ("execSQL");
		} catch (CVersAccessException e) {
//			$JL-EXC$
			query = null;
		}
		return null;
	}

}
