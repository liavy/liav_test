package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class DefinitionsHolder {

  private WSDLDefinitions definitions;
  private com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor[] portTypeDescriptors;

  public DefinitionsHolder(WSDLDefinitions definitions, com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor[] portTypeDescriptors) {
    this.definitions = definitions;
    this.portTypeDescriptors = portTypeDescriptors;
  }

  public DefinitionsHolder() {
  }

  public WSDLDefinitions getDefinitions() {
    return definitions;
  }

  public void setDefinitions(WSDLDefinitions definitions) {
    this.definitions = definitions;
  }

  public com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor[] getPortTypeDescriptors() {
    return portTypeDescriptors;
  }

  public void setPortTypeDescriptors(com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor[] portTypeDescriptors) {
    this.portTypeDescriptors = portTypeDescriptors;
  }


}