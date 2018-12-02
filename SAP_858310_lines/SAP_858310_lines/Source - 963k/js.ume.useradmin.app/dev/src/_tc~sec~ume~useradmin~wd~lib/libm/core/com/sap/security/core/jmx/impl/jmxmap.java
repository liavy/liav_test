package com.sap.security.core.jmx.impl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxMap;
import com.sap.security.core.jmx.IJmxMapEntry;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxMap extends ChangeableCompositeData implements IJmxMap {
	
	private static final long serialVersionUID = 8623330948575495053L;

    private static Location myLoc = Location.getLocation(JmxEntity.class); 
    
    private static CompositeType myType;

    public JmxMap() throws OpenDataException {
        super(getMyType());
        initialize();
    }
    
    public JmxMap(CompositeData data) throws OpenDataException{
    	super(data);
    }
	
	public String getKey() throws Exception {
		return (String) get(KEY);
	}
	
	public void setKey(String key) {
		set(KEY, key);
	}

	public IJmxMapEntry[] getEntries() throws Exception {
        javax.management.openmbean.CompositeData[] cDataArray = (javax.management.openmbean.CompositeData[]) get(ENTRIES);
        JmxMapEntry[] result = new JmxMapEntry[cDataArray.length];
        for (int i = 0; i < cDataArray.length; i++) {
            try {
                result[i] = new JmxMapEntry(cDataArray[i]);
            } catch (OpenDataException e) {
                throw new java.lang.ClassCastException();
            }
        }
		return result;
	}
	
	public void setEntries(IJmxMapEntry[] entries){
		set(ENTRIES, entries);
	}
	
	public IJmxMessage[] getMessages() {
        javax.management.openmbean.CompositeData[] cDataArray = (javax.management.openmbean.CompositeData[]) get(MESSAGES);
        JmxMessage[] result = new JmxMessage[cDataArray.length];
        for (int i = 0; i < cDataArray.length; i++) {
            try {
                result[i] = new JmxMessage(cDataArray[i]);
            } catch (OpenDataException e) {
                throw new java.lang.ClassCastException();
            }
        }
        return result;
	}

    public IJmxMessage[] setMessages(IJmxMessage[] messages) {
        return (IJmxMessage[]) set(MESSAGES, messages);
    }
	
    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxMap.class);
            myType = type;
        }
        return type;
    }
    
    private void initialize(){
        final String mn = "private void initialize()";
        try {
            this.setEntries(new IJmxMapEntry[]{new JmxMapEntry()});
        } catch (OpenDataException e) {
            if (myLoc.beInfo()){
                myLoc.traceThrowableT(Severity.INFO, mn, e);                
            }
        }
        this.setKey(CompanyPrincipalFactory.EMPTY);
    }
    
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("***************************************************************************\n");
        res.append("* ");
        res.append(this.getClass().getName());
        res.append(" ");
        res.append((new java.util.Date()).toString());
        res.append("\n");
        try {
	        res.append("Key: ").append(getKey());
	        res.append("\n");
	        res.append("*Entries: **\n");
	        IJmxMapEntry[] args = getEntries();
	        for (int i = 0; i < args.length; i++) {
	            res.append(args[i].toString());
	            res.append("\n");
	        }
        }
        catch (Exception e) {
        	myLoc.traceThrowableT(Severity.ERROR, "Exception when calling toString()", e);
        	res.append("Exception when calling to String ").append(e.getMessage());
        	res.append("\n");
        }
        res.append("***************************************************************************\n");
        return res.toString();
    }

}
