package com.sap.archtech.archconn.jobbeans;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;

import com.sap.archtech.archconn.values.IndexPropDescription;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.scheduler.runtime.JobContext;

/**
 * The <code>IWriteJobBeanMethods</code> defines methods that have to be implemented by every archiving write job bean.
 */
public interface IWriteJobBeanMethods
{
  /**
   * Load the objects to be archived according to the search parameters passed within the job context.
   * The structure of those objects is archiving-set-specific.
   * @param jobCtx The job context containing all job parameters. Only the search-relevant job parameters are needed in this method.
   * @param jobLogger To be used if any logging is required. 
   * @return A collection of objects representing of the database entries to be archived. 
   * @throws SQLException Thrown if any database-related problem occurs 
   */
  public Collection loadArchivableObjects(JobContext jobCtx, Logger jobLogger) throws SQLException;
  
  /**
   * Create an XML stream for the given object to be archived.
   * The XML serialization for the archiving set has to be implemented here.
   * @param archivableObject Object to be archived. In case there is no object representation
   * for a specific archiving set, this parameter should contain one database entry to be archived (= an object retrieved by calling <code>ResultSet.next()</code>).
   * @param jobLogger To be used if any logging is required.
   * @return A stream containing the XML representation of all objects/databse entries to be archived.
   */
  public ByteArrayInputStream createXMLStream(Object archivableObject, Logger jobLogger);
  
  /**
   * Create a property index for the given archiving set.
   * @param jobLogger To be used if any logging is required.
   * @return An instance of <code>IndexPropDescription</code> to be attached to the <code>INDEXCREATE</code> command
   */
  public IndexPropDescription createPropertyIndex(Logger jobLogger);
  
  /**
   * Fill the property index from the content of a given archivable object.
   * @param archivableObject The object to be archived.
   * @param jobLogger To be used if any logging is required.
   * @return An instance of <code>IndexPropValues</code> to be attached to the <code>PUT</code> command.
   */
  public IndexPropValues fillIndexPropertyValues(Object archivableObject, Logger jobLogger);
}
