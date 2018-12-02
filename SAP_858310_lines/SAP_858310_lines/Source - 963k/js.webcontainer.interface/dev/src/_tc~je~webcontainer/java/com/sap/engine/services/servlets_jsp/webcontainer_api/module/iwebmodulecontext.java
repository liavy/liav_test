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
package com.sap.engine.services.servlets_jsp.webcontainer_api.module;

import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.request.IRequestDispatcher;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.util.Hashtable;

/**
 * Representation of a web module context - a web module started in the web container.
 *
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebModuleContext {

  /**
   * Returns a representation of the web module deployed in the web container.
   *
   * @return a representation of the web module deployed in the web container.
   */
  public IWebModule getWebModule();

  /**
   * Gets a request dispatcher for the web module given the initial request/response pair.
   *
   * @param request  an initial request
   * @param response an initial response
   * @return a request dispatcher for the web module given the initial request/response pair.
   */
  public IRequestDispatcher getRequestDispatcher(ServletRequest request, ServletResponse response);

  /**
   * Gets a request dispatcher for the web module.
   *
   * @return a request dispatcher for the web module.
   */
  public IRequestDispatcher getRequestDispatcher();

  /**
   * Binds a resource name to a servlet.
   *
   * @param servletName    the canonical name of the servlet. Each servlet name is unique within the web module.
   * @param servlet        the servlet object.
   * @param initParameters name/value pairs as initialization parameters of the servlet.
   * @param urlPattern     the URL pattern of the servlet mapping.
   * @throws WebContainerExtensionDeploymentException
   *          thrown if web container cannot initialize and register servlet.
   */
  public void bindServlet(String servletName, Servlet servlet, Hashtable initParameters, String urlPattern) throws WebContainerExtensionDeploymentException;

  /**
   * Returns servlet context.
   *
   * @return the servlet context.
   */
  public ServletContext getServletContext();

  /**
   * Gets the local deployment folder for the web module ("root" directory) -
   * this is the directory where corresponding .war file is extracted.
   * When the application is in STOPPED mode there is no guarantee that
   * the local deployment folder exists.
   *
   * @return a local deployment folder for the web module ("root" directory) -
   *         this is the directory where corresponding .war file is extracted.
   */
  public File getLocalDeploymentFolder();

  /**
   * Gets the public class loader for the application that contains the web module.
   * When the application is in STOPPED mode there is no public class loader.
   * The public class loader is created every time before starting application using
   * the information given to the deploy service during deployment of the application.
   *
   * @return the public class loader for the application that contains the web module.
   */
  public ClassLoader getPublicClassLoader();

  /**
   * Gets the private class loader for the application that contains the web module.
   * When the application is in STOPPED mode there is no private class loader.
   * The private class loader is created from web container during starting application
   * if there are resources in 'WEB-INF/private' directory or there are set
   * files for private class loader during deployment of the application.
   * The private class loader has a public class loader as a parent.
   *
   * @return the private class loader for the application that contains the web module
   *         or null if there are no resources in 'WEB-INF/private' directory or there are not
   *         set files for private class loader during deployment of the application.
   */
  public ClassLoader getPrivateClassLoader();

}//end of interface
