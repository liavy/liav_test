package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.IOException;
import java.io.StringReader;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.attach.ProviderAttachmentProtocol;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.api.ProviderAttachmentHandlerFactory;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeteringUtil;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
import com.sap.engine.services.webservices.espbase.xi.impl.ESPXITransport;
import com.sap.engine.services.webservices.espbase.xi.util.XIAttachmentHandler;
import com.sap.engine.services.webservices.tools.SoftReferenceInstancesPool;
import com.sap.tc.logging.Location;

public class ESPXITransportBinding implements TransportBinding {

  private static final Location LOCATION = Location.getLocation(ESPXITransportBinding.class);
  
  private static SoftReferenceInstancesPool<MIMEMessageImpl> messagesPool = new SoftReferenceInstancesPool<MIMEMessageImpl>();
  
  public static final String ID = "ESPXI";
  
  public Message createInputMessage(ProviderContextHelper providerContext) throws RuntimeProcessException {
    try {
      MIMEMessageImpl mimeMsg = getMIMEMessage();
      ESPXITransport xiTransport = (ESPXITransport)(providerContext.getTransport());
      ESPXIMessage requestXIMsg = xiTransport.getRequestXIMessage();
      initMIMEMessageReadMode(mimeMsg, requestXIMsg);
      initProviderContextWithXIProperites(providerContext, requestXIMsg);
      XIAttachmentHandler.convertXIAttachmentsIntoSOAPAttachments(requestXIMsg, mimeMsg.getAttachmentContainer());
      transferMeteringData(requestXIMsg, mimeMsg);
      return(mimeMsg);
    } catch(Exception exc) {
      throw exc instanceof RuntimeProcessException ? (RuntimeProcessException)exc : new RuntimeProcessException(exc);
    }
  }
  
