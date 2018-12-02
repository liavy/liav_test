package com.sap.engine.services.servlets_jsp.server.management.beans;

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
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

import com.sap.engine.services.servlets_jsp.server.ServiceContext;

/**
 * @author I055996
 * 
 *         Exposes the CLEAR_ERROR500_MONITORS telnet command
 *         from group SERVLET_JSP as an OpenMBean
 *          
 */

public class CleanUpErr500OpenMBean implements DynamicMBean, NotificationBroadcaster {
	
	private static MBeanInfo mbeanInfo = null;
	private static Object lock = new Object();
	private ServiceContext serviceContext;
	
	public CleanUpErr500OpenMBean(){
		init();
	}
	/**
	 * Constructs an MBeanInfo object for this MBean.
	 */
	private void init(){
		if (mbeanInfo != null) {
			return;
		}
			    
	    OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
	    constructors[0] = new OpenMBeanConstructorInfoSupport(
	    		"CleanUpErr500OpenMBean",
	    	    "Constructs a CleanUpErr500OpenMBean instance.",
	    	    new OpenMBeanParameterInfoSupport[0]);	    
	    
	    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[1];
	    OpenMBeanParameterInfo[] parameters = new OpenMBeanParameterInfoSupport[0];
	    operations[0] = new OpenMBeanOperationInfoSupport("clear_error500_monitors",
	    	      "clear error 500 monitors",
	    	      parameters,
	    	      SimpleType.STRING,
	    	      MBeanOperationInfo.INFO);
	    
	    OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[0];	    
	    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];
	    mbeanInfo = new OpenMBeanInfoSupport(this.getClass().getName(),
	    	      "Clears the ISE 500 Monitors",
	    	      attributes,
	    	      constructors,
	    	      operations,
	    	      notifications);
	}
	/**
	 * Sets the ServiceContext used by this MBean.
	 * @param serviceContext
	 */
	public void setServiceContext(ServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	@Override
	public Object getAttribute(String arg0) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		throw new AttributeNotFoundException("No such attribute.");
	}

	@Override
	public AttributeList getAttributes(String[] arg0) {
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return mbeanInfo;
	}
	
	/**
	 * Invokes exposed MBean operations.
	 * @param arg0 the name of the operation to invoke
	 * @param arg1 the arguments for the operation to be invokeked
	 * @param arg2 the signature of the operation
	 * @return  the result of the operation
	 */
	@Override
	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		synchronized (lock) {
			if (arg0 == null) {
				throw new RuntimeOperationsException(new IllegalArgumentException(
					"Operation name cannot be null"),
		        	"Cannot call invoke with null operation name on CleanUpErr500OpenMbean");
		    } else if (arg0.equals("clear_error500_monitors")) {
		    	serviceContext.getWebMonitoring().dumpAndClearISE500Monitors();
				return "ISE500 Monitors successfully deleted.";
		    }
		}
		
		return null;
	}

	@Override
	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new AttributeNotFoundException("No such attribute.");		
	}

	@Override
	public AttributeList setAttributes(AttributeList arg0) {
		return null;
	}

	@Override
	public void addNotificationListener(NotificationListener arg0,
			NotificationFilter arg1, Object arg2)
			throws IllegalArgumentException {		
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return null;
	}

	@Override
	public void removeNotificationListener(NotificationListener arg0)
			throws ListenerNotFoundException {	
	}
}
