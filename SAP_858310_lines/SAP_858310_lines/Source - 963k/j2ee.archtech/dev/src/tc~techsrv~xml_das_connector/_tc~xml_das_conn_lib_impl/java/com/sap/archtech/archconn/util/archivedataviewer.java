package com.sap.archtech.archconn.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.InternalArchSessionFactory;
import com.sap.archtech.archconn.commands.AbstractArchCommand;
import com.sap.archtech.archconn.commands.ListCommand;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.values.ArchivePathData;
import com.sap.archtech.archconn.values.ArchiveStoreData;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.ColSearchClause;
import com.sap.archtech.archconn.values.ColSearchResult;
import com.sap.archtech.archconn.values.ResourceData;
import com.sap.archtech.archconn.values.ServiceInfo;
import com.sap.archtech.archconn.values.WebDavStoreConformanceData;
import com.sap.security.api.UMException;
import com.sap.security.core.role.imp.xml.XMLServiceRepository;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>ArchiveDataViewer</code> class provides a viewer on archive (meta) data maintained
 * by XML DAS. In contrast to the command API, the methods provided by this class are intended to
 * be used for administrative purposes only, e.g. for archive browing. 
 */
public class ArchiveDataViewer
{
	private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
	private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
	
	private static final Set<String> allowedPathTypes = new HashSet<String>(4);
	private static final Set<String> allowedRangeTypes = new HashSet<String>(3);
	private static final Set<String> allowedResourceTypes = new HashSet<String>(7);
	static
	{
		allowedPathTypes.add("H");	// all existing home paths
		allowedPathTypes.add("A");	// all direct descendant paths
		allowedPathTypes.add("S");	// checks if at least one child node is assigned
		allowedPathTypes.add("X"); 	// checks if archive path is assignable/unassignable
		
		allowedRangeTypes.add("COL");
		allowedRangeTypes.add("RES");
		allowedRangeTypes.add("ALL");
		
		allowedResourceTypes.add("XML");
		allowedResourceTypes.add("XSD");
		allowedResourceTypes.add("XSL");
		allowedResourceTypes.add("BIN");
		allowedResourceTypes.add("ALL_XML");
		allowedResourceTypes.add("ADK");
		allowedResourceTypes.add("NON_ADK_BIN");
	}
	
	private final String destinationName;	
	public ArchiveDataViewer(String destinationName)
	{
		this.destinationName = destinationName;
	}
	
	/**
	 * Get the name of the HTTP destination of the XML DAS instance this viewer is working on
	 */
	public String getNameOfDasDestination()
	{
		return destinationName;
	}
	
	/**
	 * Lists all archive stores maintained by a given XML DAS instance.
	 * @param userName Name of the user performing this action
	 * @return List of the archive stores. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ArchiveStoreData[] listArchiveStores(String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
			defineASCmd.addParam("action", "L");
			defineASCmd.execute();
			ArchResponse response = defineASCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"DEFINEARCHIVESTORES\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ArrayList<? extends Serializable> archiveStores = response.getArchAdminData();
			return archiveStores.toArray(new ArchiveStoreData[archiveStores.size()]);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive stores failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive stores failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}
	
	/**
	 * Lists all archive paths below a given archive path.
	 * @param pathType Must be one out of {H, A, S, X}
	 * @param parentArchivePath The given archive path (ignored if pathType = H)
	 * @param userName Name of the user performing this action
	 * @return List of the archive paths. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ArchivePathData[] listArchivePaths(String pathType, String parentArchivePath, String userName) throws ArchConnException
	{
		// check pathType
		if(pathType == null || !allowedPathTypes.contains(pathType.toUpperCase()))
		{
			throw new IllegalArgumentException("Unsupported archive path type: " + pathType);
		}
		
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand listAPCmd = session.createCommand("_list_archive_paths");
			listAPCmd.addParam("type", pathType);
			if("H".equalsIgnoreCase(pathType))
			{
				listAPCmd.addParam("archive_path", "");
			}
			else
			{
				listAPCmd.addParam("archive_path", parentArchivePath);
			}
			listAPCmd.execute();
			ArchResponse response = listAPCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"LISTARCHIVEPATHS\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ArrayList<? extends Serializable> archivePaths = response.getArchAdminData();
			return archivePaths.toArray(new ArchivePathData[archivePaths.size()]);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive paths failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive paths failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}
	
	/**
	 * @deprecated
	 */
	public ResourceData[] hierarchyQuery(String archivePath, String range, boolean isRecursive, String resourceType, boolean onlyDeletedRes, String userName)
	throws ArchConnException
	{
		return hierarchyQuery(archivePath, range, isRecursive, resourceType, onlyDeletedRes, true, userName);
	}
	
