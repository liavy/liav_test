package com.sap.engine.services.webservices.runtime.component;

import java.util.Enumeration;

import com.sap.engine.frame.ServiceContext;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.component.BaseRegistryException;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.espbase.server.ImplementationContainerAccessor;
import com.sap.engine.services.webservices.espbase.server.runtime.BuiltInWSEndpointImplContainer;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.JavaClassImplementationContainer;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public final class ImplementationContainerManager implements ImplementationContainerAccessor {

  private HashMapObjectObject table;
  //keep a reference ot this object because if the startTimer methods
  //private EJBImplementationContainer ejbImplContainer;
  private JavaClassImplementationContainer javaClassImplContainer;

  public ImplementationContainerManager(ServiceContext serviceContext) {
    this();
    //registring ejb containers
//    ejbImplContainer = new EJBImplementationContainer(serviceContext);
//    table.put(ejbImplContainer.getImplementationID(), ejbImplContainer);
    //registering javaclass impl container
    javaClassImplContainer = new JavaClassImplementationContainer(serviceContext);
    table.put(javaClassImplContainer.getImplementationID(), javaClassImplContainer);
    table.put(BuiltInWSEndpointImplContainer.IMPLEMENTATION_CONTAINER_ID, BuiltInWSEndpointImplContainer.SINGLETON);
  }

  public ImplementationContainerManager() {
    table = new HashMapObjectObject();
  }

  public synchronized void register(ImplementationContainer implContainer) throws BaseRegistryException {
    Object old;
    if ((old = table.put(implContainer.getImplementationID(), implContainer)) != null) {
      table.put(((ImplementationContainer) old).getImplementationID(), old);
      throw new BaseRegistryException("There is already registred implementation container with ID '" + implContainer.getImplementationID() + "'");
    }
  }

  public synchronized void unregister(String  implContainerId) {
    table.remove(implContainerId);
  }

  public ImplementationContainer getImplementationContainer(String implContainerID) {
    ImplementationContainer result = (ImplementationContainer) table.get(implContainerID);
    return result;
  }

  public final void stopApplication(String applicationName) {
    com.sap.engine.interfaces.webservices.runtime.EventObject stopAppEvent = new com.sap.engine.interfaces.webservices.runtime.EventObject(com.sap.engine.interfaces.webservices.runtime.EventObjectIDs.STOP_APPLICATION, applicationName);
    ImplementationContainer implContainer;

    synchronized (this) {
      Enumeration en = table.elements();
      while (en.hasMoreElements()) {
        implContainer = (ImplementationContainer) en.nextElement();
        try {
          implContainer.notify(stopAppEvent, null);
        } catch (com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException rtpE) {
          Location.getLocation(WSLogging.RUNTIME_LOCATION).catching(rtpE);
        }
      }
    }
  }
}

