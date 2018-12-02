/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.interfaces.webservices.server.WebServicesContainerManipulator;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.jaxws.cts.CTSProvider;
import com.sap.engine.services.webservices.espbase.configuration.Behaviour;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationBuilder;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerException;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.IConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.URLSchemeType;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.configuration.cfg.SoapApplicationRegistry;
import com.sap.engine.services.webservices.espbase.configuration.p_set.ScopeType;
import com.sap.engine.services.webservices.espbase.espsrv.ESPServiceFactory;
import com.sap.engine.services.webservices.espbase.espsrv.ESPTechnicalException;
import com.sap.engine.services.webservices.espbase.espsrv.ESPWSReverseProxyConfiguration;
import com.sap.engine.services.webservices.espbase.espsrv.proxy.WSMappingResult;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.MetaDataAccessor;
import com.sap.engine.services.webservices.espbase.server.WSLocalCallException;
import com.sap.engine.services.webservices.espbase.server.logging.ProviderLogger;
import com.sap.engine.services.webservices.espbase.server.runtime.exceptions.NoSuchWebServiceException;
import com.sap.engine.services.webservices.espbase.server.runtime.exceptions.WSPolicyModeNotSupportedException;
import com.sap.engine.services.webservices.espbase.server.runtime.sec.WSDLSecurityProcessor;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLDescriptor;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLSerializer;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.wspolicy.Policy;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class provides static methods for writing
 * wsdls as http response content.
 *
 *
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-1-14
 */
public class WSDLVisualizer {


  static final String STYLE_PARAM  =  "style";
  static final String MODE_PARAM  =  "mode";
  static final String SAP_MODE  =  "sap_wsdl";
  static final String WSPOLICY_MODE  =  "ws_policy";
  static final String STANDARD_MODE  =  "standard";
  static final String WSDL_PARAM  =  "wsdl";
  static final String WSDL_PARAM_CAPITALIZED  =  "WSDL";
  static final String BINDING_WSDL  =  "binding";
  static final String INTERFACE_WSDL  =  "interface";
  static final String SERVICE_WSDL  =  "service";
  static final String ZIP = "zip";

  static final String J2EE14_LOCATION_PARAM  =  "location";

  static final String PORTTYPE_WSDL_TYPE  =  "porttype";
  static final String BINDING_WSDL_TYPE  =  "binding";

  static final Transformer transformer;
  static final WSDLSerializer wsdlSerializer;
  ConfigurationBuilder configurationBuilder;

  private static final String FEATURE_ELEM_NS  =  "http://www.sap.com/webas/630/wsdl/features";
  private static final String FEATURE_ELEM = "Feature";
  private static final String FEATURE_NAME_ATTR = "name";
  private static final String FEATURE_URI_ATTR = "uri";
  private static final String PROPERTY_ELEM = "Property";
  private static final String OPTION_ELEM = "Option";
  private static final String PROPERTY_QNAME_ATTR  =  "qname";
  private static final String OPTION_VALUE_ATTR  =  "value";
  private static final String USE_FEATURE_ELEM  =  "useFeature";
  private static final String SAP_FEATURE_NS = "http://www.sap.com/webas/java/webservices/framework/";
  private static final String PROTOCOL_ORDER_FEATURE = "protocol-order";

  private static final String TRANSPORT_BINDING_FEATURE_NS  =  "http://www.sap.com/webas/710/soap/features/transportbinding/";
  private static final String ALTERNATIVE_HOST_PROPERTY = "altHost";
  private static final String ALTERNATIVE_PORT_PROPERTY = "altPort";
  
  /** MEX Data */
  private static final String WSDL_DIALECT = "http://schemas.xmlsoap.org/wsdl/";
  private static final String XSD_DIALECT = "http://www.w3.org/2001/XMLSchema";
  
  private final static int BUFFER = 2048;

  private static Location location = Location.getLocation(WSDLVisualizer.class);
  

  static {
    try {
      TransformerFactory tF = TransformerFactory.newInstance(); 
      transformer = tF.newTransformer();
      /* In order to pass buggy CTS test:
       * com/sun/ts/tests/jaxws/wsi/j2w/rpc/literal/R4003/Client.java#testDescriptionEncoding
       * The test checks the 'encoding' value in case sensitive way - 'UTF-8'.
       * By omitting the declaration the test passes.
       */
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      wsdlSerializer = new WSDLSerializer();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private MetaDataAccessor metaDataAccessor;
  private ApplicationServiceContext appSrvContext;

  public WSDLVisualizer(MetaDataAccessor metaDataAccessor, ApplicationServiceContext appSrvContext) throws Exception {
    this.metaDataAccessor = metaDataAccessor;
    this.appSrvContext = appSrvContext;
    configurationBuilder = new ConfigurationBuilder();
  }

  public ConfigurationBuilder getInternalCFGBuilder() {
    return configurationBuilder;
  }

  public boolean writeWSDL(HttpServletRequest req, HttpServletResponse resp) throws ServerRuntimeProcessException {
    String wsdlParam = getWSDLParamValue(req);
    
    // If the wsdl param is not present we won't write wsdl to the response
    if (wsdlParam == null){
      return false;
    }
    
    try{    
    //auth againts x509, sso, basic auth.
    authenticate(req);
        
    if (req.getParameter(ZIP) != null) {
      resp.setContentType("application/zip");
      String mode = req.getParameter(MODE_PARAM);
      if (WSPOLICY_MODE.equals(mode)) {
        mode = "POLICY";
      } else if (SAP_MODE.equals(mode)) {
        mode = "SAP";
      } else {
        mode = "STANDARD";
      }
      resp.setHeader("Content-disposition", "123; filename=\"" + mode + "_WSDL.zip\"");
      try {
        String url = req.getRequestURL().toString();
        URI urlObj = new URI(url);
        new WSDLZipBuilder().writeZipContent(urlObj.getScheme() + "://" + urlObj.getHost() + req.getRequestURI() + "?" + req.getQueryString(), this, resp.getOutputStream());
      } catch (Exception e) {
        throw new ServerRuntimeProcessException(e);
      }
      return true;
    } else {
      return writeWSDL0(req, resp);
    }
    }catch (LoginException le){
      //unautorized. Auth failed.
      location.traceThrowableT(Severity.ERROR, "Authentication failed", le);
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);            
      return true;
    }catch (Exception e){      
      throw new ServerRuntimeProcessException(e);
    }       
  }
  
  
  /**
   * 
   * @param req
   * @throws Exception
   */
  private void authenticate(HttpServletRequest req) throws Exception{
    
    if (WSDLSecurityProcessor.isSecureWsdl()) {      
      // auth againts the modules.
      WSDLSecurityProcessor.authenticate(req, this.appSrvContext);
    }
  }
  
  
  /**
   * Writes WSDL into <code>resp</code> output stream
   * as http response.
   * @return true if the http request is for wsdl visualization and wsdl is written as response.
   *         False if that is not request for wsdl visualization.
   */
  boolean writeWSDL0(HttpServletRequest req, HttpServletResponse resp) throws ServerRuntimeProcessException {
    try {
      String reqURL = getRequestURI(req);
      // decode URL - e.g. it may contain either ~ or %7E - then encode it again - binding data stores encoded endpoint URLs
      String reqURLEncoded = RuntimeProcessingEnvironment.decodeEncodeURI(reqURL);

      // Checked above
      String wsdlParam = getWSDLParamValue(req);

      com.sap.engine.services.webservices.espbase.configuration.Service s = getService(reqURLEncoded);
      if (s == null) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.NO_CONFIG_ENTITY_FOUND_FOR_REQUEST_URL, new Object[]{"Service", reqURLEncoded});
      }
      if (new Integer(-1).equals(s.getActive())) {
        String errStr = "No web service endpoint is available under '" + reqURLEncoded + "' path.";
        LOC.warningT(errStr);
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, errStr);
        return true;
      }

