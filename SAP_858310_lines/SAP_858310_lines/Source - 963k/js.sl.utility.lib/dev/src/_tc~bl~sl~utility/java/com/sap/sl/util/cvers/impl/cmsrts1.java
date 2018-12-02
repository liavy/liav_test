 
package com.sap.sl.util.cvers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  CMSRTS Database Access Object:
 *  The central class for reading and writing table BC_CMSRTS
 *
 *@author     JM
 *@created    18.01.2005
 *@version    1.0
 */

public class CMSRTS1 implements CMSRTSIF {

	private static boolean testing = true;
	
	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CMSRTS1.class.getName());

	private final static String REMOVESTRING =
		"DELETE FROM BC_CMSRTS WHERE NAME = ? AND VENDOR = ? AND CMSNAME = ? AND TRACKNAME = ? AND LOCATION = ?";

	private final static String SELECTSTRING =
		"SELECT NAME, VENDOR,CMSNAME, CMSURL, CONFIGTYPE, TRACKNAME, SYSTYPE, LOCATION, PROVIDER, APPLYTIME, RELEASE, SERVICELEVEL, PATCHLEVEL " + 
		"FROM BC_CMSRTS ";

	private final static String INSERTSTRING =
		"INSERT INTO BC_CMSRTS " +
		"(NAME, VENDOR,CMSNAME, CMSURL, CONFIGTYPE, TRACKNAME, SYSTYPE, LOCATION, PROVIDER, APPLYTIME, RELEASE, SERVICELEVEL, PATCHLEVEL) VALUES " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final int LEN_TRACKNAME = 8;
	
	private DataSource dataSource = null;

	public CMSRTS1(DataSource _dataSource) {
		this.dataSource = _dataSource;
	}
	
	public void updateComponentData (RTSComponentData[] elements)
				throws CVersAccessException {
					
		Connection connection = null;
		try {
			connection = this.getConnection();
			writeRTS (connection, elements);
		} catch (SQLException e) {
			log.error ("writeRTS "+e.getMessage(), e);
			throw new CVersAccessException ("writeRTS "+e.getMessage());
		} finally {
			closeConnection(connection);
		}
	}
	
	private String checkNull (String value) {
		
		if (value == null) {
			return " ";		
		}
		if (value.equals("")) {
			return " ";		
		}
		return value;
	}
	
	private String checkSize (String value, int maxsize) {
		if (value == null) {
			return value;
		}
		if (value.length() > maxsize) {
			log.warning("checkSize '"+value+"' is too long, max "+maxsize);
			return value.substring (0, maxsize);
		}
		return value;
	}
	
	private void writeRTS (Connection connection, RTSComponentData[] elements)
				throws CVersAccessException, SQLException {

		log.entering("writeRTS");
		PreparedStatement removeStatement = null;
		PreparedStatement insertStatement = null;
		
		RTSComponentData current = null;

		try {
			removeStatement = connection.prepareStatement(REMOVESTRING);
			insertStatement = connection.prepareStatement(INSERTSTRING);

			for (int i=0; i < elements.length; i++) {
				
				current = elements [i];
				try {
					removeStatement.clearParameters();
					removeStatement.setString(1, checkNull (elements [i].getName()));
					removeStatement.setString(2, checkNull (elements [i].getVendor()));
					removeStatement.setString(3, checkNull (elements [i].getCmsName()));
					removeStatement.setString(4, checkNull (checkSize (elements [i].getTrackname(), LEN_TRACKNAME)));
					removeStatement.setString(5, checkNull (elements [i].getLocation()));
					removeStatement.executeUpdate();
					log.debug ("component deleted: ", elements [i].toString());
				} catch (Exception exc) {
					// $JL-EXC$ 
					log.error ("Exc, Error during delete of "+elements[i].toString(), exc);
				} catch (Throwable thr) {
					// $JL-EXC$ 
					log.error ("Thr, Error during delete of "+elements[i].toString(), thr);
				}
				
				if (elements [i].deleteEntry()) {
					log.info ("no new data, only delete of component: ", elements [i].toString());
					continue;
				};
				
				log.debug ("insert component: "+elements [i].toString());
				int index = 1;
				insertStatement.clearParameters();
				insertStatement.setString(index++, checkNull (elements [i].getName()));
				insertStatement.setString(index++, checkNull (elements [i].getVendor()));
				insertStatement.setString(index++, checkNull (elements [i].getCmsName()));
				insertStatement.setString(index++, checkNull (elements [i].getCmsURL()));
				if (elements [i].isDevelopedSC()) {
					insertStatement.setString(index++, "dev");
				} else {
					insertStatement.setString(index++, "req");
				}
				insertStatement.setString(index++, checkNull (checkSize (elements [i].getTrackname(), LEN_TRACKNAME)));
				insertStatement.setString(index++, checkNull (elements [i].getSysType()));
				insertStatement.setString(index++, checkNull (elements [i].getLocation()));
				insertStatement.setString(index++, checkNull (elements [i].getProvider()));
				insertStatement.setString(index++, getTimeStamp());
				insertStatement.setString(index++, checkNull (elements [i].getRelease()));
				insertStatement.setString(index++, checkNull (elements [i].getServicelevel()));
				insertStatement.setString(index++, checkNull (elements [i].getPatchlevel()));

				if (insertStatement.executeUpdate() != 1) {
					log.error("Error adding row: "+elements [i].toString());
					throw new CVersAccessException("Error adding row "+elements [i].toString());
				}
				log.info ("component inserted: ", elements [i].toString());
			}
		} catch(SQLException se) {
			String msg = "";
			if (current != null) {
				msg = current.toString();
			}
			log.error ("Error executing SQL INSERT INTO BC_CMSRTS ... " +msg, se);
			throw se;
		} finally {
			closeStatement (removeStatement);
			closeStatement (insertStatement);
			log.exiting("writeRTS");
		}
	}

	public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException {
		
		Connection connection = null;
		try {
			connection = this.getConnection();
			return getModifiedComponents (connection);
		} catch (SQLException e) {
			log.error ("getModifiedComponents "+e.getMessage(), e);
			throw new CVersAccessException ("getModifiedComponents "+e.getMessage());
		} finally {
			closeConnection(connection);
		}
	}
	
	public ModifiedComponentIF[] getModifiedComponents (Connection connection) throws CVersAccessException, SQLException {

		log.entering("getModifiedComponents");
		PreparedStatement selectStatement = null;
		
		ModifiedComponent[] result = null;
		Vector tempresult = new Vector ();
		ModifiedComponent modified = null;
		
		try {
			selectStatement = connection.prepareStatement(SELECTSTRING);
			ResultSet resultSet = selectStatement.executeQuery();
			Vector res=new Vector();
			while(resultSet.next()) {
				int index = 1;
				String name = resultSet.getString(index++);
				String vendor = resultSet.getString(index++);
				String cmsname = resultSet.getString(index++);
				String cmsurl = resultSet.getString(index++);
				String comptype = resultSet.getString(index++);
				String trackname = resultSet.getString(index++);
				String systype = resultSet.getString(index++);
				String location = resultSet.getString(index++);
				String provider = resultSet.getString(index++);
				String applytime = resultSet.getString(index++);
				String release = resultSet.getString(index++);
				String servicelevel = resultSet.getString(index++);
				String patchlevel = resultSet.getString(index++);
				modified = new ModifiedComponent (vendor, name, comptype, provider, cmsname, cmsurl, trackname, 
											systype, release, servicelevel, patchlevel);
				tempresult.add (modified);
				log.debug ("modified component read: ", modified.toString());
			}
			result = new ModifiedComponent [tempresult.size()];
			for (int i=0; i < tempresult.size(); i++) {
				result [i] = (ModifiedComponent) tempresult.elementAt(i);
			}
			return result;
		} catch(SQLException se) {
			String msg = "";
			if (modified != null) {
				msg = modified.toString();
			}
			log.error ("Error executing SQL SELECT FROM BC_CMSRTS ... " +msg, se);
			throw se;
		} finally {
			closeStatement (selectStatement);
			log.exiting("getModifiedComponents");
		}
	}
	
	public static final String TS_SEC_FORMAT="yyyyMMddHHmmss";

	public String getTimeStamp() {
		Calendar calendar=new GregorianCalendar();
		SimpleDateFormat formatter = new SimpleDateFormat (TS_SEC_FORMAT);
		return formatter.format(calendar.getTime());
	}

	private Connection getConnection () throws SQLException {
		Connection conn = this.dataSource.getConnection();
		conn.setAutoCommit(true);
		return conn;
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
