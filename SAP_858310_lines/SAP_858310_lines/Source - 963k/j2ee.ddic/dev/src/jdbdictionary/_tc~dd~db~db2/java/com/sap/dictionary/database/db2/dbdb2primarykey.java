package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Iterator;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * @author d022204
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class DbDb2PrimaryKey extends DbPrimaryKey {
	private static Location loc = Logger.getLocation("db2.DbDb2PrimaryKey");

	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);

	String schema = null;

	DbDb2Index index = null;
	private boolean isV9 = false;
	private DbDb2Environment db2Env = null;
	
	public DbDb2PrimaryKey() {
	}

	public DbDb2PrimaryKey(DbFactory factory, DbPrimaryKey other) {
		super(factory, other);
		setDbEnv(factory);
		setSchema(factory);
	}

	public DbDb2PrimaryKey(DbFactory factory) {
		super(factory);
		setDbEnv(factory);
		setSchema(factory);
	}

	public DbDb2PrimaryKey(DbFactory factory, DbSchema schema, String tableName) {
		super(factory, schema, tableName);
		setDbEnv(factory);
		setSchema(factory);
	}

	public DbDb2PrimaryKey(DbFactory factory, String tableName) {
		super(factory, tableName);
		setDbEnv(factory);
		setSchema(factory);
	}

	public DbDb2PrimaryKey(DbFactory factory, String tableName, DbDb2Index index) {
		super(factory, tableName);
		setDbEnv(factory);
		setSchema(factory);
		this.index = index;
	}

	public DbDb2PrimaryKey(DbFactory factory, DbSchema schema,
			String tableName, DbDb2Index index) {
		super(factory, schema, tableName);
		setDbEnv(factory);
		setSchema(factory);
		this.index = index;
	}
	
	private void setDbEnv(DbFactory factory) {
	  	this.db2Env = ((DbDb2Environment) factory.getEnvironment());
	  	db2Env.getDb2Paramter().setValues(factory.getConnection());
	   	this.isV9 = db2Env.isV9(factory.getConnection());
	}
	
	private void setSchema(DbFactory factory) {
		String schema = null;
		DbSchema dbschema = getDbSchema();
		if (dbschema != null)
			schema = dbschema.getSchemaName();
		if (schema == null) {
			Connection con = factory.getConnection();
			this.schema = db2Env.getSchema(con);
		} else {
			this.schema = schema;
		}
	}

	public DbObjectSqlStatements getDdlStatementsForCreate() {
		loc.entering("getDdlStatementsForCreate");
		String tableName = getTable().getName();
		DbObjectSqlStatements priKeyDef = new DbObjectSqlStatements(tableName);
		DbSqlStatement createStatement = new DbSqlStatement();

		DbFactory factory = getDbFactory();
		setSchema(factory);		

		// DB2 will not create a unique index corresponding
		// to the primary key:
		// create an auxilliary unique index
		if (null == index) {
			String indName = getAuxIndName(this.getTableName());
			index = new DbDb2Index(this.getDbFactory(), this.getTableName(),
					indName);
			index.setContent(true, this.getColumnNames());
			index.setForPrimaryKey();

			DbIndexes indexes = new DbIndexes(factory);
			// Set parent
			index.setIndexes(indexes);
			indexes.add(index);
			indexes.setTable(getTable());
		}
		priKeyDef.merge(index.getDdlStatementsForCreate());

		createStatement.addLine("ALTER TABLE "
				+ DbDb2Environment.quote(tableName) + " ADD PRIMARY KEY ");
		createStatement.merge(getDdlColumnsClause());

		priKeyDef.add(createStatement);
		if (DbDb2Parameters.commit)
			priKeyDef.add(DbDb2Environment.commitLine);
		loc.exiting();
		return priKeyDef;
	}

	public DbObjectSqlStatements getDdlStatementsForCreateKey() {
		loc.entering("getDdlStatementsForCreateKey");
		String tableName = getTable().getName();
		DbObjectSqlStatements priKeyDef = new DbObjectSqlStatements(tableName);
		DbSqlStatement createStatement = new DbSqlStatement();

		DbFactory factory = getDbFactory();
		setSchema(factory);

		createStatement.addLine("ALTER TABLE " + tableName
				+ " ADD PRIMARY KEY ");
		createStatement.merge(getDdlColumnsClause());

		priKeyDef.add(createStatement);
		if (DbDb2Parameters.commit)
			priKeyDef.add(DbDb2Environment.commitLine);
		loc.exiting();
		return priKeyDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() {
		loc.entering("getDdlStatementsForDrop");
		String tableName = getTable().getName();
		DbObjectSqlStatements priKeyDef = new DbObjectSqlStatements(tableName);
		DbSqlStatement dropStatement = new DbSqlStatement(true);

		DbFactory factory = getDbFactory();
		setSchema(factory);
		Connection con = factory.getConnection();

		dropStatement
				.addLine("ALTER TABLE " + tableName + " DROP PRIMARY KEY ");
		priKeyDef.add(dropStatement);

		if (!((DbDb2Table) this.getTable()).getImplicitV9()) {		
			if (null == index) {
				// this.getTable().setCommonContentViaDb(factory);
				// index = (( DbDb2PrimaryKey )
				// this.getTable().getPrimaryKey()).index;
				index = ((DbDb2Table) this.getTable())
						.dbGetPrimaryKeyIndex(con);
				if (index != null)
					index.setForPrimaryKey();
			}
			if (index != null) {
				priKeyDef.merge(index.getDdlStatementsForDrop());
			}
		}
		if (DbDb2Parameters.commit)
			priKeyDef.add(DbDb2Environment.commitLine);
		loc.exiting();
		return priKeyDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDropKey() {
		loc.entering("getDdlStatementsForDropKey");
		String tableName = getTable().getName();
		DbObjectSqlStatements priKeyDef = new DbObjectSqlStatements(tableName);
		DbSqlStatement dropStatement = new DbSqlStatement(true);

		DbFactory factory = getDbFactory();
		setSchema(factory);

		dropStatement.addLine("ALTER TABLE "
				+ DbDb2Environment.quote(tableName) + " DROP PRIMARY KEY ");

		priKeyDef.add(dropStatement);
		if (DbDb2Parameters.commit)
			priKeyDef.add(DbDb2Environment.commitLine);
		loc.exiting();
		return priKeyDef;
	}

	public void setCommonContentViaDb() {
		loc.entering("setCommonContentViaDb()");
		if (index != null)
			setContent(index.getColumnNames());
		loc.exiting();
		return;
	}

	public void setSpecificContentViaRef(DbDb2PrimaryKey other) {
		loc.entering("setSpecificContentViaRef()");
		
		if (!this.isV9) {
			if (index != null && other.getIndex() != null) {
				index.setSpecificContentViaRef(other.getIndex());
			}
			this.setSpecificIsSet(true);
		}

		loc.exiting();
		return;
	}

	public DbDb2Index getIndex() {
		return index;
	}

	/**
	 * create name for unique index for primary key mask is: #<tabname>___
	 * substitute last three characters by random characters
	 * 
	 * @return index name
	 */
	private String getAuxIndName(String tabname) {
		long stime = new java.util.Date().getTime();
		String auxIndName = null;
		int len = Math.min(tabname.length(),
				DbDb2Parameters.maxIndexNameLen - 4);
		auxIndName = '#' + tabname.substring(0, len);
		auxIndName += DbDb2Environment.getRandomString(3);
		return auxIndName;
	}

	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
			throws JddException {
		loc.entering("writeSpecificContentToXmlFile");

		try {
			if (index != null)
				index.writeSpecificContentToXmlFile(file, offset0);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "writeSpecificContentToXmlFile: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		loc.exiting();
	}

	public void setSpecificContentViaDb() throws JddException {
		loc.entering("setSpecificContentViaDb");
		Connection con = null;
		if ((con = this.getDbFactory().getConnection()) == null) {
			loc.exiting();
			return;
		}
		
		try {
			if (!this.isV9 && null == index) {
				index = ((DbDb2Table) this.getTable())
						.dbGetPrimaryKeyIndex(con);
			    index.setSpecificContentViaDb();
			}
			loc.exiting();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setSpecificContentViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
		loc.entering("setCommonContentViaXml");
		
		try {
			super.setCommonContentViaXml(xmlMap);
			if (!this.isV9 && null == index) {
				String indName = getAuxIndName(this.getTableName());
				index = new DbDb2Index(this.getDbFactory(),
						this.getTableName(), indName);
				index.setContent(true, this.getColumnNames());

				// Set parent
				DbIndexes indexes = new DbIndexes(getDbFactory());
				index.setIndexes(indexes);
				indexes.add(index);
				indexes.setTable(getTable());
				index.setForPrimaryKey();
			}
			loc.exiting();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setCommonContentViaXml: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setTable(DbTable dbTable) {
		super.setTable(dbTable);
		
		if (!this.isV9 && null != index)
			index.getIndexes().setTable(dbTable);
	}

	public void setSpecificContentViaXml(XmlMap xml) throws JddException {
		loc.entering("setSpecificContentViaXml");

		if (!this.isV9) {
			try {
				if (null == index) {
					String indName = getAuxIndName(this.getTableName());
					index = new DbDb2Index(this.getDbFactory(), this
							.getTableName(), indName);
					index.setContent(true, this.getColumnNames());
					// Set parent
					DbIndexes indexes = new DbIndexes(getDbFactory());
					index.setIndexes(indexes);
					indexes.add(index);
					indexes.setTable(getTable());
					index.setForPrimaryKey();
				}
				index.setSpecificContentViaXml(xml);
				loc.exiting();
			} catch (Exception ex) {
				Object[] arguments = { ex.getMessage() };
				cat.errorT(loc, "setSpecificContentViaXml: {0}", arguments);
				loc.exiting();
				throw JddException.createInstance(ex);
			}
		}
	}

	/**
	 * Checks if primary key-columns are not null
	 * 
	 * @return true - if number of primary-columns are all not null, false
	 *         otherwise
	 */
	public boolean checkColumnsNotNull() {
		loc.entering("checkColumnsNotNull");

		Iterator iter = this.getColumnNames().iterator();
		String colName = null;
		DbColumns columns = this.getTable().getColumns();
		DbColumn column;
		boolean check = true;

		while (iter.hasNext()) {
			colName = ((DbIndexColumnInfo) iter.next()).getName();
			column = columns.getColumn(colName);
			if (column == null || column.isNotNull() == false) {
				check = false;
				Object[] arguments = { this.getTableName(), colName };
				cat
						.errorT(
								loc,
								"checkColumnsNotNull (primary key of {0}): column {1} must not be nullable",
								arguments);
			}
		}
		loc.exiting();
		return check;
	}

	/**
	 * Check the primaryKeys's-width
	 * 
	 * @return true - if primary Key-width is o.k
	 */
	public boolean checkWidth() {
		loc.entering("checkWidth()");
		boolean check = true;
		String tabname = this.getTable().getName();
		int maxKeyWidth = DbDb2Parameters.maxKeyLen;

		Iterator iter = this.getColumnNames().iterator();
		DbColumns columns = this.getTable().getColumns();
		int rowLength = 0;

		while (iter.hasNext()) {
			String colName = ((DbIndexColumnInfo) iter.next()).getName();
			DbColumn column = columns.getColumn(colName);
			if (column == null) {
				check = false;
				Object[] arguments = { this.getTable().getName(), colName };
				cat.errorT(loc,
						"checkWidth {0}: no such column in table ( {1} ).",
						arguments);
				continue;
			} else if (DbDb2Environment.isLob(column)) {
				check = false; // not allowed in index/key
				Object[] arguments = { this.getTable().getName(), colName };
				cat
						.errorT(
								loc,
								"checkWidth {0}: column of type LOB ({1}) not allowed in primary key.",
								arguments);
				continue;
			} else {
				int l = DbDb2Environment.getByteLengthIndex(column);
				rowLength += l;
				if (!column.isNotNull()) // add one byte if column nullable
					rowLength += 1;
			}
		}

		if (rowLength > maxKeyWidth) {
			check = false;
			Object[] arguments = { this.getTable().getName(),
					new Integer(rowLength), new Integer(maxKeyWidth) };
			cat
					.errorT(
							loc,
							"checkWidth (primary key of table {0}): total width ({1} bytes) greater than allowed maximum ({2} bytes)",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * Checks if number of primary key-columns maintained is allowed
	 * 
	 * @return true if number of primary-columns is correct, false otherwise
	 */
	public boolean checkNumberOfColumns() {
		loc.entering("checkNumberOfColumns()");
		boolean check = true;
		int colCount = this.getColumnNames().size();

		if (colCount > DbDb2Parameters.maxKeyColumns) {
			check = false;
			Object[] arguments = { this.getTable().getName(),
					new Integer(colCount),
					new Integer(DbDb2Parameters.maxKeyColumns) };
			cat
					.errorT(
							loc,
							"checkNumberOfColumns (primary key of table {0}): number of columns ({1}) greater than allowed maximum ({2})",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * Compares this primary key to its target version. The database-dependent
	 * comparison is done here, the specific parameters have to be compared in
	 * the dependent part
	 * 
	 * @param target
	 *            the primary key's target version
	 * @return the difference object for this primary key
	 */
	public DbPrimaryKeyDifference compareTo(DbPrimaryKey target)
			throws JddException {
		DbPrimaryKeyDifference difference = super.compareTo(target);
		if (difference != null && difference.getAction() != Action.NOTHING)
			return difference;

		// for V9: need to check whether java.sql.Types.BINARY column
		// in index or primary key will be altered from
		// CHAR FOR BIT DATA (V8 like) to BINARY (V9 like)
		// the index will go into RBP: to avoid this we force a RECREATE here
		// the same applies for numeric types which are changed.
		try {
			Iterator iter = this.getColumnNames().iterator();
			DbColumns columns = this.getTable().getColumns();
			DbColumns targetColumns = target.getTable().getColumns();
			while (iter.hasNext()) {
				String colName = ((DbIndexColumnInfo) iter.next()).getName();
				DbColumn column = columns.getColumn(colName);
				DbColumn targetColumn = targetColumns.getColumn(colName);
				if (column == null) {
					Object[] arguments = { this.getTable().getName(), colName };
					cat
							.errorT(
									loc,
									"compareTo {0}: no such column in original table ( {1} ).",
									arguments);
					throw new JddException();
				}
				if (targetColumn == null) {
					Object[] arguments = { this.getTable().getName(), colName };
					cat
							.errorT(
									loc,
									"compareTo {0}: no such column in target table ( {1} ).",
									arguments);
					throw new JddException();
				}
				if ((((DbDb2Column) column)
						.typeChanged((DbDb2Column) targetColumn))
						&& ((((DbDb2Column) column)
								.isConvFromCharForBitDataToBinary()) || (((DbDb2Column) column)
								.isNumeric()))) {
					difference = this.getDbFactory()
							.makeDbPrimaryKeyDifference(this, target,
									Action.DROP_CREATE);
					return difference;
				}
			}
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}

		return difference;
	}
	/**
	 * Replaces the Db specific parameters of this table (not indexes and
	 * primary key) with those of other table
	 * 
	 * @param other
	 *            an instance of DbTable
	 */
	public void replaceSpecificContent(DbTable other) {
		// nothing yet
		return;
	}

	/**
	 * Checks if Database Specific Parameters of this table and another table
	 * (not indexes and primary key) are the same. True should be delivered if
	 * both table instances have no Database Specific Parameters or if they are
	 * the same or differ in local parameters only. In all other cases false
	 * should be the return value. Local parameters mean those which can not be
	 * maintained in xml but internally only to preserve table properties on
	 * database (such as tablespaces where the table is located) when
	 * drop/create or a conversion takes place.
	 * 
	 * @param other
	 *            an instance of DbTable
	 * @return true - if both table instances have no Database Specific
	 *         Parameters or if they are the same or differ in local parameters
	 *         only.
	 */
	public boolean equalsSpecificContent(DbTable other) {
        //	nothing yet
		return true;
			
	}
}
