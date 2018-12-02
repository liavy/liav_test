package com.sap.archtech.daservice.ejb;

import javax.ejb.EJBLocalObject;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public interface ArchStoreConfigLocal extends EJBLocalObject {

	public String getArchivestore();

	public void setArchivestore(String archivestore);

	public String getStoragesystem();

	public void setStoragesystem(String storagesystem);

	public String getStoretype();

	public void setStoretype(String storetype);

	public String getWinroot();

	public void setWinroot(String winroot);

	public String getUnixroot();

	public void setUnixroot(String unixroot);

	public String getDestination();

	public void setDestination(String destination);

	public String getProxyhost();

	public void setProxyhost(String proxyhost);

	public int getProxyport();

	public void setProxyport(int proxyport);

	public short getIlmconform();

	public void setIlmconform(short ilmconform);

	public String getIsdefault();

	public void setIsdefault(String isdefault);

	public Sapxmla_Config getSapxmla_Config();
}
