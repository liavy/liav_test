package com.sap.engine.services.webservices.espbase.server.logging;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.logging.LoggingTokenReader;
import com.sap.engine.services.webservices.espbase.messaging.logging.LoggingTokenWriter;
import com.sap.engine.services.webservices.espbase.messaging.logging.ProtocolMessageLogger;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.tc.logging.Location;

/**
 * 
 * @author I056242
 * 
 * The provider logging protocol is designed to perform soap message log
 * driven by the protocol events. The protocol must always be places
 * behind the attachment and security protocol in the inbound case
 * and before them in the outbound case.
 * 
 */
public class ProviderLoggingProtocol extends ProtocolMessageLogger implements ProviderProtocol, ProtocolExtensions {

  private static Location Loc = Location.getLocation(ProviderLoggingProtocol.class);

  public static String NAME = "ProviderLoggingProtocol";

  public String getProtocolName() {
    return NAME;
  }

  //TODO: This event in not called on server side due to design reasons. 
  // The logic must be redesigned and this method should handle the faults.
  public int handleFault(ConfigurationContext context) throws ProtocolException {
    return CONTINUE;
  }

  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    boolean[] tempArray = checkLog(Loc);
    boolean debug = tempArray[0];
    boolean hideValues = tempArray[1];

    if (debug) {
      try {
        ProviderContextHelper providerContext = (ProviderContextHelper) context;

        Message message = providerContext.getMessage();

        if (message != null && message instanceof MIMEMessage) {
          MIMEMessage mimeMessage = (MIMEMessage) message;

          prepareInboundLog(mimeMessage, hideValues);

          providerContext.getDynamicContext().setProperty(INBOUND_LOG_READER, mimeMessage.getBodyReader());
        }
      } catch (Exception e) {
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
        ProviderContextHelper providerContext = (ProviderContextHelper) ctx;

        LoggingTokenReader loggingReader = (LoggingTokenReader) providerContext.getDynamicContext().getProperty(
            INBOUND_LOG_READER);

        // Log the already sent outbound message.
        logInboundMessage(loggingReader, Loc);
        
        // Remove it from the context to prevent its reusage in wrong context.
        providerContext.getDynamicContext().removeProperty(INBOUND_LOG_READER);
      } catch (Exception e) {
        throw new ProtocolException(e);
      }
    }

    return CONTINUE;
  }

  public void beforeSerialization(ConfigurationContext ctx) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);
    boolean debug = tempArray[0];
    boolean hideValues = tempArray[1];

    if (debug) {
      try {
        ProviderContextHelper providerContext = (ProviderContextHelper) ctx;

        Message message = providerContext.getMessage();

        if (message != null && message instanceof MIMEMessage) {
          MIMEMessage mimeMessage = (MIMEMessage) message;

          prepareOutboundLog(mimeMessage, hideValues);

          providerContext.getDynamicContext().setProperty(OUTBOUND_LOG_WRITER, mimeMessage.getBodyWriter());
        }
      } catch (Exception e) {
        throw new ProtocolException(e);
      }
    }
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);
    boolean debug = tempArray[0];

    if (debug) {
      try {
        ProviderContextHelper providerContext = (ProviderContextHelper) context;

        // loggingWriter will be null in the wsrm case. The message is already
        // logged in finishMessageDeserialization event.
        LoggingTokenWriter loggingWriter = (LoggingTokenWriter) providerContext.getDynamicContext().getProperty(
            OUTBOUND_LOG_WRITER);

        logOutboundMessage(loggingWriter, Loc);
        
        // Remove it from the context to prevent its reusage in wrong context.
        providerContext.getDynamicContext().removeProperty(OUTBOUND_LOG_WRITER);        
      } catch (Exception e) {
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

  /**
   * WSRM Case logging.
   * Invoked when the context is beaing hibernated.
   */
  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException {
    boolean[] tempArray = checkLog(Loc);
    boolean debug = tempArray[0];

    if (debug) {
      try {
        ProviderContextHelper providerContext = (ProviderContextHelper) ctx;

        LoggingTokenReader loggingReader = (LoggingTokenReader) providerContext.getDynamicContext().getProperty(
            INBOUND_LOG_READER);

        // Log the already sent outbound message.
        logInboundMessage(loggingReader, Loc);
        
        // Remove it from the context to prevent its reusage in wrong context.
        providerContext.getDynamicContext().removeProperty(INBOUND_LOG_READER);
      } catch (Exception e) {
        throw new ProtocolException(e);
      }
    }
  }

}
