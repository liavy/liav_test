/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID                         Description
 *  ----------    --------                   --------------------------------------------------
 *  2001/../..    Shanti Subramanyam, SUN    Created.
 *  2002/04/12    Matt Hogstrom, IBM         Conversion from ECperf 1.1 to SPECjAppServer2001.
 *  2002/07/10    Russel Raymundo, BEA       Conversion from SPECjAppServer2001 to 
 *                                           SPECjAppServer2002 (EJB2.0).
 *  2003/06/03    Tom Daly, SUN              Conversion to SPECjAppServer2004.
 *  2003/11/03    Tom Daly, SUN              Modify largeorder pass/fail criteria to match 
 *                                           new 25,25,50 weights.
 *  2004/01/08    Akara Sucharitakul, SUN    Changes for new pDemand model (osgjava-6255) - 
 *                                           new planned line passing vehicle rate is between 
 *                                           2.835 and 3.465 vehicles/sec.
 *  2004/11/30    John Stecher IBM           Added in real time statistics support
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
 * This class is the Report generator for the Mfg application. 
 * The genReport method is called with the aggregated MfgStat result 
 * and it generates the output reports - a summary report and a detail report.
 * The summary report contains a summary of the numerical quantities -
 * transaction counts, rates, response and think times.
 * The detail report contains the histogram data to draw the various graphs -
 * throughput, response times and think times.
 *
 * @author Shanti Subramanyam
 * @see SPECjAppServerReport
 * @see DealerReport
 */
public class MfgReport {
	String prefix;
	String summary, detail;
	int stdyState;

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
	double resp90;

    /**
     * Constructs the report object.
     */
	public MfgReport() {
	}


        List dumpStreams;
        int dumpInterval;
        int rampUp;
        int prevTxCnt = 0;
        double avgTps = 0;
	private static int elapsed = 0;

	/**
	 * This constructor is used for dumping data for charting.
     * @param file The file name to output the dump
     * @param dumpInterval The time interval between output
     * @param rampUp the rampup time
     * @throws IOException An error occurred writing out data
     */
	public MfgReport(String file, int dumpInterval, int rampUp)
                        throws IOException {
                dumpStreams = Collections.synchronizedList(new ArrayList());
                dumpStreams.add(new DataOutputStream(
                                         new FileOutputStream(file)));
                this.dumpInterval = dumpInterval;
                this.rampUp = rampUp;
	}

    /**
     * New constructor is used for dumping data for charting.
     * @param dumpStreams The output streams used for dumping
     * @param dumpInterval The time interval for dumping
     * @param rampUp The rampup time
     */
        public MfgReport(List dumpStreams, int dumpInterval, int rampUp) {
                this.dumpStreams = dumpStreams;
                this.dumpInterval = dumpInterval;
                this.rampUp = rampUp;
        }

	/**
	 * This method is called from SPECjAppServerReport to generate the report
	 * for the MfgApp.
     * @param aggs MfgStats object returned from Planned and LargeOrderlines
     * @param txRate The injection rate for this run
     * @param propp The print stream to output the report
     * @return WorkOrdersPerSec metric
     * @throws IOException Error writing out the report
     */
	public double genReport(MfgStats[] aggs, int txRate, PrintStream propp) throws IOException {

		String resultsDir = aggs[0].resultsDir;
		String filesep = System.getProperty("file.separator");
		summary = resultsDir + filesep + "Mfg.summary";
		detail = resultsDir + filesep + "Mfg.detail";
		PrintStream sump = new PrintStream(new FileOutputStream(summary));
		PrintStream detailp = new PrintStream(new FileOutputStream(detail));
		int i = 0;

		for (i = 0; i < aggs.length; i++) {
			processStats(aggs[i]);
		}	
		stdyState = aggs[0].stdyState;

		Debug.println("Printing summary report ...");
		double workOrdersPerSec = printSummary(sump, txRate, propp);
		Debug.println("Summary finished. Now printing detail ...");
		printDetail(detailp);
		return(workOrdersPerSec);
	}

