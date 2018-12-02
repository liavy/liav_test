package com.sap.dictionary.database.dbs;

import java.util.Properties;
import java.util.HashMap;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LoggingConfiguration {
  static Properties  props = new Properties();
  static HashMap configurations = new HashMap();
  static Class cl = null;
  
  static {
  	try {
      cl = Class.forName( "com.sap.dictionary.database.dbs" + "." 
  	                    + "LoggingConfiguration");
  	  props.load(cl.getResourceAsStream("logging/logging.properties"));                        
      configurations.put("default",props);
      props = new Properties();
      //props.load(cl.getResourceAsStream("logging/checklog.properties"));
      props.setProperty("formatter[check]","com.sap.dictionary.database.dbs.MessageFormatter");
      props.setProperty("formatter[check].pattern","%24d %-40l [%t] %s %m");
      props.setProperty("log[file]","FileLog");
      props.setProperty("log[console]","ConsoleLog");
      props.setProperty("log[file].pattern","C:\\temp\\jddlog%g.log");
      props.setProperty("log[file].formatter","formatter[check]");
      props.setProperty("log[console].formatter","formatter[check]");
      props.setProperty("com.sap.dictionary.database.bundleName","com.sap.dictionary.database.dbs.messages.messages");
      props.setProperty("com.sap.dictionary.database.severity","INFO");
      props.setProperty("com.sap.dictionary.database.logs","log[file], log[console]");
      
      props.setProperty("log[catFile].pattern","%t/jddtracelog.log");
      props.setProperty("log[catFile].formatter","formatter[check]");
      props.setProperty("log[catFile]","FileLog");
      props.setProperty("/Jddic/Database.bundleName","com.sap.dictionary.database.dbs.messages.messages");
      props.setProperty("/Jddic/Database.severity","FATAL");
      props.setProperty("/Jddic/Database.logs","log[catFile], log[console]");
      configurations.put("check",props); 
    }
    catch (Exception ex) {ex.printStackTrace();} 
  	/*props = new Properties();
  	props.setProperty("com.sap.dictionary.database.severity","INFO");
  	props.setProperty("log[check]","ConsoleLog");
  	props.setProperty("log[check].formatter","formatter[check]");
  	props.setProperty("formatter[check]","com.sap.dictionary.database.dbs.MessageFormatter");                     
  	props.setProperty("com.sap.dictionary.database.logs","log[check]"); 
  	configurations.put("check",props);*/                    
  }	

  /**
   *  Method to set a new logging-configuration-parameter (key,value)
   *  @param configuration	     the name of the configuration
   *  @param key                the key of the new property
   *  @param value              the properties value   
   * */  		
  public static void setProperty(String configuration,String key,String value) { 
    String newValue = "";	
    if (key == null || value == null) return;
    if (!key.trim().equalsIgnoreCase("") && !value.trim().equalsIgnoreCase("")) {
     if (configurations.containsKey(configuration)) {
     	Properties properties = (Properties) configurations.get(configuration);
     	//if (!properties.containsKey(key)) {
          properties.setProperty(key,value);
          configurations.put(configuration,properties);
     	//}  
        /*else {
          newValue = (String) properties.get(key);
          newValue = newValue + "," + value;
          properties.setProperty(key,newValue);
          configurations.put(configuration,properties);
        }  */  
     }     
     else {
        props = new Properties();
        props.setProperty(key,value);
        configurations.put(configuration,props);	
      }  
    }     
  }	
		
  public static Properties getConfiguration(String configName) {
  	return (Properties) configurations.get(configName);
  }
  
  public static void removeConfiguration(String configName) {
    configurations.remove(configName);
  } 	 
}