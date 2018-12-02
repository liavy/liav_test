/**
 * Title:        xml2000
 * Description:  This class maps string to int like a hashtable
 *               bypasses a bug in JDK 1.2.2 Java.lang.String
 *               as hash
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 * Caution !!! this is just a hashed list not real hash table
 * there are no methods for removing elements of the hashtable
 */
package com.sap.engine.services.webservices.wsdl;

public class HashStringtoInt {

  private int elementCount; // the count hold inte the hashtable
  private int resizeSize = 20; // the resize size of the array
  private int[] hashList; // array for holding hashcodes
  private String[] stringList; // array for holding hashed Strings
  private int[] intList; // array for holding hashed integers

  public HashStringtoInt() {
    hashList = new int[resizeSize];
    stringList = new String[resizeSize];
    intList = new int[resizeSize];
    elementCount = 0;
  }

  public void put(String text, int number) {
    // if the key exists in hash table
    int code = text.hashCode();

    for (int i = 0; i < elementCount; i++) {
      if (code == hashList[i] && text.equals(stringList[i])) {
        intList[i] = number;
        return;
      }
    } 

    if (elementCount >= stringList.length) {
      int[] permh = new int[hashList.length + resizeSize];
      String[] perms = new String[stringList.length + resizeSize];
      int[] permi = new int[intList.length + resizeSize];
      System.arraycopy(hashList, 0, permh, 0, hashList.length);
      System.arraycopy(stringList, 0, perms, 0, stringList.length);
      System.arraycopy(intList, 0, permi, 0, intList.length);
      stringList = perms;
      intList = permi;
      hashList = permh;
      perms = null;
      permi = null;
      permh = null;
    }

    hashList[elementCount] = text.hashCode();
    stringList[elementCount] = text;
    intList[elementCount] = number;
    elementCount++;
  }

  public int get(String text) {
    int textCode = text.hashCode();

    for (int i = 0; i < elementCount; i++) {
      if (textCode == hashList[i]) {
        if (text.equals(stringList[i])) {
          return intList[i];
        }
      }
    } 

    return 0;
  }

  public int size() {
    return elementCount;
  }

}

