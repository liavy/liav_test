/*
 * Copyright (c) 2003 by SAP AG.,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG..
 */
package com.sap.engine.services.timeout;

/**
 * This class is a representation of the Priority Queue data structure.
 * This structure is similar to the standard FIFO Queue , except that
 * each of its elements has its specific priority and the next element
 * to be pulled out of the queue is the one with the highest priority.
 * All operation (add, remove, recalculate) complexity is O(log(n)).
 * <p>Priority queue implementation use heap. Heap is almost complete binary
 * tree whose internal nodes stores items and for every node (i) other than
 * the root, the item stored at (i) is with smaller or equals priority than
 * the priority of its parent stored at (i/2).
 * <PRE>Example:
 *              priority (array position)
 *                    4 (1)
 *                /            \
 *        9 (2)                  18 (3)
         /     \                /
 *  10 (4)     36 (5)      19 (6)
 *
 *                             itemCounter
 *                                /
 *  items array: [ |4|8|18|10|36|19| | | ... | ]</PRE>
 *  <bl><li>TimeoutNodes are stored from 1 to itemCounter.</li>
 *  <li>Item at position (i) has children (i*2) and (i*2 + 1) and parent (i/2).</li></bl>
 *
 * @author George Manev, Krasimir Semerdzhiev, Dimitar Kostadinov
 * @version 6.30
 */
final class PriorityQueue {

  // items array
  private TimeoutNode[] items;
  // item counter
  private int itemsCounter;
  // item array max size
  private int maxSize;
  // trigger
  private byte trigger;

  /**
   * Creates a new empty TimeoutPriorityQueue using the defaults.
   * This means that the Queue will have an initial capacity for 2 items
   * and will be able to contain up to 2,147,483,647 items total.
   */
  PriorityQueue() {
    this(2147483647);
  }

  /**
   * Creates a new empty TimeoutPriorityQueue having the specified maxSize.
   * The Queue will be able to grow for up to maxSize items or 2,147,483,647
   * in case the maxSize is negative.
   *
   * @param maxSize  The maximum number of items that this queue can contain.
   */
  PriorityQueue(int maxSize) {
    this.maxSize = (maxSize < 128) ? 128 : maxSize;
    trigger = 0;
    itemsCounter = 0;
    items = new TimeoutNode[2];
  }

  /**
   * Used to determine if the queue is currently empty.
   *
   * @return  true if the queue is empty and false otherwise.
   */
  final boolean isEmpty() {
    return itemsCounter == 0;
  }

  /**
   * Used to determine if the queue is currently full, in the meaning
   * that the queueu has already reached its maximum allowed size.
   *
   * @return  true if the queue is full and false otherwise.
   */
  final boolean isFull() {
    return itemsCounter == maxSize;
  }

  /**
   * Returns a reference to this element in the queue that will be executed
   * next i.e. the one with the currently highest priority, or null if the queue
   * is empty.
   *
   * @return  a reference to the element with the highest priority.
   */
  final TimeoutNode getFirst() {
    if (isEmpty()) {
      return null;
    }
    return items[1];
  }

  /**
   * Returns the next element waiting for execution and removes it from the Queue.
   *
   * @return  The element with the highest priority in the queue.
   * @exception IllegalStateException  If there aren't any items in the Queue.
   */
  final TimeoutNode removeFirst() throws IllegalStateException {
    if (isEmpty()) {
      throw new IllegalStateException("The Queue is empty.");
    }
    TimeoutNode result = items[1];
    items[1] = items[itemsCounter];
    items[1].queuePosition = 1;
    items[itemsCounter--] = null;
    adjustQueueDown(1);
    result.queuePosition = -1;
    return result;
  }

  /**
   * Adds a new element in the queue, according ot its priority. If the queue
   * has already reached its maximum allowed size an IllegalStateException is thrown.
   *
   * @param  node   The new element to be enqueued.
   * @return true if node is put as first element in the queue.
   * @exception  IllegalStateException   If the Queue has reached its maximum allowed size.
   */
  final boolean add(TimeoutNode node) throws IllegalStateException {
    if (isFull()) {
      throw new IllegalStateException("The Queue is full.");
    }
    setLength(++itemsCounter + 1);
    items[itemsCounter] = node;
    items[itemsCounter].queuePosition = itemsCounter;
    adjustQueueUp(itemsCounter);
    return node.queuePosition == 1;
  }

