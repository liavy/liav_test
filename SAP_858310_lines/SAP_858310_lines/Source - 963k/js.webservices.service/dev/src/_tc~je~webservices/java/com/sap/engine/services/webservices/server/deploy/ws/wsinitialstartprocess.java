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
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.services.deploy.container.op.start.ApplicationStartInfo;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLSerializer;
import com.sap.engine.services.webservices.espbase.wsdl.misc.OutsideInWSDLUtils;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.InterfaceMappingProcessor;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxws.j2w.Java2WsdlOptions;
import com.sap.engine.services.webservices.jaxws.j2w.Java2WsdlResult;
import com.sap.engine.services.webservices.jaxws.j2w.JaxWsIMappingGenerator;
import com.sap.engine.services.webservices.jaxws.j2w.JaxWsIMappingGeneratorForWSProvider;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.MappingDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesJ2EEEngineDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.BindingDataType;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSInitialDeployProcess
 * Description: WSInitialDeployProcess 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
 
public class WSInitialStartProcess extends WSAbstractDProcess {
	
  private ClassLoader appLoader;
  private String classPath;        
  private JarUtil jarUtil;
  private JarUtils jarUtils;
   
  private ApplicationStartInfo applicationStartInfo;
  
  public WSInitialStartProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration, ClassLoader appLoader, String classPath) {    
    this.applicationName = applicationName; 
	this.webServicesContainerDir = webServicesContainerDir; 
	this.webServicesContainerTempDir = webServicesContainerTempDir;    
	this.appConfiguration = appConfiguration;  
	this.serviceContext = new ServiceContext();	
	this.appLoader = appLoader;
	this.classPath = classPath;
	this.jarUtil = new JarUtil();
	this.jarUtils = new JarUtils();	      		
  }
  
  /**
   * @return ApplicationStartInfo 
   */
  public ApplicationStartInfo getApplicationStartInfo() {    
    return applicationStartInfo;        
  }
   
  public void preProcess() throws WSDeploymentException, WSWarningException {
    
  }
  
  public void init() throws WSDeploymentException {
    loadWebServicesJ2EEEngineDescriptorsInitially();	
  }

  public void execute() throws WSDeploymentException {
    loadWebServicesJ2EEEngineDescriptors();  
    generateJAXWSFramework();
  }

  public void finish() throws WSDeploymentException {
	upload(); 
	
    setApplicationStartInfo();	
  }
  
  public void postProcess() throws WSDeploymentException, WSWarningException {
		
  }
  
  public void notifyProcess() throws WSWarningException {
    
  }
  
  public void commitProcess() throws WSWarningException {
	try {
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // $JL-EXC$
      // TODO - add trace    	
    }  	
  }
  
  public void rollbackProcess() throws WSWarningException {
    try {
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // $JL-EXC$
      // TODO - add trace    	
    }  
  }
  
  private void generateJAXWSFramework() throws WSDeploymentException {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    if(wsApplicationDescriptorContext == null) {
      return;  	    	
    }    
    
    ApplicationConfigurationContext wsApplicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);    
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);      
    ServiceRegistry serviceRegistry = wsApplicationConfigurationContext.getServiceRegistry();
    ServiceMetaDataRegistry serviceMetaDataRegistry = wsApplicationMetaDataContext.getServiceMetaDataRegistry();
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();    
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();
    ServiceExtDescriptorRegistry serviceExtDescriptorRegistry = wsApplicationDescriptorContext.getServiceExtDescriptorRegistry(); 
    MappingDescriptorRegistry mappingDescriptorRegistry = wsApplicationDescriptorContext.getMappingDescriptorRegistry();    
         
    Enumeration enumer = serviceDescriptorRegistry.getServiceDescriptors().elements();     
    WebserviceDescriptionType serviceDescriptor; 
    String serviceName;        
    Service service;  
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor; 
    ServiceMetaData serviceMetaData;
    String moduleName;         
    ModuleRuntimeData moduleRuntimeData; 
    MappingRules mappingDescriptor;     
    ClassLoader wsClassLoader = null;
    try {      
      //wsClassLoader = WSContainer.getServiceContext().getCoreContext().getLoadContext().createClassLoader(new ClassLoader[]{appLoader}, getArchiveFilePaths(), "ws_" + applicationName + System.currentTimeMillis()); //createWSClassLoader("ws_" + applicationName + System.currentTimeMillis());        
      while(enumer.hasMoreElements()) {    	
        serviceDescriptor = (WebserviceDescriptionType)enumer.nextElement();
        serviceName = serviceDescriptor.getWebserviceName().trim();                 
        service = serviceRegistry.getService(serviceName); 
        serviceExtDescriptor = serviceExtDescriptorRegistry.getServiceExtDescriptor(serviceName);
        if((service.getType() != null && service.getType() == 3) || (serviceExtDescriptor != null && serviceExtDescriptor.getType() != null && serviceExtDescriptor.getType() == 3)) {
          if(wsClassLoader == null) {
            wsClassLoader = WSContainer.getServiceContext().getCoreContext().getLoadContext().createClassLoader(new ClassLoader[]{appLoader}, getArchiveFilePaths(), "ws_" + applicationName + System.currentTimeMillis()); //createWSClassLoader("ws_" + applicationName + System.currentTimeMillis());  	  
          }
          serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceName);
          moduleName = serviceMetaData.getModuleName(); 
          moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);
          mappingDescriptor = mappingDescriptorRegistry.getMappingDescriptor(moduleName);
          if(mappingDescriptor == null) {            
        	mappingDescriptor = new MappingRules();
        	mappingDescriptorRegistry.putMappingDescriptor(moduleName, mappingDescriptor);                        
          }          
          generateJAXWSFramework(serviceDescriptor, service, serviceExtDescriptor, serviceMetaData, moduleRuntimeData, mappingDescriptor, new JaxWsIMappingGenerator(), new InterfaceMappingProcessor(), new WSDLSerializer(), wsClassLoader);
        }    
      }        
      saveMappingDescriptors(applicationName);       
      saveWebServicesJ2EEEngineDescriptors(applicationName);
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      //TODO - new message
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_GEN_FRM, new Object[]{applicationName}, e);
    } finally {      
      try {
	    if(wsClassLoader != null) {    	      
	      WSContainer.getServiceContext().getCoreContext().getLoadContext().unregister(wsClassLoader); 	  
	    }	
      } catch(Exception e) {
        // $JL-EXC$	      	 
      }
    }
  }
  
  private void generateJAXWSFramework(WebserviceDescriptionType serviceDescriptor, Service service, com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor, ServiceMetaData serviceMetaData, ModuleRuntimeData moduleRuntimeData, MappingRules mappingDescriptor, JaxWsIMappingGenerator jaxWSIMappingGenerator, InterfaceMappingProcessor interfaceMappingProcessor, WSDLSerializer wsdlSerializer, ClassLoader loader) throws WSDeploymentException {    
	String serviceName = serviceDescriptor.getWebserviceName().trim();
	BindingData[] bindingDatas = service.getServiceData().getBindingData();    
    if(bindingDatas == null || bindingDatas.length == 0) {
      return;  	    	
    } 
      
    try {	         
      File serviceTempDir = new File(webServicesContainerTempDir + "/ws" + System.currentTimeMillis());  
      Hashtable<String, String> seiImplClassNames = null;  
      if(serviceExtDescriptor != null) {
        seiImplClassNames = getSEIImplClassNames(serviceExtDescriptor);
      }
	        
      StringBuffer buf = new StringBuffer(512);
      getApplicationClassPath(loader, new HashSet(), buf);
      String compilerClassPath = buf.toString();
  	  Java2WsdlResult java2WsdlResult = null; 
  	  InterfaceMapping interfaceMapping = null;
  	  InterfaceMapping interfaceMappingNew;
      ServiceMapping serviceMapping = null;      
      Java2WsdlOptions java2WsdlOptions;
      int i = 0; 
            
      WsdlFileType wsdlFileDescriptor = serviceDescriptor.getWsdlFile(); 
      boolean isWSProvider = false;
      ArrayList<InterfaceMapping> intfMList = new ArrayList();
      String implClassName; 
      Class implClass; 
      for(BindingData bindingData: bindingDatas) {
        interfaceMapping = getServiceContext().getInterfaceMappingForBindingData(bindingData);
        implClassName = interfaceMapping.getImplementationLink().getProperty("impl-class");
        if(implClassName == null) { 
    	  if(seiImplClassNames != null && seiImplClassNames.size() != 0) {
        	implClassName = seiImplClassNames.get(bindingData.getConfigurationId());
    	  }	
        }
        implClass = loader.loadClass(implClassName);
        
        Annotation[] annotations = implClass.getAnnotations();
        isWSProvider = false;
        for(Annotation annotation : annotations) {
          String annClass = annotation.annotationType().getName();
          if ("javax.xml.ws.WebServiceProvider".equals(annClass)) {
            isWSProvider = true;
            break;
          }
        }
        if(isWSProvider) {
          interfaceMappingNew = JaxWsIMappingGeneratorForWSProvider.generateIMapping(implClass);
        } else {
          boolean hasWSDL = false;
          if(! (wsdlFileDescriptor == null || wsdlFileDescriptor.getWsdl() == null || wsdlFileDescriptor.getWsdl().length == 0)) { 
            hasWSDL = true;
          }
          java2WsdlOptions = new Java2WsdlOptions(implClassName, serviceTempDir.getAbsolutePath(), serviceTempDir.getAbsolutePath());
          java2WsdlOptions.setClassLoader(loader);
          java2WsdlOptions.setServerClassPath(compilerClassPath);
          java2WsdlOptions.setHasWSDL(hasWSDL);
          java2WsdlResult = jaxWSIMappingGenerator.generateWSDL(java2WsdlOptions);
          interfaceMappingNew = java2WsdlResult.getIntefaceMapping();
          serviceMapping = java2WsdlResult.getServiceMapping();      
        }
    	
    	interfaceMappingNew.setInterfaceMappingID(interfaceMapping.getInterfaceMappingID().trim());
    	interfaceMapping.setOperation(interfaceMappingNew.getOperation());
    	interfaceMapping.setProperty(interfaceMappingNew.getProperty());
      
        intfMList.add(interfaceMapping);
      }  	    	    	  

      String typesArchiveFileRelPath = null;  
      if(serviceTempDir.exists() && serviceTempDir.listFiles() != null) {
        typesArchiveFileRelPath = serviceMetaData.getTypesArchiveFileRelPath(SchemaStyleType._defaultTemp);
        String typesArchiveFilePath = moduleRuntimeData.getFilePathName(typesArchiveFileRelPath);
        new File(typesArchiveFilePath).getParentFile().mkdirs();        
        Vector<String> filters = new Vector<String>();        
        filters.add("class");
        filters.add("xml"); 
        filters.add("java"); 
        jarUtils.makeJarFromDir(typesArchiveFilePath, new String[]{serviceTempDir.getAbsolutePath()}, filters);
    
        TypeMappingFileType typesArchiveFileDescriptor = new TypeMappingFileType();
        typesArchiveFileDescriptor.setType(new SchemaTypeType(SchemaTypeType._framework));
        typesArchiveFileDescriptor.setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
        typesArchiveFileDescriptor.set_value(typesArchiveFileRelPath); 
        if(serviceDescriptor.getTypesArchiveFile() != null) {       
          TypeMappingFileType[] typesArchiveFileDescriptors = serviceDescriptor.getTypesArchiveFile(); 
          TypeMappingFileType[] typesArchiveFileDescriptorsAll = new TypeMappingFileType[typesArchiveFileDescriptors.length  + 1];
          System.arraycopy(typesArchiveFileDescriptors, 0, typesArchiveFileDescriptorsAll, 0, typesArchiveFileDescriptors.length);
          typesArchiveFileDescriptorsAll[typesArchiveFileDescriptorsAll.length - 1] = typesArchiveFileDescriptor; 
          serviceDescriptor.setTypesArchiveFile(typesArchiveFileDescriptorsAll);
        } else {
          serviceDescriptor.setTypesArchiveFile(new TypeMappingFileType[]{typesArchiveFileDescriptor});
        }             
      }    
      
  	  if(wsdlFileDescriptor == null || wsdlFileDescriptor.getWsdl() == null || wsdlFileDescriptor.getWsdl().length == 0) {
        if (java2WsdlResult == null) {
          throw new WSDeploymentException("WebServiceProvider endpoint without provided wsdl is not a valid one.");
        }
        SOAPBinding.Style style = java2WsdlResult.getBindingStyle(); 				
        DOMSource[] schemas = java2WsdlResult.getSchemas(); 		

        Definitions wsdlDefinitions = null; 
    	switch(style) {		
    	  case DOCUMENT: {
    		wsdlDefinitions = interfaceMappingProcessor.processJEEDocumentAndBareMappings(interfaceMapping, serviceMapping, schemas);
    		break;
          }
    	  case RPC: {
    	    wsdlDefinitions = interfaceMappingProcessor.processJEERPCMappings(interfaceMapping, serviceMapping, schemas);
    		break; 
    	  }
        }			  	  	 	
        String wsdlFileName = "wsdl" + System.currentTimeMillis();
    	String wsdlRelPath = META_INF + "/" + WSDL_DIR + "/" + wsdlFileName;
        String absWsdlDir = moduleRuntimeData.getModuleDir() + "/" + META_INF + "/" + WSDL_DIR;
    	wsdlSerializer.saveWsdl(wsdlDefinitions, absWsdlDir, wsdlFileName); 		
    				
    	WsdlType wsdlDescriptor = new WsdlType(); 
    	wsdlDescriptor.setType(new WsdlTypeType(WsdlTypeType._root));
    	WsdlStyleType styleDescriptor = new WsdlStyleType(WsdlStyleType._defaultTemp); 
    	wsdlDescriptor.setStyle(styleDescriptor);
    	wsdlDescriptor.set_value(wsdlRelPath);
    	wsdlFileDescriptor = new WsdlFileType();
    	wsdlFileDescriptor.setWsdl(new WsdlType[]{wsdlDescriptor});
    	serviceDescriptor.setWsdlFile(wsdlFileDescriptor); 
  	  } 	 	 	  
  	  
  	  String absWsdlDir = moduleRuntimeData.getModuleDir() + "/" + META_INF + "/" + WSDL_DIR;
  	  Definitions def = saveJEEBindingAndPortTypeTemplates(absWsdlDir, moduleRuntimeData.getModuleDir(), serviceDescriptor);
  	  
      // i044259
      if (! isWSProvider){
        updateOutputRPCOperationNS(def, intfMList);
      }
    } catch(Exception e) {
      //TODO - new message 
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);      
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_GEN_FRM_2 , new Object[]{applicationName, serviceName}, e); 
    }     
  } 
  
  private void updateOutputRPCOperationNS(Definitions def, List<InterfaceMapping> iMaps) throws Exception {
    // i044259
    QName serviceName = null;
    QName portName = null;
    for (int i = 0; i < iMaps.size(); i++) {
      InterfaceMapping iMap = iMaps.get(i);
      serviceName = iMap.getServiceQName();
      portName = iMap.getPortQName();
      
      com.sap.engine.services.webservices.espbase.wsdl.Service srv = def.getService(serviceName);
      if (srv == null){
        return;
      }
      com.sap.engine.services.webservices.espbase.wsdl.Endpoint ep = srv.getEndpoint(portName.getLocalPart());
      if (ep == null){
        return;
      }
      QName bQName = ep.getBinding();
      if (bQName != null) {
        Binding wsdlB = def.getBinding(bQName);
        if (wsdlB instanceof com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding) {
          com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding soapB = (com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding) wsdlB;
          
          OperationMapping ops[] = iMap.getOperation();
          for (OperationMapping op : ops) {
            String opName = op.getWSDLOperationName();
            SOAPBindingOperation soapBOp = soapB.getOperation(opName);
            if (soapBOp != null) {
              if ("rpc".equals(soapBOp.getProperty(SOAPBindingOperation.STYLE))) {
                String ns = soapBOp.getProperty(SOAPBindingOperation.OUTPUT_NAMESPACE);
                if (ns != null) {
                  op.setProperty(OperationMapping.OUTPUT_NAMESPACE, ns);
                  // i044259
                  op.setProperty(OperationMapping.INPUT_NAMESPACE, ns);
                }
              }
            }
          }
        }
      }
    }
  }
  
  private Hashtable<String, String> getSEIImplClassNames(com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor) {
	BindingDataType[] bindingDatas = serviceExtDescriptor.getBindingData();
    if(bindingDatas == null || bindingDatas.length == 0) {
      return new Hashtable<String, String>();  	
    }
    
    Hashtable<String, String> seiImplClassNames = new Hashtable<String, String>(); 
    String className; 
    for(BindingDataType bindingData: bindingDatas) {
      className = bindingData.getClassName(); 
      if(className != null) {
        seiImplClassNames.put(bindingData.getBindingDataName().trim(), className.trim());	  
      }     
    }
    
    return seiImplClassNames;	  
  }
    
  public static Definitions saveJEEBindingAndPortTypeTemplates(String outputWsdlFolder, String moduleDir, WebserviceDescriptionType wsDD) throws Exception {
    Definitions theDef = null;
    File rootWsdlFile = null;
    WsdlType[] wsType = wsDD.getWsdlFile().getWsdl();
    for (WsdlType type : wsType) {
      if (WsdlTypeType.root.equals(type.getType())) {
        String path = type.get_value().trim();
        rootWsdlFile = new File(moduleDir, path);
        break;
      }
    }

    if (rootWsdlFile == null || ! rootWsdlFile.exists()) {
      throw new WSDeploymentException("Unable to find 'root' wsdl type for web service '" + wsDD.getWebserviceName() + "'");
    }
    
    //remove the unnecessary CFG artefacts 
    OutsideInWSDLUtils.removeCFGArtefactsAndSave(rootWsdlFile);
    
    //load definitions
    WSDLLoader loader = new WSDLLoader();
    theDef = loader.load(rootWsdlFile.getAbsolutePath());
    
    Object[] portTypeData = saveSinglePortTypeElementInPredefinedDir(theDef, "pt_tmpl_" + System.currentTimeMillis());
    if (portTypeData == null) {
      Location.getLocation(WSDeployProcess.class).warningT("Wsdl with zero or more then one 'portType' is not supported for SAP's ws_policy framework. Target web service '" + wsDD.getWebserviceName() + "'");
      return theDef;
//	      throw new WSDeploymentException("Unable to find wsdl file with 'portType' element inside definitions: " + theDef);
    }
    
    File pTTemplate = (File) portTypeData[0];
    QName portTypeQName = (QName) portTypeData[1];
    
    //save porttype template
    OutsideInWSDLUtils.removeBindingAndServiceFromXMLAndSave(pTTemplate.getAbsolutePath());
    
    QName bindingQName = null;
    ObjectList bindings = theDef.getBindings();
    for (int i = 0; i < bindings.getLength(); i++) {
      Binding b = (Binding) bindings.item(i);
      if (portTypeQName.equals(b.getInterface())) {
        bindingQName = b.getName();
        break;
      }
    }
    if (bindingQName == null) {
      throw new WSDeploymentException("Unable to find wsdl:binding bound to wsdl:portType '" + portTypeQName + "'");
    }
    
    //save binding template
    File bindingTemplate = new File(rootWsdlFile.getParentFile(), "bnd_tmpl_" + rootWsdlFile.getName());
    OutsideInWSDLUtils.extractAndSaveBindingTemplate(theDef, bindingQName, bindingTemplate.getAbsolutePath());
    
    URI moduleDirURI = new File(moduleDir).toURI();
    
    //create porttype wsdlType
    WsdlType pt_tmp_type = new WsdlType();
    pt_tmp_type.setStyle(WsdlStyleType.defaultTemp);
    pt_tmp_type.setType(WsdlTypeType.porttype);
    pt_tmp_type.set_value(moduleDirURI.relativize(pTTemplate.toURI()).toString());
    //create binding wsdlType
    WsdlType bd_tmp_type = new WsdlType();
    bd_tmp_type.setStyle(WsdlStyleType.defaultTemp);
    bd_tmp_type.setType(WsdlTypeType.binding);
    bd_tmp_type.set_value(moduleDirURI.relativize(bindingTemplate.toURI()).toString());
    
    //update WS-DD
    wsDD.getWsdlFile().addWsdl(pt_tmp_type);
    wsDD.getWsdlFile().addWsdl(bd_tmp_type);
    
    return theDef;
  } 
  /**
   * The <code>def</code> is examined for portType element. Once such element is found, its containing wsdl:definitions
   * is stored in the <code>outputFile</code> in the same dir where the original wsdl file containing the portType resides.
   *
   * Returns object[] with 2 elements, where object[0] is the File pointing to theh wsdl file which contains portType,
   * and object[1] is portType's QName. If no portType is found null is returned.   
   */
  private static Object[] saveSinglePortTypeElementInPredefinedDir(Definitions def, String outputFile) throws Exception {
    ObjectList interfaces = def.getInterfaces();
    if (interfaces.getLength() != 1) {
      return null;
    }
    Interface intf = (Interface) interfaces.item(0);
    Node wsdlDef = intf.getDomElement().getParentNode();
    
    String origSysID = intf.getProperty(Base.SYSTEM_ID);
    if (origSysID == null) {
      throw new Exception("Interface object does not contain 'system-id' property: '" + intf + "'");
    }
    URL u = new URL(origSysID);
    File origFile = new File(u.getFile());
    File res = new File(origFile.getParentFile(), outputFile);
    res.getParentFile().mkdirs();
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.transform(new DOMSource(wsdlDef), new StreamResult(res));
    
    return new Object[]{res, intf.getName()};
  }
    
  private void saveMappingDescriptors(String applicationName) throws TypeMappingException, IOException {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
	if(wsApplicationDescriptorContext == null) {			
	  return; 
	}
	
	WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
	ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
	MappingDescriptorRegistry mappingDescriptorRegistry = wsApplicationDescriptorContext.getMappingDescriptorRegistry();
	WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry();
			
	Enumeration enumer = mappingDescriptorRegistry.getMappingDescriptors().keys(); 
	String moduleName;	
	MappingRules mappingDescriptor; 
	ModuleRuntimeData moduleRuntimeData;	
	WebservicesType webServicesJ2EEEngineDescriptor; 	
	String mappingDescriptorRelPath; 	
	while(enumer.hasMoreElements()) {
      moduleName = (String)enumer.nextElement(); 
      mappingDescriptor = mappingDescriptorRegistry.getMappingDescriptor(moduleName);
      moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);
      webServicesJ2EEEngineDescriptor = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptor(moduleName);
      mappingDescriptorRelPath = webServicesJ2EEEngineDescriptor.getWsdlMappingFile(); 
      if(mappingDescriptorRelPath == null) {
    	mappingDescriptorRelPath = META_INF + "/mappings.xml";
        webServicesJ2EEEngineDescriptor.setWsdlMappingFile(mappingDescriptorRelPath);        
      }
      mappingDescriptorRelPath = mappingDescriptorRelPath.trim();       
      MappingFactory.save(mappingDescriptor, moduleRuntimeData.getModuleDir() + "/" + mappingDescriptorRelPath);      
    }		
  }
  
  protected void upload() throws WSDeploymentException {
    try {    
      if(!appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {    
        return;        
      }  
      uploadImplementationFramework(appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME));
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);        
    }    
  }
  
  private void uploadImplementationFramework(Configuration webServicesConfiguration) throws WSDeploymentException {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName); 
    if(wsApplicationDescriptorContext == null) {
      return; 
    } 
    
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName); 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry();     
    Enumeration enumer = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptors().keys(); 
    String moduleName;
    WebservicesType webServicesJ2EEEngineDescriptor; 
    File moduleDir;     
    ModuleRuntimeData moduleRuntimeData; 
    Configuration moduleConfiguration; 
    try {    
      while(enumer.hasMoreElements()) {    	  
        moduleName = (String)enumer.nextElement();        
        webServicesJ2EEEngineDescriptor = webServicesJ2EEEngineDescriptorRegistry.getWebServicesJ2EEEngineDescriptor(moduleName);        
        moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);        
        moduleDir = new File(moduleRuntimeData.getModuleDir());
        moduleConfiguration = webServicesConfiguration.getSubConfiguration(moduleDir.getName());
        uploadFile(moduleDir, META_INF + "/"  + WEBSERVICES_J2EE_ENGINE_DESCRIPTOR, moduleConfiguration, true);
        uploadFile(moduleDir, webServicesJ2EEEngineDescriptor.getWsdlMappingFile(), moduleConfiguration, true);   
        uploadDirectory(moduleDir, META_INF + "/" + WSDL_DIR, moduleConfiguration, new HashSet());
        uploadJAXWSArchiveFiles(moduleRuntimeData, webServicesJ2EEEngineDescriptor.getWebserviceDescription(), moduleConfiguration);
      }
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);       
    }
  } 

  private void uploadJAXWSArchiveFiles(ModuleRuntimeData moduleRuntimeData, WebserviceDescriptionType[] serviceDescriptors, Configuration moduleConfiguration) throws IOException, ConfigurationException {	
    if(serviceDescriptors == null || serviceDescriptors.length == 0) {
      return; 	
    }    
        
    for(WebserviceDescriptionType serviceDescriptor: serviceDescriptors) {
      uploadJAXWSArchiveFiles(moduleRuntimeData, serviceDescriptor, moduleConfiguration); 	
    }    
  }
  
  private void uploadJAXWSArchiveFiles(ModuleRuntimeData moduleRuntimeData, WebserviceDescriptionType serviceDescriptor, Configuration moduleConfiguration) throws IOException, ConfigurationException {
    TypeMappingFileType[] archiveFileDescriptors = serviceDescriptor.getTypesArchiveFile();
    if(archiveFileDescriptors == null || archiveFileDescriptors.length == 0) {
      return; 	
    }
    
    File moduleDir = new File(moduleRuntimeData.getModuleDir()); 
    for(TypeMappingFileType archiveFileDescriptor: archiveFileDescriptors) {
      uploadFile(moduleDir, archiveFileDescriptor.get_value(), moduleConfiguration, true);	
    }
  }
  
  private void setApplicationStartInfo() {
    this.applicationStartInfo = makeApplicationStartInfo();
  }
  
  private ApplicationStartInfo makeApplicationStartInfo() {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {      
      return new ApplicationStartInfo(true);
    }  
       
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();        
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();                      
    ServiceDescriptorRegistry serviceDescriptorRegistry = (ServiceDescriptorRegistry)wsApplicationDescriptorContext.getServiceDescriptorRegistry();
        
    Enumeration enumer = serviceDescriptorRegistry.getServiceDescriptors().elements();    
    String[] filesForClassLoader = new String[0];
    WebserviceDescriptionType serviceDescriptor; 
    ModuleRuntimeData moduleRuntimeData;      
    ServiceMetaData serviceMetaData;   
    String[] serviceFilesForClassLoader; 
    while(enumer.hasMoreElements()) {
      serviceDescriptor = (WebserviceDescriptionType)enumer.nextElement();            
      serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceDescriptor.getWebserviceName());                
      moduleRuntimeData = (ModuleRuntimeData)moduleRuntimeDataRegistry.getModuleRuntimeData(serviceMetaData.getModuleName());        
      serviceFilesForClassLoader = getServiceFilesForClassLoader(serviceDescriptor, moduleRuntimeData);      
      filesForClassLoader = WSUtil.unifyStrings(new String[][]{filesForClassLoader, serviceFilesForClassLoader});                                                                                      
    } 
    
    ApplicationStartInfo applicationStartInfo = new ApplicationStartInfo();    
    applicationStartInfo.setFilesForClassloader(filesForClassLoader);
    
    return applicationStartInfo; 
  }
  
  private void getApplicationClassPath(ClassLoader loader, Set checkedLoaders, StringBuffer appClassPath) {
    LoadContext loadContext = WSContainer.getServiceContext().getCoreContext().getLoadContext(); 
    String appLoaderName = loadContext.getName(loader); 

    if(appLoaderName == null || checkedLoaders.contains(appLoaderName)) {
      return; 
    }
    
    checkedLoaders.add(appLoaderName);
    String[] appLoaderResources = loadContext.getResourceNames(loader);
    String[] refLoaderNames = loadContext.getReferences(loader);
    ClassLoader[] parents = loadContext.getClassLoaderParents(loader);
    if(appLoaderResources != null && appLoaderResources.length != 0) {
      for (int i = 0; i < appLoaderResources.length; i++) {
        appClassPath.append(appLoaderResources[i]);
        appClassPath.append(File.pathSeparatorChar);
      }
    }
    if(refLoaderNames != null && refLoaderNames.length != 0) {
      ClassLoader refLoader;
      String[] refLoaderResources;  
      for(int i = 0; i < refLoaderNames.length; i++) {
        refLoader = loadContext.getClassLoader(refLoaderNames[i]);
        if(refLoader == null) {
          continue;
        }          
        
        refLoaderResources = loadContext.getResourceNames(refLoader);
        if(refLoaderResources != null && refLoaderResources.length != 0) {
          for(int j = 0; j < refLoaderResources.length; j++) {
            appClassPath.append(refLoaderResources[j]);
            appClassPath.append(File.pathSeparatorChar);
          }
        }        
      }
    }
    
    
    if (parents != null) {
      for (ClassLoader pLoader : parents) {
        if (pLoader != null) {
          getApplicationClassPath(pLoader, checkedLoaders, appClassPath);
        } 
      } 
    }       
  }
  
  private String[] getArchiveFilePaths() throws IOException {
	WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {      
      return new String[0];
    }
	  
	WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
	ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
	
	Enumeration enumer = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements(); 
	String[] archiveFilePaths = new String[moduleRuntimeDataRegistry.getModuleRuntimeDatas().size()]; 
	ModuleRuntimeData moduleRuntimeData;
	int i = 0;
	while(enumer.hasMoreElements()) {
	  moduleRuntimeData = (ModuleRuntimeData)enumer.nextElement();
	  archiveFilePaths[i++] = getArchiveFilePath(moduleRuntimeData, new File(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP + "/" + moduleRuntimeData.getArchiveFileRelPath()));	  
	}
	
	return archiveFilePaths;	
  } 
  
  private String getArchiveFilePath(ModuleRuntimeData moduleRuntimeData, File archiveFile) throws IOException {	
	String moduleType = moduleRuntimeData.getType();               
    try {
      if(moduleType.equals(ModuleRuntimeData.EJB_SUFFIX)) {
        return archiveFile.getAbsolutePath(); 
      }           
      if(moduleType.equals(ModuleRuntimeData.WEB_SUFFIX) || moduleType.equals(ModuleRuntimeData.WS_SUFFIX)) {                	
        String tmpArchiveFilePath = moduleRuntimeData.getModuleWorkingDir() + "/" + archiveFile.getName().substring(0, archiveFile.getName().lastIndexOf(".")) + "_tmp.jar";         
        new File(tmpArchiveFilePath).getParentFile().mkdirs();               
        if(moduleType.equals(ModuleRuntimeData.WEB_SUFFIX)) {        
          jarUtil.makeJarFile(archiveFile, new String[]{WEB_INF + "/classes"}, new String[0], new String[0], new String[0], tmpArchiveFilePath);        
        } else {
          jarUtil.makeJarFile(archiveFile, new String[0], new String[0], new String[0], new String[0], tmpArchiveFilePath);
        }                                                    
        return tmpArchiveFilePath;  
      }      
    } catch(IOException e) {
      //TODO
      e.printStackTrace(); 
      throw e; 
    } 
    
    return ""; 
  }
  
}