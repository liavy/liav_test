package com.sap.sl.util.sduwrite.api;

import com.sap.sl.util.loader.Loader;

/**
 * @author d030435
 */

public abstract class SduWriterFactory {
  private static SduWriterFactory INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS
    = "com.sap.sl.util.sduwrite.impl.SduWriterFactoryImpl";
  
  /**
   * Gets an instance of <code> SduWriterFactory. </code>
   * <p>
   * If you want a special class loader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return A <code> SduWriterFactory </code> instance
   *@see  java.lang.ClassLoader
   */
  
  public static SduWriterFactory getInstance() {
    if (null == SduWriterFactory.INSTANCE) {
      SduWriterFactory.INSTANCE 
        = (SduWriterFactory)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }

  /**
   *  Gets an instance of <code> SduWriter </code>
   *
   *@return    A <code> SduWriter </code> instance
   */           
  public abstract SduWriter createSduWriter();
}