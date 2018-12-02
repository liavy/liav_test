package com.sap.sl.util.dbaccess.impl;

import java.util.ArrayList;


public class Selection
{
   private String    id;                        // Selection id
   private String    name;                      // table name
   private ArrayList field = new ArrayList();   // list of field names
   private String    where = null;              // WHERE condition

   private class jdbcField
   {
      String name;
      int type;

      jdbcField(String name)
      {
	 this.name = name;
	 type = 0;
      }

      jdbcField(String name, int type)
      {
	 this.name = name;
	 this.type = type;
      }
   }

   Selection(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   Selection(String id, String name, String where)
   {
      this.id = id;
      this.name = name;
      this.where = where;
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

   public String getWhereCond()
   {
      return where;
   }

   public int getFieldCount()
   {
      return field.size();
   }

   public void resetFields()
   {
      field.clear();
   }

   public void addField(String name)
   {
      field.add(new jdbcField(name));
   }

   public void addField(String name, int type)
   {
      field.add(new jdbcField(name, type));
   }

   public String getFieldName(int index) throws IndexOutOfBoundsException
   {
      jdbcField jf = (jdbcField) field.get(index);
      return jf.name;
   }

   public String getFieldNameQuoted(int index) throws IndexOutOfBoundsException
   {
      jdbcField jf = (jdbcField) field.get(index);
      return "\""+jf.name.toUpperCase()+"\"";
   }

   public int getFieldType(int index) throws IndexOutOfBoundsException
   {
      jdbcField jf = (jdbcField) field.get(index);
      return jf.type;
   }

   /** create a jar entry name out of a Selection */
   public String getJarEntryName(boolean dataentry)
   {
     if (dataentry)
       return(this.getId() + "_" + this.getTableName() + ".dat");
     else
       return(this.getId() + "_" + this.getTableName() + ".txt");
   }
   
   public String toXML()
   {
	 String result;
	 
	 if (getWhereCond() == null)
	   result = "<table name="+getTableNameQuoted()+" /> ";	
	 else
	   result = "<table name="+getTableNameQuoted()+"> " 
                + "<wc text=\""+getWhereCond()+"\"/>"
                + " </table> ";	
	 return result;
   }
}
