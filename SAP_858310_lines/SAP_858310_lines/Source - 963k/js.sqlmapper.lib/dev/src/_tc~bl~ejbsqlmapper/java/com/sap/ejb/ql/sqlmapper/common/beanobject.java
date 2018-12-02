package com.sap.ejb.ql.sqlmapper.common;

/**
 * Provides description of an identification variable.
 * Used for communication between
 * <code>Processor</code> and <code>Manager</code> classes.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 **/
public class BeanObject
{
  private String type;
  private String identifier;

  /**
   * Creates a <code>BeanObject</code> instance.
   * @param type
   *     abstract bean type of identification variable.
   * @param identifier
   *     name of identification variable.
   **/
  BeanObject(String type, String identifier)
  {
    this.type = type;
    this.identifier = identifier;
  }

  /**
   * Retrieves abstract bean type of identification variable.
   * @return
   *     abstract bean type of identification variable.
   **/
  String getType()
  {
    return this.type;
  }

  /**
   * Retrieves name of identification variable.
   * @return
   *     name of identification variable.
   **/
  String getIdentifier()
  {
    return this.identifier;
  }

  /**
   * Creates a string representation of <code>BeanObject</code>
   * instance.
   * @return
   *     string representation of <code>BeanObject</code> instance.
   **/
  public String toString()
  {
    StringBuffer strBuf = new StringBuffer("{ identifier = ");
    strBuf.append(this.identifier);
    strBuf.append(", type = ");
    strBuf.append(this.type);
    strBuf.append(", hascode = ");
    strBuf.append(this.hashCode()); 
    strBuf.append(" }");
    return strBuf.toString();
  }

}
