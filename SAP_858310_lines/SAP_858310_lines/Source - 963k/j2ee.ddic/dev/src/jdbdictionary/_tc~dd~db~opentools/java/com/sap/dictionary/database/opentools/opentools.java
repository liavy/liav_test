package com.sap.dictionary.database.opentools;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import com.sap.dictionary.database.dbs.JddException;
/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface OpenTools {

  /**
   *  Tries to create the table of name <name> according to the 
   *  file. If the table does exist this will lead to an error.
   *  @param name - the name of the table to create 
   *         file - the table's definition (a gdbtable-file is expected)
   *  @return true, if table could be created, false otherwise 
   **/	
  public boolean createTable(String name,File file);
  
  /**
   *  Tries to reate the table of name <name> according to the 
   *  stream. If the table does exist this will lead to an error.
   *  @param name - the name of the table to create 
   *         stream - the table's definition (a gdbtable-file is expected)
   *  @return true, if table could be created, false otherwise 
   **/	
  public boolean createTable(String name,InputStream stream);
  
  /**
   *  Tries to reate the table of name <name> according to the 
   *  stream. Additionally it can be choosen if primary key and indexes
   *  should also be created.
   *  If the table does exist this will lead to an error.
   *  @param name - the name of the table to create 
   *         stream - the table's definition (a gdbtable-file is expected)
   *         withoutPrimaryKey - if value is true primary key will not be created even if its definition
   *                             exists in stream, if value is true, primary key is created as described
   *         withoutIndexes - if value is true, no index will be created even if defined in stream
   *                          if value is false, indexes are created as described
   *  @return true, if table could be created, false otherwise 
   **/  
  public boolean createTable(String name,InputStream stream,boolean withoutPrimaryKey,
                             boolean withoutIndexes);
  
  /**
   *  Generates the create-statement of table <name>. The table definition
   *  is taken from control table. If the table does not exist in control 
   *  table this will lead to null as result.
   *  @param name - the name of the table to create 
   *  @return statement string if table exists or null otherwise
   **/  
  public String getTableCreateStatementFromDb(String name);
  
  /**
   *  Drops table of name <name>  
   *  @param name - the name of the table to delete 
   *  @return true, if table could be deleted or table does not exist yet, false otherwise 
   **/	
  public boolean dropTable(String name);
  
  /**
   *  Tries to create view of name <name> according to the definition 
   *  given in file. If the table does exist this will lead to an error.
   *  @param name - the name of the view to create 
   *         file - the view's definition (a gdbview-file is expected)
   *  @return true, if view could be created, false otherwise 
   **/	
  public boolean createView(String name,File file);
  
  /**
   *  Tries to create view of name <name> according to the definition 
   *  given in the stream. If the table does exist this will lead to an error.
   *  @param name - the name of the view to create 
   *         stream - the view's definition (a gdbview-file is expected)
   *  @return true, if view could be created, false otherwise 
   **/	
  public boolean createView(String name,InputStream stream);
  
  /**
   *  Drops view of name <name>  
   *  @param name - the name of the view to delete 
   *  @return true, if view could be deleted or view does not exist yet, false otherwise 
   **/
  public boolean dropView(String name);
  
  /**
   *  Compares the tables of the two archives with the given names. The file names
   *  have to be given with path. ClassLoader has to have access to these files.
   *  @param archiveFileNameOrig - name of file containing the objects with old structure
   *  @param archiveFileNameDest - name of file containing the objects with destination structure  
   *  @return HashMap with action for each table in destination archive. If action
   *          is null this object did not exist in original archive 
   **/  
  public HashMap checkTableStructure(String archiveFileNameOrig, String archiveFileNameDest);
}
