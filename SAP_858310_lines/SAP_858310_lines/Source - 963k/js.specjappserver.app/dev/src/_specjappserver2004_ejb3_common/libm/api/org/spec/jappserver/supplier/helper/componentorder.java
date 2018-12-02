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
 * Class to hold information about a component that is
 * to be ordered.
 *
 * @author Damian Guy
 */
public class ComponentOrder implements Serializable {

    public String id;
    public int    qty;
    public int    leadTime;
    public double balance;

    /**
     * Constructor ComponentOrder
     *
     *
     * @param id
     * @param qty
     * @param leadTime
     * @param balance
     *
     */
    public ComponentOrder(String id, int qty, int leadTime, double balance) {

        this.id       = id;
        this.qty      = qty;
        this.leadTime = leadTime;
        this.balance  = balance;
    }
}

