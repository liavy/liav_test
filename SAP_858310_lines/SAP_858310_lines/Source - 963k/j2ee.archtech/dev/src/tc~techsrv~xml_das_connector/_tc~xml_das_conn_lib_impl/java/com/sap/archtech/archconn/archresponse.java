package com.sap.archtech.archconn;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.response.IGenericHttpResponseMethods;
import com.sap.archtech.archconn.response.IGenericResponseMethods;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.ColSearchResult;
import com.sap.archtech.archconn.values.IndexPropDescription;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.archconn.values.LegalHoldValues;
import com.sap.archtech.archconn.values.ResourceData;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.archtech.archconn.values.TechResKey;

/**
 * Response of the XML Data Archiving Service on the execution of an archiving
 * command.
 */
public interface ArchResponse extends IGenericResponseMethods, IGenericHttpResponseMethods
{
  /**
   * Resource is valid against specified XSD schema.
   */
  public String RESOURCE_VALID = "V";

  /**
   * Resource is not valid against specified XSD schema.
   */
  public String RESOURCE_INVALID = "I";

  /**
   * Resource is well formed (could be parsed).
   */
  public String RESOURCE_WELLFORMED = "W";

  /**
   * Resource is not well formed (could not be parsed)
   */
  public String RESOURCE_NOTWELLFORMED = "N";

  public ArrayList<? extends Serializable> getArchAdminData() throws IOException, UnsupportedCommandException;
  
  /**
   * Returns an input stream for accessing the response body. Returns null if no
   * body is available.
   * 
   * @return Response body
   * @throws IOException
   *               if the response body could not be accesses OR if an error
   *               occurred in the XML Data Archiving Service during the processing
   *               of an archiving command.
   */
  public InputStream getBody() throws IOException, UnsupportedCommandException;

  /**
   * Parses the body of a SELECT or LIST request. Metadata information will be
   * stored in ResourceData objects.
   * 
   * @return ArrayList of ResourceData objects resulting from a LIST or SELECT
   *            command
   * @throws IOException
   *               if the response body could not be accessed OR if an error
   *               occurred in the XML Data Archiving Service during the processing
   *               of an archiving command.
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of a LIST or SELECT archiving command.
   */
  public ArrayList<ResourceData> getResourceData() throws IOException, UnsupportedCommandException;
  
  /**
   * Returns the technical key of a resource. Used in PICK.
   * 
   * @return Technical resource key
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of a PICK.
   *  
   */
  public TechResKey getResKey() throws UnsupportedCommandException;

  /**
   * Returns the description of a property index (property names and types).
   * Used in the INDEX DESCRIBE command.
   * 
   * @return Property index description.
   * @see com.sap.archtech.archconn.values.IndexPropDescription
   * @throws IOException
   *               if the response body could not be accessed OR if an error occured
   *               in the XML Data Archiving Service during the processing of an
   *               archiving command.
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of an INDEX DESCRIBE.
   */
  public IndexPropDescription getIndexProps() throws IOException, UnsupportedCommandException;

  /**
   * @deprecated Use {@link #getIndexValuesList()}
   */
  public IndexPropValues getIndexValues() throws IOException, UnsupportedCommandException;

  /**
   * 
   * Returns all property index values for a specified resource. Used for the
   * INDEXGET and PICK commands.
   * 
   * @return Property Index Values for one resource
   * @throws IOException
   *               if the response body could not be accessed OR if an error occured
   *               in the XML Data Archiving Service during the processing of an
   *               archiving command.
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of an INDEXGET or PICK.
   */
  public ArrayList<IndexPropValues> getIndexValuesList() throws IOException, UnsupportedCommandException;

  /**
   * 
   * Get the archiving property values from the response. Used for the
   * PROPERTYGET command.
   * 
   * @throws IOException
   *               if the response body could not be accessed OR if an error occured
   *               in the XML Data Archiving Service during the processing of an
   *               archiving command.
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of a PROPERTYGET.
   */
  public ArchivingPropertyValues getArchivingPropertyValues() throws IOException, UnsupportedCommandException;
  
  /**
   * 
   * Get the values of Legal Hold cases from the response. Used for the
   * LEGALHOLD commands.
   * 
   * @throws IOException
   *               if the response body could not be accessed OR if an error occured
   *               in the XML Data Archiving Service during the processing of an
   *               archiving command.
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of one of the LEGALHOLD commands.
   */
  public LegalHoldValues getLegalHoldValues() throws IOException, UnsupportedCommandException;
  
  /**
   * Returns meta information about an archiving session.
   * 
   * @return Session information,
   * @see com.sap.archtech.archconn.values.SessionInfo
   * @throws UnsupportedCommandException
   *               if it is called on an ArchResponse object, which does not result
   *               from the execution of a SESSIONINFO.
   */
  public SessionInfo getSessionInfo() throws UnsupportedCommandException;

  /**
   * Get the result of a <code>COLSEARCH</code> command
   */
  public ColSearchResult getColSearchResult() throws IOException, UnsupportedCommandException;
}