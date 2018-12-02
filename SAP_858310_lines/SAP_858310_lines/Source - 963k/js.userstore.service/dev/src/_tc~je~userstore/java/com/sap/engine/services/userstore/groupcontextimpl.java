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

import com.sap.engine.interfaces.security.userstore.listener.GroupListener;
import com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi;
import com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi;
import com.sap.engine.interfaces.security.userstore.context.SearchFilter;
import com.sap.engine.interfaces.security.userstore.context.SearchResult;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.NameAlreadyExistsException;
import com.sap.engine.services.userstore.exceptions.BaseSecurityException;
import com.sap.engine.services.userstore.search.SearchFilterImpl;
import com.sap.engine.services.userstore.search.GroupSearchResultImpl;
import com.sap.tc.logging.Severity;

public class GroupContextImpl implements GroupContextSpi {

  private ConfigurationFactory factory = null;
  private TreeManager treeManager = null;

  public static final String GROUPS_CONFIG_PATH = "groups";
  public static final String CONFIG_PATH = "configuration.path";
  private String configPath = null;
  private String locker = "";

  public GroupContextImpl() {
    this.factory = new ConfigurationFactory();
    this.treeManager = new TreeManager(factory);
  }

  public void setLocker(String lock) {
    this.locker = (lock != null && lock.length() > 0) ? lock : "";
  }

  public String getLocker() {
    return locker;
  }

