package com.sap.archtech.daservice.storage;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasPropFindRequest extends XmlDasMasterRequest {
	
	private String uri;
	private String range;
	private String type;

	public XmlDasPropFindRequest() {
		super();
	}

	public XmlDasPropFindRequest(Sapxmla_Config sac, String uri, String range,
			String type) {
		super(sac);
		this.uri = uri;
		this.range = range;
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
