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
package com.sap.engine.services.webservices.server.deploy.j2ee.ws;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.xml.namespace.QName;

import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaWsdlMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.JavaXmlTypeMappingType;
import com.sap.engine.lib.descriptors.jaxrpcmapping.VariableMappingType;
import com.sap.engine.services.webservices.jaxrpc.schema2java.AttributeMapping;
import com.sap.engine.services.webservices.jaxrpc.schema2java.CustomizationType;
import com.sap.engine.services.webservices.jaxrpc.schema2java.ElementMapping;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeInfo;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaTypeSet;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Plamen Pavlov
 */
public class J2EE14TypesConvertor {
  private SchemaTypeSet schemaTypeSet;
  private JavaWsdlMappingType jaxRpcMapping;
  private static final String COMPLEX_TYPE_STR = "complexType";
  private static final String ELEMENT_STR = "element";
  
  /**
   * @param mapping
   * @param typesFileName
   */
  public J2EE14TypesConvertor(JavaWsdlMappingType mapping, SchemaTypeSet schemaTypeSet) {
    this.schemaTypeSet = schemaTypeSet;
    jaxRpcMapping = mapping;
  }

  /**
   * 
   */
  public SchemaTypeSet convert() {
    //saveSchemaTypeSet(schemaTypeSet);
    String tmpJavaTypeName;
    QName tmpRootTypeQName;
    VariableMappingType[] tmpVariableMappings;
    JavaXmlTypeMappingType[] typeMappings = jaxRpcMapping.getJavaXmlTypeMapping();
    if(typeMappings != null && typeMappings.length > 0){
      for(int i = 0; i < typeMappings.length; i++){

          //typeMappings[i].getChoiceGroup1().getAnonymousTypeQname();
          //typeMappings[i].getChoiceGroup1().getRootTypeQname().get_value();


        if(typeMappings[i].getQnameScope().get_value().equalsIgnoreCase(COMPLEX_TYPE_STR)){ //process complex type
          tmpJavaTypeName = typeMappings[i].getJavaType().get_value();
          tmpVariableMappings = typeMappings[i].getVariableMapping();
          
          //System.out.println("JavaXmlTypeMapping: " + i);
          
          if(typeMappings[i].getChoiceGroup1().getRootTypeQname() != null){ //process not anonymous type
            tmpRootTypeQName = typeMappings[i].getChoiceGroup1().getRootTypeQname().get_value();
            processSchemaType(tmpRootTypeQName, tmpJavaTypeName, tmpVariableMappings, true);
          } else { //process anonymous type
            //System.out.println("AnonymousQName: " + typeMappings[i].getChoiceGroup1().getAnonymousTypeQname().get_value());
            //System.out.println("AnonymousQName: " + removeArrays(typeMappings[i].getChoiceGroup1().getAnonymousTypeQname().get_value()));

            if((checkForArray(typeMappings[i].getChoiceGroup1().getAnonymousTypeQname().get_value())) || 
                (typeMappings[i].getVariableMapping() == null) || (typeMappings[i].getVariableMapping().length <= 0)){
              continue;
            }

            tmpRootTypeQName = getAnonymousQName(removeArrays(typeMappings[i].getChoiceGroup1().getAnonymousTypeQname().get_value()), true);
            if(tmpRootTypeQName != null){
              processSchemaType(tmpRootTypeQName, tmpJavaTypeName, tmpVariableMappings, true);
            }
          }
        }

        if(typeMappings[i].getQnameScope().get_value().equalsIgnoreCase(ELEMENT_STR)){//process element
        }

      }
    }
    return schemaTypeSet;
  }

  /**
   * @param schemaTypeInfoQName QName of the Complex Type
   * @param schemaTypeInfoJavaClassName Java Class name of the Complex Type
   * @param schemaTypeInfoVariableMappings Variable mappings for this QName
   * @param isComplexType boolean value which shows is this Complex Type or Element
   */
  private void processSchemaType(QName schemaTypeInfoQName, String schemaTypeInfoJavaClassName, VariableMappingType[] schemaTypeInfoVariableMappings, boolean isComplexType) {
    SchemaTypeInfo tmpSchemaTypeInfo = schemaTypeSet.get(schemaTypeInfoQName);
    if (tmpSchemaTypeInfo == null) {
      return;
    }
    tmpSchemaTypeInfo.setJavaClass(schemaTypeInfoJavaClassName);
    tmpSchemaTypeInfo.setJavaClassGenerated(false);

    if(isComplexType){
      CustomizationType[] tmpSchemaTypeInfoCustomizations;
      String customizationNamespace = schemaTypeInfoQName.getNamespaceURI();
    
      if(schemaTypeInfoVariableMappings != null && schemaTypeInfoVariableMappings.length > 0){
        tmpSchemaTypeInfoCustomizations = tmpSchemaTypeInfo.getAllCustomizations();
        if(tmpSchemaTypeInfoCustomizations != null && tmpSchemaTypeInfoCustomizations.length > 0){
          updateCustomizations(tmpSchemaTypeInfoCustomizations, schemaTypeInfoVariableMappings, customizationNamespace);
        }
      }
    }
  }
  