  /**
   * Changes the properties of the GroupContext.
   *
   * @param   newProps   new Properties
   */
  public synchronized void enginePropertiesChanged(java.util.Properties newProps) {
    configPath = newProps.getProperty(CONFIG_PATH);
    if (configPath != null && configPath.length() > 0) {
      factory.setPath(configPath);
    }

    Vector users_and_groups = PropertiesParser.parseUsersAndGroups(newProps);
    ConfigurationHandler configHandler = factory.getConfigurationHandler();
    Configuration rootConfiguration = null;
    setLocker(configPath);
    UserStoreServiceFrame.lock(locker);
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    try {
      rootConfiguration = factory.createRootConfiguration(configHandler);
      rootConfiguration.createSubConfiguration(GROUPS_CONFIG_PATH);
      setInitialGroups((String[][]) users_and_groups.elementAt(0), (String[][]) users_and_groups.elementAt(1), rootConfiguration);
      factory.commit(rootConfiguration, configHandler);
    } catch (NameAlreadyExistsException _) {
      factory.rollback(rootConfiguration, configHandler);
      return;
    } catch (Exception e) {
      factory.rollback(rootConfiguration, configHandler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_INITIALIZE_GROUP_CONTEXT_SPI, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
      UserStoreServiceFrame.releaseLock(locker);
    }
  }

  private void setInitialGroups(String[][] users, String[][] groups, Configuration config) {
    factory.setTransactionAttribute(config);
    try {
      for (int i = 0; i < groups.length; i++) {
        try {
          engineGetGroupInfo(groups[i][1]);
        } catch (SecurityException se) {
          engineCreateGroup(groups[i][1]);
        }
        try {
          engineGetGroupInfo(groups[i][0]);
        } catch (SecurityException e) {
          engineCreateGroup(groups[i][0]);
        }
        engineAddGroupToParent(groups[i][0], groups[i][1]);
      }
      for (int i = 0; i < users.length; i++) {
        engineAddUserToGroup(users[i][0], users[i][2]);
      }
    } catch (Exception e) {
      return;
    } finally {
      factory.setTransactionAttribute(null);
      if (configPath != null && configPath.length() > 0) {
        factory.setPath(configPath);
      }
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineListGroups()
   */
  public Iterator engineListGroups() throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH, false, handler);
      return configuration.getAllSubConfigurations().keySet().iterator();
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LIST_GROUPS, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
	* @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineSearchGroups(SearchFilter filter)
	*/
  public SearchResult engineSearchGroups(SearchFilter filter) throws SecurityException {
    int resultSize = filter.getMaxSearchResultSize();
    Iterator attributes = filter.getSearchAttributes();
    if (attributes.hasNext()) {
      return new GroupSearchResultImpl(this, attributes, resultSize);
    } else {
      throw new BaseSecurityException(BaseSecurityException.NO_SEARCH_CRITERIA_IS_SET);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetGroupInfo(String)
   */
  public GroupInfoSpi engineGetGroupInfo(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH + "/" + groupName, false, handler);
      GroupInfoSpi info = new GroupInfoImpl(configuration);
      ((GroupInfoImpl) info).setGroupContext(this);
      return info;
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_GROUP_INFO, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineCreateGroup(String)
   */
  public GroupInfoSpi engineCreateGroup(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH, true, handler);
      Configuration groupConfiguration = configuration.createSubConfiguration(groupName);
      treeManager.createUser(groupName, false, groupConfiguration);
      GroupInfoSpi info = new GroupInfoImpl(groupConfiguration);
      ((GroupInfoImpl) info).setGroupContext(this);

      factory.commit(configuration, handler);
      UserStoreServiceFrame.logNotice("Group \"" + groupName + "\" created successfully.");
      return info;
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_CREATE_GROUP, Severity.WARNING, new Object[] {groupName}, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineDeleteGroup(String)
   */
  public void engineDeleteGroup(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH + "/" + groupName, true, handler);
      while (treeManager.getChildUsers(groupName, configuration).hasNext()
             || treeManager.getChildGroups(groupName, configuration).hasNext()) {
        throw new BaseSecurityException(BaseSecurityException.CANNOT_DELETE_NOT_EMPTY_GROUP);
      }
      Iterator parents = treeManager.getParents(groupName, configuration);
      treeManager.ungroupUser(groupName, false, parents, handler);
      configuration.deleteConfiguration();
      factory.commit(configuration, handler);
      UserStoreServiceFrame.logNotice("Group \"" + groupName + "\" deleted successfully.");
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_DELETE_GROUP, Severity.WARNING, new Object[] {groupName}, e);
      bse.log();
      throw bse;
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineAddGroupToParent(String, String)
   */
  public void engineAddGroupToParent(String groupName, String parentName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    try {
      treeManager.groupUser(groupName, false, parentName, handler);
      factory.commit(configuration, handler);
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      throw new BaseSecurityException(BaseSecurityException.CANNOT_ADD_GROUP_TO_PARENT, e);
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineRemoveGroupFromParent(String, String)
   */
  public void engineRemoveGroupFromParent(String groupName, String parentName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    try {
      treeManager.ungroupUser(groupName, false, parentName, handler);
      factory.commit(configuration, handler);
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      throw new BaseSecurityException(BaseSecurityException.CANNOT_REMOVE_GROUP_FROM_PARENT, e);
    } finally {
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetChildGroups(String)
   */
  public Iterator engineGetChildGroups(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH + "/" + groupName, false, handler);
      return treeManager.getChildGroups(groupName, configuration);
    } catch (Exception e) {
      BaseSecurityException bse = new BaseSecurityException(BaseSecurityException.CANNOT_GET_CHILD_GROUPS, e);
      bse.log();
      throw bse;
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetParentGroups(String)
   */
  public Iterator engineGetParentGroups(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH + "/" + groupName, false, handler);
      return treeManager.getParents(groupName, configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_GROUP_PARENT_GROUPS, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineAddUserToGroup(String, String)
   */
  public void engineAddUserToGroup(String userName, String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    UserStoreServiceFrame.lock(locker + UserContextImpl.USERS_CONFIG_PATH);
    try {
      treeManager.groupUser(userName, true, groupName, handler);
      factory.commit(configuration, handler);
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      throw new BaseSecurityException(BaseSecurityException.CANNOT_ADD_USER_TO_GROUP, e);
    } finally {
      UserStoreServiceFrame.releaseLock(locker + UserContextImpl.USERS_CONFIG_PATH);
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineRemoveUserFromGroup(String, String)
   */
  public void engineRemoveUserFromGroup(String userName, String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    UserStoreServiceFrame.lock(locker + GROUPS_CONFIG_PATH);
    UserStoreServiceFrame.lock(locker + UserContextImpl.USERS_CONFIG_PATH);
    try {
      treeManager.ungroupUser(userName, true, groupName, handler);
      factory.commit(configuration, handler);
    } catch (Exception e) {
      factory.rollback(configuration, handler);
      throw new BaseSecurityException(BaseSecurityException.CANNOT_REMOVE_USER_FROM_GROUP, e);
    } finally {
      UserStoreServiceFrame.releaseLock(locker + UserContextImpl.USERS_CONFIG_PATH);
      UserStoreServiceFrame.releaseLock(locker + GROUPS_CONFIG_PATH);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetUsersOfGroup(String)
   */
  public Iterator engineGetUsersOfGroup(String groupName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH + "/" + groupName, false, handler);
      return treeManager.getChildUsers(groupName, configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_USERS_OF_GROUP, e);
    } finally {
      factory.close(configuration, handler);
    }
  }


	/**
	 *  List the names of the root groups.
	 *
	 * @return  iterator with the names of the groups , which don't have parents.
	 */
  public java.util.Iterator engineListRootGroups() {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(GROUPS_CONFIG_PATH, false, handler);
      return treeManager.listRoots(configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_LIST_ROOT_GROUPS, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetGroupsOfUser(String)
   */
  public Iterator engineGetGroupsOfUser(String userName) throws SecurityException {
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration configuration = null;
    try {
      configuration = factory.getConfiguration(UserContextImpl.USERS_CONFIG_PATH + "/" + userName, false, handler);
      return treeManager.getParents(userName, configuration);
    } catch (Exception e) {
      throw new BaseSecurityException(BaseSecurityException.CANNOT_GET_GROUPS_OF_USER, e);
    } finally {
      factory.close(configuration, handler);
    }
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#registerListener(GroupListener, int)
   */
  public void registerListener(GroupListener groupListener, int modifier) throws SecurityException {
    throw new RuntimeException("RegisterListener not supported.");
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#unregisterListener(GroupListener)
   */
  public void unregisterListener(GroupListener groupListener) throws SecurityException {
    throw new RuntimeException("UnregisterListener not supported.");
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineGetSearchFilter()
   */
  public SearchFilter engineGetSearchFilter() {
    return new SearchFilterImpl();
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupContextSpi#engineRefresh(String groupName)
   */
  public void engineRefresh(String groupName) {
    //no cache of group entries supported
  }

}