  /**
   * Removes a element from the priority queue.
   *
   * @param node The element to be removed.
   * @return true if node is removed from the queue head.
   * @exception  IllegalArgumentException   If the node queue position is illegal.
   */
  final boolean remove(TimeoutNode node) throws IllegalArgumentException {
    int pos = node.queuePosition;
    if(pos < 1 || pos > itemsCounter) {
      throw new IllegalArgumentException("Illegal TimeoutNode queue position : " + node.queuePosition);
    }
    boolean result = pos == 1;
    if (pos == itemsCounter) {
      items[itemsCounter--] = null;
    } else {
      items[pos] = items[itemsCounter];
      items[pos].queuePosition = pos;
      items[itemsCounter--] = null;
      //element at position pos must be adjust up or down.
      adjustQueueUp(pos);
      adjustQueueDown(pos);
    }
    trigger++;
    trigger %= 5;
    if (trigger == 0) {
      setLength(itemsCounter + 1);
    }
    node.queuePosition = -1;
    return result;
  }

  /**
   * Recalculates prioryty for the element.
   *
   * @param node The element to be recalculated.
   * @return true if node is removed from the queue head.
   * @exception  IllegalArgumentException   If the node queue position is illegal.*
   */
  final boolean recalculate(TimeoutNode node) throws IllegalArgumentException {
    int pos = node.queuePosition;
    if(pos < 1 || pos > itemsCounter) {
      throw new IllegalArgumentException("Illegal TimeoutNode queue position : " + node.queuePosition);
    }
    boolean result = pos == 1;
    //element at position pos must be adjust up or down.
    adjustQueueUp(pos);
    adjustQueueDown(pos);
    if (result && node.queuePosition == 1) {
      result = false;
    }
    return result;
  }

  /**
   * Increase prioryties for all items with delta time.
   *
   * @param delta increase time.
   */
  final void recalculateIntervals(long delta) {
    for (int i = 1; i <= itemsCounter; i++) {
      items[i].nextCallTime += delta;
    }
  }

  /**
   * Adjust item at position pos at appropriate position search the heep up.
   */
  private final void adjustQueueUp(int pos) {
    while (pos > 1) {
      int parent = pos >> 1;
      if (items[parent].nextCallTime <= items[pos].nextCallTime) {
        break;
      }
      swap(pos, parent);
      pos = parent;
    }
  }

  /**
   * Adjust item at position pos at appropriate position search the heep down.
   */
  private final void adjustQueueDown(int pos) {
    int child;
    while ((child = pos << 1) <= itemsCounter) {
      if (child < itemsCounter && items[child].nextCallTime > items[child + 1].nextCallTime) {
        child++;
      }
      if (items[pos].nextCallTime <= items[child].nextCallTime) {
        break;
      }
      swap(pos, child);
      pos = child;
    }
  }

  /**
   * Swap item at position i with item at position j.
   */
  private final void swap(int i, int j) {
    TimeoutNode tmp = items[i];
    items[i] = items[j];
    items[i].queuePosition = i;
    items[j] = tmp;
    items[j].queuePosition = j;
  }

  /**
   * Resizes the Queue as appropriate.
   */
  private final void setLength(int newLength) {
    if (newLength < items.length >> 1) {
      TimeoutNode[] newItems = new TimeoutNode[newLength];
      System.arraycopy(items, 0, newItems, 0, newLength);
      items = newItems;
    }
    if (newLength > items.length) {
      int newCapacity = items.length;
      newCapacity <<= 1;
      if (newLength > newCapacity) {
        newCapacity = newLength;
      }
      if (newCapacity > maxSize) {
        newCapacity = maxSize;
      }
      TimeoutNode[] newItems = new TimeoutNode[newCapacity];
      System.arraycopy(items, 0, newItems, 0, items.length);
      items = newItems;
    }
  }

  final TimeoutListener[] getAllItems() {
    TimeoutListener[] result = new TimeoutListener[itemsCounter];
    for (int i = 1; i <= itemsCounter; i++) {
      result[i - 1] = items[i].work;
    }
    return result;
  }

}