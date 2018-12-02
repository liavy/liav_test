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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.*;
import com.sap.engine.services.webservices.wsdl.*;

import javax.xml.rpc.encoding.TypeMappingRegistry;
import java.io.File;

/**
 * Interface that must be implemented by all ClientTransportBinding implementations.
 * It serves two purposes :
 *  first it must assist and recognize binding extensions in WSDL's to Proxy Generator.
 *  if must be able to process (send/receive) abstract service message's in binding dependent way runtime.
 * All methods described here are intended for calling by other tool's not by end user anf Help is provided for
 * future transport binding writer. 
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */

public interface ClientTransportBinding extends FeatureProvider {
  
  /**
   * This is the name of the property containing service endpoint the stub or runtime must take care to put that property 
   */ 
  public static final String ENDPOINT = "endpointUrl";

  public static final String BINDING_CONFIG = "bindingConfig";

  public static final String FEATUTE_CONFIG = "featureConfig";

  public static final String TYPE_MAPPING = "typeMapping";

  public static final String TRANSPORT_INTERFACE = "transportInterface";

  public static final String APP_CLASSLOADER = "javax.xml.rpc.classloader"; // Client Interface classloader
  /**
   * Binding starts transmiting of soap operation. This method passes input/output/fault service params.    
   */ 
  public void startOperation(ServiceParam[] inputParams, ServiceParam[] outputParams, ServiceParam[] faultParams);

  /**
   * Calls webservice method. The order for webservice call is the following.
   * 1. startOperation (Stub job)
   * 2. initialize configuration context (Stub job) 
   * 3. invoke call method and pass context to binding. (Stub job)
   * 4. check for fault (Stub job)
   * 5. if fault get it and throw (Stub job)
   * 6. if not get output params and return. (Stub job)
   */ 
  public void call(PropertyContext context, ProtocolList globalProtocols, ProtocolList localProtocols)  throws Exception;      
  
  /**
   * Get's output parts.  
   */ 
  public ServiceParam[] getResponseParts();  
  
  /**
   * Returns fault parts.  
   */ 
  public ServiceParam[] getFaultParts();

  /**
   * This method must return true is binding is able to recognize itself in wsdl.
   * It is used by proxy generator to find out if this is binding implementation needed to use.    
   */ 
  public boolean recognizeBinding(WSDLBinding bindingElement);
  
  /**
   * This method must parse binding extension element and sets properties as it needs to be in context.
   * This element is binding top level extension element.
   */ 
  public void getMainBindingConfig(WSDLBinding binding, PropertyContext context) throws WSDLException;
  
  /**
   * This method reads operation binding extensions.
   * The corresponding  binded WSDL Operation is passed as a param and reference to WSDL definitions.
   * Set the properties your binding need in context as information for this operation to be invoked.
   */ 
  public void getOperationBindingConfig(WSDLBindingOperation obinding, WSDLOperation operation, PropertyContext context, WSDLDefinitions definitions) throws WSDLException;

  /**
   * Loads binding address from service port extension element.
   */ 
  public String loadAddress(WSDLExtension extension) throws WSDLException;
    
  /**
   * Used by Stub to set JAX-RPC Type Mapping registry to work with.
   */ 
  public void setTypeMappingRegistry(TypeMappingRegistry registry);
  
  /**
   * Returns type mapping registry if needed.
   */ 
  public TypeMappingRegistry getTypeMappingRegistry();
  
  /**
   * Checks if this Abstract Protocol is compatible with this binding.
   * Every Binding must provide abstract interfaces for extending by Protocol Writers. This method must 
   * check it some protocol is compitible with Binding specific interfaces and is called by the runtime.
   */ 
  public boolean isCompatible(AbstractProtocol protocol);
  
  /**
   * Called for generation of any custom binding classes for every operation.
   * Output dir is output directory for this package.
   * Advanced feature that give the binding to generate some binding dependant things and modify context.
   * For every operation. Before that getOperationBindingConfig is called so WSDLBindingOperation is not called.
   */ 
  public void generateCustom(String packageName, File outputDir, WSDLDefinitions definitions, PropertyContext context, WSDLOperation operation);
  
  
  /**
   * Called from proxy generator to give binding ability to add import statements into stub.
   */ 
  public void addImport(CodeGenerator generator);    
  
  /**
   * Adds constructor code. Called by proxy generator to give binding ability to add some stub constructor code. 
   */ 
  public void addConstructorCode(CodeGenerator generator);
  
  /**
   * Gives the binding ability to generate code for additional stub variables.
   */ 
  public void addVariables(CodeGenerator generator);

  /**
   * Emty call/notification.
   * @param context
   * @param globalProtocols
   * @throws Exception
   */
  public void flush(PropertyContext context,ProtocolList globalProtocols) throws Exception;

  /**
   * Implementing this method the binding can add Binding Specific Features.
   * @param globalFeatures
   * @param binding
   * @return
   */
  public GlobalFeatures importGlobalFeatures(GlobalFeatures globalFeatures, WSDLBinding binding);

  /**
   * Implementing this method the binding can add Operation Specific Features.
   * @param features
   * @param operation
   * @return
   */
  public LocalFeatures importLocalFeatures(LocalFeatures features, WSDLBindingOperation operation);

}
