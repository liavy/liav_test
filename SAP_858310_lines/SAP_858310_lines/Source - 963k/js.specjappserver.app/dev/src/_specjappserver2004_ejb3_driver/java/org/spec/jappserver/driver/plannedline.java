/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID                         Description
 *  ----------    --------                   ----------------------------------------------
 *  2001/../..    Shanti Subramanyam, SUN    Created
 *  2002/04/12    Matt Hogstrom, IBM         Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10    Russel Raymundo, BEA       Conversion from SPECjAppServer2001 to
 *                                           SPECjAppServer2002 (EJB2.0).
 *  2002/09/23    Russel Raymundo, BEA       (020813) Fix for error handling in driver when
 *                                           InitialContext calls fail (errp is not
 *                                           initialized prior to error handling)
 *  2003/06/03    Tom Daly, SUN              Conversion to SPECjAppServer2004.
 *  2003/11/06    Samuel Kounev, Darmstadt   Modified for new database scaling rules (see osgjava-5681).
 *  2003/11/25    Samuel Kounev, Darmstadt   Modified for new database scaling rules as per osgjava-5891.
 *  2004/01/08    Akara Sucharitakul, SUN    Changes for new pDemand model (osgjava-6255) - workorder size set to
 *                                           avg 5.25 vehicles to match the demand caused by the new order rate.
 */

package org.spec.jappserver.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Util;
import org.spec.jappserver.driver.event.EprofEventHandler;
import org.spec.jappserver.driver.event.StatisticBuilder2;
import org.spec.jappserver.mfg.LargeOrderSes;
import org.spec.jappserver.mfg.WorkOrderSes;
import org.spec.jappserver.mfg.WorkOrderSesEJB;
import org.spec.jappserver.mfg.WorkOrderSesEJBService;
import org.spec.jappserver.mfg.helper.LargeOrderInfo;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * This class implements the Planned Line of the Manufacturing
 * Application of the SPECjAppServer workload. The MfgAgent will create
 * the PlannedLine threads and they run for the benchmark duration.
 *
 * @see MfgAgent
 * @see MfgStats
 * @see MfgReport
 * @author Shanti Subramanyam
 */
public class PlannedLine extends Thread {
	int id;
	Timer timer;
	Properties props;
	boolean inRamp;
	int rampUp, stdyState, rampDown;
	int endRampUp, endStdyState, endRampDown;
	int numPlannedLines, timePerTx, txRate;
	int benchStartTime;		// Actual time of rampup start
	String ident, resultsDir;
	Context ctx;
	boolean start = true, statsDone = false;
	MfgStats stats;		// keeps track of aggregate stats
	PrintStream errp;
	Station stations[];
	int numItems;
	RandNum r;
	RandPart rp;
	String wohome;
	WorkOrderSes workorder;

	String name;

    boolean useWebServices;
    WorkOrderSesEJB ws_workOrderSes;

	/**
	 * Constructor.
	 * @param id of this planned line (used in error msgs)
	 * @param timer object to use for timing functions
	 * @param props properties of the run
	 */
	public PlannedLine(int id, Timer timer, Properties props) {
		this.id = id;

		this.resultsDir = props.getProperty("runOutputDir");
                this.name = props.getProperty("agentName");
                this.ident = name.concat(":" + id + ": ");

                String errfile = null;

                if ( Integer.parseInt(props.getProperty("runMfg")) == 1 ) {
                  errfile = resultsDir + System.getProperty("file.separator") + getErrFileName();
                } else {
                  errfile = resultsDir + System.getProperty("file.separator") + name + ".err";
                }

                  // Create error log if it doesn't already exist
                  try {
                          if (new File(errfile).exists()) {
                                  errp = new PrintStream(new FileOutputStream(errfile, true));
                          }
                          else {	// try creating it
                                  // Debug.println(ident + "Creating " + errfile);
                                  errp = new PrintStream(new FileOutputStream(errfile));
                          }
                  } catch (Exception e) {
                     e.printStackTrace();
                          System.err.println(ident + "Could not create " + errfile);
                          errp = System.err;
                  }


                // Get an initial context
                try {
                    ctx = new InitialContext();
                } catch (NamingException ne) {
                    errp.println(ident + " : InitialContext failed. : " + ne);
                }

                String strUseWebServices = props.getProperty("useWebServices");
                useWebServices = strUseWebServices != null && strUseWebServices.equals("true");
                if (useWebServices)
                {
                   // If we do, then get an instance of the client for this agent;
                   try {
                      WorkOrderSesEJBService ws_wosl = new WorkOrderSesEJBService();
                      this.ws_workOrderSes = ws_wosl.getWorkOrderSesEJBPort();
                      javax.xml.ws.BindingProvider bp = (javax.xml.ws.BindingProvider) this.ws_workOrderSes;
                      Map<String,Object> context = bp.getRequestContext();
                      context.put("javax.xml.ws.service.endpoint.address", props.getProperty("wsEndPointAddress"));
                   }
                   catch (MalformedURLException e)
                   {
                      errp.println("MalformedURLException thrown when creating web services client:");
                      e.printStackTrace(errp);
                   }
                }                  

        this.timer = timer;
        this.props = props;

        start();
	}

