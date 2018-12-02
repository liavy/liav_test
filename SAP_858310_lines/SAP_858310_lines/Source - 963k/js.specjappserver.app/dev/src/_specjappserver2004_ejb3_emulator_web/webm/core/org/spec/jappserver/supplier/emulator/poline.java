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

package org.spec.jappserver.supplier.emulator;


//Import statements
import org.spec.jappserver.supplier.helper.XmlTags;


/**
 * Each instance of this class represents an Orderline
 * from a Purchase Order. It is responsible for delivering
 * the OrderLine to the Supplier Domain.
 *
 * @author Damian Guy
 */
public class POLine {

    private String poNum;      // Purchase ORder that this belongs to.
    private String siteID;    // ID of site that Order should be delivered to
    private String lnum;       // Line number
    private String part;       // ID of component that was ordered.
    private String balance;    // balance due.
    private String qty;        // Qty ordered/delivered
    private int    leadTime;    // Max time that parts must be delivered within.

    /**
     * Create new POLine Object.
     * @param poNum - Purchase ORder that this belongs to.
     * @param siteID - ID of site that Order should be delivered to
     * @param lnum - Line number
     * @param part - ID of component that was ordered.
     * @param balance - balance due.
     * @param qty - Qty ordered/delivered
     * @param leadTime - Max time that parts must be delivered within.
     */
    public POLine(String poNum, String siteID, String lnum, String part,
                  String qty, String balance, int leadTime) {

        this.poNum    = poNum;
        this.siteID  = siteID;
        this.lnum     = lnum;
        this.part     = part;
        this.balance  = balance;
        this.qty      = qty;
        this.leadTime = leadTime;
    }

    /**
     * Method getXml
     *
     *
     * @return
     *
     */
    public String getXml() {

        StringBuffer xml = new StringBuffer(XmlTags.POLINESTART);

        xml.append(XmlTags.LINENUMSTART);
        xml.append(lnum);
        xml.append(XmlTags.LINENUMEND);
        xml.append(XmlTags.PARTIDSTART);
        xml.append(part);
        xml.append(XmlTags.PARTIDEND);
        xml.append(XmlTags.QTYSTART);
        xml.append(qty);
        xml.append(XmlTags.QTYEND);
        xml.append(XmlTags.POLINEEND);

        return xml.toString();
    }

    /**
     * getLeadTime.
     * @return leadTime
     */
    public int getLeadTime() {
        return leadTime;
    }
}

