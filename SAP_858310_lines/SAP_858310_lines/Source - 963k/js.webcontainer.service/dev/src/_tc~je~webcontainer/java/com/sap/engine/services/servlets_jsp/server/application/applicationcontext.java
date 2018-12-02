/*
 * Copyright (c) 2006-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

/*
 * @author Galin Galchev
 * @version 4.0
 */

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.frame.core.load.UnknownClassLoaderException;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContextFactory;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.protocol.Methods;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.chain.ApplicationScope;
import com.sap.engine.services.servlets_jsp.jspparser_api.JSPChecker;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.IDCounter;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.utils.NamingUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.NewApplicationSessionException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.lib.WebParseUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.FilterChainImpl;
import com.sap.engine.services.servlets_jsp.server.runtime.RequestDispatcherImpl;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ResourceReference;
import com.sap.engine.session.CreateException;
import com.sap.engine.session.DomainExistException;
import com.sap.engine.session.SessionContext;
import com.sap.engine.session.SessionDomain;
import com.sap.engine.session.SessionHolder;
import com.sap.engine.session.SessionNotFoundException;
import com.sap.engine.session.exec.ClientContextImpl;
import com.sap.jvm.Capabilities;
import com.sap.jvm.monitor.vm.VmDebug;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;

/**
 * Defines a set of methods that a servlet uses to communicate with its servlet
 * container, for example, to get the MIME type of a file, dispatch requests,
 * or write to a log file.
 */
public class ApplicationContext implements ApplicationScope {
  private static byte[] SLASH = new byte[] { '/' };
  /**
   * A collection of reserved Java words to be searched for in jsp file names.
   */
  private static final String javaKeywords[] = {
    "abstract", "boolean", "break", "byte", "case",
    "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "extends",
    "final", "finally", "float", "for", "goto",
    "if", "implements", "import", "instanceof", "int",
    "interface", "long", "native", "new", "package",
    "private", "protected", "public", "return", "short",
    "static", "strictfp", "super", "switch", "synchronized",
    "this", "throws", "transient", "try", "void",
    "volatile", "while"};
  private static Location currentLocation = Location.getLocation(ApplicationContext.class);
  private WebApplicationConfig webApplicationConfig = null;
  private WebComponents webComponents = null;
  private WebMappings webMappings = null;
  private WebEvents webEvents = null;
  private ServletContextImpl servletContext = null;
  private SessionServletContext securityServletContext = null;
  private JspConfiguration jspConfiguration = null;

  private static ResourceContextFactory resourceContextFactory = null;
  private ArrayList<String> wsEndPoints = null;
  private Boolean isCheckedForWsEndPoints = true;
  private String applicationName;
  private String alias = null;
  private String aliasDirName = null;
  private String componentName = null;
  private boolean isDefault = false;
  private ClassLoader applicationClassLoader;
  private ClassLoader privateClassLoader;
  private String[] filesForPrivateCL;
  private Vector privateResourceReferences;
  private volatile ConcurrentHashMap<String, ConcurrentHashMapObjectObject> classNames = null;
  private volatile ConcurrentHashMap<String, ConcurrentHashMapObjectObject> included = null;

  private volatile ConcurrentHashMap<String, RequestDispatcherImpl> reqDispCache = null;
  private String warName = null;

  private String webAppRootDir = null;
  private String workingDir = null;
  /**
   * A temporary storage directory for the servlet context.
   */
  private String servletContextTempDir = null;

  //jacc
  private String policyConfigID;

  /**
   * if application is stopping all current requests have to be processed
   */
  private boolean isDestroying = false;
  /**
   * Counts all request to this web application
   */
  private long currentRequests = 0;
  /**
   * object used for synchronization while counting requests
   */
  private Object requestsMonitor = new Object();
  private Object synchObject = new Object();
  private Vector warnings = new Vector();
  private boolean isStarted = false;

  private JSPChecker checker;

  private ArrayObject warJarClassPath = null;

  private InjectionWrapper injectionWrapper;

  /**
   * The java version may be set in the <java-version> element in the application-j2ee-engine.xml of an application.
   * The currently accepted values are 1.4 and 1.5.
   * If not explicitly set, the java version is set by default at the latest accepted - currently 1.5.
   * No sub versions are accepted - for example if <java-version>1.4.3</java-version> is specified,
   * it will be evaluated and a version 1.4 will be stored for the application.
   * This version must be used for compiling jsp files.
   */
  private String javaVersionForCompilation = "";
  private volatile IDCounter jspIdGenerator;
  private boolean initWebApplicationConfigDone = false;
  private boolean initWebComponentsDone = false;

  private String csnComponent = null;

