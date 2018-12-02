package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;
import java.io.*;

/**
 * Title:        Analysis of table and view changes: Classes for Dummy database
 * Description:  Work with dummy-database
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author A. Neufeld & T. Wenner
 * @version 1.0
 */

public class DbArtTable extends DbTable {

  public DbArtTable() {super();}

  public DbArtTable(DbFactory factory) {super(factory);}
  
  public DbArtTable(DbFactory factory, DbTable other) {super(factory,other);}

  public DbArtTable(DbFactory factory,DbSchema schema,String name) {
  	super(factory,schema,name);}

  public DbArtTable(DbFactory factory,String name) {super(factory,name);}

  //TODO: Methods have to be implemented by database-goups
  public void setTableSpecificContentViaXml(XmlMap xmlMap) {}

  public void setPrimaryKeyViaDb() {}

  public void setIndexesViaDb() {}

  public void setTableSpecificContentViaDb() {}

  public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0) {}

  public boolean existsOnDb() throws JddException {return false;}
  
  public boolean existsData() throws JddException {return false;}

  public String toString() {return  super.toString();}
}
