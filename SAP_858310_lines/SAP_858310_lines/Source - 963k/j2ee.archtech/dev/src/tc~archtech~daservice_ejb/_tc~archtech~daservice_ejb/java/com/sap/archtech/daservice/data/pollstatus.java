package com.sap.archtech.daservice.data;

import java.sql.Timestamp;

public class PollStatus {

	private Timestamp startdate;
	private Timestamp lastUpdate;
	private int packres;
	private int packedres;
	private String message;

	public PollStatus(Timestamp startdate, Timestamp lastUpdate, int packres,
			int packedres, String message) {
		this.startdate = startdate;
		this.lastUpdate = lastUpdate;
		this.packres = packres;
		this.packedres = packedres;
		this.message = message;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public String getMessage() {
		return message;
	}

	public int getPackedres() {
		return packedres;
	}

	public int getPackres() {
		return packres;
	}

	public Timestamp getStartdate() {
		return startdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPackedres(int packedres) {
		this.packedres = packedres;
	}

	public void setPackres(int packres) {
		this.packres = packres;
	}

	public void setStartdate(Timestamp startdate) {
		this.startdate = startdate;
	}
}
