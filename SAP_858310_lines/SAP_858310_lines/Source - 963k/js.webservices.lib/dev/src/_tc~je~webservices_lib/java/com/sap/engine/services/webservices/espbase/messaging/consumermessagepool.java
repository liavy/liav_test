package com.sap.engine.services.webservices.espbase.messaging;

import java.lang.ref.WeakReference;
import java.util.Stack;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;

/**
 * Message Pool for the consumption.
 * 
 * @author i056242
 *
 */
public class ConsumerMessagePool {
     
  private static Stack<WeakReference<MIMEMessage>> mimeMessages = new Stack<WeakReference<MIMEMessage>>();
  
  public static MIMEMessage getMIMEMessage(){
    MIMEMessage result = null; 
    synchronized (mimeMessages) {
      while (!mimeMessages.empty()) {
        WeakReference<MIMEMessage> topElement = mimeMessages.pop();
        result = topElement.get();               
      }
    }
           
    if (result == null) {
      result = new MIMEMessageImpl();      
    }           
    
    return result;
  }
  
  public static void returnMimeMessage(MIMEMessage message) {
    message.clear();    
    synchronized (mimeMessages) {
      mimeMessages.push(new WeakReference<MIMEMessage>(message));
    }
  }       
}
