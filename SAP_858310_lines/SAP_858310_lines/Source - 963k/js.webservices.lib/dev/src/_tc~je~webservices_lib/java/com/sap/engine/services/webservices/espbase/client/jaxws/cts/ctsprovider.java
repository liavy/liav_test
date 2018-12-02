package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.SAPServiceDelegate;

import com.sap.tc.logging.Location;

public class CTSProvider extends Provider {
  //a thread local variable used to pass data to this service delegate
  public static final ThreadLocal BRIDGE  = new ThreadLocal();
  //a thread local variable used for service initialization
  public static final ThreadLocal SERVICE_REF_META_DATA = new ThreadLocal();  
  
  public static final String JAXWS_VENDOR_PROPERTY_NAME = "jaxws.vendor.name";
  public static final String SUN_JAXWS_VENDOR_NAME = "SUN";
  public static final String SAP_JAXWS_VENDOR_NAME = "SAP";
  
  // Server host and port name
  public static final String HOSTNAME = "webServerHost.1";
  public static final String PORTNAME = "webServerPort.1";
  public static final String WEBROOT = "webServerRoot";
  private static final String MAPPING_PROP_FILE = "/META-INF/wslib.properties";  
  private static final String MAPPING_PROP_NAME = "mapping.path";   
  public static final Location LOC = Location.getLocation(CTSProvider.class);
  private static final String NON_PROXY_HOSTS_DEFAULT = "10.*|127.*|192.168.*";
  private static final String NON_PROXY_HOSTS = "http.nonProxyHosts";
  
  // SUN Provider
  private Provider sunProvider = null;
  
  // SUN Provider Class Name 
  private String sunProviderName = "com.sun.xml.ws.spi.ProviderImpl";
  
  // External mapping file path
  private String mappingFilePath = "c:/mapping.txt";
  
  /**
   * Default constructor is invoked by the JAXWS runtime.
   *
   */
  public CTSProvider() {
    // It is possible to specify the mapping file path via a property file.
    InputStream specialPath = CTSProvider.class.getClassLoader().getResourceAsStream(MAPPING_PROP_FILE);
    if (specialPath != null) {
      try {    
        Properties temp = new Properties();
        temp.load(specialPath);
        this.mappingFilePath = temp.getProperty(MAPPING_PROP_NAME,this.mappingFilePath);
      } catch (IOException x) {
        LOC.catching(x);
      } finally {
        try {
          specialPath.close();
        } catch (IOException x) {
          LOC.catching(x);
        }
      }
    }
    try {
      initProvider();      
      // Temporary proxy handling. Sets the proxy filter for LAN to bypass local hosts.
      String nonProxyHosts = System.getProperty("http.nonProxyHosts");
      /*if (nonProxyHosts == null) {
        nonProxyHosts = NON_PROXY_HOSTS_DEFAULT;
        LOC.debugT("["+this.getClass().getName()+"] 'nonProxyHosts' are not configured.");
        LOC.debugT("["+this.getClass().getName()+"] 'nonProxyHosts' are reset to '"+NON_PROXY_HOSTS_DEFAULT+"'.");
        System.setProperty(NON_PROXY_HOSTS,NON_PROXY_HOSTS_DEFAULT);
      } else {*/
        LOC.debugT("["+this.getClass().getName()+"] 'nonProxyHosts' are configured '"+nonProxyHosts+"'.");
        //LOC.debugT("["+this.getClass().getName()+"] 'nonProxyHosts' default value is '"+nonProxyHosts+"'.");
      //}
    } catch (ClassNotFoundException e) {
      LOC.catching(e);
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      LOC.catching(e);
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      LOC.catching(e);
      throw new RuntimeException(e);
    }
  }  
  
