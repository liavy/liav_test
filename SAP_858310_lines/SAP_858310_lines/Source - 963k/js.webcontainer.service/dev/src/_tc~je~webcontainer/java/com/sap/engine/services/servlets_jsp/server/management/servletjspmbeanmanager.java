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
package com.sap.engine.services.servlets_jsp.server.management;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.management.beans.CleanUpErr500OpenMBean;
import com.sap.engine.services.servlets_jsp.server.management.beans.HttpSessionsOpenMBean;
import com.sap.engine.services.servlets_jsp.server.management.beans.ListWCEProvOpenMBean;
import com.sap.engine.services.servlets_jsp.server.management.beans.PolicyDomainsOpenMBean;
import com.sap.engine.services.servlets_jsp.server.management.beans.RDStatisticsOpenMBean;
import com.sap.engine.services.servlets_jsp.server.management.beans.SessTracingOpenMBean;
import com.sap.jmx.ObjectNameFactory;
import com.sap.tc.logging.Location;

/**
 * @author Nikolai Dokovski
 * @version 1.0
 *          <p/>
 *          ServletJSPMBeanManager is responsible for managing the ServletJSP's beans lifecycle.
 */
public class ServletJSPMBeanManager {
  private MBeanServer mbs;
  private static ServletJSPMBeanManager manager = null;

  private static HttpSessionsOpenMBean http_sessions_bean = null;
  private static PolicyDomainsOpenMBean policy_domains_bean = null;
  private static RDStatisticsOpenMBean rdStatisticsBean = null;
  private static RDStatisticsOpenMBean wceRDStatisticsBean = null;
  private static CleanUpErr500OpenMBean cleanUp500Bean = null;
  private static ListWCEProvOpenMBean listWCEProvBean = null;
  private static SessTracingOpenMBean sessTracingBean = null;

  private static ObjectName hs_name;
  private static ObjectName pd_name;
  private static ObjectName rdStatisticsName;
  private static ObjectName wceRDStatisticsName;
  private static ObjectName cleanUp500BeanName;
  private static ObjectName listWCEProvBeanName;
  private static ObjectName sessTracingBeanName;

