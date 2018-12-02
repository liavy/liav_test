package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerResolver;
import org.w3c.dom.Element;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicServiceImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SOAPTransportBinding;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;

/**
 * SOAP Dispatch implementation.
 * @author I024072
 *
 */
public abstract class DispatchImpl implements Dispatch, JAXWSProxy {
  
  protected ClientConfigurationContext clientContext;
  protected Class dataType;
  protected Service.Mode dispatchMode;
  protected RequestContext requestContext;
  
  private Map<String,Object> responseContext;
  private BindingImpl binding;  
  private Transformer transformer;  
  
  public DispatchImpl(Class dataType, Service.Mode mode, ClientConfigurationContext clientContext) {
    this.dataType = dataType;
    this.dispatchMode = mode;
    this.clientContext = clientContext;
    this.requestContext = new RequestContext(this.clientContext);
    this.responseContext = new Hashtable<String,Object>();
    this.binding = createBinding();    
  }
  
  protected abstract BindingImpl createBinding();
  
  /**
   * Creates dispatch copy for async invocation.
   * @return
   */
  protected abstract Dispatch copyDispatch();
    
  /**
   * Returns a transformer object for message creation.
   * @return
   * @throws TransformerConfigurationException
   */
  private Transformer getTransformer() throws TransformerConfigurationException {
    if (transformer == null) {
      TransformerFactory factory = TransformerFactory.newInstance();      
      transformer = factory.newTransformer();
      transformer.setOutputProperty("omit-xml-declaration", "yes");     
    }
    return(transformer);
  }
  
  protected void transform(Source content, ByteArrayOutputStream byteArrayOutput) throws TransformerException {
    Transformer transformer = getTransformer();
    StreamResult result = new StreamResult(byteArrayOutput);
    transformer.transform(content, result);
  }
  
  protected void marshall(Object msg, ByteArrayOutputStream output) throws Exception {
    JAXBContext context = clientContext.getJAXBContext();
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
    marshaller.marshal(msg, output);
  }
  
  protected Object unmarshall(Source source) throws JAXBException {
    JAXBContext context = clientContext.getJAXBContext();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    return(unmarshaller.unmarshal(source));
  }

  public Object invoke(Object msg) {
    return(invoke(msg, OperationMapping.MEP_REQ_RESP));
  }
  
  protected Object invoke(Object msg, String mep) {
    if (dispatchMode == Service.Mode.PAYLOAD) {
      return(invokePayload(msg, mep));
    }
    if (dispatchMode == Service.Mode.MESSAGE) {
      return(invokeMessage(msg, mep));
    }
    return null;
  }
  
  protected abstract Object invokeMessage(Object msg, String mep);
  
  protected abstract Object invokePayload(Object msg, String mep);
  
  protected void checkMessageType(Object msg) {
    if(msg != null && !JAXBContext.class.isAssignableFrom(dataType) && !dataType.isAssignableFrom(msg.getClass())) {
      throw new WebServiceException("The dispatch data type '" + dataType.getName() + "' should be assignable from the message type '" + msg.getClass().getName() + "'!");
    }
  }
    
  public Response invokeAsync(Object msg) {
    return((Response)invokeAsync(msg, null));
  }

  public Future invokeAsync(Object msg, AsyncHandler asyncHandler) {    
    Dispatch copy = copyDispatch();
    DispatchInvokeAsync runner = new DispatchInvokeAsync(copy, msg, asyncHandler);
    clientContext.getServiceContext().getExecutor().execute(runner);
    return(runner);
  }
  
  public void invokeOneWay(Object msg) {
    invoke(msg, OperationMapping.MEP_ONE_WAY);
  }

  public Binding getBinding() {
    return(binding);
  }

  public Map<String, Object> getRequestContext() {
    return(requestContext);
  }

  public Map<String, Object> getResponseContext() {
    return(responseContext);
  }

  public ClientConfigurationContext _getConfigurationContext() {
    return(clientContext);
  }
}
