package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * @author D037363
 *
 */
public interface IJmxResult extends CompositeData {
	
	public static final String STATUS = "Status";
	public static final String MESSAGES = "Messages";
	
	public static final int STATUS_OK 		= 0x00;
	public static final int STATUS_NOT_OK 	= 0x01;
	public static final int STATUS_UNDEFINED = 0x02;
	
	public int getStatus();
	
	public IJmxMessage[] getMessages();
}
