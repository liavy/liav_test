package com.sap.security.core.jmx.impl;

import java.util.Date;
import java.util.Locale;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.api.IMessage;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.util.imp.Util;

public class JmxMessage extends ChangeableCompositeData implements IJmxMessage {
    
	private static final long serialVersionUID = 6647335688217782418L;

	private static CompositeType myType;

    public JmxMessage() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxMessage(CompositeData data) throws OpenDataException {
        super(data);
    }

    public JmxMessage(int type, int lifeTime, int category, Date timeStamp,
			String message, String localizedMessage, String guid)
			throws OpenDataException {
        super(getMyType());
        initialize();
        this.setType(type);
        this.setLifeTime(lifeTime);
        this.setCategory(category);
        this.setTimeStamp(Util.getTime( timeStamp.getTime() ));
        this.setMessage(message);
        this.setLocalizedMessage(localizedMessage);
        this.setGuid(guid);
    }
    
    public JmxMessage(IMessage message, Locale locale)
			throws OpenDataException {
        super(getMyType());
        initialize();
        this.setType(message.getType());
        this.setLifeTime(message.getLifeTime());
        this.setCategory(message.getCategory());
        this.setTimeStamp(Util.getTime( message.getTimeStamp().getTime() ));
        this.setMessage(message.getMessage());
        this.setLocalizedMessage(message.getLocalizedMessage(locale));
        this.setGuid(message.getGuid());
    }
    
    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxMessage.class);
            myType = type;
        }
        return type;
    }

    public String getGuid() {
        return (String) get(GUID);
    }

    public String setGuid(String guid) {
        return (String) set(GUID, guid);
    }

	public String getLocalizedMessage() {
		return (String) get(LOCALIZEDMESSAGE);
	}
	
    public String setLocalizedMessage(String localizedMessage) {
        return (String) set(LOCALIZEDMESSAGE, localizedMessage);
    }

	public int getType() {
		return ((Integer) get(TYPE)).intValue();
	}
	
    public int setType(int type) {
        Integer result = (Integer) set(TYPE, new Integer(type));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

	public int getLifeTime() {
		return ((Integer) get(LIFETIME)).intValue();
	}
	
    public int setLifeTime(int lifeTime) {
        Integer result = (Integer) set(LIFETIME, new Integer(lifeTime));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

	public int getCategory() {
		return ((Integer) get(CATEGORY)).intValue();
	}

    public int setCategory(int category) {
        Integer result = (Integer) set(CATEGORY, new Integer(category));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }
	
	public String getTimeStamp() {
		return (String) get(TIMESTAMP);
	}
	
    public String setTimeStamp(String timeStamp) {
        return (String) set(TIMESTAMP, timeStamp);
    }

	public String getMessage() {
		return (String) get(MESSAGE);
	}

    public String setMessage(String message) {
        return (String) set(MESSAGE, message);
    }
    
    private static String[] getStringAttributes() {
        return new String[] { LOCALIZEDMESSAGE, MESSAGE, LIFETIME, CATEGORY, TYPE, TIMESTAMP, GUID };
    }
    
    private void initialize(){
        this.setLocalizedMessage(CompanyPrincipalFactory.EMPTY);
        this.setMessage(CompanyPrincipalFactory.EMPTY);
        this.setLifeTime(0);
        this.setCategory(0);
        this.setType(0);
        this.setTimeStamp(CompanyPrincipalFactory.EMPTY);
        this.setGuid(CompanyPrincipalFactory.EMPTY);
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
	
}