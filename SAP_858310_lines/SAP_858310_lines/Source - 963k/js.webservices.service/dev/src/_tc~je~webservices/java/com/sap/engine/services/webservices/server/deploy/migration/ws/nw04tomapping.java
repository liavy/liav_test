package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import com.sap.engine.lib.descriptors.ws04vi.FaultState;
import com.sap.engine.lib.descriptors.ws04vi.FunctionSoapExtensionFunction;
import com.sap.engine.lib.descriptors.ws04vi.FunctionState;
import com.sap.engine.lib.descriptors.ws04vi.ParameterMappedTypeReference;
import com.sap.engine.lib.descriptors.ws04vi.ParameterState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceSoapExtensionVI;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.FaultConfigDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.ImplLinkDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.NameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.OperationConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.QNameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor.Choice1;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeConfigurationDescriptor;
import com.sap.engine.services.webservices.additions.soaphttp.TransportBindingIDs;
import com.sap.engine.services.webservices.espbase.mappings.EJBImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.mappings.PropertyType;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.runtime.definition.EJBImplConstants;
import com.sap.engine.services.webservices.runtime.wsdl.SchemaConvertor;
import com.sap.engine.services.webservices.runtime.wsdl.StandardTypes;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Mapping objects loaded from Vitural Interface and ws-deployment-descriptor.
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */
public class NW04toMapping {
  
  private final String EMPTY_STRING = "";
  private static final Location LOCATION = Location.getLocation(NW04toMapping.class); 
  protected Hashtable operationProperties;
  
  public NW04toMapping() {
    operationProperties = new Hashtable();
  }
  
  public InterfaceMapping[] loadInterface(Hashtable vi_wsdStructures, WSDescriptor[] nw04Webservice, WSRuntimeConfigurationDescriptor[] runtimeConfDescriptor, String applicationName) throws Exception {
    Hashtable runtimeConfiguration = new Hashtable();      
    if (runtimeConfDescriptor != null) {
	  for (int i = 0; i < runtimeConfDescriptor.length; i++) {
 		com.sap.engine.lib.descriptors.ws04wsrt.QNameDescriptor serviceEndpointName = runtimeConfDescriptor[i].getServiceEndpointName();
        runtimeConfiguration.put(serviceEndpointName.getNamespaceURI() + "_" + serviceEndpointName.getLocalName(), runtimeConfDescriptor[i]);
	  }
    } 

	WSConfigurationDescriptor[] configDescriptor = null;
	Vector interfaceMap = new Vector();
	InterfaceMapping interfaceMapping;
	for (int i = 0; i < nw04Webservice.length; i++) {
	  configDescriptor = nw04Webservice[i].getWsConfiguration();		
	  for (int j = 0; j < configDescriptor.length; j++) {		
        QNameDescriptor enpointName = configDescriptor[j].getServiceEndpointName();  
		NameDescriptor name =  configDescriptor[j].getServiceEndpointViRef();	
		// gets the data from the hashtable by the vi file name	  		
		VirtualInterfaceState viStructure = ((VirtualInterfaceState)vi_wsdStructures.get(name.getName()));
        
        name =  configDescriptor[j].getWebserviceDefinitionRef(); 
        WebServiceDefinitionState wsdStructure = ((WebServiceDefinitionState)vi_wsdStructures.get(name.getName()));	  
		    //this should not happen
        if (viStructure == null) viStructure = new VirtualInterfaceState();                           
		interfaceMapping = new InterfaceMapping();
        			
        String bindingType = getBindingType(configDescriptor[j]);  
             
        String interfaceMappingId = applicationName + "_" + nw04Webservice[i].getWebserviceInternalName() + "_" + enpointName.getLocalName();			
		interfaceMapping.setProperty(loadInterfaceMappingProperty(interfaceMappingId, bindingType));		  
		interfaceMapping.setOperation(loadOperation(viStructure.getVirtualInterfaceFunctions().getFunction(), "urn:" + wsdStructure.getName() + "/" + viStructure.getName(), viStructure.getVirtualInterfaceSoapExtensionVI() , getViTypes(viStructure), configDescriptor[j].getOperationConfiguration()));		  
		interfaceMapping.setImplementationLink(loadInterfaceImplementationLink(configDescriptor[j], (WSRuntimeConfigurationDescriptor)runtimeConfiguration.get(enpointName.getNamespaceURI() + "_" + enpointName.getLocalName())));
		interfaceMap.addElement(interfaceMapping);		 
	 }
   }

	InterfaceMapping[] im = new InterfaceMapping[interfaceMap.size()];
    interfaceMap.copyInto(im);
	return im;
  }
	
