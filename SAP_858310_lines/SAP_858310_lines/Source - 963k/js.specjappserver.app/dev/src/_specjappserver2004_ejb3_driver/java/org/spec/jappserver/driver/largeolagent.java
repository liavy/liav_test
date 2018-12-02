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
 *  2004/02/08  John Stecher              Modified code to support launching multiple LOAgents
 *
 * $Id: LargeOLAgent.java,v 1.7 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;
import java.io.FileInputStream;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Properties;

/**
 * LargeOLAgent is the agent that runs the LargeOrderLine portion of the Mfg 
 * application.
 * It receives commands from the Driver.
 * The LargeOLAgent is responsible for spawning and managing the single 
 * LargeOrderLine and collecting its stats at the end of the run.
 *
 * @author Shanti Subramanyam
 * @see Agent
 * @see Driver
 */
public class LargeOLAgent extends UnicastRemoteObject 
    implements Agent, Unreferenced {

	static LargeOLAgent largeOLAgent;
	static Controller con;
	Properties connProps, runProps;
	Timer timer;
	LargeOrderLine largeOrderLine;
	String agentName;
	Serializable results = null;
	int numThreads;

    /**
     * Constructor.
     * Create properties object from file.
     * @param name The agent's name
     * @param propsFile The properties file to use
     * @throws RemoteException If the remote call fails
     */
    protected LargeOLAgent(String name, String propsFile) throws RemoteException {
		agentName = name;
		connProps = new Properties();
		try {
			FileInputStream in = new FileInputStream(propsFile);
			connProps.load(in);
			in.close();
		}
		catch (Exception e) {
			throw new RemoteException("Cannot read properties file " +
				propsFile + e);
		}
    }

	 
    public void configure(Properties props, Timer timer) throws RemoteException {
		runProps = props;
		this.timer = timer;

		runProps.setProperty("agentName", agentName);
		results = null;		// so that we don't use old results
                int categories = Integer.parseInt(props.getProperty("runLO"));
                String category = agentName.substring(1);
                int categoryToQueryOn = Integer.parseInt(category);
		largeOrderLine = new LargeOrderLine(categoryToQueryOn-1, timer, runProps);
    }
	
    /**
     * This method is responsible for starting up the benchmark run.
	 * @param delay time before starting run
     */
    public void run(int delay) {
		Debug.println("LargeOLAgent: Starting benchmark run");
		// trigger.startRun(delay);
    }


    /**
     * This method kills off the current run.
	 * It terminates all threads.
     */
    public synchronized void kill() {

		Debug.println("LargeOLAgent: Killing benchmark run");
		largeOrderLine.destroy();
		results = null;
    }


	/**
	 * Report stats from a run.
     * @return The final results
	 */
	public Serializable getResults() {
		results = largeOrderLine.getResult();
		return(results);
	}

	public Serializable getCurrentResults() {
		return largeOrderLine.getCurrentResult();
	}


    /**
     * When this instance is unreferenced the application must exit.
     *
     * @see         Unreferenced
     *
     */
    public void unreferenced() {
			kill();
    }


    /**
     * Starts the agent and registers it for RMI serving.
     * @param argv The command line argument
     */

    public static void main(String [] argv) {

	//		LocateRegistry.createRegistry();
	System.setSecurityManager (new RMISecurityManager());
	if (argv.length != 3) {
	    System.out.println("Usage: LargeOLAgent <propsFile> <agentName> <masterMachine>");
	    System.exit(-1);
	}
	String propsFile = argv[0];
	String name = argv[1];
	String master = argv[2];

	try {
	    largeOLAgent = new LargeOLAgent(name, propsFile);
	    String s1 = "//" + master + "/Controller";
	    con = (Controller)Naming.lookup(s1);
	    con.register("LargeOLAgent", name, (Remote)largeOLAgent);
	    Debug.println(name + " started ...");
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
