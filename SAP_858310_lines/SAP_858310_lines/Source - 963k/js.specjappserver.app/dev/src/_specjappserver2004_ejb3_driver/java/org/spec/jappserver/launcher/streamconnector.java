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
 *  2002/04/23  Matt Hogstrom, IBM        Commented out IOException prints as they are not needed.  
 *                                        Some VMs have problems when the process goes away. 
 *  2002/07/10  Russell Raymundo, BEA     Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: StreamConnector.java,v 1.2 2004/02/17 17:16:03 skounev Exp $
 */

package org.spec.jappserver.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * StreamConnector provides active piping services
 * processes.
 * @author Akara Sucharitakul
 */
class StreamConnector implements Runnable {

    InputStream input;
    OutputStream output;
    byte[] buffer;
    ByteMatcher matcher= null;
    Thread parent;
    InterruptNotifyable target;

    /**
     * Constructs a new StreamConnector connecting input to output
     * with stream matching.
     * @param input The input
     * @param output The output
     * @param parent The parent thread to be interrupted
     * @param detect String to be matched
     */
    public StreamConnector(InputStream input, OutputStream output,
                           Thread parent, InterruptNotifyable target,
                           String detect) {
        this(input, output);
        this.parent = parent;
        this.target = target;
        if (detect != null && detect.length() != 0)
            matcher = new ByteMatcher(detect);
    }

    /**
     * Constructs a new StreamConnector connecting input to output.
     * @param input The input
     * @param output The output
     */
    public StreamConnector(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        buffer = new byte[256];
    }

    /**
     * Performs buffered reads from input and writes to output.
     * Checks buffer for the detect string.
     */
    public void run() {
        try {
            for (;;) {
                int len = input.read(buffer);
                if (len < 0)
                    break;
                if (matcher != null && matcher.match(buffer, len)) {
                    target.notifyInterrupt(InterruptNotifyable.MATCH);
                    parent.interrupt();
                    matcher = null;
                }
                output.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (!(output.equals(System.out) ||
                  output.equals(System.err)))
                try {
                    output.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }
}
