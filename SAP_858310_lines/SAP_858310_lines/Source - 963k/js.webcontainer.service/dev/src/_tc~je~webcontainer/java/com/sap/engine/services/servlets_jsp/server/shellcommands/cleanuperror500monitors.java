package com.sap.engine.services.servlets_jsp.server.shellcommands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

public class CleanupError500Monitors implements Command {
	private static Location currentLocation = Location.getLocation(ListSessions.class);
		
	  /**
	   * The implemetation of the corresponding method in the Command interface.
	   *
	   * @param   env  Environment object
	   * @param   is  InputStream object
	   * @param   os  OutputStream object
	   * @param   params  an array of String objects which are the input parameters of the
	   *                  corresponding command
	   */
	public void exec(Environment env, InputStream is, OutputStream os,
			String[] params) {
		  PrintWriter pw = new PrintWriter(os, true);
		try{
			  ServiceContext.getServiceContext().getWebMonitoring().dumpAndClearISE500Monitors();
			  pw.println("Error 500 web monitors are successfully cleared.");
		   } catch (OutOfMemoryError e) {
		        throw e;
		   } catch (ThreadDeath e) {
		        throw e;
		   } catch (Throwable e) {
		        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000129", 
		          "Cannot execute the http telnet command [{0}].", new Object[]{getName()}, e, null, null);
		        pw.println("ERROR: " + e.getMessage());
		        e.printStackTrace(pw);
		        pw.println("--------------------------------------");
		        pw.println(getHelpMessage());
		        return;
		  }
		 
	}

	  /**
	   * Gets the command's group
	   *
	   * @return Group name of the command
	   */

	public String getGroup() {
		return "servlet_jsp";
	}
	
	  /**
	   * Gets the command's help message
	   *
	   */
	public String getHelpMessage() {
		return  "Performs cleanup of all the information stored in the two web monitors: Error500Count and Error500CategorizationEntries. " + Constants.lineSeparator +
				"In order to trace the contents of the monitors just before they are emptied," + Constants.lineSeparator +
				"first enable \"com.sap.engine.services.servlets_jsp.Service\"  trace location in DEBUG severity. " ;
	}

	/**
	   * Gets the command's name
	   *
	   * @return Name of the command
	*/
	public String getName() {
		return "CLEAR_ERROR500_MONITORS";
	}

	public String[] getSupportedShellProviderNames() {
		return new String[] {"InQMyShell"};
	}

}
