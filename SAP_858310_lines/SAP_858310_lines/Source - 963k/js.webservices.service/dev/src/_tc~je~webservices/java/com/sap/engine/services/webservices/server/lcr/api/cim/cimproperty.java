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

public class CIMProperty extends ObjectWrapper {

  public CIMProperty(Object obj) {
    super(obj);
  }

  public void setValue(String value) throws Exception {
    Class[] classes = new Class[]{String.class};
    Object[] args = new Object[]{value};

    invokeMethod("setValue", classes, args);
  }

  public String getValue() throws Exception {
    Object obj = invokeMethod("getValue", null, null);
    return (String)obj;
  }

}
