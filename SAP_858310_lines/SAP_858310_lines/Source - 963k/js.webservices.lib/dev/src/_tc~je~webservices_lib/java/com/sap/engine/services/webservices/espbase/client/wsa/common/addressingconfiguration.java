/*
 * Copyright (c) 2006 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.wsa.common;

import java.util.Locale;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.configuration.*;

/**
 * @author Vladimir Videlov
 * @version 7.10
 */
public class AddressingConfiguration {
  public static final String VERSION_ID = "Id: //engine/j2ee.core.libs/dev/src/tc~je~webservices_lib/_tc~je~webservices_lib/java/com/sap/engine/services/webservices/espbase/client/wsa/common/AddressingConfiguration.java_1 ";
  //private static final Trace TRACE = new Trace(VERSION_ID);

  // Configuration helper operations
  /**
   * Checks if WS-Addressing protocol is configurated as enabled
   * @param clientCtx Client configuration context
   * @return true if the protocol is enabled, false otherwise
   */
  public static boolean isEnabled(ClientConfigurationContext clientCtx) {
    //final String SIGNATURE = "isEnabled(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    String enabled = getConfigValue(clientCtx, AddressingConstants.CONF_WSA_ENABLED);

    //TRACE.exiting(SIGNATURE);
    return (enabled != null && enabled.toLowerCase(Locale.ENGLISH).equals(AddressingConstants.CONST_TRUE));
  }

  /**
   * Returns property value from RT WS client config
   * @param clientCtx Client configuration context
   * @param propName Property name
   * @return property value from RT WS client config
   */
  public static String getConfigValue(ClientConfigurationContext clientCtx, String propName) {
    //final String SIGNATURE = "getConfigValue(ClientConfigurationContext, String)";
    //TRACE.entering(SIGNATURE);

    String resultValue = null;

    BindingData bData = clientCtx.getStaticContext().getRTConfig();
    InterfaceData iData = clientCtx.getStaticContext().getDTConfig();

    String operationName = clientCtx.getOperationName();

    if (bData != null) {
      resultValue = getConfigValue(bData, operationName, propName, AddressingConstants.NS_WSA_FEATURE);
    }

    if (resultValue == null && iData != null) {
      resultValue = getConfigValue(iData, operationName, propName, AddressingConstants.NS_WSA_FEATURE);
    }

    if (resultValue == null) {
	    Object prop = clientCtx.getPersistableContext().getProperty("{" + AddressingConstants.NS_WSA_FEATURE + "}" + AddressingConstants.CONF_WSA_ENABLED);	

	    if (prop != null) {
	    	resultValue = (String) prop;
	    }	    	
    }
	    
    //TRACE.exiting(SIGNATURE);
    return resultValue;
  }

  /**
   * Returns property value from RT WS client config for specific operation.
   * @param bData Binding data object
   * @param operationName Operation name
   * @param propName Property name
   * @return property value from RT WS client config for specific operation
   */
  public static String getConfigValue(BindingData bData, String operationName, String propName) {
    //final String SIGNATURE = "getStaticConfigValue(BindingData, String, String)";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return getConfigValue(bData, operationName, propName, AddressingConstants.NS_WSA_FEATURE);
  }

  /**
   * Returns property value from RT WS client config for specific operation.
   * @param iData Interface data object
   * @param operationName Operation name
   * @param propName Property name
   * @return property value from RT WS client config for specific operation
   */
  public static String getConfigValue(InterfaceData iData, String operationName, String propName) {
    //final String SIGNATURE = "getStaticConfigValue(InterfaceData, String, String)";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return getConfigValue(iData, operationName, propName, AddressingConstants.NS_WSA_FEATURE);
  }

