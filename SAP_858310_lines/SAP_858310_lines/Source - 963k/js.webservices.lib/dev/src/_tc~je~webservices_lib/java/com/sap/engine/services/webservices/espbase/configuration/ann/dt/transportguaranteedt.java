package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Type level security settings of data transport.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/transportguarantee/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransportGuaranteeDT                                                             
{
  /**
   * Attribute for minimal transport guarantee level.
   * Name of the coresponding property name is <tt>Level</tt>.
   * Possible values for the attribute are: <tt>TransportGuaranteeEnumsLevel.NONE</tt>, <tt>TransportGuaranteeEnumsLevel.INTEGRITY</tt>, 
   * <tt>TransportGuaranteeEnumsLevel.CONFIDENTIALITY</tt> and <tt>TransportGuaranteeEnumsLevel.BOTH</tt>. 
   * Coresponding property values are: <tt>"None"</tt>, <tt>"Integrity"</tt>, <tt>"Confidentiality"</tt> and <tt>"Both"</tt>.
   */
	TransportGuaranteeEnumsLevel level() default TransportGuaranteeEnumsLevel.NONE;
}
