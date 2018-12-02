/**
 * Title:        xml2000
 * Description:  This is class for all WSDL Item that have name attribute
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NamespaceContainer;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class WSDLBinding extends WSDLNamedNode {

  private QName type;
  private ArrayList extensions;
  private ArrayList operations;
  private ArrayList useFeatures;

  public WSDLBinding() {
    super();
    type = null;
    extensions = new ArrayList();
    operations = new ArrayList();
    useFeatures = new ArrayList();
  }

  public WSDLBinding(WSDLNode parent) {
    super(parent);
    type = null;
    extensions = new ArrayList();
    operations = new ArrayList();
    useFeatures = new ArrayList();
  }

  public ArrayList getUseFeatures() {
    return this.useFeatures;
  }
  
  public void setType(QName type) {
    this.type = type;
  }
  
  public QName getType() {
    return type;
  }

  public void addExtension(WSDLExtension extension) {
    extensions.add(extension);
  }

  public ArrayList getExtensions() {
    return extensions;
  }

  public void addOperation(WSDLBindingOperation operation) {
    operations.add(operation);
  }

  public void addUseFeature(SAPUseFeature useFeature) {    
    useFeatures.add(useFeature);
  }
  
  public ArrayList getOperations() {
    return operations;
  }
  
  public WSDLBindingOperation getOperation(String operationName) {
    for (int i=0; i<operations.size(); i++) {
      WSDLBindingOperation boperation = (WSDLBindingOperation) operations.get(i);
      if (boperation.getName().equals(operationName)) {
        return boperation;
      }
    }
    return null;
  }

  public void loadAttributes(SimpleAttr[] attr, int attrCount, NamespaceContainer uriContainer) throws Exception {
    super.loadAttributes(attr, attrCount);
    String type = SimpleAttr.getAttribute("type", attr, attrCount);

    if (type == null) {
      throw new WSDLException(" Type attribute must present !");
    } else {
      this.type = QName.qnameWSDLCreate(type, uriContainer, ((WSDLDefinitions) getDocument()).targetNamespace);
    }
  }

  public void loadAttributes(Element element) throws WSDLException {
    super.loadAttributes(element);
    Attr typeAttr = element.getAttributeNode("type");

    if (typeAttr == null) {
      throw new WSDLException(" Binding must have type attribute set !");
    } else {
      this.type = super.getQName(typeAttr.getValue(), element);
    }
  }

}

