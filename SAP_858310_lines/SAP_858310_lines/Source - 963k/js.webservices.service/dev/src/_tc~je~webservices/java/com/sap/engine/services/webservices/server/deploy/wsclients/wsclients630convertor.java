package com.sap.engine.services.webservices.server.deploy.wsclients;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import com.sap.engine.lib.descriptors.ws04clientsdd.ComponentScopedRefsDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.ServiceRefDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.WSClientDeploymentDescriptor;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLDescriptor;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLSerializer;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLMarshalException;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplArchiveFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.SchemaStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WsClientsType;
import com.sap.engine.services.webservices.server.deploy.j2ee.ws.J2EE14WSClientConvertor;
import com.sap.engine.services.webservices.server.deploy.migration.wsclients.SecurityPropertyListConverter;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsFactory;
import com.sap.guid.GUID;

public class WSClients630Convertor {
	
  private static final String META_INF = "META-INF";
  private static final String WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR = "ws-clients-descriptors";
  private static final String WS_CLIENTS_DEPLOYMENT_DESCRIPTOR = "ws-clients-deployment-descriptor";
  
  private String tempDir;
  private File archiveFile;
  private String applicationName; 
  private String alias; 
  
  private ProxyGeneratorNew proxyGenerator;
  private JarUtils jarUtils;  
  private LogicalPortFactory logicalPortFactory;
  private WSDLLoader wsdlLoader; 
  private WSDLSerializer wsdlSerializer; 
  
  public WSClients630Convertor() throws WSDLMarshalException {
    this.proxyGenerator = new ProxyGeneratorNew();    
    this.jarUtils = new JarUtils();     
    this.logicalPortFactory = new LogicalPortFactory();
    this.wsdlLoader = new WSDLLoader();    
    this.wsdlSerializer = new WSDLSerializer();               
  }
  
  private void init(String tempDir, File archiveFile, String applicationName, String alias) {
    this.tempDir = tempDir; 
	this.archiveFile = archiveFile;
	this.applicationName = applicationName;
	this.alias = alias; 
  }
  
