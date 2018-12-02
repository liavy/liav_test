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
package com.sap.engine.services.webservices.server.deploy.j2ee.ws;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaWsdlMappingType;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.util.legacy.StaxDOMLoader;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationBuilder;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerException;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineExtFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WsClientsType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.PortType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.WsClientsExtType;
import com.sap.engine.services.webservices.server.deploy.jee5.WSClientsAnnotationConvertor;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * 
 * @author Boyan Slavov
 */

public class J2EE14WSClientConvertor {

  public static final String SERVICE_REF_NAME = "service-ref-name";
  public static final String SERVICE_INTERFACE = "service-interface";
  public static final String SERVICE_QNAME = "service-qname";
  public static final String WSDL_FILE = "wsdl-file";
  public static final String JAXRPC_MAPPING_FILE = "jaxrpc-mapping-file";
  public static final String PORT_COMPONENT_REF = "port-component-ref";
  public static final String PORT_COMPONENT_LINK = "port-component-link";
  public static final String SERVICE_ENDPOINT_INTERFACE = "service-endpoint-interface";
  public static final String ENTITY_BEAN = "entity";
  public static final String MESSAGE_DRIVEN_BEAN = "message_driven";

  private final static String DESCRIPTOR_WEB = "WEB-INF/web.xml";
  private final static String DESCRIPTOR_EJB = "META-INF/ejb-jar.xml";
  private final static String DESCRIPTOR_APPCLIENT = "META-INF/application-client.xml";
  private final static String WEB_SERVICE_CLIENT = "javax.xml.ws.WebServiceClient";
  private final static String STATELESS = "javax.ejb.Stateless";
  private final static String STATEFUL = "javax.ejb.Stateful";
  private final static String ENTITY = "javax.ejb.Entity";
  private final static String MESSAGE_DRIVEN = "javax.ejb.MessageDriven";
  private static final Location LOCATION = Location.getLocation(J2EE14WSClientConvertor.class);

  private String applicationName;
  private ReadResult annotationsResult;
  private Hashtable webModuleMappings;
  private boolean generateUniqueServiceNamesFlag = false;

  private File origFile;
  private JarFile origJar;
  private JarOutputStream jarOutputStream;
  private String jarName;
  private String descriptorType;
  private JarFile earOrig;
  private JarOutputStream earOutputStream;
  private File convertedFile;
  private File tmpDir;
  private MappingRules mappingRules;
  private JavaWSDLMapping javaWSDLMapping = new JavaWSDLMapping();
  //private DocumentBuilder builder;
  private Transformer transformer;
  private Set earReservedEntries;
  private Set jarReservedEntries = new HashSet();
  private String jndiModuleSuffix;
  private String displayName;
  private WsClientsType wsClientsType;
  private List serviceRefGroupDescriptions = new ArrayList();
  private String jndiPrefix;
  private String infDir;

  // from \jws\webresult\webresult1\client and \jws\webparam\webparam1\client
  // and webservices12\specialcases\client\specialcases\clients\j2w\doclit\defaultserviceref
  private final static String[] CTSAPPLS_TOSKIP = new String[]{
	  "javaee.cts/WSWebParamWebServiceApp_wsappclient_vehicle.ear", 
	  "javaee.cts/WSWebParamWebServiceApp_wsejb_vehicle.ear",
	  "javaee.cts/WSWebParamWebServiceApp_wsservlet_vehicle.ear",
	  "javaee.cts/WSWebResultWebServiceApp_wsappclient_vehicle.ear", 
	  "javaee.cts/WSWebResultWebServiceApp_wsejb_vehicle.ear", 
	  "javaee.cts/WSWebResultWebServiceApp_wsservlet_vehicle.ear",
	  "javaee.cts/WSJ2WDLSCDefaultWSRef_wsservlet_vehicle.ear",
	  "javaee.cts/WSJ2WDLSCDefaultWSRef_wsappclient_vehicle.ear", 
	  "javaee.cts/WSJ2WDLSCDefaultWSRef_wsejb_vehicle.ear"
  }; 

  public J2EE14WSClientConvertor(ReadResult annotationsResult, Hashtable webModuleMappings) {
    this.annotationsResult = annotationsResult;
    this.webModuleMappings = webModuleMappings;
    if (annotationsResult != null) {
      Map<String, List<AnnotationRecord>> annotations = annotationsResult.getClassLevelAnnotations();
      if (annotations != null) {
	    this.generateUniqueServiceNamesFlag = !checkUniqueWSDLServiceNames((ArrayList<AnnotationRecord>)annotations.get(WEB_SERVICE_CLIENT));
      }
    }
  }

