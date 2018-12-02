package com.sap.engine.services.webservices.additions.client.metering;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.connector.ComponentExecutionContext;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.lib.xml.util.BASE64Encoder;
import com.sap.engine.services.licensing.LicensingRuntimeInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ConsumerProtocolFactory;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.xi.XIClientServiceMetering;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/* author: i044259
 * 
 */

public class MeteringProtocol implements ConsumerProtocol, ProtocolExtensions, XIClientServiceMetering{

  private final static MeteringProtocol METERING_PROTOCOL;

  private LicensingRuntimeInterface licensingRuntimeInterface;
  private ComponentExecutionContext componentExecutionContext;

  /*public static final String protocolName = "MeteringProtocol";
  private final static String HTTP_SID = "CallingSys";
  private final static String HTTP_INSTNO = "CallingCompany";
  private final static String HTTP_APPNAME = "CallingApp";
  private final static String HTTP_APPTYPE = "CallingType";
  private final static String HTTP_COMPONENT = "CallingComponent";
  private final static String SOAP_SID = "Sys";
  private final static String SOAP_INSTNO = "Company";
  private final static String SOAP_APPNAME = "App";
  private final static String SOAP_APPTYPE = "Type";
  private final static String SOAP_COMPONENT = "Component";
  private final static String APPTYPE_VALUE = "SJ";
  private final static String SOAPHEADER_NS= "http://www.sap.com/webas/712/soap/features/runtime/metering/";
  private final static String METERING_NS = "http://www.sap.com/webas/710/service/metering/";
  private final static String METERING_LEVEL_PROP = "Level";
  private final static String METERING_PROTOCOL_PROP = "Protocol";
  private final static String METERING_PROTOCOL_PROP_HTTP = "HTTPHeader";
  private final static String METERING_PROTOCOL_PROP_SOAP = "SOAPHeader";*/

  //private final static String METERING_DISABLED_SYSPROPERTY = "servicemetering.disable";
  private boolean meteringDisabled = false;

  private final static String[] HTTP_HEADERS;
  private final static String[] SOAP_HEADERS;

  private static final Location LOC = Location.getLocation(MeteringProtocol.class);
  private boolean protocolRegistered = false;
  private HashMap <String, String> cache = new HashMap<String, String>();

  public static enum DataTransferLevel {
    Limited("0"), Basic("1"), Full("2");

    DataTransferLevel(String dtl){
      transferLevel = dtl;
    }

    public static DataTransferLevel getDataTransferLevel(String level){
      if (Basic.transferLevel.equals(level)){
        return Basic;
      }else if (Full.transferLevel.equals(level)){
        return Full;
      }else{
        return Limited;
      }
    }

    private String transferLevel;
  };

  static{
    METERING_PROTOCOL = new MeteringProtocol();
    HTTP_HEADERS = new String[] {ServiceMeteringConstants.HTTP_HEADER_SYS, ServiceMeteringConstants.HTTP_HEADER_COMPANY, 
        ServiceMeteringConstants.HTTP_HEADER_APPNAME, ServiceMeteringConstants.HTTP_HEADER_APPTYPE, 
        ServiceMeteringConstants.HTTP_HEADER_COMPONENT, ServiceMeteringConstants.HTTP_HEADER_USER_CODE};
    SOAP_HEADERS = new String[] {ServiceMeteringConstants.SOAP_HEADER_SYS, ServiceMeteringConstants.SOAP_HEADER_COMPANY, 
        ServiceMeteringConstants.SOAP_HEADER_APPNAME, ServiceMeteringConstants.SOAP_HEADER_APPTYPE, 
        ServiceMeteringConstants.SOAP_HEADER_COMPONENT, ServiceMeteringConstants.SOAP_HEADER_USER_CODE};
  }

  private MeteringProtocol(){}

  public static MeteringProtocol  getInstance(){
    return METERING_PROTOCOL;
  }

  public String getProtocolName(){
    return ServiceMeteringConstants.PROTOCOL_NAME;
  }

  public boolean isMeteringDisabled(){
    return meteringDisabled;
  }

