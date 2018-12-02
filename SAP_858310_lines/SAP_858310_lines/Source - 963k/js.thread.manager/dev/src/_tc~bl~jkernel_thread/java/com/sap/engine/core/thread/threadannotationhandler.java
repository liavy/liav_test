package com.sap.engine.core.thread;

import java.util.HashMap;

import com.sap.engine.core.Names;
import com.sap.jvm.monitor.vm.ThreadAnnotation;
import com.sap.jvm.monitor.vm.ThreadAnnotationKey;
import com.sap.tc.logging.Location;

public class ThreadAnnotationHandler {
	
	private static Location location = Location.getLocation(ThreadAnnotationHandler.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	
	public final static boolean THREAD_ANNOTATIONS_AVAILABLE;
	
	static {
		boolean hasThreadAnnotations = false;
		try {
			if (ContextDataImpl.VM_MONITORING_ENABLED) {
				// call to the ThreadAnnotations class to see if the class is accessible on the system.
				// if the system is running with older VM the class won't be accessible
				ThreadAnnotation.class.getClassLoader();
				hasThreadAnnotations = true;
			}
		} catch (Exception e) {
			hasThreadAnnotations = false;
		} catch (NoClassDefFoundError ne) {
			hasThreadAnnotations = false;
		}
		THREAD_ANNOTATIONS_AVAILABLE = hasThreadAnnotations;
	}
	
	private static ThreadAnnotationHandler handler = new ThreadAnnotationHandler();
	
	public static ThreadAnnotationHandler getHandler() {
		return handler;
	}
		
	public void setUser(String user) {
		ThreadAnnotation.setAnnotation(ThreadAnnotationKey.get(ThreadAnnotationKey.USER_KEY), user);	 
	}
	  
	public void setApplicationName(String appName) {
		ThreadAnnotation.setAnnotation(ThreadAnnotationKey.get(ThreadAnnotationKey.APPLICATION_KEY), appName); 
	}
	
	public void setSessionID(String sessionID) {
		ThreadAnnotation.setAnnotation(ThreadAnnotationKey.get(ThreadAnnotationKey.SESSION_KEY), sessionID);
	}
	 
	public void setRequestID(String requestID) {
		ThreadAnnotation.setAnnotation(ThreadAnnotationKey.get(ThreadAnnotationKey.REQUEST_KEY), requestID);
	}
	
	public Object[] getThreadAnnotations () {
		return ThreadAnnotation.getAnnotations();
	}
	
	public void setThreadAnnotations (Object[] annotations) {
		ThreadAnnotation.setAnnotations((ThreadAnnotation.Annotation[]) annotations);
	}
	
	public void cleanThreadAnnotations() {
		ThreadAnnotation.clearAnnotations();
	}
	
	public HashMap getAnnotationsAsMap(Object[] array) {
		if (array != null) {
			HashMap temp = new HashMap();
			ThreadAnnotation.Annotation a = null;
			
			for (int i = 0; i < array.length; i++) {
				a = (ThreadAnnotation.Annotation) array[i];
				temp.put(((ThreadAnnotationKey) a.getKey()).getKey(), a.getValue());
			}				
			return temp;
		} 
		return null;
	}

}
