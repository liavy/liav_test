/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID          Description
 *  ----------    --------    ----------------------------------------------
 *  Jan 21, 2003  Tom Daly    Creation date
 *  Nov 1, 2003   TD          Add new response timing mechanism
 *  Nov 25, 2003  Tom Daly    Add support for categories
 *  Feb 15, 2004  Tom Daly    Add cacheTest and atomicityTest methods, these are called
 *                            by the Auditor
 *  Feb 16, 2004  Sam Kounev  Removed unneeded import statements.
 *  Feb 17, 2004  Sam Kounev  Fixed a bug in PrintData and PrintErrorData (& --> &&).
 *  Mar 17, 2004  Tom Daly    modified cacheTest , to support working over cluster
 *  June 15, 2004 Ning Sun    Fixed problem with multiple cookie support on SAP WebAS.
 *                            See osgjava-7221 and osgjava-7228.
 *
 */

package org.spec.jappserver.driver.http;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.spec.jappserver.driver.Debug;
import org.spec.jappserver.driver.event.StatisticBuilder2;
import org.spec.jappserver.orders.helper.ItemQuantity;

/*
 * Implements the http calls from the Dealer Domain to the Dealer Web page,
 * the functions here proxy from the actions in the DealerEntry thread of the
 * driver hence the workload is not driven from here but instead from the
 * DealerEntry thread.
 *
 * @see DealerAgent
 * @author Tom Daly
 */

public class HttpDealer extends AbstractSJASLoad {

    private HttpRequestData req = null;
    private String cidstr = null;
    private String ident = null;
    private boolean htmloutput = false ;
    private boolean debugoutput = false;

    private PrintStream  trace = null;
    private String queryString;
    private CookieStrings cookie;
    private static final int RETRYCNT = 2;
    private Properties props;
    private int httptimer = 0;
    private static BigDecimal cachedPrice;


    public HttpDealer(Properties props, Connection c) {

        super(c);
        this.props = props;
        queryString = props.getProperty("queryString");

    }

    public HttpDealer(Properties props , String cidstr, String ident, Connection c) {

        super(c);
        this.queryString = props.getProperty("queryString");
        this.cidstr = cidstr;
        this.ident = ident;
        this.props = props;


        queryString = props.getProperty("queryString");
        htmloutput  = new Boolean(props.getProperty("HTMLOUTPUT")).booleanValue() ;
        debugoutput = new Boolean(props.getProperty("DEBUGOUTPUT")).booleanValue() ;

        String trfile = null;
        Debug.println(ident + "The query is " + queryString + "customer is " + cidstr );
        if (htmloutput || debugoutput ) {
            try {
                {

                    trfile="trace" + ident.trim().substring(0,ident.length()-2) + ".html";
                    if (new File(trfile).exists()) {
                        trace = new PrintStream(new FileOutputStream(trfile, true));
                    }
                    else {  // try creating it
                        Debug.println(ident + "HTML debugging enabled : creating output file " + trfile );
                        trace = new PrintStream(new FileOutputStream(trfile));
                    }
                }

            }catch (Exception e) {
                Debug.println(ident + "Could not create " + trfile);

            }
        }
    }

    public void sendIfRetry(int i, String name) {
        if (i > 0) {
            StatisticBuilder2.send("Retry." + name + "." + i, 1);
        }
    }

