/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*
* This source code is provided as is, without any express or implied warranty.
*
*  History:
*  Date        ID, Company               Description
*  ----------  ------------------------  --------------------------------------------------------------
*  2002/01/22  Akara Sucharitakul, SUN   Created.
*  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
*  2002/04/19  Matt Hogstrom, IBM        Modified to support launching multiple agents. 
*  2002/10/07  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
*                                        SPECjAppServer2002 (EJB2.0).
*  2004/02/08  John Stecher, IBM         Modified code to support launching multiple LOAgents.
*  2004/02/16  Samuel Kounev, Darmstadt  Integrated bug fix provided by Tom Daly to address 
*                                        problem when running a satellite driver (osgjava-6348).
*  2004/02/17  Samuel Kounev, Darmstadt  Fixed to get rid of the dummy command line argument
*                                        when starting in satellite mode.
*  2004/04/20  Samuel Kounev, Darmstadt  Made the post run wait time for satellite driver to be 
*                                        configurable using the satPostRunWaitTime parameter.
*/

package org.spec.jappserver.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Script to drive the benchmark.
 * @author 
 */
public class Driver extends Script {

    String driverHost;
    Properties runProps;

    /**
     * Method contains all the scripts to drive the benchmark.
     */
    public void runScript() throws Exception {
      String  value;
      boolean runAsSatellite = false;
      boolean redirectStderr = false;

        if (args.length > 0)
            driverHost = args[0];

        if (driverHost == null)
            driverHost = System.getProperty("node.name", "localhost");

        System.out.println("Driver Host: " + driverHost);

        // Get the JAVA_HOME
        String javaHome = env.get("JAVA_HOME");
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        if (!javaHome.endsWith(fs))
            javaHome += fs;

        String configDir = "config" + fs;

        // Set the JAVA parameter
        final String javaArgs = env.get("JAVA_ARGS");
        final String javaCmd;
        if (javaArgs == null) {
            javaCmd = javaHome + "bin" + fs + "java";
        } else {
            javaCmd = javaHome + "bin" + fs + "java" + " " + javaArgs;
        }
        System.err.println("Executing java processes with command line: >>" + javaCmd + "<<.");
        // Check BINDWAIT
        int bindWait = 5000;
        String bindWaitStr = env.get("BINDWAIT");
        if (bindWaitStr != null)
            bindWait = Integer.parseInt(bindWaitStr) * 1000;
        else
           env.set("BINDWAIT", "5");

        // Read run.properties
        runProps = new Properties();
        runProps.load(new FileInputStream(configDir + "run.properties"));
      
        // Get satellire value - this indicates if we should bypass rmiregistry, controller and driver
        // M Hogstrom
        value = runProps.getProperty("satellite");
        if (value != null) {
          runAsSatellite = Boolean.valueOf(value.trim()).booleanValue();
        }

        // Get stderr value - this indicates if we should redirect stderr to files
        // M Hogstrom
        value = runProps.getProperty("redirectStderr");
        if (value != null) {
          redirectStderr = Boolean.valueOf(value.trim()).booleanValue();
        }


        // Check dumpStats
        boolean dumpStats = true;
        if ("0".equals(runProps.getProperty("dumpStats").trim()))
            dumpStats = false;

        /* showChart specifies if the chart should be launched.
         * It will only be launched if dumpStats is true as well.
         */
        int showChartInt = Integer.parseInt(runProps.getProperty("showChart", "-1").trim());

        /* Derive the showChart value:
         * 0 = false
         * 1 = true only if dumpStats is true
         * otherwise showChart equals dumpStats
         */
        boolean showChart = dumpStats;

        if (showChartInt == 0)
            showChart = false;
        else if (showChartInt > 0 && dumpStats)
            showChart = true;
            
        // Prepare the environment for the run
        String driverPolicy = configDir + fs + "security" + fs +
                              "driver.policy";
        String driverPackage = "org.spec.jappserver.driver.";
        String[] environment = env.getList();

        ArrayList cmd = new ArrayList(8);

        // rmi registry
        // Bug Id: 4487500 Fix
        String rmiCmd = javaHome + "bin" + fs + "rmiregistry";
        cmd.add(runProps.getProperty("rmiCommand", rmiCmd));
        Launcher rmiReg = new Launcher(cmd, environment);
        // rmiReg.setErr(new PrintStream(new FileOutputStream("rmi.err")));
        // rmiReg.setOut(new PrintStream(new FileOutputStream("rmi.out")));

        // controller
        cmd.clear();
        StringTokenizer st = new StringTokenizer(javaCmd);
        int cmdLen = 0;
        for (; st.hasMoreTokens(); cmdLen++)
            cmd.add(st.nextToken());
        cmd.add("-Djava.security.policy=" + driverPolicy);
        cmd.add(driverPackage + "ControllerImpl");
        Launcher controller = new Launcher(cmd, environment);

        if (redirectStderr) 
          controller.setErr(new PrintStream(new FileOutputStream("Controller.err")));
        
        controller.matchOut("Binding controller to /");

        // Modified by Ramesh. App server context will be 
        // located using -D option set in the appserver.env file.

        // dealer agent
        int numDealerAgents = Integer.parseInt(runProps.getProperty("runDealerEntry"));
        int dealerEntryStartingNumber;
        String t1 = runProps.getProperty("dealerEntryStartingNumber");
        if (t1 != null) {
          dealerEntryStartingNumber = Integer.parseInt(runProps.getProperty("dealerEntryStartingNumber"));
        } else {
          dealerEntryStartingNumber = 1;
        }

        Launcher dealerAgent[] = new Launcher[numDealerAgents];
        cmd.set(cmdLen + 1, driverPackage + "DealerAgent");
        cmd.add(configDir + "agent.properties");
        cmd.add("O"+Integer.toString(dealerEntryStartingNumber));
        cmd.add(driverHost);
        for(int i=0; i<numDealerAgents; i++) {
          dealerAgent[i] = new Launcher(cmd, environment);
          if (redirectStderr) 
            dealerAgent[i].setErr(new PrintStream(new FileOutputStream("O"+Integer.toString(dealerEntryStartingNumber)+".err")));
          cmd.set(cmdLen + 3, "O"+Integer.toString(++dealerEntryStartingNumber));
        }

        // mfg agent
        int numMfgAgents = Integer.parseInt(runProps.getProperty("runMfg"));
        int mfgStartingNumber;
        t1 = runProps.getProperty("mfgStartingNumber");
        if (t1 != null) {
          mfgStartingNumber = Integer.parseInt(runProps.getProperty("mfgStartingNumber"));
        } else {
          mfgStartingNumber = 1;
        }
        Launcher mfgAgent[] = new Launcher[numMfgAgents];
        cmd.set(cmdLen + 3, "M"+Integer.toString(mfgStartingNumber));
        for(int i=0; i<numMfgAgents; i++) {
          cmd.set(cmdLen + 1, driverPackage + "MfgAgent");
          mfgAgent[i] = new Launcher(cmd, environment);
          if (redirectStderr) 
            mfgAgent[i].setErr(new PrintStream(new FileOutputStream("M"+Integer.toString(mfgStartingNumber)+".err")));
          cmd.set(cmdLen + 3, "M"+Integer.toString(++mfgStartingNumber));
        }

        // lo agent  *** added to support multiple LO agents
        int numLOAgents = Integer.parseInt(runProps.getProperty("runLO"));
        int loStartingNumber;
        t1 = runProps.getProperty("loStartingNumber");
        if (t1 != null) {
          loStartingNumber = Integer.parseInt(runProps.getProperty("loStartingNumber"));
        } else {
          loStartingNumber = 1;
        }
        Launcher loAgent[] = new Launcher[numLOAgents];
        cmd.set(cmdLen + 3, "L"+Integer.toString(loStartingNumber));
        for(int i=0; i<numLOAgents; i++) {
          cmd.set(cmdLen + 1, driverPackage + "LargeOLAgent");
          loAgent[i] = new Launcher(cmd, environment);
          if (redirectStderr) 
            loAgent[i].setErr(new PrintStream(new FileOutputStream("L"+Integer.toString(loStartingNumber)+".err")));
          cmd.set(cmdLen + 3, "L"+Integer.toString(++loStartingNumber));
        }

        // lo agent
        /*cmd.set(cmdLen + 1, driverPackage + "LargeOLAgent");
        cmd.set(cmdLen + 3, "L1");
        Launcher loAgent[] = new Launcher[numLOAgents];
        loAgent[0] = new Launcher(cmd, environment);
        
        if (redirectStderr) 
          loAgent[0].setErr(new PrintStream(new FileOutputStream("L1.err")));
        */

        cmd.set(cmdLen, driverPackage + "Driver");
        cmd.set(cmdLen + 1, configDir + "run.properties");
        for (int i = cmd.size() - 1; i > cmdLen + 1; i--)
            cmd.remove(i);

        Launcher driver = new Launcher(cmd, environment);
        
        if (redirectStderr) 
          driver.setErr(new PrintStream(new FileOutputStream("Driver.err")));

        driver.matchOut("Starting StatsWriter");

        // fetch the runID from the sequence file
        String runID = getRunID();

        // Call switchLog() and getLog only if runMfg 
        int runMfg = Integer.parseInt(runProps.getProperty("runMfg",
                     "-1").trim());

        /**************************************************************
         Stat Collection code. It will invoke ECstat.sh script in 
         JAS_HOME/bin directory. and pass the following args
         1. numberOfSeconds to start studyState
         2. studyState duration in seconds
         3. interval for each measurement
         4. number of measurement to be done
         5. output dir to write the stat logs
        **************************************************************/
        // Now, start the run!

        //
        // If we are not a satellite start the rmiregistry and controller
        // $ Hogstrom
        if (! runAsSatellite) {
          rmiReg.bgExec();
          controller.bgExec();
          controller.waitMatch();
        }

        for (int i = 0; i < numDealerAgents; i++) {
          dealerAgent[i].bgExec();
        }
        for (int i = 0; i < numMfgAgents; i++) {
          mfgAgent[i].bgExec();
        }

        //  Added to support multiple LOAgents
        for (int i = 0; i < numLOAgents; i++) {
          loAgent[i].bgExec();
        }
        
        // If we are not a satellite run the loagent
        // $Hogstrom
        /*if (! runAsSatellite) {
          loAgent.bgExec();
        }*/

        sleep(bindWait);

        if(runMfg > 0 && !runAsSatellite) {
          System.out.println("Calling switchLog as master");
          switchLog();
        }
    
        // If we are running local (default) start the driver
        if (! runAsSatellite) {
          driver.bgExec();
          driver.waitMatch();

          driver.waitFor();
        } else {
          // Otherwise, calculate the waittime as a remote agent.
          long waittime = Long.parseLong((String)runProps.getProperty("rampUp"));
          waittime += Long.parseLong((String)runProps.getProperty("rampDown"));
          waittime += Long.parseLong((String)runProps.getProperty("stdyState"));
          waittime += Long.parseLong((String)runProps.getProperty("triggerTime"));
          waittime += Long.parseLong((String)runProps.getProperty("satPostRunWaitTime"));
          waittime *= 1000;  // Set up for the right waittime          
          Thread.sleep(waittime);  // Wait for the run to complete
        }


        // Ending the run        

        if(runMfg > 0 && !runAsSatellite) {
          System.out.println("Calling getLog as master");
          getLog(runID);
        }

        /* We do not have to destroy all the processes. This will be done
         * automatically by the Script framework.
         */
    }

