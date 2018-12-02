/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Agnes Jacob, SUN        Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.mfg.helper;


import java.io.Serializable;


/**
 * Class LargeOrderInfo
 *
 *
 * @author
 * @version %I%, %G%
 */
public class LargeOrderInfo implements Serializable {

    public Integer       id;
    public int           salesOrderId;
    public int           orderLineNumber;
    public String        assemblyId;
    public int           qty;
    public java.sql.Date dueDate;

    /**
     * Method duplicate
     *
     *
     * @return
     *
     */
    public LargeOrderInfo duplicate() {

        LargeOrderInfo loi = new LargeOrderInfo();

        loi.id              = this.id;
        loi.salesOrderId    = this.salesOrderId;
        loi.orderLineNumber = this.orderLineNumber;
        loi.assemblyId      = this.assemblyId;
        loi.qty             = this.qty;
        loi.dueDate         = this.dueDate;

        return loi;
    }
}

