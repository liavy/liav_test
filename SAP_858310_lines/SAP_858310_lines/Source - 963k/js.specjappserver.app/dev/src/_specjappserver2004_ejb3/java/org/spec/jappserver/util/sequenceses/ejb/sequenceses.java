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

package org.spec.jappserver.util.sequenceses.ejb;


import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;


/**
 * This is the interface of the SequenceSessionBean, which is
 * a key cache for the Sequence entity bean.
 */
public interface SequenceSes extends EJBObject {

    /**
     * newSequence: create a new sequence. This method is not called
     * by the benchmark.
     * @param id   - Id of sequence
     * @param firstKey  - First valid key
     * @param blockSize  - Block size of keys to be cached
     * @exception RemoteException - system or network error occurred
     * @exception CreateException - creation of the new sequence fails
     */
    public void newSequence(String id, int firstKey, int blockSize)
    throws RemoteException, CreateException;

    /**
     * nextKey: provides the next unique key from a sequence id.
     * keys are not guaranteed to be issued in order and without gaps.
     * The only guarantee is that the key is unique in this sequence id.
     * @param id  - Id of the sequence
     * @return  - an available integer key
     * @exception RemoteException - system or network error occurred
     * @exception FinderException - the sequence id is invalid
     */
    public int nextKey(String id) throws RemoteException, FinderException;

    /**
     * removeSequence: removes a sequence. This method is not called
     * by the benchmark.
     * @param id  - Id of the sequence to be removed
     * @exception RemoteException - system or network error occurred
     * @exception FinderException - the sequence id is invalid
     * @exception RemoveException - remove error
     */
    public void removeSequence(String id)
    throws RemoteException, FinderException, RemoveException;
}

