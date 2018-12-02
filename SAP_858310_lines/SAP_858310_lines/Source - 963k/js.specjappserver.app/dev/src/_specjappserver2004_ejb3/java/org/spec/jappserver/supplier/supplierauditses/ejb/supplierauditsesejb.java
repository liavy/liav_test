/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------------------------------------
 *  2002/03/22  Ramesh Ramachandran, SUN    Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2003/05/05  Matt Hogstrom, IBM        Added code to support DeliveryServlet Count from Emulator
 *  2003/11/06  Samuel Kounev, Darmstadt  Modified database scaling rules (see osgjava-5681).
 *  2003/11/06  Samuel Kounev, Darmstadt  Added initial values validation check for S_supp_component.
 *  2003/11/07  Samuel Kounev, Darmstadt  Fixed bug in setSessionContext: debug was used in catch block
 *                                        before initialized causing NullPointer exception.
 *  2003/11/25  Samuel Kounev, Darmstadt  Increased tolerance levels for table cardinalities to -/+5% (osgjava-5889).
 *                                        Modified database scaling rules as per osgjava-5891.
 *  2004/02/27  Samuel Kounev, Darmstadt  Fixed bug in validateInitialValues (see osgjava-6621).
 *  2004/04/15  John Stecher, IBM         Modified to allow for Read Committed Auditing instead of Repeatable Read.
 *  2004/04/20  Samuel Kounev, Darmstadt  Moved the call to set isolation level in a separate method to avoid using
 *                                        nested try-catch clauses.
 */

package org.spec.jappserver.supplier.supplierauditses.ejb;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.common.Util;

