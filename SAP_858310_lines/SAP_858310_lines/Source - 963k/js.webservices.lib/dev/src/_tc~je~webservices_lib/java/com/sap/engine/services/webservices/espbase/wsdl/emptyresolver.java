package com.sap.engine.services.webservices.espbase.wsdl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Empty EntityResolver to block .DTD handling and reading.
 * @author i024072
 *
 */
public class EmptyResolver implements EntityResolver {
  
  private static final byte[] EMPTY_ARR = new byte[0];
  public static final EmptyResolver EMPTY_RESOLVER = new EmptyResolver();
  
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    return new InputSource(new ByteArrayInputStream(EMPTY_ARR));
  }
  
}
