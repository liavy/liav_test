/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.ctx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 24, 2006
 */
public class MessageContextImpl implements MessageContext {
  
  private Map<String, IEntry> map = new HashMap<String, IEntry>(); //key property name, value Entry
  
  private Scope currentMode = Scope.HANDLER; //constant determing the current mode into which the context is: APPLICATION or HANDLER
  
  SOAPMessage msg; //this is the Message associated with the call. It is kept in this context, as an instance if it is associated with each call.
  
  public SOAPMessage getInternalSOAPMessage() {
    return msg;
  }
  
  public void setInternalSOAPMessage(SOAPMessage msg) {
    this.msg = msg;
  }
  
  public Scope getCurrentMode() {
    return currentMode;
  }

  public void setCurrentMode(Scope currentMode) {
    this.currentMode = currentMode;
  }

  public Scope getScope(String arg0) {
    IEntry e = map.get(arg0);
    if (e == null) {
      throw new IllegalArgumentException();
    }
    return e.scope;
  }

  public void setScope(String arg0, Scope arg1) {
    IEntry ie = map.get(arg0);
    if (ie == null) {
      throw new IllegalArgumentException();
    }
    if (currentMode == Scope.APPLICATION) {
      if (ie.scope == Scope.HANDLER) {
        throw new IllegalArgumentException("An attempt to change HANDLER property to APPLICATION.");
      }
    }
    map.get(arg0).scope = arg1;
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey(Object key) {
    Object o = get(key);
    if (o == null) {
      return false;
    } else {
      return true;
    }
  }

  public boolean containsValue(Object value) {
    Collection<IEntry> col = map.values();
    for (IEntry e : col) {
      if (e.obj.equals(value)) {
        return true;
      }
    }
    return false;
  }

  public Set<Entry<String, Object>> entrySet() {
    HashMap new_map = new HashMap<String, Object>();
    Set<Entry<String, IEntry>> s = map.entrySet();
    for (Entry<String, IEntry> entry : s) {
      if (Scope.APPLICATION == currentMode) {
        if (Scope.APPLICATION == entry.getValue().scope) { //only APPLICATION properties are returned.
          new_map.put(entry.getKey(), entry.getValue().obj);
        }
      } else {
        new_map.put(entry.getKey(), entry.getValue().obj);
      }
    }
    return new_map.entrySet();
  }

  public Object get(Object key) { //implemented according to JAXWS section 5.3.1
    IEntry e = map.get(key);
    if (e == null) {
      return null;
    }
    if (currentMode == Scope.APPLICATION) {
      if (e.scope == Scope.APPLICATION) {
        return e.obj;
      } else {
        return null;
      }
    } else {
      return e.obj;
    }
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<String> keySet() {
    Set newSet = new HashSet();
    Set<Entry<String, Object>> s = this.entrySet();
    Iterator<Entry<String, Object>> itr = s.iterator();
    while (itr.hasNext()) {
      newSet.add(itr.next().getKey());
      
    }
    return newSet;
  }
  
  public Object putWithScope(String key, Object value, Scope scope) {
    Object o = put(key, value);
    setScope(key, scope);
    return o;
  }
  
  public Object put(String key, Object value) {
    IEntry e = new IEntry(value, currentMode);
    if (currentMode == Scope.APPLICATION) {
      IEntry old = map.get(key);
      if (old != null && old.scope == Scope.HANDLER) {
        throw new IllegalArgumentException("An attempt to overwrite HANDLER property.");
      }
    }
    IEntry oldE = map.put(key, e);
    if (oldE != null) {
      return oldE.obj;
    } else {
      return null;
    }
  }

  public void putAll(Map<? extends String, ? extends Object> t) {
    Set toBePut = t.entrySet();
    Iterator<Entry<String, Object>> itr = toBePut.iterator();
    while (itr.hasNext()) {
      Entry<String, Object> e = itr.next();
      try {
        this.put(e.getKey(), e.getValue());
      } catch (IllegalArgumentException ex) {
        //$JL-EXC$
      }
    }
  }

  public Object remove(Object key) {
    IEntry e = map.get(key);
    if (e == null) {
      return null;
    }
    if (currentMode == Scope.APPLICATION) {
      if (e.scope == Scope.HANDLER) {
        throw new IllegalArgumentException("An attempt to remove HANDLER property.");
      }
    }
    e = map.remove(key);
    if (e == null) {
      return null;
    } else {
      return e.obj;
    }
  }

  public int size() {
    return map.size();
  }

  public Collection<Object> values() {
    ArrayList res = new ArrayList();
    Iterator<Entry<String, Object>> entries = this.entrySet().iterator();
    while (entries.hasNext()) {
      res.add(entries.next().getValue());
    }
    return res;
  }
  
  private class IEntry {
    Object obj;
    Scope scope;
    IEntry(Object o, Scope s) {
      obj = o;
      scope = s;
    }
  }
}
