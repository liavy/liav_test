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
public class CIMFactory extends ObjectWrapper {

  public static final String CIM_FACTORY_CLASS_NAME = "com.sap.lcr.api.cim.CIMFactory";

  public CIMFactory(Object obj) {
    super(obj);
  }

  public static CIMInstance instance(CIMClassname cimClassname, CIMInstancename leftInstance, String str, CIMInstancename rightInstance, String str1) throws Exception {
    initClass(CIMFactory.class.getClassLoader(), CIM_FACTORY_CLASS_NAME);

    Class[] classes = new Class[]{cimClassname.getObjClass(), leftInstance.getObjClass(), String.class, rightInstance.getObjClass(), String.class};
    Object[] args = new Object[]{cimClassname.getObjInstance(), leftInstance.getObjInstance(), str, rightInstance.getObjInstance(), str1};

    Object obj = invokeStaticMethod("instance", classes, args);
    return new CIMInstance(obj);
  }

}