  /**
   * Constructs new instance of ServletContextFacade with given parameters.
   *
   * @param applicationName name of application
   * @param alias           context alias
   * @param isDefault
   * @param aliasDirName    the directory for the alias may be different, i.e. some numeric mapping
   */
  public ApplicationContext(String applicationName, String alias, boolean isDefault, String aliasDirName) {
    this.applicationName = applicationName;
    this.alias = alias;
    this.isDefault = isDefault;
    this.aliasDirName = aliasDirName;
    this.warName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getWarName(alias, applicationName);
    //jacc
    this.policyConfigID = SecurityUtils.getPolicyConfigurationID(applicationName, alias);
    applicationClassLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(applicationName);
    //application class loader is needed during constructing WebApplicationConfig
    this.webApplicationConfig = new WebApplicationConfig(this);
    this.webMappings = new WebMappings(getClassLoader());
    String tempDirStat = null;
    try {
      tempDirStat = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyWorkDirectory(applicationName);
    } catch (IOException _) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000326",
        "Cannot get working directory for [{0}] web application.", new Object[]{alias}, _, null, null);
    }
    webAppRootDir = tempDirStat + ParseUtils.separator + aliasDirName + ParseUtils.separator + "root" + ParseUtils.separator;
    workingDir = tempDirStat + ParseUtils.separator + aliasDirName + ParseUtils.separator + "work" + ParseUtils.separator;
    servletContextTempDir = tempDirStat + ParseUtils.separator + aliasDirName + ParseUtils.separator + "tempwork" + ParseUtils.separator +
      ServiceContext.getServiceContext().getServerId() + ParseUtils.separator;
    try {
      workingDir = new File(workingDir).getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar);
      workingDir = workingDir + ParseUtils.separator;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
        if (LogContext.getLocationDeploy().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000450",
            	"Cannot get the absolute path of the working directory [{0}] for [{1}] web application.",
            	new Object[]{workingDir, alias}, e, null, null);
        }
    }
    try {
      servletContextTempDir = new File(servletContextTempDir).getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar);
      servletContextTempDir = servletContextTempDir + ParseUtils.separator;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
        if (LogContext.getLocationDeploy().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000451",
            	"Cannot get the absolute path of the temporary servlet context directory [{0}] for [{1}] web application.",
            	new Object[]{servletContextTempDir, alias}, e, null, null);
        }
    }
    componentName = NamingUtils.NAMING_CONTEXT + "/" + getApplicationName() + "/" + getAliasForDirectory();
    servletContext = new ServletContextImpl(this);
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public WebApplicationConfig getWebApplicationConfiguration() {
    return webApplicationConfig;
  }

  public WebComponents getWebComponents() {
    if (webComponents == null) {
      webComponents = new WebComponents(getAliasName(), this, applicationClassLoader);
    }
    if (initWebApplicationConfigDone && !initWebComponentsDone) {
      webComponents.init(getWebApplicationConfiguration());
      initWebComponentsDone = true;
    }
    return webComponents;
  }

  public WebMappings getWebMappings() {
    return webMappings;
  }

  public WebEvents getWebEvents() {
    if(webEvents == null) {
      webEvents = new WebEvents(getAliasName(), servletContext, this, getWebComponents());
    }
    return webEvents;
  }

  public boolean isDefault() {
    return isDefault;
  }

  /**
   * Returns private classloader with all parent loaders and references.
   * If private loader is not available, the public loader is used instead.
   *
   * @param warJarClassPath - Strings with resource names
   * @return
   */
  private ArrayObject addReferencesClassPath(ArrayObject warJarClassPath) {
    warJarClassPath.clear();
    // It is the private classloader of the application if exists.
    // If no private classloader is available, then application classloader is the public one.
    ClassLoader appClassLoader = getPrivateClassloader();

    // get the resources from the parent loader and from the referenced loaders
    String[] refs = ServiceContext.getServiceContext().getLoadContext().getTransitiveParents(appClassLoader);
    if (refs != null) {
      for (int i = 0; i < refs.length; i++) {
        ClassLoader rLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(refs[i]);
        if (rLoader != null) {
          String[] libJars = ServiceContext.getServiceContext().getLoadContext().getResourceNames(rLoader);
          if (libJars != null) {
            for (int j = 0; j < libJars.length; j++) {
              warJarClassPath.add(libJars[j]);
            }
          }
        }
      }
    }

    // The private loader according the classloading policy of the Engine is with lowest priority against parent classloaders.
    String[] libJars = ServiceContext.getServiceContext().getLoadContext().getResourceNames(appClassLoader);
    if (libJars != null) {
      for (int j = 0; j < libJars.length; j++) {
        warJarClassPath.add(libJars[j]);
      }
    }
    /*
     * This code was obsolete as it was carried over from 630 where classloader had 2 methods:
     * String [] jars = frameLoader.getJars();
     * File [] files = frameLoader.getDirs();
    ->       File next = new File(paths[i]);
    ->       if (next.isFile()) {
    ->         hashSet.add(paths[i]);
    ->       } else {
    ->         hashSet.add(next);
    ->       }
     * see CL 76434 in Perforce 3300
     * In JavaCompiler instance of and getPath was called for the File objects.
    */
    return warJarClassPath;
  }

  /**
   * Returns path to temp-directory, where is extracted the war-file.
   *
   * @return a String representing path to temp-directory
   */
  public String getWebApplicationRootDir() {
    return webAppRootDir;
  }

  /**
   * Returns path to work-directory, contained generated source-files and
   * theirs compiled classes.
   *
   * @return a String representing path to work-directory
   */
  public String getWorkingDir() {
    return workingDir;
  }

  /**
   * Returns ConcurrentHashMapObjectObject with all loaded class names for this parser.
   *
   * @param parserName - The name of the parser instance i.e. the value of JSPProcessor.PARSER_NAME
   * @return Hashtable with all loaded class names.
   */
  public ConcurrentHashMapObjectObject getClassNamesHashtable(String parserName) {
    if( classNames == null ) {
      synchronized (this) {
        if( classNames == null ) {
          classNames = new ConcurrentHashMap<String, ConcurrentHashMapObjectObject>(2);
        }
      }
    }
    ConcurrentHashMapObjectObject classNamesPerParser =  classNames.get(parserName);
    if (classNamesPerParser == null) {
      classNamesPerParser = new ConcurrentHashMapObjectObject();
      classNames.put(parserName, classNamesPerParser);
    }
    return classNamesPerParser;
  }

  /**
   * Returns Hashtable with included files (with include directive).
   *
   * @return Hashtable with included files.
   */
  public ConcurrentHashMapObjectObject getIncludedFilesHashtable(String parserName) {
    if( included == null ) {
      synchronized (this) {
        if( included == null ) {
          included = new ConcurrentHashMap<String, ConcurrentHashMapObjectObject>();
        }
      }
    }
    ConcurrentHashMapObjectObject includedPerParser = (ConcurrentHashMapObjectObject) included.get(parserName);
    if (includedPerParser == null) {
      includedPerParser = new ConcurrentHashMapObjectObject();
      included.put(parserName, includedPerParser);
    }
    return includedPerParser;
  }

  /**
   * Returns name of application
   *
   * @return name of application
   */
  public String getApplicationName() {
    return applicationName;
  }

  public SessionServletContext getSessionServletContext() {
    return securityServletContext;
  }


  /**
   * Returns context's name (not mapped directory name).
   *
   * @return name of context
   */
  public String getAliasForDirectory() {
    return (alias.equals("/") || alias.equals("\\")) ? Constants.defaultAliasDir : alias;
  }

  /**
   * Returns alias name
   *
   * @return name of context
   */
  public String getAliasName() {
    if (alias == null) {
      return "";
    } else {
      return alias;
    }
  }

  /**
   * Returns the public ClassLoader of the application.
   *
   * @return ServletClassLoader
   */
  public ClassLoader getClassLoader() {
    return applicationClassLoader;
  }

  /**
   * Checks request for corresponding mapping
   *
   * @param filePath       file system's representation of  requested url
   * @param httpParameters
   */
  public byte checkMap(MessageBytes filePath, HttpParameters httpParameters) throws IOException {
    if (httpParameters.getRequestPathMappings().getAliasName() == null) {
      return HttpHandler.NOOP;
    }

    HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(webApplicationConfig.isURLSessionTracking());
    if (ServiceContext.getServiceContext().getShutdownTime() > -1) {
      Enumeration en = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplications();
      while (en.hasMoreElements()) {
        SessionDomain sessionDomain = ((ApplicationContext) en.nextElement()).securityServletContext.getSession();
        if (sessionDomain.size() > 0) {
          if (sessionDomain.containsSession(cokkk.getValue())) {
            //            httpParameters.setApplicationSession(hash.get(cokkk.getValue()));
          }
          if (httpParameters.getApplicationSession() != null) {
            if (!((ApplicationSession) httpParameters.getApplicationSession()).isValid()) {
              //              hash.remove(cokkk.getValue());
              httpParameters.setApplicationSession(null);
            }
          }
        }
      }
    }


    //not used anymore - recommended by G.Stanev
//    else {
//      //httpParameters.getSessionRequest().setGSTactive(false);
//    }

    //remove alias name from the request URI
    MessageBytes requestURI = httpParameters.getRequest().getRequestLine().getUrlDecoded();
    String aliasUsed = httpParameters.getRequestPathMappings().getAliasName().toString();
    if (httpParameters.getRequestPathMappings().getZoneName() != null && !httpParameters.getRequestPathMappings().isZoneExactAlias()) {
      aliasUsed = aliasUsed + ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator()
        + httpParameters.getRequestPathMappings().getZoneName();
    }
    requestURI = getRequestedResource(requestURI, aliasUsed);

   if (cokkk != null) {
      if (ServiceContext.getServiceContext().getWebContainerProperties().getSessionIDChangeEnabled()
        && ClientContextImpl.getByClientId(SessionServletContext.getSessionIdFromJSession(cokkk.getValue())) == null
        && !(requestURI.endsWith("/j_security_check") || requestURI.endsWith("/sap_j_security_check")) ){
          httpParameters.getSessionRequest().endRequest(0);
          HttpCookie newCook = securityServletContext.setNewJsessionID(httpParameters);
          httpParameters.getRequest().getSessionCookie(webApplicationConfig.isURLSessionTracking()).setValue(newCook.getValue());
          String sessId = SessionServletContext.getSessionIdFromJSession(newCook.getValue());
          httpParameters.getSessionRequest().doSessionRequest(securityServletContext.getSession(), newCook.getValue(), sessId);          
      } else {
        if (!ServiceContext.getServiceContext().getWebContainerProperties().getSessionIPProtectionEnabled() 
            || httpParameters.getSessionRequest().checkSessionID(SessionServletContext.getSessionIdFromJSession(cokkk.getValue()))) {
          String sessId = SessionServletContext.getSessionIdFromJSession(cokkk.getValue());
          httpParameters.getSessionRequest().doSessionRequest(securityServletContext.getSession(), cokkk.getValue(), sessId);
          if (!ServiceContext.getServiceContext().getWebContainerProperties().getSessionIDRegenerationEnabled()) {
            ClientContextImpl.getByClientId(sessId).setMarkId(SessionServletContext.getMarkIdFromJSession(cokkk.getValue()));
          }
        } else {
          securityServletContext.setNewJsessionID(httpParameters);
        }
      }
    }
    //Check for debug request
    if (checkDebugRequest(httpParameters, requestURI)) {
      return HttpHandler.RESPONSE_DONE;
    }

    // TODO: Try to find better place for this check
    // If request path points under the alias root returns an error
    if (ParseUtils.canonicalize(requestURI.toStringUTF8()).startsWith("/..")) {
      String method = new String(
        httpParameters.getRequest().getRequestLine().getMethod());
      // In case of PUT request returned error is 403 Forbidden
      // in all other cases 404 Not Found
      if (method.equals("PUT")) {
        httpParameters.setErrorData(new ErrorData(ResponseCodes.code_forbidden,
          Responses.mess20, Responses.mess21, false, new SupportabilityData()));//here we do not need user action
      } else {
        httpParameters.setErrorData(new ErrorData(ResponseCodes.code_not_found,
          Responses.mess5, Responses.mess9, false, new SupportabilityData(true, "", "", "", "", "")));
      }
      return HttpHandler.ERROR;
    }

    webMappings.doMapCheck(requestURI, httpParameters.getRequestPathMappings(), false);

    //Check welcome files
    boolean welcomeFileFound = false;
    if (httpParameters.getRequestPathMappings().getServletName() == null) {
      welcomeFileFound = checkWelcomeFiles(httpParameters, filePath, requestURI, aliasUsed);
      if (welcomeFileFound) {
        return HttpHandler.RESPONSE_DONE;
      }
    }
    Thread currentThread = Thread.currentThread();
    ClassLoader threadLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(getClassLoader());
    byte response = -1;
    try {
      createChain(requestURI.toString(), httpParameters);
      response = securityServletContext.checkUser(httpParameters, requestURI, filePath);
    } catch (NewApplicationSessionException e) {
      String logId = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000347",
        "Error occurred while checking the request for mappings.", e, null, null);
      SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(e), logId);
      if (supportabilityData.getMessageId().equals("")) {
        supportabilityData.setMessageId("com.sap.ASJ.web.000347");
      }
      //TODO : Vily G : if there is no DC and CSN in the supportability data
      //and we are responsible for the problem then set our DC and CSN
      //otherwise leave them empty
      httpParameters.setErrorData(new ErrorData(e, false, supportabilityData));
      return HttpHandler.ERROR;
    } finally {
      currentThread.setContextClassLoader(threadLoader);
    }
    if (response == HttpHandler.RESPONSE_DONE) {
      return HttpHandler.RESPONSE_DONE;
    }
    if (response == HttpHandler.START_SERVLET) {
      return HttpHandler.START_SERVLET;
    }
    if (welcomeFileFound) {
      return HttpHandler.RESPONSE_DONE;
    }
    if (httpParameters.getRequestPathMappings().getServletName() != null) {
      return HttpHandler.START_SERVLET;
    }
    return HttpHandler.NOOP;
  }

  /**
   * Returns redirect location if redirect is needed. A request URI of /catalog/products
   * needs to be redirected to a URI of /catalog/products/ if such directory exists.
   * @param httpParameters request's http parameters
   * @param requestURI path relative to the context root without query string and path parameters
   * @param filePath file system's representation of  requested url
   * @return Returns redirect location if redirect is needed. Otherwise returns null.
   */
  protected byte[] checkRedirectNeeded(HttpParameters httpParameters, MessageBytes requestURI, MessageBytes filePath) {
    byte [] location = null;
    File fp = new File(filePath.toString());
    if (fp.isDirectory()) {
      MessageBytes fullUrl = httpParameters.getRequest().getRequestLine().getFullUrl();
      byte[] params = null;
      int ind = fullUrl.indexOf(";");
      int ind_question = fullUrl.indexOf("?");
      if (ind_question > -1) {
        ind = (ind > -1) ? ind : ind_question;
      }

      if (ind > -1) {
        params = new byte[fullUrl.length() - ind];
        System.arraycopy(fullUrl.getBytes(), ind, params, 0, fullUrl.length()
            - ind);
        fullUrl = new MessageBytes(fullUrl.getBytes(), 0, ind);
      }

      //URI=/a; A == alias; Redirect "/a/" OR URI=/a/dir and no url-map=/dir to servlet; Redirect "/a/dir/"
      if ((fullUrl.toString().substring(1)).equals(getAliasName()) ||
          (!fullUrl.endsWith("/") && httpParameters.getRequestPathMappings().getServletName() == null)) {
        location = constructRedirectLocation(requestURI, fullUrl, params,
            SLASH, isDefault());
      }
    }
    return location;
  }

   /**
	 * 1. When a debug request arrives and a new web session should be created
	 * then Web Container shall use Session Management API to mark the session
	 * as debug one.
	 * 2. When a debug request arrives and web session is already
	 * created then Web Container could use Session Management API to mark the
	 * session as debug one.
	 * 3. When a debug request arrives Web Container shall
	 * use Thread Management API to mark the processing thread of a debug
	 * request as debug thread.
	 * The debug request syntax is:
	 * /@@@DEBUG@@@CCCC-XXXXXXX/
	 * where CCCC is the "tag" value and XXXXXXX is the "key" value
	 * CCCC or XXXXXXX can be empty (but not both!)
     * The datatypes are:
	 * CCCC 4 ASCII A7 characters including spaces -> leading/trailing spaces
	 * must be cut off, only first 4 characters are relevant
	 * XXXXXXXX	hexadecimal string representation of an integer (e.g. 1FFA, 0FF00)
	 *
	 * @param httpParameters
	 * @param requestURI
	 *            request URI without alias name (context root)
	 * @return true if the request is debug request, otherwise - false
	 */
  private boolean checkDebugRequest(HttpParameters httpParameters,
      MessageBytes requestURI) {
    // check for debug request
    if (requestURI.startsWith("/@@@DEBUG@@@")) {
      StringBuilder requestURISB = new StringBuilder(requestURI.toString());
      int ind = requestURISB.delete(0, 1).indexOf("/");
      if (ind != -1) {
        requestURISB.delete(ind, requestURISB.length());
      }
      // check for "tag" value
      ind = requestURISB.indexOf("-");
      String tag = null;
      if (ind != -1) {
        // case @@@DEBUG@@@CCCC-XXXXXXXX or @@@DEBUG@@@-XXXXXXXX
        tag = requestURISB.substring("@@@DEBUG@@@".length(), ind).trim();
      } else {
        // case @@@DEBUG@@@CCCC
        tag = requestURISB.substring("@@@DEBUG@@@".length()).trim();
      }
      //if (tag.length() > 4) {
        // TODO: what to do in this case
      //}

      // TODO: if there is key in debug marker
      // shall we validate key?
      if (ind != -1) {
        String key = requestURISB.substring(ind + 1);
        if (key != null && key.length() > 0) {
          try {
            Integer.parseInt(key, 16);
          } catch (NumberFormatException nfe) {
            if (LogContext.getLocationRequestInfoClient().beDebug()) {
							LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceDebug(
                "Incorrect key [" + key + "] found in debug marker.", nfe, alias);
						}
            return false;
          }
        } else {
          if (LogContext.getLocationRequestInfoClient().beWarning()) {
						LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000452",
              "No key found after \"-\" in debug marker.", null, null);
        }
      }
      }

      // check for session
      HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(webApplicationConfig.isURLSessionTracking());
      if (cokkk == null) {
        // there is no web session, a new one will be created
        securityServletContext.createSession(httpParameters);
      }

      // mark session for debugging
      // TODO: up to 4 characters
      httpParameters.getSessionRequest().setDebugTag(tag);

      // generate a 302 Redirect response to the remaining URL
      MessageBytes fullUrl = httpParameters.getRequest().getRequestLine().getFullUrl();
      int startIndex = fullUrl.indexOf("/@@@DEBUG@@@");
      int endIndex = fullUrl.indexOf("/", startIndex + 1);
      if (endIndex == -1) {
        endIndex = fullUrl.length();
      }
      byte[] url = fullUrl.getBytes();
      byte[] realLocation = new byte[url.length - (endIndex - startIndex)];
      System.arraycopy(url, 0, realLocation, 0, startIndex);
      System.arraycopy(url, endIndex, realLocation, startIndex, url.length - endIndex);
      httpParameters.redirect(realLocation);
      return true;
    } else {
      // check whether session is marked for debugging
      HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(webApplicationConfig.isURLSessionTracking());
      // a null value for debug tag means session is not marked
      if (cokkk != null && httpParameters.getSessionRequest().isDebug()) {
        if (Capabilities.hasRestrictedDebugging()) {
          // mark thread for debugging
          // Adds the current thread to the scope of the already running
          // debugger in restricted mode.
          // That means after successfully calling this method, the
          // current thread is visible to the debugger front end.
          // method returns true, if the debugger is running; otherwise - false
          boolean result = VmDebug.addRestrictedDebugThread();
          if (!result) {
             LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000334",
                "Request handling thread cannot be marked as debugging thread.",
                null, null);
          }
        } else {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000335",
              "Request handling thread cannot be marked as debugging thread." +
              "The restricted debugging feature is not available.",
              null, null);
        }
      }
    }

    return false;
  }// end of checkDebugRequest()

  private boolean checkWelcomeFiles(HttpParameters httpParameters,
  																	MessageBytes filePath, MessageBytes requestURI, String aliasUsed) {
    MessageBytes fullUrl = httpParameters.getRequest().getRequestLine().getFullUrl();
    byte[] params = null;
    int ind = fullUrl.indexOf(";");
    int ind_question = fullUrl.indexOf("?");
    if (ind_question > -1) {
      ind = (ind > -1) ? ind : ind_question;
    }

    if (ind > -1) {
      params = new byte[fullUrl.length() - ind];
      System.arraycopy(fullUrl.getBytes(), ind, params, 0, fullUrl.length() - ind);
      fullUrl = new MessageBytes(fullUrl.getBytes(), 0, ind);
    }
    // SRV.9.10: Welcome files are an ordered list of partial URIs for the
    // container to use for appending to URIs when there is a request for a
    // URI that corresponds to a directory entry in the WAR ...
    File fp = new File(filePath.toString());
    if (fp.isDirectory()) {
      ArrayObject welcomeFilesArr = webApplicationConfig.getWelcomeFiles();

			for (int i = 0; i < welcomeFilesArr.size(); i++) {
				byte[] welcomeFile = (byte[]) welcomeFilesArr.elementAt(i);
        String fileName = new String(welcomeFile);
        boolean isDefaultApplication = isDefault(); //whether it is the default app, i.e. with alias "/"

        if (!isDefaultApplication && fileName.startsWith("/")) {
        	//According to Servlet 2.5 it must not start with "/" but we skip it
          fileName = fileName.substring(1, fileName.length());
        }
        File wFile = new File(fp, fileName);
        if (wFile.exists()) {
          // Redirect to the welcome file when request URL hasn't trailing
          // slash have to be done, for backward compatibility, first to the
          // original URL with trailing slash and then to the welcome file
          if (fullUrl.endsWith("/")) {
            byte[] location = constructRedirectLocation(requestURI, fullUrl,
              params, welcomeFile, isDefaultApplication);
            httpParameters.redirect(location);
          } else {
            byte[] location = constructRedirectLocation(requestURI, fullUrl,
              params, SLASH, isDefaultApplication);
            httpParameters.redirect(location);
          }
          return true;
				} else {
          // if (wFile.exists()) - is not a file or directory, then should
          // handle the servlet case
          if (welcomeFile[welcomeFile.length - 1] == ParseUtils.separatorByte) {
            // According to Servlet 2.5 The welcome file list is an ordered list
            // of partial URLs with no trailing or leading /
            byte[] tmp = new byte[welcomeFile.length - 1];
            System.arraycopy(welcomeFile, 0, tmp, 0, tmp.length);
            welcomeFile = tmp;
          }

					// check whether URI + <fileName> is a servlet mapping
          byte[] location = constructRedirectLocation(requestURI, fullUrl,
            params, welcomeFile, isDefaultApplication);
          MessageBytes checkMB = getRequestedResource(
            new MessageBytes(location), aliasUsed);

					if (this.getWebMappings().doCheckWelcomeServlets(checkMB)) {
            // Redirect to the welcome file when request URL hasn't trailing
            // slash have to be done, for backward compatibility, first to the
            // original URL with trailing slash and then to the welcome file
            if (fullUrl.endsWith("/")) {
              httpParameters.redirect(location);
            } else {
              location = constructRedirectLocation(requestURI, fullUrl, params,
                SLASH, isDefaultApplication);
              httpParameters.redirect(location);
            }
            return true;
					} else {
						if (LogContext.getLocationRequestInfoClient().beDebug()) {
							LogContext.getLocationRequestInfoClient().debugT(
			          "Will not redirect to welcome servlet [" + fileName + "] because the partial URL cannot " +
									"be mapped to any servlet.");
						}
					}

				}
      } //for
    } //else if (fullUrl.endsWith("/")) - the case when not ending with "/" is already handled by the StaticDataProcessor.findRequestedFile()

    // Will check for PUT request... if the welcomeFiles list is empty
    MessageBytes commandType = new MessageBytes(httpParameters.getRequest().getRequestLine().getMethod());
    if (commandType.equals(Methods._PUT)) {
      httpParameters.getRequestPathMappings().setServletName(new MessageBytes("PutServlet".getBytes()));
      httpParameters.getRequestPathMappings().setServletPath(httpParameters.getRequest().getRequestLine().getFullUrl().toString());
    }
    return false;
  }

	protected byte[] constructRedirectLocation(MessageBytes requestURI, MessageBytes fullUrl, byte[] params, byte[] welcomeFile, boolean isDefaultApplication) {
		byte[] location;
		if (!isDefaultApplication || !requestURI.equals("/")) {

			byte[] url = fullUrl.getBytes();
			if (params == null || params.length == 0) {
			  location = new byte[url.length + welcomeFile.length];
			  System.arraycopy(url, 0, location, 0, url.length);
			  System.arraycopy(welcomeFile, 0, location, url.length, welcomeFile.length);
			} else {
			  location = new byte[url.length + welcomeFile.length + params.length];
			  System.arraycopy(url, 0, location, 0, url.length);
			  System.arraycopy(welcomeFile, 0, location, url.length, welcomeFile.length);
			  System.arraycopy(params, 0, location, url.length + welcomeFile.length, params.length);
			}

		} else { //is the default app and requestURI is "/"

			location = new byte[1 + welcomeFile.length];
		  location[0] = ParseUtils.separatorByte;
		  System.arraycopy(welcomeFile, 0, location, 1, welcomeFile.length);
		}
		return location;
	}

  public String getRealPathLocal(String path, String aliasName, String aliasValue) {
    if (isDefault() && aliasValue != null) {
      int ind = path.indexOf(ParseUtils.separator + aliasName + ParseUtils.separator);
      if (ind != -1) {
        return (aliasValue + ParseUtils.separator + path.substring(ind + aliasName.length() + 2));
      } else {
        if (path.startsWith("/")) {
          return aliasValue + path;
        } else {
          return aliasValue + "/" + path;
        }
      }
    } else {
      return servletContext.getRealPath(path).replace(File.separatorChar, ParseUtils.separatorChar);
    }
  }

  void addInitParameter(String name, String value) {
    servletContext.addInitParameter(name, value);
  }

  /**
   * Initializes the servlet based ws end points with the already srored ws end points in the DB 
   * Set flag that the web application is not checked for ws end points during JlinEE checks
   * @param config
   * @throws DeploymentException
   */
  private void initWsEndPoints(Configuration config){
	    wsEndPoints = ActionBase.loadWsEndPointsFromDBase(config, aliasDirName);
	    if (wsEndPoints == null){
	    	setIsCheckedForWsEndPoints(false);
	    	setWsEndPoints(new ArrayList<String>());
	    }
  }
  
  //  --------------------------------------------------------
  /**
   * Returns vector of warnings.
   *
   * @throws DeploymentException
   */
  public synchronized Vector init(SecurityContext securityContext, SessionContext sessionContext, Configuration config) throws DeploymentException {


    if (applicationClassLoader == null) {
      throw new WebDeploymentException(WebDeploymentException.APPLICATIONCLASSLOADER_FOR_APPLICATION_IS_NULL,
        new Object[]{getAliasName(), applicationName});
    }

    try {
      //global-web.xml
      webApplicationConfig.parse(ServiceContext.getServiceContext().getDeployContext().getGlobalDD(), true);

      //local web.xml
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.beginMeasure("Init AppContext/Init WebAppConfig", WebApplicationConfig.class);
      }//ACCOUNTING.start - END
      
      webApplicationConfig.parse(ActionBase.loadWebDDObjectFromDBase(config, aliasDirName), false);
      
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable ex) {
      throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_APPLICATION,
        new Object[]{getAliasName()}, ex);
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("Init AppContext/Init WebAppConfig");        
      }//ACCOUNTING.end - END
    }
    initWsEndPoints(config);
    String aliasForSecurity = (alias.equals("/") || alias.equals("\\")) ? Constants.defaultAliasDir : alias;
    if (aliasForSecurity.startsWith("/") || aliasForSecurity.startsWith("\\")) {
      aliasForSecurity = aliasForSecurity.substring(1);
    }
    aliasForSecurity = aliasForSecurity.replace('/', '_');
    aliasForSecurity = aliasForSecurity.replace('\\', '_');
    String contextNameInternalUse = "";
    if (!isDefault()) {
      contextNameInternalUse = "/" + getAliasForDirectory();
    }

    String sessionDomainName = "/" + getAliasName();
    SessionDomain sessionDomain = null;
    try {
      sessionDomain = sessionContext.createSessionDomain(sessionDomainName);
    } catch (DomainExistException e) {
      sessionDomain = sessionContext.findSessionDomain(sessionDomainName);
      if (sessionDomain == null) {
          if (LogContext.getLocationDeploy().beError()) {
              LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000413",
							"Error in getting SessionDomain for the web application [{0}].", new Object[]{getAliasName()}, e, null, null);
          }
        throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_OF_WEB_APPLICATION, new Object[]{getAliasName()}, e);
      }
    } catch (CreateException e) {
        if (LogContext.getLocationDeploy().beError()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000414",
						"Error in creating SessionDomain for the web application [{0}].", new Object[]{getAliasName()}, e, null, null);
        }
      throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_OF_WEB_APPLICATION, new Object[]{getAliasName()}, e);
    }
    securityServletContext = new SessionServletContext(getAliasName(), contextNameInternalUse, aliasForSecurity,
      this, securityContext, webApplicationConfig, sessionDomain, getAppCookieName());

    try {
      sendURLSessionTracking(webApplicationConfig.isURLSessionTracking());
    } catch (HttpShmException ex) {
      throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_APPLICATION,
        new Object[]{getAliasName()}, ex);
    }

    webApplicationConfig.getNamingResources().bindNamingResources(warnings);
    if("2.5".equals(webApplicationConfig.getWebAppVersion())) {
      InjectionWrapper injWrapper = getInjectionWrapper();
      injWrapper.setInjectionMatrixes(webApplicationConfig.getNamingResources().getInjectionMatrixes());
      servletContext.setAttribute("com.sap.engine.services.servlet_jsp.annotations", injWrapper);
    }
    initWebApplicationConfigDone = true;
    webMappings.initErrorPages(webApplicationConfig.getErrorPages());

    // Policy configuration name and security policy domain name
    // of the application are required for the security integration
    servletContext.setAttribute("com.sap.engine.security.servlet.context.policy_configuration_name", getPolicyConfigID());
    servletContext.setAttribute("com.sap.engine.security.servlet.context.policy_domain_name", webApplicationConfig.getDomainName());

    if (!initTempWorkDir()) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_CREATE_PRIVATE_TEMPDIR, new Object[]{getAliasName()});
    }

    if (warnings.size() > 0) {
      return warnings;
    } else {
      return null;
    }
  }

  private boolean initTempWorkDir() {
    // Servlet 2.4 Specification, SRV.3.7.1
    boolean result = false;
    if (servletContextTempDir != null) {
      File tempDir = new File(servletContextTempDir);
      if (!tempDir.exists()) {
        if (LogContext.getLocationDeploy().beInfo()) {
					LogContext.getLocationDeploy().infoT("The necessary temporary work directory was not found [" + tempDir.getName() + "].");
				}
          tempDir.mkdirs();
        if (tempDir.exists()) {
          servletContext.setAttribute("javax.servlet.context.tempdir", tempDir);
          result = true;
        } else {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000321",
            "Cannot create a temporary storage directory [{0}] required for the servlet context of [{1}] web application .",
            new Object[]{tempDir.getAbsolutePath(), getAliasName()}, null, null);
        }
      } else {
        servletContext.setAttribute("javax.servlet.context.tempdir", tempDir);
        result = true;
      }
    }
    return result;
  }

  void addMimeType(String name, String value) {
    servletContext.addMimeType(name, value);
  }

  public void destroy() {
    String accountingTag = "/destroyApplicationContext(" + alias + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationContext.class);
      }//ACCOUNTING.start - END
      
      //todo Ehp2 - call new private method - this needs a new servlet.jar
      //java.lang.reflect.Method m = javax.el.BeanELResolver.class.getDeclaredMethod("purgeBeanClasses", new Class[]{ClassLoader.class});
      //m.setAccessible(true);
      //m.invoke(new javax.el.BeanELResolver(), new Object[]{applicationClassLoader});
      cleanBeanELResolverCache();
      applicationClassLoader = null;
      try {
        NamingUtils.unBind(alias, getApplicationName(), getAliasForDirectory());
      } catch (ThreadDeath e) {
        throw e;
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000348",
          "Cannot unbind application resources from naming context of [{0}] web application.", new Object[]{alias}, e, null, null);
      }
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Jira WEB-1806
   * There is a leak in javax.el.BeanELResolver , static ConcurrentHashMap properties field - it adds values to it, but never removes.
   * Because this class is from Servlet API, the only possible workaround in this moment is to clear the
   * hashmap records using reflection.
   */
  private void cleanBeanELResolverCache() {
    Field fieldlist[] = javax.el.BeanELResolver.class.getDeclaredFields();
    for (int i = 0; i < fieldlist.length; i++) {
      Field field = fieldlist[i];
      if (field.getName().equals("properties")) {
        removeFromBeanELResolverMap(field);
      }
    }
  }

  private void removeFromBeanELResolverMap(final Field field) {
    boolean oldAccesible = field.isAccessible();
    try {
      field.setAccessible(true);
      java.util.Map m = null;
      try {
        m = (Map) field.get(null);
      } catch (IllegalAccessException iae) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000598",
        "Cannot clear cache of javax.el.BeanELResolver for [{0}] web application.", new Object[] {alias}, iae, null, null);
        return;
      }
      if (m.size() == 0) {
        return;
      }
      Iterator<Class> iter = m.keySet().iterator();
      while (iter.hasNext()) {
        Class mbeanClass = iter.next();
        if (applicationClassLoader.equals(mbeanClass.getClassLoader())) {
          iter.remove();
        }
      }
    } finally {
      field.setAccessible(oldAccesible);
    }
  }

  /**
   * Returns full classpath for this application module
   *
   * @return Vector representation of classpaths
   */
  public ArrayObject getJarClassPathHashtable() {
    if (warJarClassPath == null) {
      warJarClassPath = addReferencesClassPath(new ArrayObject());
    }
    return warJarClassPath;
  }

  /**
   * Creates FilterChainImpl for given request, to set to http params.
   * Uses servlets_jsp.server.lib.Constants.FILTER_DISPATCHER_REQUEST for
   * requests that come directly from the client.
   */
  private void createChain(String requestPath, HttpParameters httpParameters) {
    String[] filterNames = webMappings.getFilters(requestPath,
      httpParameters.getRequestPathMappings().getServletName(),
      com.sap.engine.services.servlets_jsp.server.lib.Constants.FILTER_DISPATCHER_REQUEST);
    if (filterNames == null) {
      return;
    }
    httpParameters.getRequestPathMappings().setFilterChain(filterNames);
    if (httpParameters.getRequestPathMappings().getServletName() == null) {
      httpParameters.getRequestPathMappings().setServletName(new MessageBytes("default".getBytes()));
    }
  }

  public FilterChain instantiateFilterChain(String[] filterNames) throws ServletException {
    if (filterNames == null) {
      return null;
    }
    com.sap.engine.interfaces.resourcecontext.ResourceContext resourceContext = enterResourceContext();
    FilterChainImpl filterChain = new FilterChainImpl();
    for (int i = 0; i < filterNames.length; i++) {
      Filter f = getWebComponents().getFilter(filterNames[i]);
      if (f != null) {
        filterChain.addFilter(f);
      }
    }
    exitResourceContext(resourceContext);
    //test
    return filterChain;
  }

  public Subject getSubject(String resourceName) {
    String roleName = webApplicationConfig.getRunAsRoleName(resourceName);
    if (roleName != null) {
      return securityServletContext.getSubject(roleName);
    }
    return null;
  }

  public ContextObject getSecurityContext() {
    ThreadContext localTC = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    return localTC.getContextObject(localTC.getContextObjectId(SecurityContextObject.NAME));
  }

  public void setFailOver(String failover) {
    webApplicationConfig.setFailOver(failover);
  }

  public com.sap.engine.interfaces.resourcecontext.ResourceContext enterResourceContext() {
    com.sap.engine.interfaces.resourcecontext.ResourceContext resourceContext = null;
    try {
      if (resourceContextFactory == null) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000336",
          "Resources context factory is not available for [{0}] web application.", new Object[]{alias}, null, null);
      } else {
        // war name is send as additionalInfo (needed for SCA integration)
        resourceContext = resourceContextFactory.createContext(getApplicationName(), componentName, true, warName);
        resourceContext.enterMethod("web_container");
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000327",
    	  "Error occurred during entering resource context of [{0}] application. " +
    	  "Future lookups of objects from its resource context may fail.", new Object[]{applicationName}, e, null, null);
    }
    return resourceContext;
  }

  public void exitResourceContext(com.sap.engine.interfaces.resourcecontext.ResourceContext resourceContext) {
    try {
      if (resourceContext != null) {
        resourceContext.exitMethod("web_container", true);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000328",
        "Error occurred during exiting resource context of [{0}] application.", new Object[]{applicationName}, e, null, null);
    }
  }

  private void sendURLSessionTracking(boolean isURLSessionTracking) throws HttpShmException, WebDeploymentException {
    if (ServiceContext.getServiceContext().getHttpProvider() == null) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000322",
        "Cannot send URL session tracking property for web application [{0}]. " +
        "There is no HTTP service registered. Please check in Telnet whether HTTP service is started and start it manually if needed.",
        new Object[]{alias}, null, null);
      return;
    }
    boolean isSuccessfulRegistring = ServiceContext.getServiceContext().getHttpProvider().urlSessionTracking(getApplicationName(), getAliasName(), isURLSessionTracking);
    if (LogContext.getLocationDeploy().beDebug()) {
      LogContext.getLocationDeploy().debugT("UrlSessionTracking for [" + getAliasName() + "] alias in application [" + getApplicationName() + "] finished with [" + isSuccessfulRegistring + "]");
    }
    if (!isSuccessfulRegistring) {
      if (isDefault) {
        String deployedApplName = ((WebContainerProvider)ServiceContext.getServiceContext().getWebContainer()
            .getIWebContainerProvider()).getDeployedAppl(getAliasName()).getWholeApplicationName();
        if (deployedApplName.equals(applicationName)) {
          // It is start of the application (which one?!?!?!?) after server restart. It is normal and starting is allowed

          // TODO trace it
          if (LogContext.getLocationDeploy().beInfo()) {
            LogContext.getLocationDeploy().infoT("The same application [" + deployedApplName + "] is deployed.");
          }
        } else {
          // starting of the default application which is not in the first place in the vector with
          // deployed applications for that alias (see WebContainerProvider#getDeployedAppl(alias))
          // i.e. it is starting sap default application but on the system there is a customer
          // default application, which is returned by the WebContainerProvider#getDeployedAppl(alias)
          // In this case the default application should not start => throw deployment exception
          throw new WebDeploymentException(WebDeploymentException.CANNOT_START_APPLICATION_BECAUSE_THERE_IS_ANOTHER_APPL_WITH_HIGHER_PRIO,
              new Object[]{applicationName, getAliasName(), deployedApplName});
        }
      } else {
        // it is not expected DuplicatedAliasException to be thrown for applications different
        // than default. Start is rejected in this case - is this correct ?!?!?
        throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_APPLICATION, new Object[]{getAliasName()});
      }
    }
  }

  private String getAppCookieName() {
    //String appCookieName = CookieParser.app_cookie_prefix + getAliasNameForCookie();
    //appCookieName = appCookieName.replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar);
    //return appCookieName;
    return CookieParser.app_cookie_prefix + "*";
  }

  public void setDestroyingMode(boolean isDestroying) {
    this.isDestroying = isDestroying;
  }

  public boolean isDestroying() {
    return isDestroying;
  }

  public void addRequest() {
    synchronized (requestsMonitor) {
      currentRequests++;
    }
  }

  public void removeRequest() {
    synchronized (requestsMonitor) {
      currentRequests--;
    }
  }

  public long getAllCurrentRequests() {
    return currentRequests;
  }

  public Object getSynchObject() {
    return synchObject;
  }

  public Vector getWarnings() {
    return warnings;
  }

  public static void setResourceContextFactory(Object newresourceContextFactory) {
    resourceContextFactory = (ResourceContextFactory) newresourceContextFactory;
  }

  public boolean invalidateSession(String sessionId) {
    SessionHolder holder = securityServletContext.getSession().getSessionHolder(sessionId);
    ApplicationSession applicationSession = null;
    try {
      applicationSession = (ApplicationSession) holder.getSession();
    } catch (SessionNotFoundException e) {
      // $JL-EXC$ ok - not found
    }
    if (applicationSession == null) {
      holder.releaseAccess();
      return false;
    } else {
      applicationSession.passivate();
      holder.releaseAccess();
      return true;
    }
  }

  /**
   * Returns the JspConfiguration object associated with this application
   * context.
   *
   * @return the JspConfiguration object.
   */
  public JspConfiguration getJspConfiguration() {
    if (jspConfiguration == null) {
      // first search for servlet specification version in the web-j2ee-engine.xml
      String appVersion = webApplicationConfig.getSpecVersion();
      // if web-j2ee-engine.xml is not present then take the version from the web.xml
      if( appVersion == null ){
        appVersion = webApplicationConfig.getWebAppVersion();
      }
      jspConfiguration = new JspConfigurationImpl(webApplicationConfig.getJspPropertyGroup(), appVersion);
    }
    return jspConfiguration;
  }

  public boolean initializeDebugInfo(HttpServletRequestFacade request) {
    if (ServiceContext.getServiceContext().getHttpSessionDebugListener() != null
      && ServiceContext.getServiceContext().getDebugRequestParameterName() != null
      && ServiceContext.getServiceContext().getDebugRequestParameterName().length() != 0) {
      //take param from request
      String debugParamValue = request.getParameter(ServiceContext.getServiceContext().getDebugRequestParameterName());
      if (debugParamValue != null) {
        String sessionId = null;
        ApplicationSession httpSession = (ApplicationSession) request.getSession(false);
        if (httpSession != null) {
          sessionId = httpSession.getIdInternal();
          httpSession.setDebugParameterValue(debugParamValue);
        }
        if (!ServiceContext.getServiceContext().getHttpSessionDebugListener().startDebugRequest(sessionId, debugParamValue)) {
          if (httpSession != null) {
            httpSession.setDebugParameterValue(null);
          }
          return false;
        }
        return true;
      } else {
        //take param from session
        String sessionId = null;
        ApplicationSession httpSession = (ApplicationSession) request.getSession(false);
        if (httpSession != null) {
          sessionId = httpSession.getIdInternal();
          debugParamValue = httpSession.getDebugParameterValue();
          if (debugParamValue != null) {
            if (!ServiceContext.getServiceContext().getHttpSessionDebugListener().startDebugRequest(sessionId, debugParamValue)) {
              httpSession.setDebugParameterValue(null);
              return false;
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean initializeDebugInfo(HttpParameters request, ApplicationSession httpSession) {
    if (ServiceContext.getServiceContext().getHttpSessionDebugListener() != null
      && ServiceContext.getServiceContext().getDebugRequestParameterName() != null
      && ServiceContext.getServiceContext().getDebugRequestParameterName().length() != 0) {
      //take param from request
      String debugParamValue = getDebugParameterValueFromRequest(request);
      if (debugParamValue != null) {
        String sessionId = null;
        if (httpSession != null) {
          sessionId = httpSession.getIdInternal();
          httpSession.setDebugParameterValue(debugParamValue);
        }
        if (!ServiceContext.getServiceContext().getHttpSessionDebugListener().startDebugRequest(sessionId, debugParamValue)) {
          if (httpSession != null) {
            httpSession.setDebugParameterValue(null);
          }
          return false;
        }
        return true;
      } else {
        //take param from session
        String sessionId = null;
        if (httpSession != null) {
          sessionId = httpSession.getIdInternal();
          debugParamValue = httpSession.getDebugParameterValue();
          if (debugParamValue != null) {
            if (!ServiceContext.getServiceContext().getHttpSessionDebugListener().startDebugRequest(sessionId, debugParamValue)) {
              httpSession.setDebugParameterValue(null);
              return false;
            }
            return true;
          }
        }
      }
    }
    return false;
  }


  private String getDebugParameterValueFromRequest(HttpParameters request) {
    String characterEncoding = null;
    try {
      characterEncoding = WebParseUtils.parseEncoding(request);
      if (characterEncoding == null || characterEncoding.equals("")) {
        characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
      }
      // Ensure that the specified encoding is valid
      byte buffer[] = new byte[1];
      buffer[0] = (byte) 'a';
      new String(buffer, characterEncoding);
    } catch (UnsupportedEncodingException e) {
      characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
    }
    if (request.getRequest().getRequestLine().getQuery() == null) {
      return null;
    }
    HashMapObjectObject parameters = new HashMapObjectObject();
    try {
      WebParseUtils.parseQueryString(parameters,
        request.getRequest().getRequestLine().getQuery().getBytes(), characterEncoding);
    } catch (UnsupportedEncodingException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000349",
        "Cannot parse the parameters of the request. Incorrect encoding [{0}] specified in it.", new Object[]{characterEncoding}, e, null, null);
    }
    String[] value = (String[]) parameters.get(ServiceContext.getServiceContext().getDebugRequestParameterName());
    if (value != null && value.length > 0) {
      return value[0];
    } else {
      return null;
    }
  }








  /**
   * Returns the directory where the specified file must be located when it is compiled.
   *
   * @param fileName the name of the file.
   * @return the directory where the specified file must be located when it is compiled.
   */
  public String getCompiledFileDir(String fileName) {
    return workingDir + fileName.substring(0, fileName.lastIndexOf(ParseUtils.separator) + 1);
  }





  /**
   * Creates a legal Java path name to a class file. This path can then directly
   * be transformed into a valid package name.
   *
   * @param relativePath the java class file path name that contains the directories
   *                     to the java file.
   * @return a legal java path name.
   */
  public String getValidClassNamePath(String relativePath) {
    StringBuffer validClassName = new StringBuffer();
    StringTokenizer classNameComponents = new StringTokenizer(relativePath, ParseUtils.separator);
    while (classNameComponents.hasMoreElements()) {
      String nextName = (String) classNameComponents.nextElement();
      validClassName.append(getValidJavaIdentifier(nextName));
      if (classNameComponents.hasMoreElements()) {
        validClassName.append(ParseUtils.separator);
      }
    }
    //return validClassName.toString();
    //removes jsp packages
    String className = validClassName.toString();
    className = className.replace(ParseUtils.separatorChar, '_');
    return className;
  }

  /**
   * Creates a legal Java identifier from the specified identifier.
   *
   * @param identifier the identifier from which a legal one to be created.
   * @return legal Java identifier corresponding to the given identifier.
   */
  private String getValidJavaIdentifier(String identifier) {
    StringBuffer validIdentifier = new StringBuffer();
    char c = identifier.charAt(0);
    if (Character.isJavaIdentifierStart(c)) {
      validIdentifier.append(c);
    } else {
      validIdentifier.append("_" + Integer.toHexString(c) + "_");
    }
    for (int i = 1; i < identifier.length(); i++) {
      c = identifier.charAt(i);
      if (Character.isJavaIdentifierPart(c) && c != '_') {
        validIdentifier.append(c);
      } else {
        validIdentifier.append("_" + Integer.toHexString(c) + "_");
      }
    }
    if (isJavaKeyword(validIdentifier.toString())) {
      validIdentifier.append('_');
    }
    return validIdentifier.toString();
  }

  /**
   * Test whether the argument is a Java keyword.
   *
   * @param key the argument to be checked.
   * @return true if the passed argument is a Java keyword, otherwise false.
   */
  private boolean isJavaKeyword(String key) {
    int i = 0;
    int j = javaKeywords.length;
    while (i < j) {
      int k = (i + j) / 2;
      int result = javaKeywords[k].compareTo(key);
      if (result == 0) {
        return true;
      }
      if (result < 0) {
        i = k + 1;
      } else {
        j = k;
      }
    }
    return false;
  }

  //jacc
  public String getPolicyConfigID() {
    return policyConfigID;
  }

  /**
   * Sets application module's PolicyContext ID.
   *
   * @return previous PolicyContext ID. It's not expected to be something different from null because it's not accepted
   *         to be obtained reference to another ServletContext (another web module).
   */
  public String setPolicyContextID(boolean isStandard) {
    String prevContextID = null;
    try {
      prevContextID = PolicyContext.getContextID();
    } catch (SecurityException e) {
        if (LogContext.getLocationDeploy().beError()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000415",
            		"SecurityException while getting previous PolicyContextID", e, null, null);
        }
    }
    if (isStandard) {
      PolicyContext.setContextID(policyConfigID);
    } else {
      PolicyContext.setContextID(policyConfigID + "-EXTERNAL");
    }

    return prevContextID;
  } // setNewPolicyContextID()

  public void restorePrevPolicyContextID(String policyContextID) {
    try {
      PolicyContext.setContextID(policyContextID);
    } catch (SecurityException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000329",
        "Cannot set the  PolicyContext of [{0}] application  to [{1}]. Resetting to null.", new Object[]{applicationName, policyContextID}, e, null, null);
      PolicyContext.setContextID(null);
    }
  } // setPolicyContextID()

  /**
   * remove alias name from the request URI
   */
  public MessageBytes getRequestedResource(MessageBytes requestURI, String aliasUsed) {
    if (requestURI.startsWith('/' + aliasUsed + '/')) {
      requestURI = new MessageBytes(requestURI.getBytes(aliasUsed.length() + 1));
    } else if (requestURI.startsWith(aliasUsed + '/')) {
      requestURI = new MessageBytes(requestURI.getBytes(aliasUsed.length()));
    } else if (requestURI.equals('/' + aliasUsed)) {
      requestURI = new MessageBytes("".getBytes());
    } else {
      requestURI = new MessageBytes(requestURI.getBytes());
    }
    return requestURI;
  }

  /**
   * Returns true if the ApplicationContext is ready to serve requests.
   * Returns false if is not already initialized.
   *
   * @return Returns the isStarted.
   */
  public boolean isStarted() {
    return isStarted;
  }

  /**
   * Indicates that the application context is ready to serve requests.
   *
   * @param started The isStarted to set.
   */
  public void setStarted(boolean started) {
    this.isStarted = started;
  }

  /**
   * Returns the private classloader. If no private classloader is found - the public classloader is returned.
   *
   * @return
   */
  public ClassLoader getPrivateClassloader() {
    //ClassLoader appClassLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(Constants.PRIVATE_CL_NAME + getApplicationName());
    if (privateClassLoader != null) {
      return privateClassLoader;
    } else if (filesForPrivateCL != null) {
      Vector<ClassLoader> parents = new Vector<ClassLoader>();
      parents.add(getClassLoader());
      for (int i = 0; privateResourceReferences != null && i < privateResourceReferences.size(); i++) {
        ResourceReference resourceReference = (ResourceReference) privateResourceReferences.get(i);
        String appName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getApplicationProvidingResource(resourceReference.getResourceName(), resourceReference.getResourceType());
        if (appName != null) {
          ClassLoader appClassloader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(appName);
          if (!parents.contains(appClassloader)) {
            parents.add(appClassloader);
          }
        }
      }
      try {
        privateClassLoader = ServiceContext.getServiceContext().getLoadContext().createClassLoader((ClassLoader[]) parents.toArray(new ClassLoader[parents.size()]),
          filesForPrivateCL, Constants.PRIVATE_CL_NAME + applicationName, applicationName, LoadContext.COMP_TYPE_APPLICATION);
      } catch (UnknownClassLoaderException e) {
        if (LogContext.getLocationDeploy().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000573",
            	"Cannot create classloader [{0} {1}]. The error is :",
            	new Object[]{Constants.PRIVATE_CL_NAME, applicationName}, e, null, null);
        }
         //in case of UnknownClassLoaderException: Already registered "WCE_PRIVATE_CL_sap.com/app"
         privateClassLoader = ServiceContext.getServiceContext().getLoadContext().getClassLoader(Constants.PRIVATE_CL_NAME + applicationName);
      } catch (MalformedURLException e) {
        if (LogContext.getLocationDeploy().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000574",
            	"Cannot create classloader [{0} {1}]. The error is :",
            	new Object[]{Constants.PRIVATE_CL_NAME, applicationName}, e, null, null);
        }
      }
    }

    if(privateClassLoader != null){
      return privateClassLoader;
    }
    LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000575",
         "Private classloader [{0} {1}] is null. Will use application classloader.",
         new Object[]{Constants.PRIVATE_CL_NAME, applicationName}, null, null);
    return getClassLoader();
  }


  public JSPChecker getJspChecker() {
    //should be created after work dir is defined
    if (checker == null) {
      this.checker = new JSPChecker(this);
    }
    return checker;
  }

  public InjectionWrapper getInjectionWrapper() {
    if (injectionWrapper == null) {
      injectionWrapper = new InjectionWrapper();
    }
    return injectionWrapper; 
  }//end of getInjectionWrapper()

  public String getJavaVersionForCompilation() {
    return javaVersionForCompilation;
  }//end of getJavaVersionForCompilation()

  public void setJavaVersionForCompilation(String javaVersionForCompilation) {
    this.javaVersionForCompilation = javaVersionForCompilation;
  }//end of setJavaVersionForCompilation(String javaVersionForCompilation)

  public String getErrorPage(int statusCode) {
    return webMappings.getErrorCodeErrorPage(statusCode);
  }

  public String getErrorPage(Throwable exception) {
    return webMappings.getExceptionErrorPage(exception);
  }

  /**
   * The JspId generator for javax.servlet.jsp.tagext.JspIdConsumer.
   * Generated JspIds are unique through the application,
   *  not in the JSP page as it is mandated by the JSP 2.1 specification,
   *  because there are some problems in the JSF -  OutputTextTag.
   * @return
   */
  public IDCounter getJspIdGenerator() {
    if( jspIdGenerator == null ) {
      synchronized(this) {
        if( jspIdGenerator == null )
        jspIdGenerator = new IDCounter();
      }
    }
    return jspIdGenerator;
  }
  /**
   * Set reference to the table with all mime types defined in the global-web.xml.
   * @param globalMimesTypes
   */
  void setGlobalMimeTypes(HashMap<String, String> globalMimesTypes) {
    servletContext.setGlobalMimeTypes(globalMimesTypes);
  }

  public void setFilesForPrivateCL(String[] filesForPrivateCL) {
    this.filesForPrivateCL = filesForPrivateCL;
  }

  public void setPrivateResourceReferences(Vector privateResourceReferences) {
    this.privateResourceReferences = privateResourceReferences;
  }

  public RequestDispatcherImpl getReqDispFromCache(String path) {
    if(reqDispCache == null) {
      return null;
    }
    return reqDispCache.get(path);
  }

  public void putReqDispToCache(String path, RequestDispatcherImpl reqDisp) {
    if(reqDispCache == null) {
      reqDispCache = new ConcurrentHashMap<String, RequestDispatcherImpl>(10);
    }
    reqDispCache.put(path, reqDisp);
  }

  public String getCsnComponent() {
    if (csnComponent == null) {
      csnComponent = LoggingUtilities.getCsnComponentByDCName(applicationName);
    }
    return csnComponent;
  }

public ArrayList<String> getWsEndPoints() {
	return wsEndPoints;
}

public void setWsEndPoints(ArrayList<String> wsEndPoints) {
	this.wsEndPoints = wsEndPoints;
}

public Boolean isCheckedForWsEndPoints() {
	return isCheckedForWsEndPoints;
}

public void setIsCheckedForWsEndPoints(Boolean isCheckedForWsEndPoints) {
	this.isCheckedForWsEndPoints = isCheckedForWsEndPoints;
}

public String getJSessionCookie(HttpParameters httpParameters) {
  HttpCookie cokkk = httpParameters.getRequest().getSessionCookie(webApplicationConfig.isURLSessionTracking());
  if (cokkk == null) {
    return  null;
  }
  return cokkk.getValue();
}
}
