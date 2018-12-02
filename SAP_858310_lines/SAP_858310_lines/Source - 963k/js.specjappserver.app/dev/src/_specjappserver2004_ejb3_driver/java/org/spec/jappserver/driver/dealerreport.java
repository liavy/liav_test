/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID        Company      Description
 *  ----------    --------  -------      ----------------------------------------------
 *  March, 2003   Tom Daly  Sun          Creation date, credit also to Shanti Subramanyam
 *                          Microsystems                as this code is based on OrdersReport
 *                                                      from ECperf and SPECjAppServer2001
 *  Nov , 2003    Tom Daly  Sun          Update to reflect txn weights of 25,25,50 and added
 *                                       pass/fail criteria to average vehicles/order
 *  Dec, 2003     Tom Daly  Sun          report login counts and remove time deltas
 *  Nov, 2004     John Stecher IBM       Added in real time statistics support
 *
 * $Id: DealerReport.java,v 1.14 2004/12/02 12:35:32 skounev Exp $
 *
 */
package org.spec.jappserver.driver;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is the Report generator for the Dealer application. 
 * The genReport method is called with an array of aggregate results
 * from each agent and it generates the output reports - 
 * a summary report and a detail report.
 * The summary report contains a summary of the numerical quantities -
 * transaction counts, rates, response and cycle times.
 * The detail report contains the histogram data to draw the various graphs -
 * throughput, response times and cycle times.
 *
 * @author Tom Daly (original credit Shanti Subramanyam)
 * @see SPECjAppServerReport
 * @see MfgReport
 */
public class DealerReport {
    String prefix;
    String summary, detail;


    //purchase
    int sumPurcBadCredit = 0;
    int sumPurcLrgCnt = 0, sumPurcOlCnt = 0, sumPurcLrgOlCnt = 0;
    int sumPurcCheckCnt =0;
    int sumPurcDeferCnt =0;
    int sumPurcClearCnt =0;
    int sumPurcRemoveCnt =0;

    double sumPurcResp = 0, sumPurcTargetedCycle = 0, sumPurcCycle = 0; 
    int sumPurcCount = 0, maxPurcResp = 0, maxPurcCycle = 0;
    int minPurcCycle = 100000;
    
    //manage
    int sumManageCancelCnt = 0;
    int sumManageSoldCnt;          
    int sumManageAppErrCnt;       
	
    double sumManageResp = 0, sumManageTargetedCycle = 0;
    double sumManageCycle = 0; 
    int sumManageCount = 0, maxManageResp = 0, maxManageCycle = 0, minManageCycle = 100000;

    //browse
    int sumBrowseFwdCnt;          
    int sumBrowseBkwdCnt;
    double sumBrseResp = 0, sumBrseTargetedCycle = 0, sumBrseCycle = 0; 
    int sumBrseCount = 0, maxBrseResp = 0, maxBrseCycle = 0, minBrseCycle = 100000;

    int sumLoginCnt=0;
    
    int sumPurcThruHist[] = new int[DealerStats.THRUMAX];
    int sumPurcRespHist[] = new int[DealerStats.RESPMAX];
    int sumPurcCycleHist[] = new int[DealerStats.CYCLEMAX];
    int sumPurcTargetedCycleHist[] = new int[DealerStats.CYCLEMAX];
    int sumManageThruHist[] = new int[DealerStats.THRUMAX];
    int sumManageRespHist[] = new int[DealerStats.RESPMAX];
    int sumManageCycleHist[] = new int[DealerStats.CYCLEMAX];
    int sumManageTargetedCycleHist[] = new int[DealerStats.CYCLEMAX];
    int sumBrseThruHist[] = new int[DealerStats.THRUMAX];
    int sumBrseRespHist[] = new int[DealerStats.RESPMAX];
    int sumBrseCycleHist[] = new int[DealerStats.CYCLEMAX];
    int sumBrseTargetedCycleHist[] = new int[DealerStats.CYCLEMAX];

    int users = 0, stdyState, txRate;

    List dumpStreams;
    int dumpInterval;
    int rampUp;
    int prevTxCnt = 0;
    double avgTps = 0;
    private static int elapsed = 0;
    private static int thruIndex = 0;
    double purchaseResp90, manageResp90, browseResp90;

    /**
     * Default constructor for DealerReport.
     */
    public DealerReport() {
    }

