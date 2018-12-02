package com.sap.engine.interfaces.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.TransportBinding;

/**
 * Title: ComponentFactory
 * Description: This interface provides methods for obtaining  protocol and transport binding instances.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface ComponentFactory {

  /** Gets a  protocol instance.
   *
   * @param  id The identifier, under which the  protocol instance has been registered.
   * @exception  ComponentInstantiationException Thrown in case a protocol instance with that identifier could not be instantiated.
   */

//  public Protocol getProtocolInstance(String id) throws ComponentInstantiationException ;

  /** Gets a  transport binding instance.
   *
   * @param  id The identifier, under which the transport binding instance has been registered.
   * @exception  ComponentInstantiationException Thrown in case a transport binding instance with that identifier could not be instantiated.
   */

  public TransportBinding getTransportBindingInstance(String id) throws ComponentInstantiationException;

  /** Gets all protocol identifiers, that have been registered.
   * @return String[]
   */

  public String[] listProtocolIds();

  /** Gets all transport binding identifiers, that have been registered.
   * @return String[]
   */

  public String[] listTransportBindingIds();

  /** Gets all protocol interfaces, that have been registered.
   * @return Protocol[]
   */

//  public Protocol[] listProtocolInterfaces();

  /** Gets all transport binding interfaces, that have been registered.
   * @return TransportBinding[]
   */

  public TransportBinding[] listTransportBindingInterfaces();

}
