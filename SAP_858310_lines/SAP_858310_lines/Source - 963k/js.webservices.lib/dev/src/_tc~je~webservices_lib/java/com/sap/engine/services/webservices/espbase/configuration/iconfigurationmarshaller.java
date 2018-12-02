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
package com.sap.engine.services.webservices.espbase.configuration;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface provides method for checking the validity of configuration, applying of default properties, as well as 'validating' of BindingData object against certain list of Variants.
 * Also it provides methods for convertion of properties to assertions and vice-versa. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-13
 */
public interface IConfigurationMarshaller { 
  
  public static final String INTERFACE_LEVEL  =  "interface";
  public static final String BINDING_LEVEL  =  "binding";
  public static final String INTERFACE_OPERATION_LEVEL  =  "interface/operation";
  public static final String BINDING_OPERATION_LEVEL  =  "binding/operation";
  /**
   * Constant determing that the processing should be done as for consumer/client WS side.
   */
  public static final int CONSUMER_MODE =  1;
  /**
   * Constant determing that the processing should be done as for provider/server WS side.
   */
  public static final int PROVIDER_MODE =  2;
  
  /**
   * Returns Set of assertions' QNames that are supported. 
   * The returned value must not be null.
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   */
  public Set getKnownAssertions(int mode);
  
  /**
   * Returns Set of properties' QNames that are supported.
   * The returned value must not be null. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   */
  public Set getKnownProperties(int mode);
  
  /**
   * Converts the assertions elements into PropertyTypes.
   * 
   * @param assertion An array of DOM elements, which qnames are contained in the set returned by the getKnownAssertions() method.
   * @param level Constant denoting at which WSDL level the dom elements are from.
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return List of PropertyType objects. The returned value must not be null. 
   *  
   */
  public List unmarshalAssertions(Element[] assertions, String level, int mode) throws ConfigurationMarshallerException;

  /**
   * Converts the PropertyTypes into assertion Elements.
   *   
   * @param properties An array of PropertyType objects, which qnames are contained in the set returned by the getKnownProperties() method.
   * @param rootDoc Document object which must be used for creating DOM elements.
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return List of DOM element objects. The returned value must not be null. 
   */
  public List marshalAssertions(PropertyType[] properties, Document rootDoc, String level, int mode) throws ConfigurationMarshallerException;
  
  /**
   * Applies the default property values to properties which are missing.
   * The default properties are added to the <code>properties</code> parameter.
   * 
   * @param properties List of PropertyType objects
   * @param level the configuration level on which the list of properties comes from   
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   */
  public void applyDefaultProperties(List properties, String level, int mode) throws ConfigurationMarshallerException;
  
