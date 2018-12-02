/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/04/15	John Stecher, IBM         added debugging
 *  2003/04/20	John Stecher, IBM         Explicitly remove session beans after use
 *  2003/04/24	John Stecher, IBM         made changes to handle the correct usage of the
 *                                        orderSes session bean actually returning a object
 *                                        wrapper containing the orderDataBean and ItemsDataBean
 *  2003/05/01  John Stecher, IBM         Imported Russ Raymundo's mfg side of the benchmark for web base driving
 *  2003/05/05	John Stecher, IBM         Made changes to catch noSuchObject exception on remove and display
 *                                        warning message.
 *  2003/05/05	John Stecher, IBM         Made changes to ensure that users are updated when car is sold out from
 *                                        under them with a warning message.
 *  2003/05/06	John Stecher, IBM         Made changes to allow drive instead of application to determine
 *                                        if an order is to be deferred or added to inventory immediately
 *  2003/06/24  John Stecher, IBM         Made changes to conform with best practices guidelines
 *  2003/06/28  John Stecher, IBM         Updated BigDecimal usage for better performance
 *  2003/08/30  John Stecher, IBM         Updated for new sell functionality
 *  2003/11/25  Tom Daly    , Sun         added support for category in doVehicleQuotes()
 *  2003/12/05  John Stecher, IBM         Added Atomicity Tests
 *  2003/01/08  John Stecher, IBM         Changed code to eliminate unused objects being passed into methods
 *  2004/01/25  John Stecher, IBM         Added code for cache consistancy test.
 *  2004/02/27  Samuel Kounev, Darmstadt  Changed to call printStackTrace when catching Exceptions.
 *  2004/03/11  John Stecher, IBM         Updated to make cache test work correctly in a cluster
 */

package org.spec.jappserver.servlet.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.spec.jappserver.corp.helper.CustomerDataBean;
import org.spec.jappserver.orders.helper.InsufficientCreditException;
import org.spec.jappserver.orders.helper.ItemsDataBean;
import org.spec.jappserver.orders.helper.OrderDataBean;
import org.spec.jappserver.orders.helper.ShoppingCart;
import org.spec.jappserver.orders.helper.ShoppingCartDataBean;

/**
 * SpecServletAction provides servlet specific client side access to each of the Spec
 * vehicle dealership user operations. These include login, logout, buy, sell, browse, etc.
 * SpecServletAction manages a web interface to the SPECjAppServer application handling HttpRequests/HttpResponse objects
 * and forwarding results to the appropriate JSP page for the web interface.
 * SpecServletAction invokes {@link SpecAction} methods to actually perform
 * each dealership operation.
 *
 */
public class SpecServletAction {

    private SpecServices sAction = null;

    public SpecServletAction() {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.init() ");
        }

