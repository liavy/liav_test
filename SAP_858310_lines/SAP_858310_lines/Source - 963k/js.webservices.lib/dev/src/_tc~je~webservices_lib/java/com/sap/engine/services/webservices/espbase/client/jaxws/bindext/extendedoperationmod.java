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
package com.sap.engine.services.webservices.espbase.client.jaxws.bindext;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeInfo;

/**
 * 
 * @author Dimitar Velichkov  dimitar.velichkov@sap.com
 * Ceratin binding extensions can modify <wsdl:messages> (parameters) - this holds the mapping
 *
 */
public class ExtendedOperationMod extends ExtendedElement{
	
	public static final int INOUT = 1;
	public static final int FAULT = 2;
  public static final int IN = 3;
  public static final int OUT = 4;
	
  private String parameterName;
	private String newName;
	private QName childName;
	private String faultClass;
	private int    type;
	private String javaDoc = "";
	
		
	public ExtendedOperationMod(Parameter param, String newName, QName childName){	
		super(param, null, false);
		this.parameterName = param.getName();
		this.newName = newName;
		this.childName = childName; 
		this.type = INOUT;
	  if (param.getParamType() == param.IN) {
      this.type =  IN;
    }
    if (param.getParamType() == param.OUT) {
      this.type =  OUT;
    }    
	}

	public ExtendedOperationMod(Parameter param, String faultClass){		
		super(param, null, false);
		this.parameterName = param.getName();
		this.faultClass = faultClass;
		this.type = FAULT;				
	}
	
	/**
	 * @return Returns the newName.
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @return Returns the newType.
	 */
	public QName getChildName() {
		return childName;
	}
	
	/**
	 * @return Returns the type.
	 */
	public int getType(){ 
		return type;
	}
	
	/**
	 * 
	 * @return returns the fault class for the fault parameter
	 */
	public String getFaultClass(){
		return faultClass;
	}

	/**
	 * @return Returns the javaDoc.
	 */
	public String getJavaDoc() {
		return javaDoc;
	}
  
  /**
   * @return Returns the javaDoc.
   */
  public String getJavaType(SchemaToJavaConfig x) {
    return null;
  }
  

	/**
	 * @param javaDoc The javaDoc to set.
	 */
	public void setJavaDoc(String javaDoc) {
		this.javaDoc = javaDoc;
	}
  
  /**
   * Returns parameter name.
   * @return
   */
  public String getParameterName() {
    return this.parameterName; 
  }
	
	protected void checkParentExtensions() {
		
	}
	
  /*
  public String getJavaType(SchemaToJavaConfig conf) throws JaxWsMappingException{
    String javaType = null;
    QName schemaType = conf.getTypeSet().getElementType(newType);
    if(schemaType != null){
      javaType = conf.getTypeSet().getJavaType(schemaType);
    }else{
      SchemaTypeInfo types = conf.getTypeSet().get(newType);
      if(types == null){
        throw new JaxWsMappingException(JaxWsMappingException.CHILD_ATTR_MISSING_NOT_QNAME, newType.toString());
      }
      javaType = types.getJavaClass();
    }
    return javaType;
  }*/
  
}