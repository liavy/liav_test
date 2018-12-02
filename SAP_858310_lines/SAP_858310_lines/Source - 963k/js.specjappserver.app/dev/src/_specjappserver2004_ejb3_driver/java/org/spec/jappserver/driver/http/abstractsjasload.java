/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID        Company      Description
 *  ----------    --------  -------      -------------
 *  Jan , 2003    Tom Daly  Sun          Creation date
 *  Jan , 2004    Tom Daly  Sun          renamed vars to reflect 2004 name
 *
 */

package org.spec.jappserver.driver.http;
import java.io.Serializable;

import org.spec.jappserver.driver.event.EprofEventHandler;

public abstract class AbstractSJASLoad
implements Serializable {

    /** Variables that will/can be used by subclasses **/
    protected HttpResponseData response;
    protected Connection c;
    protected String genErr = "SPECjAppServer2004 Error";
    protected String creditErr = "Insufficient Credit Exception";


    public AbstractSJASLoad(Connection c) {

        this.c = c;
    }


    public AbstractSJASLoad() {

    }

    public HttpResponseData executeAction(int actionType, HttpRequestData req)
    throws SJASHttpException {

        try {
            HttpAction action = HttpActionFactory.getInstance(
            actionType);
            EprofEventHandler.startProfiling(req);
            HttpResponseData result = action.execute(req, c);
            EprofEventHandler.endProfiling();
            return result;
        }catch(Exception io) {
            throw new SJASHttpException(io.getMessage());
        }
    }

    public int getTxnRespTime() {
        return c.getTxnRespTime();

    }

    /**
     * @throws SJASHttpException
     */
    public void checkForError() throws SJASHttpException {

        HttpResponseParser rp = response.getData();
        int err = 0;
        try {
            err = rp.grepForError(genErr, 0);
            if (err!=0) {
                debugPrint("AbstractSJASLoad> Got an error checking html return" + rp.toString());
                throw new SJASHttpException("error while checking html" + rp.toString());
            }

        }catch(Exception e) {
           e.printStackTrace();
            // for debugging ensure the error is shown
           debugPrint("Error in html " + e  + rp.toString());
            throw new SJASHttpException(e.getMessage());

        }
    }

    /**
     * examine html response and check for credit exception
     * @throws SJASHttpInsufficientCreditException
     * @throws SJASHttpException
     */
    public void checkForCreditError()
    throws SJASHttpInsufficientCreditException, SJASHttpException {

        HttpResponseParser rp = response.getData();
        int crediterr = 0;
        crediterr = rp.grepForError(creditErr, 0);
        if (crediterr!=0) {
            //debugPrint("AbstractSJASLoad>  insufficient credit error");
            throw new SJASHttpInsufficientCreditException("insufficient credit");
        }

    }

    /**
     * Examine html data returned to see if it contains
     * an application error.
     * Application errors can be
     * a) Where a user threads tries to sell a vehicle that has already been sold by another
     *    user thread from the same dealer
     * b) where a user thread tries to cancel an open order that has already
     *    been cancelled by another thread from the same dealership.
     * @param err
     * @throws SJASHttpAppException
     * @throws SJASHttpException
     */
    public void checkForAppError(String err )
    throws SJASHttpAppException , SJASHttpException {
        HttpResponseParser rp = response.getData();
        int apperr = 0;
        apperr = rp.grepForError(err, 0);
        if (apperr!=0) {
            debugPrint("AbstractSJASLoad> Application error " + err);
            throw new SJASHttpAppException();
        }
    }

     /**
     * Examine html data returned to see if it contains the target String s
     * @param String s the string to search for
     * @throws SJASHttpException
     * @returns true: found string, false otherwise
     */
    public boolean checkForString(String s)
    throws SJASHttpException {
        HttpResponseParser rp = response.getData();
        int found = 0;
        found = rp.grepForString(s, 0);
        if (found==1) {
            return true;
        }
        else
            return false;
    }


    public void debugPrint(String s) {
        if(true)
            System.out.println(s);
    }

    public HttpResponseData getResponse() {
        return response;
    }

    public void setResponse(HttpResponseData response) {
        this.response = response;
    }
}
