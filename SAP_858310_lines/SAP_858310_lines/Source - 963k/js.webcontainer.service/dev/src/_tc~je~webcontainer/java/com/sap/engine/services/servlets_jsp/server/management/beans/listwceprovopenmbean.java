package com.sap.engine.services.servlets_jsp.server.management.beans;

import java.util.ArrayList;
import java.util.Hashtable;

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
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerExtensionWrapper;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;

/**
 * @author I055996
 * 
 *         Exposes the LIST_WCE_PROVIDERS telnet command
 *         from group SERVLET_JSP as an OpenMBean
 *          
 */

public class ListWCEProvOpenMBean implements DynamicMBean, NotificationBroadcaster{

	private static MBeanInfo mbeanInfo = null;
	private static Object lock = new Object();
	private ServiceContext serviceContext;
	
	// composite type for the array of wce names
	private static String[] wce_names;
	private static String[] wce_descriptions;
	private static OpenType[] wce_types;
	private static OpenType array_string;
	private static CompositeType comp_wce_type;
	
	// composite type for the array of wce detailed info
	private static String[] wced_names;
	private static String[] wced_descriptions;
	private static OpenType[] wced_types;
	private static CompositeType comp_wced_type;
	
	// construct CompositeData types
	static{
		try{
			//composite type for array of WCE names
			wce_names = new String[] { "wce_names" };
			wce_descriptions = new String[] { "web container extensions' names" };
			array_string = new ArrayType(1, SimpleType.STRING);
			wce_types = new OpenType[] { array_string };
			comp_wce_type = new CompositeType("WCE names",
					"Composite type for WCE names", wce_names,
					wce_descriptions, wce_types);
			
			//composite type for WCE details
			wced_names = new String[] { "wce_name",
					"IWebContainerExtension_impl",
					"IWebContainerExtensionContext_impl", "descriptor(s)" };
			wced_descriptions = new String[] { "wce name",
					"IWebContainerExtension implementation",
					"IWebContainerExtensionContext implementation",
					"wce descriptor(s)" };
			wced_types = new OpenType[] { SimpleType.STRING, SimpleType.STRING,
					SimpleType.STRING, array_string };
			comp_wced_type = new CompositeType("wceDetails_type",
					"Composite type for WCE details", wced_names,
					wced_descriptions, wced_types);
		} catch (OpenDataException e) {
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000741", "MBean ListWCEProvOpenMBean " +
						"cannot construct composite data.", e, null, null);
			}
		}
	}
	
	public ListWCEProvOpenMBean(){
		init();
	}
	
	/**
	 * Constructs an MBeanInfo object for this MBean.
	 */
	private void init(){
		if (mbeanInfo != null) {
			return;
		}
		//constructors
		OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
	    constructors[0] = new OpenMBeanConstructorInfoSupport(
	    		"ListWCEProvOpenMBean",
	    	    "Constructs a ListWCEProvOpenMBean instance.",
	    	    new OpenMBeanParameterInfoSupport[0]);
	    
	    //operations
	    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[2];
	    OpenMBeanParameterInfo[] noParameters = new OpenMBeanParameterInfoSupport[0];
	    operations[0] = new OpenMBeanOperationInfoSupport("list_wce_providers",
	    	      "lists wce providers names",
	    	      noParameters,
	    	      comp_wce_type,
	    	      MBeanOperationInfo.INFO);
	    
	    OpenMBeanParameterInfo[] parameterString = new OpenMBeanParameterInfoSupport[1];
	    parameterString[0] = new OpenMBeanParameterInfoSupport("wce name",
	            "wce name", SimpleType.STRING);
	    operations[1] = new OpenMBeanOperationInfoSupport("list_wce_details",
	    	      "lists wce details",
	    	      parameterString,
	    	      comp_wced_type,
	    	      MBeanOperationInfo.INFO);
	    
	    //no attributes or notifications
	    OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[0];	    
	    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];
	    
	    //construct the mbean info
	    mbeanInfo = new OpenMBeanInfoSupport(this.getClass().getName(),
	    	      "List WCE Providers MBean",
	    	      attributes,
	    	      constructors,
	    	      operations,
	    	      notifications);
	}
	/**
	 * Gets the names of the registered WCE providers.
	 * @return A list of registered WebContainerExtension names.
	 */
	private CompositeData list_wce_providers(){
		CompositeDataSupport result = null;
		
		// get a reference to the web container extension
		WebContainerProvider webContainerProvider = (WebContainerProvider) serviceContext
				.getWebContainer().getIWebContainerProvider();
		Hashtable<String, WebContainerExtensionWrapper> wceTable = webContainerProvider
				.getWebContainerExtensionWrappers();
		Object[] values = (String[]) wceTable.keySet().toArray(
				new String[wceTable.keySet().size()]);
		Object[] wce_values = new Object[] { values };
		try{
			result = new CompositeDataSupport(comp_wce_type, wce_names, wce_values);
		}catch (OpenDataException e){
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000742", "Cannot create composite data for WCE providers.", e, null, null);
			}
		}
		return result;
	}
	
	/**
	 * Gets details about a specific WebContainerExtension.
	 * @param wce the WCE name
	 * @return A CompositeData object containing a WCE name,
	 * WCE implementation class name, WCE Context implementation class name,
	 * and a list of descriptors
	 */
	private CompositeData list_wce_details(String wce){
		CompositeData result = null;
		
		if(wce != null && !wce.equals("")){
		// get a reference to the web container extension
			WebContainerProvider webContainerProvider = (WebContainerProvider) serviceContext
					.getWebContainer().getIWebContainerProvider();
			Hashtable<String, WebContainerExtensionWrapper> wceTable = webContainerProvider
					.getWebContainerExtensionWrappers();
			WebContainerExtensionWrapper wrapper = wceTable.get(wce);
			if (wrapper != null) {
				// extract the data
				String name = wce;
				String wce_impl = wrapper.getWebContainerExtension().toString();
				String wce_ctx_impl = wrapper.getWebContainerExtensionContext().toString();
				Object[] descriptors = (String[]) wrapper.getDescriptorNames()
						.toArray(new String[wrapper.getDescriptorNames().size()]);
				Object[] values = new Object[] { name, wce_impl, wce_ctx_impl,
						descriptors };
				try {
					// construct the composite data element
					result = new CompositeDataSupport(comp_wced_type, wced_names, values);
				} catch (OpenDataException e) {
					if (LogContext.getLocationService().beWarning()) {
						LogContext.getLocation(LogContext.LOCATION_SERVICE)
								.traceWarning(
										"ASJ.web.000743",
										"Cannot create composite data for WCE provider details.",
										e, null, null);
					}
				}
			}
		}
		return result;
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
		    } else if (arg0.equals("list_wce_providers")) {
		    	return list_wce_providers();
		    } else if (arg0.equals("list_wce_details")){
		    	return list_wce_details((String)arg1[0]);
		    }
		    else
		    	return null;
		}
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
