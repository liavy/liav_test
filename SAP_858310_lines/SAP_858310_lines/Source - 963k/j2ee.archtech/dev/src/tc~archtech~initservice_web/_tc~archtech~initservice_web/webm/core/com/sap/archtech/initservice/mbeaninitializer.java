package com.sap.archtech.initservice;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchConnAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchConnAccessor_Impl;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchHierarchyAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchHierarchyAccessor_Impl;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSelParmAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSelParmAccessor_Impl;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSessionAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSessionAccessor_Impl;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSetPropAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSetPropAccessor_Impl;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchVariantAccessorWrapper;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchVariantAccessor_Impl;
import com.sap.engine.admin.model.ManagementModelManager;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The class <code>MBeanInitializer</code> is responsible for the registration of the
 * archiving-connector-specific MBeans used inside the Netweaver Administration Framework.
 */
class MBeanInitializer
{
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector Initialization");
  private static final Location loc = Location.getLocation("com.sap.archtech.initservice");
  
  private static final String MBEAN_ARCHCONNACCESSOR = "SAP_ITSAMArchConnAccessor";
  private static final String MBEAN_ARCHHIERARCHYACCESSOR = "SAP_ITSAMArchHierarchyAccessor";
  private static final String MBEAN_ARCHSESSIONACCESSOR = "SAP_ITSAMArchSessionAccessor";
  private static final String MBEAN_ARCHSETPROPACCESSOR = "SAP_ITSAMArchSetPropAccessor";
  private static final String MBEAN_ARCHVARIANTACCESSOR = "SAP_ITSAMArchVariantAccessor";
  private static final String MBEAN_ARCHSELPARMACCESSOR = "SAP_ITSAMArchSelParmAccessor";
  
  private final MBeanServer mbeanServer;
  private final ManagementModelManager modelMgr;
  
  MBeanInitializer() throws NamingException
  {
    Context ctx = null;
    try
    {
      ctx = new InitialContext();
      mbeanServer = (MBeanServer)ctx.lookup("jmx");
      if(mbeanServer == null)
      {
        throw new NamingException("Access to MBean Server not available");
      }
      modelMgr = (ManagementModelManager)ctx.lookup("basicadmin");
      if(modelMgr == null)
      {
        throw new NamingException("Access to Model Manager not available");
      }
    }
    finally
    {
      if(ctx != null)
      {
        ctx.close();
      }
    }
  }
  
