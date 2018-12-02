/**
 * Created by IntelliJ IDEA.
 * User: alexander-z
 * Date: Dec 10, 2002
 * Time: 5:16:28 PM
 * To change this template use Options | File Templates.
 */
package com.sap.engine.services.webservices;

import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.tc.logging.Severity;

import java.net.URLEncoder;

public class UIHelper {
  public static final String SAP_MODE_PARAMETER = "mode=sap_wsdl";

  public static final String WSDL_STYLE_TEXT = "WSDL Style";
  public static final String USE_SAP_STYLE_TEXT = "Use SAP WSDL";
  public static final String PACKAGE_NAME_TEXT = "Package Name";
  public static final String URI_TO_PACKAGE_MAPPING_TEXT = "Namespace URI to Java Package Mapping";
  public static final String COMPILE_TEXT = "Compile";
  public static final String INTERFACES_ONLY_TEXT = "Generate Interfaces Only";
  public static final String RPC_STYLE_TEXT = "Try Generating RPC Style Proxy";
  public static final String JAXRPC_INTERFACES_TEXT = "Try Generating JAX-RPC Service Interfaces";
  public static final String PACKAGE_NAME_NEEDED_TEXT = "A package name must be specified";

  public static final String FILE_LOCATION_TEXT = "Save As";
  public static final String BROWSE_BUTTON_TEXT = "Browse";
  public static final String GENERATE_BUTTON_TEXT = "Generate";

  public static final String SEVERITY_LEVEL_TEXT = "Severity Level";
  public static final String LOGS_LIST_TEXT = "Logs";
  public static final String LOG_TEXT = "Text";

  public static final String SERVICE_NOT_PUBLISHED = "The service is not published, yet";
  public static final String DO_YOU_WANT_TO_SAVE_CHANGES = "This action will make some runtime changes. If you continue the application may be restarted. You will lose the changes if you redeploy the application later on. Do you want to continue?";
  public static final String MISSING_WSD_TMODELKEY = "Note that the WSD is not published too and you have to match it manually with an existing InstanceInfo.";
  public static final String DOCUMENTATION_NOT_SPECIFIED = "Documentation not specified";

  public static String[][] getAllWSDLStylesAsURLs(WSRuntimeDefinition webService, String host, int port) {
    String[][] styles = getAllWSDLStyles(webService);

    String endpointId = webService.getServiceEndpointDefinitions()[0].getServiceEndpointId();
    if (endpointId.startsWith("/")) {
      endpointId = endpointId.substring(1);
    }
    String url = "http://" + host + ":" + port + "/" + endpointId + "?wsdl";
    for (int i = 0; i < styles.length; i++) {
      if (styles[i][1].length() > 0) {
        styles[i][1] = url + "&style=" + styles[i][1];
      } else {
        styles[i][1] = url;
      }
    }
    return styles;
  }

  public static String getSapWsdlFromOriginalUrl(String wsdlURL) {
    StringBuffer buf = new StringBuffer(wsdlURL);
    if (wsdlURL.indexOf("?") == -1) {
      buf.append('?');
    } else {
      buf.append('&');
    }
    buf.append("mode=sap_wsdl");
    return buf.toString();
  }

  public static String[][] getAllWSDLStyles(WSRuntimeDefinition webService) {
    String[] supportedStyles = webService.getWsdlSupportedStyles();

    String[][] styles = new String[supportedStyles.length + 1][2];
    styles[0][0] = getNameOfStyle("");
    styles[0][1] = "";
    for (int i = 0; i < supportedStyles.length; i++) {
      String style = supportedStyles[i];
      styles[i + 1][0] = getNameOfStyle(style);
      styles[i + 1][1] = style;
    }
    return styles;
  }

  public static String getNameOfStyle(String style) {
    if (style.length() == 0) {
      return "Default";
    } else if ("document".equals(style)) {
      return "Document";
    } else if ("rpc".equals(style)) {
      return "RPC";
    } else if ("rpc_document".equals(style)) {
      return "RPC & Document";
    } else if ("rpc_enc".equals(style)) {
      return "RPC Encoded";
    } else if ("http".equals(style)) {
      return "HTTP";
    } else {
      return style;
    }
  }

  public static String getUDDIKey(WSRuntimeDefinition webService) {
    return webService.getUddiKey();
  }

  public static String[][] getTargetAddresses(WSRuntimeDefinition webService, String host, int port) {
    ServiceEndpointDefinition[] endpoints = webService.getServiceEndpointDefinitions();
    String[][] addresses = new String[endpoints.length][2];
    for (int i = 0; i < endpoints.length; i++) {
      ServiceEndpointDefinition endpoint = endpoints[i];
      addresses[i][0] = endpoint.getServiceEndpointQualifiedName().getLocalPart();
      String endpointId = endpoint.getServiceEndpointId();
      if (endpointId.startsWith("/")) {
        endpointId = endpointId.substring(1);
      }
      addresses[i][1] = "http://" + host + ":" + port + "/" + endpointId;
    }
    return addresses;
  }

  public static final String getPublishInUDDIUrl(WSRuntimeDefinition webService, String host, String port, String inquiryURL, String publishURL) {
    StringBuffer buf = new StringBuffer("http://" + host + ":" + port + "/uddiclient/tools?action=Service");
    WSIdentifier wsId = webService.getWSIdentifier();
    buf.append("&serviceName=" + URLEncoder.encode(wsId.getServiceName())); //$JL-I18N$
    buf.append("&applicationName=" + URLEncoder.encode(wsId.getApplicationName())); //$JL-I18N$
    buf.append("&jarName=" + URLEncoder.encode(wsId.getJarName())); //$JL-I18N$
    if (inquiryURL != null && publishURL != null) {
      buf.append("&inquiryURL=" + URLEncoder.encode(inquiryURL)); //$JL-I18N$
      buf.append("&publishURL=" + URLEncoder.encode(publishURL)); //$JL-I18N$
    }
    return buf.toString();
  }

  public static String severityToString(int severity) {
    switch (severity) {
      case Severity.ALL: return "All";
      case Severity.DEBUG: return "Debug";
      case Severity.PATH: return "Path";
      case Severity.INFO: return "Info";
      case Severity.WARNING: return "Warning";
      case Severity.ERROR: return "Error";
      case Severity.FATAL: return "Fatal";
      case Severity.NONE: return "None";
    }
    return "N/A";
  }

  /**
   * @param identifier
   * @return
   */
  public static String wsIDtoSLDName(WSIdentifier id) {
    return "{" + id.getApplicationName() + "}{" + id.getJarName() + "}{" + id.getServiceName() + "}";
  }
}
