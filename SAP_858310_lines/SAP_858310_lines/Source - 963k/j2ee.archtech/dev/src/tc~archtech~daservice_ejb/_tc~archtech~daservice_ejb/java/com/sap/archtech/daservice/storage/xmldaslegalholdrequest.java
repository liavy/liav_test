package com.sap.archtech.daservice.storage;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasLegalHoldRequest extends XmlDasMasterRequest {
	
	private String uri;
	private String ilm_case;
	private String range;
	private String type;

	public XmlDasLegalHoldRequest() {
		super();
	}

	public XmlDasLegalHoldRequest(Sapxmla_Config sac, String uri,
			String ilm_case, String range, String type) {
		super(sac);
		this.uri = uri;
		this.ilm_case = ilm_case;
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

	public String getIlm_case() {
		return ilm_case;
	}

	public void setIlm_case(String ilm_case) {
		this.ilm_case = ilm_case;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
