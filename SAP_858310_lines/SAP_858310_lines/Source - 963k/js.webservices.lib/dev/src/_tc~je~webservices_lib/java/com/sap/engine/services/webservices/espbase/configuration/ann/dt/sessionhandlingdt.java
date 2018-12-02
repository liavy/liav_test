package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Type level properties of a session oriented communication, 
 * which keeps the inner state of the provider side. This kind of communication is only possible in conjunction with the 
 * WSDL message exchange pattern �request-response� (synchronous Web Service call).
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/session/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionHandlingDT
{
  /**
   * Attribute, which defines whether a stateful communication for a Web Service call is performed, 
   * i.e. if the inner status on the provider side is kept over multiple calls.
   * Name of the coresponding property name is <tt>enableSession</tt>.
   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
   */
  boolean enableSession() default false;
}
