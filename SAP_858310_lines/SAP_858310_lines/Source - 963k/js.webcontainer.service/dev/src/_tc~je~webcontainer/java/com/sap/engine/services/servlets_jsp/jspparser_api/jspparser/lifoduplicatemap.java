/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * This map can contain duplicate keys. When value is requested the last entry
 * added with the given key is returned. Key cannot be null, but value can.
 * This Map will be used for storing tag libraries. It should have override all other methods from java.util.Map 
 * if is intended to be used for general purpose.  
 * @author Todor Mollov DEV_webcontainer Nov 2, 2006
 * 
 */
public class LifoDuplicateMap<K, V> extends LinkedHashMap { 

  private static final long serialVersionUID = 8934516613671589185L;

  transient private K[] keys = (K[]) new Object[5];

  transient private V[] values = (V[]) new Object[5];

  private int inserted = -1;// empty

  /**
   * Duplicates the values. It won't be possible to remove from this array 
   */
  transient private V[] valuesImmutable = (V[]) new Object[5];
  private int insertedImmutable = -1;// empty
  
  /**
   * Puts non-null key and value in the map.
   * 
   * @param key - any object but null
   * @param value
   */
  public Object put(Object keyO, Object valueO) {
    if (keyO == null) {
      return null;
    }
    K key = (K) keyO;
    V value = (V) valueO;
    if (inserted == keys.length - 1) {
      K[] tempKeys = (K[]) new Object[keys.length + keys.length];
      System.arraycopy(keys, 0, tempKeys, 0, keys.length);
      keys = tempKeys;

      // do the same for the values
      V[] tempValues = (V[]) new Object[values.length + values.length];
      System.arraycopy(values, 0, tempValues, 0, values.length);
      values = tempValues;
      
    }
    if( insertedImmutable == valuesImmutable.length - 1 ) {
      // do the same for the valuesImmutable
      V[] tempValuesImmutable = (V[]) new Object[valuesImmutable.length + valuesImmutable.length];
      System.arraycopy(valuesImmutable, 0, tempValuesImmutable, 0, valuesImmutable.length);
      valuesImmutable = tempValuesImmutable;       
    }

    inserted++;
    insertedImmutable++;
    keys[inserted] = key;
    values[inserted] = value;
    valuesImmutable[insertedImmutable] = value;
    return null;
  }

  public V get(Object keyO) {
    K key = null;
    try {
      key = (K) keyO;
    } catch (Exception e) {
      return null;
    }
    V result = null;
    int keyIndex = findKeyIndex(key);
    if (keyIndex >= 0) {
      result = values[keyIndex];
    }
    return result;
  }

  /**
   * Removes the last inserted value with this key.
   * The removed value will be returned from the values() method.
   * @param key
   * @return
   */
  public V remove(Object keyO) {
    V result = null;
    K key = null;
    try {
      key = (K) keyO;
    } catch (Exception e) {
      return null;
    }
    int keyIndex = findKeyIndex(key);
    /// the key/values should be shifted one the place of the removed object
    if (keyIndex >= 0) {
      result = values[keyIndex];
      keys[keyIndex] = null;
      values[keyIndex] = null;
      System.arraycopy(keys, keyIndex+1, keys, keyIndex, keys.length- 1 - keyIndex);
      System.arraycopy(values, keyIndex+1, values, keyIndex, values.length- 1 - keyIndex);
      if( keyIndex <= inserted) {
        keys[inserted] = null;
        values[inserted] = null;        
      }
      inserted--;
    }
    return result;
  }

  /**
   * Helper method for searching the possition of the given key. 
   * Null is not acceptable as value for key. 
   * @param key
   * @return
   */
  private int findKeyIndex(K key) {
    int result = -1; 
    if (inserted < 0 || key == null) {
      return result;
    } 
    for (int i = inserted; i >= 0; i--) {
      if (keys[i].equals(key)) {
        result = i;
        break;
      }
    }
    return result;
  }

  @Override
  public void clear() {
    keys = (K[]) new Object[5];
    values = (V[]) new Object[5];
    inserted = -1;
    insertedImmutable = -1;
  }

  @Override
  public int size() {
    return insertedImmutable + 1;
  }

  /**
   * Returns copy of all values that had been set. Even those that had been removed.
   * @return
   */
  public Collection values() {
    if( insertedImmutable < 0 ) {
      return null; 
    }    
    V[] nonNull =  (V[]) new Object[insertedImmutable+1];
    System.arraycopy(valuesImmutable, 0, nonNull, 0, nonNull.length);
    return Arrays.asList(nonNull);
  }

}
