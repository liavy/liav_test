package com.sap.archtech.daservice.storage;

public class XmlDasGetStreamResponse extends XmlDasMasterResponse {

	private long contentLength;

	public XmlDasGetStreamResponse() {
		super();
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
}