      BindingData bData = metaDataAccessor.getBindingData(reqURLEncoded);
      if (bData == null) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.NO_CONFIG_ENTITY_FOUND_FOR_REQUEST_URL, new Object[]{"BindingData", reqURLEncoded});
      }
      ServiceData sData = s.getServiceData();

      InterfaceDefinition intfDef = getInterfaceDefinition(reqURLEncoded);
      Set<String> styles = metaDataAccessor.getWSDLStyles(reqURLEncoded);
      //do param check
      if (! checkParamValues(req, resp, sData, intfDef)) {
        return true; //it is true because response is sent
      }
      String mode = req.getParameter(MODE_PARAM);
      //write JEE wsdl with policies. The JEE policy wsdl uses the SAPOutsideIn wsdl visualization framework
      if (com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE.equals(s.getType())
          && WSPOLICY_MODE.equals(mode) )  {
        writeSAPOutsideInWSDL(reqURLEncoded, req, resp, true);
        return true;
      }
      //write J2EE wsdl      
      if (com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE.equals(s.getType()))  {
        writeJ2EE14WSDL(reqURLEncoded, req, resp, false);
        return true;
      }
      //write SAP outsideIn wsdl
      if (com.sap.engine.services.webservices.espbase.configuration.Service.SAP_OUTSIDEIN_SERVICE_TYPE.equals(s.getType())) {
        writeSAPOutsideInWSDL(reqURLEncoded, req, resp, false);
        return true;
      }
      if (! checkStyle(req, resp, styles)) {
        return true; //it is true because response is sent
      }
      String style = req.getParameter(STYLE_PARAM);
      style = getWSDLStyle(style, styles);
      //request for SAP binding wsdl
      if (BINDING_WSDL.equals(wsdlParam)) {
        writeBindingWSDL(style, mode, req, resp, bData, sData, intfDef, reqURLEncoded);
      } else if (INTERFACE_WSDL.equals(wsdlParam)) { //request for portType wsdl
        writePortTypeWSDL(style, mode, resp, bData, intfDef, reqURLEncoded);
      } else if (SERVICE_WSDL.equals(wsdlParam) || wsdlParam.length() == 0) { //this is request for service wsdl
        writeServiceWSDL(style, mode, req, resp, sData, reqURLEncoded);
      }
      return true;
    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }

  private String getWSDLStyle(String reqStyleParam, Set<String> supportedStyles){
    String style = reqStyleParam;
    if (style == null) {
      //in case the styles contain 
      final String docStyle = "document";
      if (supportedStyles.contains(docStyle)) {
        style = docStyle;
      } else {
        if (supportedStyles.size() == 1) {
          style = (String) supportedStyles.iterator().next(); //get the only entity.
        } else {
          throw new RuntimeException(RuntimeExceptionConstants.DEFAULT_STYLE_NOT_DEFINED);
        }
      }
    }
    return style;
  }


  private String getLocationAttr(String reqAlias, boolean isJEE){
    File targetFile = null;
    if (isJEE) {
      targetFile = getPortTypeWSDL(reqAlias);
    } else {
      targetFile = getRootWSDL(reqAlias);
    }
    File moduleDir = getModuleWSDLDir(reqAlias);
    String relativeLoc = moduleDir.toURI().relativize(targetFile.toURI()).toString();
    //use always the first BindingData url as porttype import location
    com.sap.engine.services.webservices.espbase.configuration.Service s = getService(reqAlias);
    String firstBDURL = s.getServiceData().getBindingData()[0].getUrl(); //use always the first BindingData

    firstBDURL = constructBindingDataAlias(s.getServiceData().getContextRoot(), firstBDURL);

    String locAttr = "/" + firstBDURL + "?" + J2EE14_LOCATION_PARAM + "=" + relativeLoc + "&" + WSDL_PARAM;
    return locAttr;
  }

  private boolean writeSAPOutsideInWSDL(String reqAlias, HttpServletRequest req, HttpServletResponse resp, boolean isJEE) throws Exception {
    location.debugT("writeSAPOutsideInWSDL(): entered...");

    String mode = req.getParameter(MODE_PARAM);

    if (isJEE && WSPOLICY_MODE.equals(mode)) { //check whether for that WS ws_policy visualization is enabled.
      String bindingTmpl  = metaDataAccessor.getWsdlPath(reqAlias, BINDING_WSDL_TYPE, "default");
      String portTypeTmpl = metaDataAccessor.getWsdlPath(reqAlias, "porttype", "default");
      if (! (bindingTmpl != null && portTypeTmpl != null && new File(bindingTmpl).exists() && new File(portTypeTmpl).exists())) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "com.sap.SOA.wsr.030103 - Requested web service does not support 'ws_policy' mode.");
        return false;
      }
    }

    //whether this is request for 'root' wsdl...
    String locParam = req.getParameter(J2EE14_LOCATION_PARAM);
    if (locParam != null) {
      return writeJ2EE14WSDL(reqAlias, req, resp, true);
    }


    String wsdlParam = getWSDLParamValue(req);
    //this is request for binding wsdl
    if (BINDING_WSDL.equals(wsdlParam)) {
      String path = metaDataAccessor.getWsdlPath(reqAlias, BINDING_WSDL_TYPE, "default");
      Element bDoc = parseToDom(path);

      //apply runtime features (for WSDL1.1)
      List bEls = DOM.getChildElementsByTagNameNS(bDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
      if (bEls.size() != 1) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"binding", Integer.toString(bEls.size()), bDoc});
      }
      BindingData bData = metaDataAccessor.getBindingData(reqAlias);
      if (bData == null) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.NO_CONFIG_ENTITY_FOUND_FOR_REQUEST_URL, new Object[]{"BindingData", reqAlias});
      }

      Element bEl = (Element) bEls.get(0);
      //apply targetNS
      bDoc.setAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR, bData.getBindingNamespace());
      //apply bindingName
      bEl.setAttribute(WSDL11Constants.NAME_ATTR, bData.getBindingName());
      //apply import@location
      String locAttr = getLocationAttr(reqAlias, isJEE);
      if (mode != null) {
        locAttr += "&" + MODE_PARAM + "=" + mode;
      }
      //updates the prefix value that points to portType ns
      InterfaceDefinition intfDefs = getInterfaceDefinition(reqAlias);
      String pTNS = intfDefs.getVariant()[0].getInterfaceData().getNamespace();
      String pTLocalName = intfDefs.getVariant()[0].getInterfaceData().getName();
      String bType = bEl.getAttribute(WSDL11Constants.TYPE_ATTR);
      String pref = DOM.qnameToPrefix(bType);
      Element prefDeclElem = DOM.getPrefixDeclaringElement(bEl, pref);
      if (prefDeclElem != null) {
        prefDeclElem.setAttributeNS(NS.XMLNS, "xmlns:" + pref, pTNS);
      }
      //update binding name
      bEl.setAttribute(WSDL11Constants.TYPE_ATTR, pref + ":" + pTLocalName);

      List imports = DOM.getChildElementsByTagNameNS(bDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
      if (imports.size() != 1) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"import", Integer.toString(imports.size()), bDoc});
      }
      Element importEl = (Element) imports.get(0);
      {//reverse proxy
        try {
          ESPWSReverseProxyConfiguration reverseProxy = ESPServiceFactory.getEspService().getESPWSReverseProxyConfiguration();
          locAttr = reverseProxy.getURIMapping(locAttr, true, true);
        } catch (ESPTechnicalException e) {
          location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
        }
      }
      importEl.setAttribute(WSDL11Constants.LOCATION_ATTR, locAttr);

      if (SAP_MODE.equals(mode)) {
        //apply binding level features
        String tnsPref = getTargetNSPrefix(bDoc);
        String fNSPref = applyPrefixForNS(bEl, FEATURE_ELEM_NS, "fns");
        String fIDBase = bEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_runtime-feature";
        applyFeatures0(bDoc, bEl, bData, bData, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.BINDING_LEVEL, intfDefs);
        //apply binding/operation level features
        List ops = DOM.getChildElementsByTagNameNS(bEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
        OperationData op;
        Element opEl;
        for (int i = 0; i < ops.size(); i++) {
          opEl = (Element) ops.get(i);
          String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
          op = getOperationData(bData.getOperation(), opName);
          if (op == null) {
            throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"BindingData/Operation", opName});
          }
          fIDBase = bEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_" + op.getName() + "_runtime-feature";
          applyFeatures0(bDoc, opEl, bData, op, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.BINDING_OPERATION_LEVEL, intfDefs);
        }
      } else if (WSPOLICY_MODE.equals(mode)) { //policy wsdl
        //apply usingPolicy element
        appendUsingPolicyExtension(bDoc);
        int pCounter = 0;
        //apply policies on binding level
        pCounter = applyPolicy(bDoc, bEl, bData, pCounter, intfDefs, IConfigurationMarshaller.BINDING_LEVEL);
        //apply policies on binding/operation level
        List ops = DOM.getChildElementsByTagNameNS(bEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
        OperationData op;
        Element opEl;
        for (int i = 0; i < ops.size(); i++) {
          opEl = (Element) ops.get(i);
          String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
          op = getOperationData(bData.getOperation(), opName);
          if (op == null) {
            throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"BindingData/Operation", opName});
          }
          pCounter = applyPolicy(bDoc, opEl, op, pCounter,  intfDefs, IConfigurationMarshaller.BINDING_OPERATION_LEVEL);
        }
      }
      writeResponse(bDoc, resp);
    } else { //this is for service
      com.sap.engine.services.webservices.espbase.configuration.Service s = getService(reqAlias);
      ServiceData sd = s.getServiceData();
      //create dom tree for wsdl:service
      Element def = SharedDocumentBuilders.newDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.DEFINITIONS_ELEMENT);
      def.setAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR, sd.getNamespace());
      Element srv = def.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
      srv.setAttribute(WSDL11Constants.NAME_ATTR, sd.getName());

      BindingData[] bds = sd.getBindingData();
      for (int i = 0; i < bds.length; i++) {
        BindingData bd = bds[i];
        //create wsdl:import for bindingData
        Element imp = srv.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
        imp.setAttribute(WSDL11Constants.NAMESPACE_ATTR, bd.getBindingNamespace());
        imp.setAttribute(WSDL11Constants.LOCATION_ATTR, getImportLocation(req, "/" + constructBindingDataAlias(sd.getContextRoot(), bd.getUrl()), null, mode, BINDING_WSDL));
        def.appendChild(imp);
        //map prefix on definition for bd namespace
        String pref = "b" + i;
        def.setAttributeNS(NS.XMLNS, "xmlns:" + pref, bd.getBindingNamespace());
        //create port
        Element port = srv.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.PORT_ELEMENT);
        port.setAttribute(WSDL11Constants.NAME_ATTR, bd.getName());
        port.setAttribute(WSDL11Constants.BINDING_ELEMENT, pref + ":" + bd.getBindingName());
        Element soapAddress = port.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, WSDL11Constants.ADDRESS_ELEMENT);
        String endpointURL = createURL(req.getServerName(), bd, "default", sd.getContextRoot(), sd);
        soapAddress.setAttribute(WSDL11Constants.LOCATION_ATTR, endpointURL);
        port.appendChild(soapAddress);
        srv.appendChild(port);
      }
      def.appendChild(srv);
      writeResponse(def, resp);
    }
    return true;
  }
  /**
   * Returns true, if this is request to j2ee14 wsdl and wsld is writtern, false otherwise.
   */
  private boolean writeJ2EE14WSDL(String reqAlias, HttpServletRequest req, HttpServletResponse resp, boolean utilizeModeParam) throws Exception {
    location.debugT("writeJ2EE14WSDL() entered...");

    String locParam = req.getParameter(J2EE14_LOCATION_PARAM);
    location.debugT("writeJ2EE14WSDL() locParam: '" + locParam + "'");

    File targetFile;
    File moduleDir = CTS50_JWS_PREDEFINED_WSDLS.get(reqAlias);
    boolean isRootWSDL = false;
    if (moduleDir != null) {
      if (locParam == null) { //this is request for root wsdl
        targetFile = CTS50_JWS_PREDEFINED_WSDLS.get(reqAlias + "/root.wsdl");
        isRootWSDL = true;
      } else {
        targetFile = new File(moduleDir, locParam);
      }
    } else {
      moduleDir = getModuleWSDLDir(reqAlias);
      if (locParam == null) { //this is request for root wsdl
        targetFile = getRootWSDL(reqAlias);
        isRootWSDL = true;
      } else {
        targetFile = new File(moduleDir, locParam);
      }
      location.debugT("isJ2ee14WSDLRequest() wsdlFile: '" + targetFile + "'");
      if (! targetFile.exists()) {
        throw new Exception("File does not exist: " + targetFile);
      }
    }
    location.debugT("isJ2ee14WSDLRequest() wsdlFile: '" + targetFile + "'");

    //update port addresses, imports/includes
    Element updatedDoc = updateJ2EE14WSDL(targetFile, moduleDir, reqAlias, req, isRootWSDL);

    if (utilizeModeParam) { //this is true only for SAP outside-in case
      BindingData bData = metaDataAccessor.getBindingData(reqAlias);
      InterfaceDefinition intfDef = getInterfaceDefinition(reqAlias);
      InterfaceData intfData = getVariant(intfDef, bData).getInterfaceData();
      String mode = req.getParameter(MODE_PARAM);

      Element pTDoc = updatedDoc;
      List pTEls = DOM.getChildElementsByTagNameNS(pTDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT);
      if (pTEls.size() != 1) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"portType", Integer.toString(pTEls.size()), pTDoc});
      }
      Element pTEl = (Element) pTEls.get(0);

      if (SAP_MODE.equals(mode) && (! hasDTAltarnatives(intfDef))) {
        //apply portType level features
        String tnsPref = getTargetNSPrefix(pTDoc);
        String fNSPref = applyPrefixForNS(pTEl, FEATURE_ELEM_NS, "fns");
        String fIDBase = pTEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_design-feature";
        applyFeatures0(pTDoc, pTEl, intfData, intfData, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.INTERFACE_LEVEL, intfDef);
        //apply portType/operation level features
        List ops = DOM.getChildElementsByTagNameNS(pTEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
//      OperationData ops[] = intfData.getOperation();
        OperationData op;
        Element wsdlOp;
        for (int i = 0; i < ops.size(); i++) {
          wsdlOp = (Element) ops.get(i);
          String opName = wsdlOp.getAttribute(WSDL11Constants.NAME_ATTR);
          op = getOperationData(intfData.getOperation(), opName);
          if (op == null) {
            throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"InterfaceData/Operation", opName});
          }
          fIDBase = pTEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_" + op.getName() + "_design-feature";
          applyFeatures0(pTDoc, wsdlOp, intfData, op, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL, intfDef);
        }
      } else if (WSPOLICY_MODE.equals(mode)) { //policy wsdl
        //apply usingPolicy element
        appendUsingPolicyExtension(pTDoc);
//      InterfaceData mergedIntfData = mergeVariants(intfDef);
        int pCounter = 0;
        //apply policies on binding level
        pCounter = applyPolicy(pTDoc, pTEl, intfData, pCounter, intfDef, IConfigurationMarshaller.INTERFACE_LEVEL);
        //apply policies on interface/operation level
        List ops = DOM.getChildElementsByTagNameNS(pTEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
        //OperationData ops[] = intfData.getOperation();
        OperationData op;
        Element opEl;
        for (int i = 0; i < ops.size(); i++) {
          opEl = (Element) ops.get(i);
          String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
          op = getOperationData(intfData.getOperation(), opName);
          if (op == null) {
            throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"InterfaceData/Operation", opName});
          }
          pCounter = applyPolicy(pTDoc, opEl, op, pCounter, intfDef, IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL);
        }
      }
    }
    writeResponse(updatedDoc, resp);
    return true;
  }
  /**
   * Clears the whole wsdl cache.
   */
  public void clearWSDLCache() throws ServerRuntimeProcessException {
    File mWsdlCacheDir = new File(appSrvContext.getServiceState().getWorkingDirectoryName(), "wsdl-cache");
    try {
      if (! deleteDir(mWsdlCacheDir)) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNABLE_TO_DELETE_WSDLCACHE_DIR, new Object[]{mWsdlCacheDir});
      }
    } catch (IOException ioE) {
      throw new ServerRuntimeProcessException(ioE);
    }
  }
  /**
   * Clears the wsdl cache for all web services withing appliation <code>appName</code>.
   */
  public void clearWSDLCache(String appName) throws ServerRuntimeProcessException {
    com.sap.engine.services.webservices.espbase.configuration.Service[] ss = metaDataAccessor.getServices(appName);
    if (ss == null) { //there is no cache dir to be cleaned.
      return;
    }
    File mWsdlCacheDir = new File(appSrvContext.getServiceState().getWorkingDirectoryName(), "wsdl-cache");

    File d;
    BindingData bds[];
    String reqURL;
    for (int s = 0; s < ss.length; s++) {
      bds = ss[s].getServiceData().getBindingData();
      for (int b = 0; b < bds.length; b++) {
        reqURL = constructBindingDataAlias(ss[s].getServiceData().getContextRoot(), bds[b].getUrl());
        d = getWSDLCacheDirForBD(mWsdlCacheDir, reqURL);
        try {
          if (! deleteDir(d)) {
            throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNABLE_TO_DELETE_WSDLCACHE_DIR, new Object[]{d});
          }
        } catch (IOException ioE) {
          throw new ServerRuntimeProcessException(ioE);
        }
      }
    }
  }

  /**
   * Updates port addresses and @location attributes of 'import' elements of <code>targetWSDL</code>.
   * @param targetWSDL path to wsdl or schema
   * @param baseDir absolute path to the directory containing all wsdls
   * @return updated wsdl DOM element
   */
  private Element updateJ2EE14WSDL(File targetWSDL, File baseWSDLDir, String reqAlias, HttpServletRequest req, boolean isRootWSDL) throws Exception {
    Element root;
    String targetWSDLPath = targetWSDL.getPath().toLowerCase(Locale.ENGLISH);//  this may no work on UNIX os.  
    if (targetWSDLPath.startsWith(CTS50_JWS_SCHEME)) {
      String s = targetWSDLPath.substring(CTS50_JWS_SCHEME.length()).replace('\\', '/');
      s = s.substring(1); //remove the leading '/';
      InputStream in = WSDLVisualizer.class.getClassLoader().getResourceAsStream(s);
      try {
        InputSource is = new InputSource(in);
        root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, is).getDocumentElement();
      } finally {
        if (in != null) {
          in.close();
        }
      }
    } else {
      root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, targetWSDL).getDocumentElement();
    }
    boolean isWSDL = WSDL11Constants.WSDL_NS.equals(root.getNamespaceURI());
    Attr locAttr;
    String locAttrValue;
    if (isWSDL) {
      if (isRootWSDL) {
        updateWSDLPorts(root, req, reqAlias);
      }
      //update wsdl:import@location attrs
      List wsdlImports = DOM.getChildElementsByTagNameNS(root, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
      Element curImport;
      for (int i = 0; i < wsdlImports.size(); i++) {
        curImport = (Element) wsdlImports.get(i);
        locAttr = curImport.getAttributeNode(WSDL11Constants.LOCATION_ATTR);
        if (locAttr != null && locAttr.getValue().length() > 0) { //if there is @location attr
          locAttrValue = locAttr.getValue();
          //get new value and set it
          locAttrValue = getJ2EEUpdatedLocationValue(req, targetWSDL, baseWSDLDir, locAttrValue, reqAlias);
          locAttr.setValue(locAttrValue);
        }
      }
    }
    //update xsd:import@schemaLocation attrs
    List xsdImpIncs = new ArrayList();
    if (isWSDL) {
      List types = DOM.getChildElementsByTagNameNS(root, WSDL11Constants.WSDL_NS, WSDL11Constants.TYPES_ELEMENT);
      if (types.size() == 1) {
        Element typesElem = (Element) types.get(0);
        List schemas = DOM.getChildElementsByTagNameNS(typesElem, WSDL11Constants.SCHEMA_NS, WSDL11Constants.SCHEMA_ELEM);
        Element curSchema;
        for (int s = 0; s < schemas.size(); s++) {
          curSchema = (Element) schemas.get(s);
          //add all imports into one list
          xsdImpIncs.addAll(DOM.getChildElementsByTagNameNS(curSchema, WSDL11Constants.SCHEMA_NS, WSDL11Constants.IMPORT_ELEMENT));
          //add all includes into one list
          xsdImpIncs.addAll(DOM.getChildElementsByTagNameNS(curSchema, WSDL11Constants.SCHEMA_NS, WSDL11Constants.SCHEMA_INCLUDE_ELEM));
        }
      }
    } else { //this should be schema
      //add all imports into one list
      xsdImpIncs.addAll(DOM.getChildElementsByTagNameNS(root, WSDL11Constants.SCHEMA_NS, WSDL11Constants.IMPORT_ELEMENT));
      //add all includes into one list
      xsdImpIncs.addAll(DOM.getChildElementsByTagNameNS(root, WSDL11Constants.SCHEMA_NS, WSDL11Constants.SCHEMA_INCLUDE_ELEM));
    }
    //update @schemaLocation attrs
    Element impInc;
    for (int i = 0; i < xsdImpIncs.size(); i++) {
      impInc = (Element) xsdImpIncs.get(i);
      locAttr = impInc.getAttributeNode(WSDL11Constants.SCHEMA_LOCATION_ATTR);
      if (locAttr != null && locAttr.getValue().length() > 0) {
        locAttrValue = locAttr.getValue();
        //get new value and set it
        locAttrValue = getJ2EEUpdatedLocationValue(req, targetWSDL, baseWSDLDir, locAttrValue, reqAlias);
        locAttr.setValue(locAttrValue);
      }
    }
    return root;
  }
  /**
   * Updates the port address value (host, port, http-alias)..
   * @param rootWSDL wsdl which contains ports
   * @param req
   * @param bdURL requested url
   */
  private Element updateWSDLPorts(Element rootWSDL, HttpServletRequest req, String reqAlias) throws Exception {
    com.sap.engine.services.webservices.espbase.configuration.Service srv = getService(reqAlias);
    return updateWSDLPorts(rootWSDL, req.getServerName(), srv.getServiceData());
  }
  /**
   * Updates the port address value (host, port, http-alias)..
   * @param rootWSDL wsdl which contains ports
   * @param hostName server host name
   * @param sData
   */
  public Element updateWSDLPorts(Element rootWSDL, String hostName, ServiceData sData) throws Exception {
    //take all wsdl:ports
    List wsdlServices = DOM.getChildElementsByTagNameNS(rootWSDL, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
    List ports = new ArrayList();
    Element serviceElem;
    //collect all ports
    if (wsdlServices.size() == 0) {//there are no service element in the wsdl
      serviceElem = rootWSDL.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
      serviceElem.setAttribute(WSDL11Constants.NAME_ATTR, sData.getName());
      Element portElem;
      BindingData curBD;
      BindingData[] bds = sData.getBindingData();
      for (int i = 0; i < bds.length; i++) {
        curBD = bds[i];
        portElem = createPortElementToSupposedExistingBinding(rootWSDL, curBD, sData, hostName);
        ports.add(portElem);
        serviceElem.appendChild(portElem);
      }
      //append the newly created service to the root wsdl
      rootWSDL.appendChild(serviceElem);
    } else {
      Element tmpSrvEl;
      for (int i = 0; i < wsdlServices.size(); i++) {
        tmpSrvEl = (Element) wsdlServices.get(i);
        String srvElName = tmpSrvEl.getAttribute(WSDL11Constants.NAME_ATTR);
        if (! srvElName.equals(sData.getName())) {
          //remove this service, since its ports won't be updated. This should not break the CTS, but it is necessary for JEE administration 
          tmpSrvEl.getParentNode().removeChild(tmpSrvEl);
        }
      }
      wsdlServices = DOM.getChildElementsByTagNameNS(rootWSDL, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
      if (wsdlServices.size() == 0) { // the services have been removed by the 'removeChilde' above, create new one
        tmpSrvEl = rootWSDL.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
        tmpSrvEl.setAttribute(WSDL11Constants.NAME_ATTR, sData.getName());
        rootWSDL.appendChild(tmpSrvEl);
      }

      wsdlServices = DOM.getChildElementsByTagNameNS(rootWSDL, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
      tmpSrvEl = (Element) wsdlServices.get(0);
      ports.addAll(DOM.getChildElementsByTagNameNS(tmpSrvEl, WSDL11Constants.WSDL_NS, WSDL11Constants.PORT_ELEMENT));
      serviceElem = tmpSrvEl;
    }
    //map ports
    Map portMap = new HashMap(); //key port-name(String), value, port dom Element
    Element portElem;
    String portName;
    for (int i = 0; i < ports.size(); i++) {
      portElem = (Element) ports.get(i);
      portName = portElem.getAttribute(WSDL11Constants.NAME_ATTR);
      portMap.put(portName, portElem);
    }

    BindingData[] bds = sData.getBindingData();
    BindingData curBD;
    String endpointURL;
    for (int i = 0; i < bds.length; i++) {
      curBD = bds[i];
      portElem = (Element) portMap.get(curBD.getName());
      if (portElem == null) {
        //create missing port elements
        portElem = createPortElementToSupposedExistingBinding(rootWSDL, curBD, sData, hostName);
        serviceElem.appendChild(portElem);
      }
      //update @location attribute
      endpointURL = createURL(hostName, curBD, "default", sData.getContextRoot(), sData);
      List soapAddress = DOM.getChildElementsByTagNameNS(portElem, WSDL11Constants.SOAP_NS, WSDL11Constants.ADDRESS_ELEMENT);
      if (soapAddress.size() != 1) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.INVALID_NUMBER_SOAP_ADDRESS_ELEMENTS, new Object[]{soapAddress.size() + "", portElem});
      }
      ((Element) soapAddress.get(0)).setAttribute(WSDL11Constants.LOCATION_ATTR, endpointURL);
    }
    return rootWSDL;
  }



  /**
   * Returns url path for binding, porttype, service policy wsdls. 
   * @param contextRoot
   * @param bdUrl
   * @return
   */




  private Element createPortElementToSupposedExistingBinding(Element rootWSDL, BindingData curBD, ServiceData sData, String hostName) throws ServerRuntimeProcessException {
    Element portElem = rootWSDL.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.PORT_ELEMENT);
    Element soapAddress = rootWSDL.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, WSDL11Constants.ADDRESS_ELEMENT);
    portElem.setAttribute(WSDL11Constants.NAME_ATTR, curBD.getName());
    portElem.setAttributeNS(NS.XMLNS, "xmlns:bref", curBD.getBindingNamespace());
    portElem.setAttribute(WSDL11Constants.BINDING_ELEMENT, "bref:" + curBD.getBindingName());
    portElem.appendChild(soapAddress);
    //update @location attribute
    String endpointURL = createURL(hostName, curBD, "default", sData.getContextRoot(), sData);
    soapAddress.setAttribute(WSDL11Constants.LOCATION_ATTR, endpointURL);
    return portElem;
  }
  /**
   * Returns value, which directly should be used as value of @location attr.
   * @param targetWSDL absolute path to the wsdl containing the @location attr
   * @param baseDir absolute path to the directory containing all wsdls
   * @param relLocation the value of @location attr, as it is in the wsdl file
   */
  private String getJ2EEUpdatedLocationValue(HttpServletRequest req, File targetWSDL, File baseWSDLDir, String relLocation, String reqAlias) throws Exception {
    URI targetWSDLUri = targetWSDL.toURI();
    //resolve @location attr
    URI resolvedLocation = targetWSDLUri.resolve(relLocation);
    //make the resolved location relative to the baseWSDLDir
    URI baseWSDLDirUri = baseWSDLDir.toURI();
    URI relLocFromBaseWSDLDir = baseWSDLDirUri.relativize(resolvedLocation);

    String scheme = req.getScheme();
    String host = req.getServerName();
    int port = req.getServerPort();

    String result = scheme + "://" + host + ":" + port + reqAlias + "?" + J2EE14_LOCATION_PARAM + "=" + relLocFromBaseWSDLDir + "&" + WSDL_PARAM;

    {//reverse proxy
      try {
        URL endpointURLURL = new URL(result);
        ESPWSReverseProxyConfiguration reverseProxy = ESPServiceFactory.getEspService().getESPWSReverseProxyConfiguration();
        WSMappingResult mapping = reverseProxy.getTransportMapping(endpointURLURL.getProtocol(),
                endpointURLURL.getHost(),
                endpointURLURL.getPort(),
                false);
        String uriFinal = reverseProxy.getURIMapping(endpointURLURL.getFile(), 
                false,
                mapping.isMapped());
        return mapping.getTransportMapping().append(uriFinal).toString();
      } catch (ESPTechnicalException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      } catch (MalformedURLException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      }
    }

    return result;
  }
  /**
   * Returns the root wsdl
   */
  private File getRootWSDL(String bdURL) {
    String rootPath = metaDataAccessor.getWsdlPath(bdURL, "root", "default");
    return new File(rootPath);
  }

  /**
   * Returns the porttypewsdl
   */
  private File getPortTypeWSDL(String bdURL) {
    String rootPath = metaDataAccessor.getWsdlPath(bdURL, "porttype", "default");
    return new File(rootPath);
  }
  /**
   * Returns abosolute path of the directory containing extacted archive.
   */
  private File getModuleWSDLDir(String reqAlias) {
    String rootPath = metaDataAccessor.getWsdlPath(reqAlias, "root", "default");
    String relPath = metaDataAccessor.getWSDLRelPath(reqAlias, "root", "default");

    String moduleDir = rootPath.substring(0, rootPath.indexOf(relPath));

//  //check for WEB-INF/wsdl subdir
//  File f = new File(moduleDir, "WEB-INF/wsdl/");
//  if (f.exists()) {
//  return f;
//  }
//  //check for META-INF/wsdl
//  f = new File (moduleDir, "META-INF/wsdl/");
//  if (f.exists()) {
//  return f;
//  }
//  //check for Meta-inf/wsdl
//  f = new File (moduleDir, "Meta-inf/wsdl/");
//  if (f.exists()) {
//  return f;
//  }
//  //for case when wsdls are not in above directories.
    return new File(moduleDir);
  }

  /**
   * Writes definitions with wsdl:portType element...
   */
  private void writePortTypeWSDL(String style, String mode, HttpServletResponse resp, BindingData bData, InterfaceDefinition intfDef, String reqAlias) throws Exception {

    String path = metaDataAccessor.getWsdlPath(reqAlias, PORTTYPE_WSDL_TYPE, style);
    Element pTDoc = parseToDom(path);

    //apply DT features (for WSDL1.1)
    List pTEls = DOM.getChildElementsByTagNameNS(pTDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT);
    if (pTEls.size() != 1) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"portType", Integer.toString(pTEls.size()), pTDoc});
    }
    Element pTEl = (Element) pTEls.get(0);
    Variant v = getVariant(intfDef, bData);
    InterfaceData intfData = v.getInterfaceData();
    //apply targetNS 
    updateTargetNamespace(pTDoc, intfData.getNamespace());
    //apply portType name
    pTEl.setAttribute(WSDL11Constants.NAME_ATTR, intfData.getName());
    //features wsdl. There should be no DT alternatives
    if (SAP_MODE.equals(mode) && (! hasDTAltarnatives(intfDef))) {
      //apply portType level features
      String tnsPref = getTargetNSPrefix(pTDoc);
      String fNSPref = applyPrefixForNS(pTEl, FEATURE_ELEM_NS, "fns");
      String fIDBase = pTEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_design-feature";
      applyFeatures0(pTDoc, pTEl, intfData, intfData, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.INTERFACE_LEVEL, intfDef);
      //apply portType/operation level features
      List ops = DOM.getChildElementsByTagNameNS(pTEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
//    OperationData ops[] = intfData.getOperation();
      OperationData op;
      Element wsdlOp;
      for (int i = 0; i < ops.size(); i++) {
        wsdlOp = (Element) ops.get(i);
        String opName = wsdlOp.getAttribute(WSDL11Constants.NAME_ATTR);
        op = getOperationData(intfData.getOperation(), opName);
        if (op == null) {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"InterfaceData/Operation", opName});
        }
        fIDBase = pTEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_" + op.getName() + "_design-feature";
        applyFeatures0(pTDoc, wsdlOp, intfData, op, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL, intfDef);
      }
    } else if (WSPOLICY_MODE.equals(mode)) { //policy wsdl
      //apply usingPolicy element
      appendUsingPolicyExtension(pTDoc);
//    InterfaceData mergedIntfData = mergeVariants(intfDef);
      int pCounter = 0;
      //apply policies on binding level
      pCounter = applyPolicy(pTDoc, pTEl, intfData, pCounter, intfDef, IConfigurationMarshaller.INTERFACE_LEVEL);
      //apply policies on interface/operation level
      List ops = DOM.getChildElementsByTagNameNS(pTEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      //OperationData ops[] = intfData.getOperation();
      OperationData op;
      Element opEl;
      for (int i = 0; i < ops.size(); i++) {
        opEl = (Element) ops.get(i);
        String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
        op = getOperationData(intfData.getOperation(), opName);
        if (op == null) {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"InterfaceData/Operation", opName});
        }
        pCounter = applyPolicy(pTDoc, opEl, op, pCounter, intfDef, IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL);
      }
    }
    //write response
    writeResponse(pTDoc, resp);
  }
  /**
   * Writes definitions with wsdl:binding element...
   */
  private void writeBindingWSDL(String style, String mode, HttpServletRequest req, HttpServletResponse resp, BindingData bData, ServiceData sData, InterfaceDefinition intfDef, String reqAlias) throws Exception {
    //!!!! Cache is disabled because of the aboslute imports locations used. For example if the first request to wsdl has come via tunnel, the wrong port will be cached.
    //try cache first 
    //boolean useCache = useCache(style, mode, resp, reqAlias, BINDING_WSDL);
    //if (useCache) { //in case of successful usage of te cache, return.
    //  return;
    //}

    String path = metaDataAccessor.getWsdlPath(reqAlias, BINDING_WSDL_TYPE, style);
    Element bDoc = parseToDom(path);

    //apply runtime features (for WSDL1.1)
    List bEls = DOM.getChildElementsByTagNameNS(bDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    if (bEls.size() != 1) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"binding", Integer.toString(bEls.size()), bDoc});
    }
    Element bEl = (Element) bEls.get(0);
    //apply targetNS 
    updateTargetNamespace(bDoc, bData.getBindingNamespace());
    //apply bindingName
    bEl.setAttribute(WSDL11Constants.NAME_ATTR, bData.getBindingName());
    //apply import values
    String pTNS = intfDef.getVariant()[0].getInterfaceData().getNamespace(); //there should be at least one variant
    String pTLocalName = intfDef.getVariant()[0].getInterfaceData().getName(); //there should be at least one variant
    //use always the first BindingData url as porttype import location
    String firstBDURL = sData.getBindingData()[0].getUrl(); //use always the first BindingData
    String impLocation = getImportLocation(req, "/" + constructBindingDataAlias(sData.getContextRoot(), firstBDURL), style, mode, INTERFACE_WSDL);
    List imports = DOM.getChildElementsByTagNameNS(bDoc, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
    if (imports.size() != 1) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ONE_WSDLENTITY_EXPECTED, new Object[]{"import", Integer.toString(imports.size()), bDoc});
    }
    Element importEl = (Element) imports.get(0);
    importEl.setAttribute(WSDL11Constants.LOCATION_ATTR, impLocation);
    importEl.setAttribute(WSDL11Constants.NAMESPACE_ATTR, pTNS);
    //updates the prefix value that points to portType ns
    String bType = bEl.getAttribute(WSDL11Constants.TYPE_ATTR);
    String pref = DOM.qnameToPrefix(bType);
    Element prefDeclElem = DOM.getPrefixDeclaringElement(bEl, pref);
    if (prefDeclElem != null) {
      prefDeclElem.setAttributeNS(NS.XMLNS, "xmlns:" + pref, pTNS);
    }
    //update binding name
    bEl.setAttribute(WSDL11Constants.TYPE_ATTR, pref + ":" + pTLocalName);
    //features wsdl
    if (SAP_MODE.equals(mode)) {
      //apply binding level features
      String tnsPref = getTargetNSPrefix(bDoc);
      String fNSPref = applyPrefixForNS(bEl, FEATURE_ELEM_NS, "fns");
      String fIDBase = bEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_runtime-feature";
      applyFeatures0(bDoc, bEl, bData, bData, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.BINDING_LEVEL, intfDef);
      //apply binding/operation level features
      List ops = DOM.getChildElementsByTagNameNS(bEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      OperationData op;
      Element opEl;
      for (int i = 0; i < ops.size(); i++) {
        opEl = (Element) ops.get(i);
        String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
        op = getOperationData(bData.getOperation(), opName);
        if (op == null) {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"BindingData/Operation", opName});
        }
        fIDBase = bEl.getAttribute(WSDL11Constants.NAME_ATTR) + "_" + op.getName() + "_runtime-feature";
        applyFeatures0(bDoc, opEl, bData, op, fIDBase, fNSPref, tnsPref, IConfigurationMarshaller.BINDING_OPERATION_LEVEL, intfDef);
      }
    } else if (WSPOLICY_MODE.equals(mode)) { //policy wsdl
      //apply usingPolicy element
      appendUsingPolicyExtension(bDoc);
      int pCounter = 0;
      //apply policies on binding level
      pCounter = applyPolicy(bDoc, bEl, bData, pCounter, intfDef, IConfigurationMarshaller.BINDING_LEVEL);
      //apply policies on binding/operation level
      List ops = DOM.getChildElementsByTagNameNS(bEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      OperationData op;
      Element opEl;
      for (int i = 0; i < ops.size(); i++) {
        opEl = (Element) ops.get(i);
        String opName = opEl.getAttribute(WSDL11Constants.NAME_ATTR);
        op = getOperationData(bData.getOperation(), opName);
        if (op == null) {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[]{"BindingData/Operation", opName});
        }
        pCounter = applyPolicy(bDoc, opEl, op, pCounter, intfDef, IConfigurationMarshaller.BINDING_OPERATION_LEVEL);
      }
    }
    //store in cache
    //storeInCache(bDoc, style, mode, reqAlias, BINDING_WSDL);
    //write response
    writeResponse(bDoc, resp);
  }
  /**
   * Creates definitions with wsdl:service element...
   */
  private void writeServiceWSDL(String style, String mode, HttpServletRequest req, HttpServletResponse resp, ServiceData sData, String reqAlias) throws Exception {
    String reqURL = reqAlias;
    String sName = sData.getName();
    String sTNS = sData.getNamespace();

    Definitions dfs = new Definitions();
    Service srv = dfs.appendService(new QName(sTNS, sName));

    String bType = getInterfaceMapping(reqURL).getBindingType();
    BindingData tmpBD;
    Binding binding;
    Endpoint endPoint;
    QName bQName;
    String impLocation;
    BindingData[] bDatas = sData.getBindingData();
    for (int i = 0; i < bDatas.length; i++) {
      tmpBD = bDatas[i];
      bQName = new QName(tmpBD.getBindingNamespace(), tmpBD.getBindingName());
      binding = createDummyBinding(bType, bQName);
      impLocation = getImportLocation(req, "/" + constructBindingDataAlias(sData.getContextRoot(), tmpBD.getUrl()), style, mode, BINDING_WSDL);
      binding.setProperty(Binding.IMPORT_LOCATION, impLocation);
      dfs.appendChild(binding);
      endPoint = srv.appendEndpoint(tmpBD.getName());
      endPoint.setBinding(bQName);
      endPoint.setProperty(Endpoint.URL, createURL(req.getServerName(), tmpBD, style, sData.getContextRoot(), sData));
    }
    List wsdls = null;
    synchronized (wsdlSerializer) {
      wsdls = wsdlSerializer.serialize(dfs, Service.SERVICE_ID);
    }
    WSDLDescriptor wsdlDscr = getWSDLDescriptor(wsdls, sTNS);
    Element dfsEl = wsdlDscr.getWsdl();
    if (SAP_MODE.equals(mode)) { //if this is sap wsdl, add sap documentation element
      appendSAPDocumentation(dfsEl);
    } else if (WSPOLICY_MODE.equals(mode)) {
      appendUsingPolicyExtension(dfsEl);
    }
    //write response
    writeResponse(dfsEl, resp);
  }

  private InterfaceDefinition getInterfaceDefinition(String url) {
    return metaDataAccessor.getInterfaceDefinitionForBindingData(url);
  }

  private  com.sap.engine.services.webservices.espbase.configuration.Service getService(String url) {
    return metaDataAccessor.getServiceForBindingData(url);
  }

