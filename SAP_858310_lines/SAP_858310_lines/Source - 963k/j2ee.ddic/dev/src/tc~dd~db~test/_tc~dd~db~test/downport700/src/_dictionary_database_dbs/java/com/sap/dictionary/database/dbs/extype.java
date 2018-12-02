package com.sap.dictionary.database.dbs;

import java.lang.reflect.*;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class ExType implements DbsConstants {
  String name = " ";
  private static final Location loc = Location.getLocation(ExType.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

  private ExType(String name) {this.name = name;}

  public static final ExType NOT_ON_DB          = new ExType("NOT_ON_DB");
  public static final ExType EXISTS_ON_DB       = new ExType("EXISTS_ON_DB");
  public static final ExType IO_FAILURE         = new ExType("IO_FAILURE");
  public static final ExType SQL_ERROR          = new ExType("SQL_ERROR");
  public static final ExType XML_ERROR          = new ExType("XML_ERROR");
  public static final ExType POSITION_OVERFLOW  = new ExType("POSITION_OVERFLOW");
  public static final ExType OTHER              = new ExType("OTHER");

  public static ExType getInstance(String name) {
    ExType exType = null;

    if (name == null) return null;

    if (name.trim().equalsIgnoreCase("")) return null;
    try {
      Class cl = Class.forName("com.sap.jdd.dbs.ExType");
      try {
        Field field = cl.getField(name);
        exType = (ExType) field.get(cl);
      }
      catch (Exception ex) {//$JL-EXC$
      	cat.warning(loc, EXCEPTION_IN_CLASS, new Object[] {"ExType",ex.getMessage()});
      }
    }
    catch (Exception ex) {//$JL-EXC$
    	cat.warning(loc, EXCEPTION_IN_CLASS, new Object[] {"ExType",ex.getMessage()});
    }
    return exType;
  }

  public String toString() {return name;}
}