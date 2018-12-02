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


/*
 * This class is the abstract classes for the different states in the workOrder
 * process.  If the subclasses do not have some of the methods defined
 * in this class then an IllegalStateException is thrown indicating
 * that the desired state changed is not legal for that particular state.
 * @author Agnes Jacob
 */

/**
 * Class WorkOrderState
 *
 *
 * @author
 * @version %I%, %G%
 */
public abstract class WorkOrderState implements WorkOrderStateConstants, Serializable {

    public static final int NUMSTATES = 5;

    /**
     * Method getInstance
     *
     *
     * @param status
     *
     * @return
     *
     */
    public static WorkOrderState getInstance(int status) {

        switch( status ) {

        case OPEN :
            return(new WorkOrderCreated());

        case STAGE1 :
            return(new WorkOrderStage1());

        case STAGE2 :
            return(new WorkOrderStage2());

        case STAGE3 :
            return(new WorkOrderStage3());

        case COMPLETED :
            return(new WorkOrderFinished());

        case CANCELLED :
            return(new WorkOrderCancelled());

        default :
            throw new IllegalStateException("Unknown State " + status);
        }
    }

    /**
     * Method nextState
     *
     *
     * @return
     *
     */
    public WorkOrderState nextState() {
        throw new IllegalStateException();
    }

    /**
     * Method process
     *
     *
     * @return
     *
     */
    public WorkOrderState process() {
        throw new IllegalStateException();
    }

    /**
     * Method cancel
     *
     *
     * @return
     *
     */
    public WorkOrderState cancel() {
        throw new IllegalStateException();
    }

    /**
     * Method finish
     *
     *
     * @return
     *
     */
    public WorkOrderState finish() {
        throw new IllegalStateException();
    }

    /**
     * Method remove
     *
     *
     */
    public void remove() {
        throw new IllegalStateException();
    }

    /**
     * Method getStatus
     *
     *
     * @return
     *
     */
    public abstract int getStatus();
}

