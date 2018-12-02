/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
 */


package org.spec.jappserver.servlet.helper;

import java.util.Collection;

public class VehicleSearch implements java.io.Serializable {
    public Collection vehicles;
    public int min;
    public int max;
    public int total;
}
