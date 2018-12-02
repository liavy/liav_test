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

import java.io.PrintStream;
import java.rmi.RemoteException;

import javax.ejb.EJBException;

/**
 * Class Debug
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Debug implements java.io.Serializable {

    transient PrintStream logTarget = System.err;

    /**
     * Method setLogTarget
     *
     *
     * @param target The the new log destination to be used.
     *
     */
    public void setLogTarget(PrintStream target) {
        logTarget = target;
    }

    /**
     * Method getLogTarget
     *
     *
     * @return The PrintStream used as the current log target
     *
     */
    public PrintStream getLogTarget() {
        return logTarget;
    }

    /**
     * Method print
     *
     *
     * @param debugLevel
     * @param message
     *
     */
    public void print(int debugLevel, String message) {

        // Do nothing.
    }

    /**
     * Method println
     *
     *
     * @param debugLevel
     * @param Message
     *
     */
    public void println(int debugLevel, String Message) {

        // Do nothing.
    }

    /**
     * Method printStackTrace
     *
     *
     * @param e
     *
     */
    public void printStackTrace(Throwable e) {

        // Yes, we print the stack trace for an originating
        // exception, but nothing else.
        // If e is not a remote exception
        // or if e.detail is null
        // or if e.detail is not an EJBException
        // Print the stack
        // Otherwise the stack has already been printed, don't print.
        if( !(e instanceof RemoteException)
            || ((RemoteException) e).detail == null
            ||!(((RemoteException) e).detail instanceof EJBException) ) {
            e.printStackTrace(logTarget);
        }
    }
}

