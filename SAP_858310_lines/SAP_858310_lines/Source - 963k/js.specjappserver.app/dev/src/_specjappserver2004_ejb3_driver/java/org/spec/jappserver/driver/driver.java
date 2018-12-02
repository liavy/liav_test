/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  -------------------------------------------------------------
 *  2002/01/22  ramesh, SUN Microsystem   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/05/12  Matt Hogstrom, IBM        Modified various checks from == 1 to >= 1 to
 *                                        accomodate multiple agents.
 *  2002/10/07  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  Mar 2003    Tom Daly, SUN             Update to SPECjAppServer2004 (http driver)
 *  2003/05/29  Samuel Kounev, Darmstadt  Included a call to auditor.getDeliveryStats() at
 *                                        the end of steady state to collect statistics
 *                                        used to verify that the Receive and FulfillOrder
 *                                        queues are stable.
 *  2004/02/08  John Stecher, IBM         Modified code to support launching multiple LOAgents
 *  2004/02/14  Tom Daly , Sun            Add calls to run the cache validation
 *                                        tests at run start and run end and run atomicity
 *                                        tests automatically from auditor
 *  2004/02/17  Samuel Kounev, Darmstadt  Added auditor check to make sure that the number of
 *                                        LO agents started matches the number of LO categories
 *                                        set in LargeOrderEnt's categories env property.
 *  2004/02/27  Samuel Kounev, Darmstadt  Bug fix: call auditor.getDeliveryStats only if runMfg >= 1
 *  2004/11/30  John Stecher IBM          Added in real time statistics support
 *
 */
package org.spec.jappserver.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.spec.jappserver.driver.event.EventHandler;
import org.spec.jappserver.driver.event.EventHandlerImpl;

/**
 * This is the main Driver class for running the SPECjAppServer benchmark. The Driver is instantiated on the <b>master machine
 * </b> by the user wishing to run a benchmark. It is responsible for co-ordinating the work of all the Agents, setting up the
 * benchmark test, collecting the results etc. NOTE: The controller and agents must have been brought up before starting the
 * driver. The driver will fail otherwise.
 * 
 * @see Agent
 * @see Controller
 * 
 */
public class Driver {
    protected Properties props;
    protected String homeDir, outDir, runOutputDir, pathSep;
    protected int runID, scaleFactor;
    protected Remote[] dealerRefs, mfgRefs, largeOrderRefs; // Agent references
    // protected Agent largeOrderLine;
    protected RunInfo runInfo;
    // protected Remote[] dealerRefs;
    private String hostServer; // these vars are to enable access to the web tier to run
    private int port; // the cache validation test and the atomicity tests
    private String queryString;
    private String url;
    private String part;
    Timer timer;

    // Audit class reference
    // Auditor auditor = null;
    /**
     * Default constructor.
     */
    public Driver() {
        dealerRefs = null;
        mfgRefs = null;
        largeOrderRefs = null;
    }

    /**
     * Constructor.
     * 
     * @param propsFile
     *            Name of properties file with input parameters
     * @throws Exception
     */
    public Driver(String propsFile) throws Exception {
        runInfo = new RunInfo();
        props = new Properties();
        try {
            FileInputStream in = new FileInputStream(propsFile);
            props.load(in);
            in.close();
        } catch (Exception e) {
            throw new Exception("Cannot read properties file " + propsFile + e);
        }
        homeDir = System.getProperty("user.home");
        pathSep = System.getProperty("file.separator");
        outDir = props.getProperty("outDir");
        if (outDir == null)
            outDir = homeDir + pathSep + "output";
        // Gets the ID for this run from the sequence file.
        runID = getRunID();
        System.out.println("RunID for this run is : " + runID);
        props.setProperty("runID", Integer.toString(runID));
        // make a new directory for the run.
        runOutputDir = outDir + pathSep + runID;
        File runDirFile = null;
        try {
            runDirFile = new File(runOutputDir);
            if (!runDirFile.exists()) {
                if (!runDirFile.mkdir()) {
                    throw new Exception("Could not create the new Run Directory: " + runOutputDir);
                }
            }
        } catch (Exception e) {
            throw new Exception("Could not create the new Run Directory: " + runOutputDir + e);
        }
        System.out.println("Output directory for this run is : " + runOutputDir);
        props.setProperty("runOutputDir", runOutputDir);
        configure();
        configureAgents();
        executeRun();
    }

