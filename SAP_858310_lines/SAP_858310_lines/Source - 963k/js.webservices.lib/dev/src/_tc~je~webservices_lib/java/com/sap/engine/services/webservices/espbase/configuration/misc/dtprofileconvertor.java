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
package com.sap.engine.services.webservices.espbase.configuration.misc;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.IConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.DefaultConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
/**
 * Converts DT profiles into standard properties.
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Sep 26, 2006
 */
public class DTProfileConvertor {
  
  public static final String COMMUNICATION_PROFILE_ELEM = "communicationProfile";
  public static final String IS_SYNCHRON_ELEM = "isSynchron";
  public static final String SECURITY_PROFILE_ELEM = "securityProfile";
  public static final String RUNTIMEVERSION_PROFILE_ELEM = "runtimeVersion";
  public static final String URI_ATTRIB = "uri";
  public static final String IS_IDEMPOTENT_ELEM = "isIdempotent";
  
  //operation communicationProfile 
  private static final String SYNCHRONOUS_PROFILE = "PRF_DT_OP_COM_SYNC";
  private static final String ASYNCHRONOUS_PROFILE = "PRF_DT_OP_COM_ASYNC";
  private static final String SYNCHRONOUS_COMMIT_LABEL_PROFILE = "PRF_DT_OP_COM_SYNC_COMMIT";
  private static final String SYNCHRONOUS_ROLLBACK_LABEL_PROFILE = "PRF_DT_OP_COM_SYNC_ROLLBACK";
  private static final String TUCC_CONFIRM_PROFILE = "PRF_DT_OP_COM_TUCC_CONFIRM";
  private static final String TUCC_COMPENSATE_PROFILE = "PRF_DT_OP_COM_TUCC_COMPENSATE";
  private static final String TUCC_TENTATIVE_UPDATE_PROFILE = "PRF_DT_OP_COM_TUCC_TENTATIVE";
  private static final String COMMIT_HANDLING_F = "http://www.sap.com/NW05/soap/features/commit/";
  private static final String BLOCKING_F = "http://www.sap.com/NW05/soap/features/blocking/";
  private static final String TRANSACTION_HANDLING_F = "http://www.sap.com/NW05/soap/features/transaction/";
  private static final String RELIABLE_MSGING_F = "http://www.sap.com/NW05/soap/features/wsrm/";
  private static final String RELIABLE_MSGING_F_OLD = "http://www.sap.com/710/soap/features/reliableMessaging/";
  //securityProfile 
  private static final String NOSECURITY_PROFILE = "PRF_DT_IF_SEC_NO";
  private static final String LOWSECURITY_PROFILE = "PRF_DT_IF_SEC_LOW";
  private static final String MEDIUMSECURITY_PROFILE = "PRF_DT_IF_SEC_MEDIUM";
  private static final String HIGHSECURITY_PROFILE = "PRF_DT_IF_SEC_HIGH";
  private static final String AUTHENTICATION_F = "http://www.sap.com/webas/630/soap/features/authentication/";
  private static final String TRANSPORTGUARANTEE_F = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
  //interface communicationProfile
  private static final String STATEFUL_PROFILE = "PRF_DT_IF_COM_STATEFUL";
  private static final String STATELESS_PROFILE = "PRF_DT_IF_COM_STATELESS";
  private static final String TUCC_PROFILE = "PRF_DT_IF_COM_TUCC";
  private static final String SESSION_F = "http://www.sap.com/webas/630/soap/features/session/";
  private static final String MESSAGEID_F = "http://www.sap.com/webas/640/soap/features/messageId/";
  //RuntimeVersion profile
//  private static final String RUNTIMEVERSION_XI = "urn:sap-com:soap:xms:application:xip";
  
  //security properties
  private static final String AUTHENTICATION_AUTHENTICATIONLEVEL = "AuthenticationLevel";
  private static final String AUTHENTICATION_AUTHENTICATIONMETHOD = "AuthenticationMethod";
  private static final String TRANSPORTGUARANTEE_LEVEL = "Level";
  private static final String TRANSPORTGUARANTEE_TLSTYPE = "TLSType";
  
  //WSAddressing feature
  private static final String WSADDRESSING_F = "http://www.sap.com/710/soap/features/WSAddressing/";
  private static final String WSADDRESSING_ENABLED = "enabled";
  private static final String WSADDRESSING_WSAPROTOCOL = "WSAProtocol";
  
