package com.sap.loadobserver.ejb.monitor;

import javax.management.DynamicMBean;

public interface SystemLoadMonitorMBean{
	public int getSystemLoad();
	public String getPredictionDate();
}