  /**
   * Checks whether the configuration of the <code>behavior</code> object is correct -
   * whether some required properties are missing, whether the properties' values are correct,...
   * Only InterfaceData and BindingData objects are passed as <code>behavior</code> parameter.
   * The implementation must check the OperationDatas configs as well.
   * 
   * @param behavior The Behaviour object to be checked for correctness.
   * @param msgs In this List, a String objects should be added. The string object could contain some warning and(or) error statements.
   *                This list may be visualized, so the strings should be localized. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return true if the configuation is correct, according to this configuration marshaller.
   *         Returns false if the configuration is not correct.
   */
  public boolean checkConfiguration(Behaviour behavior, List msgs, int mode) throws ConfigurationMarshallerException;
  /**
   * Checks whether property is been defined in NW04.
   * 
   * @param prop property qname
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return true if property is defined in NW04, false otherwise.   
   */
  public boolean isNW04Defined(javax.xml.namespace.QName prop, int mode);
  /**
   * Provides default runtime properties mapping for given design-time properties. The <code>level</code> parameter determines from which configuration
   * level the <code>dtPs</code> comes from. The only valid values are <code>INTERFACE_LEVEL</code> and <code>INTERFACE_OPERATION_LEVEL</code>. It is assumed
   * that when this method is invoked with <code>INTERFACE_LEVEL</code>, the returned property list will be applied on binding level. When is invoked with
   * <code>INTERFACE_OPERATION_LEVEL</code>, the result will be applied on binding-operation level.
   * 
   * @param dtPs array of design-time properties, relevant for this configuration marshaller. See #getKnownProperties();
   * @param level determines the configuration level from which the <code>dtPs</code> properties come from. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   *        
   * @return list of PropertyType objects, which represent the default runtime properties mapping of the <code>dtPs</code>. The returned value must not be null.
   */
  public List getDefaultRTConfigForDT(PropertyType[] dtPs, String level, int mode) throws ConfigurationMarshallerException;
  /**
   * Via this method, the configuration marshaller can manage the way a property will look in 
   * the NW04 sap-wsdl. It is possible for a given property, its name to be changed, its value, or even to be ignored, i.e not to be shown in the sap-wsdl.
   * The returned property list will be applied on the same wsdl level as the <code>level</code> parameter. The default property-to-feature mapping
   * algorithm will be used to convert the properties into sap features.
   *
   * @param parent when <code>level</code> is 'binding/operation' or 'interface/operation' the operation's parent InterfaceData or BindingData object is passed.
   *               When it is 'binding' or 'interface' the concrete InterfaceData or BindingData object is passed. 
   * @param ps array of properties, relevant for this configuraiton marshaller. See #getKnownProperties();
   * @param level determines the configuration level from which the <code>ps</code> properties come from. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return list of PropertyType objects, which are about to be shown in the sap-wsdl. The returned value must not be null.
   */
  public List processPropertiesForNW04Features(Behaviour parent, PropertyType[] ps, String level, int mode) throws ConfigurationMarshallerException;
  /**
   * Checks whether the <code>rt</code> configuration matches the requirements and/or restrictions posed by <code>dt</code> configuration, 
   * i.e. whether <code>rt</code> is a valid RT configuration derived from <code>dt</code>. 
   * The implementation must check the OperationDatas configs as well.
   * Note: the <code>rt</code> and <code>dt</code> are configuration object which could contain properties from different domains - security, ws-addressing, ws-rm, ...
   *  
   * @param rt The BindingData object to be checked for correctness against <code>dt</code>.
   * @param dt The InterfaceData behaviour object against which the <code>rt</code> BindingData will be checked.
   * @param msgs In this List, a String objects should be added. The string object could contain some warning and(or) error statements.
   *                This list may be visualized, so the strings should be localized. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return true if the <code>rt</code> is valid against <code>dt</code> configuation, false otherwise.
   */
  public boolean checkRTConfigurationAgainstDT(BindingData rt, InterfaceData dt, List msg, int mode) throws ConfigurationMarshallerException;
  /**
   * Converts the NW04 properties from <code>nw04</code> into corresponding NY properties. The NY properties must be put into
   * <code>ny</code>, since the <code>nw04</code> object is read only.
   * The NW04 properties have been derived from 'sap features' by applying the default feature->properies convertion. In case
   * a feature without properties is preset, a property with namespace equal to the feature's namespace and localname and value equal to '*',
   * is generated.   
   * The method is invoked with InterfaceData and BindingData objects with operations inside. For DT conversion both parameters are InterfaceData,
   * and for RT convertion both are BindingData objects.
   * 
   * @param nw04 InterfaceData or BindingData object containing NW04 properties.
   * @param ny InterfaceData or BindingData object where NY properties must be stored.
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value.
   * @return the modified <code>ny</code> object. 
   */
  public Behaviour convertNW04CfgToNY(Behaviour nw04, Behaviour ny, int mode) throws ConfigurationMarshallerException;
  /**
   * Returns a set of String objects, denoting Variants' names, to which the <code>bData</code> parameter
   * can be applied. The method checks the run-time properties that are known against
   * the known Vairant's design-time properties. Also the operation level configurations must be checked.
   * 
   * @param variants List of Variant objects
   * @param bData BindingData for which valid Variants should be found
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return Set of String objects - the names of the Variants which match the <code>bData</code> object. 
   *
   */
//  public Set findVariantsForBindingData(List variants, BindingData bData, int mode) throws ConfigurationMarshallerException;
  /**
   * Via this method, the configuration marshaller can manage the way a property will look in 
   * the NW04 sap-wsdl. It is possible for a given property, its name to be changed, its value, or even to be ignored, i.e not to be shown in the sap-wsdl.
   * The returned property list will be applied on the same wsdl level as the <code>level</code> parameter. The default property-to-feature mapping
   * algorithm will be used to convert the properties into sap features.
   * 
   * @param ps array of properties, relevant for this configuraiton marshaller. See #getKnownProperties();
   * @param level determines the configuration level from which the <code>ps</code> properties come from. 
   * @param mode determines in which mode the method should be invoked. Takes either <code>CONSUMER_MODE</code> or <code>PROVIDER_MODE</code> value. 
   * @return list of PropertyType objects, which are about to be shown in the sap-wsdl. The returned value must not be null.
   * @deprecated
   */
  //public List processPropertiesForNW04Features(PropertyType[] ps, String level, int mode) throws ConfigurationMarshallerException;
}
