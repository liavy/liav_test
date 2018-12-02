/*
 * Created on 13.12.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.security.core.jmx.impl;

import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxMapEntry;

/**
 * @author d031174
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JmxMapEntry extends ChangeableCompositeData implements
        IJmxMapEntry, Comparable {
    
	private static final long serialVersionUID = 5664806590019361305L;

    private static CompositeType myType;
    
    private boolean compareValue = false;

    public JmxMapEntry() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxMapEntry(CompositeData data) throws OpenDataException {
        super(data);
    }

    public JmxMapEntry(String key, String value) throws OpenDataException {
        this();
        this.setKey(key);
        this.setValue(value);
    }    
    
    public JmxMapEntry(String key, String value, boolean compareValue) throws OpenDataException {
        this();
        this.setKey(key);
        this.setValue(value);
        this.compareValue = compareValue;
    }
    
    public static IJmxMapEntry[] getArray(List entryList){
    	int size = entryList.size();
    	IJmxMapEntry[] result = new IJmxMapEntry[size];
    	Iterator it = entryList.iterator();
    	for (int i = 0; i < size; i++){
    		result[i] = (IJmxMapEntry)it.next();
    	}
    	return result;
    }
    
    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxMapEntry.class);
            myType = type;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     */
    public String getKey() {
        return (String) get(KEY);
    }

    public String setKey(String key) {
        return (String) set(KEY, key);
    }

    /*
     * (non-Javadoc)
     */
    public String getValue() {
        return (String) get(VALUE);
    }

    public String setValue(String value) {
        return (String) set(VALUE, value);
    }

    private static String[] getStringAttributes() {
        return new String[] { KEY, VALUE };
    }
    
    private void initialize(){
        this.setKey(CompanyPrincipalFactory.EMPTY);
        this.setValue(CompanyPrincipalFactory.EMPTY);
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("\n");
        res
                .append("***************************************************************************\n");
        res.append("* ");
        res.append(this.getClass().getName());
        res.append(" ");
        res.append((new java.util.Date()).toString());
        res.append("\n");

        String[] args = getStringAttributes();
        for (int i = 0; i < args.length; i++) {
            res.append("* ");
            res.append(args[i]);
            res.append(" : ");
            try {
                res.append(get(args[i]));
            } catch (InvalidKeyException e) {
                res.append("n/a");
            }
            res.append("\n");
        }

        res
                .append("***************************************************************************\n");
        return res.toString();
    }
    
    public int compareTo(Object o){
    	if (o instanceof IJmxMapEntry){
    		if (compareValue){
    			return this.getValue().compareTo(((IJmxMapEntry) o).getValue());
    		} else {
    			return this.getKey().compareTo(((IJmxMapEntry) o).getKey());
    		}
    	}
    	else return 0;
    }

}