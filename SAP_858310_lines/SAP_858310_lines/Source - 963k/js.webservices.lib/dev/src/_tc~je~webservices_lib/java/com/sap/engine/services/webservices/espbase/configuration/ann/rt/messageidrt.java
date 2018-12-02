package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level message identificator.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/640/soap/features/messageId/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageIDRT
{
  /**
   * Attribute, which defines name of used message ID protocol. This is used when a new client proxy is communicating with an old runtime environment.
   * Attribute is optional and if the value is <tt>"http://www.w3.org/2005/03/addressing"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>MessageIdProtocol</tt>.
   * Possible values for the attribute are: <tt>"http://www.sap.com/webas/640/soap/features/messageId/"</tt>, 
   * <tt>"http://www.w3.org/2005/03/addressing"</tt> and <tt>"suppressTransfer"</tt>.
   */
  String MessageIdProtocol() default "http://www.w3.org/2005/03/addressing";
}
