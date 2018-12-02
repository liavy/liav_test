/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.spec.jappserver.common.names.JNDINames;

/*
 * Provides a few convenience methods for db access and batch execution.
 */
class DatabaseHelper {

    private static final String CLASSNAME = DatabaseHelper.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);

    /*
     * For now, p1 is always 1 (in RandPart).
     * We'll leave this for when we use multiple PGs
     * curBatch = j / 100 + 1; // compute value of p1
     */
    static final int CUR_BATCH = 1;
    static final int BATCH_SIZE = 500;
    
    private static final int SEQUENCE_BLOCK_SIZE = 10000;

    // Datasource cannot be injected using @Resource.
    // Otherwise an exception during injection cannot be catched
    // and LoaderServlet cannot be informed.
    // TODO: discuss if one single datasource is enough
    public static final String CORPDB_KEY = "CorpDataSource";
    public static final String ORDS_KEY = "OrderDataSource";
    public static final String MFG_KEY = "MfgDataSource";
    public static final String SUPP_KEY = "SupplierDataSource";
    
    static Connection getConnection(final String key) throws SQLException, NamingException {
        // Resource injection cannot be used since the exception generated if injection failed
        // cannot be catched and an error message cannot be sent.
        final InitialContext ctx = new InitialContext();
        DataSource dataSource;
        String dsName = "jdbc/" + key;
        try {
            if (CORPDB_KEY != key && MFG_KEY != key && SUPP_KEY != key && ORDS_KEY != key) { 
                throw new IllegalArgumentException("The datasource jndi name " + dsName + " is unknown.");
            }
            dataSource = (DataSource) ctx.lookup(dsName);
        } catch (NamingException e) {
            logger.log(Level.WARNING, "Cannot lookup data source " + dsName + " for loading SPECjAppServer database.", e);
            dataSource = (DataSource) ctx.lookup(JNDINames.DATA_SOURCE_NAME);
        }
        return getConnection(dataSource);
    }

    static Connection getConnection(final DataSource dataSource) throws SQLException {
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    static void clean(final Connection dbConnection, final String tableName) throws SQLException,
            NamingException {
        try {
            final Statement deleteStatment = dbConnection.createStatement();
            try {
                deleteStatment.executeUpdate("truncate table " + tableName);
            } catch (SQLException e) {
                deleteStatment.executeUpdate("delete from " + tableName);
            } finally {
                deleteStatment.close();
            }
            dbConnection.commit();
        } finally {
            dbConnection.close();
        }
    }

    static void loadSequence(final Connection dbConnection, final String id, final int nextSeq) throws SQLException,
            NamingException {
        try {
            final Statement deleteStatement = dbConnection.createStatement();
            try {
                deleteStatement.executeUpdate("delete from U_SEQUENCES where S_ID = '" + id + "'");
            } finally {
                deleteStatement.close();
            }
            dbConnection.commit();
            final PreparedStatement cs = dbConnection
                    .prepareStatement("insert into U_SEQUENCES (S_ID, S_NEXTNUM, S_BLOCKSIZE) values (?,?,?)");
            try {
                cs.setString(1, id);
                cs.setInt(2, nextSeq);
                cs.setInt(3, SEQUENCE_BLOCK_SIZE);
                cs.executeUpdate();
            } finally {
                cs.close();
            }
            dbConnection.commit();
        } finally {
            dbConnection.close();
        }
    }

    static void executeBatch(final Connection dbConnection, final int count, final PreparedStatement ... pss) throws SQLException {
        if (count != 0) {
            for (PreparedStatement ps : pss) {
                ps.executeBatch();
            }
            dbConnection.commit();
        }
    }

    static int executeBatchIfFull(final Connection dbConnection, int count, final PreparedStatement ... pss) throws SQLException {
        if (count >= BATCH_SIZE) {
            executeBatch(dbConnection, count, pss);
            count = 0;
        }
        return count;
    }        

    static int addAndExecuteBatchIfFull(final Connection dbConnection, int count, final PreparedStatement ps) throws SQLException {
        ps.addBatch();
        count++;
        return executeBatchIfFull(dbConnection, count, ps);
    }        
}
