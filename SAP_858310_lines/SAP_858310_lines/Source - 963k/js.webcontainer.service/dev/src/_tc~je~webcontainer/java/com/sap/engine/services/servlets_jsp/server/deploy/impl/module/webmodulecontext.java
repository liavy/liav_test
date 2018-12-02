/*
 * Copyright (c) 2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.impl.module;

import static com.sap.engine.services.servlets_jsp.server.LogContext.getExceptionStackTrace;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.WebApplicationConfig;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.ServletDeclaration;
import com.sap.engine.services.servlets_jsp.webcontainer_api.request.IRequestDispatcher;
import com.sap.tc.logging.Location;

/**
 * @author Maria Jurova
 * @version 7.0
 */
public class WebModuleContext implements IWebModuleContext {

  private static Location currentLocation = Location.getLocation(WebModuleContext.class);
  private static Location traceLocation = LogContext.getLocationWebContainerProvider();

  private IWebModule webModule = null;
  private ApplicationContext applicationContext = null;
  private ClassLoader publicClassLoader = null;
  private ClassLoader privateClassLoader = null;
  private File rootDirectory = null;

	/**
	 * Constructs new WebModuleContext object.
	 *
	 * @param webModule
	 * @param applicationContext
	 */
	public WebModuleContext(IWebModule webModule, ApplicationContext applicationContext) {
		this.webModule = webModule;
		this.applicationContext = applicationContext;
	} //end of constructor

	/**
	 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getWebModule()
	 */
	public IWebModule getWebModule() {
		return webModule;
	} //end of getWebModule()

	/**
	 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getRequestDispatcher(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public IRequestDispatcher getRequestDispatcher(ServletRequest request, ServletResponse response) {
		return new IRequestDispatcherImpl(applicationContext, request, response);
	} //end of getRequestDispatcher(ServletRequest request, ServletResponse response)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getRequestDispatcher()
   */
  public IRequestDispatcher getRequestDispatcher() {
    return new IRequestDispatcherImpl(applicationContext);
  } //end of getRequestDispatcher()

  /**
   * Adds the servlet specified by the <code>ServletDeclaration</code> to
   * this <code>IWebModuleContex</code>.
   * It must be called during <code>IWebContainerLifecycle.onStart(...)</code>.
   *
   * <B>Important:</B>
   * <br><ul><li>The annotations (specified in the Java Servlet
   * Specification ver. 2.5, SRV.14.5 Annotations and Resource Injection)
   * used in the servlets that will be added will not be taken into consideration.
   * Web Container explores only the annotations in the servlets that are declared
   * in the web application deployment descriptor (web.xml) during deployment
   * phase (Java Servlet Specification 2.5, SRV.14.5 Annotations and Resource
   * Injection).
   * <li>Run-as functionality cannot be used with this method.
   * <li>Such servlets cannot be visualized in NetWeaver Administrator
   * (Web Container provides information only for the servlets described in
   * the web application deployment descriptor (web.xml)).
   * <li>Such servlets cannot be persisted i.e. the provided method shall be used
   * always during web application start time.
   * </ul>
   *
   * @param servletDeclaration object that specifies the servlet - name, fully
   * qualified class name, init parameters.
   *
   * @throws IllegalArgumentException
   * <br><ul><li>If a servlet with the same servlet name as
   * defined in the <code>servletDeclaration</code> already exists in this
   * <code>IWebModuleContext</code> context.
   * <li>If servlet name is null or empty String, or class name is null or empty
   * String.
   * <li>If servlet class denotes object that does not implement Servlet interface.
   * </ul>
   * @throws IllegalStateException if this <code>IWebModuleContext</code> context
   * has already been initialized.
   * @throws WebContainerExtensionDeploymentException
   * thrown if web container cannot load and/or initialize the servlet.
   *
   * @deprecated A similar method is expected to be introduced with Java Servlet
   * 3.0 which implementation will be available in the next major release and can
   * be used instead of this one.
   */
  public void addServlet(ServletDeclaration servletDeclaration) throws WebContainerExtensionDeploymentException {
    if (applicationContext.isStarted()) {
      throw new IllegalStateException("Web application [" + webModule.getModuleName() + "] is already started.");
    }
    String servletName = servletDeclaration.getServletName();
    String className = servletDeclaration.getClassName();
    if (servletName == null || servletName.equals("") || className == null ||
        className.equals("")) {
      throw new IllegalArgumentException("Incorrect data for the servlet declaration (Servlet name or class is null or empty String).");
    } else {
      if (traceLocation.beDebug()) {
        traceLocation.debugT("WebModuleContext.addServlet(" + servletDeclaration.toString() + ").");
      }

      //Do we need to instantiate servlet? Florian: yes
      ResourceContext resourceContext = null;
      ClassLoader threadLoader = null;
      Thread currentThread = Thread.currentThread();
      try {
        resourceContext = applicationContext.enterResourceContext();
        threadLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(publicClassLoader);
        applicationContext.getWebComponents().addServlet(servletName, className, servletDeclaration.getInitParameters());
      } catch (IllegalArgumentException e) {
        throw e;
      } catch (ServletException e) {
        throw new WCEDeploymentException(WCEDeploymentException.CANNOT_INIT_AND_REGISTER_SERVLET,
          new Object[]{servletName, webModule.getModuleName()}, e);
      } catch (Throwable e) {
        throw new WCEDeploymentException(WCEDeploymentException.CANNOT_INIT_AND_REGISTER_SERVLET,
          new Object[]{servletName, webModule.getModuleName()}, e);
      } finally {
        try {
          applicationContext.exitResourceContext(resourceContext);
          currentThread.setContextClassLoader(threadLoader);
        } catch (Exception e) {
          if (traceLocation.beDebug()) {
            LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
              "Error while exiting resources after adding Servlet via WCE API.", e, "");
          }
        }
      }
    }

