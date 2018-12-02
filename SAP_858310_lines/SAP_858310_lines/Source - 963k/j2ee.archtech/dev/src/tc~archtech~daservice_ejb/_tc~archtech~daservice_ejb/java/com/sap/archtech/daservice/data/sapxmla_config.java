package com.sap.archtech.daservice.data;

import java.io.Serializable;

public class Sapxmla_Config implements Serializable {
	
	public static final long serialVersionUID = 1234567890l;
	
	public long store_id;
	public String archive_store;
	public String storage_system;
	public String type;
	public String win_root;
	public String unix_root;
	public String destination;
	public String proxy_host;
	public int proxy_port;
	public short ilm_conformance;
	public String is_default;

	public Sapxmla_Config(String archive_store, String storage_system,
			String type, String win_root, String unix_root, String proxy_host,
			int proxy_port) {
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.type = type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
	}

	public Sapxmla_Config(long store_id, String archive_store,
			String storage_system, String type, String win_root,
			String unix_root, String proxy_host, int proxy_port) {
		this.store_id = store_id;
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.type = type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
	}

	public Sapxmla_Config(long store_id, String archive_store,
			String storage_system, String type, String win_root,
			String unix_root, String destination, String proxy_host,
			int proxy_port) {
		this.store_id = store_id;
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.type = type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.destination = destination;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
	}

	public Sapxmla_Config(String archive_store, String storage_system,
			String type, String win_root, String unix_root, String destination,
			String proxy_host, int proxy_port) {
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.type = type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.destination = destination;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
	}

	public Sapxmla_Config(long store_id, String archive_store,
			String storage_system, String type, String win_root,
			String unix_root, String destination, String proxy_host,
			int proxy_port, short ilm_conformance, String is_default) {
		this.store_id = store_id;
		this.archive_store = archive_store;
		this.storage_system = storage_system;
		this.type = type;
		this.win_root = win_root;
		this.unix_root = unix_root;
		this.destination = destination;
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
		this.ilm_conformance = ilm_conformance;
		this.is_default = is_default;
	}
}
