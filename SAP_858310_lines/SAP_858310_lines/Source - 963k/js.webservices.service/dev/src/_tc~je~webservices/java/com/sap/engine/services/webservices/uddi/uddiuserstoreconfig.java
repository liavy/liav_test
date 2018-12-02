/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.services.webservices.uddi;

import java.util.Properties;

import com.sap.engine.interfaces.security.userstore.config.LoginModuleConfiguration;
import com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UDDIUserStoreConfig implements UserStoreConfiguration {
  public static final String USER_STORE_NAME = "UDDI";
  public static final String UDDI_CONFIG_NAME = "UDDI_SERVER_USERSTORE";
  private Properties props = new Properties();

  public UDDIUserStoreConfig() {
    props.put("configuration.path", UDDI_CONFIG_NAME);//Users will be saved under this root configuration
    props.put("RETRIEVE_PASSWORDS", "false");//If "false" then passwords will be hashed using MD5. If it is "true" then a cipher will be needed in order to encode/decode passwords, IAIK will be needed too
//  	props.put("userAdmin.name", "Administrator");
//  	props.put("userAdmin.isUser", "true");
//  	props.put("userAdmin.password", "test");
//  	props.put("userGuest.name", "Guest");
//  	props.put("userGuest.isUser", "true");
//  	props.put("userGuest.password", "test");
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getDescription()
   */
  public String getDescription() {
    return "UDDI Server Userstore";
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getName()
   */
  public String getName() {
    return USER_STORE_NAME;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getAnonymousUser()
   */
  public String getAnonymousUser() throws SecurityException {
    return NONE_ANONYMOUS_USER;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getLoginModules()
   */
  public LoginModuleConfiguration[] getLoginModules() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getUserSpiClassName()
   */
  public String getUserSpiClassName() {
    return "com.sap.engine.services.userstore.UserContextImpl";
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getGroupSpiClassName()
   */
  public String getGroupSpiClassName() {
    return "com.sap.engine.services.userstore.GroupContextImpl";
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getUserStoreProperties()
   */
  public Properties getUserStoreProperties() {
    return props;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.security.userstore.config.UserStoreConfiguration#getConfigurationEditorClassName()
   */
  public String getConfigurationEditorClassName() {
    return null;
  }
}
