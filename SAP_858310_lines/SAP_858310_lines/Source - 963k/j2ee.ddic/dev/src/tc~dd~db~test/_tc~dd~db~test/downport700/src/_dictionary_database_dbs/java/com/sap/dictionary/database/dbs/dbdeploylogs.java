package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.xml.sax.InputSource;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.StreamLog;

public class DbDeployLogs extends DbBasicTable implements DbsSeverity {
	private static final boolean dbLoggingActive = true;
	private static final DbDeployLogs m_dummy = new Dummy();
	private static final String DEPLOY_LOGS_TABLE_NAME = "BC_DDDBDP";
	private final static int INITIAL_BUFFER_CAPACITY = 512;
	private static final String EOL = System.getProperty("line.separator");
	private static final Location loc = Location.getLocation(DbDeployLogs.class);
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private static final DateFormat FORMATTER = 
		new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS",Locale.US);
  private static TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT");
	static {
		FORMATTER.setTimeZone(TIME_ZONE_GMT);
	}
	private static ThreadLocal instanceContainer = new ThreadLocal ();
	private static int m_maxLogsNumber = 15;
	private DbFactory m_factory = null;
	private String m_objname = null;
	private ByteArrayOutputStream m_messages = null;
	private StreamLog m_log = null;
	private String m_action = " ";
	private String m_phase = " ";
	private long m_timestmp = 0;
	private WeakReference m_refController = null;
	
	DbDeployLogs() {	
	}
	
	private DbDeployLogs(DbFactory factory) { 		
		super(factory, DEPLOY_LOGS_TABLE_NAME);
		m_factory = factory;
		instanceContainer.set(this);
	}
	
	public static DbDeployLogs getInstance() {
		DbDeployLogs instance = (DbDeployLogs) instanceContainer.get();
		if (instance != null)
			return instance;
		else
			return m_dummy;
	}
	
	public static synchronized DbDeployLogs getInstance(
	    DbModificationController controller) {
		DbFactory factory = controller.getFactory();
		if (factory.getConnection() == null || !dbLoggingActive)
			return m_dummy;
		DbDeployLogs currentInstance = (DbDeployLogs) instanceContainer.get();
		if (currentInstance != null
		    && (currentInstance.m_factory == null ||
		    		currentInstance.m_refController.get() == controller))
			return currentInstance;
		try {
			if (!factory.makeTable(
			    factory.getEnvironment().getRuntimeObjectsTableName()).existsOnDb()
			    || !factory.makeTable("BC_SYNCLOG").existsOnDb())
				return m_dummy;
		} catch (Exception e) {
			return m_dummy;
		}
		instanceContainer.set(m_dummy);
		DbDeployLogs newInstance = new DbDeployLogs(factory);
		newInstance.m_refController = new WeakReference(controller);
		return newInstance;
	}

	public void openObjectLog(String objname,String type,String phase,
			long timestmp) {
		if (m_objname != null || !type.equalsIgnoreCase("T"))
			return;
		initializeObjLog();
		m_objname = objname;
		m_phase = phase;
		if (timestmp != 0)
			m_timestmp = timestmp;
		else
			m_timestmp = getTimestamp();
		m_messages = new ByteArrayOutputStream(128);
		m_log = new TableLog(m_messages, new DbTraceFormatter()); //$JL-LOG_AND_TRACE$
		m_log.setName(m_objname + "_" + m_phase + "_" + Thread.currentThread().getName());
		cat.addLog(m_log);
		cat.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");
	}
	
//	public static void openObjLog(DbFactory factory,String name,String type,
//			String phase,long timestmp) {
//		getInstance(factory).openObjectLog(name,type,phase,timestmp);	
//	}
	
	public static void openObjLog(String name,String type,String phase,
			long timestmp) {
		getInstance().openObjectLog(name,type,phase,timestmp);	
	}
		
