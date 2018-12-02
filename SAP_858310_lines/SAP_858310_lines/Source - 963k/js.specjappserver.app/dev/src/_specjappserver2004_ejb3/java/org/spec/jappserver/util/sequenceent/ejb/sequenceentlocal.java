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
 *  2003/05/23  John Stecher, IBM       Removed EJBExceptions
 */

package org.spec.jappserver.util.sequenceent.ejb;


import javax.ejb.EJBLocalObject;

import org.spec.jappserver.util.helper.SequenceBlock;


/**
 * This is the public interface of the Sequence entity bean. It provides
 * access to the various fields of an item.
 */
public interface SequenceEntLocal extends EJBLocalObject {

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws 
     *
     */
    public String getId();

    /**
     * Method getNextNumber
     *
     *
     * @return
     *
     * @throws 
     *
     */
    public int getNextNumber();

    /**
     * Method getBlockSize
     *
     *
     * @return
     *
     * @throws 
     *
     */
    public int getBlockSize();

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize
     *
     * @throws 
     *
     */
    public void setBlockSize(int blockSize);

    /**
     * Method nextSequenceBlock
     *
     *
     * @return
     *
     * @throws 
     *
     */
    public SequenceBlock nextSequenceBlock();
}

