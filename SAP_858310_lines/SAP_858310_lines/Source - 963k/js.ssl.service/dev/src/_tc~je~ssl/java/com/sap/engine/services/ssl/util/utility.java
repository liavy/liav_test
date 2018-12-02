/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.util;

import java.util.*;

/**
 *  Common utilities used by SSL service.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.2
 */
public class Utility {

  /**
   *  Concatenates the given arrays.
   *
   * @param array1  first array.
   * @param array2  second array.
   *
   * @return the result array.
   */
  public static String[] add(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    System.arraycopy(array1, 0, result, 0, array1.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return set(result);
  }

  /**
   *  Removes the elements of the second array from the first array.
   *
   * @param array1  the array of elements to be removed.
   * @param array2  the array to remove elements from.
   *
   * @return the result array.
   */
  public static String[] remove(String[] array1, String[] array2) {
    int nulls = 0;
    String[] result = new String[array2.length];
    System.arraycopy(array2, 0, result, 0, result.length);

    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) {
        nulls++;
      }
    }

    for (int i = 0; i < array1.length; i++) {
      for (int j = 0; j < result.length; j++) {
        if ((result[j] != null) && result[j].equals(array1[i])) {
          result[j] = null;
          nulls++;
        }
      }
    }

    if (nulls > 0) {
      String[] array = new String[result.length - nulls];

      for (int i = 0, j = 0; i < result.length; i++) {
        if (result[i] != null) {
          array[j++] = result[i];
        }
      }

      return array;
    }

    return result;
  }

  /**
   *  Returns the same array with no doubled elements.
   *
   * @param array  the input array.
   *
   * @return  an array of the elements of the input array without doubles.
   */
  public static String[] set(String[] array) {
    int doubles = 0;
    String element = null;

    for (int i = 0; i < array.length; i++) {
      element = array[i];

      if (element != null) {
        for (int j = i + 1; j < array.length; j++) {
          if (element.equals(array[j])) {
            array[j] = null;
            doubles++;
          }
        }
      }
    }

    if (doubles > 0) {
      String[] result = new String[array.length - doubles];

      for (int i = 0, j = 0; i < array.length; i++) {
        if (array[i] != null) {
          result[j++] = array[i];
        }
      }

      return result;
    }

    return array;
  }

  /**
   * Converts String array to Vector
   *
   * @param array  the array to be converted
   *
   * @return  the Vector representation of the String array
   */
  public static Vector stringArrayToVector(String[] array) {
    Vector vector = new Vector(array.length, 10);

    for (int i = 0; i < array.length; i++) {
      vector.add(array[i]);
    }

    return vector;
  }

}

