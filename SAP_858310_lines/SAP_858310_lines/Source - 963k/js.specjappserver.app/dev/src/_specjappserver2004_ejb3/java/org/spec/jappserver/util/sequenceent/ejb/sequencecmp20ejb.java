/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          --------------------------------------------------
 *  2002/03/22  Akara Sucharitakul, SUN   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modified for SPECjAppServer2004
 *  2003/07/28  Samuel Kounev, Darmstadt  Corrected faulty error messages in setEntityContext.
 *
 */

package org.spec.jappserver.util.sequenceent.ejb;


import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.util.helper.SequenceBlock;


/**
 * SequenceCmp20EJB controls the sequence block requests.
 *
 * @author Akara Sucharitakul
 * @version %I%, %G%
 */
public abstract class SequenceCmp20EJB implements EntityBean {

    protected EntityContext  entCtx;
    protected InitialContext initCtx;
    protected Debug          debug;
    protected boolean        debugging;

    /**
     * Method ejbCreate
     *
     * @param id          Sequence identifier
     * @param firstNumber Starting number
     * @param blockSize   Sequence cache block size
     *
     * @return The sequence identifier
     *
     * @throws CreateException Create error
     */
    public String ejbCreate(String id, int firstNumber, int blockSize)
    throws CreateException {

        if( debugging )
            debug.println(3, "ejbCreate ");

        setId(id);
        setNextNumber(firstNumber);
        setBlockSize(blockSize);

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id          Sequence identifier
     * @param firstNumber First number
     * @param blockSize   Sequence cache block size
     *
     */
    public void ejbPostCreate(String id, int firstNumber, int blockSize) {
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException Remove error
     *
     */
    public void ejbRemove() throws RemoveException {
        if( debugging )
            debug.println(3, "ejbRemove");
    }

    /**
     * Method getId
     *
     *
     * @return The sequence identifier
     *
     */
    public abstract String getId();
    /**
     * Method setId
     *
     *
     * @param The sequence identifier
     *
     */
    public abstract void setId(String id);
    /**
     * Method getNextNumber gets the next number
     * without advancing the block.
     *
     * @return The next sequence number.
     *
     */
    public abstract int getNextNumber();

    /**
     * Method setNextNumber gets the next number
     * without advancing the block.
     *
     * @param The next sequence number.
     *
     */
    public abstract void setNextNumber(int nextNumber);
    /**
     * Method getBlockSize
     *
     *
     * @return Sequence cache block size.
     *
     */
    public abstract int getBlockSize();
    /**
     * Method setBlockSize
     *
     *
     * @param Sequence cache block size.
     *
     */
    public abstract void setBlockSize(int blockSize);
    /**
     * Method nextSequenceBlock obtains the next sequence block
     * and advances the sequence number in the database.
     *
     * @return The next sequence block.
     *
     */
    public SequenceBlock nextSequenceBlock() {

        if( debugging )
            debug.println(3, "nextSequenceBlock");

        SequenceBlock block = new SequenceBlock();

        block.nextNumber = getNextNumber();
        setNextNumber(getNextNumber() + getBlockSize());
        block.ceiling = getNextNumber();

        return block;
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if( debugging )
            debug.println(3, "ejbActivate ");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if( debugging )
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if( debugging )
            debug.println(3, "ejbLoad ");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if( debugging )
            debug.println(3, "ejbStore ");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entCtx The context of this entity bean.
     *
     */
    public void setEntityContext(EntityContext entCtx) {

        this.entCtx = entCtx;

        try {
            initCtx = new InitialContext();
        } catch( NamingException e ) {
            e.printStackTrace();

            throw new EJBException(e);
        }

        try {
            int debugLevel =
            ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
            .intValue();

            if( debugLevel > 0 ) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch( NamingException ne ) {

            debug = new Debug();
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        if( debugging )
            debug.println(3, "unsetEntityContext ");
    }

    /****
    public String ejbFindByPrimaryKey(String key)
        throws FinderException;
    ****/


}

