package com.sap.dictionary.database.veris;  

import java.io.*;
import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.catalog.*;
import java.sql.*;
import com.sap.sql.jdbc.internal.SAPDataSource;
import javax.sql.*;
import java.util.*;
import java.util.Date;

import com.sap.tc.logging.*;
import com.sap.sql.catalog.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Veri {
	public static final int LOG = 2; //no log = 0;console = 1;file = 2; both = 3
	private static int SAP = 1;
	private static int MSS = 1;
	private static int ORA = 1;
	private static int DB6 = 1;
	private static int DB4 = 1;
	private static int DB2 = 1;
	private static String[] fileNames = new String[]{
//		"binary.xml"
		};
	private static String SELECTION = "select.txt";
	private static int SEVERITY = Severity.ERROR;
	private static final String LOGDIR = "C:\\logs\\database dev\\";
	public static final SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat("ddMMyyyy_kkmmss");
	private static final Location loc = Location.getLocation(
		"com.sap.dictionary.database");
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	public static PrintStream out = null;

	public static void main(String[] args) {
		out = System.out;
		DataSource ds;
		Connection con;
		//cat.infoT(loc,"777777777 ERROR 77777777777");
		//Logger.setLoggingConfiguration("default");
		loc.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    cat.setResourceBundleName("com.sap.dictionary.database.dbs.messages.messages");         
    loc.setClassLoader(DbTable.class.getClassLoader());
    cat.setClassLoader(DbTable.class.getClassLoader());
    if (LOG == 2 || LOG == 3) {
			FileLog fileLog = new FileLog(LOGDIR + DATE_FORMAT.format(new Date())
					+ ".doc");
			fileLog.setFormatter(new DbTraceFormatter());
			loc.addLog(fileLog);
		}
		if (LOG == 1 || LOG == 3) {
			ConsoleLog consoleLog = new ConsoleLog();
			consoleLog.setFormatter(new DbTraceFormatter());
			loc.addLog(consoleLog);
		}
		loc.setEffectiveSeverity(Severity.FATAL);
		//cat.setEffectiveSeverity(SEVERITY);
		cat.setEffectiveSeverity(SEVERITY);
		try {
			//cat.errorT(loc,"777777777 ERROR 77777777778");
			ArrayList cnns = VeriTools.getTestConnections(SAP,MSS,ORA,DB6,DB4,DB2);
			//cat.errorT(loc,"777777777 ERROR 77777777779");                                                      
      if (exec(cnns, false))
        System.out.println("!!!!!!!!!!!!VERI  SUCCESS !!!!!!!!!!!!");
      else
        System.out.println("????????????VERI  ERRORS  ????????????");      
    }
    catch (Exception ex) {
      	ex.printStackTrace();
    }
	}
	
	public static boolean exec(ArrayList cnns) throws Exception {
		return exec(cnns,true);
	}
	

	public static boolean exec(ArrayList cnns, boolean breakIfError)
	    throws Exception {
		boolean isResultOk = true;
		if (fileNames == null || fileNames.length == 0)
			fileNames = getSelectionFileContent();
		Connection con = null;
		for (int i = 0; i < cnns.size(); i++) {
			con = (Connection) cnns.get(i);
			for (int j = 0; j < fileNames.length; j++) {
				LineInfo li = getLineInfo(con, fileNames[j]);
				if (li == null)
					continue;
				VeriCore veriCore = new VeriCore(li.getFileName(), out);
				if (!veriCore.exec(con, breakIfError, li.getIncl(), li.getExcl())) {
					if (breakIfError)
						return false;
					out.println("????????????VERI  ERRORS  ????????????");
					out.println("============================================");
					out.println(" ");
					isResultOk = false;
				} else {
					out.println("!!!!!!!!!!!!VERI  SUCCESS !!!!!!!!!!!!");
					out.println("============================================");
					out.println(" ");
				}
			}
		}
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
