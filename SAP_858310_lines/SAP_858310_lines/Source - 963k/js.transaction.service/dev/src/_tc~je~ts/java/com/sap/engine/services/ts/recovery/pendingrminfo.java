package com.sap.engine.services.ts.recovery;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.sap.engine.services.ts.Util;
import com.sap.engine.services.ts.exceptions.TSResourceAccessor;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMRMData;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class PendingRMInfo {

	public static final String ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT = "ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT";
	public static final String ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM = "ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM";
	public static final String ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM = "ROLLBACK_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM";
	public static final String ROLLBACK_FINISHED_WITH_HEURISTIC_ERROR_OMGRM = "ROLLBACK_FINISHED_WITH_HEURISTIC_ERROR_OMGRM";
	public static final String ROLLBACK_FINISHED_WITH_XA_RBBASE_RBEND = "ROLLBACK_FINISHED_WITH_XA_RBBASE_RBEND";
	public static final String ROLLBACK_FINISHED_WITH_XA_HEURRB = "ROLLBACK_FINISHED_WITH_XA_HEURRB";
	public static final String ROLLBACK_FINISHED_WITH_XA_HEURCOM = "ROLLBACK_FINISHED_WITH_XA_HEURCOM";
	public static final String ROLLBACK_FINISHED_WITH_XA_HEURMIX = "ROLLBACK_FINISHED_WITH_XA_HEURMIX";
	public static final String ROLLBACK_FINISHED_WITH_XA_HEURHAZ = "ROLLBACK_FINISHED_WITH_XA_HEURHAZ";
	public static final String ROLLBACK_FINISHED_WITH_XAER_RMFAIL = "ROLLBACK_FINISHED_WITH_XAER_RMFAIL";
	public static final String ROLLBACK_FINISHED_WITH_XAER_RMERR = "ROLLBACK_FINISHED_WITH_XAER_RMERR";
	public static final String ROLLBACK_FINISHED_WITH_XA_OTHER = "ROLLBACK_FINISHED_WITH_XA_OTHER";
	
	public static final String COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT = "COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_LT";
	public static final String COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_1PC = "COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURCOM_1PC = "COMMIT_FINISHED_WITH_XA_HEURCOM_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURHAZ_1PC = "COMMIT_FINISHED_WITH_XA_HEURHAZ_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURMIX_1PC = "COMMIT_FINISHED_WITH_XA_HEURMIX_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURRB_1PC = "COMMIT_FINISHED_WITH_XA_HEURRB_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_RBBASE_1PC = "COMMIT_FINISHED_WITH_XA_RBBASE_1PC";
	public static final String COMMIT_FINISHED_WITH_XAER_RMFAIL_1PC = "COMMIT_FINISHED_WITH_XAER_RMFAIL_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_UNKNOWN_1PC = "COMMIT_FINISHED_WITH_XA_UNKNOWN_1PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURMIX_2PC = "COMMIT_FINISHED_WITH_XA_HEURMIX_2PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURHAZ_2PC = "COMMIT_FINISHED_WITH_XA_HEURHAZ_2PC";
	public static final String COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_2PC = "COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_FROM_XARM_2PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURCOM_2PC = "COMMIT_FINISHED_WITH_XA_HEURCOM_2PC";
	public static final String COMMIT_FINISHED_WITH_XA_HEURRB_2PC = "COMMIT_FINISHED_WITH_XA_HEURRB_2PC";
	public static final String COMMIT_FINISHED_WITH_XA_RBBASE_2PC = "COMMIT_FINISHED_WITH_XA_RBBASE_2PC";
	public static final String COMMIT_FINISHED_WITH_XA_RETRY_XAER_RMFAIL_2PC = "COMMIT_FINISHED_WITH_XA_RETRY_XAER_RMFAIL_2PC";
	public static final String COMMIT_FINISHED_WITH_HeuristicHazard_ERROR_OMGRM = "COMMIT_FINISHED_WITH_HeuristicHazard_ERROR_OMGRM";
	public static final String COMMIT_FINISHED_WITH_HeuristicRollback_ERROR_OMGRM = "COMMIT_FINISHED_WITH_HeuristicRollback_ERROR_OMGRM";
	public static final String COMMIT_FINISHED_WITH_NotPrepared_ERROR_OMGRM = "COMMIT_FINISHED_WITH_NotPrepared_ERROR_OMGRM";
	public static final String COMMIT_FINISHED_WITH_HeuristicMixed_ERROR_OMGRM = "COMMIT_FINISHED_WITH_HeuristicMixed_ERROR_OMGRM";
	public static final String COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM = "COMMIT_FINISHED_WITH_UNEXPECTED_ERROR_OMGRM";
	
	private static final Location LOCATION = Location.getLocation(PendingRMInfo.class);	
	private Xid xid;
	private int rmID;
	private String rmName = null;	
	private String rmStatusID = null;
	private String[] rmStatusParams = null;
	private String[] txBriefParams = null;
	private String[] txDetailedParams = null;
	private boolean operationForRetry = false;
	private boolean forgetSupported = false;	
	private boolean isErrorOrWarning = false;
	private String messageForTrace = null;
	


	public PendingRMInfo(boolean isError,Xid xid, int rmID, String rmName, 
			String rmStatusID,String transactionIDForLogs, 
			Exception problemCause,	boolean operationForRetry, boolean forgetSupported){
		this.xid = xid;
		this.rmID = rmID;
		this.rmName = rmName;		
		this.rmStatusID = rmStatusID;
		this.operationForRetry = operationForRetry;
		this.forgetSupported = forgetSupported;
		this.rmStatusParams = new String[2];
		this.isErrorOrWarning = isError;
		rmStatusParams[0] = rmName;
		if(problemCause != null){
			rmStatusParams[1] = problemCause.toString();
		}
		txBriefParams = new String[3];
		txBriefParams[0] = rmName;
		txBriefParams[1] = transactionIDForLogs;
		if(problemCause != null){
			txBriefParams[2] = problemCause.toString();
		}
		txDetailedParams = new String[3];
		txDetailedParams[0] = rmName;
		txDetailedParams[1] = transactionIDForLogs;
		if(problemCause != null){
			txDetailedParams[2] = Util.getStackTrace(problemCause);
		}
		
		if(problemCause == null){
			if(isError){
				SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000079", "{0}",new Object[]{getMessageForTrace()});
			} else {
				SimpleLogger.trace(Severity.WARNING, LOCATION, getMessageForTrace());
			}			
		} else {
			if(isError){
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,problemCause, "ASJ.trans.000081","{0}", new Object[]{getMessageForTrace()});
			} else {
				SimpleLogger.traceThrowable(Severity.WARNING, LOCATION,problemCause, "ASJ.trans.000082","{0}", new Object[]{getMessageForTrace()});
			}
		}
	}
	
	public Xid getXid(){
		return this.xid;
	}
	
	public int getRmID(){
		return this.rmID;
	}

	public XAResource recreateXAResource(){
		return null;
	}
	
	public SAP_ITSAMRMData getITSAMRMData(){
		return new SAP_ITSAMRMData(rmID, rmName, rmStatusID, rmStatusParams,
				getTxBriefStatusID(), getTxBriefStatusParams(), null, null, null);
	}
	
	public boolean isOpertionForRetry(){
		return operationForRetry;
	}
	
	public boolean isForgetSupported(){
		return forgetSupported;
	}
	
	public String getMessageForTrace(){
		try {
			if(messageForTrace == null){
				messageForTrace = LocalizableTextFormatter.formatString(TSResourceAccessor.getResourceAccessor(), rmStatusID, rmStatusParams);
			}
			return messageForTrace;
		} catch (LocalizationException e) {
			String exceptionalMessage = "Unexpected exception ocurred during localization of message with key " + rmStatusID 
			+ "and params " + rmStatusParams[0] +" , "+ rmStatusParams[1];
//			SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, exceptionalMessage, e);
			SimpleLogger.trace(Severity.ERROR,LOCATION, "ASJ.trans.000083", "Unexpected exception ocurred during localization of a message with key {0} and params {1} , {2}",  new Object[] { rmStatusID,rmStatusParams[0],rmStatusParams[1]});
			return exceptionalMessage;
		}
	}
	
	public String getTxBriefStatusID(){
		return "TX_BRIEF_" + rmStatusID;
	}
	
	public String[] getTxBriefStatusParams(){
		return txBriefParams;
	}
	
	public String getTxDetailedStatusID(){
		return "TX_DETAILED_" + rmStatusID;
	}
	
	public String[] getTxDetailedStatusParams(){
		return txDetailedParams;
	}
	
	public boolean hasErrors(){
		return isErrorOrWarning; 
	}
	
	public boolean hasWarnings(){
		return !isErrorOrWarning;
	}
	
}
