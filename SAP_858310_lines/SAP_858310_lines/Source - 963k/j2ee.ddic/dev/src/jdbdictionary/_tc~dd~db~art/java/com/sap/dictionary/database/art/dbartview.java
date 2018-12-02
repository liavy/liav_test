package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;
/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbArtView extends DbView implements DbsConstants {
  private DbFactory factory = null;
  private DbSchema schema = null;
  private String name = " ";
					
  public DbArtView() {
  }

  public DbArtView(DbFactory factory) {
	super(factory);
  }

  public DbArtView(DbFactory factory, String name) {
	super(factory,name);
  }

  public DbArtView(DbFactory factory, DbView other) {
	super(factory,other);
  }

  public DbArtView(DbFactory factory, DbSchema schema, String name) {
	super(factory,schema,name);
  }	

  /**
   *  Analyses if view exists on database or not
   *  @return true - if table exists in database, false otherwise
   *  @exception JddException – error during analysis detected	 
   **/
  public boolean existsOnDb() throws JddException {return true;}
  
  /**
   *  gets the base table Names of this view from database and sets it 
   *  for this view 
   *  @exception JddException – error during analysis detected	 
   **/
  public void setBaseTableNamesViaDb() throws JddException {
  }		
}
