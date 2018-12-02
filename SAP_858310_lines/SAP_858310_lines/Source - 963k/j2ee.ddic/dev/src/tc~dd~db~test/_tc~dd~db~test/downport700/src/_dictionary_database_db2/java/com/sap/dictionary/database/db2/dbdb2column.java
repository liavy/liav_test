package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Title:        Analysis of table and view changes: DB2/390 specific classes
 * Description:  DB2/390 specific analysis of table and view changes. Tool to deliver Db2/390 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2Column extends DbColumn {
	String lobTableName = null; // needed for Alter 
	// in DbDb2ColumnsDifference.getDdlStatementsForAlter
	// as only table name but no object is provided by caller
	// so in case of lob table real db name cannot be provided 

	// workaround for 255 byte index width limit 
	private Integer excLength = null; // overwrite specified length 

	private static Location loc = Logger.getLocation("db2.DbDb2Column");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private boolean isV9 = false;
		
	public DbDb2Column(DbFactory factory) {
		super(factory);
		setDbEnv(factory);
	}

	public DbDb2Column(DbFactory factory, DbColumn other) {
		super(factory, other);
		setDbEnv(factory);
	}

	public DbDb2Column(DbFactory factory, XmlMap xmlMap) {
		super(factory, xmlMap);
		setDbEnv(factory);
	}

	public DbDb2Column(
		DbFactory factory,
		String name,
		int position,
		int javaSqlType,
		String dbType,
		long length,
		int decimals,
		boolean isNotNull,
		String defaultValue) {
		setDbEnv(factory);
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
	
	private void setDbEnv(DbFactory factory) {
		((DbDb2Environment) factory.getEnvironment()).getDb2Paramter().setValues(factory.getConnection());
		isV9 = ((DbDb2Environment) factory.getEnvironment()).isV9(factory.getConnection());
	}
	
	public String getTypeClauseForDdl() throws Exception {
		return (super.getDdlTypeClause());
	}

	protected DbColumnDifference comparePartTo(DbColumn target) throws JddException {
		loc.entering("compareTo");
		try {
			DbColumnDifference colDiff = null;
			DbDb2Column targetCol = null;
			DbColumnDifferencePlan plan = new DbColumnDifferencePlan();
			targetCol = (DbDb2Column) target;

			//
			// Compare column types:
			//

			if (typeChanged(target)) {
				plan.setTypeIsChanged(true);
				if (!canChangeType(target)) {
					Object[] arguments =
						{
							getName(),
							getJavaSqlTypeName(),
							target.getJavaSqlTypeName()};
					cat.infoT(
						loc,
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
			if (lengthChanged(target)) {
				plan.setLengthIsChanged(true);
				if (!canChangeType(target)) {
					Object[] arguments =
						{
							getName(),
							getJavaSqlTypeName(),
							new Long(getLength()),
							new Long(targetCol.getLength())};
					cat.infoT(
						loc,
						"compareTo ({0}): conversion necessary for type {1}: original length {2} differs from target length {3}}",
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

			if (scaleChanged(target)) {
				plan.setDecimalsAreChanged(true);
				if (!canChangeType(target)) {
					Object[] arguments =
						{
							getName(),
							getJavaSqlTypeName(),
							new Long(getDecimals()),
							new Long(targetCol.getDecimals())};
					cat.infoT(
						loc,
						"compareTo ({0}): conversion necessary for type {1}: original precision {2} differs from target precision {3}}",
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

			//
			// Compare nullability:
			//
			if (isNotNull() != targetCol.isNotNull()) {
				boolean nullabilityIsChanged = true;
				if (nullabilityIsChanged) {
					plan.setNullabilityIsChanged(true);
					Object[] arguments = { getName()};
					cat.infoT(
						loc,
						"compareTo ({0}): conversion necessary: nullability changed",
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

			if (!compareDefault(targetCol)) {
				String targetDefault = targetCol.getDefaultValue();
				// accept target default null: default-default is set on DB       	
				//				if (!((targetDefault == null)
				//					&& (this.getJavaSqlTypeInfo().getDefaultDefault() != null)
				//					&& (0
				//						== this.getDefaultValue().compareTo(
				//							this.getJavaSqlTypeInfo().getDefaultDefault())))) {
				JavaSqlTypeInfo javaSqlTypeInfo = this.getJavaSqlTypeInfo();
				boolean defaultValueIsChanged = false;
				switch (javaSqlTypeInfo.getIntCode()) {
					case (java.sql.Types.TIME) :
						Format dateFormatter =
							javaSqlTypeInfo.getFormatterForDefaultString();
						if (targetDefault != null) {
							Object obj =
								dateFormatter.parseObject(targetDefault);
							SimpleDateFormat db2DateFormatter =
								new SimpleDateFormat("HH.mm.ss");
							String defVal = db2DateFormatter.format(obj);
							if (0 != this.getDefaultValue().compareTo(defVal))
								defaultValueIsChanged = true;
						} else
							defaultValueIsChanged = true;
						break;
					case (java.sql.Types.TIMESTAMP) :
						dateFormatter =
							javaSqlTypeInfo.getFormatterForDefaultString();
						if (targetDefault != null) {
							Object obj =
								dateFormatter.parseObject(targetDefault);
							SimpleDateFormat db2DateFormatter =
								new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
							String defVal = db2DateFormatter.format(obj);
							if (0
								!= this.getDefaultValue().substring(
									0,
									defVal.length()).compareTo(
									defVal))
								defaultValueIsChanged = true;
						} else
							defaultValueIsChanged = true;
						break;
					default :
						if (javaSqlTypeInfo.hasDefaultValue())
							defaultValueIsChanged = true;
				}
				if (defaultValueIsChanged) {
					plan.setDefaultValueIsChanged(true);
					Object[] arguments =
						{
							getName(),
							getDefaultValue(),
							targetCol.getDefaultValue()};
					cat.infoT(
						loc,
						"compareTo ({0}): conversion necessary: original default value {1} differs from target default value {2}}",
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

			if (plan.somethingIsChanged()) {
				Object[] arguments = { getName()};
				cat.infoT(
					loc,
					"compareTo ({0}): column definition changed but can be handled by ALTER",
					arguments);
				loc.exiting();
				return (
					new DbColumnDifference(this, target, plan, Action.ALTER));
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
		boolean accept = true;
		loc.entering("acceptedAdd");
		if (super.isNotNull() == true && super.getDefaultValue() == null) {
			Object[] arguments = { getName()};
			cat.infoT(
				loc,
				"Add column {0} not accepted: NOT NULL is specified but no DEFAULT value is set",
				arguments);
			accept = false;
		}
		loc.exiting();
		return accept;
	}

	public boolean acceptedDrop() {
		return false;
	}

	public boolean compareDefault(DbDb2Column targetCol) {
		String origDefault = getDefaultValue();
		String targetDefault = targetCol.getDefaultValue();

		if (origDefault == null && targetDefault == null)
			return true;

		if ((origDefault == null && targetDefault != null)
			|| (origDefault != null && targetDefault == null)
			|| (origDefault != null
				&& targetDefault != null
				&& !origDefault.equals(targetDefault))) {
			return false;
		} else
			return true;
	}

	/**
	 *  Check the column's name according to its length  
	 *  @return true - if name-length is o.k
	 * */
	public boolean checkNameLength() {
		loc.entering("checkNameLength");
		boolean check = true;
		if (getName().length() > DbDb2Parameters.maxColNameLen) {
			check = false;
			Object[] arguments =
				{
					getName(),
					new Integer(getName().length()),
					new Integer(DbDb2Parameters.maxColNameLen)};
			cat.errorT(
				loc,
				"checkNameLength {0}: length of column name {1} not in allowed range [1,{2}]",
				arguments);
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
		loc.entering("checkTypeAttributes()");
		boolean check = true;

		switch (getJavaSqlTypeInfo().getIntCode()) {
			case java.sql.Types.DECIMAL :
			case java.sql.Types.NUMERIC :
				long prec = this.getLength();
				long scale = this.getDecimals();
				if (prec < 1 || prec > 31) {
					Object[] arguments =
						{
							getName(),
							new Long(prec),
							new Integer(DbDb2Parameters.maxDecimalDigits)};
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: precision {1} not in allowed range [1,{2}] for decimal-fields",
						arguments);
					check = false;
				}
				if (prec < scale) {
					Object[] arguments =
						{ getName(), new Long(scale), new Long(prec), };
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: scale of decimal ({1}) greater than precision ({2}).",
						arguments);
					check = false;
				}
				break;
			case (java.sql.Types.BINARY) :
			case (java.sql.Types.VARBINARY) :
			case (java.sql.Types.LONGVARBINARY) :
				long len = this.getLength();
				if (len < 0 || len > DbDb2Parameters.maxLongRaw) {
					check = false;
					Object[] arguments =
						{
							getName(),
							new Long(len),
							new Integer(DbDb2Parameters.maxLongRaw)};
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: length {1} not in allowed range [1,{2}] for binary-fields",
						arguments);
				}
				break;
			case (java.sql.Types.BLOB) :
				len = this.getLength();
				if (len < 0 || len > DbDb2Parameters.maxBlobLength) {
					check = false;
					Object[] arguments =
						{
							getName(),
							new Long(len),
							new Integer(DbDb2Parameters.maxBlobLength)};
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: length {1} not in allowed range [1,{2}] for blob-fields",
						arguments);
				}
				break;
			case (java.sql.Types.CHAR) :
			case (java.sql.Types.VARCHAR) :
			case (java.sql.Types.LONGVARCHAR) :
				len = this.getLength();
				if (len < 0 || len > DbDb2Parameters.maxLongChar) {
					check = false;
					Object[] arguments =
						{
							getName(),
							new Long(len),
							new Integer(DbDb2Parameters.maxLongChar)};
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: length {1} not in allowed range [1,{2}] for character-fields",
						arguments);
				}
				break;
			case (java.sql.Types.CLOB) :
				len = this.getLength();
				if (len < 0 || len > DbDb2Parameters.maxClobLength) {
					check = false;
					Object[] arguments =
						{
							getName(),
							new Long(len),
							new Integer(DbDb2Parameters.maxClobLength)};
					cat.errorT(
						loc,
						"checkTypeAttributes {0}: length {1} not in allowed range [1,{2}] for clob-fields",
						arguments);
				}
				break;

		}
		loc.exiting();
		return check;
	}

	/**
	 *  Checks if column-name is a reserved word
	 *  @return true - if column-name has no conflict with reserved words, 
	 *                    false otherwise
	 * */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");
		boolean check =
			(DbDb2Environment.isReservedWord(this.getName()) == false);
		if (check == false) {
			Object[] arguments = { this.getName()};
			cat.errorT(
				loc,
				"checkNameForReservedWord {0}: reserved",
				arguments);
		}
		loc.exiting();
		return check;
	}

	public String getDdlDefaultValueClause() throws Exception {
		loc.entering("getDdlDefaultValueClause");
		try {
			JavaSqlTypeInfo javaSqlTypeInfo = super.getJavaSqlTypeInfo();
			String clause = "";

			switch (javaSqlTypeInfo.getIntCode()) {
				case (java.sql.Types.CHAR) :
				case (java.sql.Types.VARCHAR) :
				case (java.sql.Types.LONGVARCHAR) :
					String defValOrg = super.getDefaultValue();
					if ((null != defValOrg) && defValOrg.indexOf('\'') >= 0) {
						String defValMod = escapeSpecialCharacters(defValOrg);
						clause =
							"DEFAULT "
								+ javaSqlTypeInfo.getDefaultValuePrefix()
								+ defValMod
								+ javaSqlTypeInfo.getDefaultValueSuffix();
					} else
						return (super.getDdlDefaultValueClause());
					break;
				case (java.sql.Types.TIME) :
					Format dateFormatter =
						javaSqlTypeInfo.getFormatterForDefaultString();
					String s = super.getDefaultValue();
					if (s != null) {
						Object obj = dateFormatter.parseObject(s);
						SimpleDateFormat db2DateFormatter =
							new SimpleDateFormat("HH.mm.ss");
						String defVal = db2DateFormatter.format(obj);
						clause =
							"DEFAULT "
								+ javaSqlTypeInfo.getDefaultValuePrefix()
								+ defVal
								+ javaSqlTypeInfo.getDefaultValueSuffix();
					}
					break;
				case (java.sql.Types.TIMESTAMP) :
					dateFormatter =
						javaSqlTypeInfo.getFormatterForDefaultString();
					s = super.getDefaultValue();
					if (s != null) {
						Object obj = dateFormatter.parseObject(s);
						SimpleDateFormat db2DateFormatter =
							new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
						String defVal = db2DateFormatter.format(obj);
						clause =
							"DEFAULT "
								+ javaSqlTypeInfo.getDefaultValuePrefix()
								+ defVal
								+ javaSqlTypeInfo.getDefaultValueSuffix();
					}
					break;
				case (java.sql.Types.BINARY) :
				case (java.sql.Types.VARBINARY) :
				case (java.sql.Types.LONGVARBINARY) :
					String defaultHexSpecifier = (this.isV9 ? "BX" : "X");
					String defVal = super.getDefaultValue();
					if (defVal != null) {
						clause =
							"DEFAULT "
								+ defaultHexSpecifier
								+ "'"
								+ defVal
								+ "'";
					}
					break;
				default :
					return (super.getDdlDefaultValueClause());
			}
			loc.exiting();
			return clause;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlDefaultValueClause failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setLobTableName(String lobTableName) {
		this.lobTableName = lobTableName;
	}

	public String getLobTableName() {
		return lobTableName;
	}

	public void setExcLength(Integer l) {
		excLength = l;
	}

	public Integer getExcLength() {
		return excLength;
	}

	public boolean isNumeric() {
		loc.entering("isNumeric");
		try {
			DbColumn source = this;
			int sourceType = this.getJavaSqlType();

			switch (sourceType) {
				// numeric types 
				case java.sql.Types.SMALLINT :
				case java.sql.Types.INTEGER :
				case java.sql.Types.BIGINT :
				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
				case java.sql.Types.FLOAT :
				case java.sql.Types.DOUBLE :				
					Object[] arguments =
						{ getName(), new Integer(sourceType), };
					cat.infoT(
						loc,
						"isNumeric {0} type {1}: true.",
						arguments);
					loc.exiting();
					return true;
				default :
					loc.exiting();
					return false;
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "isNumeric: {0}", arguments);
			loc.exiting();
			return false;
		}
	}
	public boolean isConvFromCharForBitDataToBinary() {
		loc.entering("isConvFromCharForBitDataToBinary");
		try {
			DbColumn source = this;
			int sourceType = this.getJavaSqlType();

			switch (sourceType) {
				// binary types 
				case java.sql.Types.BINARY :
				case (java.sql.Types.VARBINARY) :
				case (java.sql.Types.LONGVARBINARY) :
					String sourceDbType = this.getDbType();
					if (this.isV9 
						&& (sourceDbType.equalsIgnoreCase("CHAR")
							|| sourceDbType.equalsIgnoreCase("VARCHAR"))) {
						Object[] arguments =
							{ getName(), new Integer(sourceType), };
						cat.infoT(
							loc,
							"isConvFromCharForBitDataToBinary {0} type {1}: true.",						
							arguments);
						loc.exiting();
						return true;
					}
					Object[] arguments =
						{ getName(), new Integer(sourceType), };
					cat.infoT(
						loc,
						"isConvFromCharForBitDataToBinary {0} type {1}: false.",
						arguments);
					loc.exiting();
					return false;
				default :
					loc.exiting();
					return false;
			}

		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "isConvFromCharForBitDataToBinary: {0}", arguments);
			loc.exiting();
			return false;
		}
	}

	private boolean canChangeType(DbColumn target) {
		loc.entering("canChangeType");
		DbColumn source = this;
		int sourceType = this.getJavaSqlType();
		int targetType = target.getJavaSqlType();
		boolean canChangeType = false;

		switch (sourceType) {

			// numeric types 
			case java.sql.Types.SMALLINT :
				switch (targetType) {
					case java.sql.Types.SMALLINT :
					case java.sql.Types.INTEGER :
					case java.sql.Types.BIGINT :
						canChangeType = true;
						break;
					case java.sql.Types.DECIMAL :
					case java.sql.Types.NUMERIC :
						long q = target.getLength();
						int t = target.getDecimals();
						if ((q - t) > 4)
							canChangeType = true;
						break;
					case java.sql.Types.FLOAT :
					case java.sql.Types.DOUBLE :
						canChangeType = true;
						break;
				}
				break;
			case java.sql.Types.INTEGER :
				switch (targetType) {
					case java.sql.Types.INTEGER :
					case java.sql.Types.BIGINT :
						canChangeType = true;
						break;
					case java.sql.Types.DECIMAL :
					case java.sql.Types.NUMERIC :
						long q = target.getLength();
						int t = target.getDecimals();
						if ((q - t) > 9)
							canChangeType = true;
						break;
					case java.sql.Types.FLOAT :
					case java.sql.Types.DOUBLE :
						canChangeType = true;
						break;
				}
				break;
			case java.sql.Types.DECIMAL :
			case java.sql.Types.NUMERIC :
			case java.sql.Types.BIGINT : // bigint is DECIMAL(19,0)	
				long p;
				int s;
				if (sourceType == java.sql.Types.BIGINT) {
					p = 19;
					s = 0;
				} else {
					p = source.getLength();
					s = source.getDecimals();
				}

				switch (targetType) {
					case java.sql.Types.SMALLINT :
						if (s == 0 && p < 5)
							canChangeType = true;
						break;
					case java.sql.Types.INTEGER :
						if (s == 0 && p < 10)
							canChangeType = true;
						break;
					case java.sql.Types.BIGINT :
						if (s == 0 && p <= 19)
							canChangeType = true;
						break;
					case java.sql.Types.DECIMAL :
					case java.sql.Types.NUMERIC :
						long q = target.getLength();
						int t = target.getDecimals();
						if ((q >= p) && ((q - t) >= (p - s)))
							canChangeType = true;
						break;
					case java.sql.Types.FLOAT :
					case java.sql.Types.DOUBLE :
						if (p < 16)
							canChangeType = true;
						break;
				}
				break;
			case java.sql.Types.FLOAT :
			case java.sql.Types.DOUBLE :
				//				switch (targetType) {
				//					case java.sql.Types.INTEGER :
				//						changeType = true;
				//						break;
				//				}
				break;

				// binary types
			case java.sql.Types.BINARY :
			case java.sql.Types.VARBINARY :
			case java.sql.Types.LONGVARBINARY :
				long l = source.getLength();
				switch (targetType) {
					case java.sql.Types.BINARY :
					case java.sql.Types.VARBINARY :
					case java.sql.Types.LONGVARBINARY :
						long m = target.getLength();
						if (m >= l)
							canChangeType = true;
						break;
				}
				break;

				// character types 	
			case java.sql.Types.CHAR :
			case java.sql.Types.VARCHAR :
			case java.sql.Types.LONGVARCHAR :
				l = source.getLength();
				switch (targetType) {
					case java.sql.Types.CHAR :
					case java.sql.Types.VARCHAR :
					case java.sql.Types.LONGVARCHAR :
						if (l <= DbDb2Parameters.maxLongChar) {
							long m = target.getLength();
							if ((m >= l) && (m <= DbDb2Parameters.maxLongChar))
								canChangeType = true;
						}
						break;
					case java.sql.Types.CLOB :
						if (l > DbDb2Parameters.maxLongChar)
							canChangeType = true;
						break;
				}
				break;
			case java.sql.Types.CLOB :
				l = source.getLength();
				switch (targetType) {
					case java.sql.Types.CHAR :
					case java.sql.Types.VARCHAR :
					case java.sql.Types.LONGVARCHAR :
						long m = target.getLength();
						if (m > DbDb2Parameters.maxLongChar)
							canChangeType = true;
						break;
					case java.sql.Types.CLOB :
						canChangeType = true;
						break;
				}
				break;
		}

		Object[] arguments =
			{
				getName(),
				new Boolean(canChangeType),
				source.getJavaSqlTypeName(),
				new Long(source.getLength()),
				new Integer(source.getDecimals()),
				target.getJavaSqlTypeName(),
				new Long(target.getLength()),
				new Integer(target.getDecimals())};
		cat.infoT(
			loc,
			"canChangeType {0} {1}: from type {2}, length {3}, decimals {4} to type {5}, length {6}, decimals {7}",
			arguments);
		loc.exiting();
		return canChangeType;
	}

	public boolean typeChanged(DbColumn target) {
		loc.entering("typeChanged");
		int sourceType = this.getJavaSqlType();
		int targetType = target.getJavaSqlType();
		boolean typeChanged = false;

		if (sourceType != targetType
			|| sourceType == java.sql.Types.BINARY
			|| sourceType == java.sql.Types.VARBINARY
			|| sourceType == java.sql.Types.LONGVARBINARY) {
			typeChanged = true;
			// we map char and longvarchar to varchar and 
			// varbinary and longvarbinary to varbinary  and
			// float to double and 
			// numeric to decimal
			// accept difference. 

			switch (sourceType) {
				// numeric types 
				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
					// BIGINT is DECIMAL(19,0) on V8				
					long p = this.getLength();
					int s = this.getDecimals();
					switch (targetType) {
						case java.sql.Types.BIGINT :
							/*if (s == 0 && p == 19)
								if (this.isV9
									&& !this
										.getColumns()
										.getTable()
										.getName()
										.equals(
										RUNTIME_OBJECTS_TABLE_NAME))
									typeChanged = true;
								else*/
								    // accept type DECIMAL(19,0) also for V9 
								    // for compatibility reasons 
									typeChanged = false;
							break;
						case java.sql.Types.DECIMAL :
						case java.sql.Types.NUMERIC :
							long q = target.getLength();
							int t = target.getDecimals();
							if ((q == p) && (t == s))
								typeChanged = false;
							break;
					}
					break;
				case java.sql.Types.FLOAT :
				case java.sql.Types.DOUBLE :
					switch (targetType) {
						case java.sql.Types.FLOAT :
						case java.sql.Types.DOUBLE :
						case java.sql.Types.REAL :
							typeChanged = false;
							break;
					}
					break;
					// BIT is mapped to SMALLINT
				case java.sql.Types.SMALLINT :
					if (targetType == java.sql.Types.BIT) {
						typeChanged = false;
						break;
					}
					// binary types
				case java.sql.Types.BINARY :
				case java.sql.Types.VARBINARY :
				case java.sql.Types.LONGVARBINARY :
					switch (targetType) {
						case java.sql.Types.BINARY :
						case java.sql.Types.VARBINARY :
						case java.sql.Types.LONGVARBINARY :
							String sourceDbType = this.getDbType();
							if (this.isV9
								&& (sourceDbType.equalsIgnoreCase("CHAR")
									|| sourceDbType.equalsIgnoreCase("VARCHAR")))
								typeChanged = true;
							else
							    // accept type 'VARCHAR FOR BIT DATA' also for V9 
							    // for compatibility reasons 
								typeChanged = false;
							break;
					}
					break;

					// character types 	
				case java.sql.Types.CHAR :
				case java.sql.Types.VARCHAR :
				case java.sql.Types.LONGVARCHAR :
					switch (targetType) {
						case java.sql.Types.CHAR :
						case java.sql.Types.VARCHAR :
						case java.sql.Types.LONGVARCHAR :
							typeChanged = false;
							break;
					}
					break;
				case java.sql.Types.CLOB :
					switch (targetType) {
						case java.sql.Types.CHAR :
						case java.sql.Types.VARCHAR :
						case java.sql.Types.LONGVARCHAR :
							long m = target.getLength();
							if (m > DbDb2Parameters.maxLongChar)
								typeChanged = false;
							break;

					}
			}
		}
		loc.exiting();
		return typeChanged;
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
		String sourceDbType = this.getDbType();
		if ( sourceDbType == null )
			return null;
		else if (    ( sourceDbType.equalsIgnoreCase("FLOAT") )
			      || ( sourceDbType.equalsIgnoreCase("DOUBLE") ) ) {
			int types[] = { java.sql.Types.DOUBLE , java.sql.Types.REAL };
		    return types;
		} 
		else if (    sourceDbType.equalsIgnoreCase("DECIMAL")
			      && ( this.getLength() == 19 ) 
			      && ( this.getDecimals() == 0 ) ) { 
			int types[] = { java.sql.Types.BIGINT , java.sql.Types.DECIMAL };
		    return types;
		}
		else 
		    return null;
	}

	private boolean lengthChanged(DbColumn target) {
		if (this.getJavaSqlTypeInfo().hasLengthAttribute()
			&& target.getJavaSqlTypeInfo().hasLengthAttribute())
			if (this.getLength() != target.getLength())
				return true;
		return false;
	}

	private boolean scaleChanged(DbColumn target) {
		// Compare scale (i.e. decimals) and precision:
		//
		if (this.getJavaSqlTypeInfo().hasDecimals()
			&& (this.getDecimals() != target.getDecimals()))
			return true;
		return false;
	}

	/**
		  *  Mask each special character (') in the <value> string
		  *  @return modified string
		  **/
	private String escapeSpecialCharacters(String value) {
		final int length = value.length();
		final char[] chars = value.toCharArray();
		final StringBuffer buffer = new StringBuffer(length);
		String escape = "'";

		for (int i = 0; i < length; i++) {
			final char c = chars[i];
			if (c == '\'') {
				buffer.append(escape + c);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

}