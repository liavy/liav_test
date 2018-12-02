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

import java.util.StringTokenizer;
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
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

/**
 * @author Nikolai Dokovski
 * @version 1.0
 *          <p/>
 *          PolicyDomainsOpenMBean provides telnet commands from
 *          servlet_jsp's list_policy_domains group.
 */
public class PolicyDomainsOpenMBean implements DynamicMBean, NotificationBroadcaster {
  private static MBeanInfo mbean_info = null;

  private static Object lock = new Object();

  private static String[] pd_names;
  private static String[] pd_descriptions;
  private static OpenType[] pd_types;
  private static CompositeType ctype_pd;
  private static OpenType array_ctype_pd;
  private static OpenType array_string;

  private ServiceContext srv_ctx;

  static {
    try {
      array_string = new ArrayType(1, SimpleType.STRING);
      pd_names = new String[]{"name", "web_applications", "security_sessions"};
      pd_descriptions = new String[]{"domain name", "web applications", "security sessions"};
      pd_types = new OpenType[]{SimpleType.STRING, array_string, SimpleType.INTEGER};
      ctype_pd = new CompositeType("ctype_pd",
        "Composite type for policy domain",
        pd_names, pd_descriptions, pd_types);
      array_ctype_pd = new ArrayType(1, ctype_pd);

    } catch (OpenDataException e) {
      if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000518", "Can not construct composite data.", e, null, null);
			}
    }
  }

  public PolicyDomainsOpenMBean() {
    init();
  }

  /**
   * list_policy_domains
   * Lists current policy domain names
   *
   * @return policy domain names
   */
  public String[] list_policy_domains() {
    Vector temp = new Vector();
    String[] ret_data = new String[0];
    String ret = srv_ctx.getWebContainerPolicy().dump(false, Constants.lineSeparator);
    StringTokenizer tkz = new StringTokenizer(ret, Constants.lineSeparator);
    while (tkz.hasMoreTokens()) {
      String t = tkz.nextToken();
      if (t == null) {
        continue;
      }
      t = t.trim();
      if (t.indexOf("[") >= 0 && t.lastIndexOf("]") > t.indexOf("["))
        t = t.substring(t.indexOf("[") + 1, t.lastIndexOf("]"));
      if (t.length() > 1) {
        temp.add(t);
      }
    }
    if (temp.size() > 0) {
      ret_data = (String[]) temp.toArray(ret_data);
    }
    return ret_data;

  }

  /**
   * list_policy_domains_full
   * Lists policy domains properties.
   *
   * @return array of CompositeData  elements. Each elements has following structure
   *         <pre>
   *         	  java.lang.String  name - policy domain name
   *         	  java.lang.String[] web_applications - array of web application's name
   *                                                  belonging to this domain
   *            java.lang.Integer  security_sessions - number of security sessions
   *         </pre>
   */
  public CompositeData[] list_policy_domains_full() {
    Vector temp = new Vector();
    CompositeData[] ret_data = new CompositeData[0];
    String ret = srv_ctx.getWebContainerPolicy().dump(true, Constants.lineSeparator);
    StringTokenizer tkz = new StringTokenizer(ret, Constants.lineSeparator);
    while (tkz.hasMoreTokens()) {
      String t = tkz.nextToken();
      if (t == null) {
        continue;
      }
      t = t.trim();
      if (t.indexOf("[") >= 0 && t.lastIndexOf("]") > t.indexOf("[")) {
        String name = t.substring(t.indexOf("[") + 1, t.lastIndexOf("]"));
        Location traceLocation = LogContext.getLocationService();
        if (traceLocation.beDebug()) {
					traceLocation.debugT(name);
				}
				Vector wapp_vec = new Vector();
        Integer ssessions = new Integer(0);

        if (name.length() > 0) {
          String wapp = tkz.nextToken();
          if (wapp == null) {
            continue;
          }

          wapp = wapp.substring(wapp.indexOf("Web Applications: ") + 18);
          StringTokenizer apps_tkz = new StringTokenizer(wapp, ", ");

          while (apps_tkz.hasMoreTokens()) {
            String appname = apps_tkz.nextToken().trim();
            if (traceLocation.beDebug()) {
							traceLocation.debugT(appname);
						}
						wapp_vec.add(appname);
          }
          String ss = tkz.nextToken();
          ss = ss.substring(ss.indexOf("Security Sessions: ") + 19);
          try {
            ssessions = new Integer(ss);
          } catch (NumberFormatException e) {
            if (traceLocation.beWarning()) {
							LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000519", "Can not parse security sessions.", e, null,null);
						}
          }
        }

        String[] wapp_array = new String[0];
        if (wapp_vec.size() > 0) {
          wapp_array = (String[]) wapp_vec.toArray(wapp_array);
        }
        Object[] values = new Object[]{name, wapp_array, ssessions};
        CompositeData data = null;
        try {
          data = new CompositeDataSupport(ctype_pd, pd_names, values);
          temp.add(data);
        } catch (OpenDataException e) {
          if (traceLocation.beWarning()) {
						LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000520", "Can not construct composite data.", e, null, null);
					}
        }
      }
    }
    if (temp.size() > 0) {
      ret_data = (CompositeData[]) temp.toArray(ret_data);
    }
    return ret_data;
  }


  /* PolicyDomainsOpenMBean does not provide attributes. The getter method returns null.
   * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws AttributeNotFoundException,
    MBeanException, ReflectionException {
    throw new AttributeNotFoundException("No such attribute.");
  }

  /* PolicyDomainsOpenMBean does not provide attributes. The setter method has no impact.
   * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
   */
  public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
    InvalidAttributeValueException, MBeanException, ReflectionException {
    throw new AttributeNotFoundException("No such attribute.");
  }

  /* PolicyDomainsOpenMBean does not provide attributes. The getter method returns null.
   * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
   */
  public AttributeList getAttributes(String[] arg0) {
    return null;
  }

  /* PolicyDomainsOpenMBean does not provide attributes. The setter method has no impact.
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
          "Cannot call invoke with null operation name on PolicyDomainsOpenMBean");
      } else if (op_name.equals("list_policy_domains")) {
        return list_policy_domains();
      } else if (op_name.equals("list_policy_domains_full")) {
        return list_policy_domains_full();
      }
      return null;
    }
  }

  /*
   * @see javax.management.DynamicMBean#getMBeanInfo()
   */
  public MBeanInfo getMBeanInfo() {
    return mbean_info;
  }

  /* PolicyDomainsOpenMBean is not a notification broadcaster. Therefore this method does nothing.
   * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
   */
  public void addNotificationListener(NotificationListener arg0,
                                      NotificationFilter arg1, Object arg2)
    throws IllegalArgumentException {
  }

  /* PolicyDomainsOpenMBean is not a notification broadcaster. Therefore this method does nothing.
   * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
   */
  public void removeNotificationListener(NotificationListener arg0)
    throws ListenerNotFoundException {
  }

  /* PolicyDomainsOpenMBean is not a notification broadcaster. Therefore this method returns null.
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
    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[2];
    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];


    //MBean constructor
    constructors[0] = new OpenMBeanConstructorInfoSupport("PolicyDomainsOpenMBean",
      "Constructs a PolicyDomainsOpenMBean instance.",
      new OpenMBeanParameterInfoSupport[0]);

    //Parameters for list_policy_domains operations
    OpenMBeanParameterInfo[] params_lhs = new OpenMBeanParameterInfoSupport[0];

    operations[0] = new OpenMBeanOperationInfoSupport("list_policy_domains",
      "list policy domains",
      params_lhs,
      array_string,
      MBeanOperationInfo.INFO);

    operations[1] = new OpenMBeanOperationInfoSupport("list_policy_domains_full",
      "list policy domains",
      params_lhs,
      array_ctype_pd,
      MBeanOperationInfo.INFO);

    mbean_info = new OpenMBeanInfoSupport(this.getClass().getName(),
      "Policy Domain Open MBean",
      attributes,
      constructors,
      operations,
      notifications);

  }

  /**
   * @param serviceContext
   */
  public void setServiceContext(ServiceContext serviceContext) {
    srv_ctx = serviceContext;

  }
}