	/**
	 * Wrapper method for LIST command execution
	 * @param archivePath Start path for hierarchy query
	 * @param range	Must be one out of {COL, RES, ALL}
	 * @param isRecursive Indicates whether a recursive search is to be performed or not
	 * @param resourceType Must be one out of {XML, XSD, XSL, BIN}
	 * @param onlyDeletedRes Indicates whether only resources that have been deleted from the database are to be returned
	 * @param provideNrOfHits Indicates whether the number of hits is to be returned
	 * @param userName Name of the user performing this action
	 * @return List of the <code>ResourceData</code> instances. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException ArchConnException Thrown if any problem occurred
	 */
	public ResourceData[] hierarchyQuery(String archivePath, String range, boolean isRecursive, String resourceType, boolean onlyDeletedRes, boolean provideNrOfHits, String userName)
	throws ArchConnException
	{
		// Note: reference to the ArchSession must be kept as long as the command is needed - otherwise the HTTP connection
		// may become subject to garbage collection
		ArchSession archSession = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
		ArchCommand listCmd = executeListCommand(archSession, new HierarchyQueryParams(archivePath, range, isRecursive, resourceType, onlyDeletedRes, provideNrOfHits, null));
		ArchResponse response = getQueryResponse(listCmd);
		try
		{
			ArrayList<ResourceData> resources = response.getResourceData();
			if(resources == null)
			{
				return new ResourceData[0];
			}
			return resources.toArray(new ResourceData[resources.size()]);
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the resource data failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the resource data failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(archSession != null)
			{
				archSession.close();
			}
		}
	}

