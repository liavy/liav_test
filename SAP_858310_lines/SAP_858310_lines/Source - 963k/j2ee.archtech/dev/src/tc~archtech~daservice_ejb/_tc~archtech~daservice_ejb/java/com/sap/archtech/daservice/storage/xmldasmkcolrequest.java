package com.sap.archtech.daservice.storage;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasMkcolRequest extends XmlDasMasterRequest {
	
	private String uri;

	public XmlDasMkcolRequest() {
		super();
	}

	public XmlDasMkcolRequest(Sapxmla_Config sac, String uri) {
		super(sac);
		this.uri = uri.toLowerCase();
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri.toLowerCase();
	}
}
