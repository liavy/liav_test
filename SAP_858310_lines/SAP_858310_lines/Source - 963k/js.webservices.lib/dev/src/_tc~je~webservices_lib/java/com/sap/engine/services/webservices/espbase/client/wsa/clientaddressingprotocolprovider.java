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
package com.sap.engine.services.webservices.espbase.client.wsa;

import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConfiguration;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConstants;
import com.sap.engine.services.webservices.espbase.client.wsa.common.Trace;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vladimir Videlov
 * @version 7.10
 */
public class ClientAddressingProtocolProvider {
  public static final String VERSION_ID = "Id: //engine/j2ee.core.libs/dev/src/tc~je~webservices_lib/_tc~je~webservices_lib/java/com/sap/engine/services/webservices/espbase/client/wsa/ClientAddressingProtocolProvider.java_1 ";
  private static final Trace TRACE = new Trace(VERSION_ID);

  /**
   * WSA 2004/08 ConsumerProtocol impl
   */
  private ClientAddressingProtocol200408 wsa200408;

  /**
   * WSA 2005/08 ConsumerProtocol impl
   */
  private ClientAddressingProtocol200508 wsa200508;

  /**
   * Singleton object instance
   */
  private static ClientAddressingProtocolProvider SINGLETON = new ClientAddressingProtocolProvider();

  /**
   * XML Marshaller for WSA 2004/08
   */
  public static XMLMarshaller MARSHALLER_200408 = new XMLMarshaller();

  /**
   * XML Marshaller for WSA 2005/08
   */
  public static XMLMarshaller MARSHALLER_200508 = new XMLMarshaller();

  static {
    final String SIGNATURE = "init()";
    //TRACE.entering(SIGNATURE);

    InputStream typeSystem = null;
    ClassLoader cl = ClientAddressingProtocolProvider.class.getClassLoader();

    try {
      // 2004/08
      String typePath = "com/sap/engine/services/webservices/espbase/client/wsa/generated/ns200408/frm/types.xml";
      typeSystem = cl.getResourceAsStream(typePath);

      if (typeSystem == null) {
      	//TRACE.exiting(SIGNATURE);
        TRACE.errorT(SIGNATURE, "Unable to open type mapping registry for WS-Addressing (2004/08).");
      } else {
        MARSHALLER_200408.init(typeSystem, cl);
        typeSystem.close();
      }

      // 2005/08
      typePath = "com/sap/engine/services/webservices/espbase/client/wsa/generated/ns200508/frm/types.xml";
      typeSystem = cl.getResourceAsStream(typePath);

      if (typeSystem == null) {
      	//TRACE.exiting(SIGNATURE);
        TRACE.errorT(SIGNATURE, "Unable to open type mapping registry for WS-Addressing (2005/08).");
      } else {
        MARSHALLER_200508.init(typeSystem, cl);
        typeSystem.close();
      }
    } catch(IOException ex){
      TRACE.catching(SIGNATURE, ex);
    } catch(TypeMappingException ex){
      TRACE.catching(SIGNATURE, ex);
    } finally {
      if (typeSystem != null) {
        try {
          typeSystem.close();
        } catch (IOException ex) {
          TRACE.catching(SIGNATURE, ex);
        }
      }
    }

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * Returns singleton instance
   * @return Client Addressing Protocol Provider object
   */
  public static ClientAddressingProtocolProvider getInstance() {
    return SINGLETON;
  }

  private ClientAddressingProtocolProvider() {
    wsa200408 = new ClientAddressingProtocol200408();
    wsa200508 = new ClientAddressingProtocol200508();
  }

  /**
   * Returns configuration specified version of WS-Addressing protocol
   * @param clientCtx Client configuration context
   * @return Consumer Protocol impl
   */
  public ConsumerProtocol getVersionedProtocol(ClientConfigurationContext clientCtx) {
  	//final String SIGNATURE = "getVersionedProtocol(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConsumerProtocol result = wsa200408;
    String protocolNS = AddressingConfiguration.getConfigValue(clientCtx, AddressingConstants.CONF_WSA_PROTOCOL);

    if (AddressingConstants.NS_WSA_200508.equals(protocolNS)) {
      result = wsa200508;
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }
}
