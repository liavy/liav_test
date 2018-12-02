/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbTable.java#6 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbJdbTable extends DbTable {
    private static final Location LOCATION = Location.getLocation(DbJdbTable.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbJdbTable() {
        super();
    }

    public DbJdbTable(DbFactory factory) {
        super(factory);
    }

    public DbJdbTable(DbFactory factory, String name) {
        super(factory, name);
    }

    public DbJdbTable(DbFactory factory, DbSchema schema, String name) {
        super(factory, schema, name);
    }

    public DbJdbTable(DbFactory factory, DbTable other) {
        super(factory, other);
    }

    @Override
    public void setTableSpecificContentViaXml(XmlMap xmlMap) {
    }

    @Override
    public void setTableSpecificContentViaDb() {
    }

    @Override
    public void setColumnsViaDb(DbFactory factory) throws JddException {
        try {
            DbJdbColumns cols = new DbJdbColumns(factory);
            cols.setTable(this);
            cols.setContentViaDb(factory);
            setColumns(cols);
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setColumnsViaDb failed", ex);
            throw JddException.createInstance(ex);
        }
    }

    @Override
    public void setIndexesViaDb() throws JddException {
        DbFactory factory = getDbFactory();
        Connection con = factory.getConnection();
        try {
            /* Get index names belonging to this table */
            final List<String> names = dbGetIndexNames(con);
            if (!names.isEmpty()) {
                DbIndexes indexes = new DbIndexes(factory);
                for (final String indexName : names) {
                    DbJdbIndex index = new DbJdbIndex(factory, getSchema(), getName(), indexName);
                    // Set parent
                    index.setIndexes(indexes);
                    index.setCommonContentViaDb();
                    indexes.add(index);
                }
                setIndexes(indexes);
            }
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setIndexesViaDb failed", ex);
            throw JddException.createInstance(ex);
        }
    }

    @Override
    public void setPrimaryKeyViaDb() throws JddException {
        String tabname = getName();
        DbJdbPrimaryKey primaryKey = new DbJdbPrimaryKey(getDbFactory(), tabname);
        try {
            primaryKey.setCommonContentViaDb();
            super.setPrimaryKey(primaryKey);
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setPrimaryKeyViaDb failed", ex);
            throw JddException.createInstance(ex);
        }
    }

    @Override
    public DbObjectSqlStatements getDdlStatementsForCreate() throws JddException {
        // return super.getDdlStatementsForCreate();
        DbObjectSqlStatements tableDef = new DbObjectSqlStatements(getName());
        DbSqlStatement createLine = new DbSqlStatement();
        try {
            createLine.addLine("CREATE TABLE" + " " + "\"" + getName() + "\"");
            createLine.merge(getColumns().getDdlClause());
            tableDef.add(createLine);
            if (getPrimaryKey() != null) {
                tableDef.merge(getPrimaryKey().getDdlStatementsForCreate());
            }
            if (getIndexes() != null) {
                tableDef.merge(getIndexes().getDdlStatementsForCreate());
            }
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "getDdlStatementsForCreate failed", ex);
            throw JddException.createInstance(ex);
        }
        return tableDef;
    }

    @Override
    public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0) {
        file.println(offset0 + XmlHelper.xmlTitle);
        file.println(offset0 + "<db-spec>" + "</db-spec>");
    }

    private List<String> dbGetIndexNames(Connection con) throws JddException {
        final List<String> result = new ArrayList<String>();
        final String stmtStr = "select g.conglomeratename from sys.systables t, sys.sysconglomerates g where t.tablename = ? and t.tableid = g.tableid and t.schemaid = g.schemaid and g.isindex and not exists ( select * from sys.syskeys k where k.conglomerateid = g.conglomerateid )";
        try {
            final PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            try {
                ps.setString(1, getName().toUpperCase());
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        result.add(rs.getString(1));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (SQLException ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "dbGetIndexNames failed", ex);
            throw JddException.createInstance(ex);
        }
        return result;
    }

    @Override
    public boolean existsOnDb() throws JddException {
        Connection conn = getDbFactory().getConnection();
        String stmtStr = "SELECT 1 FROM SYS.SYSTABLES WHERE TABLENAME = ?";
        try {
            final boolean exists;
            PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
            try {
                ps.setString(1, getName().toUpperCase());
                ResultSet rs = ps.executeQuery();
                try {
                    exists = rs.next();
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
            return exists;
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "existsOnDb failed", ex);
            throw JddException.createInstance(ex);
        }
    }

    @Override
    public boolean existsData() throws JddException {
        boolean exists = false;
        Connection conn = getDbFactory().getConnection();
        String stmtStr = "select count(*) from \"" + getName().toUpperCase() + "\"";
        try {
            Statement stmt = NativeSQLAccess.createNativeStatement(conn);
            try {
                ResultSet rs = stmt.executeQuery(stmtStr);
                try {
                    if (!rs.next()) {
                        throw new JddException("failed to execute >>" + stmtStr + "<<.");
                    }
                    final int count = rs.getInt(1);
                    exists = count > 0;
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "existsData failed", ex);
            throw JddException.createInstance(ex);
        }
        return exists;
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
    public boolean checkWidth() {
        // compute width of one row in bytes and compare it against maximum
        DbColumns columns = getColumns();
        DbColumnIterator iter = columns.iterator();
        DbColumn column;
        int total = 0;
        int maxWidth = DbJdbEnvironment.MaxTableLength();
        while (iter.hasNext()) {
            column = iter.next();
            total += DbJdbEnvironment.GetColumnLength(column, false);
        }
        if (total > maxWidth) {
            Object[] arguments = { getName(), new Integer(total), new Integer(maxWidth) };
            CATEGORY.errorT(LOCATION, "checkWidth {0}: total width({1}) greater than allowed maximum ({2})", arguments);
            return false;
        }
        return true;
    }
}
