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

import java.io.Serializable;


/**
 * This class is the DeliveryInfo Object passed to the deliveredPO
 * method of the Receiver bean.
 *
 * @author Damian Guy
 */
public class DeliveryInfo implements Serializable {

    public int    poId;
    public int    lineNumber;
    public String partID;
    public int    qty;

    /**
     * Constructor DeliveryInfo
     *
     *
     * @param poId
     * @param lineNumber
     * @param partID
     * @param qty
     *
     */
    public DeliveryInfo(int poId, int lineNumber, String partID, int qty) {

        this.poId        = poId;
        this.lineNumber = lineNumber;
        this.partID     = partID;
        this.qty         = qty;
    }
}

