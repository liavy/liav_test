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
package com.sap.engine.services.webservices.jaxws.j2w;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 * 
 * Generates a FaultBean property from a method name
 */
public class FaultBeanProperty extends BeanProperty {

  public static final Set<String> EXCLUDED_METHODS = new HashSet<String>();
  static {
    EXCLUDED_METHODS.add("getCause");
    EXCLUDED_METHODS.add("getLocalizedMessage");
    EXCLUDED_METHODS.add("getStackTrace");
    EXCLUDED_METHODS.add("getClass");
  }
     
  /**
   * 
   */
  public FaultBeanProperty(Method m, String targetNamespace, ClassLoader applicationClassloader) {
        
    super(0, m.getReturnType(), applicationClassloader);
    
    // "getBla" becomes "bla"
    StringBuffer nameBuf = new StringBuffer(m.getName().substring(m.getName().indexOf("get") + "get".length()));
    nameBuf.setCharAt(0, Character.toLowerCase(nameBuf.charAt(0)));
    
    customName = nameBuf.toString();
            
  }

  /* (non-Javadoc)
   * @see jaxws.BeanProperty#generateProperty()
   */
  public void generateProperty(BufferedWriter wr) throws JaxWsInsideOutException, IOException {
    
    wr.write("\t\t@" + XmlElement.class.getName() + "(name = \"" + customName + "\", namespace = \"\")");
    wr.newLine();
    
    super.generateProperty(wr,false,false);
             
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return customName;
  }

}
