package com.sap.engine.interfaces.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;

/**
 * Title: ClientTransportBindingFactory
 * Description: This interface should be implemented by every provider of ClientFeatureProvider.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface ClientTransportBindingFactory extends BaseFactory {

  /**
   * @return ClientFeatureProvider instance
   */

  ClientFeatureProvider newInstance();

}
