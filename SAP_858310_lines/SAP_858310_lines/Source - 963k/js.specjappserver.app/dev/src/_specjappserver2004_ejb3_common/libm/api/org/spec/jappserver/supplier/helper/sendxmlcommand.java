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
import java.io.IOException;

import org.spec.jappserver.common.SPECjAppServerException;


/**
 * This is the abstract base class for
 * all HTTP transactions in the supplier domain.
 * Implements the Command Pattern [GOF]
 *
 *
 * @author Damian Guy
 */
public abstract class SendXmlCommand {

    String       hostname;
    String       xml;
    String       servlet;
    int          port;
    final String HTTP           = "HTTP/1.1";
    final String METHOD         = "POST ";
    final String CONTENT_TYPE   =
    "Content-type: application/x-www-form-urlencoded";
    final String CONTENT_LENGTH = "Content-length: ";

    /**
     * Constructor SendXmlCommand
     *
     *
     * @param hostname
     * @param xml
     * @param servlet
     * @param port
     *
     */
    public SendXmlCommand(String hostname, String xml, String servlet,
                          int port) {

        this.hostname = hostname;
        this.xml      = xml;
        this.servlet  = servlet;
        this.port     = port;
    }

    /**
     * Method execute
     *
     *
     * @throws SPECjAppServerException
     *
     */
    public abstract void execute() throws IOException, SPECjAppServerException;
}

