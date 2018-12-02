package com.sap.loadobserver.ejb.beans;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.LocalHome;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.loadobserver.ejb.monitor.SystemLoadMonitor;
import com.sap.log.LogContext;
import com.sap.oomp.da.GCInfoCollector;
import com.sap.oomp.da.sapcontrol.SAPControlFacade;
import com.sap.oomp.pe.PredictionEngine;
import com.sap.sapcontrol.wsclient.SAPControl;
import com.sap.sapcontrol.wsclient.SAPControlPortType;
import com.sap.tc.logging.Severity;
import com.sap.loadobserver.ejb.beans.*;



@Stateless(name="SchedularBean")
@DeclareRoles("Administrator")
@RunAs(value="Administrator")
public class SchedularBean implements SchedularLocal {

	@Resource
	TimerService timer_service;
	MBeanServerConnection mbs;
	
	Timer timer;
	int count = 0;
	int p4port = 50004;
	long mxh = Runtime.getRuntime().maxMemory();
	
	SAPControlFacade facade;
	GCInfoCollector collector;
	boolean added = false;
	SystemLoadMonitor monitor;
	PredictionEngine en;
	
	int ws_port = 0;
    String server_process="";
    String node_name="";
    boolean inited=false;
	
	
	@PostConstruct
	public void init(){
		
		LogContext.initLogContext();
		
		try {
		      InitialContext ctx = new InitialContext();
		      mbs = (MBeanServerConnection) ctx.lookup("jmx");
		    } catch (NamingException ex) {
		       LogContext.getCategory().logThrowableT(Severity.ERROR, LogContext.getSchedularLocation(), ex.getMessage(), ex);	
		    }
		    
		    try{
		    	String node_str = "*:cimclass=SAP_ITSAMJ2eeNode,SAP_J2EEClusterNode=\"\",*";
			    ObjectName p = new ObjectName(node_str);
			    Set result = mbs.queryNames(p, null);
			    if (result != null && result.size()>0){
					for (Object name: result){
						ObjectName o_name = (ObjectName) name;
						node_name = (String)mbs.getAttribute(o_name, "Name");
						CompositeData data = (CompositeData)mbs.getAttribute(o_name, "SystemProperties");
						CompositeDataSupport[] prop = (CompositeDataSupport[])data.get("Properties");
						for (CompositeDataSupport d : prop){
							String el_name = d.get("ElementName").toString();
							if (el_name.equals("SAPSYSTEM")){
								String system = d.get("Value").toString();
								int i = Integer.parseInt(system);
								ws_port = 50013 + (100 * i);
								continue;
							}
							if (el_name.equals("SAPINFO")){
								String proc = d.get("Value").toString();
								int indx = proc.lastIndexOf("_")+1;
								server_process = proc.substring(indx,proc.length());
							}
						}
					}
			    }
			    
		    	
		    }catch (Exception ex){
		    	LogContext.getCategory().logThrowableT(Severity.ERROR, LogContext.getSchedularLocation(), ex.getMessage(), ex);
		    }
		    
		    
		    try{
		    	String node_str = "com.sap.default:name="+node_name+",j2eeType=SAP_J2EEClusterNode,*";
			    ObjectName p = new ObjectName(node_str);
			    Set result = mbs.queryNames(p, null);
			    if (result != null && result.size()>0){
					for (Object name: result){
						ObjectName o_name = (ObjectName) name;
						String[] vmparams = (String[])mbs.getAttribute(o_name, "VmParameters");
						if (vmparams != null && vmparams.length > 0){
							for (String param : vmparams){
								if (param.startsWith("-Xmx")){
									try{
										int idx = param.lastIndexOf("m");
										if (idx >= 0){
											mxh = Integer.parseInt(param.substring(4, idx));
											mxh = mxh*(1024*1024);
										}
										LogContext.getCategory().logT(Severity.DEBUG, LogContext.getSchedularLocation(), "max heap size :"+mxh+"m");
										break;
									}catch (NumberFormatException ex){
										LogContext.getCategory().logThrowableT(Severity.WARNING, LogContext.getSchedularLocation(), ex.getMessage(), ex);
									}
								}
							}
						}
					}
			    }	
		    }catch (Exception ex){
		    	LogContext.getCategory().logThrowableT(Severity.ERROR, LogContext.getSchedularLocation(), ex.getMessage(), ex);
		    }
		    
		    
		    
		    
		    LogContext.getCategory().logT(Severity.INFO, LogContext.getSchedularLocation(), "max heap size :"+mxh);
		    LogContext.getCategory().logT(Severity.INFO, LogContext.getSchedularLocation(), "server_process :"+server_process);
		    
		    //wiring up da and pe
			if (collector == null){
				collector = new GCInfoCollector();
			}
			if (en == null){
				en = new PredictionEngine(collector,mxh);
			}
			if (monitor == null){
				monitor = new SystemLoadMonitor(en);
			}
			
			try{
		    	monitor.registerResourceMBean();
		    }catch (Exception ex){
		    	LogContext.getCategory().logThrowableT(Severity.ERROR, LogContext.getSchedularLocation(), ex.getMessage(), ex);	
		    }
		    

		    
		    
	}
	
