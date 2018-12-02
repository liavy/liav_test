package com.sap.archtech.archconn.ilm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.ArchSessionFactory;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.ArchConfigProviderSingle;
import com.sap.archtech.archconn.util.ArchSetConfigurator;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.RemoteXmldasConfigurator;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ArchiveStoreData;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

class IlmFacadeImpl implements IIlmFacade
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.ilm");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

	public boolean isArchiveStoreAppropriate(String archiveStoreName, String userName) throws ArchConnException
	{
		if(archiveStoreName == null || "".equals(archiveStoreName))
		{
			throw new IllegalArgumentException("Missing archive store name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		
    ArchSession session = null;
    try
    {
      session = ArchSessionFactory.getSessionFactory().getSession(userName, ArchSession.GLOBAL_ARCHSET);
      session.open();
      ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
      defineASCmd.addParam("action", "T");
      defineASCmd.addParam("archive_store", archiveStoreName);
      defineASCmd.execute();
      ArchResponse response = defineASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("DEFINEARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      ArrayList<? extends Serializable> archStoreList = response.getArchAdminData();
      if(archStoreList.size() != 1)
      {
      	String errMsg = new StringBuilder("Found unexpected amount of archive stores (expected 1, but found ")
      		.append(archStoreList.size()).append(")").toString();
      	cat.warningT(loc, errMsg);
        return false;
      }
      ArchiveStoreData storeData = (ArchiveStoreData)archStoreList.get(0);
      String archStoreStatus = storeData.getStoreStatus();
      if(archStoreStatus.startsWith("F"))
      {
      	String errMsg = new StringBuilder("Testing archive store \"")
    			.append(archiveStoreName)
    			.append("\" failed; archive store status is:  ")
    			.append(archStoreStatus).toString();
      	cat.warningT(loc, errMsg);
      	return false;
      }
      if(!"W".equals(storeData.getStoreType()))
      {
      	String errMsg = new StringBuilder("The archive store \"")
  				.append(archiveStoreName)
  				.append("\" is not a WebDAV store. It cannot be used for ILM.").toString();
      	cat.warningT(loc, errMsg);
      	return false;
      }
      if(storeData.getIlmConformance() < 2)
      {
      	String errMsg = new StringBuilder("The archive store \"")
					.append(archiveStoreName)
					.append("\" does not meet the ILM conformance requirements. It cannot be used for ILM.").toString();
      	cat.warningT(loc, errMsg);
    		return false;
      }
      return true;
    }
    catch(ArchConnException e)
    {
    	throw e;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Testing an archive store failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        closeArchivingSession(session, false);
      }
    }
	}

	public void syncHomePath(String archivingSetName, String userName) throws ArchConnException
	{
		if(archivingSetName == null || "".equals(archivingSetName))
		{
			throw new IllegalArgumentException("Missing archiving set name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		
		ArchSetConfigurator.createSetHome(userName, archivingSetName);
	}

	public String createHierarchySchemaCollections(String archivingSetName, String userName, CollectionRoutingParams routingParams)
	throws ArchConnException
	{
		if(archivingSetName == null || "".equals(archivingSetName))
		{
			throw new IllegalArgumentException("Missing archiving set name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(routingParams == null)
		{
			throw new IllegalArgumentException("Missing routing parameters");
		}
		
		ArchSession session = null;
		try
    {
			//*** create URI
			// home path collection
			String homePath = ArchConfigProviderSingle.getArchConfigProviderSingle().getArchSetHome(archivingSetName);
      StringBuilder uri = new StringBuilder(homePath);
      // org routing collections
      String orgRoutingPath = routingParams.getOrgRoutingPath();
      if(orgRoutingPath.startsWith("/"))
      {
      	orgRoutingPath = orgRoutingPath.substring(1);
      }
      StringBuilder pathExtension = new StringBuilder(orgRoutingPath);
      // time routing collection (name = value of the given start-of-retention date value)
      Date startOfRetention = routingParams.getStartOfRetention();
      String formattedStartOfRet = getFormattedDate(startOfRetention, true);
      String[] timeRoutingCollNameParts = formattedStartOfRet.split("-");
      for(String timeRoutingCollNamePart : timeRoutingCollNameParts)
      {
      	pathExtension.append(timeRoutingCollNamePart);
      }
      uri.append(pathExtension);
      //*** call MODIFYPATH command
      session = ArchSessionFactory.getSessionFactory().getSession(userName, archivingSetName);
			session.open();
      ArchCommand archCommand = session.createCommand(ArchCommand.AC_MODIFYPATH);
      archCommand.addParam(new URI(uri.toString() + "/"));// URI must end with a '/'
      archCommand.execute();
      ArchResponse response = archCommand.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("MODIFYPATH returned ")
        	.append(responseStatusCode)
        	.append("(protocol message: ")
        	.append(response.getProtMessage())
        	.append(" , service message: ")
        	.append(response.getServiceMessage())
        	.append(")")
        	.toString();
        cat.errorT(loc, errMsg);
        throw new ArchConnException("Failed to create collections: " + errMsg);
      }
      //*** return path extension
      return pathExtension + "/";
    }
		catch(ArchConnException e)
		{
			throw e;
		}
    catch(Exception e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "Creating hierarchy schema collections failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
      	closeArchivingSession(session, false);
      }
    }
	}

	public ArchSession createWriteSession(String archivingSetName, String userName, WriteSessionParams writeSessionParams) throws ArchConnException
	{
		if(archivingSetName == null || "".equals(archivingSetName))
		{
			throw new IllegalArgumentException("Missing archiving set name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(writeSessionParams == null)
		{
			throw new IllegalArgumentException("Missing write session parameters");
		}

		ArchSession writeSession = null;
		ArchSession unqualSession = null;
		try
		{
			//*** create write session to be used for subsequent archiving step (PUT)
			String pathExtension = writeSessionParams.getPathExtension();
			byte[] writeJobID = writeSessionParams.getWriteJobID();
			byte[] writeTaskID = writeSessionParams.getWriteTaskID();
			writeSession = ArchSessionFactory.getSessionFactory().getSession(userName, ArchSession.TWO_PHASE_WRITE, archivingSetName, new URI(pathExtension), null, true, writeJobID, writeTaskID);
			String sessionComment = writeSessionParams.getSessionComment();
			if(sessionComment != null && !"".equals(sessionComment))
			{
				writeSession.setComment(sessionComment);
			}
			writeSession.open();
			//*** assign archive store to write session collection
			String writeSessionUri = writeSession.getCollection().toString();
			String archiveStoreName = writeSessionParams.getArchiveStoreName();
      assignArchiveStore(archiveStoreName, userName, writeSessionUri);
      //*** call PROPERTYSET to set the ILM properties for the time routing collection
      unqualSession = ArchSessionFactory.getSessionFactory().getSession(userName, archivingSetName);
      ArchCommand archCommand = unqualSession.createCommand(ArchCommand.AC_PROPERTYSET);
			String timeRoutingCollUri = new StringBuilder(writeSession.getArchsethome().toString()).append(pathExtension).toString();
			timeRoutingCollUri = timeRoutingCollUri.substring(0, timeRoutingCollUri.length() - 1);
			archCommand.addParam(new URI(timeRoutingCollUri));// URI must not end with a '/'
      ArchivingPropertyValues archPropValues = new ArchivingPropertyValues();
      Date startOfRetention = writeSessionParams.getStartOfRetention();
      archPropValues.putProp("start_of_retention", getFormattedDate(startOfRetention, false));
      Date expirationDate = writeSessionParams.getExpirationDate();
      String formattedExpDate = getFormattedDate(expirationDate, false);
      archPropValues.putProp("expiration_date", formattedExpDate);
      archCommand.addParam(archPropValues);
      archCommand.execute();
      ArchResponse response = archCommand.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("PROPERTYSET returned ")
        	.append(responseStatusCode)
        	.append("(protocol message: ")
        	.append(response.getProtMessage())
        	.append(" , service message: ")
        	.append(response.getServiceMessage())
        	.append(")")
        	.toString();
        cat.errorT(loc, errMsg);
        throw new ArchConnException("Failed to set ILM properties: " + errMsg);
      }
      //*** call PROPERTYSET to set the ILM property for the session collection
	    archCommand = unqualSession.createCommand(ArchCommand.AC_PROPERTYSET);
	    writeSessionUri = writeSessionUri.substring(0, writeSessionUri.length() - 1);
      archCommand.addParam(new URI(writeSessionUri));// URI must not end with a '/'
      archPropValues = new ArchivingPropertyValues();
      archPropValues.putProp("expiration_date", formattedExpDate);
      archPropValues.putProp("origin", "ADMI_RUN_D");
      archCommand.addParam(archPropValues);
      archCommand.execute();
      response = archCommand.getResponse();
      responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("PROPERTYSET returned ")
        	.append(responseStatusCode)
        	.append("(protocol message: ")
        	.append(response.getProtMessage())
        	.append(" , service message: ")
        	.append(response.getServiceMessage())
        	.append(")")
        	.toString();
        cat.errorT(loc, errMsg);
        throw new ArchConnException("Failed to set ILM property: " + errMsg);
      }
	    return writeSession;
		}
		catch(ArchConnException e)
		{
			if(writeSession != null)
      {
				closeArchivingSession(writeSession, true);
			}
			cat.logThrowableT(Severity.ERROR, loc, "Creating write session failed", e);
			throw e;
		}
		catch(Exception e)
    {
			if(writeSession != null)
      {
				closeArchivingSession(writeSession, true);
      }
    	cat.logThrowableT(Severity.ERROR, loc, "Creating write session failed", e);
      throw new ArchConnException(e.getMessage());
    }
		finally
		{
			if(unqualSession != null)
			{
				closeArchivingSession(unqualSession, false);
			}
		}
	}

	public void assignArchiveStore(String archiveStoreName, String userName, String archivePath) throws ArchConnException
	{
		if(archiveStoreName == null || "".equals(archiveStoreName))
		{
			throw new IllegalArgumentException("Missing archive store name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(archivePath == null || "".equals(archivePath))
		{
			throw new IllegalArgumentException("Missing archive path");
		}
		
		boolean success = RemoteXmldasConfigurator.assignArchiveStore(archiveStoreName, archivePath, ArchConfigProviderSingle.getArchConfigProviderSingle().getArchDest(), userName);
		if(!success)
		{
			throw new ArchConnException("Assignment of archive store \"" + archiveStoreName + "\" to archive path \"" + archivePath + "\" failed");
		}
	}

	public void unassignArchiveStore(String userName, String archivePath) throws ArchConnException
	{
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(archivePath == null || "".equals(archivePath))
		{
			throw new IllegalArgumentException("Missing archive path");
		}
		
		boolean success = RemoteXmldasConfigurator.unassignArchiveStore(archivePath, ArchConfigProviderSingle.getArchConfigProviderSingle().getArchDest(), userName);
		if(!success)
		{
			throw new ArchConnException("Removing the archive store assignment for archive path \"" + archivePath + "\" failed");
		}
	}

	public void writeToArchive(String archivingSetName, String userName, Write2ArchiveParams write2ArchiveParams)	throws ArchConnException
	{
		if(archivingSetName == null || "".equals(archivingSetName))
		{
			throw new IllegalArgumentException("Missing archiving set name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(write2ArchiveParams == null)
		{
			throw new IllegalArgumentException("Missing parameters for writing to archive");
		}

		ArchSession unqualSession = null;
		try
		{
			//*** create and execute PUT command for the given resource
			ArchSession writeSession = write2ArchiveParams.getWriteSession();
			ArchCommand archCommand = writeSession.createCommand(ArchCommand.AC_PUT);
			String resourceName = write2ArchiveParams.getResourceName();
			if(resourceName == null || "".equals(resourceName))
	    {
	      // use autonaming for resources
				archCommand.addParam(ArchCommand.RESOURCE_NAME_AUTO);
	    }
	    else
	    {
	    	archCommand.addParam(ArchCommand.RESOURCE_NAME, resourceName);
	    }
			archCommand.addParam(ArchCommand.PUT_MODE_STORE);
			String resourceType = write2ArchiveParams.getResourceType();
			archCommand.addParam(ArchCommand.RESOURCE_TYPE, resourceType);
			String checkLevel = write2ArchiveParams.getCheckLevel();
			archCommand.addParam(checkLevel);
			archCommand.addParam("user", userName);
			IndexPropValues ipv = write2ArchiveParams.getIpv();
			if(ipv != null)
	    {
				archCommand.addParam(ipv);
	    }
			InputStream resourceAsStream = write2ArchiveParams.getResourceAsStream();
			archCommand.addParam(resourceAsStream);
			archCommand.execute();
	    ArchResponse response = archCommand.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("PUT returned ")
        	.append(responseStatusCode)
        	.append("(protocol message: ")
        	.append(response.getProtMessage())
        	.append(" , service message: ")
        	.append(response.getServiceMessage())
        	.append(")")
        	.toString();
        cat.errorT(loc, errMsg);
        throw new ArchConnException("Writing to the archive failed: " + errMsg);
      }
      //*** call PROPERTYSET to set the ILM property for the resource
	    unqualSession = ArchSessionFactory.getSessionFactory().getSession(userName, archivingSetName);
      archCommand = unqualSession.createCommand(ArchCommand.AC_PROPERTYSET);
      if(resourceName == null || "".equals(resourceName))
      {
      	resourceName = response.getHeaderField("resource_name");
      }
      String resourceUri = new StringBuilder(writeSession.getCollection().toString()).append(resourceName).toString();
      archCommand.addParam(new URI(resourceUri));
      ArchivingPropertyValues archPropValues = new ArchivingPropertyValues();
      Date expirationDate = write2ArchiveParams.getExpirationDate();
      archPropValues.putProp("expiration_date", getFormattedDate(expirationDate, false));
      archCommand.addParam(archPropValues);
      archCommand.execute();
      response = archCommand.getResponse();
      responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("PROPERTYSET returned ")
        	.append(responseStatusCode)
        	.append("(protocol message: ")
        	.append(response.getProtMessage())
        	.append(" , service message: ")
        	.append(response.getServiceMessage())
        	.append(")")
        	.toString();
        cat.errorT(loc, errMsg);
        throw new ArchConnException("Failed to set ILM property: " + errMsg);
      } 
		}
		catch(ArchConnException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Writing to the archive failed", e);
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Writing to the archive failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(unqualSession != null)
			{
				closeArchivingSession(unqualSession, false);
			}
		}
	}
	
	public void closeArchivingSession(ArchSession archSession, boolean toBeCanceled)
	{
		if(archSession == null)
		{
			throw new IllegalArgumentException("Missing archiving session");
		}
		
		if(!toBeCanceled)
		{
			try
			{
				archSession.close();
			}
			catch(SessionHandlingException e)
			{
				cat.logThrowableT(Severity.ERROR, loc, "Closing the archiving write session failed", e);
				cancelArchSession(archSession);
			}
		}
		else
		{
			cancelArchSession(archSession);
		}
	}
	
	public void confirmResourceDeletion(String archivingSetName, String userName, ArchSession incompleteArchSession) throws ArchConnException
	{
		if(archivingSetName == null || "".equals(archivingSetName))
		{
			throw new IllegalArgumentException("Missing archiving set name");
		}
		if(userName == null || "".equals(userName))
		{
			throw new IllegalArgumentException("Missing user name");
		}
		if(incompleteArchSession == null)
		{
			throw new IllegalArgumentException("Missing archiving session");
		}

		ArchSession archSession = null;
		try
		{
			URI writeSessionUri = incompleteArchSession.getCollection();
			URI homePathUri = incompleteArchSession.getArchsethome();
			URI pathExtension = homePathUri.relativize(writeSessionUri);
			archSession = ArchSessionFactory.getSessionFactory().getSession(userName, ArchSession.TWO_PHASE_DELETE, archivingSetName, pathExtension, false);
			archSession.open();
	    ArchResponse response = null;
	    int responseStatusCode = 0;
	    do
	    {
	      ArchCommand pickCommand = archSession.createCommand(ArchCommand.AC_PICK);
	      // process response from PICK before
	      if(response != null)
	      {
	        pickCommand.addParam(response.getResKey());
	      }
	      pickCommand.execute();
	      response = pickCommand.getResponse();
	      responseStatusCode = response.getStatusCode();
	    }
	    while(responseStatusCode == DasResponse.SC_OK);
			if(responseStatusCode != DasResponse.SC_NO_MORE_OBJECTS)
			{
				String errMsg = new StringBuilder("PICK returned ")
      	.append(responseStatusCode)
      	.append("(protocol message: ")
      	.append(response.getProtMessage())
      	.append(" , service message: ")
      	.append(response.getServiceMessage())
      	.append(")")
      	.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException("Failed to execute the PICK command: " + errMsg);
			}
			closeArchivingSession(archSession, false);
		}
		catch(ArchConnException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Completing the archive session failed", e);
			if(archSession != null)
			{
				closeArchivingSession(archSession, true);
			}
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Completing the archive session failed", e);
			if(archSession != null)
			{
				closeArchivingSession(archSession, true);
			}
			throw new ArchConnException(e.getMessage());
		}
	}

	// Helpers
	private String getFormattedDate(Date date, boolean useFallback4UnknownDate)
	{
		Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTime(date);
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;// month field is 0-based
    int day = cal.get(Calendar.DAY_OF_MONTH);
  	// check if "unknown" or fallback date must be returned if given date = 12/31/9999
  	if(year == 9999 && month == 12 && day == 31)
  	{
  		if(useFallback4UnknownDate)
      {
  			// fallback = current date
      	date = new Date(System.currentTimeMillis());
      	cal.setTime(date);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;// month field is 0-based
        day = cal.get(Calendar.DAY_OF_MONTH);
      }
  		else
  		{
  			return "unknown";
  		}
  	}
    StringBuilder formattedDate = new StringBuilder(Integer.valueOf(year).toString());
    if(month > 9)
    {
    	formattedDate.append('-').append(month);
    }
    else
    {
    	formattedDate.append("-0").append(month);
    }
    if(day > 9)
    {
    	formattedDate.append('-').append(day);
    }
    else
    {
    	formattedDate.append("-0").append(day);
    }		
    return formattedDate.toString();
	}
	
	private void cancelArchSession(ArchSession archSession)
	{
		try
		{
			archSession.cancel();
			cat.infoT(loc, "Cancellation of archiving session successful");
		}
  	catch(SessionHandlingException ex)
    {
  		cat.logThrowableT(Severity.WARNING, loc, "Cancellation of archiving session failed", ex);
    }
	}
}
