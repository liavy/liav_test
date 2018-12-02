/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Ajay Mittal, SUN        Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/06/19  John Stecher, IBM       Modified to contain method to return Assemblies
 *  2005/12/22  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.mfg;

import java.util.Vector;

import javax.ejb.Local;

/**
 * Remote interface for Enterprise Bean: WorkOrderSes
 */
@Local
public interface WorkOrderSesLocal
{
   public int scheduleWorkOrder (String assemblyId, int qty, java.util.Calendar dueDate);

   public int scheduleLargeWorkOrder (int salesId, int oLineId, String assemblyId,
         int qty, java.util.Calendar dueDate);

   public void updateWorkOrder(int wid);

   public boolean completeWorkOrder(int wid);

   public boolean cancelWorkOrder(int wid);

   public int getWorkOrderCompletedQty(int wid);

   public int getWorkOrderStatus(int wid);

   public Vector<String> getAssemblyIds();
}
