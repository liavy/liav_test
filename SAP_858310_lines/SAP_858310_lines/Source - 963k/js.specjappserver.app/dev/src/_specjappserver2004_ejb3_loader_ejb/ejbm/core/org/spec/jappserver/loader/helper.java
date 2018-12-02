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

import java.sql.Date;
import java.sql.Timestamp;

import org.spec.jappserver.common.util.DateTimeNormalizer;

/*
 * Provides a few constants, caluclation and string helper methods.
 */
public class Helper {

    static final Date NOW_DATE = new Date(DateTimeNormalizer.normalizeSqlDateMillis(System.currentTimeMillis()));
    static final Timestamp NOW_TIMESTAMP = new java.sql.Timestamp(System.currentTimeMillis());

    static final int NUM_SITES = 1;
    static final int NUM_SUPPLIERS = 10;

    private static final int CUSTOMER_FACTOR = 7500;
    private static final int ASSEMBLY_FACTOR = 100; // = ITEM_FACTOR
    private static final int ORDER_FACTOR = 750;
    
    private static final int NULLS_SIZE = 10;
    private static final String[] NULLS;

    static int getNumCustomers(int scale) {
        return CUSTOMER_FACTOR * scale;
    }

    static int getNumAssemblies(int scale) {
        return ASSEMBLY_FACTOR * scale;
    }

    static int getNumOrders(int scale) {
        return ORDER_FACTOR * scale;
    }

    static int roundedNumThreads(int quantity, int dividor) {
        if (dividor <= 0)
            throw new RuntimeException("NumThreads can't be 0 or negative!");
        for (int i = dividor; i > 0; i--) {
            int remainder = quantity % i;
            if (remainder == 0)
                return i;
        }
        return dividor;
    }

    static int getNumPerThread(int quantity, int dividor) {
        return quantity/roundedNumThreads(quantity, dividor);
    }
    
    static {
        NULLS = new String[NULLS_SIZE];
        NULLS[0] = "";
        for (int i = 1; i < NULLS_SIZE; i++)
        {
            NULLS[i] = NULLS[i-1] + "0";
        }
    }

    static String toXDigitString(int i, int x) {
        String str = Integer.toString(i);
        int additionalNulls = x - str.length();
        if (additionalNulls <= 0 || additionalNulls >= NULLS_SIZE)
        {
            return str;
        }
        return NULLS[additionalNulls] + str;
    }
}