  public static final String DTPROFILE_NS = "urn:com-sap:ifr:v2:wsdl";
  /**
   * Reads profile data from <code>propElem</code>, converts it and applies it on <code>intfData</code>.
   * @param intrfData
   * @param propertiesElem
   * @param mode IConfigurationMarshaller.CONSUMER_MODE or IConfigurationMarshaller.PROVIDER_MODE
   */
  public static void applyDTProfiles(InterfaceData intfData, BindingData bd, Element propElem, int mode) {
    if (! (DTPROFILE_NS.equals(propElem.getNamespaceURI()) && "properties".equals(propElem.getLocalName())) ) {
      throw new IllegalArgumentException("The DOM element param does not have ns equal to '" + DTPROFILE_NS + "' and localname equal to 'properties'");
    }
    //detach the propElem from the three
    propElem.getParentNode().removeChild(propElem);
    
    List runtimeVersionElements = DOM.getChildElementsByTagNameNS(propElem, DTPROFILE_NS, RUNTIMEVERSION_PROFILE_ELEM);
    if (runtimeVersionElements.size() != 0) {
      Element runtimeVersionElement = (Element)(runtimeVersionElements.get(0));
      String uriAttrValue = runtimeVersionElement.getAttribute(URI_ATTRIB);
      if (XIFrameworkConstants.XI_CLIENT_RUNTIME_VERSION_URI.equals(uriAttrValue) || XIFrameworkConstants.XI_SERVER_RUNTIME_VERSION_URI.equals(uriAttrValue)){
       if(/*XIFrameworkConstants.XI_CLIENT_RUNTIME_VERSION_URI.equals(uriAttrValue)*/ mode == IConfigurationMarshaller.CONSUMER_MODE) {
        PublicProperties.setProperty(PublicProperties.XI_RUNTIME_ENVIRONMENT_RT_PROP_NAME, PublicProperties.XI_RUNTIME_ENVIRONMENT_XI_RT_PROP_VALUE, bd);
        PublicProperties.setProperty(PublicProperties.XI_XI_TRANSPORT_IS_POSSIBLE_DT_PROP_NAME, PublicProperties.BOOLEAN_TRUE_VALUE, intfData);
        PublicProperties.setProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME, BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE, intfData);
       } else if(/*XIFrameworkConstants.XI_SERVER_RUNTIME_VERSION_URI.equals(uriAttrValue)*/ mode == IConfigurationMarshaller.PROVIDER_MODE) {
        PublicProperties.setProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME, BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_XI_VALUE, intfData);
        return;
       }
      }
    }
    List intfCommProfs = DOM.getChildElementsByTagNameNS(propElem, DTPROFILE_NS, COMMUNICATION_PROFILE_ELEM);
    if (intfCommProfs.size() == 1) { //there is communicationProfile
      Element intfCommProf = (Element) intfCommProfs.get(0);
      List props = convertInterfaceCommunicationProfile(intfCommProf);
      intfData.getSinglePropertyList().overwriteProperties(props);
    }
    List secProfiles = DOM.getChildElementsByTagNameNS(propElem, DTPROFILE_NS, SECURITY_PROFILE_ELEM);
    if (secProfiles.size() == 1) {
      Element secProf = (Element) secProfiles.get(0);
      List props = convertSecurityProfile(secProf);
      intfData.getSinglePropertyList().overwriteProperties(props);
    } 
    List methods = DOM.getChildElementsByTagNameNS(propElem, DTPROFILE_NS, "methods");
    if (methods.size() == 1) {
      Element methodsEl = (Element) methods.get(0);
      List ms = DOM.getChildElementsByTagNameNS(methodsEl, DTPROFILE_NS, "method");
      for(int i = 0; i < ms.size(); i++) {
        Element methodEl = (Element) ms.get(i);
          String opName = methodEl.getAttribute("name");
          OperationData opData = intfData.getOperationData(opName);
          if (opData != null) {
          
          List commProfs = DOM.getChildElementsByTagNameNS(methodEl, DTPROFILE_NS, COMMUNICATION_PROFILE_ELEM);
          if (commProfs.size() == 1) {
            Element commProf = (Element) commProfs.get(0);
            List list = convertOperationCommunicationProfile(commProf);
            opData.getSinglePropertyList().overwriteProperties(list);
          }
          
          if(PublicProperties.XI_RUNTIME_ENVIRONMENT_XI_RT_PROP_VALUE.equals(PublicProperties.getProperty(PublicProperties.XI_RUNTIME_ENVIRONMENT_RT_PROP_NAME, bd))) {
            List<Element> isSynchronElems = DOM.getChildElementsByTagNameNS(methodEl, DTPROFILE_NS, IS_SYNCHRON_ELEM);
            String isSynchronValue = PublicProperties.BOOLEAN_TRUE_VALUE;
            if(isSynchronElems.size() > 0) {
              Element isSynchronElem = isSynchronElems.get(0);
              isSynchronValue = isSynchronElem.getFirstChild().getNodeValue().trim();
            }
            PublicProperties.setProperty(PublicProperties.XI_IS_SYNC_DT_PROP_NAME, isSynchronValue, opData);
          } 
          
          //apply idempotency if available
          List<Element> isIdempotentElems = DOM.getChildElementsByTagNameNS(methodEl, DTPROFILE_NS, IS_IDEMPOTENT_ELEM);
          if (isIdempotentElems.size() == 1) {
            Element isIdempotent = isIdempotentElems.get(0);
            if (isIdempotent.getFirstChild() != null) {
              String isIdempotentValue = isIdempotent.getFirstChild().getNodeValue().trim();
              if ("true".equalsIgnoreCase(isIdempotentValue)) {
                opData.getSinglePropertyList().overwritePropertyValue(RMConfigurationMarshaller.NS_IDEMPOTENCY
                                                                      , RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY
                                                                      , RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY_DEFAULT_VALUE);
              }else{
                opData.getSinglePropertyList().overwritePropertyValue(RMConfigurationMarshaller.NS_IDEMPOTENCY
                    , RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY
                    , RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY_OFF);               
              }
              
            }
          }
          
        }
      }
    }
    
    applyRTPropertiesBasedOnDT(intfData, bd, mode);
  }
  
  private static List<PropertyType> convertInterfaceCommunicationProfile(Element elem) {
    ArrayList<PropertyType> res = new ArrayList();
    String uri = elem.getAttribute(URI_ATTRIB);
    if (STATELESS_PROFILE.equalsIgnoreCase(uri) || TUCC_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType session = PropertyType.newInitializedInstance(SESSION_F, "enableSession", "false");
      PropertyType msgID = PropertyType.newInitializedInstance(MESSAGEID_F, "enableMessageId", "true");
      res.add(session);
      res.add(msgID);
    } if (STATEFUL_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType session = PropertyType.newInitializedInstance(SESSION_F, "enableSession", "true");
      PropertyType msgID = PropertyType.newInitializedInstance(MESSAGEID_F, "enableMessageId", "true");
      res.add(session);
      res.add(msgID);
    }
    return res;
  }
  
  private static List<PropertyType> convertSecurityProfile(Element elem) {
    ArrayList<PropertyType> res = new ArrayList();
    String uri = elem.getAttribute(URI_ATTRIB);
    if (NOSECURITY_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType authentication = PropertyType.newInitializedInstance(AUTHENTICATION_F, "AuthenticationLevel", "None");
      PropertyType transportGuarantee = PropertyType.newInitializedInstance(TRANSPORTGUARANTEE_F, "Level", "None");
      res.add(authentication);
      res.add(transportGuarantee);
    } else if (LOWSECURITY_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType authentication = PropertyType.newInitializedInstance(AUTHENTICATION_F, "AuthenticationLevel", "Basic");
      PropertyType transportGuarantee = PropertyType.newInitializedInstance(TRANSPORTGUARANTEE_F, "Level", "None");
      res.add(authentication);
      res.add(transportGuarantee);
    } else if (MEDIUMSECURITY_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType authentication = PropertyType.newInitializedInstance(AUTHENTICATION_F, "AuthenticationLevel", "Basic");
      PropertyType transportGuarantee = PropertyType.newInitializedInstance(TRANSPORTGUARANTEE_F, "Level", "Both");
      res.add(authentication);
      res.add(transportGuarantee);
    } else if (HIGHSECURITY_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType authentication = PropertyType.newInitializedInstance(AUTHENTICATION_F, "AuthenticationLevel", "Strong");
      PropertyType transportGuarantee = PropertyType.newInitializedInstance(TRANSPORTGUARANTEE_F, "Level", "Both");
      res.add(authentication);
      res.add(transportGuarantee);
    }
    return res;
  }
  
  private static List<PropertyType> convertOperationCommunicationProfile(Element elem) {
    ArrayList<PropertyType> res = new ArrayList();
    String uri = elem.getAttribute(URI_ATTRIB);
    if (SYNCHRONOUS_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "false");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "true");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "no");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "false");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if(ASYNCHRONOUS_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "true");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "false");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "yes");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "true");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if (SYNCHRONOUS_COMMIT_LABEL_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "true");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "true");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "no");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "false");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if (SYNCHRONOUS_ROLLBACK_LABEL_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "false");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "true");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "no");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "false");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if (TUCC_CONFIRM_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "true");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "false");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "yes");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "true");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if (TUCC_COMPENSATE_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "true");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "false");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "yes");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "true");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    } else if (TUCC_TENTATIVE_UPDATE_PROFILE.equalsIgnoreCase(uri)) {
      PropertyType commHandling = PropertyType.newInitializedInstance(COMMIT_HANDLING_F, "enableCommit", "true");
      PropertyType blocking = PropertyType.newInitializedInstance(BLOCKING_F, "enableBlocking", "true");
      PropertyType txHandling = PropertyType.newInitializedInstance(TRANSACTION_HANDLING_F, "required", "no");
      PropertyType rm = PropertyType.newInitializedInstance(RELIABLE_MSGING_F, "enableWSRM", "false");
      //miss the RIF
      res.add(commHandling);
      res.add(blocking);
      res.add(txHandling);
      res.add(rm);
    }
    return res;
  }
  
  private static void applyRTPropertiesBasedOnDT(InterfaceData intfData, BindingData bd, int mode) {
    PropertyListType res = bd.getSinglePropertyList();
    PropertyListType pList = intfData.getSinglePropertyList();
    PropertyListType intfList = intfData.getSinglePropertyList();
    //check Session@enable props
    String value = pList.getPropertyValue(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_ENABLESESSION_PROP);
    if ("true".equals(value)) {
      res.overwritePropertyValue(DefaultConfigurationMarshaller.SESSION_NS, DefaultConfigurationMarshaller.SESSION_SESSIONMETHOD_PROP, DefaultConfigurationMarshaller.HTTPCOOKIES_VALUE);
    }
//    //check MessageID@enable
//    value = pList.getPropertyValue(DefaultConfigurationMarshaller.MESSAGEID_NS, DefaultConfigurationMarshaller.MESSAGEID_ENABLEMESSAGEID_PROP);
//    if ("true".equals(value)) {
//      res.overwritePropertyValue(DefaultConfigurationMarshaller.MESSAGEID_NS, DefaultConfigurationMarshaller.MESSAGEID_MESSAGEIDPROTOCOL_PROP, DefaultConfigurationMarshaller.ADDRESSING_VALUE);
//    }
    //check Authentication@AuthenticationLevel
    value = pList.getPropertyValue(AUTHENTICATION_F, AUTHENTICATION_AUTHENTICATIONLEVEL);
    if ("None".equals(value)) {
      res.overwritePropertyValue(AUTHENTICATION_F, AUTHENTICATION_AUTHENTICATIONMETHOD, "sapsp:None");
    } else if ("Basic".equals(value)) {
      res.overwritePropertyValue(AUTHENTICATION_F, AUTHENTICATION_AUTHENTICATIONMETHOD, "sapsp:HTTPBasic");
    } else if ("Strong".equals(value)) {
      res.overwritePropertyValue(AUTHENTICATION_F, AUTHENTICATION_AUTHENTICATIONMETHOD, "sapsp:HTTPX509");
    }
    //check TransportGuarantee@Level
    value = pList.getPropertyValue(TRANSPORTGUARANTEE_F, TRANSPORTGUARANTEE_LEVEL);
    if ("None".equals(value)) {
      res.overwritePropertyValue(TRANSPORTGUARANTEE_F, TRANSPORTGUARANTEE_TLSTYPE, "sapsp:HTTP");
    } else if ("Both".equals(value)) {
      res.overwritePropertyValue(TRANSPORTGUARANTEE_F, TRANSPORTGUARANTEE_TLSTYPE, "sapsp:HTTPS");
    }
    
    //check whether the WS-RM is enabled
    boolean isRMEnabled = false;
    OperationData[] ops = intfData.getOperation();
    for (OperationData data : ops) {
      pList = data.getSinglePropertyList();
      if ("true".equals(pList.getPropertyValue(RELIABLE_MSGING_F, "enableWSRM"))) {
        isRMEnabled = true;
      }
    }
    //apply SOAPApplication property
    if (isRMEnabled) {
      //set SOAPApplication
      String soapApp;
      if (IConfigurationMarshaller.PROVIDER_MODE == mode) {
        soapApp = BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_EXTENDED_VALUE;
      } else {
        soapApp = BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_EXTENDED_VALUE;
      }
      intfList.overwritePropertyValue(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI(), BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart(),
          soapApp);
      //enable addressing
      intfList.overwritePropertyValue(WSADDRESSING_F, WSADDRESSING_ENABLED, "true");
      res.overwritePropertyValue(WSADDRESSING_F, WSADDRESSING_WSAPROTOCOL, "http://www.w3.org/2005/03/addressing");
      //set WSRM protocol
      res.overwritePropertyValue(RELIABLE_MSGING_F_OLD, "RMprotocol", "http://schemas.xmlsoap.org/ws/2005/02/rm");
    } else if (intfList.getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME) == null) { //if no SOAPAPPLICATION is available
      intfList.overwritePropertyValue(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI(), BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart(),
          BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_DEFAULT_VALUE);
    }
  }
}
