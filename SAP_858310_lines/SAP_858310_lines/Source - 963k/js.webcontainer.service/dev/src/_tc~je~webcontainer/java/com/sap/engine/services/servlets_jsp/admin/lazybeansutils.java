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
package com.sap.engine.services.servlets_jsp.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;

import com.sap.engine.lib.descriptors5.javaee.ResourceRefType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.tc.logging.Location;

/**
 * Utility class for constructing queries for the lazy beans system
 *
 * @author Vera Buchkova
 * @version 7.2 30 Aug 2006
 */
public class LazyBeansUtils {

	public static final String COMMON_STATIC_PAIRS = "*";

	private static Location traceLocation = LogContext.getLocationWebadmin();

	/**
	 * Construct a list containing String objects of the servlet names for
	 * all servlets declared in the local and global deployment descriptors.
	 * As in ITSAMWebModule, servlets are loaded both from local and global DD.
	 * @param localDesc	the local DD
	 * @param globalDesc	the global DD
	 * @return	list of servlet names
	 */
	public static List loadServlets(WebDeploymentDescriptor localDesc, WebDeploymentDescriptor globalDesc) {
    ServletType[] localServletTypes = localDesc == null ? null : localDesc.getServlets();
    ServletType[] globalServletTypes = globalDesc.getServlets();

    List<String> res = new ArrayList<String>(); // element: Servlet

    for (int i = 0; localServletTypes != null && i < localServletTypes.length; i++) {
      String servletName = localServletTypes[i].getServletName().get_value();
      res.add(servletName);
    }

    for (int i = 0; globalServletTypes != null && i < globalServletTypes.length; i++) {
      String servletName = globalServletTypes[i].getServletName().get_value();
      // Check if name already used by local servlet
      boolean nameAlreadyRegistered = false;
      for (int j = 0; !nameAlreadyRegistered && j < res.size(); j++) {
        String registeredServlet = (String) res.get(j);
        nameAlreadyRegistered = servletName.equals(registeredServlet);
      }
      if (!nameAlreadyRegistered) {
        res.add(servletName);
      }
    }

    return res;
  }

	/**
	 * Construct a list containing String objects of the URL resource names for
	 * all URL resources declared in the deployment descriptor.
	 * @param localDesc	local DD
	 * @param globalDesc	the global DD
	 * @return	list of URL resource names
	 */
	public static List loadURLResources(WebDeploymentDescriptor localDesc, WebDeploymentDescriptor globalDesc) {
		List<String> result = new ArrayList<String>();

		ResourceRefType[] resourceRefTypes = localDesc == null ? null : localDesc.getResReferences();
		ResourceRefType[] resourceRefTypesGlobal = globalDesc == null ? null : globalDesc.getResReferences();

    for (int i = 0; resourceRefTypes != null && i < resourceRefTypes.length; i++) {
      ResourceRefType res = resourceRefTypes[i];
      String resType = res.getResType().get_value();
      /*
       * Notes from the 6-Sigma inspection:
       * The following check uses equals and not equalsIgnoreCase in order to
       * be consistent with the same checks in NamingResources.bindResourceReferences(...)
       * and ActionBase.setResourceReferences(...)
       */
      if ("java.net.URL".equalsIgnoreCase(resType)) {
      	result.add(res.getResRefName().get_value());
      }
    }

    for (int i = 0; resourceRefTypesGlobal != null && i < resourceRefTypesGlobal.length; i++) {
      ResourceRefType res = resourceRefTypesGlobal[i];
      String resType = res.getResType().get_value();
      String resRefName = res.getResRefName().get_value();
      if ("java.net.URL".equalsIgnoreCase(resType)) {
      	//Check if resource already declared in local descriptor, if so - skip it:
        boolean alreadyRegistered = false;
        for (int j = 0; !alreadyRegistered && j < result.size(); j++) {
          String registeredURLResource = (String) result.get(j);
          alreadyRegistered = registeredURLResource.equals(resRefName);
        }
        if (!alreadyRegistered) {
        	result.add(resRefName);
        }
      }
    }

    return result;
  }

