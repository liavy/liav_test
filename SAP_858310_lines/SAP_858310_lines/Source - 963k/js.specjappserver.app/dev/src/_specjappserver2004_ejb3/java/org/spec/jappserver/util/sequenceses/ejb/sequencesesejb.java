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


import java.util.HashMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;
import org.spec.jappserver.util.helper.SequenceBlock;
import org.spec.jappserver.util.sequenceent.ejb.SequenceEntHomeLocal;
import org.spec.jappserver.util.sequenceent.ejb.SequenceEntLocal;


/**
 * The SequenceSessionBean is a wrapper for the Sequence and Sequenceline entity beans.
 * The session bean is what is accessed by the SequenceEntry application. This
 * bean also implements the getCustStatus method to retrieve all common
 * belonging to a particular customer.
 */
public class SequenceSesEJB implements SessionBean {

    private SessionContext  sessionContext;
    private SequenceEntHomeLocal sequenceHome;
    private HashMap         sequences;
    protected Debug         debug;
    protected boolean       debugging;

    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {
        if( debugging )
            debug.println(3, "ejbCreate ");
    }

    /**
     * newSequence: create a new sequence. This method is not called
     * by the benchmark.
     * @param id   - Id of sequence
     * @param firstKey  - First valid key
     * @param blockSize  - Block size of keys to be cached
     * @exception CreateException - creation of the new sequence fails
     */
    public void newSequence(String id, int firstKey, int blockSize)
    throws CreateException {

        if( debugging )
            debug.println(3, "newSequence ");

        try {
            sequenceHome.create(id, firstKey, blockSize);
        } catch( EJBException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * nextKey: provides the next unique key from a sequence id.
     * keys are not guaranteed to be issued in order and without gaps.
     * The only guarantee is that the key is unique in this sequence id.
     * @param id  - Id of the sequence
     * @return  - an available integer key
     * @exception FinderException - the sequence id is invalid
     */
    public int nextKey(String id) throws FinderException {

        if( debugging )
            debug.println(3, "Getting next key for " + id);

        SequenceEntLocal   sequence = null;
        SequenceBlock block    = (SequenceBlock) sequences.get(id);

        // If we do not have a reference at all, we do a find.
        if( block == null ) {
            try {
                if( debugging )
                    debug.println(4, "Finding SequenceEntLocal");

                sequence = sequenceHome.findByPrimaryKey(id);
            } catch( EJBException e ) {
                debug.printStackTrace(e);

                throw new EJBException(e);
            }

            // Otherwise we might have run out of our block.
            // Here we use our saved reference instead of finding.
        } else if( block.nextNumber >= block.ceiling ) {
            sequence = block.sequence;
        }

        // If sequence is set, it means we have to get a new block
        if( sequence != null ) {

            int retries = 10;

            /* In optimistic concurrency controlled servers
             * with CMP, there's alway a chance the tx will
             * get rolled back which will show up as a
             * EJBException. In our BMP implementation
             * of SequenceEntLocal, we enforce pessimistic
             * concurrency control. So it should not be
             * retrying at all.
             */

            for( int i = 0; i < retries; i++ ) {
                try {
                    if( debugging )
                        debug.println(4, "Fetching nextSequenceBlock");

                    block = sequence.nextSequenceBlock();

                    break;
                } catch( EJBException e ) {
                    if( i == retries - 1 ) {
                        debug.printStackTrace(e);
                        throw new EJBException(e);
                    } else {
                        if( debugging )
                            debug.println(4, "Retrying nextSequenceBlock..."
                                          + i);
                    }
                }
            }

            // Save the reference and the block itself
            block.sequence = sequence;

            sequences.put(id, block);
        }

        if( debugging )
            debug.println(5, toString() + " next " + id + " key: "
                          + block.nextNumber);

        return block.nextNumber++;
    }

    /**
     * removeSequence: removes a sequence. This method is not called
     * by the benchmark.
     * @param id  - Id of the sequence to be removed
     * @exception FinderException - the sequence id is invalid
     * @exception RemoveException - remove error
     */
    public void removeSequence(String id)
    throws FinderException, RemoveException {

        try {
            sequenceHome.findByPrimaryKey(id).remove();
        } catch( EJBException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
    }

    /**
     * Method setSessionContext
     *
     *
     * @param sessionContext
     *
     */
    public void setSessionContext(SessionContext sessionContext) {

        this.sessionContext = sessionContext;

        InitialContext initCtx = null;

        try {
            initCtx = new InitialContext();
        } catch( NamingException ne ) {
            ne.printStackTrace();

            throw new EJBException(ne);
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
        } catch( NamingException e ) {

            debug = new Debug();
        }

        try {

            // the homes are available via EJB links
            sequenceHome =
            (SequenceEntHomeLocal)
            initCtx.lookup("java:comp/env/ejb/SequenceEntLocal");
        } catch( NamingException e ) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        sequences = new HashMap();
    }
}