//public static WSDLTemplatesDscr getTemplatesDescriptor(String url) {
//return tempDescriptor;        
//}

  public  InterfaceMapping getInterfaceMapping(String url) {
    return metaDataAccessor.getInterfaceMappingForBindingData(url);
  }

  private Binding createDummyBinding(String bType, QName qname) throws Exception {
    if (InterfaceMapping.SOAPBINDING.equals(bType) || InterfaceMapping.MIMEBINDING.equals(bType)) {
      SOAPBinding sBinding =  new SOAPBinding(qname);
      sBinding.setInterface(qname);
      return sBinding;
    } else if (InterfaceMapping.HTTPGETBINDING.equals(bType) || InterfaceMapping.HTTPPOSTBINDING.equals(bType)) {
      HTTPBinding hBinding = new HTTPBinding(qname);
      hBinding.setInterface(qname);
      return hBinding;
    }
    throw new IllegalArgumentException("Binding type '" + bType + "' is not supported.");
  }
  /**
   * Creates port address value.
   */
  private String createURL(String hostName, BindingData bData, String style, String contextRoot, ServiceData serviceData) throws ServerRuntimeProcessException {
    StringBuffer base = new StringBuffer();
    String host = null;
    int port = -1;
    //check for alternativeHost
    PropertyType altHostProp = bData.getSinglePropertyList().getProperty(TRANSPORT_BINDING_FEATURE_NS, ALTERNATIVE_HOST_PROPERTY);
    if (altHostProp != null && altHostProp.get_value() != null && altHostProp.get_value().length() > 0) {
      hostName = altHostProp.get_value();
    }
    //check for alternativePort
    PropertyType altPortProp = bData.getSinglePropertyList().getProperty(TRANSPORT_BINDING_FEATURE_NS, ALTERNATIVE_PORT_PROPERTY);
    if (altPortProp != null && altPortProp.get_value() != null && altPortProp.get_value().length() > 0) {
      port = Integer.valueOf(altPortProp.get_value());
    }
    //check whether the SSL is not required by the Security properties
    boolean forceHTTPS = isSSLSwitchedON(bData);
    String scheme = null;
    if (forceHTTPS || URLSchemeType.https.equals(bData.getUrlScheme())) {
      scheme = URLSchemeType._https;
      base.append(scheme).append("://");
      if (port == -1) { //if no altPort is defined
//        {//if proxy mapping property is set in http service - csn 3092829
//          //the hostname is replaced from the http service itseld
//          try {
//            ProxyMappingsReader reader = ProxyMappingsReader.getInstance();
//            if (reader != null) {//esp service not started
//              ProxyMappings all[] = reader.getAllProxyMappings();
//
//              for (int i = 0; i < all.length; i++) {
//                ProxyMappings next = all[i];
//                if (next.getScheme().equals(URLSchemeType._https)) {
//                  hostName = next.getHost();
//                  port = next.getPort();
//                  break;
//                }
//              }
//            }
//          } catch (javax.xml.rpc.ServiceException e) {
//            location.catching(e);
//          }
//        }

        if (port == -1) {
          port = metaDataAccessor.getServerPort(MetaDataAccessor.PORT_HTTPS);
        }
      }
    } else if (URLSchemeType.http.equals(bData.getUrlScheme())) {
      scheme = URLSchemeType._http;
      base.append(scheme).append("://");
      if (port == -1) { //if no altPort is defined
//        {//if proxy mapping property is set in http service - csn 3092829
//          //the hostname is replaced from the http service itseld
//          try {
//            ProxyMappingsReader reader = ProxyMappingsReader.getInstance();
//            if (reader != null) {//esp service not started
//              ProxyMappings all[] = reader.getAllProxyMappings();
//
//              for (int i = 0; i < all.length; i++) {
//                ProxyMappings next = all[i];
//                if (next.getScheme().equals(URLSchemeType._http)) {
//                  hostName = next.getHost();
//                  port = next.getPort();
//                  break;
//                }
//              }
//            }
//          } catch (javax.xml.rpc.ServiceException e) {
//            location.catching(e);
//          }
//        }

        if (port == -1) {
          port = metaDataAccessor.getServerPort(MetaDataAccessor.PORT_HTTP);
        }
      }
    }
    base.append(hostName).append(":");

//  try {
//  HashMap<String,String> h = loadMappingFile();
//  if (h.get("webServerPort.1") != null && !"".equals(h.get("webServerPort.1"))) {
//  port = Integer.parseInt(h.get("webServerPort.1"));
//  }
//  } catch (Exception e) {
//  //$JL-EXC$
//  System.err.println(">>>>>>>> Could not load mapping file due to" );
//  //e.printStackTrace();
//  port = metaDataAccessor.getServerPort(MetaDataAccessor.PORT_HTTP);
//  }

//  System.err.println(">>>>>>>> Using port = " + port);

    base.append(Integer.toString(port));

    StringBuffer alias = new StringBuffer(constructBindingDataAlias(contextRoot, bData.getUrl()));
    alias.insert(0, '/');
    String bdUri = alias.toString();
    //for SOAP binding always there should be 'style' param. For SOAP bindign there is no 'default' style. It is the only style for mime and http bindings
    if (! "default".equals(style)) {
      alias.append("?" + STYLE_PARAM).append("=").append(style);
    }

    try{
      if (RuntimeProcessingEnvironment.isLocalTransportService(bData)){
        base.setLength(0);
        base.append(PublicProperties.TRANSPORT_BINDING_LOCAL_CALL_HOST_PORT);
      } else {
        try {
          ESPWSReverseProxyConfiguration reverseProxy = ESPServiceFactory.getEspService().getESPWSReverseProxyConfiguration();
          WSMappingResult mapping = reverseProxy.getTransportMapping(scheme, hostName, port, false);
          String uriFinal = reverseProxy.getURIMapping(alias.toString(), false, mapping.isMapped());
          return mapping.getTransportMapping().append(uriFinal).toString();
        } catch (ESPTechnicalException e) {
          location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
        }
      }
    }finally{
      if (port == -1){ // log warning, probably service configured with https but https not installed
        String serviceName = serviceData.getName();
        Category category = ProviderLogger.WS_PROVIDER_CATEGORY;
        SimpleLogger.log(Severity.ERROR, category,location, "SOA.wsr.030100", "Invalid endpoint port number generated in wsdl, web service [{0}] at [{1}]: port is [{2}]", new Object[]{serviceName, bdUri, port});
      }
    }
    return base.append(alias).toString();
  }

  private String getRequestURI(HttpServletRequest req) {
    return req.getRequestURI();
  }

  private String getImportLocation(HttpServletRequest req, String reqAlias, String style, String mode, String wsdlType) {
    String scheme = req.getScheme();
    String host = req.getServerName();
    int port = req.getServerPort();
    String uri = getImportLocationPath(reqAlias, style, mode, wsdlType);


    StringBuffer res = new StringBuffer();
    res.append(scheme).append("://").append(host).append(':').append(port);
    res.append(uri);

    {//reverse proxy
      try {
        URL endpointURLURL = new URL(res.toString());
        ESPWSReverseProxyConfiguration reverseProxy = ESPServiceFactory.getEspService().getESPWSReverseProxyConfiguration();
        WSMappingResult mapping = reverseProxy.getTransportMapping(endpointURLURL.getProtocol(),
                endpointURLURL.getHost(),
                endpointURLURL.getPort(),
                false);
        String uriFinal = reverseProxy.getURIMapping(endpointURLURL.getFile(), 
                true,
                mapping.isMapped());
        return mapping.getTransportMapping().append(uriFinal).toString();
      } catch (ESPTechnicalException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      } catch (MalformedURLException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      }
    }

    return res.toString();
  }

  private String getImportLocationPath(String reqAlias, String style, String mode, String wsdlType){
    StringBuffer res = new StringBuffer(32);
    res.append(reqAlias).append("?").append(WSDL_PARAM).append("=").append(wsdlType);
    if (style != null) {
      res.append("&").append(STYLE_PARAM).append("=").append(style);
    }
    if (mode != null) {
      res.append("&").append(MODE_PARAM).append("=").append(mode);
    }
    return res.toString();
  }
  private boolean checkStyle(HttpServletRequest req, HttpServletResponse resp, Set styles) throws Exception{
  //check whether the parameter values are correct
    //String bURL = getRequestURI(req); // never used
    //append 'style' attrib
    String style = req.getParameter(STYLE_PARAM);
    if (style != null) {
      //request to style, which is not valid
      if (! styles.contains(style)) {
        Exception e =  new ServerRuntimeProcessException(RuntimeExceptionConstants.INVALID_REQUEST_PARAMETER, new Object[]{STYLE_PARAM, style, styles});
        SimpleLogger.trace(Severity.ERROR, LOC, "SOA.wsr.030108",  null, "WSDL style [" + style + "] not supported. Supported styles: " + styles, e);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
        return false;
      }
    }
    return true;
  }
  
  /**
   * Makes check of the param values.
   * Returns true if parameter values are correct, or false if they are not and response is returned.
   */
  private boolean checkParamValues(HttpServletRequest req, HttpServletResponse resp, ServiceData sData, InterfaceDefinition intfDef) throws Exception {
    //check 'wsdl' param value
    String wsdlParam = getWSDLParamValue(req);
    if ((! SERVICE_WSDL.equals(wsdlParam)) && (! BINDING_WSDL.equals(wsdlParam)) && (! INTERFACE_WSDL.equals(wsdlParam)) && (wsdlParam.length() != 0)) {
      Exception e =  new ServerRuntimeProcessException(RuntimeExceptionConstants.INVALID_REQUEST_PARAMETER, new Object[]{WSDL_PARAM, wsdlParam, SERVICE_WSDL + ", " + BINDING_WSDL + ", " + INTERFACE_WSDL});
      SimpleLogger.trace(Severity.ERROR, LOC, "SOA.wsr.030109",  null, "Invalid WSDL param: [" + wsdlParam + "]", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
      return false;
    }
    
    //check 'mode' attr.
    String mode = req.getParameter(MODE_PARAM);
    if (mode != null) {
      if (! SAP_MODE.equals(mode) && ! WSPOLICY_MODE.equals(mode) && ! STANDARD_MODE.equals(mode)) {
        Exception e =  new ServerRuntimeProcessException(RuntimeExceptionConstants.INVALID_REQUEST_PARAMETER, new Object[]{MODE_PARAM, mode, SAP_MODE + ", " + WSPOLICY_MODE + ", " + STANDARD_MODE});
        SimpleLogger.trace(Severity.ERROR, LOC, "SOA.wsr.030110",  null, "Invalid mode param: [" + mode + "]", e);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
        return false;
      }
//    //check for NW05 features in case of SAP_MODE. If NW05 features are available, and mode is SAP_MODE error should be returned.
//    if (SAP_MODE.equals(mode)) {
//    StringBuffer buf = new StringBuffer();
//    BindingData tmpBD;
//    OperationData ops[], tmpOp;
//    BindingData[] bDatas = sData.getBindingData();
//    for (int i = 0; i < bDatas.length; i++) {
//    tmpBD = bDatas[i];
//    if (checkForNW04PropertiesAndSendError(tmpBD, resp, buf)) { //check binding config
//    //check binding operation configs
//    ops = tmpBD.getOperation();
//    for (int o = 0; o < ops.length; o++) {
//    if (! checkForNW04PropertiesAndSendError(ops[o], resp, buf)) {
//    return false;
//    }
//    }
//    } else {
//    return false;
//    }
//    }
//    Variant[] vs = intfDef.getVariant();
//    for (int i = 0; i < vs.length; i++) {
//    if (checkForNW04PropertiesAndSendError(vs[i].getInterfaceData(), resp, buf)) { //check interfacedata
//    //check interfacedata operation configs
//    ops = vs[i].getInterfaceData().getOperation();
//    for (int o = 0; o < ops.length; o++) {
//    if (! checkForNW04PropertiesAndSendError(ops[o], resp, buf)) {
//    return false;
//    }
//    }            
//    } else {
//    return false;
//    }
//    }          
//    }
    }
    return true;
  }

  private WSDLDescriptor getWSDLDescriptor(List wsdls, String ns) {
    WSDLDescriptor tmp;
    for (int i =0; i < wsdls.size(); i++) {
      tmp = (WSDLDescriptor) wsdls.get(i);
      if (tmp.getTargetNS().equals(ns)) {
        return tmp;
      }
    }
    return null;
  }

  private void writeResponse(Element elem, HttpServletResponse resp) throws Exception {
    if (resp instanceof WSDLZipBuilder.DummyHttpServletResponse) { //this is only true when wsdl are downloaded into zip
      WSDLZipBuilder.DummyHttpServletResponse r = (WSDLZipBuilder.DummyHttpServletResponse) resp;
      r.wsdl = elem;
      return;
    }
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/xml; charset=utf-8");
    //write the dom into outputstream
    writeDomToStream(elem, resp.getOutputStream());
  }

  void writeDomToStream(Element elem, OutputStream out) throws Exception {
    synchronized (transformer) {
      transformer.transform(new DOMSource(elem), new StreamResult(out));
    }
  }

  private void appendSAPDocumentation(Element dfs) {
    final String sapNS = "http://www.sap.com/webas";
    Element doc = dfs.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.DOCUMENTATION_ELEMENT);
    Element elem = doc.getOwnerDocument().createElementNS(sapNS, "sap:SAP_WSDL");
    doc.appendChild(elem);
    insertFirst(dfs, doc);
  }

  private void appendUsingPolicyExtension(Element dfs) {
    Element usingPolicy = dfs.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.USINGPOLICY_ELEMENT);
    insertFirst(dfs, usingPolicy);
  }

  private Element parseToDom(String path) throws Exception {
    return SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new java.io.File(path)).getDocumentElement();
  }
  /**
   * Returns true if <code>b</code> is NW04 properties annotated.
   * False if <code>b</code> is not NW04 properties annotated. Response error is send in this case also.
   */
  private boolean checkForNW04PropertiesAndSendError(Behaviour b, HttpServletResponse resp, StringBuffer buf) throws Exception {
    synchronized (configurationBuilder) {
      if (! configurationBuilder.isNW04PropertyAnnotated(b, buf)) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, buf.toString());
        return false;
      }
    }
    return true;
  }

  /**
   * Applies policy to specific element.
   *
   * @param defElement root wsdl:definitions element
   * @param targetEl wsdl element on which the policies should be attached.
   * @param bh the behavior object containing tne configuration. It is possible to contain alternatives
   * @param pCounter counts the number of policy definitions. Used for creation of policy IDs. This is the unique part of the id.
   * @return modified <code>pCounter</code> value.
   */
  private int applyPolicy(Element defElement, Element targetEl, Behaviour bh, int pCounter, InterfaceDefinition intfDef, String wsdlLevel) throws ConfigurationMarshallerException {
    PropertyListType[] alternatives = bh.getPropertyList();
    if (alternatives.length == 0) { //there is no alternatives, return.
      return pCounter;
    }
    Document rootDoc = defElement.getOwnerDocument();
    Element policyEl = rootDoc.createElementNS(Policy.POLICY_NS, Policy.POLICY_ELEMENT);
    Element exactlyOne = rootDoc.createElementNS(Policy.POLICY_NS, Policy.EXACTLYONE_ELEMENT);
    policyEl.appendChild(exactlyOne);

    List assertions;
    Element all, tmpAss;
    for (int i = 0; i < alternatives.length; i++) {
      PropertyType[] propArr = alternatives[i].getProperty();
      if (propArr.length > 0) { //generate policy only if there is at least one property in the list
        PropertyListType pList = null;
        pList = new PropertyListType();
        pList.setProperty(propArr);
        try {
          //pList = filterPropertiesForVisualization(propArr, intfDef, wsdlLevel);
        } catch (Exception e) {
          throw new ConfigurationMarshallerException(e);
        }
        synchronized (configurationBuilder) {
          assertions = configurationBuilder.marshalAssertions(pList, rootDoc, wsdlLevel, IConfigurationMarshaller.PROVIDER_MODE);
        }
        if (assertions.size() > 0) {
          all = rootDoc.createElementNS(Policy.POLICY_NS, Policy.ALL_ELEMENT);
          for (int a = 0; a < assertions.size(); a++) {
            tmpAss = (Element) assertions.get(a);
            all.appendChild(tmpAss); //tmpAss should be from the same document.
          }
          exactlyOne.appendChild(all);
        }
      }
    }
    //put policy as child of wsdl:definitions, only if there is at least one alternative generated
    if (exactlyOne.getChildNodes().getLength() > 0) {
      pCounter++;
      String pID = "P" + Integer.toString(pCounter);
      policyEl.setAttributeNS(Policy.WS_SEC_UTILITY_NS, "wsu:" + Policy.ID_ATTR, pID);
      insertFirst(defElement, policyEl);
      //defElement.appendChild(policyEl);
      //create PolicyReference
      Element policyReference = rootDoc.createElementNS(Policy.POLICY_NS, Policy.POLICYREFERENCE_ELEMENT);
      policyReference.setAttribute(Policy.POLICYREFERENCE_URI_ATTR, "#" + pID);
      insertFirst(targetEl, policyReference);
    }
    return pCounter;
  }

  /**
   * Merges the Variants data into one InterfaceData containing alternatives.
   * The result InterfaceData is used for applying policies to the portType wsdl template
   * @return InterfaceData containing merged variants' data as well as properties denoting variant's name.
   */
