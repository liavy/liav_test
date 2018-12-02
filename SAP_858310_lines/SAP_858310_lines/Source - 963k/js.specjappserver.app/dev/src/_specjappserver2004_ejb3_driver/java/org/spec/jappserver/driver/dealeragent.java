/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
*  History:
*  Date        ID        Description
*  ----------  --------  ----------------------------------------------
*  Mar 2003    Tom Daly  Update to SPECjAppServer2004 (http driver) and
*                        dealer domain
*
* $Id: DealerAgent.java,v 1.5 2004/02/17 17:15:28 skounev Exp $
*
*/

package org.spec.jappserver.driver;
import java.io.FileInputStream;
import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Properties;
import java.util.Vector;

/**
 * DealerAgent is the agent that runs the DealerEntry application.
 * It receives commands from the Driver.
 * The DealerAgent is responsible for spawning and managing
 * threads, synchronizing between the threads and aggregating
 * the stats from all threads.
 *
 * @author Tom Daly (based on OrdersAgent by Shanti Subramanyam)
 * @see Agent
 * @see Driver
 */
public class DealerAgent extends UnicastRemoteObject
        implements Agent, Unreferenced {

    static DealerAgent dealerAgent;
    static Controller con;
    Properties connProps, runProps;
    Timer timer;
    Vector dealerEntryVector;
    String agentName;
    Serializable results[] = null;
    int numThreads;

    /**
     * Constructs the dealer agent.
     * @param name The name of this agent
     * @param propsFile The property file to read
     * @throws RemoteException If there is an exception on a remote call
     */
    protected DealerAgent(String name, String propsFile)
            throws RemoteException {
        agentName = name;
        connProps = new Properties();
        try {
            FileInputStream in = new FileInputStream(propsFile);
            connProps.load(in);
            in.close();
        }
        catch (Exception e) {
           e.printStackTrace();
            throw new RemoteException("Cannot read properties file " +
                    propsFile + e);
        }
    }

    /**
     * Configures this agent with the given properties and timer.
     * @param props The properties
     * @param timer The timer
     * @throws RemoteException The remote call failed
     */
    public void configure(Properties props, Timer timer)
            throws RemoteException {
        runProps = props;
        this.timer = timer;

        /*****
         String homePrefix = connProps.getProperty("homePrefix");
         runProps.setProperty("homePrefix", homePrefix);
         *****/
        runProps.setProperty("agentName", agentName);
        results = null;		// so that we don't use old results
        dealerEntryVector = new Vector();

        // Create the required number of DealerEntry threads
        numThreads = Integer.parseInt(runProps.getProperty("threadsPerAgent"));
        int sleepTime = Integer.parseInt(runProps.getProperty("msBetweenThreadStart"));
        for (int i = 0; i < numThreads; i++) {
            dealerEntryVector.addElement(new DealerEntry(i, timer, runProps));
            try {
                // Give time for thread to conenct to server
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
               ie.printStackTrace();
            }
        }
        /*****
         // Wait to ensure all threads are up
         try {
         Thread.sleep(200 * numThreads);
         } catch (InterruptedException ie) {
         }
         *****/
    }

    /**
     * This method is responsible for starting up the benchmark run.
     * @param delay Time before starting run
     */
    public void run(int delay) {
        Debug.println("DealerAgent: Starting benchmark run");
        // trigger.startRun(delay);
    }


    /**
     * This method kills off the current run.
     * It terminates all threads.
     */
    public synchronized void kill() {

        Debug.println("DealerAgent: Killing benchmark run");
        for (int i = 0; i < numThreads; i++) {
            ((DealerEntry)(dealerEntryVector.elementAt(i))).destroy();
        }
        // cleanup
        results = null;
    }


    /**
     * Report stats from a run
     * Each thread's result is obtained by calling that thread's getResult()
     * All these results are then aggregated by calling one of the
     * thread's getAggregateResult method.
     * @return The final results
     */
    public Serializable getResults() {
        results = new Serializable[numThreads];
        for (int i = 0; i < numThreads; i++) {
            results[i] = ((DealerEntry)(dealerEntryVector.elementAt(i))).getResult();
        }
        // Aggregate results from all threads of this agent
        DealerAggStats aggStats = new DealerAggStats();
        for (int index = 0; index < results.length; index++) {
            aggStats.addResult((DealerStats)(results[index]));
        }
        return(aggStats);
    }

    /**
     * This method is for the chart demo.
     * The Driver will call this at specific intervals, to re-compute
     * the current thruput.
     * @return The current or intermediate results
     */
    public Serializable getCurrentResults() {
        Serializable curResults[] = new Serializable[numThreads];
        for (int i = 0; i < numThreads; i++) {
            curResults[i] = ((DealerEntry)(dealerEntryVector.elementAt(i))).getCurrentResult();
        }
        // Aggregate results from all threads of this agent
        DealerAggStats aggStats = new DealerAggStats();
        for (int index = 0; index < curResults.length; index++) {
            aggStats.addResult((DealerStats)(curResults[index]));
        }
        return(aggStats);
    }


    /**
     * When this instance is unreferenced the application must exit.
     * @see Unreferenced
     */
    public void unreferenced() {
        kill();
    }
    /**
     * Starts and registers this agent for RMI serving.
     * @param argv The command line arguments
     */
    public static void main(String [] argv) {

        //		LocateRegistry.createRegistry();
        System.setSecurityManager (new RMISecurityManager());
        if (argv.length != 3) {
            System.out.println("Usage: DealerAgent <propsFile> <agentName> <masterMachine>");
            System.exit(-1);
        }
        System.out.println("DriverDebug: DealerAgent <propsFile> <agentName> <masterMachine>");
        String propsFile = argv[0];
        String name = argv[1];
        String master = argv[2];

        try {
            dealerAgent = new DealerAgent(name, propsFile);
            String s1 = "//" + master + "/Controller";
            con = (Controller)Naming.lookup(s1);
            con.register("DealerAgent", name, dealerAgent);
            Debug.println(name + " started ...");
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
