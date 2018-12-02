/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.runtime_api;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.ErrorPageTemplate;

/**
 * Defines an object that extends the <code>HttpServletResponse</code> interface
 * to provide additional response functionality and information for HTTP servlets.
 */
public interface SapHttpServletResponse extends HttpServletResponse {
  /**
   * Sends an error response to the client using the specified status code and message.
   * This is the same as HttpServletResponse.sendError(int i, String s) except that
   * the Web Container can be instructed whether to escape scripts that can be placed
   * into the error response message or not. When used with checkHtml = false,
   * the Web Container does not escape scripts.
   *
   * @param i the error status code
   * @param s the descriptive message
   * @param checkHtml if true, will instruct Web Container to escape scripts found in the
   * error message
   * @throws IOException If an input or output exception occurs
   */
  public void sendError(int i, String s, boolean checkHtml) throws IOException;

  /**
   * Sends an error response to the client using the specified status code, message
   * and additional details.
   * This is the same as HttpServletResponse.sendError(int i, String s) except that
   * the Web Container can be instructed whether to escape scripts that can be placed
   * into the error response message or not. When used with checkHtml = false,
   * the Web Container does not escape scripts.
   *
   * @param i the error status code
   * @param s the descriptive message
   * @param details more details regarding the error situation to be placed in the error response message
   * @param checkHtml if true, will instruct Web Container to escape scripts found in the
   * error message
   * @throws IOException If an input or output exception occurs
   */
  public void sendError(int i, String s, String details, boolean checkHtml) throws IOException;

  /**
   * Sends an error response to the client using the specified error data.
   * The Web Container can be instructed whether to escape scripts that can be replaced
   * into the error response message or not.
   *
   * @param errorData the error data.
   * @param errorPageTemplate specify the error page template, if it is <code>null</code>
   * then the error page template provided by the Web Container will be used.
   * @throws IOException if an input or output exception occurs.
   */
  public void sendError(ErrorData errorData, ErrorPageTemplate errorPageTemplate) throws IOException;

  /**
   * This method returns ID of the current server node to be used by applications.
   * Note that node does not match the actual persistent server node ID and the format
   * may vary.
   * @return integer alias representing ID of the current node
   */
  public int getServerNodeAliasID();

  /**
   * Generates load balancing cookie for the specified logon group for the specified server node.
   * @deprecated
   *
   * @param logonGroup logon group name
   * @param serverId server id
   * @return null
   */
  public String addLoadBalancingCookie(String logonGroup, int serverId);

  /**
   * Generates load balancing cookie for the specified logon group.
   * @deprecated
   *
   * @param logonGroup logon group name
   * @return null
   */
  public String addLoadBalancingCookie(String logonGroup);

  /**
   * Generates cookies so that next request will go the specified server node
   * The method generates saplb_ cookie to the server node which has the specified id
   * with <CODE>serverId</CODE> parameter. The correct way to retrieve this id is via
   * <CODE>getServerNodeAliasID()</CODE> method of this interface, called on the needed
   * server node.
   *
   * The method also generates a brand new jsessionid cookie, which will be used for
   * a session identification on the new server node. Session data is not transported
   * by the engine. This has to be implemented by the application if some is needed
   *
   * Both cookies has path attribute with value the alias of the application
   *
   * <CODE>logonGroup</CODE> might be null
   *
   * Note: this method works within one instance
   *
   * @param logonGroup logon group name
   * @param alias application alias
   * @param serverId server node id
   * @return the new jsessionid
   */
  public String addLoadBalancingCookie(String logonGroup, String alias, int serverId);

  /**
   * Generates load balancing cookie for the specified logon group and application alias.
   * @deprecated
   *
   * @param logonGroup logon group name
   * @param alias application alias
   * @return the new jsessionid
   */
  public String addLoadBalancingCookie(String logonGroup, String alias);

  /**
   * Returns the loadbalancing cookie associated with the request.
   * @return the loadbalancing cookie associated with the request.
   */
  public Cookie getMyLoadBalancingCookie();

  /**
   * Encodes the specified URL  taking in account that logon group is used.
   * @param url the url to be encoded
   * @param logonGroup the name of the logon group
   * @return the encoded URL
   */
  public String encodeURL(String url, String logonGroup);

  /**
   * Returns an absolute URI with the given scheme and path and adequate
   * host and port.
   * <p>
   * The method accepts only absolute paths, e.g. such that start with a
   * leading '/' and interprets them as relative to the servlet container root.
   *
   * @param scheme
   * The required scheme. Allowed values are "http" and "https"
   *
   * @param path
   * An absolute path that start with a leading '/'
   *
   * @return
   * An absolute URI or <code>null</code> in case that servlet container
   * can not find adequate host and port
   */
  public String getURLForScheme(String scheme, String path);

  /**
   * Returns an absolute URI that repeats the current request URI but with
   * the given scheme and adequate host and port
   *
   * @param scheme
   * The required scheme. Allowed values are "http" and "https"
   *
   * @return
   * An absolute URI or <code>null</code> in case that servlet container
   * can not find adequate host and port
   */
  public String getRequestURLForScheme(String scheme);

  /**
   * The method returns an URI for generating error report that can be embedded into the custom error response.
   * If the Web Container's property 'GenerateErrorReport' is not enabled this method will return <code>NULL</code>.
   *
   * @param errorData the error data.
   * @return URI that can be embedded into the error response
   * in the following format '/@@@GenerateErrorReport@@@?id=<the-id-of-the-error-report>'.
   * If the Web Container's property 'GenerateErrorReport' is not enabled this method will return <code>NULL</code>.
   */
  public String getURIForGeneratingErrorReport(ErrorData errorData);

  /**
   * The method traces the Internal Server Error 500 in the default traces with
   * - Severity: Error
   * - Location: com.sap.engine.services.servlets_jsp.ISE500
   * - Message ID: com.sap.ASJ.web.000500
   * - Message: '500 Internal Server Error will be returned for HTTP request [{0}]: component [{1}], web module [{2}], application [{3}], problem categorization [{4}], internal categorization [{5}].'
   *
   * @param errorData the error data that will be used to trace the Internal Server Error 500.
   * @return the ID of the trace record or <code>NULL</code> if a trace record is not created.
   * @deprecated
   */
  public String traceError500(ErrorData errorData);
}
