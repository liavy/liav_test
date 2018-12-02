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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeJavaMailResourceWrapperAdapter;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeServletWrapperAdapter;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeURLResourceWrapperAdapter;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebModuleSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebModuleWrapperAdapter;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.DeployContext;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.jmx.provider.ProviderException;
import com.sap.jmx.provider.adapter.ComplexLazyMBeanProvider;
import com.sap.tc.logging.Location;

/**
 * Lazy provider for Web Container's mbeans
 * ITSAMWebModule, ITSAMServlet, ITSAMJavaMailResource and ITSAMURLResource.
 *
 * @author Jasen Minov
 * @author Vera Buchkova
 * @author Violeta Georgieva
 *
 * @version 7.2 30 Aug 2006
 */
public class WebContainerLazyMBeanProvider extends ComplexLazyMBeanProvider {

	public static final int RES_TYPE_WEBMODULE = 0;
	public static final int RES_TYPE_SERVLET = 1;
	public static final int RES_TYPE_URL = 2;
	public static final int RES_TYPE_JAVAMAIL = 3;

	private ObjectName providerInterceptorObjectName;
	private String registeredProviderID;
	
	private static Location currentLocation = Location.getLocation(WebContainerLazyMBeanProvider.class);
	private static Location traceLocation = LogContext.getLocationWebadmin();

	private static final String[] cimClassNames =
		new String[] {"SAP_ITSAMJ2eeWebModule", "SAP_ITSAMJ2eeServlet", "SAP_ITSAMJ2eeURLResource", "SAP_ITSAMJ2eeJavaMailResource"};

	private static final String[] j2eeTypes =
		new String[] {"WebModule", "Servlet", "URLResource", "JavaMailResource"};

	/**
	 * Maps each cimClassName to the static part string of its object name.
	 * Used to construct the full ObjectName object corresponding to the given CIM class.
	 */
	private HashMap<String, String> staticObjectNameParts = new HashMap<String, String>();
	/**
	 * Maps the cimClassNames and COMMON_STATIC_PAIRS to the name-value pairs of the fields
	 * that are static for the corresponding cimClass or are static for all cim classes
	 * in the case of COMMON_STATIC_PAIRS.
	 * Used for the search, i.e. to complete the object name by finding its
	 * missing parts, in queryNamesInternal method.
	 */
	private HashMap<String, HashMap<String, String>> staticPairs = new HashMap<String, HashMap<String, String>>();

	/**
	 * Cache the application and web module names;
	 * loads them during server start time and update when application update.
	 * Maps application names to their web module names set.
	 **/
	private HashMap<String, HashSet<String>> namesCache = null;

	private MBeanServer jmxServer = null;
	
	private SAP_ITSAMJ2eeWebModuleSettings globalCompositeData = null;

	/**
	 * All supported cimclass and j2eeType types are defined in the constructor.
	 * Only the calls containing at least one of the defined values for the keys
	 * cimclass and j2eeType will be delegated to the provider (to its queryNames method).
	 *
	 * @param webcontainer the object holding the global web application data that
	 * 															is shared among all ITSAMWebModule mbeans
	 */
	public WebContainerLazyMBeanProvider(MBeanServer _jmxServer) {
		super(cimClassNames, j2eeTypes);
		if (traceLocation.beDebug()) {
			traceDebug("WebContainerLazyMBeanProvider constructor.");
		}
		this.jmxServer = _jmxServer;
	}

	/*
	 * Initializes the provider.
	 * Usually here the missing parts from the ObjectNames of the MBeans are retrieved.
	 * For example such part from the object name is the J2ee cluster name -
	 * there are two main ways to get it and they depend mainly where the provider is instantiated.
	 * If the provider is instantiated as part from the application startup
	 * then a simple query will be enough to retrieve the j2ee cluster name.
	 * If the provider is instantiated as part from the j2ee service startup than
	 * a special APIs are used to retrieve the j2ee cluster name.
	 */
	protected void init() throws JMException {
		boolean beDebug = traceLocation.beDebug();
		if (beDebug) {
    	traceDebug("Calls init() on WebContainerLazyMBeanProvider...");
		}
		//Step 1 - Retrieve the j2ee cluster name parts that will be used during the
		//managed object initialization and construct the static part from the object name.

		//Step 1.1 - get the Initial context
//		try {
			//Step 1.3 - query J2ee cluster MBean; the result is a Set of object names
			Set result = jmxServer.queryNames(new ObjectName("*:cimclass=SAP_ITSAMJ2eeCluster,*"), null);
			if (result == null || result.isEmpty()) {
				if (beDebug) {
		    	traceDebug("Failed to init the web container's lazy mbean provider because the cluster mbean object cannot be found.");
				}
				throw new JMException("Java EE cluster MBean was not found");
			} else if (result.size() > 1) {
				if (beDebug) {
		    	traceDebug("Found more than one the cluster mbean objects during init of the web container's lazy mbean provider.");
				}
			}

			ObjectName j2eeClusterObjectName = (ObjectName) result.iterator().next();

			//Step 1.4 - retrieve the needed info from the j2ee cluster object name
			//e.g. SAP_ITSAMJ2eeCluster.Name=E21.SystemHome.sofd12345678a
			//     SAP_ITSAMJ2eeCluster.CreationClassName=SAP_ITSAMJ2eeCluster
			String j2eeClusterName = j2eeClusterObjectName.getKeyProperty("SAP_ITSAMJ2eeCluster.Name");
			String j2eeClusterClass = j2eeClusterObjectName.getKeyProperty("SAP_ITSAMJ2eeCluster.CreationClassName");
			if (j2eeClusterName == null) {
				if (beDebug) {
		    	traceDebug("Failed to init the web container's lazy mbean provider because the cluster name object cannot be found.");
				}
				throw new JMException("Key SAP_ITSAMJ2eeCluster.Name was not found into the Java EE cluster object name " + j2eeClusterObjectName);
			}
			if (j2eeClusterClass == null) {
				if (beDebug) {
		    	traceDebug("Failed to init the web container's lazy mbean provider because the cluster class object cannot be found.");
				}
				throw new JMException("SAP_ITSAMJ2eeCluster.CreationClassName was not found into the Java EE cluster object name " + j2eeClusterObjectName);
			}
			//e.g. shortJ2eeClusterName == E21
			String shortJ2eeClusterName = j2eeClusterName.substring(0, j2eeClusterName.indexOf('.'));

			//Step 1.5 - construct the static part strings of the mbean object names:
			LazyBeansUtils.initStaticObjectNameParts(staticObjectNameParts, j2eeClusterName, j2eeClusterClass, shortJ2eeClusterName);

			//Step 2 - connect to the management object (in this case web container).
			//Loads the cached names of the applications and their aliases (names cache is used for optimization):
			initNamesCache();
			//connectoToWebContainer(); //not needed yet - implement it for the getConnected(..) method

			//Inits the static parts of each managed class as a key-value map and those that are common for all web container's managed objects:
			LazyBeansUtils.initStaticPairs(staticPairs, j2eeClusterName, j2eeClusterClass, shortJ2eeClusterName);

      //Step 3 instantiate the MBeans that are not lazy and register them to the server: here we don't have any non-lazy MBeans, thus do nothing
//		} catch(NamingException nException) {
//			if (beDebug) {
//	    	traceDebug("Failed to init the web container's lazy mbean provider.", nException);
//			}
//			throw new JMException(nException.getMessage());
//		} catch(IOException ioException) {
//			if (beDebug) {
//	    	traceDebug("Failed to init the web container's lazy mbean provider.", ioException);
//			}
//			throw new JMException(ioException.getMessage());
//		}
		if (beDebug) //{
    	traceDebug("init() on WebContainerLazyMBeanProvider finished successfully.");
//		}
	}//init()

