/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi;

//import com.sap.engine.lib.xml.parser.XMLTokenReaderImpl;
import java.io.*;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ComponentInstantiationException;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.DefaultProviders;
import com.sap.engine.services.webservices.wsdl.*;
import com.sap.exception.BaseRuntimeException;

/**
 * Class responsible for loading saving and all operations with logical ports.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class LogicalPortFactory {

  private TypeMappingRegistryImpl registry;

  public LogicalPortFactory() {
    try {
      registry = new TypeMappingRegistryImpl();
      ClassLoader classLoader = this.getClass().getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("com/sap/engine/services/webservices/jaxrpc/wsdl2java/lpapi/LPTypes.xml");
      registry.fromXML(inputStream,classLoader);
    } catch (TypeMappingException e) {
      throw new BaseRuntimeException(e);
    }
  }

  /**
   * Loads logical port configuration from input stream.
   * May be used by stub manager to load LP configuration.
   */
  public LogicalPorts loadLogicalPorts(InputStream input) throws LogicalPortException {
    LogicalPorts result = null;
    XMLTokenReader parser = XMLTokenReaderFactory.getInstance().createReader(input);
    try {
      parser.begin();
      parser.moveToNextElementStart();
      TypeMapping typeMapping = registry.getDefaultTypeMapping();
      SOAPDeserializationContext context = new SOAPDeserializationContext();
      context.setTypeMapping(typeMapping);
      LogicalPorts deserializer = new LogicalPorts();
      result = (LogicalPorts) deserializer.deserialize(parser,context,deserializer.getClass());
    } catch (ParserException e) {
      throw new LogicalPortException(LogicalPortException.PARSER_ERROR,e);
    } catch (UnmarshalException e) {
      throw new LogicalPortException(LogicalPortException.DESERIALIZATION_ERROR,e);
    }
    return result;
  }

  /**
   * Loads single logical port form input stream.
   * @param input
   * @return
   * @throws com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException
   */
  public LogicalPortType loadLogicalPort(InputStream input) throws LogicalPortException {
    LogicalPortType result = null;    
    XMLTokenReader parser = XMLTokenReaderFactory.getInstance().createReader(input);
    try {
      parser.begin();
      parser.moveToNextElementStart();
      TypeMapping typeMapping = registry.getDefaultTypeMapping();
      SOAPDeserializationContext context = new SOAPDeserializationContext();
      context.setTypeMapping(typeMapping);
      LogicalPortType deserializer = new LogicalPortType();
      result = (LogicalPortType) deserializer.deserialize(parser,context,deserializer.getClass());
    } catch (ParserException e) {
      throw new LogicalPortException(LogicalPortException.PARSER_ERROR,e);
    } catch (UnmarshalException e) {
      throw new LogicalPortException(LogicalPortException.DESERIALIZATION_ERROR,e);
    }
    return result;
  }

  /**
   * Loads logical port configuration from input file.
   * May be used by stub manager to load LP configuration.
   */
  public LogicalPorts loadLogicalPorts(String fileName) throws LogicalPortException {
    FileInputStream input = null;
    LogicalPorts result = null;
    try {
      input = new FileInputStream(fileName);
      result = loadLogicalPorts(input);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  /**
   * Loads single logical port from file.
   * @param fileName
   * @return
   * @throws com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException
   */
  public LogicalPortType loadLogicalPort(String fileName)  throws LogicalPortException {
    FileInputStream input = null;
    LogicalPortType result = null;
    try {
      input = new FileInputStream(fileName);
      result = loadLogicalPort(input);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  /**
   * Loads logical port configuration from input file.
   * May be used by stub manager to load LP configuration.
   */
  public LogicalPorts loadLogicalPorts(File file) throws LogicalPortException {
    FileInputStream input;
    try {
      input = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.PARSER_ERROR,e);
    }
    try {
      return loadLogicalPorts(input);
    } finally {
      try {
        input.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Saves Logical port configuration.
   * Warning ! Closes Output Stream.
   */
  public void saveLogicalPorts(LogicalPorts lp, OutputStream output) throws IOException {
    XMLTokenWriter writer = XMLTokenWriterFactory.newInstance();
    writer.init(output,"utf-8");
//    writer.setIndent(true);
    writer.writeInitial();
    TypeMapping typeMapping = registry.getDefaultTypeMapping();
    writer.enter(null,"LogicalPorts");
    SOAPSerializationContext context = new SOAPSerializationContext();
    context.setTypeMapping(typeMapping);
    lp.serialize(lp,writer,context);
    writer.flush();
  }

  /**
   * Saves single logical port.
   * @param lp
   * @param output
   * @throws IOException
   */
  public void saveLogicalPort(LogicalPortType lp, OutputStream output) throws IOException {
    LogicalPortType serializer = lp;
    if (serializer == null) {
      serializer = new LogicalPortType();
    }
    XMLTokenWriter writer = XMLTokenWriterFactory.newInstance();
    writer.init(output,"utf-8");
//    writer.setIndent(true);
    writer.writeInitial();
    TypeMapping typeMapping = registry.getDefaultTypeMapping();
    writer.enter(null,"LogicalPort");
    SOAPSerializationContext context = new SOAPSerializationContext();
    context.setTypeMapping(typeMapping);
    serializer.serialize(lp,writer,context);
    writer.flush();
  }

  /**
   * Saves Logical Port configuration.
   */
  public void saveLogicalPorts(LogicalPorts lp, String fileName) throws LogicalPortException {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(fileName);
      saveLogicalPorts(lp,output);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } catch (IOException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Saves single logical port.
   * @param lp
   * @param fileName
   * @throws com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException
   */
  public void saveLogicalPort(LogicalPortType lp, String fileName) throws LogicalPortException {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(fileName);
      saveLogicalPort(lp,output);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } catch (IOException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,fileName);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Saves Logical Port configuration.
   */
  public void saveLogicalPorts(LogicalPorts lp, File file) throws LogicalPortException {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(file);
      saveLogicalPorts(lp,output);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,file.getAbsolutePath());
    } catch (IOException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,file.getAbsolutePath());
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Saves single logical port to a file.
   * @param lp
   * @param file
   * @throws com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException
   */
  public void saveLogicalPort(LogicalPortType lp, File file) throws LogicalPortException {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(file);
      saveLogicalPort(lp,output);
    } catch (FileNotFoundException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,file.getAbsolutePath());
    } catch (IOException e) {
      throw new LogicalPortException(LogicalPortException.IOERROR,e,file.getAbsolutePath());
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Returns newly created stub for logical port.
   * Returns 'null' if stub can not be found or created.
   * Throws stub configuration exception if one of requested features is not available on server.
   */
  public BaseGeneratedStub getConfiguredStub(LogicalPorts ports, String portName, Hashtable protocolRegistry, ClientComponentFactory protocolFactory, ClassLoader loader, TypeMappingRegistryImpl registry) throws StubConfigurationException {
    if (loader == null) {
      loader = this.getClass().getClassLoader();
    }
    BaseGeneratedStub result = null;
    LogicalPortType[] lports = ports.getLogicalPort();
    for (int i=0; i<lports.length; i++) {
      if (lports[i].getName().equals(portName)) {
        LogicalPortType lport = lports[i];
        // Check nessary properties
        if (lport.getStubName() == null) {
          throw new StubConfigurationException(" Logical port ["+lport.getName()+"] must have associated stub to be used runtime !");
        }
//        if (lport.getEndpoint() == null) {
//          throw new StubConfigurationException(" Logical port must have endpoint specified to be used runtime !");
//        }
        try {
          Class stubClass = Class.forName(lport.getStubName(),false,loader);
          result = (BaseGeneratedStub) stubClass.newInstance();
        } catch (Exception e) {
          throw new StubConfigurationException(" Stub ["+lport.getStubName()+"] corresponding to logical port ["+lport.getName()+"] can not be instantiated or his class can not be loaded !",e);
        }
        //result._clear(); // Use only with pool enabled
        if (registry != null) {
          result._setTypeRegistry(registry);
        }
        result._setEndpoint(lport.getEndpoint());
        //ClientTransportBinding binding = result._getTransportBinding();
        ArrayList globalProtocols = result._getGlobalProtocols();
        PropertyContext globalContext = result._getGlobalFeatureConfig();
        if (lport.hasGlobalFeatures()) {
          GlobalFeatures gFeatures = null;
          gFeatures = lport.getGlobalFeatures();
          FeatureType[] features = gFeatures.getFeature();
          // Checks feature support
          for (int j=0; j<features.length; j++) {
            loadProperties(features[j].getProperty(),globalContext.getSubContext(features[j].getName()));
            String providerName = features[j].getProvider();
            if (providerName != null && ((ProtocolList) globalProtocols).getProtocol(providerName)==null) { // There is provides available
              AbstractProtocol protocol = null;
              if (protocolFactory != null) {
                try {
                  protocol = (AbstractProtocol) protocolFactory.getClientProtocolInstance(providerName);
                } catch (ComponentInstantiationException x) {
                  throw new StubConfigurationException("Unable to create instance of protocol with provider id ["+providerName+"].",x);
                }
              } else {
                protocol = (AbstractProtocol) protocolRegistry.get(providerName);
              }
              if (protocol != null) { // Protocol found
                try {
                  AbstractProtocol ap = protocol;
                  if (protocolFactory == null) {
                    ap = (AbstractProtocol) protocol.getClass().newInstance();
                  }
                  try {
                    ap.init(globalContext);
                  } catch (ClientProtocolException e) {
                    throw new StubConfigurationException("Protocol "+providerName+" was unable to initialize on init() ! It is used in logical port ["+lport.getName()+"].");
                  }
                  globalProtocols.add(ap);
                } catch (InstantiationException e) {
                  throw new StubConfigurationException("Unable to create instance of protocol(instantiation error): "+protocol.getClass().getName());
                } catch (IllegalAccessException e) {
                  throw new StubConfigurationException("Unable to create instance of protocol(illegal access): "+protocol.getClass().getName());
                }
                //loadProperties(features[j].getProperty(),globalContext.getSubContext(providerName).getSubContext(features[j].getName()));
              } else {
                if (protocolFactory != null) { // Engine provides protocols
                  throw new StubConfigurationException("Unable to find protocol with provider id ["+providerName+"].");
                }
                // Protocol not found
                //@todo nothing is done here some warning should displayed
                System.out.println("Warning ! Provider ["+providerName+"] not found for feature in logical port ["+lport.getName()+"] !");
                //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName+"'");
              }
            }
          }
        }
        if (lport.hasLocalFeatures()) {
          LocalFeatures localFeatures = null;
          localFeatures = lport.getLocalFeatures();
          OperationType[] operations = localFeatures.getOperation();
          for (int j=0; j<operations.length; j++) {
            String operationName = operations[j].getName();
            if (operationName != null) {
              FeatureType[] features = operations[j].getFeature();
              ArrayList localProtocols = result._getOperationProtocols(operationName);
              PropertyContext localContext = result._getOperationFeatureConfig(operationName);
              if (localProtocols == null || localContext == null) {
                throw new StubConfigurationException(" Operation described in LP does not exist !");
              }
              // Checks feature support
              for (int k=0; k<features.length; k++) {
                loadProperties(features[k].getProperty(),localContext.getSubContext(features[k].getName()));
                String providerName = features[k].getProvider();
                if (providerName != null) { // There is provider available
                  // If provider is already available for this feature does not add it twice
                  if (((ProtocolList) globalProtocols).getProtocol(providerName)!=  null) {
                    continue;
                  }
                  AbstractProtocol protocol = null;
                  if (protocolFactory != null) {
                    try {
                      protocol = (AbstractProtocol) protocolFactory.getClientProtocolInstance(providerName);
                    } catch (ComponentInstantiationException x) {
                      throw new StubConfigurationException("Unable to create instance of protocol with provider id ["+providerName+"].",x);
                    }
                  } else {
                    protocol = (AbstractProtocol) protocolRegistry.get(providerName);
                  }
                  if (protocol != null) {
                    localProtocols.add(protocol);
                    //loadProperties(features[k].getProperty(),localContext.getSubContext(providerName).getSubContext(features[k].getName()));
                  } else {
                    //@todo nothing is done here some warning should displayed
                    System.out.println("Provider ["+providerName+"] not found !");
                    //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName);
                  }
                }
              }
            } else {
              throw new StubConfigurationException(" Name attribute of operation is required !");
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns newly created stub for logical port.
   * Returns 'null' if stub can not be found or created.
   * Throws stub configuration exception if one of requested features is not available on server.
   * @deprecated This is not used anymore
   */
  public BaseGeneratedStub getConfiguredStub(LogicalPorts ports, String portName, Hashtable protocolRegistry, Hashtable bindingRegistry, ClassLoader loader) throws StubConfigurationException {
    if (loader == null) {
      loader = this.getClass().getClassLoader();
    }
    BaseGeneratedStub result = null;
    LogicalPortType[] lports = ports.getLogicalPort();
    for (int i=0; i<lports.length; i++) {
      if (lports[i].getName().equals(portName)) {
        LogicalPortType lport = lports[i];
        // Check nessary properties
        if (lport.getStubName() == null) {
          throw new StubConfigurationException(" Logical port ["+lport.getName()+"] must have associated stub to be used runtime !");
        }
        String bindingName = lport.getBindingImplementation();
        if (bindingName == null) {
          throw new StubConfigurationException(" Logical port ["+lport.getName()+"] must have binding implementation set to be used runtime !");
        }
        ClientTransportBinding binding = (ClientTransportBinding) bindingRegistry.get(bindingName);
        if (binding == null) {
          throw new StubConfigurationException(" Cannot find binding with name ["+bindingName+"] used in logical port ["+lport.getName()+"] !");
        }
//        if (lport.getEndpoint() == null) {
//          throw new StubConfigurationException(" Logical port must have endpoint specified to be used runtime !");
//        }
        try {
          Class stubClass = Class.forName(lport.getStubName(),false,loader);
          result = (BaseGeneratedStub) stubClass.newInstance();
        } catch (Exception e) {
          throw new StubConfigurationException(" Stub ["+lport.getStubName()+"] corresponding to logical port ["+lport.getName()+"] can not be instantiated or his class can not be loaded !",e);
        }
        result._setTransportBinding(binding);
        //result._clear(); // Use only with pool enabled
        result._setEndpoint(lport.getEndpoint());
        //ClientTransportBinding binding = result._getTransportBinding();
        ArrayList globalProtocols = result._getGlobalProtocols();
        PropertyContext globalContext = result._getGlobalFeatureConfig();
        if (lport.hasGlobalFeatures()) {
          GlobalFeatures gFeatures = null;
          gFeatures = lport.getGlobalFeatures();
          FeatureType[] features = gFeatures.getFeature();
          // Checks feature support
          for (int j=0; j<features.length; j++) {
            loadProperties(features[j].getProperty(),globalContext.getSubContext(features[j].getName()));
            String providerName = features[j].getProvider();
            if (providerName != null) { // There is provides available
              AbstractProtocol protocol = (AbstractProtocol) protocolRegistry.get(providerName);
              if (protocol != null && ((ProtocolList) globalProtocols).getProtocol(providerName)==null) { // Protocol found
                try {
                  AbstractProtocol ap = (AbstractProtocol) protocol.getClass().newInstance();
                  try {
                    ap.init(globalContext);
                  } catch (ClientProtocolException e) {
                    throw new StubConfigurationException("Protocol "+providerName+" was unable to initialize on init() ! It is used in logical port ["+lport.getName()+"].");
                  }
                  globalProtocols.add(ap);
                } catch (InstantiationException e) {
                  throw new StubConfigurationException("Unable for create instance of protocol(instantiation error): "+protocol.getClass().getName());
                } catch (IllegalAccessException e) {
                  throw new StubConfigurationException("Unable for create instance of protocol(illegal access): "+protocol.getClass().getName());
                }
                //loadProperties(features[j].getProperty(),globalContext.getSubContext(providerName).getSubContext(features[j].getName()));
              } else {
                // Protocol not found
                //@todo nothing is done here some warning should displayed
                System.out.println("Warning ! Provider ["+providerName+"] not found for feature in logical port ["+lport.getName()+"] !");
                //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName+"'");
              }
            }
          }
        }
        if (lport.hasLocalFeatures()) {
          LocalFeatures localFeatures = null;
          localFeatures = lport.getLocalFeatures();
          OperationType[] operations = localFeatures.getOperation();
          for (int j=0; j<operations.length; j++) {
            String operationName = operations[j].getName();
            if (operationName != null) {
              FeatureType[] features = operations[j].getFeature();
              ArrayList localProtocols = result._getOperationProtocols(operationName);
              PropertyContext localContext = result._getOperationFeatureConfig(operationName);
              if (localProtocols == null || localContext == null) {
                throw new StubConfigurationException(" Operation described in LP does not exist !");
              }
              // Checks feature support
              for (int k=0; k<features.length; k++) {
                loadProperties(features[k].getProperty(),localContext.getSubContext(features[k].getName()));
                String providerName = features[k].getProvider();
                if (providerName != null) { // There is provider available
                  AbstractProtocol protocol = (AbstractProtocol) protocolRegistry.get(providerName);
                  if (protocol != null) {
                    localProtocols.add(protocol);
                    //loadProperties(features[k].getProperty(),localContext.getSubContext(providerName).getSubContext(features[k].getName()));
                  } else {
                    //@todo nothing is done here some warning should displayed
                    System.out.println("Provider ["+providerName+"] not found !");
                    //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName);
                  }
                }
              }
            } else {
              throw new StubConfigurationException(" Name attribute of operation is required !");
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Creates and returns newly configured Server Managed Stub.
   * Returns 'null' if stub can not be found or created.
   * Throws stub configuration exception if one of requested features is not available on server.
   */
  public BaseGeneratedStub getConfiguredStub(LogicalPortType lport, RuntimeInformation runtimeInfo, ClientComponentFactory cfactory, ClassLoader loader, TypeMappingRegistry typeRegistry) throws StubConfigurationException {
    if (loader == null) {
      loader = this.getClass().getClassLoader();
    }
    BaseGeneratedStub result = null;
    // Check nessary properties
    if (lport.getStubName() == null) {
      throw new StubConfigurationException(" Logical port <"+lport.getName()+"> must have stub property set. Possible deploy problem.");
    }
    String bindingName = lport.getBindingImplementation();
    if (bindingName == null) {
      throw new StubConfigurationException(" Logical port <"+lport.getName()+"> must have binding implementation set. The client is invalid.");
    }
    ClientTransportBinding binding = null;
    try {
      binding = (ClientTransportBinding) cfactory.getClientTransportBindingInstance(bindingName);
    } catch (ComponentInstantiationException e) {
      throw new StubConfigurationException(" Unable to get implmementation of binding <"+bindingName+"> used in logical port <"+lport.getName()+">.",e);
    }
    if (binding == null) {
      throw new StubConfigurationException(" Unable to get implementation of binding <"+bindingName+"> used in logical port <"+lport.getName()+">.");
    }
    try {
      Class stubClass = Class.forName(lport.getStubName(),false,loader);
      result = (BaseGeneratedStub) stubClass.newInstance();
    } catch (Exception e) {
      throw new StubConfigurationException(" Stub <"+lport.getStubName()+"> corresponding to logical port <"+lport.getName()+"> can not be instantiated or his class can not be loaded !",e);
    }
    if (typeRegistry != null) {
      binding.setTypeMappingRegistry(typeRegistry);
      result._getGlobalFeatureConfig().setProperty("typeMapping",typeRegistry.getDefaultTypeMapping());
    } else {
      try {
        TypeMappingRegistryImpl reg = new TypeMappingRegistryImpl();
        binding.setTypeMappingRegistry(typeRegistry);
        result._getGlobalFeatureConfig().setProperty("typeMapping",reg.getDefaultTypeMapping());
      } catch (Exception x) {
        throw new StubConfigurationException(" Stub <"+lport.getStubName()+"> corresponding to logical port <"+lport.getName()+"> can not be instantiated or his class can not be loaded !",x);
      }
    }
    result._setTransportBinding(binding);
    result._setEndpoint(lport.getEndpoint());
    ArrayList globalProtocols = result._getGlobalProtocols();
    PropertyContext globalContext = result._getGlobalFeatureConfig();
    globalContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
    if (lport.hasGlobalFeatures()) {
      GlobalFeatures gFeatures = null;
      gFeatures = lport.getGlobalFeatures();
      FeatureType[] features = gFeatures.getFeature();
      // Checks feature support
      for (int j=0; j<features.length; j++) {
        loadProperties(features[j].getProperty(),globalContext.getSubContext(features[j].getName()));
        String providerName = features[j].getProvider();
        if (providerName != null && ((ProtocolList) globalProtocols).getProtocol(providerName)==null) {
          // There is provider for this feature.
          AbstractProtocol protocol = null;
          try {
            protocol = (AbstractProtocol) cfactory.getClientProtocolInstance(providerName);
          } catch (ComponentInstantiationException e) {
            throw new StubConfigurationException(" Unable to get instance of feature provider with name <"+providerName+"> used in logical port <"+lport.getName()+">.",e);
          }
          if (protocol == null) {
            throw new StubConfigurationException(" Unable to get instance of feature provider with name <"+providerName+"> used in logical port <"+lport.getName()+">.");
          }
          try {
            protocol.init(globalContext);
          } catch (ClientProtocolException e) {
            throw new StubConfigurationException("Protocol "+providerName+" was unable to initialize on init() ! It is used in logical port <"+lport.getName()+">.",e);
          }
          globalProtocols.add(protocol);
        }
      }
    }
    if (lport.hasLocalFeatures()) {
      LocalFeatures localFeatures = null;
      localFeatures = lport.getLocalFeatures();
      OperationType[] operations = localFeatures.getOperation();
      for (int j=0; j<operations.length; j++) {
        String operationName = operations[j].getName();
        if (operationName != null) {
          FeatureType[] features = operations[j].getFeature();
          ArrayList localProtocols = result._getOperationProtocols(operationName);
          PropertyContext localContext = result._getOperationFeatureConfig(operationName);
          if (localProtocols == null || localContext == null) {
            throw new StubConfigurationException(" Operation named <"+operationName+"> does not exist in logical port <"+lport.getName()+">.");
          }
          // Checks feature support
          for (int k=0; k<features.length; k++) {
            loadProperties(features[k].getProperty(),localContext.getSubContext(features[k].getName()));
            String providerName = features[k].getProvider();
            if (providerName != null) { // There is provider available
              // If provider is already available for this feature does not add it twice
              if (((ProtocolList) globalProtocols).getProtocol(providerName)!=  null) {
                continue;
              }
              AbstractProtocol protocol = null;
              try {
                protocol = (AbstractProtocol) cfactory.getClientProtocolInstance(providerName);
              } catch (ComponentInstantiationException e) {
                throw new StubConfigurationException(" Unable to get instance of feature provider with name <"+providerName+"> used in logical port <"+lport.getName()+">.",e);
              }
              if (protocol == null) {
                throw new StubConfigurationException(" Unable to get instance of feature provider with name <"+providerName+"> used in logical port <"+lport.getName()+">.");
              }
              try {
                protocol.init(globalContext);
              } catch (ClientProtocolException e) {
                throw new StubConfigurationException("Protocol <"+providerName+"> was unable to initialize on init(). It is used in logical port <"+lport.getName()+">.",e);
              }
              localProtocols.add(protocol);
            }
          }
        } else {
          throw new StubConfigurationException(" Name attribute of operation is required in logical port <"+lport.getName()+"> configuration.");
        }
      }
    }
    return result;
  }


  public void loadProperties(PropertyType[] properties, PropertyContext context) {
    context.define();
    for (int i=0; i<properties.length; i++) {
      if (properties[i].getName() != null) {
        if (properties[i].getValue() != null) {
          context.setProperty(properties[i].getName(),properties[i].getValue());
        }
        if (properties[i].getProperty().length != 0) {
          loadProperties(properties[i].getProperty(),context.getSubContext(properties[i].getName()));
        }
      }
    }
  }

  /**
   * Fills feature contents from sap feature to logical port feature. Existing properties are overlapped.
   * @param feature
   * @param sapFeature
   */
  private static void fillFeature(FeatureType feature, SAPFeature sapFeature) {
    String provider =  DefaultProviders.getProvider(feature.getName());
    if (provider != null) {
      feature.setProvider(provider);
    }
    feature.setOriginal(true); // Sets original flag
    ArrayList sapProperties = sapFeature.getProperties();
    for (int i=0; i<sapProperties.size(); i++) {
      PropertyType lproperty = new PropertyType();
      SAPProperty property = (SAPProperty) sapProperties.get(i);
      com.sap.engine.lib.xml.util.QName qname = property.getQname();
      lproperty.setName(qname.getLocalName());
      if (property.getOptions().size() !=0) {
        SAPOption option = (SAPOption) property.getOptions().get(0);
        lproperty.setValue(option.getValue());
      }
      feature.addProperty(lproperty);
    }
  }


  /**
   * Load Feature information from PortType. Needs only portType structure to load features.
   * @param lport
   * @param portType
   * @throws LogicalPortException
   */
  public static void fillLogicalPortTemplate(LogicalPortType lport, WSDLPortType portType, WSDLDefinitions definitions) throws LogicalPortException {
    // Loading global features
    GlobalFeatures globalFeatures = null;
    ArrayList global = portType.getUseFeatures();
    for (int i=0; i<global.size(); i++) {
      SAPUseFeature useFeature = (SAPUseFeature) global.get(i);
      SAPFeature sapFeature = definitions.getFeatureByName(useFeature.getFeatureQName());
      if (sapFeature == null) {
        throw new LogicalPortException(LogicalPortException.BUGGY_FEATURE,"{"+useFeature.getFeatureQName().getURI()+"}"+useFeature.getFeatureQName().getLocalName());
      }
      FeatureType feature = null;
      if (globalFeatures != null) {
        feature = globalFeatures.getFeature(sapFeature.getUri());
      } else { // if there are no global features create it
        globalFeatures = new GlobalFeatures();
        lport.setGlobalFeatures(globalFeatures);
      }
      if (feature == null) {
        feature = new FeatureType();
        feature.setName(sapFeature.getUri());
        globalFeatures.addFeature(feature);
      }
      fillFeature(feature, sapFeature);
    }
    // Loads local features
    LocalFeatures localFeatures = new LocalFeatures();
    ArrayList operations = portType.getOperations();
    for (int i=0; i<operations.size(); i++) {
      WSDLOperation operation = (WSDLOperation) operations.get(i);
      OperationType loperation = localFeatures.getOperation(operation.getName());
      if (loperation == null) {
        loperation = new OperationType();
        loperation.setName(operation.getName());
        localFeatures.addOperation(loperation);
      }
      if (operation.getUseFeatures().size() !=0) {
        ArrayList ouseFeatures = operation.getUseFeatures();
        for (int j=0; j<ouseFeatures.size(); j++) {
          SAPUseFeature sapUseFeature = (SAPUseFeature) ouseFeatures.get(j);
          SAPFeature sapFeature = definitions.getFeatureByName(sapUseFeature.getFeatureQName());
          if (sapFeature == null) {
            throw new LogicalPortException(LogicalPortException.BUGGY_FEATURE,"{"+sapUseFeature.getFeatureQName().getURI()+"}"+sapUseFeature.getFeatureQName().getLocalName());
          }
          FeatureType feature = loperation.getFeature(sapFeature.getUri());
          if (feature == null) {
            feature = new FeatureType();
            feature.setName(sapFeature.getUri());
            loperation.addFeature(feature);
          }
          fillFeature(feature , sapFeature);
        }
      }
    }
  }

  public static void fillLogicalPort( LogicalPortType lport, WSDLBinding binding, ClientTransportBinding tbinding, WSDLDefinitions definitions) throws LogicalPortException {
    // Loading global features
    GlobalFeatures globalFeatures = null;
    if (lport.hasGlobalFeatures()) {
      globalFeatures = lport.getGlobalFeatures();
    }
    ArrayList global = binding.getUseFeatures();
    for (int i=0; i<global.size(); i++) {
      SAPUseFeature useFeature = (SAPUseFeature) global.get(i);
      SAPFeature sapFeature = definitions.getFeatureByName(useFeature.getFeatureQName());
      if (sapFeature == null) {
        throw new LogicalPortException(LogicalPortException.BUGGY_FEATURE,"{"+useFeature.getFeatureQName().getURI()+"}"+useFeature.getFeatureQName().getLocalName());
      }
      FeatureType feature = null;
      if (globalFeatures != null) {
        feature = globalFeatures.getFeature(sapFeature.getUri());
      } else { // if there are no global features create it
        globalFeatures = new GlobalFeatures();
        lport.setGlobalFeatures(globalFeatures);
      }
      if (feature == null) {
        feature = new FeatureType();
        feature.setName(sapFeature.getUri());
      }
      fillFeature(feature, sapFeature);
      globalFeatures.addFeature(feature);
    }
    if (globalFeatures == null) {
      globalFeatures = new GlobalFeatures();
    }
    tbinding.importGlobalFeatures(globalFeatures, binding);
    lport.setGlobalFeatures(globalFeatures);

    // Loads local features
    LocalFeatures localFeatures = lport.getLocalFeatures();
    if (localFeatures == null) {
      localFeatures = new LocalFeatures();
      lport.setLocalFeatures(localFeatures);
    }
    ArrayList operations = binding.getOperations();
    for (int i=0; i<operations.size(); i++) {
      WSDLBindingOperation operation = (WSDLBindingOperation) operations.get(i);
      OperationType loperation = localFeatures.getOperation(operation.getName());
      if (loperation == null) {
        loperation = new OperationType();
        loperation.setName(operation.getName());
        localFeatures.addOperation(loperation);
      }
      if (operation.getUseFeatures().size() !=0) {
        ArrayList ouseFeatures = operation.getUseFeatures();
        for (int j=0; j<ouseFeatures.size(); j++) {
          SAPUseFeature sapUseFeature = (SAPUseFeature) ouseFeatures.get(j);
          SAPFeature sapFeature = definitions.getFeatureByName(sapUseFeature.getFeatureQName());
          if (sapFeature == null) {
            throw new LogicalPortException(LogicalPortException.BUGGY_FEATURE,"{"+sapUseFeature.getFeatureQName().getURI()+"}"+sapUseFeature.getFeatureQName().getLocalName());
          }
          FeatureType feature = loperation.getFeature(sapFeature.getUri());
          if (feature == null) {
            feature = new FeatureType();
            feature.setName(sapFeature.getUri());
            loperation.addFeature(feature);
          }
          fillFeature(feature , sapFeature);
        }
      }
    }
  }


  /**
   * Loads Logical Port features and endpoint from external WSDL.
   * All client settings are lost.
   * @param lport
   * @param definisions
   * @param portName
   * @param cfactory
   */
  public static void updateLogicalPort(LogicalPortType lport, WSDLDefinitions definisions, QName portName, ClientComponentFactory cfactory) throws LogicalPortException {
    String bindingName = lport.getBindingImplementation();
    if (bindingName == null) {
      throw new LogicalPortException(LogicalPortException.MISSING_BINDING_IMPL,lport.getName());
    }
    ClientTransportBinding binding = null;
    try {
      binding = (ClientTransportBinding) cfactory.getClientTransportBindingInstance(bindingName);
    } catch (ComponentInstantiationException e) {
      throw new LogicalPortException(LogicalPortException.UREACHABLE_BINDING_IMPL,e,bindingName,lport.getName());
    }
    if (binding == null) {
      throw new LogicalPortException(LogicalPortException.UREACHABLE_BINDING_IMPL,bindingName,lport.getName());
    }
    ArrayList services = definisions.getServices();
    if (services.size() == 0) {
      throw new LogicalPortException(LogicalPortException.UPDATEIMPOSSIBLE,lport.getName());
    }
    WSDLPort rootPort = null;
    for (int i=0; i<services.size(); i++) {
      WSDLService service = (WSDLService) services.get(i);
      ArrayList ports = service.getPorts();
      for (int j=0; j<ports.size(); j++) {
        WSDLPort port = (WSDLPort) ports.get(j);
        if (port.getName().equals(portName.getLocalPart())) {
          if (portName.getNamespaceURI() == port.getNamespace()) { //$JL-STRING$
            // both namespaces are null
            rootPort = port;
            break;
          }
          if (portName.getNamespaceURI() != null && portName.getNamespaceURI().equals(port.getNamespace())) {
            // bot namespaces are equal non null
            rootPort = port;
            break;
          }
        }
      }
      if (rootPort != null) break;
    }
    if (rootPort == null) {
      throw new LogicalPortException(LogicalPortException.UPDATEIMPOSSIBLE,lport.getName());
    }
    try {
      // Loads the new endpoint from wsdl
      String newEndpoint = binding.loadAddress(rootPort.getExtension());
      lport.setEndpoint(newEndpoint);
    } catch (WSDLException e) {
      throw new LogicalPortException(LogicalPortException.UPDATEIMPOSSIBLE,e,lport.getName());
    }
//    WSDLBinding wsdlBinding = definisions.getBinding(rootPort.getBinding());
//    if (wsdlBinding == null) {
//      throw new LogicalPortException(LogicalPortException.UPDATEIMPOSSIBLE,lport.getName());
//    }
//    WSDLPortType portType = definisions.getPortType(wsdlBinding.getType().getLocalName(),wsdlBinding.getType().getURI());
//    if (portType == null) {
//      throw new LogicalPortException(LogicalPortException.UPDATEIMPOSSIBLE,lport.getName());
//    }
//    fillLogicalPortTemplate(lport,portType,definisions);
//    fillLogicalPort(lport,wsdlBinding,binding,definisions);
  }

  /**
   * Returns newly created stub for logical port.
   * Returns 'null' if stub can not be found or created.
   * Throws stub configuration exception if one of requested features is not available on server.
   * @deprecated This is not used anymore
   */
  public BaseGeneratedStub getConfiguredStubServer(LogicalPortType lport, RuntimeInformation runtimeInfo, ClientComponentFactory cfactory,Hashtable protocolRegistry, ClassLoader loader) throws StubConfigurationException {
    if (loader == null) {
      loader = this.getClass().getClassLoader();
    }
    BaseGeneratedStub result = null;
    // Check nessary properties
    if (lport.getStubName() == null) {
      throw new StubConfigurationException(" Logical port ["+lport.getName()+"] must have associated stub to be used runtime !");
    }
    String bindingName = lport.getBindingImplementation();
    if (bindingName == null) {
      throw new StubConfigurationException(" Logical port ["+lport.getName()+"] must have binding implementation set to be used runtime !");
    }
    ClientTransportBinding binding = null;
    try {
      binding = (ClientTransportBinding) cfactory.getClientTransportBindingInstance(bindingName);
    } catch (ComponentInstantiationException e) {
      throw new StubConfigurationException(" Unable to get instance of binding ["+bindingName+"] used in logical port ["+lport.getName()+"] !",e);
    }
    if (binding == null) {
      throw new StubConfigurationException(" Can not find binding with name ["+bindingName+"] used in logical port ["+lport.getName()+"] !");
    }
    try {
      Class stubClass = Class.forName(lport.getStubName(),false,loader);
      result = (BaseGeneratedStub) stubClass.newInstance();
    } catch (Exception e) {
      throw new StubConfigurationException(" Stub ["+lport.getStubName()+"] corresponding to logical port ["+lport.getName()+"] can not be instantiated or his class can not be loaded !",e);
    }
    result._setTransportBinding(binding);
        //result._clear(); // Use only with pool enabled
    result._setEndpoint(lport.getEndpoint());
        //ClientTransportBinding binding = result._getTransportBinding();
    ArrayList globalProtocols = result._getGlobalProtocols();
    PropertyContext globalContext = result._getGlobalFeatureConfig();
    globalContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
    if (lport.hasGlobalFeatures()) {
      GlobalFeatures gFeatures = null;
      gFeatures = lport.getGlobalFeatures();
      FeatureType[] features = gFeatures.getFeature();
      // Checks feature support
      for (int j=0; j<features.length; j++) {
        loadProperties(features[j].getProperty(),globalContext.getSubContext(features[j].getName()));
        String providerName = features[j].getProvider();
        if (providerName != null) { // There is provides available
          AbstractProtocol protocol = (AbstractProtocol) protocolRegistry.get(providerName);
          if (protocol != null && ((ProtocolList) globalProtocols).getProtocol(providerName)==null) { // Protocol found
            try {
              protocol = (AbstractProtocol) cfactory.getClientProtocolInstance(providerName);
            } catch (ComponentInstantiationException e) {
              throw new StubConfigurationException(" Unable to get instance of binding ["+bindingName+"] used in logical port ["+lport.getName()+"] !",e);
            }
            try {
              protocol.init(globalContext);
            } catch (ClientProtocolException e) {
              throw new StubConfigurationException("Protocol "+providerName+" was unable to initialize on init() ! It is used in logical port ["+lport.getName()+"].");
            }
            globalProtocols.add(protocol);
          } else {
            //@todo nothing is done here some warning should displayed
            System.out.println("Warning ! Provider ["+providerName+"] not found for feature in logical port ["+lport.getName()+"] !");//$JL-SYS_OUT_ERR$
            //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName+"'");
          }
        }
      }
    }
    if (lport.hasLocalFeatures()) {
      LocalFeatures localFeatures = null;
      localFeatures = lport.getLocalFeatures();
      OperationType[] operations = localFeatures.getOperation();
      for (int j=0; j<operations.length; j++) {
        String operationName = operations[j].getName();
        if (operationName != null) {
          FeatureType[] features = operations[j].getFeature();
          ArrayList localProtocols = result._getOperationProtocols(operationName);
          PropertyContext localContext = result._getOperationFeatureConfig(operationName);
          if (localProtocols == null || localContext == null) {
            throw new StubConfigurationException(" Operation described in LP does not exist !");
          }
          // Checks feature support
          for (int k=0; k<features.length; k++) {
            loadProperties(features[k].getProperty(),localContext.getSubContext(features[k].getName()));
            String providerName = features[k].getProvider();
            if (providerName != null) { // There is provider available
              AbstractProtocol protocol = (AbstractProtocol) protocolRegistry.get(providerName);
              if (protocol != null) {
                try {
                  protocol = (AbstractProtocol) cfactory.getClientProtocolInstance(providerName);
                } catch (ComponentInstantiationException e) {
                  throw new StubConfigurationException(" Unable to get instance of binding ["+bindingName+"] used in logical port ["+lport.getName()+"] !",e);
                }
                try {
                  protocol.init(globalContext);
                } catch (ClientProtocolException e) {
                  throw new StubConfigurationException("Protocol "+providerName+" was unable to initialize on init() ! It is used in logical port ["+lport.getName()+"].");
                }
                localProtocols.add(protocol);
              } else {
                //@todo nothing is done here some warning should displayed
                System.out.println("Provider ["+providerName+"] not found !");//$JL-SYS_OUT_ERR$
                //throw new StubConfigurationException(" Unable to find feature implementation ! Specifically protocol named '"+providerName);
              }
            }
          }
        } else {
          throw new StubConfigurationException(" Name attribute of operation is required !");
        }
      }
    }
    return result;
  }

}
