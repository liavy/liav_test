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

import java.io.IOException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.sap.engine.lib.descriptors.j2ee.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.ConstructorParameterOrderType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.ExceptionMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaWsdlMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaXmlTypeMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.MethodParamPartsMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.PackageMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.PortMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.ServiceEndpointInterfaceMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.ServiceEndpointMethodMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.ServiceInterfaceMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.WsdlMessageMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.WsdlMessagePartNameType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.WsdlReturnValueMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaWsdlMappingType.Sequence1;
import com.sap.engine.lib.descriptors5.webservices.WebserviceDescriptionType;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Boyan Slavov
 */
public class JavaWSDLMapping {

  private MappingRules wsdlMapping;
  private Definitions definitions;
  private JavaWsdlMappingType mapping;
  private ProxyGeneratorConfigNew config;

  private static final HashMap parameterTypes = new HashMap();
  static {
    parameterTypes.put("IN", new Integer(ParameterMapping.IN_TYPE));
    parameterTypes.put("OUT", new Integer(ParameterMapping.OUT_TYPE));
    parameterTypes.put("INOUT", new Integer(ParameterMapping.IN_OUT_TYPE));
  }

  private static final Location LOCATION = Location.getLocation(JavaWSDLMapping.class);
  private static Pattern pattern = Pattern.compile(" ");
  
  ProxyGeneratorConfigNew convert(
    JavaWsdlMappingType mapping,
    WebserviceDescriptionType[] types,
    Definitions definitions,
    QName serviceQName,
    String serviceInterface,
    MappingRules wsdlMapping,
    SchemaTypeSet schemaTypeSet,
    Document webXml)
    throws ProxyGeneratorException, TypeMappingException, IOException {

    this.wsdlMapping = wsdlMapping;
    this.definitions = definitions;
    this.mapping = mapping;
    
    config = defaultMapping(mapping.getPackageMapping(), definitions, wsdlMapping, schemaTypeSet);

    //    mapping.getExceptionMapping();
    //    mapping.getJavaXmlTypeMapping();
    //    mapping.getPackageMapping();

    //setGenericSIName(wsdlMapping.getService());
    removeServices(wsdlMapping, serviceQName, serviceInterface);
    
    Sequence1[] sequence1s = mapping.getSequenceGroup1();
    for (int i = 0; i < sequence1s.length; i++) {
      JavaWsdlMappingType.Sequence1 sequence1 = sequence1s[i];

      convert(sequence1.getServiceEndpointInterfaceMapping(), types);
      convert(sequence1.getServiceInterfaceMapping());

      //      ServiceInterfaceMappingType sim = sequence1.getServiceInterfaceMapping();
      //      sim.getWsdlServiceName().get_value();
      //      sim.getServiceInterface();
      //      sim.getPortMapping();
    }
    J2EE14TypesConvertor convertor = new J2EE14TypesConvertor(mapping, schemaTypeSet);
    convertor.convert();
    return config;
  }

  /** 
   * @param wsdlMapping2
   * @param serviceQName
   */
  private static void removeServices(MappingRules wsdlMapping, QName serviceQName, String serviceInterface) {
    //For clients separate proxy is generated for each service-ref. Remove all services which are not needed for this service-ref
    
    if (serviceQName == null)
      return;
    
    ServiceMapping mapping = get(wsdlMapping.getService(), serviceQName);
    
    if (mapping != null) {
      mapping.setSIName(serviceInterface);
      wsdlMapping.setService(new ServiceMapping[] {mapping});
    }
  }

  /** Set all service interfaces to javax.xml.rpc.Service which can later be changed to generated SI
   * @param service
   */
  private void setGenericSIName(ServiceMapping[] services) {
    if (services == null)
       return;
    
    for (int i = 0; i < services.length; i++) {
      ServiceMapping mapping = services[i];
      mapping.setSIName("javax.xml.rpc.Service");
    }
    
  }

  /**
   * @param type
   * @param types
   * @param definitions
   * @param wsdlMapping
   */
  private void convert(ServiceInterfaceMappingType type) {
    
    if (type == null) {
      return;
    }
    
    ServiceMapping mapping = get(wsdlMapping.getService(), type.getWsdlServiceName().get_value());

    if (mapping == null) {
      LOCATION.debugT("did not find service with name " + type.getWsdlServiceName().get_value());
    } else {
      convert(type.getPortMapping(), mapping.getEndpoint());
      //mapping.setImplementationLink(); //todo: set

      QName name = type.getWsdlServiceName().get_value();
      if (name != null)
        mapping.setServiceName(name);

      String s = type.getServiceInterface().get_value();
      if (s != null)
        mapping.setSIName(s);
    }
  }

