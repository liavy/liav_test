package com.sap.archtech.daservice.storage;

public class XmlDasPutResponse extends XmlDasMasterResponse {

	private String checkSum;
	private long contentLength;

	public XmlDasPutResponse() {
		super();
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
}
