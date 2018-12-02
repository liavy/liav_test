package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

public interface IJmxMap extends CompositeData {

    public static final String ENTRIES = "Entries";
    
    public static final String KEY = "Key";
    
    public static final String MESSAGES = "Messages";
	
	public String getKey() throws Exception;
	
	public IJmxMapEntry[] getEntries() throws Exception;
	
    public IJmxMessage[] getMessages();
}
