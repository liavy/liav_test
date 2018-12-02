/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/04/04  Samuel Kounev,          Changed deliverPO method signature to receive
 *              Darmstadt Univ.         Vector of DeliveryInfo objects. Removed the
 *                                      throws SPECjAppServerException clause.
 *  2004/02/12  Samuel Kounev,          Rolled back previous change, since it might 
 *              Darmstadt Univ.         cause deadlocks when updating InventoryEnt and   
 *                                      SComponentEnt (osgjava-6527).
 */ 

package org.spec.jappserver.supplier.receiverses.ejb;


//Import statements
import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.spec.jappserver.common.DeliveryInfo;
import org.spec.jappserver.common.SPECjAppServerException;

/**
 * Remote interface for the the stateless session bean
 * ReceiverSes.
 *
 * @author Damian Guy
 */
public interface ReceiverSes extends EJBObject {

    /**
     * deliverPO - deliver a POLine.
     * @param delInfo - contains information about the POLine that is delivered.
     * @exception RemoteException - if there is a system failure.
     */
    public void deliverPO(DeliveryInfo delInfo) throws SPECjAppServerException, RemoteException;
}

