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
import java.io.Serializable;


/**
 * This class represents a Part supplied buy a particular supplier.
 *
 *
 * @author Damian Guy
 */
public class PartSpec implements Serializable {

    /** added field suppID **/
    public Integer suppID;
    public String  pID;
    public double  price;
    public int     qty;
    public double  disc;
    public int     delDate;

    /**
     * Constructor PartSpec
     *
     *
     * @param suppID
     * @param pID
     * @param price
     * @param qty
     * @param disc
     * @param delDate
     *
     */
    public PartSpec(Integer suppID, String pID, double price, int qty,
                    double disc, int delDate) {

        this.suppID  = suppID;
        this.pID     = pID;
        this.price    = price;
        this.qty      = qty;
        this.disc     = disc;
        this.delDate = delDate;
    }

    /**
     * Constructor PartSpec
     *
     *
     */
    public PartSpec() {
    }

    /**
     * Method calculatePrice
     *
     *
     * @return
     *
     */
    public double calculatePrice() {
        return(price - price * disc) / qty;
    }
}

