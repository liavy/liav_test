package com.sap.archtech.archconn.values;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value class, holds description of a property index.
 */
public class IndexPropDescription
{
  private static final String regExLastPart = "\\d{0,1}";
  private static final Pattern secIdxPattern = Pattern.compile(new StringBuilder("[A-Z]").append(regExLastPart).toString());
  private final String indexname;
  private final HashMap<String, PropTypeAndSecIndex> propTypeAndSecIdx2PropName;
  private final boolean isMultiValSupported;

  /**
   * Constructs a property index description.
   * The index will not support multiple values for a given property.
   * @param indexname Name of the index
   */
  public IndexPropDescription(String indexname)
  {
    this(indexname, false);
  }

  /**
   * Constructs a property index description.
   * @param indexname Name of the index
   * @param isMultiValSupported Indicates whether the index supports multiple values for a given property
   */
  public IndexPropDescription(String indexname, boolean isMultiValSupported)
  {
    this.indexname = indexname;
    propTypeAndSecIdx2PropName = new HashMap<String, PropTypeAndSecIndex>();
    this.isMultiValSupported = isMultiValSupported;
  }

  /**
   * Adds an entry for a single property.
   * 
   * @param propname
   *          name of an index property
   * @param proptype
   *          type of an index property
   */
  public void putDesc(String propname, String proptype)
  {
    propTypeAndSecIdx2PropName.put(propname, new PropTypeAndSecIndex(proptype, null));
  }

  /**
   * Adds an entry for a single property including the definition of one or more secondary indices for that property.<br>
   * Each secondary index must be specified as follows:
   * <ul>
   * <li>It consists of max. 2 characters.</li>
   * <li>The first character is one letter out of A - Z. This is the secondary index alias.</li>
   * <li>The second - optional - character is a digit out of 0 - 9. It indicates the order of the indexed field within the secondary index.
   * If no digit is specified, XMLDAS will interprete it as "0".</li>
   * <li>Obviously, it is not allowed to use the same index alias twice for the same indexed field.</li> 
   * </ul>
   * @param propName The name of the index property
   * @param propType The type of the index property
   * @param secondaryIndices The secondary indices to be created for the property. May be <code>null</code>.
   * @throws IllegalArgumentException Thrown if one of the given secondary indices does not meet the specification.
   */
  public void putDesc(String propName, String propType, String[] secondaryIndices)
  {
    if(secondaryIndices != null)
    {
      // parameter check
      Matcher matcher = null;
      StringBuilder usedAliases = new StringBuilder("");
      boolean isAfterFirstLoop = false;
      for(String secondaryIndex : secondaryIndices)
      {
        secondaryIndex = secondaryIndex.toUpperCase();
        matcher = secIdxPattern.matcher(secondaryIndex);
        if(!matcher.matches())
        {
          throw new IllegalArgumentException("The given secondary index does not meet the specification: " + secondaryIndex);
        }
        if(isAfterFirstLoop)
        {
          // do not use the same index alias twice for the same indexed field
          boolean isUsed = Pattern.matches(new StringBuilder("[").append(usedAliases).append("]").append(regExLastPart).toString(), secondaryIndex);
          if(isUsed)
          {
            throw new IllegalArgumentException("Do not use the same index alias twice for a given indexed field. Invalid secondary index is: " + secondaryIndex);
          }
        }
        usedAliases.append(secondaryIndex.charAt(0));
        isAfterFirstLoop = true;
      }
    }
    propTypeAndSecIdx2PropName.put(propName, new PropTypeAndSecIndex(propType, secondaryIndices));
  }
  
  /**
   * @return the name of the index
   */
  public String getIndexname()
  {
    return indexname;
  }

  /**
   * @return A <code>HashMap</code> with all property descriptions (map key = property name, mapped value = property type).
   */
  public HashMap<String, String> getPropDesc()
  {
    HashMap<String, String> tmp = new HashMap<String, String>(propTypeAndSecIdx2PropName.size());
    Set<Entry<String, PropTypeAndSecIndex>> entries = propTypeAndSecIdx2PropName.entrySet();
    for(Entry<String, PropTypeAndSecIndex> entry : entries)
    {
      tmp.put(entry.getKey(), entry.getValue().getPropType());
    }
    return tmp;
  }

  /**
   * @return A <code>HashMap</code> with map key = property name and mapped value = secondary indices (array of strings).
   * For a given property name a mapping exists only if there is at least one secondary index specification.
   */
  public HashMap<String, String[]> getSecondaryIndices()
  {
    HashMap<String, String[]> tmp = new HashMap<String, String[]>(propTypeAndSecIdx2PropName.size());
    String[] secIndices = null;
    Set<Entry<String, PropTypeAndSecIndex>> entries = propTypeAndSecIdx2PropName.entrySet();
    for(Entry<String, PropTypeAndSecIndex> entry : entries)
    {
      secIndices = entry.getValue().getSecondaryIndices();
      if(secIndices.length > 0)
      {
        tmp.put(entry.getKey(), secIndices);
      }
    }
    return tmp;
  }
  
  public String[] getSecondaryIndices(String propName)
  {
    PropTypeAndSecIndex propTypeAndSecIdx = propTypeAndSecIdx2PropName.get(propName);
    if(propTypeAndSecIdx != null)
    {
      return propTypeAndSecIdx.getSecondaryIndices();
    }
    return new String[0];
  }
  
  /**
   * Indicates whether the index represented by this instance supports multiple values for a given property.
   */
  public boolean isMultiValSupported()
  {
  	return isMultiValSupported;
  }
  
  private static class PropTypeAndSecIndex
  {
    private final String propType;
    private final String[] secondaryIndices;
    
    private PropTypeAndSecIndex(String propType, String[] secondaryIndices)
    {
      this.propType = propType;
      if(secondaryIndices != null)
      {
        this.secondaryIndices = new String[secondaryIndices.length];
        System.arraycopy(secondaryIndices, 0, this.secondaryIndices, 0, secondaryIndices.length);
      }
      else
      {
        this.secondaryIndices = new String[0];
      }
    }
    
    String getPropType()
    {
      return propType;
    }
    
    String[] getSecondaryIndices()
    {
      String[] tmp = new String[secondaryIndices.length];
      System.arraycopy(secondaryIndices, 0, tmp, 0, secondaryIndices.length);
      return tmp;
    }
  }
}