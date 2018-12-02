/*
* Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
*               All rights reserved.
*  History:
*  Date          ID        Company      Description
*  ----------    --------  -------      ----------------------------------------------
*  Jan 21, 2003  Tom Daly  Sun          Creation date, credit  to Shanti Subramanyam
*                          Microsystems                as this code is based on OrdersEntry
*                                                      from ECperf and SPECjAppServer2001
*  Nov 1 , 2003  TD        Sun          Added new response time measurements
*  Nov 24, 2003  TD        Sun          Add support for browsing and purchasing by category
*  Nov 25, 2003  S.Kounev  Darmstadt    Modified database scaling rules as per osgjava-5891.
*  Nov 29, 2003  TD        Sun          changed the login/logout logic. Each user/thread now
*                                       logs out and average of two times during steady state
*  Dec 04, 2003  S.Kounev  Darmstadt    Modified getLogoutTime to make sure that logout times are
*                                       scheduled within the steady state interval.
*  Feb 05, 2004  TD        Sun          Fix tx=1 problem with categories, eliminate
*                                       txrate and timer vars from call to HttpDealer (osgjava-6164).
*  Nov, 2004     John Stecher IBM       Added in real time statistics support
*
* $Id: DealerEntry.java,v 1.21 2004/12/02 12:35:32 skounev Exp $
*
*/

package org.spec.jappserver.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.spec.jappserver.driver.event.EprofEventHandler;
import org.spec.jappserver.driver.event.StatisticBuilder2;
import org.spec.jappserver.driver.http.Connection;
import org.spec.jappserver.driver.http.HttpDealer;
import org.spec.jappserver.driver.http.SJASHttpAppException;
import org.spec.jappserver.driver.http.SJASHttpException;
import org.spec.jappserver.driver.http.SJASHttpInsufficientCreditException;
import org.spec.jappserver.orders.helper.ItemQuantity;



/**
* This class implements the per-user thread of the DealerEntry application.
* It uses the http proxy object HttpDealer in the http package to talk to the
* web application that provides the interface into the Dealer Domain
* This class keeps track of response times, think times, transaction counts etc.
* Objects of this class are controller by the DealerAgent.
*
* @see DealerAgent
* @see HttpDealer
*
* @author Tom Daly (adapted from OrderEntry by Shanti Subramanyam)
*/
public class DealerEntry extends Thread {

    private static final String[] mTxTypes = new String[] {
        "ResponseTime.Purchase",
        "ResponseTime.Manage",
        "ResponseTime.Browse"
    };

    int    utxCnt = 0;                     // # of user txns/per current login
    int logoutTime = 0;
    boolean loggedIn = false;
    int id;
    int cid = 0 ;		// customer id
    HttpDealer dealer = null;   // http dealer proxy to the web pages
    Timer timer;
    Properties props;
    DealerStats stats;
    boolean inRamp;		// indicator for rampup or rampdown
    int msBetweenthreadStart = 0;
    int rampUp, stdyState, rampDown;
    int endRampUp, endStdyState, endRampDown;
    int numThreads, txRate, txRatePerAgent;
    int custDBSize;
    int benchStartTime;		// Actual time of rampup start
    String resultsDir;
    RandNum r;
    RandPart rp;
    int numItems;
    int timePerTx;		// Avg. cycle time
    int timeForThisTx;	        // cycle time for this tx.
    String ident;
    boolean start = true;
    boolean statsDone = false;	// Has Agent collected our stats yet ?
    String url;                 // url of the main application web page.
    int category = 0;        // vehicle category browse / purchase from
    int startitem = 0, enditem = 0;  //used to indicate start and end items in choosen category

    // To make the weight calculation
    // The size depends on num of Tx types;
    // MAKE SURE THE TX TYPES AND THE PROPS ARE IN THE SAME ORDER
    int weights[] = new int[DealerStats.TXTYPES];
    int txType[] = {DealerStats.PURCHASE, DealerStats.MANAGE,
                    DealerStats.BROWSE};
    String weightProps[] = {"purchaseWeight", "manageWeight", "browseWeight"};
    int txMenu[] = new int[DealerStats.TXTYPES];
    int totalWeight = 0;

    PrintStream errp;
    String name;

    String hostServer;
    int port;
    String queryString;
    Connection c;


