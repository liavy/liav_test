/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 *  2003/05/23  John Stecher, IBM       Removed EJBExceptions
 */

package org.spec.jappserver.supplier.supplierent.ejb;


//Import statements
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.FinderException;

import org.spec.jappserver.supplier.helper.PartSpec;


/**
 * Remote interface for the supplier entity bean.
 *
 * @author Damian Guy
 */
public interface SupplierEntLocal extends EJBLocalObject {

    /**
     * getID - get the suppliers ID
     * @return int - id of supplier.
     * @exception EJBException.
     */
    public int getID();

    /**
     * getPartSpec - return information about a part that
     * supplier supplies.
     * @param pID - id of part to get Spec for
     * @return PartSpec
     * @exception EJBException
     * @exception FinderException
     */
    public PartSpec getPartSpec(String pID) throws FinderException;
}

