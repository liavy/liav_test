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
import com.sap.lib.javalang.tool.ReadResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
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
import com.sap.engine.lib.xml.parser.JAXPProperties;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportBindingRT;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.AnnotationRecord.NamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationRecordImpl;
import com.sap.lib.javalang.element.impl.ClassInfoImpl;
import com.sap.lib.javalang.file.FileInfo;
import com.sap.lib.javalang.file.FolderInfo;

/**
 * Title: JEEModuleGenerator
 * Description: Module generator for webservices called in predeployment step in order to generate
 * war file, web.xml and soap servlet for every webservice described in the ear archive that is deployed.
 * Company: SAP Labs Sofia 
 */

public class JEEModuleGenerator extends WebServicesAbstractSupportHandler implements WebServicesSupportHandler {

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

  private final String WEB_J2EE_ENGINE     = "web-j2ee-engine";
  private final String WEB_J2EE_ENGINE_XML = "web-j2ee-engine.xml";
  private final String SECURITY_ROLE_MAP   = "security-role-map";
  private final String SERVER_ROLE_NAME    = "server-role-name";
  private final String EMPLOYEE            = "Employee";
  private final String ADMINISTRATOR       = "Administrator";
  private final String MANAGER             = "Manager";
  private final String VP                  = "VP"; 
  
  protected Document webJ2EEEngineDocument; 
  
  private final String WEBSERVICE_DESCRIPTION      = "webservice-description";
  private final String WEBSERVICE_DESCRIPTION_NAME = "webservice-description-name";
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
  private Hashtable<String,ArrayList> webserviceAnnotations = new Hashtable<String,ArrayList>();
  
  private String WEB_SERVICE_ANNOTATION = "javax.jws.WebService";
  private String WEB_SERVICE_PROVIDER_ANNOTATION = "javax.xml.ws.WebServiceProvider";
  
  /**
   * 
   * @throws Exception
   */
  public JEEModuleGenerator() throws Exception {
      
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    
    this.documentBuilder    = factory.newDocumentBuilder(); 
    this.domSerializer      = new DOMSerializer();
    this.loader             = this.getClass().getClassLoader();   
  }

