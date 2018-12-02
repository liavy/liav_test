package com.sap.engine.services.webservices.server.deploy.jee5;

public class AnnotationConstants {
  	
	public static final String AUTHENTICATION_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.AuthenticationDT";
	public static final String AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL = "authenticationLevel";
	public static final String AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_NONE = "NONE";
	public static final String AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_BASIC = "BASIC";
	public static final String AUTHENTICATION_DT_ATTRIBUTE_AUTHENTICATIONLEVEL_STRONG = "STRONG";

	public static final String BLOCKING_DT_OPERATION = "javax.jws.Oneway";
	public static final String BLOCKING_DT_OPERATION_ATTRIBUTE_ENABLEBLOCKING = "enableBlocking";
	public static final boolean BLOCKING_DT_OPERATION_ATTRIBUTE_ENABLEBLOCKING_DEFAULT = false;
	
	public static final String COMMIT_HANDLING_DT_OPERATION = "javax.ejb.TransactionAttribute";
	public static final String COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT = "value";
	public static final String COMMIT_HANDLING_DT_OPERATION_ATTRIBUTE_ENABLECOMMIT_DEFAULT = "REQUIRED";

	public static final String INTERFACE_AND_OPERATION_NAMING_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.InterfaceAndOperationNamingDT";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_PROGRAM = "program";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_PROGRAM_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SERVICEDEFINITION = "serviceDefinition";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SERVICEDEFINITION_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SOAPAPPLICATION = "soapApplication";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_ATTRIBUTE_SOAPAPPLICATION_DEFAULT = "";

	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.InterfaceAndOperationNamingDTOperation";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_INTERNALNAME = "internalName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_INTERNALNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_MAPPEDNAME = "mappedName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_MAPPEDNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAME = "externalName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAMESPACE = "externalNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALNAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAME = "externalRequestName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAMESPACE = "externalRequestNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALREQUESTNAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAME = "externalResponseName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAMESPACE = "externalResponseNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALRESPONSENAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAME = "externalFaultName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAMESPACE = "externalFaultNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_ATTRIBUTE_EXTERNALFAULTNAMESPACE_DEFAULT = "";

	public static final String MESSAGE_ID_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.MessageIDDT";
	public static final String MESSAGE_ID_DT_ATTRIBUTE_ENABLEMESSAGEID = "enableMessageId";
	public static final boolean MESSAGE_ID_DT_ATTRIBUTE_ENABLEMESSAGEID_DEFAULT = true;

	public static final String REL_MESSAGING_NW05_DT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.RelMessagingNW05DTOperation";
	public static final String REL_MESSAGING_NW05_DT_OPERATION_ATTRIBUTE_ENABLEWSRM = "enableWSRM";
	public static final boolean REL_MESSAGING_NW05_DT_OPERATION_ATTRIBUTE_ENABLEWSRM_DEFAULT = false;

	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.RuntimeIntrinsicFunctionsDTOperation";
	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_ATTRIBUTE_MEP = "mep";
	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_ATTRIBUTE_MEP_DEFAULT = "RequestResponse";

	public static final String SESSION_HANDLING_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.SessionHandlingDT";
	public static final String SESSION_HANDLING_DT_ATTRIBUTE_ENABLESESSION = "enableSession";
	public static final boolean SESSION_HANDLING_DT_ATTRIBUTE_ENABLESESSION_DEFAULT = false;

	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.TransactionHandlingNW05DTOperation";
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED = "required";
	public static final boolean TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_DEFAULT = false;
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_YES = "yes";
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_ATTRIBUTE_REQUIRED_NO = "no";

	public static final String TRANSPORT_GUARANTEE_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.TransportGuaranteeDT";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL = "level";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_NONE = "NONE";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_INTEGRITY = "INTEGRITY";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_CONFIDENTIALITY = "CONFIDENTIALITY";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_BOTH = "BOTH";
	public static final String TRANSPORT_GUARANTEE_DT_ATTRIBUTE_LEVEL_INTEGRITY_AND_CONFIDENTIALITY = "INTEGRITY_AND_CONFIDENTIALITY";
	
	public static final String WS_ADDRESSING_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.Addressing";
	public static final String WS_ADDRESSING_DT_ATTRIBUTE_ENABLED = "enabled";
	public static final boolean WS_ADDRESSING_DT_ATTRIBUTE_ENABLED_DEFAULT = true;

