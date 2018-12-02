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
 * Class ProcessingErrorException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class ProcessingErrorException extends Exception {

    private Exception nextException;

    /**
     * Catches exceptions without a specified string
     *
     */
    public ProcessingErrorException() {
    }

    /**
     * Constructor ProcessingErrorException
     *
     *
     * @param previous
     *
     */
    public ProcessingErrorException(Exception previous) {
        nextException = previous;
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public ProcessingErrorException(String message) {
        super(message);
    }

    /**
     * Constructor ProcessingErrorException
     *
     *
     * @param previous
     * @param message
     *
     */
    public ProcessingErrorException(Exception previous, String message) {

        super(message);

        nextException = previous;
    }

    /**
     * Method getNextException
     *
     *
     * @return
     *
     */
    public Exception getNextException() {
        return nextException;
    }
}

