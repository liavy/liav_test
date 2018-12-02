package com.sap.dictionary.database.dbs;

import java.util.HashMap;
import java.lang.reflect.Constructor;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ErrTexts {

  private static HashMap errorTexts = new HashMap();

  static {
    errorTexts.put("001","Xml for table & could not be read from database");
    errorTexts.put("002","Xml for table & could not be analysed");
    errorTexts.put("003","& name missing in Xml-File");
    errorTexts.put("004","Quotes at end of identifier are missing");
    errorTexts.put("005",">>> Archive could not be read");
    errorTexts.put("006","Runtime-xml of & successfully written");
    errorTexts.put("007","Runtime-xml of & could not be written");
    errorTexts.put("008","Statements could not be executed successfully");
    errorTexts.put("009","Table could not be analysed");
    errorTexts.put("010","Next file could not be read from archive");
    errorTexts.put("011","Runtime table & could not be created");
    errorTexts.put("012","Runtime tables could not be created");
    errorTexts.put("013","Table & does not exist on database");
	  errorTexts.put("014","Table & does not exist on the specified path");
	  errorTexts.put("015","Xml-file for runtime objects could not be read (e.g. SaxParser not available)");  } 

  public static String get(String messageNumber) {
    return (String) errorTexts.get(messageNumber);
  }
  
  public static String concat(String text,String replaceString) {
    int position = text.indexOf('&');
    return text.substring(0,position) + replaceString +
                      text.substring(position+1,text.length());
  }

  public static String concat(String text,String replaceString1,
                                        String replaceString2) {
    String[] replaceStrings = {replaceString1,replaceString2};
    return concat(text,replaceStrings);                                    	
  }
  
  protected static String concat(String text,String[] replaceString) {
  	int position = 0;
  	String before = text;
  	String after = "";
  	for (int i=0;i<replaceString.length;i++) {
      position = before.indexOf('&');
      after  = after + before.substring(0,position) + replaceString[i];
      before = before.substring(position+1,before.length());
  	}    
  	return after;              
  }
  
  protected static Object[] makeObject(String className,String value1,String value2) {
  	Object[] objects = {null,null};
  	try {
  		
	  Class[] argsClass = new Class[] { String.class };
	  Constructor constructor = Class.forName(className).
	                                       getConstructor(argsClass); 
	  Object[] args1 = new Object[] { value1 };
	  Object[] args2 = new Object[] { value2 };
	  objects[0] = constructor.newInstance(args1);
	  objects[1] = constructor.newInstance(args2); 
    }
	catch (Exception ex) {ex.printStackTrace();}
	return objects;
  }	   
}