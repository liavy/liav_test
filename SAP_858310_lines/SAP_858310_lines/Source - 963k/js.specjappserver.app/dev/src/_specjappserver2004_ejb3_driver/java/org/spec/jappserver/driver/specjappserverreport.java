/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID         Company      Description
 *  ----------    --------   -------      ----------------------------------------------
 *  March, 2003   Tom Daly   Sun          Creation date, credit also to Shanti Subramanyam
 *                           Microsystems                as this code is based on 
 *                                                      SPECjAppServerReport
 *                                                      from ECperf and SPECjAppServer2001
 *  Feb, 2004     Sam Kounev Darmstadt    Updated benchmark name to SPECjAppServer2004
 *
 * $Id: SPECjAppServerReport.java,v 1.23 2005/03/07 13:01:48 skounev Exp $
 *
 */
package org.spec.jappserver.driver;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * This class is the Report generator for the SPECjAppServer experiment.
 * It generates the reports from the DealerEntry and Mfg workload runs
 * and also the final cumulative report.
 *
 * @see DealerReport
 * @see MfgReport
 * @author Tom Daly
 */
public class SPECjAppServerReport {
	// Change version number below when generating a new driver version
	public static String version = "SPECjAppServer2004 v1.05";
	String summary;
	double dlrTxRate, mfgTxRate;
	int users, stdyState;
	boolean dodlr = false, domfg = false;
	RunInfo runInfo;

        // Added by Ramesh to get txCounts for Auditing
        DealerReport dlrReport = null;
        MfgReport    mfgReport  = null;

