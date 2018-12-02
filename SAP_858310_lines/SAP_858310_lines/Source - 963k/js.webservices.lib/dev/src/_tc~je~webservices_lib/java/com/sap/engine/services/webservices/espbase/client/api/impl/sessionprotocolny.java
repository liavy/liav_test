package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.Cookie;

public class SessionProtocolNY implements ConsumerProtocol, ProtocolExtensions {

  public static final String NAME = "SessionProtocol";
  
  private static final String ENABLE_SESSION_HEADER_NS = "http://www.sap.com/webas/630/soap/features/session/";
  private static final String ENABLE_SESSION_HEADER_NAME = "sapsess:Session";
  private static final String ENABLE_SESSION_ELEMENT_NAME = "enableSession";
  
  public int afterDeserialization(ConfigurationContext arg0) throws ProtocolException, MessageException {
    return(CONTINUE);
  }

  public void beforeSerialization(ConfigurationContext arg0) throws ProtocolException {    
  }

  public String getProtocolName() {
    return(NAME);
  }

  public int handleRequest(ConfigurationContext configCtx) throws ProtocolException, MessageException {
    ClientConfigurationContext clientCtx = (ClientConfigurationContext)configCtx;
    if (isPropetySet(clientCtx, PublicProperties.C_SESSION_MAINTAIN_STRING, PublicProperties.F_SESSION_MAINTAIN_FALSE)) {
      Message msg = clientCtx.getMessage();
      if(isPropetySet(clientCtx, PublicProperties.F_SESSION_FLUSH, PublicProperties.F_SESSION_FLUSH_TRUE)) {
        // If SessionMaintain property is false and "releaseServerResourses" is called ("session flush") then add special SOAP Header to the message.      
        if(clientCtx.getDynamicContext().getProperty(PublicProperties.F_SESSION_COOKIE) != null) {
          SOAPMessage soapMessage = (SOAPMessage)msg;
          SOAPHeaderList soapHeaders = soapMessage.getSOAPHeaders();
          Element enableSessionHeader = soapHeaders.createHeader(new QName(ENABLE_SESSION_HEADER_NS, ENABLE_SESSION_HEADER_NAME));
          Element enableSessionElement = enableSessionHeader.getOwnerDocument().createElement(ENABLE_SESSION_ELEMENT_NAME);
          Text content = enableSessionHeader.getOwnerDocument().createTextNode("false");
          enableSessionElement.appendChild(content);
          enableSessionHeader.appendChild(enableSessionElement);
          soapHeaders.addHeader(enableSessionHeader);
        }
        // Clear session flush flag.
        clientCtx.getPersistableContext().setProperty(PublicProperties.F_SESSION_FLUSH, PublicProperties.F_SESSION_FLUSH_FALSE);
      } else {
        // Session flush is false - only clear the session cookie.
        clientCtx.getDynamicContext().removeProperty(PublicProperties.F_SESSION_COOKIE);
        clientCtx.getPersistableContext().setProperty(PublicProperties.C_SESSION_MAINTAIN_STRING, PublicProperties.F_SESSION_MAINTAIN_TRUE);
      }
    }
    return(CONTINUE);
  }
  
  private boolean isPropetySet(ClientConfigurationContext clientCtx, String name, Object requiredValue) {
    Object propertyValue = clientCtx.getPersistableContext().getProperty(name);
    return(requiredValue.equals(propertyValue));
  }

  public int handleResponse(ConfigurationContext configCtx) throws ProtocolException {
    return(CONTINUE);
  }

  public int handleFault(ConfigurationContext configCtx) throws ProtocolException {
    return(CONTINUE);
  }

  /**
   * Store the session cookies object during Proxy Hibernation.
   * Convert from Object in the dynamic context to String in the persistable context.
   */
  public void beforeHibernation(ConfigurationContext configCtx) throws ProtocolException {
    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configCtx; 
    Object cookieObject = clientCtx.getDynamicContext().getProperty(PublicProperties.F_SESSION_COOKIE);
    if (cookieObject != null) {
    	StringBuffer line = new StringBuffer();
    	ArrayList cookies = (ArrayList) cookieObject;
    	Iterator iter = cookies.iterator();
    	while (iter.hasNext()) {
    		String cookie = Cookie.getCookieAsResponseLine((Cookie) iter.next());
    		line.append(cookie);
    		line.append('@');
    	}
    	clientCtx.getPersistableContext().setProperty(	PublicProperties.F_SESSION_COOKIE, new String(line));    	
    }         
  }
  
  /**
   * Restores the session cookies during client restore after hibernation.
   * Convert from String in the persistable context to Object in the dynamic context.
   */
  public void afterHibernation(ConfigurationContext configCtx) throws ProtocolException {
    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configCtx;
    String line = (String) clientCtx.getPersistableContext().getProperty(PublicProperties.F_SESSION_COOKIE);
    
    if (line != null) {
        StringTokenizer tokenizer = new StringTokenizer(line, "@");
        ArrayList<Cookie> content = new ArrayList<Cookie>();
        while (tokenizer.hasMoreTokens()) {
        	Cookie.updateCookies(Cookie.readCookies((String) tokenizer.nextElement()), content);
        }    
        clientCtx.getDynamicContext().setProperty(PublicProperties.F_SESSION_COOKIE, content);
        clientCtx.getPersistableContext().removeProperty(PublicProperties.F_SESSION_COOKIE);
        clientCtx.getPersistableContext().setProperty(PublicProperties.C_SESSION_MAINTAIN.toString(), PublicProperties.F_SESSION_MAINTAIN_TRUE);
    }
  }
  
  public void finishMessageDeserialization(ConfigurationContext configCtx) throws ProtocolException {
  }

  public void finishHibernation(ConfigurationContext configCtx) throws ProtocolException {
  }
}
