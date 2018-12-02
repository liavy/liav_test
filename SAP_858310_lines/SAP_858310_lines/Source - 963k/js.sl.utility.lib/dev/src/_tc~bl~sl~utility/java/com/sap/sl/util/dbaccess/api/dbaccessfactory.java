package com.sap.sl.util.dbaccess.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sl.util.loader.Loader;

/**
 *  Use this factory in order to create a {@link ComponentElementIF} instance.
 *
 *@author     md
 *@created    26. Juni 2003
 *@version    1.0
 */

public abstract class DBAccessFactory
{
  private static DBAccessFactory INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS = "com.sap.sl.util.dbaccess.impl.DBAccessFactoryImpl";

  /**
   * Gets an instance of <code> DBAccessFactory. </code>
   * <p>
   * If you want a special class loader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return A <code> DBAccessFactory </code> instance
   *@see  java.lang.ClassLoader
   */
  public static DBAccessFactory getInstance()
  {
    if (null == DBAccessFactory.INSTANCE)
    {
      DBAccessFactory.INSTANCE = (DBAccessFactory)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }

  /**
   *  Gets an instance of <code> DBTaskIF </code>
   *
   *@param connection an open jdbc vendor or openSQL connection    
   *@return    a <code> DBTaskIF </code> instance
   */						
  public abstract DBTaskIF createDBTask(Connection connection);
  
  /**
   *  Gets an instance of <code> DBTaskIF </code>
   *  DBTask will create a vendor jdbc connection with the given parameters
   *
   * @param driver DB driver
   * @param url    DB url
   * @param user   DB user
   * @param passwd password for DB user    
   *@return    a <code> DBTaskIF </code> instance
   */           
  public abstract DBTaskIF createDBTask(String driver, String url, String user, String passwd) throws SQLException, ClassNotFoundException;

  /**
   *  Gets an instance of <code> DeploymentDescriptorIF </code>
   *
   *@param archivename
   *qparam ddfilename   
   *@return    a <code> DeploymentDescriptorIF </code> instance
   */ 
  public abstract DeploymentDescriptorIF createDeploymentDescriptor (String archiveName, String ddFileName) throws java.io.IOException;

}
