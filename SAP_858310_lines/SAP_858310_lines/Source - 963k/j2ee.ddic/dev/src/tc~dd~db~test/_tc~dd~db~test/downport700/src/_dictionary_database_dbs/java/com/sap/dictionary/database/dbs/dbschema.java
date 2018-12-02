package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbSchema {
  String schemaName = null;
	
  public DbSchema() {super();}

  public DbSchema(String schemaName) {
  	if (schemaName != null)
      if (!schemaName.trim().equalsIgnoreCase(""))
        this.schemaName = schemaName;}
  
  public String getSchemaName() {return schemaName;}
  
  public boolean check() {return checkNameLength();}
  
 /**
   *  Check the schema's name according to its length  
   *  @return true - if name-length is o.k
   * */  
  public boolean checkNameLength() {return true;}
}
