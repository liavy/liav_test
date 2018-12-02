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

import java.security.Principal;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.*;

import com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi;
import com.sap.engine.interfaces.security.userstore.context.UserContext;
import com.sap.engine.interfaces.security.userstore.context.UserInfo;
import com.sap.engine.interfaces.keystore.Constants;
import com.sap.engine.interfaces.keystore.KeystoreManager;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.InconsistentReadException;
import com.sap.engine.services.userstore.filter.FilterStorage;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;
import com.sap.engine.services.userstore.exceptions.BaseIllegalArgumentException;
import com.sap.exception.IBaseException;
import com.sap.tc.logging.Severity;

/**
 * The implementation of UserInfoSpi for the internal userstore.
 *
 * @author  Ekaterina Zheleva
 * @version 6.30
 */
public class UserInfoImpl implements UserInfoSpi {
  public static final String PASSWORD = "password";
  public static final String CREATE_DATE = "PROPERTY_CREATE_DATE";
  public static final String MODIFY_DATE = "PROPERTY_MODIFY_DATE";
  public static final String FAILED_LOGON_COUNT = "PROPERTY_FAILED_LOGON_COUNT";
  public static final String LAST_FAILED_LOGON_DATE = "PROPERTY_LAST_FAILED_LOGON_DATE";
  public static final String SUCCESSFUL_LOGON_COUNT = "PROPERTY_SUCCESSFUL_LOGON_COUNT";
  public static final String LAST_SUCCESSFUL_LOGON_DATE = "PROPERTY_LAST_SUCCESSFUL_LOGON_DATE";
  public static final String VALID_FROM_DATE = "PROPERTY_VALID_FROM_DATE";
  public static final String VALID_TO_DATE = "PROPERTY_VALID_TO_DATE";
  public static final String LAST_CHANGED_PASSWORD_DATE = "PROPERTY_LAST_CHANGED_PASSWORD_DATE";
  public static final String FORCE_TO_CHANGE_PASSWORD = "PROPERTY_FORCE_TO_CHANGE_PASSWORD";
  public static final String LOCK_STATUS = "PROPERTY_LOCK_STATUS";
	public static final String PASSWORD_DISABLED = "password_disabled";

  protected static final String SEPARATOR = "_";

  private final X509Certificate[] NO_CERTIFICATES = new X509Certificate[0];

  private byte[] password = null;
  private long[] property = new long[11];
  private int lockStatus = UserContext.LOCKED_NO;
  private Principal principal = null;
  private UserContextImpl userContext = null;
  static long defaultExpiration = -1;
  private boolean passwordDisabled = false;

  UserInfoImpl(Configuration config, boolean writeAccess) throws Exception {
    String path = config.getPath();
    principal = new com.sap.engine.lib.security.Principal(path.substring(path.lastIndexOf('/') + 1));
    if (writeAccess) {
      //password = new byte[0];
      //config.addConfigEntry(PASSWORD, password);

      property[UserContext.PROPERTY_CREATE_DATE - 1] = Calendar.getInstance().getTime().getTime();
      config.addConfigEntry(CREATE_DATE, new Long(property[UserContext.PROPERTY_CREATE_DATE - 1]));

      property[UserContext.PROPERTY_MODIFY_DATE - 1] = Calendar.getInstance().getTime().getTime();
      config.addConfigEntry(MODIFY_DATE, new Long(property[UserContext.PROPERTY_MODIFY_DATE - 1]));

      property[UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT - 1] = 0;
      config.addConfigEntry(SUCCESSFUL_LOGON_COUNT, new Long(property[UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT - 1]));

      property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1] = 0;
      config.addConfigEntry(FAILED_LOGON_COUNT, new Long(property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1]));

