package com.sap.archtech.archconn.values;

/**
 * Contains archiving set name - home path pairs.
 * Used in ArchSetConfigurator.
 * 
 * @author d025792
 */
public class ArchSetHome
{

  private String archset;
  private String homepath;

/**
 * Craetes a new ArchSetHome object.
 * 
 * @param archset name of the archiving set
 * @param homepath home path of the archiving set
 */
  public ArchSetHome(String archset, String homepath)
  {
    this.archset = archset;
    this.homepath = homepath;
  }

  /**
   * @return name of this archiving set
   */
  public String getArchset()
  {
    return archset;
  }

  /**
   * @return home path associated with this archiving set
   */
  public String getHomepath()
  {
    return homepath;
  }

}
