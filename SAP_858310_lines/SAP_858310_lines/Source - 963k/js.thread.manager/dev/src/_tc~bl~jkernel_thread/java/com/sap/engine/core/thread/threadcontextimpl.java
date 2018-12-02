/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.core.thread;

import com.sap.engine.core.Names;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ContextObjectNameIterator;
import com.sap.engine.frame.core.thread.Retainable;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.jvm.monitor.thread.ThreadLocalsRemover;
import com.sap.jvm.monitor.vm.VmInfo;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import java.util.HashMap;
import java.util.Iterator;

/**
 * TODO: describe why we need such an implementation
 * Thread Context implementation.
 * 
 * @author Krasimir Semerdzhiev, Elitsa Pancheva
 * @version 710
 */
public class ThreadContextImpl extends InheritableThreadLocal implements ThreadContext {

	/**
	 * The local map (Name -> ContextObject) for the current thread.
	 * This map doesn't have to be concurrent because could be accessed always only by one thread. 
	 */
	private HashMap<String, ContextObject> localContextObjectMap = null;
	
	/**
   * Location of the ThreadContextImpl class for tracing purposes. 
   */
  private final static Location location = Location.getLocation(ThreadContextImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	
  /**
   * BaseContext instance which keeps the currently registered ContextObjects by name and ID. 
   * A ContextObject is moved from BaseContext to the local ContextObjectTable on first request.
   */
  private static BaseContext baseContext = new BaseContext();
  
  /**
   * Static ThreadContext
   */
  private static ThreadContextImpl current = new ThreadContextImpl(false);
  
  /**
   * ThreadLocalsRemover is used to remove ThreadLocals from java threads when thread pool is used
   */
  private static ThreadLocalsRemover threadLocalsRemover = null;
  
  /**
   * Specifies whether the thread locals should be removed. The removal behavior is specified with property of Application Thread Manager
   */
  private static boolean toCleanThreadLocals = true;
  
  static {
  	try { 
  	  if (ContextDataImpl.VM_MONITORING_ENABLED) {
  	    threadLocalsRemover = ThreadLocalsRemover.getInstanceOnce();
    	}
  	} catch (Exception e) {
  		try {
  		  SimpleLogger.trace(Severity.WARNING, location, 
  				  			"ASJ.krn_thd.000087", 
  				  			"The Utility class [ThreadLocalsRemover] for removing ThreadLocal instances failed to initialized due to [{0}]",
  				  			e);
  		  
  		  
  		  if (location.bePath()) {
  		    location.traceThrowableT(Severity.INFO, "Failed to initialize ThreadLocalsRemover", e);
  		  }
  		} catch (Exception e2) {
  		  //$JL-EXC$ threadLocalsRemover stays null
  		}
  	} catch (NoClassDefFoundError er) {
  		try {
  		  SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000014", 
  				  			"The Utility class [ThreadLocalsRemover] for removing ThreadLocal instances failed to initialized due to [{0}]", 
  				  			er);
  		  if (location.bePath()) {
  		    location.traceThrowableT(Severity.INFO, "Failed to initialize ThreadLocalsRemover", er);
  		  }
  		} catch (Exception e2) {
  		  //$JL-EXC$ threadLocalsRemover stays null
  		}
  	}
  }
  
  /**
   * Boolean flag showing if the current thread context is for system or application thread. 
   * "true" - system, "false" - application. 
   */
  private boolean isSystem = false;
  
  /**
   * Constructor, to be used only in the InheritableThreadLocal contract
   * 
   * @param isSystem indicates whether the current thread context represents a System or Application thread. 
   */
  private ThreadContextImpl(boolean isSystem) {
    super();
    this.isSystem = isSystem;
    this.localContextObjectMap = new HashMap<String, ContextObject>(BaseContext.initialSize);
  }
    
