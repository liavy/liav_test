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
public class CIMInstance extends ObjectWrapper {

  public CIMInstance(Object objInstance) {
    super(objInstance);
  }

  public CIMInstancename buildInstanceName(CIMClass cimClassInst) throws Exception {
    Class[] argClasses = new Class[]{cimClassInst.getObjClass()};
    Object[] args = new Object[]{cimClassInst.getObjInstance()};

    Object obj = invokeMethod("buildInstanceName", argClasses, args);
    return new CIMInstancename(obj);
  }

  public CIMClassname getCIMClassname() throws Exception {
    Object obj = invokeMethod("getCIMClassname", null, null);
    return new CIMClassname(obj);
  }

  public String getPropertyValue(String propertyName) throws Exception {
    Class[] classes = new Class[]{String.class};
    Object[] args = new Object[]{propertyName};

    Object obj = invokeMethod("getPropertyValue", classes, args);
    return (String)obj;
  }

  public CIMPropertyList getProperties() throws Exception {
    Object obj = invokeMethod("getProperties", null, null);
    return new CIMPropertyList(obj);
  }

}
