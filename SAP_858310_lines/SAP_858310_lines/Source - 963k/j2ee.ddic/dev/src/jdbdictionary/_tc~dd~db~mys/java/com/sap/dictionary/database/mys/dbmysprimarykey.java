package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;

import java.util.*;
import java.sql.*;

import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.*;

// enable dictionary on Open SQL connections
// import com.sap.sql.NativeSQLAccess;

/**
 * Ueberschrift:  Primary Key, additional specifics for MySQL
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author        Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysPrimaryKey extends DbPrimaryKey {
    private static Location loc = Logger.getLocation("mys.DbMysPrimaryKey");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

    public DbMysPrimaryKey() {
        super();
    }

    public DbMysPrimaryKey(DbFactory factory, DbPrimaryKey other) {
        super(factory, other);
    }

    public DbMysPrimaryKey(DbFactory factory) {
        super(factory);
    }

    public DbMysPrimaryKey(DbFactory factory, DbSchema schema, String tableName) {
        super(factory, schema, tableName);
    }

    public DbMysPrimaryKey(DbFactory factory, String tableName) {
        super(factory, tableName);
    }

    /**
     * definition of signatures: methods will be impelemented by database groups
     */

    /**
     * Reads the primary key specific parameters out of the XmlMap and fills the
     * corresponding database-dependent variables
     * 
     * @param xmlMap
     *            the primary-key-XmlMap containing the values for the specific
     *            properties
     */
    public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {
        loc.entering("setSpecificContentViaXml");

        try {
            XmlMap storage = xmlMap.getXmlMap("storage-parameters");
            if (storage.isEmpty() == false) {
                // Specific stuff can be set here.
                // Not needed right now. 2005-09-15
            }
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            loc.errorT("setSpecificContentViaXml failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }

        loc.exiting();
        return;
    }

    /**
     * Reads the primary-information from the database and fills the variable
     * columnsInfo. To set these variables the method setContent of the super
     * class can be used
     */
    public void setCommonContentViaDb() throws JddException {
        loc.entering("setCommonContentViaDb");

        DbFactory factory = getDbFactory();
        Connection con    = factory.getConnection();
        
        String pkColumnName;
        ArrayList columnList = new ArrayList();
        
        try {
            String schema = factory.getSchemaName();
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS "
                       + "  WHERE TABLE_SCHEMA = '" + schema + "'"
                       + "    AND TABLE_NAME   = '" + this.getTableName() + "'"
                       + "    AND INDEX_NAME   = 'PRIMARY'";
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                pkColumnName = rs.getString(1);
                columnList.add(new DbIndexColumnInfo(pkColumnName, false));
            }

            rs.close();
            stmt.close();
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }

        setContent(columnList);

        loc.exiting();
        return;
    }

    /**
     * Reads the primary key-specific information from the database and filles
     * the corresponding variables which are database dependent.
     */
    public void setSpecificContentViaDb() throws JddException {
        loc.entering("setSpecificContentViaDb");

        Connection con = getDbFactory().getConnection();

        super.setSpecificIsSet(true);

        loc.exiting();
        return;
    }

    /**
     * produce DDL statement for primary key definition
     */
    public DbObjectSqlStatements getDdlStatementsForCreate() {
        loc.entering("getDdlStatementsForCreate");

        DbObjectSqlStatements pkDef = new DbObjectSqlStatements(this.getTableName()
                                                              + "$PK"); 
        DbSqlStatement createStatement = new DbSqlStatement();

        createStatement.addLine("ALTER TABLE \"" + this.getTableName() + "\" "
                              + "ADD PRIMARY KEY ");
        createStatement.merge(getDdlColumnsClause());
        pkDef.add(createStatement);

        loc.exiting();
        return pkDef;
    }


    /**
     * build statement to drop the primary key
     */
    public DbObjectSqlStatements getDdlStatementsForDrop() {
        loc.entering("getDdlStatementsForDrop");

        DbObjectSqlStatements dropDef = new DbObjectSqlStatements(this.getTableName()
                                                                + "$PK");
        DbSqlStatement dropLine = new DbSqlStatement(true);

        dropLine.addLine("ALTER TABLE \"" + this.getTableName() + "\" "
                       + "DROP PRIMARY KEY");
        dropDef.add(dropLine);

        return dropDef;
    }

    /**
     * Check the primaryKeys's-width
     * 
     * @return true - if primary Key-width is o.k
     */
    public boolean checkWidth() {
        loc.entering("checkWidth");

        // compute length of one entry, compare against maximum (3500)
        Iterator iter = this.getColumnNames().iterator();
        String colName = null;
        DbColumns columns = this.getTable().getColumns();
        DbColumn column;
        boolean check = true;
        int total = 0;

        while (iter.hasNext()) {
            colName = ((DbIndexColumnInfo) iter.next()).getName();
            column = columns.getColumn(colName);
            if (column == null) {
                check = false;
                Object[] arguments = { this.getTableName(), colName };
                loc.errorT("checkWidth (PK to {0}, column {1}): no such column in table",
                           arguments);
                continue;
            }
            switch (column.getJavaSqlType()) {
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.LONGVARCHAR:
                check = false; // not allowed in index/key

                Object[] arguments = { this.getTableName(), colName,
                        column.getJavaSqlTypeName() };
                loc.errorT("checkWidth (PK to {0}, column {1}): type {2} not allowed for primary key",
                           arguments);
                break;
            }
        }

        if (total > 3500) {
            check = false;

            Object[] arguments = { this.getTableName(), new Integer(total) };
            loc.errorT("checkWidth (PK to {0}): total width of primary key ({1}) greater than allowed maximum (3500)",
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
        loc.entering("checkNumberOfColumns");

        int numCols = this.getColumnNames().size();
        boolean check = (numCols > 0 && numCols <= 16);

        if (check == false) {
            Object[] arguments = { this.getTableName(), new Integer(numCols) };
            loc.errorT("checkNumberOfColumns (PK to {0}): column count {1} not in allowed range [1..16]",
                       arguments);
        }
        loc.exiting();
        return check;
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
                loc.errorT("checkColumnsNotNull (PK to {0}): column {1} must not be nullable",
                           arguments);
            }
        }
        loc.exiting();
        return check;
    }
}