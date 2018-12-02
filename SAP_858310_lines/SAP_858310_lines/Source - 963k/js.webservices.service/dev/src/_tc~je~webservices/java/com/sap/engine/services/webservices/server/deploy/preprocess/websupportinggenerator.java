package com.sap.engine.services.webservices.server.deploy.preprocess;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.sap.engine.lib.descriptors5.javaee.DisplayNameType;
import com.sap.engine.lib.descriptors5.web.SessionConfigType;
import com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors5.javaee.UrlPatternType;
import com.sap.engine.lib.descriptors5.javaee.XsdIntegerType;
import com.sap.engine.lib.descriptors5.web.ServletNameType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.ServletMappingType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.descriptors5.web.WebAppVersionType;
import com.sap.engine.lib.descriptors5.web.WebAppType.Choice1;
import com.sap.engine.services.webservices.server.deploy.jee5.BuildJarFile;
import com.sap.lib.javalang.annotation.impl.AnnotationRecordImpl;
import com.sap.lib.javalang.element.impl.ClassInfoImpl;

/**
 * 
 * Title: WebSupportingGenerator
 * Description: web.xml generator called in predeployment step in order to generate new web.xml or add servlets from annotations 
 *
 * @author Tatyana Nalbantova
 */
public class WebSupportingGenerator {
    
    private String WEB_SERVICE_ANNOTATION = "javax.jws.WebService";
    private String WEB_SERVICE_PROVIDER_ANNOTATION = "javax.xml.ws.WebServiceProvider";
    protected static final String WEB_INF         = "WEB-INF";
    
    ArrayList<Choice1> generatedServlets         = new ArrayList<Choice1>();
    ArrayList<Choice1> generatedServletMappings  = new ArrayList<Choice1>();

    /**
     * Add all existing servlet classes in web.xml to HashSet
     * @param choiceGroup
     * @return
     * @throws Exception
     */
    private HashSet<String> getWebXmlServlets(Choice1[] choiceGroup) throws Exception{
        HashSet<String> servletClasses = new HashSet<String>();
        
        if (choiceGroup.length > 0) {
            for(int i=0; i < choiceGroup.length; i ++) {
                Choice1 choice1Element = choiceGroup[i];
                
                if ( choice1Element.isSetServlet() ) {
                    ServletType servletType = choice1Element.getServlet();                   
                    
                    if(servletType != null) {                    	
                        FullyQualifiedClassType fullyQualyfiedClassType = servletType.getChoiceGroup1().getServletClass();
                        if(fullyQualyfiedClassType != null) {
                          String servletClassName = fullyQualyfiedClassType.get_value();                        
                          if(servletClassName != null && !servletClassName.equals("")) {
                            servletClasses.add(servletClassName);
                          }
                        }
                    }
                }
            }
        }
        return servletClasses;
    }
    
   /**
    * Generates new Choice1 array 
    * @return
    */
    private Choice1[] generateNewChoice1Group()
    {
        Choice1[] choiceGroup;
        int size = generatedServlets.size() + generatedServletMappings.size();
        
        int newSize = size + 2;
        choiceGroup = new Choice1[newSize];
        
        String displayName  = "Servlet1";
        DisplayNameType[] names = new DisplayNameType[1];
        DisplayNameType nameType = new DisplayNameType();
        nameType.set_value(displayName);
        names[0] = nameType;
        
        WebAppType.Choice1 dispName = new WebAppType.Choice1();
        WebAppType.Choice1.DescriptionGroup displayNameGroup = new WebAppType.Choice1.DescriptionGroup();
        displayNameGroup.setDisplayName(names);
        dispName.setDescriptionGroupGroup(displayNameGroup);
        choiceGroup[0] = dispName;
        
        System.arraycopy(generatedServlets.toArray(), 0, choiceGroup, 1, generatedServlets.size());
        System.arraycopy(generatedServletMappings.toArray(), 0, choiceGroup, generatedServlets.size()+1, generatedServletMappings.size());
        
        WebAppType.Choice1 sessionConfigChoice = new WebAppType.Choice1();
        SessionConfigType sessionConfig = new SessionConfigType();
        XsdIntegerType sessionTimeout = new XsdIntegerType();
        sessionTimeout.set_value(new BigInteger("54"));
        sessionConfig.setSessionTimeout(sessionTimeout);
        sessionConfigChoice.setSessionConfig(sessionConfig);
        choiceGroup[choiceGroup.length-1] = sessionConfigChoice;
        return choiceGroup;
    }
    
