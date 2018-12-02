package com.sap.engine.services.webservices.espbase.client.dynamic;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.interfaces.webservices.runtime.ServletsHelper;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DGenericServiceImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.util.ServiceCache;
import com.sap.engine.services.webservices.espbase.discovery.TargetConfigurationException;
import com.sap.engine.services.webservices.espbase.discovery.TargetNotMappedException;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WSResourceAccessor;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.tools.WSDLDownloadResolver;
import com.sap.exception.BaseRuntimeException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.util.*;
import java.lang.ref.WeakReference;


/**
 * @author Ivan-M
 */
public class GenericServiceFactory {

  public static final String METHOD_NOT_AVAILABLE = "webservices_3610";
  public static J2EEEngineHelper engineHelper;
  public static final String JNDI_NAME = "GenericServiceFactory";
  public static final QName UNDEFINED_INTERFACE = new QName("undefined");

  private static final Location LOCATION = Location.getLocation(GenericServiceFactory.class);

  private ServiceCache cache;
  private Hashtable destinationToWSDL;
  private static final Set<WeakReference> allCaches = new HashSet<WeakReference>();

  private GenericServiceFactory(boolean reuseServices) {
    if(reuseServices) {
      cache = new ServiceCache();
      destinationToWSDL = new Hashtable();

      synchronized(allCaches) {
        {//remove those that are GC'ed
          Iterator<WeakReference> iter = allCaches.iterator();

          while (iter.hasNext()) {
            WeakReference next = iter.next();
            GenericServiceFactory nextFactory = (GenericServiceFactory) next.get();

            if (nextFactory == null) {
              iter.remove();
            }
          }

        }

        {//add next
          WeakReference ref = new WeakReference(this);
          allCaches.add(ref);
        }
      }
    }
  }

  /**
   * Returns an instance of the <code>GenericServiceFactory</code>
   */
  public static GenericServiceFactory newInstance() {
    return (new GenericServiceFactory(false));
  }

  /**
   * Returns an instance of the <code>GenericServiceFactory</code>
   *
   * @param reuseServices Identifies whether the created GenericService instances will be cached.
   */
  public static GenericServiceFactory newInstance(boolean reuseServices) {
    return (new GenericServiceFactory(reuseServices));
  }

