package com.sap.archtech.daservice.storage;

import com.sap.archtech.daservice.data.Sapxmla_Config;

public class XmlDasMasterRequest {

	private Sapxmla_Config sac;

	public XmlDasMasterRequest() {
	}

	public XmlDasMasterRequest(Sapxmla_Config sac) {
		this.sac = sac;
	}

	public void setSac(Sapxmla_Config sac) {
		this.sac = sac;
	}

	public Sapxmla_Config getSac() {
		return sac;
	}
}
