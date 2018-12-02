package com.sap.dictionary.database.veris;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sap.dictionary.database.dbs.Database;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTraceFormatter;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.veris.Veri.LineInfo;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.ConsoleLog;
import com.sap.tc.logging.FileLog;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class VeriCommon extends VeriStarter {
	public static final int LOG = 0; //no log = 0;console = 1;file = 2; both = 3
	private static int SAP = 1;
	private static int MSS = 0;
	private static int ORA = 0;
	private static int DB6 = 0;
	private static int DB4 = 0;
	private static int DB2 = 0;
	private static String[] fileNames = new String[]{
		"binary.xml"};
	private static String SELECTION = "select.txt";
	private static int SEVERITY = Severity.INFO;
	private static final String LOGDIR = "C:\\logs\\database dev\\";
	public static final SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat("ddMMyyyy_kkmmss");
	private static final Location loc = Location.getLocation(
		"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private Connection con;

	public VeriCommon() {
		super();
	}

	public static void exec() throws Exception {
		new TestSuite(VeriCommon.class).run(new TestResult());
	}

	public void setUp() throws Exception {
		con = getConnection();
		loc.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    cat.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    loc.setClassLoader(DbTable.class.getClassLoader());
    cat.setClassLoader(DbTable.class.getClassLoader());
    if (LOG == 2 || LOG == 3) {
			FileLog fileLog = new FileLog(LOGDIR + DATE_FORMAT.format(new Date())
					+ ".xls");
			fileLog.setFormatter(new DbTraceFormatter());
			loc.addLog(fileLog);
		}
		if (LOG == 1 || LOG == 3) {
			ConsoleLog consoleLog = new ConsoleLog();
			consoleLog.setFormatter(new DbTraceFormatter());
			loc.addLog(consoleLog);
		}
		loc.setEffectiveSeverity(SEVERITY);
		cat.setEffectiveSeverity(Severity.FATAL);
	}

	public void tearDown() throws Exception {
		con.close();
	}

	public void testModifications() throws Exception {
		String abbr = Database.getDatabase(con).getAbbreviation();
		if ("SAP".equalsIgnoreCase(abbr) && SAP == 0)
			return;
		if ("MSS".equalsIgnoreCase(abbr) && MSS == 0)
			return;
		if ("ORA".equalsIgnoreCase(abbr) && ORA == 0)
			return;
		if ("DB6".equalsIgnoreCase(abbr) && DB6 == 0)
			return;
		if ("DB4".equalsIgnoreCase(abbr) && DB4 == 0)
			return;
		if ("DB2".equalsIgnoreCase(abbr) && DB2 == 0)
			return;
		exec(con, false);
	}

	/*
	 * public void testModificationsWithStop() { exec(conns, true,
	 * VeriStarter.startedViaMain); }
	 */

	public static boolean exec(Connection con, boolean breakIfError)
	    throws Exception {
		ByteArrayOutputStream baos = null;
		PrintStream ps = null;
		boolean isResultOk = true;
		if (fileNames == null || fileNames.length == 0)
			fileNames = getSelectionFileContent();
		for (int i = 0; i < fileNames.length; i++) {
			LineInfo li = getLineInfo(con, fileNames[i]);
			if (li == null)
				continue;
			baos = new ByteArrayOutputStream(128);
			ps = new PrintStream(baos);
			VeriCore veriCore = new VeriCore(li.getFileName(), ps);
			ps.flush();
			if (!veriCore.exec(con, breakIfError, li.getIncl(), li.getExcl())) {
				Assert.fail(baos.toString() + " " + veriCore.getInfoText());
				ps.close();
				baos.close();
				if (breakIfError) {
					Assert.fail("????????????VERI  ERRORS  ????????????");
					Assert.fail("============================================");
					Assert.fail(" ");
					return false;
				}
				isResultOk = false;
			}
		}
		// break;
		return isResultOk;
	}
	
	private static String[] getSelectionFileContent() throws Exception {
		InputStream stream = Veri.class.getResourceAsStream("verifiles/"
		    + SELECTION);
		ArrayList fnames = new ArrayList();
		String currentFileName;
		int numberOfSelectedNames = 0;
		if (stream != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			// String filter = filterAdjust(FILTER);
			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.startsWith("*") && !line.startsWith("//") 
							&& !line.equals(""))
						fnames.add(line);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			numberOfSelectedNames = fnames.size();
			reader.close();
			stream.close();
		}
		String[] res = new String[fnames.size()];
		for (int i = 0; i < res.length; i++) {
	    res[i] = (String)fnames.get(i);
    }
		return res;
	}
	
	private static LineInfo getLineInfo(Connection con,String line) 
			throws Exception {
		String currentFileName = null;
		HashMap incl = null;
		HashMap excl = null;
		int spacepos = line.indexOf(' ');
		if (spacepos < 0)
			currentFileName = "verifiles/" + line;
		else {
			currentFileName = "verifiles/" + line.substring(0, spacepos);
			line = line.substring(spacepos + 1);
			int comentpos = line.indexOf("//");
			if (comentpos >= 0)
				line = line.substring(0,comentpos);
			char start = line.charAt(0);
			if (start == '+' || start == '-') {
				String abbr = Database.getDatabase(con).getAbbreviation().toLowerCase();
				int pos = line.indexOf(abbr);
				if (pos < 0)
					pos = line.indexOf("all");
				if (start == '+') {
					if (pos < 0)
						return null;
					if (line.length() > pos + 3 && line.charAt(pos + 3) == '(')
						incl = makeFilterMap(line,pos + 3);
				}					
				if (start == '-' && pos >= 0) {
					if (line.length() > pos + 3 && line.charAt(pos + 3) == '(')
						excl = makeFilterMap(line,pos + 3);
					else
						return null;
				}
			}
		}
		return new LineInfo(currentFileName,incl,excl);
	}
		
	private static HashMap makeFilterMap(String line,int pos) {
		HashMap res = new HashMap();
		Integer key = null;
		line = line.substring(pos + 1).trim();
		StringBuffer sb = new StringBuffer();
		ArrayList al = new ArrayList();
		char[] cs = line.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] == '/') {
				key = Integer.valueOf(sb.toString());
				sb = new StringBuffer();
				continue;
			}
			if (cs[i] == ',') {
				al.add(Integer.valueOf(sb.toString()));
				sb = new StringBuffer();
				continue;
			}
			if (cs[i] == ' ' || cs[i] == ')') {
				al.add(Integer.valueOf(sb.toString()));
				sb = new StringBuffer();
				int[] ar = new int[al.size()];
				for (int j = 0; j < ar.length; j++) {
	        ar[j] = ((Integer)al.get(j)).intValue();
        }
				al.clear();
				res.put(key, ar);
				continue;
			}
	    sb.append(cs[i]);
    }
		return res;		
	}
	
	public static class LineInfo {
		HashMap incl = null;
		HashMap excl = null;
		String fileName = null;
		private LineInfo(String fileName, HashMap incl,HashMap excl) {
			this.incl = incl;
			this.excl = excl;
			this.fileName = fileName;
		}
		private HashMap getIncl() {
			return incl;
		}
		private HashMap getExcl() {
			return excl;
		}
		private String getFileName() {
			return fileName;
		}
		public String toString() {
			String res = "\nfileName = " + fileName + "\ninclusive =" ;
			if (incl == null)
				res += "null";
			else {
				Set s = incl.entrySet();
				for (Iterator iterator = s.iterator(); iterator.hasNext();) {
					Map.Entry me = (Map.Entry) iterator.next();
					Integer key = (Integer) me.getKey();
					int[] ar = (int[])me.getValue();
					res += " " + key + "/";
					for (int i = 0; i < ar.length; i++) {
						res += "|" + ar[i] + "|";
					}
				}
			}
			res += "\nexclusiv =";
			if (excl == null)
				res += "null";
			else {
				Set s = excl.entrySet();
				for (Iterator iterator = s.iterator(); iterator.hasNext();) {
					Map.Entry me = (Map.Entry) iterator.next();
					Integer key = (Integer) me.getKey();
					int[] ar = (int[])me.getValue();
					res += " " + key + "/";
					for (int i = 0; i < ar.length; i++) {
						res += "|" + ar[i] + "|";
					}
				}
			}
			return res;
		}
	}
	
//	private static String[] fileNames = new String[]{
//		"file10.xml - all(0/2,3 1/1)",
//		"binary.xml - sap(0/3,7) "};
}
