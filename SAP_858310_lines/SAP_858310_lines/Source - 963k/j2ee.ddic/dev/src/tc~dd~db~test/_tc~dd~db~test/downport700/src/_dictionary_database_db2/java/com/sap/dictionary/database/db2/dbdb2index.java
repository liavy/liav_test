package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.PrintWriter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title: Analysis of indexes: DB2/390 specific classes Description: DB2/390
 * specific analysis of index changes. Tool to deliver Db2/390 specific database
 * information. Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2Index extends DbIndex {

	private DbDb2IdxAttr idxAttr;
	private Integer tabSizeCat;
	private Integer sizeCat;
	String schema;
	// flag if index corresponds to primary key
	private boolean forPrimaryKey = false;

	private static Location loc = Logger.getLocation("db2.DbDb2Index");
	private static Category cat = Category.getCategory(Category.SYS_DATABASE,
			Logger.CATEGORY_NAME);
	private boolean isV9 = false;
	private DbDb2Environment db2Env = null;
	
	public DbDb2Index(DbFactory factory) {
		super(factory);
		setDbEnv(factory);
		setSchema(factory);
		setDefaultPartition();
	}

	public DbDb2Index(DbFactory factory, DbIndex other) {
		super(factory, other);
		setDbEnv(factory);
		setSchema(factory);
		setDefaultPartition();
	}

	public DbDb2Index(DbFactory factory, DbSchema schema, String tabname,
			String name) {
		super(factory, schema, tabname, name);
		setDbEnv(factory);
		setSchema(factory);
		setDefaultPartition();
	}

	public DbDb2Index(DbFactory factory, String tabname, String name) {
		super(factory, tabname, name);
		setDbEnv(factory);
		setSchema(factory);
		setDefaultPartition();
	}
	
    private void setDbEnv(DbFactory factory) {
    	db2Env = ((DbDb2Environment) factory.getEnvironment());
    	db2Env.getDb2Paramter().setValues(factory.getConnection());
    	isV9 = db2Env.isV9(factory.getConnection());
    }
	
    private void setDefaultPartition() { // $JL-EXC$_
		boolean fexp = false;
		if (idxAttr == null) {
			idxAttr = new DbDb2IdxAttr();
			try {
				idxAttr.setPart();
			} catch (Exception e) {
				fexp = true; // avoid lint errors
			}
		}
	}

	public DbObjectSqlStatements getDdlStatementsForCreate() {

		loc.entering("getDdlStatementsForCreate");

		DbDb2PartAttr partAttr = idxAttr.getPart();
		if (partAttr == null) {

			// invalid call sequence -> throw exception
		}

		String name = getName();
		String tableName = getIndexes().getTable().getName();

		boolean isUnique = isUnique();

		DbObjectSqlStatements indexDef = new DbObjectSqlStatements(name);
		DbSqlStatement createStatement = new DbDb2SqlStatement();

		DbFactory factory = getDbFactory();
		setSchema(factory);
		Connection con = factory.getConnection();
		
		String SapjStogroup = null;
		if (!this.isV9)
			SapjStogroup = DbDb2Stogroup.getStogroup(con);

		String unique = isUnique ? "UNIQUE " : "";
		createStatement.addLine(" CREATE ");
		createStatement.addLine(unique + " INDEX ");
		createStatement.addLine(DbDb2Environment.quote(name));
		createStatement.addLine(" ON " + DbDb2Environment.quote(tableName)
				+ " ");
		createStatement.merge(getDdlColumnsClause());
		createStatement.addLine(" NOT PADDED ");
		if (!this.isV9)
			createStatement.addLine(" USING STOGROUP " + SapjStogroup);
		createStatement.addLine(" FREEPAGE " + partAttr.getFreePage());
		createStatement.addLine(" PCTFREE " + partAttr.getPctFree());
		createStatement.addLine(" GBPCACHE " + partAttr.getGbpCache());
		createStatement.addLine(" DEFINE " + idxAttr.getDefine());
		if (0 == idxAttr.getClustering().compareToIgnoreCase("YES"))
			createStatement.addLine(" CLUSTER ");
		createStatement.addLine(" BUFFERPOOL " + idxAttr.getBufferPool());
		createStatement.addLine(" CLOSE " + idxAttr.getClose());
		createStatement.addLine(" DEFER " + idxAttr.getDefer());
		createStatement.addLine(" COPY " + idxAttr.getCopy());
		if (idxAttr.getPartitioned() == null)
			createStatement.addLine(" PIECESIZE " + idxAttr.getPieceSize());
		if (idxAttr.getPartitioned() != null) {
			createStatement.addLine(" " + idxAttr.getPartitioned() + " ");
		}
		indexDef.add(createStatement);
		if (DbDb2Parameters.commit)
			indexDef.add(DbDb2Environment.commitLine);
		loc.exiting();
		return indexDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() {
		loc.entering("getDdlStatementsForDrop");
		DbObjectSqlStatements indexDrop = super.getDdlStatementsForDrop();
		if (DbDb2Parameters.commit)
			indexDrop.add(DbDb2Environment.commitLine);
		loc.exiting();
		return indexDrop;
	}

	public DbDb2IdxAttr getIdxAttr() {

		return idxAttr;
	}

	public DbSqlStatement getDdlColumnsClause() {
		loc.entering("getDdlColumnsClause");
		String line = "";
		Iterator iter = getColumnNames().iterator();
		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");
		while (iter.hasNext()) {
			DbIndexColumnInfo dbIndexColumnInfo = (DbIndexColumnInfo) iter
					.next();
			line = DbDb2Environment.quote(dbIndexColumnInfo.getName());
			String idxorder = "";
			if (dbIndexColumnInfo.isDescending())
				idxorder = "DESC";
			else
				idxorder = "ASC";
			line = line + " " + idxorder;
			if (iter.hasNext()) {
				line = line + ", ";
			}
			colDef.addLine(line);
		}
		colDef.addLine(")");
		loc.exiting();
		return colDef;
	}

	/**
	 * Writes the index db specific parameters to the XmlMap
	 */
	public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
			throws JddException {
		loc.entering("writeSpecificContentToXmlFile");
		boolean hiddenAttr = true;
		try {
			if (!hiddenAttr) {

				String offset1 = offset0 + XmlHelper.tabulate();
				String offset2 = offset1 + XmlHelper.tabulate();

				file.println(offset0 + "<db2>");

				DbDb2PartAttr partAttr = idxAttr.getPart();
				if (partAttr == null) {

					// invalid call sequence -> throw exception
				}

				/* storage-attributes */
				file.println(offset1 + "<priqty>" + partAttr.getPriQty()
						+ "</priqty>");
				file.println(offset1 + "<secqty>" + partAttr.getSecQty()
						+ "</secqty>");
				file.println(offset1 + "<pctfree>" + partAttr.getPctFree()
						+ "</pctfree>");
				file.println(offset1 + "<bufferpool>" + idxAttr.getBufferPool()
						+ "</bufferpool>");
				file.println(offset1 + "<freepage>" + partAttr.getFreePage()
						+ "</freepage>");
				file.println(offset1 + "<close>" + idxAttr.getClose()
						+ "</close>");
				file.println(offset1 + "<define>" + idxAttr.getDefine()
						+ "</define>");
				file.println(offset1 + "<gbpcache>" + partAttr.getGbpCache()
						+ "</gbpcache>");
				file.println(offset1 + "<clustering>" + idxAttr.getClustering()
						+ "</clustering>");
				file.println(offset1 + "<defer>" + idxAttr.getDefer()
						+ "</defer>");
				file.println(offset1 + "<piecesize>" + idxAttr.getPieceSize()
						+ "</piecesize>");
				file
						.println(offset1 + "<copy>" + idxAttr.getCopy()
								+ "</copy>");

				file.println(offset0 + "</db2>");
			}
			loc.exiting();
			return;
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "writeSpecificContentToXmlFile failed: {0}",
					arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	} /* writeSpecificContentToXmlFile */

	/**
	 * Reads the index db specific parameters out of the XmlMap and fills the
	 * correspondig variables
	 * 
	 * @param xmlMap
	 *            the index-XmlMap containing the values for the specific
	 *            properties
	 */
	public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {

		loc.entering("setSpecificContentViaXml");

		try {

			if (idxAttr == null) {
				idxAttr = new DbDb2IdxAttr();
			}

			DbDb2PartAttr partAttr = idxAttr.setPart();

			partAttr.setPriQty(xmlMap.getIntegerObject("priqty"));
			partAttr.setSecQty(xmlMap.getIntegerObject("secqty"));
			partAttr.setPctFree(xmlMap.getIntegerObject("pctfree"));
			partAttr.setFreePage(xmlMap.getIntegerObject("freepage"));
			partAttr.setGbpCache(xmlMap.getString("gbpcache"));
			idxAttr.setBufferPool(xmlMap.getString("bufferpool"));
			idxAttr.setDefer(xmlMap.getString("defer"));
			idxAttr.setClose(xmlMap.getString("close"));
			idxAttr.setDefine(xmlMap.getString("define"));
			idxAttr.setPieceSize(xmlMap.getString("piecesize"));
			idxAttr.setClustering(xmlMap.getString("clustering"));
			idxAttr.setCopy(xmlMap.getString("copy"));
			idxAttr.setPartitioned(xmlMap.getString("partitioned"));
		} catch (Exception ex) {

			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setSpecificContentViaXml failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/**
	 * Reads index information from db
	 */
	public void setCommonContentViaDb() throws JddException {
		loc.entering("setCommonContentViaDb");
		String stmtTxt = null;
		try {
			String tabname = getIndexes().getTable().getName();
			String indname = this.getName();
			boolean isUnique = false;
			boolean isDescending = false;
			ArrayList columnsInfo = new ArrayList();
			DbFactory factory = getDbFactory();
			Connection conn = factory.getConnection();
			stmtTxt = "SELECT UNIQUERULE FROM SYSIBM.SYSINDEXES "
					+ "WHERE NAME = ? AND TBNAME = ? "
					+ "AND CREATOR = ? AND TBCREATOR = ? ";
			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn,
					stmtTxt);
			ps.setString(1, indname);
			ps.setString(2, tabname);
			ps.setString(3, schema);
			ps.setString(4, schema);
			ResultSet rset = ps.executeQuery();

			if (rset.next()) {
				isUnique = rset.getString(1).equals("D") ? false : true;
			}
			rset.close();
			ps.close();

			stmtTxt = "SELECT COLNAME, ORDERING FROM SYSIBM.SYSKEYS "
					+ "WHERE IXNAME = ? AND IXCREATOR = ? ORDER BY COLSEQ";
			ps = NativeSQLAccess.prepareNativeStatement(conn, stmtTxt);
			ps.setString(1, indname);
			ps.setString(2, schema);
			rset = ps.executeQuery();

			while (rset.next()) {
				isDescending = rset.getString(2).equals("D") ? true : false;
				DbIndexColumnInfo indexColumnInfo = new DbIndexColumnInfo(rset
						.getString(1), isDescending);
				columnsInfo.add(indexColumnInfo);
			}
			rset.close();
			ps.close();
			setContent(isUnique, columnsInfo);
			loc.exiting();
			return;
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
	}

	/**
	 * copy db specific parameters
	 * 
	 * @param other
	 *            index to be copied
	 */
	public void setSpecificContentViaRef(DbDb2Index other) {
		loc.entering("copySpecificContent()");

		idxAttr = new DbDb2IdxAttr();
		DbDb2PartAttr partAttr = idxAttr.setPart();
		DbDb2IdxAttr otherIdxAttr = other.getIdxAttr();
		DbDb2PartAttr otherPartAttr = otherIdxAttr.getPart();

		partAttr.setPriQty(new Integer(otherPartAttr.getPriQty()));
		partAttr.setSecQty(new Integer(otherPartAttr.getSecQty()));
		partAttr.setFreePage(new Integer(otherPartAttr.getFreePage()));
		;
		partAttr.setPctFree(new Integer(otherPartAttr.getPctFree()));
		partAttr.setGbpCache(new String(otherPartAttr.getGbpCache()));
		idxAttr.setDefine(new String(otherIdxAttr.getDefine()));
		idxAttr.setClustering(new String(otherIdxAttr.getClustering()));
		idxAttr.setDefer(new String(otherIdxAttr.getDefer()));
		idxAttr.setPieceSize(new String(otherIdxAttr.getPieceSize()));
		idxAttr.setCopy(new String(otherIdxAttr.getCopy()));
		idxAttr.setBufferPool(new String(otherIdxAttr.getBufferPool()));
		idxAttr.setClose(new String(otherIdxAttr.getClose()));
		sizeCat = other.sizeCat;
		loc.exiting();
		return;
	}

	/**
	 * Reads db2/390 specific index information from db
	 */
	public void setSpecificContentViaDb() throws JddException {
		loc.entering("setSpecificContentViaDb");
		String stmtTxt = null;
		try {
			String indname = this.getName();

			DbFactory factory = getDbFactory();
			Connection conn = factory.getConnection();
			if (conn == null) {
				loc.exiting();
				return;
			}

			stmtTxt = " SELECT B.PQTY, "
					+ " case when B.secqtyi = 0 then B.sqty else B.secqtyi end, "
					+ " B.FREEPAGE, B.PCTFREE, "
					+ " case when B.GBPCACHE = 'A' then 'ALL' "
					+ "      when B.GBPCACHE = 'N' then 'NONE' "
					+ "      when B.GBPCACHE = 'S' then 'SYSTEM' "
					+ "      else 'CHANGED' end, "
					+ " case when B.space = -1 then 'NO' else 'YES' end, "
					+ " case when A.CLUSTERING = 'Y' then 'YES' else 'NO' end, "
					+ " A.PIECESIZE , "
					+ " case when A.COPY = 'Y' then 'YES' else 'NO' end, "
					+ " A.BPOOL , "
					+ " case when A.CLOSERULE = 'Y' then 'YES' else 'NO' end "
					+ " FROM SYSIBM.SYSINDEXES A, SYSIBM.SYSINDEXPART B "
					+ " WHERE A.NAME = ? " + " AND A.NAME =  B.IXNAME "
					+ " AND A.CREATOR =  ? " + " AND B.IXCREATOR =  ? "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.optimize_for_one_row
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(
					conn, stmtTxt);

			stmt.setString(1, indname);
			stmt.setString(2, schema);
			stmt.setString(3, schema);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				idxAttr = new DbDb2IdxAttr();
				DbDb2PartAttr partAttr = idxAttr.setPart();

				partAttr.setPriQty(new Integer(4 * rs.getInt(1)));
				partAttr.setSecQty(new Integer(4 * rs.getInt(2)));
				partAttr.setFreePage(new Integer(rs.getInt(4)));
				partAttr.setPctFree(new Integer(rs.getInt(4)));
				partAttr.setGbpCache(rs.getString(5).trim());
				idxAttr.setDefine(rs.getString(6).trim());
				idxAttr.setClustering(rs.getString(7).trim());
				// you can not get the 'defer' value from the catalog
				// only via command or stoprog call
				idxAttr.setDefer("NO");
				int pieceSize = rs.getInt(8);
				idxAttr.setPieceSize("" + pieceSize + " K");
				idxAttr.setCopy(rs.getString(9).trim());
				idxAttr.setBufferPool(rs.getString(10).trim());
				idxAttr.setClose(rs.getString(11).trim());
				sizeCat = new Integer(SizeCategoryfromSecQty(partAttr
						.getSecQty()));
			} /* if (rs.next()) */
			else {
				Object[] arguments = { getName() };
				cat.errorT(loc, "Index {0} not found in catalog", arguments);
			}
			rs.close();
			stmt.close();
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmtTxt) };
			cat.errorT(loc, "setSpecificContentViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage() };
			cat.errorT(loc, "setSpecificContentViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		return;
	}

	public void setTabSizecat(int sizecat) {
		this.tabSizeCat = new Integer(sizecat);
	}

	/**
	 * set secondary allocation size from size catagory
	 */
	int secSizeforSizeCategory(int sizeCategory) {

		switch (sizeCategory) {
		case 0:
			return 40;
		case 1:
			return 160;
		case 2:
			return 640;
		case 3:
			return 2540;
		case 4:
			return 10240;
		case 5:
			return 20480;
		case 6:
			return 40960;
		case 7:
			return 81920;
		case 8:
			return 163840;
		default:
			return 327680;
		}
	}

	/**
	 * set index is for primary key
	 */
	public void setForPrimaryKey() {
		forPrimaryKey = true;
	}

	/**
	 * check wether index is for primary key
	 * 
	 * @return true - if index is for primary key
	 */
	public boolean getForPrimaryKey() {
		return forPrimaryKey;
	}

	/**
	 * Check the index's-width
	 * 
	 * @return true - if index-width is o.k
	 */
	public boolean checkWidth() {
		loc.entering("checkWidth()");
		boolean check = true;
		Iterator iter = this.getColumnNames().iterator();
		DbColumns columns = this.getIndexes().getTable().getColumns();
		String tabname = getIndexes().getTable().getName();
		int maxIndexWidth = DbDb2Parameters.maxIndexWidth;
		int rowLength = 0;

		while (iter.hasNext()) {
			String colName = ((DbIndexColumnInfo) iter.next()).getName();
			DbColumn column = columns.getColumn(colName);
			if (column == null) {
				check = false;
				Object[] arguments = { this.getName(), colName };
				cat.errorT(loc,
						"checkWidth {0}: no such column in table ( {1} ).",
						arguments);
				continue;
			} else if (DbDb2Environment.isLob(column)) {
				check = false; // not allowed in index/key
				Object[] arguments = { this.getName(), colName };
				cat
						.errorT(
								loc,
								"checkWidth {0}: column of type LOB ({1}) not allowed in index",
								arguments);
				continue;
			} else {
				int l = DbDb2Environment.getByteLengthIndex(column);
				rowLength += l;
				if (!column.isNotNull()) // add one byte if column nullable
					rowLength += 1;
			}
		}

		if (rowLength > maxIndexWidth) {
			check = false;
			Object[] arguments = { getName(), new Integer(rowLength),
					new Integer(maxIndexWidth) };
			cat
					.errorT(
							loc,
							"checkWidth {0}: total width of index ({1} bytes) greater than allowed maximum ({2} bytes)",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * Check the index's name according to its length
	 * 
	 * @return true - if name-length is o.k
	 */
	public boolean checkNameLength() {
		loc.entering("checkNameLength()");
		String indexName = getName();
		boolean check = true;
		int length = indexName.length();
		if (length < 1 || length > DbDb2Parameters.maxIndexNameLen) {
			check = false;
			Object[] arguments = { indexName, new Integer(length),
					new Integer(DbDb2Parameters.maxIndexNameLen) };
			cat
					.errorT(
							loc,
							"checkNameLength {0}: length of index name ({1}) not in allowed range [1,{2}]",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * Checks if number of index-columns maintained is allowed
	 * 
	 * @return true if number of index-columns is correct, false otherwise
	 */
	public boolean checkNumberOfColumns() {
		loc.entering("checkNumberOfColumns()");
		boolean check = true;
		int colCount = this.getColumnNames().size();

		if (colCount > DbDb2Parameters.maxIndexColumns) {
			check = false;
			Object[] arguments = { getName(), new Integer(colCount),
					new Integer(DbDb2Parameters.maxIndexColumns) };
			cat
					.errorT(
							loc,
							"checkNumberOfColumns {0}: number of index-columns ({1}) greater than allowed maximum ({2})",
							arguments);
		}
		this.setSpecificIsSet(true);
		loc.exiting();
		return check;
	}

	/**
	 * Checks if indexname is a reserved word
	 * 
	 * @return true - if index-name has no conflict with reserved words, false
	 *         otherwise
	 */
	public boolean checkNameForReservedWord() {
		loc.entering("checkNameForReservedWord");
		boolean check = (DbDb2Environment.isReservedWord(this.getName()) == false);
		if (check == false) {
			Object[] arguments = { this.getName() };
			cat
					.errorT(loc, "checkNameForReservedWord {0}: reserved",
							arguments);
		}
		loc.exiting();
		return check;
	}

	/**
	 * set current schema
	 */
	private void setSchema(DbFactory factory) {
		String schema = null;
		DbSchema dbschema = getSchema();
		if (dbschema != null)
			schema = dbschema.getSchemaName();
		if (schema == null) {
			Connection con = factory.getConnection();
			this.schema = db2Env.getSchema(con);
		} else {
			this.schema = schema;
		}
	}

	/**
	 * check wether index is clustering
	 */
	public boolean isClustering() {

		if ((null != idxAttr.getClustering())
				&& (0 == idxAttr.getClustering().compareToIgnoreCase("YES")))
			return true;
		else
			return false;
	}

	/**
	 * get size catagory from secondary allocation size
	 */
	private int SizeCategoryfromSecQty(int qty) {
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

	/**
	 * Analyses if index exists on database or not
	 * 
	 * @return true - if table exists in database, false otherwise
	 * @exception JddException –
	 *                error during analysis detected
	 */
	public boolean existsOnDb() throws JddException {

		loc.entering("existsOnDb");

		boolean exists = false;
		String stmtTxt = null;
		Connection con = getDbFactory().getConnection();

		try {

			stmtTxt = "SELECT '1' FROM SYSIBM.SYSINDEXES WHERE "
					+ "CREATOR = ? AND NAME = ? AND "
					+ "TBCREATOR = ? AND TBNAME = ?";
			PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con,
					stmtTxt);

			ps.setString(1, schema);
			ps.setString(2, getName());
			ps.setString(3, schema);
			ps.setString(4, getIndexes().getTable().getName());

			ResultSet rs = ps.executeQuery();

			exists = (rs.next() == true);

			rs.close();
			ps.close();
		} catch (SQLException sqlex) {

			Object[] arguments = { schema, getName(), getTableName(),
					DbDb2Environment.getSQLError(sqlex, stmtTxt) };
			cat
					.errorT(
							loc,
							"existence check for index {0}.{1} on table {3} failed: {4}",
							arguments);
			loc.exiting();

			throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
		} catch (Exception ex) {

			Object[] arguments = { schema, getName(), getTableName(),
					ex.getMessage() };
			cat
					.errorT(
							loc,
							"existence check for index {0}.{1} on table {3} failed: {4}",
							arguments);
			loc.exiting();

			throw new JddException(ExType.OTHER, ex.getMessage());
		}

		Object[] arguments = { schema, getName(),
				exists ? "exists " : "doesn't exist ", getTableName() };
		cat.infoT(loc, "index {0}.{1} {2} on table {3}", arguments);
		loc.exiting();

		return exists;
	}

	/**
	 * Compares this index to its target version. The database-dependent
	 * comparison is done here, the specific parameters have to be compared in
	 * the dependent part
	 * 
	 * @param target
	 *            the index's target version
	 * @return the difference object for this index
	 */
	public DbIndexDifference compareTo(DbIndex target) throws JddException {
		DbIndexDifference difference = super.compareTo(target);
		if (difference != null && difference.getAction() != Action.NOTHING)
			return difference;

		// for V9: need to check whether java.sql.Types.BINARY column
		// in index or primary key will be altered from
		// CHAR FOR BIT DATA (V8 like) to BINARY (V9 like)
		// the index will go into RBP: to avoid this we force a RECREATE here
		// the same applies for numeric types which are changed.
		try {
			Iterator iter = this.getColumnNames().iterator();
			DbColumns columns = this.getIndexes().getTable().getColumns();
			DbColumns targetColumns = target.getIndexes().getTable()
					.getColumns();

			while (iter.hasNext()) {
				String colName = ((DbIndexColumnInfo) iter.next()).getName();
				DbColumn column = columns.getColumn(colName);
				DbColumn targetColumn = targetColumns.getColumn(colName);
				if (column == null) {
					Object[] arguments = {
							this.getIndexes().getTable().getName(), colName };
					cat
							.errorT(
									loc,
									"compareTo {0}: no such column in original table ( {1} ).",
									arguments);
					throw new JddException();
				}
				if (targetColumn == null) {
					Object[] arguments = {
							this.getIndexes().getTable().getName(), colName };
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
					difference = this.getDbFactory().makeDbIndexDifference(
							this, target, Action.DROP_CREATE);
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
		// nothing yet
		return true;

	}
}