  /**
   * @param mappings
   * @param types
   */
  private static void convert(PortMappingType[] types, EndpointMapping[] mappings) {
    if (types == null)
      return;

    for (int i = 0; i < types.length; i++) {
      PortMappingType type = types[i];

      EndpointMapping mapping = get(mappings, type.getPortName().get_value());

      //mapping.setPortBinding(nvl());
      mapping.setPortJavaName(nvl(type.getJavaPortName().get_value(), mapping.getPortJavaName()));
      //mapping.setPortPortType();
      mapping.setPortQName(nvl(type.getPortName().get_value(), mapping.getPortQName()));
    }

  }

  /**
   * @param string
   * @param string2
   * @return
   */
  private static String nvl(String s, String def) {
    if (s == null || s.length() == 0)
      return def;
    else
      return s;
  }

  /**
   * @param mappings
   * @param string
   * @return
   */
  private static EndpointMapping get(EndpointMapping[] mappings, String portName) {
    if (mappings == null || portName == null)
      return null;

    for (int i = 0; i < mappings.length; i++) {
      EndpointMapping mapping = mappings[i];
      if (portName.equals(mapping.getProperty(EndpointMapping.PORT_WSDL_NAME)))
        return mapping;
    }
    LOCATION.debugT("did not find <endpoint> for port " + portName);
    return null;
  }

  /**
   * @param mappings
   * @param type
   * @return
   */
  static ServiceMapping get(ServiceMapping[] mappings, QName service) {
    if (mappings == null)
      return null;

    if (service == null)
      return null;

    for (int i = 0; i < mappings.length; i++) {
      ServiceMapping mapping = mappings[i];
      if (service.equals(mapping.getServiceName()))
        return mapping;
    }
    return null;
  }

  private void convert(ServiceEndpointInterfaceMappingType[] types, WebserviceDescriptionType[] wsds) throws ProxyGeneratorException {

    if (types == null) {
      return;
    }
    
    for (int i = 0; i < types.length; i++) {
      ServiceEndpointInterfaceMappingType type = types[i];

      InterfaceMapping interfaceMapping = getInterfaceMapping(wsdlMapping.getInterface(), type.getWsdlBinding().get_value());

      interfaceMapping.setBindingQName(type.getWsdlBinding().get_value());

      QName portTypeName = type.getWsdlPortType().get_value();
      interfaceMapping.setPortType(portTypeName);
      convert(type.getServiceEndpointMethodMapping(), interfaceMapping.getOperation(), interfaceMapping);

      //mapping.setProperty();
      //mapping.b

      FullyQualifiedClassType sei = type.getServiceEndpointInterface();
      if (sei != null) {

        interfaceMapping.setSEIName(sei.get_value());

        /*if ( wsds != null) {
        	//wsds is null for clients
        	mapping.setImplementationLink(getImplementationLink(sei.get_value(), wsds, webXml));
        }*/
      }
    }
  }

  /**
   * @param mappings
   * @param name
   * @return
   */
  static InterfaceMapping getInterfaceMapping(InterfaceMapping[] mappings, QName binding) {

    if (mappings != null && binding != null) {
      for (int i = 0; i < mappings.length; i++) {
        InterfaceMapping mapping = mappings[i];
        if (binding.equals(mapping.getBindingQName()))
          return mapping;
      }
    }

    return null;
  }

  /**
   * @param types
   * @return
   */
  private void convert(ServiceEndpointMethodMappingType[] types, OperationMapping[] opms, InterfaceMapping interfaceMapping) throws ProxyGeneratorException {

    if (types == null)
      return;

    for (int i = 0; i < types.length; i++) {
      ServiceEndpointMethodMappingType type = types[i];
      OperationMapping opm = get(opms, type.getWsdlOperation().get_value());
      
      if (type.getWrappedElement() != null) {
        ProxyGeneratorNew temp = new ProxyGeneratorNew();
        temp.unwrapOperation(interfaceMapping, opm, config);
      }
      
      if (null != type.getJavaMethodName().get_value()) {
        opm.setJavaMethodName(type.getJavaMethodName().get_value());
      }

      String operation = type.getWsdlOperation().get_value();
      if (operation != null) {
        opm.setWSDLOperationName(operation);
        convert1(type.getMethodParamPartsMapping(), type.getWsdlReturnValueMapping(), opm.getParameter());
      }
      //opm.setProperty();
    }
  }

