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
package com.sap.engine.services.webservices.jaxws;

import java.util.*;

import javax.xml.namespace.QName;

import com.sap.engine.lib.schema.components.*;
import com.sap.engine.services.webservices.jaxrpc.schema2java.*;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class JaxbSchemaToJavaMap {
  
  private Schema schema = null;
  private SchemaTypeSet ts = null;
  private NameConvertor converter = new NameConvertor();

  
  private Model  model = null;
  
    public JaxbSchemaToJavaMap(SchemaTypeSet ts, Model m, Schema schema){      
      //do we allow null model?
      if(ts == null){
        throw new IllegalArgumentException();
      }      
      this.ts = ts;
      model = m;
      this.schema = schema;      
    }
  
    private void loadClasInfo(CClassInfo classInfo, SchemaTypeInfo schemaTypeInfo) {
      List<CPropertyInfo> properties = classInfo.getProperties();
      for (int i=0; i<properties.size(); i++) {
        CPropertyInfo property = properties.get(i);
        if (property instanceof CReferencePropertyInfo) { // JAXBElement reference
          CReferencePropertyInfo referenceProperty = (CReferencePropertyInfo) property;
          Iterator<CElement> elements = referenceProperty.getElements().iterator();
          String elementType = null;
          QName elementName = null;
          String elementJavaName = null;
          while (elements.hasNext()) {
            CElement element = elements.next();            
            elementName = element.getElementName();
            elementJavaName = property.getName(true);                               
            elementType = element.getType().fullName();
            break;
          }          
          if (elementType != null) {
            // The element is a list.
            if (referenceProperty.isCollection()) {
              elementType = List.class.getName()+"<"+elementType+">";
            }
          }
          if (elementType != null) {
            // Add element customization
            ElementMapping mapping = new ElementMapping();
            mapping.setDataMember(false);
            mapping.setJavaVarName(elementJavaName);
            mapping.setXmlName(elementName);
            mapping.setJavaTypeName(elementType);
            schemaTypeInfo.addCustomization(mapping);
          }
        }
        if (property instanceof CElementPropertyInfo) { // Element definition
          CElementPropertyInfo elementProperty = (CElementPropertyInfo) property;                            
          List<CTypeRef> types = elementProperty.getTypes();
          String elementType = null;
          QName elementName = null;
          String elementJavaName = null;              
          if (types != null && types.size() == 1) {                
            CTypeRef elType = types.get(0);
            elementName = elType.getTagName();
            elementJavaName = property.getName(true); 
            elementType = elType.getTarget().getType().fullName();
            if (elementProperty.getAdapter() != null && elementProperty.getAdapter().customType != null) {
              if (elementProperty.getAdapter().customType.fullName() != null) {
                elementType = elementProperty.getAdapter().customType.fullName();
              }
            }            
            if (elementProperty.isCollection()) {
              elementType = List.class.getName()+"<"+elementType+">";
            } else {
              if (elementProperty.isUnboxable()) {
                elementType = converter.unwrap(elementType);
              }
              if (elementProperty.isValueList()) {
                elementType = elementType+"[]";
              }                  
            }                                
          }
          if (elementType != null) {
            // Add element customization
            ElementMapping mapping = new ElementMapping();
            mapping.setDataMember(false);
            mapping.setJavaVarName(elementJavaName);
            mapping.setXmlName(elementName);
            mapping.setJavaTypeName(elementType);
            if (elementProperty.isValueList()) {
              mapping.setListType(true);
            }
            schemaTypeInfo.addCustomization(mapping);
          }              
        }
        if (property instanceof CAttributePropertyInfo) { // Attribute definition
          CAttributePropertyInfo attributeProperty = (CAttributePropertyInfo) property;
          String attributeType = null;
          QName attributeName = null;
          String attributeJavaName = null;                            
          attributeType = attributeProperty.getTarget().getType().fullName();
          if (attributeProperty.isCollection()) {
            attributeType = List.class.getName()+"<"+attributeType+">";
          } else {
            if (attributeProperty.isUnboxable()) {
              attributeType = converter.unwrap(attributeType);
            }
          }   
          attributeName = attributeProperty.getXmlName();
          attributeJavaName = property.getName(true); 
          if (attributeType != null) {
            // Add element customization                
            AttributeMapping mapping = new AttributeMapping();
            mapping.setDataMember(false);
            mapping.setJavaVarName(attributeJavaName);
            mapping.setXmlName(attributeName);
            mapping.setJavaTypeName(attributeType);
            schemaTypeInfo.addCustomization(mapping);
          }                            
        }
        if (property instanceof CValuePropertyInfo) { // Simple content
          CValuePropertyInfo valueProperty = (CValuePropertyInfo) property;
          String valueType = null;
          String valueJavaName = null;                            
          valueType = valueProperty.getTarget().getType().fullName();
          if (valueProperty.isCollection()) {
            valueType = List.class.getName()+"<"+valueType+">";
          } else {
            if (valueProperty.isUnboxable()) {
              valueType = converter.unwrap(valueType);
            }
          }   
          valueJavaName = property.getName(true); 
          if (valueType != null) {
            // Add element customization                
            SimpleContentMapping mapping = new SimpleContentMapping();
            mapping.setDataMember(false);
            mapping.setJavaVarName(valueJavaName);
            mapping.setJavaTypeName(valueType);
            schemaTypeInfo.addCustomization(mapping);
          }                                      
        }
      }
    }
    
    private void process(HashMap<QName, QName> initialElems,HashMap<String,QName> anonymousTypes) throws JaxbSchemaToJavaGenerationException {
      
      //get all anonymous types defined in complex elements
      Map<QName, CClassInfo> anonCplx = model.createTopLevelBindings();
      for(QName elName : anonCplx.keySet()){        
        QName elType = ts.getElementType(elName);
        if (elType == null) {
          continue;
        }
        CClassInfo classInfo = anonCplx.get(elName);
        ts.setJavaType(elType, classInfo.getName());
        //System.out.println(elType+" -> "+ classInfo.getName());
        // Complex type definition analysis
        SchemaTypeInfo schemaTypeInfo = ts.get(elType);
        loadClasInfo(classInfo,schemaTypeInfo);        
      }
      
      //get all enums
      for(NClass cl : model.enums().keySet()){        
        ts.setJavaType(((CEnumLeafInfo)cl).getTypeName(), cl.fullName());               
      }            
      //gel all top-defined types            
      for(QName typeQName : model.typeUses().keySet()){
        TypeUse tu = (TypeUse) model.typeUses().get(typeQName);
        CNonElement nonElement = tu.getInfo();
        NType type = nonElement.getType();        
        String javaType = type.fullName();
        //System.out.println(typeQName+" -> "+javaType);
        if (SchemaMappingConstants.SCHEMA_NAMESPACE.equals(typeQName.getNamespaceURI()) && "anySimpleType".equals(typeQName.getLocalPart())) {
          // The mapping says that anySimpleType is mapped to string wich is true only for attributes not for elements.
          // To not override the default one the algorithm bypasses the anySimpleType mapping.
          continue;
        }
        if (tu.isCollection()) {
          // This type is Schema List
          javaType = javaType+"[]";            
        }  
        if (ts.getJavaType(typeQName) == null) {        
          if (tu.getAdapterUse() != null && tu.getAdapterUse().customType != null) {
            if (tu.getAdapterUse().customType.fullName() != null) {
              javaType = tu.getAdapterUse().customType.fullName();
            }
          }           
          ts.setJavaType(typeQName, converter.unwrap(javaType) );
          if (tu.isCollection() || tu.getAdapterUse() != null) {
            SchemaTypeInfo schemaTypeInfo = ts.get(typeQName);
            if (schemaTypeInfo != null) {
              SimpleTypeMapping simpleTypeMapping = new SimpleTypeMapping();
              if (tu.isCollection()) {
                simpleTypeMapping.setListType(true);
              }
              if (tu.getAdapterUse() != null) {
                String adapterType = tu.getAdapterUse().adapterType.fullName();
                simpleTypeMapping.setAdapterClass(adapterType);
              }
              schemaTypeInfo.addCustomization(simpleTypeMapping);
            }
          }
        } else {          
          if (tu.isCollection() || tu.getAdapterUse() != null) {
            SchemaTypeInfo schemaTypeInfo = ts.get(typeQName);
            if (schemaTypeInfo != null) {              
              SimpleTypeMapping simpleTypeMapping = new SimpleTypeMapping();
              if (tu.isCollection()) {
                simpleTypeMapping.setListType(true);
              }
              if (tu.getAdapterUse() != null) {
                String adapterType = tu.getAdapterUse().adapterType.fullName();
                simpleTypeMapping.setAdapterClass(adapterType);
              }
              schemaTypeInfo.addCustomization(simpleTypeMapping);
            }            
          }
        }
        // Special processing to handle field Schema to Java mapping.
        if (tu.getInfo().getType() instanceof CClassInfo) {
          // Complex type definition
          SchemaTypeInfo schemaTypeInfo = ts.get(typeQName);
          CClassInfo classInfo = (CClassInfo) tu.getInfo().getType();
          loadClasInfo(classInfo,schemaTypeInfo);
        }        
      }   
      Map<NClass,CClassInfo> beans = model.beans();
      Iterator<NClass> it =  beans.keySet().iterator();
      while (it.hasNext()) {
        NClass item = it.next();
        CClassInfo classInfo = beans.get(item);                
        if (classInfo.getTypeName() == null && classInfo.isElement() == false) { // This is anonymous type
          XSComponent xsComponent = classInfo.getSchemaComponent();   
          if (xsComponent instanceof XSComplexType && classInfo.parent() != null) { // This is a complex type
            XSComplexType complexType = (XSComplexType) xsComponent;
            if (complexType.getName() != null) { // Anonymous complex type
              System.out.println("Error ! This type is not anonymous !");
            }            
            if (classInfo.parent() instanceof CClassInfoParent.Package) { // This is a top level class
              XSElementDecl scope = complexType.getScope();
              if (scope.isGlobal()) { // Global Element declaration
                QName elementName = new QName(scope.getTargetNamespace(),scope.getName());
                QName elementType = ts.getElementType(elementName);
                String javaClassName = classInfo.fullName();
                ts.setJavaType(elementType,javaClassName);
                SchemaTypeInfo schemaTypeInfo = ts.get(elementType);
                loadClasInfo(classInfo,schemaTypeInfo);
              } else {
                // This handles the case where special option to auto customize the inner anonymous type and assign a global name to them
                // This is obsolete case.
            String className = classInfo.fullName();             
            className = NameConvertor.getClassName(className);
            String namespace = ((XSComplexType) xsComponent).getTargetNamespace();
            String key = "["+namespace+"]"+className;
            QName newName = anonymousTypes.get(key);
            if (newName != null) {
              String javaName = classInfo.fullName();
              ts.setJavaType(newName,javaName);
              SchemaTypeInfo schemaTypeInfo = ts.get(newName);
              loadClasInfo(classInfo,schemaTypeInfo);
            }
          }
            }
            if (classInfo.parent() instanceof CClassInfo) { // This is inner class                        
            CClassInfo  parent = (CClassInfo) classInfo.parent();
            String innerElements = "";
            while (parent.getTypeName() == null && (parent.isElement() == false) && (parent.parent() != null) && (parent.parent() instanceof CClassInfo)) {
              XSComponent xsComponentTemp = parent.getSchemaComponent();
                if (xsComponentTemp instanceof XSComplexType) {
                  XSComplexType complexTypeParent = (XSComplexType) xsComponentTemp;
                  XSElementDecl elementImpl = complexTypeParent.getScope();   
                if (elementImpl != null) {
                  QName elementName = new QName(elementImpl.getTargetNamespace(),elementImpl.getName());
                  innerElements = "<"+elementName.toString()+">"+innerElements;
                }
              }                            
              parent = (CClassInfo) parent.parent();
            }
            if (complexType.getScope() != null) {
              QName parentName = null;
              if (parent.getTypeName() != null) {
                parentName = parent.getTypeName();
              }
              //if (parent.isRootElement) { - changed temporary
              if (parent.isElement()) {
                QName elementName = parent.getElementName();
                parentName = ts.getElementType(elementName);
                //System.out.println("Element found :"+elementName+" -> "+parentName);
              }
              if (parentName != null) {
                // System.out.println("Anonymous type found: "+classInfo.fullName());
                XSElementDecl elementImpl = complexType.getScope();                               
                String targetNamespace = complexType.getTargetNamespace();
                QName elementName = new QName(elementImpl.getTargetNamespace(),elementImpl.getName());
                String key=JaxbSchemaParser.createKey(targetNamespace,elementName,parentName,innerElements);
                //System.out.println("Key formed : "+key);
                QName newName = anonymousTypes.get(key);
                if (newName != null) {
                  //System.out.println("Key value found :"+newName);
                  String javaName = classInfo.fullName();
                  if (parent.parent() instanceof CClassInfoParent.Package) {                    
                    int length = ((CClassInfoParent.Package) parent.parent()).fullName().length();
                    String packagePart = javaName.substring(0,length);
                    String classPart = javaName.substring(length+1);                    
                    javaName = packagePart+"."+classPart.replace('.','$');
                  }
                  ts.setJavaType(newName,javaName);
                  SchemaTypeInfo schemaTypeInfo = ts.get(newName);
                  loadClasInfo(classInfo,schemaTypeInfo);
                  //System.out.println("Type registered !");
                } else {
                  System.out.println("Error :"+key);
                }
                
              }
            }
          }
        }
      }
      }
      //get all anonymous simple types + all elements (anonymous simple are also mapped to JAXBElement 
      for(CElementInfo el : model.getAllElements()){        
        QName schemaType = ts.getElementType(el.getElementName());        
        if (schemaType == null) {
          continue;          
        }
        String full = el.fullName();
        String normalType = null;
        if (full.indexOf("javax.xml.bind.JAXBElement<") != -1) {
          int startIx = full.indexOf("javax.xml.bind.JAXBElement<") + "javax.xml.bind.JAXBElement<".length();
          normalType = full.substring(startIx, full.lastIndexOf(">"));          
        } else {
          normalType = full;
        }
        if (ts.getJavaType(schemaType) == null) {
          ts.setJavaType(schemaType, normalType);
        }
      }
      
      
    }
    
    public void initializeMappings(HashMap<QName, QName> initialElems,HashMap<String,QName> anonymousTypes) throws JaxbSchemaToJavaGenerationException {
      
      if(initialElems == null){
        throw new IllegalArgumentException();
      }
      
      for(QName el : initialElems.keySet()){
        ts.setElementType(el, initialElems.get(el));  
      }
      
      if(model != null){                                   
        process(initialElems,anonymousTypes);        
      }      
    }   
   
}