  private String getBindingType(WSConfigurationDescriptor configDescriptor) {
    String bindingType = configDescriptor.getTransportBinding().getName();
    if (TransportBindingIDs.SOAPHTTP_TRANSPORTBINDING.equals(bindingType)) {
      bindingType = InterfaceMapping.SOAPBINDING; 
    } else if (TransportBindingIDs.MIME_TRANSPORTBINDING.equals(bindingType)) {
      bindingType = InterfaceMapping.MIMEBINDING; 
    } else if (TransportBindingIDs.HTTP_TRANSPORTBINDING.equals(bindingType)) {
      PropertyDescriptor[] propsDescriptor = configDescriptor.getTransportBinding().getProperty();
      if (propsDescriptor != null) {
        for (int k = 0; k < propsDescriptor.length; k++) {
          if ("method".equals(propsDescriptor[k].getName())) {
            if ("GET".equals(propsDescriptor[k].getValue())) {
              bindingType = InterfaceMapping.HTTPGETBINDING;
            } else if ("POST".equals(propsDescriptor[k].getValue())) {
              bindingType = InterfaceMapping.HTTPPOSTBINDING;
            }
          }
        }
      }
    }
    return bindingType;
  }

  
  private PropertyType[] loadInterfaceMappingProperty(String mappingId, String bindingType) {	
	PropertyType[] pType = new PropertyType[2];
	pType[0] = getPropertyType(InterfaceMapping.INTERFACE_MAPPING_ID, mappingId);
	pType[1] = getPropertyType(InterfaceMapping.BINDING_TYPE, bindingType);
	return pType;
  }
	
  private OperationMapping[] loadOperation(FunctionState[] functions, String faultNamespace, VirtualInterfaceSoapExtensionVI viSoapExtenstionVi, HashMap viTypes, OperationConfigurationDescriptor[] operationConfig) throws Exception { 
    Hashtable operations = new Hashtable();
    if (operationConfig != null) {
      for (int i = 0; i < operationConfig.length; i++) {
        if (operationConfig[i].getName() != null) {
	      operations.put(operationConfig[i].getName(), operationConfig[i]);
        }     	
	    if (operationConfig[i].getUniqueViName() != null) {
		  operations.put(operationConfig[i].getUniqueViName(), operationConfig[i]);
		}		   
	  }
    }
  	  
	OperationMapping[] operationMapping = new OperationMapping[(functions == null) ? 0 : functions.length];  
	for (int i = 0; i < operationMapping.length; i++) {
	  operationMapping[i] = new OperationMapping();
	  operationMapping[i].setProperty(loadOperationMappingProperty(functions[i], viSoapExtenstionVi, (OperationConfigurationDescriptor)operations.get(functions[i].getName())));
      
      boolean needNamespace = false;
      String tmpNamespace = null;     
      if (viSoapExtenstionVi != null) {
        needNamespace = viSoapExtenstionVi.getSoapExtensionVI().isUseNamespaces();
      }

      if(needNamespace == true) {
        FunctionSoapExtensionFunction soapExtensionFunction = functions[i].getFunctionSoapExtensionFunction();
        if (soapExtensionFunction != null) {
          tmpNamespace = soapExtensionFunction.getSoapExtensionFunction().getNamespace().trim();
        }
        operationMapping[i].setParameter(loadOperationParameter(functions[i], faultNamespace, viTypes, tmpNamespace, (OperationConfigurationDescriptor)operations.get(functions[i].getName())));
      } else {
        operationMapping[i].setParameter(loadOperationParameter(functions[i], faultNamespace, viTypes, null, (OperationConfigurationDescriptor)operations.get(functions[i].getName())));
      }
	}
	return operationMapping;
  }
	