	public static final String WS_ADDRESSING_DT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.WSAddressingDTOperation";
	public static final String WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_INPUTACTION = "inputAction";
	public static final String WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_INPUTACTION_DEFAULT = "";
	public static final String WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_OUTPUTACTION = "outputAction";
	public static final String WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_OUTPUTACTION_DEFAULT = "";
	public static final String WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_FAULTACTION = "faultAction";
	public static final String[] WS_ADDRESSING_DT_OPERATION_ATTRIBUTE_FAULTACTION_DEFAULT = {""};
	
	public static final String XIENABLED_DT = "com.sap.engine.services.webservices.espbase.configuration.ann.dt.XIEnabled";
	
	public static final String CENTRAL_ADMINISTRATION_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.CentralAdministrationRT";
	public static final String CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILENAME = "ProfileName";
	public static final String CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILENAME_DEFAULT = "";
	public static final String CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILEVERSION = "ProfileVersion";
	public static final int CENTRAL_ADMINISTRATION_RT_ATTRIBUTE_PROFILEVERSION_DEFAULT = 0;

	public static final String TRANSPORT_GUARANTEE_RT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportGuaranteeRTOperation";
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART
		= "IncomingSignatureExpectedSignedElementOperationMessagePart";
	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT
    = {"wssp:Body()", "wssp:Header(*)"};
  	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART
    	= "IncomingEncryptionExpectedEncryptedElementOperationMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT
    	= {"wssp:Body()"};
  	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART
    	= "OutgoingSignatureSignedElementOperationMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT
    	= {"wssp:Body()", "wssp:Header(*)"};
  	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART
	    = "OutgoingEncryptionEncryptedElementOperationMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT
    	= {"wssp:Body()"};
		
	public static final String AUTHENTICATION_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.AuthenticationRT";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_SINGLESIGNON = "SingleSignOn";
//	public static final boolean AUTHENTICATION_RT_ATTRIBUTE_SINGLESIGNON_DEFAULT = false;
	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHOD = "AuthenticationMethod";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD
//	    = "AuthenticationMethodSAMLSubjectConfirmationMethod";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD_DEFAULT
//	    = "sapsp:SenderVouchesWithXMLSignature";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLISSUER = "AuthenticationMethodSAMLIssuer";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLISSUER_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME = "AuthenticationMethodSAMLSenderVouchesAttesterName";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME_DEFAULT = "saml_default_attester";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY = "AuthenticationMethodSAMLAssertionValidity";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY_DEFAULT = "180";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION = "AuthenticationMethodSAMLDoNotCacheCondition";
//	public static final boolean AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION_DEFAULT = true;
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION
//	    = "AuthenticationMethodSAMLHolderOfKeyAttesterSystemDestination";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION_DEFAULT
//	    = "WS_SAML_attester_default";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME
//	    = "AuthenticationMethodSAMLHolderOfKeyAttesterName";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME_DEFAULT
//	    = "saml_default_attester";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICUSERNAME = "AuthenticationMethodBasicUsername";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICUSERNAME_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICPASSWORD = "AuthenticationMethodBasicPassword";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODBASICPASSWORD_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREVIEW = "AuthenticationMethodX509KeystoreView";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREVIEW_DEFAULT = "WebServiceSecurity";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREALIAS = "AuthenticationMethodX509KeystoreAlias";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODX509KEYSTOREALIAS_DEFAULT = "System-key";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME
//	    = "AuthenticationMethodABAPServiceUserUsername";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME_DEFAULT
//	    = "";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE
//	    = "AuthenticationMethodABAPServiceUserLanguage";
//	public static final String AUTHENTICATION_RT_ATTRIBUTE_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE_DEFAULT
//	    = "";
	
  	public static final String WS_ADDRESSING_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.WSAddressingRT";
  	public static final String WS_ADDRESSING_RT_ATTRIBUTE_WSAPROTOCOL = "WSAProtocol";
  	public static final String WS_ADDRESSING_RT_ATTRIBUTE_WSAPROTOCOL_DEFAULT = "http://www.w3.org/2005/03/addressing";
//  	public static final String WS_ADDRESSING_RT_ATTRIBUTE_REFERENCEPARAMETERS = "ReferenceParameters";
//  	public static final String[] WS_ADDRESSING_RT_ATTRIBUTE_REFERENCEPARAMETERS_DEFAULT = {""};
	
