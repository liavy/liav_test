 /*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2001        Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: Launcher.java,v 1.2 2004/02/17 17:16:03 skounev Exp $
 */

package org.spec.jappserver.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Launcher wraps the Runtime.exec() call and provides standard input,
 * output, and error redirection for the child process. This redirection
 * defaults to the parent process' stdin, stdout, and stderr unless set
 * otherwise.
 * @author Akara Sucharitakul
 * @see Environment
 */
public class Launcher implements InterruptNotifyable {

    String[] cmd;
    String[] env;
    Process proc;
    PrintStream out = System.out;
    PrintStream err = System.err;
    InputStream in = System.in;
    String outMatch;
    String errMatch;

    boolean running;
    int exitValue;
    int notifyValue;

    private static ArrayList procList = null;


    /**
     * Constructs a launcher.
     * @param cmd The command and arguments
     * @param env The environment
     */
    public Launcher(List aCmd, String[] env) {

        if (procList == null)
            procList = new ArrayList();

        this.cmd = new String[aCmd.size()];
        this.cmd = (String[]) aCmd.toArray(this.cmd);
        this.env = env;
        
        String driverPackage = "org.spec.jappserver.driver.";
        String agentName = null;
        for (int i = cmd.length - 1; i >= 0; i--) {
            String cmdStr = cmd[i];
            if (cmdStr.startsWith(driverPackage)) {
                if (i + 2 < cmd.length) {
                    agentName = cmd[i + 2];
                } else {
                    agentName = cmdStr.substring(driverPackage.length());
                }
                break;
            }
        }
        if (agentName != null)
        {
            String placeholder = "_agent_";
            for (int i = 0; i < cmd.length; i++) {
                String cmdStr = cmd[i];
                int pos = cmdStr.indexOf(placeholder);
                if (pos != -1) {
                    cmd[i] = cmdStr.substring(0, pos) + agentName + cmdStr.substring(pos + placeholder.length());
                }
            }
        }
    }

    /**
     * Allows notification before interrupts
     * as defined in the InterruptNotifiable interface.
     * @param value Notifaction value
     */
    public synchronized void notifyInterrupt(int value) {
        notifyValue = value;
    }

    /**
     * Allows query and reset of notification.
     * @return Value of nortification
     */
    public synchronized int interruptValue() {
        int value = notifyValue;
        notifyValue = 0;
        return value;
    }

    /**
     * Redirects the standard input for the child
     * process to come from an alternative source.
     * @param in The new InputStream source
     */
    public void setIn(InputStream in) {
        this.in = in;
    }

    /**
     * Redirects the standard output for the child
     * process to go to an alternative destination.
     * @param out The new outputStream destination
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * Redirects the standard error for the child
     * process to go to an alternative destination.
     * @param err The new error outputStream destination
     */
    public void setErr(PrintStream err) {
        this.err = err;
    }

    /**
     * Retrieves the current child's input stream source.
     * @return The child's current standard input stream source
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * Set match string for stdout.
     * @param match String to be matched
     */
    public void matchOut(String match) {
        if (running)
            throw new IllegalThreadStateException(
                "Cannot set match string while process is running!");
        else
            outMatch = match;
    }

    /**
     * Set match string for stderr.
     * @param match String to be matched
     */
    public void matchErr(String match) {
        if (running)
            throw new IllegalThreadStateException(
                "Cannot set match string while process is running!");
        else
            errMatch = match;
    }

    /**
     * Retrieves the current child's output stream destination.
     * @return The child's current standard output stream destination
     */
    public PrintStream getOut() {
        return out;
    }

    /**
     * Retrieves the current child's error output stream destination.
     * @return The child's current standard error output stream destination
     */
    public PrintStream getErr() {
        return err;
    }

    /**
     * Checks is process is still running.
     * @return true if process is still running, false otherwise
     */
    public boolean isRunning() {
        if (running)
            try {
                exitValue = proc.exitValue();
                running = false;
            } catch (IllegalThreadStateException e) {
               e.printStackTrace();
                // Still running, let running be true.
            }
        return running;
    }

    /**
     * Launches the command in the background.
     * @exception IOException I/O error on the input, output, or error streams
     */
    public void bgExec() throws IOException {

        /* // Please uncomment for debug info
         * for (int i = 0; i < cmd.length; i++) {
         *     System.err.print(cmd[i]);
         *     System.err.print(" ");
         * }
         * System.err.println("");
         */

        Thread thisThread = Thread.currentThread();
        ThreadGroup group = thisThread.getThreadGroup();
        proc = Runtime.getRuntime().exec(cmd, env);

        running = true;

        procList.add(0, proc);

        Thread inConnector  = new Thread(group, new StreamConnector(
                                              in, proc.getOutputStream()));
        Thread outConnector = new Thread(group, new StreamConnector(
                                              proc.getInputStream(), out,
                                              thisThread, this, outMatch));
        Thread errConnector = new Thread(group, new StreamConnector(
                                              proc.getErrorStream(), err,
                                              thisThread, this, errMatch));
        inConnector.setDaemon(true);
        inConnector.start();
        outConnector.setDaemon(true);
        outConnector.start();
        errConnector.setDaemon(true);
        errConnector.start();

    }

    /**
     * Waits until the err or out streams are matched with
     * the match string or the process terminates. If the
     * match strings are not set, this method will return
     * immediately.
     * @see matchOut
     * @see matchErr
     */
    public void waitMatch() {
        if ((outMatch != null && outMatch.length() != 0) ||
            (errMatch != null && errMatch.length() != 0))
            while (notifyValue != InterruptNotifyable.MATCH && running)
                try {
                    exitValue = proc.waitFor();
                    running = false;
                } catch (InterruptedException e) {
                   System.out.println("Intentionally interrupted");
                }
    }

    /**
     * Waits for the process to exit.
     * @return The exit value of the process
     */
    public int waitFor() {
        while (running)
            try {
                exitValue = proc.waitFor();
                running = false;
                procList.remove(procList.indexOf(proc));
                break;
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

        return exitValue;
    }

    /**
     * Kills the process being launched.
     */
    public void destroy() {
        proc.destroy();
    }

    /**
     * Kills all processes being launched by Launcher.
     * This is useful for exception handling to be called
     * before exiting the application.
     */
    public static void destroyAll() {
        if (procList != null)
            while (procList.size() > 0)
                ((Process) procList.remove(0)).destroy();
    }

    /**
     * Launches the command in the foreground and waits for completion.
     * @return The exit value of the process
     * @exception IOException I/O error on the input, output, or error streams
     */
    public int exec() throws IOException {
        bgExec();
        return waitFor();
    }
}
