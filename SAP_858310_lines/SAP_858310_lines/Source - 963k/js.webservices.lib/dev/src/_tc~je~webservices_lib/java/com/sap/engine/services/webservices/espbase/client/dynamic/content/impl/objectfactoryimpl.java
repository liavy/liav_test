/*
 * Created on 2005-7-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.content.impl;

import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DComplexType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DGroup;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ObjectFactoryImpl implements ObjectFactory {
  
  private ExtendedTypeMapping typeMapping;
  
  public ObjectFactoryImpl(ExtendedTypeMapping typeMapping) {
    this.typeMapping = typeMapping;
  }
  
  public GenericObject createComplexInstance(DComplexType objectType) {
    GenericObjectImpl genericObject = null;
    if (objectType.isAnonymous() == false) {
      genericObject = new GenericObjectImpl(objectType.getTypeName());
    } else {
      genericObject = new GenericObjectImpl(null);
    }

//    genericObject._setObjectType(objectType.getTypeName());
    return(genericObject);
  }

  public GenericObject createGroupInstance(DGroup objectType) {
    GenericObjectImpl genericObject = new GenericObjectImpl(null);
    return(genericObject);
  }
}