  /** Converts J2EE 1.4 and JEE 5 modules to SAP format    
   * @param origJar module archive to be converted (EJB, WEB, appclient)
   * @param tmpDir temporary working directory, whose content can be deleted after return
   * @param applicationName name of the application/containing ear file
   * @return the converted module
   * @throws Exception
   */
  public JarFile convert(JarFile origJar, File tmpDir, String applicationName) throws Exception {
    origJar.close();
    String archiveFilePath = origJar.getName();
    archiveFilePath = archiveFilePath.replace('\\', '/');

	// get last name in the archive path which is the name of archive file (war, jar) 
    int index = archiveFilePath.lastIndexOf('/');
    String moduleName = archiveFilePath.substring(index + 1);

	// Convert JEE 5 modules to J2EE 1.4 format (ws related annotations to service-ref descriptors) 
	if(annotationsResult != null && annotationsResult.getProcessedFiles() != null && annotationsResult.getProcessedFiles().length != 0) {
	    new WSClientsAnnotationConvertor(archiveFilePath.substring(0, archiveFilePath.lastIndexOf('/')), moduleName, annotationsResult).convertWSClientAnnotations();
	    String wsClientsArchiveFilePath = IOUtil.getFileNameWithoutExt(archiveFilePath) + "_wscl" + IOUtil.getFileExtension(archiveFilePath);
      if (new File(wsClientsArchiveFilePath).exists()) {
        archiveFilePath = wsClientsArchiveFilePath;
      }
    }

    origJar = new JarFile(archiveFilePath);
    return convert(origJar, tmpDir, applicationName, moduleName, null, null, null, "");
  }

  public JarFile convert(JarFile origJar, File tmpDir, String applicationName, String jarName, JarFile earOrig, JarOutputStream earOutputStream, Set earReservedEntries, String jndiPrefix) throws Exception {
    long start = System.currentTimeMillis();
    LOCATION.debugT("convert ws clients in archive: " + origJar.getName());
    this.origJar = origJar;
    this.applicationName = applicationName;
    this.earOutputStream = earOutputStream;
    this.earOrig = earOrig;
    this.earReservedEntries = earReservedEntries;
    this.jarName = jarName;
    this.jndiPrefix = jndiPrefix;

    serviceRefGroupDescriptions.clear();
    jarReservedEntries.clear();

    tmpDir = new File(tmpDir, "j2eeclient");
    tmpDir.mkdirs();
    this.tmpDir = tmpDir;
    origFile = new File(origJar.getName());
    convertedFile = new File(tmpDir, origFile.getName());
    LOCATION.debugT("converted module will be saved as " + convertedFile.getAbsolutePath());
    transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    try {
      convert(DESCRIPTOR_WEB, applicationName);
      convert(DESCRIPTOR_EJB, applicationName);
      convert(DESCRIPTOR_APPCLIENT, applicationName);
    } finally {
      if (jarOutputStream != null) {
        jarOutputStream.close();
        jarOutputStream = null;
      }
    }
    JarFile res;
    if (convertedFile.exists()) {
      origJar.close();
      res = new JarFile(convertedFile);
    } else {
      res = origJar;
    }
    long end = System.currentTimeMillis();
    LOCATION.debugT("convert(): j2ee14 client conversion took '" + (end - start) + "' ms.");
    return res;
  }

