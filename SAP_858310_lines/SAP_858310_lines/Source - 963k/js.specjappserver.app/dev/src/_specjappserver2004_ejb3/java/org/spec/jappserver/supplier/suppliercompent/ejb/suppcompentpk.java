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

package org.spec.jappserver.supplier.suppliercompent.ejb;


//Import statements
import java.io.Serializable;


/**
 * Primary Key class for SupplierCompEJB
 *
 *
 * @author Damian Guy
 */
public class SuppCompEntPK implements Serializable {

    public String  suppCompID;
    public int suppCompSuppID;

    /**
     * Constructor SuppCompEntPK
     *
     *
     */
    public SuppCompEntPK() {
    }

    /**
     * Constructor SuppCompEntPK
     *
     *
     * @param pID
     * @param suppID
     *
     */
    public SuppCompEntPK(String pID, int suppID) {
        this.suppCompID    = pID;
        this.suppCompSuppID = suppID;
    }

    /**
     * Method hashCode
     *
     *
     * @return
     *
     */
    public int hashCode() {
        return suppCompID.hashCode() | suppCompSuppID;
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

        if( rhs instanceof SuppCompEntPK ) {
            SuppCompEntPK other = (SuppCompEntPK) rhs;

            return this.suppCompID.equals(other.suppCompID)
            && (this.suppCompSuppID == other.suppCompSuppID);
        }

        return false;
    }
}