  /**
   * @param allCustomizations customizations which needed to be updated
   * @param allVariableMappings variable mappings
   * @param customizationNamespace namespace of the Complex Type
   */
  private void updateCustomizations(CustomizationType[] allCustomizations, VariableMappingType[] allVariableMappings, String customizationNamespace){
    for(int i = 0; i < allCustomizations.length; i++){
      switch(allCustomizations[i].getType()){
        case CustomizationType.ATTRIBUTE_MAPPING:
          updateAttributeMapping(allCustomizations[i], allVariableMappings, customizationNamespace);
        break;
        case CustomizationType.ELEMENT_MAPPING:
          updateElementMapping(allCustomizations[i], allVariableMappings, customizationNamespace);
        break;
      }
    }
  }

  /**
   * @param customizationType customizations which needed to be updated
   * @param allVariableMappings variable mapping
   * @param customizationNamespace namespace of the Complex Type
   */
  private void updateAttributeMapping(CustomizationType customizationType, VariableMappingType[] allVariableMappings, String customizationNamespace){
    AttributeMapping attributeMapping = (AttributeMapping) customizationType;
    for(int i = 0; i < allVariableMappings.length; i++){
      //if(allVariableMappings[i].getChoiceGroup1().getXmlAttributeName().equals(attributeMapping.getXmlName().getLocalPart()) && 
      //    customizationNamespace.equals(attributeMapping.getXmlName().getNamespaceURI())){

      if(allVariableMappings[i].getChoiceGroup1().getXmlAttributeName() == null ||
          allVariableMappings[i].getChoiceGroup1().getXmlAttributeName().get_value().equals("")){
        continue;
      }

      if(allVariableMappings[i].getChoiceGroup1().getXmlAttributeName().get_value().equals(attributeMapping.getXmlName().getLocalPart())){
        String a = allVariableMappings[i].getJavaVariableName().get_value();
        if (allVariableMappings[i].getDataMember() == null) {
          String b = a.substring(0, 1);
          b = b.toUpperCase(Locale.ENGLISH);
          if (a.length() > 1) {
            b = b + a.substring(1);
          }
          a = b;
        }
        attributeMapping.setJavaVarName(a);
        //System.out.println("Attribute Mapping updated");
      }
    }
  }

  /**
   * @param customizationType customizations which needed to be updated
   * @param allVariableMappings variable mapping
   * @param customizationNamespace namespace of the Complex Type
   */
  private void updateElementMapping(CustomizationType customizationType, VariableMappingType[] allVariableMappings, String customizationNamespace){
    ElementMapping elementMapping = (ElementMapping) customizationType;
    for(int i = 0; i < allVariableMappings.length; i++){
      
      if(allVariableMappings[i].getChoiceGroup1().getXmlElementName() == null ||
          allVariableMappings[i].getChoiceGroup1().getXmlElementName().get_value().equals("")){
        continue;
      }
      
      //System.out.println("XmlElementName: " + allVariableMappings[i].getChoiceGroup1().getXmlElementName().get_value());
      //System.out.println("Element Mapping, Local name: " + elementMapping.getXmlName().getLocalPart());
      //System.out.println("Customization Namespace: " + customizationNamespace);
      //System.out.println("Element Mapping, QName: " + elementMapping.getXmlName() + "<end>");
            
      //if(allVariableMappings[i].getChoiceGroup1().getXmlElementName().get_value().equals(elementMapping.getXmlName().getLocalPart()) && 
      //    customizationNamespace.equals(elementMapping.getXmlName().getNamespaceURI())){
      if(allVariableMappings[i].getChoiceGroup1().getXmlElementName().get_value().equals(elementMapping.getXmlName().getLocalPart())){
        String a = allVariableMappings[i].getJavaVariableName().get_value();
        if (allVariableMappings[i].getDataMember() == null) {
          String b = a.substring(0, 1);
          b = b.toUpperCase(Locale.ENGLISH);
          if (a.length() > 1) {
            b = b + a.substring(1);
          }
          a = b;
        }
        
        elementMapping.setJavaVarName(a);
        //System.out.println("Element Mapping updated");
      }
    }
  }
    
