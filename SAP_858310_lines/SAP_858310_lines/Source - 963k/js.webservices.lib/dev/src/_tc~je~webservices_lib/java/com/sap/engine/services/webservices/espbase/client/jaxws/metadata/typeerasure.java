package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class TypeErasure {
  
  public final Class visit(Type t) {
    assert t!=null;

    if (t instanceof Class)
        return onClass((Class)t);
    if (t instanceof ParameterizedType)
        return onParameterizdType( (ParameterizedType)t);
    if(t instanceof GenericArrayType)
        return onGenericArray((GenericArrayType)t);
    if(t instanceof WildcardType)
        return onWildcard((WildcardType)t);
    if(t instanceof TypeVariable)
        return onVariable((TypeVariable)t);

    // covered all the cases
    assert false;
    throw new IllegalArgumentException();
  }  
  
  private Class onClass(Class c) {
    return c;
  }

  private Class onParameterizdType(ParameterizedType p) {
    // TODO: why getRawType returns Type? not Class?
    return visit(p.getRawType());
  }

  private Class onGenericArray(GenericArrayType g) {
    return Array.newInstance(visit(g.getGenericComponentType()),0 ).getClass();
  }

  private Class onVariable(TypeVariable v) {
    return visit(v.getBounds()[0]);
  }

  private Class onWildcard(WildcardType w) {
    return visit(w.getUpperBounds()[0]);
  }
}
