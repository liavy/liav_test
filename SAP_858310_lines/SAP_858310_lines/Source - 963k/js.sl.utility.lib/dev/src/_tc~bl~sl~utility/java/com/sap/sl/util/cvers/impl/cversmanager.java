package com.sap.sl.util.cvers.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.CVersManagerIF;
import com.sap.sl.util.cvers.api.SyncElementStatusIF;
import com.sap.sl.util.cvers.api.SyncResultIF;
import com.sap.sl.util.logging.api.SlUtilLogger;


/**
 *  The central CVERS instance to handle the allowed processes
 *  (read and write CVERS)
 *
 *@author     md
 *@created    16. Mai 2003
 *@version    1.0
 */


public class CVersManager implements CVersManagerIF{

	private static final SlUtilLogger log =	SlUtilLogger.getLogger(CVersManager.class.getName());

	protected int CVERS_DB_SCHEMA = 0;	// undefined value

		// 6 = COMPVERS erzeugt am 29.10.2005 gueltig ab SP16 bzw. 7.0 SP6 oder 7.1 RIO 
		//     check for tablefield BC_COMPVERS:ALGNAME
		//
		// 5 = COMPVERS erzeugt am ??? gueltig ab SP14 bzw. 7.0 SP4
		//     check for tablefield BC_COMPVERS:STATUS
		//
		// 4 = COMPVERS erzeugt am 07.03.2005 gueltig ab SP12 bzw. 7.0
		//     check for tables BC_SCVERSION, BC_SCREQUIRED, BC_SCCOVERAGE, BC_SCCOMPAT, BC_COMPVERS:STATUS
		//
		// 3 = COMPVERS erzeugt am 03.12.2004 gueltig ab SP11
		//     BC_COMPVERS:SERVERNAME
		//     21.12.2004 SP11 LOCATION length increased to 40 
		//     03.12.2004 SP11 added PATCHLEVEL
		//     07.12.2004 SP11 added SERVERTYPE,SERVERNAME,CHANGENUMBER,PROJECTNAME
		//
		// 2 = COMPVERS erzeugt am 10.03.2003 gueltig bis 02.12.2004 (SP5 bis SP10)
		//     BC_COMPVERS:COMPID
		//
		// 1 = CVERS    erzeugt am  7.10.2003 gueltig bis 09.03.2003 (bis SP4)
		//     not implemented here
  	
  	public int getDBSchema () {
  		return CVERS_DB_SCHEMA;				  
  	}
  	
	public Connection getConnection () throws SQLException {
		long started = System.currentTimeMillis();
		try {
			Connection conn = this.currentDataSource.getConnection();
			return conn;
		} catch (Throwable t) {
			log.error("getConnection "+t.getMessage(), t);
		} finally {
			long finished = System.currentTimeMillis();
			log.debug ("getConnection CVersManager took "+(finished - started)+" ms");
		}
		return null;
	}

	private void closeConnection(Connection _connection) {
		long started = System.currentTimeMillis();
		try {
			if (_connection != null) {
				log.debug ("closeConnection");
				_connection.close();
			}
		} catch(SQLException se) {
			log.warning ("CVersManager, connection close failed", se);
		} finally {
			long finished = System.currentTimeMillis();
			log.debug ("closeConnection CVersManager took "+(finished - started)+" ms");
		}
	}

	CVersDaoIF cVersDao = null;
	DataSource currentDataSource = null;

