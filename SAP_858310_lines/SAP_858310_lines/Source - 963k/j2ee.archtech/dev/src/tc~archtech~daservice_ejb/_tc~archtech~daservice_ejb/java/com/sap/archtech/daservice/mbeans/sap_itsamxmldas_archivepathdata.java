package com.sap.archtech.daservice.mbeans;

import java.util.Date;

/* 
 ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema. 
 @version 1.0	
 */

public class SAP_ITSAMXMLDAS_ArchivePathData {

	private String ArchivePath = null;

	private String CollectionType = null;

	private Date CreationTime = null;

	private String CreationUser = null;

	private String ArchiveStore = null;

	private String StorageSystem = null;

	private boolean IsFrozen = false;

	private boolean HasChildren = false;

	private long ColId = 0;

	public SAP_ITSAMXMLDAS_ArchivePathData() {

	}

	/*
	 * 
	 * @return String
	 */
	public String getArchivePath() {
		return this.ArchivePath;
	}

	/*
	 * @param String
	 */
	public void setArchivePath(String ArchivePath) {
		this.ArchivePath = ArchivePath;
	}

	/*
	 * 
	 * @return String
	 */
	public String getCollectionType() {
		return this.CollectionType;
	}

	/*
	 * @param String
	 */
	public void setCollectionType(String CollectionType) {
		this.CollectionType = CollectionType;
	}

	/*
	 * 
	 * @return Date
	 */
	public Date getCreationTime() {
		return this.CreationTime;
	}

	/*
	 * @param Date
	 */
	public void setCreationTime(Date CreationTime) {
		this.CreationTime = CreationTime;
	}

	/*
	 * 
	 * @return String
	 */
	public String getCreationUser() {
		return this.CreationUser;
	}

	/*
	 * @param String
	 */
	public void setCreationUser(String CreationUser) {
		this.CreationUser = CreationUser;
	}

	/*
	 * 
	 * @return String
	 */
	public String getArchiveStore() {
		return this.ArchiveStore;
	}

	/*
	 * @param String
	 */
	public void setArchiveStore(String ArchiveStore) {
		this.ArchiveStore = ArchiveStore;
	}

	/*
	 * 
	 * @return String
	 */
	public String getStorageSystem() {
		return this.StorageSystem;
	}

	/*
	 * @param String
	 */
	public void setStorageSystem(String StorageSystem) {
		this.StorageSystem = StorageSystem;
	}

	/*
	 * 
	 * @return boolean
	 */
	public boolean getIsFrozen() {
		return this.IsFrozen;
	}

	/*
	 * @param boolean
	 */
	public void setIsFrozen(boolean IsFrozen) {
		this.IsFrozen = IsFrozen;
	}

	/*
	 * 
	 * @return boolean
	 */
	public boolean getHasChildren() {
		return this.HasChildren;
	}

	/*
	 * @param boolean
	 */
	public void setHasChildren(boolean HasChildren) {
		this.HasChildren = HasChildren;
	}

	/*
	 * 
	 * @return long
	 */
	public long getColId() {
		return this.ColId;
	}

	/*
	 * @param long
	 */
	public void setColId(long ColId) {
		this.ColId = ColId;
	}

}
