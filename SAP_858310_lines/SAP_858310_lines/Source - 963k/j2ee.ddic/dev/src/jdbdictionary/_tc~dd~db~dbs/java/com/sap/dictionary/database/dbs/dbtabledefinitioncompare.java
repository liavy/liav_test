package com.sap.dictionary.database.dbs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class DbTableDefinitionCompare implements DbsConstants {
	private static String[] SUFFIXES = {".gdbtable", ".gdbview"};
	private static final Location loc = Location
			.getLocation(DbModificationController.class);
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	public static Map getDifferences(String archiveFileNameOrig,
			String archiveFileNameDest) {
		DbFactory factory = null;
		HashMap result = null;
		DbChangeInfo changeInfo = null;
		HashMap info = new HashMap();
		Set keySet = null;
		Iterator iter = null;
		String tabname = null;
		Action action_db = null;
		Action action_total = null;
		String[] databaseNames = Database.getDatabaseNames();
		String allDbs = "";
		for (int i = 0; i < databaseNames.length; i++) {
			if ("INFORMIX".equalsIgnoreCase(databaseNames[i]))
				continue;
			factory = new DbFactory(Database.getDatabase(databaseNames[i]));
			// controller = new
			// DbModificationController(factory,false,null,"C:\\tmp\\difflog",false);
			result = compareTables(factory, archiveFileNameOrig, archiveFileNameDest);
			keySet = result.keySet();
			iter = keySet.iterator();
			while (iter.hasNext()) {
				tabname = (String) iter.next();
				changeInfo = (DbChangeInfo) info.get(tabname);
				if (changeInfo == null)
					changeInfo = new DbChangeInfo(tabname);
				action_db = (Action) result.get(tabname);
				System.out.println(tabname + " " + action_db + " " + databaseNames[i]);
				action_total = changeInfo.getAction();
				if (action_db.compareTo(action_total) > 0) {
					changeInfo.setAction(action_db);
					changeInfo.setDatabaseNames(databaseNames[i]);
				}
				if (action_db.compareTo(action_total) == 0) {
					if ("ALL".equalsIgnoreCase(changeInfo.getDatabaseNames()))
						allDbs = databaseNames[i];
					else
						allDbs = changeInfo.getDatabaseNames() + " " + databaseNames[i];
					changeInfo.setDatabaseNames(allDbs);
				}
				info.put(tabname, changeInfo);
			}
		}
		return info;
	}

	public static HashMap compareTables(DbFactory factory,
			String archiveFileNameOrig, String archiveFileNameDest) {
		HashMap tablesOrig = new HashMap();
		HashMap tablesDest = new HashMap();
		HashMap results = new HashMap();

		// Read archive with original tables
		ArchiveReader ar = new ArchiveReader(archiveFileNameOrig, SUFFIXES, true);
		ArchiveEntry entry = null;
		String name = null;
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(name.lastIndexOf('/') + 1, name.indexOf("."));
				tablesOrig.put(name, entry.getString());
			}
		}
		// Read destination archive
		ar = new ArchiveReader(archiveFileNameDest, SUFFIXES, true);
		entry = null;
		name = null;
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(name.lastIndexOf('/') + 1, name.indexOf("."));
				tablesDest.put(name, entry.getString());
			}
		}
		Set keys = tablesOrig.keySet();
		Iterator iter = keys.iterator();
		XmlMap tableMap = null;
		DbTable tabViaXmlDest = null;
		DbTable tabViaXmlOrig = null;
		DbTableDifference difference = null;
		Action action = null;
		while (iter.hasNext()) {
			try {
				tableMap = DbTableModificationAnalyser.extractXmlMap(tablesOrig
						.get(iter.next()));
				tabViaXmlOrig = factory.makeTable();
				tabViaXmlOrig.setCommonContentViaXml(tableMap);
				if (tablesDest.get(tabViaXmlOrig.getName()) == null) {
					// Table not found in destination set
					results.put(tabViaXmlOrig.getName(), Action.DROP);
				}
				tableMap = DbTableModificationAnalyser.extractXmlMap(tablesDest
						.get(tabViaXmlOrig.getName()));
				tabViaXmlDest = factory.makeTable();
				tabViaXmlDest.setCommonContentViaXml(tableMap);
				difference = tabViaXmlOrig.compareTo(tabViaXmlDest);
				if (difference == null)
					action = Action.NOTHING;
				else
					action = difference.getAction();
				results.put(tabViaXmlOrig.getName(), action);
			} catch (Exception ex) {
				cat.warning(loc, TABLE_ANALYSE_ERR, new Object[]{tabViaXmlOrig
						.getName()});
				continue;
			}
		}
		return results;
	}

	public static HashMap checkTableStructure(String archiveFileNameOrig,
			String archiveFileNameDest) {
		HashMap actionResults = new HashMap();
		HashMap actionResultsPerDb = new HashMap();
		HashMap actionResultsForAllDbs = new HashMap();

		HashMap dbTitles = Database.getVendorTitleForDatabaseName();
		Iterator iter = dbTitles.keySet().iterator();
		String databaseName = null;
		DbFactory factoryPerDb = null;
		DbModificationController controller = null;
		Iterator iter1;
		while (iter.hasNext()) {
			databaseName = (String) iter.next();
			factoryPerDb = new DbFactory(Database.getDatabase(databaseName));
			actionResultsPerDb = compareTables(factoryPerDb,archiveFileNameOrig,
					archiveFileNameDest);
			String name;
			Action action;
			iter1 = actionResultsPerDb.keySet().iterator();
			Action actionT = null;
			while (iter1.hasNext()) {
				name = (String) iter1.next();
				action = (Action) actionResultsPerDb.get(name);
				if (actionResults.containsKey(name)) {
					actionT = (Action) actionResults.get(name);
					actionResults.put(name, Action.max(action, actionT));
				} else
					actionResults.put(name, action);
				if (actionResultsForAllDbs.containsKey(name)) {
					((HashMap) actionResultsForAllDbs.get(name))
							.put(databaseName, action);
				} else {
					HashMap resultPerDb = new HashMap();
					resultPerDb.put(databaseName, action);
					actionResultsForAllDbs.put(name, resultPerDb);
				}
			}
		}
		return actionResults;
	}

	public static class DbChangeInfo {
		private String tabname = null;
		private Action action = null;
		private String databases = null;

		public DbChangeInfo(String tabname) {
			this.tabname = tabname;
			action = Action.NOTHING;
			databases = "ALL";
		}

		public String getTabname() {
			return tabname;
		}

		public Action getAction() {
			return action;
		}

		public void setAction(Action action) {
			this.action = action;
		}

		public String getDatabaseNames() {
			return databases;
		}

		public void setDatabaseNames(String databases) {
			this.databases = databases;
		}

		public String toString() {
			return tabname + ": " + action + " " + databases;
		}
	}

}