	private TableDescription loadTable (String tablename, boolean mandatory) throws CVersAccessException {
		
		Connection connection = null;
		boolean error = true;
		Throwable exc = null;
		try {
			connection = getConnection();
			TableDescriptionFactory fac = TableDescriptionFactory.getInstance();
			TableDescription desc = fac.getTableDescription(connection, tablename);
			this.closeConnection(connection);
			if (desc == null) {
				error = true;
				return null;
			}
			log.debug ("Table "+tablename+" has "+desc.getFieldCount()+" fields");
			for (int i=0; i < desc.getFieldCount(); i++) {
				log.debug ("Table "+tablename+" has field "+desc.getFieldDescription(i));
			}
			error = false;
			return desc;
		} catch (Exception e) {
			log.warning ("Can not load table description for table "+tablename+ " exc: "+e.getMessage());
			error = true;
			exc = e;
			return null;
		} catch (Throwable e) {
			log.warning ("Can not load table description for table "+tablename+ " throw: "+e.getMessage());
			error = true;
			exc = e;
			return null;
		} finally {
			if (mandatory && error) {
				String msg = "Can not load mandatory table "+tablename+", desc == null";
				log.error (msg, exc);
				if (exc == null) {
					throw new CVersAccessException (msg);
				} else {
					throw new CVersAccessException (msg, exc);
				}
			}
			closeConnection (connection);
		}
	} 
	
	private TableDescription loadTable (String tablename) throws CVersAccessException {
		
		return loadTable (tablename, false);
	}
	
	private void loadDBStructure () throws CVersAccessException {

		TableDescription desc = loadTable ("BC_SCVERSION");
		if (desc != null) {
			desc = loadTable ("BC_SCREQUIRED");
		};
		if (desc != null) {
			desc = loadTable ("BC_SCCOVERAGE");
		};
		if (desc != null) {
			desc = loadTable ("BC_SCCOMPAT");
		};
		
		if (desc != null) {
			desc = loadTable ("BC_CMSRTS");
			if (desc != null) {
				desc = loadTable ("BC_COMPVERS", true);
				if (desc == null) {
					CVERS_DB_SCHEMA = 0;
					return;		// ERROR BC_COMPVERS must exist --> DB schema = 0
				}
				if (desc.getFieldIndex("ALGNAME") >= 0) {
					CVERS_DB_SCHEMA = 6;
					return;
				}
				if (desc.getFieldIndex("PROVIDER") >= 0) {
					CVERS_DB_SCHEMA = 5;
					return;
				}
			}
		};

		desc = loadTable ("BC_COMPVERS", true);
		if (desc == null) {
			CVERS_DB_SCHEMA = 0;
			desc = loadTable ("BC_CVERS");
			if (desc == null) {
				log.error ("No SLUTIL DB schema found, neither BC_COMPVERS nor BC_CVERS");
				return;
			};
			log.error ("SLUTIL DB schema BC_CVERS found but without field SERVICELEVEL");
			return;
		}
		if (desc.getFieldIndex("STATUS") >= 0) {
			CVERS_DB_SCHEMA = 4;
			return;
		}
		if (desc.getFieldIndex("SERVERNAME") >= 0) {
			CVERS_DB_SCHEMA = 3;
			return;
		}
		if (desc.getFieldIndex("COMPID") >= 0) {
			CVERS_DB_SCHEMA = 2;
			return;
		}
		log.error ("SLUTIL DB schema BC_COMPVERS found, but without field COMPID");
	}
	
	protected int openDBSource (DataSource dataSource) throws CVersAccessException { 
		
		long startTime = System.currentTimeMillis();
		currentDataSource = dataSource;
		loadDBStructure ();
		
		boolean found = false;
		
		if (CVERS_DB_SCHEMA == 6) {  // from 6.40 SP16, 7.0 SP6, 7.1 RIO
			this.cVersDao = new CVersDao6(dataSource); 
			found = true;
		}
		if (CVERS_DB_SCHEMA == 5) {  // from SP15
			this.cVersDao = new CVersDao5(dataSource); 
			found = true;
		}
		if (CVERS_DB_SCHEMA == 4) {  // from SP12
			this.cVersDao = new CVersDao4(dataSource); 
			found = true;
		}
		if (CVERS_DB_SCHEMA == 3) {  // from SP11
			this.cVersDao = new CVersDao3(dataSource); 
			found = true;
		}
		if (CVERS_DB_SCHEMA == 2) {  // from SP5
			this.cVersDao = new CVersDao2(dataSource); 
			found = true;
		}
		
		if (!found) {
			log.error ("Can not identify SLUTIL DB schema");
			CVERS_DB_SCHEMA = 0;
			this.cVersDao = new CVersDao0 (dataSource); 
		}
		checkUsedTime ("openDBSource", startTime);
		return CVERS_DB_SCHEMA;
	}
	
