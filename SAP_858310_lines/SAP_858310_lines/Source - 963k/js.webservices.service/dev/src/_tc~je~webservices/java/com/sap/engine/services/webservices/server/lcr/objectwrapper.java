package com.sap.engine.services.webservices.server.lcr;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ObjectWrapper {

  public static  Class objClass = null;
  public Object objInstance = null;

  public ObjectWrapper(Object objInstance) {
    this.objInstance = objInstance;
  }

  public static void initClass(Class objClass) {
    if(ObjectWrapper.objClass == null) {
      ObjectWrapper.objClass = objClass;
    }
  }

  public static void initClass(ClassLoader loader, String className) throws ClassNotFoundException {
    ObjectWrapper.objClass = loader.loadClass(className);
  }

  public static Object invokeStaticMethod(String methodName, Class[] argClasses, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method method = objClass.getMethod(methodName, argClasses);
    return method.invoke(null, args);
  }

  public static Object getStaticField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
    Field field = objClass.getField(fieldName);
    return field.get(null);
  }

  public Object getObjInstance() {
    return objInstance;
  }

  public void setObjInstance(Object objInstance) {
    this.objInstance = objInstance;
  }

  public Class getObjClass() {
    return objInstance.getClass();
  }

  public Object invokeMethod(String methodName, Class[] argClasses, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method method = objInstance.getClass().getMethod(methodName, argClasses);
    return method.invoke(objInstance, args);
  }

  public Object getField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
    Field field = objInstance.getClass().getField(fieldName);
    return field.get(objInstance);
  }

  public static boolean isInstanceOf(Object obj, String className) {
    return isDerivableClass(obj.getClass(), className);
  }

  private static boolean isDerivableClass(Class classInst, String parentClassName) {

    if (classInst == null) {
      return false;
    }

    Class[] interfaces = classInst.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if(interfaces[i].getName().equals(parentClassName)) {
        return true;
      }
    }

    if(classInst.getName().equals(parentClassName)) {
      return true;
    } else {
      return isDerivableClass(classInst.getSuperclass(), parentClassName);
    }
  }

}