	/**
	 * Construct a list containing String objects of the Java Mail resource names for
	 * all Java Mail resources declared in the deployment descriptor.
	 * @param localDesc	local DD
	 * @param globalDesc	the global DD
	 * @return	list of Java Mail resource names
	 */
	public static List loadJavaMailResources(WebDeploymentDescriptor localDesc, WebDeploymentDescriptor globalDesc) {
		List<String> result = new ArrayList<String>();

		ResourceRefType[] resourceRefTypes = localDesc == null ? null : localDesc.getResReferences();
		ResourceRefType[] resourceRefTypesGlobal = globalDesc == null ? null : globalDesc.getResReferences();

    for (int i = 0; resourceRefTypes != null && i < resourceRefTypes.length; i++) {
      ResourceRefType res = resourceRefTypes[i];
      String resType = res.getResType().get_value();
      /*
       * Notes from the 6-Sigma inspection:
       * The following check uses equals and not equalsIgnoreCase in order to
       * be consistent with the same checks in NamingResources.bindResourceReferences(...)
       * and ActionBase.setResourceReferences(...)
       */
      if ("javax.mail.Session".equalsIgnoreCase(resType)) {
      	result.add(res.getResRefName().get_value());
      }
    }

    for (int i = 0; resourceRefTypesGlobal != null && i < resourceRefTypesGlobal.length; i++) {
      ResourceRefType res = resourceRefTypesGlobal[i];
      String resType = res.getResType().get_value();
      String resRefName = res.getResRefName().get_value();
      if ("javax.mail.Session".equalsIgnoreCase(resType)) {
      	//Check if resource already declared in local descriptor, if so - skip it:
        boolean alreadyRegistered = false;
        for (int j = 0; !alreadyRegistered && j < result.size(); j++) {
          String registeredURLResource = (String) result.get(j);
          alreadyRegistered = registeredURLResource.equals(resRefName);
        }
        if (!alreadyRegistered) {
        	result.add(resRefName);
        }
      }
    }

    return result;
  }


	/**
	 * Helper method used from the initNamesCache (in particular the registerInNamesCache(...)) method,
	 * in particular when the application is deployed/updated/changed via web admin
	 * or when the web container's lazy provider is being initialized.
	 */
	public static void addNestedMaps(HashMap<String, HashSet<String>> db, String appName, String webModuleName) {
		/*
		 * Notes from the 6-Sigma inspection:
		 * During application update some web modules could be added and other could be removed.
		 * However after receiving this event there is no way to know which are new and
		 * which are old among the web modules of the last(updated) app version.
		 * The check which are the old and new ones should be done in addition, which makes it
		 * not an optimization.
		 * That's why on application update first all previous web modules are removed from
		 * the cache and then all the web modules of the updated version are added.
		 * This is done in WebContainer.onApplicationUpdate, where the removal is
		 * done in invalidateLazyMBeans and this method is called from registerLazyMBeanNames.
		 */
		HashSet<String> modules = (HashSet<String>) db.get(appName);
		if (modules == null) {
			modules = new HashSet<String>();
			db.put(appName, modules);
		}
		modules.add(webModuleName);
	}

