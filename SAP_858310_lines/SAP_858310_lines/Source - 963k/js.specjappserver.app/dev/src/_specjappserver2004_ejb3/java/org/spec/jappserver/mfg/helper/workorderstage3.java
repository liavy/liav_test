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
 * WorkOrderStage3 is the fourth state or stage of a process.
 * Called from WorkOrderCmpEJB.java in method update.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderStage3
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderStage3 extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderFinished
     */
    public WorkOrderState nextState() {
        return(getInstance(COMPLETED));
    }

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderFinished
     */
    public WorkOrderState finish() {
        return(this.nextState());
    }

    /**
     * @return the status of this state which is STAGE3
     */
    public int getStatus() {
        return STAGE3;
    }
}

