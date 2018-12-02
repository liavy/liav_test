package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

public interface IJmxPermission extends CompositeData {

    public static final String CLASSNAME = "ClassName";

    public static final String NAME = "Name";

    public static final String TYPE = "Type";

    public static final String ACTION = "Action";

    public static final String ATTRIBUTE = "Attribute";

    public static final String UNIQUEID = "UniqueId";

    public static final String PERMISSION = "Permission";

    public String getClassName();
    
    public String getName();

    public String getAction();
    
    public String getType();

    public String getAttribute();

    public String getUniqueId();

    public boolean getPermission();

}