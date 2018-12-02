/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2002/03/22  Damian Guy, SUN         Created
 *  2002/04/12  Matt Hogstrom, IBM      Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russel Raymundo, BEA    Conversion from SPECjAppServer2001 to 
 *                                      SPECjAppServer2002 (EJB2.0).
 *  2003/01/01  John Stecher, IBM       Modifed for SPECjAppServer2004
 */

package org.spec.jappserver.supplier.helper;


//Import statements

/**
 * Enum class for XML Tags used in Supplier Domain
 *
 *
 * @author Damian Guy
 */
public class XmlTags {

    public static final String XMLVERSION = "<?xml version=\"1.0\"?>";

    //public static final String DELIVERYDOC = "<!DOCTYPE DELIVERY SYSTEM \"http://pellet/delivery.dtd\">";
    public static final String DELIVERYDOC = "<!DOCTYPE DELIVERY SYSTEM ";

    //public static final String PODOC = "<!DOCTYPE PURCHASE-ORDER SYSTEM \"http://pellet/po.dtd\">";
    public static final String PODOC         =
    "<!DOCTYPE PURCHASE-ORDER SYSTEM ";
    public static final String PO            = "PURCHASE-ORDER";
    public static final String POSTART       = "<PURCHASE-ORDER>";
    public static final String POEND         = "</PURCHASE-ORDER>";
    public static final String PONUMBER      = "PO-NUMBER";
    public static final String PONUMBERSTART = "<PO-NUMBER>";
    public static final String PONUMBEREND   = "</PO-NUMBER>";
    public static final String SITE          = "SITE-ID";
    public static final String SITESTART     = "<SITE-ID>";
    public static final String SITEEND       = "</SITE-ID>";
    public static final String NUMLINES      = "NUMLINES";
    public static final String NUMLINESSTART = "<NUMLINES>";
    public static final String NUMLINESEND   = "</NUMLINES>";
    public static final String POLINE        = "POLINE";
    public static final String POLINESTART   = "<POLINE>";
    public static final String POLINEEND     = "</POLINE>";
    public static final String LINENUM       = "LINE-NUMBER";
    public static final String LINENUMSTART  = "<LINE-NUMBER>";
    public static final String LINENUMEND    = "</LINE-NUMBER>";
    public static final String PARTID        = "PART-ID";
    public static final String PARTIDSTART   = "<PART-ID>";
    public static final String PARTIDEND     = "</PART-ID>";
    public static final String QTY           = "QTY";
    public static final String QTYSTART      = "<QTY>";
    public static final String QTYEND        = "</QTY>";
    public static final String BALANCE       = "BALANCE";
    public static final String BALANCESTART  = "<BALANCE>";
    public static final String BALANCEEND    = "</BALANCE>";
    public static final String LEADTIME      = "LEAD-TIME";
    public static final String LEADTIMESTART = "<LEAD-TIME>";
    public static final String LEADTIMEEND   = "</LEAD-TIME>";
    public static final String DELIVERY      = "DELIVERY";
    public static final String DELIVERYSTART = "<DELIVERY>";
    public static final String DELIVERYEND   = "</DELIVERY>";

    /**
     * Constructor XmlTags
     *
     *
     */
    public XmlTags() {
    }
}

