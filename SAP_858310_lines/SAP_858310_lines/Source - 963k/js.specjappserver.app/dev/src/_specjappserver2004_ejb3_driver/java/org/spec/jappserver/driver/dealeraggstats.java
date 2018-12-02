/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*  History:
*  Date         ID             Description
*  ----------   -------------  -------------------------------------------------------------------------
*  Feb, 2003    Tom Daly, Sun  Creation date, copied from DealerAggStats of
*                              ECperf and SPECjAppServer2001 and 2002
*  Dec, 2003    Tom Daly  Sun  Updated to count logins
*  Apr, 2004    Sam Kounev     Added code to initialize the cycleMin array to Integer.MAX_VALUE to
*                              avoid results skew in case the driver thread has starved and never gotten
*                              any response back from the SUT (as per bug report from Ning, Akara).
*
* $Id: DealerAggStats.java,v 1.9 2004/04/20 15:56:28 skounev Exp $
*
*/
package org.spec.jappserver.driver;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;

/**
 * This class computes the aggregate stats of all the DealerApp threads
 * run from a particular agent. A single instance of this class should be
 * instantiated
 *
 * @see DealerStats
 * @author Tom Daly (adapted from OrdersAddStats by Shanti Subramanyam)
 */
public class DealerAggStats implements java.io.Serializable {

    String logfile;	// name of output log file
    int threadCnt, stdyState, txRate;
    String resultsDir;
    boolean debug = false;


    int sumPurcAutos =0;
    int sumPurcBadCredit = 0;
    int sumPurcLrgCount =0;
    int sumPurcOlCnt = 0;
    int sumPurcLrgOlCnt =0;
    int sumPurcCheckCnt = 0 ;
    int sumPurcDeferCnt =0;
    int sumPurcClearCnt=0;
    int sumPurcRemoveCnt=0;

    int sumManageCancelCnt = 0;
    int sumManageSoldCnt = 0;
    int sumManageAppErrCnt = 0;

    int sumBrowseFwdCnt =0;
    int sumBrowseBkwdCnt =0;

    int sumHomeCnt = 0;
    int sumLoginCnt = 0;

    int sumBrseAutos = 0;
    int sumManage = 0;

    String txNames[] = {"Purc", "Manage", "Brse"};

    /* Stats for all transaction types */

    int txCnt[] = new int[DealerStats.TXTYPES];			/* number of transactions */
    int respMax[] = new int[DealerStats.TXTYPES];			/* Max. response time */
    double respSum[] = new double[DealerStats.TXTYPES];		/* Sum of response times */
    double cycleSum[] = new double[DealerStats.TXTYPES];		/* sum of cycle times */
    double targetedCycleSum[] = new double[DealerStats.TXTYPES];		/* sum of cycle times */
    int cycleMax[] = new int[DealerStats.TXTYPES];
    int cycleMin[] = new int[DealerStats.TXTYPES];
    double elapse[] = new double[DealerStats.TXTYPES];		/* sum of elapsed times */
    int respHist[][] = new int[DealerStats.TXTYPES][DealerStats.RESPMAX];/* Response time histogram */
    int cycleHist[][] = new int[DealerStats.TXTYPES][DealerStats.CYCLEMAX];	/* Think time histogram */
    int targetedCycleHist[][] = new int[DealerStats.TXTYPES][DealerStats.CYCLEMAX];	/* Think time histogram */
    int thruputHist[][] = new int[DealerStats.TXTYPES][DealerStats.THRUMAX];	/* Thruput histogram */

    /**
     * Constructs the DealerAggStats object.
     */
    public DealerAggStats() {
        for (int i = 0; i < cycleMin.length; i++)
            cycleMin[i] = Integer.MAX_VALUE;
    }