    /**
     * This constructor is used for dumping data for charting.
     * @param file
     * @param dumpInterval
     * @param rampUp
     * @throws IOException
     */
    public DealerReport(String file, int dumpInterval, int rampUp)
	throws IOException {
	dumpStreams = Collections.synchronizedList(new ArrayList());
	dumpStreams.add(new DataOutputStream(
					     new FileOutputStream(file)));
	this.dumpInterval = dumpInterval;
	this.rampUp = rampUp;
    }

    /**
     * New constructor used for dumping data for charting.
     * @param dumpStreams
     * @param dumpInterval
     * @param rampUp
     */
    public DealerReport(List dumpStreams, int dumpInterval, int rampUp) {
	this.dumpStreams = dumpStreams;
	this.dumpInterval = dumpInterval;
	this.rampUp = rampUp;
    }
    
    public void dumpRealTimeStats(DealerAggStats[] aggs, boolean display)
    {
       Format fmtr = new Format("%2.1f");
             
       int purchaseTxCnt=0;
       int purchaseRespHist[] = new int[DealerStats.RESPMAX];       
       int manageTxCnt=0;
       int manageRespHist[] = new int[DealerStats.RESPMAX];       
       int browseTxCnt=0;
       int browseRespHist[] = new int[DealerStats.RESPMAX];
       
       for(int i=0; i < aggs.length; i++)
       {
          purchaseTxCnt += aggs[i].txCnt[DealerStats.PURCHASE];                       
          manageTxCnt += aggs[i].txCnt[DealerStats.MANAGE];          
          browseTxCnt += aggs[i].txCnt[DealerStats.BROWSE];
          
          for(int j=0; j < DealerStats.RESPMAX; j++)
          {
             purchaseRespHist[j] += aggs[i].respHist[DealerStats.PURCHASE][j];             
             manageRespHist[j] += aggs[i].respHist[DealerStats.MANAGE][j];             
             browseRespHist[j] += aggs[i].respHist[DealerStats.BROWSE][j];
          }          
       }
       
       int purchaseCnt90 = (int)(purchaseTxCnt * .9);
       float purchaseResp90 = 0;
       int purchaseSumTx = 0;
       for(int i=0; purchaseSumTx < purchaseCnt90; i++)
       {
          purchaseSumTx += purchaseRespHist[i];
          purchaseResp90 += DealerStats.RESPUNIT;
       }
       
       int manageCnt90 = (int)(manageTxCnt * .9);
       float manageResp90 = 0;
       int manageSumTx = 0;
       for(int i=0; manageSumTx < manageCnt90; i++)
       {
          manageSumTx += manageRespHist[i];
          manageResp90 += DealerStats.RESPUNIT;
       }       
       
       int browseCnt90 = (int)(browseTxCnt * .9);
       float browseResp90 = 0;
       int browseSumTx = 0;
       for(int i=0; browseSumTx < browseCnt90; i++)
       {
          browseSumTx += browseRespHist[i];
          browseResp90 += DealerStats.RESPUNIT;
       }       
       
       if(display)
       {
          System.out.print("\rPurchase\\Manage\\Browse (TxCnt="+purchaseTxCnt+"\\"+manageTxCnt+"\\"+browseTxCnt+") 90% Resp="+fmtr.form(purchaseResp90)+"\\"+fmtr.form(manageResp90)+"\\"+fmtr.form(browseResp90)+"\t");
       }
       
       
       
    }
    
