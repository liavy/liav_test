package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level WS Addressing.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/710/soap/features/WSAddressing/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WSAddressingRT
{
  /**
   * Attribute, which defines protocol for WS addressing.
   * Attribute is optional and if the value is <tt>"http://www.w3.org/2005/03/addressing"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>WSAProtocol</tt>.
   * Possible values for the attribute are: <tt>"http://www.w3.org/2005/03/addressing"</tt>.
   */
  String WSAProtocol() default "http://www.w3.org/2005/03/addressing";
  
//  /**
//   * Attribute, which defines action strings, which are provided by server and send as reference parameters via SOAP header.
//   * Attribute is optional and if the value is array with <tt>""</tt>, coresponding properties are not generated.
//   * Name of the coresponding property name is <tt>ReferenceParameters</tt>.
//   */
//  String[] ReferenceParameters() default {""};
}