	private void initSapControlService(int port){
		//intializing web service connection
		LogContext.getCategory().logT(Severity.INFO, LogContext.getSchedularLocation(), "SAP Control WS port :"+port);
	    
	    try{
	    	URL url = new URL("http://localhost:"+port+"/?wsdl");
			javax.xml.namespace.QName name = new javax.xml.namespace.QName("urn:SAPControl", "SAPControl");
			SAPControlPortType sap_control_port = new SAPControl(url,name).getSAPControl();
			if (facade == null){
				facade = new SAPControlFacade(sap_control_port,server_process);
			}
			if (!added){
				added = true;
				facade.addObserver(collector);
		    }
			inited = true;
	    }catch (Exception ex){
	    	LogContext.getCategory().logThrowableT(Severity.ERROR, LogContext.getSchedularLocation(), ex.getMessage(), ex);
	    	
	    }
	}
	
	@TransactionAttribute(value = TransactionAttributeType.NEVER)
	public void schedule(long timeout, int port) {
		
		if (port != 0){
			initSapControlService(port);
		}else{
			initSapControlService(ws_port);
		}
		if(!inited){
			LogContext.getCategory().logT(Severity.INFO, LogContext.getSchedularLocation(), "observer schedular can not be executed");
			return;
		}
		LogContext.getCategory().logT(Severity.DEBUG, LogContext.getSchedularLocation(), "observer schedular initialized");
		timer = timer_service.createTimer(5000, timeout, "LoadObserver Schedular");
	}

	@PreDestroy
	public void clean() {
		monitor.removeResourceMBean();
		monitor = null;
	}
	
	@Timeout
	public void execute(Timer timer) throws MalformedURLException{
		synchronized(SchedularBean.class){
			if (!inited){
				return;
			}
			LogContext.getCategory().logT(Severity.DEBUG, LogContext.getSchedularLocation(), "observer schedular invoked "+count++);
			try{
				StringBuilder builder = facade.getGCInfo();
				if (builder != null && builder.length()>0){
					LogContext.getCategory().logT(Severity.INFO, LogContext.getSchedularLocation(), "GC INFO: "+builder.toString());
				}
			}catch(Exception ex){
				inited = false;
				
			}
			
		}
	}

	
	
	public void unschedule(){
    	try{
    	Collection timers = timer_service.getTimers();
    	if (timers != null && !timers.isEmpty()){
    		Iterator iter = timers.iterator();
    		while(iter != null && iter.hasNext()){
    			Timer t = (Timer)iter.next();
    			if (t.getInfo().equals("LoadObserver Schedular")){
    				t.cancel();
    				LogContext.getCategory().logT(Severity.DEBUG, LogContext.getSchedularLocation(), t.getInfo()+" timer is stopped");
    			}
    		}
    	}	
    	
    	}catch(Exception e){
    		LogContext.getCategory().logThrowableT(Severity.WARNING, LogContext.getSchedularLocation(), e.getMessage(), e);
    	}
    	
    	    	
    	timer = null;
    	collector = null;
    	facade = null;
    	added = false;
    	en = null;
    	
    	LogContext.getCategory().logT(Severity.DEBUG, LogContext.getSchedularLocation(), "cleaning observer schedular resources");
    	
		
	}
	
}
