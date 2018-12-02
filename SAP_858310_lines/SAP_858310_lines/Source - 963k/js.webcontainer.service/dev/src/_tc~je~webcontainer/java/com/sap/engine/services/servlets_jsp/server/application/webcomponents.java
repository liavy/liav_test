/*
 * Copyright (c) 2000-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import static com.sap.engine.services.servlets_jsp.server.deploy.WebContainer.*;

import com.sap.engine.lib.descriptors5.webservices.WebservicesType;
import com.sap.engine.lib.injection.InjectionException;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.lib.processor.impl.WebServicesProcessor5;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.WebServicesUtil;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.JspBase;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnavailableException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.engine.services.servlets_jsp.server.runtime.FilterConfigImpl;
import com.sap.engine.services.servlets_jsp.server.runtime.ServletConfigImpl;
import com.sap.engine.services.servlets_jsp.server.security.PrivilegedActionImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParser;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.webservices.servlet.SOAPServletExt;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.el.ELContextListener;

import org.xml.sax.SAXException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is storage for all web components initialized in this web module.
 *
 */
public class WebComponents {
  private static Location currentLocation = Location.getLocation(WebComponents.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();

  //context
  private String aliasName = null;
  private ApplicationContext servletContext = null;
  //components
  /**
   * Key is the servlet name.
   * could contain either String or javax.servlet.Servlet instance as values.
   */
  private HashMap servlets = new HashMap(6);
  /**
   * Key is the servlet name.
   * could contain either String or javax.servlet.Servlet instance as values.
   */
  private HashMap servletsByClass = new HashMap(0);
  private HashMap<String, HttpJspPage> jsps = new HashMap<String, HttpJspPage>(0);
  private HashMap<String, Filter> filters = new HashMap<String, Filter>(3);
  private Vector<String> jspServletsNames = null;

  private static final String WS_ANNOTATION_NAME_1 = "javax.jws.WebService";
  private static final String WS_ANNOTATION_NAME_2 = "javax.xml.ws.WebServiceProvider";

  /**
   * For values put: TagLibraryInfo or Throwable
   * ConcurrentHashMapObjectObject because used in the Parser API
   */
  private ConcurrentHashMapObjectObject tagLibDescriptors = new ConcurrentHashMapObjectObject(13);
  /**
   * ConcurrentHashMapObjectObject because used in the Parser API
   */
  private ConcurrentHashMapObjectObject tagLibraryValidators = null;

  /** All parsed and compiled tag files
   * key is the tagfile filename
   * object is TagFileInfo
   * This will be initialized only if JSP is parsed.
   * for tagfiles on the file system, the key is /WEB-INF/tags/<tagfilename>
   * for tagfiles in jars, the key is jar://WEB-INF/lib/<jarfilename>/META-INF/tags/<tagfilename>
   * ConcurrentHashMapObjectObject because used in the Parser API
   */
  private ConcurrentHashMapObjectObject tagFiles = null;

  private ServletContextListener[] servletContextListeners = null;
  private ServletContextAttributeListener[] servletContextAttributeListeners = null;
  private HttpSessionAttributeListener[] httpSessionAttributeListeners = null;
  private HttpSessionListener[] httpSessionListeners = null;
  private ConnectionEventListener[] connectionListeners = null;
  //Since servlet 2.4
  private ServletRequestListener[] servletRequestListeners = null;
  private ServletRequestAttributeListener[] servletRequestAttributeListeners = null;
  //Since JSP2.1
  private ELContextListener[] elContextListeners = null;

  private HashMap<String, String> jspMap = null;
  private HashMap<String, Hashtable<String, String>> servletArgs = null;
  private HashMap<String, String> servletClasses = null;
  private HashMap<String, String> filterClasses = new HashMap<String, String>(3);
  private HashMap<String, HashMapObjectObject> filterParams = new HashMap<String, HashMapObjectObject>(3);

  private ConcurrentHashMap<String, Long> unavailableServlets = null;
  private ConcurrentHashMap<String, Long> unavailableJSPs = null;
  private ConcurrentHashMap<String, Long> unavailableFilters = null;

  private ConcurrentHashMap<String, Throwable> unavailableServletExceptions = null;
  private ConcurrentHashMap<String, Throwable>  unavailableFilterExceptions = null;


  //other
  private ClassLoader applicationClassLoader = null;

  private static final String SOAP_SERVLET_CLASS = "com.sap.engine.services.webservices.servlet.SOAPServletExt";
  private  boolean isWsDescriptorChecked = false;  //if the webservice.xml descriptor is checked for ws end points for the current web app
  private String[] wsEndPointsInWebserviceXml =null;  //the servlet based ws end points extracted from the webservice.xml descriptor

  public WebComponents(String aliasName, ApplicationContext servletContext, ClassLoader applicationClassLoader) {
    this.aliasName = aliasName;
    this.servletContext = servletContext;
    this.applicationClassLoader = applicationClassLoader;
  }

  void init(WebApplicationConfig webApplicationConfig) {
    if ( webApplicationConfig.getServletArguments() != null  && !webApplicationConfig.getServletArguments().isEmpty()) {
      servletArgs = new HashMap<String, Hashtable<String, String>>();
      servletArgs.putAll(webApplicationConfig.getServletArguments());
    }
    if( webApplicationConfig.getServletClasses() != null && !webApplicationConfig.getServletClasses().isEmpty()) {
      servletClasses = webApplicationConfig.getServletClasses();
    } else {
      // only if there are no servlets in global-web.xml
      servletClasses = new HashMap<String, String>();
    }
  }

  //add

  /**
   * Used by WCE API to inject servlets.
   * Do not use it for anything else!
   *
   * @param servletName
   * @param className
   * @param initParameters
   *
   * @deprecated Use {@link WebComponents#addServlet(String, String, boolean, String, Vector)}
   */
  public void addServlet(String servletName, String className, Hashtable initParameters)
    throws ServletException {
    if (containsServlet(servletName)) {
      throw new IllegalArgumentException("Servlet with name [" + servletName + "] already exists in the context of the web application [" + aliasName + "].");
    } else {
      //location for tracing - because this method is used only in WCE API.
      Location trLocation = LogContext.getLocationWebContainerProvider();

      //Cache servlet declaration
      Servlet srvlt = null;
      try {
        Class servletObj = getResourceClass(className);
        if (Servlet.class.isAssignableFrom(servletObj)) {
          srvlt = (Servlet) servletObj.newInstance();
        } else {
          throw new IllegalArgumentException("Class [" + className + "] does not implement Servlet interface.");
        }
        servletClasses.put(servletName, className);
      } catch (ClassNotFoundException cnfe) {
        if (trLocation.beDebug()) {
          LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
            "Class [" + className + "] cannot be found in the classpath of the web application[" + aliasName + "].", cnfe, "");
        }
        throw new ServletException("Class [" + className + "] cannot be found in the classpath of the web application[" + aliasName + "].", cnfe);
      } catch (InstantiationException ie) {
        if (trLocation.beDebug()) {
          LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
            "Class [" + className + "] cannot be instantiated.", ie, "");
        }
        throw new ServletException("Class [" + className + "] cannot be instantiated.", ie);
      } catch (IllegalAccessException iae) {
        if (trLocation.beDebug()) {
          LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
            "Client does not have access to the definition of class [" + className + "].", iae, "");
        }
        throw new ServletException("Client does not have access to the definition of class [" + className + "].", iae);
      }

      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getSessionServletContext().getSession().getReference(),
          servletName, servletContext.getServletContext());
      sci.setInitParameters(initParameters);

      try {
        if (trLocation.beInfo()) {
          trLocation.infoT("Initializing a servlet [" + className + "] ...");
        }
        if (initParameters != null &&  !initParameters.isEmpty()) {
          servletArgs.put(servletName, initParameters);
        }
        srvlt.init(sci);
        servlets.put(servletName, srvlt);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (UnavailableException e) {
        if (trLocation.beDebug()) {
          LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
            "Servlet [" + servletName + "] is temporary unavailable.", e, "");
        }
        setServletUnavailableException(servletName, e);
        setServletUnavailable(servletName, (System.currentTimeMillis() + e.getUnavailableSeconds() * 1000));
        addServlet(servletName);
      } catch (Throwable e) {
        if (trLocation.beDebug()) {
          LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
            WebWarningException.INITIALIZATION_OF_SERVLET_FAILED_PLEASE_CHECK_INIT_METHOD_OF_SERVLET_ERROR_IS, e, "");
        }
        servletClasses.remove(servletName);
        servletArgs.remove(servletName);
        throw new WebServletException(WebWarningException.INITIALIZATION_OF_SERVLET_FAILED_PLEASE_CHECK_INIT_METHOD_OF_SERVLET_ERROR_IS, e);
      }
    }
  }

  /**
   * Checks if a servlet is a servlet based WS end point
   * This method is called when wsEndPoints are not stored to DB.
   * This indicates that during jLinEE checks the application was not checked for servlet based ws end points.
   * This applies for old applications - deployed before the changes concerning WS integration in JlinEE phase is applied.
   * Only for these application the check for ws end points should be done start time instead during the JlineEE phase.
 * @throws WebServletException
   */
    private boolean isWsEndPoint(String servletName, Class servletObj, ArrayList<String> wsEndPoints) throws WebServletException{
      String accountingTag = "isWsEndPoint(" + servletName + ")";
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure(accountingTag, WebComponents.class);

        }//ACCOUNTING.start - END

        if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())){
          	 //check annotations
        		  Annotation[] annotations = servletObj.getAnnotations();
                 	  for(Annotation annotation: annotations){
                 		Class<? extends Annotation> annotType = annotation.annotationType();
                		if (annotType!= null && ((WS_ANNOTATION_NAME_1).equals(annotType.getName()) || (WS_ANNOTATION_NAME_2).equals(annotType.getName()))){
                       	  wsEndPoints.add(servletName);
                         	  return true;
                		}
                	  }
        } else { //check descriptor
          if (!isWsDescriptorChecked){
          	wsEndPointsInWebserviceXml = checkWsDescriptor(servletName);
          }
          if (wsEndPointsInWebserviceXml != null && wsEndPointsInWebserviceXml.length >0){
          	  for (String wsName: wsEndPointsInWebserviceXml){
          		  if(servletName.equals(wsName)){
          			  wsEndPoints.add(servletName);
          			  return true;
          		  }
          	  }
          }
        }
        return false;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(accountingTag);
        }
      }//ACCOUNTING.end - END
    }

    /**
     * Extract from the webservice.xml the names of the names of the servlet based ws end points declared there.
     * @param servletName
     * @return
     * @throws WebServletException
     */
    private String[] checkWsDescriptor(String servletName) throws WebServletException{
      		  String wsDescriptorPath = servletContext.getWebApplicationRootDir() + File.separator + "WEB-INF"+File.separator+ "webservices.xml";
      		  try {
  				if (new File(wsDescriptorPath).exists()) {
  						FileInputStream fileIS = new FileInputStream(wsDescriptorPath);
  						WebServicesProcessor5 wsProc = (WebServicesProcessor5) SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEBSERVICES5);
  						wsProc.switchOffValidation();
  						WebservicesType webServices = (WebservicesType) wsProc.parse(fileIS);
  						setWsDescriptorChecked(true);
  						return WebServicesUtil.getWSServletNames(webServices);
  				}return new String[0];
       		  } catch (FileNotFoundException e) {
  				 throw new WebServletException(WebServletException.CANNOT_CHECK_CLASS_FOR_WS_END_POINT_ERROR_IS, new Object[]{servletName, e.toString()});
       		  }catch (SAXException e){
       			throw new WebServletException(WebServletException.CANNOT_CHECK_CLASS_FOR_WS_END_POINT_ERROR_IS, new Object[]{servletName, e.toString()});
       		  }catch (IOException e){
       			throw new WebServletException(WebServletException.CANNOT_CHECK_CLASS_FOR_WS_END_POINT_ERROR_IS, new Object[]{servletName, e.toString()});
       		  }
    }

    /**
     * Loads the class as servlet based ws end point and makes the proper injections if the application is Servlet 2.5 compatible
     * @param servletName
     * @param servletClass
     * @param servletObj
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InjectionException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    private Servlet addServletAsWsEndPoint(String servletName, String servletClass, Class servletObj)
      throws InstantiationException, IllegalAccessException, InjectionException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

      String accountingTag = "/addServletAsWsEndPoint(" + servletName + "," + servletClass + ")";
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure(accountingTag, WebComponents.class);

        }//ACCOUNTING.start - END

        Object endPoint = servletObj.newInstance();
        if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
            servletContext.getInjectionWrapper().inject(endPoint);
        }
        Servlet srvlt = (Servlet) applicationClassLoader.loadClass(SOAP_SERVLET_CLASS).getConstructor(new Class[]{Object.class}).newInstance(new Object[]{endPoint});
        return srvlt;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(accountingTag);
        }
      }//ACCOUNTING.end - END
    }

   public Vector addServlet(String servletName, String servletClass, boolean isJsp) {
     Vector<LocalizableTextFormatter> warnings = new Vector<LocalizableTextFormatter>();

     //Integration with Web Services
     if ("SoapServlet".equals(servletClass)) {
       servletClass = "com.sap.engine.services.webservices.servlet.SoapServlet";
     }

     servletClasses.put(servletName, servletClass);

     if (isJsp) {
       if( jspServletsNames == null ) {
         jspServletsNames = new Vector<String>();
       }
       jspServletsNames.add(servletName);
     }

     Servlet srvlt = null;
     try {

      Class servletObj = null;
      long startup = -1;
      try {//ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure("/loadClass(" + servletName + ")", servletContext.getClassLoader().getClass());
          startup = System.currentTimeMillis();        
        }//ACCOUNTING.start - END
        if (isJsp) {
          servletObj = getJSPClass(servletClass);
        } else {
          servletObj = getResourceClass(servletClass);
        }
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          startup = System.currentTimeMillis() - startup;
          loadClassTime.addAndGet(startup);
          Accounting.endMeasure("/loadClass(" + servletName + ")");
        }
      }//ACCOUNTING.end - END

      if (Servlet.class.isAssignableFrom(servletObj)) {
        srvlt = (Servlet) servletObj.newInstance();
      //if the application is not checked for ws end points yet - make the check here
      } else if (!servletContext.isCheckedForWsEndPoints()) {
    	  if (isWsEndPoint(servletName, servletObj, servletContext.getWsEndPoints())){
    		  srvlt = addServletAsWsEndPoint(servletName, servletClass, servletObj);
      	  } else {
      	    throw new WebServletException(WebServletException.CLASS_IS_NEITHER_SERVLER_NOR_WS_END_POINT, new Object[]{servletName});
      	  }
      //the check is already made during JlinEE phase
      //and the stored ws end points should contain the servlet name (if it is actually a servlet based ws end points)
      } else if (servletContext.getWsEndPoints().contains(servletName)){
    	  srvlt = addServletAsWsEndPoint(servletName, servletClass, servletObj);
      } else {
      	throw new WebServletException(WebServletException.CLASS_IS_NEITHER_SERVLER_NOR_WS_END_POINT, new Object[]{servletName});
      }
    } catch (InjectionException e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
         WebWarningException.ERROR_OCCURRED_DURING_INJECTING_NAMING_RESOURCES, new Object[]{"Servlet", servletClass, e.toString()}));
         //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
      addServlet(servletName);
      return warnings;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_LOAD_SERVLET_ERROR_IS, new Object[]{servletClass, e.toString()}));
    }

    if (srvlt == null) {
      addServlet(servletName);
      return warnings;
    }

    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure("WebComponents.injectServlet(" + servletName + ")", WebComponents.class);
        }//ACCOUNTING.start - END

        InjectionWrapper wrapper = servletContext.getInjectionWrapper();
        if (wrapper != null) {
          servletContext.getInjectionWrapper().inject(srvlt);
        }

      } catch (InjectionException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.ERROR_OCCURRED_DURING_INJECTING_NAMING_RESOURCES, new Object[]{"Servlet", servletClass, e.toString()}));

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        addServlet(servletName);
        return warnings;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure("WebComponents.injectServlet(" + servletName + ")");
        }//ACCOUNTING.end - END
      }
    }//if ("2.5")...

    Hashtable<String, String> initParameters = getServletArgs(servletName);
    //    ServletConfigImpl sci = new ServletConfigImpl(servletContext.getServletContext(), servletName);
    ServletConfigImpl sci = new ServletConfigImpl(servletContext.getSessionServletContext().getSession().getReference(),
      servletName, servletContext.getServletContext());
    sci.setInitParameters(initParameters);

    try {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Initializing a servlet [" + servletClass + "] ...");
			}
      long startupTime = -1;
      Subject subject = null;
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          startupTime = System.currentTimeMillis();
          Accounting.beginMeasure("WebComponents.getSubject(" + servletName + ")", WebComponents.class);
        }//ACCOUNTING.start - END

        subject = servletContext.getSubject(servletName);
      } finally {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure("WebComponents.getSubject(" + servletName + ")");
          startupTime = System.currentTimeMillis() - startupTime;
          getSubjectStartupTime.addAndGet(startupTime);
        }//ACCOUNTING.end - END
      }

      if (subject == null) {
        try {//Servlet.init() may throw ServletException
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            startupTime = System.currentTimeMillis();
            Accounting.beginMeasure("WebComponents.initServlet(" + servletName + ")", srvlt.getClass());
          }//ACCOUNTING.start - END

          srvlt.init(sci);
        } finally {
          if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
            Accounting.endMeasure("WebComponents.initServlet(" + servletName + ")");
            startupTime = System.currentTimeMillis() - startupTime;
            initStartupTime1.addAndGet(startupTime);
          }//ACCOUNTING.end - END
        }
      } else {
        try {
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            startupTime = System.currentTimeMillis();
            Accounting.beginMeasure("WebComponents.Subject.doAs(" + servletName + ")", srvlt.getClass());
          }//ACCOUNTING.start - END

          Subject.doAs(subject, new PrivilegedActionImpl(srvlt, sci));
        } finally {
          if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
            Accounting.endMeasure("WebComponents.Subject.doAs(" + servletName + ")");
            startupTime = System.currentTimeMillis() - startupTime;
            doAsStartupTime.addAndGet(startupTime);
          }//ACCOUNTING.end - END
        }
      }
      servlets.put(servletName, srvlt);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (UnavailableException e) {
      setServletUnavailableException(servletName, e);
      setServletUnavailable(servletName, (System.currentTimeMillis() + e.getUnavailableSeconds() * 1000));
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.INITIALIZATION_OF_SERVLET_FAILED_PLEASE_CHECK_INIT_METHOD_OF_SERVLET_ERROR_IS,
        new Object[]{servletContext.getApplicationName(), servletContext.getCsnComponent(), servletName, e.toString()}));
      addServlet(servletName);
    } catch (PrivilegedActionException ex) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.ERROR_OCCURED_WHILE_SERVLET_IS_INIT_WITH_RUN_AS_IDENTITY_ERROR_IS,
        new Object[]{servletName, ex.toString()}));
      addServlet(servletName);
    } catch (Throwable e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.INITIALIZATION_OF_SERVLET_FAILED_PLEASE_CHECK_INIT_METHOD_OF_SERVLET_ERROR_IS,
        new Object[]{servletContext.getApplicationName(), servletContext.getCsnComponent(), servletName, e.toString()}));
      addServlet(servletName);
    }
    return warnings;
  }

  public void addJsp(String name, String isJsp) {
    if( jspMap == null ) {
      jspMap = new HashMap<String, String>();
    }
    jspMap.put(name, isJsp);
  }

  public Vector addFilter(String filterName, String filterClassName, HashMapObjectObject params) {
    Class clasFilter = null;
    Filter filter = null;
    Vector<LocalizableTextFormatter> warnings = new Vector<LocalizableTextFormatter>();
    filterClasses.put(filterName, filterClassName);
    filterParams.put(filterName, params);
    try {
      clasFilter = getResourceClass(filterClassName);
      filter = (Filter) clasFilter.newInstance();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.CANNOT_LOAD_FILTER,
        new Object[]{filterName, e.toString()}));
      return warnings;
    }

    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        servletContext.getInjectionWrapper().inject(filter);
      } catch (InjectionException e) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.ERROR_OCCURRED_DURING_INJECTING_NAMING_RESOURCES, new Object[]{"Filter", filterClassName, e.toString()}));

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return warnings;
      }
    }

    try {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Initializing a filter [" + filterName + "] ...");
			}
      filter.init(new FilterConfigImpl(filterName, filter, servletContext.getServletContext(), params));
      filters.put(filterName, filter);
    } catch (UnavailableException ue) {
      setUnavalableFilterException(filterName, ue);
      if (ue.isPermanent()) {
        setFilterUnavailable(filterName, -1);
      } else {
        setFilterUnavailable(filterName, System.currentTimeMillis() + ue.getUnavailableSeconds() * 1000);
      }
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.INITIALIZATION_OF_FILTER_FAILED_PLEASE_CHECK_INIT_METHOD_OF_FILTER_ERROR_IS,
        new Object[]{servletContext.getApplicationName(), servletContext.getCsnComponent(), filterName, ue.toString()}));
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.INITIALIZATION_OF_FILTER_FAILED_PLEASE_CHECK_INIT_METHOD_OF_FILTER_ERROR_IS,
        new Object[]{servletContext.getApplicationName(), servletContext.getCsnComponent(), filterName, e.toString()}));
    }
    return warnings;
  }

  public Vector addListener(Object obj) {
    Vector<LocalizableTextFormatter> warnings = new Vector<LocalizableTextFormatter>();
    boolean listenerClassFound = false;
    if (obj instanceof ServletContextListener) {
      listenerClassFound = true;
      addListener((ServletContextListener) obj);
    }
    if (obj instanceof ServletContextAttributeListener) {
      listenerClassFound = true;
      addListener((ServletContextAttributeListener) obj);
    }
    if (obj instanceof HttpSessionAttributeListener) {
      listenerClassFound = true;
      addListener((HttpSessionAttributeListener) obj);
    }
    if (obj instanceof HttpSessionListener) {
      listenerClassFound = true;
      addListener((HttpSessionListener) obj);
    }
    //Servlet Request Events since Servlet 2.4:
    if (obj instanceof ServletRequestListener) { //Lifecycle
      listenerClassFound = true;
      addListener((ServletRequestListener) obj);
    }
    if (obj instanceof ServletRequestAttributeListener) { //Changes to attributes
      listenerClassFound = true;
      addListener((ServletRequestAttributeListener) obj);
    }
    if (obj instanceof EventListener) {
      Method[] methods = obj.getClass().getMethods();
      for (int i = 0; i < methods.length; i++) {
        Method method = methods[i];
        if (method.getName().equals(ConnectionEventListener.METHOD_CONNECTION_CLOSED)) {
          listenerClassFound = true;
          addListener((EventListener) obj, method);
          break;
        }
      }
    }
    if (obj instanceof ELContextListener) {
      listenerClassFound = true;
      addListener((ELContextListener) obj);
    }
    if (!listenerClassFound) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        WebWarningException.UNSUPPORTED_LISTENER_CLASS,
        new Object[]{servletContext.getApplicationName(), servletContext.getCsnComponent(), obj.getClass()}));
    }
    return warnings;
  }

  /**
   * Sets TagLibraryInfo for specified URI.
   *
   * @param uri URI of TagLibrary
   * @param tli TagLibraryInfo object
   */
  public void addTagLibraryInfo(String uri, TagLibraryInfo tli) {
    tagLibDescriptors.put(uri, tli);
  }


  /**
   * Add exception for this uri for later usage.
   *
   * @param uri URI of TagLibrary
   * @param t   Throwable object
   */
  public void addTagLibraryException(String uri, Throwable t) {
    tagLibDescriptors.put(uri, t);
  }

  public void addTagLibraryValidator(String uri, TagLibraryValidator tagLibraryValidator) {
    if( tagLibraryValidators == null ) {
      tagLibraryValidators = new  ConcurrentHashMapObjectObject();
    }
    tagLibraryValidators.put(uri, tagLibraryValidator);
  }

  //get

  /**
   * Returns servlet from ServletContext by specified name. Used only within a container.
   *
   * @param name name of servlet
   * @return Servlet
   */
  public Servlet getServlet(String name) throws ServletException, ServletNotFoundException {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (servlets) {
//TODO check with web container developers
	  Servlet servlet = null;
      Object objectInServletTable_ = servlets.get(name);
      if (objectInServletTable_ instanceof String) {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("Servlet [" + name + "] is unavailable. Value in servlets table: [" + objectInServletTable_ + "].");
        //ok - the servlet is unavailable
				}
      } else {
        servlet = (Servlet) objectInServletTable_;
      }

      boolean jspAsServlet_ = false;
      String jspFile_ = (jspMap != null ? jspMap.get(name) : null);
      if (jspFile_ != null) {
        jspAsServlet_ = true;
      }
      if (servlet != null && !jspAsServlet_) { //should check JSP file for changes
        return servlet;
      }

    synchronized (servlets) {
    	Object objectInServletTable = servlets.get(name);
        if (objectInServletTable instanceof String) {
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Servlet [" + name + "] is unavailable. Value in servlets table: [" + objectInServletTable + "].");
          //ok - the servlet is unavailable
  				}
        } else {
          servlet = (Servlet) objectInServletTable;
        }

        boolean jspAsServlet = false;
        String jspFile = (jspMap != null ? jspMap.get(name) : null);
        if (jspFile != null) {
          jspAsServlet = true;
        }
        if (servlet != null && !jspAsServlet) { //should check JSP file for changes
          return servlet;
        }
      throwExceptionIfServletIsStillUnavailable(name);
      String className = servletClasses.get(name);
      if (className == null && !jspAsServlet) {
        throw new ServletNotFoundException(ServletNotFoundException.Requested_resource_not_found,
          new Object[]{aliasName + "/servlet/" + name});
      }

      try {
        // TODO: Try to find in some more elegant way that the class is JSP
        Class servltObj;
        if (jspAsServlet) {
          String newClassName = ensureJspClassUpToDate(className, jspFile);
          if (newClassName != null) {
            className = newClassName;
          } else if (servlet != null){ //not changed, so return the cached servlet instance
            return servlet;
          }
          servltObj = getJSPClass(className);
          servletClasses.put(name, className);
        } else {
          servltObj = getResourceClass(className);
        }
        if (Servlet.class.isAssignableFrom(servltObj)) {
          servlet = (Servlet) servltObj.newInstance();
        //Check if the servlet is servlet or web service endpoint. The servlet based WS end points of the application are already stored in wsEndPoints
        }else if (servletContext.getWsEndPoints().contains(name)){
        	//injection in web service endpoint
        	servlet = addServletAsWsEndPoint(name, className, servltObj);
        }else {
        	throw new WebServletException(WebServletException.CLASS_IS_NEITHER_SERVLER_NOR_WS_END_POINT, new Object[]{name});
        }

      }catch (InjectionException e) {
            //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
            throw new WebServletException(WebServletException.RESOURCE_CANNOT_BE_INJECTED, new Object[]{className}, e);
      }catch (ClassCastException ie) {
        throw new WebServletException(WebServletException.CLASS_CAST_EXCEPTION,
          new Object[]{className}, ie);
      } catch (InstantiationException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, name}, ie);
      } catch (IllegalAccessException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, name}, ie);
      } catch (ClassNotFoundException ie) {
        throw new ServletNotFoundException(ServletNotFoundException.Requested_resource_not_found,
          new Object[]{name}, ie);
      } catch (NoClassDefFoundError ie) {
        throw new ServletNotFoundException(ServletNotFoundException.CANNOT_LOAD_SERVLET,
          new Object[]{aliasName + "/servlet/" + name}, ie);
      } catch (NoSuchMethodException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      } catch (InvocationTargetException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      } catch (JspParseException ie) {
        //remove it, otherwise next time will return last workable JSP_as_servlet
        //servlets.remove(name);
        //servletClasses.remove(name);
        throw new WebServletException(WebServletException.CANNOT_PARCE_JSP_FOR_SERVLET,
          new Object[]{aliasName, jspFile}, ie);
      }catch (Throwable e){
          throw new WebServletException(WebServletException.CLASS_CANNOT_BE_LOADED_ERROR_IS,new Object[]{className, e.toString()});
      }

      //injection
      if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
        try {
          servletContext.getInjectionWrapper().inject(servlet);
        } catch (InjectionException e) {
          //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
          throw new WebServletException(WebServletException.RESOURCE_CANNOT_BE_INJECTED, new Object[]{className}, e);
        }
      }

      //      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getServletContext(), name);
      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getSessionServletContext().getSession().getReference(),
        name, servletContext.getServletContext());
      sci.setInitParameters(getServletArgs(name));
      try {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("Initializing a servlet [" + className + "] ...");
				}
        Subject subject = servletContext.getSubject(name);
        if (subject == null) {
          servlet.init(sci);
        } else {
          Subject.doAs(subject, new PrivilegedActionImpl(servlet, sci));
        }
      } catch (UnavailableException ue) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000359",
          "Cannot initialize servlet [{0}].", new Object[]{name}, ue, null, null);
        setServletUnavailableException(name, ue);
        if (ue.isPermanent()) {
          setServletUnavailable(name, -1);
        } else {
          setServletUnavailable(name, (System.currentTimeMillis() + ue.getUnavailableSeconds() * 1000));
        }
        throw ue;
      } catch (PrivilegedActionException ex) {
        throw new WebServletException(WebServletException.ERROR_OCCURED_WHILE_SERVLET_IS_INIT_WITH_RUN_AS_IDENTITY,
          new Object[]{name}, ex);
      }
      servlets.put(name, servlet);
      if( unavailableServlets != null ) {
        unavailableServlets.remove(name);
        if( unavailableServlets.isEmpty() ) {
          unavailableServlets = null;
        }
      }
      if( unavailableServletExceptions != null ) {
        unavailableServletExceptions.remove(name);
        if( unavailableServletExceptions.isEmpty() ) {
          unavailableServletExceptions = null;
        }
      }
      return servlet;
    }
  }

  /**
   * Returns the servlet-class from ServletContext by specified name. Used only within a container.
   *
   * @param name name of servlet
   * @return String
   */
  public String getServletClass(String name) {
    return servletClasses.get(name);
  }

  public Servlet getServletByClass(String className) throws ServletException, ServletNotFoundException {
    //synchronized - to ensure that two threads cannot load two different instance

    //Integration with Web Services
    if ("SoapServlet".equals(className)) {
      className = "com.sap.engine.services.webservices.servlet.SoapServlet";
    }
//TODO - check with web container developers

	Servlet servlet = null;
	Object objectInServletTable_ = servletsByClass.get(className);
	if (objectInServletTable_ instanceof String) {
	  if (traceLocation.beInfo()) {
	    	traceLocation.infoT("Servlet with class [" + className + "] is unavailable. Value in servlets table: [" + objectInServletTable_ + "].");
	    //ok - the servlet is unavailable
	  }
	} else {
	    servlet = (Servlet) objectInServletTable_;
	}
	if (servlet != null) {
	    return servlet;
	}
   synchronized (servletsByClass) {
	  Object objectInServletTable = servletsByClass.get(className);
      if (objectInServletTable instanceof String) {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("Servlet with class [" + className + "] is unavailable. Value in servlets table: [" + objectInServletTable + "].");
        //ok - the servlet is unavailable
				}
      } else {
        servlet = (Servlet) objectInServletTable;
      }
      if (servlet != null) {
        return servlet;
      }

      throwExceptionIfServletIsStillUnavailable(className);

      try {
        Class servltObj = getResourceClass(className);
        if (Servlet.class.isAssignableFrom(servltObj)) {
          servlet = (Servlet) servltObj.newInstance();
        }else{ //check if the class corresponds to a servlet based ws end point and if so - load it
        	if (servletClasses.values().contains(className)){
        		String servletName = "";
        		for (String key : servletClasses.keySet()){
        			if (servletClasses.get(key).equals(className)){
        				servletName = key;
        				break;
        			}
        		}
        		if (servletContext.getWsEndPoints().contains(servletName)){
        			// injection in web service endpoint
        			addServletAsWsEndPoint(servletName, className, servltObj);
        		}else{
        			throw new WebServletException(WebServletException.CLASS_IS_NEITHER_SERVLER_NOR_WS_END_POINT, new Object[]{servletName});
        		}
        	}else {
        		 LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000674",
        				 "Cannot find servlet that corresponds to the specified class name [{0}] for web application.", new Object[]{className}, null, null);
        	}


        }
      } catch (InjectionException e) {
			// If the container fails to find a resource needed for
			// injection, initialization of the class must fail, and the
			// class must not be put into service.
			throw new WebServletException(WebServletException.RESOURCE_CANNOT_BE_INJECTED, new Object[]{className}, e);
      } catch (InstantiationException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      } catch (IllegalAccessException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      } catch (ClassNotFoundException ie) {
        throw new ServletNotFoundException(ServletNotFoundException.Requested_resource_not_found,
          new Object[]{aliasName + "/servlet/" + className}, ie);
      } catch (NoClassDefFoundError ie) {
        throw new ServletNotFoundException(ServletNotFoundException.CANNOT_LOAD_SERVLET,
          new Object[]{aliasName + "/servlet/" + className}, ie);
      } catch (NoSuchMethodException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      } catch (InvocationTargetException ie) {
        throw new WebServletException(WebServletException.Cannot_find_servlet_instance_for_path,
          new Object[]{aliasName, className}, ie);
      }

      //injection
      if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
        try {
          servletContext.getInjectionWrapper().inject(servlet);
        } catch (InjectionException e) {
          //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
          throw new WebServletException(WebServletException.RESOURCE_CANNOT_BE_INJECTED, new Object[]{className}, e);
        }
      }

      //      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getServletContext(), className);
      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getSessionServletContext().getSession().getReference(), className, servletContext.getServletContext());
      try {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("Initializing a servlet [" + className + "] ...");
				}
        servlet.init(sci);
      } catch (UnavailableException ue) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000360",
          "Cannot initialize servlet [{0}].", new Object[]{className}, ue, null, null);
        setServletUnavailableException(className, ue);
        if (ue.isPermanent()) {
          setServletUnavailable(className, -1);
          throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
            new Object[]{className}, ue);
        } else {
          setServletUnavailable(className, (System.currentTimeMillis() + ue.getUnavailableSeconds() * 1000));
          throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
            ue.getUnavailableSeconds(), new Object[]{className}, ue);
        }
      }
      servletsByClass.put(className, servlet);
      if( unavailableServlets != null ) {
        unavailableServlets.remove(className);
        if( unavailableServlets.isEmpty() ) {
          unavailableServlets = null;
        }
      }
      if( unavailableServletExceptions != null ) {
        unavailableServletExceptions.remove(className);
        if( unavailableServletExceptions.isEmpty() ) {
          unavailableServletExceptions = null;
        }
      }
      return servlet;
    }
  }

  public boolean containsServletByClass(String name) {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (servletsByClass) {
//TODO check with web container developers
	  return servletsByClass.get(name) instanceof Servlet;
    //}
  }

  public Vector<String> getJspServletsNames() {
    return jspServletsNames;
  }

  public Set<String> getFiltersNames() {
    return filters.keySet();
  }

  public Enumeration getListenersNames() {
    Vector<String> allListeners = new Vector<String>();
    String name = null;
    for (int i = 0; servletContextListeners != null && i < servletContextListeners.length; i++) {
      name = servletContextListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    for (int i = 0; servletContextAttributeListeners != null && i < servletContextAttributeListeners.length; i++) {
      name = servletContextAttributeListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    for (int i = 0; httpSessionAttributeListeners != null && i < httpSessionAttributeListeners.length; i++) {
      name = httpSessionAttributeListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    for (int i = 0; httpSessionListeners != null && i < httpSessionListeners.length; i++) {
      name = httpSessionListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    for (int i = 0; connectionListeners != null && i < connectionListeners.length; i++) {
      name = connectionListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    //Since Servlet 2.4
    for (int i = 0; servletRequestListeners != null && i < servletRequestListeners.length; i++) {
      name = servletRequestListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    for (int i = 0; servletRequestAttributeListeners != null && i < servletRequestAttributeListeners.length; i++) {
      name = servletRequestAttributeListeners[i].getClass().toString();
      allListeners.add(name.substring(name.indexOf(" ") + 1));
    }
    return allListeners.elements();
  }

  /**
   * Returns jsp-page referring to loaded class name.
   *
   * @param  class name of jsp-page
   * @return HttpJspPage object
   */
  public HttpJspPage getJSP(String className, String url) {
    //synchronized - to ensure that two threads cannot load two different instance

      HttpJspPage a_jsp = jsps.get(className);
      if (a_jsp != null) {
        return a_jsp;
      }

    synchronized (jsps) {
    	HttpJspPage jsp_ = jsps.get(className);
	    if (jsp_ != null) {
	      return jsp_;
	    }

      try {
        Class jspClass = getJSPClass(className);
        jsp_ = (HttpJspPage) jspClass.newInstance();

        //check as per JSP 2.0 11.2.4 Using the extends Attribute:
        try {
          checkExtendAttribute(jspClass);
        } catch (Exception e1) {
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Failed to check " +
          			"that all of the methods in the Servlet interface are declared final in the generated class for the JSP class [" + className + "].");
					}
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable a) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000361",
          "Cannot load [{0}] JSP page in [{1}] web application. {2}", new Object[]{className, aliasName, className}, a, null, null);
        return null;
      }
      Hashtable<String, String> initParameters = getServletArgs("jsp");
      //      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getServletContext(), className);
      ServletConfigImpl sci = new ServletConfigImpl(servletContext.getSessionServletContext().getSession().getReference(),
        className, servletContext.getServletContext());
      //todo - tuk kakvo tochno trjabva da e url
      if (url != null && getServletArgs(url) != null) {
        Hashtable<String, String> ht = getServletArgs(url);
        Enumeration<String> en = initParameters.keys();
        while (en.hasMoreElements()) {
          String key =  en.nextElement();
          ht.put(key, initParameters.get(key));
        }
        sci.setInitParameters(ht);
      } else {
        sci.setInitParameters(initParameters);
      }
      try {
        if (traceLocation.beInfo()) {
        	traceLocation.infoT("Initializing a JSP servlet [" + className + "] ...");
				}
        Subject subject = servletContext.getSubject(className);
        if (subject == null) {
          jsp_.init(sci);
        } else {
          Subject.doAs(subject, new PrivilegedActionImpl(jsp_, sci));
        }
      } catch (PrivilegedActionException pe) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000363",
          "Cannot initialize JSP Page [{0}] while starting with privileged action in [{1}] web application.", new Object[]{className, aliasName}, pe, null, null);
        return null;
      } catch (ThreadDeath tde) {
        throw tde;
      } catch (OutOfMemoryError o) {
        throw o;
      } catch (Throwable a) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000364",
          "Cannot initialize JSP Page [{0}] in [{1}] web application.", new Object[]{className, aliasName}, a, null, null);
        return null;
      }
      if ( unavailableJSPs != null ) {
        unavailableJSPs.remove(jsp_);
        if( unavailableJSPs.isEmpty() ) {
          unavailableJSPs = null;
        }
      }
      jsps.put(className, jsp_);
      return jsp_;
    }
  }

  Filter getFilter(String name) throws ServletException {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (filters) {
//TODO check with web container developers
	  Filter f_ = (Filter) filters.get(name);
      if (f_ != null) {
        return f_;
      }
    synchronized (filters) {
      Filter f = (Filter) filters.get(name);
      if (f != null) {
        return f;
      }
      //If filter is unavailable return error 503, do not skip!
      throwExceptionIfFilterIsStillUnavailable(name);
      f = reinstantiateFilter(name);
      return f;
    }
  }

  /**
   * Checks if the filter with passed name is not in the {@link #unavailableFilters} HashMap.
   *
   * @param filterName The filter name as defined in web.xml.
   * @throws WebUnavailableException If the unavailability is not expired.
   */
  private void throwExceptionIfFilterIsStillUnavailable(String filterName) throws WebUnavailableException {
    if ( isUnavailableFilter(filterName)) {
      try {
        long unavailableTimeSetBeforeLong = unavailableFilters.get(filterName);
        if (unavailableTimeSetBeforeLong == -1) {
          WebUnavailableException webUnavailableException =
            new WebUnavailableException(WebUnavailableException.Filter_is_currently_unavailable,
              (int) unavailableTimeSetBeforeLong, new Object[]{filterName});
          webUnavailableException.initCause( unavailableFilterExceptions.get(filterName) );
          throw webUnavailableException;
        } else {
          long unavailableTimeDiffLong = unavailableTimeSetBeforeLong - System.currentTimeMillis();
          if (unavailableTimeDiffLong > 0) {
            throw new WebUnavailableException(WebUnavailableException.Filter_is_currently_unavailable,
              (int) (unavailableTimeDiffLong / 1000), new Object[]{filterName});
          }
        }
      } catch (NoSuchElementException e) {
        // $JL-EXC$ ok - doesn't contain such element
      }
    }
  } // throwExceptionIfFilterIsStillUnavailable()

  /**
   * Returns TagLibraryInfo with some information for used TagLibrary
   *
   * @param uri URI of TagLibrary
   * @return TagLibraryInfo object
   */
  public TagLibraryInfo getTagLibraryInfo(String uri) {
    return (TagLibraryInfo) tagLibDescriptors.get(uri);
  }

  public ConcurrentHashMapObjectObject getTagLibDescriptors() {
    //clone in order to prevent override of TLD prefix
    return (ConcurrentHashMapObjectObject)tagLibDescriptors.clone();
  }

  public ConcurrentHashMapObjectObject getTagLibraryValidators() {
    return tagLibraryValidators;
  }

  //load

  private Filter reinstantiateFilter(String filterName) throws ServletException {
    String filterClass =  filterClasses.get(filterName);
    Filter filter = null;
    try {
      Class clasFilter = getResourceClass(filterClass);
      filter = (Filter) clasFilter.newInstance();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebServletException(WebWarningException.CANNOT_LOAD_FILTER,
        new Object[]{filter, e.toString()}, e);
    }
    try {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Initializing a filter [" + filterClass + "] ...");
			}
      filter.init(new FilterConfigImpl(filterName, filter, servletContext.getServletContext(), filterParams.get(filterName)));
    } catch (UnavailableException ue) {
      setUnavalableFilterException(filterName, ue);
      long unavailableTime = -1;
      if (ue.isPermanent()) {
        setFilterUnavailable(filterName, unavailableTime);
      } else {
        unavailableTime = ue.getUnavailableSeconds();
        setFilterUnavailable(filterName, System.currentTimeMillis() + unavailableTime * 1000);
      }
      if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000459",
        		"Cannot reinitialize filter [{0}]. It is currently unavailable in [{1}] web application.",
        		new Object[]{filterName, aliasName}, ue, null, null);

    }
