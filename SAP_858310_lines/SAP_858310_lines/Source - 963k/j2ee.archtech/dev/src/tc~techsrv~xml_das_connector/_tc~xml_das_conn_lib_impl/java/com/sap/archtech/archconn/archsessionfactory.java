package com.sap.archtech.archconn;

import java.net.MalformedURLException;

import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.ArchiveDataViewer;
import com.sap.archtech.archconn.util.RemoteXmldasConfigurator;
import com.sap.archtech.archconn.util.URI;
import com.sap.scheduler.runtime.Job;

/**
 * Creates qualified and unqualified ArchSessions.
 * Singleton.
 */
public class ArchSessionFactory 
{
	private static ArchSessionFactory asf = new ArchSessionFactory();

	private ArchSessionFactory() 
	{
		super();
	}

	/** 
	 * @return The (single) instance of the ArchSessionFactory
	 */
	public static ArchSessionFactory getSessionFactory() 
	{
		return asf;
	}

	/**
	 * Creates an unqualified archiving session. Status of the session is not recorded.
	 * ArchConfigProvider must not be provided.
	 * 
	 * @param archuser a user ID taken from e.g. the UME. No authorization
	 * checks take place within the ArchivingConnector. The user ID is used in  
	 * session monitoring.
	 * @param archset name of the archiving set on which this unqualified session
	 * is working. Used for access control (not yet implemented)    
	 * @throws MalformedURLException if the URL resulting from destination and protocol
	 * is not valid
	 */
	public ArchSession getSession(String archuser, String archset) throws ArchConfigException 
	{
		try 
		{
			return new UnqualifiedArchSession(archuser, archset);
		} 
		catch(SessionHandlingException e) 
		{
			// SessionHandlingException is only thrown in "AbstractArchSession" with "checkArchiveSet = true"
			throw new AssertionError("Caught unexpected SessionHandlingException: "	+ e.getMessage());
		}
	}

	/**
	 * @deprecated
	 * For internal usage only!
	 */
	public ArchSession getSession(Object caller, String archuser, int mode, String archiveset, URI pathextension, boolean autonaming, String destinationName) 
	throws ArchConfigException,	SessionHandlingException 
	{
		if(!isFriend(caller)) 
		{
			throw new IllegalArgumentException("Caller is not allowed to invoke this method: " + caller);
		}
		return InternalArchSessionFactory.getInstance().getQualifiedArchSession4Destination(archuser, mode, archiveset, pathextension, autonaming, destinationName);
	}

	private boolean isFriend(Object caller) 
	{
		if(caller instanceof RemoteXmldasConfigurator	|| caller instanceof ArchiveDataViewer) 
		{
			return true;
		}
		return false;
	}

	/**
	 * Creates a qualified archiving session. Status of the session is recorded.
	 * ArchConfigProvider must not be provided.
	 * 
	 * @param archuser a user ID can be taken from e.g. the UME. There are no authorization
	 * checks done within the ArchivingConnector. The user ID is used in 
	 * session monitoring. 
	 * @param mode one of TWO_PHASE_WRITE, TWO_PHASE_DELETE or ONE_PHASE
	 * @param archiveset name of the archiving set which is used in this session. Every 
	 * archiving set corresponds to at least one home collection. 
	 * @param pathextension extension of a home path to a new application-defined path
	 * @param autonaming if true, a new collection with an automatically generated name is created
	 * <b>under</b> the specified collection. If false, the specified collection is used for this session.
	 * Setting this parameter does not affect two-phase-delete sessions.
	 * @throws MalformedURLException if the URL resulting from destination and protocol
	 * is not valid
	 * @throws ArchConfigException if the archapplication is not found in Customizing
	 */
	public ArchSession getSession(String archuser, int mode, String archiveset,	URI pathextension, boolean autonaming) 
	throws ArchConfigException,	SessionHandlingException 
	{
		return getSession(archuser, mode, archiveset, pathextension, null, autonaming);
	}