    /*
     * This method retrieves the ID for the current run, by looking in the specjappserver.seq file in the user's home directory.
     * It increments the sequence file.
     */
    private int getRunID() throws Exception {
        String runIDStr;
        int runID;
        String seqFileName = homeDir + pathSep + "specjappserver.seq";
        File seqFile = new File(seqFileName);
        if (seqFile.exists()) {
            BufferedReader bufIn = null;
            try {
                bufIn = new BufferedReader(new FileReader(seqFile));
            } catch (FileNotFoundException fe) {
                throw new Exception("The sequence file '" + seqFile + "' does not exist" + fe);
            }
            runIDStr = null;
            try {
                runIDStr = bufIn.readLine();
                bufIn.close();
            } catch (IOException ie) {
                throw new Exception("Could not read/close the sequence file " + seqFileName + ie);
            }
            runID = Integer.parseInt(runIDStr);
        } else {
            try {
                seqFile.createNewFile();
            } catch (IOException ie) {
                throw new Exception("Could not create the sequence file: " + seqFileName + ie);
            }
            runID = 1;
            runIDStr = "1";
        }
        // Update the runid in the sequence file
        try {
            BufferedWriter bufOut = new BufferedWriter(new FileWriter(seqFileName));
            bufOut.write(Integer.toString(runID + 1));
            bufOut.close();
        } catch (IOException ie) {
            throw new Exception("Could not write to the sequence file: " + seqFileName + ie);
        }
        return (runID);
    }

    /**
     * Configure the run, parse run properties, and get a list of all the registered agents and configure them using the run
     * properties.
     * 
     * @throws Exception
     */
    protected void configure() throws Exception {
        timer = new Timer();
        // Compute total # of dealer and planned line threads
        // trim all properties else spaces after properties in the
        // config file cause errors.
        String propstr = (props.getProperty("scaleFactor")).trim();
        scaleFactor = Integer.parseInt(propstr);
        propstr = (props.getProperty("txRate")).trim();
        runInfo.txRate = Integer.parseInt(propstr);
        propstr = (props.getProperty("runMfg")).trim();
        runInfo.runMfg = Integer.parseInt(propstr);
        propstr = (props.getProperty("runDealerEntry")).trim();
        runInfo.runDealerEntry = Integer.parseInt(propstr);
        // Added for Multiple LO agents
        propstr = (props.getProperty("runLO")).trim();
        runInfo.runLO = Integer.parseInt(propstr);
        propstr = (props.getProperty("doAudit")).trim();
        runInfo.doAudit = Integer.parseInt(propstr);
        propstr = (props.getProperty("dumpStats")).trim();
        runInfo.dumpStats = Integer.parseInt(propstr);
        propstr = (props.getProperty("rampUp")).trim();
        runInfo.rampUp = Integer.parseInt(propstr);
        runInfo.rampUp *= 1000; // convert to ms
        props.setProperty("rampUp", Integer.toString(runInfo.rampUp));
        propstr = (props.getProperty("rampDown")).trim();
        runInfo.rampDown = Integer.parseInt(propstr);
        runInfo.rampDown *= 1000; // convert to ms
        props.setProperty("rampDown", Integer.toString(runInfo.rampDown));
        propstr = (props.getProperty("stdyState")).trim();
        runInfo.stdyState = Integer.parseInt(propstr);
        runInfo.stdyState *= 1000; // convert to ms
        props.setProperty("stdyState", Integer.toString(runInfo.stdyState));
        propstr = props.getProperty("triggerTime");
        if (propstr == null)
            runInfo.triggerTime = -1;
        else {
            propstr = propstr.trim();
            if (propstr.length() == 0)
                runInfo.triggerTime = -1;
            else
                runInfo.triggerTime = Integer.parseInt(propstr);
        }
        propstr = props.getProperty("msBetweenThreadStart");
        if (propstr == null)
            runInfo.msBetweenThreadStart = -1;
        else {
            propstr = propstr.trim();
            if (propstr.length() == 0)
                runInfo.msBetweenThreadStart = -1;
            else
                runInfo.msBetweenThreadStart = Integer.parseInt(propstr);
        }
        // Setup so that the auditor can be called to run the cache validation
        // tests
        // get the host and port numbers from the supplied url
        url = props.getProperty("Url"); // url for the main web page
        parseUrl(url); // set port, queryString and host
        props.setProperty("hostServer", hostServer);
        props.setProperty("queryString", queryString);
        props.setProperty("port", Integer.toString(port));
        // get a part name that will be used for the cache validation test
        long seed = System.currentTimeMillis() + this.hashCode();
        RandNum r = new RandNum(seed); // Seed random number generator
        RandPart rp = new RandPart(r, 100, 1); // generate a random part from the
        // first 100 parts
        part = rp.getPart(1, 100); // get the part name
        props.setProperty("part", part);
        /*
         * not yet ported to EJB3 if (runInfo.doAudit == 1) { // Create Auditor with runInfo auditor = new Auditor(props,
         * runInfo);
         *  // Validate initial DB settings auditor.validateInitialValues();
         * 
         * //set initial cache value, this value is held in the web tier and // checked again // at the end of the run to ensure
         * caches are being timed out properly. auditor.cacheTest(0); }
         */
    }

