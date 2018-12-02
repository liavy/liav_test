package com.sap.archtech.daservice.data;

public class CollectionData {

	private long colId;
	private String colURI;
	private java.sql.Timestamp cDate;
	private String cUser;
	private String frozen;
	private long parentColId;
	private long storeId;
	private long nextStoreId;
	private String colType;

	public CollectionData(long colId, String colURI, String cUser,
			java.sql.Timestamp cDate, String colType, String frozen) {
		this.colId = colId;
		this.colURI = colURI;
		this.cUser = cUser;
		this.cDate = cDate;
		this.colType = colType;
		this.frozen = frozen;
	}

	public CollectionData(long colId, long storeId) {
		this.colId = colId;
		this.storeId = storeId;
	}

	public CollectionData(long colId, long parentColId, long storeId,
			long nextStoreId) {
		this.colId = colId;
		this.parentColId = parentColId;
		this.storeId = storeId;
		this.nextStoreId = nextStoreId;
	}

	public CollectionData(long colId, String colURI) {
		this.colId = colId;
		this.colURI = colURI;
	}

	public CollectionData(long colId, String colURI, long storeId) {
		this.colId = colId;
		this.colURI = colURI;
		this.storeId = storeId;
	}

	public long getcolId() {
		return this.colId;
	}

	public void setcolId(long colId) {
		this.colId = colId;
	}

	public String getColURI() {
		return colURI;
	}

	public void setColURI(String colURI) {
		this.colURI = colURI;
	}

	public java.sql.Timestamp getCDate() {
		return cDate;
	}

	public void setCDate(java.sql.Timestamp cDate) {
		this.cDate = cDate;
	}

	public String getCUser() {
		return cUser;
	}

	public void setCUser(String cUser) {
		this.cUser = cUser;
	}

	public String getFrozen() {
		return frozen;
	}

	public void setFrozen(String frozen) {
		this.frozen = frozen;
	}

	public long getParentColId() {
		return parentColId;
	}

	public void setParentColId(long l) {
		parentColId = l;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public long getNextStoreId() {
		return nextStoreId;
	}

	public void setNextStoreId(long l) {
		nextStoreId = l;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	public String getColType() {
		return colType;
	}

}
