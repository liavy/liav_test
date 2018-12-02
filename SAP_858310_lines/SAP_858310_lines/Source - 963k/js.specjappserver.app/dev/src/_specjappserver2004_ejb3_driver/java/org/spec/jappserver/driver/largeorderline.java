/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------------------
 *  2001        Shanti Subrmanyam, SUN    Created
 *  2002/05/12  Matt Hogstrom, IBM        Corrected bug for multi-agent support.
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/06     Tom Daly, Sun             Conversion to SPECjAppServer2004
 *  2004/02/08  John Stecher, IBM         Modified code to support launching multiple LOAgents
 *  2004/02/20  Balu Bsthanikam, Oracle   Fixed performance problem by changing nested loops
 *                                        with two vectors to one single HashMap (osgjava-6608).
 *
 * $Id: LargeOrderLine.java,v 1.9 2004/02/21 15:11:46 skounev Exp $
 */

package org.spec.jappserver.driver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.driver.event.StatisticBuilder2;
import org.spec.jappserver.mfg.LargeOrderSes;
import org.spec.jappserver.mfg.WorkOrderSes;
import org.spec.jappserver.mfg.helper.LargeOrderInfo;

/**
 * This is the class that implements the LargeOrderLine driver.
 * This driver will call the get method of the LargeOrderBean every
 * second to see if there are any new large orders. It will then
 * create workorders and cycle them through the stations just like
 * the PlannedLine does.
 *
 * @see PlannedLine
 * @author Shanti Subramanyam
 */
public class LargeOrderLine extends PlannedLine {
    LargeOrderSes largeorder;

	Integer categoryToQuery = new Integer(1);

	/**
	 * Creates a large order line.
	 * @param id The thread id
	 * @param timer The timer to use
	 * @param props Properties passed in from the driver
	 */
	public LargeOrderLine(int id, Timer timer, Properties props) {
		super(id, timer, props);
		categoryToQuery = new Integer(id);
	}

	protected String getErrFileName() {
		return("loline.err");
	}

