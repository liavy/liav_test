package org.spec.jappserver.async;
 
import javax.jms.*; 
import java.io.*;
import java.util.*;

public class ObjectMessageImpl extends MessageImpl implements ObjectMessage{

   Serializable obj; 
 
   public ObjectMessageImpl(){
     super();
   }
   
   public void setObject(Serializable obj) throws JMSException{
     this.obj = obj; 
   }
   
   public Serializable getObject(){
     return obj; 
   }

}