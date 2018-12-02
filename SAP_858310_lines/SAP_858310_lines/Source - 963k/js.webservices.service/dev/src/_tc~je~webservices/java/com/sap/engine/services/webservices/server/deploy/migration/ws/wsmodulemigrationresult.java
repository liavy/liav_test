package com.sap.engine.services.webservices.server.deploy.migration.ws;

import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;

/**
 * Class containig the result from the migration - name of the module, directory of the module...
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */
public class WSModuleMigrationResult {
	
	private String moduleName;
	private String moduleDir;
	private WebservicesType webservicesJ2EEEngineDescriptor;
	private ConfigurationRoot configurationDescriptor;
	private MappingRules      mappingDescriptor;
   
	public ConfigurationRoot getConfigurationDescriptor() {
		return configurationDescriptor;
	}

	public MappingRules getMappingDescriptor() {
		return mappingDescriptor;
	}

	public String getModuleDir() {
		return moduleDir;
	}

	public String getModuleName() {
		return moduleName;
	}

	public WebservicesType getWebservicesJ2EEEngineDescriptor() {
		return webservicesJ2EEEngineDescriptor;
	}

	public void setConfigurationDescriptor(ConfigurationRoot root) {
		configurationDescriptor = root;
	}

	public void setMappingDescriptor(MappingRules rules) {
		mappingDescriptor = rules;
	}

	public void setModuleDir(String string) {
		moduleDir = string;
	}

	public void setModuleName(String string) {
		moduleName = string;
	}

	public void setWebservicesJ2EEEngineDescriptor(WebservicesType type) {
		webservicesJ2EEEngineDescriptor = type;
	}

}