  /**
   * @param wholeName
   * @param isComplexType
   * 
   * @return QName of the Anonymous ComplexType
   */
  private QName getAnonymousQName(String wholeName, boolean isComplexType){
    QName resultQName = null;
    QName tmpQName = null;
    String tmpAnonymousName;
    String tmpAnonymousNameRoot;
    String tmpAnonymousNamespace;
    String localName = null; 
    boolean isRootComplexType;
      
    int namespaceEndIndex = wholeName.lastIndexOf(":");
    tmpAnonymousNamespace = wholeName.substring(0, namespaceEndIndex);
    tmpAnonymousName = wholeName.substring(namespaceEndIndex + 1, wholeName.length());
      
    isRootComplexType = isNextStepComplexType(tmpAnonymousName);
      
    tmpAnonymousName = getAnonymousName(tmpAnonymousName);
    tmpAnonymousNameRoot = getAnonymousNameRoot(tmpAnonymousName);
    tmpAnonymousName = removeFirstName(tmpAnonymousName);
     
    if(isRootComplexType){
      //tmpQName = schemaTypeSet.getElementType(new QName(tmpAnonymousNamespace, tmpAnonymousNameRoot));//TODO replace this with the under
      //tmpQName = new QName(tmpAnonymousNamespace, tmpAnonymousNameRoot);
      resultQName = processAnonymousComplexType(new QName(tmpAnonymousNamespace, tmpAnonymousNameRoot), tmpAnonymousName);
    } else {
      tmpQName = new QName(tmpAnonymousNamespace, tmpAnonymousNameRoot);
      tmpQName = schemaTypeSet.getElementType(tmpQName);
      resultQName = processAnonymousComplexType(tmpQName, tmpAnonymousName);
    }

    //System.out.println("Anonymous QName: " + resultQName);
    return resultQName; 
  }
  
  /**
   * @param typeQName QName of the Complex Type
   * @param elementMappingQName QName of the element in the Complex Type
   * 
   * return QName referenced Complex Type from the element
   */
  private QName getTypeQNameFromElement(QName typeQName, QName elementMappingQName){
    SchemaTypeInfo tmpSchemaTypeInfo = schemaTypeSet.get(typeQName);
    if(tmpSchemaTypeInfo == null){
      return null;
    } else {
      CustomizationType[] tmpCustomizationTypes = tmpSchemaTypeInfo.getAllCustomizations();
      if(tmpCustomizationTypes != null || tmpCustomizationTypes.length > 0){
        for(int i = 0; i < tmpCustomizationTypes.length; i++){
          if(tmpCustomizationTypes[i] instanceof ElementMapping){
            ElementMapping tmpElementMapping = (ElementMapping) tmpCustomizationTypes[i];
            //if(tmpElementMapping.getXmlName().equals(elementMappingQName)){
            if(tmpElementMapping.getXmlName().getLocalPart().equals(elementMappingQName.getLocalPart())){
              return tmpElementMapping.getTypeReference();
            }
          }
        }
      return null;
      } else {
        return null;
      }
    }
  }
  
  /**
   * @param names sequence of names separated with >
   * 
   * return String names from 2nd to last
   */
  private String removeFirstName(String names){
    int symbolIndex = names.indexOf(">");
    if(symbolIndex < 0){
      return null;
    } else {
      return names.substring(symbolIndex + 1);
    }
  }
  
  /**
   * @param names sequence of names separated with >
   * 
   * return String the first name
   */
  private String getFirstName(String names){
    int symbolIndex = names.indexOf(">");
    if(symbolIndex < 0){
      return names;
    } else {
      return names.substring(0, symbolIndex);
    }
  }

  /**
   * @param typeQName QName of the Complex Type
   * @param anonymousName
   */
  private QName processAnonymousComplexType(QName typeQName, String elementsNames){
    String tmpNamespace = typeQName.getNamespaceURI();
    String tmpElementName;
    
    while (elementsNames != null && (!elementsNames.equalsIgnoreCase("")) && typeQName != null) {
      tmpElementName = getFirstName(elementsNames);
      elementsNames = removeFirstName(elementsNames);
      typeQName = getTypeQNameFromElement(typeQName, new QName(tmpNamespace, tmpElementName));
    }
    
    return typeQName;
  }

  /**
   * @param anonymousName part of Anonymous name from the Anonymous-Type-QName without starting ">"-s
   * 
   * @return 
   */
  private boolean isAnonymousNameProcessNeeded(String anonymousName){
    if(anonymousName.indexOf(">") > 0){
      return true;
    } else {
      return false;
    }
    
    //schemaTypeSet.get
  }
  
