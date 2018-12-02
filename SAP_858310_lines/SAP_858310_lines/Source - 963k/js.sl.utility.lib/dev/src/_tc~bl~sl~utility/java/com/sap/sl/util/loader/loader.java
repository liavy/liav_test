package com.sap.sl.util.loader;
/**
 * Title:        Software Delivery Manager
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Software Logistics - here: D019309
 *
 */
public class Loader {
  private static ClassLoader classloader = null;
  
  

  /**
   * Sets the classloader.
   * @param classloader The classloader to set
   */
  public static void setClassloader(ClassLoader classloader) {
    Loader.classloader = classloader;
  }
  
  /**
   * Tries to load the class with the given <code>className</code>
   * and creates an object using the default constructor of the
   * class.
   * When a <code>classloader</code> was set using the 
   * <code>setClassLoader</code> this method tries to load the class 
   * using this classloader. Otherwise the current classloader is used.
   * 
   * @param className: name of the class to be loaded
   * @return an instance of the class
   */
  public static Object getInstanceOfClass(String className) {
    Object result = null;
    Class c = Loader.getClass(className);
    try {
      result =  c.newInstance(); 
    } catch (Exception e) {
      throw new IllegalStateException(
        "Could not instanciate class " + className + ": " + e);
    }
    return result;
  }
  /**
   * Tries to load the class with the given <code>className</code>.
   * When a <code>classloader</code> was set using the 
   * <code>setClassLoader</code> this method tries to load the class 
   * using this classloader. Otherwise the current classloader is used.
   * 
   * @param className: name of the class to be loaded
   * @return a corresponding Class object
   */
  public static Class getClass(String className) {
    Class result = null;
    if (null == Loader.classloader) {
      try {
        result = Class.forName(className);
      } catch (Exception e) {
        throw new IllegalStateException(
          "Could not load class " + className + ": " + e);
      }
    } else {
      try {
        result = Loader.classloader.loadClass(className);
      } catch (Exception e) {
        throw new IllegalStateException(
          "Could not dynamically load class " + className + ": " + e);
      }
    }
    return result;
  }

}
