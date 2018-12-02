/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------------------------------------
 *  2003/01/01  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/04/01  John Stecher, IBM         Modified for Big Decimal
 *  2003/06/28  John Stecher, IBM         Added BigDecimal Constants to ease object use
 *  2003/08/15  Samuel Kounev, Darmstadt  Added final to constant declarations.
 *  2004/03/02  John Stecher, IBM         Updated to fix synchronization issue with Decimal formatter being a static.
 *                                        (see osgjava-6675 and osgjava-6676)
 *  2003/04/02  Russell Raymundo, BEA     Changed to enable placing the SpecUtil object on the session object
 *                                        (usebean on jsp). This addresses synchronization issues while avoiding
 *                                        the numerous creation of SpecUtil objects (osgjava-6804).
 */


package org.spec.jappserver.servlet.helper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
/*	This class is used to implement various utilities needed by the benchmark runtime
 *
 */
public class SpecUtils implements Serializable {

    public static final BigDecimal zeroBigDec = new BigDecimal("0.00");
    public static final BigDecimal oneBigDec = new BigDecimal("1.00");
    public static final BigDecimal onehundredBigDec = new BigDecimal("100.00");

    NumberFormat nf;
    NumberFormat cnf;
    DateFormat ddf;

    public SpecUtils() {
        nf = NumberFormat.getInstance();
        cnf = NumberFormat.getCurrencyInstance();
        ddf = DateFormat.getDateInstance(DateFormat.MEDIUM);

        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    public String formatNumber(float num) {
        return nf.format(num);
    }

    public String formatNumber(BigDecimal num) {
        return nf.format(num.floatValue());
    }

    public String formatCurrency(double num) {
        return cnf.format(num); 
    }

    public String formatCurrency(BigDecimal num) {
        return cnf.format(num.doubleValue());   
    }

    public String formatDate(java.util.Date date) {
        return ddf.format(date);
    }
}
