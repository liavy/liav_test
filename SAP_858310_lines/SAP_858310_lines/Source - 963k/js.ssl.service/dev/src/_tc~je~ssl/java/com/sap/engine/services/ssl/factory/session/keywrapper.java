/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.factory.session;

/**
 * A helper class. Suitable to add a byte[] as a key in the hashtable.
 * See also com.sap.engine.services.security.login.KeyWrapper.
 *
 * @author Jako Blagoev
 */
class KeyWrapper {

  private int hashCode = 0;
  private byte[] data = null;

  /**
   * Constructs a wrapper of a byte array key.
   *
   * @param data the byte array key to wrap
   */
  public KeyWrapper(byte[] data) {
    int offset = 0;
    int position = 0;
    this.data = (byte[]) data.clone();

    for (; position < data.length; position++, offset += 8) {
      offset = (offset > 32) ? 0 : offset;
      hashCode ^= ((int) data[position] & 0xFF) << offset;
    }
  }

  /**
   *  Compares this key with another.
   *
   * @param key the key wrapper to compare with
   *
   * @return true if the wrapped byte arrays are of the same length and same content
   */
  public boolean equals(KeyWrapper key) {
    if (key == this) {
      return true;
    }

    if (data.length != key.data.length) {
      return false;
    }

    for (int i = 0; i < data.length; i++) {
      if (data[i] != key.data[i]) {
        return false;
      }
    }

    return true;
  }

  /**
   *  Compares this key with another.
   *
   * @param object the key wrapper to compare with
   *
   * @return true if the wrapped byte arrays are of the same length and same content
   */
  public boolean equals(Object object) {
    if (object instanceof KeyWrapper) {
      return equals((KeyWrapper) object);
    } else {
      return false;
    }
  }

  /**
   *  Returns the hash code of the byte array
   *
   * @return hash code
   */
  public int hashCode() {
    return hashCode;
  }

  /**
   *  Returns the wrapped byte array key.
   *
   * @return the wrapped key.
   */
  public byte[] getKey() {
    return (byte[]) data.clone();
  }

  /**
   *  Makes a copy of the key wrapper.
   *
   * @return a copy of the key wrapper.
   */
  public Object clone() {
    return new KeyWrapper((byte[]) data.clone());
  }
}
