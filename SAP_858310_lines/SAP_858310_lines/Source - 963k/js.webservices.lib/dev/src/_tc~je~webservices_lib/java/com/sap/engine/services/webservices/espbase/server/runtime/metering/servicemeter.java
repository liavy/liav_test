package com.sap.engine.services.webservices.espbase.server.runtime.metering;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.jobs.AggregationJob;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.jobs.DeletionJob;
import com.sap.sql.DuplicateKeyException;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class ServiceMeter{
  public static final String JNDI_NAME = "ServiceMeter";

  public static final int AGGREGATE_METERING_DATA_MESSAGE = 100; // message number for cluster communication

  private final static int MAX_METERING_CACHE_RECORDS = 1000; // DB cache size

  private static int SERVICE_METERING_BUFFER_SIZE = 1000; // in-memory record
  // buffer size

  private static int SERVICE_METERING_BUFFER_AGGREGATION_TRESHOLD = (int) (0.75 * SERVICE_METERING_BUFFER_SIZE); // start aggregating before buffer is full

  public static long OLD_RECORDS_DELETION_SCHEDULE = 5 * 24 * 60 * 60 * 1000;
  // // deletion occurs every 5 days
  public static int OLD_RECORDS_DELETION_TRESHOLD = 5; // delete records older than 5 months
  //public static long OLD_RECORDS_DELETION_SCHEDULE = 4 * 60 * 1000; // deletion

  //public static int OLD_RECORDS_DELETION_TRESHOLD = 1 * 60 * 1000; // delete
  
  private static final Calendar calUTC = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));

  // SQLs
  private final static String SQL_GET_CALLER_ID = "SELECT CALLERID FROM SMET_CALLER WHERE CALLINGAPP=? AND CALLINGCMPNT=? AND "
    + "CALLINGTYPE=? AND CALLINGCOM=? AND CALLINGSYS=? AND CALLINGUSR=?";

  private final static String SQL_GET_OP_ID = "SELECT OPID FROM SMET_OPERATION WHERE OPERATIONNAME=? AND INTSERVNAME=?";

  private final static String SQL_GET_LAST_BEGINTS = "SELECT BEGINTS, ENDTS FROM SMET_METERING WHERE CALLERID=? AND OPID=? AND SERVNODEID=? ORDER BY BEGINTS DESC";

  private final static String SQL_INSERT_METERING_RECORD = "INSERT INTO SMET_METERING (BEGINTS, SERVNODEID, CALLERID, OPID, METERINGID, PERIOD, ENDTS, CALLCOUNT) VALUES(?,?,?,?,?,?,?,?)";

  private final static String SQL_UPDATE_METERING_RECORD = "UPDATE SMET_METERING SET ENDTS=?, CALLCOUNT=CALLCOUNT+? WHERE CALLERID=? AND OPID=? AND SERVNODEID=? AND BEGINTS=?"; // !!

  private final static String SQL_INSERT_CALLER_RECORD = "INSERT INTO SMET_CALLER (CALLINGAPP, CALLINGCMPNT, CALLINGTYPE, CALLINGCOM, CALLINGSYS, CALLERID, CALLINGUSR) "
    + "VALUES (?,?,?,?,?,?,?)";

  private final static String SQL_INSERT_OPERATION_RECORD = "INSERT INTO SMET_OPERATION (OPERATIONNAME, INTSERVNAME, APPNAME, PORTTYPENAME, PORTTYPENS, OPID) "
    + "VALUES (?,?,?,?,?,?)";

  private final static String SQL_GET_INDEX = "SELECT TABLEINDEX FROM SMET_INDEX WHERE ID=? FOR UPDATE";

  private final static String SQL_INCREMENT_INDEX = "UPDATE SMET_INDEX SET TABLEINDEX=TABLEINDEX+1 WHERE ID=?";

  private final static String SQL_INSERT_INDEX = "INSERT INTO SMET_INDEX (ID, TABLEINDEX) VALUES(?,?)";

  //private final static String SQL_GET_OLD_CALLCOUNT = "SELECT CALLCOUNT FROM SMET_METERING WHERE CALLERID=? AND OPID=? AND SERVNODEID=? AND BEGINTS =?";

  private final static String SQL_CHECK_OP_ID = "SELECT OPID FROM SMET_OPERATION WHERE OPID=?";

  private final static String SQL_CHECK_CALLER_ID = "SELECT CALLERID FROM SMET_CALLER WHERE CALLERID=?";

  private BlockingQueue<MeteringInMemoryRecord> serviceMeteringBuffer = new LinkedBlockingQueue<MeteringInMemoryRecord>(
      SERVICE_METERING_BUFFER_SIZE);

  private LRUHashMap meteringCache = new LRUHashMap(MAX_METERING_CACHE_RECORDS);

  private static final Location LOC = Location.getLocation(ServiceMeter.class);

  private DataSource dataSource;

  private MessageContext clusterMsgContext;

  private AggregationJob aggregationJob;

  private DeletionJob deletionJob;

  private static ServiceMeter SERVICE_METER; // singleton

  private String instalationNumber = "";
  
  private String systemID = "";
  
  private int serverNodeId;
  
  
  private static class IntHolder {
    public int number = 1;
    
    public IntHolder(){
    }
  }

  public static void initState(ApplicationServiceContext appSrvCtx) throws ServiceMeteringException {
    SERVICE_METER = new ServiceMeter(appSrvCtx);
  }

  public static ServiceMeter getInstance() {
    return SERVICE_METER;
  }

  protected ServiceMeter(ApplicationServiceContext appSrvCtx) throws ServiceMeteringException {
    try {
      Context ctx = new InitialContext();
      dataSource = (DataSource) ctx.lookup("/jdbc/SAP/WS_LOG_TRACE"); // TODO: make dedicated metering alias DS
    } catch (NamingException ne) {
      throw new ServiceMeteringException("Cannot look up [/jdbc/SAP/WSRM]", ne);
    }
    this.clusterMsgContext = appSrvCtx.getClusterContext().getMessageContext();
    this.serverNodeId = appSrvCtx.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId();
    this.aggregationJob = new AggregationJob(this, appSrvCtx.getCoreContext().getThreadSystem());
    this.deletionJob = new DeletionJob(this, appSrvCtx.getCoreContext().getThreadSystem());
  }

  public void meterCall(ProviderContextHelper context) throws Exception {
    try {
      doMeter(new MeteringInMemoryRecord(context));
    } catch (RuntimeProcessException rpe) {
      LOC.traceThrowableT(Severity.WARNING, "Error while processing request metering data", rpe);
    }
  }

  private void doMeter(MeteringInMemoryRecord minmr) throws Exception {
    if (serviceMeteringBuffer.size() > SERVICE_METERING_BUFFER_AGGREGATION_TRESHOLD) {
      triggerAggregation(null);
    }
    serviceMeteringBuffer.put(minmr);
  }

  // 1. delete metering records older than `olderThan`
  // 2. delete caller records that refer to non-existing metering records
  // 3. delete operation records that refer to non-existing metering records
  // does not invalidate cache, it is updated on the first attempt to use
  // invalid cache entry
  public void doDeleteMeteringRecords(Timestamp olderThan) throws SQLException {
    Connection connection = null;
    PreparedStatement delMetStatement = null;
    PreparedStatement delCallStatement = null;
    PreparedStatement delOpStatement = null;
     // !! TEST !!
    //olderThan = new Timestamp(new Date().getTime() - 60*1000);
    // !!
    
    LOC.pathT("[doDeleteMeteringRecords] Deleting metering records older than [" + olderThan + "]");
    try {
      connection = dataSource.getConnection();
      delMetStatement = connection.prepareStatement("DELETE FROM SMET_METERING WHERE ENDTS < ?");
      delMetStatement.setTimestamp(1, olderThan);
      delCallStatement = connection
      .prepareStatement("DELETE FROM SMET_CALLER WHERE SMET_CALLER.CALLERID NOT IN (SELECT CALLERID FROM SMET_METERING)");
      delOpStatement = connection
      .prepareStatement("DELETE FROM SMET_OPERATION WHERE SMET_OPERATION.OPID NOT IN (SELECT OPID FROM SMET_METERING)");

      LOC.pathT("[doDeleteMeteringRecords] Deleting metering records older than [" + olderThan + "]");
      delMetStatement.execute();
      LOC.pathT("[doDeleteMeteringRecords] Deleting orphaned Caller records");
      delCallStatement.execute();
      LOC.pathT("[doDeleteMeteringRecords] Deleting orphaned Operation records");
      delOpStatement.execute();
      LOC.pathT("[doDeleteMeteringRecords] Records deleted");
    } finally {
      closeStatement(delMetStatement, delCallStatement, delOpStatement);
      if (connection != null) {
        connection.close();
      }
    }
  }

  public static Timestamp getBeforeTimestamp(int howLongBefore, int field){
    GregorianCalendar gc = new GregorianCalendar();
    gc.add(field, howLongBefore);
    return new Timestamp(gc.getTimeInMillis());
  }

  // checks the timestamp in SMET_DELETION_TS
  // if older than OLD_RECORDS_DELETION_SCHEDULE, updates the timestamp to
  // indicate that deletion job will start on this node
  public boolean mustTriggerDeletionJob() throws SQLException {
    Connection connection = null;
    PreparedStatement checkTsStatement = null;
    PreparedStatement tsStatement = null;
    ResultSet rs = null;
    LOC.pathT("[isDeletionJobRunningOnCluster] Checking last deletion timestamp in DB");
    boolean res = false;
    try {
      connection = dataSource.getConnection();
      Date now = new Date();
      Timestamp deletionTresholdTs = getBeforeTimestamp((int)(OLD_RECORDS_DELETION_SCHEDULE / (1000 * 60 * 60)), GregorianCalendar.HOUR);
      
      connection.setAutoCommit(false);
      checkTsStatement = connection.prepareStatement("SELECT LASTDELETIONTS FROM SMET_DELETION_TS WHERE ID=1 FOR UPDATE");
      rs = checkTsStatement.executeQuery();
      
      Timestamp lastDelTs = null;
      while (rs.next()) { // should be only 1 row
        lastDelTs = rs.getTimestamp(1);
      }
      rs.close(); 
      if (lastDelTs == null) {
        LOC.pathT("[isDeletionJobRunningOnCluster] No deletion timestamp found in DB, inserting new timestamp");
        tsStatement = connection.prepareStatement("INSERT INTO SMET_DELETION_TS (LASTDELETIONTS, ID) VALUES(?, 1)");
        tsStatement.setTimestamp(1, new Timestamp(now.getTime()));
        tsStatement.execute();
        res = true;
      }

      if (lastDelTs.before(deletionTresholdTs)) { // time for update
        LOC.pathT("[isDeletionJobRunningOnCluster] Timestamp in DB older than deletion treshold, will trigger deletion job");
        tsStatement = connection.prepareStatement("UPDATE SMET_DELETION_TS SET LASTDELETIONTS=? WHERE ID=1");
        tsStatement.setTimestamp(1, new Timestamp(now.getTime()));
        tsStatement.execute();
        res = true;
      }
      connection.commit();
      return res;
    } catch (SQLException sqle) {
      LOC.pathT("[isDeletionJobRunningOnCluster] Error while checking deletion timestamp in DB");
      connection.rollback();
      throw sqle;
    } finally {
      connection.setAutoCommit(true);
      closeStatement(checkTsStatement, tsStatement);
      if (rs != null) {
        rs.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
  }

  public synchronized void flush() throws Exception {
    LOC.pathT("[flush] Sending cluster-wide message for metering records aggregation");
    clusterMsgContext
    .sendAndWaitForAnswer(0, (byte) -1, AGGREGATE_METERING_DATA_MESSAGE, new byte[] { 0 }, 0, 0, 60000);
  }

  // starts a new thread to do records aggregation
  public void triggerAggregation(MeteringInMemoryRecord monitor) throws Exception {
    if (monitor != null) {
      serviceMeteringBuffer.put(monitor);
    }
    LOC.pathT("[triggerAggregation] Starting aggregation job");
    aggregationJob.start();
  }

  // starts a new thread to delete old records
  public void triggerOldRecordDeletion(Timestamp olderThan) {
    LOC.pathT("[triggerOldRecordDeletion] Starting deletion job");
    deletionJob.start(olderThan);
  }

  public int doAggregateServiceCalls() throws ServiceMeteringException {
    LOC.pathT("[doAggregateServiceCalls] Starting metering records aggregation");
    int aggregatedRecords = 0;

    Hashtable<MeteringInMemoryRecord, IntHolder> flatBuffer = flattenServiceMeteringBuffer();
    if (flatBuffer.size() > 0) {
      aggregatedRecords = updateDB(flatBuffer);
    }

    LOC.pathT("[doAggregateServiceCalls] Metering records aggregation completed");
    return aggregatedRecords;
  }

  private Hashtable<MeteringInMemoryRecord, IntHolder> flattenServiceMeteringBuffer() throws ServiceMeteringException {
    LOC.pathT("[flattenServiceMeteringBuffer] Compacting metering in-memory buffer");
    Hashtable<MeteringInMemoryRecord, IntHolder> flatBuffer = new Hashtable<MeteringInMemoryRecord, IntHolder>();
    MeteringInMemoryRecord mimr;
    IntHolder ih;
    int recordsNotPersisted = 0;
    boolean isNotificator = false;
    while (serviceMeteringBuffer.peek() != null) {
      mimr = serviceMeteringBuffer.poll(); // non-blocking in current Queue 'implementation
      isNotificator = mimr instanceof MeteringInMemoryRecordNotificator;
      if (!isNotificator) {
        ih = flatBuffer.get(mimr); 
        if (ih == null) {
          flatBuffer.put(mimr, new IntHolder());
        } else {
          ih.number++;
        }
      }
      if (isNotificator || recordsNotPersisted > SERVICE_METERING_BUFFER_AGGREGATION_TRESHOLD) {
        if (flatBuffer.size() > 0) {
          updateDB(flatBuffer);
          flatBuffer.clear();
        }
        recordsNotPersisted = -1;
        mimr.receiveNotification();
      }
      recordsNotPersisted++;
    }
    LOC.pathT("[flattenServiceMeteringBuffer] Compacting metering in-memory buffer completed, [" + flatBuffer.size()
        + "] records in flatBuffer");
    return flatBuffer;
  }

  private void closeStatement(PreparedStatement... ps) throws SQLException {
    for (PreparedStatement p : ps) {
      if (p != null) {
        p.close();
      }
    }
  }

  private long determineCallerId(MeteringInMemoryRecord mimr, Connection connection,
      PreparedStatement getCallerIdStatement,    PreparedStatement getIndexStatement, 
      PreparedStatement incrementIndexStatement, PreparedStatement insertIndexStatement,
      PreparedStatement insertCallerStatement,   IntHolder duplicateKeyIndicator, IntHolder recordFoundInDBIndicator) throws Exception{
    long callerId;
    duplicateKeyIndicator.number = 0;
    recordFoundInDBIndicator.number = 0;
    MeteringRecordKey mkDB = initCallerIdFromDB(mimr, getCallerIdStatement);
    if (mkDB == null) { // no previous Caller record
      callerId = getNextCallerId(connection, getIndexStatement, incrementIndexStatement, insertIndexStatement);
      try {
        insertCallerRecordInDB(insertCallerStatement, mimr, callerId);
      } catch (DuplicateKeyException dke) { // someone inserted it before us 
        LOC.pathT("[determineCallerId] DuplicateKeyException caught while inserting Caller metering info in DB");
        //mkDB = initCacheFromDB(mimr, getCallerIdStatement, getOpIdStatement, true, false);
        mkDB = initCallerIdFromDB(mimr, getCallerIdStatement);
        if (mkDB == null) { // give up
          throw new RuntimeException("[determineCallerId] Cannot retrieve callerId from DB");
        }
        callerId = mkDB.callerId;
        duplicateKeyIndicator.number = 1;
      }
    }else{
      callerId = mkDB.callerId;
      recordFoundInDBIndicator.number = 1;
    }
    return callerId;
  }

  private long determineOperationId(MeteringInMemoryRecord mimr, Connection connection,
      PreparedStatement getOpIdStatement,         PreparedStatement getIndexStatement, 
      PreparedStatement incrementIndexStatement,  PreparedStatement insertIndexStatement,
      PreparedStatement insertOperationStatement, IntHolder duplicateKeyIndicator, IntHolder recordFoundInDBIndicator) throws Exception{
    long opId;
    duplicateKeyIndicator.number = 0;
    recordFoundInDBIndicator.number = 0;
    MeteringRecordKey mkDB = initOpIdFromDB(mimr, getOpIdStatement);
    if (mkDB == null){ // no previous Operation record
      opId = getNextOperationId(connection, getIndexStatement, incrementIndexStatement, insertIndexStatement);
      try {
        insertOperationRecordInDB(insertOperationStatement, mimr, opId);
      } catch (DuplicateKeyException dke) {
        LOC.pathT("[determineCallerId] DuplicateKeyException caught while inserting Operation metering info in DB");
        mkDB = initOpIdFromDB(mimr, getOpIdStatement);
        if (mkDB == null) { // give up
          throw new RuntimeException("[determineOperationId] Cannot retrieve copId from DB");
        }
        opId = mkDB.operationId;
        duplicateKeyIndicator.number = 1;
      }
    }else{
      opId = mkDB.operationId;
      recordFoundInDBIndicator.number = 1;
    }
    return opId;
  }


  private int updateDB(Hashtable<MeteringInMemoryRecord, IntHolder> flatBuffer) throws ServiceMeteringException {
    Connection connection = null;
    PreparedStatement getCallerIdStatement          = null, getLastBeginTsStatement       = null, 
    insertMeteringRecordStatement = null, updateMeteringRecordStatement = null, 
    insertCallerStatement         = null, getIndexStatement             = null, 
    getOldCallCount               = null, incrementIndexStatement       = null, 
    insertIndexStatement          = null, getOpIdStatement              = null, 
    checkOpIdStatement            = null, checkCallerIdStatement        = null, 
    insertOperationStatement      = null;

    LOC.pathT("[updateDB] Updating metering info in DB, will persist [" + flatBuffer.size() + "] records.");
    // prepare sql statements
    try { // TODO check if needed all SQLs to be initialized on this step.
      connection = dataSource.getConnection();
      getCallerIdStatement = connection.prepareStatement(SQL_GET_CALLER_ID);
      getOpIdStatement = connection.prepareStatement(SQL_GET_OP_ID);
      getLastBeginTsStatement = connection.prepareStatement(SQL_GET_LAST_BEGINTS);
      //insertMeteringRecordStatement = connection.prepareStatement(SQL_INSERT_METERING_RECORD);
      insertMeteringRecordStatement = NativeSQLAccess.prepareNativeStatement(connection, SQL_INSERT_METERING_RECORD);
      insertCallerStatement = connection.prepareStatement(SQL_INSERT_CALLER_RECORD);
      insertOperationStatement = connection.prepareStatement(SQL_INSERT_OPERATION_RECORD);
      //getOldCallCount = connection.prepareStatement(SQL_GET_OLD_CALLCOUNT);
      //updateMeteringRecordStatement = connection.prepareStatement(SQL_UPDATE_METERING_RECORD);
      updateMeteringRecordStatement = NativeSQLAccess.prepareNativeStatement(connection, SQL_UPDATE_METERING_RECORD);
      getIndexStatement = connection.prepareStatement(SQL_GET_INDEX);
      incrementIndexStatement = connection.prepareStatement(SQL_INCREMENT_INDEX);
      insertIndexStatement = connection.prepareStatement(SQL_INSERT_INDEX);
      checkOpIdStatement = connection.prepareStatement(SQL_CHECK_OP_ID);
      checkCallerIdStatement = connection.prepareStatement(SQL_CHECK_CALLER_ID);
    } catch (SQLException se) {
      LOC.traceThrowableT(Severity.WARNING, "Error preparing metering SQL statements", se);
      try {
        closeStatement(getCallerIdStatement, getLastBeginTsStatement, insertMeteringRecordStatement,
            updateMeteringRecordStatement, insertCallerStatement, insertOperationStatement, getIndexStatement,
            /*getOldCallCount,*/ incrementIndexStatement, getOpIdStatement, insertIndexStatement, checkOpIdStatement,
            checkCallerIdStatement);
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException sqle) {
        throw new ServiceMeteringException("Error closing connection: [" + sqle.getMessage() + "]", se);
      }
      throw new ServiceMeteringException("Error constructing prepared statements", se);
    }

    // loop over records and update DB
    MeteringInMemoryRecord mimr;
    MeteringRecordKey mkCache, mkDB;
    Timestamp beginTsForCurrentPeriod, endTsForCurrentPeriod; // for current (or last - might be expired) period
    IntHolder currentRecordCallCount, 
    duplicateKeyIndicator    = new IntHolder(),
    recordFoundInDBIndicator = new IntHolder();
    long callerId, opId;
    int month, year, aggregatedRecords = 0;
    boolean notInMeteringTable, duplicateKey, recordFoundInDB;

    Enumeration<MeteringInMemoryRecord> records = flatBuffer.keys();
    
    //Calendar cal = Calendar.getInstance();
    //long nowLong = cal.getTimeInMillis();
    long nowLong = new Date().getTime();
    Timestamp nowTimeStamp = new Timestamp(nowLong);
    //int currentMonth = cal.get(Calendar.MONTH);
    int currentMonth = nowTimeStamp.getMonth();
    int currentYear = nowTimeStamp.getYear() + 1900;

    while (records.hasMoreElements()) {
      mimr = records.nextElement();

      // get callerId and beginTs - check memory cache first
      mkCache = (MeteringRecordKey) meteringCache.get(mimr);
      currentRecordCallCount = flatBuffer.get(mimr);

      // cache empty - means 1.) first call of this operation by this user -
      // cache can be updated and mimr values used as keys for metering table
      // OR 2.) server was restarted/cache grew too big, cache (partially) lost,
      // sync cache with DB
      duplicateKey    = false;
      recordFoundInDB = false;
      if (mkCache == null) {
        try{
          callerId = determineCallerId(mimr, connection, getCallerIdStatement, getIndexStatement, incrementIndexStatement, 
              insertIndexStatement, insertCallerStatement, duplicateKeyIndicator, recordFoundInDBIndicator);
          duplicateKey = duplicateKeyIndicator.number == 1;
          recordFoundInDB = recordFoundInDBIndicator.number == 1;
          opId     = determineOperationId(mimr, connection, getOpIdStatement, getIndexStatement, incrementIndexStatement, 
              insertIndexStatement, insertOperationStatement, duplicateKeyIndicator, recordFoundInDBIndicator);
          duplicateKey = duplicateKeyIndicator.number == 1 || duplicateKey;
          recordFoundInDB = recordFoundInDBIndicator.number == 1 || recordFoundInDB;

          if (duplicateKey) { // check if metering record already exists
            beginTsForCurrentPeriod = new Timestamp(nowLong);
            endTsForCurrentPeriod = new Timestamp(nowLong);
            notInMeteringTable = !getLastTsFromDB(getLastBeginTsStatement, callerId, opId, serverNodeId,
                beginTsForCurrentPeriod, endTsForCurrentPeriod);
            month = endTsForCurrentPeriod.getMonth();
            year  = endTsForCurrentPeriod.getYear() + 1900;
          } else {
            beginTsForCurrentPeriod = endTsForCurrentPeriod = nowTimeStamp;
            month = currentMonth;
            year  = currentYear; 
            notInMeteringTable = true;
          }

          if (recordFoundInDB){ // maybe there's already a metering record for this caller+op in DB
            beginTsForCurrentPeriod = new Timestamp(nowLong);
            endTsForCurrentPeriod = new Timestamp(nowLong);
            notInMeteringTable = !getLastTsFromDB(getLastBeginTsStatement, callerId, opId, serverNodeId,
                beginTsForCurrentPeriod, endTsForCurrentPeriod);
            month = endTsForCurrentPeriod.getMonth();
            year  = endTsForCurrentPeriod.getYear() + 1900;
          }
        }catch (Exception e) {
          LOC.traceThrowableT(Severity.WARNING, "Error persisting metering record", e);
          continue; // skip this record
        }
        meteringCache.put(mimr, new MeteringRecordKey(callerId, opId, beginTsForCurrentPeriod, nowTimeStamp, month, year));
      } else { // record found in cache; check whether record in cache is expired is done later
        callerId = mkCache.callerId;
        opId = mkCache.operationId;
        beginTsForCurrentPeriod = mkCache.beginTS;
        endTsForCurrentPeriod = mkCache.endTS;
        month = mkCache.month;
        year = mkCache.year;
        notInMeteringTable = false; // best guess
      }

      // if cache record is expired, update cache and insert new record in metering table
      boolean insertNewMeteringRecord = (month < currentMonth || year < currentYear) || notInMeteringTable;
      boolean meteringRecordFound = true;
      if (! insertNewMeteringRecord){
        meteringRecordFound = updateMeteringRecordInDB(connection, updateMeteringRecordStatement, /*getOldCallCount,*/ callerId, opId,
            currentRecordCallCount.number, serverNodeId, beginTsForCurrentPeriod, nowTimeStamp);
        if (mkCache != null) {
          mkCache.endTS = nowTimeStamp;
        }
      }

      if (insertNewMeteringRecord || ! meteringRecordFound) {
        insertMeteringRecordInDB(connection, insertMeteringRecordStatement, callerId, opId, mimr, serverNodeId,
            nowTimeStamp, currentRecordCallCount.number, getIndexStatement, incrementIndexStatement,
            insertIndexStatement, checkOpIdStatement, checkCallerIdStatement, insertCallerStatement,
            insertOperationStatement, getCallerIdStatement, getOpIdStatement);
        meteringCache.put(mimr, new MeteringRecordKey(callerId, opId, nowTimeStamp, nowTimeStamp, currentMonth, currentYear));
      }     
      aggregatedRecords++;
    }

    // cleanup
    try {
      closeStatement(getCallerIdStatement, getLastBeginTsStatement, insertMeteringRecordStatement,
          updateMeteringRecordStatement, insertCallerStatement, insertOperationStatement, getIndexStatement,
          incrementIndexStatement, insertIndexStatement, /*getOldCallCount,*/ getOpIdStatement, checkOpIdStatement,
          checkCallerIdStatement);
      connection.close();
    } catch (SQLException sqle) {
      LOC.traceThrowableT(Severity.WARNING, "Error closing metering SQL statements", sqle);
    }
    LOC.pathT("[updateDB] Updating metering info in DB completed");
    return aggregatedRecords;
  }

  private MeteringRecordKey initCallerIdFromDB(MeteringInMemoryRecord mimr, PreparedStatement getCallerIdStatement){
    try{
      getCallerIdStatement.setString(1, mimr.getCallingAppName());
      getCallerIdStatement.setString(2, mimr.getCallingComponent());
      getCallerIdStatement.setString(3, mimr.getCallingType());
      getCallerIdStatement.setString(4, mimr.getCallingCompany());
      getCallerIdStatement.setString(5, mimr.getCallingSys());
      getCallerIdStatement.setString(6, mimr.getCallingUserCode());
    } catch(SQLException sqle){
      LOC.traceThrowableT(Severity.WARNING, "[initCallerIdFromDB] Error reading opId from DB", sqle);
      return null;
    }
    IntHolder result = new IntHolder();
    long id = queryDBForLong(getCallerIdStatement, result);
    return result.number == 1 ? new MeteringRecordKey(id, 0, null, null, 0, 2000) : null;
  }

  private MeteringRecordKey initOpIdFromDB(MeteringInMemoryRecord mimr, PreparedStatement getOpIdStatement){
    try{
      getOpIdStatement.setString(1, mimr.getWSDLOperationName());
      getOpIdStatement.setString(2, mimr.getInternalServiceName());
    }catch(SQLException sqle){
      LOC.traceThrowableT(Severity.WARNING, "[initOpIdFromDB] Error reading opId from DB", sqle);
      return null;
    }
    IntHolder result = new IntHolder();
    long id = queryDBForLong(getOpIdStatement, result);
    return result.number == 1 ? new MeteringRecordKey(0, id, null, null, 0, 2000) : null;
  }

//resultValid.number = 0 -> nothing found in DB
  private long queryDBForLong(PreparedStatement pst, IntHolder resultValid) {
    resultValid.number = 0;
    if (pst == null){
      return 0;
    }
    ResultSet rs = null;
    long id = 0;

    try{
      rs = pst.executeQuery();
      while (rs.next()) { // should be only 1 row
        id = rs.getLong(1);
        resultValid.number = 1;
        break;
      }
      return id; 
    }catch(SQLException sqle){
      LOC.traceThrowableT(Severity.WARNING, "[queryDBForLong] Error reading from DB", sqle);
      return 0;
    }finally {
      try{
        rs.close();
      }catch(SQLException se){
        LOC.traceThrowableT(Severity.WARNING, "[queryDBForLong] Error closing result set", se);
      }
    }
  }


  /*private MeteringRecordKey initCacheFromDB(MeteringInMemoryRecord mimr, PreparedStatement getCallerIdStatement,
      PreparedStatement getOpIdStatement) {

    MeteringRecordKey callerKey = initCallerIdFromDB(mimr, getCallerIdStatement);
    MeteringRecordKey opKey     = initOpIdFromDB(mimr, getOpIdStatement);
    return callerKey == null || opKey == null ? null : new MeteringRecordKey(callerKey.callerId, opKey.operationId, null, null, 0);
  }*/


//DuplicateKeyException handled by caller
  private void insertCallerRecordInDB(PreparedStatement insertCallerStatement, MeteringInMemoryRecord mimr,
      long callerId) throws Exception {
    try {
      insertCallerStatement.setString(1, mimr.getCallingAppName());
      insertCallerStatement.setString(2, mimr.getCallingComponent());
      insertCallerStatement.setString(3, mimr.getCallingType());
      insertCallerStatement.setString(4, mimr.getCallingCompany());
      insertCallerStatement.setString(5, mimr.getCallingSys());
      insertCallerStatement.setLong(6, callerId);
      insertCallerStatement.setString(7, mimr.getCallingUserCode());
      insertCallerStatement.execute();
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING,
          "[insertCallerRecordInDB] Error inserting caller data, transaction rolled back", e);
      throw e;
    }
  }

//DuplicateKeyException handled by caller
  private void insertOperationRecordInDB(PreparedStatement insertOperationStatement, MeteringInMemoryRecord mimr,
      long opId) throws Exception {
    try {
      insertOperationStatement.setString(1, mimr.getWSDLOperationName());
      insertOperationStatement.setString(2, mimr.getInternalServiceName());
      insertOperationStatement.setString(3, mimr.getApplicationName());
      insertOperationStatement.setString(4, mimr.getPortTypeName());
      insertOperationStatement.setString(5, mimr.getPortTypeNamespace());
      insertOperationStatement.setLong(6, opId);
      insertOperationStatement.execute();
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING,
          "[insertOperationRecordInDB] Error inserting operation data, transaction rolled back", e);
      throw e;
    }
  }

  //not necessary to handle DuplicateKeyException here
  private boolean insertMeteringRecordInDB(Connection connection, PreparedStatement insertMeteringInfoStatement, long callerId,
      long opId, MeteringInMemoryRecord mimr, int serverNodeId, Timestamp ts, int callCount,
      PreparedStatement getIndexStatement, PreparedStatement incrementIndexStatement,
      PreparedStatement insertIndexStatement, PreparedStatement checkOpIdStatement,
      PreparedStatement checkCallerIdStatement, PreparedStatement insertCallerStatement,
      PreparedStatement insertOperationStatement, PreparedStatement getCallerIdStatement,
      PreparedStatement getOpIdStatement) {
    try {
      long metId = getNextMeteringId(connection, getIndexStatement, incrementIndexStatement, insertIndexStatement);
      insertMeteringInfoStatement.setTimestamp(1, ts, calUTC);
      insertMeteringInfoStatement.setInt(2, serverNodeId);
      insertMeteringInfoStatement.setLong(3, callerId);
      insertMeteringInfoStatement.setLong(4, opId);
      insertMeteringInfoStatement.setLong(5, metId);
      insertMeteringInfoStatement.setString(6, "M");
      insertMeteringInfoStatement.setTimestamp(7, ts, calUTC);
      insertMeteringInfoStatement.setInt(8, callCount);
      insertMeteringInfoStatement.execute();

      // check whether the referred callerId and opId are not accidentally
      // deleted (by the deletion job)
      // reinsert them if necessary
      checkIfIdsExistAndReinsert(connection, callerId, opId, metId, mimr, checkOpIdStatement, checkCallerIdStatement,
          insertCallerStatement, insertOperationStatement, getCallerIdStatement, getOpIdStatement);
    } catch (SQLException sqle) {
      LOC.traceThrowableT(Severity.WARNING, "Error inserting metering data", sqle);
      return false;
    }
    return true;
  }