  private ServletJSPMBeanManager(MBeanServer obj, ServiceContext serviceContext) {
    this.mbs = obj;
    try {
      http_sessions_bean = new HttpSessionsOpenMBean();
    } catch (Exception e) {
      if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000521", "Cannot construct HttpSessionsOpenMBean.", e, null, null);
			}
    }
    try {
      policy_domains_bean = new PolicyDomainsOpenMBean();      
    } catch (Exception e) {
      if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000522", "Cannot construct PolicyDomainsOpenMBean.", e, null, null);
			}
    }
    try {
      rdStatisticsBean = new RDStatisticsOpenMBean();      
    } catch (Exception e) {      
      if (LogContext.getLocationService().beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000681", "Cannot construct RDQoSStatisticsOpenMBean.", e, null, null);
      }
    }    
    try {
      if (!ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
        wceRDStatisticsBean = new RDStatisticsOpenMBean();  
      }
    } catch (Exception e) {      
      if (LogContext.getLocationService().beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000693", "Cannot construct RDStatisticsOpenMBean for displaying statistics for WCE request dispatching.", e, null, null);
      }
    }
    try{
    	cleanUp500Bean = new CleanUpErr500OpenMBean();
    } catch (Exception e) {
    	if (LogContext.getLocationService().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000744", "Cannot construct CleanUpErr500OpenMBean for cleaning the ISE500 monitors.", e, null, null);
          }
    }
    try{
    	listWCEProvBean = new ListWCEProvOpenMBean();
    } catch (Exception e) {
    	if (LogContext.getLocationService().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000745", "Cannot construct ListWCEProvOpenMBean for listing the WCE providers details.", e, null, null);
          }
    }
    try{
    	sessTracingBean = new SessTracingOpenMBean();
    } catch (Exception e){
    	if (LogContext.getLocationService().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000746", "Cannot construct SessTracingOpenMBean for managing user session tracing.", e, null, null);
          }
    }
    if (http_sessions_bean != null) {
      http_sessions_bean.setServiceContext(serviceContext);      
    }
    if (policy_domains_bean != null) {
      policy_domains_bean.setServiceContext(serviceContext);
    }
    if (rdStatisticsBean != null) {
      rdStatisticsBean.setRDUsageMonitor(ServiceContext.getServiceContext().getRDResourceProvider().getMonitor());      
    }    
    if (wceRDStatisticsBean != null) {
      wceRDStatisticsBean.setRDUsageMonitor(ServiceContext.getServiceContext().getWCERDResourceProvider().getMonitor());     
    }
    if (cleanUp500Bean != null) {
    	cleanUp500Bean.setServiceContext(serviceContext);
    }
    if (listWCEProvBean != null) {
    	listWCEProvBean.setServiceContext(serviceContext);
    }
  }

  public static ServletJSPMBeanManager initManager(MBeanServer obj, ServiceContext serviceContext) {
    if (manager == null) {
      manager = new ServletJSPMBeanManager(obj, serviceContext);
    }
    return manager;
  }


  public void startListHttpSessionsBean() {
    if (mbs != null && http_sessions_bean != null) {
      Location traceLocation = LogContext.getLocationService();
      try {
        hs_name = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_http_sessions", null, null);
      } catch (MalformedObjectNameException e1) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000523", "Cannot construct ObjectName.", e1, null, null);
				}
      }
      try {
        if (traceLocation.beInfo()) {
					traceLocation.infoT("Registering the bean " + hs_name);
				}
				Object tt = mbs.registerMBean(http_sessions_bean, hs_name);
      } catch (InstanceAlreadyExistsException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000524", "Cannot register http_sessions_bean as an MBean.", e2, null, null);
				}
      } catch (MBeanRegistrationException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000525", "Cannot register http_sessions_bean as an MBean.", e2, null, null);
				}
      } catch (NotCompliantMBeanException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000526", "Cannot register http_sessions_bean as an MBean.", e2, null, null);
				}
      }
    }
  };

  public void stopListHttpSessionsBean() {
    if (mbs != null && hs_name != null) {
      try {
        mbs.unregisterMBean(hs_name);
      } catch (Exception e) {
        if (LogContext.getLocationService().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000527", "Can not unregister bean {0}", new Object[]{hs_name}, e, null, null);
				}
      }
    }
  };

  public void startListPolicyDomainsBean() {
    if (mbs != null && policy_domains_bean != null) {
      Location traceLocation = LogContext.getLocationService();
      try {
        pd_name = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_policy_domains", null, null);
      } catch (MalformedObjectNameException e1) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000528", "Cannot construct ObjectName.", e1, null, null);
				}
      }
      try {
        if (traceLocation.beInfo()) {
					traceLocation.infoT("Registering the bean " + pd_name);
				}
				Object tt = mbs.registerMBean(policy_domains_bean, pd_name);
      } catch (InstanceAlreadyExistsException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000529", "Cannot register http_sessions_bean as an MBean." , e2, null, null);
				}
      } catch (MBeanRegistrationException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000530", "Cannot register http_sessions_bean as an MBean.", e2, null, null);
				}
      } catch (NotCompliantMBeanException e2) {
        if (traceLocation.beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000531", "Cannot register http_sessions_bean as an MBean.", e2, null, null);
				}
      }
    }
  }

  public void stopListPolicyDomainsBean() {
    if (mbs != null && pd_name != null) {
      try {
        mbs.unregisterMBean(pd_name);
      } catch (Exception e) {
        if (LogContext.getLocationService().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000532", "Cannot unregister bean {0}", new Object[]{pd_name}, e, null, null);
				}
      }
    }
  }
  
  public void startRDStatisticsMBean() {
    if (mbs != null && rdStatisticsBean != null) {
      Location traceLocation = LogContext.getLocationService();
      try {
        rdStatisticsName = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_requestDispatcherQoSStatistics", null, null);
      } catch (MalformedObjectNameException e1) {       
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000682", "Cannot construct ObjectName for RDQoSStatisticsMBean.", e1, null, null);
        }
      }
      try {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("Registering the bean " + rdStatisticsName);
        }
        Object tt = mbs.registerMBean(rdStatisticsBean, rdStatisticsName);
      } catch (InstanceAlreadyExistsException e2) {        
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000683", "Cannot register requestDispatcherQoSStatistics as an MBean." , e2, null, null);
        }
      } catch (MBeanRegistrationException e2) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000684", "Cannot register requestDispatcherQoSStatistics as an MBean.", e2, null, null);
        }
      } catch (NotCompliantMBeanException e2) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000685", "Cannot register requestDispatcherQoSStatistics as an MBean.", e2, null, null);
        }
      }
    }
  }
  
  private void stopRDStatisticsMBean() {
    if (mbs != null && rdStatisticsName != null) {
      try {
        mbs.unregisterMBean(rdStatisticsName);
      } catch (Exception e) {
        if (LogContext.getLocationService().beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000687", "Cannot unregister bean {0}", new Object[]{rdStatisticsName}, e, null, null);
        }
      }
    }
  }

  public void startWCERDStatisticsMBean() {    
    if (mbs != null && wceRDStatisticsBean != null) {
      Location traceLocation = LogContext.getLocationService();
      try {
        wceRDStatisticsName = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_WCERequestDispatcherQoSStatistics", null, null);        
      } catch (MalformedObjectNameException e1) {       
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000694", "Cannot construct ObjectName for RDQoSStatisticsMBean for WCE request dispatching.", e1, null, null);
        }
      }
      try {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("Registering the bean " + wceRDStatisticsName);
        }
        Object tt = mbs.registerMBean(wceRDStatisticsBean, wceRDStatisticsName);        
      } catch (InstanceAlreadyExistsException e2) {        
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000695", "Cannot register requestDispatcherQoSStatistics for WCE request dispatching as an MBean." , e2, null, null);
        }
      } catch (MBeanRegistrationException e2) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000696", "Cannot register requestDispatcherQoSStatistics for WCE request dispatching as an MBean.", e2, null, null);
        }
      } catch (NotCompliantMBeanException e2) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000697", "Cannot register requestDispatcherQoSStatistics for WCE request dispatching as an MBean.", e2, null, null);
        }
      }
    }
  }
  
  private void stopWCERDStatisticsMBean() {
    if (mbs != null && wceRDStatisticsName != null) {
      try {
        mbs.unregisterMBean(wceRDStatisticsName);
      } catch (Exception e) {
        if (LogContext.getLocationService().beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000698", "Cannot unregister bean {0}", new Object[]{wceRDStatisticsName}, e, null, null);
        }
      }
    }
  }
  
  public void startCleanUpErr500MBean() {
	  if (mbs != null && cleanUp500Bean != null){
		  try{
			  cleanUp500BeanName = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_CleanUpErr500", null, null);
			  mbs.registerMBean(cleanUp500Bean, cleanUp500BeanName);
		  } catch(Exception e){
			  if (LogContext.getLocationService().beWarning()) {
		          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000747", "Cannot register CleanUpErr500OpenMBean for cleaning the ISE500 monitors.", e, null, null);
		        }
		  }
	  }
  }
  
  public void stopCleanUpErr500MBean() {
	  if (mbs != null && cleanUp500BeanName != null) {
	      try {
	        mbs.unregisterMBean(cleanUp500BeanName);
	      } catch (Exception e) {
	    	  if (LogContext.getLocationService().beWarning()) {
	              LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000748", "Cannot unregister bean {0}", new Object[]{cleanUp500BeanName}, e, null, null);
	            }
	      }
	  }
  }
  
  public void startListWCEProvBean() {
	  if (mbs != null && listWCEProvBean != null){
		  try{
			  listWCEProvBeanName = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_list_wce_providers", null, null);
			  mbs.registerMBean(listWCEProvBean, listWCEProvBeanName);
		  } catch(Exception e){
			  if (LogContext.getLocationService().beWarning()) {
		          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000749", "Cannot register ListWCEProvOpenMBean listing WCE provider details.", e, null, null);
		        }
		  }
	  }
  }
  
  public void stopListWCEProvBean() {
	  if (mbs != null && listWCEProvBeanName != null) {
	      try {
	        mbs.unregisterMBean(listWCEProvBeanName);
	      } catch (Exception e) {
	    	  if (LogContext.getLocationService().beWarning()) {
	              LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000750", "Cannot unregister bean {0}", new Object[]{listWCEProvBeanName}, e, null, null);
	            }
	      }
	  }
  }
  
  public void startSessTracingBean() {
	  if (mbs != null && sessTracingBean != null){
		  try{
			  sessTracingBeanName = ObjectNameFactory.getNameForServerChildPerNode("SAP_J2EECommandsGroupPerNode", "webcontainer_user_tracing", null, null);
			  mbs.registerMBean(sessTracingBean, sessTracingBeanName);
		  } catch(Exception e){
			  if (LogContext.getLocationService().beWarning()) {
		          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000751", "Cannot register SessTracingOpenMBean listing WCE provider details.", e, null, null);
		        }
		  }
	  }
  }
  
  public void stopSessTracingBean() {
	  if (mbs != null && sessTracingBeanName != null) {
	      try {
	        mbs.unregisterMBean(sessTracingBeanName);
	      } catch (Exception e) {
	    	  if (LogContext.getLocationService().beWarning()) {
	              LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.ASJ.web.000752", "Cannot unregister bean {0}", new Object[]{sessTracingBeanName}, e, null, null);
	            }
	      }
	  }
  }
  
  public void startAll() {
    startListHttpSessionsBean();
    startListPolicyDomainsBean();
    startRDStatisticsMBean();
    startWCERDStatisticsMBean();
    startCleanUpErr500MBean();
    startListWCEProvBean();
    startSessTracingBean();
  }


  public void stopAll() {
    stopListHttpSessionsBean();
    stopListPolicyDomainsBean();
    stopRDStatisticsMBean();
    stopWCERDStatisticsMBean();
    stopCleanUpErr500MBean();
    stopListWCEProvBean();
    stopSessTracingBean();
  }
}
