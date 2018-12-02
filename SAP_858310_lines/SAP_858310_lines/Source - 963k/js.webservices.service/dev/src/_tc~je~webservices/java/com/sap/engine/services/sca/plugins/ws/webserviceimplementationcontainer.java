package com.sap.engine.services.sca.plugins.ws;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.ws.Holder;

import com.sap.engine.services.sca.plugins.ws.tools.sdo.das.NameConverter;

import com.sap.engine.interfaces.sca.SCAEnvironment;
import com.sap.engine.interfaces.sca.SCAHelper;
import com.sap.engine.interfaces.sca.assembly.IComponentArtifact;
import com.sap.engine.interfaces.sca.assembly.ISCAAssembly;
import com.sap.engine.interfaces.sca.assembly.IServiceArtifact;
import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.runtime.ServiceInstance;
import com.sap.engine.interfaces.sca.spi.BindingContext;
import com.sap.engine.interfaces.sca.wire.Message;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.OperationContext;
import com.sap.engine.interfaces.sca.wire.Result;
import com.sap.engine.interfaces.sca.wire.Value;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.EventObject;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.sca.plugins.ws.tools.sdo.das.WebServiceRepository;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;

import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author I044263
 *
 */
public class WebServiceImplementationContainer implements ImplementationContainer {
  
  private Location iLocation = Location.getLocation(this.getClass());
  
  /**
   * Ctor.
   */
  public WebServiceImplementationContainer() {

  }
  
  public String getImplementationID() {
    return WebServicePluginConstants.SCA_IMPL_CONTAINER_NAME;
  }

  public ClassLoader getImplementationLoader(ConfigurationContext aContext) throws RuntimeProcessException {
    CallEntry entry = WSLogTrace.getESBTracer().getAttachedCallEntry();
    
    if (aContext == null)
      throw new RuntimeProcessException("aContext can not be null.", new NullPointerException());
    
    if (!(aContext instanceof  ProviderContextHelper))
      throw new RuntimeProcessException("aContext is not an instance of ProviderContextHelper.", new ClassCastException());
    
    // Get implementation link.
    ProviderContextHelper providerContextHelper = (ProviderContextHelper) aContext;
    ImplementationLink implLink = providerContextHelper.getImplementationLink();
    String prop = implLink.getProperty(WebServicePluginConstants.HELPER_CONTEXT_MANAGEMENT_ATTR_NAME);
    
    entry.setHeader("HelperContext management", prop);
    
    HelperContext hc = null;
    try {
      if (prop.equals(WebServicePluginConstants.HELPER_CONTEXT_IMPL_MANAGED))
	hc = this.processImplementationManagedCase(providerContextHelper);
      else if (prop.equals(WebServicePluginConstants.HELPER_CONTEXT_CNT_MANAGED)) 
	hc = this.processContainerManagedCase(providerContextHelper);
      else
	// Default case implementation managed HelperContext.
	hc = this.processImplementationManagedCase(providerContextHelper);
	
      if (hc == null)
	throw new NullPointerException();
    } catch (Exception e) {
      throw new RuntimeProcessException("Can not build HelperContext.", e);
    }
                 
    // Set XMLHelper for WS Runtime.
    providerContextHelper.setSdoHelper(hc.getXMLHelper());         
    
    return null;
  }

