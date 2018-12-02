package com.sap.archtech.daservice.data;

import java.io.Serializable;
import java.util.*;

public class IndexData {

	private long id;
	private String name;
	private ArrayList<String> propList;
	private HashMap<String, Serializable> propValuesMap;
	private HashMap<String, String> jdbcMap;

	public IndexData() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getPropList() {
		return propList;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPropList(ArrayList<String> propList) {
		this.propList = propList;
	}

	public HashMap<String, String> getJdbcMap() {
		return jdbcMap;
	}

	public HashMap<String, Serializable> getPropValuesMap() {
		return propValuesMap;
	}

	public void setJdbcMap(HashMap<String, String> map) {
		jdbcMap = map;
	}

	public void setPropValuesMap(HashMap<String, Serializable> map) {
		propValuesMap = map;
	}
}