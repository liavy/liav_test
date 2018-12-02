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
package com.sap.engine.services.servlets_jsp.server.application;

import com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors5.javaee.JavaIdentifierType;
import com.sap.engine.lib.descriptors5.javaee.LifecycleCallbackType;
import com.sap.engine.lib.injection.InjectionException;
import com.sap.engine.lib.injection.InjectionMatrix;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class InjectionWrapper implements com.sap.faces.injection.Annotations {
  private static Location currentLocation = Location.getLocation(InjectionWrapper.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
  private Hashtable<String, InjectionMatrix> injectionMatrixes = new Hashtable<String, InjectionMatrix>();
  private LifecycleCallbackType[] postConstructMethods = null;
  private LifecycleCallbackType[] preDestroyMethods = null;

  public Hashtable<String, InjectionMatrix> getInjectionMatrixes() {
    return injectionMatrixes;
  }//end of getInjectionMatrixes()

  public void setInjectionMatrixes(Hashtable<String, InjectionMatrix> injectionMatrixes) {
    this.injectionMatrixes = injectionMatrixes;
  }//end of setInjectionMatrixes(Hashtable<String, InjectionMatrix> injectionMatrixes)

  public LifecycleCallbackType[] getPostConstructMethods() {
    return postConstructMethods;
  }//end of getPostConstructMethods()

  public void setPostConstructMethods(LifecycleCallbackType[] postConstructMethods) {
    this.postConstructMethods = postConstructMethods;
  }//end of setPostConstructMethods(LifecycleCallbackType[] postConstructMethods)

  public LifecycleCallbackType[] getPreDestroyMethods() {
    return preDestroyMethods;
  }//end of getPreDestroyMethods()

  public void setPreDestroyMethods(LifecycleCallbackType[] preDestroyMethods) {
    this.preDestroyMethods = preDestroyMethods;
  }//end of setPreDestroyMethods(LifecycleCallbackType[] preDestroyMethods)

  /**
   * Servlet Specification, Chapter SRV.14.5.9 @PostConstruct Annotation
   * The method MUST be called after the resources injections have been
   * completed and before any lifecycle methods on the component are called.
   * The @PostConstruct annotation MUST be supported by all classes that
   * support dependency injection and called even if the class does not request any
   * resources to be injected.
   * TODO If the method throws an unchecked exception the class
   * MUST not be put into service and no method on that instance can be called.
   *
   * @param instance
   */
  public void invokePostContructMethod(Object instance) {
    for (int i = 0; postConstructMethods != null && i < postConstructMethods.length; i++) {
      FullyQualifiedClassType fqct = postConstructMethods[i].getLifecycleCallbackClass();
      if (fqct != null && fqct.get_value() != null) {
        String className = fqct.get_value();
        if (!className.equals(instance.getClass().getName())) {
          continue;
        }

        JavaIdentifierType jit = postConstructMethods[i].getLifecycleCallbackMethod();
        String methodName = jit.get_value();

        try {
          Method method = null;
          for (Class currentClass = instance.getClass(); currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (Method m : currentClass.getDeclaredMethods()) {
              if (m.getName().equals(methodName) && m.getParameterTypes().length == 0) {
                method = currentClass.getDeclaredMethod(methodName, new Class[]{});
                break;
              }
            }
          }

          if (method != null) {
            boolean oldAccessible = method.isAccessible();
            try {
              method.setAccessible(true);
              method.invoke(instance, new Object[]{});
            } finally {
              method.setAccessible(oldAccessible);
            }
          } else {
            throw new NoSuchMethodException();
          }
        } catch (NoSuchMethodException e) {
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000416",
              "Could not invoke PostConstruct method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }   
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000350",
            "Cannot perform initialization operations after resource injection due to a missing method [{0}] of the class [{1}]. " +
            "Problems during lookup of resource objects may occur.", new Object[]{methodName, className}, e, null, null);
        } catch (IllegalAccessException e) {
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000417",
              "Could not invoke PostConstruct method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }   
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000351",
            "Cannot perform initialization operations after resource injection due to a non accessible method [{0}] of the class [{1}]. " +
            "Problems during lookup of resource objects may occur.", new Object[]{methodName, className}, e, null, null);
        } catch (InvocationTargetException e) {
          if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000418", 
              "Could not invoke PostConstruct method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000332",
            "Cannot perform initialization operations after resource injection due to errors that previously occurred:[{0}]. " +
            "Problems during lookup of resource objects may occur.", new Object[]{e.getCause().toString()}, e, null, null);
        }
      }
    }
  }//end of invokePostContructMethod(Object instance)

  /**
   * Servlet Specification, Chapter SRV.14.5.10 @PreDestroy Annotation
   * The method is called prior to component being removed by the container.
   *
   * @param instance
   */
  public void invokePreDestroyMethod(Object instance) {
    for (int i = 0; preDestroyMethods != null && i < preDestroyMethods.length; i++) {
      FullyQualifiedClassType fqct = preDestroyMethods[i].getLifecycleCallbackClass();
      if (fqct != null && fqct.get_value() != null) {
        String className = fqct.get_value();
        if (!className.equals(instance.getClass().getName())) {
          continue;
        }

        JavaIdentifierType jit = preDestroyMethods[i].getLifecycleCallbackMethod();
        String methodName = jit.get_value();

        try {
          Method method = null;
          for (Class currentClass = instance.getClass(); currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (Method m : currentClass.getDeclaredMethods()) {
              if (m.getName().equals(methodName) && m.getParameterTypes().length == 0) {
                method = currentClass.getDeclaredMethod(methodName, new Class[]{});
                break;
              }
            }
          }

          if (method != null) {
            boolean oldAccessible = method.isAccessible();
            try {
              method.setAccessible(true);
              method.invoke(instance, new Object[]{});
            } finally {
              method.setAccessible(oldAccessible);
            }
          } else {
            throw new NoSuchMethodException();
          }
        } catch (NoSuchMethodException e) {
        	if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000419",
              "Could not invoke PreDestroy method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }   
        } catch (IllegalAccessException e) {
        	if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000420",
              "Could not invoke PreDestroy method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }   
        } catch (InvocationTargetException e) {
        	if (traceLocation.beError()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError( "ASJ.web.000421",
              "Could not invoke PreDestroy method [{0}] of the class [{1}].", new Object[]{methodName, className}, e, null, null);
          }   
        }
      }
    }
  }//end of invokePreDestroyMethod(Object instance)

  public void inject(Object instance) throws InjectionException {
    InjectionMatrix injectinMatrix = injectionMatrixes.get(instance.getClass().getName());
    //inject
    if (injectinMatrix != null) {
      injectinMatrix.inject(instance);
    }

    //post construct
    invokePostContructMethod(instance);
  }//end of inject(Object instance)

}//end of class
