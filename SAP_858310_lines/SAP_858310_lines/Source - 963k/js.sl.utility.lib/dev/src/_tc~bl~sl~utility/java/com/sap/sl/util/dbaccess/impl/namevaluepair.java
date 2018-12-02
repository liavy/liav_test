package com.sap.sl.util.dbaccess.impl;

public class NameValuePair
{
  private String    name;
  private String    value;

  NameValuePair(String name, String value)
  {
    this.name = name;
    this.value = value;
  }

  public String getName()
  {
    return name;
  }

  public String getValue()
  {
    return value;
  }

  public String getNameQuoted()
  {
    return ("\""+name.toUpperCase()+"\"");
  }
  
  public String getValueQuoted()
  {
    return ("'"+value+"'");
  }

}
