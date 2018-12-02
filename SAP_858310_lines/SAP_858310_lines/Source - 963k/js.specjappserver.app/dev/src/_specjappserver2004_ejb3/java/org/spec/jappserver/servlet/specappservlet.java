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
 *  2003/05/06  John Stecher, IBM       Made changes to allow driver instead of application to determine
 *                                      if an order is to be deferred or added to inventory immediately
 *  2003/11/25  Tom Daly    , Sun       added support for category
 *  2003/12/05  John Stecher, IBM       Added Atomicity Tests
 *  2003/01/08  John Stecher, IBM       Changed code to eliminate unused objects being passed into methods
 *  2004/01/25  John Stecher, IBM       Added code for cache consistancy test.
 */

package org.spec.jappserver.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.spec.jappserver.Config;
import org.spec.jappserver.servlet.helper.SpecConfig;
import org.spec.jappserver.servlet.helper.SpecServletAction;

import com.sap.engine.tools.sharecheck.SessionSerializationReport;
import com.sap.engine.tools.sharecheck.SessionSerializationReportFactory;

/**
 *
 * SpecAppServlet provides the standard web interface to Spec and can be accessed with the Go Trade Autos! link.
 * Driving benchmark load using this interface requires a sophisticated web load generator that is capable of
 * filling HTML forms and posting dynamic data.
 */

public class SpecAppServlet extends HttpServlet {

    // Local Variables
    SpecServletAction spAction = null;

    /**
     * Servlet initialization method.
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        SpecConfig.setupWebTier(this);
        spAction = new SpecServletAction();
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "initialed SpecAppServlet ");
        }
    }


    /**
     * Returns a string that contains information about SpecScenarioServlet
     *
     * @return The servlet information
     */
    public java.lang.String getServletInfo() {
        return "SpecAppServlet provides the standard web interface to SPECjAppServer2004";
    }


