package com.sap.archtech.daservice.ejb;

import java.sql.Timestamp;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import com.sap.archtech.daservice.data.PollStatus;

public abstract class PackStatusDBBean implements EntityBean {

	public void ejbLoad() {
	}

	public void ejbStore() {
	}

	public void ejbRemove() throws RemoveException {
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void setEntityContext(EntityContext context) {
		myContext = context;
	}

	public void unsetEntityContext() {
		myContext = null;
	}

	private transient EntityContext myContext;

	/**
	 * @primKeyField
	 */
	public abstract Long getColid();

	public abstract void setColid(Long colid);

	public abstract Integer getPackedres();

	public abstract void setPackedres(Integer packedres);

	public abstract Timestamp getLastupdate();

	public abstract void setLastupdate(Timestamp lastupdate);

	public abstract Integer getPackres();

	public abstract void setPackres(Integer packres);

	public abstract String getMessage();

	public abstract void setMessage(String message);

	public abstract Timestamp getStarttime();

	public abstract void setStarttime(Timestamp starttime);

	/**
	 * Create Method.
	 */
	public Long ejbCreate() throws CreateException {
		// Not used
		return null;
	}

	/**
	 * Post Create Method.
	 */
	public void ejbPostCreate() {
		// Not used
	}

	/**
	 * Create Method.
	 */
	public Long ejbCreateMethod(long colid, Timestamp starttime,
			Timestamp lastupdate, int packres, int packedres, String message)
			throws CreateException {
		Long id = Long.valueOf(colid);
		setColid(id);
		setStarttime(starttime);
		setLastupdate(lastupdate);
		setPackres(Integer.valueOf(packres));
		setPackedres(Integer.valueOf(packedres));
		setMessage(message);

		return id;
	}

	/**
	 * Post Create Method.
	 */
	public void ejbPostCreateMethod(long colid, Timestamp starttime,
			Timestamp lastupdate, int packres, int packedres, String message) {
		// Nothing to do
	}

	public void updPackStatus(int packedres, Timestamp updtime, String message) {
		setPackedres(Integer.valueOf(packedres));
		setLastupdate(updtime);
		setMessage(message);
	}

	public PollStatus getStatus() {
		PollStatus status;

		status = new PollStatus(this.getStarttime(), this.getLastupdate(), this
				.getPackres().intValue(), this.getPackedres().intValue(), this
				.getMessage());
		return status;

	}
}
