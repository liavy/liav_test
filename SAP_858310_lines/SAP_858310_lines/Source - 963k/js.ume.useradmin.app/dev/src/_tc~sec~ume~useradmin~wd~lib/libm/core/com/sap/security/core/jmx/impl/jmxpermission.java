/*
 * Created on 13.12.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.security.core.jmx.impl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxPermission;

/**
 * @author d031174
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JmxPermission extends ChangeableCompositeData implements
        IJmxPermission {
    
	private static final long serialVersionUID = 5210377963136745307L;

    private static CompositeType myType;

    public JmxPermission() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxPermission(CompositeData data) throws OpenDataException {
        super(data);
    }

    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxPermission.class);
            myType = type;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     */
    public String getClassName() {
        return (String) get(CLASSNAME);
    }

    public String setClassName(String className) {
        return (String) set(CLASSNAME, className);
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
    public String getType() {
        return (String) get(TYPE);
    }

    public String setType(String type) {
        return (String) set(TYPE, type);
    }

    /*
     * (non-Javadoc)
     */
    public String getAction() {
        return (String) get(ACTION);
    }

    public String setAction(String action) {
        return (String) set(ACTION, action);
    }

    /*
     * (non-Javadoc)
     */
    public String getAttribute() {
        return (String) get(ATTRIBUTE);
    }

    public String setAttribute(String attribute) {
        return (String) set(ATTRIBUTE, attribute);
    }

    /*
     * (non-Javadoc)
     */
    public String getUniqueId() {
        return (String) get(UNIQUEID);
    }

    public String setUniqueId(String uniqueId) {
        return (String) set(UNIQUEID, uniqueId);
    }

    /*
     * (non-Javadoc)
     */
    public boolean getPermission() {
        return ((Boolean) get(PERMISSION)).booleanValue();
    }

    public boolean setPermission(boolean permission) {
        Boolean result = (Boolean) set(PERMISSION, new Boolean(permission));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    private static String[] getStringAttributes() {
        return new String[] { CLASSNAME, NAME, TYPE, ACTION, ATTRIBUTE,
                UNIQUEID, PERMISSION };
    }
    
    private void initialize(){
        this.setAction(CompanyPrincipalFactory.EMPTY);
        this.setAttribute(CompanyPrincipalFactory.EMPTY);
        this.setClassName(CompanyPrincipalFactory.EMPTY);
        this.setName(CompanyPrincipalFactory.EMPTY);
        this.setPermission(false);
        this.setType(CompanyPrincipalFactory.EMPTY);
        this.setUniqueId(CompanyPrincipalFactory.EMPTY);
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