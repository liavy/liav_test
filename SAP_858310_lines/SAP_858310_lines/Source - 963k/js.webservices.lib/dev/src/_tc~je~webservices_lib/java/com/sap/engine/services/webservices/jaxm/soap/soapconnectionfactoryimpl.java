package com.sap.engine.services.webservices.jaxm.soap;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;

/**
 * SOAPConnection Implementation
 * @author       Chavdar Baykov , chavdar.baikov@sap.ag
 * @version      2.0
 */
public class SOAPConnectionFactoryImpl extends SOAPConnectionFactory {

  public SOAPConnectionFactoryImpl() {

  }

  public SOAPConnection createConnection() throws SOAPException {
    return new SOAPConnectionImpl();
  }

  public SOAPMessage createMessage(MimeHeaders mimeheaders, InputStream inputstream) throws IOException, SOAPException {
    return MessageFactory.newInstance().createMessage(mimeheaders, inputstream);
  }

}