	protected void getBeanHomes() {
		try {
         long t = System.currentTimeMillis();
         InitialContext ctx = new InitialContext();
//       largeorder = (LargeOrderSes) ctx.lookup("JavaEE/specj/REMOTE/LargeOrderSes/" + LargeOrderSes.class.getName());
         largeorder = (LargeOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/LargeOrderSes/" + LargeOrderSes.class.getName());
         StatisticBuilder2.send("ResponseTime.LargeOrder.Lookup", System.currentTimeMillis() - t);
		} catch (Exception e) {
         e.printStackTrace(errp);
			errp.println(ident + "Exception in creating LargeOrderBean" + e);
			start = false;
		}
	}

	/**
	 * Each thread executes in the run method until the benchmark time is up
	 * creating workorders and running them to completion.
 	 * The stats for the entire run are stored in an MfgStats object
 	 * which is returned to the MfgAgent via the getResult() method.
	 * @see MfgStats
	 */
	public void run() {
		int delay, endTime;

		getReady();		// Perform inits
		stats.numPlannedLines = 0;

		if (start == false)	// If error occured during setup, do not run
			return;

		// If we haven't reached the benchmark start time, sleep
		delay = benchStartTime - timer.getTime();
		if (delay <= 0) {
			errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
		}
		else {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
			}
		}
		try
		{
			inRamp = true;
			// Loop until time is up
			while (true) {

				// Do work
				try {
					doLargeOrders();
				} catch (Exception e) {
					e.printStackTrace(errp);
				}

				endTime = timer.getTime();
				// Debug.println(ident + "endTime = " + endTime);
				if (endTime >= endRampUp && endTime < endStdyState)
					inRamp = false;
				else
					inRamp = true;
				if (endTime >= endRampDown)
					break;
			}
			// Debug.println(ident + "Vehicle cnt = " + stats.vehicleCnt);
			endRun();
		}
		catch(Exception e)
		{
			e.printStackTrace(errp);
		}
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

	private static HashMap lrgLineMap = new HashMap();

	private void doLargeOrders () throws Exception {
      Vector loi;
		int endTime;

		// Check if there are any waiting largeorders
		try {
         Object o = largeorder.findLargeOrders(categoryToQuery.intValue());
			loi = (Vector)o; // fetch Largeorders from MT
            StatisticBuilder2.send("LargeOrderCount", loi.size());
		} catch (Exception e) {
         errp.println(ident + "Error occured in findLargeOrders " + e);
			return;
		}

		/* Create threads for all new largeorders */
		for (int i = 0; i < loi.size(); i++)
		{
			LargeOrderInfo loInfo = (LargeOrderInfo) loi.elementAt(i);
			if(!lrgLineMap.containsKey(loInfo.id))
			{
				// we have not created a thread for this row.
				// create a thrad and store in the Hashtable.
				LrgLineData lrgLineData = new LrgLineData();
				lrgLineData.stats  =  new MfgStats(0, resultsDir);
				lrgLineData.stats.setStdyState(stdyState);
				lrgLineData.lrgLine = new LrgLine(loInfo, stats);
				lrgLineData.id = loInfo.id;
				lrgLineMap.put(lrgLineData.id, lrgLineData);

			}
		}
		 endTime = timer.getTime() + 5000;
		// Sleep so that at least some of the LOs finish
		Thread.sleep(2000);

		while (timer.getTime() < endTime) {

			// Check to see which largeorders finished
			Iterator it = lrgLineMap.values().iterator();

			if(!it.hasNext())
				break; // no pending LOs, break out

			while(it.hasNext())
			{
				LrgLineData lrgd = (LrgLineData )it.next();

				if (!lrgd.lrgLine.isAlive())
				{
					/* this thread finished, remove it from HT and add stats */
					stats.addResult(lrgd.stats);
					it.remove();
					// lrgLineMap.remove(lrgd.id);
				}
			}
			Thread.sleep(50);
		}

		return;
	}
	/**
	 * This method is called at the end of the run to do cleanup.
	 * operations
	 */
	protected void endRun() {
//		// End of run, destroy bean
//		try {
//			largeorder.remove();
//		} catch (Exception e) {
//			errp.println(ident + " Error in removing beans " + e);
//		}
	}

	public class LrgLineData {
			  MfgStats stats;
			  LrgLine lrgLine;
			  Integer id;
	};

	/**
	 * Class LrgLine
	 * This class spawns and manages a workorder for a specific large order.
	 */
	private class LrgLine extends Thread {
		private MfgStats stats;
		private LargeOrderInfo loi;
		private WorkOrderSes workorder;
                private Context ctx;

		LrgLine(LargeOrderInfo loi, MfgStats stats) {
			this.loi = loi;
			this.stats = stats;
                        // Get an initial context
                        try {
                            ctx = new InitialContext();
                        } catch (NamingException ne) {
                           ne.printStackTrace(errp);
                            errp.println(ident + " : InitialContext failed. : " + ne);
                        }
			start();
		}

		public void run() {
			boolean start = true;
			try {
			// Create an WorkOrderSes object
            long t = System.currentTimeMillis();
            InitialContext ctx = new InitialContext();
            
//          workorder = (WorkOrderSes) ctx.lookup("JavaEE/specj/REMOTE/WorkOrderSes/" + WorkOrderSes.class.getName());
          workorder = (WorkOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/WorkOrderSes/" + WorkOrderSes.class.getName());
            StatisticBuilder2.send("ResponseTime.WorkOrder.Lookup.LargeOrderLine", System.currentTimeMillis() - t);
			} catch (NamingException e) {
                PlannedLine.send(e, "LargeOrder");
                e.printStackTrace(errp);
				errp.println(ident + "Failure looking up home " + e);
				start = false;
			} catch (Exception ex) {
                PlannedLine.send(ex, "LargeOrder");
                ex.printStackTrace(errp);
				errp.println(ident + "Failure in creating bean " + ex);
				start = false;
			}
			if (start) {
/****
				createStations();
****/
				int respTime = createVehicle(workorder, stats, loi);
                StatisticBuilder2.send("ResponseTime.LargeOrder", respTime);
/****
				// Now destory the stations, as we no longer need them
				for (int i = 0; i < stations.length; i++) {
					stations[i].quit();
				}
****/
			}
			return;
		}
	}
}