    /*
     * dealershipInventory - navigates to the dealership inventory web page
     * from here threads/users/dealers  can examine and manipulate open orders and or
     * sell cars.
     *
     * @throws SJASHttpException
     */
    public void dealershipInventory() throws SJASHttpException {

        resetRespTime();
        try {

            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "inventory");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "inventory");

                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            printData(response.getData());
            checkForError();
        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "Error in DealerShip Inventory  ");
            throw new SJASHttpException("Error in dealerShip Inventory" + e);
        }

    }


    /*
     * Use the Purchase Vehicles web page to allow the cars in the ItemQuantity
     * to be ordered by adding them to the shopping cart
     * @see DealerEntry
     * @param itms
     * @throws SJASHttpException
     */
    public void addVehiclesToCart(ItemQuantity[] itms , int category ) throws SJASHttpException {

        int[] searchIndex = new int[1];

        resetRespTime();
        try {

            // use the VIEW ITEMS field of the HTML FORM to select
            // cars and quantities for purchase
            for (int i=0; i< itms.length; i++ ) {
                StringBuffer sb = new StringBuffer(itms[i].itemId);
                for (int j = 0; j < RETRYCNT; j++) {
                    sendIfRetry(j, "View_Items");
                    req = new HttpRequestData(queryString, cookie);
                    req.addParam("action", "View_Items");
                    req.addParam("vehicles",sb.toString() );
                    req.addParam("quantity",Integer.toString(itms[i].itemQuantity) );
                    req.addParam("category", Integer.toString(category) );

                    response = executeAction(HttpMethodConstants.GET,req);
                    httptimer += getTxnRespTime();
                    if (response != null) break;
                }
                if (response.getCookie().num > 0) cookie = response.getCookie();


                printData(response.getData());

                // should now be on the shopping cart screen.
                HttpResponseParser rp = response.getData();
                searchIndex[0] = rp.headerEnd;
                String details = rp.getNextLineContaining("driver-tag-start", searchIndex);

                String namestr = details.substring(details.indexOf("name:")+5,details.indexOf("description"));
                String descstr = details.substring(details.indexOf("description:")+12, details.indexOf("price:"));
                String pricestr = details.substring(details.indexOf("price:")+6, details.indexOf("discount")-1);
                String discstr = details.substring(details.indexOf("discount:")+9, details.indexOf("driver-tag-end")-1);


                Debug.println(ident + "NAME is " + namestr + "DESC is " + descstr  );
                Debug.println(ident + "Price " + pricestr );
                Debug.println(ident + "discount " + discstr );

                for (int j = 0; j < RETRYCNT; j++) {
                    sendIfRetry(j, namestr);
                    // now do the actual add to the cart
                    req = new HttpRequestData(queryString, cookie);
                    req.addParam("name", namestr);
                    req.addParam("description", descstr);
                    req.addParam("price", pricestr);
                    req.addParam("discount", discstr);
                    req.addParam("quantity",Integer.toString(itms[i].itemQuantity) );
                    req.addParam("itemId", sb.toString() );
                    req.addParam("action", "Add to Cart");

                    response = executeAction(HttpMethodConstants.GET,req);

                    httptimer += getTxnRespTime();

                    if (response != null) break;
                }
                if (response.getCookie().num > 0) cookie = response.getCookie();

                printData(response.getData());
                checkForError();
            }

        }catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData(), req);
            Debug.println(ident + "error adding cars to cart for customer " + cidstr + " " + e);
            throw new SJASHttpException("got an error when adding cars to cart " + e);
        }
    }

    /*
     * checkout or purchase the shopping cart
     * @param checktype "purchase" or "defer"
     * @throws SJASHttpException
     * @throws SJASHttpInsufficientCreditException
     */
    public void  checkOut(String checktype ) throws SJASHttpException,
    SJASHttpInsufficientCreditException {

        resetRespTime();
        try {

            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "checkOut");
                req = new HttpRequestData(queryString, cookie);
                if (checktype.equals("purchase")) {
                    req.addParam("action", "purchasecart");
                } else
                    req.addParam("action", "deferorder");
                response = executeAction(HttpMethodConstants.GET,req);

                httptimer += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();
            printData(response.getData());
            checkForCreditError();
            checkForError();

        } catch (SJASHttpException sjase) {
            printErrorData(response.getData());
            if (sjase instanceof SJASHttpInsufficientCreditException ) {
                Debug.println(ident + ":got a credit error checking out customer " + cidstr );
                throw sjase;
            }
            else
                throw new SJASHttpException(ident + " " + sjase + "got an error checking out " + cidstr );
        }
    }


    /*
     * clearCart
     * This method should only get called after cars have been added to the cart
     * in the same session as this call or else an error will occur.
     *
     * @throws SJASHttpException
     */
    public void clearCart() throws SJASHttpException {

        resetRespTime();
        try {
            // navigate to the shopping cart screen
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "shoppingcart");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "shoppingcart");
                response = executeAction(HttpMethodConstants.GET,req);

                httptimer  += getTxnRespTime();
                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "clearcart");
                // now use the clearcart option on the web page to empty cart
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "clearcart");

                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();
            printData(response.getData());
            checkForError();

        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "error clearing cart  for customer " + cidstr );
            throw new SJASHttpException("error clearing cart for customer " + cidstr + " " + e);
        }

    }

    /*
     * This method should only get called after cars have been added to the cart
     * and in the same session as this call or else an error will occur.
     *
     * @throws SJASHttpException
     */
    public void removeVehiclesFromCart() throws SJASHttpException {

        resetRespTime();
        try {
            // navigate to the shopping cart screen
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "shoppingcart");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "shoppingcart");
                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            // now use the clearcart option on the web page to empty cart
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "remove");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "remove");
                req.addParam("cartID", "0");

                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            printData(response.getData());
            checkForError();

        }  catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "error removing car from cart  " + e);
            throw new SJASHttpException("error removing car from cart for customer " + cidstr + " " + e);
        }

    }

    /*
     * Get count of open orders by reading them from the dealership Inventory web page
     * If the dealership inventory is not the current web page this method will throw
     * an exception
     *
     * @param none
     * @return list of the open order ids for this dealership
     **/
    public ArrayList getOpenOrders() {

        int[] searchIndex = new int[1];

        Debug.println("DealerEntry: getOpenOrders called");
        // search for the string cancel order, which indicates an order is open
        ArrayList oo = new ArrayList();
        String ostr;

        HttpResponseParser rp = response.getData();
        searchIndex[0] = rp.headerEnd;

        resetRespTime();
        try {

            while ( (ostr=rp.getNextLineContaining("action=cancelorder", searchIndex)) != null) {
                ostr = ostr.substring(ostr.indexOf("cancelorder&orderID=")+20, ostr.indexOf("cancel order")-2);
                oo.add(ostr);
            }

            Debug.println("DealerEntry: Found " + oo.size() + "open orders");
            return oo;

        }  catch  (Exception e ) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "error while getting open orders  " + e);
            return null;
        }
    }

    /*
     * cancel a open order
     * assumes we have called dealershipInventory and we are ready to
     * remove cars using the dealerShip inventory screen of the web app
     *
     * @param oidstr    the order id to cancel
     * @throws SJASHttpException
     * @throws SJASHttpAppException
     */
    public void cancelOpenOrder(String oidstr) throws
    SJASHttpException, SJASHttpAppException {

        resetRespTime();
        try {
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "cancelorder");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "cancelorder");
                req.addParam("orderID", oidstr);
                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            //check this order hasn't already been cancelled by another thread
            printData(response.getData());
            checkForAppError("has already been cancelled");
            checkForError();

        }  catch  (SJASHttpAppException ae ) {
            printErrorData(response.getData());
            throw new SJASHttpAppException("Error cancelling open order for " + cidstr + " " + ae);
        } catch  (Exception e ) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println("error trying to cancel open order " + e);
            throw new SJASHttpException("cancelling open order for customer " + cidstr + " " + e);
        }
    }

       /*
        * Use the dealership inventory web page to look for vehicles available to sell.
        *
        * @returns : a list of item quantities which the DealerEntry thread can use to determine
        *           which cars to sell.
        *
        **/
    public ArrayList getVehiclesForSale() throws SJASHttpException {

        HttpResponseParser rp = response.getData();

        // search for the string cancel order, which indicates an order is open
        ArrayList carsList = new ArrayList();
        int[] searchIndex = new int[1];
        searchIndex[0] = rp.headerEnd;
        String invs;
        String car;
        String qtystr;
        int qty;
        String totalstr;

        try {
            while ( (invs=rp.getNextLineContaining("action=sellinventory", searchIndex)) != null) {

                //dig out the ITEM ID of the car from the table
                car = invs.substring(invs.indexOf("vehicleToSell=")+14,invs.indexOf("&total"));

                // now dig out the qty from the same string
                qtystr = invs.substring(invs.indexOf("quantity ")+9,invs.indexOf("-->")-1);
                qty = Integer.parseInt(qtystr);

                //add the total cost
                totalstr = invs.substring(invs.indexOf("total=")+6,invs.indexOf(">Sell"));
                BigDecimal total = new BigDecimal(totalstr.trim());

                ItemQuantity iq = new ItemQuantity(car,qty,total);

                carsList.add(iq);

                //dig out the ITEM ID of the car from the table
                car = invs.substring(invs.indexOf("vehicleToSell=")+14,invs.indexOf("total=")-1);

            }
            return carsList;

        } catch  (Exception e ) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println("error trying to get the vehicle sCount" + e);
            throw new SJASHttpException("error getting vehicle count for customer " + cidstr + " " + e);
        }

    }

    /*
     * sell cars from inventory
     *
     * @param car is the inventory item id to sell
     * @param qty how many cars to sell
     * @param total
     * @throws SJASHttpException
     * @throws SJASHttpAppException
     */
    public void sellVehiclesFromInventory(String car, int qty, BigDecimal total)
    throws SJASHttpException, SJASHttpAppException {

        resetRespTime();
        try {

            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "sellinventory");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "sellinventory");
                req.addParam("vehicleToSell", car);
                req.addParam("total", total.toString());

                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();

            printData(response.getData());
            checkForAppError("have already been sold");
            checkForError();

        } catch  (SJASHttpAppException ae ) {
            printErrorData(response.getData());
            throw new SJASHttpAppException("Error vehicle already sold " + cidstr + " " + ae);
        } catch  (Exception e ) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "unknown error while trying to sell vehicles " + e);
            throw new SJASHttpException("unknown error while trying to sell vehicles for customer :  " + cidstr + " " + e);
        }
    }

    /*
     * browseVehicles - select "next" auto in the list from the factory.
     * assumes user thread had navigated to the purchase vehicles screen
     *
     * @param direction
     * @throws SJASHttpException
     */
    public void browseVehicles(String direction, int category) throws SJASHttpException {

       resetRespTime();
        try {
            if (direction.equals("top")) {
                // go to the top of the cars/items list

                for (int i = 0; i < RETRYCNT; i++) {
                    sendIfRetry(i, "View_Items");
                    req = new HttpRequestData(queryString, cookie);
                    req.addParam("action", "View_Items" );
                    req.addParam("category", Integer.toString(category));
                    response = executeAction(HttpMethodConstants.GET,req);
                    httptimer  += getTxnRespTime();

                    if (response != null) break;
                }
                if (response.getCookie().num > 0) cookie = response.getCookie();
            } else {
                for (int i = 0; i < RETRYCNT; i++) {
                    sendIfRetry(i, "View_Items");
                    // now try and browse the items available for purchase
                    req = new HttpRequestData(queryString, cookie);
                    req.addParam("action", "View_Items");
                    req.addParam("browse", direction );
                    req.addParam("category", Integer.toString(category));

                    response = executeAction(HttpMethodConstants.GET,req);
                    httptimer  += getTxnRespTime();
                    if (response != null) break;
                }
                if (response.getCookie().num > 0) cookie = response.getCookie();
                printData(response.getData());
                checkForError();
            }
        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println("got an error while browsing the items/autos ");
            throw new SJASHttpException("error browsing autos for customer " + cidstr + " " + e);
        }
    }

    /*
     * goHome - take the user back to the home screen.
     *
     * @throws SJASHttpException
     */
    public void goHome() throws SJASHttpException {

        resetRespTime();
        try {
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "home");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "home" );
                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();
                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();
            printData(response.getData());
            checkForError();
        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "error returning to home screen");
            throw new SJASHttpException("error returning to home screen for : " + cidstr + " " + e);
        }
    }

    /*
     * login to SPECjappserver2004
     * @param ident : the unique agent number thread number for this thread.
     * @throws SJASHttpException
     */
    public void login() throws SJASHttpException {

        resetRespTime();
        try {
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "login");

                // invalidate login cookie from previous login
                if (cookie != null)
                    cookie.entries[0] = null;

                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "login");
                req.addParam("uid", cidstr);
                req.addParam("submit", "Log in");

                response = executeAction(HttpMethodConstants.GET,req );
                httptimer  += getTxnRespTime();

                if (response != null) break;
            }

            if (response.getCookie().num > 0) cookie = response.getCookie();
            printData(response.getData());
            checkForError();

        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "login error "  + e ) ;
            throw new SJASHttpException("error logging in customer " + cidstr + " " + e);
        }
    }

    /*
     * logout - log the virtual user / thread out from the application
     * @throws SJASHttpException
     */
    public void logout() throws SJASHttpException {

        resetRespTime();
        try {

            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "logout");
                req = new HttpRequestData(queryString,cookie);
                req.addParam("action", "logout");

                response = executeAction(HttpMethodConstants.GET,req);
                httptimer  += getTxnRespTime();
                if (response != null) break;
            }
            if (response.getCookie().num > 0) cookie = response.getCookie();
            printData(response.getData());
            checkForError();

        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println(ident + "got an error while trying to log out" + e);
            throw new SJASHttpException("error logging out for customer " + cidstr + " " + e);
        }

    }

    public int getRespTime() {
        // return the response time for the last Http Dealer transaction
        return httptimer;
    }

    public void resetRespTime() {
        httptimer = 0;
    }

     /*
      * cacheTest-call web tier to run the cache test which checks that itement cache rules
      *are being followed
      *
      *@param t the test to perform. 0 for the initial test and to set initial value and 1 for the post run test
      *@return true=cache test passed ok, false = failure this is an invalid benchmark run
      */
    public boolean cacheTest(int t, String part ) throws SJASHttpException {

        boolean passed= false;
        int[] searchIndex = new int[1];

        try {
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "cacheTest");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "cacheTest");
                req.addParam("test2perform", Integer.toString(t) );
                req.addParam("item", part);
                response = executeAction(HttpMethodConstants.GET,req );
                if (response != null) break;
            }

            if (response.getCookie().num > 0) cookie = response.getCookie();
            checkForError();

            //dig out and save the price returned
            HttpResponseParser rp = response.getData();
            searchIndex[0] = rp.headerEnd;
            String s = rp.getNextLineContaining("on this automobile price:", searchIndex);
            String priceStr = s.substring(s.indexOf("price:</b>")+31);

            //if this is is the first part of the cache test, then just save the price
            if(t == 0) {
                Debug.println("cachedValue from test " + t +  " is " + priceStr);
                cachedPrice = new BigDecimal(priceStr.trim());
                passed = true;
            } else {
             // if running the second part of the cache test then get the price and
             // compare to the cached value.
             // If the price is the same as the cached value then test passes as this
             // value must have re read from the database by the app server during the run.
              Debug.println("cachedValue from test " + t +  " is " + priceStr);
              BigDecimal newPrice = new BigDecimal( priceStr.trim());
              if (newPrice.compareTo(cachedPrice) == 0)
                  passed = true;
              else
                  passed = false;
            }
            //valid = checkForString(": </b>  true");
            //debug only
            //System.out.println(response.getData().toString());
            //System.out.println("valid response is : " + valid);
            return passed ;

        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println("cache test error  "  + e ) ;
            throw new SJASHttpException("error runnning cache test for " + e);
        }
    }

    /**
     * atomicityTest : calls the web page to run the atomicity tests
     *@returns : String: response from the web page
     *@throws SJASHttpException : encountered an error from the web tier
     **/
    public String atomicityTest() throws SJASHttpException {
        try {
            for (int i = 0; i < RETRYCNT; i++) {
                sendIfRetry(i, "atomicityTests");
                req = new HttpRequestData(queryString, cookie);
                req.addParam("action", "atomicityTests");
                response = executeAction(HttpMethodConstants.GET,req );

                if (response != null) break;
            }

            if (response.getCookie().num > 0) cookie = response.getCookie();

            printData(response.getData() );
            checkForError();

            String s = response.getData().toString();
            s = s.substring( s.indexOf("<!DOCTYPE HTML") );
            return s;

        } catch (Exception e) {
           e.printStackTrace();
            printErrorData(response.getData());
            Debug.println("atomicity test error  "  + e ) ;
            throw new SJASHttpException("error runnning atomicity test" + e);
        }

    }

    /*
     * printData - for debug purposes output the data coming back from the
     *             web tier is written to  a file which can be conveniently
     *             examined with a web browser.
     *@param HttpResponseParser data,   html data back from the web tier
     */
    public void printData(HttpResponseParser data) {
        if (htmloutput && data != null)  {
            trace.println(data.toString());
        }
    }

    /*
     * printErrorData - for debug purposes output the data coming back from the
     *             web tier after an exception has been caught
     *@param HttpResponseParser the data from the web teir
     */
    public void printErrorData(HttpResponseParser data, HttpRequestData req) {
        if (debugoutput && data != null) {
            Enumeration keys = req.getParamKeys();
            String key ;
            String value;

            while (keys.hasMoreElements() ) {
                key = (String)keys.nextElement();
                value = req.getParam(key);
                trace.println("Request: " + key + " " + value);
            }
            trace.println(data.toString());
        }
    }

     /*
     * printErrorData - for debug purposes output the data coming back from the
     *             web tier after an exception has been caught
     *@param HttpResponseParser the data from the web teir
     */
    public void printErrorData(HttpResponseParser data) {
        if (debugoutput && data != null) {
            trace.println(data.toString());
        }
    }

}
