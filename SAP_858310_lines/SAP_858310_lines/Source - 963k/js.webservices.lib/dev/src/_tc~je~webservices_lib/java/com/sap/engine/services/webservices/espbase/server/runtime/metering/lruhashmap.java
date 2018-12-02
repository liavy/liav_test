package com.sap.engine.services.webservices.espbase.server.runtime.metering; 

import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.IntHashHolder;

/*
 * LRU-sensitive methods:
 * 
 * put
 * get
 * containsKey
 * 
 */
public class LRUHashMap extends HashMapObjectObject{
  
  private LRUQueue queue = null;
  HashMapObjectObject queueLookup = new HashMapObjectObject();
  
  public LRUHashMap(int maxsize){
    this.queue = new LRUQueue(maxsize);
  }
  
  public LRUHashMap(int initialCapacity, int maxSize) {
    super(initialCapacity);
    this.queue = new LRUQueue(maxSize);
  }

  public LRUHashMap(int initialCapacity, int growStep, float loadFactor, IntHashHolder hasher) {
    super(initialCapacity, growStep, loadFactor, hasher);
  }
  
  public boolean containsKey(Object key) {
    Object queueNode = queueLookup.get(key);
    if (queueNode != null){
      queue.update((SetItem)queueNode);
    }
    return super.containsKey(key);
  }
  
  public void clear() {
    super.clear();
    queue.clear();
    queueLookup.clear();
  }
  
  public Object put(Object key, Object value) {
   Object previousValue = super.put(key, value);
   // if key exists - update the queue
   if (previousValue != null){
     SetItem item = (SetItem)queueLookup.get(key);
     queue.update(item);
     return previousValue;
   }
   // add in LRU queue
   SetItem si = new SetItem();
   si.data = key;
   SetItem removed = queue.add(si);
   // put in lookup hash map
   queueLookup.put(key, si);
   if (removed != null) {
     super.remove(removed.data);
     queueLookup.remove(removed.data);
   }
   System.out.println(queue.size + " " + queueLookup.size() + " " + size() + " ");
   return previousValue;
  }
  
  public Object get(Object key) {
    Object o = super.get(key);
    // update LRU queue
    if (o != null){
      SetItem si = (SetItem)queueLookup.get(key);
      if (si != null){
        queue.update(si);
      }
    }
    return o;
  }
  
  public Object remove(Object key) {
    Object o = super.remove(key);
    SetItem si = (SetItem)queueLookup.get(key);
    if (si != null){
      queue.remove(si);
      queueLookup.remove(key);
    }
    return o;
  }
  
    
  private class SetItem {
    public SetItem prev;
    public SetItem next;
    public Object data;

    public boolean equals(Object obj) {
      return data.equals(obj);
    }

    public int hashCode() {
      return data.hashCode();
    }
  }
  
  private class LRUQueue {

    /* Default queue minimal size */
    protected final static int MIN_SIZE_DEFAULT = 10;
    /* Default queue maximal size */
    protected final static int MAX_SIZE_DEFAULT = 100;
    /* Maximal size of the queue */
    protected int maxSize;
    /* Minimal size of the queue */
    protected int minSize;
    /* Current size of the queue */
    protected int size;
    /* First node of the queue */
    protected SetItem first;
    /* Last node of the queue */
    protected SetItem last;

    /* Constructor */
    public LRUQueue() {
      this(MIN_SIZE_DEFAULT, MAX_SIZE_DEFAULT);
    }

    /**
     * Constructor
     *
     * @param   maxSize Maximal queue size
     */
    public LRUQueue(int maxSize) {
      this(MIN_SIZE_DEFAULT, maxSize);
    }

    /**
     * Constructor
     *
     * @param   minSize Minimal queue size
     * @param   maxSize Maximal queue size
     */
    public LRUQueue(int minSize, int maxSize) {
      this.minSize = minSize;
      this.maxSize = maxSize;
      size = 0;
      first = null;
      last = null;
    }

    /**
     * Update the access frequency of an item and move it up if necessary
     *
     * @param node The node to be updated
     */
    public void update(SetItem node) {
      if (node != first) {
        if (last == node) {
          first = last;
          last = last.prev;
        } else {
          node.prev.next = node.next;
          node.next.prev = node.prev;
          node.next = first;
          node.prev = last;
          first.prev = node;
          last.next = node;
          first = node;
        }
      }
    }

