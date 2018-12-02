package com.sap.archtech.archconn.util;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.InternalArchSessionFactory;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.values.ArchiveStoreData;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>RemoteXmldasConfigurator</code> class can be used for remote administration
 * of an XMLDAS instance which is identified by an appropriate HTTP destination only.
 */
public class RemoteXmldasConfigurator 
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private RemoteXmldasConfigurator() {}
  
  /**
   * Define an archive store in XMLDAS.
   * @param archiveStore4Insertion Contains all data of the archive store to be defined
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the archive store definition was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean defineArchiveStore(ArchiveStoreData archiveStore4Insertion, String destinationName, String userName)
  throws ArchConnException
  {
    ArchSession session = null;
    try
    {
      session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
      ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
      defineASCmd.addParam("action", "I");
      defineASCmd.addParam("archive_store", archiveStore4Insertion.getArchiveStore());
      defineASCmd.addParam("storage_system", archiveStore4Insertion.getStorageSystem());
      defineASCmd.addParam("store_type", archiveStore4Insertion.getStoreType());
      defineASCmd.addParam("win_root", archiveStore4Insertion.getWinRoot());
      defineASCmd.addParam("unix_root", archiveStore4Insertion.getUnixRoot());
      defineASCmd.addParam("destination", archiveStore4Insertion.getDestination());
      defineASCmd.addParam("proxy_host", archiveStore4Insertion.getProxyHost());
      defineASCmd.addParam("proxy_port", Integer.valueOf(archiveStore4Insertion.getProxyPort()).toString());
      defineASCmd.addParam("is_default", archiveStore4Insertion.getIsDefault());
      defineASCmd.execute();
      ArchResponse response = defineASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("DEFINEARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Defining an archive store failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        session.close();
      }
    }
  }
  
  /**
   * Update an archive store in XMLDAS.
   * @param archiveStore4Insertion Contains all data of the archive store that can be updated
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the archive store update was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean updateArchiveStore(ArchiveStoreData archiveStore4Update, String destinationName, String userName)
  throws ArchConnException
  {
    ArchSession session = null;
    try
    {
      session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
      ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
      defineASCmd.addParam("action", "U");
      defineASCmd.addParam("archive_store", archiveStore4Update.getArchiveStore());
      defineASCmd.addParam("storage_system", archiveStore4Update.getStorageSystem());
      defineASCmd.addParam("store_type", archiveStore4Update.getStoreType());
      defineASCmd.addParam("win_root", archiveStore4Update.getWinRoot());
      defineASCmd.addParam("unix_root", archiveStore4Update.getUnixRoot());
      defineASCmd.addParam("destination", archiveStore4Update.getDestination());
      defineASCmd.addParam("proxy_host", archiveStore4Update.getProxyHost());
      defineASCmd.addParam("proxy_port", Integer.valueOf(archiveStore4Update.getProxyPort()).toString());
      defineASCmd.addParam("is_default", archiveStore4Update.getIsDefault());
      defineASCmd.execute();
      ArchResponse response = defineASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("DEFINEARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Updating an archive store failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        session.close();
      }
    }
  }
  
  /**
   * Assign an archive store to an archive path in XMLDAS.
   * @param archiveStoreName Name of the archive store
   * @param archivePath Archive path to be assigned to the archive store
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the archive store assignment was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean assignArchiveStore(String archiveStoreName, String archivePath, String destinationName, String userName)
  throws ArchConnException
  {
    ArchSession session = null;
    try
    {
      session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
      ArchCommand assignASCmd = session.createCommand("_assign_archive_stores");
      assignASCmd.addParam("action", "A");
      assignASCmd.addParam(new URI(archivePath));
      assignASCmd.addParam("archive_store", archiveStoreName);
      assignASCmd.execute();
      ArchResponse response = assignASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("ASSIGNARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Assigning an archive store failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        session.close();
      }
    }
  }
  
  /**
   * Tests if the given archive store is up and running.
   * @param archiveStoreName Name of the archive store
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the archive store test was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean testArchiveStore(String archiveStoreName, String destinationName, String userName) 
  throws ArchConnException
	{
  	ArchiveDataViewer archDataViewer = new ArchiveDataViewer(destinationName);
 		ArchiveStoreData storeData = archDataViewer.testArchiveStore(archiveStoreName, userName);
 		String archStoreStatus = storeData.getStoreStatus();
     if(archStoreStatus.startsWith("F"))
     {
     	String errMsg = new StringBuilder("Testing archive store \"")
   			.append(archiveStoreName)
   			.append("\" failed; archive store status is:  ")
   			.append(archStoreStatus).toString();
     	cat.warningT(loc, errMsg);
     	return false;
     }
     return true;
	}
  
  /**
   * Remove the assignment an archive store to an archive path in XMLDAS.
   * @param archivePath Archive path to be unassigned
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the archive store unassignment was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean unassignArchiveStore(String archivePath, String destinationName, String userName)
  throws ArchConnException
  {
    ArchSession session = null;
    try
    {
      session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
      ArchCommand assignASCmd = session.createCommand("_assign_archive_stores");
      assignASCmd.addParam("action", "U");
      assignASCmd.addParam(new URI(archivePath));
      assignASCmd.execute();
      ArchResponse response = assignASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("ASSIGNARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Unassigning an archive store failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        session.close();
      }
    }
  }
  
  /**
   * Delete an archive store definition in XMLDAS.
   * @param archStoreName Name of the archive store to be deleted
   * @param destinationName Name of the HTTP destination of the XMLDAS to be configured
   * @param userName Name of the user executing this configuration action
   * @return <code>true</code> if the deletion of the archive store definition was successful
   * @throws ArchConnException Thrown if any problem occurred
   */
  public static boolean deleteArchiveStoreDefinition(String archStoreName, String destinationName, String userName) throws ArchConnException
  {
    ArchSession session = null;
    try
    {
      session = InternalArchSessionFactory.getInstance().getUnqualifiedArchSession4Destination(userName, null, destinationName);
      ArchCommand defineASCmd = session.createCommand("_define_archive_stores");
      defineASCmd.addParam("action", "D");
      defineASCmd.addParam("archive_store", archStoreName);
      defineASCmd.execute();
      ArchResponse response = defineASCmd.getResponse();
      int responseStatusCode = response.getStatusCode();
      if(responseStatusCode != DasResponse.SC_OK)
      {
        String errMsg = new StringBuilder("DEFINEARCHIVESTORES returned ")
          .append(responseStatusCode)
          .append("(protocol message: ")
          .append(response.getProtMessage())
          .append(" , service message: ")
          .append(response.getServiceMessage())
          .append(")")
          .toString();
        cat.errorT(loc, errMsg);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Deleting an archive store definition failed", e);
      throw new ArchConnException(e.getMessage());
    }
    finally
    {
      if(session != null)
      {
        // there is no difference between "cancel()" or "close()" for an unqualified session
        session.close();
      }
    }
  }
}
