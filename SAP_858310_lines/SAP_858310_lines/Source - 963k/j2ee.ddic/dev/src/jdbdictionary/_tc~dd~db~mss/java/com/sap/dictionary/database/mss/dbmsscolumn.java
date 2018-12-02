package com.sap.dictionary.database.mss;

import java.io.PrintWriter;
import java.sql.Types;
import java.text.SimpleDateFormat;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JavaSqlTypeInfo;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MS SQL Server specific analysis of table and view changes. Tool to deliver MS SQL Server specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbMssColumn extends DbColumn {

	private static Location loc = Logger.getLocation("mss.DbMssColumn");
	// private static final Category cat = Logger.getCategory();
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	

	//Constructor including src-Type that means java-Type
	public DbMssColumn(DbFactory factory) {
		super(factory);
	}

	public DbMssColumn(DbFactory factory, DbColumn other) {
		super(factory, other);
	}

	public DbMssColumn(DbFactory factory, XmlMap xmlMap) {
		super(factory, xmlMap);
	}

	//Constructor excluding src-Type that means java-Type. Recommended if origin is
	//database which does not know how a database-table's column is used in java
	public DbMssColumn(
		DbFactory factory,
		String name,
		int position,
		int javaSqlType,
		String dbType,
		long length,
		int decimals,
		boolean isNotNull,
		String defaultValue) {
		// BLOB
		if (dbType.equalsIgnoreCase("IMAGE")) {
			javaSqlType = Types.BLOB;
			length = 0;
		}
		// CLOB
		else if (
			dbType.equalsIgnoreCase("TEXT")
				|| dbType.equalsIgnoreCase("NTEXT")) {
			javaSqlType = Types.CLOB;
			length = 0;
		}
		// VARCHAR
		else if (
			dbType.equalsIgnoreCase("CHAR")
				|| dbType.equalsIgnoreCase("NCHAR")
				|| dbType.equalsIgnoreCase("VARCHAR")
				|| dbType.equalsIgnoreCase("NVARCHAR")) {
			javaSqlType = Types.VARCHAR;
		}
		// FLOAT
		else if (dbType.equalsIgnoreCase("FLOAT")) {
			javaSqlType = Types.DOUBLE;
		} else if (dbType.equalsIgnoreCase("REAL")) {
			// javaSqlType = Types.FLOAT;
			javaSqlType = Types.REAL;
		}
		// SMALLINT
		else if (
			dbType.equalsIgnoreCase("SMALLINT")
				|| dbType.equalsIgnoreCase("TINYINT")) {
			javaSqlType = Types.SMALLINT;
		}
		// INTEGER
		else if (
			dbType.equalsIgnoreCase("INTEGER")
				|| dbType.equalsIgnoreCase("INT")) {
			javaSqlType = Types.INTEGER;
		}
		// BIGINT
		else if (dbType.equalsIgnoreCase("BIGINT")) {
			javaSqlType = Types.BIGINT;
		}
		// DECIMAL
		else if (
			dbType.equalsIgnoreCase("DECIMAL")
				|| dbType.equalsIgnoreCase("NUMERIC")) {
			javaSqlType = Types.DECIMAL;
		}
		// DATE
		else if (
			dbType.equalsIgnoreCase("DATETIME")
				|| dbType.equalsIgnoreCase("SMALLDATETIME")) {
			javaSqlType = Types.TIMESTAMP;
			// we use dbtype DATETIME for javasqltype date, time, timestamp
		}

		// bit, money, smallmoney, timestamp ???

		if (length > 0) {
			if (!factory
				.getJavaSqlTypes()
				.getInfo(javaSqlType)
				.hasLengthAttribute()) {
				length = 0;
			}
		}
		constructorPart(
			factory,
			name,
			position,
			javaSqlType,
			dbType,
			length,
			decimals,
			isNotNull,
			defaultValue);
	}

	public String getTypeClauseForDdl() throws Exception {
		loc.entering("getTypeClauseForDdl");
		try {
			String clause = "";

			//Take this as proposal, if not usable, replace it by your own implementation
			clause = super.getDdlTypeClause();

			loc.exiting();
			return clause;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getTypeClauseForDdl failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public String getDdlDefaultValueClause() throws Exception {
		loc.entering("getDdlDefaultValueClause");

		try {
			String clause = getDdlDefaultValueString();

			if (clause.compareTo("") != 0)
				clause = "DEFAULT " + clause;

			loc.exiting();
			return clause;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlDefaultValueClause failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	// gd030303 the MSSQL-specific default value 
	//          is used in DbMssColumn.getDdlDefaultValueClause 
	//          AND in DbMssColumnsDifference.getDdlStatementsForAlter
	//          returns "" if no default is defined
	protected String getDdlDefaultValueString() throws Exception {
		loc.entering("getDdlDefaultValueString");

		try {
			String clause = "";

			/* gd 15.10.01 need to 'normalize' default definitions */
			int javaSqlType = super.getJavaSqlType();
			JavaSqlTypeInfo javaSqlTypeInfo = super.getJavaSqlTypeInfo();

			String defVal = super.getDefaultValue();
			if (defVal != null) {
				boolean noPreSuffix = false;

				if (defVal != null) {
					if (javaSqlType == java.sql.Types.BINARY
						|| javaSqlType == java.sql.Types.VARBINARY) {
						defVal = defVal.toUpperCase();
					}
					if (javaSqlType == java.sql.Types.FLOAT) {
						try {
							Double v = Double.valueOf(defVal);
							defVal = v.toString();
						} catch (NumberFormatException ex) {
							cat.infoT(loc, "ignoring NumberFormatException");
						}
					} else if (
						javaSqlType == java.sql.Types.INTEGER
							|| javaSqlType == java.sql.Types.SMALLINT) {
						try {
							Integer v = Integer.valueOf(defVal);
							defVal = v.toString();
						} catch (NumberFormatException ex) {
							cat.infoT(loc, "ignoring NumberFormatException");
						}
					} else if (
						javaSqlType == java.sql.Types.DATE
							|| javaSqlType == java.sql.Types.TIME
							|| javaSqlType == java.sql.Types.TIMESTAMP) {
						if (defVal.equalsIgnoreCase("getdate()")) {
							// accept this one
							noPreSuffix = true;
						} else {
							String fmtStr = null;

							if (javaSqlType == java.sql.Types.DATE)
								fmtStr = "yyyy-MM-dd";
							else if (javaSqlType == java.sql.Types.TIME)
								fmtStr = "HH:mm:ss.SSS";
							else if (javaSqlType == java.sql.Types.TIMESTAMP)
								fmtStr = "yyyy-MM-dd HH:mm:ss.SSS";

							java.text.SimpleDateFormat dateFormatter =
								(SimpleDateFormat) javaSqlTypeInfo
									.getFormatterForDefaultString();
							java.util.Date datetime =
								dateFormatter.parse(defVal);
							java.text.SimpleDateFormat myFormatter =
								new java.text.SimpleDateFormat(fmtStr);
							defVal = myFormatter.format(datetime);
						}
					}

					if (noPreSuffix) {
						clause = defVal;
					} else {
						clause =
							javaSqlTypeInfo.getDefaultValuePrefix()
								+ defVal
								+ javaSqlTypeInfo.getDefaultValueSuffix();
					}
				}
			}

			loc.exiting();
			return clause;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlDefaultValueString failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
		throws Exception {
		loc.entering("writeCommonContentToXmlFile");

		try {
			//begin column-element
			file.println(
				offset0
					+ "<column name="
					+ "\""
					+ super.getName()
					+ "\""
					+ ">");

			String offset = offset0 + XmlHelper.tabulate();
			file.println(
				offset + "<position>" + super.getPosition() + "</position>");
			file.println(
				offset
					+ "<java-sql-type>"
					+ super.getJavaSqlTypeName()
					+ "</java-sql-type>");
			file.println(
				offset + "<db-type>" + super.getDbType() + "</db-type>");
			file.println(offset + "<length>" + super.getLength() + "</length>");
			file.println(
				offset + "<decimals>" + super.getDecimals() + "</decimals>");
			file.println(
				offset
					+ "<is-not-null>"
					+ super.isNotNull()
					+ "</is-not-null>");
			String defVal = super.getDefaultValue();
			if (defVal != null) {
				file.println(
					offset + "<default-value>" + defVal + "</default-value>");
			}

			//end column-element
			file.println(offset0 + "</column>");
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "writeCommonContentToXmlFile failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		loc.exiting();
		return;
	}

	public String getDdlClause() throws Exception {
		loc.entering("getDdlClause");

		try {
			String clause = "";

			//Take this as proposal, if not usable, replace it by your own implementation
			clause = super.getDdlClause();

			loc.exiting();
			return clause;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlClause failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	protected DbColumnDifference comparePartTo(DbColumn target) throws Exception {
		loc.entering("compareTo");

		boolean varbinaryBinarySituation = false;
                boolean nullToNotNullWithoutDefault = false;

		try {
			DbColumnDifference colDiff = null;
			DbMssColumn targetCol = null;
			DbColumnDifferencePlan plan = new DbColumnDifferencePlan();
			targetCol = (DbMssColumn) target;

			int originalType = getJavaSqlType();
			int targetType = targetCol.getJavaSqlType();

			// LONGVARCHAR synomym to VARCHAR
			if (originalType == java.sql.Types.LONGVARCHAR)
				originalType = java.sql.Types.VARCHAR;
			if (targetType == java.sql.Types.LONGVARCHAR)
				targetType = java.sql.Types.VARCHAR;

			// LONGVARBINARY synonym to VARBINARY
			if (originalType == java.sql.Types.LONGVARBINARY)
				originalType = java.sql.Types.VARBINARY;
			if (targetType == java.sql.Types.LONGVARBINARY)
				targetType = java.sql.Types.VARBINARY;

			// FLOAT == REAL
			if (   (originalType == java.sql.Types.FLOAT
				&& targetType == java.sql.Types.REAL)
			    || (originalType == java.sql.Types.REAL
				&& targetType == java.sql.Types.FLOAT))	{
			  // okay
			} else
			// handle time types identically
			if ((originalType == java.sql.Types.TIME
				|| originalType == java.sql.Types.TIMESTAMP
				|| originalType == java.sql.Types.DATE)
				&& (targetType == java.sql.Types.TIME
					|| targetType == java.sql.Types.TIMESTAMP
					|| targetType == java.sql.Types.DATE)) {
				// okay
			} else if (
				originalType == java.sql.Types.VARBINARY
					&& targetType == java.sql.Types.BINARY) {
				// this types are compatible
				// but if anything else changed force a conversion!
				Object[] arguments = { getName()};
				cat.infoT(loc, 
					"compareTo ({0}): original type VARBINARY -- target type BINARY",
					arguments);
				varbinaryBinarySituation = true;
			} else if (originalType != targetType) {
				plan.setTypeIsChanged(true);
				if ((originalType == java.sql.Types.SMALLINT
					&& targetType == java.sql.Types.INTEGER)
					|| (originalType == java.sql.Types.VARCHAR
						&& targetType == java.sql.Types.CLOB)
					|| (originalType == java.sql.Types.VARBINARY
						&& targetType == java.sql.Types.BLOB)
					|| (originalType == java.sql.Types.FLOAT
						&& targetType == java.sql.Types.DOUBLE)) {
					// okay
				} else {
					Object[] arguments =
						{
							getName(),
							getJavaSqlTypeName(),
							target.getJavaSqlTypeName()};
					cat.infoT(loc, 
						"compareTo ({0}): conversion necessary: original type {1} incompatible to target type {2}",
						arguments);
					loc.exiting();
					return (
						new DbColumnDifference(
							this,
							target,
							plan,
							Action.CONVERT));
				}
			}

			/*
			if (targetType != java.sql.Types.CLOB && targetType != java.sql.Types.BLOB &&
			    targetType != java.sql.Types.FLOAT && targetType != java.sql.Types.INTEGER &&
			    targetType != java.sql.Types.SMALLINT && targetType != java.sql.Types.DATE &&
			    targetType != java.sql.Types.DATE && targetType != java.sql.Types.TIME &&
			    targetType != java.sql.Types.TIMESTAMP) */
			if (targetCol.getJavaSqlTypeInfo().hasLengthAttribute()) {
				long orgLen = getLength();
				long targetLen = targetCol.getLength();

				if ((targetType == java.sql.Types.BINARY
					|| targetType == java.sql.Types.CHAR
					|| targetType == java.sql.Types.VARBINARY
					|| targetType == java.sql.Types.VARCHAR)
					&& ((orgLen == 0 && targetLen == 1)
						|| (orgLen == 1 && targetLen == 0))) {
					/* char == char(1), nchar == nchar(1), varchar == varchar(1), nvarchar == nvarchar(1) */
					/* binary == binary(1), varbinary == varbinary(1) */
					Object[] arguments =
						{
							getName(),
							targetCol.getJavaSqlTypeName(),
							new Long(orgLen),
							new Long(targetLen)};
					cat.infoT(loc, 
						"compareTo ({0}): {1} lengths original {2} target {3} (0 == 1)",
						arguments);
				} else if (orgLen > targetLen) {
					plan.setLengthIsChanged(true);
					Object[] arguments =
						{ getName(), new Long(orgLen), new Long(targetLen)};
					cat.infoT(loc, 
						"compareTo ({0}): conversion necessary: original length {1} greater than target length {2}",
						arguments);
					loc.exiting();
					return (
						new DbColumnDifference(
							this,
							target,
							plan,
							Action.CONVERT));
				} else if (orgLen < targetLen) {
					if (varbinaryBinarySituation == true) {
						Object[] arguments =
							{ getName(), new Long(orgLen), new Long(targetLen)};
						cat.infoT(loc, 
							"compareTo ({0}): original VARBINARY({1}) target BINARY({2})",
							arguments);
					}
					plan.setLengthIsChanged(true);
				}

				if (getDecimals() != targetCol.getDecimals()) {
					plan.setDecimalsAreChanged(true);
					long precOrigin = getLength() - getDecimals();
					long precTarget =
						targetCol.getLength() - targetCol.getDecimals();
					if (precOrigin > precTarget) {
						Object[] arguments =
							{
								getName(),
								new Long(precOrigin),
								new Long(precTarget)};
						cat.infoT(loc, 
							"compareTo ({0}): conversion necessary: original precision {1} greater than target precision {2}",
							arguments);
						loc.exiting();
						return (
							new DbColumnDifference(
								this,
								target,
								null,
								Action.CONVERT));
					} else if (precTarget > precOrigin) {
					}
				}
			}

			if (isNotNull() != targetCol.isNotNull()) {
				if (varbinaryBinarySituation == true) {
					Object[] arguments = { getName()};
					if (isNotNull() == true)
						cat.infoT(loc, 
							"compareTo ({0}): original VARBINARY NOT NULL target BINARY NULL",
							arguments);
					else
						cat.infoT(loc, 
							"compareTo ({0}): original VARBINARY NULL target BINARY NOT NULL",
							arguments);
				}
				plan.setNullabilityIsChanged(true);

				if (targetCol.isNotNull() == true && targetCol.getDefaultValue() == null) {
					Object[] arguments = { getName()};
					cat.infoT(loc,	"compareTo ({0}): original NOT NULL target NULL without default",
						arguments);

					nullToNotNullWithoutDefault = true;
				}
			}

			String orgDefVal = getDefaultValue();
			String targetDefVal = targetCol.getDefaultValue();

			if (orgDefVal != null || targetDefVal != null) {
				if ((orgDefVal == null && targetDefVal != null)
					|| (orgDefVal != null && targetDefVal == null)) {
					if (varbinaryBinarySituation == true) {
						Object[] arguments =
							{
								getName(),
								(orgDefVal != null
									? "default " + orgDefVal
									: "no default"),
								(targetDefVal != null
									? "default " + targetDefVal
									: "no default")};
						cat.infoT(loc, 
							"compareTo ({0}): original VARBINARY {1} target BINARY {2}",
							arguments);
					}
					plan.setDefaultValueIsChanged(true);
				} else if (orgDefVal.equals(targetDefVal) == false) {
					boolean differs = true;
					switch (targetType) {
						case (java.sql.Types.SMALLINT) :
						case (java.sql.Types.INTEGER) :
						case (java.sql.Types.BIGINT) :
							try {
								long targetIntVal =
									Long.valueOf(targetDefVal).longValue();
								long orgIntVal =
									Long.valueOf(orgDefVal).longValue();
								differs = targetIntVal != orgIntVal;
							} catch (Exception ex) {
								//$JL-EXC$ 
								Object[] arguments =
									{
										getName(),
										orgDefVal,
										targetDefVal,
										ex.getMessage()};
								cat.infoT(loc, 
									"compareTo ({0}): integer interpretation of defaults (old {1}, new {2}) failed: {3}",
									arguments);
								// not throwing an exception, just accept
							}
							break;

						case (java.sql.Types.DECIMAL) :
						case (java.sql.Types.FLOAT) :
						case (java.sql.Types.DOUBLE) :
							try {
								double targetFloatVal =
									Double.valueOf(targetDefVal).doubleValue();
								double orgFloatVal =
									Double.valueOf(orgDefVal).doubleValue();
								differs = targetFloatVal != orgFloatVal;
							} catch (Exception ex) {
								//$JL-EXC$ 
								Object[] arguments =
									{
										getName(),
										orgDefVal,
										targetDefVal,
										ex.getMessage()};
								cat.infoT(loc, 
									"compareTo ({0}): floating point interpretation of defaults (old {1}, new {2}) failed: {3}",
									arguments);
								// not throwing an exception, just accept
							}
							break;

						case (java.sql.Types.DATE) :
						case (java.sql.Types.TIME) :
						case (java.sql.Types.TIMESTAMP) :
							try {
								String fmtStr = null;
								if (targetType == java.sql.Types.DATE)
									fmtStr = "yyyy-MM-dd";
								else if (targetType == java.sql.Types.TIME)
									fmtStr = "HH:mm:ss.SSS";
								else if (
									targetType == java.sql.Types.TIMESTAMP)
									fmtStr = "yyyy-MM-dd HH:mm:ss.SSS";

								java.text.SimpleDateFormat dateFormatter =
									(SimpleDateFormat) targetCol
										.getJavaSqlTypeInfo()
										.getFormatterForDefaultString();
								java.util.Date datetime =
									dateFormatter.parse(targetDefVal);
								java.text.SimpleDateFormat myFormatter =
									new java.text.SimpleDateFormat(fmtStr);
								targetDefVal = myFormatter.format(datetime);
								//targetDefVal = dateFormatter.format(datetime);

								differs = !orgDefVal.equals(targetDefVal);
								if (differs == true) {
									if (orgDefVal.startsWith(targetDefVal)) {
										// case DATE: the time part is initial 00:00:00.000
										String rest =
											orgDefVal.substring(
												targetDefVal.length());
										differs =
											(rest.equals(" 00:00:00.000")
												== false);
									} else if (
										orgDefVal.endsWith(targetDefVal)) {
										// case TIME: the date part is (hopefully, if generated via us) 1900-01-01
										String rest =
											orgDefVal.substring(
												0,
												orgDefVal.length()
													- targetDefVal.length());
										differs =
											(rest.equals("1900-01-01 ")
												== false);
									} else
										differs = true;
								}
							} catch (Exception ex) {
								//$JL-EXC$ 
								Object[] arguments =
									{
										getName(),
										orgDefVal,
										targetDefVal,
										ex.getMessage()};
								cat.infoT(loc, 
									"compareTo ({0}): date interpretation of defaults (old {1}, new {2}) failed: {3}",
									arguments);
								// not throwing an exception, just accept
							}
							break;

						default :
							differs = true;
							if (varbinaryBinarySituation == true) {
								Object[] arguments =
									{ getName(), orgDefVal, targetDefVal };
								cat.infoT(loc, 
									"compareTo ({0}): original VARBINARY default {1} target BINARY default {2}",
									arguments);
							}
							break;
					}
					plan.setDefaultValueIsChanged(differs);
				}
			}

			if (plan.somethingIsChanged()) {
				Object[] arguments = { getName()};
				if (varbinaryBinarySituation == true  ||
				    nullToNotNullWithoutDefault == true) {
					// special case: force conversion
					cat.infoT(loc, 
						"compareTo ({0}): column definition changed",
						arguments);
					loc.exiting();
					return (
						new DbColumnDifference(
							this,
							target,
							plan,
							Action.CONVERT));
				} else if (
					originalType == java.sql.Types.BLOB
						|| originalType == java.sql.Types.CLOB
						|| originalType == java.sql.Types.LONGVARBINARY
						|| originalType == java.sql.Types.LONGVARCHAR) {
					cat.infoT(loc, 
						"compareTo ({0}): column definition changed, ALTER not possible for image/text fields",
						arguments);
					loc.exiting();
					return (
						new DbColumnDifference(
							this,
							target,
							plan,
							Action.CONVERT));
				} else {
					cat.infoT(loc, 
						"compareTo ({0}): column definition changed but can be handled by ALTER",
						arguments);
					loc.exiting();
					return (
						new DbColumnDifference(
							this,
							target,
							plan,
							Action.ALTER));
				}
			} else {
				loc.exiting();
				return null;
			}
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage()};
			cat.errorT(loc, "compareTo ({0}) failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public boolean acceptedAdd() {
//	  must be either NULL or NOT NULL WITH DEFAULT
	  if (super.isNotNull() == false || super.getDefaultValue() != null)
		 return true;
	  else {
		Object[] arguments = { getName()};
		cat.errorT(loc, "acceptedAdd ({0}): adding column NOT NULL without default requires conversion",
			   arguments);
		return false;
	  }
	}

	public boolean acceptedDrop() {
		return true;
		// ???? true but then we would have to look for the bound default objects
	}

	/**
	 *  Check the column's name according to its length
	 *  @return true - if name-length is o.k
	 * */
	public boolean checkNameLength() {
		loc.entering("checkNameLength");

		int nameLen = this.getName().length();

		boolean check = (nameLen > 0 && nameLen <= 128);

		if (check == false) {
			Object[] arguments = { getName(), new Integer(nameLen)};
			cat.errorT(loc, 
				"checkNameLength {0}: length {1} invalid (allowed range [1..128])",
				arguments);
		}
		loc.exiting();
		return (check);
	}

	/**
	 *  Checks if column-name is a reserved word
	 *  @return true - if column-name has no conflict with reserved words,
	 *                    false otherwise
	 * */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");

		boolean check =
			(DbMssEnvironment.isReservedWord(this.getName()) == false);

		if (check == false) {
			Object[] arguments = { this.getName()};
			cat.errorT(loc, "checkNameForReservedWord: {0} is reserved", arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 *  Check the columns's attributes: type, length and decimals, e.g
	 *  if length is to big for current type
	 *  @return true - if name-length is o.k
	 * */
	public boolean checkTypeAttributes() {
		loc.entering("checkTypeAttributes");

		// todo
		long len;
		boolean check = true;

		switch (this.getJavaSqlType()) {
			case java.sql.Types.DECIMAL :
			case java.sql.Types.NUMERIC :
				len = this.getLength();
				if (len < 0 || len > 38) {
					check = false;

					Object[] arguments = { this.getName(), new Long(len)};
					cat.errorT(loc, 
						"checkTypeAttributes {0}: a length of {1} is invalid for decimal-fields (allowed range [1..38])",
						arguments);
				}
				if (len < this.getDecimals()) {
					check = false;

					Object[] arguments =
						{
							this.getName(),
							new Integer(this.getDecimals()),
							new Long(len)};
					cat.errorT(loc, 
						"checkTypeAttributes {0}: scale {1} is greater than precision {2}",
						arguments);
				}
				break;

			case java.sql.Types.BINARY :
			/*	len = this.getLength();
				if (len < 0 || len > 255) {
					check = false;

					Object[] arguments = { this.getName(), new Long(len)};
					cat.errorT(loc, 
						"checkTypeAttributes {0}: length of {1} is out of range for binary-fields (allowed range [1..255])",
						arguments);
				}
				break;
				
				not done to avoid inconsistencies at customer's side */	
			
			case java.sql.Types.LONGVARBINARY :
			case java.sql.Types.VARBINARY :
				len = this.getLength();
				if (len < 0 || len > 8000) {
					check = false;

					Object[] arguments = { this.getName(), new Long(len)};
					cat.errorT(loc, 
						"checkTypeAttributes {0}: length of {1} is out of range for binary-fields (allowed range [1..8000])",
						arguments);
				}
				break;

			case java.sql.Types.LONGVARCHAR :
			case java.sql.Types.VARCHAR :
			case java.sql.Types.CHAR :
				len = this.getLength();
				if (len < 0 || len > 4000) {
					check = false;

					Object[] arguments = { this.getName(), new Long(len)};
					cat.errorT(loc, 
						"checkTypeAttributes {0}: length of {1} is out of range for character-fields (allowed range [1..4000])",
						arguments);
				}
				break;
                        default:
                                break;
		}

		loc.exiting();
		return check;
	}

	/**
	* If the DbColumn object represents the database state (dbType != null)
	* and the dbType (respecting length and decimals) can be mapped to 2 or more
	* different jdbc types
	* the method returns all this alternatives.
	* otherwise returns null.
	* The porting should overwrite this method only if necessary:
	* @return  the array of jdbc types or null
	*/
	public int[] getJavaSqlTypeAlternatives() { 
		// currently only dbType DATETYPE is of interest for this function,
                // all other dbTypes have onlyone javaType associated:
		// dbType          javaType
		// "DATETIME"      DATE, TIME, TIMESTAMP
                // "NCHAR"         CHAR
                // "NVARCHAR       VARCHAR -- LONGVARCHAR is not supported
                // "BINARY"        BINARY
                // "VARBINARY"     LONGVARBINARY -- VARBINARY is not supported
                // "SMALLINT"      SMALLINT
                // "INTEGER"       INTEGER
                // "BIGINT"        BIGINT
                // "REAL"          REAL -- FLOAT not supported
                // "FLOAT"         DOUBLE
                // "NTEXT"         CLOB
                // "IMAGE"         BLOB
                // "DECIMAL"       DECIMAL -- NUMERIC not supported 

		if (this.getDbType().equalsIgnoreCase("DATETIME")) {
			int[] resArray = { java.sql.Types.DATE, java.sql.Types.TIME, java.sql.Types.TIMESTAMP };
			return resArray;
		}
		
		return null;
	}

}