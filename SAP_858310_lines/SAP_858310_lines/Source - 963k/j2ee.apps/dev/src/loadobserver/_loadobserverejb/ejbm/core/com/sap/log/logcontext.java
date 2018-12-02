package com.sap.log;

import com.sap.loadobserver.ejb.beans.SchedularBean;
import com.sap.oomp.pe.PredictionEngine;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.FileLog;
import com.sap.tc.logging.ListFormatter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Log;
import com.sap.tc.logging.Severity;



public class LogContext {
	static final Location location = Location.getLocation(SchedularBean.class.getName());
	static final Location pe_location = Location.getLocation(PredictionEngine.class.getName());
	static final Category category = Category.getCategory(Category.getRoot(),"/LoadObserver/SystemLoad");

		public static void initLogContext(){
			location.setEffectiveSeverity(Severity.INFO);
			pe_location.setEffectiveSeverity(Severity.INFO);
			Log log = new FileLog("./log/system/systemLoad.trc",10485760,10,new ListFormatter());
			log.setAutoFlush(true);
			location.addLog(log);
			pe_location.addLog(log);
			
		}
		
		
	public static Location getSchedularLocation(){
		return location;
	}
	
	public static Location getPredictionEngineLocation(){
		return pe_location;
	}
	
	
	public static Category getCategory(){
		return category;
	}
	
}