    /**
     * Switches the servlet logfile.
     * @exception IOException if request fails
     */
    void switchLog() throws IOException {

        String okMsg = "200 OK";
        URL[] url = new URL[2];

        // URL PREFIX is added for JAS and EMULATOR. By Default it is just a "/" 
        // It is an optional field so that other app server env files need not be 
        // updated with this field if it is not using any special URL PREFIX
        // RFE 4491953
        url[0] = new URL(new StringBuffer().append("http://" + env.get("EMULATOR_HOST")) 
                                           .append(':' + env.get("EMULATOR_PORT")) 
                                           .append(env.get("EMULATOR_PREFIX", "/")) 
                                           .append("Emulator/EmulatorServlet?cmd=switchlog")
                                           .toString());

        url[1] = new URL(new StringBuffer().append("http://" + env.get("JAS_HOST") + ':')
                                   .append(env.get("JAS_PORT")) 
                                   .append(env.get("JAS_PREFIX", "/")) 
                         .append("Supplier/DeliveryServlet?cmd=switchlog").toString());

        for (int i = 0; i < url.length; i++ ) {
        	System.out.println("Open connection: " +  url[i]);
            HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            boolean ok = false;
            String r = null;

            for (;;) {
                r = reader.readLine();
                if (r == null)
                    break;
                if (r.indexOf(okMsg) != -1)
                    ok = true;
            }
            reader.close();
            if (!ok) {
            	  throw new IOException("Unsuccessful switchlog");
            }
        }
    }