    /**
     * Creates new servlet and servlet-mapping elements from annotation data wich does not exist in web.xml
     * @param lstAnnotations
     * @param servletClasses
     */
    private void createChoiceElements(ArrayList lstAnnotations, HashSet<String> servletClasses) {
        for (int i = 0; i < lstAnnotations.size(); i ++ )
        {
            AnnotationRecordImpl annRecord      = (AnnotationRecordImpl)lstAnnotations.get(i);
            ClassInfoImpl elementInfo           = (ClassInfoImpl)annRecord.getOwner();
            
            if(!elementInfo.isInterface()) {
                String serviceName  = AnnotationTools.getAnnotationAttribute(annRecord, "serviceName");
                String annClassName = AnnotationTools.getAnnotationClassName(annRecord);
                
                if (!servletClasses.contains(annClassName)) {
                    createChoiceElement(annClassName, serviceName);
                }
            }
        }         
    }
    
    /**
     * Creates servlet and servlet-mapping 
     * @param fullyQualifiedClassName
     * @param serviceName
     */
    private void createChoiceElement(String fullyQualifiedClassName, String serviceName) {
          ServletType servletType       = new ServletType();
          ServletNameType servletName   = new ServletNameType();
          servletName.set_value(fullyQualifiedClassName);
          servletType.setServletName(servletName);
          
          FullyQualifiedClassType fullyQualifiedClassType = new FullyQualifiedClassType();
          fullyQualifiedClassType.set_value(fullyQualifiedClassName);
          ServletType.Choice1 servletTypeChoice1 = new ServletType.Choice1();
          servletTypeChoice1.setServletClass(fullyQualifiedClassType);
          servletType.setChoiceGroup1(servletTypeChoice1);
          servletType.setLoadOnStartup(new Integer(0));
          
          ServletMappingType mappingType = new ServletMappingType();
          ServletNameType sNameType = new ServletNameType();
          sNameType.set_value(fullyQualifiedClassName);
          mappingType.setServletName(sNameType);
          
          String url_pattern = "";
          if(!serviceName.equals("")) {
              url_pattern = serviceName;
          }
          else {
              url_pattern = this.getShortClassName(fullyQualifiedClassName) + "Service";
          }
          url_pattern = "/" + url_pattern;
          
          UrlPatternType urlPatternType = new UrlPatternType();
          urlPatternType.set_value(url_pattern);
          UrlPatternType[] urlPatternTypeArr = new UrlPatternType[] { urlPatternType };
          mappingType.setUrlPattern(urlPatternTypeArr);
          
          Choice1 addedElement = new Choice1();
          addedElement.setServlet(servletType);
          
          Choice1 addedElement1 = new Choice1();
          addedElement1.setServletMapping(mappingType);
          
          this.generatedServlets.add(addedElement);
          this.generatedServletMappings.add(addedElement1);
    }
    
