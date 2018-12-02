/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.frame.core.configuration.CustomParameterMappings;
import com.sap.engine.lib.descriptors.webj2eeengine.FailOverAlertType;
import com.sap.engine.lib.descriptors.webj2eeengine.FlagType;
import com.sap.engine.lib.descriptors.webj2eeengine.LoginModuleType;
import com.sap.engine.lib.descriptors.webj2eeengine.OptionType;
import com.sap.engine.lib.descriptors.webj2eeengine.SecurityRoleMapType;
import com.sap.engine.lib.descriptors.webj2eeengine.WebJ2EeEngineType;
import com.sap.engine.lib.descriptors5.javaee.EjbLocalRefType;
import com.sap.engine.lib.descriptors5.javaee.EjbRefType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationRefType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationType;
import com.sap.engine.lib.descriptors5.javaee.ResourceEnvRefType;
import com.sap.engine.lib.descriptors5.javaee.ResourceRefType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.processor.SchemaProcessor;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

/**
 * The WebDeploymentDescriptor conveys the configuration information of
 * a web application between Developers, Assemblers, and Deployers.
 * This class is a facade, which is used to unify the presentation of
 * the two object trees representing the WEB.XML and the WEB-J2EE-ENGINE.XML.
 *
 * @author Georgi Gerginov
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class WebDeploymentDescriptor extends WebAppTypeFacade {
  static final long serialVersionUID = -5105736464151497736L;

  //Additional descriptor defaults
  public static final int FAILOVER_TIMEOUT_DEFAULT = 2 * 60000;
  public static final String FAILOVER_MESSAGE_DEFAULT = "";
  public static final int MAX_SESSIONS_DEFAULT = -1;
  public static final boolean URL_SESSION_TRACKING_DEFAULT = false;

  //WEB-J2EE-ENGINE.XML generated representation
  private WebJ2EeEngineType webJ2EEEngine = null;
  
  //flags that indicate if the corresponding tags are actually configured in the descriptor (not set by default)
  private boolean isMaxSessionsTagConfigured = false;
  private boolean isSessCookieConfigured = false;
  private boolean isAppCookieConfigured = false;
  
  
  private boolean hasSubstitutionVariables = false;

  /**
   * Creates a new WebDeploymentDescriptor object and initializes the default values first.
   */
  public WebDeploymentDescriptor() {
    this(true);
  }

  /**
   * Creates a new WebDeploymentDescriptor object with or without default values initialized,
   * depending on the <code>loadDefaultValues</code> parameter.
   *
   * @param loadDefaultValues if true, load the default values from the schema if not set;
   *                          if false leave them unset
   */
  public WebDeploymentDescriptor(boolean loadDefaultValues) {
    webJ2EEEngine = new WebJ2EeEngineType();

    if (loadDefaultValues) {
      //Initializing the default values
      initializeDefaults();
    }
  }

  /**
   * Initializing the default values.
   */
  private void initializeDefaults() {
    if (webJ2EEEngine.getFailOverAlert() == null) {
      FailOverAlertType failoverAlert = new FailOverAlertType();
      failoverAlert.setMessage(FAILOVER_MESSAGE_DEFAULT);
      failoverAlert.setTimeout(new Integer(FAILOVER_TIMEOUT_DEFAULT));
      webJ2EEEngine.setFailOverAlert(failoverAlert);
    }
  }//end of constructor

  /**
   * This method returns the object representation of the Web-j2ee-engine.xml.
   *
   * @return WebJ2EeEngineType  object representation of the Web-j2ee-engine.xml
   */
  public WebJ2EeEngineType getWebJ2EEEngine() {
    return webJ2EEEngine;
  }//end of getWebJ2EEEngine()

  /**
   * This method initializes the object representation of the Web-j2ee-engine.xml.
   *
   * @param type WebJ2EeEngineType object representation of the Web-j2ee-engine.xml
   */
  public void setWebJ2EEEngine(WebJ2EeEngineType type) {
    if (type == null) {
      return;
    } else {
      webJ2EEEngine = type;
    }
  }//end of setWebJ2EEEngine(WebJ2EeEngineType type)

  /** Marshall/Unmarshall methods */

  /**
   * This method loads the descriptor from the given input streams
   *
   * @param  standartDescriptor InputStream from the web.xml file
   * @param  additionalDescriptor InputStream from the web-j2ee-engine.xml file
   * @param   validation boolean switch validation on/off
   */
  public void loadDescriptorFromStreams(InputStream standartDescriptor, InputStream additionalDescriptor, boolean validation) throws Exception {
    InputStream substituted = null;
    if (standartDescriptor != null) {
      substituted = substituteParamStream(standartDescriptor);
    }

    InputStream substitutedAdditional = null;
    if (additionalDescriptor != null) {
      substitutedAdditional = substituteParamStream(additionalDescriptor);
    }

    //loads web.xml
    if (substituted != null) {
      SchemaProcessor webSchemaProcessor = ServiceContext.getServiceContext().getDeployContext().getWebSchemaProcessor();
      synchronized (webSchemaProcessor) {
        if (!validation) {
          webSchemaProcessor.switchOffValidation();
        }
        setWebApp((WebAppType) webSchemaProcessor.parse(substituted));
        if (!validation) {
          webSchemaProcessor.switchOnValidation();
        }
      }
    }

    //loads web-j2ee-engine.xml
    if (substitutedAdditional != null) {
      SchemaProcessor webJ2eeSchemaProcessor = ServiceContext.getServiceContext().getDeployContext().getWebJ2eeSchemaProcessor();
      synchronized (webJ2eeSchemaProcessor) {
        if (!validation) {
          webJ2eeSchemaProcessor.switchOffValidation();
        }
        setWebJ2EEEngine((WebJ2EeEngineType) webJ2eeSchemaProcessor.parse(substitutedAdditional));
        if (!validation) {
          webJ2eeSchemaProcessor.switchOnValidation();
        }
      }
    }
  }//end of loadDescriptorFromStreams(InputStream standartDescriptor, InputStream additionalDescriptor, boolean validation)

  private InputStream substituteParamStream(InputStream io) throws DeploymentException {
    InputStream substituted = null;
    ConfigurationHandlerFactory configurationHandlerFactory = ServiceContext.getServiceContext().getConfigurationHandlerFactory();

    CustomParameterMappings customParameterMappings = null;
    try {
      customParameterMappings = configurationHandlerFactory.getConfigurationHandler().getCustomParameterMappings();
    } catch (ConfigurationException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_HANDLER, e);
    }

    if (customParameterMappings != null) {
      Vector substitutedCustomParams = new Vector();
      Vector substitutedSystemParams = new Vector();
      try {
        substituted = customParameterMappings.substituteParamStream(io, substitutedCustomParams, substitutedSystemParams);
        if (substitutedCustomParams.size() > 0 || substitutedSystemParams.size() > 0) {
          hasSubstitutionVariables = true;
        }
        return substituted;
      } catch (Exception e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_SUBSTITUTE_STREAM, e);
      }
    } else {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_HANDLER);
    }
  }//end of substituteParamStream(InputStream io)

  /**
   * Writes the part of this class which represents the standard xml file
   * into the given  stream
   *
   * @param stream OutputStream in which the descriptor is written
   */
  public void writeStandartDescriptorToStream(OutputStream stream) throws RemoteException {
    SchemaProcessor schem = ServiceContext.getServiceContext().getDeployContext().getWebSchemaProcessor();
    synchronized (schem) {
      schem.build(getWebApp(), stream);
    }
  }//end of writeStandartDescriptorToStream(OutputStream stream)

  /**
   * Writes the part of this class which represents the additional xml file
   * into the given  stream
   *
   * @param stream OutputStream in which the descriptor is written
   */
  public void writeAdditionalDescriptorToStream(OutputStream stream) throws RemoteException {
    SchemaProcessor schem = ServiceContext.getServiceContext().getDeployContext().getWebJ2eeSchemaProcessor();
    synchronized (schem) {
      schem.build(getWebJ2EEEngine(), stream);
    }
  }//end of writeAdditionalDescriptorToStream(OutputStream stream)

  /** Methods for mapping between two XMLs*/

  /**
   * Gets resource reference from the additional xml
   *
   * @param resourceRef  resource reference from the standard xml file
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType getResourceReferenceFromAdditional(ResourceRefType resourceRef) {
    if (resourceRef == null || resourceRef.getResRefName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType[] refs = getWebJ2EEEngine().getResourceRef();
    if (refs == null || refs.length == 0) {
      return null;
    }

    String name = resourceRef.getResRefName().get_value();
    for (int i = 0; i < refs.length; i++) {
      if (refs[i].getResRefName().equals(name)) {
        return refs[i];
      }
    }

    return null;
  }//end of getResourceReferenceFromAdditional(ResourceRefType resourceRef)

  /**
   * Gets security role info from the additional xml
   *
   * @return SecurityRoles array with security roles
   */
  public SecurityRoleMapType getSecurityRoleFromAdditional(SecurityRoleType role) {
    if (role == null || role.getRoleName() == null) {
      return null;
    }

    SecurityRoleMapType[] addRoles = getWebJ2EEEngine().getSecurityRoleMap();
    if (addRoles == null || addRoles.length == 0) {
      return null;
    }

    String roleName = role.getRoleName().get_value();
    for (int i = 0; i < addRoles.length; i++) {
      if (addRoles[i].getRoleName().equals(roleName)) {
        return addRoles[i];
      }
    }

    return null;
  }//end of getSecurityRoleFromAdditional(SecurityRoleType role)

  /**
   * Gets login modules information in an appropriate form for the security service
   *
   * @return AppConfigurationEntry array with login modules
   */
  public AppConfigurationEntry[] getLoginModules() {

    if (getWebJ2EEEngine().getLoginModuleConfiguration() != null
      && getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack() != null
      && getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule() != null
      && getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule().length > 0) {
      LoginModuleType[] modules = getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule();
      AppConfigurationEntry[] temp = new AppConfigurationEntry[modules.length];
      for (int i = 0; i < modules.length; i++) {
        LoginModuleType m = modules[i];

        AppConfigurationEntry.LoginModuleControlFlag flag = null;
        if (FlagType._SUFFICIENT.equalsIgnoreCase(m.getFlag().getValue())) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
        } else if (FlagType._REQUIRED.equalsIgnoreCase(m.getFlag().getValue())) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
        } else if (FlagType._REQUISITE.equalsIgnoreCase(m.getFlag().getValue())) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
        } else if (FlagType._OPTIONAL.equalsIgnoreCase(m.getFlag().getValue())) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
        } else {
          throw new WebIllegalArgumentException(WebIllegalArgumentException.INCORRECT_VALUE_FOR_TAG,
            new String[]{m.getFlag().getValue(), Constants.FLAG_TAG, FlagType._SUFFICIENT + ", " + FlagType._REQUIRED + ", " + FlagType._REQUISITE + ", " + FlagType._OPTIONAL});
        }

        HashMap<String, String> opt = new HashMap<String, String>();
        if (m.getOptions() != null && m.getOptions().getOption() != null && m.getOptions().getOption().length > 0) {
          OptionType[] options = m.getOptions().getOption();
          for (int j = 0; j < options.length; j++) {
            opt.put(options[j].getName(), options[j].getValue());
          }
        }

        temp[i] = new AppConfigurationEntry(m.getLoginModuleName(), flag, opt);
      }

      return temp;
    }

    return null;
  }//end of getLoginModules()

  /**
   * Gets JNDI name for a given ejb reference
   *
   * @param ejbRef ejb reference from the standard xml
   * @return EJBRefType ejb reference from the additional xml
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType getEjbRefFromAdditional(EjbRefType ejbRef) {
    if (ejbRef == null || ejbRef.getEjbRefName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType[] additionalRefs = getWebJ2EEEngine().getEjbRef();
    if (additionalRefs == null || additionalRefs.length == 0) {
      return null;
    }

    String ejbRefName = ejbRef.getEjbRefName().get_value();
    for (int i = 0; i < additionalRefs.length; i++) {
      if (additionalRefs[i].getEjbRefName().equals(ejbRefName)) {
        return additionalRefs[i];
      }
    }

    return null;
  }//end of getEjbRefFromAdditional(EjbRefType ejbRef)

  /**
   * Gets JNDI name for a given local ejb reference
   *
   * @param ejbRef ejb local reference from the standard xml
   * @return EJBLocalRefType ejb local reference from the additional xml
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType getEjbLocalRefFromAdditional(EjbLocalRefType ejbRef) {
    if (ejbRef == null || ejbRef.getEjbRefName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType[] additionalRefs = getWebJ2EEEngine().getEjbLocalRef();
    if (additionalRefs == null || additionalRefs.length == 0) {
      return null;
    }

    String ejbRefName = ejbRef.getEjbRefName().get_value();
    for (int i = 0; i < additionalRefs.length; i++) {
      if (additionalRefs[i].getEjbRefName().equals(ejbRefName)) {
        return additionalRefs[i];
      }
    }

    return null;
  }//end of getEjbLocalRefFromAdditional(EjbLocalRefType ejbRef)

  /**
   * Gets resource environment reference from the additional xml
   *
   * @param resRef resource environment reference from the standard xml file
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType getResEnvRefFromAdditional(ResourceEnvRefType resRef) {
    if (resRef == null || resRef.getResourceEnvRefName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType[] additionalRefs = getWebJ2EEEngine().getResourceEnvRef();
    if (additionalRefs == null || additionalRefs.length == 0) {
      return null;
    }

    String resourceEnvRefName = resRef.getResourceEnvRefName().get_value();
    for (int i = 0; i < additionalRefs.length; i++) {
      if (additionalRefs[i].getResourceEnvRefName().equals(resourceEnvRefName)) {
        return additionalRefs[i];
      }
    }

    return null;
  }//end of getResEnvRefFromAdditional(ResourceEnvRefType resRef)

  /**
   * Gets message destination reference from the additional xml
   *
   * @param messageDestinationRef message destination reference from the standard xml file
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType getMsgDestinationRefFromAdditional(MessageDestinationRefType messageDestinationRef) {
    if (messageDestinationRef == null || messageDestinationRef.getMessageDestinationRefName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationRefType[] additionalRefs = getWebJ2EEEngine().getMessageDestinationRef();
    if (additionalRefs == null || additionalRefs.length == 0) {
      return null;
    }

    String messageDestinationRefName = messageDestinationRef.getMessageDestinationRefName().get_value();
    for (int i = 0; i < additionalRefs.length; i++) {
      if (additionalRefs[i].getMessageDestinationRefName().equals(messageDestinationRefName)) {
        return additionalRefs[i];
      }
    }

    return null;
  }//end of getMsgDestinationRefFromAdditional(MessageDestinationRefType messageDestinationRef)

  /**
   * Gets message destination from the additional xml
   *
   * @param messageDestination message destination from the standard xml file
   */
  public com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType getMsgDestinationFromAdditional(MessageDestinationType messageDestination) {
    if (messageDestination == null || messageDestination.getMessageDestinationName() == null) {
      return null;
    }

    com.sap.engine.lib.descriptors.webj2eeengine.MessageDestinationType[] additionalRefs = getWebJ2EEEngine().getMessageDestination();
    if (additionalRefs == null || additionalRefs.length == 0) {
      return null;
    }

    String messageDestinationName = messageDestination.getMessageDestinationName().get_value();
    for (int i = 0; i < additionalRefs.length; i++) {
      if (additionalRefs[i].getMessageDestinationName().equals(messageDestinationName)) {
        return additionalRefs[i];
      }
    }

    return null;
  }//end of getMsgDestinationFromAdditional(MessageDestinationType messageDestination) 

  public boolean hasSubstitutionVariables() {
    return hasSubstitutionVariables;
  }//end of hasSubstitutionVariables()

public boolean isMaxSessionsTagConfigured() {
	return isMaxSessionsTagConfigured;
}

public void setMaxSessionsTagConfigured(boolean isMaxSessionsTagConfigured) {
	this.isMaxSessionsTagConfigured = isMaxSessionsTagConfigured;
}

public boolean isSessCookieConfigured() {
	return isSessCookieConfigured;
}

public void setSessCookieConfigured(boolean isSessCookieConfigured) {
	this.isSessCookieConfigured = isSessCookieConfigured;
}

public boolean isAppCookieConfigured() {
	return isAppCookieConfigured;
}

public void setAppCookieConfigured(boolean isAppCookieConfigured) {
	this.isAppCookieConfigured = isAppCookieConfigured;
}

}//end of class
