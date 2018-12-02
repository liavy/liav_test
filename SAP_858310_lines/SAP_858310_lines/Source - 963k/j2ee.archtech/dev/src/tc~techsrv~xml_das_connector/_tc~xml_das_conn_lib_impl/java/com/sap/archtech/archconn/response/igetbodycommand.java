package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.io.InputStream;

public interface IGetBodyCommand 
{
	public InputStream getBody() throws IOException;
}
