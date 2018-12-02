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
 * Class DataIntegrityException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class DataIntegrityException extends SPECjAppServerException {

    private Exception nextException;

    /**
     * Constructor DataIntegrityException
     *
     *
     * @param previous
     *
     */
    public DataIntegrityException(Exception previous) {
        super(previous);
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public DataIntegrityException(String message) {
        super(message);
    }

    /**
     * Constructor DataIntegrityException
     *
     *
     * @param previous
     * @param message
     *
     */
    public DataIntegrityException(Exception previous, String message) {
        super(previous, message);
    }

    /**
     * Method equals
     * Compares two DataIntegrityException
     *
     * @param DataIntegrityException 
     * 
     *
     */    
    public boolean equals(DataIntegrityException theOther) {
        return this.getMessage().equals(theOther.getMessage());
    }
}