  /**
   * @param anonymousName whole Anonymous name from the Anonymous-Type-QName
   * 
   * @return Anonymous name from the Anonymous-Type-QName without starting ">"-s
   */
  private static String getAnonymousName(String anonymousName){
    while (anonymousName.indexOf(">") == 0) {
      anonymousName = anonymousName.substring(1);
    }
    return anonymousName;
  }
  
  /**
   * @param anonymousName Anonymous name from the Anonymous-Type-QName without starting ">"-s
   * 
   * @return Top-Level Element or ComplexType for this Anonymous QName
   */
  private static String getAnonymousNameRoot(String anonymousName){
    if(anonymousName.indexOf(">") < 0){
        return anonymousName;
    } else {
      return anonymousName.substring(0, anonymousName.indexOf(">"));
    }
  }

  /**
   * @param anonymousName whole Anonymous name from the Anonymous-Type-QName
   * 
   * @return accordin to incoming parameter is next iteration a ComplexType
   */
  private boolean isNextStepComplexType(String anonymousName){
    return (getDepth(anonymousName) % 2) == 0;
  }

  /**
   * @param anonymousName whole Anonymous name from the Anonymous-Type-QName
   * 
   * @return how meny iterations should be done to reach Top-Level Element or ComplexType
   */
  private int getDepth(String anonymousName){
    int symbolIndex = anonymousName.indexOf(">");
    
    if(symbolIndex < 0){
      return 0;
    } else {
      String tmpAnonymousName; 
      if(symbolIndex == 0){
        tmpAnonymousName = anonymousName.substring(1);
      } else {
        tmpAnonymousName = anonymousName.substring(0, symbolIndex) + anonymousName.substring(symbolIndex+1, anonymousName.length());
      }
      return (1 + getDepth(tmpAnonymousName));
    }
  }
  
  private String removeArrays(String anonymousName){
      String tmpAnonymousName;
      tmpAnonymousName = anonymousName;
      int arrayStartIndex;
      int arrayEndIndex;
      
      arrayStartIndex = tmpAnonymousName.lastIndexOf("[");
      arrayEndIndex   = tmpAnonymousName.lastIndexOf("]");
      
      while (arrayStartIndex > 0) {
        tmpAnonymousName = tmpAnonymousName.substring(0, arrayStartIndex) + tmpAnonymousName.substring(arrayEndIndex+1);
        arrayStartIndex = tmpAnonymousName.lastIndexOf("[");
        arrayEndIndex   = tmpAnonymousName.lastIndexOf("]");
      }
      
      return tmpAnonymousName;      
  }

  private boolean checkForArray(String anonymousName){
    if(anonymousName.lastIndexOf("[") >= 0){
      return true;
    } else {
      return false;
    }
  }

  private void saveSchemaTypeSet(SchemaTypeSet schemaTypeSet){
    try
	{
		schemaTypeSet.saveSettings(new FileOutputStream("E:/Temp/del/SchemaTypeSet.xml"));
	}
	catch (FileNotFoundException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
  }
    
  public static void main(String[] args) throws Exception {
    //SchemaTypeSet schemaTypeSet1 = null;
    //schemaTypeSet1.getElementType(new QName(""));
    //SchemaTypeInfo schemaTypeInfo1;
    //schemaTypeInfo1.
    
    //System.out.println(removeArrays("plamen[tem1]lakov[temp2]pavlov[temp3]"));
    
    //String tmpString = "plamen>pavlov";
    //System.out.println(tmpString.substring(0, tmpString.indexOf(">")));
    //tmpString = tmpString.substring(tmpString.indexOf(">") + 1);
    //System.out.println(tmpString);
    
    //String s = "http://com.sap.ide:>>>anonymousName>name1>name2>name3";
    //String s = "http://com.sap.ide:>anonymousName";
    //int s1 = s.lastIndexOf(":");
    //int index1 = s.indexOf(">");
    //String namespace = s.substring(0, s1);
    
    //String namespace1 = s.substring(0, s.lastIndexOf(":"));
    
    //System.out.println("Original string: " + s);
    //System.out.println("Last index of \":\" " + s1);
    //System.out.println("Index of >: " + index1);
    //System.out.println("Namespace: " + namespace);
    //System.out.println("Namespace1: " + namespace1);
    
    //System.out.println();
    //String anonymousname = s.substring(s.lastIndexOf(":") + 1);
    //System.out.println(anonymousname);
    //System.out.println("Anonymous name: " + getAnonymousNameRoot(anonymousname));
    //QName qName = getAnonymousQName(s, true);
    //System.out.println(getAnonymousName(anonymousname));  
    //System.out.println(anonymousname);
    //System.out.println(getAnonymousNameRoot(getAnonymousName(anonymousname)));  
  }
}
