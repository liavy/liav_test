/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ------------------------  ----------------------------------------------------------
 *  2003/07     Ning Sun, SUN             Created
 *  2003/08     Ning Sun, SUN             Modified to correct bugs in response parsing.
 *  2003/10     Tom Daly, SUN             Added methods to track response times
 *                                        added String parameter to close() to identify where
 *                                        close() is called from.
 *  2004/06     Ning Sun, SUN             Fixed problem with multiple cookie support on SAP WebAS.
 *                                        See osgjava-7221 and osgjava-7228.
 */
package org.spec.jappserver.driver.http;

public class CookieStrings {

    public int num;
    public String[] entries;
    public int MAX_NUM_COOKIES = 10;

    public CookieStrings() {
        entries = new String[MAX_NUM_COOKIES];
        num = 1;
        entries[0] = null;
    }

    public CookieStrings(int max_num) {
        MAX_NUM_COOKIES = max_num;
        entries = new String[max_num];
        num = 1;
        entries[0] = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(Thread.currentThread().getName());
        sb.append(":");
        for (int i = 0; i < num; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append("" + entries[i]);
        }
        return sb.toString();
    }
}