    /**
     * parse the URL from the porperties file, into host,port and url sections
     */
    private void parseUrl(String url) {
        byte[] url_b = url.getBytes();
        int i;
        // int j;
        i = 0;
        // find the server hostname and port #
        if (url_b[0] == 'h' && url_b[1] == 't' && url_b[2] == 't' && url_b[3] == 'p') {
            i = 7;
        }
        // j=0;
        int startIndex = i;
        while (url_b[i] != ':') {
            // hostServer_b[j++] = url_b[i++];
            i++;
            continue;
        }
        hostServer = url.substring(startIndex, i);
        i++;
        port = 0;
        while (url_b[i] >= '0' && url_b[i] <= '9') {
            port *= 10;
            port += url_b[i++] - '0';
        }
        queryString = url.substring(i);
    }

    /**
     * call the web page to run the cache validation tests
     * 
     * @returns true=passed, false = failed
     * @throws Exception
     */
    // private boolean validateCache(int test) throws Exception {
    // // parseUrl(url);
    // // c = new Connection(hostServer, port, timer);
    // // c.connect();
    // HttpDealer proxy = new HttpDealer(queryString, c);
    // boolean valid=proxy.cacheTest(test, part);
    // c.close("validation test complete");
    // return valid;
    // }
    /**
     * Gets the remote reference for all dealer agents.
     * 
     * @throws Exception
     */
    protected void getAgentRefs() throws Exception {
        String host = (InetAddress.getLocalHost()).getHostName();
        /***********************************************************************************************************************
         * String port = (props.getProperty("driverPort")).trim(); String s1 = "//" + host + ":" + port + "/" + "Controller";
         **********************************************************************************************************************/
        String s1 = "//" + host + "/" + "Controller";
        Controller con = (Controller) Naming.lookup(s1);
        if (runInfo.runDealerEntry >= 1) {
            dealerRefs = con.getServices("DealerAgent");
        }
        if (runInfo.runMfg >= 1) {
            mfgRefs = con.getServices("MfgAgent");
        }
        if (runInfo.runLO >= 1) {
            largeOrderRefs = con.getServices("LargeOLAgent");
        }
    }

