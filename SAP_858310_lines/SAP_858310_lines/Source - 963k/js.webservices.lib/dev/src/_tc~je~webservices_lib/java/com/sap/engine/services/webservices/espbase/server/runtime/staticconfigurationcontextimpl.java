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
package com.sap.engine.services.webservices.espbase.server.runtime;

import javax.xml.bind.JAXBContext;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.ConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-20
 */ 
public class StaticConfigurationContextImpl extends ConfigurationContextImpl implements StaticConfigurationContext {
  /**
   * These constans are used as property keys.
   */  
  static final String APPLICATION_NAME = "application_name";
  static final String LOCATION = "location";
  static final String WEBSERVICE_NAME = "webservice_name";
  static final String TYPEMAPPING_REGISTRY = "typemapping_registry";
  static final String JAXBCONTEXT = "jaxbcontext";
  static final String PROTOCOL_ORDER = "protocol_order";
  static final String VARIANT = "variant";
  static final String BINDING_DATA = "binding_data";
  static final String INTERFACE_MAPPING = "interface_mapping";
  static final String ENDPOINT_REQUEST_URI = "endpoint-request-uri";
  
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	StaticConfigurationContextImpl(String name, ConfigurationContext parent) {
		super(name, parent, ConfigurationContextImpl.STATIC_MODE);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext#getProtocolsOrder()
	 */
	public String[] getProtocolsOrder() {
		return (String[]) getProperty(PROTOCOL_ORDER);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext#getLogLocation()
	 */
	public Location getLogLocation() {
		return (Location) getProperty(LOCATION);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext#getWebServiceName()
	 */
	public String getWebServiceName() {
		return (String) getProperty(WEBSERVICE_NAME);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext#getTargetApplicationName()
	 */
	public String getTargetApplicationName() {
		return (String) getProperty(APPLICATION_NAME);
	}
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext#getTypeMappingRegistry()
	 */
	public TypeMappingRegistry getTypeMappingRegistry() {    
		return (TypeMappingRegistry) getProperty(TYPEMAPPING_REGISTRY);
	}
	public Variant getDTConfiguration() {
		return (Variant) getProperty(VARIANT);
	}

	public InterfaceMapping getInterfaceMapping() {
		return (InterfaceMapping) getProperty(INTERFACE_MAPPING);
	}

	public BindingData getRTConfiguration() {
		return (BindingData) getProperty(BINDING_DATA);
	}
  
  public String getEndpointRequestURI() {
    return (String) getProperty(ENDPOINT_REQUEST_URI);
  }
  
	public JAXBContext getJAXBContext() {
    return (JAXBContext) getProperty(JAXBCONTEXT);
  }

  public Object setPropertyInternal(String name, Object value) {
		return super.properties.put(name, value); 
	}
	
}