  private PropertyType[] loadOperationMappingProperty(FunctionState function, VirtualInterfaceSoapExtensionVI viSoapExtenstionVi, OperationConfigurationDescriptor operationConfigDescriptor) {	  
    Vector pTmpType = new Vector();
	pTmpType.addElement(getPropertyType(OperationMapping.WSDL_OPERATION_NAME, function.getNameMappedTo().trim()));
	pTmpType.addElement(getPropertyType(OperationMapping.JAVA_METHOD_NAME, function.getOriginalName().trim()));	 
    
    //property that comes from operation-configuration features
    PropertyType oneWayProperty = (PropertyType)operationProperties.get(function.getOriginalName().trim());
    if (oneWayProperty != null) {
      pTmpType.addElement(oneWayProperty);
    } 
    
	FunctionSoapExtensionFunction soapExtensionFunction = function.getFunctionSoapExtensionFunction();
	if (soapExtensionFunction != null) {
	  pTmpType.addElement(getPropertyType(OperationMapping.INPUT_NAMESPACE, soapExtensionFunction.getSoapExtensionFunction().getNamespace().trim()));
	  pTmpType.addElement(getPropertyType(OperationMapping.OUTPUT_NAMESPACE, soapExtensionFunction.getSoapExtensionFunction().getNamespace().trim()));
	  pTmpType.addElement(getPropertyType(OperationMapping.SOAP_REQUEST_WRAPPER, soapExtensionFunction.getSoapExtensionFunction().getSOAPRequestName().trim()));
	  pTmpType.addElement(getPropertyType(OperationMapping.SOAP_RESPONSE_WRAPPER, soapExtensionFunction.getSoapExtensionFunction().getSOAPResponseName().trim()));
	}	
	 
	if (operationConfigDescriptor != null) {
	  PropertyDescriptor[] operationOutputProperty = operationConfigDescriptor.getTransportBindingConfiguration().getOutput().getProperty();
	  if (operationOutputProperty != null) {
	    for (int i = 0; i < operationOutputProperty.length; i++) {
	      if (MigrationConstants.OMIT_PART_WRAPPER.equals(operationOutputProperty[i].getName())) {
			pTmpType.addElement(getPropertyType(OperationMapping.OMIT_RESPONSE_PART_WRAPPER, operationOutputProperty[i].getValue().trim()));
		  }	else if (MigrationConstants.NAMESPACE.equals(operationOutputProperty[i].getName())) {
		    pTmpType.addElement(getPropertyType(OperationMapping.OUTPUT_NAMESPACE, operationOutputProperty[i].getValue().trim()));
		  }
		}
	  }
		
	  PropertyDescriptor[] operationInputProperty = operationConfigDescriptor.getTransportBindingConfiguration().getInput().getProperty();
	  if (operationInputProperty != null) {
	    for (int i = 0; i < operationInputProperty.length; i++) {		
	      if (MigrationConstants.SOAP_ACTION.equals(operationInputProperty[i].getName())) {
	        pTmpType.addElement(getPropertyType(OperationMapping.SOAP_ACTION, operationInputProperty[i].getValue().trim()));			   
	      }	else if (MigrationConstants.NAMESPACE.equals(operationInputProperty[i].getName())) {
			pTmpType.addElement(getPropertyType(OperationMapping.INPUT_NAMESPACE, operationInputProperty[i].getValue().trim()));
	      }
	    }
	  }
	}

	PropertyType[] pType = new PropertyType[pTmpType.size()]; 
    pTmpType.copyInto(pType);  
	return pType;
  }
		
