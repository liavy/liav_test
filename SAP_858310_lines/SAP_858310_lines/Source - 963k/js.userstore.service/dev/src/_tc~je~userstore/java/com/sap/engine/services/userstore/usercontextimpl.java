/**
 * Copyright:    2002 by SAP AG
 * Company:      SAP AG, http://www.sap.com
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore;

import java.util.*;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;
import javax.resource.spi.security.PasswordCredential;
import javax.crypto.Cipher;

import com.sap.engine.interfaces.keystore.Constants;
import com.sap.engine.interfaces.security.userstore.listener.UserListener;
import com.sap.engine.interfaces.security.userstore.spi.FilterPassword;
import com.sap.engine.interfaces.security.userstore.spi.FilterUsername;
import com.sap.engine.interfaces.security.userstore.spi.UserContextSpi;
import com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi;
import com.sap.engine.interfaces.security.userstore.context.UserContext;
import com.sap.engine.interfaces.security.userstore.context.SearchFilter;
import com.sap.engine.interfaces.security.userstore.context.SearchResult;
import com.sap.engine.interfaces.security.CryptographyContext;
import com.sap.engine.services.userstore.filter.FilterStorage;
import com.sap.engine.services.userstore.filter.FilterUsernameImpl;
import com.sap.engine.services.userstore.filter.FilterPasswordImpl;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;
import com.sap.engine.services.userstore.search.SearchFilterImpl;
import com.sap.engine.services.userstore.search.UserSearchResultImpl;
import com.sap.engine.frame.core.configuration.*;
import com.sap.tc.logging.Severity;

/**
 */
public class UserContextImpl implements UserContextSpi {

  public final static int PROPERTY_PASSWORD = 9; //int

  protected ConfigurationFactory factory = null;
  protected TreeManager treeManager = null;
  protected FilterStorage filterStorage = null;

  public static final String USERS_CONFIG_PATH = "users";
  public static final String CONFIG_PATH = "configuration.path";
  private String configPath = null;
  private String locker = "";

  private MessageDigest digest = null;
  private CryptographyContext crypt = null;

  protected boolean retrievePasswords = false;
  protected int lock_after_invalid_attempts = 3;
  
	private String anonymousUserName = null;

  public UserContextImpl() {
    this.factory = new ConfigurationFactory();
    this.treeManager = new TreeManager(factory);
    filterStorage = new FilterStorage();
  }

  public void setLocker(String lock) {
    this.locker = (lock != null && lock.length() > 0) ? lock : "";
  }

