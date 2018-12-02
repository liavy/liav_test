package com.sap.ejb.ql.sqlmapper.common;

/**
 * Provides description of a bean field as part of a path 
 * expression (i.e. along with some information about its 
 * predecessor in the path). Used for communication between
 * <code>Processor</code> and <code>Manager</code> classes.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 **/
public class BeanField
{
  private String  parentType;
  private String  parentName;
  private String  type;
  private String  fieldName;
  private boolean isRelation;
  private boolean isDependentValue;
  private boolean isBoolean;

  /**
   * Creates a <code>BeanField</code> instance.
   * @param type
   *     bean field type.
   * @param fieldName
   *     bean field name.
   * @param isRelation
   *     <code>true</code> if bean field is cmr field;<br>
   *     <code>false</code> elsewise.
   * @param isDependentValue
   *     <code>true</code> if bean field is dependent value field;<br>
   *     <code>false</code> elsewise.
   * @param isBoolean
   *     <code>true</code> if bean field is of boolean type;<br>
   *     <code>false</code> elsewise.
   * @param parentType
   *     field type of bean field's predecessor in path.
   * @param parentName
   *     name of bean field's predecessor in path.
   **/
  BeanField(String type, String fieldName, boolean isRelation,
                    boolean isDependentValue, boolean isBoolean,
                    String parentType, String parentName)
  {
    this.parentType      = parentType;
    this.parentName      = parentName;
    this.type            = type;
    this.fieldName       = fieldName;
    this.isRelation      = isRelation;
    this.isDependentValue = isDependentValue;
    this.isBoolean       = isBoolean;
  }

  /**
   * Retrieves field type of bean field's predecessor in path.
   * @return
   *    field type of bean field's predecessor in path.
   **/
  String getParentType()
  {
    return this.parentType;
  }

  /**
   * Retrieves name of bean field's predecessor in path.
   * @return
   *    name of bean field's predecessor in path.
   **/ 
  String getParentName()
  {
    return this.parentName;
  }

  /**
   * Retrieves bean field type.
   * @return
   *    bean field type.
   **/
  String getType()
  {
    return this.type;
  }

  /**
   * Retrieves bean field name.
   * @return
   *    bean field name.
   **/
  String getFieldName()
  {
    return this.fieldName;
  }

  /**
   * Indicates whether bean field is a cmr field.
   * @return
   *     <code>true</code> if bean field is cmr field;<br>
   *     <code>false</code> elsewise.
   **/
  boolean isRelation()
  {
    return this.isRelation;
  }

  /**
   * Indicates whether bean field is a dependent value field.
   * @return
   *     <code>true</code> if bean field is dependent value field;<br>
   *     <code>false</code> elsewise.
   **/
  boolean isDependentValue()
  {
    return this.isDependentValue;
  }  

  /**
   * Indicates whether bean field is of boolean type.
   * @return
   *     <code>true</code> if bean field is of boolean type;<br>
   *     <code>false</code> elsewise.
   **/
  boolean isBoolean()
  {
    return this.isBoolean;
  }

  /**
   * Creates string representation of <code>beanField</code> instance.
   * @return
   *     string representation of <code>beanField</code> instance.
   **/
  public String toString()
  {
    StringBuffer strBuf = new StringBuffer("{ fieldName = ");
    strBuf.append(this.fieldName);
    strBuf.append(", type = ");
    strBuf.append(this.type);
    if ( this.isRelation )
    {
      strBuf.append(", relation");
    }
    if ( this.isBoolean )
    {
      strBuf.append(", boolean");
    }
    if ( this.isDependentValue )
    {
      strBuf.append(", dependent value");
    }
    strBuf.append(", parentName = ");
    strBuf.append(this.parentName);
    strBuf.append(", parentType = ");
    strBuf.append(this.parentType);
    strBuf.append(", hashcode = ");
    strBuf.append(this.hashCode());
    strBuf.append(" }");
    return  strBuf.toString();
  }
    
}
