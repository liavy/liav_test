package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.ServiceDelegate;

import com.sap.engine.services.webservices.espbase.configuration.*;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;

public class CTSServiceDelegate extends ServiceDelegate {
  
  private String serviceRef = null;
  private ServiceDelegate defaultDelegate = null;
  private ConfigurationRoot configRoot = null;
  private QName serviceQName = null;
    
  public void _setConfigInfo(ConfigurationRoot config, QName serviceQname) {
    this.serviceQName = serviceQname;
    this.configRoot = config;
  }
  
  public CTSServiceDelegate(ServiceDelegate delegate) {
    this.defaultDelegate = delegate;
  }
  

  @Override
  public <T> T getPort(QName arg0, Class<T> arg1) {
    T port = null;    
    // Gets integrated client ports.
    Iterator<QName> integratedPorts = defaultDelegate.getPorts();
    ArrayList<QName> containedPorts = new ArrayList<QName>(); 
    while (integratedPorts.hasNext()) {
      QName portName = integratedPorts.next();      
      containedPorts.add(portName);
    }
    if (_isKnownPort(containedPorts,arg0) || this.configRoot == null) {
      // This is exinsing port
      port = defaultDelegate.getPort(arg0,arg1);
    } else {
      // Get whatever port for this interface
      port = defaultDelegate.getPort(arg1);     
    }
    if (configRoot == null) { // There is no configuration
      setCTSPropertiesOnPort(port, arg1.getName(),this.serviceRef);
      return port;
    }
    BindingData bConfig = _getBindingData(arg0);
    if (bConfig != null) { // This is proxy with external configuration
      _updateProxyConfiguration((BindingProvider) port,bConfig,null);
    }    
    setCTSPropertiesOnPort(port, arg1.getName(),this.serviceRef);
    return port;
  }
  
