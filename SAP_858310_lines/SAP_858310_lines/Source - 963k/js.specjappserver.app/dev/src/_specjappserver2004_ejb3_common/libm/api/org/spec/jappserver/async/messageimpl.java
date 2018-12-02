package org.spec.jappserver.async;

import javax.jms.*; 
import java.io.*;
import java.util.*;


public class MessageImpl extends HashMap implements Message{
  static volatile int MAXID = 0; 
  
  String msgID; 
  String corrID ; 
  int deliveryMode ;
  Destination destination;
  Destination replyTo; 
  long expiration ; 
  int priority; 
  boolean isRedelivered= false; 
  long timestamp; 
  String jmstype; 
  
  public MessageImpl(){
  	super(3);
  	
        int id = ++MAXID; 
        timestamp = System.currentTimeMillis(); 
        msgID = "MSG_ID[" + id + "-" + timestamp + "]" ;  
  }
  
  void init(){
      deliveryMode = DeliveryMode.NON_PERSISTENT; 
      isRedelivered = false; 
      priority = 5; 
      expiration = 0; 
  }

  public void acknowledge(){
     
  }
  
  public void clearBody(){
  	
  }
  
  public void clearProperties(){
  	clear();
  }
  
  public boolean getBooleanProperty(String pname) throws JMSException{
  	try{
  	  String pv = (String)get(pname);
  	  return Boolean.parseBoolean(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}
  }
  
  public byte getByteProperty(String pname)throws JMSException{
        try{
  	  String pv =(String) get(pname);
  	  return Byte.parseByte(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}
  }

  public double getDoubleProperty(String pname)throws JMSException{
        try{
  	  String pv = (String)get(pname);
  	  return Double.parseDouble(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }
  
  public float getFloatProperty(String pname)throws JMSException{
        try{
  	  String pv = (String)get(pname);
  	  return Float.parseFloat(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }
  
  public int getIntProperty(String pname)throws JMSException{
        try{
  	  String pv = (String)get(pname);
  	  return Integer.parseInt(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }
 
  public long getLongProperty(String pname)throws JMSException{
        try{
  	  String pv = (String)get(pname);
  	  return Long.parseLong(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  	
  }
  
  public Object getObjectProperty(String pname)throws JMSException{
        try{
  	  return get(pname);  	  
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }

  public short getShortProperty(String pname)throws JMSException{
        try{
  	  String pv = (String)get(pname);
  	  return Short.parseShort(pv);
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }
  
  public String getStringProperty(String pname)throws JMSException{
        try{
  	  return (String)get(pname);  	  
  	}catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
  	}  
  }
  
  public void setBooleanProperty(String pname, boolean value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }    
  }
  
  public void setByteProperty(String pname, byte value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }

  public void setDoubleProperty(String pname, double value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }
  
  public void setFloatProperty(String pname, float value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }
  
  public void setIntProperty(String pname, int value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }
 
  public void setLongProperty(String pname, long value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     	
  }
  
  public void setObjectProperty(String pname, Object value)throws JMSException{
    try{
      put(pname, value);
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }

  public void setShortProperty(String pname, short value)throws JMSException{
    try{
      put(pname, String.valueOf(value));
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }
  
  public void setStringProperty(String pname, String value)throws JMSException{
    try{
      put(pname, value);
    }catch(Exception x){
  		JMSException jmsx = new JMSException("");
  		jmsx.setLinkedException(x);
  		throw jmsx ; 
    }     
  }
  
  public Enumeration getPropertyNames()throws JMSException{
     return Collections.enumeration(keySet());
  }
  
  public boolean propertyExists(String pname)throws JMSException{
    return (get(pname)!=null );
  }
  
  public String getJMSCorrelationID()throws JMSException{
    return corrID; 
  }

  public byte[] getJMSCorrelationIDAsBytes()throws JMSException{
    return corrID.getBytes();
  }

  public int getJMSDeliveryMode()throws JMSException{
    return deliveryMode; 
  }

  public Destination getJMSDestination()throws JMSException{
     return destination; 
  }

  public long getJMSExpiration()throws JMSException{
     return expiration ;
  }

  public String getJMSMessageID()throws JMSException{
    return msgID; 
  }

  public int getJMSPriority()throws JMSException{
     return priority; 
  }

  public boolean getJMSRedelivered() throws JMSException{
     return isRedelivered; 
  }

  public Destination getJMSReplyTo()throws JMSException{
     return replyTo; 
  }
  
  public long getJMSTimestamp()throws JMSException{
    return timestamp; 
  }

  public String getJMSType()throws JMSException{
     return jmstype; 
  }


  public void setJMSCorrelationID(String corrID)throws JMSException{
    this.corrID = corrID ; 
  }

  public void setJMSCorrelationIDAsBytes(byte[] corrID_b_)throws JMSException{
    this.corrID = new String(corrID_b_);
  }
  
  public void setJMSDeliveryMode(int deliveryMode)throws JMSException{
    this.deliveryMode = deliveryMode; 
  }

  public void setJMSDestination(Destination dest)throws JMSException{
     this.destination = dest; 
  }
 
  public void setJMSExpiration(long expiration)throws JMSException{
    this.expiration = expiration ; 
  }

  public void setJMSMessageID(String msgid)throws JMSException{
    this.msgID = msgid; 
  }

  public void setJMSPriority(int prio)throws JMSException{
    this.priority = prio; 
  }

  public void setJMSRedelivered(boolean redelivered)throws JMSException{
    this.isRedelivered = redelivered; 
  }

  public void setJMSReplyTo(Destination dest)throws JMSException{
    this.replyTo = dest; 
  }
  
  public void setJMSTimestamp(long timestamp)throws JMSException{
    this.timestamp = timestamp; 
  }

  public void setJMSType(String jmstype)throws JMSException{
    this.jmstype = jmstype; 
  }

}