    /*
     * This method is called by the Driver every time it wants to dump
     * the thruput data out to files
     */
    void dumpStats(DealerAggStats[] aggs) {
        int txCnt = 0;
        double tps = 0;
        for (int stream = 0; stream < dumpStreams.size(); stream++) {
            DataOutputStream dumpStream = (DataOutputStream) dumpStreams.get(stream);
            try {
                dumpStream.writeDouble(elapsed);
            } catch (IOException e) {
                dumpStreams.remove(stream--);
                closeStream(dumpStream);
                e.printStackTrace();
                System.err.println("Error writing Dealer stats.\n" + "Closing stream and removing stream from list.\n"
                        + "Benchmark continues without interruption.");
            }
        }

	// Get the aggregate tx
	for (int i = 0; i < aggs.length; i++) {
	    txCnt += aggs[i].txCnt[DealerStats.PURCHASE];
	    txCnt += aggs[i].txCnt[DealerStats.MANAGE];
	    txCnt += aggs[i].txCnt[DealerStats.BROWSE];
	}

	// Dump the immediate tps;
	tps = (double) (txCnt - prevTxCnt) / dumpInterval;

	for (int stream = 0; stream < dumpStreams.size(); stream++) {
	    DataOutputStream dumpStream = (DataOutputStream)
		dumpStreams.get(stream);
	    try {
		dumpStream.writeDouble(tps);
	    } catch (IOException e) {
		dumpStreams.remove(stream--);
		closeStream(dumpStream);
		e.printStackTrace();
	    }
	}

	// Now dump out the old average tps
	for (int stream = 0; stream < dumpStreams.size(); stream++) {
	    DataOutputStream dumpStream = (DataOutputStream)
		dumpStreams.get(stream);
	    try {
		dumpStream.writeDouble(avgTps);
	    } catch (IOException e) {
		dumpStreams.remove(stream--);
		closeStream(dumpStream);
		e.printStackTrace();
	    }
	}

	elapsed += dumpInterval;

	for (int stream = 0; stream < dumpStreams.size(); stream++) {
	    DataOutputStream dumpStream = (DataOutputStream)
		dumpStreams.get(stream);
	    try {
		dumpStream.writeDouble(elapsed);
	    } catch (IOException e) {
		dumpStreams.remove(stream--);
		closeStream(dumpStream);
		e.printStackTrace();
		System.err.println("Error writing Dealer stats.\n" +
				   "Closing stream and removing stream from list.\n" +
				   "Benchmark continues without interruption.");
	    }
	}

	// Dump the immediate tps;
	for (int stream = 0; stream < dumpStreams.size(); stream++) {
	    DataOutputStream dumpStream = (DataOutputStream)
		dumpStreams.get(stream);
	    try {
		dumpStream.writeDouble(tps);
	    } catch (IOException e) {
		dumpStreams.remove(stream--);
		closeStream(dumpStream);
		e.printStackTrace();
	    }
	}

	// Now dump out the average tps
	if (elapsed <= rampUp)
	    avgTps = 0;
	else
	    avgTps = (double)txCnt / (elapsed - rampUp);

	for (int stream = 0; stream < dumpStreams.size(); stream++) {
	    DataOutputStream dumpStream = (DataOutputStream)
		dumpStreams.get(stream);
	    try {
		dumpStream.writeDouble(avgTps);
		dumpStream.flush();
	    } catch (IOException e) {
		dumpStreams.remove(stream--);
		closeStream(dumpStream);
		e.printStackTrace();
	    }
	}

	thruIndex++;	// each time it is called, we dump the next interval
	prevTxCnt = txCnt;
    }

    private void closeStream(OutputStream s) {
	try {
	    s.close();
	} catch (IOException e) {
        // $JL-EXC$
	}
    }


    /** Method : genReport
     * This method is called from SPECjAppServerReport to generate the report
     * for the DealerApp.
     * @return double - txPerSec
     * @param propp
     * @param aggs - Array of DealerAggStats objects, one from each agent
     * @throws IOException
     */
    public double genReport(DealerAggStats[] aggs, PrintStream propp) throws IOException {

	String resultsDir = aggs[0].resultsDir;
	String filesep = System.getProperty("file.separator");
	summary = resultsDir + filesep + "Dealer.summary";
	detail = resultsDir + filesep + "Dealer.detail";
	PrintStream sump = new PrintStream(new FileOutputStream(summary));
	PrintStream detailp = new PrintStream(new FileOutputStream(detail));
	int i = 0;

	for (i = 0; i < aggs.length; i++) {
	    //	bufp = new BufferedReader(new FileReader(files[i]));
	    processStats(aggs[i]);
	    users += aggs[i].threadCnt;
	    //	bufp.close();
	}
	stdyState = aggs[0].stdyState;
	txRate = aggs[0].txRate;
	Debug.println("DealerReport: Printing Summary report...");

	double txPerSec = printSummary(sump, propp);
	Debug.println("DealerReport: Summary finished. Now printing detail ...");
	printDetail(detailp);
	return(txPerSec);
    }


