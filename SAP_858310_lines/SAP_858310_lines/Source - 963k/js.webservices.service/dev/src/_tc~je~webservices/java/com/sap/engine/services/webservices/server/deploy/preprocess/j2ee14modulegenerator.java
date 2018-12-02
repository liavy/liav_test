/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.server.deploy.preprocess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl;
import com.sap.engine.lib.xml.parser.JAXPProperties;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: J2EE14ModuleGenerator
 * Description: Module generator for j2ee 1.4. webservices called in predeployment step in order to generate
 * war file, web.xml and soap servlet for every webservice described in the ear archive that is deployed.
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class J2EE14ModuleGenerator implements WebServicesSupportHandler {

  protected static final String NAME            = "name";
  protected static final String WEB_APP         = "web-app"; 
  protected static final String DISPLAY_NAME    = "display-name";
  
  protected static final String SERVLET         = "servlet";
  protected static final String SERVLET_NAME    = "servlet-name"; 
  protected static final String SERVLET_CLASS   = "servlet-class";
  protected static final String LOAD_ON_STARTUP = "load-on-startup"; 
  protected static final int    LOAD_ON_STARTUP_VALUE = 0; 
  protected static final String SERVLET_MAPPING = "servlet-mapping";
  protected static final String URL_PATTERN     = "url-pattern";
  protected static final String SESSION_CONFIG  = "session-config"; 
  protected static final String SESSION_TIMEOUT = "session-timeout"; 
  protected static final int    SESSION_TIMEOUT_VALUE = 1; 
  
  private final String SECURITY_CONSTRAINT     = "security-constraint";
  private final String WEB_RESOURCE_COLLECTION = "web-resource-collection";
  private final String WEB_RESOURCE_NAME       = "web-resource-name";
  private final String HTPP_METHOD             = "http-method";
  private final String AUTH_CONSTRAINT         = "auth-constraint";
  private final String ROLE_NAME               = "role-name";
  private final String USER_DATA_CONSTRAINT    = "user-data-constraint";
  private final String TRANSPORT_GARANTEE      = "transport-guarantee";
  
  private final String LOGIN_CONFIG            = "login-config";
  private final String AUTH_METHOD             = "auth-method";
  private final String REALM_NAME              = "realm-name";
  private final String SECURITY_ROLE           = "security-role";
  
  protected static final String WEB_INF         = "WEB-INF";
  protected static final String WEB_CLASSES_DIR = WEB_INF + "/classes";
  protected static final String WEB_XML  = "web.xml"; 

  protected static final String SOAP_SERVLET_CLASS_NAME = "SoapServlet";
  protected static final String SOAP_SERVLET_SOURCE     = "com/sap/engine/services/webservices/server/deploy/preprocess/SoapServlet.src";

  private final String WEB_J2EE_ENGINE     = "web-j2ee-engine";
  private final String WEB_J2EE_ENGINE_XML = "web-j2ee-engine.xml";
  private final String SECURITY_ROLE_MAP   = "security-role-map";
  private final String SERVER_ROLE_NAME    = "server-role-name";
  private final String EMPLOYEE            = "Employee";
  private final String ADMINISTRATOR       = "Administrator";
  private final String MANAGER             = "Manager";
  private final String VP                  = "VP"; 
  
  protected DOMSerializer domSerializer; 
  protected ClassLoader loader; 
  protected DocumentBuilder documentBuilder; 
  protected Document webJ2EEEngineDocument; 
  
  private final String[] WEBSERVICES_EJB_ENTRIES   = { "Meta-inf/webservices.xml", "Meta-inf/webservices-j2ee-engine-ext.xml", "META-INF/webservices.xml", "META-INF/webservices-j2ee-engine-ext.xml"};
  private final String WEBSERVICE_DESCRIPTION      = "webservice-description";
  private final String WEBSERVICE_DESCRIPTION_NAME = "webservice-description-name";
  private final String WEBSERVICES_EXT     = "webservices-ext";
  private final String WEBSERVICE_NAME     = "webservice-name";
  private final String CONTEXT_ROOT        = "context-root";
  private final String BINDING_DATA        = "binding-data";
  private final String BINDING_DATA_NAME   = "binding-data-name";
  private final String URL                 = "url";
  private final String PORT_COMPONENT      = "port-component";
  private final String PORT_COMPONENT_NAME = "port-component-name";
  
  public static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
  public static final String JEE5_NAMESPACE = "http://java.sun.com/xml/ns/javaee";
  
  private String urlPattern = "/*";
  
  public J2EE14ModuleGenerator() throws Exception {
      
    DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
    factory.setAttribute(JAXPProperties.PROPERTY_REPLACE_NAMESPACE, new String[] { J2EE_NAMESPACE, JEE5_NAMESPACE});
    factory.setNamespaceAware(true);
    
    this.documentBuilder    = factory.newDocumentBuilder(); 
    this.domSerializer      = new DOMSerializer();
    this.loader             = this.getClass().getClassLoader();   
  }

  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new WebInfo[0];
    }
           
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    WebInfo[] currentWebModuleInfoes; 
    WebInfo[] newWebModuleInfoes;

    for(int i = 0; i < archiveFiles.length; i++) {
      currentWebModuleInfoes = generateWebSupport(workingDir, new JarFile(archiveFiles[i]), parsedAnnotations);
      newWebModuleInfoes = new WebInfo[webModuleInfoes.length + currentWebModuleInfoes.length];
      System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
      System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
      webModuleInfoes = newWebModuleInfoes;       
    }
    return webModuleInfoes;      
  }
  
  public WebInfo[] generateWebSupport(String tempDir, JarFile moduleRelativeFileUri, ReadResult parsedAnnotations) throws Exception {
    try {
      if (moduleRelativeFileUri.getName().endsWith(".jar")) {                      
        for (int i = 0; i < WEBSERVICES_EJB_ENTRIES.length; i++) {
          ZipEntry webservicesEntry = moduleRelativeFileUri.getEntry(WEBSERVICES_EJB_ENTRIES[i]); 
          InputStream webservicesInputStream = null;
          try {
            if (webservicesEntry != null) {             
              ZipEntry webservicesExtEntry = moduleRelativeFileUri.getEntry(WEBSERVICES_EJB_ENTRIES[++i]);
              webservicesInputStream = moduleRelativeFileUri.getInputStream(webservicesEntry);
              return createSingleModuleForEjb(new File(tempDir), webservicesInputStream, (webservicesExtEntry != null) ? moduleRelativeFileUri.getInputStream(webservicesExtEntry) : null);
            } 
          } catch(Exception exc) {
            throw exc;
          } finally {
            if (webservicesInputStream != null) {
             try {
               webservicesInputStream.close(); 
             
             } catch(IOException e) {
              // $JL-EXC$
             }
           }
          }        
        }       
      }
    } catch (Exception rtge) {
      throw rtge;
    }
    return new WebInfo[0];
  }
  
  private WebInfo[] createSingleModuleForEjb(File tempDir, InputStream webservicesIn, InputStream webservicesJ2EEEngineExtIn) throws Exception {
    Vector webInfoArr = new Vector();   
    String webserviceName;  
    String tempWorkingDir = tempDir + "";
    String warFilePath = "";
    String contextRoot = null;
     
    if (webJ2EEEngineDocument == null) {
      webJ2EEEngineDocument = generateWebJ2EEEngineXML();
    } 
     
    Hashtable extValues = new Hashtable();   
    if (webservicesJ2EEEngineExtIn != null) {
      getExtValues(webservicesJ2EEEngineExtIn, extValues);    
    }     
      
    Element webservices = documentBuilder.parse(webservicesIn).getDocumentElement();  
    NodeList nlWebServiceDescription = webservices.getElementsByTagNameNS(JEE5_NAMESPACE, WEBSERVICE_DESCRIPTION);
    for (int i = 0; i < nlWebServiceDescription.getLength(); i++) {
      NodeList nlWebserviceName = ((Element)nlWebServiceDescription.item(i)).getElementsByTagNameNS(JEE5_NAMESPACE, WEBSERVICE_DESCRIPTION_NAME);
      NodeList nlPortComponents = ((Element)nlWebServiceDescription.item(i)).getElementsByTagNameNS(JEE5_NAMESPACE, PORT_COMPONENT);
      webserviceName = nlWebserviceName.item(0).getFirstChild().getNodeValue().trim();
      
      WebServicesExtData webServicesExtData = (WebServicesExtData)extValues.get(webserviceName);  
      if (webServicesExtData != null && webServicesExtData.getContextRoot() != null) {
        contextRoot = webServicesExtData.getContextRoot();
      } else {
        contextRoot = webserviceName;
      }
      if (!contextRoot.equals("")) {
        try {
          tempWorkingDir = tempDir + "/" + webserviceName;
          warFilePath  = tempDir + "/" + webserviceName + ".war";      
          generateWar(tempWorkingDir, warFilePath, createWebDescriptorMultipleMode(webserviceName, nlPortComponents, webServicesExtData));
        } catch(Exception e) {
          throw e; 
        }
          webInfoArr.addElement(new WebInfo(contextRoot, warFilePath));
        } else {     
          Hashtable webserviceBindingData = webServicesExtData.getBindingData(); 
          Enumeration bindingDataNames = webserviceBindingData.keys();
          while (bindingDataNames.hasMoreElements()) {
            String wsBindingDataName = (String) bindingDataNames.nextElement();
            if (wsBindingDataName.startsWith(webserviceName)) {
              try {  
                 tempWorkingDir = tempDir + "/" + wsBindingDataName;
                 warFilePath  = tempDir + "/" + wsBindingDataName + ".war";
                 BindingExtData bindingExtData = (BindingExtData)webServicesExtData.getBindingData().get(wsBindingDataName);
                 generateWar(tempWorkingDir, warFilePath, createWebDescriptorSingleMode(wsBindingDataName, nlPortComponents, webServicesExtData, bindingExtData));
                 webInfoArr.addElement(new WebInfo(bindingExtData.getUrlPattern(), warFilePath)); 
              } catch(Exception e) {
                throw e; 
               }            
            }
          }
        }
      
    }
    
    FileUtils.deleteDirectory(new File(tempWorkingDir));
    WebInfo[] result = new WebInfo[webInfoArr.size()];
    webInfoArr.copyInto(result);
    return result;
  }
   
  private void getExtValues(InputStream webservicesJ2EEEngineExtIn, Hashtable extValues) throws Exception {     
			try {
				Element webservicesExt = documentBuilder.parse(webservicesJ2EEEngineExtIn).getDocumentElement();
				NodeList nlWebServicesExt = webservicesExt.getElementsByTagName(WEBSERVICE_DESCRIPTION);
				for (int i = 0; i < nlWebServicesExt.getLength(); i++) {
          WebServicesExtData webServicesExtData = new WebServicesExtData();
          webServicesExtData.setBindingData(new Hashtable());
          
				  NodeList nlWebserviceName = ((Element)nlWebServicesExt.item(i)).getElementsByTagName(WEBSERVICE_NAME);
				  NodeList nlContextRoot = ((Element)nlWebServicesExt.item(i)).getElementsByTagName(CONTEXT_ROOT);
				  String webserviceName = nlWebserviceName.item(0).getFirstChild().getNodeValue().trim();
          if (nlContextRoot.item(0) != null) { 
            Node contextRootNode = nlContextRoot.item(0).getFirstChild();
            webServicesExtData.setContextRoot((contextRootNode != null) ? contextRootNode.getNodeValue().trim() : ""); 
				  } 
				    
          NodeList nlBindingData = ((Element)nlWebServicesExt.item(i)).getElementsByTagName(BINDING_DATA);			  
          for (int j = 0; j < nlBindingData.getLength(); j++) { 
            BindingExtData bindingExtData = new BindingExtData();           
            NodeList nlBindingDataName = ((Element)nlBindingData.item(j)).getElementsByTagName(BINDING_DATA_NAME);
				    NodeList nlUri = ((Element)nlBindingData.item(j)).getElementsByTagName(URL);
            Node bindingDataNode = nlBindingDataName.item(0).getFirstChild();
            
            if (nlUri.item(0) != null) {             
              Node uriNode = nlUri.item(0).getFirstChild();
              if (bindingDataNode != null && uriNode != null) {
                bindingExtData.setUrlPattern(uriNode.getNodeValue().trim());            
              }
				    }
            
            NodeList nlAuthMethod = ((Element)nlBindingData.item(j)).getElementsByTagName(AUTH_METHOD);
            if (nlAuthMethod.item(0) != null) {
              Node authMethodNode = nlAuthMethod.item(0).getFirstChild();
              bindingExtData.setAuthMethod((authMethodNode != null) ? authMethodNode.getNodeValue().trim() : "");            
            }
          
            NodeList nlTransportGarantee = ((Element)nlBindingData.item(j)).getElementsByTagName(TRANSPORT_GARANTEE);
            if (nlTransportGarantee.item(0) != null) {
              Node transportGaranteeNode = nlTransportGarantee.item(0).getFirstChild();
              bindingExtData.setTransportGarantee((transportGaranteeNode != null) ? transportGaranteeNode.getNodeValue().trim() : "");            
            } 
          
            NodeList nlRoleName = ((Element)nlBindingData.item(j)).getElementsByTagName(ROLE_NAME);
            if (nlRoleName.item(0) != null) {
              Node roleNameNode = nlRoleName.item(0).getFirstChild();
              bindingExtData.setRoleName((roleNameNode != null) ? roleNameNode.getNodeValue().trim() : "");            
            } 
          
            NodeList nlHttpMethod = ((Element)nlBindingData.item(j)).getElementsByTagName(HTPP_METHOD);
            if (nlHttpMethod.item(0) != null) {
              Node httpMethodNode = nlHttpMethod.item(0).getFirstChild();
              bindingExtData.setHttpMethod((httpMethodNode != null) ? httpMethodNode.getNodeValue().trim() : "");            
            }
            
            bindingExtData.setBindingDataName(bindingDataNode.getNodeValue().trim());
            webServicesExtData.getBindingData().put(webserviceName + "_" + bindingDataNode.getNodeValue().trim(), bindingExtData);            
				  }
           
          extValues.put(webserviceName, webServicesExtData);
				} 
			} catch (Exception e) {
        throw e;
			}        
    } 

  private Document createWebDescriptorMultipleMode(String webserviceName, NodeList portComponents, WebServicesExtData webServicesExtData) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementMulipleMode(webDescriptorDocument, portComponents, webserviceName, webServicesExtData));    
    return webDescriptorDocument;
  }     
     
  private Element createWebAppElementMulipleMode(Document webDescriptorDocument, NodeList portComponents, String webserviceName, WebServicesExtData webServicesExtData) {               

    Element webAppElement = webDescriptorDocument.createElement(WEB_APP);
    Hashtable bindingDataUrls;
    if (webServicesExtData != null) {
      bindingDataUrls = webServicesExtData.getBindingData();  
    } else {//webservices-j2ee-engine-ext.xml does not exist
      bindingDataUrls = new Hashtable();
    }
      
    if (bindingDataUrls.size() != 0) {//webservices-j2ee-engine-ext.xml exists
      webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));        
      setServletElements(webDescriptorDocument, webAppElement, bindingDataUrls, webserviceName);
      setServletMappingElements(webDescriptorDocument, webAppElement, bindingDataUrls, webserviceName); 
      webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
    } else {
      webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));    
      for (int i = 0; i < portComponents.getLength(); i++) {
        NodeList nlPortComponentName = ((Element)portComponents.item(i)).getElementsByTagNameNS(JEE5_NAMESPACE, PORT_COMPONENT_NAME);
        String servletName = nlPortComponentName.item(0).getFirstChild().getNodeValue(); 
        webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
        webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));      
      }
      webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument)); 
      
