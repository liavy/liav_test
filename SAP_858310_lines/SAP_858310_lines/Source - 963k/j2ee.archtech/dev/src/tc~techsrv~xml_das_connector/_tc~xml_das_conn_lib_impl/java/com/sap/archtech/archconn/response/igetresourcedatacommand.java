package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.util.ArrayList;

import com.sap.archtech.archconn.values.ResourceData;

public interface IGetResourceDataCommand 
{
	public ArrayList<ResourceData> getResourceData() throws IOException;
}
