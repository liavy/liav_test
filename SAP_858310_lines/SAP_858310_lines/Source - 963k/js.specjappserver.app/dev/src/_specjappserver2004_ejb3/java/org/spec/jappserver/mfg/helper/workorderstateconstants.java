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


/**
 * Interface WorkOrderStateConstants
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface WorkOrderStateConstants {

    public static final String[] woStates  = {
        "Open", "Stage1", "Stage2", "Stage3", "Completed", "Archived",
        "Cancelled"
    };
    public static final int      OPEN      = 0;
    public static final int      STAGE1    = 1;
    public static final int      STAGE2    = 2;
    public static final int      STAGE3    = 3;
    public static final int      COMPLETED = 4;
    public static final int      ARCHIVED  = 5;
    public static final int      CANCELLED = 6;
}

