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
 *  2003/05/01  John Stecher, IBM       Imported Russ Raymundo's mfg side of the benchmark for web base driving
 *  2003/06/30  John Stecher, IBM       Added support for local interfaces from web
 *  2004/01/25  John Stecher, IBM       Added code for cache consistancy test.
 *  2004/03/11  John Stecher, IBM       Updated to make cache test work correctly in a cluster
 *  2004/03/16  Samuel Kounev, Darmstadt  Cleared unused import statements.
 */


package org.spec.jappserver.servlet.helper;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.DebugPrint;

/**
 * SpecConfig is a JavaBean holding all configuration and runtime parameters for the SPEC application
 *
 */

public class SpecConfig {

	/* SPEC Runtime Configuration Parameters */
    public static boolean debugging = false;
    public static Debug debug;
	public static boolean web_to_ejb_locals = false;

    /* SPEC Web Interface parameters */
    public static String[] webInterfaceNames = { "JSP", "JSP-Images"};
    public static final int JSP=            0;
    public static final int JSP_Images= 1;
    public static int webInterface =        JSP;

    /* JSP pages for all Spec Actions */

    public final static int WELCOME_PAGE =      0;
    public final static int REGISTER_PAGE =     1;
    public final static int SHOPPINGCART_PAGE =     2;
    public final static int VEHICLEQUOTE_PAGE =         3;
    public final static int HOME_PAGE =         4;
    public final static int INVENTORY_PAGE =        5;
    public final static int ORDER_PAGE =        6;
    public final static int ERROR_PAGE =      7;
    public final static int MFG_WELCOME_PAGE =      8;
    public final static int MFG_HOME_PAGE =      9;
    public final static int MFG_WORKORDER_PAGE =      10;
    public final static int MFG_LARGEORDER_PAGE =      11;
    public final static int MFG_SCHEDULEWO_PAGE =      12;
    public final static int ATOMICITY_PAGE = 	13;
	public final static int CACHETEST_PAGE = 	14;

    public static String webUI[][] =
    {
        {   "/welcome.jsp", "/register.jsp", "/shoppingcart.jsp", "/purchase.jsp",
            "/SpecJhome.jsp", "/dealerinventory.jsp", "/order.jsp", "/error.jsp",
            "/mfgwelcome.jsp", "/mfghome.jsp", "/mfgworkorder.jsp", "/mfglargeorder.jsp",
            "/mfgschedulewo.jsp", "/atomicity.jsp", "/cachetest.jsp"},  //JSP Interface
        {   "/welcomeImg.jsp", "/registerImg.jsp", "/portfolioImg.jsp", "/quoteImg.jsp",
            "/tradehomeImg.jsp", "/accountImg.jsp", "/orderImg.jsp", "/config.jsp",
            "/runStats.jsp"},  //JSP Graphics Interface future
    };

    /**
     * Return a Spec UI Web page based on the current configuration
     * This will return a JSP page
     */

    public static String getPage(int pageNumber) {
        return webUI[webInterface][pageNumber];
    }

    /**
     * Gets the webInterfaceNames
     * @return Returns a String[]
     */
    public static String[] getWebInterfaceNames() {
        return webInterfaceNames;
    }

    public static void setupWebTier(Object client) {
        InitialContext ic = null;
        try {
            ic    = new InitialContext();
        } catch( Exception e ) {
           e.printStackTrace();
            System.out.println("Cannot construct InitialContext!");
        }
        try {
            int debugLevel =
            ((Integer) ic.lookup("java:comp/env/debuglevel"))
            .intValue();

            if( debugLevel > 0 ) {
                debug = new DebugPrint(debugLevel, client);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }

			web_to_ejb_locals =((Boolean) ic.lookup("java:comp/env/localwebtoejb")).booleanValue();
        } catch( NamingException ne ) {
           ne.printStackTrace();
            System.out.println("SpecConfig: debuglevel Property not set. "
                               + "Turning off debug messages and using remote interfaces");

            debug = new Debug();
			web_to_ejb_locals = false;
        }
    }
}
