package com.sap.engine.services.sca.plugins.ws.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sap.engine.interfaces.sca.SCAResolver;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.spi.ArchiveManipulatorResult;
import com.sap.engine.interfaces.sca.spi.PluginException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginFrame;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants.WsarGeneratonMode;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.XSDTypeContainer;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineAltFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.ImplementationLinkType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.MetaDataType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.PortComponentType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.PropertyType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.QNameType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.preprocess.WebServicesAltSupportHandler;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.engine.services.webservices.wsdl.WSDLImportTool;
import com.sap.sdo.api.helper.SapXmlDocument;
import com.sap.sdo.api.helper.SapXmlHelper;
import com.sap.sdo.api.helper.SapXsdHelper;
import com.sap.sdo.api.types.schema.Schema;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.impl.HelperProvider;

/**
 * 
 * @author I044263
 *
 */
public class WSARGenerator {
  
  private final String SERVICENAME_COMPONENTNAME_DELIMITER = "@";
  private final String SERVICENAME_COMPONENTNAME_DOWNLOAD_DELIMITER = "_";
  
  private WsarGeneratonMode iMode;  
  private boolean iGenerate;
  private Map<String, URL> iServiceNameToUrl;
  private Map<String, Object> iServiceNameToHc;
  private Map<String, String> iServiceNameToID;
  
  /**
   * Ctor.
   */
  public WSARGenerator(WsarGeneratonMode aMode) {
    iMode = aMode;

    iGenerate = false;
    iServiceNameToUrl = new Hashtable<String, URL>(5);
    iServiceNameToHc = new Hashtable<String, Object>(5);
    iServiceNameToID = new Hashtable<String, String>(5);
  }
  
  public void generateWSAR(final ISCAAssembly aAssembly, final SCAResolver aResolver, final File aFile, List<ArchiveManipulatorResult> aResult) throws PluginException {
    try {
      // Get list of scdl files.
      List<URL> scdlURLs = aResolver.getResourcesByExtension(WebServicePluginConstants.SCA_COMPOSITE_EXTENSION);
      
      // Create ***-alt.xml if scdl artifacts exist.
      if (scdlURLs.size() > 0)
        this.createDescriptor(scdlURLs, aFile, aResolver);
                
      if (iGenerate) {
	String fileName = null; 
	switch (iMode) {
	case DII: fileName = WebServicePluginConstants.DII_WSAR_FILE_NAME; break;
	case WS: fileName = WebServicePluginConstants.WS_WSAR_FILE_NAME; break;
	}
	
	// Copy wsdl files.
	this.copyWSDLFiles(aFile);
	
	// Build .wsar file.
	fileName = String.valueOf(System.currentTimeMillis()) + fileName;
	this.createArchive(aFile, fileName);
	
	// Set wsdls for persisting.
	this.setPersistWSDLs();

	// Generate web module.
	WebServicesAltSupportHandler h = new WebServicesAltSupportHandler();
	WebInfo[] info = h.generateWebSupport(aFile.getPath(), new JarFile(aFile.getPath() + File.separator + fileName), null);

	// Fill result object.
	aResult.add(new ArchiveManipulatorResult(fileName, WebServicePluginConstants.WS_CONTAINER_NAME, "", ArchiveManipulatorResult.WS_MODULE_TYPE));
	for (int i = 0; i < info.length; ++ i)
	  aResult.add(new ArchiveManipulatorResult(new File(info[i].getWarModulePath()).getName(), "", info[i].getContextRoot(), ArchiveManipulatorResult.WEB_MODULE_TYPE));
      }
    } catch (Exception e) {
      throw new PluginException("Exception during .wsar generation.", e);
    } finally {
      // Clear generation flag.
      iGenerate = false;
      iServiceNameToHc.clear();
      iServiceNameToID.clear();
      iServiceNameToUrl.clear();
      
      try {
	// Delete copied wsdl files.
	this.deleteCopiedWsdlFiles(aFile);
      } catch (IOException ioe) {}
    }
  }
  
