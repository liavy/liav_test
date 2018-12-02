package com.sap.archtech.daservice.ejb;

import java.sql.Timestamp;

import javax.ejb.EJBLocalObject;

import com.sap.archtech.daservice.data.PollStatus;

public interface PackStatusDBLocal extends EJBLocalObject {

	public void setPackedres(Integer packedres);

	public void setLastupdate(Timestamp lastupdate);

	public void setMessage(String message);

	public void updPackStatus(int packedres, Timestamp updtime, String message);

	public Timestamp getLastupdate();

	public String getMessage();

	public Timestamp getStarttime();

	public Integer getPackres();

	public Integer getPackedres();

	public PollStatus getStatus();
}
