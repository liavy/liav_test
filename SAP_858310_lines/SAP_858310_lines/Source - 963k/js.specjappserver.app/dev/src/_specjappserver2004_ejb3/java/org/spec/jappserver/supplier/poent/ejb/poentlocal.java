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

package org.spec.jappserver.supplier.poent.ejb;


import javax.ejb.EJBLocalObject;

import org.spec.jappserver.common.SPECjAppServerException;


/**
 * This is the remote interface for the Purchase Order
 * Entity bean.
 *
 * @author Damian Guy
 */
public interface POEntLocal extends EJBLocalObject {

    /**
     * poLineDeliverd - indicate that a POline has been delivered.
     * @param lineNumber - line number of delivered line.
     */
    public void poLineDelivered(int lineNumber);

    /**
     * generateXml - generates the  XML for this Purchase Order.
     * @return String - containing XML.
     */
    public String generateXml() throws SPECjAppServerException;
}

