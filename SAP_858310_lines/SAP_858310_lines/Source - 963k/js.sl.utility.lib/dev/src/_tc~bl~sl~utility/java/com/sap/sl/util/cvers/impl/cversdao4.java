/*
 * updated from perforce CVersDao#5
 */
 
package com.sap.sl.util.cvers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.SCLevelIF;
import com.sap.sl.util.components.api.SCRequirementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.components.api.SoftwareComponentIF;
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

public class CVersDao4 implements CVersDaoIF {

	private static boolean testing = true;
	
	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CVersDao4.class.getName());
	
	private static String STATUS_DONE	= "DONE";	// database status after SDM synchronize is finished
	private static String STATUS_SYNC	= "SYNC";	// database status during syncCVers calls
	private static String STATUS_CMS	= "CMS";	// database status for components updated by CMS
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
	
	private static String FN_SPNAME = "SPNAME";
	private static int    FL_SPNAME = 256;
	
	private static String FN_SPVERSION = "SPVERSION";
	private static int    FL_SPVERSION = 64;
	
	private static String FN_SCELEMENTYPEID = "SCELEMENTYPEID";
	private static int    FL_SCELEMENTYPEID = 256;
	
	private static String FN_SPELEMENTYPEID = "SPELEMENTYPEID";
	private static int    FL_SPELEMENTYPEID = 256;
	
	private static String FN_SERVERNAME = "SERVERNAME";
	private static int    FL_SERVERNAME = 256;
	
	private static String FN_CHANGENUMBER = "CHANGENUMBER";
	private static int    FL_CHANGENUMBER = 10;
	
	private static String FN_PROJECTNAME = "PROJECTNAME";
	private static int    FL_PROJECTNAME = 256;
	
	private static String FN_UPDATEVERSION = "UPDATEVERSION";
	private static int    FL_UPDATEVERSION = 20;
	
	private static String FN_DELTAVERSION = "DELTAVERSION";
	private static int    FL_DELTAVERSION = 1;
	
	private static String FN_LOCATION = "LOCATION";
	private static int    FL_LOCATION = 40;
	
	private static String FN_COUNTER = "COUNTER";
	private static int    FL_COUNTER = 40;
	
	private static String FN_APPLYTIME = "APPLYTIME";
	private static int    FL_APPLYTIME = 20;
	
	private static String FN_SERVERTYPE = "SERVERTYPE";
	private static int    FL_SERVERTYPE = 10;
	
	private static String FN_STATUS = "STATUS";
	private static int    FL_STATUS = 10;
	
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
		"SERVERNAME = ?, CHANGENUMBER = ?, PROJECTNAME = ?, " +
		"STATUS = ? " +
		"WHERE COMPID = ? AND HASHNUMBER = ? AND COMPONENTTYPE = ? " +
		"AND SUBSYSTEM = ?"; 
	private final static String INSERTSTRING =
		"INSERT INTO BC_COMPVERS " +
		"(COMPID, HASHNUMBER, COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME, " +
		"LOCATION, COUNTER, "+
		"SCVENDOR, SCNAME, SAPRELEASE, SERVICELEVEL, PATCHLEVEL, DELTAVERSION, "+
		"UPDATEVERSION, APPLYTIME, SCELEMENTTYPEID, SPELEMENTTYPEID, "+
		"SPNAME, SPVERSION, SERVERTYPE, SERVERNAME, CHANGENUMBER,PROJECTNAME, STATUS)  VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

	public CVersDao4 () {
		
		dataSource = null;
		// needed for testing, see MainCVLTest
	}
	
	public CVersDao4(DataSource _dataSource) {
		this.dataSource = _dataSource;
	}
	
	public void writeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {
		Connection connection = null;
		try {
			connection = this.getConnection();
			writeCVers (connection, cVersElements, null);
		} catch (SQLException e) {
			log.error ("writeCVers "+e.getMessage(), e);
			throw new CVersAccessException ("writeCVers "+e.getMessage());
		} finally {
			closeConnection(connection);
		}
	}
	
	public void writeSCHeader (ComponentElementIF cVersElement)
				throws CVersAccessException {
		
		log.entering("writeSCHeader");
		Connection connection = null;
		try {
			connection = this.getConnection();
			ComponentElementIF[] complist = new ComponentElementIF [1];
			complist [0] = cVersElement;
			log.debug ("before writing SC header to COMPVERS: "+cVersElement);
			writeCVers (connection, complist, STATUS_CMS);
			log.info("SC header written to COMPVERS: "+cVersElement);
		} catch (SQLException e) {
			log.error ("writeCVers "+e.getMessage(), e);
			throw new CVersAccessException ("writeCVers "+e.getMessage());
		} finally {
			closeConnection(connection);
			log.exiting("writeSCHeader");
		}
	}
	
	public void writeCVers(Connection connection, ComponentElementIF[] cVersElements)
				throws CVersAccessException {
		writeCVers (connection, cVersElements, null);
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
	
	private void writeCVers(Connection connection, ComponentElementIF[] cVersElements, String status)
				throws CVersAccessException {
		log.entering("writeCVers4");
		CVersPK cVersPK;
		PreparedStatement updateStatement = null;
		PreparedStatement insertStatement = null;
		PreparedStatement findByRealKeyStatement = null;
		// Create connection and statements
		ComponentElementIF compElement = null;
		try {
			findByRealKeyStatement = connection.prepareStatement(FINDBYREALKEYSTRING);
			updateStatement = connection.prepareStatement(UPDATESTRING);
			insertStatement = connection.prepareStatement(INSERTSTRING);

			// Let's go
			for (int i=0; i < cVersElements.length; i++) {
				// set the applytime
				ComponentFactoryIF compFactory = ComponentFactoryIF.getInstance();
				
				String servicelevel = cVersElements[i].getServiceLevel();
				
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
								cVersElements[i].getLocation(),
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
								cVersElements[i].getProjectName(),
								cVersElements[i].getComponentVersion()
								);

				log.debug ("writeCVers4: "+status+" = "+compElement.toString ());
				
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
					if (status == null) {status = "INSERT";};
					create(insertStatement, 0, compElement, status);
				} else {
					//We have to update the correct entry
					if (status == null) {status = "UPDATE";};
					save(updateStatement, cVersPK.hashnumber, compElement, status);
				}
				SCVersionIF scv = compElement.getComponentVersion();
				if (scv != null) {
					scv.setCounter (compElement.getCounter());
					scv.setLocation (compElement.getLocation());
					scv.setProvider (compElement.getComponentProvider());
					writeSCVersion (connection, scv);
				}
			}
		} catch (SQLException se) {
			String msg = "Error writing new entries into COMPVERS4: (newest tc/SL/utiljddschema deployed?) ";
			if (compElement != null) {
				msg = msg + compElement.toString ();
			} else {
				msg = msg + "null";
			};
			log.error (msg, se);
			throw new CVersSQLException(msg	+ se.toString());
		} finally {
			closeStatement (updateStatement);
			closeStatement (insertStatement);
			closeStatement (findByRealKeyStatement);
			log.exiting("writeCVers4");
		}
	}

	public void syncCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {
					
		log.entering("syncCVers");
		Connection connection = null;
		try {
			connection = this.getConnection();
			writeCVers (connection, cVersElements, STATUS_SYNC);
		} catch (SQLException e) {
			log.error ("SQLException "+e.getMessage(), e);
			throw new CVersAccessException ("SQLException "+e.getMessage());
		} finally {
			closeConnection(connection);
			log.exiting("syncCVers");
		}
	}

	public void syncCVers(Connection connection, ComponentElementIF[] cVersElements)
				throws CVersAccessException {
					log.entering("syncCVers");
		writeCVers (connection, cVersElements, STATUS_SYNC);
		log.exiting("syncCVers");
	}

	public void syncFinished ()	throws CVersAccessException {
		
		// remove all entries, which are not up-to-date
		// means: only SYNC and CMS entries survive.
		// SYNC entries are written by previous syncCVers calls from SDM
		// CMS entries are written by CMS via web service 
		
		log.entering("syncFinished");
		Connection connection = null;
		PreparedStatement removeStatement = null;
		PreparedStatement updateStatement = null;
		PreparedStatement selectStatement = null;
		try {
			connection = this.getConnection();
			
			// first display entries, which will be deleted
			
			this.displayDeletionEntries ();
			
			// syncCVers has set status SYNC for all components, which are known by SDM
			// status CMS was set, if the component is updated via CMS/NWDI
			
			removeStatement = connection.prepareStatement ("DELETE FROM BC_COMPVERS WHERE STATUS <> '"+STATUS_SYNC+"' AND STATUS <> '"+STATUS_CMS+"'");
			int result = removeStatement.executeUpdate ();
			log.debug ("syncFinished, "+result+" component data deleted");
			
			// status SYNC is replaced by status DONE for all non-NWDI components (those have status CMS)
			// as CMS software components are just written by NWDI and are usually not registered in SDM they must keep their status CMS
			// otherwise they would be removed
			
			updateStatement = connection.prepareStatement ("UPDATE BC_COMPVERS SET STATUS = '"+STATUS_DONE+"' WHERE STATUS <> '"+STATUS_CMS+"'");
			result = updateStatement.executeUpdate();
			log.debug ("syncFinished, "+result+" component data status set to "+STATUS_DONE);

		} catch (SQLException se) {
			String msg = "syncFinished: error removing outdated entries from COMPVERS: ";
			log.error (msg, se);
			throw new CVersSQLException (msg);
		} finally {
			closeStatement(removeStatement);
			closeStatement(updateStatement);
			closeConnection(connection);
			log.exiting("syncFinished");
		}

	}

	public void error (String name) throws CVersAccessException {

		String message = name + " not supported by DB schema 4";
		log.warning (message);
		throw new CVersAccessException (message);
	}

	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) throws CVersAccessException {
		error ("syncCVersUpdate");
		return null;
	}
	
	protected void save(PreparedStatement updateStatement, int hashnumber,
				ComponentElementIF compElem, String status) throws SQLException {
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
					compElem.getProjectName(),
					status);
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
					 String projectName,
					 String status) throws SQLException {
		log.entering("save4");
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
			setStringValue (updateStatement, FN_STATUS, 21, status, FL_STATUS);
			setIntValue (updateStatement, FN_COMPID, 22, compID);
			setIntValue (updateStatement, FN_HASHNUMBER, 23, hashnumber);
			setStringValue (updateStatement, FN_TYPE, 24, type, FL_TYPE);
			setStringValue (updateStatement, FN_SUBSYSTEM, 25, subsystem, FL_SUBSYSTEM);

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
				"," + changeNumber + 
				"," + status;
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

	private CVersPK create(PreparedStatement insertStatement, int hashnumber,
				ComponentElementIF compElem, String status)
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
							compElem.getProjectName(),
							status);
	}

	private CVersPK create(PreparedStatement insertStatement, int _hashnumber,
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
               				String projectname,
               				String status)
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
			setStringValue (insertStatement, FN_STATUS, 25, status, FL_STATUS);

			if (insertStatement.executeUpdate() != 1) {
				String msg = compID + "," + _hashnumber + "," + _componentType + "," +
				  _subsystem + "," + _vendor + "," + _name + "," + 
				  _location + "," + _counter + "," + _scVendor + "," + _scName + "," + 
				  _release + "," + _serviceLevel + "," + _patchLevel + "," + _deltaVersion + "," +
				  _updateVersion + "," + _applyTime + "," + _scElementTypeID + "," +
				  _spElementTypeID + "," + _spName  + "," + _spVersion + "," +
				  status;
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
			connection = this.getConnection();
			statement = connection.prepareStatement(FINDBYPKSTRING);			
			this.setIntValue(statement, FN_COMPID, 1, _cVersPK.compID);
			this.setIntValue(statement, FN_HASHNUMBER, 2, _cVersPK.hashnumber);
			this.setStringValue(statement, FN_TYPE, 3, _cVersPK.componentType, FL_TYPE);
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
			closeStatement (statement);
			closeConnection (connection);
			log.exiting("findByPrimaryKey");
		}
	}

	public Collection findAll() throws CVersAccessException {

		log.entering("findAll");
		Connection connection = null;
		try {
			connection = this.getConnection();
			return findAll (connection);
		} catch (Exception exc) {
			log.error ("findAll "+exc.getMessage(), exc);
			throw new CVersAccessException ("findAll "+exc.getMessage());
		} finally {
			closeConnection(connection);
			log.exiting("findAll");
		}
	}

	public Collection findAll(Connection connection) throws CVersAccessException {

		log.entering("findAll");
		PreparedStatement statement = null;
		ComponentElementIF compVers = null;
		try {
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
			closeStatement (statement);
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
		this.setIntValue (findByRealKeyStatement, FN_COMPID, 1, compID);
		this.setStringValue(findByRealKeyStatement, FN_TYPE, 2, _componentType, FL_TYPE);
		this.setStringValue(findByRealKeyStatement, FN_SUBSYSTEM, 3, _subsystem, FL_SUBSYSTEM);
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
		try {
			connection = this.getConnection();
			return findByRealKey (connection, _vendor, _name, _componentType, _subsystem);
		} catch(SQLException se) {
			String message = "Error getting connection";
			log.error(message, se);
			throw new CVersAccessException (message + se.toString());
		} finally {
			closeConnection(connection);
			log.exiting("findByRealKey");
		}
	}

	protected ComponentElementIF findByRealKey (Connection connection, String _vendor, String _name,
				String _componentType, String _subsystem)
				throws CVersAccessException {

		log.entering("findByRealKey");
		PreparedStatement statement = null;
		CVersDBObject cVersDBObject;
		// get internal DB key
		int compID = HashKey.defaultHashFunction(_vendor, _name);
		ComponentElementIF compVers = null;
		try {
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
			connection = this.getConnection();
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
			closeStatement (statement);
			closeConnection (connection);
			log.exiting("findByCompType");
		}
	}

	public Collection findByTypeAndSubsys(String _componentType, String _subsystem)
			throws CVersAccessException {
		log.entering("findByTypeAndSubsys");
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getConnection();
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
			closeStatement (statement);
			closeConnection (connection);
			log.exiting("findByTypeAndSubsys");
		}
	}

	private void displayDeletionEntries () {
				
		log.entering("findByStatus");
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getConnection();
			statement = connection.prepareStatement("SELECT COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME," +
			" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
			" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
			" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
			" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, STATUS FROM BC_COMPVERS " +
			" WHERE STATUS <> '"+STATUS_SYNC+"' AND STATUS <> '"+STATUS_CMS+"'");
			
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()) {
				ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
				ComponentElementIF compVers = factory.createComponentElement(
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
				String _status = resultSet.getString(23);
				log.info ("syncCVers, status='"+_status+"' deleted: "+compVers.toString());
			}
		} catch(Throwable se) {
			//$JL-EXC$
			log.warning ("Error reading CVERS, which are not in sync, "+" "+se.getMessage());
		} finally {
			closeStatement (statement);
			closeConnection (connection);
			log.exiting("findByStatus");
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
			connection = this.getConnection();
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
			closeStatement (findByRealKeyStatement);
			closeStatement (findByRealKeyStatement);
			closeConnection (connection);
			log.exiting("removeCVers");
		}
	}

	public void remove(PreparedStatement statement, CVersPK cVersPK)
			throws SQLException {
		log.entering("remove");
		try {
			statement.clearParameters();
			this.setIntValue(statement, FN_COMPID, 1, cVersPK.compID);
			this.setIntValue(statement, FN_HASHNUMBER, 2, cVersPK.hashnumber);
			this.setStringValue(statement, FN_TYPE, 3, cVersPK.componentType, FL_TYPE);
			this.setStringValue(statement, FN_SUBSYSTEM, 4, cVersPK.subSystem, FL_SUBSYSTEM);
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

	private Connection getConnection () throws SQLException {
		Connection conn = this.dataSource.getConnection();
		conn.setAutoCommit(true);
		return conn;
	}

	public SCVersionIF readSCVersion (String name, String vendor, String location, String counter, String provider, String rel, String spl, String pl) throws CVersAccessException {

		log.entering ("readSCVersion");
		Connection connection = null;
		try {
			connection = this.getConnection();
			SCVersionIF scv = readSCVersion (connection, name, vendor, location, counter, provider, rel, spl, pl);
			return scv;
		} catch(Exception se) {
			String msg = "readSCVersion1: "+se.getMessage()+" "+name+" "+vendor+" "+location+" "+counter;
			log.error (msg, se);
			throw new CVersAccessException (msg);
		} finally {
			closeConnection (connection);
			log.exiting ("readSCVersion");
		}
	}

	protected SCVersionIF readSCVersion (Connection connection, String name, String vendor, String location, String counter, String provider, String rel, String spl, String pl) throws CVersAccessException {
		
		if (GlobalConfig.DISABLE_CVL) {
			return null;
		}
		log.entering ("readSCVersion");
		PreparedStatement statement = null;
		String updatestmt = "SELECT HISTLOCATION,HISTCOUNTER,RELEASE,SERVICELEVEL,PATCHLEVEL FROM BC_SCVERSION WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.setString (1, name);
			statement.setString (2, vendor);
			statement.setString (3, location);
			statement.setString (4, counter);
			ResultSet resultSet = statement.executeQuery();

			if (!resultSet.next()) {
				return null;
			};

			String histlocation = resultSet.getString(1);
			String histcounter = resultSet.getString(2);
			String release = resultSet.getString(3);
			String servicelevel = resultSet.getString(4);
			String patchlevel = resultSet.getString(5);

			SCVersionIF history = null;
			if (histcounter == null) {
				histcounter = "";
			}
			histcounter = histcounter.trim ();
			if (!histcounter.trim().equals("")) {
				history = readSCVersion (connection, name, vendor, histlocation, histcounter, provider, rel, spl, pl);
			}
			SCLevelIF level = ComponentFactoryIF.getInstance().createSCLevel(release, servicelevel, patchlevel);
			SCRequirementIF[] required = readRequired (connection, name, vendor, location, counter, provider);
			SCLevelIF[] coverages = readCoverages (connection, name, vendor, location, counter, provider);
			SCLevelIF[] compatibles = readCompatibles (connection, name, vendor, location, counter, provider);
			SCVersionIF scv = ComponentFactoryIF.getInstance().createSCVersion(vendor, name, provider, location, counter, level, history, required, coverages, compatibles);
			return scv;

		} catch(Exception se) {
			String msg = "readSCVersion: can not read, "+se.getClass().getName()+" "+name+" "+vendor+" "+location+" "+counter;
			log.error (msg, se);
			throw new CVersAccessException (msg);
		} finally {
			closeStatement(statement);
			log.exiting ("readSCVersion");
		}
	}

	protected SCRequirementIF[] readRequired (Connection connection, String name, String vendor, String location, String counter, String provider) {

		log.entering ("readRequired");
		PreparedStatement statement = null;
		String updatestmt = "SELECT REQNAME,REQVENDOR,REQRELEASE,REQSERVICELEVEL,REQPATCHLEVEL FROM BC_SCREQUIRED WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.setString (1, name);
			statement.setString (2, vendor);
			statement.setString (3, location);
			statement.setString (4, counter);
			ResultSet resultSet = statement.executeQuery();
			String reqname = "";
			String reqvendor = "";
			String release = "";
			String servicelevel = "";
			String patchlevel = "";
			Vector vec = new Vector ();
			ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
			while (resultSet.next()) {
				reqname = resultSet.getString(1);
				reqvendor = resultSet.getString(2);
				release = resultSet.getString(3);
				servicelevel = resultSet.getString(4);
				patchlevel = resultSet.getString(5);
				SoftwareComponentIF comp = factory.createSoftwareComponent(reqvendor, reqname);
				SCLevelIF level = factory.createSCLevel(release, servicelevel, patchlevel);
				SCRequirementIF req = ComponentFactoryIF.getInstance().createSCRequirement(comp, provider, level);
				vec.add (req);
			}
			if (vec.size() == 0) {
				return new SCRequirementIF[0];
			}
			Object[] objArray = vec.toArray();
			SCRequirementIF[] level = new SCRequirementIF[vec.size()];
			for (int i=0; i < vec.size(); i++) {
				level [i] = (SCRequirementIF) vec.elementAt(i);
			}
			return level;
		} catch(SQLException se) {
			log.error ("readRequired: SQLException "+name+" "+vendor+" "+location+" "+counter, se);
		} finally {
			closeStatement(statement);
			log.exiting ("readRequired");
		}
		return null;
	}

	public SCLevelIF[] readSCLevel (Connection connection, String tablename, String name, String vendor, String location, String counter, String provider) {

		log.entering ("readSCLevel");
		PreparedStatement statement = null;
		String updatestmt = "SELECT RELEASE,SERVICELEVEL,PATCHLEVEL FROM "+tablename+" WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.setString (1, name);
			statement.setString (2, vendor);
			statement.setString (3, location);
			statement.setString (4, counter);
			ResultSet resultSet = statement.executeQuery();
			String release = "";
			String servicelevel = "";
			String patchlevel = "";
			Vector vec = new Vector ();
			ComponentFactoryIF factory = ComponentFactoryIF.getInstance();
			while (resultSet.next()) {
				release = resultSet.getString(1);
				servicelevel = resultSet.getString(2);
				patchlevel = resultSet.getString(3);
				SCLevelIF cover = factory.createSCLevel(release, servicelevel, patchlevel);
				vec.add (cover);
			}
			if (vec.size() == 0) {
				return new SCLevelIF[0];
			}
			Object[] objArray = vec.toArray();
			SCLevelIF[] level = new SCLevelIF[vec.size()];
			for (int i=0; i < vec.size(); i++) {
				level [i] = (SCLevelIF) vec.elementAt(i);
			}
			return level;
		} catch(SQLException se) {
			log.error ("readSCLevel: SQLException "+name+" "+vendor+" "+location+" "+counter, se);
		} finally {
			closeStatement(statement);
			log.exiting ("readSCLevel");
		}
		return null;
	}

	public SCLevelIF[] readCoverages (Connection connection, String name, String vendor, String location, String counter, String provider) {

		return readSCLevel (connection, "BC_SCCOVERAGE", name, vendor, location, counter, provider);
	}

	public SCLevelIF[] readCompatibles (Connection connection, String name, String vendor, String location, String counter, String provider) {

		return readSCLevel (connection, "BC_SCCOMPAT", name, vendor, location, counter, provider);
	}

	public void writeSCVersion (Connection connection, SCVersionIF history) throws CVersCreateException, SQLException {
			
		if (GlobalConfig.DISABLE_CVL) {
			return;
		}
		log.entering ("writeSCVersion");
		if (history == null) {
			log.debug ("writeSCVersion: history == null");
			return;
		};

		deleteSCVersion (connection, history);

		boolean excFound = false;
		String lastErrorMsg = "";
		Exception lastException = null;
		SCVersionIF ptr = history;
		while (ptr != null) {
			try {
				if (this.isSCVersionExisting (connection, ptr)) {
					updateSCVersion (connection, ptr);
				} else {
					insertSCVersion (connection, ptr);
				}
			} catch (Exception e) {
				excFound = true;
				lastException = e;
				lastErrorMsg = "writeSCVersion failed for: "+ptr.toString();
				log.error (lastErrorMsg, e);
				throw new CVersCreateException (lastErrorMsg + e.getMessage());
			}
			ptr = ptr.getNextElement();
			if (ptr != null) {
				ptr.setProvider(history.getProvider());
			}
		}
		log.exiting ("writeSCVersion");
	}

	private void deleteSCVersion (Connection connection, SCVersionIF scv) throws SQLException  {
			
		log.entering ("deleteSCVersion");
		if (scv == null) {
			log.debug ("deleteSCVersion: scv == null");
			return;
		};
		PreparedStatement statement = null;
		try {
//			Vector vec = new Vector ();
//			vec.add (scv);
//			while (scv.getNextElement() != null) {
//				SCVersionIF ptr = scv.getNextElement();
//				vec.insertElementAt(ptr, 0);
//			}
			String updatestmt = "DELETE FROM BC_SCVERSION WHERE NAME = ? AND VENDOR = ?";
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			if (statement.executeUpdate() < 1) {
					String msg = "updateSCVersion: did not delete "+scv.toString();
					log.info (msg);
			} else {
				String msg = "updateSCVersion: deleted sc="+scv.getName()+" vendor="+scv.getVendor();
				log.info (msg);
			}
		} catch(SQLException se) {
			log.error ("deleteSCVersion: SQLException "+scv.toString(), se);
			throw se;
		} finally {
			closeStatement(statement);
			log.exiting ("deleteSCVersion");
		}
	} 

	private void updateSCVersion (Connection connection, SCVersionIF scv) throws SQLException, CVersSQLException, CVersCreateException {
			
		log.entering ("updateSCVersion");
		if (scv == null) {
			log.debug ("updateSCVersion: history == null");
			return;
		};
		PreparedStatement statement = null;
		String updatestmt = "UPDATE BC_SCVERSION SET PROVIDER = ?, RELEASE = ?, SERVICELEVEL = ?, PATCHLEVEL = ?, HISTLOCATION = ?, HISTCOUNTER = ? WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getProvider());
			statement.setString (2, scv.getComponentLevel().getRelease());
			statement.setString (3, scv.getComponentLevel().getServiceLevel());
			statement.setString (4, scv.getComponentLevel().getPatchLevel());
			statement.setString (5, scv.getName());
			statement.setString (6, scv.getVendor());
			statement.setString (7, scv.getLocation ());
			statement.setString (8, scv.getCounter ());
			SCVersionIF history = scv.getNextElement();
			if ((history == null) || (history.getLocation() == null) || (history.getCounter() == null)) {
				statement.setString(9, " ");
				statement.setString(10, " ");
			} else {
				statement.setString(9, history.getLocation());
				statement.setString(10, history.getCounter());
			}
			if (statement.executeUpdate() < 1) {
					String msg = "updateSCVersion: did not update "+scv.toString();
					log.error (msg);
					throw new CVersSQLException (msg+statement.getWarnings().toString());
			}
			writeRequired (connection, scv);
			writeSCLevel (connection, scv, "BC_SCCOVERAGE", scv.getCoverages());
			writeSCLevel (connection, scv, "BC_SCCOMPAT", scv.getCompatibles());
		} catch(SQLException se) {
			log.error ("updateSCVersion: SQLException "+scv.toString(), se);
		} finally {
			closeStatement(statement);
		}
		log.exiting ("updateSCVersion");
	} 

	private void insertSCVersion (Connection connection, SCVersionIF scv) throws CVersCreateException, SQLException {
			
		log.entering ("insertSCVersion");
		if (scv == null) {
			log.debug ("insertSCVersion: history == null");
			return;
		};
		PreparedStatement statement = null;
		String updatestmt = "INSERT INTO BC_SCVERSION (NAME, VENDOR, LOCATION, COUNTER, PROVIDER, RELEASE, SERVICELEVEL, PATCHLEVEL, HISTLOCATION, HISTCOUNTER) VALUES (?,?,?,?,?,?,?,?,?,?)";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			statement.setString (3, scv.getLocation ());
			statement.setString (4, scv.getCounter ());
			statement.setString (5, scv.getProvider ());
			statement.setString (6, scv.getComponentLevel().getRelease ());
			statement.setString (7, scv.getComponentLevel().getServiceLevel ());
			statement.setString (8, scv.getComponentLevel().getPatchLevel ());
			SCVersionIF history = scv.getNextElement();
			if ((history == null) || (history.getLocation() == null) || (history.getCounter() == null)) {
				statement.setString(9, " ");
				statement.setString(10, " ");
			} else {
				statement.setString(9, history.getLocation());
				statement.setString(10, history.getCounter());
			}
			if (statement.executeUpdate() < 1) {
					String msg = "insertSCVersion: can not update "+scv.toString();
					log.error (msg);
					throw new CVersCreateException (msg);
			}
			log.info ("insertSCVersion finished: "+scv.toString());
			writeRequired (connection, scv);
			writeSCLevel (connection, scv, "BC_SCCOVERAGE", scv.getCoverages());
			writeSCLevel (connection, scv, "BC_SCCOMPAT", scv.getCompatibles());
		} catch(SQLException se) {
			String msg = "insertSCVersion: can not update "+scv.toString();
			log.error (msg, se);
			throw se;
		} catch(Exception se) {
			String msg = "insertSCVersion: can not update "+scv.toString();
			log.error (msg, se);
			throw new CVersCreateException (se.getMessage());
		} finally {
			closeStatement(statement);
		}
		log.exiting ("insertSCVersion");
	}

	public void writeRequired (Connection connection, SCVersionIF history) throws CVersSQLException, CVersCreateException, SQLException {
			
		log.entering ("writeRequired");
		if (history == null) {
			log.debug ("writeRequired: scv == null");
			return;
		};
		boolean excFound = false;
		String lastErrorMsg = "";
		Exception lastException = null;
		SCRequirementIF[] required = history.getRequired();
		if (required == null) {
			log.debug ("writeRequired: required == null");
			return;
		}
		deleteRequired (connection, history);
		insertRequired (connection, history);
		log.exiting ("writeRequired");
	}

	private void deleteRequired (Connection connection, SCVersionIF scv) throws SQLException, CVersSQLException {
			
		log.entering ("deleteRequired");
		if (scv == null) {
			log.debug ("deleteRequired: history == null");
			return;
		};
		PreparedStatement statement = null;
		String updatestmt = "DELETE FROM BC_SCREQUIRED WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			statement.setString (3, scv.getLocation ());
			statement.setString (4, scv.getCounter ());
			int result = statement.executeUpdate();
			if (result < 1) {
					String msg = "deleteRequired: required deleted N="+scv.getName()+" V="+scv.getVendor()+" L="+scv.getLocation ()+" C="+scv.getCounter ();
					log.info (msg);
			}
		} catch(SQLException se) {
			log.error ("deleteRequired: SQLException, "+se.getMessage());
			log.error ("deleteRequired: can not delete required of "+scv.toString(), se);
		} finally {
			closeStatement(statement);
			log.exiting ("deleteRequired");
		}
	} 

	private void insertRequired (Connection connection, SCVersionIF scv) throws CVersCreateException, SQLException {
		
		log.entering ("insertRequired");
		if (scv == null) {
			log.debug ("insertRequired: history == null");
			return;
		};
		SCRequirementIF[] required = scv.getRequired();
		if (required == null) {
			log.debug ("insertRequired: required == null");
			return;
		};
		for (int i=0; i < required.length; i++) {
			PreparedStatement statement = null;
			String updatestmt = "INSERT INTO BC_SCREQUIRED (NAME, VENDOR, LOCATION, COUNTER, PROVIDER, REQNAME, REQVENDOR, REQRELEASE, REQSERVICELEVEL, REQPATCHLEVEL) VALUES (?,?,?,?,?,?,?,?,?,?)";
			try {
				statement = connection.prepareStatement(updatestmt);
				statement.clearParameters();
				statement.setString (1, scv.getName());
				statement.setString (2, scv.getVendor());
				statement.setString (3, scv.getLocation ());
				statement.setString (4, scv.getCounter ());
				statement.setString (5, scv.getProvider ());
				statement.setString (6, required [i].getName());
				statement.setString (7, required [i].getVendor());
				statement.setString (8, required [i].getComponentLevel().getRelease());
				statement.setString (9, required [i].getComponentLevel().getServiceLevel());
				statement.setString(10, required [i].getComponentLevel().getPatchLevel());
				if (statement.executeUpdate() < 1) {
						String msg = "insertRequired: can not insert "+i+"/"+required.length+" "+required [i].toString();
						log.error (msg);
						throw new CVersCreateException (msg);
				}
				log.info ("insertRequired finished: "+scv.toString());
			} catch(SQLException se) {
				String msg = "insertRequired: can not insert "+scv.toString();
				displayRequired (connection, scv);
				log.error (msg, se);
				throw se;
			} catch(Exception se) {
				String msg = "insertRequired: can not insert "+scv.toString();
				log.error (msg, se);
				throw new CVersCreateException (se.getMessage());
			} finally {
				closeStatement(statement);
			}
		}
		log.exiting ("insertRequired");
	}

	private void displayRequired (Connection connection, SCVersionIF scv) throws CVersCreateException, SQLException {
		
		log.entering ("displayRequired");
		if (scv == null) {
			log.debug ("displayRequired: scv == null");
			return;
		};
		PreparedStatement statement = null;
		String updatestmt = "SELECT REQNAME,REQVENDOR,REQRELEASE,REQSERVICELEVEL,REQPATCHLEVEL FROM BC_SCREQUIRED WHERE NAME=? AND VENDOR=? AND LOCATION=? AND COUNTER=?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			statement.setString (3, scv.getLocation ());
			statement.setString (4, scv.getCounter ());
			ResultSet result = statement.executeQuery();
			log.warning ("displayRequired: "+scv.toString());
			while (result.next()) {
				log.warning ("displayRequired: has required N="+result.getString(1)+" V="+result.getString(2)+" R="+result.getString(3)+" SP="+result.getString(4)+" PL="+result.getString(5));
			}
		} catch(Exception se) {
			String msg = "displayRequired: "+se.getMessage()+", can not insert "+scv.toString();
			log.error (msg, se);
		} finally {
			closeStatement(statement);
			log.exiting ("displayRequired");
		}
	}

	public void writeSCLevel (Connection connection, SCVersionIF scv, String tablename, SCLevelIF[] level) throws CVersSQLException, CVersCreateException, SQLException {
			
		log.entering ("writeSCLevel "+tablename);
		if (scv == null) {
			log.debug ("writeSCLevel: scv == null, "+tablename);
			return;
		};
		boolean excFound = false;
		String lastErrorMsg = "";
		Exception lastException = null;
		if (level == null) {
			log.debug ("writeSCLevel: ("+tablename+") level == null");
			return;
		} 
		deleteSCLevel (connection, scv, tablename);
		insertSCLevel (connection, scv, tablename, level);
		log.exiting ("writeSCLevel "+tablename);
	}

	private void deleteSCLevel (Connection connection, SCVersionIF scv, String tablename) throws SQLException, CVersSQLException {
			
		log.entering ("deleteSCLevel "+tablename);
		PreparedStatement statement = null;
		String updatestmt = "DELETE FROM "+tablename+" WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = connection.prepareStatement(updatestmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			statement.setString (3, scv.getLocation ());
			statement.setString (4, scv.getCounter ());
			int result = statement.executeUpdate();
			if (result < 1) {
				log.info ("deleteSCLevel: result = "+result+" "+tablename);
			}
			String msg = "deleteSCLevel "+tablename+" finished: "+scv.toString();
			log.debug (msg);
		} catch(SQLException se) {
			log.error ("deleteSCLevel: can not delete "+scv.toString());
			log.error (se.getMessage(), se);
		} finally {
			closeStatement(statement);
			log.exiting ("deleteSCLevel");
		}
	} 

	private void insertSCLevel (Connection connection, SCVersionIF scv, String tablename, SCLevelIF[] level) throws CVersCreateException, SQLException {
		
		log.entering ("insertSCLevel");
		if (level == null) {
			log.debug ("insertSCLevel: level == null");
			return;
		};
		for (int i=0; i < level.length; i++) {
			PreparedStatement statement = null;
			String updatestmt = "INSERT INTO "+tablename+" (NAME, VENDOR, LOCATION, COUNTER, RELEASE, SERVICELEVEL, PATCHLEVEL) VALUES (?,?,?,?,?,?,?)";
			try {
				statement = connection.prepareStatement(updatestmt);
				statement.clearParameters();
				statement.setString (1, scv.getName());
				statement.setString (2, scv.getVendor());
				statement.setString (3, scv.getLocation ());
				statement.setString (4, scv.getCounter ());
				statement.setString (5, level [i].getRelease());
				statement.setString (6, level [i].getServiceLevel());
				statement.setString (7, level [i].getPatchLevel());
				if (statement.executeUpdate() < 1) {
						String msg = "insertSCLevel: can not insert "+i+"/"+level.length+" "+level [i].toString();
						log.error (msg);
						throw new CVersCreateException (msg);
				}
				log.debug ("insertSCLevel finished: "+scv.toString());
			} catch(SQLException se) {
				String msg = "insertSCLevel: SQLException, did not insert "+scv.toString();
				log.error (msg);
				log.error (updatestmt, se);
				throw se;
			} catch(Exception se) {
				String msg = "insertSCLevel: "+se.getMessage()+", can not insert "+scv.toString();
				log.error (msg, se);
				throw new CVersCreateException (se.getMessage());
			} finally {
				closeStatement(statement);
			}
		}
		log.exiting ("insertSCLevel");
	}

	private boolean isSCVersionExisting (Connection connection, SCVersionIF scv) throws SQLException {
		
		log.entering ("isSCVersionExisting");
		if (scv == null) {
			log.debug ("isSCVersionExisting: history == null");
			return false;
		};
		PreparedStatement statement = null;
		String selectstmt = "SELECT NAME,VENDOR,LOCATION,COUNTER FROM BC_SCVERSION WHERE NAME = ? AND VENDOR = ? AND LOCATION = ? AND COUNTER = ?";
		try {
			statement = 
			
			connection.prepareStatement
			
			 (selectstmt);
			statement.clearParameters();
			statement.setString (1, scv.getName());
			statement.setString (2, scv.getVendor());
			statement.setString (3, scv.getLocation());
			statement.setString (4, scv.getCounter());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				log.debug ("isSCVersionExisting FALSE: " + scv.toString());
				return false;
			}
			log.debug ("isSCVersionExisting TRUE: "+scv.toString());
			return true;
		} catch(SQLException se) {
			log.error ("SQLException isSCVersionExisting: "+scv.toString(), se);
			throw se;
		} finally {
			closeStatement(statement);
			log.exiting ("isSCVersionExisting");
		}
	}
	
	public Vector execSQL (String query) {

	java.sql.ResultSet resultSet;
	Vector array = new Vector ();
	Connection connection = null;
	java.sql.Statement stmt = null;
	try {
		  connection = this.getConnection();
		   stmt = connection.createStatement ();
		   resultSet = stmt.executeQuery (query);
		   ResultSetMetaData rsmd = resultSet.getMetaData();
		   int numberOfColumns = rsmd.getColumnCount();

		   int row = 1; 
		   while (resultSet.next ()) 
			 { 
			   Hashtable props = new Hashtable (1000);
			   for (int column=1; column <= numberOfColumns; column++)
				 {
				   String name = rsmd.getColumnName (column);
				   String value = resultSet.getString(name);
				   if (value == null) {value = "";};
				   props.put (name, value);
				 };
			   array.addElement(props);
			   row++;
			 };
		 }
	catch (Exception e2) {
		log.error ("ERROR execSQL, query: " + query, e2);
		return (null);
	} finally {
//		closeStatement (stmt);
		closeConnection (connection);
	}
	return (array);
	}


	// close

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

}
