package com.sap.archtech.archconn.response;

import java.io.IOException;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.util.DasResponse;

/**
 * The <code>IGenericResponseMethods</code> interface exposes methods provided by the {@link ArchResponse} of each {@link ArchCommand}.
 */
public interface IGenericResponseMethods 
{
  /**
   * Returns the status code of the XML DAS HTTP response. For possible status codes see {@link DasResponse}.
   * @throws IOException Thrown if the status code could not be retrieved
   */
  public int getStatusCode() throws IOException;
  
  /**
   * Returns the message text of the XML DAS HTTP response (if any), e.g. "Ok" or "NOT FOUND".
   * @throws IOException Thrown if the protocol message could not be retrieved
   */
  public String getProtMessage() throws IOException;
  
  /**
   * Returns the message text of the XML DAS. Additionally to the pure HTTP protocol message provided by 
   * {@link IGenericResponseMethods#getProtMessage()}, this message text contains detailed information 
   * on the command processing provided by XML DAS.
   */
  public String getServiceMessage();
  
  /**
   * Returns the comprehensive error message of the XML DAS HTTP response.
   * @throws IOException Thrown if the error message could not be retrieved
   */
  public String getErrorMessage() throws IOException;
}
