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
public class CIMValueNamedInstanceList extends ObjectWrapper {

  public CIMValueNamedInstanceList(Object obj) {
    super(obj);
  }

  public int size() throws Exception {
    Object obj = invokeMethod("size", null, null);
    return ((Integer)obj).intValue();
  }

  public CIMValueNamedInstance get(int i) throws Exception {
    Class[] classes = new Class[]{int.class};
    Object[] args = new Object[]{new Integer(i)};

    Object obj = invokeMethod("get", classes, args);
    return new CIMValueNamedInstance(obj);
  }

}
