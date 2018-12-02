package com.sap.engine.services.webservices.runtime.wsdl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.ClusterException;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.interfaces.webservices.runtime.Config;
import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.common.WSConnectionConstants;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding;
import com.sap.engine.services.webservices.runtime.servlet.DocumentationHandler;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitionsParser;
import com.sap.engine.services.webservices.wsdl.WSDLDocumentation;
import com.sap.engine.services.webservices.wsdl.WSDLExtension;
import com.sap.engine.services.webservices.wsdl.WSDLImport;
import com.sap.engine.services.webservices.wsdl.WSDLPort;
import com.sap.engine.services.webservices.wsdl.WSDLService;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class ServiceGenerator {

  private static final String WSDL_HTTP_NS  =  "http://schemas.xmlsoap.org/wsdl/http/";
  private static final String TRANSPORTGUARANTEE_FEATURE  =  "http://www.sap.com/webas/630/soap/features/transportguarantee";
  private static final String SSL_PROPERTY  =  "TLSType";
  private static final String SSL_VALUE  =  "SSL";

  private static final String BINDING_NS_PREFIX  =  "bns";
  private static final String SOAP_ADDRESS_TAG   =  "address";
  private static final String IMPORT_TAG   =  "import";
  private static final String ROOT_WSDL_FILE   =  "wsdlroot.wsdl"; //this is the root file of outside in wsdls
  private static final String MAIN_WSDL_FILE   =  "main.wsdl"; //this is the root file in the downloaded wsdls

  private static WSDLDefinitionsParser wsdlDefinitionsParser = new WSDLDefinitionsParser();
  private static InstancesPool hashTablePool = new InstancesPool();
  private static DOMSerializer domSerializer = new DOMSerializer();
  private static SAXParser saxParser;
  private static DocumentationHandler wsdDocumHandler;
  private static DocumentBuilderFactory documentFactory;

  static {
    try {
      saxParser = SAXParserFactory.newInstance().newSAXParser();
      documentFactory = DocumentBuilderFactory.newInstance();
      documentFactory.setNamespaceAware(true);
    } catch (Exception e) {
      Location.getLocation(WSLogging.SERVER_LOCATION).catching(e);
    }
    wsdDocumHandler = new DocumentationHandler();
  }

  private static String getBindingImportLocation(String serverAddress, ServiceEndpointDefinition endpoint, String style, boolean isSapWSDL) {
    if (isSapWSDL) {
      if (serverAddress != null) {
        return generateHTTPLocationAttrValue(serverAddress, endpoint, null, false) + "/bindings?wsdl&" + "style=" + style + "&mode=sap_wsdl";
      } else { //for writing wsdls in a directory, used by Sasho
        return endpoint.getServiceEndpointId() + "/bindings?wsdl&" + "style=" + style + "&mode=sap_wsdl";
      }
    }
    if (serverAddress != null) {
      return generateHTTPLocationAttrValue(serverAddress, endpoint, null, false) + "/bindings?wsdl&" + "style=" + style;
    } else { //for writing wsdls in a directory, used by Sasho
      return endpoint.getServiceEndpointId() + "/bindings?wsdl&" + "style=" + style;
    }
  }

  private static String getStandAloneBindingImportLocation(ServiceEndpointDefinition endpoint, String style, boolean isSapWSDL) {
    if (isSapWSDL) {
      return "./bindings/" + endpoint.getConfigurationName() + "_" + style + "_sap.wsdl";
    } else { //for writing wsdls in a directory, used by Sasho
      return "./bindings/" + endpoint.getConfigurationName() + "_" + style + ".wsdl";
    }
  }

  private static String getTargetNamespace(ServiceEndpointDefinition endpoint, String style) {
    return "urn:" + endpoint.getOwner().getWsdName() + "/" + endpoint.getConfigurationName() + "/" + style;
  }

  private static Element getOutsideInWSDL(WSRuntimeDefinition wsDefinition, String hostAddress, String file) throws Exception {
    String outSideInWsdlPath = wsDefinition.getWsDirsHandler().getOutsideInWsdlPath(wsDefinition.getOutsideInDefinition().getWsdlRelPath());
    File wsdlFile;
    if (file == null) { //request for the root wsdl
      wsdlFile = new File(outSideInWsdlPath);
    } else {
      File wsdlDir = new File(outSideInWsdlPath).getParentFile();
      wsdlFile = new File(wsdlDir, file);
    }

    ServiceEndpointDefinition endPoints[] = wsDefinition.getServiceEndpointDefinitions();
    if (endPoints.length == 0) {
      throw new Exception("No endpoints are found for webservice: " + wsDefinition.getWsQName());
    }
    String style = endPoints[0].getOutsideInConfiguration().getStyle().trim();

    //loading the wsdl document
    Element wsdlDefin;
    synchronized (documentFactory) {
      wsdlDefin = documentFactory.newDocumentBuilder().parse(wsdlFile).getDocumentElement();
    }

    Element tmpAddEl;
    //setting the new endpoint
    NodeList nList = wsdlDefin.getElementsByTagNameNS(NS.WSDL_SOAP_EXTENSION, SOAP_ADDRESS_TAG);
    for (int i = 0; i < nList.getLength(); i++) {
      tmpAddEl = (Element) nList.item(i);
      tmpAddEl.setAttribute("location", generateHTTPLocationAttrValue(hostAddress, endPoints[0], style, true));
    }

    //setting the relative prefix of the urlnew endpoint
    if (hostAddress.endsWith("/")) {
      hostAddress = hostAddress.substring(0, hostAddress.length() - 1);
    }

    nList = wsdlDefin.getElementsByTagNameNS("*", IMPORT_TAG);
    String uriPref = hostAddress + endPoints[0].getServiceEndpointId() + "?wsdl&file="; //full uri
    String locValue;
    for (int i = 0; i < nList.getLength(); i++) {
      tmpAddEl = (Element) nList.item(i);
      //wsdl imports processing
      locValue = tmpAddEl.getAttribute("location");
      if (locValue.length() > 0) {
        tmpAddEl.setAttribute("location", uriPref + locValue);
      }
      //schema imports processing
      locValue = tmpAddEl.getAttribute("schemaLocation");
      if (locValue.length() > 0) {
        tmpAddEl.setAttribute("schemaLocation", uriPref + locValue);
      }
    }

    return wsdlDefin;
  }

  public static Object generateSOAPHTTPServiceDefinitions(String serviceName, String hostAddress, WSRuntimeDefinition wsDefinition, String style, boolean isSapWSDL, String file) throws Exception {
    if (wsDefinition.hasOutsideInDefinition()) { //in case of outsideIn
      return getOutsideInWSDL(wsDefinition, hostAddress, file);
    }

    HashMapObjectObject hashTable = (HashMapObjectObject) hashTablePool.getInstance();

    if (hashTable == null) {
      hashTable = new HashMapObjectObject();
    }

    WSDLDefinitions def;

    try {
      ServiceEndpointDefinition endPoints[] = wsDefinition.getServiceEndpointDefinitions();
      for (int i = 0; i < endPoints.length; i++) {
        hashTable.put(endPoints[i].getTransportBindingId(), WSContainer.getComponentFactory().getTransportBindingInstance(endPoints[i].getTransportBindingId()));
      }
      def = generateSOAPHTTPServiceDefinitionsInternal(serviceName, hostAddress, wsDefinition, style, isSapWSDL, hashTable, false, false, null);
    } finally {
      hashTable.clear();
      hashTablePool.rollBackInstance(hashTable);
    }
    return def;
  }

  private static void saveOutsideInWSDLsInDirectory(String hostAddress, WSRuntimeDefinition wsDefinition, File resultDirectory) throws Exception {
    ServiceEndpointDefinition endPoints[] = wsDefinition.getServiceEndpointDefinitions();
    if (endPoints.length == 0) {
      throw new Exception("No endpoints are found for webservice: " + wsDefinition.getWsQName());
    }
    String style = endPoints[0].getOutsideInConfiguration().getStyle().trim();

    File wsdlDir = new File(wsDefinition.getWsDirsHandler().getOutsideInWsdlPath(wsDefinition.getOutsideInDefinition().getWsdlRelPath())).getParentFile();

    File sWsdlFile, dWsdlFile;
    Element wsdlDefin;
    File[] wsdlFiles = wsdlDir.listFiles();
    FileOutputStream outputFile;
    for (int i = 0; i < wsdlFiles.length; i++) {
      sWsdlFile = wsdlFiles[i];
      synchronized (documentFactory) {
        wsdlDefin = documentFactory.newDocumentBuilder().parse(sWsdlFile).getDocumentElement();
      }

      Element tmpAddEl;
      //setting the new endpoint
      NodeList nList = wsdlDefin.getElementsByTagNameNS(NS.WSDL_SOAP_EXTENSION, SOAP_ADDRESS_TAG);
      for (int j = 0; j < nList.getLength(); j++) {
        tmpAddEl = (Element) nList.item(j);
        tmpAddEl.setAttribute("location", generateHTTPLocationAttrValue(hostAddress, endPoints[0], style, true));
      }
      //setting the name of the result file
      if (sWsdlFile.getName().equals(ROOT_WSDL_FILE)) {
        dWsdlFile = new File(resultDirectory, MAIN_WSDL_FILE);
      } else {
        dWsdlFile = new File(resultDirectory, sWsdlFile.getName());
      }
      //writing the file
      outputFile = new FileOutputStream(dWsdlFile);
      try {
        synchronized(domSerializer) {
          domSerializer.write(wsdlDefin, outputFile);
        }
      } finally {
        outputFile.close();
      }
    }
  }

  public static void saveAllWSDLsInDirectory(String serviceName, String hostAddress, WSRuntimeDefinition wsDefinition, String style, boolean isSapWSDL, File resultDirectory) throws Exception {
    if (wsDefinition.hasOutsideInDefinition()) {
      saveOutsideInWSDLsInDirectory(hostAddress, wsDefinition, resultDirectory);
      return;
    }

    HashMapObjectObject hashTable = (HashMapObjectObject) hashTablePool.getInstance();

    if (hashTable == null) {
      hashTable = new HashMapObjectObject();
    }

    resultDirectory.mkdirs();
    WSDLDefinitions def;

    try {
      ServiceEndpointDefinition endPoints[] = wsDefinition.getServiceEndpointDefinitions();
      for (int i = 0; i < endPoints.length; i++) {
        hashTable.put(endPoints[i].getTransportBindingId(), WSContainer.getComponentFactory().getTransportBindingInstance(endPoints[i].getTransportBindingId()));
      }
      def = generateSOAPHTTPServiceDefinitionsInternal(serviceName, hostAddress, wsDefinition, style, isSapWSDL, hashTable, true, true, resultDirectory.getAbsolutePath());
    } finally {
      hashTable.clear();
      hashTablePool.rollBackInstance(hashTable);
    }

    File result = new File(resultDirectory, MAIN_WSDL_FILE);

    synchronized(wsdlDefinitionsParser) {
      wsdlDefinitionsParser.parseDefinitionsToFile(def, result);
      wsdlDefinitionsParser.init();
    }
  }

  public static WSDLDefinitions generateSOAPHTTPServiceDefinitionsInternal(String serviceName, String hostAddress, WSRuntimeDefinition wsDefinition, String style, boolean isSapWSDL, HashMapObjectObject hashTable, boolean standAlone, boolean isOnserver, String destDir) throws Exception {

    WSDLDefinitions def = new WSDLDefinitions();
    def.setName(wsDefinition.getWsdName());
    def.targetNamespace = "urn:" + wsDefinition.getWsdName();

    //adding
    CharArray documentation = new CharArray();
    if (isSapWSDL) {
      documentation.append(WSDLPortTypeGenerator.SAP_DEFINITIONS_HINT_START_ELEMENT);
    }

    String wsdDoc = getWSDDocumentation(wsDefinition.getWsDirsHandler().getDocPath(wsDefinition.getDocumentationRelPath()));

    if (wsdDoc != null && wsdDoc.length() > 0) {
      documentation.append(wsdDoc);
    }

    if (isSapWSDL) {
      documentation.append(WSDLPortTypeGenerator.SAP_DEFINITIONS_HINT_END_ELEMENT);
    }

    if (documentation.length() > 0) {
      WSDLDocumentation wsdlDocEl = new WSDLDocumentation();
      wsdlDocEl.setContent(documentation);
      def.setDocumentation(wsdlDocEl);
    }

    com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding transportBinding;
    ServiceEndpointDefinition cur;
    String[] endPointStyles;
    String btargetNamespace;
    String tbPrefix;
    String serverAddress;
    int portTypeCodes[];

    ArrayList imports = new ArrayList();
    ServiceEndpointDefinition endPoints[] = wsDefinition.getServiceEndpointDefinitions();


    WSDLService service = new WSDLService();
    service.setName(serviceName);

    int bindingPrefixes = 0;

    for (int sE = 0; sE < endPoints.length; sE++) {
      cur = endPoints[sE];

      serverAddress = cur.getTargetServerURL();
      //read the server url from descriptor
      if (serverAddress == null || serverAddress.trim().length() == 0) {
        serverAddress = hostAddress;
      }

      transportBinding = (com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding) hashTable.get(cur.getTransportBindingId());
      endPointStyles = transportBinding.getSupportedSyles();
      portTypeCodes = transportBinding.getNecessaryPortTypes();
      int st = 0;
      if (style == null || style.equals("")) { //in case no style param is available use default
        st = endPointStyles.length;
      } else {
        for (st = 0; st < endPointStyles.length; st++) {
          if (style.equals(endPointStyles[st])) { //found request style
            btargetNamespace = getTargetNamespace(cur, style);
            tbPrefix = BINDING_NS_PREFIX + (bindingPrefixes++);
            if (standAlone) {
              if (isOnserver) { //used in the server
                File f = new File(wsDefinition.getWsDirsHandler().getWsdlDir());
                File base = new File(f, "alone");
                //binding file copying
                File source = new File(base, getStandAloneBindingImportLocation(cur, style, isSapWSDL));
                File destination = new File(destDir, getStandAloneBindingImportLocation(cur, style, isSapWSDL));
                copy(source, destination);
                //portType file copying
                copyRefPortTypes(base, destDir, cur, portTypeCodes[st], isSapWSDL);
//                source = new File(getPortTypeOutputFilePath(base.getAbsolutePath(), getVIName(cur.getViRelPath()), cur.getConfigurationName(), style));
//                destination = new File(getPortTypeOutputFilePath(destDir, getVIName(cur.getViRelPath()), cur.getConfigurationName(), style));
//                copy(source, destination);
                //adding the binding in the import
                imports.add(new WSDLImport(null, getStandAloneBindingImportLocation(cur, style, isSapWSDL), btargetNamespace));
              } else {
                imports.add(new WSDLImport(null, getStandAloneBindingImportLocation(cur, style, isSapWSDL), btargetNamespace));
              }
            } else {
              imports.add(new WSDLImport(null, getBindingImportLocation(serverAddress, cur, style, isSapWSDL), btargetNamespace));
            }
            def.addAdditionalAttribute("xmlns:" + tbPrefix , btargetNamespace);
            service.addPort(generatePort(cur.getServiceEndpointQualifiedName().getLocalPart(), serverAddress, style, tbPrefix, cur, transportBinding));
            break;
          }
        }
      }

      if (st == endPointStyles.length) { //the endpoint does not support this style use default
        endPointStyles = transportBinding.getDefaultStyles();
        portTypeCodes = getPortTypeCodes(transportBinding, endPointStyles);
        for (int i = 0; i < endPointStyles.length; i++) {
          btargetNamespace = getTargetNamespace(cur, endPointStyles[i]);
          tbPrefix = BINDING_NS_PREFIX + (bindingPrefixes++);
          if (standAlone) {
            if (isOnserver) {
              File f = new File(wsDefinition.getWsDirsHandler().getWsdlDir());
              File base = new File(f, "alone");
              //binding file copying
              File source = new File(base, getStandAloneBindingImportLocation(cur, endPointStyles[i], isSapWSDL));
              File destination = new File(destDir, getStandAloneBindingImportLocation(cur, endPointStyles[i], isSapWSDL));
              copy(source, destination);
              //portType file copying
              copyRefPortTypes(base, destDir, cur, portTypeCodes[i], isSapWSDL);
//              source = new File(getPortTypeOutputFilePath(base.getAbsolutePath(), getVIName(cur.getViRelPath()), cur.getConfigurationName(), endPointStyles[i]));
//              destination = new File(getPortTypeOutputFilePath(destDir, getVIName(cur.getViRelPath()), cur.getConfigurationName(), endPointStyles[i]));
//              copy(source, destination);
              //adding the binding in the import
              imports.add(new WSDLImport(null, getStandAloneBindingImportLocation(cur, endPointStyles[i], isSapWSDL), btargetNamespace));
            } else {
              imports.add(new WSDLImport(null, getStandAloneBindingImportLocation(cur, endPointStyles[i], isSapWSDL), btargetNamespace));
            }
          } else {
            imports.add(new WSDLImport(null, getBindingImportLocation(serverAddress, cur, endPointStyles[i], isSapWSDL), btargetNamespace));
          }
          def.addAdditionalAttribute("xmlns:" + tbPrefix , btargetNamespace);
          service.addPort(generatePort(cur.getServiceEndpointQualifiedName().getLocalPart(), serverAddress, endPointStyles[i], tbPrefix, cur, transportBinding));
        }
      }
    }

    def.setImportDeclaratuions(imports);
    def.addService(service);
    return def;
  }


  private static WSDLPort generatePort(String portName, String hostAddress, String style, String bindingNSPrefix, ServiceEndpointDefinition endpoint, com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding transportBinding) {

    WSDLPort port = new WSDLPort();
    port.setName(portName + "_" + getUpperLetteredString(style));
    port.setBinding(new QName(bindingNSPrefix, endpoint.getWsdlBindingName().getLocalPart(), null));

    WSDLExtension ext = new WSDLExtension();
    ext.setLocalName("address");

    if (transportBinding instanceof AbstractHTTPTransportBinding) { //generate SOAP address
      ext.setURI(WSDL_HTTP_NS);
    } else { //generate HTTP address
      ext.setURI(NS.WSDL_SOAP_EXTENSION);
    }
    ext.setAttribute("location", generateHTTPLocationAttrValue(hostAddress, endpoint, style, true), "");

    port.setExtension(ext);

    return port;
  }

  public static String generateHTTPLocationAttrValue(String hostAddress, ServiceEndpointDefinition def, String style, boolean isEndpoint) {

    if (hostAddress.endsWith("/")) {
      hostAddress = hostAddress.substring(0, hostAddress.length() - 1);
    }

    if (isEndpoint) {
      //check for SecurityProtocol and SSL configuration.
      Iterator itr = def.getProtocolIDFeatureMappings().values().iterator();
      Feature[] tmp;
      boolean foundHttps = false;

      while ((! foundHttps) && itr.hasNext()) {
        tmp = (Feature[]) itr.next();
        if (tmp != null) {
          for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].getFeatureName().equals(TRANSPORTGUARANTEE_FEATURE)) {
              foundHttps = true;
              Config cfg = tmp[i].getConfiguration();
              if (cfg != null && cfg.getProperty(SSL_PROPERTY) != null) {
                if (cfg.getProperty(SSL_PROPERTY).getValue().equals(SSL_VALUE)) {
                  Object[] disNodeInfo = getSSLDispatcher();
                  if (disNodeInfo != null) {
                    String host = ((java.net.InetAddress) disNodeInfo[0]).getHostAddress();
                    hostAddress = "https://" + host + ":" + disNodeInfo[1];
                  } else {
                    int ind = hostAddress.lastIndexOf(":");
                    String host = hostAddress.substring(hostAddress.indexOf("://") + 3);
                    int port = 443; //default ssl port
                    if (ind != -1) {
                      host = hostAddress.substring(hostAddress.indexOf("://") + 3, ind);
                      port = Integer.parseInt(hostAddress.substring(ind + 1)) + 1;
                    } else {
                      host = hostAddress.substring(hostAddress.indexOf("://"));
                    }
                    hostAddress = "https://" + host + ":" + port;
                  }
                  break;//the for loop
                }
              }
            }
          }
        }
      }
    }

    String endpointUri = def.getServiceEndpointId();
    if (endpointUri.startsWith("/")) {
      endpointUri = endpointUri.substring(1, endpointUri.length());
    }

    if (style == null) { //for binding wsdl generation
      return hostAddress + "/" + endpointUri;
    }

    if (style.equals("http")) {//for HTTP binding
      return hostAddress + "/" + endpointUri;
    } else {
      return hostAddress + "/" + endpointUri + "?style=" + style;
    }
  }

  private static String getWSDDocumentation(String docPath) throws Exception {
    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(docPath);
    } catch (java.io.FileNotFoundException fnfE) { //in case the path do ne exists
      Location.getLocation(WSLogging.SERVER_LOCATION).catching(fnfE);      
//      fnfE.printStackTrace();
      return null;
    }

    String doc;
    synchronized (saxParser) {
      try {
        saxParser.parse(inputStream, wsdDocumHandler);
        doc = wsdDocumHandler.getDocumentation();
        wsdDocumHandler.clear();
      } finally {
        inputStream.close();
      }
    }

    return doc;
  }

  public static String getUpperLetteredString(String s) {
    char c = Character.toUpperCase(s.charAt(0));
    c = Character.toUpperCase(c);

    if (s.length() > 1) {
      return c + s.substring(1);
    } else {
      return new String(new char[]{c});
    }
  }

  private static String getPortTypeOutputFilePath(String outDir, String viName, String confName, String style, boolean sapMode) {
    if (sapMode) {
      return outDir + "/porttypes/" + confName + "_" + viName + "_" + style + "_sap.wsdl";
    } else {
      return outDir + "/porttypes/" + confName + "_" + viName + "_" + style + ".wsdl";
    }
  }

  private static void copy(File source, File destination) throws Exception {
    destination.getParentFile().mkdirs();
    FileInputStream input = new FileInputStream(source);
    FileOutputStream output = new FileOutputStream(destination);

    byte[] arr = new byte[128];
    int b;

    try {
      while ((b = input.read(arr)) != -1) {
        output.write(arr, 0, b);
      }
    } finally {
      input.close();
      output.close();
    }
  }

  private static void copyRefPortTypes(File baseDir, String destDir, ServiceEndpointDefinition endPoint, int prCode, boolean sapMode) throws Exception {
    File source, destination;
    String style;

    String viName = endPoint.getvInterfaceName();
    if ((prCode & PortTypeDescriptor.ENCODED_PORTTYPE) == PortTypeDescriptor.ENCODED_PORTTYPE) {
      style = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.ENCODED_PORTTYPE);
      source = new File(getPortTypeOutputFilePath(baseDir.getAbsolutePath(), viName, endPoint.getConfigurationName(), style, sapMode));
      destination = new File(getPortTypeOutputFilePath(destDir, viName, endPoint.getConfigurationName(), style, sapMode));
      copy(source, destination);
    }

    if ((prCode & PortTypeDescriptor.LITERAL_PORTTYPE) == PortTypeDescriptor.LITERAL_PORTTYPE) {
      style = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.LITERAL_PORTTYPE);
      source = new File(getPortTypeOutputFilePath(baseDir.getAbsolutePath(), viName, endPoint.getConfigurationName(), style, sapMode));
      destination = new File(getPortTypeOutputFilePath(destDir, viName, endPoint.getConfigurationName(), style, sapMode));
      copy(source, destination);
    }

    if ((prCode & PortTypeDescriptor.RPC_LITERAL_PORTTYPE) == PortTypeDescriptor.RPC_LITERAL_PORTTYPE) {
      style = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.RPC_LITERAL_PORTTYPE);
      source = new File(getPortTypeOutputFilePath(baseDir.getAbsolutePath(), viName, endPoint.getConfigurationName(), style, sapMode));
      destination = new File(getPortTypeOutputFilePath(destDir, viName, endPoint.getConfigurationName(), style, sapMode));
      copy(source, destination);
    }

    if ((prCode & PortTypeDescriptor.HTTP_PORTTYPE) == PortTypeDescriptor.HTTP_PORTTYPE) {
      style = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.HTTP_PORTTYPE);
      source = new File(getPortTypeOutputFilePath(baseDir.getAbsolutePath(), viName, endPoint.getConfigurationName(), style, sapMode));
      destination = new File(getPortTypeOutputFilePath(destDir, viName, endPoint.getConfigurationName(), style, sapMode));
      copy(source, destination);
    }
  }

  private static int[] getPortTypeCodes(RuntimeTransportBinding trb, String[] defStyles) throws Exception {
    int[] res = new int[defStyles.length];

    String[] styles = trb.getSupportedSyles();
    int[] portTypeCodes  = trb.getNecessaryPortTypes();

    int nom = 0;
    for (int i = 0; i < styles.length; i++) {
      for (int j = 0; j < defStyles.length; j++) {
        if (styles[i].equals(defStyles[j])) {
          res[nom++] = portTypeCodes[i];
        }
      }
    }

    return res;
  }

  /**
   *
   * @return Object[2]. Object[0] is java.net.InetAddres object of the dispatcher node.
   *                    Object[1] is java.lang.Integer value for the SSL port.
   *                    If no SSL port is available null is returned.
   */
  private static Object[] getSSLDispatcher() {
    MessageContext msgCtx = WSContainer.getServiceContext().getClusterContext().getMessageContext();
    int[] dispatcherIDs = WSContainer.getDispatcherIDs();
    int sslPort = -1;
    for (int i = 0; i < dispatcherIDs.length; i++) {
      int dispatcherID = dispatcherIDs[i];
      ClusterElement clusterEl = WSContainer.getServiceContext().getClusterContext().getClusterMonitor().getParticipant(dispatcherID);
      if (clusterEl == null) {
        continue;
      }
      try {
        MessageAnswer msgAnswer = msgCtx.sendAndWaitForAnswer(dispatcherID,
                WSConnectionConstants.GET_SSL_PORT, new byte[0], 0, 0, 0);
        if (msgAnswer.getLength() != 0) {
          ByteArrayInputStream in = new ByteArrayInputStream(msgAnswer.getMessage(), msgAnswer.getOffset(), msgAnswer.getLength());
          DataInputStream dataStream = new DataInputStream(in);
          try {
            sslPort = dataStream.readInt();
            if (sslPort != -1) {
              Object[] res = new Object[2];
              res[0] = clusterEl.getAddress();
              res[1] = new Integer(sslPort);
              return res;
            }
          } catch (IOException ioe) {
            Location.getLocation(WSLogging.SERVER_LOCATION).catching("An error occurred while parsing getSSLPort response: " + dispatcherIDs[i], ioe);
          }
        }
      } catch (ClusterException ce) {
        Location.getLocation(WSLogging.SERVER_LOCATION).catching("An error occurred while sending getSSLPort message to dispatcher: " + dispatcherIDs[i], ce);
      }
    }
    return null;
  }

}
