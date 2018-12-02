package com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630;  

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.services.webservices.server.deploy.preprocess.WebServicesSupportHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.tool.ReadResult;
           
/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WebInfoCreatorImpl implements WebServicesSupportHandler, WebInfoCreator {

  public static final String WEBSERVICES_630_ENTRY[] = new String[]{"META-INF/ws-deployment-descriptor.xml", "meta-inf/ws-deployment-descriptor.xml"};

  public static String SOAP_SERVLET_NAME                   = "com.sap.engine.services.webservices.servlet.SoapServlet";
  public static String SOAP_SERVLET_SOURCE                 = "com/sap/engine/services/webservices/server/deploy/preprocess/SoapServlet.src";
  public static String WEB_XML_TEMPLATE                    = "com/sap/engine/services/webservices/server/deploy/preprocess/preprocess630/web.xml";
  public static String WEB_J2EE_ENGINE_XML_TEMPLATE        = "com/sap/engine/services/webservices/server/deploy/preprocess/preprocess630/web-j2ee-engine.xml";
  public static String WEB_J2EE_ENGINE_BASIC_XML_TEMPLATE  = "com/sap/engine/services/webservices/server/deploy/preprocess/preprocess630/web-j2ee-engine-basic.xml";
  public static String WEB_J2EE_ENGINE_CERT_XML_TEMPLATE   = "com/sap/engine/services/webservices/server/deploy/preprocess/preprocess630/web-j2ee-engine-certificate.xml";

  public static String AUTHENTICATION_FEATURE_NAME         = "http://www.sap.com/webas/630/soap/features/authentication";
  public static String WSS_FEATURE_NAME                    = "http://www.sap.com/webas/630/soap/features/wss";
  public static String TG_FEATURE_NAME                     = "http://www.sap.com/webas/630/soap/features/transportguarantee";
  public static String SUPPORT_SSO2_PROPERTY_NAME           = "SupportsSSO2Authentication";
  public static String AUTHENTICATION_METHOD_PROPERTY_NAME = "AuthenticationMethod";
  public static String AUTHENTICATION_MECHANISM_PROPERTY_NAME = "AuthenticationMechanism";
  public static String AUTH_BASICAUTH                      = "BasicAuth";
  public static String AUTH_CERTAUTH                       = "CertAuth";
  public static String AUTH_MECH_HTTP                      = "HTTP";
  public static String TG_TLSTYPE_PROPERTY_NAME            = "TLSType";
  public static String TG_SSL                              = "SSL";

  public static final String EJB_IMPLEMENTATION_ID    = "ejb-impllink";
  public static final String JAVA_IMPLEMENTATTION_ID  = "javaclass-impllink";

  public static String SOAPDISPATCHER_PREFIX  = "soapdispatcher";

  /**
   * SOAP over HTTP transport binding ID
   */
  public static final String SOAPHTTP_TRANSPORTBINDING  =  "SOAPHTTP_TransportBinding";

  /**
   * MIME transport binding ID
   */
  public static final String MIME_TRANSPORTBINDING      =  "MIME_TransportBinding";

  /**
   * HTTP (POST and GET) transport binding ID
   * HTTP (POST and GET) transport binding ID
   */
  public static final String HTTP_TRANSPORTBINDING      =  "HTTP_TransportBinding";


  public static int IAUTH_NO    = 1;
  public static int IAUTH_BASIC = 2;
  public static int IAUTH_CERT  = 3;

  public static int AUTH_LOGIN_TYPE = 1;
  public static int WSS_LOGIN_TYPE  = 2;

  public static int ITG_NO = 1;
  public static int ITG_YES = 2;

  private DocumentBuilder documentBuilder = null;

  public WebInfoCreatorImpl() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
  }
  
  public String[] getWebServicesEntry() {
    return WEBSERVICES_630_ENTRY; 
  }
  
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new WebInfo[0];
    }
     
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    WebInfo[] currentWebModuleInfoes; 
    WebInfo[] newWebModuleInfoes;
    for(int i = 0; i < archiveFiles.length; i++) {
      currentWebModuleInfoes = generateWebSupport(workingDir, archiveFiles[i], parsedAnnotations);
      newWebModuleInfoes = new WebInfo[webModuleInfoes.length + currentWebModuleInfoes.length];
      System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
      System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
      webModuleInfoes = newWebModuleInfoes;       
    }
  
    return webModuleInfoes;   
  }

  public WebInfo[] generateWebSupport(String workingDir, File archiveFile, ReadResult parsedAnnotations) throws Exception {
    JarFile archiveJarFile = null;    
    WebInfo[] webModuleInfoes = new WebInfo[0];    
  
    try {
      archiveJarFile = new JarFile(archiveFile);
      webModuleInfoes = generateWebSupport(workingDir, archiveJarFile, parsedAnnotations);    
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e;
    } finally {
      if(archiveJarFile != null) {
        try {
          archiveJarFile.close();
        } catch(IOException e) {
          //TODO 
          e.printStackTrace();
          throw e;
        } 
      }      
    }
  
    return webModuleInfoes;
  }  
  //has to be removed, only for temporary purposes
  private WebInfo createWebInfoFromWsar(String workingDir, JarFile archiveJarFile) throws IOException {
    String archiveName = archiveJarFile.getName();    
    String newWarName = archiveName.substring(0, archiveName.indexOf(".wsar"));
    new JarUtil().extractFiles(archiveJarFile, new String[0], new String[0], new String[0], new String[0], newWarName, false);
    IOUtil.copyDir(workingDir + "/WS_war1/WEB-INF", newWarName + "/WEB-INF");
    File newWarFile = new File(newWarName);
    String[] subDirectories = newWarFile.list();
    for (int i = 0; i < subDirectories.length; i++) {
			if (!(subDirectories[i].equals("META-INF") || subDirectories[i].equals("meta-inf") || subDirectories[i].equals("WEB-INF"))) {
        IOUtil.copyDir(newWarName + "/" + subDirectories[i], newWarName + "/WEB-INF/classes/" + subDirectories[i]);
      }
		}
     
    String warModulePath = newWarName + ".war";
    new JarUtils().makeJarFromDir(warModulePath, newWarName);
    WebInfo webInfo = new WebInfo();
    webInfo.setContextRoot(newWarFile.getName());
    webInfo.setWarModulePath(warModulePath);

    return webInfo;
  }
  
  public WebInfo[] generateWebSupport(String workingDir, JarFile archiveJarFile, ReadResult parsedAnnotations) throws Exception {    
    WebInfo[] webModuleInfoes = new WebInfo[0];       
    try {
      ZipEntry webServices630Entry;            
      WebInfo[] currentWebModuleInfoes; 
      WebInfo[] newWebModuleInfoes; 
      for(int i = 0; i < WEBSERVICES_630_ENTRY.length; i++) {
        webServices630Entry = archiveJarFile.getEntry(WEBSERVICES_630_ENTRY[i]);
        if(webServices630Entry != null) {          
          currentWebModuleInfoes = createSingleWebInfo(workingDir, archiveJarFile.getInputStream(webServices630Entry));
          int resultArrSize = webModuleInfoes.length + currentWebModuleInfoes.length;
          newWebModuleInfoes = new WebInfo[resultArrSize];
          //temp fix for wsar files, should be removed
          if (archiveJarFile.getName().endsWith(".wsar")) {
            newWebModuleInfoes = new WebInfo[resultArrSize + 1];
            WebInfo warFromWsar = createWebInfoFromWsar(workingDir, archiveJarFile);
            newWebModuleInfoes[resultArrSize] = warFromWsar;
          }
          
          System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
          System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
          webModuleInfoes = newWebModuleInfoes;          
        }      
      }    
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e;
    } 

    return webModuleInfoes;       
  }

  public WebInfo[] createSingleWebInfo(String workingDir, String wsDeploymentDescriptorPath) throws Exception {
    return  createSingleWebInfo(workingDir, new FileInputStream(wsDeploymentDescriptorPath));
  }

  public WebInfo[] createSingleWebInfo(String workingDir, InputStream in) throws Exception {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(in);
    ArrayList webInfos = new ArrayList();
    NodeList nlWebService = document.getElementsByTagName("webservice");
    for (int i=0; i < nlWebService.getLength(); i++) {
      NodeList nlConfiguration = ((Element)nlWebService.item(i)).getElementsByTagName("ws-configuration");
      for (int j=0; j < nlConfiguration.getLength(); j++) {
        WebInfo info = getWebInfo(workingDir, nlConfiguration.item(j));
        if (info != null) {
          webInfos.add(info);
        }
      }
    }

    return  getWebInfosAsArray(webInfos);
  }

  public WebInfo[] createWebInfo(String workingDir, String[] wsDeploymentDescriptorPaths) throws Exception {
    int descriptorsSize = wsDeploymentDescriptorPaths.length;
    WebInfo[] webInfos = new WebInfo[0];
    for (int i = 0; i < descriptorsSize; i++) {
      WebInfo[] currentWebInfos = createSingleWebInfo(workingDir, wsDeploymentDescriptorPaths[i]);
      WebInfo[] newWebInfos = new WebInfo[webInfos.length + currentWebInfos.length];
      System.arraycopy(webInfos, 0, newWebInfos, 0, webInfos.length);
      System.arraycopy(currentWebInfos, 0, newWebInfos, webInfos.length, currentWebInfos.length);
      webInfos = newWebInfos;
    }
    return webInfos;
  }

  private WebInfo getWebInfo(String workingDir, Node wsConfigurationNode) throws Exception {
    WebInfo webInfo = null;
    String implementationId = EJB_IMPLEMENTATION_ID;

    int authentication = IAUTH_NO;
    int transportGuarantee = ITG_NO;
    String authMechanism = null;
    boolean supportSSO = false;
    String transportBinding = SOAPHTTP_TRANSPORTBINDING;

    NodeList nl = ((Element)wsConfigurationNode).getElementsByTagName("impl-link");
    if (nl.getLength() > 0 ) {
      implementationId = ((Element)nl.item(0)).getAttribute("implementation-id");
    }

    nl = ((Element)wsConfigurationNode).getElementsByTagName("feature");
    for (int i=0; i< nl.getLength(); i++) {
      if (((Element)nl.item(i)).getAttribute("name").equals(AUTHENTICATION_FEATURE_NAME)) {
        /*
	 * Removed by Martijn as authentication moved into the wssec service for New York
	 *
	NodeList nl2 = ((Element)nl.item(i)).getElementsByTagName("property");
        for (int j=0; j<nl2.getLength(); j++) {
          Element currentElement = (Element)nl2.item(j);
          String currentElName = currentElement.getAttribute("name").trim();
          String currentElValue = currentElement.getAttribute("value");
          if (currentElName.equals(AUTHENTICATION_METHOD_PROPERTY_NAME)) {
            if (currentElValue.equals(AUTH_BASICAUTH)) {
              authentication = IAUTH_BASIC;
            } else if (currentElValue.equals(AUTH_CERTAUTH)) {
              authentication = IAUTH_CERT;
            } else {
              authentication = IAUTH_NO;
            }
          } else if (currentElName.equals(SUPPORT_SSO2_PROPERTY_NAME)) {
            if (currentElValue.equalsIgnoreCase("true")) {
              supportSSO = true;
            }
          } else if (currentElName.equals(AUTHENTICATION_MECHANISM_PROPERTY_NAME)) {
            authMechanism = currentElValue;
          }	
        }
	*/
      } else if (((Element)nl.item(i)).getAttribute("name").equals(TG_FEATURE_NAME)) {
        NodeList nl2 = ((Element)nl.item(i)).getElementsByTagName("property");
        for (int j=0; j<nl2.getLength(); j++) {
          if (((Element)nl2.item(j)).getAttribute("name").equals(TG_TLSTYPE_PROPERTY_NAME)) {
            String value = ((Element)nl2.item(j)).getAttribute("value");
            if (value.equals(TG_SSL)) {
              transportGuarantee = ITG_YES;
            }
          }
        }
      }
    }

    nl = ((Element)wsConfigurationNode).getElementsByTagName("transport-binding");
    if (nl.getLength() > 0) {
      transportBinding = ((Element)nl.item(0)).getAttribute("name");
    }

    NodeList securityRoleNodes = null;
    NodeList securityRoleDefNodes = ((Element)wsConfigurationNode).getElementsByTagName("security-roles-definition");
    if (securityRoleDefNodes.getLength() > 0) {
      securityRoleNodes = ((Element)securityRoleDefNodes.item(0)).getElementsByTagName("security-role");
    }

    String sessionTimeout = null;
    NodeList endpointSettings = ((Element)wsConfigurationNode).getElementsByTagName("entrypoint-settings");
    if (endpointSettings.getLength() > 0) {
      Element endpointSetting = (Element) endpointSettings.item(0);
      if (endpointSetting.getAttribute("type").equals("http")) {
        NodeList properties = endpointSetting.getElementsByTagName("property");
        Element tmpProp;
        for (int p = 0; p < properties.getLength(); p++) {
          tmpProp = (Element) properties.item(p);
          if (tmpProp.getAttribute("name").equals("session-timeout")) {
            String sessStr = tmpProp.getAttribute("value");
            if (sessStr.length() > 0) {
              sessionTimeout = sessStr;
            }
            break;   
          }
        }
      }
      //securityRoleNodes = ((Element)securityRoleDefNodes.item(0)).getElementsByTagName("security-role");
    }
    
    NodeList nlTransportAddress = ((Element)wsConfigurationNode).getElementsByTagName("transport-address");
    if (nlTransportAddress.getLength() > 0) {
      String transportAddress = nlTransportAddress.item(0).getFirstChild().getNodeValue().trim();
      if (transportAddress.startsWith("/")) {
        transportAddress = transportAddress.substring(1);
      }
      if (!transportAddress.startsWith(SOAPDISPATCHER_PREFIX)) {
        webInfo = new WebInfo();
        webInfo.setContextRoot(transportAddress);
        webInfo.setWarModulePath(getWar(workingDir, transportAddress, authentication, transportGuarantee, transportBinding, implementationId, securityRoleNodes, supportSSO, authMechanism, sessionTimeout, false));
      } else {
        return null;
      }
    }

    return webInfo;
  }

  private String getWar(String workingDir, String contextRoot, int authentication, int transportGuarantee, String transportBinding, String implementationId, NodeList securityRoles, boolean supportSSO, String authMechanism, String sessionTimeout, boolean includeSoapServlet) throws Exception {
    String uniquePrefix = contextRoot;
    if (uniquePrefix.startsWith("/")) {
      uniquePrefix = uniquePrefix.substring(1);
    }
    uniquePrefix = uniquePrefix.replace('\\', '/');
    uniquePrefix = uniquePrefix.replace('/', '_');

    String warWorkingDir = workingDir + File.separator + "WS_war1";
    File warWorkingDirFile = new File(warWorkingDir);
    if (warWorkingDirFile.exists()) deleteDir(warWorkingDirFile);

    if (! warWorkingDirFile.mkdirs()) {
      throw new IOException("Unable to create temporary directory for webservices needs: " + warWorkingDir);
    }

    String webInfDir = warWorkingDir + File.separator + "WEB-INF";
    if (! (new File(webInfDir)).mkdirs()) {
      throw new IOException("Unable to create temporary directory for webservices needs: " + webInfDir);
    }

    String classesDir = webInfDir + File.separator + "classes";
    if (! (new File(classesDir)).mkdirs()) {
      throw new IOException("Unable to create temporary directory for webservices needs: " + classesDir);
    }

    ClassLoader loader = this.getClass().getClassLoader();
    if(includeSoapServlet){
	    InputStream servletSourceInput = loader.getResourceAsStream(SOAP_SERVLET_SOURCE);
	    String servletFileName = classesDir + File.separator + SOAP_SERVLET_NAME + ".class";
	    File servletFile = new File(servletFileName);
	    FileOutputStream servletSourceOutput = new FileOutputStream(servletFile);
	    copy(servletSourceInput, servletSourceOutput);
	    servletSourceInput.close();
	    servletSourceOutput.close();
    }
    
    InputStream webj2eeSourceInput = loader.getResourceAsStream(WEB_J2EE_ENGINE_XML_TEMPLATE);
    String webj2eeFileName = webInfDir + File.separator + "web-j2ee-engine.xml";
    File webj2eeFile = new File(webj2eeFileName);
    Document webj2eeDocument = getModifiedWebJ2eeXML(webj2eeSourceInput, implementationId, authentication, securityRoles, supportSSO);
    FileOutputStream webj2eeSourceOutput = new FileOutputStream(webj2eeFile);
    DOMSerializer serializer = new DOMSerializer();
    serializer.write(webj2eeDocument, webj2eeSourceOutput);
    webj2eeSourceInput.close();
    webj2eeSourceOutput.close();

	  String warName = uniquePrefix + ".war";

    InputStream webXMLSourceInput = loader.getResourceAsStream(WEB_XML_TEMPLATE);
    FileOutputStream webXMLSourceOutput = new FileOutputStream(webInfDir + File.separator + "web.xml");
    Document webDocument = getModifiedWebXML(webXMLSourceInput, warName,  uniquePrefix, authentication, transportGuarantee, transportBinding, implementationId, securityRoles, supportSSO, authMechanism, sessionTimeout);
    serializer = new DOMSerializer();
    serializer.write(webDocument, webXMLSourceOutput);
    webXMLSourceInput.close();
    webXMLSourceOutput.close();
    JarUtils jarUtils = new JarUtils();
    String warModulePath = workingDir + File.separator + warName;
    jarUtils.makeJarFromDir(warModulePath, warWorkingDir);

    return warModulePath;
  }

  private Document getModifiedWebXML(InputStream webXMLSourceInput, String warName, String uniquePrefix, int authentication, int transportGuarantee, String transportBinding, String implementationId, NodeList securityRoles, boolean supportSSO, String authMechanism, String sessionTimeout) throws Exception {
    Document document = documentBuilder.parse(webXMLSourceInput);
    Element documentElement = document.getDocumentElement();
    NodeList nodes = documentElement.getChildNodes();
    int nodesSize = nodes.getLength();

    for (int i = 0; i < nodesSize; i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (node.getNodeName().equals("display-name")) node.getFirstChild().setNodeValue(warName);
        if (node.getNodeName().equals("servlet")) {
          NodeList childNodes = node.getChildNodes();
          int childsSize = childNodes.getLength();
          for (int j = 0; j < childsSize; j++) {
            Node child = childNodes.item(j);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
              if (child.getNodeName().equals("display-name")) child.getFirstChild().setNodeValue(uniquePrefix + "_" + SOAP_SERVLET_NAME);
              if (child.getNodeName().equals("servlet-name")) child.getFirstChild().setNodeValue(uniquePrefix + "_" + SOAP_SERVLET_NAME);
              if (child.getNodeName().equals("servlet-class")) child.getFirstChild().setNodeValue(SOAP_SERVLET_NAME);
            }
          }
        }
        if (node.getNodeName().equals("servlet-mapping")) {
          NodeList childNodes = node.getChildNodes();
          int childsSize = childNodes.getLength();
          for (int j = 0; j < childsSize; j++) {
            Node child = childNodes.item(j);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
              if (child.getNodeName().equals("servlet-name")) child.getFirstChild().setNodeValue(uniquePrefix + "_" + SOAP_SERVLET_NAME);
            }
          }
        }
        
        if (sessionTimeout != null) { //in case custom sessionTimeout is available
          if (node.getNodeName().equals("session-config")) {
            NodeList childNodes = node.getChildNodes();
            int childsSize = childNodes.getLength();
            for (int j = 0; j < childsSize; j++) {
              Node child = childNodes.item(j);
              if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals("session-timeout")) child.getFirstChild().setNodeValue(sessionTimeout);
              }
            }          
          }
        }    
      }
    }

    if (authentication != IAUTH_NO && (authMechanism == null || authMechanism.equalsIgnoreCase(AUTH_MECH_HTTP))) {
      Element securityConstraint = document.createElement("security-constraint");

      Element webResourceCollection = document.createElement("web-resource-collection");
      securityConstraint.appendChild(webResourceCollection);

      Element webResourceName = document.createElement("web-resource-name");
      webResourceName.appendChild(document.createTextNode("Web Services resource"));
      webResourceCollection.appendChild(webResourceName);

      Element urlPattern = document.createElement("url-pattern");
      webResourceCollection.appendChild(urlPattern);
      Text tx = document.createTextNode("/*");
      urlPattern.appendChild(tx);

      Element el = document.createElement("http-method");
      el.appendChild(document.createTextNode("POST"));
      webResourceCollection.appendChild(el);

      Element el2 = null;
      el = document.createElement("auth-constraint");

      el2 = document.createElement("role-name");
      el2.appendChild(document.createTextNode("web-services-user"));
      el.appendChild(el2);
      securityConstraint.appendChild(el);

      el = document.createElement("user-data-constraint");
      el2 = document.createElement("description");
      el2.appendChild(document.createTextNode("Security support for a WS"));
      el.appendChild(el2);

      el2 = document.createElement("transport-guarantee");
      if (transportGuarantee == ITG_NO) {
        el2.appendChild(document.createTextNode("NONE"));
      } else {
        el2.appendChild(document.createTextNode("CONFIDENTIAL"));
      }
      el.appendChild(el2);
      securityConstraint.appendChild(el);

      documentElement.appendChild(securityConstraint);

      el = document.createElement("login-config");
      el2 = document.createElement("auth-method");
      if (authentication == IAUTH_BASIC) {
        tx = document.createTextNode("BASIC");
      } else {
        tx = document.createTextNode("CLIENT-CERT");
      }
      el2.appendChild(tx);
      el.appendChild(el2);
      documentElement.appendChild(el);

      el = document.createElement("security-role");
      el2 = document.createElement("description");
      el2.appendChild(document.createTextNode("Role to access web services"));
      el.appendChild(el2);
      el2 = document.createElement("role-name");
      tx = document.createTextNode("web-services-user");
      el2.appendChild(tx);
      el.appendChild(el2);
      documentElement.appendChild(el);

      if(implementationId.equals(JAVA_IMPLEMENTATTION_ID)) {
        setSecurityRoles(document, securityRoles);
      }
      if(supportSSO) {
        setDefaultLoginSecurityRoles(document, authentication);
      }
    }

    return document;
  }

  private Document getModifiedWebJ2eeXML(InputStream in, String implementationId, int authentication,  NodeList securityRoleMaps, boolean supportSSO) throws Exception {
    Document webJ2eeXmlDocument = documentBuilder.parse(in);

    if(authentication != IAUTH_NO) {
      if(implementationId.equals(JAVA_IMPLEMENTATTION_ID)) {
        setSecurityRoleMaps(webJ2eeXmlDocument, securityRoleMaps);
      }

      if (supportSSO) {
        setDefaultLoginConfig(webJ2eeXmlDocument, authentication);
      }
    }

    return webJ2eeXmlDocument;
  }

  private void setSecurityRoles(Document webXmlDocument, NodeList wsSecurityRoles) {
    if (wsSecurityRoles == null) {
      return;
    }

    Element webXmlDocumentEl = webXmlDocument.getDocumentElement();
    for (int i = 0; i < wsSecurityRoles.getLength(); i++) {
      Element el = null;
      NodeList nl = null;
      Node node = null;

      Element wsSecRole = (Element)wsSecurityRoles.item(i);

      Element webSecRoleEl = webXmlDocument.createElement("security-role");

      el = webXmlDocument.createElement("role-name");
      nl = wsSecRole.getElementsByTagName("role-name");
      node = webXmlDocument.createTextNode(nl.item(0).getFirstChild().getNodeValue());
      el.appendChild(node);

      nl = wsSecRole.getElementsByTagName("description");
      if (nl.getLength() > 0) {
        el = webXmlDocument.createElement("description");
        node = webXmlDocument.createTextNode(nl.item(0).getFirstChild().getNodeValue());
        el.appendChild(node);
        webSecRoleEl.appendChild(el);
      }

      webSecRoleEl.appendChild(el);
     webXmlDocumentEl.appendChild(webSecRoleEl);
    }
  }

  private void setDefaultLoginSecurityRoles(Document webDocument, int authentication) throws Exception {
    setSecurityRoles(webDocument, getDefaultLoginSecurityRoleMaps(authentication));
  }

  private NodeList getDefaultLoginSecurityRoleMaps(int authentication) throws Exception {
    NodeList securityRoleMaps = null;
    if(authentication == IAUTH_BASIC) {
      securityRoleMaps = getDefaultLoginSecurityRoleMaps(WEB_J2EE_ENGINE_BASIC_XML_TEMPLATE);
    } else if(authentication == IAUTH_CERT) {
      securityRoleMaps = getDefaultLoginSecurityRoleMaps(WEB_J2EE_ENGINE_CERT_XML_TEMPLATE);
    }

    return securityRoleMaps;
  }

  private NodeList getDefaultLoginSecurityRoleMaps(String webJ2eeLoginSourceLocation) throws Exception {
    ClassLoader loader = this.getClass().getClassLoader();
    InputStream webJ2eeLoginInput = null;

    NodeList nodeList = null;
    try {
      webJ2eeLoginInput = loader.getResourceAsStream(webJ2eeLoginSourceLocation);
      nodeList = getDefaultLoginSecurityRoleMaps(webJ2eeLoginInput);
    } finally {
      if(webJ2eeLoginInput != null) {
        webJ2eeLoginInput.close();
      }
    }

    return nodeList;
  }

  private NodeList getDefaultLoginSecurityRoleMaps(InputStream webJ2eeLoginInput) throws Exception {
    Document webJ2eeLoginDocument = documentBuilder.parse(webJ2eeLoginInput);
    return webJ2eeLoginDocument.getElementsByTagName("security-role-map");
  }

  private void setSecurityRoleMaps(Document webJ2eeXmlDocument, NodeList wsSecurityRoleMaps) {
    if (wsSecurityRoleMaps == null) {
      return;
    }

    Element webJ2eeXmlDocumentEl = webJ2eeXmlDocument.getDocumentElement();
    for (int i = 0; i < wsSecurityRoleMaps.getLength(); i++) {
      Element el = null;
      NodeList nl = null;
      Node node = null;

      Element wsSecRoleMap = (Element)wsSecurityRoleMaps.item(i);

      Element webSecRoleEl = webJ2eeXmlDocument.createElement("security-role-map");

      el = webJ2eeXmlDocument.createElement("role-name");
      nl = wsSecRoleMap.getElementsByTagName("role-name");
      node = webJ2eeXmlDocument.createTextNode(nl.item(0).getFirstChild().getNodeValue());
      el.appendChild(node);
      webSecRoleEl.appendChild(el);

      nl = wsSecRoleMap.getElementsByTagName("user-name");
      for(int j = 0;j < nl.getLength(); j++) {
        el = webJ2eeXmlDocument.createElement("server-role-name");
        node = webJ2eeXmlDocument.createTextNode(nl.item(j).getFirstChild().getNodeValue());
        el.appendChild(node);
        webSecRoleEl.appendChild(el);
      }

      nl = wsSecRoleMap.getElementsByTagName("group-name");
      for(int j = 0; j < nl.getLength(); j++) {
        el = webJ2eeXmlDocument.createElement("group-name");
        node = webJ2eeXmlDocument.createTextNode(nl.item(j).getFirstChild().getNodeValue());
        el.appendChild(node);
        webSecRoleEl.appendChild(el);
      }

      nl = wsSecRoleMap.getElementsByTagName("server-role-name");
      for(int j = 0; j < nl.getLength(); j++) {
        el = webJ2eeXmlDocument.createElement("server-role-name");
        node = webJ2eeXmlDocument.createTextNode(nl.item(j).getFirstChild().getNodeValue());
        el.appendChild(node);
        webSecRoleEl.appendChild(el);
      }

      webJ2eeXmlDocumentEl.appendChild(webSecRoleEl);
    }
  }

  private void setDefaultLoginConfig(Document webJ2eeDocument, int authentication) throws Exception {
    if (authentication == IAUTH_BASIC) {
      setDefaultLoginConfig(webJ2eeDocument,  WEB_J2EE_ENGINE_BASIC_XML_TEMPLATE);
    } else if(authentication == IAUTH_CERT) {
      setDefaultLoginConfig(webJ2eeDocument, WEB_J2EE_ENGINE_CERT_XML_TEMPLATE);
    }
  }

  private void setDefaultLoginConfig(Document webJ2eeDocument, String webJ2eeLoginSourceLocation) throws Exception {
    ClassLoader loader = this.getClass().getClassLoader();
    InputStream webJ2eeLoginInput = null;
    try {
      webJ2eeLoginInput = loader.getResourceAsStream(webJ2eeLoginSourceLocation);
      setDefaultLoginConfig(webJ2eeDocument, webJ2eeLoginInput);
    } finally {
      if(webJ2eeLoginInput != null) {
        webJ2eeLoginInput.close();
      }
    }
  }

  private void setDefaultLoginConfig(Document webJ2eeDocument, InputStream webJ2eeLoginInput) throws Exception {
    Document webJ2eeLoginDocument = documentBuilder.parse(webJ2eeLoginInput);
    setDefaultLoginConfig(webJ2eeDocument, webJ2eeLoginDocument);
  }

  private void setDefaultLoginConfig(Document webJ2eeDocument, Document webJ2eeLoginDocument) {
//    NodeList securityRoleMaps = webJ2eeLoginDocument.getElementsByTagName("security-role-map");
//    setSecurityRoleMapsByImport(webJ2eeDocument, securityRoleMaps);

    NodeList loginModuleConfigs = webJ2eeLoginDocument.getElementsByTagName("login-module-configuration");
    Node loginModuleConfig = null;
    if (loginModuleConfigs.getLength() > 0) {
      loginModuleConfig = loginModuleConfigs.item(0);
    }
    setLoginConfig(webJ2eeDocument, loginModuleConfig);
  }

  private void setSecurityRoleMapsByImport(Document webJ2eeDocument, NodeList securityRolesMaps) {
    if(securityRolesMaps == null) {
      return;
    }

    for (int i = 0; i < securityRolesMaps.getLength(); i++) {
      Node securityRoleMap = securityRolesMaps.item(i);
      setSecurityRoleMapByImport(webJ2eeDocument, securityRoleMap);
    }
  }

  private void setSecurityRoleMapByImport(Document webJ2eeDocument, Node securityRoleMap) {
    if(securityRoleMap == null) {
      return;
    }

    Node newSecurityRoleMap = webJ2eeDocument.importNode(securityRoleMap, true);
    webJ2eeDocument.getDocumentElement().appendChild(newSecurityRoleMap);
  }

  private void setLoginConfig(Document webJ2eeDocument, Node loginModuleConfig) {
    if(loginModuleConfig == null) {
      return;
    }

    Node newLoginModuleConfig = webJ2eeDocument.importNode(loginModuleConfig, true);
    webJ2eeDocument.getDocumentElement().appendChild(newLoginModuleConfig);
  }

  public void copy(InputStream in, OutputStream out) throws IOException {
     int  bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int count = 0;
    while ( (count = in.read(buffer)) != -1) {
     out.write(buffer, 0 , count);
    }
    out.flush();
  }

  public boolean deleteDir(File dir) throws IOException {
    if (!dir.exists()) return true;
    if (!dir.isDirectory()) throw new IOException("The current file is not a directory.");
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) deleteDir(files[i]);
      if (files[i].isFile()) files[i].delete();
    }
    dir.delete();
    return (!dir.exists());
  }

  private WebInfo[] getWebInfosAsArray(ArrayList webInfos) {
    WebInfo[] webInfosArr = new WebInfo[webInfos.size()];
    for(int i = 0; i < webInfos.size(); i++) {
      webInfosArr[i] = (WebInfo)webInfos.get(i);
    }

    return webInfosArr;
  }

}
