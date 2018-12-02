package com.sap.archtech.daservice.storage;

import java.io.OutputStream;

import javax.xml.validation.Schema;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasGetRequest extends XmlDasMasterRequest {
	
	private OutputStream os;
	private String uri;
	private long offset;
	private int length;
	private String checksum;
	private String mode;
	private String level;
	private Schema schema;

	public XmlDasGetRequest() {
		super();
	}

	public XmlDasGetRequest(Sapxmla_Config sac, OutputStream os, String uri,
			long offset, int length, String checksum, String mode,
			String level, Schema schema) {
		super(sac);
		this.os = os;
		this.uri = uri.toLowerCase();
		this.offset = offset;
		this.length = length;
		this.checksum = checksum;
		this.mode = mode;
		this.level = level;
		this.schema = schema;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
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

	public OutputStream getOs() {
		return os;
	}

	public void setOs(OutputStream os) {
		this.os = os;
	}
}
