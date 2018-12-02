package com.sap.dictionary.database.db2;

import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.PrintWriter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title: Analysis of table and view changes: DB2/390 specific classes
 * Description: DB2/390 specific analysis of table and view changes. Tool to
 * deliver Db2/390 specific database information. Copyright: Copyright (c) 2001
 * Company: SAP AG
 * 
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2Table extends DbTable {

	private DbDb2TsAttr tsAttr = null;

	private String tspName = null;

	private String dbName = null;

	private boolean implicitV9 = false;

	private boolean lobViewMissing = false; // view for lob table if missing

	private String schema = null;

	private static Location loc = Logger.getLocation("db2.DbDb2Table");

	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);
	
	private boolean isV9 = false; 
	private boolean isV8 = true;
	private DbDb2Environment db2Env = null;
	
	public DbDb2Table(DbFactory factory) {
		super(factory);
		setDbEnv(factory);
		setDefaultPartition();
	}

	public DbDb2Table(DbFactory factory, DbSchema schema, String name) {
		super(factory, schema, name);
		setDbEnv(factory);
		setDefaultPartition();
	}

	public DbDb2Table(DbFactory factory, String name) {
		super(factory, name);
		setDbEnv(factory);
		setDefaultPartition();
	}

	public DbDb2Table(DbFactory factory, DbTable other) {
		super(factory, other.getSchema(), other.getName());
		setColumns(other.getColumns());
		setDbEnv(factory);
		setDefaultPartition();
	}

	private void setDefaultPartition() { // $JL-EXC$_
		boolean fexp = false;
		if (tsAttr == null) {
			tsAttr = new DbDb2TsAttr();
			tsAttr.setTable(this);
			tsAttr.setPartitioned(new Boolean(false));
			try {
				tsAttr.setNextPart();
			} catch (Exception e) {
				fexp = true; // avoid lint errors
			}
		}
	}

	private void setDbEnv(DbFactory factory) {
		String schema = null;
		this.db2Env = (DbDb2Environment) factory.getEnvironment();
		
		DbSchema dbschema = getSchema();
		if (dbschema != null)
			schema = dbschema.getSchemaName();
		if (schema == null)
			this.schema = DbDb2Environment.getSchema(factory.getConnection());
		else
			this.schema = schema;
		db2Env.getDb2Paramter().setValues(factory.getConnection());
		this.isV9 = db2Env.isV9(factory.getConnection());
		this.isV8 = db2Env.isV8(factory.getConnection());
	}

	public String getDbSchema() {
		return this.schema;
	}

	public DbDb2TsAttr getTsAttr() {

		return tsAttr;
	}

	public ArrayList dbGetIndexNames(Connection conn) throws JddException {
		loc.entering("dbGetIndexNames");
		if (conn == null) {
			loc.exiting();
			return null;
		}
		String stmt = null;
		try {
			ArrayList names = new ArrayList();
			stmt = "SELECT NAME, UNIQUERULE FROM SYSIBM.SYSINDEXES "
					+ "WHERE TBNAME = ? AND TBCREATOR = ? "
					+ "AND CREATOR = ? " + DbDb2Environment.fetch_only_with_ur;
			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn,
					stmt);
			String tabname = this.getName();
			ps.setString(1, tabname);
			ps.setString(2, schema);
			ps.setString(3, schema);
			ResultSet rset = ps.executeQuery();

			while (rset.next()) {
				// if the index belongs to a primary key
				// do not add it to the indexes
				// the index is created with the primary key
				if (!(0 == rset.getString(2).compareToIgnoreCase("P")))
					names.add(rset.getString(1));
			}
			rset.close();
			ps.close();

			loc.exiting();
			return names;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmt) };
			cat.errorT(loc, "dbGetIndexNames: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "dbGetIndexNames: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

	}

	public void setTableSpecificContentViaDb() throws JddException {

		loc.entering("setTableSpecificContentViaDb()");

		String stmtTxt = null;

		try {

			String tableName = this.getName();
			Connection con = getDbFactory().getConnection();
			
			if (con == null) {
				loc.exiting();
				return;
			}
			boolean dbfound = true;
			
			// Read catalog and set db specific parameters
			stmtTxt = " SELECT A.TSNAME, A.DBNAME, B.PQTY, "
					+ " case when B.secqtyi = 0 then B.sqty else B.secqtyi end, "
					+ " B.FREEPAGE, "
					+ " B.PCTFREE, "
					+ " case when B.GBPCACHE = 'A' then 'ALL' "
					+ "      when B.GBPCACHE = 'N' then 'NONE' "
					+ "      when B.GBPCACHE = 'S' then 'SYSTEM' "
					+ "      else 'CHANGED' end, "
					+ " case when B.space = -1 then 'NO' else 'YES' end, "
					+ " case when B.COMPRESS = 'Y' then 'YES' else 'NO' end, "
					+ " C.BPOOL,"
					+ " C.PGSIZE, "
					+ " case when C.LOCKRULE = 'A' then 'ANY' "
					+ "      when C.LOCKRULE = 'L' then 'LOB' "
					+ "      when C.LOCKRULE = 'P' then 'PAGE' "
					+ "      when C.LOCKRULE = 'R' then 'ROW' "
					+ "      when C.LOCKRULE = 'S' then 'TABLESPACE' "
					+ "      when C.LOCKRULE = 'T' then 'TABLE' "
					+ "      else 'UNDEFINED' end, "
					+ " case when C.CLOSERULE = 'N' then 'NO' "
					+ "      when C.CLOSERULE = 'Y' then 'YES' "
					+ "      else 'UNDEFINED' end, "
					+ " C.SEGSIZE,    "
					+ " C.LOCKMAX,    "
					+ " C.MAXROWS,    "
					// + " C.PARTITIONS, "
					+ " A.PARTKEYCOLNUM, "
					+ " case when C.TYPE ='I' or C.TYPE = 'K' then 'YES' else 'NO' end,"
					+ " case when C.ERASERULE = 'Y' then 'YES' else 'NO' end "
					+ (this.isV9 ? " , C.IMPLICIT " : " ")
					+ " FROM SYSIBM.SYSTABLES A,  SYSIBM.SYSTABLEPART B, "
					+ " SYSIBM.SYSTABLESPACE C " + " WHERE A.NAME = ? "
					+ " AND A.CREATOR = ? " + " AND A.TSNAME =  B.TSNAME "
					+ " AND A.DBNAME =  B.DBNAME " + " AND A.TSNAME =  C.NAME "
					+ " AND A.DBNAME =  C.DBNAME "
					+ " AND (B.PARTITION = 0 OR B.PARTITION = 1) "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
					con, stmtTxt);

			stmt.setString(1, tableName);
			stmt.setString(2, schema);
			ResultSet rs = stmt.executeQuery();

			tsAttr = new DbDb2TsAttr();
			tsAttr.setTable(this);

			if (rs.next()) {
				tspName = rs.getString(1).trim();
				dbName = rs.getString(2).trim();
				this.implicitV9 = (this.isV9 ? (0 == (rs.getString(20).trim())
						.compareTo("Y")) : false);
				tsAttr.setBufferPool(rs.getString(10).trim());
				tsAttr.setPageSize(new Integer(rs.getInt(11)));
				tsAttr.setLockRule(rs.getString(12).trim());
				tsAttr.setClose(rs.getString(13).trim());
				tsAttr.setSegSize(new Integer(rs.getInt(14)));
				tsAttr.setLockMax(new Integer(rs.getInt(15)));
				tsAttr.setMaxRows(new Integer(rs.getInt(16)));
				tsAttr.setMemberCluster(rs.getString(18).trim());
				tsAttr.setErase(rs.getString(19).trim());
				if (rs.getShort(17) > 0 && !this.implicitV9) {
					// partitioned tablespace (not range partitioned!!)
					tsAttr.setPartitioned(new Boolean(true));
				} else
					tsAttr.setPartitioned(new Boolean(false));
			} else {
				dbfound = false;
				tsAttr.setPartitioned(new Boolean(false));
				tsAttr.setNextPart();
				Object[] arguments = { getName() };
				cat.errorT(loc, "Table {0} not found in catalog", arguments);
			}
			rs.close();
			stmt.close();

			if (!dbfound) {
				loc.exiting();
				return;
			}

			if (tsAttr.getPartitioned() == true) {
				// read catalog for partioning key
				stmtTxt = " select name, PARTKEY_COLSEQ from sysibm.syscolumns "
						+ " where tbname = ? and tbcreator = ? "
						+ " and PARTKEY_COLSEQ  > 0 order by PARTKEY_COLSEQ "
						+ DbDb2Environment.fetch_only_with_ur;
				stmt = NativeSQLAccess.prepareNativeStatement(con, stmtTxt);

				stmt.setString(1, tableName);
				stmt.setString(2, schema);
				rs = stmt.executeQuery();
				while (rs.next()) {
					// set key col names
					String keyColName = rs.getString(1).trim();
					tsAttr.setKeyColName(keyColName);
				}
				rs.close();
				stmt.close();
			}

			// Read catalog info of all partitions and set db specific
			// parameters
			stmtTxt = " SELECT PQTY, "
					+ " case when secqtyi = 0 then sqty else secqtyi end, "
					+ " FREEPAGE, " + " PCTFREE, "
					+ " case when GBPCACHE = 'A' then 'ALL' "
					+ "      when GBPCACHE = 'N' then 'NONE' "
					+ "      when GBPCACHE = 'S' then 'SYSTEM' "
					+ "      else 'CHANGED' end, "
					+ " case when space = -1 then 'NO' else 'YES' end, "
					+ " case when COMPRESS = 'Y' then 'YES' else 'NO' end, "
					+ " LIMITKEY " + " FROM SYSIBM.SYSTABLEPART "
					+ " WHERE TSNAME = ? " + " AND DBNAME =  ? "
					+ " AND PARTITION >=  0 " + " ORDER BY PARTITION "
					+ DbDb2Environment.fetch_only_with_ur;

			stmt = NativeSQLAccess.prepareNativeStatement(con, stmtTxt);

			stmt.setString(1, tspName);
			stmt.setString(2, dbName);
			rs = stmt.executeQuery();
			while (rs.next()) {

				DbDb2PartAttr partAttr = tsAttr.setNextPart();
				partAttr.setTsAttr(tsAttr);

				partAttr.setPriQty(new Integer(4 * rs.getInt(1)));
				partAttr.setSecQty(new Integer(4 * rs.getInt(2)));
				partAttr.setFreePage(new Integer(rs.getInt(3)));
				partAttr.setPctFree(new Integer(rs.getInt(4)));
				partAttr.setGbpCache(rs.getString(5).trim());
				partAttr.setDefine(rs.getString(6).trim());
				partAttr.setCompress(rs.getString(7).trim());
				String limitKey = rs.getString(8).trim();
				String limitKeyValues[] = splitLimitKeyValue(rs.getString(8)
						.trim());
				if (tsAttr.getPartitioned() == true) {
					// set key col names
					for (int i = 0; i < limitKeyValues.length
							&& limitKeyValues[i] != null; i++) {
						partAttr.setKeyColValue(limitKeyValues[i]);
					}
				}
			}
			rs.close();
			stmt.close();

			this.setSpecificIsSet(true);

			/*
			 * // set specific content for index corresponding to primary key
			 * DbDb2PrimaryKey primaryKey = (DbDb2PrimaryKey)
			 * this.getPrimaryKey(); if (primaryKey != null)
			 * primaryKey.getIndex().setSpecificContentViaDb(); // set specific
			 * content for secondary indexes DbIndexes indexes = getIndexes();
			 * if (indexes != null) { DbIndexIterator iterator =
			 * indexes.iterator(); while (iterator.hasNext()) { DbIndex index =
			 * iterator.next(); ((DbDb2Index) index).setSpecificContentViaDb(); } }
			 */
			loc.exiting();
			return;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "setTableSpecificContentViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setTableSpecificContentViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	// cannot use string split here because
	// the limitkey values may contain the split character
	// in single quotes
	private String[] splitLimitKeyValue(String limitKeyValue) {
		char splitChar = ',';
		int cnt = 1;

		// count all split chars to (over) estimate for allocation
		for (int fromIndex = 0; fromIndex < limitKeyValue.length(); fromIndex++) {
			fromIndex = limitKeyValue.indexOf(splitChar, fromIndex);
			if (fromIndex < 0)
				break;
			else
				cnt++;
		}

		// initialize
		String[] res = new String[cnt];
		for (int j = 0; j < res.length; j++) {
			res[j] = null;
		}

		boolean protect = false;
		int fromIndex = 0, cntS = 0;
		for (int i = 0; i < limitKeyValue.length(); i++) {
			char c = limitKeyValue.charAt(i);
			if (c == '\'') {
				protect = !protect;
				continue;
			}
			if (protect)
				continue;
			if (c == splitChar) {
				res[cntS++] = limitKeyValue.substring(fromIndex, i);
				fromIndex = i + 1;
			}
		}
		res[cntS++] = limitKeyValue
				.substring(fromIndex, limitKeyValue.length());
		return res;
	}

	public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0)
			throws JddException {

		loc.entering("writeTableSpecificContentToXmlFile");

		try {
			if (tsAttr.getPartitioned() == true) {
				DbDb2PartAttr partAttr = tsAttr.getFirstPart();

				String offset1 = offset0 + XmlHelper.tabulate();
				String offset2 = offset1 + XmlHelper.tabulate();
				String offset3 = offset2 + XmlHelper.tabulate();
				String offset4 = offset3 + XmlHelper.tabulate();
				String offset5 = offset4 + XmlHelper.tabulate();

				if (tsAttr.getPartitioned() == true) {
					file.println(offset0 + "<db2>");
					file.println(offset1 + "<partitioning>");

					file.println(offset2 + "<keycols>");

					file.println(offset3 + "<name>"
							+ tsAttr.getFirstKeyColName() + "</name>");
					String keyColName = null;
					while ((keyColName = tsAttr.getNextKeyColName()) != null) {

						file.println(offset3 + "<name>" + keyColName
								+ "</name>");
					}
					file.println(offset2 + "</keycols>");

					file.println(offset2 + "<parts>");
					do {
						file.println(offset3 + "<part>");

						file.println(offset4 + "<keyvalues>");

						file.println(offset5 + "<keyvalue>"
								+ partAttr.getFirstKeyColValue()
								+ "</keyvalue>");
						String keyValue = null;
						while ((keyValue = partAttr.getNextKeyColValue()) != null) {

							file.println(offset5 + "<keyvalue>" + keyValue
									+ "</keyvalue>");
						}
						file.println(offset4 + "</keyvalues>");

						file.println(offset3 + "</part>");
					} while ((partAttr = tsAttr.getNextPart()) != null);
					file.println(offset2 + "</parts>");

					file.println(offset1 + "</partitioning>");

					file.println(offset0 + "</db2>");
				}
			}

			return;

		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "writeTableSpecificContentToXmlFile: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	} /* writeTableSpecificContentToXmlFile */

	// check if databse exists on db
	private boolean dbExists(String dbname) throws JddException {
		loc.entering("dbExists");
		String stmtTxt = null;
		try {
			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();
			boolean dbExists = false;
			stmtTxt = "SELECT NAME FROM SYSIBM.SYSDATABASE "
					+ " WHERE NAME = ? " + DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement pstmt1 = NativeSQLAccess.prepareNativeStatement(
					con, stmtTxt);
			pstmt1.setString(1, dbname);
			ResultSet rs1 = pstmt1.executeQuery();

			while (rs1.next()) {
				dbExists = true;
			}
			rs1.close();
			pstmt1.close();

			loc.exiting();
			return dbExists;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "dbExists: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "dbExists: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public DbObjectSqlStatements getDdlStatementsForCreate()
			throws JddException {
		loc.entering("getDdlStatementsForCreate");
		try {
			String name = this.getName();
			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();
			
			DbColumns cols = this.getColumns();
			String tabName = getName();
			DbDb2PartAttr partAttr = tsAttr.getFirstPart();
			// set storage attributes
			// use factor 0.7 for calculation
			tsAttr.setPageSizeFactor(0.7);
			// always recalculate bufferpool
			tsAttr.reCalculateBufferPool();

			// variables only used for V8
			boolean hasLobs = false;
			String SapjStogroup = null;
			DbSqlStatement createLineNoCon = null;

			DbObjectSqlStatements tableDef = new DbObjectSqlStatements(
					getName());

			if (!this.isV9 || tsAttr.getPartitioned()) {
				// choose clustering index if not already set
				if (!this.isV9)
					chooseClusteringIndex();

				// check if table has lob columns

				hasLobs = DbDb2Environment.hasLobs(cols);

				// select stogroup:
				// stogroup name (by convention): <schema> or SAPJ
				SapjStogroup = DbDb2Stogroup.getStogroup(con);
				if (SapjStogroup == null) {
					Object[] arguments = { getName(), this.schema };
							
					cat.errorT(loc,
							"getDdlStatementsForCreate {0}: No stogroup for java persistency for schema "
									+ this.schema
									+ " found in catalog", arguments);
					throw new JddException(ExType.NOT_ON_DB,
							"No stogroup for schema "
									+ this.schema );
				}

				boolean dbExists[] = new boolean[1];
				tspName = DbDb2Environment.getTspName(tabName);			
				dbName = DbDb2Environment.getDbName(con, tabName, tspName, tsAttr.getPageSize(),
						schema, dbExists);

				if (hasLobs) {
					DbSqlStatement setLine = new DbDb2SqlStatement();
					setLine.addLine(" SET CURRENT RULES = 'DB2' ");
					tableDef.add(setLine);
				}

				createLineNoCon = new DbDb2SqlStatement();
				DbSqlStatement dbcreateLine = new DbDb2SqlStatement();
				// create database
				if (!dbExists[0] || (con == null)) {
					dbcreateLine.addLine("CREATE DATABASE" + " " + "\""
							+ dbName + "\"");
					dbcreateLine.addLine(" BUFFERPOOL " + tsAttr.getBufferPool() );
					dbcreateLine.addLine(" CCSID " + DbDb2Environment.ccsid);
					if (con == null) {
						dbcreateLine.addLine(" | ");
						createLineNoCon.merge(dbcreateLine);
					} else {
						tableDef.add(dbcreateLine);
						if (DbDb2Parameters.commit)
							tableDef.add(DbDb2Environment.commitLine);
					}
				}

				// create tablespace
				DbSqlStatement tscreateLine = new DbDb2SqlStatement();
				tscreateLine.addLine("CREATE TABLESPACE" + " " + "\"" + tspName
						+ "\"");
				tscreateLine.addLine(" IN " + "\"" + dbName + "\"");
				tscreateLine.addLine(" USING STOGROUP " + SapjStogroup);
				tscreateLine.addLine(" FREEPAGE " + partAttr.getFreePage());
				tscreateLine.addLine(" PCTFREE " + partAttr.getPctFree());
				tscreateLine.addLine(" GBPCACHE " + partAttr.getGbpCache());
				tscreateLine.addLine(" DEFINE " + partAttr.getDefine());
				if (tsAttr.getMemberCluster() != null) {
					tscreateLine.addLine(" " + tsAttr.getMemberCluster() + " ");
				}

				tscreateLine.addLine(" BUFFERPOOL " + tsAttr.getBufferPool());
				tscreateLine.addLine(" LOCKSIZE " + tsAttr.getLockRule());
				tscreateLine.addLine(" LOCKMAX " + tsAttr.getLockMax());
				tscreateLine.addLine(" CLOSE " + tsAttr.getClose());
				tscreateLine.addLine(" COMPRESS " + partAttr.getCompress());
				tscreateLine.addLine(" MAXROWS " + tsAttr.getMaxRows());
				if (tsAttr.getPartitioned()) {
					int i = 0;
					while (partAttr != null) {
						partAttr = tsAttr.getNextPart();
						i++;
					}
					tscreateLine.addLine(" NUMPARTS " + i);
				} else
					tscreateLine.addLine(" SEGSIZE " + tsAttr.getSegSize());
				tscreateLine.addLine(" CCSID " + DbDb2Environment.ccsid);

				if (con == null) {
					tscreateLine.addLine(" | ");
					createLineNoCon.merge(tscreateLine);
				} else {
					tableDef.add(tscreateLine);
					if (DbDb2Parameters.commit)
						tableDef.add(DbDb2Environment.commitLine);
				}
			}

			// create table
			DbSqlStatement createLine = new DbDb2SqlStatement();
			createLine.addLine("CREATE TABLE "
					+ DbDb2Environment.quote(this.getName()));
			createLine.merge(DbDb2Environment.getDdlClause(this, cols));

			if (!this.isV9 || tsAttr.getPartitioned())
				createLine.addLine(" IN " + dbName.trim() + "."
						+ tspName.trim());

			// if table is partitioned, create "partitioning-by-clause"
			if (tsAttr.getPartitioned()) {

				createLine.addLine(" PARTITION BY ( " + tsAttr.getKeyColNames()
						+ " ) ");
				createLine.addLine(" ( ");

				int i = 1;
				partAttr = tsAttr.getFirstPart();
				while (partAttr != null) {
					if (i > 1)
						createLine.addLine(" , ");
					createLine.addLine(" PART " + i + " VALUES ( "
							+ partAttr.getKeyColValues() + " ) ");

					i++;
					partAttr = tsAttr.getNextPart();
				}

				createLine.addLine(" ) ");
			}

			createLine.addLine(" CCSID " + DbDb2Environment.ccsid);

			if (con == null && (!this.isV9 || tsAttr.getPartitioned())) {
				createLine.addLine(" | ");
				createLineNoCon.merge(createLine);
			} else {
				tableDef.add(createLine);
				if (DbDb2Parameters.commit)
					tableDef.add(DbDb2Environment.commitLine);
			}

			// create auxilliary tablespaces for lob columns
			if (hasLobs && !this.isV9) {

				DbColumnIterator iterator = cols.iterator();
				ArrayList tsps = this.db2Env.getTablespacesInDatabase(con,dbName);

				while (iterator.hasNext()) {
					DbColumn col = iterator.next();
					if (DbDb2Environment.isLob(col)) {
						String ltsp = DbDb2Environment.getLobAuxName(dbName,
								tabName, tsps);
						String ltb = db2Env.getAuxTabName(con, col.getName().toUpperCase());
						DbSqlStatement createLobTspLine = new DbDb2SqlStatement();
						createLobTspLine.addLine(" CREATE LOB TABLESPACE "
								+ ltsp + " IN " + dbName);
						createLobTspLine.addLine(" USING STOGROUP "
								+ SapjStogroup);
						createLobTspLine
								.addLine(" LOG YES LOCKMAX 0 GBPCACHE SYSTEM LOCKSIZE LOB "
										+ " DEFINE YES ");
						// lob tablespace can only be created with "DEFINE YES"

						createLobTspLine.addLine(" BUFFERPOOL BP40 ");
						if (con == null) {
							createLobTspLine.addLine(" | ");
							createLineNoCon.merge(createLobTspLine);
						} else {
							tableDef.add(createLobTspLine);
							if (DbDb2Parameters.commit)
								tableDef.add(DbDb2Environment.commitLine);
						}

						DbSqlStatement createAuxTbLine = new DbDb2SqlStatement();
						createAuxTbLine.addLine(" CREATE AUX TABLE "
								+ DbDb2Environment.quote(ltb) + " IN "
								+ dbName.trim() + "." + ltsp.trim());
						createAuxTbLine.addLine(" STORES "
								+ DbDb2Environment.quote(this.getName())
								+ " COLUMN "
								+ DbDb2Environment.quote(col.getName()));
						if (con == null) {
							createAuxTbLine.addLine(" | ");
							createLineNoCon.merge(createAuxTbLine);
						} else
							tableDef.add(createAuxTbLine);
						DbSqlStatement createAuxIndLine = new DbDb2SqlStatement();
						createAuxIndLine.addLine(" CREATE INDEX "
								+ DbDb2Environment.quote(ltb) + " ON  "
								+ DbDb2Environment.quote(ltb));
						createAuxIndLine.addLine(" USING STOGROUP "
								+ SapjStogroup);
						createAuxIndLine
								.addLine(" FREEPAGE 10 PCTFREE 10 GBPCACHE CHANGED PIECESIZE 2097152 K "
										+ " DEFINE YES ");
						// index for auxiliary tables can only be created with
						// "DEFINE YES"
						createAuxIndLine.addLine(" BUFFERPOOL BP40 ");
						if (con == null) {
							createAuxIndLine.addLine(" | ");
							createLineNoCon.merge(createAuxIndLine);
						} else {
							tableDef.add(createAuxIndLine);
							if (DbDb2Parameters.commit)
								tableDef.add(DbDb2Environment.commitLine);
						}
					}
				}
			}

			if (con == null && !this.isV9)
				tableDef.add(createLineNoCon);

			DbIndexes indexes = getIndexes();
			if (indexes != null) {
				tableDef.merge(indexes.getDdlStatementsForCreate());
			}

			DbPrimaryKey primaryKey = getPrimaryKey();
			if (primaryKey != null) {
				tableDef.merge(primaryKey.getDdlStatementsForCreate());
			}
			loc.exiting();
			return tableDef;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat
					.errorT(
							loc,
							"getDdlStatementsForCreate: generation of create statement failed: {0}",
							arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public DbObjectSqlStatements getDdlStatementsForDropLobView() {
		loc.entering("getDdlStatementsForDropLobView");
		String viewName = getName();
		DbObjectSqlStatements dropViewStmts = new DbObjectSqlStatements(
				viewName);
		DbSqlStatement dropLine = new DbDb2SqlStatement(true);
		dropLine.addLine("DROP  VIEW " + DbDb2Environment.quote(viewName));
		dropViewStmts.add(dropLine);
		if (DbDb2Parameters.commit)
			dropViewStmts.add(DbDb2Environment.commitLine);
		loc.exiting();
		return dropViewStmts;
	}

	private int sizeCategoryfromSecQty(int qty) {
		if (qty <= 40)
			return 0;
		if (qty > 40 && qty <= 160)
			return 1;
		if (qty > 160 && qty <= 640)
			return 2;
		if (qty > 640 && qty <= 2560)
			return 3;
		if (qty > 2560 && qty <= 10240)
			return 4;
		if (qty > 10240 && qty <= 20480)
			return 5;
		if (qty > 20480 && qty <= 40960)
			return 6;
		if (qty > 40960 && qty <= 81920)
			return 7;
		if (qty > 81920 && qty <= 163840)
			return 8;
		else
			return 9;
	}

	public void chooseClusteringIndex() {
		loc.entering("chooseClusteringIndex");

		DbDb2PrimaryKey PrimaryKey = (DbDb2PrimaryKey) getPrimaryKey();

		if ((PrimaryKey != null) && (PrimaryKey.getIndex() != null)
				&& PrimaryKey.getIndex().isClustering()) {
			// clustering indicator of indexes already set
			return;
		}

		DbIndexes indexes = getIndexes();
		if (indexes != null) {
			DbIndexIterator iterator = indexes.iterator();

			while (iterator.hasNext()) {
				DbIndex index = iterator.next();
				if (((DbDb2Index) index).isClustering()) {
					// clustering indicator of indexes already set
					return;
				}
			}
		}

		// set primary key to clustering
		// and all other indexes to non clustering
		if (PrimaryKey != null)
			PrimaryKey.getIndex().getIdxAttr().setClustering("YES");

		if (indexes != null) {
			DbIndexIterator iterator = indexes.iterator();
			while (iterator.hasNext()) {
				DbIndex index = iterator.next();
				((DbDb2Index) index).getIdxAttr().setClustering("NO");
			}
		}
		loc.exiting();
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() throws JddException {
		String name = this.getName();
		DbFactory factory = getDbFactory();
		Connection con = factory.getConnection();
		// for partitioned tables we need to drop the tablespace.
		// therefore we need to check whether the table is partitioned 
		// or not.
		if ( !this.specificIsSet() )
			setTableSpecificContentViaDb();
		
		return db2Env.getDdlStatementsForDrop(con, name, schema, this.isV9,
				tsAttr.getPartitioned());
	}

	public void setTableSpecificContentViaXml(XmlMap xmlMap) throws JddException {

		loc.entering("setTableSpecificContentViaXml");

		try {
			/*
			 * we do not check the input values eg.: freepage: if freepage is
			 * greater than segsize DB2 will create the tablespace with freepage =
			 * segsize - 1. If priqty and secqty are not 0 mod 4 DB2 will choose
			 * 4 *(priqty/4) snd so on. ToDo: check/adjust user specified
			 * values.
			 */

			tsAttr = new DbDb2TsAttr();
			tsAttr.setTable(this);
			DbDb2PartAttr partAttr = null;

			// check if table should be partitioned
			XmlMap partitioningXmlMap = xmlMap.getXmlMap("partitioning");

			if (!partitioningXmlMap.isEmpty()) {
				// partitioned table space
				tsAttr.setPartitioned(new Boolean(true));

				// getting dataset size of a partition
				Integer dsSize = xmlMap.getIntegerObject("dssize");
				if (dsSize != null) {
					tsAttr.setDsSize(dsSize);
				} else {
					tsAttr.setDsSize( new Integer( DbDb2Parameters.DEFAULT_DSSIZE) ) ;
				}

				// getting key column names
				XmlMap colsXmlMap = partitioningXmlMap.getXmlMap("keycols");

				if (colsXmlMap.isEmpty()) {

					// invalid partitioning: throw exception
				} else {

					// loop over key columns, to get their names
					String col = null;
					String colName = null;
					for (int i = 0;; i++) {

						col = "name" + ((i == 0) ? "" : "" + i);
						colName = colsXmlMap.getString(col);

						if (colName == null) {
							// all parts processed
							break;
						}

						tsAttr.setKeyColName(colName);
					}
				}

				// now look for part defintions
				XmlMap partsXmlMap = partitioningXmlMap.getXmlMap("parts");

				if (partsXmlMap.isEmpty()) {

					// invalid partitioning: throw exception
				} else {

					// loop over each part. The part names are part, part1,
					// part2 ...
					XmlMap partXmlMap = null;
					String partName = null;

					for (int i = 0;; i++) {

						partName = "part" + ((i == 0) ? "" : "" + i);
						partXmlMap = partsXmlMap.getXmlMap(partName);

						if (partXmlMap.isEmpty()) {
							// all parts processed
							break;
						} else {
							// new part definition
							partAttr = tsAttr.setNextPart();
							partAttr.setTsAttr(tsAttr);
							
							XmlMap keyValuesXmlMap = partXmlMap
									.getXmlMap("keyvalues");
							String keyValueName = null;
							String keyValue = null;
							for (int j = 0;; j++) {

								keyValueName = "keyvalue"
										+ ((j == 0) ? "" : "" + j);
								keyValue = keyValuesXmlMap
										.getString(keyValueName);
								if (keyValue == null) {
									// all key values processed
									break;
								}

								partAttr.setKeyColValue(keyValue);
							}

							partAttr.setDefine(xmlMap.getString("define"));
							partAttr.setCompress(partXmlMap
									.getString("compress"));
							partAttr.setTrackMod(partXmlMap
									.getString("trackmod"));

							setUsingBlockViaXml(partXmlMap, partAttr);
							setFreeBlockViaXml(partXmlMap, partAttr);
							setGbpCacheBlockViaXml(partXmlMap, partAttr);
						}
					}
				}
			} else {
				// non partitioned table
				tsAttr.setPartitioned(new Boolean(false));
				partAttr = tsAttr.setNextPart();
				partAttr.setTsAttr(tsAttr);
				
				setUsingBlockViaXml(xmlMap, partAttr);
				setFreeBlockViaXml(xmlMap, partAttr);
				setGbpCacheBlockViaXml(xmlMap, partAttr);

				partAttr.setDefine(xmlMap.getString("define"));
				partAttr.setTrackMod(xmlMap.getString("trackmod"));
				partAttr.setGbpCache(xmlMap.getString("gbpcache"));
				partAttr.setCompress(xmlMap.getString("compress"));
				tsAttr.setMemberCluster(xmlMap.getString("memclust"));
				tsAttr.setDsSize(xmlMap.getIntegerObject("dssize"));
				tsAttr.setSegSize(xmlMap.getIntegerObject("segsize"));
			}

			// common attributes
			tsAttr.setBufferPool(xmlMap.getString("bufferpool"));
			tsAttr.setLockRule(xmlMap.getString("lockrule"));
			tsAttr.setLockMax(xmlMap.getIntegerObject("lockmax"));
			tsAttr.setPageSize(xmlMap.getIntegerObject("pagesize"));
			tsAttr.setClose(xmlMap.getString("close"));
			tsAttr.setMaxRows(xmlMap.getIntegerObject("maxrows"));

			setSpecificIsSet(true);
			loc.exiting();
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setTableSpecificContentViaXml: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	} /* setTableSpecificContentViaXml */

	private void setUsingBlockViaXml(XmlMap xmlMap, DbDb2PartAttr partAttr) {

		partAttr.setPriQty(xmlMap.getIntegerObject("priqty"));
		partAttr.setSecQty(xmlMap.getIntegerObject("secqty"));
		tsAttr.setErase(xmlMap.getString("erase"));
	}

	private void setFreeBlockViaXml(XmlMap xmlMap, DbDb2PartAttr partAttr) {

		partAttr.setPctFree(xmlMap.getIntegerObject("pctfree"));
		partAttr.setFreePage(xmlMap.getIntegerObject("freepage"));
	}

	private void setGbpCacheBlockViaXml(XmlMap xmlMap, DbDb2PartAttr partAttr) {

		partAttr.setGbpCache(xmlMap.getString("gbpcache"));
	}

	/**
	 * Analyses if table iexists on database or not
	 * 
	 * @return true - if table exists in database, false otherwise
	 * @exception JddException -
	 *                error during analysis detected
	 */
	public boolean existsOnDb() throws JddException {

		loc.entering("existsOnDb");

		String stmtTxt = null;
		boolean exists = false;

		try {

			String name = this.getName();
			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();

			stmtTxt = "SELECT '1' FROM SYSIBM.SYSTABLES WHERE"
					+ " NAME = ? AND CREATOR = ? AND TYPE = ?"
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con,
					stmtTxt);

			ps.setString(1, name);
			ps.setString(2, schema);
			ps.setString(3, "T");

			ResultSet rs = ps.executeQuery();

			exists = (rs.next() == true);

			rs.close();
			ps.close();
		} catch (SQLException sqlex) {

			Object[] arguments = { getName(),
					DbDb2Environment.getSQLError(sqlex, stmtTxt) };
			cat.errorT(loc, "existence check for table {0} failed: {1}",
					arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {

			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "existence check for table {0} failed: {1}",
					arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments = { getName(), exists ? "exists" : "doesn't exist" };
		cat.infoT(loc, "table {0} {1} on db", arguments);
		loc.exiting();

		return exists;
	}

	/**
	 * Analyses if table has content
	 * 
	 * @return true - if table contains at least one record, false otherwise
	 * @exception JddException -
	 *                error during analysis detected
	 */
	public boolean existsData() throws JddException {

		loc.entering("existsData");

		String stmtTxt = null;
		String creatorWithDot = "";
		boolean exists = false;

		if (schema != null) {

			creatorWithDot = DbDb2Environment.quote(schema) + ".";
		}

		try {
			String name = this.getName();
			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();

			stmtTxt = "SELECT '1' FROM " + creatorWithDot
					+ DbDb2Environment.quote(name)
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con,
					stmtTxt);

			ResultSet rs = ps.executeQuery();

			exists = (rs.next() == true);

			rs.close();
			ps.close();
		} catch (SQLException sqlex) {

			Object[] arguments = { getName(),
					DbDb2Environment.getSQLError(sqlex, stmtTxt) };
			cat.errorT(loc, "data existence check for table {0} failed: {1}",
					arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {

			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "data existence check for table {0} failed: {1}",
					arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments = { getName(),
				exists ? "contains" : "doesn't contain" };
		cat.infoT(loc, "table {0} {1} data", arguments);
		loc.exiting();
		return exists;
	}

	public void setIndexesViaDb() throws JddException {
		loc.entering("setIndexesViaDb");
		try {
			ArrayList names = new ArrayList();
			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();
			/* Get index names belonging to this table */
			names = dbGetIndexNames(con);
			if ((names != null) && !names.isEmpty()) {
				DbIndexes indexes = new DbIndexes(factory);
				indexes.setTable(this);
				for (int i = 0; i < names.size(); i++) {
					DbDb2Index index = new DbDb2Index(factory, getName(),
							(String) (names.get(i)));
					// Set parent
					index.setIndexes(indexes);
					index.setCommonContentViaDb();
					index.setSpecificContentViaDb();
					indexes.add(index);
				}
				setIndexes(indexes);
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setIndexesViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setPrimaryKeyViaDb() throws JddException {
		loc.entering("setPrimaryKeyViaDb");
		try {
			Connection con = getDbFactory().getConnection();
			DbDb2Index index = dbGetPrimaryKeyIndex(con);
			if (index != null) {
				DbDb2PrimaryKey primaryKey = new DbDb2PrimaryKey(
						getDbFactory(), getSchema(), this.getName(), index);
				primaryKey.setCommonContentViaDb();
				setPrimaryKey(primaryKey);
			}
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setPrimaryKeyViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public DbDb2Index dbGetPrimaryKeyIndex(Connection con) {
		loc.entering("dbGetPrimaryKeyName");
		if (con == null) {
			loc.exiting();
			return null;
		}
		String stmtTxt = null;
		try {
			String name = null;
			DbDb2Index index = null;
			stmtTxt = "SELECT NAME  " + " FROM  SYSIBM.SYSINDEXES "
					+ " WHERE UNIQUERULE = 'P' " + " AND TBNAME  = ? "
					+ " AND CREATOR = ? " + " AND TBCREATOR = ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
					con, stmtTxt);
			stmt.setString(1, this.getName());
			stmt.setString(2, schema);
			stmt.setString(3, schema);
			ResultSet rset = stmt.executeQuery();
			if (rset.next()) {
				name = rset.getString(1);
				index = new DbDb2Index(this.getDbFactory(), this.getName(),
						name);
				// Set parent
				DbIndexes indexes = new DbIndexes(getDbFactory());
				index.setIndexes(indexes);
				indexes.add(index);
				indexes.setTable(this);

				index.setCommonContentViaDb();
				index.setForPrimaryKey();
			}
			rset.close();
			stmt.close();
			return index;
		} catch (SQLException ex) {
			Object[] arguments = { getName(),
					DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "dbGetPrimaryKeyName ({0}): {1}", arguments);
			loc.exiting();
			return null;
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "dbGetPrimaryKeyName ({0}) failed: {1}", arguments);
			loc.exiting();
			return null;
		}
	}

	public void setSizecatIndexes(int sizecat) {
		// set sizecat for index corresponding to primary key
		loc.entering("setSizecatIndexes");
		DbDb2PrimaryKey primaryKey = (DbDb2PrimaryKey) this.getPrimaryKey();
		if (primaryKey != null)
			primaryKey.getIndex().setTabSizecat(sizecat);

		// set sizecat for secondary indexes
		DbIndexes indexes = getIndexes();
		if (indexes != null) {
			DbIndexIterator iterator = indexes.iterator();

			while (iterator.hasNext()) {
				DbIndex index = iterator.next();
				((DbDb2Index) index).setTabSizecat(sizecat);
			}
		}
		loc.exiting();
	}

	// overwrite method to call db specific setColumnsViaDb
	public void setCommonContentViaDb(DbFactory factory) throws JddException {
		loc.entering("setCommonContentViaDb");
		try {
			setColumnsViaDb(factory);
			setPrimaryKeyViaDb();
			setIndexesViaDb();
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "setCommonContentViaDb ({0}) failed: {1}",
					arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}

	}

	// overwrite method to call db specific setContentViaDb
	public void setColumnsViaDb(DbFactory factory) throws JddException {
		loc.entering("setColumnsViaDb");
		try {
			DbDb2Columns cols = new DbDb2Columns(factory);
			cols.setTable(this);
			cols.setContentViaDb(factory);
			setColumns(cols);
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "setCommonContentViaDb ({0}) failed: {1}",
					arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public boolean checkNameLength() {
		loc.entering("checkNameLength()");
		String tabName = getName();
		boolean check = true;
		if (tabName.length() > DbDb2Parameters.maxTabNameLen) {
			check = false;
			Object[] arguments = { tabName, new Integer(tabName.length()),
					new Integer(DbDb2Parameters.maxTabNameLen) };
			cat
					.errorT(
							loc,
							"checkNameLength {0}: length of table name {1} not in allowed range [1,{2}]",
							arguments);
		}
		loc.exiting();
		return check;
	}

	public boolean checkWidth() {
		loc.entering("checkWidth()");
		boolean check = true;
		DbColumns cols = this.getColumns();

		int rowLength = DbDb2Environment.getRowLength(cols);
		if (rowLength > DbDb2Parameters.maxRowLen) {
			check = false;
			Object[] arguments = { getName(), new Integer(rowLength),
					new Integer(DbDb2Parameters.maxRowLen) };
			cat
					.errorT(
							loc,
							"checkWidth {0}: total width of table ({1} bytes) greater than allowed maximum ({2} bytes)",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * Checks if tablename is a reserved word
	 * 
	 * @return true - if table-name has no conflict with reserved words, false
	 *         otherwise
	 */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");
		boolean isReserved = DbDb2Environment.isReservedWord(this.getName());
		if (isReserved == true) {
			Object[] arguments = { this.getName() };
			cat
					.errorT(loc, "checkNameForReservedWord {0}: reserved",
							arguments);
		}
		loc.exiting();
		return (isReserved == false);
	}

	public DbObjectSqlStatements getDdlStatementsforAlterTsp()
			throws JddException {

		loc.entering("getDdlStatementsForAlterTsp");

		try {

			DbDb2PartAttr partAttr = tsAttr.getFirstPart();

			DbFactory factory = getDbFactory();
			Connection con = factory.getConnection();
			DbObjectSqlStatements tspAlter = new DbObjectSqlStatements(
					getName());

			// select tablespace and database name
			if (tspName == null || dbName == null)
				setTspAndDbName(con);

			// alter tablespace
			DbSqlStatement tspAlterLine = new DbDb2SqlStatement();
			tspAlterLine.addLine(" ALTER TABLESPACE " + dbName + "." + tspName);

			/*
			 * do not alter bufferpool, gbpcache, maxrows because the tablespace
			 * must be stopped first:
			 * 
			 * tspAlterLine.addLine(" BUFFERPOOL " + bufferpool );
			 * tspAlterLine.addLine(" GBPCACHE " + gbpcache );
			 * tspAlterLine.addLine(" MAXROWS " + maxrows );
			 */
			tspAlterLine.addLine(" FREEPAGE " + partAttr.getFreePage());
			tspAlterLine.addLine(" PCTFREE " + partAttr.getPctFree());
			tspAlterLine.addLine(" LOCKSIZE " + tsAttr.getLockRule());
			tspAlterLine.addLine(" LOCKMAX " + tsAttr.getLockMax());
			tspAlterLine.addLine(" CLOSE " + tsAttr.getClose());
			tspAlterLine.addLine(" COMPRESS " + partAttr.getCompress());
			tspAlter.add(tspAlterLine);
			return (tspAlter);
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "getDdlStatementsForAlterTsp ({0}) failed: {1}",
					arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	public void setTspAndDbName(Connection con) throws JddException {
		loc.entering("setTspAndDbName");
		String stmtTxt = null;
		try {
			stmtTxt = "SELECT DBNAME, TSNAME  " + " FROM  SYSIBM.SYSTABLES "
					+ " WHERE NAME = ? AND CREATOR = ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
					con, stmtTxt);
			stmt.setString(1, this.getName());
			stmt.setString(2, schema);
			ResultSet rset = stmt.executeQuery();
			if (rset.next()) {
				dbName = rset.getString(1);
				tspName = rset.getString(2);
			}
			rset.close();
			stmt.close();

			return;
		} catch (SQLException ex) {
			Object[] arguments = { getName(),
					DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "setTspAndDbName ({0}): {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { getName(), ex.getMessage() };
			cat.errorT(loc, "setTspAndDbName ({0}) failed: {1}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/**
	 * get name of table from db: for V7: if table contains lobs we hid the
	 * rowid filed under a view, this view is named like the table in jddi the
	 * name of the table on the db differs
	 */
	// public String getDbTableName() {
	// loc.entering("getDbTableName");
	// String stmtTxt = null;
	// try {
	// if (null != this.dbTableName)
	// return this.dbTableName;
	// String tableName = this.getName();
	// String dbTabName = tableName;
	//			
	//			
	// Connection con = getDbFactory().getConnection();
	// if (con == null)
	// return tableName;
	// String schema = DbDb2Environment.getSchema(con);
	// // evaluate real table name
	// stmtTxt =
	// " SELECT TYPE, NAME FROM SYSIBM.SYSTABLES A "
	// + " WHERE NAME = ? AND TYPE = ?"
	// + " AND A.CREATOR = ? "
	// + DbDb2Environment.optimize_for_one_row
	// + DbDb2Environment.fetch_only_with_ur;
	//
	// PreparedStatement stmt =
	// NativeSQLAccess.prepareNativeStatement(con, stmtTxt);
	//
	// stmt.setString(1, tableName);
	// stmt.setString(2, "T");
	// stmt.setString(3, schema);
	//
	// ResultSet rs = stmt.executeQuery();
	//
	// while (rs.next()) {
	// if (// generic case
	// (0
	// == rs.getString(1).trim().compareTo("T"))
	// && (0 == rs.getString(2).trim().compareTo(tableName))) {
	// lobViewMissing = false;
	// break;
	// } else if (
	// // view on lob table
	// (
	// 0 == rs.getString(1).trim().compareTo("V"))
	// && (0 == rs.getString(2).trim().compareTo(tableName))) {
	// String s = rs.getString(3).trim();
	// if ((s.length() > 0) && (s.charAt(0) == '#')) {
	// dbTabName = s;
	// lobViewMissing = false;
	// break;
	// }
	// } else if (
	// // lob table
	// (0 == rs.getString(1).trim().compareTo("T"))
	// && (0 == rs.getString(2).trim().compareTo(hTabName))) {
	// lobViewMissing = true;
	// dbTabName = hTabName;
	// }
	// }
	// rs.close();
	// stmt.close();
	// this.dbTableName = dbTabName;
	// loc.exiting();
	// return this.dbTableName;
	// } catch (SQLException ex) {
	// Object[] arguments =
	// { getName(), DbDb2Environment.getSQLError(ex, stmtTxt)};
	// cat.errorT(loc,"getDbTableName ({0}): {1}", arguments);
	// loc.exiting();
	// return this.getName();
	// } catch (Exception ex) {
	// Object[] arguments = { getName(), ex.getMessage()};
	// cat.errorT(loc,"getDbTableName ({0}) failed: {1}", arguments);
	// loc.exiting();
	// return this.getName();
	// }
	// }
	public DbTableDifference compareTo(DbTable target) throws Exception {
		loc.entering("compareTo");
		DbTableDifference tableDiff = super.compareTo(target);
		DbDb2TableDifference dbtableDiff = null;
		DbDb2Table dbtarget = (DbDb2Table) target;

		dbtarget.setLobTable(this.getName());

		// set storage attribute use factor 1.0
		// i.e. try to squeeze columns into old pagesize
		// to avoid conversion
		dbtarget.tsAttr.setPageSizeFactor(1.0);
		Connection con = getDbFactory().getConnection();

		if ((null != con) && !this.specificIsSet())
			this.setTableSpecificContentViaDb();

		if (tableDiff == null)
			dbtableDiff = new DbDb2TableDifference(this, target);
		else if (tableDiff instanceof DbDb2TableDifference) {
			dbtableDiff = (DbDb2TableDifference) tableDiff;
		}
		if (dbtableDiff != null) {
			dbtableDiff.diffPageSize(this, (DbDb2Table) target);
			dbtableDiff.diffLobView(this);
			dbtableDiff.diffDbSpecificContent(this, (DbDb2Table) target);
		}

		loc.exiting();
		if ((tableDiff == null)
				&& (dbtableDiff.getTblspAction() == Action.NOTHING)
				&& dbtableDiff.getLobViewMissing() == false)
			return null;
		else if (dbtableDiff != null)
			return ((DbTableDifference) dbtableDiff);
		else
			return (tableDiff);
	}

	public void setLobTable(String lobTableName) {
		DbColumnIterator iterator = this.getColumns().iterator();
		while (iterator.hasNext()) {
			DbDb2Column column = (DbDb2Column) iterator.next();
			if (DbDb2Environment.isLob(column))
				column.setLobTableName(lobTableName);
		}
	}

	public boolean isLobViewMissing() {
		return lobViewMissing;
	}

	/**
	 * Delivers the names of views using this table as basetable
	 * 
	 * @return The names of dependent views as ArrayList
	 * @exception JddException
	 *                error during selection detected
	 */
	public ArrayList getDependentViews() throws JddException {
		loc.entering("getDependentViews");
		ArrayList baseTableNames = new ArrayList();
		Connection con = this.getDbFactory().getConnection();
		if (con == null) {
			loc.exiting();
			return null;
		}
		String stmt = null;
		try {
			ArrayList names = new ArrayList();
			stmt = " with rpl ( level, bname, dname, bcreator, dcreator)   as ( "
					+ " select 0, root.bname , root.dname, root.bcreator, root.dcreator "
					+ " from sysibm.sysviewdep root "
					+ " where bname = ? and bcreator = ? "
					+ " union all "
					+ " select  parent.level+1,"
					+ " child.bname , child.dname, child.bcreator, child.dcreator "
					+ " from rpl parent,  sysibm.sysviewdep child "
					+ " where parent.dname = child.bname "
					+ " and   parent.dcreator = child.bcreator "
					+ " and parent.level < 100 ) "
					+ " select distinct(dname) from rpl group by dname "
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con,
					stmt);
			String viewname = this.getName();
			ps.setString(1, viewname);
			ps.setString(2, schema);
			ResultSet rset = ps.executeQuery();

			while (rset.next()) {
				baseTableNames.add(rset.getString(1));
			}
			rset.close();
			ps.close();
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmt) };
			cat.errorT(loc, "getDependentViews: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "getDependentViews: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		loc.exiting();
		return baseTableNames;
	}

	public boolean getImplicitV9() {
		return implicitV9;
	}

	/**
	 * Replaces the Db specific parameters of this table (not indexes and
	 * primary key) with those of other table
	 * 
	 * @param other
	 *            an instance of DbTable
	 */
	public void replaceTableSpecificContent(DbTable other) {
		loc.entering("replaceTableSpecificContent");
		if (other instanceof DbDb2Table) {
			tsAttr.replace(((DbDb2Table) other).getTsAttr());
		}
		loc.exiting();
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
	public boolean equalsTableSpecificContent(DbTable other) {
		loc.entering("equalsTableSpecificContent");
		boolean dbSpecificContentisEqual = false;
		if (this.specificIsSet() && other.specificIsSet()) {
			if (other instanceof DbDb2Table) {
				dbSpecificContentisEqual = tsAttr
						.equalsSpecificContent(((DbDb2Table) other)
								.getTsAttr());
			}
		} else
			dbSpecificContentisEqual = true;

		loc.exiting();
		return dbSpecificContentisEqual;
	}

	public String toString() {
		String fromSuperClass = super.toString();
		String dbSpecific = "";
		if (tsAttr.getPartitioned()) {
			dbSpecific = "Table partitioned: yes \n";
			dbSpecific += "Partitioning Keys: " + tsAttr.getKeyNames() + "\n";

			DbDb2PartAttr partAttr = tsAttr.getFirstPart();
			int part = 0;
			while (partAttr != null) {
				part++;
				dbSpecific += "Part: " + part + "\n";
				dbSpecific += "Limit Key Value: "
						+ partAttr.getLimitKeyValues() + "\n";
				partAttr = tsAttr.getNextPart();
			}
		}

		return fromSuperClass + dbSpecific;
	}
}