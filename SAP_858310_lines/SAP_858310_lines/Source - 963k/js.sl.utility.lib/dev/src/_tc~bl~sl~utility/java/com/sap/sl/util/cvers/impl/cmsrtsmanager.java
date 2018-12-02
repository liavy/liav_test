package com.sap.sl.util.cvers.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentElementXMLException;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.logging.api.SlUtilLogger;


/**
 *  Service to handle the BC_CMSRTS database table.
 */


public class CMSRTSManager extends CVersManager {

	private static final SlUtilLogger log =	SlUtilLogger.getLogger(CMSRTSManager.class.getName());

	protected static int CVERS_DB_SCHEMA = 0;	// undefined value

		// 1 = 24.05.2005 first version
  						  
	CMSRTSIF cVersDao = null;
	DataSource currentDataSource = null;
	
	private TableDescription loadTable (DataSource dataSource, String tablename) {
		
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			TableDescriptionFactory fac = TableDescriptionFactory.getInstance();
			TableDescription desc = fac.getTableDescription(connection, tablename);
			connection.close();
			if (desc == null) {
				return null;
			}
			log.debug ("Table "+tablename+" has "+desc.getFieldCount()+" fields");
			for (int i=0; i < desc.getFieldCount(); i++) {
				log.debug ("Table "+tablename+" has field "+desc.getFieldDescription(i));
			}
			return desc;
		} catch (Exception e) {
			// $JL-EXC$ 
			log.debug ("Can not load table description for table "+tablename+ " exc");
			return null;
		} catch (Throwable e) {
			// $JL-EXC$ 
			log.debug ("Can not load table description for table "+tablename+ " throw");
			return null;
		}
	}
	
	private void loadDBStructure (DataSource dataSource) {

		CVERS_DB_SCHEMA = 0;

		TableDescription desc = loadTable (dataSource, "BC_CMSRTS");
		if (desc == null) {
			log.error ("CMSRTS DB schema not found");
			return;
		}
		if (desc.getFieldIndex("NAME") >= 0) {
			CVERS_DB_SCHEMA = 1;
			return;
		}

		log.error ("CMSRTS DB schema BC_CMSRTS found, but without field NAME");
	}
	
	protected int openDBSource (DataSource dataSource) { 

		int oldschema = CVERS_DB_SCHEMA;
		this.cVersDao = new CMSRTS0(dataSource); 
		
		loadDBStructure (dataSource);
		currentDataSource = dataSource;
		
		if (oldschema != CVERS_DB_SCHEMA) {
			log.debug ("changing CMSRTS DB schema from "+oldschema+" to "+CVERS_DB_SCHEMA);
		} else {
			log.debug ("CMSRTS DB schema is "+CVERS_DB_SCHEMA);
		}
		
		if (CVERS_DB_SCHEMA == 1) { 
			this.cVersDao = new CMSRTS1 (dataSource); 
			return CVERS_DB_SCHEMA;
		}
		log.error ("Can not identify CMSRTS DB schema");
		CVERS_DB_SCHEMA = 0;
		return CVERS_DB_SCHEMA;
	}
	
	public CMSRTSManager() throws CVersAccessException {
		
		super ();
		DBConnector dbConnector = new DBConnector();
		openDBSource (dbConnector.getDataSource()); 
	}

	public CMSRTSManager(DataSource dataSource) throws CVersAccessException {
		
		super (dataSource);
		openDBSource (dataSource); 
	}
	
	public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException {
		return this.cVersDao.getModifiedComponents();
	}
	
	public void updateComponentData (String xml)
			throws CVersAccessException {
		
		log.entering("updateComponentData");
		try {
			if (xml == null) {
				log.warning("updateComponentData xml=null");
				return;
			}
			log.debug ("updateComponentData xml="+xml);
			XMLParser parser = new XMLParser();
			parser.parse(xml);
			
			String cmd = parser.getCommand();
			if (cmd.equals("updateSC")) {
				ComponentElementIF elem = parser.getComponentElement();
				if (elem == null) {
					log.error ("updateComponentData, parsed ComponentElement = null, xml="+xml);
					return;
				}
				CVersManager mgr = new CVersManager();
				log.debug ("writeCVers, start updateComponentData: "+elem.toString());
				mgr.writeSCHeader(elem);
				log.info ("writeCVers finish updateComponentData: "+elem.toString());
				return;
			}
			if (cmd.equals("configSCS")) {
				RTSComponentData[] data = parser.getComponentData();
				if (data == null) {
					log.error ("updateComponentData, parsed RTSComponentData[] = null, xml="+xml);
					return;
				}
				// update SC header in BC_COMPVERS first
				CVersManager mgr = new CVersManager();
				for (int i=0; i < data.length; i++) {
					if (data [i].isDevelopedSC()) {
						boolean isDevCons = false;
						if (data [i].getSysType().equalsIgnoreCase("dev")) {
							isDevCons = true;
						}
						if (data [i].getSysType().equalsIgnoreCase("cons")) {
							isDevCons = true;
						}
						if (isDevCons) {
							ComponentElementIF elem = convert (data [i], cVersDao.getTimeStamp());
							log.debug ("writeCVers, before write SC header for developed "+data [i].toString());
							mgr.writeSCHeader(elem);
							log.info ("writeCVers, SC header written for "+data [i].toString());
						} else {
							log.debug ("writeCVers, NO SC header written for "+data [i].toString()+" in non DEV/CONS system");
						}
					} else {
						log.debug ("writeCVers, NO SC header for required "+data [i].toString());
					}
				} 
				log.debug ("all SC headers written to BC_COMPVERS");
				cVersDao.updateComponentData(data);
				log.debug ("RTS component data written");
				return;
			}
			log.error ("updateComponentData, don't know command '"+cmd+"'");
		} catch (ComponentElementXMLException e) {
			// $JL-EXC$ 
			log.error ("updateComponentData, "+e.getMessage(), e);
		} catch (CVersAccessException e) {
			// $JL-EXC$ 
			log.error ("CVersAccessException, "+e.getMessage(), e);
		} finally {
			log.exiting("updateComponentData");
		}
	}
	private ComponentElementIF convert (RTSComponentData compdata, String timestamp) {

		ComponentElementIF compElem = 
				ComponentFactoryIF.getInstance().createComponentElement(
					compdata.getVendor(), compdata.getName(), 
					ComponentElementIF.COMPONENTTYPE_SC, 
					ComponentElementIF.DEFAULTSUBSYSTEM,
					compdata.getLocation(), timestamp, 
					compdata.getVendor(), compdata.getName(), 
					compdata.getRelease(), 
					compdata.getServicelevel(),
					compdata.getPatchlevel(),
					ComponentElementIF.FULLVERSION,
					ComponentElementIF.FULLVERSION, 
					timestamp,
					null, null,
					null, null,
					"NWDI", 	// servertype
					null, 		// perforce server
					null, 		// changelist number
					null);		// rootdir
		return compElem;
	}
	
}