  void registerMBeans()
  {
    try
    {
      if(mbeanServer != null)
      {
        // MBean "ArchConnAccessor"
        registerSingleMBean(MBEAN_ARCHCONNACCESSOR, new SAP_ITSAMArchConnAccessorWrapper(new SAP_ITSAMArchConnAccessor_Impl()));
        // MBean "ArchHierarchyAccessor" 
        registerSingleMBean(MBEAN_ARCHHIERARCHYACCESSOR, new SAP_ITSAMArchHierarchyAccessorWrapper(new SAP_ITSAMArchHierarchyAccessor_Impl()));
        // MBean "ArchSessionAccessor"
        registerSingleMBean(MBEAN_ARCHSESSIONACCESSOR, new SAP_ITSAMArchSessionAccessorWrapper(new SAP_ITSAMArchSessionAccessor_Impl()));
        // MBean "ArchSetPropAccessor"
        registerSingleMBean(MBEAN_ARCHSETPROPACCESSOR, new SAP_ITSAMArchSetPropAccessorWrapper(new SAP_ITSAMArchSetPropAccessor_Impl()));
        // MBean "ArchVariantAccessor"
        registerSingleMBean(MBEAN_ARCHVARIANTACCESSOR, new SAP_ITSAMArchVariantAccessorWrapper(new SAP_ITSAMArchVariantAccessor_Impl()));
        // MBean "ArchSelParmAccessor"
        registerSingleMBean(MBEAN_ARCHSELPARMACCESSOR, new SAP_ITSAMArchSelParmAccessorWrapper(new SAP_ITSAMArchSelParmAccessor_Impl()));
      }
      else
      {
        cat.warningT(loc, "Could not register MBeans. Check previous log entries for failure of MBean Server access.");
      }
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.WARNING, loc, "MBean registration failed", e);
    }
  }
  
  private void registerSingleMBean(String registeredMBeanName, DynamicMBean mbean)
  throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException
  {
    ObjectName objName = getObjectName(registeredMBeanName);
    if(!mbeanServer.isRegistered(objName))
    {
      mbeanServer.registerMBean(mbean, objName);
      loc.infoT("Successfully registered MBean \"" + objName.getCanonicalName() + "\"");
    }
  }
  
  private String getLocalJ2eeClusterName() 
  {
    ObjectName objName = modelMgr.getManagementModelHelper().getSAP_ITSAMJ2eeClusterObjectName();
    return objName.getKeyProperty("SAP_ITSAMJ2eeCluster.Name");
  }

  void unregisterMBeans()
  {
    try
    {
      if(mbeanServer != null)
      {
        unregisterSingleMBean(MBEAN_ARCHCONNACCESSOR);
        unregisterSingleMBean(MBEAN_ARCHHIERARCHYACCESSOR);
        unregisterSingleMBean(MBEAN_ARCHSESSIONACCESSOR);
        unregisterSingleMBean(MBEAN_ARCHSETPROPACCESSOR);
        unregisterSingleMBean(MBEAN_ARCHVARIANTACCESSOR);
        unregisterSingleMBean(MBEAN_ARCHSELPARMACCESSOR);
      }
      else
      {
        cat.warningT(loc, "Could not unregister MBeans. Check previous log entries for failure of MBean Server access.");
      }
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.WARNING, loc, "Could not unregister MBeans", e);
    }
  }
  
  private void unregisterSingleMBean(String registeredMBeanName)
  throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException
  {
    ObjectName objName = getObjectName(registeredMBeanName);
    mbeanServer.unregisterMBean(objName);
    loc.infoT("Successfully unregistered MBean \"" + objName.getCanonicalName() + "\"");
  }
  
  String getMBeansInfo()
  {
    try
    {
      if(mbeanServer != null)
      {
        // see CSN 0766325 2007 -> getMBeanCount() is regarded as "dangerous performance killer"
        int nrOfMBeans = 0;//mbeanServer.getMBeanCount();
        StringBuilder buf = new StringBuilder(getSingleMBeanInfo(MBEAN_ARCHCONNACCESSOR, nrOfMBeans))
          .append(getSingleMBeanInfo(MBEAN_ARCHHIERARCHYACCESSOR, nrOfMBeans))
          .append(getSingleMBeanInfo(MBEAN_ARCHSESSIONACCESSOR, nrOfMBeans))
          .append(getSingleMBeanInfo(MBEAN_ARCHSETPROPACCESSOR, nrOfMBeans))
          .append(getSingleMBeanInfo(MBEAN_ARCHVARIANTACCESSOR, nrOfMBeans))
          .append(getSingleMBeanInfo(MBEAN_ARCHSELPARMACCESSOR, nrOfMBeans));
        return buf.toString();
      }
      return "Could not get MBeans info. Check previous log entries for failure of MBean Server access.";
    }
    catch(Exception e)
    {
      return new StringBuilder("Could not get info about MBean registrations. Exception was: ").append(e.getMessage()).toString();
    }
  }
  
  private String getSingleMBeanInfo(String registeredMBeanName, final int nrOfMBeans)
  throws MalformedObjectNameException, ReflectionException, IntrospectionException, InstanceNotFoundException
  {
    ObjectName objName = getObjectName(registeredMBeanName);
    MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objName);
    if((mbeanServer.isRegistered(objName)))
    {
      return new StringBuilder("\"").append(mbeanInfo.getDescription())
        .append("\" is one of the ")
        .append(nrOfMBeans > 0 ? nrOfMBeans : "")
        .append(" registered MBeans.")
        .append('\n').toString();
    }
    return new StringBuilder("MBean \"").append(objName.getCanonicalName()).append("\" is not registered.").toString();
  }
  
  private ObjectName getObjectName(String registeredMBeanName)
  throws MalformedObjectNameException
  {
    return new ObjectName(
        new StringBuffer(":cimclass=").append(registeredMBeanName)
        .append(",version=1.0,type=SAP_ITSAMJ2eeCluster.").append(registeredMBeanName)
        .append(",SAP_ITSAMJ2eeCluster.CreationClassName=SAP_ITSAMJ2eeCluster,")
        .append("SAP_ITSAMJ2eeCluster.Name=").append(getLocalJ2eeClusterName()).append(",")
        .append(registeredMBeanName).append(".Name=").append(registeredMBeanName).append(",")
        .append(registeredMBeanName).append(".CreationClassName=").append(registeredMBeanName)
        .toString());
  }
}
