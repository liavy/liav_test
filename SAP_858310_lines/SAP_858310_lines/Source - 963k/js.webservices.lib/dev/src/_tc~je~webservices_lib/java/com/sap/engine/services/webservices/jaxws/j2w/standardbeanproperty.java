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
import java.lang.annotation.Annotation;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sap.engine.services.webservices.jaxws.j2w.ParamWrapper.PARAM_TYPE;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class StandardBeanProperty extends BeanProperty{
            
  private String targetNamespace;
  private ParamWrapper parameterWrapper;
  private boolean isSimpleValue = false;
  
  
  /**
   * 
   */
  public StandardBeanProperty(ParamWrapper pw, ClassLoader applicationClassLoader) {           
    super(pw.getParamIndex(), pw.getRawType(), applicationClassLoader); 
    this.parameterWrapper = pw;
    PARAM_TYPE mode = pw.getParamType();
        
    Annotation annot = null;
    
    switch(mode){
    case IN:      
    case INOUT:
    case OUT:
      annot = pw.getWebparam();
      customName = "arg" + argCounter;
      break;
    case RETURN:
      customName = "return";
      annot = pw.getWebresult();
    }
    
    this.targetNamespace = "";
    
    if(annot != null){
      if(annot.annotationType().equals(WebParam.class)){
        if(((WebParam)annot).name().length() > 0){
          customName = ((WebParam)annot).name();
        }
        //don't take partName for document-literal into consideration!
//        if(((WebParam)annot).partName().length() > 0){
//          customName = ((WebParam)annot).partName();
//        }
        
        if(((WebParam)annot).targetNamespace().length() > 0){
          this.targetNamespace = ((WebParam)annot).targetNamespace();
        }
      } else if(annot.annotationType().equals(WebResult.class)){
        if(((WebResult)annot).name().length() > 0){
          customName = ((WebResult)annot).name();
        }
//        don't take partName for document-literal into consideration!
//        if(((WebResult)annot).partName().length() > 0){
//          customName = ((WebResult)annot).partName();
//        }
        if(((WebResult)annot).targetNamespace().length() > 0){
          this.targetNamespace = ((WebResult)annot).targetNamespace();
        }
        
      } else {
        throw new IllegalArgumentException();
      }
    }         
   
  }

  public void setSimpleValue(boolean isSimple) {
    this.isSimpleValue = isSimple;
    this.customName = "value";
  }  
  
  /**
   * Generate bean property, such as "private int prop". The property is
   * type-erased.
   * 
   * @return
   * @throws JaxWsInsideOutException 
   */
  public void generateProperty(BufferedWriter wr) throws JaxWsInsideOutException, IOException {
    if (isSimpleValue == true) {
      super.generateProperty(wr,true,true);
    } else {
      super.generateProperty(wr,false,true);
    }
    
                   
  }
  
  public void generateAnnotations(BufferedWriter wr) throws JaxWsInsideOutException, IOException  {
    if (this.isSimpleValue) {
      wr.append("\t\t@"+XmlValue.class.getName());
      wr.newLine();
    }
    if (parameterWrapper.isListType()) {
      wr.append("\t\t@"+XmlList.class.getName());
      wr.newLine();
    }
    if (parameterWrapper.getXmlTypeAdapter() != null) {
      wr.append("\t\t@"+XmlJavaTypeAdapter.class.getName()+"("+parameterWrapper.getXmlTypeAdapter().value().getName()+".class)");
      wr.newLine();
    }          
    if (this.isSimpleValue == false) {
      if (erasedClass.isPrimitive()) {
        wr.append("\t\t@" + XmlElement.class.getName() + "(name = \"" + customName + "\", namespace = \"" + targetNamespace + "\", required = true)");
        wr.newLine();
      } else {
        if (parameterWrapper.hackCTS50Test) {
          wr.append("\t\t@" + XmlElement.class.getName() + "(name = \"" + customName + "\", namespace = \"" + targetNamespace + "\", required = true, nillable = true)");
          wr.newLine();
        } else {
          wr.append("\t\t@" + XmlElement.class.getName() + "(name = \"" + customName + "\", namespace = \"" + targetNamespace + "\")");
          wr.newLine();
        }
      }
    }       
    //"return" is a keyword, make the actual property "_return";
    if(customName.equals("return")){
      customName = "_return";      
    }    
  }  

}
