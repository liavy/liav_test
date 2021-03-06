﻿/*
 This file is generated by Code Generator
 for CIMClass SAP_ITSAMArchSessionAccessor
 */

package com.sap.archtech.archconn.mbeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.ArchSessionAccessor;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.JdbcUtils;
import com.sap.archtech.archconn.util.SchedulerDelegate;
import com.sap.security.api.UMFactory;

/*
 * ManagedElement is an abstract class that provides a common superclass (or top
 * of the inheritance tree) for the non-association classes in the CIM Schema.
 * 
 * @version 1.0
 */

public class SAP_ITSAMArchSessionAccessor_Impl implements SAP_ITSAMArchSessionAccessor
{
  private final static String GET_SINGLESESS = "SELECT * FROM bc_xmla_asessions WHERE coluri = ?";
  private final static String GET_DELSESS = "SELECT * FROM bc_xmla_asessions WHERE archset = ? AND (sessionstatus = 'WRT' OR sessionstatus = 'BRW') ORDER BY sessionuser, deluser, wsessionstart DESC, dsessionstart DESC";
  private final static String GET_ALL_MONITORED = "SELECT * FROM bc_xmla_asessions WHERE ismonitored = 1";
  private final static String ORDER_CLAUSE = " ORDER BY sessionuser, deluser, wsessionstart DESC, dsessionstart DESC";
  private final static String GET_ALL_MONITORED_ORDERED = new StringBuilder(GET_ALL_MONITORED).append(ORDER_CLAUSE).toString();
  private final static String GET_ARCHSET_MONITORED_ORDERED = new StringBuilder(GET_ALL_MONITORED).append(" AND archset = ?").append(ORDER_CLAUSE).toString();
  private final static String GET_SESSIONS_BY_STATE = "SELECT * FROM bc_xmla_asessions WHERE archset = ? AND sessionstatus = ?";
  private final static String GET_SESSION_JOBIDS = "SELECT writejobid, deletejobid FROM bc_xmla_job2session WHERE sessionuri = ?";
  private final static String GET_SCHEDULER_TASK_IDS
  	= "SELECT jobs.writetaskid, jobs.deletetaskid, sessions.sessionstatus FROM bc_xmla_job2session jobs, bc_xmla_asessions sessions WHERE jobs.sessionuri = sessions.coluri AND jobs.sessionuri = ? AND (sessions.sessionstatus = 'SCW' OR sessions.sessionstatus = 'SCD')";

  private static final String JDBC_PATH = "jdbc/SAP/BC_XMLA";
  private final DataSource dataSource;
  private final SchedulerDelegate delegate;
  
