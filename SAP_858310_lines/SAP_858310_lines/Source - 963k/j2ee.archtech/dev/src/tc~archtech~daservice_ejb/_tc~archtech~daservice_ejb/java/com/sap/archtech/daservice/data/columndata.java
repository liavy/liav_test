package com.sap.archtech.daservice.data;

public class ColumnData {

	private String cName;
	private String cType;
	private String jdbcType;
	private int length;

	public ColumnData(String cName, String cType, String jdbcType) {
		this.cName = cName;
		this.cType = cType;
		this.jdbcType = jdbcType;
	}

	public ColumnData(String cName, String cType, String jdbcType, int length) {
		this.cName = cName;
		this.cType = cType;
		this.jdbcType = jdbcType;
		this.length = length;
	}

	public String getCName() {
		return cName;
	}

	public void setCName(String cName) {
		this.cName = cName;
	}

	public void setCType(String cType) {
		this.cType = cType;
	}

	public String getCType() {
		return cType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public int getLength() {
		return length;
	}

}
