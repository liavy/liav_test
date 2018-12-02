/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.webservices.server.deploy.j2ee.ws;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.MarshalException;
import java.rmi.UnmarshalException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaWsdlMappingType;
import com.sap.engine.lib.descriptors5.webservices.EjbLinkType;
import com.sap.engine.lib.descriptors5.webservices.PortComponentType;
import com.sap.engine.lib.descriptors5.webservices.ServiceImplBeanType;
import com.sap.engine.lib.descriptors5.webservices.ServletLinkType;
import com.sap.engine.lib.descriptors5.webservices.WebserviceDescriptionType;
import com.sap.engine.lib.descriptors5.webservices.WebservicesType;
import com.sap.engine.lib.descriptors5.webservices.XsdQNameType;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.lib.processor.impl.JaxRpcMappingProcessor;
import com.sap.engine.lib.processor.impl.WebServicesProcessor5;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.util.legacy.StaxDOMLoader;
import com.sap.engine.services.deploy.container.rtgen.GenerationException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.URLSchemeType;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportBindingRT;
import com.sap.engine.services.webservices.espbase.configuration.cfg.SoapApplicationRegistry;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ConfigurationException;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.DefaultConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.sec.SecurityUtil;
import com.sap.engine.services.webservices.espbase.mappings.EJBImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.InterfaceDefinitionDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineExtFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.BindingDataType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebservicesExtType;
import com.sap.engine.services.webservices.server.deploy.jee5.AnnotationConstants;
import com.sap.engine.services.webservices.server.deploy.jee5.AnnotationParser;
import com.sap.engine.services.webservices.server.deploy.jee5.AnnotationProcessor;
import com.sap.engine.services.webservices.server.deploy.jee5.WSAnnotationConvertor;
import com.sap.engine.services.webservices.server.deploy.migration.ws.MigrationConstants;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.AnnotationRecord.NamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationRecordImpl;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.MethodInfo;
import com.sap.lib.javalang.element.impl.ClassInfoImpl;
import com.sap.lib.javalang.file.FileInfo;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Boyan Slavov
 */

public class J2EE14Convertor {
	
  private static final String WEBSERVICES_J2EE_ENGINE_EXT_NAMESJPACE = "http://www.sap.com/webas/710/ws/webservices-j2ee-engine-ext-descriptor";
  private static final String WEBSERVICES_J2EE_ENGINE_EXT_XML = "META-INF/webservices-j2ee-engine-ext.xml";
  static final String JAXRPC_HANDLERS_PROTOCOL = "JAXRPCHandlersProtocol"; 
 
  private String applicationName;
  private ReadResult annotationsResult; 
  private String contextRoot;
  private Document webXml;
  private JarFile moduleJarOriginal;
  private JarOutputStream jarOutputStream;
  private File moduleFileConverted, moduleFileOriginal;
  private Set reservedEntries = new HashSet();
  private File tmpRoot;
  public static WebServicesProcessor5 wsproc;
  public static JaxRpcMappingProcessor jaxproc;
  
  static {    	  
	wsproc = (WebServicesProcessor5) SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEBSERVICES5);
	wsproc.switchOffValidation();  
    
    jaxproc = (JaxRpcMappingProcessor) SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.JAXRPCMAPPING);
    jaxproc.switchOffValidation();
  }  

  /** Maps portType/interface QName to configuration interface*/
  private ArrayList interfaces = new ArrayList();

  static final String META_INF = "META-INF";
  static final String WEB_INF = "WEB-INF";
  static final String WEBSERVICES_XML = "webservices.xml";
  static final String WEB_XML = "web.xml";
  static final String EJB_JAR_XML = "ejb-jar.xml";
  static final String WEBSERVICES_J2EE_ENGINE = "webservices-j2ee-engine.xml";

  public ArrayList inputFiles = new ArrayList(), outputFiles = new ArrayList(); //todo: remove after debug

  public static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
  public static final String JEE5_NAMESPACE = "http://java.sun.com/xml/ns/javaee";
  
  private Transformer transformer;
  private Document webservicesXmlDoc;
  private JavaWSDLMapping javaWSDLMapping = new JavaWSDLMapping();

  private String jarName;
  private String webserviceName;
  private WebservicesExtType webservicesExtDoc;
  private Hashtable webModuleMappings;

  private static final Location LOCATION = Location.getLocation(J2EE14Convertor.class);
  private ArrayList interfaceMappings = new ArrayList();
   
  private Hashtable<QName, Hashtable<String, AnnotationRecord>> wsAnnotationsPerService; 
  private ArrayList<com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType> serviceExtDescriptorsNew = new ArrayList<com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType>();
  
  public J2EE14Convertor() {
	  
  }
  
  public J2EE14Convertor(String applicationName, ReadResult annotationsResult, Hashtable webModuleMappings) {
    this();
    this.applicationName = applicationName;
    this.annotationsResult = annotationsResult; 
    this.webModuleMappings = webModuleMappings;
  }
  
  public JarFile convertJ2EEModule(JarFile archiveFile, String webServicesTempDir) throws WSDLException, TypeMappingException, ProxyGeneratorException, ParserException, ParserConfigurationException, FactoryConfigurationError, SAXException, TransformerFactoryConfigurationError, TransformerException, IOException, GenerationException, ConfigurationException, XMLStreamException{    
    wsAnnotationsPerService = null; 
    
	String jarName = new File(archiveFile.getName()).getName();
    String contextRoot = (String) webModuleMappings.get(jarName);
   
    if(annotationsResult != null) {
      archiveFile.close();
      String path = archiveFile.getName();
      String strForReplace = File.separator+jarName;
      String tmpPath = path.replace(strForReplace, "");
      convertAnnotations(tmpPath, jarName, annotationsResult);     
      
      if(wsAnnotationsPerService != null && wsAnnotationsPerService.size() != 0) {
        //containsWebServicesDescriptor(tmpPath, archiveFile.getName());
        archiveFile = new JarFile(IOUtil.getFileNameWithoutExt(archiveFile.getName()) + "_ws" + IOUtil.getFileExtension(archiveFile.getName()));
      } else {
        archiveFile = new JarFile(archiveFile.getName());  
      }    
    }
   
    return convertJ2EEModule(archiveFile, webServicesTempDir, contextRoot, jarName);
  }
  
  /** Convert J2EE 1.4 webservices archive to SAP New York format 
   * @param archiveFile J2EE Module archive (WEB or EJB-JAR)
   * @param webServicesTempDir directory in which to unpack and modify the contents
   * @return
   * @throws UnmarshalException
   * @throws WSDLException
   * @throws TypeMappingException
   * @throws ProxyGeneratorException
   * @throws IOException
   * @throws ParserException
   * @throws XMLStreamException 
   */
  public JarFile convertJ2EEModule(JarFile archiveFile, String webServicesTempDir, String contextRoot, String jarName) throws WSDLException, TypeMappingException, ProxyGeneratorException, ParserException, ParserConfigurationException, FactoryConfigurationError, SAXException, TransformerFactoryConfigurationError, TransformerException, IOException, ConfigurationException, XMLStreamException {
	LOCATION.debugT("convert webservices in archive: " + archiveFile.getName());
    
	if (null != archiveFile.getJarEntry("META-INF/" + WEBSERVICES_J2EE_ENGINE)) {
	  LOCATION.debugT("META-INF/" + WEBSERVICES_J2EE_ENGINE + " exists. Conversion skipped.");
	  return archiveFile;
	} 
	
	if (archiveFile.getJarEntry("META-INF/webservices.xml") == null && archiveFile.getJarEntry("WEB-INF/webservices.xml") == null) {
      LOCATION.debugT("META-INF/webservices.xml does not exist. Conversion skipped.");           
      return archiveFile;
    } 
	
  interfaces.clear();
  interfaceMappings.clear();
  reservedEntries.clear();
    
	transformer = TransformerFactory.newInstance().newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  
	try {
      moduleJarOriginal = archiveFile;
      this.contextRoot = contextRoot;
      this.jarName = jarName;
      moduleFileOriginal = new File(archiveFile.getName());

      tmpRoot = new File(webServicesTempDir, "j2ee");
      tmpRoot.mkdirs();
      webServicesTempDir = tmpRoot.getPath();
      
      webXml = null;
      String webservicesXmlEntry = META_INF + "/" + WEBSERVICES_XML;
      InputStream is = getInputStream(moduleJarOriginal, webservicesXmlEntry);

      if (is == null) {
        webservicesXmlEntry = WEB_INF + "/" + WEBSERVICES_XML;
        is = getInputStream(moduleJarOriginal, webservicesXmlEntry);
        ZipEntry zeWebXml = moduleJarOriginal.getEntry(WEB_INF + "/web.xml");
        if (zeWebXml != null) {          
          webXml = SharedDocumentBuilders.newDocument();
          
          String jarWebURI = makeJarURL(moduleFileOriginal.getPath(), WEB_INF + "/web.xml");
          
          URL webXMLURL = new URL(jarWebURI);
          
          //Convert j2ee14 ns to j2ee15 ns
          StaxDOMLoader.load(webXMLURL.openStream(), webXml,new String[]{J2EE_NAMESPACE, JEE5_NAMESPACE});                             
        }
      }

      if (is != null) {
        moduleFileConverted = new File(tmpRoot, jarName);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
        jarOutputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(moduleFileConverted)));
        if(wsAnnotationsPerService != null && wsAnnotationsPerService.size() != 0) {
          JarUtil.copyEntries(moduleJarOriginal, null, new String[]{WEBSERVICES_J2EE_ENGINE_EXT_XML}, null, null, false, jarOutputStream);	
        } else {
          JarUtil.copyEntries(moduleJarOriginal, null, new String[]{/*"WEB-INF/web.xml"*/}, null, null, false, jarOutputStream);
        }  
       
        WebServicesProcessor5 wsproc  = (WebServicesProcessor5) SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEBSERVICES5);
        wsproc.switchOffValidation();
        WebservicesType ws = (WebservicesType)wsproc.parse(is);
        com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType sapws = convert(ws, tmpRoot, reservedEntries, moduleJarOriginal);
         
        
        webservicesExtDoc = loadWebservicesExt();        
        if(webservicesExtDoc == null && wsAnnotationsPerService != null && wsAnnotationsPerService.size() != 0) {
          webservicesExtDoc = new WebservicesExtType();	         
        }
        
        webservicesXmlDoc = SharedDocumentBuilders.newDocument();               
        // Convert j2ee14 ns to j2ee15 ns        
        String webservicesXMLLocation = makeJarURL(moduleFileOriginal.getAbsolutePath(), webservicesXmlEntry);
        URL webservicesXMLURL = new URL(webservicesXMLLocation);
        StaxDOMLoader.load(webservicesXMLURL.openStream(), webservicesXmlDoc,new String[]{J2EE_NAMESPACE, JEE5_NAMESPACE});                             
 
        ConfigurationRoot cr = new ConfigurationRoot();
        MappingRules wsdlMapping = new MappingRules();        
        convert(ws, cr, wsdlMapping, sapws, jarOutputStream);
        wsdlMapping.setInterface((InterfaceMapping[])interfaceMappings.toArray(new InterfaceMapping[interfaceMappings.size()]));
  
        SecurityUtil.addMissingSecurityProperties(cr);       
        ConfigurationFactory.save(cr, new ZipEntryOutputStream(jarOutputStream, sapws.getConfigurationFile()));
        
        MappingFactory.save(wsdlMapping, new ZipEntryOutputStream(jarOutputStream, sapws.getWsdlMappingFile()));

        WebServicesJ2EEEngineFactory.save(sapws, new ZipEntryOutputStream(jarOutputStream, "META-INF/" + WEBSERVICES_J2EE_ENGINE));
        if(wsAnnotationsPerService != null && wsAnnotationsPerService.size() != 0) {
          if(serviceExtDescriptorsNew != null && serviceExtDescriptorsNew.size() != 0) {
            com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] serviceExtDescriptorsNewArr = serviceExtDescriptorsNew.toArray(new com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[serviceExtDescriptorsNew.size()]);
        	com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] serviceExtDescriptorsOld = webservicesExtDoc.getWebserviceDescription();
        	com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] serviceExtDescriptorsAll = null;
        	if(serviceExtDescriptorsOld == null || serviceExtDescriptorsOld.length == 0) {
        	  serviceExtDescriptorsAll = serviceExtDescriptorsNewArr;  
            } else {
              serviceExtDescriptorsAll = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[serviceExtDescriptorsNewArr.length + serviceExtDescriptorsOld.length];
              System.arraycopy(serviceExtDescriptorsOld, 0, serviceExtDescriptorsAll, 0, serviceExtDescriptorsOld.length);
              System.arraycopy(serviceExtDescriptorsNewArr,0, serviceExtDescriptorsAll, serviceExtDescriptorsOld.length, serviceExtDescriptorsNewArr.length);
            }
        	webservicesExtDoc.setWebserviceDescription(serviceExtDescriptorsAll);
          }
          
          WebServicesJ2EEEngineExtFactory.save(webservicesExtDoc, new ZipEntryOutputStream(jarOutputStream, WEBSERVICES_J2EE_ENGINE_EXT_XML));
        }	

        moduleJarOriginal.close();
        jarOutputStream.close();
        jarOutputStream = null;
        return new JarFile(moduleFileConverted);
      }else
