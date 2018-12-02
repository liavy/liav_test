package com.sap.archtech.archconn;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.HashMap;

import com.sap.archtech.archconn.httpclients.DTRdserviceClient;
import com.sap.archtech.archconn.util.DASResponseInputStream;
import com.sap.archtech.archconn.values.ServiceInfo;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.IResponseStream;

/**
 * Implementation of the ArchResponse
 * interface for the DTR HTTP client.
 * Users of the API get an ArchResponse by
 * invoking ArchCommand.getResponse().
 * 
 * @see com.sap.archtech.archconn.ArchResponse
 * @see com.sap.archtech.archconn.ArchCommand
 * @author D025792
 * 
 */
public class DTRHTTPArchResponse extends HttpArchResponse
{
  private final IResponse resp;

  /**
   * @deprecated
   */
	public DTRHTTPArchResponse(IResponse resp, ArchCommand archCommand)
	{
    super(archCommand, null);
		this.resp = resp;
  }

  public DTRHTTPArchResponse(IResponse resp, ArchCommand archCommand, HashMap<? extends Object, ? extends Object> requestParams, Object caller)
  {
    super(archCommand, requestParams);
    if(caller instanceof DTRdserviceClient)
    {
      this.resp = resp;
    }
    else
    {
      throw new IllegalArgumentException("Not allowed to call this method!");
    }
  }
  
  public BufferedInputStream getBufferedInputStream() throws IOException
  {
    IResponseStream stresp = resp.getContent();
    if(stresp != null)
    {
      return new BufferedInputStream(stresp.asStream());
    }
    return null;
  }

  public InputStreamReader getErrorStreamReader() throws IOException
  {
    InputStream is = resp.getStream();
    if(is != null)
    {
      return new InputStreamReader(is, "UTF-8");
    }
    return null;
  }
  
  public InputStreamReader getInputStreamReader() throws IOException
  {
    return new InputStreamReader(new DASResponseInputStream(resp.getStream()), "UTF-8");
  }
  
  public InputStreamReader getBufferedInputStreamReader() throws IOException
  {
    return new InputStreamReader(new DASResponseInputStream(new BufferedInputStream(resp.getStream())), "UTF-8");
  }
  
  public ObjectInputStream getObjectInputStream() throws IOException
  {
    return new ObjectInputStream(resp.getStream());
  }
  
  public int getStatusCode() throws IOException
	{
		return resp.getStatus();
	}

	public String getProtMessage() throws IOException
	{
		return resp.getStatusDescription();
	}

	public String getServiceMessage()
	{
		return resp.getHeaderValue("service_message");
	}

	public String getHeaderField(String headerField)
	{
		return resp.getHeaderValue(headerField);
	}

	public ServiceInfo getServiceInfo()
	{
		ServiceInfo sinfo = new ServiceInfo();
		sinfo.setArchive_store(resp.getHeaderValue("archive_store"));
		sinfo.setDbproductname(resp.getHeaderValue("dbproductname"));
		sinfo.setDbproductversion(resp.getHeaderValue("dbproductversion"));
		sinfo.setJavavendor(resp.getHeaderValue("javavendor"));
		sinfo.setJavaversion(resp.getHeaderValue("javaversion"));
		sinfo.setJavavmname(resp.getHeaderValue("javavmname"));
		sinfo.setJavavmvendor(resp.getHeaderValue("javavmvendor"));
		sinfo.setJavavmversion(resp.getHeaderValue("javavmversion"));
		sinfo.setJdbcdrivername(resp.getHeaderValue("jdbcdrivername"));
		sinfo.setJdbcdriverversion(resp.getHeaderValue("jdbcdriverversion"));
		sinfo.setOsname(resp.getHeaderValue("osname"));
		sinfo.setOsversion(resp.getHeaderValue("osversion"));
		sinfo.setPhysical_path(resp.getHeaderValue("physical_path"));
		sinfo.setRelease(resp.getHeaderValue("release"));
    String ilmConformanceValue = resp.getHeaderValue("ilm_conformance_class");
    if(ilmConformanceValue == null || "".equals(ilmConformanceValue))
    {
      sinfo.setIlmConformance((short)0);
    }
    else
		{
      sinfo.setIlmConformance(Short.parseShort(ilmConformanceValue));
    }
		String sysID = resp.getHeaderValue("sap_sid");
		sinfo.setSysID(sysID == null ? "" : sysID);
    
		return sinfo;
	}
}