    /**
     * Process WebService and WebServiceProvider annotations
     * Creates new web.xml or add servlet elements 
     * 
     * @param tempDir
     * @param moduleRelativeFileUri
     * @param annotationMap
     * @throws Exception
     */
    public void generateModuleWebSupport(String tempDir, JarFile moduleRelativeFileUri, Map annotationMap) throws Exception {

        if(annotationMap == null) {
            return;
        }
        
        String webXmlFile = "web.xml";
        boolean buildJar = false;
        WebAppType webServiceXmlType   = new WebAppType();
        HashSet<String> webXmlServlets = new HashSet<String>();
        
        try {
              com.sap.engine.lib.processor.impl.WebProcessor5 webProcessor5 = com.sap.engine.lib.processor.impl.WebProcessor5.getInstance();              
              
              ZipEntry webXmlEntry = moduleRelativeFileUri.getEntry(WEB_INF + "/" + webXmlFile);
              Choice1[] webAppChoiceGroup   = new Choice1[0];
              Choice1[] privateChoiceGroup  = null;
              ArrayList lstAnnotations = getAnnotations(annotationMap);
              
              InputStream webXmlInputStream = null;
              if (webXmlEntry != null) {
                  webXmlInputStream  = moduleRelativeFileUri.getInputStream(webXmlEntry);
                  webServiceXmlType  = (WebAppType)webProcessor5.parse(webXmlInputStream);
                  privateChoiceGroup = webServiceXmlType.getChoiceGroup1();
              }
              
              if (privateChoiceGroup != null && privateChoiceGroup.length > 0) {
                  try{
                      webXmlServlets = getWebXmlServlets(privateChoiceGroup);
                      createChoiceElements(lstAnnotations, webXmlServlets);

                      if (generatedServlets.size() > 0)
                      {
                          int size = generatedServlets.size() + generatedServletMappings.size();
                          buildJar = true;
                          webAppChoiceGroup = new Choice1[privateChoiceGroup.length + size];
                          
                          int firstServletIndex = -1;
                          int firstMappingIndex = -1;
                          
                          for (int i=0; i < privateChoiceGroup.length; i ++) {
                              Choice1 choice = privateChoiceGroup[i];
                              
                              if(checkFirstServlet(choice) ) {
                                  firstServletIndex = i;
                                  break;
                              }
                          }
                          
                          for (int i=0; i < privateChoiceGroup.length; i ++) {
                              Choice1 choice = privateChoiceGroup[i];
                             
                              if(checkFirstServletMapping(choice)) {
                                  firstMappingIndex = i;
                                  break;
                              }
                          }
                          
                          if(firstServletIndex == -1) { //add servlets and servlet mappings
                              System.arraycopy(privateChoiceGroup, 0, webAppChoiceGroup, 0, privateChoiceGroup.length);
                              System.arraycopy(generatedServlets.toArray(), 0, webAppChoiceGroup, privateChoiceGroup.length, generatedServlets.size());
                              System.arraycopy(generatedServletMappings.toArray(), 0, webAppChoiceGroup, privateChoiceGroup.length+generatedServlets.size(), generatedServletMappings.size());
                          }
                          else {    // insert servlets and servlet mappings
                              int destMappingPos = firstMappingIndex+generatedServlets.size();
                              System.arraycopy(privateChoiceGroup, 0, webAppChoiceGroup, 0, firstServletIndex);
                              System.arraycopy(generatedServlets.toArray(), 0, webAppChoiceGroup, firstServletIndex, generatedServlets.size());
                              System.arraycopy(privateChoiceGroup, firstServletIndex, webAppChoiceGroup, firstServletIndex+generatedServlets.size(), firstMappingIndex-firstServletIndex);
                              System.arraycopy(generatedServletMappings.toArray(), 0, webAppChoiceGroup, destMappingPos, generatedServletMappings.size());
                              System.arraycopy(privateChoiceGroup, firstMappingIndex, webAppChoiceGroup, destMappingPos+generatedServletMappings.size(), privateChoiceGroup.length-firstMappingIndex);
                          }
                      }
                  }
                  finally {
                      if(webXmlInputStream != null ){ 
                          webXmlInputStream.close();
                      }
                  }
              }
              else {
                  createChoiceElements(lstAnnotations, webXmlServlets);
                  
                  if(generatedServlets.size() > 0) {
                      webAppChoiceGroup = generateNewChoice1Group();
                      buildJar = true;
                  }
              }
              
              if (buildJar) {
                  webServiceXmlType.setChoiceGroup1(webAppChoiceGroup);
                  int index = moduleRelativeFileUri.getName().lastIndexOf(File.separator);
                  String fileName = moduleRelativeFileUri.getName().substring(index + 1);
                  String filePath = moduleRelativeFileUri.getName().substring(0, index);

                  webServiceXmlType.setVersion(WebAppVersionType.fromString(WebAppVersionType._Enum25));
                  webProcessor5.build(webServiceXmlType, filePath + File.separator + webXmlFile);
                  BuildJarFile.buildJar(webXmlFile, filePath, fileName);
              }
          } catch (Exception rtge) {
            throw rtge;
          }
    }
    
    private boolean checkFirstServlet(Choice1 choice) {
        if(choice.isSetServlet() ||
           checkFirstServletMapping(choice) ) {
           
            return true;
        }
        
        return false;
    }
    
    private boolean checkFirstServletMapping(Choice1 choice) {
        if(     choice.isSetServletMapping() ||
                choice.isSetSessionConfig()  ||
                choice.isSetMimeMapping()    ||
                choice.isSetWelcomeFileList() ||
                choice.isSetErrorPage() ||
                choice.isSetJspConfig() ||
                choice.isSetSecurityConstraint() ||
                choice.isSetLoginConfig() ||
                choice.isSetSecurityRole() ||
                choice.isSetJndiEnvironmentRefsGroupGroup() ||
                choice.isSetMessageDestination() ||
                choice.isSetLocaleEncodingMappingList() ) {
                 return true;
             }
             
             return false;
    }
    
    /**
     * Returns WebService and WebServiceProvider annotations
     * @param annotationMap
     * @return
     */
    ArrayList getAnnotations(Map annotationMap) {
        ArrayList lstWebService   = (ArrayList)annotationMap.get(WEB_SERVICE_ANNOTATION);
        ArrayList lstWebServiceProvider = (ArrayList)annotationMap.get(WEB_SERVICE_PROVIDER_ANNOTATION);
        
        ArrayList lstAnnotations = new ArrayList();
        
        if(lstWebService != null) {
            lstAnnotations.addAll(lstWebService);
        }
        
        if(lstWebServiceProvider != null) {
            lstAnnotations.addAll(lstWebServiceProvider);
        }
        
        return lstAnnotations;
    }
    
    private String getShortClassName(String fullClassName) {
        if(fullClassName.lastIndexOf(".") > -1) {
            return fullClassName.substring(fullClassName.lastIndexOf(".") + 1); 
        }
        return fullClassName;    
    }
}