	private void showUsedTime (String name, long startTime) {
		
		long finishTime = System.currentTimeMillis();
		long usedTime = finishTime - startTime;
		log.debug (name+" took "+usedTime+" ms");
	}
	
	private void checkUsedTime (String name, long startTime, long maxtime) {
		
		long finishTime = System.currentTimeMillis();
		long usedTime = finishTime - startTime;
		if (usedTime > maxtime) {
			log.info (name+" took "+usedTime+" ms");
		} else {
			log.debug (name+" took "+usedTime+" ms");
		}
	}
	
	private void checkUsedTime (String name, long startTime) {
		
		checkUsedTime(name, startTime, 1000);
	}
	
	public CVersManager() throws CVersAccessException {
		long startTime = System.currentTimeMillis();
		DBConnector dbConnector = new DBConnector();
		openDBSource (dbConnector.getDataSource()); 
		checkUsedTime ("CVersManager engine", startTime);
	}

	public CVersManager(DataSource _dataSource) throws CVersAccessException {
		long startTime = System.currentTimeMillis();
		openDBSource (_dataSource);
		checkUsedTime ("CVersManager standAloneMode", startTime);
	}

	public ComponentElementIF[] readCVers() throws CVersAccessException {
		
		long startTime = System.currentTimeMillis();
		Collection cVers = cVersDao.findAll();
		Iterator it = cVers.iterator();
		ComponentElementIF[] compElems = null;
		if (cVers != null) {
			compElems = new ComponentElementIF[cVers.size()];
			int i = 0;
			while (it.hasNext()) {
				compElems[i] = (ComponentElementIF) it.next();
				i++;
			}
		}
		checkUsedTime ("readCVers()", startTime);
		return compElems;  
  	}

	public ComponentElementIF[] readCVers(String componentType)
			throws CVersAccessException {
				
		long startTime = System.currentTimeMillis();
		Collection cVers = cVersDao.findByCompType(componentType);
		Iterator it = cVers.iterator();
		ComponentElementIF[] compElems = null;
		if (cVers != null) {
			compElems = new ComponentElementIF[cVers.size()];
			int i = 0;
			while (it.hasNext()) {
				compElems[i] = (ComponentElementIF) it.next();
				i++;
			}
		}
		checkUsedTime ("readCVers(type="+componentType+")", startTime);
		return compElems;
	}

	public ComponentElementIF[] readCVers(String componentType, String subsystem)
			throws CVersAccessException {
				
		long startTime = System.currentTimeMillis();
		Collection cVers = cVersDao.findByTypeAndSubsys(componentType, subsystem);
		Iterator it = cVers.iterator();
		ComponentElementIF[] compElems = null;
		if (cVers != null) {
			compElems = new ComponentElementIF[cVers.size()];
			int i = 0;
			while (it.hasNext()) {
				compElems[i] = (ComponentElementIF) it.next();
				i++;
			}
		}
		checkUsedTime ("readCVers(type="+componentType+",subsys="+subsystem+")", startTime);
		return compElems;
	}

	public ComponentElementIF readCVers(String vendor, String name, String componentType,
			String subsystem) throws CVersAccessException {
		return cVersDao.findByRealKey(vendor, name, componentType, subsystem);
	}

