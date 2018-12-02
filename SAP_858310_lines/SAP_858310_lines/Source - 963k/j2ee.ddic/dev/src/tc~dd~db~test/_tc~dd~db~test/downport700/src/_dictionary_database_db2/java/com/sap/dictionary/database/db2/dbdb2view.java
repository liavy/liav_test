package com.sap.dictionary.database.db2;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDb2View extends DbView implements DbsConstants {
	private DbFactory factory = null;
	private DbSchema dbschema = null;
	private String name = " ";
	private String schema = null;
	private String createStatement = null;
	private static Location loc = Logger.getLocation("db2.DbDb2View");
	private static Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private DbDb2Environment db2Env = null;
	
	public DbDb2View() {
		super();
	}

	public DbDb2View(DbFactory factory) {
		super(factory);
		this.factory = factory;
		setDbEnv(factory);
	}

	public DbDb2View(DbFactory factory, String name) {
		super(factory, name);
		this.factory = factory;
		this.name = name;
		setDbEnv(factory);
	}

	public DbDb2View(DbFactory factory, DbView other) {
		super(factory, other);
		this.factory = factory;
		setDbEnv(factory);
	}

	public DbDb2View(DbFactory factory, DbSchema schema, String name) {
		super(factory, schema, name);
		this.factory = factory;
		this.dbschema = schema;
		this.name = name;
		setDbEnv(factory);
	}

	/**
	   *  Analyses if view exists on database or not
	   *  @return true - if table exists in database, false otherwise
	   *  @exception JddException � error during analysis detected	 
	   **/
	public boolean existsOnDb() throws JddException {
		loc.entering("existsOnDb");
		boolean exists = false;
		Connection con = this.getDbFactory().getConnection();
		if (con == null) {
			loc.exiting();
			return false;
		}
		String stmt = null;
		try {
			stmt =
				"SELECT NAME FROM SYSIBM.SYSVIEWS "
					+ "WHERE NAME = ? AND CREATOR = ? "
					+ " AND SEQNO = 1 "
					+ DbDb2Environment.fetch_first_row
					+ DbDb2Environment.fetch_only_with_ur;

			PreparedStatement ps =
				NativeSQLAccess.prepareNativeStatement(con, stmt);
			String viewname = this.getName();
			ps.setString(1, viewname);
			ps.setString(2, schema);
			ResultSet rset = ps.executeQuery();
			if (rset.next()) {
				exists = true;
			}
			rset.close();
			ps.close();
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmt)};
			cat.errorT(loc,"existsOnDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc,"existsOnDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		loc.exiting();
		return exists;
	}

	/**
	*  Gets the base table names of this view from database and sets it 
	*  for this view with method setBaseTableNames
	*  @exception JddException � error during analysis detected	 
	**/
	public void setBasetableNamesViaDb() throws JddException {
		loc.entering("setBasetableNamesViaDb");
		ArrayList baseTableNames = new ArrayList();
		Connection con = factory.getConnection();
		if (con == null) {
			loc.exiting();
			return;
		}
		String stmt = null;
		try {
			ArrayList names = new ArrayList();
			stmt =
				" with rpl ( level, bname, dname, bcreator, dcreator, btype)   as ( "
					+ " select 0, root.bname , root.dname, root.bcreator, root.dcreator, root.btype "
					+ " from sysibm.sysviewdep root where dname = ?  and dcreator = ? "
					+ " union all select  child.level+1, "
					+ " parent.bname , parent.dname, parent.bcreator, parent.dcreator, parent.btype "
					+ " from rpl child ,  sysibm.sysviewdep parent "
					+ " where child.btype = 'V' and   parent.dname = child.bname "
					+ " and   parent.dcreator = child.bcreator and child.level < 100 ) "
					+ " select distinct(bname) from rpl where btype = 'T' group by bname "
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement ps =
				NativeSQLAccess.prepareNativeStatement(con, stmt);
			ps.setString(1, this.name);
			ps.setString(2, schema);
			ResultSet rset = ps.executeQuery();

			while (rset.next()) {
				baseTableNames.add(rset.getString(1));
			}
			rset.close();
			ps.close();
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmt)};
			cat.errorT(loc,"setBasetableNamesViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc,"setBasetableNamesViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		setBaseTableNames(baseTableNames);
		loc.exiting();
	}

	/**
		*  Gets the create statement of this view from the database and 
		*  sets it to this view with method setCreateStatement
		*  @exception JddException � error during detection detected	 
		**/
	public void setCreateStatementViaDb() throws JddException {
		String createStatement = "";
		loc.entering("setCreateStatementViaDb");
		Connection con = factory.getConnection();
		if (con == null) {
			loc.exiting();
			return;
		}
		String stmt = null;
		try {
			ArrayList names = new ArrayList();
			stmt =
				"SELECT TEXT FROM SYSIBM.SYSVIEWS "
					+ "WHERE NAME = ? AND CREATOR = ? "
					+ " ORDER BY SEQNO "
					+ DbDb2Environment.fetch_only_with_ur;
			PreparedStatement ps =
				NativeSQLAccess.prepareNativeStatement(con, stmt);
			ps.setString(1, this.name);
			ps.setString(2, schema);
			ResultSet rset = ps.executeQuery();

			while (rset.next()) {
				String s = rset.getString(1);
				createStatement += rset.getString(1) + " ";

			}
			rset.close();
			ps.close();
		} catch (SQLException ex) {
			Object[] arguments = { DbDb2Environment.getSQLError(ex, stmt)};
			cat.errorT(loc,"setCreateStatementViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		} catch (Exception ex) {
			Object[] arguments = { ex.getMessage()};
			cat.errorT(loc,"setCreateStatementViaDb: {0}", arguments);
			loc.exiting();
			throw JddException.createInstance(ex);
		}
		setCreateStatement(createStatement);
		loc.exiting();
	}

	private void setDbEnv(DbFactory factory) {
		this.db2Env = (DbDb2Environment) factory.getEnvironment();
		if (this.schema == null)
			this.schema = db2Env.getSchema(factory.getConnection());
	}
}
