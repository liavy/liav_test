package com.sap.sl.util.cvers.impl;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.sap.sl.util.logging.api.SlUtilLogger;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.CVersLookupException;

/**
 *  The central class for providing the connection to DB
 *
 *@author     md
 *@created    12. Juni 2003
 *@version    1.0
 */

public class DBConnector {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(DBConnector.class.getName());

	DataSource dataSource;

	public DBConnector() {
	}

	public DataSource getDataSource() throws CVersAccessException {
		log.entering("getDataSource");
		Context jndiCtx;
		try {
			Properties ctxProp = new Properties();
			ctxProp.put(Context.INITIAL_CONTEXT_FACTORY, "com.sap.engine.services.jndi.InitialContextFactoryImpl");
			// special engine command...
			ctxProp.put("domain", "true");

			log.debug("get initial context...");
			jndiCtx = new InitialContext(ctxProp);
		} catch(Exception e) {
			log.error("Error initializing context:", e);
			throw new CVersLookupException("Error initializing context:" + e.toString());
		}
		try {			
			log.debug("get data source...");
			dataSource = (DataSource) jndiCtx.lookup("jdbc/notx/SAP/BC_UME");
    	} catch(Exception e2) {
    		log.error("Error getting dataSource:", e2);
			throw new CVersLookupException("Error getting dataSource:" + e2.toString());
    	}
    	log.debug("got data source!");
    	log.exiting("getDataSource");
    	return dataSource;
	}

}