    /**
     * Use a negative exponential distribution with specified mean and max.
     * We truncate the distribution at 5 times the mean for cycle times.
     * @param mean time
     * @param max time
     * @return time to use for this transaction
     */
    private int getFromDistribution(int mean, int max) {
        if (mean <= 0)
            return(0);
        else {
            double x = r.drandom(0.0, 1.0);
            if (x == 0)
                x = 0.05;
            int delay = (int)(mean * (-Math.log(x)));
            if (delay > max)
                delay = max;
            return(delay);
        }
    }


	/**
    * Each thread executes in the run method until the benchmark time is up
    * creating workorders and running them to completion. The stats for the
    * entire run are stored in an MfgStats object which is returned to the
    * MfgAgent via the getResult() method.
    * @see MfgStats
    */
	public void run() {
		int delay, startTime, endTime;

		getReady();		// Perform inits
		if (start == false)	// If error occured during setup, do not run
			return;
		/**
	 	 * Getting rid of stations, as in rare cases there can
		 * be conflict between multiple threads - createVehicle will do
		 * status updates. Shanti 5/2/01
		createStations();
		 */

		// If we haven't reached the benchmark start time, sleep
		delay = benchStartTime - timer.getTime();
		if (delay <= 0) {
			errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
		}
		else {
			// We vary the sleep time a bit
			try {
				Thread.sleep(delay + id*5);
			} catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
			}
		}
		inRamp = true;
		// Loop until time is up
        long nextStartTime = System.currentTimeMillis();
		while (true) {
            int maxSleepTime = timePerTx - (int)(System.currentTimeMillis() - nextStartTime);
            if (maxSleepTime > 0) {
                try {
                    Thread.sleep(r.random(0, maxSleepTime));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            nextStartTime += timePerTx;
            
			// Do work
			// Create workorder, send it thru stations
			startTime = timer.getTime();
            int respTime = createVehicle(useWebServices ? ws_workOrderSes : workorder, stats);
            StatisticBuilder2.send("ResponseTime.Order", respTime);
			endTime = timer.getTime();
			delay = endTime - startTime;

			endTime = timer.getTime();
			if (endTime >= endRampUp && endTime < endStdyState)
				inRamp = false;
			else
				inRamp = true;
			if (endTime >= endRampDown)
				break;
		}
		Debug.println(ident + "Vehicle cnt = " + stats.vehicleCnt);
		endRun();

		// Now sleep forever. We can't exit, as if we do, the thread
		// will be destroyed and the OrdersAgent won't be able to
		// retrieve our stats.
		while ( !statsDone) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
			}
		}
		Debug.println(ident + " Exiting...");
	}

	/**
	 * Return result of running this PlannedLine
	 * @return serializable form of MfgStats
	 * @see MfgStats
	 */
	public java.io.Serializable getResult() {
		// Debug.println(ident + "Returning stats");
		statsDone = true;
		return(stats);
	}

    /**
     * Obtains the current/intermerdiate results
     * @return The results
     */
	public java.io.Serializable getCurrentResult() {
		return(stats);
	}


	/**
	 * This method is called from configure to open and read the
	 * parameter file and set up instance variables from it. It also
	 * create an error log in the run directory and does a lookup
	 * on the OrdersBean home interface
	 */
    protected void getReady() {
        File runDirFile = null;
        try {
            runDirFile = new File(resultsDir);
            if ( !runDirFile.exists()) {
                if ( !runDirFile.mkdir()) {
                    throw new RuntimeException("Could not create the new Run Directory: " + resultsDir);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create the new Run Directory: " + resultsDir + e);
        }

        // Get our thread name and append it to the Agent name to
        // uniquely identify ourselves
        System.out.println("MfgAgent " + name + ", Thread " + id + " started");

        // Get some properties
        numPlannedLines = Integer.parseInt(props.getProperty("plannedLines"));
        double woRatePerAgent = Double.parseDouble(props.getProperty("woRatePerAgent"));
        txRate = Integer.parseInt(props.getProperty("txRate"));
        stats = new MfgStats(numPlannedLines, resultsDir);

        // compute our mean arrival rate in msecs
        timePerTx = (int)((numPlannedLines *1000)/ woRatePerAgent);
        System.err.println("timePerTx = " + timePerTx);

        // Calculate time periods
        benchStartTime = Integer.parseInt(props.getProperty("benchStartTime"));
        rampUp = Integer.parseInt(props.getProperty("rampUp"));
        stdyState = Integer.parseInt(props.getProperty("stdyState"));
        stats.setStdyState(stdyState);
        rampDown = Integer.parseInt(props.getProperty("rampDown"));
        endRampUp = benchStartTime + rampUp;
        endStdyState = endRampUp + stdyState;
        endRampDown = endStdyState + rampDown;
/****
		Debug.println(ident + "rampup end time = " + endRampUp +
			", stdy endtime = " + endStdyState +
			", rampdown endtime = " + endRampDown);
****/
		long seed = timer.getTime() + this.hashCode();
		r = new RandNum(seed);		// Seed random number generator
		/*** This should be the final version
		rp = new RandPart(r, txRate);
		****/

		numItems = txRate * 100;

		rp = new RandPart(r, numItems, 1);

		getBeanHomes();
	}

    /**
     * Obtains the error file name.
     * @return The error file name
     */
	protected String getErrFileName() {
              return "plannedlines.err";
	}

    /**
     * Obtains the home references for the EJBs tro call.
     */
	protected void getBeanHomes() {
		try {
			// Create an WorkOrderSes object
			String prefix = props.getProperty("homePrefix");
                        // The homePrefix will have the trailing '/'
			if (prefix != null) {
				wohome = prefix + "WorkOrderSes";
			}
			else {
				wohome = "WorkOrderSes";
			}
			// Debug.println("wohome = " + wohome);
//            long t = System.currentTimeMillis();
            InitialContext ctx = new InitialContext();
//            workorder = (WorkOrderSes) ctx.lookup("JavaEE/specj/REMOTE/WorkOrderSes/" + WorkOrderSes.class.getName());
            workorder = (WorkOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/WorkOrderSes/" + WorkOrderSes.class.getName());
//            StatisticBuilder.send("ResponseTime.WorkOrder.Lookup.PlannedLine", System.currentTimeMillis() - t);
		} catch (NamingException e) {
			errp.println(ident + "Failure looking up home " + e);
			start = false;
		} catch (Exception ex) {
         ex.printStackTrace();
         ex.printStackTrace(System.out);
         ex.printStackTrace(System.err);
			errp.println(ident + "Failure in creating bean " + ex);
			start = false;
		}
	}

/* ****
	protected void createStations() {
		// Now create the stations
		stations = new Station[3];
		for (int i = 0; i < 3; i++) {
			stations[i] = new Station(ctx, ident, errp, wohome, i+1);
		}
	}
*****/

    /**
     * Enters a non-largeorder assembly line to create vehicles.
     * @param workorder The work order reference
     * @param stats The statistics object
     */
	int createVehicle(Object workorder, MfgStats stats) {
		return createVehicle(workorder, stats, null);
	}

    /**
     * Enters a large order assembly line to create vehicles.
     * @param workorder The work order reference
     * @param stats The statistics object
     * @param lrgOrderInfo The large order information
     */
	int createVehicle(Object workorder, MfgStats stats,
			LargeOrderInfo lrgOrderInfo) {
		int startTime, endTime, elapsedTime, respTime;
		int qty;
		Integer woId = new Integer(0);
		int woStatus = 1;
		boolean fail = false;
		// 75% of the time, choose a qty of 5
		// 25% of the time, choose a qty of 6
		// This will result in a mean of 5.25
		int x = r.random(1, 100);
		if (x <= 75) {
			qty = 5;
		} else {
			qty = 6;
		}
/******
		// Pick a qty to manufacture
		int qty = r.random(1, peak);
		int qty = r.random(22, 23);
*****/
		// select a part number
		String assembly = rp.getPart();

		// Create workorder
		startTime = timer.getTime();
		try {
			if (lrgOrderInfo == null) {
				// Debug.println(ident + "PlannedLine: creating " + qty + " of workorder for " + assembly);
               java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
               gc.setTime(Util.getDateRoundToDay((new java.util.Date()).getTime())); // very inperformant since GregCal is used in getDateRoundToDay, too, but it's removed if bug in JPA is fixed
               if (workorder instanceof WorkOrderSes)
               {
				woId = ((WorkOrderSes)workorder).scheduleWorkOrder(assembly, qty, gc);
               }
               else
               {
                woId = ((WorkOrderSesEJB)workorder).scheduleWorkOrder(assembly, qty, new XMLGregorianCalendarImpl(gc)); 
               }
			}
			else  {
				// Debug.println(ident + "LargeOrderLine: creating " +
				// 	lrgOrderInfo.qty + " of workorder for " + lrgOrderInfo.assemblyId);
               java.util.Calendar c = new java.util.GregorianCalendar();
               if (lrgOrderInfo.dueDate == null){
                   c = null;
               } else {
                   c.setTime(lrgOrderInfo.dueDate);
               }        
               if (workorder instanceof WorkOrderSes)
               {
				woId = ((WorkOrderSes)workorder).scheduleLargeWorkOrder(
					lrgOrderInfo.salesOrderId, lrgOrderInfo.orderLineNumber,
					lrgOrderInfo.assemblyId,
					lrgOrderInfo.qty, c);
               }
               else
               {
                woId = ((WorkOrderSesEJB)workorder).scheduleLargeWorkOrder(
                    lrgOrderInfo.salesOrderId, lrgOrderInfo.orderLineNumber,
                    lrgOrderInfo.assemblyId,
                    lrgOrderInfo.qty, new XMLGregorianCalendarImpl((java.util.GregorianCalendar) c));
               }
			}
		} catch (Exception e) {
            send(e, "scheduleWorkOrder");
            e.printStackTrace(errp);
			errp.println(ident + "Error occured in scheduleWorkOrder" + e);
			fail = true;
		}

		// Now simulate activity at the 3 stations, sleeping
		// for a fixed 0.3333secs and updating status
		// Debug.println(ident + "workorder created for " + assembly);
		if ( ! fail) {
			for (int i = 0; i < 3; i++) {
				try {
					sleep(333);
				} catch (InterruptedException ie) {
                    System.err.println("Who interrupted createVehicle()?");
                    Thread.currentThread().interrupt();
                    fail = true;
                    break;
                }
				if ( i == 2) {
					woStatus = 2;
                    try {
                        if (workorder instanceof WorkOrderSes)
                        {
         					((WorkOrderSes)workorder).completeWorkOrder(woId);
                        }
                        else
                        {
                           ((WorkOrderSesEJB)workorder).completeWorkOrder(woId);
                        }
                    } catch (Exception e) {
                        send(e, "completeWorkOrder");
                        e.printStackTrace();
                        errp.println(ident + "Exception in complete WorkOrder(" + woId + ", " + woStatus + ") " + e);
                        fail = true;
                        break;
                    }
				}
				else {
					woStatus = 1;
                    try {
                        if (workorder instanceof WorkOrderSes)
                        {
                            ((WorkOrderSes)workorder).updateWorkOrder(woId);
                        }
                        else
                        {
                           ((WorkOrderSesEJB)workorder).updateWorkOrder(woId);
                        }
                    } catch (Exception e) {
                        send(e, "updateWorkOrder");
                        e.printStackTrace();
                        errp.println(ident + "Exception in update WorkOrder(" + woId + ", " + woStatus + ") " + e);
                        fail = true;
                        break;
                    }
				}
			}
		}
		endTime = timer.getTime();
		respTime = endTime - startTime;
		elapsedTime = endTime - benchStartTime;
		if ( ! fail) {
		if ((elapsedTime / MfgStats.THRUBUCKET) >= MfgStats.THRUMAX)
			stats.thruput[MfgStats.THRUMAX - 1]++;
		else
			stats.thruput[elapsedTime / MfgStats.THRUBUCKET]++;

		if ( !inRamp && endTime <= endStdyState) {
			if (lrgOrderInfo != null) {
				stats.largeOrderCnt++;
				stats.largeOrderVehicleCnt += lrgOrderInfo.qty;
			}
			else
				stats.vehicleCnt += qty;	// PlannedLine cnt
			stats.workOrderCnt++;
			stats.respTime += respTime;
			if (respTime > stats.respMax)
				stats.respMax = respTime;
			if ((respTime / MfgStats.RESPBUCKET) >= MfgStats.RESPMAX)
				stats.respHist[MfgStats.RESPMAX - 1]++;
			else
				stats.respHist[respTime / MfgStats.RESPBUCKET]++;
		}
		}
        
        return respTime;
	}

	/**
	 * This method is called at the end of the run to do cleanup
	 * operations.
	 */
	protected void endRun() {
/****
		for (int i = 0; i < stations.length; i++) {
			stations[i].quit();
		}
****/
		// End of run, destroy bean
		Debug.println(ident + "End of run. Removing beans");
//		try {
//			workorder.remove();
//		} catch (Exception e) {
//			errp.println(ident + " Error in removing workorder bean " + e);
//		}
	}
        
    public static void send(Throwable t, String name) {
        boolean isOptimisticLockException = false;
        boolean isInsuffCreditException = false;
        while (!isOptimisticLockException && !isInsuffCreditException && t != null) {
            String exStr = t.toString();
            isOptimisticLockException = exStr.indexOf("OptimisticLockException") != -1;
            isInsuffCreditException = !isOptimisticLockException && exStr.indexOf("SJASHttpInsufficientCreditException") != -1;
            t = t.getCause();
        }
        if (isOptimisticLockException) {
            StatisticBuilder2.send("Errors.OptLockEx", 1);
            StatisticBuilder2.send("Errors.OptLockEx." + name, 1);
        } else if (isInsuffCreditException) {
            StatisticBuilder2.send("Errors.InsuffCreditEx", 1);
        } else {
            StatisticBuilder2.send("Errors.Others." + name, 1);
        }
    }

    public void executePlannedLineRMICalls () throws NamingException, MalformedURLException {
        // large orders contains the work which is done for work orders -> execute only large order RMI calls

        EprofEventHandler.setRequestNamePrefix("RMI");
        EprofEventHandler.startProfiling("InitialContext.<init>");
        InitialContext ctx = new InitialContext();
        EprofEventHandler.endProfiling();

        LargeOrderSes largeorder = (LargeOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/LargeOrderSes/" + LargeOrderSes.class.getName());
        
        EprofEventHandler.startProfiling("InitialContext.lookup-WorkOrderSes");
        WorkOrderSes workorder = (WorkOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/WorkOrderSes/" + WorkOrderSes.class.getName());
        EprofEventHandler.endProfiling();

        // get large orders
        int category = 0; // we assume only one large order agent
        EprofEventHandler.startProfiling("LargeOrderSes.findLargeOrders");
        Vector loi = (Vector)largeorder.findLargeOrders(category);
        EprofEventHandler.endProfiling();
        if (loi.size() != 2) {
            throw new RuntimeException("There is more or less than two large orders!");
        }

        // RMI (normal order)
        LargeOrderInfo loInfo = (LargeOrderInfo) loi.elementAt(0);
        java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
        gc.setTime(Util.getDateRoundToDay((new java.util.Date()).getTime()));
        EprofEventHandler.startProfiling("WorkOrderSes.scheduleLargeWorkOrder");
        Integer woId = workorder.scheduleLargeWorkOrder(loInfo.salesOrderId, loInfo.orderLineNumber, loInfo.assemblyId,
                loInfo.qty, gc);
        EprofEventHandler.endProfiling();
        EprofEventHandler.startProfiling("WorkOrderSes.updateWorkOrder");
        workorder.updateWorkOrder(woId);
        EprofEventHandler.endProfiling();
        workorder.updateWorkOrder(woId);
        EprofEventHandler.startProfiling("WorkOrderSes.completeWorkOrder");
        workorder.completeWorkOrder(woId);
        EprofEventHandler.endProfiling();


        
        // Web Services (normal order)
        EprofEventHandler.setRequestNamePrefix("WS");
        EprofEventHandler.startProfiling("config");
        WorkOrderSesEJBService ws_wosl = new WorkOrderSesEJBService();
        WorkOrderSesEJB ws_workOrderSes = ws_wosl.getWorkOrderSesEJBPort();
        javax.xml.ws.BindingProvider bp = (javax.xml.ws.BindingProvider) ws_workOrderSes;
        Map<String,Object> context = bp.getRequestContext();
        context.put("javax.xml.ws.service.endpoint.address", System.getProperty("wsEndPointAddress"));
        EprofEventHandler.endProfiling();

        loInfo = (LargeOrderInfo) loi.elementAt(1);
        EprofEventHandler.startProfiling("WorkOrderSes.scheduleLargeWorkOrder");
        woId = ws_workOrderSes.scheduleLargeWorkOrder(loInfo.salesOrderId, loInfo.orderLineNumber, loInfo.assemblyId,
                loInfo.qty, new XMLGregorianCalendarImpl(gc)); 
        EprofEventHandler.endProfiling();
        EprofEventHandler.startProfiling("WorkOrderSes.updateWorkOrder");
        ws_workOrderSes.updateWorkOrder(woId);
        EprofEventHandler.endProfiling();
        ws_workOrderSes.updateWorkOrder(woId);
        EprofEventHandler.startProfiling("WorkOrderSes.completeWorkOrder");
        ws_workOrderSes.completeWorkOrder(woId);
        EprofEventHandler.endProfiling();
    }
    
    public PlannedLine() {}
}
