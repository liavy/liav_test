package com.sap.archtech.daservice.admin;

import java.net.*;

public class PackStatus {

	private URL url;
	private String authorization;
	private String archivepath;

	public PackStatus(URL url, String authorization, String archivepath) {
		this.url = url;
		this.authorization = authorization;
		this.archivepath = archivepath;
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
		connection.setRequestProperty("method", "PACKSTATUS");
		if (this.archivepath != null)
			connection.setRequestProperty("archive_path", this.archivepath);

		// Send Request
		connection.connect();
		int responsecode = connection.getResponseCode();

		// Close HTTP Connection
		connection.disconnect();

		// Return Response
		return responsecode + " "
				+ connection.getHeaderField("service_message") + " "
				+ connection.getHeaderField("packstatus");
	}
}