  private String getStringBDProperty(QName property, ClientConfigurationContext ctx){
    StaticConfigurationContext sctx = ctx.getStaticContext();
    BindingData bd = (BindingData)sctx.getRTConfig();
    PropertyListType plt = bd.getSinglePropertyList();
    PropertyType pt = plt.getProperty(property);
    return pt == null ? null : pt.get_value();
  }


  private boolean useHTTPHeaders(ConfigurationContext ctx){
    String property = getStringBDProperty(new QName(ServiceMeteringConstants.METERING_NS, ServiceMeteringConstants.METERING_PROTOCOL_PROP), 
        (ClientConfigurationContext)ctx);
    if (property == null){
      return true;
    }
    return ServiceMeteringConstants.METERING_PROTOCOL_PROP_HTTP.equals(property);
  }

  private DataTransferLevel getDataTransferLevel(ConfigurationContext ctx){
    String property = getStringBDProperty(new QName(ServiceMeteringConstants.METERING_NS, ServiceMeteringConstants.METERING_LEVEL_PROP), 
        (ClientConfigurationContext)ctx);
    return DataTransferLevel.getDataTransferLevel(property);
  }

  /*private String getUserCode(){
    try{
    IUser user = UMFactory.getAuthenticator().getLoggedInUser();
    String userName = user.getName();
    userName = userName.toUpperCase();
    MessageDigest digestAlgorithm = MessageDigest.getInstance(ServiceMeteringConstants.METERING_USER_CODE_HASH_ALGORITHM);
    digestAlgorithm.reset();
    digestAlgorithm.update(userName.getBytes(ServiceMeteringConstants.METERING_USER_CODE_ENCODING));
    byte[] digest = digestAlgorithm.digest();
    byte [] digest64 = BASE64Encoder.encode(digest);
    return new String(digest64);
    }catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING, "Error computing user hash code.", e);
      return "";
    }
  }*/
  
  private void createHTTPHeaders(String sid, String instNo, String appName, String appType, String component, Hashtable headersTable, ConfigurationContext ctx){
    switch(getDataTransferLevel(ctx)){
    case Full:
      //headersTable.put(ServiceMeteringConstants.HTTP_HEADER_USER_CODE, new String[]{getUserCode()});
      headersTable.put(ServiceMeteringConstants.HTTP_HEADER_COMPANY, new String[]{instNo == null ? getCompany() : instNo});
      headersTable.put(ServiceMeteringConstants.HTTP_HEADER_SYS, new String[]{sid == null ? getSystem() : sid});
    case Basic:
      headersTable.put(ServiceMeteringConstants.HTTP_HEADER_APPNAME, new String[]{appName == null ? getAppName() : appName});
      headersTable.put(ServiceMeteringConstants.HTTP_HEADER_COMPONENT, new String[]{component == null ? getComponent() : component});

    case Limited:
      headersTable.put(ServiceMeteringConstants.HTTP_HEADER_APPTYPE, new String[]{appType == null ? getAppType() : appType});
      break;
    }
  }

  private String getAppType(){
    return ServiceMeteringConstants.HEADER_APPTYPE_VALUE;
  }

  private String getComponent(){
    return " ";
  }

  private String getAppName(){
    String appName = componentExecutionContext ==  null ? "" : componentExecutionContext.getApplicationName();
    if (appName == null){
      appName = " ";
    }
    return appName;
  }

