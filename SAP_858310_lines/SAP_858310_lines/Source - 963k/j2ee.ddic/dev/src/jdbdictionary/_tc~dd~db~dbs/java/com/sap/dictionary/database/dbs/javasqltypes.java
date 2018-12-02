package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;
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

public class JavaSqlTypes implements DbsConstants {
  private static final Location loc = Location.getLocation(JavaSqlTypes.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);	
  private static final HashMap typesViaName          = new HashMap();
  private static final HashMap typesViaIntValue      = new HashMap();
  private final HashMap typesInfoViaName             = new HashMap();
  private final HashMap typesInfoViaIntValue         = new HashMap();

  //Initialization of hashmaps
  static {
    try {
      Class types = Class.forName("java.sql.Types");
      Field[] fields = types.getFields();
      for (int i=0;i<fields.length; i++) {
        String name  = fields[i].getName();
        int value = fields[i].getInt(types);
        typesViaName.put(name,new Integer(value));
        typesViaIntValue.put(new Integer(value),name);
      }
    }
    catch (Exception ex) {
      cat.warning(loc, EXCEPTION_IN_CLASS, new Object[] {"JavaSqlTypes",ex.getMessage()});
      ex.printStackTrace();                    
   }
  }

  public JavaSqlTypes(DbFactory factory) {
  	try {
      Class types = Class.forName("java.sql.Types");
      Field[] fields = types.getFields();
      JavaSqlTypeInfo info = null;
      for (int i=0;i<fields.length; i++) {
        String name  = fields[i].getName();
        int value = fields[i].getInt(types);
        info = factory.makeJavaSqlTypeInfo(name,value);
        typesInfoViaName.put(name,info);
        typesInfoViaIntValue.put(new Integer(value),info);
      }
    }
    catch (Exception ex) {
      cat.error(loc, EXCEPTION_IN_CLASS, new Object[] {"JavaSqlTypes",ex.getMessage()});
      ex.printStackTrace();
    }
  }

  public static String getName(int value) {
    return (String) typesViaIntValue.get(new Integer(value));
  }

  public static int getIntCode(String name) {
    Object type = typesViaName.get(name.toUpperCase());

    if (type == null) return java.sql.Types.OTHER;
    else {return (((Integer) type).intValue());}
  }

  public JavaSqlTypeInfo getInfo(String name) {
    return (JavaSqlTypeInfo) typesInfoViaName.get(name.toUpperCase());
  }

  public JavaSqlTypeInfo getInfo(int value) {
     return (JavaSqlTypeInfo) typesInfoViaIntValue.get(new Integer(value));
  }
}
