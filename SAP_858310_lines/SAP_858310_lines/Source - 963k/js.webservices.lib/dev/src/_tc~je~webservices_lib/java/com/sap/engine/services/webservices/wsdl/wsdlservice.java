/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import java.util.ArrayList;

/**
 * Class foe service WSDL component.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLService extends WSDLNamedNode {

  private ArrayList ports;
  private WSDLExtension extension;

  /**
   * Default constructor.
   */
  public WSDLService() {
    super();
    ports = new ArrayList();
    extension = null;
  }

  /**
   * Constructor with parent.
   */
  public WSDLService(WSDLNode parent) {
    super(parent);
    ports = new ArrayList();
    extension = null;
  }

  /**
   * Adds por to service component.
   */
  public void addPort(WSDLPort port) throws WSDLException {
    for (int i = 0; i < ports.size(); i++) {
      if (((WSDLPort) ports.get(i)).getName().equals(port.getName())) {
        throw new WSDLException("Couldn't have ports with equals names");
      }
    } 

    ports.add(port);
  }

  /**
   * Returns ports.
   */
  public ArrayList getPorts() {
    return ports;
  }

  /**
   * Sets extension element of this service only first found is added
   */
  public void setExtension(WSDLExtension extension) {
    this.extension = extension;
  }

  /**
   * Returns extension element of service.
   */
  public WSDLExtension getExtension() {
    return extension;
  }

  /**
   * Returns port count.
   */
  public int getPortCount() {
    return ports.size();
  }

  /**
   * Return specific port.
   */
  public WSDLPort getPort(int portIndex) {
    return (WSDLPort) ports.get(portIndex);
  }

}

