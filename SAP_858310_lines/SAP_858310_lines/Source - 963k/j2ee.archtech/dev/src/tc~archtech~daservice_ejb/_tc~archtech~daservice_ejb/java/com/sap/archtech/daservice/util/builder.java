package com.sap.archtech.daservice.util;

import com.sap.sld.api.builder.DeltaElement;
import com.sap.sld.api.builder.GenericInputData;
import com.sap.sld.api.builder.IData;
import com.sap.sld.api.builder.IDelta;
import com.sap.sld.api.builder.SimpleDeltaBuilder;
import com.sap.sld.api.log.Logger;
import com.sap.sld.api.util.Version;
import com.sap.sld.api.wbem.client.WBEMClient;
import com.sap.sld.api.wbem.exception.CIMException;

public class Builder extends SimpleDeltaBuilder {

	private static final Logger logger = Logger.getLogger(Builder.class
			.getName());

	protected Builder(WBEMClient aCIMClient) throws CIMException {
		super(aCIMClient);
	}

	protected boolean evaluateSpecificAssociationDelta(DeltaElement obj,
			IDelta delta) throws CIMException {
		return false;
	}

	protected DeltaElement findSpecificOriginalObject(DeltaElement obj)
			throws CIMException {
		return null;
	}

	protected boolean evaluateSpecificRenameDelta(DeltaElement originalObj,
			DeltaElement currentObj, IDelta delta) throws CIMException {
		return false;
	}

	protected Logger getLogger() {
		return logger;
	}

	public String getSupplierName() {
		return "Builder";
	}

	public Version getRequiredModelVersion() {
		return new Version(0, 0);
	}

	public void refresh() throws CIMException {
		return;
	}

	public boolean accept(IData dataObj) {
		if (dataObj instanceof GenericInputData) {
			return true;
		}
		return false;
	}
}
