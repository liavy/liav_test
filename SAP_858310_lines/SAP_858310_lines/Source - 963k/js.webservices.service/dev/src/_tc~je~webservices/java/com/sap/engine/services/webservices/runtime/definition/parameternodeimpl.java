package com.sap.engine.services.webservices.runtime.definition;




/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ParameterNodeImpl implements com.sap.engine.interfaces.webservices.runtime.ParameterNode {

  private String parameterName = null;
  private String javaParameterName = null;
  private String javaClassName = null;
  private int parameterMode = com.sap.engine.interfaces.webservices.runtime.ParameterNode.IN;
  private boolean isOptional;
  private boolean isExposed;
  private String defaultValue;
  private String originalClassName;
  private boolean isHeader;
  private String headerElementNamespace;

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setJavaClassName(String javaClassName) {
    this.javaClassName = javaClassName;
  }

  public String getJavaClassName() {
    return javaClassName;
  }

  public String getJavaParameterName() {
    if (parameterMode == com.sap.engine.interfaces.webservices.runtime.ParameterNode.OUT && javaParameterName == null) return "returnValue";
    return javaParameterName;
  }

  public void setJavaParameterName(String javaParameterName) {
    this.javaParameterName = javaParameterName;
  }

  public void setOptional(boolean optional) {
    isOptional = optional;
  }

  public void setExposed(boolean exposed) {
    isExposed = exposed;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setParameterMode(String paramMode) {
    paramMode = paramMode.trim();
    if (paramMode.equalsIgnoreCase("IN")) parameterMode = IN;
    if (paramMode.equalsIgnoreCase("INOUT")) parameterMode = INOUT;
    if (paramMode.equalsIgnoreCase("OUT")) parameterMode = OUT;
  }

  public void setParameterMode(int parameterMode) {
    this.parameterMode = parameterMode;
  }

  public void setOriginalClassName(String originalClassName) {
    this.originalClassName = originalClassName;
  }

  public int getParameterMode() {
    return parameterMode;
  }

  public boolean isExposed() {
    return isExposed;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getOriginalClassName() {
    return originalClassName;
  }

  public void setHeader(boolean header) {
    isHeader = header;
  }

  public void setHeaderElementNamespace(String headerElementNamespace) {
    this.headerElementNamespace = headerElementNamespace;
  }

  public boolean isHeader() {
    return this.isHeader;
  }

  public String getHeaderElementNamespace() {
    return this.headerElementNamespace;
  }

  public String toString() {
    String result = "";
    result = "parameter name:" + parameterName + "\n";
    result += "java parameter name:" + javaParameterName + "\n";
    result += "java class name:" + javaClassName + "\n";
    return result;
  }

}