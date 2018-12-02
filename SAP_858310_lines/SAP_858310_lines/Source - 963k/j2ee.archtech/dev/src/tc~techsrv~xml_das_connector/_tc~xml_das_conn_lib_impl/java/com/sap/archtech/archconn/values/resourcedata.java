package com.sap.archtech.archconn.values;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sap.archtech.archconn.util.URI;

/**
 * Value class; contains meta data about resources
 * or collections.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class ResourceData
{
  private final String type;
  private final String collectionType;
  private final URI uri;
  private Date creationTime;// lazily  initialized
  private final String unparsedCreationTime;
  private final String user;
  private final long length;
  private final String checkStatus;
  private final boolean isPacked;
  private final boolean isFrozen;
  private final int nrOfResources;

  // ------------
  // Constructors ------------------------------------------------------------
  // ------------

  /**
   * Constructor for a resource
   * 
   */
  public ResourceData(String type, URI uri, String unparsedCreationTime, String user, long length, String checkStatus, boolean isPacked)
  {
    this.type = type;
    this.collectionType = null;
    this.uri = uri;
    this.unparsedCreationTime = unparsedCreationTime;
    this.creationTime = null;
    this.user = user;
    this.length = length;
    this.checkStatus = checkStatus;
    this.isPacked = isPacked;
    this.isFrozen = false;
    this.nrOfResources = -1;
  }

  /**
   * @deprecated Do not use constructors with a <code>Date</code> field
   */
  public ResourceData(String type, URI uri, Date creationTime, String user, long length, String checkStatus, boolean isPacked)
  {
    this.type = type;
    this.collectionType = null;
    this.uri = uri;
    this.unparsedCreationTime = null;
    this.creationTime = creationTime != null ? new Date(creationTime.getTime()) : null;
    this.user = user;
    this.length = length;
    this.checkStatus = checkStatus;
    this.isPacked = isPacked;
    this.isFrozen = false;
    this.nrOfResources = -1;
  }

  /**
   * @deprecated
   */
  public ResourceData(String type, String collectionType, URI uri, Date creationTime, String user, boolean isFrozen)
  {
    this(type, collectionType, uri, creationTime, user, isFrozen, -1);
  }

  /**
   * @deprecated Do not use constructors with a <code>Date</code> field
   */
  public ResourceData(String type, String collectionType, URI uri, Date creationTime, String user, boolean isFrozen, int nrOfResources)
  {
    this.type = type;
    this.collectionType = collectionType;
    this.uri = uri;
    this.creationTime = creationTime != null ? new Date(creationTime.getTime()) : null;
    this.unparsedCreationTime = null;
    this.user = user;
    this.length = 0;
    this.checkStatus = null;
    this.isPacked = false;
    this.isFrozen = isFrozen;
    this.nrOfResources = nrOfResources;
  }

  /**
   * Constructor for a collection
   * 
   */
  public ResourceData(String type, String collectionType, URI uri, String unparsedCreationTime, String user, boolean isFrozen, int nrOfResources)
  {
    this.type = type;
    this.collectionType = collectionType;
    this.uri = uri;
    this.creationTime = null;
    this.unparsedCreationTime = unparsedCreationTime;
    this.user = user;
    this.length = 0;
    this.checkStatus = null;
    this.isPacked = false;
    this.isFrozen = isFrozen;
    this.nrOfResources = nrOfResources;
  }

  // --------------
  // Public Methods ----------------------------------------------------------
  // --------------

  /**
   * @return status of the XML check of a resource. Possible values are
   * <ul><li>'W' - resource is well-formed,
   * <li>'N' - resource is not well-formed,
   * <li>'V' - resource is valid,
   * <li>'I' - resource is not valid,
   * <li>'_' - not checked.
   * </ul>
   * Set to <b>null</b> for collections.
   */
  public String getCheckStatus()
  {
    return checkStatus;
  }

  /**
   * @return type of collection. Possible values are
   * <ul><li>'H' - home collection,
   * <li>'S' - system collection,
   * <li>'A' - application collection.
   * </ul>
   * 
   *  Set to <b>null</b> for resources.
   */
  public String getCollectionType()
  {
    return collectionType;
  }

  /**
   * @return date and time of the creation of the resource/collection
   */
  public Date getCreationTime()
  {
    if(creationTime == null)
    {
      // not yet calculated so far
      if(unparsedCreationTime != null)
      {
        // date format must match the format used by XMLDAS
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try
        {
          creationTime = sdf.parse(unparsedCreationTime);
          return new Date(creationTime.getTime());
        }
        catch(ParseException e)
        {
          // should not happen
          throw new RuntimeException(e.getMessage());
        }
      }
    }
    else
    {
      return new Date(creationTime.getTime());
    }
    return null;
  }

  /**
   * @return 'Y' if the collection is frozen (closed for further archiving),
   * 'N' otherwise. Set to <b>null</b> for resources.
   */
  public boolean isFrozen()
  {
    return isFrozen;
  }

  /**
   * @return 'Y' if the resource is packed, 'N' otherwise. 
   * Set to <b>null</b> for collections.
   */
  public boolean isPacked()
  {
    return isPacked;
  }

  /**
   * @return length (in Bytes) of a resource. 
   * Set to <b>null</b> for collections.
   */
  public long getLength()
  {
    return length;
  }

  /**
   * @return type of a resource (e.g. XML, XSD, BIN...). Valid 
   * types are defined in table BC_XMLA_RESTYPES. Set to 'COL'
   * for collections.
   */
  public String getType()
  {
    return type;
  }

  /**
   * @return uri of the collection/resource
   */
  public URI getUri()
  {
    return uri;
  }

  /**
   * @return user who created the resource/collection
   */
  public String getUser()
  {
    return user;
  }

  public int getNrOfResources()
  {
    return nrOfResources;
  }
}
