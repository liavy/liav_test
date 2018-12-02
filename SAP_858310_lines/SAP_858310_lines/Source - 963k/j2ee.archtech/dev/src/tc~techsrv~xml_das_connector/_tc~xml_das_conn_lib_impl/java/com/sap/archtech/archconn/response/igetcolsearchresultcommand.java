package com.sap.archtech.archconn.response;

import java.io.IOException;

import com.sap.archtech.archconn.values.ColSearchResult;

public interface IGetColSearchResultCommand 
{
	public ColSearchResult getColSearchResult() throws IOException;
}