    //Add this injected servlet to the change log.
    LogContext.getCategory(LogContext.CATEGORY_CHANGE_LOG_LIFECYCLE).logInfo(currentLocation, "ASJ.web.000625",
      "New servlet [{0}] was added to the context of the web application [{1}].",
      new Object[]{servletDeclaration.toString(), webModule.getModuleName()}, null, null);
  }

  /**
   * Adds servlet mappings from the given URL patterns to the servlet
   * with the given servlet name.
   * It must be called during <code>IWebContainerLifecycle.onStart(...)</code>.
   *
   * <p>The servlet with the given name may have been declared in the
   * deployment descriptor or may be added using <tt>addServlet</tt> method.
   * It is legal to add a servlet mapping for a servlet that has not yet been
   * added.
   *
   * <B>Important:</B>
   * <br><ul><li>If there is already a servlet that is mapped to one of the specified
   * URL patterns, this servlet will be no more accessible as the new mapping
   * will take precedence over the old one.
   * <li>Such servlet's mappings cannot be visualized in NetWeaver Administrator
   * (Web Container provides information only for the servlet's mappings described
   * in the web application deployment descriptor (web.xml)).
   * <li>Such servlet's mappings cannot be persisted i.e. the provided method shall be
   * used always during web application start time.
   * </ul>
   *
   * @param servletName the name of the servlet for which the servlet
   * mapping is added
   * @param urlPatterns the URL patterns of the servlet mapping
   *
   * @throws IllegalArgumentException
   * <br><ul><li>if <tt>urlPatterns</tt> is null or empty.
   * <li>if <tt>urlPatterns</tt> elements does not follow the rules specified in Java Servlet
   * Specification ver. 2.5, SRV.11.2 Specification of Mappings.
   * (The url pattern is assumed to be in URL-decoded form and must not contain CR(#xD) or LF(#xA).)
   * <li>if <tt>servletName</tt> is null or empty String.
   * </ul>
   * @throws IllegalStateException if this <code>IWebModuleContext</code> context
   * has already been initialized
   *
   * @deprecated A similar method is expected to be introduced with Java Servlet
   * 3.0 which implementation will be available in the next major release and can
   * be used instead of this one.
   */
  public void addServletMapping(String servletName, String[] urlPatterns) {
    if (applicationContext.isStarted()) {
      throw new IllegalStateException("Web application [" + webModule.getModuleName() + "] is already started.");
    }
    if (urlPatterns == null || urlPatterns.length == 0 || servletName == null ||
        servletName.equals("")) {
      throw new IllegalArgumentException("Incorrect url patterns or servlet name (value is null or empty).");
    }

    if (traceLocation.beDebug()) {
      traceLocation.debugT("WebModuleContext.addServletMapping(" + servletName + ", " + arrayToString(urlPatterns) + ").");
    }

    for (String urlPattern : urlPatterns) {
      try {
        applicationContext.getWebMappings().addMapping(
          WebApplicationConfig.canonicalizeMapping(urlPattern, webModule.getModuleName()), servletName);
      } catch (DeploymentException e) {
        throw new IllegalArgumentException("Invalid URL pattern [" + urlPattern + "] for servlet [" + servletName + "] in web application [" + webModule.getModuleName() + "].");
      }
    }

    //Add this injected servlet's mappings to the change log.
    LogContext.getCategory(LogContext.CATEGORY_CHANGE_LOG_LIFECYCLE).logInfo(currentLocation, "ASJ.web.000632",
      "New servlet's mappings with servlet name [{0}] and URL patterns [{1}] were added to the context of the web application [{2}].",
      new Object[]{servletName, arrayToString(urlPatterns), webModule.getModuleName()}, null, null);
  }

  /**
   * Returns the fully qualified class name of the servlet associated with the
   * specified <tt>servletName</tt>.
   *
   * @param servletName the name of the servlet for which the servlet
   * class is checked
   * @return the fully qualified class name of the servlet associated with the
   * specified <tt>servletName</tt> or <tt>null</tt> if there is no servlet
   * declared that match the given <tt>servletName</tt>
   *
   * @throws IllegalArgumentException if <tt>servletName</tt> is null or empty
   * String.
   *
   * @deprecated A similar method is expected to be introduced with Java Servlet
   * 3.0 which implementation will be available in the next major release and can
   * be used instead of this one.
   */
  public String getServlet(String servletName) {
    if (servletName == null || servletName.equals("")) {
      throw new IllegalArgumentException("Incorrect value for the servlet name - null or empty String.");
    }
    return applicationContext.getWebComponents().getServletClass(servletName);
  }

  /**
   * Retrieves the instance of the servlet based WS end point associated with the
   * specified <tt>servletName</tt>.
   * @param servletName the name of the servlet associated with the WS end point that is to be retrieved
   * @return the instance of the servlet based WS end point associated with the
   * specified <tt>servletName</tt> or <tt>null</tt> if the servlet based WS end point cannot be retrieved from the servlet associated with it.
   * @throws IllegalArgumentException if <tt>servletName</tt> is null or empty String.
   * @throws ServletException if there is no servlet
   * declaration that matches the given <tt>servletName</tt>
   * @throws ServletException if the servlet that matches the given <tt>servletName<tt> cannot be casted to <tt>SOAPServletExt</tt> type.
   */

  public Object getWebServiceEndPoint(String servletName) throws ServletException{
	  return applicationContext.getWebComponents().getWebServiceEndPoint(servletName);
	}

  /**
   * Checks whether servlet mapping from the given URL pattern to the servlet
   * with the given servlet name exists.
   *
   * @param servletName the name of the servlet for which the servlet
   * mapping is checked
   * @param urlPattern the URL pattern of the servlet mapping
   * @return true if servlet mapping from the given URL pattern to the servlet
   * with the given servlet name exists.
   *
   * @throws IllegalArgumentException
   * <br><ul><li>if <tt>urlPattern</tt> is null or empty String.
   * <li>if <tt>urlPattern</tt> does not follow the rules specified in Java Servlet
   * Specification ver. 2.5, SRV.11.2 Specification of Mappings.
   * (The url pattern is assumed to be in URL-decoded form and must not contain CR(#xD) or LF(#xA).)
   * <li>if <tt>servletName</tt> is null or empty String.
   * </ul>
   */
  public boolean isServletMappingExist(String servletName, String urlPattern) {
    if (servletName == null || servletName.equals("") ||
        urlPattern == null || urlPattern.equals("")) {
      throw new IllegalArgumentException("Incorrect servlet name or url pattern (value is null or empty).");
    }
    try {
      return applicationContext.getWebMappings().isServletMappingExist(
        servletName, WebApplicationConfig.canonicalizeMapping(urlPattern, webModule.getModuleName()));
    } catch (DeploymentException e) {
      throw new IllegalArgumentException("Invalid URL pattern [" + urlPattern + "] for servlet [" + servletName + "] in web application [" + webModule.getModuleName() + "].");
    }
  }

	/**
	 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getServletContext()
	 */
	public ServletContext getServletContext() {
		return applicationContext.getServletContext();
	} //end of getServletContext()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getLocalDeploymentFolder()
   */
  public File getLocalDeploymentFolder() {
    return rootDirectory;
  } //end of getLocalDeploymentFolder()

  /**
   * Sets the local deployment folder for the module ("root" directory) -
   * this is the directory where war file is extracted.
   *
   * @param rootDirectory local deployment folder for the module ("root" directory) -
   * this is the directory where war file is extracted.
   */
  public void setLocalDeploymentFolder(File rootDirectory) {
    this.rootDirectory = rootDirectory;
  } //end of setLocalDeploymentFolder(File rootDirectory)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getPublicClassLoader()
   */
  public ClassLoader getPublicClassLoader() {
    return publicClassLoader;
  } //end of getPublicClassLoader()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getPrivateClassLoader()
   */
  public ClassLoader getPrivateClassLoader() {
    //lazy initialized in ApplicationContext
    return applicationContext.getPrivateClassloader();
  } //end of getPrivateClassLoader()

  /**
   * Sets the public class loader for the application that contains the module.
   *
   * @param publicClassLoader public class loader for the application that contains the module.
   */
  public void setPublicClassLoader(ClassLoader publicClassLoader) {
    this.publicClassLoader = publicClassLoader;
  } //end of setPublicClassLoader(ClassLoader publicClassLoader)

  /**
   * Sets the private class loader for the application that contains the module.
   *
   * @param privateClassLoader private class loader for the application that contains the module.
   */
  public void setPrivateClassLoader(ClassLoader privateClassLoader) {
    this.privateClassLoader = privateClassLoader;
  } //end of setPrivateClassLoader(ClassLoader privateClassLoader)

  /**
   * Returns a string representation of the Object array that is a parameter of the method.
   * If the array is empty then returns empty string.
   * The elements are separated by ", " (comma and space).
   *
   * @param array the Object array that has to be transformed to String.
   * @return a string representation of the Object array that is a parameter of the method.
   * If the array is empty then returns empty string.
   */
  private String arrayToString(Object[] array) {
    if (array == null) {
      return "null";
    }

    if (array != null && array.length == 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    for (Object element : array) {
      builder.append(element).append(", ");
    }

    if (builder.length() > 2) {
      if (builder.charAt(builder.length() - 2) == ',' && builder.charAt(builder.length() - 1) == ' ') {
        builder.delete(builder.length() - 2, builder.length());
      }
    }

    return builder.toString();
  }//end of arrayToString(Object[] array)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext#getLocalContextDispatcher(ServletRequest, ServletResponse, String)
   */
  public IRequestDispatcher getLocalContextDispatcher(ServletRequest request,
      ServletResponse response, String path) {
    try {
      return new IRequestDispatcherImpl(applicationContext, request, response, path);
    }
    catch(IllegalArgumentException e){
      if (traceLocation.beDebug()) {
        traceLocation.debugT("Cannot instantiate IRequestDispatcherImpl. Reason is = [" + getExceptionStackTrace(e) + "].");
      }
      return null;
    }
  }

} //end of class
