package com.sap.engine.services.webservices.jaxr.impl.uddi_v2;

// Import libraries
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;

public class InquireSoapStub extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub implements com.sap.engine.services.webservices.jaxr.impl.uddi_v2.Inquire {

  // Proxy variables
  private com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl typeRegistry;

  public InquireSoapStub() {
    super();
    this.transportBinding = new com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding();
    try {
      this.typeRegistry = new com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl();
      this.typeRegistry.fromXML(this.getClass().getClassLoader().getResourceAsStream("com/sap/engine/services/webservices/jaxr/impl/uddi_v2/types.xml"),this.getClass().getClassLoader());
    } catch (java.lang.Exception e) {
      throw new RuntimeException("Can not load type mapping information");
    }
    this.featureConfiguration.setProperty("typeMapping",this.typeRegistry.getDefaultTypeMapping());
    this.localProtocols.put("find_binding",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("find_binding",new PropertyContext());
    this.localProtocols.put("find_business",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("find_business",new PropertyContext());
    this.localProtocols.put("find_relatedBusinesses",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("find_relatedBusinesses",new PropertyContext());
    this.localProtocols.put("find_service",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("find_service",new PropertyContext());
    this.localProtocols.put("find_tModel",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("find_tModel",new PropertyContext());
    this.localProtocols.put("get_bindingDetail",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_bindingDetail",new PropertyContext());
    this.localProtocols.put("get_businessDetail",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_businessDetail",new PropertyContext());
    this.localProtocols.put("get_businessDetailExt",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_businessDetailExt",new PropertyContext());
    this.localProtocols.put("get_serviceDetail",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_serviceDetail",new PropertyContext());
    this.localProtocols.put("get_tModelDetail",new com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList());
    this.localFeatures.put("get_tModelDetail",new PropertyContext());
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail findBinding(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindBinding body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","find_binding");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindBinding.class;
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
      bindingConfiguration.setProperty("soapAction","find_binding");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","find_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","find_binding");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("find_binding",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("find_binding"));
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

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessList findBusiness(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindBusiness body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","find_business");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindBusiness.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","businessList");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessList.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessList bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","find_business");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","find_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","find_business");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("find_business",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("find_business"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessList) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessesList findRelatedBusinesses(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindRelatedBusinesses body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","find_relatedBusinesses");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindRelatedBusinesses.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","relatedBusinessesList");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessesList.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessesList bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","find_relatedBusinesses");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","find_relatedBusinesses");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","find_relatedBusinesses");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("find_relatedBusinesses",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("find_relatedBusinesses"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.RelatedBusinessesList) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceList findService(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindService body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","find_service");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindService.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","serviceList");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceList.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceList bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","find_service");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","find_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","find_service");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("find_service",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("find_service"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceList) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelList findTModel(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindTModel body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","find_tModel");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.FindTModel.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","tModelList");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelList.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelList bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","find_tModel");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","find_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","find_tModel");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("find_tModel",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("find_tModel"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelList) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BindingDetail getBindingDetail(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBindingDetail body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_bindingDetail");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBindingDetail.class;
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
      bindingConfiguration.setProperty("soapAction","get_bindingDetail");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_bindingDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_bindingDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_bindingDetail",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_bindingDetail"));
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

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetail getBusinessDetail(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBusinessDetail body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_businessDetail");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBusinessDetail.class;
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
      bindingConfiguration.setProperty("soapAction","get_businessDetail");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_businessDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_businessDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_businessDetail",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_businessDetail"));
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

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetailExt getBusinessDetailExt(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBusinessDetailExt body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_businessDetailExt");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetBusinessDetailExt.class;
      this.inputParams[0].content = body;
      // Operation output params initialization
      this.outputParams = new ServiceParam[1];
      this.outputParams[0] = new ServiceParam();
      this.outputParams[0].isElement = true;
      this.outputParams[0].schemaName = new QName("urn:uddi-org:api_v2","businessDetailExt");
      this.outputParams[0].name = "body";
      this.outputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetailExt.class;
      // Operation faults initialization
      this.faultParams = new ServiceParam[1];
      this.faultParams[0] = new ServiceParam();
      this.faultParams[0].isElement = true;
      this.faultParams[0].schemaName = new QName("urn:uddi-org:api_v2","dispositionReport");
      this.faultParams[0].name = "body";
      this.faultParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport.class;
      com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetailExt bodyTemp;
      this.transportBinding.setTypeMappingRegistry(this.typeRegistry);
      this.transportBinding.startOperation(this.inputParams,this.outputParams,this.faultParams);
      // Binding Context initialization
      this.bindingConfiguration.clear();
      bindingConfiguration.setProperty("soapAction","get_businessDetailExt");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_businessDetailExt");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_businessDetailExt");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_businessDetailExt",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_businessDetailExt"));
      if (this.faultParams[0].content != null) {
        throw (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport) this.faultParams[0].content;
      }
      bodyTemp = (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.BusinessDetailExt) this.outputParams[0].content;
      return bodyTemp;
    } catch (com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport e) {
      throw e;
    } catch (javax.xml.rpc.soap.SOAPFaultException e) {
      throw e;
    } catch (java.lang.Exception e) {
      throw new RemoteException("Service call exception",e);
    }
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.ServiceDetail getServiceDetail(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetServiceDetail body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_serviceDetail");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetServiceDetail.class;
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
      bindingConfiguration.setProperty("soapAction","get_serviceDetail");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_serviceDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_serviceDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_serviceDetail",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_serviceDetail"));
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

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.TModelDetail getTModelDetail(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetTModelDetail body) throws java.rmi.RemoteException,com.sap.engine.services.webservices.jaxr.impl.uddi_v2.DispositionReport {
    try {
      // Operation input params initialization
      this.inputParams = new ServiceParam[1];
      this.inputParams[0] = new ServiceParam();
      this.inputParams[0].isElement = true;
      this.inputParams[0].schemaName = new QName("urn:uddi-org:api_v2","get_tModelDetail");
      this.inputParams[0].name = "body";
      this.inputParams[0].contentClass = com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.GetTModelDetail.class;
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
      bindingConfiguration.setProperty("soapAction","get_tModelDetail");
      bindingConfiguration.setProperty("style","document");
      bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
      PropertyContext bindingConfigurationX;
      bindingConfigurationX = bindingConfiguration.getSubContext("output");
      bindingConfigurationX.setProperty("operationName","get_tModelDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      bindingConfigurationX = bindingConfiguration.getSubContext("input");
      bindingConfigurationX.setProperty("operationName","get_tModelDetail");
      bindingConfigurationX.setProperty("use","literal");
      bindingConfigurationX.setProperty("namespace","urn:uddi-org:api_v2");
      bindingConfigurationX.setProperty("parts","body");
      super._fillEndpoint(bindingConfiguration);
      _buildOperationContext("get_tModelDetail",this.transportBinding);
      this.transportBinding.call(this.stubConfiguration,this.globalProtocols,_getOperationProtocols("get_tModelDetail"));
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


}
