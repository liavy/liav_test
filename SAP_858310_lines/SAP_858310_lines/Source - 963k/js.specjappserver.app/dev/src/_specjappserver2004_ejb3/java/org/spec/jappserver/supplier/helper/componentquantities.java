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

import org.spec.jappserver.supplier.scomponentent.ejb.SComponentEntLocal;


/**
 * Class to hold a Component and the quantity that is
 * required.
 *
 * @author Damian Guy
 */
public class ComponentQuantities implements Serializable {

    public SComponentEntLocal component;
    public int           quantity;

    /**
     * Constructor ComponentQuantities
     *
     *
     * @param component
     * @param qty
     *
     */
    public ComponentQuantities(SComponentEntLocal component, int qty) {
        this.component = component;
        this.quantity  = qty;
    }
}

