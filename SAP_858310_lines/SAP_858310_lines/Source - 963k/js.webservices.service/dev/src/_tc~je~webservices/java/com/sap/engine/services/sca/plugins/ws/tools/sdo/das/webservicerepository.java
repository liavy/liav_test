package com.sap.engine.services.sca.plugins.ws.tools.sdo.das;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.osoa.sca.ServiceRuntimeException;
import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TBindingOperation;
import org.xmlsoap.schemas.wsdl.TBindingOperationMessage;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TParam;
import org.xmlsoap.schemas.wsdl.TPart;
import org.xmlsoap.schemas.wsdl.TPort;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.TService;
import org.xmlsoap.schemas.wsdl.TTypes;
import org.xmlsoap.schemas.wsdl.soap.TAddress;
import org.xmlsoap.schemas.wsdl.soap.TBody;

import com.sap.engine.interfaces.sca.SCAEnvironment;
import com.sap.engine.interfaces.sca.SCAHelper;
import com.sap.engine.interfaces.sca.assembly.IIncomingBinding;
import com.sap.engine.interfaces.sca.runtime.ServiceInstance;
import com.sap.engine.interfaces.sca.spi.BindingContext;
import com.sap.engine.interfaces.sca.spi.PluginException;
import com.sap.engine.interfaces.sca.wire.InterfaceMetadata;
import com.sap.engine.interfaces.sca.wire.Message;
import com.sap.engine.interfaces.sca.wire.Operation;
import com.sap.engine.interfaces.sca.wire.Parameter;
import com.sap.engine.interfaces.sca.wire.Result;
import com.sap.sdo.api.helper.SapXmlHelper;
import com.sap.sdo.api.types.PropertyConstants;
import com.sap.sdo.api.types.TypeConstants;
import com.sap.sdo.api.util.URINamePair;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;

public class WebServiceRepository {

  private final Map<String, Object> iServiceNameToHc = new ConcurrentHashMap<String, Object>(5);
  private final Map<QName, BindingContext> iServiceNameToAssembly = new ConcurrentHashMap<QName, BindingContext>(5);
  private final Map<String, QName> iSoapActionToOperationName = new ConcurrentHashMap<String, QName>(5);
  
  private static final WebServiceRepository _instance = new WebServiceRepository();
    
  public static String SOAP = "Soap";
  public static String IN = "In";
  public static String OUT = "Out";
  public static String RESPONSE = "Response";
  public static String SOAP_HTTP = "http://schemas.xmlsoap.org/soap/http";
    
  /**
   * TODO Should it be really fixed?
   */
  public static String NAMESPACE_BASE = "http://ns.sap.com/";
    
  private WebServiceRepository() {
    super();    
  }
		
  public static WebServiceRepository getInstance() {
    return _instance;
  }
	
  /* (non-Javadoc)
   * @see com.sap.sca.prototype.bindings.ws.IWebServiceRepository#publish(org.osoa.sca.CompositeContext, org.osoa.sca.sdo.Service, java.lang.String)
   */
  public void publish(BindingContext aBinding) throws PluginException {
    String appName = aBinding.getIncomingBinding().getAssembly().getAppName();
    SCAEnvironment env = SCAHelper.INSTANCE.getSCAEnvironment();
    try {      
      QName bindingAddress = aBinding.getIncomingBinding().getAddress();
      ObjectInputStream in = env.getSerializedWsdl(appName);
      
      Object obj = in.readObject();
      Boolean b = (Boolean) obj;            
      
      if (b) {
        // Read service name to wsdl map.
        obj = in.readObject();
        Map<String, Object> serviceNameToHc = (Map<String, Object>) obj;
  
        // Read service name to ID map.
        obj = in.readObject();
        Map<String, String> serviceNameToID = (Map<String, String>) obj;
        
        String serviceName = bindingAddress.getLocalPart();
        if (serviceNameToID.containsKey(serviceName) && serviceNameToHc.containsKey(serviceName))
          iServiceNameToHc.put(serviceNameToID.get(serviceName), serviceNameToHc.get(serviceName));        
      }
      
      // Put binding in maps.      
      iServiceNameToAssembly.put(bindingAddress, aBinding);
      
      if (in != null) {
	in.close();
      }
    } catch (FileNotFoundException fnfe) {
      throw new PluginException("Can not serialize wsdl for application " + appName + " from file. " ,fnfe);
    } catch (IOException ioe) {
      throw new PluginException("Can not serialize wsdl for application " + appName + " from file. " ,ioe);
    } catch (ClassNotFoundException cnfe) {
      throw new PluginException("Error during serialization of wsdl for application " + appName, cnfe);
    }
  }
	