	/**
	 * Preserved for backwards compatibility: it should not be called anymore.
	 * @deprecated replaced by queryNames(ObjectName, QueryExp)
	 */
	public Set queryNames(ObjectName name) {
		return queryNames(name, null);
	}

	/*
	 * Searches among mbeans with domain "com.sap.default", empty or "*".
	 * The object names "*:*" and null must return all web container's mbeans,
	 * and filtered if a query expression is specified.
	 *
	 * @param	name
	 * @param query
	 */
	public Set queryNames(ObjectName name, QueryExp query) {
		return queryNamesInternal(name, query, true);
	}	//queryNames

	/**
	 * Searches among mbeans with domain "com.sap.default", empty or "*".
	 * The object names "*:*" and null must return all web container's mbeans,
	 * and filtered if a query expression is specified.
	 *
	 * For example
	 * queryNames(null, new ObjectName(*:j2eeType=SAP_J2EEClusterNode,SAP_J2EECluster="",*))
	 * should return empty set.
	 *
	 * Used internally to distinguish the case of unregistration of the mbeans, during
	 * which some exceptions should not be shown in the trace. This was done because it
	 * was not possible to invalidate mbeans in cluster and is needed until
	 * the mbeans invalidate by pattern gets implemented in JMX.
	 *
	 * @param name
	 * @param query
	 * @param showErrors	when true writes exceptions in the log by default; ow only in increased levels;
	 * 	When query is made internally, e.g. from onApplicationRemoved on one server node, while on
	 *  the other nodes it was already removed and there's no config in apps, the configuration exception
	 *  is not an error and should not be logged by default.
	 * @return
	 */
	private Set queryNamesInternal(ObjectName name, QueryExp query, boolean showErrors) {
		boolean beDebug = traceLocation.beDebug();
		if (beDebug) {
    	traceDebug("Calls queryNames() with name=" + name + " and query=" + query);
		}
    //TODO: Refactor with 4 separated methods for each mbean type to fill the result set successively

		//The name that is passed as argument is in fact object name pattern;
		//The algorithm that could be implemented by each provider and is implemented
		//by WebContainer's Provider is the following:
		// 1. Check the domain
		// 2. Iterate through all the key-value pairs that this object name contains;
		// for each such pair next cases are possible:
		//		- the key is from the static object name part;
		//			the value equals to the static one;
		//			ignore this pair (it is fine for all MBeans)
		//    - the key is from the static object name part;
		//			the value is different from the static one;
		//			return empty set
		//		- the key is from the dynamic object name part;
		//			retrieve the value for later search;
		//			if retrieved value is in contradiction with some already retrieved values then return empty set
		//		- the key is not part from the static object name part nor from the dynamic object name part
		//			return empty set
		// 3. If there are already retrieved values for search - use them to retrieve all matching MBean names
		// 4. Return set with the object names of all matching the search criteria MBeans

		if (name == null) {
			//Must return the same as the "*:*" query: all web container's mbeans.
			try {
				name = new ObjectName("*:*");
			} catch (MalformedObjectNameException e) {
				if (beDebug) {
		    	traceDebug("Failed to process null query.", e);
				}
        return new HashSet();
			} catch (NullPointerException e) {
				if (beDebug) {
		    	traceDebug("Failed to process null query.", e);
				}
        return new HashSet();
			}
		}

		//Step 1
		String domain = name.getDomain();
		if (!(domain.equals("com.sap.default") || domain.equals("") || domain.equals(LazyBeansUtils.COMMON_STATIC_PAIRS))) {
      return new HashSet();
		}

		//Step 2
		String applicationName = null;
		String webModuleName = null;
		String servletName = null;
		String urlResourceName = null;
		String javaMailResourceName = null;

		HashMap<String, String> commonStaticPairs = (HashMap<String, String>) staticPairs.get(LazyBeansUtils.COMMON_STATIC_PAIRS);
		HashMap<String, String> webModuleStaticPairs = (HashMap<String, String>) staticPairs.get("SAP_ITSAMJ2eeWebModule");
		HashMap<String, String> servletStaticPairs = (HashMap<String, String>) staticPairs.get("SAP_ITSAMJ2eeServlet");
		HashMap<String, String> urlResourceStaticPairs = (HashMap<String, String>) staticPairs.get("SAP_ITSAMJ2eeURLResource");
		HashMap<String, String> javaMailResourceStaticPairs = (HashMap<String, String>) staticPairs.get("SAP_ITSAMJ2eeJavaMailResource");

		String commonStaticValue = null;
		String webModuleStaticValue = null;

		String servletStaticValue = null;
		String urlResourceStaticValue = null;
		String javaMailResourceStaticValue = null;

		Hashtable pairs = name.getKeyPropertyList();
		Iterator iter = pairs.keySet().iterator();
		String undefName = null;

		boolean isAllQuery = pairs.isEmpty();//it is "*:*" query and must return all web container' mbeans
		boolean isWebModuleQuery = false;
		boolean isServletQuery = false;
		boolean isURLResourceQuery = false;
		boolean isJavaMailResourceQuery = false;

		while (iter.hasNext()) {
			String key = (String) iter.next();
      String value = (String) pairs.get(key);

			commonStaticValue = (String) commonStaticPairs.get(key);
			webModuleStaticValue = (String) webModuleStaticPairs.get(key);
			servletStaticValue = (String) servletStaticPairs.get(key);
			urlResourceStaticValue = (String) urlResourceStaticPairs.get(key);
			javaMailResourceStaticValue = (String) javaMailResourceStaticPairs.get(key);

      if (commonStaticValue != null) {
      	//it is a static key
      	if (!commonStaticValue.equals(value)) {
      		//the keys of the common static pairs do not match any of the other static pairs keys
      		//(see LazyBeansUtils.initStaticPairs()) so there is no need to check further:
      		return new HashSet();
      	}
      } else {
      	//commonStaticValue is null, i.e. is not from the common part, then search the other values
      	boolean searchStaticValueFurther = true;

      	if (webModuleStaticValue != null) {
      		//it is a static key
      		if (webModuleStaticValue.equals(value)) {
      			if (isURLResourceQuery || isJavaMailResourceQuery || isServletQuery) {
      				//the query is wrong - it cannot be of several types at a time:
      				return new HashSet();
      			}
      			isWebModuleQuery = true;
      			searchStaticValueFurther = false;
      		}
      	}
      	if (servletStaticValue != null && searchStaticValueFurther) {
      		//it is a static key
      		if (servletStaticValue.equals(value)) {
      			if (isURLResourceQuery || isJavaMailResourceQuery || isWebModuleQuery) {
      				//the query is wrong - it cannot be of several types at a time:
      				return new HashSet();
      			}
      			isServletQuery = true;
      			searchStaticValueFurther = false;
      		}
      	}
      	if (urlResourceStaticValue != null && searchStaticValueFurther) {
      		//it is a static key
      		if (urlResourceStaticValue.equals(value)) {
      			if (isServletQuery || isJavaMailResourceQuery || isWebModuleQuery) {
      				//the query is wrong - it cannot be of several types at a time:
      				return new HashSet();
      			}
      			isURLResourceQuery = true;
      			searchStaticValueFurther = false;
      		}
      	}
      	if (javaMailResourceStaticValue != null && searchStaticValueFurther) {
      		//it is a static key
      		if (javaMailResourceStaticValue.equals(value)) {
      			if (isServletQuery || isURLResourceQuery || isWebModuleQuery) {
      				//the query is wrong - it cannot be of several types at a time:
      				return new HashSet();
      			}
      			isJavaMailResourceQuery = true;
      			searchStaticValueFurther = false;
      		}
      	}
      	if (commonStaticValue != null || webModuleStaticValue != null || servletStaticValue != null || urlResourceStaticValue != null || javaMailResourceStaticValue != null) {
      		if (searchStaticValueFurther) {
      			//all static values have been searched and nothing found, i.e. the value does not match anything:
      			return new HashSet();
      		}
      	} else {
					//it is not a static key - compare with all other keys that are part from the object name
					if ("cimclass".equals(key)) {
						if ("SAP_ITSAMJ2eeWebModule".equals(value)) {
							//check only for inconsistency
							isWebModuleQuery = true;
							if (isServletQuery || isURLResourceQuery || isJavaMailResourceQuery) {
					      return new HashSet();
							}
						} else if ("SAP_ITSAMJ2eeServlet".equals(value)) {
							isServletQuery = true;
							if (isURLResourceQuery || isJavaMailResourceQuery || isWebModuleQuery) {
					      return new HashSet();
							}
						} else if ("SAP_ITSAMJ2eeURLResource".equals(value)) {
							isURLResourceQuery = true;
							if (isServletQuery || isJavaMailResourceQuery || isWebModuleQuery) {
					      return new HashSet();
							}
						} else if ("SAP_ITSAMJ2eeJavaMailResource".equals(value)) {
							isJavaMailResourceQuery = true;
							if (isServletQuery || isURLResourceQuery || isWebModuleQuery) {
					      return new HashSet();
							}
						}
					} else if (key.equals("SAP_ITSAMJ2eeApplication.Name") || key.equals("SAP_ITSAMJ2eeWebModule.SystemName") || key.equals("J2EEApplication")) {
						//the key defines the j2ee application name
						if (applicationName == null) {
							applicationName = value;
						} else if (!applicationName.equals(value)) {
					    return new HashSet();
						}
					} else if (key.equals("SAP_ITSAMJ2eeWebModule.Name")
							|| key.equals("SAP_ITSAMJ2eeServlet.SystemName")
							|| key.equals("SAP_ITSAMJ2eeJavaMailResource.SystemName")
							|| key.equals("SAP_ITSAMJ2eeURLResource.SystemName")
							|| key.equals("WebModule")) {
						//the key defines the web module name
						if (webModuleName == null) {
							webModuleName = value;
						} else if (!webModuleName.equals(value)) {
					    return new HashSet();
						}
					} else if (key.equals("SAP_ITSAMJ2eeServlet.Name")) {
						//the key defines the servlet name
						isServletQuery = true;
						if (isURLResourceQuery || isJavaMailResourceQuery) {
					    return new HashSet();
						}
						if (servletName == null) {
							servletName = value;
						} else if (!servletName.equals(value)) {
					    return new HashSet();
						}
					} else if (key.equals("SAP_ITSAMJ2eeURLResource.Name")) {
						//the key defines the URLResource name
						isURLResourceQuery = true;
						if (isServletQuery || isJavaMailResourceQuery) {
					    return new HashSet();
						}
						if (urlResourceName == null) {
							urlResourceName = value;
						} else if (!urlResourceName.equals(value)) {
					    return new HashSet();
						}
					} else if (key.equals("SAP_ITSAMJ2eeJavaMailResource.Name")) {
						//the key defines the JavaMailResource name
						isJavaMailResourceQuery = true;
						if (isServletQuery || isURLResourceQuery) {
					    return new HashSet();
						}
						if (javaMailResourceName == null) {
							javaMailResourceName = value;
						} else if (!javaMailResourceName.equals(value)) {
					    return new HashSet();
						}
					} else if (key.equals("name")) {
						undefName = value;
					} else {
						//key was not recognized by the web container
						return new HashSet();
					}
	      	}
        }
		} //iterate on pairs of the query
		if (undefName != null) {
			if (isServletQuery) {
				if (servletName != null && !servletName.equals(undefName)) {
					//conflicts with already retrieved value
					return new HashSet();
				}
				servletName = undefName;
			} else if (isURLResourceQuery) {
				if (urlResourceName != null && !urlResourceName.equals(undefName)) {
					//conflicts with already retrieved value
					return new HashSet();
				}
				urlResourceName = undefName;
			} else if (isJavaMailResourceQuery) {
				if (javaMailResourceName != null && !javaMailResourceName.equals(undefName)) {
					//conflicts with already retrieved value
					return new HashSet();
				}
				javaMailResourceName = undefName;
			} else if (isWebModuleQuery) {
				if (webModuleName != null && !webModuleName.equals(undefName)) {
					//conflicts with already retrieved value
					return new HashSet();
				}
				webModuleName = undefName;
			} else {
        return new HashSet();
			}
		}
		//Step 3
		Set<ObjectName> result = new HashSet<ObjectName>();
		String currentApplicationName = null;
		Iterator applicationNameIterator = null;
		if (applicationName != null) {
			currentApplicationName = applicationName;
		} else {
			//init the iteration on all applications
			applicationNameIterator = namesCache.keySet().iterator();
			if (applicationNameIterator.hasNext()) {
				currentApplicationName = (String) applicationNameIterator.next();
			}
		}
		//loop the iteration on all applications
		while (currentApplicationName != null) {
			if (applicationName == null || applicationName.equals(currentApplicationName)) {
				HashSet modulesData = (HashSet) namesCache.get(currentApplicationName);
				if (modulesData != null) {
					String currentWebModuleName = null;
					Iterator webModuleNameIterator = null;
					if (webModuleName != null) {
						if (modulesData.contains(webModuleName)) {
							currentWebModuleName = webModuleName;
						}
					} else {
						//init the iteration on all web modules
						webModuleNameIterator = modulesData.iterator();
						if (webModuleNameIterator.hasNext()) {
							currentWebModuleName = (String) webModuleNameIterator.next();
						}
					}
					//loop the iteration on all web modules
					while (currentWebModuleName != null) {
						if (webModuleName == null || webModuleName.equals(currentWebModuleName)) {
							if (isServletQuery) {
								getResourceData(RES_TYPE_SERVLET, servletName, currentWebModuleName, currentApplicationName, query, result, showErrors);
							} else if (isURLResourceQuery) {
								getResourceData(RES_TYPE_URL, urlResourceName, currentWebModuleName, currentApplicationName, query, result, showErrors);
							} else if (isJavaMailResourceQuery) {
								getResourceData(RES_TYPE_JAVAMAIL, javaMailResourceName, currentWebModuleName, currentApplicationName, query, result, showErrors);
							} else if (isWebModuleQuery) {
								try {
									ObjectName webModuleObjectName = LazyBeansUtils.constructWebModuleObjectName(staticObjectNameParts, currentApplicationName, currentWebModuleName);
									if (query != null) {
										LazyBeansUtils.addFiltered(query, webModuleObjectName, result);
									} else {
										result.add(webModuleObjectName);
									}
								} catch (MalformedObjectNameException monException) {
									if (beDebug) {
							    	traceDebug("Failed to create web module's object name for application [" + currentApplicationName +
							    			"] and web module [" + currentWebModuleName + "].", monException);
									}
								}
							} else if (isAllQuery) {
								//is a query for all mbeans
								getResourceData(RES_TYPE_SERVLET, servletName, currentWebModuleName, currentApplicationName, query, result, showErrors);
								getResourceData(RES_TYPE_URL, urlResourceName, currentWebModuleName, currentApplicationName, query, result, showErrors);
								getResourceData(RES_TYPE_JAVAMAIL, javaMailResourceName, currentWebModuleName, currentApplicationName, query, result, showErrors);
								try {
									ObjectName webModuleObjectName = LazyBeansUtils.constructWebModuleObjectName(staticObjectNameParts, currentApplicationName, currentWebModuleName);
									if (query != null) {
										LazyBeansUtils.addFiltered(query, webModuleObjectName, result);
									} else {
										result.add(webModuleObjectName);
									}
								} catch (MalformedObjectNameException monException) {
									if (beDebug) {
							    	traceDebug("Failed to create web module's object name for application [" + currentApplicationName +
							    			"] and web module [" + currentWebModuleName + "].", monException);
									}
								}
							} else {
								//for unrecognized query must return empty result set
                return new HashSet();
							}
						}//if webModuleName == currentWebModuleName
						if ((webModuleNameIterator != null) && webModuleNameIterator.hasNext()) {
							currentWebModuleName = (String) webModuleNameIterator.next();
						} else {
							currentWebModuleName = null;
						}
					} //iterate on all web modules;
				}
			} //if applicationName == currentApplicationName
			if ((applicationNameIterator != null) && applicationNameIterator.hasNext()) {
				currentApplicationName = (String) applicationNameIterator.next();
			} else {
				currentApplicationName = null;
			}
		} //iterate on all applications
		//Step 4
		if (beDebug) {
    	traceDebug("Calls queryNames() with name=" + name + " and query=" + query + " will return result=" + result);
		}
		return result;
	}

