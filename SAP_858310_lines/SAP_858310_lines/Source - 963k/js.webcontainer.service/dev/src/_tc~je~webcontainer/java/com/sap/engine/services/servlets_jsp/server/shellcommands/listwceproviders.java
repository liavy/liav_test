package com.sap.engine.services.servlets_jsp.server.shellcommands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerExtensionWrapper;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class ListWCEProviders implements Command {

	/**
	 * @param env
	 * @param is
	 * @param os
	 * @param params
	 */
	public void exec(Environment env, InputStream is, OutputStream os, String[] params) {
		PrintWriter pw = new PrintWriter(os, true);
		int count = params.length;

		try {
			if ((count > 0 && (params[0].toUpperCase().equals("-H") || params[0].equals("-?") || params[0].toUpperCase().equals("-HELP")))) {
				pw.println();
				pw.println(getHelpMessage());
				return;
			} else if (count > 0 && params[0].toUpperCase().equals("-A")) {
				pw.println();
				pw.println("All registered WCE providers:");
				pw.println("-----------------------------"); 

				WebContainerProvider webContainerProvider = (WebContainerProvider) ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider();
				Hashtable<String, WebContainerExtensionWrapper> wceTable = webContainerProvider.getWebContainerExtensionWrappers();
				for (String name : wceTable.keySet()) {
					pw.println(name);
				}
				pw.println();
				return;
			} else if (count > 1 && params[0].toUpperCase().equals("-P")) {
				if (params[1] != null) {
					WebContainerProvider webContainerProvider = (WebContainerProvider) ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider();
					Hashtable<String, WebContainerExtensionWrapper> wceTable = webContainerProvider.getWebContainerExtensionWrappers();
					WebContainerExtensionWrapper wrapper = wceTable.get(params[1]);
					if (wrapper != null) {
						pw.println();
						pw.println("WCE provider's name: " + params[1]);
						pw.println("IWebContainerExtension impl: " + wrapper.getWebContainerExtension().toString());
						pw.println("IWebContainerExtensionContext impl: " + wrapper.getWebContainerExtensionContext().toString());
						pw.println("Descriptor(s) name(s): " + wrapper.getDescriptorNames().toString());
						pw.println();
						return;
					} else {
						pw.println();
						pw.println("There is no such WCE provider currently registered to Web Container.");
						pw.println();
						return;
					}
				} else {
					throw new Exception("WCE provider's name is not specified.");
				}
			} else {
				pw.println();
				pw.println(getHelpMessage());
				return;
			}
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			pw.println();
			pw.print("ERROR: ");
			e.printStackTrace(pw);
			pw.println();
			pw.println(getHelpMessage());
			return;
		}
	}// end of exec(Environment env, InputStream is, OutputStream os, String[] params)

	/**
	 * Returns the command's group name.
	 * 
	 * @return the group's name for this command.
	 */
	public String getGroup() {
		return "servlet_jsp";
	}// end of getGroup() 

	/**
	 * Returns the command's help message.
	 * 
	 * @return a help message for this command.
	 */
	public String getHelpMessage() {
		return "Prints a list of all Web Container Extension (WCE) providers " + Constants.lineSeparator 
			+ "which are currently registered to Web Container." + Constants.lineSeparator
			+ Constants.lineSeparator 
			+ "Usage: " + getName() + " <options>" + Constants.lineSeparator 
			+ Constants.lineSeparator 
			+ "Parameters:" + Constants.lineSeparator
			+ "  -a                       - Lists the names of all Web Container Extension" + Constants.lineSeparator 
			+ "                             (WCE) providers which are currently registered to" + Constants.lineSeparator 
			+ "                             Web Container." + Constants.lineSeparator 
			+ "  -p <WCE provider's name> - Detailed information about Web Container Extension" + Constants.lineSeparator 
			+ "                             (WCE) provider with the given name." + Constants.lineSeparator;
	}// end of getHelpMessage()

	/**
	 * Returns the name of the command.
	 * 
	 * @return the name of the command.
	 */
	public String getName() {
		return "LIST_WCE_PROVIDERS";
	}// end of getName()

	/**
	 * Returns the name of the supported shell providers.
	 * 
	 * @return the Shell providers' names who supports this command.
	 */
	public String[] getSupportedShellProviderNames() {
		return new String[] { "InQMyShell" };
	}// end of getSupportedShellProviderNames()

}// end of class
