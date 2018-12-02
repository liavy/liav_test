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

package org.spec.jini.constants;

/**
 * Various constants useful in calculating times.
 */
public interface TimeConstants {
    /** Tics per second. */
    long SECONDS    = 1000;

    /** Tics per minute. */
    long MINUTES    = 60 * SECONDS;

    /** Tics per hour. */
    long HOURS      = 60 * MINUTES;

    /** Tics per day. */
    long DAYS       = 24 * HOURS;
}
