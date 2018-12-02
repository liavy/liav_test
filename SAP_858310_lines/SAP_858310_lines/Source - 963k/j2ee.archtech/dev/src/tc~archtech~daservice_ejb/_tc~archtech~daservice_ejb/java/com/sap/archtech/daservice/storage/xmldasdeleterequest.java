package com.sap.archtech.daservice.storage;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasDeleteRequest extends XmlDasMasterRequest {
	
	private String uri;
	private String range;
	private HashSet<Long> notDeletedColIds;
	private HashSet<Long> notDeletedResIds;
	private HashMap<Long, HashSet<Long>> notDeletedColStoreIds;
	private Connection connection;

	public XmlDasDeleteRequest() {
		super();
	}

	public XmlDasDeleteRequest(Sapxmla_Config sac, String uri, String range) {
		super(sac);
		this.uri = uri.toLowerCase();
		this.range = range;
	}

	public XmlDasDeleteRequest(Sapxmla_Config sac, String uri, String range,
			HashSet<Long> notDeletedColIds, HashSet<Long> notDeletedResIds,
			HashMap<Long, HashSet<Long>> notDeletedColStoreIds,
			Connection connection) {
		super(sac);
		this.uri = uri.toLowerCase();
		this.range = range;
		this.notDeletedColIds = notDeletedColIds;
		this.notDeletedResIds = notDeletedResIds;
		this.notDeletedColStoreIds = notDeletedColStoreIds;
		this.connection = connection;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}

	public HashSet<Long> getNotDeletedColIds() {
		return notDeletedColIds;
	}

	public void setNotDeletedColIds(HashSet<Long> notDeletedColIds) {
		this.notDeletedColIds = notDeletedColIds;
	}

	public HashSet<Long> getNotDeletedResIds() {
		return notDeletedResIds;
	}

	public void setNotDeletedResIds(HashSet<Long> notDeletedResIds) {
		this.notDeletedResIds = notDeletedResIds;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public HashMap<Long, HashSet<Long>> getNotDeletedColStoreIds() {
		return notDeletedColStoreIds;
	}

	public void setNotDeletedColStoreIds(
			HashMap<Long, HashSet<Long>> notDeletedColStoreIds) {
		this.notDeletedColStoreIds = notDeletedColStoreIds;
	}
}
