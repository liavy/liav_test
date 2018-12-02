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
package com.sap.engine.services.servlets_jsp.server.application;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.session.util.SessionEnumeration;
import com.sap.tc.logging.Location;

public class WebEvents {
  private static Location currentLocation = Location.getLocation(WebEvents.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
  //todo - check classloaders
  private ServletContext servletContext = null;
  private WebComponents webComponents = null;
  private ServletContextEvent servletContextEvent = null;
  private String aliasName = null;
  private ApplicationContext applicationContext = null;

  /**
   * A flag indicating an exception thrown by listener's code.
   */
  private boolean eventListenerError = false;

  private String listenerExceptionMessage = null;

  private String unhandledExceptionMessage = null;
  private Throwable unhandledException = null;
  private Object synchronousMonitor = new Object();
  private Object asynchronousMonitor = new Object();

  public WebEvents(String aliasName, ServletContext servletContext, ApplicationContext applicationContext, WebComponents webComponents) {
    this.aliasName = aliasName;
    this.servletContext = servletContext;
    this.applicationContext = applicationContext;
    this.webComponents = webComponents;
    this.servletContextEvent = new ServletContextEvent(servletContext);
  }

  //servlet context

  /**
   * Notification that the web application is ready to process requests.
   *
   */
  public void contextInitialized(ApplicationContext applicationContext) {
    ServletContextListener[] listeners = webComponents.getServletContextListeners();
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.contextInitialized", listeners[0].getClass());
      }//ACCOUNTING.start - END
    
      for (int i = 0; listeners != null && i < listeners.length; i++) {
        try {
          listeners[i].contextInitialized(servletContextEvent);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	 // TODO:Polly 1 trace and 1 log
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000368",
            "Cannot notify listener [{0}] that [{1}] web application initialization process is starting.", 
            new Object[]{listeners[i].getClass(), aliasName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000464",
              "Error occurred in invoking event \"contextInitialized()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          }
          if (!applicationContext.getWebApplicationConfiguration().isJ2ee13OrLess()) {
            synchronized (synchronousMonitor) {
              if (getUnhandledException() == null) {
                setEventListenerError();
                setUnhandledException(e);
                setUnhandledExceptionMessage("Error occurred in invoking event \"contextInitialized()\" on listener " + listeners[i].getClass() + ".");
              }
            }
          }
        }
      } //for
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.contextInitialized");
      }//ACCOUNTING.end - END
    }
  }//contextInitialized

  /**
   * Notification that the servlet context is about to be shut down.
   *
   */
  public void contextDestroyed() {
    ServletContextListener[] listeners = webComponents.getServletContextListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.contextDestroyed", listeners[0].getClass());
      }//ACCOUNTING.start - END
      
      for (int i = listeners.length - 1; i >= 0; i--) {
        try {
          listeners[i].contextDestroyed(servletContextEvent);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly 1trace + 1log
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000369",
            "Cannot notify listener [{0}] that servlet context of [{1}]web application is about to be destroyed.", new Object[]{listeners[i].getClass(), aliasName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000465",
              "Error occurred in invoking event \"contextDestroyed()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.contextDestroyed");
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Notification that a new attribute was added to the servlet context.
   *
   * @param   attributeName  name of the attribute
   * @param   attributeValue  attribute's object
   */
  public void contextAttributeReplaced(String attributeName, Object attributeValue) {
    ServletContextAttributeListener[] listeners = webComponents.getServletContextAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.contextAttributeReplaced", WebEvents.class);
      }//ACCOUNTING.start - END
      
      ServletContextAttributeEvent servletContextAttributeEvent = new ServletContextAttributeEvent(servletContext, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeReplaced()\" on listener ";
      synchronized (asynchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null){
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                "The attribute is " + attributeName + " with value " + attributeValue + ".");
              listeners[i].attributeReplaced(servletContextAttributeEvent);
             
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].attributeReplaced(servletContextAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            	//TODO:polly 1trace + 1log
  //add web application name
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000370",
                "Cannot notify listener [{0}] that an attribute on the server context has been replaced. The attribute [{1}] is with value [{2}].", new Object[]{listeners[i].getClass(), attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000466",
                  "Error occurred in invoking event \"attributeReplaced()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.contextAttributeReplaced");
      }//ACCOUNTING.end - END
    }
  }//contextAttributeReplaced

  /**
   * Notification that a new attribute was added to the servlet context.
   *
   * @param   attributeName  name of the attribute
   * @param   attributeValue  attribute's object
   */
  public void contextAttributeAdded(String attributeName, Object attributeValue) {
    ServletContextAttributeListener[] listeners = webComponents.getServletContextAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.contextAttributeAdded", WebEvents.class);
      }//ACCOUNTING.start - END
      
      ServletContextAttributeEvent servletContextAttributeEvent = new ServletContextAttributeEvent(servletContext, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeAdded()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null){
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                "The attribute is " + attributeName + " with value " +  attributeValue + ".");
              listeners[i].attributeAdded(servletContextAttributeEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].attributeAdded(servletContextAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            //TODO:Polly 1trace 1log
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000371", "Cannot notify listener [{0}] that a new attribute was added to the servlet context. "+
                "The attribute is [{1}] with value [{2}].", new Object[]{listeners[i].getClass(), attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000467",
                  "Error occurred in invoking event \"attributeAdded()\" on listener {0}. The attribute is {1} with value {2}.", new Object[]{listeners[i].getClass(), attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.contextAttributeAdded");
      }//ACCOUNTING.end - END
    }
  }//contextAttributeAdded

  /**
   * Notification that an existing attribute has been removed from the servlet context.
   *
   * @param   attributeName  name of the attribute
   * @param   attributeValue  attribute's object
   */
  public void contextAttributeRemoved(String attributeName, Object attributeValue) {
    ServletContextAttributeListener[] listeners = webComponents.getServletContextAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.contextAttributeRemoved", WebEvents.class);
      }//ACCOUNTING.start - END
      
      ServletContextAttributeEvent servletContextAttributeEvent = new ServletContextAttributeEvent(servletContext, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeRemoved()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null){
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
              "The attribute is " + attributeName + " with value " +  attributeValue + ".");
              listeners[i].attributeRemoved(servletContextAttributeEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].attributeRemoved(servletContextAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            	//TODO:Polly 1trace + 1log
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000372", "Cannot notify listener [{0}] that a new attribute was removed from the servlet context of [{1}] web application." +
                "The attribute is [{2}] with value [{3}].", new Object[]{listeners[i].getClass(), aliasName, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000468",
                  "Error occurred in invoking event \"attributeRemoved()\" on listener  {0}. The attribute is {1} with value {2}.", new Object[]{listeners[i].getClass(), attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.contextAttributeRemoved");
      }//ACCOUNTING.end - END
    }
  }//contextAttributeRemoved

  //http session

  /**
   * Notifies all HttpSessionListener objects that session has been created.
   *
   */
  public void sessionCreated(ApplicationSession applicationSession) {
    HttpSessionListener[] listeners = webComponents.getHttpSessionListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.sessionCreated", WebEvents.class);
      }//ACCOUNTING.start - END
      HttpSessionEvent httpSessionEvent = new HttpSessionEvent(applicationSession);
      String message = "Error occurred in invoking event \"sessionCreated()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                "The http session is " + applicationSession + ".");
              listeners[i].sessionCreated(httpSessionEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].sessionCreated(httpSessionEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            	//TODO:Polly 1trace + 1log
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000373", "Cannot notify listener [{0}] that a session was created." +
                "The HTTP session is [{1}].", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000469",
                  "Error occurred in invoking event \"sessionCreated()\" on listener {0}.The HTTP session is {1}.", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
  
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.sessionCreated");
      }//ACCOUNTING.end - END
    }
  }//sessionCreated

  /**
   * Notifies all HttpSessionListener objects that session has been destroyed.
   *
   */
  public void sessionDestroyed(ApplicationSession applicationSession, boolean invalidatedByApplication) {
    HttpSessionListener[] listeners = webComponents.getHttpSessionListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.sessionDestroyed", WebEvents.class);
      }//ACCOUNTING.start - END
      HttpSessionEvent httpSessionEvent = new HttpSessionEvent(applicationSession);
      String message = "Error occurred in invoking event \"sessionDestroyed()\" on listener ";
      if (invalidatedByApplication) {
        synchronized (synchronousMonitor) {
          if (!isEventListenerError() && getListenerExceptionMessage() == null) {
            setEventListenerError();
            for (int i = 0; i < listeners.length; i++) {
              try {
                setListenerExceptionMessage(message +  listeners[i].getClass() + ". " +
                "The HTTP session is " + applicationSession + ".");
                listeners[i].sessionDestroyed(httpSessionEvent);
                clearListenerExceptionMessage();
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              }
            }
            clearEventListenerError();
          } else if (isEventListenerError()) {
            if (getListenerExceptionMessage() != null &&
              !getListenerExceptionMessage().startsWith(message)) {
              int i = 0;
              try {
                for (i = 0; i < listeners.length; i++) {
                  listeners[i].sessionDestroyed(httpSessionEvent);
                }
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              } catch (Throwable e) {
            	  //TODO:Polly trace
                LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation,"ASJ.web.000374", "Cannot notify listener [{0}] that a session is about to be invalidated." +
                  "The HTTP session is {1}.", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
                if (traceLocation.beWarning()) {
                  LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000470",
                    "Error occurred in invoking event \"sessionDestroyed()\" on listener {0}.The HTTP session is {1}.", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
                }
              }
            }
          }
        }
      } else {
        int i = 0;
        try {
          for (i = 0; i < listeners.length; i++) {
            listeners[i].sessionDestroyed(httpSessionEvent);
          }
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly 1trace + 1 log
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000375", "Cannot notify listener [{0}] that a session is about to be invalidated. " +
            "The HTTP session is [{1}].", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000471",
              "Error occurred in invoking event \"sessionDestroyed()\" on listener {0}.The HTTP session is {1}.", new Object[]{listeners[i].getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          }
        }
  
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.sessionDestroyed");
      }//ACCOUNTING.end - END
    }
  }//sessionDestroyed

  public void sessionsDidActivate(Enumeration sessEn) {
    boolean isListenerException = false;
    while ( sessEn.hasMoreElements() != isListenerException) {
      ApplicationSession applicationSession = (ApplicationSession) sessEn.nextElement();
      try {
        if (applicationSession.isValid()) {
          isListenerException = sessionDidActivate(applicationSession);
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly 1trace + 1log
    	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000376", "Error occurred during notification that [{0}] session has just been activated.", new Object[]{applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  if (traceLocation.beWarning()) {
    		  LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000472",
    		    "Error occurred in invoking event \"sessionDidActivate()\" about http session {0}.", new Object[]{applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  }
      }
    }
  }

  public void sessionsWillPassivate(SessionEnumeration sessions) {
    boolean isListenerException = false;
    while( sessions.hasMoreElements() && !isListenerException) {
      ApplicationSession applicationSession = (ApplicationSession) sessions.nextElement();
      try {
        if (applicationSession.isValid()) {
          isListenerException = sessionPassivate(applicationSession);
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly 1log and 1trace
    	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000377", "Error occurred during notification that [{0}]session is about to be passivated.", new Object[]{applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  if (traceLocation.beWarning()) {
    		  LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000473", 
    		    "Error occurred in invoking event \"sessionsWillPassivate()\" about http session {0}.", new Object[]{applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  }
      }
    }
  }

  /**
   * Notifies all HttpSessionActivationListener objects (bounded to session)
   * that session has been activated.
   *
   */
  private boolean sessionDidActivate(ApplicationSession applicationSession) {
    HttpSessionEvent httpSessionEvent = new HttpSessionEvent(applicationSession);
    boolean isListenerException = false;
    for (Enumeration attributeNames = applicationSession.getAttributeNames(); attributeNames.hasMoreElements() && !isListenerException;) {
      Object obj = applicationSession.getAttribute((String)attributeNames.nextElement());
      if (obj instanceof HttpSessionActivationListener) {
        HttpSessionActivationListener listener = (HttpSessionActivationListener) obj;
        try {
          listener.sessionDidActivate(httpSessionEvent);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly 1trace +1log
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000378", "Cannot notify listener [{0}] that [{1}] session has just been activated." +
           "The HTTP session is {2}.", new Object[]{listener.getClass(), applicationSession, applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000474",
              "Error occurred in invoking event \"sessionDidActivate()\" on listener {0}.The HTTP session is {1}.", new Object[]{listener.getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
        	}
        	synchronized (asynchronousMonitor) {
        	  isListenerException = true;
        	  if (getUnhandledException() == null) {
        	    setEventListenerError();
        	    setUnhandledException(e);
        	    setUnhandledExceptionMessage(
        	      "Error occurred in invoking event \"sessionDidActivate()\" on listener " + listener.getClass() + ". " +
        	      "The HTTP session is " + applicationSession + ".");
        	  }
        	}
        }
      }
    }
    return isListenerException;
  }

  /**
   * Notifies all HttpSessionActivationListener objects (bounded to session)
   * that session is about to be passivated.
   *
   */
  public void sessionWillPassivate(ApplicationSession applicationSession) {
    sessionPassivate(applicationSession);
  }

  private boolean sessionPassivate(ApplicationSession applicationSession) {
    HttpSessionEvent httpSessionEvent = new HttpSessionEvent(applicationSession);
    boolean isListenerException = false;
    for (Enumeration attributeNames = applicationSession.getAttributeNames(); attributeNames.hasMoreElements() && !isListenerException;) {
      Object obj = applicationSession.getAttribute((String)attributeNames.nextElement());
      if (obj instanceof HttpSessionActivationListener) {
        HttpSessionActivationListener listener = (HttpSessionActivationListener) obj;
        try {
          listener.sessionWillPassivate(httpSessionEvent);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly 1trace + 1 log
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000379", "Cannot notify listener [{0}] that [{1}] session is about to be passivated. " +
            "The HTTP session is {2}.", new Object[]{listener.getClass(), applicationSession, applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000475",
              "Error occurred in invoking event \"sessionWillPassivate()\" on listener {0}.The HTTP session is {1}.", new Object[]{listener.getClass(), applicationSession}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          }
          synchronized (asynchronousMonitor) {
            isListenerException = true;
            if (getUnhandledException() == null) {
              setEventListenerError();
              setUnhandledException(e);
              setUnhandledExceptionMessage("Error occurred in invoking event \"sessionWillPassivate()\" on listener " + listener.getClass() + ". " +
                "The HTTP session is " + applicationSession + ".");
            }
          }
        }
      }
    }
    return isListenerException;
  }

  /**
   * Notification that an attribute has been replaced in this session.
   *
   */
  public void sessionAttributeReplaced(String attributeName, Object attributeValue, ApplicationSession applicationSession) {
    HttpSessionAttributeListener[] listeners = webComponents.getHttpSessionAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.sessionAttributeReplaced", WebEvents.class);
      }//ACCOUNTING.start - END
      HttpSessionBindingEvent httpSessionBindingEvent = new HttpSessionBindingEvent(applicationSession, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeReplaced()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                "The http session is " + applicationSession + ". The attribute is " + attributeName + " with value " + attributeValue + ".");
              listeners[i].attributeReplaced(httpSessionBindingEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].attributeReplaced(httpSessionBindingEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
          	  //TODO:Polly trace
            	//add log message more readable
            	//Check API
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000380",
                "Cannot notify listener [{0}] that an attribute has been replaced in [{1}] session. The attribute is [{2}] with value [{3}].", new Object[]{listeners[i].getClass(), applicationSession, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000476",
                  "Error occurred in invoking event \"attributeReplaced()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.sessionAttributeReplaced");
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Notification that an attribute has been added to this session.
   *
   */
  public void sessionAttributeAdded(String attributeName, Object attributeValue, ApplicationSession applicationSession) {
    HttpSessionAttributeListener[] listeners = webComponents.getHttpSessionAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.sessionAttributeAdded", WebEvents.class);
      }//ACCOUNTING.start - END
      HttpSessionBindingEvent httpSessionBindingEvent = new HttpSessionBindingEvent(applicationSession, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeAdded()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                "The HTTP session is " + applicationSession + ". The attribute is " + attributeName + " with value " + attributeValue + ".");
              listeners[i].attributeAdded(httpSessionBindingEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; i < listeners.length; i++) {
                listeners[i].attributeAdded(httpSessionBindingEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            	//TODO:Polly
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000381",
                "Cannot notify listener [{0}] that an attribute on the server context has been added to [{1}] session. The attribute is [{2}] with value [{3}].", new Object[]{listeners[i].getClass(), applicationSession, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000477",
                  "{0} {1}.The HTTP session is {2}. The attribute is {3} with value {4}.", new Object[]{message, listeners[i].getClass(), applicationSession, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.sessionAttributeAdded");
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Notification that an attribute has been removed from this session.
   *
   */
  public void sessionAttributeRemoved(String attributeName, Object attributeValue, ApplicationSession applicationSession, boolean invalidatedByApplication) {
    HttpSessionAttributeListener[] listeners = webComponents.getHttpSessionAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.sessionAttributeRemoved", WebEvents.class);
      }//ACCOUNTING.start - END
      HttpSessionBindingEvent httpSessionBindingEvent = new HttpSessionBindingEvent(applicationSession, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeRemoved()\" on listener ";
      if (invalidatedByApplication) {
        synchronized (synchronousMonitor) {
          if (!isEventListenerError() && getListenerExceptionMessage() == null) {
            setEventListenerError();
            for (int i = 0; i < listeners.length; i++) {
              try {
                setListenerExceptionMessage(message + listeners[i].getClass() + ". " +
                  "The HTTP session is " + applicationSession + ". The attribute is " + attributeName + " with value " + attributeValue + ".");
                listeners[i].attributeRemoved(httpSessionBindingEvent);
                clearListenerExceptionMessage();
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              }
            }
            clearEventListenerError();
          } else if (isEventListenerError()) {
            if (getListenerExceptionMessage() != null &&
              !getListenerExceptionMessage().startsWith(message)) {
              int i = 0;
              try {
                for (i = 0; i < listeners.length; i++) {
                  listeners[i].attributeRemoved(httpSessionBindingEvent);
                }
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              } catch (Throwable e) {
            	  //TODO:Polly
            	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation,  "ASJ.web.000382", "Cannot notify listener [{0}] that an attribute has been removed from  [{1}] session. " +
            	    "The attribute name is [{2}].", new Object[]{listeners[i].getClass(), applicationSession, attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	  if (traceLocation.beWarning()) {
            	    LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000478",
            	      "Error occurred in invoking event \"attributeRemoved()\" on listener {0}. " +
            	      "The HTTP session is {1}. The attribute is {2} with value {3}.", new Object[]{listeners[i].getClass(), applicationSession, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	  }
              }
            }
          }
        }
      } else {
        int i = 0;
        try {
          for (i = 0; i < listeners.length; i++) {
            listeners[i].attributeRemoved(httpSessionBindingEvent);
          }
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly
      	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000383", "Cannot notify listener [{0}] that an attribute has been removed from  [{1}] session. " +
      	    "The attribute name is [{2}].", new Object[]{listeners[i].getClass(), applicationSession, attributeName}, e, null, null);
      	  if (traceLocation.beWarning()) {
      	    LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000479",
      	      "Error occurred in invoking event \"attributeRemoved()\" on listener {0}. " +
      	      "The HTTP session is {1}. The attribute is {2} with value {3}.", new Object[]{listeners[i].getClass(), applicationSession, attributeName, attributeValue}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
      	  }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.sessionAttributeRemoved");
      }//ACCOUNTING.end - END
    }
  }

  //http session attributes
  public void sessionValueBound(String attributeName, Object bindingListener, ApplicationSession session) {
    if (bindingListener instanceof HttpSessionBindingListener) {
      sessionValueBound(attributeName, (HttpSessionBindingListener) bindingListener, session);
    }
  }

  private void sessionValueBound(String attributeName, HttpSessionBindingListener bindingListener, ApplicationSession session) {
    String message = "Error occurred in invoking event \"valueBound()\" on listener " + bindingListener.getClass() + ". ";
    synchronized (synchronousMonitor) {
      if (!isEventListenerError() && getListenerExceptionMessage() == null) {
        setEventListenerError();
        try {
          setListenerExceptionMessage(message +
            "The HTTP session is " + session + ". The attribute is " + attributeName + ".");
          bindingListener.valueBound(new HttpSessionBindingEvent(session, attributeName));
          clearListenerExceptionMessage();
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        }
        clearEventListenerError();
      } else if (isEventListenerError()) {
        if (getListenerExceptionMessage() != null &&
          !getListenerExceptionMessage().startsWith(message)) {
           try {
             bindingListener.valueBound(new HttpSessionBindingEvent(session, attributeName));
           } catch (OutOfMemoryError e) {
             throw e;
           } catch (ThreadDeath e) {
             throw e;
           } catch (Throwable e) {
           	 //TODO:Polly
           	 LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000384", "Cannot notify listener [{0}] that an attribute it is being unbound from  [{1}] session." +
            	" The attribute is {2}." , new Object[]{bindingListener.getClass(), session, attributeName},e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            if (traceLocation.beWarning()) {
              LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000480",
                "Error occurred in invoking event \"valueBound()\" on listener {0}. " +
                "The HTTP session is {1}. The attribute is {2}.", new Object[]{bindingListener.getClass(), session, attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            } 
          }
        }
      }
    }
  }

  public void sessionValueUnbound(String attributeName, Object bindingListener, ApplicationSession session, boolean invalidatedByApplication) {
    if (bindingListener instanceof HttpSessionBindingListener) {
      sessionValueUnbound(attributeName, (HttpSessionBindingListener) bindingListener, session, invalidatedByApplication);
    }
  }

  private void sessionValueUnbound(String attributeName, HttpSessionBindingListener bindingListener, ApplicationSession session, boolean invalidatedByApplication) {
    if (invalidatedByApplication){
      String message = "Error occurred in invoking event \"valueUnbound()\" on listener " + bindingListener.getClass() + ". " +
        "The http session is " + session + ". The attribute is " + attributeName + ".";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          try {
            setListenerExceptionMessage(message);
            bindingListener.valueUnbound(new HttpSessionBindingEvent(session, attributeName));
            clearListenerExceptionMessage();
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            try {
              bindingListener.valueUnbound(new HttpSessionBindingEvent(session, attributeName));
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              //TODO:Polly
            	LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000385", "Cannot notify listener [{0}] that an attribute is being unbound from  [{1}] session. " +
    	          "The attribute name is [{2}].", new Object[]{bindingListener.getClass(), session, attributeName }, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	if (traceLocation.beWarning()) {
            		LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000481",
            	  "Error occurred in invoking event \"valueUnbound()\" on listener {0}. " +
            	  "The HTTP session is {1}. The attribute is {2}.", new Object[]{bindingListener.getClass(), session, attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	}
            }
          }
        }
      }
    } else {
      try {
        bindingListener.valueUnbound(new HttpSessionBindingEvent(session, attributeName));
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly
    	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation,  "ASJ.web.000386", "Cannot notify listener [{0}] that an attribute is being unbound from  [{1}] session. " +
    	    "The attribute name is [{2}].", new Object[]{bindingListener.getClass(), session, attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  if (traceLocation.beWarning()) {
    	    LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000482", 
    	      "Error occurred in invoking event \"valueUnbound()\" on listener {0}. " +
    	      "The HTTP session is {1}. The attribute is {2}.", new Object[]{bindingListener.getClass(), session,  attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
    	  }
      }
    }
  }

  //connection events

  public void connectionClosed(ServletRequest request, ServletResponse response) {
    ConnectionEventListener[] listeners = webComponents.getConnectionEventListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.connectionClosed", listeners[0].getClass());
      }//ACCOUNTING.start - END
      for (int i = 0; i < listeners.length; i++) {
        try {
          listeners[i].connectionClosed(request, response);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly
      	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000387", "Cannot notify listener [{0}] that connection is closed.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
      	  if (traceLocation.beWarning()) {
        		LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000483", 
              "Error occurred in invoking event \"connectionClosed()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
      	  }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.connectionClosed");
      }//ACCOUNTING.end - END
    }
  }

  //Servlet request events

  /**
   * Notification that the request is about to come into scope of the web application:
   * when it is about to enter the first servlet or filter in each web application.
   */
  public void requestInitialized(ServletRequest request) {
    ServletRequestListener[] listeners = webComponents.getServletRequestListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.requestInitialized", WebEvents.class);
      }//ACCOUNTING.start - END
      ServletRequestEvent servletRequestEvent = new ServletRequestEvent(servletContext, request);
      String message = "Error occurred in invoking event \"requestInitialized()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; listeners != null && i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ".");
              listeners[i].requestInitialized(servletRequestEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; listeners != null && i < listeners.length; i++) {
                listeners[i].requestInitialized(servletRequestEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              //TODO:Polly
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000388", "Cannot notify listener [{0}] that the request is about to come into scope of the web application [{1}].", new Object[]{listeners[i].getClass(), aliasName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000484",
                  "Error occurred in invoking event \"requestInitialized()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.requestInitialized");
      }//ACCOUNTING.end - END
    }
  }//requestInitialized

  /**
   * Notification that the request is about to go out of scope of the web application:
   * when it exits the last servlet or the first filter in the chain.
   */
  public void requestDestroyed(ServletRequest request) {
    ServletRequestListener[] listeners = webComponents.getServletRequestListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.requestDestroyed", listeners[0].getClass());
      }//ACCOUNTING.start - END
      ServletRequestEvent servletRequestEvent = new ServletRequestEvent(servletContext, request);
      for (int i = 0; listeners != null && i < listeners.length; i++) {
        try {
          listeners[i].requestDestroyed(servletRequestEvent);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
      	  //TODO:Polly
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000389", "Cannot notify listener [{0}] that the request is about to go out of scope of the web application [{1}].", new Object[]{listeners[i].getClass(), aliasName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000485",
              "Error occurred in invoking event \"requestDestroyed()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          }
          synchronized (asynchronousMonitor) {
            if (getUnhandledException() == null) {
              setEventListenerError();
              setUnhandledException(e);
              setUnhandledExceptionMessage("Error occurred in invoking event \"requestDestroyed()\" on listener " + listeners[i].getClass() + ".");
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.requestDestroyed");
      }//ACCOUNTING.end - END
    }
  }//requestDestroyed

  /**
   * Notification that a new attribute was added to the servlet request. Called
   * after the attribute is added.
   */
  public void requestAttributeAdded(ServletRequest request, String attributeName, Object attributeValue) {
    ServletRequestAttributeListener[] listeners = webComponents.getServletRequestAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.requestAttributeAdded", WebEvents.class);
      }//ACCOUNTING.start - END
      ServletRequestAttributeEvent servletRequestAttributeEvent =
        new ServletRequestAttributeEvent(servletContext, request, attributeName, attributeValue);
      String message = "Error occurred in invoking event \"attributeAdded()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; listeners != null && i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ".");
              listeners[i].attributeAdded(servletRequestAttributeEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; listeners != null && i < listeners.length; i++) {
                listeners[i].attributeAdded(servletRequestAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000390", "Cannot notify listener [{0}] that an attribute has been added to the servlet request." +
                "Attribute name is [{1}]." , new Object[]{listeners[i].getClass(), attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000486",
                  "Error occurred in invoking event \"attributeAdded()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.requestAttributeAdded");
      }//ACCOUNTING.end - END
    }
  }//requestAttributeAdded

  /**
   * Notification that a new attribute was removed from the servlet request.
   * Called after the attribute is removed.
   */
  public void requestAttributeRemoved(ServletRequest request, String attributeName, Object attributeRemovedValue) {
    ServletRequestAttributeListener[] listeners = webComponents.getServletRequestAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.requestAttributeRemoved", WebEvents.class);
      }//ACCOUNTING.start - END
      ServletRequestAttributeEvent servletRequestAttributeEvent =
        new ServletRequestAttributeEvent(servletContext, request, attributeName, attributeRemovedValue);
      String message = "Error occurred in invoking event \"attributeRemoved()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; listeners != null && i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ".");
              listeners[i].attributeRemoved(servletRequestAttributeEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; listeners != null && i < listeners.length; i++) {
                listeners[i].attributeRemoved(servletRequestAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              //TODO:Polly
              //
            	LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000391",  "Cannot notify listener [{0}] that an attribute has been removed from the servlet request." +
            	  "Attribute is [{1}].", new Object[]{listeners[i].getClass(), attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	if (traceLocation.beWarning()) {
            	  LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000487", 
            	    "Error occurred in invoking event \"attributeRemoved()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
            	}
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.requestAttributeRemoved");
      }//ACCOUNTING.end - END
    }
  }//requestAttributeRemoved

  /**
   * Notification that an attribute was replaced on the servlet request.
   * Called after the attribute is replaced.
   */
  public void requestAttributeReplaced(ServletRequest request, String attributeName, Object attributeOldValue) {
    ServletRequestAttributeListener[] listeners = webComponents.getServletRequestAttributeListeners();
    if (listeners == null) {
      return;
    }
    boolean accounting = Accounting.isEnabled() && listeners != null && listeners.length > 0;
    try {
      if (accounting) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WebEvents.requestAttributeReplaced", WebEvents.class);
      }//ACCOUNTING.start - END
      ServletRequestAttributeEvent servletRequestAttributeEvent =
                      new ServletRequestAttributeEvent(servletContext, request, attributeName, attributeOldValue);
      String message = "Error occurred in invoking event \"attributeReplaced()\" on listener ";
      synchronized (synchronousMonitor) {
        if (!isEventListenerError() && getListenerExceptionMessage() == null) {
          setEventListenerError();
          for (int i = 0; listeners != null && i < listeners.length; i++) {
            try {
              setListenerExceptionMessage(message + listeners[i].getClass() + ".");
              listeners[i].attributeReplaced(servletRequestAttributeEvent);
              clearListenerExceptionMessage();
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            }
          }
          clearEventListenerError();
        } else if (isEventListenerError()) {
          if (getListenerExceptionMessage() != null &&
            !getListenerExceptionMessage().startsWith(message)) {
            int i = 0;
            try {
              for (i = 0; listeners != null && i < listeners.length; i++) {
                listeners[i].attributeReplaced(servletRequestAttributeEvent);
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
            	//TODO:Polly
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000392","Cannot notify listener [{0}] that an attribute has been replaced in the servlet request." +
                "Attribute is [{1}].", new Object[]{listeners[i].getClass(), attributeName}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              if (traceLocation.beWarning()) {
                LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000488",
                  "Error occurred in invoking event \"attributeReplaced()\" on listener {0}.", new Object[]{listeners[i].getClass()}, e, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
              }
            }
          }
        }
      }
    } finally {
      if (accounting) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WebEvents.requestAttributeReplaced");
      }//ACCOUNTING.end - END
    }
  }//requestAttributeReplaced

  public void clearEventListenerError() {
    synchronized (synchronousMonitor) {
      eventListenerError = false;
    }
  }

  public void setEventListenerError() {
    synchronized (synchronousMonitor) {
      eventListenerError = true;
    }
  }

  public boolean isEventListenerError() {
    return eventListenerError;
  }

  public String getListenerExceptionMessage() {
    return listenerExceptionMessage;
  }

  public void setListenerExceptionMessage(String message) {
    synchronized (synchronousMonitor) {
      listenerExceptionMessage = message;
    }
  }

  public void clearListenerExceptionMessage() {
    synchronized (synchronousMonitor) {
      listenerExceptionMessage = null;
    }
  }

  public Throwable getUnhandledException() {
    return unhandledException;
  }

  public String getUnhandledExceptionMessage() {
    return unhandledExceptionMessage;
  }

  public void setUnhandledExceptionMessage(String message) {
    synchronized (asynchronousMonitor) {
      unhandledExceptionMessage = message;
    }
  }

  public void setUnhandledException(Throwable exception) {
    synchronized (asynchronousMonitor) {
      unhandledException = exception;
    }
  }

  public void clearUnhandledExcepionMessage() {
    synchronized (asynchronousMonitor) {
      unhandledExceptionMessage = null;
    }
  }

  public void clearUnhandledException() {
    synchronized (asynchronousMonitor) {
      unhandledException = null;
    }
  }
}
