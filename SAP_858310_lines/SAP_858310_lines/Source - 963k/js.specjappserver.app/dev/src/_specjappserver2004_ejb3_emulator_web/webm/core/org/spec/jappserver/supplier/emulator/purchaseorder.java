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
 *  2003/05/05  Matt Hogstrom, IBM      Added code to support Delivery Servlet Tx Count from Emulator
 */

package org.spec.jappserver.supplier.emulator;


//Import statements
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.spec.jappserver.common.Debug;
import org.spec.jappserver.common.NotReadyException;
import org.spec.jappserver.common.SPECjAppServerException;
import org.spec.jappserver.supplier.helper.NonSecureXmlCommand;
import org.spec.jappserver.supplier.helper.XmlTags;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Each instance of this class represents
 * a Purchase Order. This class is responsible
 * for parsing of XML and creating POLine Objects.
 *
 * @author Damian Guy
 */
public class PurchaseOrder {

    ArrayList lines;
    String    poNumber;
    String    siteID;
    String    numLines;
    String    supplierHost;
    String    supplierServlet;
    int       supplierPort;
    String    deliveryDTD;
    int       maxRetries;
    int       retryInterval;
    static Integer lock = new Integer(0);   // Added to support Delivery Tx Count $Hogstrom 

    boolean   debugging;
    Debug     debug;
    Scheduler scheduler;

    /**
     * create Purchase Order
     * @param xmlDoc - XML Document containing the Purchase Order
     */
    public PurchaseOrder(Document xmlDoc, Map config) {

        this.scheduler       = (Scheduler) config.get("scheduler");
        this.maxRetries      = ((Integer) config.get("maxRetries"))
                               .intValue();
        this.retryInterval   = ((Integer) config.get("retryInterval"))
                               .intValue();
        this.debugging       = ((Boolean) config.get("debugging"))
                               .booleanValue();
        this.debug           = (Debug) config.get("debug");
        this.supplierHost    = (String) config.get("supplierHost");
        this.supplierServlet = (String) config.get("supplierServlet");
        this.supplierPort    = ((Integer) config.get("supplierPort"))
                               .intValue();
        this.deliveryDTD     = (String) config.get("deliveryDTD");
        poNumber             =
        getData(xmlDoc.getElementsByTagName(XmlTags.PONUMBER).item(0));
        siteID              =
        getData(xmlDoc.getElementsByTagName(XmlTags.SITE).item(0));
        numLines             =
        getData(xmlDoc.getElementsByTagName(XmlTags.NUMLINES).item(0));

        NodeList polines = xmlDoc.getElementsByTagName(XmlTags.POLINE);

        lines = new ArrayList();

        for( int i = 0; i < polines.getLength(); i++ ) {
            getLineData(polines.item(i));
        }
    }

    private String getData(Node node) {

        if( node.hasChildNodes() ) {
            Node dataNode = node.getChildNodes().item(0);

            return dataNode.getNodeValue().trim();
        }

        return null;
    }

    private void getLineData(Node line) {

        String   data[];
        NodeList children = line.getChildNodes();

        data = new String[5];

        for( int i = 0; i < data.length; i++ ) {
            data[i] = getData(children.item(i));
        }

        insert(new POLine(poNumber, siteID, data[0], data[1], data[2],
                          data[3], Integer.parseInt(data[4])));
    }

    /**
     * Inserts  POLines into  ArrayList in ascending order
     * based on lead time.
     */
    private void insert(POLine current) {

        int     j     = 0;
        boolean found = false;

        while( (j <= lines.size()) && (found == false) ) {
            Vector v = null;

            if( j == lines.size() ) {
                v = new Vector();

                v.addElement(current);
                lines.add(j, v);

                found = true;
            } else {
                v = (Vector) lines.get(j);

                POLine line = (POLine) v.firstElement();

                if( line.getLeadTime() == current.getLeadTime() ) {
                    v.addElement(current);

                    found = true;
                } else if( line.getLeadTime() > current.getLeadTime() ) {
                    v = new Vector();

                    v.addElement(current);
                    lines.add(j, v);

                    found = true;
                }
                // Otherwise continue searching.
            }

            j++;
        }
    }

    /**
     * processPO - Creates XML to deliver the POLines.
     *
     */
    public void processPO() {

        long refTime = System.currentTimeMillis();

        for( int i = 0; i < lines.size(); i++ ) {
            Vector v = (Vector) lines.get(i);

            // Calculates lead time per delivery.
            long partsLeadTime = (long) ((POLine) v.firstElement())
                                 .getLeadTime();
            long deliveryTime = partsLeadTime * 5000l + refTime;

            scheduler.schedule(deliveryTime, new DeliveryOrder(v));
        }
    }

    class DeliveryOrder implements Runnable {

        Vector poLineVector;
        String xml = null;
        int retries = 0;

        public DeliveryOrder(Vector poLineVector) {
            this.poLineVector = poLineVector;
        }

        public void run() {
            if( xml == null )
                processPOLine();
            deliverGoods();
        }

        public void processPOLine() {

            StringBuffer xml = new StringBuffer(XmlTags.XMLVERSION);
            xml.append(XmlTags.DELIVERYDOC);
            xml.append("\"");
            xml.append(deliveryDTD);
            xml.append("\">");
            xml.append(XmlTags.DELIVERYSTART);
            xml.append(XmlTags.PONUMBERSTART);
            xml.append(poNumber);
            xml.append(XmlTags.PONUMBEREND);
            xml.append(XmlTags.NUMLINESSTART);
            xml.append(poLineVector.size());
            xml.append(XmlTags.NUMLINESEND);

            for( int i = 0; i < poLineVector.size(); i++ ) {
                POLine current = (POLine) poLineVector.elementAt(i);
                xml.append(current.getXml());
            }

            xml.append(XmlTags.DELIVERYEND);
            this.xml = xml.toString();
        }

        private void deliverGoods() {

            NonSecureXmlCommand comm = new NonSecureXmlCommand(supplierHost,
                                                               xml, supplierServlet, supplierPort);
            try {
                comm.execute();
                synchronized(lock) {                  // $Hogstrom, added to support Delivery Tx, begin
                  EmulatorServlet.deliveryCounter++;
                }                                     // $Hogstrom, added to support Delivery Tx, end
            } catch( NotReadyException e ) {
                if( retries < maxRetries ) {
                    if( debugging )
                        debug.println(2, "Not ready, rescheduling...");
                    ++retries;
                    scheduler.schedule(System.currentTimeMillis() +
                                       retryInterval, this);
                } else {
                    debug.println(0, "Giving up after " + maxRetries 
                                  + " delivery trials.");
                }
            } catch( SPECjAppServerException e ) {
                if( debugging )
                    debug.println(1, e.getMessage());
                debug.printStackTrace(e);
            } catch( IOException e ) {
                if( debugging )
                    debug.println(1, e.getMessage());
                debug.printStackTrace(e);
            }
        }
    }
}
