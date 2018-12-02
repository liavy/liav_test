/*
 * Created on Apr 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.interfaces.webservices.uddi;

import java.util.Properties;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface UDDIServer {
  /**
   * Drops existing tables and (re)creates an empty UDDI DB schema.
   * Except for the configuration parameters, which are kept.
   * @throws Exception if failing for any reason (eg DB unavailable)
   */
  public void resetDB() throws Exception;
  
  /**
   * Preloads base tModels and taxonomies (UNSPSC, ISO-3166, NAICS etc...)
   * DataBase should be reinitialized and empty (cf resetDB() ) before this service is called.
   * All xml files specified by a valid db.preload.url property are loaded
   * The db.preload.taxo.path property must specify where txt-based taxonomy files are installed.
   * @throws Exception if failing for any reason
   */
  public void preloadCanonicalData() throws Exception;
  
  /**
   * A method for getting all properties of the UDDI Server, so that they can be displayed in the Visual Administrator
   * @return The Properties 
   * @throws Exception if failing for any reason
   */
  public Properties getUDDIServerProperties() throws Exception;
  
  /**
   * A method for getting all the users
   * @return The UserAccounts filled in with the information from the UDDI DB
   * @throws Exception if failing for any reason 
   */
  public UserAccount[] getAllUsers() throws Exception;
  
  /**
   * A method for updating the details of a user
   * @param userAccount The new details
   * @throws Exception if failing for any reason
   */
  public void updateUserAccount(UserAccount userAccount) throws Exception;

  /**
   * A method for setting a specific UDDI property
   * @param key The key of the property
   * @param value The value of the property
   * @throws Exception if failing for any reason
   */
  public void setUDDIProperty(String key, String value) throws Exception;
  
  /**
   * Checks if base tModels and taxonomies (UNSPSC, ISO-3166, NAICS etc...) are loaded.
   * @throws Exception if failing for any reason
   */
  public boolean isCanonicalDataPreloaded() throws Exception;
}
