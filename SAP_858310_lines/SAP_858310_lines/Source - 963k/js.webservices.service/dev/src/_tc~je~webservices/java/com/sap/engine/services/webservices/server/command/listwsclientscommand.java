/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.server.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.WebServicesContainer;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.tc.logging.Location;

/**
 * Title: ListWSClientsCommand
 * Description: Class that is representing list_wsclients shell command from the webservices group  
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class ListWSClientsCommand implements Command {
  
  private String nl = SystemProperties.getProperty("line.separator");
  
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#exec(com.sap.engine.interfaces.shell.Environment, java.io.InputStream, java.io.OutputStream, java.lang.String[])
	 */
  public void exec(Environment environment, InputStream inputStream, OutputStream outputStream, String[] params) {
    try {
      if (params.length == 0) {
        outputStream.write(getAllServices(false).getBytes()); //$JL-I18N$       
      } else if (params.length == 1) {
        if (params[0].equals("-d")) {
          outputStream.write(getAllServices(true).getBytes()); //$JL-I18N$
        } else {
          outputStream.write(getServices(params[0], false).getBytes()); //$JL-I18N$
        }       
      } else if (params.length == 2) {
        if (params[1].equals("-d")) {
          outputStream.write(getServices(params[0], true).getBytes()); //$JL-I18N$
        } else {
          outputStream.write(getServices(params[0], params[1], false).getBytes()); //$JL-I18N$
        }
        
      } else if (params.length == 3 && params[2].equals("-d")) {
        outputStream.write(getServices(params[0], params[1], true).getBytes()); //$JL-I18N$
      } else {
        outputStream.write(getHelpMessage().getBytes()); //$JL-I18N$
      }
      outputStream.flush();
    } catch (IOException e) {
      Location serverLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      serverLocation.catching("Unexpected exception occurred in calling list_wsclients command.", e);
      try {
        environment.getErrorStream().write(("Unexpected exception occurred in calling list_wsclients command: " + e.getMessage()).getBytes()); //$JL-I18N$
      } catch (IOException exc) {
        serverLocation.catching("IOException occurred, printing error message while executing list_wsclients command.", exc);
      }
    }   
  }

  private String getAllServices(boolean withDetails) {
    String resultString = "";
    ConfigurationContext configurationContext = WebServicesContainer.getServiceRefContext().getConfigurationContext();
    
    Hashtable appConfigContexts = configurationContext.getApplicationConfigurationContexts();
    if (appConfigContexts == null || appConfigContexts.size() == 0) {
      resultString = "No wsclients have been registered/started."  + nl + nl;
    }
    Enumeration enum1 = appConfigContexts.keys();
    String applicationName;
    while (enum1.hasMoreElements()) {
      applicationName = (String)enum1.nextElement();
      resultString += getServices(applicationName, withDetails);     
    }     
    
    return resultString;
  }
  
  private String getServices(String applicationName, boolean withDetails) {    
    ApplicationConfigurationContext applicationContext = (ApplicationConfigurationContext)WebServicesContainer.getServiceRefContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);   
    if (applicationContext == null) {
      if (applicationName.startsWith("-")) {
       return getHelpMessage();
      } else {
        return "Webservice client with Application name " + applicationName + " does not exists." + nl + nl;
      }
    }
    
    String resultString = "Application: " + applicationName + nl;    
    Service[] services = applicationContext.getServiceRegistry().listServices();
    for (int i = 0; i < services.length; i++) {
      resultString += "Service: " + services[i].getName() + nl;
      resultString += "Service contextRoot: " + services[i].getServiceData().getContextRoot() + nl;
      
      if (withDetails) {
        resultString += "Service service-mapping-id: " + services[i].getServiceMappingId() + nl;
      }
      
      BindingData[] bindingData = services[i].getServiceData().getBindingData();
      if (bindingData != null && bindingData.length != 0) {
        for (int j = 0; j < bindingData.length; j++) {
          resultString += "BindingData: " + bindingData[j].getBindingName() + nl;
          resultString += "BindingData url: " + bindingData[j].getUrl() + nl;
          
          if (withDetails) {
            resultString += "BindingData interface-id: " + bindingData[j].getInterfaceId() + nl;
            resultString += "BindingData interafce-mapping-id: " + bindingData[j].getInterfaceMappingId() + nl;
          }
          
        }
      }
    } 
    return resultString + nl;
  }
  
  private String getServices(String applicationName, String serviceName, boolean withDetails) {
    ApplicationConfigurationContext applicationContext = (ApplicationConfigurationContext)WebServicesContainer.getServiceRefContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);  
    if (applicationContext == null) {
      if (applicationName.startsWith("-")) {
       return getHelpMessage();
      } else {
        return "Webservice client with Application name " + applicationName + " does not exists." + nl + nl;
      }
    }
    
    if (serviceName.startsWith("-")) {
      return getHelpMessage();
    }
    
    String resultString = "Application: " + applicationName + nl;
    Service[] services = applicationContext.getServiceRegistry().listServices();
    boolean serviceExists = false;
    for (int i = 0; i < services.length; i++) {
      if (services[i].getName().trim().equals(serviceName)) {
        serviceExists = true;
        
        resultString += "Service: " + services[i].getName() + nl;
        resultString += "Service contextRoot: " + services[i].getServiceData().getContextRoot() + nl;
        
        if (withDetails) {
          resultString += "Service service-mapping-id: " + services[i].getServiceMappingId() + nl;
        }
        
        BindingData[] bindingData = services[i].getServiceData().getBindingData();
        if (bindingData != null && bindingData.length != 0) {
          for (int j = 0; j < bindingData.length; j++) {
            resultString += "BindingData: " + bindingData[j].getBindingName() + nl;
            resultString += "BindingData url: " + bindingData[j].getUrl() + nl;
            
            if (withDetails) {
              resultString += "BindingData interface-id: " + bindingData[j].getInterfaceId() + nl;
              resultString += "BindingData interafce-mapping-id: " + bindingData[j].getInterfaceMappingId() + nl;
            }
            
          }
        }
      }
    } 
    if (serviceExists) {
      return resultString + nl;
    } else {
      return "Application " + applicationName + " does not have Service with name " + serviceName + "." + nl + nl;
    }  
  }


	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#getName()
	 */
	public String getName() {
		return "list_wsclients";
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#getGroup()
	 */
	public String getGroup() {
		return "webservices";
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#getSupportedShellProviderNames()
	 */
	public String[] getSupportedShellProviderNames() {
    return new String[]{"InQMyShell"};
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#getHelpMessage()
	 */
	public String getHelpMessage() {
    String nl = SystemProperties.getProperty("line.separator");
    String helpMessage = "For every application prints Application name, Service name, Service service-mapping-id, Service contextRoot, BindingData name, BindingData url" + nl + nl
                         + "USAGE    : " + getName() + " [<application name>] [<service name>] -[<parameter>] "  + nl
                         + "Parameter: " + nl
                         + " d : lists additional information for the specified application: " + nl
                         + " Service name, Service service-mapping-id, Service contextRoot, BindingData name, BindingData url, BindingData interface-id, BindingData interface-mapping-id." + nl + nl;

   return helpMessage;
  }

}
