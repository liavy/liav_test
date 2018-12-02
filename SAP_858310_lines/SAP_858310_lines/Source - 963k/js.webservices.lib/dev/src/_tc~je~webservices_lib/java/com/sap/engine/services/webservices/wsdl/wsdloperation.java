/**
 * Title:        xml2000
 * Description:  Holds PortType operation Input/Output/And Fault Channels
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import java.util.ArrayList;

public class WSDLOperation extends WSDLNamedNode {

  private WSDLChannel input;
  private WSDLChannel output;
  private ArrayList faultList;
  private String parameterOrder;
  private ArrayList useFeatures;

  public WSDLOperation() {
    super();
    input = null;
    output = null;
    faultList = new ArrayList();
    useFeatures  = new ArrayList();
  }

  public WSDLOperation(WSDLNode parent) {
    super(parent);
    input = null;
    output = null;
    faultList = new ArrayList();
    useFeatures = new ArrayList();
  }
  
  public ArrayList getUseFeatures() {
    return this.useFeatures;
  }
  
  public void addUseFeature(SAPUseFeature useFeature) {
    this.useFeatures.add(useFeature);
  }
  
  public void setInput(WSDLChannel input) {
    this.input = input;
  }

  public void setOutput(WSDLChannel output) {
    this.output = output;
  }

  public void addFault(WSDLFault fault) {
    faultList.add(fault);
  }

  public WSDLChannel getInput() {
    return input;
  }

  public WSDLChannel getOutput() {
    return output;
  }

  public ArrayList getFaultList() {
    return faultList;
  }

  public String getParameterOrder() {
    return parameterOrder;
  }

  public void setParameterOrder(String s) {
    parameterOrder = s;
  }

}

