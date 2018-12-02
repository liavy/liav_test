/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*  History:
*  Date         ID                Description
*  ----------   ---------------   -------------------------------------------------------------------
*  Feb, 2003    Tom Daly, Sun     Creation date, copied from OrderStats of
*                                 ECperf and SPECjAppServer2001 and 2002
*  Dec, 2003    Tom Daly, Sun     Added count for logins  
*  Apr, 2004    Sam Kounev,       Modified code to initialize the cycleMin array to Integer.MAX_VALUE  
*               Darmstadt Univ.   instead of 9999999.
*                                 
* $Id: DealerStats.java,v 1.8 2004/04/20 09:22:51 skounev Exp $
*
*/

package org.spec.jappserver.driver;

import org.spec.jappserver.driver.event.StatisticBuilder2;

/**
 *
 * This class contains the stats object that is saved by each 
 * DealersApp thread. This object is passed back after the end of a 
 * run to be aggregated with results from other threads run within an
 * Agent.
 *
 * @author Tom Daly (adapted from OrderStats.java by Shanti Subramanyam )
 */
public class DealerStats implements java.io.Serializable {
    public static final int TXTYPES = 3;	// Number of tx. types
    public static final int PURCHASE = 0;
    public static final int MANAGE = 1;
    public static final int BROWSE = 2;

    /* The following finals are for histograms */

    // Response times
    public static final int RESPINTERVAL = 10;	/* total interval of buckets, in secs */
    public static final double RESPUNIT = 0.1;	/* time period of each bucket */
    public static final int RESPMAX = 100;	        /* # of buckets = INTERVAL/UNIT */
    public static final int RESPBUCKET = 100;	/* bucket time in msec */

    public static final int PURCHASEFAST = 2;
    public static final int MANAGEFAST = 2;
    public static final int BROWSEFAST = 2;

    // Cycle times
    public static final int CYCLEINTERVAL = 50;	/* 5 * max. think time */ 
    public static final double CYCLEUNIT = 0.25;
    public static final int CYCLEMAX = 200;
    public static final int CYCLEBUCKET = 250;

    // Thruput
    public static final int THRUINTERVAL = 172800;	/*  48 hours */
    public static final int THRUUNIT = 30;	/* 30 secs */
    public static final int THRUMAX = 5760; /* THRUINTERVAL / THRUUNIT */
    public static final int THRUBUCKET = 30000; /* Each bucket is for 30 sec */


    int purcBadCredit;	        /* neworders that failed due to insufficient credit */
    int purcLrgCnt;		/* number of neworders for large orders */
    int purcOlCnt;		/* total number of items ordered */
    int purcLrgOlCnt;	        /* total number of items for large orders */
    int purcCheckCnt;           /* Number checkout immediates */
    int purcDeferCnt;           /* number of defered checkouts */
    int purcClearCnt;           /* number of times shopping cart is cleared*/
    int purcRemoveCnt;          /* number of vehicles removed from shopping cart */


    int manageCancelCnt;        /* number of open orders cancelled  */
    int manageSoldCnt;          /* number of vehicles sold from lot */
    int manageAppErrCnt;        /* Number of times dealer sells already sold car
                                   or cancels and already cancelled order */

    int browseFwdCnt;           /* no of  browse next operations */
    int browseBkwdCnt;          /* no of browse bkwd operations */


    int threadCnt;		/* threads in this Agent */
    int stdyState;		/* Needed by OrdersAggStats */
    String resultsDir;	        /* Name of results directory */
    int txRate;

    int loginCnt;

    /* Stats for all transaction types */

    int txCnt[] = new int[TXTYPES];			/* number of transactions */
    int respMax[] = new int[TXTYPES];			/* Max. response time */
    double respSum[] = new double[TXTYPES];		/* Sum of response times */
    double cycleSum[] = new double[TXTYPES];	        /* sum of cycle times */
    double targetedCycleSum[] = new double[TXTYPES];	/* targeted cycle times */
    int cycleMax[] = new int[TXTYPES];
    int cycleMin[] = new int[TXTYPES];
    double elapse[] = new double[TXTYPES];		/* sum of elapsed times */
    int respHist[][] = new int[TXTYPES][RESPMAX];       /* Response time histogram */
    int cycleHist[][] = new int[TXTYPES][CYCLEMAX];	/* Cycle time histogram */
    int targetedCycleHist[][] = new int[TXTYPES][CYCLEMAX];	/* Cycle time histogram */
    int thruputHist[][] = new int[TXTYPES][THRUMAX];	/* Thruput histogram */

    /**
     * Constructs the DealerStats object.
     * @param threadCnt  The number of threads represented by the stats
     * @param resultsDir The results directory
     * @param txRate The injection rate
     */
    public DealerStats(int threadCnt, String resultsDir, int txRate) {
        this.threadCnt = threadCnt;
        this.resultsDir = resultsDir;
        this.txRate = txRate;
        for (int i = 0; i < cycleMin.length; i++)
            cycleMin[i] = Integer.MAX_VALUE;
    }    

    /**
     * Sets the steady state time.
     * @param stdyState The steady state time
     */
    public void setStdyState(int stdyState) {
        this.stdyState = stdyState;
    }

    /**
     * This method updates the thruput histogram for the
     * given transaction type.
     * @param txType Transaction type
     * @param elapsedTime Elapsed time
     */
    public void updateThruput(int txType, int elapsedTime) {
        if ((elapsedTime / THRUBUCKET) >= THRUMAX)
            thruputHist[txType][THRUMAX - 1]++;
        else
            thruputHist[txType][elapsedTime / THRUBUCKET]++;
    }

    /**
     * This method updates the various stats for the requested
     * type of transaction - txCnt, resptime, and cycletime stats.
     * @param txType Transaction type
     * @param respTime Response ime
     * @param targetedCycleTime Targeted cycle time
     * @param actualCycleTime Actual cycle time
     */
    public void update(int txType, int respTime,
                       int targetedCycleTime, int actualCycleTime) {
        txCnt[txType]++;
        respSum[txType] += respTime;
        cycleSum[txType] += actualCycleTime;
        targetedCycleSum[txType] += targetedCycleTime;
        if (respTime > respMax[txType])
            respMax[txType] = respTime;

        // post in histogram of response times
        if ((respTime / RESPBUCKET) >= RESPMAX)
            respHist[txType][RESPMAX - 1]++;
        else
            respHist[txType][respTime / RESPBUCKET]++;

        if (actualCycleTime > cycleMax[txType])
            cycleMax[txType] = actualCycleTime;
        if (actualCycleTime < cycleMin[txType])
            cycleMin[txType] = actualCycleTime;

        if ((actualCycleTime / CYCLEBUCKET) >= CYCLEMAX)
            cycleHist[txType][CYCLEMAX - 1]++;
        else
            cycleHist[txType][actualCycleTime / CYCLEBUCKET]++;
        if ((targetedCycleTime / CYCLEBUCKET) >= CYCLEMAX)
            targetedCycleHist[txType][CYCLEMAX - 1]++;
        else
            targetedCycleHist[txType][targetedCycleTime / CYCLEBUCKET]++;
    }
}
