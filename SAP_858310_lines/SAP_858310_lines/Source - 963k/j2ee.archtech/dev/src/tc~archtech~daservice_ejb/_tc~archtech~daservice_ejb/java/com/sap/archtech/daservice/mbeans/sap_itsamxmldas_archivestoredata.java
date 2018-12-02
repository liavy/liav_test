package com.sap.archtech.daservice.mbeans;

/*
 ManagedElement is an abstract class that provides a common superclass (or top of the inheritance tree) for the non-association classes in the CIM Schema.
 @version 1.0
 */

public class SAP_ITSAMXMLDAS_ArchiveStoreData {

	private String ArchiveStore = null;

	private String StorageSystem = null;

	private String StoreType = null;

	private String WinRoot = null;

	private String UnixRoot = null;

	private String ProxyHost = null;

	private int ProxyPort = 0;

	private String StoreStatus = null;

	private String Destination = null;

	private short IlmConformance = 0;

	private boolean IsDefault = false;;

	public SAP_ITSAMXMLDAS_ArchiveStoreData() {

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
	 * @return String
	 */
	public String getStoreType() {
		return this.StoreType;
	}

	/*
	 * @param String
	 */
	public void setStoreType(String StoreType) {
		this.StoreType = StoreType;
	}

	/*
	 * 
	 * @return String
	 */
	public String getWinRoot() {
		return this.WinRoot;
	}

	/*
	 * @param String
	 */
	public void setWinRoot(String WinRoot) {
		this.WinRoot = WinRoot;
	}

	/*
	 * 
	 * @return String
	 */
	public String getUnixRoot() {
		return this.UnixRoot;
	}

	/*
	 * @param String
	 */
	public void setUnixRoot(String UnixRoot) {
		this.UnixRoot = UnixRoot;
	}

	/*
	 * 
	 * @return String
	 */
	public String getProxyHost() {
		return this.ProxyHost;
	}

	/*
	 * @param String
	 */
	public void setProxyHost(String ProxyHost) {
		this.ProxyHost = ProxyHost;
	}

	/*
	 * 
	 * @return int
	 */
	public int getProxyPort() {
		return this.ProxyPort;
	}

	/*
	 * @param int
	 */
	public void setProxyPort(int ProxyPort) {
		this.ProxyPort = ProxyPort;
	}

	/*
	 * 
	 * @return String
	 */
	public String getStoreStatus() {
		return this.StoreStatus;
	}

	/*
	 * @param String
	 */
	public void setStoreStatus(String StoreStatus) {
		this.StoreStatus = StoreStatus;
	}

	/*
	 * 
	 * @return String
	 */
	public String getDestination() {
		return this.Destination;
	}

	/*
	 * @param String
	 */
	public void setDestination(String Destination) {
		this.Destination = Destination;
	}

	/*
	 * 
	 * @return short
	 */
	public short getIlmConformance() {
		return this.IlmConformance;
	}

	/*
	 * @param short
	 */
	public void setIlmConformance(short IlmConformance) {
		this.IlmConformance = IlmConformance;
	}

	/*
	 * 
	 * @return boolean
	 */
	public boolean getIsDefault() {
		return this.IsDefault;
	}

	/*
	 * @param boolean
	 */
	public void setIsDefault(boolean IsDefault) {
		this.IsDefault = IsDefault;
	}

}