  /**
   * 
   * 
   */
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult resultAnnotations) throws Exception {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new WebInfo[0];
    }
           
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    WebInfo[] currentWebModuleInfoes; 
    WebInfo[] newWebModuleInfoes;

    for(int i = 0; i < archiveFiles.length; i++) {
      currentWebModuleInfoes = generateWebSupport(workingDir, new JarFile(archiveFiles[i]), resultAnnotations);
      newWebModuleInfoes = new WebInfo[webModuleInfoes.length + currentWebModuleInfoes.length];
      System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
      System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
      webModuleInfoes = newWebModuleInfoes;       
    }
    return webModuleInfoes;      
  }
  
  /**
   * 
   * @param tempDir
   * @param moduleRelativeFileUri
   * @param resultAnnotations
   * @return
   * @throws Exception
   */
  private WebInfo[] generateModuleWebSupport(String tempDir, JarFile moduleRelativeFileUri, ReadResult resultAnnotations) throws Exception {
      WebInfo[] webInfo = new WebInfo[0];
      try {
            Map annotationMap = null;
            
            if(resultAnnotations != null ) {
                FileInfo[] processedFiles = resultAnnotations.getProcessedFiles();
                if(processedFiles != null && processedFiles.length > 0) {
                    annotationMap = getClassLevelAnnotations(moduleRelativeFileUri.getName(), resultAnnotations);
                    
                    if(annotationMap != null && !annotationMap.isEmpty()) {
                        if(annotationMap.containsKey(WEB_SERVICE_ANNOTATION) || annotationMap.containsKey(WEB_SERVICE_PROVIDER_ANNOTATION)) {
                            WebSupportingGenerator warGenerator = new WebSupportingGenerator();
                            warGenerator.generateModuleWebSupport(tempDir, moduleRelativeFileUri, annotationMap);
                        }
                    }
                }
            }
        } catch (Exception rtge) {
          throw rtge;
        }
        return webInfo;
  }
  /**
   * Generates .war file, web.xml and soap servlet in case of ejb archive
   * @param tempDir
   * @param moduleRelativeFileUri
   * @param resultAnnotations
   * @return
   * @throws Exception
   */
  private WebInfo[] generateModuleEjbSupport(String tempDir, JarFile moduleRelativeFileUri, ReadResult resultAnnotations) throws Exception {
      
      WebInfo[] webInfo = new WebInfo[0];
      try {
            if (webJ2EEEngineDocument == null) {
                  webJ2EEEngineDocument = generateWebJ2EEEngineXML();
            }
            
            ZipEntry webservicesEntry = moduleRelativeFileUri.getEntry("Meta-inf/webservices.xml");
            if(webservicesEntry == null) {
                webservicesEntry = moduleRelativeFileUri.getEntry("META-INF/webservices.xml");
            }
            
            ZipEntry webservicesExtEntry = moduleRelativeFileUri.getEntry("Meta-inf/webservices-j2ee-engine-ext.xml");
            if(webservicesExtEntry == null) {
                webservicesExtEntry = moduleRelativeFileUri.getEntry("META-INF/webservices-j2ee-engine-ext.xml");
            }
            
            InputStream webservicesInputStream = null;
            InputStream WebServicesExtStream   = null;
            
            try {
                
                if(webservicesEntry != null) {
                    webservicesInputStream = moduleRelativeFileUri.getInputStream(webservicesEntry);
                }
                
                Hashtable extValues = new Hashtable();
                if(webservicesExtEntry != null) {
                    WebServicesExtStream = moduleRelativeFileUri.getInputStream(webservicesExtEntry);
                    getExtValues(WebServicesExtStream, extValues);    
                }
                
                Document webservicesDoc = null;
                if(webservicesInputStream != null ) { 
                    webservicesDoc = documentBuilder.parse(webservicesInputStream);
                }

                if(resultAnnotations != null ) {
                    FileInfo[] processedFiles = resultAnnotations.getProcessedFiles();
                    if(processedFiles != null && processedFiles.length > 0) {
                        Map annotationMap = annotationMap = getClassLevelAnnotations(moduleRelativeFileUri.getName(), resultAnnotations);
                        
                        if(annotationMap != null && !annotationMap.isEmpty()) {
                            if(annotationMap.containsKey(WEB_SERVICE_ANNOTATION) || annotationMap.containsKey(WEB_SERVICE_PROVIDER_ANNOTATION)) {
                                getAnnotationValues(WEB_SERVICE_ANNOTATION, annotationMap);
                                getAnnotationValues(WEB_SERVICE_PROVIDER_ANNOTATION, annotationMap);
                            }
                        }
                    }
                }
                
                if(!webserviceAnnotations.isEmpty()) {
                      Hashtable portComponentNames = getPortComponentNames(webservicesDoc);
                      
                      WebInfo[] infoArr1 = generateModuleSupportAnnotations(new File(tempDir), extValues, portComponentNames);
                      WebInfo[] infoArr2 = new WebInfo[0];
                      
                      if (portComponentNames.size() > 0) {
                          infoArr2 = generateModuleSupportWebservicesXml(new File(tempDir), webservicesDoc, extValues);
                      }
                      
                      WebInfo[] infoArr  = new WebInfo[infoArr1.length + infoArr2.length];
                      System.arraycopy(infoArr1, 0, infoArr, 0, infoArr1.length);
                      System.arraycopy(infoArr2, 0, infoArr, infoArr1.length, infoArr2.length);

                      webserviceAnnotations     = null;
                      webInfo = infoArr;
                }
                else {
                    webInfo = generateModuleSupportWebservicesXml(new File(tempDir), webservicesDoc, extValues);
                }
            }
            finally {
                if (webservicesInputStream != null) {
                    webservicesInputStream.close(); 
                }
                
                if(WebServicesExtStream != null) {
                    WebServicesExtStream.close();
                }
            }
        } catch (Exception rtge) {
          throw rtge;
        }
        return webInfo;
  }
  
  /**
   * 
   * @param tempDir
   * @param moduleRelativeFileUri
   * @param resultAnnotations
   * @return
   * @throws Exception
   */
  public WebInfo[] generateWebSupport(String tempDir, JarFile moduleRelativeFileUri, ReadResult resultAnnotations) throws Exception {
    
      WebInfo[] webInfo = new WebInfo[0];
      try {
          if (moduleRelativeFileUri.getName().endsWith(".jar")) {
              webInfo = generateModuleEjbSupport(tempDir, moduleRelativeFileUri, resultAnnotations);
          }
          else if (moduleRelativeFileUri.getName().endsWith(".war")){
              webInfo = generateModuleWebSupport(tempDir, moduleRelativeFileUri, resultAnnotations);
          }
      
    } catch (Exception rtge) {
      throw rtge;
    }
    
    return webInfo;
}

  /**
   * Returns Hashtable with port-component-name values in webservices.xml
   * @param webservicesDoc
   * @return
   * @throws Exception
   */
  private Hashtable getPortComponentNames(Document webservicesDoc) throws Exception {
      Hashtable<String,String> resultPortComponentNames = new Hashtable<String,String>();
      
      if (webservicesDoc != null) {
          Element webservices         = webservicesDoc.getDocumentElement();  
          NodeList portComponentNames = webservices.getElementsByTagName(PORT_COMPONENT_NAME);
          
          for (int i = 0; i < portComponentNames.getLength(); i++) {
              String name = portComponentNames.item(i).getFirstChild().getNodeValue();
              resultPortComponentNames.put(name, name);
          }
      }
      return resultPortComponentNames;
  }
  
  /**
   * 
   * @param tempDir
   * @param webservicesDoc
   * @param extValues
   * @return
   * @throws Exception
   */
  private WebInfo[] generateModuleSupportWebservicesXml(File tempDir, Document webservicesDoc, Hashtable extValues) throws Exception {
      
    if(webservicesDoc == null) {
        return new WebInfo[0];
    }
    Vector webInfoArr = new Vector();   
    String webserviceName;  
    String tempWorkingDir = tempDir + "";
    String contextRoot = null;
    String warFilePath = "";
     
    Element webservices = webservicesDoc.getDocumentElement();  
    NodeList nlWebServiceDescription = webservices.getElementsByTagName(WEBSERVICE_DESCRIPTION);
    for (int i = 0; i < nlWebServiceDescription.getLength(); i++) {
      NodeList nlWebserviceName = ((Element)nlWebServiceDescription.item(i)).getElementsByTagName(WEBSERVICE_DESCRIPTION_NAME);
      NodeList nlPortComponents = ((Element)nlWebServiceDescription.item(i)).getElementsByTagName(PORT_COMPONENT);
      webserviceName = nlWebserviceName.item(0).getFirstChild().getNodeValue().trim();
      
      WebServicesExtData webServicesExtData = (WebServicesExtData)extValues.get(webserviceName);  
      if (webServicesExtData != null && webServicesExtData.getContextRoot() != null) {
        contextRoot = webServicesExtData.getContextRoot();
      } else {
        contextRoot = webserviceName;
      }
      if (!contextRoot.equals("")) {
        try {
          tempWorkingDir     = tempDir + "/" + webserviceName;
          warFilePath        = tempWorkingDir + ".war";
          generateWar(tempWorkingDir, createWebDescriptorMultipleMode(webserviceName, nlPortComponents, webServicesExtData, null));
          FileUtils.deleteDirectory(new File(tempWorkingDir));
        } catch(Exception e) {
          throw e; 
        }
          webInfoArr.addElement(new WebInfo(contextRoot, warFilePath));
        } else {     
          Hashtable webserviceBindingData = webServicesExtData.getBindingData(); 
          Enumeration bindingDataNames = webserviceBindingData.keys();
          while (bindingDataNames.hasMoreElements()) {
            String wsBindingDataName = (String) bindingDataNames.nextElement();
            try {  
                 tempWorkingDir     = tempDir + "/" + wsBindingDataName;
                 warFilePath        = tempWorkingDir + ".war";
                 BindingExtData bindingExtData = (BindingExtData)webServicesExtData.getBindingData().get(wsBindingDataName);
                 generateWar(tempWorkingDir, createWebDescriptorSingleMode(wsBindingDataName, webServicesExtData, bindingExtData));
                 FileUtils.deleteDirectory(new File(tempWorkingDir));
                 webInfoArr.addElement(new WebInfo(bindingExtData.getUrlPattern(), warFilePath)); 
              } catch(Exception e) {
                throw e; 
               }            
          }
        }
    }
    
    WebInfo[] result = new WebInfo[webInfoArr.size()];
    webInfoArr.copyInto(result);
    return result;
  }
   
  /**
   * Stores data from webservices-j2ee-engine-ext.xml in Hashtable
   * @param webservicesJ2EEEngineExtIn
   * @param extValues
   * @throws Exception
   */
  private void getExtValues(InputStream webservicesJ2EEEngineExtIn, Hashtable extValues) throws Exception {     
    try {
        Element webservicesExt    = documentBuilder.parse(webservicesJ2EEEngineExtIn).getDocumentElement();
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
              webServicesExtData.getBindingData().put(bindingDataNode.getNodeValue().trim(), bindingExtData);            
          }
          webServicesExtData.setWebserviceName(webserviceName);
          extValues.put(webserviceName, webServicesExtData);
        }
    } catch (Exception e) {
        throw e;
    }     
  }

  /**
   * 
   * @param webserviceName
   * @param portComponents
   * @param webServicesExtData
   * @param webserviceAnnotations
   * @return
   */
  private Document createWebDescriptorMultipleMode(String webserviceName, NodeList portComponents, WebServicesExtData webServicesExtData, ArrayList<AnnotationData> webserviceAnnotations) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementMulipleMode(webDescriptorDocument, portComponents, webserviceName, webServicesExtData, webserviceAnnotations));    
    return webDescriptorDocument;
  }
  
  /**
   * 
   * @param webDescriptorDocument
   * @return
   */
  private Element createWebAppElement(Document webDescriptorDocument) {
      Element webAppElement = webDescriptorDocument.createElement(WEB_APP);
      return webAppElement;
  }
  /**
   * @param webDescriptorDocument
   * @param portComponents
   * @param webserviceName
   * @param webServicesExtData
   * @param webserviceAnnotations
   * @return
   */   
  private Element createWebAppElementMulipleMode(Document webDescriptorDocument, NodeList portComponents, String webserviceName, WebServicesExtData webServicesExtData, ArrayList<AnnotationData> webserviceAnnotations) {               
    Element webAppElement = createWebAppElement(webDescriptorDocument);
    
    if (webServicesExtData != null) {//webservices-j2ee-engine-ext.xml exists
      Hashtable bindingDataUrls = webServicesExtData.getBindingData(); 
      
      if (bindingDataUrls.size() > 0 ) {
          webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));        
          setServletElements(webDescriptorDocument, webAppElement, bindingDataUrls, webserviceName);
          setServletMappingElements(webDescriptorDocument, webAppElement, bindingDataUrls, webserviceName); 
          webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
      }
    } else {
        
      if (portComponents != null) {
          webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));    
          for (int i = 0; i < portComponents.getLength(); i++) {
            NodeList nlPortComponentName = ((Element)portComponents.item(i)).getElementsByTagName(PORT_COMPONENT_NAME);
            String servletName = nlPortComponentName.item(0).getFirstChild().getNodeValue(); 
            webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
            webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));      
          }
          webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument)); 
      }
      else if(webserviceAnnotations != null) {
          webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));
          for(int annIx = 0; annIx < webserviceAnnotations.size(); annIx ++) {
              
              AnnotationData annData = (AnnotationData)webserviceAnnotations.get(annIx);

              String servletName     = annData.getBindingDataName();
              webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
              webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));     
          }
          webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
      }
      
