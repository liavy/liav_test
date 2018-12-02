/*
 * Created on 2005-7-26
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.content.impl;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenericObjectImpl implements GenericObject {
  
  private QName objectType;
  private Hashtable fieldNamesToValuesMapping;
  private Hashtable attribNamesToValuesMapping;
  
  public GenericObjectImpl() {
    fieldNamesToValuesMapping = new Hashtable();
    attribNamesToValuesMapping = new Hashtable();
  }

  public GenericObjectImpl(QName objectType) {
    this();
    this.objectType = objectType;
  }
  
  public Hashtable getFieldNamesToValuesMapping() {
    return(fieldNamesToValuesMapping);
  }
  
  public Hashtable getAttribNamesToValuesMapping() {
    return(attribNamesToValuesMapping);
  }
  
  public GenericObjectImpl(String namespace, String name) {
    this(new QName(namespace,name));
  }

  public QName _getObjectType() {
    return(objectType);
  }

  public void _setField(QName fieldName, Object fieldContent) {
    set(fieldNamesToValuesMapping, fieldName, fieldContent);
  }
  
  private void set(Hashtable mapping, QName keyName, Object value) {
    if(value == null) {
      mapping.remove(keyName);
    } else {
      mapping.put(keyName, value);
    }
  }

  public Object _getField(QName fieldName) {
    return(fieldNamesToValuesMapping.get(fieldName));
  }

  public void _setAttribute(QName attributeName, Object attrContent) {
    set(attribNamesToValuesMapping, attributeName, attrContent);
  }

  public Object _getAttribute(QName attributeName) {
    return(attribNamesToValuesMapping.get(attributeName));
  }
  
  public String toString() {
    StringBuffer representationBuffer = new StringBuffer();
    initStringRepresentationBuffer(representationBuffer, "");
    return(representationBuffer.toString());
  }
  
  private void initStringRepresentationBuffer(StringBuffer representationBuffer, String offset) {
    representationBuffer.append("GenericObject");
    initStringRepresentationBuffer_Object(representationBuffer, offset + Util.TO_STRING_OFFSET, "hashcode = ", hashCode());
    initStringRepresentationBuffer_Object(representationBuffer, offset + Util.TO_STRING_OFFSET, "type name = ", objectType);
    initStringRepresentationBuffer_Components(representationBuffer, offset + Util.TO_STRING_OFFSET, attribNamesToValuesMapping, "Attributes :");
    initStringRepresentationBuffer_Components(representationBuffer, offset + Util.TO_STRING_OFFSET, fieldNamesToValuesMapping, "Fields :");
  }
  
  private void initStringRepresentationBuffer_Object(StringBuffer representationBuffer, String offset, String label, Object obj) {
    initStringRepresentationBuffer_Label(representationBuffer, offset, label);
    initStringRepresentationBuffer_Object(representationBuffer, createOffset(offset, label), obj);
  }
  
  private String createOffset(String offset, String prefix) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(offset);
    for(int i = 0; i < prefix.length(); i++) {
      buffer.append(' ');
    }
    return(buffer.toString());
  }
  
  private void initStringRepresentationBuffer_Object(StringBuffer representationBuffer, String offset, Object obj) {
    if(obj == null) {
      representationBuffer.append("null");
    } else if(obj instanceof GenericObjectImpl) {
      ((GenericObjectImpl)obj).initStringRepresentationBuffer(representationBuffer, offset);
    } else if(obj.getClass().isArray()) {
      initStringRepresentationBuffer_Array(representationBuffer, offset, obj);
    } else if(obj instanceof ArrayList) {
      initStringRepresentationBuffer_ArrayListValue(representationBuffer, offset, (ArrayList)obj);
    } else {
      representationBuffer.append(determineStringRepresentation(obj));
    }
  }
  
  private void initStringRepresentationBuffer_Label(StringBuffer representationBuffer, String offset, String label) {
    representationBuffer.append("\n");
    representationBuffer.append(offset);
    representationBuffer.append(label);
  }
  
  private void initStringRepresentationBuffer_Components(StringBuffer representationBuffer, String offset, Hashtable componentsMap, String prefix) {
    initStringRepresentationBuffer_Label(representationBuffer, offset, prefix);
    Enumeration namesEnum = componentsMap.keys();
    while(namesEnum.hasMoreElements()) {
      QName name = (QName)(namesEnum.nextElement());
      Object value = componentsMap.get(name);
      initStringRepresentationBuffer_Object(representationBuffer, offset + Util.TO_STRING_OFFSET, name.toString() + " = ", value);
    }
  }
  
  private void initStringRepresentationBuffer_Array(StringBuffer representationBuffer, String offset, Object value) {
    representationBuffer.append("Array ");
    representationBuffer.append(value.getClass().getName());
    representationBuffer.append(" : [");
    for(int i = 0; i < Array.getLength(value); i++) {
      Object memberValue = Array.get(value, i);
      initStringRepresentationBuffer_Object(representationBuffer, offset + Util.TO_STRING_OFFSET, "value" + i + " = ", memberValue);
    }
    representationBuffer.append(" ]");
  }

  private void initStringRepresentationBuffer_ArrayListValue(StringBuffer representationBuffer, String offset, ArrayList value) {
    representationBuffer.append("ArrayList ");
    representationBuffer.append(" : [");
    for(int i = 0; i < value.size(); i++) {
      Object memberValue = value.get(i);
      initStringRepresentationBuffer_Object(representationBuffer, offset + Util.TO_STRING_OFFSET, "value" + i + " = ", memberValue);
    }
    representationBuffer.append(" ]");
  }
  
  private String determineStringRepresentation(Object value) {
    return(value instanceof Node ? transformElementToString((Node)value) : value.toString());
  }
  
  private String transformElementToString(Node node) {
    ByteArrayOutputStream xmlByteArrayOutput = new ByteArrayOutputStream(); 
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(node), new StreamResult(xmlByteArrayOutput));
    } catch(Exception exc) {
      return(null);
    } finally {
      try {
        xmlByteArrayOutput.close();
      } catch(Exception exc) {
    	//$JL-EXC$
    	//nothing to do
      }
    }
    return(new String(xmlByteArrayOutput.toByteArray())); //$JL-I18N$
  }
}
