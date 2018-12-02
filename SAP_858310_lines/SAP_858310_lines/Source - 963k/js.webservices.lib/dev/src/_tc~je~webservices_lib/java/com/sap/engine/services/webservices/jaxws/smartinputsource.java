/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */


package com.sap.engine.services.webservices.jaxws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.InputSource;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 *
 * This class exists only because the RI XJC (Schema to JAVA) JAXB compiler does not reset the ByteArrayInputStream
 * after it has read it once and before the real parsing is done. Thus, the second time it tries to read the schema, it fails
 * with the SAX "Premature End of File" Exception. This class overrides the "getByteStream" method to prevent this. 
 * Should a future JAXB implementation fix its own behavior, this class might become unncessary
 *
 */
public class SmartInputSource extends InputSource {

  /**
   * 
   */
  public SmartInputSource() {
    super();
  }

  /**
   * @param systemId
   */
  public SmartInputSource(String systemId) {
    super(systemId);
  }

  /**
   * @param byteStream
   */
  public SmartInputSource(InputStream byteStream) {
    super(byteStream);
  }

  /**
   * @param characterStream
   */
  public SmartInputSource(Reader characterStream) {
    super(characterStream);
  }

  /* (non-Javadoc)
   * @see org.xml.sax.InputSource#getByteStream()
   */
  @Override
  public InputStream getByteStream() {
    
    InputStream inS = super.getByteStream();
    if(inS instanceof ByteArrayInputStream){
      ((ByteArrayInputStream)inS).reset();
    }
    
    return inS;
  }

  
  
}
