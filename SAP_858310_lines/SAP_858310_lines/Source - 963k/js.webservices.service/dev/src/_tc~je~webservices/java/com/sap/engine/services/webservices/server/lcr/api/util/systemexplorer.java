package com.sap.engine.services.webservices.server.lcr.api.util;

import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class SystemExplorer extends ObjectWrapper {

  public static final String SYSTEM_EXPLORER_CLASS_NAME = "com.sap.lcr.api.util.SystemExplorer";

  public SystemExplorer(Object obj) {
    super(obj);
  }

  public static String getJ2EEClusterEngineName() throws Exception {
    initClass(SystemExplorer.class.getClassLoader(), SYSTEM_EXPLORER_CLASS_NAME);

    Object obj = invokeStaticMethod("getJ2EEClusterEngineName", null, null);
    return (String)obj;
  }

}
