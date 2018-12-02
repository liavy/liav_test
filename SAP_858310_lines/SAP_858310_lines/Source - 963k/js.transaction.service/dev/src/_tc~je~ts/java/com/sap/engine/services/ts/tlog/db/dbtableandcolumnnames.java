package com.sap.engine.services.ts.tlog.db;

/**
 * 
 * DBTableAndColumnNames is used just to store the DB table and column names
 * on a single place. It should not be instantiated.
 *
 * @author Dimitar Iv. Dimitrov
 * @version SAP NetWeaver 7.11 SP0
 */
public abstract class DBTableAndColumnNames {
	// Table and column names for BC_JTA_TLOG table
	public static final String TLOG = "BC_JTA_TLOG";
	public static final String TLOG_ID = "TLOG_ID";
	public static final String TLOG_SYSTEM_ID = "SYSTEM_ID";
	public static final String TLOG_NODE_ID = "NODE_ID";
	public static final String TLOG_TM_STARTUP_TIME = "TM_STARTUP_TIME";
	public static final String TLOG_LEASED_UNTIL = "LEASED_UNTIL";

	// Table and column names for BC_JTA_TX_CLASS table
	public static final String TX_CLASSIFIER = "BC_JTA_TX_CLASS";
	public static final String TX_CLASSIFIER_TX_CLASSIFIER_ID = "TX_CLASSIFIER_ID";
	public static final String TX_CLASSIFIER_TLOG_ID = "TLOG_ID";
	public static final String TX_CLASSIFIER_CLASSIFIER = "TRANSACTION_CLASS";

	// Table and column names for BC_JTA_RES_MANAGER table
	public static final String RES_MANAGERS = "BC_JTA_RES_MANAGER";
	public static final String RES_MANAGERS_RM_ID = "RM_ID";
	public static final String RES_MANAGERS_TLOG_ID = "TLOG_ID";
	public static final String RES_MANAGERS_RM_CONTAINER_NAME = "RM_CONTAINER_NAME";
	public static final String RES_MANAGERS_RM_NAME = "RM_NAME";
	public static final String RES_MANAGERS_STATUS = "STATUS";
	// Constants for the STATUS column:
	public static final byte RES_MANAGERS_STATUS_ACTIVE = 0;
	public static final byte RES_MANAGERS_STATUS_MARKED_FOR_DELETE = 1;

	// Table and column names for BC_JTA_RM_PROPS table
	public static final String RM_PROPS = "BC_JTA_RM_PROPS";
	public static final String RM_PROPS_RM_ID = "RM_ID";
	public static final String RM_PROPS_TLOG_ID = "TLOG_ID";
	public static final String RM_PROPS_PROPERTY_TYPE = "PROPERTY_TYPE";
	public static final String RM_PROPS_PROPERTY_NAME = "PROPERTY_NAME";
	public static final String RM_PROPS_PROPERTY_VALUE = "PROPERTY_VALUE";
	// Two constants for the type of the property:
	// 0 - secure; 1 - non secure
	public static final byte RM_PROPS_SECURE_PROPERTY = 0;
	public static final byte RM_PROPS_NON_SECURE_PROPERTY = 1;

	// Table and column names for BC_JTA_TX_RECORDS table
	public static final String TX_RECORDS = "BC_JTA_TX_RECORDS";
	public static final String TX_RECORDS_TX_NUMBER = "TX_NUMBER";
	public static final String TX_RECORDS_TLOG_ID = "TLOG_ID";
	public static final String TX_RECORDS_TX_BIRTH_TIME = "TX_BIRTH_TIME";
	public static final String TX_RECORDS_TX_ABANDON_TIME = "TX_ABANDON_TIME";
	public static final String TX_RECORDS_TX_CLASSIFIER = "TX_CLASSIFIER";
	public static final String TX_RECORDS_BRANCH_IDS_LIST = "BRANCH_IDS_LIST";
	public static final String TX_RECORDS_BRANCH_ITER_LIST = "BRANCH_ITER_LIST";

	// Table and column names for BC_JTA_INBOUND_TX table
	public static final String INBOUND_TX = "BC_JTA_INBOUND_TX";
	// this table "extends" BC_JTA_TX_RECORDS so the field of BC_JTA_TX_RECORDS
	// are relevant for this table
	public static final String INBOUND_TX_EXTERNAL_XID = "EXTERNAL_XID";
	public static final String INBOUND_TX_HEURISTIC_OUTCOME = "HEURISTIC_OUTCOME";


	// Prevent class from being
	// instantiated by inheritance
	private DBTableAndColumnNames() {}
}
