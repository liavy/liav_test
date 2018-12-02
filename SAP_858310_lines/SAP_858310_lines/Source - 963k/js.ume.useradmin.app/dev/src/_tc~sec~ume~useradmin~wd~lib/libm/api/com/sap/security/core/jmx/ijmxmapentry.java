package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * @author d031174
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IJmxMapEntry extends CompositeData {

    public static final String KEY = "Key";

    public static final String VALUE = "Value";

    public String getKey();

    public String getValue();

}