package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import java.sql.*;
import com.sap.tc.logging.*;
import java.util.ArrayList;

/**
 * Ueberschrift: Functions for views. Porting for MySQL 
 * Beschreibung: 
 * Copyright: (c) 2001 Organisation: SAP AG, MySQL AB
 * 
 * @author Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 */

public class DbMysView extends DbView implements DbsConstants {
    private static Location loc = Logger.getLocation("mys.DbMysView");

    private static final Category cat = Category.getCategory(
            Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbMysView() {
        super();
    }

    public DbMysView(DbFactory factory) {
        super(factory);
    }

    public DbMysView(DbFactory factory, String name) {
        super(factory, name);
    }

    public DbMysView(DbFactory factory, DbView other) {
        super(factory, other);
    }

    public DbMysView(DbFactory factory, DbSchema schema, String name) {
        super(factory, schema, name);
    }

    /**
     * Analyses if view exists on database or not
     * 
     * @return true - if table exists on database, false otherwise
     * @exception JddException
     *                error during analysis detected
     */
    public boolean existsOnDb() throws JddException {
        loc.entering("existsOnDb");
        boolean exists = false;
        Connection con = getDbFactory().getConnection();

        try {
            String schema = getDbFactory().getSchemaName();
            
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS " 
                         + "  WHERE TABLE_NAME   = '" + this.getName() + "' " 
                         + "    AND TABLE_SCHEMA = '" + schema + "'";

            ResultSet rs = stmt.executeQuery(query);
            exists = (rs.next() == true);
            rs.close();
            stmt.close();
        } catch (SQLException sqlex) {
            Object[] arguments = { this.getName(), sqlex.getMessage() };
            loc.errorT("existence check for view {0} failed: {1}", arguments);
            loc.exiting();

            throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
        } catch (Exception ex) {
            Object[] arguments = { this.getName(), ex.getMessage() };
            loc.errorT("existence check for view {0} failed: {1}", arguments);
            loc.exiting();

            throw new JddException(ExType.OTHER, ex.getMessage());
        }

        Object[] arguments = { this.getName(), exists ? "exits " : "doesn't exist" };
        loc.infoT("view {0} {1} on db", arguments);
        loc.exiting();
        return exists;
    }

    /**
     * Gets the base table Names of this view from database and sets it for this
     * view
     * 
     * @exception JddException
     *                error during analysis detected
     */
    public void setBaseTableNamesViaDb() throws JddException {
        loc.entering("setBaseTableNamesViaDb");
        ArrayList names = new ArrayList();
        Connection con = getDbFactory().getConnection();

        /* TODO: Implent setBaseTableNamesViaDb().
         * Build arrayList names with the names of the base tables
         */

        setBaseTableNames(names);

        loc.exiting();
        return;
    }

    /**
     * Gets the create statement of this view from the database and sets it to
     * this view with method setCreateStatement
     * 
     * @exception JddException
     *                error during detection detected
     */
    public void setCreateStatementViaDb() throws JddException {
        loc.entering("setCreateStatementViaDb");
        
        Connection con = getDbFactory().getConnection();
        String result = "";

        try {
            String schema = getDbFactory().getSchemaName();

            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            String query =  "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS " 
                          + "  WHERE TABLE_NAME   = '" + this.getName() + "'" 
                          + "    AND TABLE_SCHEMA = '" + schema + "'";
                        
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                result = result + rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            loc.errorT("setCreateStatement failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }

        if (result.length() == 0) {
            result = null;
        }

        setCreateStatement(result);

        loc.exiting();
        return;
    }
}