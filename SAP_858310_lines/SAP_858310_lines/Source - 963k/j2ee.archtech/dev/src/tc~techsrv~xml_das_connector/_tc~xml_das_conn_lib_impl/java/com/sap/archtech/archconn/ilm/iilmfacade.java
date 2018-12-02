package com.sap.archtech.archconn.ilm;

import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.exceptions.ArchConnException;

/** 
 * The <code>IIlmFacade</code> interface is the main entry into the ILM functions provided by the XML DAS Archive API for Java.
 */
public interface IIlmFacade 
{
	/**
	 * Checks if the an archive store of the given name has been defined in the XMLDAS at the "DASdefault" destination.
	 * Furthermore, the given archive store must be compliant to ILM conformance class 2.
	 * @param archiveStoreName Name of the archive store
	 * @param userName Name of the user executing this action
	 * @return <code>true</code> if the given archive store has been defined and is ILM-compliant
	 * @throws ArchConnException Thrown if testing the given archive store failed
	 */
	public boolean isArchiveStoreAppropriate(String archiveStoreName, String userName) throws ArchConnException;

	/**
	 * Perform the home path synchronization for the archiving set of the given name.
	 * The XMLDAS at the "DASdefault" destination is used.
	 * @param archivingSetName Name of the archiving set
	 * @param userName Name of the user executing this action
	 * @throws ArchConnException Thrown if the home path synchronization failed
	 */
	public void syncHomePath(String archivingSetName, String userName) throws ArchConnException;

	/**
	 * Create collections for the given org routing and one time routing collection. The collections will be created
	 * below the home path collection, i.e. the home path synchronization must have taken place before for the given 
	 * archiving set.
	 * @param archivingSetName Name of the archiving set
	 * @param userName Name of the user executing this action
	 * @param routingParams Contains the routing information required to create the org routing  and time routing collections
	 * @return The path extension (without home collection part) of the collections created
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public String createHierarchySchemaCollections(String archivingSetName, String userName, CollectionRoutingParams routingParams)
	throws ArchConnException;

	/**
	 * Performs the following actions:
	 * <li>
	 * <ul>Create an archiving write session.</ul>
	 * <ul>Assign the given archive store to the session collection created.</ul>
	 * <ul>Set the "start_of_retention" and "expiration_date" ILM properties for the given path extension.</ul>
	 * <ul>Set the "expiration_date" ILM property for the session collection created.</ul>
	 * </li>
	 * @param archivingSetName Name of the archiving set
	 * @param userName Name of the user executing this action
	 * @param writeSessionParams Contains all information required to create a write session collection
	 * @return The write session created
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public ArchSession createWriteSession(String archivingSetName, String userName, WriteSessionParams writeSessionParams) throws ArchConnException;

	/**
	 * Assign the archive store of the given name to the given archive path (e.g. the session collection)
	 * @param archiveStoreName Name of the archive store
	 * @param userName Name of the user executing this action
	 * @param archivePath Archive path denoting the collection the archive store is to be assigned to
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public void assignArchiveStore(String archiveStoreName, String userName, String archivePath) throws ArchConnException;

	/**
	 * Remove the archive store assignment for the given archive path (e.g. the session collection)
	 * @param userName Name of the user executing this action
	 * @param archivePath Archive path denoting the collection the archive store is to be assigned to
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public void unassignArchiveStore(String userName, String archivePath) throws ArchConnException;

	/**
	 * Writes a given resource to the archive using a given write session.
	 * Set the "expiration_date" ILM property on that resource.
	 * @param archivingSetName Name of the archiving set
	 * @param userName Name of the user executing this action
	 * @param write2ArchiveParams Contains all information required to write a single resource to the archive
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public void writeToArchive(String archivingSetName, String userName, Write2ArchiveParams write2ArchiveParams)	throws ArchConnException;
	
	/**
	 * Close the given archiving session. Call this method to release the connection to the XMLDAS.
	 * @param archSession The archiving session used before
	 * @param toBeCanceled Pass <code>true</code> if you want to cancel the session (e.g. if a previous write step failed)
	 */
	public void closeArchivingSession(ArchSession archSession, boolean toBeCanceled);
	
	/**
	 * Confirm that all resources archived below the given collection have been deleted from the
	 * local application database. Marks those resources as "deleted in database".
	 * @param archivingSetName Name of the archiving set
	 * @param userName Name of the user executing this action
	 * @param incompleteArchSession Archiving session that has been used before to perform the archiving step 
	 * @throws ArchConnException Thrown if the involved XMLDAS commands were not successful
	 */
	public void confirmResourceDeletion(String archivingSetName, String userName, ArchSession incompleteArchSession) throws ArchConnException;
}