  public String getLocker() {
    return locker;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#enginePropertiesChanged(Properties)
   */
  public synchronized void  enginePropertiesChanged(Properties newProps) {
    String propPassword = newProps.getProperty("RETRIEVE_PASSWORDS");
    if (propPassword != null) {
      retrievePasswords = propPassword.equals("true");
    }

    try {
      lock_after_invalid_attempts = Integer.parseInt(newProps.getProperty("LOCK_AFTER_INVALID_ATTEMPTS"));
    } catch (Exception _) {
      lock_after_invalid_attempts = 3;
    }

    String expirationPasswords = newProps.getProperty("EXPIRATION_PASSWORDS");
    try {
      UserInfoImpl.defaultExpiration = Long.parseLong(expirationPasswords);
    } catch (Exception ex) {
      UserInfoImpl.defaultExpiration = -1;
    }

    configPath = newProps.getProperty(CONFIG_PATH);
    if (configPath != null && configPath.length() > 0) {
      factory.setPath(configPath);
    }
    
		anonymousUserName = newProps.getProperty("anonymous-user");

    String[][] users = (String[][]) PropertiesParser.parseUsersAndGroups(newProps).elementAt(0);
    ConfigurationHandler configHandler = factory.getConfigurationHandler();
    Configuration rootConfiguration = null;
    setLocker(configPath);
    UserStoreServiceFrame.lock(locker);
    UserStoreServiceFrame.lock(locker + USERS_CONFIG_PATH);
    try {
      rootConfiguration = factory.createRootConfiguration(configHandler);
      rootConfiguration.createSubConfiguration(USERS_CONFIG_PATH);
      Configuration filterConfiguration = rootConfiguration.createSubConfiguration(FilterStorage.FILTER_CONFIG_PATH);
      new FilterUsernameImpl(filterConfiguration.createSubConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH.substring(FilterStorage.USER_FILTER_CONFIG_PATH.indexOf("/") + 1)), true);
      new FilterPasswordImpl(filterConfiguration.createSubConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH.substring(FilterStorage.PASSWORD_FILTER_CONFIG_PATH.indexOf("/") + 1)), true);
      setInitialUsers(users, rootConfiguration);
      factory.commit(rootConfiguration, configHandler);
    } catch (NameAlreadyExistsException _) {
      factory.rollback(rootConfiguration, configHandler);
      return;
    } catch (Exception e) {
      factory.rollback(rootConfiguration, configHandler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_INITIALIZE_USER_CONTEXT_SPI, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + USERS_CONFIG_PATH);
      UserStoreServiceFrame.releaseLock(locker);
    }
  }

  private void setInitialUsers(String[][] users, Configuration config) throws Exception {
    factory.setTransactionAttribute(config);
    try {
      for (int i = 0; i < users.length; i++) {
        try {
          engineGetUserInfo(users[i][0]);
        } catch (SecurityException se) {
          UserInfoSpi spi = null;
          try {
            spi = engineCreateUser(users[i][0]);
            spi.engineWriteUserProperty(PROPERTY_PASSWORD, users[i][1].toCharArray());
          } catch (Exception e) {
            UserStoreServiceFrame.logWarning("Cannot create user \"" + users[i][0] + "\".", e);
          }

          if (spi != null && users[i][3] != null && users[i][3].equals("true")) {
            try {
              spi.engineWriteUserProperty(UserContext.PROPERTY_LOCK_STATUS, new Integer(UserContext.LOCKED_BY_ADMIN));
            } catch (SecurityException e) {
              throw new BaseSecurityException(BaseSecurityException.CANNOT_LOCK_USER, new Object[] {users[i][0]}, e);
            }
          }
        }
      }
    } finally {
      factory.setTransactionAttribute(null);
      if (configPath != null && configPath.length() > 0) {
        factory.setPath(configPath);
      }
    }
  }

  public Iterator engineGetParentGroups(String userName) throws SecurityException {
    ConfigurationHandler handler = null;
    Configuration configuration = null;
    boolean exception = true;

    do {
      try {
        handler = factory.getConfigurationHandler();
        configuration = factory.getConfiguration(USERS_CONFIG_PATH + "/" + userName, false, handler);
        return treeManager.getParents(userName, configuration);
      } catch (InconsistentReadException ire) {
        //the method waits until the finish of the write-operation over this configuration
        continue;
      } catch (Exception e) {
        throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_PARENT_GROUPS, e);
      } finally {
        factory.close(configuration, handler);
      }
    } while (exception);

    return null;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineListUsers()
   */
  public Iterator engineListUsers() throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(USERS_CONFIG_PATH, false, handler);
      return configuration.getAllSubConfigurations().keySet().iterator();
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LIST_USERS, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineSearchUsers(SearchFilter filter)
   */
  public SearchResult engineSearchUsers(SearchFilter filter) throws SecurityException {
    int resultSize = filter.getMaxSearchResultSize();
    Iterator attributes = filter.getSearchAttributes();
    if (attributes.hasNext()) {
      return new UserSearchResultImpl(this, attributes, resultSize);
    } else {
      throw new BaseSecurityException(BaseSecurityException.NO_SEARCH_CRITERIA_IS_SET);
    }
  }

  /**
   *  List the names of the root users.
   *
   * @return  iterator with the names of the users , which don't have parent groups.
   */
  public java.util.Iterator engineListRootUsers() {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(USERS_CONFIG_PATH, false, handler);
      return treeManager.listRoots(configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LIST_ROOT_USERS, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetUserInfo(String)
   */
  public UserInfoSpi engineGetUserInfo(String userName) throws SecurityException {
    ConfigurationHandler handler = null;
    Configuration configuration = null;
    boolean exception = true;

    do {
      try {
        handler = factory.getConfigurationHandler();
        configuration = factory.getConfiguration(USERS_CONFIG_PATH + "/" + userName, false, handler);
        UserInfoSpi info = new UserInfoImpl(configuration, false);
        ((UserInfoImpl) info).setUserContext(this);
        return info;
      } catch (InconsistentReadException ire) {
        //the method waits until the finish of the write-operation over this configuration
        continue;
      } catch (Exception e) {
        throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_USER_INFO, e);
      } finally {
        factory.close(configuration, handler);
      }
    } while (exception);

    return null;
  }


  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetUserInfo(X509Certificate)
   */
  public UserInfoSpi engineGetUserInfo(X509Certificate cert) throws SecurityException {
    String alias = null;

    try {
      alias = UserStoreServiceFrame.getKeystoreInterface().getKeystore(Constants.KV_ALIAS_DBMS_USERS).getCertificateAlias(cert);
    } catch (Exception e) {
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_FIND_KEYSTORE_VIEW, new Object[] {Constants.KV_ALIAS_DBMS_USERS}, e);
      throw bse;
    }

    if (alias != null) {
      return engineGetUserInfo(alias.substring(0, alias.lastIndexOf(UserInfoImpl.SEPARATOR)));
    } else {
      throw new BaseSecurityException(BaseSecurityException.USER_NOT_CORRESPONDING_TO_CERT);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetUserInfoByLogonAlias(String)
   */
  public UserInfoSpi engineGetUserInfoByLogonAlias(String alias) throws SecurityException {
    return engineGetUserInfo(alias);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineCreateUser(String)
   */
  public UserInfoSpi engineCreateUser(String userName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;

    try {
      configuration = factory.getConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH, false, handler);
      if (!filterStorage.getFilterUsername(configuration).filterUsername(userName)) {
        throw new BaseSecurityException(BaseSecurityException.UNACCEPTABLE_USER_NAME, new Object[] {userName});
      }
    } catch (NameNotFoundException e) {
      UserStoreServiceFrame.logError("No configuration of user name filter found!");
    } finally {
      factory.close(configuration, handler);
    }

    handler = factory.getConfigurationHandler();
    UserStoreServiceFrame.lock(locker + USERS_CONFIG_PATH);

    try {
      configuration = factory.getConfiguration(USERS_CONFIG_PATH, true, handler);
      Configuration userConfiguration = configuration.createSubConfiguration(userName);
      UserInfoSpi info = new UserInfoImpl(userConfiguration, true);
      treeManager.createUser(userName, true, userConfiguration);

      factory.commit(configuration, handler);
      ((UserInfoImpl) info).setUserContext(this);
      UserStoreServiceFrame.logNotice("User \"" + userName + "\" created successfully.");
      return info;
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_CREATE_USER, Severity.WARNING, new Object[] {userName}, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + USERS_CONFIG_PATH);
    }
  }
  
  /**
	 * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineCreateUser(String, char[])
	 */
  public UserInfoSpi engineCreateUser(String userName, char[] password) throws SecurityException {
		ConfigurationHandler handler = factory.getConfigurationHandler();
		Configuration configuration = null;

		try {
			configuration = factory.getConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH, false, handler);
			if (!filterStorage.getFilterUsername(configuration).filterUsername(userName)) {
				throw new BaseSecurityException(BaseSecurityException.UNACCEPTABLE_USER_NAME, new Object[] {userName});
			}
		} catch (NameNotFoundException e) {
			UserStoreServiceFrame.logError("No configuration of user name filter found!");
		} finally {
			factory.close(configuration, handler);
		}

		handler = factory.getConfigurationHandler();
		UserStoreServiceFrame.lock(locker + USERS_CONFIG_PATH);

		try {
			configuration = factory.getConfiguration(USERS_CONFIG_PATH, true, handler);
			Configuration userConfiguration = configuration.createSubConfiguration(userName);
			UserInfoSpi info = new UserInfoImpl(userConfiguration, true);
			treeManager.createUser(userName, true, userConfiguration);

			factory.commit(configuration, handler);
			((UserInfoImpl) info).setUserContext(this);
			UserStoreServiceFrame.logNotice("User \"" + userName + "\" created successfully.");
			info.engineSetPassword(null, password);
			return info;
		} catch (Exception e) {
			factory.rollback(configuration, handler);
			BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_CREATE_USER, Severity.WARNING, new Object[] {userName}, e);
			bse.log();
			throw bse;
		} finally {
			UserStoreServiceFrame.releaseLock(locker + USERS_CONFIG_PATH);
		}
	}

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineDeleteUser(String)
   */
  public void engineDeleteUser(String userName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + USERS_CONFIG_PATH);
    UserStoreServiceFrame.lock(locker + GroupContextImpl.GROUPS_CONFIG_PATH);
    try {
      engineGetUserInfo(userName).engineSetCertificates(new X509Certificate[0]);
      configuration = factory.getConfiguration(USERS_CONFIG_PATH + "/" + userName, true, handler);
      Iterator parents = treeManager.getParents(userName, configuration);
      treeManager.ungroupUser(userName, true, parents, handler);
      configuration.deleteConfiguration();
      factory.commit(configuration, handler);
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_DELETE_USER_INFO, Severity.WARNING, new Object[] {userName}, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GroupContextImpl.GROUPS_CONFIG_PATH);
      UserStoreServiceFrame.releaseLock(locker + USERS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetFilterUsername()
   */
  public FilterUsername engineGetFilterUsername() throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH, false, handler);
      return filterStorage.getFilterUsername(configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LOAD_FILTER_USERNAME, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineSetFilterUsername(FilterUsername)
   */
  public void engineSetFilterUsername(FilterUsername filterUsername) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(FilterStorage.USER_FILTER_CONFIG_PATH, true, handler);
      filterStorage.setFilterUsername(filterUsername, configuration);
      factory.commit(configuration, handler);
      UserStoreServiceFrame.logNotice("FilterUsername stored successfully.");
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_STORE_FILTER_USERNAME, e);
      bse.log();
      throw bse;
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetFilterPassword()
   */
  public FilterPassword engineGetFilterPassword() throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH, false, handler);
      return filterStorage.getFilterPassword(configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LOAD_FILTER_PASSWORD, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineSetFilterPassword(FilterPassword)
   */
  public void engineSetFilterPassword(FilterPassword filterPassword) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH, true, handler);
      filterStorage.setFilterPassword(filterPassword, configuration);
      factory.commit(configuration, handler);
      UserStoreServiceFrame.logNotice("FilterPassword saved successfully.");
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_STORE_FILTER_PASSWORD, e);
      bse.log();
      throw bse;
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineIsUserPropertySupported(int, int)
   */
  public boolean engineIsUserPropertySupported(int userProperty, int operation) throws SecurityException {
    switch (userProperty) {
      case UserContext.PROPERTY_CREATE_DATE: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_MODIFY_DATE: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_FAILED_LOGON_COUNT: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_LAST_FAILED_LOGON_DATE: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_LAST_SUCCESSFUL_LOGON_DATE: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_VALID_FROM_DATE: {
        return false;
      }
      case UserContext.PROPERTY_VALID_TO_DATE: {
        return false;
      }
      case PROPERTY_PASSWORD: {
        if (operation == UserContext.OPERATION_WRITE || operation == UserContext.OPERATION_CHECK) {
          return true;
        }
        return retrievePasswords;
      }
      case UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_CHECK || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      case UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD: {
        return true;
      }
      case UserContext.PROPERTY_LOCK_STATUS: {
        if (operation == UserContext.OPERATION_READ || operation == UserContext.OPERATION_WRITE) {
          return true;
        }
        return false;
      }
      default: {
        return true;
      }
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineRegisterListener(UserListener, int)
   */
  public void engineRegisterListener(UserListener userListener, int modifier) throws SecurityException {
    throw new BaseSecurityException(BaseSecurityException.NOT_SUPPORTED);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineUnregisterListener(UserListener)
   */
  public void engineUnregisterListener(UserListener userlistener) throws SecurityException {
    throw new BaseSecurityException(BaseSecurityException.NOT_SUPPORTED1);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineIsSubjectRetrievalSupported()
   */
  public boolean engineIsSubjectRetrievalSupported() {
    return true;//isSubjectRetrievalSupported;
  }

     /**
      * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineFillSubject(UserInfoSpi, Subject)
      */
  public long engineFillSubject(UserInfoSpi user, Subject subject) throws SecurityException {
    if (retrievePasswords) {
      ((UserInfoImpl) user).setUserContext(this);
      char[] password = (char[]) user.engineReadUserProperty(PROPERTY_PASSWORD);
      PasswordCredential credential = new PasswordCredential(user.engineGetName(), password);
      subject.getPrivateCredentials().add(credential);
    }

    subject.getPrincipals().add(user.engineGetPrincipal());

       // todo
//    X509Certificate[] certificates = user.engineGetCertificates();
//    if (certificates.length > 0) {
//      Set credentials = subject.getPublicCredentials();
//      for (int i = 0; i < certificates.length; i++) {
//        credentials.add(certificates[i]);
//      }
//    }

    return Long.MAX_VALUE;
  }

  /**
   *  Removes from subject the associated principals and credentials.
   *
   * @param  subject  the subject which prinsipals and credentials will be removed.
   **/
  public void engineEmptySubject(Subject subject) throws SecurityException {
	  try {
	    Iterator principals = subject.getPrincipals(com.sap.engine.lib.security.Principal.class).iterator();

		  while (principals.hasNext()) {
        principals.next();
        principals.remove();
		  }
	  } catch (Exception e) {
      UserStoreServiceFrame.logError("Unable to remove credentials from subject", e);
	  }

    try {
		  Iterator privateCredentials = subject.getPrivateCredentials(PasswordCredential.class).iterator();

		  while (privateCredentials.hasNext())  {
		    privateCredentials.next();
		    privateCredentials.remove();
		  }
    } catch (Exception e) {
      UserStoreServiceFrame.logError("Unable to remove credentials from subject", e);
    }

    // todo
//    try {
//      Iterator publicCredentials = subject.getPublicCredentials(X509Certificate.class).iterator();
//
//      while (publicCredentials.hasNext()) {
//        publicCredentials.next();
//        publicCredentials.remove();
//      }
//    } catch (Exception e) {
//      // ignore
//    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetLockAfterInvalidAttempts()
   */
  public int engineGetLockAfterInvalidAttempts() {
    return lock_after_invalid_attempts;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineIsInEmergencyMode()
   */
  public boolean engineIsInEmergencyMode() {
    return false;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineIsEmergencyUser(String userName)
   */
  public boolean engineIsEmergencyUser(String userName) {
    return false;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetEmergencyUserName()
   */
  public String engineGetEmergencyUserName() {
    return null;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetSearchFilter()
   */
  public SearchFilter engineGetSearchFilter() {
    return new SearchFilterImpl();
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineRefresh(String userName)
   */
  public void engineRefresh(String userName) {
    //no cache of user entries supported
  }
   
  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserContextSpi#engineGetAnonymousUserName()
   */
  public String engineGetAnonymousUserName() {
    if (anonymousUserName == null || anonymousUserName.trim().equals("")) {
      throw new BaseSecurityException(BaseSecurityException.ANONYMOUS_USER_CORRUPT);
    }

    return anonymousUserName;
  }

  protected byte[] encode(byte[] toencode) {
    if (retrievePasswords) {
      try {
        if (crypt == null) {
          this.crypt = UserStoreServiceFrame.getSecurityContext().getCryptographyContext();
        }
        Cipher encodeCipher = crypt.getCipher(Cipher.ENCRYPT_MODE);
        return encodeCipher.doFinal(toencode);
      } catch (Exception e) {
        return symmetricEncode(toencode, (byte) toencode.length);
      }
    } else {
      if (digest == null) {
        try {
          digest = MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException se) {
          return toencode;
        }
      }
      synchronized (digest) {
        return digest.digest(toencode);
      }
    }
  }

  protected byte[] decode(byte[] todecode) {
    if (retrievePasswords) {
      try {
        if (crypt == null) {
          this.crypt = UserStoreServiceFrame.getSecurityContext().getCryptographyContext();
        }
        Cipher decodeCipher = crypt.getCipher(Cipher.DECRYPT_MODE);
        return decodeCipher.doFinal(todecode);
      } catch (Exception e) {
        return symmetricDecode(todecode, (byte) todecode.length);
      }
    } else {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_DECODE);
    }
  }

  protected boolean equals(byte[] arr1, byte[] arr2) {
    if (arr1 == null && arr2 == null) {
      return true;
    }
    if (arr1 == null && arr2 != null || arr1 != null && arr2 == null || arr1.length != arr2.length) {
      return false;
    }
    for (int i = 0; i < arr1.length; i++) {
      if (arr1[i] != arr2[i]) {
        return false;
      }
    }
    return true;
  }

  private static byte[] symmetricEncode(byte[] toencode, byte key) {
    byte[] result = new byte[toencode.length];
    for (int i = 0; i < toencode.length; i++) {
      result[i] = (byte) (key ^ toencode[i]);
      key = result[i];
    }
    return result;
  }

  private static byte[] symmetricDecode(byte[] todecode, byte key) {
    byte[] result = new byte[todecode.length];
    for (int i = 0; i < todecode.length; i++) {
      result[i] = (byte) (key ^ todecode[i]);
      key = todecode[i];
    }
    return result;
  }
}