        // TODO replace SPecAction_Local and Spec Action by one class (@remote and @local inherit from one business interace)
        if (SpecConfig.web_to_ejb_locals)
        {
           sAction = new SpecAction_Local();
        }
        else
        {
           sAction = new SpecAction();
        }
    }

    /**
     * Display Dealership inventory information such as closed orders, open orders, and overall number
     * of vehicles currently in the inventory.  Dispatch to the Spec dealerinventory JSP for display
     *
     * @param userID The User to display profile info
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @param results A short description of the results/success of this web request provided on the web page
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doInventory(
                           ServletContext ctx,
                           HttpServletRequest req,
                           HttpServletResponse resp,
                           Integer userID)
    throws javax.servlet.ServletException, java.io.IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doInventory() ");
        }
        try {
            Collection  customerDataBeans = sAction.getHoldings(userID);
            Collection  orderDataBean     = sAction.getOpenOrders(userID);
            HashMap     itemDataBeans     = sAction.getItemDataBeans(customerDataBeans, orderDataBean);
            req.setAttribute("inventory", customerDataBeans);
            req.setAttribute("itemInfo", itemDataBeans);
            req.setAttribute("openOrders", orderDataBean);
            req.setAttribute("results", "");
            HttpSession session = req.getSession(true);
            Integer InventoryVisits = (Integer)session.getAttribute("inventoryPageVisits");
            session.setAttribute("inventoryPageVisits",new Integer(InventoryVisits.intValue()+1));
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.INVENTORY_PAGE));
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doInventory");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doInventory() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

/**
     * Cancel a non-completed order for the given dealership
     * Dispatch to the Spec dealerinventory JSP for display
     *
     * @param userID The dealership cancelling its order
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doCancelOrder(
                             ServletContext ctx,
                             HttpServletRequest req,
                             HttpServletResponse resp,
                             Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doCancelOrder() ");
        }
        try {
            int orderID = Integer.parseInt(req.getParameter("orderID"));
            boolean cancelled = sAction.cancel(orderID);
            HttpSession session = req.getSession(true);
            Integer cancels = (Integer)session.getAttribute("ordersCancelled");
            session.setAttribute("ordersCancelled",new Integer(cancels.intValue()+1));
            if(!cancelled){
            	req.setAttribute("results_cancel", "*** NOTICE: The order you tried to cancel has already been cancelled! ***");
            }
            this.doInventory(ctx, req, resp, userID);
        } catch( Exception e ) {
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doCancelOrder");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doCancelOrder() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }


    /**
     * Create the Spec Home page with personalized information such as the dealerships account balance
     * Dispatch to the SpecJHome JSP for display
     *
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @param results A short description of the results/success of this web request provided on the web page
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */


    public void doHome(
                      ServletContext ctx,
                      HttpServletRequest req,
                      HttpServletResponse resp,
                      Integer userID,
                      String results)
    throws javax.servlet.ServletException, java.io.IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doHome() ");
        }
        String result = "";
        try {
            HttpSession session = req.getSession();
            CustomerDataBean customerData = sAction.getCustomerData(userID);
            Collection  customerDataBeans = sAction.getHoldings(userID);
            req.setAttribute("customerData", customerData);
            req.setAttribute("customerDataBeans", customerDataBeans);
            req.setAttribute("results", results);
            session.setAttribute("homepageVisits", new Integer(((Integer)session.getAttribute("homepageVisits")).intValue()+1));
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.HOME_PAGE));
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doHome");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doHome() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }


