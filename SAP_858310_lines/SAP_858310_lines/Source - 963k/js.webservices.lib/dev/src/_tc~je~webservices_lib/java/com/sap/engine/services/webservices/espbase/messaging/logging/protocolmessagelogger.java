package com.sap.engine.services.webservices.espbase.messaging.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.sap.engine.interfaces.sca.logtrace.CallEntry.TraceLevel;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;

/**
 * 
 * @author I056242
 * Superclass of the message logging protocols.
 * The actual logging functionality is places here and is
 * reused in its children.
 */
public abstract class ProtocolMessageLogger {
  
  protected final static String OUTBOUND_LOG_WRITER = "outboundLoggingTokenWriter";

  protected final static String INBOUND_LOG_READER = "inboundLoggingTokenReader";

  /**
   * Replaces the body token writer the logger instance.
   * @param message
   * @param loc
   * @param hideValues
   * @throws IOException
   */
  protected void prepareOutboundLog(MIMEMessage message, boolean hideValues) throws IOException {

    LoggingTokenWriter loggingWrapperWriter = new LoggingTokenWriter(message, LoggingTokenUtil.getPredefinedNamespaces(message), hideValues);

    message.replaceBodyWriter(loggingWrapperWriter);

  }

  /**
   * If the LoggingTokenWriter is present - flush and log the outbound message
   * to the passed location.
   * 
   * @param loggingWriter
   * @param loc
   * @throws IOException
   */
  protected void logOutboundMessage(LoggingTokenWriter loggingWriter, Location loc) throws IOException {
    if (loggingWriter != null){
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      loggingWriter.flush();
      
      loggingWriter.dumpPayload(baos);

      LogRecord logRecord = loc.debugT(baos.toString("UTF-8"));
      
      recordLogID(logRecord, false);
    }
  }

  /**
   * Replace the body token reader with the logger instance.
   * @param message
   * @param hideValues hide business values?
   * @throws IOException
   */
  protected void prepareInboundLog(MIMEMessage message, boolean hideValues) throws IOException {
    XMLTokenReader bodyReader = message.getBodyReader();

    LoggingTokenReader loggingWrapperReader = new LoggingTokenReader(bodyReader, message, LoggingTokenUtil.getPredefinedNamespaces(message), hideValues, LoggingTokenReader.ENVELOPE_PROCESSING);

    // Init the new wrapper reader here.  
    message.replaceBodyReader(loggingWrapperReader);
  }

  /**
   * If the LoggingTokenReader is present dump the envelope to the
   * given location.
   * @param loggingReader
   * @param loc
   * @throws IOException
   */
  protected void logInboundMessage(LoggingTokenReader loggingReader, Location loc) throws IOException {
    if (loggingReader != null){
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      loggingReader.dumpEnvelope(baos);

      LogRecord logRecord = loc.debugT(baos.toString("UTF-8"));
                             
      recordLogID(logRecord, true);   
    }
  }
  
  
  /**
   * If the LoggingTokenReader is present read the message to its end(is it very likely 
   * that the processing is stopped before the message is read) and dump it to
   * the given location.
   * @param loggingReader
   * @param loc
   * @throws IOException
   * @throws ParserException
   */
  protected void logInboundFault(LoggingTokenReader loggingReader, Location loc) throws IOException, ParserException {
    if (loggingReader != null){
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      // Read the reader till it's end when something goes wrong
      while(loggingReader.getState() != XMLTokenReader.EOF){
        loggingReader.next();
      }
      
      loggingReader.dumpEnvelope(baos);

      LogRecord logRecord = loc.debugT(baos.toString("UTF-8"));
                             
      recordLogID(logRecord, true);   
    }
  } 
  
  
  /**
   * Add the logRecord to the ESBTracer statistics    
   * @param logRecord
   * @param inbound is the log inbound message?
   */
  private void recordLogID(LogRecord logRecord, boolean inbound){
    // It is possible for the logRecord to be null.
    if (logRecord == null){           
      return;
    }
    if (inbound) {
      WSLogTrace.setInboundPayloadTraceID(logRecord.getId().toString());
    } else {
      WSLogTrace.setOutboundPayloadTraceID(logRecord.getId().toString());
    }

  }
    
  
  
  
  
  protected boolean[] checkLog(Location loc){
    boolean debug = false;
    boolean hideValues = false;

    TraceLevel level = WSLogTrace.getTraceLevel();

    if (level.equals(TraceLevel.NONE)){
      //configuration was not found or standalone case - use the old way (just check the location)
      debug = loc.beDebug();        
      hideValues = true;              
    }else if (level.equals(TraceLevel.LOW)){
      debug = false;        
      hideValues = false;                     
    }else if (level.equals(TraceLevel.MEDIUM)){
      debug = true;        
      hideValues = true;          
    }else if (level.equals(TraceLevel.HIGH)) {
      debug = true;        
      hideValues = false;       
    }   

    return new boolean[]{debug,hideValues};
  }
  

}