//      webServicesExtData.setWebResourceName(webserviceName + "Res");
//      webAppElement.appendChild(createSecurityConstraint(webDescriptorDocument, webServicesExtData));
//      webAppElement.appendChild(createLoginConfig(webDescriptorDocument, webServicesExtData));
//      webAppElement.appendChild(createSecurityRole(webDescriptorDocument, webServicesExtData));
    }
    return webAppElement;
  }  
     
  private void setServletElements(Document webDescriptorDocument, Element webAppElement, Hashtable bindingDataUrls, String webserviceName) {
    Enumeration bindingDataNames = bindingDataUrls.keys();
    while (bindingDataNames.hasMoreElements()) {
      String bindingDataName = (String) bindingDataNames.nextElement();
      if (bindingDataName.startsWith(webserviceName)) {
        webAppElement.appendChild(createServletElement(webDescriptorDocument, bindingDataName, bindingDataName, SOAP_SERVLET_CLASS_NAME, 0));    
      }
    }
  }      
     
  private void setServletMappingElements(Document webDescriptorDocument, Element webAppElement, Hashtable bindingDataUrls, String webserviceName) {
    Enumeration bindingDataNames = bindingDataUrls.keys();
    while (bindingDataNames.hasMoreElements()) {
      String bindingDataName = (String)bindingDataNames.nextElement();
      String bindingDataUrl  = null;
      BindingExtData bindingData = null;
      
      if (bindingDataUrls.containsKey(bindingDataName)){
          bindingData = (BindingExtData)bindingDataUrls.get(bindingDataName);
      }
      
      if ( bindingData != null ) {
          bindingDataUrl = bindingData.getUrlPattern();
      }
          
      if (bindingDataName.startsWith(webserviceName)) {
        webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, bindingDataName, (bindingDataUrl != null) ? bindingDataUrl : "/*"));
      }
    }
  }  
  
  private Document createWebDescriptorSingleMode(String webserviceName, NodeList portComponents, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementSingleMode(webDescriptorDocument, webserviceName, portComponents, webServicesExtData, bindingExtData));
    return webDescriptorDocument;
  }
  
  private Element createWebAppElementSingleMode(Document webDescriptorDocument, String webserviceName, NodeList portComponents, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element webAppElement = webDescriptorDocument.createElement(WEB_APP);      
    webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));    
