/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
 *  2003/06/28  John Stecher, IBM       Removed unnecessary imports
 *  2003/11/25  Tom Daly, Sun           Add category to the getItems() method declaration
 *  2006/01/18  Bernhard Riedhofer, SAP Modified for the EJB3 version of SPECjAppServer2004
 */

package org.spec.jappserver.orders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Remote;
import javax.ejb.Remove;

/**
 * Remote interface for Enterprise Bean: ItemBrowserSes
 */
@Remote
public interface ItemBrowserSes extends Serializable
{
    // This method returns to the user a collection of 10 items
    // from the query they specified (All or specific vehicles).
    public Collection getItems (ArrayList<String> queryItems, int category);

    // Returns the next 10 items in the query of items the user executed
    public Collection browseForward();

    // Returns the total number of items that the user has in their query
    public int getTotalItems();

    // Returns the previous 10 items in the query of items the user executed
    public Collection browseReverse();

    // Returns the current min item the user is browsing (ie 11 if the user is browsing 11-20)
    public int getCurrentMin();

    // Returns the current max item the user is browsing (ie 20 if the user is browsing 11-20)
    public int getCurrentMax();

    // removes the stateful session bean
    @Remove
    public void removeBean();
}
