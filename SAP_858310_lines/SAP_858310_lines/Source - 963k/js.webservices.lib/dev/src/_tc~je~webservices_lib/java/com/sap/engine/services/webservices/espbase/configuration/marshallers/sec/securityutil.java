/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration.marshallers.sec;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.Variant;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Nov 28, 2006
 */
public class SecurityUtil {
  public final static String AF_NS = "http://www.sap.com/webas/630/soap/features/authentication/";
  public final static String TRANSPORT_GUARANTEE_NS = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
  
  public final static String AUTHENTICATION_METHOD = "AuthenticationMethod";
  public final static String AUTHENTICATION_LEVEL = "AuthenticationLevel";
  public final static String TLS_LEVEL = "Level";
  public final static String TLS_TLSType = "TLSType";
  
  public static void addMissingSecurityProperties(ConfigurationRoot cfgRoot) {
    InterfaceDefinition intfDefs[] = cfgRoot.getDTConfig().getInterfaceDefinition();
    for (InterfaceDefinition definition : intfDefs) {
      Variant[] vs = definition.getVariant();
      if (vs.length == 1) {
        Variant v = vs[0];
        PropertyListType pList = v.getInterfaceData().getSinglePropertyList();
        PropertyType tmp = pList.getProperty(AF_NS, AUTHENTICATION_LEVEL);
        if (tmp == null) {
          pList.addProperty(AF_NS, AUTHENTICATION_LEVEL, "None");
        }
        tmp = pList.getProperty(TRANSPORT_GUARANTEE_NS, TLS_LEVEL);
        if (tmp == null) {
          pList.addProperty(TRANSPORT_GUARANTEE_NS, TLS_LEVEL, "None");
        }
      }
    }
    
    Service ss[] = cfgRoot.getRTConfig().getService();
    for (Service service : ss) {
      BindingData[] bds = service.getServiceData().getBindingData();
      for (BindingData data : bds) {
        PropertyListType pList = data.getSinglePropertyList();
        PropertyType tmp = pList.getProperty(AF_NS, AUTHENTICATION_METHOD);
        if (tmp == null) {
          pList.addProperty(AF_NS, AUTHENTICATION_METHOD, "sapsp:None");
        }
        tmp = pList.getProperty(TRANSPORT_GUARANTEE_NS, TLS_TLSType);
        if (tmp == null) {
          pList.addProperty(TRANSPORT_GUARANTEE_NS, TLS_TLSType, "sapsp:HTTP");
        }
      }
    }
  }
}
