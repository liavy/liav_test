package com.sap.dictionary.database.dbs;

import java.sql.Types;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;
import com.sap.tc.logging.*;

/**
 * Title: Analyse Tables and Views for structure changes Description: Contains
 * Extractor-classes which gain table- and view-descriptions from database and
 * XML-sources. Analyser-classes allow to examine this objects for
 * structure-changes, code can be generated and executed on the database.
 * Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbColumn implements DbsConstants, Cloneable {
	private DbFactory factory = null;
	private String name = null;
	private boolean isCaseSensitive = false;
	private int position = 1;
	private String srcType = null;
	private int javaSqlType = 0;
	private String javaSqlTypeName = null;
	private String dbType = null;
	private long length = 0;
	private int decimals = 0;
	private boolean isNotNull = false;
	private String defaultValue = null;
	private Object defaultObject = null;
	private DbColumn next = null;
	private DbColumn previous = null;
	private JavaSqlTypeInfo javaSqlTypeInfo = null;
	private DbColumns columns = null;
	private ArrayList addInfos = null;
	private static String BIN_CHS[] = new String[256];
	private static final int OTHER_GROUP = 0;
	private static final int CHAR_GROUP = 1;
	private static final int NUMERIC_GROUP = 2;
	private static final int BINARY_GROUP = 3;
	private static final int DATE_TIME_GROUP = 4;
	private static final Location loc = Location.getLocation(DbColumn.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	static {
		// For BINARY Default Check
		BIN_CHS[48] = "0";
		BIN_CHS[65] = "A";
		BIN_CHS[49] = "1";
		BIN_CHS[66] = "B";
		BIN_CHS[50] = "2";
		BIN_CHS[67] = "C";
		BIN_CHS[51] = "3";
		BIN_CHS[68] = "D";
		BIN_CHS[52] = "4";
		BIN_CHS[69] = "E";
		BIN_CHS[53] = "5";
		BIN_CHS[70] = "F";
		BIN_CHS[54] = "6";
		BIN_CHS[55] = "7";
		BIN_CHS[56] = "8";
		BIN_CHS[57] = "9";
	}

	// Empty constructor
	public DbColumn() {
	}

	// Constructor with factory only
	public DbColumn(DbFactory factory) {
		this.factory = factory;
	}

	// Empty constructor
	public DbColumn(DbFactory factory, DbColumn other) {
		this.factory = factory;
	}

	// Constructor including src-Type that means java-Type
	public DbColumn(DbFactory factory, XmlMap xmlMap) {
		this.factory = factory;
		name = xmlMap.getString("name");
		try {
			name = XmlHelper.checkAndGetName(name, "Column");
		} catch (JddException ex) {
			ex.printStackTrace();
		}
		position = xmlMap.getInt("position");
		srcType = xmlMap.getString("src-type");
		javaSqlTypeInfo = factory.getJavaSqlTypes().getInfo(
		    xmlMap.getString("java-sql-type"));
		javaSqlType = javaSqlTypeInfo.getIntCode();
		javaSqlTypeName = javaSqlTypeInfo.getName();
		dbType = xmlMap.getString("db-type");
		length = xmlMap.getInt("length");
		decimals = xmlMap.getInt("decimals");
		isNotNull = xmlMap.getBoolean("is-not-null");
		// defaultValue = xmlMap.getAuthenticString("default-value");
		defaultValue = removeTrailingSpaces(xmlMap
		    .getAuthenticString("default-value"));
		if (defaultValue != null && !javaSqlTypeInfo.hasDefaultValue())
			throw new JddRuntimeException(DEV_VALUE_FORBID, new Object[] { name,
			    javaSqlTypeName }, cat, Severity.ERROR, loc);
		setDefaultObject();
	}

	// Constructor excluding src-Type that means java-Type. Recommended if origin
	// is
	// database which does not know how a database-table's column is used in java
	public DbColumn(DbFactory factory, String name, int position,
	    int javaSqlType, String dbType, long length, int decimals,
	    boolean isNotNull, String defaultValue) {
		this.factory = factory;
		constructorPart(factory, name, position, javaSqlType, dbType, length,
		    decimals, isNotNull, defaultValue);
		setDefaultObject();
	}

	// Method excluding src-Type that means java-Type. Recommended if origin is
	// database which does not know how a database-table's column is used in java

	// Method is called in database-dependent constructor
	public void constructorPart(DbFactory factory, String name, int position,
	    int javaSqlType, String dbType, long length, int decimals,
	    boolean isNotNull, String defaultValue) {
		this.factory = factory;
		this.name = name;
		this.position = position;
		this.javaSqlType = javaSqlType;
		javaSqlTypeInfo = factory.getJavaSqlTypes().getInfo(javaSqlType);
		this.javaSqlTypeName = javaSqlTypeInfo.getName();
		this.dbType = dbType;
		this.length = (javaSqlTypeInfo.hasByteFactor() ? length
		    / javaSqlTypeInfo.getByteFactor() : length);
		this.decimals = decimals;
		this.isNotNull = isNotNull;
		this.defaultValue = defaultValue;
		if (defaultValue != null) {
			String prefix = javaSqlTypeInfo.getDefaultValuePrefix();
			String suffix = javaSqlTypeInfo.getDefaultValueSuffix();
			if (prefix != null && suffix != null
			    && !prefix.trim().equalsIgnoreCase("")
			    && !suffix.trim().equalsIgnoreCase("")) {
				// Database delivers defaults with prefix and suffix.
				// so delete trailing and leading characters and afterwards
				// prefix and suffix
				String defaultValueTrim = defaultValue.trim();
				if (defaultValueTrim.startsWith(prefix)
				    && defaultValueTrim.endsWith(suffix)) {
					if (defaultValueTrim.length() > 1) {
						this.defaultValue = defaultValueTrim.substring(prefix.length(),
						    defaultValueTrim.length() - suffix.length());
					}
				}
			} else {
				if (javaSqlTypeInfo.trimDefaultValue())
					this.defaultValue = defaultValue.trim();
				else
					this.defaultValue = defaultValue;
			}
		}
	}

	public String getDdlTypeClause() throws Exception {
		String clause = "";
		long byteLength = length;

		clause = javaSqlTypeInfo.getDdlName();
		if (javaSqlTypeInfo.hasLengthAttribute() && length > 0) {
			if (javaSqlTypeInfo.hasByteFactor()) {
				byteLength = length * javaSqlTypeInfo.getByteFactor();
			}
			clause = clause + "(" + byteLength;
			if (javaSqlTypeInfo.hasDecimals())
				clause = clause + "," + decimals;
			clause = clause + ")";
		} else {
			if (javaSqlTypeInfo.getDdlDefaultLength() > 0) {
				clause = clause + "(" + javaSqlTypeInfo.getDdlDefaultLength();
				clause = clause + ")";
			}
		}
		clause = clause + javaSqlTypeInfo.getDdlSuffix();
		return clause;
	}

	public String getDdlDefaultValueClause() throws Exception {
		String clause = "";

		if (javaSqlTypeInfo.hasDefaultValue() && defaultValue != null) {
			clause = "DEFAULT " + javaSqlTypeInfo.getDefaultValuePrefix()
			    + defaultValue + javaSqlTypeInfo.getDefaultValueSuffix();
		}
		return clause;
	}

	public String getDdlClause() throws Exception {
		String clause = " ";

		clause = "\"" + name + "\"" + " " + getDdlTypeClause() + " "
		    + getDdlDefaultValueClause() + " ";
		if (isNotNull)
			clause = clause + "NOT NULL";
		return clause;
	}

	/**
	 * Compares this column to a target column
	 * 
	 * @param target
	 *          the column's target version
	 * @return The differences about datatypes, length, decimals, notNull,
	 *         defaultValue,
	 * */
	protected DbColumnDifference compareTo(DbColumn target) throws Exception {
		DbTable targetTable = target.columns.getTable();
		if (targetTable.ignoreConfig())
			return comparePartTo(target);
		DbColumnDifference diff = comparePartTo(target);
		DbColumn rtorig = null;
		if (!targetTable.ignoreRuntimeAtCompare()) {
			if (factory.getConnection() != null)
				rtorig = columns.getRuntimeColumn(name);
			else
				rtorig = this; // in order to compare without a real connection
		}
		int originType = java.sql.Types.OTHER; // to be defined
		long originLength = -1; // to be defined
		int originDecimals = -1; // to be defined
		int targetType = adjustType(target.javaSqlType);
		long targetLength = target.length;
		int targetDecimals = target.decimals;
		if (rtorig == null) {
			if (targetTable.ignoreRuntimeAtCompare()
			    || targetTable.acceptRuntimeAbsence()) {
				if (diff == null || diff.getAction() == Action.NOTHING)
					return diff;
				originType = adjustType(this.javaSqlType);
				originLength = this.length;
				originDecimals = this.decimals;
			} else {
				if (diff == null) {
					diff = new DbColumnDifference(this, target,
					    new DbColumnDifferencePlan(), Action.REFUSE);
				} else
					diff.setAction(Action.REFUSE);
				cat
				    .error(loc, REFUSE_DUE_TO_COLUMN_RUNTIME_LOSS,
				        new Object[] { name });
				return diff;
			}
		} else {
			originType = adjustType(rtorig.javaSqlType);
			originLength = rtorig.length;
			originDecimals = rtorig.decimals;
		}
		int originGroup = getTypeGroup(originType);
		int targetGroup = getTypeGroup(targetType);
		if (originGroup != targetGroup) {
			diff.setAction(Action.REFUSE);
			return diff;
		}
		if (diff == null || diff.getAction() == Action.NOTHING) {
			if (originType == targetType)
				return diff;
			else {
				diff = new DbColumnDifference(this, target,
				    new DbColumnDifferencePlan(), Action.CONVERT);
				diff.getDifferencePlan().setTypeIsChanged(true);
			}
		}
		switch (originGroup) {
		case (CHAR_GROUP):
			if (originType == targetType
			    && originType != java.sql.Types.CLOB
			    && originLength > targetLength
			    || originType != targetType
			    && (originType == java.sql.Types.CLOB || targetType == java.sql.Types.VARCHAR))
				diff.getDifferencePlan().setDataLoss(true);
			break;
		case (BINARY_GROUP):
			if (originType == targetType
			    && originType != java.sql.Types.BLOB
			    && originLength > targetLength
			    || originType != targetType
			    && (originType == java.sql.Types.BLOB || targetType == java.sql.Types.BINARY))
				diff.getDifferencePlan().setDataLoss(true);
			break;
		case (DATE_TIME_GROUP):
			if (originType != targetType)
				diff.setAction(Action.REFUSE);
			break;
		case (NUMERIC_GROUP):
			long originIntegers = originLength - originDecimals;
			long targetIntegers = targetLength - targetDecimals;
			switch (originType) {
			case (java.sql.Types.SMALLINT):
				if (targetType == java.sql.Types.DECIMAL && targetIntegers < 5)
					diff.getDifferencePlan().setDataLoss(true);
				break;
			case (java.sql.Types.INTEGER):
				if (targetType == java.sql.Types.DECIMAL && targetIntegers < 10
				    || targetType == java.sql.Types.SMALLINT)
					diff.getDifferencePlan().setDataLoss(true);
				break;
			case (java.sql.Types.REAL):
				if (targetType != java.sql.Types.REAL
				    && targetType != java.sql.Types.DOUBLE)
					diff.getDifferencePlan().setDataLoss(true);
				break;
			case (java.sql.Types.DOUBLE):
				if (targetType != java.sql.Types.DOUBLE)
					diff.getDifferencePlan().setDataLoss(true);
				break;
			case (java.sql.Types.DECIMAL):
				if (targetType == java.sql.Types.DECIMAL
				    && (originIntegers > targetIntegers || originDecimals > targetDecimals)
				    || (targetType == java.sql.Types.SMALLINT && (originIntegers > 4 || originDecimals > 0))
				    || (targetType == java.sql.Types.INTEGER && (originIntegers > 9 || originDecimals > 0))
				    || (targetType == java.sql.Types.BIGINT && (originIntegers > 18 || originDecimals > 0)))
					diff.getDifferencePlan().setDataLoss(true);
				break;
			}
		}
		targetTable.adjust(diff);
		return diff;
	}

	private int adjustType(int initialType) {
		switch (initialType) {
		case (java.sql.Types.CHAR):
			return java.sql.Types.VARCHAR;
		case (java.sql.Types.LONGVARCHAR):
			return java.sql.Types.VARCHAR;
		case (java.sql.Types.NUMERIC):
			return java.sql.Types.DECIMAL;
		case (java.sql.Types.FLOAT):
			return java.sql.Types.REAL;
		case (java.sql.Types.VARBINARY):
			return java.sql.Types.BINARY;
		}
		return initialType;
	}

	private int getTypeGroup(int type) {
		switch (type) {
		case (java.sql.Types.CHAR):
		case (java.sql.Types.VARCHAR):
		case (java.sql.Types.LONGVARCHAR):
		case (java.sql.Types.CLOB):
			return CHAR_GROUP;
		case (java.sql.Types.BINARY):
		case (java.sql.Types.VARBINARY):
		case (java.sql.Types.LONGVARBINARY):
		case (java.sql.Types.BLOB):
			return BINARY_GROUP;
		case (java.sql.Types.NUMERIC):
		case (java.sql.Types.DECIMAL):
		case (java.sql.Types.REAL):
		case (java.sql.Types.DOUBLE):
		case (java.sql.Types.SMALLINT):
		case (java.sql.Types.INTEGER):
		case (java.sql.Types.BIGINT):
			return NUMERIC_GROUP;
		case (java.sql.Types.DATE):
		case (java.sql.Types.TIME):
		case (java.sql.Types.TIMESTAMP):
			return DATE_TIME_GROUP;
		}
		return OTHER_GROUP;
	}

	/**
	 * Compares this column to a target column
	 * 
	 * @param target
	 *          the column's target version
	 * @return The differences about datatypes, length, decimals, notNull,
	 *         defaultValue,
	 * */
	protected abstract DbColumnDifference comparePartTo(DbColumn target)
	    throws Exception;

	/**
	 * Analyses the behaviour in case a column has to be added
	 * 
	 * @return true if a column can be added via a ddl-statement
	 * */
	protected boolean acceptedAdd() {
		boolean accept = true;
		if (isNotNull() == true && getDefaultValue() == null) {
			cat.info(loc, ADD_COLUMN_NOT_ACCEPTED, new Object[] { name });
			accept = false;
		}
		return accept;
	}

	/**
	 * Analyses the behaviour in case a column has to be dropped
	 * 
	 * @return true if a column can be dropped via a ddl-statement
	 * */
	protected abstract boolean acceptedDrop();

	/**
	 * Database dependent checks
	 * 
	 * @return true - if name-length and type attributes are o.k
	 * */
	public boolean check() {
		return checkNameLength() & checkTypeAttributes();
	}

	boolean checkDbIndependent() {
		return checkJdbcType() & checkDbDefault() & checkDecimalAttributes();
	}

	/**
	 * Check the column's name according to its length
	 * 
	 * @return true - if name-length is o.k
	 * */
	public boolean checkNameLength() {
		return true;
	}

	/**
	 * Check the columns's attributes: type, length and decimals, e.g if length is
	 * to big for current type
	 * 
	 * @return true - if name-length is o.k
	 * */
	public boolean checkTypeAttributes() {
		return true;
	}

	/**
	 * Checks if column-name is a reserved word
	 * 
	 * @return true - if column-name has no conflict with reserved words, false
	 *         otherwise
	 * */
	public boolean checkNameForReservedWord() {
		return true;
	}

	/**
	 * Checks the column's name Name contains only characters A..Z 0..9 _ First
	 * Character is of set A..Z Name contains one _ Name length is checked from
	 * every database by method checkNameLength()
	 * */
	boolean checkName() {
		return DbTools.checkName(name, true, true, true, false);
	}

	/**
	 * Checks decimal attributes are correct
	 * 
	 * @return true - if total and fraction digits are correct
	 * */
	boolean checkDecimalAttributes() {
		boolean decimalCheck = true;
		if ("DECIMAL".equalsIgnoreCase(javaSqlTypeName)) {
			if (length == 0) {
				cat.error(loc, DECIMAL_LENGTH_MISSING, new Object[] { name });
				decimalCheck = false;
			} else {
				if (decimals > length)
					cat.error(loc, DECIMAL_FRACTION_TOO_BIG, new Object[] { name });
				decimalCheck = false;
			}
		}
		return decimalCheck;
	}

	/**
	 * Checks if JDBC type is correct, meaning out of the supported set
	 * 
	 * @return true - if type is correct, false otherwise
	 * */
	boolean checkJdbcType() {
		boolean typeCheck = true;
		if (!("DATE".equalsIgnoreCase(javaSqlTypeName)
		    || "TIME".equalsIgnoreCase(javaSqlTypeName)
		    || "TIMESTAMP".equalsIgnoreCase(javaSqlTypeName)
		    || "LONGVARBINARY".equalsIgnoreCase(javaSqlTypeName)
		    || "BLOB".equalsIgnoreCase(javaSqlTypeName)
		    || "BINARY".equalsIgnoreCase(javaSqlTypeName)
		    || "VARCHAR".equalsIgnoreCase(javaSqlTypeName)
		    || "CLOB".equalsIgnoreCase(javaSqlTypeName)
		    || "SMALLINT".equalsIgnoreCase(javaSqlTypeName)
		    || "INTEGER".equalsIgnoreCase(javaSqlTypeName)
		    || "BIGINT".equalsIgnoreCase(javaSqlTypeName)
		    || "REAL".equalsIgnoreCase(javaSqlTypeName)
		    || "DOUBLE".equalsIgnoreCase(javaSqlTypeName) || "DECIMAL"
		    .equalsIgnoreCase(javaSqlTypeName))) {
			cat.error(loc, WRONG_TYPE, new Object[] { name, javaSqlTypeName });
			typeCheck = false;
		}
		return typeCheck;
	}

	/**
	 * Checks if not null is allowed/possible for column type
	 * 
	 * @return true - if not null attribute is correctly set
	 * */
	boolean checkDbDefault() {
		boolean dbDefaultCheck = true;

		if (defaultValue == null || defaultValue.length() == 0)
			return dbDefaultCheck;

		if ("DATE".equalsIgnoreCase(javaSqlTypeName)
		    || "TIME".equalsIgnoreCase(javaSqlTypeName)
		    || "TIMESTAMP".equalsIgnoreCase(javaSqlTypeName)
		    || "LONGVARBINARY".equalsIgnoreCase(javaSqlTypeName)
		    || "CLOB".equalsIgnoreCase(javaSqlTypeName)
		    || "BLOB".equalsIgnoreCase(javaSqlTypeName)) {
			cat.error(loc, DB_DEFAULT_NOT_POSSIBLE_FOR_TYPE, new Object[] { name,
			    javaSqlTypeName });
			dbDefaultCheck = false;
		} else {
			if ("VARCHAR".equalsIgnoreCase(javaSqlTypeName)) {
				if (defaultValue.length() > length) {
					cat.error(loc, DB_DEFAULT_IS_TOO_LONG, new Object[] { name,
					    new Long(length) });
					dbDefaultCheck = false;
				}
			} else if ("BINARY".equalsIgnoreCase(javaSqlTypeName)) {
				checkBinaryDefaultValue();
			} else {// Numeric value expected
				dbDefaultCheck = checkNumericValue();
			}
		}
		return dbDefaultCheck;
	}

	boolean checkBinaryDefaultValue() {
		boolean binaryDefaultValueCheck = true;
		if (defaultValue.length() != 2 * length) {
			cat.error(loc, DB_DEFAULT_BINARY_WRONG_LENGTH, new Object[] { name,
			    new Long(length) });
			binaryDefaultValueCheck = false;
		}
		char ch;
		String s;
		for (int i = 0; i < defaultValue.length(); i++) {
			ch = defaultValue.charAt(i);
			s = BIN_CHS[ch];
			if (s == null) { // non allowed character found
				binaryDefaultValueCheck = false;
				cat.error(loc, DB_DEFAULT_BINARY_WRONG_CHARS, new Object[] { name });
				break;
			}
		}
		return binaryDefaultValueCheck;
	}

	boolean checkNumericValue() {
		boolean numericDefaultValueCheck = true;
		char ch;
		for (int i = 0; i < defaultValue.length(); i++) {
			ch = defaultValue.charAt(i);
			if (!Character.isDigit(ch)) {
				cat.error(loc, DB_DEFAULT_WRONG_TYPE, new Object[] { name,
				    javaSqlTypeName, defaultValue });
				numericDefaultValueCheck = false;
				return numericDefaultValueCheck; // No further checks
			}
		}
		try {
			if ("SMALLINT".equalsIgnoreCase(javaSqlTypeName)) {
				new Short(defaultValue);
			}
			if ("INTEGER".equalsIgnoreCase(javaSqlTypeName)) {
				new Integer(defaultValue);
			}
			if ("BIGINT".equalsIgnoreCase(javaSqlTypeName)) {
				new Long(defaultValue);
			}
			if ("DECIMAL".equalsIgnoreCase(javaSqlTypeName)) {
				new BigDecimal(defaultValue);
			}
			if ("REAL".equalsIgnoreCase(javaSqlTypeName)
			    || "DOUBLE".equalsIgnoreCase(javaSqlTypeName)) {
				new Double(defaultValue);
			}
		} catch (NumberFormatException ex) {
			cat.error(loc, DB_DEFAULT_WRONG_TYPE, new Object[] { name,
			    javaSqlTypeName, defaultValue });
			numericDefaultValueCheck = false;
		}
		return numericDefaultValueCheck;
	}

	public String getName() {
		return name;
	}

	public void setPosition(int pos) {
		this.position = pos;
	}

	public int getPosition() {
		return position;
	}

	public String getSrcType() {
		return srcType;
	}

	public int getJavaSqlType() {
		return javaSqlType;
	}

	/**
	 * If the DbColumn object represents the database state (dbType != null) and
	 * the dbType (respecting length and decimals) can be mapped to 2 or more
	 * different jdbc types the method returns all this alternatives. otherwise
	 * returns null. The porting should overwrite this method only if necessary:
	 * 
	 * @return the array of jdbc types or null
	 */
	public int[] getJavaSqlTypeAlternatives() { // overload at porting if
																							// necessary
		return null;
	}

	public String getJavaSqlTypeName() {
		return javaSqlTypeName;
	}

	public String getDbType() {
		return dbType;
	}

	public long getLength() {
		return length;
	}

	public int getDecimals() {
		return decimals;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

	public void setIsNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Object getDefaultObject() {
		return defaultObject;
	}

	public void setDefaultObject() {
		if (defaultValue == null)
			return;
		switch (javaSqlType) {
		case Types.VARCHAR:
			defaultObject = defaultValue;
			break;
		case Types.BINARY:
			defaultObject = convertToByteArray(defaultValue, length);
			break;
		case Types.SMALLINT:
			defaultObject = new Short(defaultValue);
			break;
		case Types.INTEGER:
			defaultObject = new Integer(defaultValue);
			break;
		case Types.BIGINT:
			defaultObject = new Long(defaultValue);
			break;
		case Types.DECIMAL:
			defaultObject = new BigDecimal(defaultValue);
			break;
		case Types.REAL:
			defaultObject = new Float(defaultValue);
			break;
		case Types.DOUBLE:
			defaultObject = new Double(defaultValue);
			break;
		}
	}

	public static byte[] convertToByteArray(String line, long len) {
		int ilen = new Long(len).intValue();
		byte[] res = null;
		if (len == 0)
			res = new byte[line.length() / 2];
		else
			res = new byte[ilen];
		Integer tmpI = null;
		int tmpi = 0;
		for (int i = 0; i <= line.length(); i = i + 2) {
			if (i == 0)
				continue;
			tmpI = Integer.decode("#" + line.substring(i - 2, i));
			tmpi = tmpI.intValue();
			res[i / 2 - 1] = tmpi > Byte.MAX_VALUE ? new Integer(tmpi - 256)
			    .byteValue() : tmpI.byteValue();
		}
		return res;
	}

	public JavaSqlTypeInfo getJavaSqlTypeInfo() {
		return javaSqlTypeInfo;
	}

	public DbColumn getNext() {
		return next;
	}

	public DbColumn getPrevious() {
		return previous;
	}

	public void setColumns(DbColumns dbColumns) {
		this.columns = dbColumns;
	}

	public DbColumns getColumns() {
		return columns;
	}

	protected void setNext(DbColumn column) {
		next = column;
	}

	protected void setPrevious(DbColumn column) {
		previous = column;
	}

	public String toString() {
		return "Column = " + name + "\n" + "Position      : " + position + "\n"
		    + "Source Type   : " + srcType + "\n" + "Java SqlType  : "
		    + javaSqlTypeName + "\n" + "DB Type       : " + dbType + "\n"
		    + "Length        : " + length + "\n" + "Decimals      : " + decimals
		    + "\n" + "is Not Null   : " + isNotNull + "\n" + "Default Value : "
		    + defaultValue + "\n";
	}

	public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
	    throws Exception {

		// begin column-element
		file.println(offset0 + "<column name=" + "\"" + name + "\"" + ">");

		String offset = offset0 + XmlHelper.tabulate();
		file.println(offset + "<position>" + position + "</position>");
		file.println(offset + "<java-sql-type>" + javaSqlTypeName
		    + "</java-sql-type>");
		// kh 20040406: No longer necessary: OpenSqlType suffices, special Db-types
		// are no longer necessary
		/*
		 * if ( s != null && !addInfos.isEmpty()) { file.println(offset +
		 * "<types-info-additional>"); int i = 0; String offset1 = offset +
		 * XmlHelper.tabulate(); while (i<addInfos.size()){ file.println(offset1 +
		 * "<type-info-additional>"); ((DbColumnAdditionalTypeInfo)
		 * addInfos.get(i)). writeCommonContentToXmlFile(file,offset);
		 * file.println(offset1 + "</type-info-additional>"); i = i + 1; }
		 * file.println(offset + "</types-info-additional>"); }
		 */
		file.println(offset + "<db-type>" + dbType + "</db-type>");
		file.println(offset + "<length>" + length + "</length>");
		file.println(offset + "<decimals>" + decimals + "</decimals>");
		file.println(offset + "<is-not-null>" + isNotNull + "</is-not-null>");
		if (defaultValue != null)
			file.println(offset + "<default-value>" + defaultValue
			    + "</default-value>");

		// end column-element
		file.println(offset0 + "</column>");
	}

	/**
	 * Removes the trailing spaces of a string. In case string only contains
	 * spaces, one space is returned
	 * 
	 * @param a
	 *          string
	 * @return the string without trailing spaces, can be null if input is null
	 * */
	public static String removeTrailingSpaces(String s) {
		if (s == null)
			return null;

		int count = s.length();
		int len = s.length();
		int st = 0;
		char[] val = new char[len]; /* avoid getfield opcode */

		if (len == 0)
			return null;
		for (int i = count; i > 0; i--) {
			if (s.charAt(i - 1) <= ' ')
				len--;
			else
				break;
		}
		if (len > 0)
			return s.substring(0, len);
		else
			return " ";
	}

	/**
	 * Set the column's name
	 * 
	 * @param the
	 *          name
	 * */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the column's type
	 * 
	 * @param the
	 *          type
	 * */
	public void setType(String javaSqlTypeName) {
		this.javaSqlTypeName = javaSqlTypeName;
	}

	/**
	 * Set the column's length
	 * 
	 * @param the
	 *          length
	 * */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Set the column's decimals
	 * 
	 * @param the
	 *          decimals
	 * */
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}

	/**
	 * Checks the table's name 1. Name contains only characters A..Z 0..9 _ 2.
	 * First Character is of set A..Z 3. Name <=18
	 * 
	 * @param name
	 *          columnname to check
	 * @return true - if name is correctly maintained, false otherwise
	 **/
	public static boolean checkName(String name) {
		return DbTools.checkName(name, true, true, false, true);
	}

	public DbColumn cloneColumn() {
		try {
			return (DbColumn) super.clone();
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen because Stack is Cloneable
			throw new InternalError();
		}
	}
}
