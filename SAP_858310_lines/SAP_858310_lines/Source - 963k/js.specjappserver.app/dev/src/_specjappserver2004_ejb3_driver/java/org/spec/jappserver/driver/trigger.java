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
 * $Id: Trigger.java,v 1.4 2004/02/17 17:15:28 skounev Exp $
 */
 
package org.spec.jappserver.driver;
 /**
  * This class is used to synchronize all the threads of an Agent.
  * All threads wait on this object's monitor until the Agent
  * signals that the run should begin
  */
class Trigger {
	private int delay;

    public Trigger() {
       super();
    }

    synchronized void waitForRun() {
        try {
            wait();
        } catch (Exception e) {
            System.err.println ("Exception in trigger.wiatForRun(): " + e);
        }
        return;
    }

	synchronized void startRun(int delay) {
		try {
			Thread.sleep(delay);
		} catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
		}
		notifyAll();
		return;
	}
}
