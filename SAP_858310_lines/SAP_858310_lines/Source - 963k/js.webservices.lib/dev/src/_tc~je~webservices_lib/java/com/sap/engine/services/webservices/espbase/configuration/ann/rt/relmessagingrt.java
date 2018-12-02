package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level reliable messaging exchange.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/710/soap/features/reliableMessaging/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelMessagingRT
{
  /**
   * Attribute, which defines protocol for reliable messaging.
   * Attribute is optional and if the value is <tt>"http://schemas.xmlsoap.org/ws/2005/02/rm"</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>RMprotocol</tt>.
   * Possible values for the attribute are: <tt>"http://schemas.xmlsoap.org/ws/2005/02/rm"</tt>.
   */
  String RMprotocol() default "http://schemas.xmlsoap.org/ws/2005/02/rm";
  
  /**
   * Attribute, which defines the interval for retransmission of a message in seconds.
   * Attribute is optional and if the value is <tt>42000</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>RetransmissionInterval</tt>.
   * Possible values for the attribute are: <tt>Integer >= 0, (0 = no retransmission)</tt>.
   */
  int RetransmissionInterval() default 42000;
  
  /**
   * Attribute, which defines some time interval in seconds within the termination of a sequence will not be performed.
   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>InactivityTimeout</tt>.
   * Possible values for the attribute are: <tt>Integer >= 0, (0 = infinite)</tt>.
   */
  int InactivityTimeout() default 0;
  
  /**
   * Attribute, which defines a time interval for sending seperate acknowledgements.
   * Attribute is optional and if the value is <tt>600</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>AckInterval</tt>.
   * Possible values for the attribute are: <tt>Integer >= 0, (0 = no async sending of acks)</tt>.
   */
  int AckInterval() default 600;
  
  /**
   * Attribute, which defines the lifetime of a message sequence.
   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>SequenceLifetime</tt>.
   * Possible values for the attribute are: <tt>Integer >= 0, (0 = infinite)</tt>.
   */
  int SequenceLifetime() default 0;
}
