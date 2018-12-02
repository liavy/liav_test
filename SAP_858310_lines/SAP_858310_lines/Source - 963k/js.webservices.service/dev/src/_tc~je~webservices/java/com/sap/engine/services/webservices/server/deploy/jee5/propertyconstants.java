package com.sap.engine.services.webservices.server.deploy.jee5;

import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;

public class PropertyConstants
{
	public static final String AUTHENTICATION_DT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/authentication/";
	public static final String AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL = "AuthenticationLevel";
	public static final String AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_DEFAULT = "None";
	public static final String AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_NONE = "None";
	public static final String AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_BASIC = "Basic";
	public static final String AUTHENTICATION_DT_PROPERTY_AUTHENTICATIONLEVEL_STRONG = "Strong";
	 
	public static final String BLOCKING_DT_OPERATION_NAMESPACE = "http://www.sap.com/NW05/soap/features/blocking/";
	public static final String BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING = "enableBlocking";
	public static final String BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING_DEFAULT = "false";
	public static final String BLOCKING_DT_OPERATION_PROPERTY_ENABLEBLOCKING_TRUE = "true";
	
	public static final String COMMIT_HANDLING_DT_OPERATION_NAMESPACE = "http://www.sap.com/NW05/soap/features/commit/";
	public static final String COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT = "enableCommit";
	public static final String COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_DEFAULT = "true";
	public static final String COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_TRUE = "true";
	public static final String COMMIT_HANDLING_DT_OPERATION_PROPERTY_ENABLECOMMIT_FALSE = "true";
	
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/runtime/interface/";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_PROGRAM = "Program";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_PROGRAM_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SERVICEDEFINITION = "ServiceDefinition";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SERVICEDEFINITION_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SOAPAPPLICATION = "SOAPApplication";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_PROPERTY_SOAPAPPLICATION_DEFAULT = "";
	
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_NAMESPACE = "http://www.sap.com/webas/630/soap/features/runtime/interface/";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_INTERNALNAME = "internalName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_INTERNALNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_MAPPEDNAME = "mappedName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_MAPPEDNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAME = "externalName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAMESPACE = "externalNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALNAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAME = "externalRequestName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAMESPACE = "externalRequestNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALREQUESTNAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAME = "externalResponseName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAMESPACE = "externalResponseNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALRESPONSENAMESPACE_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAME = "externalFaultName";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAME_DEFAULT = "";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAMESPACE = "externalFaultNamespace";
	public static final String INTERFACE_AND_OPERATION_NAMING_DT_OPERATION_PROPERTY_EXTERNALFAULTNAMESPACE_DEFAULT = "";
	
	public static final String MESSAGE_ID_DT_NAMESPACE = "http://www.sap.com/webas/640/soap/features/messageId/";
	public static final String MESSAGE_ID_DT_PROPERTY_ENABLEMESSAGEID = "enableMessageId";
	public static final String MESSAGE_ID_DT_PROPERTY_ENABLEMESSAGEID_DEFAULT = "true";
	
	public static final String REL_MESSAGING_NW05_DT_OPERATION_NAMESPACE = "http://www.sap.com/NW05/soap/features/wsrm/";
	public static final String REL_MESSAGING_NW05_DT_OPERATION_PROPERTY_ENABLEWSRM = "enableWSRM";
	public static final String REL_MESSAGING_NW05_DT_OPERATION_PROPERTY_ENABLEWSRM_DEFAULT = "false";
	
	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_NAMESPACE = "http://www.sap.com/esi/NW05/rif/";
	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_PROPERTY_MEP = "met";
	public static final String RUNTIME_INTRINSIC_FUNCTIONS_DT_OPERATION_PROPERTY_MEP_DEFAULT = "RequestResponse";
	
	public static final String SESSION_HANDLING_DT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/session/";
	public static final String SESSION_HANDLING_DT_PROPERTY_ENABLESESSION = "enableSession";
	public static final String SESSION_HANDLING_DT_PROPERTY_ENABLESESSION_DEFAULT = "false";
	
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_NAMESPACE = "http://www.sap.com/NW05/soap/features/transaction/";
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_PROPERTY_REQUIRED = "required";
	public static final String TRANSACTION_HANDLING_NW05_DT_OPERATION_PROPERTY_REQUIRED_DEFAULT = "no";
	
