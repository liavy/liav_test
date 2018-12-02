package com.sap.engine.interfaces.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.TransportBinding;

/**
 * Title: TransportBinding
 * Description:This interface should be implemented by every provider of TransportBinding.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface TransportBindingFactory extends BaseFactory {

  /**
   * @return TransportBinding instance
   */

  TransportBinding newInstance();

}
