package com.sap.archtech.daservice.storage;

public class XmlDasMasterResponse {

	private int statusCode;
	private String reasonPhrase;
	private String entityBody;
	private Exception exception;

	public XmlDasMasterResponse() {
	}

	public String getEntityBody() {
		return entityBody;
	}

	public void setEntityBody(String entityBody) {
		this.entityBody = entityBody;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