  public BindingContext getPublishedBinding(QName aKey) {
    return iServiceNameToAssembly.get(aKey);
  }
    
//    /* (non-Javadoc)
//	 * @see com.sap.sca.prototype.bindings.ws.IWebServiceRepository#fire(com.sap.sca.prototype.bindings.ws.WebServiceRepository.CompositeServiceName, com.sap.sca.prototype.wireFormats.InvocationRequest)
//	 */
//    public InvocationResult fire(CompositeServiceName compositeServiceName, InvocationRequest request) {
//        
//        System.out.println("WebServicePublisher.fire: " + compositeServiceName);
//        CompositeContext compositeContext = _compositeNameToCompositeContext.get(compositeServiceName.getCompositeName());
//        Runtime.newThread(compositeContext);
//        
//        String componentName = _serviceNameToComponentName.get(compositeServiceName);
//        GenericClientInterface component = compositeContext.locateService(null, componentName);
//        ((CompositeContextImpl)compositeContext).setRequestContext(new RequestContextImpl(request,(ServiceReference)component));
//        InvocationResult result = component.invoke(request);
//		return result;
//	}
    
    /* (non-Javadoc)
	 * @see com.sap.sca.prototype.bindings.ws.IWebServiceRepository#getWsdl(com.sap.sca.prototype.bindings.ws.WebServiceRepository.CompositeServiceName)
	 */
  public Object getHelperContext(String aServiceID) {
    return iServiceNameToHc.get(aServiceID);
  }
    
  public QName getCompositeServiceOperationName(String soapAction) {
    return iSoapActionToOperationName.get(soapAction);
  }

  public Operation getCompositeServiceOperation(QName operation) {
    String nuri = operation.getNamespaceURI();
    int ind = nuri.lastIndexOf("/");
    QName service = new QName(nuri.substring(0,ind),nuri.substring(ind+1));
    IIncomingBinding ib = iServiceNameToAssembly.get(service).getIncomingBinding();
    for ( Operation op : ib.getInterfaceMetadata().getOperations() ) {
      if (op.getName().equals(operation.getLocalPart())) {
	return op;
      }
    }
    return null;
  }

