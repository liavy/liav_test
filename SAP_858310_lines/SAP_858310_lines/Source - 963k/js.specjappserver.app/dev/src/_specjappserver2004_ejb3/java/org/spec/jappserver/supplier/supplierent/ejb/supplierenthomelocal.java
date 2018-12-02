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

package org.spec.jappserver.supplier.supplierent.ejb;


//Import statements
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * Home interface for the Supplier Entity bean.
 *
 * @author Damian Guy
 */
public interface SupplierEntHomeLocal extends EJBLocalHome {

    /**
     * create: create a new supplier.
     * @param suppID - id of supplier.
     * @param suppName - supplier name.
     * @param suppStreet1 - street line 1.
     * @param suppStreet2 - street line 2.
     * @param suppCity - city supplier is located.
     * @param suppState
     * @param suppCountry - country supplier is located.
     * @param suppZip - zip/postal code.
     * @param suppPhone - contact phone number.
     * @param suppContact - contact person.
     * @return SupplierEntLocal - newly created Supplier
     * @exception CreateException - if the create fails.
     */
    public SupplierEntLocal create(
                                  int suppID, String suppName, String suppStreet1,
                                  String suppStreet2, String suppCity, String suppState,
                                  String suppCountry, String suppZip, String suppPhone,
                                  String suppContact)
    throws CreateException;

    /**
     * findByPrimaryKey: find the supplier whose id = pk.
     * @param pk - id of supplier.
     * @return SUpplierEntLocal.
     * @exception FinderException - if cannot find object for pk.
     */
    public SupplierEntLocal findByPrimaryKey(Integer pk)
    throws FinderException;

    /**
     * findAll: find all suppliers.
     * @return Collection - of Suppliers.
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - if there are not any suppliers.
     */
    public Collection findAll() throws FinderException;
}

