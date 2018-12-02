package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level properties for the central administration .
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/700/soap/features/CentralAdministration/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CentralAdministrationRT
{
  /**
   * Attribute, which defines name of the mass configuration profile.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>ProfileName</tt>.
   */
  String ProfileName() default "";
  
  /**
   * Attribute, which defines version of the mass configuration profile.
   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>ProfileVersion</tt>.
   * Possible values for the attribute are: <tt>Integer</tt>.
   */
  int ProfileVersion() default 0;
}
