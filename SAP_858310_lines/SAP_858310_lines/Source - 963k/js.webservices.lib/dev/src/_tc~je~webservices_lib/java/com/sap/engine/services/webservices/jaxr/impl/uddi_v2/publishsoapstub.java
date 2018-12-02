package com.sap.engine.services.webservices.jaxr.impl.uddi_v2;

// Import libraries
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;

public class PublishSoapStub extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub implements com.sap.engine.services.webservices.jaxr.impl.uddi_v2.Publish {

  // Proxy variables
  private com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl typeRegistry;

  public PublishSoapStub() {
    super();
    this.transportBinding = new com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding();
    try {
      this.typeRegistry = new com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl();
      this.typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream("com/sap/engine/services/webservices/jaxr/impl/uddi_v2/types.xml"),this.getClass().getClassLoader());
    } catch (java.lang.Exception e) {
      throw new RuntimeException("Cannot load type mapping information");
    }
    this.featureConfiguration.setProperty("typeMapping",this.typeRegistry.getDefaultTypeMapping());
    this.localProtocols.put("add_publisherAssertions",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("add_publisherAssertions",new PropertyContext());
    this.localProtocols.put("delete_binding",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("delete_binding",new PropertyContext());
    this.localProtocols.put("delete_business",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("delete_business",new PropertyContext());
    this.localProtocols.put("delete_publisherAssertions",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("delete_publisherAssertions",new PropertyContext());
    this.localProtocols.put("delete_service",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("delete_service",new PropertyContext());
    this.localProtocols.put("delete_tModel",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("delete_tModel",new PropertyContext());
    this.localProtocols.put("discard_authToken",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("discard_authToken",new PropertyContext());
    this.localProtocols.put("get_assertionStatusReport",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_assertionStatusReport",new PropertyContext());
    this.localProtocols.put("get_authToken",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_authToken",new PropertyContext());
    this.localProtocols.put("get_publisherAssertions",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_publisherAssertions",new PropertyContext());
    this.localProtocols.put("get_registeredInfo",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_registeredInfo",new PropertyContext());
    this.localProtocols.put("save_binding",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("save_binding",new PropertyContext());
    this.localProtocols.put("save_business",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("save_business",new PropertyContext());
    this.localProtocols.put("save_service",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("save_service",new PropertyContext());
    this.localProtocols.put("save_tModel",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("save_tModel",new PropertyContext());
    this.localProtocols.put("set_publisherAssertions",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("set_publisherAssertions",new PropertyContext());
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport addPublisherAssertions(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AddPublisherAssertions body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","add_publisherAssertions");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AddPublisherAssertions.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","add_publisherAssertions");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","add_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","add_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("add_publisherAssertions",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("add_publisherAssertions"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport deleteBinding(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteBinding body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","delete_binding");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteBinding.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","delete_binding");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","delete_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","delete_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("delete_binding",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("delete_binding"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport deleteBusiness(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteBusiness body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","delete_business");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteBusiness.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","delete_business");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","delete_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","delete_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("delete_business",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("delete_business"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport deletePublisherAssertions(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeletePublisherAssertions body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","delete_publisherAssertions");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeletePublisherAssertions.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","delete_publisherAssertions");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","delete_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","delete_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("delete_publisherAssertions",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("delete_publisherAssertions"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport deleteService(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteService body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","delete_service");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteService.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","delete_service");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","delete_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","delete_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("delete_service",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("delete_service"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport deleteTModel(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteTModel body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","delete_tModel");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DeleteTModel.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","delete_tModel");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","delete_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","delete_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("delete_tModel",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("delete_tModel"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport discardAuthToken(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscardAuthToken body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","discard_authToken");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DiscardAuthToken.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","discard_authToken");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","discard_authToken");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","discard_authToken");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("discard_authToken",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("discard_authToken"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AssertionStatusReport getAssertionStatusReport(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetAssertionStatusReport body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_assertionStatusReport");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetAssertionStatusReport.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","assertionStatusReport");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AssertionStatusReport.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AssertionStatusReport bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","get_assertionStatusReport");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_assertionStatusReport");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_assertionStatusReport");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_assertionStatusReport",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_assertionStatusReport"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AssertionStatusReport) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AuthToken getAuthToken(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetAuthToken body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_authToken");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetAuthToken.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","authToken");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AuthToken.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AuthToken bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","get_authToken");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_authToken");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_authToken");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_authToken",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_authToken"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.AuthToken) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions getPublisherAssertions(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetPublisherAssertions body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_publisherAssertions");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetPublisherAssertions.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","publisherAssertions");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","get_publisherAssertions");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_publisherAssertions",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_publisherAssertions"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RegisteredInfo getRegisteredInfo(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetRegisteredInfo body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_registeredInfo");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetRegisteredInfo.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","registeredInfo");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RegisteredInfo.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RegisteredInfo bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","get_registeredInfo");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_registeredInfo");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_registeredInfo");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_registeredInfo",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_registeredInfo"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RegisteredInfo) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail saveBinding(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveBinding body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","save_binding");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveBinding.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","bindingDetail");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","save_binding");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","save_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","save_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("save_binding",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("save_binding"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetail saveBusiness(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveBusiness body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","save_business");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveBusiness.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","businessDetail");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetail.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetail bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","save_business");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","save_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","save_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("save_business",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("save_business"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetail) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail saveService(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveService body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","save_service");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveService.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","serviceDetail");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","save_service");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","save_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","save_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("save_service",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("save_service"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail saveTModel(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveTModel body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","save_tModel");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SaveTModel.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","tModelDetail");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","save_tModel");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","save_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","save_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("save_tModel",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("save_tModel"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions setPublisherAssertions(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SetPublisherAssertions body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","set_publisherAssertions");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.SetPublisherAssertions.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","publisherAssertions");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","set_publisherAssertions");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","set_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","set_publisherAssertions");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("set_publisherAssertions",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("set_publisherAssertions"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.PublisherAssertions) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }


}
