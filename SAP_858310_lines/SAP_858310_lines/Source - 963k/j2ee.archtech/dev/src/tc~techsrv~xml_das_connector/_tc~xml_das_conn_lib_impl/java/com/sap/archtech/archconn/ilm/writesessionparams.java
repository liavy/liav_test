package com.sap.archtech.archconn.ilm;

import java.util.Date;

/**
 * The <code>WriteSessionParams</code> class represents the parameters required to create an archive write session. 
 */
public class WriteSessionParams 
{
	private final String archiveStoreName;
	private final String pathExtension;
	private final Date startOfRetention;
	private final Date expirationDate;
	private final String sessionComment;
	private final byte[] writeJobID;
	private final byte[] writeTaskID;
	
	public WriteSessionParams(String archiveStoreName, String pathExtension, Date startOfRetention, Date expirationDate, String sessionComment, byte[] writeJobID, byte[] writeTaskID)
	{
		if(archiveStoreName == null || "".equals(archiveStoreName))
		{
			throw new IllegalArgumentException("Missing archive store name");
		}
		this.archiveStoreName = archiveStoreName;
		this.pathExtension = pathExtension != null ? pathExtension : "";
		if(startOfRetention == null)
		{
			throw new IllegalArgumentException("Missing start-of-retention date");
		}
		this.startOfRetention = new Date(startOfRetention.getTime());
		if(expirationDate == null)
		{
			throw new IllegalArgumentException("Missing expiration date");
		}
		this.expirationDate = new Date(expirationDate.getTime());
		// note: cannot persist empty session comment, but passing "null" is ok
		this.sessionComment = "".equals(sessionComment) ? null : sessionComment;
		if(writeJobID != null)
    {
    	this.writeJobID = new byte[writeJobID.length];
    	System.arraycopy(writeJobID, 0, this.writeJobID, 0, writeJobID.length);
    }
		else
		{
			this.writeJobID = null;
		}
		if(writeTaskID != null)
    {
    	this.writeTaskID = new byte[writeTaskID.length];
    	System.arraycopy(writeTaskID, 0, this.writeTaskID, 0, writeTaskID.length);
    }
    else
    {
    	this.writeTaskID = null;
    }
	}

	/**
	 * Get the name of the archive store which is to be assigned to the write session collection
	 */
	public String getArchiveStoreName() 
	{
		return archiveStoreName;
	}

	/**
	 * Get the path of the parent collection of the session collection to be created
	 */
	public String getPathExtension() 
	{
		return pathExtension;
	}

	/**
	 * Get the start-of-retention date (to be set for the session collection)
	 */
	public Date getStartOfRetention() 
	{
		return new Date(startOfRetention.getTime());
	}

	/**
	 * Get the expiration date (to be set for the session collection)
	 */
	public Date getExpirationDate() 
	{
		return new Date(expirationDate.getTime());
	}

	/**
	 * Get the descriptive text of the session
	 */
	public String getSessionComment() 
	{
		return sessionComment;
	}

	/**
	 * Get the ID of the scheduler job that creates the archive write session (optional parameter)
	 */
	public byte[] getWriteJobID() 
	{
		byte[] tmp = null;
		if(writeJobID != null)
    {
    	tmp = writeJobID;
    	System.arraycopy(writeJobID, 0, tmp, 0, writeJobID.length);
    }
		return tmp;
	}

	/**
	 * Get the ID of the scheduler task ID that creates the archive write session (optional parameter)
	 */
	public byte[] getWriteTaskID() 
	{
		byte[] tmp = null;
		if(writeTaskID != null)
    {
    	tmp = writeTaskID;
    	System.arraycopy(writeTaskID, 0, tmp, 0, writeTaskID.length);
    }
		return tmp;
	}
}
