package com.sap.sl.util.sduread.api;

import com.sap.sl.util.loader.Loader;

/**
 * @author d030435
 */

public abstract class SduReaderFactory {
  private static SduReaderFactory INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS
    = "com.sap.sl.util.sduread.impl.SduReaderFactoryImpl";

  /**
   * Gets an instance of <code> SduReaderFactory. </code>
   * <p>
   * If you want a special class loader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return A <code> SduReaderFactory </code> instance
   *@see  java.lang.ClassLoader
   */
  public static SduReaderFactory getInstance() {
    if (null == SduReaderFactory.INSTANCE) {
      SduReaderFactory.INSTANCE 
        = (SduReaderFactory)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }

  /**
   *  Gets an instance of <code> SduReader </code>
   *
   *@return    A <code> SduReader </code> instance
   */           
  public abstract SduReader createSduReader();
  
  /**
   *  Gets an instance of <code> VersionFactory </code>
   *
   *@return    A <code> VersionFactory </code> instance
   */           
  public abstract VersionFactoryIF getVersionFactory();
  
  /**
   *  Creats an instance of <code> SduSelectionEntry </code>. It is possible to use '*' at the end
   *  of the value parameter in order to define a generic value definition.
   *  For example:
   *               value='ABC*' fits to ABC, ABCA but not to AB, ABE.
   * 
   * @param selectionattribute the name of the selection attribute 
   * @param value the value of the selection attribute
   * @return {@link SduSelectionEntry}
   */
  public abstract SduSelectionEntry createSduSelectionEntry(String selectionattribute, String value);
}
