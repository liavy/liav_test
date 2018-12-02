package com.sap.archtech.archconn.util;

/**
 * 
 * Status Codes defined by the XML DAS.
 *
 *@author     D025792
 */
public class DasResponse
{
   public final static int SC_OK = 200;

   /**
    * Status code (409) indicating that a XML DAS object
    * (e.g. resource, collection or property) already exists
    *  with the same name.
    */
   public final static int SC_CONFLICT = 409;

   /**
    * Status code (499) indacating that a value of a parameter
    * is syntactically wrong; mostly missing or wrong / or \ e.g. / 
    * in a collection name.
    */
   public final static int SC_PARAMETER_SYNTAX_INCORRECT = 499;
   /**
    * Status code (498) indicating that a parameter is expected but
    * not provided by the application, e.g. property missing in SELECT,
    * non optional attribute not given etc.
    */
   public final static int SC_PARAMETER_MISSING = 498;
   /**
    * Status code (497) indicating that a parameter contains a value different
    * from predefined key words; e.g. check_level='Parser' (instead of 'Parse').
    */
   public final static int SC_KEYWORD_UNKNOWN = 497;
   /**
    * Status code (496) indicating that a combination of parameters is not possible.
    */
   public final static int SC_PARAMETERS_INCONSISTENT = 496;
   /**
    * Status code (495) indicating that the value of a (string) parameter exceeds
    * the limit.
    */
   public final static int SC_PARAMETER_TOO_LONG = 495;
   /**
    * Status code (494) indicating that the type of resource or action
    * is not supported for this type (PUT, LIST).
    */
   public final static int SC_RESOURCE_TYPE_NOT_SUPPORTED = 494;
   /**
    * Status code (493) indicating that a XML resource is not well-formed
    * or the validation for this resource failed or the schema is not available
    * for validation.
    */
   public final static int SC_CHECK_FAILED = 493;
   /**
    * Status code (492) indicating that a PUT/MKCOL for a 'frozen'
    * collection was requested.
    */
   public final static int SC_COLLECTION_FROZEN = 492;
   /**
    * Status code (491) indicating that a PUT/MKCOL for a system collection 
    * or a DELETE for a System/Home collection was requested.
    */
   public final static int SC_SYSTEM_COLLECTION = 491;
   /**
    * Status code (490) indicating that the property MANDT is missing
    * for a client depending proprty index.
    */
   public final static int SC_CLIENT_MISSING = 490;
   /**
    * Status code (489) indicating that the http request body does not meet
    * specification; e.g. body empty or otherwise not interpretable by XML DAS.
    */
   public final static int SC_BODY_FORMAT_CORRUPT = 489;
   /**
    * Status code (488) indicating that a expected resource, collection or
    * index does not exist. 
    */
   public final static int SC_DOES_NOT_EXISTS = 488;
   /**
    * Status code (487) indicating that a property name is not allowed
    * (INDEX CREATE). 
    */
   public final static int SC_PROPERTY_NAME_FORBIDDEN = 487;
   /**
    * Status code (486) indicating that a property of type java.sql.Timestamp is
    * not in format yyyy-mm-dd hh:mm:ss.fff (INDEX INSERT, PUT).
    */
   public final static int SC_WRONG_TIMESTAMP_FORMAT = 486;
   /**
    * Status code (485) indicating that a DELETE via SYNC_HOME_PATH finds
    * resources below Home Collection.
    */
   public final static int SC_HOME_NOT_EMPTY = 485;
   /**
    * Status code (484) indicating that a SYNC_HOME_PATH
    * finds a home collection that has already the specified name.
    */
   public final static int SC_HOME_COLLECTION_EXISTS = 484;
   /**
    * Status code (483) indicating an invalid character in a proposed
    * name for a collection, resource or URI.
    */
   public final static int SC_INVALID_CHARACTER = 483;
   /**
    * Status code (482) indicating a missing index property
    * in PUT or INDEX INSERT. 
    */
   public final static int SC_INDEX_PROPERTY_MISSING = 482;
   /**
    * Status code (481) indicating that PICK finds nor more
    * resources for deletion in the specified collection.
    */
   public final static int SC_NO_MORE_OBJECTS = 481;
   /**
   	* Status code (480) indicating that in index property
   	* in an PUT or INDEXINSERT command has is not of the same
   	* type as defined during the INDEXCREATE for that index.
   	*/
   public final static int SC_WRONG_INDEX_PROPERTY_TYPE = 480;
   /**
    * Status code (479) indicating that a given index does not exist
    * or does not contain a certain index property (LIST command).
    */
   public final static int SC_WRONG_PROPERTY_OR_INDEX_IN_LIST = 479;
   /**
    * Status code (478) indicating that the client has requested the 
    * cancellation of a running Write or Delete Session. This status code
    * is used by the PUT and PICK commands.
    */
   public final static int SC_CANCEL_REQUESTED = 478;
   /**
    * Status code (599) indiaction that a database/OpenSQL
    * problem occured.
    */
   public final static int SC_SQL_ERROR = 599;
   /**
    * Status code (598) indicating that an I/O error ocuured
    * (file system, WebDAV, HTTP protocol etc.).
    */
   public final static int SC_IO_ERROR = 598;
   /**
    * Status code (597) indicating that configuration or meta data 
    * in the archiving service is inconsistent (archive store ID not found,
    * DB meta data not readable in INDEX_CREATE etc.).
    */
   public final static int SC_CONFIG_INCONSISTENT = 597;
   /**
    * Status code (596) indicating that archived data must have changed
    * according to check sum.
    */
   public final static int SC_CHECKSUM_INCORRECT = 596;
   /**
    * Status code (595) indicating that an error reading resources 
    * from a packed AXML-file occured.
    */
   public final static int SC_WRONG_OFFSET = 595;
   /**
    * Status code (594) indicating that the specified collection
    * is not assigned to an archive store (MKCOL, MODIFYPATH).
    */
   public final static int SC_NO_STORE_ASSIGNED = 594;
   /**
    * Status code (593) indicating that the specified collection
    * is not assigned to an archive store (PUT).
    */
   public final static int SC_STORE_NOT_ASSIGNED = 593;
   /**
    * Status code (592) indicating that a WebDAV property cannot be updated.
    */
   public final static int SC_CANNOT_UPDATE_WEBDAV_PROPERTY = 592;
}
