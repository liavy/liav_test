package com.sap.engine.services.ts.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
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
import com.sap.engine.services.ts.mbeans.SAP_ITSAMExtendedPendingTransactionData;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMJ2eeActionStatus;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMPendingTransactionData;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionServiceManagementClusterWrapper;
import com.sap.tc.logging.Location;

public class ForgetTransaction extends AbstractTxCommand {

	private static final Location LOCATION = Location.getLocation(ForgetTransaction.class);
			
	/**
	 * Constructor
	 * 
	 */
	public ForgetTransaction() {
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
		Scanner sc = new Scanner(is); //BufferedReader f = new BufferedReader( new InputStreamReader( System.in ) );

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
			return;
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
		
		Set<SAP_ITSAMExtendedPendingTransactionData> set = new HashSet<SAP_ITSAMExtendedPendingTransactionData>();
		
		CompositeData[] compositeData = ((CompositeData[]) getPendingTxCD);
		SAP_ITSAMPendingTransactionData[] pendingTransactions = 
		SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMPendingTransactionDataArrForCData(compositeData);
		//--------------------------------------------------------------------------------------
		if(pendingTransactions == null || pendingTransactions.length == 0) {
			out.println(" There are no pending transactions.");
			return;
		}
		for(int i = 0; i < pendingTransactions.length; i++) {
			int nodeId = pendingTransactions[i].getNodeId();
			String systemId = pendingTransactions[i].getSystemId();
			long tmStartupTime = pendingTransactions[i].getTmStartupTime();
			long txSeqNum = pendingTransactions[i].getTransactionSeqNumber();
			
			CompositeData pendInfo = null;
			try {
				pendInfo = (CompositeData) mbeanServer.invoke(
						getTransactionServiceManagementMBeanObjectName(),
						"retrievePendingTransactionInfo", 
						new Object[] { systemId, nodeId, tmStartupTime, txSeqNum },
						new String[] { String.class.getName(), int.class.getName(),
										long.class.getName(), long.class.getName() }
						);
			} catch (InstanceNotFoundException e) {
				logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			} catch (MalformedObjectNameException e) {
				logException(out, "The exception(MalformedObjectNameException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			} catch (NumberFormatException e) {
				logException(out, "The exception(NumberFormatException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			} catch (MBeanException e) {
				logException(out, "The exception(MBeanException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			} catch (ReflectionException e) {
				logException(out, "The exception(ReflectionException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			} catch (IOException e) {
				logException(out, "The exception(IOException) occured while trying to" +
						" invoke forget on mbean.", LOCATION, e);
				return;
			}
			
			SAP_ITSAMExtendedPendingTransactionData pendindTransactionData = 
				SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMExtendedPendingTransactionDataForCData(pendInfo);
			
			if(pendindTransactionData.getForgetSupported()) {
				set.add(pendindTransactionData);
			}
		}

		if(set.size() == 0) {
			out.println(" There are no pending transactions for forget.");
			return;
		}
		int num = 1;
		for (SAP_ITSAMExtendedPendingTransactionData data : set) {
			out.println("------------------------------------------------------------------------------------------");
			
			long txSeqNumber = data.getTransactionSeqNumber();
			String classifier = data.getTransactionClassifier();
			String status = data.getStatus();
			String briefDescriptionId = data.getBriefDescriptionId();
			String[] briefDescriptionParams = data.getBriefDescriptionParams();
			SAP_ITSAMJ2eeActionStatus actionStatus = data.getActionStatus();
			
			out.println("Number : " + (num++));
			print(out, "Transaction sequence number", txSeqNumber);
			print(out, "Classifier", classifier);
			print(out, "Status", status);
//			print(out, "BriefDescription", briefDescriptionId);
//			if(briefDescriptionParams != null && briefDescriptionParams.length != 0) {
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
//				print(out, "Message : ", actionStatus.getMessageId());
				
//				String[] args = actionStatus.getMessageParameters();
//				print(out, "MessageParameters are", "");
//				for(int j = 0; j < args.length; j++) {
//					print(out, "Param " + j, args[j]);
//				}
				String stackTrace = actionStatus.getStackTrace();
				if(stackTrace != null && !stackTrace.equals("")) {
					out.println("Stacktrace : ");
					out.println(stackTrace);
				}
			} else {
				out.println("   " + code);
			}
			out.println();
		}

		int opt = sc.nextInt();
		
		Iterator<SAP_ITSAMExtendedPendingTransactionData> it = set.iterator();
		for(int i = 1; i < opt; i++) { // skipping first elements to get chosen element
			it.next();
		}
			
		SAP_ITSAMExtendedPendingTransactionData chosenForForget = it.next();
		
		CompositeData cdata = null;
		try {
			cdata = (CompositeData) mbeanServer.invoke(
					getTransactionServiceManagementMBeanObjectName(),
					"forgetTransaction", new Object[] { chosenForForget.getSystemId(),
							chosenForForget.getNodeId(), chosenForForget.getTmStartupTime(),
							chosenForForget.getTransactionSeqNumber() },
					new String[] { String.class.getName(), int.class.getName(),
							long.class.getName(), long.class.getName() });
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		} catch (MalformedObjectNameException e) {
			logException(out, "The exception(MalformedObjectNameException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		} catch (NumberFormatException e) {
			logException(out, "The exception(NumberFormatException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		} catch (IOException e) {
			logException(out, "The exception(IOException) occured while trying to" +
					" invoke forget on mbean.", LOCATION, e);
			return;
		}
		
		SAP_ITSAMJ2eeActionStatus stat = 
			SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMJ2eeActionStatusForCData(cdata);
		
		String code = stat.getCode();
		if(!code.equals(SAP_ITSAMJ2eeActionStatus.OK_CODE)) {
			out.println("ActionStatus is not OK!!!");
			printIfCodeIsNotOK(out, stat, LOCATION);
			
//			out.println("   " + code);
//			print(out, "Message", stat.getMessageId());
			
//			String[] par = stat.getMessageParameters();
//			if(par.length == 0) {
//				out.println("   MessageParams : Not Available");
//				return;
//			}
//			for (int i = 0; i < par.length; i++) {
//				print(out, "Param " + i, par[i]);
//			}
			
			if(stat.getStackTrace() != null && !stat.equals("")) {
				out.println("Stacktrace : ");
				out.println(stat.getStackTrace());
			}
		} else {
			out.println("   " + code);
			out.println("   Forgotten");
		}
		return;
	}

	public String getHelpMessage() {
		return "\n" +
		"Lists all pending transactions which can be forgotten\n" +
		"and gives the option to choose which one to be forgotten.\n\n" +
		"Usage: forgetTx  ... and then the number of chosen transaction to forget.\n";
	}

	/**
	 * Gets the name of the command
	 * 
	 * @return The name of the command
	 */
	public String getName() {
		return "forgetTx";
	}
	
}