  private void createDescriptor(List<URL> aURLs, File aFile, SCAResolver aResolver) throws	ParserConfigurationException, 
  												SAXException, 
  												IOException, 
  												URISyntaxException, 
  												TypeMappingException, 
  												WSDLException, DOMException, RuntimeProcessException, TransformerFactoryConfigurationError, TransformerException  {        
    URL url = null;
    List<WebserviceDescriptionType> wsDescTypes = new LinkedList<WebserviceDescriptionType>();
    
    Iterator<URL> i = aURLs.iterator();        
    while (i.hasNext()) {
      url = i.next();
      
      // Create webservice description types for scdl.
      List<WebserviceDescriptionType> wsDescs = this.generateWSDescriptionTypes(url, aResolver);
      Iterator<WebserviceDescriptionType> iterator = wsDescs.iterator();
      while (iterator.hasNext()) {
	wsDescTypes.add(iterator.next());
      }            
    }
    
    if (iGenerate) {
	// Move elements from list to array;
	WebserviceDescriptionType[] wsDesc = new WebserviceDescriptionType[wsDescTypes.size()];
	for (int k = 0; k < wsDescTypes.size(); ++ k)
	  wsDesc[k] = wsDescTypes.get(k);        

	WebservicesType wsType = new WebservicesType();
	wsType.setWebserviceDescription(wsDesc);
	      
	// Create ***-atl.xml
	WebServicesJ2EEEngineAltFactory.save(wsType, 	aFile.getCanonicalPath() +	
	    						File.separator + 
	    						WebServicePluginConstants.META_INF_FOLDER_STR + 
	          					File.separator + 
	          					WebServicePluginConstants.SCA_ALT_FILE_NAME);	
    }                         
  }
  
  private  List<WebserviceDescriptionType> generateWSDescriptionTypes(URL aURL, SCAResolver aResolver) throws	ParserConfigurationException, 
  														SAXException, 
  														IOException, 
  														URISyntaxException, 
  														WSDLException, DOMException, RuntimeProcessException, TransformerFactoryConfigurationError, TransformerException {
    List<WebserviceDescriptionType> wsDescTypes = new LinkedList<WebserviceDescriptionType>();
    
    Document document = null;
    try {
      // Parse application.composite file.
      document = this.parseSCDL(aURL);
    } catch (IllegalArgumentException iae) {
      try {
	document = this.parseArchiveSCDL(aURL);
      } catch (Exception e) {
	throw new WSDLException("Error during parsing SCDL from archive " + aURL, e);
      }
    }

    Node composite = null;
    // Search for exposed webservices.
    if (document.getChildNodes().getLength() == 1)
      composite = document.getChildNodes().item(0);
    else 
      throw new UnsupportedOperationException("More than one composite described in single file " + aURL.toString() + " .");
    
    // Get all components in composite.
    List<Node> components = new LinkedList<Node>();
    NodeList compositeArtefacts = composite.getChildNodes();
    for (int i = 0; i < compositeArtefacts.getLength(); ++ i) {
      Node node = compositeArtefacts.item(i);
      String nodeName = node.getNodeName();
      if (nodeName.equals(WebServicePluginConstants.COMPOSITE_COMPONENT_TAG_NAME))
	components.add(node);
    }
    
    // Search for exposed services as a Web Services.
    List<Node> exposedWS = new LinkedList<Node>();
    for (int i = 0; i < components.size(); ++ i) {
      Node component = components.get(i);
      NodeList componentArtefacts = component.getChildNodes();      
      for (int j = 0; j < componentArtefacts.getLength(); ++ j) {
	Node node = componentArtefacts.item(j);
	String nodeName = node.getNodeName();
	if (nodeName.equals(WebServicePluginConstants.COMPOSITE_SERVICE_TAG_NAME))
	  // Check if it is exposed WS, has binding.ws.
	  if (this.isExposedWS(node))
	    exposedWS.add(node);
      }
    }

    if (exposedWS.size() > 0) {
      // Have to generate.
      iGenerate = true;
      
      // Generate WS Description type per service.
      Iterator<Node> iterator = exposedWS.iterator();
      while (iterator.hasNext())
	wsDescTypes.add(this.generateWSDescriptionType(iterator.next(), aResolver));      
    } else {
      // Nothing to generate.
      iGenerate = false;
    }

    return wsDescTypes;
  }
  