//returns true if records reinserted in DB
  private boolean checkIfIdsExistAndReinsert(Connection connection, long callerId, long opId, long metId,
      MeteringInMemoryRecord mimr, PreparedStatement checkOpIdStatement, PreparedStatement checkCallerIdStatement,
      PreparedStatement insertCallerStatement, PreparedStatement insertOperationStatement,
      PreparedStatement getCallerIdStatement, PreparedStatement getOpIdStatement) throws SQLException {
    ResultSet rsOpId = null;
    ResultSet rsCallerId = null;
    try {
      checkOpIdStatement.setLong(1, opId);
      rsOpId = checkOpIdStatement.executeQuery();
      boolean opIdFound = false;
      boolean callerIdFound = false;
      boolean doMeteringRecordUpdate = false;
      while (rsOpId.next()) { // should be only 1 row
        opIdFound = true;
        break;
      }

      checkCallerIdStatement.setLong(1, callerId);
      rsCallerId = checkCallerIdStatement.executeQuery();
      while (rsCallerId.next()) { // should be only 1 row
        callerIdFound = true;
        break;
      }

      if (!callerIdFound) {
        try {
          insertCallerRecordInDB(insertCallerStatement, mimr, callerId);
        } catch (DuplicateKeyException dke) { // someone did it before us - take
          LOC.pathT("[checkIfIdsExistAndReinsert] DuplicateKeyException caught while inserting Caller metering info in DB");
          // the actual callerId from DB and update metering record
          MeteringRecordKey mrk = initCallerIdFromDB(mimr, getCallerIdStatement);
          if (mrk == null) { // give up
            return false;
          }
          callerId = mrk.callerId;
          doMeteringRecordUpdate = true;
        }
      }
      if (!opIdFound) {
        try {
          insertOperationRecordInDB(insertOperationStatement, mimr, opId);
        } catch (DuplicateKeyException dke) { // someone did it before us - take
          LOC.pathT("[checkIfIdsExistAndReinsert] DuplicateKeyException caught while inserting Operation metering info in DB");
          // the actual opId from DB and update metering record
          MeteringRecordKey mrk = initOpIdFromDB(mimr, getOpIdStatement);
          if (mrk == null) { // give up
            return false;
          }
          opId = mrk.operationId;
          doMeteringRecordUpdate = true;
        }
      }
      if (doMeteringRecordUpdate) {
        PreparedStatement updateMeteringStatement = null;
        try {
          updateMeteringStatement = connection
          .prepareStatement("UPDATE SMET_METERING SET CALLERID=?, OPID=? WHERE METERINGID=?");
          updateMeteringStatement.setLong(1, callerId);
          updateMeteringStatement.setLong(2, opId);
          updateMeteringStatement.setLong(3, metId);
          updateMeteringStatement.execute();
        } finally {
          if (updateMeteringStatement != null) {
            updateMeteringStatement.close();
          }
        }
      }
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING,
          "[checkIfOpIdExistsAndReinsert] Error inserting caller and operation data in DB", e);
      return false;
    } finally {
      if (rsOpId != null) {
        rsOpId.close();
      }
      if (rsCallerId != null) {
        rsCallerId.close();
      }
    }
    return true;
  }

