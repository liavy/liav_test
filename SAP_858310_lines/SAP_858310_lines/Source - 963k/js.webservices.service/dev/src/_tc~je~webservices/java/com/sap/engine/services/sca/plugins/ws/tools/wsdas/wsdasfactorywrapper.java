package com.sap.engine.services.sca.plugins.ws.tools.wsdas;

import com.sap.engine.interfaces.sca.config.binding.WSConfigurationObject;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;
import com.sap.engine.services.webservices.espbase.wsdas.WSDASFactory;
import commonj.sdo.helper.HelperContext;

public class WsdasFactoryWrapper {

  public enum WsdasFactoryMode { CacheEnabled, CacheDisabled };
  
  private WSDASFactory iFactory = null;
  private static WsdasFactoryWrapper iInstance = null;
  
  private WsdasFactoryWrapper(WsdasFactoryMode aMode) {    
    switch (aMode) {
    case CacheEnabled: iFactory = WSDASFactory.newInstanceWithCache(); break;
    case CacheDisabled: iFactory = WSDASFactory.newInstance(); break;
    }
  }
  
  public static WsdasFactoryWrapper getInstance(WsdasFactoryMode aMode) {
    if (iInstance == null)
      iInstance = new WsdasFactoryWrapper(aMode);
    
    return iInstance;
  }
  
  public WSDAS createWsdas(WSConfigurationObject aDestination, HelperContext aContext) throws Exception {
    return iFactory.createWSDAS(aDestination.getApplicationName(), aDestination.getServiceReferenceId(), aContext);
  }
}
