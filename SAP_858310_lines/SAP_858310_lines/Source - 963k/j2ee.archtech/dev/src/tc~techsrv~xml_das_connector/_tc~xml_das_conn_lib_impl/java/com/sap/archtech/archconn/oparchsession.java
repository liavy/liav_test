package com.sap.archtech.archconn;

import java.sql.SQLException;
import java.sql.Timestamp;

import com.sap.archtech.archconn.commands.PickCommand;
import com.sap.archtech.archconn.commands.PutCommand;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.tc.logging.Severity;

/**
 * "One-phase" archiving session. Write and delete session 
 * occurs in one phase.
 * 
 * @author d025792
 *
 */
class OPArchSession extends QualifiedArchSession
{
  private int dcount;
  private int wcount;
  private final byte[] writeJobID;
  private final byte[] writeTaskID;
  
  OPArchSession(String archuser, String archiveset, String pathextension, String sessionName, boolean autonaming, byte[] writeJobID, byte[] writeTaskID)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, ONE_PHASE, archiveset, pathextension, sessionName, autonaming);
    if(writeJobID != null)
    {
    	this.writeJobID = writeJobID;
    	System.arraycopy(writeJobID, 0, this.writeJobID, 0, writeJobID.length);
    }
    else
    {
    	this.writeJobID = null;
    }
    if(writeTaskID != null)
    {
    	this.writeTaskID = writeJobID;
    	System.arraycopy(writeTaskID, 0, this.writeTaskID, 0, writeTaskID.length);
    }
    else
    {
    	this.writeTaskID = null;
    }
  }

  OPArchSession(String archuser, String archiveset, String pathextension, boolean autonaming, String destination)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, ONE_PHASE, archiveset, pathextension, null, autonaming, destination);
    writeJobID = null;
    writeTaskID = null;
  }
  
  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.QualifiedArchSession#count_res()
   */
  public void count_res(ArchCommand acom) throws SessionHandlingException
  {
    if (acom instanceof PutCommand)
    {
      wcount++;
    }
    if (acom instanceof PickCommand)
    {
      dcount++;
    }

    int updfrq = getUpdateFrequency();
    if(updfrq == 0)
    {
      return;
    }

    if(wcount % updfrq == 0)
    {
      try
      {
        ArchSessionAccessor.updateResourceCounters(format(getCollection().toString()), wcount, dcount);
      }
      catch (SQLException sqlex)
      {
        cat.logThrowableT(Severity.ERROR, loc, "OPArchSession.count_res()", sqlex);
        throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
      }
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.QualifiedArchSession#open_dbaction()
   */
  protected void open_dbaction() throws SessionHandlingException
  {
    Timestamp dateTime = new Timestamp(System.currentTimeMillis());
    SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), 0, 0, "O", SESSION_OPEN, 
        										getArchUser(), getComment(), dateTime, null, dateTime, null, 
        										false, getArchiveSet(), getSessionName());
    try
    {
    	boolean hasWriteJob = writeJobID != null ? true : false;
      ArchSessionAccessor.insertSession(sessionInfo, hasWriteJob, writeJobID, writeTaskID);
    }
    catch(SQLException sqlex)
    {
      throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.QualifiedArchSession#close_dbaction()
   */
  protected void close_dbaction() throws SessionHandlingException
  {
    Timestamp dateTime = new Timestamp(System.currentTimeMillis());
    // no PICK - no dcount
    SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), wcount, wcount, null, SESSION_CLOSED, 
        										null, null, null, dateTime, null, dateTime, false, "", getSessionName());
    try
    {
      ArchSessionAccessor.stopSession(sessionInfo);
    }
    catch (SQLException sqlex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "OPArchSession.close_dbaction()", sqlex);
      throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.QualifiedArchSession#cancel_dbaction()
   */
  protected void cancel_dbaction() throws SessionHandlingException
  {
    Timestamp dateTime = new Timestamp(System.currentTimeMillis());
    // no PICK - no dcount
    SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), wcount, wcount, null, SESSION_CANCELED, 
        										null, null, null, dateTime, null, dateTime, false, "", getSessionName());
    try
    {
      ArchSessionAccessor.stopSession(sessionInfo);
    }
    catch (SQLException sqlex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "OPArchSession.cancel_dbaction()", sqlex);
      throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
    }
  }
}
