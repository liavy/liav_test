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
 * WorkOrderFinish is the final state or stage of a process.
 * Called from WorkOrderCmpEJB.java in method update/finish.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderFinished
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderFinished extends WorkOrderState {

    /**
     * Indication that a remove is possible from this state.
     */
    public void remove() {
    }

    /**
     * @return the status of this state which is STAGE3
     */
    public int getStatus() {
        return COMPLETED;
    }
}

