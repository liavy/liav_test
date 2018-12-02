package com.sap.archtech.archconn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.sap.archtech.archconn.commands.ArchCommandEnum;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.ColSearchClause;
import com.sap.archtech.archconn.values.IndexPropDescription;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.archconn.values.SelectClause;
import com.sap.archtech.archconn.values.TechResKey;

/**
 *
 * Represents all available
 * archiving commands.  Allows the adding of parameters
 * and the execution of commands. Parameters are added to
 * archiving commands with the help of the addParam(...) methods.
 * Archiving commands are processed in the following way:
 * <ul>
 * <li>A new archiving command is created with the help of the
 * ArchSession.createCommand(String) method.
 * <li>Parameters are added to the archiving command. Which parameters are allowed
 * depends on the specified archiving command.
 * <li>The archiving command is executed.
 * <li>The result is aquired as an ArchResponse object.
 * <li>Error handling.
 * </ul>
 * 
 * @author D025792
 * @version 1.0
 * 
 */

public interface ArchCommand
{

   /*
    * Constants for the Archiving Commands
    */

   String AC_CHECK = ArchCommandEnum.CHECK.toString();
   String AC_DELETE = ArchCommandEnum.DELETE.toString();
   String AC_DELTIONMARK = ArchCommandEnum.DELETIONMARK.toString();
   String AC_GET = ArchCommandEnum.GET.toString();
   String AC_HEAD = ArchCommandEnum.HEAD.toString();
   String AC_INDEXCREATE = ArchCommandEnum.INDEXCREATE.toString();
   String AC_INDEXDESCRIBE = ArchCommandEnum.INDEXDESCRIBE.toString();
   String AC_INDEXEXISTS = ArchCommandEnum.INDEXEXISTS.toString();
   String AC_INDEXDROP = ArchCommandEnum.INDEXDROP.toString();
   String AC_INDEXINSERT = ArchCommandEnum.INDEXINSERT.toString();
   String AC_INDEXGET = ArchCommandEnum.INDEXGET.toString();
   String AC_INFO = ArchCommandEnum.INFO.toString();
   String AC_LEGALHOLDADD = ArchCommandEnum.LEGALHOLDADD.toString();
   String AC_LEGALHOLDGET = ArchCommandEnum.LEGALHOLDGET.toString();
   String AC_LEGALHOLDREMOVE = ArchCommandEnum.LEGALHOLDREMOVE.toString();
   String AC_LIST = ArchCommandEnum.LIST.toString();
   String AC_MKCOL = ArchCommandEnum.MKCOL.toString();
   String AC_MODIFYPATH = ArchCommandEnum.MODIFYPATH.toString();
   String AC_PACK = ArchCommandEnum.PACK.toString();
   String AC_PACKSTATUS = ArchCommandEnum.PACKSTATUS.toString();
   String AC_PICK = ArchCommandEnum.PICK.toString();
   String AC_PROPERTYSET = ArchCommandEnum.PROPERTYSET.toString();
   String AC_PROPERTYGET = ArchCommandEnum.PROPERTYGET.toString();
   String AC_PUT = ArchCommandEnum.PUT.toString();
   String AC_RESETDELSTAT = ArchCommandEnum.RESETDELSTATUS.toString();
   String AC_SELECT = ArchCommandEnum.SELECT.toString();
   String AC_SESSIONINFO = ArchCommandEnum.SESSIONINFO.toString();
   String AC_UNPACK = ArchCommandEnum.UNPACK.toString();
   String AC_SYNCHOMEPATH = ArchCommandEnum.SYNCHOMEPATH.toString();
   String AC_ASSIGNARCHSTORE = ArchCommandEnum.ASSIGNARCHIVESTORE.toString();
   String AC_DESTROY = ArchCommandEnum.DESTROY.toString();
   String AC_COLSEARCH = ArchCommandEnum.COLSEARCH.toString();
   String AC_ORIGINSEARCH = ArchCommandEnum.ORIGINSEARCH.toString();
   String AC_ORIGINLIST = ArchCommandEnum.ORIGINLIST.toString();
   
   /*
    * Constants used in the parameters for Archiving Commands
    */
   
   /**
    * No integrity check for the resource.
    */
   String CHECK_LEVEL_NO = "check_level:NO";
   
   /**
    * XML resource will be parsed.
    */
   String CHECK_LEVEL_PARSE = "check_level:PARSE";
   
   /**
    * XML resource will be validated against the provided XML schema.
    */
   String CHECK_LEVEL_VALIDATE = "check_level:VALIDATE";
   
   /**
    * Delete a complete collection including all resources.
    */
   String DELETE_RANGE_COL = "delete_range:COL";
   
   /**
    * Delete only the specified resource.
    */
   String DELETE_RANGE_RES = "delete_range:RES";
   
   /**
    * Deliver the resource back as a stream.
    */
   String GET_MODE_DELIVER = "mode:DELIVER";
   
   /**
    * Read the resource only in the XML DAS. Do not
    * deliver the resource back.
    */
   String GET_MODE_NODELIVER = "mode:NODELIVER";
   
   /**
    * Recalculate the fingerprint while reading the resource and 
    * check against the fingerprint stored in the XML DAS
    * database for this resource.
    */
   String FP_CHECK = "checksum:Y";
   
   /**
    * Ignore the fingerprint.
    */
   String FP_NOCHECK = "checksum:N";
   
   /**
    * Store the resource actually into the archive.
    */
   String PUT_MODE_STORE = "mode:STORE";
   
   /**
    * Simulate the PUT. Do not store the resource into
    * the archive.
    */
   String PUT_MODE_NOSTORE = "mode:NOSTORE";
   
