package com.sap.engine.services.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.component.BaseFactory;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;

import java.util.Enumeration;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class ComponentRegistry {

  private HashMapObjectObject components = null;

  protected ComponentRegistry() {

  }

  public boolean contains(String componentId) {
    if (components == null) {
      return false;
    }
    return (components.get(componentId) != null) ? true : false;
  }

  public BaseFactory getComponent(String componentId) throws RegistryException {
    if (!contains(componentId)) {
      throw new RegistryException(PatternKeys.NO_SUCH_WS_ELEMENT, new Object[]{componentId, this.getClass().getName()});
    }
    return (BaseFactory) components.get(componentId);
  }

  public String[] listComponentIds() {
    String[] componentIdsArr = new String[0];
    if (components != null) {
      componentIdsArr = new String[components.size()];
      Enumeration componentsEnum = components.keys();
      int i = 0;
      while (componentsEnum.hasMoreElements()) {
        componentIdsArr[i++] = (String) componentsEnum.nextElement();
      }
    }
    return componentIdsArr;
  }

  public BaseFactory[] listComponents() {
    if (components != null) {
      BaseFactory[] componentsArr = new BaseFactory[components.size()];
      Enumeration componentsEnum = components.elements();
      int i = 0;
      while (componentsEnum.hasMoreElements()) {
        componentsArr[i++] = (BaseFactory) componentsEnum.nextElement();
      }
      return componentsArr;
    } else return new BaseFactory[0];
  }

  public void registerComponent(String componentId, BaseFactory component) throws RegistryException{
    if (contains(componentId)) {
      throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{componentId, this.getClass().getName()});
    }
    if (components == null) {
      components = new HashMapObjectObject();
    }
    components.put(componentId, component);
  }

  public void unregisterComponent(String componentId) {
    if (components != null) {
      components.remove(componentId);
    }
  }

}

