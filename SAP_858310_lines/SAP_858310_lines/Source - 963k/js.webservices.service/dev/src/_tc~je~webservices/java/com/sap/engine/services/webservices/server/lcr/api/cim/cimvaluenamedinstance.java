package com.sap.engine.services.webservices.server.lcr.api.cim;

import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class CIMValueNamedInstance extends ObjectWrapper {

  public CIMValueNamedInstance(Object obj) {
    super(obj);
  }

  public CIMInstance getInstance() throws Exception {
    Object obj = invokeMethod("getInstance", null, null);
    return new CIMInstance(obj);
  }

}
