/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2002/03/22  ramesh, SUN Microsystem   Created
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA      Conversion from SPECjAppServer2001 to
 *                                        SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM         Modifed for SPECjAppServer2004
 *  2003/04/20  John Stecher, IBM         Changed newOrder to return an ArrayList
 *  2003/05/05  John Stecher, IBM         Made changes to catch noSuchObject exception on remove and display
 *                                        warning message.
 *  2003/05/06  John Stecher, IBM         Made changes to allow drive instead of application to determine
 *                                        if an order is to be deferred or added to inventory immediately
 *  2003/06/18  John Stecher, IBM         Updated to implement changes suggested for best practices
 *  2003/12/16  John Stecher, IBM         Added atomicity tests
 *  2004/01/25  John Stecher, IBM         Added code for cache consistancy test.
 *  2004/02/18  Samuel Kounev, Darmstadt  Moved method updateItemPrice to OrderAuditSes.
 *  2006/01/18  Bernhard Riedhofer, SAP   Modified for the EJB3 version of SPECjAppServer2004
 */
package org.spec.jappserver.orders;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.RemoveException;

import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.orders.helper.ItemsDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;

/**
 * This is the interface of the OrderSessionBean, which is a wrapper
 * for the Order and Orderline entity beans. The session bean also
 * implements the getCustStatus method to retrieve all orders
 * belonging to a particular customer
 */
@Local
public interface OrderSesLocal
{
   public List<Object> newOrder (int customerId, ShoppingCart sc,
         boolean deferred, boolean atomicityTest)
         throws RemoteException, CreateException, RemoveException,
         SPECjAppServerException;

   public boolean cancelOrder (int orderId);

   public Collection getOpenOrders (int userID);

   public HashMap<String, ItemsDataBean> getItemInfo (
         HashMap<String, ItemsDataBean> itemIdList);

   public int getOrderCount (int userID);
}