	private void checkForDBUpdate (ComponentElementIF[] cVersElements) throws CVersAccessException {

		log.entering("checkForDBUpdate");
		if (cVersElements == null) {
			log.exiting("checkForDBUpdate cVersElements == null");
			return;
		}
		long startTime = System.currentTimeMillis();
		boolean newDBSchema = false;
		for (int i=0; i < cVersElements.length; i++) {
			String name = cVersElements [i].getName();
			log.debug ("checkForDBUpdate "+i+"/"+cVersElements.length+" = '"+name+"'");
			if (name.equals("tc/SL/UTIL_JDD")) {	// old name up to 7.0
				newDBSchema = true;
				break;
			}
			if (name.equals("tc/sl/utiljddschema")) {	// new name from 7.1
				newDBSchema = true;
				break;
			}
		}
		checkUsedTime ("checkForDBUpdate with "+cVersElements.length+" elements", startTime);
		if (newDBSchema) {
			log.info ("new CVERS DB schema detected, checking DB structure");
			openDBSource (currentDataSource);
		} else {
			log.debug ("checkForDBUpdate nothing changed");
		}
		log.exiting("checkForDBUpdate "+newDBSchema);
	}

	public void writeCVers(ComponentElementIF[] cVersElements)
			throws CVersAccessException {

		/*
		 * writeCVers is called by SDM after the deployment was successful.
		 * Therefore check, if a new COMPVERS DB schema was deployment.
		 * 
		 * If so, then the corresponding (newer) DB structure shall be used.
		 * 
		 */
		
		log.entering ("writeCVers");
		checkForDBUpdate (cVersElements);
		long startTime = System.currentTimeMillis();
		cVersDao.writeCVers(cVersElements);
		checkUsedTime ("writeCVers with "+cVersElements.length+" elements", startTime);
		log.exiting ("writeCVers");
	}
	
	/**
	 * Internal method for NWDI/CMS to mark modified components and write/update
	 * SC data from CMS during autodeployment.
	 * @param cVersElement data of autodeployed software component
	 * @throws CVersAccessException if an error occurrs
	 */
	public void writeSCHeader (ComponentElementIF cVersElements)
			throws CVersAccessException {

		log.entering ("writeCVers");
		long startTime = System.currentTimeMillis();
		cVersDao.writeSCHeader (cVersElements);
		checkUsedTime ("writeSCHeader", startTime);
		log.exiting ("writeCVers");
	}

	private long overallSyncTime = 0;
	
	/**
	 * @deprecated
	 */
	public void syncCVers(ComponentElementIF[] cVersElements)
			throws CVersAccessException {

		/*
		 * syncCVers is called by SDM to resync CVERS with repository entries.
		 * There is no start sync method ...
		 */
		if (cVersElements == null) {
			log.entering("syncCVers with null element");
		} else {
			log.entering("syncCVers with "+cVersElements.length+" elements");
		}
		long startTime = System.currentTimeMillis();
		if (overallSyncTime == 0) {
			// remember time of first sync call ...
			overallSyncTime = startTime;
		}
		cVersDao.syncCVers (cVersElements);
		checkUsedTime ("syncCVers", startTime);
		log.exiting("syncCVers");
	}
	