  private WebserviceDescriptionType generateWSDescriptionType(Node aNode, SCAResolver aResolver) throws WSDLException, URISyntaxException, DOMException, RuntimeProcessException, TransformerFactoryConfigurationError, TransformerException, IOException {
    WebserviceDescriptionType wsDescType = new WebserviceDescriptionType();
    String ejbName = "";
    String ejbJar = "";
    String ejbInterface = "";
    String owner = "";    
    
    // Find helperContextManagement attribute and setup HelperContext management.
    NamedNodeMap attr = aNode.getParentNode().getAttributes();
    Node hcManager = attr.getNamedItem(WebServicePluginConstants.HELPER_CONTEXT_MANAGEMENT_ATTR_NAME);
    if(hcManager == null)
      hcManager = attr.getNamedItemNS(WebServicePluginConstants.SAP_SCA_NS_10, WebServicePluginConstants.HELPER_CONTEXT_MANAGEMENT_ATTR_NAME);

    Node componentName = attr.getNamedItem(WebServicePluginConstants.COMPOSITE_NAME_ATTR_NAME);
    if (hcManager == null)
      owner = WebServicePluginConstants.HELPER_CONTEXT_IMPL_MANAGED;
    else
      owner = hcManager.getNodeValue();
    
    // Get implementation.ejb node.
    Node ejb = null;
    NodeList nodes = aNode.getParentNode().getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++ i) {
      Node n = nodes.item(i);
      if (n.getNodeName().equals(WebServicePluginConstants.COMPOSITE_IMPLEMENTATION_EJB_TAG_NAME)) {
	ejb = n;
      	break;
      }
    }

    if (ejb != null) {
      NamedNodeMap attrs = ejb.getAttributes();
      Node n = attrs.getNamedItem(WebServicePluginConstants.COMPOSITE_EJB_LINK_ATTR_NAME);
      String s = n.getNodeValue();
      String[] strArray = s.split("#");
      ejbJar = strArray[0];
      ejbName = strArray[1];
    }
    
    // Get interface.java or interface.wsdl node.
    Node interfaceNode = null;
    NodeList nl = aNode.getChildNodes();
    for (int i = 0; i < nl.getLength(); ++ i) {
      Node node = nl.item(i);
      String nodeName = node.getNodeName();
      if (nodeName.indexOf(':') != -1)
	nodeName = node.getLocalName();
      if (nodeName.equals(WebServicePluginConstants.COMPOSITE_INTERFACEWSDL_TAG_NAME) || 
	  nodeName.equals(WebServicePluginConstants.COMPOSITE_INTERFACEJAVA_TAG_NAME) ||
	  nodeName.equals(WebServicePluginConstants.COMPOSITE_INTERFACEDII_TAG_NAME)) {
	interfaceNode = node;
	break;
      }
    }
    
    if (interfaceNode == null)
      throw new WSDLException("Exposed WS name " + aNode.getNodeName() + " without interface specified.");
    
    // Have a interface.wsdl, interface.java, interface.dii.
    NamedNodeMap map = interfaceNode.getAttributes();
    Node wsdlNode = map.getNamedItem(WebServicePluginConstants.COMPOSITE_LOCATION_ATTR_NAME);
    if (wsdlNode == null){
      wsdlNode = map.getNamedItemNS(WebServicePluginConstants.SAP_SCA_NS_10, WebServicePluginConstants.COMPOSITE_LOCATION_ATTR_NAME);
      if(wsdlNode == null)
	throw new WSDLException("Missing 'location' attribute from " + interfaceNode.getNodeName() + " node.");    	
    }
      
    
    String wsdl = wsdlNode.getNodeValue();
    if(wsdl.contains("#"))
      wsdl = wsdl.replaceFirst("#", "!/");
    
    // In case of interface.dii, check for interface attribute.
    if (interfaceNode.getNodeName().endsWith(WebServicePluginConstants.COMPOSITE_INTERFACEDII_TAG_NAME)) {
      Node interfacez = map.getNamedItem(WebServicePluginConstants.COMPOSITE_INTERFACE_ATTR_NAME);
      if (interfacez == null)
	throw new WSDLException("Missing 'interfaceJava' attribute from " + interfaceNode.getNodeName() + " node.");
      
      ejbInterface = interfacez.getNodeValue();
    }
	              
    // Load wsdl artefacts from resolver and search for ours.
    List<URL> wsdls = aResolver.getResourcesByExtension(WebServicePluginConstants.WSDL_EXTENSION);
    
    URL wsdlURL = null;
    Iterator<URL> iterator = wsdls.iterator();
    while (iterator.hasNext()) {
	wsdlURL = iterator.next();
	if (wsdlURL.toString().endsWith(wsdl)) {
	  break;
	}
    }
    
