/*
 * Created on 17.01.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package com.sap.sl.util.cvers.impl;
import java.util.ArrayList;

import com.sap.sl.util.logging.api.SlUtilLogger;

public class TableDescription {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(TableDescription.class.getName());

	/*
	 * Created on 29.01.2004
	 *
	 * Description of a table field
	 */
	  private String name;
	  private ArrayList fieldlist = new ArrayList();

	  public  TableDescription (String name)
	  {
	  	log.debug ("TableDescription name='"+name+"'");
		this.name = name;
	  }
  
	  public String getName()
	  {
		return name;
	  }
  
	  public String getNameQuoted()
	  {
		 return ("\""+name.toUpperCase()+"\"");
	  }
  
	  public void addFieldDescription(FieldDescription fdesc)
	  {
	  	log.debug ("addFieldDescription "+FieldDescription.getFieldDescriptionString(fdesc));
		fieldlist.add(fdesc);
	  }
  
	  public int getFieldCount()
	  {
	  	log.debug ("getFieldCount "+fieldlist.size());
		return fieldlist.size();
	  }
  
	  public FieldDescription getFieldDescription(int i)
	  {
		log.debug ("getFieldDescription "+i+" = "+FieldDescription.getFieldDescriptionString((FieldDescription)fieldlist.get(i)));
		return (FieldDescription)fieldlist.get(i);
	  }
  
	  public int getFieldIndex(String fieldname)
	  {
		int retval = -1;
    
		for (int i=0; i < getFieldCount(); i++)
		  if (getFieldDescription(i).getName().equals(fieldname)) {
			log.debug ("getFieldIndex "+fieldname+" ==> "+i);
			return i;
		  }
    
		log.debug ("getFieldIndex "+fieldname+" NOT FOUND");
		return retval;
	  }
	  
	  public String toString () {
	    String retval = name + "<br>";
		for (int i=0; i < fieldlist.size(); i++) {
			retval = retval + i+" = "+fieldlist.get(i)+"\n";
		};
		return retval; 
	  }
	}