	/** Initializes the static pairs (elements for the static part of the ObjectName-s) of the queries */
	public static void initStaticPairs(HashMap<String, HashMap<String, String>> staticPairs,
			String j2eeClusterName, String j2eeClusterClass, String shortJ2eeClusterName) {
		HashMap<String, String> currentStaticPairs = new HashMap<String, String>();
		//Init common static pairs
		currentStaticPairs.put("version", "1.0");
		currentStaticPairs.put("SAP_ITSAMJ2eeCluster.Name", j2eeClusterName);
		currentStaticPairs.put("SAP_ITSAMJ2eeCluster.CreationClassName", j2eeClusterClass);
		currentStaticPairs.put("SAP_ITSAMJ2eeApplication.CreationClassName", "SAP_ITSAMJ2eeApplication");
		currentStaticPairs.put("SAP_ITSAMJ2eeWebModule.CreationClassName", "SAP_ITSAMJ2eeWebModule");
		currentStaticPairs.put("SAP_ITSAMJ2eeWebModule.SystemCreationClassName", "SAP_ITSAMJ2eeApplication");
		currentStaticPairs.put("J2EEServer", shortJ2eeClusterName);
		staticPairs.put(COMMON_STATIC_PAIRS, currentStaticPairs);

		//Init WebModule static pairs
		currentStaticPairs = new HashMap<String, String>();
		currentStaticPairs.put("type", "SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule");
		currentStaticPairs.put("j2eeType", "WebModule");
		staticPairs.put("SAP_ITSAMJ2eeWebModule", currentStaticPairs);

		//Init Servlet static pairs
		currentStaticPairs = new HashMap<String, String>();
		currentStaticPairs.put("type", "SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule.SAP_ITSAMJ2eeServlet");
		currentStaticPairs.put("SAP_ITSAMJ2eeServlet.CreationClassName", "SAP_ITSAMJ2eeServlet");
		currentStaticPairs.put("SAP_ITSAMJ2eeServlet.SystemCreationClassName", "SAP_ITSAMJ2eeWebModule");
		currentStaticPairs.put("j2eeType", "Servlet");
		staticPairs.put("SAP_ITSAMJ2eeServlet", currentStaticPairs);

		//Init URLResource static pairs
		currentStaticPairs = new HashMap<String, String>();
		currentStaticPairs.put("type", "SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeURLResource");
		currentStaticPairs.put("SAP_ITSAMJ2eeURLResource.CreationClassName", "SAP_ITSAMJ2eeURLResource");
		currentStaticPairs.put("SAP_ITSAMJ2eeURLResource.SystemCreationClassName", "SAP_ITSAMJ2eeWebModule");
		currentStaticPairs.put("j2eeType", "URLResource");
		staticPairs.put("SAP_ITSAMJ2eeURLResource", currentStaticPairs);

		//Init JavaMailResource static pairs
		currentStaticPairs = new HashMap<String, String>();
		currentStaticPairs.put("type", "SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeJavaMailResource");
		currentStaticPairs.put("SAP_ITSAMJ2eeJavaMailResource.CreationClassName", "SAP_ITSAMJ2eeJavaMailResource");
		currentStaticPairs.put("SAP_ITSAMJ2eeJavaMailResource.SystemCreationClassName", "SAP_ITSAMJ2eeWebModule");
		currentStaticPairs.put("j2eeType", "JavaMailResource");
		staticPairs.put("SAP_ITSAMJ2eeJavaMailResource", currentStaticPairs);
	}//initStaticPairs

