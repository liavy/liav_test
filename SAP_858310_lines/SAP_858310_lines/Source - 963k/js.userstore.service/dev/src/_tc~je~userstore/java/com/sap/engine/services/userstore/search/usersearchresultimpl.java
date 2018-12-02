/**
 * Copyright:    2002 by SAP AG
 * Company:      SAP AG, http://www.sap.com
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore.search;

import com.sap.engine.interfaces.security.userstore.context.SearchResult;
import com.sap.engine.interfaces.security.userstore.context.UserContext;
import com.sap.engine.interfaces.security.userstore.context.SearchAttribute;
import com.sap.engine.services.userstore.UserContextImpl;

import java.util.*;

/**
 * This class is an implementation of the SearchResult interface to perform search for users.
 * SearchResult is an extension of Iterator and provides additional methods
 * to determine the state of the contained collection of objects.
 *
 * @author  Ekaterina Zheleva
 * @version 6.40
 *
 */
public class UserSearchResultImpl implements SearchResult {
  private int state = SEARCH_RESULT_UNDEFINED;
  private int index = 0;
  private Iterator iterator  = null;
  private Object next  = null;
  private UserContextImpl context = null;
  private int searchResultSize = 0;
  private boolean searchByName = false;
  private boolean searchByParent = false;
  private SearchAttribute nameAttribute = null;
  private SearchAttribute parentAttribute = null;
  private boolean searchLocked = false;

  public UserSearchResultImpl(UserContextImpl context, Iterator attributes, int searchResultSize) {

    this.context = context;
    this.searchResultSize = searchResultSize;
    this.iterator = context.engineListUsers();
    SearchAttribute attribute = null;
    while (attributes.hasNext()) {
      attribute = (SearchAttribute) attributes.next();
      if (attribute.getAttributeName().equals(UserContext.ATTRIBUTE_USERNAME)) {
        searchByName = true;
        nameAttribute = attribute;
      } else if (attribute.getAttributeName().equals(UserContext.ATTRIBUTE_PARENT_GROUP)) {
        searchByParent = true;
        parentAttribute = attribute;
      } else if (attribute.getAttributeName().equals(UserContext.ATTRIBUTE_LOCKED)) {
        searchLocked = attribute.getAttributeValue().equals("true");
      }
    }
  }

  /**
   * @see java.util.Iterator#hasNext()
   */
  public boolean hasNext() {
    if (iterator == null || index == searchResultSize) {
      if (state != SEARCH_RESULT_INCOMPLETE) {
        if (findNext() != null) {
          state = SIZE_LIMIT_EXCEEDED;
        } else {
          state = SEARCH_RESULT_OK;
        }
      }
      return false;
    }

    try {
      next = findNext();
      return (next != null);
    } finally {
      if (next == null && searchResultSize < 0 && state != SEARCH_RESULT_INCOMPLETE) {
        state = SEARCH_RESULT_UNDEFINED;
      }
    }
  }

  /**
   * @see java.util.Iterator#next()
   */
  public Object next() {
    if (iterator == null || index == searchResultSize) {
      if (state != SEARCH_RESULT_INCOMPLETE) {
        if (findNext() != null) {
          state = SIZE_LIMIT_EXCEEDED;
        } else {
          state = SEARCH_RESULT_OK;
        }
      }
      return null;
    }
    if (next != null) {
      index++;
      return next;
    }
    try {
      next = findNext();
      if (next != null) {
        index++;
      }
      return next;
    } finally {
      if (next == null && searchResultSize < 0 && state != SEARCH_RESULT_INCOMPLETE) {
        state = SEARCH_RESULT_UNDEFINED;
      }
    }
  }

  /**
   * @see java.util.Iterator#remove()
   */
  public void remove() {
    //not implemented
  }

  private Object findNext() {
    String node = null;
    while (iterator.hasNext()) {
      node = (String) iterator.next();
      if (searchLocked) {
        try {
          if (((Integer) context.engineGetUserInfo(node).engineReadUserProperty(UserContext.PROPERTY_LOCK_STATUS)).intValue() != 0
              && isMatching(node)) {
            return node;
          }
        } catch (SecurityException se) {
          state = SEARCH_RESULT_INCOMPLETE;
          continue;
        }
      } else {
        if (isMatching(node)) {
          return node;
        }
      }
    }
    return null;
  }

  public int getState () {
    return state;
  }

  private boolean isMatching(String node) {
    boolean isMatching = true;
    if (searchByName) {
      isMatching = matchByNameAttribute(node);
    }
    if (searchByParent) {
      isMatching = isMatching && matchByParentAttribute(node);
    }
    return isMatching;
  }

  private boolean matchByNameAttribute(String node) {
    int operator = nameAttribute.getOperator();
    Object attributeValue = nameAttribute.getAttributeValue();
    switch (operator) {
      case SearchAttribute.EQUALS_OPERATOR: {
        return node.equals(attributeValue);
      }
      case SearchAttribute.LIKE_OPERATOR: {
        return SearchUtil.isLike((String) attributeValue, node);
      }
    }
    return false;
  }

  private boolean matchByParentAttribute(String node) {
    int operator = parentAttribute.getOperator();
    Object attributeValue = parentAttribute.getAttributeValue();
    Iterator parents = null;
    String parent = null;
    try {
      parents = context.engineGetParentGroups(node);
    } catch (Exception _) {
      state = SEARCH_RESULT_INCOMPLETE;
    }
    while (parents != null && parents.hasNext()) {
      parent = (String) parents.next();
      switch (operator) {
        case SearchAttribute.EQUALS_OPERATOR: {
          if (parent.equals(attributeValue)) {
            return true;
          }
          break;
        }
        case SearchAttribute.LIKE_OPERATOR: {
          if (SearchUtil.isLike((String) attributeValue, parent)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}