  private void transferMeteringData(ESPXIMessage xiMsg, MIMEMessageImpl mimeMsg){
    
    ServiceMeteringUtil.moveMeteringDataToSOAPHeaders(xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_APPNAME), 
                                                      xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_COMPONENT),
                                                      xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_APPTYPE),
                                                      xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_COMPANY),
                                                      xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_SYS),
                                                      xiMsg.getHeader(ServiceMeteringConstants.QNAME_HEADER_USER_CODE),
                                                      mimeMsg);
  }
  
  private void initMIMEMessageReadMode(MIMEMessageImpl mimeMsg, ESPXIMessage requestXIMsg) throws Exception {
    String xiRequestData = requestXIMsg.getData();
    if(xiRequestData == null) {
      throw new RuntimeProcessException("XI request payload data is null!");
    }
    mimeMsg.initReadMode(new StringReader(xiRequestData), SOAPMessage.SOAP11_NS);
  }
  
  private void initProviderContextWithXIProperites(ProviderContextHelper providerContext, ESPXIMessage inXIMsg) {
    PublicProperties.setDynamicProperty(PublicProperties.XI_APP_ACK_REQUESTED_RT_PROP_NAME, inXIMsg.getApplicationAckRequested(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_APP_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, inXIMsg.getApplicationErrorAckRequested(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_QUEUE_ID_RT_PROP_NAME, inXIMsg.getQueueId(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_SENDER_PARTY_NAME_RT_PROP_NAME, inXIMsg.getSenderPartyName(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_SENDER_SERVICE_RT_PROP_NAME, inXIMsg.getSenderService(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_SYS_ACK_REQUESTED_RT_PROP_NAME, inXIMsg.getSystemAckRequested(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_SYS_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, inXIMsg.getSystemErrorAckRequested(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_SERVICE_INTERFACE_NAME_RT_PROP_NAME, inXIMsg.getServiceInterfaceName(), providerContext);
    PublicProperties.setDynamicProperty(PublicProperties.XI_IS_ASYNC_RT_PROP_NAME, inXIMsg.isAsync() ? Boolean.TRUE : Boolean.FALSE, providerContext);
  }
  
  private MIMEMessageImpl getMIMEMessage() {
    MIMEMessageImpl mimeMsg = messagesPool.getInstance();
    if (mimeMsg == null) {
      return(new MIMEMessageImpl());
    }
    return(mimeMsg);
  }
  
  public OperationMapping resolveOperation(ProviderContextHelper ctx) throws RuntimeProcessException {
    MIMEMessageImpl mimeMsg = (MIMEMessageImpl)(ctx.getMessage());
    InterfaceMapping interfaceMapping = ctx.getStaticContext().getInterfaceMapping();
    String keys[] = getMessageKeysFromReader(mimeMsg, ctx);
    OperationMapping[] operationMappings = interfaceMapping.getOperation();
    for(int i = 0; i < operationMappings.length; i++) {
      OperationMapping operationMapping = operationMappings[i];
      String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER); 
      String operationNs = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);
      LOCATION.debugT("OperationName: " + operationName + ", OperationNS: " + operationNs);
      if(compareNSes(keys[0], operationNs) && operationName.equals(keys[1])) {
        return(operationMapping);
      }
    }
    String keysStr = "[" + keys[0] + "], [" + keys[1] + "]";
    throw new ProcessException(ExceptionConstants.OPERATION_NOT_FOUND, new Object[]{SOAPHTTPTransportBinding.TB_TYPE, keysStr, interfaceMapping});
  }
  
  private boolean compareNSes(String ns1, String ns2) {
    if(ns1 == null) {
      return(ns2 == null || ns2.length() == 0);
    }
    if(ns2 == null) {
      return(ns1.length() == 0);
    }
    return(ns1.equals(ns2));
  }
  
  private String[] getMessageKeysFromReader(MIMEMessageImpl mimeMsg, ProviderContextHelper ctx) throws RuntimeProcessException {
    String[] msgKeys = new String[2];
    XMLTokenReader reader = mimeMsg.getBodyReader();
    LOCATION.debugT("getMessageKeysFromReader(), reader: " + reader + ", state: " + reader.getState());
    try {
      while(reader.getState() != XMLTokenReader.EOF && reader.getState() != XMLTokenReader.STARTELEMENT) {
          reader.next();
      }
      if(reader.getState() == XMLTokenReader.EOF) {
        throw new ProcessException(ExceptionConstants.EOF_IN_BODY_ELEMENT_SEARCH, new Object[]{ID});
      }
    } catch (ParserException parserExc) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{parserExc.getLocalizedMessage()}, parserExc);
    }
    if (reader.getState() == XMLTokenReader.STARTELEMENT) {
      msgKeys[0] = reader.getURI();
      msgKeys[1] = reader.getLocalName();
    }    
    return(msgKeys);
  }
  
  public Object[] getParameters(Class[] methodClasses, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException {
    return(StreamEngine.deserializeJEE(methodClasses, loader, ctx));
  }
  
  public Message initOutputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    MIMEMessageImpl mimeMsg = getMIMEMessage(ctx);
    mimeMsg.clear();
    mimeMsg.initWriteMode(SOAPMessage.SOAP11_NS);    
    return(mimeMsg);
  }
  
  private MIMEMessageImpl getMIMEMessage(ProviderContextHelper ctx) {
    MIMEMessageImpl mimeMsg = (MIMEMessageImpl)(ctx.getProperty(ProviderContextHelperImpl.MESSAGE));
    if (mimeMsg == null) {
      mimeMsg = getMIMEMessage();
    }
    return(mimeMsg);
  }
  
  public Message createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    MIMEMessageImpl mimeMsg = (MIMEMessageImpl)(ctx.getMessage());
    try {
      initMessageBodyWriter(mimeMsg);
      StreamEngine.serializeJEE(returnObject, returnObjectClass, resultParams, resultParamsClasses, ctx);
      finishResponseMessage(false, ctx);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
    return(mimeMsg);
  }
  
  private XMLTokenWriter initMessageBodyWriter(MIMEMessageImpl mimeMsg) throws Exception {
    XMLTokenWriter writer = mimeMsg.getBodyWriter();
    setPrefixForNamespace(writer, mimeMsg.getSOAPVersionNS());
    setPrefixForNamespace(writer, NS.XSI);
    setPrefixForNamespace(writer, NS.XS);  
    return(writer);
  }
  
  private void setPrefixForNamespace(XMLTokenWriter writer, String namespace) throws IOException {
    String prefix = writer.getPrefixForNamespace(namespace);
    writer.setPrefixForNamespace(prefix, namespace);
  }
  
  private void finishResponseMessage(boolean isFault, ProviderContextHelper providerContext) throws Exception {
    MIMEMessageImpl mimeMsg = (MIMEMessageImpl)(providerContext.getMessage());
    XMLTokenWriter writer = mimeMsg.getBodyWriter();
    writer.flush();
    mimeMsg.commitWrite();
    ESPXITransport xiTransport = (ESPXITransport)(providerContext.getTransport());
    xiTransport.setResponseXIMessage(createResponseXIMessage(mimeMsg, xiTransport.getXIMessageProcessor(), isFault));
  }
  
  public Message createFaultMessage(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    ParameterMapping[] faults = null;
    OperationMapping operation = null;
    try {
      operation = ctx.getOperation();
    } catch (Throwable tr) { //in case it is not possible to resolve the operation, ignore the problem
      LOCATION.catching(tr);
      operation = null;
    }
    if(operation != null) { 
      faults = operation.getParameters(ParameterMapping.FAULT_TYPE);
    }
    MIMEMessageImpl mimeMsg = getMIMEMessage(ctx);
    if(faults != null) {
      for(int i = 0; i < faults.length; i++) {
        ParameterMapping faultParamMapping = faults[i];
        if(faultParamMapping.getJavaType().equalsIgnoreCase(thr.getClass().getName())) {
          serializeThrowable(thr, mimeMsg, faultParamMapping, ctx);
          return(mimeMsg);
        }
      }
    }
    serializeThrowable(thr, mimeMsg, null, ctx);
    return(mimeMsg);
  }
  
  private void serializeThrowable(Throwable thr, MIMEMessageImpl mimeMsg, ParameterMapping fault, ProviderContextHelper ctx) throws ProcessException {
    try {
      initMessageBodyWriter(mimeMsg);
      StreamEngine.serializeJEEThrowable(thr, fault, StreamEngine.SERVER_ERROR_CODE, ctx);
      finishResponseMessage(true, ctx);
    } catch(Exception exc) {
      if(exc instanceof ProcessException) {
        throw (ProcessException)exc;
      } 
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{exc.getLocalizedMessage()}, exc);
    }
  }
  
  private ESPXIMessage createResponseXIMessage(MIMEMessageImpl mimeMsg, ESPXIMessageProcessor xiMessageProcessor, boolean isFault) throws Exception {
    ESPXIMessage responseXIMessage = xiMessageProcessor.createMessage();
    responseXIMessage.setData(mimeMsg.getInternalWriterBuffer().toString(XIFrameworkConstants.XI_MESSAGE_ENCODING));
    responseXIMessage.setFault(isFault);
    XIAttachmentHandler.convertSOAPAttachmentsIntoXIAttachments(((ProviderAttachmentProtocol)(ProviderAttachmentHandlerFactory.getAttachmentHandler())).getOutboundAttachmentContainer(), responseXIMessage);
    return(responseXIMessage);
  }
  
  public void sendResponseMessage(ProviderContextHelper ctx, int commPattern) throws RuntimeProcessException {
  }
  
  public void sendServerError(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    ESPXITransport xiTransport = (ESPXITransport)(ctx.getTransport());
    xiTransport.sendServerError(thr);
  }
  
  public void onContextReuse(ProviderContextHelper ctx) {
    MIMEMessageImpl mimeMessage = (MIMEMessageImpl)(ctx.getProperty(ProviderContextHelperImpl.MESSAGE));
    if (mimeMessage != null) {
      mimeMessage.clear();
      messagesPool.rollBackInstance(mimeMessage);
    }
  }
  
  public int getCommunicationPattern(ProviderContextHelper ctx) throws RuntimeProcessException {
    return(TransportBinding.SYNC_COMMUNICATION);
  }
  
  public void sendAsynchronousResponse(ProviderContextHelper ctx) throws RuntimeProcessException {
  }
  
  public String getAction(ProviderContextHelper ctx) throws RuntimeProcessException {
    return(null);
  }
  
  public void sendMessageOneWay(String endpointURL, Message msg, String action) throws RuntimeProcessException {
  }
}
