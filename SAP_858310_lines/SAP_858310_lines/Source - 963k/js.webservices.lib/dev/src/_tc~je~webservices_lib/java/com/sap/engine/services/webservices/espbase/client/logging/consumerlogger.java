package com.sap.engine.services.webservices.espbase.client.logging;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import com.sap.engine.interfaces.webservices.runtime.ProtocolExceptionExt;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class ConsumerLogger {
  
  /** Proxies types */
  protected final static String JAX_WS_TYPE = "jax_ws";
  
  protected final static String JAX_RPC_TYPE = "jax_rpc";
  
  protected final static String DYNAMIC_MASS_CONFIG_TYPE = "dynamic_mass_config";
  
  protected final static String DYNAMIC_WSDL_TYPE = "dynamic_wsdl";
  
  protected final static String DYNAMIC_DESTINATION_TYPE = "dynamic_destination" ;

  /** Property keys */
  protected final static String PROXY_TYPE = "proxy_type";
  
  protected final static String APPLICATION_NAME = "applicationName";
  
  protected final static String PORT_QNAME = "port_qname";
  
  protected final static String INTERFACE_QNAME = "interface_qname";
  
  protected final static String SERVICE_REF_ID = "service_ref_id";
  
  protected final static String LMT_NAME = "lmt_name";
  
  protected final static String IF_NAME = "if_name";
  
  protected final static String WSDL_URL = "wsdl_url";
      
  protected final static Category category = Category.getCategory(Category.SYS_SERVER, "WS/Consumer");
        
  protected static void logRedirectResponse(Map contextInfo, String endpoint, Location location){    
    String proxyInformation = getProxyInformation(contextInfo);
        
    SimpleLogger.log(Severity.ERROR, category,location, "SOA.wsr.030000", "{0}The service with endpoint {1} makes HTTP Post redirect more than once.", new Object[]{proxyInformation, endpoint});       
  }

  
  protected static void log404Response(Map contextInfo, String endpoint, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
                   
    SimpleLogger.log(Severity.ERROR, category,location, "SOA.wsr.030001", "{0}The called service does not exist in the called endpoint: {1}", new Object[]{proxyInformation, endpoint});
  }
  
  
  protected static void log500Reponse(Map contextInfo, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
           
    SimpleLogger.log(Severity.ERROR, category,location, "SOA.wsr.030002", "{0}The service failed without a returning an infomative SoapFault.", new Object[]{proxyInformation});  
  }
  
  protected static void log502Response(Map contextInfo, String endpoint, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
            
    SimpleLogger.log(Severity.INFO, category,location, "SOA.wsr.030003", "{0}The proxy can no resolve the called endpoint: {1}", new Object[]{proxyInformation, endpoint}); 
  }
  
  protected static void log503Response(Map contextInfo, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
          
    SimpleLogger.log(Severity.ERROR, category,location, "SOA.wsr.030004", "{0}The called service is currently unavailable.", new Object[]{proxyInformation});
  }
  
  protected static void logUnableToConnect(IOException e, Map contextInfo, String endpoint,Location location){
    String proxyInformation = getProxyInformation(contextInfo);
             
    LoggingUtilities.logAndTrace(Severity.ERROR, category, location, e, "SOA.wsr.030005", location.getDCName(),
        location.getCSNComponent(), "{0}Unable to connect to host: {1}", new Object[]{proxyInformation, endpoint});
  }
  
  
  protected static void logNoHTTPSPeer(IOException e, Map contextInfo, String endpoint, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
    
    String normalizedEndpoint = endpoint.replace("https", "http");
                    
    LoggingUtilities.logAndTrace(Severity.ERROR, category, location, e, "SOA.wsr.030006", location.getDCName(),
        location.getCSNComponent(), "{0}Tried to invoke a http ednpoint {1} through https protocol.", new Object[]{proxyInformation, normalizedEndpoint});                      
  }
  
  
  protected static void logSocketTimeout(SocketTimeoutException e, Map contextInfo, String endpoint, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
           
    LoggingUtilities.logAndTrace(Severity.ERROR, category, location, e, "SOA.wsr.030007", location.getDCName(),
        location.getCSNComponent(), "{0}The server response timed out.", new Object[]{proxyInformation});    

  }
  
  
  protected static void logSecurityFailure(ProtocolExceptionExt exception, Map contextInfo, Location location){
    String proxyInformation = getProxyInformation(contextInfo);
               
    String securityLogId = exception.getLogId();
                    
    LoggingUtilities.logAndTrace(Severity.ERROR, category, location, exception, "SOA.wsr.030008", location.getDCName(),
        location.getCSNComponent(), "{0}The webservices security setup failed. For details see log entry: {1}", new Object[] {
            proxyInformation, securityLogId });    
  }
  
  
  
  private static String getProxyInformation(Map contextInfo){
    String proxyType = (String) contextInfo.get(PROXY_TYPE);
    
    String proxyInformation = null;
    if (JAX_WS_TYPE.equals(proxyType)){
      StringBuilder builder = new StringBuilder("Jaxws client.");
      builder.append(" Application: ");
      builder.append(contextInfo.get(APPLICATION_NAME));
      builder.append(", InterfaceName: ");
      builder.append(contextInfo.get(INTERFACE_QNAME));
      builder.append(", PortName: ");
      builder.append(contextInfo.get(PORT_QNAME));
      builder.append(". ");
      proxyInformation = builder.toString();      
    } else if (JAX_RPC_TYPE.equals(proxyType)){
      StringBuilder builder = new StringBuilder("Jaxrpc client.");
      builder.append(" Application: ");
      builder.append(contextInfo.get(APPLICATION_NAME));
      builder.append(", InterfaceName: ");
      builder.append(contextInfo.get(INTERFACE_QNAME));
      builder.append(", PortName: ");
      builder.append(contextInfo.get(PORT_QNAME));
      builder.append(". ");
      proxyInformation = builder.toString();      
    } else if (DYNAMIC_MASS_CONFIG_TYPE.equals(proxyType)) {            
      // This client is created by mass configuration       
      StringBuilder builder = new StringBuilder("Dynamic mass configuration client.");
      builder.append(" Application: ");
      builder.append(contextInfo.get(APPLICATION_NAME));
      builder.append(", ServiceRefId: ");    
      builder.append(contextInfo.get(SERVICE_REF_ID));
      builder.append(". ");
      proxyInformation = builder.toString();      
    } else if (DYNAMIC_DESTINATION_TYPE.equals(proxyType)) {
      // This client is created by destination
      StringBuilder builder = new StringBuilder("Dynamic destination client.");
      builder.append(" Destination: "); //Logical Metadata Name
      builder.append(contextInfo.get(LMT_NAME));
      builder.append(". ");
      proxyInformation = builder.toString();      
    } else if (DYNAMIC_WSDL_TYPE.equals(proxyType)){
      // This client is created by WSDL URL.
      StringBuilder builder = new StringBuilder("Dynamic client generated from wsdl.");
      builder.append(" WSDL: "); 
      builder.append(contextInfo.get(WSDL_URL));
      builder.append(". ");
      proxyInformation = builder.toString();      
    } else{
      throw new RuntimeException("This case should not occure.");
    }
    
    return proxyInformation;
  }  
}
