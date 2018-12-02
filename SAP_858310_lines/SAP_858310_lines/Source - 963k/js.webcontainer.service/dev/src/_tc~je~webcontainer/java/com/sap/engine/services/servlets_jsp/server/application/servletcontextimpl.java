/*
 * Copyright (c) 2000 - 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebMalformedURLException;
import com.sap.engine.services.servlets_jsp.server.lib.EmptyEnumeration;
import com.sap.engine.services.servlets_jsp.server.runtime.RequestDispatcherImpl;
import com.sap.tc.logging.Location;

public class ServletContextImpl implements ServletContext {
  private static Location currentLocation = Location.getLocation(ServletContextImpl.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoClient();

  private ApplicationContext applicationContext = null;
  private String webAppRootDir = null;

  private HashMapObjectObject initParam = new HashMapObjectObject();
  private ConcurrentHashMapObjectObject attributes = new ConcurrentHashMapObjectObject();
  private HashMap<String, String> mimeTypes = null;
  /**
   * This map contains all the mime types defined in the global-web.xml.
   * The table is instantiated during global-web.xml parsing and filled with the mime types found there.
   * No other mime types could/should be added there.
   */
  private HashMap<String, String> globalMimes = null;

  public ServletContextImpl(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.webAppRootDir = applicationContext.getWebApplicationRootDir();
  }

  /**
   * Do not change signature of the method neither the return type value.
   * Used in generated java for JSP. See TagBeginGenerator - injecting tag handlers.
   * @return
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * Returns the name and version of the servlet container on which the servlet is running.
   *
   * @return  InQMy WebServer/4.0.0
   */
  public String getServerInfo() {
    return ServiceContext.getServiceContext().getFullServerVersion();
  }

  /**
   * Returns the name of web application corresponding to this ServletContext
   * as specified in the deployment descriptor for this web application by
   * the display-name element.
   *
   * @return  String representing the name of the web application or null
   *          if no name has been declared in the deployment descriptor.
   */
  public String getServletContextName() {
    return applicationContext.getWebApplicationConfiguration().getDisplayName();
  }

  /**
   * Returns the major version of the Java Servlet API that this servlet
   * container supports.
   *
   * @return  2 i.e. implements Version 2.5
   */
  public int getMajorVersion() {
    return 2;
  }

  /**
   * Returns the minor version of the Servlet API that this servlet
   * container supports.
   *
   * @return  5 i.e. implements Version 2.5
   */
  public int getMinorVersion() {
		return 5;
  }

  /**
   * Returns a String containing the value of the named context-wide
   * initialization parameter, or null if the parameter does not exist.
   * This method can make available configuration information useful
   * to an entire "web application". For example, it can provide a
   * webmaster's e-mail address or the name of a system that holds critical data.
   *
   * @param   name  a String containing the name of the parameter whose value
   *                 is requested
   * @return  a String containing at least the servlet container name and version number
   */
  public String getInitParameter(String name) {
    return (String) initParam.get(name);
  }

  /**
   * Returns the names of the context's initialization parameters as an
   * Enumeration of String objects, or an empty Enumeration if the context
   * has no initialization parameters.
   *
   * @return  an Enumeration of String objects containing the names of the
   *           context's initialization parameters
   */
  public Enumeration getInitParameterNames() {
    return initParam.keys();
  }

  /**
   * Returns the servlet container attribute with the given name, or null if
   * there is no attribute by that name.
   *
   * @param   s  name of attribute
   * @return  servlet container attribute
   */
  public Object getAttribute(String s) {
    return attributes.get(s);
  }

  /**
   * Returns an Enumeration containing the attribute names available
   * within this servlet context.
   *
   * @return  Enumeration containing the attribute names
   */
  public Enumeration getAttributeNames() {
    return attributes.keys();
  }

  public void setAttribute(String s, Object obj) {
    if (traceLocation.beDebug()) {
      traceLocation.debugT("ServletContext.setAttribute [" +
          getObjectInstance() + "] in application [" + applicationContext.getAliasName() + "]: " +
          "name = [" + s + "], value = [" + obj + "]");
    }
    if (s == null) {
      return;
    }
    if (obj == null) {
      removeAttribute(s);
      return;
    }
    Object oldObj = attributes.get(s);
    attributes.put(s, obj);
    if (oldObj != null) {
      // this is by spec, but CTS is opposite
      applicationContext.getWebEvents().contextAttributeReplaced(s, oldObj);
    } else {
      applicationContext.getWebEvents().contextAttributeAdded(s, obj);
    }
  }

  /**
   * Removes the attribute with the given name from the servlet context.
   *
   * @param   s a String specifying the name of the attribute to be removed
   */
  public void removeAttribute(String s) {
    if (traceLocation.beDebug()) {
      traceLocation.debugT(
          "ServletContext.removeAttribute [" + getObjectInstance() + "] in application [" + applicationContext.getAliasName() + "]: " +
          "name = [" + s + "]");
    }
    if (s == null) {
      return;
    }
    Object obj = attributes.remove(s);
    applicationContext.getWebEvents().contextAttributeRemoved(s, obj);
  }

  /**
   * Servlet 2.5
   * Returns the context path of the web application.
   * The context path is the portion of the request URI that is used to select the context of the request.
   * The context path always comes first in a request URI. The path starts with a "/" character but does not end
   * with a "/" character. For servlets in the default (root) context, this method returns "".
   * @return  context path of the web application
   */
  public String getContextPath() {
    String res = null;
    if (applicationContext.isDefault()) {
      res = "";
    } else {
      res = "/".concat(applicationContext.getAliasName());
    }
    //todo zones?
    return res;
  }

  //returns null if no match to an existing alias is found
  private ServletContext checkAlias(byte[] url, int len){
	  MessageBytes alias = new MessageBytes(url, 1, len-1);
	  //check the application
	  //if it is started returns application context
	  //if it has lazy startup mode then it will start it and after it will return the application context
	  ApplicationContext applicationContext = ServiceContext.getServiceContext().getDeployContext().startLazyApplication(alias);
	  if (applicationContext != null){
		          return applicationContext.getServletContext();
	  }
	  return null;
  }
  /**
   * Returns a ServletContext object that corresponds to a specified URL
   * on the server.
   *
   * @param s a String specifying the absolute URL of a resource on the server
   * @return the ServletContext object that corresponds to the named URL
   */
  public ServletContext getContext(String s) {
	  if (s == null || !s.startsWith("/")) {
	      return null;
	    }
	  	byte[] url = ParseUtils.separatorsToSlash(s);
	    int len = url.length;
	    //check 
	    ServletContext tmpSrvCtx = checkAlias(url, len);
	    if (tmpSrvCtx != null){
	    	return tmpSrvCtx;
	    }
	    if (len > 1) {
	        int ind = len-1;
	    	byte[] zoneSeparator = null;
	    	if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() != null
	    	        && ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator().length() != 0) {
	    	   zoneSeparator = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator().getBytes();
	    	   while (ind > 0) {
	    		   //assuming the zone separator consists of one symbol only - zoneSeparator[0]
	    		   //TODO: check for problems in case of longer zone separator
	    		   if (url[ind] == zoneSeparator[0] || url[ind] == ParseUtils.separatorByte) {
	    		     tmpSrvCtx = checkAlias(url, ind);
	    		     if(tmpSrvCtx != null) {
	    			   return tmpSrvCtx;
	    		     }
	    		   }
	    		   ind--;
	    	   }
	        }
	    }
	  //no alias found or s == "/"
	  //then return default app's context (or null if default is not found)
	    ApplicationContext appCtxt = ServiceContext.getServiceContext().getDeployContext().startLazyApplication(new MessageBytes(ParseUtils.separatorBytes));
	    
	    if (appCtxt == null){
	      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logInfo(currentLocation, "ASJ.web.000626", "Cannot find application context for default application. Possible reason: missing default application.", null, null);
	      return null;
	    }
	      return appCtxt.getServletContext();  
  }

  /**
   * Returns a RequestDispatcher object that acts as a wrapper for the
   * resource located at the given path. A RequestDispatcher object can be
   * used to forward a request to the resource or to include the resource
   * in a response. The resource can be dynamic or static.
   *
   * @param   path  a String specifying the path name to the resource
   * @return  a RequestDispatcher object that acts as a wrapper for
   *           the resource at the specified path
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    if (path == null || !path.startsWith("/")) {
      return null;
    }
    if( !applicationContext.isStarted() ){
      if (traceLocation.beWarning()) {
				//the context is not ready to dispatch requests
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000453",
					"Tried to get dispatcher before application start. Path: {0}", 
					new Object[]{path}, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
			}
			return null;
    }
    RequestDispatcherImpl result = null;
    if (path.indexOf("?") > -1) {
      String prop = path.substring(path.indexOf("?") + 1);
      path = path.substring(0, path.indexOf("?"));
      result = applicationContext.getReqDispFromCache(path);
      if (result == null) {
        return new RequestDispatcherImpl(applicationContext, path, prop);
      }
      result.setProp(prop);
    } else {
      result = applicationContext.getReqDispFromCache(path);
      if (result == null) {
        return new RequestDispatcherImpl(applicationContext, path, false);
      }
      result.setProp(null);
    }
    return result;
  }

  /**
   * Returns a RequestDispatcher object that acts as a wrapper for the named servlet.
   *
   * @param   name  a String specifying the name of a servlet to wrap
   * @return  a RequestDispatcher object that acts as a wrapper for the named servlet
   */
  public RequestDispatcher getNamedDispatcher(String name) {
    if( !applicationContext.isStarted() ){
      if (traceLocation.beWarning()) {
				//the context is not ready to dispatch requests
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000454",
					"Tried to get named dispatcher before application start. Name: {0}", 
					new Object[]{name}, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
			}
			return null;
    }
    RequestDispatcherImpl rd = applicationContext.getReqDispFromCache(name);
    if(rd != null) {
      return rd;
    }
    if (applicationContext.getWebComponents().containsServlet(name)) {
      return new RequestDispatcherImpl(applicationContext, name, true);
    } else if (applicationContext.getWebComponents().containsJsp(name)) {
      return new RequestDispatcherImpl(applicationContext, name, true);
    } else {
      return null;
    }
  }

  /**
   * Returns the MIME type of the specified file, or null if the MIME
   * type is not known. Common MIME types are "text/html" and "image/gif".
   *
   * @param   fileExtension  the name of a file
   * @return  a String specifying the file's MIME type
   */
  public String getMimeType(String fileExtension) {
    int extInd = fileExtension.lastIndexOf('.');
    if (extInd > 0) {
      fileExtension = fileExtension.substring(extInd + 1);
    }
    String mime = null ;
    if( mimeTypes != null) {
      mime = mimeTypes.get(fileExtension);
    }
    if( mime == null) {
      mime = globalMimes.get(fileExtension);
    }
    return mime;
  }

  public String getMimeType(char[] fileExtensionChars) {
    String fileExtension = new String(fileExtensionChars);
    return getMimeType(fileExtension);
  }

  /**
   * Writes the specified message to log file of service.
   *
   * @param   s  message
   */
  public void log(String s) {
    LogContext.getLocationWebApplications().pathT(s);
  }

  /**
   * Writes an Throwable exception's stack trace and an error message to
   * log file of service.
   *
   * @param   s message
   * @param   throwable Throwable object
   */
  //TODO: call logAndTrace(...) when this method becomes available 
  public void log(String s, Throwable throwable) {
    LogContext.getCategory(LogContext.CATEGORY_WEB_APPLICATIONS).logWarning(currentLocation, "", s, throwable, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
  }


  /**
   * Writes an exception's stack trace and an error message to
   * log file of service.
   *
   * @param   exception specifies Exception
   * @param   s message
   * @deprecated
   */
  //TODO: call logAndTrace(...) when this method becomes available 
  public void log(Exception exception, String s) {
    LogContext.getCategory(LogContext.CATEGORY_WEB_APPLICATIONS).logWarning(currentLocation, "", s, exception, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
  }

  /**
   * Returns real path to resource located at the given path.
   *
   * @param   path  a String specifying the path name to the resource
   * @return  real path to  resource
   */
  
  public String getRealPath(String path) {
	if (path == null) {
	  return null;
	}
	path = path.replace('/', File.separatorChar);
	String res = (new File(webAppRootDir, path)).getAbsolutePath();	
	if (path.endsWith(File.separator) && !res.endsWith(File.separator)) {
		res += File.separator;
	}
	return res;
  }

  
  /**
   * Return all the paths to resources held in the web application.
   * All paths are java.lang.String objects, begin
   * with a leading /, and are relative to the root of the web application.
   *
   * @param   s  a String specifying the path to the resource
   * @return   an immutable set containing the paths
   */
  public Set getResourcePaths(String s) {
    if (s == null || s.equals("") || !s.startsWith("/")) {
      return null;
    }
    s = ParseUtils.canonicalize(s);
    String absPath = getRealPath(s);
    if (absPath == null) {
      return null;
    }
    HashSet set = new HashSet();
    File file = new File(absPath);
    if (file.exists()) {
      File[] list = null;
      if (file.isDirectory()) {
        list = file.listFiles();
      } else {
        //supplied passed argument doesn't match a sub-path in the web application
        set = null;
      }
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          if (list[i].isDirectory()) {
            set.add(s + "/" + list[i].getName() + "/");
          } else {
            set.add(s + "/" + list[i].getName());
          }
        }
      }
    } else {
      set = null;
    }
    return set;
  }

  /**
   * Returns a URL to the resource that is mapped to a specified path.
   *
   * @param   s  a String specifying the path to the resource
   * @return   the resource located at the named path,
   *           or null if there is no resource at that path
   * @exception   java.net.MalformedURLException  if the path name is not given in the correct form
   */
  public URL getResource(String s) throws MalformedURLException {
    URL url = getDocumentBase();
    if (s == null || s.equals("")) {
      throw new WebMalformedURLException(WebMalformedURLException.Cannot_getResource_of_empty_string);
    }
    if (!s.startsWith("/")) {
      throw new WebMalformedURLException(WebMalformedURLException.Path_must_begin_with_slash);
    } else {
      URL url1 = null;
      s = s.substring(1);
      if (webAppRootDir.endsWith(ParseUtils.separator)) {
        url1 = new URL(url.getProtocol(), url.getHost(), url.getPort(),
            ("/" + (new File(webAppRootDir + s)).getAbsolutePath()).replace('\\', '/'));
      } else {
        url1 = new URL(url.getProtocol(), url.getHost(), url.getPort(),
            ("/" + (new File(webAppRootDir + "/" + s)).getAbsolutePath()).replace('\\', '/'));
      }
      if (new File(url1.getFile()).exists()) {
        return url1;
      } else {
        return null;
      }
    }
  }

  /**
   * Returns the resource located at the named path as an InputStream object.
   *
   * @param   path a String specifying the path to the resource
   * @return  the InputStream returned to the servlet, or null if no resource
   *           exists at the specified path
   */
  public InputStream getResourceAsStream(String path) {
    InputStream is = null;
    try {
      URL url = getResource(path);

      if (url == null) {
        return null;
      }

      URLConnection con = url.openConnection();
      con.connect();
      is = con.getInputStream();
    } catch (MalformedURLException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000357",
                "Cannot get resource located at [{0}] as an InputStream object.", new Object[]{path}, e, null, null);
    } catch (IOException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000358",
              "Cannot get resource located at [{0}] as an InputStream object.", new Object[]{path}, e, null, null);

    }
    return is;
  }

  /**
   * Returns servlet from ServletContext by specified name.
   *
   * @param   name  name of servlet
   * @return  Servlet
   * @deprecated
   */
  public Servlet getServlet(String name) {
    return null;
  }

  /**
   * Returns an Enumeration of all the servlet names known to this context.
   *
   * @return  Enumeration of servlets
   * @deprecated
   */
  public Enumeration getServletNames() {
    return new EmptyEnumeration();
  }

  /**
   * Returns Enumeration , containing servlets from ServletContext.
   *
   * @return  Enumeration of servlets
   * @deprecated
   */
  public Enumeration getServlets() {
    return new EmptyEnumeration();
  }

  // -------------------- PROTECTED --------------------

  void addInitParameter(String name, String value) {
    initParam.put(name, value);
  }

  void addMimeType(String name, String value) {
    if( mimeTypes == null ) {
      mimeTypes = new HashMap<String, String>();
    }
    mimeTypes.put(name, value);
  }

  // -------------------- PRIVATE --------------------

  private URL getDocumentBase() {
    try {
      String absName = new File("").getCanonicalPath();
      return new URL("file://localhost/" + absName);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000456", 
        		  "Error in method getDocumentBase().", e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    }
      return null;
    }
  }

  private String getObjectInstance() {
    String instance = super.toString();
    return instance.substring(instance.indexOf('@') + 1);
  }

  /**
   * Set reference to the table with all mime types defined in the global-web.xml.
   * @param globalMimesTypes
   */
  void setGlobalMimeTypes(HashMap<String, String> globalMimesTypes) {
    if( globalMimes != null ) {
      if (traceLocation.beWarning()) {
				// reference to this table is set only once during application startup.
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning( "ASJ.web.000457",
						"Call to this method is forbidden:", new Exception(), applicationContext.getApplicationName(), applicationContext.getCsnComponent());
			}
			return;
    }
    this.globalMimes = globalMimesTypes;
  }
}
