/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime.soaphttp;

/**
 *   Userdefined exceptions implement this interface in order to
 * specify the content of the Fault chiled element(faultcode, faultstring, faultactor),
 * when this exception is thrown.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface ISoapFaultException {

  /**
   * Returns the value which will be set in the <faultcode> element.
   * In case the value is QName, the prefix will be mapped to the
   * namespace returned by the _getFaultCodeNS(), otherwise default
   * prefix will be used. In case this method returns null, than
   * defult faultcode will be used.
   */
  public String _getFaultCode();

  /**
   * Returns namespace string denoting the namespace to which
   * the fault code returned by _getFaultCode() method belongs.
   */
  public String _getFaultCodeNS();

  /**
   * Returns string value which will be used as value of the
   * <faultstring> element. In case the value is null,
   * than getLocalizedMessage() of java.lang.Throwable returned value is used.
   */
  public String _getFaultString();

  /**
   * Returns string value which will be use as value of the
   * xml:lang attribute on <faultstring> element.
   * In case value is null the xml:lang attribute won't be set.
   */
  public String _getFaultStringLang();

  /**
   * Returns string value which will be use as value of the
   * <soapactor> element. In case null is returned, this element
   * will no be present.
   */
  public String _getFaultActor();

}
