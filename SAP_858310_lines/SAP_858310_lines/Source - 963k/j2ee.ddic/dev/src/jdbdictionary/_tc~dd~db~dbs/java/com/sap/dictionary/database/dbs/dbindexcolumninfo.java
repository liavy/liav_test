package com.sap.dictionary.database.dbs;

import java.io.*;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbIndexColumnInfo {
  private String name = " ";
  private boolean isDescending = false;

  public DbIndexColumnInfo(String name, boolean isDescending) {
    this.name         = name;
    this.isDescending = isDescending;
  }

  public DbIndexColumnInfo(XmlMap xmlMap) {
    name         = xmlMap.getString("name");
    isDescending = xmlMap.getBoolean("is-descending");
  }

  public String getName() {return name;}

  public boolean isDescending() {return isDescending;}

  public boolean equals(DbIndexColumnInfo other) {
    return (name.equalsIgnoreCase(other.name) &&
            isDescending == other.isDescending);
  }

  public String toString() {
    return "Columnname = " + name + "\n" +
           "isDescending:" + isDescending + "\n" ;
  }

  void writeCommonContentToXmlFile(PrintWriter file,String offset0) throws Exception {

    //begin column-element
    file.println(offset0 + "<column>");

    String offset1 = offset0 + XmlHelper.tabulate();
    file.println(offset1 + "<name>" + name +  "</name>");
    file.println(offset1 + "<is-descending>" + isDescending + "</is-descending>");

    //end column-element
    file.println(offset0 + "</column>");
  }
  
  /**
	*  Checks the table's name
	*  1. Name contains only characters A..Z 0..9 _
	*  2. First Character is of set A..Z 
	*  3. Name <=18
	*  @param name  index columnname to check
	*  @return true - if name is correctly maintained, false otherwise
	**/ 	
	public static boolean checkName(String name) {
      return DbTools.checkName(name,true,true,false,true);
	}
}