  /**
   * Inits JAX-WS Implementation Provider.
   * @return
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  private void initProvider() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    // this.sunProvider is never initialized
    /*
    String jaxwsVendor = System.getProperty(JAXWS_VENDOR_PROPERTY_NAME, SAP_JAXWS_VENDOR_NAME);
    if(SUN_JAXWS_VENDOR_NAME.equalsIgnoreCase(jaxwsVendor)) {
      this.sunProvider = createSUNProvider();
    } */    
  }
  
  /**
   * Creates SUN RI JAX-WS Implementation Provider.
   * @return
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  private Provider createSUNProvider() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Class providerClass = Class.forName(sunProviderName, true, this.getClass().getClassLoader());
    return((Provider)(providerClass.newInstance()));      
  }
  
  /**
   * Creates ServiceDelegate for WSClient.
   */
  public ServiceDelegate createServiceDelegate(URL arg0, QName arg1, Class arg2) {
    if (arg0 != null) {
      // Mapping of WSDL URL Locations, required for CTS integration. 
      HashMap<String,String> fileMapping = loadMappingFile();
      String mappedPath = fileMapping.get(arg2.getName());
      if (mappedPath == null) {
        mappedPath = fileMapping.get(arg1.toString());
      }
      if (mappedPath == null ) {
        LOC.debugT(">>>>>>>>> ERROR: Could not find mapping for service QName=" + arg1 + ", class=" + arg2);
      }
      if (mappedPath != null) {
        arg0 = replacePathVariables(arg2.getName(),arg0,mappedPath,fileMapping);        
      }
    }
    LOC.debugT(this.getClass().getName()+ ": Creating client instance with WSDL URL ["+arg0 +"] ("+arg1+") <"+arg2.getName()+">");
    // Thread local object that provides deployment specific client configuration
    ServiceRefMetaData serviceRefMetaData = (ServiceRefMetaData) SERVICE_REF_META_DATA.get();    
    if(serviceRefMetaData != null && serviceRefMetaData.getWsdlFile() != null) {
      try {
        // Read the WSDL location from the service ref metadata location
        arg0 = new URL(serviceRefMetaData.getWsdlFile());
      } catch(MalformedURLException e) {
        LOC.debugT(this.getClass().getName()+ ": " + e.toString()); 
      }                  
    }
    ServiceDelegate returnDelegate = createDelegate(arg0,arg1,arg2,serviceRefMetaData);    
    return returnDelegate;
  }
  
  private ServiceDelegate createDelegate(URL wsdlPath, QName serviceQName, Class serviceClass, ServiceRefMetaData serviceRefMetadata) {
    if (this.sunProvider != null) {
      LOC.debugT(this.getClass().getName()+": Using SUN JAX-WS RI to create service delegate.");
      // Using SUN JAX-WS RI.
      ServiceDelegate sunDelegate = this.sunProvider.createServiceDelegate(wsdlPath,serviceQName,serviceClass);
      CTSServiceDelegate wrapperDelegate = new CTSServiceDelegate(sunDelegate);
      // Add the config here
      if (serviceRefMetadata != null) {           
        wrapperDelegate._setConfigInfo(serviceRefMetadata.getConfigurationDescriptor(),serviceQName);
      }
      utilizeServiceObjectFactoryData(wrapperDelegate);  
      return wrapperDelegate;
    } else { 
      // Using SAP JAX-WS Implementation
      LOC.debugT(this.getClass().getName()+": Using SAP JAX-WS Implemetation to create service delegate.");
      SAPServiceDelegate delegate = new SAPServiceDelegate(wsdlPath, serviceQName, serviceClass,serviceRefMetadata);
      String srvRef = (String) BRIDGE.get();
      if (srvRef != null) {
        delegate.setServiceRef(srvRef);
      }
      return delegate;
    }
  }
  
  
  /**
   * Replaces variable with actual value in specified path.
   * @param mappedPath
   * @param variableName
   * @param variableValue
   * @return
   */
  private String replacePathVariable(String mappedPath, String variableName, String variableValue) {
    int index = mappedPath.indexOf(variableName);
    if (index != -1) {
      mappedPath = mappedPath.substring(0,index)+variableValue+mappedPath.substring(index+variableName.length());
    } 
    return mappedPath;
  }
  
  /**
   * Creates URL from mapped path.
   * @param serviceName
   * @param originalPath
   * @param mappedPath
   * @param fileMapping
   * @return
   */
  private URL replacePathVariables(String serviceName,URL originalPath,String mappedPath, HashMap<String,String> fileMapping) {
    try {
      String hostName = fileMapping.get(HOSTNAME);
      String portName = fileMapping.get(PORTNAME);
      if (hostName != null) {
        mappedPath = replacePathVariable(mappedPath,HOSTNAME,hostName);
      }
      if (portName != null) {
        mappedPath = replacePathVariable(mappedPath,PORTNAME,portName);
      }        
      String webRoot = fileMapping.get(WEBROOT);
      URL baseURL = null;
      if (webRoot != null) {
        try {
          baseURL = URLLoader.fileOrURLToURL(null,webRoot);
        } catch (Throwable x) {
          baseURL = null;
        }
      }
      URL result = URLLoader.fileOrURLToURL(baseURL,mappedPath);      
      LOC.debugT(this.getClass().getName()+": Remapping url ["+originalPath+"] for service ["+serviceName+"] to URL ["+result+"].");
      return result;
    } catch (IOException x) {
      LOC.debugT(this.getClass().getName()+": URL ["+mappedPath+"] is malformed. Check for such uri in the file ["+mappingFilePath+"].");
      return originalPath;
    }
  }    
  
  private void utilizeServiceObjectFactoryData(CTSServiceDelegate sapSrv) {
    String srvRef = (String) BRIDGE.get();
    if (srvRef == null) {
      return;
    }
    sapSrv.setServiceRef(srvRef);
  }
  
  /**
   * Closes Reader.
   * @param reader
   */
  private void closeReader(Reader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException x) {
        if (LOC != null) {
          LOC.catching(x);
        }
      }
    }
  }
  
  /**
   * Closes InputStream.
   * @param input
   */
  private void closeStream(InputStream input) {
    if (input != null) {
      try {
        input.close();
      } catch (IOException x) {
        if (LOC != null) {
          LOC.catching(x);
        }        
      }
    }
  }
  
  /**
   * Loads custom WSDL url mapping file.
   *
   */
  private HashMap<String,String> loadMappingFile() {    
    FileInputStream input = null;
    InputStreamReader reader = null;
    BufferedReader bufferedReader = null;
    HashMap<String,String> fileMapping = new HashMap<String,String>();
    try {
      LOC.debugT("Loadin mapping file from location :["+mappingFilePath+"]");
      input = new FileInputStream(mappingFilePath);
      reader = new InputStreamReader(input); //$JL-I18N$
      bufferedReader = new BufferedReader(reader);
      fileMapping = readFileContents(bufferedReader);
    } catch (FileNotFoundException e) {
      LOC.debugT("Additional file for CTS configuration can not be found ["+e.getMessage()+"].");      
    } finally {
      closeStream(input);
      closeReader(reader);
      closeReader(bufferedReader);      
    }
    return fileMapping;
  }
  
  /**
   * Reads file mapping contents from input reader.
   * @param reader
   * @return
   */
  private HashMap<String,String> readFileContents(BufferedReader reader) {
    HashMap<String,String> fileMapping = new HashMap<String,String>();
    try {      
      fileMapping.clear();
      String currentLine = null;
      while ((currentLine = reader.readLine()) != null) {
        int separatorIndex = currentLine.indexOf('=');
        if (separatorIndex != -1) {
          String key = currentLine.substring(0,separatorIndex).trim();
          String value = currentLine.substring(separatorIndex+1).trim();
          fileMapping.put(key,value);
        }      
      }
    } catch (IOException x) {
      if (LOC != null) {
        LOC.catching(x);
      }              
    }
    return fileMapping;    
  }

  /**
   * Creates endpoint.
   */
  public Endpoint createEndpoint(String arg0, Object arg1) {
    try {
//      if (this.sunProvider != null) return this.sunProvider.createEndpoint(arg0,arg1); 
//        else return createSUNProvider().createEndpoint(arg0,arg1);                  
      return new CTSEndpoint(arg0,arg1);
    } catch (Exception e) {
      LOC.catching(e);
      return null;
    }
  }

  /**
   * Create and publish endpoint.
   */
  public Endpoint createAndPublishEndpoint(String arg0, Object arg1) {
    try {
//      if (this.sunProvider != null) return this.sunProvider.createAndPublishEndpoint(arg0,arg1);
//       else return createSUNProvider().createEndpoint(arg0,arg1);
      return new CTSEndpoint(arg0,arg1);
    } catch (Exception x) {
      LOC.catching(x);
      return null;
    }
  }

  @Override
  public W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName,
      List<Element> metadata, String wsdlDocumentLocation, List<Element> referenceParameters) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public EndpointReference readEndpointReference(Source eprInfoset) {
    throw new RuntimeException("Method not supported");
  }

}
