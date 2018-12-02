/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.common;


import java.io.Serializable;

/**
 * This class defines the object ComponentDemand that is basically
 * composed of the componentId and qty. Used in LargeOrderSes*
 *
 * @author Agnes Jacob
 * @see LargeOrderSesEJB.java
 */
public class ComponentDemand implements Serializable {

    String componentId;
    int    qty;

    /**
     * Constructor ComponentDemand
     *
     *
     */
    public ComponentDemand() {
    }

    /**
     * Sets the componentDemand fields
     * @param componentId
     * @param qty
     */
    public ComponentDemand(String componentId, int qty) {
        this.componentId = componentId;
        this.qty         = qty;
    }

    /**
     * Method getComponentId
     *
     *
     * @return
     *
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Method getQty
     *
     *
     * @return
     *
     */
    public int getQty() {
        return qty;
    }
}

