package com.sap.sl.util.dbaccess.impl;

/*
 * Created on 14.09.2005
 * @author d000706
 *
 * store knowledge about the DB connection
 */
public class ConnectionInfo
{
  /**
   *  The ConnectionInfo instance (singleton)
   */
  public static ConnectionInfo instance = null;

  private String dbProductName = "";
  private boolean vendorConnection = false;
  
  private ConnectionInfo()
  {
    this.reset();
  }

  /**
   *  Gets the ConnectionInfo instance
   *
   *@return the ConnectionInfo instances
   */
  public final static synchronized ConnectionInfo getInstance()
  {
    if (instance == null) {
      instance = new ConnectionInfo();
    }
    return instance;
  }
  
  public void setdbProductName(String dbProductName)
  {
    this.dbProductName = dbProductName;
  }
  
  public String getdbProductName()
  {
	return dbProductName;
  }
  
  public boolean isRunningOnOracle()
  {
    return (dbProductName.toLowerCase().startsWith("ora"));
  }
  
  public boolean time_is_stored_like_date()
  {
    return isRunningOnOracle();
  }
  
  public void setVendorConnection(boolean vendorConnection)
  {
    this.vendorConnection = vendorConnection;
  }
  
  public boolean isVendorConnection()
  {
    return (vendorConnection);
  }
  
  public void reset()
  {
    dbProductName = "";
    vendorConnection = false;
  }
}