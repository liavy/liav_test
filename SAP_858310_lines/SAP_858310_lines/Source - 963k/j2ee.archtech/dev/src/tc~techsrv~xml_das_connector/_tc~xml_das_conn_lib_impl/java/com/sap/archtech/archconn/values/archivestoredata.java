package com.sap.archtech.archconn.values;

import java.io.Serializable;

/**
 *
 * Value class used for the Webdynpro
 * XML DAS Admin console
 *
 * @author d025792
 *
 */
public class ArchiveStoreData implements Serializable
{

   /**
    * versions of this class must declare this SUID to allow
    * serialization between different versions
    */
   private static final long serialVersionUID = 2981967667556762245L;

   private String archiveStore;
   private String storageSystem;
   private String storeType;
   private String winRoot;
   private String unixRoot;
   private String proxyHost;
   private int proxyPort;
   private String storeStatus;
   private String destination;
   private short ilmconform;
   private String isdefault;

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
   public String getProxyHost()
   {
      return proxyHost;
   }

   /**
    * @return
    */
   public int getProxyPort()
   {
      return proxyPort;
   }

   /**
    * @return
    */
   public String getStorageSystem()
   {
      return storageSystem;
   }

   /**
    * @return
    */
   public String getStoreType()
   {
      return storeType;
   }

   /**
    * @return
    */
   public String getUnixRoot()
   {
      return unixRoot;
   }

   /**
    * @return
    */
   public String getWinRoot()
   {
      return winRoot;
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
   public void setProxyHost(String string)
   {
      proxyHost = string;
   }

   /**
    * @param i
    */
   public void setProxyPort(int i)
   {
      proxyPort = i;
   }

   /**
    * @param string
    */
   public void setStorageSystem(String string)
   {
      storageSystem = string;
   }

   /**
    * @param string
    */
   public void setStoreType(String string)
   {
      storeType = string;
   }

   /**
    * @param string
    */
   public void setUnixRoot(String string)
   {
      unixRoot = string;
   }

   /**
    * @param string
    */
   public void setWinRoot(String string)
   {
      winRoot = string;
   }

   /**
    * @return
    */
   public String getStoreStatus()
   {
      return storeStatus;
   }

   /**
    * @param string
    */
   public void setStoreStatus(String string)
   {
      storeStatus = string;
   }
   /**
    * @return
    */
   public String getDestination()
   {
      return destination;
   }

   /**
    * @param string
    */
   public void setDestination(String string)
   {
      destination = string;
   }

   /**
    * @return
    */
   public short getIlmConformance()
   {
      return ilmconform;
   }

   /**
    * @param short
    */
   public void setIlmConformance(short s)
   {
      ilmconform = s;
   }

   /**
    * @return
    */
   public String getIsDefault()
   {
      return isdefault;
   }

   /**
    * @param string
    */
   public void setIsDefault(String string)
   {
      isdefault = string;
   }

}
