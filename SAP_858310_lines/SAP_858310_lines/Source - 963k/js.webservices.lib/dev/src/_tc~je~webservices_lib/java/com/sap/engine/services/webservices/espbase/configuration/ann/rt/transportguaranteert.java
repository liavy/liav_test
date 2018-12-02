package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level Security Settings of Data Transport. 
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/transportguarantee/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransportGuaranteeRT
{
//  /**
//   * Attribute for security mechanism (document security or HTTP security). 
//   * Attribute is optional and if the value is <tt>"sapsp:HTTP"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>SecurityMechanism</tt>.
//   * Possible values for the attribute are: <tt>"sapsp:HTTP"</tt> and <tt>"sapsp:WSSE"</tt>.
//   */
//  String SecurityMechanism() default "sapsp:HTTP";
  
  /**
   * Attribute for data transport protocol (e.g. HTTP).
   * Name of the coresponding property name is <tt>TLSType</tt>.
   * Possible values for the attribute are: <tt>"sapsp:HTTP"</tt> and <tt>"sapsp:HTTPS"</tt>.
   */
  String TLSType() default "sapsp:HTTP";

//  /**
//   * Attribute, which defines whether the client checks the trustworthiness of SSL server certificates during handshake.
//   * Attribute is optional and if the value is <tt>true</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>SSLServerCerts.IgnoreSSLServerCerts</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean SSLServerCertsIgnoreSSLServerCerts() default true;

//  /**
//   * Attribute, which defines whether SSL server certificates are accept in keystore.
//   * Attribute is optional and if the value is <tt>"WebServiceSecurity"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>SSLServerCerts.AcceptedServerCerts.Keystore.View</tt>.
//   */
//  String SSLServerCertsAcceptedServerCertsKeystoreView() default "WebServiceSecurity";
  
  /**
   * Attribute, which defines whether protocol providing Signature and Encryption using dynamically generated keys negotiated by the involved systems.
   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>SecureConversation</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean SecureConversation() default false;
  
  /**
   * Attribute, which defines whether an incoming signature is expected.
   * Name of the coresponding property name is <tt>IncomingSignature</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean IncomingSignature() default false;
  
  /**
   * Attribute, which defines whether keystore view/PSE containing certificates which are trusted for incoming signatures.
   * Attribute is optional and if the value is <tt>"WebServiceSecurity"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>IncomingSignature.Trustanchor.Keystore.View</tt>.
   */
  String IncomingSignatureTrustanchorKeystoreView() default "WebServiceSecurity";
  
//  /**
//   * Attribute, which defines patterns for trusted certificates.
//   * Attribute is optional and if the value is array with <tt>"Subject=*;Issuer=*;SerialNumber=*"</tt>, coresponding properties are not generated.
//   * Name of the coresponding property name is <tt>IncomingSignature.Trustanchor.CertificatePattern</tt>.
//   */
//  String[] IncomingSignatureTrustanchorCertificatePattern() default {"Subject=*;Issuer=*;SerialNumber=*"};
  
  /**
   * Attribute, which defines expected signed elements of message.
   * Attribute is optional and if the value is array with <tt>"wssp:Body()"</tt> and <tt>"wssp:Header(*)"</tt>, coresponding properties are not generated.
   * Name of the coresponding property name is <tt>IncomingSignature.ExpectedSignedElement.MessagePart</tt>.
   */
  String[] IncomingSignatureExpectedSignedElementMessagePart() default {"wssp:Body()", "wssp:Header(*)"};
  
  /**
   * Attribute, which defines whether an incoming encryption is expected.
   * Name of the coresponding property name is <tt>IncomingEncryption</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean IncomingEncryption() default false;
  
  /**
   * Attribute, which defines expected encrypted elements of message.
   * Attribute is optional and if the value is array with <tt>"wssp:Body()"</tt>, coresponding properties are not generated.
   * Name of the coresponding property name is <tt>IncomingEncryption.ExpectedEncryptedElement.MessagePart</tt>.
   * Possible values for the attribute are: <tt>"wssp:Body()"</tt> and <tt>"wssp:Header(*)"</tt>.
   */
  String[] IncomingEncryptionExpectedEncryptedElementMessagePart() default {"wssp:Body()"};
  
  /**
   * Attribute, which defines whether signature is added.
   * Name of the coresponding property name is <tt>OutgoingSignature</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean OutgoingSignature() default false;
  
  /**
   * Attribute, which defines whether keystore view/PSE containing signature key.
   * Attribute is optional and if the value is <tt>"WebServiceSecurity"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>OutgoingSignature.SigningKey.Keystore.View</tt>.
   */
  String OutgoingSignatureSigningKeyKeystoreView() default "WebServiceSecurity";
  
  /**
   * Attribute, which defines signature private key.
   * Attribute is optional and if the value is <tt>"System-key"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>OutgoingSignature.SigningKey.Keystore.Alias</tt>.
   */
  String OutgoingSignatureSigningKeyKeystoreAlias() default "System-key";
  
  /**
   * Attribute, which defines elements of message to sign.
   * Attribute is optional and if the value is array with <tt>"wssp:Body()"</tt> and <tt>"wssp:Header(*)"</tt>, coresponding properties are not generated.
   * Name of the coresponding property name is <tt>OutgoingSignature.SignedElement.MessagePart</tt>.
   * Possible values for the attribute are: <tt>"wssp:Body()"</tt>, <tt>"wssp:Header(wsse:Security/wsu:Timestamp)"</tt> and <tt>"wssp:Header(*)"</tt>.
   */
  String[] OutgoingSignatureSignedElementMessagePart() default {"wssp:Body()", "wssp:Header(*)"};
  
  /**
   * Attribute, which defines whether Encryption is added.
   * Name of the coresponding property name is <tt>OutgoingEncryption</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean OutgoingEncryption() default false;
  
  /**
   * Attribute, which defines whether keystore view/PSE containing encryption key.
   * Attribute is optional and if the value is <tt>"WebServiceSecurity_Certs"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>OutgoingEncryption.EncryptingKey.Keystore.View</tt>.
   */
  String OutgoingEncryptionEncryptingKeyKeystoreView() default "WebServiceSecurity_Certs";
  
  /**
   * Attribute, which defines X.500 name of encrypting private key inside PSE.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>OutgoingEncryption.EncryptingKey.Keystore.Alias</tt>.
   */
  String OutgoingEncryptionEncryptingKeyKeystoreAlias() default "";
  
  /**
   * Attribute, which defines origin of key.
   * Attribute is optional and if the value is <tt>"sapsp:UseSignatureCertificate"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>OutgoingEncryption.EncryptingKey.Origin</tt>.
   * Possible values for the attribute are: <tt>"sapsp:UserAssignedCertificate"</tt> and <tt>"sapsp:UseSignatureCertificate"</tt>.
   */
  String OutgoingEncryptionEncryptingKeyOrigin() default "sapsp:UseSignatureCertificate";

  /**
   * Attribute, which defines elements of message to encrypt.  
   * Attribute is optional and if the value is Array with <tt>"wssp:Body()"</tt>, coresponding properties are not generated.
   * Name of the coresponding property name is <tt>OutgoingEncryption.EncryptedElement.MessagePart</tt>.
   * Possible values for the attribute are: <tt>"wssp:Body()"</tt>, <tt<"wssp:Header(*)"</tt> and <tt>"wssp:Header(wsse:Security/*)"</tt>.
   */
  String[] OutgoingEncryptionEncryptedElementMessagePart() default {"wssp:Body()"};
  
//  /**
//   * Attribute, which defines whether message age is required.
//   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>IncomingMessageAge</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>true</tt>.
//   */
//  boolean IncomingMessageAge() default false;
  
  /**
   * Attribute, which defines maximum message age in seconds within message is still proc-essed.
   * Attribute is optional and if the value is <tt>180</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>IncomingMessageAge.Age</tt>.
   * Possible values for the attribute are: <tt>Integer</tt>.
   */
  int IncomingMessageAgeAge() default 180;
  
//  /**
//   * Attribute, which defines whther security settings should be applied in case of SOAP Faults.
//   * Attribute is optional and if the value is <tt>"sapsp:NoSOAPFaults"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>SOAPErrorBehavior.ApplySecuritySettingsFor</tt>.
//   * Possible values for the attribute are: <tt>"sapsp:AllSOAPFaults"</tt>, <tt>"sapsp:NoSOAPFaults"</tt>, <tt>"sapsp:TechSOAPFaults"</tt> and <tt>"sapsp:AppSOAPFaults"</tt>.
//   */
//  String SOAPErrorBehaviorApplySecuritySettingsFor() default "sapsp:NoSOAPFaults";
  
  /**
   * Attribute, which defines whether timestamps are used.
   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>IncludeTimestamp</tt>.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean IncludeTimestamp() default false;
  
  /**
   * Attribute, which defines whether algorithm suite is used.
   * Attribute is optional and if the value is <tt>"Basic128Rsa15"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>AlgorithmSuite</tt>.
   * Possible values for the attribute are: <tt>"Basic256Rsa15"</tt>, <tt>"Basic192Rsa15"</tt>, <tt>"Basic128Rsa15"</tt>, 
   * <tt>"TripleDesRsa15"</tt>, <tt>"Basic256Sha256Rsa15"</tt>, <tt>"Basic192Sha256Rsa15"</tt>, <tt>"Basic128Sha256Rsa15"</tt> and <tt>"TripleDesSha256Rsa1"</tt>.
   */
  String AlgorithmSuite() default "Basic128Rsa15";  
}
