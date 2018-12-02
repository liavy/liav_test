package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level Security Settings of Authentication.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/authentication/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticationRT
{
//  /**
//   * Attribute, which defines whether single sign on is used.
//   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>SingleSignOn</tt>.
//   * Possible values for the attribute are: <tt>false</tt> and <tt>true</tt>.
//   */
//  boolean SingleSignOn() default false;
  
  /**
   * Attribute, which defines authentication method.
   * Name of the coresponding property name is <tt>AuthenticationMethod</tt>.
   * Possible values for the attribute are: <tt>"sapsp:None"</tt>, <tt>"sapsp:HTTPBasic"</tt>, <tt>"sapsp:HTTPDigest"</tt>, <tt>"sapsp:UsernameTokenDigest"</tt>, 
   * <tt>"sapsp:HTTPX509"</tt>, <tt>"sapsp:HTTPSSO2"</tt>, <tt>"wsse:UsernameToken"</tt>, <tt>"wsse:SAMLAssertion"</tt>, <tt>"wsse:X509v3"</tt>, 
   * <tt>"sapsp:SAMLAssertionPrincipalPropagation"</tt> and <tt>"sapsp:STS"</tt>.
   */
  String[] AuthenticationMethod() default {"sapsp:None"};
  
//  /**
//   * Attribute, which defines method to confirm the trustworthiness of the subject to authenticate.
//   * Attribute is optional and if the value is <tt>"sapsp:SenderVouchesWithXMLSignature"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.SubjectConfirmationMethod</tt>.
//   * Possible values for the attribute are: <tt>"sapsp:SenderVouchesWithXMLSignature"</tt> and <tt>"urn:oasis:names:tc:SAML:1.0:cm:holder-of-key"</tt>.
//   */
//  String AuthenticationMethodSAMLSubjectConfirmationMethod() default "sapsp:SenderVouchesWithXMLSignature";
  
//  /**
//   * Attribute, which defines issuer name.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.Issuer</tt>.
//   */
//  String AuthenticationMethodSAMLIssuer() default "";
  
//  /**
//   * Attribute, which defines attester name.
//   * Attribute is optional and if the value is <tt>"saml_default_attester"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.SenderVouches.AttesterName</tt>.
//   */
//  String AuthenticationMethodSAMLSenderVouchesAttesterName() default "saml_default_attester";
  
//  /**
//   * Attribute, which defines validity of SAML assertion in seconds.
//   * Attribute is optional and if the value is <tt>"180"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.AssertionValidity</tt>.
//   */
//  String AuthenticationMethodSAMLAssertionValidity() default "180";
  
//  /**
//   * Attribute, which defines whether prohibit caching of SAML assertions.
//   * Attribute is optional and if the value is <tt>true</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.DoNotCacheCondition</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean AuthenticationMethodSAMLDoNotCacheCondition() default true;

//  /**
//   * Attribute, which defines destination to attesting system which contains attester that can do the subject confirmation.
//   * Attribute is optional and if the value is <tt>"WS_SAML_attester_default"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.HolderOfKey.AttesterSystemDestination</tt>.
//   */
//  String AuthenticationMethodSAMLHolderOfKeyAttesterSystemDestination() default "WS_SAML_attester_default";
  
//  /**
//   * Attribute, which defines destination to attesting system which contains attester that can do the subject confirmation.
//   * Attribute is optional and if the value is <tt>"saml_default_attester"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.SAML.HolderOfKey.AttesterName</tt>.
//   */
//  String AuthenticationMethodSAMLHolderOfKeyAttesterName() default "saml_default_attester";
  
//  /**
//   * Attribute, which defines user for authentication.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.Basic.Username</tt>.
//   */
//  String AuthenticationMethodBasicUsername() default "";
  
//  /**
//   * Attribute, which defines password for authentication.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.Basic.Password</tt>.
//   */
//  String AuthenticationMethodBasicPassword() default "";
  
//  /**
//   * Attribute, which defines keystore/PSE for X.509 certificate used for authentication.
//   * Attribute is optional and if the value is <tt>"WebServiceSecurity"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.X509.Keystore.View</tt>.
//   */
//  String AuthenticationMethodX509KeystoreView() default "WebServiceSecurity";
  
//  /**
//   * Attribute, which defines X.509 certificate used for authentication.
//   * Attribute is optional and if the value is <tt>"System-key"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.X509.Keystore.Alias</tt>.
//   */
//  String AuthenticationMethodX509KeystoreAlias() default "System-key";
  
//  /**
//   * Attribute, which defines username for logon to ABAP system to process document security.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.ABAPServiceUser.Password</tt>.
//   */
//  String AuthenticationMethodABAPServiceUserUsername() default "";

//  /**
//   * Attribute, which defines language for logon to ABAP system to process document security.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>AuthenticationMethod.ABAPServiceUser.Language</tt>.
//   */
//  String AuthenticationMethodABAPServiceUserLanguage() default "";
}
