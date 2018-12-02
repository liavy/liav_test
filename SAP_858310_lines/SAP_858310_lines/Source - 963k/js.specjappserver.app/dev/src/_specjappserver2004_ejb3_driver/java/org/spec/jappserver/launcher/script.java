/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ----------------------------------------------------------------------
 *  2001        Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2004/04/05  Akara Sucharitakul, SUN   Added shutdown hook to ensure child processes get terminated when
 *                                        Ctrl-C is pressed (osgjava-6782,6864).
 *
 * $Id: Script.java,v 1.4 2004/04/05 08:34:45 skounev Exp $
 */

package org.spec.jappserver.launcher;

import java.io.IOException;

/**
 * Superclass of all scripts. Provides framework, initialization,
 * and termination services, and utility methods. All scripts are
 * supposed to inherit from this class.
 * @author Akara Sucharitakul
 */
public abstract class Script {

    String[] args;
    String fs = null;
    String ps = null;
    Environment env = null;

    /**
     * Method main contains initialization and termination services.
     * @param args Arguments passed in from the command line.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Runtime.getRuntime().addShutdownHook(new ProcessTerminator());
                Script script = (Script) Class.forName(
                                "org.spec.jappserver.launcher." +
                                args[0]) .newInstance();

                script.args = new String[args.length - 1];

                System.arraycopy(args, 1, script.args,
                             0, script.args.length);

                script.runScript();

            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                Launcher.destroyAll();
            }
        }
    }

    /**
     * Script constructor initializes often used parameters for subclass.
     */
    public Script() {

        fs = System.getProperty("file.separator");
        ps = System.getProperty("path.separator");

        try {
            env = new Environment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method sleep provides accurate sleeping services without interruption.
     * @param millis Milliseconds to sleep
     */
    public void sleep(long millis) {
        long end = System.currentTimeMillis() + millis;
        for (;;)
            try {
                Thread.sleep(end - System.currentTimeMillis());
                break;
            } catch (InterruptedException e) {
               e.printStackTrace();
            } catch (IllegalArgumentException e) {
                break; // If sleep value goes below 0
            }
    }

    /**
     * Method runScript is the real script content. It is supposed to be
     * implemented by Script subclasses.
     * @exception Exception Any uncaught exception in the script.
     */
    public abstract void runScript() throws Exception;


    /**
     * The ProcessTerminator is a shutdown hook used to terminate
     * all child processes.
     */
    static class ProcessTerminator extends Thread {

        /**
         * Run will get called when the JVM shuts down.
         * All child processes will get terminated.
         */
        public void run() {
            Launcher.destroyAll();
        }
    }
}
