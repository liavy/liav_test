/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.supplier.helper;


//Import statements

/**
 * This class will implement secure sockets.
 *
 *
 * @author Damian Guy
 */
public class SecureXmlCommand extends SendXmlCommand {

    /**
     * Constructor SecureXmlCommand
     *
     *
     * @param hostname
     * @param xml
     * @param servlet
     * @param port
     *
     */
    public SecureXmlCommand(String hostname, String xml, String servlet,
                            int port) {
        super(hostname, xml, servlet, port);
    }

    /**
     * Method execute
     *
     *
     */
    public void execute() {
    }
}

