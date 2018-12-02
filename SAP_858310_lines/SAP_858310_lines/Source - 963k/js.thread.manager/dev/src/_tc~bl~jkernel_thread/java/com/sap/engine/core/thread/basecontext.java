package com.sap.engine.core.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.frame.client.ContextObjectNameIteratorImpl;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ContextObjectNameIterator;
import com.sap.engine.frame.core.thread.Transferable;
import com.sap.engine.lib.util.ConcurrentReadHashMapIntObj;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * The common data for all ThreadContext instances is kept in this class. 
 * 
 * @author Elitsa Pancheva
 * @version 7.10
 * */
public class BaseContext {
	
  /**
   * The initial size of all the maps and sets used in this class 
   */
  static final int initialSize = 50;	
  
  /**
   * The central ContextObject Map (Name -> ContextObject instance) used to keep all the registered ContextObjects. 
   */
  private ConcurrentHashMap<String, ContextObject> centralCOMap = new ConcurrentHashMap<String, ContextObject>(initialSize);
  
  /**
   * The central ContextObject Map (ID -> Name) used to keep all the registered ContextObjects' names and IDs.
   * We need this map because in the ThreadContext interface there are methods to get and set ContextObjects by name. 
   */
  private ConcurrentReadHashMapIntObj centralIdNameMap = new ConcurrentReadHashMapIntObj(initialSize);
  
  /**
   * AtomicInteger to be used to create unique increasing IDs for registered ContextObjects.
   */
  private AtomicInteger idAllocator = new AtomicInteger(0);
  
  /**
   * A set keeping the names of all ContextObjects that implement Transferable interface. Only these ContextObjects are 
   * supposed to be transferred at client side.
   */
  private CopyOnWriteArraySet<String> transferableNames = new CopyOnWriteArraySet<String>();
  
  /**
   * Location of the BaseContext class for tracing purposes. 
   */
  private final static Location location = Location.getLocation(BaseContext.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  
  /**
   * Return the id of context object and -1 if it doesn't exist.
   * This operation is pretty slow so avoid using it in scenarios where the performance matters.
   * 
   * @deprecated All methods for ContextObjects that work with IDs are deprecated and must not be used.
   * The proper way to work with ContextObjects is by using String names. 
   * 
   * @param    name - the name of the object
   */
  public int getContextObjectId(String name) {
    if (name == null) {
    	throw new NullPointerException("The name of the ContextObject cannot be null");
    }
	  int[] keys = centralIdNameMap.getAllKeys();
    for (int i = 0; i < keys.length; i++) {
      if (name.equals(centralIdNameMap.get(keys[i]))) {
    	  return keys[i];
      }
	  }
    return -1;
  }

  /**
   * Get ContextObject instance from the central CO table.
   * If such an object doesn't exists return <source> null <source>
   * 
   * @param   name - the name of the context object
   */
  public ContextObject getContextObject(String name) {
	  return centralCOMap.get(name);
  }
  
  /**
   * Gets the name of the ContextObject instance for the specified ID.
   *
   * @param   id - the id of context object
   * @deprecated All methods for ContextObjects that work with IDs are deprecated and must not be used.
   * The proper way to work with ContextObjects is by using String names. 
   */
  public String getContextObjectName(int id) { 
	  return (String) centralIdNameMap.get(id);
  }
  
  /**
   * Register ContextObject in the central CO table. This ContextObject will be registered in the thread 
   * on first request, i.e. lazy registration is performed.
   * Every thread that requests this ContextObject and its children will get in their inheritable thread local tables
   * different instances of this ContextObject. On first user request with the current Thread will be associated initialValue 
   * of the ContextObject. If this thread starts a child thread - childValue of the ContextObject will be associated with the
   * child thread.  
   * The ContextObject can be requested by name.
   * Usually performed at service start.
   * 
   * @param  name    Name of the ContextObject
   * @param  object  ContextObject to be registered
   * @return ID of the registered object. Returns -1 if working on client side and value >= 0 if
   *         working on server process.
   */ 
  public int registerContextObject(String name, ContextObject object) { 
  	//synchronization is not needed due to the atomic behavior of centralCOMap

    if (name == null) {
      throw new NullPointerException("The name of the ContextObject cannot be null");
    }
    
    if (object == null) {
      throw new NullPointerException("The ContextObject instance cannot be null");
    }

		// Check if ContextObject with that name is already registered and if not - register it. 
	  Object returnedValue = centralCOMap.putIfAbsent(name, object);
	  
	  if (returnedValue != null) { 		
	  	// this call was not able to put the ContextObject in the map because there was already a key with this name
	  	//throw new IllegalStateException("Cannot register ContextObject ["+object+"] with name ["+ name + "]. ContextObject ["+returnedValue+"] with such name is already registered in ThreadContext.");
	  	SimpleLogger.trace(Severity.WARNING, location, 
	  			LoggingUtilities.getDcNameByClassLoader(object.getClass().getClassLoader()),
					null,
					"ASJ.krn_thd.000085",
					"Cannot register ContextObject [{0}] with name [{1}]. ContextObject [{2}] with such name is already registered in ThreadContext => return the ID of the old ContextObject.",
					null,
					object, name, returnedValue);
	  	return getContextObjectId(name);
	  }

		int newID = idAllocator.incrementAndGet();
	  //registration passed successfull - continue with the rest of it. Maintained only for the deprecated methods.  
	  centralIdNameMap.put(newID, name); 	
		 
		if (object instanceof Transferable) {
		  transferableNames.add(name);
		}
	    
		return newID;
  }
    
  /**
   * Unregister Context Object - usually performed at service stop.
   * Removes the ContextObject from the central CO table. Does not remove the instances of the ContextObject
   * associated with Threads so far.
   *
   * @param name name of the object to be unregistered.
   */
  public void unregisterContextObject(String name) {
    if (name == null) {
      throw new NullPointerException("The name of the ContextObject cannot be null");
    }
  	
  	ContextObject co = centralCOMap.get(name);
  	if (co != null) {
  		//It's important to keep the reversed order of the operations that are performed during registration to 
  		//make it possible not to have synchronization of the operations and still operate consistently.

  		if (co instanceof Transferable) {
  	  	transferableNames.remove(name);
  	  }

    	int id = getContextObjectId(name);
    	if (id >= 0) {
        centralIdNameMap.remove(id);
  	  }
      
    	centralCOMap.remove(name);
  	} 
  }
  
  public ContextObjectNameIterator getTransferableContextObjectNames() {
  	return new ContextObjectNameIteratorImpl(transferableNames.toArray());
  }
  
  public ContextObjectNameIterator getContextObjectNames() {
  	return new ContextObjectNameIteratorImpl(centralCOMap.keySet().toArray());
  }

}