  /**
   * @param opms
   * @param wsdlOperation
   * @return
   */
  private OperationMapping get(OperationMapping[] opms, String wsdlOperation) {
    if (opms == null || wsdlOperation == null)
      return null;
    
    for (int i = 0; i < opms.length; i++) {
      OperationMapping mapping = opms[i];
      if (wsdlOperation.equals(mapping.getWSDLOperationName()))
          return mapping;
    }
    return null;
  }

  /**
   * @param types
   * @param type
   * @return
   */
  private void convert(MethodParamPartsMappingType[] params, WsdlReturnValueMappingType ret, ParameterMapping[] pms) {
    //TODO: remove the method
    ParameterMapping pm;
    String s;

    if (ret != null) {

      pm = getParameterMapping(pms, ret.getWsdlMessagePartName(), ParameterMapping.RETURN_TYPE);

      s = ret.getMethodReturnValue().get_value();

      if (pm != null) { //todo: must never be null
        if (s != null)
          pm.setJavaType(s);

        WsdlMessagePartNameType pname = ret.getWsdlMessagePartName();

        QName sname = getSchemaQName(s, mapping.getJavaXmlTypeMapping());
        if (sname != null)
          pm.setSchemaQName(sname);

        if (pname.get_value() != null) {
          pm.setWSDLParameterName(pname.get_value());
        }

      }

      //pm.setDefaultValue();
      //pm.setExposed();
      //pm.setFaultConstructorParamOrder(ret.get);
      //pm.setFaultElementQName();
      //pm.setHeader();
      //pm.setHolderName();
      //pm.setIsElement();
      //pm.setJavaType(ret.getMethodReturnValue().get_value());
      //pm.setParameterType(ParameterMapping.RETURN_TYPE);
      //pm.setPosotion();
    }

    for (int i = 0; i < params.length; i++) {

      MethodParamPartsMappingType param = params[i];

      param.getParamPosition();
      param.getParamType();
      WsdlMessageMappingType wmm = param.getWsdlMessageMapping();
      //wmm.getParameterMode();
      //wmm.getWsdlMessage();

      pm = getParameterMapping(pms, wmm.getWsdlMessagePartName(), convertParameterType(wmm.getParameterMode().get_value()));

      if (pm == null) //todo: must never be null
        return;

      //pm.setDefaultValue();
      //pm.setExposed();
      //pm.setFaultConstructorParamOrder(ret.get); //todo: exception mapping
      //pm.setFaultElementQName();
      pm.setHeader(wmm.getSoapHeader() != null);
      //pm.setHolderName();
      //pm.setIsElement();

      s = param.getParamType().get_value();

      if (s != null)
        pm.setJavaType(s);

      //pm.setParameterType(convertParameterType(param.getParamType().get_value()));
      pm.setPosition(param.getParamPosition().get_value().intValue());

      WsdlMessagePartNameType pname = wmm.getWsdlMessagePartName();
      QName sname = getSchemaQName(s, mapping.getJavaXmlTypeMapping());
      if (sname != null)
        pm.setSchemaQName(sname);

      if (pname.get_value() != null) {
        pm.setWSDLParameterName(pname.get_value());
      }
    }
  }

