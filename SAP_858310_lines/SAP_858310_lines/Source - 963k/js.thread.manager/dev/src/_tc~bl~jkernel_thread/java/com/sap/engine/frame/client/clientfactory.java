/*
 * ...Copyright...
 */
package com.sap.engine.frame.client;

import com.sap.engine.frame.core.thread.ClientIDPropagator;
import com.sap.engine.frame.core.thread.ClientMonitorThreadBuilder;
import com.sap.engine.frame.core.thread.ThreadRuntimeInfoProvider;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingManager;
import com.sap.tc.logging.Severity;
import com.sap.engine.core.Names;


public class ClientFactory {

  private static ClientThreadContextFactory threadContextFactory;
  
  private static ClientIDPropagator propagator = new ClientIDPropagator(null, false);
  
  private static Location location = Location.getLocation(ClientFactory.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);

  public synchronized static ClientThreadContextFactory getThreadContextFactory() throws ClientException {
    if (threadContextFactory != null) {
    	ClientIDPropagator trid = (ClientIDPropagator) threadContextFactory.getThreadContext().getContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME);
    	if (trid != null) {
    		threadContextFactory.getThreadContext().setContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME, trid);
    	}
      return threadContextFactory;
    } else {
      String className = null;
      try {
        try {
          className = System.getProperty("CLIENT_THREAD_CONTEXT_FACTORY_CLASS");
        } catch (SecurityException e) {
          // the jvm is running in a restricted security policy
          // probably the jvm runs an applet. 
          className = null;
        }

        if (className == null) {
          className = "com.sap.engine.frame.client.ClientThreadContextImpl";
        }

        Class threadContextFactoryClass = Class.forName(className);
        threadContextFactory = (ClientThreadContextFactory) threadContextFactoryClass.newInstance();
        // register info provider in logging manager on client side
        try {
	        if (!ThreadRuntimeInfoProvider.isAlreadyRegistered()) {
	        	ThreadRuntimeInfoProvider infoProvider = new ThreadRuntimeInfoProvider();
	          LoggingManager.registerThreadRuntimeInfoProvider(infoProvider);
	          infoProvider.setRegistered();
	        }	
        } catch (ThreadDeath td) {
        	throw td;
        } catch (OutOfMemoryError oom) {
        	throw oom;
        } catch (Throwable e) {
        	location.traceThrowableT(Severity.PATH, "Registration of thread info provider in logging manager has failed", e);
        }
        
        try {
          // set propagator ContextObject in ThreadContext
	        threadContextFactory.getThreadContext().setContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME, propagator);
	        // get it once to create initial value before hook in the logging is registered. Otherwise we can get StackOverFlow because the 
	        // initialValue() uses logging API
	        threadContextFactory.getThreadContext().getContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME);
	        // set monitoring thread builder in ThreadWrapper to enable transaction id functionality on client side
	        ThreadWrapper.setMonitorThreadBuilder(new ClientMonitorThreadBuilder(), true);
        } catch (Exception e) {
        	location.traceThrowableT(Severity.PATH, "Creation of ClientIDPropagator context object has failed", e);
        }
        
        return threadContextFactory;
      } catch (ClassNotFoundException cnfException) {
        throw new ClientException("Class " + className + " not found", cnfException);
      } catch (InstantiationException iException) {
        throw new ClientException("Can't instantiate object from class " + className, iException);
      } catch (IllegalAccessException iaException) {
        throw new ClientException(iaException);
      }
    }
  }
   

}
