/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration.cfg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ConfigurationException;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.p_set.FeatureType;
import com.sap.engine.services.webservices.espbase.configuration.p_set.PropertySetFactory;
import com.sap.engine.services.webservices.espbase.configuration.p_set.PropertySetType;
import com.sap.engine.services.webservices.espbase.configuration.p_set.SubjectType;
import com.sap.engine.services.webservices.espbase.configuration.soap_app.DependencyListType;
import com.sap.engine.services.webservices.espbase.configuration.soap_app.PType;
import com.sap.engine.services.webservices.espbase.configuration.soap_app.SoapApplicationFactory;
import com.sap.engine.services.webservices.espbase.configuration.soap_app.SoapApplicationType;

/**
 * Provides methods for obtaining SoapApplication and Relation intialized structures
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, May 29, 2006
 */
public class SoapApplicationRegistry {  
  private static final String PROPERTY_SET_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/property_set.xml";
  
  private static final String CLIENT_DEFAULT_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/client_default_soapapp.xml";  
  private static final String CLIENT_EXTENDED_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/client_extended_soapapp.xml";  
  private static final String CLIENT_JAXWS_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/client_jaxws_default_soapapp.xml";  
  private static final String SERVICE_JAXWS_DEFAULT_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/service_jaxws_default_soapapp.xml";  
  private static final String SERVICE_JAXWS_HANDLER_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/service_jaxws_handlers_soapapp.xml";
  private static final String SERVICE_DEFAULT_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/service_default_soapapp.xml";  
  private static final String SERVICE_EXTENDED_SOAPAPP_PATH = "/com/sap/engine/services/webservices/espbase/configuration/cfg/service_extended_soapapp.xml";
  
  private static final Hashtable<String, String> SOAP_APPLICATION_PATH_TABLE = new Hashtable(); //key: SoapApplicationID, value: path to the soap_application.xml  

  private static PropertySetType property_set = null;
  private static Hashtable<String, SoapApplicationType> soap_application_table = new Hashtable();  //key: SoapAppID
  private static Hashtable<String, List<SubjectType>> subject_table = new Hashtable();  //key: SoapAppID
  private static Hashtable<String, Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType>> soap_application_propMap = new Hashtable();  //key: SoapAppID
  
