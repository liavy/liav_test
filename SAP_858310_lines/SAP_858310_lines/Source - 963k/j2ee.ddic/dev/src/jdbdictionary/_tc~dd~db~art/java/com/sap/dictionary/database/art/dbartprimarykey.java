package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbArtPrimaryKey extends DbPrimaryKey {
	DbFactory factory = null;
	DbSchema  schema  = null;
	 

  public DbArtPrimaryKey() {}
  
  public DbArtPrimaryKey(DbFactory factory, DbPrimaryKey other) {
  	super(factory,other);}
  
  public DbArtPrimaryKey(DbFactory factory) {super(factory);}
  
  public DbArtPrimaryKey(DbFactory factory, DbSchema schema, String tableName) {
  	super(factory,schema,tableName);
  }

  public void setSpecificContentViaXml(XmlMap xml) {}
  
  public void setCommonContentViaDb() {}
    
  public void setSpecificContentViaDb() {}  
  
  public void writeSpecificContentToXmlFile() {}

  public DbObjectSqlStatements getDdlStatementsForCreate() {return null;}
  
  public DbObjectSqlStatements getDdlStatementsForDrop() {return null;}

}