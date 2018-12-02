package com.sap.archtech.daservice.beanfacade;

import java.io.IOException;
import java.sql.SQLException;

import javax.ejb.EJBLocalObject;
import javax.naming.NamingException;

import com.sap.archtech.archconn.values.ArchiveStoreData;

public interface CommandBeanLocal extends EJBLocalObject {
	/**
	 * Check the connection to XMLDAS. See <i>INFO</i> command.
	 * 
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse checkServiceConnection() throws IOException,
			SQLException;

	/**
	 * Get the list of archive stores known to XMLDAS. See
	 * <i>DEFINE_ARCHIVE_STORES</i> command.
	 * 
	 * @return XMLDAS command response containing a list of
	 *         <code>ArchiveStoreData</code> objects each representing the
	 *         properties of one archive store.
	 */
	public ObjectListResponse getArchiveStores() throws IOException,
			SQLException;

	/**
	 * @deprecated Use {@link #defineArchStore(String)}
	 */
	public ServiceResponse defineArchiveStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException;

	/**
	 * Define an archive store. See <i>DEFINE_ARCHIVE_STORES</i> command.
	 * 
	 * @param archiveStore
	 *            Contains the properties of the archive store to be defined.
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ObjectListResponse defineArchStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException;

	/**
	 * @deprecated Use {@link #updateArchStore(ArchiveStoreData)}
	 */
	public ServiceResponse updateArchiveStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException;

	/**
	 * Update the properties of a previously defined archive store. See
	 * <i>DEFINE_ARCHIVE_STORES</i> command.
	 * 
	 * @param archiveStore
	 *            Contains the (updated) properties of a previously defined
	 *            archive store.
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ObjectListResponse updateArchStore(ArchiveStoreData archiveStore)
			throws IOException, SQLException;

	/**
	 * Delete the definition of an archive store. See <i>DEFINE_ARCHIVE_STORES</i>
	 * command.
	 * 
	 * @param archStoreName
	 *            Identifies the archive store
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse deleteArchiveStore(String archStoreName)
			throws IOException, SQLException;

	/**
	 * Test the archive store. See <i>DEFINE_ARCHIVE_STORES</i> command.
	 * 
	 * @param archStoreName
	 *            Identifies the archive store
	 * @return XMLDAS command response containing a list with the
	 *         <code>ArchiveStoreData</code> object representing the
	 *         properties of the archive store under test.
	 */
	public ObjectListResponse testArchiveStore(String archStoreName)
			throws IOException, SQLException;

	/**
	 * Get the list of archive paths known to XMLDAS. See
	 * <i>ASSIGN_ARCHIVE_STORES</i> command.
	 * 
	 * @return XMLDAS command response containing a list of
	 *         <code>ArchivePathData</code> objects each representing the
	 *         properties of one archive path.
	 */
	public ObjectListResponse getArchivePaths() throws IOException,
			SQLException, NamingException;

	/**
	 * Assign an archive store to an archive path. See <i>ASSIGN_ARCHIVE_STORES</i>
	 * command.
	 * 
	 * @param archStoreName
	 *            Identifies the archive store.
	 * @param archivePath
	 *            Identifies the archive path.
	 * @return XMLDAS command response containing a list of
	 *         <code>ArchivePathData</code> objects each representing the
	 *         properties of one archive path.
	 */
	public ObjectListResponse assignArchiveStore(String archStoreName,
			String archivePath) throws IOException, SQLException,
			NamingException;

	/**
	 * Remove the assignment of an archive path. See <i>ASSIGN_ARCHIVE_STORES</i>
	 * command.
	 * 
	 * @param archivePath
	 *            Identifies the archive path.
	 * @return XMLDAS command response containing a list of
	 *         <code>ArchivePathData</code> objects each representing the
	 *         properties of one archive path.
	 */
	public ObjectListResponse unassignArchiveStore(String archivePath)
			throws IOException, SQLException, NamingException;

	/**
	 * Get the list of archive paths according to the specified search criteria.
	 * See <i>LIST_ARCHIVE_PATHS</i> command.
	 * 
	 * @param archivePathType
	 *            The type of archive paths to be listed (home collection or
	 *            non).
	 * @param rootArchivePath
	 *            Identifies the archive path where the search is started
	 *            (ignored for home collection search).
	 * @return XMLDAS command response containing a list of
	 *         <code>ArchivePathData</code> objects each representing the
	 *         properties of one archive path.
	 */
	public ObjectListResponse getArchivePaths(String archivePathType,
			String rootArchivePath) throws IOException, SQLException;

	/**
	 * Pack the resources stored under a given archive path. See <i>PACK</i>
	 * command.
	 * 
	 * @param archivePath
	 *            Identifies the archive path.
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse packResources(String archivePath)
			throws IOException, SQLException;

	/**
	 * Unpack previously packed resources. See <i>UNPACK</i> command.
	 * 
	 * @param archivePath
	 *            Identifies the archive path.
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse unpackResources(String archivePath)
			throws IOException, SQLException;

	/**
	 * Get the status of the <i>PACK</i> command execution.
	 * 
	 * @param archivePath
	 *            Identifies the archive path.
	 * @return XMLDAS command response containing the status of the <i>PACK</i>
	 *         command execution.
	 */
	public PackStatusResponse getPackStatus(String archivePath)
			throws IOException, SQLException;

	/**
	 * Define Home Path. See <i>SYNCHOMEPATH</i> command.
	 * 
	 * @param homePath
	 *            The Home Path of an archiving set.
	 * @param archStoreName
	 *            The archive store to be assigned to the home path (optional
	 *            parameter).
	 * @param context
	 *            (??? taken over from existing SYNCHOMEPATH API)
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse insertHomepath(String homePath,
			String archStoreName, String context) throws IOException,
			SQLException, NamingException;

	/**
	 * Delete a previously defined Home Path. See <i>SYNCHOMEPATH</i> command.
	 * 
	 * @param homePath
	 *            The Home Path to be deleted.
	 * @param context
	 *            (??? taken over from existing SYNCHOMEPATH API)
	 * @return XMLDAS command response containing the status code, the service
	 *         message, and the protocol message.
	 */
	public ServiceResponse deleteHomepath(String homePath, String context)
			throws IOException, SQLException, NamingException;
}