  private void convert1(MethodParamPartsMappingType[] params, WsdlReturnValueMappingType ret, ParameterMapping[] pms) {
   
    for (int i = 0; i < pms.length; i++) {
      ParameterMapping pm = pms[i];
      
      String javaType = null;
      WsdlMessagePartNameType wsdlMessagePartName = null;

      int parameterType = pm.getParameterType();

      if (ParameterMapping.RETURN_TYPE == parameterType) {
        if (ret != null) {
          javaType = ret.getMethodReturnValue().get_value();
          wsdlMessagePartName = ret.getWsdlMessagePartName();
        }
      } else if (ParameterMapping.FAULT_TYPE == parameterType) {
        String messageQName = pm.getProperty(ParameterMapping.FAULT_MESSAGE);
        String messagePartName = pm.getWSDLParameterName();
        ExceptionMappingType exceptionMapping = get(mapping.getExceptionMapping(), messageQName, messagePartName);
        if (exceptionMapping!= null) {
          javaType = exceptionMapping.getExceptionType().get_value();
          
          ConstructorParameterOrderType jaxrpcOrder = exceptionMapping.getConstructorParameterOrder();
          String[] oldOrder = split(pm.getFaultConstructorParamOrder());
          String[] paramTypes = split(pm.getProperty(ParameterMapping.FAULT_CONSTRUCTOR_PARAM_TYPES));
          String[] attrCount = split(pm.getProperty(ParameterMapping.FAULT_ATTRIBUTE_COUNT));
          
          if (jaxrpcOrder != null && oldOrder != null) {
            com.sap.engine.lib.descriptors.j2ee.String[] names = jaxrpcOrder.getElementName();
            StringBuffer paramTypesNew = new StringBuffer();
            StringBuffer attrCountNew = new StringBuffer();

            for (int j = 0; j < names.length; j++) {
              String name = names[j].get_value();
              int pos = getPosition(oldOrder, name);

              if (j > 0) {
                paramTypesNew.append(' ');
                attrCountNew.append(' ');
              }

              if (paramTypes != null) {
                paramTypesNew.append(paramTypes[pos]);  
              }
              if (attrCount != null) {
                attrCountNew.append(attrCount[pos]);                
              }
            }
            LOCATION.debugT("processing ConstructorParameterOrder");
            if (paramTypes != null) {
              pm.setProperty(ParameterMapping.FAULT_CONSTRUCTOR_PARAM_TYPES, paramTypesNew.toString());
            }
            
            if (attrCount != null) {
              pm.setProperty(ParameterMapping.FAULT_ATTRIBUTE_COUNT, attrCountNew.toString());  
            }
          }
        }
      } else {
   
        MethodParamPartsMappingType param = get(params, pm.getWSDLParameterName());

        WsdlMessageMappingType wmm = param.getWsdlMessageMapping();
        //wmm.getParameterMode();
        //wmm.getWsdlMessage();

        //pm.setDefaultValue();
        //pm.setExposed();
        //pm.setFaultConstructorParamOrder(ret.get); //todo: exception mapping
        //pm.setFaultElementQName();
        pm.setHeader(wmm.getSoapHeader() != null);
        //pm.setHolderName();
        //pm.setIsElement();

        javaType = param.getParamType().get_value();

        //pm.setParameterType(convertParameterType(param.getParamType().get_value()));
        pm.setPosition(param.getParamPosition().get_value().intValue());

        wsdlMessagePartName = wmm.getWsdlMessagePartName();
      }

      
      if (javaType != null) {
        pm.setJavaType(javaType);
      }

      if (wsdlMessagePartName != null && wsdlMessagePartName.get_value() != null) {
        pm.setWSDLParameterName(wsdlMessagePartName.get_value());
      }

      QName schemaQName = getSchemaQName(javaType, mapping.getJavaXmlTypeMapping());
      if (schemaQName != null) {
        pm.setSchemaQName(schemaQName);
      }
    }
  }

  
  /**
   * @param faultConstructorParamOrder
   * @return
   */
  private static String[] split(String s) {
    return s == null ? null: pattern.split(s);
  }