  static {
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE, CLIENT_EXTENDED_SOAPAPP_PATH);
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_DEFAULT_VALUE, CLIENT_DEFAULT_SOAPAPP_PATH);
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_JAXWS_VALUE, CLIENT_JAXWS_SOAPAPP_PATH);
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_DEFAULT_VALUE, SERVICE_JAXWS_DEFAULT_SOAPAPP_PATH);
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_HANDLERS_VALUE, SERVICE_JAXWS_HANDLER_SOAPAPP_PATH);    
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_DEFAULT_VALUE, SERVICE_DEFAULT_SOAPAPP_PATH);    
    SOAP_APPLICATION_PATH_TABLE.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_EXTENDED_VALUE, SERVICE_EXTENDED_SOAPAPP_PATH);    
  }
  /**
   * Returns a Map of all used properties in SOAPApplication with id <code>soapAppID</code>.
   */
  public static Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType> getSoapApplicationPropMap(String soapAppID) throws ConfigurationException {
    synchronized (soap_application_propMap) {
      Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType> res = soap_application_propMap.get(soapAppID);
      if (res == null) {
        SoapApplicationType soapAppType = getSoapApplication(soapAppID);
        res = createSoapApplicationPropMap(soapAppType);
        soap_application_propMap.put(soapAppID, Collections.unmodifiableMap(res)); //the map should not change
      }
      return res;
    }
  }
  /**
   * Returns a list of SubjectType objects relevant for SOAPApplication with id <code>soapAppID</code>.
   */
  public static List<SubjectType> getSoapAppSubjects(String soapAppID) throws ConfigurationException {
    synchronized (subject_table) {
      List<SubjectType> res = subject_table.get(soapAppID);
      if (res == null) {
        SoapApplicationType soapAppType = getSoapApplication(soapAppID);
        res = createSubjectSetForSoapApp(soapAppType);
        subject_table.put(soapAppID, Collections.unmodifiableList(res)); //the List should not change
      }
      return res;
    }
  }
  /**
   * Returns DependencyListType object relevant for SOAPApplication with id <code>soapAppID</code>.
   */
  public static DependencyListType getSoapAppDependencies(String soapAppID) throws ConfigurationException {
    SoapApplicationType soapAppType = getSoapApplication(soapAppID);
    return soapAppType.getDependencies();
  }
  /**
   * Extracts and returns SOAPApplication id for the specified <code>intfDef</code>
   * @param isConsumer true denotes consumer, false provider
   */
  public static String getSoapApplicationID(InterfaceDefinition intfDef, boolean isConsumer) throws ConfigurationException {
    InterfaceData intfData = getInterfaceData(intfDef);
    PropertyListType propList = intfData.getSinglePropertyList();
    PropertyType soapAppProp = propList.getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME);
    
    String soapAppID;
    if (soapAppProp == null) {
      //in case of missing soap_application property, use the default values
      if (isConsumer) { 
        soapAppID = BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_DEFAULT_VALUE;
      } else {
        soapAppID = BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_DEFAULT_VALUE;
      }
    } else {
      soapAppID = soapAppProp.get_value();
    }
    return soapAppID;
  }
  /**
   * Based on the properties inside the InterfaceData a SoapApplicationID is returned. If the InterfaceData already contains SOAPApplication property its
   * value is returned.
   * @param intfDef an InterfaceDefinition object containing single Variant with single InterfaceData
   * @param isConsumer denotes whether the InterfaceDefinition is for consumer or provider ws proxy.
   * @return 
   * @throws ConfigurationException
   */
  public static String calculateSoapApplicationID(InterfaceDefinition intfDef, boolean isConsumer) throws ConfigurationException {
    InterfaceData intfData = getInterfaceData(intfDef);
    PropertyListType propList = intfData.getSinglePropertyList();
    
    //in case the InterfaceData contains SOAPApplication property return its value.
    PropertyType soapAppProp = propList.getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME);
    if (soapAppProp != null) {
      return soapAppProp.get_value();
    }
    
    //check whether the WS-RM is enabled
    boolean isRMEnabled = false;
    OperationData[] ops = intfData.getOperation();
    for (OperationData data : ops) {
      PropertyListType pList = data.getSinglePropertyList();
      if ("true".equals(pList.getPropertyValue(RMConfigurationMarshaller.NS_RM_FEATURE_NEW, "enableWSRM"))) {
        isRMEnabled = true;
      }
    }
    
    if (isRMEnabled) {
      if (isConsumer) {
        return BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE;
      } else {
        return BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_EXTENDED_VALUE;
      }
    } else {
      if (isConsumer) {
        return BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_DEFAULT_VALUE;
      } else {
        return BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_DEFAULT_VALUE;
      }
    }
  }
  /**
   * Based on the properties inside the InterfaceData a SoapApplicationID is determined and SOAPApplication property is applied. 
   * If the InterfaceData already contains SOAPApplication property its value is preserved. 
   * @param intfDef an InterfaceDefinition object containing single Variant with single InterfaceData
   * @param isConsumer denotes whether the InterfaceDefinition is for consumer or provider ws proxy.
   * @return the applied SoapApplicationID.
   * @throws ConfigurationException
   */
  public static String applySoapApplicationProperty(InterfaceDefinition intfDef, boolean isConsumer) throws ConfigurationException {
    InterfaceData intfData = getInterfaceData(intfDef);
    PropertyListType propList = intfData.getSinglePropertyList();

    String soapAppID = calculateSoapApplicationID(intfDef, isConsumer);
    
    propList.overwritePropertyValue(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI()
                                    , BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart()
                                    , soapAppID);
    return soapAppID;
  }
  /**
   * 
   * @param soapApp
   * @return
   * @throws ConfigurationException
   */
  private static List<SubjectType> createSubjectSetForSoapApp(SoapApplicationType soapApp) throws ConfigurationException {    
    List<SubjectType> res = new ArrayList();
    
    PType[] soapAppProps = soapApp.getProperties().getProperty();
    boolean used[] = new boolean[soapAppProps.length];
    SubjectType subjects[] = getPropertySet().getSubject();
    
    for (SubjectType subject: subjects) {
      SubjectType newS = subject.shadowCopy();
      boolean propAdded = false;
      for (int i = 0; i < soapAppProps.length; i++) {
        PType prop = soapAppProps[i];
        String propNS = prop.getNamespace();
        //obtain feature
        FeatureType feature = subject.getFeatureByNS(propNS);
        if (feature != null) {
          //obtain property
          String propName = prop.getName();
          com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType fullProp = feature.getPropertyByName(propName);
          if (fullProp != null) {
            FeatureType newFeature = newS.getFeatureByNS(propNS);
            if (newFeature == null) {
              newFeature = feature.shadowCopy(); 
              newS.addFeature(newFeature);
            }
            newFeature.addProperty(fullProp);
            used[i] = true;
            propAdded = true;
          }
        }
      }
      if (propAdded) {
        res.add(newS);
      }
    }
    //check whether all of the soapapplication properties are contained in the property_set
    StringBuffer excMsg = new StringBuffer();
    for (int i = 0; i < used.length; i++) {
      if (! used[i]) {
        excMsg.append("Property '" + soapAppProps[i] + "' does not have corresponding description in the PropertySet.xml.").append("\r\n");
      }
    }
    if (excMsg.length() > 0) {
      throw new ConfigurationException(excMsg.toString());
    }
    
    return res;
  }
  
  private static Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType> createSoapApplicationPropMap(SoapApplicationType soapApp) throws ConfigurationException {
    Map<QName, com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType> res = new Hashtable();
    
    PType[] soapAppProps = soapApp.getProperties().getProperty();  
    boolean used[] = new boolean[soapAppProps.length];
    SubjectType subjects[] = getPropertySet().getSubject();
    
    for (SubjectType subject: subjects) {
      for (int i = 0; i < soapAppProps.length; i++) {
        PType prop = soapAppProps[i];
        String propNS = prop.getNamespace();
        //obtain feature
        FeatureType feature = subject.getFeatureByNS(propNS);
        if (feature != null) {
          //obtain property
          String propName = prop.getName();
          com.sap.engine.services.webservices.espbase.configuration.p_set.PropertyType fullProp = feature.getPropertyByName(propName);
          if (fullProp != null) {
            res.put(new QName(propNS, propName), fullProp);
            used[i] = true;
          }
        }
      }
    }
    //check whether all of the soapapplication properties are contained in the property_set
    StringBuffer excMsg = new StringBuffer();
    for (int i = 0; i < used.length; i++) {
      if (! used[i]) {
        excMsg.append("Property '" + soapAppProps[i] + "' does not have corresponding description in the PropertySet.xml.").append("\r\n");
      }
    }
    if (excMsg.length() > 0) {
      throw new ConfigurationException(excMsg.toString());
    }

    return res;
  }
  
  private static synchronized SoapApplicationType getSoapApplication(String soapApplicationID) throws ConfigurationException {
    SoapApplicationType soapAppType = soap_application_table.get(soapApplicationID);
    if (soapAppType == null) {
      String soapAppXmlPath = SOAP_APPLICATION_PATH_TABLE.get(soapApplicationID);
      if (soapAppXmlPath == null) {
        throw new ConfigurationException("SoapApplication with id '" + soapApplicationID + "' is not recorgnized.");
      }
      //load soap_application.xml
      try {
        InputStream in = SoapApplicationRegistry.class.getResourceAsStream(soapAppXmlPath); 
        try {
          if (in != null) {
            SoapApplicationType soapApp = SoapApplicationFactory.load(in);
            soap_application_table.put(soapApplicationID, soapApp);
            soapAppType = soapApp;
          } else {
            throw new RuntimeException("Unable to load resource '" + soapAppXmlPath + "'");
          }
        } finally {
          if (in != null) {
            in.close();
          }
        }
      } catch (Exception e) {
        throw new ConfigurationException(e);
      }

    }
    return soapAppType;
  }
  
  private static InterfaceData getInterfaceData(InterfaceDefinition intfDef) throws ConfigurationException {
    Variant[] vv = intfDef.getVariant();
    if (vv.length == 1) {
      return vv[0].getInterfaceData();   
    }
    throw new ConfigurationException("Only InterfaceDefinition with single variant is supported. Found '" + vv.length + "'");
  }
  
  private static synchronized PropertySetType getPropertySet() throws ConfigurationException {
    if (property_set != null) {
      return property_set;
    }
    try {
      //loading Property_Set
      InputStream in = SoapApplicationRegistry.class.getResourceAsStream(PROPERTY_SET_PATH);
      try {
        if (in != null) {
          property_set = PropertySetFactory.load(in);
          return property_set;
        } else {
          throw new ConfigurationException("Unable to load resource '" + PROPERTY_SET_PATH + "'");
        }
      } finally {
        if (in != null) {
          in.close();
        }
      }
    } catch (Exception e) {
      if (e instanceof ConfigurationException) {
        throw (ConfigurationException) e;
      }
      throw new ConfigurationException("Unable to load resource '" + PROPERTY_SET_PATH + "'", e);
    }
  }
  
//  public static void main(String[] args) throws Exception {
////    ConfigurationRoot root = ConfigurationFactory.load("D:/projects/work/configuration_wsdl_visualization_and_packaging/CentralizedConfigurationFramework/configurations.xml");
////    InterfaceDefinition[] defs = root.getDTConfig().getInterfaceDefinition();
//    
//    List<SubjectType> s = SoapApplicationRegistry.getSoapAppSubjects("URN:SAP-COM:SOAP:RUNTIME:APPLICATION:CLIENT:EXTENDED");
//    System.out.println(s);
//    
//    PropertySetType soapAppPropSet = new PropertySetType();
//    soapAppPropSet.setName("tmpName");
//    
//    SubjectType ss[] = s.toArray(new SubjectType[s.size()]);
//    soapAppPropSet.setSubject(ss);
//    
//    PropertySetFactory.save(soapAppPropSet, System.out);
//  }
}
