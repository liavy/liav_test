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

package org.spec.jappserver.supplier.scomponentent.ejb;


import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * This is the home interface for the Component Entity Bean
 *
 *
 * @author Damian Guy
 */
public interface SComponentEntHomeLocal extends EJBLocalHome {

    /**
     * create: Create new Component.
     * @param id - Id of component.
     * @param name - component name.
     * @param description - description of the component.
     * @param unit - the unit of measure for this component.
     * @param cost - price per component.
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     * @return SComponentEntLocal
     * @exception EJBException - if there is a system failure.
     * @exception CreateException - if the create fails.
     */
    public SComponentEntLocal create(
                                    String id, String name, String description, String unit, double cost,
                                    int qtyOnOrder, int qtyDemanded, int leadTime,
                                    int containerSize) throws CreateException;

    /**
     * findByPrimaryKey: find the component that matches pk.
     * @param  pk - object that represents the pk.
     * @return SComponentEntLocal.
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - if cannot find object for pk.
     */
    public SComponentEntLocal findByPrimaryKey(String pk)
    throws FinderException;

    /**
     * findAll: retrieves all components.
     * @return Collection - all components.
     * @exception EJBException - if there is a system failure.
     * @exception FinderException - No components exist in database.
     */
    public Collection findAll() throws FinderException;
}

