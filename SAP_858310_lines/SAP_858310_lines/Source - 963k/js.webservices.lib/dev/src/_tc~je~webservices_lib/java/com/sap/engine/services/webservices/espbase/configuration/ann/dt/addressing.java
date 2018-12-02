package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Type level WS addressing. This annotattion is equivalent to
 * <tt>@javax.xml.ws.soap.Addressing</tt> na in the future (JAX-WS 2.1) should be replaced with it.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/710/soap/features/WSAddressing/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Addressing
{
  /**
   * Attribute, which defines whether do a WS address transfer.
   * Name of the coresponding property name is <tt>enabled</tt>.
   * Attribute is optional and if the value is <tt>true</tt>, coresponding property is not generated.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean enabled() default true;
}
