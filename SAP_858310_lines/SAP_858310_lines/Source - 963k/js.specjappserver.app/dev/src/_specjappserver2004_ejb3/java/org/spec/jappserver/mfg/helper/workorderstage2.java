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
 * WorkOrderStage2 is the third state or stage of a process.
 * Called from WorkOrderCmpEJB.java in update() function
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderStage2
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderStage2 extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderStage3
     */
    public WorkOrderState nextState() {
        return(getInstance(STAGE3));
    }

    /**
     * @return the status of this state which is STAGE2
     */
    public int getStatus() {
        return STAGE2;
    }
}