	/*
	 * Here is the place where the instantiation of the MBeans is done.
	 * Do not keep any reference to the instantiated MBeans, it is
	 * up to the lazy system to keep such references.
	 * It is possible for the lazy system to instantiate several times the same MBean,
	 * but it will register only one of this instance to the MBean server at any moment.
	 * If you couldn't fulfill the request of instantiation of the MBean just return null.
	 */
	public Object instantiateMBean(ObjectName name) {
		// Here we apply next algorithm:
    // - Find which cim class (or j2ee type) is requested with this object name
		// - Instantiate the mbean
		// - return the MBean
		if (traceLocation.beDebug()) {
    	traceDebug("Calls instantiateMBean() with object name=" + name);
		}
		Object result = null;

		String cimclass = name.getKeyProperty("cimclass");
		try {
			if ("SAP_ITSAMJ2eeWebModule".equals(cimclass)) {
				result = instantiateMBean(RES_TYPE_WEBMODULE, name);
			} else if ("SAP_ITSAMJ2eeServlet".equals(cimclass)) {
				result = instantiateMBean(RES_TYPE_SERVLET, name);
			} else if ("SAP_ITSAMJ2eeURLResource".equals(cimclass)) {
				result = instantiateMBean(RES_TYPE_URL, name);
			} else if ("SAP_ITSAMJ2eeJavaMailResource".equals(cimclass)) {
				result = instantiateMBean(RES_TYPE_JAVAMAIL, name);
			} else {
				if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_WEBADMIN).traceWarning("ASJ.web.000444",
							"Failed to instantiate mbean of unrecognized cimclass [{0}]", new Object[]{cimclass}, null, null);
				}
			}
		} catch (MBeanRegistrationException e) {
			if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_WEBADMIN).traceWarning("ASJ.web.000445",
						"Failed to instantiate mbean of cimclass [{0}]", new Object[]{cimclass}, e, null, null);
			}
		} catch (NotCompliantMBeanException e) {
			if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_WEBADMIN).traceWarning("ASJ.web.000446",
						"Failed to instantiate mbean of cimclass [{0}]", new Object[]{cimclass}, e, null, null);
			}
		} catch (InstanceAlreadyExistsException e) {
			if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_WEBADMIN).traceWarning("ASJ.web.000447",
						"Failed to instantiate mbean of cimclass [{0}]", new Object[]{cimclass}, e, null, null);
			}
		}

		return result;
	}

	/*
	 * Here is the place where to get connected MBeans to an MBean that is already loaded. Usually the user browse the UI following
	 * some associations between the entities (for example when the user browse a specific servlet it is highly possible to see
	 * the other servlets from the same web module. The method below is called by the system exactly for this purpose - to ask for
	 * connected with the existing entity and to load them before the user ask for them. The implementation of this method is not
	 * mandatory but it could increase the performance of the system.
	 *
	 * Into our implementation we will return all the names of the servlets that belong to the same web module.
	 */
	public ObjectName[] getConnected(ObjectName name, Object mBean)	throws ProviderException, JMException {
		//TODO: implement
		if (traceLocation.beDebug()) {
    	traceDebug("WebContainerLazyMBeanProvider.getConnected is not yet implemented and will return null.");
		}
		return null;
	}


	/*
	 * Here is the place where the unregistration of the non lazy MBeans is done.
	 * Also some other resources could be released (for example db connections).
	 */
	public void unregisterProvider() {
		if (traceLocation.beDebug()) {
			traceDebug("Destroy WebContainerLazyMBeanProvider.");
		}
		try {
			this.jmxServer.invoke(providerInterceptorObjectName, "unregisterProvider",
					new Object[] {registeredProviderID}, new String[] {"java.lang.String"});
			this.jmxServer = null;
			this.registeredProviderID = null;
			this.providerInterceptorObjectName = null;

		} catch (Exception ex) {
			logError("ASJ.web.000237", "Problem occurred while unregistering the WebContainerLazyMBeanProvider.", ex);
		}
		
	}	//destroy

