package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

  public static final String MTOM_ENABLED = "{" + PublicProperties.TRANSPORT_BINDING_FEATURE + "}" + PublicProperties.TRANSPORT_BINDING_OPTIMIZED_XML;
  
  private HashSet roles;
  private boolean mtomEnabled;
  private SOAPFactory soapFactory;
  private MessageFactory messageFactory;
  private ClientConfigurationContext clientCfgCtx;
  
  protected SOAPBindingImpl(ClientConfigurationContext clientCfgCtx) {
    super();
    this.clientCfgCtx = clientCfgCtx;
    try {
      String soapVersion = clientCfgCtx.getStaticContext().getInterfaceData().getProperty(InterfaceMapping.SOAP_VERSION);
      roles = new HashSet();
      if(soapVersion.equals(InterfaceMapping.SOAP_VERSION_11)) {
        init_SOAP11(false);
      } else if(soapVersion.equals(InterfaceMapping.SOAP_VERSION_12)) {
        init_SOAP12(false);
      } else if(soapVersion.equals(InterfaceMapping.SOAP_VERSION_11_MTOM)) {
        init_SOAP11(true);
      } else if(soapVersion.equals(InterfaceMapping.SOAP_VERSION_11_MTOM)) {
        init_SOAP12(true);
      }
    } catch(Throwable thr) {
      throw new WebServiceException(thr);
    }
  }
  
  private void init_SOAP11(boolean isMTOMEnabled) throws SOAPException {
    initRoles_SOAP11();
    messageFactory = MessageConvertor.SOAP11Factory;
    soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    setMTOMEnabled(isMTOMEnabled);
  }
  
  private void init_SOAP12(boolean isMTOMEnabled) throws SOAPException {
    initRoles_SOAP12();
    messageFactory = MessageConvertor.SOAP12Factory;
    soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    setMTOMEnabled(isMTOMEnabled);
  }
  
  private void initRoles_SOAP12() {
    roles.add(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
    roles.add(SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER);
  }
  
  private void initRoles_SOAP11() {
    roles.add(SOAPConstants.URI_SOAP_ACTOR_NEXT);
  }
  
  public Set getRoles() {
    return roles;
  }
  
  public void setRoles(Set clientRoles) {
    if (clientRoles.contains(SOAPConstants.URI_SOAP_1_2_ROLE_NONE)) {
      throw new WebServiceException(SOAPConstants.URI_SOAP_1_2_ROLE_NONE + " role is prohibited!");
    }
    this.roles.addAll(clientRoles);
  }
  
  public void setMTOMEnabled(boolean mtomEnabled) {
    this.mtomEnabled = mtomEnabled;
    ConfigurationContext persistableCtx = clientCfgCtx.getPersistableContext();
    if(mtomEnabled) {
      persistableCtx.setProperty(MTOM_ENABLED, PublicProperties.TRANSPORT_BINDING_OPTXML_MTOM);
    } else {
      persistableCtx.removeProperty(MTOM_ENABLED);
    }
  }
  
  public boolean isMTOMEnabled() {
    return(mtomEnabled);
  }
  
  public SOAPFactory getSOAPFactory() {
    return(soapFactory);
  }
  
  public MessageFactory getMessageFactory() {
    return(messageFactory);
  }
}
