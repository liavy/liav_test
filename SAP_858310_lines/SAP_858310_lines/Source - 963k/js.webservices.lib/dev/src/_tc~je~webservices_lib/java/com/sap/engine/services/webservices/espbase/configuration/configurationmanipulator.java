/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration;

import java.util.List;

import com.sap.engine.services.webservices.espbase.wsdl.Definitions;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-11-28
 */
public interface ConfigurationManipulator {
  /**
   * Constant determing provider configuration processing.
   */
  public static final int PROVIDER_MODE = IConfigurationMarshaller.PROVIDER_MODE;
  /**
   * Constant determing consumer configuration processing.
   */
  public static final int CONSUMER_MODE = IConfigurationMarshaller.CONSUMER_MODE;
  /**
   * Updates the <code>bd</code>, as well its nested OperationDatas, by using default DT-RT mapping for
   * the design-time properties described in <code>v</code>.
   * 
   * @param bd Binding object that is to be updated
   * @param v Variant object that contains desing-time configuration
   * @deprecated use the overloaded method with three parameters instead.
   */
  public void updateBindingDataWithDefaults(BindingData bd, Variant v) throws ConfigurationMarshallerException;
  /**
   * Updates the <code>bd</code>, as well its nested OperationDatas, by using default DT-RT mapping for
   * the design-time properties described in <code>v</code>.
   * 
   * @param bd Binding object that is to be updated
   * @param v Variant object that contains desing-time configuration
   * @param mode
   */
  public void updateBindingDataWithDefaults(BindingData bd, Variant v, int mode) throws ConfigurationMarshallerException;
  /**
   * Checks whether the <code>rt</code> configuration matches the requirements and/or restrictions posed by <code>dt</code> configuration, 
   * i.e. whether <code>rt</code> is a valid RT configuration derived from <code>dt</code>. 
   * The implementation checks the OperationDatas configs as well.
   *  
   * @param rt The BindingData object to be checked for correctness against <code>dt</code>.
   * @param dt The InterfaceData behaviour object against which the <code>rt</code> BindingData will be checked.
   * @param msgs List object into which String objects should be added. The string object could contain some warning and(or) error statements in regards to the check.
   *                This data from the list could be visualized in UIs. 
   * @return true if the <code>rt</code> is valid against <code>dt</code> configuation, false otherwise.
   * 
   * @exception ConfigurationMarshallerException in case anything goes wrong.
   */
  public boolean checkRTConfigurationAgainstDT(BindingData rt, InterfaceData dt, List msg, int mode) throws ConfigurationMarshallerException;
  /**
   * Extracts configuration data from <code>def</code> and builds configuration tree.
   * @param def wsdl definition
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value.
   * @return configuration tree
   */
  public ConfigurationRoot create(Definitions def, int mode) throws Exception;

}
