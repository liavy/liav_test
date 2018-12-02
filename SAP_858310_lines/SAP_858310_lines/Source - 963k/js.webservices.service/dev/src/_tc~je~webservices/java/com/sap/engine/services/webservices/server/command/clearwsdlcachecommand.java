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

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.tc.logging.Location;

/**
 * Title: ClearWSDLCacheCommand
 * Description: 
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class ClearWSDLCacheCommand implements Command {

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#exec(com.sap.engine.interfaces.shell.Environment, java.io.InputStream, java.io.OutputStream, java.lang.String[])
	 */
	public void exec(Environment environment, InputStream inputStream, OutputStream outputStream, String[] params) {
		try {
			if (params.length == 0) {
        WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().clearWSDLCache();
			  outputStream.write(("WSDL cache was successfully deleted!\n\n").getBytes());  //$JL-I18N$  
			} else if (params.length == 1) {
        WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().clearWSDLCache(params[0]);
			  outputStream.write(("WSDL cache for Application " + params[0] + " was successfully deleted!\n\n").getBytes()); //$JL-I18N$    
			} else {
			  outputStream.write(getHelpMessage().getBytes()); //$JL-I18N$
			}
		} catch (Exception e) {
      Location serverLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      serverLocation.catching("Unexpected exception occurred while executing clear_wsdl_cache command.", e);
      try {
        environment.getErrorStream().write(("Unexpected exception occurred while executing clear_wsdl_cache command: " + e.getMessage() + ".\nThe wsdl cache was not deleted!\n\n").getBytes()); //$JL-I18N$
      } catch (IOException exc) {
        serverLocation.catching("Exception occurred, printing error message while executing clear_wsdl_cache command.", exc);
    }
   }
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.shell.Command#getName()
	 */
	public String getName() {
		return "clear_wsdl_cache";
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
    String helpMessage = "Clears wsdl cache" + nl + nl
                         + "USAGE    : " + getName() + " [<application name>] "  + nl
                         + " clears wsdl cache for application by applicationName." + nl;

   return helpMessage;
	}

}
