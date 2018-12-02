package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Operation level reliable messaging exchange.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/NW05/soap/features/wsrm/</tt>.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelMessagingNW05DTOperation
{
  /**
   * Attribute, which defines enabling of reliable messaging.
   * Name of the coresponding property name is <tt>enableWSRM</tt>.
   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean enableWSRM() default false;
}
