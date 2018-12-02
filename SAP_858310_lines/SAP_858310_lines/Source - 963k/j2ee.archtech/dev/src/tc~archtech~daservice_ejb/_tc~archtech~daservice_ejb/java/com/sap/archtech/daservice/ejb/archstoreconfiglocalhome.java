package com.sap.archtech.daservice.ejb;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface ArchStoreConfigLocalHome extends EJBLocalHome {

	public ArchStoreConfigLocal create() throws CreateException;

	public ArchStoreConfigLocal create(Long storeid, String archivestore,
			String storagesystem, String storetype, String winroot,
			String destination, String unixroot, String proxyhost,
			int proxyport, short ilmconform, String isdefault)
			throws CreateException;

	public ArchStoreConfigLocal findByPrimaryKey(Long primKey)
			throws FinderException;

	public Collection<ArchStoreConfigLocal> findAll() throws FinderException;

	public Collection<ArchStoreConfigLocal> findByArchiveStore(
			String archivestore) throws FinderException;
}
