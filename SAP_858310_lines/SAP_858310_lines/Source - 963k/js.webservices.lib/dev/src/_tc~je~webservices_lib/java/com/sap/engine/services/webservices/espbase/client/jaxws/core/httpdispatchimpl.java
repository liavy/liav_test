package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;

public class HTTPDispatchImpl extends DispatchImpl {
  
  private static final String HTTP_LOCATION_HEADER_NAME = "location"; 
  private static final int INITIAL_READ_BUFFER_LENGTH = 2000;
  private static final String ENABLE_KEEP_ALIVE_PROPERTY_VALUE = "true";
  
  private ClientHTTPTransport httpTransport;
  
  public HTTPDispatchImpl(Class dataType, Service.Mode mode, ClientConfigurationContext clientContext) {
    super(dataType, mode, clientContext);
    httpTransport = new ClientHTTPTransport(); 
    ((RequestContext)requestContext).putPersistableContextProperty(PublicProperties.C_KEEP_ALIVE.toString(), ENABLE_KEEP_ALIVE_PROPERTY_VALUE);
  }
  
  protected BindingImpl createBinding() {
    return(new HTTPBindingImpl());
  }
  
  protected Dispatch copyDispatch() {
    try {
      return new HTTPDispatchImpl(dataType, dispatchMode, ((ClientConfigurationContextImpl)clientContext).copy());
    } catch(Exception exc) {
      throw new WebServiceException(exc);
    }
  }
  
  protected Object invokeMessage(Object msg, String mep) {
    return(invokePayload(msg, mep));
  }
  
  protected void checkMessageType(Object msg) {
    if(msg instanceof SOAPMessage) {
      throw new WebServiceException("Message instances of javax.xml.soap.SOAPMessage are forbidden for dispatches if the binding is HTTP!" );
    }
    super.checkMessageType(msg);
  }
  
  protected Object invokePayload(Object msg, String operationMEP) {
    try {
      checkMessageType(msg);
      if(InterfaceMapping.HTTPPOSTBINDING.equals(clientContext.getStaticContext().getInterfaceData().getBindingType())) {
        return(invokePayload_Post(msg, operationMEP));
      }
      return(invokePayload_Get());
    } catch(Throwable thr) {
      if(thr instanceof WebServiceException) {
        throw (WebServiceException)thr;
      }
      throw new WebServiceException(thr);
    }
  }

  private Object invokePayload_Post(Object msg, String operationMEP) throws Exception {
    if(msg == null) {
      throw new WebServiceException("The binding is HTTP POST. The message can not be NULL!");
    }
    if(Source.class.isAssignableFrom(dataType)) {
      return(sendHTTPPostRequest_Source((Source)msg, operationMEP));
    }
    if(JAXBContext.class.isAssignableFrom(dataType)) {
      return(sendHTTPPostRequest_JAXBContext(msg, operationMEP));
    }
    if(DataSource.class.isAssignableFrom(dataType)) {
      return(sendHTTPPostRequest_DataSource((DataSource)msg, operationMEP));
    }
    return(null);
  }
  
  private Object invokePayload_Get() throws Exception {
    sendHTTPRequest(new byte[0], ClientHTTPTransport.REQUEST_METHOD_GET, OperationMapping.MEP_ONE_WAY);
    return(null);
  }

  private Object sendHTTPPostRequest_JAXBContext(Object msg, String operationMEP) throws Exception {
    ByteArrayOutputStream byteArrayOutput = null;
    try {
      byteArrayOutput = new ByteArrayOutputStream();
      marshall(msg, byteArrayOutput);
      byte[] responseBytes = sendHTTPRequest(byteArrayOutput, ClientHTTPTransport.REQUEST_METHOD_POST, operationMEP);
      return(unmarshall(new StreamSource(new ByteArrayInputStream(responseBytes))));
    } finally {
      byteArrayOutput.close();
    }
  }
  
  private Object sendHTTPPostRequest_Source(Source msg, String operationMEP) throws Exception {
    ByteArrayOutputStream byteArrayOutput = null;
    try {
      byteArrayOutput = new ByteArrayOutputStream();
      transform(msg, byteArrayOutput);
      byte[] responseBytes = sendHTTPRequest(byteArrayOutput, ClientHTTPTransport.REQUEST_METHOD_POST, operationMEP);
      return(new StreamSource(new ByteArrayInputStream(responseBytes)));
    } finally {
      byteArrayOutput.close();
    }
  }
  
