package com.sap.archtech.archconn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.NamingException;

import com.sap.archtech.archconn.commands.PutCommand;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.ArchConfigProviderSingle;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.tc.logging.Severity;

/**
 * Two-phase write session
 * 
 * @author d025792
 *
 */
class TPWriteArchSession extends QualifiedArchSession
{
	private final static String SEL_BRW =
		"SELECT coluri FROM BC_XMLA_ASESSIONS WHERE archset = ? AND sessionstatus = 'BRW'";

	private int wcount;
	private final byte[] writeJobID;
  private final byte[] writeTaskID;

  TPWriteArchSession(String archuser, String archiveset, String pathextension, String sessionName, boolean autonaming, byte[] writeJobID, byte[] writeTaskID)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, TWO_PHASE_WRITE, archiveset, pathextension, sessionName, autonaming);
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
    	this.writeTaskID = writeTaskID;
    	System.arraycopy(writeTaskID, 0, this.writeTaskID, 0, writeTaskID.length);
    }
    else
    {
    	this.writeTaskID = null;
    }
  }

  TPWriteArchSession(String archuser, String archiveset, String pathextension, boolean autonaming, String destination)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, TWO_PHASE_WRITE, archiveset, pathextension, null, autonaming, destination);
    writeJobID = null;
    writeTaskID = null;
  } 

  /**
   * Creates a scheduled archiving write session
   */
  TPWriteArchSession(String archuser, String archiveset, URI sessionURI, byte[] writeTaskID)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, TWO_PHASE_WRITE, archiveset, sessionURI);
    if(writeTaskID != null)
    {
    	this.writeTaskID = writeTaskID;
    	System.arraycopy(writeTaskID, 0, this.writeTaskID, 0, writeTaskID.length);
    }
    else
    {
    	this.writeTaskID = null;
    }
    writeJobID = null;
  }

  // -----------------
	// Protected Methods ----------------------------------------------------------
	// -----------------

	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.QualifiedArchSession#count_res()
	 */
	public void count_res(ArchCommand acom) throws SessionHandlingException
	{
		if(acom instanceof PutCommand)
    {
      wcount++;
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
			  ArchSessionAccessor.updateResourceCounters(format(getCollection().toString()), wcount, -1);
			} 
      catch (SQLException sqlex)
			{
				cat.logThrowableT(
					Severity.ERROR,
					loc,
					"TPWriteArchSession.count_res()",
					sqlex);
				throw new SessionHandlingException(
					"Problem persisting session info: " + sqlex.getMessage());
			} 
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.QualifiedArchSession#open_dbaction()
	 */
	protected void open_dbaction() throws SessionHandlingException
	{
		PreparedStatement pst2 = null;
		Connection conn = null;
		Timestamp dateTime = new Timestamp(System.currentTimeMillis());
		ArchConfigProviderSingle acps =
			ArchConfigProviderSingle.getArchConfigProviderSingle();

		ResultSet rs2 = null;
		try
		{
			conn = this.getConnection();

			// Check if a broken write session for this archving set exists
			// if yes, a deletion run for this session is forced
			// (can be overwritten by archset property WSTART, see ArchConfigProviderSingle)
      String archiveset = getArchiveSet();
			if (!acps.getApplRestart(archiveset))
			{
				pst2 = conn.prepareStatement(SEL_BRW);
				pst2.setString(1, archiveset);
				rs2 = pst2.executeQuery();
				if (rs2.next())
				{
					cat.infoT(
						loc,
						"Broken write session for archiving set "
							+ archiveset
							+ " detected. New write session can not be opened.");
					throw new SessionHandlingException("A broken write session for this archive set exists. Deletion is required before starting a new write session");
				}
			}
			SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), 0, 0, "T", WRITE_SESSION_OPEN, getArchUser(), 
			    										getComment(), dateTime, null, null, null, false, archiveset, getSessionName());
			boolean hasWriteJob = writeJobID != null ? true : false;
			ArchSessionAccessor.insertSession(sessionInfo, hasWriteJob, writeJobID, writeTaskID);
		} catch (NamingException namex)
		{
			cat.logThrowableT(
				Severity.ERROR,
				loc,
				"TPWriteArchSession.open_dbaction()",
				namex);
			throw new SessionHandlingException(
				"Problem persisting session info: " + namex.getMessage());
		} catch (SQLException sqlex)
		{
			cat.logThrowableT(
				Severity.ERROR,
				loc,
				"TPWriteArchSession.open_dbaction()",
				sqlex);
			throw new SessionHandlingException(
				"Problem persisting session info: " + sqlex.getMessage());
		} finally
		{
			if(rs2 != null)
			{
				try
				{
					rs2.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.ERROR, loc, "TPWriteArchSession.open_dbaction()", e);
				}
			}
			if(pst2 != null)
			{
				try
				{
					pst2.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.ERROR, loc, "TPWriteArchSession.open_dbaction()", e);
				}
			}
			if(conn != null)
			{
				try
				{
					conn.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.ERROR, loc, "TPWriteArchSession.open_dbaction()", e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.QualifiedArchSession#close_dbaction()
	 */
	protected void close_dbaction() throws SessionHandlingException
	{
		Timestamp dateTime = new Timestamp(System.currentTimeMillis());
		SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), wcount, -1, null, SESSION_WRITTEN, 
													null, null, null, dateTime, null, null, false, "", getSessionName());
		try
		{
		  ArchSessionAccessor.stopSession(sessionInfo);
		} catch (SQLException sqlex)
		{
			cat.logThrowableT(
				Severity.ERROR,
				loc,
				"TPWriteArchSession.close_dbaction()",
				sqlex);
			throw new SessionHandlingException(
				"Problem persisting session info: " + sqlex.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.QualifiedArchSession#cancel_dbaction()
	 */
	protected void cancel_dbaction() throws SessionHandlingException
	{
		Timestamp dateTime = new Timestamp(System.currentTimeMillis());
		SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), wcount, -1, null, WRITE_SESSION_CANCELED, 
													null, null, null, dateTime, null, null, false, "", getSessionName());
		try
		{
		  ArchSessionAccessor.stopSession(sessionInfo);
		} catch (SQLException sqlex)
		{
			cat.logThrowableT(
				Severity.ERROR,
				loc,
				"TPWriteArchSession.cancel_dbaction()",
				sqlex);
			throw new SessionHandlingException(
				"Problem persisting session info: " + sqlex.getMessage());
		} 
	}
  
  protected void setScheduled(Timestamp scheduledStartTime) throws SessionHandlingException
  {
    try
    {
      // set to state "scheduled for write"
      SessionInfo sessionInfo = new SessionInfo(format(getCollection().toString()), 0, 0, "T", WRITE_SESSION_SCHEDULED, getArchUser(), 
        getComment(), scheduledStartTime, null, null, null, false, getArchiveSet(), getSessionName());
      ArchSessionAccessor.insertScheduledSession(sessionInfo, writeTaskID);
    }
    catch(SQLException sqlex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Setting an archiving session to state \"" + WRITE_SESSION_SCHEDULED + "\" failed", sqlex);
      throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
    }
  }
}
