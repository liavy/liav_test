package com.sap.archtech.archconn.jobbeans;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.sap.archtech.archconn.values.IndexPropValues;

/**
 * The <code>IDeleteJobBeanMethods</code> defines methods that have to be implemented by every archiving delete job bean.
 */
public interface IDeleteJobBeanMethods
{
  /**
   * Delete a given archived object from the database of the application system.
   * The archived object is passed as XML stream and must be de-serialized first. 
   * Implementors must create an appropriate SQL DELETE statement from the attributes of the archived object. 
   * @param archivedObjectStream The serialized archived object.
   * @param conn Connection to the database. Do not commit, rollback or close this connection inside this method!
   * @param jobLogger To be used if any logging is required.
   * @throws SQLException Thrown if any database-related problem occurs during deletion.
   */
  public void deleteByXmlDeserialization(InputStream archivedObjectStream, Connection conn, Logger jobLogger) throws SQLException;
  
  /**
   * Delete a given archived object from the database of the application system.
   * Only the index property values are available that had been attached to the object during the archiving write phase.
   * No de-serialization is required.
   * Implementors must create an appropriate SQL DELETE statement from the given index property values.
   * @param indexPropValues The index property values of the given archived object.
   * @param conn Connection to the database. Do not commit, rollback or close this connection inside this method!
   * @param jobLogger To be used if any logging is required.
   * @throws SQLException Thrown if any database-related problem occurs during deletion. 
   */
  public void deleteByIndexPropertyValues(IndexPropValues indexPropValues, Connection conn, Logger jobLogger) throws SQLException;
}
