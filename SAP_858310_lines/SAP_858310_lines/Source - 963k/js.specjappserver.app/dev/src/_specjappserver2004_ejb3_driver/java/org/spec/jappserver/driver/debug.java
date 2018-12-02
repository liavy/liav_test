/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ---------------------------------------------------------------
 *  2001        Shanti Subrmanyam, SUN    Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: Debug.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

/**
 * This class can be used to print Debug messages during development.
 * <pre>
 * Use as follows : 
 * Debug.print("Now in method foo in class bar");
 * </pre>
 * For production, both the print methods should be null methods.
 */

public class Debug {

    /**
     * Outputs a debug message.
     * @param msg The message
     */
	public static void print(String msg) {
	  //System.out.print(msg);
	}

	/**
     * Outputs a debug message followed by the end of line.
     * @param msg The message
     */
    public static void println(String msg) {
	  //System.out.println(msg);
	}
}
