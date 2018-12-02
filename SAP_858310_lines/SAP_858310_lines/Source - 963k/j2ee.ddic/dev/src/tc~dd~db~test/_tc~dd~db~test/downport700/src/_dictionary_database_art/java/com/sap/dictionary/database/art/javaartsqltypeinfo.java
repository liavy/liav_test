package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: Oracle specific classes
 * Description:  Oracle specific analysis of table and view changes. Tool to deliver Oracle specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Andrea Neufeld & Tobias Wenner
 * @version 1.0
 */

public class JavaArtSqlTypeInfo extends JavaSqlTypeInfo {

  public JavaArtSqlTypeInfo(DbFactory factory,String name,int intCode) {
  	super(factory,name,intCode);}
}
