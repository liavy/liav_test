/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.ParseException;
import com.sap.tc.logging.Location;

import java.beans.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import javax.el.FunctionMapper;
import javax.el.ELException;

/**
 * Introspects some class finding out its setter methods.
 */
public class RunLibrary {
  private static Location currentLocation = Location.getLocation(RunLibrary.class);
  /**
   * Discover what properties are present, and, for each, its name, whether they are
   * simple or indexed, their type, and setter and getter methods.
   *
   * @param   beanObject  bean object
   * @param   servletrequest  servlet request
   */
  public static void introspect(Object beanObject, ServletRequest servletrequest) throws WebServletException {
    for (Enumeration enumeration = servletrequest.getParameterNames(); enumeration.hasMoreElements();) {
      String requestParameterName = (String) enumeration.nextElement();
      String[] requestParameterValues = servletrequest.getParameterValues(requestParameterName);
      if (requestParameterValues != null && requestParameterValues.length > 0) {
        introspecthelper(beanObject, requestParameterName, requestParameterValues);
      }
    }
  }

  /**
   * Helper method for introspect.
   *
   * @param   beanObject  	- the bean object found in the page context
   * @param   requestParameterName  	-	parameter name
   * @param   requestParameterValues  	parameter value as String
   */
  public static void introspecthelper(Object beanObject, String requestParameterName, String[] requestParameterValues) throws WebServletException {
    Method method = null;
    Class beanPropertyClass = null;
    Class propertyEditorClass = null;
    BeanInfo beaninfo = null;
    try {
      beaninfo = Introspector.getBeanInfo(beanObject.getClass());
    } catch (IntrospectionException ie) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000286", 
        "Cannot get BeanInfo for the java bean [{0}]. Introspection fails.", new Object[]{beanObject.getClass()}, ie, null, null);
      return;
    }
    try {
      if (beaninfo != null) {
        PropertyDescriptor propertyDescriptor[] = beaninfo.getPropertyDescriptors();

        for (int i = 0; i < propertyDescriptor.length; i++) {
          if (!propertyDescriptor[i].getName().equalsIgnoreCase(requestParameterName)) {
            continue;
          }

          method = propertyDescriptor[i].getWriteMethod();
          beanPropertyClass = propertyDescriptor[i].getPropertyType();
          propertyEditorClass = propertyDescriptor[i].getPropertyEditorClass();
          break;
        }
      }
      if (method != null) {
        if (beanPropertyClass.isArray()) {
          Class class2 = beanPropertyClass.getComponentType();
          if (class2 == String.class) {
            method.invoke(beanObject, new Object[] {requestParameterValues});
          } else if (propertyEditorClass != null) {
            Object[] tmpval = new Object[requestParameterValues.length];
            for (int i = 0; i < requestParameterValues.length; i++) {
              tmpval[i] = getValueFromBeanInfoPropertyEditor(requestParameterValues[i], propertyEditorClass);
            }
            method.invoke(beanObject, new Object[] {tmpval});
          } else {
            createArray(beanObject, method, class2, requestParameterValues);
          }
        } else {
          if (requestParameterValues == null || requestParameterValues.length == 0) {
            return;
          }
          String requestParameterValue = requestParameterValues[0];
          if (requestParameterValue == null || (requestParameterName != null && requestParameterValue.equals(""))) {
            return;
          }
          Object obj1 = convert(requestParameterValue, beanPropertyClass, propertyEditorClass);
          if (obj1 != null) {
            method.invoke(beanObject, new Object[] {obj1});
          }
        }
      }
    } catch (Exception e) {
      throw new WebServletException(WebServletException.Method_invocation_error, new Object[] {method.getName(), beanObject.getClass()}, e);
    }
  }

  public static void setProp(Object bean, String name, String value) {
    Method method = null;
    Class class1 = null;
    Class propertyEditorClass = null;
    BeanInfo beaninfo = null;
    try {
      beaninfo = Introspector.getBeanInfo(bean.getClass());
    } catch (IntrospectionException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000287", 
          "Cannot get BeanInfo of the java bean [{0}].", new Object[]{bean.getClass()}, e, null, null);
    }

    if (beaninfo != null) {
      PropertyDescriptor apropertydescriptor[] = beaninfo.getPropertyDescriptors();

      for (int i = 0; i < apropertydescriptor.length; i++) {
        if (!apropertydescriptor[i].getName().equalsIgnoreCase(name)) {
          continue;
        }

        method = apropertydescriptor[i].getWriteMethod();
        class1 = apropertydescriptor[i].getPropertyType();
        propertyEditorClass = apropertydescriptor[i].getPropertyEditorClass();
        break;
      }
    }

    if (method != null) {
      Object tmpval = convert(value, class1, propertyEditorClass);
      try {
        method.invoke(bean, new Object[] {tmpval});
      } catch (IllegalAccessException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000288",
            "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, e, null, null);
      } catch (InvocationTargetException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000289", 
            "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, e, null, null);
      }
    }
  }

  public static void setELProp(Object bean, String name, String value, PageContextImpl pageContext, FunctionMapper mapper) throws ELException {
    Method method = null;
    Class class1 = null;
    Class propertyEditorClass = null;
    BeanInfo beaninfo = null;
    try {
      beaninfo = Introspector.getBeanInfo(bean.getClass());
    } catch (IntrospectionException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000290",
          "Cannot get BeanInfo of the java bean [{0}].", new Object[]{bean.getClass()}, e, null, null);
    }

    if (beaninfo != null) {
      PropertyDescriptor apropertydescriptor[] = beaninfo.getPropertyDescriptors();

      for (int i = 0; i < apropertydescriptor.length; i++) {
        if (!apropertydescriptor[i].getName().equalsIgnoreCase(name)) {
          continue;
        }

        method = apropertydescriptor[i].getWriteMethod();
        class1 = apropertydescriptor[i].getPropertyType();
        propertyEditorClass = apropertydescriptor[i].getPropertyEditorClass();
        break;
      }
    }

    if (method != null) {
      Object tmpval = pageContext.evaluateInternal(value, class1, false, mapper);
      try {
        method.invoke(bean, new Object[] {tmpval});
      } catch (IllegalAccessException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation,"ASJ.web.000291",
            "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, e, null, null);
      } catch (InvocationTargetException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000292",
            "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, e, null, null);
      }
    }
  }

  public static String getProp(Object bean, String name) throws Exception {
    String tmpval = "";

    if (bean == null) {
      throw new ParseException(ParseException.BEAN_NOT_FOUND);
    }

    Method method = null;
    BeanInfo beaninfo = Introspector.getBeanInfo(bean.getClass());

    if (beaninfo != null) {
      PropertyDescriptor apropertydescriptor[] = beaninfo.getPropertyDescriptors();

      for (int i = 0; i < apropertydescriptor.length; i++) {
        if (!apropertydescriptor[i].getName().equalsIgnoreCase(name)) {
          continue;
        }

        method = apropertydescriptor[i].getReadMethod();
        break;
      }
    }

    if (method != null) {
      tmpval = toString(method.invoke(bean, null));
    } else {
      throw new ParseException(ParseException.METHOD_OF_BEAN_NOT_FOUND, new Object[]{bean.getClass().getName()});
    }

    return tmpval;
  }

  // ------------------------ PRIVATE ------------------------
  /**
   * Returns new object constructed from given class and value.
   *
   * @param   s              value of object
   * @param   class1         class of object
   * @return     			new Object
   */
  private static Object convert(String s, Class class1, Class propertyEditorClass) {
    if (s == null) {
      if (class1.equals(Boolean.class) || class1.equals(Boolean.TYPE)) {
        s = "false";
      } else {
        return null;
      }
    }

    if (propertyEditorClass != null) {
      return getValueFromBeanInfoPropertyEditor(s, propertyEditorClass);
    }

    if (class1.equals(Boolean.class) || class1.equals(Boolean.TYPE)) {
      if (s.equalsIgnoreCase("true")) {
        s = "true";
      } else {
        s = "false";
      }

      return new Boolean(s);
    }

    if (class1.equals(new Byte("0").getClass()) || class1.equals(Byte.TYPE)) {
      return new Byte(s);
    }

    if (class1.equals(Character.class) || class1.equals(Character.TYPE)) {
      if (s.length() > 0) {
        return new Character(s.charAt(0));
      } else {
        return null;
      }
    }

    if (class1.equals(Short.class) || class1.equals(Short.TYPE)) {
      return new Short(s);
    }

    if (class1.equals(Integer.class) || class1.equals(Integer.TYPE)) {
      return new Integer(s);
    }

    if (class1.equals(Float.class) || class1.equals(Float.TYPE)) {
      return new Float(s);
    }

    if (class1.equals(Long.class) || class1.equals(Long.TYPE)) {
      return new Long(s);
    }

    if (class1.equals(Double.class) || class1.equals(Double.TYPE)) {
      return new Double(s);
    }

    if (class1.equals(String.class)) {
      return s;
    }

    if (class1.equals(File.class)) {
      return new File(s);
    }

    return getValueFromPropertyEditorManager(class1, s);
  }

  /**
   * Uses the given property editor to search the valur for the given bean attribute.
   * @param attrValue - the name of the bean attribute which value is searched in the property editor
   * @param propertyEditorClass - the assosiated property editor for this bean attribute
   * @return the value for this bean atrribute taken from the property editor with getAsText() method.
   */
  public static Object getValueFromBeanInfoPropertyEditor(String attrValue, Class propertyEditorClass) {
    PropertyEditor pe = null;
    try {
      pe = (PropertyEditor) propertyEditorClass.newInstance();
    } catch (InstantiationException ie) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000293", 
          "Cannot get a value from BeanInfo PropertyEditor. {0}", new Object[]{ie.getMessage()}, null, null);
      return null;
    } catch (IllegalAccessException ie) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000294",
          "Cannot get a value from BeanInfo PropertyEditor. {0}", new Object[]{ie.getMessage()}, null, null);
      return null;
    }
    pe.setAsText(attrValue);
    return pe.getValue();
  }

  private static Object getValueFromPropertyEditorManager(Class attrClass, String attrValue) {
    PropertyEditor propEditor = PropertyEditorManager.findEditor(attrClass);
    if (propEditor != null) {
      propEditor.setAsText(attrValue);
      return propEditor.getValue();
    }
    return null;
  }

  private static void createArray(Object bean, Method method, Class fieldClass, String[] values) {
    try {
      if (fieldClass.equals(Integer.class)) {
        Integer[] tmpval = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Integer(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Byte.class)) {
        Byte[] tmpval = new Byte[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Byte(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Boolean.class)) {
        Boolean[] tmpval = new Boolean[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Boolean(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Short.class)) {
        Short[] tmpval = new Short[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Short(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Long.class)) {
        Long[] tmpval = new Long[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Long(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Double.class)) {
        Double[] tmpval = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Double(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Float.class)) {
        Float[] tmpval = new Float[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Float(values[i]);
        }
        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(Character.class)) {
        Character[] tmpval = new Character[values.length];
        for (int i = 0; i < values.length; i++) {
          tmpval[i] = new Character(values[i].charAt(0));
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(int.class)) {
        int[] tmpval = new int[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Integer.parseInt(values[i]);
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(byte.class)) {
        byte[] tmpval = new byte[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Byte.parseByte(values[i]);
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(boolean.class)) {
        boolean[] tmpval = new boolean[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = (Boolean.valueOf(values[i])).booleanValue();
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(short.class)) {
        short[] tmpval = new short[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Short.parseShort(values[i]);
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(long.class)) {
        long[] tmpval = new long[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Long.parseLong(values[i]);
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(double.class)) {
        double[] tmpval = new double[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Double.valueOf(values[i]).doubleValue();
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(float.class)) {
        float[] tmpval = new float[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = Float.valueOf(values[i]).floatValue();
        }

        method.invoke(bean, new Object[] {tmpval});
      } else if (fieldClass.equals(char.class)) {
        char[] tmpval = new char[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = values[i].charAt(0);
        }

        method.invoke(bean, new Object[] {tmpval});
      } else {
        Object[] tmpval = new Object[values.length];

        for (int i = 0; i < values.length; i++) {
          tmpval[i] = getValueFromPropertyEditorManager(fieldClass, values[i]);
        }

        method.invoke(bean, new Object[] {tmpval});
      }
    } catch (IllegalAccessException ex) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000295",
          "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, ex, null, null);
    } catch (InvocationTargetException ex) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000296",
          "Cannot invoke method [{0}] of the java bean [{1}].", new Object[]{method, bean.getClass()}, ex, null, null);
    }
  }

  private static String toString(Object o) {
    if (o == null) {
      return "";
    }
    return o.toString();
  }
}

