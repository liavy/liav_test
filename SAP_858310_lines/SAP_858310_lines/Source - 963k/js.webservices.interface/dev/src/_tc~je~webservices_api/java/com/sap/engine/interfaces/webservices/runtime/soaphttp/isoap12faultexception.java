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
package com.sap.engine.interfaces.webservices.runtime.soaphttp;

import java.util.List;

/**
 * This interface exposes methods via which the web service endpoints or protocols could
 * customize the way a java exception is serialized into SOAP1.2 fault.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-21
 */
public interface ISoap12FaultException extends ISoapFaultException {
  /**
   * Returns fault subcode value. In case the returned value is QName, the prefix will be mapped to the
   * namespace returned by the _getFaultCodeNS(), otherwise default
   * prefix will be used. In case this method returns null, than
   * defult faultcode will be used.
   *
   * @return subcode localname value. If null is returned no subcode will be added.
   */  
  public String _getSubCode();
  /**
   * @return subcome namespace value
   */
  public String _getSubCodeNS();
  /**
   * Returns list of abbreviations of languages for which there is a translation of the fault reason. The
   * language abbreviations are as parameters for <code>_getReason(String lang)</code> method, which
   * returns the actual translated string. 
   * The method will be used in conjunction with <code>_getFaultString()</code> and <code>_getFaultStringLang()</code> -
   * that means that first the latter methods are called and the values returned by them are added into the fault reason's list
   * and after that this method is called and the values returned by it are passed to the <code>_getReason(String lang)</code> method.
   * Also the returned values are used as values of xml:lang attribute.
   * @return List containing <code>java.lang.String</code> objects which represent the different languages
   *         into which the reason has been defined. Null or empty list is returned when no additional language trnslations are available.   
   */
  public List _getAdditionalReasonsLanguages();
  /**
   * Returns translated reason string into the language specified by <code>lang</code>.
   * @param lang language abbreviation (e.g. 'en', 'de', ...)
   * @return the reason string translated into the specified language.
   *  
   */  
  public String _getReason(String lang);
}