//      webServicesExtData.setWebResourceName(webserviceName + "Res");
//      webAppElement.appendChild(createSecurityConstraint(webDescriptorDocument, webServicesExtData));
//      webAppElement.appendChild(createLoginConfig(webDescriptorDocument, webServicesExtData));
//      webAppElement.appendChild(createSecurityRole(webDescriptorDocument, webServicesExtData));
    }
    return webAppElement;
  }  
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webAppElement
   * @param bindingDataUrls
   * @param webserviceName
   */
  private void setServletElements(Document webDescriptorDocument, Element webAppElement, Hashtable bindingDataUrls, String webserviceName) {
    Enumeration bindingDataNames = bindingDataUrls.keys();
    while (bindingDataNames.hasMoreElements()) {
      String bindingDataName = (String) bindingDataNames.nextElement();
      webAppElement.appendChild(createServletElement(webDescriptorDocument, bindingDataName, bindingDataName, SOAP_SERVLET_CLASS_NAME, 0));    
    }
  }      
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webAppElement
   * @param bindingDataUrls
   * @param webserviceName
   */
  private void setServletMappingElements(Document webDescriptorDocument, Element webAppElement, Hashtable bindingDataUrls, String webserviceName) {
    Enumeration bindingDataNames = bindingDataUrls.keys();
    while (bindingDataNames.hasMoreElements()) {
      String bindingDataName = (String)bindingDataNames.nextElement();
      String bindingDataUrl  = null;
      
      BindingExtData bindingData = (BindingExtData)bindingDataUrls.get(bindingDataName);
      
      if ( bindingData != null ) {
          bindingDataUrl = bindingData.getUrlPattern();
      }
      webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, bindingDataName, (bindingDataUrl != null) ? bindingDataUrl : "/*"));
    }
  }  
  
  /**
   * 
   * @param webserviceName
   * @param webServicesExtData
   * @param bindingExtData
   * @return
   */
  private Document createWebDescriptorSingleMode(String webserviceName, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementSingleMode(webDescriptorDocument, webserviceName, webServicesExtData, bindingExtData));
    return webDescriptorDocument;
  }
  
  /**
   * 
   * @param webserviceName
   * @param annotationData
   * @return
   */
  private Document createWebDescriptorSingleMode(String webserviceName, AnnotationData annotationData) {
      Document webDescriptorDocument = documentBuilder.newDocument(); 
      webDescriptorDocument.appendChild(createWebAppElementSingleMode(webDescriptorDocument, webserviceName, annotationData));
      return webDescriptorDocument;
    }
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webserviceName
   * @param annotationData
   * @return
   */
  private Element createWebAppElementSingleMode(Document webDescriptorDocument, String webserviceName, AnnotationData annotationData) {
      Element webAppElement = createWebAppElement(webDescriptorDocument);
      
      String servletName = webserviceName + "_"  + annotationData.getBindingDataName();
      //String servletName = annotationData.getBindingDataName(); 
      webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, servletName));    

      webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
      webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));          
      webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
      
      return webAppElement;
    }    
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webserviceName
   * @param webServicesExtData
   * @param bindingExtData
   * @return
   */
  private Element createWebAppElementSingleMode(Document webDescriptorDocument, String webserviceName, WebServicesExtData webServicesExtData, BindingExtData bindingExtData) {
    Element webAppElement = createWebAppElement(webDescriptorDocument); 
    
    webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, webserviceName));    

    String servletName = bindingExtData.getBindingDataName(); 
    webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
    webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, urlPattern));          
    webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument));
    
    if(webServicesExtData != null) {
      webServicesExtData.setWebResourceName(webserviceName + "Res");
      if ((bindingExtData.getAuthMethod() != null || bindingExtData.getTransportGarantee() != null)) {
        webAppElement.appendChild(createSecurityConstraint(webDescriptorDocument, webServicesExtData.getWebResourceName(), bindingExtData));        
      } 
      if (bindingExtData.getAuthMethod() != null || webServicesExtData.getRealmName() != null) {
        webAppElement.appendChild(createLoginConfig(webDescriptorDocument, webServicesExtData, bindingExtData));
      }
      if (bindingExtData.getRoleName() != null) {
        webAppElement.appendChild(createSecurityRole(webDescriptorDocument, bindingExtData));
      }
    } 
    return webAppElement;
  }    
  /**
   * 
   * @param document
   * @param childElementName
   * @param childElementValue
   * @return
   */
 /* private Element createTextChildElement(Document document, String childElementName, String childElementValue) {    
    Element textChildElement = document.createElement(childElementName);       
    textChildElement.appendChild(document.createTextNode(childElementValue));
    return textChildElement;                    
  }*/
  
  /**
   * 
   * @param webDescriptorDocument
   * @param servletName
   * @param urlPattern
   * @return
   */
