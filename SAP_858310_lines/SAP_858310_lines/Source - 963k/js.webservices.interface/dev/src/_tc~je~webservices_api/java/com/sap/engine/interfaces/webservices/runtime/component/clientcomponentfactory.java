package com.sap.engine.interfaces.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;

/**
 * Title: ClientComponentFactory
 * Description: This interface provides methods for obtaining client protocol and transport binding instances.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface ClientComponentFactory {

  /** Gets a client protocol instance.
   *
   * @param  id The identifier, under which the client protocol instance has been registered.
   * @exception  ComponentInstantiationException Thrown in case a client protocol instance with that identifier  could not be instantiated.
   */

  public ClientFeatureProvider getClientProtocolInstance(String id) throws ComponentInstantiationException;

  /** Gets a client transport binding instance.
   *
   * @param  id The identifier, under which the client transport binding instance has been registered.
   * @exception ComponentInstantiationException Thrown in case a client transport binding instance with that identifier could not be instantiated.
   */

  public ClientFeatureProvider getClientTransportBindingInstance(String id) throws ComponentInstantiationException;

  /** Gets all client protocol interfaces, that have been registered.
   * @return ClientFeatureProvider[]
   */

  public String[] listClientProtocolIds();

  /** Gets all client transport binding identifiers, that have been registered.
   * @return String[]
   */

  public String[] listClientTransportBindingIds();

  /** Gets all client protocol identifiers, that have been registered.
   * @return String[]
   */

  public ClientFeatureProvider[] listClientProtocolInterfaces();

  /** Gets all client transport binding interfaces, that have been registered.
   * @return ClientFeatureProvider[]
   */

  public ClientFeatureProvider[] listClientransportBindingInterfaces();

}
