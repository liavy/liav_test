/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.*;
import javax.servlet.jsp.tagext.BodyContent;
import javax.el.*;
import javax.el.FunctionMapper;


import com.sap.engine.lib.util.Stack;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIllegalStateException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspNullPointerException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspServletException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.tc.logging.Location;

/**
 * Provides access to all the namespaces associated with a JSP page,
 * provides access to several page attributes, as well as a layer above the implementation details.
 */
public class PageContextImpl extends PageContext {
  /**
   * name used to store ServletContext in PageContext name table
   */
  private static final String APPCONTEXT = "javax.servlet.jsp.jspAppContext";
	private static Location currentLocation = Location.getLocation(PageContextImpl.class);
  /**
   * a servlet instance of this jsp
   */
  private Servlet servlet = null;
  /**
   *  used to pass information to a servlet during initialization
   */
  private ServletConfig config = null;
  /**
   * a set of methods that a servlet uses to communicate with its servlet container
   */
  private ServletContext context = null;
  /**
   * if some error page is specified in jsp
   */
  private String errorPageURL = null;
  /**
   * page scope attributes
   */
  private transient Hashtable attributes = new Hashtable(16);
  /**
   * response invoked this jsp
   */
  private transient ServletRequest request = null;
  /**
   * response that will be returned from this jsp
   */
  private transient ServletResponse response = null;
  /**
   * a session object for this jsp
   */
  private transient HttpSession session = null;
  /**
   * initial output stream
   */
  private transient JspWriter out = null;
  /**
   * save the current "out" JspWriter
   */
  private Stack writerStack = null;
  /**
   * Expression evaluator
   */
  private static ExpressionEvaluator expressionEvaluator = null;
  /**
   * Variable resolver
   */
  private VariableResolver variableResolver = null;
  /**
   * JspApplicationContext cached
   */
  private JspApplicationContextImpl jspApplicationContext;
  /**
   * ELContext cached
   */
  private ELContext elContext;
  /**
   * ELResolver cached
   */
  private ELResolver elResolver;

  /**
   * Primary constructor for PageContext, requires subsequent call to
   * initialize() in order to handle a Servlet request.
   */
  public PageContextImpl() {
    super();
  }

  private String getRelativeUrl(String relativeUrl) {
    String path = relativeUrl;

    if (!path.startsWith("/")) {
      String uri = (String) request.getAttribute("javax.servlet.include.servlet_path");

      if (uri == null) {
        uri = ((HttpServletRequest) request).getServletPath();
      }

      String relativeURI = uri;

      if (!"".equals(uri)) {
        relativeURI = uri.substring(0, uri.lastIndexOf('/'));
      }

      path = relativeURI + '/' + path;
    }

    return path;
  }

  /**
   * Causes the resource specified to be processed as part of the current
   * ServletRequest and ServletResponse being processed by the calling Thread.
   * The output of the target resources processing of the request is written
   * the the JspWriter specified.
   *
   * @param       jspFile            jspFile to be included
   * @exception   ServletException  throws it
   * @exception   IOException        throws it
   */
  public void include(String jspFile) throws ServletException, IOException {
    String path = getRelativeUrl(jspFile);
    out.flush();
    context.getRequestDispatcher(path).include(request, response);
  }

  /**
   * Causes the resource specified to be processed as part of the current
   * ServletRequest and ServletResponse being processed by the calling Thread.
   * The output of the target resources processing of the request is written
   * the the JspWriter specified.
   * This uses when generating Java file from jsp.
   *
   * @param       jspFile            jspFile to be included
   * @param       flush              to flush content
   * @exception   ServletException   throws it
   * @exception   IOException        throws it
   */
  public void include(String jspFile, boolean flush) throws ServletException, IOException {
    String path = getRelativeUrl(jspFile);
    request.setAttribute("com.sap.engine.internal.jsp.includeInOut", "true");
    request.setAttribute("com.sap.engine.internal.jsp.include.flush", new Boolean(flush));
    if (flush) {
      out.flush();
    }
    RequestDispatcher requestDispatcher = context.getRequestDispatcher(path);
		requestDispatcher.include(request, response);
		request.removeAttribute("com.sap.engine.internal.jsp.include.flush");
    request.removeAttribute("com.sap.engine.internal.jsp.includeInOut");    
  }

