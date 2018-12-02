package com.sap.engine.services.webservices.jaxm.soap;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator implementation used in JAXM Implementation.
 * @author       Chavdar Baykov , Chavdarb@yahoo.com
 * @version      2.0
 */
public class IteratorImpl implements Iterator {

  public static int INITIAL_COUNT = 10; // The initial count of elements created in this iterator
  private Object contents[] = null; // Content of this iterator
  private int currentSize = 0; // Current Element count in this iterator
  private int currentPointer = 0; // current element on wich iterator is on

  //private boolean lock = false;       - Read or Write Mode not used
  /**
   * Relligns array that holds iterator Elements
   */
  private void rearrange() {
    if (contents == null) {
      contents = new Object[INITIAL_COUNT];
      return;
    }

    Object bufferContents[] = contents;
    contents = new Object[currentSize + INITIAL_COUNT];
    System.arraycopy(bufferContents, 0, contents, 0, bufferContents.length);
    bufferContents = null;
  }

  /**
   * Adds object to Iterator contents.
   */
  public void addElement(Object o) {
    if (contents == null || currentSize == contents.length) {
      rearrange();
    }

    contents[currentSize] = o;
    currentSize++;
  }

  /**
   * Has next method.
   */
  public boolean hasNext() {
    if (currentPointer >= currentSize) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns next object in Iterator.
   */
  public Object next() {
    if (currentPointer < currentSize) {
      return contents[currentPointer++];
    }

    throw new NoSuchElementException("Iterator elements are over.");
  }

  /**
   * Removing from this Iterator Implementation is unsupported.
   */
  public void remove() {
    throw new UnsupportedOperationException(" Removing from this Iterator implementation is unsupported ! ");
  }

  /**
   * Enumeration initializer.
   */
  public void init(Enumeration e) {
    while (e.hasMoreElements()) {
      addElement(e.nextElement());
    }
  }

  /**
   * Returns true if element contains in Iterator
   */
  public boolean includes(Object o) {
    for (int i = 0; i < currentSize; i++) {
      if (contents[i].equals(o)) {
        return true;
      }
    } 

    return false;
  }

}

//final class IteratorImpl implements java.util.Iterator{
//
//  private java.util.ArrayList content = new java.util.ArrayList();
//  private int counter = 0;
//
//  public void addElement(Object o) {
//    content.add(o);
//  }
//
//  public boolean hasNext() {
//    if (counter < content.size()) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public Object next() {
//    counter++;
//    if (counter>content.size()) {
//      throw new java.util.NoSuchElementException(" The Iterator has no more Elements !");
//    }
//    return content.get(counter-1);
//  }
//
//  public void remove() {
//    // Not implemented
//  }
//
//  public boolean includes(Object o) {
//    for (int i=0; i<content.size(); i++) {
//      if (content.get(i).equals(o)) {
//        return true;
//      }
//    }
//    return false;
//  }
//
//}

