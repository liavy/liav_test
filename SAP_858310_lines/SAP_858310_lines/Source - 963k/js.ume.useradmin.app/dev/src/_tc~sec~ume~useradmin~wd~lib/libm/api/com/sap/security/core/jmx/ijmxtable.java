package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * This interface contains a table structure.
 */
public interface IJmxTable extends CompositeData {

    public static final String STATE = "State";

    public static final String SIZE = "Size";

    public static final String GUID = "Guid";

    public static final String TABLEROWS = "TableRows";
    
    public static final String MESSAGES = "Messages";

    public int getState();

    public int getSize();

    public String getGuid();

    public IJmxTableRow[] getTableRows();

    public IJmxMessage[] getMessages();

}