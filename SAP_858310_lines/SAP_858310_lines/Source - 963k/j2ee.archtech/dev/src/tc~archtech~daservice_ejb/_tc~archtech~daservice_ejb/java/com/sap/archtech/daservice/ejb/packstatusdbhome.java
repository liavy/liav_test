package com.sap.archtech.daservice.ejb;

import javax.ejb.EJBHome;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.sql.Timestamp;

public interface PackStatusDBHome extends EJBHome {

	public PackStatusDB create() throws CreateException, RemoteException;

	public PackStatusDB createMethod(long colid, Timestamp starttime,
			Timestamp lastupdate, int packres, int packedres, String message)
			throws CreateException, RemoteException;

	public PackStatusDB findByPrimaryKey(Long primKey) throws FinderException,
			RemoteException;
}
