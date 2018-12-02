package com.sap.engine.services.userstore;

import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.interfaces.security.SecurityModificationContextObject;
import com.sap.engine.interfaces.security.ModificationContext;

public class SecurityModificationContext implements ContextObject, SecurityModificationContextObject {

  private static SecurityModificationContext initialValue = null;
  private SecurityModificationContext parent = null;
  private Configuration config = null;
  private ConfigurationHandler configHandler = null;
  private ModificationContext modifications = null;

  public SecurityModificationContext() {
  }

  public SecurityModificationContext(Configuration configuration) {
    this.config = configuration;
  }

  public SecurityModificationContext(SecurityModificationContext parent) {
    this.parent = parent;
  }

  public ContextObject childValue(ContextObject parent, ContextObject child) {
    if (child == null) {
      child = new SecurityModificationContext((SecurityModificationContext) parent);
    } else {
      ((SecurityModificationContext) child).parent = (SecurityModificationContext) parent;
    }

    return child;
  }

  /**
   * Create new context object (or get it from the pool).
   *
   */
  public ContextObject getInitialValue() {
    if (initialValue != null) {
      return new SecurityModificationContext(initialValue);
    } else {
      return new SecurityModificationContext();
    }
  }

  /**
   * Release context object (or return it to the pool).
   *
   */
  public void empty() {
    config = null;
    configHandler = null;
    modifications = null;
    parent = null;
  }

  public void setConfiguration(Object configuration) {
    this.config = (Configuration) configuration;
  }

  /**
   *  Returns the thread's associated Configuration.
   *
   * @return  an instance of Configuration or null.
   */
  public Object getConfiguration() {
    return config;
  }
  
	/**
   *  Associates a ModificationContext to the thread.
   *
   * @param  modificationContext  an instance of ModificationContext or null.
   */
	public void setModificationContext(Object modificationContext) {
		this.modifications = (ModificationContext) modificationContext;
	}

	/**
   *  Returns the thread's associated ModificationContext.
   *
   * @return  an instance of ModificationContext or null.
   */
	public Object getModificationContext() {
		return modifications;
	}
	
	/**
   *  Associates an AppConfigurationHandler to the thread.
   *
   * @param  appConfigurationHandler  an instance of AppConfigurationHandler or null.
   */
  public void setAppConfigurationHandler(Object appConfigurationHandler) {
    this.configHandler = (ConfigurationHandler) appConfigurationHandler;
  }
  
	/**
   *  Returns the thread's associated AppConfigurationHandler.
   *
   * @return  an instance of AppConfigurationHandler or null.
   */
  public Object getAppConfigurationHandler() {
    return configHandler;
  }
}
