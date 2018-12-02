/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2001/       Shanti Subrmanyam         Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001 
 *  2002/07/10  Russell R.                Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/03     Tom Daly                  Update to SPECjAppServer2004 (http driver)
 */

package org.spec.jappserver.driver;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * The methods in this interface are the public face of the
 * Orders and Manufacturing Agents. The agents register with the
 * Controller on startup. The Driver gets a reference to all the agents
 * from the Controller and can then communicate with them using
 * this (the Agent) interface.
 * @author Shanti Subrmanyam
 * @see Driver
 * @see PlannedLine
 * @see LargeOrderLine
 * @see DealerEntry
 */
public interface Agent extends Remote {

	/**
	 * initialize remote Agents.
	 * @param properties run properties
     * @param timer central timer
     * @throws RemoteException remote call did not succeed
	 */
	public void configure(Properties properties, Timer timer) throws RemoteException;

	/**
	 * This method is responsible for starting the benchmark run.
	 * The caller does not wait for the run to complete. 
	 * @param delay time to delay(ms) before starting the run
     * @throws RemoteException remote call did not succeed
	 */
	public void run(int delay) throws RemoteException;

	/**
	 * This method is responsible for aborting a run.
     * @throws RemoteException remote call did not succeed
	 */
	public void kill() throws RemoteException;

	/**
	 * Report stats from a run, aggregating across all threads of
	 * the Agent.
	 * The stats object is actually different for each Agent.
     * @return The final results
     * @throws RemoteException remote call did not succeed
	 * @see DealerAggStats
	 * @see MfgStats
	 */
	public Serializable getResults() throws RemoteException;

	/**
     * Gets intermediate results during the run.
     * @return The current results
     * @throws RemoteException
     */
    public Serializable getCurrentResults() throws RemoteException;
}
