/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Akara Sucharitakul, SUN Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.util.sequenceent.ejb;


import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;


/**
 * This is the home interface of the Sequence entity bean in the Customer
 * domain.
 */
public interface SequenceEntHome extends EJBHome {

    SequenceEnt create(String id, int firstNumber, int blockSize)
    throws RemoteException, CreateException;

    SequenceEnt findByPrimaryKey(String id)
    throws RemoteException, FinderException;

    java.util.Collection findAll() throws RemoteException, FinderException;
}

