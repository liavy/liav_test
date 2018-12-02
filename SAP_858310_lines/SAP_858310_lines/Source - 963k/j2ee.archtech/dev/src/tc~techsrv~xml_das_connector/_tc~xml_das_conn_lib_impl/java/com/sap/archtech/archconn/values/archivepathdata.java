package com.sap.archtech.archconn.values;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 
 * Value class used for the Webdynpro 
 * XML DAS Admin console
 * 
 * @author d025792
 * 
 */
public class ArchivePathData implements Serializable
{

   /**
    * versions of this class must declare this SUID to allow
    * serialization between different versions
    */
   private static final long serialVersionUID = -4191881489259937415L;

   private String archivePath;
   private String collectionType;
   private Timestamp creationTime;
   private String creationUser;
   private String archiveStore;
   private String storageSystem;
   private boolean isFrozen;
   private boolean hasChildren;
   private long colId;

  /**
   * @return Returns the colId.
   */
  public long getColId()
  {
    return colId;
  }
  /**
   * @param colId The colId to set.
   */
  public void setColId(long colId)
  {
    this.colId = colId;
  }
   /**
    * @return
    */
   public String getArchivePath()
   {
      return archivePath;
   }

   /**
    * @return
    */
   public String getArchiveStore()
   {
      return archiveStore;
   }

   /**
    * @return
    */
   public String getCollectionType()
   {
      return collectionType;
   }

   /**
    * @return
    */
   public Timestamp getCreationTime()
   {
      return creationTime;
   }

   /**
    * @return
    */
   public String getCreationUser()
   {
      return creationUser;
   }

   /**
    * @return
    */
   public boolean isFrozen()
   {
      return isFrozen;
   }

   /**
    * @return
    */
   public String getStorageSystem()
   {
      return storageSystem;
   }

   /**
    * @param string
    */
   public void setArchivePath(String string)
   {
      archivePath = string;
   }

   /**
    * @param string
    */
   public void setArchiveStore(String string)
   {
      archiveStore = string;
   }

   /**
    * @param string
    */
   public void setCollectionType(String string)
   {
      collectionType = string;
   }

   /**
    * @param timestamp
    */
   public void setCreationTime(Timestamp timestamp)
   {
      creationTime = timestamp;
   }

   /**
    * @param string
    */
   public void setCreationUser(String string)
   {
      creationUser = string;
   }

   /**
    * @param b
    */
   public void setFrozen(boolean b)
   {
      isFrozen = b;
   }

   /**
    * @param string
    */
   public void setStorageSystem(String string)
   {
      storageSystem = string;
   }
   
  /**
   * @return Returns the hasChildren.
   */
  public boolean getHasChildren()
  {
    return hasChildren;
  }
  
  /**
   * @param hasChildren The hasChildren to set.
   */
  public void setHasChildren(boolean hasChildren)
  {
    this.hasChildren = hasChildren;
  }
}