    /**
     * Constructor.
     * @param id for the agent
     * @param timer The timer
     * @param props Properties of the run
     */
    public DealerEntry(int id, Timer timer, Properties props) {

        this.id = id;
        this.timer = timer;

        String errfile = null;

        this.resultsDir = props.getProperty("runOutputDir");
        this.name = props.getProperty("agentName");
        this.ident = name.concat(":" + id + ": ");

        if ( Integer.parseInt(props.getProperty("runDealerEntry")) == 1 ) {
            errfile = resultsDir + System.getProperty("file.separator") + "dealer.err";
        } else {
            errfile = resultsDir + System.getProperty("file.separator") + name + ".err";
        }

        // Create error log if it doesn't already exist
        try {
            if (new File(errfile).exists()) {
                errp = new PrintStream(new FileOutputStream(errfile, true));
            }
            else {	// try creating it
                // Debug.println(ident + "Creating " + errfile);
                errp = new PrintStream(new FileOutputStream(errfile));
            }
        } catch (Exception e) {
           e.printStackTrace();
            System.err.println(ident + "Could not create " + errfile);
            errp = System.err;
        }

        this.props = props;

        // Temp array to sort the weights
        int tmp[] = new int[DealerStats.TXTYPES];

        // Get the TX weights
        for (int i = 0; i < DealerStats.TXTYPES; i++) {
            weights[i] =
                    Integer.parseInt(props.getProperty(weightProps[i]));
            tmp[i] = weights[i];
            totalWeight += weights[i];
        }

        // Sort the weights in ascending order
        Arrays.sort(tmp);
        // Place the TX Types in ascending order of wight
        // To make sure the type is not duplicated when the weights are
        // equal, make the type -1 after putting into the txMenu
        for (int i = 0; i < DealerStats.TXTYPES; i++)
            for (int j = 0; j < DealerStats.TXTYPES; j++)
                if ((weights[i] == tmp[j]) && (txType[i] != -1)) {
                    txMenu[j] = txType[i];
                    txType[i] = -1;
                }

        // We can now use the sorted weights
        weights = tmp;
        // Now adjust the weights
        for (int i = 1; i < DealerStats.TXTYPES; i++)
            weights[i] += weights[i - 1];

        start();
    }


