package com.sap.security.core.jmx.impl;

import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxMapEntry;
import com.sap.security.core.jmx.IJmxProperty;
import com.sap.security.core.util.config.IProperty;

public class JmxProperty extends ChangeableCompositeData implements IJmxProperty {

    private static final long    serialVersionUID = 3285337804986493502L;

    private static CompositeType myType;

    public JmxProperty(CompositeData data) {
        super(data);
    }

    public JmxProperty(IProperty property) throws OpenDataException {
        super(getMyType());

        set(ITEM_NAME, property.getName());

        String globalValue = property.getGlobalValue();
        String defaultValue = property.getDefaultValue();
        Map<String, String> differingInstanceValues = property.getDifferingInstanceValues();

        if (globalValue == null)
            globalValue = CompanyPrincipalFactory.EMPTY;
        if (defaultValue == null)
            globalValue = CompanyPrincipalFactory.EMPTY;

        IJmxMapEntry[] differingInstanceValuesJmxMap;
        if (differingInstanceValues != null)
            differingInstanceValuesJmxMap = JmxUtils.convertMapToJmxMapEntries(differingInstanceValues);
        else
            differingInstanceValuesJmxMap = new IJmxMapEntry[0];

        set(ITEM_GLOBAL_VALUE, globalValue);
        set(ITEM_DEFAULT_VALUE, defaultValue);
        set(ITEM_DIFFERING_INSTANCE_VALUES, differingInstanceValuesJmxMap);
        set(ITEM_IS_ONLINE_MODIFIABLE, Boolean.valueOf(property.isOnlineModifiable()));
        set(ITEM_IS_SECURE, Boolean.valueOf(property.isSecure()));
    }

    public String getName() {
        return (String) get(ITEM_NAME);
    }

    public String getGlobalValue() {
        return (String) get(ITEM_GLOBAL_VALUE);
    }

    public String getDefaultValue() {
        return (String) get(ITEM_DEFAULT_VALUE);
    }

    public IJmxMapEntry[] getDifferingInstanceValues() {
        return (IJmxMapEntry[]) get(ITEM_DIFFERING_INSTANCE_VALUES);
    }

    public Boolean getIsOnlineModifiable() {
        return (Boolean) get(ITEM_IS_ONLINE_MODIFIABLE);
    }

    public Boolean getIsSecure() {
        return (Boolean) get(ITEM_IS_SECURE);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("************************************************************");
        buffer.append("Type: ").append(this.getClass().getName()).append('\n');
        buffer.append("Property name: ").append(getName()).append('\n');

        boolean isSecure = getIsSecure().booleanValue();

        buffer.append("Global value: ");
        if (! isSecure)
            buffer.append('"').append(getGlobalValue()).append('"');
        else
            buffer.append("(secure property)");
        buffer.append('\n');

        buffer.append("Default value: ");
        if (! isSecure)
            buffer.append('"').append(getDefaultValue()).append('"');
        else
            buffer.append("(secure property)");
        buffer.append('\n');

        buffer.append("Differing instance values: ");
        IJmxMapEntry[] differingInstanceValues = getDifferingInstanceValues();
        if (differingInstanceValues != null) {
            if (! isSecure) {
                for (int i = 0; i < differingInstanceValues.length; i++) {
                    IJmxMapEntry currentInstanceEntry = differingInstanceValues[i];
                    buffer.append("instance ")
                          .append(currentInstanceEntry.getKey())
                          .append(" -> \"")
                          .append(currentInstanceEntry.getValue())
                          .append('"');
                    if (i < differingInstanceValues.length - 1)
                        buffer.append(", ");
                }
            }
            else
                buffer.append("(secure property)");
        }
        else {
            buffer.append("(none)");
        }
        buffer.append('\n');

        buffer.append("Onlinemodifiable?: ").append(getIsOnlineModifiable()).append('\n');
        buffer.append("Secure?: ").append(isSecure).append('\n');
        buffer.append("************************************************************");

        return buffer.toString();
    }

    private static CompositeType getMyType() throws OpenDataException {
        if (myType == null) {
            myType = OpenTypeFactory.getCompositeType(IJmxProperty.class);
        }
        return myType;
    }

}
