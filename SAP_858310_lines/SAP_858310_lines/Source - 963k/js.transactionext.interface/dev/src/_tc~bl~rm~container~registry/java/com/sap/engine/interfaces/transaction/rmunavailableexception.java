package com.sap.engine.interfaces.transaction;

/**
 * Thrown when resource manager is unavailable. 
 * 
 * @author I024163
 *
 */
public class RMUnavailableException extends Exception {
    
	
	/**
	 * Resource manager is removed and not reachable. 
	 */
	public static final int RM_UNREACHABLE = 1;
	/**
	 * Resource manager is temporary not reachable but after some time can be accessed. 
	 */
	public static final int RM_TEMPORARY_UNREACHABLE = 2;
	/**
	 * Resource manager is undeployed and there are no enough properties to connect to backend. 
	 */
	public static final int RM_UNDEPLOYED = 3;
	/**
	 * Resource manager is unreachable because of a problem in container which is responsible for this RM. 
	 */
	public static final int INTERNAL_RM_CONTAINER_ERROR = 4;
	/**
	 * Unexpected problem prevents connection to RM
	 */
	public static final int UNKNOWN = 5;
	
	
	/**
	 * Error code of the reason for this exception.  
	 */
	private int errorCode = UNKNOWN;
	
	/**
	 * Automatically generated field for fast serialization of the exception. 
	 */
	private static final long serialVersionUID = 166389740484730852L;

	/**
	 * @param message the message of the exception 
	 * @param cause nested exception
	 * @param errorCode of the problem
	 */
	public RMUnavailableException(String message, Throwable cause, int errorCode){
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	/**
	 * @return the error code of the problem. 
	 */
	public int getErrorCode(){
		return errorCode;
	}
}
