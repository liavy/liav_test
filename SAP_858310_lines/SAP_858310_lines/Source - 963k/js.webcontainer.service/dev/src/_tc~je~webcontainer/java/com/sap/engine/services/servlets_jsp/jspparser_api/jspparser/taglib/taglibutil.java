/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.tagext.TagLibraryInfo;

import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.LifoDuplicateMap;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;

/**
 * Helper methods for processing tags.
 * 
 * @author Todor Mollov DEV_webcontainer Jun 30, 2006
 * 
 */
public class TagLibUtil {
  /**
   * Returns the <tt>Class</tt> object associated with the class or interface
   * with the given string name.
   * 
   * <p>
   * The <tt>Class</tt> object is determined by passing the given string name
   * to the <tt>Class.forName()</tt> method, unless the given string name
   * represents a primitive type, in which case it is converted to a
   * <tt>Class</tt> object by appending ".class" to it (e.g., "int.class").
   */
  public static Class toClass(String type, ClassLoader loader) throws ClassNotFoundException {

    Class classResult = null;
    boolean isArray = type.indexOf('[') > 0;
    int dimensionsInArray = 0;
    if (isArray) {
      // This is an array. Count the dimensions
      for (int i = 0; i < type.length(); i++) {
        if (type.charAt(i) == '[')
          dimensionsInArray++;
      }
      type = type.substring(0, type.indexOf('['));
    }

    if ("boolean".equals(type)) {
      classResult = boolean.class;
    } else if ("char".equals(type)) {
      classResult = char.class;
    } else if ("byte".equals(type)) {
      classResult = byte.class;
    } else if ("short".equals(type)) {
      classResult = short.class;
    } else if ("int".equals(type)) {
      classResult = int.class;
    } else if ("long".equals(type)) {
      classResult = long.class;
    } else if ("float".equals(type)) {
      classResult = float.class;
    } else if ("double".equals(type)) {
      classResult = double.class;
    } else { 
      classResult = loader.loadClass(type);
    }

    if ( !isArray )
      return classResult;

    if (dimensionsInArray == 1)
      return java.lang.reflect.Array.newInstance(classResult, 1).getClass();

    // Array of more than i dimension
    return java.lang.reflect.Array.newInstance(classResult, new int[dimensionsInArray]).getClass();
  }
  
  /**
   * Takes all declared tag libraries in a JSP page and returns an array with the unique ones.
   * The uniqueness is implemented in TagLibDescriptor.equalTLDs.
   * @param taglibs - all taglibs in the JSP
   * @return only the unique tag libs from the given ones.
   */
  public static TagLibraryInfo[] getUniqueTaglibs(LifoDuplicateMap<String, TagLibraryInfo> taglibs) {
    List<TagLibraryInfo> tldInfos = new ArrayList<TagLibraryInfo>();
    Collection tldsCol = taglibs.values();
    if( tldsCol != null ) {
      ArrayList<TagLibraryInfo> tlds = new ArrayList<TagLibraryInfo>(tldsCol); 
      for (TagLibraryInfo obj : tlds) {
        if (tldInfos.isEmpty()) {
          tldInfos.add(obj);
        } else {
          boolean found = false;
          for(Object infoObj : tldInfos.toArray()) {
            if (((TagLibDescriptor)obj).equalTLDs((TagLibDescriptor)infoObj)){
              found = true;
              break;
            }
          }
          if (!found) {
            tldInfos.add((TagLibraryInfo)obj);
          }
        }
      }
    }
    TagLibraryInfo[] infos = new TagLibraryInfo[tldInfos.size()];
    for (int i = 0; i < infos.length; i++) {
      infos[i] = tldInfos.get(i);
    }
    return infos;
  }
}
