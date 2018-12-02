package com.sap.archtech.archconn;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import com.sap.archtech.archconn.httpclients.W3CdserviceClient;
import com.sap.archtech.archconn.util.DASResponseInputStream;
import com.sap.archtech.archconn.values.ServiceInfo;

/**
 * Default implementation of the ArchResponse interface. 
 * Users of the API get an ArchResponse by invoking ArchCommand.getResponse().
 * 
 * @see com.sap.archtech.archconn.ArchResponse
 * @see com.sap.archtech.archconn.ArchCommand
 * @author D025792
 * 
 */
public class DefaultArchResponse extends HttpArchResponse
{
  private final HttpURLConnection conn;

  /**
   * @deprecated
   */
  public DefaultArchResponse(HttpURLConnection conn, ArchCommand archCommand) 
  {
    super(archCommand, null);
    this.conn = conn;
  }

  public DefaultArchResponse(HttpURLConnection conn, ArchCommand archCommand, HashMap<? extends Object, ? extends Object> requestParams, Object caller)
  {
    super(archCommand, requestParams);
    if(caller instanceof W3CdserviceClient)
    {
      this.conn = conn;
    }
    else
    {
      throw new IllegalArgumentException("Not allowed to call this method!");
    }
  }

  public BufferedInputStream getBufferedInputStream() throws IOException
  {
    InputStream is = conn.getInputStream();
    if(is != null)
    {
      return new BufferedInputStream(is);
    }
    return null;
  }
  
  public InputStreamReader getErrorStreamReader() throws IOException
  {
    InputStream is = conn.getErrorStream();
    if(is == null)
    {
      is = conn.getInputStream();
    }
    if(is != null)
    {
      return new InputStreamReader(is, "UTF-8");
    }
    return null;
  }
  
  public InputStreamReader getInputStreamReader() throws IOException
  {
    return new InputStreamReader(new DASResponseInputStream(conn.getInputStream()), "UTF-8");
  }
  
  public InputStreamReader getBufferedInputStreamReader() throws IOException
  {
    return new InputStreamReader(new DASResponseInputStream(new BufferedInputStream(conn.getInputStream())), "UTF-8");
  }
  
  public ObjectInputStream getObjectInputStream() throws IOException
  {
    return new ObjectInputStream(conn.getInputStream());
  }

  public String getProtMessage() throws IOException
  {
    return conn.getResponseMessage();
  }

  public String getServiceMessage()
  {
    return conn.getHeaderField("service_message");
  }

  public int getStatusCode() throws IOException
  {
    return conn.getResponseCode();
  }

  public String getHeaderField(String headerField)
  {
    return conn.getHeaderField(headerField);
  }

  public ServiceInfo getServiceInfo()
  {
    ServiceInfo sinfo = new ServiceInfo();
    sinfo.setArchive_store(conn.getHeaderField("archive_store"));
    sinfo.setDbproductname(conn.getHeaderField("dbproductname"));
    sinfo.setDbproductversion(conn.getHeaderField("dbproductversion"));
    sinfo.setJavavendor(conn.getHeaderField("javavendor"));
    sinfo.setJavaversion(conn.getHeaderField("javaversion"));
    sinfo.setJavavmname(conn.getHeaderField("javavmname"));
    sinfo.setJavavmvendor(conn.getHeaderField("javavmvendor"));
    sinfo.setJavavmversion(conn.getHeaderField("javavmversion"));
    sinfo.setJdbcdrivername(conn.getHeaderField("jdbcdrivername"));
    sinfo.setJdbcdriverversion(conn.getHeaderField("jdbcdriverversion"));
    sinfo.setOsname(conn.getHeaderField("osname"));
    sinfo.setOsversion(conn.getHeaderField("osversion"));
    sinfo.setPhysical_path(conn.getHeaderField("physical_path"));
    sinfo.setRelease(conn.getHeaderField("release"));
    String ilmConformanceValue = conn.getHeaderField("ilm_conformance_class");
    if(ilmConformanceValue == null || "".equals(ilmConformanceValue))
    {
      sinfo.setIlmConformance((short)0);
    }
    else
    {
      sinfo.setIlmConformance(Short.parseShort(ilmConformanceValue));
    }
    String sysID = conn.getHeaderField("sap_sid");
		sinfo.setSysID(sysID == null ? "" : sysID);

    return sinfo;
  }
}