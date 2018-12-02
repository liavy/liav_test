package com.sap.archtech.daservice.storage;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasPropPatchRequest extends XmlDasMasterRequest {
	
	private String uri;
	private String action;
	private String properties;
	private String type;

	public XmlDasPropPatchRequest() {
		super();
	}

	public XmlDasPropPatchRequest(Sapxmla_Config sac, String uri,
			String action, String properties, String type) {
		super(sac);
		this.uri = uri;
		this.action = action;
		this.properties = properties;
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
