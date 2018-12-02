package com.sap.engine.services.webservices.espbase.messaging.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Set;
import org.w3c.dom.Element;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderDOMFactory;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;

abstract class AbstractLoggingTokenizer {
  
  private final static String ATTACHMENT_LOG_BORDER = "------------Attchment------------";
  
  private final static String CONTENT_TYPE = "Content-Type:";
  
  private final static String CONTENT_ID = "Content-Id:";
  
 
  protected MIMEMessage message;
  
  protected ByteArrayOutputStream bodyBuffer = null;
  
  protected Hashtable predefinedNS;   
  
  /** Indicates if the business values must be hidden or not. */
  protected boolean modifyValue = false;
  
  
  /**
   * Dumps the logged element.
   * @param os
   * @throws IOException
   */
  public abstract void dump(OutputStream os) throws IOException;


  /**
   * Dumps the logged soap:envolope containing headers,body elements and attachments.
   * @param output
   * @throws IOException
   */
  public abstract void dumpPayload(OutputStream output) throws IOException; 
  
  
  
  
  protected void dumpEnvelope(OutputStream output) throws IOException {
    if (output != null) {
      LoggingTokenUtil.outputEnvelopeOpen(this.message, output);
 
      SOAPHeaderList list = message.getSOAPHeaders();
      if (list.size() > 0) {
        LoggingTokenUtil.outputHeaderOpenTag(this.message, output);
        Element[] headerElements = list.getHeaders();
        for (int i = 0; i < headerElements.length; i++) {
          try {
            serializeHeader(headerElements[i], output);
            // serializer.write(headerElements[i], output);
          } catch (Exception e) {
            throw new IOException("Unable to serialize SOAPHeader content into SOAPMessage.", e);
          }
        }
        LoggingTokenUtil.outputHeaderCloseTag(this.message, output);
      }

      // write the actual content.
      bodyBuffer.writeTo(output);

      LoggingTokenUtil.outputEnvelopeClose(this.message, output);
    }
  }
  
  
  protected void dumpAttachments(OutputStream output) throws IOException {
    if (output != null) {
      AttachmentContainer attachmentContainer = message.getAttachmentContainer();

      Set<Attachment> attachments = attachmentContainer.getAttachments();

      String temp = null;
      for (Attachment att : attachments) {
        StringBuilder builder = new StringBuilder("\n\n");
        builder.append(ATTACHMENT_LOG_BORDER);
        builder.append("\n");
        builder.append(CONTENT_TYPE);
        builder.append(att.getContentType());
        builder.append("\n");
        builder.append(CONTENT_ID);
        builder.append(att.getContentId());
        temp = builder.toString();
        output.write(temp.getBytes()); // $JL-I18N$
      }
    }
  }
  
  

  private void serializeHeader(Element headerElement, OutputStream output) throws IOException, ParserException {

    XMLTokenReader domReader = XMLTokenReaderDOMFactory.newInstance(headerElement);

    domReader.begin();

    LoggingTokenReader loggingReader = new LoggingTokenReader(domReader, message, predefinedNS, modifyValue, LoggingTokenReader.ELEMENT_PROCESSING);

    while (loggingReader.getState() != XMLTokenReader.EOF) {
      loggingReader.next();
    }

    loggingReader.dump(output);
  }
  
  
  
  
}
