package com.sap.dictionary.database.dbs;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbMsgHandler implements DbsConstants {
	public static final ResourceBundle BUNDLE =
	   ResourceBundle.getBundle(BUNDLE_NAME);	

   public static String get(String msgCode) {
   	  return BUNDLE.getString(msgCode);
   }	
   
   public static String get(Object msgCode) {
   	  return BUNDLE.getString((String) msgCode);
   }
   
   public static String get(String msgCode, Object[] args) {
   	 MessageFormat formatter = new MessageFormat(BUNDLE.getString(msgCode));
   	 return formatter.format(args);
   }
   
   public static String get(Object msgCode, Object[] args) {
   	 MessageFormat formatter = new MessageFormat(
   	                    BUNDLE.getString((String) msgCode));
   	 return formatter.format(args);
   }	
}
