/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionListener;

import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.lib.security.http.HttpSecureSession;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.InvalidSessionException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebInvalidSessionException;
import com.sap.engine.services.servlets_jsp.server.lib.EmptyEnumeration;
import com.sap.engine.session.AppSession;
import com.sap.tc.logging.Location;

/**
 * The servlet container uses this interface to create a session between an
 * HTTP client and an HTTP server.
 * The session persists for a specified time period, across more than one
 * connection or page request from the user.
 * A session usually corresponds to one user, who may visit a site many times.
 */
public class ApplicationSession extends AppSession implements
    HttpSecureSession, HttpSession, Serializable, Cloneable {
  static final long serialVersionUID = -2261177310183640443L;

  /**
   * Session id
   */
  private String id = null;
  /**
   * True when session is created and not accessed yet.
   */
  private boolean isNew = false;
  /**
   * True if session is in process of invalidation. This flag is needed because
   * during session invalidation session attributes have to be visible in the
   * {@link HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)}
   * method. Otherwise there can appear a loop for re-invalidation.
   */
  private boolean sessionInvalidationStarted = false;
  private boolean sessionInvalidationFinished = false;
  /**
   * Hashtable containing security session values that are accessible only from the Web Container
   */
  private Hashtable secValues = new Hashtable();
  /**
   * ServletContext associated with this ApplicationSession.
   */
  private String contextName = null;
  private boolean isFirst = true;
  // TODO: Remove it when POST params are ready
  private boolean formLoginBodyParameters = false;
  private boolean invalidatedByApplication = false;
  private String debugParameterValue = null;

  /*
   * monitor object used for internal synchronization. As an object exposed to the
   * applications it is dangerous to  synchronize to the ApplicationSession object itself
   */
   private final String invalidationMonitor = new String("InvalidationMonitor");
   
  /**
   * Create new session
   * @param s	id for the session
   * @param timeOut	session timeout in seconds
   * @param clientIP
   * @param contextName
   */
  public ApplicationSession(String s, int timeOut, byte[] clientIP, String contextName) {
    super(s);
    isNew = true;
    super.setMaxInactiveInterval(timeOut);
    sessionInvalidationStarted = false;
    sessionInvalidationFinished = false;
    id = s;
    secValues.put("clientIP", clientIP);
    this.contextName = contextName;
    if (getServletContextFacade() != null) {
      getServletContextFacade().getWebEvents().sessionCreated(this);
    }
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
    	traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "<new>", "MaxInactiveInterval = [" + timeOut + "].");
    }
  }

  /**
   * Indicates the last accessing of the session.
   *
   */
  public void accessed() {
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
    	traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "accessed", "lastAccessed = [" + super.getLastAccessedTime() + "], thisAccessTime = [" + System.currentTimeMillis() + "].");
    }
    super.access(); //may throw IllegalStateException on explicitlyInvalidated session
    isNew = false;
    validate();
  }

  /**
   * Check if the session is still valid and invalidate it if not.
   * Called from the Web container to clear invalid sessions.
   */
  private void validate() {
    if (sessionInvalidationFinished) {
      return;
    }
  	if (!super.isValid()) {
  		invalidateSession();
  	}
  }

  /**
   * Returns the id of the current session
   *
   * @return     The session id
   */
  public String getId() {
    /*
     * In Servlet 2.4 this method must throw InvalidSessionException when called on invalid sessions,
     * but the current implementation conforms to Servlet 2.5 and backwards compatibility only
     * with SAP J2EE Engine 640 only (i.e. Servlet 2.3).
     */
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getId", "");
    }
    return id;
  }

  /**
   * Returns the id of the current session.
   * For internal use to get the session id of invalid sessions,
   * since getId() may throw exception in some cases.
   *
   * @see	#getId() getId
   * @return     The id
   */
  public String getIdInternal() {
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getIdInternal", "");
    }
    return id;
  }

  /**
   * Returns the creation time of the session in milliseconds.
   *
   * @return     Creation time in milliseconds
   * @throws     InvalidSessionException when called on invalid sessions.
   */
  public long getCreationTime() {
  	//Super may throw IllegalStateException; Webcontainer may throw: InvalidSessionException
    //TODO: check why getCreationTime does not call validate();
    long creationTime;
  	try {
	    creationTime = super.getCreationTime();
	    if (super.isValid()) {
	      return creationTime;
	    } else {
	      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[] { "getCreationTime()" });
	    }
  	} catch (IllegalStateException e) {
      if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getCreationTime", "creationTime = unknown, valid = [" + isValid() + "], super.isValid=[" + super.isValid() + "].");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getCreationTime()"});
  	}
  }//getCreationTime()

  /**
   * Returns the session context
   *
   * @return     Session context
   * @deprecated
   */
  public HttpSessionContext getSessionContext() {
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getSessionContext<deprecated>", "result: [null].");
    }
    return null;
  }

  /**
   * Returns last time session was accessed
   *
   * @return     Last accessed time in milliseconds
   * @throws     InvalidSessionException when called on invalid sessions
   * for web applications with version 2.4.
   */
  public long getLastAccessedTime() {
  	//Super does not throw exception, Webcontainer does throw InvalidSessionException
  	long lastAccessedTime = super.getLastAccessedTime();
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getLastAccessedTime", "lastAccessed = [" + lastAccessedTime + "]");
    }
    //Not in Spec. Servlet 2.4, but in API JavaTM 2 Platform Ent. Ed. v1.4
    if (! getServletContextFacade().getWebApplicationConfiguration().isJ2ee13OrLess()) {
      validate();
      if (!super.isValid()) {
        throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getLastAccessedTime()"});
      }
    }
    return lastAccessedTime;
  }

  public long getLastAccessedTimeInternal() {
  	//Super does not throw exception, Webcontainer does throw InvalidSessionException
  	long lastAccessedTime = super.getLastAccessedTime();
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getLastAccessedTimeInternal", "lastAccessed = [" + lastAccessedTime + "]");
    }
    return lastAccessedTime;
  }

  /**
   * Makes the current session invalid.
   *
   * @throws     InvalidSessionException when called on invalid sessions
   */
  public void invalidate() {
    //super.invalidate();
    if (!super.isValid() && sessionInvalidationFinished) {//means already invalidated
      if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "invalidate", "session is not valid!");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Session_already_invalidated);
    }
    invalidatedByApplication = true;
    invalidateSession();
  }

  public void passivate() {
  	Location traceLocation = LogContext.getLocationHttpSessionLifecycle();
    if (traceLocation.beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "passivate", "invalidatedByApplication = [" + invalidatedByApplication + "].");
    }
    ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(contextName.getBytes()));
    if (scf == null) {
      if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE).traceWarning("ASJ.web.000534", "ApplicationSession.passivate [{0}], application [{1}]: session's application [{2}] not started.", new Object[]{id, contextName, contextName}, null, null);				
			}
			super.invalidate();
      return;
    }
    //com.sap.engine.interfaces.resourcecontext.ResourceContext resourceContext = scf.enterResourceContext();
    scf.getSessionServletContext().getSession().removeSession(id);
    //scf.getSessionServletContext().getPolicyDomain().joinSession(this);
    super.invalidate();
    //scf.getWebEvents().sessionWillPassivate(this);
    if (scf.getSessionServletContext().getPolicyDomain().removeMe(this)) {
      if (traceLocation.bePath()) {
        tracePath(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "invalidateSession", "invalidating security session.");
      }
      if (ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted()) {
				ServiceContext.getServiceContext().getWebMonitoring().securitySessionInvalidated(invalidatedByApplication);
      }
    }
    //scf.exitResourceContext(resourceContext);
  }

  /**
   * Makes current session invalid by setting valid to false.
   * To synchronize with supper.s_valid it should be called after checking
   * that super.s_valid == false or super.invalidate() is called.
   *
   * Supports different behavior for previous versions before Servlet 2.4
   * concerning the order of event HttpSessionListener.sessionDestroyed and
   * invalidation.
   */
  private void invalidateSession() {
  	Location traceLocation = LogContext.getLocationHttpSessionLifecycle();
    if (traceLocation.beDebug()) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "invalidateSession",
                "invalidatedByApplication = [" + invalidatedByApplication +
                "], sessionInvalidationFinished = [" + sessionInvalidationFinished + "].");
      }
      synchronized (invalidationMonitor) {
        if (sessionInvalidationFinished || sessionInvalidationStarted) {
          return;
        }
        sessionInvalidationStarted = true;
      }
      ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(contextName.getBytes()));
      if (scf == null) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE).traceWarning("ASJ.web.000533", "ApplicationSession.invalidateSession [{0}], application [{1}]: session's application [{2}] not started.", new Object[]{id, contextName, contextName}, null, null);
        }
		super.invalidate();
        throw new WebIllegalStateException(WebIllegalStateException.CANNOT_INVALIDATE_SESSION_APPP_CONTEXT_NOT_FOUND, new Object[]{getIdInternal(), contextName});
      }

      ResourceContext resourceContext = scf.enterResourceContext();
      //scf.getSessionServletContext().getSession().invalidateSession(this);

      if (ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted()) {
        ServiceContext.getServiceContext().getWebMonitoring().sessionInvalidated(invalidatedByApplication);
      }

      /* TODO: Remove it but check if some change in logic is required
      scf.getSessionServletContext().getPolicyDomain().joinSession(this);
      */

      //Since 2.4 HttpSessionListener.sessionDestroyed must be invoked BEFORE the http session is invalidated:
      if (! getServletContextFacade().getWebApplicationConfiguration().isJ2ee13OrLess()) {
        // HttpSession.sessionDestroyed() Servlet 2.4 new definition of the event
        try {
          scf.getWebEvents().sessionDestroyed(this, invalidatedByApplication);
  
          //remove session attributes
          for(String name: attributeNames()) {
            Object obj = super.getAttribute(name);
            // Here the object is no longer available via the getAttribute method,
            // which uses validate() for versions 2.4 and here as by SPEC.2.4
            scf.getWebEvents().sessionValueUnbound(name, obj, this, invalidatedByApplication);
            scf.getWebEvents().sessionAttributeRemoved(name, obj, this, invalidatedByApplication);
          }
        }
        finally {
          super.invalidate();
        }
      } else {
        //remove session attributes
        for(String name: attributeNames()) {
          Object obj = super.getAttribute(name);
//          //TODO: check whether here the object may still be available via the getAttribute method
//          //and whether a fix is needed for versions earlier than 2.4:
          scf.getWebEvents().sessionValueUnbound(name, obj, this, invalidatedByApplication);
          scf.getWebEvents().sessionAttributeRemoved(name, obj, this, invalidatedByApplication);
        }
        super.invalidate();

        //Before 2.4 HttpSessionListener.sessionDestroyed must be invoked AFTER the http session is invalidated:
        scf.getWebEvents().sessionDestroyed(this, invalidatedByApplication);
      }

      if (scf.getSessionServletContext().getPolicyDomain().removeMe(this)) {
        if (traceLocation.bePath()) {
          tracePath(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "invalidateSession", "invalidating security session.");
        }
        if (ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted()) {
          ServiceContext.getServiceContext().getWebMonitoring().securitySessionInvalidated(invalidatedByApplication);
        }
      }
      scf.exitResourceContext(resourceContext);
      if (ServiceContext.getServiceContext().getHttpSessionDebugListener() != null
       && ServiceContext.getServiceContext().getDebugRequestParameterName() != null
          && ServiceContext.getServiceContext().getDebugRequestParameterName().length() != 0 && debugParameterValue != null) {
            ServiceContext.getServiceContext().getHttpSessionDebugListener().endSession(id);
      }
      synchronized (invalidationMonitor) {
        sessionInvalidationStarted = false; // reset flag back
        sessionInvalidationFinished = true;
      }
  }

  /**
   * Checks if the current session is new
   *
   * @return     true if session is new
   * @throws     InvalidSessionException when called on invalid sessions
   */
  public boolean isNew() {
    validate();

    if (!isValid()) {
      if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "isNew", "session is not valid!");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"isNew()"});
    }
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "isNew", "" + (isNew));
    }
    return isNew;
  }


  protected void invalidated() {
  }

  /**
   * Puts Object in current session
   *
   * @param   name      Object name
   * @param   attribute    Object value
   * @deprecated
   */
  public void putValue(String name, Object attribute) {
    setAttribute(name, attribute);
  }

  /**
   * Gets Object from session
   *
   * @param   name  Object name
   * @return     Object value
   * @deprecated
   */
  public Object getValue(String name) {
    return getAttribute(name);
  }

  /**
   * Returns names of all Objects containing in current session
   *
   * @return     Array of all Object names
   * @deprecated
   */
  public String[] getValueNames() {
    String as[] = new String[attributeNames().size()];
    int i = 0;

    for (String name: attributeNames()) {
      as[i++] = name;
    }
    if (LogContext.getLocationHttpSessionAttributes().beDebug()) {
      String result = "";
      for (String a : as) {
        result += a + ", ";
      }
      if (result.endsWith(", ")) {
        result = result.substring(0, result.length() - 2);
      }
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getValueNames", "return: [" + result + "]");
    }
    return as;
  }

  /**
   * Removes Object from session
   *
   * @param   name  Name of the Object
   * @deprecated
   */
  public void removeValue(String name) {
  	removeAttribute(name);
  }

  /**
   * Sets the maximum of the inactive interval for this session
   *
   * @param   i  interval in seconds
   * @throws     InvalidSessionException when called on invalid sessions
   */
  public void setMaxInactiveInterval(int i) {
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "setMaxInactiveInterval", "inactive Interval = [" + i + "], valid = [" + isValid() + "].");
    }
    //TODO:in Servlet2.4 spec, neither in API JavaTM 2 Platform Ent. Ed. v1.4 there is no throw exception, but it does.
    if (!isValid()) {
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"setMaxInactiveInterval()"});
    } else {
      super.setMaxInactiveInterval(i);
    }
  }

  /**
   * Gets maximum inactive interval for this session
   *
   * @return     interval in seconds
   * @throws     InvalidSessionException when called on invalid sessions
   */
  public int getMaxInactiveInterval() {
    validate();
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getMaxInactiveInterval", "inactiveInterval = [" + super.getMaxInactiveInterval() + "], valid = [" + isValid() + "]");
    }
    //TODO:in Servlet2.4 spec, neither in API JavaTM 2 Platform Ent. Ed. v1.4 there is no throw exception, but it does.
    if (!isValid()) {
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getMaxInactiveInterval()"});
    } else {
    	return super.getMaxInactiveInterval();
    }
  }

  /**
   * Gets attribute from session
   *
   * @param   name  Name of the attribute
   * @return	Attribute value
   * @throws	InvalidSessionException when called on invalid sessions
   */
  public Object getAttribute(String name) {
    validate();

    boolean beDebug = LogContext.getLocationHttpSessionAttributes().beDebug();
    if (!isValid()) {
      if (beDebug) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getAttribute", "name = [" + name + "], session is not valid!.");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getAttribute()"});
    }

    if (name == null) {
      if (beDebug) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getAttribute", "name = [" + name + "], return: [null].");
      }
      return null;
    } else {
      Object result = super.getAttribute(name);
      if (beDebug) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getAttribute", "name = [" + name + "], return: [" + result + "].");
      }
      return result;
    }
  }

  /**
   * Gets all names for the attributes contained in session
   *
   * @return	array of names of attributes
   * @throws	InvalidSessionException when called on invalid sessions
   */
  public Enumeration getAttributeNames() {
    validate();

    if (!isValid()) {
      if (LogContext.getLocationHttpSessionAttributes().beDebug()) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getAttributeNames", "session is not valid!.");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getAttributeNames()"});
    }
    if (LogContext.getLocationHttpSessionAttributes().beDebug()) {
      String result = "";
      for (String name: attributeNames()) {
        result += name + ", ";
      }
      if (result.endsWith(", ")) {
        result = result.substring(0, result.length() - 2);
      }
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getAttributeNames", "result = [" + result + "].");
    }

    synchronized (attributeNames()) {
      int count = attributeNames().size();

      if (count == 0) {
        return new EmptyEnumeration();
      }

      HashSet<String> set = new HashSet<String>();

      for (String name: attributeNames()) {
        set.add(name);
      }

      return Collections.enumeration(set);
    }
  }

  /**
   * Sets an attribute into session
   *
   * @param   name        name of the attribute
   * @param   attribute    attribute value
   * @throws	InvalidSessionException when called on invalid sessions
   */
  public void setAttribute(String name, Object attribute) {
    Boolean beDebug = LogContext.getLocationHttpSessionAttributes().beDebug();
  	if (beDebug) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "setAttribute", "name = [" + name + "], attribute = [" + attribute + "].");
    }
    validate();
    if (!isValid()) {
      if (beDebug) {
        traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "setAttribute", "session is not valid!.");
      }
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"setAttribute()"});
    }

    if (name == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Method_called_with_null_value_name, new Object[]{"setAttribute()"});
    }

    if (attribute == null) {
      removeAttribute(name);
      return;
    }

    //By SRV.2.4 and same in SRV.2.3 call the valueBound first:
    ApplicationContext scf = getServletContextFacade();
    if (scf != null) {
      scf.getWebEvents().sessionValueBound(name, attribute, this);
    }

    //Begin: Make the new object available via the getAttribute method
    Object oldObj = null;
    oldObj = super.getAttribute(name);
    if (beDebug) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "setAttribute", "old value = [" + oldObj + "].");
    }
    super.setAttribute(name, attribute);
    //End  : Make the new object available via the getAttribute method

    if (scf != null) {
      if (oldObj != null) { //object is replaced
        scf.getWebEvents().sessionAttributeReplaced(name, oldObj, this);
        scf.getWebEvents().sessionValueUnbound(name, oldObj, this, invalidatedByApplication);
      } else {
        scf.getWebEvents().sessionAttributeAdded(name, attribute, this);
      }
    }
  }

  /**
   * Remove attribute from session
   *
   * @param   name  Attribute name
   * @throws     InvalidSessionException when called on invalid sessions
   * @throws     WebIllegalArgumentException when name is null
   */
  public void removeAttribute(String name) {
    //TODO: in Servlet 2.4 no exception is thrown
    if (LogContext.getLocationHttpSessionAttributes().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "removeAttribute", "name = [" + name + "], valid = [" + isValid() + "].");
    }
    validate();

    if (!isValid()) {
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"removeAttribute()"});
    }

    if (name == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Method_called_with_null_value_name, new Object[]{"removeAttribute()"});
    }

    Object obj = getRemoveAttribute(name);
    if (LogContext.getLocationHttpSessionAttributes().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "removeAttribute", "value = [" + obj + "]");
    }
    ApplicationContext scf = getServletContextFacade();
    if (scf != null) {
      scf.getWebEvents().sessionValueUnbound(name, obj, this, invalidatedByApplication);
      scf.getWebEvents().sessionAttributeRemoved(name, obj, this, invalidatedByApplication);
    }
  }

  /**
   * @deprecated As of NY, this method is replaced by
   * {@link #setSecurityAttribute}.
   * @param name
   * name of the security value
   * @param value
   * the security value
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public void putSecurityValue(String name, Object value) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "putSecurityValue", "name = [" + name + "], value = [" + value + "].");
    }
    setSecurityAttribute(name, value);
  }

  /**
   * @deprecated As of NY, this method is replaced by
   * {@link #removeSecurityAttribute}.
   * @param name
   * name of the security value
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public Object removeSecurityValue(String name) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "removeSecurityValue", "name = [" + name + "], valid = [" + isValid() + "].");
    }
    return removeSecurityAttribute(name);
  }

  /**
   * @deprecated As of NY, this method is replaced by
   * {@link #getSecurityAttribute}.
   * @param name
   * name of the security value
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public Object getSecurityValue(String name) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
       traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES,
         "getSecurityValue", "name = [" + name +
         "], valid = [" + isValid() + "].");
    }
    return getSecurityAttribute(name);
  }

  /**
   * Binds an security object to this session, using the name specified. If an
   * object of the same name is already bound to the session, the object is
   * replaced.
   * <p>
   * If the value passed in is <code>null</code>, this has the same effect as
   * calling <code>removeAttribute()</code>.
   *
   * @param name
   * the name to which the object is bound; cannot be null
   * @param value
   * the object to be bound
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public void setSecurityAttribute(String name, Object value) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES,
        "setSecurityAttribute()", "name = [" + name +
        "], value = [" + value + "].");
    }

    validate();
    if (!isValid()) {
      throw new WebInvalidSessionException(
        WebInvalidSessionException.Method_called_on_invalid_session,
        new Object[] { "setSecurityAttribute()" });
    }

    // TODO: Check if this is necessary?
    if (name == null) {
      throw new WebIllegalArgumentException(
        WebIllegalArgumentException.Method_called_with_null_value_name,
        new Object[] { "setSecurityAttribute()" });
    }

    // If value is null removes the attribute
    if (value == null) {
      secValues.remove(name);
    }

    secValues.put(name, value);
  }

  /**
   * Returns the security <code>Object</code> bound with the specified name in
   * this session, otherwise <code>null</code>.
   *
   * @param name
   * a <code>String</code> specifying the name of the object
   * @return the security <code>Object</code> with the specified name
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public Object getSecurityAttribute(String name) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES,
          "getSecurityAttribute()", "name = [" + name + "].");
    }

    validate();
    if (!isValid()) {
      throw new WebInvalidSessionException(
        WebInvalidSessionException.Method_called_on_invalid_session,
        new Object[] { "getSecurityAttribute()" });
    }

    if (name == null) {
      throw new WebIllegalArgumentException(
        WebIllegalArgumentException.Method_called_with_null_value_name,
        new Object[] { "getSecurityAttribute()" });
    } else if (name.equals("userId")) {
      return secValues.get(name);
    } else if (name.equals("clientIP")) {
      return new String(ParseUtils.inetAddressByteToString(
        (byte[]) secValues.get(name)));
    } else {
      return secValues.get(name);
    }
  }

  /**
   * Returns an <code>Enumeration</code> of <code>String</code> objects
   * containing the names of all the security objects bound to this session.
   *
   * @return an <code>Enumeration</code> of <code>String</code> objects
   * specifying the names of all the security objects bound to this session
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public Enumeration getSecurityAttributeNames() {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES,
          "getSecurityAttributeNames()", null);
    }

    validate();
    if (!isValid()) {
      throw new WebInvalidSessionException(
        WebInvalidSessionException.Method_called_on_invalid_session,
        new Object[] { "getSecurityAttributeNames()" });
    }

    return secValues.keys();
  }

  /**
   * Removes the security object bound with the specified name from this
   * session. If the session does not have an object bound with the specified
   * name, this method does nothing.
   *
   * @param name
   * the name of the object to remove from this session
   * @exception IllegalStateException
   * if this method is called on an invalidated session
   */
  public Object removeSecurityAttribute(String name) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES,
          "removeSecurityAttribute()", "name = [" + name + "].");
    }

    validate();
    if (!isValid()) {
      throw new WebInvalidSessionException(
        WebInvalidSessionException.Method_called_on_invalid_session,
        new Object[] { "removeSecurityAttribute()" });
    }

    if (name == null) {
      throw new WebIllegalArgumentException(
        WebIllegalArgumentException.Method_called_with_null_value_name,
        new Object[] { "removeSecurityAttribute()" });
    }

    return secValues.remove(name);
  }

  public String getSecurityValueNoAccess(String s) {
    if (LogContext.getLocationHttpSessionAttributes().beInfo()) {
      traceInfo(LogContext.LOCATION_HTTP_SESSION_ATTRIBUTES, "getSecurityValueNoAccess", "name = [" + s + "], valid = [" + isValid() + "].");
    }
    if (!isValid()) {
      throw new WebInvalidSessionException(WebInvalidSessionException.Method_called_on_invalid_session, new Object[]{"getSecurityValueNoAccess()"});
    }
    if (s == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Method_called_with_null_value_name, new Object[]{"getSecurityValueNoAccess()"});
    } else if (s.equals("clientIP")) {
      return new String(ParseUtils.inetAddressByteToString((byte[]) secValues.get(s)));
    } else {
      return (String) secValues.get(s);
    }
  }

  // TODO: Remove it when POST params are ready
  public void setFormLoginBodyParameters(boolean formLoginBodyParameters) {
    this.formLoginBodyParameters = formLoginBodyParameters;
  }

  // TODO: Remove it when POST params are ready
  public boolean isFormLoginBodyParameters() {
    return formLoginBodyParameters;
  }

  public void setDebugParameterValue(String debugParameterValue) {
    this.debugParameterValue = debugParameterValue;
  }

  public String getDebugParameterValue() {
    return debugParameterValue;
  }

  //servlet2.3
  public ServletContext getServletContext() {
    ServletContext result = getServletContextFacade().getServletContext();
    if (LogContext.getLocationHttpSessionLifecycle().beDebug()) {
      traceDebug(LogContext.LOCATION_HTTP_SESSION_LIFECYCLE, "getServletContext", "result: [" + result + "].");
    }
    return result;
  }

  private ApplicationContext getServletContextFacade() {
    MessageBytes contextNameMB = null;
    char[] ch = contextName.toCharArray();
    byte[] b = new byte[ch.length];
    for (int i = 0; i < ch.length; i++) {
      b[i] = (byte) ch[i];
    }
    contextNameMB = new MessageBytes(b);
    return ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(contextNameMB);
  }

  public boolean isFirst() {
    return isFirst;
  }

  public void setFirst() {
    isFirst = false;
  }

  /**
   * Return a string representation of this object.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("ApplicationSession[id=");
    sb.append(id);
    sb.append("]");
    return (sb.toString());
  }

  private void traceWarning(String msgId, int location, String method, String msg, String dcName, String csnComponent) {
    LogContext.getLocation(location).traceWarning( msgId, "ApplicationSession." + method + " [" + id + "], application [" + contextName + "]: " + msg, dcName, csnComponent);
  }

  private void traceInfo(int location, String method, String msg) {
    LogContext.getLocation(location).traceInfo("ApplicationSession." + method + " [" + id + "], application [" + contextName + "]: " + msg, contextName);
  }

  private void traceDebug(int location, String method, String msg) {
    LogContext.getLocation(location).trace("ApplicationSession." + method + " [" + id + "], application [" + contextName + "]: " + msg, contextName);
  }

  private void tracePath(int location, String method, String msg) {
    LogContext.getLocation(location).tracePath("ApplicationSession." + method + " [" + id + "], application [" + contextName + "]: " + msg, contextName);
  }
}