  private String getCompany(){
    String company = cache.get(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
    if (company != null){
      return company;
    }else{
      company = licensingRuntimeInterface == null ? "" : licensingRuntimeInterface.getInstNo();
      if (company == null){
        company = " ";
      }
      cache.put(ServiceMeteringConstants.HTTP_HEADER_COMPANY, company);
    }
    return company;
  }

  private String getSystem(){
    String sys = cache.get(ServiceMeteringConstants.HTTP_HEADER_SYS);
    if (sys != null){
      return sys;
    }else{
      sys = licensingRuntimeInterface == null ? "" : licensingRuntimeInterface.getSystemId();
      if (sys == null){
        sys = " ";
      }
      cache.put(ServiceMeteringConstants.HTTP_HEADER_SYS, sys);
    }
    return sys;
  }

  private void createSOAPHeaders(String system, String company, String appName, String appType, String component, SOAPMessage msg, ConfigurationContext ctx){
    SOAPHeaderList soapHeaders = msg.getSOAPHeaders();
    if (soapHeaders == null){
      throw new IllegalStateException("No SOAP header list found in messasge");
    }else{
      Element meteringHeader = soapHeaders.createHeader(new QName(ServiceMeteringConstants.SOAPHEADER_NS, 
          ServiceMeteringConstants.SOAPHEADER_NAME, "m"));
      Document doc = meteringHeader.getOwnerDocument();
      DataTransferLevel dtl = getDataTransferLevel(ctx);

      // application type
      Element appTypeEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPTYPE);
      Text appTypeElTxt = doc.createTextNode(appType == null ? getAppType() : appType);
      appTypeEl.appendChild(appTypeElTxt);
      meteringHeader.appendChild(appTypeEl);
      soapHeaders.addHeader(meteringHeader);

      if (dtl == DataTransferLevel.Basic || dtl == DataTransferLevel.Full){
        // application name
        Element appNameEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPNAME);
        Text appNameElTxt = doc.createTextNode(appName == null ? getAppName() : appName);
        appNameEl.appendChild(appNameElTxt);
        meteringHeader.appendChild(appNameEl);

        // component
        Element componentEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPONENT);
        Text componentElTxt = doc.createTextNode(component == null ? getComponent() : component);
        componentEl.appendChild(componentElTxt);
        meteringHeader.appendChild(componentEl);

        if (dtl == DataTransferLevel.Full){
          // company
          Element instNoEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPANY);
          Text instNoTxt = doc.createTextNode(company == null ? getCompany() : company);
          instNoEl.appendChild(instNoTxt);
          meteringHeader.appendChild(instNoEl);

          // sid
          Element sidEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_SYS);
          Text sidElTxt = doc.createTextNode(system == null ? getSystem() : system);
          sidEl.appendChild(sidElTxt);
          meteringHeader.appendChild(sidEl);
          