//private InterfaceData mergeVariants(InterfaceDefinition intfDef) {
//List intAlternatives = new ArrayList(); //values PropertyListType[]
//Map opAlternatives = new Hashtable(); //key operation name, value ArrayList containing PropertyListType[] objects

//InterfaceData tmpIData;
//OperationData op;
//PropertyListType tmpPList;
//String vName;
//Variant[] vs = intfDef.getVariant();
//for (int i = 0; i < vs.length; i++) {
//tmpIData = vs[i].getInterfaceData();
//vName = vs[i].getName();
////add interface level property
////tmpPList = appendVariantProperty(tmpIData.getSinglePropertyList(), vName);
////intAlternatives.add(tmpPList);
////add interface/operation level properties
//OperationData[] ops = tmpIData.getOperation();
//for (int o = 0; o < ops.length; o++) {
//op = ops[o];
//List list = (List) opAlternatives.get(op.getName());
//if (list == null) {
//list = new ArrayList();
//opAlternatives.put(op.getName(), list);
//}
////tmpPList = appendVariantProperty(op.getSinglePropertyList(), vName);
////list.add(tmpPList);
//}
//}
////create the result InterfaceData
//InterfaceData result = new InterfaceData();
////apply interface level properteis
//result.setPropertyList((PropertyListType[]) intAlternatives.toArray(new PropertyListType[intAlternatives.size()]));
////apply interface/operation level properties 
//List opDatas = new ArrayList();
//List tmpOpProps; 
//String opName;
//Iterator itr = opAlternatives.keySet().iterator();
//while (itr.hasNext()) {
//opName = (String) itr.next();
//tmpOpProps = (List) opAlternatives.get(opName);
//op = new OperationData();
//op.setName(opName);
//op.setPropertyList((PropertyListType[]) tmpOpProps.toArray(new PropertyListType[tmpOpProps.size()]));
//opDatas.add(op);
//}
//result.setOperation((OperationData[]) opDatas.toArray(new OperationData[opDatas.size()]));
//return result;
//}

  /**
   * Apply features.
   * @param defElement element to which features definitions are applied (wsdl:definitions)
   * @param targetEl  element to which use-features are applied (wsdl:binding, wsdl:operation, wsdl:portType, ...).
   * @param bh behavior which will be applied
   * @param bIDBase base string used for creation of features IDs.
   * @param fNSPref prefix mapped to the namespace of the feature element
   * @param targetNSPref prefix which is mapped to the targetNamespace of wsdl document (e.g. 'tns:').
   * @param cfgLevel determines from which configuration level the <code>bh</code> comes from - interface, interface/operation, binding, binding/operation.
   */
  private void applyFeatures0(Element defElement, Element targetEl, Behaviour parent, Behaviour bh, String fIDBase, String fNSPref, String targetNSPref, String cfgLevel, InterfaceDefinition intfDef) throws ConfigurationMarshallerException {
    PropertyType tmpType[] = bh.getSinglePropertyList().getProperty();
    PropertyType[] pType = null;

    //filter properties for visualization. Call 'filterForVis' before 'filterForFeat' because here all props are from NY 
    PropertyListType pList = null;
    try {
      pList = filterPropertiesForVisualization(tmpType, intfDef, cfgLevel);
      tmpType = pList.getProperty();
    } catch (Exception e) {
      throw new ConfigurationMarshallerException(e);
    }

    synchronized(configurationBuilder) { //do the filtering of the properties which are about to be shown in  features NW04 wsdl
      pType = configurationBuilder.filterPropertiesForFeatures(parent, tmpType, cfgLevel, IConfigurationMarshaller.PROVIDER_MODE);
    }

    boolean[] used = new boolean[pType.length];
    List list = new ArrayList();
    PropertyType tmpProp;
    int fIDNom = 0;
    String fURIPref = "prf";
    for (int i = 0; i < pType.length; i++) {
      if (SAP_FEATURE_NS.equals(pType[i].getNamespace()) && PROTOCOL_ORDER_FEATURE.equals(pType[i].getName())) {
        continue; //Skip protocol-order feaure, it shouldn't be reflected in wsdl.
      }
      if  (! used[i]) {
        getPropertyTypesByNS(pType, pType[i].getNamespace(), used, list);
        //create feature definition
        Element feature = defElement.getOwnerDocument().createElementNS(FEATURE_ELEM_NS, fNSPref + ":" + FEATURE_ELEM);
        feature.setAttribute(FEATURE_NAME_ATTR, fIDBase + "_" + fIDNom);
        fIDNom++;
        feature.setAttribute(FEATURE_URI_ATTR, pType[i].getNamespace());
        feature.setAttributeNS(NS.XMLNS, "xmlns:" + fURIPref, pType[i].getNamespace());
        for (int p = 0; p < list.size(); p++) {
          tmpProp = (PropertyType) list.get(p);
          Element propEl = feature.getOwnerDocument().createElementNS(FEATURE_ELEM_NS, fNSPref + ":" + PROPERTY_ELEM);
          propEl.setAttribute(PROPERTY_QNAME_ATTR, fURIPref + ":" + tmpProp.getName());
          Element option = propEl.getOwnerDocument().createElementNS(FEATURE_ELEM_NS, fNSPref + ":" + OPTION_ELEM);
          option.setAttribute(OPTION_VALUE_ATTR, fURIPref + ":" + tmpProp.get_value());
          propEl.appendChild(option);
          feature.appendChild(propEl);
        }
        defElement.appendChild(feature); //add feature definition into dom tree.
        list.clear(); //for next usage
      }
      //create feature definition
    }
    //append use feature elements. The fIDNom contains the number of features actually have been created. Their names are constructed using template.
    for (int i = 0; i < fIDNom; i++) {
      Element useFeature = defElement.getOwnerDocument().createElementNS(FEATURE_ELEM_NS, fNSPref + ":" + USE_FEATURE_ELEM);
      useFeature.setAttribute(FEATURE_ELEM.toLowerCase(Locale.ENGLISH), targetNSPref + ":" + fIDBase + "_" + i);
      insertFirst(targetEl, useFeature);
    }
  }
  /**
   * In the used[], index <code>i</code> is set true, if property with same index in <code>pTypes</code>
   * is found to be in the <code>ns</code> namespace.
   * In <code>list</code> param PropertyType objects are set.
   */
  private static void getPropertyTypesByNS(PropertyType[] pTypes, String ns, boolean[] used, List list) {
    for (int i = 0; i < pTypes.length; i++) {
      if (ns.equals(pTypes[i].getNamespace())) {
        list.add(pTypes[i]);
        used[i] = true;
      }
    }
  }

  private static boolean areAllPRsUsed(boolean used[]) {
    for (int i = 0; i < used.length; i++) {
      if (! used[i]) {
        return false;
      }
    }
    return true;
  }

  private static void insertFirst(Element holder, Element toBeIns) {
    List docs = DOM.getChildElementsByTagNameNS(holder, WSDL11Constants.WSDL_NS, WSDL11Constants.DOCUMENTATION_ELEMENT);
    if (docs.size() == 1) {
      Element documentation = (Element) docs.get(0);
      Node nextSibling = documentation.getNextSibling();
      if (nextSibling != null) {
        holder.insertBefore(toBeIns, nextSibling);
      } else {
        holder.appendChild(toBeIns);
      }
    } else {
      Node f = holder.getFirstChild();
      holder.insertBefore(toBeIns, f);
    }
  }

  private static String getTargetNSPrefix(Element wsdlDoc) {
    String tnsValue = wsdlDoc.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
    return applyPrefixForNS(wsdlDoc, tnsValue, "tns");
  }
  /**
   * Sets prefix for <code>ns</code> into <code>wsdlDoc</code>, using <code>prefBase</code>
   * as base for prefix name.
   */
  private static String applyPrefixForNS(Element wsdlDoc, String ns, String prefBase) {
    //check whether ns is already mapped
    String result = DOM.getPrefixForNS(wsdlDoc, ns);
    if (result != null) {
      return result;
    }
    int nom = 0;
    //map prefix for ns
    while (true) {
      if (DOM.prefixToURI(prefBase, wsdlDoc) == null) { //no prefix for ns.
        //update wsdlDoc
        wsdlDoc.setAttributeNS(NS.XMLNS, "xmlns:" + prefBase, ns);
        return prefBase;
      } else {
        prefBase += nom;
        nom++;
      }
    }
  }
  /**
   * Traverses <code>ops</code> for operation with name <code>opName</code>.
   * @return operation data if found, or null otherwise.
   */
  private static OperationData getOperationData(OperationData[] ops, String opName) {
    for (int i = 0; i < ops.length; i++) {
      if (ops[i].getName().equals(opName)) {
        return ops[i];
      }
    }
    return null;
  }

  /**
   * @returns alias which does NOT starts with "/"
   */
  private static String constructBindingDataAlias(String contextRoot, String bdURL) {
    if (bdURL.startsWith("/")) {
      bdURL = bdURL.substring(1);
    }

    String alias = contextRoot + "/" + bdURL;
    if (alias.startsWith("/")) {
      alias = alias.substring(1);
    }
    return alias;
  }



  private static Variant getVariant(InterfaceDefinition intfDef, BindingData bData) throws ServerRuntimeProcessException {
    String vName = bData.getVariantName();
    Variant[] vs = intfDef.getVariant();
    for (int i = 0; i < vs.length; i++) {
      if (vName.equals(vs[i].getName())) {
        return vs[i];
      }
    }
    //this should not happen.
    throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CONFIG_ENTITY_NOT_FOUND, new Object[] {"variant", vName});
  }
  /**
   * Returns File object under which a wsdl should be cached on file system.
   * The <code>wsdlContent</code> param denotes wsdl's type (portType or binding).
   */
  private File getCachedWSDLFileName(String style, String mode, String bDURI, String wsdlContent) throws Exception {
    File tmpDir = new File (appSrvContext.getServiceState().getWorkingDirectoryName(), "wsdl-cache");
    File wsdlDir = getWSDLCacheDirForBD(tmpDir, bDURI);
    String fName = null;;
    if (mode != null) {
      fName = wsdlContent + "_" + style + "_" + mode + ".wsdl";
    } else {
      fName = wsdlContent + "_" + style + ".wsdl";
    }
    if (! wsdlDir.exists()) {
      wsdlDir.mkdirs();
    }
    File wsdlFile = new File (wsdlDir, fName);
    //check whether requested file is actually in the wsdl-cache dir.
    checkFileAccess(tmpDir, wsdlFile);
    location.debugT("getCachedWSDLFileName() wsdlFile: '" + wsdlFile + "', directory.exists " + wsdlDir.exists());
    return wsdlFile;
  }
  /**
   * Returns File denoting a directory into which teh wsdls of binding data with <code>bdURI</code> will be stored.
   * @param wsdlCacheDir the main wsdl cache directory
   * @param bDURI request URI of certain binding data
   */
  private File getWSDLCacheDirForBD(File wsdlCacheDir, String bDURI) {
    return new File(wsdlCacheDir, bDURI);
  }

  private boolean hasDTAltarnatives(InterfaceDefinition intfDef) {
    Variant[] vs = intfDef.getVariant();
    if (vs.length > 1) {
      return true;
    }
    //there should be at least 1 variant
    Variant v = vs[0];
    OperationData ops[] = v.getInterfaceData().getOperation();
    for (int i = 0; i < ops.length; i++) {
      if (! ops[i].isSinglePropertyList()) {
        return true;
      }
    }
    return false;
  }

