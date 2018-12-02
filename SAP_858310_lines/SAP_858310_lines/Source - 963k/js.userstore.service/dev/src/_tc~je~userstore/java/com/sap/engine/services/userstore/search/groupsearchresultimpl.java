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
import com.sap.engine.interfaces.security.userstore.context.SearchAttribute;
import com.sap.engine.interfaces.security.userstore.context.GroupContext;
import com.sap.engine.services.userstore.GroupContextImpl;

import java.util.Iterator;

/**
 * This class is an implementation of the SearchResult interface to perform search for groups.
 * SearchResult is an extension of Iterator and provides additional methods to determine
 * the state of the contained collection of objects.
 *
 * @author  Ekaterina Zheleva
 * @version 6.40
 *
 */
public class GroupSearchResultImpl implements SearchResult {
  private int state = SEARCH_RESULT_UNDEFINED;
  int index = 0;
  Iterator iterator  = null;
  Object next  = null;
  GroupContextImpl context = null;
  private int searchResultSize = 0;
  private boolean searchByName = false;
  private boolean searchByParent = false;
  private boolean searchByChildGroup = false;
  private boolean searchByChildUser = false;
  private SearchAttribute nameAttribute = null;
  private SearchAttribute parentAttribute = null;
  private SearchAttribute childGroupAttribute = null;
  private SearchAttribute childUserAttribute = null;

  public GroupSearchResultImpl(GroupContextImpl context, Iterator attributes, int searchResultSize) {
    this.context = context;
    this.searchResultSize = searchResultSize;
    this.iterator = context.engineListGroups();
    SearchAttribute attribute = null;
    while (attributes.hasNext()) {
      attribute = (SearchAttribute) attributes.next();
      if (attribute.getAttributeName().equals(GroupContext.ATTRIBUTE_GROUPNAME)) {
        searchByName = true;
        nameAttribute = attribute;
      } else if (attribute.getAttributeName().equals(GroupContext.ATTRIBUTE_PARENT_GROUP)) {
        searchByParent = true;
        parentAttribute = attribute;
      } else if (attribute.getAttributeName().equals(GroupContext.ATTRIBUTE_CHILD_GROUP)) {
        searchByChildGroup = true;
        childGroupAttribute = attribute;
      } else if (attribute.getAttributeName().equals(GroupContext.ATTRIBUTE_CHILD_USER)) {
        searchByChildUser = true;
        childUserAttribute = attribute;
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

  /**
   * @see com.sap.engine.interfaces.security.userstore.context.SearchResult#getState()
   */
  public int getState () {
    return state;
  }

  private Object findNext() {
    String node = null;
    while (iterator.hasNext()) {
      node = (String) iterator.next();
      if (isMatching(node)) {
        return node;
      }
    }
    return null;
  }

  private boolean isMatching(String node) {
    boolean isMatching = true;
    if (searchByName) {
      isMatching = matchByNameAttribute(node);
    }
    if (searchByParent) {
      isMatching = isMatching && matchByOtherAttribute(node, parentAttribute, GroupContext.ATTRIBUTE_PARENT_GROUP);
    }
    if (searchByChildGroup) {
      isMatching = isMatching && matchByOtherAttribute(node, childGroupAttribute, GroupContext.ATTRIBUTE_CHILD_GROUP);
    }
    if (searchByChildUser) {
      isMatching = isMatching && matchByOtherAttribute(node, childUserAttribute, GroupContext.ATTRIBUTE_CHILD_USER);
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

  private boolean matchByOtherAttribute(String node, SearchAttribute attribute, String type) {
    int operator = attribute.getOperator();
    Object attributeValue = attribute.getAttributeValue();
    Iterator subNodes = null;
    String subnode = null;
    try {
      if (type.equals(GroupContext.ATTRIBUTE_PARENT_GROUP)) {
        subNodes = context.engineGetParentGroups(node);
      } else if (type.equals(GroupContext.ATTRIBUTE_CHILD_GROUP)) {
        subNodes = context.engineGetChildGroups(node);
      } else {
        subNodes = context.engineGetUsersOfGroup(node);
      }
    } catch (Exception _) {
      state = SEARCH_RESULT_INCOMPLETE;
    }
    while (subNodes != null && subNodes.hasNext()) {
      subnode = (String) subNodes.next();
      switch (operator) {
        case SearchAttribute.EQUALS_OPERATOR: {
          if (subnode.equals(attributeValue)) {
            return true;
          }
          break;
        }
        case SearchAttribute.LIKE_OPERATOR: {
          if (SearchUtil.isLike((String) attributeValue, subnode)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