  /**
   * Get ContextObject instance that is connected to the current thread by id.
   * If such an object doesn't exists return <code>null</code>.
   *
   * @param   id  the id of context object
   * @deprecated All methods for ContextObjects that work with IDs are deprecated and must not be used.
   * The proper way to work with ContextObjects is by using String names. 
   */
  public ContextObject getContextObject(int id) {
    try {
    	String coName = baseContext.getContextObjectName(id);
    	if (coName == null) {
    		if (location.bePath()) {
      		location.pathT("Getting context object by ID ["+id+"] which is not existing => Return NULL");
      	}
    		return null;
    	}
    	ContextObject co = getContextObject(coName);
    	if (location.bePath()) {
    		location.pathT("Getting context object by ID ["+id+"] and name ["+coName+"]. Returned --> " + co);
    	}
    	return co;
    } catch (Exception e) {
    	location.traceThrowableT(Severity.PATH, "Exception caught at getContextObject("+id+") => will return null", e);
      // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log here
      // Please do not remove this comment !
      return null;
    }
  }

  /**
   * Return the id of context object and -1 if it doesn't exist.
   *
   * @param    name   the name of the object
   * @deprecated All methods for ContextObjects that work with IDs are deprecated and must not be used.
   * The proper way to work with ContextObjects is by using String names. 
   */
  public int getContextObjectId(String name) {
  	return baseContext.getContextObjectId(name);
  }

  /**
   * Set ContextObject instance for current thread.
   *
   * @param   id  the id of context object
   * @param   obj ContextObject to associate with this ID.
   * @deprecated All methods for ContextObjects that work with IDs are deprecated and must not be used.
   * The proper way to work with ContextObjects is by using String names. 
   *
   */
  public void setContextObject(int id, ContextObject obj) { 
  	if (location.bePath()) {
  		location.pathT("Setting context object ["+obj+"] with ID ["+id+"]");
  	}
  		
  	String name = baseContext.getContextObjectName(id);
  	if (name == null) { // such ID doesn't exist
  		if (location.beWarning()) {//TODO: Once the stack is cleaned up - decrease the severity
        SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000016",
        					"ThreadContextImpl.setContextObject() is called for ID: [{0}] which is not existing! Cannot set value for ContextObject [{1}] which is not previously registered. To see more information switch severity to DEBUG.", 
        					id, obj);
        if (location.beDebug()) {
          location.traceThrowableT(Severity.DEBUG, "Wrong usage of setContextObject() method, Caller stack trace follows.", new Exception("Wrong usage of setContextObject() method - ContextObject with ID "+id+" is not registered in ThreadContext!"));
        }
      }
  		throw new IllegalStateException("ThreadContextImpl.setContextObject() is called for ID: "+id+" which is not existing! Cannot set value for ContextObject ["+obj+"] which is not previously registered.");
  	}
  	
