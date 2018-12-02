package com.sap.archtech.archconn.response;

import java.io.IOException;

import com.sap.archtech.archconn.values.LegalHoldValues;

public interface IGetLegalHoldValuesCommand 
{
	public LegalHoldValues getLegalHoldValues() throws IOException;
}
