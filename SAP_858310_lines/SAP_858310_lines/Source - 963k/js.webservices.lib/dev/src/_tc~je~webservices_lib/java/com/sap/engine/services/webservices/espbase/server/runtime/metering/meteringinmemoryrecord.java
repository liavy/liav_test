package com.sap.engine.services.webservices.espbase.server.runtime.metering;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.StaticConfigurationContextImpl;

public class MeteringInMemoryRecord {
  private String wsdlOperationName;
  private String portTypeName;
  private String portTypeNamespace;
  private String applicationName;
  private String internalServiceName;

  private String callingAppName;
  private String callingComponent;
  private String callingType;
  private String callingCompany;
  private String callingSys;
  private String callingUserCode;

  private final static int wsdlOperationNameLength = 128;
  private final static int portTypeNamespaceLength = 256;
  private final static int portTypeNameLength = 64;
  private final static int applicationNameLength = 128;
  private final static int internalServiceNameLength = 256;

  private final static int callingAppNameLength = 128;
  private final static int callingComponentLength = 40;
  private final static int callingTypeLength = 10;
  private final static int callingCompanyLength = 40;
  private final static int callingSysLength = 20;
  private final static int callingUserCodeLength = 40;

  private final static String trimSuffix = "[#trim]";

  public String getWSDLOperationName(){
    return wsdlOperationName;
  }

  public String getPortTypeName(){
    return portTypeName;
  }

  public String getPortTypeNamespace(){
    return portTypeNamespace;
  }

  public String getApplicationName(){
    return applicationName;
  }

  public String getInternalServiceName(){
    return internalServiceName;
  }

  public String getCallingAppName(){
    return callingAppName;
  }

  public String getCallingComponent(){
    return callingComponent;
  }

  public String getCallingType(){
    return callingType;
  }

  public String getCallingCompany(){
    return callingCompany;
  }

  public String getCallingSys(){
    return callingSys;
  }
  
  public String getCallingUserCode(){
    return callingUserCode;
  }

  protected MeteringInMemoryRecord() {

  }

  public MeteringInMemoryRecord(ProviderContextHelper context) throws RuntimeProcessException{
    int suffixLen = trimSuffix.length();
    wsdlOperationName = context.getOperation().getWSDLOperationName();
    if (wsdlOperationName == null || wsdlOperationName.length() < 1){
      wsdlOperationName = " ";
    } else if (wsdlOperationName.length() > wsdlOperationNameLength){
      wsdlOperationName = wsdlOperationName.substring(0, wsdlOperationNameLength - suffixLen) + trimSuffix;
    }

    StaticConfigurationContextImpl staticContext = (StaticConfigurationContextImpl) context.getStaticContext();
    BindingData bd = staticContext.getRTConfiguration();
    Variant variant = staticContext.getDTConfiguration();
    InterfaceData iData = variant.getInterfaceData();
    portTypeName = iData.getName();
    //InterfaceMapping intfM = context.getStaticContext().getInterfaceMapping();
    //QName portType = intfM.getPortType();
    //QName portType = null;
    //portTypeName = portType.getLocalPart();
    if (portTypeName == null || portTypeName.length() < 1){
      portTypeName = " ";
    } else if (portTypeName.length() > portTypeNameLength){
      portTypeName = portTypeName.substring(0, portTypeNameLength - suffixLen) + trimSuffix;
    }

    portTypeNamespace = iData.getNamespace();
    if (portTypeNamespace == null || portTypeNamespace.length() < 1){
      portTypeNamespace = " ";
    }else if (portTypeNamespace.length() > portTypeNamespaceLength){
      portTypeNamespace = portTypeNamespace.substring(0, portTypeNamespaceLength - suffixLen) + trimSuffix;
    }

    applicationName = (String)staticContext.getTargetApplicationName();
    if (applicationName == null || applicationName.length() < 1){
      applicationName = " ";
    }else if (applicationName.length() > applicationNameLength){
      applicationName = applicationName.substring(0, applicationNameLength - suffixLen) + trimSuffix;
    }

    internalServiceName = bd.getInterfaceId();
    if (internalServiceName == null || internalServiceName.length() < 1){
      internalServiceName = " ";
    }else if (internalServiceName.length() > internalServiceNameLength){
      internalServiceName = internalServiceName.substring(0, internalServiceNameLength - suffixLen) + trimSuffix;
    }

    if (! extractHTTPMeteringHeaders(context)){
      extractSOAPMeteringHeaders(context);
    }
    if (callingAppName == null || callingAppName.length() < 1){
      callingAppName = " ";
    }else{
      if (callingAppName.length() > callingAppNameLength){
        callingAppName = callingAppName.substring(0, callingAppNameLength - suffixLen) + trimSuffix;
      }
    }

    if (callingComponent == null || callingComponent.length() < 1){
      callingComponent = " ";
    }else{
      if (callingComponent.length() > callingComponentLength){
        callingComponent = callingComponent.substring(0, callingComponentLength - suffixLen) + trimSuffix;
      }
    }

    if (callingType == null || callingType.length() < 1){
      callingType = " ";
    }else{
      if (callingType.length() > callingTypeLength){
        callingType = callingType.substring(0, callingTypeLength - suffixLen) + trimSuffix;
      } 
    }

    if (callingCompany == null || callingCompany.length() < 1){
      callingCompany = " ";
    }else{
      if (callingCompany.length() > callingCompanyLength){
        callingCompany = callingCompany.substring(0, callingCompanyLength - suffixLen) + trimSuffix;
      }
    }

    if (callingSys == null || callingSys.length() < 1){
      callingSys = " ";
    }else{
      if (callingSys.length() > callingSysLength){
        callingSys = callingSys.substring(0, callingSysLength - suffixLen) + trimSuffix;
      }
    }
    
    if (callingUserCode == null || callingUserCode.length() < 1){
      callingUserCode = " ";
    }else{
      if (callingUserCode.length() > callingUserCodeLength){
        callingUserCode = callingUserCode.substring(0, callingUserCodeLength - suffixLen) + trimSuffix;
      }
    }
  }

