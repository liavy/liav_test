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
package com.sap.engine.services.servlets_jsp.server.management.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.session.exec.ClientContextImpl;

/**
 * @author Nikolai Dokovski
 * @version 1.0
 *          HttpSessionsOpenMBean provides telnet commands from
 *          servlet_jsp's http_sessions group.
 */
public class HttpSessionsOpenMBean implements DynamicMBean, NotificationBroadcaster {
  private static MBeanInfo mbean_info = null;
  private static Object lock = new Object();
  private SimpleDateFormat date = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.US);

  //Composite Type http session
  private static String[] hs_names;
  private static String[] hs_descriptions;
  private static OpenType[] hs_types;
  private static CompositeType ctype_hs;

  private static OpenType array_ctype_hs;

  //Composite Type list_http_sessions  short
  private static String[] lhs_names;
  private static String[] lhs_descriptions;
  private static OpenType[] lhs_types;
  private static CompositeType ctype_lhs;

  private static String[] hsf_names;
  private static String[] hsf_descriptions;
  private static OpenType[] hsf_types;
  private static CompositeType ctype_hsf;
  private static OpenType array_ctype_hsf;

  private static String[] lhsf_names;
  private static String[] lhsf_descriptions;
  private static OpenType[] lhsf_types;
  private static CompositeType ctype_lhsf;

  private ServiceContext srv_ctx;

  static {
    try {
      hs_names = new String[]{"username", "expire", "isSticky", "application"};
      hs_descriptions = new String[]{"user name", "expire", "isSticky", "application name"};
      hs_types = new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.STRING};
      ctype_hs = new CompositeType("ctype_hs",
        "Composite type for http sessions",
        hs_names, hs_descriptions, hs_types);

      array_ctype_hs = new ArrayType(1, ctype_hs);
      lhs_names = new String[]{"total", "sessions"};
      lhs_descriptions = new String[]{"total number of http sessions", "http sessions"};
      lhs_types = new OpenType[]{SimpleType.INTEGER, array_ctype_hs};
      ctype_lhs = new CompositeType("ctype_lhs",
        "Composite type for http session list",
        lhs_names, lhs_descriptions, lhs_types);

      hsf_names = new String[]{"username", "session_id", "created",
                               "last_accessed", "expire", "isSticky", "application"};
      hsf_descriptions = new String[]{"user name", "session id", "created",
                                      "last accessed", "expire", "isSticky", "application name"};
      hsf_types = new OpenType[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                                 SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.STRING};

      ctype_hsf = new CompositeType("ctype_hsf",
        "Composite type for http sessions",
        hsf_names, hsf_descriptions, hsf_types);

      array_ctype_hsf = new ArrayType(1, ctype_hsf);

      lhsf_names = new String[]{"total", "sessions"};
      lhsf_descriptions = new String[]{"total number of http sessions", "http sessions"};
      lhsf_types = new OpenType[]{SimpleType.INTEGER, array_ctype_hsf};
      ctype_lhsf = new CompositeType("ctype_lhsf",
        "Composite type for http session list",
        lhsf_names, lhsf_descriptions, lhsf_types);
    } catch (OpenDataException e) {
      if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000516", "Can not construct composite data.", e, null, null);
			}
    }
  }

  public HttpSessionsOpenMBean() {
    init();
  }

  /**
   * list_http_sessions
   * Lists  properties of http session.
   *
   * @return array of CompositeData  elements. Each elements has following structure
   *         <pre>
   *            java.lang.Integer total - total number of http sessions
   *            javax.management.CompositeData[] sessions - http sessions
   *         </pre>
   *         <p/>
   *         each "sessions" element has following structure
   *         <pre>
   *         		java.lang.String  username - user name
   *         		java.lang.String  expire  -  expiration time
   *         		java.lang.String  application - application name
   *         </pre>
   */
  public CompositeData list_http_sessions() {
    return list_http_sessions_internal(false);
  }

  /**
   * list_http_sessions_full
   * Lists extended http session properties.
   *
   * @return array of CompositeData  elements. Each elements has following structure
   *         <pre>
   *            java.lang.Integer total - total number of http sessions
   *            javax.management.CompositeData[] sessions - http sessions
   *         </pre>
   *         <p/>
   *         each "sessions" element has following structure
   *         <pre>
   *         		java.lang.String  username - user name
   *              java.lang.String  session_id - if of the http session
   *         		java.lang.String  created  -  created time
   *         		java.lang.String  last_accessed - last accessed time
   *         		java.lang.String  expire  -  expiration time
   *         		java.lang.String  application - application name
   *         </pre>
   */
  public CompositeData list_http_sessions_full() {
    return list_http_sessions_internal(true);
  }

  /**
   * list_http_sessions_count
   * Lists http sessions count.
   *
   * @return count of http sessions
   */
  public Integer list_http_sessions_count() {
    int res = 0;
    if (srv_ctx != null) {
      Enumeration en = srv_ctx.getDeployContext().getStartedWebApplications();
      while (en.hasMoreElements()) {
        res += ((ApplicationContext) en.nextElement()).getSessionServletContext().getSession().size();
      }
    }

    return new Integer(res);
  }

  private CompositeData list_http_sessions_internal(boolean long_version) {
    ApplicationContext ctx = null;
    ApplicationSession sess = null;
    Vector temp = new Vector();
    int count = 0;
    try {

      Enumeration e = getApplicationContextEnumeration();
      while (e.hasMoreElements()) {
        ctx = (ApplicationContext) e.nextElement();
        Enumeration t = getApplicationSessionEnumeration(ctx);
        while (t.hasMoreElements()) {
          sess = (ApplicationSession) t.nextElement();
          if (!sess.isValid()) {
            continue;
          }
          String name = "user not logged in";
          if (ClientContextImpl.getByClientId(sess.getIdInternal()) != null) {
            name = ClientContextImpl.getByClientId(sess.getIdInternal()).getUser();
          }
          String expire = "never";
          if (sess.getMaxInactiveInterval() != -1) {
            expire = date.format(new Date(sess.getLastAccessedTime() + sess.getMaxInactiveInterval() * 1000));
          }
          boolean isSticky = sess.isSticky();
          String app = ctx.getApplicationName();

          if (long_version) {
            String sessionID = sess.getId();
            String created = date.format(new Date(sess.getCreationTime()));
            String laccessed = date.format(new Date(sess.getLastAccessedTime()));           
            Object[] values = new Object[]{name, sessionID, created, laccessed, expire, isSticky, app};
            temp.add(new CompositeDataSupport(ctype_hsf, hsf_names, values));
            count++;
          } else {
            Object[] values = new Object[]{name, expire, isSticky, app};
            temp.add(new CompositeDataSupport(ctype_hs, hs_names, values));
            count++;
          }
        }
      }
      CompositeData[] tmp_data = new CompositeData[0];
      if (temp.size() > 0) {
        tmp_data = (CompositeData[]) temp.toArray(tmp_data);
      }
      Object[] ret_values = new Object[]{new Integer(count), tmp_data};
      if (long_version) {
        return new CompositeDataSupport(ctype_lhsf, lhsf_names, ret_values);
      } else {
        return new CompositeDataSupport(ctype_lhs, lhs_names, ret_values);
      }

    } catch (OpenDataException e1) {
      if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000517", "Can not construct composite type for list HTTP sessions.", e1, null, null);
			}
    }
    return null;
  }

  /* HttpSessionsOpenMBean does not provide attributes. The getter method returns null.
   * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws AttributeNotFoundException,
    MBeanException, ReflectionException {
    throw new AttributeNotFoundException("No such attribute.");
  }

  /* HttpSessionsOpenMBean does not provide attributes. The setter method has no impact.
   * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
   */
  public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
    InvalidAttributeValueException, MBeanException, ReflectionException {
    throw new AttributeNotFoundException("No such attribute.");
  }

  /* HttpSessionsOpenMBean does not provide attributes. The getter method returns null.
   * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
   */
  public AttributeList getAttributes(String[] arg0) {
    return null;
  }

  /* HttpSessionsOpenMBean does not provide attributes. The setter method has no impact.
   * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
   */
  public AttributeList setAttributes(AttributeList arg0) {
    return null;
  }

  /*
   * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
   */
  public Object invoke(String op_name, Object[] params, String[] signature)
    throws MBeanException, ReflectionException {

    synchronized (lock) {
      if (op_name == null) {
        throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
          "Cannot call invoke with null operation name on HttpSessionsOpenMbean");
      } else if (op_name.equals("list_http_sessions")) {
        return list_http_sessions();
      } else if (op_name.equals("list_http_sessions_count")) {
        return list_http_sessions_count();
      } else if (op_name.equals("list_http_sessions_full")) {
        return list_http_sessions_full();
      }
    }
    return null;
  }

  /*
   * @see javax.management.DynamicMBean#getMBeanInfo()
   */
  public MBeanInfo getMBeanInfo() {
    return mbean_info;
  }

  /* HttpSessionsOpenMBean is not a notification broadcaster. Therefore this method does nothing.
   * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
   */
  public void addNotificationListener(NotificationListener arg0,
                                      NotificationFilter arg1, Object arg2)
    throws IllegalArgumentException {
  }

  /* HttpSessionsOpenMBean is not a notification broadcaster. Therefore this method does nothing.
   * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
   */
  public void removeNotificationListener(NotificationListener arg0)
    throws ListenerNotFoundException {
  }

  /* HttpSessionsOpenMBean is not a notification broadcaster. Therefore this method returns null.
   * @see javax.management.NotificationBroadcaster#getNotificationInfo()
   */
  public MBeanNotificationInfo[] getNotificationInfo() {
    return null;
  }

  private void init() {
    if (mbean_info != null) {
      return;
    }
    //Building OpenMBeanInfo
    OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[0];
    OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[3];
    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];

    //MBean constructor
    constructors[0] = new OpenMBeanConstructorInfoSupport("HttpSessionsOpenMbean",
      "Constructs a HttpSessionsOpenMbean instance.",
      new OpenMBeanParameterInfoSupport[0]);

    //Parameters for clear_http_cache operations
    OpenMBeanParameterInfo[] params_lhs = new OpenMBeanParameterInfoSupport[0];

    operations[0] = new OpenMBeanOperationInfoSupport("list_http_sessions",
      "list http sessions",
      params_lhs,
      ctype_lhs,
      MBeanOperationInfo.INFO);

    operations[1] = new OpenMBeanOperationInfoSupport("list_http_sessions_full",
      "list http sessions",
      params_lhs,
      ctype_lhsf,
      MBeanOperationInfo.INFO);

    operations[2] = new OpenMBeanOperationInfoSupport("list_http_sessions_count",
      "list http sessions",
      params_lhs,
      SimpleType.INTEGER,
      MBeanOperationInfo.INFO);


    mbean_info = new OpenMBeanInfoSupport(this.getClass().getName(),
      "Http Sessions MBean",
      attributes,
      constructors,
      operations,
      notifications);

  }

  private Enumeration getApplicationContextEnumeration() {
    if (srv_ctx != null) {
      Enumeration en = srv_ctx.getDeployContext().getStartedWebApplications();
      return en;
    }
    return null;
  }

  private Enumeration getApplicationSessionEnumeration(ApplicationContext ctx) {
    return ctx.getSessionServletContext().getSession().enumerateSessions();
  }

  /**
   * @param serviceContext
   */
  public void setServiceContext(ServiceContext serviceContext) {
    srv_ctx = serviceContext;

  }
}
