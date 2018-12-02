/**
 * Copyright (c) 2000 by InQMy Software AG.,
 * url: http://www.inqmy.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of InQMy Software AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with InQMy.
 */
package com.sap.engine.services.userstore;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;

import java.util.Iterator;
import java.util.Vector;

/**
 *  Tree manager is responsible for managing the user tree.
 *
 * @author  Ekaterina Zheleva
 * @version 6.30
 */
public class TreeManager {

  public static final String PARENT_GROUPS_CONTAINER  = "parent.groups";
  public static final String CHILD_GROUPS_CONTAINER = "child.groups";
  public static final String CHILD_USERS_CONTAINER = "child.users";
  
  private ConfigurationFactory factory = null;

  /**
   *  Constructor.
   */
  public TreeManager(ConfigurationFactory factory) {
    this.factory = factory;
  }

  /**
   *  Creates user.
   *
   * @param   user          String
   * @param   isUser        boolean
   * @param   config   Configuration
   *
   * @exception   SecurityException thrown if user or group with such name already exists.
   */
  public void createUser(String user, boolean isUser, Configuration config) throws Exception {
    config.createSubConfiguration(PARENT_GROUPS_CONTAINER);
    if (!isUser) {
      config.createSubConfiguration(CHILD_GROUPS_CONTAINER);
      config.createSubConfiguration(CHILD_USERS_CONTAINER);
    }
  }

  /**
   *  Returns the the children of the node
   *
   * @param  node  name of the node
   *
   * @return  iterator of the child nodes
   */
  public Iterator getChildUsers(String node, Configuration config) throws Exception {
    return config.getSubConfiguration(CHILD_USERS_CONTAINER).getAllConfigEntries().keySet().iterator();
  }

  /**
   *  Returns the the children of the node
   *
   * @param  node  name of the node
   *
   * @return  iterator of the child nodes
   */
  public Iterator getChildGroups(String node, Configuration config) throws Exception {
    return config.getSubConfiguration(CHILD_GROUPS_CONTAINER).getAllConfigEntries().keySet().iterator();
  }

  public Iterator listRoots(Configuration config) throws Exception {
    Vector roots = new Vector();
    String[] allNodes = config.getAllSubConfigurationNames();
    for (int i = 0; i < allNodes.length; i++) {
      if (!getParents(allNodes[i], config.getSubConfiguration(allNodes[i])).hasNext()) {
        roots.addElement(allNodes[i]);
      }
    }
    return roots.iterator();
  }

  /**
   *  Returns the parents of the node
   *
   * @param  node  name of the node
   *
   * @return  iterator of the parent nodes
   */
  public Iterator getParents(String node, Configuration config) throws Exception {
    return config.getSubConfiguration(PARENT_GROUPS_CONTAINER).getAllConfigEntries().keySet().iterator();
  }
  /**
   *  Groups user.
   *
   * @param   user    String
   * @param   isUser  boolean
   * @param   parent  String
   * @param   handler ConfigurationHandler
   *
   * @exception   SecurityException thrown if some of the sids are invalid or
   * cycle found.
   */
  public void groupUser(String user, boolean isUser, String parent, ConfigurationHandler handler) throws Exception {
    if (!isUser) {
      checkCycle(user, parent);
    }

    String path = GroupContextImpl.GROUPS_CONFIG_PATH + "/" + parent + "/" + ((isUser) ? CHILD_USERS_CONTAINER : CHILD_GROUPS_CONTAINER);
    factory.getConfiguration(path, true, handler).addConfigEntry(user, "");

    path = ((isUser) ? UserContextImpl.USERS_CONFIG_PATH : GroupContextImpl.GROUPS_CONFIG_PATH) + "/" + user + "/" + PARENT_GROUPS_CONTAINER;
    factory.getConfiguration(path, true, handler).addConfigEntry(parent, "");
  }

  /**
   *  Ungroups users.
   *
   * @param   user    String
   * @param   isUser  boolean
   * @param   parent  String
   * @param   handler ConfigurationHandler
   * @exception   SecurityException
   */
  public void ungroupUser(String user, boolean isUser, String parent, ConfigurationHandler handler) throws Exception {
    String path = GroupContextImpl.GROUPS_CONFIG_PATH + "/" + parent + "/" + ((isUser) ? CHILD_USERS_CONTAINER : CHILD_GROUPS_CONTAINER);
    factory.getConfiguration(path, true, handler).deleteConfigEntry(user);

    path = ((isUser) ? UserContextImpl.USERS_CONFIG_PATH : GroupContextImpl.GROUPS_CONFIG_PATH) + "/" + user + "/" + PARENT_GROUPS_CONTAINER;
    factory.getConfiguration(path, true, handler).deleteConfigEntry(parent);
  }

  public void ungroupUser(String user, boolean isUser, Iterator parents, ConfigurationHandler handler) throws Exception {
    String path = null;
    while (parents.hasNext()) {
      path = GroupContextImpl.GROUPS_CONFIG_PATH + "/" + parents.next() + "/" + ((isUser) ? CHILD_USERS_CONTAINER : CHILD_GROUPS_CONTAINER);
      factory.getConfiguration(path, true, handler).deleteConfigEntry(user);
    }
  }

  private void checkCycle(String child, String parent) throws Exception {
    if (child.equals(parent)) {
      //throw new BaseSecurityException(BaseSecurityException.CYCLE_FOUND);
      throw new Exception("Cycle found!");
    }
    ConfigurationHandler handler = factory.getConfigurationHandler();
    Configuration config = null;
    String[] parents = null;
    try {
      config = factory.getConfiguration(GroupContextImpl.GROUPS_CONFIG_PATH + "/" + parent + "/" + PARENT_GROUPS_CONTAINER, false, handler);
      parents = config.getAllConfigEntryNames();
    } catch (Exception e) {
      throw e;
    } finally {
      factory.close(config, handler);
    }
    if (parents != null) {
      for (int i = 0; i < parents.length; i++) {
        checkCycle(child, parents[i]);
      }
    }
  }
}
