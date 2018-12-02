package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.messaging.ConsumerMessagePool;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializationUtil;

public class SOAPDispatchImpl extends DispatchImpl {

  private static String BODY_OPEN_TAG = "<"+com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAPENV_PREFIX+":" +com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.BODYTAG_NAME+">";
  private static String BODY_CLOSE_TAG = "</"+com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAPENV_PREFIX+":" +com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.BODYTAG_NAME+">";    
  
  public SOAPDispatchImpl(Class dataType, Service.Mode mode, ClientConfigurationContext clientContext) {
    super(dataType, mode, clientContext);
  }
  
  protected BindingImpl createBinding() {
    return(new SOAPBindingImpl(clientContext));
  }
  
  protected Dispatch copyDispatch() {
    try {
      return new SOAPDispatchImpl(dataType, dispatchMode, ((ClientConfigurationContextImpl)clientContext).copy());
    } catch(Exception exc) {
      throw new WebServiceException(exc);
    }
  }
  
  /**
   * Checks context message existence. And puts message reference into the client context.
   * @param context
   */
  private void checkContextSOAPMessage(String mep) {
    Message msg = this.clientContext.getMessage();
    if(msg == null) {
      msg = ConsumerMessagePool.getMIMEMessage();
      this.clientContext.setProperty(ClientConfigurationContextImpl.MESSAGE,msg);
    }
    this.clientContext.getPersistableContext().setProperty(OperationMapping.OPERATION_MEP,mep);
    this.clientContext.getPersistableContext().setProperty(PublicProperties.CALL_PROTOCOLS,"false");    
  }
  
  
  /**
   * Releases the used message.
   */
  private void releaseMessage(){
    MIMEMessage message = (MIMEMessage) clientContext.getMessage();
    if (message != null) {
     ConsumerMessagePool.returnMimeMessage(message);
     clientContext.removeProperty(ClientConfigurationContextImpl.MESSAGE);
    }
  }
  
  
  /**
   * Utility method that writes empty body.
   * @param message
   * @throws Exception
   */
  private void writeEmptyBody(MIMEMessageImpl message) throws Exception {
    ByteArrayOutputStream bodyBuffer = message.getInternalWriterBuffer();
    bodyBuffer.write(BODY_OPEN_TAG.getBytes()); //$JL-I18N$  
    bodyBuffer.write(BODY_CLOSE_TAG.getBytes()); //$JL-I18N$
    bodyBuffer.flush();    
  }
  
  /**
   * Initializes the soap message output.
   * @return
   */
  private MIMEMessageImpl initMessageOutput() {
    MIMEMessageImpl message = (MIMEMessageImpl) this.clientContext.getMessage();
    message.initWriteMode(com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_NS);    
    message.commitWrite();
    return message;
  }

  /**
   * Serialize message payload from source.
   * @param content
   */
  private void serializeSOAPMessage(SOAPMessage content) {
    if (content == null) {
      throw new WebServiceException("Passed NULL message to dispatch method.");
    }
    MIMEMessageImpl message = (MIMEMessageImpl) this.clientContext.getMessage();    
    message.initWriteMode(com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_NS);         
    try {
      MessageConvertor.convertSAAJMessageIntoOutputSAPMessage(content,message);
    } catch (Exception x) {
      throw new WebServiceException(x);
    }          
    message.commitWrite();    
  }  

  /**
   * Serialize message payload from source.
   * @param content
   */
  private void serializeSOAPMessagePayload(Source content) {
    try {
      MIMEMessageImpl message = initMessageOutput();     
      if (content != null) {        
        ByteArrayOutputStream bodyBuffer = message.getInternalWriterBuffer();
        bodyBuffer.write(BODY_OPEN_TAG.getBytes());  //$JL-I18N$
        transform(content, bodyBuffer);
        bodyBuffer.write(BODY_CLOSE_TAG.getBytes()); //$JL-I18N$
        bodyBuffer.flush();
      } else {
        writeEmptyBody(message);
      }
    } catch (Exception x) {
      throw new WebServiceException(x);
    }
  }
  
