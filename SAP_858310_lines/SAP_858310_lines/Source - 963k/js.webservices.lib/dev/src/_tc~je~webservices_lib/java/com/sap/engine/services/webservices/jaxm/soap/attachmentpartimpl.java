package com.sap.engine.services.webservices.jaxm.soap;

import com.sap.engine.services.webservices.jaxm.soap.accessor.NestedSOAPException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.MessagingException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class AttachmentPartImpl extends AttachmentPart {

  MimeHeaders headers;
  private DataHandler dataHandler;

  public AttachmentPartImpl() {
    headers = new MimeHeaders();
  }

  public void addMimeHeader(String s, String s1) {
    headers.addHeader(s, s1);
  }

  public void clearContent() {
    dataHandler = null;
  }

  public Iterator getAllMimeHeaders() {
    return headers.getAllHeaders();
  }

  public Object getContent() throws SOAPException {
    try {
      if (dataHandler == null) {
        throw new NestedSOAPException(NestedSOAPException.EMPTY_ATTACHMENT);
      }
      if(dataHandler.getContent() instanceof String)
	  	return new ByteArrayInputStream(dataHandler.getContent().toString().getBytes()); //$JL-I18N$
	  else
	    return dataHandler.getContent();
    } catch (IOException ioexception) {
      throw new NestedSOAPException(NestedSOAPException.IO_PROBLEM,ioexception);
    }
  }

  public DataHandler getDataHandler() throws SOAPException {
    if (dataHandler == null) {
      throw new NestedSOAPException(NestedSOAPException.EMPTY_DATAHANDLER);
    }
    return dataHandler;
  }

  public Iterator getMatchingMimeHeaders(String as[]) {
    return headers.getMatchingHeaders(as);
  }

  public String[] getMimeHeader(String s) {
    return headers.getHeader(s);
  }

  public Iterator getNonMatchingMimeHeaders(String as[]) {
    return headers.getNonMatchingHeaders(as);
  }

  /**
   * Returns the size of the attachment content.
   * @return
   */
  public int getSize() throws SOAPException {
    if (dataHandler == null) {
      return 0; // no attachment
    } else {
      ByteArrayOutputStream perm = new ByteArrayOutputStream();
      try {
        dataHandler.writeTo(perm);
      } catch (IOException e) {
        throw new NestedSOAPException(NestedSOAPException.MIME_PART_PROBLEM,e);
        //e.printStackTrace();
        //return -1;
      }
      return perm.size();
    }
  }

  public void removeAllMimeHeaders() {
    headers.removeAllHeaders();
  }

  public void removeMimeHeader(String s) {
    headers.removeHeader(s);
  }

  public void setContent(Object obj, String s) throws IllegalArgumentException {
    if ("text/xml".equals(s) && obj instanceof StreamSource) {
      final StreamSource src = (StreamSource) obj;
      DataSource dsrc = new DataSource() {
        StreamSource x = src;
        public InputStream getInputStream()
          throws IOException {
           return src.getInputStream();
        }

        public OutputStream getOutputStream()
          throws IOException {
          return null;
        }

        public String getContentType() {
          return "text/xml";
        }

        public String getName() {
          return "No Name";
        }
      };
      setDataHandler(new DataHandler(dsrc));
    } else if (s.equals("image/gif")) {  
      final InputStream in = (InputStream) obj;
      DataSource dsrc = new DataSource() {
        //StreamSource x = src;
        public InputStream getInputStream()
          throws IOException {
           return in;
        }

        public OutputStream getOutputStream()
          throws IOException {
          return null;
        }

        public String getContentType() {
          return "image/gif";
        }

        public String getName() {
          return "No Name";
        }
      };
      setDataHandler(new DataHandler(dsrc));    
    } else if (s.equals("image/jpeg")) {
      final InputStream is = (InputStream) obj;
      DataSource dsrc = new DataSource() {
        public InputStream getInputStream()
          throws IOException {
           return is;
        }

        public OutputStream getOutputStream()
          throws IOException {
          return null;
        }

        public String getContentType() {
          return "image/jpeg";
        }

        public String getName() {
          return "No Name";
        }
      };
      setDataHandler(new DataHandler(dsrc));    
    } else {
      setDataHandler(new DataHandler(obj, s));
    }
  }

  public void setDataHandler(DataHandler datahandler) throws IllegalArgumentException {
    if (datahandler == null) {
      throw new IllegalArgumentException("NULL is not allowed as data handler.");
    }
    dataHandler = datahandler;
  }

  public void setMimeHeader(String s, String s1) {
    headers.setHeader(s, s1);
  }

  MimeBodyPart getMimePart() throws SOAPException {
    try {
      MimeBodyPart mimebodypart = new MimeBodyPart();
      mimebodypart.setDataHandler(dataHandler);
      copyMimeHeaders(headers, mimebodypart);
      mimebodypart.setHeader("Content-Type",dataHandler.getContentType());
      return mimebodypart;
    } catch (MessagingException e) {
      throw new NestedSOAPException(NestedSOAPException.MIME_PART_PROBLEM,e);
    }
  }

  static void copyMimeHeaders(MimeHeaders mimeheaders, MimeBodyPart mimebodypart) throws SOAPException {
    for (Iterator iterator = mimeheaders.getAllHeaders(); iterator.hasNext();) {
      try {
        MimeHeader mimeheader = (MimeHeader) iterator.next();
        mimebodypart.addHeader(mimeheader.getName(), mimeheader.getValue());
      } catch (MessagingException exception) {
        throw new NestedSOAPException(NestedSOAPException.MIME_PART_PROBLEM,exception);
      }
    } 
  }

  boolean hasAllHeaders(MimeHeaders mimeheaders) {
    for (Iterator iterator = mimeheaders.getAllHeaders(); iterator.hasNext();) {
      MimeHeader mimeheader = (MimeHeader) iterator.next();
      String as[] = headers.getHeader(mimeheader.getName());
      boolean flag = false;

      for (int i = 0; i < as.length; i++) {
        if (!mimeheader.getValue().equalsIgnoreCase(as[i])) {
          continue;
        }
        flag = true;
        break;
      } 

      if (!flag) {
        return false;
      }
    } 

    return true;
  }

  @Override
  public InputStream getBase64Content() throws SOAPException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getRawContent() throws SOAPException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getRawContentBytes() throws SOAPException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setBase64Content(InputStream content, String contentType) throws SOAPException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setRawContent(InputStream content, String contentType) throws SOAPException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setRawContentBytes(byte[] content, int offset, int len, String contentType) throws SOAPException {
    // TODO Auto-generated method stub
    
  } 

}

