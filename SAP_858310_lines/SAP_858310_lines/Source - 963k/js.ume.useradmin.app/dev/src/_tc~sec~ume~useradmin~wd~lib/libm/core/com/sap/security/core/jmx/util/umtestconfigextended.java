package com.sap.security.core.jmx.util;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.config.IConfigFilesChangedListener;
import com.sap.security.core.util.config.IPropertiesChangedListener;
import com.sap.security.core.util.config.IUMConfigExtended;
import com.sap.security.core.util.config.UMConfigurationException;
import com.sap.security.core.util.config.UMConfigurationUtils;
import com.sap.tc.logging.Location;

public class UMTestConfigExtended implements IUMConfigExtended{
	
    private static final Location _loc = Location.getLocation(UMTestConfigExtended.class);

	private Properties _staticProps;
	private Properties _dynSecProps;
	private Properties _dynProps;
	
	public UMTestConfigExtended() {
		this(new Properties());
	}

	public UMTestConfigExtended(Properties newProps) {
		_staticProps = (Properties) InternalUMFactory.getConfigExtended().getAllPropertiesStatic().clone();
		_dynSecProps = (Properties) InternalUMFactory.getConfigExtended().getAllSecurePropertiesDynamic().clone();
		_dynProps = (Properties) InternalUMFactory.getConfigExtended().getAllPropertiesDynamic().clone();
		_loc.infoT("Current configuration loaded");
		Enumeration newPropkeys = newProps.keys();
		while(newPropkeys.hasMoreElements()) {
			Object key = newPropkeys.nextElement();
			if (_staticProps.containsKey(key)) { 
				_staticProps.remove(key);
				_staticProps.setProperty((String) key, newProps.getProperty((String) key));
			}
			if (_dynSecProps.containsKey(key)) {
				_dynSecProps.remove(key);
				_dynSecProps.setProperty((String)key, newProps.getProperty((String) key));
			}
			if (_dynProps.containsKey(key)) {
				_dynProps.remove(key);
				_dynProps.setProperty((String) key, newProps.getProperty((String) key));
			}
			_loc.infoT("Property {0} updated successfully", new Object[] {key});
		}
		_loc.infoT("Current configuration updated with new UI properties");
	}

    public Properties getAllPropertiesStatic() {
    	return _staticProps;
    }

    public Properties getAllSecurePropertiesDynamic() {
    	return _dynSecProps;
    }
    
    public Properties getAllPropertiesDynamic() {
    	return _dynProps;
    }
    
    
    public boolean isPropertySecure(String property) {
    	return InternalUMFactory.getConfigExtended().isPropertySecure(property);
    }

    public String[] getAllConfigFileNames() {
    	return InternalUMFactory.getConfigExtended().getAllConfigFileNames();
    }

    public void registerPropertiesChangedListener(IPropertiesChangedListener listener) {
    	//TODO or do nothing
    }

    public void unregisterPropertiesChangedListener(IPropertiesChangedListener listener) {
    	//TODO or do nothing
    }

    public void registerConfigFileChangedListener(IConfigFilesChangedListener listener) {
    	//TODO or do nothing
    }

    public void unregisterConfigFileChangedListener(IConfigFilesChangedListener listener) {
    	//TODO or do nothing
    }
    
    public void forceRefresh() throws UMConfigurationException {
    	//TODO or do nothing
    }
    
    public final String getStringDynamic(String property) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!");

        return _dynProps.getProperty(property);
    }

    public final String getStringDynamic(String property, String defaultValue) {
        String propertyValue = getStringDynamic(property);
        if(propertyValue == null) return defaultValue;
        return propertyValue;
    }

    public final String getStringStatic(String property) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        return _staticProps.getProperty(property);
    }

    public final String getStringStatic(String property, String defaultValue) {
        String propertyValue = getStringStatic(property);
        if(propertyValue == null) return defaultValue;
        return propertyValue;
    }

    public final boolean getBooleanStatic(String property, boolean defaultValue) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        return UMConfigurationUtils.getBooleanFromString(property, getStringStatic(property), defaultValue);
    }

    public final boolean getBooleanDynamic(String property, boolean defaultValue) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        return UMConfigurationUtils.getBooleanFromString(property, getStringDynamic(property), defaultValue);
    }


    public final int getIntStatic(String property, int defaultValue) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        return UMConfigurationUtils.getIntFromString(property, getStringStatic(property), defaultValue);
    }

    public final int getIntDynamic(String property, int defaultValue) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        return UMConfigurationUtils.getIntFromString(property, getStringDynamic(property), defaultValue);
    }

    public final char[] getSecurePropertyDynamic(String property) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!");

        String value = _dynSecProps.getProperty(property);
        if(value != null) return value.toCharArray();
        return null;
    }

    public final char[] getSecurePropertyStatic(String property) {
        // Check arguments.
        if(property == null) throw new NullPointerException("The property name argument is null!" );

        String value = _dynSecProps.getProperty(property);
        if(value == null) return null;
        return value.toCharArray();
    }

    public final InputStream readConfigFile(String fileName) {
    	return InternalUMFactory.getConfigExtended().readConfigFile(fileName);
    }

    @SuppressWarnings("unused")
    public byte[] downloadConfiguration() throws UMConfigurationException {
    	// This method must not be used for test configurations.
    	return null;
    }
	
}
