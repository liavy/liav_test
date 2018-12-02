package com.sap.archtech.daservice.mbeans;

/*
 The server hosting an XML Data Archiving Service that is to be used by one or more application systems to carry out XML-based archiving. For brevity and convenience, an XML Data Archiving Server is also referred to as XML DAS.
 @version  1.0
 */

public interface SAP_ITSAMXMLDataArchivingServer {

	public String getOverviewName();

	public void setOverviewName(String OverviewName);

	public String getOverviewRelease();

	public void setOverviewRelease(String OverviewRelease);

	public boolean getOverviewIsRunning();

	public void setOverviewIsRunning(boolean OverviewIsRunning);

	public boolean getOverviewIsRegistered();

	public void setOverviewIsRegistered(boolean OverviewIsRegistered);

	public String getOverviewURL();

	public void setOverviewURL(String OverviewURL);

	public String getOverviewNamespace();

	public void setOverviewNamespace(String OverviewNamespace);

	public com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDAS_ArchiveStoreData[] DefineArchiveStores(
			String action,
			String user,
			com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDAS_ArchiveStoreData input_value);

	public String SynchronizeHomePaths(String action, String user,
			String home_path, String context, String archive_store);

	public com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDAS_ArchivePathData[] AssignArchiveStores(
			String action, String user, String archive_path,
			String archive_store);

	public com.sap.archtech.daservice.mbeans.SAP_ITSAMXMLDAS_ArchivePathData[] ListArchivePaths(
			String type, String user, String archive_path);

	public String Pack(String archive_path, String user);

	public String Unpack(String archive_path, String user);

	public String PackStatus(String archive_path);

	public String[] GetLocalDestinations();

	public void GetOverViewInfos();
}