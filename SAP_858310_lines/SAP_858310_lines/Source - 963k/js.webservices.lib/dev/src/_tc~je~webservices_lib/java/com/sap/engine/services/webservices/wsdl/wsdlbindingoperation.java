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

import java.util.ArrayList;

public class WSDLBindingOperation extends WSDLNamedNode {

  private ArrayList extensions;
  private WSDLBindingChannel input;
  private WSDLBindingChannel output;
  private ArrayList faultList;
  private ArrayList useFeatures;

  public WSDLBindingOperation() {
    super();
    extensions = new ArrayList();
    faultList = new ArrayList();
    input = null;
    output = null;
    useFeatures = new ArrayList();    
  }

  public WSDLBindingOperation(WSDLNode parent) {
    super(parent);
    extensions = new ArrayList();
    faultList = new ArrayList();
    useFeatures = new ArrayList();
    input = null;
    output = null;
  }

  public ArrayList getUseFeatures() {
    return this.useFeatures;   
  }
  
  public void addUseFeatire( SAPUseFeature useFeature) {
    this.useFeatures.add(useFeature);
  }
  
  public void addExtension(WSDLExtension extension) {
    extensions.add(extension);
  }

  public void addFault(WSDLBindingFault fault) {
    faultList.add(fault);
  }

  public void setInput(WSDLBindingChannel input) {
    this.input = input;
  }

  public void setOutput(WSDLBindingChannel output) {
    this.output = output;
  }

  public ArrayList getExtensions() {
    return extensions;
  }

  public ArrayList getFaults() {
    return faultList;
  }

  public WSDLBindingChannel getInput() {
    return input;
  }

  public WSDLBindingChannel getOutput() {
    return output;
  }

}

