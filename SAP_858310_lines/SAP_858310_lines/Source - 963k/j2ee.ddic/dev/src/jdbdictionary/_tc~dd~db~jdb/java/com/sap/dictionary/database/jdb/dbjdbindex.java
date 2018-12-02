/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbIndex.java#5 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class DbJdbIndex extends DbIndex {
    private static final Location LOCATION = Location.getLocation(DbJdbIndex.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
    static final String UNIQUE_INDEX_DESCRIPTOR_START = "UNIQUE BTREE (";
    static final String INDEX_DESCRIPTOR_START = "BTREE (";

    static List<Integer> parseColumnNumbers(final String descriptor) {
        final String columnNumberString;
        if (descriptor.startsWith(UNIQUE_INDEX_DESCRIPTOR_START)) {
            columnNumberString = descriptor.substring(UNIQUE_INDEX_DESCRIPTOR_START.length());
        } else if (descriptor.startsWith(INDEX_DESCRIPTOR_START)) {
            columnNumberString = descriptor.substring(INDEX_DESCRIPTOR_START.length());
        } else {
            throw new IllegalArgumentException("failed to parse index descriptor: " + descriptor);
        }
        final List<Integer> columnNumbers = new ArrayList<Integer>();
        for (final StringTokenizer tok = new StringTokenizer(columnNumberString, ", )"); tok.hasMoreTokens();) {
            final String columnNumberText = tok.nextToken();
            columnNumbers.add(Integer.valueOf(columnNumberText));
        }
        return columnNumbers;
    }

    public DbJdbIndex() {
        super();
    }

    public DbJdbIndex(DbFactory factory) {
        super(factory);
    }

    public DbJdbIndex(DbFactory factory, DbIndex other) {
        super(factory, other);
    }

    public DbJdbIndex(DbFactory factory, String tableName, String indexName) {
        super(factory, tableName, indexName);
    }

    public DbJdbIndex(DbFactory factory, DbSchema schema, String tableName, String indexName) {
        super(factory, schema, tableName, indexName);
    }

    @Override
    public void setSpecificContentViaXml(XmlMap xmlMap) {
    }

    @Override
    public void setCommonContentViaDb() throws JddException {
        final ArrayList<DbIndexColumnInfo> columnsInfo = new ArrayList<DbIndexColumnInfo>();
        final Connection conn = getDbFactory().getConnection();
        final String descriptor;
        try {
            final String stmtStr = "select g.descriptor from sys.systables t, sys.sysconglomerates g where t.tablename = ? and t.tableid = g.tableid and t.schemaid = g.schemaid and g.isindex and g.conglomeratename = ? and not exists ( select * from sys.syskeys k where k.conglomerateid = g.conglomerateid )";
            final PreparedStatement indexDescriptorStmt = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
            try {
                indexDescriptorStmt.setString(1, getTableName().toUpperCase());
                indexDescriptorStmt.setString(2, getName());
                final ResultSet rs = indexDescriptorStmt.executeQuery();
                try {
                    if (!rs.next()) {
                        return;
                    }
                    descriptor = rs.getString(1);
                } finally {
                    rs.close();
                }
            } finally {
                indexDescriptorStmt.close();
            }
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            CATEGORY.errorT(LOCATION, "setCommonContentViaDb failed: {0}", arguments);
            throw JddException.createInstance(ex);
        }
        final boolean isUnique = descriptor.startsWith(DbJdbIndex.UNIQUE_INDEX_DESCRIPTOR_START);
        final List<Integer> columnNumbers = parseColumnNumbers(descriptor);
        try {
            final String columnNameStr = "select c.columnname from sys.syscolumns c, sys.systables t where t.tablename = ? and c.referenceid = t.tableid and c.columnnumber = ?";
            final PreparedStatement columnNameStmt = NativeSQLAccess.prepareNativeStatement(conn, columnNameStr);
            try {
                columnNameStmt.setString(1, getTableName().toUpperCase());
                for (final Integer columnNumber : columnNumbers) {
                    columnNameStmt.setInt(2, columnNumber.intValue());
                    final ResultSet rs = columnNameStmt.executeQuery();
                    try {
                        if (!rs.next()) {
                            continue;
                        }
                        final String columnName = rs.getString(1);
                        columnsInfo.add(new DbIndexColumnInfo(columnName, false));
                    } finally {
                        rs.close();
                    }
                }
            } finally {
                columnNameStmt.close();
            }
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            CATEGORY.errorT(LOCATION, "setCommonContentViaDb failed: {0}", arguments);
            throw JddException.createInstance(ex);
        }
        setContent(isUnique, columnsInfo);
    }

    @Override
    public void setSpecificContentViaDb() {
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForCreate() {
        DbObjectSqlStatements indexDef = new DbObjectSqlStatements(getName());
        DbSqlStatement createStatement = new DbSqlStatement();
        createStatement.addLine("CREATE " + (isUnique() ? "UNIQUE " : "") + "INDEX \"" + getName() + "\" ON \""
                + getTableName() + "\"");
        createStatement.merge(getDdlColumnsClause());
        indexDef.add(createStatement);
        return indexDef;
    }

    @Override
    public DbSqlStatement getDdlColumnsClause() {
        DbSqlStatement colDef = new DbSqlStatement();
        colDef.addLine("(");
        Iterator icIterator = getColumnNames().iterator();
        DbIndexColumnInfo indColumn = null;
        String sep = ",";
        while (icIterator.hasNext()) {
            indColumn = (DbIndexColumnInfo) icIterator.next();
            if (icIterator.hasNext() == false)
                sep = "";
            if (indColumn.isDescending())
                colDef.addLine("\"" + indColumn.getName() + "\" DESC" + sep);
            else
                colDef.addLine("\"" + indColumn.getName() + "\"" + sep);
        }
        colDef.addLine(")");
        return colDef;
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForDrop() {
        DbObjectSqlStatements dropDef = new DbObjectSqlStatements(getName());
        DbSqlStatement dropLine = new DbSqlStatement(true);
        dropLine.addLine("DROP INDEX \"" + getName() + "\"");
        dropDef.add(dropLine);
        return dropDef;
    }

    @Override
    public boolean checkWidth() {
        // compute length of one entry, compare against maximum (1024)
        Iterator iter = getColumnNames().iterator();
        String colName = null;
        DbColumns columns = getIndexes().getTable().getColumns();
        DbColumn column;
        int total = 0;
        int maxWidth = DbJdbEnvironment.MaxIndexLength();
        while (iter.hasNext()) {
            colName = ((DbIndexColumnInfo) iter.next()).getName();
            column = columns.getColumn(colName);
            if (column == null) {
                Object[] arguments = { getName() };
                CATEGORY.errorT(LOCATION, "checkWidth {0}: no such column in table", arguments);
                return false;
            }
            switch (column.getJavaSqlType()) {
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.LONGVARCHAR:
                Object[] arguments = { getName() };
                CATEGORY.errorT(LOCATION, "checkWidth {0}: index on BLOB, CLOB, LONGVARBINARY or LONGVARCHAR is not allowed",
                        arguments);
                return false; // not allowed in index/key
            }
            total += DbJdbEnvironment.GetColumnLength(column, true);
        }
        if (total > maxWidth) {
            Object[] arguments = { getName(), new Integer(total), new Integer(maxWidth) };
            CATEGORY.errorT(LOCATION, "checkWidth {0}: total width({1}) greater than allowed maximum ({2})", arguments);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkNameLength() {
        int nameLen = getName().length();
        int maxLen = DbJdbEnvironment.MaxNameLength();
        if (nameLen > 0 && nameLen <= maxLen) {
            return true;
        } else {
            Object[] arguments = { getName(), new Integer(nameLen), new Integer(maxLen) };
            CATEGORY.errorT(LOCATION, "checkNameLength {0}: length {1} invalid (allowed range [1..{2}])", arguments);
            return false;
        }
    }

    @Override
    public boolean checkNameForReservedWord() {
        boolean isReserved = DbJdbEnvironment.isReservedWord(getName());
        if (isReserved == true) {
            Object[] arguments = { getName() };
            CATEGORY.errorT(LOCATION, "{0} is a reserved word", arguments);
        }
        return (isReserved == false);
    }

    @Override
    public boolean checkNumberOfColumns() {
        int numCols = getColumnNames().size();
        int maxCols = DbJdbEnvironment.MaxColumnsPerIndex();
        if (numCols <= 0 || numCols > maxCols) {
            Object[] arguments = { getName(), new Integer(numCols), new Integer(maxCols) };
            CATEGORY.errorT(LOCATION, "checkNumberOfColumns{0}: column count {1} not in allowed range [1..{2}]", arguments);
            return false;
        }
        return true;
    }

    @Override
    public boolean existsOnDb() throws JddException {
        boolean exists = false;
        Connection conn = getDbFactory().getConnection();
        String stmtStr = "SELECT 1 FROM DOMAIN.INDEXES WHERE OWNER = USER AND " + "INDEXNAME = ? and TABLENAME = ? ";
        try {
            PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
            ps.setString(1, getName());
            ps.setString(2, getTableName().toUpperCase());
            java.sql.ResultSet rs = ps.executeQuery();
            exists = rs.next() ? true : false;
            rs.close();
            ps.close();
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            CATEGORY.errorT(LOCATION, "existsOnDb failed: {0}", arguments);
            throw JddException.createInstance(ex);
        }
        return exists;
    }
}