/*  private Element createServletMappingElement(Document webDescriptorDocument, String servletName, String urlPattern) {
    Element servletMappingElement = webDescriptorDocument.createElement(SERVLET_MAPPING);        
    servletMappingElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_NAME, servletName));
    servletMappingElement.appendChild(createTextChildElement(webDescriptorDocument, URL_PATTERN, urlPattern));    
    return servletMappingElement;      
  }*/
  
  /**
   * 
   * @param webDescriptorDocument
   * @param servletName
   * @param displayName
   * @param servletClass
   * @param loadOnStartUp
   * @return
   */
/*  private Element createServletElement(Document webDescriptorDocument, String servletName, String displayName, String servletClass, int loadOnStartUp) {    
    Element servletElement = webDescriptorDocument.createElement(SERVLET);     
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_NAME, servletName));
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, SERVLET_CLASS, servletClass));
    servletElement.appendChild(createTextChildElement(webDescriptorDocument, LOAD_ON_STARTUP, (new Integer(loadOnStartUp)).toString()));
    return servletElement; 
  } */
  
  /**
   * 
   * @param webDescriptorDocument
   * @return
   */
  private Element createSessionConfigElement(Document webDescriptorDocument) {
    Element sessionConfigElement = webDescriptorDocument.createElement(SESSION_CONFIG); 
    sessionConfigElement.appendChild(createTextChildElement(webDescriptorDocument, SESSION_TIMEOUT, new Integer(SESSION_TIMEOUT_VALUE).toString()));   
    return sessionConfigElement;
  }
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webResourceName
   * @param bindingExtData
   * @return
   */
  private Element createSecurityConstraint(Document webDescriptorDocument, String webResourceName, BindingExtData bindingExtData) {
    Element securityConstraint = webDescriptorDocument.createElement(SECURITY_CONSTRAINT);
    
    securityConstraint.appendChild(createWebResourceCollection(webDescriptorDocument, webResourceName, bindingExtData));
    if (bindingExtData.getRoleName() != null) {
      securityConstraint.appendChild(createAuthConstraint(webDescriptorDocument, bindingExtData));    
    }
    if (bindingExtData.getTransportGarantee() != null) {
      securityConstraint.appendChild(createUserDataConstraint(webDescriptorDocument, bindingExtData));    
    }
    return securityConstraint;
  }
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webresourceName
   * @param bindingExtData
   * @return
   */
  private Element createWebResourceCollection(Document webDescriptorDocument, String webresourceName, BindingExtData bindingExtData) {
    Element webResourceColection = webDescriptorDocument.createElement(WEB_RESOURCE_COLLECTION);
    
    if (webresourceName != null) {
      webResourceColection.appendChild(createTextChildElement(webDescriptorDocument, WEB_RESOURCE_NAME, webresourceName));    
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
  
  /**
   * 
   * @param webDescriptorDocument
   * @param bindingExtData
   * @return
   */
  private Element createAuthConstraint(Document webDescriptorDocument, BindingExtData bindingExtData) {
    Element authConstraint = webDescriptorDocument.createElement(AUTH_CONSTRAINT);
    authConstraint.appendChild(createTextChildElement(webDescriptorDocument, ROLE_NAME, bindingExtData.getRoleName()));
    return authConstraint;
  }
  
  /**
   * 
   * @param webDescriptorDocument
   * @param bindingExtData
   * @return
   */
  private Element createUserDataConstraint(Document webDescriptorDocument, BindingExtData bindingExtData) {
    Element userDataConstraint = webDescriptorDocument.createElement(USER_DATA_CONSTRAINT);
    userDataConstraint.appendChild(createTextChildElement(webDescriptorDocument, TRANSPORT_GARANTEE, bindingExtData.getTransportGarantee()));
    return userDataConstraint;
  } 
  
  /**
   * 
   * @param webDescriptorDocument
   * @param webServicesExtData
   * @param bindingExtData
   * @return
   */
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
  
  /**
   * 
   * @param webDescriptorDocument
   * @param bindingExtData
   * @return
   */
  private Element createSecurityRole(Document webDescriptorDocument, BindingExtData bindingExtData) {
    Element securityRole = webDescriptorDocument.createElement(SECURITY_ROLE);
    securityRole.appendChild(createTextChildElement(webDescriptorDocument, ROLE_NAME, bindingExtData.getRoleName()));
    return securityRole;
  }
  
  public String[] getWebServicesEntry() {
    return new String[] { "Meta-inf/webservices.xml", "META-INF/webservices.xml" };
  }

 /* *//**
   * 
   * @param workingDir
   * @param webDescriptor
   * @throws Exception
   *//*
  protected void generateWar(String workingDir, Document webDescriptor) throws Exception {
    String warFilePath = workingDir + ".war";
    String webInfDir = workingDir + "/" + WEB_INF; 
    String webClassesDir = workingDir + "/" + WEB_CLASSES_DIR;
    
    String webJ2EEEngineDescriptorPath = webInfDir + "/" + WEB_J2EE_ENGINE_XML;   

    String webDescriptorPath = webInfDir + "/" + WEB_XML;
    new File(webDescriptorPath).getParentFile().mkdirs();
    
    FileOutputStream webDescriptorOut = null; 
    FileOutputStream webDescriptorOutEngineDescr = null; 
    try {
      webDescriptorOut = new FileOutputStream(webDescriptorPath); 
      domSerializer.write(webDescriptor, webDescriptorOut);
      
      webDescriptorOutEngineDescr = new FileOutputStream(webJ2EEEngineDescriptorPath); 
      domSerializer.write(webJ2EEEngineDocument, webDescriptorOutEngineDescr);
    } catch(Exception e) { 
      throw e; 
    } finally { 
      try{
    	if(webDescriptorOut != null) {
           webDescriptorOut.close();
          }
      } catch(Exception e){
    		  // $JL-EXC$
    	}
    	try{
    		if (webDescriptorOutEngineDescr != null){
    			webDescriptorOutEngineDescr.close();
    		}
    	}catch (Exception e) {
  		  // $JL-EXC$
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
      e.printStackTrace(); 
      throw e; 
    } finally { 
      if(soapServletOut != null) {
          soapServletOut.close();   
      }
    }        
    
    new File(warFilePath).getParentFile().mkdirs();
    try {    
      new JarUtils().makeJarFromDir(warFilePath, workingDir);
    } catch(Exception e) {
      e.printStackTrace(); 
      throw e; 
    }    
  }*/
  
  /**
   * 
   * @return
   */
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
  
  /**
   * 
   * @param roleName
   * @param webJ2EEEngineDocument
   * @return
   */
  private Element createSecurityRoleMap(String roleName, Document webJ2EEEngineDocument) {
    Element securityRoleMap = webJ2EEEngineDocument.createElement(SECURITY_ROLE_MAP);
    securityRoleMap.appendChild(createTextChildElement(webJ2EEEngineDocument, ROLE_NAME, roleName));
    securityRoleMap.appendChild(createTextChildElement(webJ2EEEngineDocument, SERVER_ROLE_NAME, "CTS." + roleName));
    return securityRoleMap;
  }
  
  /**
   * 
   * @param in
   * @param out
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out) throws IOException {
    int  bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int count = 0;
    while ( (count = in.read(buffer)) != -1) {
     out.write(buffer, 0 , count);
    }
    out.flush();
  } 
   
  /**
   * 
   * @param jarPath
   * @param resultAnnotations
   * @return
   */
  private Map getClassLevelAnnotations(String jarPath, ReadResult resultAnnotations)
  {
      FileInfo[] files  = ((FolderInfo)resultAnnotations.getProcessedFiles()[0]).getFiles();

      for(int i = 0; i < files.length; i ++) {
          FileInfo fileInfo = files[i];
          
          if (jarPath.endsWith(fileInfo.getName())) {
              return fileInfo.getClassLevelAnnotations();
          }
          
          int length = fileInfo.getName().length();
          
          if(fileInfo.getName().endsWith(".extracted.war")) {
              int endIx = length - 14; 
              String warDirName  = fileInfo.getName().substring(0, endIx);
              warDirName += ".war";
              
              if (jarPath.endsWith(warDirName)) {
                  return fileInfo.getClassLevelAnnotations();
              }
          }
      }      return null;
  }

 /**
  *  Process WebService and WebServiceProvider annotations excluding when annotation values exist in webservices.xml or when 
  *  class of the annotation is interface. 
  *  For every binding data from annotations searches the corresponding binding data in webservices-j2ee-engine-ext.xml.
  *  Generates war module 
  * @param tempDir
  * @param webInfoArr
  * @param extValues
  * @param portComponentNames
  * @throws Exception
  */
  private void generateModuleSupportAnnotations(File tempDir, Vector webInfoArr, Hashtable extValues, Hashtable portComponentNames) throws Exception{
      Enumeration webservices = webserviceAnnotations.elements();
      while(webservices.hasMoreElements()) { 
          ArrayList webServiceAnnotations = (ArrayList)webservices.nextElement();
          WebServicesExtData extData = null;
          boolean skipWebService     = false;
          
          for(int i = 0; i < webServiceAnnotations.size(); i ++ ) {
              AnnotationData annotationData = (AnnotationData)webServiceAnnotations.get(i);
              String bindingDataKey  = (String)annotationData.getBindingDataName();
              String annClassName    = annotationData.getFullClassName();
              
              if(portComponentNames.containsKey(bindingDataKey) || portComponentNames.containsKey(getClassName(annClassName)) || portComponentNames.containsKey(annClassName)) {
                 skipWebService = true;
                 break;
              }
              
              extData = getExtData(bindingDataKey, extValues, annClassName);
              
              if (extData != null) {
                  break;
              }
          }
          
          if(skipWebService) {
              continue;
          }
          
          if (extData != null) {
              // at least one binding-data is found in the data structure of webservices-j2ee-engine-ext.xml,
              // process all binding data of the webservice 
              
              String webserviceName  = extData.getWebServiceName();
              String contextRoot     = extData.getContextRoot();
              
              // context-root is null or is not empty string, create general .war
              if (contextRoot == null || !contextRoot.trim().equals("")) { 
                  
                  if (contextRoot == null) {
                      contextRoot = GenerateContextRootMultipleMode(webServiceAnnotations);
                  }
                  
                  if (contextRoot == null) {
                      // context root is null, presume there is one binding data 
                      
                      BindingExtData bindingData = (BindingExtData)extData.getBindingData().values().toArray()[0];
                      contextRoot     = GenerateContextRootSingleMode(webServiceAnnotations, bindingData.getBindingDataName());
                      String tempWorkingDir  = tempDir + "/" + contextRoot;
                      createWebInfoElement(webInfoArr, contextRoot, createWebDescriptorSingleMode(webserviceName, extData, bindingData), tempWorkingDir);
                  }
                  else {
                      String tempWorkingDir      = tempDir + "/" + webserviceName;
                      createWebInfoElement(webInfoArr, contextRoot, createWebDescriptorMultipleMode(webserviceName, null, extData, null), tempWorkingDir);
                  }
              } else if(contextRoot.equals("")) {     
                // context-root is empty string, create .war for every binding data
                  
                Hashtable bindingExtData = extData.getBindingData();
                Enumeration bindingExtDataValues = bindingExtData.elements();

                while (bindingExtDataValues.hasMoreElements()) {
                    BindingExtData bindingData = (BindingExtData)bindingExtDataValues.nextElement();
                    
                    String bdName = getClassName(bindingData.getBindingDataName());
                    String tempWorkingDir  = tempDir + "/" + bdName;
                    Document webDescriptorSingleDocument = createWebDescriptorSingleMode(bdName, extData, bindingData);
                    createWebInfoElement(webInfoArr, bindingData.getUrlPattern(), webDescriptorSingleDocument, tempWorkingDir);
                }
              }
          } else {              
              // binding data is not found, get information from annotations
        	if(containsAltPathOR(webServiceAnnotations)) {
        	  generateWebSupportSingleMode(webServiceAnnotations, tempDir.getAbsolutePath(), webInfoArr);  	  
        	} else {        	  
              String contextRoot  = GenerateContextRootMultipleMode(webServiceAnnotations);
              if(contextRoot == null) {
                  // context root is null, presume there is one binding data
                // TODO - check if such a scenario exists
                  AnnotationData annData = (AnnotationData)webServiceAnnotations.get(0);
                  contextRoot            = GenerateContextRootSingleMode(webServiceAnnotations, annData.getBindingDataName());
                  String tempWorkingDir  = tempDir + "/" + contextRoot;
                  createWebInfoElement(webInfoArr, contextRoot, createWebDescriptorSingleMode(contextRoot, annData), tempWorkingDir);
              } else {
                  String tempWorkingDir  = tempDir + "/" + contextRoot;
                  Document webDescriptor = createWebDescriptorMultipleMode(contextRoot, null, null, webServiceAnnotations);
                  createWebInfoElement(webInfoArr, contextRoot, webDescriptor, tempWorkingDir);
              }
          }
      }
  }
  }  
  
  /**
   * 
   * @param webInfoArr
   * @param contextRoot
   * @param webDescriptor
   * @param tempWorkingDir
   * @throws Exception
   */
  private void createWebInfoElement(Vector webInfoArr, String contextRoot, Document webDescriptor, String tempWorkingDir) throws Exception {
      String fileWarPath    = tempWorkingDir + ".war";
      generateWar(tempWorkingDir, webDescriptor);
      webInfoArr.addElement(new WebInfo(contextRoot, fileWarPath));
      FileUtils.deleteDirectory(new File(tempWorkingDir));
  }
  
 /**
  * 
  * @param webServiceAnnotations
  * @return
  */
   private String GenerateContextRootMultipleMode(ArrayList webServiceAnnotations) {
      if (webServiceAnnotations.size() > 0) {
          AnnotationData annData = (AnnotationData)webServiceAnnotations.get(0);

          if(!annData.getServiceName().equals("")) {
                  return annData.getServiceName();
              }
      }
      return null;
  }
  
   /**
    * 
    * @param webServiceAnnotations
    * @param bindingDataKey
    * @return
    */
   private String GenerateContextRootSingleMode(ArrayList webServiceAnnotations, String bindingDataKey) {
       if (webServiceAnnotations.size() > 0) {
           
           for(int ix=0; ix<webServiceAnnotations.size(); ix++) {
               AnnotationData annData = (AnnotationData)webServiceAnnotations.get(ix);
               
               String annName = annData.getBindingDataName();
               if(annName.equals(bindingDataKey) || annName.equals(annData.getFullClassName()) || annName.equals(getClassName(annData.getFullClassName()))) {
                   return getClassName(annData.getFullClassName()) + "Service";
               }
           }
       }
       return null;
   }
   
   /**
    * 
    * @param fullClassName
    * @return
    */
   private String getClassName(String packageClassName) {
       if(packageClassName.lastIndexOf(".") > -1) {
           return packageClassName.substring(packageClassName.lastIndexOf(".") + 1); 
       }
       return packageClassName;    
   }
   
   /**
    * 
    * @param bindingDataKey
    * @param extValues
    * @param fullClassName
    * @return
    */
  private WebServicesExtData getExtData(String bindingDataKey, Hashtable extValues, String fullClassName) {
      Enumeration extDataKeys = extValues.keys();
      while (extDataKeys.hasMoreElements()) {
        String webserviceName         = (String) extDataKeys.nextElement();
        WebServicesExtData extData    = (WebServicesExtData)extValues.get(webserviceName);
        Hashtable bindingExtData      = extData.getBindingData();
        
        if (bindingExtData != null) {
            if(bindingExtData.containsKey(bindingDataKey) || bindingExtData.containsKey(getClassName(fullClassName)) || bindingExtData.containsKey(fullClassName)) {
                return extData;
            }
        }
      }          
      return null; 
  }
  
  /**
   * Gets annotations of annotationType from annotationMap
   * Puts founded annotations in webserviceAnnotations hashtable
   * @param annotationType
   * @param annotationMap
   */
  private void getAnnotationValues(String annotationType, Map annotationMap) {
      ArrayList annDataList   = (ArrayList)annotationMap.get(annotationType);
      if (annDataList == null) {
          return;
      }
    
    for (int i = 0; i < annDataList.size(); i ++ ) {
          AnnotationRecordImpl annRecord      = (AnnotationRecordImpl)annDataList.get(i);
          ClassInfoImpl elementInfo           = (ClassInfoImpl)annRecord.getOwner();
      Map annotationMap_ = elementInfo.getAnnotations(); 
          
          if(!elementInfo.isInterface()) {
              String annWsdlLocation = AnnotationTools.getAnnotationAttribute(annRecord, "wsdlLocation");
              String serviceName     = AnnotationTools.getAnnotationAttribute(annRecord, "serviceName");
              String annNameValue    = "";
              
              if(annotationType.equals(WEB_SERVICE_ANNOTATION)) {
                  annNameValue = AnnotationTools.getAnnotationAttribute(annRecord, "name");
                  
                  if(annNameValue == null || annNameValue.equals("") ) {
                      annNameValue = AnnotationTools.getAnnotationClassName(annRecord);
                  }
        } else {
          annNameValue = AnnotationTools.getAnnotationClassName(annRecord);
        }
        
        String altPath = null; 
        AnnotationRecord trBindingAnnotationRecord = (AnnotationRecord)annotationMap_.get(TransportBindingRT.class.getName());
        if(trBindingAnnotationRecord != null) {
          NamedMember altPathMember = trBindingAnnotationRecord.getMember("AltPath");
          if(altPathMember != null) {
            altPath = altPathMember.getStringValue(); 
              }
              }
              
              AnnotationData data = new AnnotationData();
              data.setBindingDataName(annNameValue);
              data.setServiceName(serviceName);
              data.setFullClassName(AnnotationTools.getAnnotationClassName(annRecord));
        if(altPath != null && !altPath.equals("")) {
          data.setAltPath(altPath);
        }              
              
              if(annWsdlLocation.equals("")) {
                  annWsdlLocation = annNameValue;
              }
              
              if(webserviceAnnotations.containsKey(annWsdlLocation)) {
                  webserviceAnnotations.get(annWsdlLocation).add(data);
        }  else {
                  ArrayList<AnnotationData> webServiceAnnotations = new ArrayList<AnnotationData>();
                  webServiceAnnotations.add(data);
                  webserviceAnnotations.put(annWsdlLocation, webServiceAnnotations);
              }
          }
      }
  }
  
  /**
   * 
   * @param tempDir
   * @param extValues
   * @param portComponentNames
   * @return
   * @throws Exception
   */
  private WebInfo[] generateModuleSupportAnnotations(File tempDir, Hashtable extValues, Hashtable portComponentNames) throws Exception {
      Vector webInfoArr = new Vector();   
      generateModuleSupportAnnotations(tempDir, webInfoArr, extValues, portComponentNames);
      
      if (webInfoArr.size() > 0) {
        WebInfo[] result = new WebInfo[webInfoArr.size()];
        webInfoArr.copyInto(result);
        return result;
      }

      return new WebInfo[0];
    }
    
  private boolean containsAltPathOR(ArrayList<AnnotationData> wsAnnotationDatas) {
    if(wsAnnotationDatas == null || wsAnnotationDatas.size() == 0) {
      return false; 
    }
    
    for(AnnotationData wsAnnotationData: wsAnnotationDatas) {
      if(wsAnnotationData.getAltPath() != null) {
        return true;   
      }	
    }
    
    return false; 
  }
  
  private void generateWebSupportSingleMode(ArrayList<AnnotationData> wsAnnotationDatas, String tempDir, Vector webInfoArr) throws Exception {	  
    if(wsAnnotationDatas == null || wsAnnotationDatas.size() == 0) {
      return; 	
    }
    
    Document webDescriptor; 
    String contextRoot; 
    for(AnnotationData wsAnnotationData: wsAnnotationDatas) {
      webDescriptor = createWebDescriptorSingleMode(wsAnnotationData.getServiceName(), wsAnnotationData);
      contextRoot = wsAnnotationData.getAltPath(); 
      if(contextRoot == null) {
        contextRoot = wsAnnotationData.getServiceName() + "/" + wsAnnotationData.getBindingDataName(); 	  
      }
      createWebInfoElement(webInfoArr, contextRoot, webDescriptor, tempDir + "/" + wsAnnotationData.getServiceName() + "_" + wsAnnotationData.getBindingDataName()); 
    }    		
  } 
  
}

