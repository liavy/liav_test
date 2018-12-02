package com.sap.archtech.archconn.httpclients;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.DTRHTTPArchResponse;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.tssap.dtr.client.lib.protocol.HTTPException;
import com.tssap.dtr.client.lib.protocol.IConnection;
import com.tssap.dtr.client.lib.protocol.IConnectionTemplate;
import com.tssap.dtr.client.lib.protocol.IRequestEntity;
import com.tssap.dtr.client.lib.protocol.IResponse;
import com.tssap.dtr.client.lib.protocol.entities.StreamEntity;
import com.tssap.dtr.client.lib.protocol.requests.http.PostRequest;

public class DTRdserviceClient implements ArchHTTPClient
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private final IConnection conn;
  private final IConnectionTemplate httpTemplate;

  public DTRdserviceClient(IConnectionTemplate httpTemplate, IConnection conn)
  {
    this.httpTemplate = httpTemplate;
    this.conn = conn;
  }

  public ArchResponse executeRequest(HashMap<? extends Object, ? extends Object> params, ArchCommand archCommand) throws IOException
  {
    String desturl = httpTemplate.getBasePath();

    /**
     * Temporary until fix in destination service is available!
     */
    if (desturl.endsWith("/"))
    {
    	desturl = desturl.substring(0, desturl.length() - 1);
    }

    InputStream in = (InputStream)params.get("STREAM");
    PostRequest preq = new PostRequest(desturl);
    IResponse httpresp = null;
    try
    {
      HashMap<Object, Object> tmp = new HashMap<Object, Object>(params);
      Set<Entry<Object, Object>> entrySet = tmp.entrySet();
      if (in != null)
      {
        IRequestEntity body = new StreamEntity(in, "application/octet-stream");
        preq.setRequestEntity(body);
        for(Entry<Object, Object> entry : entrySet)
        {
          if (!entry.getKey().equals("STREAM"))
          {
          	preq.setHeader((String) entry.getKey(), (String) entry.getValue());
          }
        }
      }
      else
      {
        for (Entry<Object, Object> entry : entrySet)
        {
          preq.setHeader((String) entry.getKey(), (String) entry.getValue());
        }
      }

      httpresp = conn.send(preq);
    }
    catch (HTTPException httpex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "DTRdserviceClient().execute()", httpex);
      throw new IOException(httpex.getMessage());
    }

    return new DTRHTTPArchResponse(httpresp, archCommand, params, this);
  }
}