	/**
	 * Initializes the static string parts of the queries.
	 *
	 * There are 4 CIM classes that the WebContainer's provider processes and it
	 * is necessary to construct the static part for each such class;
	 * It is constructed accordingly to the SAP ITSAM Object Name convention
	 * and additionally the required keys from the JSR-77 are added.
	 * The object name should contain the domain (com.sap.default) and key-value pairs.
	 *
	 * Here is the description of some keys:
	 * - cimclass - it has static value SAP_ITSAMJ2eeServlet, SAP_ITSAMJ2eeWebModule,
	 * SAP_ITSAMJ2eeURLResource or SAP_ITSAMJ2eeJavaMailResource and comes from
	 * SAP ITSAM standard;
	 * - type - it has static value:
	 * SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule.SAP_ITSAMJ2eeServlet,
	 * SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule,
	 * SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeURLResource or
	 * SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeJavaMailResource respectively, and comes from
	 * SAP ITSAM standard;
	 * - version - it has static value 1.0;
	 * - SAP_ITSAMJ2eeCluster.Name - it has static value for this server node that is the name of the j2eecluster and comes from SAP ITSAM standard;
	 * - SAP_ITSAMJ2eeCluster.CreationClassName - it has static value the creation class name of the j2eecluster and comes from SAP ITSAM standard;
	 * ...
	 * - j2eeType - it has static value Servlet, WebModule, URLResource or JavaMailResource and comes from JSR-77;
	 * - J2EEServer - its static value is part from the name of the cluster (without the host info) and comes from JSR-77;
	 * The remaining keys are dynamic (could be different for each managed object) and couldn't be added to the static part.
	 *
	 * The order of the different key-value pairs is not important (they could change positions).
	 */
	public static void initStaticObjectNameParts(HashMap<String, String> staticObjectNameParts,
			String j2eeClusterName, String j2eeClusterClass, String shortJ2eeClusterName) {
		/*
		 * Notes from the 6-Sigma inspection:
		 * Here StringBuilder will not be used for String concatenation, because
		 * it is not an optimization since this method is used to initialize the
		 * staticObjectNameParts and is called once at the initialization of the
		 * LazyMBeanProvider, i.e. at server startup and that's why it has been chosen
		 * the current code to be more readable than doubtfully optimized.
		 */
		String currentStaticObjectNamePart = "com.sap.default:" +
			"cimclass=SAP_ITSAMJ2eeServlet," +
			"type=SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule.SAP_ITSAMJ2eeServlet," +
			"version=1.0," +
			"SAP_ITSAMJ2eeCluster.Name=" + j2eeClusterName + "," +
			"SAP_ITSAMJ2eeCluster.CreationClassName=" + j2eeClusterClass + "," +
			"SAP_ITSAMJ2eeApplication.CreationClassName=SAP_ITSAMJ2eeApplication," +
			"SAP_ITSAMJ2eeWebModule.CreationClassName=SAP_ITSAMJ2eeWebModule," +
			"SAP_ITSAMJ2eeWebModule.SystemCreationClassName=SAP_ITSAMJ2eeApplication," +
			"SAP_ITSAMJ2eeServlet.CreationClassName=SAP_ITSAMJ2eeServlet," +
			"SAP_ITSAMJ2eeServlet.SystemCreationClassName=SAP_ITSAMJ2eeWebModule," +
			"j2eeType=Servlet," +
			"J2EEServer=" + shortJ2eeClusterName;
		staticObjectNameParts.put("SAP_ITSAMJ2eeServlet", currentStaticObjectNamePart);

		currentStaticObjectNamePart = "com.sap.default:" +
			"cimclass=SAP_ITSAMJ2eeWebModule," +
			"type=SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeApplication.SAP_ITSAMJ2eeWebModule," +
			"version=1.0," +
			"SAP_ITSAMJ2eeCluster.Name=" + j2eeClusterName + "," +
			"SAP_ITSAMJ2eeCluster.CreationClassName=" + j2eeClusterClass + "," +
			"SAP_ITSAMJ2eeApplication.CreationClassName=SAP_ITSAMJ2eeApplication," +
			"SAP_ITSAMJ2eeWebModule.CreationClassName=SAP_ITSAMJ2eeWebModule," +
			"SAP_ITSAMJ2eeWebModule.SystemCreationClassName=SAP_ITSAMJ2eeApplication," +
			"j2eeType=WebModule," +
			"J2EEServer=" + shortJ2eeClusterName;
		staticObjectNameParts.put("SAP_ITSAMJ2eeWebModule", currentStaticObjectNamePart);

		currentStaticObjectNamePart = "com.sap.default:" +
			"cimclass=SAP_ITSAMJ2eeURLResource," +
			"type=SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeURLResource," +
			"version=1.0," +
			"SAP_ITSAMJ2eeCluster.Name=" + j2eeClusterName + "," +
			"SAP_ITSAMJ2eeCluster.CreationClassName=" + j2eeClusterClass + "," +
			"j2eeType=URLResource," +
			"J2EEServer=" + shortJ2eeClusterName;
		staticObjectNameParts.put("SAP_ITSAMJ2eeURLResource", currentStaticObjectNamePart);

		currentStaticObjectNamePart = "com.sap.default:" +
			"cimclass=SAP_ITSAMJ2eeJavaMailResource," +
			"type=SAP_ITSAMJ2eeCluster.SAP_ITSAMJ2eeJavaMailResource," +
			"version=1.0," +
			"SAP_ITSAMJ2eeCluster.Name=" + j2eeClusterName + "," +
			"SAP_ITSAMJ2eeCluster.CreationClassName=" + j2eeClusterClass + "," +
			"j2eeType=JavaMailResource," +
			"J2EEServer=" + shortJ2eeClusterName;
		staticObjectNameParts.put("SAP_ITSAMJ2eeJavaMailResource", currentStaticObjectNamePart);
	}//initStaticObjectNameParts

