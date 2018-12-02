/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.util;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class XmlUtils {
  private static final Location currentLocation = Location.getLocation(XmlUtils.class);
  private static final Location traceLocation = LogContext.getLocationDeploy();

  /**
   * @param xmlFile
   * @param additonalXmlFile
   * @param aliasCanonicalized
   * @param xmlFileName
   * @param additonalXmlFileName
   * @param validate
   * @return
   * @throws DeploymentException
   */
  public static WebDeploymentDescriptor parseXml(InputStream xmlFile, InputStream additonalXmlFile, String aliasCanonicalized, String xmlFileName, String additonalXmlFileName, boolean validate) throws DeploymentException {
    return parseXml(xmlFile, additonalXmlFile, aliasCanonicalized, xmlFileName, additonalXmlFileName, validate, true);
  }//end of parseXml(InputStream xmlFile, InputStream additonalXmlFile, String aliasCanonicalized, String xmlFileName, String additonalXmlFileName, boolean validate)

  /**
   * @param xmlFile
   * @param additonalXmlFile
   * @param aliasCanonicalized
   * @param xmlFileName
   * @param additonalXmlFileName
   * @param validate
   * @param initDefaults         if true, initialized the default values if not set in the xml files
   * @return
   * @throws DeploymentException
   */
  public static WebDeploymentDescriptor parseXml(InputStream xmlFile, InputStream additonalXmlFile, String aliasCanonicalized, String xmlFileName,
                                                 String additonalXmlFileName, boolean validate, boolean initDefaults) throws DeploymentException {
    WebDeploymentDescriptor webDesc = new WebDeploymentDescriptor(initDefaults);

    try {
      if (xmlFile == null) {
        throw new DeploymentException("File: " + xmlFileName + " not found!");
      }
      webDesc.loadDescriptorFromStreams(xmlFile, additonalXmlFile, validate);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_READ_XML_OR_ERROR_IN_XML_FOR_WEB_APPLICATION,
        new Object[]{xmlFileName + " or " + additonalXmlFileName, aliasCanonicalized}, e);
    } finally {
      if (xmlFile != null) {
        try {
          xmlFile.close();
        } catch (IOException io) {
					if (traceLocation.beWarning()) {
			LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000495",
								"Error while closing InputStream for [{0}] while deploying [{1}] application", new Object[]{xmlFileName, aliasCanonicalized}, io, null, null);
		}
        }
      }
      if (additonalXmlFile != null) {
        try {
          additonalXmlFile.close();
        } catch (IOException io) {
					if (traceLocation.beWarning()) {
			LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000496",
								"Error while closing InputStream for [{0}] while deploying [{1}] application", new Object[]{additonalXmlFileName, aliasCanonicalized}, io, null, null);
		}
        }
      }
    }
    return webDesc;
  }

  /**
   * @param xmlFile
   * @param addXmlFile
   * @return
   */
  public static WebDeploymentDescriptor getDescriptor(String xmlFile, String addXmlFile) {
    WebDeploymentDescriptor webDeploymentDescriptor = new WebDeploymentDescriptor();
    try {
      FileInputStream fis = new FileInputStream(xmlFile);
      byte[] tempStream = null;
      try {
        tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(fis, "web", "simple");
      } finally {
        fis.close();
      }

      if (addXmlFile != null) {
        fis = new FileInputStream(addXmlFile);
        byte[] addTempStream = null;
        try {
          addTempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(fis, "web-j2ee-engine", "simple");
        } finally {
          fis.close();
        }
        webDeploymentDescriptor.loadDescriptorFromStreams(new ByteArrayInputStream(tempStream), new ByteArrayInputStream(addTempStream), true);
      } else {
        webDeploymentDescriptor.loadDescriptorFromStreams(new ByteArrayInputStream(tempStream), null, true);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000022",
        "Cannot load a descriptor for web.xml or web-j2ee-engine.xml.", e, null, null);
      return null;
    }

    return webDeploymentDescriptor;
  }//end of getDescriptor(String xmlFile, String addXmlFile, boolean validate)

  /**
   * @param document
   * @param element
   * @param value
   * @param elementName
   * @return
   */
  public static Element createSubElement(Document document, Element element, String value, String elementName) {
    if (value == null) {
      value = new String("");
    }
    Element subElement = document.createElement(elementName);
    Text textNode = document.createTextNode(value);
    subElement.appendChild(textNode);
    element.insertBefore(subElement, element.getFirstChild());
    return subElement;
  }//end of createSubElement(Document document,Element element,String value,String elementName)

}//end of class
