package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.logging.api.SlUtilLogger;

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
public class FieldDescription {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(FieldDescription.class.getName());
	
	  private String name;
	  private int type;
	  private long length;

	  public  FieldDescription (String name, int type, long length)
	  {
		this.name = name;
		this.type = type;
		this.length = length;
		log.debug ("FieldDescription n="+name+" t="+type+" l="+length);
	  }
  
	  public String getName()
	  {
		return name;
	  }
  
	  public String getNameQuoted()
	  {
		 return ("\""+name.toUpperCase()+"\"");
	  }

	  public int getType()
	  {
		return type;
	  }
  
	  public long getLength()
	  {
		return length;
	  }
	  
	  public static String getFieldDescriptionString (FieldDescription field) {
	  	if (field == null) {
	  		return "NullField";
	  	}
	  	return field.toString();
	  }
	  
	  public String toString () {
	  	return "n="+name+" t="+type+" l="+length;
	  }
	}