  public File convert(String tempDir, File archiveFile, String applicationName, String alias) throws Exception {
    init(tempDir, archiveFile, applicationName, alias);
            
	JarFile archiveJarFile = null; 
    File archiveFileNew; 
    FileOutputStream out = null; 
    JarOutputStream jarOut = null;
	try {
      archiveJarFile = new JarFile(archiveFile);      
      String[] wsClientsDeploymentDescriptorsEntries = JarUtil.getEntryNames(META_INF + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTOR, archiveJarFile);      
      if(wsClientsDeploymentDescriptorsEntries != null && wsClientsDeploymentDescriptorsEntries.length != 0) {     
         
        InputStream wsClientsDeploymentDescriptorIn = null;
        WSClientDeploymentDescriptor[] wsClientsDeploymentDescriptors = new WSClientDeploymentDescriptor[wsClientsDeploymentDescriptorsEntries.length];         
        int i = 0; 
        for(String wsClientsDeploymentDescriptorEntry: wsClientsDeploymentDescriptorsEntries) {
          try {
    	    wsClientsDeploymentDescriptorIn = archiveJarFile.getInputStream(archiveJarFile.getEntry(wsClientsDeploymentDescriptorEntry));
    	    wsClientsDeploymentDescriptors[i++] = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(wsClientsDeploymentDescriptorIn);               
    	  } finally {
    	    try {        	 
    	      if(wsClientsDeploymentDescriptorIn != null) {
    	        wsClientsDeploymentDescriptorIn.close(); 	  
    	      }
    	    } catch(Exception e) {
    	      // $JL-EXC$	
    	    }
    	  }      	  
        }  
        
        if(!WSClientsFactory.checkWSClientsDeploymentDescriptorsOR(wsClientsDeploymentDescriptors)) {  	                            
    	  Hashtable<String, ConfigurationRoot>  configurationDescriptors = new Hashtable<String, ConfigurationRoot>();
          Hashtable<String, MappingRules> mappingDescriptors = new Hashtable<String, MappingRules>(); 
          Hashtable<String, SchemaTypeSet> typeMappings = new Hashtable<String, SchemaTypeSet>();
          Hashtable<String, Definitions> wsdlDefinitions = new Hashtable<String, Definitions>(); 
          Hashtable<String, String> implArchiveFilePaths = new Hashtable<String, String>(); 
          WsClientsType wsClientsJ2EEEngineDescriptor = convertWSClientsDeploymentDescriptors(wsClientsDeploymentDescriptors, archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths);
       
          archiveFileNew = new File(tempDir + "/" + archiveFile.getName());
          archiveFileNew.getParentFile().mkdirs(); 
          out = new FileOutputStream(archiveFileNew); 
          jarOut = new JarOutputStream(out);
          String metaInfDir = ModuleRuntimeData.getMetaInfRelDir(ModuleRuntimeData.getType(archiveFile));
          JarUtil.copyEntries(archiveJarFile, new String[0], new String[]{META_INF + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR}, new String[0], new String[0], false, jarOut);                 
          addWSClientsJ2EEEngineDescriptors(metaInfDir, wsClientsJ2EEEngineDescriptor, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, jarOut);
          return archiveFileNew;
        }
      }         
    } catch(Exception e) {
      // $JL-EXC$ 
      //TODO
      e.printStackTrace(); 
      throw e;  
    } finally {
      try {
        if(archiveJarFile != null) {
          archiveJarFile.close();	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }      
      try {
        if(jarOut != null) {
          jarOut.close(); 	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }
      try {
        if(out != null) {
          out.close();	
        }  
      } catch(Exception e) {
        // $JL-EXC$	   
      }      
    } 
    
    return null; 	  
  }
  
  private WsClientsType convertWSClientsDeploymentDescriptors(WSClientDeploymentDescriptor[] wsClientsDeploymentDescriptors, JarFile archiveJarFile, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths) throws Exception {
    if(wsClientsDeploymentDescriptors == null || wsClientsDeploymentDescriptors.length == 0) {
      return null; 	
    }
    
    ArrayList<ServiceRefGroupDescriptionType[]> serviceRefGroupDescriptors = new ArrayList<ServiceRefGroupDescriptionType[]>();
    for(WSClientDeploymentDescriptor wsClientsDeploymentDescriptor: wsClientsDeploymentDescriptors) {
      convertWSClientsDeploymentDescriptors(wsClientsDeploymentDescriptor, archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, serviceRefGroupDescriptors);	
    }
        
    WsClientsType wsClientsJ2EEEngineDesciptor = new WsClientsType();
    wsClientsJ2EEEngineDesciptor.setServiceRefGroupDescription(unifyServiceRefGroupDescriptors(serviceRefGroupDescriptors));
    return wsClientsJ2EEEngineDesciptor;	  
  } 
  
  private void convertWSClientsDeploymentDescriptors(WSClientDeploymentDescriptor wsClientsDeploymentDescriptor, JarFile archiveJarFile, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths, ArrayList<ServiceRefGroupDescriptionType[]> serviceRefGroupDescriptors) throws Exception {	  
    convertComponentServiceRefDescriptors(wsClientsDeploymentDescriptor.getComponentScopedRefs(), archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, serviceRefGroupDescriptors); 	
    serviceRefGroupDescriptors.add(convertServiceRefDescriptors(wsClientsDeploymentDescriptor.getServiceRef(), archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, false, null));                 
  }
  
  private void convertComponentServiceRefDescriptors(ComponentScopedRefsDescriptor[] componentServiceRefDescriptors, JarFile archiveJarFile, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths, ArrayList<ServiceRefGroupDescriptionType[]> serviceRefGroupDescriptors) throws Exception {    
	if(componentServiceRefDescriptors == null || componentServiceRefDescriptors.length == 0) {
      return; 	
    }	  
    
    String componentName;  
    for(ComponentScopedRefsDescriptor componentServiceRefDescriptor: componentServiceRefDescriptors) {
      componentName = componentServiceRefDescriptor.getComponentName();
      if(componentName == null) {      
        if(archiveFile.getName().endsWith(".war") ) {
          componentName = alias;  
        }        
      } 
      serviceRefGroupDescriptors.add(convertServiceRefDescriptors(componentServiceRefDescriptor.getServiceRef(), archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, true, componentName));
    }    
  }
  
  private ServiceRefGroupDescriptionType[] convertServiceRefDescriptors(ServiceRefDescriptor[] serviceRefDescriptors, JarFile archiveJarFile, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths, boolean isConsumerMode, String componentName) throws Exception { 
    if(serviceRefDescriptors == null || serviceRefDescriptors.length == 0) {
      return new ServiceRefGroupDescriptionType[0]; 
    }	  
    
    ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors = new ServiceRefGroupDescriptionType[serviceRefDescriptors.length];
    int i = 0; 
    for(ServiceRefDescriptor serviceRefDescriptor: serviceRefDescriptors) {
      serviceRefGroupDescriptors[i++] = convertServiceRefDescriptor(serviceRefDescriptor, archiveJarFile, configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths, isConsumerMode, componentName); 	
    }
    
    return serviceRefGroupDescriptors; 
  } 
  
  private ServiceRefGroupDescriptionType convertServiceRefDescriptor(ServiceRefDescriptor serviceRefDescriptor, JarFile archiveJarFile, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths, boolean isComponentMode, String componentName) throws Exception {    	     
    String moduleType = ModuleRuntimeData.getType(archiveJarFile.getName()); 
    String metaInfRelDir = ModuleRuntimeData.getMetaInfRelDir(moduleType); 
    String binRelDir = ModuleRuntimeData.getBinRelDir(moduleType);
    
    InputStream logicalPortsDescriptorIn = null; 
    InputStream typeMappingIn = null;    
    ByteArrayOutputStream typeMappingOut = null; 
    ServiceRefGroupDescriptionType serviceRefGroupDescriptor; 
    try {
      serviceRefGroupDescriptor = new ServiceRefGroupDescriptionType();	
      String serviceRefGroupName = serviceRefDescriptor.getServiceRefName();              
      serviceRefGroupDescriptor.setServiceRefGroupName(serviceRefGroupName);         
      
      String guid = new GUID().toHexString(); 
      String outputDir = tempDir + "/wscl" + guid;       
      ProxyGeneratorConfigNew proxyGeneratorConfig = new ProxyGeneratorConfigNew();
      String packageName = serviceRefDescriptor.getPackageName();
      Properties[] locationMaps = loadProperties(META_INF + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR, serviceRefDescriptor.getUriMappingFile().split(";"), archiveJarFile);       
      String[] wsdlFileRelPaths = serviceRefDescriptor.getWsdlFile().split(";");
      Definitions[] currentWSDLDefinitions = loadWSDLDefinitions(META_INF + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR, wsdlFileRelPaths, locationMaps, archiveJarFile, wsdlLoader);      
      Definitions wsdlDefinitionsAll = mergeWSDLDefinitions(currentWSDLDefinitions, wsdlLoader); 
      proxyGeneratorConfig.setWsdl(wsdlDefinitionsAll); 
      ZipEntry logicalPortsDescriptorEntry = archiveJarFile.getEntry(META_INF + "/" + WS_CLIENTS_DEPLOYMENT_DESCRIPTORS_DIR + "/" + serviceRefDescriptor.getLogicalPortsFile()); 
      logicalPortsDescriptorIn = archiveJarFile.getInputStream(logicalPortsDescriptorEntry); 
      LogicalPorts logicalPortsDescriptor = logicalPortFactory.loadLogicalPorts(logicalPortsDescriptorIn);
      proxyGeneratorConfig.setLogicalPorts(logicalPortsDescriptor);
                      
      String typeMappingRelPath = packageName.replace('.', '/') + "/types.xml";
      if (!binRelDir.equals("")) {
        typeMappingRelPath = binRelDir + "/" + typeMappingRelPath;        
      }
      
      ZipEntry typeMappingEntry = archiveJarFile.getEntry(typeMappingRelPath);      
      if(typeMappingEntry == null) {
        TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl();
        typeMappingOut = new ByteArrayOutputStream();        
        typeMappingRegistry.toXmlFile(typeMappingOut);        
        typeMappingIn = new ByteArrayInputStream(typeMappingOut.toByteArray());                 
      } else {
        typeMappingIn = archiveJarFile.getInputStream(typeMappingEntry);                        
      } 
      
      proxyGeneratorConfig.getSchemaConfig().getTypeSet().loadSettings(typeMappingIn);                    
      proxyGeneratorConfig.setOutputPackage(packageName); 
      proxyGeneratorConfig.setOutputPath(outputDir);      
      proxyGeneratorConfig.setUnwrapDocumentStyle(true);
      proxyGeneratorConfig.setGenerateSerializable(false);
      ConfigurationMarshallerFactory factory = com.sap.engine.services.webservices.server.WSContainer.createInitializedServerCFGFactory();
      proxyGeneratorConfig.setConfigMarshaller(factory);
      proxyGeneratorConfig.setPropertyConvertor(new SecurityPropertyListConverter());
      proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.MIGRATION_MODE);      
      proxyGenerator.generateAll(proxyGeneratorConfig);                   
      
      String implArchiveFileRelPath = "jar" + guid + "_src.jar"; 
      String implArchiveFilePath = tempDir + "/" + implArchiveFileRelPath;      
      jarUtils.makeJarFromDir(implArchiveFilePath, outputDir); 
      
      ConfigurationRoot configurationDescriptor = proxyGeneratorConfig.getProxyConfig();
      MappingRules mappingDescriptor = proxyGeneratorConfig.getMappingRules();
      Service service = configurationDescriptor.getRTConfig().getService()[0]; 
      overrideService(applicationName, serviceRefDescriptor, service, J2EE14WSClientConvertor.loadInterfaceDefinitions(configurationDescriptor.getDTConfig().getInterfaceDefinition()), mappingDescriptor.getService(service.getServiceMappingId()), isComponentMode, componentName);    
      
      configurationDescriptors.put(serviceRefGroupName, configurationDescriptor);
      mappingDescriptors.put(serviceRefGroupName, mappingDescriptor);      
      typeMappings.put(serviceRefGroupName, proxyGeneratorConfig.getSchemaConfig().getTypeSet());
      wsdlDefinitions.put(serviceRefGroupName, wsdlDefinitionsAll); 
      implArchiveFilePaths.put(serviceRefGroupName, implArchiveFilePath);
      
      serviceRefGroupDescriptor.setWsdlFile(metaInfRelDir + "/wsdl/" + wsdlFileRelPaths[0]);
      serviceRefGroupDescriptor.setConfigurationFile(metaInfRelDir + "/" + serviceRefGroupName + "_configurations.xml"); 
      serviceRefGroupDescriptor.setWsdlMappingFile(metaInfRelDir + "/" + serviceRefGroupName + "_mappings.xml"); 
      TypeMappingFileType typeMappingFileType = new TypeMappingFileType(); 
      typeMappingFileType.setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
      typeMappingFileType.setType(new SchemaTypeType(SchemaTypeType._framework));
      typeMappingFileType.set_value(metaInfRelDir + "/" + serviceRefGroupName + "_types.xml");
      serviceRefGroupDescriptor.setTypeMappingFile(new TypeMappingFileType[]{typeMappingFileType});
      ImplArchiveFileType implArchiveFileRelPathType = new ImplArchiveFileType();      
      implArchiveFileRelPathType.setType(ImplTypeType.src);
      implArchiveFileRelPathType.set_value("jars/" + implArchiveFileRelPath);            
      serviceRefGroupDescriptor.setImplArchiveFile(new ImplArchiveFileType[]{implArchiveFileRelPathType});
    } catch(Exception e) {
      //TODO 
      e.printStackTrace(); 
      throw e;  	
    } finally {      
      try {
	    if(logicalPortsDescriptorIn != null) {
	      logicalPortsDescriptorIn.close(); 	
	    }	  
      } catch(Exception e) {
  	    // $JL-EXC$	   
      }
      try {
        if(typeMappingOut != null) {
          typeMappingOut.close(); 	
        }	  
      } catch(Exception e) {
        // $JL-EXC$	   
      }       
      try {
        if(typeMappingIn != null) {
          typeMappingIn.close(); 	
        }	  
      } catch(Exception e) {
	    // $JL-EXC$	   
      }           
    }        
    
    return serviceRefGroupDescriptor; 
  }
  
  private void overrideService(String applicationName, ServiceRefDescriptor serviceRefDescriptor, Service service, Hashtable<String, InterfaceDefinition> interfaceDefinitions, ServiceMapping serviceMapping, boolean isComponentMode, String componentName) {
    service.setName(serviceRefDescriptor.getServiceRefName() + "_"  + service.getName());     
	overrideServiceMapping(serviceRefDescriptor, serviceMapping, isComponentMode, componentName);
	J2EE14WSClientConvertor.overrideInterfaceDefinitionIDs(applicationName, serviceMapping.getImplementationLink().getServiceRefJNDIName(), service.getServiceData().getBindingData(), interfaceDefinitions);
  }
  
  private void overrideServiceMapping(ServiceRefDescriptor serviceRefDescriptor, ServiceMapping serviceMapping, boolean isComponentMode, String componentName) {    
    ImplementationLink implLink = serviceMapping.getImplementationLink();
    if(implLink == null) {
      serviceMapping.setImplementationLink(new ImplementationLink()); 
      implLink = serviceMapping.getImplementationLink(); 
    }
    
    implLink.setServiceRefJNDIName(serviceRefDescriptor.getServiceRefName()); 
    if(isComponentMode) {
      implLink.setProperty("IsComponentMode", "true"); 
      if(componentName != null) {
        implLink.setProperty("ComponentName", componentName);	
      }
    }
  }  
  
  private Hashtable<String, ServiceMapping> loadServiceMappings(ServiceMapping[] serviceMappings) {
    if(serviceMappings == null || serviceMappings.length == 0) {
      return new Hashtable<String, ServiceMapping>();       
    }
    
    Hashtable<String, ServiceMapping> serviceMappingsTable = new Hashtable<String, ServiceMapping>();
    for(ServiceMapping serviceMapping: serviceMappings) {
      serviceMappingsTable.put(serviceMapping.getServiceMappingId(), serviceMapping);	
    }
    
    return serviceMappingsTable; 
  }
  
  private ServiceRefGroupDescriptionType[] unifyServiceRefGroupDescriptors(ArrayList<ServiceRefGroupDescriptionType[]> serviceRefGroupDescriptors) {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.size() == 0) {
      return new ServiceRefGroupDescriptionType[0]; 	
    }
    
    if(serviceRefGroupDescriptors.size() == 1) {
      return serviceRefGroupDescriptors.get(0);	
    }
    
    ArrayList<ServiceRefGroupDescriptionType> serviceRefGroupDescriptorsList = new ArrayList<ServiceRefGroupDescriptionType>(); 
    for(ServiceRefGroupDescriptionType[] currentServiceRefGroupDescriptors: serviceRefGroupDescriptors) {
      addServiceRefGroupDescriptors(currentServiceRefGroupDescriptors, serviceRefGroupDescriptorsList); 	
    }
 
    return serviceRefGroupDescriptorsList.toArray(new ServiceRefGroupDescriptionType[serviceRefGroupDescriptorsList.size()]); 
  }
  
