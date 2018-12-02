/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Ken Arnold, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jini.thread;

import org.spec.jini.debug.Debug;

/**
 * This is a package-accessible class for turning on debug output in
 * the util classes.  The property name is for the debug property is
 * <code>com.sun.jini.thread.debug</code>.
 *
 * @see Debug
 */
class ThreadDebug {
    /**
     * The <code>Debug</code> object for
     * <code>com.sun.jini.thread.debug</code>.
     */
    static final Debug debug = new Debug("com.sun.jini.thread.debug");

    /**
     * A shorthand for invoking <code>getWriter</code> on the <code>debug</code>
     * field.
     */
    static java.io.PrintWriter getWriter(String subsystem) {
        return debug.getWriter(subsystem);
    }
}
