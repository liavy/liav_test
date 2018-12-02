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
 * PortType representation.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 2.0
 */
public class WSDLPortType extends WSDLNamedNode {

  private ArrayList operations;
  private ArrayList useFeatures;

  /**
   * Default constructor.
   */
  public WSDLPortType() {
    super();
    operations = new ArrayList();
    useFeatures = new ArrayList();
  }

  /**
   * Constructor with parent.
   */
  public WSDLPortType(WSDLNode parent) {
    super(parent);
    operations = new ArrayList();
    useFeatures = new ArrayList();
  }
  
  /**
   * Returns ArrayList of use features in this PortType.
   */ 
  public ArrayList getUseFeatures() {
    return this.useFeatures;
  }
  
  public void addUseFeature(SAPUseFeature useFeature) {
    this.useFeatures.add(useFeature);
  }
  
  /**
   * Adds portType Operation.
   */
  public void addOperation(WSDLOperation operation) {
    operations.add(operation);
  }

  /**
   * Returns operations.
   */
  public ArrayList getOperations() {
    return operations;
  }

  /**
   * Returns operation count.
   */
  public int getOperationCount() {
    return operations.size();
  }

  /**
   * Returns indexed operation.
   */
  public WSDLOperation getOperation(int index) {
    return (WSDLOperation) operations.get(index);
  }

}

