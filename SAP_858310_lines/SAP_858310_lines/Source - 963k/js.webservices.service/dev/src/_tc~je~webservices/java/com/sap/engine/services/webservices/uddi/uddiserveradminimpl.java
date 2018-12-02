/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.engine.services.webservices.uddi;

import java.util.Iterator;
import java.util.Vector;

import com.sap.engine.interfaces.security.userstore.UserStore;
import com.sap.engine.interfaces.security.userstore.context.GroupContext;
import com.sap.engine.interfaces.security.userstore.context.GroupInfo;
import com.sap.engine.interfaces.security.userstore.context.UserInfo;
import com.sap.engine.interfaces.webservices.uddi.UDDIServer;
import com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin;
import com.sap.engine.interfaces.webservices.uddi.UserDoesNotExistException;
import com.sap.engine.interfaces.webservices.uddi.UserExistsException;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UDDIServerAdminImpl implements UDDIServerAdmin {
  private UDDIServer uddiServer = null;
  private UserStore userStore;
  private GroupInfo levelTier1;
  private GroupInfo levelTierN;
  private GroupInfo levelAdmin;

  public void setUserStore(UserStore store) {
    this.userStore = store;
    GroupContext groupCtx = userStore.getGroupContext();
    levelTier1 = getGroup("LEVEL_PUB_TIER1", groupCtx);
    levelTierN = getGroup("LEVEL_PUB_TIERN", groupCtx);
    levelAdmin = getGroup("LEVEL_ADMIN", groupCtx);
  }
  
  private GroupInfo getGroup(String groupName, GroupContext ctx) {
    Iterator it = ctx.listGroups();
    while (it.hasNext()) {
      String group = it.next().toString();
      if (groupName.equals(group)) {
        return ctx.getGroupInfo(groupName);
      }
    }
    return ctx.createGroup(groupName);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#registerUDDIServer(com.sap.engine.interfaces.webservices.uddi.UDDIServer)
   */
  public void registerUDDIServer(UDDIServer server) {
    uddiServer = server;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#unregisterUDDIServer()
   */
  public void unregisterUDDIServer() {
    uddiServer = null;
  }
  
  public UDDIServer getUDDIServer() {
    return uddiServer;
  }

  private boolean groupContainsUser(GroupInfo group, String username) {
    Iterator it = group.getUsersInGroup();
    while (it.hasNext()) {
      String user = it.next().toString();
      if (username.equals(user)) {
        return true;
      }
    }    
    return false;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#getUserLevel(java.lang.String)
   */
  public int getUserLevel(String username) {
    if (groupContainsUser(levelAdmin, username)) {
      return LEVEL_ADMIN;
    } else if (groupContainsUser(levelTierN, username)) {
      return LEVEL_PUB_TIERN;
    } else if (groupContainsUser(levelTier1, username)) {
      return LEVEL_PUB_TIER1;
    } else {
      return NOT_VALID_USER;
    }
  }

  private UserInfo getUserInfoPerGroup(String username, GroupInfo group) {
    Iterator it = group.getUsersInGroup();
    while (it.hasNext()) {
      String user = it.next().toString();
      if (username.equals(user)) {
        return userStore.getUserContext().getUserInfo(username);
      }
    }
    return null;    
  }

  private UserInfo getUserInfo(String username) {
    UserInfo user = getUserInfoPerGroup(username, levelAdmin);
    if (user == null) {
      user = getUserInfoPerGroup(username, levelTierN);
      if (user == null) {
        user = getUserInfoPerGroup(username, levelTier1);
      }
    }
    return user;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#isValidUser(java.lang.String, char[])
   */
  public boolean isValidUser(String username, char[] password) {
    UserInfo user = getUserInfo(username);
    if (user == null) {
      return false;
    }
    return user.checkPassword(password);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#createNewUser(java.lang.String, char[], int)
   */
  public void createNewUser(String username, char[] password, int level) throws UserExistsException {
    UserInfo user = getUserInfo(username);
    if (user != null) {
      throw new UserExistsException("Such a user already exists: " + username);
    }
    user = userStore.getUserContext().createUser(username);
    user.setPassword(password);
    GroupContext groupCtx = userStore.getGroupContext();
    switch (level) {
      case LEVEL_ADMIN: groupCtx.addUserToGroup(username, levelAdmin.getName());
                        break;
      case LEVEL_PUB_TIERN: groupCtx.addUserToGroup(username, levelTierN.getName());
                            break;
      case LEVEL_PUB_TIER1: groupCtx.addUserToGroup(username, levelTier1.getName());
                            break;
      default: userStore.getUserContext().deleteUser(username);
               throw new IllegalArgumentException("Unknown level passed: " + level);
    }
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#changeUserLevel(java.lang.String, int)
   */
  public void changeUserLevel(String username, int level) throws UserDoesNotExistException {
    int oldLevel = getUserLevel(username);
    if (oldLevel == NOT_VALID_USER) {
      throw new UserDoesNotExistException("Such a user does not exist: " + username);
    }
    if (oldLevel != level) {
      GroupContext groupCtx = userStore.getGroupContext();
      switch (oldLevel) {
        case LEVEL_ADMIN: groupCtx.removeUserFromGroup(username, levelAdmin.getName());
                          break; 
        case LEVEL_PUB_TIERN: groupCtx.removeUserFromGroup(username, levelTierN.getName());
                              break; 
        case LEVEL_PUB_TIER1: groupCtx.removeUserFromGroup(username, levelTier1.getName());
                              break; 
      }
      switch (level) {
        case LEVEL_ADMIN: groupCtx.addUserToGroup(username, levelAdmin.getName());
                          break;
        case LEVEL_PUB_TIERN: groupCtx.addUserToGroup(username, levelTierN.getName());
                              break;
        case LEVEL_PUB_TIER1: groupCtx.addUserToGroup(username, levelTier1.getName());
                              break;
        default: throw new IllegalArgumentException("Unknown level passed: " + level);
      }
    }
  }
  
  
  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#deleteUser(java.lang.String)
   */
  public void deleteUser(String username) throws UserDoesNotExistException {
    if (getUserInfo(username) == null) {
      throw new UserDoesNotExistException("Such a user does not exist: " + username);
    }
    userStore.getUserContext().deleteUser(username);
  }


  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin#getUsersOfLevel(int)
   */
  public String[] getUsersOfLevel(int level) {
    GroupInfo group = null;
    switch (level) {
      case LEVEL_ADMIN: group = levelAdmin;
                        break;
      case LEVEL_PUB_TIERN: group = levelTierN;
                            break;
      case LEVEL_PUB_TIER1: group = levelTier1;
                            break;
      default: throw new IllegalArgumentException("Unknown level passed: " + level);
    }    
    Iterator it = group.getUsersInGroup();
    Vector usernames = new Vector();
    while (it.hasNext()) {
      String user = it.next().toString();
      usernames.addElement(user);
    }
    String[] names = new String[usernames.size()];
    usernames.copyInto(names);
    return names;
  }
}