  /**
   * @param oldOrder
   * @param name
   * @return
   */
  private int getPosition(String[] names, String name) {
    
    if (name != null) {
      for (int i = 0, n = names.length; i < n; ++i) {
        if (name.equals(names[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * @param exceptionMapping
   * @param faultMessageQName
   * @return
   */
  private ExceptionMappingType get(ExceptionMappingType[] exceptionMappings, String messageQName, String paramPartName) {
    QName qname = QName.valueOf(messageQName);
    if (exceptionMappings != null) {
      for (int i = 0; i < exceptionMappings.length; i++) {
        ExceptionMappingType exceptionMapping = exceptionMappings[i];
        QName qn = exceptionMapping.getWsdlMessage().get_value();
        
        WsdlMessagePartNameType wsdlMessagePartName = exceptionMapping.getWsdlMessagePartName();
        if (qname.equals(qn)) {
          if (paramPartName == null
              || wsdlMessagePartName == null 
              || paramPartName.equals(wsdlMessagePartName.get_value())) {
              return exceptionMapping;
          }
        }
      }
    }
    LOCATION.debugT("Could not find exception mapping with wsdl-message '" + messageQName+"' and wsdl-message-part-name '"+ paramPartName+"'" );
    return null;
  }


  /**
   * @param params
   * @param parameterName
   * @return
   */
  private MethodParamPartsMappingType get(MethodParamPartsMappingType[] params, String parameterName) {
    if (params != null && parameterName != null) {
      for (int i = 0; i < params.length; i++) {
        MethodParamPartsMappingType param = params[i];
        if (parameterName.equals(param.getWsdlMessageMapping().getWsdlMessagePartName().get_value())) {
          return param;
        }
      }
    }
    return null;
  }

  /**
   * @param pms
   * @param type
   * @param i
   * @return
   */
  private static ParameterMapping getParameterMapping(ParameterMapping[] pms, WsdlMessagePartNameType type, int parameterMode) {

    if (pms == null || type == null)
      return null;

    String s = type.get_value();

    if (s == null)
      return null;

    for (int i = 0; i < pms.length; i++) {
      ParameterMapping mapping = pms[i];

      //checkModes(mapping.getParameterType(), parameterMode);
      if (s.equals(mapping.getWSDLParameterName())) {
        return mapping;
      }
    }
    LOCATION.debugT("did not find parameter with message part name = " + s + " and ParameterMode = " + parameterMode);
    return null;
  }

  /**
   * @param i
   * @param parameterMode
   * @return
   */
  private static boolean checkModes(int actualMode, int searchedMode) {
    if (actualMode == searchedMode)
      return true;

    // inout matches return and vice versa
    if ((actualMode == ParameterMapping.IN_OUT_TYPE || actualMode == ParameterMapping.RETURN_TYPE) && (searchedMode == ParameterMapping.IN_OUT_TYPE || searchedMode == ParameterMapping.RETURN_TYPE))
      return true;
    return false;
  }

  /**
   * @param string
   * @return
   */
  private static int convertParameterType(String parameterMode) {
    Integer m = (Integer) parameterTypes.get(parameterMode);
    return m.intValue();
  }

  /**
   * @param type
   * @param definitions
   * @return
   */
  private static QName getSchemaQName(String className, JavaXmlTypeMappingType[] typeMappings) {

    if (typeMappings == null || className == null)
      return null;
    
    for (int i = 0; i < typeMappings.length; i++) {
      JavaXmlTypeMappingType type = typeMappings[i];
      if (className.equals(type.getJavaType())) {
        if (type.getChoiceGroup1().isSetAnonymousTypeQname()) {
          //TODO: support anonymous types
          LOCATION.debugT("no support for anonymous types yet");
          //return type.getChoiceGroup1().getAnonymousTypeQname().;
          return null;
        } else if (type.getChoiceGroup1().isSetRootTypeQname()) {
          return type.getChoiceGroup1().getRootTypeQname().get_value();
        }
      }
    }
    return null;
  }

  static ProxyGeneratorConfigNew defaultMapping(PackageMappingType[] types, Definitions definitions, MappingRules rules, SchemaTypeSet schemaTypeSet)
    throws ProxyGeneratorException, TypeMappingException, IOException {

    //LOCATION.debugT(definitions.toString());

    ProxyGeneratorConfigNew config = new ProxyGeneratorConfigNew();
    config.setGenerationMode(ProxyGeneratorConfigNew.LOAD_MODE);

    config.setOutputPath("ignore");
    config.setWsdl(definitions);
    config.setMappingRules(rules);
    try {
      ConfigurationMarshallerFactory factory = com.sap.engine.services.webservices.server.WSContainer.createInitializedServerCFGFactory();
      config.setConfigMarshaller(factory);
    } catch (Exception e) {
      throw new ProxyGeneratorException("", e);
    }

    SchemaToJavaConfig schemaConfig = getSchemaConfig(types);
    schemaConfig.setTypeSet(schemaTypeSet);
    config.setSchemaConfig(schemaConfig);

    config.setOutputPackage(getOutputPackage(definitions, schemaConfig));

    ProxyGeneratorNew proxyGenerator = new ProxyGeneratorNew();
    
    config.setUnwrapDocumentStyle(false);
    
    proxyGenerator.generateAll(config);

    removeUnusedInterfaceMappings(config.getMappingRules());
    //setInterfaceMappingID(rules.getInterface());

    //ByteArrayOutputStream os = new ByteArrayOutputStream();
    //MappingFactory.save(rules, os);
    //LOCATION.debugT(os);
    return config;
  }

  /** Removes unused bindings/interfaceMappings to prevent compilation errors for missing classes during proxy generation
   * @param rules
   * 
   */
  private static void removeUnusedInterfaceMappings(MappingRules rules) {
    Set usedBindings = new HashSet();
    // find used bindgins
    ServiceMapping[] services = rules.getService();
    if (services != null) {
      for (int i = 0; i < services.length; i++) {
        ServiceMapping service = services[i];
        
        EndpointMapping[] endpoints = service.getEndpoint();
        if (endpoints != null) {
          for (int j = 0; j < endpoints.length; j++) {
            EndpointMapping endpoint = endpoints[j];
            usedBindings.add(endpoint.getPortBinding());
          }
        }
      }
    }

    // remove unused bindings (interfaceMappings)
    InterfaceMapping[] interfaces = rules.getInterface();
    if (interfaces != null) {
      int usedBindingsCount = 0;
      for (int i = 0; i < interfaces.length; ++i) {
        InterfaceMapping intf = interfaces[i];
        if (usedBindings.contains(intf.getBindingQName())) {
          interfaces[usedBindingsCount] = intf;
          ++usedBindingsCount;
        }
        // there should not be references in configuration.xml to removed interface mappings
        // Interfaces in configuration.xml are created on demand when referenced by services
      }
      InterfaceMapping[] usedInterfaces = new InterfaceMapping[usedBindingsCount];
      System.arraycopy(interfaces, 0, usedInterfaces, 0, usedBindingsCount);
      rules.setInterface(usedInterfaces);
    }
  }

  /**
   * @param definitions
   * @param schemaConfig
   * @return
   */
  private static String getOutputPackage(Definitions definitions, SchemaToJavaConfig schemaConfig) {
    ObjectList list = definitions.getServices();

    Properties properties = schemaConfig.getUriToPackageMapping();

    if (list != null || list.getLength() == 0) {
      if (properties.elements().hasMoreElements())
        return (String) properties.elements().nextElement();
    }

    Service service = (Service) list.item(0);
    String ns = service.getName().getNamespaceURI();

    return properties.getProperty(ns);
  }

  /**
   * @param mappings
   */
  private static void setInterfaceMappingID(InterfaceMapping[] mappings) {
    if (mappings == null)
      return;
    for (int i = 0; i < mappings.length; i++) {
      InterfaceMapping mapping = mappings[i];
      if (null == mapping.getInterfaceMappingID()) {
        mapping.setInterfaceMappingID(new UID().toString());        
      }
    }
  }

  /**
   * @param types
   * @return
   */
  private static SchemaToJavaConfig getSchemaConfig(PackageMappingType[] types) {
    SchemaToJavaConfig config = new SchemaToJavaConfig();

    config.setGenerationMode(SchemaToJavaConfig.LOAD_MODE);

    Properties properties = new Properties();

    for (int i = 0; i < types.length; i++) {
      PackageMappingType type = types[i];
      properties.put(type.getNamespaceURI().get_value().toString(), type.getPackageType().get_value());
    }
    config.setUriToPackageMapping(properties);

    return config;
  }

  public static ProxyGeneratorConfigNew generateClientProxies(
    MappingRules mappingRules,
    String outputPath,
    ConfigurationRoot configurationRoot,
    Definitions definitions,
    SchemaToJavaConfig schemaToJavaConfig)
    throws ProxyGeneratorException {

    ProxyGeneratorConfigNew config = new ProxyGeneratorConfigNew();
    config.setBufferedOutput(false);
    config.setGenerationMode(ProxyGeneratorConfigNew.STANDALONE_MODE);
    config.setMappingRules(mappingRules);
    //config.setOutputFiles();
    config.setOutputPackage(getOutputPackage(definitions, schemaToJavaConfig));
    config.setOutputPath(outputPath);

    config.setProxyConfig(configurationRoot);

    config.setSchemaConfig(schemaToJavaConfig);
    config.setWsdl(definitions);

    ProxyGeneratorNew pg = new ProxyGeneratorNew();
    pg.generateAll(config);
    return config;
  }
}
