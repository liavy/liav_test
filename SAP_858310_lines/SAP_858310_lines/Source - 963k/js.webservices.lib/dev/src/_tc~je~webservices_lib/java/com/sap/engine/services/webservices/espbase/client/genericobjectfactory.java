/*
 * Created on 2005-7-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.impl.GenericObjectImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttribute;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DBaseType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DComplexType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DElement;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DGroup;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DNamedNode;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleContent;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DStructure;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class GenericObjectFactory {

  private static GenericObject createComplexTypeObject(DComplexType complType, ExtendedTypeMapping typeMapping) {
    GenericObject genericObject = new GenericObjectImpl(complType.getTypeName());
    DField[] attributes = complType.getAttributes();
    for(int i = 0; i < attributes.length; i++) {
      DAttribute attrib = (DAttribute)attributes[i];
      String attribDefaultValue = attrib.getDefaultValue();
      DSimpleType attribType = (DSimpleType)(typeMapping.getTypeMetadata(attrib.getFieldType()));
      genericObject._setAttribute(attrib.getFieldName(), createSipleTypeObject(attribType, attribDefaultValue, typeMapping));
    }
    intWithStructureFields(genericObject, (DStructure)complType, typeMapping);
    return(genericObject);
  }
  
  private static void intWithStructureFields(GenericObject genericObject, DStructure structure, ExtendedTypeMapping typeMapping) {
    DField[] fields = structure.getFields();
    for(int i = 0; i < fields.length; i++) {
      DField field = fields[i];
      QName fieldName = ((DNamedNode)field).getFieldName();
      genericObject._setField(fieldName, createFieldObject(field, typeMapping));
    }
  }
  
  private static Object createFieldObject(DField field, ExtendedTypeMapping typeMapping) {
    switch(field.getType()) {
      case DField.ELEMENT : {
        return(createElementObject((DElement)field, typeMapping));
      }
      case DField.SIMPLE : {
        return(createSimpleContentObject((DSimpleContent)field, typeMapping));
      }
      default : {
        return(createGroupObject((DGroup)field, typeMapping));
      }
    }
  }

  private static Object createGroupObject(DGroup group, ExtendedTypeMapping typeMapping) {
    GenericObject groupObject = new GenericObjectImpl(null);
    intWithStructureFields(groupObject, (DStructure)group, typeMapping);
    int maxOccurs = group.getMaxOccurs();
    if(maxOccurs > 1) {
      GenericObject[] groupObjects = new GenericObject[maxOccurs];
      for(int i = 0; i < groupObjects.length; i++) {
        groupObjects[i] = groupObject;
      }
      return(groupObjects);
    }
    return(groupObject);
  }
  
  private static Object createSimpleContentObject(DSimpleContent simpleContent, ExtendedTypeMapping typeMapping) {
    QName typeName = simpleContent.getFieldType();
    DSimpleType simpleType = (DSimpleType)(typeMapping.getTypeMetadata(typeName));
    return(createSipleTypeObject(simpleType, null, typeMapping));
  }
  
  public static Object createTypeObject(DBaseType type, String defaultValue, ExtendedTypeMapping typeMapping) {
    return(type instanceof DSimpleType ? createSipleTypeObject((DSimpleType)type, defaultValue, typeMapping) : createComplexTypeObject((DComplexType)type, typeMapping));
  }
  
  private static Object createElementObject(DElement element, ExtendedTypeMapping typeMapping) {
    QName elementTypeName = element.getFieldType();
    DBaseType elementType = typeMapping.getTypeMetadata(elementTypeName);
    Object elementObject = createTypeObject(elementType, element.getDefaultValue(), typeMapping);
    int maxOccurs = element.getMaxOccurs();
    if(maxOccurs > 1) {
      Object[] elementObjects = new Object[5];
      for(int i = 0; i < elementObjects.length; i++) {
        elementObjects[i] = elementObject;
      }
      return(elementObjects);
    }
    return(elementObject);
  }
  
  private static Object createSipleTypeObject(DSimpleType simpleType, String defaultValue, ExtendedTypeMapping typeMapping) {
    Class javaClass = typeMapping.getDefaultJavaClass(simpleType.getTypeName());
    if(javaClass.equals(Byte.class)) {
      return(defaultValue == null ? new Byte((byte)0) : new Byte(defaultValue));
    } else if(javaClass.equals(Long.class)) {
      return(defaultValue == null ? new Long(0) : new Long(defaultValue));
    } else if(javaClass.equals(Integer.class)) {
      return(defaultValue == null ? new Integer(0) : new Integer(defaultValue));
    } else if(javaClass.equals(Short.class)) {
      return(defaultValue == null ? new Short((short)0) : new Short(defaultValue));
    } else if(javaClass.equals(Double.class)) {
      return(defaultValue == null ? new Double(0) : new Double(defaultValue));
    } else if(javaClass.equals(Float.class)) {
      return(defaultValue == null ? new Float(0) : new Float(defaultValue));
    } else if(javaClass.equals(Boolean.class)) {
      return(defaultValue == null ? new Boolean(true) : new Boolean(defaultValue));
    } else if(javaClass.equals(String.class)) {
      return(defaultValue == null ? "" : defaultValue);
    } else if(javaClass.equals(BigInteger.class)) {
      return(defaultValue == null ? BigInteger.valueOf(0) : new BigInteger(defaultValue));
    } else if(javaClass.equals(BigDecimal.class)) {
      return(defaultValue == null ? BigDecimal.valueOf(0) : new BigDecimal(defaultValue));
    } else if(javaClass.equals(Calendar.class)) {
      return(new GregorianCalendar());
    } else if(javaClass.equals(ArrayList.class)) {
      ArrayList list = new ArrayList();
      list.add(new Integer(0));
      list.add("string");
      return(list);
    } else if(javaClass.isArray()) {
      return(Array.newInstance(javaClass.getComponentType(), 5));
    }
    return(null);
  }
}