  /**
   * Returns property value from RT WS client config for specific operation.
   * @param bData Binding data object
   * @param operationName Operation name
   * @param propName Property name
   * @param ns Namespace
   * @return property value from RT WS client config for specific operation
   */
  public static String getConfigValue(BindingData bData, String operationName, String propName, String ns) {
    //final String SIGNATURE = "getStaticConfigValue(BindingData, String, String, String)";
    //TRACE.entering(SIGNATURE);

    String result;
    PropertyListType[] propLists = null;

    if (ns == null || ns.equals("")) {
      ns = AddressingConstants.NS_WSA_FEATURE;
    }

    if (operationName != null) {
      //TRACE.debugT(SIGNATURE, "Check for operation specific property list. Operation name: " + operationName);
      OperationData[] oData = bData.getOperation();

      for (int i = 0; i < oData.length; i++) {
        if (oData[i].getName().equalsIgnoreCase(operationName)) {
          //TRACE.debugT(SIGNATURE, "Found operation specific property list.");
          propLists = oData[i].getPropertyList();
          break;
        }
      }
    }

    if (propLists == null || (result = findProperty(propLists, propName, ns)) == null) {
      //TRACE.debugT(SIGNATURE, "Using global binding data property list.");
      result = findProperty(bData.getPropertyList(), propName, ns);
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  /**
   * Returns property value from RT WS client config for specific operation.
   * @param iData Interface data object
   * @param operationName Operation name
   * @param propName Property name
   * @param ns Namespace
   * @return property value from RT WS client config for specific operation
   */
  public static String getConfigValue(InterfaceData iData, String operationName, String propName, String ns) {
    //final String SIGNATURE = "getStaticConfigValue(InterfaceData, String, String, String)";
    //TRACE.entering(SIGNATURE);

    String result;
    PropertyListType[] propLists = null;

    if (ns == null || ns.equals("")) {
      ns = AddressingConstants.NS_WSA_FEATURE;
    }

    if (operationName != null) {
      //TRACE.debugT(SIGNATURE, "Check for operation specific property list. Operation name: " + operationName);
      OperationData[] oData = iData.getOperation();

      for (int i = 0; i < oData.length; i++) {
        if (oData[i].getName().equalsIgnoreCase(operationName)) {
          //TRACE.debugT(SIGNATURE, "Found operation specific property list.");
          propLists = oData[i].getPropertyList();
          break;
        }
      }
    }

    if (propLists == null || (result = findProperty(propLists, propName, ns)) == null) {
      //TRACE.debugT(SIGNATURE, "Using global interface data property list.");
      result = findProperty(iData.getPropertyList(), propName, ns);
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  /**
   * Gets default WSA request action, if it's missing in the WS client config generates default one from the WSDL
   * @param clientCtx Configuration context
   * @param isFaultMsg Defines whether the action is for fault message or not
   * @return default WSA action
   */
  public static String getDefaultRequestAction(ClientConfigurationContext clientCtx, boolean isFaultMsg) {
    //final String SIGNATURE = "getDefaultRequestAction(ConfigurationContext, boolean)";
    //TRACE.entering(SIGNATURE);

    String result = null;

    if (isFaultMsg) {
      String protocolNS = getConfigValue(clientCtx, AddressingConstants.CONF_WSA_PROTOCOL);

      if (AddressingConstants.NS_WSA_200408.equals(protocolNS)) {
        result = AddressingConstants.ACTION_FAULT_200408;
      } else if (AddressingConstants.NS_WSA_200508.equals(protocolNS)) {
        result = AddressingConstants.ACTION_FAULT_200508;
      }
    } else if ((result = getConfigValue(clientCtx, AddressingConstants.CONF_WSA_INPUT_ACTION)) == null) {
      //generation default action
      InterfaceData id = clientCtx.getStaticContext().getDTConfig();
      String ptNS = id.getNamespace();
      String ptName = id.getName();
      String wsdlOpName = clientCtx.getOperationName();

      if (ptNS.charAt(ptNS.length() - 1) == '/') {
        result = ptNS + ptName + "/" + wsdlOpName + "Request";
      } else {
        result = ptNS + "/" + ptName + "/" + wsdlOpName + "Request";
      }
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  /**
   * Finds property value in the property lists.
   * @param propLists Property lists
   * @param propName Property name
   * @param ns Namespace
   * @return property value in the property lists
   */
  public static String findProperty(PropertyListType[] propLists, String propName, String ns) {
    //final String SIGNATURE = "findProperty(PropertyListType[], String, String, String)";
    //TRACE.entering(SIGNATURE);

    String result = null;

    //TRACE.debugT(SIGNATURE, "Looking for property name: " + propName);

    for (int i = 0; i < propLists.length; i++) {
      //TRACE.debugT(SIGNATURE, "Checking properties for NS: " + AddressingConstants.NS_WSA_FEATURE);
      PropertyType[] props = propLists[i].getPropertiesByNS(AddressingConstants.NS_WSA_FEATURE);//propLists[i].getProperty()

      for (int j = 0; j < props.length; j++) {
        PropertyType prop = props[j];

        //TRACE.debugT(SIGNATURE, "Current property name: " + props[j].getName());
        //TRACE.debugT(SIGNATURE, "Current property value: " + props[j].get_value());

        if (prop.getName().equals(propName) && prop.getNamespace().equals(ns)) {
          //TRACE.exiting(SIGNATURE);

          result = prop.get_value();
          //TRACE.debugT(SIGNATURE, "Property [name]: " + propName + " / [value]: " + result);

          return result;
        }
      }
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }
}
