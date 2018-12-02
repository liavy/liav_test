package com.sap.dictionary.database.dbs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.sap.tc.logging.*;

import java.util.Collection;

/**
 * @author d003550
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class Logger {
	private static HashMap locations = new HashMap();
	private static Category category = Category.getCategory("/Ddic/Database");
	private static PropertiesConfigurator config = null;
	public static final String PACKAGE_PREFIX = "com.sap.dictionary.database";
	private static String configurationName = null;
	public static final String CATEGORY_NAME = "/Jddic/Database";
	private static final Location loc = Location
	    .getLocation("com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, CATEGORY_NAME);
	private static boolean isAdjusted = false;
	private ByteArrayOutputStream infoText = null;
	private StreamLog tempLog = null;

	public synchronized static void adjust() {
		if (isAdjusted)
			return;
		cat
		    .setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
		cat.setClassLoader(DbTable.class.getClassLoader());
		cat.setEffectiveSeverity(Severity.INFO);
		Collection logs = cat.getLogs();
		if (logs == null)
			return;
		for (Iterator iterator = logs.iterator(); iterator.hasNext();) {
			Log log = (Log) iterator.next();
			log.setFormatter(new DbTraceFormatter());
		}
		isAdjusted = true;
	}
	
	public synchronized static void addFileLog(String name) {
		if (!isAdjusted)
			adjust();
		FileLog fileLog = new FileLog(name);
		fileLog.setFormatter(new DbTraceFormatter());
		cat.addLog(fileLog);
	}

	public Logger() {
		adjust();
	}

	/**
	 * Switches on the trace for the choosen severity.
	 * 
	 * @param severity
	 *          All messages with this severity are considered in an additional
	 *          log. For documentation of severity e.g. severity levels, see
	 * @com.sap.tc.logging.Severity
	 */
	public void switchOn(int severity) {
		if (tempLog != null)
			cat.removeLog(tempLog);
		infoText = new ByteArrayOutputStream(128);
		tempLog = new StreamLog(infoText, new DbTraceFormatter()); //$JL-LOG_AND_TRACE$
		tempLog.setName("temp_" + super.toString());
		tempLog.setEffectiveSeverity(severity);
		cat.addLog(tempLog);
	}

	public void switchOn() {
		switchOn(Severity.INFO);
	}

	/**
	 * Switches off the trace. Log-messages written after calling this method are
	 * not considered. This method should always be executed if switchOnTrace(int)
	 * was called because it clears the static logging variables
	 */
	public void switchOff() {
		if (tempLog == null)
			return;
		if (infoText != null) {
			try {
				infoText.flush();
			} catch (IOException e) {
				infoText = null;
			}
		}
		tempLog.close();
		cat.removeLog(tempLog);
		tempLog = null;
	}

	/**
	 * Delivers the addtional log as string, if this was initiated by
	 * switchOnTrace(int) and stopped with switchOffTrace() before and after
	 * relevant parts
	 */
	public String getText() {
		if (infoText != null)
			return infoText.toString();
		else
			return null;
	}

	/**
	 * Method to get a Location-object which is used to write log messages. When
	 * calling this method a default configuration for the location
	 * com.sap.dictionary.database is set. Due to the hierarchical naming feature
	 * of location you can use this default configuration by using as extention
	 * db-packagename.classname
	 * 
	 * @param extention
	 *          db-abbreviation.classname, e.g mss.classname,ora.classname
	 * @return the location-object for logging
	 */
	public static Location getLocation(String extention) {
		if (extention.equalsIgnoreCase(""))
			return Location.getLocation(PACKAGE_PREFIX);
		else
			return Location.getLocation(PACKAGE_PREFIX + "." + extention);
	}

	/**
	 * Method to get a Category-object. The category is a second classification
	 * for logs. If you write your log-messages by using this category-object all
	 * logs can be found with a category /DICTIONARY/DATABASE
	 * 
	 * @return the category-object for /DICTIONARY/DATABASE *
	 */
	public static Category getCategory() {
		return category;
	}

	/**
	 * Method to activate a new logging-configuration. To define such a
	 * configuration see
	 * 
	 * @see LoggingConfigration
	 * @param configuration
	 *          the name of the configuration which should become active
	 */
	public static void setLoggingConfiguration(String configuration) {
		// if (configurationName == null) {
		config = new PropertiesConfigurator(LoggingConfiguration
		    .getConfiguration(configuration), Logger.class.getClassLoader());
		config.configure();
		configurationName = configuration;
		// }
	}

	public static boolean hasLogs(Category cat) {
		Category ccat = cat;
		Collection logs;
		while (ccat != null) {
			logs = ccat.getLogs();
			if ((logs != null) && (!logs.isEmpty()))
				return true;
			if (ccat.getName().equalsIgnoreCase(CATEGORY_NAME))
				return false;
			ccat = ccat.getParent();
		}
		return false;
	}

	public static boolean hasLogs(Location loc) {
		Location cloc = loc;
		Collection logs;
		while (cloc != null) {
			logs = cloc.getLogs();
			if ((logs != null) && (!logs.isEmpty()))
				return true;
			if (cloc.getName().equalsIgnoreCase(PACKAGE_PREFIX))
				return false;
			cloc = cloc.getParent();
		}
		return false;
	}

	public static void init() {
		loc
		    .setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
		cat
		    .setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
		loc.setClassLoader(DbTable.class.getClassLoader());
		cat.setClassLoader(DbTable.class.getClassLoader());
		loc.setEffectiveSeverity(Severity.ERROR);
		cat.setEffectiveSeverity(Severity.INFO);
	}

	public static void init(String fileName) {
		FileLog fileLog = new FileLog(fileName);
		fileLog.setFormatter(new DbTraceFormatter());
		cat.addLog(fileLog);
		init();
	}

	public static void adjust(Category cat) {
		cat
		    .setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
		cat.setClassLoader(DbTable.class.getClassLoader());
		cat.setEffectiveSeverity(Severity.INFO);
		Collection logs = cat.getLogs();
		if (logs == null)
			return;
		for (Iterator iterator = logs.iterator(); iterator.hasNext();) {
			Log log = (Log) iterator.next();
			log.setFormatter(new DbTraceFormatter());
		}
	}

}
