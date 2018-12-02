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
public class CIMClass extends ObjectWrapper {

  public CIMClass(Object obj) {
    super(obj);
  }

  public CIMInstance createInstanceTemplate() throws Exception {
    Object obj = invokeMethod("createInstanceTemplate", null, null);
    return new CIMInstance(obj);
  }

}
