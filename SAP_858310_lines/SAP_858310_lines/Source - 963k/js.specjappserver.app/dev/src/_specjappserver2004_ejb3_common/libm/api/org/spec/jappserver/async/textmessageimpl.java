package org.spec.jappserver.async;
 
import javax.jms.*; 
import java.io.*;
import java.util.*;

public class TextMessageImpl extends MessageImpl implements TextMessage{

   String text; 
 
   public TextMessageImpl(){
     super();
   }
   
   public void setText(String text) throws JMSException{
      this.text = text; 
   }
   
   public String getText(){
     return text; 
   }

}