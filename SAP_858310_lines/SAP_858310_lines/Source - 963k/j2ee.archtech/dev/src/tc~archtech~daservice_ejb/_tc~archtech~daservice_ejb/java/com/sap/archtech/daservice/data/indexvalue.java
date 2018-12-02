package com.sap.archtech.daservice.data;

public class IndexValue {
	private String type;
	private String value;

	public IndexValue(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
