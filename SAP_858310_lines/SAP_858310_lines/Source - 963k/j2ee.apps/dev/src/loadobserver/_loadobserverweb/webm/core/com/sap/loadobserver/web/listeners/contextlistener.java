package com.sap.loadobserver.web.listeners;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sap.loadobserver.ejb.beans.SchedularLocal;

public class ContextListener implements ServletContextListener {

	@EJB(beanName="SchedularBean")
	SchedularLocal local;
	
	
	
	public void contextDestroyed(ServletContextEvent event) {
		event.getServletContext().log("Load Observer servlet context is destroyed.");
		
	}

	
	public void contextInitialized(ServletContextEvent event) {
		int timeout = 1000*30;
		int port = 0;
		event.getServletContext().log("Load Observer servlet context is initialized.");
		String interval = event.getServletContext().getInitParameter("interval");
		if (interval != null){
			try {
				timeout = 1000 * Integer.parseInt(interval);
			}catch (NumberFormatException e){
				event.getServletContext().log("Interval context parameter is not an integer");
			}
		}
		String ws_port = event.getServletContext().getInitParameter("ws_port");
		if (ws_port != null){
			try {
				port = Integer.parseInt(ws_port);
			}catch (NumberFormatException e){
				event.getServletContext().log("Interval context parameter is not an integer");
			}
		}
		
		
		local.schedule(timeout,port);
	}

}
