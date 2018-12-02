package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * This interface contains an attribute structure.
 */
public interface IJmxAttribute extends CompositeData {
	
    public static final String NAME = "Name";

    public static final String TEXT = "Text";

    public static final String VALUE = "Value";
    
	public static final String VALUES = "Values";

    public static final String BINARY = "Binary";

    public static final String BINARYVALUE = "BinaryValue";

    public static final String MODIFYABLE = "Modifyable";

    public static final String NAMESPACE = "Namespace";

    public static final String OPERATOR = "Operator";

    public static final String VISIBLE = "Visible";

    public static final String CASESENSITIVE = "CaseSensitive";

    public String getName();

    public String getText();

    public String getValue();
    
	public String[] getValues();

    public boolean getBinary();

    public String getBinaryValue();

    public boolean getModifyable();

    public String getNamespace();

    public int getOperator();

    public boolean getVisible();

    public boolean getCaseSensitive();

}