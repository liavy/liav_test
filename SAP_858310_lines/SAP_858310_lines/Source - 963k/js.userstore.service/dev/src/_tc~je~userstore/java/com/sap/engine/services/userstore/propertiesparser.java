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

import java.util.*;
/**
 * @author Ekaterina Zheleva
 * @author Jako Blagoev
 * @version 6.30
 */
public class PropertiesParser {

  static Vector parseUsersAndGroups(Properties props) {
    String[] userEntry = null;
    String[] groupEntry = null;
    List tempUsers = new ArrayList();
    List tempGroups = new ArrayList();
    Enumeration enumeration = props.propertyNames();
    String propPrefix = null;
    Set prefixSet = new HashSet();
    String isUser = null;

    while (enumeration.hasMoreElements()) {
      propPrefix = (String) enumeration.nextElement();
      if (propPrefix.indexOf(".") == -1) {
        continue;
      }
      propPrefix = propPrefix.substring(0, propPrefix.indexOf("."));
      if (prefixSet.contains(propPrefix)) {
        continue;
      } else {
        prefixSet.add(propPrefix);
        isUser = (String) props.get(propPrefix + ".isUser");
        if (isUser == null) {
          continue;
        } else if (isUser.equals("true")) {
          userEntry = new String[4];
          userEntry[0] = (String) props.get(propPrefix + ".name");
          userEntry[1] = (String) props.get(propPrefix + ".password");
          userEntry[2] = (String) props.get(propPrefix + ".parentGroups");
          userEntry[3] = (String) props.get(propPrefix + ".isLocked");
          tempUsers.add(userEntry);
        } else if (isUser.equals("false")) {
          groupEntry = new String[2];
          groupEntry[0] = (String) props.get(propPrefix + ".name");
          groupEntry[1] = (String) props.get(propPrefix + ".parentGroups");
          tempGroups.add(groupEntry);
        } else {
          continue;
        }
      }
    }
    Vector result = new Vector(2);
    String[][] users = new String[tempUsers.size()][];
    for (int i = 0; i < tempUsers.size(); i++) {
      users[i] = (String[]) tempUsers.get(i);
    }
    result.addElement(users);
    String[][] groups = new String[tempGroups.size()][];
    for (int i = 0; i < tempGroups.size(); i++) {
      groups[i] = (String[]) tempGroups.get(i);
    }
    result.addElement(groups);
    return result;
  }
}