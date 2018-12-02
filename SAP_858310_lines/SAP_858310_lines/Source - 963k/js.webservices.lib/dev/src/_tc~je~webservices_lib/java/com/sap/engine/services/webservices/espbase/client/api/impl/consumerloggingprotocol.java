package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.IOException;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.logging.LoggingTokenReader;
import com.sap.engine.services.webservices.espbase.messaging.logging.LoggingTokenWriter;
import com.sap.engine.services.webservices.espbase.messaging.logging.ProtocolMessageLogger;
import com.sap.tc.logging.Location;

/**
 * 
 * @author I056242
 * The consumer logging protocol is designed to perform soap message log
 * driven by the protocol events. The protocol must always be places
 * behind the attachment and security protocol in the inbound case
 * and before them in the outbound case.
 * 
 */
public class ConsumerLoggingProtocol extends ProtocolMessageLogger implements ConsumerProtocol, ProtocolExtensions {

  private static Location Loc = Location.getLocation(ConsumerLoggingProtocol.class);

  public static String NAME = "ConsumerLogginProtocol";

  public String getProtocolName() {
    return NAME;
  }


  public void beforeSerialization(ConfigurationContext ctx) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);    
    boolean debug = tempArray[0];
    boolean hideValues = tempArray[1];
       
    if (debug) {    
      try {
        ClientConfigurationContext clientContext = (ClientConfigurationContext) ctx;

        Message message = clientContext.getMessage();

        if (message != null && message instanceof MIMEMessage) {
          MIMEMessage mimeMessage = (MIMEMessage) message;

          prepareOutboundLog(mimeMessage, hideValues);

          clientContext.getDynamicContext().setProperty(OUTBOUND_LOG_WRITER, mimeMessage.getBodyWriter());
        }
      } catch (IOException e) {
        throw new ProtocolException(e);
      }
    }
  }

  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    boolean[] tempArray = checkLog(Loc);    
    boolean debug = tempArray[0];
   
    if (debug) {
      try {
        ClientConfigurationContext clientContext = (ClientConfigurationContext) context;

        LoggingTokenWriter loggingWriter = (LoggingTokenWriter) clientContext.getDynamicContext().getProperty(OUTBOUND_LOG_WRITER);
          
        logOutboundMessage(loggingWriter, Loc);
        
        // Remove it from the context to prevent its reusage in wrong context.
        clientContext.getDynamicContext().removeProperty(OUTBOUND_LOG_WRITER);
      } catch (IOException e) {
        throw new ProtocolException(e);
      }
    }
    return CONTINUE;
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);    
    boolean debug = tempArray[0];
    boolean hideValues = tempArray[1];
        
    if (debug) {
      try {
        ClientConfigurationContext clientContext = (ClientConfigurationContext) context;

        Message message = clientContext.getMessage();

        // If the message is null we have nothing to log.
        if (message != null && message instanceof MIMEMessage) {   
          MIMEMessage mimeMessage = (MIMEMessage) message;

          prepareInboundLog(mimeMessage, hideValues);
          
          clientContext.getDynamicContext().setProperty(INBOUND_LOG_READER, mimeMessage.getBodyReader());
        }
      } catch (IOException e) {
        throw new ProtocolException(e);
      }
    }
    return CONTINUE;
  }

  public int afterDeserialization(ConfigurationContext ctx) throws ProtocolException, MessageException {
    boolean[] tempArray = checkLog(Loc);    
    boolean debug = tempArray[0];    
    
    if (debug) {
      try {
        ClientConfigurationContext clientContext = (ClientConfigurationContext) ctx;

        LoggingTokenReader loggingReader = (LoggingTokenReader) clientContext.getDynamicContext().getProperty(INBOUND_LOG_READER);
        
        logInboundMessage(loggingReader, Loc);      

        // Remove it from the context to prevent its reusage in wrong context.
        clientContext.getDynamicContext().removeProperty(INBOUND_LOG_READER);
      } catch (IOException e) {
        throw new ProtocolException(e);
      }
    }
    return CONTINUE;
  }

  public int handleFault(ConfigurationContext context) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);    
    boolean debug = tempArray[0];    

    
    if (debug) {
      try {
        ClientConfigurationContext clientContext = (ClientConfigurationContext) context;

        Message message = clientContext.getMessage();

        if (message != null && message instanceof MIMEMessage) {
          MIMEMessage mimeMessage = (MIMEMessage) message;

          int state = mimeMessage.getMessageMode();

          if (state == MIMEMessage.BEFORE_READ || state == MIMEMessage.AFTER_READ) {
            // Read case
            LoggingTokenReader loggingReader = (LoggingTokenReader) clientContext.getDynamicContext().getProperty(INBOUND_LOG_READER);

            logInboundMessage(loggingReader, Loc);
            
            // Remove it from the context to prevent its reusage in wrong context.
            clientContext.getDynamicContext().removeProperty(INBOUND_LOG_READER);           
          } else {
            // Write case
            LoggingTokenWriter loggingWriter = (LoggingTokenWriter) clientContext.getDynamicContext().getProperty(
                OUTBOUND_LOG_WRITER);

            logOutboundMessage(loggingWriter, Loc);

            // Remove it from the context to prevent its reusage in wrong context.
            clientContext.getDynamicContext().removeProperty(OUTBOUND_LOG_WRITER);
          }
        }
      } catch (IOException e) {
        throw new ProtocolException(e);
      }
    }  
    return CONTINUE;
  }
  
    
  public void afterHibernation(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
  }

  public void beforeHibernation(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
  }

  public void finishHibernation(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
  }

  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
  }

}
