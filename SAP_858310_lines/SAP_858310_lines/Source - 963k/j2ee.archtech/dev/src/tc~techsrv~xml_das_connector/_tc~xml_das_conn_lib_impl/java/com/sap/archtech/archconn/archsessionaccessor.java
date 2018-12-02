package com.sap.archtech.archconn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.commands.SessioninfoCommand;
import com.sap.archtech.archconn.commands.SyncHPCommand;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.JdbcUtils;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.GUIDVersionException;
import com.sap.guid.IGUID;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>ArchSessionAccessor</code> class provides access to the persistent data of Archiving Sessions.
 */
public class ArchSessionAccessor
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.ArchSessionAccessor");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private static final String LOAD_SESSION = "SELECT * FROM BC_XMLA_ASESSIONS WHERE coluri = ?";
  private static final String INSERT_SESSION 
  	= "INSERT INTO BC_XMLA_ASESSIONS(coluri, wsessionstart, dsessionstart, sessiontype, sessionstatus, sessionuser, comment, archset, ainstance, cancelrequested, parent, ismonitored, sessionname) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String UPDATE_WRITERES_DELETERES_COUNTER
  	= "UPDATE BC_XMLA_ASESSIONS SET wcount = ?, dcount = ? where coluri = ?";
  private static final String UPDATE_WRITERES_COUNTER
  	= "UPDATE BC_XMLA_ASESSIONS SET wcount = ? where coluri = ?";
  private static final String UPDATE_DELETERES_COUNTER
  	= "UPDATE BC_XMLA_ASESSIONS SET dcount = ? where coluri = ?";
  private static final String UPDATE_DELETE_SESSION_START
  	= "UPDATE BC_XMLA_ASESSIONS SET dsessionstart = ?, sessionstatus = ?, ainstance = ?, deluser = ?, ismonitored = 1 WHERE coluri = ?";
  private static final String UPDATE_DELETE_SESSION_STOP
  	= "UPDATE BC_XMLA_ASESSIONS SET dsessionstop = ?, dcount = ?, sessionstatus = ?, cancelrequested = ? WHERE coluri = ?";
  private static final String UPDATE_WRITE_SESSION_STOP
  	= "UPDATE BC_XMLA_ASESSIONS SET wsessionstop = ?, wcount = ?, sessionstatus = ?, cancelrequested = ? WHERE coluri = ?";
  private static final String UPDATE_WRITE_DELETE_SESSION_STOP
	= "UPDATE BC_XMLA_ASESSIONS SET wsessionstop = ?, dsessionstop = ?, wcount = ?, dcount = ?, sessionstatus = ?, cancelrequested = ? WHERE coluri = ?";
  private static final String UPDATE_CANCEL_INFO
  	= "UPDATE BC_XMLA_ASESSIONS SET cancelrequested = ? WHERE coluri = ?";
  private static final String INSERT_WRITEJOB
    = "INSERT INTO BC_XMLA_JOB2SESSION(sessionuri, writejobid) values (?, ?)";
  private static final String UPDATE_DELETEJOB
    = "UPDATE BC_XMLA_JOB2SESSION SET deletejobid = ? WHERE sessionuri = ?";
  private static final String INSERT_WRITETASK
    = "INSERT INTO BC_XMLA_JOB2SESSION(sessionuri, writejobid, writetaskid) values (?, ?, ?)";
  private static final String UPDATE_DELETETASK
  	= "UPDATE BC_XMLA_JOB2SESSION SET deletetaskid = ? WHERE sessionuri = ?";
  private static final String SELECT_URI_BY_WRITETASK
    = "SELECT sessionuri FROM BC_XMLA_JOB2SESSION WHERE writetaskid = ?";
  private static final String DELETE_SESSION_PART1
    = "DELETE FROM BC_XMLA_ASESSIONS WHERE coluri = ?";
  private static final String DELETE_SESSION_PART2
    = "DELETE FROM BC_XMLA_JOB2SESSION WHERE sessionuri = ?";
  
  // SQL statements for BC_XMLA_HIERARCHY access
  private static final String LOAD_COLID = "SELECT coluri, colid FROM BC_XMLA_HIERARCHY WHERE coluri = ?";
  private static final String INSERT_INTO_HIERARCHY = "INSERT INTO BC_XMLA_HIERARCHY (coluri, colid, parent, archset) VALUES (?, ?, ?, ?)";
  private static final String HAS_CHILDREN = "SELECT 1 FROM BC_XMLA_HIERARCHY WHERE parent = ?";
  private static final String DELETE_FROM_HIERARCHY = "DELETE FROM BC_XMLA_HIERARCHY WHERE coluri = ?";
  private static final String LOAD_ARCHSETNAME = "SELECT asetname from BC_XMLA_ASETS WHERE asetprop = 'AHOME' AND propvalue LIKE ?";
  
  private static final IGUIDGenerator guidgen = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
  
  private ArchSessionAccessor()
  {
  }
  
  /**
   * Load the data of the Archiving Session identified by the given Collection URI. For internal usage only.
   * @param caller The calling object. It is checked whether this caller is allowed to invoke this method (only
   * few internal classes are allowed to).
   * @param collectionURI URI of the Collection created by the Archiving Session to be loaded. The URI must not end with a "/".
   * @return A <code>SessionInfo</code> object holding the persistent data of the Archiving Session or <code>null</code>
   * if no Archiving Session exists for the given URI.
   * @throws SQLException Thrown if any database-related problem occurred while trying to load the session data.
   * @throws SessionHandlingException Thrown if the JDBC resource look-up failed
   * @throws IllegalArgumentException Thrown if the given <code>caller</code> is not allowed to invoke this method.
   */
  public static SessionInfo loadSessionInfo(Object caller, String collectionURI) throws SQLException, SessionHandlingException
  {
    if(!isFriend(caller))
    {
      throw new IllegalArgumentException("Caller is not allowed to invoke this method: " + caller);
    }
    
    return _loadSessionInfo(collectionURI.toLowerCase());
  }
  
  /**
   * Mark the Archiving Session identified by the given Collection URI as to be cancelled.
   * Use this method to request the cancellation of long-running Write or Delete Archiving Sessions.<br>
   * Prior to its execution, the Archiving Command involved (e.g. <code>PUT</code>) checks 
   * if a session cancellation request has been issued via this method. If so, the Command will stop executing
   * and return <code>DasResponse.SC_SESSION_CANCELLED</code> as response to the caller.  
   * @param collectionURI URI of the Collection created by the Archiving Session to be cancelled. The URI must not end with a "/".
   * @param toBeCancelled <code>true</code> if cancellation is to be requested
   * @throws SQLException Thrown if any database-related problem occurred while trying to store the cancellation request.
   * @throws SessionHandlingException Thrown if any other problem occurred
   */
  public static void markForCancellation(String collectionURI, boolean toBeCancelled) throws SQLException, SessionHandlingException
  {
    // check session existence first
    collectionURI = collectionURI.toLowerCase();
    SessionInfo sessionInfo = _loadSessionInfo(collectionURI);
    if(sessionInfo == null)
    {
      throw new SessionHandlingException("There is no Archiving Session for the given Collection URI \"" + collectionURI + "\"");
    }
    updateCancellationInfo(collectionURI, toBeCancelled);
  }
  
  /**
   * Checks if the Archiving Session identified by the given Collection URI is currently marked for cancellation.
   * @param collectionURI URI of the Collection created by the Archiving Session. The URI must not end with a "/".
   * @return <code>true</code> if the given Archiving Session is currently marked for cancellation
   * @throws SQLException Thrown if any database-related problem occurred while trying to load the cancellation information.
   * @throws SessionHandlingException Thrown if any other problem occurred
   */
  public static boolean isMarkedForCancellation(String collectionURI) throws SQLException, SessionHandlingException
  {
    collectionURI = collectionURI.toLowerCase();
    SessionInfo sessionInfo = _loadSessionInfo(collectionURI);
    if(sessionInfo == null)
    {
      throw new SessionHandlingException("There is no Archiving Session for the given Collection URI \"" + collectionURI + "\"");
    }
    return sessionInfo.isCancellationRequested();
  }
  
  /**
   * Insert Home Path Collection URI into archive hierarchy. Only for internal usage.
   * @param caller The caller of this method. Only privileged classes are granted access.
   * @param homePathCollURI The URI of the Home Path Collection
   * @throws SQLException Thrown if any database-related problem occurred while trying to insert the Home Path Collection URI
   * @throws SessionHandlingException Thrown if the insertion failed for another reason
   */
  public static void insertHomePathCollection(Object caller, String homePathCollURI) throws SQLException, SessionHandlingException
  {
    if(!(caller instanceof SyncHPCommand))
    {
      throw new IllegalArgumentException("Method \"insertHomePathCollection()\" must not be invoked by objects of class " + (caller != null ? caller.getClass().getName() : "\"null\""));
    }
    
    Connection conn = null;
    homePathCollURI = homePathCollURI.toLowerCase();
    try
    {
      conn = getConnection();
      // get archiving set name
      String archSetName = getArchSetName(conn, homePathCollURI);
      // insert all parent folders of the given URI
      byte[] currentParentFolderID = insertParentCollections(conn, homePathCollURI, archSetName);
      // insert the URI
      insertCollection(conn, homePathCollURI, currentParentFolderID, archSetName);
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the Home Path Collection into the archive hierarchy failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Inserting the Home Path Collection into the archive hierarchy failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, new PreparedStatement[0]);
    }
  }
  
  /**
   * Check whether the given Home Path Collection URI has already been added to the archive hierarchy. Only for internal usage.
   * @param caller The caller of this method. Only privileged classes are granted access.
   * @param homePathCollURI The URI of the Home Path Collection
   * @throws SQLException Thrown if any database-related problem occurred while trying to insert the Home Path Collection URI
   * @throws SessionHandlingException Thrown if the insertion failed for another reason
   */
  public static boolean isHomePathCollectionInHierarchy(Object caller, String homePathCollURI) throws SQLException, SessionHandlingException
  {
    if(!(caller instanceof SyncHPCommand))
    {
      throw new IllegalArgumentException("Method \"isHomePathCollectionInHierarchy()\" must not be invoked by objects of class " + (caller != null ? caller.getClass().getName() : "\"null\""));
    }
    
    Connection conn = null;
    homePathCollURI = homePathCollURI.toLowerCase();
    try
    {
      conn = getConnection();
      byte[] homePathCollID = getCollectionFolderID(conn, homePathCollURI);
      return homePathCollID.length > 0;
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Checking the existence of the Home Path Collection in the archive hierarchy failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Checking the existence of the Home Path Collection in the archive hierarchy failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, new PreparedStatement[0]);
    }
  }
  
  private static String getArchSetName(final Connection conn, final String homePathUri) throws SQLException
  {
    int lastSlashPos = homePathUri.lastIndexOf('/');
    String homePathUriLastPart = homePathUri.substring(lastSlashPos + 1, homePathUri.length());
    PreparedStatement selectArchSetNameStmt = null;
    ResultSet rsArchSetName = null;
    try
    {
      selectArchSetNameStmt = conn.prepareStatement(LOAD_ARCHSETNAME);
      String likeClause = new StringBuilder("%").append(homePathUriLastPart).append("%").toString();
      selectArchSetNameStmt.setString(1, likeClause);
      rsArchSetName = selectArchSetNameStmt.executeQuery();
      if(rsArchSetName.next())
      {
        return rsArchSetName.getString("asetname");
      }
      throw new RuntimeException("Missing Archiving Set name for Home Path URI " + homePathUri);
    }
    finally
    {
      if(rsArchSetName != null)
      {
      	try
      	{
      		rsArchSetName.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
      	}
      }
      if(selectArchSetNameStmt != null)
      {
      	try
      	{
      		selectArchSetNameStmt.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
      	}
      }
    }
  }
  
  /**
   * Remove Home Path Collection URI from archive hierarchy. Only for internal usage.
   * @param caller The caller of this method. Only privileged classes are granted access.
   * @param homePathCollURI The URI of the Home Path Collection
   * @throws SQLException Thrown if any database-related problem occurred while trying to remove the Home Path Collection URI
   * @throws SessionHandlingException Thrown if removal failed for another reason
   */
  public static void removeHomePathCollection(Object caller, String homePathCollURI) throws SQLException, SessionHandlingException
  {
    if(!(caller instanceof SyncHPCommand))
    {
      throw new IllegalArgumentException("Method \"removeHomePathCollection()\" must not be invoked by objects of class " + (caller != null ? caller.getClass().getName() : "\"null\""));
    }
    
    Connection conn = null;
    homePathCollURI = homePathCollURI.toLowerCase();
    try
    {
      conn = getConnection();
      // calculate parent folders of "homePathCollURI"
      ArrayList<String> collFolderURIs = calculateCollectionHierarchy(homePathCollURI);
      // add "homePathCollURI" to the end of the list
      collFolderURIs.add(collFolderURIs.size(), homePathCollURI);
      // loop starts with last list entry
      String currentFolderUri = null;
      byte[] currentFolderID = null;
      int maxListIdx = collFolderURIs.size()-1;
      for(int i = maxListIdx; i >= 0; i--)
      {
        currentFolderUri = collFolderURIs.get(i);
        currentFolderID = getCollectionFolderID(conn, currentFolderUri);
        if(hasHierarchyChildren(conn, currentFolderID))
        {
          // list element has children -> cannot be removed
          break;
        }
        // no children -> delete list element from hierarchy
        deleteFromHierarchy(conn, currentFolderUri);
      }
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Removing the Home Path Collection from the archive hierarchy failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Removing the Home Path Collection from the archive hierarchy failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, new PreparedStatement[0]);
    }
  }
  
  private static boolean hasHierarchyChildren(final Connection conn, byte[] parentCollectionID) throws SQLException
  {
    if(parentCollectionID.length == 0)
    {
      return false;
    }
    PreparedStatement hasChildrenStmt = null;
    ResultSet rsHasChildren = null;
    try
    {
      hasChildrenStmt = conn.prepareStatement(HAS_CHILDREN);
      hasChildrenStmt.setBytes(1, parentCollectionID);
      rsHasChildren = hasChildrenStmt.executeQuery();
      return rsHasChildren.next();
    }
    finally
    {
      if(rsHasChildren != null)
      {
      	try
      	{
      		rsHasChildren.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
      	}
      }
      if(hasChildrenStmt != null)
      {
        try
        {
        	hasChildrenStmt.close();
        }
        catch(SQLException e)
        {
        	cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
        }
      }
    }
  }
  
  private static void deleteFromHierarchy(final Connection conn, String collectionUriLowerCase) throws SQLException
  {
    PreparedStatement deleteStmt = null;
    try
    {
      deleteStmt = conn.prepareStatement(DELETE_FROM_HIERARCHY);
      deleteStmt.setString(1, collectionUriLowerCase);
      deleteStmt.executeUpdate();
    }
    finally
    {
      if(deleteStmt != null)
      {
        deleteStmt.close();
      }
    }
  }
  
  /**
   * Insert new Archiving Session into database.
   * @param sessionInfo Holds the session data to be stored in the database.
   * @param writeJob Meta data of the scheduler job to be assigned to the session. May be <code>null</code>. 
   * @throws SQLException Thrown if any database-related problem occurred while trying to insert the session data.
   * @throws SessionHandlingException Thrown if the insertion failed for another reason
   */
  static void insertSession(SessionInfo sessionInfo, boolean hasWriteJob, byte[] writeJobID, byte[] writeTaskID) throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    try
    {
      conn = getConnection();
      // check if there is already a scheduled session for this job
      String sessionURI = getUriOfScheduledSession(hasWriteJob, writeTaskID, conn);
      if(sessionURI != null)
      {
        // remove the scheduled session - must be replaced by the new session
        sessionURI = sessionURI.toLowerCase();
        deleteScheduledSession(sessionURI, conn);
      }
      insertSessionInfo(sessionInfo, conn);
      // assign job meta data
      insertWriteJob(sessionInfo, writeJobID, conn);
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Inserting the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, new PreparedStatement[0]);
    }
  }
  
  /**
   * Insert new Archiving Session into database. The session is scheduled only but not started yet.
   * @param sessionInfo Holds the session data to be stored in the database.
   * @param writeTaskBytes Meta data of the scheduler task to be assigned to the session. 
   * @throws SQLException Thrown if any database-related problem occurred while trying to insert the session data.
   * @throws SessionHandlingException Thrown if the insertion failed for another reason
   */
  static void insertScheduledSession(SessionInfo sessionInfo, byte[] writeTaskBytes) throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    try
    {
      conn = getConnection();
      insertSessionInfo(sessionInfo, conn);
      // assign scheduler task meta data
      insertWriteSchedulerTask(sessionInfo, writeTaskBytes, conn);
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the Archiving Session data failed", e);
      throw e;
    }
    catch(GUIDVersionException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the Archiving Session data failed", e);
      throw new SessionHandlingException("Inserting the Archiving Session data failed due to GUID creation problems: " + e.getMessage());
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Inserting the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, new PreparedStatement[0]);
    }
  }
  
  private static void insertSessionInfo(SessionInfo sessionInfo, final Connection conn) throws SQLException, SessionHandlingException
  {
    PreparedStatement insertStmt = null;
    try
    {
      // insert into hierarchy table
      byte[] parentID = insertParentCollections(conn, sessionInfo.getUri().toLowerCase(), sessionInfo.getArchSetName());
      // insert into sessions table
      insertStmt = conn.prepareStatement(INSERT_SESSION);
      //(coluri, wsessionstart, dsessionstart, sessiontype, sessionstatus, sessionuser, comment, ainstance)
      insertStmt.setString(1, sessionInfo.getUri().toLowerCase());
      insertStmt.setTimestamp(2, sessionInfo.getWritesession_start());
      insertStmt.setTimestamp(3, sessionInfo.getDeletesession_start());
      insertStmt.setString(4, sessionInfo.getSessiontype());
      insertStmt.setString(5, sessionInfo.getSessionstatus());
      insertStmt.setString(6, sessionInfo.getSessionuser());
      String comment = sessionInfo.getSessioncomment();
      if(comment == null)
      {
        insertStmt.setString(7, " ");
      }
      else
      {
        insertStmt.setString(7, comment);
      }
      String archSetName = sessionInfo.getArchSetName();
      if(archSetName == null)
      {
        insertStmt.setString(8, " ");
      }
      else
      {
        insertStmt.setString(8, archSetName);
      }
      insertStmt.setString(9, System.getProperty("SAPMYNAME"));
      // cancellation is never requested at session creation time 
      insertStmt.setShort(10, (short)0);
      insertStmt.setBytes(11, parentID);
      // new session can be monitored
      insertStmt.setShort(12, (short)1);
      insertStmt.setString(13, sessionInfo.getSessionName());
      int recCount = insertStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException(
            "Cannot update/insert archsession " + sessionInfo.getUri() + ". Check session name");
      }
    }
    finally
    {
      if(insertStmt != null)
      {
        insertStmt.close();
      }
    }
  }
  
  /**
   * Assign meta data of a write job to an archiving session.
   * @param sessionInfo Represents the archiving session
   * @param writeJob Meta data of the write job
   */
  private static void insertWriteJob(SessionInfo sessionInfo, byte[] writeJobID, final Connection conn) throws SQLException, SessionHandlingException
  {
    if(writeJobID == null)
    {
      // sessions may be started without specifying a write job
      return;
    }
    PreparedStatement insertStmt = null;
    try
    {
      // insert into JOB2SESSION table
      insertStmt = conn.prepareStatement(INSERT_WRITEJOB);
      //(sessionuri, writejobid)
      insertStmt.setString(1, sessionInfo.getUri().toLowerCase());
      insertStmt.setBytes(2, writeJobID);
      int recCount = insertStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException(
          "Cannot assign write job \"" + new String(writeJobID) + "\" to archiving session \"" + sessionInfo.getUri() + "\".");
      }
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the job meta data failed", e);
      throw e;
    }
    finally
    {
      if(insertStmt != null)
      {
        insertStmt.close();
      }
    }
  }
  
  /**
   * Assign meta data of a delete job to an archiving session.
   * @param sessionInfo Represents the archiving session
   * @param deleteJob Meta data of the delete job
   */
  private static void updateDeleteJob(SessionInfo sessionInfo, byte[] deleteJobID, byte[] deleteTaskID, final Connection conn) throws SQLException
  {
  	PreparedStatement updateStmt = null;
  	try
  	{
  		if(deleteJobID != null)
  		{
  			// update JOB2SESSION table with delete job ID
  			updateStmt = conn.prepareStatement(UPDATE_DELETEJOB);
  			updateStmt.setBytes(1, deleteJobID);
  			updateStmt.setString(2, sessionInfo.getUri().toLowerCase());
  			int recCount = updateStmt.executeUpdate();
  			if(recCount != 1)
  			{
  				loc.warningT("Could not assign delete job information to archiving session \"" + sessionInfo.getUri() + "\". The write phase of this archiving session seems to have been run without job context.");
  			}
  		}
  		if(deleteTaskID != null)
  		{
  			// update JOB2SESSION table with delete task ID
  			updateStmt = conn.prepareStatement(UPDATE_DELETETASK);
  			updateStmt.setBytes(1, deleteTaskID);
  			updateStmt.setString(2, sessionInfo.getUri().toLowerCase());
  			int recCount = updateStmt.executeUpdate();
  			if(recCount != 1)
  			{
  				loc.warningT("Could not assign delete task information to archiving session \"" + sessionInfo.getUri() + "\". The write phase of this archiving session seems to have been run without job context.");
  			}
  		}
  	}
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating the job meta data failed", e);
      throw e;
    }
    finally
    {
      if(updateStmt != null)
      {
        updateStmt.close();
      }
    }
  }
  
  /**
   * Assign meta data of a write scheduler task to an archiving session.
   * @param sessionInfo Represents the archiving session
   * @param writeTask Meta data of the write scheduler task
   */
  private static void insertWriteSchedulerTask(SessionInfo sessionInfo, byte[] writeTaskID, final Connection conn) 
  throws SQLException, SessionHandlingException, GUIDVersionException
  {
    PreparedStatement insertStmt = null;
    try
    {
      // insert into JOB2SESSION table
      insertStmt = conn.prepareStatement(INSERT_WRITETASK);
      //(sessionuri, writejobid, writetaskid)
      insertStmt.setString(1, sessionInfo.getUri().toLowerCase());
      // NULL-GUID for write job ID
      insertStmt.setBytes(2, guidgen.createGUID(IGUID.VERSION_NIL).toBytes());
      insertStmt.setBytes(3, writeTaskID);
      int recCount = insertStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException(
          "Cannot assign write task \"" + new String(writeTaskID) + "\" to archiving session \"" + sessionInfo.getUri() + "\".");
      }
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the scheduler task meta data failed", e);
      throw e;
    }
    catch(GUIDVersionException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Inserting the scheduler task meta data failed", e);
      throw e;
    }
    finally
    {
      if(insertStmt != null)
      {
        insertStmt.close();
      }
    }
  }

  /**
   * Update resource counters of given Archiving Session.
   * @param collectionURI URI of the Collection created by the Archiving Session to be cancelled. The URI must not end with a "/".
   * @param nrOfWrittenRes Number of written resources. Pass "-1" if you do not want to update this value.
   * @param nrOfDeletedRes Number of deleted resources. Pass "-1" if you do not want to update this value.
   * @throws SQLException Thrown if any database-related problem occurred while trying to update the session data.
   * @throws SessionHandlingException Thrown if the update failed for another reason
   * @throws IllegalArgumentException Thrown if both counter values are "-1"
   */
  static void updateResourceCounters(String collectionURI, long nrOfWrittenRes, long nrOfDeletedRes) 
  throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    PreparedStatement updateStmt = null;
    collectionURI = collectionURI.toLowerCase();
    try
    {
      conn = getConnection();
      if(nrOfWrittenRes >= 0 && nrOfDeletedRes < 0)
      {
        updateStmt = conn.prepareStatement(UPDATE_WRITERES_COUNTER);
        updateStmt.setLong(1, nrOfWrittenRes);
        updateStmt.setString(2, collectionURI);
      }
      else if(nrOfWrittenRes < 0 && nrOfDeletedRes >= 0)
      {
        updateStmt = conn.prepareStatement(UPDATE_DELETERES_COUNTER);
        updateStmt.setLong(1, nrOfDeletedRes);
        updateStmt.setString(2, collectionURI);
      }
      else if(nrOfWrittenRes >= 0 && nrOfDeletedRes >=0)
      {
        updateStmt = conn.prepareStatement(UPDATE_WRITERES_DELETERES_COUNTER);
        updateStmt.setLong(1, nrOfWrittenRes);
        updateStmt.setLong(2, nrOfDeletedRes);
        updateStmt.setString(3, collectionURI);
      }
      else
      {
        throw new IllegalArgumentException("Missing parameter to be updated.");
      }
      
      int recCount = updateStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException("Cannot update counter for deleted/written documents for session " + collectionURI);
      }
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Updating the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, updateStmt);
    }
  }

  /**
   * Update stop timestamps of given Archiving Session.
   * @param sessionInfo Holds the session data to be stored in the database.
   * @throws SQLException Thrown if any database-related problem occurred while trying to update the session data.
   * @throws SessionHandlingException Thrown if the update failed for another reason
   * @throws IllegalArgumentException Thrown if both stop timestamp values are <code>null</code>
   */
  static void stopSession(SessionInfo sessionInfo) throws SQLException, SessionHandlingException
  {
    Timestamp writeSessionStop = sessionInfo.getWritesession_stop();
    Timestamp deleteSessionStop = sessionInfo.getDeletesession_stop();
    Connection conn = null;
    PreparedStatement updateStmt = null;
    try
    {
      conn = getConnection();
      if(writeSessionStop != null && deleteSessionStop == null)
      {
        updateStmt = conn.prepareStatement(UPDATE_WRITE_SESSION_STOP);
        updateStmt.setTimestamp(1, writeSessionStop);
        updateStmt.setLong(2, sessionInfo.getWritten_res());
        updateStmt.setString(3, sessionInfo.getSessionstatus());
        // clear cancellation request
        updateStmt.setShort(4, (short)0);
        updateStmt.setString(5, sessionInfo.getUri().toLowerCase());
      }
      else if(writeSessionStop == null && deleteSessionStop != null)
      {
        updateStmt = conn.prepareStatement(UPDATE_DELETE_SESSION_STOP);
        updateStmt.setTimestamp(1, deleteSessionStop);
        updateStmt.setLong(2, sessionInfo.getDeleted_res());
        updateStmt.setString(3, sessionInfo.getSessionstatus());
        // clear cancellation request
        updateStmt.setShort(4, (short)0);
        updateStmt.setString(5, sessionInfo.getUri().toLowerCase());
      }
      else if(writeSessionStop != null && deleteSessionStop != null)
      {
        updateStmt = conn.prepareStatement(UPDATE_WRITE_DELETE_SESSION_STOP);
        updateStmt.setTimestamp(1, writeSessionStop);
        updateStmt.setTimestamp(2, deleteSessionStop);
        updateStmt.setLong(3, sessionInfo.getWritten_res());
        updateStmt.setLong(4, sessionInfo.getDeleted_res());
        updateStmt.setString(5, sessionInfo.getSessionstatus());
        // clear cancellation request
        updateStmt.setShort(6, (short)0);
        updateStmt.setString(7, sessionInfo.getUri().toLowerCase());
      }
      else
      {
        throw new IllegalArgumentException("Missing stop timestamp.");
      }
      int recCount = updateStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException("Cannot update stop timestamps for session " + sessionInfo.getUri());
      }
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Updating the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, updateStmt);
    }
  }

  /**
   * Update start timestamp of Delete Archiving Session.
   * @param sessionInfo Holds the session data to be stored in the database.
   * @param deleteJob Meta data of the scheduler job to be assigned to the session. May be <code>null</code>.
   * @throws SQLException Thrown if any database-related problem occurred while trying to update the session data.
   * @throws SessionHandlingException Thrown if the update failed for another reason
   */
  static void startDeleteSession(SessionInfo sessionInfo, byte[] deleteJobID, byte[] deleteTaskID) throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    PreparedStatement updateStmt = null;
    try
    {
      conn = getConnection();
      updateStmt = conn.prepareStatement(UPDATE_DELETE_SESSION_START);
      updateStmt.setTimestamp(1, sessionInfo.getDeletesession_start());
      updateStmt.setString(2, sessionInfo.getSessionstatus());
      updateStmt.setString(3, System.getProperty("SAPMYNAME"));
      updateStmt.setString(4, sessionInfo.getSessionuser());
      updateStmt.setString(5, sessionInfo.getUri().toLowerCase());
      int recCount = updateStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException("Cannot update start timestamp for session " + sessionInfo.getUri());
      }
      // update job meta data
      updateDeleteJob(sessionInfo, deleteJobID, deleteTaskID, conn);
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Updating the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, updateStmt);
    }
  }
  
  private static SessionInfo _loadSessionInfo(String collectionUriLowerCase) throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    PreparedStatement selectStmt = null;
    ResultSet rs = null;
    try
    {
      conn = getConnection();
      selectStmt = conn.prepareStatement(LOAD_SESSION);
      selectStmt.setString(1, collectionUriLowerCase);
      rs = selectStmt.executeQuery();
      if(!rs.next())
      {
        return null;
      }
      return new SessionInfo(
          rs.getString("coluri").concat("/"),
          rs.getLong("wcount"),
          rs.getLong("dcount"),
          rs.getString("sessiontype"),
          rs.getString("sessionstatus"),
          rs.getString("sessionuser"),
          rs.getString("comment"),
          rs.getTimestamp("wsessionstart"),
          rs.getTimestamp("wsessionstop"),
          rs.getTimestamp("dsessionstart"),
          rs.getTimestamp("dsessionstop"),
          rs.getShort("cancelrequested") == 0 ? false : true,
          rs.getString("archset"),
          rs.getString("sessionname"));
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Loading the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Loading the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, selectStmt);
    }
  }
  
  private static String getUriOfScheduledSession(boolean hasWriteJob, byte[] writeTaskID, final Connection conn) throws SQLException
  {
    
    if(!hasWriteJob)
    {
      // sessions may be started without specifying a write job
      return null;
    }
        
    // get scheduler task that created the given job
    if(writeTaskID == null)
    {
      // there MUST be a scheduler task ID for every write job
      cat.errorT(loc, "Missing the Scheduler Task ID");
      throw new AssertionError("Missing the Scheduler Task ID");
    }
    
    PreparedStatement selectStmt = null;
    ResultSet rs = null;
    try
    {
      selectStmt = conn.prepareStatement(SELECT_URI_BY_WRITETASK);
      selectStmt.setBytes(1, writeTaskID);
      rs = selectStmt.executeQuery();
      if(!rs.next())
      {
        // maybe the write job had not been scheduled (no cron job)
        return null;
      }
      return rs.getString("sessionuri");
    }
    finally
    {
      JdbcUtils.closeJdbcResources(null, selectStmt);
    }
  }
  
  private static void deleteScheduledSession(String sessionUriLowerCase, final Connection conn) throws SQLException
  {
    PreparedStatement deleteStmt1 = null;
    PreparedStatement deleteStmt2 = null;
    try
    {
      // delete from ASESSIONS table
      deleteStmt1 = conn.prepareStatement(DELETE_SESSION_PART1);
      deleteStmt1.setString(1, sessionUriLowerCase);
      deleteStmt1.executeUpdate();
      // delete from JOB2SESSION table
      deleteStmt2 = conn.prepareStatement(DELETE_SESSION_PART2);
      deleteStmt2.setString(1, sessionUriLowerCase);
      deleteStmt2.executeUpdate();
      // delete from HIERARCHY table
      deleteFromHierarchy(conn, sessionUriLowerCase);
    }
    finally
    {
      if(deleteStmt1 != null)
      {
      	try
      	{
      		deleteStmt1.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
      	}
      }
      if(deleteStmt2 != null)
      {
      	try
      	{
      		deleteStmt2.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
      	}
      }
    }
  }
  
  private static void updateCancellationInfo(String collectionUriLowerCase, boolean toBeCancelled) throws SQLException, SessionHandlingException
  {
    Connection conn = null;
    PreparedStatement updateStmt = null;
    try
    {
      conn = getConnection();
      updateStmt = conn.prepareStatement(UPDATE_CANCEL_INFO);
      updateStmt.setShort(1, toBeCancelled == false ? ((short)0) : ((short)1));
      updateStmt.setString(2, collectionUriLowerCase);
      int recCount = updateStmt.executeUpdate();
      if(recCount != 1)
      {
        throw new SessionHandlingException("Cannot update cancellation information for session " + collectionUriLowerCase);
      }
      conn.commit();
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating the Archiving Session data failed", e);
      throw e;
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Look-up for database connection pool failed", e);
      throw new SessionHandlingException("Updating the Archiving Session data failed due to JNDI look-up problems: " + e.getMessage());
    }
    finally
    {
      JdbcUtils.closeJdbcResources(conn, updateStmt);
    }
  }
  
  static void scheduleDeleteSession(SessionInfo sessionInfo, byte[] deleteTaskBytes) throws SQLException, SessionHandlingException
  {
    startDeleteSession(sessionInfo, null, deleteTaskBytes);
  }
  
  private static boolean isFriend(Object caller)
  {
    if(caller instanceof SessioninfoCommand)
    {
      return true;
    }
    return false;
  }
  
  private static byte[] insertParentCollections(final Connection conn, final String collUriLowerCase, final String archSetName) throws SQLException
  {
    // calculate parent folders of "collUri"
    ArrayList<String> collFolderURIs = calculateCollectionHierarchy(collUriLowerCase);
    // add root
    collFolderURIs.add(0, "/");
    String folderUri4Insertion = null;
    String currentParentFolderUri = null;
    byte[] currentParentFolderID = null;
    // loop starts with last list entry = direct parent folder of "collUri"
    // look for next-most parent folder that already exists in hierarchy
    int maxParentIdx = collFolderURIs.size()-1;
    for(int i = maxParentIdx; i >= 0; i--)
    {
      currentParentFolderUri = collFolderURIs.get(i);
      currentParentFolderID = getCollectionFolderID(conn, currentParentFolderUri);
      if(i == 0 && currentParentFolderID.length == 0)
      {
        // root collection has not been inserted yet
        currentParentFolderID = insertRootCollection(conn);
        folderUri4Insertion = collFolderURIs.get(i+1);
      }
      if(currentParentFolderID.length != 0)
      {
        if(folderUri4Insertion != null)
        {
          // insert folderURI4Insertion ...
          currentParentFolderID = insertCollection(conn, folderUri4Insertion, currentParentFolderID, archSetName);
          // ... and all sub folders
          for(int k = i+1; k < maxParentIdx; k++)
          {
            currentParentFolderUri = collFolderURIs.get(k);
            folderUri4Insertion = collFolderURIs.get(k+1);
            currentParentFolderID = insertCollection(conn, folderUri4Insertion, currentParentFolderID, archSetName);
          }
        }
        break;
      }
      folderUri4Insertion = currentParentFolderUri;
    }
    return currentParentFolderID;
  }
  
  private static ArrayList<String> calculateCollectionHierarchy(String collUri)
  {
     ArrayList<String> collist = new ArrayList<String>();
     StringTokenizer tok = new StringTokenizer(collUri, "/");
     StringBuilder sb = new StringBuilder();
     while(tok.hasMoreTokens())
     {
        if (sb.length() != 0)
        {
          collist.add(sb.toString());
        }
        sb.append("/" + tok.nextToken());
     }
     // last list entry = direct parent folder of "collUri"
     return collist;
  }
  
  private static byte[] getCollectionFolderID(final Connection conn, final String collUriLowerCase) throws SQLException
  {
    PreparedStatement selectCollIDStmt = null;
    ResultSet rsCollID = null;
    try
    {
      selectCollIDStmt = conn.prepareStatement(LOAD_COLID);
      selectCollIDStmt.setString(1, collUriLowerCase);
      rsCollID = selectCollIDStmt.executeQuery();
      if(rsCollID.next())
      {
        return rsCollID.getBytes("colid");
      }
      return new byte[0];
      
    }
    finally
    {
      if(rsCollID != null)
      {
      	try
      	{
      		rsCollID.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
      	}
      }
      if(selectCollIDStmt != null)
      {
      	try
      	{
      		selectCollIDStmt.close();
      	}
      	catch(SQLException e)
      	{
      		cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
      	}
      }
    }
  }
  
  private static byte[] insertCollection(final Connection conn, final String collUriLowerCase, final byte[] parentCollFolderID, final String archSetName) throws SQLException
  {
    PreparedStatement insertHierarchyStmt = null;
    try
    {
      insertHierarchyStmt = conn.prepareStatement(INSERT_INTO_HIERARCHY);
      byte[] newCollFolderID = guidgen.createGUID().toBytes();
      insertHierarchyStmt.setString(1, collUriLowerCase);
      insertHierarchyStmt.setBytes(2, newCollFolderID);
      insertHierarchyStmt.setBytes(3, parentCollFolderID);
      if(archSetName != null)
      {
        insertHierarchyStmt.setString(4, archSetName);
      }
      else
      {
        insertHierarchyStmt.setNull(4, java.sql.Types.NULL);
      }
      insertHierarchyStmt.executeUpdate();
      return newCollFolderID;
    }
    finally
    {
      if(insertHierarchyStmt != null)
      {
        insertHierarchyStmt.close();
      }
    }
  }
  
  private static byte[] insertRootCollection(final Connection conn) throws SQLException
  {
    PreparedStatement insertHierarchyStmt = null;
    try
    {
      insertHierarchyStmt = conn.prepareStatement(INSERT_INTO_HIERARCHY);
      byte[] newCollFolderID = guidgen.createGUID().toBytes();
      insertHierarchyStmt.setString(1, "/");
      insertHierarchyStmt.setBytes(2, newCollFolderID);
      insertHierarchyStmt.setNull(3, java.sql.Types.NULL);
      insertHierarchyStmt.setNull(4, java.sql.Types.NULL);
      insertHierarchyStmt.executeUpdate();
      return newCollFolderID;
    }
    finally
    {
      if(insertHierarchyStmt != null)
      {
        insertHierarchyStmt.close();
      }
    }
  }
  
  private static Connection getConnection() throws NamingException, SQLException
  {
    Context ctx = new InitialContext();
    DataSource ds = (DataSource)ctx.lookup("jdbc/SAP/BC_XMLA");
    return JdbcUtils.getConnection(ds);
  }
}