	/**
	 * Creates a qualified archiving session. Status of the session is recorded.
	 * 
	 * @param archuser a user ID can be taken from e.g. the UME. There are no authorization
	 * checks done within the ArchivingConnector. The user ID is used in 
	 * session monitoring. 
	 * @param mode one of TWO_PHASE_WRITE, TWO_PHASE_DELETE or ONE_PHASE
	 * @param archiveset name of the archiving set which is used in this session. Every 
	 * archiving set corresponds to at least one home collection. 
	 * @param pathextension extension of a home path to a new application-defined path
	 * @param sessionName Human-readable name of the session. This parameter is ignored if <code>autonaming</code>
	 * is set to <code>true</code>.
	 * @param autonaming if true, a new collection with an automatically generated name is created
	 * <b>under</b> the specified collection. If false, the specified collection is used for this session.
	 * Setting this parameter does not affect two-phase-delete sessions.
	 * @throws MalformedURLException if the URL resulting from destination and protocol
	 * is not valid
	 * @throws ArchConfigException if the archapplication is not found in Customizing
	 */
	public ArchSession getSession(String archuser, int mode, String archiveset,	URI pathextension, String sessionName, boolean autonaming)
	throws ArchConfigException, SessionHandlingException 
	{
		return getSession(archuser, mode, archiveset, pathextension, sessionName,	autonaming, null, null);
	}

	/**
	 * @deprecated Use {@link #getSession(String, Integer, String, URI, String, Boolean, byte[], byte[])}
	 */
	public ArchSession getSession(String archuser, Integer mode, String archiveset, URI pathextension, String sessionName, Boolean autonaming, Job schedulerJob) 
	throws ArchConfigException,	SessionHandlingException 
	{
		throw new UnsupportedOperationException("Usage of this method is not supported any longer. Please use \"getSession(String archuser, Integer mode, String archiveset, URI pathextension, String sessionName, Boolean autonaming, byte[] writeJobID, byte[] writeTaskID)\".");
	}

	/**
	 * Creates a qualified archiving session to be run inside a scheduler job. Status of the session is recorded.
	 * 
	 * @param archuser a user ID can be taken from e.g. the UME. There are no authorization
	 * checks done within the ArchivingConnector. The user ID is used in 
	 * session monitoring. 
	 * @param mode one of TWO_PHASE_WRITE, TWO_PHASE_DELETE or ONE_PHASE
	 * @param archiveset name of the archiving set which is used in this session. Every 
	 * archiving set corresponds to at least one home collection. 
	 * @param pathextension extension of a home path to a new application-defined path
	 * @param sessionName Human-readable name of the session. This parameter is ignored if <code>autonaming</code>
	 * is set to <code>true</code>.
	 * @param autonaming if true, a new collection with an automatically generated name is created
	 * <b>under</b> the specified collection. If false, the specified collection is used for this session.
	 * Setting this parameter does not affect two-phase-delete sessions.
	 * @param schedulerJob Meta data of the scheduler job the session is run inside
	 * @throws MalformedURLException if the URL resulting from destination and protocol
	 * is not valid
	 * @throws ArchConfigException if the archapplication is not found in Customizing
	 */
	public ArchSession getSession(String archuser, Integer mode, String archiveset, URI pathextension, String sessionName, Boolean autonaming, byte[] writeJobID, byte[] writeTaskID)
	throws ArchConfigException, SessionHandlingException 
	{
		switch(mode) 
		{
			case ArchSession.ONE_PHASE:
				return new OPArchSession(archuser, archiveset, pathextension.toString(), sessionName, autonaming, writeJobID, writeTaskID);
			case ArchSession.TWO_PHASE_WRITE:
				return new TPWriteArchSession(archuser, archiveset, pathextension.toString(), sessionName, autonaming, writeJobID, writeTaskID);
			case ArchSession.TWO_PHASE_DELETE:
				return new TPDeleteArchSession(archuser, archiveset, pathextension.toString(), sessionName, writeJobID, writeTaskID);
			case ArchSession.TWO_PHASE_DELETE_SIM:
				return new TPDeleteSimArchSession(archuser, archiveset,	pathextension.toString(), sessionName, writeJobID, writeTaskID);
			default:
				throw new SessionHandlingException("Invalid mode specified");
		}
	}
}