  /**
   * This method is used to re-direct, or "forward" the current ServletRequest
   * and ServletResponse to another active component in the application.
   *
   * @param       jspFile            jspFile to be forworded
   * @exception   ServletException  throws it
   * @exception   IOException        throws it
   */
  public void forward(String jspFile) throws ServletException, IOException {
    // JSP.5.5 If the page output was unbuffered and anything has been written to it, an
    // attempt to forward the request will result in an IllegalStateException.
    if (out.getBufferSize() == 0) {
      throw new JspIllegalStateException(JspIllegalStateException.ATTEMP_FORWARD_NO_BUFFER);
    }
    String path = getRelativeUrl(jspFile);
    context.getRequestDispatcher(path).forward(request, response);
  }

  /**
   * This method is intended to process an unhandled "page" level exception
   * by redirecting the exception to either the specified error page for this
   * JSP, or if none was specified, to perform some implementation dependent
   * action.
   *
   * @param   exception  thrown exception to be handled
   */
  public void handlePageException(Exception exception) throws ServletException, IOException {
    if (exception == null) {
      throw new JspNullPointerException(JspNullPointerException.INTERNAL_SERVER_ERROR);
    }

    handleErrorPage(exception);
  }

  /**
   * Creates and returns a new JspWriter.
   *
   * @param   		buffer  			buffer of writer
   * @param   		flag  				autoflush
   * @return     	new JspWriter
   * @exception   IllegalArgumentException  throws it
   */
  private JspWriter _createOut(int buffer, boolean flag) throws IOException, IllegalArgumentException {
    return new JspWriterImpl(response, request, buffer, flag);
  }

  /**
   * Initiates the instance with servlet parameters.
   *
   * @param   servlet  A refference to this servlet
   * @param   request  A refference to the request to this jsp
   * @param   response  A refference to the responce that will be returned from jsp
   * @param   errorPageURL  If an error page is set in jsp
   * @param   needsSession  if a new session will be created if there isn't such
   * @param   bufferSize  size of the buffer
   * @param   autoFlush  if buffer will be autoflushed
   * @exception   IllegalStateException
   * @exception   IllegalArgumentException
   */
  public void initialize(Servlet servlet, ServletRequest request, ServletResponse response,
                         String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush)
      throws IOException, IllegalStateException, IllegalArgumentException {
    // initialize state
    this.servlet = servlet;
    this.config = servlet.getServletConfig();
    this.context = config.getServletContext();
    this.errorPageURL = errorPageURL;
    this.request = request;
    this.response = response;
		this.variableResolver = new VariableResolverWrapper(this);
    this.jspApplicationContext = JspApplicationContextImpl.getJspApplicationContext(context);
    // setup session (if required)
    if ((request instanceof HttpServletRequest) && needsSession) {
      this.session = ((HttpServletRequest) request).getSession(needsSession);
    }

    if (needsSession && session == null) {
      throw new JspIllegalStateException(JspIllegalStateException.PAGE_NEEDS_SESSION_AND_NONE_IS_AVAILABLE);
    }

    // initialize the initial out ...
    //in case of include out must be the same !
    JspWriter out_included = null;
    String includeInOut = (String)request.getAttribute("com.sap.engine.internal.jsp.includeInOut");
    if (includeInOut != null && includeInOut.equals("true")) {
      out_included = (JspWriter)request.getAttribute("com.sap.engine.internal.jsp.out");
    }
    if (out_included != null) {
      out = out_included;
    } else {
      if (out == null) {
        out = _createOut(bufferSize, autoFlush); // throws
      } else {
        ((JspWriterImpl) out).init(response, request, bufferSize, autoFlush);
      }
    }

    if (this.out == null) {
      throw new JspIllegalStateException(JspIllegalStateException.FAILED_INITILIZE_JSP_WRITER);
    }

    // register names/values as per spec
    setAttribute(OUT, this.out);
    request.setAttribute("com.sap.engine.internal.jsp.out", this.out);
    setAttribute(REQUEST, request);
    setAttribute(RESPONSE, response);

    if (session != null) {
      setAttribute(SESSION, session);
    }

    setAttribute(PAGE, servlet);
    setAttribute(CONFIG, config);
    setAttribute(PAGECONTEXT, this);
    setAttribute(APPCONTEXT, context);
    jspApplicationContext.setFirstRequest(true);
    //setAttribute("com.sap.engine.internal.jsp.firstRequest", "true", APPLICATION_SCOPE);
  }

