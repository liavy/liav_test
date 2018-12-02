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
 * $Id: Station.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;

import java.io.PrintStream;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.spec.jappserver.mfg.WorkOrderSes;

/**
 * This class manages a particular station in the assembly line.
 * It sleeps until asked to update the workorder status.
 * Both the PlannedLine and the LargeOrderLine will create 3 stations
 * each per line.
 *
 * @see PlannedLine
 * @author Shanti Subramanyam
 */
 public class Station extends Thread {
	private WorkOrderSes workorder;
	private String sident;
	boolean start, finish = false;
	PrintStream errp;

    /**
     * Constructs the station.
     * @param ctx The JNDI context to be used by this station
     * @param pident Mfg line identification
     * @param errp The error output stream
     * @param wohome The home interface for the workorder bean
     * @param id The station id
     */
	public Station(Context ctx, String pident, PrintStream errp,
				String wohome, int id) {
		this.errp = errp;
		sident = pident.concat("Station" +id + ": ");
		// Debug.println(sident  + "Creating workorder ...");
		try {
			// Create a workorder object that we will use
         ctx = new InitialContext();
         workorder = (WorkOrderSes) ctx.lookup("sap.com/SPECjAppServer2004_EJB3_ear/REMOTE/WorkOrderSes/" +WorkOrderSes.class.getName());
		} catch (Exception e) {
         e.printStackTrace();
         errp.println(sident + "Exception in creating WorkOrderBean " + e);
			start = false;
		}
		start();		/* Start running */
	}

	// We simply sleep in the run method
	public void run() {
		if (start == false)
			return;
		while (true) {
			try {
				sleep(3 * 60 * 60 * 1000);	// sleep for a long time
			} catch (InterruptedException ie) {
			if (finish)		// If we are finished, quit
				return;
			else
				continue;
			}
		}
	}


	/**
	 * This method updates the status of a particular work-order
	 * by making a method call on our bean.
	 * @param wo_id workorder to be updated
	 * @param wo_status value of status to update to
	 * @return true if update successful, false otherwise
	 */
	public boolean updateStatus(Integer wo_id, int wo_status) {
		try {
			if (wo_status == 2)
				workorder.completeWorkOrder(wo_id);
			else
				workorder.updateWorkOrder(wo_id);
		} catch (Exception e) {
         e.printStackTrace();
         errp.println(sident + "Exception in updateStatus(" +
					wo_id + ", " + wo_status + ") " + e);
			return(false);
		}
		return(true);
	}

	/**
	 * This method is called to terminate the Station thread
	 * It destroys the workorder bean and causes a return from
	 * the run method indirectly.
	 */
	public void quit() {
//		try {
//			workorder.remove();
//		} catch (Exception e) {
//			errp.println(sident + " Error in removing WorkOrderbean" + e);
//		}
		finish = true;		// Since 'destroy' is not implemented, tell
					// our 'run' method to return
	}
}
