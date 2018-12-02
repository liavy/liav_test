package com.sap.security.core.jmx.impl;

import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxAttribute;
import com.sap.security.core.jmx.IJmxEntity;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxEntity extends ChangeableCompositeData implements IJmxEntity {

	private static final long serialVersionUID = 7419542174724511095L;

    private static Location myLoc = Location.getLocation(JmxEntity.class); 
    
    private static CompositeType myType;

    public JmxEntity() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxEntity(CompositeData data) throws OpenDataException {
        super(data);
    }

    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxEntity.class);
            myType = type;
        }
        return type;
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
    public String getClient() {
        return (String) get(CLIENT);
    }

    public String setClient(String client) {
        return (String) set(CLIENT, client);
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
    public String getType() {
        return (String) get(TYPE);
    }

    public String setType(String type) {
        return (String) set(TYPE, type);
    }

    /*
     * (non-Javadoc)
     */
    public IJmxAttribute[] getAttributes() {
        javax.management.openmbean.CompositeData[] cDataArray = (javax.management.openmbean.CompositeData[]) get(ATTRIBUTES);
        JmxAttribute[] result = new JmxAttribute[cDataArray.length];
        for (int i = 0; i < cDataArray.length; i++) {
            try {
                result[i] = new JmxAttribute(cDataArray[i]);
            } catch (OpenDataException e) {
                throw new java.lang.ClassCastException();
            }
        }
        return result;
    }

    public IJmxAttribute[] setAttributes(IJmxAttribute[] attributes) {
        return (IJmxAttribute[]) set(ATTRIBUTES, attributes);
    }

    public IJmxAttribute[] setAttributes(List attributes) {
        IJmxAttribute[] attributeArray = new IJmxAttribute[attributes.size()];
        Iterator attrListIt = attributes.iterator();
        for (int i = 0; i < attributeArray.length; i++) {
            IJmxAttribute tempAttribute = (IJmxAttribute) attrListIt.next();
            attributeArray[i] = tempAttribute;
        }
        return (IJmxAttribute[]) set(ATTRIBUTES, attributeArray);
    }

    private static String[] getStringAttributes() {
        return new String[] { TYPE, CLIENT, UNIQUEID, MODIFYABLE, ATTRIBUTES, MESSAGES};
    }
    
    private void initialize(){
        final String mn = "private void initialize()";
        try {
            this.setAttributes(new JmxAttribute[]{new JmxAttribute()});
        } catch (OpenDataException e) {
            if (myLoc.beInfo()){
                myLoc.traceThrowableT(Severity.INFO, mn, e);                
            }
        }
        this.setClient(CompanyPrincipalFactory.EMPTY);
        this.setModifyable(false);
        this.setType(CompanyPrincipalFactory.EMPTY);
        this.setUniqueId(CompanyPrincipalFactory.EMPTY);
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
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

    public IJmxMessage[] setMessages(List messages) {
    	if (messages != null){
            IJmxMessage[] messageArray = new IJmxMessage[messages.size()];
            Iterator messagesListIt = messages.iterator();
            for (int i = 0; i < messageArray.length; i++) {
                IJmxMessage tempMessage = (IJmxMessage) messagesListIt.next();
                messageArray[i] = tempMessage;
            }
            return (IJmxMessage[]) set(MESSAGES, messageArray);
    	}
    	return null;
    }
	
}