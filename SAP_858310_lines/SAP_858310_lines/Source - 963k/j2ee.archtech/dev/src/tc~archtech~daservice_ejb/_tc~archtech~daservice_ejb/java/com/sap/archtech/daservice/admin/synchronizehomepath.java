package com.sap.archtech.daservice.admin;

import java.net.*;

public class SynchronizeHomePath {

	private URL url;
	private String authorization;
	private String homePath;
	private String action;
	private String user;
	private String context;
	private String archiveStore;

	public SynchronizeHomePath(URL url, String authorization, String homePath,
			String action, String user, String context, String archiveStore) {
		this.url = url;
		this.authorization = authorization;
		this.homePath = homePath;
		this.action = action;
		this.user = user;
		this.context = context;
		this.archiveStore = archiveStore;
	}

	public String execute() throws Exception {

		// Open HTTP Connection
		HttpURLConnection connection = (HttpURLConnection) this.url
				.openConnection();

		// Specify Request Method
		connection.setRequestMethod("POST");

		// Set Request Headers
		connection
				.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		connection.setRequestProperty("Content-Length", "0");
		connection.setRequestProperty("Authorization", authorization);
		connection.setRequestProperty("method", "_SYNC_HOME_PATH");
		if (this.homePath != null)
			connection.setRequestProperty("home_path", this.homePath);
		if (this.action != null)
			connection.setRequestProperty("action", this.action);
		if (this.user != null)
			connection.setRequestProperty("user", this.user);
		if (this.context != null)
			connection.setRequestProperty("context", this.context);
		if (this.archiveStore != null)
			connection.setRequestProperty("archive_store", this.archiveStore);

		// Send Request
		connection.connect();
		int responsecode = connection.getResponseCode();

		// Close HTTP Connection
		connection.disconnect();

		// Return Response
		return responsecode + " "
				+ connection.getHeaderField("service_message");
	}
}
