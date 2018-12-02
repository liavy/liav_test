package com.sap.sl.util.components.xml.api;

import com.sap.sl.util.components.api.ComponentElementIF;

/**
 * @author d030435
 */

public interface ComponentElementXMLizerIF {
  public ComponentElementIF getComponentElement();
  public String getXML();
  /** @deprecated */
  public void fromXML(String xmlelement) throws ComponentElementXMLizerException;
}
  