//    for (int i = 0; i < portComponents.getLength(); i++) {
//      NodeList nlPortComponentName = ((Element)portComponents.item(i)).getElementsByTagName(PORT_COMPONENT_NAME);
//      String servletName = nlPortComponentName.item(0).getFirstChild().getNodeValue(); 
//      System.out.println(servletName);
//      webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
//      webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, bindingExtData.getUrlPattern()));      
//    }

    String servletName = bindingExtData.getBindingDataName(); 
    webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
    webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));          
    webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
    
    if(webServicesExtData != null) {
      webServicesExtData.setWebResourceName(webserviceName + "Res");
      if ((bindingExtData.getAuthMethod() != null || bindingExtData.getTransportGarantee() != null)) {
        webAppElement.appendChild(createSecurityConstraint(webDescriptorDocument, webServicesExtData, bindingExtData));        
      } 
      if (bindingExtData.getAuthMethod() != null || webServicesExtData.getRealmName() != null) {
        webAppElement.appendChild(createLoginConfig(webDescriptorDocument, webServicesExtData, bindingExtData));
      }
      if (bindingExtData.getRoleName() != null) {
        webAppElement.appendChild(createSecurityRole(webDescriptorDocument, webServicesExtData, bindingExtData));
      }
    } 

    return webAppElement;
  }    
  
  private Element createTextChildElement(Document document, String childElementName, String childElementValue) {    
    Element textChildElement = document.createElement(childElementName);       
    textChildElement.appendChild(document.createTextNode(childElementValue));
    return textChildElement;                    
  }
  
  private Element createServletMappingElement(Document webDescriptorDocument, String servletName, String urlPattern) {
    Element servletMappingElement = webDescriptorDocument.createElement(SERVLET_MAPPING);        
    servletMappingElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_NAME, servletName));
    servletMappingElement.appendChild(createTextChildElement(webDescriptorDocument, URL_PATTERN, urlPattern));    
    return servletMappingElement;      
  }
  
  private Element createServletElement(Document webDescriptorDocument, String servletName, String displayName, String servletClass, int loadOnStartUp) {    
    Element servletElement = webDescriptorDocument.createElement(SERVLET);     
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_NAME, servletName));
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_CLASS, servletClass));
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, LOAD_ON_STARTUP, (new Integer(loadOnStartUp)).toString()));
    return servletElement; 
  } 
  
  private Element createSessionConfigElement(Document webDescriptorDocument) {
    Element sessionConfigElement = webDescriptorDocument.createElement(SESSION_CONFIG); 
    sessionConfigElement.appendChild(createTextChildElement(webDescriptorDocument, SESSION_TIMEOUT, new Integer(SESSION_TIMEOUT_VALUE).toString()));   
    return sessionConfigElement;
  }
  
  private Element createSecurityConstraint(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element securityConstraint = webDescriptorDocument.createElement(SECURITY_CONSTRAINT);
    securityConstraint.appendChild(createWebResourceCollection(webDescriptorDocument, webServicesExtData, bindingExtData));
    if (bindingExtData.getRoleName() != null) {
      securityConstraint.appendChild(createAuthConstraint(webDescriptorDocument, webServicesExtData, bindingExtData));    
    }
    if (bindingExtData.getTransportGarantee() != null) {
      securityConstraint.appendChild(createUserDataConstraint(webDescriptorDocument, webServicesExtData, bindingExtData));    
    }
    return securityConstraint;
  }
  
  private Element createWebResourceCollection(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element webResourceColection = webDescriptorDocument.createElement(WEB_RESOURCE_COLLECTION);
    if (webServicesExtData.getWebResourceName() != null) {
      webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, WEB_RESOURCE_NAME, webServicesExtData.getWebResourceName()));    
    }
    if (bindingExtData.getUrlPattern() != null) {
      webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, URL_PATTERN, urlPattern));    
    }
    String httpMethod = bindingExtData.getHttpMethod();
    if (httpMethod != null) {
      if ("POST".equals(httpMethod.trim()) || "GET".equals(httpMethod.trim())) {
        webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, HTPP_METHOD, httpMethod));      
      } else {
        webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, HTPP_METHOD, "POST"));  
        webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, HTPP_METHOD, "GET"));          
      }
    }

    return webResourceColection;  
  }
  
  private Element createAuthConstraint(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element authConstraint = webDescriptorDocument.createElement(AUTH_CONSTRAINT);
    authConstraint.appendChild(createTextChildElement(webDescriptorDocument, ROLE_NAME, bindingExtData.getRoleName()));
    return authConstraint;
  }
  
  private Element createUserDataConstraint(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element userDataConstraint = webDescriptorDocument.createElement(USER_DATA_CONSTRAINT);
    userDataConstraint.appendChild(createTextChildElement(webDescriptorDocument, TRANSPORT_GARANTEE, bindingExtData.getTransportGarantee()));
    return userDataConstraint;
  } 
  
  private Element createLoginConfig(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element loginConfig = webDescriptorDocument.createElement(LOGIN_CONFIG);
    if (bindingExtData.getAuthMethod() != null) {
      loginConfig.appendChild(createTextChildElement(webDescriptorDocument, AUTH_METHOD, bindingExtData.getAuthMethod()));    
    }
    if (webServicesExtData.getRealmName() != null) {
      loginConfig.appendChild(createTextChildElement(webDescriptorDocument, REALM_NAME, webServicesExtData.getRealmName()));
    }
    return loginConfig;
  }
  
  private Element createSecurityRole(Document webDescriptorDocument, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element securityRole = webDescriptorDocument.createElement(SECURITY_ROLE);
    securityRole.appendChild(createTextChildElement(webDescriptorDocument, ROLE_NAME, bindingExtData.getRoleName()));
    return securityRole;
  }
  
