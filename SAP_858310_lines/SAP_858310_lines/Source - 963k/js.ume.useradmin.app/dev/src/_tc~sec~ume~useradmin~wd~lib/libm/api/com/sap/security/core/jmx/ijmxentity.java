package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * This interface contains an entity structure.
 */
public interface IJmxEntity extends CompositeData {

    public static final String TYPE = "Type";

    public static final String CLIENT = "Client";

    public static final String UNIQUEID = "UniqueId";

    public static final String MODIFYABLE = "Modifyable";

    public static final String ATTRIBUTES = "Attributes";
    
    public static final String MESSAGES = "Messages";

    public String getUniqueId();

    public String getClient();

    public boolean getModifyable();

    public String getType();

    public IJmxAttribute[] getAttributes();
    
    public IJmxMessage[] getMessages();

}