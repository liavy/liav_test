package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

public interface IJmxProperty extends CompositeData {

    public static final String ITEM_NAME                      = "Name";
    public static final String ITEM_GLOBAL_VALUE              = "GlobalValue";
    public static final String ITEM_DEFAULT_VALUE             = "DefaultValue";
    public static final String ITEM_DIFFERING_INSTANCE_VALUES = "DifferingInstanceValues";
    public static final String ITEM_IS_ONLINE_MODIFIABLE      = "IsOnlineModifiable";
    public static final String ITEM_IS_SECURE                 = "IsSecure";

    /**
     * Get the name of the property.
     * 
     * @return property name
     */
    public String getName();

    /**
     * Get the current value for the property which is set for the whole cluster.
     * 
     * Differing values for single instances can be retrieved using
     * {@link #getDifferingInstanceValues()}.
     * 
     * @return current global value (can be <code>null</code> or an empty string)
     */
    public String getGlobalValue();

    /**
     * Get the default value for the property.
     * 
     * @return default value (can be <code>null</code> or an empty string)
     */
    public String getDefaultValue();

    /**
     * Get differing property values for single instances.
     * 
     * @return Array of map entries covering all instances where the property differs from the
     *         global value. The instance name is the map key, the instance specific property value
     *         is the map value. If there are no differing values, the result can be
     *         <code>null</code> or an empty array.
     */
    public IJmxMapEntry[] getDifferingInstanceValues();

    /**
     * Check whether modifications of the property get effective immediately, i.e. without server
     * restart.
     * 
     * @return <code>true</code> if modifications get effective immediately, else
     *         <code>false</code>
     */
    public Boolean getIsOnlineModifiable();

    /**
     * Check whether the property contains sensitive information which must not be displayed or
     * logged.
     * 
     * @return <code>true</code> the property contains sensitive information, else
     *         <code>false</code>
     */
    public Boolean getIsSecure();

}