  public byte[] generateWsdl(final InterfaceMetadata imd, final String nameSpace, final String soapActionName, String decodedPortStr) {
    Port decodedPort = new Port(decodedPortStr);
    TDefinitions definitions = (TDefinitions)DataFactory.INSTANCE.create(TDefinitions.class);        
    definitions.setTargetNamespace(nameSpace);
        
    TTypes types = (TTypes)DataFactory.INSTANCE.create(TTypes.class);
    definitions.getTypes().add(types);
    
    String serviceSoap = decodedPort.getServiceName() + SOAP;
    TPortType portType = (TPortType)DataFactory.INSTANCE.create(TPortType.class);
    portType.setName(serviceSoap);

    QName tnsServiceSoap = getQName(nameSpace, serviceSoap);
    TBinding binding = (TBinding)DataFactory.INSTANCE.create(TBinding.class);
    binding.setName(serviceSoap);
    binding.setTBindingType(tnsServiceSoap);
        
    

        org.xmlsoap.schemas.wsdl.soap.TBinding soapBinding = (org.xmlsoap.schemas.wsdl.soap.TBinding)DataFactory.INSTANCE.create(org.xmlsoap.schemas.wsdl.soap.TBinding.class);
        soapBinding.setTransport(SOAP_HTTP);
        
        Property soapBindingPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)soapBinding).getType().getURI(), "binding");
       
        List list = (List)((DataObject)binding).get(soapBindingPropterty);
        list.add(soapBinding);
        ((DataObject)binding).set(soapBindingPropterty,list);
        
        
        TService service = (TService)DataFactory.INSTANCE.create(TService.class);
        service.setName(decodedPort.getServiceName());
        
        TPort port = (TPort)DataFactory.INSTANCE.create(TPort.class);
        port.setName(decodedPort.getPortName());
        port.setBinding(tnsServiceSoap);
        service.getPort().add(port);
        
        TAddress address = (TAddress)DataFactory.INSTANCE.create(TAddress.class);
        address.setLocation(decodedPort.getUri());
        
        Property addressPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)address).getType().getURI(), "address");
        
        //((DataObject)port).set(addressPropterty, address);
        
        list = (List)((DataObject)port).get(addressPropterty);
        list.add(address);
        ((DataObject)port).set(addressPropterty,list);
        

        List<Operation> methods = imd.getOperations();
        Set<String> nameSpaces = new HashSet<String>();
        nameSpaces.add(TypeHelper.INSTANCE.getType(GenericError.class).getURI());
        //nameSpaces.add(nameSpace);
        Set<Class> allExceptions = new HashSet<Class>();
        for (Operation method : methods) {
            defineTypes(method, nameSpace, nameSpaces);
            
            String operationName = method.getName();
            //Class[] exceptions = method.getExceptionTypes(); we Do not support exceptions
            
            TMessage messageIn = (TMessage)DataFactory.INSTANCE.create(TMessage.class);
            String operationSoapIn = operationName + SOAP + IN;
            messageIn.setName(operationSoapIn);
            TPart partIn = (TPart)DataFactory.INSTANCE.create(TPart.class);
            messageIn.getPart().add(partIn);
            partIn.setName("parameter");
            partIn.setElement(getQName(nameSpace, operationName));
            
            TMessage messageOut = (TMessage)DataFactory.INSTANCE.create(TMessage.class);
            String operationSoapOut = operationName + SOAP + OUT;
            messageOut.setName(operationSoapOut);
            TPart partOut = (TPart)DataFactory.INSTANCE.create(TPart.class);
            messageOut.getPart().add(partOut);
            partOut.setName("parameter");
            partOut.setElement(getQName(nameSpace, operationName + RESPONSE));
            
            List<TMessage> messages = definitions.getMessage();
            messages.add(messageIn);
            messages.add(messageOut);

            
            TOperation operation = (TOperation)DataFactory.INSTANCE.create(TOperation.class);
            portType.getOperation().add(operation);
            operation.setName(operationName);
            TParam operationInput = (TParam)DataFactory.INSTANCE.create(TParam.class);
            operationInput.setMessage(getQName(nameSpace, operationSoapIn));
            operation.setInput(operationInput);
            TParam operationOutput = (TParam)DataFactory.INSTANCE.create(TParam.class);
            operationOutput.setMessage(getQName(nameSpace, operationSoapOut));
            operation.setOutput(operationOutput);

            TBindingOperation bindingOperation = (TBindingOperation)DataFactory.INSTANCE.create(TBindingOperation.class);
            bindingOperation.setName(operationName);
            binding.getOperation().add(bindingOperation);
            
            org.xmlsoap.schemas.wsdl.soap.TOperation soapOperation = (org.xmlsoap.schemas.wsdl.soap.TOperation)DataFactory.INSTANCE.create(org.xmlsoap.schemas.wsdl.soap.TOperation.class);
            String soapAction = nameSpace + operationName;
            soapOperation.setSoapAction(soapAction);
           
            iSoapActionToOperationName.put(soapAction, new QName(soapActionName,operationName));
            
            soapOperation.setStyle("document");
            
            Property soapOperationPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)soapOperation).getType().getURI(), "operation");
            
            //((DataObject)bindingOperation).set(soapOperationPropterty, soapOperation);
            list = (List)((DataObject)bindingOperation).get(soapOperationPropterty);
            list.add(soapOperation);
            ((DataObject)bindingOperation).set(soapOperationPropterty,list);
            
            
            TBindingOperationMessage input = (TBindingOperationMessage)DataFactory.INSTANCE.create(TBindingOperationMessage.class);
            bindingOperation.setInput(input);
            
            TBody inputBody = (TBody)DataFactory.INSTANCE.create(TBody.class);
            inputBody.setUse("literal");
            
            Property inputBodyPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)inputBody).getType().getURI(), "body");
            
            //((DataObject)input).set(inputBodyPropterty, inputBody);
            list = (List)((DataObject)input).get(inputBodyPropterty);
            list.add(inputBody);
            ((DataObject)input).set(inputBodyPropterty,list);            
            
            
            TBindingOperationMessage output = (TBindingOperationMessage)DataFactory.INSTANCE.create(TBindingOperationMessage.class);
            bindingOperation.setOutput(output);
            
            TBody outputBody = (TBody)DataFactory.INSTANCE.create(TBody.class);
            outputBody.setUse("literal");
            
            Property outputBodyPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)outputBody).getType().getURI(), "body");
