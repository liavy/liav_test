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
public class CIMPropertyList extends ObjectWrapper {

  public CIMPropertyList(Object obj) {
    super(obj);
  }

  public CIMProperty get(String propertyName) throws Exception {
    Class[] classes = new Class[]{String.class};
    Object[] args = new Object[]{propertyName};

    Object obj = invokeMethod("get", classes, args);
    return new CIMProperty(obj);
  }

}