    if (wsdlURL == null)
	throw new URISyntaxException("", "Can not find " + wsdl + " file in resources.");
    
    // Check if wsdl from archive this is.
    if (wsdlURL.toString().indexOf('!') == -1) {
      wsdl = new File(wsdlURL.toURI()).getName();      
    } else {
      wsdl = wsdlURL.toString().substring(wsdlURL.toString().lastIndexOf('/') + 1);
    }
    
    // Load wsdl definitions.
    WSDLLoader loader = new WSDLLoader();
    Definitions defs = loader.load(wsdlURL.toString());	
    
    // Add wsdl to list of copy wsdls.
    NamedNodeMap attributes = aNode.getAttributes();
    Node serviceName = attributes.getNamedItem(WebServicePluginConstants.COMPOSITE_NAME_ATTR_NAME);
    String expServiceName = serviceName.getNodeValue() + SERVICENAME_COMPONENTNAME_DELIMITER + componentName.getNodeValue();
    iServiceNameToUrl.put(expServiceName, wsdlURL);    
    
    // Build and save service HelperContext
    if (owner.equals(WebServicePluginConstants.HELPER_CONTEXT_CNT_MANAGED))
      iServiceNameToHc.put(expServiceName, this.buildHelperContext(defs));  
    
    // Map values.              
    if (defs.getServices().getLength() == 1) {
	// Set service wsdl file.
	wsDescType.setWsdlFile(	WebServicePluginConstants.META_INF_FOLDER_STR + 
	    			File.separator + 
	    			WebServicePluginConstants.WSDL_FOLDER_STR + 
	    			File.separator + 
	    			wsdl);
	  
	// Set service name.
	Service service = (Service)(defs.getServices().item(0));        	
	wsDescType.setWebserviceName(service.getName().getLocalPart() + "Name");
	  
	// Set wsdl-service.
	QNameType type = new QNameType();
	type.set_value(service.getName().getLocalPart());
	type.setNamespace(service.getName().getNamespaceURI());
	wsDescType.setWsdlService(type);

	PortComponentType port = new PortComponentType();
	if (service.getEndpoints().getLength() == 1) {
	  Endpoint e = (Endpoint) service.getEndpoints().item(0);
	  
	  // Set port name.
	  port.setPortName(e.getName() + "Name");
	  
	  // Set port url.
	  port.setUrl(	service.getName().getLocalPart() + 
	      		"/" + 						
	      		e.getName());        	  
	  
	  // Set implementation link and its properties.
	  ImplementationLinkType link = new ImplementationLinkType();
	  
	  PropertyType prop1 = new PropertyType();
	  prop1.set_value(WebServicePluginConstants.SCA_IMPL_CONTAINER_NAME);
	  prop1.setName(WebServicePluginConstants.SCA_IMPL_ID);
	  
	  PropertyType prop2 = new PropertyType();
	  prop2.set_value(service.getName().getLocalPart());
	  prop2.setName(WebServicePluginConstants.SCA_SERVICE_IDENTIFIER);
	  
	  PropertyType prop3 = new PropertyType();
	  prop3.set_value(ejbJar);
	  prop3.setName(WebServicePluginConstants.SCA_EJB_JAR_NAME);
	  
	  PropertyType prop4 = new PropertyType();
	  prop4.set_value(ejbName);
	  prop4.setName(WebServicePluginConstants.SCA_EJB_CLASS_NAME);
	  
	  PropertyType prop5 = new PropertyType();
	  prop5.set_value(owner);
	  prop5.setName(WebServicePluginConstants.HELPER_CONTEXT_MANAGEMENT_ATTR_NAME);
	  
	  PropertyType prop6 = new PropertyType();
	  prop6.set_value(WebServicePluginConstants.SCA_EJB_INTERFACE_LOCAL_TYPE_VALUE);
	  prop6.setName(WebServicePluginConstants.SCA_EJB_INTERFACE_TYPE);
	  
	  PropertyType prop7 = new PropertyType();
	  prop7.set_value(ejbInterface);
	  prop7.setName(WebServicePluginConstants.SCA_EJB_INTERFACE_NAME);
	  
	  PropertyType prop8 = new PropertyType();
	  prop8.set_value(componentName.getNodeValue());
	  prop8.setName(WebServicePluginConstants.SCA_EJB_SCA_COMPONENT_NAME);
	  
	  PropertyType prop9 = new PropertyType();
	  prop9.set_value(serviceName.getNodeValue());
	  prop9.setName(WebServicePluginConstants.SCA_SERVICE_NAME);
	  
	  link.setProperty(new PropertyType[] {prop1, prop2, prop3, prop4, prop5, prop6, prop7, prop8, prop9});
	  port.setImplementationLink(link);	  
	  
	  // Set wsdl-port.
	  QNameType portType = new QNameType(); 
	  portType.set_value(e.getName());
	  portType.setNamespace(e.getBinding().getNamespaceURI());
	  port.setWsdlPort(portType);
	  
	  // Set port metadata.
	  MetaDataType metadata = new MetaDataType();
	  PropertyType pr = new PropertyType();
	  pr.set_value("TODO");						// <-- !!!
	  pr.setName("TODO");						// <-- !!!
	  metadata.setProperty(new PropertyType[] {pr});
	  port.setMetaData(metadata);
	  
	  // Store service name to service id for later persisting.
	  this.iServiceNameToID.put(expServiceName, service.getName().getLocalPart());
	} else {
	  throw new RuntimeProcessException("Could not generate wsar, because more that one endpoint per service found.", new UnsupportedOperationException());
	}
	  
	PortComponentType[] ports = new PortComponentType[] {port};      	  
	wsDescType.setPortComponent(ports);
    } else {
	throw new UnsupportedOperationException("More than one definitions in single wsdl file.");
    }    
                