//----------------------------------------------------PRIVATE METHODS-------------------------------------------------------------------

	/**
	 * Reads them from the configuration.
	 * @param resourceType
	 * @param resourceName
	 * @param currentWebModuleName
	 * @param currentApplicationName
	 * @param query	the query to use for filtering the result
	 * @param result
	 * @param showErrors	whether to dump the exception in the log by default
	 */
	private void getResourceData(int resourceType, String resourceName, String currentWebModuleName,
			String currentApplicationName, QueryExp query, Set<ObjectName> result, boolean showErrors) {

		//begin read from config
		ServiceContext serviceContext = ServiceContext.getServiceContext();
    DeployContext deployContext = serviceContext.getDeployContext();
    DeployCommunicator dc = deployContext.getDeployCommunicator();
    String webAliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(currentWebModuleName));

    Configuration appConfig = null;
    Configuration servlet_jspConfig = null;
    WebDeploymentDescriptor dd = null;

    try {
    	appConfig = dc.getAppConfigurationForReadAccess(currentApplicationName);
      servlet_jspConfig = appConfig.getSubConfiguration(Constants.CONTAINER_NAME);
    	dd = ActionBase.loadWebDDObjectFromDBase(servlet_jspConfig, webAliasDir);
    } catch (ConfigurationException e) {
    	if (showErrors) {
    	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000270", 
    	      "Cannot load deployment descriptor for webAliasDir [{0}] and application [{1}].", 
    	      new Object[]{webAliasDir, currentApplicationName}, e, null, null);
    	} else if (traceLocation.beDebug()) {
    		traceDebug("Cannot load deployment descriptor for webAliasDir [" + webAliasDir + "] and application [" + currentApplicationName + "].", e);
    	}
    } catch (Exception e) {
    	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000271", 
    	  "Cannot load deployment descriptor for webAliasDir [{0}] and application [{1}].", 
    	  new Object[]{webAliasDir, currentApplicationName}, e, null, null);
    } finally {
    	if (appConfig != null) {
        try {
          appConfig.close();
        } catch (ConfigurationException e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000272", 
            "Cannot close configuration (open in read-only mode) for application [{0}].", new Object[]{currentApplicationName}, e, null, null);
        }
      }
    }
    //read from config done

		List resourceData = null;
		WebDeploymentDescriptor globalDesc = deployContext.getGlobalDD();
		switch (resourceType) {
			case RES_TYPE_SERVLET:
				resourceData = LazyBeansUtils.loadServlets(dd, globalDesc);
				break;
			case RES_TYPE_URL:
				resourceData = LazyBeansUtils.loadURLResources(dd, globalDesc);
				break;
			case RES_TYPE_JAVAMAIL:
				resourceData = LazyBeansUtils.loadJavaMailResources(dd, globalDesc);
				break;
		}
		if (resourceData != null) {
			String currentResourceName = null;
			Iterator resourceNameIterator = null;
			if (resourceName != null) {
				if (resourceData.contains(resourceName)) {
					currentResourceName = resourceName;
				}
			} else {
				//init the iteration on all resources
				resourceNameIterator = resourceData.iterator();
				if (resourceNameIterator.hasNext()) {
					currentResourceName = (String) resourceNameIterator.next();
				}
			}
			//loop the iteration on all resources
			while (currentResourceName != null) {
				if (resourceName == null || resourceName.equals(currentResourceName)) {
					ObjectName resultName = null;
					try {
						resultName = LazyBeansUtils.constructResourceObjectName(staticObjectNameParts, resourceType,
																							currentApplicationName, currentWebModuleName, currentResourceName);
					} catch(MalformedObjectNameException monException) {
						if (traceLocation.beDebug()) {
				    	traceDebug("Failed check for object name for mbeans type [" + j2eeTypes[resourceType] +
				    			"] for application [" + currentApplicationName + "], web module [" + currentWebModuleName
				    			+ "] and resource [" + currentResourceName + "].", monException);
						}
					}
					if (resultName != null) {
						if (query != null) {
							LazyBeansUtils.addFiltered(query, resultName, result);
						} else {
							result.add(resultName);
						}
					}
				} //if resourceName == currentResourceName
				if ((resourceNameIterator != null) && resourceNameIterator.hasNext()) {
					currentResourceName = (String) resourceNameIterator.next();
				} else {
					currentResourceName = null;
				}
			}
		}
	} //getResourceData

	/**
	 * Internal method called from instantiateMBean(ObjectName name) to instantiate mbean
	 * of specific type: WebModule, Servlet, URLResource or JavaMailResource.
	 */
	private Object instantiateMBean(int resourceType, ObjectName name)
							throws MBeanRegistrationException, NotCompliantMBeanException, InstanceAlreadyExistsException {
		boolean beDebug = traceLocation.beDebug();
		// Here we apply next algorithm:
		// 1 - Retrieve the application name, web module name and
		// possibly the resource name (servlet or url resource r java mail resource)
		// depending on the passed resourceType.
		// 2 - Construct the ObjectName of the MBean
		// 3 - Create new instance of the MBean instance
		// 4 - return the MBean
		//TODO: add this if needed - Check does the names are equal (if yes -> then this is our MBean)
		Object result = null;
		//Step 1
		String applicationName = name.getKeyProperty("SAP_ITSAMJ2eeApplication.Name");
		String webModuleName = name.getKeyProperty("SAP_ITSAMJ2eeWebModule.Name");
		String resourceName = null;

		if ((applicationName == null) || (webModuleName == null)) {
			return null;
		}
		switch (resourceType) {
			case RES_TYPE_SERVLET:
				resourceName = name.getKeyProperty("SAP_ITSAMJ2eeServlet.Name");
				break;
			case RES_TYPE_URL:
				resourceName = name.getKeyProperty("SAP_ITSAMJ2eeURLResource.Name");
				break;
			case RES_TYPE_JAVAMAIL:
				resourceName = name.getKeyProperty("SAP_ITSAMJ2eeJavaMailResource.Name");
				break;
		}
		if (resourceName == null && resourceType != RES_TYPE_WEBMODULE) {
			return null;
		}
		//Checks whether the application has web modules, if not return null, because
		//the request of instantiation of the MBean cannot be fulfilled.
		//TODO: check if this is really needed.
		HashSet webModules = (HashSet) namesCache.get(applicationName);
		if (webModules == null) {
			return null;
		}
		//Step 2
		try {
			ObjectName objName = null;
			if (resourceType == RES_TYPE_WEBMODULE) {
				objName = LazyBeansUtils.constructWebModuleObjectName(staticObjectNameParts, applicationName, webModuleName);
			} else {
				objName = LazyBeansUtils.constructResourceObjectName(staticObjectNameParts, resourceType, applicationName, webModuleName, resourceName);
			}
			if (!objName.equals(name)) {
				//this could happen if the name is not full (some keys are missing, etc...)
				return null;
			}
		} catch(MalformedObjectNameException monException) {
			if (beDebug) {
	    	traceDebug("Failed check for object name for mbeans type [" + j2eeTypes[resourceType] +
	    			"] for application [" + applicationName + "].", monException);
			}
			return null;
		}
		//Step 3
		switch (resourceType) {
			case RES_TYPE_SERVLET:
				result = new SAP_ITSAMJ2eeServletWrapperAdapter(new ITSAMServlet(name, applicationName, webModuleName, resourceName));
				((SAP_ITSAMJ2eeServletWrapperAdapter) result).setObjectName(name.toString()); //otherwise it is null
				if (beDebug) {
					traceDebug("Instantiated mbean for servlet with name=[" + resourceName +
							"], web module=[" + webModuleName + "] and application=[" + applicationName + "] successfully.");
				}
				break;
			case RES_TYPE_URL:
				result = new SAP_ITSAMJ2eeURLResourceWrapperAdapter(new ITSAMURLResource(name, applicationName, webModuleName, resourceName));
				((SAP_ITSAMJ2eeURLResourceWrapperAdapter) result).setObjectName(name.toString()); //otherwise it is null
				if (beDebug) {
					traceDebug("Instantiated mbean for URL resource with name=[" + resourceName +
							"], web module=[" + webModuleName + "] and application=[" + applicationName + "] successfully.");
				}
				break;
			case RES_TYPE_JAVAMAIL:
				result = new SAP_ITSAMJ2eeJavaMailResourceWrapperAdapter(new ITSAMJavaMailResource(name, applicationName, webModuleName, resourceName));
				((SAP_ITSAMJ2eeJavaMailResourceWrapperAdapter) result).setObjectName(name.toString()); //otherwise it is null
				if (beDebug) {
					traceDebug("Instantiated mbean for JavaMail resource with name=[" + resourceName +
							"], web module=[" + webModuleName + "] and application=[" + applicationName + "] successfully.");
				}
				break;
			case RES_TYPE_WEBMODULE:
				result = new SAP_ITSAMJ2eeWebModuleWrapperAdapter(new ITSAMWebModule(name, applicationName, webModuleName, this));
				((SAP_ITSAMJ2eeWebModuleWrapperAdapter) result).setObjectName(name.toString()); //otherwise it is null
				if (beDebug) {
					traceDebug("Instantiated mbean for web module with name=[" + webModuleName +
							"] and application=[" + applicationName + "] successfully.");
				}
				break;
		}
		//Step 4
		return result;
	} //instantiateMBean


	/**
	 * This method initialize the internal structure that will contain
	 * the application names and web module names to speed up the queries, i.e.
	 * add in the cache the names and web aliases of all deployed applications.
	 *
	 * Here the structure is defined in this way: HashMap<String, HashSet<String>>,
	 * where the keys are application names and values are HashSet of web module names.
	 * namesCache is updated when the application is deployed/updated/changed via web admin
	 * or when the current provider is being initialized.
	 */
	private void initNamesCache() {
		namesCache = new HashMap<String, HashSet<String>>();

		DeployCommunicator dc = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
		if (dc == null) {
			if (traceLocation.beDebug()) {
	    	traceDebug("Failed to register names in cache because deploy communicator is null.");
			}
		} else {
			String[] appNames = dc.getMyApplications();

	    // Load application names and web module names only.
			for (int j = 0; j < appNames.length; j++) {
			  registerInNamesCache(appNames[j], dc);
			}
		}
	}//initNamesCache() method

	/**
	 * Register the applicationName and its web aliases in the
	 * internal structure, i.e. the namesCache.
	 * @param applicationName
	 */
	public void registerInNamesCache(String applicationName) {
		DeployCommunicator dc = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
		if (dc == null) {
			if (traceLocation.beDebug()) {
	    	traceDebug("Failed to register names in cache for application [" + applicationName + "] because deploy communicator is null.");
			}
		} else {
			registerInNamesCache(applicationName, dc);
		}
	}//registerInNamesCache(String)

	/**
	 * Using the passed deploy communicator retrieve the web aliases for the
	 * passed application and add them in the internal structure, i.e. the names-cache.
	 * @param applicationName
	 * @param dc
	 */
	private void registerInNamesCache(String applicationName, DeployCommunicator dc) {
		try {
			//If the application is being updated the following are all the web aliases of the new application;
			//The web aliases of the old version have been removed during invalidateLazyMBeans call.
			String[] webNames = dc.getAliases(applicationName);
			for (int i = 0; i < webNames.length; i++) {
			  String webName = webNames[i];
			  LazyBeansUtils.addNestedMaps(namesCache, applicationName, webName);
			}
		} catch (DeploymentException e) {
			if (traceLocation.beDebug()) {
	    	traceDebug("Failed to get the web aliases for application [" + applicationName + "].", e);
			}
		}
	}//registerInNamesCache(String, DeployCommunicator)

	/** Used from ITSAMWebModule to construct the object name for the Servlet, URLResource and JavaMailResource queries */
	ObjectName constructResourceObjectName(int resourceType, String application,
			String webModule, String resourceName) throws MalformedObjectNameException {
		return LazyBeansUtils.constructResourceObjectName(staticObjectNameParts, resourceType, application, webModule, resourceName);
	} //constructResourceObjectName

	/*
	private void connectoToWebContainer() {
		//May be used for optimization e.g. to open configuration
	}
	*/

	/**
	 * Invalidates all mbeans of this application including web modules, servlets, url resources and javamail resources.
	 * And updates the internal namesCache by removing the given application from it.
	 * @param applicationName
	 */
	public void invalidateLazyMBeans(String applicationName) {
		try {
			//Finds the web module mbeans (and loads them?) and invalidates them
			ObjectName webModulesQuery = new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeWebModule,SAP_ITSAMJ2eeApplication.Name=" + applicationName);
			invalidate(webModulesQuery, applicationName, "web modules");

			//Finds the servlet mbeans (and loads them?) and invalidates them
			ObjectName servletsQuery = new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeServlet,SAP_ITSAMJ2eeApplication.Name=" + applicationName);
			invalidate(servletsQuery, applicationName, "servlets");

			//Finds the URLResource mbeans (and loads them?) and invalidates them
			ObjectName urlResourceQuery = new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeURLResource,SAP_ITSAMJ2eeApplication.Name=" + applicationName);
			invalidate(urlResourceQuery, applicationName, "URL resources");

			//Finds the JavaMailResource mbeans (and loads them?) and invalidates them
			ObjectName javaMailQuery = new ObjectName("*:*,cimclass=SAP_ITSAMJ2eeJavaMailResource,SAP_ITSAMJ2eeApplication.Name=" + applicationName);
			invalidate(javaMailQuery, applicationName, "JavaMail resources");

			namesCache.remove(applicationName);

		} catch (MalformedObjectNameException e) {
			if (traceLocation.beDebug()) {
	    	traceDebug("Failed to get mbeans' object names for application [" + applicationName + "].", e);
			}
		}
  } //invalidateLazyMBeans

	/**
	 * Invalidates all mbeans of this application including web modules, servlets, url resources and javamail resources.
	 * @param query  partial object name of the lazy mbean to invalidate
	 * @param applicationName  the name of the application this mbean belongs to
	 * @param mbeanType  the type of the mbean could be one of the "Web Modules", "Servlets",
	 * 		"URL resources" or "JavaMail resources" and is used for logging only.
	 */
	private void invalidate(ObjectName query, String applicationName, String mbeanType) {
		Set result = queryNamesInternal(query, null, false); //note that here the config entries can already be removed
		if (result == null || result.isEmpty()) {
			if (traceLocation.beDebug()) {
	    	traceDebug("Failed to unregister [" + mbeanType + "] mbeans for application [" + applicationName +
	    			"] because no mbeans are found for invalidation query [" + query + "].");
			}
		} else {
			try {
				ObjectName[] mbeans = (ObjectName[]) result.toArray(new ObjectName[result.size()]);
				super.context.invalidate(mbeans);
			} catch (JMException e) {
				if (traceLocation.beDebug()) {
		    	traceDebug("Failed to unregister [" + mbeanType + "] mbeans for application [" + applicationName + "].", e);
				}
			}
		}
	} //invalidate

	//getters

	public MBeanServer getMBS() {
		return jmxServer;
	}

  public static void logWarning(String msgId, String msg) {
    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, msgId, msg, null, null);
  }

  public static void logWarning(String msgId, String msg, Throwable t) {
    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, msgId, msg, t, null, null);
  }

  public static void traceDebug(String msg) {
  	traceLocation.debugT(msg);
  }

  public static void traceDebug(String msg, Throwable t) {
  	traceLocation.debugT(msg + " The exception is: " + LogContext.getExceptionStackTrace(t));
  }
  
  public void registerProvider() {
	    try {
	    	if (traceLocation.beInfo()) {
					traceLocation.infoT("Register WebContainerLazyMBeanProvider.");
				}
	      //Register Complex Provider

			//Step 0.2. Query LazyProviderInterceptor
			//Set result = jmxServer.queryNames(new ObjectName("com.sap.default:name=ProviderInterceptor,j2eeType=com.sap.pj.jmx.server.interceptor.MBeanServerInterceptor,*"), null);
			Set result = jmxServer.queryNames(new ObjectName("com.sap.default:name=ProviderInterceptor,j2eeType=com.sap.pj.jmx.server.interceptor.MBeanServerInterceptor,SAP_J2EEClusterNode=\"\",*"), null);
			if (result.isEmpty()) {
		  //TODO:Polly type:ok
				logError("ASJ.web.000239", "Unable to register the WebContainerLazyMBeanProvider: the query for LazyProviderInterceptor returned empty set.");
				return;    
			}
			if (result.size() > 1) {
				LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000240", "Unable to register the WebContainerLazyMBeanProvider: " +
						"the query for LazyProviderInterceptor returned inconsistent set with size [{0}].", new Object[]{result.size()}
						, null, null);
				return;    
			}
			providerInterceptorObjectName = (ObjectName) result.iterator().next();
	
			//TODO: This ID may be used later for unregistering on server stop or for something else like future diagnostic.
			registeredProviderID = (String) jmxServer.invoke(providerInterceptorObjectName, "registerExtendedProvider", new Object[] { this }, new String[] {"com.sap.jmx.provider.ExtendedMBeanProvider"});
			if (traceLocation.beInfo()) {
				traceLocation.infoT("Provider successfully registered; id=[" + registeredProviderID + "].");
			}
	      return;
	    } catch (OutOfMemoryError e) {
	      throw e;
	    } catch (ThreadDeath e) {
	      throw e;
	    } catch (Throwable t) {
	    	logError("ASJ.web.000241",  "Error while registering JMX web model.", t);
	      return;
	    }
  }
  
  public SAP_ITSAMJ2eeWebModuleSettings getGlobalCompositeData() {
	    return globalCompositeData;
	  }
	  
	  public void setGlobalCompositeData(SAP_ITSAMJ2eeWebModuleSettings globalCompositeData) {
	    this.globalCompositeData = globalCompositeData;
	  }
	  

  public static void logError(String msgId, String msg) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, msgId, msg, null, null);
	  }

	  public static void logError(String msgId, String msg, Throwable t) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, msgId, msg, t, null, null);
	  }


}
