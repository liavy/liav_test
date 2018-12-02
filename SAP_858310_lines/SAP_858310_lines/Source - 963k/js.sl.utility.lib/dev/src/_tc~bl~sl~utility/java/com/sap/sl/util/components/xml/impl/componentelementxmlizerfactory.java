package com.sap.sl.util.components.xml.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerException;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerFactoryIF;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerIF;

/**
 * @author d030435
 */

public final class ComponentElementXMLizerFactory extends ComponentElementXMLizerFactoryIF {
  public ComponentElementXMLizerFactory() {
  }
  public ComponentElementXMLizerIF createComponentXMLizerElement(ComponentElementIF component) {
    return new ComponentElementXMLizer(component);        
  }
  public ComponentElementXMLizerIF createComponentXMLizerElementFromXML(String xmlelement) throws ComponentElementXMLizerException {
    return new ComponentElementXMLizer(xmlelement);
  }
}