	public void closeObjectLog(String objname, Action action,
			boolean containsErrors) {
		if (objname != null && !objname.equalsIgnoreCase(m_objname))
			return;
		if ((m_messages == null)
		    || (m_phase.equalsIgnoreCase("A") && !containsErrors &&
		    		(action == null || action == Action.NOTHING))) {
			initializeObjLog();
			return;
		}
		try {
	    m_messages.flush();
    } catch (IOException e1) {
    	initializeObjLog();
			return;
    }
		String logtext = m_messages.toString();
		if (logtext.trim().equals("")) {
			initializeObjLog();
			return;
		}
		try {
			if (containsErrors) {
				DbModificationController controller = 
					(DbModificationController)m_refController.get();
				if (controller != null) 
					controller.writeToInfoTextStream(m_messages);
			}
			m_action = action == null ? " " : action.getSign();
			insertToDb(this,containsErrors,logtext);
		} catch (Exception e) {
			JddException.log(e, cat, Severity.ERROR, loc);
			e.printStackTrace();
		} finally {
			initializeObjLog();
			closeStatements();
		}
	}
	
	public static synchronized void insertToDb(DbDeployLogs log,
			boolean containsErrors,String logtext) throws Exception {
		ResultSet oldLogs = log.getRows(new String[] { "NAME" },
		    new String[] { log.m_objname });
		ArrayList timestamps = new ArrayList();
		while (oldLogs.next()) {
			timestamps.add(new Long(oldLogs.getLong("TIMESTMP")));
		}
		if (!timestamps.isEmpty()) {
			Collections.sort(timestamps);
			long laststmp = ((Long) timestamps.get(timestamps.size() - 1))
			    .longValue();
			if (laststmp >= log.m_timestmp)
				log.m_timestmp = laststmp + 1;
			for (int i = 0; i < timestamps.size() - m_maxLogsNumber + 1; i++) {
				log.removeRows(new String[] { "NAME", "TIMESTMP" }, new Object[] {
				    log.m_objname, timestamps.get(i) });
			}
		}
		log.insertRow(new Object[] { log.m_objname, new Long(log.m_timestmp),
				log.m_phase, "T",log.m_action, containsErrors ? "X" : " ", logtext });
		log.factory.getTools().commit();		
	}
	
	public static void closeObjLog(String objname,Action action,
			boolean containsErrors) {
		getInstance().closeObjectLog(objname,action,containsErrors);	
	}
	
//	public void closeCurrentObjLog(boolean containsErrors) {
//		closeObjectLog(null,null,containsErrors);	
//	}
//	
//	public static void closeCurrObjLog() {
//		getInstance().closeCurrentObjLog();	
//	}
	
	public static long getTimestamp() {
		return DbTools.currentTime();
	}
	
	public void initializeObjLog() {
		m_objname = null;
		if (m_log != null) {
			m_log.close();
			cat.removeLog(m_log);
		}
		m_log = null;
		m_messages = null;
		m_action = " ";
		m_phase = " ";
		m_timestmp = 0;
	}
	