//!! terribly ineffective update
  // returns false if update failed because of missing metering record - caller must insert a new one
  private boolean updateMeteringRecordInDB(Connection connection, PreparedStatement updateCallCountStatement,
      /*PreparedStatement getOldCallCountStatement,*/ long callerId, long opId, int callCount, int serverNodeId,
      Timestamp beginTs, Timestamp endTs) {
    try {
      // get old callCount
      /*getOldCallCountStatement.setLong(1, callerId);
      getOldCallCountStatement.setLong(2, opId);
      getOldCallCountStatement.setInt(3, serverNodeId);
      getOldCallCountStatement.setTimestamp(4, beginTs);
      ResultSet rs = getOldCallCountStatement.executeQuery();
      int oldCallCount = 0;
      boolean idFound = false;
      while (rs.next()) { // must be only 1 row
        oldCallCount = rs.getInt(1);
        idFound = true;
      }
      rs.close();
      if (!idFound) {
        LOC.warningT("[updateMeteringRecordInDB] Cannot find metering record for callerId [" + callerId + "], operationId [" + opId + "], will insert a new one");
        return false;
      }else{
        callCount += oldCallCount;
        updateCallCountStatement.setTimestamp(1, endTs);
        updateCallCountStatement.setInt(2, callCount);
        updateCallCountStatement.setLong(3, callerId);
        updateCallCountStatement.setLong(4, opId);
        updateCallCountStatement.setInt(5, serverNodeId);
        updateCallCountStatement.setTimestamp(6, beginTs);
        updateCallCountStatement.execute();
      }*/
      
      //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
      updateCallCountStatement.setTimestamp(1, endTs, calUTC);
      updateCallCountStatement.setInt(2, callCount);
      updateCallCountStatement.setLong(3, callerId);
      updateCallCountStatement.setLong(4, opId);
      updateCallCountStatement.setInt(5, serverNodeId);
      updateCallCountStatement.setTimestamp(6, beginTs, calUTC);
      int updated = updateCallCountStatement.executeUpdate();
      //int updated = updateCallCountStatement.getUpdateCount();
      if (updated < 1){
        LOC.warningT("[updateMeteringRecordInDB] Cannot find metering record for callerId [" + callerId + "], operationId [" + opId + "], will insert a new one");
        return false;
      }
    } catch (SQLException sqle) {
      LOC.traceThrowableT(Severity.WARNING, "Error updating metering data", sqle);
    }
    return true;
  }

  private boolean getLastTsFromDB(PreparedStatement statement, long callerId, long opId, int serverNodeId,
      Timestamp beginTS, Timestamp endTS) {
    boolean found = false;
    ResultSet rs = null;
    try {
      statement.setLong(1, callerId);
      statement.setLong(2, opId);
      statement.setInt(3, serverNodeId);
      rs = statement.executeQuery();
      Timestamp ts = null;
      while (rs.next()) { // last begin ts must be on top
        ts = rs.getTimestamp(1);
        beginTS.setTime(ts.getTime());
        ts = rs.getTimestamp(2);
        endTS.setTime(ts.getTime());
        found = true;
        break;
      }
    } catch (SQLException sqle) {
      LOC.traceThrowableT(Severity.WARNING, "[getLastTsFromDB] Error getting metering data from DB", sqle);
    } finally {
      if (rs != null) {
        try{
          rs.close();
        }catch(SQLException e){
          LOC.traceThrowableT(Severity.WARNING, "[getLastTsFromDB] Error closing result set", e);
        }
      }
    }
    return found;
  }

  private long getNextCallerId(Connection connection, PreparedStatement getIndexStatement,
      PreparedStatement incrementIndexStatement, PreparedStatement insertIndexStatement) throws SQLException {
    return getNextId((short) 2, getIndexStatement, incrementIndexStatement, insertIndexStatement, connection);
  }

  private long getNextOperationId(Connection connection, PreparedStatement getIndexStatement,
      PreparedStatement incrementIndexStatement, PreparedStatement insertIndexStatement) throws SQLException {
    return getNextId((short) 1, getIndexStatement, incrementIndexStatement, insertIndexStatement, connection);
  }

  private long getNextMeteringId(Connection connection, PreparedStatement getIndexStatement,
      PreparedStatement incrementIndexStatement, PreparedStatement insertIndexStatement) throws SQLException {
    return getNextId((short) 3, getIndexStatement, incrementIndexStatement, insertIndexStatement, connection);
  }

  private long getNextId(short tableIndex, PreparedStatement getIndexStatement,
      PreparedStatement incrementIndexStatement, PreparedStatement insertIndexStatement, Connection connection)
  throws SQLException {
    // get last id
    try {
      connection.setAutoCommit(false);
      getIndexStatement.setShort(1, tableIndex);
      ResultSet rs = getIndexStatement.executeQuery();
      long id = 0;
      boolean idFound = false;
      while (rs.next()) { // must be only 1 row
        id = rs.getLong(1);
        idFound = true;
      }
      rs.close();
      if (!idFound) {// first time - add index for this table
        id = Long.MIN_VALUE;
        insertIndexStatement.setShort(1, tableIndex);
        insertIndexStatement.setLong(2, id + 1);
        insertIndexStatement.execute();
      } else {
        // increment id
        incrementIndexStatement.setShort(1, tableIndex);
        incrementIndexStatement.execute();

      }
      connection.commit();
      return id;
    } catch (SQLException sqle) {
      LOC.traceThrowableT(Severity.WARNING, "Error getting metering index for [" + tableIndex
          + "], transaction rolled back", sqle);
      connection.rollback();
      throw sqle;
    } finally {
      connection.setAutoCommit(true);
    }
  }
  
  public String getInstalationNumber(){
    return instalationNumber;
  }
  
  public String getSystemID(){
    return systemID;
  }
  
  public void setInstalationNumber(String instalationNumber){
   this.instalationNumber = instalationNumber; 
  }
  
  public void setSystemID(String systemID){
    this.systemID = systemID;
  }
 }