  private boolean extractHTTPMeteringHeaders(ProviderContextHelper context){
    /*Transport t = context.getTransport();
    if (! (t instanceof HTTPTransport)){
      return false;
    }

    HTTPTransport httpT = (HTTPTransport) t;
    HttpServletRequest request = httpT.getRequest();
    callingAppName = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPNAME);
    headerFound = callingAppName != null;

    callingComponent = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPONENT);
    headerFound = headerFound || callingComponent != null;

    callingType = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPTYPE);
    headerFound = headerFound || callingType != null;

    callingCompany = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
    headerFound = headerFound || callingCompany != null;

    callingSys = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_SYS); 
    headerFound = headerFound || callingSys != null;
    
    callingUserCode = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_USER_CODE); 
    headerFound = headerFound || callingUserCode != null;*/

    boolean headerFound = false;
    
    // take HTTP headers from persistable context (see ServiceMeteringUtil.moveHTTPMeteringHeadersToPersistableContext()
    ConfigurationContext ctx = context.getPersistableContext();
    callingAppName = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_APPNAME);
    headerFound = callingAppName != null;
    
    callingComponent = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_COMPONENT);
    headerFound = callingComponent != null;
    
    callingType = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_APPTYPE);
    headerFound = callingType != null;
    
    callingCompany = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
    headerFound = callingCompany != null;
    
    callingSys = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_SYS);
    headerFound = callingSys != null;
    
    callingUserCode = (String)ctx.getProperty(ServiceMeteringConstants.HTTP_HEADER_USER_CODE);
    headerFound = callingUserCode != null;

    return headerFound;
  }

  private void extractSOAPMeteringHeaders(ProviderContextHelper context) throws RuntimeProcessException{
    SOAPMessage message = (SOAPMessage) context.getMessage();
    SOAPHeaderList headers = message.getSOAPHeaders();
    QName soapMeteringHeaderQName = new QName(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAPHEADER_NAME);
    Element soapMeteringHeader = headers.getHeader(soapMeteringHeaderQName);
    
    if (soapMeteringHeader == null){
      return;
    }
    
    callingAppName = " ";
    callingComponent = " ";
    callingType = " ";
    callingCompany = " ";
    callingSys = " ";
    callingUserCode = " ";

    String ns = soapMeteringHeaderQName.getNamespaceURI();
    NodeList header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_APPNAME);
    if (header != null && header.getLength() > 0){
      callingAppName = getSOAPHeaderValue(header);
    }

    header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_COMPONENT);
    if (header != null && header.getLength() > 0){
      callingComponent = getSOAPHeaderValue(header);
    }

    header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_APPTYPE);
    if (header != null && header.getLength() > 0){
      callingType = getSOAPHeaderValue(header);
    }

    header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_COMPANY);
    if (header != null && header.getLength() > 0){
      callingCompany = getSOAPHeaderValue(header);
    }

    header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_SYS);
    if (header != null && header.getLength() > 0){
      callingSys = getSOAPHeaderValue(header);
    }
    
    header = soapMeteringHeader.getElementsByTagNameNS(ns, ServiceMeteringConstants.SOAP_HEADER_USER_CODE);
    if (header != null && header.getLength() > 0){
      callingUserCode = getSOAPHeaderValue(header);
    }
  }

  private String getSOAPHeaderValue(NodeList nl){
    // extract header element
    Element headerElement = null;
    if (nl.getLength() > 0){
      if (nl.item(0) instanceof Element){
        headerElement = (Element) nl.item(0);
      }
    }
    if (headerElement == null){
      return " ";
    }
    NodeList headerChilds = headerElement.getChildNodes();
    int size = headerChilds.getLength();
    if (size == 1){
      Node node1 = headerChilds.item(0);
      if (node1 instanceof Text){
        Text txtNode = (Text) node1;
        return txtNode.getNodeValue();
      }else{
        return  " "; 
      }
    }else{ // check if all nodes are text nodes - text is fragmented when it contains entity references
      StringBuilder nodeText = new StringBuilder(32);
      Node n;
      for (int i = 0; i < headerChilds.getLength(); i ++){
        n = headerChilds.item(i);
        if (n instanceof Text){
          nodeText.append(((Text)n).getNodeValue());
        }else{
          return " ";
        }
      }
      return nodeText.toString();
    }
  }

  public int hashCode(){
    int hash = "".hashCode();
    hash ^= wsdlOperationName   == null ? "null".hashCode() : wsdlOperationName.hashCode();
    hash ^= internalServiceName == null ? "null".hashCode() : internalServiceName.hashCode();
    hash ^= callingAppName      == null ? "null".hashCode() : callingAppName.hashCode();
    hash ^= callingComponent    == null ? "null".hashCode() : callingComponent.hashCode();
    hash ^= callingSys          == null ? "null".hashCode() : callingSys.hashCode();
    hash ^= callingCompany      == null ? "null".hashCode() : callingCompany.hashCode();
    hash ^= callingType         == null ? "null".hashCode() : callingType.hashCode();
    hash ^= callingUserCode     == null ? "null".hashCode() : callingUserCode.hashCode();
    
    return hash;
  }

  // compare only keys that are primary keys in `caller` and `operation` tables
  public boolean equals(Object o){
    if (! (o instanceof MeteringInMemoryRecord)){
      return false;
    }
    MeteringInMemoryRecord mimr = (MeteringInMemoryRecord)o;

    if (stringFieldDifferent(this.wsdlOperationName, mimr.wsdlOperationName) || 
        // stringFieldDifferent(this.portTypeName, mimr.portTypeName) || 
        // stringFieldDifferent(this.portTypeNamespace, mimr.portTypeNamespace) || 
        stringFieldDifferent(this.internalServiceName, mimr.internalServiceName) ||
        stringFieldDifferent(this.callingAppName, mimr.callingAppName) || 
        stringFieldDifferent(this.callingComponent, mimr.callingComponent) ||
        stringFieldDifferent(this.callingSys, mimr.callingSys) ||
        stringFieldDifferent(this.callingCompany, mimr.callingCompany) ||
        stringFieldDifferent(this.callingType, mimr.callingType) ||
        stringFieldDifferent(this.callingUserCode, mimr.callingUserCode)) {
      return false;
    }
    return true;
  }

  private boolean stringFieldDifferent(String f1, String f2){
    if (f1 != null){
      if (f2 == null){
        return true;
      }else{
        return ! f1.equals(f2);
      }
    }else{
      return f2 != null;
    }
  }

  public void receiveNotification() {} // overridden in MeteringInMemoryRecordNotificator
}

