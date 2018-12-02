package com.sap.engine.services.webservices.espbase.configuration;

import javax.xml.namespace.QName;

public interface ServiceMeteringConstants {
   /**
    * Protocol name as it appears in ConsumerProtocolFactory lists
    */
   public static final String PROTOCOL_NAME = "MeteringProtocol";
   /**
    * Calling system HTTP header - value is engine SID
    */
   public static final String HTTP_HEADER_SYS = "CallingSys";
   /**
    * Calling company HTTP header - value is installation number
    */
   public static final String HTTP_HEADER_COMPANY = "CallingCompany";
   /**
    * Calling application HTTP header
    */
   public static final String HTTP_HEADER_APPNAME = "CallingApp";
   /**
    * Calling application type HTTP header - see APPTYPE_VALUE
    */
   public static final String HTTP_HEADER_APPTYPE = "CallingType";
   /*
    * Calling component HTTP header
    */
   public static final String HTTP_HEADER_COMPONENT = "CallingComponent";
   /*
    * Calling user HTTP header - BASE64 encoded SHA-1 digest of the user name
    */
   public static final String HTTP_HEADER_USER_CODE = "CallingUsr";
   /**
    * Calling system SOAP header - value is engine SID
    */
   public static final String SOAP_HEADER_SYS = "Sys";
   /**
    * Calling company SOAP header - value is installation number
    */
   public static final String SOAP_HEADER_COMPANY = "Company";
   /**
    * Calling application SOAP header
    */
   public static final String SOAP_HEADER_APPNAME = "App";
   /**
    * Calling application type SOAP header - see APPTYPE_VALUE
    */
   public static final String SOAP_HEADER_APPTYPE = "Type";
   /*
    * Calling component SOAP header
    */
   public static final String SOAP_HEADER_COMPONENT = "Component";
   /*
    * Calling user SOAP header
    */
   public static final String SOAP_HEADER_USER_CODE = "UsrCode";
   /**
    * Calling application type value for the respective HTTP and SOAP headers
    */
   public static final String HEADER_APPTYPE_VALUE = "SJ";
   /**
    * SOAP Header name
    */
   public static final String SOAPHEADER_NAME = "CallerInformation";
   /**
    * SOAP header namespace
    */
   public static final String SOAPHEADER_NS= "http://www.sap.com/webas/712/soap/features/runtime/metering/";
   /**
    * Service metering feature namespace
    */
   public static final String METERING_NS = "http://www.sap.com/webas/710/service/metering/";
   /**
    * Service Metering feature `Level` property 
    */
   public static final String METERING_LEVEL_PROP = "Level";
   /**
    * Service Metering feature `Protocol` property
    */
   public static final String METERING_PROTOCOL_PROP = "Protocol";
   /**
    * Service Metering feature `Protocol` property value (default)
    */
   public static final String METERING_PROTOCOL_PROP_HTTP = "HTTPHeader";
   /**
    * Service Metering feature `Protocol` property value
    */
   public static final String METERING_PROTOCOL_PROP_SOAP = "SOAPHeader";
   
   /**
    * Property for disabling service metering 
    * NOTE: not available for customers
    *       available only internally (uncomment the relevant section in Metering protocol and recompile)
    *       for performance measurement purposes
    */
   public static final String METERING_DISABLED_SYSPROPERTY = "servicemetering.disable";
   
   
   public static final String METERING_USER_CODE_HASH_ALGORITHM = "SHA-1";
   public static final String METERING_USER_CODE_ENCODING = "UTF-8";
   
   public static final QName QNAME_HEADER_COMPONENT = new QName(SOAPHEADER_NS, HTTP_HEADER_COMPONENT);
   public static final QName QNAME_HEADER_COMPANY = new QName(SOAPHEADER_NS, HTTP_HEADER_COMPANY);
   public static final QName QNAME_HEADER_SYS = new QName(SOAPHEADER_NS, HTTP_HEADER_SYS);
   public static final QName QNAME_HEADER_APPNAME = new QName(SOAPHEADER_NS, HTTP_HEADER_APPNAME);
   public static final QName QNAME_HEADER_APPTYPE = new QName(SOAPHEADER_NS, HTTP_HEADER_APPTYPE);
   public static final QName QNAME_HEADER_USER_CODE = new QName(SOAPHEADER_NS, HTTP_HEADER_USER_CODE);
}