	public Record[] getObjectLogs(String name) throws Exception {
		ArrayList al = new ArrayList();
		PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
				m_factory.getConnection(),
				"SELECT * FROM \"BC_DDDBDP\" WHERE \"NAME\" = ? ORDER BY \"TIMESTMP\"");
		try {
	    stmt.setString(1,name);
	    ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
	    	al.add(new Record(rs.getString("NAME"),rs.getLong("TIMESTMP"),
	    			rs.getString("PHASE"),rs.getString("TYPE"),rs.getString("ACTION"),
	    			rs.getString("ERRORS"),
	    			getString(new BufferedReader(rs.getCharacterStream("MESSAGES")))));
	    }
    } finally {
	    stmt.close();
    }
    
		Record[] records = new Record[al.size()];
		for (int i = 0; i < records.length; i++) {
			records[i] = (Record) al.get(i);
    }
		return records;
	}
	
	public Record[] getObjectLogs(String min, String max) throws Exception {
		ArrayList al = new ArrayList();
		PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
				m_factory.getConnection(),
				"SELECT * FROM \"BC_DDDBDP\" WHERE \"TIMESTMP\" > ? AND \"TIMESTMP\" < ? ORDER BY \"TIMESTMP\"");
		try {
	    stmt.setLong(1,FORMATTER.parse(min).getTime());
	    stmt.setLong(2,FORMATTER.parse(max).getTime());
	    ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
	    	al.add(new Record(rs.getString("NAME"),rs.getLong("TIMESTMP"),
	    			rs.getString("PHASE"),rs.getString("TYPE"),rs.getString("ACTION"),
	    			rs.getString("ERRORS"),
	    			getString(new BufferedReader(rs.getCharacterStream("MESSAGES")))));
	    }
    } finally {
	    stmt.close();
    }
    
		Record[] records = new Record[al.size()];
		for (int i = 0; i < records.length; i++) {
			records[i] = (Record) al.get(i);
    }
		return records;
	}
	
	public Record[] getObjectLogsInfo(String name) throws Exception {
		ArrayList al = new ArrayList();
		PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
				m_factory.getConnection(),
				"SELECT \"NAME\",\"TIMESTMP\",\"PHASE\",\"TYPE\",\"ACTION\",\"ERRORS\" FROM \"BC_DDDBDP\" WHERE \"NAME\" = ? ORDER BY \"TIMESTMP\"");
		try {
	    stmt.setString(1,name);
	    ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
	    	al.add(new Record(rs.getString("NAME"),rs.getLong("TIMESTMP"),
	    			rs.getString("PHASE"),rs.getString("TYPE"),rs.getString("ACTION"),
	    			rs.getString("ERRORS"),
	    			null));
	    }
    } finally {
	    stmt.close();
    }
    
		Record[] records = new Record[al.size()];
		for (int i = 0; i < records.length; i++) {
			records[i] = (Record) al.get(i);
    }
		return records;
	}
	
	public Record[] getObjectLogsInfo(String min, String max) throws Exception {
		ArrayList al = new ArrayList();
		PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
				m_factory.getConnection(),
				"SELECT \"NAME\",\"TIMESTMP\",\"PHASE\",\"TYPE\",\"ACTION\",\"ERRORS\" FROM \"BC_DDDBDP\" WHERE \"TIMESTMP\" > ? AND \"TIMESTMP\" < ? ORDER BY \"TIMESTMP\"");
		try {
	    stmt.setLong(1,FORMATTER.parse(min).getTime());
	    stmt.setLong(2,FORMATTER.parse(max).getTime());
	    ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
	    	al.add(new Record(rs.getString("NAME"),rs.getLong("TIMESTMP"),
	    			rs.getString("PHASE"),rs.getString("TYPE"),rs.getString("ACTION"),
	    			rs.getString("ERRORS"),
	    			null));
	    }
    } finally {
	    stmt.close();
    }
    
		Record[] records = new Record[al.size()];
		for (int i = 0; i < records.length; i++) {
			records[i] = (Record) al.get(i);
    }
		return records;
	}
	
	private static class Dummy extends DbDeployLogs {
		public void openObjectLog(String objname,String type,String phase,
				long timestmp) {
		}
		
		public void closeObjectLog(String objname,Action action) {
		}
	}
	
	public class TableLog extends StreamLog { 
		private ByteArrayOutputStream correspondingStream = null; 
		public TableLog(ByteArrayOutputStream stream,DbTraceFormatter formatter) {
			super(stream,formatter);
			correspondingStream = stream;
		}
		public boolean isFiltersAgreeng(LogRecord record) {
			if (getInstance().m_messages == correspondingStream)
				return super.isFiltersAgreeing(record);
			else
				return false;
		}
	};
	
	public static class Record {
		String name = null;
		long timestamp = 0;
		String phase = null;
		String type = null;
		String action = null;
		String errors = null;
		String messages = null;
		public Record(String name) {
			
		}
		public Record(String name,long timestamp,String phase,
				String type,String action,String errors,String messages) {
			this.name = name;
			this.timestamp = timestamp;
			this.phase = phase;
			this.type = type;
			this.action = action;
			this.errors = errors;
			this.messages = messages;
		}
		
		public String toString() {
	  	String res = "*** ";
	  	res += name + " ";
	  	res += phase + " ";
	  	if (errors != null && errors.trim().length() != 0)
	  		res += "ERRORS! ";
	  	res += action + " ";
	  	res += FORMATTER.format(new java.util.Date(timestamp)) + "-UTC ";
	  	res += type + " ";
	  	if (messages != null && messages.trim().length() != 0)
	  		res += "\n" + messages;
	  	return res; 
		}
		
		public boolean containsErrors() {
			if (errors != null && errors.trim().length() != 0)
				return true;
			else
				return false;			
		}
		
		public boolean containsConversion() {
			if ("U".equals(action))
				return true;
			else
				return false;			
		}
	}
	
}