//            ((DataObject)output).set(outputBodyPropterty, outputBody);
            list = (List)((DataObject)output).get(outputBodyPropterty);
            list.add(outputBody);
            ((DataObject)output).set(outputBodyPropterty,list);              
            
            
//            Map<Class<? extends Throwable>, Class<? extends Throwable>> runtimeExceptionsToDeclaredExceptions
//                = new HashMap<Class<? extends Throwable>, Class<? extends Throwable>>();
//            _operationTosExceptions.put(compositeServiceOperationName, runtimeExceptionsToDeclaredExceptions);
//            for (Class<Throwable> exception: exceptions) {
//                runtimeExceptionsToDeclaredExceptions.put(exception, exception);
//                allExceptions.add(exception);
//                String faultName = exception.getName();
//
//                TFault operationFault = (TFault)DataFactory.INSTANCE.create(TFault.class);
//                operationFault.setName(faultName);
//                operationFault.setMessage(getQName(nameSpace, faultName));
//                operation.getFault().add(operationFault);
//                org.xmlsoap.schemas.wsdl.soap.TFault soapOperationFault = 
//                    (org.xmlsoap.schemas.wsdl.soap.TFault)DataFactory.INSTANCE.create(org.xmlsoap.schemas.wsdl.soap.TFault.class);
//                soapOperationFault.setName(faultName);
//                soapOperationFault.setUse("literal");
//                Property soapOperationFaultPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)soapOperationFault).getType().getURI(), "fault");
//                ((DataObject)operationFault).set(soapOperationFaultPropterty, soapOperationFault);
//                
//                TBindingOperationFault bindingOperationFault = (TBindingOperationFault)DataFactory.INSTANCE.create(TBindingOperationFault.class);
//                bindingOperationFault.setName(faultName);
//                bindingOperation.getFault().add(bindingOperationFault);
//                org.xmlsoap.schemas.wsdl.soap.TFault soapBindingOperationFault = 
//                    (org.xmlsoap.schemas.wsdl.soap.TFault)DataFactory.INSTANCE.create(org.xmlsoap.schemas.wsdl.soap.TFault.class);
//                soapBindingOperationFault.setName(faultName);
//                soapBindingOperationFault.setUse("literal");
//                Property soapBindingOperationFaultPropterty = TypeHelper.INSTANCE.getOpenContentProperty(((DataObject)soapBindingOperationFault).getType().getURI(), "fault");
//                ((DataObject)bindingOperationFault).set(soapBindingOperationFaultPropterty, soapBindingOperationFault);
//                
//            }
        }
        
