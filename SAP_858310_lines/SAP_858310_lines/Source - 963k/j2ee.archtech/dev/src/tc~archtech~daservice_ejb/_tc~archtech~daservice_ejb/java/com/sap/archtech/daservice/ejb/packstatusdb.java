package com.sap.archtech.daservice.ejb;

import java.rmi.RemoteException;
import java.sql.Timestamp;

import javax.ejb.EJBObject;

public interface PackStatusDB extends EJBObject {

	public void setPackedres(Integer packedres) throws RemoteException;

	public void setLastupdate(Timestamp lastupdate) throws RemoteException;

	public void setMessage(String message) throws RemoteException;

	public Timestamp getLastupdate() throws RemoteException;

	public String getMessage() throws RemoteException;

	public Timestamp getStarttime() throws RemoteException;

	public Integer getPackres() throws RemoteException;

	public Integer getPackedres() throws RemoteException;
}
