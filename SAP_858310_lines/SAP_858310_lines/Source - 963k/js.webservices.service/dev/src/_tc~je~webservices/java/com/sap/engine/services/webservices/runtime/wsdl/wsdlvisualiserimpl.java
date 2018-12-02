package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.ServerRuntimeProcessException;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.registry.RuntimeRegistry;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitionsParser;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

import org.w3c.dom.Element;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WsdlVisualiserImpl {
  private static ServiceGenerator serviceGenerator = new ServiceGenerator();
  private static WSDLDefinitionsParser definitionsParser = new WSDLDefinitionsParser();
  private static DOMSerializer domSerializer = new DOMSerializer();

  public static void writeServiceWsld(String serviceEndpointId, HttpServletRequest request, String wsdlStyle, String sapParamValue, OutputStream out) throws ServerRuntimeProcessException {
    try {
      ServiceEndpointDefinition endpointDefinition = getServiceEndpoint(serviceEndpointId);

      WSRuntimeDefinition wsRuntimeDefinition = endpointDefinition.getOwner();
      QName wsQName = wsRuntimeDefinition.getWsQName();

      String requestedUrl = request.getRequestURL().toString();
      String hostAddress = requestedUrl.substring(0, requestedUrl.indexOf(request.getContextPath()));
      String file = request.getParameter("file");
      boolean isSapWSDL;

      if (sapParamValue != null && sapParamValue.equals("sap_wsdl")) { //it is sap wsdl
        isSapWSDL = true;
      } else {
        isSapWSDL = false;
      }

      Object retObj = serviceGenerator.generateSOAPHTTPServiceDefinitions(wsQName.getLocalPart(), hostAddress, endpointDefinition.getOwner(), wsdlStyle, isSapWSDL, file);

      if (retObj instanceof WSDLDefinitions) { //this is for the normal deployed applications
        synchronized (definitionsParser) {
          definitionsParser.writeDefintionsToStream((WSDLDefinitions) retObj, out);
          definitionsParser.init();
        }
      } else { //this is for outside in deployed applications
        synchronized(domSerializer) {
          domSerializer.write((Element) retObj, out);
        }
      }
    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }

  public static void writeBindingWsld(String serviceEndpointId, String wsdlStyle, String sapParamValue, OutputStream out) throws ServerRuntimeProcessException {

    boolean isSapWSDL;
    if (sapParamValue != null && sapParamValue.equals("sap_wsdl")) { //it is sap wsdl
      isSapWSDL = true;
    } else {
      isSapWSDL = false;
    }

    try {

      ServiceEndpointDefinition endpointDefinition = getServiceEndpoint(serviceEndpointId);;

      String bindingWsdlRel = endpointDefinition.getBindingWsdl(wsdlStyle, isSapWSDL);
      File source = new File(bindingWsdlRel);

      if (source.exists()) {
        FileInputStream inputStream = new FileInputStream(source);
        try {
          copy(inputStream, out);
        } finally {
          inputStream.close();
        }
      } else {
        throw new Exception("The file:" + source.getPath() + " does not exist");
      }

    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }

  public static void writePortTypeWsld(String serviceEndpointId, String wsdlStyle, OutputStream out,  String sapParamValue) throws ServerRuntimeProcessException {
    try {

      boolean isSapWSDL;
      if (sapParamValue != null && sapParamValue.equals("sap_wsdl")) { //it is sap wsdl
        isSapWSDL = true;
      } else {
        isSapWSDL = false;
      }

      ServiceEndpointDefinition endpointDefinition = getServiceEndpoint(serviceEndpointId);

      String bindingWsdlRel = endpointDefinition.getVIWsdlPath(wsdlStyle, isSapWSDL);
      File source = new File(bindingWsdlRel);

      if (source.exists()) {
        FileInputStream inputStream = new FileInputStream(source);
        try {
          copy(inputStream, out);
        } finally {
          inputStream.close();
        }
      } else {
        throw new Exception("The file:" + source.getPath() + " does not exist");
      }

    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }

  private static ServiceEndpointDefinition getServiceEndpoint(String serviceEndpointId) throws ServerRuntimeProcessException {
    ServiceEndpointDefinition endpointDefinition = null;
    try {
      RuntimeRegistry runtimeRegistry = WSContainer.getRuntimeRegistry();
      endpointDefinition = runtimeRegistry.getServiceEndpoint(serviceEndpointId);
    } catch(RegistryException e) {
      e.setLogSettings(null, Severity.PATH, Location.getLocation(WSLogging.RUNTIME_LOCATION));
      e.log();
      throw new ServerRuntimeProcessException(e);
    }
    return endpointDefinition;
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    int  bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int count = 0;
    while ( (count = in.read(buffer)) != -1) {
     out.write(buffer, 0 , count);
    }
    out.flush();
  }

}