//    	  moduleJarOriginal.close();
      return moduleJarOriginal;
    } finally {
      if (jarOutputStream != null) {
        jarOutputStream.close();
      }
    }   
    
  }

	/** Loads if exists webservices-j2ee-engine-ext.xml which contains additional non standard deployment information 
   * @return
	 * @throws IOException
	 * @throws TypeMappingException
	 * @throws IOException
	 * @throws SAXException
   */
  private WebservicesExtType loadWebservicesExt() throws TypeMappingException, IOException {
    InputStream inputStream = getInputStream(moduleJarOriginal, WEBSERVICES_J2EE_ENGINE_EXT_XML);
    
    if (inputStream != null) {
      try {
        WebservicesExtType webservicesExtType = WebServicesJ2EEEngineExtFactory.load(inputStream);
        return webservicesExtType;
      } finally {
        inputStream.close();
      }
    }
    return null;
  }

  public com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType convert(WebservicesType srcWs, File tmpDir, Set reservedEntries, ZipFile zipFile) throws TypeMappingException, IOException {

    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType dstWs = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType();

    String configName = J2EE14Convertor.reserveEntry(zipFile, reservedEntries, "configuration", ".xml");
    dstWs.setConfigurationFile(configName);

    String mappingName = J2EE14Convertor.reserveEntry(zipFile, reservedEntries, "mapping", ".xml");
    dstWs.setWsdlMappingFile(mappingName);

    dstWs.setWebserviceDescription(convert(srcWs.getWebserviceDescription()));

    return dstWs;
  }

  private static com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] convert(com.sap.engine.lib.descriptors5.webservices.WebserviceDescriptionType[] srcs) {
    if (srcs == null)
      return null;

    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] dsts =
      new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[srcs.length];

    for (int i = 0; i < dsts.length; i++) {
      com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType dst = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType();
      dsts[i] = dst;
     
      dst.setWebserviceName(srcs[i].getWebserviceDescriptionName().get_value());
      
      if (srcs[i].getWsdlFile() != null) {
        String wsdlLocation = srcs[i].getWsdlFile().get_value();
        if (wsdlLocation != null && wsdlLocation.length() > 0) {
          WsdlFileType wf = new WsdlFileType();
          WsdlType wsdl = new WsdlType();
          wsdl.set_value(wsdlLocation);
          wsdl.setStyle(WsdlStyleType.defaultTemp);
          wsdl.setType(WsdlTypeType.root);
          wf.setWsdl(new WsdlType[] { wsdl });
          dst.setWsdlFile(wf);
        }
      }
    }
    return dsts;
  }

  public static InputStream getInputStream(ZipFile archiveFile, String name) throws IOException {
    ZipEntry entry = archiveFile.getEntry(name);
    if (entry == null)
      return null;
    try {
      return archiveFile.getInputStream(entry);
    } catch (IOException e) {
      LOCATION.debugT("IOException while trying to getInputStream for ZipEntry '" + name + "' from zip '" + archiveFile.getName() + "'");
      throw e;
    }
  }

  private void convert(WebservicesType ws, ConfigurationRoot cr, MappingRules wsdlMapping, com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType sapws, ZipOutputStream os)
    throws WSDLException, ProxyGeneratorException, TypeMappingException, ParserException, TransformerException, SAXException, IOException, ConfigurationException {

    cr.setDTConfig(new InterfaceDefinitionCollection());
    cr.setRTConfig(new ServiceCollection());
    
    convert(ws.getWebserviceDescription(), cr, wsdlMapping, sapws, os);
  }

  /**
   * @param types
   * @param cr
   * @throws TypeMappingException
   * @throws ParserException
   * @throws TransformerException
   * @throws IOException
   * @throws SAXException
   */
  private void convert(WebserviceDescriptionType[] types, ConfigurationRoot cr, MappingRules wsdlMapping, com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType sapws, ZipOutputStream os) throws WSDLException, ProxyGeneratorException, TypeMappingException, ParserException, TransformerException, SAXException, IOException, ConfigurationException {
    if(types == null) {
      return;
    }  

    com.sap.engine.services.webservices.espbase.configuration.Service[] services = new com.sap.engine.services.webservices.espbase.configuration.Service[types.length];
    ArrayList interfaceDefinitionDescriptors = new ArrayList<InterfaceDefinitionDescriptionType>(); 
    
    for (int i = 0; i < types.length; i++) {
      WebserviceDescriptionType type = types[i];
      
      Definitions definitions = null;
      if (type.getWsdlFile() != null) {
        String wsdl = type.getWsdlFile().get_value();
        WSDLLoader wl = new WSDLLoader();
        LOCATION.debugT("Load wsdl " + wsdl);
        if(wsdl != null && wsdl.length() > 0)
          definitions = wl.load(makeJarURL(moduleFileOriginal.getPath(), wsdl));
      }
      
      SchemaTypeSet schemaTypeSet = new SchemaTypeSet();
      if(type.getJaxrpcMappingFile() != null) {
        String jaxrpc = type.getJaxrpcMappingFile()
        .get_value();
        InputStream is = getInputStream(moduleJarOriginal, jaxrpc);        
        if(is != null) {
    	  JavaWsdlMappingType mapping = (JavaWsdlMappingType) jaxproc.parse(is);
    	  if(definitions != null) {
    	    javaWSDLMapping.convert(mapping, types, definitions, null, null, wsdlMapping, schemaTypeSet, webXml);
    	  }  
        }
      }  
      com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType sapwsd = getWebservice(sapws.getWebserviceDescription(), type.getWebserviceDescriptionName().get_value());
     
      TypeMappingFileType typeMappingFile = new TypeMappingFileType();
      String typesXml = reserveEntry(moduleJarOriginal, reservedEntries, "types/types", ".xml");
      typeMappingFile.set_value(typesXml);
      sapwsd.setTypeMappingFile(new TypeMappingFileType[] { typeMappingFile });

      schemaTypeSet.saveSettings(new ZipEntryOutputStream(os, typesXml));

      com.sap.engine.services.webservices.espbase.configuration.Service dst = new com.sap.engine.services.webservices.espbase.configuration.Service();
      services[i] = dst;
      
      webserviceName = type.getWebserviceDescriptionName().get_value();
      dst.setName(webserviceName);
      dst.setType(com.sap.engine.services.webservices.espbase.configuration.Service.J2EE14_SERVICE_TYPE);
      
      ServiceData data = new ServiceData();
      dst.setServiceData(data);
                       
      if(definitions != null && definitions.getServices().getLength() == 1) {
        QName serviceQName = ((Service)definitions.getServices().item(0)).getName(); 
        data.setNamespace(serviceQName.getNamespaceURI());
        data.setName(serviceQName.getLocalPart());
      } else {           
	    PortComponentType[] portComponents = type.getPortComponent();                         
	    if(data.getName() == null && portComponents != null && portComponents.length != 0) {
	      XsdQNameType serviceQName = portComponents[0].getWsdlService();
	      if(serviceQName != null) {
	        data.setNamespace(serviceQName.get_value().getNamespaceURI().trim());
	        data.setName(serviceQName.get_value().getLocalPart().trim());
	      } 
	    }      
      }
      
      Hashtable<String, AnnotationRecord> serviceAnnotations = null; 
      if(wsAnnotationsPerService != null) {
        serviceAnnotations = wsAnnotationsPerService.get(new QName(data.getNamespace(), data.getName())); 
        if(serviceAnnotations == null) {
          serviceAnnotations = wsAnnotationsPerService.get(new QName(data.getNamespace(), webserviceName));     	
        }
      }
      
      int serviceType = -1;
      if(serviceAnnotations != null && serviceAnnotations.size() != 0) {
        serviceType = 3;
        com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor = getServiceExtDescriptor(webserviceName, webservicesExtDoc.getWebserviceDescription());
        if(serviceExtDescriptor == null) {
          serviceExtDescriptor = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType();
          serviceExtDescriptorsNew.add(serviceExtDescriptor);
          serviceExtDescriptor.setWebserviceName(webserviceName);
        }
        serviceExtDescriptor.setType(new Integer(3));       
        //dst.setType(3);
      }
      
      if(webXml == null) {
        data.setContextRoot(getContextRoot(webservicesExtDoc, webserviceName));       
      } else {
        data.setContextRoot(contextRoot);
      }
      if(definitions != null) {
        data.setBindingData(convertPortComponents(dst, type.getPortComponent(), definitions, wsdlMapping, interfaceDefinitionDescriptors, serviceAnnotations));        
      } else {    	     	    	    	    	
        PortComponentType[] ports = type.getPortComponent();
        BindingData[] bds = new BindingData[ports.length];
        AnnotationRecord wsAnnotation; 
        for(int p = 0; p < ports.length; p++) {
          wsAnnotation = null; 
          PortComponentType port = ports[p];
          String portComponentName = port.getPortComponentName().get_value();
          if(serviceType == 3) {
            wsAnnotation = serviceAnnotations.get(portComponentName);	  
          }
                    
          BindingData bd = new BindingData();
          bds[p] = bd;
          InterfaceDefinition intfDef = new InterfaceDefinition();
          intfDef.setName("jee-default_" + portComponentName);
          intfDef.setType(0); //to pass serialization
          interfaces.add(intfDef);
          Variant v = new Variant();
          v.setName("jee-default_" + portComponentName);
          InterfaceData iData = new InterfaceData();
          //iData.setName("jee-default_" + portComponentName); 
          v.setInterfaceData(iData);
          intfDef.setVariant(new Variant[]{v});
          InterfaceMapping intfMap = new InterfaceMapping();
          ImplementationLink implLinkNew = getImplementationLink(port, webXml, jarName);
          intfMap.setImplementationLink(implLinkNew);
          if(serviceType == 3 && wsAnnotation != null) {
            implLinkNew.setProperty("impl-class", wsAnnotation.getOwner().getName()); 	  
          }          
          interfaceMappings.add(intfMap);
          
          bd.setUrl(getBindingDataUrl(webXml, port.getServiceImplBean(), portComponentName, null));
          
          PropertyListType iDataPropertyList = new PropertyListType();
          PropertyListType bdPropertyList = new PropertyListType();          
                                        
          PropertyType handlersConfigProperty = convertHandler(portComponentName);                 
          if(handlersConfigProperty != null) {
            bdPropertyList.addProperty(handlersConfigProperty);            
            bdPropertyList.addProperty(createProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY, JAXRPC_HANDLERS_PROTOCOL));
          }
                                            
          PropertyType handlersConfigProperty2 = convertHandlerChains(webservicesXmlDoc, portComponentName, transformer);
          if(handlersConfigProperty2 != null) { 
        	iDataPropertyList.addProperty(createProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI(), BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart(), BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_HANDLERS_VALUE));          
            bdPropertyList.addProperty(handlersConfigProperty2);	  
          }                   
                    
          if(serviceType == 3 && wsAnnotation != null) {        	           
            NamedMember endpointInterfaceNameMember = wsAnnotation.getMember("endpointInterface"); 
            String endpointInterfaceName = null; 
            if(endpointInterfaceNameMember != null) {
              endpointInterfaceName = endpointInterfaceNameMember.getStringValue(); 
            }
            
            QName portTypeQName = null;             
            if(endpointInterfaceName != null && !endpointInterfaceName.equals("")) {                           
              portTypeQName = getPortTypeQName(annotationsResult.getClass(endpointInterfaceName).getAnnotation("javax.jws.WebService"));
            } else {            	
              portTypeQName = getPortTypeQName(wsAnnotation);             
            } 
            
            iData.setName(portTypeQName.getLocalPart());
            iData.setNamespace(portTypeQName.getNamespaceURI()); 
            bd.setBindingName(portTypeQName.getLocalPart() + "Binding"); 
            bd.setBindingNamespace(portTypeQName.getNamespaceURI());             
          }                    
           	            
          ServiceImplBeanType implLink = port.getServiceImplBean(); 
          if(serviceType == 3 && wsAnnotation != null && implLink.getEjbLink() != null) {
            AnnotationRecord statefulAnnotation = wsAnnotation.getOwner().getAnnotation("javax.ejb.Stateful");
            if(statefulAnnotation != null) {	  
        	  
//            BindingDataType bindingDataExt_ = getBindingDataExt(webserviceName, portComponentName, webservicesExtDoc);        
//            ImplementationLinkType implLinkExt = null; 
//            if(bindingDataExt_ != null) {
//             implLinkExt = bindingDataExt_.getImplementationLink();
//            }
//            com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.PropertyType sessionTypeProperty = null;
//            if(implLinkExt != null) {
//              sessionTypeProperty = getProperty(implLinkExt.getProperty(), EJBImplementationLink.EJB_SESSION_TYPE);                      
//            }                     
//            
//            if(sessionTypeProperty != null && sessionTypeProperty.get_value().equals(EJBImplementationLink.SESSION_STATEFUL)) {          
              iDataPropertyList.addProperty(createProperty(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_ENABLESESSION_PROP, "true"));              
              bdPropertyList.addProperty(createProperty(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_SESSIONMETHOD_PROP, DefaultConfigurationMarshaller.HTTPCOOKIES_VALUE)); 
          	}
          }
                            
          iData.setPropertyList(new PropertyListType[]{iDataPropertyList});
          bd.setPropertyList(new PropertyListType[]{bdPropertyList});
          
          bd.setVariantName(v.getName());
          bd.setName(port.getWsdlPort().get_value().getLocalPart());          
          bd.setConfigurationId(portComponentName);                                                
         
          bd.setUrlScheme(getUrlScheme(port)); //TODO: when is UrlScheme == https

          String imUid = new UID().toString();
          intfMap.setInterfaceMappingID(imUid);
          intfDef.setInterfaceMappingId(imUid);
          bd.setInterfaceMappingId(imUid);
          
          String defUid = applicationName + "_" + getModifiedModuleName(jarName) + "_" + portComponentName;
          intfDef.setId(defUid);
          bd.setInterfaceId(defUid);
          
          if(serviceType == 3 && wsAnnotation != null) {                       
            ClassInfo classInfo = (ClassInfo)wsAnnotation.getOwner();            
            AnnotationRecord srPublicationAnnotation = null;
            srPublicationAnnotation = classInfo.getAnnotation("com.sap.engine.services.webservices.annotations.SrPublication");                         
            if(srPublicationAnnotation != null) {
              NamedMember locationMember = srPublicationAnnotation.getMember("location");
              String location = null; 
              if(locationMember != null) {
                location = locationMember.getStringValue();	  
              }          
              if(location != null && !location.equals("")) {
                InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor = new InterfaceDefinitionDescriptionType();
                interfaceDefinitionDescriptor.setInterfaceDefinitionId(intfDef.getId());
                interfaceDefinitionDescriptor.setSrPublicationFile(location);
                interfaceDefinitionDescriptors.add(interfaceDefinitionDescriptor);
              } 
            }
              
            AnnotationRecord transportBindingAnnRecord = classInfo.getAnnotation(TransportBindingRT.class.getName());
            if(transportBindingAnnRecord != null) {
              NamedMember altPathMember = transportBindingAnnRecord.getMember("AltPath");
              if(altPathMember != null && !altPathMember.getStringValue().equals("")) {
                data.setContextRoot("");  	
              }
            }  	                                           
                          
            bd.setUrl(getBindingDataUrl(webXml, port.getServiceImplBean(), portComponentName, transportBindingAnnRecord));
            addProperties(wsAnnotation, iData, bd);
              
            NamedMember serviceInterfaceNameMember = wsAnnotation.getMember("endpointInterface"); 
            String serviceInterfaceName = null;  
            if(serviceInterfaceNameMember != null) {
          	  serviceInterfaceName = serviceInterfaceNameMember.getStringValue();
            }
              
           ClassInfo seiClassInfo = null; 
           if(serviceInterfaceName != null && !serviceInterfaceName.equals("")) {
             seiClassInfo = annotationsResult.getClass(serviceInterfaceName); 
           } 
              
           AnnotationParser annotationParser = new AnnotationParser((ClassInfo)wsAnnotation.getOwner(), seiClassInfo); 
           if((annotationParser.containsAnnotationsOR(AnnotationConstants.classLevelDTAnnotations_SAP) || annotationParser.containsAnnotationsORMethodLevel(AnnotationConstants.methodLevelDTAnnotations_SAP)) && !(annotationParser.containsAnnotationsOR(AnnotationConstants.classLevelRTAnnotations_SAP) || annotationParser.containsAnnotationsORMethodLevel(AnnotationConstants.methodLevelRTAnnotations_SAP))) {
              dst.setActive(-1);	  
           }
                            
           SoapApplicationRegistry.applySoapApplicationProperty(intfDef, false);           
          }    
        }
                
        data.setBindingData(bds);
      }          
    }
    
    cr.getDTConfig().setInterfaceDefinition((InterfaceDefinition[]) interfaces.toArray(new InterfaceDefinition[interfaces.size()]));
    cr.getRTConfig().setService(services);
    sapws.setInterfaceDefinitionDescription((InterfaceDefinitionDescriptionType[])interfaceDefinitionDescriptors.toArray(new InterfaceDefinitionDescriptionType[interfaceDefinitionDescriptors.size()])); 
  }
    
  /**
   * @param string
   * @param wsdl
   * @return
   */
  public static String makeJarURL(String jarPath, String entryName) {
    String res = "jar:file:" + jarPath + "!/" + entryName;
    res = res.replace('\\', '/');
    LOCATION.debugT(res);
    return res;
  }

  private BindingData[] convertPortComponents(com.sap.engine.services.webservices.espbase.configuration.Service service, PortComponentType[] types, Definitions definitions, MappingRules wsdlMapping, ArrayList<InterfaceDefinitionDescriptionType> interfaceDefinitionDescriptors, Hashtable<String, AnnotationRecord> serviceAnnotions) throws TypeMappingException, ParserException, TransformerException, IOException, ConfigurationException {    
    if(types == null) {
      return null;
    }  
    
    int serviceType = -1; 
    if(serviceAnnotions != null) {
      serviceType = 3; 	
    }    
   
    BindingData[] bds = new BindingData[types.length];
    AnnotationRecord wsAnnotation; 
    for(int i = 0; i < types.length; i++) {
      wsAnnotation = null;
      PortComponentType type = types[i];
      String portComponentName = type.getPortComponentName().get_value();
      if(serviceType == 3) {
        wsAnnotation = serviceAnnotions.get(portComponentName);
      }
      
      BindingData bd = new BindingData();
      bds[i] = bd;

      Endpoint ep = getPort(definitions.getServices(), type.getWsdlPort().get_value().getNamespaceURI(), type.getWsdlPort().get_value().getLocalPart());

      QName b = ep.getBinding();

      bd.setBindingName(b.getLocalPart());
      bd.setBindingNamespace(b.getNamespaceURI());
      bd.setName(type.getWsdlPort().get_value().getLocalPart());
      bd.setConfigurationId(portComponentName);                 
      bd.setUrl(getBindingDataUrl(webXml, type.getServiceImplBean(), portComponentName, null));
      bd.setUrlScheme(getUrlScheme(type)); //TODO: when is UrlScheme == https
     
      PropertyListType iDataPropertyList = new PropertyListType(); 
      PropertyListType bdPropertyList = new PropertyListType();          
      
      PropertyType handlersConfigProperty = convertHandler(portComponentName);                 
      if(handlersConfigProperty != null) {
        bdPropertyList.addProperty(handlersConfigProperty);            
        bdPropertyList.addProperty(createProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY, JAXRPC_HANDLERS_PROTOCOL));
      }
                                     
      PropertyType handlersConfigProperty2 = convertHandlerChains(webservicesXmlDoc, portComponentName, transformer);
      if(handlersConfigProperty2 != null) { 
    	iDataPropertyList.addProperty(createProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI(), BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart(), BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_HANDLERS_VALUE));          
        bdPropertyList.addProperty(handlersConfigProperty2);	  
      }            
      
      ServiceImplBeanType implLink = type.getServiceImplBean(); 
      if(serviceType == 3 && wsAnnotation != null && implLink.getEjbLink() != null) {
        AnnotationRecord statefulAnnotation = wsAnnotation.getOwner().getAnnotation("javax.ejb.Stateful");
        if(statefulAnnotation != null) {
//        BindingDataType bindingDataExt = getBindingDataExt(webserviceName, portComponentName, webservicesExtDoc);        
//        ImplementationLinkType implLinkExt = null; 
//        if(bindingDataExt != null) {
//         implLinkExt = bindingDataExt.getImplementationLink();
//        }          
//        com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.PropertyType sessionTypeProperty = null;
//        if(implLinkExt != null) {
//          sessionTypeProperty = getProperty(implLinkExt.getProperty(), EJBImplementationLink.EJB_SESSION_TYPE);                      
//        }
//                
//        if(sessionTypeProperty != null && sessionTypeProperty.get_value().equals(EJBImplementationLink.SESSION_STATEFUL)) {          
          iDataPropertyList.addProperty(createProperty(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_ENABLESESSION_PROP, "true"));
          bdPropertyList.addProperty(createProperty(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_SESSIONMETHOD_PROP, DefaultConfigurationMarshaller.HTTPCOOKIES_VALUE)); 
      	//}
        }
      }
      
      bd.setPropertyList(new PropertyListType[]{bdPropertyList});         
      InterfaceDefinition interfaceDefinition = getInterface(definitions, b, applicationName + "_" + getModifiedModuleName(jarName) + "_" + portComponentName);
      Variant variant = interfaceDefinition.getVariant()[0];            
      bd.setInterfaceId(interfaceDefinition.getId());      
      bd.setVariantName(variant.getName());
      variant.getInterfaceData().setPropertyList(new PropertyListType[]{iDataPropertyList});
      
      // set operations to bindingData:
      OperationData[] operationsIntfcDef = variant.getInterfaceData().getOperation();
      if(operationsIntfcDef != null && operationsIntfcDef.length > 0) {
	      OperationData[] operations = new OperationData[operationsIntfcDef.length];
	      for (int j = 0; j < operationsIntfcDef.length; j++) {
	    	  operations[j] = new OperationData();
	    	  operations[j].setName(operationsIntfcDef[j].getName());
	      }
	      bd.setOperation(operations);
      }
      
      if(serviceType == 3 && wsAnnotation != null) {
        ClassInfo classInfo = (ClassInfo)wsAnnotation.getOwner();
        //Map annotations = classInfo.getAnnotations(); 
        AnnotationRecord srPublicationAnnotation = null;
        //if(annotations != null && annotations.size() != 0) {
          srPublicationAnnotation = classInfo.getAnnotation("com.sap.engine.services.webservices.annotations.SrPublication");
        //}               
        if(srPublicationAnnotation != null) {
          NamedMember locationMember = srPublicationAnnotation.getMember("location");
          String location = null; 
          if(locationMember != null) {
            location = locationMember.getStringValue();	  
          }          
          if(location != null && !location.equals("")) {
            InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor = new InterfaceDefinitionDescriptionType();
            interfaceDefinitionDescriptor.setInterfaceDefinitionId(interfaceDefinition.getId());
            interfaceDefinitionDescriptor.setSrPublicationFile(location);
            interfaceDefinitionDescriptors.add(interfaceDefinitionDescriptor);
           }
         }
          
         AnnotationRecord transportBindingAnnRecord = classInfo.getAnnotation(TransportBindingRT.class.getName());
         if(transportBindingAnnRecord != null) {
           NamedMember altPathMember = transportBindingAnnRecord.getMember("AltPath");
           if(altPathMember != null && !altPathMember.getStringValue().equals("")) {
             service.getServiceData().setContextRoot("");  	
           }
         }  	                             
         bd.setUrl(getBindingDataUrl(webXml, type.getServiceImplBean(), portComponentName, transportBindingAnnRecord));
         addProperties(wsAnnotation, variant.getInterfaceData(), bd); 
          
         NamedMember serviceInterfaceNameMember = wsAnnotation.getMember("endpointInterface"); 
         String serviceInterfaceName = null;  
         if(serviceInterfaceNameMember != null) {
      	   serviceInterfaceName = serviceInterfaceNameMember.getStringValue();
         }
          
         ClassInfo seiClassInfo = null; 
         if(serviceInterfaceName != null && !serviceInterfaceName.equals("")) {
           seiClassInfo = annotationsResult.getClass(serviceInterfaceName); 
         } 
                    
         AnnotationParser annotationParser = new AnnotationParser((ClassInfo)wsAnnotation.getOwner(), seiClassInfo); 
         if((annotationParser.containsAnnotationsOR(AnnotationConstants.classLevelDTAnnotations_SAP) || annotationParser.containsAnnotationsORMethodLevel(AnnotationConstants.methodLevelDTAnnotations_SAP)) && !(annotationParser.containsAnnotationsOR(AnnotationConstants.classLevelRTAnnotations_SAP) || annotationParser.containsAnnotationsORMethodLevel(AnnotationConstants.methodLevelRTAnnotations_SAP))) {
           service.setActive(-1);	  
         }
          
         SoapApplicationRegistry.applySoapApplicationProperty(interfaceDefinition, false);        
      }    
                 
      InterfaceMapping intf = getInterface(wsdlMapping, type.getWsdlPort().get_value(), definitions);
      if(intf != null){
	    String uid = new UID().toString();
		intf.setInterfaceMappingID(uid);
		bd.setInterfaceMappingId(uid);
		interfaceDefinition.setInterfaceMappingId(uid);
		interfaceMappings.add(intf);		
		intf.setImplementationLink(getImplementationLink(type, webXml, jarName));
		intf.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING); //TODO: determine BINDING_TYPE from wsdl
		intf.setOusideInInterfaceFlag(true);
		//LOCATION.debugT(port);
      } else { //jaxws - add implementation link        
    	intf = new InterfaceMapping();  
    	String uid = new UID().toString();
		intf.setInterfaceMappingID(uid);
		bd.setInterfaceMappingId(uid);
		interfaceDefinition.setInterfaceMappingId(uid);
		interfaceMappings.add(intf);
		ImplementationLink implLinkNew = getImplementationLink(type, webXml, jarName); 
		intf.setImplementationLink(implLinkNew);	    
		if(wsAnnotation != null) {
		  implLinkNew.setProperty("impl-class", wsAnnotation.getOwner().getName()); 	
		}
      }    
    }

    return bds;
  } 

  /**
   * @param type
   * @return
   */
  private URLSchemeType getUrlScheme(PortComponentType type) {
    ServletLinkType servletLink = type.getServiceImplBean().getServletLink();
    if (servletLink == null) {
      //ejb
      String portComponentName = type.getPortComponentName().get_value();
      BindingDataType bindingData = getBindingDataExt(webserviceName, portComponentName, webservicesExtDoc);
      if (bindingData != null) {
        String transportGuarantee = bindingData.getTransportGuarantee();
        if ("INTEGRAL".equals(transportGuarantee)
            ||"CONFIDENTIAL".equals(transportGuarantee)) {
          return URLSchemeType.https;
        }
      }
      return URLSchemeType.http;
    } else {
      //servlet
      if (webXml != null) {
        String servletName = servletLink.get_value();
       
        String servletPattern = servletName;//assign some default
        NodeList urlPatterns = webXml.getElementsByTagNameNS(JEE5_NAMESPACE, "url-pattern");
        for(int i=0, n = urlPatterns.getLength(); i < n; ++i) {
          Element urlPattern = (Element)urlPatterns.item(i);
          Node servletMapping = urlPattern.getParentNode();
          if (JEE5_NAMESPACE.equals(servletMapping.getNamespaceURI())
              && "servlet-mapping".equals(servletMapping.getLocalName())) {
            servletPattern = getElementContent(urlPattern);
            break;
          }
        }
        
        //check web-resource-collections whether http is allowed
        boolean webResourceCollectionFound = false;
        NodeList collections = webXml.getElementsByTagNameNS(JEE5_NAMESPACE, "web-resource-collection");
        for(int i = 0, n = collections.getLength(); i < n; ++i) {
          Element collection = (Element) collections.item(i);
          
          NodeList httpMethods = collection.getElementsByTagNameNS(JEE5_NAMESPACE, "http-method");
          if (0 == httpMethods.getLength()
              || null != getElementByContent(httpMethods, "POST")) {
            // collection applies to post
            
            urlPatterns = collection.getElementsByTagNameNS(JEE5_NAMESPACE, "url-pattern");
            for(int urlPatternCounter = 0, urlPatternCount = urlPatterns.getLength();
              urlPatternCounter < urlPatternCount;
              ++urlPatternCounter) {

              Element urlPattern = (Element) urlPatterns.item(urlPatternCounter);
              String pattern = getElementContent(urlPattern);
              if (matchPattern(pattern, servletPattern)) {
                NodeList userDataConstraints = ((Element)collection.getParentNode()).getElementsByTagNameNS(JEE5_NAMESPACE, "user-data-constraint");
                webResourceCollectionFound = true;
                if (userDataConstraints.getLength()==0) {
                  // we found a resource collection for this url-pattern and http method that allows both http and https
                  return URLSchemeType.http;
                } else {
                  Element userDataConstraint = (Element) userDataConstraints.item(0);
                  if (null != getElementByContent(userDataConstraint.getElementsByTagNameNS(JEE5_NAMESPACE, "transport-guarantee"), "NONE")) {
                    return URLSchemeType.http;
                  }
                }
              }
            } 
          }
        }
        if (webResourceCollectionFound) {
          // collection for this url and method found, but it did not allow http
          return URLSchemeType.https;
        } else {
          // no collection found -> all are allowed
          return URLSchemeType.http;
        }
      }
    }
    return URLSchemeType.https;
  }

  /** Determine whether pattern matches urlPath
   * @param pattern
   * @param servletPattern
   * @return
   */
  private boolean matchPattern(String pattern, String urlPath) {
    // TODO: implement real matching if required
    return pattern.equals(urlPath);
  }

  /**
   * @param handler
   * @return
   * @throws ParserException
   * @throws TypeMappingException
   * @throws MarshalException
   * @throws TransformerException
   */
  private PropertyType convertHandler(String portComponentName) throws MarshalException, TypeMappingException, ParserException, TransformerException {  
    if(webservicesXmlDoc == null) {
      return null;
    }  
    
    NodeList handlers = webservicesXmlDoc.getElementsByTagNameNS(JEE5_NAMESPACE, "handler");    
    if(handlers.getLength() == 0) {
      return null;
    }  
    
    StringWriter handlerWriter = new StringWriter();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    
    for (int i = 0, n = handlers.getLength(); i < n; i++) {
      Node handler = handlers.item(i);
      
      if (portComponentName.equals(getElementContent((Element) handler.getParentNode(), "port-component-name"))) {
        transformer.transform(new DOMSource(handler), new StreamResult(handlerWriter));
      }
    }
    
    String handlerString = handlerWriter.toString();
    
    if (handlerString.length() > 0) {
      return createProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.JAXRPC_HANDLERS_CONFIG_PROPERTY, "<jax-rpc-handlers>" + handlerString + "</jax-rpc-handlers>");
    } else {
      return null;
    }
  }
  
  private PropertyType convertHandlerChains(Document webServicesDescriptor, String portComponentName, Transformer transformer) throws TransformerException {    	    
    NodeList portComponentNodes = webServicesDescriptor.getElementsByTagNameNS(JEE5_NAMESPACE, "port-component");
    if(portComponentNodes == null || portComponentNodes.getLength() == 0) {
      return null; 
    } 	
    
    Node portComponentNode = null; 
    for(int i = 0; i < portComponentNodes.getLength(); i++) {
      portComponentNode = portComponentNodes.item(i);
      if(getElementContent((Element)portComponentNode, "port-component-name").equals(portComponentName)) {
        break;  
      }
    }
    
    if(portComponentNode == null) {
      return null;    
    }      
    
    NodeList handlerChainsNodes = ((Element)portComponentNode).getElementsByTagNameNS(JEE5_NAMESPACE, "handler-chains");
    if(handlerChainsNodes == null || handlerChainsNodes.getLength() == 0) {
      return null; 	
    }
   
    StringWriter writer = new StringWriter();
    StreamResult streamResult = new StreamResult(writer);
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(handlerChainsNodes.item(0)), streamResult);	   
                   
    return createProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.JAXWS_HANDLERS_CONFIG_PROPERTY, "<jax-rpc-handlers>" + writer.toString() + "</jax-rpc-handlers>"); 
  }
 
  /**
   * @param namespace
   * @param name
   * @param value
   * @return
   */
  static PropertyType createProperty(String namespace, String name, String value) {
    PropertyType p = new PropertyType();
    p.set_value(value);
    p.setName(name);
    p.setNamespace(namespace);
    return p;
  }

  /**
   * @param wsdlMapping
   * @param name
   * @param definitions
   * @return
   * @throws IOException
   */
  private InterfaceMapping getInterface(MappingRules wsdlMapping, QName wsdlPort, Definitions definitions) throws IOException {
    
    Endpoint endpoint = getPort(definitions.getServices(), wsdlPort.getNamespaceURI(), wsdlPort.getLocalPart());
		QName bindingName = endpoint.getBinding();
    Binding binding = definitions.getBinding(bindingName);

    InterfaceMapping orig = wsdlMapping.getInterface(binding.getInterface(), bindingName);
    InterfaceMapping im = null;
    if(orig != null){
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	new ObjectOutputStream(bos).writeObject(orig);
    
	    try {
	      im = (InterfaceMapping)new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
	    } catch (ClassNotFoundException e) {
	      // $JL-EXC$ 
	      // this will not happen, it was just serialized
	    }
    }
    return im; 
  }

  /**
   * @param webXml
   * @param type
   * @return
   */
  private String getBindingDataUrl(Document webXml, ServiceImplBeanType type, String portComponentName, AnnotationRecord transportBindingRTAnnRecord) {
    String res = null;

    if (webXml == null) { // EJB based ws
      res = getBindingDataUrl(webservicesExtDoc, webserviceName, portComponentName, transportBindingRTAnnRecord);
    } else {
      ServletLinkType servletLink = type.getServletLink();
      
      if (servletLink == null) {
        LOCATION.debugT("No <servlet-link> found");
      }
      
      String servletName = servletLink.get_value();

			if (servletName == null) {
				LOCATION.debugT("empty <servlet-link/>");
			} else {
        NodeList list = webXml.getElementsByTagNameNS(JEE5_NAMESPACE, "servlet-mapping");
        
        Element servletMapping = null;
        for (int i = 0, n = list.getLength(); i < n; ++i) {
          Element currentServletMapping = (Element) list.item(i);
          if (servletName.equals(getElementContent(currentServletMapping, "servlet-name"))) {
            servletMapping = currentServletMapping;
            break;
          }
        }
        
        if (servletMapping == null) {
          servletMapping = webXml.createElementNS(JEE5_NAMESPACE, "servlet-mapping");
          Node n = servletMapping.appendChild(webXml.createElementNS(JEE5_NAMESPACE, "servlet-name"));
          n.appendChild(webXml.createTextNode(servletName));
          
          n = servletMapping.appendChild(webXml.createElementNS(JEE5_NAMESPACE, "url-pattern"));
          res = '/' + contextRoot + '/' + portComponentName;
          n.appendChild(webXml.createTextNode(res));
        } else {
          res = getElementContent(servletMapping, "url-pattern");   
        }
      }
    }
    return res;
  }

  static String getContextRoot(WebservicesExtType webservicesExtDoc, String webserviceName) {
    if (webservicesExtDoc == null)
      return webserviceName;
    
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] webserviceDescriptions = webservicesExtDoc.getWebserviceDescription();
    for (int i = 0; i < webserviceDescriptions.length; i++) {
      com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType webserviceDescription = webserviceDescriptions[i];
      if (webserviceName.equals(webserviceDescription.getWebserviceName())) {
        String contextRoot = webserviceDescription.getContextRoot();
        if (contextRoot == null) {
          return webserviceName;
        } else {
          return contextRoot;
        }
      }
    }
    return webserviceName;
  }

  private static String getBindingDataUrl(WebservicesExtType webservicesExtDoc, String webserviceName, String portComponentName, AnnotationRecord tranpsortBindingRTAnnRecord) {
    BindingDataType bindingData = getBindingDataExt(webserviceName, portComponentName, webservicesExtDoc);    
    if(bindingData == null || bindingData.getUrl() == null) {
      String url = null; 
      if(tranpsortBindingRTAnnRecord != null) {
        NamedMember altPathMember = tranpsortBindingRTAnnRecord.getMember("AltPath");
        if(altPathMember != null) {
          url = altPathMember.getStringValue();  	
        }
      }
      if(url == null || url.equals("")) {
        url = '/' + portComponentName; 	  
      }
      return url;
    } else {
      return '/' + bindingData.getUrl();
    }
  }

  /**
   * @param definitions
   * @param b
   * @return
   */
  private InterfaceDefinition getInterface(Definitions definitions, QName bindingName, String id) {
    Binding binding = definitions.getBinding(bindingName);

    QName interfaceName = binding.getInterface();

    InterfaceDefinition intDefinition = null;//(InterfaceDefinition) interfaces.get(interfaceName);
    if (intDefinition == null) {
      intDefinition = WSDLConverter.convert(definitions.getInterface(interfaceName));
      intDefinition.setId(id);
      interfaces.add(intDefinition);
    }

    return intDefinition;
  }

  /**
   * @param list
   * @param string
   * @param string2
   */
  private Endpoint getPort(ObjectList services, String ns, String portName) {
    if (services == null)
      return null;

    for (int i = 0, n = services.getLength(); i < n; ++i) {
      Endpoint ep = getPort((Service) services.item(i), ns, portName);
      if (ep != null)
        return ep;
    }
    return null;
  }

  /**
   * @param service
   */
  private Endpoint getPort(Service service, String ns, String portName) {

    if (service == null || portName == null)
      return null;

    if (ns == null && service.getName().getNamespaceURI() != null)
      return null;

    if (ns != null && !ns.equals(service.getName().getNamespaceURI()))
      return null;

    ObjectList list = service.getEndpoints();

    for (int i = 0, n = list.getLength(); i < n; ++i) {
      Endpoint ep = (Endpoint) list.item(i);
      if (portName.equals(ep.getName()))
        return ep;
    }
    return null;
  }

  /**
   * @param sapws
   * @param wsdname
   * @return
   */
  private static com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType getWebservice(
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] sapws,
    String wsdname) {
    if (sapws == null || wsdname == null)
      return null;

    for (int i = 0; i < sapws.length; i++) {
      com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType type = sapws[i];
      if (wsdname.equals(type.getWebserviceName()))
        return type;
    }
    return null;
  }

  /**
   * @param string
   * @param string2
   * @param meta
   * @return
   */
  static File createTempFile(String prefix, String suffix, File parentDir) throws IOException {
    File res = new File(parentDir, prefix + suffix);
    if (res.exists())
      return File.createTempFile(prefix, suffix, parentDir);
    else
      return res;
  }

  /**
   * @param string
   * @param string2
   * @param rootTmp
   * @return
   */
  public static File createTempDir(String prefix, String suffix, File parentDir) throws IOException {
    // create short names to avoid problems with too long paths
    //File dir = createTempFile(prefix.substring(0,3), ""/*suffix*/, parentDir);
    File dir = createTempFile(prefix, suffix, parentDir);
    dir.delete();
    dir.mkdirs();

    return dir;
  }

  public static String reserveEntry(ZipFile zipFile, Set reservedEntries, String prefix, String suffix) {
    int n = 0;
    String name = prefix + suffix;
    for (; reservedEntries.contains(name) || zipFile.getEntry(name) != null; ++n) {
      name = prefix + n + suffix;
    }
    reservedEntries.add(name);
    return name;
  }

  /** Copies file into jarOutputStream. jarOutputStream is left unclosed for further copying
   * @param file
   * @param jarOutputStream
   * @param zipEntryName
   */
  public static final void copy(File f, JarOutputStream jarOutputStream, String zipEntryName) throws IOException {
		OutputStream os = new ZipEntryOutputStream(jarOutputStream, zipEntryName);
		copy(new FileInputStream(f), os);
  }

	/** Copies InputStream into OutputStream. Closes both streams
	 * @param file
	 * @param jarOutputStream
	 * @param zipEntryName
	 */
	public static final void copy(InputStream is, OutputStream os) throws IOException {
		try {
			IOUtil.copy(is, os);
		} finally {
			try {
				is.close();
			} finally {
				os.close();
			}
		}
	}


  /**
   * @param webXml
   * @param name
   */
  static Element getServlet(Document webXml, String name) {
  	NodeList list = webXml.getElementsByTagNameNS(JEE5_NAMESPACE, "servlet");
  
  	if (name == null)
  		return null;
  	
  	for(int i = 0, n = list.getLength(); i < n; ++i) {
  		Node node = list.item(i);
  		if (node.getNodeType() == Node.ELEMENT_NODE) {
  			Element el = (Element)node;
  			if (name.equals(J2EE14Convertor.getElementContent(el, "servlet-name"))) {
  				return el;				
  			}
  		}
  	}
  	return null;
  }

  
  /**
   * @param el
   * @param elementName
   * @return
   */
  static String getElementContent(Element el, String elementName) {
    return getElementContent(el, JEE5_NAMESPACE, elementName);
  }
    
  static String getElementContent(Element el, String namespace, String elementName) {
    NodeList list = el.getElementsByTagNameNS(namespace, elementName);
  
    if (list.getLength() > 1) {
      LOCATION.debugT("multiple elements found with name " + elementName);
    } else if (list.getLength() == 0) {
    	LOCATION.debugT("no element " + elementName + " found");
    	return null;
    }
  	return getElementContent((Element) list.item(0));
  }
  
  static String getElementContent(Element el) {
    for(Node nd = el.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
      if (nd.getNodeType() == Node.TEXT_NODE) {
        return nd.getNodeValue();
      }
    }
    LOCATION.debugT("element " + el.getTagName()+ " is empty");
    return null;
  }
  
  static Element getElementByContent(NodeList elements, String content) {
    if (content != null) {
      for (int i = 0, n = elements.getLength(); i < n; ++i) {
        Element el = (Element) elements.item(i);
        if (content.equals(getElementContent(el))) {
          return el;
        }
      }      
    }
    return null;
  }
  
  private static ImplementationLink getImplementationLink(PortComponentType type, Document webXml, String jarName) {
    ServiceImplBeanType sib = type.getServiceImplBean();
  
    if (sib != null) {
  
      String name = null;
      EjbLinkType el = sib.getEjbLink();
  
      if (el != null) {
        // ejb ws
        name = el.get_value();
        if (name != null) {
          ImplementationLink il = new ImplementationLink();
          il.setProperty(ImplementationLink.IMPLCONTAINER_ID, EJBImplementationLink.IMPLEMENTATION_CONTAINER_ID);
          il.setProperty(EJBImplementationLink.JEE_VERSION, EJBImplementationLink.J2EE14);
          il.setProperty(EJBImplementationLink.EJB_NAME, name);
          il.setProperty(EJBImplementationLink.JAR_NAME, jarName);
          il.setProperty(EJBImplementationLink.EJB_INTERFACE_TYPE, EJBImplementationLink.EJB_INTERFACE_SEI);
          il.setProperty(EJBImplementationLink.EJB_SESSION_TYPE, EJBImplementationLink.SESSION_STATELESS);
          //il.setProperty(EJBImplementationLink.EJB_JNDI_NAME, "asd");
          //il.setSIImplName("");
          // set application name from parameter
          //				il.setStubName();
          return il;
        }
      } else {
        // servlet ws
        ServletLinkType sl = sib.getServletLink();
        name = sl.get_value();
        //MigrationConstants.JAVACLASS_IMPLLINK
  
        Element servlet = J2EE14Convertor.getServlet(webXml, name);
        String si = J2EE14Convertor.getElementContent(servlet, "servlet-class");
  
        ImplementationLink il = new ImplementationLink();
        if(name != null){
        	il.setProperty(MigrationConstants.SERVLET_NAME, name);
        }
        il.setProperty(ImplementationLink.IMPLCONTAINER_ID, MigrationConstants.JAVACLASS_IMPLLINK);
        il.setProperty(MigrationConstants.JAVA_CLASS, si);
       
  
        Node servletClass = servlet.getElementsByTagNameNS(JEE5_NAMESPACE, "servlet-class").item(0);
        servletClass.getFirstChild().setNodeValue("SoapServlet");
        return il;
      }
    }
    return null;
  }

  /**
   * @param document
   * @return
   */
  public static Element getFirstElement(Document document) {
    NodeList childNodes = document.getChildNodes();
    for(int i = 0, n = childNodes.getLength(); i < n; ++i) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE)
        return (Element)node;
    }
    return null;
  }
  
  public void convertAnnotations(String tmpPath, String moduleRelativeFileUri, ReadResult parsedAnnotations) throws GenerationException{              
    FileInfo[] fileInfoes = parsedAnnotations.getProcessedFiles();
	if(fileInfoes == null || fileInfoes.length == 0) {
	  return; 	  
	}
	  
	  try{
            for (FileInfo file: ((com.sap.lib.javalang.file.FolderInfo) parsedAnnotations.getProcessedFiles()[0]).getFiles())
            {
                String archiveName = file.getName();
                if(!moduleRelativeFileUri.endsWith(".war")){
                    if(!moduleRelativeFileUri.equals(archiveName))
                        continue;
                }
                                       
                if(moduleRelativeFileUri.endsWith(".war")) {
                    
                    int extIndex   = moduleRelativeFileUri.lastIndexOf(".war");
                    String module  = moduleRelativeFileUri.substring(0, extIndex);
                    module         += ".extracted.war";
                    
                    if(!archiveName.equals(module)){
                        continue;
                    }
                }
                                
                Map annotationMap = file.getClassLevelAnnotations();            
                if(annotationMap.isEmpty())
                    continue;
                Vector ejbAnnotations = new Vector();
                String annotationType = "";
                if(annotationMap.get("javax.ejb.Stateless") != null){
                    buildAnnotations("javax.ejb.Stateless", annotationMap, ejbAnnotations);
                    annotationType = EJBImplementationLink.SESSION_STATELESS;
                } else if (annotationMap.get("javax.ejb.Stateful") != null){
                    buildAnnotations("javax.ejb.Stateful", annotationMap, ejbAnnotations);
                    annotationType = EJBImplementationLink.SESSION_STATEFUL;
                }                
            }
            WSAnnotationConvertor wsAnnotationConvertor = new WSAnnotationConvertor();
            wsAnnotationConvertor.convertAnnotations(parsedAnnotations,tmpPath, moduleRelativeFileUri);
            wsAnnotationsPerService = wsAnnotationConvertor.getWSAnnotations(); 
        } catch(Exception e){
          e.printStackTrace();
          throw new GenerationException("Error during web services annotations conversion", e);
        }              
    }
    
    private void buildAnnotations(String annotation, Map annotationMap, Vector ejbAnnotations){
        ArrayList<AnnotationRecordImpl> annotationRecordImplArr = (ArrayList)annotationMap.get(annotation);
        for(int i = 0; i < annotationRecordImplArr.size(); i++){
            ClassInfoImpl elementInfo = (ClassInfoImpl) annotationRecordImplArr.get(i).getOwner();
            String fullyQualifiedAnnClassName = elementInfo.getName();
            Hashtable webEjbAnnotations = getAnotations(annotationRecordImplArr.get(i),annotation, "name", fullyQualifiedAnnClassName);
            if(!webEjbAnnotations.isEmpty()){
                ejbAnnotations.addElement(webEjbAnnotations);
            }
        }   
    }
    
    private Hashtable getAnotations(AnnotationRecordImpl annRecord, String key, String annName, String fullyQualifiedAnnClassName) {
        Hashtable<String, String> annotationPerService = new Hashtable<String, String>();
        Map memberMap = annRecord.getNamedMembersMap();
        Set memberMapSet = memberMap.keySet(); 
        Iterator memberMapIterator = memberMapSet.iterator();
        while(memberMapIterator.hasNext()){
            //Vector annotations = new Vector();
            String keyMemberMap = (String) memberMapIterator.next();
            AnnotationNamedMember annotationValues = (AnnotationNamedMember)memberMap.get(keyMemberMap);
            if (!(annotationValues != null && !annotationValues.getStringValue().equals("")))
                continue;
                String name = annotationValues.getName();
                String value = annotationValues.getStringValue();
                
                if(!name.equals(annName))                
                    continue;
                if(value != null && !value.equals("")){
                    annotationPerService.put(fullyQualifiedAnnClassName, value);
                }
        }
        return annotationPerService;
    }
     
  private static com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType getServiceExtDescriptor(String serviceName, com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] serviceExtDescriptors) {
    if(serviceExtDescriptors == null || serviceExtDescriptors.length == 0) {
      return null;  	
    }	  
    
    for(com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor: serviceExtDescriptors) {
      if(serviceExtDescriptor.getWebserviceName().trim().equals(serviceName)) {
        return serviceExtDescriptor; 	  
      }	
    }
    
    return null;     
  }  
  
  private static BindingDataType getBindingDataExt(String serviceName, String bindingDataName, WebservicesExtType webservicesExtDoc) {
    if(webservicesExtDoc != null) {
      com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType[] webserviceDescriptions = webservicesExtDoc.getWebserviceDescription();
      for(int i = 0; i < webserviceDescriptions.length; i++) {
        com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType webserviceDescription = webserviceDescriptions[i];
        if (serviceName.equals(webserviceDescription.getWebserviceName())) {
          BindingDataType[] bindingDatas = webserviceDescription.getBindingData();
          for (int j = 0; j < bindingDatas.length; j++) {
            BindingDataType bindingData = bindingDatas[j];
            if(bindingDataName.equals(bindingData.getBindingDataName())) {
              return bindingData;
            }
          }
        }
      }
    }
    return null;
  }
  
  private QName getPortTypeQName(AnnotationRecord wsAnnotation) {      	
    NamedMember portTypeNameMember = wsAnnotation.getMember("name");	  
    String portTypeName = null;       
	if(portTypeNameMember != null) {
	  portTypeName = portTypeNameMember.getStringValue();	
	}	 	  	
	if(portTypeName == null || portTypeName.equals("")) {
	  portTypeName = getShortClassName(wsAnnotation.getOwner().getName()); 
	}
		    
	NamedMember portTypeTNSMember = wsAnnotation.getMember("targetNamespace");
	String portTypeTNS = null; 
	if(portTypeTNSMember != null) {
	  portTypeTNS = portTypeTNSMember.getStringValue(); 	
	}
	    
	if(portTypeTNS == null || portTypeTNS.equals("")) {      
	  portTypeTNS = WSAnnotationConvertor.getTargetNamespace(WSAnnotationConvertor.getPackage(wsAnnotation.getOwner().getName()));	
	}
    	
    return new QName(portTypeTNS, portTypeName);     
  }
  
  public String getModifiedModuleName(String moduleName) {	
    int cutIndex = moduleName.lastIndexOf(".");     	
    if(cutIndex == -1) {
      return moduleName; 	
    }
	
    String suffix = null; 
    if(moduleName.endsWith(".jar")) {
      suffix = "_EJB"; 
    }
    if(moduleName.endsWith(".war")) {
      suffix = "_WEB"; 	  
    }              
    
    return moduleName.substring(0, cutIndex) + suffix;    	         
  }
  
  private void addProperties(AnnotationRecord wsAnnotation, InterfaceData interfaceData, BindingData bindingData) {
    NamedMember serviceInterfaceNameMember = wsAnnotation.getMember("endpointInterface"); 
    String serviceInterfaceName = null;  
    if(serviceInterfaceNameMember != null) {
	  serviceInterfaceName = serviceInterfaceNameMember.getStringValue();
    }
    
    ClassInfo seInterface = null; 
    if(serviceInterfaceName != null && !serviceInterfaceName.equals("")) {
      seInterface = annotationsResult.getClass(serviceInterfaceName); 
    }    
    
    addProperties((ClassInfo)wsAnnotation.getOwner(), seInterface, interfaceData, bindingData);     	 
  }
  
  private void addProperties(ClassInfo implClass, ClassInfo seInterface, InterfaceData interfaceData, BindingData bindingData) {    
	addGlobalProperties(AnnotationConstants.getTypeLevelDTAnnotations(), implClass, interfaceData, null, true);
	addGlobalProperties(AnnotationConstants.getTypeLevelRTAnnotations(), implClass, null, bindingData, false);
	
	addPropertiesOperationLevel(implClass, seInterface, interfaceData, bindingData);              
  }  
  
  private void addGlobalProperties(String[] annotationNames, ClassInfo implClass, InterfaceData interfaceData, BindingData bindingData, boolean isDTMode) {
    Map<String, AnnotationRecord> annotations = implClass.getAnnotations();
    if(annotations == null || annotations.size() == 0) {
      return;  	    	
    }
    
    PropertyType[] properties = getProperties(annotationNames, annotations);
    if(properties == null || properties.length == 0) {
      return; 
    }
    
	PropertyListType[] propertyLists; 
	if(isDTMode) {
	  propertyLists = interfaceData.getPropertyList();	  
	} else {
	  propertyLists = bindingData.getPropertyList();	
	}
	
	if(propertyLists == null || propertyLists.length == 0) {
      PropertyListType propertyList = new PropertyListType();
	  propertyList.setProperty(properties);
	  if(isDTMode) {
	    interfaceData.setPropertyList(new PropertyListType[]{propertyList});
	  } else {
	    bindingData.setPropertyList(new PropertyListType[]{propertyList});	  
	  }
    } else {
      PropertyListType propertyList = propertyLists[0];
      propertyList.setProperty(unifyPropertyTypes(new PropertyType[][]{propertyList.getProperty(), properties}));
    }  
  }
  
  private void addPropertiesOperationLevel(ClassInfo implClass, ClassInfo seInterface, InterfaceData interfaceData, BindingData bindingData) {
    Hashtable<String, MethodInfo> wsMethodsInterface;  
	Hashtable<String, MethodInfo> wsMethods;  
	if(seInterface == null) {
	  wsMethods = AnnotationParser.getWSMethods(implClass); 
	  wsMethodsInterface = wsMethods;    	         
	} else {
	  wsMethodsInterface = AnnotationParser.getWSMethods(seInterface);
	  wsMethods = new Hashtable<String, MethodInfo>();
	  AnnotationParser.getMethodsClass(wsMethodsInterface.keySet(), implClass, wsMethods);
	}
	 
	if(wsMethodsInterface == null || wsMethodsInterface.size() == 0) {
	  return; 	
	}
	
	Hashtable<String, String> operationNames = AnnotationParser.getOperationNames(wsMethodsInterface);
		
	addPropertiesOperationLevel(AnnotationConstants.getMethodLevelDTAnnotations(), wsMethods, operationNames, interfaceData, null, true);
	addPropertiesOperationLevel(AnnotationConstants.getMethodLevelRTAnnotations(), wsMethods, operationNames, null, bindingData, false);
  } 
  
  private void addPropertiesOperationLevel(String[] annotationNames, Hashtable<String, MethodInfo> wsMethods, Hashtable<String, String> operationNames, InterfaceData interfaceData, BindingData bindingData, boolean isDTMode) {	 
    if(operationNames == null || operationNames.size() == 0) {
      return;  	
    } 
    
    ArrayList<OperationData> operationDatasNew = new ArrayList<OperationData>();    
    Enumeration enumer = operationNames.keys(); 
    String methodName;                       
    MethodInfo wsMethod;     
    String operationName;
    OperationData operationData;       
    while(enumer.hasMoreElements()) {    	    
      methodName = (String)enumer.nextElement();
      wsMethod = wsMethods.get(methodName);      
      operationName = operationNames.get(methodName);
      if(isDTMode) {
        operationData = interfaceData.getOperationData(operationName);	  
      } else {       
        operationData = bindingData.getOperationData(operationName);
      }       
      if(operationData == null) {
        operationData = new OperationData(); 
        operationData.setName(operationName);
        operationDatasNew.add(operationData); 
      }
      
      addPropertiesOperationLevel(annotationNames, wsMethod, operationData);      
    }   
    
    if(operationDatasNew == null || operationDatasNew.size() == 0) {
      return; 	
    }
        
    OperationData[] operationDatas; 
    if(isDTMode) {
      operationDatas = interfaceData.getOperation();
    } else {
      operationDatas = bindingData.getOperation();   	
    }    
    OperationData[] operationDatasNewArr = operationDatasNew.toArray(new OperationData[operationDatasNew.size()]); 
    
    OperationData[] operationDatasAll = new OperationData[operationDatas.length + operationDatasNewArr.length];
    System.arraycopy(operationDatas, 0, operationDatasAll, 0, operationDatas.length); 
    System.arraycopy(operationDatasNewArr, 0, operationDatasAll, operationDatas.length, operationDatasNewArr.length);
    if(isDTMode) {
      interfaceData.setOperation(operationDatasAll);
    } else {
      bindingData.setOperation(operationDatasAll);  	
    }          
  }  
  
  private void addPropertiesOperationLevel(String[] annotationNames, MethodInfo methodInfo, OperationData operationData) {
    Map<String, AnnotationRecord> annotations = methodInfo.getAnnotations();
	if(annotations == null || annotations.size() == 0) {
      return; 	
    }	  
    
    PropertyType[] properties = getProperties(annotationNames, annotations);
    if(properties == null || properties.length == 0) {
      return;  	
    }
    
    PropertyListType[] propertyLists = operationData.getPropertyList(); 
    if(propertyLists == null || propertyLists.length == 0) {
      PropertyListType propertyList = new PropertyListType(); 
      propertyList.setProperty(properties);
      operationData.setPropertyList(new PropertyListType[]{propertyList});      
    } else {
      PropertyListType propertyList = propertyLists[0];
      propertyList.setProperty(unifyPropertyTypes(new PropertyType[][]{propertyList.getProperty(), properties}));
    }            
  }
   
  private PropertyType[] getProperties(String[] annotationNames, Map<String, AnnotationRecord> annotations) {
    if(annotationNames == null || annotationNames.length == 0) {
      return new PropertyType[0]; 	
    }
    
    PropertyType[] properties = new PropertyType[0]; 
    AnnotationRecord annotation;  
    PropertyType[] currentProperties;     
    for(String annotationName: annotationNames) {    	
      annotation = annotations.get(annotationName);
      if(annotation != null) {
        currentProperties = AnnotationProcessor.getProperties(annotation);    
        properties = unifyPropertyTypes(new PropertyType[][]{properties, currentProperties});
      }
    }	  
    
    return properties;
  }	 
  
  private PropertyType[] unifyPropertyTypes(PropertyType[][] properties) {
    if(properties == null || properties.length == 0) {
      return new PropertyType[0];	
    }
    
    PropertyType[] propertiesAll = new PropertyType[0];
    PropertyType[] propertiesNew;
    for(PropertyType[] currentProperties: properties) {
      propertiesNew = new PropertyType[propertiesAll.length + currentProperties.length];
      System.arraycopy(propertiesAll, 0, propertiesNew, 0, propertiesAll.length);
      System.arraycopy(currentProperties, 0, propertiesNew, propertiesAll.length, currentProperties.length);
      propertiesAll = propertiesNew;  	
    }
       
    return propertiesAll;	      
  }
    
  public String getShortClassName(String className) {
    int cutIndex = className.lastIndexOf(".");
	if (cutIndex == -1) {
	  return className;
	}
	
	return className.substring(cutIndex + 1);
  }
  	  
}
