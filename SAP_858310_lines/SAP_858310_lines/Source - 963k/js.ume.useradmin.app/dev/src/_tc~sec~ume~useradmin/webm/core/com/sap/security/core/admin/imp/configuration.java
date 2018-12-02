package com.sap.security.core.admin.imp;

/**
 * @author d028785
 *
 */

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException; 

import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException; 

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException; 

import org.w3c.dom.Document;
import org.w3c.dom.DOMException; 

import com.sap.security.api.UMException;

import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.InternalUMFactory;


public class Configuration {

    public static final String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/imp/Configuration.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    public final static String TRUE                         = "true";
    public final static String FALSE                        = "false";

    public final static String PRINCIPAL_TYPE_WILDCARD      = "*";

    public final static String DEFAULT_ENCODING             = "UTF-8";

    InputStream mConfigFile;

    private static IUMTrace mTrace;

    static {
        //get the trace
        mTrace = InternalUMFactory.getTrace(VERSIONSTRING);
    }

    public Configuration(InputStream configurationFile) 
    	throws UMException {
    	final String methodName = "Configuration(InputStream configurationFile)";
        if (mTrace.bePath()) {
            mTrace.entering(methodName,new Object[]{ configurationFile });
        }
        //check whether the given configuration files exists or not
        mConfigFile = configurationFile;
        if ( null == configurationFile ) {
        	String errorMessage = "Config file is null!";
            mTrace.errorT(methodName, errorMessage);
            throw new UMException(errorMessage);
        }
        refresh();
        if (mTrace.bePath()) {
            mTrace.exiting(methodName);
        }
    }

    public void refresh() throws UMException {
    	final String methodName = "refresh";
        if (mTrace.bePath()) {
            mTrace.entering(methodName);
        }
        //initialize XML parser

        DocumentBuilderFactory factory;
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        ClassLoader mine    = this.getClass().getClassLoader();
		if(mine == null) mine = ClassLoader.getSystemClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mine);
            factory = DocumentBuilderFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }

        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder parser = null;
        try {
            parser = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pcex) {
            mTrace.errorT(methodName, pcex.getMessage(), pcex);
            throw new UMException(pcex.getMessage());
        }

        Document document   = null;
        try {
            //build a dom from the content of the configFile
            document = parser.parse(mConfigFile, DEFAULT_ENCODING);
        } catch (IOException ioex) {
            String errorMessage = "Error while opening config file "+mConfigFile+": " + ioex.getMessage();
            mTrace.errorT("refresh", errorMessage, ioex);
            throw new UMException(errorMessage);
        } catch (SAXException saxex) {
            String errorMessage = "Error while parsing config file "+mConfigFile+": " + saxex.getMessage();
            mTrace.errorT("refresh", errorMessage, saxex);
            throw new UMException(errorMessage);
        }
    }
}

