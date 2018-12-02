/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbView.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbView;
import com.sap.dictionary.database.dbs.DbsConstants;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbJdbView extends DbView implements DbsConstants {
    private static final Location LOCATION = Location.getLocation(DbJdbView.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbJdbView() {
    }

    public DbJdbView(DbFactory factory) {
        super(factory);
    }

    public DbJdbView(DbFactory factory, String name) {
        super(factory, name);
    }

    public DbJdbView(DbFactory factory, DbView other) {
        super(factory, other);
    }

    public DbJdbView(DbFactory factory, DbSchema schema, String name) {
        super(factory, schema, name);
    }

    @Override
    public boolean existsOnDb() throws JddException {
        final String stmtStr = "select 1 from sys.systables t, sys.sysviews v where t.tablename = ? and t.tabletype = 'V' and t.tableid = v.tableid";
        try {
            final Connection conn = getDbFactory().getConnection();
            final boolean exists;
            final PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
            try {
                ps.setString(1, getName());
                final ResultSet rs = ps.executeQuery();
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
    public void setCreateStatementViaDb() throws JddException {
        final String stmtStr = "select v.viewdefinition from sys.systables t, sys.sysviews v where t.tablename = ? and t.tabletype = 'V' and t.tableid = v.tableid";
        try {
            Connection conn = getDbFactory().getConnection();
            PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
            try {
                ps.setString(1, getName().toUpperCase());
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        final String createStatement = rs.getString(1);
                        setCreateStatement(createStatement);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "setCreateStatementViaDb failed", ex);
            throw JddException.createInstance(ex);
        }
    }
}