	public static final String REL_MESSAGING_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.RelMessagingRT";
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_RMPROTOCOL = "RMprotocol";
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_RMPROTOCOL_DEFAULT = "";
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_RETRANSMISSIONINTERVAL = "RetransmissionInterval";
  	public static final int REL_MESSAGING_RT_ATTRIBUTE_RETRANSMISSIONINTERVAL_DEFAULT = 42000;
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_INACTIVITYTIMEOUT = "InactivityTimeout";
  	public static final int REL_MESSAGING_RT_ATTRIBUTE_INACTIVITYTIMEOUT_DEFAULT = 0;
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_ACKINTERVAL = "AckInterval";
  	public static final int REL_MESSAGING_RT_ATTRIBUTE_ACKINTERVAL_DEFAULT = 600;
  	public static final String REL_MESSAGING_RT_ATTRIBUTE_SEQUENCELIFETIME = "SequenceLifetime";
  	public static final int REL_MESSAGING_RT_ATTRIBUTE_SEQUENCELIFETIME_DEFAULT = 0;
	
	public static final String MESSAGE_ID_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.MessageIDRT";
  	public static final String MESSAGE_ID_RT_ATTRIBUTE_MESSAGEIDPROTOCOL = "MessageIdProtocol";
  	public static final String MESSAGE_ID_RT_ATTRIBUTE_MESSAGEIDPROTOCOL_DEFAULT = "http://www.w3.org/2005/03/addressing";

  	public static final String SESSION_HANDLING_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.SessionHandlingRT";
  	public static final String SESSION_HANDLING_RT_ATTRIBUTE_SESSIONMETHOD = "SessionMethod";
  	public static final String SESSION_HANDLING_RT_ATTRIBUTE_SESSIONMETHOD_DEFAULT = "httpCookies";
	
	public static final String TRANSPORT_BINDING_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportBindingRT";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLPATH = "URLPath";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLPATH_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLPROTOCOL = "URLProtocol";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLPROTOCOL_DEFAULT = "http";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLHOST = "URLHost";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLHOST_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLPORT = "URLPort";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_URLPORT_DEFAULT = 80;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLCLIENT = "URLClient";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLCLIENT_DEFAULT = "000";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLLANGUAGE = "URLLanguage";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_URLLANGUAGE_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYHOST = "ProxyHost";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYHOST_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPORT = "ProxyPort";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPORT_DEFAULT = 0;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYUSER = "ProxyUser";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYUSER_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPASSWORD = "ProxyPassword";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_PROXYPASSWORD_DEFAULT = "";
  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_ALTHOST = "AltHost";
  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_ALTHOST_DEFAULT = "";
  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPORT = "AltPort";
  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPORT_DEFAULT = 0;
  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPATH = "AltPath";
  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_ALTPATH_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPATH = "CalcPath";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPATH_DEFAULT = 0;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPROTOCOL = "CalcProtocol";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_CALCPROTOCOL_DEFAULT = "http";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATION = "Destination";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATION_DEFAULT = "";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATIONPATH = "DestinationPath";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_DESTINATIONPATH_DEFAULT = 0;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_LOCALCALL = "LocalCall";
//  	public static final boolean TRANSPORT_BINDING_RT_ATTRIBUTE_LOCALCALL_DEFAULT = false;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_STYLE = "Style";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_STYLE_DEFAULT = "Document";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_TYPE = "Type";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_SERVICESESSIONTIMEOUT = "ServiceSessionTimeout";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_SERVICESESSIONTIMEOUT_DEFAULT = 0;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_CONSUMERMAXWAITTIME = "ConsumerMaxWaitTime";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_CONSUMERMAXWAITTIME_DEFAULT = 0;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_OPTIMIZEDXMLTRANSFER = "OptimizedXMLTransfer";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_OPTIMIZEDXMLTRANSFER_DEFAULT = "None";
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSREQUEST = "CompressRequest";
//  	public static final boolean TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSREQUEST_DEFAULT = false;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSRESPONSE = "CompressResponse";
//  	public static final boolean TRANSPORT_BINDING_RT_ATTRIBUTE_COMPRESSRESPONSE_DEFAULT = true;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_KEEPALIVESTATUS = "KeepAliveStatus";
//  	public static final boolean TRANSPORT_BINDING_RT_ATTRIBUTE_KEEPALIVESTATUS_DEFAULT = true;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_SOCKETTIMEOUT = "SocketTimeout";
//  	public static final int TRANSPORT_BINDING_RT_ATTRIBUTE_SOCKETTIMEOUT_DEFAULT = 60000;
//  	public static final String TRANSPORT_BINDING_RT_ATTRIBUTE_CHUNKEDREQUEST = "ChunkedRequest";
//  	public static final boolean TRANSPORT_BINDING_RT_ATTRIBUTE_CHUNKEDREQUEST_DEFAULT = false;
	
//	public static final String TRANSPORT_BINDING_RT_OPERATION = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportBindingRTOperation";
//  	public static final String TRANSPORT_BINDING_RT_OPERATION_ATTRIBUTE_SOAPACTION = "SOAPAction";
//  	public static final String TRANSPORT_BINDING_RT_OPERATION_ATTRIBUTE_SOAPACTION_DEFAULT = "";

//  	public static final String XI_30_RUNTIME_INTEGRATION_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.XI30RuntimeIntegrationRT";
//  	public static final String XI_30_RUNTIME_INTEGRATION_RT_ATTRIBUTE_RUNTIMEENVIRONMENT = "RuntimeEnvironment";
//  	public static final String XI_30_RUNTIME_INTEGRATION_RT_ATTRIBUTE_RUNTIMEENVIRONMENT_DEFAULT = "WS";

//  	public static final String ATTACHMENT_HANDLING_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.AttachmentHandlingRT";
//  	public static final String ATTACHMENT_HANDLING_RT_ATTRIBUTE_ENABLED = "Enabled";
//  	public static final boolean ATTACHMENT_HANDLING_RT_ATTRIBUTE_ENABLED_DEFAULT = true;

//  	public static final String EXTERNAL_ASSERTION_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.ExternalAssertionRT";
//  	public static final String EXTERNAL_ASSERTION_RT_ATTRIBUTE_ASSERTIONROOTNAME = "AssertionRootName";
//  	public static final String[] EXTERNAL_ASSERTION_RT_ATTRIBUTE_ASSERTIONROOTNAME_DEFAULT = {""};
	
