package com.sap.engine.services.ts.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.interfaces.shell.Environment;
import com.sap.tc.logging.Location;

public class ClearTransactionStatistics extends AbstractTxCommand {

	private static final Location LOCATION = Location.getLocation(ClearTransactionStatistics.class);
			
	/**
	 * Constructor
	 * 
	 */
	public ClearTransactionStatistics() {
	}

	/**
   	* A method that executes the command .
   	*
   	* @param   env  An implementation of Environment.
   	* @param   is  The InputStream , used by the command.
   	* @param   os  The OutputStream , used by the command.
   	* @param   params  Parameters of the command.
   	*/
	public void exec(Environment env, InputStream is, OutputStream os, String[] params) {
		PrintStream out = new PrintStream(os);
		
		if(params.length == 1 && (params[0].equalsIgnoreCase("-help") ||
				params[0].equalsIgnoreCase("-h") || params[0].equalsIgnoreCase("-?"))) {
			out.println(getHelpMessage());
			return;
		}
		
		MBeanServer mbeanServer = null;
		try {
			mbeanServer = (MBeanServer)new InitialContext().lookup("jmx");
		} catch (NamingException e) {
			logException(out, "The exception(NamingException) occured while trying to" +
					" get mbeanServer.", LOCATION, e);
			return;
		}
		
		try {
			mbeanServer.invoke(getTransactionServiceManagementMBeanObjectName(), "clearTransactionsStatistics",
					new Object[] {}, new String[] {});
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" invoke clearStatistics on mbean.", LOCATION, e);
			return;
		} catch (MalformedObjectNameException e) {
			logException(out, "The exception(MalformedObjectNameException) occured while trying to" +
					" invoke clearStatistics on mbean.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" invoke clearStatistics on mbean.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" invoke clearStatistics on mbean.", LOCATION, e);
			return;
		} catch (IOException e) {
			logException(out, "The exception(IOException) occured while trying to" +
					" invoke clearStatistics on mbean.", LOCATION, e);
			return;
		}
		
		out.println("Successfully executed");
		return;
	}

	public String getHelpMessage() {
		return "\n" +
		"Clears all transaction statistics.\n\n" +
		"Usage: clearTxStats\n";
	}

	/**
	 * Gets the name of the command
	 * 
	 * @return The name of the command
	 */
	public String getName() {
		return "clearTxStats";
	}

}
