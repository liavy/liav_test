/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.common;


/**
 * NotReadyException indicates that the operation being performed
 * hits a "not ready" state and should be retried at a later point
 * in time. It is up to the application logic to schedule for a
 * retry if this exception is received.
 *
 * @author Akara Sucharitakul
 * @version %I%, %G%
 */
public class NotReadyException extends SPECjAppServerException {

    /**
     * Catches exceptions without a specified string
     *
     */
    public NotReadyException() {
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public NotReadyException(String message) {
        super(message);
    }
}

