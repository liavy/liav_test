package com.sap.security.core.jmx.impl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.api.IMessage;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.jmx.IJmxResult;
import com.sap.security.core.jmx.IJmxState;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxResult extends ChangeableCompositeData implements IJmxResult {

	private static final long serialVersionUID = 4014628500119843326L;
	
	private static Location myLoc = Location.getLocation(JmxResult.class);

	private static CompositeType myType;

	public JmxResult() throws OpenDataException {
		super(getMyType());
		initialize();
	}

	public JmxResult(CompositeData data) {
		super(data);
	}

	public JmxResult(int status, IMessage[] messages, IJmxState jmxState) throws OpenDataException {
        super(getMyType());
	    
        IJmxMessage[] jmxMessages = new IJmxMessage[messages.length];
        for(int i = 0; i < messages.length; i++) {
            jmxMessages[i] = new JmxMessage(messages[i], jmxState.getLocale());
        }

        setStatus(status);
	    setMessages(jmxMessages);
	}

	private static CompositeType getMyType() throws OpenDataException {
		CompositeType type = myType;
		if (type == null) {
			type = OpenTypeFactory.getCompositeType(IJmxResult.class);
			myType = type;
		}
		return type;
	}
	
	private void initialize() {
		final String mn = "private void initialize()";
		this.setStatus(0);
		this.setMessages(new JmxMessage[0]);
        try {
            this.setMessages(new JmxMessage[]{new JmxMessage()});
        } catch (OpenDataException e) {
            if (myLoc.beInfo()){
                myLoc.traceThrowableT(Severity.INFO, mn, e);   
            }
        }
	}

	@Override
    public String toString() {

		StringBuffer res = new StringBuffer();
		res.append("\n");
		res.append("***************************************************************************\n");
		res.append("* ");
		res.append(this.getClass().getName());
		res.append(" ");
		res.append((new java.util.Date()).toString());
		res.append("\n");
		
		res.append("Status: ").append(getStatus());
		res.append("\n");
		
		IJmxMessage[] msgs = getMessages();
		for (int i = 0; i < msgs.length; i++) {
			res.append(msgs[i].toString());
			res.append("\n");
		}
		res.append("***************************************************************************\n");
		return res.toString();
	}

	public int getStatus() {
		return ((Integer) get(STATUS)).intValue();
	}

	public IJmxMessage[] getMessages() {
        CompositeData[] cDataArray = (CompositeData[]) get(MESSAGES);
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
	
	public void setStatus(int status) {
		set(STATUS,new Integer(status));
	}
	
	public void setMessages(IJmxMessage[] messages) {
		set(MESSAGES, messages);
	}
}