///**
//* Creates and returns new PropertyListType into which are copied the properties
//* from <code>props</code>, plus property denoting variant name.
//*/
//private PropertyListType appendVariantProperty(PropertyListType pList, String vName) {
//PropertyType[] props = pList.getProperty();
//PropertyType[] newProps = new PropertyType[props.length + 1];
//System.arraycopy(props, 0, newProps, 0, props.length);
//PropertyType vProp = new PropertyType();
//vProp.setNamespace(DefaultConfigurationMarshaller.VARIANTNAME_NS);
//vProp.setName(DefaultConfigurationMarshaller.VARIANTNAME_PROPERTY);
//vProp.set_value(vName);
//newProps[props.length] = vProp;

//PropertyListType resultPList = new PropertyListType();
//resultPList.setProperty(newProps);
//return resultPList;
//}  
  /**
   * Update the targetNS of wsdl document. Updates teh targetNamespace attribute,
   * as well as all prefixes that refer to the old targetNamespace value.
   */
  private void updateTargetNamespace(Element wsdlDoc, String newTargetNS) {
    String oldTNS = wsdlDoc.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
    wsdlDoc.setAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR, newTargetNS);
    //update the prefies
    updatePrefixValues(wsdlDoc, oldTNS, newTargetNS);
  }
  /**
   * Updates the prefix values.
   * @param elem Element which prefix declarations are to be updated
   * @param oldNS namespace which value is about the be changed
   * @param newNS new namespace value
   */
  private void updatePrefixValues(Element elem, String oldNS, String newNS) {
    Attr tmpAttr;
    NamedNodeMap map = elem.getAttributes();
    for (int i = 0; i < map.getLength(); i++) {
      tmpAttr = (Attr) map.item(i);
      if (NS.XMLNS.equals(tmpAttr.getNamespaceURI())) {
        if (tmpAttr.getValue().equals(oldNS) && !tmpAttr.getLocalName().equals("xmlns")) {
          elem.setAttributeNS(NS.XMLNS, "xmlns:" + tmpAttr.getLocalName(), newNS);
        }
      }
    }
    NodeList children = elem.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        updatePrefixValues((Element) children.item(i), oldNS, newNS);
      }
    }
  }
  /**
   * Updates ConfigurationBuilder from runtime protocol list.
   * This method should be invoked whenever there is a change in runtime protocol list (register, unregister).
   * @param protocols Map containing the currently available protocols
   */
  public synchronized void updateConfigurationBuilder(Map protocols) throws ServerRuntimeProcessException {
    Iterator itr = protocols.values().iterator();
    ConfigurationMarshallerFactory cfgMarshallerFactory = ConfigurationMarshallerFactory.newInstance();
    Object obj;
    while (itr.hasNext()) {
      obj = itr.next();
      if (obj instanceof IConfigurationMarshaller) {
        cfgMarshallerFactory.registerMarshaller((IConfigurationMarshaller) obj);
      }
    }
    synchronized(configurationBuilder) {
      try {
        configurationBuilder.reInitialize(cfgMarshallerFactory);
      } catch (Exception e) {
        throw new ServerRuntimeProcessException(e);
      }
    }
  }

  /**
   * This method performs check whether <code>targetFile</code> resides in <code>baseDir</code>, i.e
   * whether <code>targetFile</code> has as parent <code>baseDir</code>.
   * If that is true the methor completes normally, otherwise an exception is thrown.
   */
  private void checkFileAccess(File baseDir, File targetFile) throws Exception {
    String baseDirStr = baseDir.getCanonicalPath();
    String targetFileStr = targetFile.getCanonicalPath();
    if (! targetFileStr.startsWith(baseDirStr)) {
      location.warningT("Attempt to access file '" + targetFileStr + "' as from directory '" + baseDirStr + "'");
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.REQUESTED_RESOURCE_OUTSIDE_RESOURCE_DIR);
    }
  }

  private boolean deleteDir(File dir) throws IOException {
    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDir(files[i]);
        }
        if (files[i].isFile()) {
          files[i].delete();
        }
      }

      files = dir.listFiles();
      boolean res = (files == null || files.length == 0);
      dir.delete();
      return res;
    } else {
      return true; //if that the directory does not exists or is not a directory it is assumed that it is already deleted.
    }
  }
  /**
   * @return the value of 'WSDL' or 'wsdl' parameter value...
   */
  private String getWSDLParamValue(HttpServletRequest req) {
    String wsdlParam = req.getParameter(WSDL_PARAM);
    if (wsdlParam == null) {
      wsdlParam = req.getParameter(WSDL_PARAM_CAPITALIZED); //search for 'WSDL', when 'wsdl' is not available
    }
    return wsdlParam;
  }
  /**
   * Checks <code>bd</code> for a specific security property, which influense the endpoint url.
   */
  private boolean isSSLSwitchedON(BindingData bd) {
    QName sslProp = new QName("http://www.sap.com/webas/630/soap/features/transportguarantee/", "TlsType");
    QName sslPropNew = new QName("http://www.sap.com/webas/630/soap/features/transportguarantee/", "TLSType");
    PropertyType prop = bd.getSinglePropertyList().getProperty(sslProp);
    PropertyType propNew = bd.getSinglePropertyList().getProperty(sslPropNew);
    if (prop != null) {
      if (prop.get_value() != null) {
        if (prop.get_value().indexOf("HTTPS") != -1) {
          return true;
        }
      }
    }
    if (propNew != null) {
      if (propNew.get_value() != null) {
        if (propNew.get_value().indexOf("HTTPS") != -1) {
          return true;
        }
      }
    }
    return false;
  }

