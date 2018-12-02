package com.sap.engine.services.webservices.tools;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.interfaces.webservices.uddi.UDDIServer;
import com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin;
import com.sap.engine.interfaces.webservices.uddi4j.DispatcherPortsGetter;
import com.sap.engine.interfaces.webservices.uddi4j.SAPTransportInterface;
import com.sap.engine.services.webservices.jaxm.soap.SOAPConnectionImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.messaging.Endpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

/**
 * 
 * @author Alexander Zubev
 * 
 */
public class SAPTransportImpl implements SAPTransportInterface {
  public static final String NAME = "SAPTransport";
  private HTTPProxyResolver proxyResolver;
  private UDDIServerAdmin uddiServerAdmin;
  private DispatcherPortsGetter dispatcher;
  
  public SAPTransportImpl() {    
  }
  
  public SAPTransportImpl(HTTPProxyResolver proxyResolver, UDDIServerAdmin uddiServerAdmin, DispatcherPortsGetter dispatcher) {
    this.proxyResolver = proxyResolver;
    this.uddiServerAdmin = uddiServerAdmin;
    this.dispatcher = dispatcher;
  }

  /**
   * @see com.sap.engine.interfaces.webservices.uddi4j.SAPTransportInterface#send(Hashtable)
   */
  public Object send(Hashtable parameters) throws Exception {
    Element el = (Element) parameters.get(SAPTransportInterface.UDDI_ELEMENT);
    URL url = (URL) parameters.get(SAPTransportInterface.URL);
    String urlStr = url.toExternalForm();
    
    boolean http = true;
    boolean local = false;
    if (urlStr.startsWith("http://localhost.sap")) {
      http = true;
      local = true;
    } else if (urlStr.startsWith("https://localhost.sap")) {
      UDDIServer uddiServer = uddiServerAdmin.getUDDIServer();
      if (uddiServer == null) {
        throw new IOException("UDDI Server application is not running");
      }
      
      Properties props = uddiServer.getUDDIServerProperties();
      String allowHTTPProp = props.getProperty("debug.allow.http", "yes");
      if ("yes".equalsIgnoreCase(allowHTTPProp)) {
        http = true;
      } else {
        http = false;
      }
      local = true;
    }

    if (local) {
      Object[] hostPort;
      String schema;
      if (http) {
        hostPort = dispatcher.getHTTPPort();
        schema = "http://";
      } else {
        hostPort = dispatcher.getHTTPSPort();
        schema = "https://";
      }
      InetAddress host = (InetAddress) hostPort[0];
      int port = ((Integer) hostPort[1]).intValue();
      String uri = url.getPath();
      url = new URL(schema + host.getHostAddress() + ":" + port + uri);
    }

    HTTPProxy httpProxy = null;
    if (proxyResolver != null) {
      httpProxy = proxyResolver.getHTTPProxyForHost(url.getHost());
    }

    SOAPConnectionImpl connection = new SOAPConnectionImpl();
    if (httpProxy != null) {
      connection.setProxy(httpProxy.getProxyHost(), httpProxy.getProxyPort());
    }
    
    Element base = null;
 
    String bodyStr = null;
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
    transformer.transform(new DOMSource(el), new StreamResult(arrayStream));
    bodyStr = arrayStream.toString("utf-8");
    bodyStr = bodyStr.substring(bodyStr.indexOf("?>")+2); //ignore the xml declaration
    int indexOfXMLNS;
    while ((indexOfXMLNS = bodyStr.indexOf("xmlns=\"\"")) != -1) {
      bodyStr = bodyStr.substring(0, indexOfXMLNS) + bodyStr.substring(indexOfXMLNS + 8);
    }

    MimeHeaders headers = new MimeHeaders();
    headers.setHeader("Content-Type", "text/xml; charset=utf-8");
    headers.setHeader("SOAPAction","\"\"");
    ByteArrayInputStream source = new ByteArrayInputStream(new String("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\"><SOAP-ENV:Body>" 
                       + bodyStr + "</SOAP-ENV:Body></SOAP-ENV:Envelope>").getBytes("UTF-8"));
    MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    SOAPMessage soapMessage = mf.createMessage(headers, source);
    //SOAPMessageImpl soapMessage = new SOAPMessageImpl();

    SOAPMessage reply = connection.call((SOAPMessage) soapMessage, new Endpoint(url.toString()));
    Document document = (Document) (reply.getSOAPPart().getEnvelope().getBody()).getOwnerDocument();
    org.w3c.dom.Node envelope = getFirstChildNotText(document);
    org.w3c.dom.Node body = getFirstChildNotText(envelope);
    base = (Element) getFirstChildNotText(body);
        
    return base;
  }

  private org.w3c.dom.Node getFirstChildNotText(org.w3c.dom.Node node) {
    NodeList nodeList = node.getChildNodes();
    for (int i=0; i < nodeList.getLength(); i++) {
      org.w3c.dom.Node n = nodeList.item(i);
      if (n.getNodeType() != org.w3c.dom.Node.TEXT_NODE) {
        return n;
      }
    }
    return null;
  }
}