          // user code
          /*Element userCodeEl = doc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_USER_CODE);
          Text userCodeElTxt = doc.createTextNode(getUserCode());
          userCodeEl.appendChild(userCodeElTxt);
          meteringHeader.appendChild(userCodeEl);*/
        }
      }
    }
  }

  public void beforeSerialization(ConfigurationContext configurationContext){
    if (isMeteringDisabled()){
      LOC.pathT("MeteringProtocol.beforeSerialization exits - metering disabled.");
      return;
    }
    ClientConfigurationContext clientContext = (ClientConfigurationContext) configurationContext;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();

    /*String sid = licensingRuntimeInterface == null ? "" : licensingRuntimeInterface.getSystemId();
    String instNo = licensingRuntimeInterface == null ? "" : licensingRuntimeInterface.getInstNo();
    String appName = componentExecutionContext ==  null ? "" : componentExecutionContext.getApplicationName();
    String component = ""; 
//    if (instNo.indexOf("not yet assigned") > -1){
//      instNo = "";
//    }

    if (sid == null){
      sid = "";
    }
    if (instNo == null){
      instNo = "";
    }
    if (appName == null){
      appName = "";
    }*/
    if (useHTTPHeaders(configurationContext)){
      Hashtable table = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS); //This property contains table with the request headers
      if (table == null){ // create headers table if not already created
        table = new Hashtable();
        dynamicContext.setProperty(PublicProperties.P_HTTP_REQUEST_HEADERS, table); 
      }
      createHTTPHeaders(null, null,  null, null, null, table, configurationContext);

    }else{ // use SOAP headers
      Message message = clientContext.getMessage();        
      if (message != null && message instanceof SOAPMessage) {
        SOAPMessage soapMessage = (SOAPMessage) message;
        createSOAPHeaders(null, null, null, null, null, soapMessage, configurationContext);
      }else{
        LOC.pathT("MeteringProtocol.beforeSerialization: no soap message found in context or message not a SOAPMessage");
      }
    }
    LOC.pathT("MeteringProtocol.beforeSerialization exit.");
  }

  public int afterDeserialization(ConfigurationContext configurationContext){
    return Protocol.CONTINUE;
  }

  public int handleRequest(ConfigurationContext configurationContext) throws ProtocolException, MessageException {
    return Protocol.CONTINUE;
  }

  // copy headers to persistable context
  public void beforeHibernation(ConfigurationContext ctx) throws ProtocolException{
    if (isMeteringDisabled()){
      LOC.pathT("MeteringProtocol.beforeHibernation exits - metering disabled.");
      return;
    }
    // in case there's no security protocol, copy headers to persistent
    ClientConfigurationContext clientContext = (ClientConfigurationContext) ctx;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    ConfigurationContext persistableContext = clientContext.getPersistableContext();

    if (useHTTPHeaders(ctx)){    // extract metering HTTP headers from dynamic context
      Hashtable headersTable = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS); //This property contains table with the request headers
      if (headersTable == null){
        LOC.pathT("MeteringProtocol.beforeHibernation: cannot persist metering HTTP headers");
        return;
      }
      for (String header : HTTP_HEADERS){
        String[] headerValues = (String[])headersTable.get(header); // assumed size always will be 1
        if (headerValues == null || headerValues.length < 1){
          LOC.pathT("MeteringProtocol.beforeHibernation: skipping empty or missing HTTP header [" + header + "]");
          continue;
        }
        persistableContext.setProperty(header, headerValues[0]);
        headersTable.remove(header);               
      }      
      if (headersTable.isEmpty()) {
        dynamicContext.removeProperty(PublicProperties.P_HTTP_REQUEST_HEADERS);
      }      
    }else{     // extract metering SOAP headers values from soap message
      Message message = clientContext.getMessage();        
      if (message != null && message instanceof SOAPMessage) {
        SOAPMessage soapMessage = (SOAPMessage) message;
        SOAPHeaderList soapHeaders = soapMessage.getSOAPHeaders();
        if (soapHeaders == null){
          throw new IllegalStateException("No SOAP header list found in messasge");
        }else{ 
          Element meteringHeader = soapHeaders.getHeader(new QName(ServiceMeteringConstants.SOAPHEADER_NS, "CallerInformation"));
          if (meteringHeader == null){
            LOC.pathT("MeteringProtocol.beforeHibernation: no soap metering header found in soap message");
          }else{
            for (String header : SOAP_HEADERS){
              String value = extractSOAPHeaderValue(meteringHeader, header, ServiceMeteringConstants.SOAPHEADER_NS);
              if (value != null){
                persistableContext.setProperty(header, value); 
              }
            }
          }
        }
      }else{
        LOC.pathT("MeteringProtocol.beforeHibernation: no soap message found in context or message not a SOAPMessage");
      }
    }
  }

  private String extractSOAPHeaderValue(Element meteringHeader, String childElement, String namespace){
    NodeList nl = meteringHeader.getElementsByTagNameNS(namespace, childElement);
    if (nl != null && nl.getLength() > 0){
      return ((Element)nl.item(0)).getFirstChild().getNodeValue();
    }else{
      LOC.pathT("MeteringProtocol.extractSOAPHeaderValue: skipping empty or missing SOAP header [" + childElement + "]");
    }
    return null;
  }

  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException{}
  public void finishHibernation(ConfigurationContext ctx) throws ProtocolException{}

  // restore headers from persistable context
  public void afterHibernation(ConfigurationContext ctx) throws ProtocolException{
    if (isMeteringDisabled()){
      LOC.pathT("MeteringProtocol.afterHibernation exits - metering disabled.");
      return;
    }
    ClientConfigurationContext clientContext = (ClientConfigurationContext) ctx;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    ConfigurationContext persistableContext = clientContext.getPersistableContext();

    if (useHTTPHeaders(ctx)){
      String[] headerValues = new String[HTTP_HEADERS.length];
      int i = 0;
      for (String header : HTTP_HEADERS){
        headerValues[i] = (String)persistableContext.getProperty(header);
        persistableContext.removeProperty(header);
        if (headerValues[i] == null){
          LOC.pathT("MeteringProtocol.afterHibernation: cannot restore HTTP header [" + header + "] from configuration context, using empty header");
          headerValues[i] = " ";
        }
        i++;
      }

      Hashtable headersTable = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS); //This property contains table with the request headers
      if (headersTable == null){
        LOC.pathT("MeteringProtocol.afterHibernation: no HTTP headers table found in dynamic context, creating one");
        headersTable = new Hashtable();
        dynamicContext.setProperty(PublicProperties.P_HTTP_REQUEST_HEADERS, headersTable);
      }
      createHTTPHeaders(headerValues[0], headerValues[1], headerValues[2], headerValues[3], headerValues[4], headersTable, ctx);
    }else{ // SOAP headers
      String[] headerValues = new String[SOAP_HEADERS.length];
      int i = 0;
      for (String header : SOAP_HEADERS){
        headerValues[i] = (String)persistableContext.getProperty(header);
        persistableContext.removeProperty(header);
        if (headerValues[i] == null){
          LOC.pathT("MeteringProtocol.afterHibernation: cannot restore SOAP header [" + header + "] from configuration context, using empty header");
          headerValues[i] = "";
        }
        i++;
      }
      Message message = clientContext.getMessage();        
      if (message != null && message instanceof SOAPMessage) {
        SOAPMessage soapMessage = (SOAPMessage) message;
        createSOAPHeaders(headerValues[0], headerValues[1], headerValues[2], headerValues[3], headerValues[4], soapMessage, ctx);
      }else{
        LOC.pathT("MeteringProtocol.afterHibernation: no soap message found in context or message not a SOAPMessage");
      }
    }
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException{
    return Protocol.CONTINUE;
  }

  public int handleFault(ConfigurationContext context) throws ProtocolException{
    return Protocol.CONTINUE;
  }

  public void setLicensingInterface(LicensingRuntimeInterface lri){
    licensingRuntimeInterface = lri;
    LOC.pathT("MeteringProtocol.setLicensingInterface: got licensing interface, protocol is registered [" + protocolRegistered + "]");
    if (! protocolRegistered){
      registerProtocol();
    }
  }

  public void removeLicensingInterface(){
    licensingRuntimeInterface = null;
  }

  public void setAppContextInterface(ComponentExecutionContext cec){
    componentExecutionContext = cec;
    LOC.pathT("MeteringProtocol.setAppContextInterface: got component execution context, protocol is registered [" + protocolRegistered + "]");
    if (! protocolRegistered){
      registerProtocol();
    }
  }

  public void removeAppContextInterface(){
    componentExecutionContext = null;
  }

  private void registerProtocol(){
    ConsumerProtocolFactory.protocolFactory.registerProtocol(getProtocolName(), this);
    protocolRegistered = true;
    LOC.pathT("MeteringProtocol.registerProtocol: Metering protocol registered");


    // !! NOTE: commented for customers, uncomment for performance testing
    /*
    String prop = System.getProperty(ServiceMeteringConstants.METERING_DISABLED_SYSPROPERTY);
    if (prop != null){
       meteringDisabled = prop.equals("consumer") || prop.equals("both");
    }else{
      meteringDisabled = false;
    }
    if (meteringDisabled){
      LOC.pathT("MeteringProtocol.registerProtocol: Metering is disabled");
    }
    */
  }
  
  public void unregisterProtocol(){
    ConsumerProtocolFactory.protocolFactory.unregisterProtocol(getProtocolName());
    LOC.pathT("Metering protocol unregistered");
  }
  
  
  public void addMeteringDataToXIMessage(ESPXIMessage xiMsg){
    xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_APPNAME, getAppName());
    xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_SYS, getSystem());
    xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_COMPANY, getCompany());
    xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_COMPONENT, getComponent());
    xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_APPTYPE, getAppType());
    //xiMsg.addHeader(ServiceMeteringConstants.QNAME_HEADER_USER_CODE, getUserCode());
  }
  
  public static void main(String... s) throws Exception{
    String userName = "userName";
    MessageDigest digestAlgorithm = MessageDigest.getInstance("SHA-1");
    digestAlgorithm.reset();
    digestAlgorithm.update(userName.getBytes("utf-8"));
    byte[] digest = digestAlgorithm.digest();
    System.out.println("digest [" + Arrays.toString(digest) + "] [" + new String(digest) + "]"); //$JL-I18N$
    byte [] digest64 = BASE64Encoder.encode(digest);
    System.out.println("digest64 [" + Arrays.toString(digest64) + "] [" + new String(digest64) + "]"); //$JL-I18N$
  }
}