  @Override
  public <T> T getPort(Class<T> arg0) {    
    T o = defaultDelegate.getPort(arg0);
    if (this.configRoot != null) {
      QName portTypeName = _getPortTypeName(arg0);            
      InterfaceDefinition iDefinition = getInterfaceData(portTypeName);
      if (iDefinition != null) {
        ServiceData serviceData = _getService();
        BindingData[] bindings = serviceData.getBindingData();
        BindingData result = null;
        String id = iDefinition.getId();
        for (int i=0; i<bindings.length; i++) {
          BindingData bData = bindings[i];
          if (id.equals(bData.getInterfaceId())) {
            if (result == null) {
              result = bData;             
            }
            if (bData.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.DEFAULT_LP_FLAG) != null) {
              result = bData;
              break;
            }          }
        }
        if (result != null) {
          _updateProxyConfiguration((BindingProvider) o,result,iDefinition);          
        }
      }
    }
    setCTSPropertiesOnPort(o, arg0.getName(),this.serviceRef);
    return o; 
  }

  @Override
  public void addPort(QName arg0, String arg1, String arg2) {
    defaultDelegate.addPort(arg0,arg1,arg2);
  }

  @Override
  public <T> Dispatch<T> createDispatch(QName arg0, Class<T> arg1, Mode arg2) {
    return defaultDelegate.createDispatch(arg0,arg1,arg2);
  }

  @Override
  public Dispatch<Object> createDispatch(QName arg0, JAXBContext arg1, Mode arg2) {
    return defaultDelegate.createDispatch(arg0,arg1,arg2);
  }

  @Override
  public QName getServiceName() {
    return defaultDelegate.getServiceName();
  }

  @Override
  public Iterator<QName> getPorts() {
    if (this.configRoot != null && this.serviceQName != null) {
      String namespace = serviceQName.getNamespaceURI();
      ArrayList<QName> ports = new ArrayList<QName>();
      ServiceData serviceData = _getService();
      if (serviceData != null) {
        BindingData[] bindingDatas = serviceData.getBindingData();
        for (int i=0; i<bindingDatas.length; i++) {
          QName qname = new QName(namespace,bindingDatas[i].getName());
          ports.add(qname);
        }
        return ports.iterator();
      }
    }
    return defaultDelegate.getPorts();
  }
  
  @Override
  public URL getWSDLDocumentLocation() {
    return defaultDelegate.getWSDLDocumentLocation();
  }

  @Override
  public HandlerResolver getHandlerResolver() {
    return defaultDelegate.getHandlerResolver();
  }

  @Override
  public void setHandlerResolver(HandlerResolver arg0) {
    this.defaultDelegate.setHandlerResolver(arg0);
  }

  @Override
  public Executor getExecutor() {
    return defaultDelegate.getExecutor();
  }

  @Override
  public void setExecutor(Executor arg0) {
    this.defaultDelegate.setExecutor(arg0);
  }
  
  
  public String getServiceRef() {
    return serviceRef;
  }

  public void setServiceRef(String serviceRef) {
    this.serviceRef = serviceRef;
  }
  
  /**
   * Returns the cesvice in the configuration.
   */
  private ServiceData _getService() {  
    Service[] services = configRoot.getRTConfig().getService();
    for (int i=0; i<services.length; i++) {
      Service service = services[i];
      if (this.serviceQName.getLocalPart().equals(service.getServiceData().getName())) {
        return service.getServiceData();
      }      
    }
    return null;
  }  
    
  private <T> QName _getPortTypeName(Class<T> sei) {
    WebService wsAnnotation = sei.getAnnotation(WebService.class);
    String name = wsAnnotation.name();
    String namespaceUri = wsAnnotation.targetNamespace();
    if (name == null || name.length() == 0) {
      name = sei.getSimpleName();
    }
    if (namespaceUri == null || namespaceUri.length() == 0) {
      NameConvertor nc = new NameConvertor();
      namespaceUri = nc.packageToUri(sei.getPackage());
    }  
    QName result = new QName(namespaceUri,name);
    return result;
  }
  
  private InterfaceDefinition getInterfaceData(QName portTypeName) {
    InterfaceDefinitionCollection interfaces = this.configRoot.getDTConfig();
    InterfaceDefinition[] interfaceDefinition = interfaces.getInterfaceDefinition();
    for (int i=0; i<interfaceDefinition.length; i++) {
      Variant variant = interfaceDefinition[i].getVariant()[0];
      InterfaceData interfaceData = variant.getInterfaceData();
      QName interfaceName = new QName(interfaceData.getNamespace(),interfaceData.getName());
      if (portTypeName.equals(interfaceName)) {
        return interfaceDefinition[i];
      }
    }
    return null;
  }
  
  private boolean _isKnownPort(ArrayList<QName> ports,QName portName) {
    for (int i=0; i<ports.size(); i++) {
      if (portName.equals(ports.get(i))) {
        return true;
      }
    }
    return false;  
  }
  
  /**
   * Updates the proxy according the configuration given.
   * @param proxy
   * @param bindingConfig
   * @param interfaceConfig
   */
  private void _updateProxyConfiguration(BindingProvider proxy, BindingData bindingConfig, InterfaceDefinition interfaceConfig) {
    Map<String,Object> requestContext = proxy.getRequestContext();
    // Configure endpoint url
    String url = bindingConfig.getUrl();
    if (url != null) {
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,url);
    }
    // Configure basic user/pass property
    String authenticationMethod = bindingConfig.getSinglePropertyList().getPropertyValue("http://www.sap.com/webas/630/soap/features/authentication/","AuthenticationMethod");    
    if ("sapsp:HTTPBasic".equals(authenticationMethod)) {
      // Basic authentication
      String userName = bindingConfig.getSinglePropertyList().getPropertyValue("http://www.sap.com/webas/630/soap/features/authentication/","AuthenticationMethod.Basic.Username");
      String pass = bindingConfig.getSinglePropertyList().getPropertyValue("http://www.sap.com/webas/630/soap/features/authentication/","AuthenticationMethod.Basic.Password");
      if (userName != null) {
        requestContext.put(BindingProvider.USERNAME_PROPERTY,userName);        
      }
      if (pass != null) {
        requestContext.put(BindingProvider.PASSWORD_PROPERTY,pass);
      }      
    }
    if ("sapsp:HTTPSSO2".equals(authenticationMethod)) {
      // Configure SSO.      
      List<Handler> handlers = proxy.getBinding().getHandlerChain();
      handlers.add(new SAPSSOHandler());    
      proxy.getBinding().setHandlerChain(handlers);
      requestContext.put(SAPSSOHandler.INTERFACE_DEF_ID,bindingConfig.getInterfaceId());
      requestContext.put(SAPSSOHandler.PORT_NAME,bindingConfig.getName());      
    }
  }

  private BindingData _getBindingData(QName portQName) {
    BindingData[] bindings = _getService().getBindingData();
    for (int i=0; i<bindings.length; i++) {
      if (bindings[i].getName().equals(portQName.getLocalPart())) {
        return bindings[i];
      }
    }
    return null;
  }

  public static String createKey(String srvRef, String portClassName) { 
    String serviceRefModified = srvRef;  
    int cutIndex = srvRef.indexOf("service/");  
    if(cutIndex != -1) {
        serviceRefModified = srvRef.substring(cutIndex);    
    }   
    return serviceRefModified + "/" + portClassName;
  }  

  public static void setCTSPropertiesOnPort(Object port, String portClassName, String serviceRef) {
    if (serviceRef == null) {
      return;
    }
    String curKey = createKey(serviceRef, portClassName);
    
    if (curKey.equals("service/wsw2jhellosecureservice/com.sun.ts.tests.jaxws.sharedclients.hellosecureclient.Hello")) {
      BindingProvider bProv = (BindingProvider) port;
      Map reqCtx = bProv.getRequestContext();
      reqCtx.put(BindingProvider.USERNAME_PROPERTY, "j2ee");
      reqCtx.put(BindingProvider.PASSWORD_PROPERTY, "j2ee");
    } else if (curKey.equals("service/protectedvalidid/com.sun.ts.tests.jaxws.ee.w2j.rpc.literal.sec.secbasic.HelloProtected")) {
      BindingProvider bProv = (BindingProvider) port;
      Map reqCtx = bProv.getRequestContext();
      reqCtx.put(BindingProvider.USERNAME_PROPERTY, "j2ee");
      reqCtx.put(BindingProvider.PASSWORD_PROPERTY, "j2ee");
    } else if (curKey.equals("service/protectedinvalidid/com.sun.ts.tests.jaxws.ee.w2j.rpc.literal.sec.secbasic.HelloProtected")) {
      BindingProvider bProv = (BindingProvider) port;
      Map reqCtx = bProv.getRequestContext();
      reqCtx.put(BindingProvider.USERNAME_PROPERTY, "invalid");
      reqCtx.put(BindingProvider.PASSWORD_PROPERTY, "invalid");
    } else if (curKey.equals("service/protectedunauthid/com.sun.ts.tests.jaxws.ee.w2j.rpc.literal.sec.secbasic.HelloProtected")) {
      BindingProvider bProv = (BindingProvider) port;
      Map reqCtx = bProv.getRequestContext();
      reqCtx.put(BindingProvider.USERNAME_PROPERTY, "javajoe");
      reqCtx.put(BindingProvider.PASSWORD_PROPERTY, "javajoe");
    } else if (curKey.equals("service/guest/com.sun.ts.tests.jaxws.ee.w2j.rpc.literal.sec.secbasic.HelloGuest")) {
      BindingProvider bProv = (BindingProvider) port;
      Map reqCtx = bProv.getRequestContext();
      reqCtx.put(BindingProvider.USERNAME_PROPERTY, "javajoe");
      reqCtx.put(BindingProvider.PASSWORD_PROPERTY, "javajoe");
    } 
    //enabling of MTOM
    if (curKey.equals("service/wsservletmtomsoapbindingwithfullddstest/com.sun.ts.tests.webservices12.servlet.WSMTOMSBFullDDsTest.MTOMTest")
        || curKey.equals("service/wsservletmtomsoapbindingwithfullddstest/com.sun.ts.tests.webservices12.servlet.WSMTOMSBFullDDsTest.MTOMTestTwo") ) {
      BindingProvider bProv = (BindingProvider) port;
      Binding b = bProv.getBinding();
      if (b instanceof SOAPBinding) {
        SOAPBinding sb = (SOAPBinding) b;
        sb.setMTOMEnabled(true);
      }
    }
  }

  @Override
  public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Mode mode,
      WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Mode mode,
      WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  }

  @Override
  public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
    throw new RuntimeException("Method not supported");
  } 
  
}
