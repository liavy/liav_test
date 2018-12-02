package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.util.ArrayList;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.values.ServiceInfo;

/**
 * The <code>IGenericHttpResponseMethods</code> interface exposes methods provided by the {@link ArchResponse} of each {@link ArchCommand}
 * that requires XML DAS (HTTP) communication.
 */
public interface IGenericHttpResponseMethods 
{
  /**
   * Get the result strings contained in the XML DAS HTTP response stream of the command.
   * This method will not interpret those result strings. 
   */
  public ArrayList<String> getStringResult() throws IOException;
  
  /**
   * Returns meta information about the XML DAS involved.
   */
  public ServiceInfo getServiceInfo();
  
  /**
   * Returns the value of a XML DAS response header field.
   */
  public String getHeaderField(String headerField);
}
