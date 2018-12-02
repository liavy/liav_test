package com.sap.engine.services.webservices.runtime.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Title: ExtHashtable
 * Description: Extends Hashtable, adding a new method for registering objects
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 1.0
 */
public class ExtHashtable extends Hashtable {

  public ExtHashtable() {
    super();
  }

  public ExtHashtable(int initialCapacity) {
    super(initialCapacity);
  }

  public ExtHashtable(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public ExtHashtable(Map t) {
    super(t);
  }

  /**
   * @param  Object key, Object value
   *
   * @return void - The method imitates the put(Object key, Object value) method of class Hashtable,
   *                but when there are more than one objects, corresponding to the same key,
   *                all such objects are added to an ArrayList object
   *                and the latter becomes the object corresponding to the key ...
   */
  public void register(Object key, Object value) {
    if (containsKey(key)) {
      Object obj = get(key);

      if (obj instanceof ArrayList) {
        if (value instanceof Collection) {
          ((ArrayList) obj).addAll((Collection) value);
        } else {
          ((ArrayList) obj).add(value);
        }
      } else {
        ArrayList list = new ArrayList();
        list.add(obj);
        if (value instanceof Collection) {
          list.addAll((Collection) value);
        } else {
          list.add(value);
        }
        put(key, list);
      }
    } else {
      put(key, value);
    }
  }

  public void replace(Object key, Object value) {
    put(key, value);
  }

}

