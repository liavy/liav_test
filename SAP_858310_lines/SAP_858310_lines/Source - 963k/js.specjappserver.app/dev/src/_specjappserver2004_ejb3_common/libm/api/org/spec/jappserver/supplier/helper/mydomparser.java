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
 *  2003/04/01  John Stecher, IBM       updated debugging
 *  2003/06/30  John Stecher, IBM       Added JAXP parsing of the document
 */

package org.spec.jappserver.supplier.helper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * This is a wrapper for the xerces xml parser (or any other parser
 * that we choose to use)
 */
public class MyDOMParser implements ErrorHandler {

	DocumentBuilderFactory docBuilderFactory;
	DocumentBuilder docparser;

    /**
     * Constructor MyDOMParser
     *
     *
     */
    public MyDOMParser() {

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating(true);
		try {
            docparser = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method parse
     *
     *
     * @param source
     *
     * @return
     *
     * @throws Exception
     *
     */
    public Document parse(InputSource source) throws Exception {
        return docparser.parse(source);
    }

    /**
     * Method warning
     *
     *
     * @param ex
     *
     */
    public void warning(SAXParseException ex) {
        System.err.println("WARNING: " + ex.getMessage());
    }

    /**
     * Method error
     *
     *
     * @param ex
     *
     */
    public void error(SAXParseException ex) {
        System.err.println("ERROR: " + ex.getMessage());
    }

    /**
     * Method fatalError
     *
     *
     * @param ex
     *
     * @throws SAXException
     *
     */
    public void fatalError(SAXParseException ex) throws SAXException {

        System.err.println("FATAL ERROR: " + ex.getMessage());

        throw ex;
    }
}

