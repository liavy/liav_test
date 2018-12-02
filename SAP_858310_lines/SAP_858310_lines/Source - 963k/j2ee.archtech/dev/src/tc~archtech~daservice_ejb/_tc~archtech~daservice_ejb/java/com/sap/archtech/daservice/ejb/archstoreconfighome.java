package com.sap.archtech.daservice.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface ArchStoreConfigHome extends EJBHome {

	public ArchStoreConfig create() throws CreateException, RemoteException;

	public ArchStoreConfig create(Long storeid, String archivestore,
			String storagesystem, String storetype, String winroot,
			String unixroot, String destination, String proxyhost,
			int proxyport, short ilmconform, String isdefault)
			throws CreateException, RemoteException;

	public ArchStoreConfig findByPrimaryKey(Long primKey)
			throws FinderException, RemoteException;

	public Collection<ArchStoreConfigHome> findAll() throws FinderException,
			RemoteException;

	public Collection<ArchStoreConfigHome> findByArchiveStore(
			String archivestore) throws FinderException, RemoteException;
}
