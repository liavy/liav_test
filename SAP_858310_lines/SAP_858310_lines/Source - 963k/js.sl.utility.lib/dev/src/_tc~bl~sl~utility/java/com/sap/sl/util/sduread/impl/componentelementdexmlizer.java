package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.components.xml.api.ComponentElementXMLizerFactoryIF;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerException;
import com.sap.sl.util.components.xml.api.ComponentElementXMLizerIF;

/**
 * @author d030435
 */

class ComponentElementDeXMLizer {
  ComponentElementDeXMLizer() {
  }
  /**
   * convert the given <code>xmlString</code> to an instance of
   * com.sap.sl.util.components.api.ComponentElementIF.
   * @param xmlString: the String to be converted
   * @param requestorComment: a comment about the type of requestor
   *   that can be used for error text and exceptions
   */
  static ComponentElementIF getComponentFromXMLString(String xmlString, String requestorComment) throws IllFormattedSduManifestException {
    ComponentElementIF componentelement;
    ComponentElementXMLizerIF cex= null;
    cex = ComponentElementXMLizerFactoryIF.getInstance().createComponentXMLizerElement(null);
    try {
      cex.fromXML(xmlString);
    }
    catch (ComponentElementXMLizerException e) {
      throw new IllFormattedSduManifestException("The  "+requestorComment+" contains an illegally formated componentelement in ("+xmlString+"): ("+e.getMessage()+").",e);
    }
    componentelement=cex.getComponentElement();
    return componentelement;
  }
}
