package org.spec.jappserver.async;

import javax.jms.* ; 
import java.util.*;
import java.util.concurrent.*;

public abstract class AsyncExecutor{
  
  static Class impl_class; 
  
  protected MessageListener m_listener; 
  

  public static void setImplementationClass(Class __impl_class) {
  	if( !AsyncExecutor.class.isAssignableFrom(__impl_class) ){
  		throw new RuntimeException("Not a AsyncExecutor subclass : " + __impl_class);
  	}
  	impl_class = __impl_class;   	
  }

  public static AsyncExecutor createAsyncExecutor(MessageListener m_listener) {
  	try{
  	  AsyncExecutor exec = (AsyncExecutor)impl_class.newInstance(); 
  	  exec.m_listener = m_listener; 
  	  return exec; 
  	}catch(RuntimeException rx){
  		throw rx; 
  	}catch(Exception x){
  	   throw new RuntimeException(x);
  	}
  }

  public abstract void  deliverMessage(Message msg); 

}