/**
 * This class is SupplierAuditSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class SupplierAuditSesEJB implements SessionBean {

    private static final int EMULATOR_INDEX = 0;
    private static final int DELIVERY_INDEX = 1;


    private String           className = "SupplierAuditSesEJB";
    protected Debug          debug;
    protected boolean        debugging;
    protected javax.sql.DataSource dataSource;

    private String deliveryCountURL, emulatorCountURL;

    /**
     * Method ejbCreate
     *
     *
     * @throws CreateException
     *
     */
    public void ejbCreate() throws CreateException {
        if( debugging )
            debug.println(3, "ejbCreate ");
    }

    /**
     * Constructor SupplierAuditSesEJB
     *
     *
     */
    public SupplierAuditSesEJB() {
    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
    }

    /**
     * Method setSessionContext
     *
     *
     * @param sc
     *
     */
    public void setSessionContext(SessionContext sc) {

        InitialContext initCtx;

        try {
            initCtx = new InitialContext();
            dataSource =
            (javax.sql
             .DataSource) initCtx
            .lookup("java:comp/env/SupplierDataSource");
        } catch( NamingException e ) {
            throw new EJBException("Failure looking up DataSource " + e);
        }

        try {
            int debugLevel =
            ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
            .intValue();

            if( debugLevel > 0 ) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch( NamingException ne ) {
            debug = new Debug();
        }

        try {
            deliveryCountURL = (String)initCtx.lookup("java:comp/env/deliveryServlet");
            emulatorCountURL = (String)initCtx.lookup("java:comp/env/emulatorServlet");
        } catch( NamingException e ) {
            debug.println(1, "Unable to get Servlet URLs " + e);
            debug.printStackTrace(e);
        }

        if( debugging )
            debug.println(3, "setSessionContext");
    }


// Methods
    public boolean validateInitialValues(int txRate) {

        Connection        conn = null;
        Statement stmt = null;
        boolean isValid = true;
        int rowCount = 0;

        if( debugging )
            debug.println(3, "validateInitialValues");

        try {
            conn = dataSource.getConnection();
            setTxReadCommitted(conn);
            stmt = conn.createStatement();

            // Check S_site count
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_site");
            if( rs.next() )
                rowCount = rs.getInt(1);
            if( rowCount !=  1 ) {
                isValid = false;

                if( debugging )
                    debug.println(3, "Invalid Initial count for S_site");
            }

            // Check S_supplier count
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_supplier");
            if( rs.next() )
                rowCount = rs.getInt(1);
            if( rowCount != 10 ) {
                isValid = false;

                if( debugging )
                    debug.println(3, "Invalid Initial count for S_supplier");
            }

            // Check S_component
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_component");
            if( rs.next() )
                rowCount = rs.getInt(1);
            if( Math.abs(rowCount -  1000 * txRate) / (1000 * txRate) > 0.05 ) {
                isValid = false;

                if( debugging )
                    debug.println(3, "Invalid Initial count for S_component");
            }

            // Check S_purchase_order
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_order");
            if( rs.next() )
                rowCount = rs.getInt(1);
            /************
            // **** Too much variation
            if((rowCount <  20 * txRate) &&
               (Math.abs(rowCount -  20 * txRate) / (20 * txRate) > 0.05)) {
                isValid = false;

                if (debugging)
                    debug.println(3, "Invalid Initial count for S_purchase_order");
            }
            ****************/

            // Check S_purchase_orderline
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_orderli");
            if( rs.next() )
                rowCount = rs.getInt(1);
            if( (rowCount < 100 * txRate) &&
                (Math.abs(rowCount - 100 * txRate) / (100 * txRate) > 0.05) ) {
                isValid = false;

                if( debugging )
                    debug.println(3, "Invalid Initial count for S_purchase_orderli");
            }

            // Check S_supp_component
            rs = stmt.executeQuery("SELECT COUNT (*) FROM S_supp_component");
            if( rs.next() )
                rowCount = rs.getInt(1);
            if( Math.abs(rowCount - 10000 * txRate) / (10000 * txRate) > 0.05 )  {
                isValid = false;
                if( debugging )
                    debug.println(3, "Invalid Initial count for S_supp_component");
            }

        } catch( SQLException e ) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return isValid;
    }

    public int getPOCount() {

        Connection conn = null;
        Statement stmt = null;

        int rowCount = 0;

        if( debugging )
            debug.println(3, "getPOCount()");

        try {
            conn = dataSource.getConnection();
            setTxReadCommitted(conn);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_order");
            if( rs.next() )
                rowCount = rs.getInt(1);

            if( debugging )
                debug.println(3, "SELECT COUNT (*) FROM S_purchase_order returned " + rowCount);

        } catch( SQLException e ) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
        return rowCount;
    }


    public int getPOLineCount() {

        Connection conn = null;
        Statement stmt = null;

        int rowCount = 0;

        if( debugging )
            debug.println(3, "getPoLineCount()");
        try {
            conn = dataSource.getConnection();
            setTxReadCommitted(conn);
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM S_purchase_orderli");
            if( rs.next() )
                rowCount = rs.getInt(1);

            if( debugging )
                debug.println(3, "SELECT COUNT (*) FROM S_purchase_orderline returned " + rowCount);

        } catch( SQLException e ) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        return rowCount;
    }

    public int[] getServletTx() {

        String okMsg = "200 OK";
        String txMsg = "TxCount";
        // index 0 for Emulator, and 1 for Delivery
        int[] txCount = {0, 0};

        URL[] url = new URL[2];

        try {
            url[0] = new URL(emulatorCountURL);                  // $Hogstrom, modified to support DeliveryCount from Emulator
            url[1] = new URL(deliveryCountURL);                  // $Hogstrom, modified to support DeliveryCount from Emulator

            for( int i = 0; i < url.length; i++ ) {
                HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
                BufferedReader reader = new BufferedReader(
                                                          new InputStreamReader(conn.getInputStream()));

                if( debugging )
                    debug.println(3, "Connected to " + url[i]);

                boolean ok = false;
                String r = null;

                for( ;; ) {
                    r = reader.readLine();
                    if( r == null )
                        break;
                    else if( r.indexOf(txMsg) != -1 )
                        txCount[i] = Integer.parseInt(r.substring(r.indexOf('=') + 1,
                                                                  r.indexOf(';')).trim());
                    else if( r.indexOf(okMsg) != -1 )
                        ok = true;
                }
                reader.close();
                if( !ok ) {
                    debug.println(1, "Unable to get Tx counts from servlets");
                    throw new EJBException("Unable to get Tx counts from servlets");
                }
            }
        } catch( Exception e ) {
            debug.println(1, "Unable to get Tx counts from servlets " + e);
            throw new EJBException("Unable to get Tx counts from servlets " + e);
        }
        if( debugging )
            debug.println(3, "Emulator Tx = " + txCount[0] + ": Delivery Tx = " + txCount[1]);
        return txCount;
    }

    private void setTxReadCommitted(Connection conn) {
        try {
            conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            debug.printStackTrace(e);
        }
    }

}

