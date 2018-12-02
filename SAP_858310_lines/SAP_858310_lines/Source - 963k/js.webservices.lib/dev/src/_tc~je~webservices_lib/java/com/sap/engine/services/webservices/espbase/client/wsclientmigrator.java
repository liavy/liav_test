/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.*;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.FeatureType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.GlobalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;

/**
 * Class that implements WS Client migration logic.
 * @version 1.0 (2006-1-13)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class WSClientMigrator {

  /**
   * Method that implements logical port migration for 6.30 deployable web services clients.
   */
  public static void migrateLogicalPorts(ProxyGeneratorConfigNew config) {
    // Migrate service data - set Service Interface Name
    MappingRules mappingRules = config.getMappingRules();
    ConfigurationRoot configRoot = config.getProxyConfig();
    Service[] servicesConfig = configRoot.getRTConfig().getService();    
    LogicalPorts logicalPorts = config.getLogicalPorts();
    ServiceMapping[] services = mappingRules.getService();
    if (services.length == 0 || services.length != 1) {
      throw new RuntimeException("Migration of web service clients with no service tag is not possible");
    }
    ServiceMapping serviceMapping = services[0];
    Service serviceConfig = servicesConfig[0];
    serviceMapping.setSIName(logicalPorts.getInterfaceName());
    serviceConfig.setName(logicalPorts.getName());
    
    // Migrate service endpoint interface data (Logical Port Data)
    LogicalPortType[] lPorts = logicalPorts.getLogicalPort();
    Definitions definitions = config.getWsdl();
    // Clear the original configuration data
    ServiceData serviceData = serviceConfig.getServiceData();
    ArrayList newLogicalPorts = new ArrayList();
    for (int i=0; i < lPorts.length; i++) {
      // For each logical port find the matching interface mapping and update the SEI name.
      LogicalPortType lPort = lPorts[i];
      QName bindingQName = new QName(lPort.getBindingUri(),lPort.getBindingName());      
      Binding bindingDefinition = definitions.getBinding(bindingQName);
      QName portTypeQName = bindingDefinition.getInterface();
      InterfaceMapping iMapping = mappingRules.getInterface(portTypeQName,bindingQName);
      // if there is no interface mapping then the interface dublicates with other interface - replace the binding name
      if (iMapping == null) { // Try to find interface witht the same portType
        InterfaceMapping[] interfaceMappings = mappingRules.getInterface();
        for (int j = 0; j < interfaceMappings.length; j++) {
          if (interfaceMappings[j].getPortType().equals(portTypeQName)) {
            iMapping = interfaceMappings[j];
            bindingQName = interfaceMappings[j].getBindingQName();
            break;
          }
        }
      }
      if (iMapping == null) {
        throw new RuntimeException("Can not find corresponding interface mapping for logical port !");
      }
      iMapping.setSEIName(lPort.getInterfaceName());
      // For each logical port it creates a BindingData in the configuration model
      String interfaceMappingId = iMapping.getInterfaceMappingID();
      InterfaceDefinition interfaceDefinition = getInterfaceDefinition(interfaceMappingId,configRoot);
      String variantName = interfaceDefinition.getVariant()[0].getName();
      String interfaceId = interfaceDefinition.getId();
      String endpointURL = lPort.getEndpoint();
      BindingData bData = new BindingData();
      bData.setName(lPort.getName());
      bData.setUrl(endpointURL);
      bData.setBindingName(bindingQName.getLocalPart());
      bData.setBindingNamespace(bindingQName.getNamespaceURI());
      bData.setActive(true);
      bData.setEditable(true);
      bData.setVariantName(variantName);
      bData.setInterfaceId(interfaceId);
      bData.setInterfaceMappingId(interfaceMappingId);
      bData.setPropertyList(new PropertyListType[] {new PropertyListType()});
      OperationData[] operations = interfaceDefinition.getVariant()[0].getInterfaceData().getOperation();
      OperationData[] newOperations = new OperationData[operations.length];
      for (int j=0; j < newOperations.length; j++) {
        newOperations[j] = new OperationData();
        newOperations[j].setName(operations[j].getName());
        newOperations[j].setPropertyList(new PropertyListType[] {new PropertyListType()});
      }
      bData.setOperation(newOperations);
      migrateLogicalPortSettings(lPort,bData); 
      newLogicalPorts.add(bData);
    }
    BindingData[] newPorts = new BindingData[newLogicalPorts.size()];
    newLogicalPorts.toArray(newPorts);
    serviceData.setBindingData(newPorts);
  }
  
  /**
   * This method implements the migration of old runtime settings to new runtime protocol settings.
   * @param lPort
   * @param bData
   */
  public static void migrateLogicalPortSettings(LogicalPortType lPort, BindingData bData) {
    PropertyListType propertyList = bData.getSinglePropertyList();
    PropertyType property = null;
    // Migrate the global feature information
    GlobalFeatures globalFeatures = lPort.getGlobalFeatures();
    // Migrate the timeout feature
    FeatureType feature = globalFeatures.getFeature(PublicProperties.F_TIMEOUT_NAMESPACE);
    if (feature != null) {
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType timeOut = feature.getProperty(PublicProperties.F_TIMEOUT_LOCALNAME);
      if (timeOut != null) {
        property = new PropertyType();
        property.setName(PublicProperties.F_TIMEOUT_LOCALNAME);
        property.setNamespace(PublicProperties.F_TIMEOUT_NAMESPACE);
        property.set_value(timeOut.getValue());
        propertyList.addProperty(property);
      }
    }
    // Migrate the message id feature
    feature = globalFeatures.getFeature(PublicProperties.F_MESSAGEID_NAMESPACE);
    if (feature != null) {
      property = new PropertyType();
      property.setName(PublicProperties.F_MESSAGEID_LOCALNAME);
      property.setNamespace(PublicProperties.F_MESSAGEID_NAMESPACE);
      // If the property is set, message id is send
      property.set_value(PublicProperties.F_MESSAGEID_USE);
      propertyList.addProperty(property);
    }
    // Migrates the keep alive feature
    feature = globalFeatures.getFeature(PublicProperties.F_KEEPALIVE_NAMESPACE);
    if (feature != null) {
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType keepAlive = feature.getProperty(PublicProperties.F_KEEPALIVE_PROPERTY);
      if (keepAlive != null) {
        property = new PropertyType();
        property.setName(PublicProperties.F_KEEPALIVE_PROPERTY);
        property.setNamespace(PublicProperties.F_KEEPALIVE_NAMESPACE);        
        property.set_value(keepAlive.getValue());
        propertyList.addProperty(property);
      }
    }
    // Migrates the compress response feature
    feature = globalFeatures.getFeature(PublicProperties.F_COMPRESS_RESPONSE_NAMESPACE);
    if (feature != null) {
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType compressResponse = feature.getProperty(PublicProperties.F_COMPRESS_RESPONSE_PROPERTY);
      if (compressResponse != null) {
        property = new PropertyType();
        property.setName(PublicProperties.F_COMPRESS_RESPONSE_PROPERTY);
        property.setNamespace(PublicProperties.F_COMPRESS_RESPONSE_NAMESPACE);        
        property.set_value(compressResponse.getValue());
        propertyList.addProperty(property);
      }
    }   
    // Migrates the proxy feature
    feature = globalFeatures.getFeature(PublicProperties.F_PROXY_NAMESPACE);
    if (feature != null) {
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType proxyHost = feature.getProperty(PublicProperties.F_PROXY_HOST);
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType proxyPort = feature.getProperty(PublicProperties.F_PROXY_PORT);
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType proxyUser = feature.getProperty(PublicProperties.F_PROXY_USER);
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType proxyPass = feature.getProperty(PublicProperties.F_PROXY_PASS);
      if (proxyHost != null) {
        property = new PropertyType();
        property.setName(PublicProperties.F_PROXY_HOST);
        property.setNamespace(PublicProperties.F_PROXY_NAMESPACE);
        property.set_value(proxyHost.getValue());
        propertyList.addProperty(property);
        if (proxyPort != null) {
          property = new PropertyType();
          property.setName(PublicProperties.F_PROXY_PORT);
          property.setNamespace(PublicProperties.F_PROXY_NAMESPACE);
          property.set_value(proxyPort.getValue());
          propertyList.addProperty(property);          
        }
        if (proxyUser != null) {
          property = new PropertyType();
          property.setName(PublicProperties.F_PROXY_USER);
          property.setNamespace(PublicProperties.F_PROXY_NAMESPACE);
          property.set_value(proxyUser.getValue());
          propertyList.addProperty(property);
          if (proxyPass != null) {
            property = new PropertyType();
            property.setName(PublicProperties.F_PROXY_PASS);
            property.setNamespace(PublicProperties.F_PROXY_NAMESPACE);
            property.set_value(proxyPass.getValue());
            propertyList.addProperty(property);            
          }
        }
      }
    }   
    // Migrates session feature
    feature = globalFeatures.getFeature(PublicProperties.F_SESSION_NAMESPACE); 
    if (feature != null) {      
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType sessionMethod = feature.getProperty(PublicProperties.F_SESSION_METHOD);
      com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType abapSession = feature.getProperty(PublicProperties.F_SESSION_ABAP);
      if (sessionMethod != null) {
        property = new PropertyType();
        property.setName(PublicProperties.F_SESSION_METHOD);
        property.setNamespace(PublicProperties.F_SESSION_NAMESPACE);
        property.set_value(sessionMethod.getValue());
        propertyList.addProperty(property);        
        if (abapSession != null) {
          property = new PropertyType();
          property.setName(PublicProperties.F_SESSION_ABAP);
          property.setNamespace(PublicProperties.F_SESSION_NAMESPACE);
          property.set_value("true");
          propertyList.addProperty(property);                  
        }
      }
    } else {
      // The default session method is http
      property = new PropertyType();
      property.setName(PublicProperties.F_SESSION_METHOD);
      property.setNamespace(PublicProperties.F_SESSION_NAMESPACE);
      property.set_value(PublicProperties.F_SESSION_METHOD_HTTP);
      propertyList.addProperty(property);              
    }
  }
  
  /**
   * Returns the interface definition for specific interface mapping id.
   * @param interfaceMappingId
   * @param configRoot
   * @return
   */
  public static InterfaceDefinition getInterfaceDefinition(String interfaceMappingId, ConfigurationRoot configRoot) {
    InterfaceDefinition[] interfaces = configRoot.getDTConfig().getInterfaceDefinition();
    for (int i=0; i < interfaces.length; i++) {
      InterfaceDefinition interfaceDefinition = interfaces[i];
      if (interfaceDefinition.getInterfaceMappingId().equals(interfaceMappingId)) {
        return interfaceDefinition;        
      }
    }
    return null;
  }
}
