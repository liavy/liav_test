
package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis
 * @version 1.0
 */

public class DbModificationManager {
	private DbModificationController controller = null;

	public DbModificationManager(DbFactory factory) {
		controller = new DbModificationController(factory);
	}

	public int distribute(String archiveFileName, String logFileName) {
		Logger.addFileLog(logFileName);
		return controller.distribute(archiveFileName);
	}	
	
	public int delete(String archiveFileName, String logFileName) {
		Logger.addFileLog(logFileName);
		return controller.delete(archiveFileName);
	}	
}