//        for (Class<? extends Throwable> exception: allExceptions) {
//            Property faultProperty = getErrorElement(exception, nameSpace);
//            
//            TMessage messageFault = (TMessage)DataFactory.INSTANCE.create(TMessage.class);
//            String faultName = exception.getName();
//            messageFault.setName(faultName);
//            definitions.getMessage().add(messageFault);
//            
//            TPart partFault = (TPart)DataFactory.INSTANCE.create(TPart.class);
//            messageFault.getPart().add(partFault);
//            partFault.setName(faultName);
//            String errorNamespace = faultProperty.getType().getURI();
//            nameSpaces.add(errorNamespace);
//            partFault.setElement(getQName(errorNamespace, faultProperty.getName()));
//        }

        DataObject prop =
            DataFactory.INSTANCE.create(URINamePair.PROPERTY.getURI(), URINamePair.PROPERTY.getName());
        prop.set(PropertyConstants.NAME, "schema");
        prop.set(
            PropertyConstants.TYPE,
            TypeHelper.INSTANCE.getType(
                URINamePair.XSD_TYPE.getURI(),
                URINamePair.XSD_TYPE.getName()));
        prop.set(PropertyConstants.MANY, true);
        prop.set(PropertyConstants.CONTAINMENT, true);
        
        Property propterty = TypeHelper.INSTANCE.defineOpenContentProperty(null, prop);
        nameSpaces.remove("commonj.sdo");
        nameSpaces.remove(nameSpace);
        List<String> nsList = new ArrayList<String>(nameSpaces);
        nsList.add(nameSpace);
        ((DataObject)types).setList(propterty, nsList);
        
        definitions.getPortType().add(portType);
        definitions.getBinding().add(binding);
        definitions.getService().add(service);
        
        Map<String, Map> options = new HashMap<String, Map>();
        Map<String,String> uriToPrefix = new HashMap<String,String>();
        options.put(SapXmlHelper.OPTION_KEY_PREFIX_MAP, uriToPrefix);
        uriToPrefix.put(definitions.getTargetNamespace(),"wsdlns");
        uriToPrefix.put("http://schemas.xmlsoap.org/wsdl/soap/","SOAP"); 
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        try {
			XMLHelper.INSTANCE.save(
                XMLHelper.INSTANCE.createDocument((DataObject)definitions, "http://schemas.xmlsoap.org/wsdl/", "definitions"),
                oStream,
                options);
		} catch (IOException e) {
			throw new ServiceRuntimeException("",e);
		}
        System.out.println("Generated WSDL:");
        try {
            System.out.println(oStream.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return oStream.toByteArray();
	}
    
    private QName getQName(String nameSpace, String localPart) {
        return new QName(nameSpace, localPart);
    }
    
    public Result fire(QName service, Message message) {
    	ServiceInstance instance = iServiceNameToAssembly.get(service).getServiceInstance(null);
    	return instance.accept(message);
    }
    
    private Type getType(Parameter parameter) {
        QName typeUri = parameter.getTypeUri();
		URINamePair unp = new URINamePair(typeUri.getNamespaceURI(), typeUri.getLocalPart());
        return TypeHelper.INSTANCE.getType(unp.getURI(),unp.getName());
    }
    
    private void defineTypes(Operation method, String nameSpace, Set<String> nameSpaces) {
        List<Parameter> returnParams = method.getResults();
        Type resultType = null;
        if (returnParams.size()>0) {
        	resultType = getType(returnParams.get(0));
        	recurse(resultType,nameSpaces,new HashSet<Type>());

        }
        String operation = method.getName();
        Type requestType = TypeHelper.INSTANCE.getType(nameSpace, operation);
        if (requestType == null) {
            DataObject responseTypeDO =
                DataFactory.INSTANCE.create(URINamePair.TYPE.getURI(), URINamePair.TYPE.getName());
            responseTypeDO.set(TypeConstants.NAME, operation);
            responseTypeDO.set(TypeConstants.URI, nameSpace);
            List<Parameter> parameterTypes = method.getParameters();
            
            for (int i = 0; i < parameterTypes.size(); i++) {
                Type paramType = getType(parameterTypes.get(i));
                recurse(paramType,nameSpaces,new HashSet<Type>());

                DataObject resultProperty = responseTypeDO.createDataObject(TypeConstants.PROPERTY);
                resultProperty.set(PropertyConstants.NAME, "arg" + i);
                resultProperty.set(PropertyConstants.TYPE, paramType);
                resultProperty.set(PropertyConstants.CONTAINMENT, true);
                resultProperty.set(
                    XSDHelper.INSTANCE.getGlobalProperty("commonj.sdo/xml", "xmlElement", false),
                    true);
            }
            requestType = TypeHelper.INSTANCE.define(responseTypeDO);
            
            DataObject operationProp =
                DataFactory.INSTANCE.create(URINamePair.PROPERTY.getURI(), URINamePair.PROPERTY.getName());
            operationProp.set(PropertyConstants.NAME, operation);
            operationProp.set(PropertyConstants.TYPE, requestType);
            operationProp.set(PropertyConstants.CONTAINMENT, true);
            
            TypeHelper.INSTANCE.defineOpenContentProperty(nameSpace, operationProp);
        }
        
        final String operationResponse = operation + "Response";
        Type responseType = TypeHelper.INSTANCE.getType(nameSpace, operationResponse);
        if (responseType == null) {
            DataObject responseTypeDO =
                DataFactory.INSTANCE.create(URINamePair.TYPE.getURI(), URINamePair.TYPE.getName());
            responseTypeDO.set(TypeConstants.NAME, operationResponse);
            responseTypeDO.set(TypeConstants.URI, nameSpace);
            DataObject resultProperty = responseTypeDO.createDataObject(TypeConstants.PROPERTY);
            if (resultType!=null) {
            	final String operationResult = operation + "Result";
            	resultProperty.set(PropertyConstants.NAME, operationResult);
            	resultProperty.set(PropertyConstants.TYPE, resultType);
            	resultProperty.set(PropertyConstants.CONTAINMENT, true);
            	responseType = TypeHelper.INSTANCE.define(responseTypeDO);

            	DataObject operationResponseProp =
                    DataFactory.INSTANCE.create(URINamePair.PROPERTY.getURI(), URINamePair.PROPERTY.getName());
            	operationResponseProp.set(PropertyConstants.NAME, operationResponse);
            	operationResponseProp.set(PropertyConstants.TYPE, responseType);
            	operationResponseProp.set(PropertyConstants.CONTAINMENT, true);
            
            	TypeHelper.INSTANCE.defineOpenContentProperty(nameSpace, operationResponseProp);
            }
        }
        
    }
    
    private void recurse(Type resultType, Set<String> nameSpaces, HashSet<Type> types) {
    	if (resultType.getURI().startsWith("commonj.sdo")) {
    		return;
    	}
    	if (types.contains(resultType)) {
    		return;
    	}
    	types.add(resultType);
    	String tmpNameSpace = resultType.getURI();
    	if (tmpNameSpace != null) {
    		nameSpaces.add(tmpNameSpace);
    		//TODO add deeper namespaces
    	} else {
//        	throw new IllegalArgumentException("No namespace");                
    	}
		for (Property p: (List<Property>)resultType.getProperties()) {
			recurse(p.getType(),nameSpaces,types);
		}
	}

	private String getNameSpace(QName service) {
        return NAMESPACE_BASE + service + '/';
    }
    
//    private Property getErrorElement(Class<? extends Throwable> exception, String nameSpace) {
//        Class errorInterface;
//        if (DasRuntimeException.class.isAssignableFrom(exception)
//            || DasException.class.isAssignableFrom(exception)) {
//            Set<Class> interfaces = new HashSet<Class>();
//            for (Constructor constructor: exception.getConstructors()) {
//                for (Class parameterType: constructor.getParameterTypes()) {
//                    if (parameterType.isInterface()) {
//                        interfaces.add(parameterType);
//                    }
//                }
//            }
//            if (interfaces.size() != 1) {
//                throw new IllegalArgumentException("Exception is ambiguous: " + interfaces);
//            }
//            errorInterface = interfaces.iterator().next();
//                
//        } else {
//            errorInterface = GenericError.class;
//        }
//        
//        Type type = TypeHelper.INSTANCE.getType(errorInterface);
//        DataObject faultProp = DataFactory.INSTANCE.create(PropertyConstants.getInstance());
//        faultProp.set(PropertyConstants.NAME, exception.getName());
//        faultProp.set(PropertyConstants.TYPE, type);
//        faultProp.set(PropertyConstants.CONTAINMENT, true);
//
//        String uri = type.getURI();
//        if (uri == null) {
//            uri = nameSpace;
//        }
//        return TypeHelper.INSTANCE.defineOpenContentProperty(uri, faultProp);
//    }
    
    /**
     * Finds the declared Exception for the currently thrown exception.
     * The exception that is thrown at runtime can be more concrete than the
     * declared exception at the interface. This method tries to find the
     * matching declared exeption of the operation. If there is no matching
     * exeption because it is a non-declared RuntimeException the result is null.
     * @param pCompositeServiceOperationName The key for the operation.
     * @param pRuntimeException The current exception.
     * @return The matching declared exception or null.
     */
//    private Class<? extends Throwable> getDeclaredException(CompositeServiceOperationName pCompositeServiceOperationName, Class<? extends Throwable> pRuntimeException) {
//        Map<Class<? extends Throwable>, Class<? extends Throwable>> runtimeExceptionsToDeclaredExceptions
//            = _operationTosExceptions.get(pCompositeServiceOperationName);
//        Class<? extends Throwable> declaredException = runtimeExceptionsToDeclaredExceptions.get(pRuntimeException);
//        if ((declaredException != null) || runtimeExceptionsToDeclaredExceptions.containsKey(pRuntimeException)) {
//            return declaredException;
//        }
//        for (Class<? extends Throwable> exception: new HashSet<Class<? extends Throwable>>(runtimeExceptionsToDeclaredExceptions.values())) {
//            if (exception.isAssignableFrom(pRuntimeException)) {
//                declaredException = exception;
//                break;
//            }
//        }
//        runtimeExceptionsToDeclaredExceptions.put(pRuntimeException, declaredException);
//        return declaredException;
//    }
    
    private class Port {
        private static final String WSDL_ENDPOINT = "#wsdl.endpoint";
        private String _uri;
        private String _serviceName;
        private String _portName;
        
        public String getPortName() {
            return _portName;
        }
        public void setPortName(String pPortName) {
            _portName = pPortName;
        }
        public String getServiceName() {
            return _serviceName;
        }
        public void setServiceName(String pServiceName) {
            _serviceName = pServiceName;
        }
        public String getUri() {
            return _uri;
        }
        public void setUri(String pUri) {
            _uri = pUri;
        }
        
        public Port(QName address) {
    		final String host_port = getHostAndHttpPort() ;
        		
        		
    		final String alias = "wsapp";
        		
				// TODO:  How can we determine our http port?
			setUri("http://"+host_port + "/" + alias + "/servlet/"
					+address.getNamespaceURI()+"/"
					+address.getLocalPart());
			setServiceName(address.getLocalPart());
			setPortName(address.getLocalPart()+"Port");
        }
        
        public Port(String pPortUri) {
            int hashSign = pPortUri.lastIndexOf(WSDL_ENDPOINT);
            setUri(pPortUri.substring(0, hashSign));
            String endpoint = pPortUri.substring(0, hashSign);
            //String endpoint = pPortUri.substring(hashSign + WSDL_ENDPOINT.length());
            StringTokenizer stringTokenizer = new StringTokenizer(endpoint, "/");
            setServiceName(stringTokenizer.nextToken());
            setPortName(stringTokenizer.nextToken());
            if (stringTokenizer.hasMoreTokens()) {
                throw new ServiceRuntimeException("Can not parse port entpoint: " + endpoint);
            }
        }
    }
    
    
    private static String getHostAndHttpPort() {
//		try {
//		    ShmAccessPoint[] accessPoints = 
//		      ShmAccessPoint.getAllAccessPoints(ShmAccessPoint.PID_HTTP);
//		    if (accessPoints.length > 0) {
//		      return "" + accessPoints[0].getAddress().getHostName() + ":" + accessPoints[0].getPort();
//		    }
            throw new ServiceRuntimeException("Can not detect http port. There is not http access point for this server node");
//		} catch (ShmException e) {
//            throw new ServiceRuntimeException("Can not detect http port ",e);
//		}    	
    }
}
