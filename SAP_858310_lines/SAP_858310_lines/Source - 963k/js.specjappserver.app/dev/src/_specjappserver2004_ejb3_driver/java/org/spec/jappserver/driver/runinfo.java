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
 *  2003/02     Tom Daly, Sun             Conversion from SPECjAppServer2002 to SPECjAppServer2004
 *                                        OrderEntry no longer used, use DealerEntry.
 *  2004/02/08  John Stecher, IBM         Modified code to support launching multiple LOAgents
 *
 */

 package org.spec.jappserver.driver;

 /**
  * RunInfo
  * This class contains the run parameters used for this run. These
  * are printed out in the SPECjAppServer.summary report
  */
public class RunInfo {
	public int txRate;
	public int runDealerEntry;
	public int runMfg;
        public int runLO;
	public int dumpStats;
	public int rampUp;
	public int rampDown;
	public int stdyState;
	public int triggerTime;
        public int msBetweenThreadStart;
	public int benchStartTime;
	public int numDealerAgents;
	public int numMfgAgents;
        public int numLOAgents;
	public boolean runLargeOrderLine;
	public long start;			// benchStartTime in actual time

        public int doAudit;
}
