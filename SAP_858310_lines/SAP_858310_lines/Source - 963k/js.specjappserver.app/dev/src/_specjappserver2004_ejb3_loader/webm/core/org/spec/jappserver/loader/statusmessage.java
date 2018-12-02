/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * JMS message flow:
 * threads of LoaderMDB which are loading table sections -> controller thread of LoaderMDB
 *                                                          controller thread of LoaderMDB -> LoaderServlet
 *
 * It describes the status of a (jms) thread which loads a table section
 * or the status of the controller thread of LoaderMDB.
 */
public class StatusMessage extends LoadMessage {

    private static final long serialVersionUID = -2973134010818283334L;

    /* loading database started */
    static final String STATUS_STARTING = "starting";
    /* try stopping loading database */
    static final String STATUS_STOPPING = "stopping";
    /* loading database has ended (successfull or not), no thread is running */
    static final String STATUS_ENDED = "ended";

    /* deleting a table */
    static final String STATUS_DELETING = "deleting";
    /* deleting a table */
    static final String STATUS_DELETING_FINISHED = "deleting finished";

    /* a thread has been started */
    static final String STATUS_STARTED = "started";
    /* a thread has finished successfull */
    static final String STATUS_SUCCESSFULLY_FINISHED = "successfully finished";
    /* a thread has failed */
    static final String STATUS_FAILED = "failed";
    /* a thread was stopped successfully */
    static final String STATUS_STOPPED = "stopped";

    private String text;
    private String status;
    private Throwable throwable;
    private long timestamp;

    public StatusMessage(String text, String status) {
        this.text = text;
        this.status = status;
        timestamp = System.currentTimeMillis();
    }

    public StatusMessage(ActionMessage loadMessage, String text, String status) {
        setDomain(loadMessage.getDomain());
        setTables(loadMessage.getTables());
        setThread(loadMessage.getThread());
        this.text = text;
        this.status = status;
        throwable = null;
    }

    public Throwable getThrowable() {
        return throwable;
    }
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getTimestampString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd|HH-mm-ss");
        return sdf.format(new Date(timestamp));
    }

    @Override
    public String getValueStr() {
        StringBuffer str = new StringBuffer();
        if (text != null) {
            str.append(text);
        }
        str.append(super.getValueStr());
        append(str, "status=");
        str.append(status);
        if (throwable != null) {
            append(str, "throwable=\n   ");
            str.append(throwable.toString());
            for (StackTraceElement ste : throwable.getStackTrace()) {
                str.append("\n      at ");
                str.append(ste.toString());
            }
        }
        return str.toString();
    }
    
    @Override
    public String toString() {
        return "[StatusMessage " + getValueStr() + "]";
    }
}