    setContextObject(name, obj);
  }

  /**
   * Get ContextObject instance that is connected to the current thread by name.
   * If such an object doesn't exists return <code> null </code>
   *
   * @param   name  the name of context object
   * @return ContextObject, associated with that name
   */
  public ContextObject getContextObject(String name) {
//    System.out.println("GETTING CONTEXT OBJECT BY NAME ["+name+"] ...");
  	if (name == null) {
    	throw new NullPointerException("The name of the ContextObject cannot be null");
    }
  	ContextObject result = null;
  	ContextObject initialValue = null;
  	// first try to return the object from the local context object map
  	if ((result = localContextObjectMap.get(name)) != null) {
  		return result;
  	} else {
  		// if the name is not found in the local ContextObject map => try to find it in the BaseContext.
  		result = baseContext.getContextObject(name);
  		if (location.bePath()) {
    		location.pathT("Get context object with name ["+name+"] from baseContext");
    	}
  		if (result != null) {
  			try {
          // there is such ContextObject => get its initial value, save it in the local ContextObject map and return 
    			// it to the caller.
  				initialValue = result.getInitialValue();
  				
  			} catch (Exception e) {
  				SimpleLogger.trace(Severity.ERROR, location,
  									LoggingUtilities.getDcNameByClassLoader(result.getClass().getClassLoader()),
  									null,
  									"ASJ.krn_thd.000081",
  									"Initial value of ContextObject [{0}] registered with name [{1}] throws Exception [{2}]! To see the full stack trace switch the severity to PATH. getContextObject() method will return NULL to the caller.",
  									e,
  									result, name, e);
  				if (location.bePath()) {
  	    		location.traceThrowableT(Severity.PATH, "Initial value of ContextObject ["+result+"] registered with name ["+name+"] throws the following Exception.", e);
  	    	}
  				return null;
  			}
  			
  			// put only if not null! some ContextObjects are bad guys
  			if (initialValue != null) {
          // check if initial value is the same instance as the one initially registered in ThreadContext and log warning in this case.
  				if (initialValue == result) {
  					SimpleLogger.trace(Severity.WARNING, location, 
			  							LoggingUtilities.getDcNameByClassLoader(initialValue.getClass().getClassLoader()),
											null,
  										"ASJ.krn_thd.000018", 
  									  "Initial value of ContextObject [{0}] registered with name [{1}] returns the same context instance [{2}] as the one registered in ThreadContext! This is identified as fault behavior and can lead to hard to find problems. initialValue and childValue methods of ContextObject implementations must always return new instance of the context object.",
  									  null,
  									  result, name, initialValue);
  				}
  				if (location.bePath()) {
        		location.pathT("Put initial value ["+initialValue+"] of context object with name ["+name+"] in the thread's local table");
        	}
  			  localContextObjectMap.put(name, initialValue);
  			} else {
  				SimpleLogger.trace(Severity.WARNING, location, 
  									LoggingUtilities.getDcNameByClassLoader(result.getClass().getClassLoader()),
  									null,
  									"ASJ.krn_thd.000020", 
  									"Initial value of ContextObject [{0}] registered with name [{1}] returns NULL! This is identified as fault behavior and can lead to hard to find problems. " +
      							"initialValue and childValue methods of ContextObject implementations must always return new instance of the context object.", 
      							null,
      							result, name);
  			}
  		}
  	}
    return initialValue;
  }

  /**
   * Get names of all context objects that are registered.
   *
   * @return  Iterator to all the registered contextObjectNames
   */
  public ContextObjectNameIterator getContextObjectNames() {
//  	if (location.bePath()) {
//  		location.pathT("getContextObjectNames isSystem ["+isSystem+"] thread name ["+Thread.currentThread().getName()+"] caller stack trace:");
//  		location.traceThrowableT(Severity.PATH, "getContextObjectNames stack trace ", new Exception("getContextObjectNames stack trace"));
//  	}
  	return baseContext.getContextObjectNames();
  }

  /**
   * Get names of all context objects that are registered and are transferable
   * If one object is transferrable then remote protocols will try to keep it
   * into thread context during remote calls. Such an objects are session,
   * transactions, etc...
   *
   * @return Iterator to all the registered contextObjectNames, that implement
   * the Transferable interface
   */
  public ContextObjectNameIterator getTransferableContextObjectNames() {
    return baseContext.getTransferableContextObjectNames();
  }

  /**
   * Register object that is connected to a thread. Depends from which thread
   * you access it, the object has different instance. You can access the
   * object by name. Returns -1 if working on client side and value >= 0 if
   * working on cluster node.
   *
   * @param   name    the name of context object
   * @param   object  Object to register with this name
   */
  public int registerContextObject(String name, ContextObject object) {
//    System.out.println("REGISTER ["+name+"] with object ["+object+"]");
    return baseContext.registerContextObject(name, object);
  }

  /**
   * Set ContextObject instance for current thread.
   *
   * @param   name    name of the context object
   * @param   obj  Object to set to that name, but just to the local ThreadContext instance
   * @throws NullPointerException in case any of the arguments is null.
   */
  public void setContextObject(String name, ContextObject obj) {

  	if (name == null) {
      throw new NullPointerException("The name of the ContextObject cannot be null");
    }

    if (obj == null) {
      if (location.beWarning()) { //TODO: severity to be decreased.
        SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000094", 
        					"ThreadContextImpl.setContextObject() is called with ContextObject argument null! This can lead to unexpected and hard to find problems. To see more information switch severity to DEBUG.");
        if (location.beDebug()) {
          location.traceThrowableT(Severity.DEBUG, "Wrong usage of setContextObject() method, Caller stack trace follows.", new Exception("Wrong usage of setContextObject() method - ContextObject argument is null!"));
        }
      }
      localContextObjectMap.remove(name); // TODO add javadoc if removing stays
    }
    
    if (location.bePath()) {
  		location.pathT("Setting context object ["+obj+"] with name ["+name+"]");
  	}
    // put it locally no matter if the ContextObject with this name is registered or not
    // no ID is available for this object and it is not visible in transferables also
    localContextObjectMap.put(name, obj);
    
    
    // trace warning in case the name is not found in baseContext
    if (baseContext.getContextObject(name) == null) {
    	if (location.beWarning()) {
    		String dcName = null;
      	if (obj != null) {
      		dcName = LoggingUtilities.getDcNameByClassLoader(obj.getClass().getClassLoader());
      	}
    		SimpleLogger.trace(Severity.WARNING, location, 
    				dcName,
						null,
    				"ASJ.krn_thd.000010", 
    				"ThreadContextImpl.setContextObject() is called for ContextObject [{0}] with name [{1}] which is not registered in ThreadContext! ContextObject will be set only locally in the current thread which can lead to unexpected and hard to find problems. To see more information switch severity to DEBUG.", 
    				null,
    				obj, name);
        if (location.beDebug()) {
          location.traceThrowableT(Severity.DEBUG, "Wrong usage of setContextObject() method, Caller stack trace follows.", new Exception("Wrong usage of setContextObject() method - ContextObject with name ["+name+"] is not registered in ThreadContext!"));
        }
      }
    }
  }

  /**
   * Unregister object that is attached to a thread - suitable only for client VMs. 
   *
   * @param   name name of context object
   */
  public void unregisterContextObject(String name) {
  	baseContext.unregisterContextObject(name);
    ContextObject co = localContextObjectMap.remove(name);
    if (location.bePath()) {
  		location.pathT("Unregister context object with ["+name+"]. Object removed is: " + co);
  	}
    if (co != null) {
    	try {
    	  co.empty();
    	  co = null;
      } catch (Exception e) {
      	String dcName = null;
      	if (co != null) {
      		dcName = LoggingUtilities.getDcNameByClassLoader(co.getClass().getClassLoader());
      	}
      	SimpleLogger.trace(Severity.ERROR, location, 
      			dcName,
      			null,
      			"ASJ.krn_thd.000103",
      			"empty() method of ContextObject [{0}] registered with name [{1}] throws Exception [{2}]! Cannot clean up this ContextObject.", e, co, name, e.toString());
			} catch (OutOfMemoryError oom) {
				ProcessEnvironment.handleOOM(oom);
			} catch (ThreadDeath td) {
				throw td;
			} catch (Throwable t) {
				String dcName = null;
				if (co != null) {
      		dcName = LoggingUtilities.getDcNameByClassLoader(co.getClass().getClassLoader());
      	}
				SimpleLogger.trace(Severity.ERROR, location, 
      			dcName,
      			null,
      			"ASJ.krn_thd.000104",
      			"empty() method of ContextObject [{0}] registered with name [{1}] throws Error [{2}]! Cannot clean up this ContextObject.", t, co, name, t.toString());
			}
    }
  }

  /**
   * Returns the calling thread's initial value for this ThreadLocal
   * variable. This method will be called once per accessing thread for
   * each ThreadLocal, the first time each thread accesses the variable
   * with get or set.  If the programmer desires ThreadLocal variables
   * to be initialized to some value other than null, ThreadLocal must
   * be subclassed, and this method overridden.  Typically, an anonymous
   * inner class will be used.  Typical implementations of initialValue
   * will call an appropriate constructor and return the newly constructed
   * object.
   *
   * @return the initial value for this ThreadLocal
   */
  protected Object initialValue() {
  	if (location.bePath()) {
  		location.pathT("Initial Value is called (isSystem: "+isSystem+" )");
  	}
    return new ThreadContextImpl(isSystem);
  }

  /**
   * Computes the child's initial value for this InheritableThreadLocal
   * as a function of the parent's value at the time the child Thread is
   * created.  This method is called from within the parent thread before
   * the child is started.
   * <p>
   * This method merely returns its input argument, and should be overridden
   * if a different behavior is desired.
   *
   * @param parentValue the parent thread's value
   * @return the child thread's initial value
   */
  protected Object childValue(Object parentValue) {
  	ThreadContextImpl parent = ((ThreadContextImpl)parentValue);
    ThreadContextImpl child = new ThreadContextImpl(parent.isSystem());
    if (!parent.isSystem()) {
    	//updateParent(parent);    	
      child.inheritCOMap(parent.localContextObjectMap);
    }
    
    if (location.bePath()) {
  		location.pathT("Child value is called (isSystem: "+parent.isSystem+" ) parent map is [" + parent.localContextObjectMap + "] child map is " + child.localContextObjectMap);
  	}
    
    return child;
  }
  
