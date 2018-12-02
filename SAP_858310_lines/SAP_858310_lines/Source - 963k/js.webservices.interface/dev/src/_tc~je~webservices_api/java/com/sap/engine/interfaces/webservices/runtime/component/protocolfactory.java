package com.sap.engine.interfaces.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;

/**
 * Title: ProtocolFactory
 * Description: This interface should be implemented by every provider of Protocol.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface ProtocolFactory extends BaseFactory {

  /**
   * @return Protocol instance
   */

   //Protocol newInstance();
   ProviderProtocol newStatelessInstance();
}
