package com.sap.engine.services.servlets_jsp.server.management.beans;

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
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.qos.RDUsageMonitor;

public class RDStatisticsOpenMBean implements DynamicMBean,NotificationBroadcaster {
  private static Object lock = new Object();

  private MBeanInfo mbeanInfo;
  private String[] resourceStatusNames;
  private CompositeType resourceStatusType;
  private ArrayType resourceStatusTypeArray;

  private RDUsageMonitor rdUsageMonitor;

  public RDStatisticsOpenMBean() throws OpenDataException {
    // constructing data types
    // for status operation
    resourceStatusNames = new String[] {"resource name", "current usage",
        "total usage", "number of unavailable response",
        "current unavailable period", "total unavailable period"};
    resourceStatusType = new CompositeType("threadsInfoType", // name
        "Status details per resource/alias", // description
        resourceStatusNames, // itemNames
        resourceStatusNames, // itemDescription
        new OpenType[] {SimpleType.STRING, SimpleType.LONG, SimpleType.LONG,
            SimpleType.LONG, SimpleType.LONG, SimpleType.LONG}); // itemTypes
    resourceStatusTypeArray = new ArrayType(1, resourceStatusType);

    // Building OpenMBeanInfo
    OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[0];
    OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[1];
    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];

    // MBean constructor
    constructors[0] = new OpenMBeanConstructorInfoSupport(
        "RDStatisticsOpenMBean",
        "Constructs a RDApplStatisticsOpenMBean instance.",
        new OpenMBeanParameterInfoSupport[0]);

    // Parameters for listLogonGroup, addLogonGroup, removeLogonGroup
    // operations
    OpenMBeanParameterInfo[] applNameParam = new OpenMBeanParameterInfoSupport[1];
    applNameParam[0] = new OpenMBeanParameterInfoSupport("application name",
        "application name", SimpleType.STRING);

    // get statistic per application
    operations[0] = new OpenMBeanOperationInfoSupport("status", // name
        "gets status details per resource/alias", // description
        applNameParam, // signature
        resourceStatusTypeArray, // returnOpenType
        MBeanOperationInfo.INFO); // impact

    mbeanInfo = new OpenMBeanInfoSupport(this.getClass().getName(),
        "RD Statisitcs Open MBean", attributes, constructors, operations,
        notifications);

  }

  public void setRDUsageMonitor(RDUsageMonitor rdUsageMonitor) {
    this.rdUsageMonitor = rdUsageMonitor;
  }

  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    // TODO Auto-generated method stub
    return null;
  }

  public AttributeList getAttributes(String[] attributes) {
    // TODO Auto-generated method stub
    return null;
  }

  public MBeanInfo getMBeanInfo() {
    return mbeanInfo;
  }

  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
    synchronized (lock) {
      if (actionName == null) {
        throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
            "Cannot call invoke with null operation name on RDApplStatisticsOpenMBean");
      } else if (actionName.equals("status")) {
        checkSingleParam(params, true);
        return status((String) params[0]);
      }
      return null;
    }
  }

  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    // TODO Auto-generated method stub
  }

  public AttributeList setAttributes(AttributeList attributes) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
    // TODO Auto-generated method stub
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    // TODO Auto-generated method stub
  }

  private CompositeData[] status(String resourceName) throws MBeanException {
    CompositeData[] result = new CompositeData[0];
    Vector<CompositeData> tempResult = new Vector<CompositeData>();

    if (resourceName == null || resourceName.equals("")) {
      String resources[] = rdUsageMonitor.getAllResourceNames();
      if (resources == null || resources.length == 0) {
        return result;
      }

      for (int i = 0; i < resources.length; i++) {
        try {
          tempResult.add(resStatusToCompositeData(resources[i]));
        } catch (OpenDataException e) {
          if (LogContext.getLocationService().beWarning()) {            
            LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000679", 
                "Cannot convert the thread usage information for resource [{0}] to composite data.", new Object[] {resources[i]}, e, null, null);
          }
        }
      }
      result = new CompositeData[tempResult.size()];
      result = (CompositeData[]) tempResult.toArray(result);
      return result;
    } else {
      try {
        result = new CompositeData[1];
        result[0] = resStatusToCompositeData(resourceName);
        return result;
      } catch (Exception e) {
        if (LogContext.getLocationService().beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000680", 
              "Cannot convert the thread usage information for resource [{0}] to composite data.", new Object[] { resourceName }, e, null, null);
        }
        return result;
      }
    }
  }

  // --------- helper methods
  private CompositeData resStatusToCompositeData(String resourceName)
      throws OpenDataException {
    // "resource name", "current usage", "total usage", "number of unavailable
    // response", "current unavailable period", "total unavailable period"
    Object[] itemValues = new Object[] {resourceName,
        rdUsageMonitor.getCurrentUsage(resourceName),
        rdUsageMonitor.getTotalUsage(resourceName),
        rdUsageMonitor.getUnavailableResonces(resourceName),
        rdUsageMonitor.getCurrentUnavailablePeriod(resourceName),
        rdUsageMonitor.getTotalUnavailablePeriod(resourceName)};
    return new CompositeDataSupport(resourceStatusType, resourceStatusNames,
        itemValues);
  }

  private void checkSingleParam(Object[] params, boolean nullAllowed)
      throws RuntimeOperationsException {
    if (params.length != 1) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Illegal number of parameters"), "Cannot invoke request dispatcher QoS statistics mbean operation");
    }
    if (params[0] != null && !(params[0] instanceof String)) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Illegal type of the first parameter. The type is:" + params[0].getClass().getName() + ". Should be java.lang.String"), "Cannot invoke request dispatcher QoS statistics mbean operation");
    }
    if (!nullAllowed && params[0] == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Illegal type of the first parameter. The parameter can not be null"), "Cannot invoke request dispatcher QoS statistics mbean operation");
    }
  }
}
