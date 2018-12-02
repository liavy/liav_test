package com.sap.dictionary.database.friendtools;

import java.io.InputStream;
import java.util.Map;

import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.JddRuntimeException;
/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface FriendTools {
	/**
	  *  Creates a table according to its descripiton in the xml given
	  *  in this InputStream
	  *  @param name Name of table
      *  @param stream xml-File as stream
	  *  @return true - if modify-operation was possible, false otherwise
	  **/  	
	public boolean createTable(String name, InputStream stream);
	
	/**
	  *  Drops table with given name from the database
	  *  @param name Name of table
	  *  @return true - if modify-operation was possible, false otherwise
	  **/  	
	public boolean dropTable(String name);
	   
	/**
	  *  Modifies a table according to its descripiton in the xml given
	  *  in this InputStream
	  *  @param name Name of table
      *  @param stream xml-File as stream
	  *  @return true - if modify-operation was possible, false otherwise
	  **/   
	public boolean modifyTable(String name, InputStream stream);	

	/**
	 *  Copies a stream from original table to destination table, where
	 *  source and destination table are defined via ITranslator
	 *  @param names names of xmls
	 *  @return true - if copy was possible, false otherwise
	 *  @throws JddRuntimeException if copy for an xml is not possible
	 **/	
	public boolean xmlCopy(String[] names);

	/**
	 *  Copies a stream from original table to destination table, where
	 *  source and destination table are defined via ITranslator
	 *  The copy operation is exectued for tables which are in original table 
	 *  but nor in destination table
	 *  @deprecated use xmlCopy(String[]) instead  
	 *  @return true - if copy was possible, false otherwise
	 *  @throws JddRuntimeException if copy for an xml is not possible
	 **/	
	public boolean xmlCopy();	
	
	/**
	 * Deletes an xml from the runtime-table
	 * @param name Name of table
	 * @return true - if xml-entry could be deleted, false otherwise
	 * @throws JddRuntimeException if xml could not be deleted
	 **/	
	public boolean xmlDelete(String name);

	/**
     * Gets an xml with the specified name from the runtime-table
     * @param name Name of table
     * @return the xml as String
     * @throws JddRuntimeException if an error occurs during reading
     **/    
	public String xmlRead(String name);
	
  /**
   * Writes stream to runtime-table. Database positions are accepted
   * as written to xml-file and are not changed.
   * 
   * @param name Name of table
   *        stream xml-File as stream
   * @return true - if stream could be written, false otherwise
   * @throws JddRuntimeException if xml could not be written
   */
   public boolean xmlWrite(String name, InputStream stream);
 
  /**
   * Writes stream to runtime-table. Database positions are newly set.
   * The method only works if the database table exists
   * 
   * @param name Name of table
   *        stream xml-File as stream
   * @return true - if stream could be written, false otherwise
   * @throws JddRuntimeException if xml could not be written
   */   
   public boolean xmlWriteSetDatabasePosition(String name, InputStream stream);
   
   /**
    * Computes the maximal necessary action for all tables of the original sda.
    * The information on which databases this action is necessary is also given.
    * The map contains instances of DbChangeInfo for every table.
    * 
    * @param archiveFileNameOrig name of original sda, path included
    *        archiveFileNameDest name of destination sda, path included
    * @return map with tablenames and the maximal action and database info
    * @throws JddRuntimeException error occurs
    */   
   public Map getDifferences(String archiveFileNameOrig,String archiveFileNameDest);
   
   /**
    * Returns the names of all runtime object entries. The source are 	 
    * always the runtimeObjects pointing to BC_DDDBTABLERT and not to
    * the shadow table.
    */
   public String[] getNames();   
}
