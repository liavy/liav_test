package com.sap.engine.services.webservices.runtime.wsdl;

import javax.xml.namespace.QName;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class PortTypeDescriptorImpl implements com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor {

  static final String LITERAL_PORTTYPE_STYLE  =  "document";

  static final String ENCODED_PORTTYPE_STYLE  =  "rpc_enc";

  static final String RPC_LITERAL_PORTTYPE_STYLE  =  "rpc";

  static final String HTTP_PORTTYPE_STYLE  =  "http";

  private QName portTypeQName;
  private int portTypeType;

  public PortTypeDescriptorImpl(int portTypeType, QName portTypeQName) {
    this.portTypeType = portTypeType;
    this.portTypeQName = portTypeQName;
  }

  public PortTypeDescriptorImpl() {
  }

  public QName getQName() {
    return portTypeQName;
  }

  public int getType() {
    return portTypeType;
  }

  public static String getPortTypeType(int nom) {
    switch (nom) {
      case LITERAL_PORTTYPE: {
        return LITERAL_PORTTYPE_STYLE;
      }
      case ENCODED_PORTTYPE: {
        return ENCODED_PORTTYPE_STYLE;
      }
      case RPC_LITERAL_PORTTYPE: {
        return RPC_LITERAL_PORTTYPE_STYLE;
      }
      case HTTP_PORTTYPE: {
        return HTTP_PORTTYPE_STYLE;
      }
      default: {
        return null;
      }
    }
  }
}