  /**
   * Serialize message payload from source.
   * @param content
   */
  private void serializeSOAPMessagePayloadJAXB(Object content) {
    try {
      MIMEMessageImpl message = initMessageOutput();
      if (content != null) {
        ByteArrayOutputStream bodyBuffer = message.getInternalWriterBuffer();        
        bodyBuffer.write(BODY_OPEN_TAG.getBytes()); //$JL-I18N$        
        marshall(content, bodyBuffer);
        bodyBuffer.write(BODY_CLOSE_TAG.getBytes()); //$JL-I18N$
        bodyBuffer.flush();
      } else {
        writeEmptyBody(message);
      }
    } catch (Exception x) {
      throw new WebServiceException(x);
    }
  }

  /**
   * Reads response as JAXB.
   * @return
   */
  private Object readMessagePayloadAsJAXB(Source source) {
    try {
      return unmarshall(source);
    } catch (Exception e) {
      throw new WebServiceException(e);
    }  
  }

  /**
   * Reads the message body as Source.
   * @return
   */
  private Source readMessagePayloadAsSource(SOAPMessage saajMessage) {
    try {      
      Element bodyElement = (Element)saajMessage.getSOAPBody();
      bodyElement = SerializationUtil.getFirstElementChild(bodyElement);
      return new DOMSource(bodyElement);
    } catch (Exception e) {
      throw new WebServiceException(e);
    }
  }
  
  /**
   * Reads the response message as SOAPMessage. 
   * @return
   */
  private SOAPMessage readMessage() {
    try {
      MIMEMessageImpl message = (MIMEMessageImpl) this.clientContext.getMessage();
      SOAPMessage saajMessage = MessageConvertor.convertInboundSAPMessageIntoSAAJ(message);
      message.clear();
      return saajMessage;
    } catch (Exception e) {
      throw new WebServiceException(e);
    }
  }
  
  /**
   * Invokes the transport binding.
   *
   */   
  private void invokeBinding() {
    try {
      this.clientContext.getTransportBinding().sendMessage(this.clientContext);
    } catch(SOAPFaultException soapFaultExc) {
      throw soapFaultExc;
    } catch (Exception exc) {
      throw new WebServiceException(exc);
    }
  }
  
  protected Object invoke(Object msg, String mep) {
    // Checks that message is created    
    checkContextSOAPMessage(mep);
    
    Object result = super.invoke(msg, mep);
    
    // Releases the soap message that is no longer needed.
    releaseMessage();
    
    return result;
  }
  
  protected Object invokePayload(Object arg0, String mep) {
    Object result = null;;
    if (Source.class.equals(dataType)) { // Source content      
      serializeSOAPMessagePayload((Source) arg0);
      invokeBinding();      
      if (OperationMapping.MEP_REQ_RESP.equals(mep)) {
        SOAPMessage soapMessage = readMessage();        
        result = readMessagePayloadAsSource(soapMessage);
      }
    }
    if (JAXBContext.class.equals(dataType)) {
      serializeSOAPMessagePayloadJAXB(arg0);
      invokeBinding();
      if (OperationMapping.MEP_REQ_RESP.equals(mep)) { 
        SOAPMessage soapMessage = readMessage();
        Source source = readMessagePayloadAsSource(soapMessage);
        result = readMessagePayloadAsJAXB(source);
      }            
    }
    return result;    
  }
    
  protected Object invokeMessage(Object arg0, String mep) {
    Object result = null;
    if (SOAPMessage.class.equals(dataType)) { // SOAP Message content
      SOAPMessage message = (SOAPMessage) arg0;
      serializeSOAPMessage(message);
      invokeBinding();
      if (OperationMapping.MEP_REQ_RESP.equals(mep)) {        
        result = readMessage();
      }                        
    }
    return result;
  }

  public EndpointReference getEndpointReference() {
    throw new RuntimeException("Method not supported");
//    return null;
  }

  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
    throw new RuntimeException("Method not supported");
//    return null;
  }
  
}