    /**
     * Process incoming HTTP GET requests
     *
     * @param request Object that encapsulates the request to the servlet
     * @param response Object that encapsulates the response from the servlet
     */
    public void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
    throws ServletException, IOException
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAppServlet.doGet() ");
        }
        performTaskAndCheckSharability(request,response);
    }

    /**
     * Process incoming HTTP POST requests
     *
     * @param request Object that encapsulates the request to the servlet
     * @param response Object that encapsulates the response from the servlet
     */
    public void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
    throws ServletException, IOException
    {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAppServlet.doPost() ");
        }
        performTaskAndCheckSharability(request,response);
        
    }

    private void performTaskAndCheckSharability(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        performTask(req, resp);
        if (Config.isSharabilityChecked)
        {
           Config.checkSharability(req.getSession());
        }
    }
    
    /**
     * Main service method for SpecAppServlet
     *
     * @param request Object that encapsulates the request to the servlet
     * @param response Object that encapsulates the response from the servlet
     */
    public void performTask(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAppServlet.performTask() ");
        }
        String action = null;
        Integer uid = null;
        String dispPath = null; // Dispatch Path to SpecAppServlet
        resp.setContentType("text/html");
        //SpecServletAction spAction = new SpecServletAction();
        action = req.getParameter("action");
        ServletContext ctx = getServletConfig().getServletContext();
        if( action == null ) {
            spAction.doWelcome(ctx, req, resp, "");
            return;
        } else if( action.equals("login") ) {
            String userID = req.getParameter("uid");
            try {
                uid = Integer.valueOf(userID);
            } catch( NumberFormatException e ) {
                spAction.doWelcome(ctx, req, resp, "Invalid User ID.  Must be an Integer");
                if( SpecConfig.debugging ) {
                    SpecConfig.debug.println(3, "User Login failed with a " + e);
                }
                return;
            }
            // Here we need to check the return and see if login succeeded
            spAction.doLogin(ctx, req, resp, uid);
            return;
        } else if( action.equals("mfglogin") ) {
            String userID = req.getParameter("uid");
            if( userID == null ) {
                spAction.doMfgWelcome(ctx,req,resp,"");
                return;
            }
            try {
                uid = Integer.valueOf(userID);
            } catch( NumberFormatException e ) {
                spAction.doMfgWelcome(ctx, req, resp, "Invalid User ID.  Must be an Integer");
                return;
            }
            // Here we need to check the return and see if login succeeded
            spAction.doMfgLogin(ctx, req, resp, uid);
            return;
        } else if( action.equals("atomicityTests")){
        	System.out.println("Starting Atomicity Tests...");
			spAction.doAtomicityTests(ctx,req,resp);
			return;
        } else if( action.equals("cacheTest")){
        	int cacheTestId = Integer.parseInt((String)req.getParameter("test2perform"));
			String itemID = (String)req.getParameter("item");
        	spAction.doCacheTest(ctx,req,resp,cacheTestId,itemID);
        	return;
        }

        // The rest of the operations require the user to be logged in -
        // Get the Session and validate the user.
        HttpSession session = req.getSession();
        try {
            uid = (Integer) session.getAttribute("uidBean");
        } catch( Exception e ) {
            spAction.doWelcome(ctx, req, resp, "Invalid UserID  ");
        }

        if( uid == null ) {
            System.out.println("SpecAppServlet service error: User Not Logged in");
            spAction.doWelcome(ctx, req, resp, "User Not Logged in");
            return;
        }
        if( action.equals("View_Items") ) {
            String vehicles = req.getParameter("vehicles");
            String catstr = req.getParameter("category");

            int category=0;
            try {
                category = Integer.parseInt(catstr);
                //System.out.println("DEBUG SpecAppServlet category is " + category );
                spAction.doVehicleQuotes(ctx, req, resp, uid, vehicles, category);

            } catch ( NumberFormatException e ) {
                System.out.println("SpecAppServlet: Invalid Category=" + catstr);

                spAction.doWelcome(ctx, req, resp, "SpecAppServlet: Invalid Category" + catstr);
                return;
            }

        } else if( action.equals("shoppingcart") ) {
            spAction.doShoppingCart(ctx, req, resp, uid);
        } else if( action.equals("Add to Cart") ) {
            spAction.doAddToShoppingCart(ctx, req, resp, uid);
        } else if( action.equals("remove") ) {
            spAction.doRemoveFromShoppingCart(ctx, req, resp, uid);
        } else if( action.equals("clearcart") ) {
            spAction.doClearCart(ctx, req, resp, uid);
        } else if( action.equals("purchasecart") ) {
            spAction.doPurchase(ctx, req, resp, uid);
        } else if( action.equals("deferorder") ) {
            spAction.doDeferedPurchase(ctx, req, resp, uid);
        } else if( action.equals("cancelorder") ) {
            spAction.doCancelOrder(ctx, req, resp, uid);
        } else if( action.equals("sellinventory") ) {
            spAction.doSell(ctx, req, resp, uid);
        } else if( action.equals("logout") ) {
            spAction.doLogout(ctx, req, resp, uid);
        } else if( action.equals("home") ) {
            spAction.doHome(ctx, req, resp, uid, "Ready to Wheel and deal");
        } else if( action.equals("inventory") ) {
            spAction.doInventory(ctx, req, resp, uid);
        } else if( action.equals("mfghome") ) {
            spAction.doMfgHome(ctx, req, resp, uid, "Ready to Mfg!");
        } else if( action.equals("mfglogout") ) {
            spAction.doMfgLogout(ctx, req, resp, uid);
        } else if( action.equals("mfgschedulewo") ) {
            spAction.doMfgScheduleWO(ctx, req, resp);
        } else if( action.equals("mfgcancelwo") ) {
            spAction.doMfgCancelWO(ctx, req, resp);
        } else if( action.equals("mfgcompletewo") ) {
            spAction.doMfgCompleteWO(ctx, req, resp);
        } else if( action.equals("mfgupdatewo") ) {
            spAction.doMfgUpdateWO(ctx, req, resp);
        } else if( action.equals("mfgfindlos") ) {
            spAction.doMfgFindLOs(ctx, req, resp);
        } else {
            System.out.println("SpecAppServlet: Invalid Action=" + action);
            spAction.doWelcome(ctx, req, resp, "SpecAppServlet: Invalid Action" + action);
        };
    }
    
    private void sendRedirect(HttpServletResponse resp, String page)
    throws ServletException, IOException {
        if( SpecConfig.debugging ) {
            SpecConfig.debug.println(3, "SpecAppServlet.sendRedirect() ");
        }
        resp.sendRedirect(resp.encodeRedirectURL(page));
    }

    // URL Path Prefix for dispatching to SpecAppServlet
    private final static String sasPathPrefix = "/app?action=";

}
