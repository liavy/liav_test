package com.sap.security.core.sapmimp.logon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


public class LanguagesBean implements java.io.Serializable {
  private static Location myLoc = Location.getLocation(LanguagesBean.class);
  
  public static final String beanId = "languages";
  private static final String baseName = "languages";
  
  private ArrayList sortedList = null;
  private transient ResourceBundle resourceBundle;
  private Locale locale;
//    private String[][] entries;

  public LanguagesBean() {
    this(Locale.getDefault());
  }
  
  public LanguagesBean(Locale locale) {
    this.resourceBundle = ResourceBundle.getBundle(baseName, locale);
    this.locale = locale;
  }

  private ResourceBundle getResourceBundle() {
    if (resourceBundle == null) {
      resourceBundle = ResourceBundle.getBundle(baseName, locale);
    }
    
    return resourceBundle;
  }
    
  protected void appendHtmlOption(StringBuffer result, String id,
                                  String name, boolean select) {
    result.append("<option value=\"");
    result.append(id);

    if (select) {
      result.append("\" selected >");
    } else {
      result.append("\" >");
    }

    result.append(name);
  } // appendHtmlOption

  public Enumeration getIds() {
    return this.getResourceBundle().getKeys();
  }

  public String getKeyByValue(String value) {
    String key = null;
    Enumeration keys = getIds();

    while (keys.hasMoreElements()) {
      String id = (String) keys.nextElement();
      String name = getName(id);

      if (name.equals(value)) {
        key = id;
        break;
      }
    }

    return key;
  }

  public String getName(String id) {
    try {
      return this.getResourceBundle().getString(id);
    } catch (MissingResourceException ex) {
      myLoc.traceThrowableT(Severity.DEBUG, "getName", ex);
      return "";
    }
  }

  public boolean exists(String id) {
    try {
      this.getResourceBundle().getString(id);
      return true;
    } catch (MissingResourceException ex) {
      myLoc.traceThrowableT(Severity.DEBUG, "exists", ex);
      return false;
    }
  }

  public String getHtmlOptions(String selectedId) {
    if (this.sortedList == null) {
      synchronized (this) {
        // create list which is sorted for name
        sortedList = new ArrayList();

        Enumeration ids = getIds();

        while (ids.hasMoreElements()) {
          String id = (String) ids.nextElement();
          sortedList.add(new Pair(getName(id), id));
        }

        Collections.sort(sortedList);
      }
    }

    // build sorted html options
    Iterator iter = sortedList.iterator();
    StringBuffer result = new StringBuffer("");

    //appendHtmlOption(result, "", "", selectedId != null && !exists(selectedId));
    while (iter.hasNext()) {
      Pair pair = (Pair) iter.next();
      appendHtmlOption(result, pair.getId(), pair.getName(), 
                       pair.getId().equals(selectedId));
    }

    return result.toString();
  } // getHtmlOptions

  // helper class to sort for name
  class Pair implements Comparable, java.io.Serializable {
    private String name;
    private String id;

    Pair(String name, String id) {
      this.name = name;
      this.id = id;
    }

    public int compareTo(Object o) {
      return name.compareTo(((Pair) o).name);
    }

    public String getName() {
      return name;
    }

    public String getId() {
      return id;
    }
  }
}
