/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;

/**
 * This class provides implementation of CofigurationContext interface.
 * It allows instances of this class to be initialized in three modes: normal, persistent and static.
 * Depending of the mode, some methods are not applicable, or some parameters are invalid.
 * In static mode it is not allowed removement or additiong of properties or sub contexts.
 * In persi 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-20
 */
public class ConfigurationContextImpl implements ConfigurationContext,Serializable {
  
  public static final byte NORMAL_MODE  =  1;
  public static final byte STATIC_MODE  =  2;
  public static final byte PRERSISTENT_MODE  =  3;
    
  //final private String name;
  //final protected byte mode;
  private String name;
  protected byte mode;
  
  protected ConfigurationContext parent;//$JL-SER$
  protected Hashtable properties = new Hashtable();
  protected Hashtable  subContexts = new Hashtable();
  
  public ConfigurationContextImpl() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Creates context and attches it to the parent, in case the parent is not null.
   * If child context with same name already exists, it is replaced.
   * 
   * @param name
   * @param parent
   * @param mode
   */ 
  public ConfigurationContextImpl(String name, ConfigurationContext parent, byte mode) {
    if (name == null) {
      throw new IllegalArgumentException("ConfigurationContext name is null"); //TODO use ResourceBundle here.
    }
    if (mode < NORMAL_MODE || mode > PRERSISTENT_MODE) {
      throw new IllegalArgumentException("Invalid context mode: " + mode); //TODO use ResourceBundle here.
    }
    this.name = name; 
    this.parent = parent;
    this.mode = mode;
    if (parent != null) {
      ((ConfigurationContextImpl) parent).subContexts.put(name, this);
    }
  }
  
  public ConfigurationContextImpl(String name, ConfigurationContext parent) {
    this(name, parent, parent != null ? ((ConfigurationContextImpl) parent).mode : NORMAL_MODE);
  }
  
  
  public ConfigurationContextImpl(String name) {
    this(name, null);
  }
  
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#setProperty(java.lang.String, java.lang.Object)
	 */
	public Object setProperty(String name, Object value) {
    if (mode == STATIC_MODE) {
      throw new java.lang.UnsupportedOperationException("Method not supported in static mode.");
    }
    if (mode == PRERSISTENT_MODE) {
      if (! (value instanceof String)) {
         throw new IllegalArgumentException("Object '" + value + "' is not supported. Only java.lang.String objects are allowed.");
      }
    }
		return properties.put(name, value);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return properties.get(name);  
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#createSubContext(java.lang.String)
	 */
	public ConfigurationContext createSubContext(String name) {
    if (mode == STATIC_MODE) {
      throw new java.lang.UnsupportedOperationException("Method not supported in static mode.");
    }
    
    ConfigurationContext ctx = getSubContext(name); 
    if (ctx != null) {
      return ctx;
    }
    
    return new ConfigurationContextImpl(name, this);
	}
      
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getSubContext(java.lang.String)
	 */
	public ConfigurationContext getSubContext(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Sub-context name is null");
    }
    
    return (ConfigurationContext) subContexts.get(name);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getParent()
	 */
	public ConfigurationContext getParent() {
		return this.parent;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#removeSubContext(java.lang.String)
	 */
	public ConfigurationContext removeSubContext(String name) {
    if (mode == STATIC_MODE) {
      throw new java.lang.UnsupportedOperationException("Method not supported in static mode.");
    }
    if (name == null) {
      throw new IllegalArgumentException("Sub-context name is null");
    }
		ConfigurationContextImpl ctx = (ConfigurationContextImpl) subContexts.remove(name);
		if (ctx != null) {
      ctx.parent = null;
		}
    return ctx;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#properties()
	 */
	public Iterator properties() {
    if (mode == STATIC_MODE) {
      return Collections.unmodifiableSet(properties.keySet()).iterator();
    }
		return properties.keySet().iterator();
	}
 
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#subContexts()
	 */
	public Iterator subContexts() {
    if (mode == STATIC_MODE) {
      return Collections.unmodifiableSet(subContexts.keySet()).iterator();
    }
		return subContexts.keySet().iterator();
	}
  
  public Object removeProperty(String name) {
    if (mode == STATIC_MODE) {
      throw new UnsupportedOperationException("Method not supported in static mode.");
    }
    if (name == null) {
      throw new IllegalArgumentException("Property name is null");
    }
    return properties.remove(name);
  }

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getPath()
	 */
	public String[] getPath() {
    ArrayList list = new ArrayList();
    
    ConfigurationContextImpl tmp = this;
    while (tmp != null) {
      list.add(0, tmp.name);
      tmp = (ConfigurationContextImpl) tmp.parent;
    }
    
    return (String[]) list.toArray(new String[list.size()]);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#getSubContext(java.lang.String[])
	 */
	public ConfigurationContext getSubContext(String[] path) {
    if (path == null) {
      throw new IllegalArgumentException("Sub-context path is null");
    }
    ConfigurationContext ctx = this;
    for (int i = 0; i < path.length; i++) {
      ctx = ctx.getSubContext(path[i]);
      if (ctx == null) {
        return null;
      }
    }
    
		return ctx;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContext#removeSubContext(java.lang.String[])
	 */
	public ConfigurationContext removeSubContext(String[] path) {
    if (mode == STATIC_MODE) {
      throw new UnsupportedOperationException("Method not supported in static mode.");
    }
    if (path == null || path.length == 0) {
      throw new IllegalArgumentException("Sub-context path is null");
    }
    ConfigurationContext ctx = this;
    for (int i = 0; i < path.length - 1; i++) {
      ctx = ctx.getSubContext(path[i]);
      if (ctx == null) {
        return null;
      }
    }
    
    return ctx.removeSubContext(path[path.length - 1]);
	}

  public void clear() {
    this.properties.clear();
    Iterator itr = this.subContexts.values().iterator();
    ConfigurationContextImpl ctx;
    while (itr.hasNext()) {
      ctx = (ConfigurationContextImpl) itr.next();
      ctx.parent = null;
    }
    this.subContexts.clear();
  }

  public void clearPropertiesRecursive() {
    this.properties.clear();
    ConfigurationContextImpl ctx;
    Iterator itr = this.subContexts.values().iterator();
    while (itr.hasNext()) {
      ctx = (ConfigurationContextImpl) itr.next();
      ctx.clearPropertiesRecursive();
    }    
  }
  
  
  public String toStringInternal(int pos) {
    StringBuffer buf = new StringBuffer(64); // **
    String mode;
    switch (this.mode) {
     case PRERSISTENT_MODE: {
       mode = "persistent"; break;
     }
     case STATIC_MODE: {
       mode = "static"; break; 
     }
     case NORMAL_MODE:  {
       mode = "normal"; break;        
     }
     default: { //should not happend
       mode = "undefined";
     }
    }
    String space = "";
    for (int i = 0; i < pos; i++) {
      space += " ";   
    }
    buf.append(space + "context: name=" + this.name + ", mode=" + mode).append("\r\n");
    buf.append(space + "  properties:").append(properties.toString()).append("\r\n");
    
    Iterator en = subContexts.values().iterator();
    Object o;
    while (en.hasNext()) {
      o = en.next();
//      System.out.println("ConfiguraitonContextImpl.toStringInternal(): " + o.getClass());
      buf.append(((ConfigurationContextImpl) o).toStringInternal(pos + 2));
    }
    return buf.toString();    
  }
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
    return toStringInternal(0);
	}
	
}