	public static final String TRANSPORT_GUARANTEE_DT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL = "Level";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_DEFAULT = "None";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_NONE = "None";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_INTEGRITY = "Integrity";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_CONFIDENTIALITY = "Confidentiality";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_BOTH = "Both";
	public static final String TRANSPORT_GUARANTEE_DT_PROPERTY_LEVEL_INTEGRITY_AND_CONFIDENTIALITY = "Both";
	
	public static final String WS_ADDRESSING_DT_NAMESPACE = "http://www.sap.com/710/soap/features/WSAddressing/";
	public static final String WS_ADDRESSING_DT_PROPERTY_ENABLED = "enabled";
	public static final String WS_ADDRESSING_DT_PROPERTY_ENABLED_DEFAULT = "true";
	
	public static final String WS_ADDRESSING_DT_OPERATION_NAMESPACE = "http://www.sap.com/710/soap/features/WSAddressing/";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_INPUTACTION = "InputAction";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_INPUTACTION_DEFAULT = "";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_OUTPUTACTION = "OutputAction";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_OUTPUTACTION_DEFAULT = "";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_FAULTACTION = "FaultAction";
	public static final String WS_ADDRESSING_DT_OPERATION_PROPERTY_FAULTACTION_DEFAULT = "";
	  
	public static final String XIENABLED_DT_NAMESPACE = BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI();
	public static final String XIENABLED_DT_PROPERTY_SOAPAPPLICATION = BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart();
	public static final String XIENABLED_DT_PROPERTY_SOAPAPPLICATION_DEFAULT = BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_XI_VALUE;
		
	public static final String CENTRAL_ADMINISTRATION_RT_NAMESPACE = "http://www.sap.com/webas/700/soap/features/CentralAdministration/";
	public static final String CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILENAME = "ProfileName";
	public static final String CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILENAME_DEFAULT = "";
	public static final String CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILEVERSION = "ProfileVersion";
	public static final String CENTRAL_ADMINISTRATION_RT_PROPERTY_PROFILEVERSION_DEFAULT = "";
	
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_NAMESPACE = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART
		= "IncomingSignature.ExpectedSignedElement.Operation.MessagePart";
	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT
		= {"wssp:Body()", "wssp:Header(*)"};
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART
	    = "IncomingEncryption.ExpectedEncryptedElement.Operation.MessagePart";
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT
		= "wssp:Body()";
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART
	    = "OutgoingSignature.SignedElement.Operation.MessagePart";
	public static final String[] TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTOPERATIONMESSAGEPART_DEFAULT
		= {"wssp:Body()", "wssp:Header(*)"}; 
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART
	    = "OutgoingEncryption.EncryptedElement.Operation.MessagePart";
	public static final String TRANSPORT_GUARANTEE_RT_OPERATION_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTOPERATIONMESSAGEPART_DEFAULT
		= "wssp:Body()";
	
	public static final String AUTHENTICATION_RT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/authentication/";
//	public static final String AUTHENTICATION_RT_PROPERTY_SINGLESIGNON = "SingleSignOn";
//	public static final String AUTHENTICATION_RT_PROPERTY_SINGLESIGNON_DEFAULT = "false";
	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHOD = "AuthenticationMethod";
	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHOD_DEFAULT = "sapsp:None";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD
//		= "AuthenticationMethod.SAML.SubjectConfirmationMethod";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSUBJECTCONFIRMATIONMETHOD_DEFAULT
//		= "sapsp:SenderVouchesWithXMLSignature";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLISSUER = "AuthenticationMethod.SAML.Issuer";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLISSUER_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME = "AuthenticationMethod.SAML.SenderVouches.AttesterName";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLSENDERVOUCHESATTESTERNAME_DEFAULT = "saml_default_attester";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY = "AuthenticationMethod.SAML.AssertionValidity";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLASSERTIONVALIDITY_DEFAULT = "180";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION = "AuthenticationMethod.SAML.DoNotCacheCondition";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLDONOTCACHECONDITION_DEFAULT = "true";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION
//	    = "AuthenticationMethod.SAML.HolderOfKey.AttesterSystemDestination";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERSYSTEMDESTINATION_DEFAULT = "WS_SAML_attester_default";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME
//	    = "AuthenticationMethod.SAML.HolderOfKey.AttesterName";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODSAMLHOLDEROFKEYATTESTERNAME_DEFAULT
//		= "saml_default_attester";
		