	public static final String TRANSPORT_GUARANTEE_RT = "com.sap.engine.services.webservices.espbase.configuration.ann.rt.TransportGuaranteeRT";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURITYMECHANISM = "SecurityMechanism";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURITYMECHANISM_DEFAULT = "sapsp:HTTP";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_TLSTYPE = "TLSType";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_TLSTYPE_DEFAULT = "sapsp:HTTP";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSIGNORESSLSERVERCERTS
//    	= "SSLServerCertsIgnoreSSLServerCerts";
//  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSIGNORESSLSERVERCERTS_DEFAULT
//    	= true;
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW
//	    = "SSLServerCertsAcceptedServerCertsKeystoreView";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW_DEFAULT
//    	= "WebServiceSecurity";
    public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURECONVERSATION = "SecureConversation";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SECURECONVERSATION_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURE = "IncomingSignature";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURE_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW
    	= "IncomingSignatureTrustanchorKeystoreView";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW_DEFAULT
	    = "WebServiceSecurity";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN
//	    = "IncomingSignatureTrustanchorCertificatePattern";
//  	public static final String[] TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN_DEFAULT
//	    = {"Subject=*;Issuer=*;SerialNumber=*"};
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART
    	= "IncomingSignatureExpectedSignedElementMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART_DEFAULT
  		= {"wssp:Body()", "wssp:Header(*)"};
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTION = "IncomingEncryption";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTION_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART
    	= "IncomingEncryptionExpectedEncryptedElementMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART_DEFAULT
    	= {"wssp:Body()"};
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURE = "OutgoingSignature";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURE_DEFAULT = false;
	static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW
    = "OutgoingSignatureSigningKeyKeystoreView";
   	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW_DEFAULT
    	= "WebServiceSecurity";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS
	    = "OutgoingSignatureSigningKeyKeystoreAlias";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS_DEFAULT
	    = "System-key";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART
    	= "OutgoingSignatureSignedElementMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART_DEFAULT
    	= {"wssp:Body()", "wssp:Header(*)"};
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTION = "OutgoingEncryption";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTION_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW
	    = "OutgoingEncryptionEncryptingKeyKeystoreView";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW_DEFAULT
	    = "WebServiceSecurity_Certs";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS
	    = "OutgoingEncryptionEncryptingKeyKeystoreAlias";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS_DEFAULT
	    = "";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN
	    = "OutgoingEncryptionEncryptingKeyOrigin";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN_DEFAULT
	    = "sapsp:UseSignatureCertificate";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART
	    = "OutgoingEncryptionEncryptedElementMessagePart";
  	public static final String[] TRANSPORT_GUARANTEE_RT_ATTRIBUTE_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART_DEFAULT
    	= {"wssp:Body()"};
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGE = "IncomingMessageAge";
//  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGE_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGEAGE
    	= "IncomingMessageAgeAge";
  	public static final int TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCOMINGMESSAGEAGEAGE_DEFAULT
	    = 180;
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR
//	    = "SOAPErrorBehaviorApplySecuritySettingsFor";
//  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR_DEFAULT
//	    = "sapsp:NoSOAPFaults";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCLUDETIMESTAMP = "IncludeTimestamp";
  	public static final boolean TRANSPORT_GUARANTEE_RT_ATTRIBUTE_INCLUDETIMESTAMP_DEFAULT = false;
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_ALGORITHMSUITE = "AlgorithmSuite";
  	public static final String TRANSPORT_GUARANTEE_RT_ATTRIBUTE_ALGORITHMSUITE_DEFAULT = "Basic128Rsa15";
  	
