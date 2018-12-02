package com.sap.dictionary.database.dbs;

import java.text.MessageFormat;
import java.util.*;
import com.sap.tc.logging.*;
/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MessageFormatter extends TraceFormatter {
  private static HashMap messages = new HashMap();	
  private static HashSet outMessages = new HashSet();	
	
  public MessageFormatter() {setPattern("%24d %l[%s] %m");}		
	
  public void setPattern(String pattern) {
  	super.setPattern(pattern); 
  }	
	
  public String format(LogRecord log) {
  	messages.put(log.getMsgClear(),log.getArgs()); 	
  	return super.format(log); 	 
  }
  
  public static HashSet getMessages() {
  	Set keyset = messages.keySet();
  	Iterator iter = keyset.iterator();
  	Object[] arguments = new Object[4];
  	String mess = null;
  	MessageFormat form = null;
  	List list = null;
  	int i = 0;
  	while (iter.hasNext()) {
  	  mess = (String) iter.next();
  	  form = new MessageFormat(mess);	
      list = (java.util.List) messages.get(mess);	 	
      Iterator listIter = list.iterator();
      i = 0;
      while (listIter.hasNext()) {
      	arguments[i] = listIter.next();
      	i = i + 1;
      }
      outMessages.add(form.format(arguments));	
  	}
  	return outMessages;	
  }
}
