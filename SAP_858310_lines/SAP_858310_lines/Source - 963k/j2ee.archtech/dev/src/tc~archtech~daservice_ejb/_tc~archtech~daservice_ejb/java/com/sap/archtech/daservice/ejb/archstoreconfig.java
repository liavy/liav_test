package com.sap.archtech.daservice.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public interface ArchStoreConfig extends EJBObject
{

  public String getArchivestore() throws RemoteException;

  public void setArchivestore(String archivestore) throws RemoteException;

  public String getStoragesystem() throws RemoteException;

  public void setStoragesystem(String storagesystem) throws RemoteException;

  public String getStoretype() throws RemoteException;

  public void setStoretype(String storetype) throws RemoteException;

  public String getWinroot() throws RemoteException;

  public void setWinroot(String winroot) throws RemoteException;

  public String getUnixroot() throws RemoteException;

  public void setUnixroot(String unixroot) throws RemoteException;

  public String getProxyhost() throws RemoteException;

  public void setDestination(String destination) throws RemoteException;

  public String getDestination() throws RemoteException;

  public void setProxyhost(String proxyhost) throws RemoteException;

  public int getProxyport() throws RemoteException;

  public void setProxyport(int proxyport) throws RemoteException;

  public short getIlmconform() throws RemoteException;

  public void setIlmconform(short ilmconform) throws RemoteException;
  
  public String getIsdefault() throws RemoteException;

  public void setIsdefault(String isdefault) throws RemoteException;

  public Sapxmla_Config getSapxmla_Config() throws RemoteException;
}
