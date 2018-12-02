package com.sap.sl.util.dbaccess.impl;

import java.util.ArrayList;


class TableEntry
{
   private String    id;                        // Selection id
   private String    name;                      // table name
   private ArrayList fields = new ArrayList();   // list of field names

   TableEntry(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public String getId()
   {
      return id;
   }

   public String getTableName()
   {
      return name;
   }

   public String getTableNameQuoted()
   {
      return ("\""+name.toUpperCase()+"\"");
   }

   public int getFieldCount()
   {
      return fields.size();
   }

   public void addField(NameValuePair pair)
   {
      fields.add(pair);
   }
   
   public void setValue(NameValuePair pair)
   {
     int i;
     
     for (i = 0; i < fields.size(); i++)
     {
       if (((NameValuePair)fields.get(i)).getName().equals(pair.getName()))
         fields.set(i,pair);
     }
   }

   public String getFieldName(int index) throws IndexOutOfBoundsException
   {
      return ((NameValuePair) fields.get(index)).getName();
   }

   public String getFieldNameQuoted(int index) throws IndexOutOfBoundsException
   {
      return "\""+this.getFieldName(index).toUpperCase()+"\"";
   }
  
  public String getFieldValue(int index) throws IndexOutOfBoundsException
  {
     return ((NameValuePair) fields.get(index)).getValue();
  }
 
  public String getValueQuoted(int index) throws IndexOutOfBoundsException
  {
     return "\""+this.getFieldValue(index)+"\"";
  }
  
  public String toXML()
  {
	int i;
	String result = "<table name="+getTableNameQuoted()+"> ";
	
	result = result + "<entry> ";
    
	for (i = 0; i < fields.size(); i++)
    {
      result = result + "<field name="+getFieldNameQuoted(i)+" value="+getValueQuoted(i)+ "/> ";
    }
	
	result = result + " </entry> ";
	result = result + " </table> ";
	
    return result;
  }
}