    private void processStats(DealerAggStats agg) {
	int j;

	//get purchase sums
	sumPurcCount += agg.txCnt[DealerStats.PURCHASE]; //# of purchase txns
	sumPurcBadCredit += agg.sumPurcBadCredit;		
	sumPurcLrgCnt += agg.sumPurcLrgCount;
	sumPurcOlCnt += agg.sumPurcOlCnt;
	sumPurcLrgOlCnt += agg.sumPurcLrgOlCnt;
	sumPurcCheckCnt += agg.sumPurcCheckCnt;
	sumPurcDeferCnt += agg.sumPurcDeferCnt;
	sumPurcClearCnt += agg.sumPurcClearCnt;
	sumPurcRemoveCnt += agg.sumPurcRemoveCnt;

	sumPurcResp += agg.respSum[DealerStats.PURCHASE];
	if ( agg.respMax[DealerStats.PURCHASE] > maxPurcResp)
	    maxPurcResp = agg.respMax[DealerStats.PURCHASE];
	sumPurcCycle += agg.cycleSum[DealerStats.PURCHASE];
	sumPurcTargetedCycle += agg.targetedCycleSum[DealerStats.PURCHASE];
	if ( agg.cycleMax[DealerStats.PURCHASE] > maxPurcCycle)
	    maxPurcCycle = agg.cycleMax[DealerStats.PURCHASE];
	if ( agg.cycleMin[DealerStats.PURCHASE] < minPurcCycle)
	    minPurcCycle = agg.cycleMin[DealerStats.PURCHASE];

	//get manage sums
	sumManageCancelCnt += agg.sumManageCancelCnt;        
	sumManageSoldCnt += agg.sumManageSoldCnt;         
	sumManageAppErrCnt += agg.sumManageAppErrCnt;  
	sumManageCount += agg.txCnt[DealerStats.MANAGE];   //#of manage txns

	sumManageResp += agg.respSum[DealerStats.MANAGE];
	if ( agg.respMax[DealerStats.MANAGE] > maxManageResp)
	    maxManageResp = agg.respMax[DealerStats.MANAGE];
	sumManageCycle += agg.cycleSum[DealerStats.MANAGE];
	sumManageTargetedCycle += agg.targetedCycleSum[DealerStats.MANAGE];
	if (agg.cycleMax[DealerStats.MANAGE] > maxManageCycle)
	    maxManageCycle = agg.cycleMax[DealerStats.MANAGE];
	if ( agg.cycleMin[DealerStats.MANAGE] < minManageCycle)
	    minManageCycle = agg.cycleMin[DealerStats.MANAGE];

	//get browse sums
	sumBrseCount += agg.txCnt[DealerStats.BROWSE];
	sumBrowseFwdCnt += agg.sumBrowseFwdCnt;           
	sumBrowseBkwdCnt += agg.sumBrowseBkwdCnt;     

	sumBrseResp += agg.respSum[DealerStats.BROWSE];
	if ( agg.respMax[DealerStats.BROWSE] > maxBrseResp)
	    maxBrseResp = agg.respMax[DealerStats.BROWSE];
	sumBrseCycle += agg.cycleSum[DealerStats.BROWSE];
	sumBrseTargetedCycle += agg.targetedCycleSum[DealerStats.BROWSE];
	if (agg.cycleMax[DealerStats.BROWSE] > maxBrseCycle)
	    maxBrseCycle = agg.cycleMax[DealerStats.BROWSE];
	if ( agg.cycleMin[DealerStats.BROWSE] < minBrseCycle)
	    minBrseCycle = agg.cycleMin[DealerStats.BROWSE];

        
        //logins 
        sumLoginCnt += agg.sumLoginCnt;
        
        
	/* Now get the histogram data */
	for (j = 0; j < DealerStats.RESPMAX; j++)
	    sumPurcRespHist[j] += agg.respHist[DealerStats.PURCHASE][j];
	for (j = 0; j < DealerStats.THRUMAX; j++)
	    sumPurcThruHist[j] += agg.thruputHist[DealerStats.PURCHASE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumPurcCycleHist[j] += agg.cycleHist[DealerStats.PURCHASE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumPurcTargetedCycleHist[j] += agg.targetedCycleHist[DealerStats.PURCHASE][j];

	for (j = 0; j < DealerStats.RESPMAX; j++)
	    sumManageRespHist[j] += agg.respHist[DealerStats.MANAGE][j];
	for (j = 0; j < DealerStats.THRUMAX; j++)
	    sumManageThruHist[j] += agg.thruputHist[DealerStats.MANAGE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumManageCycleHist[j] += agg.cycleHist[DealerStats.MANAGE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumManageTargetedCycleHist[j] += agg.targetedCycleHist[DealerStats.MANAGE][j];

	for (j = 0; j < DealerStats.RESPMAX; j++)
	    sumBrseRespHist[j] += agg.respHist[DealerStats.BROWSE][j];
	for (j = 0; j < DealerStats.THRUMAX; j++)
	    sumBrseThruHist[j] += agg.thruputHist[DealerStats.BROWSE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumBrseCycleHist[j] += agg.cycleHist[DealerStats.BROWSE][j];
	for (j = 0; j < DealerStats.CYCLEMAX; j++)
	    sumBrseTargetedCycleHist[j] += agg.targetedCycleHist[DealerStats.BROWSE][j];
    }

    // Print summary report
    private double printSummary(PrintStream p, PrintStream propp) {
	double txcnt = 0, txPerSec = 0;
	double purcPer = 0, managePer = 0, brsePer = 0;
	boolean success = true;
	double avg, tavg, resp90;
	int sumtx, cnt90;
	boolean fail90 = false, failavg =false;
	int i;
	String passStr = null;
	p.println();
	p.println("\t\t\tDealer Summary Report");
	p.println("\t\t\tVersion : " + SPECjAppServerReport.version);
	p.println();

	txcnt = sumPurcCount + sumManageCount + sumBrseCount ;
	if(txcnt > 0) {
	    txPerSec = txcnt * 1000 / stdyState;
	    purcPer = (sumPurcCount*100) / txcnt;
	    managePer = (sumManageCount*100) / txcnt;
	    brsePer = (sumBrseCount*100) / txcnt;
	}
	p.print("Dealer Transaction Rate : "); 
	Debug.println("Transactions/sec = " + txPerSec);
	Format.print(p, "%.02f Transactions/sec", txPerSec);
	p.println();
	p.println();
	p.println("TRANSACTION MIX\n");
	p.println("Total number of transactions = " + (int)txcnt);
	p.println("TYPE\t\tTX. COUNT\tMIX\t\tREQD. MIX.(5% Deviation Allowed)");
	p.println("----\t\t---------\t---\t\t----------");
	Format.print(p, "Purchase:\t%05d\t\t", sumPurcCount);
	Format.print(propp,"result.dealer.purchase.count=%d\n", sumPurcCount);
	//TD TODO old weights if (purcPer < 47.5 || purcPer > 52.5) {
        if (purcPer < 23.75 || purcPer > 26.25 ) { 
	    success = false;
	    passStr = "FAILED";
	}
	else 
	    passStr = "PASSED";
	Format.print(p, "%5.02f%\t\t25%\t", purcPer);
	Format.print(p, "%s\n", passStr);
	Format.print(p, "Manage     :\t%05d\t\t", sumManageCount);
	Format.print(propp,"result.dealer.manage.count=%d\n", sumManageCount);
        
        //TD TODO was 19, 21 
	if (managePer < 23.75 || managePer > 26.25 ) {
	    success = false;
	    passStr = "FAILED";
	}
	else 
	    passStr = "PASSED";
	Format.print(p, "%5.02f%\t\t25%\t", managePer);
	Format.print(p, "%s\n", passStr);
	Format.print(p, "Browse Autos:\t%05d\t\t", sumBrseCount);
	Format.print(propp,"result.dealer.browse.count=%d\n", sumBrseCount);

	if (brsePer < 47.5 || brsePer > 52.5 )  {
	    success = false;
	    passStr = "FAILED";
	}
	else 
	    passStr = "PASSED";
	Format.print(p, "%5.02f%\t\t50%\t", brsePer);
	Format.print(p, "%s\n\n", passStr);

	/* Compute response time info */
	p.println("RESPONSE TIMES\t\tAVG.\t\tMAX.\t\t90TH%\tREQD. 90TH%\n");
	if (sumPurcCount > 0) {
	    avg  = (sumPurcResp/sumPurcCount) / 1000;
	    sumtx = 0;
	    cnt90 = (int)(sumPurcCount * .90);
	    for (i = 0; i < DealerStats.RESPMAX; i++) {
		sumtx += sumPurcRespHist[i];
		if (sumtx >= cnt90)		/* 90% of tx. got */
		    break;
	    }
	    resp90 = (i + 1) * DealerStats.RESPUNIT;
	    if (resp90 > DealerStats.PURCHASEFAST)
		fail90 = true;
	    if (avg > (resp90 + 0.1))
		failavg = true;
	    Format.print(p, "Purchase\t\t%.03f\t\t", avg);
	    Format.print(p, "%.03f\t\t", (double)maxPurcResp/1000);
	    Format.print(p, "%.03f\t\t", resp90);
	    purchaseResp90 = resp90;
	    p.println(DealerStats.PURCHASEFAST);

	    // Following added for SPECjAppServer - Shanti
	    propp.print("result.dealer.purchase.resp_time.avg=");
	    Format.print(propp, "%.03f\n",avg);
	    propp.print("result.dealer.purchase.resp_time.max=");
	    Format.print(propp, "%.03f\n", (double)maxPurcResp/1000);
	    propp.print("result.dealer.purchase.resp_time.90p=");
	    Format.print(propp, "%.03f\n", resp90);
	}
	else {
	    p.println("Purchase\t\t0.000\t\t0.000\t\t0.000\n");
	    propp.println("result.dealer.purchase.resp_time.avg=0.0");
	    propp.println("result.dealer.purchase.resp_time.max=0.0");
	    propp.println("result.dealer.purchase.resp_time.90p=0.0");
	}
	if (sumManageCount > 0) {
	    avg  = (sumManageResp/sumManageCount) / 1000;
	    sumtx = 0;
	    cnt90 = (int)(sumManageCount * .90);
	    for (i = 0; i < DealerStats.RESPMAX; i++) {
		sumtx += sumManageRespHist[i];
		if (sumtx >= cnt90)		/* 90% of tx. got */
		    break;
	    }
	    resp90 = (i + 1) * DealerStats.RESPUNIT;
	    if (resp90 > DealerStats.PURCHASEFAST)
		fail90 = true;
	    if (avg > (resp90 + 0.1))
		failavg = true;
	    Format.print(p, "Manage       \t\t%.03f\t\t", avg);
	    Format.print(p, "%.03f\t\t", (double)maxManageResp/1000);
	    Format.print(p, "%.03f\t\t", resp90);
	    manageResp90 = resp90;
	    p.println(DealerStats.MANAGEFAST);

	    propp.print("result.dealer.manage.resp_time.avg=");
	    Format.print(propp, "%.03f\n", avg);
	    propp.print("result.dealer.manage.resp_time.max=");
	    Format.print(propp, "%.03f\n", (double)maxManageResp/1000);
	    propp.print("result.dealer.manage.resp_time.90p=");
	    Format.print(propp, "%.03f\n", resp90);
	}
	else {
	    p.println("Manage    \t\t0.000\t\t0.000\t\t0.000\n");
	    propp.println("result.dealer.manage.resp_time.avg=0.0");
	    propp.println("result.dealer.manage.resp_time.max=0.0");
	    propp.println("result.dealer.manage.resp_time.90p=0.0");
	}
	if (sumBrseCount > 0) {
	    avg  = (sumBrseResp/sumBrseCount) / 1000;
	    sumtx = 0;
	    cnt90 = (int)(sumBrseCount * .90);
	    for (i = 0; i < DealerStats.RESPMAX; i++) {
		sumtx += sumBrseRespHist[i];
		if (sumtx >= cnt90)		/* 90% of tx. got */
		    break;
	    }
	    resp90 = (i + 1) * DealerStats.RESPUNIT;
	    if (resp90 > DealerStats.PURCHASEFAST)
		fail90 = true;
	    if (avg > (resp90 + 0.1))
		failavg = true;
	    Format.print(p, "Browse  \t\t%.03f\t\t", avg);
	    Format.print(p, "%.03f\t\t", (double)maxBrseResp/1000);
	    Format.print(p, "%.03f\t\t", resp90);
	    browseResp90 = resp90;
	    p.println(DealerStats.BROWSEFAST);

	    // Following added for SPECjAppServer - Shanti
	    propp.print("result.dealer.browse.resp_time.avg=");
	    Format.print(propp, "%.03f\n", avg);
	    propp.print("result.dealer.browse.resp_time.max=");
	    Format.print(propp, "%.03f\n", (double)maxBrseResp/1000);
	    propp.print("result.dealer.browse.resp_time.90p=");
	    Format.print(propp, "%.03f\n", resp90);
	}
	else {
	    p.println("Browse   \t\t0.000\t\t0.000\t\t0.000\n");
	    propp.println("result.dealer.browse.resp_time.avg=0.0");
	    propp.println("result.dealer.browse.resp_time.max=0.0");
	    propp.println("result.dealer.browse.resp_time.90p=0.0");
	}

	if (fail90)
	    p.println("Requirement for 90% Response Time FAILED");
	else
	    p.println("Requirement for 90% Response Time PASSED");
	if (failavg)
	    p.println("Requirement for Avg. Response Time FAILED\n\n");
	else
	    p.println("Requirement for Avg. Response Time PASSED\n\n");
		
		
	p.println("CYCLE TIMES\tTARGETED AVG.\tACTUAL AVG.\tMIN.\t\tMAX.\n");
	if (sumPurcCount > 0) {
	    avg = sumPurcCycle / sumPurcCount;
	    tavg = sumPurcTargetedCycle / sumPurcCount;
	    Format.print(p, "Purchase\t%6.3f\t\t", tavg/1000);
	    Format.print(p, "%6.3f\t\t", avg/1000);
	    Format.print(p, "%5.3f\t\t", (double)minPurcCycle/1000);
	    Format.print(p, "%6.3f\t\t", (double)maxPurcCycle/1000);
	    if (Math.abs(avg - tavg)/tavg <= .05)
		p.println("PASSED");
	    else
		p.println("FAILED");
	}
	else
	    p.println("Purchase\t0.000\t0.000");
	if (sumManageCount > 0) {
	    avg = sumManageCycle / sumManageCount;
	    tavg = sumManageTargetedCycle / sumManageCount;
	    Format.print(p, "Manage    \t%6.3f\t\t", tavg/1000);
	    Format.print(p, "%6.3f\t\t", avg/1000);
	    Format.print(p, "%5.3f\t\t", (double)minManageCycle/1000);
	    Format.print(p, "%6.3f\t\t", (double)maxManageCycle/1000);
	    if (Math.abs(avg - tavg)/tavg <= .05)
		p.println("PASSED");
	    else
		p.println("FAILED");
	}
	else
	    p.println("Manage    \t0.000\t0.000");
	if (sumBrseCount > 0) {
	    avg = sumBrseCycle / sumBrseCount;
	    tavg = sumBrseTargetedCycle / sumBrseCount;
	    Format.print(p, "Browse    \t%6.3f\t\t", tavg/1000);
	    Format.print(p, "%6.3f\t\t", avg/1000);
	    Format.print(p, "%5.3f\t\t", (double)minBrseCycle/1000);
	    Format.print(p, "%6.3f\t\t", (double)maxBrseCycle/1000);
	    if (Math.abs(avg - tavg)/tavg <= .05)
		p.println("PASSED");
	    else
		p.println("FAILED");
	}
	else
	    p.println("Browse    \t0.000\t0.000");

	p.println("\nMISC. STATISTICS\n");

	if (sumPurcCount > 0) {
	    avg = (double)(sumPurcOlCnt) / sumPurcCount;
	    double vehicles = avg * txPerSec * purcPer/100;
	    Format.print(p, "Average vehicles per order\t\t\t%5.3f", avg);
  	    if (avg < 25.27 || avg  > 27.93)
		p.println("\t\tFAILED");
	    else
		p.println("\t\tPASSED");          
	    Format.print(p, "Vehicle Purchasing Rate\t\t\t\t%5.3f/sec", vehicles);
	    if (vehicles < 6.32 * txRate || vehicles > 6.98 * txRate)
		p.println("\tFAILED");
	    else
		p.println("\tPASSED");
	    double percentLrgDealer = (double)(sumPurcLrgCnt) * 100.0 / sumPurcCount;
	    Format.print(p, "Percent Purchases that are Large Orders \t%3.2f", percentLrgDealer);
	    if (percentLrgDealer < 9.5 || percentLrgDealer > 10.5)
		p.println("\t\tFAILED");
	    else
		p.println("\t\tPASSED");
	    avg = (double)(sumPurcLrgOlCnt) / sumPurcLrgCnt;
	    vehicles = avg * txPerSec * purcPer * percentLrgDealer/(100 * 100);
	    Format.print(p, "Average # of vehicles per Large Order\t\t%5.3f", avg);
	    if (avg < 133 || avg > 147)
		p.println("\t\tFAILED");
	    else
		p.println("\t\tPASSED");
	    Format.print(p, "Largeorder Vehicle Purchase Rate\t\t%5.3f/sec", vehicles);
	    if (vehicles < 3.33 * txRate || vehicles > 3.68 * txRate)
		p.println("\tFAILED");
	    else
		p.println("\tPASSED");
	    avg = (double)(sumPurcOlCnt - sumPurcLrgOlCnt) / (sumPurcCount - sumPurcLrgCnt);
	    Format.print(p, "Average # of vehicles per regular order \t%5.3f", avg);
	    if (avg < 13.3 || avg > 14.7)
		p.println("\t\tFAILED");
	    else
		p.println("\t\tPASSED");
	    avg = avg * (100 - percentLrgDealer)/100;	// regular orders
	    vehicles = avg * txPerSec * purcPer /100;
	    Format.print(p, "Regular Vehicle Purchase Rate\t\t\t%5.3f/sec", vehicles);
	    if (vehicles < 2.99 * txRate || vehicles > 3.31 * txRate)
		p.println("\tFAILED");
	    else
		p.println("\tPASSED");

	    p.println("\n\n -- SUNDRY COUNTS (no pass/fail) -- \n");	    
	    Format.print(p, "Purchase: denied due to bad credit\t%05d\n",sumPurcBadCredit );
	    Format.print(p, "Purchase: vehicles immediate txns\t%05d\n",sumPurcCheckCnt );
	    Format.print(p, "Purchase: vehicles deferred txns\t%05d\n",sumPurcDeferCnt );
	    Format.print(p, "Purchase: cart clears \t\t\t%05d\n",sumPurcClearCnt );
	    Format.print(p, "Purchase: vehicles removed from cart \t%05d\n",sumPurcRemoveCnt );

	    Format.print(p, "Manage: open orders cancelled \t\t%05d\n",sumManageCancelCnt );
	    Format.print(p, "Manage: vehicles sold from lot \t\t%05d\n",sumManageSoldCnt );
            Format.print(p, "Manage: average vehicles sold per txn \t%05d\n",sumManageSoldCnt/sumManageCount);
	    Format.print(p, "Manage: application level errors \t%05d\n",sumManageAppErrCnt );

	    Format.print(p, "Browse: forwards \t\t\t%05d\n",sumBrowseFwdCnt );
	    Format.print(p, "Browse: backwards \t\t\t%05d\n",sumBrowseBkwdCnt );
            Format.print(p, "Logins:            \t\t\t%5d\n", sumLoginCnt);
	}

	p.println("\n\nLITTLE'S LAW VERIFICATION\n\n");
	p.println("Number of users = " + users);

	/* avg.rt = cycle time = tx. rt + cycle time */
	Format.print(p, "Sum of Avg. RT * TPS for all Tx. Types = %f\n",
		     ((sumPurcCycle + sumManageCycle + sumBrseCycle ) /
		      stdyState));
	return(txPerSec);
    }

    /** This method prints the detailed report. This data is used to generate
     * graphs of throughput, response times and cycle times
     * @param p
     */
    private void printDetail(PrintStream p) {
	int i, j;
	double f;

	p.println("                   Dealer Detailed Report\n");
	p.println("Purchase Throughput");
	p.println("TIME COUNT OF TX.");
	for (i = 0, j = 0; i < DealerStats.THRUMAX; i++, j+= DealerStats.THRUUNIT) {
	    if(sumPurcThruHist[i] == 0)
		break;
	    p.println(j + "\t" +  sumPurcThruHist[i]);
	}
	p.println("Manage     Throughput");
	p.println("TIME COUNT OF TX.");
	for (i = 0, j = 0; i < DealerStats.THRUMAX; i++, j+= DealerStats.THRUUNIT) {
	    if(sumManageThruHist[i] == 0)
		break;
	    p.println(j + "\t" +  sumManageThruHist[i]);
	}
	p.println("Browse Throughput");
	p.println("TIME COUNT OF TX.");
	for (i = 0, j = 0; i < DealerStats.THRUMAX; i++, j+= DealerStats.THRUUNIT) {
	    if(sumBrseThruHist[i] == 0)
		break;
	    p.println(j + "\t" +  sumBrseThruHist[i]);
	}

	p.println("\n\nFrequency Distribution of Response Times");
	p.println("\nPURCHASE");
	for (i = 0, f = 0; i < DealerStats.RESPMAX; i++, f+= DealerStats.RESPUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumPurcRespHist[i]);
	}
	p.println("\nMANAGE");
	for (i = 0, f = 0; i < DealerStats.RESPMAX; i++, f+= DealerStats.RESPUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumManageRespHist[i]);
	}
	p.println("\nBROWSE");
	for (i = 0, f = 0; i < DealerStats.RESPMAX; i++, f+= DealerStats.RESPUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumBrseRespHist[i]);
	}
	
	p.println("\n\nFrequency Distribution of Cycle Times");
	p.println("\nPURCHASE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumPurcCycleHist[i]);
	}
	p.println("\nMANAGE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumManageCycleHist[i]);
	}
	p.println("\nBROWSE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumBrseCycleHist[i]);
	}

	p.println("\n\nFrequency Distribution of Targeted Cycle Times");
	p.println("\nPURCHASE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumPurcTargetedCycleHist[i]);
	}
	p.println("\nMANAGE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumManageTargetedCycleHist[i]);
	}
	p.println("\nBROWSE");
	for (i = 0, f = 0; i < DealerStats.CYCLEMAX; i++, f+= DealerStats.CYCLEUNIT) {
	    Format.print(p, "%5.3f\t", f);
	    p.println(sumBrseTargetedCycleHist[i]);
	}

	p.close();
    }



}
