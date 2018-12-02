package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.IOException;
import java.rmi.RemoteException;

import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.services.webservices.espbase.client.api.SessionInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.messaging.ConsumerMessagePool;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;

/**
 * API for controlling the session handling.
 * @author I024072
 *
 */
public class SessionInterfaceNYImpl implements SessionInterface {
  
  private ClientConfigurationContext clientContext;
  
  public SessionInterfaceNYImpl(ClientConfigurationContext clientContext) {
    this.clientContext = clientContext;
  }
  
  public boolean isMaintainSession() {
    Object httpMaintainSession = clientContext.getPersistableContext().getProperty(PublicProperties.C_SESSION_MAINTAIN_STRING);
    Object httpCookies = clientContext.getDynamicContext().getProperty(PublicProperties.F_SESSION_COOKIE);    
    return(PublicProperties.F_SESSION_MAINTAIN_TRUE.equals(httpMaintainSession) && httpCookies != null);
  }

  public void closeSession() {
    clientContext.getPersistableContext().setProperty(PublicProperties.C_SESSION_MAINTAIN_STRING, PublicProperties.F_SESSION_MAINTAIN_FALSE);
  }

  /**
   * SAP Specific method which is used to release server resourses that are associated with the HTTP Session.
   */
  public void releaseServerResources() throws RemoteException {    
    Object httpCookies = clientContext.getDynamicContext().getProperty(PublicProperties.F_SESSION_COOKIE);    
    if (httpCookies == null) {
      // These is no cookie so leave the call      
      return;
    }
    clientContext.getPersistableContext().setProperty(PublicProperties.F_SESSION_FLUSH, PublicProperties.F_SESSION_FLUSH_TRUE);
    closeSession();
    // TODO: Clear the protocol order index. This is a hack the protocol order should be cleared by the transport binding !
    clientContext.getPersistableContext().removeProperty(SOAPTransportBinding.PROTOCOL_INDEX);
    initMessage();
    clientContext.getTransportBinding().sendMessage(clientContext);
    releaseMessage();
  }
  
  private void initMessage() throws RemoteException {
    createMessageIfNeeded();
    String msgNs = initMessageWriteMode();
    initMessageBody(msgNs);
  }
          
  private void createMessageIfNeeded() {
    MIMEMessage mimeMsg = (MIMEMessage)(clientContext.getMessage());
    if (mimeMsg == null) {
      mimeMsg = ConsumerMessagePool.getMIMEMessage();
      clientContext.setProperty(ClientConfigurationContextImpl.MESSAGE, mimeMsg);
    }
  }
  
  private String initMessageWriteMode() {
    MIMEMessage mimeMsg = (MIMEMessage)(clientContext.getMessage());
    String msgNs = isSOAP12Mode() ? SOAPMessage.SOAP12_NS : SOAPMessage.SOAP11_NS;
    mimeMsg.initWriteMode(msgNs);
    return(msgNs);
  }
  
  private void initMessageBody(String msgNs) throws RemoteException {
    MIMEMessage mimeMsg = (MIMEMessage)(clientContext.getMessage());
    XMLTokenWriter xmlTokenWriter = mimeMsg.getBodyWriter();
    try {
      xmlTokenWriter.enter(msgNs, SOAPMessage.BODYTAG_NAME);
      xmlTokenWriter.leave();
      mimeMsg.commitWrite();
      xmlTokenWriter.flush();
    } catch(IOException ioExc) {
      throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION, ioExc, ioExc.getMessage());
    }
  }
  
  private void releaseMessage(){
    MIMEMessage soapMessage = (MIMEMessage) clientContext.getMessage();
    if (soapMessage != null) {
     ConsumerMessagePool.returnMimeMessage(soapMessage);       
     clientContext.removeProperty(ClientConfigurationContextImpl.MESSAGE);
    }    
  }
  
  private boolean isSOAP12Mode() {
    String soapVersion = clientContext.getStaticContext().getInterfaceData().getProperty(InterfaceMapping.SOAP_VERSION);
    if(InterfaceMapping.SOAP_VERSION_12.equals(soapVersion)) {
      return(true);
    }
    String stubSoapVersion = (String)(clientContext.getPersistableContext().getProperty(PublicProperties.P_STUB_SOAP_VERSION));
    if(PublicProperties.P_STUB_SOAP_VERSION_12.equals(stubSoapVersion)) {
      return(true);
    }   
    return(false);
  }
}
