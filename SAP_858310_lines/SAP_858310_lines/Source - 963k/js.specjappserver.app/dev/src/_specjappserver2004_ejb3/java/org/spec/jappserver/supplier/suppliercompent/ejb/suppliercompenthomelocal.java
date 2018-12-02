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

package org.spec.jappserver.supplier.suppliercompent.ejb;


//Import statements
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * This is the Home interface for the Supplier Component Entity Bean.
 *
 *
 * @author Damian Guy
 */
public interface SupplierCompEntHomeLocal extends EJBLocalHome {

    /**
     * create
     * @param suppCompID - part number.
     * @param suppCompSuppID - supplier id.
     * @param suppCompPrice - price of supplied qty (suppCompQty).
     * @param suppCompQty - quantity that is supplied.
     * @param suppCompDiscount - discount the applies.
     * @param suppCompDelDate - probably should be lead time.
     * @return SuppCompEntPK - primary Key for this object (suppCompID + suppCompSuppID).
     * @exception EJBException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public SupplierCompEntLocal create(
                                      String suppCompID, int suppCompSuppID, double suppCompPrice, int suppCompQty,
                                      double suppCompDiscount, int suppCompDelDate)
    throws CreateException;

    /**
     * findByPrimaryKey
     * @retrun SupplierCompEntLocal
     *
     * @param key
     *
     * @return
     *
     * @throws FinderException
     */
    public SupplierCompEntLocal findByPrimaryKey(SuppCompEntPK key)
    throws FinderException;

    /**
     * findAllBySupplier: find all components for supplier.
     * @param suppID - id of supplier.
     * @return Collection
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - if there are not any rows found.
     */
    public Collection findAllBySupplier(int suppID)
    throws FinderException;
}

