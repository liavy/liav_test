package com.sap.engine.services.webservices.webservices630.server.command;

import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.registry.wsclient.WSClientRegistry;
import com.sap.engine.services.webservices.server.WSContainer;
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
public class ListWSClientsCommand implements Command {

  public void exec(Environment environment, InputStream input, OutputStream output, String[] params) {
      try {
        if (params.length == 0) {
          output.write(getWSClients(false, null).getBytes()); //$JL-I18N$
        } else if (params[0].equalsIgnoreCase("-a")) {
          output.write(getWSClients(true, params[1]).getBytes()); //$JL-I18N$
        } else {
          output.write(getHelpMessage().getBytes()); //$JL-I18N$
        }
        output.flush();
      } catch (IOException e) {
        Location serverLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
        serverLocation.catching("Unexpected exception occurred executing list_wsclients_630 command.", e);
        try {
          environment.getErrorStream().write(("Unexpected exception occurred executing list_wsclients_630 command: " + e.getMessage()).getBytes()); //$JL-I18N$
        } catch (IOException exc) {
          serverLocation.catching("IOException occurred, printing error message while executing list_wsclients_630 command.",exc);
        }
      }
    }

    public String getName() {
      return "list_wsclients_630";
    }

    public String getGroup() {
      return "webservices";
    }

    public String[] getSupportedShellProviderNames() {
      return new String[]{"InQMyShell"};
    }

    public String getHelpMessage() {
      String nl = SystemProperties.getProperty("line.separator");
      String helpMessage =   "Prints all started wsClients on the current cluster element." + nl + nl
                           + "USAGE    : " + getName() + " -[<parameter>"  + nl
                           + "Parameter: " + nl
                           + "  default             : lists all started wsclients_630" + nl
                           + "  a <application name>: lists all started wsclients_630 for the specified application" + nl;


     return helpMessage;
    }

    private String getWSClients(boolean applicationFlag, String applicationName) {
      String nl = SystemProperties.getProperty("line.separator");
      String resultString = nl;

      WSClientRegistry wsClientRegistry = WSContainer.getWsClientRegistry();
      WSClientIdentifier[] wsClientIds = new WSClientIdentifier[0];
      if (applicationFlag) {
        wsClientIds = wsClientRegistry.listWSClientsByApplicationName(applicationName);
      } else {
        wsClientIds = wsClientRegistry.listWSClients();
      }

      int wsClientIdsSize = wsClientIds.length;

      if (wsClientIdsSize == 0) {
        if (applicationFlag) {
          resultString = "No wsclients registered for application: " + applicationName + ". " + nl;
        } else {
          resultString = "No wsclients have been registered."  + nl;
        }
      }


      for (int i = 0; i < wsClientIdsSize; i++) {
        WSClientIdentifier wsClientId = wsClientIds[i];
        if (!applicationFlag) {
          resultString += "Application: " + wsClientId.getApplicationName() + nl;
        }
        resultString += "Module     : " + wsClientId.getJarName() + nl;
        resultString += "WS Client  : " + wsClientId.getServiceRefName() + nl + nl;
      }

      if (wsClientIdsSize == 0) {
        if (applicationFlag) {
          resultString = "No wsclients registered/started for application: " + applicationName + ". " + nl;
        } else {
          resultString = "No wsclients have been registered/started."  + nl;
        }
      }

      return resultString;
    }

}
