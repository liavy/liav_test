package com.sap.loadobserver.ejb.monitor;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.log.LogContext;
import com.sap.oomp.da.GCInfoCollector;
import com.sap.oomp.pe.PredictionEngine;
import com.sap.tc.logging.Severity;

public class SystemLoadMonitor implements SystemLoadMonitorMBean{

	private PredictionEngine engine;
	private MBeanServer mbs;
	private ObjectName name;
	public SystemLoadMonitor(PredictionEngine prediction) {
		this.engine = prediction;
	}

	public int getSystemLoad(){
		return engine.getSystemLoad();
	}
	
	public String getPredictionDate(){
		return engine.getPreditcionDate();
	}
	
	public void registerResourceMBean() throws NamingException, JMException, MBeanRegistrationException{
		InitialContext initialCtx = new InitialContext();
		mbs = (MBeanServer) initialCtx.lookup("jmx");
		name = new ObjectName("com.sap.default:name=MonitorMBean,j2eeType=SAP_MonitoredResources");
		try {
			mbs.unregisterMBean(name);
			} catch (Exception e) { 
				// $JL-EXC$
			}
		name = mbs.registerMBean(this, name).getObjectName();
		
	}
	
	public void removeResourceMBean(){
		if (mbs != null && name != null)
			try {
				mbs.unregisterMBean(name);
			} catch (Exception e) {
				LogContext.getCategory().logThrowableT(Severity.WARNING, LogContext.getSchedularLocation(), "SystemLoad managed bean unregistration.", e);
			}
	    }
}
