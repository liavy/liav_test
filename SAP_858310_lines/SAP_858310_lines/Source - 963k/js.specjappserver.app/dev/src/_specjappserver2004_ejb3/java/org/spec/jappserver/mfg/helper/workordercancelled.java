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


/*
 * WorkOrderCancelled marks the WorkOrder as cancelled. Only can be done
 * during WorkOrderCreated and WorkOrderStage1. Afterwards cancel is not
 * allowed. An IllegalStateException will be thrown in the other stages.
 * Called from WorkOrderCmpEJB.java in method update/finish.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderCancelled
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderCancelled extends WorkOrderState {

    /**
     * Method remove
     *
     *
     */
    public void remove() {
    }

    /**
     * Method getStatus
     *
     *
     * @return
     *
     */
    public int getStatus() {
        return CANCELLED;
    }
}

