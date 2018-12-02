package com.sap.dictionary.database.dbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbDeployConfig implements DbsConstants {
	private boolean immutable = true;
	private static final String TABNAME = "BC_EXTRA_RULES";
	private static final String SELECT = "SELECT * FROM \"BC_EXTRA_RULES\"";
	private static final String REMOVE = "DELETE FROM \"BC_EXTRA_RULES\"";
	private static final Location loc = Location
	    .getLocation(DbDeployConfig.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	public final Attribute ignoreConfig = new Attribute("ignoreConfig");
	public final Attribute ignoreCheckTableWidth = new Attribute("ignoreCheckTableWidth");
	public final Attribute specificForce = new Attribute("specificForce");
	public final Attribute conversionForce = new Attribute("conversionForce");
	public final Attribute dropCreateForce = new Attribute("dropCreateForce");
	public final Attribute acceptDataLoss = new Attribute("acceptDataLoss");
	public final Attribute acceptAbortRisk = new Attribute("acceptAbortRisk",
			new Attribute[] { acceptDataLoss });
	public final Attribute acceptLongTime = new Attribute("acceptLongTime");
	public final Attribute fakeMissingDefaultValues = new Attribute("fakeMissingDefaultValues");
	public final Attribute acceptDropColumn = new Attribute("acceptDropColumn");
	public final Attribute acceptConversion = new Attribute("acceptConversion");
	public final Attribute ignoreRuntimeAtCompare = new Attribute("ignoreRuntimeAtCompare");
	public final Attribute acceptRuntimeAbsence = new Attribute("acceptRuntimeAbsence");

	private final Attribute[] ATTRIBUTES = new Attribute[] {
	    ignoreConfig,ignoreCheckTableWidth, specificForce, conversionForce,
	    dropCreateForce,acceptAbortRisk, acceptDataLoss, acceptLongTime,
	    fakeMissingDefaultValues, acceptDropColumn, acceptConversion,
	    ignoreRuntimeAtCompare, acceptRuntimeAbsence };

	public static final DbDeployConfig LIGHT= new DbDeployConfig(false);
	public static final DbDeployConfig STRONG = new DbDeployConfig(false);
	public static final DbDeployConfig STANDARD = STRONG;
	public static final String FILE_PACKAGE = "config/";
	static {
		LIGHT.immutable = false;
		STRONG.immutable = false;
		LIGHT.addFromFile("light");
		LIGHT.addFromFile("addon");
		STRONG.addFromFile("strong");
		STRONG.addFromFile("addon");
		LIGHT.immutable = true;
		STRONG.immutable = true;
	}

	public DbDeployConfig() {
	}

	public DbDeployConfig(boolean immutable) {
		this.immutable = immutable;
	}

	public static DbDeployConfig getInstance() {
		return STANDARD;
	}
	
	public static DbDeployConfig getInstance(boolean light) {
		if (light)
			return LIGHT;
		else
			return STRONG;
	}

	public static DbDeployConfig getMutableInstance(DbDeployConfig origin) {
		DbDeployConfig target = new DbDeployConfig(false);
		for (int i = 0; i < target.ATTRIBUTES.length; i++)
			target.ATTRIBUTES[i].setFrom(origin.ATTRIBUTES[i]);
		return target;
	}

	public static DbDeployConfig getInstance(String[][] initSet) {
		return getInstance(initSet,false);
	}
	
	public static DbDeployConfig getInstance(String[][] initSet,boolean light) {
		if (initSet == null || initSet.length == 0 || initSet[0].length != 3)
			return light?LIGHT:STRONG;
		DbDeployConfig instance = getMutableInstance(light?LIGHT:STRONG);		
		if (!instance.addFromArray(initSet))
			return light?LIGHT:STRONG;
		return instance;
	}
	
	public static DbDeployConfig getInstance(DbFactory factory) {
		return getInstance(factory,false);
	}

	public static DbDeployConfig getInstance(DbFactory factory,boolean light) {
		DbDeployConfig instance = getMutableInstance(light?LIGHT:STRONG);	
		if (!instance.addFromDb(factory))
			return light?LIGHT:STRONG;
		return instance;
	}
	
	public boolean addFromFile(String fname) {
		InputStream stream = DbDeployConfig.class.getResourceAsStream(FILE_PACKAGE
		    + fname + ".txt");
		if (stream == null)
			return false;
		ArrayList attributes = new ArrayList();
		ArrayList inclnames = new ArrayList();
		ArrayList exclnames = new ArrayList();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("*") || line.startsWith("//") || line.length() == 0)
					continue;
				char[] cs = line.toCharArray();
				StringBuffer sb = new StringBuffer();
				int itemnum = 1;
				for (int i = 0; i < cs.length; i++) {
					if (cs[i] == ' ')
						continue;
					else if (cs[i] == ',') {
						if (itemnum == 1)
							attributes.add(sb.toString());
						else if (itemnum == 2)
							inclnames.add(sb.toString());
						sb = new StringBuffer();
						itemnum++;
					} else {
						sb.append(cs[i]);
					}
				}
				String lastitem = sb.toString().trim();
				if (lastitem.length() == 0)
					continue;
				if (itemnum == 1)
					attributes.add(lastitem);
				else if (itemnum == 2)
					inclnames.add(lastitem);
				else
					exclnames.add(lastitem);
			}
			reader.close();
			stream.close();
		} catch (IOException e) {
			return false;
		}
		if (attributes == null || attributes.isEmpty())
			return false;
		for (int i = 0; i < ATTRIBUTES.length; i++) {
			ATTRIBUTES[i].addInclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    inclnames));
			ATTRIBUTES[i].addExclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    exclnames));
		}
		cat.info(loc, DEPLOY_CONFIGURATION, new Object[] { toString() });
		return true;
	}

	public boolean addFromArray(String[][] initSet) {
		ArrayList attributes = new ArrayList();
		ArrayList inclnames = new ArrayList();
		ArrayList exclnames = new ArrayList();
		String[] isi = null;
		for (int i = 0; i < initSet.length; i++) {
			isi = initSet[i];
			if (isi.length == 0)
				continue;
			attributes.add(isi[0]);
			inclnames.add(isi[1]);
			exclnames.add(isi[2]);
		}
		if (attributes == null || attributes.isEmpty())
			return false;
		for (int i = 0; i < ATTRIBUTES.length; i++) {
			ATTRIBUTES[i].addInclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    inclnames));
			ATTRIBUTES[i].addExclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    exclnames));
		}
		cat.info(loc, DEPLOY_CONFIGURATION, new Object[] { toString() });
		return true;
	}

	public boolean addFromDb(DbFactory factory) {
		Connection con = factory.getConnection();
		if (con == null)
			return false;
		try {
			if (!factory.getTools().tableExistsOnDb(TABNAME))
				return false;
		} catch (JddException e) {
			JddException.log(e, cat, Severity.INFO, loc);
			return false;
		}
		ArrayList attributes = new ArrayList();
		ArrayList inclnames = new ArrayList();
		ArrayList exclnames = new ArrayList();
		try {
			PreparedStatement statementObject = NativeSQLAccess
			    .prepareNativeStatement(con, SELECT);
			ResultSet result = statementObject.executeQuery();
			while (result.next()) {
				attributes.add(result.getString(1));
				inclnames.add(result.getString(2));
				exclnames.add(result.getString(3));
			}
			result.close();
			statementObject.close();
			statementObject = NativeSQLAccess.prepareNativeStatement(con, REMOVE);
			statementObject.executeUpdate();
			// con.commit();
			statementObject.close();
		} catch (SQLException ex) {
			JddException.log(ex, cat, Severity.ERROR, loc);
			return false;
		}
		if (attributes == null || attributes.isEmpty())
			return false;
		for (int i = 0; i < ATTRIBUTES.length; i++) {
			ATTRIBUTES[i].addInclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    inclnames));
			ATTRIBUTES[i].addExclusive(makeArray(ATTRIBUTES[i].name, attributes,
			    exclnames));
		}
		cat.info(loc, DEPLOY_CONFIGURATION, new Object[] { toString() });
		return true;
	}

	void addFromTableDeployInfo(DbTable table) {
		if (table == null)
			return;
		DbDeploymentInfo info = table.getDeploymentInfo();
		if (info == null || info.ignoreConfig())
			return;
		String name = table.getName();
		ignoreCheckTableWidth.addTable(name, info.ignoreCheckTableWidth());
		specificForce.addTable(name, info.specificForce());
		conversionForce.addTable(name, info.conversionForce());
		dropCreateForce.addTable(name, info.dropCreateForce());
		acceptAbortRisk.addTable(name, info.acceptAbortRisk());
		acceptDataLoss.addTable(name, info.acceptDataLoss());
		acceptLongTime.addTable(name, info.acceptLongTime());
		fakeMissingDefaultValues.addTable(name, info.fakeMissingDefaultValues());
		acceptDropColumn.addTable(name, info.acceptDropColumn());
		acceptConversion.addTable(name, info.acceptConversion());
		ignoreRuntimeAtCompare.addTable(name, info.ignoreRuntimeAtCompare());
		acceptRuntimeAbsence.addTable(name, info.acceptRuntimeAbsence());
	}

	public boolean isImmutable() {
		return this.immutable;
	}

	public void makeImmutable() {
		this.immutable = true;
	}

	public void adjust(DbColumnDifference coldiff) {
		if (coldiff == null)
			return;
		DbColumn origin = coldiff.getOrigin();
		DbColumn target = coldiff.getTarget();
		DbColumnDifferencePlan diffplan = coldiff.getDifferencePlan();
		Action action = coldiff.getAction();
		if (action == null || action == Action.NOTHING)
			// || diffplan != null && !diffplan.somethingIsChanged())
			return;
		String tabname = origin != null ? origin.getColumns().getTable().getName()
		    : target.getColumns().getTable().getName();
		String colname = origin != null ? origin.getName() : target.getName();
		if (target == null) {
			if (!acceptDropColumn.isOn(tabname)) {
				coldiff.setAction(Action.REFUSE);
				cat.error(loc, REFUSE_DUE_TO_DROP_COLUMN, new Object[] { colname });
			}
		}
		if (origin == null) {
			if (target.isNotNull() && target.getDefaultValue() == null
			    && !fakeMissingDefaultValues.isOn(tabname)) {
				coldiff.setAction(Action.REFUSE);
				cat.error(loc, REFUSE_ADD_NOTNULL_COLUMN_WITHOUT_DEF,
				    new Object[] { colname });
			}
		}
		if (diffplan == null)
			return;
		if (diffplan.dataLoss() && !acceptDataLoss.isOn(tabname)) {
			coldiff.setAction(Action.REFUSE);
			cat.error(loc, REFUSE_DUE_TO_DATA_LOSS, new Object[] { colname });
		}
		if (diffplan.dataLoss() && !acceptLongTime.isOn(tabname)) {
			coldiff.setAction(Action.REFUSE);
			cat.error(loc, REFUSE_DUE_TO_LONG_TIME, new Object[] { colname });
		}
		if (!origin.isNotNull() && target.isNotNull()
		    && target.getDefaultValue() == null
		    && !fakeMissingDefaultValues.isOn(tabname)) {
			coldiff.setAction(Action.REFUSE);
			cat.error(loc, REFUSE_MODIFY_TO_NOTNULL_WITHOUT_DEF,
			    new Object[] { colname });
		}
		return;
	}

	private String[] makeArray(String attrname, ArrayList attributes,
	    ArrayList names) {
		if (names == null || names.size() == 0)
			return null;
		ArrayList alist = new ArrayList();
		String itabname = null;
		String iattrname = null;
		for (int i = 0; i < attributes.size(); i++) {
			itabname = ((String) names.get(i)).trim();
			iattrname = ((String) attributes.get(i)).trim();
			if (itabname != null && itabname.length() != 0
			    && !Character.isDigit(itabname.charAt(0))
			    && iattrname.equalsIgnoreCase(attrname))
				alist.add(names.get(i));
		}
		if (alist.isEmpty())
			return null;
		String[] res = new String[alist.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = (String) alist.get(i);
		}
		return res;
	}

	private static String[] merge(String[] source, String[] append) {
		for (int i = 0; i < append.length; i++) {
			if (append[i].equals("*"))
				return new String[] { "*" };
		}
		if (source == null || source.length == 0)
			return append;
		String[] result = new String[source.length + append.length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < source.length; j++)
				result[i] = source[j];
			for (int j = 0; j < append.length; j++)
				result[i] = append[j];
		}
		return result;
	}

	private static boolean matchesTemplate(String name, String[] template) {
		if (template == null)
			return false;
		for (int i = 0; i < template.length; i++) {
			if (template[i].equals("*") || template[i].equals(name)
			    || template[i].indexOf(name) == 0)
				return true;
		}
		return false;
	}

	public String toString() {
		String res = null;
		String fullres = "";
		for (int i = 0; i < ATTRIBUTES.length; i++) {
			res = "";
			String[] inc = ATTRIBUTES[i].inclusive;
			String[] exc = ATTRIBUTES[i].exclusive;
			if (inc != null && inc.length > 0) {
				res += "-->incl [";
				for (int j = 0; j < ATTRIBUTES[i].inclusive.length; j++) {
					res += ATTRIBUTES[i].inclusive[j];
					if (j < ATTRIBUTES[i].inclusive.length - 1)
						res += ",";
				}
				res += "]\n";
			}
			if (exc != null && exc.length > 0) {
				res += "-->excl [";
				for (int j = 0; j < ATTRIBUTES[i].exclusive.length; j++) {
					res += ATTRIBUTES[i].exclusive[j];
					if (j < ATTRIBUTES[i].exclusive.length - 1)
						res += ",";
				}
				res += "]\n";
			}
			if (res.trim().length() > 0)
				fullres += ATTRIBUTES[i].name + "\n" + res;
		}
		if (fullres.trim().length() > 0)
			fullres = "\n" + fullres;
		return fullres;
	}

	public class Attribute {
		private String name;
		private String[] inclusive = null;
		private String[] exclusive = null;
		private Attribute[] coveredAttributes = null;

		private Attribute(String name) {
			this(name, null, null, null);
		}

		private Attribute(String name, String[] inclusive, String[] exclusive) {
			this(name, null, inclusive, exclusive);
		}

		private Attribute(String name, Attribute[] coveredAttributes) {
			this(name, coveredAttributes, null, null);
		}

		private Attribute(String name, Attribute[] coveredAttributes,
		    String[] inclusive, String[] exclusive) {
			this.name = name;
			this.coveredAttributes = coveredAttributes;
			this.inclusive = inclusive;
			this.exclusive = exclusive;

		}

		private void setFrom(Attribute other) {
			if (immutable)
				return;
			if (other == null)
				return;
			inclusive = other.inclusive;
			exclusive = other.exclusive;
		}

		private void setInclusive(String[] inclusive) {
			if (!immutable)
				this.inclusive = inclusive;
		}

		private void addInclusive(String[] inclusive) {
			if (inclusive == null || inclusive.length == 0)
				return;
			if (!immutable)
				this.inclusive = merge(this.inclusive, inclusive);
		}

		private void setExclusive(String[] exclusive) {
			if (!immutable)
				this.exclusive = exclusive;
		}

		private void addExclusive(String[] exclusive) {
			if (exclusive == null || exclusive.length == 0)
				return;
			if (!immutable)
				this.exclusive = merge(this.exclusive, exclusive);

		}

		private void addTable(String name, Boolean value) {
			if (immutable)
				return;
			if (name == null || value == null)
				return;
			boolean val = value.booleanValue();
			if (val == true)
				this.inclusive = merge(this.inclusive, new String[] { name });
			else
				this.exclusive = merge(this.exclusive, new String[] { name });
		}

		public boolean isOn(String name) {
			if (coveredAttributes != null) {
				for (int i = 0; i < coveredAttributes.length; i++) {
					if (coveredAttributes[i].isOn(name))
						return true;
				}
			}
			if (inclusive == null)
				return false;
			if (matchesTemplate(name, inclusive) && !matchesTemplate(name, exclusive))
				return true;
			return false;
		}

	}

}