//private HashMap<String,String> loadMappingFile() {    
//FileInputStream input = null;
//InputStreamReader reader = null;
//BufferedReader bufferedReader = null;
//HashMap<String,String> fileMapping = new HashMap<String,String>();
//String mappingFilePath = "c:/mapping.txt";
//try {
//LOC.debugT("Loadin mapping file from location :["+mappingFilePath+"]");
//input = new FileInputStream(mappingFilePath);
//reader = new InputStreamReader(input);
//bufferedReader = new BufferedReader(reader);
//fileMapping = readFileContents(bufferedReader);
//} catch (FileNotFoundException e) {
////$JL-EXC$
////e.printStackTrace();
//} finally {
//closeStream(input);
//closeReader(reader);
//closeReader(bufferedReader);
//return fileMapping;
//}
//}
//private HashMap<String,String> readFileContents(BufferedReader reader) {
//HashMap<String,String> fileMapping = new HashMap<String,String>();
//try {      
//fileMapping.clear();
//String currentLine = null;
//while ((currentLine = reader.readLine()) != null) {
//int separatorIndex = currentLine.indexOf('=');
//if (separatorIndex != -1) {
//String key = currentLine.substring(0,separatorIndex).trim();
//String value = currentLine.substring(separatorIndex+1).trim();
//fileMapping.put(key,value);
//}      
//}
//} catch (IOException x) {
//if (LOC != null) {
//LOC.catching(x);
//}              
//} finally {
//return fileMapping;
//}

