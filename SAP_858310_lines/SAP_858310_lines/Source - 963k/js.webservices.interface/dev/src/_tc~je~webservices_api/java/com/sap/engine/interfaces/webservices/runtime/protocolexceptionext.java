package com.sap.engine.interfaces.webservices.runtime;

/**
 * Extended Protocol Exception which carries the logId of the exception cause.
 * @author I056242
 *
 */
public class ProtocolExceptionExt extends ProtocolException {
  
  /**
   * LogId record of the cause of the exception.
   */
  private String logId;

  /**
   * 
   */
  private static final long serialVersionUID = 1292211090202229358L;
  
  
  public ProtocolExceptionExt() {
  }

  /**
   * @param cause
   */
  public ProtocolExceptionExt(Throwable cause) {
    super(cause);

  }

  public ProtocolExceptionExt(String s) {
    super(s);
  }
    
  /**
   * @param message
   * @param cause
   */
  public ProtocolExceptionExt(String message, Throwable cause) {
    super(message, cause);
  }

  

  /**
   * @param cause
   * @param logId the logId to set
   */
  public ProtocolExceptionExt(Throwable cause, String logId) {
    super(cause);
    
    this.setLogId(logId);
  }

  /**
   * @param message 
   * @param logId the logId to set
   */
  public ProtocolExceptionExt(String message, String logId) {
    super(message);
    
    this.setLogId(logId);
  }
    
  /**
   * @param message
   * @param cause
   * @param logId the logId to set
   */
  public ProtocolExceptionExt(String message, Throwable cause, String logId) {
    super(message, cause);
    
    this.setLogId(logId);
  }

  /**
   * @param logId the logId to set
   */
  public void setLogId(String logId) {
    this.logId = logId;
  }

  /**
   * @return the logId
   */
  public String getLogId() {
    return logId;
  }


}