	/**
	 * Execute a hierarchy query and return the result in blocks of fixed length.
	 * Use this method to avoid memory problems if the query result becomes too large. 
	 * @param blockLength The length of the result block (max. number of <code>ResourceData</code> instances returned) 
	 * @param queryCmd Archiving command representing the hierarchy query. Must be non-null if the next result block is to be read.
	 * @param params Contains all params required to perform a hierarchy query. Ignored if <code>queryCmd</code> is non-null.
	 * @return The list of <code>ResourceData</code> instances read and a reference to the archiving command used. 
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public BlockHierarchyQueryResult blockHierarchyQuery(int blockLength, ArchCommand queryCmd, HierarchyQueryParams params)
	throws ArchConnException
	{
		ArchSession archSession = null;
		if(queryCmd == null)
		{
			//*** this is the first call for this command	
			archSession = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(params.getUserName(), null, destinationName);
			queryCmd = executeListCommand(archSession, params);
		}
		else
		{
			archSession = ((AbstractArchCommand)queryCmd).getArchSessionRef(this);
		}
		
		// get resource data from response stream -> do not close the session here
		try
		{
			ArrayList<ResourceData> resources = ((ListCommand)queryCmd).getNextResourceDataBlock(blockLength);
			if(resources == null)
			{
				return new BlockHierarchyQueryResult(new ArrayList<ResourceData>(0), queryCmd, archSession);
			}
			return new BlockHierarchyQueryResult(resources, queryCmd, archSession);
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the resource data failed", e);
			throw new ArchConnException("io_exception");
		}
	}

	private ArchCommand executeListCommand(ArchSession archSession, HierarchyQueryParams params) throws ArchConnException
	{
		String range = params.getRange();
		if(range != null && !allowedRangeTypes.contains(range.toUpperCase()))
		{
			throw new IllegalArgumentException("Unsupported range type: " + range);
		}
		// check resource type parameter (optional parameter)
		String resourceType = params.getResourceType();
		if(resourceType != null && !allowedResourceTypes.contains(resourceType.toUpperCase()))
		{
			throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
		}
			
		try
		{
			ArchCommand queryCmd = archSession.createCommand(ArchCommand.AC_LIST);
			queryCmd.addParam(new URI(params.getArchivePath()));
			if(range != null)
			{
				queryCmd.addParam("list_range", range);
			} 
			queryCmd.addParam("recursive", params.isRecursive() ? "Y" : "N");
			if(resourceType != null)
			{  
				queryCmd.addParam("type", resourceType);
			}
			queryCmd.addParam("del_only", params.onlyDeletedRes() ? "Y" : "N");
			queryCmd.addParam("provide_nr_of_hits", params.provideNrOfHits() ? "Y" : "N");
			queryCmd.execute();
			return queryCmd;
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the LIST command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the LIST command failed", e);
			throw new ArchConnException(e.getMessage());
		}
	}
	
	private ArchResponse getQueryResponse(ArchCommand queryCmd) throws ArchConnException
	{
		ArchResponse response = queryCmd.getResponse();
		try
		{
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"LIST\" returned ")
					.append(responseStatusCode)
		 			.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
					cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			return response;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the command response failed", e);
			throw new ArchConnException("io_problem");
		}
	}
	
	/**
	 * Release all internal resources held by the given hierarchy query (LIST command).
	 * This method must be called after finishing loading all query results via {@link #blockHierarchyQuery(int, ArchCommand, HierarchyQueryParams)}.
	 * @param listCmd Represents the hierarchy query
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public void releaseHierarchyQuery(ArchCommand listCmd) throws ArchConnException
	{
		if(!(listCmd instanceof ListCommand))
		{
			throw new IllegalArgumentException("Unsupported command class: " + listCmd.getClass().getName());
		}
		try
		{
			// closing the response stream may take several minutes because the whole stream is completely read first
			releaseResponseAsynchronously(listCmd);
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Releasing the hierarchy query failed", e);
			throw new ArchConnException(e.getMessage());
		}
	}
	
	/**
	 * Check if the response stream held by the given hierarchy query is still open.
	 * Use this method to check whether another call to {@link #blockHierarchyQuery(int, ArchCommand, HierarchyQueryParams)}
	 * would return another block of data. 
	 * @param listCmd Represents the hierarchy query
	 * @return <code>true</code> if the response stream is still open
	 */
	public boolean isHierarchyQueryResponseStreamOpen(ArchCommand listCmd)
	{
		if(!(listCmd instanceof ListCommand))
		{
			throw new IllegalArgumentException("Unsupported command class: " + listCmd.getClass().getName());
		}
		return ((ListCommand)listCmd).isResponseStreamOpen();
	}
	
