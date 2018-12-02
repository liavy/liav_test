package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.SduReader;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.SduSelectionEntry;
import com.sap.sl.util.sduread.api.VersionFactoryIF;

/**
 * @author d030435
 */

public class SduReaderFactoryImpl extends SduReaderFactory {
  public SduReaderFactoryImpl() {
  }
  public SduReader createSduReader() {
    return new SduReaderImpl();
  }
  public VersionFactoryIF getVersionFactory() {
    return new VersionFactory();
  }
  public SduSelectionEntry createSduSelectionEntry(String selectionattribute, String value) {
    return new SduSelectionEntryImpl(selectionattribute,value);
  }
}