  /**
   * This method shall "reset" the internal state of a PageContext, releasing
   * all internal references, and preparing the PageContext for potential
   * reuse by a later invocation of initialize(). This method is typically
   * called from JspFactory.releasePageContext(). Subclasses shall envelope this method.
   *
   */
  public void release() {
    try {
      if (!FilterUtils.unWrapResponse(response).isClosed()) {

          Boolean willFlush = (Boolean) request.getAttribute("com.sap.engine.internal.jsp.include.flush");
          if (willFlush == null || willFlush.booleanValue()) {
            if (out instanceof JspWriterImpl) {
              ((JspWriterImpl) out).flushBuffer();
            } else {
              ((BodyContentImpl) out).flushBuffer();
            }
          }
       
      }
      //Stack includeStack = (Stack)request.getAttribute("com.sap.servlet.private.include.stack");
      //if (includeStack == null || includeStack.isEmpty()) {
      //  out.flush();
      //}
    } catch (IOException ex) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000285",
          "Error in finalizing PageContext.", ex, null, null); 
    }
    out = null;
    servlet = null;
    config = null;
    context = null;
    errorPageURL = null;
    request = null;
    response = null;
    session = null;
    this.variableResolver = null;
    attributes.clear();
    if (writerStack != null) {
      writerStack.clear();
    }
    jspApplicationContext = null;
    variableResolver = null;
    expressionEvaluator = null;
    elContext = null;
    elResolver = null;
  }

  /**
   * Registers the name and object specified with page scope semantics
   *
   * @param   name  name of the attribute
   * @param   attribute  Object of the attribuete
   */
  public void setAttribute(String name, Object attribute) {
    if (name == null) {
      throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL, new Object[]{name});
    }
    if (attribute == null) {
      removeAttribute(name, PageContext.PAGE_SCOPE);
      return;
    }

    attributes.put(name, attribute);
  }

  /**
   * Register the name and object specified with appropriate scope semantics.
   *
   * @param   name  name of the attribute
   * @param   o  Object of the attribuete
   * @param   scope  scope of the attribute
   */
  public void setAttribute(String name, Object o, int scope) {
    if (name == null) {
      throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL, new Object[]{name});
    }
    if (o == null) {
      removeAttribute(name, scope);
      return;
    }

    switch (scope) {
      case PAGE_SCOPE: {
        attributes.put(name, o);
        break;
      }
      case REQUEST_SCOPE: {
        request.setAttribute(name, o);
        break;
      }
      case SESSION_SCOPE: {
        if (session == null) {
          throw new JspIllegalStateException(JspIllegalArgumentException.CANNOT_ACCESS_SESSION_SCOPE_WITHOUT_HTTP_SESSION);
        } else {
          session.setAttribute(name, o);
        }

        break;
      }
      case APPLICATION_SCOPE: {
        context.setAttribute(name, o);
        break;
      }
      default: {
        throw new JspIllegalArgumentException(JspIllegalArgumentException.UNIDENTIFIED_ATTRIBUTE_SCOPE, new Object[]{name});
      }
    }
  }

  /**
   * Returns the object named .
   *
   * @param   name  name of the object
   * @return the object named or null if not found
   */
  public Object getAttribute(String name) {
    if (name == null) {
      throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL, new Object[]{name});
    }

    return attributes.get(name);
  }

  /**
   * Gets an attribute within the scope.
   *
   * @param   name  name of attribute
   * @param   scope  ID of the scope of the attribute
   * @return     attribute within the scope
   */
  public Object getAttribute(String name, int scope) {
    if (name == null) {
      throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL, new Object[]{name});
    }

    switch (scope) {
      case PAGE_SCOPE: {
        return attributes.get(name);
      }
      case REQUEST_SCOPE: {
        return request.getAttribute(name);
      }
      case SESSION_SCOPE: {
        if (session == null) {
          throw new JspIllegalArgumentException(JspIllegalArgumentException.CANNOT_ACCESS_SESSION_SCOPE_WITHOUT_HTTP_SESSION);
        } else {
          return session.getAttribute(name);
        }
      }
      case APPLICATION_SCOPE: {
        return context.getAttribute(name);
      }
      default: {
        throw new JspIllegalArgumentException(JspIllegalArgumentException.UNIDENTIFIED_ATTRIBUTE_SCOPE, new Object[]{name});
      }
    }
  }

  /**
   * Removes the object reference associated with the specified name from all scopes.
   * If null throws JspNullPointerException
   * @param   name name of the object
   */
  public void removeAttribute(String name) {
		if (name == null) {
			throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL);
		}

		attributes.remove(name);
		removeAttribute(name, REQUEST_SCOPE);
		if (getSession() != null) {
			removeAttribute(name, SESSION_SCOPE);
		}
		removeAttribute(name, APPLICATION_SCOPE);
  }

  /**
   * Removes the object reference associated with the specified name
   *
   * @param   name name of the object
   * @param   scope ID of the scope of the object
   */
  public void removeAttribute(String name, int scope) {
    switch (scope) {
      case PAGE_SCOPE: {
        attributes.remove(name);
        break;
      }
      case REQUEST_SCOPE: {
        request.removeAttribute(name);
        break;
      }
      case SESSION_SCOPE: {
        if (session == null) {
          throw new JspIllegalArgumentException(JspIllegalArgumentException.CANNOT_ACCESS_SESSION_SCOPE_WITHOUT_HTTP_SESSION);
        } else {
          session.removeAttribute(name);
        }

        break;
      }
      case APPLICATION_SCOPE: {
        context.removeAttribute(name);
        break;
      }
      default:
        throw new JspIllegalArgumentException(JspIllegalArgumentException.UNIDENTIFIED_ATTRIBUTE_SCOPE, new Object[]{name});
    }
  }

  /**
   * Returns the scope of the object associated with the name specified or 0.
   *
   * @param   name name of the object
   * @return the scope of the object associated with the name specified or 0
   */
  public int getAttributesScope(String name) {
    if (name == null) {
      throw new JspNullPointerException(JspNullPointerException.ATTRIBUTE_NAME_NULL, new Object[]{name});
    }

    if (attributes.get(name) != null) {
      return PAGE_SCOPE;
    }

    if (request.getAttribute(name) != null) {
      return REQUEST_SCOPE;
    }

    if (session != null) {
      if (session.getAttribute(name) != null) {
        return SESSION_SCOPE;
      }
    }

    if (context.getAttribute(name) != null) {
      return APPLICATION_SCOPE;
    }

    return 0;
  }

  /**
   * Returns an Enumeration of the elements of the given array.
   *
   * @param   ar  an array of String
   * @return     Enumeration of the elements of the array
   */
  public Enumeration getEnum(String[] ar) {
    Vector v = new Vector();

    for (int i = 0; i < ar.length; i++) {
      v.addElement(ar[i]);
    }

    return v.elements();
  }

  /**
   * Returns an enumeration of names (java.lang.String) of all the attributes the specified scope.
   *
   * @param   scope  ID of the scope
   * @return an enumeration of names (java.lang.String) of all the attributes the specified scope
   */
  public Enumeration getAttributeNamesInScope(int scope) {
    switch (scope) {
      case PAGE_SCOPE: {
        return attributes.keys();
      }
      case REQUEST_SCOPE: {
        return request.getAttributeNames();
      }
      case SESSION_SCOPE: {
        if (session != null) {
          return session.getAttributeNames();
        } else {
          throw new JspIllegalArgumentException(JspIllegalArgumentException.CANNOT_ACCESS_SESSION_SCOPE_WITHOUT_HTTP_SESSION);
        }
      }
      case APPLICATION_SCOPE: {
        return context.getAttributeNames();
      }
      default: {
        throw new JspIllegalArgumentException(JspIllegalArgumentException.UNIDENTIFIED_ATTRIBUTE_SCOPE);
      }
    }
  }

  /**
   * Returns a new BodyContent object, save the current "out" JspWriter,
   * and update the value of the "out" attribute in the page scope attribute namespace of the PageContext.
   *
   * @return     the new BodyContent
   */
  public JspWriter pushBody(java.io.Writer writer) {
    if (writerStack == null) {
      writerStack = new Stack();
    }

    JspWriter previous = out;
    //if (out instanceof JspWriterImpl) {
    //  try {
    //    ((JspWriterImpl) out).flushBuffer();
    //  } catch (IOException e) {
    //    LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation,
    //        "Can't flush buffer in the method pushBody().", e, "");
    //  }
    //}
    writerStack.push(out);
    out = new BodyContentImpl(previous);
    if (writer != null) {
      ((BodyContentImpl)out).setWriter(writer);
    }
    setAttribute(OUT, out);
    request.setAttribute("com.sap.engine.internal.jsp.out", this.out);
    return (BodyContent) out;
  }

  public BodyContent pushBody() {
    return (BodyContent) pushBody(null);
  }

  /**
   * Returns the previous JspWriter "out" saved by the matching pushBody(),
   * and update the value of the "out" attribute in the page scope attribute namespace of the PageConxtext.
   *
   * @return     the saved JspWriter
   */
  public JspWriter popBody() {
    if (writerStack == null) {
      writerStack = new Stack();
      return out;
    }

    out = (JspWriter) writerStack.pop();
    setAttribute(OUT, out);
    request.setAttribute("com.sap.engine.internal.jsp.out", this.out);
    return out;
  }

  /**
   * Returns the JspWriter for this PageContext.
   *
   * @return the current JspWriter stream being used for client response
   */
  public JspWriter getOut() {
    return out;
  }

  /**
   * Returns the HttpSession for this PageContext or null.
   *
   * @return the HttpSession for this PageContext or null
   */
  public HttpSession getSession() {
    return session;
  }

  /**
   * Returns the Servlet associated with this PageContext.
   *
   * @return the Servlet associated with this PageContext
   */
  public Servlet getServlet() {
    return servlet;
  }

  /**
   * Returns the ServletConfig for this PageContext.
   *
   * @return the ServletConfig for this PageContext
   */
  public ServletConfig getServletConfig() {
    return config;
  }

  /**
   * Returns the ServletContext for this PageContext.
   *
   * @return the ServletContext for this PageContext
   */
  public ServletContext getServletContext() {
    return config.getServletContext();
  }

  /**
   * Returns The ServletRequest for this PageContext.
   *
   * @return The ServletRequest for this PageContext
   */
  public ServletRequest getRequest() {
    return request;
  }

  /**
   * Returns the ServletResponse for this PageContext.
   *
   * @return the ServletResponse for this PageContext
   */
  public ServletResponse getResponse() {
    return response;
  }

  /**
   * Returns any exception passed to this as an errorpage.
   *
   * @return any exception passed to this as an errorpage
   */
  public Exception getException() {
    return (Exception) request.getAttribute(EXCEPTION);
  }

  /**
   * Returns the Page implementation class instance (Servlet)  associated with this PageContext.
   *
   * @return the Page implementation class instance (Servlet)  associated with this PageContext
   */
  public Object getPage() {
    return getAttribute(PAGE);
  }

  /**
   * Searches for the named attribute in page, request, session (if valid),
   * and application scope(s) in order and returns the value associated or
   * null.
   *
   * @param   name  name of the attribute
   * @return the value associated or null
   */
  public Object findAttribute(String name) {
    Object obj = null;
    //  case PAGE_SCOPE:
    obj = attributes.get(name);

    if (obj != null) {
      return obj;
    }

    //  case REQUEST_SCOPE:
    obj = request.getAttribute(name);

    if (obj != null) {
      return obj;
    }

    //  case SESSION_SCOPE:
    if (session != null) {
      obj = session.getAttribute(name);

      if (obj != null) {
        return obj;
      }
    }

    //  case APPLICATION_SCOPE:
    return context.getAttribute(name);
  }

  //jsp1.2
  public void handlePageException(Throwable trowable) throws ServletException, IOException {
    if (trowable == null) {
      throw new JspNullPointerException(JspNullPointerException.INTERNAL_SERVER_ERROR);
    }

    handleErrorPage(trowable);
  }

  // ---- JSP 2.0 ----

  public ExpressionEvaluator getExpressionEvaluator() {
    if (expressionEvaluator == null) {
      expressionEvaluator = new ExpressionEvaluatorWrapper(this);
    }
    return expressionEvaluator;
  }

  public VariableResolver getVariableResolver() {
    if (variableResolver == null) {
      variableResolver = new VariableResolverWrapper(this);
    }
    return variableResolver; 
  }

  // ---- JSP 2.1 ----

  public ELContext getELContext() {
    if (elContext == null) {
      elContext = getJspApplicationContext().createELContext(getELResolver(), this);
    }
    return elContext;
  }

  protected JspApplicationContextImpl getJspApplicationContext() {
    if (jspApplicationContext == null) {
      jspApplicationContext = JspApplicationContextImpl.getJspApplicationContext(this.getServletContext());
    }
    return jspApplicationContext;
  }


  private ELResolver getELResolver() {
    if (elResolver == null) {
      CompositeELResolver compositeELResolver = new CompositeELResolver();
      compositeELResolver.add(new ImplicitObjectELResolver());

      Object[] ctxResolvers = getJspApplicationContext().getElResolvers();
      for (int i = 0; i < ctxResolvers.length; i++) {
        compositeELResolver.add((ELResolver) ctxResolvers[i]);
      }
      compositeELResolver.add(new MapELResolver());
      compositeELResolver.add(new ResourceBundleELResolver());
      compositeELResolver.add(new ListELResolver());
      compositeELResolver.add(new ArrayELResolver());
      compositeELResolver.add(new BeanELResolver());
      compositeELResolver.add(new ScopedAttributeELResolver());
      elResolver = compositeELResolver;
    }
    return elResolver;
  }

  public ValueExpression getValueExpression(String expression, PageContext pageContext, Class expectedType, FunctionMapper functionMap) {
    ELContextImpl elctxt = (ELContextImpl) pageContext.getELContext();
    elctxt.setFunctionMapper(functionMap);
    return getJspApplicationContext().getExpressionFactory().createValueExpression(elctxt, expression, expectedType);
  }

  public MethodExpression getMethodExpression(String expression, PageContext pageContext, FunctionMapper functionMap, Class expectedType, Class[] paramTypes) {
    ELContextImpl elctxt = (ELContextImpl) pageContext.getELContext();
    elctxt.setFunctionMapper(functionMap);
    return getJspApplicationContext().getExpressionFactory().createMethodExpression(elctxt, expression, expectedType, paramTypes);
  }

  public void setValueVariable(PageContext pageContext, String variable, ValueExpression expression) {
    ELContextImpl elctxt = (ELContextImpl) pageContext.getELContext();
    elctxt.getVariableMapper().setVariable(variable, expression);
  }

  public void setMethodVariable(PageContext pageContext, String variable, MethodExpression expression) {
    ValueExpression exp = getJspApplicationContext().getExpressionFactory().createValueExpression(expression, Object.class);
    setValueVariable(pageContext, variable, exp);
  }

  public Object evaluateInternal(String expression , Class toClass, boolean escape, FunctionMapper mapper) throws javax.el.ELException {
    ((ELContextImpl)getELContext()).setFunctionMapper(mapper);
    ValueExpression expr = getJspApplicationContext().getExpressionFactory().createValueExpression(getELContext(), expression, toClass);
    return expr.getValue(getELContext());
  }
  // used in tag files
  public Object evaluateInternal(String expression , Class toClass, boolean escape, PageContext pageContext, FunctionMapper mapper) throws javax.el.ELException {
    ELContextImpl elCtx = (ELContextImpl)pageContext.getELContext();
    elCtx.setFunctionMapper(mapper);
    ValueExpression expr = getJspApplicationContext().getExpressionFactory().createValueExpression(elCtx, expression, toClass);
    return expr.getValue(getELContext());
  }

  private static String escapeXml(String s) {
      if( s == null ) {
        return null;
      }
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if(c == '<') {
              sb.append("&lt;");
              continue;
          }
          if(c == '>') {
              sb.append("&gt;");
              continue;
          }
          if(c == '\'') {
              sb.append("&#039;");
              continue;
          }
          if(c == '&') {
              sb.append("&amp;");
              continue;
          }
          if(c == '"') {
            sb.append("&#034;");
          } else {
            sb.append(c);
          }
      }
      return sb.toString();
  }



  private void handleErrorPage(Throwable throwable) throws ServletException, IOException {
    if (request.getAttribute("javax.servlet.jsp.jspException") != null ) {
      throw new JspServletException(JspServletException.ERROR_IN_JSP, throwable);
    }

    request.setAttribute("javax.servlet.jsp.jspException", throwable);
    request.setAttribute("javax.servlet.error.exception", throwable);
    request.setAttribute("javax.servlet.error.status_code", new Integer(ResponseCodes.code_internal_server_error));
    request.setAttribute("javax.servlet.error.message", throwable.getLocalizedMessage());
    request.setAttribute("javax.servlet.error.request_uri", ((HttpServletRequest)getRequest()).getRequestURI());
    request.setAttribute("javax.servlet.error.servlet_name", ((HttpServletRequest)getRequest()).getServletPath());
    
    
    if (errorPageURL != null && !errorPageURL.equals("")) {
      try {
        forward(errorPageURL);
      } catch (IllegalStateException ise) {
        try {
          include(errorPageURL);
        } catch (IOException io) {
          throw io;
        } catch (ServletException se) {
          throw se;
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          request.setAttribute("javax.servlet.jsp.jspException", e);
          request.setAttribute("javax.servlet.error.exception", e);
          throw new JspServletException(JspServletException.ERROR_IN_PROCESSING_ERROR_BY_ERROR_PAGE, new Object[] {errorPageURL}, e);
        }
      } catch (IOException io) {
        throw io;
      } catch (ServletException se) {
        throw se;
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        request.setAttribute("javax.servlet.jsp.jspException", e);
        request.setAttribute("javax.servlet.error.exception", e);
        throw new JspServletException(JspServletException.ERROR_IN_PROCESSING_ERROR_BY_ERROR_PAGE, new Object[] {errorPageURL}, e);
      }
    } else {
			if ( throwable instanceof ServletException ){
				throw (ServletException)throwable;
			}
			if ( throwable instanceof IOException ){
				throw (IOException)throwable;
			}
			if ( throwable instanceof RuntimeException ){
				throw (RuntimeException)throwable;
			}

      throw new JspServletException(JspServletException.ERROR_IN_JSP, throwable);
    }
  }
}