	//TODO check this ****************************
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICUSERNAME = "AuthenticationMethod.Basic.Username";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICUSERNAME_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICPASSWORD = "AuthenticationMethod.Basic.Password";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODBASICPASSWORD_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREVIEW = "AuthenticationMethod.X509.Keystore.View";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREVIEW_DEFAULT = "WebServiceSecurity";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREALIAS = "AuthenticationMethod.X509.Keystore.Alias";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODX509KEYSTOREALIAS_DEFAULT = "System-key";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME
//	    = "AuthenticationMethod.ABAPServiceUser.Password";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERUSERNAME_DEFAULT = "";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE
//	    = "AuthenticationMethod.ABAPServiceUser.Language";
//	public static final String AUTHENTICATION_RT_PROPERTY_AUTHENTICATIONMETHODABAPSERVICEUSERLANGUAGE_DEFAULT = "";
	//TODO check this ****************************
	
	public static final String WS_ADDRESSING_RT_NAMESPACE = "http://www.sap.com/710/soap/features/WSAddressing/";
	public static final String WS_ADDRESSING_RT_PROPERTY_WSAPROTOCOL = "WSAProtocol";
	public static final String WS_ADDRESSING_RT_PROPERTY_WSAPROTOCOL_DEFAULT = "http://www.w3.org/2005/03/addressing";
//	public static final String WS_ADDRESSING_RT_PROPERTY_REFERENCEPARAMETERS = "ReferenceParameters";
//	public static final String WS_ADDRESSING_RT_PROPERTY_REFERENCEPARAMETERS_DEFAULT = "";

	public static final String REL_MESSAGING_RT_NAMESPACE = "http://www.sap.com/710/soap/features/reliableMessaging/";
	public static final String REL_MESSAGING_RT_PROPERTY_RMPROTOCOL = "RMprotocol";
	public static final String REL_MESSAGING_RT_PROPERTY_RMPROTOCOL_DEFAULT = "http://schemas.xmlsoap.org/ws/2005/02/rm";
	public static final String REL_MESSAGING_RT_PROPERTY_RETRANSMISSIONINTERVAL = "RetransmissionInterval";
	public static final String REL_MESSAGING_RT_PROPERTY_RETRANSMISSIONINTERVAL_DEFAULT = "600";
	public static final String REL_MESSAGING_RT_PROPERTY_INACTIVITYTIMEOUT = "InactivityTimeout";
	public static final String REL_MESSAGING_RT_PROPERTY_INACTIVITYTIMEOUT_DEFAULT = "0";
	public static final String REL_MESSAGING_RT_PROPERTY_ACKINTERVAL = "AckInterval";
	public static final String REL_MESSAGING_RT_PROPERTY_ACKINTERVAL_DEFAULT = "600";
	public static final String REL_MESSAGING_RT_PROPERTY_SEQUENCELIFETIME = "SequenceLifetime";
	public static final String REL_MESSAGING_RT_PROPERTY_SEQUENCELIFETIME_DEFAULT = "0";

	public static final String MESSAGE_ID_RT_NAMESPACE = "http://www.sap.com/webas/640/soap/features/messageId/";
	public static final String MESSAGE_ID_RT_PROPERTY_MESSAGEIDPROTOCOL = "MessageIdProtocol";
	public static final String MESSAGE_ID_RT_PROPERTY_MESSAGEIDPROTOCOL_DEFAULT = "http://www.w3.org/2005/03/addressing";

	public static final String SESSION_HANDLING_RT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/session/";
	public static final String SESSION_HANDLING_RT_PROPERTY_SESSIONMETHOD = "SessionMethod";
	public static final String SESSION_HANDLING_RT_PROPERTY_SESSIONMETHOD_DEFAULT = "httpCookies";

