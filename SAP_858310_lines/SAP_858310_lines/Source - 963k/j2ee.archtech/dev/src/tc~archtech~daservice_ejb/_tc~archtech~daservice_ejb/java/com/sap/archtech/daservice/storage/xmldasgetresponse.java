package com.sap.archtech.daservice.storage;

public class XmlDasGetResponse extends XmlDasMasterResponse {

	private String checksum;
	private boolean checksumIdentical;
	private long contentLength;

	public XmlDasGetResponse() {
		super();
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public boolean isChecksumIdentical() {
		return checksumIdentical;
	}

	public void setChecksumIdentical(boolean checksumIdentical) {
		this.checksumIdentical = checksumIdentical;
	}
}