/**
     * Login a Spec dealership User.
     * Dispatch to the SpecJHome JSP for display
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doLogin(
                       ServletContext ctx,
                       HttpServletRequest req,
                       HttpServletResponse resp,
                       Integer userID)
    throws javax.servlet.ServletException, java.io.IOException
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doLogin() ");
        }
        String results = "";
        try {
            boolean loggedIn = sAction.login(userID);
            if( loggedIn ) {
                HttpSession session = req.getSession(false);
                if( session != null ) {
                    session.invalidate();
                }
                session = req.getSession(true);
                session.setAttribute("uidBean", userID);
                session.setAttribute("sessionCreationDate", new java.util.Date());
                session.setAttribute("inventoryPageVisits", new Integer(0));
                session.setAttribute("shoppingCartPageVisits", new Integer(0));
                session.setAttribute("browsePageVisits", new Integer(0));
                session.setAttribute("homepageVisits", new Integer(0));
                session.setAttribute("ordersCancelled", new Integer(0));
                session.setAttribute("ordersPlaced", new Integer(0));
                session.setAttribute("holdingsSold", new Integer(0));
                session.setAttribute("totalSalesProfits", SpecUtils.zeroBigDec);
                session.setAttribute("totalPurchaseDebits", SpecUtils.zeroBigDec);
                results = "Ready to work";
                doHome(ctx, req, resp, userID, results);
                return;
            } else {
                req.setAttribute("results", results + "\nCould not find account for " + userID +".");
                requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.WELCOME_PAGE));
            }
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doLogin");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doInventory() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    /**
     * Logout a Spec dealership User
     * Dispatch to the Spec Welcome JSP for display
     *
     * @param userID The User to logout
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doLogout(
                        ServletContext ctx,
                        HttpServletRequest req,
                        HttpServletResponse resp,
                        Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doLogout() ");
        }
        String results = "";
        HttpSession session = req.getSession();
        try {
            sAction.logout(session);
        } catch( Exception e ) {
            //log the exception and foward to a error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doLogout");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doLogout() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
        if( session != null ) {
            session.invalidate();
        }
        req.setAttribute("results", "");
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.WELCOME_PAGE));
    }


    /**
     * Retrieve the current dealerships shopping cart of vehicles
     * Dispatch to the Spec shoppingcart JSP for display
     *
     * @param userID The User requesting to view their dealership shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doShoppingCart(
                              ServletContext ctx,
                              HttpServletRequest req,
                              HttpServletResponse resp,
                              Integer userID)
    throws ServletException, IOException {

        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doShoppingCart() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null ) {
            sc = new ShoppingCart();
        }
        session.setAttribute("shoppingcart", sc);
        req.setAttribute("shoppingcart", sc);
        session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
    }

    /**
     * Add an item to the current dealerships shopping cart of vehicles
     * Dispatch to the Spec shoppingcart JSP for display
     *
     * @param userID The User requesting to add to their dealership's shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doAddToShoppingCart(
                                   ServletContext ctx,
                                   HttpServletRequest req,
                                   HttpServletResponse resp,
                                   Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doAddToShoppingCart() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null ) {
            sc = new ShoppingCart();
        }
        ShoppingCartDataBean scdb = new ShoppingCartDataBean();
        scdb.setId(req.getParameter("itemId"));
        scdb.setQuantity(Integer.parseInt(req.getParameter("quantity")));
        scdb.setVehicle(req.getParameter("name"));
        scdb.setDescription(req.getParameter("description"));
        scdb.setDiscount(new BigDecimal((String)req.getParameter("discount")));
        scdb.setMSRP(new BigDecimal((String)req.getParameter("price")));
        sc.addItem(scdb);
        session.setAttribute("shoppingcart", sc);
        req.setAttribute("shoppingcart", sc);
        session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
    }

    /**
     * Remove an item from the current dealerships shopping cart of vehicles
     * Dispatch to the Spec shoppingcart JSP for display
     *
     * @param userID The User requesting to remove from their dealership's shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doRemoveFromShoppingCart(
                                        ServletContext ctx,
                                        HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doRemoveFromShoppingCart() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null ) {
            sc = new ShoppingCart();
        } else {
            int removeID = Integer.parseInt(req.getParameter("cartID"));
            sc.removeItem(removeID);
        }
        session.setAttribute("shoppingcart", sc);
        req.setAttribute("shoppingcart", sc);
        session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
    }


    /**
     * Clear all items from the current dealerships shopping cart of vehicles
     * Dispatch to the Spec shoppingcart JSP for display
     *
     * @param userID The User requesting to clear their dealership's shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doClearCart(
                           ServletContext ctx,
                           HttpServletRequest req,
                           HttpServletResponse resp,
                           Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doClearCart() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null ) {
            sc = new ShoppingCart();
        } else {
            sc.clearCart();
        }
        session.setAttribute("shoppingcart", sc);
        req.setAttribute("shoppingcart", sc);
        session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
    }

	/**
     * Purchase all items from the current dealerships shopping cart of vehicles
     * Dispatch to the Spec order JSP for display
     *
     * @param userID The User requesting to purchase their dealership's shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doDeferedPurchase(
                          ServletContext ctx,
                          HttpServletRequest req,
                          HttpServletResponse resp,
                          Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doDeferedPurchase() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null || sc.getItemCount() == 0 ) {
            if( sc == null ) sc = new ShoppingCart();
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doDeferedPurchase() warning shopping cart was null ");
            }
            session.setAttribute("shoppingcart", sc);
            req.setAttribute("shoppingcart", sc);
            session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
            return;
        }

        ArrayList objWrapper = null;
        try {
            session.setAttribute("ordersPlaced", new Integer(((Integer)session.getAttribute("ordersPlaced")).intValue()+1));
            objWrapper = sAction.doPurchase(userID, sc, true, false);
            req.setAttribute("order", (OrderDataBean)objWrapper.get(0));
            req.setAttribute("items", (HashMap)objWrapper.get(1));
            req.setAttribute("results", "has been added to your open orders.");
            sc.clearCart();
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ORDER_PAGE));
        } catch( InsufficientCreditException e ) {
            e.printStackTrace();
            req.setAttribute("error", "Insufficient Credit Exception");
            req.setAttribute("message", "An Insufficient Credit Exception has occured in doPurchase");
            req.setAttribute("status_code", "0");
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doDeferedPurchase() InsufficientCredit error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        } catch( Exception e ) {
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doDeferedPurchase");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doDeferedPurchase() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    /**
     * Purchase all items from the current dealerships shopping cart of vehicles
     * Dispatch to the Spec order JSP for display
     *
     * @param userID The User requesting to purchase their dealership's shopping cart
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doPurchase(
                          ServletContext ctx,
                          HttpServletRequest req,
                          HttpServletResponse resp,
                          Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doPurchase() ");
        }
        /* In here we need to just look into the session and display it in the cart */
        HttpSession session = req.getSession();
        ShoppingCart sc = (ShoppingCart) session.getAttribute("shoppingcart");
        if( sc == null || sc.getItemCount() == 0 ) {
            if( sc == null ) sc = new ShoppingCart();
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doPurchase() warning shopping cart was null ");
            }
            session.setAttribute("shoppingcart", sc);
            req.setAttribute("shoppingcart", sc);
            session.setAttribute("shoppingCartPageVisits", new Integer(((Integer)session.getAttribute("shoppingCartPageVisits")).intValue()+1));
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.SHOPPINGCART_PAGE));
            return;
        }

        ArrayList objWrapper = null;
        try {
            session.setAttribute("ordersPlaced", new Integer(((Integer)session.getAttribute("ordersPlaced")).intValue()+1));
            session.setAttribute("totalPurchaseDebits", ((BigDecimal)session.getAttribute("totalPurchaseDebits")).add(sc.getTotal()));
            objWrapper = sAction.doPurchase(userID, sc, false, false);
            req.setAttribute("order", (OrderDataBean)objWrapper.get(0));
            req.setAttribute("items", (HashMap)objWrapper.get(1));
            req.setAttribute("results", "has been submitted for manufacturing.");
            sc.clearCart();
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ORDER_PAGE));
        } catch( InsufficientCreditException e ) {
           e.printStackTrace();
            req.setAttribute("error", "Insufficient Credit Exception");
            req.setAttribute("message", "An Insufficient Credit Exception has occured in doPurchase");
            req.setAttribute("status_code", "0");
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doPurchase() InsufficientCredit error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        } catch( Exception e ) {
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doPurchase");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doPurchase() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    /**
     * Retrieve the current price quote for the given vehicle or return all
     * vehicles to the user
     * Dispatch to the Trade Quote JSP for display
     *
     * @param vehicles The vehicle ID used to get the price quote
     * @param userID The ID of the user for who we are performing the query
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doVehicleQuotes(
                               ServletContext ctx,
                               HttpServletRequest req,
                               HttpServletResponse resp,
                               Integer userID,
                               String vehicles,
                               int category)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doVehicleQuotes() ");
        }
        String results = "";
        java.util.ArrayList vehicleQuotes = null;
        if( vehicles != null ) {
            vehicleQuotes = new java.util.ArrayList();
            java.util.StringTokenizer st = new java.util.StringTokenizer(vehicles, " ,");
            while( st.hasMoreElements() ) {
                String vehicle = st.nextToken();
                vehicleQuotes.add(vehicle);
            }
        }
        try {
            HttpSession session = req.getSession();
            VehicleSearch itemInfo = sAction.getVehicleQuotes(vehicleQuotes, session, req.getParameter("browse"), category);
            session.setAttribute("browsePageVisits", new Integer(((Integer)session.getAttribute("browsePageVisits")).intValue()+1));
            req.setAttribute("itemInfo", itemInfo);
            req.setAttribute("results", "");
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.VEHICLEQUOTE_PAGE));
        } catch( Exception e ) {
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doVehicleQuotes");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doVehicleQuotes() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }


    /**
     * Sell a set of vehiclesfor the given dealership.
     * Dispatch to the Spec dealerinventory JSP for display
     *
     * @param userID The User selling the vehicles
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doSell(
                      ServletContext ctx,
                      HttpServletRequest req,
                      HttpServletResponse resp,
                      Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doSell() ");
        }
        String results = "";
        try {
            HttpSession session = req.getSession();
			Integer holdingID = new Integer(req.getParameter("vehicleToSell"));
            BigDecimal total = new BigDecimal((String)req.getParameter("total"));
			boolean sold = sAction.sell(userID, holdingID, false);
            if(sold){
            	session.setAttribute("holdingsSold", new Integer(((Integer)session.getAttribute("holdingsSold")).intValue()+1));
            	session.setAttribute("totalSalesProfits", ((BigDecimal)session.getAttribute("totalSalesProfits")).add(total));
            	req.setAttribute("results", "");
            }else{
            	req.setAttribute("results_sell", "*** NOTICE: The vehicles you tried to sell have already been sold! ***");
            }
            this.doInventory(ctx, req, resp, userID);
        } catch( Exception e ) {
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doSell");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            if( SpecConfig.debugging ) {
                SpecConfig.debug.println(3, "SpecServletAction.doSell() error " + e);
            }
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    public void doWelcome(
                         ServletContext ctx,
                         HttpServletRequest req,
                         HttpServletResponse resp,
                         String status)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doWelcome() ");
        }
        req.setAttribute("results", status);
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.WELCOME_PAGE));
    }


    private void requestDispatch(
                                ServletContext ctx,
                                HttpServletRequest req,
                                HttpServletResponse resp,
                                String page)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.requestDispatch() ");
        }
        ctx.getRequestDispatcher(page).forward(req, resp);
    }


    private void sendRedirect(HttpServletResponse resp, String page)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.sendRedirect() ");
        }
        resp.sendRedirect(resp.encodeRedirectURL(page));
    }

    /**
  * MFG Welcome Page.
  * Dispatch to the MFG Welcome Page
  *
  * @param userID The User to login
  * @param ctx the servlet context
  * @param req the HttpRequest object
  * @param resp the HttpResponse object
  * @exception javax.servlet.ServletException If a servlet specific exception is encountered
  * @exception javax.io.IOException If an exception occurs while writing results back to the user
  *
  */
    public void doMfgWelcome(
                            ServletContext ctx,
                            HttpServletRequest req,
                            HttpServletResponse resp,
                            String status)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgWelcome(" + status + ")");
        }
        req.setAttribute("results", status);
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WELCOME_PAGE));
    }


    /**
    * Login a Spec Mfg User.
    * Dispatch to the SpecJMfgHome JSP for display
    *
    * @param userID The User to login
    * @param ctx the servlet context
    * @param req the HttpRequest object
    * @param resp the HttpResponse object
    * @exception javax.servlet.ServletException If a servlet specific exception is encountered
    * @exception javax.io.IOException If an exception occurs while writing results back to the user
    *
    */

    public void doMfgLogin(
                          ServletContext ctx,
                          HttpServletRequest req,
                          HttpServletResponse resp,
                          Integer userID)
    throws javax.servlet.ServletException, java.io.IOException
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgLogin(" + userID + ")");
        }
        String results = "";
        try {
            boolean loggedIn = sAction.login(userID);
            if( loggedIn ) {
                HttpSession session = req.getSession(true);
                if( session != null ) {
                    session.invalidate();
                    session = req.getSession(true);
                }
                session.setAttribute("uidBean", userID);
                results = "Ready to work";
                doMfgHome(ctx, req, resp, userID, results);
                return;
            } else {
                req.setAttribute("results", results + "\nCould not find account for " + userID +".");
                requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WELCOME_PAGE));
            }
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doLogin");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    /**
     * Logout a Spec Mfg User
     * Dispatch to the Spec MFg Welcome JSP for display
     *
     * @param userID The User to logout
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */

    public void doMfgLogout(
                           ServletContext ctx,
                           HttpServletRequest req,
                           HttpServletResponse resp,
                           Integer userID)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgLogout(" + userID + ")");
        }
        String results = "";
        HttpSession session = req.getSession();
        try {
            sAction.logout(session);
        } catch( Exception e ) {
            //log the exception and foward to a error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doLogout");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
        if( session != null ) {
            session.invalidate();
        }
        // Recreate Session object before writing output to the response
        // Once the response headers are written back to the client the opportunity
        // to create a new session in this request may be lost
        session = req.getSession(true);
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WELCOME_PAGE));
    }
    /**
     * Create the Mfg Home page
     * Dispatch to the MfgHome JSP for display
     *
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @param results A short description of the results/success of this web request provided on the web page
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */


    public void doMfgHome(
                         ServletContext ctx,
                         HttpServletRequest req,
                         HttpServletResponse resp,
                         Integer userID,
                         String results)
    throws javax.servlet.ServletException, java.io.IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgHome(" + userID + ")");
        }
        String result = "";
        try {
            HttpSession session = req.getSession();
            req.setAttribute("results", results);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_HOME_PAGE));
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doHome");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
        }
    }

    /**
     * MFG Schedule WorkOrder Page.
     * Dispatch to the MFG Schedule WorkOrder Page
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doMfgScheduleWO(
                               ServletContext ctx,
                               HttpServletRequest req,
                               HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgScheduleWO()");
        }
        Vector assemblyIds;
        Integer woID = null;
        String results;
        int qty=0,oLineId=0,salesId=0;
        java.sql.Date dueDate=null;
        boolean errorFound = false;

        req.setAttribute("nextAction","mfgschedulewo");

        String str_assemblyId = req.getParameter("assemblyId");
        String str_qty = req.getParameter("qty");
        String str_oLineId = req.getParameter("oLineId");
        String str_salesId = req.getParameter("salesId");
        String str_dueDate = req.getParameter("dueDate");

        if( str_assemblyId == null || str_qty == null ) {
            try {
                assemblyIds = sAction.getAssemblyIds();
                req.setAttribute("results","");
                req.setAttribute("assemblyIds", assemblyIds);
                requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_SCHEDULEWO_PAGE));
                return;

            } catch( Exception e ) {
                //log the exception with error page
                req.setAttribute("error", "internal error");
                req.setAttribute("message", "An internal error has occured in doMfgScheduleWO");
                req.setAttribute("status_code", "1");
                req.setAttribute("exception", e);
                e.printStackTrace(System.err);
                requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
                return;
            }
        } else {

            try {
                qty = Integer.parseInt(str_qty);
                if( str_dueDate != null ) dueDate = java.sql.Date.valueOf(str_dueDate);
                if( str_salesId != null ) salesId = Integer.parseInt(str_salesId);
                if( str_oLineId != null ) oLineId = Integer.parseInt(str_oLineId);

            } catch( NumberFormatException e ) {
                req.setAttribute("results","Invalid Quantity: "+str_qty+".");
                e.printStackTrace(System.err);
                errorFound = true;
            } catch( Exception e ) {
                req.setAttribute("results","Invalid Parameter.");
                e.printStackTrace(System.err);
                errorFound = true;
            }

            if( errorFound ) {
                try {
                    assemblyIds = sAction.getAssemblyIds();
                    req.setAttribute("assemblyIds", assemblyIds);
                    requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_SCHEDULEWO_PAGE));
                    return;

                } catch( Exception e ) {
                    //log the exception with error page
                    req.setAttribute("error", "internal error");
                    req.setAttribute("message", "An internal error has occured in doMfgScheduleWO");
                    req.setAttribute("status_code", "1");
                    req.setAttribute("exception", e);
                    e.printStackTrace(System.err);
                    requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
                    return;
                }

            }

            try {

                if( str_oLineId == null || str_salesId == null || str_dueDate == null ) {
                    woID = sAction.scheduleWorkOrder(str_assemblyId, qty, dueDate);
                } else {
                    woID = sAction.scheduleWorkOrder(salesId,oLineId,str_assemblyId,qty, dueDate);
                }
                req.setAttribute("results","Scheduled order Work Order: "+woID.toString()+".");
                req.setAttribute("defaultWoID",woID.toString());
                req.setAttribute("displayAction","Update");
                req.setAttribute("nextAction","mfgupdatewo");

            } catch( Exception e ) {
                //log the exception with error page
                req.setAttribute("error", "internal error");
                req.setAttribute("message", "An internal error has occured in doMfgScheduleWO");
                req.setAttribute("status_code", "1");
                req.setAttribute("exception", e);
                e.printStackTrace(System.err);
                requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
                return;
            }
        }
        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
        return;
    }


    /**
     * MFG Cancel WorkOrder Page.
     * Dispatch to the MFG Cancel WorkOrder Page
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doMfgCancelWO(
                             ServletContext ctx,
                             HttpServletRequest req,
                             HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgCancelWO()");
        }
        String results;
        Integer woID = null;

        req.setAttribute("displayAction","Cancel");
        req.setAttribute("nextAction","mfgcancelwo");

        String str_woID = req.getParameter("order_id");
        if( str_woID == null ) {
            req.setAttribute("results","");
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }
        try {
            woID = Integer.valueOf(str_woID);
        } catch( NumberFormatException e ) {
            req.setAttribute("results","Invalid Work Order: "+str_woID+".");
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }

        try {

            if( sAction.cancelWorkOrder(woID) ) {
                req.setAttribute("results","Cancelled order Work Order: "+str_woID+".");

            } else {
                req.setAttribute("results","Work Order Number: "+str_woID+" can't be Cancelled.");

            }
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doMfgCancelWO");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
            return;
        }

        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
        return;
    }


    /**
     * MFG Complete WorkOrder Page.
     * Dispatch to the MFG Complete WorkOrder Page
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doMfgCompleteWO(
                               ServletContext ctx,
                               HttpServletRequest req,
                               HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgCompleteWO()");
        }
        String results;
        Integer woID = null;

        req.setAttribute("displayAction","Complete");
        req.setAttribute("nextAction","mfgcompletewo");

        String str_woID = req.getParameter("order_id");
        if( str_woID == null ) {
            req.setAttribute("results","");
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }
        try {
            woID = Integer.valueOf(str_woID);
        } catch( NumberFormatException e ) {
            req.setAttribute("results","Invalid Work Order: "+str_woID+".");
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }

        try {

            if( sAction.completeWorkOrder(woID) ) {
                req.setAttribute("results","Completed order Work Order: "+str_woID+".");

            } else {
                req.setAttribute("results","Work Order Number: "+str_woID+" can't be Completed.");

            }
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doMfgCompleteWO");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
            return;
        }

        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
        return;
    }


    /**
     * MFG Update WorkOrder Page.
     * Dispatch to the MFG Update WorkOrder Page
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doMfgUpdateWO(
                             ServletContext ctx,
                             HttpServletRequest req,
                             HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgUpdateWO()");
        }
        String results;
        Integer woID = null;

        req.setAttribute("displayAction","Update");
        req.setAttribute("nextAction","mfgupdatewo");

        String str_woID = req.getParameter("order_id");
        if( str_woID == null ) {
            req.setAttribute("results","");
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }
        try {
            woID = Integer.valueOf(str_woID);
        } catch( NumberFormatException e ) {
            req.setAttribute("results","Invalid Work Order: "+str_woID+".");
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
            return;
        }

        try {
            if( sAction.updateWorkOrder(woID) ) {
                req.setAttribute("results","Updated order Work Order: "+str_woID+".  The current status of the order is "+sAction.getWorkOrderStatus(woID)+".");
                req.setAttribute("defaultWoID",str_woID);

            } else {
                req.setAttribute("results","Work Order Number: "+str_woID+" can't be Updated.  It is in the final stage");
                req.setAttribute("defaultWoID",str_woID);
                req.setAttribute("displayAction","Complete");
                req.setAttribute("nextAction","mfgcompletewo");
            }
        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doMfgUpdateWO");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
            return;
        }

        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_WORKORDER_PAGE));
        return;
    }


    /**
     * MFG Large Order Page.
     * Dispatch to the MFG Large Order Page
     *
     * @param userID The User to login
     * @param ctx the servlet context
     * @param req the HttpRequest object
     * @param resp the HttpResponse object
     * @exception javax.servlet.ServletException If a servlet specific exception is encountered
     * @exception javax.io.IOException If an exception occurs while writing results back to the user
     *
     */
    public void doMfgFindLOs(
                            ServletContext ctx,
                            HttpServletRequest req,
                            HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecServletAction.doMfgUpdateWO()");
        }


        try {
            Vector listLOs = sAction.findLargeOrders();

            req.setAttribute("listLOs", listLOs);

        } catch( Exception e ) {
            //log the exception with error page
            req.setAttribute("error", "internal error");
            req.setAttribute("message", "An internal error has occured in doMfgFindLOs");
            req.setAttribute("status_code", "1");
            req.setAttribute("exception", e);
            e.printStackTrace(System.err);
            requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ERROR_PAGE));
            return;
        }

        requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.MFG_LARGEORDER_PAGE));
        return;
    }

	/**
	 * Tests that the correct transactional attributes are applied to ensure data integrity
	 *
	 * @param userID The User to login
	 * @param ctx the servlet context
	 * @param req the HttpRequest object
	 * @param resp the HttpResponse object
	 * @exception javax.servlet.ServletException If a servlet specific exception is encountered
	 * @exception javax.io.IOException If an exception occurs while writing results back to the user
	 *
	 */
	public void doAtomicityTests(
							ServletContext ctx,
							HttpServletRequest req,
							HttpServletResponse resp)
							throws ServletException, IOException {

		boolean test1, test2, test3, test4;
		try{
			test1 = sAction.atomicityTestOne();
			req.setAttribute("test1", new Boolean(test1));
		}catch(Exception e){
			req.setAttribute("test1", new Boolean(false));
			e.printStackTrace(System.err);
		}
		try{
			test2 = sAction.atomicityTestTwo();
			req.setAttribute("test2", new Boolean(test2));
		}catch(Exception e){
			req.setAttribute("test2", new Boolean(false));
			e.printStackTrace(System.err);
		}
		try{
			test3 = sAction.atomicityTestThree();
			req.setAttribute("test3", new Boolean(test3));
		}catch(Exception e){
			req.setAttribute("test3", new Boolean(false));
			e.printStackTrace(System.err);
		}
		requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.ATOMICITY_PAGE));
		return;
	}

	public void doCacheTest(
							ServletContext ctx,
							HttpServletRequest req,
							HttpServletResponse resp,
							int cacheTestId,
							String itemToUpdate)
							throws ServletException, IOException {
		ItemsDataBean idb = null;
		if(cacheTestId==0){
			BigDecimal newValue = null;
			// This is where we want to hydrate the cache and then update the value in teh
			// database so later on we can check that it has been updated
			try{
				idb = sAction.getItemInfo(itemToUpdate);
				newValue = idb.getPrice().add(new BigDecimal(2000.00));
				sAction.updateItemPrice(itemToUpdate, newValue);
			}catch(Exception e){
				req.setAttribute("cacheTest", null);
				e.printStackTrace(System.err);
			}
			req.setAttribute("cacheTest", newValue);
		}else{
			try {
				idb = sAction.getItemInfo(itemToUpdate);
			} catch (Exception e) {
				req.setAttribute("cacheTest", null);
				e.printStackTrace(System.err);
			}
			req.setAttribute("cacheTest", idb.getPrice());
		}
		requestDispatch(ctx, req, resp, SpecConfig.getPage(SpecConfig.CACHETEST_PAGE));
		return;
	}
}