  public SAP_ITSAMArchSessionAccessor_Impl()
  {
    InitialContext ctx = null;
    try
    {
      ctx = new InitialContext();
      dataSource = (DataSource) ctx.lookup(JDBC_PATH);
      delegate = new SchedulerDelegate();
    }
    catch(NamingException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(ctx != null)
      {
        try
        {
          ctx.close();
        }
        catch(Exception e)
        {
          // $JL-EXC$
        }
      }
    }
  }
  
  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession getSessionForDeletion(String collURI)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    PreparedStatement pst1 = null;
    Connection conn = null;
    try
    {
      conn = JdbcUtils.getConnection(dataSource);
      pst1 = conn.prepareStatement(GET_SINGLESESS);
      pst1.setString(1, collURI.toLowerCase());
      ResultSet rs1 = pst1.executeQuery();
      SAP_ITSAMArchSession archSession = null;
      if(rs1.next())
      {
        archSession = new SAP_ITSAMArchSession();
        archSession.seturi(rs1.getString("coluri"));
        archSession.setname(rs1.getString("sessionname"));
        archSession.setwrittenResources(rs1.getLong("wcount"));
        archSession.setdeletedResources(rs1.getLong("dcount"));
        archSession.setarchSetName(rs1.getString("archset"));
        archSession.settype(rs1.getString("sessiontype"));
        archSession.setstatus(rs1.getString("sessionstatus"));
        archSession.setwritePhaseUser(rs1.getString("sessionuser"));
        archSession.setdeletePhaseUser(rs1.getString("deluser"));
        archSession.setcomment(rs1.getString("comment"));
        archSession.setwritePhaseStart(JdbcUtils.timestamp2Date(rs1.getTimestamp("wsessionstart")));
        archSession.setwritePhaseStop(JdbcUtils.timestamp2Date(rs1.getTimestamp("wsessionstop")));
        archSession.setdeletePhaseStart(JdbcUtils.timestamp2Date(rs1.getTimestamp("dsessionstart")));
        archSession.setdeletePhaseStop(JdbcUtils.timestamp2Date(rs1.getTimestamp("dsessionstop")));
        archSession.setisCancelRequested(rs1.getShort("cancelrequested") == 0 ? false : true);
      }
      rs1.close();
      return archSession;
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, pst1);
    }
  }

  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getSessionsForDeletion(String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    ArrayList<SAP_ITSAMArchSession> sessions = new ArrayList<SAP_ITSAMArchSession>();
    PreparedStatement pst1 = null;
    Connection conn = null;
    try
    {
      conn = JdbcUtils.getConnection(dataSource);
      pst1 = conn.prepareStatement(GET_DELSESS);
      pst1.setString(1, archSetName);
      ResultSet rs1 = pst1.executeQuery();
      SAP_ITSAMArchSession archSession = null;
      while(rs1.next())
      {
        archSession = new SAP_ITSAMArchSession();
        archSession.seturi(rs1.getString("coluri"));
        archSession.setname(rs1.getString("sessionname"));
        archSession.setwrittenResources(rs1.getLong("wcount"));
        archSession.setdeletedResources(rs1.getLong("dcount"));
        archSession.setarchSetName(rs1.getString("archset")); 
        archSession.settype(rs1.getString("sessiontype"));
        archSession.setstatus(rs1.getString("sessionstatus")); 
        archSession.setwritePhaseUser(rs1.getString("sessionuser"));
        archSession.setdeletePhaseUser(rs1.getString("deluser")); 
        archSession.setcomment(rs1.getString("comment"));
        archSession.setwritePhaseStart(JdbcUtils.timestamp2Date(rs1.getTimestamp("wsessionstart"))); 
        archSession.setwritePhaseStop(JdbcUtils.timestamp2Date(rs1.getTimestamp("wsessionstop")));
        archSession.setdeletePhaseStart(JdbcUtils.timestamp2Date(rs1.getTimestamp("dsessionstart"))); 
        archSession.setdeletePhaseStop(JdbcUtils.timestamp2Date(rs1.getTimestamp("dsessionstop")));
        archSession.setisCancelRequested(rs1.getShort("cancelrequested") == 0 ? false : true);
        sessions.add(archSession);
      }
      rs1.close();
      return sessions.toArray(new SAP_ITSAMArchSession[sessions.size()]);
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, pst1);
    }
  }

  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getMonitoredSessions(String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    PreparedStatement pst2 = null;
    Connection conn = null;
    try
    {
      conn = JdbcUtils.getConnection(dataSource);
      if(archSetName == null)
      {
        pst2 = conn.prepareStatement(GET_ALL_MONITORED_ORDERED);
      }
      else
      {
        pst2 = conn.prepareStatement(GET_ARCHSET_MONITORED_ORDERED);
        pst2.setString(1, archSetName);
      }
      ResultSet rs2 = pst2.executeQuery();
      SAP_ITSAMArchSession archSession = null;
      ArrayList<SAP_ITSAMArchSession> sessions = new ArrayList<SAP_ITSAMArchSession>();
      while(rs2.next())
      {
        archSession = new SAP_ITSAMArchSession();
        archSession.seturi(rs2.getString("coluri"));
        archSession.setname(rs2.getString("sessionname"));
        archSession.setwrittenResources(rs2.getLong("wcount"));
        archSession.setdeletedResources(rs2.getLong("dcount"));
        archSession.setarchSetName(rs2.getString("archset")); 
        archSession.settype(rs2.getString("sessiontype"));
        archSession.setstatus(rs2.getString("sessionstatus")); 
        archSession.setwritePhaseUser(rs2.getString("sessionuser"));
        archSession.setdeletePhaseUser(rs2.getString("deluser")); 
        archSession.setcomment(rs2.getString("comment"));
        archSession.setwritePhaseStart(JdbcUtils.timestamp2Date(rs2.getTimestamp("wsessionstart"))); 
        archSession.setwritePhaseStop(JdbcUtils.timestamp2Date(rs2.getTimestamp("wsessionstop")));
        archSession.setdeletePhaseStart(JdbcUtils.timestamp2Date(rs2.getTimestamp("dsessionstart"))); 
        archSession.setdeletePhaseStop(JdbcUtils.timestamp2Date(rs2.getTimestamp("dsessionstop")));
        archSession.setisCancelRequested(rs2.getShort("cancelrequested") == 0 ? false : true);
        sessions.add(archSession);
      }
      rs2.close();
      return sessions.toArray(new SAP_ITSAMArchSession[sessions.size()]);
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, pst2);
    }
  }

  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getBrokenWriteSessions(String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    try
    {
      return getArchSessionsByState(archSetName, "BRW");
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSession[] getIncompleteSessions(String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    try
    {
      return getArchSessionsByState(archSetName, "WRT");
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public String getSessionLog(String colUri)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    PreparedStatement selectStmt = null;
    Connection conn = null;
    try
    {
      conn = JdbcUtils.getConnection(dataSource);
      selectStmt = conn.prepareStatement(GET_SESSION_JOBIDS);
      selectStmt.setString(1, colUri.toLowerCase());
      ResultSet rs = selectStmt.executeQuery();
      StringBuilder logs = new StringBuilder("");
      if(rs.next())
      {
        byte[] writeJobIDArr = rs.getBytes("writejobid");
        byte[] deleteJobIDArr = rs.getBytes("deletejobid");
        if(writeJobIDArr != null && writeJobIDArr.length > 0)
        {
          // there has been a write job for the given session
          String writeJobLog = delegate.getJobLogs(writeJobIDArr);
          logs.append("Write phase log:").append('\n').append(writeJobLog).append('\n');
        }
        if(deleteJobIDArr != null && deleteJobIDArr.length > 0)
        {
          // there has been a delete job for the given session
          String deleteJobLog = delegate.getJobLogs(deleteJobIDArr);
          logs.append("Delete phase log:").append('\n').append(deleteJobLog).append('\n');
        }
      }
      return logs.toString();
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, selectStmt);
    }
  }
  
  public void startJobImmediately(byte[] jobParams , String archSetName , boolean isWriteJob , byte[] uiLocale)
  {
    ConnectorAccessPermission.checkPermission("archive", archSetName);

    ObjectInputStream jobParamsIS = null;
    ObjectInputStream uiLocaleIS = null;
    try
    {
      // deserialize parameters
      jobParamsIS = new ObjectInputStream(new ByteArrayInputStream(jobParams));
      HashMap<String, Object> jobParamsMap = (HashMap<String, Object>)jobParamsIS.readObject();
      // extract information whether ILM properties have to be determined during the archiving session
      Boolean isRetentionRequested =  null;
      if(jobParamsMap.containsKey("isretentionrequested"))
      {
      	isRetentionRequested = (Boolean)jobParamsMap.get("isretentionrequested");
      	// Note: Must not be passed on with the remaining job definition parameters
      	jobParamsMap.remove("isretentionrequested");
      }
      else
      {
      	isRetentionRequested = Boolean.FALSE;
      }
      uiLocaleIS = new ObjectInputStream(new ByteArrayInputStream(uiLocale));
      Locale uiLocaleObj = (Locale)uiLocaleIS.readObject();
      // call Scheduler
      delegate.startJobImmediately(jobParamsMap, archSetName, isWriteJob, isRetentionRequested, uiLocaleObj);
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
    catch(ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(jobParamsIS != null)
      {
        try
        {
          jobParamsIS.close();
        }
        catch(IOException e)
        {
          // $JL-EXC$
        }
      }
      if(uiLocaleIS != null)
      {
        try
        {
          uiLocaleIS.close();
        }
        catch(IOException e)
        {
          // $JL-EXC$
        }
      }
    }
  }
  
  public void startCronJob(byte[] jobParams , String archSetName , byte[] scheduleTime , boolean isWriteJob , byte[] uiLocale)
  {
    ConnectorAccessPermission.checkPermission("archive", archSetName);
    
    ObjectInputStream jobParamsIS = null;
    ObjectInputStream scheduleTimeIS = null;
    ObjectInputStream uiLocaleIS = null;
    try
    {
      // deserialize parameters
      jobParamsIS = new ObjectInputStream(new ByteArrayInputStream(jobParams));
      HashMap<String, Object> jobParamsMap = (HashMap<String, Object>)jobParamsIS.readObject();
      // extract information whether ILM properties have to be determined during the archiving session
      Boolean isRetentionRequested =  null;
      if(jobParamsMap.containsKey("isretentionrequested"))
      {
      	isRetentionRequested = (Boolean)jobParamsMap.get("isretentionrequested");
      	// Note: Must not be passed on with the remaining job definition parameters
      	jobParamsMap.remove("isretentionrequested");
      }
      else
      {
      	isRetentionRequested = Boolean.FALSE;
      }
      scheduleTimeIS = new ObjectInputStream(new ByteArrayInputStream(scheduleTime));
      Calendar scheduleTimeObj = (Calendar)scheduleTimeIS.readObject();
      uiLocaleIS = new ObjectInputStream(new ByteArrayInputStream(uiLocale));
      Locale uiLocaleObj = (Locale)uiLocaleIS.readObject();
      // call Scheduler
      delegate.startCronJob(jobParamsMap, archSetName, scheduleTimeObj, isWriteJob, isRetentionRequested, uiLocaleObj);
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
    catch(ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(jobParamsIS != null)
      {
        try
        {
          jobParamsIS.close();
        }
        catch(IOException e)
        {
          // $JL-EXC$
        }
      }
      if(scheduleTimeIS != null)
      {
        try
        {
          scheduleTimeIS.close();
        }
        catch(IOException e)
        {
          // $JL-EXC$
        }
      }
      if(uiLocaleIS != null)
      {
        try
        {
          uiLocaleIS.close();
        }
        catch(IOException e)
        {
          // $JL-EXC$
        }
      }
    }
  }
  
  public void markForCancellation(String archSetName , String collURI)
  {
    ConnectorAccessPermission.checkPermission("archive", archSetName);
    
    //*** mark for cancellation and check if if "collURI" refers to a scheduled session
    PreparedStatement selectStmt = null;
    Connection conn = null;
    byte[] writeTaskID = null;
    byte[] deleteTaskID = null;
    String archUser = null;
    try
    {
    	// mark for cancellation
      ArchSessionAccessor.markForCancellation(collURI, true);
      // check session state
      conn = JdbcUtils.getConnection(dataSource);
      selectStmt = conn.prepareStatement(GET_SCHEDULER_TASK_IDS);
      selectStmt.setString(1, collURI.toLowerCase());
      ResultSet rs = selectStmt.executeQuery();
      if(rs.next())
      {
      	archUser = UMFactory.getAuthenticator().getLoggedInUser().getName();
      	String sessionState = rs.getString("sessionstatus");
        if(ArchSession.WRITE_SESSION_SCHEDULED.equals(sessionState))
        {
        	// the given session has been scheduled for write
        	writeTaskID = rs.getBytes("writetaskid");
        }
        else if(ArchSession.DELETE_SESSION_SCHEDULED.equals(sessionState))
        {
        	// the given session has been scheduled for delete
        	deleteTaskID = rs.getBytes("deletetaskid");
        }
        else
        {
        	throw new AssertionError("Unexpected value of \"sessionstatus\": " + sessionState);
        }
      }
    }
    catch(SessionHandlingException e)
    {
      throw new RuntimeException(e);
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, selectStmt);
    }
    //*** after releasing the DB connection trigger task and session cancellation "collURI" refers to a scheduled session
    try
    {
    	if(writeTaskID != null && writeTaskID.length > 0)
    	{
    		delegate.cancelWriteTask(archSetName, archUser, collURI, writeTaskID);
    	}
    	else if(deleteTaskID != null && deleteTaskID.length > 0)
    	{
    		delegate.cancelDeleteTask(archSetName, archUser, collURI, deleteTaskID);
    	}
    }
    catch(SessionHandlingException e)
    {
    	throw new RuntimeException(e);
    }
  }
  
  public boolean isJobParamAvailable(String archSetName, String jobParamName)
  {
  	try
  	{
  		ConnectorAccessPermission.checkPermission("archive", archSetName);
  		return delegate.isJobParamAvailable(archSetName, jobParamName, true, false);
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  private SAP_ITSAMArchSession[] getArchSessionsByState(String archSet, String sessionState) throws SQLException
  {
    PreparedStatement selectStmt = null;
    Connection conn = null;
    ArrayList<SAP_ITSAMArchSession> sessions = new ArrayList<SAP_ITSAMArchSession>();
    try
    {
      conn = JdbcUtils.getConnection(dataSource);
      selectStmt = conn.prepareStatement(GET_SESSIONS_BY_STATE);
      selectStmt.setString(1, archSet);
      selectStmt.setString(2, sessionState);
      ResultSet rs = selectStmt.executeQuery();
      SAP_ITSAMArchSession archSession = null;
      while(rs.next())
      {
        archSession = new SAP_ITSAMArchSession();
        archSession.seturi(rs.getString("coluri"));
        archSession.setname(rs.getString("sessionname"));
        archSession.setwrittenResources(rs.getLong("wcount"));
        archSession.setdeletedResources(rs.getLong("dcount"));
        archSession.setarchSetName(rs.getString("archset")); 
        archSession.settype(rs.getString("sessiontype"));
        archSession.setstatus(rs.getString("sessionstatus")); 
        archSession.setwritePhaseUser(rs.getString("sessionuser"));
        archSession.setdeletePhaseUser(rs.getString("deluser")); 
        archSession.setcomment(rs.getString("comment"));
        archSession.setwritePhaseStart(JdbcUtils.timestamp2Date(rs.getTimestamp("wsessionstart"))); 
        archSession.setwritePhaseStop(JdbcUtils.timestamp2Date(rs.getTimestamp("wsessionstop")));
        archSession.setdeletePhaseStart(JdbcUtils.timestamp2Date(rs.getTimestamp("dsessionstart"))); 
        archSession.setdeletePhaseStop(JdbcUtils.timestamp2Date(rs.getTimestamp("dsessionstop")));
        archSession.setisCancelRequested(rs.getShort("cancelrequested") == 0 ? false : true);
        sessions.add(archSession);
      }
      rs.close();
      return sessions.toArray(new SAP_ITSAMArchSession[sessions.size()]);
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, selectStmt);
    }
  }
}