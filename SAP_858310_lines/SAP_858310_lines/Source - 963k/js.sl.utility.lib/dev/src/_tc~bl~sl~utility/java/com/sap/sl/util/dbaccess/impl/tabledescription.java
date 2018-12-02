package com.sap.sl.util.dbaccess.impl;

import java.util.ArrayList;

/*
 * Created on 29.01.2004
 *
 * Description of a table field
 */
 
class TableDescription
{
  private String name;
  private ArrayList fieldlist = new ArrayList();

  TableDescription (String name)
  {
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
    fieldlist.add(fdesc);
  }
  
  public int getFieldCount()
  {
    return fieldlist.size();
  }
  
  public FieldDescription getFieldDescription(int i)
  {
    return (FieldDescription)fieldlist.get(i);
  }
  
  public int getFieldIndex(String fieldname)
  {
    int retval = -1;
    
    for (int i=0; i < getFieldCount(); i++)
      if (getFieldDescription(i).getName().equals(fieldname))
        return i;
    
    return retval;
  }
}