	/**
	 * Method : generateReport
	 * This is the method required by the Reporter interface to generate
	 * the final reports for a particular MWBench run.
	 * A seperate set of reports is generated for each set within
	 * the Experiment. All the reports live in a run directory
	 * (called <username>.<runid>) in the home directory
	 * of the user running the experiment.
	 * The final summary report is in a file called
	 * SPECjAppServer.summary<setnum> where the suffix setnum refers to the set
	 * within the experiment for which this report was generated.
     * @param runInfo The run information
     * @param dealerResults The aggregated dealer stats
     * @param mfgResults The aggregated manufacturing stats
     */
	public void generateReport(RunInfo runInfo,
		DealerAggStats dealerResults[], MfgStats mfgResults[]) {
		this.runInfo = runInfo;
		String resultsDir = null;
		String filesep = System.getProperty("file.separator");
		String propfile;
		PrintStream propp = null;

		/*
		 * Each element in the results vector is the result from
		 * one agent (repeated across sets). Each element is an array 
		 * of serializables, one per workload.
		 * We walk through the results vector to find out how many
		 * results are from the DealersApp.
		 */

		int numDlr = 0, numMfg = 0;
		if (dealerResults != null) {
			numDlr = dealerResults.length;
			dodlr = true;
		}
		if (mfgResults != null) {
			numMfg = mfgResults.length;
			domfg = true;
		}
		if (numDlr > 0) {
			Debug.println("SPECjAppServerReport: Printing report from " + 
				numDlr + " DealersAgents");
			resultsDir = (dealerResults[0]).resultsDir;
			/* Shanti
                 	* Added following for printing result properties for reporter
		 	*/
                	propfile = resultsDir + filesep + "result.props";
			try {
				propp = new PrintStream(new FileOutputStream(propfile));
			} catch (IOException ie) {
				System.out.println("Could not create result.props file");
				ie.printStackTrace();
			}
			try {
                                dlrReport = new DealerReport();
				dlrTxRate = dlrReport.genReport(dealerResults, propp);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
		if (numMfg > 0) {
			Debug.println("SPECjAppServerReport: Printing report from " + numMfg +
				" Mfg Agents");
			resultsDir = (mfgResults[0]).resultsDir;
			if (propp == null) {
                		propfile = resultsDir + filesep + "result.props";
				try {
				propp = new PrintStream(new FileOutputStream(propfile));
				} catch (IOException ie) {
				System.out.println("Could not create result.props file");
				ie.printStackTrace();
				}
			}
			try {
                                mfgReport = new MfgReport();
				mfgTxRate = mfgReport.genReport(mfgResults, runInfo.txRate, propp);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
		summary = resultsDir + filesep + "SPECjAppServer.summary";
		System.out.println("summary file is " + summary);
		try {
			PrintStream sump = new PrintStream(new FileOutputStream(summary));
			printSummary(sump, propp);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}



	// Print summary report
	private void printSummary(PrintStream p, PrintStream propp) {
		p.println();
		p.println("\t\t\tSPECjAppServer2004 Summary Report");
		p.println("\t\t\tVersion : " + version);
		p.println("\n");

		propp.println("benchmark.version=" + version);
		propp.println();

		p.println("Run Parameters : ");
		p.print("runDealerEntry = ");
		Format.print(p, "%d", runInfo.runDealerEntry);
		p.println();
		p.print("runMfg = ");
		Format.print(p, "%d", runInfo.runMfg);
		p.println();
		if (runInfo.runLargeOrderLine == false)
			p.println("LargeOrderLine was not run. Non-compliant run");
		p.print("txRate = ");
		Format.print(p, "%d", runInfo.txRate);
		propp.print("config.injection_rate=");
		Format.print(propp, "%d", runInfo.txRate);
		p.println();
		propp.println();
		p.print("rampUp (in seconds) = ");
		propp.print("config.ramp_up_seconds=");
		Format.print(p, "%d", runInfo.rampUp/1000);
		Format.print(propp, "%d", runInfo.rampUp/1000);
		p.println();
		propp.println();
		p.print("rampDown (in seconds) = ");
		propp.print("config.ramp_down_seconds=");
		Format.print(p, "%d", runInfo.rampDown/1000);
		Format.print(propp, "%d", runInfo.rampDown/1000);
		p.println();
		propp.println();
		p.print("stdyState (in seconds) = ");
		propp.print("config.steady_state_seconds=");
		Format.print(p, "%d", runInfo.stdyState/1000);
		Format.print(propp, "%d", runInfo.stdyState/1000);
		p.println();
		propp.println();
		p.print("triggerTime (in seconds) = ");
		propp.print("config.trigger_time_seconds=");
		Format.print(p, "%d", runInfo.triggerTime);
		Format.print(propp, "%d", runInfo.triggerTime);
		p.println();
		propp.println();
		// p.print("benchStartTime = ");
		// Format.print(p, "%d", runInfo.benchStartTime);
		// p.println();
		p.print("numDealerAgents = ");
		propp.print("config.dealer_agents=");
		Format.print(p, "%d", runInfo.numDealerAgents);
		Format.print(propp, "%d", runInfo.numDealerAgents);
		propp.println();
		p.print(", numMfgAgents = ");
		propp.print("config.mfg_agents=");
		Format.print(p, "%d", runInfo.numMfgAgents);
		Format.print(propp, "%d", runInfo.numMfgAgents);
		p.println();
		propp.println();
		propp.println();
		p.print("dumpStats = ");
		Format.print(p, "%d", runInfo.dumpStats);
		p.println();
		Date d = new Date(runInfo.start);
		p.println("Benchmark Started At : " + d.toString());
		propp.println("result.start_time=" + d.toString());
		p.println();
		p.println();
		if (dodlr) {
		p.println("Dealer Summary report is in : Dealer.summary");
		p.println("Dealer Detailed report is in : Dealer.detail");
		p.print("Dealer Transaction Rate : "); 
		Format.print(p, "%.02f Transactions/sec", dlrTxRate);
		p.println();
		p.println();
		}
		if (domfg) {
		p.println("Manufacturing Summary report is in : Mfg.summary");
		p.println("Manufacturing Detail report is in : Mfg.detail");
		p.print("Manufaturing Rate : "); 
		Format.print(p, "%.02f WorkOrders/sec", mfgTxRate);
		p.println();
		p.println();
		}
		if (dodlr && domfg) {
			p.print("SPECjAppServer2004 Metric : ");	
			Format.print(p, "%.02f JOPS", dlrTxRate + mfgTxRate);
			p.println();
			p.println();
		}
		p.close();
		propp.close();
	}

}