	/**
	 * Get the meta data of a given resource or collection.
	 * @param uri Identifies the resource or collection
	 * @param userName Name of the user performing this action
	 * @return A <code>ResourceData</code> instance holding the meta data. Is <code>null</code> if the meta data cannot be loaded. 
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ResourceData getMetaData(URI uri, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand headCmd = session.createCommand(ArchCommand.AC_HEAD);
			headCmd.addParam(uri);
			headCmd.execute();
			ArchResponse response = headCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"HEAD\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			// There should be only one ResourceData object contained in the list
			return (ResourceData)response.getResourceData().get(0);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the HEAD command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the HEAD command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}	
	}
	
	/**
	 * Load physical information for a given collection e.g. the physical path or archive store information. 
	 * @param collectionUri Identifies the collection. If <code>null</code> is passed, the method will return general
	 * information about the Data Archiving Service e.g. the release number.
	 * @param userName Name of the user performing this action
	 * @return A <code>ServiceInfo</code> instance holding the data. Is <code>null</code> if the data cannot be retrieved.
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ServiceInfo getCollectionInfo(URI collectionUri, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand infoCmd = session.createCommand(ArchCommand.AC_INFO);
			if(collectionUri != null)
			{
				infoCmd.addParam(collectionUri);
			} 
			infoCmd.execute();
			ArchResponse response = infoCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"INFO\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			return response.getServiceInfo();
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the INFO command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the INFO command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}	
	}
	
	/**
	 * Get the release information of the Data Archiving Service.
	 * @param userName Name of the user performing this action
	 * @return Release information of the Data Archiving Service or <code>null</code> if this information could not be retrieved
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public String getReleaseInfo(String userName) throws ArchConnException
	{
		ServiceInfo serviceInfo = getCollectionInfo(null, userName);
		if(serviceInfo != null)
		{
			return serviceInfo.getRelease();
		}
		cat.errorT(loc, "Could not get the release information of the Data Archiving Service. Check the preceeding log entries.");
		return null;
	}
	
	/**
	 * Get the names of all all legal hold cases stored with a given collection or resource.
	 * @param uri Identifies the collection or resource
	 * @param userName Name of the user performing this action
	 * @return List of all names of legal hold cases. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public String[] getLegalHoldCases(URI uri, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand infoCmd = session.createCommand(ArchCommand.AC_LEGALHOLDGET);
			infoCmd.addParam(uri);
			infoCmd.execute();
			ArchResponse response = infoCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"LEGALHOLDGET\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			return response.getLegalHoldValues().getLegalHoldCases(uri.toString());
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the LEGALHOLDGET command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the LEGALHOLDGET command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}	
	}
	
	/**
	 * Get all properties stored with a given collection or resource.
	 * Primarily intended to be used to check for ILM-specific properties (e.g. retention time). 
	 * @param uri Identifies the collection or resource
	 * @param userName Name of the user performing this action
	 * @return A <code>ArchivingPropertyValues</code> instance holding all properties. Is <code>null</code> if the information cannot be retrieved.
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ArchivingPropertyValues getArchivingProperties(URI uri, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand infoCmd = session.createCommand(ArchCommand.AC_PROPERTYGET);
			infoCmd.addParam(uri);
			infoCmd.execute();
			ArchResponse response = infoCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"PROPERTYGET\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			return response.getArchivingPropertyValues();
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the PROPERTYGET command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Executing the PROPERTYGET command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}	
	}
	
	/**
	 * List all archive paths that are currently assigned to a given archive store.
	 * @param archStoreName Name of the user performing this action
	 * @param userName Name of the user performing this action
	 * @return List of assigned archive paths. The list may be empty but is never <code>null</code>.
	 * @throws UnsupportedCommandException Thrown if the given XML DAS does not support this command yet 
	 * @throws ArchConnException Thrown if any other problem occurred
	 */
	public ArchivePathData[] listAssignedArchivePaths(String archStoreName, String userName) throws UnsupportedCommandException, ArchConnException
	{ 
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand listAssignedAPCmd = session.createCommand("_list_assigned_archive_paths");
			listAssignedAPCmd.addParam("archive_store", archStoreName);
			listAssignedAPCmd.execute();
			ArchResponse response = listAssignedAPCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode == 405)
			{
				// method "_LIST_ASSIGNED_ARCHIVE_PATHS" not yet implemented by the given XMLDAS
				throw new UnsupportedCommandException("Data Archiving Service at destination \"" + destinationName + "\" does not support the command LIST_ASSIGNED_ARCHIVE_PATHS");
			}
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"LIST_ASSIGNED_ARCHIVE_PATHS\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ArrayList<? extends Serializable> archivePaths = response.getArchAdminData();
			return archivePaths.toArray(new ArchivePathData[archivePaths.size()]);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive paths failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Listing the archive paths failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}
	
	/**
	 * Deploy security roles to UME. This method has been added here 
	 * because there is no access to the UME core DC in the Webdynpro layer.
	 * @param deployCompName Name of the component to be deployed.
	 * Reusing the same value for this parameter in subsequent invocations of this
	 * method will result in overwriting preceeding deployments.
	 * @param actionsXml XML stream containing the actions and security roles to be deployed
	 * @throws UMException Thrown if deploying the security roles failed
	 * @throws ArchConnException Thrown if any unexpected error occurred
	 */
	public void deploySecurityRoles(String deployCompName, String actionsXml) 
	throws UMException, ArchConnException
	{
		try
		{
			XMLServiceRepository.deployActionsXMLFile(deployCompName, new ByteArrayInputStream(actionsXml.getBytes("UTF-8")));
		}
		catch(UnsupportedEncodingException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Deployment of security roles failed due to an encoding problem of the XML stream", e);
			throw new ArchConnException(e.getMessage());
		}
	}
	
	/**
	 * Undeploy security roles in UME. This method has been added here 
	 * because there is no access to the UME core DC in the Webdynpro layer.
	 * @param deployCompName Name of the component to be undeployed.
	 */
	public void undeploySecurityRoles(String deployCompName)
	{
		XMLServiceRepository.undeployActionsFile(deployCompName);
	}
	
	/**
	 * @deprecated Use {@link #collectionSearch4AllOrigins(URI, String)} instead
	 */
	public String[] collectionSearch4AllOrigins(String userName) throws ArchConnException
	{
		return collectionSearch4AllOrigins(null, userName);
	}
	
	/**
	 * Get all values of "origin" properties stored with collections of the given XMLDAS instance.
	 * @param searchRoot URI of the root collection below of which the search is to be started. May be <code>null</code>. 
	 * @param userName Name of the user performing this action
	 * @return List of all values of "origin" properties. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred.
	 */
	public String[] collectionSearch4AllOrigins(URI searchRoot, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand archCommand = session.createCommand(ArchCommand.AC_COLSEARCH);
			ColSearchClause clause = new ColSearchClause();
			clause.addQueryPart("origin", "*", null, null);
			archCommand.addParam(clause);
			if(searchRoot != null)
			{
				archCommand.addParam(searchRoot);
			}
			archCommand.execute();
			ArchResponse response = archCommand.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"COLSEARCH\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(", service message: ")
					.append(response.getServiceMessage())
					.append(", error message: ")
					.append(response.getErrorMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ColSearchResult result = response.getColSearchResult();
			// colpropvalues are contained in the resulting string array
			return result.getResultStrings();
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}
	
	/**
	 * @deprecated Use {@link #collectionSearch4Origin(String, URI, String)} instead
	 */
	public String[] collectionSearch4Origin(String originPropValue, String userName) throws ArchConnException
	{
		return collectionSearch4Origin(originPropValue, null, userName);
	}

	/**
	 * Get the names of all collections having their "origin" property set to the given value.
	 * @param originPropValue Value of the "origin" property. Must neither be <code>null</code> nor an empty string.
	 * @param searchRoot URI of the root collection below of which the search is to be started. May be <code>null</code>. 
	 * @param userName Name of the user performing this action
	 * @return List of the names of all matching collections. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred.
	 */
	public String[] collectionSearch4Origin(String originPropValue, URI searchRoot, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand archCommand = session.createCommand(ArchCommand.AC_COLSEARCH);
			ColSearchClause clause = new ColSearchClause();
			clause.addQueryPart("origin", originPropValue, null, null);
			archCommand.addParam(clause);
			archCommand.addParam("colname_only", true);
			if(searchRoot != null)
			{
				archCommand.addParam(searchRoot);
			}
			archCommand.execute();
			ArchResponse response = archCommand.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"COLSEARCH\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(", service message: ")
					.append(response.getServiceMessage())
					.append(", error message: ")
					.append(response.getErrorMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ColSearchResult result = response.getColSearchResult();
			// colnames are contained in the resulting string array
			return result.getResultStrings();
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}

	/**
	 * @deprecated Use {@link #collectionSearch4OriginAndName(String, String, String, String)} instead
	 */
	public ResourceData[] collectionSearch4Origin_FullInfo(String originPropValue, String userName) throws ArchConnException
	{
		return collectionSearch4Origin_FullInfo(originPropValue, null, userName);
	}

	/**
	 * Get the full meta data of all collections having their "origin" property set to the given value.
	 * @param originPropValue Value of the "origin" property. Must neither be <code>null</code> nor an empty string.
	 * @param searchRoot URI of the root collection below of which the search is to be started. May be <code>null</code>. 
	 * @param userName Name of the user performing this action
	 * @return List of the names of all matching collections. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred.
	 */
	public ResourceData[] collectionSearch4Origin_FullInfo(String originPropValue, URI searchRoot, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand archCommand = session.createCommand(ArchCommand.AC_COLSEARCH);
			ColSearchClause clause = new ColSearchClause();
			clause.addQueryPart("origin", originPropValue, null, null);
			archCommand.addParam(clause);
			if(searchRoot != null)
			{
				archCommand.addParam(searchRoot);
			}
			archCommand.execute();
			ArchResponse response = archCommand.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuffer("XML DAS command \"COLSEARCH\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(", service message: ")
					.append(response.getServiceMessage())
					.append(", error message: ")
					.append(response.getErrorMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ColSearchResult result = response.getColSearchResult();
			// collection meta data is contained in the resulting string array
			return result.getResultResources();
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}

	/**
	 * @deprecated Use {@link #collectionSearch4OriginAndName(String, String, String, URI, String)} instead
	 */
	public ResourceData[] collectionSearch4OriginAndName(String originPropValue, String operator, String collName, String userName) throws ArchConnException
	{
		return collectionSearch4OriginAndName(originPropValue, operator, collName, null, userName);
	}

	/**
	 * Get all collections having the same (part of the) name and having their "origin" property set to the given value.
	 * @param originPropValue Value of the "origin" property. Must neither be <code>null</code> nor an empty string.
	 * @param operator SQL comparison operator that will be applied to the <code>collName</code> parameter. May be <code>null</code> if <code>collName</code> is <code>null</code>, too.
	 * @param collName (Part of the) name of the collections to search for. May be <code>null</code> if <code>operator</code> is <code>null</code>, too.
	 * @param searchRoot URI of the root collection below of which the search is to be started. May be <code>null</code>. 
	 * @param userName Name of the user performing this action
	 * @return List of all matching collections. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred.
	 */
	public ResourceData[] collectionSearch4OriginAndName(String originPropValue, String operator, String collName, URI searchRoot, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand archCommand = session.createCommand(ArchCommand.AC_COLSEARCH);
			ColSearchClause clause = new ColSearchClause();
			clause.addQueryPart("origin", originPropValue, operator, collName);
			archCommand.addParam(clause);
			if(searchRoot != null)
			{
				archCommand.addParam(searchRoot);
			}
			archCommand.execute();
			ArchResponse response = archCommand.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"COLSEARCH\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(", service message: ")
					.append(response.getServiceMessage())
					.append(", error message: ")
					.append(response.getErrorMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ColSearchResult result = response.getColSearchResult();
			// collection metadata are contained in the resulting ResourceData array
			return result.getResultResources();
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}

	/**
	 * @deprecated Use {@link #collectionSearch4OriginAndNameRange(String, String, String, URI, String)} instead
	 */
	public ResourceData[] collectionSearch4OriginAndNameRange(String originPropValue, String lowerCollName, String upperCollName, String userName) throws ArchConnException
	{
		return collectionSearch4OriginAndNameRange(originPropValue, lowerCollName, upperCollName, null, userName);
	}

	/**
	 * Get all collections having a name out of a certain alphabetical range and having their "origin" property set to the given value.
	 * @param originPropValue Value of the "origin" property. Must neither be <code>null</code> nor an empty string.
	 * @param lowerCollName Lower border of the range of collection names. Must neither be <code>null</code> nor an empty string.
	 * @param upperCollName Upper border of the range of collection names. Must neither be <code>null</code> nor an empty string.
	 * @param searchRoot URI of the root collection below of which the search is to be started. May be <code>null</code>. 
	 * @param userName Name of the user performing this action
	 * @return List of all matching collections. The list may be empty but is never <code>null</code>.
	 * @throws ArchConnException Thrown if any problem occurred.
	 */
	public ResourceData[] collectionSearch4OriginAndNameRange(String originPropValue, String lowerCollName, String upperCollName, URI searchRoot, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand archCommand = session.createCommand(ArchCommand.AC_COLSEARCH);
			ColSearchClause clause = new ColSearchClause();
			clause.addQueryPart("origin", originPropValue, ">=", lowerCollName);
			clause.addQueryPart("origin", originPropValue, "<=", upperCollName);
			archCommand.addParam(clause);
			if(searchRoot != null)
			{
				archCommand.addParam(searchRoot);
			}
			archCommand.execute();
			ArchResponse response = archCommand.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"COLSEARCH\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(", service message: ")
					.append(response.getServiceMessage())
					.append(", error message: ")
					.append(response.getErrorMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ColSearchResult result = response.getColSearchResult();
			// collection metadata are contained in the resulting ResourceData array
			return result.getResultResources();
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "COLSEARCH command failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}

	/**
	 * Get the DAV and ILM conformance data of the given archive store.
	 * @param archiveStoreName Name of the archive store
	 * @param userName Name of the user performing this action
	 * @return The DAV and ILM conformance data of the given archive store
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public WebDavStoreConformanceData getWebDAVStoreConformanceData(String archiveStoreName, String userName) throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand getASMetaDataCmd = session.createCommand("get_webdav_store_meta_data");
			getASMetaDataCmd.addParam("archive_store", archiveStoreName);
			getASMetaDataCmd.execute();
			ArchResponse response = getASMetaDataCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuffer("XML DAS command \"GET_WEBDAV_STORE_META_DATA\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			String davConformance = response.getHeaderField("dav-conformance");
			short ilmConformance = Short.parseShort(response.getHeaderField("ilm-conformance"));
			short ilmAlConformance = Short.parseShort(response.getHeaderField("ilm-al-conformance"));
			return new WebDavStoreConformanceData(davConformance, ilmConformance, ilmAlConformance);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the WebDAV store conformance data failed", e);
			throw new ArchConnException("io_problem");
		}
		catch(Exception e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Getting the WebDAV store conformance data failed", e);
			throw new ArchConnException(e.getMessage());
		}
		finally
		{
			if(session != null)
			{
				// there is no difference between "cancel()" or "close()" for an unqualified session
				session.close();
			}
		}
	}

	private void releaseResponseAsynchronously(final ArchCommand listCmd)
	{
		final ArchSession hierarchyQuerySession = ((AbstractArchCommand)listCmd).getArchSessionRef(this);
		Thread releaseThread = new Thread() 
		{
			public void run() 
			{
				// close response stream
				((ListCommand)listCmd).closeResponseStream();
				try
				{
					// close archiving session
					hierarchyQuerySession.close();
				}
				catch(SessionHandlingException e)
				{
					cat.logThrowableT(Severity.ERROR, loc, "Releasing the hierarchy query failed", e);
				}
			}
		};
		releaseThread.start();
	}

	/**
	 * Tests if the given archive store is up and running.
	 * @param archiveStoreName Name of the archive store
	 * @param userName Name of the user executing this configuration action
	 * @return The metadata of the archive store including the test result
	 * @throws ArchConnException Thrown if any problem occurred
	 */
	public ArchiveStoreData testArchiveStore(String archiveStoreName, String userName) 
	throws ArchConnException
	{
		ArchSession session = null;
		try
		{
			session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
			ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
			defineASCmd.addParam("action", "T");
			defineASCmd.addParam("archive_store", archiveStoreName);
			defineASCmd.execute();
			ArchResponse response = defineASCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuffer("XML DAS command \"DEFINEARCHIVESTORES\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			ArrayList<? extends Serializable> archStoreList = response.getArchAdminData();
			if(archStoreList.size() != 1)
			{
				String errMsg = new StringBuilder("Found unexpected amount of archive stores (expected 1, but found ")
					.append(archStoreList.size()).append(")").toString();
				cat.errorT(loc, errMsg);
				throw new ArchConnException(errMsg);
			}
			return (ArchiveStoreData)archStoreList.get(0);
		}
		catch(ArchConnException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Testing an archive store failed", e);
			throw new ArchConnException("io_problem");
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
				session.close();
			}
		}
	}
}