  private void addServiceRefGroupDescriptors(ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors, ArrayList<ServiceRefGroupDescriptionType> serviceRefGroupDescriptorsList) {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 	
    }	  
    
    for(ServiceRefGroupDescriptionType serviceRefGroupDescriptor: serviceRefGroupDescriptors) {
      serviceRefGroupDescriptorsList.add(serviceRefGroupDescriptor); 	
    }
  }
   
  private void addWSClientsJ2EEEngineDescriptors(String metaInfRelDir, WsClientsType wsClientsJ2EEngineDescriptor, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths, JarOutputStream jarOut) throws IOException, TypeMappingException, WSDLMarshalException, WSDLException {        
    try { 
	  jarOut.putNextEntry(new JarEntry(metaInfRelDir + "/" + WSClientsAbstractDProcess.WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR));
	  WSClientsJ2EEEngineFactory.save(wsClientsJ2EEngineDescriptor, jarOut); 	  	 
	} finally {
	  try {
        jarOut.closeEntry();  
      } catch(Exception e) {
        // $JL-EXC$		
      }	  
    } 
	
	addServiceRefGroupDescriptors(jarOut, wsClientsJ2EEngineDescriptor.getServiceRefGroupDescription(), configurationDescriptors, mappingDescriptors, typeMappings, wsdlDefinitions, implArchiveFilePaths);
  }
  
  private void addServiceRefGroupDescriptors(JarOutputStream jarOut, ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors, Hashtable<String, ConfigurationRoot> configurationDescriptors, Hashtable<String, MappingRules> mappingDescriptors, Hashtable<String, SchemaTypeSet> typeMappings, Hashtable<String, Definitions> wsdlDefinitions, Hashtable<String, String> implArchiveFilePaths) throws IOException, TypeMappingException, WSDLMarshalException, WSDLException {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 	
    }	  
    
    String serviceRefGroupName; 
    for(ServiceRefGroupDescriptionType serviceRefGroupDescriptor: serviceRefGroupDescriptors) {
      serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName(); 
      addServiceRefGroupDescriptors(jarOut, serviceRefGroupDescriptor, configurationDescriptors.get(serviceRefGroupName), mappingDescriptors.get(serviceRefGroupName), typeMappings.get(serviceRefGroupName), wsdlDefinitions.get(serviceRefGroupName), implArchiveFilePaths.get(serviceRefGroupName));
    }    
  }
  
  private void addServiceRefGroupDescriptors(JarOutputStream jarOut, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, ConfigurationRoot configurationDescriptor, MappingRules mappingDescriptor, SchemaTypeSet typeMapping, Definitions wsdlDefinitions, String implArchiveFilePath) throws IOException, TypeMappingException, WSDLMarshalException, WSDLException {
    try {
	  jarOut.putNextEntry(new JarEntry(serviceRefGroupDescriptor.getConfigurationFile())); 
      ConfigurationFactory.save(configurationDescriptor, jarOut);      
    } finally {
      try {
        jarOut.closeEntry();  
      } catch(Exception e) {
        // $JL-EXC$		
      }	 
    }
    
    try {
      jarOut.putNextEntry(new JarEntry(serviceRefGroupDescriptor.getWsdlMappingFile())); 
      MappingFactory.save0(mappingDescriptor, jarOut);
    } finally {
      try {
        jarOut.closeEntry();  
      } catch(Exception e) {
        // $JL-EXC$		
      }	 
    }
   
    try {       	
      jarOut.putNextEntry(new JarEntry(serviceRefGroupDescriptor.getTypeMappingFile()[0].get_value()));      
      typeMapping.saveSettings0(jarOut);	            
    } finally {	  
      try {
        jarOut.closeEntry();  
      } catch(Exception e) {
        // $JL-EXC$		
      }	 
    }  
        
    String wsdlFileRelPath = serviceRefGroupDescriptor.getWsdlFile();      
    ArrayList<WSDLDescriptor> wsdlDescriptors = (ArrayList<WSDLDescriptor>)wsdlSerializer.serialize(wsdlDefinitions);      
    setWSDLDescriptorFileNames(wsdlDescriptors, generateIndexedFilePaths(wsdlFileRelPath, 0, wsdlDescriptors.size()));               
    wsdlDescriptors.get(0).setFileName(wsdlFileRelPath);
    wsdlSerializer.save(wsdlDescriptors, jarOut);           
    
    if(implArchiveFilePath != null) {
      JarUtil.addFile(jarOut, serviceRefGroupDescriptor.getImplArchiveFile()[0].get_value(), new File(implArchiveFilePath));	
    }   
  }
  
  private Properties[] loadProperties(String baseDirPath, String[] fileRelPaths, JarFile jarFile) throws IOException {
    if(fileRelPaths == null || fileRelPaths.length == 0) {
      return new Properties[0]; 	
    }  
    
    Properties[] properties = new Properties[fileRelPaths.length];
    JarEntry entry;
    InputStream in = null; 
    Properties currentProperties;         
    int i = 0; 
    for(String fileRelPath: fileRelPaths) {
      try {
        entry = jarFile.getJarEntry(baseDirPath + "/" + fileRelPath); 
        in = jarFile.getInputStream(entry);
        currentProperties = new Properties();
        currentProperties.load(in);
        properties[i++] = currentProperties;          
      } finally {
    	try { 
          if(in != null)  {
            in.close();  	             
          }
    	} catch(Exception e) {
          // $JL-EXC$	  
    	}	
      }
    }
    
    return properties; 
  } 
 
  private Definitions[] loadWSDLDefinitions(String baseDirPath, String[] wsdlFileRelPaths, Properties[] properties, JarFile jarFile, WSDLLoader wsdlLoader) throws WSDLException {
    if(wsdlFileRelPaths == null || wsdlFileRelPaths.length == 0) {
      return new Definitions[0];
    }	  
        		
    Definitions[] wsdlDefinitions = new Definitions[wsdlFileRelPaths.length];
    int i = 0; 
    for(String wsdlFileRelPath: wsdlFileRelPaths) {     
     wsdlDefinitions[i] = wsdlLoader.load("jar:file:" + jarFile.getName() + "!/" + baseDirPath + "/" + wsdlFileRelPath, properties[i]);
      i++; 
    } 
    
    return wsdlDefinitions;
  }
  
  private Definitions mergeWSDLDefinitions(Definitions[] wsdlDefinitions, WSDLLoader wsdlLoader) throws WSDLException { 
    Definitions wsdlDefinitionsMerged = wsdlDefinitions[0];     
    
    Definitions currentWSDLDefinitions; 
    for(int i = 1; i < wsdlDefinitions.length; i++) {
      currentWSDLDefinitions = wsdlDefinitions[i]; 
      addBingings(currentWSDLDefinitions.getBindings(), wsdlDefinitionsMerged); 
      addPortTypes(currentWSDLDefinitions.getInterfaces(), wsdlDefinitionsMerged);      
    }
    
    return wsdlDefinitionsMerged; 
  }
  
  private void addPortTypes(ObjectList portTypes, Definitions wsdlDefinitions) throws WSDLException {
    if(portTypes == null || portTypes.getLength() == 0) {
      return; 	
    }
    
    Interface portType;
    for(int i = 0; i < portTypes.getLength(); i++) {      
      portType = (Interface)portTypes.item(i);       
      if(wsdlDefinitions.getInterface(portType.getName()) == null) {
        wsdlDefinitions.appendChild(portType);	  
      } 
    }
  }
  
  private void addBingings(ObjectList bindings, Definitions wsdlDefinitions) throws WSDLException {
    if(bindings == null || bindings.getLength() == 0) {
      return; 	   
    } 	  
   
    Binding binding; 
    for(int i = 0; i < bindings.getLength(); i++) {
      binding = (Binding)bindings.item(i); 
      if(wsdlDefinitions.getBinding(binding.getName()) == null) {
        wsdlDefinitions.appendChild(binding);	  
      }          
    }
  }
  
  private String[] generateIndexedFilePaths(String filePath, int startIndex, int length) {
    String filePathNoExt = filePath; 
	String ext = ""; 	
	int cutIndex = filePath.lastIndexOf(".");  
	if(cutIndex != -1) {
	  filePathNoExt= filePath.substring(0, cutIndex);
	  ext = filePath.substring(cutIndex + 1); 
	}
 
	String[] filePaths = new String[length];
	for(int i = 0; i < length; i++) {
	  filePaths[i] = filePathNoExt + (startIndex + i) + "." + ext;	
	}
	
	return filePaths;	
  }
  
  private void setWSDLDescriptorFileNames(ArrayList<WSDLDescriptor> wsdlDescriptors, String[] fileNames) {
    if(wsdlDescriptors == null || wsdlDescriptors.size() == 0) {
      return; 	
    } 
    
    int i = 0; 
    for(WSDLDescriptor wsdlDescriptor: wsdlDescriptors) {
      wsdlDescriptor.setFileName(fileNames[i++]); 	
    }	   
  }
  
}
