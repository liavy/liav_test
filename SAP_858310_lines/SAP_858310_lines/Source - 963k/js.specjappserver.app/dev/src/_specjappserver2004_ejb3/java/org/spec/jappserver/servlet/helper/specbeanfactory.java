/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company                Description
 *  ----------  ------------------------   ----------------------------------------------
 *  2003/06/19  John Stecher, IBM          Created to making an abstraction around the naming
 *                                         lookups on the web tier of the application
 *  2003/06/19  John Stecher, IBM          Added Remote Interface Support
 *  2003/06/30  John Stecher, IBM          Added Local Interface Support
 *  2004/02/18  Samuel Kounev, Darmstadt   Added OrderAuditSes bean lookup
 */
package org.spec.jappserver.servlet.helper;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.Config;
import org.spec.jappserver.orders.ItemBrowserSes;
import org.spec.jappserver.orders.ItemBrowserSesLocal;

/**
 * @author jstecher
 * 
 */
public class SpecBeanFactory {
    private static boolean useLocal = SpecConfig.web_to_ejb_locals;

    private static InitialContext ic;
    private static Object customerFacade = lookup("CustomerSes");
    private static Object largeOrderFacade = lookup("LargeOrderSes");
    private static Object orderFacade = lookup("OrderSes");
    private static Object workOrderFacade = lookup("WorkOrderSes");
    
    private static Object lookup(String beanName)
    {
        try
        {
           if (ic == null)
           {
              ic = new InitialContext();
           }
            return ic.lookup("java:comp/env/" + beanName + (useLocal ? "Local" : ""));
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the CustomerSes.
     * @return CustomerSes
     */
    public static Object createCustomerSes() throws RemoteException, CreateException  {
       return customerFacade;
    }

    /**
     * Returns the ItemBrowserSes.
     * @return ItemBrowserSes
     */
    public static Object createItemBrowserSes() throws RemoteException, CreateException {
        // TODO: cache HomeInterface of EJB2.1 client view of EJB3 bean
        return lookup("ItemBrowserSes");
    }

    /**
     * Returns the OrderAuditSes.
     * @return OrderAuditSes
     */
    public static Object createOrderAuditSes() throws RemoteException, CreateException {
        // TODO
        return null;
    }

    /**
     * Returns the LargeOrderSes.
     * @return LargeOrderSes
     */
    public static Object createLargeOrderSes() throws RemoteException, CreateException {
        return largeOrderFacade;
    }

    /**
     * Returns the OrderSes.
     * @return OrderSes
     */
    public static Object createOrderSes() throws RemoteException, CreateException {
        return orderFacade;
    }

    /**
     * Returns the WorkOrderSes.
     * @return WorkOrderSes
     */
    public static Object createWorkOrderSes() throws RemoteException, CreateException {
        return workOrderFacade;
    }

    /**
     * Returns the CustomerSes.
     * @return CustomerSes
     */
    public static void destroyCustomerSes(Object cs) throws RemoteException, RemoveException {
        // with EJB3: nothing to do since according to Spec remove() does not change client state 
    }

    /**
     * Returns the ItemBrowserSes.
     * @return ItemBrowserSes
     */
    public static void destroyItemBrowserSes(Object ibs) throws RemoteException, RemoveException {
        if (useLocal)
        {
            ((ItemBrowserSesLocal)ibs).removeBean();
        }
        else
        {
           ((ItemBrowserSes)ibs).removeBean();
        }
    }

    /**
     * Destroys the OrderAuditSes.
     * @return
     */
    public static void destroyOrderAuditSes(Object oas) throws RemoteException, RemoveException {
        // with EJB3: nothing to do since according to Spec remove() does not change client state 
    }

    /**
     * Returns the LargeOrderSes.
     * @return LargeOrderSes
     */
    public static void destroyLargeOrderSes(Object los) throws RemoteException, RemoveException {
        // with EJB3: nothing to do since according to Spec remove() does not change client state 
    }

    /**
     * Returns the OrderSes.
     * @return OrderSes
     */
    public static void destroyOrderSes(Object os) throws RemoteException, RemoveException {
        // with EJB3: nothing to do since according to Spec remove() does not change client state 
    }

    /**
     * Returns the WorkOrderSes.
     * @return WorkOrderSes
     */
    public static void destroyWorkOrderSes(Object wos) throws RemoteException, RemoveException {
        // with EJB3: nothing to do since according to Spec remove() does not change client state 
    }
}
