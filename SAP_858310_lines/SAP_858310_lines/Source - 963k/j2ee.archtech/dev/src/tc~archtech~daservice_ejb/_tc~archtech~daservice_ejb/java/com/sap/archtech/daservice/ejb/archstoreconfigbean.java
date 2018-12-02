package com.sap.archtech.daservice.ejb;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public abstract class ArchStoreConfigBean implements EntityBean {

	private transient EntityContext myContext;

	public Long ejbCreate() throws CreateException {
		return null;
	}

	public void ejbPostCreate() {
	}

	public Long ejbCreate(Long storeid, String archivestore,
			String storagesystem, String storetype, String winroot,
			String unixroot, String destination, String proxyhost,
			int proxyport, short ilmconform, String isdefault)
			throws CreateException {
		this.setPrimKey(storeid);
		this.setArchivestore(archivestore);
		this.setStoragesystem(storagesystem);
		this.setStoretype(storetype);
		this.setWinroot(winroot);
		this.setUnixroot(unixroot);
		this.setDestination(destination);
		this.setProxyhost(proxyhost);
		this.setProxyport(proxyport);
		this.setIlmconform(ilmconform);
		this.setIsdefault(isdefault);
		return null;
	}

	public void ejbPostCreate(Long storeid, String archivestore,
			String storagesystem, String storetype, String winroot,
			String unixroot, String destination, String proxyhost,
			int proxyport, short ilmconform, String isdefault) {
	}

	// Call Back Methods
	public void setEntityContext(EntityContext context) {
		myContext = context;
	}

	public void unsetEntityContext() {
		myContext = null;
	}

	public void ejbRemove() throws RemoveException {
	}

	public void ejbLoad() {
	}

	public void ejbStore() {
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	// Abstract SETTER/GETTER Methods
	public abstract Long getPrimKey();

	public abstract void setPrimKey(Long primKey);

	public abstract String getArchivestore();

	public abstract void setArchivestore(String archivestore);

	public abstract String getStoragesystem();

	public abstract void setStoragesystem(String storagesystem);

	public abstract String getStoretype();

	public abstract void setStoretype(String storetype);

	public abstract String getWinroot();

	public abstract void setWinroot(String winroot);

	public abstract String getUnixroot();

	public abstract void setUnixroot(String unixroot);

	public abstract String getDestination();

	public abstract void setDestination(String destination);

	public abstract String getProxyhost();

	public abstract void setProxyhost(String proxyhost);

	public abstract int getProxyport();

	public abstract void setProxyport(int proxyport);

	public abstract short getIlmconform();

	public abstract void setIlmconform(short ilmconform);

	public abstract String getIsdefault();

	public abstract void setIsdefault(String isdefault);

	// Business Methods Defined In Component Interface
	public Sapxmla_Config getSapxmla_Config() {
		return new Sapxmla_Config(this.getPrimKey().longValue(), this
				.getArchivestore(), this.getStoragesystem(), this
				.getStoretype(), this.getWinroot(), this.getUnixroot(), this
				.getDestination(), this.getProxyhost(), this.getProxyport(),
				this.getIlmconform(), this.getIsdefault());
	}
}