	public static final String TRANSPORT_BINDING_RT_NAMESPACE = "http://www.sap.com/webas/710/soap/features/transportbinding/";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPATH = "URLPath";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPATH_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPROTOCOL = "URLProtocol";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPROTOCOL_DEFAULT = "http";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLHOST = "URLHost";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLHOST_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPORT = "URLPort";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLPORT_DEFAULT = "80";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLCLIENT = "URLClient";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLCLIENT_DEFAULT = "000";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLLANGUAGE = "URLLanguage";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_URLLANGUAGE_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYHOST = "ProxyHost";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYHOST_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYPORT = "ProxyPort";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYPORT_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYUSER = "ProxyUser";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYUSER_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYPASSWORD = "ProxyPassword";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_PROXYPASSWORD_DEFAULT = "";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTHOST = "altHost";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTHOST_DEFAULT = "";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTPORT = "altPort";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTPORT_DEFAULT = "";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTPATH = "altPath";
	public static final String TRANSPORT_BINDING_RT_PROPERTY_ALTPATH_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CALCPATH = "calcPath";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CALCPATH_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CALCPROTOCOL = "calcProtocol";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CALCPROTOCOL_DEFAULT = "http";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_DESTINATION = "Destination";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_DESTINATION_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_DESTINATIONPATH = "DestinationPath";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_DESTINATIONPATH_DEFAULT = "";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_LOCALCALL = "LocalCall";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_LOCALCALL_DEFAULT = "false";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_STYLE = "Style";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_STYLE_DEFAULT = "Document";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_TYPE = "Type";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_TYPE_DEFAULT = "http://schemas.xmlsoap.org/soap/http";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_SERVICESESSIONTIMEOUT = "ServiceSessionTimeout";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_SERVICESESSIONTIMEOUT_DEFAULT = "0";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CONSUMERMAXWAITTIME = "ConsumerMaxWaitTime";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CONSUMERMAXWAITTIME_DEFAULT = "0";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_OPTIMIZEDXMLTRANSFER = "OptimizedXMLTransfer";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_OPTIMIZEDXMLTRANSFER_DEFAULT = "None";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_COMPRESSREQUEST = "compressRequest";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_COMPRESSREQUEST_DEFAULT = "false";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_COMPRESSRESPONSE = "compressResponse";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_COMPRESSRESPONSE_DEFAULT = "true";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_KEEPALIVESTATUS = "keepAliveStatus";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_KEEPALIVESTATUS_DEFAULT = "true";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_SOCKETTIMEOUT = "socketTimeout";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_SOCKETTIMEOUT_DEFAULT = "60000";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CHUNKEDREQUEST = "chunkedRequest";
//	public static final String TRANSPORT_BINDING_RT_PROPERTY_CHUNKEDREQUEST_DEFAULT = "false";

//	public static final String TRANSPORT_BINDING_RT_OPERATION_NAMESPACE = "http://www.sap.com/webas/710/soap/features/transportbinding/";
//	public static final String TRANSPORT_BINDING_RT_OPERATION_PROPERTY_SOAPACTION = "SOAPAction";
//	public static final String TRANSPORT_BINDING_RT_OPERATION_PROPERTY_SOAPACTION_DEFAULT = "";

//	public static final String XI_30_RUNTIME_INTEGRATION_RT_NAMESPACE = "http://www.sap.com/710/features/xi/integration/";
//	public static final String XI_30_RUNTIME_INTEGRATION_RT_PROPERTY_RUNTIMEENVIRONMENT = "RuntimeEnvironment";
//	public static final String XI_30_RUNTIME_INTEGRATION_RT_PROPERTY_RUNTIMEENVIRONMENT_DEFAULT = "WS";

//	public static final String ATTACHMENT_HANDLING_RT_NAMESPACE = "http://www.sap.com/710/features/attachment/";
//	public static final String ATTACHMENT_HANDLING_RT_PROPERTY_ENABLED = "Enabled";
//	public static final String ATTACHMENT_HANDLING_RT_PROPERTY_ENABLED_DEFAULT = "false";

//	public static final String EXTERNAL_ASSERTION_RT_NAMESPACE = "http://www.sap.com/webas/710/soap/features/external/assertion/";
//	public static final String EXTERNAL_ASSERTION_RT_PROPERTY_ASSERTIONROOTNAME = "assertionRootName";
//	public static final String EXTERNAL_ASSERTION_RT_PROPERTY_ASSERTIONROOTNAME_DEFAULT = "";