    /**
     * Add a new item into the queue
     * Replace last if Queue Size exceeded
     *
     * @param node The node to be added
     */
    public SetItem add(SetItem node) {
      SetItem removed = null;

      if (first == null) {
        size = 1;
        node.prev = node;
        node.next = node;
        first = node;
        last = node;
        return null;
      } else {
        if (size < maxSize) {
          size++;
          node.next = first;
          node.prev = last;
          last.next = node;
          first.prev = node;
          first = node;
        } else {
          //adding before first
          node.next = first;
          node.prev = last.prev;
          last.prev.next = node;
          first.prev = node;
          first = node;
          //and deleting last
          removed = last;
          last = last.prev;
        }

        return removed;
      }
    }

    /**
     *
     * @return whether this queue is full or not
     */
    public boolean isFull() {
      return (size >= maxSize);
    }

    /**
     * Removes an item in the queue
     *
     * @param node The node to be removed
     */
    public SetItem remove(SetItem node) {
      SetItem snode = node;
      size--;

      if (size > 0) {
        if (first == node) {
          first = node.next;
          last.next = first;
          node.prev.next = node.next;
          node.next.prev = node.prev;
        } else if (last == node) {
          last = node.prev;
          first.prev = last;
          node.prev.next = node.next;
          node.next.prev = node.prev;
        } else {
          node.prev.next = node.next;
          node.next.prev = node.prev;
        }

        node.next = null;
        node.prev = null;
      } else {
        first = null;
        last = null;
      }

      return snode;
    }

    /**
     * Removes the last item of the queue
     */
    public SetItem removeLast() {
      return remove(last);
    }

    /**
     * Prints out to the screen the contents of the queue in first->last order
     */
    public void print() {
      SetItem temp;
//      System.out.print("QUEUE ");
//      System.out.print("(size:" + size + ")");

      if (first != null) {
        temp = first.next;

        while (temp != last) {
          temp = temp.next;
        }
      } else {
//        System.out.println("Queue empty.");
      }
    }

    /**
     * Get max Size of queue
     *
     * return maxSize
     */
    public int getMaxSize() {
      return maxSize;
    }

    /**
     * Get min Size of queue
     *
     * return minSize
     */
    public int getMinSize() {
      return minSize;
    }

    /**
     * Get size of queue
     *
     * return size
     */
    public int getSize() {
      return size;
    }

    /**
     * Empties this queue
     */
    public void clear() {
      size = 0;
      first = null;
      last = null;
    }
  }
  
  public static void main(String[] args){
     LRUHashMap lrum = new LRUHashMap(5);
     lrum.put(new String("key1"), new String("value1"));
     lrum.put(new String("key2"), new String("value2"));
     lrum.put(new String("key3"), new String("value3"));
     lrum.put(new String("key4"), new String("value4"));
     lrum.put(new String("key5"), new String("value5"));
     lrum.containsKey(new String("key3"));
     lrum.containsKey(new String("key2"));
     
     /*System.out.println(lrum.toString());
     boolean c = lrum.containsKey(new String("key1"));
     System.out.println(c);
     System.out.println(lrum.toString());
     lrum.get(new String("key1"));
     System.out.println(lrum.toString());
     lrum.put(new String("key6"), new String("value6"));
     System.out.println(lrum.toString());
     lrum.put(new String("key6"), new String("value66"));
     System.out.println(lrum.toString());*/
     
     lrum.put(new String("key7"), new String("value7"));
     lrum.put(new String("key8"), new String("value8"));
     lrum.put(new String("key9"), new String("value9"));
     lrum.put(new String("key10"), new String("value10"));
     lrum.put(new String("key11"), new String("value11"));
     lrum.put(new String("key12"), new String("value12"));
     System.out.println(lrum.toString());
     /*lrum.put(new String("key7"), new String("value7"));
     System.out.println(lrum.toString());
     lrum.put(new String("key8"), new String("value8"));
     System.out.println(lrum.toString());
     lrum.put(new String("key9"), new String("value9"));
     System.out.println(lrum.toString());
     lrum.put(new String("key10"), new String("value10"));
     System.out.println(lrum.toString());*/
     
     
       long start = System.currentTimeMillis();
       java.util.UUID uid = java.util.UUID.randomUUID();
       String s = uid.toString();
       long end = System.currentTimeMillis();
       System.out.println(s.length() + " [" + s + "]" + (end - start));
     
   
       
  }
}
