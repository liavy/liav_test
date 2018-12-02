package com.sap.archtech.daservice.storage;

import java.io.OutputStream;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasGetStreamRequest extends XmlDasMasterRequest {
	
	private OutputStream os;
	private String uri;
	private long offset;
	private int length;

	public XmlDasGetStreamRequest() {
		super();
	}

	public XmlDasGetStreamRequest(Sapxmla_Config sac, OutputStream os,
			String uri, long offset, int length) {
		super(sac);
		this.os = os;
		this.uri = uri.toLowerCase();
		this.offset = offset;
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getOffset() {
		return offset;
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public void setOutputStream(OutputStream os) {
		this.os = os;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}
}
