/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.common;


import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import javax.ejb.EJBException;


/**
 * Class Util
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Util {

    private static long offset = TimeZone.getDefault().getOffset(
        System.currentTimeMillis());

    private Util() {
    }

    public static Date getCurrentDateRoundToDay()
    {
       return getDateRoundToDay(System.currentTimeMillis());
    }

    public static Date getDateRoundToDay(long millis)
    {
       Date today = new Date(DateTimeNormalizer.normalizeSqlDateMillies(millis));
       return today;
    }

    /**
     * Method round
     *
     *
     * @param value
     * @param digits
     *
     * @return
     *
     */
    public static double round(double value, int digits) {

        double base = Math.pow(10, digits);

        value = (double) Math.round(value * base);
        value /= base;

        return value;
    }

    /**
     * Method closeConnection
     *
     *
     * @param conn
     * @param st
     *
     */
    public static void closeConnection(Connection conn, Statement st) {

        boolean closeError = false;

        if( st != null ) {
            try {
                st.close();
                st = null;
            } catch( SQLException e ) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if( conn != null ) {
            try {
                conn.close();
                conn = null;
            } catch( SQLException e ) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if( closeError ) {
            throw new EJBException("Exceptions trying to close connection!");
        }
    }

    /**
     * Method closeConnection
     *
     *
     * @param conn
     * @param st
     *
     */
    public static void closeConnection(Connection conn, Statement[] st) {

        boolean closeError = false;

        for( int i = 0; i < st.length; i++ ) {
            if( st[i] != null ) {
                try {
                    st[i].close();
                    st[i] = null;
                } catch( SQLException e ) {
                    e.printStackTrace(System.err);

                    closeError = true;
                }
            }
        }

        if( conn != null ) {
            try {
                conn.close();
                conn = null;
            } catch( SQLException e ) {
                e.printStackTrace(System.err);

                closeError = true;
            }
        }

        if( closeError ) {
            throw new EJBException("Exceptions trying to close connection!");
        }
    }
}

