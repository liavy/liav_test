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
 * $Id: Timer.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

/**
 * This class has the functions to get timestamps.
 */
public class Timer implements java.io.Serializable {
	long startSec;

	/**
     * Construct the timer object.
     */
    public Timer() {
		startSec = System.currentTimeMillis();
	//	Debug.println("Timer: startSec = " + startSec);
	}


    /**
     * Gets the time marking the starting point of this timer.
     * @return The starting or offset time
     */
	public long getOffsetTime() {
		Debug.println("Timer: startSec in getOffsetTime = " + startSec);
		return(startSec);
	}


	/**
	 * This  method returns the current time relative to startSec.
	 * This way, we don't need to keep track of large numbers and
	 * worry about long variables.
     * @return the current time, offset from the offset time
	 */
	public int getTime() {
		long c = System.currentTimeMillis();
		return ((int)(c - startSec));
	}

}