	public static final String TRANSPORT_GUARANTEE_RT_NAMESPACE = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SECURITYMECHANISM = "SecurityMechanism";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SECURITYMECHANISM_DEFAULT = "sapsp:HTTP";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_TLSTYPE = "TLSType";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_TLSTYPE_DEFAULT = "sapsp:HTTP";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSIGNORESSLSERVERCERTS
//	    = "SSLServerCerts.IgnoreSSLServerCerts";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSIGNORESSLSERVERCERTS_DEFAULT
//    = "true";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW
//	    = "SSLServerCerts.AcceptedServerCerts.Keystore.View";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SSLSERVERCERTSACCEPTEDSERVERCERTSKEYSTOREVIEW_DEFAULT
//    = "WebServiceSecurity";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SECURECONVERSATION = "SecureConversation";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SECURECONVERSATION_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURE = "IncomingSignature";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURE_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW
	    = "IncomingSignature.Trustanchor.Keystore.View";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORKEYSTOREVIEW_DEFAULT
    = "WebServiceSecurity";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN
//	    = "IncomingSignature.Trustanchor.CertificatePattern";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATURETRUSTANCHORCERTIFICATEPATTERN_DEFAULT
//    = "Subject=*;Issuer=*;SerialNumber=*";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART
	    = "IncomingSignature.ExpectedSignedElement.MessagePart";
	public static final String[] TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGSIGNATUREEXPECTEDSIGNEDELEMENTMESSAGEPART_DEFAULT
    = {"wssp:Body()", "wssp:Header(*)"};
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTION_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTION = "IncomingEncryption";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART
	    = "IncomingEncryption.ExpectedEncryptedElement.MessagePart";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGENCRYPTIONEXPECTEDENCRYPTEDELEMENTMESSAGEPART_DEFAULT
    = "wssp:Body()";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURE = "OutgoingSignature";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURE_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW
	    = "OutgoingSignature.SigningKey.Keystore.View";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREVIEW_DEFAULT
    = "WebServiceSecurity";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS
	    = "OutgoingSignature.SigningKey.Keystore.Alias";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNINGKEYKEYSTOREALIAS_DEFAULT
    = "System-key";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART
	    = "OutgoingSignature.SignedElement.MessagePart";
	public static final String[] TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGSIGNATURESIGNEDELEMENTMESSAGEPART_DEFAULT
    = {"wssp:Body()", "wssp:Header(*)"};
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTION = "OutgoingEncryption";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTION_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW
	    = "OutgoingEncryption.EncryptingKey.Keystore.View";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREVIEW_DEFAULT
    = "WebServiceSecurity_Certs";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS
	    = "OutgoingEncryption.EncryptingKey.Keystore.Alias";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYKEYSTOREALIAS_DEFAULT
    = "";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN
	    = "OutgoingEncryption.EncryptingKey.Origin";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTINGKEYORIGIN_DEFAULT
    = "sapsp:UseSignatureCertificate";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART
	    = "OutgoingEncryption.EncryptedElement.MessagePart";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_OUTGOINGENCRYPTIONENCRYPTEDELEMENTMESSAGEPART_DEFAULT
    = "wssp:Body()";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGE = "IncomingMessageAge";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGE_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGEAGE
	    = "IncomingMessageAge.Age";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCOMINGMESSAGEAGEAGE_DEFAULT
    = "180";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR
//	    = "SOAPErrorBehavior.ApplySecuritySettingsFor";
//	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_SOAPERRORBEHAVIORAPPLYSECURITYSETTINGSFOR_DEFAULT
//    = "sapsp:NoSOAPFaults";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCLUDETIMESTAMP = "IncludeTimestamp";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_INCLUDETIMESTAMP_DEFAULT = "false";
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_ALGORITHMSUITE = "AlgorithmSuite";	
	public static final String TRANSPORT_GUARANTEE_RT_PROPERTY_ALGORITHMSUITE_DEFAULT = "Basic128Rsa15";	
}
