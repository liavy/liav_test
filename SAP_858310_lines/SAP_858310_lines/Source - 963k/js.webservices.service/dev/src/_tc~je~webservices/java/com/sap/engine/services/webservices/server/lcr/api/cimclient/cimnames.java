package com.sap.engine.services.webservices.server.lcr.api.cimclient;

import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;
import com.sap.engine.services.webservices.server.lcr.api.cim.CIMClassname;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class CIMNames extends ObjectWrapper{

  public static final String CIM_NAMES_CLASS_NAME = "com.sap.lcr.api.cimclient.CIMNames";


  public CIMNames(Object obj) {
    super(obj);
  }

  public static String getProperty(String propertyName) throws Exception {
    initClass(CIMNames.class.getClassLoader(), CIM_NAMES_CLASS_NAME);

    Object obj = getStaticField(propertyName);
    return (String)obj;
  }

  public static CIMClassname getCimClassNameValue(String propertyName) throws Exception {
    initClass(CIMNames.class.getClassLoader(), CIM_NAMES_CLASS_NAME);

    Object obj = getStaticField(propertyName);
    return new CIMClassname(obj);
  }

}
