package com.sap.archtech.archconn;

import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.URI;

public class InternalArchSessionFactory 
{
	private static InternalArchSessionFactory theFactory = new InternalArchSessionFactory();

	private InternalArchSessionFactory() 
	{
	}

	public static InternalArchSessionFactory getInstance() 
	{
		return theFactory;
	}
	
	public ArchSession getUnqualifiedArchSession4Destination(String archuser, String archSetName, String destinationName) throws ArchConfigException 
	{
		try 
		{
			return new UnqualifiedArchSession(archuser, archSetName != null ? archSetName : ArchSession.GLOBAL_ARCHSET, destinationName);
		} 
		catch(SessionHandlingException e) 
		{
			// SessionHandlingException is only thrown in "AbstractArchSession" with "checkArchiveSet = true"
			throw new AssertionError("Caught unexpected SessionHandlingException: "	+ e.getMessage());
		}
	}

	public ArchSession getQualifiedArchSession4Destination(String archuser, int mode, String archiveset, URI pathextension, boolean autonaming, String destinationName) 
	throws ArchConfigException,	SessionHandlingException 
	{
		switch(mode) 
		{
			case ArchSession.ONE_PHASE:
				return new OPArchSession(archuser, archiveset, pathextension.toString(), autonaming, destinationName);
			case ArchSession.TWO_PHASE_WRITE:
				return new TPWriteArchSession(archuser, archiveset, pathextension.toString(), autonaming, destinationName);
			case ArchSession.TWO_PHASE_DELETE:
				return new TPDeleteArchSession(archuser, archiveset, pathextension.toString(), destinationName);
			case ArchSession.TWO_PHASE_DELETE_SIM:
				return new TPDeleteSimArchSession(archuser, archiveset,	pathextension.toString(), destinationName);
			default:
				throw new SessionHandlingException("Invalid mode specified");
		}
	}
}
