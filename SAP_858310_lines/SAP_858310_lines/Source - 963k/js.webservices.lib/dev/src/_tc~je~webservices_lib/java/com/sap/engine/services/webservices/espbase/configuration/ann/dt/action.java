package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Operation level WS addressing.This annotattion is equivalent to
 * <tt>@javax.xml.ws.Action</tt> na in the future (JAX-WS 2.1) should be replaced with it. 
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/710/soap/features/WSAddressing/</tt>.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Action
{
  /**
   * Attribute, which defines action string for SOAP input message.
   * Name of the coresponding property name is <tt>InputAction</tt>.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   */
  String input() default "";

  /**
   * Attribute, which defines action string for SOAP output message.
   * Name of the coresponding property name is <tt>OutputAction</tt>.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   */
  String output() default "";
  
  /**
   * Attribute, which defines action string for SOAP fault message.
   * Name of the coresponding property name is <tt>FaultAction</tt>.
   * Attribute is optional and if the value is empty array, coresponding properties are not generated.
   */
  FaultAction[] fault() default {};
}
