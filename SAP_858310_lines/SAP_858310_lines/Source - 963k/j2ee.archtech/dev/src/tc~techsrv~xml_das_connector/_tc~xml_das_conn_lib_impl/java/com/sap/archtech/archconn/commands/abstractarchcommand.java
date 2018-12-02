package com.sap.archtech.archconn.commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.util.ArchiveDataViewer;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.ColSearchClause;
import com.sap.archtech.archconn.values.IndexPropDescription;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.archconn.values.SelectClause;
import com.sap.archtech.archconn.values.TechResKey;
import com.sap.tc.logging.Location;

/**
 * Implements interface ArchCommand. Implements behavior common to all
 * ArchCommands. Serves as root for specific ArchCommands.
 * 
 * Note: This class is declared abstract so that no instances can be created.
 * 
 * 
 * @author D025792
 * @version 1.0
 *  
 */

public abstract class AbstractArchCommand implements ArchCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands.AbstractArchCommand");
  
  private ArchResponse response;
  private HashMap<String, String> stringHeaderFields;
  private HashMap<Object, Object> mixedHeaderFields;
  private final AbstractArchSession archSessionRef;
  private final boolean hasMixedHeaderFields;

  protected AbstractArchCommand(AbstractArchSession archSessionRef, String archuser)
  {
    this(archSessionRef, archuser, false);
  }
  
  protected AbstractArchCommand(AbstractArchSession archSessionRef, String archuser, boolean hasMixedHeaderFields)
  {
    if(hasMixedHeaderFields)
    {
      mixedHeaderFields = new HashMap<Object, Object>();
      mixedHeaderFields.put("User-Agent", "SAPArchivingConnector");
      mixedHeaderFields.put("user", archuser);
      stringHeaderFields = null;
    }
    else
    {
      stringHeaderFields = new HashMap<String, String>();
      stringHeaderFields.put("User-Agent", "SAPArchivingConnector");
      stringHeaderFields.put("user", archuser);
      mixedHeaderFields = null;
    }
    this.archSessionRef = archSessionRef;
    this.hasMixedHeaderFields = hasMixedHeaderFields;
  }
  
  protected final void addObjectParam(String hfield, Object value)
  {
    if(!hasMixedHeaderFields)
    {
      throw new UnsupportedOperationException("Method \"addObjectParam()\" may only be used by archiving commands with mixed-type header fields!");
    }
    mixedHeaderFields.put(hfield, value);
  }
  
  protected final Object getObjectParam(String hfield)
  {
    if(!hasMixedHeaderFields)
    {
      throw new UnsupportedOperationException("Method \"getObjectParam()\" may only be used by archiving commands with mixed-type header fields!");
    }
    return mixedHeaderFields.get(hfield);
  }
  
  protected final String getStringParam(String hfield)
  {
    if(hasMixedHeaderFields)
    {
      throw new UnsupportedOperationException("Method \"getStringParam()\" may only be used by archiving commands with string-type header fields!");
    }
    return stringHeaderFields.get(hfield);
  }
  
  /**
   * @see ArchCommand#addParam(String, String)
   */
  public void addParam(String hfield, String value)
  {
    if ((hfield != null) && (value != null))
    {
      if(hasMixedHeaderFields)
      {
        mixedHeaderFields.put(hfield, value);
      }
      else
      {
        stringHeaderFields.put(hfield, value);
      }
    }
  }

  /**
   * @see ArchCommand#addParam(String)
   */
  public void addParam(String param)
  {
    String paramKey;
    String paramValue;

    if (param != null)
    {
      StringTokenizer tok = new StringTokenizer(param, ":");
      if (tok.countTokens() == 2)
      {
        paramKey = tok.nextToken();
        paramValue = tok.nextToken();
        if ("<auto>".equals(paramValue))
        {
          if(hasMixedHeaderFields)
          {
            mixedHeaderFields.put(paramKey, "");
          }
          else
          {
            stringHeaderFields.put(paramKey, "");
          }
        }
        else
        {
          if(hasMixedHeaderFields)
          {
            mixedHeaderFields.put(paramKey, paramValue);
          }
          else
          {
            stringHeaderFields.put(paramKey, paramValue);
          }
        }
      }
    }
  }
  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri) throws MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(URI) is not supported for this archiving command");
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(String, boolean)
   */
  public void addParam(String headerField, boolean value) throws MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(String, boolean) is not supported for this archiving command");
  }
  
  /**
   * @see ArchCommand#addParam(String, String)
   */
  public void addParam(InputStream in1) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(InputStream) is not supported for this archiving command");
  }

  /**
   * @see ArchCommand#addParam(IndexPropValues)
   */
  public void addParam(IndexPropValues ipv) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(IndexPropValues) is not supported for this archiving command");
  }

  /**
   * @see ArchCommand#addParam(IndexPropDescription)
   */
  public void addParam(IndexPropDescription ipd) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(IndexPropDescription) is not supported for this archiving command");
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.values.SelectClause)
   */
  public void addParam(SelectClause slc) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(SelectClause) is not supported for this archiving command");

  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.values.TechResKey)
   */
  public void addParam(TechResKey reskey) throws MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(IndexProp) is not supported for this archiving command");
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.values.ArchivingPropertyValues)
   */
  public void addParam(ArchivingPropertyValues archPropertyValues) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(ArchivingPropertyValues) is not supported for this archiving command");
  }
  
  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.values.ColSearchClause)
   */
  public void addParam(ColSearchClause colSearchClause) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(ColSearchClause) is not supported for this archiving command");
  }

  public void addParam(Set<URI> uris) throws IOException, MethodNotApplicableException
  {
    throw new MethodNotApplicableException("addParam(Set<URI>) is not supported for this archiving command");
  }
  
  /**
   * @see ArchCommand#execute()
   */
  public void execute() throws IOException
  {
    AbstractArchSession archSession = getArchSessionRef();
    HashMap<? extends Object, ? extends Object> requParams = getHeaderFields();
    try
    {
      response = archSession.getHttpClient().executeRequest(requParams, this);
    }
    catch(IOException e)
    {
      // $JL-EXC$
      // indicates a connection problem -> maybe the URL of the HTTP destination has changed
      // -> reload destination settings and try once more
      response = retryRequestExecution(archSession, requParams);
    }
    
    if(response.getStatusCode() == 401)
    {
      // authorization problem -> maybe the user credentials of the HTTP destination have changed
      // -> reload destination settings and try once more
      response = retryRequestExecution(archSession, requParams);
    }
      
    // check the response header - must contain field "service_message" indicating the request reached XMLDAS
    boolean xmldasReached = response.getServiceMessage() != null ? true : false;
    if(!xmldasReached)
    {
      String commandName = (String)requParams.get("method");
      if(requParams.get("submethod") != null)
      {
        commandName = commandName.concat((String)requParams.get("submethod"));
      }
      loc.warningT(new StringBuilder("The HTTP request of command \"")
        .append(commandName.toUpperCase())
        .append("\" did not reach the XMLDAS. This may indicate that the credentials for the HTTP destination have expired.")
        .toString());
			// return response status code in exception text to point the caller to the user-pw-problem
			throw new IOException(new StringBuilder("response_code=").append(response.getStatusCode()).toString());            
    }
  }

  private ArchResponse retryRequestExecution(final AbstractArchSession archSession, final HashMap<? extends Object, ? extends Object> requParams) throws IOException
  {
    try
    {
      archSession.createNewHttpConnection(this, true);
      return archSession.getHttpClient().executeRequest(requParams, this);
    }
    catch(ArchConfigException e)
    {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * @see ArchCommand#getResponse()
   */
  public ArchResponse getResponse()
  {
    return response;
  }
  
  protected void setResponse(ArchResponse response)
  {
    this.response = response;
  }
  
  protected AbstractArchSession getArchSessionRef()
  {
    return archSessionRef;
  }
  
  // non-private because needed for test code
  HashMap<? extends Object, ? extends Object> getHeaderFields()
  {
    return hasMixedHeaderFields ? mixedHeaderFields : stringHeaderFields;
  }

  /**
   * For internal usage only!
   */
  public AbstractArchSession getArchSessionRef(Object caller)
  {
    if(!(caller instanceof ArchiveDataViewer))
    {
      throw new IllegalArgumentException("This method must not be called by instances of " + caller.getClass().getName());  
    }
    return archSessionRef;
  }
}