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

import java.util.Iterator;
import com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi;
import com.sap.engine.frame.core.configuration.Configuration;


/**
 */
public class GroupInfoImpl implements GroupInfoSpi {

  private String name = null;
  private GroupContextImpl groupContext = null;

  public GroupInfoImpl(Configuration config) {
    String path = config.getPath();
    this.name = path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi#engineGetName()
   */
  public String engineGetName() {
    return name;
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi#engineGetParentGroups()
   */
  public Iterator engineGetParentGroups() {
    return groupContext.engineGetParentGroups(name);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi#engineGetChildGroups()
   */
  public Iterator engineGetChildGroups() {
    return groupContext.engineGetChildGroups(name);
  }

  /**
   * @see com.sap.engine.interfaces.security.userstore.spi.GroupInfoSpi#engineGetUsersInGroup()
   */
  public Iterator engineGetUsersInGroup() {
    return groupContext.engineGetUsersOfGroup(name);
  }

  public String toString() {
     String result = "Group ID:      " + engineGetName();
     result = result + "\nUsers:         ";
     Iterator users = engineGetUsersInGroup();
     while (users.hasNext()) {
        result = result + (String) users.next();
        if (users.hasNext()) result = result + "\n               ";
     }
     result = result + "\nChild Groups:  ";
     Iterator children = engineGetChildGroups();
     while (children.hasNext()) {
        result = result + (String) children.next();
        if (children.hasNext()) result = result + "\n               ";
     }
     result = result + "\nParent Groups: ";
     Iterator parents = engineGetParentGroups();
     while (parents.hasNext()) {
        result = result + (String) parents.next();
        if (parents.hasNext()) result = result + "\n               ";
     }
     result += "\n";
     return result;
  }

  protected void setGroupContext(GroupContextImpl groupContext) {
    this.groupContext = groupContext;
  }
}


