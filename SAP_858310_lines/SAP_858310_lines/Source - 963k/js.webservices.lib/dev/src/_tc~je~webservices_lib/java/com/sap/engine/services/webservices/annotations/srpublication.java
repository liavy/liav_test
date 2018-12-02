package com.sap.engine.services.webservices.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation used in a webservice implementation class that points to the
 * corresponding classifications file. The classifications file contains data 
 * that allow publication of the service to the services registry
 * during deployment time.
 * @author d042817 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SrPublication
{
	/** Provides the jar-relative path that points to the classifications
	 * file. 
	 * @return the classifications file location that is relative inside of
	 * the jar the file is situated in */
	String location();
}