throw ue;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebServletException(WebWarningException.INITIALIZATION_OF_FILTER_FAILED_PLEASE_CHECK_INIT_METHOD_OF_FILTER_ERROR_IS,
        new Object[]{filter, e.toString()}, e);
    }
    filters.put(filterName, filter);
    if( unavailableFilters != null ) {
      unavailableFilters.remove(filterName);
      if( unavailableFilters.isEmpty() ) {
        unavailableFilters = null;
      }
    }
    if( unavailableFilterExceptions != null  ) {
      unavailableFilterExceptions.remove(filterName);
      if( unavailableFilterExceptions.isEmpty() ) {
        unavailableFilterExceptions = null;
      }
    }

    return filter;
  }

  //list

  public Enumeration getServletNames() {
    //synchronized - to ensure that two threads cannot load two different instance
    Vector<String> allServlets = new Vector<String>();
    synchronized (servlets) {
      Iterator<String> servletNamesIterator = servlets.keySet().iterator();
      while (servletNamesIterator.hasNext()) {
        String servletName =  servletNamesIterator.next();
        if (jspServletsNames == null || !jspServletsNames.contains(servletName)) {
          allServlets.add(servletName);
        }
      }
    }
    synchronized (servletsByClass) {
      Iterator<String> en = servletsByClass.keySet().iterator();
      while (en.hasNext()) {
        allServlets.add(en.next());
      }
    }
    return allServlets.elements();
  }

  public Set<String> getJspNames() {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (jsps) {
// TODO check with web container developers
	  return jsps.keySet();
    //}
  }

  //unavailable

  public void setServletUnavailable(String name, long time) {
    if( unavailableServlets == null ) {
      unavailableServlets = new ConcurrentHashMap<String, Long>();
    }
    unavailableServlets.put(name, time);
  }

  public void setServletUnavailableException(String name, Throwable e) {
    if( unavailableServletExceptions == null ) {
      unavailableServletExceptions = new ConcurrentHashMap<String, Throwable>();
    }
    unavailableServletExceptions.put(name, e);
  }

  public void setJspUnavailable(String name, long time) {
    if( unavailableJSPs == null ) {
      unavailableJSPs = new ConcurrentHashMap<String, Long>();
    }
    unavailableJSPs.put(name, time);
  }

  private void setFilterUnavailable(String name, long time) {
    if( unavailableFilters == null ) {
      unavailableFilters = new ConcurrentHashMap<String, Long>();
    }
    unavailableFilters.put(name, time);
  }

  private boolean isUnavailableServlet(String servletName) {
    return (unavailableServlets != null) && (unavailableServlets.containsKey(servletName));
  }

  public boolean isUnavailableJsp(String jspName) {
    return unavailableJSPs != null && unavailableJSPs.containsKey(jspName);
  }

  private boolean isUnavailableFilter(String filterName) {
    return unavailableFilters != null && unavailableFilters.containsKey(filterName);
  }

  private void setUnavalableFilterException(String name, Throwable exception) {
    if( unavailableFilterExceptions == null ) {
      unavailableFilterExceptions = new ConcurrentHashMap<String, Throwable>();
    }
    unavailableFilterExceptions.put(name, exception);
  }
  /**
   * Returns the time when will expire unavailability status of the JSP page.
   * For better performance first check isUnavailableJsp() method.
   * @param jspName
   * @return
   */
  public long getUnavailableJspSeconds(String jspName) {
    if( unavailableJSPs == null ) {
      throw new java.util.NoSuchElementException();
    }
    try {
      return unavailableJSPs.get(jspName);
    }catch (NullPointerException e) {
      throw new java.util.NoSuchElementException();
    }
  }

  // destroy

  public Object getWebServiceEndPoint(String servletName) throws ServletException{
	  if (servletName == null || servletName.equals("")) {
	        throw new IllegalArgumentException("Incorrect value for the servlet name - null or empty String.");
	  }
	  SOAPServletExt wsServlet = null;
	try {
		wsServlet = (SOAPServletExt) getServlet(servletName);
	}catch (ClassCastException cce){
		throw new ServletException("The servlet with servlet name [" +servletName +"] cannot be casted to SOAPServletExt type.");
	}catch (ServletNotFoundException snfe) {
		throw new ServletException("The given servlet name ["+servletName+"] does not match to any of the servlet declarations in the web deployment descriptor of the web application [" +aliasName+"].");
	}
	 return wsServlet.getServiceEndpointInstance();
  }

  public void destroyServlets() {
    try {
      if (Accounting.isEnabled()) {
        Accounting.beginMeasure("WebComponents.destroyServlets", WebComponents.class);
      }

      //synchronized - to ensure that two threads cannot load two different instance
      synchronized (servlets) {
        Iterator<String> keys = servlets.keySet().iterator();
        while(keys.hasNext()){
          String servletName = keys.next();
          try {
            Servlet s;
            Object o = servlets.get(servletName);
            // first invoke predestroy method if the servlet is a servlet based WS end point
            if (o instanceof SOAPServletExt) {
  			    s = (SOAPServletExt) o;
  			    if (getWebServiceEndPoint(servletName)!=null){
  			      if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
  			        servletContext.getInjectionWrapper().invokePreDestroyMethod(getWebServiceEndPoint(servletName));
  			      }
  			    }
  		  }// then invoke predestroy method if the servlet is plain servlet
  		  else if (o instanceof Servlet) {
              s = (Servlet) o;

              //pre destroy
              if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
                servletContext.getInjectionWrapper().invokePreDestroyMethod(s);
              }
            } else {
              continue;
            }
            if (traceLocation.beInfo()) {
            	traceLocation.infoT("Destroying a servlet [" + servletName + "] ...");
  					}
            Subject subject = servletContext.getSubject(servletName);
            if (subject == null) {
              s.destroy();
            } else {
              Subject.doAs(subject, new PrivilegedActionImpl(s));
            }
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000365",
              "Error occurred while destroying servlet [{0}] in [{1}] web application.", new Object[]{servletName, aliasName}, e, null, null);
          }
        }
      }
      synchronized (servletsByClass) {
        Iterator<String> keys = servletsByClass.keySet().iterator();
        while(keys.hasNext()) {
          String servletName = keys.next();
          try {
            Servlet s;
            Object o = servletsByClass.get(servletName);
            if (o instanceof Servlet) {
              s = (Servlet) o;

              //pre destroy
              if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
                servletContext.getInjectionWrapper().invokePreDestroyMethod(s);
              }
            } else {
              continue;
            }
            if (traceLocation.beInfo()) {
            	traceLocation.infoT("Destroying a servlet [" + servletName + "] ...");
  					}
            Subject subject = servletContext.getSubject(servletName);
            if (subject == null) {
              s.destroy();
            } else {
              Subject.doAs(subject, new PrivilegedActionImpl(s));
            }
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000366",
              "Error occurred while destroying servlet [{0}] in [{1}] web application.", new Object[]{servletName, aliasName}, e, null, null);
          }
        }
      }
    } finally {
      if (Accounting.isEnabled()) {
        Accounting.endMeasure("WebComponents.destroyServlets");
      }
    }
  }

  public void destroyJsps() {
    try {
      if (Accounting.isEnabled()) {
        Accounting.beginMeasure("WebComponents.destroyJsps", WebComponents.class);
      }

      //synchronized - to ensure that two threads cannot load two different instance
      synchronized (jsps) {
        Iterator<String> keys = jsps.keySet().iterator();
        while(keys.hasNext()) {
          String jspName = keys.next();
          try {
            HttpJspPage s = (HttpJspPage) jsps.get(jspName);
            if (traceLocation.beInfo()) {
            	traceLocation.infoT("Destroying a JSP servlet [" + jspName + "] ...");
  					}
            Subject subject = servletContext.getSubject(jspName);
            if (subject == null) {
              s.destroy();
            } else {
              Subject.doAs(subject, new PrivilegedActionImpl(s));
            }
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000367",
              "Error occurred while destroying JSP file [{0}] in [{1}] web application.", new Object[]{jspName, aliasName}, e, null, null);
          }
        }
      }
    } finally {
      if (Accounting.isEnabled()) {
        Accounting.endMeasure("WebComponents.destroyJsps");
      }
    }
  }

  /**
   * Calls destroy() methods for all Filters.
   */
  public void destroyFilters() {
    try {
      if (Accounting.isEnabled()) {
        Accounting.beginMeasure("WebComponents.destroyFilters", WebComponents.class);
      }

      //synchronized - to ensure that two threads cannot load two different instance
      synchronized (filters) {
        Iterator<Filter> en = filters.values().iterator();
        while (en.hasNext()) {
          Filter f =  en.next();

          //pre destroy
          if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
           servletContext.getInjectionWrapper().invokePreDestroyMethod(f);
          }
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Destroying a filter [" + f.getClass().getName() + "] ...");
  				}
          f.destroy();
        }
      }
    } finally {
      if (Accounting.isEnabled()) {
        Accounting.endMeasure("WebComponents.destroyFilters");
      }
    }
  }

  public void addServlet(String name) {
    //synchronized - to ensure that two threads cannot load two different instance
    synchronized (servlets) {
      servlets.put(name, "");
    }
  }

  public void addServletByClass(String name) {
    //synchronized - to ensure that two threads cannot load two different instance
    synchronized (servletsByClass) {
      servletsByClass.put(name, "");
    }
  }

  boolean containsServlet(String name) {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (servlets) {
      return servlets.containsKey(name);
    //}
  }

  boolean containsJsp(String name) {
    //synchronized - to ensure that two threads cannot load two different instance
    //synchronized (jsps) {
//TODO - check with web container developers
      return jsps.containsKey(name);
    //}
  }

  ServletContextListener[] getServletContextListeners() {
    return servletContextListeners;
  }

  ServletContextAttributeListener[] getServletContextAttributeListeners() {
    return servletContextAttributeListeners;
  }

  HttpSessionListener[] getHttpSessionListeners() {
    return httpSessionListeners;
  }

  HttpSessionAttributeListener[] getHttpSessionAttributeListeners() {
    return httpSessionAttributeListeners;
  }

  ConnectionEventListener[] getConnectionEventListeners() {
    return connectionListeners;
  }

  //Added since Servlet 2.4:
  ServletRequestListener[] getServletRequestListeners() {
    return servletRequestListeners;
  }

  ServletRequestAttributeListener[] getServletRequestAttributeListeners() {
    return servletRequestAttributeListeners;
  }

  private void addListener(ServletContextListener servletContextListener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        InjectionWrapper wrapper =  servletContext.getInjectionWrapper();
        if (wrapper != null) {
          wrapper.inject(servletContextListener);
        }
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000405",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{servletContextListener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (servletContextListeners == null) {
      servletContextListeners = new ServletContextListener[]{servletContextListener};
    } else {
      ServletContextListener[] newListeners = new ServletContextListener[servletContextListeners.length + 1];
      System.arraycopy(servletContextListeners, 0, newListeners, 0, servletContextListeners.length);
      newListeners[servletContextListeners.length] = servletContextListener;
      servletContextListeners = newListeners;
    }
  }

  private void addListener(ServletContextAttributeListener listener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        servletContext.getInjectionWrapper().inject(listener);
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000404",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{listener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (servletContextAttributeListeners == null) {
      servletContextAttributeListeners = new ServletContextAttributeListener[]{listener};
    } else {
      ServletContextAttributeListener[] newListeners = new ServletContextAttributeListener[servletContextAttributeListeners.length + 1];
      System.arraycopy(servletContextAttributeListeners, 0, newListeners, 0, servletContextAttributeListeners.length);
      newListeners[servletContextAttributeListeners.length] = listener;
      servletContextAttributeListeners = newListeners;
    }
  }

  private void addListener(HttpSessionAttributeListener listener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        servletContext.getInjectionWrapper().inject(listener);
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000402",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{listener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (httpSessionAttributeListeners == null) {
      httpSessionAttributeListeners = new HttpSessionAttributeListener[]{listener};
    } else {
      HttpSessionAttributeListener[] newListeners = new HttpSessionAttributeListener[httpSessionAttributeListeners.length + 1];
      System.arraycopy(httpSessionAttributeListeners, 0, newListeners, 0, httpSessionAttributeListeners.length);
      newListeners[httpSessionAttributeListeners.length] = listener;
      httpSessionAttributeListeners = newListeners;
    }
  }

  private void addListener(HttpSessionListener listener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        InjectionWrapper wrapper =  servletContext.getInjectionWrapper();
        if (wrapper != null) {
          wrapper.inject(listener);
        }
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000403",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{listener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (httpSessionListeners == null) {
      httpSessionListeners = new HttpSessionListener[]{listener};
    } else {
      HttpSessionListener[] newListeners = new HttpSessionListener[httpSessionListeners.length + 1];
      System.arraycopy(httpSessionListeners, 0, newListeners, 0, httpSessionListeners.length);
      newListeners[httpSessionListeners.length] = listener;
      httpSessionListeners = newListeners;
    }
  }

  private void addListener(EventListener listener, Method method) {
    if (connectionListeners == null) {
      connectionListeners = new ConnectionEventListener[]{new ConnectionEventListener(listener, method)};
    } else {
      ConnectionEventListener[] newListeners = new ConnectionEventListener[connectionListeners.length + 1];
      System.arraycopy(connectionListeners, 0, newListeners, 0, connectionListeners.length);
      newListeners[connectionListeners.length] = new ConnectionEventListener(listener, method);
      connectionListeners = newListeners;
    }
  }

  //Added since Servlet 2.4:
  private void addListener(ServletRequestListener listener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        servletContext.getInjectionWrapper().inject(listener);
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000407",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{listener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (servletRequestListeners == null) {
      servletRequestListeners = new ServletRequestListener[]{listener};
    } else {
      ServletRequestListener[] newListeners = new ServletRequestListener[servletRequestListeners.length + 1];
      System.arraycopy(servletRequestListeners, 0, newListeners, 0, servletRequestListeners.length);
      newListeners[servletRequestListeners.length] = listener;
      servletRequestListeners = newListeners;
    }
  }

  private void addListener(ServletRequestAttributeListener listener) {
    //injection
    if ("2.5".equals(servletContext.getWebApplicationConfiguration().getWebAppVersion())) {
      try {
        servletContext.getInjectionWrapper().inject(listener);
      } catch (InjectionException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000406",
          "Cannot inject naming resources in the Listener [{0}].", new Object[]{listener.getClass().getName()}, e, null, null);

        //If the container fails to find a resource needed for injection, initialization of the class must fail, and the class must not be put into service.
        return;
      }
    }

    if (servletRequestAttributeListeners == null) {
      servletRequestAttributeListeners = new ServletRequestAttributeListener[]{listener};
    } else {
      ServletRequestAttributeListener[] newListeners = new ServletRequestAttributeListener[servletRequestAttributeListeners.length + 1];
      System.arraycopy(servletRequestAttributeListeners, 0, newListeners, 0, servletRequestAttributeListeners.length);
      newListeners[servletRequestAttributeListeners.length] = listener;
      servletRequestAttributeListeners = newListeners;
    }
  }

  //since JSP2.1
  private void addListener(ELContextListener listener) {
    if (elContextListeners == null) {
      elContextListeners = new ELContextListener[]{listener};
    } else {
      ELContextListener[] newListeners = new ELContextListener[elContextListeners.length + 1];
      System.arraycopy(elContextListeners, 0, newListeners, 0, elContextListeners.length);
      newListeners[elContextListeners.length] = listener;
      elContextListeners = newListeners;
    }
  }

  public  ELContextListener[] getELContextListeners() {
    return elContextListeners;
  }

  /**
   * Checks if the servlet with passed name is not in the {@link #unavailableServlets} HashMap.
   *
   * @param servletName The servlet name as defined in web.xml or by class name.
   * @throws WebUnavailableException If the unavailability is not expired.
   */
  private void throwExceptionIfServletIsStillUnavailable(String servletName) throws WebUnavailableException {
    if (isUnavailableServlet(servletName)) {
      long unavailableTimeSetBeforeLong = unavailableServlets.get(servletName);
      if (unavailableTimeSetBeforeLong == -1) {
        WebUnavailableException webUnavailableException =
          new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
            (int) (unavailableTimeSetBeforeLong / 1000), new Object[]{servletName});
        webUnavailableException.initCause( unavailableServletExceptions.get(servletName));
        throw webUnavailableException;
      } else {
        long unavailableTimeDiffLong = unavailableTimeSetBeforeLong - System.currentTimeMillis();
        if (unavailableTimeDiffLong > 0) {
          throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
            (int) (unavailableTimeDiffLong / 1000), new Object[]{servletName});
        }
      }
    }
  } // throwExceptionIfServletIsStillUnavailable()

  public ConcurrentHashMapObjectObject getTagFiles() {
    if( tagFiles == null ) {
      tagFiles = new ConcurrentHashMapObjectObject();
    }
    return tagFiles;
  }

  /**
   * takes the resource from the private classloader if available. If no private CL is available, then the resource is taken from the public one.
   *
   * @param servletClass
   * @return
   * @throws ClassNotFoundException
   */
  private Class getJSPClass(String servletClass) throws ClassNotFoundException {
    Class servletObj = servletContext.getPrivateClassloader().loadClass(servletClass);
    return servletObj;
  }

  public String ensureJspClassUpToDate(String oldClassName, String jspName) throws JspParseException {
    String className = null;
    JspParser jspParser = JspParserFactory.getInstance().getParserInstance(JSPProcessor.PARSER_NAME);
    if (jspParser.getContainerParameters().isProductionMode()) {
      //production mode is set
      return null;
    }
    File jspFile = new File(servletContext.getWebApplicationRootDir()+jspName);
    if (oldClassName == null) {
      className = jspParser.generateJspClass(jspFile, null, servletContext.getAliasName(), null);
    } else if (!servletContext.getJspChecker().isJspClassUpToDate(oldClassName, jspFile.lastModified())) {
      className = jspParser.generateJspClass(jspFile, null, servletContext.getAliasName(), null);
    }
    return className;
  }

  /**
   * takes the resource from the private classloader if available. If no private CL is available, then the resource is taken from the public one.
   *
   * @param servletClass
   * @return
   * @throws ClassNotFoundException
   */
  private Class getResourceClass(String servletClass) throws ClassNotFoundException {
    Class servletObj = servletContext.getClassLoader().loadClass(servletClass);
    return servletObj;
  }

  /**
   * Checks for 'Using the extends Attribute' JSP 2.0 Spec, JSP.11.2.4 and logs WARNING if not OK.
   *
   * @param jspClass the class name of the compiled JSP
   */
  private void checkExtendAttribute(Class jspClass) {
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("Checking the declared servlet methods in Superclass of the JSP class [" + jspClass + "].");
    }
    boolean isOK = true;

    if (jspClass == null) {
      isOK = false;
    } else {
      Class jspSuperClass = jspClass.getSuperclass();

      if (jspSuperClass != null
        && !jspSuperClass.getName().equals(JspBase.class.getName())) {
        //check weather all of the methods in the Servlet interface are declared final:
        Method[] servletMethods = Servlet.class.getDeclaredMethods();
        for (int j = 0; j < servletMethods.length; j++) {
          try {
            Method current = jspSuperClass.getMethod(servletMethods[j].getName(), servletMethods[j].getParameterTypes());
            if (!Modifier.isFinal(current.getModifiers())) {
              isOK = false;
              if (traceLocation.beWarning()) {
								LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000460",
									"The superclass [{0}] of the JSP class [{1}] does not declare servlet's method [{2}] as final.",
									new Object[]{jspSuperClass.getName(), jspClass.getName(), current.getName()}, null, null);
							}
              break;
            }
          } catch (SecurityException e) {
            if (traceLocation.beWarning()) {
							LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000461",
								"Cannot determine method [{0}] of the superclass [{1}] of the JSP class [{2}].",
								new Object[]{servletMethods[j].getName(), jspSuperClass.getName(), jspClass.getName()}, e, null, null);
						}
          } catch (NoSuchMethodException e) {
            if (traceLocation.beWarning()) {
							LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000462",
								"The superclass [{0}] of the JSP class  [{1}] does not declare servlet's method [{2}].",
								new Object[]{jspSuperClass.getName(), jspClass.getName(), servletMethods[j].getName()}, e, null, null);
						}
            isOK = false;
            break;
          }
        }
      } else if (jspSuperClass == null) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000463",
							"Not found superclass of JSP class [{0}].", new Object[]{jspClass.getName()}, null, null);
				}
        isOK = false;
      }
    }
    if (traceLocation.beDebug()) {
      if (isOK) {
      	traceLocation.debugT("Check the declared servlet methods in Superclass of the JSP class [" + jspClass + "] is OK.");
      } else {
      	traceLocation.debugT("Check the declared servlet methods in Superclass of the JSP class [" + jspClass + "] failed.");
      }
    }
  } //checkExtendAttribute

  private Hashtable<String, String> getServletArgs(String url) {
    if( servletArgs != null ) {
      return servletArgs.get(url);
    } else {
      return null;
    }
  }

public boolean isWsDescriptorChecked() {
	return isWsDescriptorChecked;
}

public void setWsDescriptorChecked(boolean isWsDescriptorChecked) {
	this.isWsDescriptorChecked = isWsDescriptorChecked;
}


}
