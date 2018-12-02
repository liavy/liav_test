package com.sap.engine.services.webservices.webservices630.server.command;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.runtime.registry.WebServiceRegistry;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.boot.SystemProperties;
import com.sap.tc.logging.Location;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ListWebServicesCommand implements Command {

  public void exec(Environment environment, InputStream input, OutputStream output, String[] params) {
    try {
      if (params.length == 0) {
        output.write(getWebServices(false, null).getBytes()); //$JL-I18N$
      } else if (params[0].equalsIgnoreCase("-a")) {
        output.write(getWebServices(true, params[1]).getBytes()); //$JL-I18N$
      } else if (params[0].equals("-ws") && params.length == 4 ) {
        output.write(getWSInfo(new WSIdentifier(params[1], params[2], params[3]), environment).getBytes()); //$JL-I18N$
      } else {
        output.write(getHelpMessage().getBytes()); //$JL-I18N$
      }
      output.flush();
    } catch (IOException e) {
      Location serverLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      serverLocation.catching("Unexpected exception occurred list_ws command.", e);
      try {
        environment.getErrorStream().write(("Unexpected exception occurred list_ws command: " + e.getMessage()).getBytes()); //$JL-I18N$
      } catch (IOException exc) {
        serverLocation.catching("IOException occurred, printing error message while executing list_ws command.", exc);
      }
    }
  }

  public String getName() {
    return "list_ws";
  }

  public String getGroup() {
    return "webservices";
  }

  public String[] getSupportedShellProviderNames() {
    return new String[]{"InQMyShell"};
  }

  public String getHelpMessage() {
    String nl = SystemProperties.getProperty("line.separator");
    String helpMessage =   "Prints all started webservices on the current cluster element." + nl + nl
                         + "USAGE    : " + getName() + " -[<parameter>] "  + nl
                         + "Parameter: " + nl
                         + "  default             : lists all started webservices" + nl
                         + "  a <application name>: lists all started webservices for the specified application" + nl
                         + "  ws <application name> <module name> <web service name>: lists additional information for the specified web service " + nl
                                                                                    + "- all ws congigurations and their transport addresses." + nl;

   return helpMessage;
  }

  private String getWebServices(boolean applicationFlag, String applicationName) {
    String nl = SystemProperties.getProperty("line.separator");
    String resultString = nl;

    WebServiceRegistry wsRegistry = WSContainer.getWSRegistry();
    WSIdentifier[] wsIds = new WSIdentifier[0];
    if (applicationFlag) {
      wsIds = wsRegistry.listWebServicesByApplicationName(applicationName);
    } else {
      wsIds = wsRegistry.listWebServices();
    }

    int wsIdsSize = wsIds.length;

    if (wsIdsSize == 0) {
      if (applicationFlag) {
        resultString = "No webservices registered/started for application: " + applicationName + ". " + nl;
      } else {
        resultString = "No webservices have been registered/started."  + nl;
      }
    }

    for (int i = 0; i < wsIdsSize; i++) {
      WSIdentifier wsId = wsIds[i];
      if (!applicationFlag) {
        resultString += "Application: " + wsId.getApplicationName() + nl;
      }
      resultString += "Module     : " + wsId.getJarName() + nl;
      resultString += "Web Service: " + wsId.getServiceName() + nl + nl;
    }


    return resultString;
  }

  private String getWSInfo(WSIdentifier wsIdentifier, Environment environment) {
    String nl = SystemProperties.getProperty("line.separator");
    String resultString = nl;

    WebServiceRegistry  webServiceRegistry = WSContainer.getWSRegistry();
    if (!webServiceRegistry.contains(wsIdentifier)) {
      resultString += "The specified web service is not registered/started: " + nl + wsIdentifier + nl;
      return resultString;
    }

    try {
      WSRuntimeDefinition wsRuntimeDefinition = webServiceRegistry.getWebService(wsIdentifier);
      String[] seiInfoes = getSEIInfoes(wsRuntimeDefinition.getServiceEndpointDefinitions());
      resultString += getConcatStrings(seiInfoes);
    } catch (RegistryException e) {
      Location serverLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      serverLocation.catching("Unexpected exception occurred, executing list_ws command.", e);
      try {
        environment.getErrorStream().write(("Unexpected exception occurred, executing list_ws command: " + e.getMessage()).getBytes()); //$JL-I18N$
      } catch (IOException exc) {
        serverLocation.catching("IOException occurred, printing error message while executing list_ws command.", exc);
      }
    }

    return resultString;
  }


  private String[] getSEIInfoes(ServiceEndpointDefinition[] endpointDefinitions) {
    if (endpointDefinitions == null) {
      return new String[0];
    }
    int endpointSize = endpointDefinitions.length;
    String[] seiInfoes = new String[endpointSize];
    for (int i = 0; i < endpointSize; i++) {
      seiInfoes[i] = getSEIInfo(endpointDefinitions[i]);
    }

    return seiInfoes;
  }

  private String getSEIInfo(ServiceEndpointDefinition endpointDefinition) {
    String nl = SystemProperties.getProperty("line.separator");
    String resultString = "";

    resultString += "WS Configuration : " + endpointDefinition.getConfigurationName() + nl;
    resultString += "Transport Address: " + endpointDefinition.getServiceEndpointId() + nl;

    return resultString;
  }

  private String getConcatStrings(String[] strs) {
    String nl = SystemProperties.getProperty("line.separator");

    String resultString = "";
    int strSize = strs.length;
    for (int i = 0; i < strSize; i++) {
      resultString += strs[i] + nl;
    }
    return resultString;
  }

}
