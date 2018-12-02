package com.sap.archtech.daservice.util;

import com.sap.sld.api.builder.IBuilder;
import com.sap.sld.api.builder.IBuilderFactory;
import com.sap.sld.api.wbem.client.WBEMClient;
import com.sap.sld.api.wbem.exception.CIMException;

public class BuilderFactory implements IBuilderFactory {

	public BuilderFactory() {
		super();
	}

	public IBuilder newBuilder(WBEMClient aCIMClient) throws CIMException {
		Builder builder = new Builder((aCIMClient));
		builder.setSyncTimeUpdated(false);
		return builder;
	}

	public String getBuilderClassName() {
		return Builder.class.getName();
	}
}
