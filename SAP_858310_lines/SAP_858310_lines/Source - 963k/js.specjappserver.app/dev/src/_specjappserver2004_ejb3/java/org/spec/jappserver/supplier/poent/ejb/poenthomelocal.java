/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN           Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2004/02/21  Samuel Kounev, Darmstadt  Removed throws EJBException clauses 
 *  2004/03/16  Samuel Kounev, Darmstadt  Cleared unused import statements. 
 */

package org.spec.jappserver.supplier.poent.ejb;


//Import statements
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

import org.spec.jappserver.supplier.helper.ComponentOrder;


/**
 * This is the Home interface for the Purchase Order Entity Bean.
 *
 * @author Damian Guy
 */
public interface POEntHomeLocal extends EJBLocalHome {

    /**
     * create: create a new Purchase Order.
     * @param suppID - id of supplier.
     * @param siteID - site id of Mfg.
     * @param orders - Array of Objects containing qty + pricing information for components.
     * @return POEntLocal
     * @exception CreateException - if there is a create failure.
     */
    public POEntLocal create(int suppID, int siteID, ComponentOrder[] orders)
    throws CreateException;

    /**
     * findByPrimaryKey: find the PO that is identified by pk.
     * @param pk - find PO with the primary key.
     * @return POEntLocal.
     * @exception FinderException - if cannot find PO.
     */
    public POEntLocal findByPrimaryKey(Integer pk)
    throws FinderException;
}

