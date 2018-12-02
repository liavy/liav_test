package com.sap.engine.services.webservices.espbase.server.runtime.metering;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;

public class ServiceMeteringUtil {

  /**
   * Move metering http headers to persistable context. Necessary only for WS-RM cases, but done for all for consistency.
   * Previously metering headers were added to the SOAP  headers of the message in beforeHibernation(), but this caused problems with message level security
   */
  public static boolean moveHTTPMeteringHeadersToPersistableContext(ProviderContextHelper context){
    Object oTr = context.getTransport();

    if (oTr != null && oTr instanceof HTTPTransport) {
      HttpServletRequest request = ((HTTPTransport) oTr).getRequest();
      if (request == null){
        return false;
      }
      ConfigurationContext ctx = context.getPersistableContext();
      String header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPNAME);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_APPNAME,   header);
      }
      header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPONENT);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_COMPONENT, header);
      }
      header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPTYPE);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_APPTYPE,   header);
      }
      header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_COMPANY,   header);
      }
      header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_SYS);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_SYS, header);
      }
      header = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_USER_CODE);
      if (header != null){
        ctx.setProperty(ServiceMeteringConstants.HTTP_HEADER_USER_CODE, header);
      }
      return true;
    }
    return false;
  }

  /*public static Map<String, String> extractHTTPMeteringHeaders(ProviderContextHelper context){
    Map<String, String> headersMap = new HashMap<String, String>();
    Object oTr = context.getTransport();

    if (oTr != null && oTr instanceof HTTPTransport) {
      HttpServletRequest request = ((HTTPTransport) oTr).getRequest();
      if (request == null){
        return headersMap;
      }
      headersMap.put("callingAppName", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPNAME));
      headersMap.put("callingComponent", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPONENT));
      headersMap.put("callingAppType", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPTYPE));
      headersMap.put("callingCompany", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPANY));
      headersMap.put("callingSys", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_SYS));
      headersMap.put("callingUsr", request.getHeader(ServiceMeteringConstants.HTTP_HEADER_USER_CODE));
    }
    return headersMap;
  }*/

  public static boolean moveMeteringDataToSOAPHeaders(ProviderContextHelper context) throws RuntimeProcessException{
    Object oTr = context.getTransport();

    if (oTr != null && oTr instanceof HTTPTransport) {
      HttpServletRequest request = ((HTTPTransport) oTr).getRequest();
      if (request == null){
        return false;
      }

      String callingAppName = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPNAME);
      String callingComponent = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPONENT);
      String callingAppType = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPTYPE);
      String callingCompany = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
      String callingSys = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_SYS); 
      String callingUsr = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_USER_CODE);


      //Map<String, String> headersMap = extractHTTPMeteringHeaders(context);

      Object oMsg = context.getMessage();
      if (oMsg == null){
        return false;
      }

      SOAPMessage soapMsg = (SOAPMessage) oMsg;
      /*return moveMeteringDataToSOAPHeaders(headersMap.get("callingAppName"), headersMap.get("callingComponent"),headersMap.get("callingAppType"),
        headersMap.get("callingCompany"),headersMap.get("callingSys"),headersMap.get("callingUsr"),
        soapMsg);*/
      return moveMeteringDataToSOAPHeaders(callingAppName, callingComponent, callingAppType, callingCompany, callingSys, callingUsr, soapMsg);
    }
    return false;
  }

  public static boolean moveMeteringDataToSOAPHeaders(String[] callingAppName, String[] callingComponent, String[] callingAppType, 
      String[] callingCompany, String[] callingSys, String[] callingUsr, SOAPMessage soapMsg){

    String _callingAppName = callingAppName == null || callingAppName.length < 1 ? "" : callingAppName[0];
    String _callingComponent = callingComponent == null || callingComponent.length < 1 ? "" : callingComponent[0];
    String _callingAppType = callingAppType == null || callingAppType.length < 1 ? "" : callingAppType[0];
    String _callingCompany = callingCompany == null || callingCompany.length < 1 ? "" : callingCompany[0];
    String _callingSys = callingSys == null || callingSys.length < 1 ? "" : callingSys[0];
    String _callingUsr = callingUsr == null || callingUsr.length < 1 ? "" : callingUsr[0];
    return moveMeteringDataToSOAPHeaders(_callingAppName, _callingComponent, _callingAppType, _callingCompany, _callingSys, _callingUsr, soapMsg);
  }

  public static boolean moveMeteringDataToSOAPHeaders(String callingAppName, String callingComponent, String callingAppType, 
      String callingCompany, String callingSys, String callingUsr, SOAPMessage soapMsg) {

    SOAPHeaderList soapHeaders = soapMsg.getSOAPHeaders();
    Document hDoc = soapHeaders.getInternalDocument();
    Element meteringHeader = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, 
        ServiceMeteringConstants.SOAPHEADER_NAME);

    // application type
    Element appTypeEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPTYPE);
    Text appTypeElTxt = hDoc.createTextNode(callingAppType != null ? callingAppType : "");
    appTypeEl.appendChild(appTypeElTxt);
    meteringHeader.appendChild(appTypeEl);

    // application name
    Element appNameEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPNAME);
    Text appNameElTxt = hDoc.createTextNode(callingAppName != null ? callingAppName : "");
    appNameEl.appendChild(appNameElTxt);
    meteringHeader.appendChild(appNameEl);

    // component
    Element componentEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPONENT);
    Text componentElTxt = hDoc.createTextNode(callingComponent != null ? callingComponent : "");
    componentEl.appendChild(componentElTxt);
    meteringHeader.appendChild(componentEl);


    // company
    Element instNoEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPANY);
    Text instNoTxt = hDoc.createTextNode(callingCompany != null  ? callingCompany : "");
    instNoEl.appendChild(instNoTxt);
    meteringHeader.appendChild(instNoEl);

    // sid
    Element sidEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_SYS);
    Text sidElTxt = hDoc.createTextNode(callingSys != null ? callingSys : "");
    sidEl.appendChild(sidElTxt);
    meteringHeader.appendChild(sidEl);

    // user code
    Element usrEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_USER_CODE);
    Text usrElTxt = hDoc.createTextNode(callingUsr != null ? callingUsr : "");
    usrEl.appendChild(usrElTxt);
    meteringHeader.appendChild(usrEl);

    soapHeaders.addHeader(meteringHeader);

    return true;
  }
}