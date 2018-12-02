/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import javax.xml.namespace.QName;

/**
 *   Represents a description of server generated
 * WSDL PortType element.
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface PortTypeDescriptor {

  public static final int LITERAL_PORTTYPE  =  1;

  public static final int ENCODED_PORTTYPE  =  2;

  public static final int RPC_LITERAL_PORTTYPE  =  4;

  public static final int HTTP_PORTTYPE  =  8;

  /**
   * Returns the portType Qname;
   */
  public abstract QName getQName();

  /**
   * Returns the portType type.
   */
  public abstract int getType();

}