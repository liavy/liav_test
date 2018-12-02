package com.sap.engine.services.webservices.server.lcr.api.cimclient;

import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;
import com.sap.engine.services.webservices.server.lcr.api.cim.*;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class CIMOMClient extends ObjectWrapper {

  public CIMOMClient(Object obj) {
    super(obj);
  }

  public CIMClass getCIMClass(CIMClassname className, boolean b, boolean b1, boolean b2, String[] strs) throws Exception {
    Class[] classes = new Class[]{className.getObjClass(), boolean.class, boolean.class, boolean.class, String[].class};
    Object[] args = new Object[]{className.getObjInstance() , new Boolean(b), new Boolean(b1), new Boolean(b2), strs};

    Object obj = invokeMethod("getCIMClass", classes,  args);
    return new CIMClass(obj);
  }

  public void createInstance(CIMInstance cimInstance) throws Exception {
    Class[] classes = new Class[]{cimInstance.getObjClass()};
    Object[] args = new Object[]{cimInstance.getObjInstance()};

    invokeMethod("createInstance", classes, args);
  }

  public CIMValueNamedInstanceList enumerateInstances(CIMClassname cimClassname, boolean b, boolean b1, boolean b2, boolean b3, String[] strs) throws Exception {
    Class[] classes = new Class[]{cimClassname.getObjClass(), boolean.class, boolean.class, boolean.class, boolean.class, String[].class};
    Object[] args = new Object[]{cimClassname.getObjInstance(), new Boolean(b), new Boolean(b1), new Boolean(b2), new Boolean(b3), strs};

    Object obj = invokeMethod("enumerateInstances", classes, args);
    return new CIMValueNamedInstanceList(obj);
  }

  public void disconnect() throws Exception {
    invokeMethod("disconnect", null, null);
  }

  public void modifyInstance(CIMValueNamedInstance cimValueNamedInstance) throws Exception {
    Class[] classes = new Class[]{cimValueNamedInstance.getObjClass()};
    Object[] args = new Object[]{cimValueNamedInstance.getObjInstance()};

    invokeMethod("modifyInstance", classes, args);
  }

}
