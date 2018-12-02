package com.sap.sl.util.dbaccess.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sl.util.dbaccess.api.DBAccessFactory;
import com.sap.sl.util.dbaccess.api.DBTaskIF;
import com.sap.sl.util.dbaccess.api.DeploymentDescriptorIF;


/**
 *  Use this factory in order to create an conponent element instance, like:
 *  <ul>
 *    <li> a ComponentElement ({@link com.sap.components.api.ComponentElementIF}</li>
 *  </ul>
 *
 *@author     ua
 *@created    30. Juni 2004
 *@version    1.0
 */

public final class DBAccessFactoryImpl extends DBAccessFactory {

  public DBAccessFactoryImpl()
  {
  }
  
  /**
   *  Gets an instance of <code> DBTaskIF </code>
   *
   *@param connection an open jdbc vendor or openSQL connection    
   *@return    a <code> DBTaskIF </code> instance
   */						
  public DBTaskIF createDBTask(Connection connection)
  {
    return new DBTask(connection);
  }
  
  /**
   *  Gets an instance of <code> DBTaskIF </code>
   *  DBTask will create a venodr jdbc connection with the given parameters
   *
   * @param driver DB driver
   * @param url    DB url
   * @param user   DB user
   * @param passwd password for DB user    
   *@return    a <code> DBTaskIF </code> instance
   */
  public DBTaskIF createDBTask(String driver, String url, String user, String passwd) throws SQLException, ClassNotFoundException
  {
    return new DBTask(driver,url,user,passwd);
  }

  /**
   *  Gets an instance of <code> DeploymentDescriptorIF </code>
   *
   *@param archivename
   *qparam ddfilename   
   *@return    a <code> DeploymentDescriptorIF </code> instance
   */ 
  public  DeploymentDescriptorIF createDeploymentDescriptor (String archiveName, String ddFileName) throws java.io.IOException
  {
    return new DeploymentDescriptor(archiveName, ddFileName);
  }

}