    /**
     * This method aggregates the stats of all the threads on this RTE machine.
     * It is called repeatedly, and the called passes it the stats of a different
     * thread, each time.
     * @param s Stats of next thread to be aggregated
     *
     */
    public void addResult(DealerStats s) {
        int j;
        txRate = s.txRate;
        threadCnt = s.threadCnt;
        stdyState = s.stdyState;
        resultsDir = s.resultsDir;

        /* Dealer info */

        /* purchase stats */
        sumPurcBadCredit += s.purcBadCredit;
        sumPurcLrgCount += s.purcLrgCnt;
        sumPurcOlCnt += s.purcOlCnt;
        sumPurcLrgOlCnt += s.purcLrgOlCnt;
        sumPurcCheckCnt += s.purcCheckCnt;
        sumPurcDeferCnt += s.purcDeferCnt;
        sumPurcClearCnt += s.purcClearCnt;
        sumPurcRemoveCnt += s.purcRemoveCnt;

        /* manage stats */

        sumManageCancelCnt += s.manageCancelCnt;
        sumManageSoldCnt += s.manageSoldCnt;
        sumManageAppErrCnt += s.manageAppErrCnt;

        /* browse stats */

        sumBrowseFwdCnt += s.browseFwdCnt;
        sumBrowseBkwdCnt += s.browseBkwdCnt;

        /* logins */
        sumLoginCnt += s.loginCnt;


        for (int i = 0; i < DealerStats.TXTYPES; i++) {
            txCnt[i] += s.txCnt[i];
            respSum[i] += s.respSum[i];
            cycleSum[i] += s.cycleSum[i];
            targetedCycleSum[i] += s.targetedCycleSum[i];
            if (s.respMax[i] > respMax[i])
                respMax[i] = s.respMax[i];
            if (s.cycleMax[i] > cycleMax[i])
                cycleMax[i] = s.cycleMax[i];
            if (s.cycleMin[i] < cycleMin[i])
                cycleMin[i] = s.cycleMin[i];

            // sum up histogram buckets
            for (j = 0; j < DealerStats.RESPMAX; j++)
                respHist[i][j] += s.respHist[i][j];
            for (j = 0; j < DealerStats.THRUMAX; j++)
                thruputHist[i][j] += s.thruputHist[i][j];
            for (j = 0; j < DealerStats.CYCLEMAX; j++)
                cycleHist[i][j] += s.cycleHist[i][j];
            for (j = 0; j < DealerStats.CYCLEMAX; j++)
                targetedCycleHist[i][j] += s.targetedCycleHist[i][j];
        }
    }

    /**
     * This method is used by the Agent to get a displayable
     * result for MWBench. In addition to computing a tps for display.
     * we use this method to write out the results to a file if in
     * debug mode.
     * @return The string representation of these stats
     */
    public String toString() {
        int i, j, totalCnt = 0;
        PrintStream p = System.out;
        double tps;

        for (i = 0; i < DealerStats.TXTYPES; i++)
            totalCnt += txCnt[i];
        tps = (double)(totalCnt) * 1000 / stdyState;

        /* Write out aggregate info into file */
        if (debug) {
            try {
                logfile = resultsDir + System.getProperty("file.separator") +
                        (InetAddress.getLocalHost()).getHostName() + ".log";
                p = new PrintStream(new FileOutputStream(logfile));
            } catch (Exception e) {
               e.printStackTrace();

            }

            p.println("sumusers=" + threadCnt);
            p.println("runtime=" + stdyState);
            p.println("sumPurcLrgCount=" + sumPurcLrgCount);
            p.println("sumPurcOlCnt=" + sumPurcOlCnt);
            p.println("sumPurcLrgOlCnt=" + sumPurcLrgOlCnt);

            for (i = 0; i < DealerStats.TXTYPES; i++) {
                p.println("sum" + txNames[i] + "Count=" + txCnt[i]);
                p.println("sum" + txNames[i] + "Resp=" + respSum[i]);
                p.println("max" + txNames[i] + "Resp=" + respMax[i]);
                p.println("sum" + txNames[i] + "Cycle=" + cycleSum[i]);
                p.println("max" + txNames[i] + "Cycle=" + cycleMax[i]);
                p.println("min" + txNames[i] + "Cycle=" + cycleMin[i]);
            }
            /* Now print out the histogram data */
            for (i = 0; i < DealerStats.TXTYPES; i++) {
                p.println(txNames[i] + " Response Times Histogram");
                for (j = 0; j < DealerStats.RESPMAX; j++)
                    p.print(" " + respHist[j]);
                p.println();
                p.println(txNames[i] + " Throughput Histogram");
                for (j = 0; j < DealerStats.THRUMAX; j++)
                    p.print(" " + thruputHist[j]);
                p.println();
                p.println(txNames[i] + " Cycle Times Histogram");
                for (j = 0; j < DealerStats.CYCLEMAX; j++)
                    p.print(" " + cycleHist[j]);
                p.println();
            }
            p.close();
        }
        return(Double.toString(tps));
    }
}
