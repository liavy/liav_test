package com.sap.sl.util.cvers.api;

import javax.sql.DataSource;

import com.sap.sl.util.loader.Loader;

/**
 *  Use this factory in order to get the central CVERS object {@link CVersManagerIF}, 
 *  to read, write and remove CVERS entries.
 *
 *@author     md
 *
 *@version    1.0
 */

public abstract class CVersFactoryIF {
  private static CVersFactoryIF INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS
    = "com.sap.sl.util.cvers.impl.CVersFactory";

  /**
   * Gets an instance of <code> CVERSFactoryIF. </code>
   * <p>
   * If you want a special @see java.lang.ClassLoader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return    A <code> CVersFactoryIF </code> instance
   */
  public static CVersFactoryIF getInstance() {
    if (null == CVersFactoryIF.INSTANCE) {
      CVersFactoryIF.INSTANCE = 
        (CVersFactoryIF)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }

  /**
   *  Creates an instance of <code> CVERSManagerIF </code> (to read and write CVERS)
   *
   *@return    A <code> CVersManagerIF </code> instance
   */
  public abstract CVersManagerIF createCVersManager() throws CVersAccessException;

  /**
   *  Creates an instance of <code> CVERSManagerIF </code> (to read and write CVERS)
   *
   *@param  dataSource  DataSource containing a open-SQL-connection to the database
   *@return    A <code> CVersManagerIF </code> instance
   */
  public abstract CVersManagerIF createCVersManager(DataSource dataSource)
		throws CVersAccessException;

}
