package com.sap.archtech.archconn.values;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;

import com.sap.guid.IGUID;

/**
 * Value class; holds one property index entry.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class IndexPropValues implements Serializable
{

  /**
   * versions of this class must declare this SUID to allow
   * serialization between different versions
   */
  private static final long serialVersionUID = 7823259969230855065L;

  private String indexname;
  private HashMap<String, Serializable> propVals;

  public IndexPropValues()
  {
  }

  /**
   * 
   * @param indexname the name of the property index
   */
  public IndexPropValues(String indexname)
  {
    this.indexname = indexname;
    propVals = new HashMap<String, Serializable>();
  }

  /**
   * Adds a property with a String value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, String propvalue)
  {
    propVals.put(propname.toLowerCase(), propvalue);
  }

  /**
   * Adds a property with a short value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, short propvalue)
  {
    propVals.put(propname.toLowerCase(), Short.valueOf(propvalue));
  }

  /**
   * Adds a property with a int value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, int propvalue)
  {
    propVals.put(propname.toLowerCase(), Integer.valueOf(propvalue));
  }

  /**
   * Adds a property with a long value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, long propvalue)
  {
    propVals.put(propname.toLowerCase(), Long.valueOf(propvalue));
  }

  /**
   * Adds a property with a float value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, float propvalue)
  {
    propVals.put(propname.toLowerCase(), new Float(propvalue));
  }

  /**
   * Adds a property with a double value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, double propvalue)
  {
    propVals.put(propname.toLowerCase(), new Double(propvalue));
  }

  /**
   * Adds a property with a Timestamp value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, Timestamp propvalue)
  {
    propVals.put(propname.toLowerCase(), propvalue);
  }

  /**
   * Adds a property with an IGUID value.
   * 
   * @param propname name of the property
   * @param propvalue value of the property
   */
  public void putProp(String propname, IGUID propvalue)
  {
		// store as hex string because IGUID does not implement Serializable
  	propVals.put(propname.toLowerCase(), propvalue.toHexString());
  }

  /**
   * @return name of the property index
   */
  public String getIndexname()
  {
    return indexname.toLowerCase();
  }

  /**
   * @deprecated Use {@link #getPropertyValues()}
   */
  public HashMap<String, Object> getPropVals()
  {
  	HashMap<String, Object> map = new HashMap<String, Object>(propVals.size());
  	for(Entry<String, Serializable> propValEntry : propVals.entrySet())
  	{
  		map.put(propValEntry.getKey(), propValEntry.getValue());
  	}
  	return map;
  }
  
  /**
   * @return HashMap with all property values
   */
  public HashMap<String, Serializable> getPropertyValues()
  {
    return propVals;
  }

  /**
   * Returns one specific property value or null if the
   * property does not exists.
   * 
   * @param propName name of the property
   * @return value of the property
   */
  public Object getProperty(String propName)
  {
  	return propVals.get(propName);
  }
}
