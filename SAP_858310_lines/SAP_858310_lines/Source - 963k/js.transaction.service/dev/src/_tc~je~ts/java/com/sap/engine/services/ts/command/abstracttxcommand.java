package com.sap.engine.services.ts.command;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.services.ts.exceptions.TSResourceAccessor;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMJ2eeActionStatus;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public abstract class AbstractTxCommand implements Command {

	/**
	 * Method used to log an exception occurred
	 * 
	 * @param out - Stream to log exception
	 * @param info - Message to be logged
	 * @param e - Causing exception
	 */
	public void logException(PrintStream out, String info, Location location, Exception e) {
		out.println(info + " Reason:" + e.toString());
		
		if(location.beLogged(Severity.DEBUG)) {
			location.traceThrowableT(Severity.DEBUG, "ListTransactionStatistics.exec(): Error occured. Full stacktrace:", e);
		}
	}

	/**
	 * Method used to print the count of some value if it is not negative
	 * 
	 * @param out - stream to print in
	 * @param message - message corresponding to the value
	 * @param count - value to be printed in stream
	 */
	public void print(PrintStream out, String message, long count) {
		out.printf("   %s : ",message);// "   " + message + " : "
		if(count < 0) {
			out.println("Not Available");
		} else {
			out.println(count);
		}
	}
	
	/**
	 * Method used to print the count of some value if it is not negative
	 * 
	 * @param out - stream to print in
	 * @param message - message corresponding to the value
	 * @param count - value to be printed in stream
	 */
	public void print(PrintStream out, String message, String count) {
		out.printf("   %s : ",message);// "   " + message + " : "
		if(count == null || count.equals("")) {
			out.println("Not Available");
		} else {
			out.println(count);
		}
	}
	
	/**
	 * Returns the name of the group the command belongs to
	 *
	 * @return The name of the group of commands, in which this command belongs.
	 */
	public String getGroup() {
		return "transactions";
	}
	
	/**
	 * Gives the name of the supported shell providers
	 *
	 * @return   The Shell providers' names who supports this command.
	 */
	public String[] getSupportedShellProviderNames() {
		return new String[] {"InQMyShell"};
	}
	
	protected ObjectName getTransactionServiceManagementMBeanObjectName() throws IOException, MalformedObjectNameException {
		Properties sysProperties = System.getProperties();
		String sysName = sysProperties.getProperty("SAPSYSTEMNAME");
		String dbHost = sysProperties.getProperty("j2ee.dbhost");

		String clusterMBeanName = ":cimclass=SAP_ITSAMTransactionServiceManagementCluster,version=3.3,"
				+ "SAP_ITSAMJ2eeCluster.CreationClassName=SAP_ITSAMJ2eeCluster,"
				+ "SAP_ITSAMJ2eeCluster.Name=" + sysName+ ".SystemName." + dbHost
				+ ",SAP_ITSAMTransactionServiceManagementCluster.SystemName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "SAP_ITSAMTransactionServiceManagementCluster.CreationClassName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "SAP_ITSAMTransactionServiceManagementCluster.Name=SAP_ITSAMTransactionServiceManagementCluster," 
				+ "SAP_ITSAMTransactionServiceManagementCluster.SystemCreationClassName=SAP_ITSAMTransactionServiceManagementCluster,"
				+ "type=SAP_ITSAMJ2eeCluster.SAP_ITSAMTransactionServiceManagementCluster";
		return new ObjectName(clusterMBeanName);
	}
	
	
	protected void printIfCodeIsNotOK(PrintStream out,
			SAP_ITSAMJ2eeActionStatus actionStatus, Location location) {
		String errMessage = null;
		try {
			errMessage = LocalizableTextFormatter.formatString(TSResourceAccessor.getResourceAccessor(), actionStatus.getMessageId(), actionStatus.getMessageParameters());
		} catch (LocalizationException e) {
			errMessage = "Unexpected exception ocurred during localization of message with key " + actionStatus.getMessageId() + "and params ";
		    for(String param: actionStatus.getMessageParameters()){
		    	errMessage = errMessage + " " + param;
		    }
		    // no message id for this trace because the message cannot be displayed
			SimpleLogger.trace(Severity.ERROR, location, errMessage);
		}
		out.print("Status in not OK because of : ");
		out.println(errMessage);
	}
}
