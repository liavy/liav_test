/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2001        Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: InterruptNotifyable.java,v 1.2 2004/02/17 17:16:03 skounev Exp $
 */

package org.spec.jappserver.launcher;

/**
 * InterruptNotifyable provides an interface to notify
 * the cause of a thread interruption.
 * @author Akara Sucharitakul
 */
public interface InterruptNotifyable {

    /**
     * MATCH signifies that output or error
     * stream has been matched successfully
     * with given string. It's value is 101.
     */ 
    public static final int MATCH = 101;

    /**
     * Tells the target opject the reason of the interrupt.
     */
    public void notifyInterrupt(int value);
}
