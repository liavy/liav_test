package com.sap.dictionary.database.veris;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.xml.sax.InputSource;

import com.sap.dictionary.database.dbs.*;

public class MappingVeri extends TestCase{
	public static final DbFactory[] FACTORIES = new DbFactory[] {
	    new DbFactory(Database.SAPDB), new DbFactory(Database.MSSQL),
	    new DbFactory(Database.ORACLE), new DbFactory(Database.DB6),
	    new DbFactory(Database.DB4), new DbFactory(Database.DB2) };
	private static PrintStream out = null;
	private static PrintStream preout = null;
	private static Class thisClass = MappingVeri.class;
	
	public MappingVeri() {
		super();
	}
	
	public void setUp() throws Exception {
		
	}

	public static void testMapping() {
		XmlMap typemaps = null;
		int type;
		String typename;
		String dbs = null;
		String[] versions = null;
		String version = null;
		InputStream stream = MappingVeri.class
		    .getResourceAsStream("verifiles/mapping.xml");
		XmlExtractor extractor = new XmlExtractor();
		XmlMap maps = extractor.map(new InputSource(stream)).getXmlMap(
		    "mapping-check");
		for (int i = 0; !(typemaps = maps.getXmlMap("maps-for-jdbc-type"
		    + (i == 0 ? "" : "" + i))).isEmpty(); i++) {
			typename = typemaps.getString("jdbc-type");
			type = JavaSqlTypes.getIntCode(typename);
			// System.out.println(type);
			for (int j = 0; j < FACTORIES.length; j++) {
				dbs = FACTORIES[j].getDatabase().getAbbreviation();
				// System.out.println(dbs);
				versions = FACTORIES[j].getEnvironment().getSupportedDatabaseVersions();
				try {
	        if (versions == null) {
	        	check(typemaps, type,typename, FACTORIES[j], dbs, null);
	        } else {
	        	for (int j2 = 0; j2 < versions.length; j2++) {
	        		check(typemaps, type,typename, FACTORIES[j], dbs, versions[j2]);
	        	}
	        }
        } catch (Exception e) {
	        failure(e.getMessage());
        }

			}

		}
	}

	public static void check(XmlMap typemaps, int type,String typename,
			DbFactory factory,String dbs, String version) throws Exception {
		String versname = isEmpty(version)? "":"(" + version + ")"; 
		XmlMap dbmap;
		XmlMap fitdbmap = null;
		String nextdb;
		String nextver;
		for (int i = 0; !(dbmap = typemaps
		    .getXmlMap("map" + (i == 0 ? "" : "" + i))).isEmpty(); i++) {
			nextdb = dbmap.getString("dbs");
			if (!dbs.equalsIgnoreCase(nextdb))
				continue;
			if (version == null) {
				fitdbmap = dbmap;
				break;
			}
			nextver = dbmap.getString("version");
			if (nextver == null || nextver.trim().length() == 0
			    || nextver.trim().equals("*"))
				fitdbmap = dbmap;
			else if (nextver.equals(version)
			    || (nextver.length() <= version.length() && nextver.equals(version
			        .substring(0, nextver.length())))) {
				fitdbmap = dbmap;
				break;
			}
		}
		if (fitdbmap == null || fitdbmap.isEmpty())
			return;
		String templ = fitdbmap.getString("db-type");
		if (templ == null || templ.trim().length() == 0)
			return;
		if (version != null)
	    try {
	      factory.setDatabaseVersion(version);
      } catch (Exception e) {
	      return;
      }
		long maxlen = fitdbmap.getLong("max-length");
		DbTable table = factory.makeTable("TMP_TEST");
		DbColumns cols = factory.makeDbColumns();
		cols.setTable(table);
		DbColumn col;
		XmlMap colmap = new XmlMap();
		colmap.put("name", "F1");
		colmap.put("position","1");
		colmap.put("java-sql-type",typename);
		if (maxlen > 0) {
			colmap.put("length",new Long(maxlen + 1).toString());
			col = factory.makeDbColumn(colmap);
			if (col.check()) {
				failure(maxlen + " is not max length for " + typename + " " + dbs +
						versname);
				return;
			}
		}
		int pos = templ.indexOf('?');
		if (pos > 0 && maxlen == 0)
			colmap.put("length","10");
		else
			colmap.put("length",fitdbmap.getString("max-length"));
		col = factory.makeDbColumn(colmap);
		col.setColumns(cols);
		if (!col.check()) {
			failure("length " + maxlen + " is not acceptable for " + typename +
					" " + dbs + versname);
			return;
		}
		String ddl = col.getDdlTypeClause();
		String expectedDdl;
		if (pos > 0) {
			int factor = fitdbmap.getInt("length-factor");
			String lenstr = null;
			if (factor == 0) {
				if (maxlen > 0)
					lenstr = fitdbmap.getString("max-length");
				else
					lenstr = "10";
			} else {
				if (maxlen > 0)
					lenstr = new Integer(factor * fitdbmap.getInt("max-length")).toString();
				else
					lenstr = new Integer(factor * 10).toString();
			}
			StringBuffer sb = new StringBuffer(templ);
			sb.replace(pos, pos + 1,lenstr);
			expectedDdl = sb.toString();
		} else {
			expectedDdl = templ;
		}
		
		if (!ddl.equals(expectedDdl)) {
			failure(" type clause is " + ddl + " but expected " + expectedDdl +
					" for " + typename + " " + dbs + versname);
			return;
		} else {
//			out.println("type clause is " + ddl + " expected " + expectedDdl +
//					" for " + typename + " " + dbs + versname);
			return;
		}
	}
	
	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}
	
	public static void failure(String mess) {
		if (out != null) {
			out.println(mess);
		} else {
			Assert.fail(mess);
		}
	}
	public static void setOut(PrintStream stream) {
		preout = out;
		out = stream;
	}
	public static void setOutToConsole() throws Exception{
		preout = out;
		out = new PrintStream(System.out,true,"UTF-8");
	}
	public static void resetOut() {
		out = preout;
	}
	
	public static void exec() throws Exception{
		setOutToConsole();
  	TestSuite ts = new TestSuite(thisClass);
  	TestResult tr = new TestResult();
  	String name = ts.getName();
  	out.print("------" + thisClass.getSimpleName());
  	long t0 = System.nanoTime();
  	ts.run(tr);
  	long t1 = System.nanoTime();
  	out.print(" " + (t1 - t0)/1000000000. + "s");
  	if (tr.wasSuccessful())
  		out.println(" OK");
  	else {
  		if (tr.errors().hasMoreElements())
  			failure(tr.errors().nextElement().toString());
  		else
  			failure(tr.failures().nextElement().toString());
  	}
	}
	
	public static void main(String[] args) {
		try {
      exec();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
	}

}