	/**
	 * @deprecated
	 */
	public void syncFinished () throws CVersAccessException {

		/*
		 * This method must be called after all syncCVers calls have been done.
		 */
		 
		log.entering ("syncFinished");
		openDBSource (currentDataSource);
		long startTime = System.currentTimeMillis();
		cVersDao.syncFinished ();
		checkUsedTime ("syncFinished", startTime);
		showUsedTime ("syncFinished", overallSyncTime);
		overallSyncTime = 0;	// initialize for next sync calls
		log.exiting ("syncFinished");
	}

	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) {
		if (cVersElements == null) {
			log.entering("syncCVersUpdate with null element");
			return new SyncResult (new ComponentElementIF[0], new SyncElementStatusIF [0]);
		}
		log.entering("syncCVersUpdate with "+cVersElements.length+" elements");
		long startTime = System.currentTimeMillis();
		SyncResultIF result = null;
		try {
			result = cVersDao.syncCVersUpdate (cVersElements);
			if (result.failedSyncs().length == 0) {
				cVersDao.syncFinished ();
			} else {
				log.error ("syncCVersUpdate failed, no finish called");
			}
		} catch (Throwable t) {
			log.error ("Error during syncFinished");
		}
		checkUsedTime ("syncCVersUpdate", startTime);
		log.exiting("syncCVersUpdate");
		return result;
	}
	
	public void removeCVers(ComponentElementIF[] cVersElements)
			throws CVersAccessException {
		log.entering ("removeCVers");
		cVersDao.removeCVers(cVersElements);
		log.exiting ("removeCVers");
	}

	private void checkNullPointer (String text, Object compElem) throws CVersAccessException {
		
		if (compElem == null) {
			String msg = "readSCVersion called with "+text+" = null";
			log.error (msg);
			throw new CVersAccessException ();
		}
	}
	
	public SCVersionIF readSCVersion (ComponentElementIF compElem) throws CVersAccessException {
		
		log.entering("readSCVersion");
		checkNullPointer ("component", compElem);
		checkNullPointer ("name", compElem.getName());
		checkNullPointer ("vendor", compElem.getVendor());
		checkNullPointer ("location", compElem.getLocation()); 
		checkNullPointer ("counter", compElem.getCounter());
		checkNullPointer ("release", compElem.getRelease());
		SCVersionIF scv = cVersDao.readSCVersion (compElem.getName(), compElem.getVendor(), compElem.getLocation(), compElem.getCounter(), compElem.getComponentProvider(),
					compElem.getRelease(),
					compElem.getServiceLevel(),
					compElem.getPatchLevel());
		showUsedTime ("readSCVersion", overallSyncTime);
		log.exiting("readSCVersion");
		return scv;
	}

	public Vector execSQL (String query) {

		Vector array = cVersDao.execSQL(query);
		return (array);
	}

	private boolean checkDevConsSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromDEVSystem()) {
				return true;
			}
			if (mod [i].comesFromCONSSystem()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkTestProdSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromTESTSystem()) {
				return true;
			}
			if (mod [i].comesFromPRODSystem()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkDEVSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromDEVSystem()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkCONSSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromCONSSystem()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkTESTSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromTESTSystem()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkPRODSystem (ModifiedComponentIF[] mod) {
		
		for (int i=0; i < mod.length; i++) {
			if (mod [i].comesFromPRODSystem()) {
				return true;
			}
		}
		return false;
	}
	
	public int getNwdiRole () throws CVersAccessException {
		ModifiedComponentIF[] modified = this.getModifiedComponents();
		if (checkDevConsSystem(modified)) {
			return NWDI_SYSTEMROLE_DEV;
		}
		if (checkTestProdSystem(modified)) {
			return NWDI_SYSTEMROLE_PROD;
		}
		return NWDI_SYSTEMROLE_NONE;
	}

	/**
	 * @deprecated
	 */
	public boolean isDEVsystem () throws CVersAccessException {
		ModifiedComponentIF[] modified = this.getModifiedComponents();
		return checkDEVSystem (modified);
	}

	/**
	 * @deprecated
	 */
	public boolean isCONSsystem () throws CVersAccessException {
		ModifiedComponentIF[] modified = this.getModifiedComponents();
		return checkCONSSystem (modified);
	}

	/**
	 * @deprecated
	 */
	public boolean isTESTsystem () throws CVersAccessException {
		ModifiedComponentIF[] modified = this.getModifiedComponents();
		return checkTESTSystem (modified);
	}

	/**
	 * @deprecated
	 */
	public boolean isPRODsystem () throws CVersAccessException {
		ModifiedComponentIF[] modified = this.getModifiedComponents();
		return checkPRODSystem (modified);
	}
	
	public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException {
		CMSRTSManager mgr = new CMSRTSManager (this.currentDataSource);
		ModifiedComponentIF[] mod = mgr.getModifiedComponents();
		if (mod == null) {
			mod = new ModifiedComponentIF[0];
		}
		return mod;
	}
	
	
}