//  private void updateParent(ThreadContextImpl parent) {
//  	/*
//  	 * wseki pat pri child value na celia context - sabiraj wsichkite context object-i 
//  	 * or central parent-a, initial value - put w current thread, 
//  	 * a child value - w inherited context-a. 
//  	 */
//  	ContextObjectNameIterator iterrator = baseContext.getContextObjectNames();
//  	ContextObject result = null;
//  	String name = null;
//  	while (iterrator.hasNext()) {
//      name = iterrator.nextName();
//  		result = baseContext.getContextObject(name).getInitialValue();
//  		if (location.bePath()) {
//    		location.pathT("updateParent initialValue of CO ["+name+"] returns: ["+result+ "]");
//    	}
//  		if (result != null) {
//  		  parent.localContextObjectMap.putIfAbsent(name, result);
//  		}
//  	} 
//  	if (location.bePath()) {
//  		location.pathT("updateParent is called as a result parent map is ["+parent.localContextObjectMap + "]");
//  	}
//  }

  public HashMap<String, ContextObject> inheritClean(ThreadContextImpl parentValue, HashMap<String, ContextObject> childCOMap) {
  	if (childCOMap == null) {
    	childCOMap = new HashMap<String, ContextObject>(BaseContext.initialSize); 
    }
  	
  	HashMap<String, ContextObject> pCoTable = parentValue.localContextObjectMap;
  	if (location.bePath()) {
  		location.pathT("inheritClean parent ContextObject map ["+pCoTable +"] to child ContextObject map ["+childCOMap+"]");
  	}
        
    return inheritInternal(pCoTable, childCOMap, true); 
  }

  public HashMap<String, ContextObject> inherit(ThreadContextImpl parentValue, HashMap<String, ContextObject> childCOMap) {
  	if (childCOMap == null) {
  		childCOMap = new HashMap<String, ContextObject>(BaseContext.initialSize);
  	}
  	HashMap<String, ContextObject> pCoTable = parentValue.localContextObjectMap;
  	if (location.bePath()) {
  		location.pathT("inherit parent ContextObject map ["+pCoTable+ "] to child ContextObject map ["+childCOMap+"]");
  	}
  	  	    
    return inheritInternal(pCoTable, childCOMap, false);
  }
  
  private HashMap<String, ContextObject> inheritInternal(HashMap<String, ContextObject> pCoTable, HashMap<String, ContextObject> childCOMap, boolean cleanInheritance) {
  	Object[] names = pCoTable.keySet().toArray();
    String tname = null;
    ContextObject childValue = null;
    ContextObject currentValue = null;
    
    for (int i=0; i<names.length; i++) {
    	
			tname = (String) names[i]; 
			currentValue = pCoTable.get(tname);
			
			if (currentValue != null) {
				if (cleanInheritance && !(currentValue instanceof Retainable)) {
					continue;
				}
				try {
			    
					childValue = currentValue.childValue(currentValue, null);
				
				} catch (Exception e) {
					SimpleLogger.trace(Severity.ERROR, location,
							LoggingUtilities.getDcNameByClassLoader(currentValue.getClass().getClassLoader()),
							null,
							"ASJ.krn_thd.000009",
							"Child value of ContextObject [{0}] registered with name [{1}] throws Exception [{2}]! To see the full stack trace switch the severity to PATH. This context object will be skipped from the inheritance.",
							e,
							currentValue, tname, e.toString());
  				if (location.bePath()) {
  	    		location.traceThrowableT(Severity.PATH, "Child value of ContextObject ["+currentValue+"] registered with name ["+tname+"] throws the following Exception.", e);
  	    	}
  				continue;
  			} 
			    
        if (childValue != null) {			
		  	  
        	childCOMap.put(tname, childValue);
		  	  
        	if (currentValue == childValue) {
        		SimpleLogger.trace(Severity.WARNING, location, 
        				LoggingUtilities.getDcNameByClassLoader(currentValue.getClass().getClassLoader()),
  							null,
        				"ASJ.krn_thd.000011", "Child value of ContextObject [{0}] registered with name [{1}] returns the same context instance [{2}]! " +
        				"This is identified as fault behavior and can lead to hard to find problems. initialValue and childValue methods of ContextObject implementations must always return new instance of the context object.", 
        				null,
        				currentValue, tname, childValue);
		  	  }
        } else {
        	SimpleLogger.trace(Severity.WARNING, location, 
        			LoggingUtilities.getDcNameByClassLoader(currentValue.getClass().getClassLoader()),
							null,
  						"ASJ.krn_thd.000021",
  						"Child value of ContextObject [{0}] registered with name [{1}] returns NULL! This is identified as fault behavior and can lead to hard to find problems. initialValue and childValue methods of ContextObject implementations must always return new instance of the context object.",
  						null,
  						currentValue, tname);
        }
			} else {
				SimpleLogger.trace(Severity.WARNING, location, 
						"ASJ.krn_thd.000019",
						"Get value of ContextObject registered with name [{0}] returns NULL!. This context object will be skipped from the inheritance.", 
						null,
						tname);
			}
		}
    
  	return childCOMap;
  }
  
  private void inheritCOMap(HashMap<String, ContextObject> pCoTable) {
    if (location.bePath()) {
  		location.pathT("inheritCOMap  parent ContextObject map ["+pCoTable+ "] local ContextObject map for this thread is ["+localContextObjectMap+"]");
  	}
//    Enumeration<String> names = pCoTable.keys();
//    String tname = null;
//    while (names.hasMoreElements()) {
//			tname = names.nextElement();//TODO: better handling of throwables, crashing our of ChildValue and initialValue! 
//    	localContextObjectMap.put(tname, (pCoTable.get(tname)).childValue(pCoTable.get(tname), null));
//		}
    inheritInternal(pCoTable, localContextObjectMap, false);
  }

  public HashMap<String, ContextObject> setCOTable(HashMap<String, ContextObject> newTable) {
  	if (location.bePath()) {
  		location.pathT("setCOTable  new ContextObject map ["+newTable+ "] will replace the locally existing one ["+localContextObjectMap+"]");
  	}
  	HashMap<String, ContextObject> table = localContextObjectMap;
    localContextObjectMap = newTable;
    return table;
  }
  
  //supposed to be used ONE AND ONLY for executing a Runnable in the same thread! 
  public HashMap<String, ContextObject> getCOTable() {
    //we need a new copy here, as the original one will be wiped out by the ContextDataImpl <-> ThreadContext contract during the execution. 
  	return new HashMap<String, ContextObject>(localContextObjectMap);
  }

  public static ThreadContext getThreadContext() {
    return (ThreadContext)ThreadContextImpl.current.get();
  }
  
  public void enableThreadLocalsCleanup(boolean enabled) {
  	if (location.beInfo()) {
  		location.traceThrowableT(Severity.INFO, "Cleanup of ThreadLoclas when the thread is returned back in the engine pool will be " + ((enabled)?"enabled":"disabled"), new Exception("The caller stack trace follows"));
  	}
  	toCleanThreadLocals = enabled;
  }
  
  private void cleanThreadLocals() {
  	if (toCleanThreadLocals) {
	  	if (threadLocalsRemover != null) {
	  	  try {
	  	  	threadLocalsRemover.removeAllThreadLocals();
	  	  	// only if the thread is a system one we have to recreate the thread context and mark it as system
	  	  	if (isSystem) {
	  	  		((ThreadContextImpl) getThreadContext()).markAsSystem();
	  	  	}
	  	  	// get MonitoringInfo to associate value in thread local table. We need every thread to have value of MonitoringInfo in its
	  	  	// thread local table so that when a new thread is spawned MonitoringInfo.childValue() will be called in parent thread and the
	  	  	// operation Id will be correctly propagated to the child thread.
	  	  	JavaThreadsCallback.info.get();
	  	  	if (location.bePath()) {
	  	  	  location.pathT("ThreadLocals are successfully cleaned for thread ["+Thread.currentThread()+"] ThreadContext is initialized anew with type [" + (isSystem?"system":"not_system") + "]");
	  	  	}  	  	
	  	  } catch (Exception e) {
	  	  	if (location.bePath()) {
	  	  	  location.traceThrowableT(Severity.PATH, "Cannot remove ThreadLoacals from the current thread ["+Thread.currentThread()+"]", e);
	  	  	}
	  	  }
	  	}
  	}
  }
  
  private void resetThreadResourceCounters() {
    // first check if VM monitoring functionality is available 
    if (ContextDataImpl.VM_MONITORING_ENABLED) { 
    	try {
        // for SAP JVM thread stack dumps and accounting infrastructure - reset the CPU and memory counters when the thread is returned back in the pool
        VmInfo.resetThreadStackDumpCounters();
    	} catch (ThreadDeath td) {
    		throw td;
    	} catch (OutOfMemoryError oom) {
    		ProcessEnvironment.handleOOM(oom);
    	} catch (Throwable e) {
  	  	if (location.bePath()) {
  	  	  location.traceThrowableT(Severity.PATH, "Cannot reset the resource counters in the current thread ["+Thread.currentThread()+"]", e);
  	  	}
  	  }
    } 
	}
  
  public void empty() {
  	this.empty(true);
  }
  
  public void empty(boolean beforeReleaseInPool) {
  	try {
  		Object[] names = localContextObjectMap.keySet().toArray();
	  	ContextObject co = null;
	  	String coName = null;
	  	for (int i=0; i<names.length; i++) {
	  		coName = (String) names[i];
	  		co = localContextObjectMap.get(coName);
	      if (co != null) {
	        try {
	      	  co.empty();
	        } catch (Exception e) {
	        	SimpleLogger.trace(Severity.ERROR, location, 
	        			LoggingUtilities.getDcNameByClassLoader(co.getClass().getClassLoader()),
								null,
								"ASJ.krn_thd.000103",
								"empty() method of ContextObject [{0}] registered with name [{1}] throws Exception [{2}]! Cannot clean up this ContextObject.", e, co, coName, e.toString());
	  				continue;
	  			} catch (OutOfMemoryError oom) {
	  				ProcessEnvironment.handleOOM(oom);
	  			} catch (ThreadDeath td) {
	  				throw td;
	  			} catch (Throwable t) {
	  				SimpleLogger.trace(Severity.ERROR, location, 
	        			LoggingUtilities.getDcNameByClassLoader(co.getClass().getClassLoader()),
								null,
								"ASJ.krn_thd.000104",
								"empty() method of ContextObject [{0}] registered with name [{1}] throws Error [{2}]! Cannot clean up this ContextObject.", t, co, coName, t.toString());
	  				continue;
	  			}
	      }
	    }
	  	if (beforeReleaseInPool) { //we should not clean thread locals and resource metrics when Executable.process() method is called
		  	// clean ThreadLocals of the current thread before releasing  to the pool
		  	cleanThreadLocals();
		  	// reset resource (CPU, memory, io) counters before releasing the thread in the pool
		  	resetThreadResourceCounters();
	  	}
	  	
	  	if (location.bePath()) {
	  	  location.pathT("ThreadContext is successfully cleaned for thread ["+Thread.currentThread()+"]");
	  	}
  	} finally {
  	  localContextObjectMap.clear();
  	}
  }

  public boolean isSystem() {
    return isSystem;
  }

  public void setSystem(boolean isSystem) {
//    System.out.println("Set System (current:"+isSystem+" ) (new:"+state+" )");
    this.isSystem = isSystem;
    if (isSystem) {
        empty();
    }
  }
  
  public void markAsSystem() {
    this.isSystem = true;
  }

}

