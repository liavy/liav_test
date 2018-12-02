package com.sap.archtech.archconn.response;

import java.io.IOException;

import com.sap.archtech.archconn.values.IndexPropDescription;

public interface IGetIndexPropDescriptionCommand 
{
	public IndexPropDescription getIndexProps() throws IOException;
}
