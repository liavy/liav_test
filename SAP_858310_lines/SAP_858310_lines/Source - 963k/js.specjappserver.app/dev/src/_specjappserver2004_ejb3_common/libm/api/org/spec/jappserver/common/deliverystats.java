/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ---------------------------------------------
 *  2003/05/29  Samuel Kounev, Darmstadt  Created.
 */

package org.spec.jappserver.common;

import java.io.Serializable;


public class DeliveryStats implements Serializable {

    int depletedCompCnt;
    double totalQtyOrdered;
    int pendingLOsCnt;

    public DeliveryStats() {
    }

    public DeliveryStats(int depletedCompCnt, double totalQtyOrdered, int pendingLOsCnt) {
        this.depletedCompCnt = depletedCompCnt;
        this.totalQtyOrdered = totalQtyOrdered;
        this.pendingLOsCnt = pendingLOsCnt;
    }

    public int getDepletedCompCnt() {
        return depletedCompCnt;
    }

    public double getTotalQtyOrdered() {
        return totalQtyOrdered;
    }

    public int getPendingLOsCnt() {
        return pendingLOsCnt;
    }
}
