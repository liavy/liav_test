package com.sap.engine.services.webservices.espbase.messaging.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;

public class LoggingTokenUtil {
  
  public static final String XS_PREFIX  =  "xs";
  public static final String XSI_PREFIX  =  "xsi";
  public static final String UTF8 = "UTF-8";      
   
  
  /**
   * Outputs message envelope tag.
   * @param output
   * @throws IOException
   */
   public static void outputEnvelopeOpen(MIMEMessage message, OutputStream output) throws IOException {        
    String soapVersionNS = message.getSOAPVersionNS();
    
    StringBuilder stringBuilder = new StringBuilder(128);
    stringBuilder.append("<");    
    stringBuilder.append(message.getEnvelopeNamespacePrefix(soapVersionNS));
    stringBuilder.append(":");
    stringBuilder.append(SOAPMessage.ENVELOPETAG_NAME);        
    
    
    //append the additional namespaces.
    Enumeration en = message.getEnvelopeNamespaces();
    while (en.hasMoreElements()) {
      String namespace = (String) en.nextElement();
      
      String prefix = (String) message.getEnvelopeNamespacePrefix(namespace);
      if (prefix.length()==0) {
        stringBuilder.append(" xmlns=\"").append(namespace).append("\"");
      } else {
        stringBuilder.append(" xmlns:").append(prefix).append("=\"").append(namespace).append("\"");
      }      
    }
    
    stringBuilder.append(">");
    
    String element = stringBuilder.toString();
    
    // use Charset! - no need to create a string object for this
    output.write(element.getBytes(UTF8));    
  }

  /**
   * Outputs envelope close tag.
   * @param output
   * @throws IOException
   */
  public static void outputEnvelopeClose(MIMEMessage message, OutputStream output) throws IOException {        
      String soapVersionNS = message.getSOAPVersionNS();      
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("</");    
      stringBuilder.append(message.getEnvelopeNamespacePrefix(soapVersionNS));
      stringBuilder.append(":");
      stringBuilder.append(SOAPMessage.ENVELOPETAG_NAME);
      stringBuilder.append(">");
      
      String element = stringBuilder.toString();
      
      output.write(element.getBytes(UTF8));                   
  }

  
  /**
   * Outputs Header open tag.
   * @param output
   * @throws IOException
   */
  public static void outputHeaderOpenTag(MIMEMessage message, OutputStream output) throws IOException {        
    String soapVersionNS = message.getSOAPVersionNS();      
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<");    
    stringBuilder.append(message.getEnvelopeNamespacePrefix(soapVersionNS));
    stringBuilder.append(":");
    stringBuilder.append(SOAPMessage.HEADERTAG_NAME);
    stringBuilder.append(">");
    
    String element = stringBuilder.toString();
    
    output.write(element.getBytes(UTF8));           
  }
  
  /**
   * Outputs Header close tag.
   * @param output
   * @throws IOException
   */
  public static void outputHeaderCloseTag(MIMEMessage message, OutputStream output) throws IOException {        
    String soapVersionNS = message.getSOAPVersionNS();      
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("</");    
    stringBuilder.append(message.getEnvelopeNamespacePrefix(soapVersionNS));
    stringBuilder.append(":");
    stringBuilder.append(SOAPMessage.HEADERTAG_NAME);
    stringBuilder.append(">");
    
    String element = stringBuilder.toString();
    
    output.write(element.getBytes(UTF8));         ;
  }
  
      
  /**
   * Modify the business data for logging purposes.
   */
  public static Object modifyValue(Object value, boolean modify){
    Object returnValue = null;
    
    if (!modify){
      return value;
    }
    
    if (value instanceof String){
      String originalValue = (String)value;
      
      char[] newCharArray = (char[]) modifyValue(originalValue.toCharArray(), modify);
      
      returnValue = new String(newCharArray);      
    }else if (value instanceof char[]){
      char[] charValue = (char[])value;
      
      for(int i = 0; i< charValue.length; i++){
        if ((charValue[i] == '\n') 
            || (charValue[i] == '\r')
            || (charValue[i] == '\t')
            || (charValue[i] == ' ')){
          continue;
        }
                
          charValue[i] = 'x'; 
       }
                                   
      returnValue = charValue;
    }else{
      throw new IllegalArgumentException("The modified value can only be from type String or char[].");
    }
    
    
    return returnValue;
  }
  
  
  
  public static Hashtable<String, String> getPredefinedNamespaces(MIMEMessage message) {
    if (message == null){
      throw new IllegalArgumentException("MIMEMessage can not be null.");
    }
    Hashtable<String, String> builtInEnvNamespaces = new Hashtable<String, String>();
       
    Enumeration en = message.getEnvelopeNamespaces();
    while (en.hasMoreElements()) {
      String namespace = (String) en.nextElement();
      
      String prefix = (String) message.getEnvelopeNamespacePrefix(namespace);
      
      builtInEnvNamespaces.put(namespace, prefix);
    }
                
    return builtInEnvNamespaces;
  }
  
  
  
  /**
   * Outputs message xml declaration.
   * @param output
   */
/*  private void outputXMLDeclaration(MIMEMessage message, OutputStream output) throws IOException {
    
    
    output.write(XMLDECLARATION_START);
    output.write(encoding.getBytes());
    output.write(XMLDECLARATION_END);
  }
*/

}