  private Object sendHTTPPostRequest_DataSource(DataSource msg, String operationMEP) throws Exception {
    ByteArrayOutputStream byteArrayOutput = null;
    try {
      byteArrayOutput = new ByteArrayOutputStream();
      transform(new StreamSource(msg.getInputStream()), byteArrayOutput);
      byte[] responseBytes = sendHTTPRequest(byteArrayOutput, ClientHTTPTransport.REQUEST_METHOD_POST, operationMEP);
      return(new javax.mail.util.ByteArrayDataSource(responseBytes, msg.getContentType()));
    } finally {
      byteArrayOutput.close();
    }
  }

  private byte[] sendHTTPRequest(ByteArrayOutputStream byteArrayOutput, String requestMethod, String operationMEP) throws Exception {
    byteArrayOutput.flush();
    return(sendHTTPRequest(byteArrayOutput.toByteArray(), requestMethod, operationMEP));
  }
  
  private byte[] sendHTTPRequest(byte[] bodyBytes, String requestMethod, String operationMEP) throws Exception {
    try {
      initHTTPTransport(bodyBytes, requestMethod);
      return(sendHTTPRequest(bodyBytes, operationMEP));
    } finally {
      httpTransport.closeSession();
    }
  }
  
  private byte[] sendHTTPRequest(byte[] bodyBytes, String operationMEP) throws Exception {
    OutputStream httpOutput = httpTransport.getRequestStream();
    httpOutput.write(bodyBytes);
    httpOutput.flush();
    int responseCode = httpTransport.getResponseCode();
    if(responseCode == 301 || responseCode == 302 || responseCode == 307) {
      return(redirectHTTPRequest(bodyBytes, operationMEP));
    }
    if(OperationMapping.MEP_ONE_WAY.equals(operationMEP)) {
      if(responseCode == 200 || responseCode == 202) {
        while(httpTransport.getResponseStream().read() != -1);
        return(null);
      }
    } else if(responseCode == 200) {
      return(readHTTPResponse());
    }
    throw new WebServiceException("HTTP server return code '" + responseCode + "'!");
  }
  
  private byte[] readHTTPResponse() throws IOException {
    InputStream httpInput = httpTransport.getResponseStream();
    byte[] buffer = new byte[INITIAL_READ_BUFFER_LENGTH];
    int readBytes = 0;
    int iterationReadBytes = 0;
    while((iterationReadBytes = httpInput.read(buffer, readBytes, buffer.length - readBytes)) != -1) {
      readBytes += iterationReadBytes;
      if(readBytes == buffer.length) {
        buffer = increaseBuffer(buffer);
      }
    }
    return(createReadBytesArray(buffer, readBytes));
  }
  
  private byte[] createReadBytesArray(byte[] buffer, int readBytes) {
    return(createByteArray(buffer, readBytes, readBytes));
  }
  
  private byte[] increaseBuffer(byte[] buffer) {
    return(createByteArray(buffer, buffer.length + INITIAL_READ_BUFFER_LENGTH, buffer.length));
  }
  
  private byte[] createByteArray(byte[] srcBytes, int dstByteArrayLength, int dstBytesCount) {
    byte[] dstBytes = new byte[dstByteArrayLength];
    System.arraycopy(srcBytes, 0, dstBytes, 0, dstBytesCount);
    return(dstBytes);
  }
  
  private byte[] redirectHTTPRequest(byte[] bodyBytes, String operationMEP) throws Exception {
    Hashtable headres = new Hashtable(httpTransport.getHeaders());
    while(httpTransport.getResponseStream().read() != -1);
    String redirectURL = determineRedirectEndpointURL();
    httpTransport.closeSession(); 
    httpTransport.getHeaders().clear();
    httpTransport.getHeaders().putAll(headres);
    clientContext.getPersistableContext().setProperty(PublicProperties.P_ENDPOINT_URL, redirectURL);
    return(sendHTTPRequest(bodyBytes, operationMEP));
  }
  
  private String determineRedirectEndpointURL() throws IOException {
    String[] locationHeaders = httpTransport.getHeader(HTTP_LOCATION_HEADER_NAME);
    if(locationHeaders.length != 0) {
      URL rootRedirectURL =  URLLoader.fileOrURLToURL(null, httpTransport.getEndpoint());
      URL redirectURL = URLLoader.fileOrURLToURL(rootRedirectURL, locationHeaders[0]);
      return(redirectURL.toExternalForm());
    } else {
      throw new WebServiceException("HTTP redirect should be processed but 'location' header is missing!");
    }
  }
  
  private void initHTTPTransport(byte[] bodyBytes, String requestMethod) throws Exception {
    httpTransport.init(requestMethod, clientContext);
    httpTransport.setHeader(SOAPTransportBinding.CONTENT_LENGTH_HEADER, String.valueOf(bodyBytes.length));
  }


  public EndpointReference getEndpointReference() {
//    throw new RuntimeException("Method not supported");
    return null;
  }

  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {        
//    throw new RuntimeException("Method not supported");  
    return null;
  }
}
