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
import java.lang.reflect.Type;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public abstract class BeanProperty {

  protected String customName = null;
  
  protected int argCounter;
  protected String typeName;
  protected Class<?> erasedClass;
  
  public BeanProperty(int argCounter, Type rawType, ClassLoader applicationClassLoader){
    
    
    JaxWsTypeEraser eraser = new JaxWsTypeEraser(rawType, applicationClassLoader);
    eraser.eraseType();
    typeName = eraser.getErasedTypeAsString();
    erasedClass = eraser.getErasedClass();
    this.argCounter = argCounter;
       
  }
  
  public abstract void generateProperty(BufferedWriter bw) throws JaxWsInsideOutException, IOException;
  
  /**
   * Return the string representation of the bean property 
   * @return the bean property as string
   * @throws JaxWsInsideOutException
   */
  protected void generateProperty(BufferedWriter wr,boolean inlineAnnotations, boolean addAnotations) throws JaxWsInsideOutException, IOException{
    
    if (inlineAnnotations == false &&  addAnotations) {
      generateAnnotations(wr);
    }
    StringBuffer propGetterSetter = new StringBuffer(customName);
    String fieldName = customName;
    if(customName.equals("_return")){
      propGetterSetter.deleteCharAt(0);
      fieldName = "Return";
    }        
    propGetterSetter.setCharAt(0, Character.toUpperCase(propGetterSetter.charAt(0)));
    
    wr.append("\t\tprivate " + typeName + " " + fieldName + ";");
    wr.newLine();
    //  Getter
    if (inlineAnnotations == true  && addAnotations) {
      generateAnnotations(wr);
    }
    wr.append("\t\tpublic " + typeName + " get" + propGetterSetter + "(){");
    wr.newLine();
    wr.append("\t\t\treturn " + fieldName + ";");
    wr.newLine();
    wr.append("\t\t}");
    wr.newLine();
    
    //Setter
    wr.append("\t\tpublic void set" + propGetterSetter + "(" + typeName + " " + fieldName + "){");
    wr.newLine();
    wr.append("\t\t\tthis." + fieldName + " = " + fieldName + ";");
    wr.newLine();
    wr.append("\t\t}");
    
    wr.newLine();
        
  }
  
  public void generateAnnotations(BufferedWriter wr) throws JaxWsInsideOutException, IOException  {
    // do nothing 
  }
  


  /**
   * @return Returns the erasedClass.
   */
  public Class<?> getErasedClass() {
    return erasedClass;
  }
  
}
