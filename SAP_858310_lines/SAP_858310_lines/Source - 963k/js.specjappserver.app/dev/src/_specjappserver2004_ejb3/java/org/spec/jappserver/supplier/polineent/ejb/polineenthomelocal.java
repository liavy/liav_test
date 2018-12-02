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

package org.spec.jappserver.supplier.polineent.ejb;


//Import statements
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * This is the home interface for the POLine Entity bean.
 *
 * @author Damian Guy
 */
public interface POLineEntHomeLocal extends EJBLocalHome {

    /**
     * create - create a POLine
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     * @return POLineEntLocal
     * @exception EJBException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public POLineEntLocal create(
                                int poLineNumber, Integer poID, String pID, int qty, double balance,
                                int leadTime, String message)
    throws CreateException;

    /**
     * findByPrimaryKey - find the POLIne that matches key.
     * @param key - Key of POLineEntLocal to find.
     * @return POLineEntLocal
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - if there is a find exception.
     */
    public POLineEntLocal findByPrimaryKey(POLineEntPK key)
    throws FinderException;

    /**
     * findByPO - find all of the PO lines for a
     * given Purchase Order.
     * @param poID - id of the purchase order
     * @return Collection
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - if there are not any order lines for poID
     */
    public Collection findByPO(Integer poID)
    throws FinderException;
}

