package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLDataContentHandler implements DataContentHandler {
  private static ActivationDataFlavor myDF = new ActivationDataFlavor(StreamSource.class,
      "text/xml", "XML Stream");

  protected ActivationDataFlavor getDF() {
    return myDF;
  }

  public Object getContent(DataSource ds) throws IOException {
    InputStream is = ds.getInputStream();
    ByteArrayInputStream result = null;
    try {
      int pos = 0;
      int count;
      byte buf[] = new byte[1024];
      while ((count = is.read(buf, pos, buf.length - pos)) != -1) {
        pos += count;
        if (pos >= buf.length) {
          int size = buf.length;
          if (size < 256 * 1024)
            size += size;
          else
            size += 256 * 1024;
          byte tbuf[] = new byte[size];
          System.arraycopy(buf, 0, tbuf, 0, pos);
          buf = tbuf;
        }
      }
      result = new ByteArrayInputStream(buf,0,pos);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new StreamSource(ds.getInputStream());
  }

  public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException,
      IOException {
    if (getDF().equals(df)) {
      return getContent(ds);
    } else {
      return null;
    }
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { getDF() };
  }

  private Transformer getTransformer() throws IOException {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer tr = factory.newTransformer();
      tr.setOutputProperty(OutputKeys.INDENT, "no");
      return tr;
    } catch (TransformerConfigurationException e) {
      throw new IOException("Unable to obtain transformer instance." + e.getMessage());
    }
  }

  public void writeTo(Object obj, String type, OutputStream os) throws IOException {
    if (!(obj instanceof StreamSource))
      throw new IOException("\"" + getDF().getMimeType()
          + "\" DataContentHandler requires StreamSource object, was given object of type "
          + obj.getClass().toString());
    try {
      Transformer tr = getTransformer();
      StreamResult result = new StreamResult(os);
      tr.transform((StreamSource) obj, result);
      os.flush();
    } catch (TransformerException x) {
      throw new IOException("Unable to convert StreamSource to byte stream." + x.getMessage());
    } finally {
      // TODO: Implement transformer pool
    }
  }

  private String getCharset(String type) {
    try {
      ContentType ct = new ContentType(type);
      String charset = ct.getParameter("charset");
      if (charset == null)
        // If the charset parameter is absent, use US-ASCII.
        charset = "us-ascii";
      return MimeUtility.javaCharset(charset);
    } catch (Exception ex) {
      return null;
    }
  }
}
