package com.sap.engine.services.ts.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMJ2eeActionStatus;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMPendingTransactionData;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionServiceManagementClusterWrapper;
import com.sap.tc.logging.Location;

public class ListPendingTransactions extends AbstractTxCommand {

	private static final Location LOCATION = Location.getLocation(ListPendingTransactions.class);
	
	/**
	 * Constructor
	 * 
	 */
	public ListPendingTransactions() {
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
		
		/// Query the Cluster Mbean
		Set mBeans = null;
		try {
			mBeans = mbeanServer.queryNames(
					getTransactionServiceManagementMBeanObjectName(), // mbean name
					null
					);
		} catch (MalformedObjectNameException e) {
			logException(out, "The exception(MalformedObjectNameException) occured while trying to" +
					" query mbeanServer to get mbean.", LOCATION, e);
			return;
		} catch (NullPointerException e) {
			logException(out, "The exception(NullPointerException) occured while trying to" +
					" query mbeanServer to get mbean.", LOCATION, e);
		} catch (IOException e) {
			logException(out, "The exception(IOException) occured while trying to" +
					" query mbeanServer to get mbean.", LOCATION, e);
			return;
		}
		
		
		Iterator iterator = mBeans.iterator();
		ObjectName clusterObjectName = null;
		if (iterator.hasNext()) {
			clusterObjectName = (ObjectName) iterator.next();
		}  //getJ2eeNodeObjectName - clusterObjectName
		
		
		//// Query the Mbean
		Object getPendingTxCD = null;
		try {
			getPendingTxCD = mbeanServer.getAttribute(
				clusterObjectName,
				"PendingTransactions" // property of mbean TransactionStatisticsPerTxClassifier
			);
		} catch (AttributeNotFoundException e) {
			logException(out, "The exception(AttributeNotFoundException) occured while trying to" +
					" get PendingTransactions.", LOCATION, e);
			return;
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" get PendingTransactions.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" get PendingTransactions.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" get PendingTransactions.", LOCATION, e);
			return;
		}
		
		CompositeData[] compositeData = ((CompositeData[]) getPendingTxCD);
		SAP_ITSAMPendingTransactionData[] pendingTransactions = 
		SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMPendingTransactionDataArrForCData(compositeData);
		//--------------------------------------------------------------------------------------
		if(pendingTransactions == null || pendingTransactions.length == 0 || pendingTransactions.length == 1) {
			out.println(" There are no pending transactions.");
			return;
		}
		for(int i = 0; i < pendingTransactions.length - 1; i++) {
			out.println();
			//write info to out
			long txSeqNumber = pendingTransactions[i].getTransactionSeqNumber();
			String classifier = pendingTransactions[i].getTransactionClassifier();
			String status = pendingTransactions[i].getStatus();
			String briefDescriptionId = pendingTransactions[i].getBriefDescriptionId();
			String[] briefDescriptionParams = pendingTransactions[i].getBriefDescriptionParams();
			SAP_ITSAMJ2eeActionStatus actionStatus = pendingTransactions[i].getActionStatus();
			
			print(out, "Transaction sequence number", txSeqNumber);
			print(out, "Classifier", classifier);
			print(out, "Status", status);
			
//			print(out, "BriefDescription", briefDescriptionId);
//			if(briefDescriptionParams != null) {
//				for (int j = 0; j < briefDescriptionParams.length; j++) {
//					print(out,"Param[" + j + "]", briefDescriptionParams[j]);
//				}
//			} else {
//				out.println("   BriefDescriptionParams : Not Available");
//			}

			String code = actionStatus.getCode();
			if(!code.equals(SAP_ITSAMJ2eeActionStatus.OK_CODE)) {
				out.println("ActionStatus is not OK!!!");
				printIfCodeIsNotOK(out, actionStatus, LOCATION);
				
//				//display code and info for it
//				print(out, "Action status", code);
//				print(out, "Message ID : ", actionStatus.getMessageId());
//				String[] args = actionStatus.getMessageParameters();
//				print(out, "MessageParameters are", "");
//				for(int j = 0; j < args.length; j++) {
//					print(out, "Param " + j, args[j]);
//				}
				if(actionStatus.getStackTrace() != null && !actionStatus.getStackTrace().equals("")) {
					out.println("Stacktrace : ");
					out.println(actionStatus.getStackTrace());
				}
			} else {
				out.println("   " + code);
			}
		}
		out.println();
		
		return;
	}

	public String getHelpMessage() {
		return "\n" +
		"Lists all pending transaction statistics.\n\n" +
		"Usage: listPendingTx\n";
	}

	/**
	 * Gets the name of the command
	 * 
	 * @return The name of the command
	 */
	public String getName() {
		return "listPendingTx";
	}

}
