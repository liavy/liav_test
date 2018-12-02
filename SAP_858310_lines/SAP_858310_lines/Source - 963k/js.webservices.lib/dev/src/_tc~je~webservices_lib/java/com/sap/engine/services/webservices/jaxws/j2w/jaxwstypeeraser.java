/*
 * Copyright (c) 2004 by SAP AG, Walldorf., http://www.sap.com All rights
 * reserved. This software is the confidential and proprietary information of
 * SAP AG, Walldorf. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you
 * entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.Holder;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class JaxWsTypeEraser {

  private ClassLoader cl;

  private boolean isArray = false;

  private boolean isCollection = false;

  private Type type;

  private Class<Collection> collType = null;

  private Class<?> arrayClass = null;

  private Class<?> erasedClass = null;

  private int arrDim = 0; // in this case this is not an array

  private static HashMap<Class<?>, String> primArrayToString = new HashMap<Class<?>, String>();
  static {
    primArrayToString.put(Boolean.TYPE, "[Z");
    primArrayToString.put(Character.TYPE, "[C");
    primArrayToString.put(Byte.TYPE, "[B");
    primArrayToString.put(Short.TYPE, "[S");
    primArrayToString.put(Integer.TYPE, "[I");
    primArrayToString.put(Long.TYPE, "[J");
    primArrayToString.put(Float.TYPE, "[F");
    primArrayToString.put(Double.TYPE, "[D");
  }

  public JaxWsTypeEraser(Type type, ClassLoader cl) {

    if (type == null || cl == null) {
      throw new IllegalArgumentException("Type is [" + type + "], ClassLoader is [" + cl + "]");
    }

    this.type = type;
    this.cl = cl;
  }

  @Deprecated
  public JaxWsTypeEraser(Type type) {
    this(type, Thread.currentThread().getContextClassLoader());
  }

  public Class<?> getArrayType() {
    Class<?> c = null;

    // if (arrayClass.isPrimitive()) {
    // c = cl.loadClass(primArrayToString.get(arrayClass));
    // } else {
    if (arrDim > 0) {
      c = java.lang.reflect.Array.newInstance(arrayClass, new int[arrDim]).getClass();
    }

    // c = cl.loadClass("[L" + arrayClass.getName() + ";");
    // }

    return c;
  }

  /**
   * Determine if the type being erased is an array type.
   * 
   * @return
   */
  public boolean isArray() {

    return isArray;

  }

  /**
   * if the erased type is an array, this returns the name of the schema type
   * that will be produces by JAXB. E.g., Object[] -> "anyTypeArray", String[] ->
   * "stringArray", etc
   * 
   * @return the name as string
   */
  public String getArraySchemaName() {
    return null;
  }

  /**
   * Determine if the type being erased is a collection type (special rules
   * apply, see JAX-WS spec)
   * 
   * @return
   */
  public boolean isCollection() {

    return isCollection;
  }

  /**
   * Get the specific collection type of the type being erased. This is because
   * Collection types themeselves are NOT erased, rather, their components are.
   * 
   * @return the collection type for the object being erased, if it's a
   *         collection, null otherwise!
   */
  public Class<Collection> getCollectionType() {
    return collType;
  }

  /**
   * Utility class. JAX-WS requires type "erasure" for generic types, i.e., List<?
   * extends String> -> List<String> Passing a class object, such as Integer,
   * does not modify it, so it's safe!
   * 
   * @param type
   *          the type to erase, can be anything
   * @throws ClassNotFoundException
   */
  public void eraseType() {
    erasedClass = eraseTypeInt(type);
  }

  /**
   * Call "eraseType" first to erase the type
   * 
   * @return the erased class, null if "eraseType" has not been called
   */
  public Class<?> getErasedClass() {
    if(isArray){
      return getArrayType();
    }
    return erasedClass;
  }

  /**
   * Determine if the type is an annotated bean class. This cannot determine if
   * the type is a bean, only if it's annotated! A non-annotated bean can still
   * be used as input for JAXB!
   * 
   * @return true if \@XmlType or \@XmlRootElement annotations are present
   */
  public boolean isAnnotatedBean() {

    if (erasedClass == null) {
      erasedClass = eraseTypeInt(this.type);
    }

    return (erasedClass.getAnnotation(XmlRootElement.class) != null)
        || (erasedClass.getAnnotation(XmlType.class) != null);

  }

  private Class<?> eraseTypeInt(Type type) {

    Class<?> rawClass = null;

    // terminate recursion
    if (type instanceof Class) {
      // String[], Integer[], etc.
      if (((Class) type).isArray()) {
        isArray = true;
        arrayClass = (Class) type;
        int dim = 0;
        while(arrayClass.isArray()) {
          arrayClass = arrayClass.getComponentType();
          dim++;
        }
        this.arrDim = dim;
      }
      return (Class) type;
    }

    else if (type instanceof ParameterizedType) {

      Type rawType = ((ParameterizedType) type).getRawType();

      if (rawType instanceof Class
          && (Collection.class.isAssignableFrom((Class) rawType) || Holder.class.isAssignableFrom((Class) rawType))) {
        if (Collection.class.isAssignableFrom((Class) rawType)) {
          isCollection = true;
          collType = (Class<Collection>) rawType;
        }

        // List<Integer>, or List<?extends Integer>, or List<X extends<T
        // extendsInteger>> , or List<T>
        rawClass = eraseTypeInt(((ParameterizedType) type).getActualTypeArguments()[0]);

      } else {
        rawClass = eraseTypeInt(rawType);
      }

    } else if (type instanceof WildcardType) {

      Type[] upperBnds = ((WildcardType) type).getUpperBounds();
      rawClass = eraseTypeInt(upperBnds[0]);

    } else if (type instanceof GenericArrayType) {

      isArray = true;

      Type genComponent = ((GenericArrayType) type).getGenericComponentType();
      int arrDim = 1;
      while(genComponent instanceof GenericArrayType){
        genComponent = ((GenericArrayType)genComponent).getGenericComponentType();
        arrDim++;
      }
      
      if(genComponent instanceof Class<?>){
        rawClass = eraseTypeInt(Array.newInstance((Class<?>)genComponent, new int[arrDim]).getClass());
      } else {
        arrayClass = eraseTypeInt(genComponent);
      }

    } else if (type instanceof TypeVariable) { // e.g., List<T>
      rawClass = eraseTypeInt(((TypeVariable) type).getGenericDeclaration().getTypeParameters()[0].getBounds()[0]);

    }

    return rawClass;
  }

  /**
   * Special rules for determining the type of the bean property.
   * 
   * @return the bean property as a string
   */
  public String getErasedTypeAsString() {

    String erased = null;

    // Collections
    if (isCollection) {
      erased = collType.getName() + "<" + erasedClass.getName() + ">";

      // Arrays
    } else if (isArray) {
      erased = arrayClass.getName() + "[]";

    } else {
      if (Collection.class.isAssignableFrom(erasedClass)) {
        // Special case, a non-parameterized collection, e.g. - java.util.List
        // was used in the SEI, this becomes java.util.List<?>
        erased = erasedClass.getName() + "<?>";
      } else {

        erased = erasedClass.isMemberClass() ? erasedClass.getCanonicalName() : erasedClass.getName();
      }

    }

    return erased;
  }

}
