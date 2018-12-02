package com.sap.engine.interfaces.webservices.server.deploy.ws;

import com.sap.engine.interfaces.webservices.runtime.ImplLink;
import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;

/**
 * Title: SEIContext
 * Description: This context provides access to the base service endpoint configuration settings.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface SEIContext {

  /**
   * Method for access to the configuration name that represents the service endpoint in the ws-deploymemt-descriptor.xml.
   *
   * @return String - The configuration name.
   */

  public String getConfigurationName();

  /**
   * Method for access to the service endpoint transport address.
   *
   * @return String - The transport address.
   */

   public String getTransportAddress();

  /**
   * Method for access to the implementation link that this service endpoint is associated to.
   *
   * @return ImplLink - The implementation link.
   */

  public ImplLink getImplementationLink();

  /**
   * Method for access to the transport binding identifier that is associated to the service endpoint.
   *
   * @return ImplLink - The transport binding identifier.
   */

  public String getRuntimeTransportBinding();

  /**
   * Method for access to the protocol chain that is configured for the service endpoint.
   *
   * @return Feature[] - An array of feature objects that represent each protocol.
   */

  public Feature[] getProtocolChain();

  /**
   * Method for access to the operations of the service endpoint.
   *
   * @return OperationDefinition[] - An array of operation objects.
   */

  public OperationDefinition[] getOperations();

  /**
   * Method for access to the virutial interface (VI) file name that describes the service endpoint.
   *
   * @return String - The VI name.
   */

  public String getVIName(); /*?*/

 //context.getMetaData()?

}
