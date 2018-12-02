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

import java.lang.reflect.Type;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class ParamWrapper {

  public static enum PARAM_TYPE {IN, INOUT, OUT, RETURN};
  
  private WebParam  wp;
  private WebResult wr;
  private XmlJavaTypeAdapter xmlTypeAdapter;
  private Type      parameterType;
  private Class<?>  originalType;
  private PARAM_TYPE pType;
  private int        paramIndex;
  private boolean isListType = false;
  boolean hackCTS50Test = false; //this denotes hack for com/sun/ts/tests/jaxws/ee/w2j/document/literal/wrapperstyle/marshalltest/Client.java#MarshallSimpleTypesTest 
  
  public ParamWrapper(Type p, WebResult wr) {

    this.parameterType = p;
    this.wr = wr;
    this.wp = null;
  }

  public ParamWrapper(Type p, WebParam wp) {

    this.parameterType = p;
    this.wp = wp;
    this.wr = null;
    this.pType = PARAM_TYPE.IN;
    
  }
  
  public boolean isListType() {
    return this.isListType;
  }
   
  public void setListType(boolean arg) {
    this.isListType = arg;
  }

  /**
   * @return Returns the xmlTypeAdapter.
   */
  public XmlJavaTypeAdapter getXmlTypeAdapter() {
    return xmlTypeAdapter;
  }

  /**
   * @param xmlTypeAdapter The xmlTypeAdapter to set.
   */
  public void setXmlTypeAdapter(XmlJavaTypeAdapter xmlTypeAdapter) {
    this.xmlTypeAdapter = xmlTypeAdapter;
  }

  /**
   * @return Returns the parameterType.
   */
  public Type getRawType() {
    return parameterType;
  }

  /**
   * @return Returns the pType.
   */
  public PARAM_TYPE getParamType() {
    return pType;
  }
  
  /**
   * @param type The pType to set.
   */
  public void setParamType(PARAM_TYPE type) {
    pType = type;
  }
   

  /**
   * @return Returns the wp.
   */
  public WebParam getWebparam() {
    return wp;
  }

  /**
   * @return Returns the wr.
   */
  public WebResult getWebresult() {
    return wr;
  }

  public Class<?> getOriginalType() {
    return originalType;
  }

  public void setOriginalType(Class<?> originalType) {
    this.originalType = originalType;
  }

  /**
   * @return Returns the paramIndex.
   */
  public int getParamIndex() {
    return paramIndex;
  }

  /**
   * @param paramIndex The paramIndex to set.
   */
  public void setParamIndex(int paramIndex) {
    this.paramIndex = paramIndex;
  }

  

}
