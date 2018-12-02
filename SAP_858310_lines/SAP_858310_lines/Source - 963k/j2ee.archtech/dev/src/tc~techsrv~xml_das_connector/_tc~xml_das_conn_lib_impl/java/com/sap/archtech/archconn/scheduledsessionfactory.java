package com.sap.archtech.archconn;

import java.sql.Timestamp;

import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.ArchConfigProviderSingle;
import com.sap.archtech.archconn.util.URI;

public class ScheduledSessionFactory 
{

	private static ScheduledSessionFactory theFactory = new ScheduledSessionFactory();

	private ScheduledSessionFactory() 
	{
	}

	public static ScheduledSessionFactory getInstance() 
	{
		return theFactory;
	}
	/**
	 * Creates a qualified archiving session for TWO_PHASE_WRITE which is set to status "scheduled for write".
	 * For internal usage only!
	 */
	public void createScheduledWriteSession(String archUser, String archiveSet, URI pathExtension, byte[] writeTaskID, Timestamp scheduledStartTime)
	throws ArchConfigException, SessionHandlingException 
	{
		// only applicable for TWO_PHASE_WRITE
		// Note: At the moment this method is called the write job has not been created yet, only a write task exists 
		URI archSetHome = new URI(ArchConfigProviderSingle.getArchConfigProviderSingle().getArchSetHome(archiveSet));
		URI sessionUri = archSetHome.resolve(pathExtension);
   	TPWriteArchSession tpWriteSession = new TPWriteArchSession(archUser, archiveSet, sessionUri, writeTaskID);
		tpWriteSession.setScheduled(scheduledStartTime);
	}

	/**
	 * Creates a qualified archiving session for TWO_PHASE_DELETE which is set to status "scheduled for delete".
	 * For internal usage only!
	 */
	public void createScheduledDeleteSession(String archUser, boolean isSimulated, String archiveSet, URI pathExtension, Timestamp scheduledStartTime, byte[] deleteTaskID)
	throws ArchConfigException, SessionHandlingException 
	{
		// only applicable for TWO_PHASE_DELETE
		// Note: At the moment this method is called the delete job has not been created yet, only a delete task exists 
		TPDeleteArchSession tpDeleteSession = null;
		URI archSetHome = new URI(ArchConfigProviderSingle.getArchConfigProviderSingle().getArchSetHome(archiveSet));
		URI sessionUri = archSetHome.resolve(pathExtension);
		if(!isSimulated) 
		{
			tpDeleteSession = new TPDeleteArchSession(archUser, archiveSet,	sessionUri, deleteTaskID);
		}
		else
		{
			tpDeleteSession = new TPDeleteSimArchSession(archUser, archiveSet,	sessionUri, deleteTaskID);
		}
		tpDeleteSession.setScheduled(scheduledStartTime);
	}
	
	public void cancelScheduledWriteSession(String archUser, String archiveSet, URI sessionUri, byte[] writeTaskID)
	throws ArchConfigException, SessionHandlingException 
	{
		// only applicable for TWO_PHASE_WRITE
		// Note: At the moment this method is called the write job has not been created yet, only a write task exists
		TPWriteArchSession tpWriteSession = new TPWriteArchSession(archUser, archiveSet, sessionUri,	writeTaskID);
		tpWriteSession.cancel_dbaction();
	}

	public void cancelScheduledDeleteSession(String archUser, boolean isSimulated, String archiveSet, URI sessionUri, byte[] deleteTaskID)
	throws ArchConfigException, SessionHandlingException 
	{
		// only applicable for TWO_PHASE_DELETE
		// Note: At the moment this method is called the delete job has not been created yet, only a delete task exists
		TPDeleteArchSession tpDeleteSession = null;
		if(!isSimulated) 
		{
			tpDeleteSession = new TPDeleteArchSession(archUser, archiveSet,	sessionUri, deleteTaskID);
		}
		else
		{
			tpDeleteSession = new TPDeleteSimArchSession(archUser, archiveSet, sessionUri, deleteTaskID);
		}
		tpDeleteSession.cancel_dbaction();
	}
}
