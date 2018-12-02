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

package com.sap.engine.services.webservices.server.deploy.ws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.InconsistentReadException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.InterfaceDefinitionRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.InterfaceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.ws.ApplicationServiceTypeMappingContext;
import com.sap.engine.services.webservices.server.container.ws.JAXBContextRegistry;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.ServiceTypeMappingRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebservicesExtType;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSStartNotifictionHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

/**
 * Title: WSStartProcess
 * Description: WSStartProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSStartProcess extends WSAbstractDProcess {     
  
  private ClassLoader appLoader; 
  private ArrayList<String> warnings; 
  
  public WSStartProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, ClassLoader appLoader, Configuration appConfiguration, ServiceContext serviceContext, RuntimeProcessingEnvironment runtimeProcessingEnvironment) {    
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;  
    this.appLoader = appLoader; 
    this.appConfiguration = appConfiguration;   
    this.serviceContext = serviceContext;
    this.wsDNotificationHandler = new WSStartNotifictionHandler(applicationName, serviceContext, runtimeProcessingEnvironment);
  }
  
  public void additionalInit(ClassLoader appLoader, Configuration appConfiguration) {
    this.appLoader = appLoader;  
    this.appConfiguration = appConfiguration;
  }
  
  public ArrayList<String> getWarnings() {
    if(this.warnings == null) {
      this.warnings = new ArrayList<String>(); 	
    }
    
    return this.warnings; 
  }
  
  public void preProcess() throws WSDeploymentException, WSWarningException {        
    download();         
  }
    
  public void init() throws WSDeploymentException {               
    loadWebServicesJ2EEEngineDescriptorsInitially();            
  }
         
  public void execute() throws WSDeploymentException {              
    loadWebServicesJ2EEEngineDescriptors();                       
    loadJAXBContexts(); 
  }

  public void finish() throws WSDeploymentException {        
    try { 
	 wsDNotificationHandler.onExecutePhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), getWarnings()); 	
    } 
  }
  
  public void postProcess() throws WSDeploymentException, WSWarningException {
      
  }
  
  public void notifyProcess() throws WSWarningException {    
	  
  }
  
  public void commitProcess() throws WSWarningException {    
    wsDNotificationHandler.onCommitPhase();      
  }
  
  public void rollbackProcess() throws WSWarningException {    
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {    
      wsDNotificationHandler.onRollbackPhase();  
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
    
    try {
      unregister();
    } catch(Exception e) {
      //TODO 
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));      
      warnings.add(strWriter.toString());  
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }
  }
     
  protected void loadWebServicesJ2EEEngineDescriptors(String applicationName, WebservicesType webServicesJ2EEEngineDescriptor, WebservicesExtType webServicesJ2EEEngineExtDescriptor, ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {                                  
    super.loadWebServicesJ2EEEngineDescriptors(applicationName, webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineExtDescriptor, moduleRuntimeData);
    loadServiceTypeMapping(webServicesJ2EEEngineDescriptor.getWebserviceDescription(), moduleRuntimeData);    
  }
  
  private void loadServiceTypeMapping(WebserviceDescriptionType[] serviceDescriptors, ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {
    if(serviceDescriptors == null) {
      return;
    }
    
    WebserviceDescriptionType serviceDescriptor; 
    for(int i = 0; i < serviceDescriptors.length; i++) {
      serviceDescriptor = serviceDescriptors[i];      
      loadServiceTypeMapping(serviceDescriptor.getWebserviceName().trim(), moduleRuntimeData.getModuleDir(), serviceDescriptor.getTypeMappingFile());      
    }    
  }
  
  private void loadServiceTypeMapping(String serviceName, String sourceDir, TypeMappingFileType[] typeMappingFileDescriptors) throws WSDeploymentException {             
    ApplicationServiceTypeMappingContext applicationServiceTypeMappingContext = (ApplicationServiceTypeMappingContext)getServiceContext().getApplicationServiceTypeMappingContexts().get(applicationName); 
    if(applicationServiceTypeMappingContext == null) {
      applicationServiceTypeMappingContext = new ApplicationServiceTypeMappingContext();
      getServiceContext().getApplicationServiceTypeMappingContexts().put(applicationName, applicationServiceTypeMappingContext);
    }
            
    if(typeMappingFileDescriptors == null || typeMappingFileDescriptors.length == 0) {
      return; 
    }
    
    TypeMappingFileType typeMappingFileDescriptor;
    String type;
    String style;           
    TypeMappingRegistryImpl typeMappingRegistry;
    String typeMappingFileRelPath;
    InputStream typeMappingIn = null; 
    for(int i = 0; i < typeMappingFileDescriptors.length; i++) {
      try {      
        typeMappingFileDescriptor = typeMappingFileDescriptors[i];                         
        type = typeMappingFileDescriptor.getType().getValue().trim();      
        style = typeMappingFileDescriptor.getStyle().getValue().trim();
        if(type.equals(SchemaTypeType._config)) {
          continue; 
        } 
                
        ServiceTypeMappingRegistry serviceTypeMappingRegistry = applicationServiceTypeMappingContext.getServiceTypeMappingRegistry(style);
        if(serviceTypeMappingRegistry == null) {
          serviceTypeMappingRegistry = new ServiceTypeMappingRegistry(); 
          applicationServiceTypeMappingContext.putServiceTypeMappingRegistry(style, serviceTypeMappingRegistry);                   
        }         
                  
        typeMappingRegistry = new TypeMappingRegistryImpl();                                     
        typeMappingFileRelPath = typeMappingFileDescriptor.get_value().trim();           
        if(typeMappingFileRelPath.indexOf("#") != -1) {                        
          typeMappingIn = appLoader.getResourceAsStream(typeMappingFileRelPath.substring(typeMappingFileRelPath.lastIndexOf("#") + 1));          
          typeMappingRegistry.fromXML(typeMappingIn, appLoader); 
        } else {                      
          typeMappingRegistry.fromXML(sourceDir + "/" + typeMappingFileRelPath, appLoader);                      
        }                    
        serviceTypeMappingRegistry.getTypeMappingRegistries().put(serviceName, typeMappingRegistry);                                 
      } catch(Exception e) {
        Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD_FRM, new Object[]{applicationName, serviceName}, e);
      } finally {
        try {                
          if(typeMappingIn != null) {
            typeMappingIn.close();                 
          }         
        } catch(Exception e) {  
          // $JL-EXC$         
        }
      }       
    }         
  }
    
  private void loadJAXBContexts() throws WSDeploymentException {  
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return;   
    } 
    
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    ServiceExtDescriptorRegistry serviceExtDescriptorRegistry = wsApplicationDescriptorContext.getServiceExtDescriptorRegistry(); 
    
    ApplicationServiceTypeMappingContext applicationServiceTypeMappingContext = (ApplicationServiceTypeMappingContext)getServiceContext().getApplicationServiceTypeMappingContexts().get(applicationName);
    if(applicationServiceTypeMappingContext == null) {
      applicationServiceTypeMappingContext = new ApplicationServiceTypeMappingContext(); 
      getServiceContext().getApplicationServiceTypeMappingContexts().put(applicationName, applicationConfigurationContext);
    }    
    JAXBContextRegistry jaxbContextRegistry = applicationServiceTypeMappingContext.getJaxbContextRegistry(); 
   
    Enumeration enumer = serviceRegistry.getServices().elements();
    Service service;
    String serviceName;
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor;   
    JAXBContext jaxbContext; 
    try {
      while(enumer.hasMoreElements()) {      
        service = (Service)enumer.nextElement();
        serviceName = service.getName().trim(); 
        serviceExtDescriptor = serviceExtDescriptorRegistry.getServiceExtDescriptor(serviceName);
        if((service.getType() != null && service.getType() == 3) || (serviceExtDescriptor != null && serviceExtDescriptor.getType() != null && serviceExtDescriptor.getType() == 3)) {                 

          InterfaceDefinitionRegistry interfaceDefinitionRegistry = getServiceContext().getConfigurationContext().getInterfaceDefinitionRegistry();
          InterfaceMappingRegistry interfaceMappingRegistry = getServiceContext().getMappingContext().getInterfaceMappingRegistry();
          
          String[] paramClassNames = new String[0]; 
          InterfaceDefinition interfaceDefinition; 
          InterfaceMapping interfaceMapping; 
          BindingData[] bindingDatas = service.getServiceData().getBindingData();

          for(BindingData bindingData:bindingDatas) {
            interfaceDefinition = interfaceDefinitionRegistry.getInterfaceDefinition(bindingData.getInterfaceId().trim());
            String intfMappID = interfaceDefinition.getInterfaceMappingId().trim();
            String key = JAXBContextRegistry.generateKey(serviceName, intfMappID);
            if (jaxbContextRegistry.getJAXBContext(key) == null) { //several binding datas could point to one and same interfacemapping (possible wieh BD are added from UI)
              interfaceMapping = interfaceMappingRegistry.getInterfaceMapping(intfMappID);
              jaxbContext = createJAXBRIContext(interfaceMapping, appLoader);
//              paramClassNames = InterfaceMappingClassExtractor.extractUsedClassNames(interfaceMapping);
//              try {
//                jaxbContext = JAXBContext.newInstance(loadClasses(paramClassNames, appLoader));
//              } catch (JAXBException origE) {
//                //needed .toString() of the exception to be called, since it returns detailed info.
//                JAXBException newE = new JAXBException(origE.toString(), origE);
//                throw newE;
//              } 
              jaxbContextRegistry.putJAXBContext(key, jaxbContext);
            }
          }
        }
      }     
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD_FRM, new Object[]{applicationName, "not available"}, e);
    }        
  }
  
//  private String[] getParamClassNames(Service service) {
//    BindingData[] bindingDatas = service.getServiceData().getBindingData();
//    if(bindingDatas == null || bindingDatas.length == 0) {
//      return new String[0]; 
//    }
//        
//    InterfaceDefinitionRegistry interfaceDefinitionRegistry = getServiceContext().getConfigurationContext().getInterfaceDefinitionRegistry();
//    InterfaceMappingRegistry interfaceMappingRegistry = getServiceContext().getMappingContext().getInterfaceMappingRegistry();
//    
//    String[] paramClassNames = new String[0]; 
//    InterfaceDefinition interfaceDefinition; 
//    InterfaceMapping interfaceMapping; 
//    for(BindingData bindingData:bindingDatas) {
//      interfaceDefinition = interfaceDefinitionRegistry.getInterfaceDefinition(bindingData.getInterfaceId().trim());
//      interfaceMapping = interfaceMappingRegistry.getInterfaceMapping(interfaceDefinition.getInterfaceMappingId().trim());
////      paramClassNames = WSUtil.unifyStrings(new String[][]{paramClassNames, getParamClassNames(interfaceMapping)});
//      paramClassNames = getParamClassNames(interfaceMapping);
//      break; //the InterfaceMapping should be one and same for all BindingDatas, so no need to iterate again and add again teh same classes into the array....
//    }
//    
//    return paramClassNames; 
//  }
  
  private JAXBRIContext createJAXBRIContext(InterfaceMapping iMap, ClassLoader loader) throws Exception {
    // temporary commented for galaxy testing
    List<TypeReference> list = initTypeReferences(iMap, loader);
    Class[] cls = new Class[list.size()];
    for (int i = 0; i < list.size(); i++) {
      cls[i] = (Class) list.get(i).type;
    }
    
    QName defaultNS = iMap.getPortType(); // CSN 4161046 2008
    JAXBRIContext context = JAXBRIContext.newInstance(cls, list, defaultNS == null ? null : defaultNS.getNamespaceURI(), false);
    
    return context;
    
    //return (JAXBRIContext)JAXBContext.newInstance(new Class[] {String.class});
 }
  
  private Class heuristicJaxbBeanLoading(String beanClass, ClassLoader cl) throws Exception {
    try {
      Class c = cl.loadClass(beanClass);
      return c;
    } catch (ClassNotFoundException cnfE) {
      if (beanClass.endsWith("Special")) {
        beanClass = beanClass.substring(0, beanClass.lastIndexOf("Special"));
        Class c = cl.loadClass(beanClass);
        return c;
      } else {
        throw cnfE;
      }
    }
  }
  

  private List<TypeReference> initTypeReferences(InterfaceMapping iMap, ClassLoader loader) throws Exception {
    List<TypeReference> refs = new ArrayList<TypeReference>();
    OperationMapping ops[] = iMap.getOperation();
    
    if (iMap.isJAXWSProviderInterface()){
      return refs;
    }
    
    String seiName = iMap.getSEIName();
    Class seiClass = null;
    if (seiName != null){
      seiClass = loader.loadClass(seiName);
    }
    
    
    for (OperationMapping op : ops) {
      String opStyle = op.getProperty(OperationMapping.OPERATION_STYLE);
      Method m = null;
      if (seiClass != null) {
        m = findJavaMethod(seiClass, loader, op);
      }
      //load Request/ResponseWrapper bean
      if (OperationMapping.DOCUMENT_OPERATION_STYLE.equals(opStyle)) {
        String reqWBean = op.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN);
        String respWBean = op.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN); 
        if (reqWBean != null) {
          Class c = heuristicJaxbBeanLoading(reqWBean, loader);
          op.setProperty(OperationMapping.REQUEST_WRAPPER_BEAN, c.getName()); //override the original value in order the correct one to be used at runtime
          QName q = new QName(op.getProperty(OperationMapping.INPUT_NAMESPACE), op.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER));
          TypeReference t = new TypeReference(q, c, c.getAnnotations());
          refs.add(t);
        }
        if (respWBean != null) {
          Class c = heuristicJaxbBeanLoading(respWBean, loader);
          op.setProperty(OperationMapping.RESPONSE_WRAPPER_BEAN, c.getName()); //override the original value in order the correct one to be used at runtime
          QName q = new QName(op.getProperty(OperationMapping.OUTPUT_NAMESPACE), op.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER));
          TypeReference t = new TypeReference(q, c, c.getAnnotations());
          refs.add(t);
        }
      } else if (OperationMapping.DOCUMENT_BARE_OPERATION_STYLE.equals(opStyle)) { // i044259
        //obtain in 
        ParameterMapping inParams[] = op.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.OUT_TYPE | ParameterMapping.IN_OUT_TYPE);
        Annotation[][] anns = m != null ? m.getParameterAnnotations() : new Annotation[inParams.length][0];
        if (inParams.length != anns.length) {
          throw new Exception("Mapping input parameter number " + inParams.length + " does not match annotations array number " + anns.length + " for method " + m);
        }
        int b = 0;
        for (ParameterMapping p: inParams) {
          QName elemQName = p.getSchemaQName();
          Class cParam = loadClass0(p.getJavaType(), loader);
          QName q = new QName(elemQName.getNamespaceURI(), elemQName.getLocalPart()); //why make copy
          TypeReference tr = new TypeReference(q, cParam, anns[b]);
          p.setJaxbTypeRef(tr);
          refs.add(tr);
          b++;
        }
        //obtain return
        inParams = op.getParameters(ParameterMapping.RETURN_TYPE);
        if (inParams.length == 1) {
          Annotation[] mAn = m != null ? m.getAnnotations(): new Annotation[0];
          ParameterMapping p = inParams[0];
          QName elemQName = p.getSchemaQName();
          Class cParam = loadClass0(p.getJavaType(), loader);
          QName q = new QName(elemQName.getNamespaceURI(), elemQName.getLocalPart()); //?
          TypeReference tr = new TypeReference(q, cParam, mAn);
          p.setJaxbTypeRef(tr);
          refs.add(tr);
        }
        
      } else { //this should be rpc
        //process input params
        ParameterMapping inParams[] = op.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.OUT_TYPE | ParameterMapping.IN_OUT_TYPE);
        Annotation[][] anns = m != null ? m.getParameterAnnotations() : new Annotation[inParams.length][0];
        if (inParams.length != anns.length) {
          throw new Exception("Mapping input parameter number " + inParams.length + " does not match annotations array number " + anns.length + " for method " + m);
        }
        for (int i = 0; i < anns.length; i++) {
          Annotation[] an = anns[i];
          ParameterMapping p = inParams[i];
          Class cParam = loadClass0(p.getJavaType(), loader);
          QName q = new QName(null, p.getWSDLParameterName());
          TypeReference tr = new TypeReference(q, cParam, an);
          p.setJaxbTypeRef(tr);
          refs.add(tr);
        }
        //process return
        ParameterMapping retParam[] = op.getParameters(ParameterMapping.RETURN_TYPE);
        if (retParam.length == 1) {
          Annotation[] mAn = m != null ? m.getAnnotations() : new Annotation[0];
          ParameterMapping retP = retParam[0];
          Class retClass = loadClass0(retP.getJavaType(), loader);
          QName q = new QName(null, retP.getWSDLParameterName());
          TypeReference tr = new TypeReference(q, retClass, mAn);
          retP.setJaxbTypeRef(tr);
          refs.add(tr);
        }
      }
      ParameterMapping[] faults = op.getParameters(ParameterMapping.FAULT_TYPE);
      refs.addAll(initFaults(faults, loader));
    }
    
    return refs;
  }
  
  private List<TypeReference> initFaults(ParameterMapping[] fs, ClassLoader loader) throws Exception {
    List<TypeReference> res = new ArrayList<TypeReference>();
    for (ParameterMapping f : fs) {
      String fClassN = f.getProperty(ParameterMapping.JAXB_BEAN_CLASS);    
      if (fClassN != null) { //this is necessary for faults. They have JAXB bean class and javaType as well. But javaType must not by passed to the JAXBContext. 
        Class fClass = loadClass0(fClassN, loader);
        res.add(new TypeReference(f.getFaultElementQName(), fClass, new Annotation[0]));
      }
    }
    return res;
  }
  
  private Method findJavaMethod(Class sei, ClassLoader loader, OperationMapping op) throws Exception {
    Class[] cls = RuntimeProcessingEnvironment.loadParameterClasses(op, loader);
    String mName = op.getJavaMethodName();
    return sei.getMethod(mName, cls);
  }
  
  
  private Class[] loadClasses(String[] classNames, ClassLoader loader) throws ClassNotFoundException {
    if(classNames == null || classNames.length == 0) {
      return new Class[0];  
    }      
    
    Class[] classes = new Class[classNames.length];
    int i = 0;
    for(String className: classNames) {
      classes[i++] = loadClass0(className, loader);   
    }
    
    return classes;
  }
  
  private Class loadClass0(String clDecl, ClassLoader loader)  throws ClassNotFoundException {
    int ind = clDecl.indexOf(ParameterMapping.JAVA_ARRAY_DIMENSION); //as it is described in the VI.

    if (ind == -1) { //this is not array
      //for simple types
      if (clDecl.equals("byte")) {
        return byte.class; 
      } else if (clDecl.equals("char")) {
        return char.class;
      } else if (clDecl.equals("boolean")) {
        return boolean.class;
      } else if (clDecl.equals("short")) {
        return short.class;
      } else if (clDecl.equals("int")) {
        return int.class;
      } else if (clDecl.equals("float")) {
        return float.class;
      } else if (clDecl.equals("long")) {
        return long.class;
      } else if (clDecl.equals("double")) {
        return double.class;
      } else if (clDecl.equals("void")) {
        return Void.TYPE;
      }

      //this is not a simple type use the loader
      return loader.loadClass(clDecl);
    }

    //this is an array
    int[] arrDim = new int[(clDecl.length() - ind) / 2];
    Class compClass = loadClass0(clDecl.substring(0, ind), loader);
    return java.lang.reflect.Array.newInstance(compClass, arrDim).getClass();
  }
  
  public void download() throws WSDeploymentException {
    try {           
      if(!appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {        
        return;
      }                
      
      Configuration webServiceContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME);
      
      Configuration metaDataConfiguration = null; 
      try { 
        metaDataConfiguration = webServiceContainerConfiguration.getSubConfiguration(WSApplicationMetaDataContext.METADATA); 
      } catch(NameNotFoundException e) {
        // $JL-EXC$         
      }      
      if(metaDataConfiguration == null || !((String)metaDataConfiguration.getConfigEntry(WSApplicationMetaDataContext.VERSION)).equals(WSApplicationMetaDataContext.VERSION_71)) {
        return;   
      }
      
      HashSet<String> skippedChildDirs = new HashSet<String>();
      skippedChildDirs.add(WSApplicationMetaDataContext.METADATA);  
      skippedChildDirs.add(WSApplicationMetaDataContext.BACKUP);
      
      try {
        Configuration backupConfiguration = webServiceContainerConfiguration.getSubConfiguration(WSApplicationMetaDataContext.BACKUP);        
        if(!downloadDirectory(new File(webServicesContainerDir), backupConfiguration, true) && checkModuleDirs(webServicesContainerDir, webServiceContainerConfiguration, skippedChildDirs)) {          
          return; 
        }         
      } catch(NameNotFoundException e) {
        // $JL-EXC$         
      }           
      downloadDirectory(new File(webServicesContainerDir), webServiceContainerConfiguration, skippedChildDirs);      
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_DOWNLOAD, new Object[]{applicationName}, e);      
    }     
  }  
  
  private static boolean checkModuleDirs(String webServicesContainerDir, Configuration webServicesConfiguration, Set skippedChildDirs) throws ConfigurationException, IOException {
    Map subConfigurations = webServicesConfiguration.getAllSubConfigurations();   
    
    Iterator iter = subConfigurations.keySet().iterator();
    Configuration subConfiguration; 
    String name; 
    while(iter.hasNext()) {
      subConfiguration = (Configuration)subConfigurations.get(iter.next());
      name = subConfiguration.getMetaData().getName(); 
      if(!skippedChildDirs.contains(name)) {
        if(!checkModuleDir(webServicesContainerDir + "/" + name, subConfiguration)) {
          return false;   
        }   
      }
    }
    
    return true; 
  }
  
  private static boolean checkModuleDir(String moduleDir, Configuration moduleConfiguration) throws InconsistentReadException, ConfigurationException, IOException {
    return checkWSModuleDir(moduleDir, moduleConfiguration) && checkWSClientsModuleDir(moduleDir, moduleConfiguration);         
  }
  
  private static boolean checkWSModuleDir(String moduleDir, Configuration moduleConfiguration) throws InconsistentReadException, ConfigurationException, IOException {
    Configuration typesConfiguration = null; 
    try {
      typesConfiguration = moduleConfiguration.getSubConfiguration("types"); 
    } catch(NameNotFoundException e) {
      // $JL-EXC$         
    } 
  
    if(typesConfiguration == null) {
      return true;  
    }  
         
    return !downloadDirectory(new File(moduleDir, "types"), typesConfiguration); 
  }
  
  private static boolean checkWSClientsModuleDir(String moduleDir, Configuration moduleConfiguration) throws InconsistentReadException, ConfigurationException, IOException {
  Configuration jarsConfiguration = null; 
    try {
      jarsConfiguration = moduleConfiguration.getSubConfiguration("jars"); 
    } catch(NameNotFoundException e) {
      // $JL-EXC$         
    } 
    
    if(jarsConfiguration == null) {
      return true;        
    }  
    
    return !downloadDirectory(new File(moduleDir, "jars"), jarsConfiguration);          
  }
     
  public static void download630(String applicationName, String webServicesContainerDir, Configuration appConfiguration) throws WSDeploymentException {             
  try {
    Configuration webServicesContainerConfiguration = null;   
    try {
      webServicesContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME); 
      } catch(NameNotFoundException e) {
        // $JL-EXC$ 
    }   
    if(webServicesContainerConfiguration == null) {
        return; 
    }
    
    Configuration webServicesConfiguration = null; 
      try { 
        webServicesConfiguration = webServicesContainerConfiguration.getSubConfiguration("webservices"); 
      } catch(NameNotFoundException e) {
        // $JL-EXC$         
      }      
      Configuration wsClientsConfiguration = null; 
      try { 
        wsClientsConfiguration = webServicesContainerConfiguration.getSubConfiguration("wsClients"); 
      } catch(NameNotFoundException e) {
        // $JL-EXC$         
      }      
      if(webServicesConfiguration == null && wsClientsConfiguration == null) {
        return;   
      }   
    
    try {
      Configuration backupConfiguration = webServicesContainerConfiguration.getSubConfiguration(WSApplicationMetaDataContext.BACKUP);        
        if(!downloadDirectory(new File(webServicesContainerDir), backupConfiguration, true) && checkWSClientsDir(webServicesContainerDir, webServicesContainerConfiguration)) {         
          return; 
        }    
    } catch(NameNotFoundException e) {
      // $JL-EXC$         
    }  
     
      HashSet<String> skippedChildDirs = new HashSet<String>();    
      skippedChildDirs.add(WSApplicationMetaDataContext.BACKUP);     
      downloadDirectory(new File(webServicesContainerDir), webServicesContainerConfiguration, skippedChildDirs);
  } catch(Exception e) {
    Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
    throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_DOWNLOAD, new Object[]{applicationName}, e);      
  }   
  }
  
  private static boolean checkWSClientsDir(String webServicesContainerDir, Configuration webServicesContainerConfiguration) throws InconsistentReadException, ConfigurationException, IOException {
    Configuration wsClientsConfiguration = null; 
    try {
      wsClientsConfiguration = webServicesContainerConfiguration.getSubConfiguration("wsClients");     
    } catch(NameNotFoundException e) {
      // $JL-EXC$         
    }    
    if(wsClientsConfiguration == null) {
      return true; 
    }
  
    HashSet<String> skippedChildDirs = new HashSet<String>();
    skippedChildDirs.add("app_jars");
    return checkModuleDirs(webServicesContainerDir + "/wsClients", wsClientsConfiguration, skippedChildDirs);      
  }
  
}
