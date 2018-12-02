package com.sap.dictionary.database.mss;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Vector;
import java.util.*;

import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.mss.DbMssIndex;
import com.sap.dictionary.database.mss.DbMssPrimaryKey;
import com.sap.dictionary.database.mss.DbMssEnvironment;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

import java.io.FileWriter;

/**
 * @author d000312
 *
 * MSSQL specific redefinition of DbTable
 */
public class DbMssTable extends DbTable {
	
	private static Location loc = Logger.getLocation("mss.DbMssTable");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	

	public DbMssTable() {
		super();
	}

	public DbMssTable(DbFactory factory) {
		super(factory);
	}

	public DbMssTable(DbFactory factory, String name) {
		super(factory, name);
	}

	public DbMssTable(DbFactory factory, DbSchema schema, String name) {
		super(factory, schema, name);
	}

	public DbMssTable(DbFactory factory, DbTable other) {
		super(factory, other);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#setSpecificContentViaXml(com.sap.dictionary.database.dbs.XmlMap)
	 */
	public void setTableSpecificContentViaXml(XmlMap xmlMap) throws JddException {
		loc.entering("setTableSpecificContentViaXml");
		try {
			if (xmlMap.isEmpty() == false) {
				
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "setTableSpecificContentViaXml failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		loc.exiting();
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#setColumnsViaDb(com.sap.dictionary.database.dbs.DbFactory)
	 * Retrieve column metadata information from database
	 */
	public void setColumnsViaDb(DbFactory factory) throws JddException {
		loc.entering("setColumnsViaDb");
		Connection con = factory.getConnection();
		String name = this.getName();
		String tabname = " ";
		DatabaseMetaData dbmd = null;
		DbColumns columns = null;
		
		String schemaName = null;
		schemaName = retrieveSchemaName(con);

		try {
			columns = factory.makeDbColumns();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"setColumnsViaDb failed on call of factory.makeDbColumns(): {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		try {
			dbmd = NativeSQLAccess.getNativeMetaData(con);	// gd 170303 con.getMetaData();
			
			if (dbmd.storesUpperCaseIdentifiers()) {
				tabname = name.toUpperCase();
			} else if (dbmd.storesLowerCaseIdentifiers()) {
				tabname = name.toLowerCase();
			} else /*if ( dbmd.storesMixedCaseIdentifiers() )*/ {
				tabname = name;
			}
		
			// gd191005 mask wildcards
                        tabname = maskWildcards(tabname);
      			schemaName = maskWildcards(schemaName);

			java.sql.ResultSet rs =
				dbmd.getColumns(null, schemaName, tabname, null);
			while (rs.next()) {
				/* Attention:
				 * Here it is necessary to get the values of the columns of the result
				 * set in the same order as the columns have in the result set.
				 * (Oracle has here a bug).
				 */
				String colName = rs.getString("COLUMN_NAME");
				short sqlType = rs.getShort("DATA_TYPE");
				String dbType = rs.getString("TYPE_NAME");
				int colSize = rs.getInt("COLUMN_SIZE");
				int char_octet_length = rs.getInt("CHAR_OCTET_LENGTH"); /* gd150109 */
				int decDigits = rs.getInt("DECIMAL_DIGITS");
				String defVal = rs.getString("COLUMN_DEF");
				
				/*GD
				System.out.println();
				System.out.println("java.sql.Types.BINARY " + java.sql.Types.BINARY);
				System.out.println("java.sql.Types.VARBINARY " + java.sql.Types.VARBINARY);
				System.out.println("java.sql.Types.LONGVARBINARY " + java.sql.Types.LONGVARBINARY);
				
				System.out.println("COLUMN_NAME " + colName);
				System.out.println("DATA_TYPE " + sqlType);
				System.out.println("TYPE_NAME " + dbType);
				System.out.println("COLUMN_SIZE " + colSize);
				System.out.println("CHAR_OCTET_LENGTH " + char_octet_length);
				System.out.println("DECIMAL_DIGITS " + decDigits);
				System.out.println("COLUMN_DEF " + defVal);
				GD*/
				
				if(dbType.equalsIgnoreCase("nvarchar")
						|| dbType.equalsIgnoreCase("nchar")) {
					/* gd150109
					 * sqljdbc 1.2 returned wrong COLUMN_SIZE 7 for nvarchar(24)
					 * therefore: check with OCTET_LENGTH and correction if necessary
					 */
					if (colSize >= 0 &&
							colSize != char_octet_length / 2) {
						Object[] arguments = { colName, dbType, new Integer(colSize), new Integer(char_octet_length) };
						
						cat.infoT(loc, 
					"setColumnsViaDb DatabaseMetaData: COL_NAME {0} TYPE_NAME {1} COL_SIZE {2} but OCTET_LENGTH {3} -- using OCTET_LENGTH/2 for COL_SIZE",
								arguments);
						colSize = char_octet_length / 2;
					}
				}
				else
				if(dbType.equalsIgnoreCase("varbinary")
						|| dbType.equalsIgnoreCase("binary")) {
					/* gd150109
					 * sqljdbc 1.2 returned wrong COLUMN_SIZE 7 for varbinary(24)
					 * therefore: check with OCTET_LENGTH and correction if necessary
					 */
					if (colSize >= 0 &&
							colSize != char_octet_length) {
						Object[] arguments = { colName, dbType, new Integer(colSize), new Integer(char_octet_length) };
							
						cat.infoT(loc, 
					"setColumnsViaDb DatabaseMetaData: COL_NAME {0} TYPE_NAME {1} COLUMN_SIZE {2} but OCTET_LENGTH {3} -- using OCTET_LENGTH for COL_SIZE",
								arguments);
						colSize = char_octet_length;
					}

					if (dbType.equalsIgnoreCase("varbinary")) {
						sqlType = java.sql.Types.LONGVARBINARY;
					}
				}
				else
				if (dbType.equalsIgnoreCase("float")
					|| dbType.equalsIgnoreCase("int")
					|| dbType.equalsIgnoreCase("integer")
					|| dbType.equalsIgnoreCase("smallint")
					|| dbType.equalsIgnoreCase("image")
					|| dbType.equalsIgnoreCase("text")
					|| dbType.equalsIgnoreCase("ntext")
					|| dbType.equalsIgnoreCase("datetime")) {
					colSize = 0;
					decDigits = 0;
				}

				boolean defaultBound = false;

				if (defVal != null) {
					defVal = defVal.trim();
					String defValUpper = defVal.toUpperCase();
					if (defValUpper.startsWith("CREATE DEFAULT")) {
						int beg = defValUpper.indexOf(" AS ");
						beg += 4; // skip " AS "
						defVal = defVal.substring(beg);
						defVal = defVal.trim();
						defaultBound = true;
					}
				}
				if (defVal != null) {
					boolean charType =
						dbType.endsWith("char") || dbType.endsWith("CHAR");

					while (defVal.startsWith("(") && defVal.endsWith(")")) {
						// eliminate brackets
						defVal =
							defVal.substring(
								defVal.indexOf('(') + 1,
								defVal.lastIndexOf(')'));
					}

					if (charType == true) {
						// in case of character eliminate enclosing quotes
						defVal =
							defVal.substring(
								defVal.indexOf('\'') + 1,
								defVal.lastIndexOf('\''));
					}

					if (dbType.endsWith("binary")
						|| dbType.endsWith("BINARY")) {
						// remove 0x
						defVal = defVal.trim();
						if (defVal.startsWith("0x") || defVal.startsWith("0X"))
							defVal = defVal.substring(2);
						// gd 15.10.01 'normalization'
						defVal = defVal.toUpperCase();
					}

					// gd 15.10.01 'normalization'
					if (dbType.equalsIgnoreCase("float")) {
						try {
							Double v = Double.valueOf(defVal);
							defVal = v.toString();
						} catch (NumberFormatException ex) {
							cat.infoT(loc, "ignoring NumberFormatException");
						}
					} else if (
						dbType.equalsIgnoreCase("integer")
							|| dbType.equalsIgnoreCase("int")
							|| dbType.equalsIgnoreCase("smallint")) {
						try {
							Integer v = Integer.valueOf(defVal);
							defVal = v.toString();
						} catch (NumberFormatException ex) {
							cat.infoT(loc, "ignoring NumberFormatException");
						}
					} else if (dbType.equalsIgnoreCase("datetime")) {
						if (defVal.startsWith("'") && defVal.endsWith("'"))
							defVal = defVal.substring(1, defVal.length() - 1);
					}
				}

				int pos = rs.getInt("ORDINAL_POSITION");
				boolean isNotNull =
					rs.getString("IS_NULLABLE").trim().equalsIgnoreCase("NO")
						? true
						: false;
//				System.out.println("CATALOG " +
//        		"colName|" + colName + "| " +
//        		"dbType|" + dbType + "| " +
//        		"sqlType|" + JavaSqlTypes.getName(sqlType) + "| " +
//        		"colSize|" + colSize + "| " +
//        		"defVal|" + defVal + "| ");
				DbColumn column =
					factory.makeDbColumn(
						colName,
						pos,
						sqlType,
						dbType,
						colSize,
						decDigits,
						isNotNull,
						defVal);

				columns.add(column);
			}

			rs.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage(), System.getProperty("java.io.tmpdir") };
			cat.errorT(loc, "setColumnsViaDb failed: {0} java.io.tmpdir is: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		setColumns(columns);
		loc.exiting();
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#setIndexesViaDb()
	 * Retrieve metadata information to indexes from database
	 */
	public void setIndexesViaDb() throws JddException {
		loc.entering("setIndexesViaDb");

		DbFactory factory = getDbFactory();
		Connection con = factory.getConnection();
		/* Get index names belonging to this table */

		String tabName = this.getName();
		DatabaseMetaData dbmd = null;
		Vector iNames = new Vector();

		String schemaName = null;
		String prefix = null;
		schemaName = retrieveSchemaName(con);
		if (schemaName != null) {
			prefix = "'" + schemaName + "'";
		} else {
			prefix = "user";
		}

		// determine name of primary key
		String pkName = "";
		DbMssPrimaryKey primaryKey = null;

		try {
			Statement pkstmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet pkrs =
				pkstmt.executeQuery(
					"select sopk.name from sysobjects sopk "
						+ "where "
						+ "sopk.parent_obj = object_id(" + prefix + " + '.' + '" + tabName + "') and "
						+ "sopk.xtype = 'PK'");

			if (pkrs.next()) {
				pkName = pkrs.getString(1);
			}
			pkrs.close();
			pkstmt.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"setIndexesViaDb (det. primary key) failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		// list index names
		try {
			dbmd = NativeSQLAccess.getNativeMetaData(con);
			// gd 170303 con.getMetaData();

			java.sql.ResultSet rs =
				dbmd.getIndexInfo(null, null, tabName, false, false);

			String actIndexName = "";
			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");
				if (indexName != null
					&& indexName.equals(actIndexName) == false) {
					if (indexName.equals(pkName) == false)
						iNames.add(indexName);
					actIndexName = indexName;
				}
			}
			rs.close();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"setIndexesViaDb (listing of index names) failed: {0}",
				arguments);
			loc.exiting();
			throw new JddException(ExType.OTHER, ex.getMessage());
		}

                DbMssIndexes dbIndexes = new DbMssIndexes(factory);
		
		if (iNames.isEmpty() == false) {
			for (int i = 0; i < iNames.size(); i++) {
				String indexName = (String) iNames.get(i);
				DbMssIndex dbIndex =
					new DbMssIndex(factory, getSchema(), tabName, indexName);
				/*DbIndex dbIndex = DbFactory.makeIndex(indexName, name);*/
				dbIndex.setCommonContentViaDb();
				dbIndexes.add(dbIndex);
			}
		}

		super.setIndexes(dbIndexes);

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#setPrimaryKeyViaDb()
	 * Retrieve metadata information to primary key from database
	 */
	public void setPrimaryKeyViaDb() throws JddException {
		DbFactory factory = getDbFactory();
		Connection con = getDbFactory().getConnection();

		loc.entering("SetPrimaryKeyViaDb");

		String tabName = this.getName();

		String schemaName = null;
		String prefix = null;
		schemaName = retrieveSchemaName(con);
		if (schemaName != null) {
			prefix = "'" + schemaName + "'";
		} else {
			prefix = "user";
		}

		// primary key
		String pkName = "";
		DbMssPrimaryKey primaryKey = null;

		try {
			Statement pkstmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet pkrs =
				pkstmt.executeQuery(
					"select sopk.name from sysobjects sopk  "
						+ "where "
						+ "sopk.parent_obj = object_id(" + prefix + " + '.' + '" + tabName + "') and "
						+ "sopk.xtype = 'PK'");

			if (pkrs.next()) {
				pkName = pkrs.getString(1);
			}
			pkrs.close();
			pkstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"setPrimaryKeyViaDb (det. of PK name) failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		if (pkName.equals("") == false) {
			primaryKey = new DbMssPrimaryKey(factory, tabName);
			primaryKey.setCommonContentViaDb();

			super.setPrimaryKey(primaryKey);
		} else {
			super.setPrimaryKey(null);
		}

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#getDependentViews()
	 * Delivers the names of views using this table as basetable
	 * @return  the names of dependent views as ArrayList
	 * @exception JddException error during selection detected
	 */
	public ArrayList getDependentViews() throws JddException {
		ArrayList names = new ArrayList();
		Connection con = getDbFactory().getConnection();

		String schemaName = retrieveSchemaName(con);
		String prefix = null;
                if (schemaName != null) {
			prefix = "'" + schemaName + "'";
		} else {
			prefix = "user";
		}

		loc.entering("getDependentViews");

		/* 1. step: drop help table */
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			dstmt.execute("drop table #sap_depobjs");
			dstmt.close();
		} catch (SQLException ex) {
			cat.infoT(loc, "ignoring errors on 'drop table #sap_depobjs'");
		}

		/* 2. step: create help table */
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			dstmt.execute(
				"create table #sap_depobjs "
					+ "( name sysname, "
					+ "  id   int, "
					+ "  lvl  int )");
			dstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"getDependentViews (creation help table) failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		/* 3. step: fill help table */
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			dstmt.execute("declare @lvl int " + "select @lvl = 0 " +

			/* determine all views directly used by this view */
			"insert into #sap_depobjs "
				+ "select distinct so2.name, so2.id, 0 from sysdepends sd, sysobjects so1, sysobjects so2 "
				+ "where so1.id = object_id(" + prefix + " + '.' + '" + this.getName() + "') "
				+ "and   so1.type = 'U' "
				+ "and   so1.id = sd.depid "
				+ "and   sd.id = so2.id "
				+ "and   so2.type = 'V' "
				+ 

			/* determine all views using these views */

			"while @@rowcount > 0 "
				+ "begin "
				+ "select @lvl = @lvl + 1 "
				+ "insert into #sap_depobjs "
				+ "select distinct so.name, so.id, @lvl from sysdepends sd, sysobjects so, #sap_depobjs sv "
				+ "where  sv.lvl = @lvl - 1 "
				+ "and    sd.depid = sv.id "
				+ "and    sd.id = so.id "
				+ "and    so.type = 'V' "
				+ "end");
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"getDependentViews (filling help table) failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		/* 4. step: selecting results */
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			java.sql.ResultSet rs =
				dstmt.executeQuery("select distinct name from #sap_depobjs");

			while (rs.next()) {
				names.add(rs.getString(1));
			}

			rs.close();
			dstmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, 
				"getDependentViews (select results) failed: {0}",
				arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

		/* 5. step: drop help table */
		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			dstmt.execute("drop table #sap_depobjs");
			dstmt.close();
		} catch (SQLException ex) {
			cat.infoT(loc, "ignoring errors on 'drop table #sap_depobjs'");
		}

		loc.exiting();
		return names;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#setTableSpecificContentViaDb()
	 * Retrieve all information for specific content from database
	 */
	public void setTableSpecificContentViaDb() throws JddException {
		loc.entering("setTableSpecificContentViaDb");
	
		//myTrace("setTableSpecificContentViaDb");
		//myTrace("this.getName() " + this.getName());
	
		// nothing yet		

                /*
                // index handling
		// this.getIndexes().setSpecificContentViaDbCatalog(con);

		DbIndexes indexes = this.getIndexes();
		if (indexes != null) {
			DbIndexIterator iterator = indexes.iterator();
			//new DbIndexIterator(first);
			while (iterator.hasNext()) {
				((DbMssIndex) iterator.next()).setSpecificContentViaDb();
			}
		}
                */

		super.setSpecificIsSet(true);

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#writeTableSpecificContentToXmlFile(java.io.PrintWriter, java.lang.String)
	 */
	public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0)
		throws JddException {
		loc.entering("writeTableSpecificContentToXmlFile");
		
		/* omit printing of empty item 
		try {
			file.println(offset0 + "<mss>");

			String offset2 = offset0 + XmlHelper.tabulate();

			// nothing yet

			file.println(offset0 + "</mss>");
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "writeTableSpecificContentToXml failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		*/

		loc.exiting();
		return;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#getDdlStatementsForDrop()
	 * Build DDL statement for dropping of table
	 */
	public DbObjectSqlStatements getDdlStatementsForDrop()
		throws JddException {
		loc.entering("getDdlStatementsForDrop");
		try {
			DbObjectSqlStatements tableDef =
				new DbObjectSqlStatements(this.getName());
			DbSqlStatement dropLine = new DbSqlStatement(true);

			String schemaName = null;
			if (this.getSchema() != null) {
				schemaName = this.getSchema().getSchemaName();
			}

			String userdot = "";
			if (schemaName != null) {
				userdot = schemaName + ".";
			}

			dropLine.addLine(
				"DROP TABLE " + userdot + "[" + this.getName() + "]");
			tableDef.add(dropLine);
			loc.exiting();
			return tableDef;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlStatementsForDrop failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#getDdlStatementsForCreate()
	 * Build DDL statement for creation of table
	 */
	public DbObjectSqlStatements getDdlStatementsForCreate()
		throws JddException {
		loc.entering("getDdlStatementsForCreate");
		try {
			String schemaName = null;
			if (this.getSchema() != null) {
				schemaName = this.getSchema().getSchemaName();
			}

			String userdot = "";
			if (schemaName != null) {
				userdot = schemaName + ".";
			}

			/*
			//Call of generation of simple create statement in independent class. Can be
			//replaced or completed here if necessary
			DbObjectSqlStatements tableDef = super.getDdlStatementsForCreate();
			*/
			DbObjectSqlStatements tableDef =
				new DbObjectSqlStatements(this.getName());
			DbSqlStatement createLine = new DbSqlStatement();

			createLine.addLine(
				"CREATE TABLE " + userdot + "[" + this.getName() + "] ");
			createLine.merge(this.getColumns().getDdlClause());
			tableDef.add(createLine);

			if (this.getPrimaryKey() != null) {
				tableDef.merge(
					this.getPrimaryKey().getDdlStatementsForCreate());
			}

			if (this.getIndexes() != null) {
				tableDef.merge(this.getIndexes().getDdlStatementsForCreate());
			}

			loc.exiting();
			return tableDef;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "getDdlStatementsForCreate failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}


	public DbTableDifference compareTo(DbTable target) throws Exception {
		DbTableDifference difference = super.compareTo(target);
                if (difference != null) {
			Action action = difference.getAction();

			if (action == Action.DROP || action == Action.CREATE || action == Action.CONVERT)
				return difference;

			// myTrace("primarykeyDiff: " + difference.getPrimaryKeyDifference());
			// myTrace("indexesDiff:    " + difference.getIndexesDifference());
			// myTrace("columnsDiff:    " + difference.getColumnsDifference());
		}

		loc.entering("compareTo");
		
		// check: did clustered property move?
		String oldOne = null;
		String newOne = null;
		DbPrimaryKey sourcePrimaryKey = super.getPrimaryKey();
		DbPrimaryKey targetPrimaryKey = target.getPrimaryKey(); 

		if (sourcePrimaryKey != null && ((DbMssPrimaryKey) sourcePrimaryKey).isNonClustered == false) {
			oldOne = "$PK";
		} 
		else {
			DbIndexes sourceIndexes = super.getIndexes();

			if (sourceIndexes != null) {
				DbIndexIterator iterator = sourceIndexes.iterator();
				while (iterator.hasNext()) {
					DbMssIndex ind = (DbMssIndex) iterator.next();
					if (ind.isClustered == true ) {
						oldOne = ind.getName();
						break;
					}
				}
			}
		}

		if (targetPrimaryKey != null && ((DbMssPrimaryKey) targetPrimaryKey).isNonClustered == false) {
			newOne = "$PK";
		} 
		else {
			DbIndexes targetIndexes = target.getIndexes();

			if (targetIndexes != null) {
				DbIndexIterator iterator = targetIndexes.iterator();
				while (iterator.hasNext()) {
					DbMssIndex ind = (DbMssIndex) iterator.next();
					if (ind.isClustered == true ) {
						newOne = ind.getName();
						break;
					}
				}
			}
		}

		if (oldOne != null && newOne != null) {
			if (oldOne.equals(newOne) == false) {
				// clustered property moved from one index/primary key to another
				// ==> let's convert
				Object[] arguments = { getName(), oldOne, newOne };
				cat.infoT(loc, 
					"compareTo ({0}): clustered property moved from {1} to {2}",
					arguments);

				if (difference == null) {
					difference = getDbFactory().makeDbTableDifference(this, target);
				}

				difference.mergeAction(Action.CONVERT); /* gd090109 setAction -> mergeAction */
				loc.exiting();
				return difference;
			}
		}  

		if (difference == null)
			return difference;

		// if a column is changed which participates in an index or primary key
		// in certain cases direct ALTERs are not possible:
		// -- if its type has changed
		// -- if it becomes not-nullable
		// -- if its length decreases
		// -- if it's a decimal column and the definition changes

		DbColumnsDifference colsDiff = difference.getColumnsDifference();
		if (colsDiff == null) {
			// no column changes at all
			// we're done
			loc.exiting();
			return difference;
		}

		DbIndexes sourceIndexes = super.getIndexes();
		
		if (	sourcePrimaryKey == null && 
			(sourceIndexes == null || sourceIndexes.isEmpty() == true)) {
			// no index to check
			loc.exiting();
			return difference;
		}

		// build list with all columns contained in an index
		ArrayList list = new ArrayList();

		// (a) primary key columns
		if (sourcePrimaryKey != null) {
			Iterator icIterator = sourcePrimaryKey.getColumnNames().iterator();
			DbIndexColumnInfo indColumn = null;
			while (icIterator.hasNext() == true) {
				indColumn =  (DbIndexColumnInfo) icIterator.next();
				list.add(indColumn.getName());
			}
		}
	
		// (b) columns of secondary indexes

		if (sourceIndexes != null && sourceIndexes.isEmpty() == false) {
			// i don't have to consider the indexes which get dropped or recreated
			// build exclusion list
			ArrayList droppedIndexes = new ArrayList();
			DbIndexesDifference indexesDiff = difference.getIndexesDifference();

			// myTrace("indexesDiff: " + indexesDiff);

			if (indexesDiff != null && indexesDiff.isEmpty() == false) {
				Iterator idIterator = indexesDiff.iterator();

				while (idIterator.hasNext() == true) {
					DbIndexDifference ixDiff = (DbIndexDifference)idIterator.next();
					Action ixAction = ixDiff.getAction();

					// if (ixDiff.getOrigin() != null)
					// 	myTrace(ixDiff.getOrigin().getName() + " action " + ixAction.toString());
					// else
					//	myTrace(ixDiff.getTarget().getName() + " action " + ixAction.toString());

					if (ixAction == Action.DROP || ixAction == Action.DROP_CREATE) {
						droppedIndexes.add(ixDiff.getOrigin().getName());
					}
				}
			}
			if (droppedIndexes.isEmpty() == true) {
				droppedIndexes = null;
			}
			else {
				Collections.sort(droppedIndexes);
			}

			DbIndexIterator indIterator = sourceIndexes.iterator();
			while (indIterator.hasNext() == true) {
				DbIndex dbIndex = (DbIndex) indIterator.next();
				if (droppedIndexes == null || 
                                    Collections.binarySearch(droppedIndexes, dbIndex.getName()) < 0) {
					// index is not dropped or recreated

					Iterator icIterator = dbIndex.getColumnNames().iterator();
					DbIndexColumnInfo indColumn = null;
					while (icIterator.hasNext() == true) {
						indColumn =  (DbIndexColumnInfo) icIterator.next();
						list.add(indColumn.getName());
					}
				}
			}
		}

		Collections.sort(list);

		// iterate through the changed columns and check the critical ones

		DbColumnsDifference.MultiIterator iterator = colsDiff.iterator();

		while (iterator.hasNextWithModify()) {
			DbColumnDifference colDiff = (DbColumnDifference) iterator.nextWithModify();
			DbColumnDifferencePlan plan = colDiff.getDifferencePlan();

			// if a column's type has changed
			// if a column becomes not-nullable
			// if a column's length decreases
			// if a decimal column's definition changes
			// ... then ALTER is only allowed if the column doesn't participate in an index or
			// primary key
			DbColumn origin = colDiff.getOrigin();
			if (        (plan.typeIsChanged() == true)
				 || (plan.nullabilityIsChanged() == true && origin.isNotNull() == false)
				 || (plan.lengthIsChanged() == true && origin.getLength() > colDiff.getTarget().getLength()) 
				 || (plan.decimalsAreChanged() == true)
	   		) {
				String colName = origin.getName();
				if (Collections.binarySearch(list, colName) >= 0) {
					Object[] arguments = { getName(), colName };
						cat.infoT(loc, 
						"compareTo ({0}): altered column {1} takes part in index/primary key",
						arguments);

					if (difference == null) {
						difference = getDbFactory().makeDbTableDifference(this, target);
					}

					difference.mergeAction(Action.CONVERT); /* gd090109 setAction => mergeAction */
					loc.exiting();
					return difference;
				}
			}
		}	

		loc.exiting();
		return difference;

	}

	public boolean existsOnDb() throws JddException {
		loc.entering("existsOnDb");
		boolean exists = false;
		Connection con = getDbFactory().getConnection();

		String schemaName = retrieveSchemaName(con);
		String prefix = null;
		if (schemaName != null) {
			prefix = "'" + schemaName + "'";
		} else {
			prefix = "user";
		}

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();
			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select 1 from sysobjects "
                                                + "where id = object_id(" + prefix + " + '.' + '" + this.getName() + "') and "
						+ "type = 'U' ");
			exists = (drs.next() == true);
			drs.close();
			dstmt.close();
		} catch (SQLException sqlex) {
			Object[] arguments = { this.getName(), sqlex.getMessage()};
			cat.errorT(loc, "existence check for table {0} failed: {1}", arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {
			Object[] arguments = { this.getName(), ex.getMessage()};
			cat.errorT(loc, "existence check for table {0} failed: {1}", arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments =
			{ this.getName(), exists ? "exists " : "doesn't exist" };
		cat.infoT(loc, "table {0} {1} on db", arguments);
		loc.exiting();
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#existsData()
	 * Check if table contains data
	 */
	public boolean existsData() throws JddException {
		loc.entering("existsData");
		boolean exists = false;
		Connection con = getDbFactory().getConnection();

		String schemaName = retrieveSchemaName(con);

		String userdot = "";
		if (schemaName != null) {
			userdot = schemaName + ".";
		}

		try {
			Statement dstmt = NativeSQLAccess.createNativeStatement(con);
			// gd 170303 con.createStatement();
			java.sql.ResultSet drs =
				dstmt.executeQuery(
					"select top 1 'a' from "
						+ userdot
						+ "["
						+ this.getName()
						+ "] ");
			exists = (drs.next() == true);
			drs.close();
			dstmt.close();
		} catch (SQLException sqlex) {
			Object[] arguments = { this.getName(), sqlex.getMessage()};
			cat.errorT(loc, 
				"data existence check for table {0} failed: {1}",
				arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {
			Object[] arguments = { this.getName(), ex.getMessage()};
			cat.errorT(loc, 
				"data existence check for table {0} failed: {1}",
				arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments =
			{ this.getName(), exists ? "contains" : "doesn't contain" };
		cat.infoT(loc, "table {0} {1} data", arguments);
		loc.exiting();
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#checkNameLength()
	 * Check the table's name according to its length
	 * @return true  if name's length is o.k
	 *         false if length exceeds database limit
	 */
	public boolean checkNameLength() {
		loc.entering("checkNameLength");
		int nameLen = this.getName().length();

		if (nameLen > 0 && nameLen <= 128) {
			loc.exiting();
			return true;
		} else {
			Object[] arguments = { this.getName(), new Integer(nameLen)};
			cat.errorT(loc, "checkNameLength {0}: length {1} invalid", arguments);

			loc.exiting();
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#checkNameForReservedWord()
	 *  Checks if tablename is a reserved word
	 *  @return true  if table name has no conflict with reserved words
	 *          false otherwise
	 */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");
		boolean isReserved = DbMssEnvironment.isReservedWord(this.getName());

		if (isReserved == true) {
			Object[] arguments = { this.getName()};
			cat.errorT(loc, "checkNameForReservedWord {0}: reserved", arguments);
		}

		loc.exiting();
		return (isReserved == false);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbTable#checkWidth()
	 * Check the table-width
	 * @return true  if table-width is o.k.
	 *         false if width exceeds database limit
	 */
	public boolean checkWidth() {
		// compute width of one row in bytes and compare it against maximum (8060 bytes)
		// used formula:
		// table's width = fixlength part + variablelength part + bitmap =
		//                 sum of length of all columns with fix length +
		//                 sum of maximal length of all columns with variable length +
		//                 number of columns with variable length x 2 +
		//                 2 +
		//                 2 +
		//                 ((number of columns + 7) / 8) (null-bitmap)

		loc.entering("checkWidth");

		DbColumns columns = this.getColumns();
		DbColumnIterator iter = columns.iterator();
		DbColumn column;
		boolean check = true;
		int total = 0;
		int colCnt = 0;
		int varCnt = 0;

                /* gd070109 
                   distinguish between SQL server releases 08, 09, 10
                   starting with 09 we consider row-overflow for (n)varchar and varbinary
                 */
		DbMssEnvironment mssEnv = (DbMssEnvironment) getDbFactory().getEnvironment();
		String dbVersion = mssEnv.getDatabaseVersion();
        boolean rowOverflowAllowed = dbVersion.compareTo("09") >= 0;


		while (iter.hasNext()) {
			column = (DbColumn) iter.next();
			colCnt++;

			switch (column.getJavaSqlType()) {
				case java.sql.Types.BLOB :
				case java.sql.Types.CLOB :
				case java.sql.Types.LONGVARBINARY :
				case java.sql.Types.LONGVARCHAR :
					total += 16;
					break;

				case java.sql.Types.BIGINT :
					total += 8;
					break;
				case java.sql.Types.BINARY :
					total += column.getLength();
					break;
				case java.sql.Types.VARBINARY :
					if (rowOverflowAllowed == true &&
					    column.getLength() > 24)
						total += 24; /* only consider the potential pointer */
					else
						total += column.getLength();
					varCnt++;
					break;
				case java.sql.Types.CHAR :
					total += (column.getLength() * 2);
					break;
				case java.sql.Types.VARCHAR :
					if (rowOverflowAllowed == true &&
					    column.getLength() > 12)
						total += 24; /* only consider the potential pointer */
					else
						total += (column.getLength() * 2);
					varCnt++;
					break;
				case java.sql.Types.DATE :
				case java.sql.Types.TIME :
				case java.sql.Types.TIMESTAMP :
					total += 8;
					break;
				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
					long prec = column.getLength();
					if (prec < 10)
						total += 5;
					else if (prec < 20)
						total += 9;
					else if (prec < 29)
						total += 13;
					else
						total += 17;
					break;
				case java.sql.Types.DOUBLE :
				case java.sql.Types.FLOAT :
				case java.sql.Types.REAL :
					total += 8;
					break;
				case java.sql.Types.INTEGER :
					total += 4;
					break;
				case java.sql.Types.SMALLINT :
					total += 4;
					break;
				case java.sql.Types.TINYINT :
					total += 8;
					break;
			}
		}

		total += 2;
		total += 2;
		total += (varCnt * 2);
		total += ((colCnt + 7) / 8);
		if (total > 8060) {
			check = false;

			Object[] arguments = { this.getName(), new Integer(total)};
			cat.errorT(loc, 
				"checkWidth {0}: total width({1}) greater than allowed maximum (8060)",
				arguments);
		}

		loc.exiting();
		return check;
	}

	
	/**
	 *  Check the Db specific Parameters.
         *  @return true - if table parameters are  o.k.
         **/
    	public boolean checkTableSpecificContent() {

		boolean check = true;

		loc.entering("checkWidth");

		// check: there's maximally one index or primary key clustered
		int clusteredCnt = 0;
		DbIndexes indexes = this.getIndexes();

		if (indexes != null) {
			DbIndexIterator iterator = indexes.iterator();
			//new DbIndexIterator(first);
			while (iterator.hasNext()) {
				if (((DbMssIndex) (iterator.next())).isClustered == true)
					clusteredCnt ++;
			}
		}

		DbPrimaryKey primaryKey = this.getPrimaryKey();
		if ((primaryKey != null) && (((DbMssPrimaryKey)primaryKey).isNonClustered == false))
			clusteredCnt ++;

		if (clusteredCnt > 1) {
			check = false;

			
			Object[] arguments = { this.getName() };
			cat.errorT(loc, 
				"checkTableSpecificContent {0}: only one clustered index or primary key allowed",
				arguments);
		}

		// other checks ...		


        	return check;
    	}

	/**
    	 *  Replaces the Db specific parameters of this table (not indexes and
    	 *  primary key) with those of other table
    	 *  @param other   an instance of DbTable
    	 **/   
    	public void mergeDbSpecificContent(DbTable other) {
 
		// nothing (yet)
	   
    	}

	public void replaceTableSpecificContent(DbTable other) {
 
		// nothing (yet)
	   
    	}


        /**
         *  Checks if Database Specific Parameters of this table and another table 
         *  (not indexes and primary key) are the same. True should be delivered if 
         *  both table instances have no Database Specific Parameters or if they are 
         *  the same or differ in local parameters only. 
         *  In all other cases false should be the return value.
         *  Local parameters mean those which can not be maintained in xml but internally
         *  only to preserve table properties on database (such as tablespaces where the table 
         *  is located) when drop/create or a conversion takes place.
         *  @param other   an instance of DbTable
         *  @return true - if both table instances have no Database Specific Parameters or if they are 
         *  the same or differ in local parameters only.  
         **/      
        public boolean equalsTableSpecificContent(DbTable other) {
                return true;  /* no db specifics for tables at this point in time */
        }


	/**
	 * Retrieve the SQL Server release of the connected database
	 * @param con  current connection
	 * @return     SQL Server release (major)
	 * @throws JddException
	 */
	private long retrieveSQLServerRelease(Connection con) {
		long rel = 8;

		try {
			Statement schStmt = NativeSQLAccess.createNativeStatement(con);
			
			java.sql.ResultSet rs = schStmt.executeQuery("select (@@microsoftversion / 65536) / 256");
			rs.next();
			rel = rs.getInt(1);
			rs.close();
			schStmt.close();
		} catch (Exception ex) {
                        loc.entering("retrieveSQLServerRelease");
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc, "retrieveSQLServerRelease failed: {0}", arguments);
			loc.exiting();
		}
		
		return rel;
	}


	/**
	 * Retrieve current schema's name (== user name)
	 * @param con  current connection
	 * @return     user name
	 * errors are ignored
	 */
	private String retrieveSchemaName(Connection con) {
		String schemaName = null;

		if (this.getSchema() != null) {
			schemaName = this.getSchema().getSchemaName();
		} else {
			// determine schema name i'm in
			try {
				Statement schStmt = NativeSQLAccess.createNativeStatement(con);
				// gd 170303 con.createStatement();

				java.sql.ResultSet rs = schStmt.executeQuery("select user");
				rs.next();
				schemaName = rs.getString(1);
				rs.close();
				schStmt.close();
			} catch (SQLException sex) {
				Object[] arguments = { sex.getMessage()};
				cat.errorT(loc, "retrieveSchemaName failed: {0}", arguments);

				SQLException ex = sex;

				while ((sex = sex.getNextException()) != null) {
					Object[] f_arguments = { sex.getMessage()};
					cat.errorT(loc, " {0}", f_arguments);
				}
				loc.exiting();
			} catch (Exception ex) {
                                loc.entering("retrieveSchemaName");
				Object[] arguments = { ex.getMessage()};
				cat.errorT(loc, "retrieveSchemaName failed: {0}", arguments);
				loc.exiting();
			}
		}
		return schemaName;
	}

	/**
  	 *  Mask each wildcard character (_, %, [)  in the <orgName> (<orgName> will be used
         *  in a like-condition but is no like pattern). 
  	 *  @return modified string
   	 **/
        // gd191005 old method replaced by SQL Server specific, driver independent method
	
        private String maskWildcards(String likePattern) {
		final int length = likePattern.length();
		final char[] chars = likePattern.toCharArray();
		final StringBuffer buffer = new StringBuffer(length);

		for (int i = 0; i < length; i++) {
			final char c = chars[i];
			if (c == '_' || c == '%' || c == '[') {
				// this should work for every driver:
				buffer.append("[" + c + "]");
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	public String toString() {
		return super.toString()	+ "\n";
	}

        /*
	private void myTrace(String str) {
		FileWriter out = null;
		
		System.out.println(str);
		try {
			out = new FileWriter("c:/GDDbMssTable.txt", true);
			out.write(str + "\n");
			out.close();
		}
		catch (Exception ex) {
		}
   	}
        */
}