  public Object invokeMethod(String aOperation, Class[] aClass, Object[] aValues, ConfigurationContext aContext) throws RuntimeProcessException,
  															InvocationTargetException {
    CallEntry entry = WSLogTrace.getESBTracer().getAttachedCallEntry();
    
    if (aOperation == null && aContext == null)
      throw new RuntimeProcessException("", new NullPointerException());
    
    if (!(aContext instanceof ProviderContextHelper))
      throw new RuntimeProcessException("", new ClassCastException());
    
    // Get implementation link.
    ProviderContextHelper providerContextHelper = (ProviderContextHelper) aContext;
    ImplementationLink implLink = providerContextHelper.getImplementationLink(); 
    
    // Find current assembly.
    SCAEnvironment sca = SCAHelper.INSTANCE.getSCAEnvironment();
    ISCAAssembly assembly = sca.getDomainRegistry().getSCAApplication(providerContextHelper.getStaticContext().getTargetApplicationName());
    if (assembly == null) {
      throw new RuntimeProcessException("Can not find corresponding assembly.");
    }
    
    // Get our component.
    IComponentArtifact component = assembly.getDomainComponent(implLink.getProperty(WebServicePluginConstants.SCA_EJB_SCA_COMPONENT_NAME));
    if (component == null)
      throw new RuntimeProcessException("Can not find corresponding component.");    
    
    // Get our service.
    IServiceArtifact service = component.getService(implLink.getProperty(WebServicePluginConstants.SCA_SERVICE_NAME));
    if (service == null)
      throw new RuntimeProcessException("Can not find corresponding service.");
    
    NameConverter converter = NameConverter.CONVERTER;
    Operation operation = null;   
      
    // Find operation to invoke from our component and service.
    List<Operation> operations = service.getInterfaceMetaData().getOperations();
    Iterator<Operation> q = operations.iterator();
    while (q.hasNext()) {	  
      Operation o = q.next(); 
      if (converter.toVariableName(o.getName()).equals(converter.toVariableName(aOperation))) {
	operation = o;
	    
	break;
      }
    }	
    
    if (operation == null)
      throw new RuntimeProcessException("Can not find corresponding operation.");
    
    // Get corresponding binding for operation.
    QName componentAddress = component.getAddress();
    String lookupString = service.getService().getName() + "@" + componentAddress.getLocalPart();
    BindingContext bc = WebServicePlugin.getPublishedBinding(new QName(componentAddress.getNamespaceURI(), lookupString));
    if (bc == null)
      throw new RuntimeProcessException(new NullPointerException("Can not find BindingContext for service with name " + lookupString + "."));
    
    // Add headers to call entry.
    entry.setHeader("SCA Component name", componentAddress.getLocalPart());
    entry.setHeader("SCA Service name", service.getService().getName());
    entry.setHeader("SCA Service operation", operation.getName());    
    
    // Get proxy for invokation.
    ServiceInstance proxy = bc.getServiceInstance(null);
    
    // Send message and get back the result.
    Message m = this.buildMessage(operation, aValues, providerContextHelper);
        
    LogRecord log = iLocation.debugT(m.toString());
    if (log != null)
      entry.setInboundPayloadTraceID(log.getId().toString());
    
    Result res = proxy.accept(m);
    
    log = iLocation.debugT(res.toString());
    if (log != null)
      entry.setInboundPayloadTraceID(log.getId().toString());
    
    if (res.getException() != null) {
      throw new InvocationTargetException(res.getException());
    }
    
    if (res.isValue() && Boolean.parseBoolean((String) m.getValue(OperationContext.HOLDER_VALUE)))
      this.checkForHolderValue(m, aValues);

    return res.getValue();
  }

  public void notify(EventObject aEvent, ConfigurationContext aContext)
  	throws RuntimeProcessException {
    // Notification is send when app stop.
  }
  
