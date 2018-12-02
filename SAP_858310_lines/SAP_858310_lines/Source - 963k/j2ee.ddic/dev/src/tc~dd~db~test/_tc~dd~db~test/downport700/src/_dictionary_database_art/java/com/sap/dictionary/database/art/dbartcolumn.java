package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: Oracle specific classes
 * Description:  Oracle specific analysis of table and view changes. Tool to deliver Oracle specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author A. Neufeld & T. Wenner
 * @version 1.0
 */

  public class DbArtColumn extends DbColumn {

 //Empty constructor
  public DbArtColumn() {super();}

  public DbArtColumn(DbFactory factory) {super(factory);}

  public DbArtColumn(DbFactory factory,DbColumn other) {
  	super(factory,other);}

  //Constructor including src-Type that means java-Type
  public DbArtColumn(DbFactory factory,XmlMap xmlMap) {
    super(factory,xmlMap);
  }

  //Constructor excluding src-Type that means java-Type. Recommended if origin is
  //database which does not know how a database-table's column is used in java
  public DbArtColumn(DbFactory factory,String name,int position,
                       int javaSqlType,String dbType,long length,int decimals ,
                       boolean isNotNull,String defaultValue) {
    super(factory,name,position,javaSqlType,dbType,length,decimals,isNotNull,
                   defaultValue);
  }

  protected boolean acceptedAdd() {return false;}

  protected boolean acceptedDrop() {return false;}

  protected DbColumnDifference comparePartTo(DbColumn target)
    throws Exception {return null;}
}