    /**
     * Get a list of all the registered agents and configure them.
     * 
     * @exception Exception
     */
    protected void configureAgents() throws Exception {
        // To make sure that the right Controller class
        // is used to get the remote refs.
        getAgentRefs();
        int thrdTimeFactor = 0; // Time factor to wait
        // before starting the trigger.
        int dlrAgentCnt = 0;
        int mfgAgentCnt = 0;
        int loAgentCnt = 0;
        int dlrThrdsPerAgent = 0;
        int dlrRemThrds = 0;
        if (runInfo.runDealerEntry >= 1) {
            dlrAgentCnt = dealerRefs.length;
            runInfo.numDealerAgents += dlrAgentCnt;
            // Number of threads = 10 per txrate for 2004, this is
            // offset by an increase in cycletime in DealerEntry
            // and is to reflect real world web tier usage.
            int numThreads = runInfo.txRate * 10;
            // int numThreads = 1; // <= for debug only
            if (dlrAgentCnt == 0) {
                throw new RuntimeException("Cannot find DealerAgent, please ensure it gets started!");
            }
            dlrThrdsPerAgent = numThreads / dlrAgentCnt;
            dlrRemThrds = numThreads - (dlrAgentCnt * dlrThrdsPerAgent);
            thrdTimeFactor += numThreads + dlrAgentCnt * 3 + 80;
            props.setProperty("threadsPerAgent", Integer.toString(dlrThrdsPerAgent));
            props.setProperty("txRatePerAgent", Integer.toString(runInfo.txRate / dlrAgentCnt));
        }
        if (runInfo.runMfg >= 1) {
            mfgAgentCnt = mfgRefs.length;
            runInfo.numMfgAgents += mfgAgentCnt;
            int numThreads = runInfo.txRate * 3;
            if (mfgAgentCnt == 0) {
                throw new RuntimeException("Cannot find MfgAgent, please ensure it gets started!");
            }
            int thrdsPerAgent = numThreads / mfgAgentCnt;
            // thrdTimeFactor += numThreads + mfgAgentCnt * 3;
            // if (runInfo.runDealerEntry >= 1)
            System.out.println("TTF1 = " + thrdTimeFactor);
            // thrdTimeFactor += 10;
            props.setProperty("plannedLines", Integer.toString(thrdsPerAgent));
        }
        if (runInfo.runLO >= 1) {
            loAgentCnt = largeOrderRefs.length;
            runInfo.numLOAgents += loAgentCnt;
            if (loAgentCnt == 0) {
                throw new RuntimeException("Cannot find LOAgent, please ensure it gets started!");
            }
            /*
             * if ((runInfo.doAudit == 1) && (loAgentCnt != auditor.getNumCategoriesLO())) { throw new RuntimeException("Error: Number
             * of LO agents started does not match number " + "of LO categories configured in the mfg domain. " + "Check
             * LargeOrderEnt's categories env. property in mfg.xml"); }
             */
        }
        // Recalculate the trigger time or wait time
        // Add 5 sec buffer time after all threads have
        // started before trigger.
        if (runInfo.msBetweenThreadStart >= 0) {
            System.out.println("ttf = " + thrdTimeFactor);
            int minTriggerTime = 5 + runInfo.msBetweenThreadStart * thrdTimeFactor / 1000;
            if (runInfo.triggerTime < 0)
                runInfo.triggerTime = minTriggerTime;
            else if (runInfo.triggerTime < minTriggerTime) {
                System.out.println("         Minimum triggerTime of " + minTriggerTime + " required.");
                System.out.println("         Current triggerTime of " + runInfo.triggerTime + " changed to " + minTriggerTime
                        + ".");
                runInfo.triggerTime = minTriggerTime;
            }
        } else if (runInfo.msBetweenThreadStart < 0 && runInfo.triggerTime >= 0) {
            runInfo.msBetweenThreadStart = 1000 * (runInfo.triggerTime - 5) / thrdTimeFactor;
            if (runInfo.msBetweenThreadStart < 1)
                runInfo.msBetweenThreadStart = 1;
            int minTriggerTime = 5 + runInfo.msBetweenThreadStart * thrdTimeFactor / 1000;
            if (runInfo.triggerTime < minTriggerTime) {
                System.out.println("         Minimum triggerTime of " + minTriggerTime + " required.");
                System.out.println("         Current triggerTime of " + runInfo.triggerTime + " changed to " + minTriggerTime
                        + ".");
                runInfo.triggerTime = minTriggerTime;
            }
        } else {
            throw new RuntimeException("Neither triggerTime nor msBetweenThreadStart is configured, exiting");
        }
        runInfo.benchStartTime = timer.getTime() + runInfo.triggerTime * 1000;
        Debug.println("triggerTime = " + runInfo.triggerTime + " seconds");
        Debug.println("benchStartTime = " + runInfo.benchStartTime);
        props.setProperty("benchStartTime", Integer.toString(runInfo.benchStartTime));
        props.setProperty("triggerTime", Integer.toString(runInfo.triggerTime));
        props.setProperty("msBetweenThreadStart", Integer.toString(runInfo.msBetweenThreadStart));
        if (runInfo.runDealerEntry >= 1) {
            Remote[] refs = dealerRefs;
            System.out.println("Configuring " + refs.length + " DealerAgent(s)...");
            for (int i = 0; i < refs.length; i++) {
                // If there are remaining threads left, assign them to
                // to the last agent. Ditto for txRate
                if (i == refs.length - 1) {
                    dlrThrdsPerAgent += dlrRemThrds;
                    props.setProperty("threadsPerAgent", Integer.toString(dlrThrdsPerAgent));
                    int rem = runInfo.txRate % dlrAgentCnt;
                    props.setProperty("txRatePerAgent", Integer.toString(runInfo.txRate / dlrAgentCnt + rem));
                }
                ((Agent) refs[i]).configure(props, timer);
            }
        }
        if (runInfo.runMfg >= 1) {
            // The workerrate assumes that each workorder will take 5 seconds
            // Since there are 3 threads, one workorder will finish in 5/3 seconds.
            props.setProperty("woRatePerAgent", Double.toString((double) runInfo.txRate / (1.66667 * mfgAgentCnt)));
            /*
             * if (largeOrderLine != null) { runInfo.runLargeOrderLine = true; System.out.println("Configuring
             * LargeOLAgent..."); largeOrderLine.configure(props, timer); } else { runInfo.runLargeOrderLine = false;
             * System.out.println("Warning: MfgAgents configured, but LargeOLAgent missing."); System.out.println("
             * LargeOrderLine will not be run."); }
             */
            Remote[] refs = mfgRefs;
            System.out.println("Configuring " + refs.length + " MfgAgent(s)...");
            for (int i = 0; i < refs.length; i++) {
                ((Agent) refs[i]).configure(props, timer);
            }
        }
        if (runInfo.runLO >= 1) {
            // The workerrate assumes that each workorder will take 5 seconds
            // Since there are 3 threads, one workorder will finish in 5/3 seconds.
            props.setProperty("woRatePerAgent", Double.toString((double) runInfo.txRate / (1.66667 * mfgAgentCnt)));
            runInfo.runLargeOrderLine = true;
            Remote[] refs = largeOrderRefs;
            System.out.println("Configuring " + refs.length + " LargeOLAgent(s)...");
            for (int i = 0; i < refs.length; i++) {
                ((Agent) refs[i]).configure(props, timer);
            }
            // largeOrderLine.configure(props, timer);
        }
        // tony
        System.out.println("");
        System.out.println("");
        long tuel_timerOffset = timer.getOffsetTime();
        long tuel_start = tuel_timerOffset + (long) runInfo.benchStartTime;
        System.out.println("Rampup      = " + new Date(tuel_start));
        long tuel_steady = tuel_start + (long) runInfo.rampUp;
        System.out.println("SteadyState = " + new Date(tuel_steady));
        long tuel_rampdown = tuel_steady + (long) runInfo.stdyState;
        System.out.println("Rampdown    = " + new Date(tuel_rampdown));
        long tuel_done = tuel_rampdown + (long) runInfo.rampDown;
        System.out.println("Finish      = " + new Date(tuel_done));
        System.out.println("");
        // tony
    }