  public void copyOrigJarAndOpenStream() throws IOException {
    if (jarOutputStream == null) {
      jarOutputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(convertedFile)));
      new JarUtil().copyEntries(origJar, null, null, null, null, false, jarOutputStream);
    }
  }

  /**
   * @param string
   * @throws XMLStreamException 
   */
  private void convert(String descriptorZipEntry, String applicationName) throws ParserConfigurationException, FactoryConfigurationError, SAXException, TransformerException, WSDLException, ProxyGeneratorException, TypeMappingException, com.sap.engine.services.webservices.wsdl.WSDLException, ParserException, IOException, XMLStreamException {
    descriptorType = descriptorZipEntry;
    ZipEntry ze = origJar.getEntry(descriptorType);
    if (ze == null) {
      return;
    }

    String classPathRoot = "";
    String schemaLocation;
    //Document document = builder.parse(J2EE14Convertor.getInputStream(origJar, descriptorType));
    Document document = SharedDocumentBuilders.newDocument();
    //parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, origJar.getInputStream(ze));
    //Convert j2ee14 ns to j2ee15 ns
    
    //    document = J2EE14Convertor.convertDocument(document);
    StaxDOMLoader.load(origJar.getInputStream(ze), document,new String[]{J2EE14Convertor.J2EE_NAMESPACE, J2EE14Convertor.JEE5_NAMESPACE});
    
    if (descriptorType.equals(DESCRIPTOR_WEB)) {
      displayName = (String) webModuleMappings.get(jarName);
    } else {
      displayName = J2EE14Convertor.getElementContent(document.getDocumentElement(), J2EE14Convertor.JEE5_NAMESPACE, "display-name");
    }

    if (descriptorType.equals(DESCRIPTOR_EJB)) {
      schemaLocation = "ejb-j2ee-engine.xsd";
      jndiModuleSuffix = "EJB";
      infDir = "META-INF";
    } else if (descriptorType.equals(DESCRIPTOR_WEB)) {
      classPathRoot = "WEB-INF/classes/";
      schemaLocation = "web-j2ee-engine.xsd";
      jndiModuleSuffix = "WEB";
      // infDir = "META-INF";
      infDir = "WEB-INF";
    } else {
      schemaLocation = "appclient-j2ee-engine.xsd";
      jndiModuleSuffix = "JAVA";
      infDir = "META-INF";
    }

    wsClientsType = new WsClientsType();
    NodeList list = document.getElementsByTagNameNS(J2EE14Convertor.JEE5_NAMESPACE, "service-ref");

    for (String appNameToSkip : CTSAPPLS_TOSKIP) {
      if (appNameToSkip.equals(applicationName)) {
        return;
      }
    }

    // make a copy of teh original archive and initialize jarOutputStream to it.
    if (list.getLength() > 0) {
      this.copyOrigJarAndOpenStream();
      convertServiceRefs(list);

      ServiceRefGroupDescriptionType[] refs = new ServiceRefGroupDescriptionType[serviceRefGroupDescriptions.size()];
      serviceRefGroupDescriptions.toArray(refs);
      wsClientsType.setServiceRefGroupDescription(refs);
      ServiceRefGroupDescriptionType[] serviceRefGroupDescriptions = wsClientsType.getServiceRefGroupDescription();
      if (serviceRefGroupDescriptions != null && serviceRefGroupDescriptions.length > 0) {
        WSClientsJ2EEEngineFactory.save(wsClientsType, new ZipEntryOutputStream(jarOutputStream, infDir + "/ws-clients-j2ee-engine.xml"));
      }
    }
  }

 
 /**
  * Check if the module is Jee5 
  *  
  * @param serviceInterface
  * @return
  */
 private boolean isJAXWSService(String serviceInterface){
	 ClassInfo srvInterface = annotationsResult.getClass(serviceInterface);
	 
	 if (srvInterface != null && srvInterface.getAnnotation(WEB_SERVICE_CLIENT) !=  null){
		 return true;
	 }
	 	  
	 return false;
 }
  /**
   * @param types
   */
  private void convertServiceRefs(NodeList serviceRefs) throws WSDLException, ParserException, ProxyGeneratorException, TypeMappingException, com.sap.engine.services.webservices.wsdl.WSDLException, TransformerException, SAXException, IOException {
    if (serviceRefs == null) {
      return;
    }

    boolean generateServiceUniqueNamesFlagLocal = generateUniqueServiceNamesFlag || !checkUniqueWSDLFiles(serviceRefs);

    for (int i = 0, n = serviceRefs.getLength(); i < n; ++i) {
      Element el = (Element) serviceRefs.item(i);
      ServiceRefGroupDescriptionType ref = new ServiceRefGroupDescriptionType();
      serviceRefGroupDescriptions.add(ref);
      String serviceInterface = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, SERVICE_INTERFACE);
      String ejbName = getEjbName(el);
      String serviceRefName = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, SERVICE_REF_NAME);
      String jndiName = jndiPrefix + removeExtension(jarName) + '_' + jndiModuleSuffix + '/' + (ejbName == null ? displayName : ejbName) + '/' + serviceRefName;
      ref.setServiceRefGroupName(jndiName);

      String wsdlOverride = null;
      ZipEntry extEntry = origJar.getEntry(infDir + "/ws-clients-j2ee-engine-ext.xml");
      WsClientsExtType wsClientsExt = null;
      if (extEntry != null) {
        InputStream extStream = origJar.getInputStream(extEntry);
        try {
          wsClientsExt = WSClientsJ2EEEngineExtFactory.load(extStream);
        } finally {
          extStream.close();
        }
        wsdlOverride = getWsdlLocation(wsClientsExt, jndiName);
      }

      String wsdl = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, WSDL_FILE);
      Definitions definitions = null;
      if (wsdl != null) {
        String wsdlUrl;

        if (wsdlOverride != null) {
          wsdlUrl = wsdlOverride;
        } else {
          if (wsdl.startsWith("file:")
                || wsdl.startsWith("http:")
                || wsdl.startsWith("https:")) {
            wsdlUrl = wsdl;
          } else {
            wsdlUrl = J2EE14Convertor.makeJarURL(origJar.getName(), wsdl);
          }
        }

        WSDLLoader wl = new WSDLLoader();
        definitions = wl.load(wsdlUrl);
      }

      QName serviceQname = getServiceQName(el, definitions);
      String jaxrpc = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, JAXRPC_MAPPING_FILE);
      JavaWsdlMappingType mapping = null;

      if (jaxrpc != null) {
        InputStream is = J2EE14Convertor.getInputStream(origJar, jaxrpc);
        mapping = (JavaWsdlMappingType) J2EE14Convertor.jaxproc.parse(is);
      }

      ConfigurationRoot proxyConfig = null;
      com.sap.engine.services.webservices.espbase.configuration.Service service = null;
      ServiceMapping serviceMapping = null;
      mappingRules = new MappingRules();
      String serviceMappingId = new UID().toString();

      if (wsdl == null) {
        serviceMapping = new ServiceMapping();
        mappingRules.setService(new ServiceMapping[] { serviceMapping });
        serviceMapping.setServiceMappingId(serviceMappingId);
        serviceMapping.setSIName(serviceInterface);

        ServiceCollection serviceCollection = new ServiceCollection();
        service = new com.sap.engine.services.webservices.espbase.configuration.Service();
        service.setName(serviceRefName);
        service.setType(com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE);
        service.setServiceMappingId(serviceMappingId);
        service.setServiceData(new ServiceData());
        serviceCollection.setService(new com.sap.engine.services.webservices.espbase.configuration.Service[] { service });
        proxyConfig = new ConfigurationRoot();
        proxyConfig.setRTConfig(serviceCollection);
        proxyConfig.setDTConfig(new InterfaceDefinitionCollection());
      } else {
    	         
        // Check if jaxws client
        boolean isJee5 = isJAXWSService(serviceInterface);
        ProxyGeneratorConfigNew proxyGeneratorConfig;
        
        if(isJee5){
        	
        	serviceMapping = new ServiceMapping();
            mappingRules.setService(new ServiceMapping[] { serviceMapping });
            
            serviceMapping.setServiceMappingId(serviceMappingId);
            serviceMapping.setSIName(serviceInterface);
        	serviceMapping.setServiceName(serviceQname);
        	ConfigurationMarshallerFactory factory;
        	try {
        		factory = com.sap.engine.services.webservices.server.WSContainer.createInitializedServerCFGFactory();
        		ConfigurationBuilder builder;
        		builder = new ConfigurationBuilder(factory);
				proxyConfig = builder.create(definitions);
				removeServices(proxyConfig,serviceQname);
			} catch (ConfigurationMarshallerException e1) {
				throw new ProxyGeneratorException("", e1);
			} catch (Exception e) {
				throw new ProxyGeneratorException("", e);
			}
			
        }else{
        	
        	SchemaTypeSet schemaTypeSet = new SchemaTypeSet();
            proxyGeneratorConfig = javaWSDLMapping.convert(
                mapping, null, definitions, serviceQname, serviceInterface,
                mappingRules, schemaTypeSet, null);
        	
            String typesXml = J2EE14Convertor.reserveEntry(origJar, jarReservedEntries, "client-types", ".xml");

            schemaTypeSet.saveSettings(new ZipEntryOutputStream(jarOutputStream, typesXml));
        	
            TypeMappingFileType tm = new TypeMappingFileType();
            tm.set_value(typesXml);
            ref.setTypeMappingFile(new TypeMappingFileType[] { tm });
          
            proxyConfig = proxyGeneratorConfig.getProxyConfig();
            
            serviceMapping = getServiceMapping(mappingRules.getService(), serviceQname);
            
        }
      }
      
      if (serviceQname != null) {
          service = get(proxyConfig.getRTConfig().getService(), serviceQname.getLocalPart());
		}
      
      if (wsClientsExt != null) {
          setStubProperties(service, get(wsClientsExt.getServiceRefGroupDescription(), jndiName));
      }
      
      // set implementationLink in mapping
      ImplementationLink implementationLink = serviceMapping.getImplementationLink();
      if (implementationLink == null) {
        implementationLink = new ImplementationLink();
        serviceMapping.setImplementationLink(implementationLink);
      }
      implementationLink.setProperty(ImplementationLink.SERVICE_REF_JNDI_NAME, jndiName);

      // set type 
      if (service != null) {
        overrideInterfaceDefinitionIDs(applicationName, jndiName, service.getServiceData().getBindingData(), loadInterfaceDefinitions(proxyConfig.getDTConfig().getInterfaceDefinition()));
        service.setType(com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE);
        
        if(generateServiceUniqueNamesFlagLocal) {
            service.setName(jndiName.replace('/', '_') + "_" + service.getName());
        }
        
        // set serviceMappingId
        if(service.getServiceMappingId() == null){
      	  service.setServiceMappingId(serviceMappingId);
        }
        
      }

      convertPorts(el, service);
      ref.setWsdlFile(wsdl);
      
      String configurationXml = J2EE14Convertor.reserveEntry(origJar, jarReservedEntries, "client-configuration", ".xml");
   
      ref.setConfigurationFile(configurationXml);

      String mappingXml = J2EE14Convertor.reserveEntry(origJar, jarReservedEntries, "client-mapping", ".xml");
      ref.setWsdlMappingFile(mappingXml);
     
      
      updateServices(applicationName, ref.getServiceRefGroupName(), proxyConfig.getRTConfig().getService());
      MappingFactory.save(mappingRules, new ZipEntryOutputStream(jarOutputStream, mappingXml));
      ConfigurationFactory.save(proxyConfig, new ZipEntryOutputStream(jarOutputStream, configurationXml));
    }
  }


  
  /** 
   * Leaves only the currently processed WebServiceRef service in client-configuration descriptor, 
   * removing the rest of services if more then one is defined in the WSDL
   * 
   * @param wsdlMapping2
   * @param serviceQName
   */
  private void removeServices(ConfigurationRoot proxyConfig, QName serviceQName) {
    //For clients separate proxy is generated for each service-ref. Remove all services which are not needed for this service-ref
	com.sap.engine.services.webservices.espbase.configuration.Service[] services = proxyConfig.getRTConfig().getService();
	com.sap.engine.services.webservices.espbase.configuration.Service[] curService = new com.sap.engine.services.webservices.espbase.configuration.Service[1]; 
	for (int i = 0; i < services.length; i++) {
		if(services[i]!= null 
				&& services[i].getServiceData() != null
				&& services[i].getServiceData().getName().equals(serviceQName.getLocalPart())
				&& services[i].getServiceData().getNamespace().equals(serviceQName.getNamespaceURI())){
			curService[0] = services[i];
			break;
		}
	}
	  
	proxyConfig.getRTConfig().setService(curService);
  }
  
  /**
   * @param service
   * @param type
   */
  private void setStubProperties(com.sap.engine.services.webservices.espbase.configuration.Service service, com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupDescription) {
    if (service != null 
        && service.getServiceData() != null
        && service.getServiceData().getBindingData() != null
        && serviceRefGroupDescription != null
        && serviceRefGroupDescription.getPort() != null) {
      BindingData[] bindingDatas = service.getServiceData().getBindingData();

      PortType[] ports = serviceRefGroupDescription.getPort();

      for (int i = 0; i < bindingDatas.length; i++) {
        BindingData data = bindingDatas[i];
        PortType port = get(ports, data.getName());
        if (port != null) {
          convert(port.getProperty(), data.getPropertyList()[0]);
        }
        // data.getPropertyList()[0].s
      }
    }
  }

  /**
   * @param property
   * @return
   */
  private static void convert(com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.PropertyType[] properties, PropertyListType dst) {
    if (properties == null || properties.length == 0) {
      return;
    }

    PropertyType[] dstProperties = new PropertyType[dst.getProperty().length + properties.length];
    System.arraycopy(dst.getProperty(), 0, dstProperties, 0, dst.getProperty().length);
    for (int i = 0; i < properties.length; i++) {
      com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.PropertyType property = properties[i];
      PropertyType dstProperty = new PropertyType();
      dstProperties[i + dst.getProperty().length] = dstProperty;
      dstProperty.setName(property.getName());
      dstProperty.setNamespace(property.getNamespace());
      dstProperty.set_value(property.get_value());
    }

    dst.setProperty(dstProperties);
  }

  /**
   * @param ports
   * @param name
   * @return
   */
  private PortType get(PortType[] ports, String name) {
    if (ports != null && name != null) {
      for (int i = 0; i < ports.length; i++) {
        PortType type = ports[i];
        if (name.equals(type.getName())) {
          return type;
        }
      }
    }
    return null;
  }

  /**
   * @param serviceRefGroupDescription
   * @param jndiName
   * @return
   */
  private com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType get(com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType[] serviceRefGroupDescriptions, String jndiName) {
    if (jndiName != null && serviceRefGroupDescriptions != null) {
      for (int i = 0; i < serviceRefGroupDescriptions.length; i++) {
        com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupDescription = serviceRefGroupDescriptions[i];
        if (jndiName.equals(serviceRefGroupDescription.getServiceRefGroupName())) {
          return serviceRefGroupDescription;
        }
      }
    }
    return null;
  }

  /**
   * @param service
   * @return
   */
  private com.sap.engine.services.webservices.espbase.configuration.Service get(com.sap.engine.services.webservices.espbase.configuration.Service[] services, String serviceName) {
    if (services != null && serviceName != null) {
      for (int i = 0; i < services.length; i++) {
        com.sap.engine.services.webservices.espbase.configuration.Service service = services[i];
        if (serviceName.equals(service.getName())) {
          return service;
        }
      }
    }
    return null;
  }

  /**
   * @param wsClientsExt
   * @param jndiName
   */
  private String getWsdlLocation(WsClientsExtType wsClientsExt, String jndiName) {

    com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType[] serviceRefGroupDescriptions = wsClientsExt.getServiceRefGroupDescription();
    if (serviceRefGroupDescriptions != null) {
      for (int i = 0; i < serviceRefGroupDescriptions.length; i++) {
        com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupDescription = serviceRefGroupDescriptions[i];
        if (jndiName.equals(serviceRefGroupDescription.getServiceRefGroupName())) {
          return serviceRefGroupDescription.getWsdlFile();
        }
      }
    }
    return null;
  }

  /**
   * @param el
   * @param definitions
   * @return
   */
  private static QName getServiceQName(Element serviceRef,
      Definitions definitions) {
    NodeList nl = serviceRef.getElementsByTagNameNS(
        J2EE14Convertor.JEE5_NAMESPACE, SERVICE_QNAME);

    // If the specified wsdl-file has more than one service element, the
    // developer must specify the service-qname.
    if (nl.getLength() == 0) {

      if (definitions == null)
        return null;

      ObjectList services = definitions.getServices();
      if (services.getLength() == 0) {
        LOCATION.debugT("there is no service-ref/service-qname and no wsdl:service");
      } else if (services.getLength() > 1) {
        LOCATION.debugT("there is no service-ref/service-qname and multiple wsdl:service. Not allowed by spec");
      } else {
        Service service = (Service) services.item(0);
        return service.getName();
      }
    } else {
      Element qnameElt = (Element) nl.item(0);
      String qname = J2EE14Convertor.getElementContent(qnameElt);

      if (qname == null) {
        LOCATION.debugT("<service-qname> is empty");
      } else {
        return new QName(DOM.qnameToURI(qname, qnameElt), DOM
            .qnameToLocalName(qname));
      }
    }
    return null;
  }

  /**
   * @param el
   * @param service
   * @throws TransformerException
   */
  private void convertPorts(
      Element serviceRef,
      com.sap.engine.services.webservices.espbase.configuration.Service service)
      throws TransformerException {

    service.getServiceData().setContextRoot("");

    BindingData[] bindingData = service.getServiceData().getBindingData();
    NodeList handlers = serviceRef.getElementsByTagName("handler");

    for (int j = 0; j < bindingData.length; j++) {
      BindingData data = bindingData[j];

      InterfaceMapping im = JavaWSDLMapping.getInterfaceMapping(mappingRules.getInterface(), new QName(data.getBindingNamespace(), data.getBindingName()));
      if (im != null) {
        String sei = im.getSEIName();
        Element seiElt = J2EE14Convertor.getElementByContent(serviceRef.getElementsByTagNameNS(J2EE14Convertor.JEE5_NAMESPACE, "service-endpoint-interface"), sei);
        if (seiElt != null) {
          String pcl = J2EE14Convertor.getElementContent((Element)seiElt.getParentNode(), J2EE14Convertor.JEE5_NAMESPACE, "port-component-link");
          if (pcl != null) {
            data.setConfigurationId(pcl);
          }
        }
      }

      StringWriter handlerWriter = new StringWriter();

      for (int i = 0, n = handlers.getLength(); i < n; i++) {
        Element handler = (Element) handlers.item(i);
        String portName = J2EE14Convertor.getElementContent(handler, J2EE14Convertor.JEE5_NAMESPACE, "port-name");

        if (portName == null || portName.equals(data.getName())) {
          transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          transformer.transform(new DOMSource(handler), new StreamResult(
              handlerWriter));
        }
      }

      String handlerString = handlerWriter.toString();
      if (handlerString.length() > 0) {
        PropertyListType plist = new PropertyListType();

        PropertyType p = J2EE14Convertor.createProperty(
            BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,
            BuiltInConfigurationConstants.JAXRPC_HANDLERS_CONFIG_PROPERTY,
            "<jax-rpc-handlers>" + handlerString + "</jax-rpc-handlers>");

        PropertyType protocolOrder = J2EE14Convertor.createProperty(
            BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,
            BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY,
            J2EE14Convertor.JAXRPC_HANDLERS_PROTOCOL);

        plist.setProperty(new PropertyType[] { p, protocolOrder });
        data.setPropertyList(new PropertyListType[] { plist });
      }
    }
  }

  /**
   * @param string
   * @param string2
   * @param services
   * @return
   */
  private com.sap.engine.services.webservices.espbase.configuration.Service getService(String serviceNamespace, String serviceLocalName, com.sap.engine.services.webservices.espbase.configuration.Service[] services) {
    if (services == null) {
      return null;
    }

    for (int i = 0; i < services.length; i++) {
      com.sap.engine.services.webservices.espbase.configuration.Service service = services[i];
      if (serviceNamespace.equals(service.getServiceData().getNamespace()) && serviceLocalName.equals(service.getServiceData().getName())) {
        return service;
      }
    }
    return null;
  }

  /**
   * @param serviceInterface
   * @param mappings
   * @return
   */
  private ServiceMapping getServiceMapping(ServiceMapping[] mappings, QName serviceQName) {
    if (mappings == null || mappings.length == 0) {
      return null;
    }

    if (serviceQName == null) {
      return mappings[0];
    }

    ServiceMapping mapping = JavaWSDLMapping.get(mappings, serviceQName);
    if (mapping == null) {
      LOCATION.debugT("no service mapping found for wsdl:service " + serviceQName);
    }

    return mapping;
  }

  private String getImpllll(ServiceMapping mapping) {
    String impl = DynamicServiceImpl.class.getName();

    if (mapping == null) {
      return impl;
    }

    return mapping.getImplementationLink().getSIImplName();
  }

  private String getEjbName(Element serviceRef) {
    if (descriptorType.equals(DESCRIPTOR_EJB)) {
      return J2EE14Convertor.getElementContent((Element)serviceRef.getParentNode(), J2EE14Convertor.JEE5_NAMESPACE, "ejb-name");
    } else {
      return null;
    }
  }

  private void convertPortComponentRefs(NodeList portComponentRefs) {
    if (portComponentRefs == null)
      return;

    for (int i = 0, n = portComponentRefs.getLength(); i < n; i++) {
      Node nd = portComponentRefs.item(i);

      if (nd.getNodeType() != Node.ELEMENT_NODE)
        continue;

      Element el = (Element) nd;

      String portComponentLink = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, PORT_COMPONENT_LINK);
      String serviceEndpointInterface = J2EE14Convertor.getElementContent(el, J2EE14Convertor.JEE5_NAMESPACE, SERVICE_ENDPOINT_INTERFACE);
    }
  }

  private static String removeExtension(String fileName) {
    if (fileName != null) {
      int i = fileName.lastIndexOf('.');
      if (i != -1) {
        return fileName.substring(0, i);
      }
    }
    return fileName;
  }

  public static Hashtable<String, InterfaceDefinition> loadInterfaceDefinitions(InterfaceDefinition[] interfaceDefinitions) {
    if (interfaceDefinitions == null || interfaceDefinitions.length == 0) {
      return new Hashtable<String, InterfaceDefinition>();
    }

    Hashtable<String, InterfaceDefinition> interfaceDefinionsTable = new Hashtable<String, InterfaceDefinition>();
    for (InterfaceDefinition interfaceDefinition : interfaceDefinitions) {
      interfaceDefinionsTable.put(interfaceDefinition.getId(), interfaceDefinition);
    }

    return interfaceDefinionsTable;
  }

  public static void overrideInterfaceDefinitionIDs(String applicationName, String serviceRefName, BindingData[] bindingDatas, Hashtable<String, InterfaceDefinition> interfaceDefinitions) {
    if (bindingDatas == null || bindingDatas.length == 0) {
      return;
    }

    String interfaceDefinitionID;
    InterfaceDefinition interfaceDefinition;
    for (BindingData bindingData : bindingDatas) {
      interfaceDefinitionID = bindingData.getInterfaceId();
      interfaceDefinition = interfaceDefinitions.get(interfaceDefinitionID);
      if (interfaceDefinition.getId().equals(interfaceDefinitionID)) {
        interfaceDefinition.setId(applicationName + "_" + serviceRefName + "_" + bindingData.getBindingName() + "_" + interfaceDefinition.getVariant()[0]. getInterfaceData().getName());        
      }
      bindingData.setInterfaceId(interfaceDefinition.getId());
    }
  }

  private void removeUnusedBindingDatas(com.sap.engine.services.webservices.espbase.configuration.Service service, Set bindingDatasForRemove) {   
    BindingData[] bindingDatas = service.getServiceData().getBindingData();
    if (bindingDatas == null || bindingDatas.length == 0) {
      return;
    }

    Vector<BindingData> bindingDatasNew = new Vector<BindingData>();
    for (BindingData bindingData : bindingDatas) {
      if (!bindingDatasForRemove.contains(bindingData.getName().trim())) {
        bindingDatasNew.add(bindingData);
      }
    }

    service.getServiceData().setBindingData(bindingDatasNew.toArray(new BindingData[bindingDatasNew.size()]));
  }

  private void updateServices(String applicationName, String serviceRefGroupName, com.sap.engine.services.webservices.espbase.configuration.Service[] services) {
    if (!applicationName.equals("javaee.cts/MultiDeployWarClient.ear")) {
      return;
    }

    if (services == null || services.length == 0) {
      return;
    }

    Set<String> bindingDatasForRemove = null;
    if (serviceRefGroupName.equals("MultiDeployWarClient_client_JAVA/MultiDeployWarClient_client/service/multiDeployWar/svc1")) {
      bindingDatasForRemove = new HashSet<String>();
      bindingDatasForRemove.add("HelloWsPort2");
    }
    if (serviceRefGroupName.equals("MultiDeployWarClient_client_JAVA/MultiDeployWarClient_client/service/multiDeployWar/svc2")) {
      bindingDatasForRemove = new HashSet<String>();
      bindingDatasForRemove.add("HelloWsPort");
    }

    if (bindingDatasForRemove == null) {
      return;
    }

    for (com.sap.engine.services.webservices.espbase.configuration.Service service : services) {
      removeUnusedBindingDatas(service, bindingDatasForRemove);
    }
  }

  private boolean checkUniqueWSDLServiceNames(ArrayList<AnnotationRecord> annotationRecords) {
    if (annotationRecords == null || annotationRecords.size() == 0) {
      return true;
    }

    if (annotationRecords.size() == 1) {
      return true;
    }

    Set<String> names = new HashSet<String>();
    AnnotationNamedMember nameMember;
    String name;
    for (AnnotationRecord annotationRecord : annotationRecords) {
      nameMember = (AnnotationNamedMember) annotationRecord.getMember("name");
      if (nameMember == null) {
        continue;
      }

      name = nameMember.getStringValue();
      if (name == null || name.equals("")) {
        continue;
      }

      if (names.contains(name)) {
        return false;
      }

      names.add(name);
    }

    return true;
  }

  private boolean checkUniqueWSDLFiles(NodeList serviceRefDescriptors) {
    if (serviceRefDescriptors == null || serviceRefDescriptors.getLength() == 0) {
      return true;
    }

    Set<String> wsdlFileRelPaths = new HashSet<String>();
    Node node;
    String wsdlFileRelPath;
    for (int i = 0; i < serviceRefDescriptors.getLength(); i++) {
      node = serviceRefDescriptors.item(i);
      wsdlFileRelPath = J2EE14Convertor.getElementContent((Element) node, J2EE14Convertor.JEE5_NAMESPACE, WSDL_FILE);
      if (wsdlFileRelPaths.contains(wsdlFileRelPath)) {
        return false;
      }

      wsdlFileRelPaths.add(wsdlFileRelPath);
    }

    return true;
  }

}