    return wsDescType;
  }
  
  private void createArchive(File aFile, String aFileName) throws IOException {        
    File jarFile = new File(aFile, aFileName);
    OutputStream fileOut = new FileOutputStream(jarFile);
    JarOutputStream jarOut = new JarOutputStream(fileOut);    
    
    this.writeToJar(new File(	aFile.getAbsolutePath() + 
	  			File.separator +
	  			WebServicePluginConstants.META_INF_FOLDER_STR + 
	  			File.separator +
	  			WebServicePluginConstants.SCA_ALT_FILE_NAME), jarOut);
    
    this.writeToJar(new File(	aFile.getAbsolutePath() + 
				File.separator +
				WebServicePluginConstants.META_INF_FOLDER_STR + 
	  			File.separator +
	  			WebServicePluginConstants.WSDL_FOLDER_STR), jarOut);
    
    jarOut.flush();
    jarOut.close();
  }
  
  private void writeToJar(File aFile, JarOutputStream aJarOut) throws IOException, FileNotFoundException {
    if(aFile.isDirectory()){
      for(File child: aFile.listFiles()){
	this.writeToJar(child, aJarOut);
      }
    } else {
      byte[] fileBytes = this.getFileBytes(aFile);
      String fileName = aFile.getName();

      JarEntry entry = null;
      if (fileName.endsWith(WebServicePluginConstants.WSDL_FOLDER_STR)) {
	entry = new JarEntry(	WebServicePluginConstants.META_INF_FOLDER_STR + 
	    			"/" +								
	    			WebServicePluginConstants.WSDL_FOLDER_STR + 
	    			"/" + 								
	    			fileName);
      } else {
	entry = new JarEntry(	WebServicePluginConstants.META_INF_FOLDER_STR + 
	    			"/" + 								
	  			fileName);
      }
      aJarOut.putNextEntry(entry);
      aJarOut.write(fileBytes);
      aJarOut.flush();
    }
  }
  
  private byte[] getFileBytes(File aFile) throws IOException, FileNotFoundException {
    long fileSize = aFile.length();
    byte[] arr = new byte[(int)fileSize];

    FileInputStream fileIn = new FileInputStream(aFile);
    fileIn.read(arr);
    fileIn.close();

    return arr;
  }
  
  private void copyWSDLFiles(File aFile) throws IOException, URISyntaxException, com.sap.engine.services.webservices.wsdl.WSDLException {
    String serviceName = null;
    
    // Create wsdl folder.
    File newFile = new File(	aFile.getCanonicalPath() + 
				File.separator + 
				WebServicePluginConstants.META_INF_FOLDER_STR + 
				File.separator + 
				WebServicePluginConstants.WSDL_FOLDER_STR);
    newFile.mkdir();        
    
    Set<String> keys = iServiceNameToUrl.keySet();
    Iterator<String> i = keys.iterator();
    
    // Get iterator over wsdls for copy.
    URL url = new URL("file:");
    while (i.hasNext()) {
      serviceName = i.next();
      
      URL u = iServiceNameToUrl.get(serviceName);
      if (url.equals(u))
	continue;
      else 
	url = u;
      
      String newFileName = null;
      // Check if archived wsdl this is.
      if (u.toString().indexOf('!') == -1) {
	File f = new File(u.getPath());
	
	newFileName = newFile.getCanonicalPath() + File.separator + f.getName();
      } else {
	String[] chunks = u.getPath().split("!/");
	if (chunks.length == 1)
	  throw new MalformedURLException("Url does not contain a '!' character: " + u);
		
	String entryPath = chunks[1];
	newFileName = newFile.getCanonicalPath() + File.separator + entryPath.substring(entryPath.lastIndexOf('/') + 1);
      }      

      // Download wsdl files.
      Hashtable<String, String> idMapping = new Hashtable<String, String>();
      String sufix = serviceName.replace(SERVICENAME_COMPONENTNAME_DELIMITER, SERVICENAME_COMPONENTNAME_DOWNLOAD_DELIMITER);
      
      WSDLImportTool tool = new WSDLImportTool();
      tool.setSufix(sufix);
      tool.setImportFix(true);
      File[] downloadedFiles = tool.downloadWSDL(u.toString(), newFile, idMapping);
      
      // Find root wsdl file and rename it.
      for (int j = 0; j < downloadedFiles.length; ++ j) {
	File f = downloadedFiles[j];
	if (f.getName().equals("wsdlroot" + sufix + ".wsdl")) {
	  File nf = new File(newFileName);
	  boolean b = f.renameTo(nf);
	  if (!b)
	    f.delete();
	    
	  break;
	}	
      }
    }
  }
  
  private void deleteCopiedWsdlFiles(File aFile) throws IOException {
    File metaInfFolder = new File(aFile.getCanonicalPath() + File.separator + WebServicePluginConstants.META_INF_FOLDER_STR);
    
    // Find ***_alt.xml file.
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File aDir, String aName) {
	return aName.equals(WebServicePluginConstants.SCA_ALT_FILE_NAME);
      }
    };
    
    // Delete ***_alt.xml file.
    String[] altDescs = metaInfFolder.list(filter);
    if (altDescs.length == 1) {
      File desc = new File(metaInfFolder.getCanonicalPath() + File.separator + WebServicePluginConstants.SCA_ALT_FILE_NAME);
      if (desc.isFile())
	desc.delete();
    }
    
    // Find wsdl folder.
    filter = new FilenameFilter() {
      public boolean accept(File aDir, String aName) {
	return aName.equals(WebServicePluginConstants.WSDL_FOLDER_STR);
      }      
    };
    
    // Delete wsdl folder.
    String[] wsdlFolders = metaInfFolder.list(filter);
    if (wsdlFolders.length == 1) {
      File wsdlFolder = new File(metaInfFolder.getCanonicalPath() + File.separator + WebServicePluginConstants.WSDL_FOLDER_STR);
      if (wsdlFolder.isDirectory())
	this.deleteFolderContent(wsdlFolder);
    }
  }
  
  private Document parseSCDL(URL aURL) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new File(aURL.toURI()));
  }
  
  private Document parseArchiveSCDL(URL aURL) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
    String[] chunks = aURL.getPath().split("!/");
    if (chunks.length == 1) {
      throw new URISyntaxException("", "Url does not contain a '!' character: " + aURL);
    }
    
    String file = chunks[0];
    String entryPath = chunks[1];
    
    if (file.startsWith("file:")) {
      file = file.substring("file:".length());
    } 
    
    JarFile jar = new JarFile(file);
    ZipEntry archSCDL = jar.getEntry(entryPath);
    if (archSCDL == null)
      throw new IOException("Can not get entry from archve file " + aURL.toString());
    
    InputStream inStream = jar.getInputStream(archSCDL);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(inStream);
  }
  
  private boolean isExposedWS(Node aNode) {
    boolean result = false;
    
    boolean hasBinding = false;
    boolean hasInterface = false;

    NodeList nl = aNode.getChildNodes();
    if (nl.getLength() > 0) {
      
      for (int i = 0; i < nl.getLength(); ++ i) {
	if (hasInterface && hasBinding)
	  break;
	
	Node node = nl.item(i);
	String nodeName = node.getLocalName();
	if (nodeName == null)
	  continue;
	
	// Check if node is with corresponding interface for generation mode.
	if (nodeName.equals(WebServicePluginConstants.COMPOSITE_INTERFACEDII_TAG_NAME) && iMode == WsarGeneratonMode.DII)
	  hasInterface = true;
	
	if (nodeName.equals(WebServicePluginConstants.COMPOSITE_INTERFACEWSDL_TAG_NAME) && iMode == WsarGeneratonMode.WS)
	  hasInterface = true;
	
	// Check if node is a binding.ws.
	if (nodeName.equals(WebServicePluginConstants.COMPOSITE_BINDINGWS_TAG_NAME))
	  hasBinding = true;
      }
      
      if (hasBinding && hasInterface)
	result = true;
    } else {
      result = false;
    }

    return result;    
  }
  
  private  void setPersistWSDLs() {
    int j = iServiceNameToHc.size();
    if (j > 0) {
      Object[] objects = new Object[j * 3];
      Set<String> keys = iServiceNameToHc.keySet();
      Iterator<String> iterator = keys.iterator();
      int i = 0;
      while (iterator.hasNext()) {
        String key = iterator.next();
        objects[i] = (Object) key;
        objects[i + 1] = (Object) iServiceNameToHc.get(key);
        objects[i + 2] = (Object) iServiceNameToID.get(key);      
        
        i += 3;
      }
  
      // Set wsdls to SCADeploymentContext for future processing.
      WebServicePluginFrame.getFacade().putSCADeploymentContextEntry("wsdls", objects);
    }
  } 
  
  private HelperContext buildHelperContext(Definitions aDefinition) throws 	RuntimeProcessException, 
										WSDLException, 
										TransformerFactoryConfigurationError, 
										TransformerException, 
										IOException {    

    List<Schema> schemaDOs = new ArrayList<Schema>();

    Map<String, String> options = new HashMap<String, String>();
    options.put(SapXmlHelper.OPTION_KEY_DEFINE_SCHEMAS, SapXmlHelper.OPTION_VALUE_FALSE);

    HelperContext hc = HelperProvider.getDefaultContext();

    XMLHelper xh = hc.getXMLHelper();
    if (!(xh instanceof SapXmlHelper))
      throw new RuntimeProcessException("", new ClassCastException());
    SapXmlHelper sxh = (SapXmlHelper) xh;

    XSDTypeContainer xsdTypeCont = aDefinition.getXSDTypeContainer(); 

    for (Object schema : xsdTypeCont.getSchemas()) {
      if (schema instanceof DOMSource) {
	DOMSource source = (DOMSource)schema;
	this.copyNamespaces((Element) source.getNode(), (Element) source.getNode());

	Transformer trans = TransformerFactory.newInstance().newTransformer();
	StringWriter swr = new StringWriter();

	trans.transform(source, new StreamResult(swr));	  
	SapXmlDocument schemaDocument = sxh.load(new StringReader(swr.toString()), null, options);
	Schema schemaDO = (Schema)schemaDocument.getRootObject();
	schemaDOs.add(schemaDO);	  
      }
    }
    
    // Define schema data objects.
    SapXsdHelper xsdHelper = ((SapXsdHelper)hc.getXSDHelper()); 
    xsdHelper.define(schemaDOs, null);

    return hc;
  } 
  
  private void copyNamespaces(Element sourceElement, Element destinationElement) {
    //Get all valid namespace mappings in scope
    Hashtable hash = DOM.getNamespaceMappingsInScope(sourceElement);
    Enumeration enumeration = hash.keys();
	    
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String value = (String) hash.get(key);
	     
      if (key == null || key.length() == 0) {
	destinationElement.setAttribute("xmlns", value);
      } else {
	destinationElement.setAttributeNS(NS.XMLNS, "xmlns:" + key, value);
      }
    }              
  }
  
  private void deleteFolderContent(File aFile) {
    File[] files = aFile.listFiles();
    for (int i = 0; i < files.length; ++ i) {
      File f = files[i];
      if (f.isDirectory())
	this.deleteFolderContent(f);
      else
	f.delete();
    }    
  }
}