   /**
    * Search the archiving hierarchy recursivly down
    * starting at the specified collection.
    */
   String SEARCH_MODE_RECURSIVE = "recursive:Y";
   
   /**
    * Search only in the specified collection.
    */
   String SEARCH_MODE_NONRECURSIVE = "recursive:N";
   
   /**
    * List only qualifying resources.
    */
   String LIST_RANGE_RES = "list_range:RES";
   
   /**
    * List only qualifying collections
    */
   String LIST_RANGE_COL = "list_range:COL";
   
   /**
    * LIST qualifying resources and collections.
    */
   String LIST_RANGE_ALL = "list_range:ALL";
   
   /**
    * Property index name
    */
   String INDEX_NAME = "index_name";
   
   /**
    * Resource names
    */
   String RESOURCE_NAME = "resource_name";
   
   /**
    * Autonaming for resources.
    */
   String RESOURCE_NAME_AUTO = "resource_name:<auto>";
   
   /**
    * Resources type, e.g. XML, XSD, BIN.
    */
   String RESOURCE_TYPE = "type";
   
   /**
    * Limit the search up to the specified number of
    * results.
    */
   String SEARCH_MAX_HITS = "maxhits";
   
   /**
    * Limit the search resources which are deleted
    * in the local database (LIST and SELECT)
    */
   String DELETED_RES = "del_only:Y";

   /**
    * Adds a String (text-) parameter to the archiving command.
    * The string consist of a key and a value part. The parts are
    * delimited with a colon. This method is only useful in combination
    * with the constants provided by this interface.
    * @param param name of the parameter
    * 
    */
   void addParam(String param);   
   
   /**
    * Adds a String (text-) parameter to the archiving command. The
    * parameter consists of a key part (hfield) describing the name of
    * the parameter and a value part.
    * @param hfield name of the parameter
    * @param value value of the parameter
    * 
    */
   void addParam(String hfield, String value);

   /**
    * Adds a URI parameter to the archiving command
    * @param uri URI to be added
    */
   void addParam(URI uri) throws MethodNotApplicableException;
   
   /**
    * Adds a boolean parameter to the archiving command
    * @param headerField Name of the parameter
    * @param value Boolean value of the parameter
    * @throws MethodNotApplicableException Thrown if the archiving command does not accept boolean parameters
    */
   void addParam(String headerField, boolean value) throws MethodNotApplicableException;

   /**
    * Adds an InputStream object as parameter to the archiving command.
    * The input stream usually contains the XML document to be archived. 
    * @param in InputStream associated with this archiving command 
    * @throws IOException if a streaming error occurs or this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept
    *         an InputStream as a parameter
    */
   void addParam(InputStream in) throws IOException, MethodNotApplicableException;

   /**
    * Adds an IndexPropValues object as parameter to the archiving command.
    * IndexPropValues are used to specify the properties of the XML documents
    * to be archived. 
    * @param ipv IndexPropValue object containing the properties
    * @throws IOException if a streaming error occurs or this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept
    *         IndexPropValues as a parameter 
    */
   void addParam(IndexPropValues ipv) throws IOException, MethodNotApplicableException;

   /**
    * Adds an IndexPropDescription object as parameter to the archiving command.
    * IndexPropDescription objects are used to specify new property indexes.
    * @param ipd IndexPropDescription object containing a property index description
    * @throws IOException if a streaming error occurs or this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept
    *         IndexPropDescription as a parameter
    */
   void addParam(IndexPropDescription ipd) throws IOException, MethodNotApplicableException;

   /**
    * Adds a SelectClause to an archiving command. Only applicable for the SELECT
    * archiving command.
    * @param slc SelectClause describing the search criteria
    * @throws IOException if a streaming error occurs
    * @throws MethodNotApplicableException if the archiving command does not accept
    *         SelectClause as a parameter
    */
   void addParam(SelectClause slc) throws IOException, MethodNotApplicableException;

   /**
    * Adds a technical resource key to an archiving command. Only applicable for the PICK
    * archiving command.
    * @param reskey technical resource key
    * @throws MethodNotApplicableException if the archiving command does not accept
    *         TechResKey as a parameter
    */
   void addParam(TechResKey reskey) throws MethodNotApplicableException;
   
   /**
    * Adds an <code>ArchivingPropertyValues</code> object as parameter to the archiving command.
    * <code>ArchivingPropertyValues</code> objects represent arbitrary name-value-pairs that may be attached to 
    * an archived resource or collection. 
    * @param archPropertyValues <code>ArchivingPropertyValues</code> object containing name-value-pairs
    * @throws IOException if a streaming error occurs or if this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept an
    *         <code>ArchivingPropertyValues</code> as parameter 
    */
   void addParam(ArchivingPropertyValues archPropertyValues) throws IOException, MethodNotApplicableException;
   
   /**
    * Adds a set of URIs as parameter to the archiving command.
    * @throws IOException if a streaming error occurs or if this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept a a set of URIs
    *         as parameter 
    */
   void addParam(Set<URI> uris) throws IOException, MethodNotApplicableException;
   
   /**
    * Adds a <code>ColSearchClause</code> object as parameter to the archiving command.
    * @param colSearchClause The object to be added
    * @throws IOException if a streaming error occurs or if this method is called
    *         a second time on the same archiving command
    * @throws MethodNotApplicableException if the archiving command does not accept an
    *         <code>ColSearchClause</code> as parameter
    */   
   void addParam(ColSearchClause colSearchClause) throws IOException, MethodNotApplicableException;

  /**
    * Execute the archiving command.
    * @throws IOException if a streaming/network error occurs
    */
   void execute() throws IOException;

   /**
    * Fetches archiving response after execution.
    * 
    * @return the ArchReponse of the ArchiveCommand
    */
   ArchResponse getResponse();
}