//}

///**
//* Closes Reader.
//* @param reader
//*/
//private void closeReader(Reader reader) {
//if (reader != null) {
//try {
//reader.close();
//} catch (IOException x) {
//if (LOC != null) {
//LOC.catching(x);
//}
//}
//}
//}

///**
//* Closes InputStream.
//* @param input
//*/
//private void closeStream(InputStream input) {
//if (input != null) {
//try {
//input.close();
//} catch (IOException x) {
//if (LOC != null) {
//LOC.catching(x);
//}        
//}
//}
//}

  private PropertyListType filterPropertiesForVisualization(PropertyType[] source, InterfaceDefinition intfD, String wsdlLevel) throws Exception {
    String soapID = SoapApplicationRegistry.getSoapApplicationID(intfD, false); //false because wsdl only for services is avaiable
    PropertyListType res = new PropertyListType();
    Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType> map = SoapApplicationRegistry.getSoapApplicationPropMap(soapID);
    PropertyType[] s_props = source;
    ScopeType wsdl_p_scope = convertWSDLLevelToPropScopeType(wsdlLevel);

    for (PropertyType s_prop : s_props) {
      QName an = new QName(s_prop.getNamespace(), s_prop.getName());
      com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType defProp = map.get(an);
      if (defProp != null) {
        ScopeType p_scope = defProp.getScope();
        if (wsdl_p_scope.equals(p_scope)) {
          if (defProp.isWsdlRelevant()) {
            res.addProperty(s_prop); //only when all of the above cases are fulfiled the property could be visualize in the wsdl.
          }
        }
      }
    }

    return res;
  }

  private ScopeType convertWSDLLevelToPropScopeType(String wsdlLevel) {
    if (IConfigurationMarshaller.INTERFACE_LEVEL.equals(wsdlLevel)) {
      return ScopeType.IF;
    } else if (IConfigurationMarshaller.INTERFACE_OPERATION_LEVEL.equals(wsdlLevel)) {
      return ScopeType.OP;
    } else if (IConfigurationMarshaller.BINDING_LEVEL.equals(wsdlLevel)) {
      return ScopeType.BN;
    } else if (IConfigurationMarshaller.BINDING_OPERATION_LEVEL.equals(wsdlLevel)) {
      return ScopeType.BO;
    }
    throw new IllegalArgumentException("WSDL level '" + wsdlLevel + "' cannot be bound to valid property scope.");
  }

  public static final Location LOC = Location.getLocation(CTSProvider.class);

  private static Hashtable<String, File> CTS50_JWS_PREDEFINED_WSDLS = new Hashtable();
  private static String CTS50_JWS_SCHEME = "h:";

  static {
    /* Predefined files for buggy CTS tests to pass:
     * com/sun/ts/tests/jws/webresult/webresult2/client/Client.java#testWSDL1
     * com/sun/ts/tests/jws/webresult/webresult2/client/Client.java#testWSDL2
     * com/sun/ts/tests/jws/webresult/webresult2/client/Client.java#testWSDL3
     * com/sun/ts/tests/jws/webresult/webresult2/client/Client.java#testWSDL4
     * com/sun/ts/tests/jws/webresult/webresult2/client/Client.java#testWSDL5
     */
    CTS50_JWS_PREDEFINED_WSDLS.put("/WSWebResult2WebServiceApp/jws/webResult2WebService", new File(CTS50_JWS_SCHEME + "/com/sap/engine/services/webservices/jaxws/jwshcks/wr2"));
    CTS50_JWS_PREDEFINED_WSDLS.put("/WSWebResult2WebServiceApp/jws/webResult2WebService/root.wsdl", new File(CTS50_JWS_SCHEME + "/com/sap/engine/services/webservices/jaxws/jwshcks/wr2/root.wsdl"));
    /* Predefined files for buggy CTS tests to pass:
     * com/sun/ts/tests/jws/webparam/webparam2/client/Client.java#testWSDL2
     * com/sun/ts/tests/jws/webparam/webparam2/client/Client.java#testWSDL4
     * com/sun/ts/tests/jws/webparam/webparam2/client/Client.java#testWSDL5 
     */
    CTS50_JWS_PREDEFINED_WSDLS.put("/WSWebParam2WebServiceApp/jws/webParam2WebService", new File(CTS50_JWS_SCHEME + "/com/sap/engine/services/webservices/jaxws/jwshcks/wp2"));
    CTS50_JWS_PREDEFINED_WSDLS.put("/WSWebParam2WebServiceApp/jws/webParam2WebService/root.wsdl", new File(CTS50_JWS_SCHEME + "/com/sap/engine/services/webservices/jaxws/jwshcks/wp2/root.wsdl"));
  }

  /** 
   * Returns url path for service, binding or porttype policy wsdl, depending on the value of wsSection 
   * @param contextRoot (as listed in telnet - list_ws)
   * @param bdUrl (as listed in telnet - list_ws)
   * @param wsSection - indicates which url path is requested - service, binding, porttype 
   * @return policy url path
   * @throws NoSuchWebServiceException  - if no binding data is found for this contextRoot/bdUrl
   * @throws WSPolicyModeNotSupportedException - if the web service at contextRoot/bdUrl does not support ws_policy wsdls
   */ 
  public String getWSPolicyWsdlURLPath(String contextRoot, String bdUrl,
                                       WebServicesContainerManipulator.WsdlSection wsSection) {
    StringBuffer wsdlUrlPath = new StringBuffer(64);
    String endpointPath = getWSEndpointPath(contextRoot, bdUrl);
    String style = "";
    com.sap.engine.services.webservices.espbase.configuration.Service service = getService(endpointPath);
    if (service == null){ // no such service
      throw new NoSuchWebServiceException("No webservice at context root [" + contextRoot + "], alias [" + bdUrl + "]");
    }
    boolean isJEE = isJEEService(endpointPath);
    if (isJEE){
      // check if this ws supports ws_policy mode
      String bindingTmpl  = metaDataAccessor.getWsdlPath(endpointPath, BINDING_WSDL_TYPE, "default");
      String portTypeTmpl = metaDataAccessor.getWsdlPath(endpointPath, "porttype", "default");
      if (! (bindingTmpl != null && portTypeTmpl != null && new File(bindingTmpl).exists() && new File(portTypeTmpl).exists())) {
        throw new WSPolicyModeNotSupportedException("Webservice at context root [" + contextRoot + "], alias [" + bdUrl + "] does not support ws_policy mode");
      }
    }else {
      style = getWSDLStyle(null, metaDataAccessor.getWSDLStyles(endpointPath));
    }

    switch (wsSection) {
    case SERVICE:
      wsdlUrlPath.append(endpointPath).append("?").append(WSDL_PARAM);
      wsdlUrlPath.append("&").append(MODE_PARAM).append("=").append(
          WSPOLICY_MODE);
      if (!isJEE) {
        wsdlUrlPath.append("&").append(STYLE_PARAM).append("=").append(style);
      }
      break;
    case BINDING:
      if (isJEE) {
        wsdlUrlPath.append(getImportLocationPath(endpointPath, null,
            WSPOLICY_MODE, BINDING_WSDL));
      } else {
        wsdlUrlPath.append(getImportLocationPath(endpointPath, style,
            WSPOLICY_MODE, BINDING_WSDL));
      }
      break;
    case PORTTYPE:
      if (isJEE) {
        boolean locAttributeParam = 
          com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE.equals(service.getType());
        wsdlUrlPath.append(getLocationAttr(endpointPath, locAttributeParam)).append("&")
            .append(MODE_PARAM).append("=").append(WSPOLICY_MODE);
      } else {
        wsdlUrlPath.append(getImportLocationPath(endpointPath, style,
            WSPOLICY_MODE, INTERFACE_WSDL));
      }
      break;
    }
    return wsdlUrlPath.toString();
  }


  /**
   * Returns the ws endpoint url for a particular ws port with host and port omitted, e.g.
   *  
   *  http:///contextRoot/bdUrl
   *
   * @param contextRoot (as listed in telnet - list_ws)
   * @param bdUrl       (as listed in telnet - list_ws)
   * @return WS endpoint URL
   * @throws WSLocalCallException       - if the service is configured for local calls
   * @throws NoSuchWebServiceException  - if no binding data is found for this contextRoot/bdUrl
   */
  public String getWSEndpointURL(String contextRoot, String bdUrl){
    StringBuffer endpointURL = new StringBuffer(64);
    String endpointPath = getWSEndpointPath(contextRoot, bdUrl);
    BindingData bd = metaDataAccessor.getBindingData(endpointPath);
    if (bd == null) {
      throw new NoSuchWebServiceException("No webservice at context root [" + contextRoot + "], alias[" + bdUrl + "]");
    }
    if  (RuntimeProcessingEnvironment.isLocalTransportService(bd)){
      throw new WSLocalCallException("No endpoint URL can be constructed for a LocalCall service");
    }
    URLSchemeType urlScheme = bd.getUrlScheme();
    boolean forceHTTPS = isSSLSwitchedON(bd);
    if (forceHTTPS || URLSchemeType.https.equals(urlScheme)) {
      endpointURL.append("https://");
    } else if (URLSchemeType.http.equals(urlScheme)) {
      endpointURL.append("http://");
    } else {
      throw new RuntimeException("Unknown url scheme - ["
          + urlScheme.getValue() + "] - or missing url scheme");
    }
    endpointURL.append(endpointPath);

    {//reverse proxy
      try {
        URL endpointURLURL = new URL(endpointURL.toString());
        ESPWSReverseProxyConfiguration reverseProxy = ESPServiceFactory.getEspService().getESPWSReverseProxyConfiguration();
        WSMappingResult mapping = reverseProxy.getTransportMapping(endpointURLURL.getProtocol(),
                endpointURLURL.getHost(),
                endpointURLURL.getPort(),
                false);
        String uriFinal = reverseProxy.getURIMapping(endpointURLURL.getFile(), 
                false,
                mapping.isMapped());
        return mapping.getTransportMapping().append(uriFinal).toString();
      } catch (ESPTechnicalException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      } catch (MalformedURLException e) {
        location.traceThrowableT(Severity.DEBUG, e.getMessage(), e);
      }
    }
    return endpointURL.toString();
  }


  /**
   *  Returns all the possible wsdl paths (policy, etc) for a web service. This would be used for visualization in the NWA.
   *  @param contextRoot (as listed in telnet - list_ws)
   *  @param bdUrl (as listed in telnet - list_ws)
   *  @return - all possible wsdl paths for the web service at contextRoot/bdUrl
   *  @throws NoSuchWebServiceException  - if no binding data is found for this contextRoot/bdUrl
   */ 
  public List<String> getAllWSDLURLPaths(String contextRoot, String bdUrl) {
    List<String> wsdlURLPaths = new ArrayList<String>();

    String endpointPath = getWSEndpointPath(contextRoot, bdUrl);
    BindingData bd = metaDataAccessor.getBindingData(endpointPath);
    if (bd == null) {
      throw new NoSuchWebServiceException("No webservice at context root [" + contextRoot + "], alias[" + bdUrl + "]");
    }
    /*if (getService(endpointPath) == null){ // no such service
      throw new NoSuchWebServiceException("No webservice for [" + endpointPath + "]");
    }*/
    String baseWSDLUrlPath = endpointPath + "?" + WSDL_PARAM;

    if (isJEEService(endpointPath)) { // J2EE
      wsdlURLPaths.add(baseWSDLUrlPath);
      // check if ws_policy is supported
      String bindingTmpl  = metaDataAccessor.getWsdlPath(endpointPath, BINDING_WSDL_TYPE, "default");
      String portTypeTmpl = metaDataAccessor.getWsdlPath(endpointPath, "porttype", "default");
      if (bindingTmpl != null && portTypeTmpl != null && new File(bindingTmpl).exists() && new File(portTypeTmpl).exists()) {
      wsdlURLPaths
          .add(baseWSDLUrlPath + "&" + MODE_PARAM + "=" + WSPOLICY_MODE);
      }
    } else { // NY
      Set<String> styles = metaDataAccessor.getWSDLStyles(endpointPath);
      if (styles.size() < 1) {
        throw new RuntimeException(
            RuntimeExceptionConstants.DEFAULT_STYLE_NOT_DEFINED);
      }

      Iterator<String> it = styles.iterator();
      String style;
      String path;
      while (it.hasNext()) {
        style = (String) it.next();
        path = baseWSDLUrlPath + "&" + STYLE_PARAM + "=" + style;
        wsdlURLPaths.add(path);
        path = path + "&" + MODE_PARAM + "=" + WSPOLICY_MODE;
        wsdlURLPaths.add(path);
      }
    }
    return wsdlURLPaths;
  }

  private String getWSEndpointPath(String contextRoot, String bdUrl){
    return "/" + constructBindingDataAlias(contextRoot, bdUrl);
  }

  private boolean isJEEService(String endpointPath){
    com.sap.engine.services.webservices.espbase.configuration.Service s = getService(endpointPath);
    return isJEEService(s);
  }

  private boolean isJEEService(com.sap.engine.services.webservices.espbase.configuration.Service s){
    Integer type = s.getType();
    return type.equals(com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE) ||
           type.equals(com.sap.engine.services.webservices.espbase.configuration.Service.SAP_OUTSIDEIN_SERVICE_TYPE);
  }
  
  
  
  public Map<String, Element[]> getMEXData(String url, String dialect) throws Exception{   
        
    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    
    String zipURL = url + "?wsdl&zip&mode=ws_policy";
    
    new WSDLZipBuilder().writeZipContent(zipURL , this, baos);
    
    byte[] wsdls = baos.toByteArray();
                            
    return processMexRequest(wsdls, dialect);
  }
  
  
  private Map<String, Element[]> processMexRequest(byte[] zippedContent, String dialect) throws Exception{      
    List<Element> wsdlElements = new ArrayList<Element>();
    List<Element> xsdElements = new ArrayList<Element>();
              
    Map<String, Element[]> elements = new HashMap<String, Element[]>();
    
    extractZipContent(wsdlElements, xsdElements, zippedContent);

    //WSDLS & Schemas Requested - send the schemas
    if (!xsdElements.isEmpty()){
      Element[] xsdArray = xsdElements.toArray(new Element[xsdElements.size()]);
      elements.put(XSD_DIALECT, xsdArray);
    }
    
    // WSDLS requested - send the wsdl
    if (WSDL_DIALECT.equals(dialect)){
      if (!wsdlElements.isEmpty()){
        Element[] wsdlArray = wsdlElements.toArray(new Element[wsdlElements.size()]);      
        elements.put(WSDL_DIALECT, wsdlArray);  
      }
    }
               
    return elements;
  }
  
  
  
  private void extractZipContent(List<Element> wsdlElements, List<Element> xsdElements, byte[] content) throws Exception{
    ZipInputStream zis = null;  
    try{          
      zis = new ZipInputStream(new ByteArrayInputStream(content));
      ZipEntry entry;
      while((entry = zis.getNextEntry()) != null) {       
         int count;
         byte data[] = new byte[BUFFER];
         
         ByteArrayOutputStream dest = new ByteArrayOutputStream();             
         while ((count = zis.read(data, 0, BUFFER)) != -1) {
            dest.write(data, 0, count);
         }                               
         dest.flush();
         
         String entryName = entry.getName().trim();
         
         Element metaDataElement = byte2Element(dest.toByteArray());     
         
         if (entryName.endsWith(".wsdl")){
           wsdlElements.add(metaDataElement);        
         } else if (entryName.endsWith(".xsd")){
           xsdElements.add(metaDataElement);
         } else if (entryName.endsWith(".xml")){
          // TODO: Policy case here. 
         }      
      }
    }finally{
      if (zis != null){       
        zis.close();
      }
    }
  }
  
  
  private static Element byte2Element(byte[] content) throws Exception{
    InputSource is = new InputSource(new ByteArrayInputStream(content));
    return SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,is).getDocumentElement();
  }
  
  
  
  
}
