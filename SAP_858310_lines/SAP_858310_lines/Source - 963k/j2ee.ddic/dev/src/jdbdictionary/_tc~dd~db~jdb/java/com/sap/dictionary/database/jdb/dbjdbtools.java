/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbTools.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTools;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbJdbTools extends DbTools {
    private static final Location LOCATION = Location.getLocation(DbJdbTools.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbJdbTools(DbFactory factory) {
        super(factory);
    }

    @Override
    public void renameTable(String sourceName, String destinationName) throws JddException {
        try {
            Connection conn = getFactory().getConnection();
            Statement stmt = NativeSQLAccess.createNativeStatement(conn);
            try {
                stmt.execute("RENAME TABLE \"" + sourceName.toUpperCase() + "\" TO \"" + destinationName + "\" ");
            } finally {
                stmt.close();
            }
        } catch (SQLException sqlEx) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "renameTable({0},{1}) failed", new Object[] { sourceName,
                    destinationName, }, sqlEx);
            ExType xt;
            switch (sqlEx.getErrorCode()) {
            case -4004:
                xt = ExType.NOT_ON_DB;
                break;
            case -6000:
                xt = ExType.EXISTS_ON_DB;
                break;
            default:
                xt = ExType.SQL_ERROR;
                break;
            }
            throw new JddException(xt, sqlEx.getMessage());
        } catch (Exception ex) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "renameTable({0},{1}) failed", new Object[] { sourceName,
                    destinationName, }, ex);
            throw new JddException(ExType.OTHER, ex.getMessage());
        }
        Object[] arguments = { sourceName, destinationName };
        CATEGORY.infoT(LOCATION, "renameTable: renamed {0} to {1}", arguments);
        return;
    }

    @Override
    public boolean isAlias(final String tableName) {
        try {
            final Connection conn = getFactory().getConnection();
            final boolean result;
            final PreparedStatement stmt = NativeSQLAccess.prepareNativeStatement(conn, 
                    "select 1 from sys.systables t where t.tablename = ? and t.tabletype = 'A'");
            try {
                stmt.setString(1, tableName);
                final ResultSet rs = stmt.executeQuery();
                try {
                    result = rs.next();
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
            return result;
        } catch (SQLException sqlEx) {
            CATEGORY.logThrowableT(Severity.ERROR, LOCATION, "isAlias failed", sqlEx);
            throw new RuntimeException("SQLException in isAlias", sqlEx);
        }
    }
}
