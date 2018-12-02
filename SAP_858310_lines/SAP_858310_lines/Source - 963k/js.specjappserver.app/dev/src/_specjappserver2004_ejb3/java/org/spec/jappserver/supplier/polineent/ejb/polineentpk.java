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

package org.spec.jappserver.supplier.polineent.ejb;


//Import statements
import java.io.Serializable;


/**
 * Primary Key class for POLine Entity Bean.
 *
 *
 * @author Damian Guy
 */
public class POLineEntPK implements Serializable {

    public int poLineNumber;
    public Integer poLinePoID;

    /**
     * Constructor POLineEntPK
     *
     *
     */
    public POLineEntPK() {
    }

    /**
     * Constructor POLineEntPK
     *
     *
     * @param poLineNumber
     * @param poLinePoID
     *
     */
    public POLineEntPK(int poLineNumber, Integer poLinePoID) {
        this.poLineNumber = poLineNumber;
        this.poLinePoID  = poLinePoID;
    }

    /**
     * Method hashCode
     *
     *
     * @return
     *
     */
    public int hashCode() {
        return poLineNumber | poLinePoID.intValue();
    }

    /**
     * Method equals
     *
     *
     * @param rhs
     *
     * @return
     *
     */
    public boolean equals(Object rhs) {

        if( rhs instanceof POLineEntPK ) {
            POLineEntPK other = (POLineEntPK) rhs;

            return(other.poLineNumber == poLineNumber)
            && (poLinePoID.equals(other.poLinePoID));
        }

        return false;
    }
}

