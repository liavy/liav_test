/*
 * Created on 29.01.2004
 *
 * Description of a table field
 */
package com.sap.sl.util.dbaccess.impl;

class FieldDescription
{
  private String name;
  private int type;
  private int length;
  private boolean nullable;

  FieldDescription (String name, int type, int length, boolean nullable)
  {
    this.name = name;
    this.type = type;
    this.length = length;
    this.nullable = nullable;
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
  
  public int getLength()
  {
    return length;
  }
  
  public boolean getnullable()
  {
    return nullable;
  }
}
