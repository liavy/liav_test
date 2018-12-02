/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ----------------------------------------------
 *  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/12/02  Samuel Kounev, Darmstadt  Added second addInventory() method for completed LOs
 *  2003/05/05  John Stecher, IBM         Made changes to ensure that users are updated when car is sold out from
 *                                        under them with a warning message.
 *  2003/06/17  John Stecher, IBM         Updated to make better use of BigDecimal
 *  2003/06/28  John Stecher, IBM         Removed unnecessary imports
 *  2003/08/30  John Stecher, IBM         Updated for new sell functionality
 *  2003/12/15  John Stecher, IBM         Changed for Atomicity Test
 *  2003/01/08  John Stecher, IBM         Changed code to eliminate unused objects being passed into methods
 *  2005/12/22  Bernhard Riedhofer, SAP   Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.corp;

import java.math.BigDecimal;
import java.util.Collection;

import javax.ejb.Local;

import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.corp.helper.CustomerDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;

/**
 * Remote interface for Enterprise Bean: CustomerSes
 */
@Local
public interface CustomerSesLocal
{
   public boolean validateCustomer (int userID);

   public CustomerDataBean getCustomerInfo (int userID);

   public Collection getInventory (int userID);

   public boolean sellInventory (int userID, int inventoryID,
         boolean isAtomicityTest);

   public void addInventory (int custID, ShoppingCart sc, BigDecimal orderCost,
         boolean atomicityTest)
         throws SPECjAppServerException;

   public void addInventory (int custId, String itemId, int quantity,
         BigDecimal totalValue);

   public boolean checkCustomerCredit (int custID, BigDecimal orderCost);
}
