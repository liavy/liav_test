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

import java.io.Serializable;

/*
 *  Super class of all database loading messages
 */ 
public class LoadMessage implements Serializable {

    private static final long serialVersionUID = -6131524394905701566L;

    static final String CORPORATE_DOMAIN = "corporate";
    static final String ORDER_DOMAIN = "order";
    static final String MANUFACTURER_DOMAIN = "manufacturer";
    static final String SUPPLIER_DOMAIN = "supplier";

    private String domain = null;
    private String[] tables = null;
    private int thread = -1;

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String[] getTables() {
        return tables;
    }
    public void setTables(String[] table) {
        this.tables = table;
    }
    public int getThread() {
        return thread;
    }
    public void setThread(int thread) {
        this.thread = thread;
    }

    void append(StringBuffer str, String value) {
        int length = str != null ? str.length() : 0;
        if (length > 0 && str.charAt(length - 1) != ' ') {
            str.append(", ");
        }
        str.append(value);
    }
    
    public String getValueStr() {
        StringBuffer str = new StringBuffer();
        if (getDomain() != null) {
            append(str, "domain=");
            str.append(getDomain());
        }
        String tableList = MessageHelper.getCommaSeperatedList(getTables());
        if (tableList != null) {
            append(str, "table(s)=");
            str.append(tableList);
        }
        if (getThread() >= 0) {
            append(str, "thread=");
            str.append(getThread());
        }
        return str.toString();
    }

    @Override
    public String toString() {
        return "[LoadMessage " + getValueStr() + "]";
    }
}
