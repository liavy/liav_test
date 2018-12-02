package com.sap.archtech.daservice.storage;

import java.io.InputStream;

import javax.xml.validation.Schema;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasPutRequest extends XmlDasMasterRequest {
	
	private InputStream is;
	private String uri;
	private String type;
	private String mode;
	private String level;
	private Schema schema;

	public XmlDasPutRequest() {
		super();
	}

	public XmlDasPutRequest(Sapxmla_Config sac, InputStream is, String uri,
			String type, String mode, String level, Schema schema) {
		super(sac);
		this.is = is;
		this.uri = uri.toLowerCase();
		this.type = type;
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}
}
