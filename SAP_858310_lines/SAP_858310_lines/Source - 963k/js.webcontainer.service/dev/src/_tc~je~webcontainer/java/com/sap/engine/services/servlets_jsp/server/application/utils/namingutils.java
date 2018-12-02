/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application.utils;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class NamingUtils {
  private static Location currentLocation = Location.getLocation(NamingUtils.class);
  public static final String NAMING_CONTEXT = "webContainer/applications";

  /**
   * @return
   * @throws WebDeploymentException
   */
  public static Context createNamingContexts(String aliasForDirectory, String aliasName, String applicationName) throws DeploymentException {
    DirContext ctx = getDirContext(aliasName, true);

    Context subContext = null;
    try {
      subContext = (Context) ctx.lookup(NAMING_CONTEXT);
    } catch (NamingException er) {
      // $JL-EXC$
      try {
        try {
          subContext = ctx.createSubcontext("/" + NAMING_CONTEXT);
        } catch (NameAlreadyBoundException err) {
          // $JL-EXC$
          subContext = (Context) ctx.lookup(NAMING_CONTEXT);
        }
      } catch (NamingException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_NAMING_CONTEXT_ORBIND, new Object[]{aliasName}, e);
      }
    }
    
    try {
      createSubcontext(subContext, applicationName); 
    } catch (NamingException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_NAMING_CONTEXT_ORBIND, new Object[]{applicationName}, e);
    }

    try {
      subContext = (Context) ctx.lookup(NAMING_CONTEXT + "/" + applicationName);
    } catch (NamingException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_NAMING_CONTEXT_ORBIND, new Object[]{applicationName}, e);
    }
    
    try {
      createSubcontext(subContext, aliasForDirectory + "/java:comp/env");
    } catch (NamingException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_NAMING_CONTEXT_ORBIND, new Object[]{applicationName}, e);
    }
 
    return subContext;
  }//end of createNamingContexts(ApplicationContext servletContextFacade)

  /**
   * @param root
   * @param name
   * @param obj
   * @throws NamingException
   */
  public static void bind(Context root, String name, Object obj) throws NamingException {
    Context ic = root;
    Context current = null;
    for (StringTokenizer st = new StringTokenizer(name, "/"); ;) {
      String s = st.nextToken();
      if (st.hasMoreTokens()) {
        try {
          current = (Context) ic.lookup(s);
        } catch (Exception e) {
          // $JL-EXC$
          try {
            current = ic.createSubcontext(s);
          } catch (NameAlreadyBoundException er) {
            // $JL-EXC$
            current = (Context) ic.lookup(s);
          }
        }
        ic = current;
      } else {
        ic.rebind("+/" + s, obj);
        break;
      }
    }
  }//end of bind(Context root, String name, Object obj)

  /**
   * @param applicationName
   */
  public static void unBindApp(String applicationName) {
    Properties p = initEnvironmentProperties();

    Context ctx = null;
    try {
      ctx = new InitialDirContext(p);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000108",
        "Cannot get a naming context for [{0}] application.", new Object[]{applicationName}, e, null, null);
    }

    try {
      if (applicationName.equals("/")) {
        applicationName = Constants.defaultAliasDir;
      }

      Context web = (Context) ctx.lookup(NAMING_CONTEXT + "/" + applicationName);

      destroy(web);

      ((Context) ctx.lookup(NAMING_CONTEXT)).destroySubcontext(applicationName);
    } catch (NamingException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000109",
        "Cannot lookup and destroy the naming context of [{0}] application.", new Object[]{applicationName}, e, null, null);
    }
  }//end of unBindApp(String applicationName)

  public static void unBind(String alias, String applicationName, String aliasForDirectory) {
    DirContext ctx = null;
    try {
      ctx = getDirContext(alias, false);
    } catch (DeploymentException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000110",
        "Cannot get a naming context of [{0}] application.", new Object[]{applicationName}, e, null, null);
    }

    if (ctx != null) {
      try {
        destroyContext((Context) ctx.lookup("/" + NAMING_CONTEXT + "/" + applicationName + "/" + aliasForDirectory));
        ((Context) ctx.lookup("/" + NAMING_CONTEXT + "/" + applicationName)).destroySubcontext(aliasForDirectory);
      } catch (NamingException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000175",
          "Cannot destroy naming naming context of [{0}] application.", new Object[]{applicationName}, e, null, null);
      }
    }
  }//end of unBind(String alias, String applicationName)

  private static Properties initEnvironmentProperties() {
    Properties p = new Properties();
    p.put(Context.INITIAL_CONTEXT_FACTORY, "com.sap.engine.services.jndi.InitialContextFactoryImpl");
    p.put("Replicate", "false");
    p.put("domain", "true");
    return p;
  }//end of initEnvironmentProperties()

  private static DirContext getDirContext(String alias, boolean throwException) throws DeploymentException {
    Properties props = initEnvironmentProperties();

    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(props);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      if (throwException) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_NAMING_CONTEXT_ORBIND,new Object[]{alias}, e);
      }else{
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000111",
            "Cannot get a naming context of [{0}] web application.", new Object[]{alias}, e, null, null);
      }
    }

    return ctx;
  }//end of getDirContext(boolean throwException)

  public static void destroyContext(Context ctxTemp) throws NamingException {
    Enumeration<NameClassPair> enumeration = ctxTemp.list("");

    while (enumeration.hasMoreElements()) {
      NameClassPair nameClassPair = (NameClassPair) enumeration.nextElement();
      String name = nameClassPair.getName();
      String className = nameClassPair.getClassName();

      if ((className != null) & (className.equals("javax.naming.Context"))) {
        destroyContext((Context) ctxTemp.lookup(name));
        ctxTemp.destroySubcontext(name);
      } else {
        ctxTemp.unbind(name);
      }
    }
  }//end of destroyContext(Context ctxTemp)

  /**
   * @param root
   * @throws NamingException
   */
  private static void destroy(Context root) throws NamingException {
    NameClassPair pair = null;

    for (NamingEnumeration<NameClassPair> ne = root.list(""); ne.hasMoreElements();) {
      pair = (NameClassPair) ne.nextElement();
 
      if (isContext(pair.getClassName())) {
        try {
          destroy((Context) root.lookup(pair.getName()));
          root.destroySubcontext(pair.getName());
        } catch (ContextNotEmptyException e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000112",
            "Cannot destroy a naming context.", e, null, null);
        }
      } else {
        root.unbind("+/" + pair.getName());
      }
    }
  }//end of destroy(Context root)

  /**
   * @param nm
   * @return
   */
  private static boolean isContext(String nm) {
    return ((nm.indexOf("javax.naming.Context") != -1) || (nm.indexOf("javax.naming.directory.DirContext") != -1));
  }//end of isContext(String nm)

  /**
   * @param root
   * @param name
   * @throws NamingException
   */
  private static void createSubcontext(Context root, String name) throws NamingException {
    Context current = root;
    String temp = null;
    for (StringTokenizer st = new StringTokenizer(name, "/"); st.hasMoreTokens();) {
      temp = st.nextToken();
      try {
        current = current.createSubcontext(temp);
      } catch (NameAlreadyBoundException er) {
        // $JL-EXC$
        current = (Context) current.lookup(temp);
      }
    }
  }//end of createSubcontext(Context root, String name)

}//end of class