  private ParameterMapping[] loadOperationParameter(FunctionState function, String faultNamespace, HashMap viTypes, String namespace, OperationConfigurationDescriptor operationConfigDescriptor) throws Exception {
    Vector pMapping = new Vector();

	ParameterState[] incoming = null;
	if (function.getFunctionIncomingParameters() == null) {
	  incoming = new ParameterState[0];
	} else {
      incoming = function.getFunctionIncomingParameters().getParameter();
    }
	for (int k = 0; k < incoming.length; k++) {
	  ParameterMapping parameter = new ParameterMapping();
      Vector pTmpType = loadParameterProperty(incoming[k], function.getFunctionSoapExtensionFunction(), k, ParameterMapping.IN_TYPE, viTypes, namespace);
      PropertyDescriptor[] input = operationConfigDescriptor.getTransportBindingConfiguration().getInput().getProperty();
      for (int i = 0; i < input.length; i++) {
        if (MigrationConstants.ATTACHMENT_PARTS.equals(input[i].getName())) {        
          Choice1 attachment_choice = input[i].getChoiceGroup1();
          if (attachment_choice != null) {
            PropertyDescriptor[] attachment_property = attachment_choice.getProperty();
            for (int j = 0; j < attachment_property.length; j++) {            
              if (incoming[k].getNameMappedTo().equals(attachment_property[j].getName())) {
                pTmpType.addElement(getPropertyType(ParameterMapping.IS_ATTACHMENT, "true"));
                PropertyDescriptor[] property = attachment_property[j].getChoiceGroup1().getProperty();
                for (int index = 0; index < property.length; index++) {
                  if(MigrationConstants.CONTENT_TYPE.equals(property[index].getName())) {
                    pTmpType.addElement(getPropertyType(ParameterMapping.ATTACHMENT_CONTENT_TYPE, property[index].getValue()));
                  } else if (MigrationConstants.CONTENT_TRANSFER_ENCODING.equals(property[index].getName())) {
                    pTmpType.addElement(getPropertyType(ParameterMapping.ATTACHMENT_TRANSFER_ENCODING, property[index].getValue()));
                  }
                }
              }
            }
          }
        }
      }
    
      PropertyType[] pType = new PropertyType[pTmpType.size()];
      pTmpType.copyInto(pType);
      parameter.setProperty(pType);
         	
	  pMapping.addElement(parameter);
	  }
	    	  
	  ParameterState[] outgoing = null;
	  if (function.getFunctionOutgoingParameters() == null) {
		outgoing = new ParameterState[0];
	  } else {
        outgoing = function.getFunctionOutgoingParameters().getParameter();
    }
    for (int k = 0; k < outgoing.length; k++) {
	  ParameterMapping parameter = new ParameterMapping();
      Vector pTmpType = loadParameterProperty(outgoing[k], function.getFunctionSoapExtensionFunction(), k, ParameterMapping.RETURN_TYPE, viTypes, namespace);
      
      PropertyDescriptor[] output = operationConfigDescriptor.getTransportBindingConfiguration().getOutput().getProperty();
      for (int i = 0; i < output.length; i++) {
        if (MigrationConstants.ATTACHMENT_PARTS.equals(output[i].getName())) {        
          Choice1 attachment_choice = output[i].getChoiceGroup1();
          if (attachment_choice != null) {
            PropertyDescriptor[] attachment_property = attachment_choice.getProperty();
            for (int j = 0; j < attachment_property.length; j++) {            
              if (outgoing[k].getNameMappedTo().equals(attachment_property[j].getName())) {
                pTmpType.addElement(getPropertyType(ParameterMapping.IS_ATTACHMENT, "true"));
                PropertyDescriptor[] property = attachment_property[j].getChoiceGroup1().getProperty();
                for (int index = 0; index < property.length; index++) {
                  if(MigrationConstants.CONTENT_TYPE.equals(property[index].getName())) {
                    pTmpType.addElement(getPropertyType(ParameterMapping.ATTACHMENT_CONTENT_TYPE, property[index].getValue()));
                  } else if (MigrationConstants.CONTENT_TRANSFER_ENCODING.equals(property[index].getName())) {
                    pTmpType.addElement(getPropertyType(ParameterMapping.ATTACHMENT_TRANSFER_ENCODING, property[index].getValue()));
                  }
                }
              }
            }
          } 
        }
      }
      
      if (pTmpType.size() != 0) {
        PropertyType[] pType = new PropertyType[pTmpType.size()];
        pTmpType.copyInto(pType);
        parameter.setProperty(pType);      
        pMapping.addElement(parameter);
      }
	}
	//TODO check whether information is correct
	FaultState[] faultStates = null;
	if (function.getFunctionFaults() == null) {
	  faultStates = new FaultState[0];
	} else {
      faultStates = function.getFunctionFaults().getFault();
    }
	for (int j = 0; j < faultStates.length; j++) {
      if (!faultStates[j].getName().trim().equals("void")) {        
        ParameterMapping parameter = new ParameterMapping();
        Vector pTmpType = loadFaultParameterProperty(faultStates[j].getName().trim(), viTypes);
        String faultElementNS = null;
        String faultElementName = null;
        FaultConfigDescriptor[] fault = null;
        if (operationConfigDescriptor.getTransportBindingConfiguration() != null) {
          fault = operationConfigDescriptor.getTransportBindingConfiguration().getFault();
          for (int i = 0; i < fault.length; i++) { 
            PropertyDescriptor[] properties = fault[i].getProperty(); 
            if (properties != null) {
			  for (int k = 0; k < properties.length; k++) {
                if (properties[k].getName().trim().equals("namespace")) {
                  faultElementNS = properties[k].getValue();
				} else if (properties[k].getName().trim().equals("fault-element-name")) {
                  faultElementName = properties[k].getValue();
				}
			  }
			}   
          }
        } 
      
        if (faultElementNS == null) { //apply default NS
          faultElementNS = faultNamespace;      				
		}
        if (faultElementName == null) { //apply default falult element name
          faultElementName = function.getNameMappedTo() + "_" + faultStates[j].getName().trim();      
        }     
        QName fElemeQName = new QName( faultElementNS, faultElementName);
        pTmpType.addElement(getPropertyType(ParameterMapping.FAULT_ELEMENT_QNAME, fElemeQName.toString())); 
   
        PropertyType[] pType = new PropertyType[pTmpType.size()];
        pTmpType.copyInto(pType);
        parameter.setProperty(pType);     
		pMapping.addElement(parameter);	
      }
	}
	  
	ParameterMapping[] parameterMapping = new ParameterMapping[pMapping.size()]; 
    pMapping.copyInto(parameterMapping);	   
	return parameterMapping;	  
  }
	
