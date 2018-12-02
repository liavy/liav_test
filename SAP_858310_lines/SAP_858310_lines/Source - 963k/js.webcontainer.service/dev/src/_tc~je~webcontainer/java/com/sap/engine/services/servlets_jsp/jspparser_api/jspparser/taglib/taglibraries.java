/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import java.util.*;

/**
 * A container for all tag libraries that have been imported using
 * the taglib directive.
 *
 * @author Maria Jurova
 */
public class TagLibraries {

  /**
   * Contains the all TagCache object generated for the actions used in jsp file.
   */
  Hashtable tagCaches = new Hashtable();

  /**
   * Returns the TagCache object generated for the action with name shortTagName.
   *
   * @param   shortTagName  name of the action
   * @return     TagCache object for this action
   */
  public TagCache getTagCache(String shortTagName) {
    return (TagCache) tagCaches.get(shortTagName.trim());
  }

  /**
   * Puts some TagCache object generated for the action with name shortTagName.
   *
   * @param   shortTagName  name of some action
   * @param   tc  TagCache object for this action
   */
  public void putTagCache(String shortTagName, TagCache tc) {
    tagCaches.put(shortTagName.trim(), tc);
  }

}

