package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level properties of a session oriented communication, 
 * which keeps the inner state of the provider side. This kind of communication is only possible in conjunction with the 
 * WSDL message exchange pattern �request-response� (synchronous Web Service call).
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/session/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionHandlingRT
{
  /**
   * Attribute, which defines mechanism for realisation of stateful communication links.
   * Attribute is optional and if the value is <tt>"httpCookies"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>SessionMethod</tt>.
   * Possible values for the attribute are: <tt>"httpCookies"</tt>.
   */
  String SessionMethod() default "httpCookies";
}
