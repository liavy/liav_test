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
import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionServiceManagementClusterWrapper;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionStatisticsData;
import com.sap.tc.logging.Location;

public class ListTxStats extends AbstractTxCommand {
	
	private static final Location LOCATION = Location.getLocation(ListTxStats.class);
	
	public ListTxStats(){
	}

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
			return;
		} catch (IOException e) {
			logException(out, "The exception(IOException) occured while trying to" +
					" query mbeanServer to get mbean.", LOCATION, e);
			return;
		}
		
		if(mBeans == null) {
			logException(out, "The query for mbean doesn't work", LOCATION, null);
			return;
		}
		Iterator iterator = mBeans.iterator();
		ObjectName clusterObjectName = null;
		if (iterator.hasNext()) {
			clusterObjectName = (ObjectName) iterator.next();
		}  //getJ2eeNodeObjectName - clusterObjectName
		
		
		// classifiers
		if(params.length == 1 && (params[0].equalsIgnoreCase("-c") || params[0].equalsIgnoreCase("-classifier"))) {
			listClassifiers(out, mbeanServer, clusterObjectName);
			return;
		}
		
		// resource managers
		if(params.length == 1 && (params[0].equalsIgnoreCase("-rm"))) {
			listResourceManagers(out, mbeanServer, clusterObjectName);
			return;
		}
		
		// all statistics
		listAllStatistics(out, mbeanServer, clusterObjectName);
		return;
	}

	private void listAllStatistics(PrintStream out, MBeanServer mbeanServer, ObjectName clusterObjectName) {
		//// Query the Mbean
		Object cd = null;
		try {
			cd = mbeanServer.getAttribute(
				clusterObjectName,
				"TransactionStatistics" // property of mbean TransactionStatistics
			);
		} catch (AttributeNotFoundException e) {
			logException(out, "The exception(AttributeNotFoundException) occured while trying to" +
					" get TransactionStatistics.", LOCATION, e);
			return;
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" get TransactionStatistics.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" get TransactionStatistics.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" get TransactionStatistics.", LOCATION, e);
			return;
		}
		
		
		CompositeData compositeData = ((CompositeData) cd);
		SAP_ITSAMTransactionStatisticsData txStatistics = 
		SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMTransactionStatisticsDataForCData(compositeData);
		//--------------------------------------------------------------------------------------
		if(txStatistics == null) {
			out.println("There are no transaction statistics.");
			return;
		}
		//write info to out
		long pendingTxCount = txStatistics.getPendingTxCount();
		long notRecoveredTxCount = txStatistics.getNotRecoveredTxCount();
		
		/// total number of transactions 
		long activeTxCount = txStatistics.getActiveTxCount();
		long totalCompletedTx = txStatistics.getTotalCompletedTx();
		long totalCommittedTx = txStatistics.getTotalCommittedTx();
		long totalRollbackedTx = txStatistics.getTotalRollbackedTx();
		long txWithHeuristicOutcomesCount = txStatistics.getTxWithHeuristicOutcomesCount();
		long txPassedAbandonTimeout = txStatistics.getTxPassedAbandonTimeout();
		
		/// total number of transactions rolled back
		long txRollbackedByApplication = txStatistics.getTxRollbackedByApplication();
		long txRollbackedBecauseRMError = txStatistics.getTxRollbackedBecauseRMError();
		long timeoutedTxCount = txStatistics.getTimeoutedTxCount();		
		long averageCommitTime = txStatistics.getAverageCommitTime();
		
		long suspendedTxCount = txStatistics.getSuspendedTxCount();		
		
		SAP_ITSAMJ2eeActionStatus actionStatus = txStatistics.getActionStatus();
		
		out.println();
		// print commit ratio
		out.print("Commit ratio : ");
		if (totalCommittedTx < 0 || totalCompletedTx < 0) {
			out.println("Not Available");
		} else if (totalCompletedTx == 0 ) {
			out.println("100%");
		} else {
			out.println(Long.toString(totalCommittedTx * (long)100 / totalCompletedTx) + "%");
		}
		
		out.println();
		out.println("Total number of transactions");
		print(out, "Active",activeTxCount);
		print(out, "Pending", pendingTxCount);		
		print(out, "Completed", totalCompletedTx);
		print(out, "Committed", totalCommittedTx);
		print(out, "Rollbacked", totalRollbackedTx);
		print(out, "With heuristic outcomes", txWithHeuristicOutcomesCount);
		print(out, "Passed abandon timeout", txPassedAbandonTimeout);	
		print(out, "Not recovered", notRecoveredTxCount);
		out.println();
		
		out.println("Total number of transactions rollbacked");
		print(out, "By application", txRollbackedByApplication);
		print(out, "Because of RM error", txRollbackedBecauseRMError);
		print(out, "Because of timeout", timeoutedTxCount);
		out.printf("   %s : ", "For other reasons");		
		if (totalRollbackedTx < 0 || txRollbackedBecauseRMError < 0
				|| txRollbackedByApplication < 0 || timeoutedTxCount < 0) {
			out.println("Not Available");
		} else {
			out.println(Long.toString(totalRollbackedTx - txRollbackedBecauseRMError
					- txRollbackedByApplication - timeoutedTxCount));
		}
		out.println();
		
		print(out, "Average commit time", averageCommitTime);
		print(out, "Total number of suspended transactions", suspendedTxCount);
		out.println();
		
		String code = actionStatus.getCode();
		if(!code.equals(SAP_ITSAMJ2eeActionStatus.OK_CODE)) {
			out.println("ActionStatus is not OK!!!");
			printIfCodeIsNotOK(out, actionStatus, LOCATION);
			
//			//display code and info for it
//			out.printf("The action status is : %s\n", code);
//			out.printf("Message : %s\n", actionStatus.getMessageId());
			
//			String[] args = actionStatus.getMessageParameters();
//			if(args.length == 0) {
//				out.println("   MessageParams : Not Available");
//			} else { 
//				out.println("MessageParameters are : ");
//				for(int j = 0; j < args.length; j++) {
//					out.println(args[j]);
//				}
//			}
			if(actionStatus.getStackTrace() != null && !actionStatus.getStackTrace().equals("")) {
				out.println("Stacktrace : ");
				out.println(actionStatus.getStackTrace());
			}
		}
		return;
	}

	private void listResourceManagers(PrintStream out, MBeanServer mbeanServer, ObjectName clusterObjectName) {
		//// Query the Mbean
		Object cd = null;
		try {
			cd = mbeanServer.getAttribute(
				clusterObjectName,
				"TransactionStatisticsPerRM" // property of mbean TransactionStatisticsPerRM
			);
		} catch (AttributeNotFoundException e) {
			logException(out, "The exception(AttributeNotFoundException) occured while trying to" +
					" get TransactionStatisticsPerRM.", LOCATION, e);
			return;
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" get TransactionStatisticsPerRM.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" get TransactionStatisticsPerRM.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" get TransactionStatisticsPerRM.", LOCATION, e);
			return;
		}
		
		
		CompositeData[] compositeData = ((CompositeData[]) cd);
		SAP_ITSAMTransactionStatisticsData[] txStatistics = 
		SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMTransactionStatisticsDataArrForCData(compositeData);
		//--------------------------------------------------------------------------------------
		if(txStatistics == null || txStatistics.length == 0) {
			out.println("There are no transaction statistics per resource manager.");
			return;
		}
		for(int i = 0; i < txStatistics.length; i++) {
			out.println();
			//write some info about RM
			out.println("------------------------------------------------------------------------------------");
			out.printf("The name of RM is : %s\n", txStatistics[i].getName());
			//write info to out
			long pendingTxCount = txStatistics[i].getPendingTxCount();
			long notRecoveredTxCount = txStatistics[i].getNotRecoveredTxCount();
			
			/// total number of transactions 
			long activeTxCount = txStatistics[i].getActiveTxCount();
			long totalCompletedTx = txStatistics[i].getTotalCompletedTx();
			long totalCommittedTx = txStatistics[i].getTotalCommittedTx();
			long totalRollbackedTx = txStatistics[i].getTotalRollbackedTx();
			long txWithHeuristicOutcomesCount = txStatistics[i].getTxWithHeuristicOutcomesCount();
			long txPassedAbandonTimeout = txStatistics[i].getTxPassedAbandonTimeout();
			
			/// 
			long txRollbackedByApplication = txStatistics[i].getTxRollbackedByApplication();
			long txRollbackedBecauseRMError = txStatistics[i].getTxRollbackedBecauseRMError();
			long timeoutedTxCount = txStatistics[i].getTimeoutedTxCount();		
			long averageCommitTime = txStatistics[i].getAverageCommitTime();
			
			long suspendedTxCount = txStatistics[i].getSuspendedTxCount();	
			SAP_ITSAMJ2eeActionStatus actionStatus = txStatistics[i].getActionStatus();
			
			// print commit ratio
			out.print("Commit ratio : ");
			if (totalCommittedTx < 0 || totalCompletedTx < 0) {
				out.println("Not Available");
			} else if (totalCompletedTx == 0 ) {
				out.println("100%");
			} else {
				out.println(Long.toString(totalCommittedTx * (long)100 / totalCompletedTx) + "%");
			}
			
			out.println();
			out.println("Total number of transactions");
			print(out, "Active",activeTxCount);
			print(out, "Pending", pendingTxCount);		
			print(out, "Completed", totalCompletedTx);
			print(out, "Committed", totalCommittedTx);
			print(out, "Rollbacked", totalRollbackedTx);
			print(out, "With heuristic outcomes", txWithHeuristicOutcomesCount);
			print(out, "Passed abandon timeout", txPassedAbandonTimeout);	
			print(out, "Not recovered", notRecoveredTxCount);
			out.println();
			
			out.println("Total number of transactions rollbacked");
			print(out, "By application", txRollbackedByApplication);
			print(out, "Because of RM error", txRollbackedBecauseRMError);
			print(out, "Because of timeout", timeoutedTxCount);
			out.printf("   %s : ", "For other reasons");		
			if (totalRollbackedTx < 0 || txRollbackedBecauseRMError < 0
					|| txRollbackedByApplication < 0 || timeoutedTxCount < 0) {
				out.println("Not Available");
			} else {
				out.println(Long.toString(totalRollbackedTx - txRollbackedBecauseRMError
						- txRollbackedByApplication - timeoutedTxCount));
			}
			out.println();
			
			print(out, "Average commit time", averageCommitTime);
			print(out, "Total number of suspended transactions", suspendedTxCount);
			
			String code = actionStatus.getCode();
			if(!code.equals(SAP_ITSAMJ2eeActionStatus.OK_CODE)) {
				out.println("ActionStatus is not OK!!!");
				printIfCodeIsNotOK(out, actionStatus, LOCATION);
				
//				//display code and info for it
//				out.printf("The action status is : %s\n", code);
//				out.printf("Message : %s\n", actionStatus.getMessageId());

//				String[] args = actionStatus.getMessageParameters();
//				if(args.length == 0) {
//					out.println("   MessageParams : Not Available");
//				} else {
//					out.println("MessageParameters are : ");
//					for(int j = 0; j < args.length; j++) {
//						out.println(args[j]);
//					}
//				}
				if(actionStatus.getStackTrace() != null && !actionStatus.getStackTrace().equals("")) {
					out.println("Stacktrace : ");
					out.println(actionStatus.getStackTrace());
				}
			}
		}
		return;
	}

	private void listClassifiers(PrintStream out, MBeanServer mbeanServer, ObjectName clusterObjectName) {
		//// Query the Mbean
		Object cd = null;
		try {
			cd = mbeanServer.getAttribute(
				clusterObjectName,
				"TransactionStatisticsPerTxClassifier" // property of mbean TransactionStatisticsPerTxClassifier
			);
		} catch (AttributeNotFoundException e) {
			logException(out, "The exception(AttributeNotFoundException) occured while trying to" +
					" get TransactionStatisticsPerTxClassifier.", LOCATION, e);
			return;
		} catch (InstanceNotFoundException e) {
			logException(out, "The exception(InstanceNotFoundException) occured while trying to" +
					" get TransactionStatisticsPerTxClassifier.", LOCATION, e);
			return;
		} catch (MBeanException e) {
			logException(out, "The exception(MBeanException) occured while trying to" +
					" get TransactionStatisticsPerTxClassifier.", LOCATION, e);
			return;
		} catch (ReflectionException e) {
			logException(out, "The exception(ReflectionException) occured while trying to" +
					" get TransactionStatisticsPerTxClassifier.", LOCATION, e);
			return;
		}
		
		
		CompositeData[] compositeData = ((CompositeData[]) cd);
		SAP_ITSAMTransactionStatisticsData[] txStatistics = 
		SAP_ITSAMTransactionServiceManagementClusterWrapper.getSAP_ITSAMTransactionStatisticsDataArrForCData(compositeData);
		//--------------------------------------------------------------------------------------
		if(txStatistics == null || txStatistics.length == 0) {
			out.println("There are no transaction statistics per classifier.");
			return;
		}
		for(int i = 0; i < txStatistics.length; i++) {
			out.println();
			//write some info about Classifier
			out.println("------------------------------------------------------------------------------------");
			out.printf("The Classifier is : %s\n", txStatistics[i].getName());
			//write info to out
			long pendingTxCount = txStatistics[i].getPendingTxCount();
			long notRecoveredTxCount = txStatistics[i].getNotRecoveredTxCount();
			
			/// total number of transactions 
			long activeTxCount = txStatistics[i].getActiveTxCount();
			long totalCompletedTx = txStatistics[i].getTotalCompletedTx();
			long totalCommittedTx = txStatistics[i].getTotalCommittedTx();
			long totalRollbackedTx = txStatistics[i].getTotalRollbackedTx();
			long txWithHeuristicOutcomesCount = txStatistics[i].getTxWithHeuristicOutcomesCount();
			long txPassedAbandonTimeout = txStatistics[i].getTxPassedAbandonTimeout();
			
			/// 
			long txRollbackedByApplication = txStatistics[i].getTxRollbackedByApplication();
			long txRollbackedBecauseRMError = txStatistics[i].getTxRollbackedBecauseRMError();
			long timeoutedTxCount = txStatistics[i].getTimeoutedTxCount();		
			long averageCommitTime = txStatistics[i].getAverageCommitTime();
			
			long suspendedTxCount = txStatistics[i].getSuspendedTxCount();	
			SAP_ITSAMJ2eeActionStatus actionStatus = txStatistics[i].getActionStatus();
			
			// print commit ratio
			out.print("Commit ratio : ");
			if (totalCommittedTx < 0 || totalCompletedTx < 0) {
				out.println("Not Available");
			} else if (totalCompletedTx == 0 ) {
				out.println("100%");
			} else {
				out.println(Long.toString(totalCommittedTx * (long)100 / totalCompletedTx) + "%");
			}
			
			out.println();
			out.println("Total number of transactions");
			print(out, "Active",activeTxCount);
			print(out, "Pending", pendingTxCount);		
			print(out, "Completed", totalCompletedTx);
			print(out, "Committed", totalCommittedTx);
			print(out, "Rollbacked", totalRollbackedTx);
			print(out, "With heuristic outcomes", txWithHeuristicOutcomesCount);
			print(out, "Passed abandon timeout", txPassedAbandonTimeout);	
			print(out, "Not recovered", notRecoveredTxCount);
			out.println();
			
			out.println("Total number of transactions rollbacked");
			print(out, "By application", txRollbackedByApplication);
			print(out, "Because of RM error", txRollbackedBecauseRMError);
			print(out, "Because of timeout", timeoutedTxCount);
			out.printf("   %s : ", "For other reasons");		
			if (totalRollbackedTx < 0 || txRollbackedBecauseRMError < 0
					|| txRollbackedByApplication < 0 || timeoutedTxCount < 0) {
				out.println("Not Available");
			} else {
				out.println(Long.toString(totalRollbackedTx - txRollbackedBecauseRMError
						- txRollbackedByApplication - timeoutedTxCount));
			}
			out.println();
			
			print(out, "Average commit time", averageCommitTime);
			print(out, "Total number of suspended transactions", suspendedTxCount);
			
			String code = actionStatus.getCode();
			if(!code.equals(SAP_ITSAMJ2eeActionStatus.OK_CODE)) {
				out.println("ActionStatus is not OK!!!");
				printIfCodeIsNotOK(out, actionStatus, LOCATION);
				
//				//display code and info for it
//				out.printf("The action status is : %s\n", code);
//				out.printf("Message : %s\n", actionStatus.getMessageId());
				
//				String[] args = actionStatus.getMessageParameters();
//				if(args.length == 0) {
//					out.println("   MessageParams : Not Available");
//				} else {
//					out.println("MessageParameters are : ");
//					for(int j = 0; j < args.length; j++) {
//						out.println(args[j]);
//					}
//				}
				if(actionStatus.getStackTrace() != null && !actionStatus.getStackTrace().equals("")) {
					out.println("Stacktrace : ");	
					out.println(actionStatus.getStackTrace());
				}
			}
		}
		
		return;
	}

	public String getHelpMessage() {
		return "\n" +
		"Lists all transaction statistics.\n\n" +
		"Usage: listTxStats [<options>]\n" +
		"  Parameters:\n" +
		"   [-rm]                 - lists transaction statistics per resource manager.\n" +
		"   [-c] or [-classifier] - lists transaction statistics per classifier.\n";
	}

	public String getName() {
		return "listTxStats";
	}

}