  private Vector loadFaultParameterProperty(String faultName, HashMap viTypes) {
	Vector faultTmpType = new Vector();			
	faultTmpType.addElement(getPropertyType(ParameterMapping.PARAMETER_TYPE, "" + ParameterMapping.FAULT_TYPE));
	faultTmpType.addElement(getPropertyType(ParameterMapping.WSDL_PARAM_NAME, faultName));    
    faultTmpType = generateJava_SchemaProps(faultTmpType, faultName, viTypes);      
    return faultTmpType;
  } 
	
  private Vector loadParameterProperty (ParameterState parameter, FunctionSoapExtensionFunction soapExtensionFunction, int position, int parameterType, HashMap viTypes, String namespace) throws ConversionException {	
    Vector pTmpType = new Vector();	  
    //add parameter's position info
    pTmpType.addElement(getPropertyType(ParameterMapping.POSITION, "" + position));    
    //add parameter's type info	  
    pTmpType.addElement(getPropertyType(ParameterMapping.PARAMETER_TYPE, "" + parameterType));
    //add parameter's wsdl name info
    pTmpType.addElement(getPropertyType(ParameterMapping.WSDL_PARAM_NAME, parameter.getNameMappedTo().trim()));
    //add parameter's is exposed info
    pTmpType.addElement(getPropertyType(ParameterMapping.IS_EXPOSED, "" + parameter.isIsExposed()));
    //add parameter's is optional info   
    pTmpType.addElement(getPropertyType(ParameterMapping.IS_OPTIONAL, "" + parameter.isIsOptional()));         		  
	  
    //add parameter's namespace info  
    if (parameterType == ParameterMapping.RETURN_TYPE) {
      if(namespace != null){
        pTmpType.addElement(getPropertyType(ParameterMapping.NAMESPACE, namespace));
      } else {
        pTmpType.addElement(getPropertyType(ParameterMapping.NAMESPACE, EMPTY_STRING));
      }
    }
      
    if (parameter.getParameterDefaultValue()!= null) {
      if (parameter.getParameterDefaultValue().getDefaultValue().isIsInitial()) {
        pTmpType.addElement(getPropertyType(ParameterMapping.DEFAULT_VALUE, "")); //that is when for string parameter the VI's isInitial checkbox is checked.
      } else {
        pTmpType.addElement(getPropertyType(ParameterMapping.DEFAULT_VALUE, parameter.getParameterDefaultValue().getDefaultValue().getName().trim()));              
      }		  
    }	  	  	     
    if (parameter.getParameterSoapExtensionParameter() != null) {
 	  pTmpType.addElement(getPropertyType(ParameterMapping.IS_HEADER, "" + parameter.getParameterSoapExtensionParameter().getSoapExtensionParameter().isIsHeader()));
    }
	       
	try {
	  ParameterMappedTypeReference mappedTypeReference = parameter.getParameterMappedTypeReference();		  
	  if (mappedTypeReference.getConvertedTypeReference() != null) {
	    String covertedTypeName = mappedTypeReference.getConvertedTypeReference().getName().trim();
        if (covertedTypeName.equals("void")) {
          return new Vector();
        }
        pTmpType = generateJava_SchemaProps(pTmpType, covertedTypeName, viTypes);		  
	  }		   
	  if (mappedTypeReference.getConvertedTableReference() != null) {
		String convertedTableName = mappedTypeReference.getConvertedTableReference().getName().trim();
        if (convertedTableName.equals("void")) {
          return new Vector();
        }
        pTmpType = generateJava_SchemaProps(pTmpType, convertedTableName, viTypes);     
	  }		  
	  if (mappedTypeReference.getComplexTypeReference() != null){
	    String complexTypeName = mappedTypeReference.getComplexTypeReference().getName().trim();	
        if (complexTypeName.equals("void")) {
          return new Vector();
        }		  
        pTmpType = generateJava_SchemaProps(pTmpType, complexTypeName, viTypes);     
	  }
	  
	  //adding header info
	  if (parameter.getParameterSoapExtensionParameter() != null) {	  
	    pTmpType.addElement(getPropertyType(ParameterMapping.IS_HEADER, "" + parameter.getParameterSoapExtensionParameter().getSoapExtensionParameter().isIsHeader()));
	    //add namespace info
	    if (parameter.getParameterSoapExtensionParameter().getSoapExtensionParameter().getNamespace() != null) {
	      pTmpType.addElement(getPropertyType(ParameterMapping.NAMESPACE, parameter.getParameterSoapExtensionParameter().getSoapExtensionParameter().getNamespace()));
	    } else if(soapExtensionFunction != null) {
	      pTmpType.addElement(getPropertyType(ParameterMapping.NAMESPACE, soapExtensionFunction.getSoapExtensionFunction().getNamespace().trim()));
	    }
	  }	    		
	} catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "loadParameterProperty (ParameterState parameter, FunctionSoapExtensionFunction soapExtensionFunction, int position, int parameterType, HashMap viTypes, String namespace)", "Problem with getting elements of element 'Parameter.MappedTypeReference'. Root clause is: " + e.getMessage(), e);                     
	  throw new ConversionException(MigrationConstants.PROBLEM_PARAMETER_MAPPEDTYPEREFERENCE, new Object[] { e.getMessage() }, e);
	}
	return pTmpType;
  }
	
  private Vector generateJava_SchemaProps(Vector pTmpType, String name, HashMap viTypes) {  
    pTmpType.addElement(getPropertyType(ParameterMapping.JAVA_TYPE_NAME, name));    
    String qName;     
    if (StandardTypes.isStandardType(name)) {
      com.sap.engine.lib.xml.util.QName qOld = StandardTypes.getMapTypeInQNameForm(name);
      QName qNew = new QName(qOld.getURI(), qOld.getLocalName()); 
      qName = qNew.toString();    
    } else {
      qName = viTypes.get(name).toString();
    }
    pTmpType.addElement(getPropertyType(ParameterMapping.SCHEMA_QNAME, qName)); 
    return pTmpType;  
  }
  
  private ImplementationLink loadInterfaceImplementationLink(WSConfigurationDescriptor configDescriptor, WSRuntimeConfigurationDescriptor runtimeDescriptor) {
	ImplementationLink implLink = new ImplementationLink();
	ImplLinkDescriptor implDescriptor = configDescriptor.getImplLink();
	boolean hasEjbName = configDescriptor.getEjbName() != null;
	boolean hasImplLink = implDescriptor != null;
		
    Vector pTmpTypes = new Vector();     
	if (hasEjbName) {
  	  pTmpTypes.addElement(getPropertyType(MigrationConstants.IMPLEMENTATION_ID, EJBImplementationLink.IMPLEMENTATION_CONTAINER_ID));
	  pTmpTypes.addElement(getPropertyType(EJBImplementationLink.EJB_NAME, configDescriptor.getEjbName()));
      pTmpTypes.addElement(getPropertyType(EJBImplementationLink.EJB_INTERFACE_TYPE, EJBImplementationLink.EJB_INTERFACE_REMOTE));
      
      if (runtimeDescriptor.getImplLink().getProperties() != null) {
        com.sap.engine.lib.descriptors.ws04wsrt.PropertyDescriptor[]  propDescriptor = runtimeDescriptor.getImplLink().getProperties().getProperty();
  		for (int i = 0; i < propDescriptor.length; i++) {
  	      if(EJBImplConstants.EJB_JNDI_NAME.equals(propDescriptor[i].getName())) {
  		    pTmpTypes.addElement(getPropertyType(EJBImplementationLink.EJB_JNDI_NAME, propDescriptor[i].getValue()));
  		  }
  		  if (EJBImplConstants.EJB_SESSION_TYPE.equals(propDescriptor[i].getName())) {
  			 pTmpTypes.addElement(getPropertyType(EJBImplementationLink.EJB_SESSION_TYPE, propDescriptor[i].getValue()));					
  		  }
  		}
      }
    }
      
	if(hasImplLink) {    
    pTmpTypes.addElement(getPropertyType(MigrationConstants.IMPLEMENTATION_ID, implDescriptor.getImplementationId().trim()));    
    if(implDescriptor.getProperties() != null) {
      PropertyDescriptor[] properties = implDescriptor.getProperties().getProperty();  		
  		for(int i = 0; i < properties.length; i++) {
  		  pTmpTypes.addElement(getPropertyType(properties[i].getName().trim(), properties[i].getValue().trim()));
  		}
    }
	}
		
	PropertyType[] pType = new PropertyType[pTmpTypes.size()];
  pTmpTypes.copyInto(pType); 
	implLink.setProperty(pType);
	return implLink;
  }

  public ServiceMapping[] loadService() {
	// TODO  fill data in service element
	ServiceMapping[] serviceMappings = new ServiceMapping[1];
	for (int i = 0; i < serviceMappings.length; i++) {
      serviceMappings[i] = new ServiceMapping();
//			TODO fill info
	  serviceMappings[i].setProperty(loadServiceMappingProperty());//?
	  serviceMappings[i].setEndpoint(loadEndpoint());//?
	  serviceMappings[i].setImplementationLink(loadServiceImplementationLink());//?
	}
	return serviceMappings;
  }

  private PropertyType[] loadServiceMappingProperty() {
//		TODO fill info
	PropertyType[] pType = new PropertyType[1];
	for (int i = 0; i < pType.length; i++) {
      pType[i] = getPropertyType("", "");
	}
	return pType;
  }

  private EndpointMapping[] loadEndpoint() {
//		TODO fill info
	EndpointMapping[] endpointMappings = new EndpointMapping[1];
	for (int i = 0; i < endpointMappings.length; i++) {
	  endpointMappings[i] = new EndpointMapping();
	  endpointMappings[i].setProperty(loadEMProperty());
	}
	return endpointMappings;
  }

  private PropertyType[] loadEMProperty() {
//		TODO fill info
	PropertyType[] pType = new PropertyType[1];
	for (int i = 0; i < pType.length; i++) {
   	  pType[i] = getPropertyType("", "");
	}
	return pType;
  }
	
  private ImplementationLink loadServiceImplementationLink() {
	ImplementationLink implLink = new ImplementationLink();
	// TODO fill info
	implLink.setProperty(loadImplementationLinkProperty());
	return implLink;
  }
	
  private PropertyType[] loadImplementationLinkProperty() {
    //	TODO fill info
    PropertyType[] pType = new PropertyType[1];
    for (int i = 0; i < pType.length; i++) {
	  pType[i] = getPropertyType("", "");
    }
    return pType;
  }

  private HashMap getViTypes(VirtualInterfaceState viStructure)throws Exception {
    SchemaConvertor schConvertor = new SchemaConvertor();
    DOMSource[] sources = schConvertor.parseInLiteralMode(viStructure);
    return schConvertor.getJavaToQNameMappings();
  } 
  
  private PropertyType getPropertyType(String pName, String pValue) {
    PropertyType pType = new PropertyType();
    pType.setName(pName);
    pType.set_value(pValue);   
    return pType;
  }
  
  public Hashtable getOperationProperties() {
    return operationProperties;
  }

}
