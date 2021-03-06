﻿/*
 This file is generated by Code Generator
 for CIMClass SAP_ITSAMArchConnAccessor
 */

package com.sap.archtech.archconn.mbeans;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.ArchSessionFactory;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.util.ArchConfigProviderSingle;
import com.sap.archtech.archconn.util.ArchSetConfigurator;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ResourceData;
import com.sap.archtech.archconn.values.ServiceInfo;

/*
 * ManagedElement is an abstract class that provides a common superclass (or top
 * of the inheritance tree) for the non-association classes in the CIM Schema.
 * 
 * @version 1.0
 */

public class SAP_ITSAMArchConnAccessor_Impl implements SAP_ITSAMArchConnAccessor
{
  // for access to Home Path Collection URIs
  private final SAP_ITSAMArchSetPropAccessor archSetPropAccessor = new SAP_ITSAMArchSetPropAccessor_Impl();
  
  public String checkXMLDASConnection(String userName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    ArchSession uqas = null;
    try
    {
      ArchSessionFactory asf = ArchSessionFactory.getSessionFactory();
      uqas = asf.getSession(userName, ArchSession.GLOBAL_ARCHSET);
      ArchCommand infocom = uqas.createCommand(ArchCommand.AC_INFO);
      infocom.execute();
      ArchResponse aresp = infocom.getResponse();
      if(aresp.getStatusCode() == DasResponse.SC_OK)
      {
        return aresp.getServiceInfo().getRelease();
      }
      throw new ArchConnException("Could not connect to XML Data Archiving Service. The status code is: " + aresp.getStatusCode());
    }
    catch(IOException e)
    {
      // command execution failed
      throw new RuntimeException(new ArchConnException(e.getMessage()));
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(uqas != null)
      {
        try
        {
          uqas.close();
        }
        catch(Exception e)
        {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public void createAllHomePaths(String userName)
  {
    try
    {
      // check if all home paths are created and create them if necessary
      ArchSetConfigurator.createSetHome(userName);
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
  }

  public boolean isArchiveStoreAssigned(String userName , String collNameAsPath , String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    ArchSession archSession = null;
    try
    {
      URI fullCollectionPath = _getFullCollectionPath(collNameAsPath, archSetName);
      ArchSessionFactory asf = ArchSessionFactory.getSessionFactory();
      archSession = asf.getSession(userName, ArchSession.GLOBAL_ARCHSET);
      return isArchiveStoreAssigned_Recursive(archSession, fullCollectionPath, archSetName);
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
    catch(IOException e)
    {
      throw new RuntimeException(new ArchConnException(e.getMessage()));
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(archSession != null)
      {
        try
        {
          archSession.close();
        }
        catch(Exception e)
        {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public com.sap.archtech.archconn.mbeans.SAP_ITSAMArchResource[] getResourceList(String userName , String fullCollPath , String archSetName , String resourceType)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    ArchSession archSession = null;
    try
    {
      URI fullPath = new URI(fullCollPath);
      ArchSessionFactory asf = ArchSessionFactory.getSessionFactory();
      archSession = asf.getSession(userName, archSetName);
      ArchCommand listCommand = archSession.createCommand(ArchCommand.AC_LIST);
      listCommand.addParam(fullPath);
      listCommand.addParam(ArchCommand.LIST_RANGE_RES);
      listCommand.addParam(ArchCommand.SEARCH_MODE_RECURSIVE);
      listCommand.addParam(ArchCommand.RESOURCE_TYPE, resourceType);
      listCommand.execute();

      ArchResponse archResponse = listCommand.getResponse();
      if(archResponse.getStatusCode() == DasResponse.SC_OK)
      {
        ArrayList<ResourceData> resources = archResponse.getResourceData();
        if(resources == null)
        {
          return new SAP_ITSAMArchResource[0];
        }
        SAP_ITSAMArchResource[] resourceArr = new SAP_ITSAMArchResource[resources.size()];
        int i = 0;
        for(ResourceData resource : resources)
        {
          resourceArr[i] = new SAP_ITSAMArchResource(
            resource.getType(), resource.getCollectionType(), 
            resource.getUri().toString(), resource.getCreationTime(), 
            resource.getUser(), resource.getLength(), resource.getCheckStatus(), 
            resource.isPacked(), resource.isFrozen());
          i++;
        }
        return resourceArr;
      }
      throw new ArchConnException("LIST command failed (protocol message = " + archResponse.getProtMessage() + ")");
    }
    catch(IOException e)
    {
      throw new RuntimeException(new ArchConnException(e.getMessage()));
    }
    catch(ArchConnException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      if(archSession != null)
      {
        try
        {
          archSession.close();
        }
        catch(Exception e)
        {
          throw new RuntimeException(e);
        }
      }
    }
  }
  
  public String getFullCollectionPath(String collNameAsPath , String archSetName)
  {
    ConnectorAccessPermission.checkPermission("view", "*");
    try
    {
      URI fullCollURI = _getFullCollectionPath(collNameAsPath, archSetName);
      return fullCollURI.toString();
    }
    catch(ArchConfigException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public boolean ping()
  {
    return true;
  }
  
  // Helpers
  private boolean isArchiveStoreAssigned_Recursive(final ArchSession archSession, URI fullCollectionPath, final String archSetName)
  throws UnsupportedCommandException, SessionHandlingException, MethodNotApplicableException, IOException, ArchConnException, SQLException
  {
    ArchCommand infoCommand = archSession.createCommand(ArchCommand.AC_INFO);
    infoCommand.addParam(fullCollectionPath);
    infoCommand.execute();
    ArchResponse archResponse = infoCommand.getResponse();
    int responseStatusCode = archResponse.getStatusCode();
    if(responseStatusCode == DasResponse.SC_OK)
    {
      // given Collection exists -> get info about assigned Archive Store
      ServiceInfo serviceInfo = archResponse.getServiceInfo();
      String archiveStore = serviceInfo.getArchive_store();
      if(archiveStore == null || "".equals(archiveStore))
      {
        return false;
      }
      return true;
    }
    else if(responseStatusCode == DasResponse.SC_DOES_NOT_EXISTS)
    {
      // given Collection does not exist yet -> try again with its parent collection
      URI parentURI = getParentURI(fullCollectionPath.toString(), archSetName);
      if(parentURI != null)
      {
        return isArchiveStoreAssigned_Recursive(archSession, parentURI, archSetName);
      }
      return false;
    }
    // unexpected response code -> throw exception
    throw new ArchConnException("Unexpected response from INFO command (code = " + responseStatusCode + ")");
  }
  
  private URI getParentURI(String fullCollectionPath, String archSetName) throws SQLException
  {
    // cut trailing "/"
    String tmp = collectionNameAsURI(fullCollectionPath);
    // tmp must refer to a Collection below the Home Path
    if(isHomePathCollectionURIContained(tmp, archSetName))
    {
      int lastSlash = tmp.lastIndexOf('/');
      if(lastSlash > 0)
      {
        // cut everything beyond the last "/"
        return new URI(tmp.substring(0, lastSlash + 1));
      }
    }
    // there is no parent of the given Collection below the Home Path 
    return null;
  }
  
  private boolean isHomePathCollectionURIContained(String collUri, String archSetName) throws SQLException
  {
    String homePathCollUri = getHomePathCollectionURI(archSetName);
    if(collUri.indexOf(homePathCollUri) > -1)
    {
      return true;
    }
    return false;
  }
  
  private String getHomePathCollectionURI(String archSetName) throws SQLException 
  {
    try
    {
      return archSetPropAccessor.getHomePathCollectionURI(archSetName);
    }
    catch(RuntimeException e)
    {
      // thrown by MBean due to missing possibility to model exceptions
      Throwable th = e.getCause();
      if(th instanceof SQLException)
      {
        throw (SQLException)th;
      }
      // unexpected RuntimeException occurred
      throw e;
    }
  }
  
  private String collectionNameAsURI(String collectionName)
  {
    StringBuilder collName = new StringBuilder(collectionName);
    if(collName.charAt(collName.length() - 1) == '/')
    {
      collName.deleteCharAt(collName.length() - 1);
    }
    return collName.toString();
  }
  
  private URI _getFullCollectionPath(String collectionNameAsPath, String archSetName) throws ArchConfigException
  {
    ArchConfigProviderSingle provider = ArchConfigProviderSingle.getArchConfigProviderSingle();
    URI archSetHome = new URI(provider.getArchSetHome(archSetName));
    return archSetHome.resolve(new URI(collectionNameAsPath));
  }
}