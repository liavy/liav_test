package com.sap.archtech.daservice.data;

public class TechnicalIndexData {
	private String tablename;
	private long indexId;

	public TechnicalIndexData(String tablename, long indexId) {
		this.tablename = tablename;
		this.indexId = indexId;
	}

	public long getIndexId() {
		return indexId;
	}

	public String getTablename() {
		return tablename;
	}
}
