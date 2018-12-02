package com.sap.security.core.jmx.impl;

import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxMessage;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.jmx.IJmxTableRow;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @
 */
public class JmxTable extends ChangeableCompositeData implements IJmxTable {
    
	private static final long serialVersionUID = 3893288750881192483L;

    private static Location myLoc = Location.getLocation(JmxTable.class);
    
    private static CompositeType myType;

    public JmxTable() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxTable(CompositeData data) throws OpenDataException {
        super(data);
    }

    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxTable.class);
            myType = type;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     */
    public IJmxTableRow[] getTableRows() {
        javax.management.openmbean.CompositeData[] cDataArray = (javax.management.openmbean.CompositeData[]) get(TABLEROWS);
        JmxTableRow[] result = new JmxTableRow[cDataArray.length];
        for (int i = 0; i < cDataArray.length; i++) {
            try {
                result[i] = new JmxTableRow(cDataArray[i]);
            } catch (OpenDataException e) {
                throw new java.lang.ClassCastException();
            }
        }
        return result;
    }

    public IJmxTableRow[] setTableRows(IJmxTableRow[] tableRows) {
        return (IJmxTableRow[]) set(TABLEROWS, tableRows);
    }

    public IJmxTableRow[] setTableRows(List tableRows) {
    	if (tableRows != null){
            JmxTableRow[] tableRowArray = new JmxTableRow[tableRows.size()];
            Iterator tableRowIt = tableRows.iterator();
            for (int i = 0; i < tableRowArray.length; i++) {
                tableRowArray[i] = (JmxTableRow) tableRowIt.next();
            }
            return (IJmxTableRow[]) set(TABLEROWS, tableRowArray);    		
    	}
    	return null;
    }

    /*
     * (non-Javadoc)
     */
    public int getState() {
        return ((Integer) get(STATE)).intValue();
    }

    public int setState(int state) {
        Integer result = (Integer) set(STATE, new Integer(state));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    /*
     * (non-Javadoc)
     */
    public int getSize() {
        return ((Integer) get(SIZE)).intValue();
    }

    public int setSize(int size) {
        Integer result = (Integer) set(SIZE, new Integer(size));
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    /*
     * (non-Javadoc)
     */
    public String getGuid() {
        return (String) get(GUID);
    }

    public String setGuid(String guid) {
        return (String) set(GUID, guid);
    }

    private static String[] getStringAttributes() {
        return new String[] { STATE, SIZE, GUID, TABLEROWS, MESSAGES };
    }
    
    private void initialize(){
        final String mn = "private void initialize()";
        this.setGuid(CompanyPrincipalFactory.EMPTY);
        this.setSize(0);
        this.setState(0);
        try {
            this.setTableRows(new JmxTableRow[]{new JmxTableRow()});
        } catch (OpenDataException e) {
            if (myLoc.beInfo()){
                myLoc.traceThrowableT(Severity.INFO, mn, e);   
            }
        }
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

    public static IJmxTable generatEmptyJmxTable() throws OpenDataException {
        JmxTable table = new JmxTable();
        JmxTableRow[] tableRows = new JmxTableRow[0];
        table.setTableRows(tableRows);
        return table;
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

    public IJmxMessage[] addMessages(List messages) {
    	if (messages != null){
            IJmxMessage[] messageArray = new IJmxMessage[messages.size()];
            Iterator messagesListIt = messages.iterator();
            for (int i = 0; i < messageArray.length; i++) {
                IJmxMessage tempMessage = (IJmxMessage) messagesListIt.next();
                messageArray[i] = tempMessage;
            }
            IJmxMessage[] oldMessageArray = this.getMessages();
            if (oldMessageArray != null && oldMessageArray.length > 0 && oldMessageArray[0] != null){
            	IJmxMessage[] newMessageArray = new IJmxMessage[messageArray.length + oldMessageArray.length];
            	System.arraycopy(oldMessageArray, 0, newMessageArray, 0, oldMessageArray.length);
            	System.arraycopy(messageArray, 0, newMessageArray, oldMessageArray.length, messageArray.length);
                return (IJmxMessage[]) set(MESSAGES, newMessageArray);
            }
    		return oldMessageArray;
    	}
    	return null;
    }
    
}