package com.sap.security.core.jmx.impl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxAttribute;

public class JmxAttribute extends ChangeableCompositeData implements
        IJmxAttribute {
    
	private static final long serialVersionUID = -2090798800786763402L;

    private static CompositeType myType;

    public JmxAttribute() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public static JmxAttribute[] generateJmxAttributes(CompositeData[] data) throws OpenDataException {
    	JmxAttribute[] attributes = new JmxAttribute[data.length];
    	for (int i = 0; i < attributes.length; i++){
            attributes[i] = new JmxAttribute(data[i]);
    	}
    	return attributes;
    }    
    
    public JmxAttribute(CompositeData data) throws OpenDataException {
        super(data);
    }
    
    public JmxAttribute(String namespace, String name, String value)
    throws OpenDataException {
        this();
    	setNamespace(namespace);
    	setName(name);
    	setValue(value);
    	setValues(new String[]{value});
    }

	public JmxAttribute(String namespace, String name, boolean value)
	throws OpenDataException {
		this();
		setNamespace(namespace);
		setName(name);
		setBinary(value);
	}

    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxAttribute.class);
            myType = type;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     */
    public String getName() {
        return (String) get(NAME);
    }

    public String setName(String name) {
        return (String) set(NAME, name);
    }

    /*
     * (non-Javadoc)
     */
    public String getText() {
        return (String) get(TEXT);
    }

    public String setText(String text) {
        return (String) set(TEXT, text);
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

	/*
	 * (non-Javadoc)
	 */
	public String[] getValues() {
		return (String[]) get(VALUES);
	}

	public String[] setValues(String[] values) {
		return (String[]) set(VALUES, values);
	}

    /*
     * (non-Javadoc)
     */
    public boolean getModifyable() {
        return ((Boolean) get(MODIFYABLE)).booleanValue();
    }

    public boolean setModifyable(boolean modifyable) {
        Boolean result = (Boolean) set(MODIFYABLE, new Boolean(modifyable));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * (non-Javadoc)
     */
    public String getNamespace() {
        return (String) get(NAMESPACE);
    }

    public String setNamespace(String namespace) {
        return (String) set(NAMESPACE, namespace);
    }

    /*
     * (non-Javadoc)
     */
    public int getOperator() {
        return ((Integer) get(OPERATOR)).intValue();
    }

    public int setOperator(int operator) {
        Integer result = (Integer) set(OPERATOR, new Integer(operator));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    /*
     * (non-Javadoc)
     */
    public boolean getVisible() {
        return ((Boolean) get(VISIBLE)).booleanValue();
    }

    public boolean setVisible(boolean visible) {
        Boolean result = (Boolean) set(VISIBLE, new Boolean(visible));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * (non-Javadoc)
     */
    public boolean getCaseSensitive() {
        return ((Boolean) get(CASESENSITIVE)).booleanValue();
    }

    public boolean setCaseSensitive(boolean caseSensitive) {
        Boolean result = (Boolean) set(CASESENSITIVE,
                new Boolean(caseSensitive));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * (non-Javadoc)
     */
    public String getBinaryValue() {
        return (String) get(BINARYVALUE);
    }

    public String setBinaryValue(String binaryValue) {
        return (String) set(BINARYVALUE, binaryValue);
    }

    /*
     * (non-Javadoc)
     */
    public boolean getBinary() {
        return ((Boolean) get(BINARY)).booleanValue();
    }

    public boolean setBinary(boolean binary) {
        Boolean result = (Boolean) set(BINARY, new Boolean(binary));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    private static String[] getStringAttributes() {
        return new String[] { NAME, TEXT, VALUE, VALUES, BINARY, BINARYVALUE,
                MODIFYABLE, NAMESPACE, OPERATOR, VISIBLE, CASESENSITIVE };
    }
    
    private void initialize(){
        this.setBinary(false);
        this.setBinaryValue(CompanyPrincipalFactory.EMPTY);
        this.setCaseSensitive(false);
        this.setModifyable(false);
        this.setName(CompanyPrincipalFactory.EMPTY);
        this.setNamespace(CompanyPrincipalFactory.EMPTY);
        this.setOperator(0);
        this.setText(CompanyPrincipalFactory.EMPTY);
        this.setValue(CompanyPrincipalFactory.EMPTY);
		this.setValues(new String[]{CompanyPrincipalFactory.EMPTY});
        this.setVisible(false);
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

}