    /**
     * Tell the agents to start the run execution Note that the Agent's run method call is non-blocking i.e the Driver does not
     * wait for an Agent. Instead, we wait for the total length of the run, after we signal all the agents to start.
     */
    protected void executeRun() {
        Remote refs[];
        StatsWriter sw = null;
        RTStats rtstats = new RTStats();
        /* Now wait for the run to start */
        int delay = timer.getTime();
        int sleepTime = runInfo.benchStartTime - delay;
        System.out.println("sleeptime is " + sleepTime + " note this is time in excess needed for trigger ");
        if (sleepTime <= 0) {
            throw new RuntimeException("triggerTime set too short for thread startup.\nPlease increase by at least "
                    + (int) Math.ceil(Math.abs(sleepTime / 1000.0)) + " and rerun.");
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }
        // Start thread to dump stats for charting
        if (runInfo.dumpStats == 1) {
            System.out.println("Starting StatsWriter ...");
            sw = new StatsWriter();
        }
        System.out.println("Starting Ramp Up...");
        EventHandler eventHandler = new EventHandlerImpl();
        eventHandler.rampUpStart();
        // wait for rampUp
        try {
            Thread.sleep(runInfo.rampUp);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        eventHandler.rampUpEnd();
        System.out.println("Starting Steady State...");
        eventHandler.steadyStateStart();
        // start the RTStats thread
        rtstats.start();
        // sleep for steady state
        // delay = runInfo.rampUp + runInfo.stdyState;
        try {
            Thread.sleep(runInfo.stdyState);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }
        eventHandler.steadyStateEnd();
        /*
         * if (runInfo.runMfg >= 1 && runInfo.doAudit == 1) { try { auditor.getDeliveryStats(); } catch (RemoteException re) {
         * System.err.println("Driver: RemoteException got " + re); } }
         */
        rtstats.quit();
        System.out.println("Starting Ramp Down...");
        delay = runInfo.rampDown + 60000;
        eventHandler.rampDownStart();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }
        eventHandler.rampDownEnd();
        /* Start gathering stats */
        DealerAggStats dealerResults[] = null;
        MfgStats mfgResults[] = null;
        try {
            if (runInfo.runDealerEntry >= 1) {
                dealerResults = new DealerAggStats[runInfo.numDealerAgents];
                refs = dealerRefs;
                System.out.println("Gathering DealerStats ...");
                for (int i = 0; i < refs.length; i++) {
                    dealerResults[i] = (DealerAggStats) (((Agent) refs[i]).getResults());
                }
            }
            if (runInfo.runMfg >= 1) {
                if (runInfo.runLO < 1)
                    mfgResults = new MfgStats[runInfo.numMfgAgents];
                else
                    mfgResults = new MfgStats[runInfo.numMfgAgents + runInfo.numLOAgents];
                refs = mfgRefs;
                System.out.println("Gathering MfgStats ...");
                int i;
                int j = refs.length;
                for (i = 0; i < refs.length; i++) {
                    mfgResults[i] = (MfgStats) (((Agent) refs[i]).getResults());
                }
                if (runInfo.runLO >= 1) {
                    refs = largeOrderRefs;
                    for (i = 0; i < refs.length; i++) {
                        mfgResults[j + i] = (MfgStats) (((Agent) refs[i]).getResults());
                    }
                }
            }
        } catch (RemoteException re) {
            System.err.println("Driver: RemoteException got " + re);
        }
        // Adjust times to real times
        runInfo.start = runInfo.benchStartTime + timer.getOffsetTime();
        // Consolidate the reports from all the agents
        SPECjAppServerReport ecReport = new SPECjAppServerReport();
        ecReport.generateReport(runInfo, dealerResults, mfgResults);
        /*
         * if (runInfo.doAudit == 1) { // Auditor will validate the report try { auditor.validateReport(ecReport);
         * 
         * //now run the second cache test auditor.cacheTest(1);
         *  // now run the atomicity tests auditor.atomicityTests();
         *  } catch (RemoteException re) { System.err.println("Driver: RemoteException got " + re); } catch (Exception e) {
         * e.printStackTrace(); System.err.println("Driver: Error from Auditor " + e); } }
         */
        // Tell StatsWriter to quit
        if (runInfo.dumpStats == 1 && sw != null) {
            System.out.println("Quitting StatsWriter...");
            sw.quit();
        }
        System.out.println(SPECjAppServerReport.version + " Results");
        System.out.println("JOPS: " + (ecReport.mfgTxRate + ecReport.dlrTxRate));
        System.out.println("Dealer Response Times");
        System.out.println("     Purchase..." + ecReport.dlrReport.purchaseResp90);
        System.out.println("     Manage....." + ecReport.dlrReport.manageResp90);
        System.out.println("     Browse....." + ecReport.dlrReport.browseResp90);
        System.out.println("Manufacturing Response Times");
        System.out.println("     Mfg........" + ecReport.mfgReport.resp90);
        return;
    }

