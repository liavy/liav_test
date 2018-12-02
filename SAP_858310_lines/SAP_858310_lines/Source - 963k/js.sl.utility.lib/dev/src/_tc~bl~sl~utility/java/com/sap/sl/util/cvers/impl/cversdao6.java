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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.sap.sl.util.components.impl.ComponentElement;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.CVersCreateException;
import com.sap.sl.util.cvers.api.CVersFinderException;
import com.sap.sl.util.cvers.api.CVersSQLException;
import com.sap.sl.util.cvers.api.SyncElementStatusIF;
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

public class CVersDao6 implements CVersDaoIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CVersDao6.class.getName());
	
	private static String STATUS_DONE	= "DONE";	// database status after SDM synchronize is finished
	private static String STATUS_SYNC	= "SYNC";	// database status during syncCVers calls
	private static String STATUS_CMS	= "CMS";	// database status for components updated by CMS

	private static String FN_COMPID = "COMPID";	// integer field
	
	private static String FN_HASHNUMBER = "HASHNUMBER";	// integer field
	
	private static String FN_TYPE = "TYPE";
	private static int    FL_TYPE = 10;
	
	private static String FN_SUBSYSTEM = "SUBSYSTEM";
	private static int    FL_SUBSYSTEM = 10;
	
	private static String FN_NAME = "NAME";
	private static int    FL_NAME = 256;
	
	private static String FN_VENDOR = "VENDOR";
	private static int    FL_VENDOR = 256;
	
	private static String FN_LOCATION = "LOCATION";
	private static int    FL_LOCATION = 40;
	
	private static String FN_COUNTER = "COUNTER";
	private static int    FL_COUNTER = 40;
	
	private static String FN_PROVIDER = "PROVIDER";
	private static int    FL_PROVIDER = 40;
	
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
	
	private static String FN_APPLYTIME = "APPLYTIME";
	private static int    FL_APPLYTIME = 20;
	
	private static String FN_SERVERTYPE = "SERVERTYPE";
	private static int    FL_SERVERTYPE = 10;
	
	private static String FN_STATUS = "STATUS";
	private static int    FL_STATUS = 10;
	
	private static String FN_ALGNAME = "ALGNAME";
	private static int    FL_ALGNAME = 5;
	
	private static String FN_ALGVALUE = "ALGVALUE";
	private static int    FL_ALGVALUE = 40;
	
	private final static String FINDBYREALKEYSTRING =
		"SELECT HASHNUMBER, VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, PROVIDER, ALGNAME, ALGVALUE "+
		" FROM BC_COMPVERS WHERE COMPID = ? AND COMPONENTTYPE = ?" +
		" AND SUBSYSTEM = ?";
		
	// get all current SC levels from database
	
	private final static String FINDSCLEVELSTRING =
		"SELECT VENDOR, NAME, SAPRELEASE, SERVICELEVEL, PATCHLEVEL FROM BC_COMPVERS WHERE COMPONENTTYPE= ?";
		
	// UPDATE database items during SDM sync command
	
	private final static String SYNCUPDATESTRING =
		"UPDATE BC_COMPVERS SET VENDOR = ?, NAME = ?, " +
		"LOCATION = ?, COUNTER = ?, SCVENDOR = ?, "+
		"SCNAME = ?, SAPRELEASE = ?, SERVICELEVEL = ?, PATCHLEVEL = ?, DELTAVERSION = ?, "+
		"UPDATEVERSION = ?, SCELEMENTTYPEID = ?, "+
		"SPELEMENTTYPEID = ?, SPNAME = ?, SPVERSION = ?, SERVERTYPE = ?, "+
		"SERVERNAME = ?, CHANGENUMBER = ?, PROJECTNAME = ?, " +
		"STATUS = ?, PROVIDER = ?, ALGNAME = ?, ALGVALUE = ? " +
		"WHERE COMPID = ? AND HASHNUMBER = ? AND COMPONENTTYPE = ? " +
		"AND SUBSYSTEM = ? AND STATUS <> 'CMS'"; 
		
	// UPDATE database items during normal SDM deployment
	
	private final static String UPDATESTRING =
		"UPDATE BC_COMPVERS SET VENDOR = ?, NAME = ?, " +
		"LOCATION = ?, COUNTER = ?, SCVENDOR = ?, "+
		"SCNAME = ?, SAPRELEASE = ?, SERVICELEVEL = ?, PATCHLEVEL = ?, DELTAVERSION = ?, "+
		"UPDATEVERSION = ?, APPLYTIME = ?, SCELEMENTTYPEID = ?, "+
		"SPELEMENTTYPEID = ?, SPNAME = ?, SPVERSION = ?, SERVERTYPE = ?, "+
		"SERVERNAME = ?, CHANGENUMBER = ?, PROJECTNAME = ?, " +
		"STATUS = ?, PROVIDER = ?, ALGNAME = ?, ALGVALUE = ? " +
		"WHERE COMPID = ? AND HASHNUMBER = ? AND COMPONENTTYPE = ? " +
		"AND SUBSYSTEM = ?"; 
	private final static String INSERTSTRING =
		"INSERT INTO BC_COMPVERS " +
		"(COMPID, HASHNUMBER, COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME, " +
		"LOCATION, COUNTER, "+
		"SCVENDOR, SCNAME, SAPRELEASE, SERVICELEVEL, PATCHLEVEL, DELTAVERSION, "+
		"UPDATEVERSION, APPLYTIME, SCELEMENTTYPEID, SPELEMENTTYPEID, "+
		"SPNAME, SPVERSION, SERVERTYPE, SERVERNAME, CHANGENUMBER,PROJECTNAME, "+
		"STATUS,PROVIDER, ALGNAME, ALGVALUE) VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String FINDBYPKSTRING =
		"SELECT  VENDOR, NAME, LOCATION, COUNTER, SCVENDOR, SCNAME," +
		" SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, SERVERTYPE, " +
 		" SERVERNAME, CHANGENUMBER, PROJECTNAME, PROVIDER, ALGNAME, ALGVALUE "+
		" FROM BC_COMPVERS WHERE COMPID = ? AND HASHNUMBER = ? AND " +
		"COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	private final static String FINDALLSTRING =
		"SELECT COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, PROVIDER, "+
		" ALGNAME, ALGVALUE "+
		" FROM BC_COMPVERS";
	private final static String FINDBYCOMPTYPESTRING =
		"SELECT SUBSYSTEM, VENDOR, NAME, " +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, PROVIDER, "+
		" ALGNAME, ALGVALUE "+
		" FROM BC_COMPVERS WHERE COMPONENTTYPE = ?";
	private final static String FINDBYTYPEANDSUBSYSSTRING =
		"SELECT VENDOR, NAME," +
		" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
		" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
		" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
		" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, PROVIDER, "+
		" ALGNAME, ALGVALUE "+
		" FROM BC_COMPVERS WHERE COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	private final static String REMOVESTRING =
		"DELETE FROM BC_COMPVERS WHERE COMPID = ?" +
		" AND HASHNUMBER = ? AND COMPONENTTYPE = ? AND SUBSYSTEM = ?";
	
	private DataSource dataSource;

	public CVersDao6 () {
		
		dataSource = null;
		// needed for testing, see MainCVLTest
	}
	
	public CVersDao6(DataSource _dataSource) {
		this.dataSource = _dataSource;
	}
	
	private void showUsedTime (String name, long startTime) {
		
		long finishTime = System.currentTimeMillis();
		long usedTime = finishTime - startTime;
		log.debug (name+" took "+usedTime+" ms");
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
		if (cVersElement == null) {
			log.warning ("writeSCHeader cVersElement=null");
			return;
		}
		Connection connection = null;
		try {
			connection = this.getConnection();
			ComponentElementIF[] complist = new ComponentElementIF [1];
			complist [0] = cVersElement;
			log.debug ("before writing SC header to COMPVERS: "+cVersElement.toString());
			writeCVers (connection, complist, STATUS_CMS);
			log.info("SC header written to COMPVERS: "+cVersElement);
		} catch (SQLException e) {
			log.error ("writeCVers "+e.getMessage() + " " + cVersElement.toString(), e);
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
					log.warning ("setStringValue value truncated to '"+value+"'");
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
	 
	private void updateDCLevels (Connection connection, ComponentElementIF[] cVersElements) throws SQLException {
		PreparedStatement findByRealKeyStatement = null;
		HashMap result = new HashMap ();
		
		try {
			// first read all current SCs from database and initially
			// fill their data into the result hash
			
			findByRealKeyStatement = connection.prepareStatement(FINDSCLEVELSTRING);
			findByRealKeyStatement.clearParameters();
			this.setStringValue(findByRealKeyStatement, FN_TYPE, 1, ComponentElementIF.COMPONENTTYPE_SC, FL_TYPE);
			ResultSet resultSet = findByRealKeyStatement.executeQuery();
			int index = 0;
			while(resultSet.next()) {
				index++;
				String vendor = resultSet.getString(1);
				String name = resultSet.getString(2);
				String release = resultSet.getString(3);
				String servicelevel = resultSet.getString(4);
				String patchlevel = resultSet.getString(5);
				String key = vendor + "#" + name;
				String value = release + "#" + servicelevel+"#"+patchlevel;
				log.debug ("updateDCLevels #"+index+" "+key+" = "+value+" database");
				result.put(key, value);
			}
			
			// update result hash by all newly given SCs
			
			for (int i=0; i < cVersElements.length; i++) {
				if (cVersElements [i].getComponentType().equals(ComponentElementIF.COMPONENTTYPE_SC)) {
					String vendor = cVersElements [i].getVendor();
					String name = cVersElements [i].getName();
					String release = cVersElements [i].getRelease();
					String servicelevel = cVersElements [i].getServiceLevel();
					String patchlevel = cVersElements [i].getPatchLevel();
					String key = vendor + "#" + name;
					String value = release + "#" + servicelevel+"#"+patchlevel;
					log.debug ("updateDCLevels "+key+" = "+value+" writeCVers");
					result.put (key, value);
				}
			}
			for (int i=0; i < cVersElements.length; i++) {
				if (cVersElements [i].getComponentType().equals(ComponentElementIF.COMPONENTTYPE_DC)) {
					String scvendor = cVersElements [i].getSCVendor();
					String scname = cVersElements [i].getSCName();
					String key = scvendor + "#" + scname;
					String value = (String) result.get(key);
					log.debug ("HashMap "+key+" = "+value);
					if (value == null) {
						log.warning ("SC "+scvendor + " " + scname + " not known yet, can not get release level for "+cVersElements [i].toString());
					} else {
						String release = null;
						String servicelevel = null;
						String patchlevel = null;
						int pos1 = value.indexOf('#');
						if (pos1 < 0) {
							log.error ("InternalERROR, updateDCLevels release not defined for "+key+" val="+value);
						} else {
							release = value.substring(0, pos1);
							int pos2 = value.indexOf('#', pos1+1);
							if (pos2 < 0) {
								log.error ("InternalERROR, updateDCLevels servicelevel not defined for "+key+" val="+value);
							} else {
								servicelevel = value.substring(pos1+1, pos2);
								patchlevel = value.substring(pos2+1);
								if (release.trim().length() == 0) {
									release = null;
									log.warning ("updateDCLevels, release == null for SC "+scvendor+" "+scname+" of DC "+cVersElements [i].toString()+" val="+value);
								}
								if (servicelevel.trim().length() == 0) {
									servicelevel = null;
									log.warning ("updateDCLevels, servicelevel == null for SC "+scvendor+" "+scname+" of DC "+cVersElements [i].toString()+" val="+value);
								}
								if (patchlevel.trim().length() == 0) {
									patchlevel = null;
									log.warning ("updateDCLevels, patchlevel == null for SC "+scvendor+" "+scname+" of DC "+cVersElements [i].toString()+" val="+value);
								}
								ComponentElement elem = (ComponentElement) cVersElements [i];
								elem.setRelease(release);
								elem.setServiceLevel(servicelevel);
								elem.setPatchLevel(patchlevel);
								log.debug ("updateDCLevels "+elem.getVendor()+" "+elem.getName()+" R="+release+" SP="+servicelevel+" PL="+patchlevel+" key="+key+" val="+value);
							}
						}
					}
				}
			}
		} finally {
			if (findByRealKeyStatement != null) {
				findByRealKeyStatement.close();
			}
		}
	}
	
	private void checkDCUpdate (Connection connection, ComponentElementIF compElement) throws CVersSQLException, SQLException {
		int index = -1;
		PreparedStatement searchDCs = null;
		PreparedStatement updateStatement = null;
		try {
			if (compElement.getComponentType().equals(ComponentElementIF.COMPONENTTYPE_SC)) {
				searchDCs = connection.prepareStatement ("SELECT VENDOR,NAME,COMPID,HASHNUMBER,SAPRELEASE,SERVICELEVEL,PATCHLEVEL "+
																			" FROM BC_COMPVERS WHERE SCNAME = '"+compElement.getName()+"'" + 
																			" AND SCVENDOR = '" + compElement.getVendor()+"'");
				ResultSet resultSet = searchDCs.executeQuery();
				updateStatement = connection.prepareStatement ("UPDATE BC_COMPVERS SET SAPRELEASE = ?, SERVICELEVEL = ?, PATCHLEVEL = ? WHERE COMPID = ? AND HASHNUMBER = ?");
				index = 0;
				while (resultSet.next()) {
					index++;
					String name = resultSet.getString(2);
					int compid = resultSet.getInt(3);
					int hashnumber = resultSet.getInt(4);
					String release = resultSet.getString(5);
					String sp = resultSet.getString(6);
					String pl = resultSet.getString(7);
					String screlease = compElement.getRelease();
					String scsplevel = compElement.getServiceLevel();
					String scplevel = compElement.getPatchLevel();
					if (screlease == null) {
						log.warning ("checkDCUpdate, SC "+compElement.getName()+" has no defined release");
					}
					if (scsplevel == null) {
						log.warning ("checkDCUpdate, SC "+compElement.getName()+" has no defined servicelevel");
					} 
					if (scplevel == null) {
						log.warning ("checkDCUpdate, SC "+compElement.getName()+" has no defined patchlevel");
					} 
					boolean updateData = false;
					if (release == null) {
						if (screlease == null) {
						} else {
							updateData = true;
						}
					} else {
						if (screlease == null) {
							updateData = true;
						}
					}
					if (updateData) {
					} else {
						if (sp == null) {
							if (scsplevel == null) {
							} else {
								updateData = true;
							}
						} else {
							if (scsplevel == null) {
								updateData = true;
							}
						}
						if (updateData) {
						} else {
							if (pl == null) {
								if (scplevel == null) {
								} else {
									updateData = true;
								}
							} else {
								if (scplevel == null) {
									updateData = true;
								}
							}
						}
					}
					if (updateData) {
						updateStatement.setString(1, screlease); 
						updateStatement.setString(2, scsplevel);
						updateStatement.setString(3, scplevel);
						updateStatement.setInt(4, compid);
						updateStatement.setInt(5, hashnumber);
						updateStatement.executeUpdate();
						log.debug ("checkDCUpdate, DC "+name+" levels updated to "+screlease+" "+scsplevel+" "+scplevel);
					} else {
						log.debug ("checkDCUpdate, DC "+name+" levels up-to-date "+screlease+" "+scsplevel+" "+scplevel);
					}
				}
			}
		} catch (Throwable e){
			String msg = "checkDCUpdate for "+index+" with exception "+e.getMessage();
			log.error (msg, e);
			throw new CVersSQLException(msg	+ e.toString());
		} finally {
			if (searchDCs != null) {
				searchDCs.close();
			}
			if (updateStatement != null) {
				updateStatement.close();
			}
		}
	}
	
	private void writeCVers(Connection connection, ComponentElementIF[] cVersElements, String status)
				throws CVersAccessException {
		log.entering("writeCVers6");
		
		try {
			updateDCLevels (connection, cVersElements);
		} catch (SQLException se) {
			String msg = "Error setting DC levels from SC data";
			log.error (msg, se);
			throw new CVersSQLException(msg	+ se.toString());
		}
				
		CVersPK cVersPK;
		PreparedStatement updateStatement = null;
		PreparedStatement syncUpdateStatement = null;
		PreparedStatement insertStatement = null;
		PreparedStatement findByRealKeyStatement = null;
		// Create connection and statements
		ComponentElementIF compElement = null;
		try {
			findByRealKeyStatement = connection.prepareStatement(FINDBYREALKEYSTRING);
			updateStatement = connection.prepareStatement(UPDATESTRING);
			syncUpdateStatement = connection.prepareStatement(SYNCUPDATESTRING);
			insertStatement = connection.prepareStatement(INSERTSTRING);
			
			if (cVersElements == null) {
				log.warning ("writeCVers cVersElements = null, status="+status);
				return;
			}
			if (cVersElements.length == 0) {
				log.warning ("writeCVers cVersElements.length == 0, status="+status);
				return;
			}
			log.info ("writeCVers with "+cVersElements.length+" elements, status="+status+" "+cVersElements [0].toString());
			
			// Let's go
			for (int i=0; i < cVersElements.length; i++) {
				// set the applytime
				ComponentFactoryIF compFactory = ComponentFactoryIF.getInstance();
				
				String servicelevel = cVersElements[i].getServiceLevel();
				
				log.debug ("writeCVers "+i+"/"+cVersElements.length+": "+cVersElements[i].toString());
				if (servicelevel != null) {
					
					// check for special problem where exportSC wrote SP.timestamp
					int pos = servicelevel.indexOf('.');
					if (pos >= 0) {
						log.warning ("writeCVers servicelevel='"+servicelevel+"' contains .");
						servicelevel = servicelevel.substring(0, pos);
						log.warning ("writeCVers servicelevel='"+servicelevel+"' reduced");
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
								cVersElements[i].getComponentVersion(),
								cVersElements[i].getComponentProvider(),
								cVersElements[i].getChecksumAlgorithmName (),
								cVersElements[i].getChecksumValue ()
								);

				log.debug ("writeCVers6: status="+status+" => CompElem: "+compElement.toString ());
				
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
					log.debug ("writeCVers6: INSERT: "+compElement.toString ());
					create(insertStatement, 0, compElement, status);
					checkDCUpdate (connection, compElement);
				} else {
					//We have to update the correct entry
					if (status == null) {status = "UPDATE";};
					
					// STATUS_CMS is used for updated items by CMS
					// this status must not be overwritten by SDM data
					
					if (status.equals(STATUS_SYNC)) {
						log.debug ("writeCVers6: SYNCUPDATE: "+compElement.toString ());
						save (syncUpdateStatement, cVersPK.hashnumber, compElement, status);
					} else {
						log.debug ("writeCVers6: UPDATE: "+compElement.toString ());
						save (updateStatement, cVersPK.hashnumber, compElement, status);
						checkDCUpdate (connection, compElement);
					}
				}
				SCVersionIF scv = compElement.getComponentVersion();
				if (scv != null) {
					long startTime = System.currentTimeMillis();
					scv.setCounter (compElement.getCounter());
					scv.setLocation (compElement.getLocation());
					scv.setProvider (compElement.getComponentProvider());
					writeSCVersion (connection, scv);
					showUsedTime ("writeSCVersion", startTime);
				}
			}
		} catch (SQLException se) {
			String msg = "Error writing new entries into COMPVERS6: (newest tc/SL/utiljddschema deployed?) ";
			if (compElement != null) {
				msg = msg + compElement.toString ();
			} else {
				msg = msg + "null";
			};
			log.error (msg, se);
			throw new CVersSQLException(msg	+ se.toString());
		} finally {
			closeStatement (updateStatement);
			closeStatement (syncUpdateStatement);
			closeStatement (insertStatement);
			closeStatement (findByRealKeyStatement);
			log.exiting("writeCVers6");
		}
	}

	private SyncResultIF writeToDatabase (Connection connection, ComponentElementIF[] cVersElements, String status, boolean stopAfterFailure)
				throws CVersAccessException {
		
		long overallSyncTime = System.currentTimeMillis();
		CVersPK cVersPK;
		PreparedStatement updateStatement = null;
		PreparedStatement syncUpdateStatement = null;
		PreparedStatement insertStatement = null;
		PreparedStatement findByRealKeyStatement = null;
		try {
			try {
				updateDCLevels (connection, cVersElements);
			} catch (SQLException se) {
				String msg = "Error setting DC levels from SC data";
				log.error (msg, se);
			}
			boolean noData = false;
			if (cVersElements == null) {
				log.warning ("writeToDatabase cVersElements = null, status="+status);
				noData = true;
			} else {
				if (cVersElements.length == 0) {
					log.warning ("writeToDatabase cVersElements.length == 0, status="+status);
					noData = true;
				}
			}
			if (noData) {
				SyncResultIF result = new SyncResult (new ComponentElementIF[0], new SyncElementStatusIF[0]);
				return result;
			}
			log.info ("writeToDatabase with "+cVersElements.length+" elements, status="+status+" "+cVersElements [0].toString());
			
			// Create connection and statements
			ComponentElementIF compElement = null;
			findByRealKeyStatement = connection.prepareStatement(FINDBYREALKEYSTRING);
			updateStatement = connection.prepareStatement(UPDATESTRING);
			syncUpdateStatement = connection.prepareStatement(SYNCUPDATESTRING);
			insertStatement = connection.prepareStatement(INSERTSTRING);
			
			ArrayList successful = new ArrayList ();
			ArrayList failed = new ArrayList ();
			
			for (int i=0; i < cVersElements.length; i++) {
				// set the applytime
				try {
					ComponentFactoryIF compFactory = ComponentFactoryIF.getInstance();
					
					String servicelevel = cVersElements[i].getServiceLevel();
					
					log.debug ("writeToDatabase "+i+"/"+cVersElements.length+": "+status+" "+cVersElements[i].toString());
					if (servicelevel != null) {
						
						// check for special problem where exportSC wrote SP.timestamp
						int pos = servicelevel.indexOf('.');
						if (pos >= 0) {
							log.warning ("writeToDatabase servicelevel='"+servicelevel+"' contains .");
							servicelevel = servicelevel.substring(0, pos);
							log.warning ("writeToDatabase servicelevel='"+servicelevel+"' reduced");
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
									cVersElements[i].getComponentVersion(),
									cVersElements[i].getComponentProvider(),
									cVersElements[i].getChecksumAlgorithmName (),
									cVersElements[i].getChecksumValue ()
									);
	
					log.debug ("writeToDatabase: status="+status+" => CompElem: "+compElement.toString ());
					
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
						log.debug ("writeToDatabase: INSERT: "+compElement.toString ());
						create(insertStatement, 0, compElement, status);
					} else {
						//We have to update the correct entry
						if (status == null) {status = "UPDATE";};
						
						// STATUS_CMS is used for updated items by CMS
						// this status must not be overwritten by SDM data
						
						if (status.equals(STATUS_SYNC)) {
							log.debug ("writeToDatabase: SYNCUPDATE: "+compElement.toString ());
							save (syncUpdateStatement, cVersPK.hashnumber, compElement, status);
						} else {
							log.debug ("writeToDatabase: UPDATE: "+compElement.toString ());
							save (updateStatement, cVersPK.hashnumber, compElement, status);
						}
					}
					SCVersionIF scv = compElement.getComponentVersion();
					if (scv != null) {
						long startTime = System.currentTimeMillis();
						scv.setCounter (compElement.getCounter());
						scv.setLocation (compElement.getLocation());
						scv.setProvider (compElement.getComponentProvider());
						writeSCVersion (connection, scv);
						showUsedTime ("writeSCVersion", startTime);
					}
					successful.add(cVersElements [i]);
				} catch (Throwable t) {
					if (stopAfterFailure) {
						String msg = "Error writing new entries into COMPVERS6: (newest tc/SL/utiljddschema deployed?) ";
						if (compElement != null) {
							msg = msg + compElement.toString ();
						} else {
							msg = msg + "null";
						};
						log.error (msg, t);
						throw new CVersSQLException(msg	+ t.toString());
					}
					failed.add (new SyncElementStatus (cVersElements [i], t.getMessage(), t));
				}
			}
			ComponentElementIF[] complist = (ComponentElementIF[]) successful.toArray(new ComponentElementIF [successful.size()]);
			SyncElementStatusIF[] failures = (SyncElementStatusIF[]) failed.toArray(new SyncElementStatusIF [failed.size()]);
			log.debug ("writeToDatabase "+complist.length+" successful, "+failures.length+" failures");
			if (failures.length > 0) {
				for (int i=0; i < failures.length; i++) {
					log.error (i+"/"+failures.length+" sync error: "+failures [i].getErrorText()+" "+failures [i].getElement().toString());
				}
			}
			SyncResultIF result = new SyncResult (complist, failures);
			return result;
		} catch (Throwable t) {
			if (stopAfterFailure) {
				throw new CVersAccessException (t.getMessage(), t);
			}
			SyncElementStatusIF[] failed = new SyncElementStatus [cVersElements.length];
			for (int i=0; i < cVersElements.length; i++) {
				failed [i] = new SyncElementStatus (cVersElements [i], t.getMessage(), t);
			}
			SyncResultIF result = new SyncResult (new ComponentElementIF[0], failed);
			return result;
		} finally {
			showUsedTime ("writeToDatabase1", overallSyncTime);
			closeStatement (updateStatement);
			closeStatement (syncUpdateStatement);
			closeStatement (insertStatement);
			closeStatement (findByRealKeyStatement);
			showUsedTime ("writeToDatabase2", overallSyncTime);
			log.exiting("writeToDatabase");
		}
	}
	
	/**
	 * remember sync connection to avoid performance problems.
	 * sync command is only used in case of stand alone call by SDM.
	 */
	private static Connection syncConnection = null;
	
	public void syncCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {
					
		log.entering("syncCVers6");
		try {
			if (syncConnection == null) {
				syncConnection = this.getConnection();
			} else {
				if (syncConnection.isClosed()) {
					syncConnection = this.getConnection();
				}
			}
			writeCVers (syncConnection, cVersElements, STATUS_SYNC);
		} catch (SQLException e) {
			log.error ("SQLException "+e.getMessage(), e);
			throw new CVersAccessException ("SQLException "+e.getMessage());
		} finally {
			log.exiting("syncCVers");
		}
	}

	public void syncCVers(Connection connection, ComponentElementIF[] cVersElements)
				throws CVersAccessException {
					log.entering("syncCVers");
		writeCVers (connection, cVersElements, STATUS_SYNC);
		log.exiting("syncCVers");
	}

	private void syncFinished (Connection connection) throws CVersAccessException {
		
		// remove all entries, which are not up-to-date
		// means: only SYNC and CMS entries survive.
		// SYNC entries are written by previous syncCVers calls from SDM
		// CMS entries are written by CMS via web service 
		
		log.entering("syncFinished");
		PreparedStatement removeStatement = null;
		PreparedStatement updateStatement = null;
		try {
			
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
			throw new CVersAccessException (msg);
		} finally {
			closeStatement(removeStatement);
			closeStatement(updateStatement);
			log.exiting("syncFinished");
		}
	}

	public void syncFinished ()	throws CVersAccessException {
		
		// remove all entries, which are not up-to-date
		// means: only SYNC and CMS entries survive.
		// SYNC entries are written by previous syncCVers calls from SDM
		// CMS entries are written by CMS via web service 
		
		log.entering("syncFinished");
		try {
			if (syncConnection == null) {
				syncConnection = this.getConnection();
			} else {
				if (syncConnection.isClosed()) {
					syncConnection = this.getConnection();
				}
			}
			syncFinished(syncConnection);
		} catch (SQLException se) {
			String msg = "syncFinished: ";
			log.error (msg, se);
			throw new CVersAccessException (msg);
		} finally {
			closeConnection(syncConnection);
			syncConnection = null;
			log.exiting("syncFinished");
		}

	}

	public void error (String name) throws CVersAccessException {

		String message = name + " not supported by DB schema 6";
		log.warning (message);
		throw new CVersAccessException (message);
	}

	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) {
		log.entering("syncCVersUpdate");
		Connection connection = null;
		SyncResultIF result = null;
		try {
			connection = this.getConnection();
			result = writeToDatabase (connection, cVersElements, STATUS_SYNC, false);
			return result;
		} catch (Throwable se) {
			String msg = "syncCVersUpdate: ";
			log.error (msg, se);
			return result;
		} finally {
			closeConnection(connection);
			log.exiting("syncCVersUpdate");
		}
	}
	
	protected void save (PreparedStatement updateStatement, int hashnumber,
				ComponentElementIF compElem, String status) throws SQLException {
		
		log.entering("save6");
		log.debug ("save: hash="+hashnumber+" '"+status+"' "+(compElem==null?"null":compElem.toString()));
		if (compElem == null) {
			log.error ("save6: compElem==null");
			return;
		}
		// get internal DB key
		int compID = HashKey.defaultHashFunction(compElem.getVendor(), compElem.getName());
		try {
			updateStatement.clearParameters();
			setStringValue (updateStatement, FN_VENDOR, 1, compElem.getVendor(), FL_VENDOR);
			setStringValue (updateStatement, FN_NAME, 2, compElem.getName(), FL_NAME);
			setStringValue (updateStatement, FN_LOCATION, 3, compElem.getLocation(), FL_LOCATION);
			setStringValue (updateStatement, FN_COUNTER, 4, compElem.getCounter(), FL_COUNTER);
			setStringValue (updateStatement, FN_SCVENDOR, 5, compElem.getSCVendor(), FL_SCVENDOR);
			setStringValue (updateStatement, FN_SCNAME, 6, compElem.getSCName(), FL_SCNAME);
			setStringValue (updateStatement, FN_RELEASE, 7, compElem.getRelease(), FL_RELEASE);
			setStringValue (updateStatement, FN_SERVICELEVEL, 8, compElem.getServiceLevel(), FL_SERVICELEVEL);
			setStringValue (updateStatement, FN_PATCHLEVEL, 9, compElem.getPatchLevel(), FL_PATCHLEVEL);
			setStringValue (updateStatement, FN_DELTAVERSION, 10, compElem.getDeltaVersion(), FL_DELTAVERSION);
			setStringValue (updateStatement, FN_UPDATEVERSION, 11, compElem.getUpdateVersion(), FL_UPDATEVERSION);
			int index = 12;
			if (!status.equals(STATUS_SYNC)) {
				setStringValue (updateStatement, FN_APPLYTIME, index++, compElem.getApplyTime(), FL_APPLYTIME);
			}
			setStringValue (updateStatement, FN_SCELEMENTYPEID, index++, compElem.getSCElementTypeID(), FL_SCELEMENTYPEID);
			setStringValue (updateStatement, FN_SPELEMENTYPEID, index++, compElem.getSPElementTypeID(), FL_SPELEMENTYPEID);
			setStringValue (updateStatement, FN_SPNAME, index++, compElem.getSPName(), FL_SPNAME);
			setStringValue (updateStatement, FN_SPVERSION, index++, compElem.getSPVersion(), FL_SPVERSION);
			setStringValue (updateStatement, FN_SERVERTYPE, index++, compElem.getServerType(), FL_SERVERTYPE);
			setStringValue (updateStatement, FN_SERVERNAME, index++, compElem.getSourceServer(), FL_SERVERNAME);
			setStringValue (updateStatement, FN_CHANGENUMBER, index++, compElem.getChangeNumber(), FL_CHANGENUMBER);
			setStringValue (updateStatement, FN_PROJECTNAME, index++, compElem.getProjectName(), FL_PROJECTNAME);
			setStringValue (updateStatement, FN_STATUS, index++, status, FL_STATUS);
			setStringValue (updateStatement, FN_PROVIDER, index++, compElem.getComponentProvider(), FL_PROVIDER);
			setStringValue (updateStatement, FN_ALGNAME, index++, compElem.getChecksumAlgorithmName(), FL_ALGNAME);
			setStringValue (updateStatement, FN_ALGVALUE, index++, compElem.getChecksumValue (), FL_ALGVALUE);
			setIntValue (updateStatement, FN_COMPID, index++, compID);
			setIntValue (updateStatement, FN_HASHNUMBER, index++, hashnumber);
			setStringValue (updateStatement, FN_TYPE, index++, compElem.getComponentType(), FL_TYPE);
			setStringValue (updateStatement, FN_SUBSYSTEM, index++, compElem.getSubsystem(), FL_SUBSYSTEM);
			if (status.equals(STATUS_SYNC)) {
				log.debug ("Update: sync status="+status+" "+compElem.toString());
				int rows = updateStatement.executeUpdate();
				log.debug ("Debug: "+rows+" rows updated for "+compElem.toString());
			} else {
				log.debug ("Update: normal status="+status+" "+compElem.toString());
				if (updateStatement.executeUpdate() < 1) {
					log.error("Row does not exist: "+compElem.toString());
					throw new NoSuchElementException("Row does not exist "+compElem.getName()+" "+compElem.getVendor());
				}
			}
		} catch(SQLException se) {
			log.error("Error executing SQL UPDATE BC_COMPVERS... "+compElem.toString(), se);
			throw se;
		}
		log.exiting("save6");
	}

	private CVersPK create(PreparedStatement insertStatement, int hashnumber,
				ComponentElementIF compElem, String status)
				throws SQLException, CVersAccessException {

		log.debug ("create: hash="+hashnumber+" '"+status+"' "+(compElem==null?"null":compElem.toString()));
		
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
							status,
							compElem.getComponentProvider(),
							compElem.getChecksumAlgorithmName(),
							compElem.getChecksumValue ());
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
               				String status,
               				String provider,
               				String algname,
               				String algvalue)
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
			setStringValue (insertStatement, FN_PROVIDER, 26, provider, FL_PROVIDER);
			setStringValue (insertStatement, FN_ALGNAME, 27, algname, FL_ALGNAME);
			setStringValue (insertStatement, FN_ALGVALUE, 28, algvalue, FL_ALGVALUE);

			if (insertStatement.executeUpdate() != 1) {
				String msg = compID + "," + _hashnumber + "," + _componentType + "," +
				  _subsystem + "," + _vendor + "," + _name + "," + 
				  _location + "," + _counter + "," + _scVendor + "," + _scName + "," + 
				  _release + "," + _serviceLevel + "," + _patchLevel + "," + _deltaVersion + "," +
				  _updateVersion + "," + _applyTime + "," + _scElementTypeID + "," +
				  _spElementTypeID + "," + _spName  + "," + _spVersion + "," +
				  status + "," + provider + "," + algname + "," + algvalue;
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
			  _spElementTypeID + "," + _spName  + "," + _spVersion + "," +
			  status + "," + provider + "," + algname + "," + algvalue;
			log.error("Error executing SQL INSERT INTO BC_COMPVERS... " +msg, se);
			throw se;
		}
	}

	public ComponentElementIF findByPrimaryKey(CVersPK _cVersPK)
			throws CVersAccessException {

		log.entering("findByPrimaryKey");
		log.debug ("findByPrimaryKey(CVersPK): "+_cVersPK.toString());
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
														resultSet.getString(20),
														null,
														resultSet.getString(21),
														resultSet.getString(22),
														resultSet.getString(23)
														);
			log.debug ("findByPrimaryKey(CVersPK): "+compVers.toString());
			return compVers;
		} catch(SQLException se) {
			String message = "Error executing SQL "+ FINDBYPKSTRING+" pk="+_cVersPK.toString();
			if (compVers != null) {
				message = message + " CV=" +compVers.toString();
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
		long startTime = System.currentTimeMillis();
		Connection connection = null;
		try {
			connection = this.getConnection();
			return findAll (connection);
		} catch (Exception exc) {
			log.error ("findAll "+exc.getMessage(), exc);
			throw new CVersAccessException ("findAll "+exc.getMessage());
		} finally {
			closeConnection(connection);
			showUsedTime ("findAll", startTime);
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
			int index = 0;
			while(resultSet.next()) {
				index++;
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
											resultSet.getString(22),
											null,
											resultSet.getString(23),
											resultSet.getString(24),
											resultSet.getString(25));
				log.debug ("findAll: "+index+" "+compVers.toString());
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
		int index = 0;
		while(resultSet.next()) {
			if (resultSet.getString(2).equalsIgnoreCase(_vendor) &&
				resultSet.getString(3).equalsIgnoreCase(_name)) {
					index++;
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
										resultSet.getString(21),
										resultSet.getString(22));
					log.debug ("findByRealKey "+index+" "+compID+" "+_vendor+" "+_name+" "+_componentType+" "+_subsystem+" "+cVersDBObject.toString());
			}
		} 
		log.debug("Found the following real key: "+((cVersDBObject==null)?"null":cVersDBObject.toString()));
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
			log.warning ("findByRealKey returns null for v="+_vendor+" n="+_name+" id="+compID);
			log.exiting("findByRealKey");
			return null;
		} else {
			CVersPK pk = new CVersPK(cVersDBObject.getCompID(),
							   cVersDBObject.getHashnumber(),
							   cVersDBObject.getComponentType(),
							   cVersDBObject.getSubsystem());
			log.debug ("findPKByRealKey: "+pk.toString());
			log.exiting("findByRealKey");
			return pk;
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
				log.debug ("findByRealKey, cVersDBObject == null for v="+_vendor+" n="+_name+" t="+_componentType+" s="+_subsystem);
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
											cVersDBObject.getProjectName(),
											null,
											cVersDBObject.getProvider(),
											cVersDBObject.getAlgorithmName (),
											cVersDBObject.getChecksumValue ()
											);			
			log.debug ("findByRealKey returns CV="+compVers);
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
			int index = 0;
			while(resultSet.next()) {
				index++;
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
											resultSet.getString(21),
											null,
											resultSet.getString(22),
											resultSet.getString(23),
											resultSet.getString(24));
				log.debug ("findByCompType: "+index+" "+compVers.toString());
				res.addElement(compVers);
			}
			log.debug ("findByCompType returns "+res.size()+" elements");
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
			int index = 0;
			while(resultSet.next()) {
				index++;
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
											resultSet.getString(20),
											null,
											resultSet.getString(21),
											resultSet.getString(22),
											resultSet.getString(23));

				log.debug ("findByTypeAndSubsys: "+index+" "+compVers.toString());
				res.addElement(compVers);
			}
			log.debug ("findByTypeAndSubsys returns "+res.size()+" elements");
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
				
		log.entering("displayDeletionEntries");
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getConnection();
			statement = connection.prepareStatement("SELECT COMPONENTTYPE, SUBSYSTEM, VENDOR, NAME," +
			" LOCATION, COUNTER, SCVENDOR, SCNAME, SAPRELEASE,"+
			" SERVICELEVEL, PATCHLEVEL, DELTAVERSION, UPDATEVERSION, APPLYTIME,"+
			" SCELEMENTTYPEID, SPELEMENTTYPEID, SPNAME, SPVERSION, "+
			" SERVERTYPE, SERVERNAME, CHANGENUMBER, PROJECTNAME, STATUS, PROVIDER FROM BC_COMPVERS " +
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
											resultSet.getString(22),
											null,
											resultSet.getString(23));
				String _status = resultSet.getString(23);
				log.info ("displayDeletionEntries, status='"+_status+"' deleted: "+compVers.toString());
			}
		} catch(Throwable se) {
			// $JL-EXC$
			log.warning ("Error reading CVERS, which are not in sync, "+" "+se.getMessage());
		} finally {
			closeStatement (statement);
			closeConnection (connection);
			log.exiting("displayDeletionEntries");
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
						log.debug("object will be removed: " + cVersPK.toString() + ", " + cVersElements[i].toString());
						remove(removeStatement, cVersPK);
					} else {
						log.debug("object does not exists in CVERS: " +	
									cVersElements[i].getVendor() +
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
			closeStatement (removeStatement);
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
				log.warning ("Row can't be deleted since it does not exist: " +
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
				log.debug ("readSCVersion returns null");
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
			SCLevelIF[] coverages = readCoverages (connection, name, vendor, location, counter);
			SCLevelIF[] compatibles = readCompatibles (connection, name, vendor, location, counter);
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

	private SCLevelIF[] readSCLevel (Connection connection, String tablename, String name, String vendor, String location, String counter) {

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

	private SCLevelIF[] readCoverages (Connection connection, String name, String vendor, String location, String counter) {

		return readSCLevel (connection, "BC_SCCOVERAGE", name, vendor, location, counter);
	}

	private SCLevelIF[] readCompatibles (Connection connection, String name, String vendor, String location, String counter) {

		return readSCLevel (connection, "BC_SCCOMPAT", name, vendor, location, counter);
	}

	public void writeSCVersion (Connection connection, SCVersionIF history) throws CVersCreateException, SQLException {
			
		log.entering ("writeSCVersion");
		if (history == null) {
			log.debug ("writeSCVersion: history == null");
			return;
		};

		deleteSCVersion (connection, history);

		String lastErrorMsg = "";
		SCVersionIF ptr = history;
		while (ptr != null) {
			try {
				if (this.isSCVersionExisting (connection, ptr)) {
					updateSCVersion (connection, ptr);
				} else {
					insertSCVersion (connection, ptr);
				}
			} catch (Exception e) {
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
					String msg = "deleteSCVersion: did not delete "+scv.toString();
					log.info (msg);
			} else {
				String msg = "deleteSCVersion: deleted sc="+scv.getName()+" vendor="+scv.getVendor();
				log.debug (msg);
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
			log.debug ("insertSCVersion finished: "+scv.toString());
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
					log.debug (msg);
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
		log.debug ("insertRequired started for "+required.length+" of "+scv.toString());
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
				log.debug ("insertRequired finished: "+i+" = "+required [i].toString());
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
			log.debug ("deleteSCLevel: result = "+result+" "+tablename);
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
			statement = connection.prepareStatement	 (selectstmt);
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
		   log.debug ("execSQL '"+query+"'");
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

	private long openConnections = 0;
	private long getCalls = 0;
	private long closeCalls = 0;
	
	private Connection getConnection () throws SQLException {
		long started = System.currentTimeMillis();
		Connection conn = this.dataSource.getConnection();
		openConnections++;
		openConnections++;
		conn.setAutoCommit(true);
		long finished = System.currentTimeMillis();
		log.debug ("getConnection took "+(finished - started)+" ms "+openConnections+" "+getCalls+" "+closeCalls);
		return conn;
	}

	private void closeConnection(Connection _connection) {
		long started = System.currentTimeMillis();
		try {
			closeCalls++;
			if (_connection != null) {
				_connection.close();
				openConnections--;
			}
		} catch(SQLException se) {
			log.warning ("connection close failed", se);
		}
		long finished = System.currentTimeMillis();
		log.debug ("closeConnection took "+(finished - started)+" ms "+openConnections+" "+getCalls+" "+closeCalls);
	}

}
