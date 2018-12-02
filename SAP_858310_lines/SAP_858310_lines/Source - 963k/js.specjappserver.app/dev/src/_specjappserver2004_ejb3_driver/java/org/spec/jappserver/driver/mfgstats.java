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
 *  2003/06     Tom Daly, Sun             Conversion to SPECjAppServer2004
 *
 *  $Id: MfgStats.java,v 1.6 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

/**
 * This file contains the stats object that is saved by each mfg line.
 * This object is passed back after the end of a run to be aggregated 
 * with other mfg lines.
 *
 * @author Shanti Subramanyam
 */
public class MfgStats implements java.io.Serializable {
	/* The following finals are for histograms */

	// Response times
	public static final int RESPINTERVAL = 25;	/* total interval of buckets, in secs */
	public static final double RESPUNIT = 0.25;	/* time period of each bucket */
	public static final int RESPMAX = 100;	/* # of buckets = INTERVAL/UNIT */
	public static final int RESPBUCKET = 250;	/* bucket time in msec */

	public static final int RESPFAST = 5;

        // Thruput
        public static final int THRUINTERVAL = 172800;  /*  48 hours */
        public static final int THRUUNIT = 30;  /* 30 secs */
        public static final int THRUMAX = 5760; /* THRUINTERVAL / THRUUNIT */
        public static final int THRUBUCKET = 30000; /* Each bucket is for 30 sec */

	int workOrderCnt = 0;			/* number of workorders scheduled */
	int vehicleCnt = 0;			/* number of vehicles produced */
	int largeOrderCnt = 0;			/* no. of workorders scheduled due to largeorders */
	int largeOrderVehicleCnt = 0;	/* vehicles produced by LargeOrderLine */
	int respMax = 0;			/* Max. response time */
	double respTime = 0;		/* Sum of response times */
	int respHist[] = new int [MfgStats.RESPMAX];/* Response time histogram */
	int thruput[] = new int[MfgStats.THRUMAX];	/* Thruput histogram */
	int numPlannedLines;
	String resultsDir;		/* Name of results directory */
	int stdyState;

    /**
     * Constructs the MfgStats object.
     * @param numPlannedLines The number of planned lines
     * @param resultsDir The output directory for the results
     */
	public MfgStats(int numPlannedLines, String resultsDir) {
		this.numPlannedLines = numPlannedLines;
		this.resultsDir = resultsDir;
	}

	/**
     * Sets the steady state time.
     * @param stdyState The steady state time
     */
    public void setStdyState(int stdyState) {
		this.stdyState = stdyState;
	}

	/**
	 * This method adds the stats from the passed object to ours.
     * @param m The input stats for the aggregation
	 */
	public void addResult(MfgStats m) {
		resultsDir = m.resultsDir;
		stdyState = m.stdyState;
		numPlannedLines += m.numPlannedLines;
		workOrderCnt += m.workOrderCnt;
		largeOrderCnt += m.largeOrderCnt;
		vehicleCnt += m.vehicleCnt;
		largeOrderVehicleCnt += m.largeOrderVehicleCnt;
		if (m.respMax > respMax)
			respMax = m.respMax;
		respTime += m.respTime;
		for (int i = 0; i < RESPMAX; i++) {
			respHist[i] += m.respHist[i];
		}
		for (int i = 0; i < THRUMAX; i++) {
			thruput[i] += m.thruput[i];
		}
	}


	public String toString() {
		return (Double.toString((double)(workOrderCnt) * 1000.0/stdyState));
	}
}
