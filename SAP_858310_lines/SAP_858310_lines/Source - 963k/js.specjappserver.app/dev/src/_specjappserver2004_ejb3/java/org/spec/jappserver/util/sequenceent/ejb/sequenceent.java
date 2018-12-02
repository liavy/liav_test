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

import javax.ejb.EJBObject;

import org.spec.jappserver.util.helper.SequenceBlock;


/**
 * This is the public interface of the Sequence entity bean. It provides
 * access to the various fields of an item.
 */
public interface SequenceEnt extends EJBObject {

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getId() throws RemoteException;

    /**
     * Method getNextNumber
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getNextNumber() throws RemoteException;

    /**
     * Method getBlockSize
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getBlockSize() throws RemoteException;

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize
     *
     * @throws RemoteException
     *
     */
    public void setBlockSize(int blockSize) throws RemoteException;

    /**
     * Method nextSequenceBlock
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public SequenceBlock nextSequenceBlock() throws RemoteException;
}