  /**
   * Creates instance of <code>DGenericService</code> associated with the given wsdl file.
   * The standalone proxy files are generated.
   *
   * @param wsdlURL The URL associated with the wsdl file
   * @return Instance of <code>DGenericService</code> associated with the given wsdl file
   * @throws Exception
   */
  public DGenericService createService(String wsdlURL, ServiceFactoryConfig serviceFactoryConfig) throws ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering method createService(String " + wsdlURL + ", ServiceFactoryConfig " + serviceFactoryConfig + ")");
    }
    Object helper = null;
    Object serviceKey = null;
    if (serviceFactoryConfig != null) {
      helper = serviceFactoryConfig.get(ServiceFactoryConfig.HELPER_CONTEXT);
    }
    if (helper != null) {
      serviceKey = generateServiceKey(wsdlURL, helper.hashCode());
    } else {
      serviceKey = wsdlURL;
    }
    DGenericService service = getServiceFromCache(serviceKey);
    if (service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + wsdlURL);
      }
      service = createService_NewInstance(wsdlURL, serviceFactoryConfig);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + wsdlURL);
      }
    }
    return (service);
  }

  private DGenericService createService_NewInstance(String wsdlURL, ServiceFactoryConfig serviceFactoryConfig) throws ServiceException {
    ServiceFactoryConfig finalServiceFactoryConfig = determineFinalServiceFactoryConfig(serviceFactoryConfig);
    return(createService_NewInstance(wsdlURL, finalServiceFactoryConfig, null, wsdlURL,false));
  }

  private ServiceFactoryConfig determineFinalServiceFactoryConfig(ServiceFactoryConfig serviceFactoryConfig) {
    return(engineHelper == null ? serviceFactoryConfig : createMergedServiceFactoryConfig(serviceFactoryConfig));
  }

  private ServiceFactoryConfig createMergedServiceFactoryConfig(ServiceFactoryConfig serviceFactoryConfig) {
    ServiceFactoryConfig mergedServiceFactoryConfig = new ServiceFactoryConfig();
    mergedServiceFactoryConfig.putAll(engineHelper.getServiceFactoryConfig());
    if(serviceFactoryConfig.getPassword() != null ||
       serviceFactoryConfig.getProxyHost() != null ||
       serviceFactoryConfig.getProxyPassword() != null ||
       serviceFactoryConfig.getProxyPort() != null ||
       serviceFactoryConfig.getProxyUser() != null ||
       serviceFactoryConfig.getUser() != null) {
      mergedServiceFactoryConfig.remove(ServiceFactoryConfig.ENTITY_RESOLVER);
    }
    mergedServiceFactoryConfig.putAll(serviceFactoryConfig);
    return(mergedServiceFactoryConfig);
  }

  /**
   * Creates dynamic service in J2EE environment. The parameter ServiceFactoryConfig is not necessary.
   *
   * @param wsdlUrl
   * @return Returns DGenericService implementation.
   * @throws ServiceException
   */
  public DGenericService createService(String wsdlUrl) throws ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering method createService(String " + wsdlUrl + ")");
    }
    checkJ2EEEngineHelper("createService(String wsdlUrl)");
    DGenericService service = getServiceFromCache(wsdlUrl);
    if(service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + wsdlUrl);
      }
      service = createService_NewInstance(wsdlUrl);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + wsdlUrl);
      }
    }
    return(service);
  }

  private DGenericService createService_NewInstance(String wsdlUrl) throws ServiceException {
    ServiceFactoryConfig finalServiceFactoryConfig = determineFinalServiceFactoryConfig();
    return(createService_NewInstance(wsdlUrl, finalServiceFactoryConfig, null, wsdlUrl,false));
  }

  private ServiceFactoryConfig determineFinalServiceFactoryConfig() {
    ServiceFactoryConfig serviceFactoryConfig = engineHelper.getServiceFactoryConfig();
    initServiceFactoryConfigWithSystemProxySettings(serviceFactoryConfig);
    return(serviceFactoryConfig);
  }

  private void initServiceFactoryConfigWithSystemProxySettings(ServiceFactoryConfig serviceFactoryConfig) {
    if (System.getProperty("http.proxyHost") != null) {
      serviceFactoryConfig.setProxy(System.getProperty("http.proxyHost"), System.getProperty("http.proxyPort"));
    }
  }

  private void checkJ2EEEngineHelper(String unavailableMethodName) {
    if (engineHelper == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), METHOD_NOT_AVAILABLE, new Object[]{unavailableMethodName});
    }
  }

  /**
   * Creates dynamic service using LogicalMetadataTarget name and interface QName.
   *
   * @param lmtName
   * @param interfaceName
   * @return
   * @deprecated replaced by createService(String applicationName, String serviceReferenceId)
   * The new method doesn't need a concrete destination,
   * but uses the destination that is assigned to the consumer group
   * to which that service reference belongs 
   */
  @Deprecated
  public DGenericService createService(String lmtName, QName interfaceName) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering deprecated method createService(String " + lmtName + ", QName " + interfaceName + ")");
    }
    return(createService(lmtName,interfaceName,null));
  }

  /**
   * Creates dynamic service using LogicalMetadataTarget name and interface QName.
   *
   * @param lmtName
   * @param interfaceName
   * @return
   * @deprecated replaced by createService(String applicationName, String serviceReferenceId)
   * The new method doesn't need a concrete destination,
   * but uses the destination that is assigned to the consumer group
   * to which that service reference belongs 
   */
  @Deprecated
  public DGenericService createService(String lmtName, QName interfaceName,Map<String,Object> options) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering deprecated method createService(String " + lmtName + ", QName " + interfaceName + ",Map<String,Object> " + options + ")");
    }
    checkJ2EEEngineHelper("createService(String lmtName, QName interfaceName)");
    Object helper = null;
    Object serviceKey = null;
    if (options != null) {
      helper = options.get(ServiceFactoryConfig.HELPER_CONTEXT);
    }
    if (helper != null) {
      serviceKey = generateServiceKey(lmtName, interfaceName, helper.hashCode());
    } else {
      serviceKey = generateServiceKey(lmtName, interfaceName);
    }
    DGenericService service = getServiceFromCache(serviceKey);
    if(service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + serviceKey);
      }
      service = createService_NewInstance(lmtName, interfaceName, serviceKey,options);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + serviceKey);
      }
    }
    return(service);
  }

  /**
   * This method replaces the createService(String logicalDestination, QName portTypeName)  
   * Parameters uniquely specify a consumer group to which that service reference belongs.
   * It the consumer group is assigned to a physical system, 
   * the physical system is used for finding the service with portType qname 
   * taken from the service reference 
   * 
   * @param applicationName - applicationName is the name of the application, 
   * which contains the definition of the serviceReference
   * @param serviceRefID - the ID of the service reference that is about to be invoked
   * @return
   * @throws TargetNotMappedException
   * @throws IOException
   * @throws TargetConfigurationException
   * @throws ServiceException
   */
  public DGenericService createService(String applicationName, String serviceRefID) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering method createService(String " + applicationName + ", String " + serviceRefID + ")");
    }

    return createService(applicationName, serviceRefID, null);
  }

  /**
   * This method replaces the createService(String logicalDestination, QName portTypeName)  
   * Parameters uniquely specify a consumer group to which that service reference belongs.
   * It the consumer group is assigned to a physical system, 
   * the physical system is used for finding the service with portType qname 
   * taken from the service reference 
   * 
   * @param applicationName - applicationName is the name of the application, 
   * which contains the definition of the serviceReference
   * @param serviceRefID - the ID of the service reference that is about to be invoked
   * @return
   * @throws TargetNotMappedException
   * @throws IOException
   * @throws TargetConfigurationException
   * @throws ServiceException
   */
  public DGenericService createService(String applicationName, String serviceRefID, Map<String, Object> options) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering method createService(String " + applicationName + ", String " + serviceRefID + ", Map<String, Object> " + options + ")");
    }
    checkJ2EEEngineHelper("createService(String applicationName, String serviceRefID)");
    Object helper = null;
    Object serviceKey = null;
    if (options != null) {
      helper = options.get(ServiceFactoryConfig.HELPER_CONTEXT);
    }
    if (helper != null) {
      serviceKey = generateServiceKey(applicationName, serviceRefID, helper.hashCode());
    } else {
      serviceKey = generateServiceKey(applicationName, serviceRefID);
    }
    DGenericService service = getServiceFromCache(serviceKey);
    if(service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + serviceKey);
      }
      service = createService_NewInstance(applicationName, serviceRefID, serviceKey, options);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + serviceKey);
      }
    }

    return(service);
  }

  private DGenericService createService_NewInstance(String applicationName, String serviceRefID, Object serviceKey, Map<String, Object> options) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    DestinationsHelper destinationsHelper = null;
    String wsdlUrl;
    Definitions[] outWsdl = new Definitions[1];
    DGenericServiceImpl service = null;
    destinationsHelper = getDestinationsHelper(applicationName, serviceRefID);
    try {
      wsdlUrl = getWSDLUrl(destinationsHelper.getInterfaceName(), destinationsHelper, outWsdl);
    } catch (ServiceException e1) {
        QName interfaceName = null;
        try {
          interfaceName = convertName(destinationsHelper.getInterfaceName());
          destinationsHelper = getDestinationsHelper(destinationsHelper.getDestinationName(), interfaceName);
          wsdlUrl = getWSDLUrl(interfaceName, destinationsHelper, outWsdl);
        } catch (Exception e2) {
          LOCATION.traceThrowableT(Severity.DEBUG, "Failed to find Webservice with converted name [" + interfaceName +"]", e2);
            throw e1;
        }
    }
    Object helper = null;
    Object wsdlServiceKey = null;
    if (options != null) {
      helper = options.get(ServiceFactoryConfig.HELPER_CONTEXT);
    }
    if (helper != null) {
      wsdlServiceKey = generateServiceKey(wsdlUrl, helper.hashCode());
    } else {
      wsdlServiceKey = wsdlUrl;
    }
    service = getServiceFromCache(wsdlServiceKey);
    if(service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + wsdlUrl);
      }
      ServiceFactoryConfig serviceFactoryConfig = determineFinalServiceFactoryConfig(destinationsHelper);

      if (options != null) {
        if (options.get(ServiceFactoryConfig.HELPER_CONTEXT) != null) {
          serviceFactoryConfig.put(ServiceFactoryConfig.HELPER_CONTEXT, options.get(ServiceFactoryConfig.HELPER_CONTEXT));
        }
      }
      if (outWsdl[0] != null) {
        serviceFactoryConfig.setProperty(ServiceFactoryConfig.WSDL_DEFINITIONS, outWsdl[0]);
      }
      service =  (DGenericServiceImpl) createService_NewInstance(wsdlUrl, serviceFactoryConfig, destinationsHelper, serviceKey,true);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + wsdlUrl);
      }
      BindingData newData = destinationsHelper.selectBindingData(service.getServiceContext().getServiceData().getBindingData());
      if (newData != null) {
        service.getServiceContext().getServiceData().addBindingData(newData);
      }      
    }
    if (applicationName != null) {
      ((ClientServiceContextImpl) service.getServiceContext()).setApplicationName(applicationName);
    }
    if (serviceRefID != null) {
      ((ClientServiceContextImpl) service.getServiceContext()).setProperty(ClientServiceContextImpl.SERVICE_REF_ID,serviceRefID);
    }
    return service;
  }

  private DGenericService createService_NewInstance(String lmtName, QName interfaceName, Object serviceKey, Map<String,Object> options) throws TargetNotMappedException, IOException, TargetConfigurationException, ServiceException {
    DestinationsHelper destinationsHelper = null;
    DestinationContainer destinationContainer = DestinationContainer.getDestinationContainer();
    BindingData temporaryConfig = null;
    String wsdlUrl;
    Definitions[] outWsdl = new Definitions[1];
    if (destinationContainer.containsDestination(lmtName)) {
      // this is temporary destination
      temporaryConfig = destinationContainer.getDestination(lmtName);
      wsdlUrl = temporaryConfig.getUrl();
    } else {//read the wsdl of the port type
      destinationsHelper = getDestinationsHelper(lmtName, interfaceName);

      try {
        wsdlUrl = getWSDLUrl(interfaceName, destinationsHelper, outWsdl);
      } catch (ServiceException e) {
        LOCATION.catching(e);
        if (LOCATION.beDebug()) {
          LOCATION.debugT("not found interface with name :" + interfaceName);
        }
        interfaceName = convertName(interfaceName);
        if (LOCATION.beDebug()) {
          LOCATION.debugT("will try old interface name format :" + interfaceName);
        }

        destinationsHelper = getDestinationsHelper(lmtName, interfaceName);

        try {
          wsdlUrl = getWSDLUrl(interfaceName, destinationsHelper, outWsdl);
        } catch (ServiceException e1) {
          LOCATION.catching(e1);
          throw e;//throw the first exception - not the converted name
        }
      }
    }

    if (LOCATION.beDebug()) {
      if (temporaryConfig != null) {
        LOCATION.debugT("Temporary destination helper name :" + lmtName);
        LOCATION.debugT("will process wsdl url :" + wsdlUrl);
      } else {
        LOCATION.debugT("destination helper name :" + destinationsHelper.getDestinationName());
        LOCATION.debugT("destination helper interface name :" + destinationsHelper.getInterfaceName());
        LOCATION.debugT("will process wsdl url :" + wsdlUrl);
      }
    }
    DGenericService service = getServiceFromCache(wsdlUrl);
    if(service == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("not found cached service for key :" + wsdlUrl);
      }
      ServiceFactoryConfig serviceFactoryConfig = null;
      if (temporaryConfig != null) {
        serviceFactoryConfig = determineFinalServiceFactoryConfig(temporaryConfig);
      } else {
        serviceFactoryConfig = determineFinalServiceFactoryConfig(destinationsHelper);
      }

      if (options != null) {
        if (options.get(ServiceFactoryConfig.HELPER_CONTEXT) != null) {
          serviceFactoryConfig.put(ServiceFactoryConfig.HELPER_CONTEXT,options.get(ServiceFactoryConfig.HELPER_CONTEXT));
        }
      }

      if (outWsdl[0] != null) {
        serviceFactoryConfig.setProperty(ServiceFactoryConfig.WSDL_DEFINITIONS, outWsdl[0]);
      }

      service = createService_NewInstance(wsdlUrl, serviceFactoryConfig, destinationsHelper, serviceKey,false);
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("found cached service for key :" + wsdlUrl);
      }
    }

    // Sets the source of service initialization.
    ClientServiceContext context = ((DGenericServiceImpl) service).getServiceContext();
    context.setProperty(ClientServiceContextImpl.LMT_NAME,lmtName);
    if (!UNDEFINED_INTERFACE.equals(interfaceName)) {
      // It is temporart destination and the interface name is selected at later point
      context.setProperty(ClientServiceContextImpl.IF_NAME,interfaceName.toString());
    }
    return service;
  }

  private Object generateServiceKey(String lmtName, QName interfaceName) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(lmtName);
    if (interfaceName != null) {
      buffer.append(interfaceName);
    }
    return(buffer.toString());
  }

  private Object generateServiceKey(String applicationName, String serviceRefID) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(applicationName);
    if (serviceRefID != null) {
      buffer.append(serviceRefID);
    }
    return(buffer.toString());
  }

  private Object generateServiceKey(String wsdl, int hash) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(wsdl);
    buffer.append(hash);
    return(buffer.toString());
  }

  private Object generateServiceKey(String lmtName, QName interfaceName, int hash) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(lmtName);
    buffer.append(interfaceName);
    buffer.append(hash);
    return(buffer.toString());
  }

  private Object generateServiceKey(String applicationName, String serviceRefID, int hash) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(applicationName);
    buffer.append(serviceRefID);
    buffer.append(hash);
    return(buffer.toString());
  }

  private ServiceFactoryConfig determineFinalServiceFactoryConfig(BindingData bindingData) throws TargetNotMappedException, ServiceException {
    ServiceFactoryConfig serviceFactoryConfig = engineHelper.getServiceFactoryConfig();
    WSDLDownloadResolver resolver = new WSDLDownloadResolver();
    // Copy the binding data properties to the WSDLDownload Resolver
    String proxyHost = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_PROXY_HOST),bindingData);
    String proxyPort = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_PROXY_PORT),bindingData);
    if (proxyHost != null && proxyPort != null) {
      resolver.setProxyHost(proxyHost);
      int proxyPortInt = 80;
      try {
        proxyPortInt = Integer.valueOf(proxyPort.trim());
      } catch (Exception x) {
        LOCATION.catching(x);
      }
      resolver.setProxyPort(proxyPortInt);
    }
    String proxyUser = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_PROXY_USER),bindingData);
    String proxyPass = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_PROXY_PASS),bindingData);
    if (proxyUser != null) {
      resolver.setProxyUser(proxyUser);
    }
    if (proxyPass != null) {
      resolver.setProxyPass(proxyPass);
    }
    String nonProxyHosts = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_NON_PROXY_HOSTS),bindingData);
    if (nonProxyHosts != null) {
      resolver.setProxyExcludeList(nonProxyHosts.trim());
    }
    String socketTimeout = PublicProperties.getProperty(new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_SOCKET_TIMEOUT),bindingData);
    if (socketTimeout != null) {
      try {
        int socketTimeoutInt = Integer.parseInt(socketTimeout);
        resolver.setSocketTimeout(socketTimeoutInt);
      } catch (Exception x) {
        LOCATION.catching(x);
      }
    }
    String authenticationMethod = PublicProperties.getProperty(new QName(PublicProperties.AUTHENTICATION_FEATURE,PublicProperties.AUTHENTICATION_METHOD),bindingData);
    if (PublicProperties.AUTHENTICATION_METHOD_BASIC.equals(authenticationMethod)) {
      String userName = PublicProperties.getProperty(new QName(PublicProperties.AUTHENTICATION_FEATURE,PublicProperties.AUTHENTICATION_METHOD_BASIC_USER),bindingData);
      if (userName != null) {
        resolver.setUsername(userName);
      }
      String password = PublicProperties.getProperty(new QName(PublicProperties.AUTHENTICATION_FEATURE,PublicProperties.AUTHENTICATION_METHOD_BASIC_PASS),bindingData);
      if (password != null) {
        resolver.setPassword(password);
      }
    }
    serviceFactoryConfig.put(ServiceFactoryConfig.ENTITY_RESOLVER, resolver);
    return(serviceFactoryConfig);
  }


  private ServiceFactoryConfig determineFinalServiceFactoryConfig(DestinationsHelper destinationsHelper) throws TargetNotMappedException, ServiceException {
    ServiceFactoryConfig serviceFactoryConfig = engineHelper.getServiceFactoryConfig();
    serviceFactoryConfig.put(ServiceFactoryConfig.ENTITY_RESOLVER, destinationsHelper.getEntityResolverForTarget());
    return(serviceFactoryConfig);
  }

  private String getWSDLUrl(QName interfaceName, DestinationsHelper destinationsHelper,
                            Definitions[] outWsdl) throws ServiceException, TargetNotMappedException {

    String wsdlUrl = destinationsHelper.getWSDLUrl(outWsdl);
    if (wsdlUrl == null) {
      throw new ServiceException("Web Service " + interfaceName + " not found via destination " + destinationsHelper.getDestinationName() + ". Please, check that the physical destination points to a system, containing a web service with such a name.");
    }
    return(wsdlUrl);
  }

  private DestinationsHelper getDestinationsHelper(String lmtName, QName interfaceName) throws IOException, ServiceException {
    DestinationsHelper destinationsHelper = DestinationsHelper.getDestinationsHelper(lmtName, interfaceName);
    if (destinationsHelper == null) {
      throw new IOException("Cannot find matching interface : " + interfaceName + " with destination : " + lmtName);
    }
    return(destinationsHelper);
  }

  private DestinationsHelper getDestinationsHelper(String applicationName, String serviceRefID) throws IOException, ServiceException {
    DestinationsHelper destinationsHelper = DestinationsHelper.getDestinationsHelper(applicationName, serviceRefID);
    if (destinationsHelper == null) {
        throw new IOException("Cannot find matching serviceRefID : " + serviceRefID + " within application : " + applicationName);
    }
    return(destinationsHelper);
  }

  /**
   * Creates service new instance.
   * @param wsdlURL
   * @param serviceFactoryConfig
   * @param destinationsHelper
   * @param serviceKey
   * @return
   * @throws ServiceException
   */
  private DGenericService createService_NewInstance(String wsdlURL, ServiceFactoryConfig serviceFactoryConfig, DestinationsHelper destinationsHelper, Object serviceKey, boolean massConfig) throws ServiceException {
    try {
      initHTTPProxyResolver();
      DGenericServiceImpl service = new DGenericServiceImpl(wsdlURL, serviceFactoryConfig, destinationsHelper);
      // In case of mass config, add the additional binding data.
      if (massConfig) {
        BindingData newData = destinationsHelper.selectBindingData(service.getServiceContext().getServiceData().getBindingData());
        if (newData != null) {
        	service.getServiceContext().getServiceData().addBindingData(newData);
        }
      }
      if(useCache(serviceKey)) {
        if (LOCATION.beDebug()) {
          LOCATION.debugT("will store the dynamic service in the cache with key " + serviceKey);
        }
        synchronized (this) { //to ensure that the destinationToWSDL and cache work properly
        cache.put(serviceKey, service);
        if (destinationsHelper != null) { // serviceKey is not wsdlURL
          if (LOCATION.beDebug()) {
            LOCATION.debugT("will store the dynamic service in the cache with key " + wsdlURL);
          }
          Object wsdlServiceKey = null;
          Object helper = serviceFactoryConfig.get(ServiceFactoryConfig.HELPER_CONTEXT);
          if (helper != null) {
            wsdlServiceKey = generateServiceKey(wsdlURL, helper.hashCode());
          } else {
            wsdlServiceKey = wsdlURL;
          }
          cache.put(wsdlServiceKey, service);
          destinationToWSDL.put(serviceKey, wsdlServiceKey);
        }
        }
        if (engineHelper == null) {
          // The process is running in standalone mode
          ((ClientServiceContextImpl) service.getServiceContext()).setStandalone(true);
        }
        if (wsdlURL != null) {
          ((ClientServiceContextImpl) service.getServiceContext()).setProperty(ClientServiceContextImpl.WSDL_URL, wsdlURL);
        }
      } else {
        if (LOCATION.beDebug()) {
          LOCATION.debugT("will not store the dynamic service in the cache with key " + serviceKey);
        }
      }
      if (engineHelper == null) {
         // The process is running in standalone mode
        ((ClientServiceContextImpl) service.getServiceContext()).setStandalone(true);
      }
      if (wsdlURL != null) {
        ((ClientServiceContextImpl) service.getServiceContext()).setProperty(ClientServiceContextImpl.WSDL_URL,wsdlURL);
      }
      return(service);
    } catch (Exception x) {
      throw new WebserviceClientException(WebserviceClientException.WS_CREATE_ERROR, x, x.getLocalizedMessage());
    }
  }

  private DGenericServiceImpl getServiceFromCache(Object serviceKey) {
    if(useCache(serviceKey)) {
      return((DGenericServiceImpl)(cache.get(serviceKey)));
    }
    return(null);
  }

  private boolean useCache(Object serviceKey) {
    return(cache != null && serviceKey != null);
  }

  private void initHTTPProxyResolver() {
    if (engineHelper != null) {
      HTTPProxyResolver proxyResolver = getGlobalHTTPProxyResolver();
      if (proxyResolver != null) {
        if (LOCATION.beDebug()) {
          LOCATION.debugT("global http proxy settings will be checked");
        }
        HTTPSocket.PROXY_RESOLVER = proxyResolver;
      } else {
        if (LOCATION.beDebug()) {
           LOCATION.debugT("no global http proxy settings");
        }
      }
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("cannot access global http proxy settings");
      }
    }
  }

  /**
   * Defines the capacity of the GenericService instances cache. The cache is based on
   * soft references so this method is not sufficient.
   * 
   * @param capacity The capacity of the cache.
   * @exception UnsupportedOperationException will be thrown if the cache is not used.
   */
  @Deprecated
  public void setInternalCacheCapacity(int capacity) {
  }

  /**
   * Returns the capacity of the GenericService instances cache. The cache is based on
   * soft references so this method returns always value -1.
   *
   * @return The capacity of the internal cache.
   * @exception UnsupportedOperationException will be thrown if the cache is not used.
   */
  @Deprecated
  public int getInternalCacheCapacity() {
    return(-1);
  }

  /**
   * Clears the GenericService instances cache if such is used.
   */
  public void stop() {
    if(cache != null) {
      cache.clear();
    }
  }

  public static void clearAllCaches() {
    synchronized (allCaches) {
      Iterator<WeakReference> iter = allCaches.iterator();

      while (iter.hasNext()) {
        WeakReference next = iter.next();
        GenericServiceFactory nextFactory = (GenericServiceFactory) next.get();

        if (nextFactory != null && nextFactory.cache != null) {
          nextFactory.cache.clear();
        } else {
          //those are already GC'ed
          iter.remove();
        }
      }
    }
  }

  public static void clearCachesForDestination(String lmtName) {
    synchronized (allCaches) {
      Iterator<WeakReference> iter = allCaches.iterator();

      while (iter.hasNext()) {
        WeakReference next = iter.next();
        GenericServiceFactory nextFactory = (GenericServiceFactory) next.get();

        if (nextFactory != null && nextFactory.cache != null) {
          nextFactory.purgeServicesFromDestination(lmtName);
        } else {
          //those are already GC'ed
          iter.remove();
        }
      }
    }
  }

  public static void  clearCachesForApplication(String appName) {
    synchronized (allCaches) {
      Iterator<WeakReference> iter = allCaches.iterator();

      while (iter.hasNext()) {
        WeakReference next = iter.next();
        GenericServiceFactory nextFactory = (GenericServiceFactory) next.get();

        if (nextFactory != null && nextFactory.cache != null) {
          nextFactory.purgeServicesFromApplication(appName);
        } else {
          //those are already GC'ed
          iter.remove();
        }
      }
    }
  }

  public static void  clearCachesForServiceRef(String appName, String sRef) {
    synchronized (allCaches) {
      Iterator<WeakReference> iter = allCaches.iterator();

      while (iter.hasNext()) {
        WeakReference next = iter.next();
        GenericServiceFactory nextFactory = (GenericServiceFactory) next.get();

        if (nextFactory != null && nextFactory.cache != null) {
          nextFactory.purgeServiceFromCache(appName, sRef);
        } else {
          //those are already GC'ed
          iter.remove();
        }
      }
    }
  }

  public void purgeServiceFromCache(String wsdlUrl) {
    purge(wsdlUrl);
  }

  public void purgeServicesFromDestination(String lmtName) {
    Object serviceKey = generateServiceKey(lmtName, (QName)null);
    purgeDestination(serviceKey);
  }

  public void purgeServiceFromCache(String lmtName, QName interfaceName) {
    Object serviceKey = generateServiceKey(lmtName, interfaceName);
    purgeDestination(serviceKey);
  }

  public void purgeServicesFromApplication(String appName) {
    Object serviceKey = generateServiceKey(appName, (String)null);
    purgeDestination(serviceKey);
  }

  public void purgeServiceFromCache(String applicationName, String serviceRefID) {
    Object serviceKey = generateServiceKey(applicationName, serviceRefID);
    purgeDestination(serviceKey);
  }

  private synchronized void purgeDestination(Object serviceKey) {
    if (useCache(serviceKey)) {
      String stringKey = null;
      Object wsdlUrl = null;
      Enumeration keys = cache.keys();

      while (keys.hasMoreElements()) {
        stringKey = (String) (keys.nextElement());

        if (stringKey.startsWith((String) serviceKey)) {
          cache.remove(stringKey);

          if (LOCATION.beDebug()) {
            LOCATION.debugT("cleared cached service for key :" + stringKey);
          }
          wsdlUrl = destinationToWSDL.get(stringKey);
          if (wsdlUrl == null) {
            return;
          }

          destinationToWSDL.remove(stringKey);
          purge(wsdlUrl);
        }
      }
    }
  }

  private synchronized void purge(Object wsdlUrl) {
    if (useCache(wsdlUrl)) {
      String stringKey = null;
      Enumeration keys = cache.keys();
      while (keys.hasMoreElements()) {
        stringKey = (String) (keys.nextElement());
        if (stringKey.startsWith((String) wsdlUrl)) {
          cache.remove(stringKey);
          if (LOCATION.beDebug()) {
            LOCATION.debugT("cleared cached service for key :" + stringKey);
          }
        }
      }
    }
  }

  private static HTTPProxyResolver getGlobalHTTPProxyResolver() {
    try {
      Context ctx = new InitialContext();
      ServletsHelper servletsHelper = (ServletsHelper) ctx.lookup("wsContext/" + ServletsHelper.NAME);
      return servletsHelper.getHTTPProxyResolver();
    } catch (NamingException e) {
      LOCATION.catching(e);
      return null;
    }
  }

  //convert the name to the other format NY->04s or 04s->NY 
  public static QName convertName(QName param) {
    String namespace = param.getNamespaceURI();
    String localName = param.getLocalPart();
    if (namespace == null || localName == null) {
      return null;
    }

    //check whether the ptQname is NW04(s)
    String nsSuffix = null;
    if (namespace.endsWith("/document")) {
      nsSuffix = "/document";
    } else if (namespace.endsWith("/rpc")) {
      nsSuffix = "/rpc";
    } else if (namespace.endsWith("/rpc_enc")) {
      nsSuffix = "/rpc_enc";
    }

    String nameSuffix = null;
    if (nsSuffix != null) {
      if (localName.toUpperCase(Locale.ENGLISH).endsWith("_DOCUMENT")) {
        nameSuffix = "_DOCUMENT";
      } else if (localName.toUpperCase(Locale.ENGLISH).endsWith("_RPC")) {
        nameSuffix = "_RPC";
      } else if (localName.toUpperCase(Locale.ENGLISH).endsWith("_RPC_ENC")) {
        nameSuffix = "_RPC_ENC";
      }

      if (nameSuffix != null) {
        // <      Remove trailing fragments from the namespace and localName and try with the new Qname >
        return new QName(namespace.substring(0, namespace.length() - nsSuffix.length()),
                localName.substring(0, localName.length() - nameSuffix.length()));
      }
    }

    return new QName(namespace + "/document", localName + "_Document");
  }

}