	/** Constructs the object name for the Servlet, URLResource and JavaMailResource queries */
	public static ObjectName constructResourceObjectName(HashMap<String, String> staticObjectNameParts,
			int resourceType, String application, String webModule, String resourceName) throws MalformedObjectNameException {
		/*
		 * Notes from the 6-Sigma inspection:
		 * Here StringBuilder is preferred than the + concatenation because the method
		 * is called several times on each query, thus the performance is considered
		 * more important than readability.
		 */
		String staticObjectNamePart = null;
		StringBuilder name = new StringBuilder(",SAP_ITSAMJ2eeApplication.Name=");
		name.append(application);
		name.append(",SAP_ITSAMJ2eeWebModule.SystemName=");
		name.append(application);
		name.append(",SAP_ITSAMJ2eeWebModule.Name=");
		name.append(webModule);
		switch (resourceType) {
			case WebContainerLazyMBeanProvider.RES_TYPE_SERVLET:
				staticObjectNamePart = (String) staticObjectNameParts.get("SAP_ITSAMJ2eeServlet");
				name.append(",SAP_ITSAMJ2eeServlet.SystemName=");
				name.append(webModule);
				name.append(",SAP_ITSAMJ2eeServlet.Name=");
				break;
			case WebContainerLazyMBeanProvider.RES_TYPE_URL:
				staticObjectNamePart = (String) staticObjectNameParts.get("SAP_ITSAMJ2eeURLResource");
				name.append(",SAP_ITSAMJ2eeURLResource.SystemName=");
				name.append(webModule);
				name.append(",SAP_ITSAMJ2eeURLResource.Name=");
				break;
			case WebContainerLazyMBeanProvider.RES_TYPE_JAVAMAIL:
				staticObjectNamePart = (String) staticObjectNameParts.get("SAP_ITSAMJ2eeJavaMailResource");
				name.append(",SAP_ITSAMJ2eeJavaMailResource.SystemName=");
				name.append(webModule);
				name.append(",SAP_ITSAMJ2eeJavaMailResource.Name=");
				break;
			default:
				throw new IllegalArgumentException("Unsupported resourceType argument.");
		}
		name.insert(0, staticObjectNamePart);
		name.append(resourceName);
		name.append(",J2EEApplication=");
		name.append(application);
		name.append(",WebModule=");
		name.append(webModule);
		name.append(",name=");
		name.append(resourceName);
		return new ObjectName(name.toString());
	} //constructResourceObjectName


	/** Constructs the object name for the web module queries */
	public static ObjectName constructWebModuleObjectName(HashMap<String, String> staticObjectNameParts,
			String application, String webModule) throws MalformedObjectNameException {

		String staticObjectNamePart = (String) staticObjectNameParts.get("SAP_ITSAMJ2eeWebModule");
		String name = staticObjectNamePart + ",SAP_ITSAMJ2eeApplication.Name=" + application;
		name += ",SAP_ITSAMJ2eeWebModule.SystemName=" + application;
		name += ",SAP_ITSAMJ2eeWebModule.Name=" + webModule;
		name += ",J2EEApplication=" + application;
		name += ",WebModule=" + webModule;
		name += ",name=" + webModule;
		return new ObjectName(name);
	}

	/**
	 * Adds the object name to the result set only if the query applies or the check fails.
	 * @param query	the query to which the resultName must match in order to be added to the result
	 * @param resultName	the object name to be checked and added to the result
	 * @param result	the result set where the object name to be added
	 * @throws IllegalArgumentException if result is null
	 */
	public static void addFiltered(QueryExp query, ObjectName resultName, Set<ObjectName> result) {
		if (result == null) {
			throw new IllegalArgumentException("The argument result must not be null.");
		}
		if (query == null) {
			throw new IllegalArgumentException("The argument query must not be null.");
		}
		boolean applied = true;
		boolean beDebug = traceLocation.beDebug();
		try {
			applied = query.apply(resultName);
		} catch (BadStringOperationException e) {
			if (beDebug) {
		  	traceDebug("Failed to apply query [" + query + "] on object name [" + resultName +
		  			"]. The object will be added to the result set.", e);
			}
		} catch (BadBinaryOpValueExpException e) {
			if (beDebug) {
		  	traceDebug("Failed to apply query [" + query + "] on object name [" + resultName +
		  			"]. The object will be added to the result set.", e);
			}
		} catch (BadAttributeValueExpException e) {
			if (beDebug) {
		  	traceDebug("Failed to apply query [" + query + "] on object name [" + resultName +
		  			"]. The object will be added to the result set.", e);
			}
		} catch (InvalidApplicationException e) {
			if (beDebug) {
		  	traceDebug("Failed to apply query [" + query + "] on object name [" + resultName +
		  			"]. The object will be added to the result set.", e);
			}
		}
		if (applied) {
			result.add(resultName);
		}
	}//addFiltered

  public static void traceDebug(String msg) {
  	traceLocation.debugT(msg);
  }

  public static void traceDebug(String msg, Throwable t) {
  	traceLocation.debugT(msg + " The exception is: " + LogContext.getExceptionStackTrace(t));
  }
}
