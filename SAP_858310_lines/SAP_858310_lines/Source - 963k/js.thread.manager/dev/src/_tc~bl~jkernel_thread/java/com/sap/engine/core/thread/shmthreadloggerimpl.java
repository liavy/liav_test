/*
 * Created on May 15, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.core.thread;


import com.sap.engine.core.Names;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.engine.system.ShmThreadLogger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
/**
 * @author Elitsa Pancheva
 */
public class ShmThreadLoggerImpl implements ShmThreadLogger {

  /**
   * Location to use for tracing
   */
  private final static Location location = Location.getLocation(ShmThreadLoggerImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  /**
   * Category instance
   */
  public static Category category = Category.SYS_SERVER;
  
  /*
   * The cluster ID of this instance
   * */
  static int clusterId = 0;
  
  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogPathInLocation()
   */
  public boolean toLogPathInLocation() {
    return location.bePath();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogInfoInCategory()
   */
  public boolean toLogInfoInCategory() {
    return category.beInfo();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogInfoInLocation()
   */
  public boolean toLogInfoInLocation() {
    return location.beInfo();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogDebugInLocation()
   */
  public boolean toLogDebugInLocation() {
    return location.beDebug();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogWarningInCategory()
   */
  public boolean toLogWarningInCategory() {
    return category.beWarning();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#toLogWarningInLocation()
   */
  public boolean toLogWarningInLocation() {
    return location.beWarning();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#errorT(java.lang.String)
   */
  public void errorT(String msg) {
    location.errorT(msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#errorT(java.lang.String, java.lang.Object[])
   */
  public void errorT(String msg, Object[] params) {
    location.errorT(msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#infoT(java.lang.String)
   */
  public void infoT(String msg) {
    location.infoT(msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#infoT(java.lang.String, java.lang.Object[])
   */
  public void infoT(String msg, Object[] params) {
    location.infoT(msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#warningT(java.lang.String)
   */
  public void warningT(String msg) {
    location.warningT(msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#warningT(java.lang.String, java.lang.Object[])
   */
  public void warningT(String msg, Object[] params) {
    location.warningT(msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#pathT(java.lang.String)
   */
  public void pathT(String msg) {
    location.pathT(msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#pathT(java.lang.String, java.lang.Object[])
   */
  public void pathT(String msg, Object[] params) {
    location.pathT(msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logError(java.lang.String)
   */
  public void logError(String msg) {
    category.errorT(location, msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logError(java.lang.String, java.lang.Object[])
   */
  public void logError(String msg, Object[] params) {
    category.errorT(location, msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logInfo(java.lang.String)
   */
  public void logInfo(String msg) {
    category.infoT(location, msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logInfo(java.lang.String, java.lang.Object[])
   */
  public void logInfo(String msg, Object[] params) {
    category.infoT(location, msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logWarning(java.lang.String)
   */
  public void logWarning(String msg) {
    category.warningT(location, msg);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#logWarning(java.lang.String, java.lang.Object[])
   */
  public void logWarning(String msg, Object[] params) {
    category.warningT(location, msg, params);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#traceThrowable(java.lang.Throwable)
   */
  public void traceThrowable(String msg, Throwable throwable) {
    LoggingHelper.traceThrowable(Severity.PATH, location, msg, throwable);
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#throwing(java.lang.Throwable)
   */
  public void throwing(Throwable throwable) {
    location.throwing(throwable);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.ShmThreadLogger#catching(java.lang.Throwable)
   */
  public void catching(Throwable throwable) {
    location.catching(throwable);
  }
  
  public void setClusterId(int cid) {
    clusterId = cid;
  }
  

}
