package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.util.ArrayList;

import com.sap.archtech.archconn.values.IndexPropValues;

public interface IGetIndexValuesListCommand 
{
	public ArrayList<IndexPropValues> getIndexValuesList() throws IOException;
}