    /**
     * Gets the servlet log for both the emulator and delivery
     * servlet.
     * @param runID The id of the run received from getRunID
     * @exception IOException If downloading the logs fail
     * @see getRunID
     */
    void getLog(String runID) throws IOException {

        URL[] url = new URL[2];

        url[0] = new URL(new StringBuffer().append("http://" + env.get("EMULATOR_HOST")) 
                                           .append(':' + env.get("EMULATOR_PORT")) 
                                           .append(env.get("EMULATOR_PREFIX", "/")) 
                                           .append("Emulator/EmulatorServlet?cmd=getlog")
                                           .toString());

        url[1] = new URL(new StringBuffer().append("http://" + env.get("JAS_HOST") + ':')
                                   .append(env.get("JAS_PORT")) 
                                   .append(env.get("JAS_PREFIX", "/")) 
                         .append("Supplier/DeliveryServlet?cmd=getlog").toString());


        String homeDir = System.getProperty("user.home");
        String outDir = runProps.getProperty("outDir");
        if (outDir == null)
                outDir = homeDir + fs + "output";

        FileOutputStream[] outStream = new FileOutputStream[2];
        outStream[0] = new FileOutputStream(outDir + fs + runID + fs + "emulator.err");
        outStream[1] = new FileOutputStream(outDir + fs + runID + fs + "delivery.err");

        for (int i = 0; i < url.length; i++ ) {
            HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
            StreamConnector s = new StreamConnector(conn.getInputStream(), outStream[i]);
            s.run();
        }
    }

    /*
     * This method retrieves the ID for the current run, by looking
     * in the specjappserver.seq file in the user's home directory.
     * It increments the sequence file.
     */
    private String getRunID() throws IOException {
        String runId = null;
        File seqFile = new File(System.getProperty("user.home"), "specjappserver.seq");
        if (seqFile.exists()) {
            BufferedReader bufIn = new BufferedReader(
                                   new FileReader(seqFile));
            runId = bufIn.readLine();
            bufIn.close();
            runId = String.valueOf(Integer.parseInt(runId));
        }
        else {
            runId = "1";
        }
        return runId;
    }
}
