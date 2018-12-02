package com.sap.archtech.archconn.values;

/**
 * 
 * Value object; contains result of an INFO request
 * @see com.sap.archtech.archconn.commands.InfoCommand
 * 
 * @author d025792
 * 
 */
public class ServiceInfo
{
	private String release;
	private String dbproductname;
	private String dbproductversion;
	private String jdbcdrivername;
	private String jdbcdriverversion;
	private String osname;
	private String osversion;
	private String javavendor;
	private String javaversion;
	private String javavmvendor;
	private String javavmname;
	private String javavmversion;
	private String archive_store;
	private String physical_path;
  private short ilmConformance;
  private String sysID;

  /**
   * @return name of the (physical) archive where the content
   * of the specified collection is located, <tt>null</tt> if 
   * no collection was specified 
   */
  public String getArchive_store()
  {
    return archive_store;
  }

  /**
   * @return name of the database product used by the 
   * XML Data Archiving Service
   */
  public String getDbproductname()
  {
    return dbproductname;
  }

  /**
   * @return version of the database product used by the
   * XML Data Archiving Service
   */
  public String getDbproductversion()
  {
    return dbproductversion;
  }

  /**
   * @return java vendor used by the
   * XML Data Archiving Service
   */
  public String getJavavendor()
  {
    return javavendor;
  }

  /**
   * @return java version used by the
   * XML Data Archiving Service
   */
  public String getJavaversion()
  {
    return javaversion;
  }

  /**
   * @return name of the Java Virtual Machine used by the
   * XML Data Archiving Service
   */
  public String getJavavmname()
  {
    return javavmname;
  }

  /**
   * @return vendor of the Java Virtual Machine used by the
   * XML Data Archiving Service
   */
  public String getJavavmvendor()
  {
    return javavmvendor;
  }

  /**
   * @return version of the Java Virtual Machine used by the
   * XML Data Archiving Service
   */
  public String getJavavmversion()
  {
    return javavmversion;
  }

  /**
   * @return name of the JDBC driver used by the 
   * XML Data Archiving Service
   */
  public String getJdbcdrivername()
  {
    return jdbcdrivername;
  }

  /**
   * @return version of the JDBC driver used by the 
   * XML Data Archiving Service
   */
  public String getJdbcdriverversion()
  {
    return jdbcdriverversion;
  }

  /**
   * @return name of the operation system used by the
   * XML Data Archiving Service
   */
  public String getOsname()
  {
    return osname;
  }

  /**
   * @return version of the operation system used by the
   * XML Data Archiving Service
   */
  public String getOsversion()
  {
    return osversion;
  }

  /**
   * @return (physical) archive path of the specified collection,
   * <tt>null</tt> if no collection was specified  
   */
  public String getPhysical_path()
  {
    return physical_path;
  }

  /**
   * @return release number of the XML Data Archiving 
   * Service
   */
  public String getRelease()
  {
    return release;
  }

  public short getIlmConformance()
  {
    return ilmConformance;
  }
  
  public String getSysID()
  {
  	return sysID;
  }
  
  /**
   * @param string
   */
  public void setArchive_store(String string)
  {
    archive_store = string;
  }

  /**
   * @param string
   */
  public void setDbproductname(String string)
  {
    dbproductname = string;
  }

  /**
   * @param string
   */
  public void setDbproductversion(String string)
  {
    dbproductversion = string;
  }

  /**
   * @param string
   */
  public void setJavavendor(String string)
  {
    javavendor = string;
  }

  /**
   * @param string
   */
  public void setJavaversion(String string)
  {
    javaversion = string;
  }

  /**
   * @param string
   */
  public void setJavavmname(String string)
  {
    javavmname = string;
  }

  /**
   * @param string
   */
  public void setJavavmvendor(String string)
  {
    javavmvendor = string;
  }

  /**
   * @param string
   */
  public void setJavavmversion(String string)
  {
    javavmversion = string;
  }

  /**
   * @param string
   */
  public void setJdbcdrivername(String string)
  {
    jdbcdrivername = string;
  }

  /**
   * @param string
   */
  public void setJdbcdriverversion(String string)
  {
    jdbcdriverversion = string;
  }

  /**
   * @param string
   */
  public void setOsname(String string)
  {
    osname = string;
  }

  /**
   * @param string
   */
  public void setOsversion(String string)
  {
    osversion = string;
  }

  /**
   * @param string
   */
  public void setPhysical_path(String string)
  {
    physical_path = string;
  }

  /**
   * @param string
   */
  public void setRelease(String string)
  {
    release = string;
  }

  public void setIlmConformance(short ilmConformance)
  {
    this.ilmConformance = ilmConformance;
  }
  
  public void setSysID(String sysID)
  {
  	this.sysID = sysID;
  }
}