  private HelperContext processImplementationManagedCase(ProviderContextHelper aContext) throws RuntimeProcessException, 
  												ClassNotFoundException, 
  												InvocationTargetException {
    
    ImplementationLink implLink = aContext.getImplementationLink();    
    String lookupString = 	"ejb:/appName=" + 
    				aContext.getStaticContext().getTargetApplicationName() + 
    				", jarName=" + 
    				implLink.getProperty(WebServicePluginConstants.SCA_EJB_JAR_NAME) + 
    				", beanName=" + 
    				implLink.getProperty(WebServicePluginConstants.SCA_EJB_CLASS_NAME) + 
    				", interfaceName=" + 
    				implLink.getProperty(WebServicePluginConstants.SCA_EJB_INTERFACE_NAME);    
    
    // Lookup ejb from naming.
    Object ejb = null;
    try {
      InitialContext context = new InitialContext();
      ejb = context.lookup(lookupString);
    } catch (NamingException ne) {
      throw new RuntimeProcessException("Can not lookup ejb " + lookupString + " from naming.", ne);
    }

    // Put ejb lookup string and ejb object to context.
    ConfigurationContext c = aContext.getDynamicContext();
    c.setProperty(OperationContext.EJB_LOOKUP_STRING, lookupString);
    c.setProperty(OperationContext.EJB_OBJECT, ejb);
    
    // Invoke ejb's getHelperContext() method.
    Object returnObject = null;
    try {
      Method method = ejb.getClass().getMethod(WebServicePluginConstants.SA_GET_HELPER_CONTEXT_METHOD_NAME, new Class[]{});
      returnObject = method.invoke(ejb, new Object[]{});
    } catch (SecurityException se) {
      throw new InvocationTargetException(se, "Can not invoke getHelperContext() method.");
    } catch (NoSuchMethodException nsme) {
      throw new InvocationTargetException(nsme, "Can not invoke getHelperContext() method.");
    } catch (IllegalAccessException iae) {
      throw new InvocationTargetException(iae, "Can not invoke getHelperContext() method.");
    }

    if (returnObject == null)
      throw new RuntimeProcessException(new NullPointerException("Could not get HelperContext value from SDO based web service."));
    
    if (!(returnObject instanceof HelperContext))
      throw new RuntimeProcessException(new ClassCastException("Return value from SDO based web service is not an instance of HelperContext."));
    
    return (HelperContext) returnObject;
  }
  
  private HelperContext processContainerManagedCase(ProviderContextHelper aContext) throws 	RuntimeProcessException, 
  												WSDLException, 
  												TransformerFactoryConfigurationError, 
  												TransformerException, 
  												IOException {
    String serviceId = aContext.getImplementationLink().getProperty(WebServicePluginConstants.SERVICE_IDENTIFIER_STR);

    return (HelperContext) WebServiceRepository.getInstance().getHelperContext(serviceId);
  }
  
  private Message buildMessage(Operation aOperation, Object[] aValues, ProviderContextHelper aContextHelper) {
    // Create a message.
    OperationContext operationContext = new OperationContext();
    if (aOperation.getParameters().size() > 0) {
      Value[] values = new Value[aValues.length];
      for (int j = 0; j < aValues.length; ++ j) {
	Object o = aValues[j];
	if (o instanceof ObjectHolder) {
	  Holder h = new Holder(((ObjectHolder) o).value);
	  values[j] = new Value(h);
	  
	  operationContext.put(OperationContext.HOLDER_VALUE, "True");
	} else	
	  values[j] = new Value(aValues[j]);
      }	      
    	
      operationContext.put(OperationContext.VALUE, values);
    } else
      // Case operation with no input params.
      operationContext.put(OperationContext.VALUE, new Value[0]);
    
    // Get lookup string and ejb object from context and pass it to implementation.
    ConfigurationContext c = aContextHelper.getDynamicContext();
    Object obj = null;
    obj = c.getProperty(OperationContext.EJB_LOOKUP_STRING);
    if (obj != null)
      operationContext.put(OperationContext.EJB_LOOKUP_STRING, obj);
    
    obj = c.getProperty(OperationContext.EJB_OBJECT);
    if (obj != null)
      operationContext.put(OperationContext.EJB_OBJECT, obj);
    
    operationContext.put(OperationContext.EJB_CONTEXT, (ConfigurationContext) aContextHelper);
    
    return new Message(aOperation, operationContext);    
  }
  
  private void checkForHolderValue(Message aMessage, Object[] aValue) throws RuntimeProcessException {
    Value[] v = aMessage.getValues();
    
    if (v.length != aValue.length)
      throw new RuntimeProcessException("Could not match holder values, because of different array sizes.");
    
    for (int i = 0; i < aValue.length; ++ i) {
      Object o = v[i].getValue();
      
      if (o instanceof Holder && aValue[i] instanceof ObjectHolder)
	((ObjectHolder) aValue[i]).value = ((Holder) o).value;
      else
	throw new RuntimeProcessException("Could not set a holder value.");
    }
  }
}