      property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1] = defaultExpiration;
      config.addConfigEntry(FORCE_TO_CHANGE_PASSWORD, new Long(property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1]));

      lockStatus = UserContext.LOCKED_NO;
      config.addConfigEntry(LOCK_STATUS, new Integer(lockStatus));

    } else {
      if (config.existsConfigEntry(PASSWORD)) {
        password = (byte[]) config.getConfigEntry(PASSWORD);
      } else {
        password = null;
      }
      setProperty(CREATE_DATE, UserContext.PROPERTY_CREATE_DATE, config);
      setProperty(MODIFY_DATE, UserContext.PROPERTY_MODIFY_DATE, config);
      setProperty(FAILED_LOGON_COUNT, UserContext.PROPERTY_FAILED_LOGON_COUNT, config);
      setProperty(LAST_FAILED_LOGON_DATE, UserContext.PROPERTY_LAST_FAILED_LOGON_DATE, config);
      setProperty(SUCCESSFUL_LOGON_COUNT, UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT, config);
      setProperty(LAST_SUCCESSFUL_LOGON_DATE, UserContext.PROPERTY_LAST_SUCCESSFUL_LOGON_DATE, config);
      setProperty(VALID_FROM_DATE, UserContext.PROPERTY_VALID_FROM_DATE, config);
      setProperty(VALID_TO_DATE, UserContext.PROPERTY_VALID_TO_DATE, config);
      setProperty(LAST_CHANGED_PASSWORD_DATE, UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE, config);
      setProperty(FORCE_TO_CHANGE_PASSWORD, UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD, config);
      if (config.existsConfigEntry(LOCK_STATUS)) {
        lockStatus = ((Integer) config.getConfigEntry(LOCK_STATUS)).intValue();
      }
			if (config.existsConfigEntry(PASSWORD_DISABLED)) {
				passwordDisabled = ((String) config.getConfigEntry(PASSWORD_DISABLED)).equals("true");
			}
    }
  }

  private void setProperty(String propName, int propIndex, Configuration config) throws Exception {
    if (config.existsConfigEntry(propName)) {
      property[propIndex - 1] = ((Long) config.getConfigEntry(propName)).longValue();
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineGetPrincipal()
   */
  public Principal engineGetPrincipal() {
    return principal;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineGetName()
   */
  public String engineGetName() {
    return principal.getName();
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineGetParentGroups()
   */
  public Iterator engineGetParentGroups() throws SecurityException {
    return userContext.engineGetParentGroups(principal.getName());
  }


  /**
   *  Returns the certificates associated with this user.
   *
   * @return  an array of all certificates associated with the user.
   *
   * @throws SecurityException
   */
  public X509Certificate[] engineGetCertificates() throws SecurityException {
    Vector certs = new Vector();

    try {
      KeystoreManager keystore = UserStoreServiceFrame.getKeystoreInterface();

      if (keystore.existKeystoreView(Constants.KV_ALIAS_DBMS_USERS)) {
        KeyStore store = keystore.getKeystore(Constants.KV_ALIAS_DBMS_USERS);
        int size = store.size();

        for (int i = 0; i < size; i++) {
          String alias = principal.getName() + SEPARATOR + i;
          Certificate certificate = store.getCertificate(alias);

          if (certificate != null) {
            certs.add(certificate);
          } else {
            break;
          }
        }
      }

      if (certs.size() > 0) {
        return (X509Certificate[]) certs.toArray(NO_CERTIFICATES);
      } else {
        return NO_CERTIFICATES;
      }
    } catch (Exception e) {
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_GET_USER_CERT, com.sap.tc.logging.Severity.WARNING, e);
      bse.log();
      throw bse;
    }
  }


  /**
   * Changes the password of the user to the given one. After this call the user will be forced
   * to change his password on the following authentication.
   *
   * @param   newPassword  the new password of the user
   */
  public void engineSetPassword(char[] newPassword) throws SecurityException {
    engineWriteUserProperty(UserContextImpl.PROPERTY_PASSWORD, newPassword);
  }


  /**
   * Changes the password of the user to the given one after verifying that the current password
   * is the given one.
   *
   * @param   oldPassword  the current password of the user
   * @param   newPassword  the new password of the user
   */
	public void engineSetPassword(char[] oldPassword, char[] newPassword) throws SecurityException {
		if (!engineCheckPassword(oldPassword)) {
			throw new BaseSecurityException(BaseSecurityException.INCORRECT_PASSWORD);
		}
		ConfigurationHandler handler = null;
		Configuration configuration = null;
		if (newPassword != null) {
			handler = userContext.factory.getConfigurationHandler();
			try {
				configuration = userContext.factory.getConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH, false, handler);
				if (!userContext.filterStorage.getFilterPassword(configuration).filterPassword(newPassword)) {
					throw new BaseSecurityException(BaseSecurityException.UNACCEPTABLE_PASSWORD);
				}
			} catch (SecurityException e) {
				if (e instanceof IBaseException) {
					throw e;
				}
				throw new BaseSecurityException(BaseSecurityException.CANNOT_FILTER_PASSWORD, e);
			} catch (Exception e) {
				throw new BaseSecurityException(BaseSecurityException.CANNOT_FILTER_PASSWORD1, e);
			} finally {
				userContext.factory.close(configuration, handler);
			}
			char[] targetpassword = newPassword;
			byte[] tocompare = new byte[targetpassword.length];
			for (int i = 0; i < tocompare.length; i++) {
				tocompare[i] = (byte) targetpassword[i];
			}
			this.password = userContext.encode(tocompare);
		} else {
			this.password = null;
		}
		property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1] = System.currentTimeMillis();

		handler = userContext.factory.getConfigurationHandler();
		configuration = null;
		UserStoreServiceFrame.lock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
		try {
			configuration = userContext.factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + engineGetName(), true, handler);
			if (this.password == null) {
				if (configuration.existsConfigEntry(PASSWORD)) {
					configuration.deleteConfigEntry(PASSWORD);
				}
			} else {
				if (configuration.existsConfigEntry(PASSWORD)) {
					configuration.modifyConfigEntry(PASSWORD, this.password);
				} else {
					configuration.addConfigEntry(PASSWORD, this.password);
				}
			}
			configuration.modifyConfigEntry(LAST_CHANGED_PASSWORD_DATE, new Long(property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1]));
			userContext.factory.commit(configuration, handler);
		} catch (Exception e) {
			userContext.factory.rollback(configuration, handler);
			throw new BaseSecurityException(BaseSecurityException.CANNOT_SET_NEW_PASSWORD, new Object[] {engineGetName()}, e);
		} finally {
			UserStoreServiceFrame.releaseLock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
		}
	}

  /**
   *  Changes the certificates associated with this user.
   *
   * @param  certificates  an array of all certificates associated with the user.
   *
   * @throws SecurityException
   */
  public void engineSetCertificates(X509Certificate[] certificates) throws SecurityException {
    try {
      KeystoreManager keystore = UserStoreServiceFrame.getKeystoreInterface();

      if (!keystore.existKeystoreView(Constants.KV_ALIAS_DBMS_USERS)) {
        keystore.createKeystoreView(Constants.KV_ALIAS_DBMS_USERS, new Properties());
      }

      X509Certificate[] certs = engineGetCertificates();
      KeyStore store = keystore.getKeystore(Constants.KV_ALIAS_DBMS_USERS);

      for (int i = 0; i < certificates.length; i++) {
        int j = 0;
        for (; j < certs.length; j++) {
          if (certs[j].equals(certificates[i])) {
            break;
          }
        }

        if (j < certs.length) {
          continue;
        }

        Enumeration aliases = store.aliases();

        while (aliases.hasMoreElements()) {
          String alias = (String) aliases.nextElement();
          Certificate cert = store.getCertificate(alias);

          if ((cert != null) && (cert instanceof X509Certificate)) {
            if (cert.equals(certificates[i])) {
              throw new BaseSecurityException(BaseSecurityException.CERT_BELONGS_TO_ANOTHER_USER);
            }
          }
        }
      }

      for (int i = 0; i < certs.length; i++) {
        store.deleteEntry(store.getCertificateAlias(certs[i]));
      }

      for (int i = 0, j = 0; i < certificates.length; i++) {
        if (certificates[i] != null) {
          store.setCertificateEntry((principal.getName() + SEPARATOR + j), certificates[i]);
          j++;
        }
      }
    } catch (SecurityException e) {
      UserStoreServiceFrame.logWarning("Cannot assign certificate to user " + principal.getName(), e);
      if (e instanceof IBaseException) {
        throw e;
      }
      throw new BaseSecurityException(BaseSecurityException.CANNOT_ASIGN_CERT, new Object[] {principal.getName()}, e);
    } catch (Exception e) {
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_ASIGN_CERT1, Severity.WARNING, new Object[] {principal.getName()}, e);
      bse.log();
      throw bse;
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineReadUserProperty(int)
   * For a non trivial work of this method the <code>setUserContext()</code> method
   * has to be invoked first, to set the appropriate UserContect implementation.
   * Only in case of connector userstore!
   */
  public Object engineReadUserProperty(int userProperty) throws SecurityException {
    if (userContext == null) {
      return null;
    }
    switch (userProperty) {
      case UserContextImpl.PROPERTY_PASSWORD: {
				if (password == null) {
				  return null;
				}
        try {
          byte[] read = userContext.decode(password);
          char[] decodedPassword = new char[read.length];
          for (int i = 0; i < read.length; i++) {
            decodedPassword[i] = (char) read[i];
          }
          return decodedPassword;
        } catch (Exception ex) {
          return null;
        }
      }
      case UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD: {
        if (property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1] == -1) {
          property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1] = 10L * 365 * 24 * 60 * 60 * 1000;
        }

        return new Long(property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1]);
      }
      case UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE:  {
        long time = property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1];
        if (time == 0) {
           return null;
        }
        Date date = new Date(time);
        return date;
      }
      case UserContext.PROPERTY_CREATE_DATE:  {
        long time = property[UserContext.PROPERTY_CREATE_DATE - 1];
        if (time == 0) {
           return null;
        }
        Date date = new Date(time);
        return date;
      }
      case UserContext.PROPERTY_MODIFY_DATE:  {
        long time = property[UserContext.PROPERTY_MODIFY_DATE - 1];
        if (time == 0) {
           return null;
        }
        Date date = new Date(time);
        return date;
      }
      case UserContext.PROPERTY_LAST_FAILED_LOGON_DATE:  {
        long time = property[UserContext.PROPERTY_LAST_FAILED_LOGON_DATE - 1];
        if (time == 0) {
           return null;
        }
        Date date = new Date(time);
        return date;
      }
      case UserContext.PROPERTY_LAST_SUCCESSFUL_LOGON_DATE:  {
        long time = property[UserContext.PROPERTY_LAST_SUCCESSFUL_LOGON_DATE - 1];
        if (time == 0) {
           return null;
        }
        Date date = new Date(time);
        return date;
      }
      case UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT:  {
        int count = (int) property[UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT - 1];
        return new Integer(count);
      }
      case UserContext.PROPERTY_FAILED_LOGON_COUNT:  {
        int count = (int) property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1];
        return new Integer(count);
      }
      case UserContext.PROPERTY_LOCK_STATUS: {
        return new Integer(lockStatus);
      }
      default: {
        return readUnknownProperty(userProperty);
      }
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineWriteUserProperty(int, Object)
   * For a non trivial work of this method the <code>setUserContext()</code> method
   * has to be invoked first, to set the appropriate UserContect implementation.
   * Only in case of connector userstore!
   */
  public void engineWriteUserProperty(int userProperty, Object value) throws SecurityException {
    ConfigurationHandler handler = userContext.factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
    try {
      configuration = userContext.factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + engineGetName(), true, handler);
      if (userProperty == UserContextImpl.PROPERTY_PASSWORD) {
				if (value != null) {
	        if (!(value instanceof char[])) {
		        throw new BaseSecurityException(BaseSecurityException.PROPERTY_MUST_BE_CHARARR);
	        }
	        ConfigurationHandler handlerToFilter = userContext.factory.getConfigurationHandler();
	        Configuration filterContainer = null;
	        try {
		        filterContainer = userContext.factory.getConfiguration(FilterStorage.PASSWORD_FILTER_CONFIG_PATH, false, handlerToFilter);
		        if (!userContext.filterStorage.getFilterPassword(filterContainer).filterPassword((char[]) value)) {
			        throw new BaseSecurityException(BaseSecurityException.CANNOT_WRITE_PROP_PASSWORD);
		        }
	        } catch (Exception e) {
		        if (e instanceof BaseSecurityException) {
			        throw (SecurityException) e;
		        }
		        throw new BaseSecurityException(BaseSecurityException.CANNOT_WRITE_PROP_PASSWORD1, e);
	        } finally {
		        userContext.factory.close(filterContainer, handlerToFilter);
	        }

	        char[] targetpassword = (char[]) value;
	        byte[] tocompare = new byte[targetpassword.length];
	        for (int i = 0; i < tocompare.length; i++) {
		        tocompare[i] = (byte) targetpassword[i];
	        }
	        this.password = userContext.encode(tocompare);
	        if (configuration.existsConfigEntry(PASSWORD)) {
		        configuration.modifyConfigEntry(PASSWORD, this.password);
	        } else {
		        configuration.addConfigEntry(PASSWORD, this.password);
	        }
        } else {
	        this.password = null;
	        if (configuration.existsConfigEntry(PASSWORD)) {
		        configuration.deleteConfigEntry(PASSWORD);
	        }
        }
        property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1] = 0;
        if (configuration.existsConfigEntry(LAST_CHANGED_PASSWORD_DATE)) {
	        configuration.modifyConfigEntry(LAST_CHANGED_PASSWORD_DATE, new Long(property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1]));
        } else {
	        configuration.addConfigEntry(LAST_CHANGED_PASSWORD_DATE, new Long(property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1]));
        }
        passwordDisabled = false;
				if (configuration.existsConfigEntry(PASSWORD_DISABLED)) {
					configuration.modifyConfigEntry(PASSWORD_DISABLED, "false");
				}
      } else if (userProperty == UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD) {
        long expiration = -1;
        if (value instanceof Long) {
          expiration = ((Long) value).longValue();
        } else if (value instanceof String) {
          expiration = Long.parseLong((String) value);
        } else {
          throw new BaseSecurityException(BaseSecurityException.PROPERTY_MUST_BE_LONG_OR_STRING);
        }

        if (expiration < -1) {
          throw new BaseSecurityException(BaseSecurityException.PROPERTY_MUST_BE_POSITIVE);
        }
        property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1] = expiration;
        if (configuration.existsConfigEntry(FORCE_TO_CHANGE_PASSWORD)) {
          configuration.modifyConfigEntry(FORCE_TO_CHANGE_PASSWORD, new Long(property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1]));
        } else {
          configuration.addConfigEntry(FORCE_TO_CHANGE_PASSWORD, new Long(property[UserContext.PROPERTY_FORCE_TO_CHANGE_PASSWORD - 1]));
        }
      } else if ((userProperty == UserContext.PROPERTY_FAILED_LOGON_COUNT) || (userProperty == UserContext.PROPERTY_SUCCESSFUL_LOGON_COUNT)) {
        if (value instanceof Integer) {
          property[userProperty - 1] = ((Integer) value).intValue();
          configuration.modifyConfigEntry((userProperty == UserContext.PROPERTY_FAILED_LOGON_COUNT) ? FAILED_LOGON_COUNT : SUCCESSFUL_LOGON_COUNT, new Long(property[userProperty - 1]));
        } else {
          throw new BaseIllegalArgumentException(BaseIllegalArgumentException.PROPERTY_MUST_BE_INTEGER, new Object[] {new Integer(userProperty)});
        }
      } else if ((userProperty == UserContext.PROPERTY_LAST_FAILED_LOGON_DATE) || (userProperty == UserContext.PROPERTY_LAST_SUCCESSFUL_LOGON_DATE)) {
        if (value instanceof Date) {
          property[userProperty - 1] = ((Date) value).getTime();
          if (configuration.existsConfigEntry((userProperty == UserContext.PROPERTY_LAST_FAILED_LOGON_DATE) ? LAST_FAILED_LOGON_DATE: LAST_SUCCESSFUL_LOGON_DATE)) {
            configuration.modifyConfigEntry((userProperty == UserContext.PROPERTY_LAST_FAILED_LOGON_DATE) ? LAST_FAILED_LOGON_DATE: LAST_SUCCESSFUL_LOGON_DATE, new Long(property[userProperty - 1]));
          } else {
            configuration.addConfigEntry((userProperty == UserContext.PROPERTY_LAST_FAILED_LOGON_DATE) ? LAST_FAILED_LOGON_DATE: LAST_SUCCESSFUL_LOGON_DATE, new Long(property[userProperty - 1]));
          }
        } else {
          throw new BaseIllegalArgumentException(BaseIllegalArgumentException.PROPERTY_MUST_BE_DATE, new Object[] {new Integer(userProperty)});
        }
      } else if (userProperty == UserContext.PROPERTY_LOCK_STATUS) {
        if (value instanceof Integer) {
          if (((Integer) value).intValue() < UserContext.LOCKED_NO || ((Integer) value).intValue() > UserContext.LOCKED_BY_ADMIN) {
            throw new BaseIllegalArgumentException(BaseIllegalArgumentException.UNACCEPTABLE_PROPERTY_VALUE);
          }

          int lock = ((Integer) value).intValue();
          if (lock != lockStatus) {
            this.lockStatus = lock;

            if (lockStatus == UserContext.LOCKED_NO) {
              property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1] = 0;
            }

            configuration.modifyConfigEntry(LOCK_STATUS, value);
          }
        } else {
          throw new BaseIllegalArgumentException(BaseIllegalArgumentException.PROPERTY_MUST_BE_INTEGER1);
        }
      } else {
        //throw new BaseSecurityException(BaseSecurityException.PROPERTY_NOT_SUPPORTED, new Object[] {new Integer(userProperty)});
        String propEntry = "" + userProperty;
        if (configuration.existsConfigEntry(propEntry)) {
          configuration.modifyConfigEntry(propEntry, value.toString());
        } else {
          configuration.addConfigEntry(propEntry, value.toString());
        }
      }
      userContext.factory.commit(configuration, handler);
    } catch (Exception e) {
      userContext.factory.rollback(configuration, handler);
      if (e instanceof BaseSecurityException) {
        throw (BaseSecurityException) e;
      }
      throw new BaseSecurityException(BaseSecurityException.CANNOT_WRITE_USER_PROPERTY, e);
    } finally {
      UserStoreServiceFrame.releaseLock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
    }
  }
  
  public void engineWriteUserProperty(Map properties) throws SecurityException {
  	Iterator keys = properties.keySet().iterator();
  	Object value = null;
  	Object key = null;
  	int userProperty = -1;
  	while (keys.hasNext()) {
  		key = keys.next();
  		userProperty = ((Integer)key).intValue(); 
  		value = properties.get(key);
  		engineWriteUserProperty(userProperty, value);
  	}
  }  

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineCheckUserProperty(int, Object)
   */
  public boolean engineCheckUserProperty(int userProperty, Object value) throws SecurityException {
    if (userProperty == UserContextImpl.PROPERTY_PASSWORD) {
      try {
        char[] targetpassword = (char[]) value;
        byte[] tocompare = new byte[targetpassword.length];
        for (int i = 0; i < tocompare.length; i++) {
          tocompare[i] = (byte) targetpassword[i];
        }
        return (userContext.equals(password, userContext.encode(tocompare)));
      } catch (Exception e) {
        return false;
      }
    }

    throw new BaseSecurityException(BaseSecurityException.PROPERTY_NOT_SUPPORTED1);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineCheckPassword(char[])
   */
  public boolean engineCheckPassword(char[] targetPassword) throws SecurityException {
  	return (engineCheckPasswordExtended(targetPassword) == UserInfo.CHECKPWD_OK);
  }
  
  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineCheckPasswordExtended(char[])
   */
  public int engineCheckPasswordExtended(char[] targetPassword) throws SecurityException {
    // check whether password is disabled
    if (passwordDisabled) {
      return UserInfo.CHECKPWD_NOPWD;
    }
    
    // check whether password is locked due too many wrong password attempts
    int failedLogonAttempts = (int) property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1];
    if ((userContext.lock_after_invalid_attempts > 0) && (failedLogonAttempts >= userContext.lock_after_invalid_attempts)) {
      //TODO auto-unlock!
      return UserInfo.CHECKPWD_PWDLOCKED;
    }
    
    Date now = new Date();
    ConfigurationHandler handler = userContext.factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
    try {
      configuration = userContext.factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + engineGetName(), true, handler);
      //check password
      if (!checkPassword(targetPassword)) {//wrong password
        property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1] += 1;
        configuration.modifyConfigEntry(FAILED_LOGON_COUNT, new Long(property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1]));
        property[UserContext.PROPERTY_LAST_FAILED_LOGON_DATE - 1] = now.getTime();
        if (configuration.existsConfigEntry(LAST_FAILED_LOGON_DATE)) {
          configuration.modifyConfigEntry(LAST_FAILED_LOGON_DATE, new Long(property[UserContext.PROPERTY_LAST_FAILED_LOGON_DATE - 1]));
        } else {
          configuration.addConfigEntry(LAST_FAILED_LOGON_DATE, new Long(property[UserContext.PROPERTY_LAST_FAILED_LOGON_DATE - 1]));
        }
        userContext.factory.commit(configuration, handler);
        return UserInfo.CHECKPWD_WRONGPWD;
      }
      //correct password
      property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1] = 0;
      configuration.modifyConfigEntry(FAILED_LOGON_COUNT, new Long(property[UserContext.PROPERTY_FAILED_LOGON_COUNT - 1]));
      //check whether password is expired -> return UserInfo.CHECKPWD_PWDEXPIRED
      //TODO: Add security property with default date if (new property) LastSuccessfulPwdCheckDate == null
      userContext.factory.commit(configuration, handler);
      return UserInfo.CHECKPWD_OK; 
    } catch (Exception e) {
      userContext.factory.rollback(configuration, handler);
      throw new BaseSecurityException(BaseSecurityException.CANNOT_WRITE_USER_PROPERTY, e);
    } finally {
      UserStoreServiceFrame.releaseLock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
    }
  }
  
  private boolean checkPassword(char[] targetPassword) throws SecurityException {
    if (this.password == null && targetPassword == null) {
      return true;
    }
    try {
      byte[] tocompare = new byte[targetPassword.length];
      for (int i = 0; i < tocompare.length; i++) {
        tocompare[i] = (byte) targetPassword[i];
      }
      return userContext.equals(password, userContext.encode(tocompare));
    } catch (Exception e) {
      return false;
    }
  }

  protected void setUserContext(UserContextImpl userContext) {
    this.userContext = userContext;
  }

  public String toString() {
    String result = "User ID:      " + engineGetName();
    result = result + "\nParent Groups: ";
    Iterator parents = engineGetParentGroups();
    while (parents.hasNext()) {
      result = result + (String) parents.next();
      if (parents.hasNext()) result = result + "\n               ";
    }
    result += "\n";
    return result;
  }

  private Object readUnknownProperty(int userProperty) {
    ConfigurationHandler handler = null;
    Configuration configuration = null;
    boolean exception = true;

    do {
      try {
        handler = userContext.factory.getConfigurationHandler();
        configuration = userContext.factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + engineGetName(), false, handler);
        return configuration.getConfigEntry("" + userProperty);
      } catch (InconsistentReadException ire) {
        //the method waits until the finish of the write-operation over this configuration
        continue;
      } catch (Exception e) {
        UserStoreServiceFrame.logWarning("Cannot read \"" + engineGetName() + "\" user's property [" + userProperty + "].", e);
      } finally {
        userContext.factory.close(configuration, handler);
      }
    } while (exception);

    return null;
  }
	
  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineSetPasswordDisabled()
   */
	public void engineSetPasswordDisabled() throws SecurityException {
  	if (property[UserContext.PROPERTY_LAST_CHANGED_PASSWORD_DATE - 1] != 0) {
		passwordDisabled = true;
		ConfigurationHandler handler = userContext.factory.getConfigurationHandler();
		Configuration configuration = null;
		UserStoreServiceFrame.lock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
		try {
			configuration = userContext.factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + engineGetName(), true, handler);
			if (configuration.existsConfigEntry(PASSWORD_DISABLED)) {
			  configuration.modifyConfigEntry(PASSWORD_DISABLED, "true");
      } else {
        configuration.addConfigEntry(PASSWORD_DISABLED, "true");
      }
			userContext.factory.commit(configuration, handler);
		} catch (Exception e) {
			userContext.factory.rollback(configuration, handler);
			throw new BaseSecurityException(BaseSecurityException.CANNOT_WRITE_USER_PROPERTY, e);
		} finally {
			UserStoreServiceFrame.releaseLock(userContext.getLocker() + UserContextImpl.USERS_CONFIG_PATH);
		}
  	}
  }
 
	/**
	 * @see com.sap.engine.interfaces.security.userstore.spi.UserInfoSpi#engineIsPasswordDisabled()
	 */
	public boolean engineIsPasswordDisabled() throws SecurityException {
    return passwordDisabled;
  }
}

