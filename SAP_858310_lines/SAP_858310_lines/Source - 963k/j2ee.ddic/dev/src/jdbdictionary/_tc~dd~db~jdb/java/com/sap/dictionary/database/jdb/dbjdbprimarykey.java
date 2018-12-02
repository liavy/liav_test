/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbPrimaryKey.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbJdbPrimaryKey extends DbPrimaryKey {
    private static final Location LOCATION = Location.getLocation(DbJdbPrimaryKey.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
    public DbJdbPrimaryKey() {
    }

    public DbJdbPrimaryKey(DbFactory factory, DbPrimaryKey other) {
        super(factory, other);
    }

    public DbJdbPrimaryKey(DbFactory factory) {
        super(factory);
    }

    public DbJdbPrimaryKey(DbFactory factory, DbSchema schema, String tablename) {
        super(factory, schema, tablename);
    }

    public DbJdbPrimaryKey(DbFactory factory, String tablename) {
        super(factory, tablename);
    }

    @Override
    public void setSpecificContentViaXml(XmlMap xml) {
    }

    @Override
    public void setCommonContentViaDb() throws JddException {
        final Connection conn = getDbFactory().getConnection();
        final String descriptorStr = "select g.descriptor from sys.systables t, sys.sysconstraints c, sys.syskeys k, sys.sysconglomerates g where t.tablename = ? and t.tableid = c.tableid and c.type = 'P' and k.constraintid = c.constraintid and k.conglomerateid = g.conglomerateid";
        final String descriptor;
        try {
            final PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, descriptorStr);
            try {
                ps.setString(1, getTableName().toUpperCase());
                ResultSet rs = ps.executeQuery();
                try {
                    if (!rs.next()) {
                        return;
                    }
                    descriptor = rs.getString(1);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (SQLException ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setCommonContentViaDb", ex);
            throw JddException.createInstance(ex);
        }
        if (!descriptor.startsWith(DbJdbIndex.UNIQUE_INDEX_DESCRIPTOR_START)) {
            CATEGORY.warningT(LOCATION, "setCommonContentViaDb: failed to parse {1}", new Object[] { descriptor, });
            return;
        }
        final List<Integer> columnNumbers = DbJdbIndex.parseColumnNumbers(descriptor);
        final ArrayList<DbIndexColumnInfo> columnList = new ArrayList<DbIndexColumnInfo>();
        final String columnNameStr = "select c.columnname from sys.syscolumns c, sys.systables t where t.tablename = ? and c.referenceid = t.tableid and c.columnnumber = ?";
        try {
            final PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, columnNameStr);
            try {
                ps.setString(1, getTableName().toUpperCase());
                for (final Integer columnNumber : columnNumbers) {
                    ps.setInt(2, columnNumber.intValue());
                    final ResultSet rs = ps.executeQuery();
                    try {
                        if (!rs.next()) {
                            continue;
                        }
                        final String columnName = rs.getString(1);
                        columnList.add(new DbIndexColumnInfo(columnName, false));
                    } finally {
                        rs.close();
                    }
                }
            } finally {
                ps.close();
            }
        } catch (SQLException ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setCommonContentViaDb", ex);
            throw JddException.createInstance(ex);
        }
        setContent(columnList);
    }

    @Override
    public void setSpecificContentViaDb() {
    }

    public void writeSpecificContentToXmlFile() {
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForCreate() {
        String columns = "\"" + ((DbIndexColumnInfo) super.getColumnNames().get(0)).getName().toUpperCase() + "\"";
        for (int column = 1; column < super.getColumnNames().size(); column++)
            columns += ", \"" + ((DbIndexColumnInfo) super.getColumnNames().get(column)).getName().toUpperCase() + "\"";
        DbObjectSqlStatements dbObjectSqlStatements = new DbObjectSqlStatements(getTableName());
        DbSqlStatement dbSqlStatement = new DbSqlStatement();
        dbSqlStatement.addLine("ALTER TABLE \"" + getTableName().toUpperCase() + "\" ");
        dbSqlStatement.addLine("ADD PRIMARY KEY ( " + columns + " ) ");
        dbObjectSqlStatements.add(dbSqlStatement);
        return dbObjectSqlStatements;
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForDrop() {
        DbObjectSqlStatements dbObjectSqlStatements = new DbObjectSqlStatements(getTableName());
        DbSqlStatement dbSqlStatement = new DbSqlStatement(true);
        dbSqlStatement.addLine("ALTER TABLE \"" + getTableName().toUpperCase() + "\" ");
        dbSqlStatement.addLine("DROP PRIMARY KEY");
        dbObjectSqlStatements.add(dbSqlStatement);
        return dbObjectSqlStatements;
    }

    @Override
    public boolean checkWidth() {
        // compute length of one entry, compare against maximum (1024)
        Iterator iter = getColumnNames().iterator();
        String colName = null;
        DbColumns columns = getTable().getColumns();
        DbColumn column;
        int total = 0;
        int maxWidth = DbJdbEnvironment.MaxKeyLength();
        while (iter.hasNext()) {
            colName = ((DbIndexColumnInfo) iter.next()).getName();
            column = columns.getColumn(colName);
            if (column == null) {
                Object[] arguments = { getTableName() };
                CATEGORY.errorT(LOCATION, "checkWidth {0}: no such column in table", arguments);
                return false;
            }
            switch (column.getJavaSqlType()) {
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.LONGVARCHAR:
                Object[] arguments = { getTableName() };
                CATEGORY.errorT(LOCATION, "checkWidth {0}: index on BLOB, CLOB, LONGVARBINARY or LONGVARCHAR is not allowed",
                        arguments);
                return false; // not allowed in index/key
            }
            total += DbJdbEnvironment.GetColumnLength(column, true);
        }
        if (total > maxWidth) {
            Object[] arguments = { getTableName(), new Integer(total), new Integer(maxWidth) };
            CATEGORY.errorT(LOCATION, "checkWidth {0}: total width({1}) greater than allowed maximum ({2})", arguments);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkNumberOfColumns() {
        int numCols = getColumnNames().size();
        int maxCols = DbJdbEnvironment.MaxKeysPerTable();
        if (numCols <= 0 || numCols > maxCols) {
            Object[] arguments = { getTableName(), new Integer(numCols), new Integer(maxCols) };
            CATEGORY.errorT(LOCATION, "checkNumberOfColumns{0}: column count {1} not in allowed range [1..{2}]", arguments);
            return false;
        }
        return true;
    }
}
