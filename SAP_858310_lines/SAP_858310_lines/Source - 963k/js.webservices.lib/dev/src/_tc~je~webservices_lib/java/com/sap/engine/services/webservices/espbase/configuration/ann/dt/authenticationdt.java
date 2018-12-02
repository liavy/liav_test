package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Design-Time Type level security settings of authentication.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/630/soap/features/authentication/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticationDT
{
  /**
   * Attribute, which defines authentication level.
   * Name of the coresponding property name is <tt>AuthenticationLevel</tt>.
   * Possible values for the attribute are: <tt>AuthenticationEnumsAuthenticationLevel.NONE</tt>, 
   * <tt>AuthenticationEnumsAuthenticationLevel.BASIC</tt> and <tt>AuthenticationEnumsAuthenticationLevel.STRONG</tt>.
   * Coresponding property values are: <tt>"None"</tt>, <tt>"Basic"</tt> and <tt>"Strong"</tt>.
   */
	AuthenticationEnumsAuthenticationLevel authenticationLevel() default AuthenticationEnumsAuthenticationLevel.NONE;
}