//  public boolean supportsFile(String moduleRelativeFileUri) {
//    return moduleRelativeFileUri.endsWith(".war") || moduleRelativeFileUri.endsWith(".jar") 
//           || moduleRelativeFileUri.endsWith(".wsar");
//  }
//  
//  public boolean removeModule(String moduleRelativeFileUri) { 
//    return false;
//  }

 /* public static void main(String[] args) throws Exception {
    J2EE14ModuleGenerator generator = new J2EE14ModuleGenerator();
    File[] f =  { new File("D:/temp/j2ee14/HSTest_ejb.jar") };
    generator.generateWebSupport("D:/temp/j2ee14",f);
  }*/
  
  public String[] getWebServicesEntry() {
    return new String[] { "Meta-inf/webservices.xml", "META-INF/webservices.xml" };
  }

  protected void generateWar(String workingDir, String warFilePath, Document webDescriptor) throws Exception {    
    String webInfDir = workingDir + "/" + WEB_INF; 
    String webClassesDir = workingDir + "/" + WEB_CLASSES_DIR;
    
    String webJ2EEEngineDescriptorPath = webInfDir + "/" + WEB_J2EE_ENGINE_XML;   

    String webDescriptorPath = webInfDir + "/" + WEB_XML;
    new File(webDescriptorPath).getParentFile().mkdirs();
    
    FileOutputStream webDescriptorOut = null; 
    
    try {
      webDescriptorOut = new FileOutputStream(webDescriptorPath); 
      domSerializer.write(webDescriptor, webDescriptorOut);
      
      webDescriptorOut = new FileOutputStream(webJ2EEEngineDescriptorPath); 
      domSerializer.write(webJ2EEEngineDocument, webDescriptorOut);
    } catch(Exception e) { 
      throw e; 
    } finally { 
      if(webDescriptorOut != null) {
        try {
          webDescriptorOut.close();   
        } catch(Exception e) {
          //$JL-EXC$
        }
      }
    }    
    
    String soapServletClassFileName = SOAP_SERVLET_CLASS_NAME + ".class"; 
    String soapServletClassFilePath = webClassesDir + "/" + soapServletClassFileName;
    new File(soapServletClassFilePath).getParentFile().mkdirs(); 
    FileOutputStream soapServletOut = null; 
    try {
      soapServletOut = new FileOutputStream(soapServletClassFilePath); 
      copy(loader.getResourceAsStream(SOAP_SERVLET_SOURCE), soapServletOut);
    } catch(Exception e) {
      //TODO
      e.printStackTrace(); 
      throw e; 
    } finally { 
      if(soapServletOut != null) {
        try {
          soapServletOut.close();   
        } catch(Exception e) {
         //$JL-EXC$
        }
      }
    }        
    
    new File(warFilePath).getParentFile().mkdirs();
    try {    
      new JarUtils().makeJarFromDir(warFilePath, workingDir);
    } catch(Exception e) {
      //TODO
      e.printStackTrace(); 
      throw e; 
    }    
  }
  
  private Document generateWebJ2EEEngineXML() {
    Document webJ2EEEngineDocument = documentBuilder.newDocument(); 
    
    Element webJ2EEEngineElement = webJ2EEEngineDocument.createElement(WEB_J2EE_ENGINE);
    webJ2EEEngineElement.appendChild(createSecurityRoleMap(EMPLOYEE, webJ2EEEngineDocument));
    webJ2EEEngineElement.appendChild(createSecurityRoleMap(ADMINISTRATOR, webJ2EEEngineDocument));
    webJ2EEEngineElement.appendChild(createSecurityRoleMap(MANAGER, webJ2EEEngineDocument)); 
    webJ2EEEngineElement.appendChild(createSecurityRoleMap(VP, webJ2EEEngineDocument));
    
    webJ2EEEngineDocument.appendChild(webJ2EEEngineElement);
    return webJ2EEEngineDocument;  
  }
  
  private Element createSecurityRoleMap(String roleName, Document webJ2EEEngineDocument) {
    Element securityRoleMap = webJ2EEEngineDocument.createElement(SECURITY_ROLE_MAP);
    securityRoleMap.appendChild(createTextChildElement(webJ2EEEngineDocument, ROLE_NAME, roleName));
    securityRoleMap.appendChild(createTextChildElement(webJ2EEEngineDocument, SERVER_ROLE_NAME, "CTS." + roleName));
    return securityRoleMap;
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