    private class RTStats extends Thread {
        boolean endFlag = false;

        public RTStats() {
        }

        public void run() {
            boolean doOrdersAgent = true;
            boolean die = false;
            while (!die) {
                try {
                    // sleep for a while
                    Thread.sleep(10000);
                    if (doOrdersAgent) {
                        if (runInfo.runDealerEntry >= 1) {
                            DealerAggStats curDealerResults[] = new DealerAggStats[runInfo.numDealerAgents];
                            int i;
                            for (i = 0; i < dealerRefs.length; i++) {
                                curDealerResults[i] = (DealerAggStats) (((Agent) dealerRefs[i]).getCurrentResults());
                            }
                            DealerReport dRep = new DealerReport();
                            dRep.dumpRealTimeStats(curDealerResults, true); // null pointer here
                        }
                    } else // MfgAgent
                    {
                        if (runInfo.runMfg >= 1) {
                            MfgStats curMfgResults[] = new MfgStats[runInfo.numMfgAgents + runInfo.numLOAgents];
                            int i;
                            for (i = 0; i < mfgRefs.length; i++) {
                                curMfgResults[i] = (MfgStats) (((Agent) mfgRefs[i]).getCurrentResults());
                            }
                            int j;
                            for (j = 0; j < largeOrderRefs.length; j++) {
                                curMfgResults[i + j] = (MfgStats) (((Agent) largeOrderRefs[j]).getCurrentResults());
                            }
                            MfgReport mRep = new MfgReport();
                            mRep.dumpRealTimeStats(curMfgResults, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                doOrdersAgent = !doOrdersAgent;
                die = getQuit();
            }
        }

        private synchronized boolean getQuit() {
            return endFlag;
        }

        public synchronized void quit() {
            endFlag = true;
        }
    }

    private class DumpListener extends Thread {
        String resource;
        List resourceList;
        int elapsed = 0; // current time position

        // to zero set new streams.
        public DumpListener(String resource, List resourceList) {
            this.resource = resource;
            this.resourceList = resourceList;
            setDaemon(true);
            start();
        }

        public int getSocketPort() {
            int port = 0;
            for (int i = 0; i < resource.length(); i++) {
                char c = resource.charAt(i);
                if (c < '0' || c > '9')
                    return -1;
            }
            try {
                port = Integer.parseInt(resource);
            } catch (NumberFormatException e) {
                return -1;
            }
            return port;
        }

        public void run() {
            int port = getSocketPort();
            try {
                // Check if it is a simple file
                if (port == -1) {
                    resourceList.add(new DataOutputStream(new FileOutputStream(resource)));
                } else {
                    // Here we act as a server
                    ServerSocket sock = new ServerSocket(port);
                    for (;;)
                        /*
                         * IOException on one connection should not terminate the loop. So we catch it internally.
                         */
                        try {
                            DataOutputStream dumpStream = new DataOutputStream(sock.accept().getOutputStream());
                            // Zero set the current time position
                            int elapsed = this.elapsed;
                            // Avoid locking, use atomic op
                            // to capture value.
                            if (elapsed > 0) {
                                dumpStream.writeDouble(elapsed);
                                dumpStream.writeDouble(0);
                                dumpStream.writeDouble(0);
                            }
                            // Then let people write to it.
                            resourceList.add(dumpStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class StatsWriter extends Thread {
        DumpListener dlrListener = null;
        DumpListener mfgListener = null;
        boolean endFlag = false;
        DealerAggStats curOrdResults[];
        MfgStats curMfgResults[];
        DealerReport dRep;
        MfgReport mRep;
        Remote refs[];
        long dumpInterval;
        int dumpSecs;

        public StatsWriter() {
            List dealerTargets = Collections.synchronizedList(new ArrayList());
            List mfgTargets = Collections.synchronizedList(new ArrayList());
            dlrListener = new DumpListener(props.getProperty("dlrDumpTarget"), dealerTargets);
            mfgListener = new DumpListener(props.getProperty("mfgDumpTarget"), mfgTargets);
            String dumpInt = props.getProperty("dumpInterval", "5");
            dumpSecs = Integer.parseInt(dumpInt);
            dRep = new DealerReport(dealerTargets, dumpSecs, runInfo.rampUp / 1000);
            mRep = new MfgReport(mfgTargets, dumpSecs, runInfo.rampUp / 1000);
            dumpInterval = dumpSecs * 1000;
            // Make millis so we do not
            // have to re-calculate.
            start();
        }

        public void run() {
            long baseTime = System.currentTimeMillis();
            // Loop, sleeping for dumpInterval and then dump stats
            while (!endFlag) {
                baseTime += dumpInterval;
                for (;;)
                    /*
                     * This algorithm may not be very accurate but accurate enough. The more important thing is it adjusts for
                     * cumulative errors/delays caused by other ops, network, and environment.
                     */
                    try {
                        // Adjust for time spent in other ops.
                        long sleepTime = baseTime - System.currentTimeMillis();
                        // Only sleep the remaining time.
                        if (sleepTime > 0)
                            Thread.sleep(sleepTime);
                        /*
                         * Break loop when sleep complete or no time left to sleep.
                         */
                        break;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                try {
                    if (runInfo.runDealerEntry >= 1) {
                        curOrdResults = new DealerAggStats[runInfo.numDealerAgents];
                        refs = dealerRefs;
                        // System.out.println("Gathering interim dealerStats ...");
                        for (int i = 0; i < refs.length; i++) {
                            curOrdResults[i] = (DealerAggStats) (((Agent) refs[i]).getCurrentResults());
                        }
                        dRep.dumpStats(curOrdResults);
                    }
                    /*
                     * if (runInfo.runMfg >= 1) { if (runInfo.runLO < 1) mfgResults = new MfgStats[runInfo.numMfgAgents]; else
                     * mfgResults = new MfgStats[runInfo.numMfgAgents + runInfo.numLOAgents]; refs = mfgRefs;
                     * System.out.println("Gathering MfgStats ..."); int i; int j = refs.length; for (i = 0; i < refs.length;
                     * i++) { mfgResults[i] = (MfgStats)(((Agent)refs[i]).getResults()); } if (runInfo.runLO >= 1){ refs =
                     * largeOrderRefs; for (i = 0; i < refs.length; i++) { mfgResults[j+i] =
                     * (MfgStats)(((Agent)refs[i]).getResults()); } } }
                     */
                    if (runInfo.runMfg >= 1) {
                        if (runInfo.runLO < 1)
                            curMfgResults = new MfgStats[runInfo.numMfgAgents];
                        else
                            curMfgResults = new MfgStats[runInfo.numMfgAgents + runInfo.numLOAgents];
                        refs = mfgRefs;
                        System.out.println("Gathering MfgStats ...");
                        int i;
                        int j = refs.length;
                        for (i = 0; i < refs.length; i++) {
                            curMfgResults[i] = (MfgStats) (((Agent) refs[i]).getResults());
                        }
                        if (runInfo.runLO >= 1) {
                            refs = largeOrderRefs;
                            for (i = 0; i < refs.length; i++) {
                                curMfgResults[j + i] = (MfgStats) (((Agent) refs[i]).getResults());
                            }
                        }
                        mRep.dumpStats(curMfgResults);
                    }
                } catch (RemoteException re) {
                    System.err.println("Driver: RemoteException got " + re);
                }
                // Intentionally do this last.
                dlrListener.elapsed += dumpSecs;
                mfgListener.elapsed += dumpSecs;
            }
        }

        void quit() {
            endFlag = true;
        }
    }

    /**
     * Starts the driver.
     * 
     * @param argv
     *            The driver command line arguments
     * @throws Exception
     */
    public static void main(String[] argv) throws Exception {
        if (argv.length < 1 || argv.length > 1) {
            System.err.println("Usage: Driver <properties_file>");
            return;
        }
        String propsfile = argv[0];
        new Driver(propsfile);
    }
}
