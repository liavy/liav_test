package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.xml.util.QName;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public final class StandardTypes {
  public static final String SCHEMA_PREFIX            =  "xs";

  private static Hashtable table = new Hashtable();
  private static Hashtable javaUtilTypes = new Hashtable();


  static {
    table.put(boolean.class.getName(), new QName(SCHEMA_PREFIX, "boolean", NS.XS));
    table.put(byte.class.getName(), new QName(SCHEMA_PREFIX, "byte", NS.XS));
    table.put(short.class.getName(), new QName(SCHEMA_PREFIX, "short", NS.XS));
    table.put(int.class.getName(), new QName(SCHEMA_PREFIX, "int", NS.XS));
    table.put(long.class.getName(), new QName(SCHEMA_PREFIX, "long", NS.XS));
    table.put(float.class.getName(), new QName(SCHEMA_PREFIX, "float", NS.XS));
    table.put(double.class.getName(), new QName(SCHEMA_PREFIX, "double", NS.XS));
    table.put(char.class.getName(), new QName(SCHEMA_PREFIX, "string", NS.XS));
    table.put(java.lang.String.class.getName(), new QName(SCHEMA_PREFIX, "string", NS.XS));
    table.put(java.math.BigInteger.class.getName(), new QName(SCHEMA_PREFIX, "integer", NS.XS));
    table.put(java.math.BigDecimal.class.getName(), new QName(SCHEMA_PREFIX, "decimal", NS.XS));
    table.put(java.util.Calendar.class.getName(), new QName(SCHEMA_PREFIX, "dateTime", NS.XS));
    table.put(java.util.Date.class.getName(), new QName(SCHEMA_PREFIX, "dateTime", NS.XS));
    table.put(java.util.GregorianCalendar.class.getName(), new QName(SCHEMA_PREFIX, "dateTime", NS.XS));
    table.put(java.sql.Date.class.getName(), new QName(SCHEMA_PREFIX, "dateTime", NS.XS));
    table.put(java.sql.Time.class.getName(), new QName(SCHEMA_PREFIX, "dateTime", NS.XS));
    table.put(java.lang.Integer.class.getName(), new QName(SCHEMA_PREFIX, "int", NS.XS));
    table.put(java.lang.Float.class.getName(), new QName(SCHEMA_PREFIX, "float", NS.XS));
    table.put(java.lang.Double.class.getName(), new QName(SCHEMA_PREFIX, "double", NS.XS));
    table.put(java.lang.Boolean.class.getName(), new QName(SCHEMA_PREFIX, "boolean", NS.XS));
    table.put(java.lang.Short.class.getName(), new QName(SCHEMA_PREFIX, "short", NS.XS));
    table.put(java.lang.Byte.class.getName(), new QName(SCHEMA_PREFIX, "byte", NS.XS));
    table.put(java.lang.Long.class.getName(), new QName(SCHEMA_PREFIX, "long", NS.XS));
    table.put(java.lang.Character.class.getName(), new QName(SCHEMA_PREFIX, "string", NS.XS));
    table.put(java.lang.Object.class.getName(), new QName(SCHEMA_PREFIX, "anyType", NS.XS));
    table.put("byte[]", new QName(SCHEMA_PREFIX, "base64Binary", NS.XS));

    //java util types initialization
    javaUtilTypes.put(java.util.ArrayList.class.getName(), "");
    javaUtilTypes.put(java.util.Enumeration.class.getName(), "");
    javaUtilTypes.put(java.util.LinkedList.class.getName(), "");
    javaUtilTypes.put(java.util.List.class.getName(), "");
    javaUtilTypes.put(java.util.Set.class.getName(), "");
    javaUtilTypes.put(java.util.HashSet.class.getName(), "");
    javaUtilTypes.put(java.util.SortedSet.class.getName(), "");
    javaUtilTypes.put(java.util.Stack.class.getName(), "");
    javaUtilTypes.put(java.util.Vector.class.getName(), "");
  }

  public static boolean isJavaUtilType(String className) {
    return javaUtilTypes.containsKey(className);
  }

  public static boolean isStandardType(String s) {
    if (table.containsKey(s) || s.equals(Void.class.getName())) {
      return true;
    }
    return false;
  }

  public static boolean isStandardType(Class cl) {
    return isStandardType(cl.getName());
  }

  public static String getMapType(String s) {
    return ((QName) table.get(s)).getQName();
  }

  public static QName getMapTypeInQNameForm(String s) {
    return (QName) table.get(s);
  }

  public static QName getMapTypeInQNameForm(Class cl) {
    return (QName) table.get(cl.getName());
  }

  public static boolean isNillableStandardType(String className) {
    if (className.equals(String.class.getName()) || className.equals(Integer.class.getName()) ||
        className.equals(java.math.BigInteger.class.getName()) || className.equals(java.math.BigDecimal.class.getName()) ||
        className.equals(Calendar.class.getName()) || className.equals(Date.class.getName()) ||
        className.equals(Float.class.getName()) || className.equals(Double.class.getName()) ||
        className.equals(Boolean.class.getName()) || className.equals(Short.class.getName()) ||
        className.equals(Byte.class.getName()) || className.equals(Long.class.getName()) ||
        className.equals(Character.class.getName()) || className.equals(java.util.GregorianCalendar.class.getName()) ||
        className.equals(java.sql.Date.class.getName()) || className.equals(java.sql.Time.class.getName()) ||
        className.equals("[B") || className.equals("byte[]") || className.equals(Object.class.getName()   )) {
      return true;
    }
    return false;
  }

}