/*
 * Copyright (c) 2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdas;

/**
 * Operation configuration for a specific operation. 
 * It is used to set input parameters and obtain output parameters.
 * 
 * Copyright (c) 2007, SAP-AG
 * @author Dimitar Angelov
 * @author Mariela Todorova
 * @version 1.0, May 3, 2007
 */
public interface OperationConfig {
  
  /**
   * Sets value for the specified input parameter.
   * 
   * @param paramName parameter name
   * @param value input value 
   */
  public void setInputParamValue(String paramName, Object value);
  
  /**
   * Returns the value of the specified output parameter.  
   * 
   * @param paramName parameter name
   * @return output parameter value
   */
  public Object getOutputParamValue(String paramName);
  
  /**
   * Returns the value of an operation level design time property.
   * 
   * @param propertyName property name as specified in DesignProperties
   * @return property value
   */
  public String getProperty(String propertyName);
}