  	public static String[] classLevelDTAnnotations_SAP = new String[]{AUTHENTICATION_DT, MESSAGE_ID_DT, SESSION_HANDLING_DT, TRANSPORT_GUARANTEE_DT, WS_ADDRESSING_DT, XIENABLED_DT}; 
  	public static String[] classLevelDTAnnotations = classLevelDTAnnotations_SAP;
  	public static String[] methodLevelDTAnnotations_SAP = new String[]{REL_MESSAGING_NW05_DT_OPERATION};  	 
  	public static String[] classLevelRTAnnotations_SAP = new String[]{CENTRAL_ADMINISTRATION_RT, AUTHENTICATION_RT, WS_ADDRESSING_RT, REL_MESSAGING_RT, MESSAGE_ID_RT, SESSION_HANDLING_RT, TRANSPORT_BINDING_RT, TRANSPORT_GUARANTEE_RT};  	
  	public static String[] methodLevelRTAnnotations_SAP = new String[]{TRANSPORT_GUARANTEE_RT_OPERATION};
  	
  	public static String[] getTypeLevelDTAnnotations() {
  		String[] result = new String[6];
  		result[0] = AUTHENTICATION_DT;
  		result[1] = MESSAGE_ID_DT;
  		result[2] = SESSION_HANDLING_DT;
  		result[3] = TRANSPORT_GUARANTEE_DT;
  		result[4] = WS_ADDRESSING_DT;
  		result[5] = XIENABLED_DT;
  		return result;
  	}
    	
  	public static String[] getMethodLevelDTAnnotations() {
  		String[] result = new String[3];
  		result[0] = BLOCKING_DT_OPERATION;//@OneWay
  		result[1] = COMMIT_HANDLING_DT_OPERATION;//@TransactionAttribute
  		result[2] = REL_MESSAGING_NW05_DT_OPERATION;
  		//result[2] = INTERFACE_AND_OPERATION_NAMING_DT_OPERATION;
  		//result[4] = RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION;
  		//result[5] = TRANSACTION_HANDLING_NW05_DT_OPERATION;
  		//result[6] = WS_ADDRESSING_DT_OPERATION;
  		return result;
  	}
  	
  	public static String[] getTypeLevelRTAnnotations() {
  		String[] result = new String[8];
  		result[0] = CENTRAL_ADMINISTRATION_RT;
  		result[1] = AUTHENTICATION_RT;
  		result[2] = WS_ADDRESSING_RT;
  		result[3] = REL_MESSAGING_RT;
  		result[4] = MESSAGE_ID_RT;
  		result[5] = SESSION_HANDLING_RT;
  		result[6] = TRANSPORT_BINDING_RT;
  		//result[7] = XI_30_RUNTIME_INTEGRATION_RT;
  		//result[8] = ATTACHMENT_HANDLING_RT;
  		//result[8] = EXTERNAL_ASSERTION_RT;
  		result[7] = TRANSPORT_GUARANTEE_RT;
  		return result;
  	}

  public static String[] getMethodLevelRTAnnotations() {
    String[] result = new String[1];
  	//result[0] = TRANSPORT_BINDING_RT_OPERATION;
  	result[0] = TRANSPORT_GUARANTEE_RT_OPERATION;  		
  	return result;
  }
  	
}
