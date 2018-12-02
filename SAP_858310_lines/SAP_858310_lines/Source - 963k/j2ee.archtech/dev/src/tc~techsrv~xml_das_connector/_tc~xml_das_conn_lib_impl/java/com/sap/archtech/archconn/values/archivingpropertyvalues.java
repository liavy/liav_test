package com.sap.archtech.archconn.values;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ArchivingPropertyValues</code> class represents name-value-pairs to be attached to a single archived resources
 * or collection. Note, that property attachments are only supported by WebDAV-based archive stores.
 */
public class ArchivingPropertyValues implements Serializable
{
  private static final long serialVersionUID = 42L;
  
  private HashMap<String, String> archPropertyValues;
  
  public ArchivingPropertyValues()
  {
    archPropertyValues = new HashMap<String, String>();
  }
  
  /**
   * Adds an archiving property
   * @param archPropName Name of the property
   * @param archPropNalue Value of the property
   */
  public void putProp(String archPropName, String archPropNalue)
  {
    archPropertyValues.put(archPropName.toLowerCase(), archPropNalue);
  }
  
  /**
   * Get all archiving properties contained in this instance.
   * @return An unmodifiable map containing all archiving properties
   */
  public Map<String, String> getAllProperties()
  {
    return Collections.unmodifiableMap(archPropertyValues);
  }
  
  /**
   * Get one specific archiving property or <code>null</code> if the
   * property does not exists.
   * @param archPropName Name of the property
   * @return Value of the property
   */
  public String getProperty(String archPropName)
  {
    return archPropertyValues.get(archPropName);
  }
}