    /**
     * Each thread executes in the run method until the benchmark time is up
     * The main loop chooses a tx. type according to the mix specified in
     * the parameter file and calls the appropriate transaction
     * method to do the job.
     * The stats for the entire run are stored in an DealerStats object
     * which is returned to the DealerAgent via the getResult() method.
     * @see DealerStats
     */
    public void run() {
        int tx_type;
        int delay, endTime;

        getReady();		// Perform inits
        if (start == false)	// If error occured during setup, do not run
            return;

        // Calculate time periods
        benchStartTime = Integer.parseInt(props.getProperty("benchStartTime"));
        rampUp = Integer.parseInt(props.getProperty("rampUp"));
        stdyState = Integer.parseInt(props.getProperty("stdyState"));
        stats.setStdyState(stdyState);
        rampDown = Integer.parseInt(props.getProperty("rampDown"));
        endRampUp = benchStartTime + rampUp;
        endStdyState = endRampUp + stdyState;
        endRampDown = endStdyState + rampDown;
        msBetweenthreadStart=Integer.parseInt(props.getProperty("msBetweenThreadStart"));

        /****
         Debug.println(ident + "rampup end time = " + endRampUp +
         ", stdy endtime = " + endStdyState +
         ", rampdown endtime = " + endRampDown);
         ****/

        // If we haven't reached the benchmark start time, sleep
        delay = benchStartTime - timer.getTime();
        if (delay <= 0) {
            errp.println(ident + "Warning: triggerTime has expired. Need " + (-delay) + " ms more");
        }
        else {
            // Debug.println(ident + "Sleeping for " + delay + "ms");
            try {
                //add a small incremental delay so as to limit the
                // number of concurrent threads hitting server at startup
                delay += id*msBetweenthreadStart;
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
               ie.printStackTrace();
               Thread.currentThread().interrupt();
               return;
            }
            int waketime = timer.getTime();
            if (waketime >= endRampUp ) {
                System.err.println(ident + "\nWarning: rampTime expired. Please increase rampTime");
                return;

            } else {
                //tony
                //System.out.print("Starting  " + ident + " at " + waketime );
                //System.out.println("   EndRampUp is at  " + endRampUp );
                //tony
            }
        }

        inRamp = true;
        // Loop until time is up
        while (true) {
            //Compute cycle time for this tx
            timeForThisTx = getFromDistribution(timePerTx, timePerTx * 5);
            tx_type = doMenu();
            switch(tx_type) {
                case DealerStats.PURCHASE:     doPurchase();
                    break;
                case DealerStats.MANAGE:	   doManage();
                    break;
                case DealerStats.BROWSE:       doBrowse();
                    break;
                default:
                    errp.println(ident + "Internal error. Tx-type = " + tx_type);
                    return;
            }
            endTime = timer.getTime();
            // Debug.println(ident + "endTime = " + endTime);
            if (endTime >= endRampUp && endTime < endStdyState)
                inRamp = false;
            else
                inRamp = true;
            if (endTime >= endRampDown)
                break;
        }
        // End of run, destroy bean
        Debug.println(ident + "End of run, logging users out cleanly");
        try {
            //Do logout of thread cleanly
            if (dealer != null)
                dealer.logout();
            else
                Debug.println(ident + "error logging out dealer proxy is null ");
        } catch (Exception e) {
            PlannedLine.send(e, "logout");

            e.printStackTrace();
            errp.println(ident + " Error in logging out " + e);
        }

        // Now sleep forever. We can't exit, as if we do, the thread
        // will be destroyed and the DealerAgent won't be able to
        // retrieve our stats.
        while ( !statsDone) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ie) {
               ie.printStackTrace();
               Thread.currentThread().interrupt();
               return;
            }
        }
        Debug.println(ident + " Exiting...");
    }

    /**
     * Return result of running DealerEntry.
     * @return serializable form of DealerStats
     * @see DealerStats
     */
    public Serializable getResult() {
        // Debug.println(ident + "Returning stats");
        statsDone = true;
        return(stats);
    }

    /**
     * Returns the current or intermediate results
     * from running DealerEntry.
     * @return The intermediate results
     */
    public Serializable getCurrentResult() {
        return(stats);
    }


    /**
     * This method is called from configure to open and read the
     * parameter file and set up instance variables from it. It also
     * create an error log in the run directory.
     */
    private void getReady() {
        File runDirFile = null;
        try {
            runDirFile = new File(resultsDir);
            if ( !runDirFile.exists()) {
                if ( !runDirFile.mkdir()) {
                    throw new RuntimeException("Could not create the new Run Directory: " + resultsDir);
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
            throw new RuntimeException("Could not create the new Run Directory: " + resultsDir + e);
        }

        // Get our thread name and append it to the Agent name to
        // uniquely identify ourselves
        System.out.println("DealerAgent " + name + ", Thread " + id + " started");


        // Get some properties
        numThreads = Integer.parseInt(props.getProperty("threadsPerAgent"));
        txRatePerAgent = Integer.parseInt(props.getProperty("txRatePerAgent"));
        txRate = Integer.parseInt(props.getProperty("txRate"));
        url = props.getProperty("Url");  //url for the main web page

        hostServer=props.getProperty("hostServer");
        port=Integer.parseInt(props.getProperty("port"));
        queryString=props.getProperty("queryString");

        this.c = new Connection(hostServer, port, timer);

        stats = new DealerStats(numThreads, resultsDir, txRate);
        long seed = timer.getTime() + this.hashCode();
        r = new RandNum(seed);	    // Seed random number generator

        numItems = txRate * 100;
        rp = new RandPart(r, numItems, 1);

        custDBSize = 7500 * txRate;

        // set the mean for the cycletime
        timePerTx = 10000;

        // category calculation : the DB loader creates a new category for every 200 items
        // so we assign a random category to the user thread between  0 and the number of categories
        // that the db loader has created.

        int cats = numItems/200;               // calculate the number of categories
        if (cats < 1) cats = 1;                //ensure at least one category

        if (cats > 1)
            category = r.random(0, cats-1);   // assign a random category to this user thread

        //setup the item range within selected category, so we can purchase vehicles/items
        // only in this category
        startitem = category*200 + 1;
        enditem = Math.min (numItems, (category + 1)*200 );
    }

    /**
     * This method selects a given tx. from the specified mix using the
     * weighted distributed algorithm
     * @return The selection
     */
    private int doMenu() {
        int val = r.random(1, totalWeight);

        for (int i = 0; i < DealerStats.TXTYPES; i++)
            if (val <= weights[i])
                return txMenu[i];

        return -1; // error !!!
    }



    /**
     * Use a negative exponential distribution with specified mean and max.
     * We truncate the distribution at 5 times the mean for cycle times.
     * @param mean time
     * @param max time
     * @return time to use for this transaction
     */
    private int getFromDistribution(int mean, int max) {
        if (mean <= 0)
            return(0);
        else {
            double x = r.drandom(0.0, 1.0);
            if (x == 0)
                x = 0.05;
            int delay = (int)(mean * (-Math.log(x)));
            if (delay > max)
                delay = max;
            return(delay);
        }
    }

    /*
    * Purchase vehicles by adding them to the web tier shopping cart.
    * Once the orders have been all placed into the cart the cart is either checked
    * out or the cart contents are dropped and then checked out.
    *
    * @param none
    * @return none
    */
    private void doPurchase() {
        int olCnt, totalQty = 0;
        String cidstr = null;
        int startTime=0;
        int endTime, txnTime, thinkTime, elapsedTime;
        int calcTime, cycleTime;
        int resptime = 0, logtime1 =0 , logtime2 = 0;
        int lrg;
        int rmCnt = 0;
        boolean fail = false;
        boolean badCredit = false;
        boolean cartClear = false;
        String checktype=null;

        calcTime = timer.getTime(); //  keep track of time to do computations

        olCnt = r.random(1, 5);
        ItemQuantity itms[] = new ItemQuantity[olCnt];
        String itmIds[] = new String[olCnt];

        // Select a small order 90% of the time
        lrg = r.random(1, 100);
        if (lrg <= 90)
            totalQty = r.random(10, 20);
        else
            totalQty = r.random(100, 200);

        int q = totalQty/olCnt;
        int rem = totalQty - (q*olCnt);
        for (int i = 0; i < olCnt; i++) {
            boolean done = false;
            while ( !done) {
                itmIds[i] = rp.getPart(startitem, enditem);
                int l;
                for (l = 0; l < i; l++) {
                    if (itmIds[i].equals(itmIds[l]))
                        break;
                }
                if (l == i)
                    done = true;
            }
        }

        Arrays.sort(itmIds); //sort items to avoid deadlock
        for (int i = 0; i < olCnt; i++) {
            itms[i] = new ItemQuantity(itmIds[i], q, new BigDecimal(0.0));
        }
        // Add left-over qty to last item
        itms[olCnt-1].itemQuantity += rem;

        // Choose a random customer, using uniform distribution
        cid = r.random(1, custDBSize);
        cidstr = new Integer(cid).toString();

        // Set the checkout type: if this is a large order then on checkout
        // it is immediately purchased and added to the dealers stock
        // if it is not a large order then approximately 50% of the time
        // the checkout type will be defer and this results in an open
        // order for the dealer against the manufacturer
        int y = r.random(1, 100);
        if (y<=50 || lrg>90 ) {
            checktype="purchase";
        } else
            checktype="defer";


        // Setup the http proxy object then start purchasing vehicles
        // 50% of time the cart is checked out as soon as the items/vehicles
        // have been added.
        // In the other 50% of the time prior to purchasing the cart is
        // either cancelled(80%) then re entered  or
        // (20%) some vehicles are removed from it.
        startTime = timer.getTime();
        try {

//           Sender sender = new Sender();

            // setup the login and the http proxy then add vehicles to the cart
            logtime1 = loginCtl(cidstr, "doPurchaseVehicles-1");
            resptime += logtime1;
            if (logtime1 > 0)
            {
//               sender.append("ResponseTime.Purchase.loginReq", logtime1);
//               sender.append("ResponseTime.Purchase.loginReq", logtime1);
            }

            dealer.addVehiclesToCart(itms, category);
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.Purchase.addVehiclesToCartReq", dealer.getRespTime());

            // 50% of time checkout the entire cart
            int x = r.random(1, 100);
            if ( x <= 50) {
                dealer.checkOut(checktype);

                resptime += dealer.getRespTime();
//                sender.sender.append("ResponseTime.Purchase.checkOutReq", dealer.getRespTime());

            } else {
                // perform cart operations to generate web-tier work
                // 80% of time clear cart, then refill and checkout
                // 20% remove some vehicles from cart then checkout
                x = r.random(1, 100);
                if ( x <= 80) {
                    dealer.clearCart();
                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.Purchase.clearCartReq", dealer.getRespTime());

                    cartClear = true;
                    dealer.addVehiclesToCart(itms, category);

                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.Purchase.addVehiclesToCartReq", dealer.getRespTime());

                    dealer.checkOut(checktype);

                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.Purchase.checkOutReq", dealer.getRespTime());

                } else {
                    // remove all but one of the rows from the cart, one row
                    // at a time leaving just one orderline to be ordered
                    rmCnt = olCnt;
                    rmCnt = (rmCnt== 0) ? 1: rmCnt;
                    for (int i=0; i<rmCnt-1; i++) {
                        dealer.removeVehiclesFromCart();

                        resptime += dealer.getRespTime();
//                        sender.append("ResponseTime.Purchase.removeVehiclesFromCartReq", dealer.getRespTime());
                    }

                    //need to adjust the totalqty to represent actual number of cars purchased
                    totalQty=itms[olCnt-1].itemQuantity;

                    //Debug.println(ident + "DealerEntry: Number of vehicles to remove is " + rmCnt);

                    dealer.checkOut(checktype);
                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.Purchase.checkOutReq", dealer.getRespTime());

                }
            }

            dealer.goHome();
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.Purchase.goHomeReq", dealer.getRespTime());
            logtime2 = loginCtl(cidstr, "doPurchaseVehicles-2");
            resptime += logtime2;
            if (logtime2 > 0)
            {
//               sender.append("ResponseTime.Purchase.loginReq", logtime2);
            }
//            sender.send();

        } catch  (SJASHttpInsufficientCreditException ie) {
            PlannedLine.send(ie, "Purchase");

            badCredit = true;
            //errp.println(ident + "Insufficient credit exception for  " +  cid);
            Debug.println(ident + "Got insufficient credit exception for " + cid );
            loggedIn = false;
        } catch (Exception e) {
            PlannedLine.send(e, "Purchase");

            errp.println(ident + "Error occured in purchaseVehicles for cid " +  cid);
            errp.println("        Number of orderlines = " + olCnt);
            for (int i = 0; i < olCnt; i++) {
                errp.println("        itemId = " + itms[i].itemId +
                        "itemQuantity = " + itms[i].itemQuantity + " for category " + category);
            }
            errp.println("       " + e);
            e.printStackTrace(errp);
            fail = true;
            loggedIn = false;
        }

        endTime = timer.getTime();

        // Compute think time to use
        calcTime = startTime - calcTime;   // time taken in driver
        txnTime = endTime - startTime;    // elapsed time for txn
        //Debug.println(ident + "purchaseVehicles resptime = " + respTime + ", calcTime = " + calcTime);

        cycleTime = txnTime + calcTime;
        thinkTime = timeForThisTx - txnTime - calcTime;

        //Debug.println(ident + "purchaseVehicles thinkTime = " + thinkTime);
        if (thinkTime > 0) {
            cycleTime += thinkTime;
            try{
                Thread.sleep(thinkTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        // Store elapsed time info for thruput
        elapsedTime = endTime - benchStartTime;
        if ( !fail) {
            stats.updateThruput(DealerStats.PURCHASE, elapsedTime);
            StatisticBuilder2.send(mTxTypes[DealerStats.PURCHASE], resptime);

            // Post all stats if in stdy-state
            if ( !inRamp && endTime <= endStdyState) {
                if (lrg > 90) {
                    stats.purcLrgCnt++;
                    stats.purcLrgOlCnt += totalQty;
                }
                if (checktype.equals("purchase"))
                    stats.purcCheckCnt++;
                else
                    stats.purcDeferCnt++;

                if (cartClear == true ) {
                    stats.purcClearCnt++;
                }

                if (rmCnt > 0) {
                    stats.purcRemoveCnt += rmCnt;
                }

                // increment login counters if login was done
                if (logtime1 > 0)
                    stats.loginCnt++;
                if (logtime2  > 0 )
                    stats.loginCnt++;

                stats.purcOlCnt += totalQty;
                if (badCredit)
                    stats.purcBadCredit++;
                stats.update(DealerStats.PURCHASE, resptime, timeForThisTx, cycleTime);
            }
        }
    }


    /*
    * allow the dealer to manage the inventory in the dealerships lot
    * done by selling vehicles and cancelling open orders.
    * The dealership inventory will initially be called to figure out how many
    * open orders (if any) there are and how many vehicles are in the lot to be
    * sold.
    * @param none
    * @return none
    */
    private void doManage() {
        int startTime, endTime, txnTime, thinkTime, elapsedTime;
        int resptime = 0, logtime1 =0 , logtime2 = 0;
        boolean fail = false;
        boolean appErr = false; //sold already sold car or cancel cancelled order
        int cycleTime;
        int sold = 0;           //cars sold
        int cancelCnt=0;

//        Sender sender = new Sender();

        cid = r.random(1, custDBSize);
        String cidstr = new Integer(cid).toString();
        startTime = timer.getTime();
        try {
            Debug.println(ident + "doManageInventory " + cid );
            logtime1 += loginCtl(cidstr,"doManageInventory");
            resptime += logtime1;
            if (logtime1 > 0)
            {
//               sender.append("ResponseTime.loginReq", logtime1);
            }

            dealer.dealershipInventory();
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.dealershipInventoryReq", dealer.getRespTime());

            ArrayList openo  = dealer.getOpenOrders();    // don't add to resp time, there is no call to server
            if (openo.size() >  0 ) {
                Debug.println(ident + "cancel open orders for " + cid + " #open:" + openo.size() );

                // open orders exist, cancel some so that just 5 are left
                // if there are < 5 found then cancel 1
                int openCnt = openo.size();
                if(openCnt >= 5)
                    cancelCnt=openCnt-5;
                else if (openCnt > 0)
                    cancelCnt = 1;

                for (int i=0; i< cancelCnt ; i++ ) {
                    dealer.cancelOpenOrder(openo.get(i).toString() );

                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.cancelOpenOrderReq", dealer.getRespTime());
                }

            } else
                Debug.println(ident + "No open orders found for customer " + cid );

            // sell down to 10 vehicles in the lot
            // Vehicles/qtys are obtained from the dealershipInventory web page
            dealer.dealershipInventory();
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.dealershipInventoryReq", dealer.getRespTime());

            Debug.println(ident + "selling vehicles for " + cid );
            ArrayList vl = dealer.getVehiclesForSale();

            int totalvehicles = 0;
            if (vl.size() > 0 ) {
                Debug.println(ident + "Selling Vehicles for " + cid );

                // determine total vehicles available for sale by
                // this dealer
                for (int i = 0; i < vl.size(); i++ ) {
                    totalvehicles += ((ItemQuantity) vl.get(i)).itemQuantity;
                }

                //attempt to sell down to the last 10 vehicles on the lot
                // as long as there are vehicles to sell always try to sell at least one
                int sellCnt = (totalvehicles <= 10) ? 0 : (totalvehicles-10);
                Debug.println(ident + "Selling " + sellCnt + " vehicles " );
                for (int i=0; i < vl.size(); i++ ) {

                    //sell vehicles one type at a time
                    Debug.println(ident + "Selling " + ((ItemQuantity) vl.get(i)).itemQuantity + " of " + ((ItemQuantity) vl.get(i)).itemId );
                    dealer.sellVehiclesFromInventory( ((ItemQuantity) vl.get(i)).itemId, ((ItemQuantity) vl.get(i)).itemQuantity, ((ItemQuantity) vl.get(i)).itemTotal );

                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.sellVehiclesFromInventoryReq", dealer.getRespTime());

                    sold += ((ItemQuantity) vl.get(i)).itemQuantity;
                    if (sold >=sellCnt)
                        break;
                }

            }

            //return to the home screen
            dealer.goHome();
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.goHomeReq", dealer.getRespTime());

            logtime2 += loginCtl(cidstr, "doManageInventory");
            resptime += logtime2;
            if (logtime2 > 0)
            {
//               sender.append("ResponseTime.loginReq", logtime2);
            }
//            sender.send();
        } catch (SJASHttpAppException ae) {
            PlannedLine.send(ae, "Manage");

            errp.println(ident + "Application error for " +  cid + "error is " + ae);
            Debug.println(ident + "Application error for " +  cid + "error is " + ae);
            loggedIn=false;
            appErr = true;

        } catch (Exception e) {
            PlannedLine.send(e, "Manage");

            e.printStackTrace();
            errp.println(ident + "Error occured during manageInventory for customer " + cid);
            errp.println("        " + e);
            fail = true;
            loggedIn=false;
        }

        endTime = timer.getTime();

        // Compute think time to use
        txnTime = endTime - startTime;
        cycleTime = txnTime;
        thinkTime = timeForThisTx - txnTime;
        if (thinkTime > 0) {
            cycleTime += thinkTime;
            try {
                Thread.sleep(thinkTime);
            } catch (InterruptedException ie) { 
                Thread.currentThread().interrupt();
                return;
            }
        }
        // Store elapsed time info for thruput
        elapsedTime = endTime - benchStartTime;

        if ( ! fail) {
            stats.updateThruput(DealerStats.MANAGE, elapsedTime);
            StatisticBuilder2.send(mTxTypes[DealerStats.MANAGE], resptime);

            // Post all stats if in stdy-state
            if ( !inRamp && endTime <= endStdyState) {
                if (appErr)
                    stats.manageAppErrCnt++;

                // increment login counters if login was done
                if (logtime1 > 0)
                    stats.loginCnt++;
                if (logtime2  > 0 )
                    stats.loginCnt++;

                stats.manageCancelCnt += cancelCnt;
                stats.manageSoldCnt += sold;
                stats.update(DealerStats.MANAGE, resptime, timeForThisTx, cycleTime);
            }
        }
    }


    /*
    * browse items/vehicles in the list
    */

    private void doBrowse() {
        int startTime, endTime, txnTime, thinkTime, elapsedTime;
        boolean fail = false;
        int resptime = 0, logtime1 = 0, logtime2 = 0;
        int cycleTime;

        cid = r.random(1, custDBSize);
        String cidstr = new Integer(cid).toString();
        int fwdCnt=0, bkwdCnt =0;

        startTime = timer.getTime();
        try {

//           Sender sender = new Sender();

           //Debug.println(ident + "doBrowseVehicles" + cid );

            //create the http dealer proxy
            logtime1 += loginCtl(cidstr, "doBrowseVehicles");
            resptime += logtime1;
//            sender.append("ResponseTime.loginBrowseReq", logtime1);

            dealer.browseVehicles("top", category);
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.browseVehiclesReq", dealer.getRespTime());

            // 10 is a magic number as this is the number of ItemEnt references
            // cached  by the ItemSes bean
            for (int i=0; i < 10; i++ ) {

                dealer.browseVehicles("fwd", category);
                resptime += dealer.getRespTime();
//                sender.append("ResponseTime.browseVehiclesFwdReq", dealer.getRespTime());

                fwdCnt++;
                if (i % 3 == 2 ) {
                    dealer.browseVehicles("bkwd", category);
                    resptime += dealer.getRespTime();
//                    sender.append("ResponseTime.browseVehiclesBkwdReq", dealer.getRespTime());

                    bkwdCnt++;
                }

            }
//            sender.append("ResponseTime.browseVehiclesFwdReq-Fwd", fwdCnt);
//            sender.append("ResponseTime.browseVehiclesReq-Bkwd", bkwdCnt);

            // return to home screen
            dealer.goHome();
            resptime += dealer.getRespTime();
//            sender.append("ResponseTime.goHomeBrowseReq", dealer.getRespTime());

            logtime2 += loginCtl(cidstr, "doBrowseVehicles");
            resptime += logtime2;
//            sender.append("ResponseTime.loginBrowseReq", logtime2);
//            sender.send();

        } catch (Exception e) {
            PlannedLine.send(e, "Browse");
            
            errp.println(ident + "Error occured during BrowseVehicles  " + cid);
            errp.println("        " + e);
            e.printStackTrace();
            fail = true;
            loggedIn=false;
        }

        endTime = timer.getTime();

        // Compute think time to use
        txnTime = endTime - startTime;
        cycleTime = txnTime;
        thinkTime = timeForThisTx - txnTime;
        if (thinkTime > 0) {
            cycleTime += thinkTime;
            try {
                Thread.sleep(thinkTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        // Store elapsed time info for thruput
        elapsedTime = endTime - benchStartTime;
        if ( !fail) {
            stats.updateThruput(DealerStats.BROWSE, elapsedTime);
            StatisticBuilder2.send(mTxTypes[DealerStats.BROWSE], resptime);

            // Post all stats if in stdy-state
            if ( !inRamp && endTime <= endStdyState) {

                stats.browseFwdCnt += fwdCnt;
                stats.browseBkwdCnt += bkwdCnt;

                // increment login counters if login was done
                if (logtime1 > 0)
                    stats.loginCnt++;
                if (logtime2  > 0 )
                    stats.loginCnt++;

                stats.update(DealerStats.BROWSE, resptime, timeForThisTx, cycleTime);
            }
        }
    }

    /*
    * this method controls the thread's logging in or logging out of the
    * application
    *
    * @param cidstr the customer id used to login to the web app
    * @param caller with method in DealerEntry is causing the login our logout
    *        this is only important for debugging.
    *
    * @return int: the response time for the call to the server, this will be zero if
    *              the user thread wasn't logged in or out.
    */
    private int loginCtl(String cidstr, String caller ) throws SJASHttpException {
        int logtime = 0;

        if (!loggedIn) {
            //set the logout time for this thread and login

            // do login
            logtime = logIn(cidstr);
            loggedIn=true;
            Debug.println(ident + "logged in: called from:  " + caller  );

            //set time for next logout
            logoutTime = getLogoutTime();

        } else if (timer.getTime() >= logoutTime ) {
            //logout time reached so logout then log back in again

            //logout
            logtime += logOut();
            loggedIn=false;
            Debug.println(ident + "logged out called from " + caller);

            //log back in
            logtime += logIn(cidstr);
            loggedIn=true;
            logoutTime = getLogoutTime();

        }
        return logtime;

    }

    /**
     * we schedule each thread to logout approximately twice during steady state
     * this is done each time the thread (user) logs in and is achieved
     * by selecting a random logout time in secs between 0 and the steady state
     * for the run. The mean of this distribution is therefore steadystate/2.
     *
     * @return the millisecs until next thread logout
     **/
    private int getLogoutTime() {
        int currentTime = timer.getTime();
        int lot = r.random(0, stdyState/1000);
        //System.out.println(ident + "   logoutsecs = " + lot );
        if (currentTime >= endRampUp)
            lot = (lot * 1000) + currentTime;
        else
            lot = (lot * 1000) + endRampUp;
        //System.out.println("   logout time is  " + lot );
        return lot;
    }

    /**
     * This method logs into the web tier. It creates a new http proxy
     * object and opens a new connection to the web tier.
     * @param cidstr the customer id used to login to the web app
     * @return The response time for the call to the server
     * @throws SJASHttpException An http exception occurred
     **/
    private int logIn(String cidstr) throws SJASHttpException {
        c.connect();
        dealer = new HttpDealer(props , cidstr, ident, c);
        dealer.login();
        return dealer.getRespTime();
    }

    /**
     * Logs out from the application and close the connection to the web
     * server.
     * @return The response time for the call to the server
     * @exception SJASHttpException An http error occurred
     **/
    private int logOut() throws SJASHttpException {

        dealer.logout();
        c.close("reached user logout time");
        return dealer.getRespTime();

    }

    public void executeDealerHTTPRequests(int customerId) throws SJASHttpException {
        props = System.getProperties();
        
        hostServer = System.getProperty("hostname");
        port = Integer.parseInt(System.getProperty("port"));
        queryString = System.getProperty("queryString");
        timer = new Timer();

        c = new Connection(hostServer, port, timer);

        category = 0;
        startitem = category*200 + 1;
        enditem = (category + 1)*200;

        r = new RandNum();
        txRate = 1;
        numItems = txRate * 100;
        rp = new RandPart(r, numItems, 1);
        
        int olCnt = 1;
        ItemQuantity itms[] = new ItemQuantity[olCnt];
        String itmIds[] = new String[olCnt];
        for (int i = 0; i < olCnt; i++) {
            boolean done = false;
            while ( !done) {
                itmIds[i] = rp.getPart(startitem, enditem);
                int l;
                for (l = 0; l < i; l++) {
                    if (itmIds[i].equals(itmIds[l]))
                        break;
                }
                if (l == i)
                    done = true;
            }
        }
        Arrays.sort(itmIds); //sort items to avoid deadlock

        int q = 1;
        for (int i = 0; i < olCnt; i++) {
            itms[i] = new ItemQuantity(itmIds[i], q, new BigDecimal(0.0));
        }

        // login
        EprofEventHandler.enableProfiling(true);
        logIn("" + customerId);

        // doPurchase
        dealer.addVehiclesToCart(itms, category);
        dealer.checkOut("defer");  // could throw SJASHttpInsufficientCreditException
        EprofEventHandler.enableProfiling(false);
        dealer.addVehiclesToCart(itms, category); // normal order
        EprofEventHandler.enableProfiling(true);
        dealer.checkOut("purchase");  // could throw SJASHttpInsufficientCreditException
        // doPurchase: 2x large order (for RMI and web services)
        itms[olCnt-1].itemQuantity += 20;
        EprofEventHandler.enableProfiling(false);
        dealer.addVehiclesToCart(itms, category); // first large order
        EprofEventHandler.enableProfiling(true);
        dealer.checkOut("purchase");  // could throw SJASHttpInsufficientCreditException
        EprofEventHandler.enableProfiling(false);
        dealer.addVehiclesToCart(itms, category); // second large order (needed for RMI requests)
        dealer.checkOut("purchase");  // could throw SJASHttpInsufficientCreditException
        dealer.addVehiclesToCart(itms, category);
        EprofEventHandler.enableProfiling(true);
        dealer.removeVehiclesFromCart();
        EprofEventHandler.enableProfiling(false);
        dealer.addVehiclesToCart(itms, category);
        EprofEventHandler.enableProfiling(true);
        dealer.clearCart();
        EprofEventHandler.enableProfiling(false);

        // doBrowse
        dealer.browseVehicles("top", category);
        EprofEventHandler.enableProfiling(true);
        dealer.browseVehicles("fwd", category);
        dealer.browseVehicles("bkwd", category);

        // doManage
        dealer.dealershipInventory();
        ArrayList openo = dealer.getOpenOrders();
        ArrayList vl = dealer.getVehiclesForSale();
        for (int i = 0; i < openo.size(); i++) { // added by checkOut("defer")
//            System.out.println("Cancel open order for " + cid + " #open:" + openo.size());
            if (i == 0) {
                EprofEventHandler.enableProfiling(true);
            }
            dealer.cancelOpenOrder(openo.get(i).toString());
            if (i == 0) {
                EprofEventHandler.enableProfiling(false);
            }
        }
        for (int i = 0; i < vl.size(); i++) { // added by checkOut("purchase")
//            System.out.println("Sell vehicels for " + cid + " #sell:" + vl.size());
            if (i == 0) {
                EprofEventHandler.enableProfiling(true);
            }
            ItemQuantity iq = (ItemQuantity) vl.get(i);
            dealer.sellVehiclesFromInventory(iq.itemId, iq.itemQuantity, iq.itemTotal);
            if (i == 0) {
                EprofEventHandler.enableProfiling(false);
            }
        }

        // logout
        dealer.goHome();
        logOut();
        EprofEventHandler.enableProfiling(false);
    }
    
    public DealerEntry() {}    
}
