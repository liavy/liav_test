package com.sap.engine.core.thread;

public class LogHelper {
	
	/**
	   * 
	   * @param message
	   * @param args	Values to be placed in the text message.
	   * @return
	   */
	  public static String buildMessage(String message, Object... args) {
	      if (args != null) {
	          StringBuilder messageBuilder = new StringBuilder(message);
	          int ix = 0;
	          for (Object obj : args) {
	              int ii = messageBuilder.indexOf("{" + ix + "}");
	              if (ii > -1) {
	                  messageBuilder.replace(ii, ii + 3, (obj == null) ? "null" : obj.toString());                    
	              }
	              ix++;
	          }
	          message = messageBuilder.toString();
	      }
	      
	      return message;
	  }
}