    public void dumpRealTimeStats(MfgStats[] aggs, boolean display)
    {
       int mfgTxCnt = 0;
       Format fmtr = new Format("%2.2f");
       
       int mfgRespHist[] = new int[MfgStats.RESPMAX];
       
       for(int i=0; i < aggs.length; i++)
       {
          mfgTxCnt += aggs[i].workOrderCnt;
          for(int j=0; j< MfgStats.RESPMAX; j++)
          {
             mfgRespHist[j] += aggs[i].respHist[j];
          }
       }
       
       int cnt90 = (int)(mfgTxCnt * .9);
       float resp90 = 0;
       int sumTx = 0;
       for(int i=0; sumTx < cnt90; i++)
       {
          sumTx += mfgRespHist[i]; 
          resp90 += MfgStats.RESPUNIT;
       }
       
       if(display)
       {
          System.out.print("\rMfg (TxCnt="+mfgTxCnt+") 90% Resp="+fmtr.form(resp90)+"\t\t\t\t\t\t\t");
       }
    }
	
	/**
	 * This method is called by the Driver every time it wants to dump
	 * the thruput data out to a file.
     * @param aggs The collection of stats to aggregate and dump out
	 */
	public void dumpStats(MfgStats[] aggs) {
		int txCnt = 0;
		double tps = 0;

                for (int stream = 0; stream < dumpStreams.size(); stream++) {
                    DataOutputStream dumpStream = (DataOutputStream)
                                                  dumpStreams.get(stream);
                    try {
                        dumpStream.writeDouble(elapsed);
                    } catch (IOException e) {
                        dumpStreams.remove(stream--);
                        closeStream(dumpStream);
                        e.printStackTrace();
                        System.err.println("Error writing Mfg stats.\n" +
                            "Closing stream and removing stream from list.\n" +
                            "Benchmark continues without interruption.");

                    }
                }

                // Get the aggregate tx
		for (int i = 0; i < aggs.length; i++) {
			txCnt += aggs[i].workOrderCnt;
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

		/* Now dump out the old average tps */
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
                        System.err.println("Error writing Mfg stats.\n" +
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

		/* Now dump out the average tps */
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

                prevTxCnt = txCnt;
	}


        private void closeStream(OutputStream s) {
            try {
                s.close();
            } catch (IOException e) {
                // $JL-EXC$
            }
        }


	private void processStats(MfgStats m) {
		int i;
		
		numPlannedLines += m.numPlannedLines;
		workOrderCnt += m.workOrderCnt;
		vehicleCnt += m.vehicleCnt;
		largeOrderCnt += m.largeOrderCnt;
		largeOrderVehicleCnt += m.largeOrderVehicleCnt;
		if (m.respMax > respMax)
			respMax = m.respMax;
		respTime += m.respTime;
		for (i = 0; i < MfgStats.RESPMAX; i++) {
			respHist[i] += m.respHist[i];
		}
		for (i = 0; i < MfgStats.THRUMAX; i++) {
			thruput[i] += m.thruput[i];
		}
	}


	// Print summary report
	private double printSummary(PrintStream p, int txRate, PrintStream propp) {
		double txcnt, vehiclesPerSec, workOrdersPerSec, largeOrdersPerSec;
		double avg;
		int sumtx, cnt90;
		boolean fail90 = false, failavg =false;
		int i;
                p.println();
                p.println("\t\t\tMfg Summary Report");
                p.println("\t\t\tVersion : " + SPECjAppServerReport.version);
                p.println();

		p.print("Total Number of WorkOrders Processed : "); 
		Format.print(p, "%d", workOrderCnt);
		Format.print(propp, "result.manufacturing.workorders.count=%d\n", workOrderCnt);
		p.println();
		p.print("Number of WorkOrders as a result of LargeOrders : "); 
		Format.print(p, "%d", largeOrderCnt);
		p.println();
		p.print("Total WorkOrders Production Rate : "); 
		workOrdersPerSec = (double)workOrderCnt * 1000 / stdyState;
		Format.print(p, "%.02f WorkOrders/sec", workOrdersPerSec);
		p.println();
		p.print("LargeOrders Production Rate : "); 
		largeOrdersPerSec = (double)largeOrderCnt * 1000 / stdyState;
		Format.print(p, "%.02f LargeOrders/sec", largeOrdersPerSec);
		p.println();
		p.println();

		txcnt = vehicleCnt + largeOrderVehicleCnt;
		vehiclesPerSec = txcnt * 1000 / stdyState;
		p.print("Total Vehicle Manufacturing Rate : "); 
		Debug.println("vehicles/sec = " + vehiclesPerSec);
		Format.print(p, "%.02f vehicles/sec", vehiclesPerSec);
		p.println();
		p.print("LargeOrderLine Vehicle Rate : "); 
                double loRate = (double)largeOrderVehicleCnt * 1000 / stdyState;
		Format.print(p, "%.02f vehicles/sec", loRate);
                if((loRate >= 3.15 * txRate) && (loRate <= 3.85 * txRate))
                    p.print("\tPASSED");
                else
                    p.print("\tFAILED");
		p.println();
		p.print("PlannedLines Vehicle Rate : "); 
                double plRate = (double)vehicleCnt * 1000 / stdyState;
		Format.print(p, "%.02f vehicles/sec", plRate);
                if((plRate >= 2.835 * txRate) && (plRate <= 3.465 * txRate))
                    p.print("\tPASSED");
                else
                    p.print("\tFAILED");
		p.println();
		p.println();
		/* Compute response time info */
		p.println("RESPONSE TIMES\t\tAVG.\t\tMAX.\t\t90TH%\tREQD. 90TH%\n");
		if (workOrderCnt > 0) {
			avg  = (respTime/workOrderCnt) / 1000;
			sumtx = 0;
			cnt90 = (int)(workOrderCnt * .90);
			for (i = 0; i < MfgStats.RESPMAX; i++) {
				sumtx += respHist[i];
				if (sumtx >= cnt90)		/* 90% of tx. got */
					break;
			}
			resp90 = (i + 1) * MfgStats.RESPUNIT;
			if (resp90 > MfgStats.RESPFAST)
				fail90 = true;
			if (resp90 <= (avg - 0.1))
				failavg = true;
			Format.print(p, "\t\t\t%.03f\t\t", avg);
			Format.print(p, "%.03f\t\t", (double)respMax/1000);
			Format.print(p, "%.03f\t\t", resp90);
			p.println(MfgStats.RESPFAST);
			propp.print("result.manufacturing.resp_time.avg=");
			Format.print(propp, "%.03f\n", avg);
			propp.print("result.manufacturing.resp_time.max=");
			Format.print(propp, "%.03f\n", (double)respMax/1000);
			propp.print("result.manufacturing.resp_time.90p=");
			Format.print(propp, "%.03f\n", resp90);
		}
		else {
			p.println("\t\t\t0.000\t\t0.000\t\t0.000\n");
			propp.println("result.manufacturing.resp_time.avg=0.0");
			propp.println("result.manufacturing.resp_time.max=0.0");
			propp.println("result.manufacturing.resp_time.90p=0.0");
		}
		if (fail90)
			p.println("Requirement for 90% Response Time FAILED");
		else
			p.println("Requirement for 90% Response Time PASSED");
		if (failavg)
			p.println("Requirement for Avg. Response Time FAILED\n\n");
		else
			p.println("Requirement for Avg. Response Time PASSED\n\n");
		
		return(workOrdersPerSec);
	}


	/**
	 * This method prints the detailed report. This data is used to generate
	 * graphs of throughput, response times and think times.
     * @param p The print stream to output the detailed stats
	 */
	private void printDetail(PrintStream p) {
		int i, j;
		double f;

		p.println("                   Mfg Detailed Report\n");
		p.println("Manufacturing Throughput");
		p.println("TIME COUNT OF TX.");
		for (i = 0, j = 0; i < MfgStats.THRUMAX; i++, j += MfgStats.THRUUNIT) {
                        if(thruput[i] == 0 && i > 1)
                                break;
			p.println(j + "\t" +  thruput[i]);
                }
		p.println("\n\nFrequency Distribution of Response Times");
		for (i = 0, f = 0; i < MfgStats.RESPMAX; i++, f+= MfgStats.RESPUNIT) {
			Format.print(p, "%5.3f\t", f);
			p.println(respHist[i]);
		}
		p.close();
	}


}
