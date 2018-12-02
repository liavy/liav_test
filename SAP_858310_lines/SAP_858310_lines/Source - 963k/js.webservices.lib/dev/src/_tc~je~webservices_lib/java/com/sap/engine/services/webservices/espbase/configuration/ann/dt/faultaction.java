package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotattion is equivalent to
 * <tt>@javax.xml.ws.FaultAction</tt> na in the future (JAX-WS 2.1) should be replaced with it.
 *
 *
 * The <code>FaultAction</code> annotation is used inside an {@link Action}
 * annotation to allow an explicit association of a WS-Addressing 
 * <code>Action</code> message addressing property with the <code>fault</code> 
 * messages of the WSDL operation mapped from the exception class.
 * <p>
 * In this version of JAX-WS there is no standard way to specify 
 * <code>Action</code> values in a WSDL and there is no standard default value.  It is intended that, 
 * after the W3C WG on WS-Addressing has defined these items in a recommendation,
 * a future version of JAX-WS will require the new standards.
 * 
 * @see Action
 *
 * @since JAX-WS 2.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FaultAction
{
    /**
     * Name of the exception class
     */
    Class className();

    /**
     * Value of WS-Addressing <code>Action</code> message addressing property for the exception
     */
    String value() default "";
}
