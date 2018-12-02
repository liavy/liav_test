package com.sap.engine.services.ts.exceptions;

/**
 * Transaction Service exception constants
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class ExceptionConstants {

  public static final String Transaction_marked_for_rollback = "ts_0001";
  public static final String Transaction_is_rolled_back = "ts_0002";
  public static final String Status_not_active = "ts_0003";
  public static final String Exception_in_beforeCompletion = "ts_0004";
  public static final String Transaction_rolled_back_spec_violation_error = "ts_0005";
  public static final String Commit_failed = "ts_0006";
  public static final String XAResource_not_commited = "ts_0007";
  public static final String Exception_in_afterCompletion = "ts_0008";
  public static final String Delist_already_delisted_XAResource = "ts_0009";
  public static final String XAResource_not_found = "ts_0010";
  public static final String XAResource_manager_not_found = "ts_0011";
  public static final String Transaction_is_prepared = "ts_0012";
  public static final String Exception_in_rollback = "ts_0013";
  public static final String No_running_transaction = "ts_0014";
  public static final String Prepare_of_OMG_Resource = "ts_0015";
  public static final String Commit_of_OMG_Resource = "ts_0016";
  public static final String Transaction_rolled_back_LT_resource_error = "ts_7117";

  public static final String No_ThreadContext_in_System_Thread = "ts_0018";
  public static final String Thread_already_has_transaction = "ts_0019";
  public static final String Exception_in_begin_of_transaction = "ts_0020";
  public static final String Thread_is_not_associated_with_transaction = "ts_0021";
  public static final String Exception_in_get_status_of_transaction = "ts_0022";
  public static final String Exception_in_set_rollback_only = "ts_0023";
  public static final String Not_valid_transaction_object = "ts_0024";

  public static final String Enlist_of_local_resource_in_OTS_Transaction_Not_Allowed = "ts_0026";
  public static final String Exception_in_commit_of_OMGWrapper = "ts_0027";
  public static final String Exception_in_rollback_of_OMGWrapper = "ts_0028";
  public static final String Exception_in_prepare_of_OMGWrapper = "ts_0029";

  public static final String Thread_Context_has_transaction_but_revived_null_current = "ts_0030";
  public static final String Error_setting_rollbackOnly_for_the_current_transaction = "ts_0031";

  // JCA Exceptions
  public static final String Exception_Commit_JCATransactio_used_in_another_thread = "ts_0032";
  public static final String Exception_Rollback_JCATransactio_used_in_another_thread = "ts_0033";
  public static final String Exception_one_phase_commit_of_unprepared_transaction = "ts_0034";
  
  public static final String Second_local_resource = "ts_0035";

  public static final String Exception_XAResource_End = "ts_0036";

  public static final String Exception_XAResource_isSameRM  = "ts_0037";
  public static final String Exception_local_resource_begin = "ts_0038";

  public static final String Exception_TS_Cannot_start = "ts_0039";

  public static final String CONNECTOR_NOT_AVAILABLE = "ts_0040";  
  public static final String INVALID_PRIORITY = "ts_0041";
  
  public static final String Cannot_enlist_RtEx_isSameRM = "ts_0042";
  public static final String Cannot_enlist_XAEx_isSameRM = "ts_0043";
  public static final String Cannot_enlist_RtEx_start_tmjoin = "ts_0044";
  public static final String Cannot_enlist_XAEx_start_tmjoin = "ts_0045";
  public static final String Cannot_enlist_RtEx_start_tmnoflag = "ts_0046";
  public static final String Cannot_enlist_XAEx_start_tmnoflag = "ts_0047";
  /**
   * TxManager exception constants
   **/
  public static final String BEGIN_TRANSACTION_ERROR = "ts_0048";
  public static final String SUSPEND_TRANSACTION_ERROR = "ts_0049";
  public static final String RESUME_TRANSACTION_ERROR = "ts_0050";
  public static final String COMMIT_TRANSACTION_ERROR = "ts_0051";
  public static final String ROLLBACK_TRANSACTION_ERROR = "ts_0052";
  public static final String SET_ROLLBACK_ONLY_ERROR = "ts_0053";
  public static final String GET_STATUS_ERROR = "ts_0054";
  public static final String GET_TRANSACTION_ERROR = "ts_0055";
  public static final String REGISTER_SYNCHRONIZATION_ERROR = "ts_0056";
  public static final String UNEXPECTED_STATUS_ERROR = "ts_0057";
  public static final String DOUBLE_REGISTRATION_ERROR = "ts_0058";
  public static final String UNBALANCED_DEMARCATION_ERROR = "ts_0059";
  public static final String MARKED_FOR_ROLLBACK = "ts_0060";
  public static final String DUPLICATE_OID_REGISTRATION_EXCEPTION = "ts_0061";
  public static final String ILLEGAL_REGISTRATION_EXCEPTION = "ts_0062";
  public static final String TX_SERVICE_NOT_READY = "ts_0063";
  public static final String TICKET_IS_NULL = "ts_0064";
  

  public static final String Transaction_Is_Started_From_Other_Component = "ts_0770";
  public static final String Cannot_Enlist_Resources_Into_Transaction = "ts_0771";
  public static final String Cannot_Delist_Resources_from_Transaction = "ts_0772";
  public static final String Associated_object_alwready_exist = "ts_0773"; 
  public static final String Current_Is_Null = "ts_0774";

  public static final String Unexpected_Exception_during_AbandonTransaction = "UNEXPECTED_EXCEPTION_DURING_ABANDON_TRANSACTION_TXid";
  				// {0} - transaction sequence number; {1} - exception caused the problem
  public static final String Unexpected_Exception_during_ForgetTransaction = "UNEXPECTED_EXCEPTION_DURING_FORGET_TRANSACTION_TXid";
  				// {0} - transaction sequence number; {1} - exception caused the problem
  public static final String Unexpected_Exception_during_RetrievePendingTxInfo= "UNEXPECTED_EXCEPTION_DURING_RETRIEVE_PENDING_TX_INFO_FOR_TXid";
  public static final String Pending_Transaction_is_already_completed = "PENDING_TRANSACTION_IS_ALREADY_COMPLETED_TXid";
  public static final String Unexpected_Exception_during_ClearTransactionStatistics = "UNEXPECTED_EXCEPTION_DURING_CLEAR_TRANSACTION_STATISTICS";
  public static final String Cannot_Get_Transaction_Statistics = "Cannot_get_transaction_statistics";
  				// {0} - exception caused the problem
  public static final String Transaction_Statistics_May_Not_Be_Full = "Transaction_statistics_may_not_be_full";
  				// {0} - exception caused the problem
  public static final String Cannot_Get_Pending_Transactions = "Cannot_get_pending_transactions";
  				// {0} - exception caused the problem
  public static final String Pending_Transactions_May_Not_Be_Full = "Pending_transactions_may_not_be_full";
  				// {0} - exception caused the problem
  public static final String Cannot_Get_Extended_Pending_Transaction_Info = "Cannot_get_extended_pending_transaction_info";
  				// {0} - transaction sequence number; {1} - exception caused the problem

}
