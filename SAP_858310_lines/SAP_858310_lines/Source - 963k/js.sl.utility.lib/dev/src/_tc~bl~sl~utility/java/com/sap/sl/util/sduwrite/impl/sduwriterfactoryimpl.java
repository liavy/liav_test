package com.sap.sl.util.sduwrite.impl;

import com.sap.sl.util.sduwrite.api.SduWriter;
import com.sap.sl.util.sduwrite.api.SduWriterFactory;

/**
 * @author d030435
 */

public class SduWriterFactoryImpl extends SduWriterFactory {
  public SduWriterFactoryImpl() {
  }
  public SduWriter createSduWriter() {
    return new SduWriterImpl();
  }
}
